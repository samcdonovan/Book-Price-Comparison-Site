//Import the express and url modules
var express = require('express');
var url = require("url");
var bodyParser = require('body-parser')

//Status codes defined in external file
require('./http_status.js');
var database = require('./database.js');

var app = express();
app.use(bodyParser.json());

app.get('/books', getRequest);
app.get('/comparisons', getRequest);
app.get('/bookInstances', getRequest);

app.use(express.static(__dirname + '/public'));

// start the app listening on port 8080
app.listen(8080);

/* Handles GET request sent to all paths
   Processes path and query string and calls appropriate functions to
   return the data */
async function getRequest(request, response) {

    let urlObj = url.parse(request.url, true); /* parse url */

    let queries = urlObj.query; /* extract query parameters */

    let pathArray = urlObj.pathname.split("/");

    let pathEnd = pathArray[pathArray.length - 1]; /* get last part of path */

    try {
        /* depending on what the path end of the query is, pass the 
        query parameters to the respective function */
        if (pathEnd === 'books') {
            returnObj = await handleBooks(queries);
            response.json(returnObj);
        }

        if (pathEnd === 'bookInstances') {
            returnObj = await handleBookInstances(queries);
            response.json(returnObj);
        }

        if (pathEnd === 'comparisons') {
            returnObj = await handleComparisons(queries);
            response.json(returnObj);
        }

        //If the last part of the path is a valid product id, return data about that product
        let regEx = new RegExp('^[0-9]+$');//RegEx returns true if string is all digits.
        if (regEx.test(pathEnd)) {
            let book = await database.getBookWithID(pathEnd);
            response.json(book);
        }
    }
    catch (ex) {
        response.status(HTTP_STATUS.NOT_FOUND);
        response.send("{error: '" + JSON.stringify(ex) + "', url: " + request.url + "}");
    }
}

/*
Handles GET paths for the books table
*/
async function handleBooks(queries) {
    let numItems = queries['num_items'];
    let offset = queries['offset'];
    let searchTerm = queries['search'];
    let id = queries['id'];
    let books;

    if (searchTerm !== undefined) /* if there is a search term, retrieve books that satisfy that search */
        books = await database.getBookSearch(numItems, offset, searchTerm);
    else if (id !== undefined) /* if there is an id, retrieve the book with that id */
        books = await database.getBookWithID(id);
    else /* otherwise, return all books */
        books = await database.getAllBooks(numItems, offset);

    // combine into a single object and send back to client 
    let returnObj = {
        count: books.count,
        data: books
    }

    return returnObj;
}

/*
Handles GET paths for the book_instances table
*/
async function handleBookInstances(queries) {

    let id = queries['id'];

    /* retrieve all book instances with a specific id */
    let bookInstances = await database.getBookInstance(id);

    // combine into a single object and send back to client
    let returnObj = {
        count: bookInstances.countResult,
        data: bookInstances
    }
    return returnObj;
}

/*
Handles GET paths for the comparisons table
*/
async function handleComparisons(queries) {

    let isbn = queries['isbn'];

    /* retrieve all comparisons with specific isbn */
    let comparisonBooks = await database.getComparisons(isbn);

    // combine into a single object and send back to client
    let returnObj = {
        count: comparisonBooks.countResult,
        data: comparisonBooks
    }

    return returnObj;
}

module.exports = app;
