package com.fkhrayef.sharge.Repository;

import com.fkhrayef.sharge.Model.Charger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargerRepository extends JpaRepository<Charger, Integer> {
    Charger findChargerById(Integer id);

    List<Charger> findChargerByIsActiveTrue();

}
