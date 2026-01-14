package com.nines.nutsfact.api.v1.controller;

import com.nines.nutsfact.api.v1.request.FoodPreProductRequest;
import com.nines.nutsfact.api.v1.response.ApiResponse;
import com.nines.nutsfact.api.v1.response.DeleteResponse;
import com.nines.nutsfact.domain.model.FoodPreProductItem;
import com.nines.nutsfact.domain.model.SelectItem;
import com.nines.nutsfact.domain.service.FoodPreProductService;
import com.nines.nutsfact.infrastructure.converter.FoodPreProductConverter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 仕込品API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/food-pre-products")
@RequiredArgsConstructor
public class FoodPreProductController {

    private final FoodPreProductService service;
    private final FoodPreProductConverter converter;

    /**
     * 仕込品一覧取得
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FoodPreProductItem>>> findAll() {
        List<FoodPreProductItem> items = service.findAll();
        return ResponseEntity.ok(ApiResponse.success(items, items.size()));
    }

    /**
     * 区分別仕込品一覧取得（businessAccountIdでフィルタリング）
     */
    @GetMapping("/kind/{preKind}")
    public ResponseEntity<ApiResponse<List<FoodPreProductItem>>> findByKind(
            @PathVariable Integer preKind) {
        List<FoodPreProductItem> items = service.findByKindWithBusinessAccountFilter(preKind);
        return ResponseEntity.ok(ApiResponse.success(items, items.size()));
    }

    /**
     * 選択リスト用仕込品一覧取得
     */
    @GetMapping("/select-items")
    public ResponseEntity<ApiResponse<List<SelectItem>>> findSelectItems(
            @RequestParam(required = false) Integer preKind) {
        List<SelectItem> items = service.findSelectItems(preKind);
        return ResponseEntity.ok(ApiResponse.success(items, items.size()));
    }

    /**
     * 仕込品詳細取得（明細含む、businessAccountIdでフィルタリング）
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodPreProductItem>> findById(@PathVariable Integer id) {
        FoodPreProductItem item = service.findByIdWithBusinessAccountFilter(id);
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    /**
     * 仕込品新規作成（businessAccountIdを自動設定）
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FoodPreProductItem>> create(
            @Valid @RequestBody FoodPreProductRequest request) {
        FoodPreProductItem entity = converter.toEntity(request);
        FoodPreProductItem created = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(created));
    }

    /**
     * 仕込品更新（businessAccountIdでフィルタリング）
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodPreProductItem>> update(
            @PathVariable Integer id,
            @Valid @RequestBody FoodPreProductRequest request) {
        request.setPreId(id);
        FoodPreProductItem entity = converter.toEntity(request);
        FoodPreProductItem updated = service.updateWithBusinessAccountFilter(entity);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * 仕込品削除（businessAccountIdでフィルタリング）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResponse> delete(@PathVariable Integer id) {
        service.deleteWithBusinessAccountFilter(id);
        return ResponseEntity.ok(DeleteResponse.success(id));
    }
}
