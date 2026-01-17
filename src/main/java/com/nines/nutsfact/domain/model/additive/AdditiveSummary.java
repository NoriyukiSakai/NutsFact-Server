package com.nines.nutsfact.domain.model.additive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 添加物集計情報
 * 半完成品の構成材料から集計された添加物情報
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdditiveSummary implements Serializable {

    private List<AdditiveItem> additives;
    private LocalDateTime calculatedAt;

    /**
     * 添加物項目
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdditiveItem implements Serializable {
        private Integer additiveId;
        private String substanceName;       // 物質名
        private String simplifiedName;      // 簡略名
        private Integer purposeCategory;    // 用途区分
        private Integer collectiveName;     // 一括名
        private Boolean requiresPurposeDisplay;
        private Double totalWeight;         // 合計使用量
        private Integer exemptionType;      // 表示免除区分
        private String allergenOrigin;      // アレルゲン由来

        /**
         * 表示用名称を取得
         */
        public String getDisplayName() {
            if (collectiveName != null) {
                String collective = AdditiveCollectiveName.getDisplayName(collectiveName);
                if (collective != null) {
                    return collective;
                }
            }
            return simplifiedName != null ? simplifiedName : substanceName;
        }

        /**
         * 表示文字列を生成（用途名併記対応）
         */
        public String toDisplayString() {
            String name = getDisplayName();
            if (Boolean.TRUE.equals(requiresPurposeDisplay) && purposeCategory != null && purposeCategory > 0) {
                String purposeName = AdditivePurposeCategory.getDisplayName(purposeCategory);
                return purposeName + "（" + name + "）";
            }
            return name;
        }

        /**
         * 表示が必要かどうか
         */
        public boolean isDisplayRequired() {
            return !AdditiveExemptionType.isExempted(exemptionType != null ? exemptionType : 0);
        }
    }

    /**
     * 表示が必要な添加物のみを取得（使用量順）
     */
    public List<AdditiveItem> getDisplayableAdditives() {
        if (additives == null) {
            return List.of();
        }
        return additives.stream()
            .filter(AdditiveItem::isDisplayRequired)
            .sorted((a, b) -> Double.compare(
                b.getTotalWeight() != null ? b.getTotalWeight() : 0,
                a.getTotalWeight() != null ? a.getTotalWeight() : 0))
            .toList();
    }

    /**
     * 添加物表示文字列を生成（スラッシュ区切り）
     */
    public String toDisplayString() {
        List<AdditiveItem> displayable = getDisplayableAdditives();
        if (displayable.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder("／");
        for (int i = 0; i < displayable.size(); i++) {
            if (i > 0) {
                sb.append("、");
            }
            AdditiveItem item = displayable.get(i);
            sb.append(item.toDisplayString());
            if (item.getAllergenOrigin() != null && !item.getAllergenOrigin().isEmpty()) {
                sb.append("（").append(item.getAllergenOrigin()).append("）");
            }
        }
        return sb.toString();
    }
}
