package com.kakaopay.payment.config.exception;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kakaopay.payment.model.constant.ExceptionType;
import com.kakaopay.payment.model.dto.ResultDTO;

import lombok.RequiredArgsConstructor;

@RestControllerAdvice(basePackages = "com.kakaopay.payment.controller")
@RequiredArgsConstructor
public class ExceptionAdvice {
	
	@ExceptionHandler(CustomException.class)
	public ResultDTO customExceptionAdvice(CustomException e) {
		return getFailResultStatus(e.getError());
	}
	
	private ResultDTO getFailResultStatus(ExceptionType except) {
		ResultDTO res = new ResultDTO();
		res.setTimeStamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		res.setStatus(except.getCode());
		res.setMsg(except.getDesc());
		
		return res;
	}
}
