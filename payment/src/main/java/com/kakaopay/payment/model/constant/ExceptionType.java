package com.kakaopay.payment.model.constant;

import lombok.Getter;

public enum ExceptionType {
	SERVER_ERROR("001", "에러"),
	
	PARAM_NULL_ERROR("101", "필수 요청정보를 누락하였습니다."),
	PARAM_LENS_ERROR("102", "허용된 길이를 벗어났습니다."),
	PARAM_DATE_ERROR("103", "날짜형식이 올바르지 않습니다."),
	PARAM_RANGE_ERROR("104", "허용된 값의 범위를 벗어났습니다."),
	PARAM_NOID_ERROR("105", "존재하지 않는 관리번호입니다."),
	PARAM_NOPAY_ERROR("106", "결재정보가 없는 관리번호입니다."),
	
	PRICE_VATRANGE_ERROR("201", "취소 부가가치세가 남은 부가가치세보다 클 수 없습니다."),
	PRICE_PRICELICK_ERROR("202", "남은 금액이 부족합니다."),
	PRICE_PAYRANGE_ERROR("203", "부가가치세가 결제 금액보다 클 수 없습니다."),
	PRICE_CANCELRANGE_ERROR("204", "부가가치세가 취소 금액보다 클 수 없습니다."),
	PRICE_PAYLICK_ERROR("205", "취소후 남은 부가가치세가 남은 금액보다 클 수 없습니다."),
	
	LOCK_PAYMENT_ERROR("301", "하나의 카드번호로 동시에 결제할 수 없습니다."),
	LOCK_CANCEL_ERROR("302", "하나의 결제정보를 동시에 취소할 수 없습니다."),
	
	CRT_MNGID_ERROR("401", "관리번호 생성에 실패하였습니다."),
	CRT_TRANSACTIONID_ERROR("402", "트랜잭션번호 생성에 실패하였습니다."),

	CIPHER_ENCRYPT_ERROR("501", "카드정보를 암호화하는데 실패하였습니다."),
	CIPHER_DECRYPT_ERROR("501", "카드정보를 복호화하는데 실패하였습니다."),
	
	SEND_TRANSACTION_ERROR("601", "카드사에 정보를 전송하는데 실패하였습니다.");
	
	@Getter
	private final String code;
	
	@Getter
	private final String desc;
	
	ExceptionType(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}
}
