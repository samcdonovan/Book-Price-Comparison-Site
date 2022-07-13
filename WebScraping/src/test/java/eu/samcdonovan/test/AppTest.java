package eu.samcdonovan.test;

import eu.samcdonovan.application.AppConfig;
import eu.samcdonovan.application.ScraperManager;
import eu.samcdonovan.application.WebScraper;
import eu.samcdonovan.data.Book;
import eu.samcdonovan.data.BookDAO;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Unit test class for Web Scraping
 */
@DisplayName("Test Scrapers and database functions")
public class AppTest {

    static ScraperManager manager;
    static BookDAO bookDao;
    List<Book> booksToDelete;
    static ApplicationContext context;

    @BeforeAll
    static void initAll() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        manager = (ScraperManager) context.getBean("scraperManager");
        manager.setScrapeMax(5);
        bookDao = manager.getScraperList().get(0).getBookDao();
    }

    @BeforeEach
    void init() {
        booksToDelete = new ArrayList<Book>();
    }

    /**
     * Tests that scrapers actually scrape a specified amount of books
     */
    @Test
    @DisplayName("Test amount of books being scraped")
    void testScrapeAmount() {

        context = new AnnotationConfigApplicationContext(AppConfig.class);
        manager = (ScraperManager) context.getBean("scraperManager");
        manager.setScrapeMax(5);
        bookDao = manager.getScraperList().get(0).getBookDao();

        try {
            for (WebScraper scraper : manager.getScraperList()) {
                try {
                    Thread.sleep(1000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                scraper.scrape();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Failed to scrape in testScrapeAmount(). Exception thrown: " + ex.getMessage());
        }
        for (WebScraper scraper : manager.getScraperList()) {
            assertEquals(1, scraper.getScrapedCount());
        }
    }

    @Test
    @DisplayName("WebScraper helper function 'getNextUrl' works as intended")
    void testGetNextUrl() {

        context = new AnnotationConfigApplicationContext(AppConfig.class);
        manager = (ScraperManager) context.getBean("scraperManager");
        manager.setScrapeMax(5);
        WebScraper scraper = manager.getScraperList().get(0);
        String url = scraper.getUrlList().get(0) + (scraper.getPageOffset() + scraper.getPageIncrement());
        try {

        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Failed to scrape in testScrapeAmount(). Exception thrown: " + ex.getMessage());
        }

        assertEquals(scraper.getNextUrl(), url);

    }

    /**
     * Tests that the data retrieved from scraping is valid, and not empty
     */
    @Test
    @DisplayName("Scrapes valid data")
    void testScrapeData() {

        context = new AnnotationConfigApplicationContext(AppConfig.class);
        manager = (ScraperManager) context.getBean("scraperManager");
        manager.setScrapeMax(5);

        List<Book> books = new ArrayList<Book>();
        List<String> urls = new ArrayList<String>();
        urls.add("https://www.whsmith.co.uk/products/the-man-who-died-twice/richard-osman/hardback/9780241425428.html");
        urls.add("https://www.foyles.co.uk/witem/fiction-poetry/the-man-who-died-twice-(the-thursday-mur,richard-osman-9780241425428");
        urls.add("https://www.waterstones.com/book/the-man-who-died-twice/richard-osman/9780241425428");
        urls.add("https://dauntbooks.co.uk/shop/books/the-man-who-died-twice/");
        urls.add("https://blackwells.co.uk/bookshop/product/The-Man-Who-Died-Twice-by-Richard-Osman-author/9780241425428");

        try {
            for (int i = 0; i < manager.getScraperList().size(); i++) {

                books.add(manager.getScraperList().get(i).scrapeBook(urls.get(i)));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Failed to scrap. Exception thrown: " + ex.getMessage());

        }

        for (int i = 0; i < books.size(); i++) {
            assertNotEquals("", books.get(i).getTitle());
            assertNotEquals("", books.get(i).getAuthor());
            assertEquals("9780241425428", books.get(i).getIsbn());
            assertNotEquals("", books.get(i).getFormat());

        }
    }

    /**
     * Tests that when data is inserted into the database, it actually is
     * inserted and can be retrieved again straight from the database.
     */
    @Test
    @DisplayName("Test database insertion")
    void testDatabaseInsert() {

        Book book = new Book("TEST - TITLE", "TEST - DESCRIPTION", "TEST - IMAGE", "TEST - AUTHOR",
                "00000000000", 0.0f, "TEST - URL", "TEST - FORMAT");

        try {

            bookDao.addBook(book);
            booksToDelete.add(book);

            List<Book> bookList = bookDao.searchBooks("from Book where title='" + book.getTitle()
                    + "' AND author='" + book.getAuthor() + "'");
            assertEquals(1, bookList.size());
            assertEquals("TEST - TITLE", bookList.get(0).getTitle());
            assertEquals("TEST - DESCRIPTION", bookList.get(0).getDescription());
            assertEquals("TEST - IMAGE", bookList.get(0).getImage());
            assertEquals("TEST - AUTHOR", bookList.get(0).getAuthor());

        } catch (Exception ex) {
            fail("Failed to set add to database. " + book.getTitle() + " " + book.getAuthor() + " Exception thrown: " + ex.getMessage());
        }

    }

    /**
     * Tests the book cleaning function, which removes certain elements from
     * text in the book objects
     */
    @Test
    @DisplayName("Test the cleaning of the data before it is inserted into the database")
    void testDataCleaning() {

        Book bookWithColons = new Book("TEST: COLON:", "TEST - DESCRIPTION", "TEST - IMAGE", "TEST: COLON:AUTHOR",
                "00000000000", 0.0f, "TSET - URL", "TEST - FORMAT");

        bookDao.cleanBook(bookWithColons);

        assertEquals("TEST", bookWithColons.getTitle());
        assertEquals("TEST", bookWithColons.getAuthor());

        Book bookWithAppostrophes = new Book("TEST 'APPOSTROPHES'", "TEST - DESCRIPTION", "TEST - IMAGE", "TEST: 'AUTHOR'",
                "00000000000", 0.0f, "TSET - URL", "TEST - FORMAT");

        bookDao.cleanBook(bookWithAppostrophes);

        assertEquals("TEST APPOSTROPHES", bookWithAppostrophes.getTitle());
        assertEquals("TEST", bookWithAppostrophes.getAuthor());
    }

    /**
     * Test that when two books with the same title, author and ISBN are
     * inserted into the database, they don't both get inserted and are instead
     * merged
     */
    @Test
    @DisplayName("Test data merging when inserting into database")
    void testDatabaseMerge() {

        Book firstBook = new Book("MERGE1", "TEST - DESCRIPTION", "TEST - IMAGE", "TEST - AUTHOR",
                "00000000000", 0.0f, "https://.merge1.co.uk", "TEST - FORMAT");

        Book secondBook = new Book("MERGE1", "TEST - DESCRIPTION", "TEST - IMAGE", "TEST - AUTHOR",
                "00000000000", 1.0f, "https://.merge2.co.uk", "TEST - FORMAT");
        try {

            bookDao.addToDatabase(firstBook);

            bookDao.addToDatabase(secondBook);
            booksToDelete.add(firstBook);
            booksToDelete.add(secondBook);

            List<Book> bookList = bookDao.searchBooks("from Book where (title like '%" + firstBook.getTitle()
                    + "%' OR title like '%" + firstBook.getTitle() + "' OR title like '" + firstBook.getTitle()
                    + "%') AND author like '%" + firstBook.getAuthor() + "%'");

            /* if it merges correctly, there should only be one version of the book in the books table */
            assertEquals(1, bookList.size());

        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Failed to add to database. Exception thrown: " + ex.getMessage());
        }

    }

    @AfterEach
    void tearDown() {

        manager.closeDrivers();

    }

    @AfterAll
    static void tearDownAll() {
        bookDao.shutDown();
        manager.stopThreads();
    }
}
