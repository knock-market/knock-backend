package com.knock.storage.memory.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import java.time.Duration;

@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600)
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig {

	private final RedisProperties redisProperties;

	public RedisConfig(RedisProperties redisProperties) {
		this.redisProperties = redisProperties;
	}

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
		redisConfig.setHostName(redisProperties.getHost());
		redisConfig.setPort(redisProperties.getPort());

		if (redisProperties.getPassword() != null && !redisProperties.getPassword().isBlank()) {
			redisConfig.setPassword(redisProperties.getPassword());
		}

		LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = LettuceClientConfiguration.builder()
			.commandTimeout(Duration.ofMillis(redisProperties.getTimeout()));

		if (redisProperties.isSsl()) {
			builder.useSsl();
		}
		return new LettuceConnectionFactory(redisConfig, builder.build());
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate() {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory());
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		return redisTemplate;
	}

	@Bean
	public CookieSerializer cookieSerializer() {
		DefaultCookieSerializer serializer = new DefaultCookieSerializer();
		serializer.setCookieName("SESSION_ID");
		serializer.setCookiePath("/");
		serializer.setUseBase64Encoding(false);
		// serializer.setUseBase64Encoding(true);
		serializer.setUseHttpOnlyCookie(true);
		serializer.setUseSecureCookie(redisProperties.isSsl());
		serializer.setSameSite("Lax");
		// return new SesssionEncryptedCookieSerializer(serializer,
		// redisProperties.getEncryptionKey());
		return serializer;
	}

}