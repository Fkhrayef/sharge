package com.fkhrayef.sharge.Controller;

import com.fkhrayef.sharge.Api.ApiResponse;
import com.fkhrayef.sharge.Model.ChargerAvailability;
import com.fkhrayef.sharge.Service.ChargerAvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1/charger-availabilities")
@RequiredArgsConstructor
public class ChargerAvailabilityController {

    private final ChargerAvailabilityService chargerAvailabilityService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllChargerAvailabilities() {
        return ResponseEntity.status(HttpStatus.OK).body(chargerAvailabilityService.getAllChargerAvailabilities());
    }

    @PostMapping("/add")
    public ResponseEntity<?> addChargerAvailability(@Valid @RequestBody ChargerAvailability chargerAvailability, Errors errors) {
        if (errors.hasErrors()) {
            ArrayList<String> errorMessages = new ArrayList<>();
            for (FieldError error : errors.getFieldErrors()) {
                errorMessages.add(error.getDefaultMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessages);
        }

        chargerAvailabilityService.addChargerAvailability(chargerAvailability);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("ChargerAvailability added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateChargerAvailability(@PathVariable("id") Integer id, @Valid @RequestBody ChargerAvailability chargerAvailability, Errors errors) {
        if (errors.hasErrors()) {
            ArrayList<String> errorMessages = new ArrayList<>();
            for (FieldError error : errors.getFieldErrors()) {
                errorMessages.add(error.getDefaultMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessages);
        }

        chargerAvailabilityService.updateChargerAvailability(id, chargerAvailability);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("ChargerAvailability updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteChargerAvailability(@PathVariable("id") Integer id) {
        chargerAvailabilityService.deleteChargerAvailability(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
