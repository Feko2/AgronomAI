package com.felipe.agroapp.repository;

import com.felipe.agroapp.model.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
} 