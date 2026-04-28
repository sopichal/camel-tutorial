package com.tutorial.camel;

import org.apache.camel.main.Main;

public class JmsRequestReplyResponderExample {

  public static void main(String[] args) throws Exception {
    Main main = new Main();
    main.setPropertyPlaceholderLocations("classpath:config.properties");
    main.configure().addRoutesBuilder(new JmsRequestReplyResponderRoute());
    main.run();
  }
}
