package eu.samcdonovan.application;

import static eu.samcdonovan.application.Debug.DEBUG_BOOKDAO;
import eu.samcdonovan.data.Book;
import eu.samcdonovan.data.BookDAO;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AppConfig manages the configuration class for Spring Beans, which injects
 * class dependencies where needed
 */
@Configuration
public class AppConfig {

    SessionFactory sessionFactory; // the session factory to be injected into the bookDAO
    List<Book> scrapedBooks = new ArrayList<Book>();

    /**
     * ScraperManager bean injects web scrapers into the scraper manager, and is
     * the bean which is called to control the scraping methods
     *
     * @return ScraperManager Manager for the web scrapers
     */
    @Bean
    public ScraperManager scraperManager() {
        ScraperManager scraperManager = new ScraperManager();

        /* create list of web scrapers using the below setup methods 
           and add them to the scraper manager */
        List<WebScraper> scraperList = new ArrayList();

        scraperList.add(WHSmithScraper());
        scraperList.add(foylesScraper());
        scraperList.add(waterstonesScraper());
        scraperList.add(dauntbooksScraper());
        scraperList.add(blackwellsScraper());

        scraperManager.setScraperList(scraperList);

        return scraperManager;
    }

    /**
     * FoylesScraper sets up the Foyles scraper by injecting a BookDAO object
     * and setting the member variables
     *
     * @return WebScraper Foyles web scraper
     */
    @Bean
    public WebScraper foylesScraper() {
        Foyles foyles = new Foyles();

        foyles.setBookDao(bookDAO()); // inject bookDAO into the webscraper
        foyles.setCrawlDelay(2000); // set the crawl delay - the time between each scrape

        /* create a list of URLs which correspond to 4 different genres of book. 
           the web scraper will take a set amount of books from one genre and then
           move onto the next URL */
        List<String> urlList = new ArrayList<String>();

        urlList.add("https://www.foyles.co.uk/all?term=harry+potter&skip=");
        urlList.add("https://www.foyles.co.uk/all?term=george+orwell&skip=");
        urlList.add("https://www.foyles.co.uk/all?term=agathe+christie&skip=");
        urlList.add("https://www.foyles.co.uk/all?term=artificial+intelligence&skip=");
        urlList.add("https://www.foyles.co.uk/biography?skip=");

        foyles.setUrlList(urlList);

        /* set initial page offset and page increment value; these are used to 
           get the correct url, accounting for pagination */
        foyles.setPageOffset(0);
        foyles.setPageIncrement(20);

        foyles.setupSelenium(); // sets up the chrome driver for Selenium

        return foyles;
    }

    /**
     * WHSmithScraper sets up the WHSmith scraper by injecting a BookDAO object
     * and setting the member variables
     *
     * @return WebScraper WHSmith web scraper
     */
    @Bean
    public WebScraper WHSmithScraper() {
        WHSmith whsmith = new WHSmith();

        whsmith.setBookDao(bookDAO()); // inject bookDAO into the webscraper
        whsmith.setCrawlDelay(2000); // set the crawl delay - the time between each scrape

        /* create a list of URLs which correspond to 4 different genres of book. 
           the web scraper will take a set amount of books from one genre and then
           move onto the next URL */
        List<String> urlList = new ArrayList<String>();

        urlList.add("https://www.whsmith.co.uk/search/?c_productFormat=Paperback%7CHardback&q=harry+potter&cgid=&category=Books&sz=36&start=");
        urlList.add("https://www.whsmith.co.uk/search/?c_productFormat=Paperback%7CHardback&q=george+orwell&cgid=&category=Books&sz=36&start=");
        urlList.add("https://www.whsmith.co.uk/search/?c_productFormat=Paperback%7CHardback&q=agatha+christie&cgid=&category=Books&sz=36&start=");
        urlList.add("https://www.whsmith.co.uk/search/?c_productFormat=Hardback%7CPaperback&q=artificial+intelligence&cgid=&category=Books&sz=36&start=");
        urlList.add("https://www.whsmith.co.uk/books/biography-autobiography-memoir-and-true-story-books/bks00001/?start=");

        whsmith.setUrlList(urlList);

        /* set initial page offset and page increment value; these are used to 
           get the correct url, accounting for pagination */
        whsmith.setPageOffset(0);
        whsmith.setPageIncrement(36);

        whsmith.setupSelenium(); // sets up the chrome driver for Selenium

        return whsmith;
    }

