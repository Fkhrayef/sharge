package com.fkhrayef.sharge.Repository;

import com.fkhrayef.sharge.Model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Integer> {
    Car findCarById(Integer id);
    
    List<Car> findCarsByUserId(Integer userId);
}
