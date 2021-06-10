package com.kakaopay.payment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.kakaopay.payment.model.dto.ResponseDTO;
import com.kakaopay.payment.model.dto.RequestDTO;
import com.kakaopay.payment.service.PaymentService;

class PaymentTest extends CommonTest{
	@Autowired
	private PaymentService paymentService;
	
	@Test
	public void paymentTransactionTest() {
		RequestDTO.PaymentReqDTO req = super.getPaymentParam();
		
		Object response = paymentService.paymentTransaction(req);
		assertTrue(response instanceof ResponseDTO.PaymentIdDTO);
	}
	
	@Test
	public void cancelTransactionTest() {
		RequestDTO.CancelReqDTO cancelInfo = super.getCancelParam();
		
		Object response = paymentService.cancelTransaction(cancelInfo);
		assertTrue(response instanceof ResponseDTO.PaymentIdDTO);
	}
	
	@Test
	public void findTransactionTest() {
		RequestDTO.FindReqDTO findInfo = super.getFindParam();
		
		Object response = paymentService.findTransaction(findInfo);
		assertTrue(response instanceof ResponseDTO.PaymentInfoDTO);
	}
}
