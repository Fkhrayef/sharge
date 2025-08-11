package com.fkhrayef.sharge.Service;

import com.fkhrayef.sharge.Api.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HereService {

    @Value("${here.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int DEFAULT_REGION_RADIUS = 10000; // 10km radius

    // Get detailed location data by nationalAddress
    public Map<String, Object> geocode(String nationalAddress) {
        try {
            String url = "https://geocode.search.hereapi.com/v1/geocode?q=" +
                    nationalAddress + "&apiKey=" + apiKey;

            // REST call
            String response = restTemplate.getForObject(url, String.class);

            // Parse JSON
            JsonNode root = objectMapper.readTree(response);
            JsonNode firstItem = root.get("items").get(0);

            // Extract data
            Map<String, Object> result = new HashMap<>();
            result.put("address", firstItem.get("address").get("label").asText());
            result.put("latitude", firstItem.get("position").get("lat").asDouble());
            result.put("longitude", firstItem.get("position").get("lng").asDouble());
            result.put("city", firstItem.get("address").get("city").asText());
            result.put("district", firstItem.get("address").get("district").asText());

            return result;

        } catch (Exception e) {
            throw new ApiException("Failed to geocode address: " + nationalAddress);
        }
    }


    public List<Map<String, Object>> calculateTravelTimes(double userLat, double userLng,
                                                          List<Map<String, Object>> destinations) {
        try {
            // Step 1: Build the request body as a nested Map structure
            // This mirrors the JSON structure you showed in your example
            Map<String, Object> requestBody = new HashMap<>();

            // Set origin (user location)
            List<Map<String, Object>> origins = Arrays.asList(
                    Map.of("lat", userLat, "lng", userLng)
            );
            requestBody.put("origins", origins);

            // Clean destinations - only lat/lng for HERE API (no extra fields like "name")
            List<Map<String, Object>> cleanDestinations = destinations.stream()
                    .map(dest -> Map.of(
                            "lat", dest.get("lat"),
                            "lng", dest.get("lng")
                    ))
                    .collect(Collectors.toList());
            requestBody.put("destinations", cleanDestinations);

            // Use current time for real-time traffic data
            requestBody.put("departureTime", Instant.now().toString());

            // Use user location as region center with 10km radius
            // This ensures we only calculate routes within user's vicinity
            Map<String, Object> regionDef = Map.of(
                    "type", "circle",
                    "center", Map.of("lat", userLat, "lng", userLng),
                    "radius", DEFAULT_REGION_RADIUS
            );
            requestBody.put("regionDefinition", regionDef);
            System.out.println("Calculating travel times from user location (" + userLat + ", " + userLng + ") within " + DEFAULT_REGION_RADIUS + "m radius");

            // Step 2: Prepare HTTP request
            String url = "https://matrix.router.hereapi.com/v8/matrix?async=false&apiKey=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Step 3: Make API call
            System.out.println("Calling HERE Matrix API for " + destinations.size() + " destinations from user location (" + userLat + ", " + userLng + ")");

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            // Step 4: Process response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return processMatrixResponse(response.getBody(), destinations);
            } else {
                System.err.println("HERE Matrix API call failed with status: " + response.getStatusCode());
                throw new ApiException("HERE Matrix API call failed");
            }

        } catch (Exception e) {
            System.err.println("Error calling HERE Matrix API: " + e.getMessage());
            throw new ApiException("Failed to calculate travel times: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked") // Suppress warnings for Map casting
    private List<Map<String, Object>> processMatrixResponse(Map<String, Object> response,
                                                            List<Map<String, Object>> destinations) {
        List<Map<String, Object>> results = new ArrayList<>();

        // Extract the matrix data from response
        Map<String, Object> matrix = (Map<String, Object>) response.get("matrix");
        if (matrix == null) {
            System.err.println("No matrix data in HERE API response");
            return results;
        }

        // Get travel times and distances arrays
        // These can be either 2D arrays: [[299, 450]] or flat arrays: [299, 450] depending on matrix size
        Object travelTimeData = matrix.get("travelTimes");
        Object distanceData = matrix.get("distances");

        List<Integer> originTravelTimes = null;
        List<Integer> originDistances = null;

        // Handle both flat array and 2D array formats
        if (travelTimeData instanceof List) {
            List<?> travelTimeMatrix = (List<?>) travelTimeData;
            if (!travelTimeMatrix.isEmpty()) {
                Object firstElement = travelTimeMatrix.get(0);
                if (firstElement instanceof Integer) {
                    // Flat array format: [299, 450]
                    originTravelTimes = (List<Integer>) travelTimeMatrix;
                } else if (firstElement instanceof List) {
                    // 2D array format: [[299, 450]]
                    originTravelTimes = (List<Integer>) travelTimeMatrix.get(0);
                }
            }
        }

        if (distanceData instanceof List) {
            List<?> distanceMatrix = (List<?>) distanceData;
            if (!distanceMatrix.isEmpty()) {
                Object firstElement = distanceMatrix.get(0);
                if (firstElement instanceof Integer) {
                    // Flat array format: [850, 1200]
                    originDistances = (List<Integer>) distanceMatrix;
                } else if (firstElement instanceof List) {
                    // 2D array format: [[850, 1200]]
                    originDistances = (List<Integer>) distanceMatrix.get(0);
                }
            }
        }


        if (originTravelTimes != null && !originTravelTimes.isEmpty()) {
            // Process each destination
            for (int i = 0; i < destinations.size(); i++) {
                Map<String, Object> dest = destinations.get(i);
                int travelTimeSeconds = originTravelTimes.get(i);
                int distanceMeters = originDistances != null && i < originDistances.size() ?
                        originDistances.get(i) : 0;

                // Build result map for this destination
                Map<String, Object> result = new HashMap<>();
                result.put("destination", dest);
                result.put("destinationName", getDestinationName(dest));
                result.put("travelTimeSeconds", travelTimeSeconds);
                result.put("distanceMeters", distanceMeters);
                result.put("formattedDuration", formatDuration(travelTimeSeconds));
                result.put("formattedDistance", formatDistance(distanceMeters));

                results.add(result);
            }
        }

        return results;
    }

    // Extract destination name from Map - uses "name" field or falls back to coordinates
    private String getDestinationName(Map<String, Object> dest) {
        Object name = dest.get("name");
        if (name != null && !name.toString().trim().isEmpty()) {
            return name.toString();
        }
        // Fallback to coordinates if no name provided
        return String.format("%.5f,%.5f", dest.get("lat"), dest.get("lng"));
    }


    // Convert seconds to human-readable duration format
    // Examples: 420 seconds -> "7m", 3900 seconds -> "1h 5m"
    private String formatDuration(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        }
        return String.format("%dm", minutes);
    }

    // Convert meters to human-readable distance format
    // Examples: 850 -> "850 m", 12500 -> "12.5 km"
    private String formatDistance(int meters) {
        if (meters >= 1000) {
            return String.format("%.1f km", meters / 1000.0);
        }
        return String.format("%d m", meters);
    }


    // Fetch EV Chargers
    public List<Map<String, Object>> getExternalChargers(Double userLat, Double userLng, int radiusKm) {
        try {
            String url = String.format(
                    "https://browse.search.hereapi.com/v1/browse?at=%f,%f&categories=700-7600-0322&apiKey=%s",
                    userLat, userLng, apiKey
            );

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return processExternalChargersResponse(response.getBody(), radiusKm);
            } else {
                return new ArrayList<>();
            }

        } catch (Exception e) {
            System.err.println("Error calling HERE Browse API: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> processExternalChargersResponse(Map<String, Object> response, int radiusKm) {
        List<Map<String, Object>> results = new ArrayList<>();

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
        if (items == null) {
            return results;
        }

        for (Map<String, Object> item : items) {
            try {
                Map<String, Object> position = (Map<String, Object>) item.get("position");
                Double lat = ((Number) position.get("lat")).doubleValue();
                Double lng = ((Number) position.get("lng")).doubleValue();

                int distance = ((Number) item.get("distance")).intValue();
                if (distance > radiusKm * 1000) {
                    continue;
                }

                Map<String, Object> address = (Map<String, Object>) item.get("address");
                String addressLabel = address != null ? (String) address.get("label") : "Address not available";

                Map<String, Object> externalCharger = new HashMap<>();
                externalCharger.put("lat", lat);
                externalCharger.put("lng", lng);
                externalCharger.put("name", item.get("title"));
                externalCharger.put("address", addressLabel);
                externalCharger.put("isExternal", true);
                externalCharger.put("type", "commercial");

                results.add(externalCharger);

            } catch (Exception e) {
                System.err.println("Error processing external charger: " + e.getMessage());
            }
        }

        return results;
    }
}