package eu.samcdonovan.application;

import eu.samcdonovan.data.Book;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

/**
 *
 * Foyles web scraping class, a subclass of WebScraper. Contains functions that
 * help perform web scraping from www.foyles.co.uk
 */
public class Foyles extends WebScraper {

    /**
     * Empty Foyles constructor for Beans class dependency injection
     */
    public Foyles() {
    }

    /**
     * Foyles scrape function, responsible for navigating the current url,
     * looping through all of the books in the current page and adding them to
     * the scraped books list
     */
    synchronized public void scrape() {
        driver.get(currentUrl);

        System.out.println(this.getClass().getName() + " thread is running.");

        try {
            Thread.sleep(3000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        /* if the 'Accept Cookies' button is still on the page, click it */
        if (driver.findElements(By.cssSelector("button[class='onetrust-close-btn-handler onetrust-close-btn-ui banner-close-button ot-close-icon']")).size() > 0)
            driver.findElement(By.cssSelector("button[class='onetrust-close-btn-handler onetrust-close-btn-ui banner-close-button ot-close-icon']")).click();

        List<String> tabs;
        String url = "";
        Book newBook;

        List<WebElement> bookList = driver.findElements(By.cssSelector("div[class='NewSRItem']"));

        /* loop through every book on the current page */
        for (WebElement book : bookList) {

            /* get the url of the current book */
            url = book.findElement(By.cssSelector("a[class='Title']")).getAttribute("href");

            try {
                
                /* if scrapeBook throws an exception, don't add anything to 
                   the database, and continue onto the next book */
                newBook = scrapeBook(url);
                
                if (newBook != null) {
                    
                    /* add book to database and increase scrape count */
                    getBookDao().addToDatabase(newBook);
                    scrapedCount++;
                }
            } catch (Exception ex) {
                System.out.println("ERROR IN FOYLES, URL = " + url);
                ex.printStackTrace();

                /* close the tab and go back to the search page */
                tabs = new ArrayList<String>(driver.getWindowHandles());
                driver.close();
                driver.switchTo().window(tabs.get(0));
            }
            /* break out of the loop if the scrapedCount is more than or equal to
              he max amount of scraped books allowed, or if the scrapedCount is 
              a factor of 5 of the scrapeMax */
            if ((!runThread || scrapedCount % (scrapeMax / 5) == 0 || scrapedCount >= scrapeMax) && scrapedCount != 0)
                break;

        }

        /* get the next page of books to scrape */
        currentUrl = getNextUrl();
    }

    /**
     * Scrapes all of the book details from a given Foyles book url
     *
     * @param url url of the book to scrape details for
     * @return book object containing scraped details
     * @throws Exception to avoid crashes, throws a catch-all exception, mostly
     * used to catch selenium.NoSuchElementException
     */
    public Book scrapeBook(String url) throws Exception {

        /* open a new tab with the url of the book page, and switch to it */
        ArrayList<String> tabs = switchTabs(url, 1);

        try {
            sleep(1000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        /* if the 'Accept Cookies' button is still on the page, click it */
        if (driver.findElements(By.cssSelector("button[class='onetrust-close-btn-handler onetrust-close-btn-ui banner-close-button ot-close-icon']")).size() > 0)
            driver.findElement(By.cssSelector("button[class='onetrust-close-btn-handler onetrust-close-btn-ui banner-close-button ot-close-icon']")).click();

        /* click the synopsis tab on the book page, this allows the description and other information to be scraped */
        driver.findElement(By.cssSelector("div[id='ProductId'] a[class='InternalLink']")).click();

        String title = driver.findElement(By.cssSelector("span[itemprop='name']")).getText();
        String author = driver.findElement(By.cssSelector("div[class='Author'] a[class='Author']")).getText();

        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,400)", "");

        /* scrape title, author, description, isbn, format, image and price from book page */
        String description = driver.findElement(By.cssSelector("div[class='Column1']")).getText();
        String format = driver.findElement(By.cssSelector("span[itemprop='bookFormat']")).getText().replaceAll("\\(", "").replaceAll("\\)", "");
        String image = driver.findElement(By.cssSelector("img[id='ctl00_MainContent_uxDetailItemControl_DetailThumbBig']")).getAttribute("src");
        String isbn = driver.findElement(By.cssSelector("span[itemprop='isbn']")).getText().replace(" ", "");
        float price = Float.parseFloat(driver.findElement(By.cssSelector("div[class='Price1']")).getText().substring(1));

        /* if debugging, print debugging info */
        if (DEBUG_FOYLES) {
            System.out.println(title + " by " + author + ", " + format + ", " + price + ", " + isbn + ", " + image);
            System.out.println(url);
            System.out.println("Foyles scrape count = " + scrapedCount);
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

        /* return the book to be added to the scrapedBooks list , null if it is invalid */
        return newBook;
    }
}
