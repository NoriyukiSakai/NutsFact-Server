package com.nines.nutsfact.api.v1.response;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class DeleteResponse implements Serializable {

    private String status;
    private Integer id;
    private String message;

    public static DeleteResponse success(Integer id) {
        return DeleteResponse.builder()
            .status("Success")
            .id(id)
            .build();
    }

    public static DeleteResponse error(String message) {
        return DeleteResponse.builder()
            .status("Error")
            .message(message)
            .build();
    }
}
