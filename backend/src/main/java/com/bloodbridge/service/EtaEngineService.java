package com.bloodbridge.service;

import com.bloodbridge.dto.response.EtaResultDTO;

/**
 * Service interface for computing accurate ETAs via Google Maps Directions API or Haversine speed fallback.
 */
public interface EtaEngineService {

    EtaResultDTO calculateEta(Double originLat, Double originLon, Double destLat, Double destLon);
}
