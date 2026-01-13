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
     * 仕込品IDで明細一覧取得
     */
    @GetMapping("/getData")
    public ResponseEntity<Map<String, Object>> getData(
            @RequestParam("id") Integer preId) {

        List<FoodPreProductDetailItem> items = service.findByPreId(preId);

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
     * ID指定で明細取得
     */
    @GetMapping("/findById")
    public ResponseEntity<Map<String, Object>> findById(
            @RequestParam("detailId") Integer detailId) {

        FoodPreProductDetailItem item = service.findById(detailId);
        Map<String, Object> response = toMap(item);
        response.put("status", "Success");
        return ResponseEntity.ok(response);
    }

    /**
     * 明細登録
     */
    @PostMapping("/insert")
    public ResponseEntity<Map<String, Object>> insert(
            @RequestBody Map<String, Object> request) {

        FoodPreProductDetailItem entity = fromMap(request);
        FoodPreProductDetailItem created = service.create(entity);

        Map<String, Object> response = toMap(created);
        response.put("status", "Success");
        return ResponseEntity.ok(response);
    }

    /**
     * 明細更新
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(
            @RequestBody Map<String, Object> request) {

        Integer detailId = getInteger(request, "detailId");
        if (detailId == null) {
            throw new IllegalArgumentException("detailIdが指定されていません");
        }

        // 既存データを取得
        FoodPreProductDetailItem existing = service.findById(detailId);

        // リクエストの値で上書き
        mergeFromMap(existing, request);

        FoodPreProductDetailItem updated = service.update(existing);

        Map<String, Object> response = toMap(updated);
        response.put("status", "Success");
        return ResponseEntity.ok(response);
    }

    /**
     * 明細削除
     */
    @GetMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(
            @RequestParam("detailId") Integer detailId) {

        service.delete(detailId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("id", detailId);
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toMap(FoodPreProductDetailItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put("detailId", item.getDetailId());
        map.put("preId", item.getPreId());
        map.put("componentKb", item.getComponentKb());
        map.put("detailFoodId", item.getDetailFoodId());
        map.put("detailPreId", item.getDetailPreId());
        map.put("compositeRawMaterialsKb", item.getCompositeRawMaterialsKb());
        map.put("detailFoodName", item.getDetailFoodName());
        map.put("detailPreName", item.getDetailPreName());
        map.put("mixingRatio", item.getMixingRatio());
        map.put("weight", item.getWeight());
        map.put("costPrice", item.getCostPrice());
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
        entity.setDetailId(getInteger(map, "detailId"));
        entity.setPreId(getInteger(map, "preId"));
        entity.setComponentKb(getBoolean(map, "componentKb"));
        entity.setDetailFoodId(getInteger(map, "detailFoodId"));
        entity.setDetailPreId(getInteger(map, "detailPreId"));
        entity.setCompositeRawMaterialsKb(getBoolean(map, "compositeRawMaterialsKb"));
        entity.setDetailFoodName(getString(map, "detailFoodName"));
        entity.setDetailPreName(getString(map, "detailPreName"));
        entity.setMixingRatio(getFloat(map, "mixingRatio"));
        entity.setWeight(getFloat(map, "weight"));
        entity.setCostPrice(getFloat(map, "costPrice"));
        entity.setEnergy(getFloat(map, "energy"));
        entity.setProtein(getFloat(map, "protein"));
        entity.setFat(getFloat(map, "fat"));
        entity.setCarbo(getFloat(map, "carbo"));
        entity.setSugar(getFloat(map, "sugar"));
        entity.setSodium(getFloat(map, "sodium"));
        return entity;
    }

    private void mergeFromMap(FoodPreProductDetailItem entity, Map<String, Object> map) {
        if (map.containsKey("preId")) entity.setPreId(getInteger(map, "preId"));
        if (map.containsKey("componentKb")) entity.setComponentKb(getBoolean(map, "componentKb"));
        if (map.containsKey("detailFoodId")) entity.setDetailFoodId(getInteger(map, "detailFoodId"));
        if (map.containsKey("detailPreId")) entity.setDetailPreId(getInteger(map, "detailPreId"));
        if (map.containsKey("compositeRawMaterialsKb")) entity.setCompositeRawMaterialsKb(getBoolean(map, "compositeRawMaterialsKb"));
        if (map.containsKey("detailFoodName")) entity.setDetailFoodName(getString(map, "detailFoodName"));
        if (map.containsKey("detailPreName")) entity.setDetailPreName(getString(map, "detailPreName"));
        if (map.containsKey("mixingRatio")) entity.setMixingRatio(getFloat(map, "mixingRatio"));
        if (map.containsKey("weight")) entity.setWeight(getFloat(map, "weight"));
        if (map.containsKey("costPrice")) entity.setCostPrice(getFloat(map, "costPrice"));
        if (map.containsKey("energy")) entity.setEnergy(getFloat(map, "energy"));
        if (map.containsKey("protein")) entity.setProtein(getFloat(map, "protein"));
        if (map.containsKey("fat")) entity.setFat(getFloat(map, "fat"));
        if (map.containsKey("carbo")) entity.setCarbo(getFloat(map, "carbo"));
        if (map.containsKey("sugar")) entity.setSugar(getFloat(map, "sugar"));
        if (map.containsKey("sodium")) entity.setSodium(getFloat(map, "sodium"));
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
