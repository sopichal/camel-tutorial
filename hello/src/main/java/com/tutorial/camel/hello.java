package com.tutorial.camel;

import org.apache.camel.builder.RouteBuilder;

public class hello extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // Write your routes here, for example:
        from("timer:java?period={{time:1000}}")
            .setBody()
                .simple("Hello Camel from ${routeId}")
            .log("${body}");
    }
}
