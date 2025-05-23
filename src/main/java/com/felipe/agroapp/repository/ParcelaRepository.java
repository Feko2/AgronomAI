package com.felipe.agroapp.repository;

import com.felipe.agroapp.model.Parcela;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ParcelaRepository extends JpaRepository<Parcela, String> {
    
    List<Parcela> findByEstado(String estado);
    
    List<Parcela> findByTipoCultivo(String tipoCultivo);
    
    @Query("SELECT p FROM Parcela p WHERE p.estado = 'ACTIVA'")
    List<Parcela> findParcelasActivas();
} 