package com.airchive.bootstrap;

import com.airchive.db.DbConnectionManager;
import com.airchive.repository.*;
import com.airchive.service.*;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Application bootstrap class for initializing and wiring services and repositories.
 * <p>
 * This class implements {@link ServletContextListener} and is automatically loaded by the servlet
 * container (Tomcat) during application startup and shutdown.
 * <p>
 * On startup, it creates and registers singleton service instances into the global {@link ServletContext}.
 * These services can then be retrieved throughout the application, specifically within our REST resources, via
 * {@code context.getAttribute(...)}, enabling simple and manual dependency injection.
 * <p>
 * On shutdown, it cleans up shared resources such as the HikariCP database connection pool.
 * <p>
 * This class should be registered in {@code web.xml} so that it can be loaded by the servlet container.
 */
public class AppBootstrap implements ServletContextListener {

  /**
   * Called automatically when the application context is initialized.
   * <p>
   * This method instantiates all repositories and services required by the application, and then
   * registers them in the {@link ServletContext}, making them globally accessible across the application.
   *
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
        collectionRepository,
        recommendationRepository
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
        recommendationRepository,
        interactionRepository,
        publicationAuthorRepository,
        publicationTopicRepository,
        publicationRepository
    );

    var interactionService = new InteractionService(
        interactionRepository,
        publicationRepository
    );

    // Register services with ServletContext
    // Retrieve services via: context.getAttribute("<serviceName>")
    ctx.setAttribute("personAccountService", personAccountService);
    ctx.setAttribute("authorRequestService", authorRequestService);
    ctx.setAttribute("topicService", topicService);
    ctx.setAttribute("publicationService", publicationService);
    ctx.setAttribute("collectionService", collectionService);
    ctx.setAttribute("interactionService", interactionService);
  }

  /**
   * Called automatically when the application context is being destroyed.
   * <p>
   * This method ensures proper shutdown and cleanup of shared application resources. At the moment the only
   * resource that needs to be closed is the HikariCP database connection pool.
   *
   * @param sce the context event provided by the servlet container
   */
  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    DbConnectionManager.closePool();
  }
}