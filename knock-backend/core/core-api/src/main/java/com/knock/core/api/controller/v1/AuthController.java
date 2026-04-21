package com.knock.core.api.controller.v1;

import com.knock.auth.AuthService;
import com.knock.auth.SessionAuthService;
import com.knock.core.api.controller.v1.request.AuthLoginRequestDto;
import com.knock.core.support.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@Value("${GOOGLE_LOGIN_SUCCESS_REDIRECT_URI:http://localhost:3000/#/home}")
	private String googleLoginSuccessRedirectUri;

	@PostMapping("/api/v1/auth/login")
	public ApiResponse<?> login(@RequestBody AuthLoginRequestDto request, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		authService.login(new SessionAuthService.LoginRequestData(request.email(), request.password()), httpRequest,
				httpResponse);
		return ApiResponse.success();
	}

	@PostMapping("/api/v1/auth/logout")
	public ApiResponse<?> logout(HttpServletRequest request) {
		authService.logout(request);
		return ApiResponse.success();
	}

	@GetMapping("/api/v1/auth/social/google/start")
	public ResponseEntity<Void> googleLogin(@RequestParam(required = false) String next, HttpServletRequest request) {
		String authorizationUrl = authService.getGoogleAuthorizationUrl(next, request);
		return ResponseEntity.status(302).header(HttpHeaders.LOCATION, authorizationUrl).build();
	}

	@GetMapping("/api/v1/auth/social/google/callback")
	public ResponseEntity<Void> googleCallback(@RequestParam String code, @RequestParam String state,
			HttpServletRequest request, HttpServletResponse response) {
		String next = authService.loginWithGoogle(code, state, request, response);
		return ResponseEntity.status(302).header(HttpHeaders.LOCATION, resolveSuccessRedirectUrl(next)).build();
	}

	private String resolveSuccessRedirectUrl(String next) {
		if (next == null || next.isBlank()) {
			return googleLoginSuccessRedirectUri;
		}
		int hashRouterSeparatorIndex = googleLoginSuccessRedirectUri.indexOf("/#");
		if (hashRouterSeparatorIndex < 0) {
			return googleLoginSuccessRedirectUri;
		}
		String frontendBaseUrl = googleLoginSuccessRedirectUri.substring(0, hashRouterSeparatorIndex);
		return frontendBaseUrl + "/#" + next;
	}

}
