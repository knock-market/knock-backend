package com.knock.auth.google;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GoogleOAuthProperties {

	private final String clientId;

	private final String clientSecret;

	private final String redirectUri;

	public GoogleOAuthProperties(@Value("${GOOGLE_CLIENT_ID}") String clientId,
			@Value("${GOOGLE_CLIENT_SECRET}") String clientSecret,
			@Value("${GOOGLE_REDIRECT_URI}") String redirectUri) {
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.redirectUri = redirectUri;
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public String getRedirectUri() {
		return redirectUri;
	}

	public boolean isConfigured() {
		return !clientId.isBlank() && !clientSecret.isBlank() && !redirectUri.isBlank();
	}

}
