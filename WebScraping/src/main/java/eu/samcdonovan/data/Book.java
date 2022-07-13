package eu.samcdonovan.data;

import javax.persistence.*;

/**
 * Book class which stores all of the information scraped by the web scrapers.
 * The following fields are mapped onto the books table in the database: id,
 * title, description, genre, image, author. The other fields (isbn, format,
 * price, websiteUrl) are transient and are used only for transferring this data
 * to BookInstance and Comparison objects
 */
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private int id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "image")
    private String image;

    @Column(name = "author")
    private String author;

    @Transient
    private float price;

    @Transient
    private String websiteUrl;

    @Transient
    private String format;

    @Transient
    private String isbn;

    /**
     * Empty Book constructor for Hibernate
     */
    public Book() {
    }

    /**
     * Book constructor that sets all of its fields when a book is scraped. This
     * is used to access that data when putting books, book instances and
     * comparisons into the database
     *
     * @param title Title of the book
     * @param description Description of the book
     * @param image Image URL, used to get a display picture for the book
     * @param author Author of the book
     * @param isbn The ISBN of the book, its unique identifier
     * @param price Price of the book
     * @param websiteUrl URL of the book
     * @param format Format of the book, either Paperback or Hardback
     */
    public Book(String title, String description, String image, String author,
            String isbn, float price, String websiteUrl, String format) {
        this.title = title;
        this.description = description;
        this.image = image;
        this.author = author;
        this.isbn = isbn;
        this.price = price;
        this.websiteUrl = websiteUrl;
        this.format = format;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Override toString method, returns relevant information about the book
     *
     * @return String containing information about the book
     */
    @Override
    public String toString() {

        return title + " by " + author + ", " + isbn + ", " + format + ", " + price + ", " + image;

    }
}
