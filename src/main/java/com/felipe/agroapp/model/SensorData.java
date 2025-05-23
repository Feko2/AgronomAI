package com.felipe.agroapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_readings")
public class SensorData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sensor_seq")
    @SequenceGenerator(name = "sensor_seq", sequenceName = "sensor_readings_seq", allocationSize = 1)
    private Long id;
    
    @Column(name = "parcela_id")
    private String parcelaId;
    
    private Double humedad;
    private Double nitrogeno;
    private Double ph;
    private Double temperatura;
    private Double luminosidad;
    private LocalDateTime fecha;
    private String estado; // NORMAL, ALERTA, CRITICO

    // Constructores
    public SensorData() {}

    public SensorData(String parcelaId, Double humedad, Double nitrogeno, Double ph, 
                     Double temperatura, Double luminosidad, LocalDateTime fecha) {
        this.parcelaId = parcelaId;
        this.humedad = humedad;
        this.nitrogeno = nitrogeno;
        this.ph = ph;
        this.temperatura = temperatura;
        this.luminosidad = luminosidad;
        this.fecha = fecha;
        this.estado = "NORMAL";
    }

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

    public Double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(Double temperatura) {
        this.temperatura = temperatura;
    }

    public Double getLuminosidad() {
        return luminosidad;
    }

    public void setLuminosidad(Double luminosidad) {
        this.luminosidad = luminosidad;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return String.format("SensorData{id=%d, parcelaId='%s', humedad=%.1f, nitrogeno=%.1f, ph=%.2f, fecha=%s}",
                id, parcelaId, humedad, nitrogeno, ph, fecha);
    }
} 