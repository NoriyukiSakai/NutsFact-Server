package com.nines.nutsfact.domain.service;

import com.nines.nutsfact.domain.model.*;
import com.nines.nutsfact.domain.repository.FoodPreProductDetailRepository;
import com.nines.nutsfact.domain.repository.FoodPreProductRepository;
import com.nines.nutsfact.domain.repository.FoodRawMaterialRepository;
import com.nines.nutsfact.domain.repository.FoodSemiFinishedProductDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 原材料展開サービス
 * 半完成品の構成材料を再帰的に展開し、ラベル表示用の原材料リストを生成
 */
@Slf4j
@Service
public class IngredientExpansionService {

    private final FoodSemiFinishedProductDetailRepository semiDetailRepository;
    private final FoodPreProductDetailRepository preProductDetailRepository;
    private final FoodPreProductRepository preProductRepository;
    private final FoodRawMaterialRepository rawMaterialRepository;

    public IngredientExpansionService(
            FoodSemiFinishedProductDetailRepository semiDetailRepository,
            FoodPreProductDetailRepository preProductDetailRepository,
            FoodPreProductRepository preProductRepository,
            FoodRawMaterialRepository rawMaterialRepository) {
        this.semiDetailRepository = semiDetailRepository;
        this.preProductDetailRepository = preProductDetailRepository;
        this.preProductRepository = preProductRepository;
        this.rawMaterialRepository = rawMaterialRepository;
    }

    /**
     * 展開済み原材料情報
     */
    public static class ExpandedIngredient {
        private Integer rawMaterialId;
        private String labelDisplayName;
        private Double effectiveWeight;
        private Boolean isComposite;

        public ExpandedIngredient(Integer rawMaterialId, String labelDisplayName, Double effectiveWeight, Boolean isComposite) {
            this.rawMaterialId = rawMaterialId;
            this.labelDisplayName = labelDisplayName;
            this.effectiveWeight = effectiveWeight;
            this.isComposite = isComposite;
        }

        public Integer getRawMaterialId() { return rawMaterialId; }
        public String getLabelDisplayName() { return labelDisplayName; }
        public Double getEffectiveWeight() { return effectiveWeight; }
        public Boolean getIsComposite() { return isComposite; }
    }

    /**
     * 展開済み原材料レスポンス
     */
    public static class ExpandedIngredientsResponse {
        private List<ExpandedIngredient> ingredients;
        private LocalDateTime calculatedAt;

        public ExpandedIngredientsResponse(List<ExpandedIngredient> ingredients, LocalDateTime calculatedAt) {
            this.ingredients = ingredients;
            this.calculatedAt = calculatedAt;
        }

        public List<ExpandedIngredient> getIngredients() { return ingredients; }
        public LocalDateTime getCalculatedAt() { return calculatedAt; }
    }

    /**
     * 半完成品の原材料を再帰的に展開
     * @param semiId 半完成品ID
     * @return 展開済み原材料レスポンス
     */
    @Transactional(readOnly = true)
    public ExpandedIngredientsResponse expand(Integer semiId) {
        List<FoodSemiFinishedProductDetail> details = semiDetailRepository.findBySemiId(semiId);
        Set<Integer> visitedPreIds = new HashSet<>();

        List<ExpandedIngredient> expandedList = new ArrayList<>();

        for (FoodSemiFinishedProductDetail detail : details) {
            Float weight = detail.getWeight() != null ? detail.getWeight() : 0f;

            if (Boolean.TRUE.equals(detail.getComponentKb())) {
                // 仕込品の場合: 再帰的に展開
                Integer preId = detail.getDetailPreId();
                if (preId != null && !visitedPreIds.contains(preId)) {
                    expandPreProduct(preId, weight.doubleValue(), visitedPreIds, expandedList);
                }
            } else {
                // 原材料の場合: 直接追加
                addRawMaterialIngredient(detail, weight.doubleValue(), expandedList);
            }
        }

        return new ExpandedIngredientsResponse(expandedList, LocalDateTime.now());
    }

    /**
     * 仕込品を再帰的に展開
     */
    private void expandPreProduct(Integer preId, Double preProductWeight,
                                   Set<Integer> visitedPreIds, List<ExpandedIngredient> expandedList) {
        if (preId == null || visitedPreIds.contains(preId)) {
            log.warn("循環参照を検出またはnullのpreId: preId={}", preId);
            return;
        }

        visitedPreIds.add(preId);

        // 仕込品の総重量を取得
        FoodPreProductItem preProduct = preProductRepository.findById(preId).orElse(null);
        if (preProduct == null) {
            log.warn("仕込品が見つかりません: preId={}", preId);
            return;
        }

        Float totalWeight = preProduct.getWeightSum();
        if (totalWeight == null || totalWeight <= 0) {
            // weightSumがない場合は明細から計算
            List<FoodPreProductDetailItem> preDetails = preProductDetailRepository.findByPreId(preId);
            totalWeight = (float) preDetails.stream()
                .mapToDouble(d -> d.getWeight() != null ? d.getWeight() : 0f)
                .sum();
        }

        if (totalWeight <= 0) {
            return;
        }

        // 使用比率を計算
        double ratio = preProductWeight / totalWeight;

        List<FoodPreProductDetailItem> preDetails = preProductDetailRepository.findByPreId(preId);

        for (FoodPreProductDetailItem preDetail : preDetails) {
            Float detailWeight = preDetail.getWeight() != null ? preDetail.getWeight() : 0f;
            double adjustedWeight = detailWeight * ratio;

            if (Boolean.TRUE.equals(preDetail.getComponentKb())) {
                // 仕込品内の仕込品（ネスト）: 再帰的に展開
                Integer nestedPreId = preDetail.getDetailPreId();
                if (nestedPreId != null && !visitedPreIds.contains(nestedPreId)) {
                    expandPreProduct(nestedPreId, adjustedWeight, visitedPreIds, expandedList);
                }
            } else {
                // 原材料: 展開リストに追加
                addPreProductDetailIngredient(preDetail, adjustedWeight, expandedList);
            }
        }
    }

