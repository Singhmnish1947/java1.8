/**
 * 
 */
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.RetriableException;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.ub.datacenter.DataCenterCommonUtils;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOFinancialPostingMessage;
import com.trapedza.bankfusion.core.PostingHelper;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.postingengine.gateway.interfaces.IPostingMessage;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.servercommon.utils.FatomUtils;
import com.trapedza.bankfusion.steps.refimpl.AbstractFetchConfiguredCharges;
import com.trapedza.bankfusion.steps.refimpl.AbstractPreparePostingMessageforCharge;

import bf.com.misys.ub.types.treasurycollectionpayement.IBOFinancialPostingMsg;
import bf.com.misys.ub.types.treasurycollectionpayement.IBOFinancialPostingMsgList;

/**
 * @author summahto
 *
 */
public class PreparePostingMessageforCharge extends AbstractPreparePostingMessageforCharge{

	private transient final static Log logger = LogFactory.getLog(PreparePostingMessageforCharge.class.getName());
	private static final String CB_CMN_COLLECTIONPOSTING_MESSAGE_SRV = "CB_CMN_CollectionPostingMessage_SRV";
	
    public PreparePostingMessageforCharge(BankFusionEnvironment env) {
        super(env);
    }
    
public void process(BankFusionEnvironment env) {
    	
	
	
    	VectorTable chargeVector = new VectorTable();
    	chargeVector = getF_IN_ChargeVector();
    	
    	String debitChargeNarrative = StringUtils.EMPTY;
    	String creditChargeNarrative = StringUtils.EMPTY;
    	String debitTaxNarrative =  StringUtils.EMPTY;
    	
    	String creditTaxNarrative =  StringUtils.EMPTY;
    	String debitPostingAction = "D" ;
    	String creditPostingAction = "C" ;
    	BigDecimal chargeAmount = BigDecimal.ZERO;
    	
    	ArrayList<IBOFinancialPostingMessage> postingMsgList = new ArrayList<>();
    	IBOFinancialPostingMsgList postingMsgIBOList = new IBOFinancialPostingMsgList();
    	
    	String ourChargeCode = DataCenterCommonUtils.readModuleConfiguration(PaymentSwiftConstants.MODULE_ID,
				PaymentSwiftConstants.CHARGE_CODE_USED_FOR_OUR_71G_CHARGE_POSTING);
        
        for(int i=0; i < chargeVector.size(); i++) {
        	HashMap chargeMap = chargeVector.getRowTags(i);
        	IBOFinancialPostingMsg chargeDebitLeg = new IBOFinancialPostingMsg();
        	
        	if (ourChargeCode.equals(chargeMap.get("CHARGECODE"))) {
        		debitChargeNarrative = getF_IN_DebitChargeNarrative();
        		creditChargeNarrative = getF_IN_CreditChargeNarrative();
        		debitTaxNarrative = getF_IN_DebitTaxNarrative();
        		creditTaxNarrative = getF_IN_CreditTaxNarrative();
        		
        	}else {
        		debitChargeNarrative = (String) chargeMap.get("CHARGENARRATIVE");
        		creditChargeNarrative = (String) chargeMap.get("CHARGENARRATIVE");
        		debitTaxNarrative = (String) chargeMap.get("TAXNARRATIVE");
        		creditTaxNarrative = (String) chargeMap.get("TAXNARRATIVE");
        		
        	}
        	chargeDebitLeg = populatePostingMessage((BigDecimal) chargeMap.get("CHARGEAMOUNT"),
        			(String) chargeMap.get("FUNDINGACCOUNTID"), debitPostingAction,
        			getF_IN_DebitChargeTxnCode(), debitChargeNarrative,
        			getF_IN_DebitChargeCurrency(),(BigDecimal) chargeMap.get("CHARGEEXCHANGERATE"));
        	
        	postingMsgIBOList.addIBOFinancialPostingMsg(chargeDebitLeg);
        	
        	IBOFinancialPostingMsg chargeCreditLeg = new IBOFinancialPostingMsg();
        	chargeCreditLeg = populatePostingMessage((BigDecimal) chargeMap.get("CHARGEAMOUNT"),
        			(String) chargeMap.get("CHARGERECIEVINGACCOUNT"), creditPostingAction, 
        			 getF_IN_CreditChargeTxnCode(), creditChargeNarrative,
        			 getF_IN_CreditChargeCurrency(), (BigDecimal) chargeMap.get("CHARGEEXCHANGERATE"));
        	
        	postingMsgIBOList.addIBOFinancialPostingMsg(chargeCreditLeg);
        	
        	BigDecimal taxAmount = (BigDecimal)chargeMap.get("TAXAMOUNT");
        	if(taxAmount.compareTo(BigDecimal.ZERO) != 0) {
	        	IBOFinancialPostingMsg taxDebitLeg = new IBOFinancialPostingMsg();
	        	 
	        	chargeDebitLeg = populatePostingMessage(taxAmount,
	        			(String) chargeMap.get("FUNDINGACCOUNTID"), debitPostingAction,
	        			getF_IN_DebitTaxTxnCode(), debitTaxNarrative,
	        			getF_IN_DebitTaxCurrency(),(BigDecimal) chargeMap.get("TAXEXCHANGERATE"));
	        	
	        	postingMsgIBOList.addIBOFinancialPostingMsg(taxDebitLeg);
	        	
	        	IBOFinancialPostingMsg taxCreditLeg = new IBOFinancialPostingMsg();
	        	chargeCreditLeg = populatePostingMessage(taxAmount,
	        			(String) chargeMap.get("TAXRECIEVINGACCOUNT"), creditPostingAction, 
	        			 getF_IN_CreditTaxTxnCode(), creditTaxNarrative,
	        			 getF_IN_CreditTaxCurrency(), (BigDecimal) chargeMap.get("TAXEXCHANGERATE"));
	        	
	        	postingMsgIBOList.addIBOFinancialPostingMsg(taxCreditLeg);
        	}
        	
        }
        
        HashMap paramsForPosting = new HashMap();
        HashMap outputParamsFromPosting = null;
        
            postingMsgList = createPostingMessages(postingMsgIBOList, env);
           
            
            VectorTable postingMessagesVector = new VectorTable();

            for (IPostingMessage postingMessage : postingMsgList) {

                IBOFinancialPostingMessage financialPostingMessage = (IBOFinancialPostingMessage) postingMessage;
                postingMessagesVector.addAll(new VectorTable(financialPostingMessage.getDataMap()));
            }

            if (postingMessagesVector.size() > 0) {

            	BankFusionThreadLocal.setChannel("SWIFT");
            	BankFusionThreadLocal.setApplicationID("UniversalBanking");
            	
                paramsForPosting.put("PostingMessages", postingMessagesVector);
                paramsForPosting.put("transactionID", getF_IN_TransactionId());
                paramsForPosting.put("suppressSchedulerIfForwardValued", true);
                paramsForPosting.put("isBlocking", false);
                paramsForPosting.put("manualValueTime", SystemInformationManager.getInstance().getBFBusinessTime());
				IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory
						.getInstance().getServiceManager()
						.getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
				
				outputParamsFromPosting = tryPosting(paramsForPosting);
					
							
			}	
            String outputTransactionId = (String) outputParamsFromPosting.get("transactionID");
            VectorTable postingRespVector = new VectorTable();
            postingRespVector = (VectorTable) outputParamsFromPosting.get("postingResponseMessage");
            String postingStatus = StringUtils.EMPTY;
            String message = (String)postingRespVector.getRowTags(0).get("MESSAGE");
            if(!(message.equalsIgnoreCase("Transaction Completed."))) {
            	postingStatus = message;
            }
            setF_OUT_PostingStatus(postingStatus);
            setF_OUT_transactionID(outputTransactionId);
            
}

