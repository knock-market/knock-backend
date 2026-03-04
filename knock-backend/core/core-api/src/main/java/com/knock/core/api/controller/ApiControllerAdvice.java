package com.knock.core.api.controller;

import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.core.support.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiControllerAdvice {

	private final Logger log = LoggerFactory.getLogger(getClass());

	/** 비즈니스 로직 예외처리 */
	@ExceptionHandler(CoreException.class)
	protected ResponseEntity<ApiResponse<?>> handleCoreException(CoreException e) {
		switch (e.getErrorType().getLogLevel()) {
			case ERROR -> log.error("CoreException : {}", e.getMessage(), e);
			case WARN -> log.warn("CoreException : {}", e.getMessage(), e);
			default -> log.info("CoreException : {}", e.getMessage(), e);
		}
		return new ResponseEntity<>(ApiResponse.error(e.getErrorType(), e.getData()), e.getErrorType().getStatus());
	}

	/** Validation 검증 예외처리 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException e) {
		log.warn("Validation Exception : {}", e.getMessage());

		Map<String, String> errors = new HashMap<>();
		e.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});

		return new ResponseEntity<>(ApiResponse.error(ErrorType.VALIDATION_ERROR, errors),
				ErrorType.VALIDATION_ERROR.getStatus());
	}

	@ExceptionHandler(ResponseStatusException.class)
	protected ResponseEntity<ApiResponse<?>> handleResponseStatusException(ResponseStatusException e) {
		log.warn("ResponseStatusException : {}", e.getMessage());

		HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
		ErrorType errorType = switch (status) {
			case BAD_REQUEST -> ErrorType.VALIDATION_ERROR;
			case UNAUTHORIZED -> ErrorType.AUTHENTICATION_FAILED;
			case FORBIDDEN -> ErrorType.FORBIDDEN;
			case CONFLICT -> ErrorType.DUPLICATE_EMAIL;
			default -> ErrorType.DEFAULT_ERROR;
		};

		return new ResponseEntity<>(ApiResponse.error(errorType, e.getReason()), status);
	}

	@ExceptionHandler({ NoResourceFoundException.class, NoHandlerFoundException.class })
	protected ResponseEntity<ApiResponse<?>> handleNotFoundException(Exception e) {
		log.debug("NotFound : {}", e.getMessage());
		return new ResponseEntity<>(ApiResponse.error(ErrorType.NOT_FOUND), ErrorType.NOT_FOUND.getStatus());
	}

	@ExceptionHandler(Exception.class)
	protected ResponseEntity<ApiResponse<?>> handleException(Exception e) {
		log.error("Exception : {}", e.getMessage(), e);
		return new ResponseEntity<>(ApiResponse.error(ErrorType.DEFAULT_ERROR, e.getMessage()),
				ErrorType.DEFAULT_ERROR.getStatus());
	}

}
