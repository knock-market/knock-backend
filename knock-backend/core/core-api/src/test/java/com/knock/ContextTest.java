package com.knock;

import com.knock.core.v1.TestRedisConfig;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

@Tag("context")
@ActiveProfiles("test")
@SpringBootTest
@Import(TestRedisConfig.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public abstract class ContextTest {
}
