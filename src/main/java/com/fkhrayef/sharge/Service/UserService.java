package com.fkhrayef.sharge.Service;

import com.fkhrayef.sharge.Api.ApiException;
import com.fkhrayef.sharge.Model.Car;
import com.fkhrayef.sharge.Model.Charger;
import com.fkhrayef.sharge.Model.User;
import com.fkhrayef.sharge.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final InvoiceRepository invoiceRepository;
    private final ChargerRepository chargerRepository;
    private final CarRepository carRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void addUser(User user) {
        user.setNoShowCount(0);
        userRepository.save(user);
    }

    public void updateUser(Integer id, User user) {
        User oldUser = userRepository.findUserById(id);
        if (oldUser == null) {
            throw new ApiException("User not found");
        }

        oldUser.setName(user.getName());
        oldUser.setEmail(user.getEmail());

        userRepository.save(oldUser);
    }

    public void deleteUser(Integer id) {
        User oldUser = userRepository.findUserById(id);
        if (oldUser == null) {
            throw new ApiException("User not found");
        }

        userRepository.delete(oldUser);
    }

    public Map<String, Object> getUserBookingStats(Integer userId) {
        // Validate user exists
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }

        // Get booking statistics
        Integer completedSessions = bookingRepository.countCompletedBookingsByUserId(userId);
        Double totalSpent = invoiceRepository.sumSpendingByUserId(userId);
        Double avgDuration = bookingRepository.avgSessionDurationByUserId(userId);

        // Get most used charger
        List<Object[]> mostUsedChargerData = bookingRepository.findMostUsedChargerByUserId(userId);
        Integer mostUsedChargerId = null;
        String mostUsedChargerName = null;
        if (!mostUsedChargerData.isEmpty()) {
            mostUsedChargerId = (Integer) mostUsedChargerData.get(0)[0];
            Charger mostUsedCharger = chargerRepository.findChargerById(mostUsedChargerId);
            mostUsedChargerName = mostUsedCharger != null ? mostUsedCharger.getName() : "Unknown";
        }

        // Get most used car
        List<Object[]> mostUsedCarData = bookingRepository.findMostUsedCarByUserId(userId);
        Integer mostUsedCarId = null;
        String mostUsedCarName = null;
        if (!mostUsedCarData.isEmpty()) {
            mostUsedCarId = (Integer) mostUsedCarData.get(0)[0];
            Car mostUsedCar = carRepository.findCarById(mostUsedCarId);
            mostUsedCarName = mostUsedCar != null ? (mostUsedCar.getMake() + " " + mostUsedCar.getModel()) : "Unknown";
        }

        // Calculate no-show rate
        int totalAttempts = completedSessions != null ? completedSessions.intValue() + user.getNoShowCount() : user.getNoShowCount();
        double noShowRate = totalAttempts > 0 ? (user.getNoShowCount().doubleValue() / totalAttempts) * 100 : 0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("userId", userId);
        stats.put("userName", user.getName());
        stats.put("userEmail", user.getEmail());
        stats.put("completedSessions", completedSessions != null ? completedSessions : 0);
        stats.put("totalSpent", totalSpent != null ? totalSpent : 0.0);
        stats.put("averageSessionDuration", avgDuration != null ? Math.round(avgDuration) : 0);
        stats.put("mostUsedChargerId", mostUsedChargerId);
        stats.put("mostUsedChargerName", mostUsedChargerName != null ? mostUsedChargerName : "None");
        stats.put("mostUsedCarId", mostUsedCarId);
        stats.put("mostUsedCarName", mostUsedCarName != null ? mostUsedCarName : "None");
        stats.put("noShowCount", user.getNoShowCount());
        stats.put("noShowRate", Math.round(noShowRate * 100.0) / 100.0);
        stats.put("reliabilityScore", Math.round((100 - noShowRate) * 100.0) / 100.0);
        
        return stats;
    }

    public List<User> getTopProviders(String sortBy, int limit) {
        // Get all chargers
        List<Charger> allChargers = chargerRepository.findAll();
        
        // Group chargers by owner
        Map<Integer, List<Charger>> chargersByOwner = allChargers.stream()
                .collect(Collectors.groupingBy(Charger::getOwnerId));
        
        // Calculate revenue for each owner and store in maps
        Map<Integer, Double> ownerRevenues = new HashMap<>();
        for (Integer ownerId : chargersByOwner.keySet()) {
            List<Charger> ownerChargers = chargersByOwner.get(ownerId);
            
            double totalRevenue = 0.0;
            for (Charger charger : ownerChargers) {
                Double revenue = invoiceRepository.sumRevenueByChargerId(charger.getId());
                totalRevenue += (revenue != null ? revenue : 0.0);
            }
            
            ownerRevenues.put(ownerId, totalRevenue);
        }
        
        // Sort owner IDs by revenue
        List<Integer> sortedOwnerIds = new ArrayList<>(ownerRevenues.keySet());
        sortedOwnerIds.sort((a, b) -> Double.compare(ownerRevenues.get(b), ownerRevenues.get(a)));
        
        // Get top users
        List<User> topProviders = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, sortedOwnerIds.size()); i++) {
            User user = userRepository.findUserById(sortedOwnerIds.get(i));
            if (user != null) {
                topProviders.add(user);
            }
        }
        
        return topProviders;
    }
}
