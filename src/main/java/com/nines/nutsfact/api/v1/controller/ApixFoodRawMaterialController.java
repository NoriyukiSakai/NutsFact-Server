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
import com.nines.nutsfact.domain.model.nutrition.NutritionBasic;
import com.nines.nutsfact.domain.model.nutrition.NutritionMinerals;
import com.nines.nutsfact.domain.model.nutrition.NutritionVitamins;
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
     * patternId: 1=8訂, 2=拡張, 3=ユーザ定義
     * patternId=1,2はbusinessAccountIdでフィルタリングしない
     * patternId=3はbusinessAccountIdでフィルタリング
     */
    @GetMapping("/getDataX")
    public ResponseEntity<Map<String, Object>> getDataX(
            @RequestParam(value = "patternId", required = false) Integer patternId) {

        List<FoodRawMaterial> items;
        if (patternId != null) {
            items = service.findByCategoryWithBusinessAccountFilter(patternId);
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
     * categoryId=1（8訂）, 2（拡張）の場合はbusinessAccountIdでフィルタリングしない
     */
    @GetMapping("/findById")
    public ResponseEntity<Map<String, Object>> findById(
            @RequestParam("foodId") Integer foodId) {

        FoodRawMaterial item = service.findByIdWithBusinessAccountFilter(foodId);
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

        // 新バージョンとして保存された場合はメッセージを追加
        if (created.getRevisionOfFoodNo() != null && created.getRevisionOfFoodNo() > 0) {
            response.put("message", "新たなバージョン（リビジョン" + created.getRevisionOfFoodNo() + "）として保存されました");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 原材料更新
     * businessAccountIdでフィルタリング
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

        // 既存データを取得（businessAccountIdでフィルタリング）
        FoodRawMaterial existing = service.findByIdWithBusinessAccountFilter(foodId);

        // リクエストの値で上書き（nullでないフィールドのみ）
        mergeFromFlatMap(existing, request);

        FoodRawMaterial updated = service.updateWithBusinessAccountFilter(existing);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("item", toFlatMap(updated));
        return ResponseEntity.ok(response);
    }

    /**
     * 原材料削除
     * businessAccountIdでフィルタリング
     */
    @GetMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(
            @RequestParam("foodId") Integer foodId) {

        service.deleteWithBusinessAccountFilter(foodId);

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
        map.put("business_account_id", item.getBusinessAccountId());
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
        map.put("saller_id", item.getSallerId());
        map.put("saller_name", item.getSallerName());
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
        if (hasKey(map, "last_price_per_unit", "lastPricePerUnit")) entity.setLastPricePerUnit(getFloatAny(map, "last_price_per_unit", "lastPricePerUnit"));
        if (hasKey(map, "maker_id", "makerId")) entity.setMakerId(getIntegerAny(map, "maker_id", "makerId"));
        if (hasKey(map, "maker_name", "makerName")) entity.setMakerName(getStringAny(map, "maker_name", "makerName"));
        if (hasKey(map, "saller_id", "sallerId")) entity.setSallerId(getIntegerAny(map, "saller_id", "sallerId"));
        if (hasKey(map, "saller_name", "sallerName")) entity.setSallerName(getStringAny(map, "saller_name", "sallerName"));
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

        // 基本栄養成分（既存がnullの場合は新規作成）
        NutritionBasic basicNutrition = entity.getBasicNutrition() != null ? entity.getBasicNutrition() : new NutritionBasic();
        if (map.containsKey("refuse")) basicNutrition.setRefuse(getFloat(map, "refuse"));
        if (hasKey(map, "enerc", "enerc")) basicNutrition.setEnerc(getIntegerAny(map, "enerc", "enerc"));
        if (hasKey(map, "enerc_kcal", "enercKcal")) basicNutrition.setEnercKcal(getIntegerAny(map, "enerc_kcal", "enercKcal"));
        if (map.containsKey("water")) basicNutrition.setWater(getFloat(map, "water"));
        if (map.containsKey("prot")) basicNutrition.setProt(getFloat(map, "prot"));
        if (map.containsKey("protcaa")) basicNutrition.setProtcaa(getFloat(map, "protcaa"));
        if (map.containsKey("fat")) basicNutrition.setFat(getFloat(map, "fat"));
        if (map.containsKey("fatnlea")) basicNutrition.setFatnlea(getFloat(map, "fatnlea"));
        if (map.containsKey("chole")) basicNutrition.setChole(getFloat(map, "chole"));
        if (map.containsKey("chocdf")) basicNutrition.setChocdf(getFloat(map, "chocdf"));
        if (map.containsKey("choavlm")) basicNutrition.setChoavlm(getFloat(map, "choavlm"));
        if (hasKey(map, "choavlm_mark", "choavlmMark")) basicNutrition.setChoavlmMark(getBooleanAny(map, "choavlm_mark", "choavlmMark"));
        if (map.containsKey("choavl")) basicNutrition.setChoavl(getFloat(map, "choavl"));
        if (map.containsKey("choavldf")) basicNutrition.setChoavldf(getFloat(map, "choavldf"));
        if (hasKey(map, "choavldf_mark", "choavldfMark")) basicNutrition.setChoavldfMark(getBooleanAny(map, "choavldf_mark", "choavldfMark"));
        if (map.containsKey("fib")) basicNutrition.setFib(getFloat(map, "fib"));
        if (map.containsKey("polyl")) basicNutrition.setPolyl(getFloat(map, "polyl"));
        if (map.containsKey("oa")) basicNutrition.setOa(getFloat(map, "oa"));
        if (map.containsKey("ash")) basicNutrition.setAsh(getFloat(map, "ash"));
        if (map.containsKey("alc")) basicNutrition.setAlc(getFloat(map, "alc"));
        if (hasKey(map, "nacl_eq", "naclEq")) basicNutrition.setNaclEq(getFloatAny(map, "nacl_eq", "naclEq"));
        entity.setBasicNutrition(basicNutrition);

        // ミネラル
        NutritionMinerals minerals = entity.getMinerals() != null ? entity.getMinerals() : new NutritionMinerals();
        if (map.containsKey("na")) minerals.setNa(getFloat(map, "na"));
        if (map.containsKey("k")) minerals.setK(getFloat(map, "k"));
        if (map.containsKey("ca")) minerals.setCa(getFloat(map, "ca"));
        if (map.containsKey("mg")) minerals.setMg(getFloat(map, "mg"));
        if (map.containsKey("p")) minerals.setP(getFloat(map, "p"));
        if (map.containsKey("fe")) minerals.setFe(getFloat(map, "fe"));
        if (map.containsKey("zn")) minerals.setZn(getFloat(map, "zn"));
        if (map.containsKey("cu")) minerals.setCu(getFloat(map, "cu"));
        if (map.containsKey("mn")) minerals.setMn(getFloat(map, "mn"));
        if (map.containsKey("idd")) minerals.setIdd(getFloat(map, "idd"));
        if (map.containsKey("se")) minerals.setSe(getFloat(map, "se"));
        if (map.containsKey("cr")) minerals.setCr(getFloat(map, "cr"));
        if (map.containsKey("mo")) minerals.setMo(getFloat(map, "mo"));
        entity.setMinerals(minerals);

        // ビタミン
        NutritionVitamins vitamins = entity.getVitamins() != null ? entity.getVitamins() : new NutritionVitamins();
        if (map.containsKey("ret")) vitamins.setRet(getFloat(map, "ret"));
        if (map.containsKey("carta")) vitamins.setCarta(getFloat(map, "carta"));
        if (map.containsKey("cartb")) vitamins.setCartb(getFloat(map, "cartb"));
        if (map.containsKey("crypxb")) vitamins.setCrypxb(getFloat(map, "crypxb"));
        if (map.containsKey("cartbeq")) vitamins.setCartbeq(getFloat(map, "cartbeq"));
        if (hasKey(map, "vita_rae", "vitaRae")) vitamins.setVitaRae(getFloatAny(map, "vita_rae", "vitaRae"));
        if (map.containsKey("vitd")) vitamins.setVitd(getFloat(map, "vitd"));
        if (map.containsKey("tocpha")) vitamins.setTocpha(getFloat(map, "tocpha"));
        if (map.containsKey("tocphb")) vitamins.setTocphb(getFloat(map, "tocphb"));
        if (map.containsKey("tocphg")) vitamins.setTocphg(getFloat(map, "tocphg"));
        if (map.containsKey("tocphd")) vitamins.setTocphd(getFloat(map, "tocphd"));
        if (map.containsKey("vitk")) vitamins.setVitk(getFloat(map, "vitk"));
        if (map.containsKey("thia")) vitamins.setThia(getFloat(map, "thia"));
        if (map.containsKey("ribf")) vitamins.setRibf(getFloat(map, "ribf"));
        if (map.containsKey("nia")) vitamins.setNia(getFloat(map, "nia"));
        if (map.containsKey("niac")) vitamins.setNiac(getFloat(map, "niac"));
        if (map.containsKey("vitb6a")) vitamins.setVitb6a(getFloat(map, "vitb6a"));
        if (map.containsKey("vitb12")) vitamins.setVitb12(getFloat(map, "vitb12"));
        if (map.containsKey("fol")) vitamins.setFol(getFloat(map, "fol"));
        if (map.containsKey("pantac")) vitamins.setPantac(getFloat(map, "pantac"));
        if (map.containsKey("biot")) vitamins.setBiot(getFloat(map, "biot"));
        if (map.containsKey("vitc")) vitamins.setVitc(getFloat(map, "vitc"));
        entity.setVitamins(vitamins);
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

        // 基本情報（snake_case/camelCase両対応）
        entity.setFoodId(getIntegerAny(map, "food_id", "foodId"));
        entity.setFoodNo(getStringAny(map, "food_no", "foodNo"));
        entity.setFoodGroupId(getIntegerAny(map, "food_group_id", "foodGroupId"));
        entity.setIndexNo(getStringAny(map, "index_no", "indexNo"));
        entity.setClassCategoryId(getIntegerAny(map, "class_category_id", "classCategoryId"));
        entity.setOriginalFoodId(getIntegerAny(map, "original_food_id", "originalFoodId"));
        entity.setOriginalFoodGroupId(getIntegerAny(map, "original_food_group_id", "originalFoodGroupId"));
        entity.setOriginalFoodNo(getStringAny(map, "original_food_no", "originalFoodNo"));
        entity.setOriginalIndexNo(getStringAny(map, "original_index_no", "originalIndexNo"));
        entity.setOriginalFoodName(getStringAny(map, "original_food_name", "originalFoodName"));
        entity.setFoodName(getStringAny(map, "food_name", "foodName"));
        entity.setFoodFukuBunrui(getStringAny(map, "food_fuku_bunrui", "foodFukuBunrui"));
        entity.setFoodRuiKubun(getStringAny(map, "food_rui_kubun", "foodRuiKubun"));
        entity.setFoodDaiBunrui(getStringAny(map, "food_dai_bunrui", "foodDaiBunrui"));
        entity.setFoodCyuBunrui(getStringAny(map, "food_cyu_bunrui", "foodCyuBunrui"));
        entity.setFoodSyoBunrui(getStringAny(map, "food_syo_bunrui", "foodSyoBunrui"));
        entity.setFoodSaibun(getStringAny(map, "food_saibun", "foodSaibun"));
        entity.setCategoryId(getIntegerAny(map, "category_id", "categoryId"));
        entity.setHashtag(getStringAny(map, "hashtag", "hashtag"));
        // compositeRawMaterialsKb はNOT NULL制約があるためデフォルトfalse
        Boolean compositeKb = getBooleanAny(map, "composite_raw_materials_kb", "compositeRawMaterialsKb");
        entity.setCompositeRawMaterialsKb(compositeKb != null ? compositeKb : false);
        entity.setPricePerUnit(getFloatAny(map, "price_per_unit", "pricePerUnit"));
        entity.setLastPricePerUnit(getFloatAny(map, "last_price_per_unit", "lastPricePerUnit"));
        entity.setMakerId(getIntegerAny(map, "maker_id", "makerId"));
        entity.setMakerName(getStringAny(map, "maker_name", "makerName"));
        entity.setSallerId(getIntegerAny(map, "saller_id", "sallerId"));
        entity.setSallerName(getStringAny(map, "saller_name", "sallerName"));
        entity.setCompositeRawItemlist(getStringAny(map, "composite_raw_itemlist", "compositeRawItemlist"));
        entity.setDisplayName(getStringAny(map, "display_name", "displayName"));
        entity.setPlaceOfOrigin(getStringAny(map, "place_of_origin", "placeOfOrigin"));
        entity.setDisplayPlaceOfOrigin(getStringAny(map, "display_place_of_origin", "displayPlaceOfOrigin"));
        // revisionOfFoodNo はNOT NULL制約があるためデフォルト0
        Integer revisionNo = getIntegerAny(map, "revision_of_food_no", "revisionOfFoodNo");
        entity.setRevisionOfFoodNo(revisionNo != null ? revisionNo : 0);
        entity.setNextFoodId(getIntegerAny(map, "next_food_id", "nextFoodId"));
        // status はNOT NULL制約があるためデフォルト0
        Integer status = getIntegerAny(map, "status", "status");
        entity.setStatus(status != null ? status : 0);
        entity.setUpdateInformation(getStringAny(map, "update_information", "updateInformation"));
        entity.setDescription(getStringAny(map, "description", "description"));
        // isActive はNOT NULL制約があるためデフォルトtrue
        Boolean isActive = getBooleanAny(map, "is_active", "isActive");
        entity.setIsActive(isActive != null ? isActive : true);

        // 基本栄養成分
        NutritionBasic basicNutrition = new NutritionBasic();
        basicNutrition.setRefuse(getFloatAny(map, "refuse", "refuse"));
        basicNutrition.setEnerc(getIntegerAny(map, "enerc", "enerc"));
        basicNutrition.setEnercKcal(getIntegerAny(map, "enerc_kcal", "enercKcal"));
        basicNutrition.setWater(getFloatAny(map, "water", "water"));
        basicNutrition.setProt(getFloatAny(map, "prot", "prot"));
        basicNutrition.setProtcaa(getFloatAny(map, "protcaa", "protcaa"));
        basicNutrition.setFat(getFloatAny(map, "fat", "fat"));
        basicNutrition.setFatnlea(getFloatAny(map, "fatnlea", "fatnlea"));
        basicNutrition.setChole(getFloatAny(map, "chole", "chole"));
        basicNutrition.setChocdf(getFloatAny(map, "chocdf", "chocdf"));
        basicNutrition.setChoavlm(getFloatAny(map, "choavlm", "choavlm"));
        basicNutrition.setChoavlmMark(getBooleanAny(map, "choavlm_mark", "choavlmMark"));
        basicNutrition.setChoavl(getFloatAny(map, "choavl", "choavl"));
        basicNutrition.setChoavldf(getFloatAny(map, "choavldf", "choavldf"));
        basicNutrition.setChoavldfMark(getBooleanAny(map, "choavldf_mark", "choavldfMark"));
        basicNutrition.setFib(getFloatAny(map, "fib", "fib"));
        basicNutrition.setPolyl(getFloatAny(map, "polyl", "polyl"));
        basicNutrition.setOa(getFloatAny(map, "oa", "oa"));
        basicNutrition.setAsh(getFloatAny(map, "ash", "ash"));
        basicNutrition.setAlc(getFloatAny(map, "alc", "alc"));
        basicNutrition.setNaclEq(getFloatAny(map, "nacl_eq", "naclEq"));
        entity.setBasicNutrition(basicNutrition);

        // ミネラル
        NutritionMinerals minerals = new NutritionMinerals();
        minerals.setNa(getFloatAny(map, "na", "na"));
        minerals.setK(getFloatAny(map, "k", "k"));
        minerals.setCa(getFloatAny(map, "ca", "ca"));
        minerals.setMg(getFloatAny(map, "mg", "mg"));
        minerals.setP(getFloatAny(map, "p", "p"));
        minerals.setFe(getFloatAny(map, "fe", "fe"));
        minerals.setZn(getFloatAny(map, "zn", "zn"));
        minerals.setCu(getFloatAny(map, "cu", "cu"));
        minerals.setMn(getFloatAny(map, "mn", "mn"));
        minerals.setIdd(getFloatAny(map, "idd", "idd"));
        minerals.setSe(getFloatAny(map, "se", "se"));
        minerals.setCr(getFloatAny(map, "cr", "cr"));
        minerals.setMo(getFloatAny(map, "mo", "mo"));
        entity.setMinerals(minerals);

        // ビタミン
        NutritionVitamins vitamins = new NutritionVitamins();
        vitamins.setRet(getFloatAny(map, "ret", "ret"));
        vitamins.setCarta(getFloatAny(map, "carta", "carta"));
        vitamins.setCartb(getFloatAny(map, "cartb", "cartb"));
        vitamins.setCrypxb(getFloatAny(map, "crypxb", "crypxb"));
        vitamins.setCartbeq(getFloatAny(map, "cartbeq", "cartbeq"));
        vitamins.setVitaRae(getFloatAny(map, "vita_rae", "vitaRae"));
        vitamins.setVitd(getFloatAny(map, "vitd", "vitd"));
        vitamins.setTocpha(getFloatAny(map, "tocpha", "tocpha"));
        vitamins.setTocphb(getFloatAny(map, "tocphb", "tocphb"));
        vitamins.setTocphg(getFloatAny(map, "tocphg", "tocphg"));
        vitamins.setTocphd(getFloatAny(map, "tocphd", "tocphd"));
        vitamins.setVitk(getFloatAny(map, "vitk", "vitk"));
        vitamins.setThia(getFloatAny(map, "thia", "thia"));
        vitamins.setRibf(getFloatAny(map, "ribf", "ribf"));
        vitamins.setNia(getFloatAny(map, "nia", "nia"));
        vitamins.setNiac(getFloatAny(map, "niac", "niac"));
        vitamins.setVitb6a(getFloatAny(map, "vitb6a", "vitb6a"));
        vitamins.setVitb12(getFloatAny(map, "vitb12", "vitb12"));
        vitamins.setFol(getFloatAny(map, "fol", "fol"));
        vitamins.setPantac(getFloatAny(map, "pantac", "pantac"));
        vitamins.setBiot(getFloatAny(map, "biot", "biot"));
        vitamins.setVitc(getFloatAny(map, "vitc", "vitc"));
        entity.setVitamins(vitamins);

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
