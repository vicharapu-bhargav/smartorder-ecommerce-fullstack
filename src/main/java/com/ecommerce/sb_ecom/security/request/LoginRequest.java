package com.ecommerce.sb_ecom.security.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

public class LoginRequest {

    @Getter
    @NotBlank
    private String username;

    @Getter
    @NotBlank
    private String password;


}
