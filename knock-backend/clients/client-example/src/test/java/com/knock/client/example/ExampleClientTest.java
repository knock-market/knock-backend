package com.knock.client.example;

import com.knock.client.ClientExampleContextTest;
import feign.RetryableException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ExampleClientTest extends ClientExampleContextTest {

	private final ExampleClient exampleClient;

	public ExampleClientTest(ExampleClient exampleClient) {
		this.exampleClient = exampleClient;
	}

	@Test
	public void shouldBeThrownExceptionWhenExample() {
		try {
			exampleClient.example("HELLO!");
		}
		catch (Exception e) {
			assertThat(e).isExactlyInstanceOf(RetryableException.class);
		}
	}

}
