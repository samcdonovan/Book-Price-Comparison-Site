<h1 align="center">Book Price Comparison Website (with web scraping)</h1>

<div align="center">

  [![License](https://img.shields.io/badge/license-MIT-blue.svg)](/LICENSE)

</div>

---

## 📝 Table of Contents
- [About](#about)
- [Libraries/Frameworks/Services](#built_using)
- [Authors](#authors)

## ℹ️ About <a name = "about"></a>

The aim of this project was to create a price comparison website for books where a user could input a search term and related books would be displayed. They could then select one of those books, and more detailed information about the book would be displayed along with price comparisons. This was done using web scraping to get comparison data from 5 different online book retailers (WHSmith, Blackwells, Dauntbooks, Foyles, Waterstones). This data was put into a database to be used later by Node.js to display it in a meaningful way on the website. 

## 💻 Libraries/Frameworks <a name = "built_using"></a>
## Java
- [Maven](https://maven.apache.org/what-is-maven.html): Used to build this project, manage and maintain the different Java framework dependencies, run unit testing, and efficiently organise and package the project.
- [Hibernate](https://hibernate.org/): Used to map object fields onto their respective fields in the tables in a MySQL database, for example a Book object’s “title” field would map onto the “title” column in the books table.
- [Spring Beans](https://spring.io/): Manage class dependencies within the code, for example there was a “ScraperManager” class which depended on the WebScraper class, so Spring was used to inject those classes.
- [Selenium](https://www.selenium.dev/documentation/webdriver/): For scraping data from book retailer websites for 500+ books. Selenium was chosen over alternatives such as JSoup for its useful page interaction capabilities.
- [JUnit](https://junit.org/junit5/): Used to run unit tests, testing different scraper and database functions.
## JavaScript
- [Vue](https://vuejs.org/): Used to dynamically display books on the front end (depending the user's query).
- [Node.js](https://nodejs.org/en/)
- [Express](https://expressjs.com/)
- [Axios](https://axios-http.com/): For retrieving data from the local MySQL database through the RESTful API, and passing it to the front end.
- [Mocha Chai](https://mochajs.org/): Used for testing JS in the front end, and to test the RESTful API endpoints.
## Other
- RESTful API: A RESTful API was created for this project, allowing users to scrape data straight from the database in JSON format.

## ✍️ Authors <a name = "authors"></a>
- [@samcdonovan](https://github.com/samcdonovan)
