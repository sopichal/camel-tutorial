package com.tutorial.camel;

import org.apache.camel.main.Main;

public class App {

    public static void main(String[] args) throws Exception {
        // Create a Main instance
        Main main = new Main();
        // Add route to Main
        main.configure().addRoutesBuilder(new hello());
        // Run the application
        main.run(args);
    }
}