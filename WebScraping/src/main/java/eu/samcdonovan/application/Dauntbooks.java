package eu.samcdonovan.application;

import eu.samcdonovan.data.Book;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

/**
 *
 * Dauntbooks web scraping class, a subclass of WebScraper. Contains functions
 * that help perform web scraping from www.dauntbooks.co.uk
 */
public class Dauntbooks extends WebScraper {

    /**
     * Empty Dauntbooks constructor for Spring beans class dependency injections
     */
    public Dauntbooks() {
    }

    /**
     * Dauntbooks scrape function, responsible for navigating the current url,
     * looping through all of the books in the current page and adding them to
     * the scraped books list
     */
    synchronized public void scrape() {

        driver.get(currentUrl);

        System.out.println(this.getClass().getName() + " thread is running.");

        List<String> tabs;
        String url = "";
        Book newBook;

        /* scroll to load all of the books on the current page */
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,8000)", "");

        try {
            Thread.sleep(1000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        List<WebElement> bookList = driver.findElements(By.cssSelector("ul li[class='col-6 col-sm-4 col-xl-3 archive-product_container search_result fade in']"));
        List<WebElement> checkUrl;

        /* loop through every book on the current page */
        for (WebElement book : bookList) {

            checkUrl = book.findElements(By.cssSelector("a[class='woocommerce-LoopProduct-link woocommerce-loop-product__link']"));

            /* get the url of the current book */
            if (checkUrl.size() > 0)
                url = book.findElement(By.cssSelector("a[class='woocommerce-LoopProduct-link woocommerce-loop-product__link']")).getAttribute("href");
            else
                url = book.findElement(By.cssSelector("div[class='archive-product-image-box ']")).getAttribute("data-href");

            try {
                /* if scrapeBook throws an exception, don't add anything to 
                   the scraped books list, and continue onto the next book */
                newBook = scrapeBook(url);
                if (newBook != null) {

                    /* add book to database and increase scrape count */
                    getBookDao().addToDatabase(newBook);
                    scrapedCount++;
                }
            } catch (Exception ex) {

                System.out.println("ERROR IN DAUNTBOOKS, URL = " + url);
                ex.printStackTrace();

                /* close the tab and go back to the search page */
                tabs = new ArrayList<String>(driver.getWindowHandles());
                driver.close();
                driver.switchTo().window(tabs.get(0));
            }
            /* break out of the loop if the scrapedCount is more than or equal to
                   the max amount of scraped books allowed, or if the scrapedCount is 
                   a factor of 5 of the scrapeMax */
            if ((!runThread || scrapedCount % (scrapeMax / 5) == 0 || scrapedCount >= scrapeMax) && scrapedCount != 0)
                break;

        }

        /* get the next page of books to scrape */
        currentUrl = getNextUrl();
    }

    /**
     * Scrapes all of the book details from a given Dauntbooks book url
     *
     * @param url url of the book to scrape details for
     * @return book object containing scraped details
     * @throws Exception to avoid crashes, throws a catch-all exception, mostly
     * used to catch selenium.NoSuchElementException
     */
    public Book scrapeBook(String url) throws Exception {
        String isbn = "";

        /* open a new tab with the url of the book page, and switch to it */
        ArrayList<String> tabs = switchTabs(url, 1);

        /* scroll to load in elements on the page */
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,400)", "");
        try {
            sleep(1000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        /* scrape title, author, description, isbn, format, image and price from book page */
        String title = driver.findElement(By.cssSelector("h1[class='product_title entry-title']")).getText();
        String author = driver.findElement(By.cssSelector("h2[class='event_subtitle']")).getText();
        String description = driver.findElement(By.cssSelector("section[class='product-description-content']")).getText();
        String[] descriptionList = driver.findElement(By.cssSelector("div[class='product_meta book fade in']")).getText().split("\n");
        for (String text : descriptionList) {
            if (text.contains("ISBN"))
                isbn = text.substring(6);
        }
        String format = descriptionList[0];
        String image = driver.findElement(By.cssSelector("div[class='woocommerce-product-gallery__image'] a")).getAttribute("href");
        float price = Float.parseFloat(driver.findElement(By.cssSelector("span[class='woocommerce-Price-amount amount'] bdi")).getText().substring(1));

        /* if debugging, print debugging info */
        if (DEBUG_DAUNTBOOKS) {
            System.out.println(title + " by " + author + ", " + format + ", " + price + ", " + isbn + ", " + image);
            System.out.println(url);
            System.out.println("Dauntbooks scrape count = " + scrapedCount);
        }

        /* close the current tab and switch back to the original search page */
        driver.close();
        driver.switchTo().window(tabs.get(0));

        String lowercaseTitle = title.toLowerCase();
        Book newBook;

        /* check that the book is a valid book, return null if it isn't */
        if (lowercaseTitle.contains("lego") || lowercaseTitle.contains("sticker")
                || lowercaseTitle.contains("kit") || isbn.charAt(0) != '9')
            newBook = null;
        else
            newBook = new Book(title, description, image, author, isbn, price, url, format);

        /* return the book to be added to the scrapedBooks list */
        return newBook;
    }

    /**
     * Retrieves the next url for the web scraper
     *
     * @return string containing the next url
     */
    @Override
    public String getNextUrl() {
        String url = "";
        if (scrapedCount % (scrapeMax / 5) == 0 && scrapedCount != 0) {
            System.out.print("Changing from " + urlList.get(0));
            urlList.remove(0);

            pageOffset = initialOffset;
            if (urlList.size() > 0) {
                /* get next url */
                url = urlList.get(0);

                /* Dauntbooks handles its urls differently depending on whether a search was performed.
                If the current url is based on a genre and not a search, pagination works in the url, 
                so pageOffset needs to be appendied to url */
                if (url.substring(url.length() - 1).equals("/"))
                    url += pageOffset;
            }
            System.out.print(" to " + url + "\n");

        } else if (urlList.get(0).substring(urlList.get(0).length() - 1).equals("/"))
            url = urlList.get(0) + incrementOffset();

        return url;
    }
}
