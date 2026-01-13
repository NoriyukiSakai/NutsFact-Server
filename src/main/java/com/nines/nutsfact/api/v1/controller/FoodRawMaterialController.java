package com.nines.nutsfact.api.v1.controller;

import com.nines.nutsfact.api.v1.request.FoodRawMaterialRequest;
import com.nines.nutsfact.api.v1.response.ApiResponse;
import com.nines.nutsfact.api.v1.response.DeleteResponse;
import com.nines.nutsfact.domain.model.FoodRawMaterial;
import com.nines.nutsfact.domain.model.SelectItem;
import com.nines.nutsfact.domain.service.FoodRawMaterialService;
import com.nines.nutsfact.infrastructure.converter.FoodRawMaterialConverter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 原材料API Controller
 * RESTful API設計に準拠
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/food-raw-materials")
@RequiredArgsConstructor
public class FoodRawMaterialController {

    private final FoodRawMaterialService service;
    private final FoodRawMaterialConverter converter;

    /**
     * 原材料一覧取得
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FoodRawMaterial>>> findAll() {
        List<FoodRawMaterial> items = service.findAll();
        return ResponseEntity.ok(ApiResponse.success(items, items.size()));
    }

    /**
     * カテゴリ別原材料一覧取得
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<FoodRawMaterial>>> findByCategory(
            @PathVariable Integer categoryId) {
        List<FoodRawMaterial> items = service.findByCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success(items, items.size()));
    }

    /**
     * 選択リスト用原材料一覧取得
     */
    @GetMapping("/select-items")
    public ResponseEntity<ApiResponse<List<SelectItem>>> findSelectItems(
            @RequestParam(required = false) Integer categoryId) {
        List<SelectItem> items = service.findSelectItems(categoryId);
        return ResponseEntity.ok(ApiResponse.success(items, items.size()));
    }

    /**
     * 原材料詳細取得
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodRawMaterial>> findById(@PathVariable Integer id) {
        FoodRawMaterial item = service.findById(id);
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    /**
     * 食品番号で原材料取得
     */
    @GetMapping("/by-food-no/{foodNo}")
    public ResponseEntity<ApiResponse<FoodRawMaterial>> findByFoodNo(@PathVariable String foodNo) {
        FoodRawMaterial item = service.findByFoodNo(foodNo);
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    /**
     * 原材料新規作成
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FoodRawMaterial>> create(
            @Valid @RequestBody FoodRawMaterialRequest request) {
        FoodRawMaterial entity = converter.toEntity(request);
        FoodRawMaterial created = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(created));
    }

    /**
     * 原材料更新
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodRawMaterial>> update(
            @PathVariable Integer id,
            @Valid @RequestBody FoodRawMaterialRequest request) {
        request.setFoodId(id);
        FoodRawMaterial entity = converter.toEntity(request);
        FoodRawMaterial updated = service.update(entity);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * 原材料削除
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResponse> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok(DeleteResponse.success(id));
    }
}
