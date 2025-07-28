package com.airchive.bootstrap;

import com.airchive.repository.AccountRepository;
import com.airchive.repository.AuthorRequestRepository;
import com.airchive.repository.CollectionItemRepository;
import com.airchive.repository.CollectionRepository;
import com.airchive.repository.InteractionRepository;
import com.airchive.repository.PersonRepository;
import com.airchive.repository.PublicationAuthorRepository;
import com.airchive.repository.PublicationRepository;
import com.airchive.repository.PublicationTopicRepository;
import com.airchive.repository.RecommendationRepository;
import com.airchive.repository.TopicRepository;
import com.airchive.service.PublicationService;
import com.airchive.service.TopicService;
import com.airchive.service.AuthorRequestService;
import com.airchive.service.PersonAccountService;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class AppBootstrap implements ServletContextListener {

  @Override
  public void contextInitialized(ServletContextEvent sce) {

    ServletContext ctx = sce.getServletContext();

    // Create repositories
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

    // Create services
    var personAccountService = new PersonAccountService(personRepository,
        accountRepository, collectionRepository);
    var authorRequestService = new AuthorRequestService(authorRequestRepository,
        accountRepository);
    var topicService = new TopicService(topicRepository);
    //var publicationService = new PublicationService(publicationRepository,);


    // Set up servlet context
    ctx.setAttribute("personAccountService", personAccountService);
    ctx.setAttribute("authorRequestService", authorRequestService);
    ctx.setAttribute("topicService", topicService);
    //ctx.setAttribute("publicationService", publicationService);

  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {}
}
