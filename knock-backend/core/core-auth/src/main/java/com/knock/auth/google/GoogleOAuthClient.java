package com.knock.auth.google;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class GoogleOAuthClient {

	private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";

	private static final String USERINFO_ENDPOINT = "https://openidconnect.googleapis.com/v1/userinfo";

	private final RestClient restClient;

	public GoogleOAuthClient(RestClient.Builder restClientBuilder) {
		this.restClient = restClientBuilder.build();
	}

	public GoogleTokenResponse getAccessToken(String code, GoogleOAuthProperties properties) {
		MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
		request.add("code", code);
		request.add("client_id", properties.getClientId());
		request.add("client_secret", properties.getClientSecret());
		request.add("redirect_uri", properties.getRedirectUri());
		request.add("grant_type", "authorization_code");

		return restClient.post()
			.uri(TOKEN_ENDPOINT)
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(request)
			.retrieve()
			.body(GoogleTokenResponse.class);
	}

	public GoogleUserInfoResponse getUserInfo(String accessToken) {
		return restClient.get()
			.uri(USERINFO_ENDPOINT)
			.headers(headers -> headers.setBearerAuth(accessToken))
			.retrieve()
			.body(GoogleUserInfoResponse.class);
	}

	public record GoogleTokenResponse(String access_token) {
	}

	public record GoogleUserInfoResponse(String sub, String email, Boolean email_verified, String name,
			String picture) {
	}

}
