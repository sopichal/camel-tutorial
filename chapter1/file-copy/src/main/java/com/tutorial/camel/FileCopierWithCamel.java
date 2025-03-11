package com.tutorial.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class FileCopierWithCamel {
    
    public static void main(String args[]) throws Exception {
        // Print the current working directory
        System.out.println("Current working directory: " + System.getProperty("user.dir"));

        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("file:data/inbox?noop=true&delay=1000")
                    .to("file:data/outbox");
            }
        });
        context.start();
        Thread.sleep(100000);
        context.stop();
    }
}
