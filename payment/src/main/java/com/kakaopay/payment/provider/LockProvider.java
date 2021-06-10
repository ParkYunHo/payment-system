package com.kakaopay.payment.provider;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.kakaopay.payment.config.exception.CustomException;
import com.kakaopay.payment.model.constant.ExceptionType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LockProvider {
	
	private final RedisTemplate<String, String> redisTemplate;
	
	public <T> String getUniqueKey(String type, T value) {
		return String.format("%s::%s", type, value.toString());
	}
	
	public void cancelLock(String uniqueKey) throws CustomException {
		boolean isUnLocked = redisTemplate.opsForValue().setIfAbsent(uniqueKey, "LOCK", 1, TimeUnit.MINUTES);
		if(isUnLocked == false)
			throw new CustomException(ExceptionType.LOCK_CANCEL_ERROR);
	}
	
	public void paymentLock(String uniqueKey) throws CustomException {
		boolean isUnLocked = redisTemplate.opsForValue().setIfAbsent(uniqueKey, "LOCK", 1, TimeUnit.MINUTES);
		if(isUnLocked == false)
			throw new CustomException(ExceptionType.LOCK_PAYMENT_ERROR);
	}
	
	public void unlock(String uniqueKey) {
		redisTemplate.delete(uniqueKey);
	}
}