    /**
     * 半完成品明細の原材料を展開リストに追加
     */
    private void addRawMaterialIngredient(FoodSemiFinishedProductDetail detail,
                                           Double weight, List<ExpandedIngredient> expandedList) {
        Integer foodId = detail.getDetailFoodId();
        if (foodId == null || weight <= 0) {
            return;
        }

        // ラベル表示名を決定（labelDisplayName > displayName > foodName）
        String labelDisplayName = detail.getLabelDisplayName();
        if (labelDisplayName == null || labelDisplayName.isEmpty()) {
            labelDisplayName = detail.getDetailFoodName();
        }
        if (labelDisplayName == null || labelDisplayName.isEmpty()) {
            // DBから原材料情報を取得
            FoodRawMaterial rawMaterial = rawMaterialRepository.findById(foodId).orElse(null);
            if (rawMaterial != null) {
                labelDisplayName = rawMaterial.getDisplayName();
                if (labelDisplayName == null || labelDisplayName.isEmpty()) {
                    labelDisplayName = rawMaterial.getFoodName();
                }
            }
        }

        if (labelDisplayName == null || labelDisplayName.isEmpty()) {
            labelDisplayName = "不明な原材料";
        }

        Boolean isComposite = detail.getCompositeRawMaterialsKb() != null
            ? detail.getCompositeRawMaterialsKb() : false;

        expandedList.add(new ExpandedIngredient(foodId, labelDisplayName, weight, isComposite));
    }

    /**
     * 仕込品明細の原材料を展開リストに追加
     */
    private void addPreProductDetailIngredient(FoodPreProductDetailItem detail,
                                                Double weight, List<ExpandedIngredient> expandedList) {
        Integer foodId = detail.getDetailFoodId();
        if (foodId == null || weight <= 0) {
            return;
        }

        // ラベル表示名を決定
        String labelDisplayName = detail.getLabelDisplayName();
        if (labelDisplayName == null || labelDisplayName.isEmpty()) {
            labelDisplayName = detail.getDetailFoodName();
        }
        if (labelDisplayName == null || labelDisplayName.isEmpty()) {
            // DBから原材料情報を取得
            FoodRawMaterial rawMaterial = rawMaterialRepository.findById(foodId).orElse(null);
            if (rawMaterial != null) {
                labelDisplayName = rawMaterial.getDisplayName();
                if (labelDisplayName == null || labelDisplayName.isEmpty()) {
                    labelDisplayName = rawMaterial.getFoodName();
                }
            }
        }

        if (labelDisplayName == null || labelDisplayName.isEmpty()) {
            labelDisplayName = "不明な原材料";
        }

        Boolean isComposite = detail.getCompositeRawMaterialsKb() != null
            ? detail.getCompositeRawMaterialsKb() : false;

        expandedList.add(new ExpandedIngredient(foodId, labelDisplayName, weight, isComposite));
    }

    /**
     * 展開済み原材料を表示名で合算し、重量順にソート
     */
    public List<AggregatedIngredient> aggregateByDisplayName(List<ExpandedIngredient> expandedList) {
        // 表示名でグループ化して重量を合算
        Map<String, AggregatedData> aggregated = new HashMap<>();

        for (ExpandedIngredient ingredient : expandedList) {
            String name = ingredient.getLabelDisplayName();
            AggregatedData existing = aggregated.get(name);
            if (existing != null) {
                existing.weight += ingredient.getEffectiveWeight();
                existing.isComposite = existing.isComposite || ingredient.getIsComposite();
            } else {
                aggregated.put(name, new AggregatedData(
                    ingredient.getEffectiveWeight(),
                    ingredient.getIsComposite()
                ));
            }
        }

        // 重量順にソート
        return aggregated.entrySet().stream()
            .sorted((a, b) -> Double.compare(b.getValue().weight, a.getValue().weight))
            .map(e -> new AggregatedIngredient(
                e.getKey(),
                e.getValue().weight,
                e.getValue().isComposite
            ))
            .collect(Collectors.toList());
    }

    private static class AggregatedData {
        double weight;
        boolean isComposite;

        AggregatedData(double weight, boolean isComposite) {
            this.weight = weight;
            this.isComposite = isComposite;
        }
    }

    /**
     * 合算済み原材料情報
     */
    public static class AggregatedIngredient {
        private String name;
        private Double weight;
        private Boolean isComposite;

        public AggregatedIngredient(String name, Double weight, Boolean isComposite) {
            this.name = name;
            this.weight = weight;
            this.isComposite = isComposite;
        }

        public String getName() { return name; }
        public Double getWeight() { return weight; }
        public Boolean getIsComposite() { return isComposite; }
    }
}
