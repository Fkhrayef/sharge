package com.fkhrayef.sharge.Controller;

import com.fkhrayef.sharge.Api.ApiResponse;
import com.fkhrayef.sharge.Model.Charger;
import com.fkhrayef.sharge.Service.ChargerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chargers")
@RequiredArgsConstructor
public class ChargerController {

    private final ChargerService chargerService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllChargers() {
        return ResponseEntity.status(HttpStatus.OK).body(chargerService.getAllChargers());
    }

    @PostMapping("/add")
    public ResponseEntity<?> addCharger(@Valid @RequestBody Charger charger, Errors errors) {
        if (errors.hasErrors()) {
            ArrayList<String> errorMessages = new ArrayList<>();
            for (FieldError error : errors.getFieldErrors()) {
                errorMessages.add(error.getDefaultMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessages);
        }

        chargerService.addCharger(charger);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("Charger added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCharger(@PathVariable("id") Integer id, @Valid @RequestBody Charger charger, Errors errors) {
        if (errors.hasErrors()) {
            ArrayList<String> errorMessages = new ArrayList<>();
            for (FieldError error : errors.getFieldErrors()) {
                errorMessages.add(error.getDefaultMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessages);
        }

        chargerService.updateCharger(id, charger);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("Charger updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteCharger(@PathVariable("id") Integer id) {
        chargerService.deleteCharger(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/deactivate/{id}")
    public ResponseEntity<?> deactivateCharger(@PathVariable("id") Integer id) {
        chargerService.deactivateCharger(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("Charger deactivated successfully"));
    }

    @PutMapping("/activate/{id}")
    public ResponseEntity<?> activateCharger(@PathVariable("id") Integer id) {
        chargerService.activateCharger(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("Charger activated successfully"));
    }

    @GetMapping("/nearby")
    public ResponseEntity<?> findNearbyChargers(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "50") int radiusKm,
            @RequestParam("carId") Integer carId) {

        List<Map<String, Object>> nearbyChargers = chargerService.findNearbyChargers(carId, latitude, longitude, limit, radiusKm);
        return ResponseEntity.status(HttpStatus.OK).body(nearbyChargers);
    }

    @GetMapping("/nearby/external")
    public ResponseEntity<?> findNearbyChargersWithExternal(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "50") int radiusKm,
            @RequestParam("carId") Integer carId) {

        List<Map<String, Object>> nearbyChargers = chargerService.findNearbyChargersWithExternal(carId, latitude, longitude, limit, radiusKm);
        return ResponseEntity.status(HttpStatus.OK).body(nearbyChargers);
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<?> getChargerStats(@PathVariable Integer id) {
        Map<String, Object> stats = chargerService.getChargerStats(id);
        return ResponseEntity.status(HttpStatus.OK).body(stats);
    }

}
