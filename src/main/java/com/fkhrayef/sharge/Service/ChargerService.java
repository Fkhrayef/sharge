package com.fkhrayef.sharge.Service;

import com.fkhrayef.sharge.Api.ApiException;
import com.fkhrayef.sharge.Model.Car;
import com.fkhrayef.sharge.Model.Charger;
import com.fkhrayef.sharge.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChargerService {

    private final ChargerRepository chargerRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ChargerConnectorRepository chargerConnectorRepository;
    private final CarRepository carRepository;
    private final HereService hereService;
    private final BookingService bookingService; // just to take the isWithinAvailabilityWindow method
    private final InvoiceRepository invoiceRepository;

    public List<Charger> getAllChargers() {
        return chargerRepository.findAll();
    }

    public void addCharger(Charger charger) {
        if (userRepository.findUserById(charger.getOwnerId()) == null) {
            throw new ApiException("User not found");
        }

        Map<String, Object> geoData = hereService.geocode(charger.getNationalAddress());
        charger.setAddress((String) geoData.get("address"));
        charger.setLatitude((Double) geoData.get("latitude"));
        charger.setLongitude((Double) geoData.get("longitude"));

        charger.setIsActive(true);
        chargerRepository.save(charger);
    }

    public void updateCharger(Integer id, Charger charger) {
        Charger oldCharger = chargerRepository.findChargerById(id);
        if (oldCharger == null) {
            throw new ApiException("Charger not found");
        }
        if (userRepository.findUserById(charger.getOwnerId()) == null) {
            throw new ApiException("User not found");
        }
        if (charger.getIsActive() != oldCharger.getIsActive()) {
            throw new ApiException("You cannot change the activation status of a charger from this endpoint");
        }

        oldCharger.setOwnerId(charger.getOwnerId());
        oldCharger.setName(charger.getName());
        oldCharger.setDescription(charger.getDescription());

        // If nationalAddress changed, update related fields as well!
        if (!oldCharger.getNationalAddress().equals(charger.getNationalAddress())) {
            oldCharger.setNationalAddress(charger.getNationalAddress());
            Map<String, Object> geoData = hereService.geocode(charger.getNationalAddress());
            oldCharger.setAddress((String) geoData.get("address"));
            oldCharger.setLatitude((Double) geoData.get("latitude"));
            oldCharger.setLongitude((Double) geoData.get("longitude"));
        } else {
            oldCharger.setAddress(charger.getAddress());
            oldCharger.setLatitude(charger.getLatitude());
            oldCharger.setLongitude(charger.getLongitude());
        }

        oldCharger.setPricePerKwh(charger.getPricePerKwh());
        oldCharger.setMaxKw(charger.getMaxKw());

        chargerRepository.save(oldCharger);
    }

    public void deleteCharger(Integer id) {
        Charger oldCharger = chargerRepository.findChargerById(id);
        if (oldCharger == null) {
            throw new ApiException("Charger not found");
        }

        if (bookingRepository.existsByChargerIdAndStatusIn(id, List.of("RESERVED", "IN_PROGRESS"))) {
            throw new ApiException("You cannot delete charger while charger has an active booking");
        }

        chargerRepository.delete(oldCharger);
    }

    public void deactivateCharger(Integer id) {
        Charger oldCharger = chargerRepository.findChargerById(id);
        if (oldCharger == null) {
            throw new ApiException("Charger not found");
        }

        if (oldCharger.getIsActive()) {
            if (bookingRepository.existsByChargerIdAndStatusIn(id, List.of("RESERVED", "IN_PROGRESS"))) {
                throw new ApiException("You cannot deactivate charger while charger has an active booking");
            }
            oldCharger.setIsActive(false);
            chargerRepository.save(oldCharger);
        } else {
            throw new ApiException("Charger is already deactivated");
        }
    }

    public void activateCharger(Integer id) {
        Charger oldCharger = chargerRepository.findChargerById(id);
        if (oldCharger == null) {
            throw new ApiException("Charger not found");
        }

        if (oldCharger.getIsActive()) {
            throw new ApiException("Charger is already activated");
        } else {
            oldCharger.setIsActive(true);
            chargerRepository.save(oldCharger);
        }
    }

    public List<Map<String, Object>> findNearbyChargers(Integer carId, Double userLat, Double userLng, int limit, int radiusKm) {
        try {
            // 1. Get car details
            Car car = carRepository.findCarById(carId);
            if (car == null) {
                throw new ApiException("Car not found");
            }

            // 2. Get all active chargers and filter for compatibility & availability
            List<Charger> compatibleChargers = chargerRepository.findChargerByIsActiveTrue()
                    .stream()
                    .filter(charger -> chargerConnectorRepository.existsByChargerIdAndConnectorType(charger.getId(), car.getConnectorType()) && bookingService.isWithinAvailabilityWindow(charger.getId(), LocalDateTime.now()))
                    .toList();

            if (compatibleChargers.isEmpty()) {
                return new ArrayList<>();
            }

            // 3. Convert chargers to destinations
            List<Map<String, Object>> destinations = compatibleChargers.stream()
                    .map(charger -> Map.of(
                            "lat", charger.getLatitude(),
                            "lng", charger.getLongitude(),
                            "name", charger.getName(),
                            "charger", charger // Include full charger object
                    ))
                    .collect(Collectors.toList());

            // 4. Calculate travel times and process results
            return hereService.calculateTravelTimes(userLat, userLng, destinations)
                    .stream()
                    .filter(result -> (Integer) result.get("distanceMeters") / 1000 <= radiusKm)
                    .sorted((a, b) -> Integer.compare(
                            (Integer) a.get("travelTimeSeconds"),
                            (Integer) b.get("travelTimeSeconds")
                    ))
                    .limit(limit)
                    .map(result -> {
                        Map<String, Object> response = new HashMap<>(result);
                        Map<String, Object> destination = (Map<String, Object>) result.get("destination");
                        response.put("charger", destination.get("charger"));
                        response.put("travelTimeMinutes", (Integer) result.get("travelTimeSeconds") / 60);
                        response.put("distanceKm", (Integer) result.get("distanceMeters") / 1000);
                        return response;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new ApiException("Failed to find nearby chargers: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> findNearbyChargersWithExternal(Integer carId, Double userLat, Double userLng, int limit, int radiusKm) {
        try {
            // 1. Get car details
            Car car = carRepository.findCarById(carId);
            if (car == null) {
                throw new ApiException("Car not found");
            }

            // 2. Get our internal bookable chargers
            List<Charger> compatibleChargers = chargerRepository.findChargerByIsActiveTrue()
                    .stream()
                    .filter(charger -> chargerConnectorRepository.existsByChargerIdAndConnectorType(charger.getId(), car.getConnectorType())
                            && bookingService.isWithinAvailabilityWindow(charger.getId(), LocalDateTime.now()))
                    .toList();

            // 3. Get external commercial chargers
            List<Map<String, Object>> externalChargers = hereService.getExternalChargers(userLat, userLng, radiusKm);

            // 4. Convert internal chargers to destination format
            List<Map<String, Object>> internalDestinations = compatibleChargers.stream()
                    .map(charger -> Map.of(
                            "lat", charger.getLatitude(),
                            "lng", charger.getLongitude(),
                            "name", charger.getName(),
                            "charger", charger,
                            "isExternal", false,
                            "type", "bookable"
                    ))
                    .toList();

            // 5. Combine all destinations
            List<Map<String, Object>> allDestinations = new ArrayList<>(internalDestinations);
            allDestinations.addAll(externalChargers);

            if (allDestinations.isEmpty()) {
                return new ArrayList<>();
            }

            // 6. Calculate travel times for all chargers
            List<Map<String, Object>> allResults = hereService.calculateTravelTimes(userLat, userLng, allDestinations)
                    .stream()
                    .filter(result -> (Integer) result.get("distanceMeters") / 1000 <= radiusKm)
                    .map(result -> {
                        Map<String, Object> response = new HashMap<>(result);
                        Map<String, Object> destination = (Map<String, Object>) result.get("destination");

                        if ((Boolean) destination.getOrDefault("isExternal", false)) {
                            // External charger - add relevant fields
                            response.put("name", destination.get("name"));
                            response.put("address", destination.get("address"));
                            response.put("isExternal", true);
                            response.put("isBookable", false);
                            response.put("type", "commercial");
                        } else {
                            // Internal charger - add charger object
                            response.put("charger", destination.get("charger"));
                            response.put("isExternal", false);
                            response.put("isBookable", true);
                            response.put("type", "bookable");
                        }

                        response.put("travelTimeMinutes", (Integer) result.get("travelTimeSeconds") / 60);
                        response.put("distanceKm", (Integer) result.get("distanceMeters") / 1000);
                        return response;
                    })
                    .toList();

            // 7. Sort: bookable chargers first, then by travel time
            return allResults.stream()
                    .sorted((a, b) -> {
                        boolean aBookable = (Boolean) a.getOrDefault("isBookable", false);
                        boolean bBookable = (Boolean) b.getOrDefault("isBookable", false);

                        if (aBookable && !bBookable) return -1;
                        if (!aBookable && bBookable) return 1;

                        return Integer.compare(
                                (Integer) a.get("travelTimeSeconds"),
                                (Integer) b.get("travelTimeSeconds")
                        );
                    })
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new ApiException("Failed to find nearby chargers: " + e.getMessage());
        }
    }

    public Map<String, Object> getChargerStats(Integer chargerId) {
        // Validate charger exists
        Charger charger = chargerRepository.findChargerById(chargerId);
        if (charger == null) {
            throw new ApiException("Charger not found");
        }

        // Get booking statistics
        Integer totalBookings = bookingRepository.countBookingsByChargerId(chargerId);
        Integer completedBookings = bookingRepository.countCompletedBookingsByChargerId(chargerId);
        Integer cancelledBookings = bookingRepository.countCancelledBookingsByChargerId(chargerId);

        // Get financial statistics
        Double totalRevenue = invoiceRepository.sumRevenueByChargerId(chargerId);
        Double totalEnergy = invoiceRepository.sumEnergyByChargerId(chargerId);
        Double avgSessionValue = invoiceRepository.avgSessionValueByChargerId(chargerId);

        // Calculate rates
        double utilizationRate = totalBookings > 0 ? (completedBookings.doubleValue() / totalBookings.doubleValue()) * 100 : 0;
        double cancellationRate = totalBookings > 0 ? (cancelledBookings.doubleValue() / totalBookings.doubleValue()) * 100 : 0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("chargerId", chargerId);
        stats.put("chargerName", charger.getName());
        stats.put("location", charger.getAddress());
        stats.put("pricePerKwh", charger.getPricePerKwh());
        stats.put("maxKw", charger.getMaxKw());
        stats.put("isActive", charger.getIsActive());
        stats.put("totalBookings", totalBookings != null ? totalBookings : 0);
        stats.put("completedSessions", completedBookings != null ? completedBookings : 0);
        stats.put("cancelledBookings", cancelledBookings != null ? cancelledBookings : 0);
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        stats.put("totalEnergyDelivered", totalEnergy != null ? totalEnergy : 0.0);
        stats.put("averageSessionValue", avgSessionValue != null ? avgSessionValue : 0.0);
        stats.put("utilizationRate", Math.round(utilizationRate * 100.0) / 100.0);
        stats.put("cancellationRate", Math.round(cancellationRate * 100.0) / 100.0);
        
        return stats;
    }

}
