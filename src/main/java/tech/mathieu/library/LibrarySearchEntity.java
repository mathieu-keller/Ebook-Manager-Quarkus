package tech.mathieu.library;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "V_LIBRARY_SEARCH")
public class LibrarySearchEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  @Column(name = "ID", updatable = false, insertable = false, nullable = false)
  Long id;

  @Column(name = "TITLE", updatable = false, insertable = false, nullable = false)
  String title;

  @Column(name = "SEARCH_TERMS", updatable = false, insertable = false, nullable = false)
  String searchTerms;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSearchTerms() {
    return searchTerms;
  }

  public void setSearchTerms(String searchTerms) {
    this.searchTerms = searchTerms;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LibrarySearchEntity that)) {
      return false;
    }
    return Objects.equals(id, that.id)
        && Objects.equals(title, that.title)
        && Objects.equals(searchTerms, that.searchTerms);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, searchTerms);
  }
}
