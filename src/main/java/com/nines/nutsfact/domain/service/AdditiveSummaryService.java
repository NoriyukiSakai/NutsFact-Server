package com.nines.nutsfact.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nines.nutsfact.domain.model.FoodPreProductDetailItem;
import com.nines.nutsfact.domain.model.FoodSemiFinishedProductDetail;
import com.nines.nutsfact.domain.model.additive.AdditiveSummary;
import com.nines.nutsfact.domain.model.additive.RawMaterialAdditive;
import com.nines.nutsfact.domain.repository.FoodPreProductDetailRepository;
import com.nines.nutsfact.domain.repository.FoodSemiFinishedProductDetailRepository;
import com.nines.nutsfact.domain.repository.FoodSemiFinishedProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 添加物集約サービス
 * 半完成品の構成材料から全添加物情報を集計し、使用量順にソートして返す
 */
@Slf4j
@Service
public class AdditiveSummaryService {

    private final FoodSemiFinishedProductRepository semiFinishedProductRepository;
    private final FoodSemiFinishedProductDetailRepository semiDetailRepository;
    private final FoodPreProductDetailRepository preProductDetailRepository;
    private final RawMaterialAdditiveService rawMaterialAdditiveService;

    private final ObjectMapper objectMapper;

    public AdditiveSummaryService(
            FoodSemiFinishedProductRepository semiFinishedProductRepository,
            FoodSemiFinishedProductDetailRepository semiDetailRepository,
            FoodPreProductDetailRepository preProductDetailRepository,
            RawMaterialAdditiveService rawMaterialAdditiveService) {
        this.semiFinishedProductRepository = semiFinishedProductRepository;
        this.semiDetailRepository = semiDetailRepository;
        this.preProductDetailRepository = preProductDetailRepository;
        this.rawMaterialAdditiveService = rawMaterialAdditiveService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * 半完成品の添加物情報を集計
     */
    @Transactional(readOnly = true)
    public AdditiveSummary aggregate(Integer semiId) {
        List<FoodSemiFinishedProductDetail> details = semiDetailRepository.findBySemiId(semiId);

        // 添加物IDごとの集計データ
        Map<Integer, AdditiveAggregation> additiveMap = new HashMap<>();

        for (FoodSemiFinishedProductDetail detail : details) {
            Float weight = detail.getWeight() != null ? detail.getWeight() : 0f;

            if (Boolean.TRUE.equals(detail.getComponentKb())) {
                // 仕込品の場合: 仕込品の明細を展開
                expandPreProductAdditives(detail.getDetailPreId(), weight.doubleValue(), additiveMap);
            } else {
                // 原材料の場合: 直接添加物を取得
                addAdditives(detail.getDetailFoodId(), weight.doubleValue(), additiveMap);
            }
        }

        // 使用量順にソートしてリストに変換
        List<AdditiveSummary.AdditiveItem> sortedList = additiveMap.values().stream()
            .filter(agg -> agg.totalWeight > 0)
            .sorted(Comparator.comparingDouble(agg -> -agg.totalWeight))
            .map(this::toAdditiveItem)
            .collect(Collectors.toList());

        return AdditiveSummary.builder()
            .additives(sortedList)
            .calculatedAt(LocalDateTime.now())
            .build();
    }

    /**
     * 仕込品の添加物情報を展開して集計に追加
     */
    private void expandPreProductAdditives(Integer preId, Double preProductWeight, Map<Integer, AdditiveAggregation> additiveMap) {
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
                expandPreProductAdditives(preDetail.getDetailPreId(), adjustedWeight, additiveMap);
            } else {
                // 原材料: 添加物を取得して追加
                addAdditives(preDetail.getDetailFoodId(), adjustedWeight, additiveMap);
            }
        }
    }

    /**
     * 原材料の添加物情報を集計に追加
     */
    private void addAdditives(Integer foodId, Double materialWeight, Map<Integer, AdditiveAggregation> additiveMap) {
        if (foodId == null || materialWeight == null || materialWeight <= 0) {
            return;
        }

        // 原材料に割り当てられた添加物を取得（添加物マスタ情報付き）
        List<RawMaterialAdditive> rawMaterialAdditives = rawMaterialAdditiveService.findByFoodIdWithAdditive(foodId);

        for (RawMaterialAdditive rma : rawMaterialAdditives) {
            if (rma.getAdditive() == null || !Boolean.TRUE.equals(rma.getIsActive())) {
                continue;
            }

            Integer additiveId = rma.getAdditiveId();

            // 使用量を計算: usageAmount (g/100g) × materialWeight / 100
            Float usageAmount = rma.getUsageAmount() != null ? rma.getUsageAmount() : 0f;
            double additiveWeight = (usageAmount / 100.0) * materialWeight;

            // 既存の集計データに追加または新規作成
            AdditiveAggregation agg = additiveMap.computeIfAbsent(additiveId,
                id -> new AdditiveAggregation(rma));
            agg.totalWeight += additiveWeight;
        }
    }

    /**
     * 集計データをAdditiveItemに変換
     */
    private AdditiveSummary.AdditiveItem toAdditiveItem(AdditiveAggregation agg) {
        return AdditiveSummary.AdditiveItem.builder()
            .additiveId(agg.additiveId)
            .substanceName(agg.substanceName)
            .simplifiedName(agg.simplifiedName)
            .purposeCategory(agg.purposeCategory)
            .collectiveName(agg.collectiveName)
            .requiresPurposeDisplay(agg.requiresPurposeDisplay)
            .totalWeight(agg.totalWeight)
            .exemptionType(agg.exemptionType)
            .allergenOrigin(agg.allergenOrigin)
            .build();
    }

    /**
     * 添加物集約情報をJSON文字列に変換
     */
    public String toJson(AdditiveSummary summary) {
        try {
            return objectMapper.writeValueAsString(summary);
        } catch (JsonProcessingException e) {
            log.error("添加物情報のJSON変換に失敗: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * JSON文字列から添加物集約情報に変換
     */
    public AdditiveSummary fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, AdditiveSummary.class);
        } catch (JsonProcessingException e) {
            log.error("添加物情報のJSON解析に失敗: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 半完成品の添加物情報を再計算して保存
     */
    @Transactional
    public AdditiveSummary recalculateAndSave(Integer semiId) {
        AdditiveSummary summary = aggregate(semiId);
        String json = toJson(summary);

        if (json != null) {
            semiFinishedProductRepository.updateAdditiveSummary(semiId, json);
            log.info("半完成品の添加物情報を更新しました: semiId={}", semiId);
        }

        return summary;
    }

    /**
     * 集計用の内部クラス
     */
    private static class AdditiveAggregation {
        Integer additiveId;
        String substanceName;
        String simplifiedName;
        Integer purposeCategory;
        Integer collectiveName;
        Boolean requiresPurposeDisplay;
        Integer exemptionType;
        String allergenOrigin;
        double totalWeight = 0;

        AdditiveAggregation(RawMaterialAdditive rma) {
            this.additiveId = rma.getAdditiveId();
            this.exemptionType = rma.getExemptionType();
            this.allergenOrigin = rma.getAllergenOrigin();

            if (rma.getAdditive() != null) {
                this.substanceName = rma.getAdditive().getSubstanceName();
                this.simplifiedName = rma.getAdditive().getSimplifiedName();
                this.purposeCategory = rma.getAdditive().getPurposeCategory();
                this.collectiveName = rma.getAdditive().getCollectiveName();
                this.requiresPurposeDisplay = rma.getAdditive().getRequiresPurposeDisplay();
            }
        }
    }
}
