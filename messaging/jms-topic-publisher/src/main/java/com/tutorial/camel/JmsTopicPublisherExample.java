package com.tutorial.camel;

import org.apache.camel.main.Main;

public class JmsTopicPublisherExample {

  public static void main(String[] args) throws Exception {
    Main main = new Main();
    main.setPropertyPlaceholderLocations("classpath:config.properties");
    main.configure().addRoutesBuilder(new JmsTopicPublisherRoute());
    main.run();
  }
}
