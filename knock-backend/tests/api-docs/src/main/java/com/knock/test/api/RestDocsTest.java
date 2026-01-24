package com.knock.test.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

@Tag("restdocs")
@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocsTest {

	protected MockMvcRequestSpecification mockMvc;

	private RestDocumentationContextProvider restDocumentation;

	@BeforeEach
	public void setUp(RestDocumentationContextProvider restDocumentation) {
		this.restDocumentation = restDocumentation;
	}

	protected MockMvcRequestSpecification restDocGiven() {
		return mockMvc;
	}

	protected MockMvcRequestSpecification mockController(Object controller, Object advice,
			HandlerMethodArgumentResolver... resolvers) {
		MockMvc mockMvc = createMockMvc(controller, advice, resolvers);
		return RestAssuredMockMvc.given().mockMvc(mockMvc);
	}

	private MockMvc createMockMvc(Object controller, Object advice, HandlerMethodArgumentResolver... resolvers) {
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper());

		return MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(advice)
				.apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
				.setMessageConverters(converter)
				.setCustomArgumentResolvers(resolvers)
				.build();
	}

	private ObjectMapper objectMapper() {
		return new ObjectMapper().registerModule(new JavaTimeModule())
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
				.disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS);
	}

}