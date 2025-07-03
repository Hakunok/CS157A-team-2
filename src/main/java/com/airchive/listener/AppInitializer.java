package com.airchive.listener;

import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppInitializer implements ServletContextListener {

  private static final Logger logger = Logger.getLogger(AppInitializer.class.getName());

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    logger.info("Application initialized.");
  }

  @Override
  public void contextDestroyed(ServletContextEvent arg0) {
    logger.info("Application shutting down...");
  }
}
