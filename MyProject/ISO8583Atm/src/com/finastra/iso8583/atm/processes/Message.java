package com.finastra.iso8583.atm.processes;

import java.util.HashMap;
import java.util.Map;

public class Message implements Fields{

	private String messageHeader;
	
	private String messageTypeIdentifier;

	private String primaryBitMap;
	
	private String secondaryBitMap;

	private String messageLength;
	
	private String messageData;	
	
	private HashMap<String, Object> messageFields;
	

	public Message() {	
	}
	
	public Message(String messageLength,String messageType, String messageData) {
		this.messageLength = messageLength;
		this.messageTypeIdentifier = messageType;
		this.primaryBitMap = messageData.substring(0, 16);
		this.messageData = messageData.substring(16);
		this.messageFields = new HashMap<String, Object>();
	}
	
	public String getMessageHeader() {
		return messageHeader;
	}

	public void setMessageHeader(String messageHeader) {
		this.messageHeader = messageHeader;
	}

	public void setMessageFields(HashMap<String, Object> messageFields) {
		this.messageFields = messageFields;
	}

	public Message(HashMap<String, Object> messageFields) {
		this.messageFields = messageFields;
	}
	
	
	
	public String getMessageData() {
		return messageData;
	}

	public void setMessageData(String messageData) {
		this.messageData = messageData;
	}

	public HashMap<String, Object> getMessageFields() {
		return messageFields;
	}

	public void setMessageFields(String fieldName, Object fieldValue) {
		this.messageFields.put(fieldName, fieldValue);
	}

	@Override
	public Object getFieldValue(String fieldName) {
		return this.messageFields.get(fieldName);
	}

	@Override
	public void setFieldValue(String fieldName, String fieldValue) {
		//Abstract Method
	}

	public String getMessageTypeIdentifier() {
		return messageTypeIdentifier;
	}

	public void setMessageTypeIdentifier(String messageTypeIdentifier) {
		this.messageTypeIdentifier = messageTypeIdentifier;
	}

	public String getPrimaryBitMap() {
		return primaryBitMap;
	}

	public String getSecondaryBitMap() {
		return this.secondaryBitMap;
	}

	public void setSecondaryBitMap(String secondaryBitMap) {
		this.secondaryBitMap = secondaryBitMap;
	}

	public void setPrimaryBitMap(String primaryBitMap) {
		this.primaryBitMap = primaryBitMap;
	}

	public String getMessageLength() {
		return messageLength;
	}

	public void setMessageLength(String messageLength) {
		this.messageLength = messageLength;
	}
	
	@Override
	public String toString() {
		return super.toString();
	}

}
