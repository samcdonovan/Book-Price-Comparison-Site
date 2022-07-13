package eu.samcdonovan.application;

import eu.samcdonovan.data.Book;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 *
 * Blackwells web scraping class, a subclass of WebScraper. Contains functions
 * that help perform web scraping from www.blackwells.co.uk
 */
public class Blackwells extends WebScraper {

    /**
     * Empty Blackwells constructor for Spring beans class dependency injections
     */
    public Blackwells() {
    }

    /**
     * Blackwells scrape function, responsible for navigating the current url,
     * looping through all of the books in the current page and adding them to
     * the scraped books list
     */
    synchronized public void scrape() {
        driver.get(currentUrl);

        System.out.println(this.getClass().getName() + " thread is running.");

        List<String> tabs;
        String url = "";
        Book newBook;

        /* if the 'Accept Cookies' button is still on the page, click it */
        if (driver.findElements(By.cssSelector("button[id='onetrust-accept-btn-handler']")).size() > 0)
            driver.findElement(By.cssSelector("button[id='onetrust-accept-btn-handler']")).click();

        List<WebElement> bookList = driver.findElements(By.cssSelector("div[class='search-result'] li[class='search-result__item ']"));

        /* loop through every book on the current page */
        for (WebElement book : bookList) {

            /* get the url of the current book */
            url = book.findElement(By.cssSelector("a[class='product-name']")).getAttribute("href");

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
                System.out.println("ERROR IN BLACKWELLS, URL = " + url);

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
     * Scrapes all of the book details from a given Blackwells book url
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
        if (driver.findElements(By.cssSelector("button[id='onetrust-accept-btn-handler']")).size() > 0)
            driver.findElement(By.cssSelector("button[id='onetrust-accept-btn-handler']")).click();

        /* check if the current item actually has an author, therefore checking if it actually is a book */
        if (driver.findElements(By.cssSelector("p[itemprop='author'] a")).size() > 0) {

            /* scrape title, author, description, isbn, format, image and price from book page */
            String title = driver.findElement(By.cssSelector(".content.product__info h1[class='product__name']")).getText();
            String author = driver.findElement(By.cssSelector("p[itemprop='author'] a")).getText();
            String description = driver.findElement(By.cssSelector("div[itemprop='description']")).getText().replace("\n", "");
            String format = driver.findElement(By.cssSelector("span[itemprop='bookFormat']")).getText();
            String image = driver.findElement(By.cssSelector("figure[class='picture-wrapper'] img")).getAttribute("src");
            String isbn = driver.findElement(By.cssSelector("td[itemprop='isbn']")).getText();
            float price = Float.parseFloat(driver.findElement(By.cssSelector("li[class='product-price--current']")).getText().substring(1));

            /* if debugging, print debugging info */
            if (DEBUG_BLACKWELLS) {
                System.out.println(title + " by " + author + ", " + format + ", " + price + ", " + isbn + ", " + image);
                System.out.println(url);
                System.out.println("Blackwells scrapecount = " + scrapedCount);
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

        /* close the current tab and switch back to the original search page */
        driver.close();
        driver.switchTo().window(tabs.get(0));

        /* if there is no author on the page, the item is invalid so return null */
        return null;
    }
}
