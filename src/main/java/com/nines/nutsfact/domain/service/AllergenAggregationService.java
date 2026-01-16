package com.nines.nutsfact.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nines.nutsfact.domain.model.FoodPreProductDetailItem;
import com.nines.nutsfact.domain.model.FoodSemiFinishedProductDetail;
import com.nines.nutsfact.domain.model.allergy.AllergenicControl;
import com.nines.nutsfact.domain.model.allergy.AllergenSummary;
import com.nines.nutsfact.domain.repository.FoodPreProductDetailRepository;
import com.nines.nutsfact.domain.repository.FoodSemiFinishedProductDetailRepository;
import com.nines.nutsfact.domain.repository.FoodSemiFinishedProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * アレルゲン集約サービス
 * 半完成品の構成材料から全アレルゲン情報を集計し、使用量順にソートして返す
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AllergenAggregationService {

    private final FoodSemiFinishedProductRepository semiFinishedProductRepository;
    private final FoodSemiFinishedProductDetailRepository semiDetailRepository;
    private final FoodPreProductDetailRepository preProductDetailRepository;
    private final AllergenicControlService allergenicControlService;

    private final ObjectMapper objectMapper;

    public AllergenAggregationService(
            FoodSemiFinishedProductRepository semiFinishedProductRepository,
            FoodSemiFinishedProductDetailRepository semiDetailRepository,
            FoodPreProductDetailRepository preProductDetailRepository,
            AllergenicControlService allergenicControlService) {
        this.semiFinishedProductRepository = semiFinishedProductRepository;
        this.semiDetailRepository = semiDetailRepository;
        this.preProductDetailRepository = preProductDetailRepository;
        this.allergenicControlService = allergenicControlService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * 半完成品のアレルゲン情報を集計
     */
    @Transactional(readOnly = true)
    public AllergenSummary aggregate(Integer semiId) {
        List<FoodSemiFinishedProductDetail> details = semiDetailRepository.findBySemiId(semiId);

        // アレルゲン番号ごとの使用量を集計
        Map<Integer, Double> allergenWeights = new HashMap<>();

        for (FoodSemiFinishedProductDetail detail : details) {
            Float weight = detail.getWeight() != null ? detail.getWeight() : 0f;

            if (Boolean.TRUE.equals(detail.getComponentKb())) {
                // 仕込品の場合: 仕込品の明細を展開
                expandPreProductAllergens(detail.getDetailPreId(), weight.doubleValue(), allergenWeights);
            } else {
                // 原材料の場合: 直接AllergenicControlを取得
                addAllergens(detail.getDetailFoodId(), weight.doubleValue(), allergenWeights);
            }
        }

        // 使用量順にソートしてリストに変換
        List<AllergenSummary.AllergenItem> sortedList = allergenWeights.entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .sorted(Comparator.comparingDouble(e -> -e.getValue()))
            .map(e -> AllergenSummary.AllergenItem.builder()
                .itemNo(e.getKey())
                .type(AllergenSummary.getAllergenType(e.getKey()))
                .name(AllergenSummary.getAllergenName(e.getKey()))
                .totalWeight(e.getValue())
                .isMandatory(AllergenSummary.isMandatory(e.getKey()))
                .build())
            .collect(Collectors.toList());

        return AllergenSummary.builder()
            .allergens(sortedList)
            .calculatedAt(LocalDateTime.now())
            .build();
    }

    /**
     * 仕込品のアレルゲン情報を展開して集計に追加
     */
    private void expandPreProductAllergens(Integer preId, Double preProductWeight, Map<Integer, Double> allergenWeights) {
        if (preId == null) {
            return;
        }

        List<FoodPreProductDetailItem> preDetails = preProductDetailRepository.findByPreId(preId);

        // 仕込品内の合計重量を計算
        double totalWeight = preDetails.stream()
            .mapToDouble(d -> d.getWeight() != null ? d.getWeight() : 0f)
            .sum();

        if (totalWeight == 0) {
            return;
        }

        for (FoodPreProductDetailItem preDetail : preDetails) {
            Float detailWeight = preDetail.getWeight() != null ? preDetail.getWeight() : 0f;
            // 仕込品の重量に対する明細の比率を計算し、半完成品での仕込品の重量に適用
            double adjustedWeight = (detailWeight / totalWeight) * preProductWeight;

            if (Boolean.TRUE.equals(preDetail.getComponentKb())) {
                // 仕込品内の仕込品（ネスト）: 再帰的に展開
                expandPreProductAllergens(preDetail.getDetailPreId(), adjustedWeight, allergenWeights);
            } else {
                // 原材料: AllergenicControlを取得して追加
                addAllergens(preDetail.getDetailFoodId(), adjustedWeight, allergenWeights);
            }
        }
    }

    /**
     * 原材料のアレルゲン情報を集計に追加
     */
    private void addAllergens(Integer foodId, Double weight, Map<Integer, Double> allergenWeights) {
        if (foodId == null || weight == null || weight <= 0) {
            return;
        }

        Optional<AllergenicControl> allergenOpt = allergenicControlService.findByFoodIdOptional(foodId);
        if (allergenOpt.isEmpty()) {
            return;
        }

        AllergenicControl allergen = allergenOpt.get();

        // 各アレルゲン項目をチェックして集計
        if (Boolean.TRUE.equals(allergen.getItem1Val())) {
            allergenWeights.merge(1, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem2Val())) {
            allergenWeights.merge(2, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem3Val())) {
            allergenWeights.merge(3, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem4Val())) {
            allergenWeights.merge(4, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem5Val())) {
            allergenWeights.merge(5, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem6Val())) {
            allergenWeights.merge(6, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem7Val())) {
            allergenWeights.merge(7, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem8Val())) {
            allergenWeights.merge(8, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem9Val())) {
            allergenWeights.merge(9, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem10Val())) {
            allergenWeights.merge(10, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem11Val())) {
            allergenWeights.merge(11, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem12Val())) {
            allergenWeights.merge(12, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem13Val())) {
            allergenWeights.merge(13, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem14Val())) {
            allergenWeights.merge(14, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem15Val())) {
            allergenWeights.merge(15, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem16Val())) {
            allergenWeights.merge(16, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem17Val())) {
            allergenWeights.merge(17, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem18Val())) {
            allergenWeights.merge(18, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem19Val())) {
            allergenWeights.merge(19, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem20Val())) {
            allergenWeights.merge(20, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem21Val())) {
            allergenWeights.merge(21, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem22Val())) {
            allergenWeights.merge(22, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem23Val())) {
            allergenWeights.merge(23, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem24Val())) {
            allergenWeights.merge(24, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem25Val())) {
            allergenWeights.merge(25, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem26Val())) {
            allergenWeights.merge(26, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem27Val())) {
            allergenWeights.merge(27, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem28Val())) {
            allergenWeights.merge(28, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem29Val())) {
            allergenWeights.merge(29, weight, Double::sum);
        }
        if (Boolean.TRUE.equals(allergen.getItem30Val())) {
            allergenWeights.merge(30, weight, Double::sum);
        }
    }

    /**
     * アレルゲン集約情報をJSON文字列に変換
     */
    public String toJson(AllergenSummary summary) {
        try {
            return objectMapper.writeValueAsString(summary);
        } catch (JsonProcessingException e) {
            log.error("アレルゲン情報のJSON変換に失敗: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * JSON文字列からアレルゲン集約情報に変換
     */
    public AllergenSummary fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, AllergenSummary.class);
        } catch (JsonProcessingException e) {
            log.error("アレルゲン情報のJSON解析に失敗: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 半完成品のアレルゲン情報を再計算して保存
     */
    @Transactional
    public AllergenSummary recalculateAndSave(Integer semiId) {
        AllergenSummary summary = aggregate(semiId);
        String json = toJson(summary);

        if (json != null) {
            semiFinishedProductRepository.updateAllergenSummary(semiId, json);
            log.info("半完成品のアレルゲン情報を更新しました: semiId={}", semiId);
        }

        return summary;
    }
}
