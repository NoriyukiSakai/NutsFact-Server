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
     * 仕込品一覧取得（区分指定、businessAccountIdでフィルタリング）
     */
    @GetMapping("/getData")
    public ResponseEntity<Map<String, Object>> getData(
            @RequestParam(value = "preKind", required = false) Integer preKind) {
        List<FoodPreProductItem> items;
        if (preKind != null) {
            items = service.findByKindWithBusinessAccountFilter(preKind);
        } else {
            items = service.findAllWithBusinessAccountFilter();
        }

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
     * ID指定で仕込品取得（businessAccountIdでフィルタリング）
     */
    @GetMapping("/findById")
    public ResponseEntity<Map<String, Object>> findById(
            @RequestParam("preId") Integer preId) {

        FoodPreProductItem item = service.findByIdWithBusinessAccountFilter(preId);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("item", toMap(item));
        return ResponseEntity.ok(response);
    }

    /**
     * 仕込品登録（businessAccountIdを自動設定）
     */
    @PostMapping("/insert")
    public ResponseEntity<Map<String, Object>> insert(
            @RequestBody Map<String, Object> request) {

        FoodPreProductItem entity = fromMap(request);
        FoodPreProductItem created = service.create(entity);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("item", toMap(created));
        return ResponseEntity.ok(response);
    }

    /**
     * 仕込品更新（businessAccountIdでフィルタリング）
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(
            @RequestBody Map<String, Object> request) {

        Integer preId = getIntegerAny(request, "pre_id", "preId");
        if (preId == null) {
            throw new IllegalArgumentException("preIdが指定されていません");
        }

        // 既存データを取得（businessAccountIdでフィルタリング）
        FoodPreProductItem existing = service.findByIdWithBusinessAccountFilter(preId);

        // リクエストの値で上書き
        mergeFromMap(existing, request);

        FoodPreProductItem updated = service.updateWithBusinessAccountFilter(existing);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("item", toMap(updated));
        return ResponseEntity.ok(response);
    }

    /**
     * 仕込品削除（businessAccountIdでフィルタリング）
     */
    @GetMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(
            @RequestParam("preId") Integer preId) {

        service.deleteWithBusinessAccountFilter(preId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("id", preId);
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toMap(FoodPreProductItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put("pre_id", item.getPreId());
        map.put("pre_no", item.getPreNo());
        map.put("pre_kind", item.getPreKind());
        map.put("pre_name", item.getPreName());
        map.put("display_name", item.getDisplayName());
        map.put("weight_input_mode", item.getWeightInputMode());
        map.put("class_category_id", item.getClassCategoryId());
        map.put("capacity", item.getCapacity());
        map.put("unit", item.getUnit());
        map.put("inf_unit", item.getInfUnit());
        map.put("inf_volume", item.getInfVolume());
        map.put("inf_display", item.getInfDisplay());
        map.put("inf_energy", item.getInfEnergy());
        map.put("inf_protein", item.getInfProtein());
        map.put("inf_fat", item.getInfFat());
        map.put("inf_carbo", item.getInfCarbo());
        map.put("inf_sugar", item.getInfSugar());
        map.put("inf_sodium", item.getInfSodium());
        map.put("inf_lmt_kind", item.getInfLmtKind());
        map.put("inf_lmt_date_flag", item.getInfLmtDateFlag());
        map.put("inf_lmt_date", item.getInfLmtDate());
        map.put("inf_storage_method", item.getInfStorageMethod());
        map.put("inf_contami_flag", item.getInfContamiFlag());
        map.put("inf_contamination", item.getInfContamination());
        map.put("weight_sum", item.getWeightSum());
        map.put("cost_price_sum", item.getCostPriceSum());
        map.put("detail_count", item.getDetailCount());
        map.put("place_of_origin", item.getPlaceOfOrigin());
        map.put("purpose", item.getPurpose());
        map.put("is_active", item.getIsActive());
        return map;
    }

    private FoodPreProductItem fromMap(Map<String, Object> map) {
        FoodPreProductItem entity = new FoodPreProductItem();
        entity.setPreId(getIntegerAny(map, "pre_id", "preId"));
        entity.setPreNo(getStringAny(map, "pre_no", "preNo"));
        entity.setPreKind(getIntegerAny(map, "pre_kind", "preKind"));
        entity.setPreName(getStringAny(map, "pre_name", "preName"));
        entity.setDisplayName(getStringAny(map, "display_name", "displayName"));
        entity.setWeightInputMode(getIntegerAny(map, "weight_input_mode", "weightInputMode"));
        entity.setClassCategoryId(getIntegerAny(map, "class_category_id", "classCategoryId"));
        entity.setCapacity(getFloatAny(map, "capacity", "capacity"));
        entity.setUnit(getIntegerAny(map, "unit", "unit"));
        entity.setInfUnit(getIntegerAny(map, "inf_unit", "infUnit"));
        entity.setInfVolume(getFloatAny(map, "inf_volume", "infVolume"));
        entity.setInfDisplay(getIntegerAny(map, "inf_display", "infDisplay"));
        entity.setInfEnergy(getFloatAny(map, "inf_energy", "infEnergy"));
        entity.setInfProtein(getFloatAny(map, "inf_protein", "infProtein"));
        entity.setInfFat(getFloatAny(map, "inf_fat", "infFat"));
        entity.setInfCarbo(getFloatAny(map, "inf_carbo", "infCarbo"));
        entity.setInfSugar(getFloatAny(map, "inf_sugar", "infSugar"));
        entity.setInfSodium(getFloatAny(map, "inf_sodium", "infSodium"));
        entity.setInfLmtKind(getIntegerAny(map, "inf_lmt_kind", "infLmtKind"));
        entity.setInfLmtDateFlag(getBooleanAny(map, "inf_lmt_date_flag", "infLmtDateFlag"));
        entity.setInfStorageMethod(getStringAny(map, "inf_storage_method", "infStorageMethod"));
        entity.setInfContamiFlag(getBooleanAny(map, "inf_contami_flag", "infContamiFlag"));
        entity.setInfContamination(getStringAny(map, "inf_contamination", "infContamination"));
        entity.setPlaceOfOrigin(getStringAny(map, "place_of_origin", "placeOfOrigin"));
        entity.setPurpose(getStringAny(map, "purpose", "purpose"));
        entity.setIsActive(getBooleanAny(map, "is_active", "isActive"));
        return entity;
    }

    private void mergeFromMap(FoodPreProductItem entity, Map<String, Object> map) {
        if (hasKey(map, "pre_no", "preNo")) entity.setPreNo(getStringAny(map, "pre_no", "preNo"));
        if (hasKey(map, "pre_kind", "preKind")) entity.setPreKind(getIntegerAny(map, "pre_kind", "preKind"));
        if (hasKey(map, "pre_name", "preName")) entity.setPreName(getStringAny(map, "pre_name", "preName"));
        if (hasKey(map, "display_name", "displayName")) entity.setDisplayName(getStringAny(map, "display_name", "displayName"));
        if (hasKey(map, "weight_input_mode", "weightInputMode")) entity.setWeightInputMode(getIntegerAny(map, "weight_input_mode", "weightInputMode"));
        if (hasKey(map, "class_category_id", "classCategoryId")) entity.setClassCategoryId(getIntegerAny(map, "class_category_id", "classCategoryId"));
        if (hasKey(map, "capacity", "capacity")) entity.setCapacity(getFloatAny(map, "capacity", "capacity"));
        if (hasKey(map, "unit", "unit")) entity.setUnit(getIntegerAny(map, "unit", "unit"));
        if (hasKey(map, "inf_unit", "infUnit")) entity.setInfUnit(getIntegerAny(map, "inf_unit", "infUnit"));
        if (hasKey(map, "inf_volume", "infVolume")) entity.setInfVolume(getFloatAny(map, "inf_volume", "infVolume"));
        if (hasKey(map, "inf_display", "infDisplay")) entity.setInfDisplay(getIntegerAny(map, "inf_display", "infDisplay"));
        if (hasKey(map, "inf_energy", "infEnergy")) entity.setInfEnergy(getFloatAny(map, "inf_energy", "infEnergy"));
        if (hasKey(map, "inf_protein", "infProtein")) entity.setInfProtein(getFloatAny(map, "inf_protein", "infProtein"));
        if (hasKey(map, "inf_fat", "infFat")) entity.setInfFat(getFloatAny(map, "inf_fat", "infFat"));
        if (hasKey(map, "inf_carbo", "infCarbo")) entity.setInfCarbo(getFloatAny(map, "inf_carbo", "infCarbo"));
        if (hasKey(map, "inf_sugar", "infSugar")) entity.setInfSugar(getFloatAny(map, "inf_sugar", "infSugar"));
        if (hasKey(map, "inf_sodium", "infSodium")) entity.setInfSodium(getFloatAny(map, "inf_sodium", "infSodium"));
        if (hasKey(map, "inf_lmt_kind", "infLmtKind")) entity.setInfLmtKind(getIntegerAny(map, "inf_lmt_kind", "infLmtKind"));
        if (hasKey(map, "inf_lmt_date_flag", "infLmtDateFlag")) entity.setInfLmtDateFlag(getBooleanAny(map, "inf_lmt_date_flag", "infLmtDateFlag"));
        if (hasKey(map, "inf_storage_method", "infStorageMethod")) entity.setInfStorageMethod(getStringAny(map, "inf_storage_method", "infStorageMethod"));
        if (hasKey(map, "inf_contami_flag", "infContamiFlag")) entity.setInfContamiFlag(getBooleanAny(map, "inf_contami_flag", "infContamiFlag"));
        if (hasKey(map, "inf_contamination", "infContamination")) entity.setInfContamination(getStringAny(map, "inf_contamination", "infContamination"));
        if (hasKey(map, "place_of_origin", "placeOfOrigin")) entity.setPlaceOfOrigin(getStringAny(map, "place_of_origin", "placeOfOrigin"));
        if (hasKey(map, "purpose", "purpose")) entity.setPurpose(getStringAny(map, "purpose", "purpose"));
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
