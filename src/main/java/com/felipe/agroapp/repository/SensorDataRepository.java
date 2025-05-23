package com.felipe.agroapp.repository;

import com.felipe.agroapp.model.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
    
    List<SensorData> findByParcelaId(String parcelaId);
    
    List<SensorData> findByParcelaIdOrderByFechaDesc(String parcelaId);
    
    @Query("SELECT s FROM SensorData s WHERE s.parcelaId = :parcelaId AND s.fecha >= :fechaInicio AND s.fecha <= :fechaFin ORDER BY s.fecha DESC")
    List<SensorData> findByParcelaIdAndFechaBetween(@Param("parcelaId") String parcelaId, 
                                                   @Param("fechaInicio") LocalDateTime fechaInicio, 
                                                   @Param("fechaFin") LocalDateTime fechaFin);
    
    @Query("SELECT s FROM SensorData s WHERE s.fecha >= :fechaInicio ORDER BY s.fecha DESC")
    List<SensorData> findRecentData(@Param("fechaInicio") LocalDateTime fechaInicio);
    
    @Query("SELECT s FROM SensorData s WHERE s.parcelaId = :parcelaId ORDER BY s.fecha DESC LIMIT 1")
    SensorData findLatestByParcelaId(@Param("parcelaId") String parcelaId);
    
    @Query("SELECT DISTINCT s.parcelaId FROM SensorData s")
    List<String> findDistinctParcelaIds();
} 