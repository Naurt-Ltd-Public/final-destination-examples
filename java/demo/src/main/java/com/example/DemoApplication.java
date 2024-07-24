package com.example;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class DemoApplication {

    public static void main(String[] args) throws IOException {
        // Create an HTTP server that listens on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Create an instance of ApiHandler
        ApiHandler apiHandler = new ApiHandler();

        // Create a context for the root path
        server.createContext("/", apiHandler);

        // Set a default executor
        server.setExecutor(null);

        // Start the server
        server.start();
        System.out.println("Server is listening on port 8080");
    }
}
