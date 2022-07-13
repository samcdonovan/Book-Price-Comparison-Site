package eu.samcdonovan.controller;

import eu.samcdonovan.application.AppConfig;
import eu.samcdonovan.application.ScraperManager;
import java.util.Scanner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Main class, retrieves the ScraperManager beans and then runs the scraper
 * threads
 */
public class Main {

    public static void main(String[] args) {

        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        ScraperManager manager = (ScraperManager) context.getBean("scraperManager");

        /* start all scraper threads */
        manager.startThreads();

        /* user can stop the threads by typing and entering "stop" */
        Scanner scanner = new Scanner(System.in);
        String userInput = scanner.nextLine();
        while (!userInput.equals("stop")) {
            userInput = scanner.nextLine();
        }

        /* stop all threads and close their web drivers */
        manager.stopThreads();
    }

}
