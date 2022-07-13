package eu.samcdonovan.application;

import eu.samcdonovan.data.Book;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

/**
 *
 * WHSmith web scraping class, a subclass of WebScraper. Contains functions that
 * help perform web scraping from www.whsmith.co.uk
 */
public class WHSmith extends WebScraper {

    /**
     * Empty WHSmith constructor for Spring beans class dependency injections
     */
    public WHSmith() {
    }

    /**
     * WHSmith scrape function, responsible for navigating the current url,
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

        /* scroll to load all of the books on the current page */
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,8000)", "");
        List<WebElement> bookList = driver.findElements(By.cssSelector("div[class='t-product-list__container-items'] div[class='product-tile-pro']"));

        /* loop through every book on the current page */
        for (WebElement book : bookList) {


            /* get the url of the current book */
            url = book.findElement(By.cssSelector("div[class=' product-image'] a")).getAttribute("href");

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

                System.out.println("ERROR IN WHSMITH, URL = " + url);
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
     * Scrapes all of the book details from a given WHSmith book url
     *
     * @param url url of the book to scrape details for
     * @return book object containing scraped details
     * @throws Exception to avoid crashes, throws a catch-all exception, mostly
     * used to catch selenium.NoSuchElementException
     */
    public Book scrapeBook(String url) throws Exception {

        String isbn = "", format = "";
        String[] descriptionList;

        /* open a new tab with the url of the book page, and switch to it */
        ArrayList<String> tabs = switchTabs(url, 1);

        /* if the 'Accept Cookies' button is still on the page, click it */
        if (driver.findElements(By.cssSelector("button[id='onetrust-accept-btn-handler']")).size() > 0)
            driver.findElement(By.cssSelector("button[id='onetrust-accept-btn-handler']")).click();

        /* check if the current item actually has an author, therefore checking if it actually is a book */
        if (driver.findElements(By.cssSelector("a[class='pw-link author']")).size() > 0) {

            /* scroll to load items */
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,800)", "");

            /* get title, description, author, image and price from page */
            String description = driver.findElement(By.cssSelector("span[itemprop='description']")).getText();

            driver.findElement(By.cssSelector("label[for='tab-moredetails']")).click();

            /* wait for 1 second to load the description in */
            try {
                Thread.sleep(1000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            descriptionList = driver.findElement(By.cssSelector("ul[class='more-details-content']")).getText().split("\n");

            for (int i = 0; i < descriptionList.length; i++) {
                if (descriptionList[i].contains("ISBN"))
                    isbn = descriptionList[i + 1];
                if (descriptionList[i].contains("Format"))
                    format = descriptionList[i + 1];
            }

            String title = driver.findElement(By.cssSelector("h1[class='h1 product-name']")).getText();
            String author = driver.findElement(By.cssSelector("a[class='pw-link author']")).getText();
            String image = driver.findElement(By.cssSelector("div[class='product-thumbnails'] img")).getAttribute("src");

            if (!image.split("\\.")[1].equals("whsmith"))
                image = "https://www.whsmith.co.uk" + image;

            float price = Float.parseFloat(driver.findElement(By.cssSelector("span[class='product-price-value']")).getText().substring(1));

            /* close the current tab and switch back to the original search page */
            driver.close();
            driver.switchTo().window(tabs.get(0));

            /* if debugging, print debugging info */
            if (DEBUG_WHSMITH) {
                System.out.println(title + " by " + author + ", " + format + ", " + price + ", " + isbn + ", " + image);
                System.out.println(url);
                System.out.println("WHSmith scrape count = " + scrapedCount);
            }

            String lowercaseTitle = title.toLowerCase();
            Book newBook;

            /* check that the book is a valid book, return null if it isn't */
            if (author.equals("") || lowercaseTitle.contains("lego") || lowercaseTitle.contains("sticker")
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