	private HashMap tryPosting(HashMap paramsForPosting) {
	
		return MFExecuter.executeMF(CB_CMN_COLLECTIONPOSTING_MESSAGE_SRV, paramsForPosting,
                    BankFusionThreadLocal.getUserLocator().getStringRepresentation());
    	
	}

	private ArrayList<IBOFinancialPostingMessage> createPostingMessages(IBOFinancialPostingMsgList postingContext, BankFusionEnvironment env) {
		
		logger.info("Start of createPostingMessages");

        int msgCnt = postingContext.getIBOFinancialPostingMsgCount();
        ArrayList<IBOFinancialPostingMessage> FINPostingMsges = new ArrayList<IBOFinancialPostingMessage>();

        for (int i = 0; i < msgCnt; i++) {

			IBOFinancialPostingMessage postingMessage = (IBOFinancialPostingMessage) BankFusionThreadLocal
					.getPersistanceFactory().getStatelessNewInstance(IBOFinancialPostingMessage.BONAME);

            FatomUtils.createStandardItemsMessage(postingMessage, env);
            PostingHelper.setDefaultValuesForFinPosting(postingMessage, env);

			BigDecimal txnAmount = postingContext.getIBOFinancialPostingMsg(i).getTransactionAmount();

            postingMessage.setPrimaryID(postingContext.getIBOFinancialPostingMsg(i).getAccountID());// account
            postingMessage.setAcctCurrencyCode(postingContext.getIBOFinancialPostingMsg(i).getAccountCurrency());
			postingMessage.setF_AMOUNT(txnAmount);
            postingMessage.setTransCode(postingContext.getIBOFinancialPostingMsg(i).getTransactionCode());
            //postingMessage.setTransCode(getMisTranxCode(postingContext.getIBOFinancialPostingMsg(i).getCreditDebitFlag()));// Module
            postingMessage.setTransactionRef(postingContext.getIBOFinancialPostingMsg(i).getTransactionReference());
            postingMessage.setBranchID(postingContext.getIBOFinancialPostingMsg(i).getBranchID());
            postingMessage.setTransactionDate(SystemInformationManager.getInstance().getBFBusinessDate());
			postingMessage.setF_ACTUALAMOUNT(txnAmount);
            postingMessage.setF_TXNCURRENCYCODE(postingContext.getIBOFinancialPostingMsg(i).getTransactionCurrency());
            postingMessage.setF_CHANNELID(postingContext.getIBOFinancialPostingMsg(i).getChannelID());


            postingMessage.setNarrative(postingContext.getIBOFinancialPostingMsg(i).getNarration());

            if (postingContext.getIBOFinancialPostingMsg(i).getCreditDebitFlag().equalsIgnoreCase("C")) {

				postingMessage.setSign('+');
				postingMessage.setF_AMOUNTCREDIT(txnAmount);
			}

            if (postingContext.getIBOFinancialPostingMsg(i).getCreditDebitFlag().equalsIgnoreCase("D")) {

				postingMessage.setSign('-');
				postingMessage.setF_AMOUNTDEBIT(txnAmount);
			}

            if (postingContext.getIBOFinancialPostingMsg(i).getValueDate() != null) {

                postingMessage.setValueDate(postingContext.getIBOFinancialPostingMsg(i).getValueDate());
            } else {

                postingMessage.setValueDate(SystemInformationManager.getInstance().getBFBusinessDate());
            }

            FINPostingMsges.add(postingMessage);
        }
        logger.info("End of createPostingMessages");

        return FINPostingMsges;
}

