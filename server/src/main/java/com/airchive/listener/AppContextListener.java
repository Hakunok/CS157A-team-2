package com.airchive.listener;

import com.airchive.repository.AuthorRepository;
import com.airchive.repository.AuthorRequestRepository;
import com.airchive.repository.UserRepository;
import com.airchive.service.AuthorRequestService;
import com.airchive.service.AuthorService;
import com.airchive.service.UserService;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * The {@code ServletContextListener} class is a custom implementation of the
 * {@code javax.servlet.ServletContextListener} interface, which listens for
 * changes to the lifecycle of the Servlet context.
 *
 * Main responsibilities include:
 * - Initializing a {@code DriverManagerDataSource} for database connectivity.
 * - Creating and storing DAOs and services in the Servlet context.
 * - Handling any exceptions and ensuring the proper cleanup of resources.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

  private static final Logger logger = Logger.getLogger(AppContextListener.class.getName());

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {

      // Get servlet context
      ServletContext context = sce.getServletContext();

      // Setup logger
      setupLogger(context);

      // Setup repositories & services

      // Instantiate repositories
      UserRepository userRepository = new UserRepository();
      AuthorRequestRepository authorRequestRepository = new AuthorRequestRepository();
      AuthorRepository authorRepository = new AuthorRepository();

      // Instantiate services and inject repositories
      UserService userService = new UserService(userRepository);
      AuthorRequestService authorRequestService = new AuthorRequestService(authorRequestRepository, userRepository, authorRepository);
      AuthorService authorService = new AuthorService(authorRepository, userRepository);

      // Add services to servlet context
      context.setAttribute("userService", userService);
      context.setAttribute("authorRequestService", authorRequestService);
      context.setAttribute("authorService", authorService);

    } catch (Exception e) {
      logger.severe("Initialization failed: " + e.getMessage());
      throw new RuntimeException("Application initialization failed", e);
    }

    logger.info("Application initialized.");
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
  }

  private void setupLogger(ServletContext context) throws IOException {
    String logPath = context.getRealPath("/WEB-INF/logs");

    File logDir = new File(logPath);
    if (!logDir.exists()) {
      logDir.mkdirs();
    }

    FileHandler fileHandler = new FileHandler(logPath + File.separator + "app-%u-%g.log", 5000000, 3, true);
    fileHandler.setFormatter(new SimpleFormatter());

    Logger rootLogger = Logger.getLogger("");
    for (var handler : rootLogger.getHandlers()) {
      rootLogger.removeHandler(handler);
    }
    rootLogger.addHandler(fileHandler);

    try (InputStream config = getClass().getResourceAsStream("/logger.properties")) {
      if (config != null) {
        LogManager.getLogManager().readConfiguration(config);
      }
    }
  }
}