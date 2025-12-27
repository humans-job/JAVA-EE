package edu.service;

import edu.dto.LoginReq;
import edu.dto.LoginResp;
import jakarta.validation.Valid;

public interface LoginService {
    LoginResp login(@Valid LoginReq req);
}
