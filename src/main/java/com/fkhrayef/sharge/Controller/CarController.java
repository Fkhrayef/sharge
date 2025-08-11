package com.fkhrayef.sharge.Controller;

import com.fkhrayef.sharge.Api.ApiResponse;
import com.fkhrayef.sharge.Model.Car;
import com.fkhrayef.sharge.Service.CarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllCars() {
        return ResponseEntity.status(HttpStatus.OK).body(carService.getAllCars());
    }

    @PostMapping("/add")
    public ResponseEntity<?> addCar(@Valid @RequestBody Car car, Errors errors) {
        if (errors.hasErrors()) {
            ArrayList<String> errorMessages = new ArrayList<>();
            for (FieldError error : errors.getFieldErrors()) {
                errorMessages.add(error.getDefaultMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessages);
        }

        carService.addCar(car);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("Car added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCar(@PathVariable("id") Integer id, @Valid @RequestBody Car car, Errors errors) {
        if (errors.hasErrors()) {
            ArrayList<String> errorMessages = new ArrayList<>();
            for (FieldError error : errors.getFieldErrors()) {
                errorMessages.add(error.getDefaultMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessages);
        }

        carService.updateCar(id, car);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("Car updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteCar(@PathVariable("id") Integer id) {
        carService.deleteCar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getCarsByUserId(@PathVariable Integer userId) {
        List<Car> userCars = carService.getCarsByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(userCars);
    }
}
