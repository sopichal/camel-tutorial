package com.tutorial.camel;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.main.Main;
import org.apache.camel.component.properties.PropertiesComponent;
import java.util.concurrent.atomic.AtomicInteger;

public class JmsSenderExample {
    public static void main(String args[]) throws Exception {
        System.out.println("Starting JMS Sender Example");
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
        
        // Parse command line arguments
        boolean isQueueOnly = false;
        boolean isTopicOnly = false;
        
        for (String arg : args) {
            if (arg.equals("--queue-only")) {
                isQueueOnly = true;
            } else if (arg.equals("--topic-only")) {
                isTopicOnly = true;
            } else if (arg.equals("--help")) {
                System.out.println("Usage: java -cp ... com.tutorial.camel.JmsSenderExample [OPTIONS]");
                System.out.println("Options:");
                System.out.println("  --queue-only    Run only the queue producer");
                System.out.println("  --topic-only    Run only the topic producer");
                System.out.println("  --help          Display this help message");
                return;
            }
        }
        
        // Make variables effectively final for use in lambdas
        final boolean queueOnly = isQueueOnly;
        final boolean topicOnly = isTopicOnly;
        
        if (queueOnly) {
            System.out.println("Running in queue-only mode");
        } else if (topicOnly) {
            System.out.println("Running in topic-only mode");
        }
        
        Main main = new Main();
        
        // Configure properties
        main.setPropertyPlaceholderLocations("classpath:config.properties");
        
        // Register counter beans
        main.bind("queueCounter", new AtomicInteger(0));
        main.bind("topicCounter", new AtomicInteger(0));
        
        // Define routes using Java DSL instead of YAML to avoid compatibility issues
        main.configure().addRoutesBuilder(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // Get the Camel context
                CamelContext context = this.getCamelContext();
                
                // Configure properties component
                PropertiesComponent propertiesComponent = new PropertiesComponent();
                propertiesComponent.setLocation("classpath:config.properties");
                context.setPropertiesComponent(propertiesComponent);
                
                // Resolve the properties manually before using them
                String brokerUrl = context.resolvePropertyPlaceholders("{{broker.url}}");
                String brokerUser = context.resolvePropertyPlaceholders("{{broker.user}}");
                String brokerPassword = context.resolvePropertyPlaceholders("{{broker.password}}");
                String queueName = context.resolvePropertyPlaceholders("{{jms.queue.name}}");
                String topicName = context.resolvePropertyPlaceholders("{{jms.topic.name}}");
                
                // Create ActiveMQConnectionFactory with credentials
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
                connectionFactory.setUser(brokerUser);
                connectionFactory.setPassword(brokerPassword);
                
                // Create and configure JMS component
                JmsComponent jmsComponent = new JmsComponent();
                jmsComponent.setConnectionFactory(connectionFactory);
                jmsComponent.setPreserveMessageQos(true);
                
                // Add JMS component to the context
                context.addComponent("jms", jmsComponent);
                
                // Define routes based on command-line arguments
                if (!topicOnly) {
                    // Queue Producer route
                    from("timer:queueSender?period=5000")
                        .process(exchange -> {
                            int count = exchange.getContext().getRegistry().lookupByNameAndType("queueCounter", AtomicInteger.class).incrementAndGet();
                            exchange.setProperty("counter", count);
                        })
                        .setBody(simple("[Queue] Message #${exchangeProperty.counter}: Hello from JMS Queue Sender! Generated at ${date:now:yyyy-MM-dd HH:mm:ss}"))
                        .log("Sending to queue: ${body}")
                        .to("jms:queue:" + queueName);
                }
                
                if (!queueOnly) {
                    // Topic Publisher route
                    from("timer:topicPublisher?period=7000")
                        .process(exchange -> {
                            int count = exchange.getContext().getRegistry().lookupByNameAndType("topicCounter", AtomicInteger.class).incrementAndGet();
                            exchange.setProperty("counter", count);
                        })
                        .setBody(simple("[Topic] Message #${exchangeProperty.counter}: Hello from JMS Topic Publisher! Published at ${date:now:yyyy-MM-dd HH:mm:ss}"))
                        .log("Publishing to topic: ${body}")
                        .to("jms:topic:" + topicName);
                }
            }
        });
        
        // Start the Camel application
        main.run();
    }
}