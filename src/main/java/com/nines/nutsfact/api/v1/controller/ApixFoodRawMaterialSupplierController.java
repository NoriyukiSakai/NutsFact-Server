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
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("item", toMap(item));
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

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("item", toMap(created));
        return ResponseEntity.ok(response);
    }

    /**
     * 仕入先情報更新
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(
            @RequestBody Map<String, Object> request) {

        Integer id = getIntegerAny(request, "id", "id");
        if (id == null) {
            throw new IllegalArgumentException("idが指定されていません");
        }

        // 既存データを取得
        FoodRawMaterialSupplier existing = service.findById(id);

        // リクエストの値で上書き
        mergeFromMap(existing, request);

        FoodRawMaterialSupplier updated = service.update(existing);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("item", toMap(updated));
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
        map.put("food_id", item.getFoodId());
        map.put("supplier_id", item.getSupplierId());
        map.put("supplier_name", item.getSupplierName());
        map.put("purchase_price", item.getPurchasePrice());
        map.put("volume_amount", item.getVolumeAmount());
        map.put("weight_or_capacity", item.getWeightOrCapacity());
        map.put("convert_ratio", item.getConvertRatio());
        map.put("price_per_unit", item.getPricePerUnit());
        map.put("create_date", item.getCreateDate());
        map.put("last_update_date", item.getLastUpdateDate());
        map.put("is_active", item.getIsActive());
        return map;
    }

    private FoodRawMaterialSupplier fromMap(Map<String, Object> map) {
        FoodRawMaterialSupplier entity = new FoodRawMaterialSupplier();
        entity.setId(getIntegerAny(map, "id", "id"));
        entity.setFoodId(getIntegerAny(map, "food_id", "foodId"));
        entity.setSupplierId(getIntegerAny(map, "supplier_id", "supplierId"));
        entity.setSupplierName(getStringAny(map, "supplier_name", "supplierName"));
        entity.setPurchasePrice(getIntegerAny(map, "purchase_price", "purchasePrice"));
        entity.setVolumeAmount(getIntegerAny(map, "volume_amount", "volumeAmount"));
        entity.setWeightOrCapacity(getIntegerAny(map, "weight_or_capacity", "weightOrCapacity"));
        entity.setConvertRatio(getFloatAny(map, "convert_ratio", "convertRatio"));
        entity.setPricePerUnit(getFloatAny(map, "price_per_unit", "pricePerUnit"));
        entity.setIsActive(getBooleanAny(map, "is_active", "isActive"));
        return entity;
    }

    private void mergeFromMap(FoodRawMaterialSupplier entity, Map<String, Object> map) {
        if (hasKey(map, "food_id", "foodId")) entity.setFoodId(getIntegerAny(map, "food_id", "foodId"));
        if (hasKey(map, "supplier_id", "supplierId")) entity.setSupplierId(getIntegerAny(map, "supplier_id", "supplierId"));
        if (hasKey(map, "purchase_price", "purchasePrice")) entity.setPurchasePrice(getIntegerAny(map, "purchase_price", "purchasePrice"));
        if (hasKey(map, "volume_amount", "volumeAmount")) entity.setVolumeAmount(getIntegerAny(map, "volume_amount", "volumeAmount"));
        if (hasKey(map, "weight_or_capacity", "weightOrCapacity")) entity.setWeightOrCapacity(getIntegerAny(map, "weight_or_capacity", "weightOrCapacity"));
        if (hasKey(map, "convert_ratio", "convertRatio")) entity.setConvertRatio(getFloatAny(map, "convert_ratio", "convertRatio"));
        if (hasKey(map, "price_per_unit", "pricePerUnit")) entity.setPricePerUnit(getFloatAny(map, "price_per_unit", "pricePerUnit"));
        if (hasKey(map, "is_active", "isActive")) entity.setIsActive(getBooleanAny(map, "is_active", "isActive"));
    }

    private boolean hasKey(Map<String, Object> map, String snakeCase, String camelCase) {
        return map.containsKey(snakeCase) || map.containsKey(camelCase);
    }

    private Integer getIntegerAny(Map<String, Object> map, String snakeCase, String camelCase) {
        Integer val = getInteger(map, snakeCase);
        return val != null ? val : getInteger(map, camelCase);
    }

    private String getStringAny(Map<String, Object> map, String snakeCase, String camelCase) {
        String val = getString(map, snakeCase);
        return val != null ? val : getString(map, camelCase);
    }

    private Boolean getBooleanAny(Map<String, Object> map, String snakeCase, String camelCase) {
        if (map.containsKey(snakeCase)) return getBoolean(map, snakeCase);
        return getBoolean(map, camelCase);
    }

    private Float getFloatAny(Map<String, Object> map, String snakeCase, String camelCase) {
        Float val = getFloat(map, snakeCase);
        return val != null ? val : getFloat(map, camelCase);
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
