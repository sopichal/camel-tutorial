package com.tutorial.camel;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
// import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.main.Main;
import org.apache.camel.component.properties.PropertiesComponent;
// import jakarta.jms.ConnectionFactory;

public class FtpToJMSExample {
  public static void main(String args[]) throws Exception {

    System.out.println("Current working directory: " + System.getProperty("user.dir"));
    Main main = new Main();

    // // ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616?user=artemis&password=artemis");
    // ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("failover:(tcp://localhost:61616)?initialReconnectDelay=1000");
    // connectionFactory.setUser("artemis");
    // connectionFactory.setPassword("artemis");

    main.configure().addRoutesBuilder(new RouteBuilder() {
      @Override
      public void configure() throws Exception {
        // Get the Camel context
        CamelContext context = this.getCamelContext();

        PropertiesComponent propertiesComponent = new PropertiesComponent();
        propertiesComponent.setLocation("classpath:config.properties");

        context.setPropertiesComponent(propertiesComponent);

        // Resolve the properties manually before using them
        String brokerUrl = context.resolvePropertyPlaceholders("{{broker.url}}");
        String brokerUser = context.resolvePropertyPlaceholders("{{broker.user}}");
        String brokerPassword = context.resolvePropertyPlaceholders("{{broker.password}}");

        // Create ActiveMQConnectionFactory with credentials
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        connectionFactory.setUser(brokerUser);
        connectionFactory.setPassword(brokerPassword);

        JmsComponent jmsComponent = new JmsComponent();
        jmsComponent.setConnectionFactory(connectionFactory);
        jmsComponent.setPreserveMessageQos(true);

        context.addComponent("jms", jmsComponent);

        // from("ftp://localhost:2121/orders?username=rider&password=secret&delay=5000").to("jms:incomingOrders");
        from("ftp://{{ftp.server}}:{{ftp.port}}/orders?username={{ftp.user}}&password={{ftp.password}}&delay=5000&move=done&autoCreate=true")
          .to("jms:incomingOrders");
        // from("ftp://localhost:2121/orders?username=rider&password=secret")
        //  .to("file:data/outbox");

        from("jms:incomingOrders")
          .choice()
            .when(header("CamelFileName").endsWith(".xml"))
              .to("jms:xmlOrders")
            .when(header("CamelFileName").regex("^.*(csv|csl)$"))
              .to("jms:csvOrders")
            .otherwise()
              .to("jms:badOrders");

        // from("jms:xmlOrders")
        //   .log("Received XML order: ${header.CamelFileName}")
        //   .to("mock:xml");

        // from("jms:csvOrders")
        //   .log("Received CSV order: ${header.CamelFileName}")
        //   .to("mock:csv");
      }
    });
    // main.start();
    // Thread.sleep(60000);
    // main.stop();
    main.run();
  }

}
