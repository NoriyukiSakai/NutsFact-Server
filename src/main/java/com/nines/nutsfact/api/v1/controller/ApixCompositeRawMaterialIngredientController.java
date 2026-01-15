package com.nines.nutsfact.api.v1.controller;

import java.util.ArrayList;
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

import com.nines.nutsfact.domain.model.CompositeRawMaterialIngredient;
import com.nines.nutsfact.domain.service.CompositeRawMaterialIngredientService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 複合原材料使用原材料API Controller（apix互換）
 */
@Slf4j
@RestController
@RequestMapping("/apix/CompositeRawMaterialIngredient")
@RequiredArgsConstructor
public class ApixCompositeRawMaterialIngredientController {

    private final CompositeRawMaterialIngredientService service;

    /**
     * 原材料IDで使用原材料一覧取得
     * businessAccountIdでフィルタリング
     */
    @GetMapping("/findByFoodId")
    public ResponseEntity<Map<String, Object>> findByFoodId(
            @RequestParam("foodId") Integer foodId) {

        List<CompositeRawMaterialIngredient> items = service.findByFoodIdWithBusinessAccountFilter(foodId);

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
     * ID指定で使用原材料取得
     * businessAccountIdでフィルタリング
     */
    @GetMapping("/findById")
    public ResponseEntity<Map<String, Object>> findById(
            @RequestParam("id") Integer id) {

        CompositeRawMaterialIngredient item = service.findByIdWithBusinessAccountFilter(id);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("item", toMap(item));
        return ResponseEntity.ok(response);
    }

    /**
     * 使用原材料登録
     */
    @PostMapping("/insert")
    public ResponseEntity<Map<String, Object>> insert(
            @RequestBody Map<String, Object> request) {

        CompositeRawMaterialIngredient entity = fromMap(request);
        CompositeRawMaterialIngredient created = service.create(entity);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("item", toMap(created));
        return ResponseEntity.ok(response);
    }

    /**
     * 使用原材料更新
     * businessAccountIdでフィルタリング
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(
            @RequestBody Map<String, Object> request) {

        Integer id = getIntegerAny(request, "id", "id");
        if (id == null) {
            throw new IllegalArgumentException("idが指定されていません");
        }

        // 既存データを取得（businessAccountIdでフィルタリング）
        CompositeRawMaterialIngredient existing = service.findByIdWithBusinessAccountFilter(id);

        // リクエストの値で上書き
        mergeFromMap(existing, request);

        CompositeRawMaterialIngredient updated = service.updateWithBusinessAccountFilter(existing);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("item", toMap(updated));
        return ResponseEntity.ok(response);
    }

    /**
     * 使用原材料削除
     * businessAccountIdでフィルタリング
     */
    @GetMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(
            @RequestParam("id") Integer id) {

        service.deleteWithBusinessAccountFilter(id);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("id", id);
        return ResponseEntity.ok(response);
    }

    /**
     * 使用原材料一括保存
     * 既存データを削除してから新規登録
     * businessAccountIdでフィルタリング
     */
    @PostMapping("/saveAll")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> saveAll(
            @RequestBody Map<String, Object> request) {

        Integer foodId = getIntegerAny(request, "food_id", "foodId");
        if (foodId == null) {
            throw new IllegalArgumentException("foodIdが指定されていません");
        }

        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) request.get("items");
        List<CompositeRawMaterialIngredient> ingredients = new ArrayList<>();

        if (itemsData != null) {
            for (Map<String, Object> itemData : itemsData) {
                ingredients.add(fromMap(itemData));
            }
        }

        service.saveAll(foodId, ingredients);

        // 保存後のデータを取得
        List<CompositeRawMaterialIngredient> savedItems = service.findByFoodIdWithBusinessAccountFilter(foodId);

        List<Map<String, Object>> itemList = savedItems.stream()
            .map(this::toMap)
            .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("records", itemList.size());
        response.put("item", itemList);
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toMap(CompositeRawMaterialIngredient item) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", item.getId());
        map.put("business_account_id", item.getBusinessAccountId());
        map.put("food_id", item.getFoodId());
        map.put("display_order", item.getDisplayOrder());
        map.put("ingredient_food_id", item.getIngredientFoodId());
        map.put("ingredient_name", item.getIngredientName());
        map.put("ratio", item.getRatio());
        map.put("is_active", item.getIsActive());
        map.put("create_date", item.getCreateDate());
        map.put("last_update_date", item.getLastUpdateDate());
        return map;
    }

    private CompositeRawMaterialIngredient fromMap(Map<String, Object> map) {
        CompositeRawMaterialIngredient entity = new CompositeRawMaterialIngredient();
        entity.setId(getIntegerAny(map, "id", "id"));
        entity.setFoodId(getIntegerAny(map, "food_id", "foodId"));
        entity.setDisplayOrder(getIntegerAny(map, "display_order", "displayOrder"));
        entity.setIngredientFoodId(getIntegerAny(map, "ingredient_food_id", "ingredientFoodId"));
        entity.setIngredientName(getStringAny(map, "ingredient_name", "ingredientName"));
        entity.setRatio(getDoubleAny(map, "ratio", "ratio"));
        entity.setIsActive(getBooleanAny(map, "is_active", "isActive"));
        return entity;
    }

    private void mergeFromMap(CompositeRawMaterialIngredient entity, Map<String, Object> map) {
        if (hasKey(map, "food_id", "foodId")) entity.setFoodId(getIntegerAny(map, "food_id", "foodId"));
        if (hasKey(map, "display_order", "displayOrder")) entity.setDisplayOrder(getIntegerAny(map, "display_order", "displayOrder"));
        if (hasKey(map, "ingredient_food_id", "ingredientFoodId")) entity.setIngredientFoodId(getIntegerAny(map, "ingredient_food_id", "ingredientFoodId"));
        if (hasKey(map, "ingredient_name", "ingredientName")) entity.setIngredientName(getStringAny(map, "ingredient_name", "ingredientName"));
        if (hasKey(map, "ratio", "ratio")) entity.setRatio(getDoubleAny(map, "ratio", "ratio"));
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

    private Double getDoubleAny(Map<String, Object> map, String snakeCase, String camelCase) {
        Double val = getDouble(map, snakeCase);
        return val != null ? val : getDouble(map, camelCase);
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

    private Double getDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}
