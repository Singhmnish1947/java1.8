package com.finastra.iso8583.atm.processes;

public interface Fields {
	
	public Object getFieldValue(String fieldName);
	
	public void setFieldValue(String fieldName, String fieldValue);

}
