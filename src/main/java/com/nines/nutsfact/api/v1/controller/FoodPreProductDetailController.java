package com.nines.nutsfact.api.v1.controller;

import com.nines.nutsfact.api.v1.request.FoodPreProductDetailRequest;
import com.nines.nutsfact.api.v1.response.ApiResponse;
import com.nines.nutsfact.api.v1.response.DeleteResponse;
import com.nines.nutsfact.domain.model.FoodPreProductDetailItem;
import com.nines.nutsfact.domain.service.FoodPreProductDetailService;
import com.nines.nutsfact.infrastructure.converter.FoodPreProductConverter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 仕込品明細API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/food-pre-product-details")
@RequiredArgsConstructor
public class FoodPreProductDetailController {

    private final FoodPreProductDetailService service;
    private final FoodPreProductConverter converter;

    /**
     * 仕込品IDで明細一覧取得
     */
    @GetMapping("/by-pre-id/{preId}")
    public ResponseEntity<ApiResponse<List<FoodPreProductDetailItem>>> findByPreId(
            @PathVariable Integer preId) {
        List<FoodPreProductDetailItem> items = service.findByPreId(preId);
        return ResponseEntity.ok(ApiResponse.success(items, items.size()));
    }

    /**
     * 明細詳細取得
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodPreProductDetailItem>> findById(@PathVariable Integer id) {
        FoodPreProductDetailItem item = service.findById(id);
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    /**
     * 明細新規作成
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FoodPreProductDetailItem>> create(
            @Valid @RequestBody FoodPreProductDetailRequest request) {
        FoodPreProductDetailItem entity = converter.toDetailEntity(request);
        FoodPreProductDetailItem created = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(created));
    }

    /**
     * 明細更新
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodPreProductDetailItem>> update(
            @PathVariable Integer id,
            @Valid @RequestBody FoodPreProductDetailRequest request) {
        request.setDetailId(id);
        FoodPreProductDetailItem entity = converter.toDetailEntity(request);
        FoodPreProductDetailItem updated = service.update(entity);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * 明細削除
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResponse> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok(DeleteResponse.success(id));
    }
}
