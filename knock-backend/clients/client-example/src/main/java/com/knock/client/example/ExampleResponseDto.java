package com.knock.client.example;

import com.knock.client.example.model.ExampleClientResult;

record ExampleResponseDto(String exampleResponseValue) {
	ExampleClientResult toResult() {
		return new ExampleClientResult(exampleResponseValue);
	}
}
