package com.nines.nutsfact.api.v1.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nines.nutsfact.api.v1.request.UnitRequest;
import com.nines.nutsfact.api.v1.response.ApiResponse;
import com.nines.nutsfact.domain.model.master.Unit;
import com.nines.nutsfact.domain.service.UnitService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Unit>>> findAll() {
        List<Unit> units = unitService.findAll();
        return ResponseEntity.ok(ApiResponse.success(units));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<Unit>>> findByType(@PathVariable("type") Integer type) {
        List<Unit> units = unitService.findByType(type);
        return ResponseEntity.ok(ApiResponse.success(units));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Unit>> findById(@PathVariable("id") Integer id) {
        Unit unit = unitService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(unit));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Unit>> create(@Valid @RequestBody UnitRequest request) {
        Unit unit = new Unit();
        unit.setUnitName(request.getUnitName());
        unit.setUnitType(request.getUnitType());

        Unit created = unitService.create(unit);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Unit>> update(
            @PathVariable("id") Integer id,
            @Valid @RequestBody UnitRequest request) {
        Unit unit = new Unit();
        unit.setUnitName(request.getUnitName());
        unit.setUnitType(request.getUnitType());

        Unit updated = unitService.update(id, unit);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Integer id) {
        unitService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
