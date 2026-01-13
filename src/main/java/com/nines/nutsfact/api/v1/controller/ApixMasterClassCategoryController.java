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

import com.nines.nutsfact.domain.model.master.ClassCategory;
import com.nines.nutsfact.domain.service.ClassCategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * クラス分類API Controller（apix互換）
 */
@Slf4j
@RestController
@RequestMapping("/apix/MasterClassCategory")
@RequiredArgsConstructor
public class ApixMasterClassCategoryController {

    private final ClassCategoryService service;

    /**
     * クラス分類一覧取得
     */
    @GetMapping("/getData")
    public ResponseEntity<Map<String, Object>> getData(
            @RequestParam(value = "classType", required = false) Integer classType) {

        List<ClassCategory> items;
        if (classType != null) {
            items = service.findByType(classType);
        } else {
            items = service.findAll();
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
     * ID指定でクラス分類取得
     */
    @GetMapping("/findById")
    public ResponseEntity<Map<String, Object>> findById(
            @RequestParam("classCategoryId") Integer classCategoryId) {

        ClassCategory item = service.findById(classCategoryId);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("item", toMap(item));
        return ResponseEntity.ok(response);
    }

    /**
     * クラス分類登録
     */
    @PostMapping("/insert")
    public ResponseEntity<Map<String, Object>> insert(
            @RequestBody Map<String, Object> request) {

        ClassCategory entity = fromMap(request);
        ClassCategory created = service.create(entity);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("item", toMap(created));
        return ResponseEntity.ok(response);
    }

    /**
     * クラス分類更新
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(
            @RequestBody Map<String, Object> request) {

        Integer classCategoryId = getInteger(request, "class_category_id");
        if (classCategoryId == null) {
            throw new IllegalArgumentException("class_category_idが指定されていません");
        }

        ClassCategory entity = fromMap(request);
        ClassCategory updated = service.update(classCategoryId, entity);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("item", toMap(updated));
        return ResponseEntity.ok(response);
    }

    /**
     * クラス分類削除
     */
    @GetMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(
            @RequestParam("classCategoryId") Integer classCategoryId) {

        service.delete(classCategoryId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("id", classCategoryId);
        return ResponseEntity.ok(response);
    }

    /**
     * クラス分類選択肢取得
     */
    @GetMapping("/getSelect")
    public ResponseEntity<Map<String, Object>> getSelect(
            @RequestParam("classType") Integer classType) {

        List<ClassCategory> items = service.findByType(classType);

        List<Map<String, Object>> itemList = items.stream()
            .map(item -> {
                Map<String, Object> map = new HashMap<>();
                map.put("value", item.getCategoryId());
                map.put("label", item.getCategoryName());
                return map;
            })
            .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("records", itemList.size());
        response.put("item", itemList);
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toMap(ClassCategory item) {
        Map<String, Object> map = new HashMap<>();
        map.put("class_category_id", item.getCategoryId());
        map.put("class_type", item.getCategoryType());
        map.put("class_category_name", item.getCategoryName());
        map.put("description", item.getDescription());
        map.put("is_active", item.getIsActive() != null ? item.getIsActive() : true);
        return map;
    }

    private ClassCategory fromMap(Map<String, Object> map) {
        ClassCategory entity = new ClassCategory();
        entity.setCategoryId(getInteger(map, "class_category_id"));
        entity.setCategoryType(getInteger(map, "class_type"));
        entity.setCategoryName(getString(map, "class_category_name"));
        entity.setDescription(getString(map, "description"));
        entity.setIsActive(getBoolean(map, "is_active"));
        return entity;
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
}
