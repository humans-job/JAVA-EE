package edu.dto;

import lombok.Data;

@Data
public class LoginResp {
    private Long userId;
    private String username;
    private Integer userType;
}
