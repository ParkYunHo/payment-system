package com.kakaopay.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kakaopay.payment.model.entity.CardCompany;

public interface CardCompanyRepository extends JpaRepository<CardCompany, String>{
	
}
