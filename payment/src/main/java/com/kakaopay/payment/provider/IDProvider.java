package com.kakaopay.payment.provider;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import com.kakaopay.payment.config.exception.CustomException;
import com.kakaopay.payment.model.constant.ExceptionType;
import com.kakaopay.payment.model.constant.IDType;
import com.kakaopay.payment.model.constant.NumberType;
import com.kakaopay.payment.model.dto.ResponseDTO;
import com.kakaopay.payment.model.entity.Payment;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IDProvider {
	
	private final CardInfoProvider cardInfoProvider;
	
	public String getMngId() throws CustomException {
		try {
			return new StringBuffer()
					.append(new SimpleDateFormat("yyMMddHHmmssSSS").format(new Date()))
					.append(RandomStringUtils.random(5, "0123456789abcdefghijklmnopqrstuvwxyzABCDEFHIJKLMNOPSRQTUVWXYZ"))
					.toString();	
		}catch(Exception e) {
			throw new CustomException(ExceptionType.CRT_MNGID_ERROR);
		}
	}
	
	public String getTransactionId(Payment payment) throws CustomException {
		try {
			ResponseDTO.CardInfoDTO cardInfo = cardInfoProvider.decrypt(payment.getCardInfo());
			String payMngId = payment.getPayMngId() == null ? null : payment.getPayMngId().getMngId();
			
			return new StringBuffer()
						.append(formatter(cardInfo.getCardNo(), IDType.CARD_NO))
						.append(formatter(payment.getInstallMonths(), IDType.INSTALL_MONTHS))
						.append(formatter(cardInfo.getExpiryDate(), IDType.EXPIRY_DATE))
						.append(formatter(cardInfo.getCvc(), IDType.CVC))
						.append(formatter(payment.getPrice(), IDType.PRICE))
						.append(formatter(payment.getVat(), IDType.VAT))
						.append(formatter(payMngId, IDType.PAY_MNG_ID))
						.append(formatter(payment.getCardInfo(), IDType.ENCRYPT_CARD_INFO))
						.append(formatter("", IDType.EXTRA))
						.insert(0, formatter(payment.getMngId(), IDType.MNG_ID))
						.insert(0, formatter(payment.getStatus(), IDType.STATUS))
						.insert(0, formatter(446, IDType.TOTAL_LENS))
						.toString();
		}catch(Exception e) {
			throw new CustomException(ExceptionType.CRT_TRANSACTIONID_ERROR);
		}
	}
	
	public <T> String formatter(T obj, IDType info) throws CustomException {
		String data = obj == null ? "" : obj.toString();
		String type = info.getType();
		int lens = info.getLens();
		
		switch(type) {
		case NumberType.NUMBER:
			return String.format("%"+lens+"d", Long.parseLong(data));
		case NumberType.NUMBER_O:
			return String.format("%0"+lens+"d", Long.parseLong(data));
		case NumberType.NUMBER_L:
			return String.format("%-"+lens+"d", Long.parseLong(data));
		case NumberType.STRING:
			return String.format("%"+lens+"s", data);
		default:
			throw new CustomException(ExceptionType.CRT_TRANSACTIONID_ERROR);
		}
	}
}
