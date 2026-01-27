package com.knock;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

/**
 * 개발용 테스트 (테스트 포함 X)
 */
@Tag("develop")
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public abstract class DevelopTest {

}
