package com.airchive.config;

import com.airchive.dao.UserDAO;
import com.airchive.datasource.DriverManagerDataSource;
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
      // Get ServletContext
      ServletContext servletContext = sce.getServletContext();

      // Setup Logger
      setupLogger(servletContext);

      // Setup DataSource
      DataSource dataSource = new DriverManagerDataSource();
      servletContext.setAttribute("dataSource", dataSource);
      logger.info("DataSource initialized.");

      // Setup DAOs
      UserDAO userDAO = new UserDAO(servletContext);
      servletContext.setAttribute("userDAO", userDAO);
      logger.info("DAOs initialized.");

      // Setup Services
      UserService userService = new UserService(servletContext);
      servletContext.setAttribute("userService", userService);
      logger.info("Services initialized.");
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