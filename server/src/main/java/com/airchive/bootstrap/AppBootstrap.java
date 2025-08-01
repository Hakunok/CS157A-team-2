package com.airchive.bootstrap;

import com.airchive.db.DbConnectionManager;
import com.airchive.repository.*;
import com.airchive.service.*;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * The {@code AppBootstrap} class is an "application-wide bootstrap class" that initializes all
 * repositories and services and registers them with the servlet context for dependency injection
 * across the REST resources.
 * This class implements {@link ServletContextListener}, which is automatically invoked during
 * application startup/shutdown by the servlet container (Tomcat in our case).
 */
public class AppBootstrap implements ServletContextListener {

  /**
   * Called automatically when the application context is initialized.
   * Sets up and registers all services and their dependencies so that they can be used by the
   * REST resources.
   * @param sce the context event provided by the servlet container
   */
  @Override
  public void contextInitialized(ServletContextEvent sce) {

    ServletContext ctx = sce.getServletContext();

    // Initialize repository layer
    var personRepository = new PersonRepository();
    var accountRepository = new AccountRepository();
    var authorRequestRepository = new AuthorRequestRepository();
    var topicRepository = new TopicRepository();
    var publicationRepository = new PublicationRepository();
    var publicationTopicRepository = new PublicationTopicRepository();
    var publicationAuthorRepository = new PublicationAuthorRepository();
    var collectionRepository = new CollectionRepository();
    var collectionItemRepository = new CollectionItemRepository();
    var interactionRepository = new InteractionRepository();
    var recommendationRepository = new RecommendationRepository();

    // Initialize service layer
    var personAccountService = new PersonAccountService(
        personRepository,
        accountRepository,
        collectionRepository
    );

    var authorRequestService = new AuthorRequestService(
        authorRequestRepository,
        accountRepository
    );

    var topicService = new TopicService(
        topicRepository
    );

    var publicationService = new PublicationService(
        publicationRepository,
        publicationTopicRepository,
        publicationAuthorRepository,
        interactionRepository,
        recommendationRepository,
        personRepository,
        topicRepository,
        collectionItemRepository
    );

    var collectionService = new CollectionService(
        collectionRepository,
        collectionItemRepository,
        recommendationRepository
    );

    // Register services in servlet context
    ctx.setAttribute("personAccountService", personAccountService);
    ctx.setAttribute("authorRequestService", authorRequestService);
    ctx.setAttribute("topicService", topicService);
    ctx.setAttribute("publicationService", publicationService);
    ctx.setAttribute("collectionService", collectionService);
  }

  /**
   * Called automatically when the application context is being destroyed.
   * Closes the HikariCP connection pool.
   * @param sce the context event provided by the servlet container
   */
  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    DbConnectionManager.closePool();
  }
}