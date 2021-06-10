package com.kakaopay.payment.config.exception;

import com.kakaopay.payment.model.constant.ExceptionType;

import lombok.Getter;

public class CustomException extends RuntimeException {
	@Getter
	private ExceptionType error;
	
	public CustomException(ExceptionType error) {
		this.error = error;
	}
}
