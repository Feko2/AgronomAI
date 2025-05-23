package com.felipe.agroapp.controller;

import com.felipe.agroapp.model.Parcela;
import com.felipe.agroapp.repository.ParcelaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/parcelas")
@CrossOrigin(origins = "http://localhost:5173")
public class ParcelaController {

    @Autowired
    private ParcelaRepository parcelaRepository;

    @GetMapping
    public ResponseEntity<List<Parcela>> getAllParcelas() {
        List<Parcela> parcelas = parcelaRepository.findAll();
        return ResponseEntity.ok(parcelas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Parcela> getParcelaById(@PathVariable String id) {
        Optional<Parcela> parcela = parcelaRepository.findById(id);
        return parcela.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/activas")
    public ResponseEntity<List<Parcela>> getParcelasActivas() {
        List<Parcela> parcelasActivas = parcelaRepository.findParcelasActivas();
        return ResponseEntity.ok(parcelasActivas);
    }

    @GetMapping("/cultivo/{tipoCultivo}")
    public ResponseEntity<List<Parcela>> getParcelasByCultivo(@PathVariable String tipoCultivo) {
        List<Parcela> parcelas = parcelaRepository.findByTipoCultivo(tipoCultivo);
        return ResponseEntity.ok(parcelas);
    }
} 