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

import com.nines.nutsfact.domain.model.allergy.AllergenicControl;
import com.nines.nutsfact.domain.service.AllergenicControlService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * アレルギー情報API Controller（apix互換）
 */
@Slf4j
@RestController
@RequestMapping("/apix/AllergenicControl")
@RequiredArgsConstructor
public class ApixAllergenicControlController {

    private final AllergenicControlService service;

    /**
     * アレルギー情報一覧取得
     */
    @GetMapping("/getData")
    public ResponseEntity<Map<String, Object>> getData() {
        List<AllergenicControl> items = service.findAll();

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
     * foodId指定でアレルギー情報取得
     */
    @GetMapping("/findById")
    public ResponseEntity<Map<String, Object>> findById(
            @RequestParam("foodId") Integer foodId) {

        AllergenicControl item = service.findByFoodId(foodId);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        if (item != null) {
            response.put("item", toMap(item));
        }
        return ResponseEntity.ok(response);
    }

    /**
     * アレルギー情報登録・更新
     */
    @PostMapping("/upsert")
    public ResponseEntity<Map<String, Object>> upsert(
            @RequestBody Map<String, Object> request) {

        AllergenicControl entity = fromMap(request);
        AllergenicControl saved = service.save(entity);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("item", toMap(saved));
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toMap(AllergenicControl item) {
        Map<String, Object> map = new HashMap<>();
        map.put("food_id", item.getFoodId());
        map.put("item1_val", item.getItem1Val());
        map.put("item2_val", item.getItem2Val());
        map.put("item3_val", item.getItem3Val());
        map.put("item4_val", item.getItem4Val());
        map.put("item5_val", item.getItem5Val());
        map.put("item6_val", item.getItem6Val());
        map.put("item7_val", item.getItem7Val());
        map.put("item8_val", item.getItem8Val());
        map.put("item9_val", item.getItem9Val());
        map.put("item10_val", item.getItem10Val());
        map.put("item11_val", item.getItem11Val());
        map.put("item12_val", item.getItem12Val());
        map.put("item13_val", item.getItem13Val());
        map.put("item14_val", item.getItem14Val());
        map.put("item15_val", item.getItem15Val());
        map.put("item16_val", item.getItem16Val());
        map.put("item17_val", item.getItem17Val());
        map.put("item18_val", item.getItem18Val());
        map.put("item19_val", item.getItem19Val());
        map.put("item20_val", item.getItem20Val());
        map.put("item21_val", item.getItem21Val());
        map.put("item22_val", item.getItem22Val());
        map.put("item23_val", item.getItem23Val());
        map.put("item24_val", item.getItem24Val());
        map.put("item25_val", item.getItem25Val());
        map.put("item26_val", item.getItem26Val());
        map.put("item27_val", item.getItem27Val());
        map.put("item28_val", item.getItem28Val());
        map.put("item29_val", item.getItem29Val());
        map.put("item30_val", item.getItem30Val());
        return map;
    }

    private AllergenicControl fromMap(Map<String, Object> map) {
        AllergenicControl entity = new AllergenicControl();
        entity.setFoodId(getInteger(map, "food_id"));
        entity.setItem1Val(getBoolean(map, "item1_val"));
        entity.setItem2Val(getBoolean(map, "item2_val"));
        entity.setItem3Val(getBoolean(map, "item3_val"));
        entity.setItem4Val(getBoolean(map, "item4_val"));
        entity.setItem5Val(getBoolean(map, "item5_val"));
        entity.setItem6Val(getBoolean(map, "item6_val"));
        entity.setItem7Val(getBoolean(map, "item7_val"));
        entity.setItem8Val(getBoolean(map, "item8_val"));
        entity.setItem9Val(getBoolean(map, "item9_val"));
        entity.setItem10Val(getBoolean(map, "item10_val"));
        entity.setItem11Val(getBoolean(map, "item11_val"));
        entity.setItem12Val(getBoolean(map, "item12_val"));
        entity.setItem13Val(getBoolean(map, "item13_val"));
        entity.setItem14Val(getBoolean(map, "item14_val"));
        entity.setItem15Val(getBoolean(map, "item15_val"));
        entity.setItem16Val(getBoolean(map, "item16_val"));
        entity.setItem17Val(getBoolean(map, "item17_val"));
        entity.setItem18Val(getBoolean(map, "item18_val"));
        entity.setItem19Val(getBoolean(map, "item19_val"));
        entity.setItem20Val(getBoolean(map, "item20_val"));
        entity.setItem21Val(getBoolean(map, "item21_val"));
        entity.setItem22Val(getBoolean(map, "item22_val"));
        entity.setItem23Val(getBoolean(map, "item23_val"));
        entity.setItem24Val(getBoolean(map, "item24_val"));
        entity.setItem25Val(getBoolean(map, "item25_val"));
        entity.setItem26Val(getBoolean(map, "item26_val"));
        entity.setItem27Val(getBoolean(map, "item27_val"));
        entity.setItem28Val(getBoolean(map, "item28_val"));
        entity.setItem29Val(getBoolean(map, "item29_val"));
        entity.setItem30Val(getBoolean(map, "item30_val"));
        return entity;
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.parseInt(value.toString());
    }

    private Boolean getBoolean(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }
}
