package com.kakaopay.payment.service;

import org.springframework.stereotype.Service;

import com.kakaopay.payment.config.exception.CustomException;
import com.kakaopay.payment.model.constant.ExceptionType;
import com.kakaopay.payment.model.constant.PaymentType;
import com.kakaopay.payment.model.dto.RequestDTO;
import com.kakaopay.payment.model.entity.Payment;
import com.kakaopay.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidationService {
	
	private final PaymentRepository paymentRepo;
	
	public void checkPaymentRequest(RequestDTO.PaymentReqDTO paymentInfo) throws CustomException {
		if(paymentInfo == null)
			throw new CustomException(ExceptionType.PARAM_NULL_ERROR);
		
		if(		
				paymentInfo.getCardNo() == null || 
				paymentInfo.getCvc() == null || 
				paymentInfo.getExpiryDate() == null || 
				paymentInfo.getInstallMonths() == null || 
				paymentInfo.getPrice() == null
			)
			throw new CustomException(ExceptionType.PARAM_NULL_ERROR);
		
		int digits = getDigits(paymentInfo.getCardNo());
		if(digits < 10 || digits > 16)
			throw new CustomException(ExceptionType.PARAM_LENS_ERROR);
		
		digits = getDigits(paymentInfo.getCvc());
		if(digits != 3)
			throw new CustomException(ExceptionType.PARAM_LENS_ERROR);
		
		digits = getDigits(paymentInfo.getExpiryDate());
		int month = (int)(paymentInfo.getExpiryDate()/100);
		if(digits != 4)
			throw new CustomException(ExceptionType.PARAM_LENS_ERROR);
		if(month < 1 || month > 12)
			throw new CustomException(ExceptionType.PARAM_DATE_ERROR);
		
		if(paymentInfo.getInstallMonths() < 0 || paymentInfo.getInstallMonths() > 12)
			throw new CustomException(ExceptionType.PARAM_DATE_ERROR);
		
		if(paymentInfo.getPrice() < 100 || paymentInfo.getPrice() > 1000000000)
			throw new CustomException(ExceptionType.PARAM_RANGE_ERROR);

		// VAT가 null인 경우는 비교안하고 종료
		if(paymentInfo.getVat() != null) {
			if(paymentInfo.getPrice() < paymentInfo.getVat())
				throw new CustomException(ExceptionType.PRICE_PAYRANGE_ERROR);	
		}
	}
	
	public Payment checkCancelRequest(RequestDTO.CancelReqDTO cancelInfo) throws CustomException {
		if(cancelInfo == null)
			throw new CustomException(ExceptionType.PARAM_NULL_ERROR);
		if(cancelInfo.getMngId() == null || cancelInfo.getPrice() == null)
			throw new CustomException(ExceptionType.PARAM_NULL_ERROR);
		if(cancelInfo.getVat() != null) {
			if(cancelInfo.getPrice() < cancelInfo.getVat())
				throw new CustomException(ExceptionType.PRICE_CANCELRANGE_ERROR);
		}
		
		Payment payment = paymentRepo.findByMngId(cancelInfo.getMngId()); 
		
		if(payment == null)
			throw new CustomException(ExceptionType.PARAM_NOID_ERROR);
		if(payment.getStatus().equals(PaymentType.PAYMENT) == false)
			throw new CustomException(ExceptionType.PARAM_NOPAY_ERROR);
		
		return payment;
	}
	
	public void checkCancelPrice(RequestDTO.CancelReqDTO cancelInfo, Payment payment) throws CustomException {
		// 취소금액이 남은 금액보다 큰지 여부
		if(cancelInfo.getPrice() > payment.getPrice())
			throw new CustomException(ExceptionType.PRICE_PRICELICK_ERROR);
		
		// 취소VAT가 남은 VAT보다 큰지 여부
		if(cancelInfo.getVat() > payment.getVat())
			throw new CustomException(ExceptionType.PRICE_VATRANGE_ERROR);
		
		// 취소시 남은 결제금액보다 남은 VAT가 더 큰지 여부
		Long remainPrice = payment.getPrice() - cancelInfo.getPrice();
		Long remainVAT = payment.getVat() - cancelInfo.getVat();
		if(remainVAT > remainPrice)
			throw new CustomException(ExceptionType.PRICE_PAYLICK_ERROR);
	}
	
	public void checkFindRequest(RequestDTO.FindReqDTO findInfo) throws CustomException {
		if(findInfo == null)
			throw new CustomException(ExceptionType.PARAM_NULL_ERROR);
		if(findInfo.getMngId() == null)
			throw new CustomException(ExceptionType.PARAM_NULL_ERROR);
		if(paymentRepo.existsById(findInfo.getMngId()) == false)
			throw new CustomException(ExceptionType.PARAM_NOID_ERROR);
	}
	
	public <T> int getDigits(T param) {
		return String.valueOf(param).length();
	}
}
