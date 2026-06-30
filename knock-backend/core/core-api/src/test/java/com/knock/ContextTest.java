package com.knock;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestConstructor;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 테스트 포함 및 test 프로파일 사용
 */
@Tag("context")
@ActiveProfiles("test")
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public abstract class ContextTest {

	private static final int REDIS_PORT = 6379;

	private static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
		.withExposedPorts(REDIS_PORT);

	static {
		redis.start();
	}

	@DynamicPropertySource
	static void redisProperties(DynamicPropertyRegistry registry) {
		registry.add("storage.redis.host", redis::getHost);
		registry.add("storage.redis.port", () -> redis.getMappedPort(REDIS_PORT));
		registry.add("storage.redis.password", () -> "");
		registry.add("storage.redis.ssl", () -> false);
	}

}
