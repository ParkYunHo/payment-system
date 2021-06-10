package com.kakaopay.payment.model.dto;

import lombok.Data;

@Data
public class ResultDTO {
	private String timeStamp;
	private String status;
	private String msg;	
}
