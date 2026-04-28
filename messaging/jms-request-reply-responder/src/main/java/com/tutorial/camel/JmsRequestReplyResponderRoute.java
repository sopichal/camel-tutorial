package com.tutorial.camel;

import com.tibco.tibjms.TibjmsConnectionFactory;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.builder.RouteBuilder;

public class JmsRequestReplyResponderRoute extends RouteBuilder {

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

    // Responder: receive request, process it, and send reply with correlation ID
    // exchangePattern=InOnly: prevent Camel from treating this as InOut just because JMSReplyTo is set
    from("jms:queue:{{jms.request.queue.name}}?exchangePattern=InOnly")
        .log("Responder received request with CorrelationId: ${header.JMSCorrelationID} | Body: ${body}")
        .process(exchange -> {
          String correlationId = exchange.getIn().getHeader("JMSCorrelationID", String.class);
          // Read the reply queue from the custom header (not JMSReplyTo, to avoid TIBCO EMS receipt behaviour)
          String replyQueueName = exchange.getIn().getHeader("X-Reply-Queue", String.class);

          String request = exchange.getIn().getBody(String.class);

          String orderNumber = "UNKNOWN";
          java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<orderNumber>([^<]+)</orderNumber>");
          java.util.regex.Matcher matcher = pattern.matcher(request);
          if (matcher.find()) {
            orderNumber = matcher.group(1);
          }

          String response = "<response><orderNumber>" + orderNumber + "</orderNumber><status>APPROVED</status>"
              + "<processedAt>" + System.currentTimeMillis() + "</processedAt>"
              + "<correlationId>" + correlationId + "</correlationId></response>";

          exchange.getIn().setBody(response);
          exchange.getIn().setHeader("JMSCorrelationID", correlationId);
          exchange.setProperty("replyQueueName", replyQueueName);
        })
        .log("Responder sending reply to ${exchangeProperty.replyQueueName} with CorrelationId: ${header.JMSCorrelationID} | Response: ${body}")
        .toD("jms:queue:${exchangeProperty.replyQueueName}?exchangePattern=InOnly");
  }
}
