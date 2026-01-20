package com.knock.core.api.controller.v1;

import com.knock.auth.AuthService;
import com.knock.auth.SessionAuthService;
import com.knock.core.api.controller.v1.request.AuthLoginRequestDto;
import com.knock.core.support.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/api/v1/auth/login")
	public ApiResponse<?> login(@RequestBody AuthLoginRequestDto request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		authService.login(new SessionAuthService.LoginRequestData(request.email(), request.password()), httpRequest, httpResponse);
		return ApiResponse.success();
	}

	@PostMapping("/api/v1/auth/logout")
	public ApiResponse<?> logout(HttpServletRequest request) {
		authService.logout(request);
		return ApiResponse.success();
	}

}
