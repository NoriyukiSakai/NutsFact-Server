package com.nines.nutsfact.domain.model.nutrition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * ミネラル成分
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionMinerals implements Serializable {

    private Float na;       // ナトリウム(mg)
    private Integer naFlag;

    private Float k;        // カリウム(mg)
    private Integer kFlag;

    private Float ca;       // カルシウム(mg)
    private Integer caFlag;

    private Float mg;       // マグネシウム(mg)
    private Integer mgFlag;

    private Float p;        // リン(mg)
    private Integer pFlag;

    private Float fe;       // 鉄(mg)
    private Integer feFlag;

    private Float zn;       // 亜鉛(mg)
    private Integer znFlag;

    private Float cu;       // 銅(mg)
    private Integer cuFlag;

    private Float mn;       // マンガン(mg)
    private Integer mnFlag;

    private Float idd;      // ヨウ素(μg)
    private Integer iddFlag;

    private Float se;       // セレン(μg)
    private Integer seFlag;

    private Float cr;       // クロム(mg)
    private Integer crFlag;

    private Float mo;       // モリブデン(mg)
    private Integer moFlag;
}
