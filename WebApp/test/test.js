let server = require('../server');

let database = require('../database.js');
var sql = require('mysql');
const { response } = require('express');

let chai = require('chai');
let should = chai.should();
let assert = chai.assert;
let expect = chai.expect;

let chaiHttp = require('chai-http');
chai.use(chaiHttp);

//Create a connection object with the user details
var connectionPool = sql.createPool({
    connectionLimit: 1,
    host: "localhost",
    user: "root",
    password: "",
    database: "comparison_website",
    debug: false
});

describe('Database tests', () => {

    describe('#getAllBooks', () => {
        it('Returns all books in the database, count should equal number of books returned',
            async () => {
                try {

                    let books = await database.getAllBooks();

                    expect(books.count > 0).to.be.true;

                    /* the count value inside the books object should equal the length of the books object */
                    expect(books.count == books.length).to.be.true;

                    return;

                } catch (er) {
                    assert.fail(er);
                }
            });
    });

    describe('#getComparisons', () => {
        it('Returns all comparisons for a given book',
            async () => {
                try {
                    let books = await database.getAllBooks();
                    let comparisons = await database.getComparisons(books[0].isbn);

                    /* find all comparisons for the book and check that their ISBN's are equal */
                    for (var i = 0; i < comparisons.length; i++)
                        expect(books[0].isbn == comparisons[i].isbn).to.be.true;

                    return;

                } catch (er) {
                    assert.fail(er);
                }
            });
    });
    describe('#getBookInstances', () => {
        it('Returns all book instances which have a given book id',
            async () => {
                try {
                    let books = await database.getAllBooks();
                    let bookInstances = await database.getBookInstance(books[0].id);

                    /* find all comparisons for the book and check that their ISBN's are equal */
                    for (var i = 0; i < bookInstances.length; i++)
                        expect(books[0].id == bookInstances[i].id).to.be.true;

                    return;

                } catch (er) {
                    assert.fail(er);
                }
            });
    });
});

describe('Web Service', () => {

    describe('/GET comparisons?isbn=', () => {
        it('Returns the comparison info of the book with a certain ISBN', async () => {
            try {
                let books = await database.getAllBooks();

                chai.request(server)
                    .get('/comparisons?isbn=' + books[0].isbn)
                    .end((err, result) => {
                        result.should.have.status(200);

                        let comparisonObj = JSON.parse(result.text);

                        comparisonObj.data.should.be.a('array');

                        /* check that data inside comparison is valid */
                        if (comparisonObj.data.length > 0) {
                            comparisonObj.data[0].should.have.property('id');
                            comparisonObj.data[0].should.have.property('isbn');
                            comparisonObj.data[0].should.have.property('price');
                            comparisonObj.data[0].should.have.property('website_url');
                            expect(comparisonObj.data[0].isbn == books[0].isbn).to.be.true;
                        }

                    });

            } catch (er) {
                assert.fail(er);
            }
        });
    });

    describe('/GET books', () => {
        it('GET all books in the database and check that they have the required fields', async () => {
            try {
                chai.request(server)
                    .get('/books')
                    .end((err, result) => {
                        result.should.have.status(200);

                        let bookObj = JSON.parse(result.text);

                        bookObj.data.should.be.a('array');

                        expect(bookObj.data.length == bookObj.count).to.be.true;

                        /* check that data inside book is valid */
                        if (bookObj.data.length > 0) {
                            bookObj.data[0].should.have.property('book_id');
                            bookObj.data[0].should.have.property('title');
                            bookObj.data[0].should.have.property('author');
                            bookObj.data[0].should.have.property('description');
                            bookObj.data[0].should.have.property('image');
                            bookObj.data[0].should.have.property('title');
                        }

                    });

            } catch (er) {
                assert.fail(er);
            }
        });
    });
    describe('/GET books?num_items=12&offset=0&search=and', () => {
        it('GET the first 12 books that have "and" in their title', async () => {
            try {
                chai.request(server)
                    .get('/books?num_items=12&offset=0&search=and')
                    .end((err, result) => {
                        result.should.have.status(200);

                        let bookObj = JSON.parse(result.text);

                        bookObj.data.should.be.a('array');

                        /* check that num_items restriction works */
                        expect(bookObj.data.length == 12).to.be.true;

                        /* check that search terms match with returned books */
                        for (var i = 0; i < bookObj.data.length; i++) {
                            'and'.should.satisfy(function (searchTerm) {
                                if ((bookObj.data[i].title.toLowerCase().includes(searchTerm))
                                    || (bookObj.data[i].author.toLowerCase().includes(searchTerm))) {
                                    return true;
                                } else {
                                    return false;
                                }
                            });

                        }

                    });

            } catch (er) {
                assert.fail(er);
            }
        });
    });

    describe('/GET bookInstances?id=2', () => {
        it('GET the book instance details for the book with id 2', async () => {
            try {
                chai.request(server)
                    .get('/bookInstances?id=2')
                    .end((err, result) => {
                        result.should.have.status(200);

                        let bookInstanceObj = JSON.parse(result.text);

                        bookInstanceObj.data.should.be.a('array');

                        /* check that returned book instance is valid */
                        if (bookInstanceObj.data.length > 0) {
                            bookInstanceObj.data[0].should.have.property('isbn');
                            bookInstanceObj.data[0].should.have.property('book_id');
                            bookInstanceObj.data[0].book_id.should.equal(2);
                            bookInstanceObj.data[0].should.have.property('format');

                        }

                    });

            } catch (er) {
                assert.fail(er);
            }
        });
    });

});