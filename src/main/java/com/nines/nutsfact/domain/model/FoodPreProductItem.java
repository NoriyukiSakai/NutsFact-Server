package com.nines.nutsfact.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 仕込品エンティティ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodPreProductItem implements Serializable {

    private Integer preId;
    private Integer businessAccountId;
    private String preNo;
    private Integer preKind;            // 1:仕込品 2:中間加工品 3:完成品
    private String preName;
    private String displayName;
    private Integer weightInputMode;    // 1:重量入力モード 0:配合率モード
    private Integer classCategoryId;

    // 内容量
    private Float capacity;
    private Integer unit;

    // 栄養成分表示設定
    private Integer infUnit;            // 0:100g当たり 1:1個当たり
    private Float infVolume;
    private Integer infDisplay;         // 表記種別
    private Float infEnergy;
    private Float infProtein;
    private Float infFat;
    private Float infCarbo;
    private Float infSugar;
    private Float infSodium;

    // 期限表示
    private Integer infLmtKind;         // 0:賞味期限 1:消費期限
    private Boolean infLmtDateFlag;
    private Date infLmtDate;
    private String infStorageMethod;

    // コンタミネーション
    private Boolean infContamiFlag;
    private String infContamination;

    // 合計値
    private Float weightSum;
    private Float costPriceSum;
    private Integer detailCount;

    // その他
    private String placeOfOrigin;
    private String purpose;
    private Boolean isActive;

    // 明細（関連）
    private List<FoodPreProductDetailItem> details;
}
