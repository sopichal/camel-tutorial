package com.tutorial.camel;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.component.sql.SqlComponent;
import org.apache.camel.main.Main;
import org.apache.commons.dbcp2.BasicDataSource;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public class JmsToPostgresqlExample {

    public static void main(String[] args) throws Exception {
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
        
        Main main = new Main();
        
        main.configure().addRoutesBuilder(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // Get the Camel context
                CamelContext context = this.getCamelContext();
                
                // Setup properties component
                PropertiesComponent propertiesComponent = new PropertiesComponent();
                propertiesComponent.setLocation("classpath:config.properties");
                context.setPropertiesComponent(propertiesComponent);
                
                // Resolve the properties manually
                String brokerUrl = context.resolvePropertyPlaceholders("{{broker.url}}");
                String brokerUser = context.resolvePropertyPlaceholders("{{broker.user}}");
                String brokerPassword = context.resolvePropertyPlaceholders("{{broker.password}}");
                
                String dbUrl = context.resolvePropertyPlaceholders("{{db.url}}");
                String dbUser = context.resolvePropertyPlaceholders("{{db.user}}");
                String dbPassword = context.resolvePropertyPlaceholders("{{db.password}}");
                
                // Configure JMS component
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
                connectionFactory.setUser(brokerUser);
                connectionFactory.setPassword(brokerPassword);
                
                JmsComponent jmsComponent = new JmsComponent();
                jmsComponent.setConnectionFactory(connectionFactory);
                jmsComponent.setPreserveMessageQos(true);
                
                context.addComponent("jms", jmsComponent);
                
                // Configure SQL component with PostgreSQL datasource
                BasicDataSource dataSource = new BasicDataSource();
                dataSource.setDriverClassName("org.postgresql.Driver");
                dataSource.setUrl(dbUrl);
                dataSource.setUsername(dbUser);
                dataSource.setPassword(dbPassword);
                dataSource.setInitialSize(5);
                dataSource.setMaxTotal(10);
                
                SqlComponent sqlComponent = new SqlComponent();
                sqlComponent.setDataSource(dataSource);
                
                context.addComponent("sql", sqlComponent);
                
                // Define the route
                from("jms:xmlOrders")
                    .routeId("jmsToPostgresRoute")
                    .log("Received XML order: ${body}")
                    .process(exchange -> {
                        // Parse XML message and extract order details
                        String xml = exchange.getIn().getBody(String.class);
                        
                        // Simple XML parsing (in production, use proper XML parsing)
                        String name = extractXmlAttribute(xml, "name");
                        String amount = extractXmlAttribute(xml, "amount");
                        String customer = extractXmlAttribute(xml, "customer");
                        
                        // Create parameters map for SQL insert
                        Map<String, Object> parameters = new HashMap<>();
                        parameters.put("order_name", name);
                        parameters.put("amount", Integer.parseInt(amount));
                        parameters.put("customer", customer);
                        
                        // Set the parameters as body
                        exchange.getIn().setBody(parameters);
                    })
                    .to("sql:INSERT INTO purchase_orders(order_name, amount, customer) VALUES (:#order_name, :#amount, :#customer)")
                    .log("Order inserted into database: ${body}");
            }
            
            // Simple method to extract XML attribute
            private String extractXmlAttribute(String xml, String attributeName) {
                String search = attributeName + "=\"";
                int start = xml.indexOf(search) + search.length();
                int end = xml.indexOf("\"", start);
                if (start >= search.length() && end > start) {
                    return xml.substring(start, end);
                }
                return "";
            }
        });
        
        main.run();
    }
}