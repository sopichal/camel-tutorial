package com.tutorial.camel;

import com.tibco.tibjms.TibjmsConnectionFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.builder.RouteBuilder;

public class JmsTopicPublisherRoute extends RouteBuilder {

  private final AtomicInteger messageCounter = new AtomicInteger(0);
  private final String[] events = {
    "Event: OrderCreated - Customer placed new order",
    "Event: PaymentProcessed - Payment approved",
    "Event: OrderShipped - Order dispatched to warehouse"
  };

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

    // Publisher: publishes events to topic (every 6 seconds)
    from("timer:eventPublisher?period=6000")
        .process(exchange -> {
          int counter = messageCounter.getAndIncrement();
          int index = counter % events.length;
          String event = events[index] + " [" + System.currentTimeMillis() + "]";
          exchange.getIn().setBody(event);
        })
        .log("Publishing to topic: ${body}")
        .to("jms:topic:{{jms.topic.name}}");
  }
}
