package com.kakaopay.payment.model.constant;

import lombok.Getter;

public enum IDType {
	CARD_NO("NUMBER_L", 20),
	INSTALL_MONTHS("NUMBER_O", 2),
	EXPIRY_DATE("NUMBER_L", 4),
	CVC("NUMBER_L", 3),
	PRICE("NUMBER", 10),
	VAT("NUMBER_O", 10),
	PAY_MNG_ID("STRING", 20),
	ENCRYPT_CARD_INFO("STRING", 300),
	EXTRA("STRING", 47),
	MNG_ID("STRING", 20),
	STATUS("STRING", 10),
	TOTAL_LENS("NUMBER", 4);
	
	@Getter
	private final String type;
	
	@Getter
	private final int lens;
	
	IDType(String type, int lens) {
		this.type = type;
		this.lens = lens;
	}
}
