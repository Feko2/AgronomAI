package com.felipe.agroapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "PARCELAS")
public class Parcela {
    
    @Id
    @Column(name = "PARCELA_ID")
    private String parcelaId;
    
    @Column(name = "NOMBRE")
    private String nombre;
    
    @Column(name = "TIPO_CULTIVO")
    private String tipoCultivo;
    
    @Column(name = "AREA_HECTAREAS")
    private Double areaHectareas;
    
    @Column(name = "UBICACION")
    private String ubicacion;
    
    @Column(name = "FECHA_SIEMBRA")
    private LocalDateTime fechaSiembra;
    
    @Column(name = "ESTADO")
    private String estado; // ACTIVA, COSECHADA, EN_PREPARACION
    
    @Column(name = "HUMEDAD_OPTIMA_MIN")
    private Double humedadOptimaMin;
    
    @Column(name = "HUMEDAD_OPTIMA_MAX")
    private Double humedadOptimaMax;
    
    @Column(name = "PH_OPTIMO_MIN")
    private Double phOptimoMin;
    
    @Column(name = "PH_OPTIMO_MAX")
    private Double phOptimoMax;
    
    @Column(name = "NITROGENO_OPTIMO_MIN")
    private Double nitrogenoOptimoMin;
    
    @Column(name = "NITROGENO_OPTIMO_MAX")
    private Double nitrogenoOptimoMax;

    // Constructores
    public Parcela() {}

    public Parcela(String parcelaId, String nombre, String tipoCultivo, Double areaHectareas, String ubicacion) {
        this.parcelaId = parcelaId;
        this.nombre = nombre;
        this.tipoCultivo = tipoCultivo;
        this.areaHectareas = areaHectareas;
        this.ubicacion = ubicacion;
        this.estado = "ACTIVA";
    }

    // Getters y Setters
    public String getParcelaId() {
        return parcelaId;
    }

    public void setParcelaId(String parcelaId) {
        this.parcelaId = parcelaId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoCultivo() {
        return tipoCultivo;
    }

    public void setTipoCultivo(String tipoCultivo) {
        this.tipoCultivo = tipoCultivo;
    }

    public Double getAreaHectareas() {
        return areaHectareas;
    }

    public void setAreaHectareas(Double areaHectareas) {
        this.areaHectareas = areaHectareas;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public LocalDateTime getFechaSiembra() {
        return fechaSiembra;
    }

    public void setFechaSiembra(LocalDateTime fechaSiembra) {
        this.fechaSiembra = fechaSiembra;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Double getHumedadOptimaMin() {
        return humedadOptimaMin;
    }

    public void setHumedadOptimaMin(Double humedadOptimaMin) {
        this.humedadOptimaMin = humedadOptimaMin;
    }

    public Double getHumedadOptimaMax() {
        return humedadOptimaMax;
    }

    public void setHumedadOptimaMax(Double humedadOptimaMax) {
        this.humedadOptimaMax = humedadOptimaMax;
    }

    public Double getPhOptimoMin() {
        return phOptimoMin;
    }

    public void setPhOptimoMin(Double phOptimoMin) {
        this.phOptimoMin = phOptimoMin;
    }

    public Double getPhOptimoMax() {
        return phOptimoMax;
    }

    public void setPhOptimoMax(Double phOptimoMax) {
        this.phOptimoMax = phOptimoMax;
    }

    public Double getNitrogenoOptimoMin() {
        return nitrogenoOptimoMin;
    }

    public void setNitrogenoOptimoMin(Double nitrogenoOptimoMin) {
        this.nitrogenoOptimoMin = nitrogenoOptimoMin;
    }

    public Double getNitrogenoOptimoMax() {
        return nitrogenoOptimoMax;
    }

    public void setNitrogenoOptimoMax(Double nitrogenoOptimoMax) {
        this.nitrogenoOptimoMax = nitrogenoOptimoMax;
    }
} 