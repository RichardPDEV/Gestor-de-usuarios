package com.ejemplo.usuario.dto.response;

import lombok.Data;

@Data
public class MessageResponse {

    private String message;
    private Boolean success;

    public MessageResponse(String message, Boolean success) {
        this.message = message;
        this.success = success;
    }

}

