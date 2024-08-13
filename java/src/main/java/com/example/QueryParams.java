package com.example;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class QueryParams {
    private String addressString;
    private Boolean additionalMatches;
    private Float latitude;
    private Float longtiude;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public QueryParams(String query) {
        if (query == null || query.isEmpty()) {
            return;
        }

        Map<String, String> queryParams = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length > 1) {
                queryParams.put(keyValue[0], keyValue[1]);
            } else {
                queryParams.put(keyValue[0], null);
            }
        }

        this.addressString = queryParams.get("address_string");
        this.additionalMatches = this.parseBoolean(queryParams.get("additional_matches"));
        this.latitude = this.parseFloat(queryParams.get("latitude"));
        this.longtiude = this.parseFloat(queryParams.get("longitude"));
    }

    public String convertToNaurtRequestJson() throws JsonProcessingException {
        Map<String, Object> naurtRequest = new HashMap<>();

        if (this.addressString != null) {
            naurtRequest.put("address_string", this.addressString);
        }

        if (this.additionalMatches != null) {
            naurtRequest.put("additional_matches", this.additionalMatches);
        }

        if (this.latitude != null) {
            naurtRequest.put("latitude", this.latitude);
        }

        if (this.longtiude != null) {
            naurtRequest.put("longitude", this.longtiude);
        }

        return objectMapper.writeValueAsString(naurtRequest);
    }

    private Boolean parseBoolean(String value) {
        try {
            return value != null ? Boolean.parseBoolean(value) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Float parseFloat(String value) {
        try {
            return value != null ? Float.parseFloat(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
