package com.nines.nutsfact.api.v1.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SystemParameterUpdateRequest {
    @NotNull(message = "パラメータIDは必須です")
    private Integer id;

    @NotNull(message = "パラメータ値は必須です")
    @Size(max = 500, message = "パラメータ値は500文字以内で入力してください")
    private String parameterValue;
}
