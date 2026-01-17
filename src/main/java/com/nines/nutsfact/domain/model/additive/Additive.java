package com.nines.nutsfact.domain.model.additive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 添加物マスタエンティティ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Additive implements Serializable {

    private Integer additiveId;
    private Integer businessAccountId;
    private String additiveCode;
    private String substanceName;       // 物質名
    private String simplifiedName;      // 簡略名・類別名
    private Integer purposeCategory;    // 用途区分（AdditivePurposeCategory）
    private Integer collectiveName;     // 一括名（AdditiveCollectiveName）
    private Boolean requiresPurposeDisplay; // 用途名併記必要
    private String description;
    private Boolean isActive;
    private Date createDate;
    private Date updateDate;

    /**
     * 用途区分Enumを取得
     */
    public AdditivePurposeCategory getPurposeCategoryEnum() {
        return AdditivePurposeCategory.fromCode(purposeCategory != null ? purposeCategory : 0);
    }

    /**
     * 一括名Enumを取得
     */
    public AdditiveCollectiveName getCollectiveNameEnum() {
        return AdditiveCollectiveName.fromCode(collectiveName);
    }

    /**
     * 表示用の名称を取得（一括名があれば一括名、なければ物質名）
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
}
