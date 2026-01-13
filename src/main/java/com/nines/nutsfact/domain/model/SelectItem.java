package com.nines.nutsfact.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 選択リスト用アイテム
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectItem implements Serializable {

    private Integer id;
    private String name;
    private String no;
    private String description;
}
