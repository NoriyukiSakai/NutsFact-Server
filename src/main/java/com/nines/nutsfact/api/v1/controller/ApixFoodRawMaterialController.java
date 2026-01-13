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

import com.nines.nutsfact.domain.model.FoodRawMaterial;
import com.nines.nutsfact.domain.service.FoodRawMaterialService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 原材料API Controller（apix互換）
 * クライアント互換のための旧形式エンドポイント
 */
@Slf4j
@RestController
@RequestMapping("/apix/FoodRawMaterial")
@RequiredArgsConstructor
public class ApixFoodRawMaterialController {

    private final FoodRawMaterialService service;

    /**
     * カテゴリ別原材料一覧取得
     * patternId: 1=8訂, 2=公開, 3=登録済
     */
    @GetMapping("/getDataX")
    public ResponseEntity<Map<String, Object>> getDataX(
            @RequestParam(value = "patternId", required = false) Integer patternId) {

        List<FoodRawMaterial> items;
        if (patternId != null) {
            items = service.findByCategory(patternId);
        } else {
            items = service.findAll();
        }

        List<Map<String, Object>> flatItems = items.stream()
            .map(this::toFlatMap)
            .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("records", flatItems.size());
        response.put("item", flatItems);
        return ResponseEntity.ok(response);
    }

    /**
     * ID指定で原材料取得
     */
    @GetMapping("/findById")
    public ResponseEntity<Map<String, Object>> findById(
            @RequestParam("foodId") Integer foodId) {

        FoodRawMaterial item = service.findById(foodId);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("item", toFlatMap(item));
        return ResponseEntity.ok(response);
    }

    /**
     * 食品番号指定で原材料取得
     */
    @GetMapping("/findByFoodNo")
    public ResponseEntity<Map<String, Object>> findByFoodNo(
            @RequestParam("foodNo") String foodNo) {

        FoodRawMaterial item = service.findByFoodNo(foodNo);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("item", toFlatMap(item));
        return ResponseEntity.ok(response);
    }

    /**
     * 原材料登録
     */
    @PostMapping("/insert")
    public ResponseEntity<Map<String, Object>> insert(
            @RequestBody Map<String, Object> request) {

        FoodRawMaterial entity = fromFlatMap(request);
        FoodRawMaterial created = service.create(entity);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("item", toFlatMap(created));
        return ResponseEntity.ok(response);
    }

