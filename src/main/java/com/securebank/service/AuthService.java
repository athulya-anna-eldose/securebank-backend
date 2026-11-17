package com.securebank.service;

import com.securebank.dto.*;

public interface AuthService {

    String register(RegisterRequest request);

    LoginResponse login(LoginRequest request, String ipAddress);
}