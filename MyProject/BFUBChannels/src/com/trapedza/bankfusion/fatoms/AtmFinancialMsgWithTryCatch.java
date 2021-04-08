/**
 *
 */
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.common.exception.RetriableException;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ATM_FinancialMsgWithTryCatch;
import com.trapedza.bankfusion.steps.refimpl.IUB_ATM_FinancialMsgWithTryCatch;
import com.trapedza.bankfusion.utils.BankFusionMessages;

import bf.com.misys.ub.types.atm.UB_ATM_Financial_Details;

/**
 * @author ayumital
 *
 */
public class AtmFinancialMsgWithTryCatch extends
		AbstractUB_ATM_FinancialMsgWithTryCatch implements
		IUB_ATM_FinancialMsgWithTryCatch {


	ConcurrentHashMap<String,Object> inputParams = new ConcurrentHashMap<String,Object>();
	Map<String,Object> outputParams = new ConcurrentHashMap<String,Object>();
	private static final transient Log logger = LogFactory.getLog(AtmFinancialMsgWithTryCatch.class.getName());

	/**
	 *
	 */
	public AtmFinancialMsgWithTryCatch() {
		super();
	}

	public AtmFinancialMsgWithTryCatch(BankFusionEnvironment env) {
		super(env);
	}

	@SuppressWarnings("unchecked")
	public void process(BankFusionEnvironment env) throws BankFusionException {

        Date startDate = SystemInformationManager.getInstance().getBFSystemDate();
		String microflowId = getF_IN_MfId();
		if(!microflowId.equals(CommonConstants.EMPTY_STRING)){

			try{

			inputParams.put("CHEQUEBOOKCOPIESSENT", getF_IN_ACTIVEAUDITFATOMCOUNT());
			inputParams.put("ACCOUNTNUMBER", getF_IN_Account_Identification_1_102());
			inputParams.put("MESSAGEID", getF_IN_Account_Identification_2_103());

			outputParams = MFExecuter.executeMF(microflowId, env, inputParams);
			setF_OUT_Responder_Code((String)outputParams.get("MESSAGEID"));
			setF_OUT_update_ERRORDESCRIPTION((String)outputParams.get("StatusMessage"));
			setF_OUT_update_ERRORSTATUS((String)(outputParams.get("EventCode")));
			}
			catch (BankFusionException bfException) {
				if(logger.isDebugEnabled()){
					logger.info(bfException.getMessageNumber() + " : " + bfException.getLocalizedMessage());
				}
				setF_OUT_Responder_Code((String)getF_IN_Account_Identification_2_103());
				setF_OUT_update_ERRORDESCRIPTION((bfException.getLocalizedMessage()!= null)? bfException.getLocalizedMessage():bfException.getLocalisedMessage());
				setF_OUT_update_ERRORSTATUS(Integer.toString(bfException.getMessageNumber()));
				logger.error(ExceptionUtil.getExceptionAsString(bfException));
			}
			catch (Exception e) {
				setF_OUT_Responder_Code((String)getF_IN_Account_Identification_2_103());
				setF_OUT_update_ERRORDESCRIPTION(BankFusionMessages.getFormattedMessage(40000127, new Object[]{CommonConstants.EMPTY_STRING}));
				setF_OUT_update_ERRORSTATUS(Integer.toString(40000127));
				logger.error(ExceptionUtil.getExceptionAsString(e));
			}
		}
		else{

		inputParams.put("ACTIVEAUDITFATOMCOUNT", getF_IN_ACTIVEAUDITFATOMCOUNT());
		inputParams.put("Account_Identification_1_102", getF_IN_Account_Identification_1_102());
		inputParams.put("Account_Identification_2_103", getF_IN_Account_Identification_2_103());
		inputParams.put("BALANCEFROZEN", getF_IN_BALANCEFROZEN());
		inputParams.put("BANKLOGO", getF_IN_BANKLOGO());
		inputParams.put("BANKNAME", getF_IN_BANKNAME());
		inputParams.put("BRANCHNAME", getF_IN_BRANCHNAME());
		inputParams.put("BRANCHSORTCODE", getF_IN_BRANCHSORTCODE());
		inputParams.put("CHANNELID", getF_IN_CHANNELID());
		inputParams.put("CURRENTFREEZETIMESTAMP", getF_IN_CURRENTFREEZETIMESTAMP());
		inputParams.put("Card_Issuer_Authorizer_Data_61", getF_IN_Card_Issuer_Authorizer_Data_61());
		inputParams.put("Card_acceptor_name_location_43", getF_IN_Card_acceptor_name_location_43());
		inputParams.put("DATE", getF_IN_DATE());
		inputParams.put("DATETIME", getF_IN_DATETIME());
		inputParams.put("DepositCreditAmount_123", getF_IN_DepositCreditAmount_123());
		inputParams.put("ForwardingInstituionID_33", getF_IN_ForwardingInstituionID_33());
		inputParams.put("INPUTMASK", getF_IN_INPUTMASK());
		inputParams.put("CommNumCurrencyCode", getF_IN_ISOCURRENCYCODE());
		inputParams.put("LASTFREEZETIMESTAMP", getF_IN_LASTFREEZETIMESTAMP());
		inputParams.put("LOCALE_COUNTRY", getF_IN_LOCALE_COUNTRY());
		inputParams.put("LOCALE_LANGUAGE", getF_IN_LOCALE_LANGUAGE());
		inputParams.put("LOCALE_VARIANT_", getF_IN_LOCALE_VARIANT_());
		inputParams.put("LocalTransactionSqlDate_13", getF_IN_LocalTransactionSqlDate_13());
		inputParams.put("LocalTransactionSqlTime_12", getF_IN_LocalTransactionSqlTime_12());
		inputParams.put("Message_Type", getF_IN_Message_Type());
		inputParams.put("MsgFunction", getF_IN_MsgFunction());
		inputParams.put("NUMBEROFROWSFORPAGING", getF_IN_NUMBEROFROWSFORPAGING());
		inputParams.put("OUTPUTMASK", getF_IN_OUTPUTMASK());
		inputParams.put("OriginalDataElements_90", getF_IN_OriginalDataElements_90());
		inputParams.put("OriginalTransactionSqlDate_90_3", getF_IN_OriginalTransactionSqlDate_90_3());
		inputParams.put("OriginalTransactionSqlTime_90_4", getF_IN_OriginalTransactionSqlTime_90_4());
		inputParams.put("Originator_Code", getF_IN_Originator_Code());
		inputParams.put("PIN_Data_52", getF_IN_PIN_Data_52());
		inputParams.put("PUBLISHNAME", getF_IN_PUBLISHNAME());
		inputParams.put("Processing_Code_3", getF_IN_Processing_Code_3());
		inputParams.put("Product_Indicator", getF_IN_Product_Indicator());
		inputParams.put("Release_Number", getF_IN_Release_Number());
		inputParams.put("ReplcamentAmount_95", getF_IN_ReplcamentAmount_95());
		inputParams.put("Responder_Code", getF_IN_Responder_Code());
		inputParams.put("SYSTEMDATE", getF_IN_SYSTEMDATE());
		inputParams.put("SYSTEMDATETIME", getF_IN_SYSTEMDATETIME());
		inputParams.put("SYSTEMTIME", getF_IN_SYSTEMTIME());
		inputParams.put("Status", getF_IN_Status());
		inputParams.put("TIME", getF_IN_TIME());
		inputParams.put("Terminal_Data_60", getF_IN_Terminal_Data_60());
		inputParams.put("Track_2_Data_35", getF_IN_Track_2_Data_35());
		inputParams.put("Track_I_Data_45", getF_IN_Track_I_Data_45());
		//check the following three - not sure where they came from
		inputParams.put("TransactionDetails_NUMBEROFROWS", getF_IN_TransactionDetails_NUMBEROFROWS());
		inputParams.put("TransactionDetails_PAGENUMBER", getF_IN_TransactionDetails_PAGENUMBER());
		inputParams.put("TransactionDetails_PAGINGSUPPORT", getF_IN_TransactionDetails_PAGINGSUPPORT());

		inputParams.put("USERNAME", getF_IN_USERNAME());
		inputParams.put("WEBSERVICE_AVAILABILITY", getF_IN_WEBSERVICE_AVAILABILITY());
		inputParams.put("accountNumber1_102_2", getF_IN_accountNumber1_102_2());
		inputParams.put("accountNumber2_103_2", getF_IN_accountNumber2_103_2());
		inputParams.put("accountType_3_1", getF_IN_accountType_3_1());
		inputParams.put("acquirerFee_95_2", getF_IN_acquirerFee_95_2());
		inputParams.put("acquiringInstitutionId_32", getF_IN_acquiringInstitutionId_32());
		inputParams.put("actualTransactionAmount_95_1", getF_IN_actualTransactionAmount_95_1());
		inputParams.put("additionalData1_48", getF_IN_additionalData1_48());
		inputParams.put("atmAccountIndicator_125", getF_IN_atmAccountIndicator_125());
		inputParams.put("captureDate_17", getF_IN_captureDate_17());
		inputParams.put("cardAcceptorId_42", getF_IN_cardAcceptorId_42());
		inputParams.put("cardAcceptorNameLoc_43", getF_IN_cardAcceptorNameLoc_43());
		inputParams.put("cardAcceptorTerminalId_41", getF_IN_cardAcceptorTerminalId_41());
		inputParams.put("cardIssuerAuthoriser_61", getF_IN_cardIssuerAuthoriser_61());
		inputParams.put("cardIssuerAuthoriser_61_6", getF_IN_cardIssuerAuthoriser_61_6());
		inputParams.put("cardIssuerFIID_61_2", getF_IN_cardIssuerFIID_61_2());
		inputParams.put("cardIssuerFromAccountType_61_4", getF_IN_cardIssuerFromAccountType_61_4());
		inputParams.put("cardIssuerToAccountType_61_5", getF_IN_cardIssuerToAccountType_61_5());
		inputParams.put("cardLogicalNetwork_61_3", getF_IN_cardLogicalNetwork_61_3());
		inputParams.put("cardNumber_35", getF_IN_cardNumber_35());
		inputParams.put("cardNumber_35_2", getF_IN_cardNumber_35_2());
		inputParams.put("currencyCode_49", getF_IN_currencyCode_49());
		inputParams.put("environment_60_2", getF_IN_environment_60_2());
		inputParams.put("fieldLength_35_1", getF_IN_fieldLength_35_1());
		inputParams.put("localTransactionDate_13", getF_IN_localTransactionDate_13());
		inputParams.put("localTransactionTime_12", getF_IN_localTransactionTime_12());
		inputParams.put("originalCaptureDate_90_5", getF_IN_originalCaptureDate_90_5());
		inputParams.put("originalFiller_90_6", getF_IN_originalFiller_90_6());
		inputParams.put("originalSequenceNumber_90_2", getF_IN_originalSequenceNumber_90_2());
		inputParams.put("originalTransactionDate_90_3", getF_IN_originalTransactionDate_90_3());
		inputParams.put("originalTransactionTime_90_4", getF_IN_originalTransactionTime_90_4());
		inputParams.put("originalTransactionType_90_1", getF_IN_originalTransactionType_90_1());
		inputParams.put("processingCode", getF_IN_processingCode());
		inputParams.put("processingCodeFiller_3_3", getF_IN_processingCodeFiller_3_3());
		inputParams.put("receivingInstitutionId_100", getF_IN_receivingInstitutionId_100());
		inputParams.put("retrievalReferenceNo_37", getF_IN_retrievalReferenceNo_37());
		inputParams.put("systemsTraceAuditNumber_11", getF_IN_systemsTraceAuditNumber_11());
		inputParams.put("terminalLogicalNetwork_43_1", getF_IN_terminalLogicalNetwork_43_1());
		inputParams.put("terminalLogicalNetwork_60_1", getF_IN_terminalLogicalNetwork_60_1());
		inputParams.put("track2AdditionalDdata_35_3", getF_IN_track2AdditionalDdata_35_3());
		inputParams.put("transactionAmount_4", getF_IN_transactionAmount_4());
		inputParams.put("transactionFeeAmount_28", getF_IN_transactionFeeAmount_28());
		inputParams.put("transactionType_3_2", getF_IN_transactionType_3_2());
		inputParams.put("transmissionDateTime_7", getF_IN_transmissionDateTime_7());
		inputParams.put("CreditAmount", getF_IN_Credit_Amount());
		inputParams.put("CreditCurrency", getF_IN_credit_currency());
		inputParams.put("CardHolderBillingAmt", getF_IN_CardHolderBillingAmount());
		inputParams.put("CardHldBillingCurr", getF_IN_CardHldBillCurrency());
		inputParams.put("CardHolderFeeAmt", getF_IN_CardHolderFeeAmt());
		inputParams.put("OriginalTxnAmt", getF_IN_originalTxnAmt());
		//all input tag set up to here
		inputParams.put("amountRecon", getF_IN_amountRecon());
		inputParams.put("amountReconCurrency", getF_IN_amountReconCurrency());

		try {
		outputParams = MFExecuter.executeMF("UB_ATM_FinancialMessage_PRC", env, inputParams);
	    setF_OUT__100((String)outputParams.get("_100"));
		setF_OUT__102((String)outputParams.get("_102"));
		setF_OUT__103((String) outputParams.get("_103"));
		setF_OUT__11((String) outputParams.get("_11"));
		setF_OUT__12((Time) outputParams.get("_12"));
		setF_OUT__123((String) outputParams.get("_123"));
		setF_OUT__125((String) outputParams.get("_125"));
		setF_OUT__13((Timestamp) outputParams.get("_13"));
		setF_OUT__17((Timestamp) outputParams.get("_17"));
		setF_OUT__28((BigDecimal) outputParams.get("_28"));
		setF_OUT__3((String) outputParams.get("_3"));
		setF_OUT__32((String) outputParams.get("_32"));
		setF_OUT__33((String) outputParams.get("_33"));
		setF_OUT__35((String) outputParams.get("_35"));
		setF_OUT__35_1((String) outputParams.get("_35_1"));
		setF_OUT__35_2((String) outputParams.get("_35_2"));
		setF_OUT__35_3((String) outputParams.get("_35_3"));
		setF_OUT__37((String) outputParams.get("_37"));
		setF_OUT__39((String) outputParams.get("_39"));
		setF_OUT__3_1((String) outputParams.get("_3_1"));
		setF_OUT__3_2((String) outputParams.get("_3_2"));
		setF_OUT__3_3((String) outputParams.get("_3_3"));
		setF_OUT__4((BigDecimal) outputParams.get("_4"));
		setF_OUT__41((String) outputParams.get("_41"));
		setF_OUT__42((String) outputParams.get("_42"));
		setF_OUT__43((String) outputParams.get("_43"));
		setF_OUT__43_1((String) outputParams.get("_43_1"));
		setF_OUT__44((String) outputParams.get("_44"));
		setF_OUT__44_1((String) outputParams.get("_44_1"));
		setF_OUT__44_2((BigDecimal) outputParams.get("_44_2"));
		setF_OUT__44_3((BigDecimal) outputParams.get("_44_3"));
		setF_OUT__48((String) outputParams.get("_48"));
		setF_OUT__49((String) outputParams.get("_49"));
		setF_OUT__52((String) outputParams.get("_52"));
		setF_OUT__60((String) outputParams.get("_60"));
		setF_OUT__60_1((String) outputParams.get("_60_1"));
		setF_OUT__60_2((String) outputParams.get("_60_2"));
		setF_OUT__61((String) outputParams.get("_61"));
		setF_OUT__61_1((String) outputParams.get("_61_1"));
		setF_OUT__61_2((String) outputParams.get("_61_2"));
		setF_OUT__61_3((String) outputParams.get("_61_3"));
		setF_OUT__61_4((String) outputParams.get("_61_4"));
		setF_OUT__7((Timestamp) outputParams.get("_7"));
		setF_OUT__90((String) outputParams.get("_90"));
		setF_OUT__90_1((String) outputParams.get("_90_1"));
		setF_OUT__90_2((String) outputParams.get("_90_2"));
		setF_OUT__90_3((Timestamp) outputParams.get("_90_3"));
		setF_OUT__90_4((Timestamp) outputParams.get("_90_4"));
		setF_OUT__90_5((Timestamp) outputParams.get("_90_5"));
		setF_OUT__90_6((String) outputParams.get("_90_6"));
		setF_OUT__95((String) outputParams.get("_95"));
		setF_OUT__95_1((BigDecimal) outputParams.get("_95_1"));
		setF_OUT__95_2((BigDecimal) outputParams.get("_95_2"));
		setF_OUT_AvailableBalance((BigDecimal) outputParams.get("AvailableBalance"));
		setF_OUT_Message_Type((String) outputParams.get("Message_Type"));
		setF_OUT_Originator_Code((String) outputParams.get("Originator_Code"));
		setF_OUT_Product_Indicator((String) outputParams.get("Product_Indicator"));
		setF_OUT_Release_Number((String) outputParams.get("Release_Number"));
		setF_OUT_Status((String) outputParams.get("Status"));
		setF_OUT_Responder_Code((String) outputParams.get("Responder_Code"));
		setF_OUT_TransactionDetails((VectorTable) outputParams.get("TransactionDetails"));
		setF_OUT_update_ACCOUNTCURRENCY((String) outputParams.get("update_ACCOUNTCURRENCY"));
		setF_OUT_update_ACCOUNTID((String) outputParams.get("update_ACCOUNTID"));
		setF_OUT_update_AccountIndicatorLength((Integer) outputParams.get("update_AccountIndicatorLength"));
		setF_OUT_update_AMOUNTDISPENSED((BigDecimal) outputParams.get("update_AMOUNTDISPENSED"));
		setF_OUT_update_ATMCARDNUMBER((String) outputParams.get("update_ATMCARDNUMBER"));
		setF_OUT_update_ATMDEVICEID((String) outputParams.get("update_ATMDEVICEID"));
		setF_OUT_update_atmPosting((UB_ATM_Financial_Details) outputParams.get("update_atmPosting"));
		setF_OUT_update_ATMTRANDESC((String) outputParams.get("update_ATMTRANDESC"));
		setF_OUT_update_ATMTRANSACTIONCODE((String) outputParams.get("update_ATMTRANSACTIONCODE"));
		setF_OUT_update_ATMTRANSDESCRIPTION((String) outputParams.get("update_ATMTRANSDESCRIPTION"));
		setF_OUT_update_AUTHORIZEDFLAG((Integer) outputParams.get("update_AUTHORIZEDFLAG"));
		setF_OUT_update_BASEEQUIVALENT((BigDecimal) outputParams.get("update_BASEEQUIVALENT"));
		setF_OUT_update_CARDSEQUENCENUMBER((Integer) outputParams.get("update_CARDSEQUENCENUMBER"));
		setF_OUT_update_COMMAMOUNT((BigDecimal) outputParams.get("update_COMMAMOUNT"));
		setF_OUT_update_DATEMON((String) outputParams.get("update_DATEMON"));
		setF_OUT_update_DESTACCOUNTID((String) outputParams.get("update_DESTACCOUNTID"));
		setF_OUT_update_DESTBRANCH((String) outputParams.get("update_DESTBRANCH"));
		setF_OUT_update_DESTCIB((String) outputParams.get("update_DESTCIB"));
		setF_OUT_update_DESTCOUNTRY((String) outputParams.get("update_DESTCOUNTRY"));
		setF_OUT_update_DESTIMD((String) outputParams.get("update_DESTIMD"));
		setF_OUT_update_ERRORDESCRIPTION((String) outputParams.get("update_ERRORDESCRIPTION"));
		setF_OUT_isChargeWaivedBasedOnCounter((Boolean)outputParams.get("isChargeWaivedBasedOnCounter"));
		if(((String) outputParams.get("update_ERRORSTATUS") == null) || (((String) outputParams.get("update_ERRORSTATUS")).equals(CommonConstants.EMPTY_STRING)))
		{
		setF_OUT_update_ERRORSTATUS("0");
		}
		else{
		setF_OUT_update_ERRORSTATUS((String) outputParams.get("update_ERRORSTATUS"));
		}
		setF_OUT_update_FORCEPOST((Integer) outputParams.get("update_FORCEPOST"));
		setF_OUT_update_ISOCURRENCYCODE_TXN((String) outputParams.get("update_ISOCURRENCYCODE_TXN"));
		setF_OUT_update_MISTRANSACTIONCODE((String) outputParams.get("update_MISTRANSACTIONCODE"));
		setF_OUT_update_MSGRECVDATETIME((Timestamp) outputParams.get("update_MSGRECVDATETIME"));
		setF_OUT_update_POSTDATETIME((Timestamp) outputParams.get("update_POSTDATETIME"));
		setF_OUT_update_SOURCEBRANCH((String) outputParams.get("update_SOURCEBRANCH"));
		setF_OUT_update_SOURCECIB((String) outputParams.get("update_SOURCECIB"));
		setF_OUT_update_SOURCECOUNTRY((String) outputParams.get("update_SOURCECOUNTRY"));
		setF_OUT_update_SOURCEIMD((String) outputParams.get("update_SOURCEIMD"));
		setF_OUT_update_SUBINDEX((String) outputParams.get("update_SUBINDEX"));
		setF_OUT_update_SUBSTITUTION_REQUIRED((Boolean) outputParams.get("update_SUBSTITUTION_REQUIRED"));
		setF_OUT_update_TRANSACTIONAMOUNT((BigDecimal) outputParams.get("update_TRANSACTIONAMOUNT"));
		setF_OUT_update_TRANSACTIONDTTM((Timestamp) outputParams.get("update_TRANSACTIONDTTM"));
		setF_OUT_update_TRANSACTIONID((String) outputParams.get("update_TRANSACTIONID"));
		setF_OUT_update_TRANSACTIONREFERENCE((String) outputParams.get("update_TRANSACTIONREFERENCE"));
		setF_OUT_update_TRANSNARRATION((String) outputParams.get("update_TRANSNARRATION"));
		setF_OUT_update_TRANSSEQ((Integer) outputParams.get("update_TRANSSEQ"));
		setF_OUT_update_UBCAPTUREDTMON((String) outputParams.get("update_UBCAPTUREDTMON"));
		setF_OUT_update_UBORIGINALTXNDATA((String) outputParams.get("update_UBORIGINALTXNDATA"));
		setF_OUT_update_UBPROCESSINGCODE((String) outputParams.get("update_UBPROCESSINGCODE"));
		setF_OUT_update_UBTERMINALDATA((String) outputParams.get("update_UBTERMINALDATA"));
		setF_OUT_noAccountError((String) outputParams.get("noAccountError"));
		}

		catch (BankFusionException be) {
			logger.error(ExceptionUtil.getExceptionAsString(be));
			throw be;
		}
		catch (RetriableException re) {
			throw re;
		}
		catch (Exception e) {
			logger.error(ExceptionUtil.getExceptionAsString(e));
			EventsHelper.handleEvent(40000127, new Object[] {  e.toString() }, new HashMap(), env);
		}

		}
        Date endDate = SystemInformationManager.getInstance().getBFSystemDate();
		long executionTime = endDate.getTime()-startDate.getTime();
		if(logger.isInfoEnabled()){
        logger.info("Execution time for transaction " + BankFusionThreadLocal.getCorrelationID() + "\t"
                + getF_IN_retrievalReferenceNo_37() + "\tis\t" + executionTime + "\t" + startDate.getTime() + "\t"
                + endDate.getTime());
		}
	}
}
