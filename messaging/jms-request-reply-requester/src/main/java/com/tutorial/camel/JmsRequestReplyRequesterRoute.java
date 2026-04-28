package com.tutorial.camel;

import com.tibco.tibjms.TibjmsConnectionFactory;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.builder.RouteBuilder;

public class JmsRequestReplyRequesterRoute extends RouteBuilder {
  // Unique instance ID for this requester
  private final String instanceId = UUID.randomUUID().toString().substring(0, 8);

  // Instance-specific reply queue name
  private final String replyQueueName = "CAMEL.REPLY.QUEUE." + instanceId;

  // Instance-specific pending requests (not shared with other requesters)
  private final Map<String, Long> pendingRequests = Collections.synchronizedMap(new ConcurrentHashMap<>());

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

    // Requester: sends request with correlation ID and instance-specific reply queue
    from("timer:requester?period=8000")
        .process(exchange -> {
          // Generate unique correlation ID for this request
          String correlationId = UUID.randomUUID().toString();
          exchange.getIn().setHeader("JMSCorrelationID", correlationId);

          // Create request body
          String request = "<request><orderNumber>ORD-" + System.currentTimeMillis() % 1000
              + "</orderNumber><status>PENDING</status></request>";
          exchange.getIn().setBody(request);

          exchange.getIn().setHeader("CorrelationId", correlationId);

          // Use a custom header (not JMSReplyTo) to avoid TIBCO EMS delivery receipt behaviour:
          // TIBCO EMS sends an automatic receipt to JMSReplyTo when it accepts the message,
          // which would create a duplicate in the reply queue.
          exchange.getIn().setHeader("X-Reply-Queue", replyQueueName);

          // TRACK this correlation ID as a pending request (instance-specific)
          pendingRequests.put(correlationId, System.currentTimeMillis());
        })
        .log("Requester [" + replyQueueName + "] sending request with CorrelationId: ${header.CorrelationId} | Body: ${body}")
        .to("jms:queue:{{jms.request.queue.name}}?exchangePattern=InOnly");

    // Requester: listen for replies on instance-specific queue with correlation validation
    // exchangePattern=InOnly: prevent JMS consumer from bouncing the reply back into the same queue
    from("jms:queue:" + replyQueueName + "?exchangePattern=InOnly")
        .process(exchange -> {
          String correlationId = exchange.getIn().getHeader("JMSCorrelationID", String.class);

          // THREAD-SAFE VALIDATION: Atomically remove and check correlation ID
          // This prevents race conditions where multiple threads consume from reply queue
          Long timestamp = pendingRequests.remove(correlationId);

          if (timestamp != null) {
            // This is a valid reply to one of our requests
            exchange.setProperty("validatedCorrelation", true);
            exchange.setProperty("requestSentAt", timestamp);
          } else {
            // This should never happen with per-requester queue!
            exchange.setProperty("validatedCorrelation", false);
            exchange.setProperty("rejectionReason", "Correlation ID " + correlationId + " not found in pending requests");
          }
        })
        .choice()
          .when(exchange -> exchange.getProperty("validatedCorrelation", Boolean.class))
            .log("Requester [" + replyQueueName + "] received VALID reply for CorrelationId: ${header.JMSCorrelationID} | Response: ${body}")
          .otherwise()
            .log("Requester [" + replyQueueName + "] REJECTED reply for CorrelationId: ${header.JMSCorrelationID} | Reason: ${exchangeProperty.rejectionReason}")
        .end();
  }
}
