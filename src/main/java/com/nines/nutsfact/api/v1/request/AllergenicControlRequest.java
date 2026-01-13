package com.nines.nutsfact.api.v1.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AllergenicControlRequest {
    @NotNull(message = "食品IDは必須です")
    private Integer foodId;

    private Boolean item1Val = false;  // えび
    private Boolean item2Val = false;  // かに
    private Boolean item3Val = false;  // 小麦
    private Boolean item4Val = false;  // そば
    private Boolean item5Val = false;  // 卵
    private Boolean item6Val = false;  // 乳
    private Boolean item7Val = false;  // 落花生
    private Boolean item8Val = false;  // アーモンド
    private Boolean item9Val = false;  // あわび
    private Boolean item10Val = false; // いか
    private Boolean item11Val = false; // いくら
    private Boolean item12Val = false; // オレンジ
    private Boolean item13Val = false; // カシューナッツ
    private Boolean item14Val = false; // キウイフルーツ
    private Boolean item15Val = false; // 牛肉
    private Boolean item16Val = false; // くるみ
    private Boolean item17Val = false; // ごま
    private Boolean item18Val = false; // さけ
    private Boolean item19Val = false; // さば
    private Boolean item20Val = false; // 大豆
    private Boolean item21Val = false; // 鶏肉
    private Boolean item22Val = false; // バナナ
    private Boolean item23Val = false; // 豚肉
    private Boolean item24Val = false; // まつたけ
    private Boolean item25Val = false; // もも
    private Boolean item26Val = false; // やまいも
    private Boolean item27Val = false; // りんご
    private Boolean item28Val = false; // ゼラチン
    private Boolean item29Val = false; // マカダミアナッツ
    private Boolean item30Val = false; // 予備
}
