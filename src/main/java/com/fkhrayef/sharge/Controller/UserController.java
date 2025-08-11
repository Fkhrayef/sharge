package com.fkhrayef.sharge.Controller;

import com.fkhrayef.sharge.Api.ApiResponse;
import com.fkhrayef.sharge.Model.User;
import com.fkhrayef.sharge.Service.UserService;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUsers());
    }

    @PostMapping("/add")
    public ResponseEntity<?> addUser(@Valid @RequestBody User user, Errors errors) {
        if (errors.hasErrors()) {
            ArrayList<String> errorMessages = new ArrayList<>();
            for (FieldError error : errors.getFieldErrors()) {
                errorMessages.add(error.getDefaultMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessages);
        }

        userService.addUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("User added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable("id") Integer id, @Valid @RequestBody User user, Errors errors) {
        if (errors.hasErrors()) {
            ArrayList<String> errorMessages = new ArrayList<>();
            for (FieldError error : errors.getFieldErrors()) {
                errorMessages.add(error.getDefaultMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessages);
        }
        userService.updateUser(id, user);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("User updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{userId}/booking-stats")
    public ResponseEntity<?> getUserBookingStats(@PathVariable Integer userId) {
        Map<String, Object> stats = userService.getUserBookingStats(userId);
        return ResponseEntity.status(HttpStatus.OK).body(stats);
    }

    @GetMapping("/owners/leaderboard")
    public ResponseEntity<?> getTopProviders(
            @RequestParam(defaultValue = "revenue") String sortBy,
            @RequestParam(defaultValue = "10") int limit) {
        List<User> providers = userService.getTopProviders(sortBy, limit);
        return ResponseEntity.status(HttpStatus.OK).body(providers);
    }
}
