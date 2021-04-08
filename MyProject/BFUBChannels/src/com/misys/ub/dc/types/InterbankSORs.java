package com.misys.ub.dc.types;

public class InterbankSORs {

	String status;
	String msgId;
	String msgType;
	Amount charges;
	Amount tax;
	String instructionId;
	String soId;
	
	
	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	
	public Amount getTax() {
		return tax;
	}

	public void setTax(Amount tax) {
		this.tax = tax;
	}


	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public Amount getCharges() {
		return charges;
	}

	public void setCharges(Amount charges) {
		this.charges = charges;
	}

	public String getInstructionId() {
		return instructionId;
	}

	public void setInstructionId(String instructionId) {
		this.instructionId = instructionId;
	}

	public String getSoId() {
		return soId;
	}

	public void setSoId(String soId) {
		this.soId = soId;
	}

}
