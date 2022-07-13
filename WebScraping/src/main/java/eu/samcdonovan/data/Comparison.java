package eu.samcdonovan.data;

import javax.persistence.*;

/**
 * Comparison class which maps comparison information (retrieved from scraping)
 * onto the comparisons table in the database.
 */
@Entity
@Table(name = "comparisons")
public class Comparison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "price")
    private float price;

    @Column(name = "website_url")
    private String websiteUrl;

    @ManyToOne
    @JoinColumn(name = "isbn", nullable = false)
    private BookInstance bookInstance;

    /**
     * Empty Comparison constructor for Hibernate
     */
    public Comparison() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BookInstance getBookInstance() {
        return bookInstance;
    }

    public void setBookInstance(BookInstance bookInstance) {
        this.bookInstance = bookInstance;
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

    /**
     * Override toString method, prints ISBN, price and URL of the comparison
     *
     * @return String containing the relevant comparison information
     */
    @Override
    public String toString() {
        return this.bookInstance.getIsbn() + ", " + this.price + ", " + this.websiteUrl;
    }

}
