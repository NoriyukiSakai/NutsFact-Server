package com.nines.nutsfact.api.v1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 仕込品リクエストDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodPreProductRequest {

    private Integer preId;

    @NotBlank(message = "仕込品番号は必須です")
    private String preNo;

    @NotNull(message = "仕込品区分は必須です")
    private Integer preKind;

    @NotBlank(message = "仕込品名は必須です")
    private String preName;

    private String displayName;
    private Integer weightInputMode;
    private Integer classCategoryId;

    // 内容量
    private Float capacity;
    private Integer unit;

    // 栄養成分表示設定
    private Integer infUnit;
    private Float infVolume;
    private Integer infDisplay;
    private Float infEnergy;
    private Float infProtein;
    private Float infFat;
    private Float infCarbo;
    private Float infSugar;
    private Float infSodium;

    // 期限表示
    private Integer infLmtKind;
    private Boolean infLmtDateFlag;
    private Date infLmtDate;
    private String infStorageMethod;

    // コンタミネーション
    private Boolean infContamiFlag;
    private String infContamination;

    // その他
    private String placeOfOrigin;
    private String purpose;
    private Boolean isActive;

    // 明細
    private List<FoodPreProductDetailRequest> details;
}
