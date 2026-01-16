package com.nines.nutsfact.api.v1.controller;

import com.nines.nutsfact.api.v1.request.FoodSemiFinishedProductDetailRequest;
import com.nines.nutsfact.api.v1.response.ApiResponse;
import com.nines.nutsfact.api.v1.response.DeleteResponse;
import com.nines.nutsfact.domain.model.FoodSemiFinishedProductDetail;
import com.nines.nutsfact.domain.service.FoodSemiFinishedProductDetailService;
import com.nines.nutsfact.infrastructure.converter.FoodSemiFinishedProductConverter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 半完成品明細API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/food-semi-finished-product-details")
@RequiredArgsConstructor
public class FoodSemiFinishedProductDetailController {

    private final FoodSemiFinishedProductDetailService service;
    private final FoodSemiFinishedProductConverter converter;

    /**
     * 半完成品IDで明細一覧取得（businessAccountIdでフィルタリング）
     */
    @GetMapping("/semi/{semiId}")
    public ResponseEntity<ApiResponse<List<FoodSemiFinishedProductDetail>>> findBySemiId(
            @PathVariable Integer semiId) {
        List<FoodSemiFinishedProductDetail> items = service.findBySemiIdWithBusinessAccountFilter(semiId);
        return ResponseEntity.ok(ApiResponse.success(items, items.size()));
    }

    /**
     * 明細詳細取得（businessAccountIdでフィルタリング）
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodSemiFinishedProductDetail>> findById(@PathVariable Integer id) {
        FoodSemiFinishedProductDetail item = service.findByIdWithBusinessAccountFilter(id);
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    /**
     * 明細新規作成
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FoodSemiFinishedProductDetail>> create(
            @Valid @RequestBody FoodSemiFinishedProductDetailRequest request) {
        FoodSemiFinishedProductDetail entity = converter.toDetailEntity(request);
        FoodSemiFinishedProductDetail created = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(created));
    }

    /**
     * 明細更新（businessAccountIdでフィルタリング）
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodSemiFinishedProductDetail>> update(
            @PathVariable Integer id,
            @Valid @RequestBody FoodSemiFinishedProductDetailRequest request) {
        request.setDetailId(id);
        FoodSemiFinishedProductDetail entity = converter.toDetailEntity(request);
        FoodSemiFinishedProductDetail updated = service.updateWithBusinessAccountFilter(entity);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * 明細削除（businessAccountIdでフィルタリング）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResponse> delete(@PathVariable Integer id) {
        service.deleteWithBusinessAccountFilter(id);
        return ResponseEntity.ok(DeleteResponse.success(id));
    }
}
