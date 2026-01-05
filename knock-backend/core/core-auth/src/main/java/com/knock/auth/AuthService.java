package com.knock.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

	void login(String email, String rawPassword, HttpServletRequest request, HttpServletResponse response);

	void logout(HttpServletRequest request);

}
