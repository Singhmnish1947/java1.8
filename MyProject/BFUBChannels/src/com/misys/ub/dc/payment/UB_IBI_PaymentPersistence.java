package com.misys.ub.dc.payment;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.interfaces.IfmConstants;
import com.misys.ub.interfaces.UB_IBI_PaymentsHelper;
import com.trapedza.bankfusion.bo.refimpl.IBOCB_CMN_NarrativeTemplate;
import com.trapedza.bankfusion.bo.refimpl.IBOTransactionScreenControl;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.fatoms.UB_IND_PaymentPostingFatom;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;

@SuppressWarnings("rawtypes")
public class UB_IBI_PaymentPersistence {
	
	private IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

	private static final transient Log LOGGER = LogFactory
			.getLog(UB_IBI_PaymentPersistence.class.getName());
	
	public static boolean postTransactions(
			String exchangeRate,
			String channelId,
			String txnReference,
			String transactionID,
			String inputMsg_TransactionReference,
			String debitAccBranchSortCode, 
			BigDecimal debitAmount,
			BigDecimal txnAmtInCreditAccCurrency,
			BigDecimal txnAmount,
			String txnCurrency,
			
			
			String dr_Account,
			String dr_Accountname,
			String dr_Currency,
			String dr_TxnCode,
			String cr_Account,
			String cr_AccountName,
			String cr_TxnCode,
			String cr_Currency,
			
			boolean updateCounterPartyInfo,
			String txnType,
			boolean isLoanPayment,
			String swiftCounterPartyAccount,
			String swiftCounterPartyName,
			Date transferDt
			) {
		LOGGER.info("Start of postTransactions");
		String debitTxnRef = getNarrative(dr_TxnCode, "debit",
				inputMsg_TransactionReference);
		String creditTxnRef = getNarrative(cr_TxnCode, "credit",
				inputMsg_TransactionReference);
		
		if(isLoanPayment){
		    /*Loan payment transfer transaction */
		    UB_IBI_LoanPayment.loanPosting(txnAmount,dr_Account,cr_Account,dr_Accountname,cr_AccountName,
		                                   dr_TxnCode,cr_TxnCode,debitTxnRef,creditTxnRef,txnReference,
		                                   transactionID,debitAmount,txnAmtInCreditAccCurrency,txnCurrency,
		                                   exchangeRate,debitAccBranchSortCode,channelId);
		    
		}else{
		    /* Main payment transfer transaction */
	        UB_IND_PaymentPostingFatom.posting(dr_TxnCode, exchangeRate, cr_Account, debitAmount,
	                cr_TxnCode, dr_Account, debitTxnRef, creditTxnRef,
	                dr_Currency, cr_Currency, txnAmtInCreditAccCurrency,
	                txnReference, transactionID, debitAccBranchSortCode,
	                channelId,transferDt);
	       
		}
		
		if(updateCounterPartyInfo){
			LOGGER.info("-- updating counter party info channel IBI");
			//Changes for FBIT-2434
			if(txnType.equalsIgnoreCase("INTRAPYMT") || txnType.equalsIgnoreCase("INTERNALPYMT") || txnType.equalsIgnoreCase("MOBILETOPUP") ){
			/* Counter Party detail populated in Counter Party table */
				UB_IND_PaymentPostingFatom.postCounterPartyDetails(transactionID, cr_Account,
						cr_AccountName,
						UB_IND_PaymentPostingFatom.OUTGOING_IDENTIFIER, channelId);
				UB_IND_PaymentPostingFatom.postCounterPartyDetails(transactionID, dr_Account, dr_Accountname,
						UB_IND_PaymentPostingFatom.INCOMING_IDENTIFIER, channelId);
			} else {
				UB_IND_PaymentPostingFatom.postCounterPartyDetails(transactionID, swiftCounterPartyAccount,
						swiftCounterPartyName,
						UB_IND_PaymentPostingFatom.OUTGOING_IDENTIFIER, channelId);
			}
				
		}
		LOGGER.info("End of postTransactions");
		return true;
	}

		
	public static HashMap postCharges(
				String txnType,
				String txnReference,
				String transactionID,
				String spotPseudonym,
				String positionAccountContext,
				int chargeIndicator,
				String channelId,
				BigDecimal txnAmount,
	
				String fromAccount,
				String chargeFundingAccount,
				
				String trasnferCurrency,
				String fromAccountCurrency,
				String chargeFundingAccountCurrency,

				String dr_TxnCode,
				String dr_AccountBranchSortCode,
				String cr_TxnCode,
				String cr_AccountBranchSortCode,
				String creditAccountNumber,
				Boolean error
			){
		LOGGER.info("Start of postCharges");				
		HashMap onlineCharges = UB_IBI_PaymentsHelper.fetchOnlinecharges(trasnferCurrency,fromAccount,chargeFundingAccount, txnAmount, txnType, dr_TxnCode,cr_TxnCode, creditAccountNumber, true);
        
		VectorTable vector = (VectorTable) onlineCharges.get("RESULT");
		//Changes for FBIT-2434
		if(txnType.equalsIgnoreCase("INTRAPYMT") || txnType.equalsIgnoreCase("INTERNALPYMT") || txnType.equalsIgnoreCase("INTERNALSOPYMT") || txnType.equalsIgnoreCase("MOBILETOPUP")){
			
			/* Loop for online charges and taxes */
			for (int i = 0; i < vector.size(); i++) {
				HashMap map = vector.getRowTags(i);
				String chgRecievingAccountCurrency = (String) map.get("CHARGECURRENCY");
                boolean chargeWaived = (boolean) map.get("CHARGEWAIVED");

				if (!(chargeFundingAccountCurrency
						.equalsIgnoreCase(chgRecievingAccountCurrency))
						&& (UB_IBI_PaymentsHelper.arePositionAccountsAvailable(
								chargeFundingAccountCurrency,
								chgRecievingAccountCurrency, spotPseudonym,
								positionAccountContext, dr_AccountBranchSortCode,
								cr_AccountBranchSortCode) == false)) {
					// return "40580005";
					UB_IND_PaymentPostingFatom.handleEvent(40580005, new String[] {});
				}
				

                if (!chargeWaived) {

                    doChargePosting(map,chargeFundingAccount,chargeFundingAccountCurrency,txnReference,transactionID,
                            dr_AccountBranchSortCode,channelId);
                        

				
				if (error)
					UB_IND_PaymentPostingFatom.handleEvent(40580005, new String[] {});
				
				
				doTaxPosting(map,chargeFundingAccount,chargeFundingAccountCurrency,txnReference,transactionID,
						dr_AccountBranchSortCode,channelId);

                }

			}

		} else if(txnType.equalsIgnoreCase("INTNAT")){
	        for (int i = 0; i < vector.size(); i++) {
	        	HashMap map = vector.getRowTags(i);
                boolean chargeWaived = (boolean) map.get("CHARGEWAIVED");
                /* Online charge posting */
	        	

                if (!chargeWaived) {
	        	doChargePosting(map, chargeFundingAccount, chargeFundingAccountCurrency, txnReference, transactionID, dr_AccountBranchSortCode, channelId);

	            if (error)
	                UB_IND_PaymentPostingFatom.handleEvent(40422013, new String[] {});
	            // return "40422013";
	            /* Tax posting for charges */
                    doTaxPosting(map, chargeFundingAccount, chargeFundingAccountCurrency, txnReference, transactionID,
                            dr_AccountBranchSortCode, channelId);
	            if (error)
	            	UB_IND_PaymentPostingFatom.handleEvent(40422013, new String[] {});
                }

	        }
		}
		
		HashMap<String, Object> MfParams = new HashMap<String, Object>();
		MfParams.put("ChargeDetailsVector", vector);
		MfParams.put("AccountId", fromAccount);
		MfParams.put("TransactionId", transactionID);
		MfParams.put("CHARGEFUNDINGACCOUNTID", chargeFundingAccount);
		MfParams.put("CHARGEINDICATOR", chargeIndicator);
		MfParams.put("TRANSACTIONCODE", dr_TxnCode);
		LOGGER.info("Calling UB_CHG_UpdateChargeHistory_SRV - Microflow");
		MFExecuter.executeMF(UB_IND_PaymentPostingFatom.UPDATE_CHARGES_MFID, BankFusionThreadLocal.getBankFusionEnvironment(), MfParams);
		LOGGER.info("End of postCharges");
		return onlineCharges;
	}
	
