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

import com.nines.nutsfact.domain.model.FoodPreProductDetailItem;
import com.nines.nutsfact.domain.service.FoodPreProductDetailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 仕込品明細API Controller（apix互換）
 */
@Slf4j
@RestController
@RequestMapping("/apix/FoodPreProductDetail")
@RequiredArgsConstructor
public class ApixFoodPreProductDetailController {

    private final FoodPreProductDetailService service;

    /**
     * 仕込品IDで明細一覧取得（businessAccountIdでフィルタリング）
     */
    @GetMapping("/getData")
    public ResponseEntity<Map<String, Object>> getData(
            @RequestParam("id") Integer preId) {

        List<FoodPreProductDetailItem> items = service.findByPreIdWithBusinessAccountFilter(preId);

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
     * ID指定で明細取得（businessAccountIdでフィルタリング）
     */
    @GetMapping("/findById")
    public ResponseEntity<Map<String, Object>> findById(
            @RequestParam("detailId") Integer detailId) {

        FoodPreProductDetailItem item = service.findByIdWithBusinessAccountFilter(detailId);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("item", toMap(item));
        return ResponseEntity.ok(response);
    }

    /**
     * 明細登録（businessAccountIdを自動設定）
     */
    @PostMapping("/insert")
    public ResponseEntity<Map<String, Object>> insert(
            @RequestBody Map<String, Object> request) {

        FoodPreProductDetailItem entity = fromMap(request);
        FoodPreProductDetailItem created = service.create(entity);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("item", toMap(created));
        return ResponseEntity.ok(response);
    }

    /**
     * 明細更新（businessAccountIdでフィルタリング）
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(
            @RequestBody Map<String, Object> request) {

        Integer detailId = getIntegerAny(request, "detail_id", "detailId");
        if (detailId == null) {
            throw new IllegalArgumentException("detailIdが指定されていません");
        }

        // 既存データを取得（businessAccountIdでフィルタリング）
        FoodPreProductDetailItem existing = service.findByIdWithBusinessAccountFilter(detailId);

        // リクエストの値で上書き
        mergeFromMap(existing, request);

        FoodPreProductDetailItem updated = service.updateWithBusinessAccountFilter(existing);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("item", toMap(updated));
        return ResponseEntity.ok(response);
    }

    /**
     * 明細削除（businessAccountIdでフィルタリング）
     */
    @GetMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(
            @RequestParam("detailId") Integer detailId) {

        service.deleteWithBusinessAccountFilter(detailId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("id", detailId);
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toMap(FoodPreProductDetailItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put("detail_id", item.getDetailId());
        map.put("pre_id", item.getPreId());
        map.put("component_kb", item.getComponentKb());
        map.put("detail_food_id", item.getDetailFoodId());
        map.put("detail_pre_id", item.getDetailPreId());
        map.put("composite_raw_materials_kb", item.getCompositeRawMaterialsKb());
        map.put("detail_food_name", item.getDetailFoodName());
        map.put("detail_pre_name", item.getDetailPreName());
        map.put("mixing_ratio", item.getMixingRatio());
        map.put("weight", item.getWeight());
        map.put("cost_price", item.getCostPrice());
        map.put("energy", item.getEnergy());
        map.put("protein", item.getProtein());
        map.put("fat", item.getFat());
        map.put("carbo", item.getCarbo());
        map.put("sugar", item.getSugar());
        map.put("sodium", item.getSodium());
        return map;
    }

    private FoodPreProductDetailItem fromMap(Map<String, Object> map) {
        FoodPreProductDetailItem entity = new FoodPreProductDetailItem();
        entity.setDetailId(getIntegerAny(map, "detail_id", "detailId"));
        entity.setPreId(getIntegerAny(map, "pre_id", "preId"));
        entity.setComponentKb(getBooleanAny(map, "component_kb", "componentKb"));
        entity.setDetailFoodId(getIntegerAny(map, "detail_food_id", "detailFoodId"));
        entity.setDetailPreId(getIntegerAny(map, "detail_pre_id", "detailPreId"));
        entity.setCompositeRawMaterialsKb(getBooleanAny(map, "composite_raw_materials_kb", "compositeRawMaterialsKb"));
        entity.setDetailFoodName(getStringAny(map, "detail_food_name", "detailFoodName"));
        entity.setDetailPreName(getStringAny(map, "detail_pre_name", "detailPreName"));
        entity.setMixingRatio(getFloatAny(map, "mixing_ratio", "mixingRatio"));
        entity.setWeight(getFloatAny(map, "weight", "weight"));
        entity.setCostPrice(getFloatAny(map, "cost_price", "costPrice"));
        entity.setEnergy(getFloatAny(map, "energy", "energy"));
        entity.setProtein(getFloatAny(map, "protein", "protein"));
        entity.setFat(getFloatAny(map, "fat", "fat"));
        entity.setCarbo(getFloatAny(map, "carbo", "carbo"));
        entity.setSugar(getFloatAny(map, "sugar", "sugar"));
        entity.setSodium(getFloatAny(map, "sodium", "sodium"));
        return entity;
    }

    private void mergeFromMap(FoodPreProductDetailItem entity, Map<String, Object> map) {
        if (hasKey(map, "pre_id", "preId")) entity.setPreId(getIntegerAny(map, "pre_id", "preId"));
        if (hasKey(map, "component_kb", "componentKb")) entity.setComponentKb(getBooleanAny(map, "component_kb", "componentKb"));
        if (hasKey(map, "detail_food_id", "detailFoodId")) entity.setDetailFoodId(getIntegerAny(map, "detail_food_id", "detailFoodId"));
        if (hasKey(map, "detail_pre_id", "detailPreId")) entity.setDetailPreId(getIntegerAny(map, "detail_pre_id", "detailPreId"));
        if (hasKey(map, "composite_raw_materials_kb", "compositeRawMaterialsKb")) entity.setCompositeRawMaterialsKb(getBooleanAny(map, "composite_raw_materials_kb", "compositeRawMaterialsKb"));
        if (hasKey(map, "detail_food_name", "detailFoodName")) entity.setDetailFoodName(getStringAny(map, "detail_food_name", "detailFoodName"));
        if (hasKey(map, "detail_pre_name", "detailPreName")) entity.setDetailPreName(getStringAny(map, "detail_pre_name", "detailPreName"));
        if (hasKey(map, "mixing_ratio", "mixingRatio")) entity.setMixingRatio(getFloatAny(map, "mixing_ratio", "mixingRatio"));
        if (hasKey(map, "weight", "weight")) entity.setWeight(getFloatAny(map, "weight", "weight"));
        if (hasKey(map, "cost_price", "costPrice")) entity.setCostPrice(getFloatAny(map, "cost_price", "costPrice"));
        if (hasKey(map, "energy", "energy")) entity.setEnergy(getFloatAny(map, "energy", "energy"));
        if (hasKey(map, "protein", "protein")) entity.setProtein(getFloatAny(map, "protein", "protein"));
        if (hasKey(map, "fat", "fat")) entity.setFat(getFloatAny(map, "fat", "fat"));
        if (hasKey(map, "carbo", "carbo")) entity.setCarbo(getFloatAny(map, "carbo", "carbo"));
        if (hasKey(map, "sugar", "sugar")) entity.setSugar(getFloatAny(map, "sugar", "sugar"));
        if (hasKey(map, "sodium", "sodium")) entity.setSodium(getFloatAny(map, "sodium", "sodium"));
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
