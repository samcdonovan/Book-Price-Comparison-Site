package eu.samcdonovan.application;

import java.util.ArrayList;
import java.util.List;

/**
 * ScraperManager class that stores all of the web scrapers and contains methods
 * which are used for controlling the scraper threads
 */
public class ScraperManager {

    List<WebScraper> scraperList = new ArrayList<WebScraper>();

    /**
     * Empty ScraperManager constructor for Spring Beans class dependency
     * injections
     */
    public ScraperManager() {
    }

    public List<WebScraper> getScraperList() {
        return scraperList;
    }

    public void setScraperList(List<WebScraper> scraperList) {
        this.scraperList = scraperList;
    }

    /**
     * Sets scrapeMax for each scraper in the scraper list
     *
     * @param scrapeMax scrape max to set it to
     */
    public void setScrapeMax(int scrapeMax) {
        for (WebScraper scraper : scraperList) {
            scraper.setScrapeMax(scrapeMax);
        }
    }

    /**
     * Closes all ChromeDrivers that are still running
     */
    public void closeDrivers() {
        System.out.println("Closing all scraper threads.");
        for (WebScraper scraper : scraperList) {
            if (scraper.getDriver().toString() != null)
                scraper.getDriver().quit();
        }
    }

    /**
     * Stops all of the scraper threads and closes all of the chrome drivers
     */
    public void stopThreads() {
        System.out.println("Stopping threads.");

        for (WebScraper scraper : scraperList) {
            scraper.setRunThread(false);

        }
        scraperList.get(0).getBookDao().shutDown();
        closeDrivers();

    }

    /**
     * Joins all of the scraper threads
     */
    public void joinThreads() {
        System.out.println("Joining threads.");
        try {
            for (WebScraper scraper : scraperList) {

                scraper.join();
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Starts all of the scraper threads
     */
    public void startThreads() {
        System.out.println("Starting all scraper threads.");

        for (WebScraper scraper : scraperList) {
            scraper.start();
        }
    }

}
