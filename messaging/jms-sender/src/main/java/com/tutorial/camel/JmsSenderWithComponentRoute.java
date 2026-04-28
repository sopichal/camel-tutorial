package com.tutorial.camel;

import com.tibco.tibjms.TibjmsConnectionFactory;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.builder.RouteBuilder;

public class JmsSenderWithComponentRoute extends RouteBuilder {

  private final AtomicInteger messageCounter = new AtomicInteger(0);
  private final String[] orderFiles = {"order-1.xml", "order-2.xml", "order-3.xml"};
  private final String[] orderXmls = new String[3];

  @Override
  public void configure() throws Exception {
    // Configure properties component
    PropertiesComponent propertiesComponent = new PropertiesComponent();
    propertiesComponent.setLocation("classpath:config.properties");
    getContext().setPropertiesComponent(propertiesComponent);

    // Configure JMS component with TIBCO EMS
    String brokerUrl = getContext().resolvePropertyPlaceholders("{{broker.url}}");
    String brokerUser = getContext().resolvePropertyPlaceholders("{{broker.user}}");
    String brokerPassword = getContext().resolvePropertyPlaceholders("{{broker.password}}");

    TibjmsConnectionFactory connectionFactory = new TibjmsConnectionFactory(brokerUrl);
    connectionFactory.setUserName(brokerUser);
    connectionFactory.setUserPassword(brokerPassword);

    JmsComponent jmsComponent = new JmsComponent();
    jmsComponent.setConnectionFactory(connectionFactory);
    jmsComponent.setPreserveMessageQos(true);
    getContext().addComponent("jms", jmsComponent);

    // Load order XML files from classpath at startup
    for (int i = 0; i < orderFiles.length; i++) {
      orderXmls[i] = loadXmlFile("data/" + orderFiles[i]);
    }

    // Define the sender route
    from("timer:orderSender?period=5000")
        .process(exchange -> {
          int counter = messageCounter.getAndIncrement();
          int index = counter % orderXmls.length;
          exchange.getIn().setBody(orderXmls[index]);
        })
        .log("Sending order from ${exchangeProperty.counter}: ${body}")
        .to("jms:queue:{{jms.queue.name}}");
  }

  private String loadXmlFile(String resourcePath) throws Exception {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        Scanner scanner = new Scanner(is).useDelimiter("\\A")) {
      return scanner.hasNext() ? scanner.next() : "";
    }
  }
}
