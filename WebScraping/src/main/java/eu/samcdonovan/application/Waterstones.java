package eu.samcdonovan.application;

import eu.samcdonovan.data.Book;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * Waterstones web scraping class, a subclass of WebScraper. Contains functions
 * that help perform web scraping from www.waterstones.co.uk
 */
public class Waterstones extends WebScraper {

    /**
     * Empty Waterstones constructor for Spring beans class dependency
     * injections
     */
    public Waterstones() {
    }

    /**
     * Waterstones scrape function, responsible for navigating the current url,
     * looping through all of the books in the current page and adding them to
     * the scraped books list
     */
    synchronized public void scrape() {
        driver.get(currentUrl);

        System.out.println(this.getClass().getName() + " thread is running.");

        List<String> tabs;
        String url = "", priceInformation = "";
        Book newBook;


        /* if the 'Accept Cookies' button is still on the page, click it */
        if (driver.findElements(By.cssSelector("button[id='onetrust-accept-btn-handler']")).size() > 0)
            driver.findElement(By.cssSelector("button[id='onetrust-accept-btn-handler']")).click();

        List<WebElement> bookList = driver.findElements(By.cssSelector("div[class='search-results-list'] div[class='inner']"));

        /* loop through every book on the current page */
        for (WebElement book : bookList) {

            priceInformation = book.findElement(By.cssSelector("div[class='book-price']")).getText();

            /* if the current book is not a Paperback or Hardback book, continue onto the next book */
            if (!priceInformation.contains("Paperback") && !priceInformation.contains("Hardback"))
                continue;

            /* get the url of the current book */
            url = book.findElement(By.cssSelector("div[class='title-wrap'] a")).getAttribute("href");

            try {
                /* if scrapeBook throws an exception, don't add anything to 
                   the database, and continue onto the next book */
                newBook = scrapeBook(url);
                if (newBook != null) {

                    /* check that the book is a valid book, return null if it isn't */
                    getBookDao().addToDatabase(newBook);
                    scrapedCount++;
                }
            } catch (Exception ex) {

                System.out.println("ERROR IN WATERSTONES, URL = " + url);
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
     * Scrapes all of the book details from a given Waterstones book url
     *
     * @param url url of the book to scrape details for
     * @return book object containing scraped details
     * @throws Exception to avoid crashes, throws a catch-all exception, mostly
     * used to catch selenium.NoSuchElementException
     */
    public Book scrapeBook(String url) throws Exception {

        String isbn = "", description = "";

        /* open a new tab with the url of the book page, and switch to it */
        ArrayList<String> tabs = switchTabs(url, 1);

        /* if the 'Accept Cookies' button is still on the page, click it */
        if (driver.findElements(By.cssSelector("button[id='onetrust-accept-btn-handler']")).size() > 0) {
            
            /* scroll to load in elements on the page */
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,400)", "");
            WebDriverWait wait = new WebDriverWait(driver, 10);
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[id='onetrust-accept-btn-handler']")));
            element.click();

        }

        /* check if the current item actually has an author, therefore checking if it actually is a book */
        if (driver.findElements(By.cssSelector("span[itemprop='author']")).size() > 0) {

            /* scrape title, author, description, isbn, format, image and price from book page */
            String[] descriptionTab = driver.findElement(By.cssSelector("div[class='tabs-content-container clearfix']")).getText().split("\n");
            for (int j = 0; j < descriptionTab.length; j++) {

                if (j < 3)
                    description += descriptionTab[j] + "\n";

                if (descriptionTab[j].contains("ISBN"))
                    isbn = descriptionTab[j].substring(6);

            }
            String[] fullTitle = driver.findElement(By.cssSelector("span[id='scope_book_title']")).getText().split("\\(");
            String title = fullTitle[0].substring(0, fullTitle[0].length() - 1);
            String format = fullTitle[1].substring(0, fullTitle[1].length() - 1);
            String author = driver.findElement(By.cssSelector("span[itemprop='author']")).getText();
            String image = driver.findElement(By.cssSelector("img[id='scope_book_image']")).getAttribute("src");
            float price = Float.parseFloat(driver.findElement(By.cssSelector("b[itemprop='price']")).getText().substring(1));

            /* if debugging, print debugging info */
            if (DEBUG_WATERSTONES) {
                System.out.println(title + " by " + author + ", " + format + ", " + price + ", " + isbn + ", " + image);
                System.out.println(url);
                System.out.println("Waterstones scrape count = " + scrapedCount);
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
