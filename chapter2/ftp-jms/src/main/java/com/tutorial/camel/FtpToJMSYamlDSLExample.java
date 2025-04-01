package com.tutorial.camel;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.main.Main;
import org.apache.camel.component.properties.PropertiesComponent;

public class FtpToJMSYamlDSLExample {
  public static void main(String[] args) throws Exception {
    System.out.println("Current working directory: " + System.getProperty("user.dir"));
    Main main = new Main();

    // Add properties
    main.init();
    PropertiesComponent propertiesComponent = new PropertiesComponent();
    propertiesComponent.setLocation("classpath:config.properties");
    main.getCamelContext().setPropertiesComponent(propertiesComponent);

    CamelContext camelContext = main.getCamelContext();
    String brokerUrl = camelContext.resolvePropertyPlaceholders("{{broker.url}}");
    String brokerUser = camelContext.resolvePropertyPlaceholders("{{broker.user}}");
    String brokerPassword = camelContext.resolvePropertyPlaceholders("{{broker.password}}");

    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
    connectionFactory.setUser(brokerUser);
    connectionFactory.setPassword(brokerPassword);

    JmsComponent jms = JmsComponent.jmsComponentAutoAcknowledge(connectionFactory);
    jms.setPreserveMessageQos(true);
    camelContext.addComponent("jms", jms);

    // Load route from YAML
    main.configure().withRoutesIncludePattern("routes/ftp-to-jms-route.yaml");

    main.run();
  }
}