    /**
     * WaterstonesScraper sets up the Waterstones scraper by injecting a BookDAO
     * object and setting the member variables
     *
     * @return WebScraper Waterstones web scraper
     */
    @Bean
    public WebScraper waterstonesScraper() {
        Waterstones waterstones = new Waterstones();
        waterstones.setBookDao(bookDAO());
        waterstones.setCrawlDelay(5000);
        List<String> urlList = new ArrayList<String>();

        urlList.add("https://www.waterstones.com/books/search/term/harry+potter/format/17/page/");
        urlList.add("https://www.waterstones.com/books/search/term/george+orwell/page/");
        urlList.add("https://www.waterstones.com/books/search/term/agatha+christie/page/");
        urlList.add("https://www.waterstones.com/books/search/term/artificial+intelligence/page/");
        urlList.add("https://www.waterstones.com/category/biography-true-stories/page/");

        waterstones.setUrlList(urlList);

        waterstones.setPageOffset(1);
        waterstones.setPageIncrement(1);

        waterstones.setupSelenium();

        return waterstones;
    }

    /**
     * DauntbooksScraper sets up the Dauntbooks scraper by injecting a BookDAO
     * object and setting the member variables
     *
     * @return WebScraper Dauntbooks web scraper
     */
    @Bean
    public WebScraper dauntbooksScraper() {
        Dauntbooks dauntbooks = new Dauntbooks();
        dauntbooks.setBookDao(bookDAO());
        dauntbooks.setCrawlDelay(2000);

        List<String> urlList = new ArrayList<String>();

        urlList.add("https://dauntbooks.co.uk/?s=harry+potter");
        urlList.add("https://dauntbooks.co.uk/?s=george+orwell");
        urlList.add("https://dauntbooks.co.uk/?s=agatha+christie");
        urlList.add("https://dauntbooks.co.uk/?s=artificial+intelligence");
        urlList.add("https://dauntbooks.co.uk/book-categories/non-fiction/biography-memoir/page/");

        dauntbooks.setUrlList(urlList);

        dauntbooks.setPageOffset(1);
        dauntbooks.setPageIncrement(1);

        dauntbooks.setupSelenium();

        return dauntbooks;
    }

    /**
     * BlackwellsScraper sets up the Blackwells scraper by injecting a BookDAO
     * object and setting the member variables
     *
     * @return WebScraper Blackwells web scraper
     */
    @Bean
    public WebScraper blackwellsScraper() {
        Blackwells blackwells = new Blackwells();
        blackwells.setBookDao(bookDAO());
        blackwells.setCrawlDelay(2000);
        List<String> urlList = new ArrayList<String>();
        
        urlList.add("https://blackwells.co.uk/bookshop/search?keyword=harry+potter&offset=");
        urlList.add("https://blackwells.co.uk/bookshop/search?keyword=george+orwell&offset=");
        urlList.add("https://blackwells.co.uk/bookshop/search/?keyword=agatha+christie&offset=");
        urlList.add("https://blackwells.co.uk/bookshop/search?keyword=artificial+intelligence&offset=");
        urlList.add("https://blackwells.co.uk/bookshop/category/_biography/?offset=");
        
        blackwells.setUrlList(urlList);

        blackwells.setPageOffset(0);
        blackwells.setPageIncrement(48);

        blackwells.setupSelenium();

        return blackwells;
    }

    /**
     * BookDAO bean, injected into each web scraper for access to the database
     *
     * @return BookDAO bean
     */
    @Bean
    public BookDAO bookDAO() {
        BookDAO bookDAO = new BookDAO();

        /* uses the SessionFactory member variable to 
           setup the session factory for the bookDAO */
        bookDAO.setSessionFactory(sessionFactory());
        if (!DEBUG_BOOKDAO)
            bookDAO.resetDatabase();
        return bookDAO;
    }

    /**
     * SessionFactory bean, injected into the BookDAO. Sets up the session
     * factory if it is currently null, or returns the current session factory
     * if it is already set up
     *
     * @return SessionFactory The newly setup SessionFactory or the current
     * SessionFactory
     */
    @Bean
    public SessionFactory sessionFactory() {

        // if the session factory is not set up yet, build one
        if (sessionFactory == null) {

            try {

                // create a builder for the standard service registry
                StandardServiceRegistryBuilder standardServiceRegistryBuilder = new StandardServiceRegistryBuilder();

                /* loads the Hibernate configuration from the hibernate configuration file.
                   in this project, Hibernate uses annotations as opposed to xml 
                   files so the Hibernate comfiguration file is set up as such */
                standardServiceRegistryBuilder.configure("hibernate.cfg.xml");

                // create the registry that will be used to build the session factory
                StandardServiceRegistry registry = standardServiceRegistryBuilder.build();
                try {
                    // creates the session factory using the registry and builder
                    sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
                } catch (Exception e) {

                    System.err.println("Session Factory build failed.");
                    e.printStackTrace();
                    StandardServiceRegistryBuilder.destroy(registry);
                }

                System.out.println("Session factory built.");
            } catch (Throwable ex) {

                System.err.println("SessionFactory creation failed." + ex);
                ex.printStackTrace();
            }
        }

        // if the session factory already exists, this method returns that session factory
        return sessionFactory;

    }
}
