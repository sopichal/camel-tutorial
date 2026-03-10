package com.tutorial.camel;

import org.apache.camel.main.Main;

public class FileCopierWithYamlDSL {
    public static void main(String[] args) throws Exception {
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
        Main main = new Main();
        main.configure().withRoutesIncludePattern("classpath:routes/file-copy-route.yaml");
        main.run();
    }
}
