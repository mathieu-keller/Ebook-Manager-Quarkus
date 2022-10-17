package tech.mathieu.book;

import tech.mathieu.collection.CollectionService;
import tech.mathieu.contributor.ContributorService;
import tech.mathieu.creator.CreatorDto;
import tech.mathieu.creator.CreatorService;
import tech.mathieu.epub.Reader;
import tech.mathieu.epub.opf.Opf;
import tech.mathieu.epub.opf.metadata.Date;
import tech.mathieu.epub.opf.metadata.Meta;
import tech.mathieu.identifier.IdentifierService;
import tech.mathieu.language.LanguageDto;
import tech.mathieu.language.LanguageService;
import tech.mathieu.publisher.PublisherDto;
import tech.mathieu.publisher.PublisherService;
import tech.mathieu.subject.SubjectDto;
import tech.mathieu.subject.SubjectService;
import tech.mathieu.title.TitleEntity;
import tech.mathieu.title.TitleService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

@ApplicationScoped
@Transactional
public class BookService {

  @Inject
  EntityManager entityManager;

  @Inject
  SubjectService subjectService;

  @Inject
  CreatorService creatorService;

  @Inject
  PublisherService publisherService;

  @Inject
  LanguageService languageService;

  @Inject
  ContributorService contributorService;

  @Inject
  IdentifierService identifierService;

  @Inject
  TitleService titleService;

  @Inject
  CollectionService collectionService;

  @Inject
  Reader reader;

  public BookEntity getBookById(Long id) {
    return entityManager.find(BookEntity.class, id);
  }

  public BookDto getBookDto(Long bookId) {
    var entity = entityManager.find(BookEntity.class, bookId);
    return getBookDto(entity);
  }

  public BookDto getBookDto(BookEntity entity) {
    return new BookDto(entity.getId(),
        entity.getTitleEntities().stream().map(TitleEntity::getTitle).collect(Collectors.joining(", ")),
        null,
        Optional.ofNullable(entity.getLanguageEntities())
            .map(languageEntities -> languageEntities
                .stream()
                .map(languageEntity -> new LanguageDto(languageEntity.getId(), languageEntity.getName()))
                .toList()).orElse(null),
        Optional.ofNullable(entity.getSubjectEntities())
            .map(subjectEntities -> subjectEntities
                .stream()
                .map(subjectEntity -> new SubjectDto(subjectEntity.getId(), subjectEntity.getName()))
                .toList()).orElse(null),
        Optional.ofNullable(entity.getPublisherEntities())
            .map(publisherEntities -> publisherEntities
                .stream()
                .map(publisherEntity -> new PublisherDto(publisherEntity.getId(), publisherEntity.getName()))
                .toList()).orElse(null),
        Optional.ofNullable(entity.getCover())
            .map(cover -> "data:image/jpg;base64," + new String(cover))
            .orElse(null),
        Optional.ofNullable(entity.getCreatorEntities())
            .map(creatorEntities -> creatorEntities
                .stream()
                .map(creatorEntity -> new CreatorDto(creatorEntity.getId(), creatorEntity.getName()))
                .toList()).orElse(null),
        entity.collectionEntity.getId(),
        entity.groupPosition
    );
  }

  public void uploadBook(InputStream in) {
    var uuid = String.valueOf(UUID.randomUUID());
    var inboxPath = "upload/inbox";
    new File(inboxPath).mkdirs();
    var inboxBookPath = inboxPath + "/" + uuid + ".epub";
    try {
      saveBookToInbox(in, inboxBookPath);
      processInbox(inboxBookPath);
    } catch (RuntimeException e) {
      new File(inboxBookPath).delete();
      throw e;
    }
  }

  private void processInbox(String inboxPath) {
    try {
      var zipFile = new ZipFile(inboxPath);
      var book = saveBook(zipFile);
      var destPath = new File(book.getPath());
      destPath.mkdirs();
      var dest = new File(destPath + "/orginal.epub");
      var result = new File(zipFile.getName()).renameTo(dest);
      if (!result) {
        throw new IOException("can't rename file " + zipFile.getName() + " to " + dest.getName());
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void saveBookToInbox(InputStream in, String inboxPath) {
    var file = new File(inboxPath);
    try (var fos = new FileOutputStream(file)) {
      int read;
      byte[] bytes = new byte[1024];
      while ((read = in.read(bytes)) != -1) {
        fos.write(bytes, 0, read);
      }
      fos.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private BookEntity saveBook(ZipFile zipFile) {
    try {
      var epub = reader.read(zipFile);
      var opf = epub.opf();
      var book = new BookEntity();
      var metaData = new HashMap<String, Map<String, Meta>>();
      epub.opf().getMetadata().getMeta()
          .stream()
          .filter(meta -> meta.getRefines() != null)
          .filter(meta -> meta.getProperty() != null)
          .forEach(meta -> {
            var existingId = metaData.get(meta.getRefines());
            if (existingId == null) {
              metaData.put(meta.getRefines(), new HashMap<>());
              existingId = metaData.get(meta.getRefines());
            }
            existingId.put(meta.getProperty(), meta);
          });
      book.setCover(epub.cover());
      book.setTitleEntities(titleService.getTitle(opf, metaData, book));
      book.setMeta(getMeta(opf));
      book.setDate(getDates(opf));
      book.setCreatorEntities(creatorService.getCreators(opf));
      book.setIdentifierEntities(identifierService.getIdentifiers(opf, book));
      book.setContributorEntities(contributorService.getContributors(opf));
      book.setLanguageEntities(languageService.getLanguages(opf));
      book.setPublisherEntities(publisherService.getPublishers(opf));
      book.setSubjectEntities(subjectService.getSubjects(opf));
      book.setPath("upload/ebooks/" + book.getTitleEntities()
          .stream()
          .map(TitleEntity::getTitle)
          .collect(Collectors.joining(", ")));
      var collection = collectionService.getCollection(epub, metaData);
      book.setCollectionEntity(collection.getLeft());
      book.setGroupPosition(collection.getRight());


      return entityManager.merge(book);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String getDates(Opf epub) {
    if (epub.getMetadata().getDates() != null) {
      return epub.getMetadata().getDates()
          .stream()
          .map(Date::getValue).
          collect(Collectors.joining(", "));
    }
    return null;
  }

  private String getMeta(Opf epub) {
    if (epub.getMetadata().getMeta() != null) {
      return epub.getMetadata().getMeta()
          .stream()
          .map(Meta::toString).
          collect(Collectors.joining(", "));
    }
    return null;
  }

}
