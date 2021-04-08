package com.misys.ub.dc.types;

import java.math.BigDecimal;

public class Amount {
	
	private BigDecimal value;
	private String currency;

	
	
	public Amount() {
		value = BigDecimal.ZERO;
		currency = "";
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
}
