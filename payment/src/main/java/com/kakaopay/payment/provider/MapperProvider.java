package com.kakaopay.payment.provider;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class MapperProvider {
	public ModelMapper getMapper() {
		return new ModelMapper();
	}
}
