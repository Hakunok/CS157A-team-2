package com.airchive;

import com.airchive.dao.UserDAO;
import com.airchive.datasource.DriverManagerDataSource;
import com.airchive.service.UserService;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;

/**
 * Application initializer for setting up the servlet context and application dependencies during startup.
 */
@WebListener
public class AppInitializer implements ServletContextListener {
  private static final Logger logger = Logger.getLogger(AppInitializer.class.getName());

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {
      // Setup logging
      try (InputStream config = getClass().getResourceAsStream("/logger.properties")) {
        if (config != null) {
          LogManager.getLogManager().readConfiguration(config);
        } else {
          logger.warning("logger.properties not found, using default configuration");
        }
      }

      // Get ServletContext
      ServletContext servletContext = sce.getServletContext();

      // Setup DataSource
      DataSource dataSource = new DriverManagerDataSource();
      servletContext.setAttribute("dataSource", dataSource);
      logger.info("DataSource initialized.");

      // Setup DAOs
      UserDAO userDAO = new UserDAO(dataSource);
      servletContext.setAttribute("userDAO", userDAO);
      logger.info("DAOs instantiated and added to servlet context.");

      // Setup Services
      UserService userService = new UserService(userDAO);
      servletContext.setAttribute("userService", userService);
      logger.info("Services instantiated and added to servlet context.");
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
}