package com.kakaopay.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kakaopay.payment.model.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, String>{
	public String findStatusByMngId(String mngId);
	public Payment findByMngId(String mngId);
}
