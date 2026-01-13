package com.nines.nutsfact.api.v1.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeRoleRequest {
    @NotNull(message = "ユーザーIDは必須です")
    private Integer userId;

    @NotNull(message = "ユーザー権限は必須です")
    private Integer role;
}
