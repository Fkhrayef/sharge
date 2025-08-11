package com.fkhrayef.sharge.Service;

import com.fkhrayef.sharge.Api.ApiException;
import com.fkhrayef.sharge.Model.Car;
import com.fkhrayef.sharge.Repository.BookingRepository;
import com.fkhrayef.sharge.Repository.CarRepository;
import com.fkhrayef.sharge.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    public void addCar(Car car) {
        if (userRepository.findUserById(car.getUserId()) == null) {
            throw new ApiException("User not found");
        }

        carRepository.save(car);
    }

    public void updateCar(Integer id, Car car) {
        Car oldCar = carRepository.findCarById(id);
        if (oldCar == null) {
            throw new ApiException("Car not found");
        }

        if (userRepository.findUserById(car.getUserId()) == null) {
            throw new ApiException("User not found");
        }

        if (bookingRepository.existsByCarIdAndStatusIn(id, List.of("RESERVED", "IN_PROGRESS")) && !oldCar.getUserId().equals(car.getUserId())) {
            throw new ApiException("You cannot transfer car ownership while car has an active booking");
        }

        oldCar.setUserId(car.getUserId());
        oldCar.setMake(car.getMake());
        oldCar.setModel(car.getModel());
        oldCar.setNickname(car.getNickname());
        oldCar.setConnectorType(car.getConnectorType());
        oldCar.setMaxKw(car.getMaxKw());

        carRepository.save(oldCar);
    }

    public void deleteCar(Integer id) {
        Car oldCar = carRepository.findCarById(id);
        if (oldCar == null) {
            throw new ApiException("Car not found");
        }

        if (bookingRepository.existsByCarIdAndStatusIn(id, List.of("RESERVED", "IN_PROGRESS"))) {
            throw new ApiException("You cannot delete car while car has an active booking");
        }

        carRepository.delete(oldCar);
    }

    public List<Car> getCarsByUserId(Integer userId) {
        if (userRepository.findUserById(userId) == null) {
            throw new ApiException("User not found");
        }
        return carRepository.findCarsByUserId(userId);
    }
}
