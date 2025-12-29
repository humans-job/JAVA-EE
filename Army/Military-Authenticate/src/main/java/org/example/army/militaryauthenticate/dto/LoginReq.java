package org.example.army.militaryauthenticate.dto;

import lombok.Data;

@Data
public class LoginReq {
    private Integer authType;
    private String username;
    private String password;
    private String usbKey;
    private String certSn;
    private String loginIp;
}
