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

import com.nines.nutsfact.api.v1.request.RawMaterialAdditiveRequest;
import com.nines.nutsfact.api.v1.response.ApiResponse;
import com.nines.nutsfact.domain.model.additive.RawMaterialAdditive;
import com.nines.nutsfact.domain.service.RawMaterialAdditiveService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/raw-materials/{foodId}/additives")
@RequiredArgsConstructor
public class RawMaterialAdditiveController {

    private final RawMaterialAdditiveService rawMaterialAdditiveService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RawMaterialAdditive>>> findByFoodId(
            @PathVariable("foodId") Integer foodId) {
        List<RawMaterialAdditive> additives = rawMaterialAdditiveService.findByFoodIdWithAdditive(foodId);
        return ResponseEntity.ok(ApiResponse.success(additives, additives.size()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RawMaterialAdditive>> findById(
            @PathVariable("foodId") Integer foodId,
            @PathVariable("id") Integer id) {
        RawMaterialAdditive additive = rawMaterialAdditiveService.findByIdWithBusinessAccountFilter(id);
        return ResponseEntity.ok(ApiResponse.success(additive));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RawMaterialAdditive>> create(
            @PathVariable("foodId") Integer foodId,
            @Valid @RequestBody RawMaterialAdditiveRequest request) {
        RawMaterialAdditive additive = convertToEntity(request);
        additive.setFoodId(foodId);
        RawMaterialAdditive created = rawMaterialAdditiveService.create(additive);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RawMaterialAdditive>> update(
            @PathVariable("foodId") Integer foodId,
            @PathVariable("id") Integer id,
            @Valid @RequestBody RawMaterialAdditiveRequest request) {
        RawMaterialAdditive additive = convertToEntity(request);
        additive.setFoodId(foodId);
        RawMaterialAdditive updated = rawMaterialAdditiveService.updateWithBusinessAccountFilter(id, additive);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable("foodId") Integer foodId,
            @PathVariable("id") Integer id) {
        rawMaterialAdditiveService.deleteWithBusinessAccountFilter(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private RawMaterialAdditive convertToEntity(RawMaterialAdditiveRequest request) {
        return RawMaterialAdditive.builder()
                .foodId(request.getFoodId())
                .additiveId(request.getAdditiveId())
                .displayOrder(request.getDisplayOrder())
                .usageAmount(request.getUsageAmount())
                .exemptionType(request.getExemptionType())
                .exemptionReason(request.getExemptionReason())
                .allergenOrigin(request.getAllergenOrigin())
                .isActive(request.getIsActive())
                .build();
    }
}
