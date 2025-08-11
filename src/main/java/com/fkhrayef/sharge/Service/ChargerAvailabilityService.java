package com.fkhrayef.sharge.Service;

import com.fkhrayef.sharge.Api.ApiException;
import com.fkhrayef.sharge.Model.ChargerAvailability;
import com.fkhrayef.sharge.Repository.ChargerAvailabilityRepository;
import com.fkhrayef.sharge.Repository.ChargerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChargerAvailabilityService {

    private final ChargerAvailabilityRepository chargerAvailabilityRepository;
    private final ChargerRepository chargerRepository;

    public List<ChargerAvailability> getAllChargerAvailabilities() {
        return chargerAvailabilityRepository.findAll();
    }

    public void addChargerAvailability(ChargerAvailability chargerAvailability) {
        if (chargerRepository.findChargerById(chargerAvailability.getChargerId()) == null) {
            throw new ApiException("Charger not found");
        }
        chargerAvailabilityRepository.save(chargerAvailability);
    }

    public void updateChargerAvailability(Integer id, ChargerAvailability chargerAvailability) {
        ChargerAvailability oldChargerAvailability = chargerAvailabilityRepository.findChargerAvailabilityById(id);
        if (oldChargerAvailability == null) {
            throw new ApiException("ChargerAvailability not found");
        }
        if (chargerRepository.findChargerById(chargerAvailability.getChargerId()) == null) {
            throw new ApiException("Charger not found");
        }

        oldChargerAvailability.setChargerId(chargerAvailability.getChargerId());
        oldChargerAvailability.setDayOfWeek(chargerAvailability.getDayOfWeek());
        oldChargerAvailability.setStartTime(chargerAvailability.getStartTime());
        oldChargerAvailability.setEndTime(chargerAvailability.getEndTime());

        chargerAvailabilityRepository.save(oldChargerAvailability);
    }

    public void deleteChargerAvailability(Integer id) {
        ChargerAvailability oldChargerAvailability = chargerAvailabilityRepository.findChargerAvailabilityById(id);
        if (oldChargerAvailability == null) {
            throw new ApiException("ChargerAvailability not found");
        }

        chargerAvailabilityRepository.delete(oldChargerAvailability);
    }
}
