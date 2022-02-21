package com.boa.api.request;

import lombok.Data;

@Data
public class LoginRequest {

    private String username;
    private String password;
}
