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

import com.nines.nutsfact.domain.model.FoodPreProductItem;
import com.nines.nutsfact.domain.service.FoodPreProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 仕込品API Controller（apix互換）
 */
@Slf4j
@RestController
@RequestMapping("/apix/FoodPreProduct")
@RequiredArgsConstructor
public class ApixFoodPreProductController {

    private final FoodPreProductService service;

    /**
     * 仕込品一覧取得
     */
    @GetMapping("/getData")
    public ResponseEntity<Map<String, Object>> getData() {
        List<FoodPreProductItem> items = service.findAll();

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
     * ID指定で仕込品取得
     */
    @GetMapping("/findById")
    public ResponseEntity<Map<String, Object>> findById(
            @RequestParam("preId") Integer preId) {

        FoodPreProductItem item = service.findById(preId);
        Map<String, Object> response = toMap(item);
        response.put("status", "Success");
        return ResponseEntity.ok(response);
    }

    /**
     * 仕込品登録
     */
    @PostMapping("/insert")
    public ResponseEntity<Map<String, Object>> insert(
            @RequestBody Map<String, Object> request) {

        FoodPreProductItem entity = fromMap(request);
        FoodPreProductItem created = service.create(entity);

        Map<String, Object> response = toMap(created);
        response.put("status", "Success");
        return ResponseEntity.ok(response);
    }

    /**
     * 仕込品更新
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(
            @RequestBody Map<String, Object> request) {

        Integer preId = getInteger(request, "preId");
        if (preId == null) {
            throw new IllegalArgumentException("preIdが指定されていません");
        }

        // 既存データを取得
        FoodPreProductItem existing = service.findById(preId);

        // リクエストの値で上書き
        mergeFromMap(existing, request);

        FoodPreProductItem updated = service.update(existing);

        Map<String, Object> response = toMap(updated);
        response.put("status", "Success");
        return ResponseEntity.ok(response);
    }

    /**
     * 仕込品削除
     */
    @GetMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(
            @RequestParam("preId") Integer preId) {

        service.delete(preId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("id", preId);
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toMap(FoodPreProductItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put("preId", item.getPreId());
        map.put("preNo", item.getPreNo());
        map.put("preKind", item.getPreKind());
        map.put("preName", item.getPreName());
        map.put("displayName", item.getDisplayName());
        map.put("weightInputMode", item.getWeightInputMode());
        map.put("classCategoryId", item.getClassCategoryId());
        map.put("capacity", item.getCapacity());
        map.put("unit", item.getUnit());
        map.put("infUnit", item.getInfUnit());
        map.put("infVolume", item.getInfVolume());
        map.put("infDisplay", item.getInfDisplay());
        map.put("infEnergy", item.getInfEnergy());
        map.put("infProtein", item.getInfProtein());
        map.put("infFat", item.getInfFat());
        map.put("infCarbo", item.getInfCarbo());
        map.put("infSugar", item.getInfSugar());
        map.put("infSodium", item.getInfSodium());
        map.put("infLmtKind", item.getInfLmtKind());
        map.put("infLmtDateFlag", item.getInfLmtDateFlag());
        map.put("infLmtDate", item.getInfLmtDate());
        map.put("infStorageMethod", item.getInfStorageMethod());
        map.put("infContamiFlag", item.getInfContamiFlag());
        map.put("infContamination", item.getInfContamination());
        map.put("weightSum", item.getWeightSum());
        map.put("costPriceSum", item.getCostPriceSum());
        map.put("detailCount", item.getDetailCount());
        map.put("placeOfOrigin", item.getPlaceOfOrigin());
        map.put("purpose", item.getPurpose());
        map.put("isActive", item.getIsActive());
        return map;
    }

    private FoodPreProductItem fromMap(Map<String, Object> map) {
        FoodPreProductItem entity = new FoodPreProductItem();
        entity.setPreId(getInteger(map, "preId"));
        entity.setPreNo(getString(map, "preNo"));
        entity.setPreKind(getInteger(map, "preKind"));
        entity.setPreName(getString(map, "preName"));
        entity.setDisplayName(getString(map, "displayName"));
        entity.setWeightInputMode(getInteger(map, "weightInputMode"));
        entity.setClassCategoryId(getInteger(map, "classCategoryId"));
        entity.setCapacity(getFloat(map, "capacity"));
        entity.setUnit(getInteger(map, "unit"));
        entity.setInfUnit(getInteger(map, "infUnit"));
        entity.setInfVolume(getFloat(map, "infVolume"));
        entity.setInfDisplay(getInteger(map, "infDisplay"));
        entity.setInfEnergy(getFloat(map, "infEnergy"));
        entity.setInfProtein(getFloat(map, "infProtein"));
        entity.setInfFat(getFloat(map, "infFat"));
        entity.setInfCarbo(getFloat(map, "infCarbo"));
        entity.setInfSugar(getFloat(map, "infSugar"));
        entity.setInfSodium(getFloat(map, "infSodium"));
        entity.setInfLmtKind(getInteger(map, "infLmtKind"));
        entity.setInfLmtDateFlag(getBoolean(map, "infLmtDateFlag"));
        entity.setInfStorageMethod(getString(map, "infStorageMethod"));
        entity.setInfContamiFlag(getBoolean(map, "infContamiFlag"));
        entity.setInfContamination(getString(map, "infContamination"));
        entity.setPlaceOfOrigin(getString(map, "placeOfOrigin"));
        entity.setPurpose(getString(map, "purpose"));
        entity.setIsActive(getBoolean(map, "isActive"));
        return entity;
    }

    private void mergeFromMap(FoodPreProductItem entity, Map<String, Object> map) {
        if (map.containsKey("preNo")) entity.setPreNo(getString(map, "preNo"));
        if (map.containsKey("preKind")) entity.setPreKind(getInteger(map, "preKind"));
        if (map.containsKey("preName")) entity.setPreName(getString(map, "preName"));
        if (map.containsKey("displayName")) entity.setDisplayName(getString(map, "displayName"));
        if (map.containsKey("weightInputMode")) entity.setWeightInputMode(getInteger(map, "weightInputMode"));
        if (map.containsKey("classCategoryId")) entity.setClassCategoryId(getInteger(map, "classCategoryId"));
        if (map.containsKey("capacity")) entity.setCapacity(getFloat(map, "capacity"));
        if (map.containsKey("unit")) entity.setUnit(getInteger(map, "unit"));
        if (map.containsKey("infUnit")) entity.setInfUnit(getInteger(map, "infUnit"));
        if (map.containsKey("infVolume")) entity.setInfVolume(getFloat(map, "infVolume"));
        if (map.containsKey("infDisplay")) entity.setInfDisplay(getInteger(map, "infDisplay"));
        if (map.containsKey("infEnergy")) entity.setInfEnergy(getFloat(map, "infEnergy"));
        if (map.containsKey("infProtein")) entity.setInfProtein(getFloat(map, "infProtein"));
        if (map.containsKey("infFat")) entity.setInfFat(getFloat(map, "infFat"));
        if (map.containsKey("infCarbo")) entity.setInfCarbo(getFloat(map, "infCarbo"));
        if (map.containsKey("infSugar")) entity.setInfSugar(getFloat(map, "infSugar"));
        if (map.containsKey("infSodium")) entity.setInfSodium(getFloat(map, "infSodium"));
        if (map.containsKey("infLmtKind")) entity.setInfLmtKind(getInteger(map, "infLmtKind"));
        if (map.containsKey("infLmtDateFlag")) entity.setInfLmtDateFlag(getBoolean(map, "infLmtDateFlag"));
        if (map.containsKey("infStorageMethod")) entity.setInfStorageMethod(getString(map, "infStorageMethod"));
        if (map.containsKey("infContamiFlag")) entity.setInfContamiFlag(getBoolean(map, "infContamiFlag"));
        if (map.containsKey("infContamination")) entity.setInfContamination(getString(map, "infContamination"));
        if (map.containsKey("placeOfOrigin")) entity.setPlaceOfOrigin(getString(map, "placeOfOrigin"));
        if (map.containsKey("purpose")) entity.setPurpose(getString(map, "purpose"));
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
