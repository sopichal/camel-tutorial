package com.tutorial.camel;

import com.tibco.tibjms.TibjmsConnectionFactory;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.builder.RouteBuilder;

public class JmsTopicSubscriberRoute extends RouteBuilder {

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

    // Subscriber: subscribes to topic and processes events
    from("jms:topic:{{jms.topic.name}}")
        .log("[SUBSCRIBER] Received event at ${date:now:yyyy-MM-dd HH:mm:ss}: ${body}");
  }
}
