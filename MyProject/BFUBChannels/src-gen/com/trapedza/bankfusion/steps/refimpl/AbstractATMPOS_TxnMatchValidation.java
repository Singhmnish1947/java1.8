package com.trapedza.bankfusion.steps.refimpl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;

import bf.com.misys.bankfusion.attributes.UserDefinedFields;

/**
 * 
 * DO NOT CHANGE MANUALLY - THIS IS AUTOMATICALLY GENERATED CODE.<br>
 * This will be overwritten by any subsequent code-generation.
 *
 */
public abstract class AbstractATMPOS_TxnMatchValidation implements IATMPOS_TxnMatchValidation {
	/**
	 * @deprecated use no-argument constructor!
	 */
	public AbstractATMPOS_TxnMatchValidation(BankFusionEnvironment env) {
	}

	public AbstractATMPOS_TxnMatchValidation() {
	}

	private Timestamp f_IN_dateAndTime_12 = new Timestamp(0L);

	private String f_IN_txnType = CommonConstants.EMPTY_STRING;

	private String f_IN_orgAcIDC_56_4 = CommonConstants.EMPTY_STRING;

	private String f_IN_atmTransactionCode = CommonConstants.EMPTY_STRING;

	private String f_IN_messageFunction = CommonConstants.EMPTY_STRING;

	private String f_IN_acquiredIdentificationCode_32 = CommonConstants.EMPTY_STRING;

	private String f_IN_RetrievalReferenceNumber_37 = CommonConstants.EMPTY_STRING;

	private String f_IN_uniqueEndTransactionReference_48_031 = CommonConstants.EMPTY_STRING;

	private String f_IN_msgType = CommonConstants.EMPTY_STRING;

	private String f_IN_AccountId = CommonConstants.EMPTY_STRING;
	private Timestamp f_IN_orgDateTime_56_3 = new Timestamp(0L);
	private ArrayList<String> udfBoNames = new ArrayList<String>();
	private HashMap udfStateData = new HashMap();

	private String f_OUT_errorCode = CommonConstants.EMPTY_STRING;

	public void process(BankFusionEnvironment env) throws BankFusionException {
	}

	public Timestamp getF_IN_dateAndTime_12() {
		return f_IN_dateAndTime_12;
	}

	public void setF_IN_dateAndTime_12(Timestamp param) {
		f_IN_dateAndTime_12 = param;
	}

	public String getF_IN_txnType() {
		return f_IN_txnType;
	}

	public void setF_IN_txnType(String param) {
		f_IN_txnType = param;
	}

	public String getF_IN_orgAcIDC_56_4() {
		return f_IN_orgAcIDC_56_4;
	}

	public void setF_IN_orgAcIDC_56_4(String param) {
		f_IN_orgAcIDC_56_4 = param;
	}

	public String getF_IN_atmTransactionCode() {
		return f_IN_atmTransactionCode;
	}

	public void setF_IN_atmTransactionCode(String param) {
		f_IN_atmTransactionCode = param;
	}

	public String getF_IN_messageFunction() {
		return f_IN_messageFunction;
	}

	public void setF_IN_messageFunction(String param) {
		f_IN_messageFunction = param;
	}

	public String getF_IN_acquiredIdentificationCode_32() {
		return f_IN_acquiredIdentificationCode_32;
	}

	public void setF_IN_acquiredIdentificationCode_32(String param) {
		f_IN_acquiredIdentificationCode_32 = param;
	}

	public String getF_IN_RetrievalReferenceNumber_37() {
		return f_IN_RetrievalReferenceNumber_37;
	}

	public void setF_IN_RetrievalReferenceNumber_37(String param) {
		f_IN_RetrievalReferenceNumber_37 = param;
	}

	public String getF_IN_uniqueEndTransactionReference_48_031() {
		return f_IN_uniqueEndTransactionReference_48_031;
	}

	public void setF_IN_uniqueEndTransactionReference_48_031(String param) {
		f_IN_uniqueEndTransactionReference_48_031 = param;
	}

	public String getF_IN_msgType() {
		return f_IN_msgType;
	}

	public void setF_IN_msgType(String param) {
		f_IN_msgType = param;
	}

	public Timestamp getF_IN_orgDateTime_56_3() {
		return f_IN_orgDateTime_56_3;
	}

	public void setF_IN_orgDateTime_56_3(Timestamp param) {
		f_IN_orgDateTime_56_3 = param;
	}

	public String getF_IN_AccountId() {
		return f_IN_AccountId;
	}

	public void setF_IN_AccountId(String param) {
		f_IN_AccountId = param;
	}

	public Map getInDataMap() {
		Map dataInMap = new HashMap();
		dataInMap.put(IN_dateAndTime_12, f_IN_dateAndTime_12);
		dataInMap.put(IN_txnType, f_IN_txnType);
		dataInMap.put(IN_orgAcIDC_56_4, f_IN_orgAcIDC_56_4);
		dataInMap.put(IN_atmTransactionCode, f_IN_atmTransactionCode);
		dataInMap.put(IN_messageFunction, f_IN_messageFunction);
		dataInMap.put(IN_acquiredIdentificationCode_32, f_IN_acquiredIdentificationCode_32);
		dataInMap.put(IN_RetrievalReferenceNumber_37, f_IN_RetrievalReferenceNumber_37);
		dataInMap.put(IN_msgType, f_IN_msgType);
		dataInMap.put(IN_orgDateTime_56_3, f_IN_orgDateTime_56_3);
		dataInMap.put(IN_AccountId, f_IN_AccountId);
		return dataInMap;
	}

	public String getF_OUT_errorCode() {
		return f_OUT_errorCode;
	}

	public void setF_OUT_errorCode(String param) {
		f_OUT_errorCode = param;
	}

	public void setUDFData(String boName, UserDefinedFields fields) {
		if (!udfBoNames.contains(boName.toUpperCase())) {
			udfBoNames.add(boName.toUpperCase());
		}
		String udfKey = boName.toUpperCase() + CommonConstants.CUSTOM_PROP;
		udfStateData.put(udfKey, fields);
	}

	public Map getOutDataMap() {
		Map dataOutMap = new HashMap();
		dataOutMap.put(OUT_errorCode, f_OUT_errorCode);
		dataOutMap.put(CommonConstants.ACTIVITYSTEP_UDF_BONAMES, udfBoNames);
		dataOutMap.put(CommonConstants.ACTIVITYSTEP_UDF_STATE_DATA, udfStateData);
		return dataOutMap;
	}
}