package org.example.army.militaryauthenticate.service;

import org.example.army.militaryauthenticate.dto.LoginReq;
import org.example.army.militaryauthenticate.dto.LoginResp;
import jakarta.validation.Valid;

public interface LoginService {
    LoginResp login(@Valid LoginReq req);
}
