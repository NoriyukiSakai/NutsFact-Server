package com.nines.nutsfact.api.v1.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nines.nutsfact.domain.model.FoodRawMaterialSupplier;
import com.nines.nutsfact.domain.service.FoodRawMaterialSupplierService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 原材料仕入先情報API Controller（apix互換）
 */
@Slf4j
@RestController
@RequestMapping("/apix/FoodRawMaterialSupplier")
@RequiredArgsConstructor
public class ApixFoodRawMaterialSupplierController {

    private final FoodRawMaterialSupplierService service;

    /**
     * 原材料IDで仕入先情報一覧取得
     */
    @GetMapping("/findByFoodId")
    public ResponseEntity<Map<String, Object>> findByFoodId(
            @RequestParam("foodId") Integer foodId) {

        List<FoodRawMaterialSupplier> items = service.findByFoodId(foodId);

        List<Map<String, Object>> itemList = items.stream()
            .map(this::toMap)
            .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("records", itemList.size());
        response.put("item", itemList);
        return ResponseEntity.ok(response);
    }

    /**
     * ID指定で仕入先情報取得
     */
    @GetMapping("/findById")
    public ResponseEntity<Map<String, Object>> findById(
            @RequestParam("id") Integer id) {

        FoodRawMaterialSupplier item = service.findById(id);
        Map<String, Object> response = toMap(item);
        response.put("status", "Success");
        return ResponseEntity.ok(response);
    }

    /**
     * 仕入先情報登録
     */
    @PostMapping("/insert")
    public ResponseEntity<Map<String, Object>> insert(
            @RequestBody Map<String, Object> request) {

        FoodRawMaterialSupplier entity = fromMap(request);
        FoodRawMaterialSupplier created = service.create(entity);

        Map<String, Object> response = toMap(created);
        response.put("status", "Success");
        return ResponseEntity.ok(response);
    }

    /**
     * 仕入先情報更新
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(
            @RequestBody Map<String, Object> request) {

        Integer id = getInteger(request, "id");
        if (id == null) {
            throw new IllegalArgumentException("idが指定されていません");
        }

        // 既存データを取得
        FoodRawMaterialSupplier existing = service.findById(id);

        // リクエストの値で上書き
        mergeFromMap(existing, request);

        FoodRawMaterialSupplier updated = service.update(existing);

        Map<String, Object> response = toMap(updated);
        response.put("status", "Success");
        return ResponseEntity.ok(response);
    }

    /**
     * 仕入先情報削除
     */
    @GetMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(
            @RequestParam("id") Integer id) {

        service.delete(id);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("id", id);
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toMap(FoodRawMaterialSupplier item) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", item.getId());
        map.put("foodId", item.getFoodId());
        map.put("supplierId", item.getSupplierId());
        map.put("supplierName", item.getSupplierName());
        map.put("purchasePrice", item.getPurchasePrice());
        map.put("volumeAmount", item.getVolumeAmount());
        map.put("weightOrCapacity", item.getWeightOrCapacity());
        map.put("convertRatio", item.getConvertRatio());
        map.put("pricePerUnit", item.getPricePerUnit());
        map.put("createDate", item.getCreateDate());
        map.put("lastUpdateDate", item.getLastUpdateDate());
        map.put("isActive", item.getIsActive());
        return map;
    }

    private FoodRawMaterialSupplier fromMap(Map<String, Object> map) {
        FoodRawMaterialSupplier entity = new FoodRawMaterialSupplier();
        entity.setId(getInteger(map, "id"));
        entity.setFoodId(getInteger(map, "foodId"));
        entity.setSupplierId(getInteger(map, "supplierId"));
        entity.setSupplierName(getString(map, "supplierName"));
        entity.setPurchasePrice(getInteger(map, "purchasePrice"));
        entity.setVolumeAmount(getInteger(map, "volumeAmount"));
        entity.setWeightOrCapacity(getInteger(map, "weightOrCapacity"));
        entity.setConvertRatio(getFloat(map, "convertRatio"));
        entity.setPricePerUnit(getFloat(map, "pricePerUnit"));
        entity.setIsActive(getBoolean(map, "isActive"));
        return entity;
    }

    private void mergeFromMap(FoodRawMaterialSupplier entity, Map<String, Object> map) {
        if (map.containsKey("foodId")) entity.setFoodId(getInteger(map, "foodId"));
        if (map.containsKey("supplierId")) entity.setSupplierId(getInteger(map, "supplierId"));
        if (map.containsKey("purchasePrice")) entity.setPurchasePrice(getInteger(map, "purchasePrice"));
        if (map.containsKey("volumeAmount")) entity.setVolumeAmount(getInteger(map, "volumeAmount"));
        if (map.containsKey("weightOrCapacity")) entity.setWeightOrCapacity(getInteger(map, "weightOrCapacity"));
        if (map.containsKey("convertRatio")) entity.setConvertRatio(getFloat(map, "convertRatio"));
        if (map.containsKey("pricePerUnit")) entity.setPricePerUnit(getFloat(map, "pricePerUnit"));
        if (map.containsKey("isActive")) entity.setIsActive(getBoolean(map, "isActive"));
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.parseInt(value.toString());
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private Boolean getBoolean(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }

    private Float getFloat(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Float) return (Float) value;
        if (value instanceof Number) return ((Number) value).floatValue();
        return Float.parseFloat(value.toString());
    }
}
