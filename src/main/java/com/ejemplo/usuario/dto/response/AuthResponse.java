package com.ejemplo.usuario.dto.response;

import lombok.Data;

@Data
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";
    private Long expiresIn;
    private UserResponse user;

}

