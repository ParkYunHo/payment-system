package com.kakaopay.payment.model.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name="PAYMENT")
public class Payment {
	@Id
	@Column(name = "MNG_ID")
	private String mngId;
	
	@Column(name = "STATUS")
	private String status;

	@Column(name = "PRICE")
	private Long price;
	
	@Column(name = "VAT")
	private Long vat;
	
	@Column(name = "INSTALL_MONTHS")
	private Long installMonths;
	
	@Column(name = "ENCRYPT_CARD_INFO")
	private String cardInfo;

	
	@OneToMany(mappedBy="payMngId")
    private Set<Payment> subPayMngId;

    @ManyToOne
    private Payment payMngId;
}