    /**
     * 原材料更新
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(
            @RequestBody Map<String, Object> request) {

        // snake_case または camelCase どちらでも対応
        Integer foodId = getInteger(request, "food_id");
        if (foodId == null) {
            foodId = getInteger(request, "foodId");
        }
        if (foodId == null) {
            throw new IllegalArgumentException("food_idが指定されていません");
        }

        // 既存データを取得
        FoodRawMaterial existing = service.findById(foodId);

        // リクエストの値で上書き（nullでないフィールドのみ）
        mergeFromFlatMap(existing, request);

        FoodRawMaterial updated = service.update(existing);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("item", toFlatMap(updated));
        return ResponseEntity.ok(response);
    }

    /**
     * 原材料削除
     */
    @GetMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(
            @RequestParam("foodId") Integer foodId) {

        service.delete(foodId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("id", foodId);
        return ResponseEntity.ok(response);
    }

    /**
     * FoodRawMaterialをフラットなMapに変換（snake_case）
     */
    private Map<String, Object> toFlatMap(FoodRawMaterial item) {
        Map<String, Object> map = new HashMap<>();

        // 基本情報
        map.put("food_id", item.getFoodId());
        map.put("food_no", item.getFoodNo());
        map.put("food_group_id", item.getFoodGroupId());
        map.put("index_no", item.getIndexNo());
        map.put("class_category_id", item.getClassCategoryId());
        map.put("original_food_id", item.getOriginalFoodId());
        map.put("original_food_group_id", item.getOriginalFoodGroupId());
        map.put("original_food_no", item.getOriginalFoodNo());
        map.put("original_index_no", item.getOriginalIndexNo());
        map.put("original_food_name", item.getOriginalFoodName());
        map.put("food_name", item.getFoodName());
        map.put("food_fuku_bunrui", item.getFoodFukuBunrui());
        map.put("food_rui_kubun", item.getFoodRuiKubun());
        map.put("food_dai_bunrui", item.getFoodDaiBunrui());
        map.put("food_cyu_bunrui", item.getFoodCyuBunrui());
        map.put("food_syo_bunrui", item.getFoodSyoBunrui());
        map.put("food_saibun", item.getFoodSaibun());
        map.put("category_id", item.getCategoryId());
        map.put("hashtag", item.getHashtag());
        map.put("composite_raw_materials_kb", item.getCompositeRawMaterialsKb());
        map.put("price_per_unit", item.getPricePerUnit());
        map.put("maker_id", item.getMakerId());
        map.put("maker_name", item.getMakerName());
        map.put("saller_id", item.getSellerId());
        map.put("saller_name", item.getSellerName());
        map.put("composite_raw_itemlist", item.getCompositeRawItemlist());
        map.put("display_name", item.getDisplayName());
        map.put("place_of_origin", item.getPlaceOfOrigin());
        map.put("display_place_of_origin", item.getDisplayPlaceOfOrigin());
        map.put("revision_of_food_no", item.getRevisionOfFoodNo());
        map.put("next_food_id", item.getNextFoodId());
        map.put("expire_date", item.getExpireDate());
        map.put("create_date", item.getCreateDate());
        map.put("last_update_date", item.getLastUpdateDate());
        map.put("status", item.getStatus());
        map.put("update_information", item.getUpdateInformation());
        map.put("description", item.getDescription());
        map.put("is_active", item.getIsActive());

        // 基本栄養成分
        if (item.getBasicNutrition() != null) {
            var bn = item.getBasicNutrition();
            map.put("refuse", bn.getRefuse());
            map.put("enerc", bn.getEnerc());
            map.put("enerc_kcal", bn.getEnercKcal());
            map.put("water", bn.getWater());
            map.put("prot", bn.getProt());
            map.put("protcaa", bn.getProtcaa());
            map.put("fat", bn.getFat());
            map.put("chole", bn.getChole());
            map.put("chocdf", bn.getChocdf());
            map.put("choavlm", bn.getChoavlm());
            map.put("choavl", bn.getChoavl());
            map.put("choavldf", bn.getChoavldf());
            map.put("fib", bn.getFib());
            map.put("polyl", bn.getPolyl());
            map.put("oa", bn.getOa());
            map.put("ash", bn.getAsh());
            map.put("alc", bn.getAlc());
            map.put("nacl_eq", bn.getNaclEq());
        }

        // ミネラル
        if (item.getMinerals() != null) {
            var mn = item.getMinerals();
            map.put("na", mn.getNa());
            map.put("k", mn.getK());
            map.put("ca", mn.getCa());
            map.put("mg", mn.getMg());
            map.put("p", mn.getP());
            map.put("fe", mn.getFe());
            map.put("zn", mn.getZn());
            map.put("cu", mn.getCu());
            map.put("mn", mn.getMn());
            map.put("idd", mn.getIdd());
            map.put("se", mn.getSe());
            map.put("cr", mn.getCr());
            map.put("mo", mn.getMo());
        }

        // ビタミン
        if (item.getVitamins() != null) {
            var vt = item.getVitamins();
            map.put("ret", vt.getRet());
            map.put("carta", vt.getCarta());
            map.put("cartb", vt.getCartb());
            map.put("crypxb", vt.getCrypxb());
            map.put("cartbeq", vt.getCartbeq());
            map.put("vita_rae", vt.getVitaRae());
            map.put("vitd", vt.getVitd());
            map.put("tocpha", vt.getTocpha());
            map.put("tocphb", vt.getTocphb());
            map.put("tocphg", vt.getTocphg());
            map.put("tocphd", vt.getTocphd());
            map.put("vitk", vt.getVitk());
            map.put("thia", vt.getThia());
            map.put("ribf", vt.getRibf());
            map.put("nia", vt.getNia());
            map.put("niac", vt.getNiac());
            map.put("vitb6a", vt.getVitb6a());
            map.put("vitb12", vt.getVitb12());
            map.put("fol", vt.getFol());
            map.put("pantac", vt.getPantac());
            map.put("biot", vt.getBiot());
            map.put("vitc", vt.getVitc());
        }

        return map;
    }

    /**
     * 既存データにフラットなMapの値をマージ（nullでないフィールドのみ上書き）
     * snake_caseとcamelCase両方に対応
     */
    private void mergeFromFlatMap(FoodRawMaterial entity, Map<String, Object> map) {
        if (hasKey(map, "food_no", "foodNo")) entity.setFoodNo(getStringAny(map, "food_no", "foodNo"));
        if (hasKey(map, "food_group_id", "foodGroupId")) entity.setFoodGroupId(getIntegerAny(map, "food_group_id", "foodGroupId"));
        if (hasKey(map, "index_no", "indexNo")) entity.setIndexNo(getStringAny(map, "index_no", "indexNo"));
        if (hasKey(map, "class_category_id", "classCategoryId")) entity.setClassCategoryId(getIntegerAny(map, "class_category_id", "classCategoryId"));
        if (hasKey(map, "original_food_id", "originalFoodId")) entity.setOriginalFoodId(getIntegerAny(map, "original_food_id", "originalFoodId"));
        if (hasKey(map, "original_food_group_id", "originalFoodGroupId")) entity.setOriginalFoodGroupId(getIntegerAny(map, "original_food_group_id", "originalFoodGroupId"));
        if (hasKey(map, "original_food_no", "originalFoodNo")) entity.setOriginalFoodNo(getStringAny(map, "original_food_no", "originalFoodNo"));
        if (hasKey(map, "original_index_no", "originalIndexNo")) entity.setOriginalIndexNo(getStringAny(map, "original_index_no", "originalIndexNo"));
        if (hasKey(map, "original_food_name", "originalFoodName")) entity.setOriginalFoodName(getStringAny(map, "original_food_name", "originalFoodName"));
        if (hasKey(map, "food_name", "foodName")) entity.setFoodName(getStringAny(map, "food_name", "foodName"));
        if (hasKey(map, "food_fuku_bunrui", "foodFukuBunrui")) entity.setFoodFukuBunrui(getStringAny(map, "food_fuku_bunrui", "foodFukuBunrui"));
        if (hasKey(map, "food_rui_kubun", "foodRuiKubun")) entity.setFoodRuiKubun(getStringAny(map, "food_rui_kubun", "foodRuiKubun"));
        if (hasKey(map, "food_dai_bunrui", "foodDaiBunrui")) entity.setFoodDaiBunrui(getStringAny(map, "food_dai_bunrui", "foodDaiBunrui"));
        if (hasKey(map, "food_cyu_bunrui", "foodCyuBunrui")) entity.setFoodCyuBunrui(getStringAny(map, "food_cyu_bunrui", "foodCyuBunrui"));
        if (hasKey(map, "food_syo_bunrui", "foodSyoBunrui")) entity.setFoodSyoBunrui(getStringAny(map, "food_syo_bunrui", "foodSyoBunrui"));
        if (hasKey(map, "food_saibun", "foodSaibun")) entity.setFoodSaibun(getStringAny(map, "food_saibun", "foodSaibun"));
        if (hasKey(map, "category_id", "categoryId")) entity.setCategoryId(getIntegerAny(map, "category_id", "categoryId"));
        if (map.containsKey("hashtag")) entity.setHashtag(getString(map, "hashtag"));
        if (hasKey(map, "composite_raw_materials_kb", "compositeRawMaterialsKb")) entity.setCompositeRawMaterialsKb(getBooleanAny(map, "composite_raw_materials_kb", "compositeRawMaterialsKb"));
        if (hasKey(map, "price_per_unit", "pricePerUnit")) entity.setPricePerUnit(getFloatAny(map, "price_per_unit", "pricePerUnit"));
        if (hasKey(map, "maker_id", "makerId")) entity.setMakerId(getIntegerAny(map, "maker_id", "makerId"));
        if (hasKey(map, "saller_id", "sallerId")) entity.setSellerId(getIntegerAny(map, "saller_id", "sallerId"));
        if (hasKey(map, "composite_raw_itemlist", "compositeRawItemlist")) entity.setCompositeRawItemlist(getStringAny(map, "composite_raw_itemlist", "compositeRawItemlist"));
        if (hasKey(map, "display_name", "displayName")) entity.setDisplayName(getStringAny(map, "display_name", "displayName"));
        if (hasKey(map, "place_of_origin", "placeOfOrigin")) entity.setPlaceOfOrigin(getStringAny(map, "place_of_origin", "placeOfOrigin"));
        if (hasKey(map, "display_place_of_origin", "displayPlaceOfOrigin")) entity.setDisplayPlaceOfOrigin(getStringAny(map, "display_place_of_origin", "displayPlaceOfOrigin"));
        if (hasKey(map, "revision_of_food_no", "revisionOfFoodNo")) entity.setRevisionOfFoodNo(getIntegerAny(map, "revision_of_food_no", "revisionOfFoodNo"));
        if (hasKey(map, "next_food_id", "nextFoodId")) entity.setNextFoodId(getIntegerAny(map, "next_food_id", "nextFoodId"));
        if (map.containsKey("status")) entity.setStatus(getInteger(map, "status"));
        if (hasKey(map, "update_information", "updateInformation")) entity.setUpdateInformation(getStringAny(map, "update_information", "updateInformation"));
        if (map.containsKey("description")) entity.setDescription(getString(map, "description"));
        if (hasKey(map, "is_active", "isActive")) entity.setIsActive(getBooleanAny(map, "is_active", "isActive"));
    }

    // snake_caseまたはcamelCaseのキーが存在するかチェック
    private boolean hasKey(Map<String, Object> map, String snakeCase, String camelCase) {
        return map.containsKey(snakeCase) || map.containsKey(camelCase);
    }

    // snake_caseまたはcamelCaseから値を取得
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

    /**
     * フラットなMapからFoodRawMaterialに変換
     */
    private FoodRawMaterial fromFlatMap(Map<String, Object> map) {
        FoodRawMaterial entity = new FoodRawMaterial();

        // 基本情報
        entity.setFoodId(getInteger(map, "foodId"));
        entity.setFoodNo(getString(map, "foodNo"));
        entity.setFoodGroupId(getInteger(map, "foodGroupId"));
        entity.setIndexNo(getString(map, "indexNo"));
        entity.setClassCategoryId(getInteger(map, "classCategoryId"));
        entity.setOriginalFoodId(getInteger(map, "originalFoodId"));
        entity.setOriginalFoodGroupId(getInteger(map, "originalFoodGroupId"));
        entity.setOriginalFoodNo(getString(map, "originalFoodNo"));
        entity.setOriginalIndexNo(getString(map, "originalIndexNo"));
        entity.setOriginalFoodName(getString(map, "originalFoodName"));
        entity.setFoodName(getString(map, "foodName"));
        entity.setFoodFukuBunrui(getString(map, "foodFukuBunrui"));
        entity.setFoodRuiKubun(getString(map, "foodRuiKubun"));
        entity.setFoodDaiBunrui(getString(map, "foodDaiBunrui"));
        entity.setFoodCyuBunrui(getString(map, "foodCyuBunrui"));
        entity.setFoodSyoBunrui(getString(map, "foodSyoBunrui"));
        entity.setFoodSaibun(getString(map, "foodSaibun"));
        entity.setCategoryId(getInteger(map, "categoryId"));
        entity.setHashtag(getString(map, "hashtag"));
        entity.setCompositeRawMaterialsKb(getBoolean(map, "compositeRawMaterialsKb"));
        entity.setPricePerUnit(getFloat(map, "pricePerUnit"));
        entity.setMakerId(getInteger(map, "makerId"));
        entity.setSellerId(getInteger(map, "sallerId"));
        entity.setCompositeRawItemlist(getString(map, "compositeRawItemlist"));
        entity.setDisplayName(getString(map, "displayName"));
        entity.setPlaceOfOrigin(getString(map, "placeOfOrigin"));
        entity.setDisplayPlaceOfOrigin(getString(map, "displayPlaceOfOrigin"));
        entity.setRevisionOfFoodNo(getInteger(map, "revisionOfFoodNo"));
        entity.setNextFoodId(getInteger(map, "nextFoodId"));
        entity.setStatus(getInteger(map, "status"));
        entity.setUpdateInformation(getString(map, "updateInformation"));
        entity.setDescription(getString(map, "description"));
        entity.setIsActive(getBoolean(map, "isActive"));

        // 栄養成分はinsert/update時に必要であれば追加

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

    private Float getFloat(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Float) return (Float) value;
        if (value instanceof Number) return ((Number) value).floatValue();
        return Float.parseFloat(value.toString());
    }
}
