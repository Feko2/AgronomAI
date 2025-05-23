package com.felipe.agroapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SENSOR_READINGS")
public class SensorData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "PARCELA_ID")
    private String parcelaId;
    
    @Column(name = "HUMEDAD")
    private Double humedad;
    
    @Column(name = "NITROGENO")
    private Double nitrogeno;
    
    @Column(name = "PH")
    private Double ph;
    
    @Column(name = "FECHA")
    private LocalDateTime fecha;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getParcelaId() {
        return parcelaId;
    }

    public void setParcelaId(String parcelaId) {
        this.parcelaId = parcelaId;
    }

    public Double getHumedad() {
        return humedad;
    }

    public void setHumedad(Double humedad) {
        this.humedad = humedad;
    }

    public Double getNitrogeno() {
        return nitrogeno;
    }

    public void setNitrogeno(Double nitrogeno) {
        this.nitrogeno = nitrogeno;
    }

    public Double getPh() {
        return ph;
    }

    public void setPh(Double ph) {
        this.ph = ph;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
} 