package com.behaimits.cameltutorial;


import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;

public class FtpToJMSExample {
  public static void main(String args[]) throws Exception {
    CamelContext context = new DefaultCamelContext(); 

  org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory amqcf = new  org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory("vm://localhost");
  jakarta.jms.ConnectionFactory cf = amqcf;

  // Create the Camel JMS component and wire it to our broker connection factory
  JmsComponent jms = new JmsComponent();
  jms.jmsComponentAutoAcknowledge(cf);

    context.addComponent("jms", jms);
    context.addRoutes(new RouteBuilder() { 
      public void configure() {
        from("ftp://rider.com/orders?username=rider&password=secret").to("jms:incomingOrders");
        //from("ftp://localhost:2121/orders?username=rider&password=secret")
        //  .to("file:data/outbox");
      } 
    });
    context.start();
    Thread.sleep(10000);
    context.stop();
  }

}
