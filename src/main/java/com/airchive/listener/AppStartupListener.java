package com.airchive.listener;

import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppStartupListener implements ServletContextListener {
  private static final Logger logger = Logger.getLogger(AppStartupListener.class.getName());

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    logger.info("AIrchive starting up.");
    logger.info("AIrchive initialization complete.");
  }

  @Override
  public void contextDestroyed(ServletContextEvent arg0) {
    logger.info("AIrchive shutting down.");
    logger.info("AIrchive shut down complete.");
  }
}
