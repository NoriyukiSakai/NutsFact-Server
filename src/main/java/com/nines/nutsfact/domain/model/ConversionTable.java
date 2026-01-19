package com.nines.nutsfact.domain.model;

import lombok.Data;

/**
 * 重量変換テーブル（システム共通シードデータ）
 */
@Data
public class ConversionTable {
    private Integer id;
    private Integer kubun;  // 1:液状 2:粉類（調味料） 3:粉類（飲料） 4:固形・半固形
    private String foodName;
    private Float rateOfConversion;  // 100ccに対するg
}