	private static void doChargePosting(HashMap map,String chargeFundingAccount,String chargeFundingAccountCurrency,
							String txnReference,String transactionID, String dr_AccountBranchSortCode, String channelId){
		
		UB_IND_PaymentPostingFatom.posting((String) map.get(IfmConstants.CHARGEPOSTINGTXNCODE),
				(String) map.get(IfmConstants.CHARGEEXCHANGERATETYPE),
				(String) map.get(IfmConstants.CHARGERECIEVINGACCOUNT),
				(BigDecimal) map
						.get(IfmConstants.CHARGEAMOUNT_IN_FUND_ACC_CURRENCY),
				(String) map.get(IfmConstants.CHARGEPOSTINGTXNCODE),
				chargeFundingAccount,
				(String) map.get(IfmConstants.CHARGENARRATIVE),
				(String) map.get(IfmConstants.CHARGENARRATIVE),
				chargeFundingAccountCurrency,
				(String) map.get(UB_IND_PaymentPostingFatom.chargeCode),
				(BigDecimal) map.get(IfmConstants.CHARGEAMOUNT_IN_FUND_ACC_CURRENCY),
				txnReference, 
				transactionID, 
				dr_AccountBranchSortCode, 
				channelId,null);
	}
	
	private static void doTaxPosting(HashMap map,String chargeFundingAccount,String chargeFundingAccountCurrency,
			String txnReference,String transactionID, String dr_AccountBranchSortCode, String channelId){
		
		UB_IND_PaymentPostingFatom.posting((String) map.get(IfmConstants.TAXPOSTINGTXNCODE),
				(String) map.get(IfmConstants.TAXEXCHANGERATETYPE),
				(String) map.get(IfmConstants.TAXRECIEVINGACCOUNT),
				(BigDecimal) map.get(UB_IND_PaymentPostingFatom.TAXAMOUNT_IN_FUND_ACC_CURRENCY),
				(String) map.get(IfmConstants.TAXPOSTINGTXNCODE),
				chargeFundingAccount,
				(String) map.get(IfmConstants.TAXNARRATIVE),
				(String) map.get(IfmConstants.TAXNARRATIVE),
				chargeFundingAccountCurrency, 
				(String) map.get(UB_IND_PaymentPostingFatom.taxCode),
				(BigDecimal) map.get(UB_IND_PaymentPostingFatom.TAXAMOUNT_IN_FUND_ACC_CURRENCY),
				txnReference, 
				transactionID, 
				dr_AccountBranchSortCode,
				channelId,null
				);
	}
	
	

