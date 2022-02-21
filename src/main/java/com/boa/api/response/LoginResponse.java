package com.boa.api.response;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class LoginResponse extends GenericResponse {

    private String token;
    private String expiration;
    private String username;
    private String lastConnection;
    private String userRole;
    private String idMerchant;
    private String merchantName;
    private Access access;
    private Map<String, Object> echec = new HashMap<>();
}
