package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.StringWriter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

public class ApiHandler implements HttpHandler {

    private final String apiUrl = "https://api.naurt.net/final-destination/v1";
    private final String apiKey;

    private final MustacheFactory mustacheFactory;

    public ApiHandler() {
        try {
            this.apiKey = new String(Files.readAllBytes(Paths.get("api.key"))).trim();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read API key from file", e);
        }

        this.mustacheFactory = new DefaultMustacheFactory();

    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            this.handleGet(exchange);
        } else {
            this.handleBadRequest(exchange, "You must use a GET request");
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {

        try {
            String query = exchange.getRequestURI().getQuery();

            QueryParams queryParams = new QueryParams(query);
            String jsonInputString = queryParams.convertToNaurtRequestJson();
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonInputString))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Model
            Map<String, Object> model = new HashMap<>();
            model.put("response", response.body());

            // Get the template
            Mustache mustache = mustacheFactory.compile("index.mustache");

            // Render the template with the model
            StringWriter writer = new StringWriter();
            mustache.execute(writer, model).flush();
            String reply = writer.toString();

            this.handleAcceptedResponse(exchange, reply);
        } catch (Exception e) {
            e.printStackTrace();
            this.handleError(exchange);
        }
    }

    private void handleAcceptedResponse(HttpExchange exchange, String response) throws IOException {
        this.handleResponse(exchange, response, 200);
    }

    private void handleBadRequest(HttpExchange exchange, String response) throws IOException {
        this.handleResponse(exchange, response, 400);
    }

    private void handleError(HttpExchange exchange) throws IOException {
        this.handleResponse(exchange, "Internal Server Error", 500);
    }

    private void handleResponse(HttpExchange exchange, String response, int code) throws IOException {
        exchange.sendResponseHeaders(code, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        try {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
        }
        os.close();
    }
}