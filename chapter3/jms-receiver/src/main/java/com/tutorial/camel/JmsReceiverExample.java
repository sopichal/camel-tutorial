package com.tutorial.camel;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.main.Main;
import org.apache.camel.component.properties.PropertiesComponent;

public class JmsReceiverExample {
    public static void main(String args[]) throws Exception {
        System.out.println("Starting JMS Receiver Example");
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
        
        Main main = new Main();
        
        // Configure where to find the YAML routes
        main.setPropertyPlaceholderLocations("classpath:config.properties");
        main.configure().withRoutesIncludePattern("classpath:routes/jms-receiver-route.yaml");
        
        // Alternatively, configure routes in Java
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
                
                // NOTE: The actual routes are defined in the YAML file,
                // but we could define them here as well if needed
                /*
                from("jms:queue:" + queueName)
                    .log("Received message from queue: ${body}");
                
                from("jms:topic:" + topicName)
                    .log("Received message from topic: ${body}");
                */
            }
        });
        
        // Start the Camel application
        main.run();
    }
}