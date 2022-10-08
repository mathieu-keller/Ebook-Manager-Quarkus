package tech.mathieu.ebook;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "publisher")
public class PublisherEntity {
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  @Id
  @Column(name = "id", nullable = false)
  int id;

  @Column(name = "name", nullable = true, length = 4000)
  String name;

  @ManyToMany(
      targetEntity = BookEntity.class,
      fetch = FetchType.LAZY,
      cascade = CascadeType.MERGE
  )
  @JoinTable(
      name = "publisher2book",
      joinColumns = @JoinColumn(name = "publisher_id"),
      inverseJoinColumns = @JoinColumn(name = "BOOK_ID")
  )
  List<BookEntity> bookEntities;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<BookEntity> getBookEntities() {
    return bookEntities;
  }

  public void setBookEntities(List<BookEntity> bookEntities) {
    this.bookEntities = bookEntities;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PublisherEntity that)) {
      return false;
    }
    return id == that.id && Objects.equals(name, that.name) && Objects.equals(bookEntities, that.bookEntities);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, bookEntities);
  }
}
