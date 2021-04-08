package com.misys.ub.dc.payment;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.fbe.compliance.IKYCFinalResponseHandler;
import com.misys.fbe.compliance.KYCDataCache;
import com.misys.fbe.compliance.types.FircoFinalResponseType;
import com.misys.ub.interfaces.UB_IBI_PaymentsHelper;

public class UB_IBI_KYCPostingHandler implements IKYCFinalResponseHandler {

	private static final transient Log LOGGER = LogFactory
			.getLog(UB_IBI_KYCPostingHandler.class.getName());

	@Override
	public void handleResponse(String msgId,String msgType,
			FircoFinalResponseType fircoFinalResponse) {
		
		String dataStr = readDataFromTable(msgId);
		
		JsonParser parser = new JsonParser();
		JsonObject data = (JsonObject) parser.parse(dataStr);
		JsonObject responseMessageData = data.getAsJsonObject("responseMessageData");
//		String customerId = responseMessageData.get("customerId").getAsString();
		String transactionalItem = responseMessageData.get("transactionalItem").getAsString();		
		String channelId = responseMessageData.get("channelId").getAsString();
		if(fircoFinalResponse == FircoFinalResponseType.TRUE_HIT){
			UB_IBI_KYCAckResponseHandler.replyWithErrorMsg(transactionalItem, channelId);
//			UB_IBI_KYCAckResponseHandler.blockCustomer(customerId);
			LOGGER.warn(" =+= Got a True_hit as final response");
		}
		else if(fircoFinalResponse == FircoFinalResponseType.FALSE_HIT){
			if(UB_IBI_KYCPostingHandler.doPostings(msgId, dataStr)){
				UB_IBI_KYCAckResponseHandler.replyWithSuccessMsg(transactionalItem, channelId);				
			} 
			else {
				UB_IBI_KYCAckResponseHandler.replyWithPostingErrorMsg(transactionalItem, channelId);
			}
		}
	}

	
	public static String readDataFromTable(String msgId) {
		KYCDataCache kycData = new KYCDataCache();
		String dataStr = kycData.getData(msgId);
		return dataStr;
	}
	
	public static boolean doPostings(String msgId,String dataStr) {
		boolean isPosted = true;
		HashMap onlineCharges= null;
		LOGGER.info(" =+= gg wp DATA FETCHED -=> " + dataStr);

		JsonParser parser = new JsonParser();
		JsonObject data = (JsonObject) parser.parse(dataStr);
		JsonObject transactionPostingData = data.getAsJsonObject("transactionPostingData");
		JsonObject chargePostingData = data.getAsJsonObject("chargePostingData");
		JsonObject counterPartyData = data.getAsJsonObject("counterPartyData");
		
		BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
        BankFusionThreadLocal.getPersistanceFactory().beginTransaction();

		BankFusionThreadLocal.setChannel("IBI");
        BankFusionThreadLocal.getUserSession().setChannelID("IBI");
        BankFusionThreadLocal.getBankFusionEnvironment().getUserSession().setChannelID("IBI");

        LOGGER.info(" =+= Got a False_hit as final response doing the postings");
        String account = "";
        String name = "";
		try{
			if(counterPartyData != null) {
				account = counterPartyData.get("account").getAsString();
				name = counterPartyData.get("name").getAsString();
			}
			UB_IBI_PaymentPersistence.postTransactions(
					transactionPostingData.get("exchangeRate").getAsString(),
					transactionPostingData.get("channelId").getAsString(),
					transactionPostingData.get("txnReference").getAsString(),
					transactionPostingData.get("transactionID").getAsString(),
					transactionPostingData.get("inputMsg_TransactionReference").getAsString(),
					transactionPostingData.get("debitAccBranchSortCode").getAsString(),
					transactionPostingData.get("debitAmount").getAsBigDecimal(),
					transactionPostingData.get("txmAmtInCreditAccCurrency").getAsBigDecimal(),
					transactionPostingData.get("txnAmount").getAsBigDecimal(),
					transactionPostingData.get("txnCurrency").getAsString(),
					transactionPostingData.get("dr_Account").getAsString(),
					transactionPostingData.get("dr_Accountname").getAsString(),
					transactionPostingData.get("dr_Currency").getAsString(),
					transactionPostingData.get("dr_TxnCode").getAsString(),
					transactionPostingData.get("cr_Account").getAsString(),
					transactionPostingData.get("cr_AccountName").getAsString(),
					transactionPostingData.get("cr_TxnCode").getAsString(),
					transactionPostingData.get("cr_Currency").getAsString(),
					transactionPostingData.get("updateCounterPartyInfo").getAsBoolean(),
					transactionPostingData.get("txnType").getAsString(),
					transactionPostingData.get("isLoanPayment").getAsBoolean(),
					account,
					name,null
				);
			
			onlineCharges = UB_IBI_PaymentPersistence.postCharges(
				    chargePostingData.get("txnType").getAsString(),
				    chargePostingData.get("txnReference").getAsString(),
				    chargePostingData.get("transactionID").getAsString(),
				    chargePostingData.get("spotPseudonym").getAsString(),
				    chargePostingData.get("positionAccountContext").getAsString(),
				    chargePostingData.get("chargeIndicator").getAsInt(),
				    chargePostingData.get("channelId").getAsString(),
				    chargePostingData.get("txnAmount").getAsBigDecimal(),
				    chargePostingData.get("fromAccount").getAsString(),
				    chargePostingData.get("chargeFundingAccount").getAsString(),
				    chargePostingData.get("trasnferCurrency").getAsString(),
				    chargePostingData.get("fromAccountCurrency").getAsString(),
				    chargePostingData.get("chargeFundingAccountCurrency").getAsString(),
				    chargePostingData.get("dr_TxnCode").getAsString(),
				    chargePostingData.get("dr_AccountBranchSortCode").getAsString(),
				    chargePostingData.get("cr_TxnCode").getAsString(),
				    chargePostingData.get("cr_AccountBranchSortCode").getAsString(),
				    chargePostingData.get("creditAccountNumber").getAsString(),
				    chargePostingData.get("error").getAsBoolean()	    			
				);
		}catch(Exception e){
			e.printStackTrace();
	        BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
	        BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
	        LOGGER.info(" =+= =+= Exception While doing posting");
	        isPosted = false;
		}finally{
	        BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
	        BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
			LOGGER.info(" =+= =+= No Exception while posting");
		}
		
		if(isPosted && chargePostingData.get("txnType").getAsString().equalsIgnoreCase("INTNAT")){
			JsonObject swiftMessageData = data.getAsJsonObject("swiftMessageData");
			try{
				UB_IBI_PaymentsHelper.generateSwiftMessage(
						swiftMessageData.getAsJsonObject("txnInput"),
						onlineCharges,
						swiftMessageData.get("customerId").getAsString(),
						swiftMessageData.get("settlDtlId").getAsString(),
						swiftMessageData.get("debitAmount").getAsBigDecimal(),
						swiftMessageData.get("txnReference").getAsString(),
						transactionPostingData.get("inputMsg_TransactionReference").getAsString(),
						swiftMessageData.get("transactionID").getAsString(),
						swiftMessageData.get("creditAccountNumber").getAsString()
						);
			}catch(Exception e){
				e.printStackTrace();
		        BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
		        BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
		        LOGGER.info(" =+= =+= Exception While generating Swift Message -> " + swiftMessageData.toString());
			}finally{
		        BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
		        BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
				LOGGER.info(" =+= =+= No Exception while generating Swift Message");
			}
		}
		
		return isPosted;
	}
}
