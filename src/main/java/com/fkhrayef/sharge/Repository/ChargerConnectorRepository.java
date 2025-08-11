package com.fkhrayef.sharge.Repository;

import com.fkhrayef.sharge.Model.ChargerConnector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChargerConnectorRepository extends JpaRepository<ChargerConnector, Integer> {
    ChargerConnector findChargerConnectorById(Integer id);

    boolean existsByChargerIdAndConnectorType(Integer chargerId, String connectorType);
}
