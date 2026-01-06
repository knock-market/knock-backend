package com.knock.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

	void login(SessionAuthService.LoginRequestData data, HttpServletRequest request, HttpServletResponse response);

	void logout(HttpServletRequest request);

}
