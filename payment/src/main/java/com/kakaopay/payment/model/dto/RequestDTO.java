package com.kakaopay.payment.model.dto;

import lombok.Data;

public class RequestDTO {
	@Data
	public static class PaymentReqDTO{
		private Long cardNo;
		private Long expiryDate;
		private Long cvc;
		private Long installMonths;
		private Long price;
		private Long vat;
	}
	
	@Data
	public static class CancelReqDTO {
		private String mngId;
		private Long price;
		private Long vat;
	}
	
	@Data
	public static class FindReqDTO {
		private String mngId;
	}
}
