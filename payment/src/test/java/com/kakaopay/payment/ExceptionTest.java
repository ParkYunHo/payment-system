package com.kakaopay.payment;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.kakaopay.payment.config.exception.CustomException;
import com.kakaopay.payment.model.constant.ExceptionType;
import com.kakaopay.payment.model.dto.RequestDTO;
import com.kakaopay.payment.service.PaymentService;

class ExceptionTest extends CommonTest {

	@Autowired
	PaymentService paymentService;
	
	@Test
	public void paramNullTest() {
		try {
			RequestDTO.PaymentReqDTO req = super.getPaymentParam();
			req.setCardNo(null);
			
			paymentService.paymentTransaction(req);	
		}catch(CustomException e) {
			assertTrue(e.getError() == ExceptionType.PARAM_NULL_ERROR);
		}
	}
	
	@Test
	public void paramLensTest() {
		try {
			RequestDTO.PaymentReqDTO req = super.getPaymentParam();
			req.setCvc(1111L);
			
			paymentService.paymentTransaction(req);	
		}catch(CustomException e) {
			assertTrue(e.getError() == ExceptionType.PARAM_LENS_ERROR);
		}
	}
	
	@Test
	public void paramDateTest() {
		try {
			RequestDTO.PaymentReqDTO req = super.getPaymentParam();
			req.setExpiryDate(1321L);
			
			paymentService.paymentTransaction(req);	
		}catch(CustomException e) {
			assertTrue(e.getError() == ExceptionType.PARAM_DATE_ERROR);
		}
	}
	
	@Test
	public void paramRangeTest() {
		try {
			RequestDTO.PaymentReqDTO req = super.getPaymentParam();
			req.setPrice(1L);
			
			paymentService.paymentTransaction(req);	
		}catch(CustomException e) {
			assertTrue(e.getError() == ExceptionType.PARAM_RANGE_ERROR);
		}
	}
	
	@Test
	public void paramNoIdTest() {
		try {
			RequestDTO.CancelReqDTO req = super.getCancelParam();
			req.setMngId("????????????????");
			
			paymentService.cancelTransaction(req);	
		}catch(CustomException e) {
			assertTrue(e.getError() == ExceptionType.PARAM_NOID_ERROR);
		}
	}
	
	@Test
	public void paramNoPayTest() {
		try {
			RequestDTO.CancelReqDTO req = super.getCancelParam();
			req.setMngId(super.getCancelID().getMngId());
			
			paymentService.cancelTransaction(req);
		}catch(CustomException e) {
			assertTrue(e.getError() == ExceptionType.PARAM_NOPAY_ERROR);
		}
	}
	
	@Test
	public void priceVatRangeTest() {
		try {
			RequestDTO.CancelReqDTO req = super.getCancelParam();
			req.setVat(1100L);
			
			paymentService.cancelTransaction(req);
		}catch(CustomException e) {
			assertTrue(e.getError() == ExceptionType.PRICE_VATRANGE_ERROR);
		}
	}
	
	@Test
	public void pricePriceLickTest() {
		try {
			RequestDTO.CancelReqDTO req = super.getCancelParam();
			req.setPrice(12000L);
			
			paymentService.cancelTransaction(req);
		}catch(CustomException e) {
			assertTrue(e.getError() == ExceptionType.PRICE_PRICELICK_ERROR);
		}
	}
	
	@Test
	public void pricePayRangeTest() {
		try {
			RequestDTO.PaymentReqDTO req = super.getPaymentParam();
			req.setVat(12000L);
			
			paymentService.paymentTransaction(req);
		}catch(CustomException e) {
			assertTrue(e.getError() == ExceptionType.PRICE_PAYRANGE_ERROR);
		}
	}
	
	@Test
	public void priceCancelRangeTest() {
		try {
			RequestDTO.CancelReqDTO req = super.getCancelParam();
			req.setVat(1300L);
			
			paymentService.cancelTransaction(req);
		}catch(CustomException e) {
			assertTrue(e.getError() == ExceptionType.PRICE_CANCELRANGE_ERROR);
		}
	}
	
	@Test
	public void pricePayLickTest() {
		try {
			RequestDTO.CancelReqDTO req = super.getCancelParam();
			req.setPrice(10500L);
			
			paymentService.cancelTransaction(req);
		}catch(CustomException e) {
			assertTrue(e.getError() == ExceptionType.PRICE_PAYLICK_ERROR);
		}
	}
}
