package com.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ApiHandler implements HttpHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiUrl = "https://api.naurt.net/final-destination/v1";
    private final String apiKey;

    public ApiHandler() {
        // Read the API key from the file
        try {
            apiKey = new String(Files.readAllBytes(Paths.get("api.key"))).trim();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read API key from file", e);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            // Extract the address_string parameter from the URL
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> queryParams = parseQuery(query);
            String addressString = queryParams.get("address_string");

            if (addressString == null) {
                sendResponse(exchange, 400, "{\"error\":\"address_string is required\"}");
                return;
            }

            Map<String, String> requestBody = Map.of("address_string", addressString);
            String jsonInputString = objectMapper.writeValueAsString(requestBody);

            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .header("Content-Type", "application/json")
                        .header("Authorization", apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(jsonInputString))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                // Extract GeoJSON and address from the response
                String geoJsonResponse = extractGeoJson(response.body());
                String address = extractAddress(response.body());
                String htmlResponse = generateHtmlWithLeafletMap(geoJsonResponse, address);

                sendResponse(exchange, 200, htmlResponse);
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\":\"An error occurred\"}");
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
        }
    }

    private Map<String, String> parseQuery(String query) {
        if (query == null || query.isEmpty()) {
            return new HashMap<>();
        }
        return Stream.of(query.split("&"))
                .map(param -> param.split("="))
                .collect(Collectors.toMap(
                    param -> param[0],
                    param -> param.length > 1 ? param[1] : ""
                ));
    }

    private String extractGeoJson(String response) throws IOException {
        JsonNode root = objectMapper.readTree(response);
        return objectMapper.writeValueAsString(root.path("best_match").path("geojson"));
    }

    private String extractAddress(String response) throws IOException {
        JsonNode root = objectMapper.readTree(response);
        return root.path("best_match").path("address").asText();
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private String generateHtmlWithLeafletMap(String geoJson, String address) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<title>Naurt Geocoder</title>"
                + "<meta charset=\"utf-8\" />"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "<link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.css\" />"
                + "<style>"
                + "#map { height: 100vh; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div id=\"map\"></div>"
                + "<script src=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.js\"></script>"
                + "<script>"
                + "var map = L.map('map').setView([0, 0], 2);"
                + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {"
                + "maxZoom: 19,"
                + "attribution: '&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors'"
                + "}).addTo(map);"
                + "var geojson = " + geoJson + ";"
                + "var address = \"" + address + "\";"
                + "function getColor(type) {"
                + "  switch (type) {"
                + "    case 'naurt_door': return '#FF0000';"  // Red
                + "    case 'naurt_building': return '#9d79f2';"  // Lavender
                + "    case 'naurt_parking': return '#00FF00';"  // Green
                + "    default: return '#000000';"  // Black
                + "  }"
                + "}"
                + "function style(feature) {"
                + "  return {"
                + "    fillColor: getColor(feature.properties.naurt_type),"
                + "    weight: 2,"
                + "    opacity: 1,"
                + "    color: 'white',"
                + "    dashArray: '5',"
                + "    fillOpacity: 0.4"
                + "  };"
                + "}"
                + "function onEachFeature(feature, layer) {"
                + "  if (feature.properties) {"
                + "    var popupContent = 'Type: ' + feature.properties.naurt_type;"
                + "    if (feature.properties.naurt_type == 'naurt_building') {"
                + "      popupContent += '<br>Address: ' + address;"
                + "    }"
                + "    layer.bindPopup(popupContent).openPopup();"
                + "  }"
                + "}"
                + "L.geoJSON(geojson, {"
                + "  style: style,"
                + "  onEachFeature: onEachFeature"
                + "}).addTo(map);"
                + "var bounds = L.geoJSON(geojson).getBounds();"
                + "map.fitBounds(bounds);"
                + "</script>"
                + "</body>"
                + "</html>";
    }
}
