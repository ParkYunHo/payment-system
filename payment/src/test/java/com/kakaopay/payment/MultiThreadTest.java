package com.kakaopay.payment;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

import com.kakaopay.payment.config.exception.CustomException;
import com.kakaopay.payment.model.constant.ExceptionType;
import com.kakaopay.payment.model.dto.RequestDTO;

class MultiThreadTest extends CommonTest {

	@Test
	public void multiThreadPaymentTest() {
		List<ExceptionType> errorList = new ArrayList<ExceptionType>();
		RequestDTO.PaymentReqDTO req = super.getPaymentParam();
		
		Runnable thread = () -> {
			try {
				Object obj = super.paymentThreadTest(req);
				
				if(obj instanceof CustomException) {
					CustomException exception = (CustomException)obj;
					if(exception.getError() == ExceptionType.LOCK_PAYMENT_ERROR) errorList.add(exception.getError());
				}
			}catch(Throwable e) {}	
		};
		CompletableFuture
			.allOf(CompletableFuture.runAsync(thread), CompletableFuture.runAsync(thread))
			.join();
		
		assertFalse(errorList.isEmpty());
	}
	
	@Test
	public void multiThreadCancelTest() {
		List<ExceptionType> errorList = new ArrayList<ExceptionType>();
		RequestDTO.CancelReqDTO req = super.getCancelParam();
		
		Runnable thread = () -> {
			try {
				Object obj = super.cancelThreadTest(req);
				
				if(obj instanceof CustomException) {
					CustomException exception = (CustomException)obj;
					if(exception.getError() == ExceptionType.LOCK_CANCEL_ERROR) errorList.add(exception.getError());
				}
			}catch(Throwable e) {}	
		};
		CompletableFuture
			.allOf(CompletableFuture.runAsync(thread), CompletableFuture.runAsync(thread))
			.join();
		
		assertFalse(errorList.isEmpty());
	}
}