	private static String getNarrative(String code, String type, String payRef) {
        SimplePersistentObject narrativeDtl = new UB_IBI_PaymentPersistence().factory.findByPrimaryKey(IBOTransactionScreenControl.BONAME, code, false);
        String description = CommonConstants.EMPTY_STRING;
        if (narrativeDtl.getDataMap().size() > 0) {
            String mainNarrative = CommonConstants.EMPTY_STRING;

            if (type.equalsIgnoreCase("debit")) {
                mainNarrative = narrativeDtl.getDataMap().get("f_UBMAINNARRATIVE").toString();
            }
            else {
                mainNarrative = narrativeDtl.getDataMap().get("f_UBCONTRANARRATIVE1").toString();
                if (mainNarrative.equalsIgnoreCase(CommonConstants.EMPTY_STRING)) {
                    mainNarrative = narrativeDtl.getDataMap().get("f_UBCONTRANARRATIVE2").toString();
                }
                if (mainNarrative.equalsIgnoreCase(CommonConstants.EMPTY_STRING)) {
                    mainNarrative = narrativeDtl.getDataMap().get("f_UBCONTRANARRATIVE3").toString();
                }
            }
            if (!mainNarrative.equalsIgnoreCase(CommonConstants.EMPTY_STRING)) {
                IBOCB_CMN_NarrativeTemplate narrativeTemplate = ((IBOCB_CMN_NarrativeTemplate) BankFusionThreadLocal
                        .getPersistanceFactory().findByPrimaryKey(IBOCB_CMN_NarrativeTemplate.BONAME, mainNarrative, true));
                String narrative = narrativeTemplate != null ? narrativeTemplate.getF_NARRATIVEPATTERN()
                        : CommonConstants.EMPTY_STRING;
                if (description.equalsIgnoreCase(CommonConstants.EMPTY_STRING)) {
                    description = description.concat(narrative);
                }
                else {
                    description = description.concat(" " + narrative);
                }
            }
        }
        if (null != payRef && !CommonConstants.EMPTY_STRING.equals(payRef)) {
            return description + " " + payRef;
        }

        return description;
    }

}
