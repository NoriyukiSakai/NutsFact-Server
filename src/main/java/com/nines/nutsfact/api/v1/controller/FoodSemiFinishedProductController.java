package com.nines.nutsfact.api.v1.controller;

import com.nines.nutsfact.api.v1.request.FoodSemiFinishedProductRequest;
import com.nines.nutsfact.api.v1.response.ApiResponse;
import com.nines.nutsfact.api.v1.response.DeleteResponse;
import com.nines.nutsfact.domain.model.FoodSemiFinishedProduct;
import com.nines.nutsfact.domain.model.SelectItem;
import com.nines.nutsfact.domain.model.allergy.AllergenSummary;
import com.nines.nutsfact.domain.service.AllergenAggregationService;
import com.nines.nutsfact.domain.service.FoodSemiFinishedProductService;
import com.nines.nutsfact.infrastructure.converter.FoodSemiFinishedProductConverter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 半完成品API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/food-semi-finished-products")
@RequiredArgsConstructor
public class FoodSemiFinishedProductController {

    private final FoodSemiFinishedProductService service;
    private final FoodSemiFinishedProductConverter converter;
    private final AllergenAggregationService allergenAggregationService;

    /**
     * 半完成品一覧取得（businessAccountIdでフィルタリング）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FoodSemiFinishedProduct>>> findAll() {
        List<FoodSemiFinishedProduct> items = service.findAllWithBusinessAccountFilter();
        return ResponseEntity.ok(ApiResponse.success(items, items.size()));
    }

    /**
     * 選択リスト用半完成品一覧取得
     */
    @GetMapping("/select-items")
    public ResponseEntity<ApiResponse<List<SelectItem>>> findSelectItems() {
        List<SelectItem> items = service.findSelectItems();
        return ResponseEntity.ok(ApiResponse.success(items, items.size()));
    }

    /**
     * 半完成品詳細取得（明細含む、businessAccountIdでフィルタリング）
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodSemiFinishedProduct>> findById(@PathVariable Integer id) {
        FoodSemiFinishedProduct item = service.findByIdWithBusinessAccountFilter(id);
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    /**
     * 半完成品新規作成（businessAccountIdを自動設定）
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FoodSemiFinishedProduct>> create(
            @Valid @RequestBody FoodSemiFinishedProductRequest request) {
        FoodSemiFinishedProduct entity = converter.toEntity(request);
        FoodSemiFinishedProduct created = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(created));
    }

    /**
     * 半完成品更新（businessAccountIdでフィルタリング）
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodSemiFinishedProduct>> update(
            @PathVariable Integer id,
            @Valid @RequestBody FoodSemiFinishedProductRequest request) {
        request.setSemiId(id);
        FoodSemiFinishedProduct entity = converter.toEntity(request);
        FoodSemiFinishedProduct updated = service.updateWithBusinessAccountFilter(entity);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * 半完成品削除（businessAccountIdでフィルタリング）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResponse> delete(@PathVariable Integer id) {
        service.deleteWithBusinessAccountFilter(id);
        return ResponseEntity.ok(DeleteResponse.success(id));
    }

    /**
     * アレルゲン情報再計算
     */
    @PostMapping("/{id}/recalculate-allergens")
    public ResponseEntity<ApiResponse<AllergenSummary>> recalculateAllergens(@PathVariable Integer id) {
        AllergenSummary summary = allergenAggregationService.recalculateAndSave(id);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