	private IBOFinancialPostingMsg populatePostingMessage(BigDecimal amount, String account, String postingAction,
			String txnCode, String narrative, String txnCurrency, BigDecimal exchangeRate) {
		IBOFinancialPostingMsg postingMsg = new IBOFinancialPostingMsg();
		
		postingMsg.setTransactionAmount(amount);
		postingMsg.setAccountID(account);
		postingMsg.setCreditDebitFlag(postingAction);
		postingMsg.setTransactionCurrency(txnCurrency);
		postingMsg.setTransactionReference(getF_IN_TxnReference());
		postingMsg.setTransactionCode(txnCode);
		postingMsg.setNarration(narrative);
		postingMsg.setChannelID(getF_IN_ChannelId());
		postingMsg.setAccountCurrency(getAccountCurrency(account));
		postingMsg.setBranchID(getAccountBranch(account));
		/*DateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		java.util.Date sourceDate = sourceFormat.parse((String)getF_IN_ManualValueDate());
		java.sql.Date sqlDate = new java.sql.Date(sourceDate.getTime());
		postingMsg.setValueDate(sqlDate);*/
		postingMsg.setExchangeRate(exchangeRate);
		
		
		
		return postingMsg;
}

	private String getAccountBranch(String account) {
		// TODO Auto-generated method stub
		PaymentSwiftUtils utils = new PaymentSwiftUtils();
		String accountBranch = StringUtils.EMPTY;
		IBOAttributeCollectionFeature accountIbo = utils.getAccountDetails(account);
		if (null != accountIbo) {
			accountBranch = accountIbo.getF_BRANCHSORTCODE();
		}
		return accountBranch;
	}

	private String getAccountCurrency(String account) {
		PaymentSwiftUtils utils = new PaymentSwiftUtils();
		String accountCurrency = StringUtils.EMPTY;
		IBOAttributeCollectionFeature accountIbo = utils.getAccountDetails(account);
		if (null != accountIbo) {
			accountCurrency = accountIbo.getF_ISOCURRENCYCODE();
		}
		return accountCurrency;
		
	}
    	
}
