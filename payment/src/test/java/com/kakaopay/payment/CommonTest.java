package com.kakaopay.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.kakaopay.payment.config.exception.CustomException;
import com.kakaopay.payment.model.dto.RequestDTO;
import com.kakaopay.payment.model.dto.ResponseDTO;
import com.kakaopay.payment.service.PaymentService;

@SpringBootTest
class CommonTest {

	@Autowired
	private PaymentService paymentService;
	
	public RequestDTO.PaymentReqDTO getPaymentParam(){
		RequestDTO.PaymentReqDTO req = new RequestDTO.PaymentReqDTO();
		req.setCardNo(1234567890123453L);
		req.setExpiryDate(1125L);
		req.setCvc(777L);
		req.setInstallMonths(0L);
		req.setPrice(11000L);
		req.setVat(1000L);
		
		return req;
	}
	
	public RequestDTO.CancelReqDTO getCancelParam() {
		ResponseDTO.PaymentIdDTO payment = getPaymentID();
		
		RequestDTO.CancelReqDTO cancelInfo = new RequestDTO.CancelReqDTO();
		cancelInfo.setMngId(payment.getMngId());
		cancelInfo.setPrice(1200L);
		cancelInfo.setVat(100L);
		
		return cancelInfo;
	}
	
	public RequestDTO.FindReqDTO getFindParam(){
		ResponseDTO.PaymentIdDTO payment = getPaymentID();
		
		RequestDTO.FindReqDTO findInfo = new RequestDTO.FindReqDTO();
		findInfo.setMngId(payment.getMngId());	
		
		return findInfo; 
	}
	
	public ResponseDTO.PaymentIdDTO getPaymentID(){
		return paymentService.paymentTransaction(getPaymentParam());
	}
	
	public ResponseDTO.PaymentIdDTO getCancelID(){
		return paymentService.cancelTransaction(getCancelParam());
	}
	
	public Object paymentThreadTest(RequestDTO.PaymentReqDTO req) {
		try {
			return paymentService.paymentTransaction(req);
		}catch(CustomException e) {
			return e;
		}
	}
	
	public Object cancelThreadTest(RequestDTO.CancelReqDTO req) {
		try {
			return paymentService.cancelTransaction(req);
		}catch(CustomException e) {
			return e;
		}
	}
}
