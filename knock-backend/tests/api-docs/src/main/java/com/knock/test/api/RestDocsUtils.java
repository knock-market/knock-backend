package com.knock.test.api;

import org.springframework.core.MethodParameter;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class RestDocsUtils {

	public static OperationRequestPreprocessor requestPreprocessor() {
		return Preprocessors.preprocessRequest(
				Preprocessors.modifyUris().scheme("http").host("dev.knock.com").removePort(),
				Preprocessors.prettyPrint());
	}

	public static OperationResponsePreprocessor responsePreprocessor() {
		return Preprocessors.preprocessResponse(Preprocessors.prettyPrint());
	}

	public static HandlerMethodArgumentResolver principalResolver(Object principal) {
		return new HandlerMethodArgumentResolver() {
			@Override
			public boolean supportsParameter(MethodParameter parameter) {
				return parameter.hasParameterAnnotation(AuthenticationPrincipal.class) ||
						parameter.getParameterAnnotation(AuthenticationPrincipal.class) != null;
			}

			@Override
			public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
					NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
				return principal;
			}
		};
	}

}
