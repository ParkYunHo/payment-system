package com.kakaopay.payment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.kakaopay.payment.config.exception.CustomException;
import com.kakaopay.payment.model.dto.RequestDTO;
import com.kakaopay.payment.model.dto.ResponseDTO;
import com.kakaopay.payment.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PaymentController {
	
	private final PaymentService paymentService;
	
	@PostMapping("/payment")
	public @ResponseBody ResponseDTO.PaymentIdDTO paymentTransaction(@RequestBody RequestDTO.PaymentReqDTO paymentInfo) throws CustomException {
		return paymentService.paymentTransaction(paymentInfo);
	}
	
	@PutMapping("/payment")
	public @ResponseBody ResponseDTO.PaymentIdDTO cancelTransaction(@RequestBody RequestDTO.CancelReqDTO cancelInfo) throws CustomException {
		return paymentService.cancelTransaction(cancelInfo);
	}
	
	@GetMapping("/payment")
	public @ResponseBody ResponseDTO.PaymentInfoDTO findTransaction(@RequestBody RequestDTO.FindReqDTO findInfo) throws CustomException {
		return paymentService.findTransaction(findInfo);
	}
}
