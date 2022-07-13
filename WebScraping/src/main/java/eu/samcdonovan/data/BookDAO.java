package eu.samcdonovan.data;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * Book Data Access Object class, contains functions that deal with Hibernate
 * database queries to be used when inserting, searching and deleting to/from
 * the database
 */
public class BookDAO {

    private SessionFactory sessionFactory;

    /**
     * Empty constructor for Spring Beans class dependency injection
     */
    public BookDAO() {
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Closes Hibernate down and stops its threads from running
     */
    public void shutDown() {
        sessionFactory.close();
    }

    /**
     * Cleans the data in the Book object; removes characters that either cause
     * issues when inserting into the database, or cause issues when comparing
     * books e.g. "Harry Potter and The Philosopher's Stone" would not be
     * considered equal to "Harry Potter and The Philosophers Stone", so the
     * apostrophe is removed to account for that
     *
     * @param book Book to be cleaned
     */
    public void cleanBook(Book book) {
        String newTitle = book.getTitle().split(":")[0].split("\\(")[0].replace(":", "")
                .replaceAll("\u0027", "").replace("’", "").replace("'", "").replace(",", "").trim();

        String newAuthor = book.getAuthor().split(":")[0].split("\\(")[0]
                .replace(":", "").replace("'", "").replace(",", "")
                .replace(".", "").trim();

        String newIsbn = book.getIsbn().replace("-", "").trim();
        String newFormat = book.getFormat().split("\\(")[0].replaceAll("\\)", "").trim();

        /* set the books fields to the newly updated fields */
        book.setTitle(newTitle);
        book.setAuthor(newAuthor);
        book.setIsbn(newIsbn);
        book.setFormat(newFormat);
    }

    /**
     * Adds a Book along with its BookInstance and Comparisons to the database.
     * This is the main function called for inserting into the database, it
     * handles all of the insertion functions
     *
     * @param book Book to be added to the database
     */
    public void addToDatabase(Book book) {

        /* clean the Books data before doing anything with it */
        cleanBook(book);

        /* create a new Book and set its variables */
        Book newBook = new Book();
        newBook.setTitle(book.getTitle());
        newBook.setAuthor(book.getAuthor());
        newBook.setDescription(book.getDescription());
        newBook.setImage(book.getImage());
        newBook.setIsbn(book.getIsbn());

        /* add the Book to the database or return it if it already exists */
        newBook = addBook(newBook);
        if (newBook != null) {
            /* create a new BookInstance and set its variables */
            BookInstance newInstance = new BookInstance();
            newInstance.setBook(newBook);
            newInstance.setIsbn(book.getIsbn());
            newInstance.setFormat(book.getFormat());

            /* add the BookInstance to the database or return it if it already exists */
            newInstance = addInstance(newInstance);

            /* addInstance will return null if there is already a bookInstance that points to same book
               but has a different isbn. This helps eliminate some duplicates*/
            if (newInstance != null) {
                /* create a new Comparison and set its variables */
                Comparison newComparison = new Comparison();
                newComparison.setBookInstance(newInstance);
                newComparison.setPrice(book.getPrice());
                newComparison.setWebsiteUrl(book.getWebsiteUrl());

                /* add the Comparison to the database or return it if it already exists */
                addComparison(newComparison);
            }
        }
    }

    /**
     * Book insertion function; checks if the book exists in the database and if
     * it doesn't, inserts a new book and otherwise either updates the book (if
     * it has an empty description) or returns the book from the database
     *
     * @param book Book to be inserted into the database
     * @return Newly inserted Book or the Book from the database
     */
    public Book addBook(Book book) {

        // get a new Session instance from the session factory
        Session session = sessionFactory.getCurrentSession();

        // start session transaction
        session.beginTransaction();

        /* build HQL book_instances query; checks if the isbn already exists in the database */
        String isbnQuery = "from BookInstance where isbn='" + book.getIsbn() + "'";

        /* retrieve lists containing Book and BookInstance objects which satisfy the queries */
        List<BookInstance> bookInstanceList = session.createQuery(isbnQuery).getResultList();

        /* if either list has any objects in them, the book already exists in the database */
        if (bookInstanceList.size() > 0) {
            String bookQuery = "from Book where id='" + bookInstanceList.get(0).getBook().getId() + "'";

            List<Book> books = session.createQuery(bookQuery).getResultList();
            session.close();

            /* if the book in the database has no description, update it with the description from the input book */
            if (books.get(0).getDescription().equals("")) {
                books.get(0).setDescription(book.getDescription());
                updateBook(books.get(0));
            }
            /* return the book from the database */
            return books.get(0);
        }

        /* if the book doesn't exist in the database already, insert it and commit the transaction */
        session.save(book);
        session.getTransaction().commit();
        session.close(); // close the session and release database connection

        System.out.println("Book added in database. ID: " + book.getId());

        return book;
    }

    /**
     * Updates a books details in the database
     *
     * @param book Book to be updated
     */
    public void updateBook(Book book) {

        // get a new Session instance from the session factory
        Session session = sessionFactory.getCurrentSession();

        // start transaction
        session.beginTransaction();

        // update the Book in the database with the new details
        session.update(book);
        session.getTransaction().commit();

        // close the session and release database connection
        session.close();
        System.out.println("Book updated in database. ID: " + book.getId());
    }

    /**
     * BookInstance insertion function; checks if the BookInstance exists in the
     * database and if it doesn't, inserts a new BookInstance and otherwise
     * either updates the BookInstance in the database
     *
     * @param bookInstance BookInstance to be inserted into the database
     * @return Newly inserted BookInstance or the BookInstance from the database
     */
    public BookInstance addInstance(BookInstance bookInstance) {

        // get a new Session instance from the session factory
        Session session = sessionFactory.getCurrentSession();

        // start transaction
        session.beginTransaction();

        /* build HQL book_instances query; checks if the isbn already exists in the database */
        String queryStr = "from BookInstance where isbn='" + bookInstance.getIsbn() + "'";

        /* build HQL book_instances query; checks if a book instance exists with the same book id and format as this
        one, but a different ISBN. this helps eliminate duplicates */
        String duplicateCheck = "from BookInstance where book='" + bookInstance.getBook().getId() + "' AND "
                + "format='" + bookInstance.getFormat() + "' AND isbn!='" + bookInstance.getIsbn() + "'";

        /* retrieve lists containing BookInstance objects which satisfy the query */
        List<BookInstance> bookInstanceList = session.createQuery(queryStr).getResultList();
        List<BookInstance> duplicateCheckList = session.createQuery(duplicateCheck).getResultList();

        session.close(); // close the session and release database connection

        if (duplicateCheckList.size() > 0)
            return null;

        /* if the book instance already exists in the database, return that BookInstance,
           otherwise add a new BookInstance to the database*/
        if (bookInstanceList.size() > 0) {
            return bookInstanceList.get(0);
        } else {
            return saveInstance(bookInstance);
        }
    }

    /**
     * Saves a BookInstance to the database; should only be called after
     * checking that the BookInstance does not already exist
     *
     * @param bookInstance BookInstance to add to the database
     * @return Newly added BookInstance
     */
    public BookInstance saveInstance(BookInstance bookInstance) {
        Session session = sessionFactory.getCurrentSession();

        // start transaction
        session.beginTransaction();
        session.save(bookInstance);
        /* save the new instance to the database */
        session.getTransaction().commit();
        session.close();

        System.out.println("Instance added in database. ISBN: " + bookInstance.getIsbn());
        return bookInstance;
    }

    /**
     * Comparison insertion function; checks if the Comparison exists in the
     * database and if it doesn't, inserts a new Comparison and otherwise either
     * updates the Comparison in the database
     *
     * @param comparison Comparison to be inserted into the database
     * @return Newly inserted Comparison or the Comparison from the database
     */
    public Comparison addComparison(Comparison comparison) {

        // get a new Session instance from the session factory
        Session session = sessionFactory.getCurrentSession();

        // start transaction
        session.beginTransaction();

        String websiteName;

        /* get the website name from the url of the comparison, used to run a like query */
        if (comparison.getWebsiteUrl().contains("dauntbooks"))
            websiteName = "dauntbooks";
        else if (comparison.getWebsiteUrl().contains("blackwells"))
            websiteName = "blackwells";
        else
            websiteName = comparison.getWebsiteUrl().split("\\.")[1];

        /* build HQL comparisons query; checks if a comparison with the same isbn 
           and from the same website exists in the database */
        String queryStr = "FROM Comparison c WHERE c.bookInstance.isbn='" + comparison.getBookInstance().getIsbn() + "'"
                + " AND c.websiteUrl LIKE '%" + websiteName + "%'";

        /* retrieve list containing Comparison objects which satisfy the queries */
        List<Comparison> comparisonList = session.createQuery(queryStr).getResultList();

        /* if the comparison already exists, update it with the input comparisons price and url */
        if (comparisonList.size() > 0) {
            /* update price and website of comparison */
            comparisonList.get(0).setPrice(comparison.getPrice());
            comparisonList.get(0).setWebsiteUrl(comparison.getWebsiteUrl());
            session.update(comparisonList.get(0));

            session.getTransaction().commit();

            session.close();
            return comparisonList.get(0);
        } else {

            /* otherwise save the comparison */
            session.close();
            return saveComparison(comparison);
        }
    }

    /**
     * Saves a Comparison to the database; should only be called after checking
     * that the Comparison does not already exist
     *
     * @param comparison Comparison to add to the database
     * @return Newly added Comparison
     */
    public Comparison saveComparison(Comparison comparison) {

        // get a new Session instance from the session factory
        Session session = sessionFactory.getCurrentSession();

        // start session transaction
        session.beginTransaction();

        // add the Comparison to the database
        session.save(comparison);
        session.getTransaction().commit();

        // close the session and release database connection
        session.close();
        System.out.println("Comparison added in database. ID: " + comparison.getId() + ", website: " + comparison.getWebsiteUrl());

        /* return newly added Comparison */
        return comparison;
    }

    /**
     * Resets database settings before inserting anything into the database
     */
    public void resetDatabase() {

        Session session = sessionFactory.getCurrentSession();

        session.beginTransaction();

        /* reset the auto increment value to 0 because it did not do it automatically */
        session.createSQLQuery("ALTER TABLE comparisons AUTO_INCREMENT=0").executeUpdate();
        session.createSQLQuery("ALTER TABLE book_instances AUTO_INCREMENT=0").executeUpdate();
        session.createSQLQuery("ALTER TABLE books AUTO_INCREMENT=0").executeUpdate();

        session.getTransaction().commit();
        session.close();
    }

    /**
     * Searches for all books that satisfy the query
     *
     * @param query HQL query to run on database
     * @return List containing all books that satisfy the query
     */
    public List<Book> searchBooks(String query) {

        List<Book> booksFound = new ArrayList<Book>();

        Session session = sessionFactory.getCurrentSession();

        session.beginTransaction();

        /* execute query on database and add to list */
        List<Book> bookList = session.createQuery(query).getResultList();

        for (Book book : bookList) {
            booksFound.add(book);
        }

        session.close();
        return booksFound;
    }

    /**
     * Deletes a given book from the database
     *
     * @param book Book to be deleted
     */
    public void deleteBook(Book book) {

        Session session = sessionFactory.getCurrentSession();

        session.beginTransaction();

        /* search for book with the exact same title and author as the input book */
        String queryStr = "from Book where title='" + book.getTitle() + "' AND author='" + book.getAuthor() + "'";

        List<Book> bookList = session.createQuery(queryStr).getResultList();

        /* if the book exists in the database, delete it */
        if (bookList.size() > 0) {
            Object persistentInstance = session.load(Book.class,
                    bookList.get(0).getId());

            // delete object (and corresponding data) if a match is found
            if (persistentInstance != null) {
                session.delete(persistentInstance);
            }

            session.getTransaction().commit();
            /* commit change */
        }

        session.close();
        System.out.println("Deleted book from database. ID: " + book.getId());
    }

}
