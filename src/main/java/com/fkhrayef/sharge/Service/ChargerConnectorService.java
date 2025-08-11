package com.fkhrayef.sharge.Service;

import com.fkhrayef.sharge.Api.ApiException;
import com.fkhrayef.sharge.Model.ChargerConnector;
import com.fkhrayef.sharge.Repository.ChargerConnectorRepository;
import com.fkhrayef.sharge.Repository.ChargerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChargerConnectorService {

    private final ChargerConnectorRepository chargerConnectorRepository;
    private final ChargerRepository chargerRepository;

    public List<ChargerConnector> getAllChargerConnectors() {
        return chargerConnectorRepository.findAll();
    }

    public void addChargerConnector(ChargerConnector chargerConnector) {
        if (chargerRepository.findChargerById(chargerConnector.getChargerId()) == null) {
            throw new ApiException("Charger not found");
        }
        for (ChargerConnector cc : chargerConnectorRepository.findAll()) {
            if (cc.getChargerId().equals(chargerConnector.getChargerId()) && cc.getConnectorType().equals(chargerConnector.getConnectorType())) {
                throw new ApiException("ChargerConnector already exists");
            }
        }
        
        chargerConnectorRepository.save(chargerConnector);
    }

    public void updateChargerConnector(Integer id, ChargerConnector chargerConnector) {
        ChargerConnector oldChargerConnector = chargerConnectorRepository.findChargerConnectorById(id);
        if (oldChargerConnector == null) {
            throw new ApiException("ChargerConnector not found");
        }
        if (chargerRepository.findChargerById(chargerConnector.getChargerId()) == null) {
            throw new ApiException("Charger not found");
        }

        oldChargerConnector.setChargerId(chargerConnector.getChargerId());
        oldChargerConnector.setConnectorType(chargerConnector.getConnectorType());

        chargerConnectorRepository.save(oldChargerConnector);
    }

    public void deleteChargerConnector(Integer id) {
        ChargerConnector oldChargerConnector = chargerConnectorRepository.findChargerConnectorById(id);
        if (oldChargerConnector == null) {
            throw new ApiException("ChargerConnector not found");
        }

        chargerConnectorRepository.delete(oldChargerConnector);
    }
}
