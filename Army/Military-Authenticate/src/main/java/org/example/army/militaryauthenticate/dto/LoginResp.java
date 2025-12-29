package org.example.army.militaryauthenticate.dto;

import lombok.Data;

@Data
public class LoginResp {
    private String token;
    private String username;
    private Integer userType;
}
