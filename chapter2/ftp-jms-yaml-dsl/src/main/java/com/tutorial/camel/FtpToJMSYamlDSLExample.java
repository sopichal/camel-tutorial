package com.tutorial.camel;

import org.apache.camel.main.Main;

public class FtpToJMSYamlDSLExample {
    public static void main(String[] args) throws Exception {
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
        System.out.println("Classpath: " + System.getProperty("java.class.path"));

        // System.setProperty(
        //     "camel.main.property-placeholder-locations",
        //     "./config.properties,classpath:config.properties"
        // );
        
        // Create a Main instance
        Main main = new Main();
        
        // Configure where to find the YAML routes
        main.setPropertyPlaceholderLocations("classpath:config.properties");
        main.configure().withRoutesIncludePattern("classpath:routes/ftp-to-jms-route.yaml");

        
        // Run the Camel application
        main.run();
    }
}