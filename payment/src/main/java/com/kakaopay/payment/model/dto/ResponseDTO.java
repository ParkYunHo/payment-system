package com.kakaopay.payment.model.dto;

import lombok.Data;

public class ResponseDTO {
	/**
	 * 결제/결제취소Response시 관리번호, StringID를 리턴하기 위한 DTO
	 */
	@Data
	public static class PaymentIdDTO {
		private String mngId;
		private String transactionId;
	}
	
	/**
	 * 조회Response시 결제데이터를 리턴하기 위한 DTO
	 */
	@Data
	public static class PaymentInfoDTO {
		private String mngId;
		private String status;
		private CardInfoDTO cardInfo;
		private PriceInfoDTO priceInfo;
	}
	
	/**
	 * 조회Response시 카드정보를 리턴하기 위한 DTO
	 */
	@Data
	public static class CardInfoDTO {
		private String cardNo;
		private Long expiryDate;
		private Long cvc;
	}
	
	/**
	 * 조회Response시 금액정보를 리턴하기 위한 DTO
	 */
	@Data
	public static class PriceInfoDTO {
		private Long price;
		private Long vat;
	}
}
