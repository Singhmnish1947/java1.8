package com.trapedza.bankfusion.steps.refimpl;

import java.sql.Timestamp;
import java.util.Map;

import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;

/**
 * 
 * DO NOT CHANGE MANUALLY - THIS IS AUTOMATICALLY GENERATED CODE.<br>
 * This will be overwritten by any subsequent code-generation.
 *
 */
public interface IATMPOS_TxnMatchValidation extends com.trapedza.bankfusion.servercommon.steps.refimpl.Processable {
	public static final String IN_dateAndTime_12 = "dateAndTime_12";
	public static final String IN_txnType = "txnType";
	public static final String IN_orgAcIDC_56_4 = "orgAcIDC_56_4";
	public static final String IN_atmTransactionCode = "atmTransactionCode";
	public static final String IN_messageFunction = "messageFunction";
	public static final String IN_acquiredIdentificationCode_32 = "acquiredIdentificationCode_32";
	public static final String IN_RetrievalReferenceNumber_37 = "RetrievalReferenceNumber_37";
	public static final String IN_uniqueEndTransactionReference_48_031 = "uniqueEndTransactionReference_48_031";
	public static final String IN_msgType = "msgType";
	public static final String IN_orgDateTime_56_3 = "orgDateTime_56_3";
	public static final String OUT_errorCode = "errorCode";
	public static final String IN_AccountId = "AccountId";

	public void process(BankFusionEnvironment env) throws BankFusionException;

	public Timestamp getF_IN_dateAndTime_12();

	public void setF_IN_dateAndTime_12(Timestamp param);

	public String getF_IN_txnType();

	public void setF_IN_txnType(String param);

	public String getF_IN_orgAcIDC_56_4();

	public void setF_IN_orgAcIDC_56_4(String param);

	public String getF_IN_atmTransactionCode();

	public void setF_IN_atmTransactionCode(String param);

	public String getF_IN_messageFunction();

	public void setF_IN_messageFunction(String param);

	public String getF_IN_acquiredIdentificationCode_32();

	public void setF_IN_acquiredIdentificationCode_32(String param);

	public String getF_IN_RetrievalReferenceNumber_37();

	public void setF_IN_RetrievalReferenceNumber_37(String param);

	public String getF_IN_uniqueEndTransactionReference_48_031();

	public void setF_IN_uniqueEndTransactionReference_48_031(String param);

	public String getF_IN_msgType();

	public void setF_IN_msgType(String param);

	public Timestamp getF_IN_orgDateTime_56_3();

	public void setF_IN_orgDateTime_56_3(Timestamp param);

	public Map getInDataMap();

	public String getF_OUT_errorCode();

	public void setF_OUT_errorCode(String param);

	public String getF_IN_AccountId();

	public void setF_IN_AccountId(String param);

	public Map getOutDataMap();
}