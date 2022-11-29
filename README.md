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

This website allows the user to view charts displaying data about different teams that are currently playing in the NBA. The types of data that are displayed are numerical data (score difference for each match), predictions about that numerical data (score difference for future matches) and the results of sentiment analysis of Tweets (about the teams). The data for these teams was extracted from the free API service BallDontLie (https://www.balldontlie.io/#introduction). This included information about each match: mainly the scores, the team they were playing against, and the date of the match. A Twitter Developer account was required to collect the Tweets about each team for sentiment analysis. 

## 💻 Libraries/Frameworks <a name = "built_using"></a>
## Java
- [Maven](): Used to build this project, manage and maintain the different Java framework dependencies, run unit testing, and efficiently organise and package the project.
- [Hibernate](): Used to map object fields onto their respective fields in the tables in a MySQL database, for example a Book object’s “title” field would map onto the “title” column in the books table.
- [Spring Beans](): Manage class dependencies within the code, for example there was a “ScraperManager” class which depended on the WebScraper class, so Spring was used to inject those classes.
- [Selenium](): For scraping data from book retailer websites for 500+ books. Selenium was chosen over alternatives such as JSoup for its useful page interaction capabilities.
- [JUnit](): Used to run unit tests, testing different scraper and database functions.
## JavaScript
- [Vue](https://vuejs.org/)
- [Node.js](https://nodejs.org/en/)
- [Axios](https://axios-http.com/): For sending GET requests to the BallDontLie API and retrieving data
- [Mocha Chai](): Plotting data in real-time on the front-end
## Other
- RESTful API: 

## ✍️ Authors <a name = "authors"></a>
- [@samcdonovan](https://github.com/samcdonovan)
