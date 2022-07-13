package eu.samcdonovan.data;

import javax.persistence.*;

/**
 * BookInstance class which maps book instance information (retrieved from
 * scraping) onto the book_instances table in the database
 */
@Entity
@Table(name = "book_instances")
public class BookInstance {

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Id
    @Column(name = "isbn")
    private String isbn;

    @Column(name = "format")
    private String format;

    /**
     * Empty BookInstance constructor for Hibernate
     */
    public BookInstance() {
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Override toString method, returns the book that is associated with thie
     * instance, the isbn and the books format
     *
     * @return string containing class information
     */
    @Override
    public String toString() {
        return "BookInstance:\n"
                + book + ", " + isbn + ", " + format;
    }

}
