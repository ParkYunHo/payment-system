package com.kakaopay.payment.service;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.kakaopay.payment.config.exception.CustomException;
import com.kakaopay.payment.model.constant.PaymentType;
import com.kakaopay.payment.model.dto.RequestDTO;
import com.kakaopay.payment.model.dto.ResponseDTO;
import com.kakaopay.payment.model.dto.ResponseDTO.CardInfoDTO;
import com.kakaopay.payment.model.entity.Payment;
import com.kakaopay.payment.provider.CardInfoProvider;
import com.kakaopay.payment.provider.IDProvider;
import com.kakaopay.payment.provider.LockProvider;
import com.kakaopay.payment.provider.MapperProvider;
import com.kakaopay.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
	
	private final CardCompanyService cardCompanyService;
	private final ValidationService validationService;
	
	private final MapperProvider mapperProvider;
	private final CardInfoProvider cardInfoProvider;
	private final IDProvider idProvider;
	private final LockProvider lockProvider;
	
	private final PaymentRepository paymentRepo;
	
	@Transactional
	public ResponseDTO.PaymentIdDTO paymentTransaction(RequestDTO.PaymentReqDTO paymentInfo) throws CustomException {
		String paymentKey = "";
		try {
			validationService.checkPaymentRequest(paymentInfo);
			
			paymentKey = lockProvider.getUniqueKey(PaymentType.PAYMENT, paymentInfo.getCardNo());
			lockProvider.paymentLock(paymentKey);
			
			Payment payment = new Payment(); 
			payment.setMngId(idProvider.getMngId());
			payment.setStatus(PaymentType.PAYMENT);
			payment.setPrice(paymentInfo.getPrice());
			payment.setVat(getVat(paymentInfo.getVat(), paymentInfo.getPrice()));
			payment.setInstallMonths(paymentInfo.getInstallMonths());
			payment.setCardInfo(cardInfoProvider.encypt(paymentInfo));
			paymentRepo.save(payment);
			
			// Response객체 데이터 저장
			ResponseDTO.PaymentIdDTO response = new ResponseDTO.PaymentIdDTO();
			response.setMngId(payment.getMngId());
			response.setTransactionId(idProvider.getTransactionId(payment));
			
			// 카드회사로 결제취소 정보 전송(트랜잭션ID 전송)
			cardCompanyService.sendTransaction(response.getTransactionId());
			return response;
		}catch(CustomException e){
			throw e;
		}finally {
			lockProvider.unlock(paymentKey);
		}
	}
	

	@Transactional
	public ResponseDTO.PaymentIdDTO cancelTransaction(RequestDTO.CancelReqDTO cancelInfo) throws CustomException {
		String cancelKey = "";
		try {
			Payment payment = validationService.checkCancelRequest(cancelInfo);
			
			cancelKey = lockProvider.getUniqueKey(PaymentType.CANCEL, cancelInfo.getMngId());
			lockProvider.cancelLock(cancelKey);
			
			cancelInfo.setVat(getCancelVat(payment, cancelInfo));
			validationService.checkCancelPrice(cancelInfo, payment);
			
			// 결제정보에 대해 취소금액,취소VAT만큼 감소후 Update
			Long remainPrice = payment.getPrice() - cancelInfo.getPrice();
			Long remainVat = payment.getVat() - cancelInfo.getVat();
			payment.setPrice(remainPrice);
			payment.setVat(remainVat);
			paymentRepo.save(payment);
			
			// 취소정보 Payment테이블에 Insert
			Payment cancel = new Payment();
			cancel.setMngId(idProvider.getMngId());
			cancel.setStatus(PaymentType.CANCEL);
			cancel.setPrice(cancelInfo.getPrice());
			cancel.setVat(cancelInfo.getVat());
			cancel.setInstallMonths(0L);
			cancel.setCardInfo(payment.getCardInfo());
			cancel.setPayMngId(payment);
			paymentRepo.save(cancel);
			
			// Response객체 데이터 저장
			ResponseDTO.PaymentIdDTO response = new ResponseDTO.PaymentIdDTO();
			response.setMngId(cancel.getMngId());
			response.setTransactionId(idProvider.getTransactionId(cancel));
			
			// 카드회사로 결제취소 정보 전송(트랜잭션ID 전송)
			cardCompanyService.sendTransaction(response.getTransactionId());
			return response;
		}catch(CustomException e){
			throw e;
		}finally {
			lockProvider.unlock(cancelKey);
		}
	}
	
	public ResponseDTO.PaymentInfoDTO findTransaction(RequestDTO.FindReqDTO findInfo) throws CustomException {
		validationService.checkFindRequest(findInfo);
		
		Payment payment = paymentRepo.findByMngId(findInfo.getMngId());
		
		ResponseDTO.PaymentInfoDTO response = new ResponseDTO.PaymentInfoDTO();
		response.setMngId(payment.getMngId());
		response.setStatus(payment.getStatus());
		
		// 카드번호 마스킹처리
		CardInfoDTO cardInfo = cardInfoProvider.decrypt(payment.getCardInfo());
		cardInfo.setCardNo(cardInfoProvider.maskingCardNo(cardInfo.getCardNo()));
		
		response.setCardInfo(cardInfo);
		response.setPriceInfo(
				mapperProvider.getMapper().map(payment, ResponseDTO.PriceInfoDTO.class)
			);
		
		return response;
	}
	
	public Long getCancelVat(Payment payment, RequestDTO.CancelReqDTO cancelInfo) {
		Long remainPrice = payment.getPrice() - cancelInfo.getPrice();
		if(remainPrice == 0 && cancelInfo.getVat() == null)
			return payment.getVat();
		else
			return getVat(cancelInfo.getVat(), cancelInfo.getPrice());
	}
	
	public Long getVat(Long vat, Long price) {
		return vat == null ? Math.round(price/11) : vat;   
	}
}
