package com.kakaopay.payment.provider;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.kakaopay.payment.config.exception.CustomException;
import com.kakaopay.payment.model.constant.ExceptionType;
import com.kakaopay.payment.model.dto.RequestDTO;
import com.kakaopay.payment.model.dto.ResponseDTO;

@Component
public class CardInfoProvider {
	
	@Value("${cipher.algorithm}")
	private String algorithm;
	
	@Value("${cipher.secretkey}")
	private String secretkey;
	
	@Value("${cipher.separator}")
	private String separator;
	
	@Value("${cipher.charset}")
	private String charset;
	
	private Cipher cipher;
	private SecretKeySpec keyspec;
	
	
	@PostConstruct
	public void initCipher() throws CustomException {
		try {
			cipher = Cipher.getInstance(algorithm);
			keyspec = new SecretKeySpec(secretkey.getBytes(), algorithm);	
		}catch(Exception e) {
			throw new CustomException(ExceptionType.SERVER_ERROR);
		}
	}
	
	public String encypt(RequestDTO.PaymentReqDTO paymentInfo) throws CustomException {
		try {
			cipher.init(Cipher.ENCRYPT_MODE, keyspec);
			
			String combineCardInfo = new StringBuffer()
											.append(paymentInfo.getCardNo())
											.append(separator)
											.append(paymentInfo.getExpiryDate())
											.append(separator)
											.append(paymentInfo.getCvc())
											.toString();
			
			return new String(Base64.encodeBase64(cipher.doFinal(combineCardInfo.getBytes())), charset);	
		}catch(Exception e) {
			throw new CustomException(ExceptionType.CIPHER_ENCRYPT_ERROR);
		}
	}
	
	public ResponseDTO.CardInfoDTO decrypt(String encryptCardInfo) throws CustomException {
		try {
			cipher.init(Cipher.DECRYPT_MODE, keyspec);

			byte[] encryptBytes = Base64.decodeBase64(encryptCardInfo.getBytes());
			String splits[] = new String(cipher.doFinal(encryptBytes), charset).split(separator);
			
			ResponseDTO.CardInfoDTO cardInfo = new ResponseDTO.CardInfoDTO();
			cardInfo.setCardNo(Long.parseLong(splits[0]));
			cardInfo.setExpiryDate(Long.parseLong(splits[1]));
			cardInfo.setCvc(Long.parseLong(splits[2]));
			return cardInfo;
		}catch(Exception e) {
			throw new CustomException(ExceptionType.CIPHER_DECRYPT_ERROR);
		}
	}
}
