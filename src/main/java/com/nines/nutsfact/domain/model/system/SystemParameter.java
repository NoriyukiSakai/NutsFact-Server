package com.nines.nutsfact.domain.model.system;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * システムパラメータ
 */
@Data
public class SystemParameter {
    private Integer id;

    @JsonProperty("parameter_key")
    private String parameterKey;

    @JsonProperty("parameter_value")
    private String parameterValue;

    @JsonProperty("parameter_type")
    private Integer parameterType;  // 1:String 2:Integer 3:Boolean

    private String description;

    @JsonProperty("create_date")
    private LocalDateTime createDate;

    @JsonProperty("last_update_date")
    private LocalDateTime lastUpdateDate;

    /**
     * パラメータ値をInteger型で取得
     */
    public Integer getIntValue() {
        try {
            return Integer.parseInt(parameterValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * パラメータ値をBoolean型で取得
     */
    public Boolean getBooleanValue() {
        return "true".equalsIgnoreCase(parameterValue) || "1".equals(parameterValue);
    }
}
