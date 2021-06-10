package com.kakaopay.payment.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name="CARD_COMPANY")
public class CardCompany {
	@Id
	@Column(name = "TRANSACTION_ID", length = 500)
	private String transactionId;
}
