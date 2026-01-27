package com.knock;

import com.knock.core.config.TestMockRedisConfig;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

/**
 * 테스트 포함 및 test 프로파일 사용
 */
@Tag("context")
@ActiveProfiles("test")
@SpringBootTest
@Import(TestMockRedisConfig.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public abstract class ContextTest {

}
