package com.airchive.config;

import com.airchive.dao.AuthorDAO;
import com.airchive.dao.AuthorRequestDAO;
import com.airchive.dao.TopicDAO;
import com.airchive.dao.TopicInteractionDAO;
import com.airchive.dao.UserDAO;
import com.airchive.datasource.DriverManagerDataSource;
import com.airchive.service.AuthorRequestService;
import com.airchive.service.AuthorService;
import com.airchive.service.TopicInteractionService;
import com.airchive.service.TopicService;
import com.airchive.service.UserService;
import com.airchive.util.ApplicationContextProvider;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;

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
public class ServletContextListener implements javax.servlet.ServletContextListener {
  private static final Logger logger = Logger.getLogger(ServletContextListener.class.getName());

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {

      // Get ServletContext
      ServletContext context = sce.getServletContext();
      ApplicationContextProvider.setServletContext(context);

      // Setup Logger
      setupLogger(context);

      // Setup DataSource
      context.setAttribute("dataSource", new DriverManagerDataSource());

      // Setup DAOs & Services
      context.setAttribute("userDAO", new UserDAO());
      context.setAttribute("userService", new UserService());

      context.setAttribute("authorDAO", new AuthorDAO());
      context.setAttribute("authorService", new AuthorService());

      context.setAttribute("authorRequestDAO", new AuthorRequestDAO());
      context.setAttribute("authorRequestService", new AuthorRequestService());

      context.setAttribute("topicDAO", new TopicDAO());
      context.setAttribute("topicService", new TopicService());

      context.setAttribute("topicInteractionDAO", new TopicInteractionDAO());
      context.setAttribute("topicInteractionService", new TopicInteractionService());

    } catch (Exception e) {
      logger.severe("Initialization failed: " + e.getMessage());
      throw new RuntimeException("Application initialization failed", e);
    }

    logger.info("Application initialized.");
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    logger.info("Application shutting down....");
    Object ds = sce.getServletContext().getAttribute("dataSource");
    if (ds instanceof AutoCloseable) {
      try {
        ((AutoCloseable) ds).close();
      } catch (Exception e) {
        logger.warning("Failed to close DataSource: " + e.getMessage());
      }
    }

    logger.info("Context destroyed, application shut down successfully.");
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