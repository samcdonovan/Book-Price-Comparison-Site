package eu.samcdonovan.application;

import eu.samcdonovan.data.Book;
import eu.samcdonovan.data.BookDAO;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Abstract WebScraper superclass, extends Thread so that the scrapers can be
 * run multi-threaded, implements Debug to get access to Debug bools
 */
public abstract class WebScraper extends Thread implements Debug {

    int pageOffset; // current offset for the page
    int initialOffset; // initial page offset
    int pageIncrement; // amount to increment the offset
    int scrapedCount = 0; // number of books currently scraped
    int scrapeMax = 120;// max number of books to scrape
    private BookDAO bookDao;
    private int crawlDelay;
    WebDriver driver;
    String currentUrl;
    List<String> urlList = new ArrayList<String>();
    boolean runThread = true;

    /**
     * Empty WebScraper constructor for Spring Beans class dependency injection
     */
    public WebScraper() {
    }

    /**
     * Increment pageOffset by pageIncrement
     *
     * @return incremented offset
     */
    public int incrementOffset() {
        return this.pageOffset += pageIncrement;
    }

    public int getPageOffset() {
        return pageOffset;
    }

    public void setPageOffset(int pageOffset) {
        this.pageOffset = pageOffset;
        this.initialOffset = pageOffset;
    }

    public int getPageIncrement() {
        return pageIncrement;
    }

    public void setPageIncrement(int pageIncrement) {
        this.pageIncrement = pageIncrement;
    }

    public int getScrapedCount() {
        return scrapedCount;
    }

    public void setScrapedCount(int scrapedCount) {
        this.scrapedCount = scrapedCount;
    }

    public BookDAO getBookDao() {
        return bookDao;
    }

    public void setBookDao(BookDAO bookDao) {
        this.bookDao = bookDao;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public int getCrawlDelay() {
        return crawlDelay;
    }

    public void setCrawlDelay(int crawlDelay) {
        this.crawlDelay = crawlDelay;
    }

    public String getCurrentUrl() {
        return currentUrl;
    }

    public void setCurrentUrl(String currentUrl) {
        this.currentUrl = currentUrl;
    }

    public void setRunThread(boolean runThread) {
        this.runThread = runThread;
    }

    public List<String> getUrlList() {
        return urlList;
    }

    public void setUrlList(List<String> urlList) {
        this.urlList = urlList;
    }

    public int getScrapeMax() {
        return scrapeMax;
    }

    public void setScrapeMax(int scrapeMax) {
        this.scrapeMax = scrapeMax;
    }

    public void setupSelenium() {
        ChromeOptions options = new ChromeOptions();

        //sets headless depending on whether this scraper is currently being debugged
        options.setHeadless(!getDebugBool());

        /* settings for Selenium to help deal with ChromeDriver issues when scraping */
        options.addArguments("--start-maximized");

        ChromeDriver driver = new ChromeDriver(options);

        this.driver = driver;

        /* set initial url to the first url in the list */
        this.currentUrl = urlList.get(0);

        /* set page offset for all scrapers  */
        this.currentUrl += pageOffset;
    }

    /**
     * Retrieves the next url for the web scraper
     *
     * @return string containing the next url
     */
    public String getNextUrl() {
        String url = "";
        if (scrapedCount % (scrapeMax / 5) == 0 && scrapedCount != 0) {
            System.out.print("Changing from " + urlList.get(0));

            urlList.remove(0);

            pageOffset = initialOffset;

            /* if there are still urls in the url list, get the next one, and append the pageOffset to it */
            if (urlList.size() > 0)
                url = urlList.get(0) + pageOffset;
            System.out.print(" to " + url + "\n");
        } else
            url = urlList.get(0) + incrementOffset();

        return url;
    }

    /**
     * Gets the appropriate debug bool for the current web scraper
     *
     * @return debug boolean from the Debug interface
     */
    public boolean getDebugBool() {

        /* checks the class of the current web scraper and returns the corresponding debug boolean */
        if (this.getClass() == Waterstones.class)
            return DEBUG_WATERSTONES;

        if (this.getClass() == Foyles.class)
            return DEBUG_FOYLES;

        if (this.getClass() == Dauntbooks.class)
            return DEBUG_DAUNTBOOKS;

        if (this.getClass() == Blackwells.class)
            return DEBUG_BLACKWELLS;

        if (this.getClass() == WHSmith.class)
            return DEBUG_WHSMITH;

        return false;
    }

    /**
     * Web scraper helper function, uses a JavaScript executor to open a new tab
     * in the chrome driver, and then switch to that new tab
     *
     * @param url URL of the new tab
     * @param tabNum the index to switch the tabs to
     * @return list of tabs
     */
    public ArrayList<String> switchTabs(String url, int tabNum) {

        ((JavascriptExecutor) driver).executeScript("window.open('" + url + "')");
        ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());

        driver.switchTo().window(tabs.get(tabNum));

        return tabs;
    }

    /**
     * Main scrape function that must be implemented in every scraper
     */
    abstract public void scrape();

    /**
     * Scrapes book details from a given url
     *
     * @param url url to scrape
     * @return book object containing book details
     * @throws Exception stops the scrapers from crashing if there is an
     * exception
     */
    abstract public Book scrapeBook(String url) throws Exception;

    /**
     * Thread run() override; runs the scraping function until the required
     * amount of books have been scraped. This is inherited by each web scraper,
     * and in the scraper manager it is used to run 5 separate threads for the
     * scrapers
     */
    @Override
    synchronized public void run() {

        runThread = true;

        try {
            while (runThread) {

                scrape();
                sleep(crawlDelay);

                /* if the scrape count of the current scraper is more than or equal to a specified scrape max, stop the thread */
                if (scrapedCount >= scrapeMax) {
                    System.out.println(this.getClass().getName() + " thread is finished.");
                    runThread = false;
                }
            }
        } //Interrupt exception will break us out of while loop
        catch (InterruptedException ex) {
            System.out.println("Thread interrupted during sleep");
        }
        driver.quit();
    }

    /**
     * Helper function to find an element using a specified CSS selector that
     * contains the specified text
     *
     * @param cssSelector CSS selector of the web element
     * @param text text that needs to be found within the web element
     * @return if an element is found containing the text, return that element,
     * otherwise return null
     */
    public WebElement findElementWithText(String cssSelector, String text) {
        List<WebElement> list = driver.findElements(By.cssSelector(cssSelector));

        for (WebElement element : list) {
            if (element.getText().contains(text))
                return element;
        }
        return null;
    }
}
