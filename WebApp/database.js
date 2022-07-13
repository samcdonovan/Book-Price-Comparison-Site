var sql = require('mysql');

//Create a connection object with the user details
var connectionPool = sql.createPool({
    connectionLimit: 1,
    host: "localhost",
    user: "root",
    password: "",
    database: "comparison_website",
    debug: false
});

/** Main query execution function; takes the SQL query as well as a query to get the count
 * for that SQL query, wraps connection pool query in a promise and returns the promise */
async function executeQuery(sqlQuery, countQuery) {

    let queryPromise = new Promise((resolve, reject) => {

        connectionPool.query(sqlQuery, function (err, result) {

            // check for errors
            if (err) {
                // reject promise if there are errors
                reject(err);
            }
            resolve(result);
        });
    });

    /* retrieve the count for SQL query */
    let countPromise = new Promise((resolve, reject) => {
        connectionPool.query(countQuery, function (err, countResult) {
            // check for errors
            if (err) {
                // reject promise if there are errors
                reject(err);
            }
            // resole promise with data from database.
            resolve(countResult);

        });
    });

    // put both promises into an object and return that object
    let count = await countPromise;
    let returnPromise = await queryPromise;
    returnPromise.count = count[0]["COUNT(*)"];

    return returnPromise;
}

/*
Gets all books that satisfy a search term; used in the front 
end of the website and in the RESTful API
*/
module.exports.getBookSearch = async (numItems, offset, searchTerm) => {

    /* look for books where the searfch term is in the title or the author of the book */
    let query = "SELECT books.*, book_instances.isbn, book_instances.format " +
        "FROM (books INNER JOIN book_instances ON books.book_id=book_instances.book_id) " +
        "WHERE (books.author LIKE '%" + searchTerm + "%' OR books.title LIKE '%" + searchTerm + "%')";

    let countQuery = "SELECT COUNT(*) FROM (books INNER JOIN book_instances ON books.book_id=book_instances.book_id) " +
        "WHERE (books.author LIKE '%" + searchTerm + "%' OR books.title LIKE '%" + searchTerm + "%')";

    /* limit the number of results returned, if this has been specified in the query string. 
        used for pagination in the front end of the website*/
    if (numItems !== undefined && offset !== undefined) {
        query += "ORDER BY books.book_id LIMIT " + numItems + " OFFSET " + offset;

    }

    // return promise to run query
    return executeQuery(query, countQuery);
}

/*
 Gets a book from the database with the specified ID; used in the RESTful API
*/
module.exports.getBookWithID = async (bookId, numItems, offset) => {
    let query = "SELECT books.*, book_instances.isbn, book_instances.format " +
        "FROM (books INNER JOIN book_instances ON books.book_id=book_instances.book_id) " +
        "WHERE books.book_id = " + bookId;


    let countQuery = "SELECT COUNT(*) FROM books WHERE books.book_id = " + bookId;

    // limit the number of results returned, if this has been specified in the query string
    if (numItems !== undefined && offset !== undefined) {
        query += "ORDER BY books.book_id LIMIT " + numItems + " OFFSET " + offset;
    }

    // return promise to run query
    return executeQuery(query, countQuery);
}

/*
 Gets every book from the database, joined on the book_instances table
*/
module.exports.getAllBooks = async (numItems, offset) => {
    let query = "SELECT books.*, book_instances.isbn, book_instances.format " +
        "FROM (books INNER JOIN book_instances ON books.book_id=book_instances.book_id)";

    let countQuery = "SELECT COUNT(*) FROM (books INNER JOIN book_instances ON books.book_id=book_instances.book_id)";

    // limit the number of results returned, if this has been specified in the query string
    if (numItems !== undefined && offset !== undefined) {
        query += "ORDER BY books.book_id LIMIT " + numItems + " OFFSET " + offset;

    }

    // return promise to run query
    return executeQuery(query, countQuery);
}

/*
Gets a comparison with a specific ISBN; used in the front end of the website
and also for the RESTful API
*/
module.exports.getComparisons = async (isbn) => {
    let query = "SELECT comparisons.* FROM comparisons";
    let countQuery = "SELECT COUNT(*) FROM comparisons";
    if (isbn !== undefined) {
        query += " WHERE comparisons.isbn = " + isbn + " ORDER BY comparisons.price";
        countQuery += " WHERE comparisons.isbn = " + isbn;
    }
    return executeQuery(query, countQuery);
}

/*
Gets a book instance with a specific book id; used in the RESTful API
*/
module.exports.getBookInstance = async (bookId) => {

    let query = "SELECT book_instances.* FROM book_instances";

    let countQuery = "SELECT COUNT(*) FROM book_instances";

    if (bookId !== undefined) {
        query += " WHERE book_instances.book_id = " + bookId;
        countQuery += " WHERE book_instances.book_id = " + bookId;
    }

    // return promise to run query
    return executeQuery(query, countQuery);
}
