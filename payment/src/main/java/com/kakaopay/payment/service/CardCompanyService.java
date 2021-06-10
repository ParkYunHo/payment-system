package com.kakaopay.payment.service;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.kakaopay.payment.config.exception.CustomException;
import com.kakaopay.payment.model.constant.ExceptionType;
import com.kakaopay.payment.model.entity.CardCompany;
import com.kakaopay.payment.repository.CardCompanyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CardCompanyService {
	
	private final CardCompanyRepository cardCompanyRepo;
	
	@Transactional
	public void sendTransaction(String transactionId) throws CustomException {
		try {
			CardCompany cardCompany = new CardCompany();
			cardCompany.setTransactionId(transactionId);
			cardCompanyRepo.save(cardCompany);	
		}catch(Exception e) {
			throw new CustomException(ExceptionType.SEND_TRANSACTION_ERROR);
		}
	}
}
