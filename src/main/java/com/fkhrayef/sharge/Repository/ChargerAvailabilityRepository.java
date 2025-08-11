package com.fkhrayef.sharge.Repository;

import com.fkhrayef.sharge.Model.ChargerAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface ChargerAvailabilityRepository extends JpaRepository<ChargerAvailability, Integer> {
    ChargerAvailability findChargerAvailabilityById(Integer id);

    List<ChargerAvailability> findChargerAvailabilityByChargerId(Integer chargerId);

    @Query("SELECT c FROM ChargerAvailability c WHERE c.chargerId = :chargerId AND c.dayOfWeek = :dayOfWeek AND c.startTime <= :currentTime AND c.endTime >= :currentTime")
    ChargerAvailability getChargerAvailabilitiesByCurrentTime(Integer chargerId, String dayOfWeek, LocalTime currentTime);
}
