package com.misys.ub.fatoms;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.finastra.fbe.fatoms.UpdateAccountChargeThresholdCounter;
import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.ub.interfaces.IfmConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOATMActivityDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOATMSettlementAccount;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ATM_ChargesMsgProcessor;
import com.trapedza.bankfusion.utils.GUIDGen;

import bf.com.misys.cbs.msgs.v1r0.ReadAccountRq;
import bf.com.misys.cbs.msgs.v1r0.ReadAccountRs;
import bf.com.misys.cbs.services.ReadModuleConfigurationRq;
import bf.com.misys.cbs.services.ReadModuleConfigurationRs;
import bf.com.misys.cbs.types.AccountKeys;
import bf.com.misys.cbs.types.ChargeThresholdCounterUpdateRq;
import bf.com.misys.cbs.types.ChargeWaiverConfigRq;
import bf.com.misys.cbs.types.ChargeWaiverConfigRs;
import bf.com.misys.cbs.types.ModuleKeyRq;
import bf.com.misys.cbs.types.Pseudonym;
import bf.com.misys.ub.types.atm.postingvector.UB_Atm_PostingMessages;

public class ATM_ChargesMsgProcessor extends AbstractUB_ATM_ChargesMsgProcessor {

	private static final String DEFAULT = "DEFAULT";
	private static final String ATM_TRANSTYPE_02 = "02";
	private static final String HEADOFFICEBRANCH = "HEADOFFICEBRANCH";
	private static final String CASHDEP_TXN_CODE = "CSHDEP";
	private static final String CHARGEFUNDINGACCOUNTID = "CHARGEFUNDINGACCOUNTID";
	private static final String ISOCURRENCYCODE = "ISOCURRENCYCODE";
	private static final String MIS_TXN_CODE = "MISTxnCode";
	private static final String CASH_DEPOSIT_MSG_TYPE = "ATMCashDeposit";
	private static final String BRANCHSORTCODE = "BRANCHSORTCODE";
	private static final String FINANCIAL_POSTING_MESSAGE = "N";
	private static final String DEBIT_SIGN = "-";
	private static final String CREDIT_SIGN = "+";
	private static final String CONSOLIDATEDTAXAMT = "CONSOLIDATEDTAXAMT";
	private static final String DESCRIPTION = "Description";
	private static final String RESULT = "RESULT";
	private static final String CONSOLIDATEDCHARGEAMT = "CONSOLIDATEDCHARGEAMT";
	private static final String CHARGESELECT = "CHARGESELECT";
	private static final String ACCOUNTID = "ACCOUNTID";
	private static final String TXN_CODE_CHG_CONSOLIDATED_POSTING = "TXN_CODE_CHG_CONSOLIDATED_POSTING";
	private static final String IS_CHG_POSTING_CONSOLIDATED = "IS_CHG_POSTING_CONSOLIDATED";
	private static final String ATM = "ATM";
	private static final String CHARGE_WAIVER_RQ = "chargeWaiverConfigRq";
	private static final String CHARGE_WAIVER_RS = "chargeWaiverConfigRs";
	private static final String BRANCH = "BRANCH";
	private static final String READ_ACCT_RQ = "ReadAccountRq";
	private static final String READ_ACCT_RS = "ReadAccountRs";
	private static final String ATM_TRANS_TYPE = "AtmTransType";
	private static final String ATM_TXN_CODE = "AtmTxnCode";
	private static final String ACCOUNTNUM = "ACCOUNTNUM";
	private static final String READ_MODULE_CONFIG_RQ = "ReadModuleConfigurationRq";
	private static final String READ_MODULE_CONFIG_RS = "ReadModuleConfigurationRs";
	private static final String CONTRA_ACCOUNT_TAG_NAME = "1_contraAccountNumber";
	private static final String TXN_CURRENCY_TAG_NAME = "TxnCurrency";
	private static final String TXN_AMOUNT_TAG_NAME = "1_postingMessageTransactionAmount";
	private static final String BRANCH_CODE_ATM_TAG_NAME = "BranchCodeATM";
	private static final String TXN_CODE_TAG_NAME = "1_postingMessageTransactionCode";
	private static final String ISO_CURRENCY_CODE_TAG_NAME = "1_postingMessageISOCurrencyCode";
	private static final String ACCOUNT_ID_TAG_NAME = "1_postingMessageAccountId";
	private static final String READ_MODULE_CONFIG_MF = "CB_CMN_ReadModuleConfiguration_SRV";
	private static final String FETCH_ACCOUNT_SERVICE_MF = "UB_CMN_FetchAccountService";
	private static final String FETCH_ATM_TXN_CODE_MF = "UB_ATM_FetchAtmTxnCodeDtls_SRV";
	private static final String GET_ACCT_IDENTIFIER_MF = "UB_FIN_GetAccountByAccountIdentifier_SRV";
	private static final String CALCULATE_ONLINE_CHARGE_MF = "UB_CHG_CalculateOnlineCharges_SRV";
	private static final String GET_CHARGE_WAIVER_MF = "CB_CHG_GetAccountChargeWaiverConfiguration_SRV";
	
	private static final transient Log logger = LogFactory.getLog(ATM_ChargesMsgProcessor.class.getName());

	public ATM_ChargesMsgProcessor(BankFusionEnvironment env) {
		super(env);

	}

	@Override
	public void process(BankFusionEnvironment env) {
		logger.debug("Calculating Charge for ATM/POS transaction with reference " + getF_IN_RetrievalReferenceNumber());
		Map<String, Object> atmTxnCodeDtlsMap = fetchAtmTxnCodeDtls();
		String txnCode = (String) atmTxnCodeDtlsMap.get(MIS_TXN_CODE);
		String description = (String) atmTxnCodeDtlsMap.get(DESCRIPTION);
		String referenceNo = getF_IN_RetrievalReferenceNumber();
		boolean isChargeWaivedDuringBlock = false;
		setF_OUT_AtmTxnDesc(description);
		String accountId = "";
		if (getF_IN_MessageType()
				.equals(CASH_DEPOSIT_MSG_TYPE)) {
			accountId = getF_IN_AccountNumber2();
		} else {
			accountId = getF_IN_AccountNumber1();
		}
		if("ATMAccPOS".equals(getF_IN_MessageType())) {
		    isChargeWaivedDuringBlock = checkIsChargeWaivedDuringBlock(referenceNo,BankFusionThreadLocal.getPersistanceFactory());
		}
		Map accntServiceMap = fetchAccountService(accountId);
		String chargeFundingAcc = (String) accntServiceMap.get(CHARGEFUNDINGACCOUNTID);
		if (chargeFundingAcc.isEmpty()) {
			chargeFundingAcc = (String) accntServiceMap.get(ACCOUNTID);
		}
		String isoCurrency = (String) accntServiceMap.get(ISOCURRENCYCODE);

		String branchCode = getBranchCode();
		Map calcOnlineChrgMap = getCalculateOnlineCharges(accountId, isoCurrency, txnCode, branchCode);
		BigDecimal consolidatedChargeAmt = (BigDecimal) calcOnlineChrgMap.get(CONSOLIDATEDCHARGEAMT);
		BigDecimal consolidateTaxAmt = (BigDecimal) calcOnlineChrgMap.get(CONSOLIDATEDTAXAMT);
		VectorTable calcResult = (VectorTable) calcOnlineChrgMap.get(RESULT);

		BigDecimal result = consolidatedChargeAmt.add(consolidateTaxAmt);
		logger.debug("Total charges/taxes calculated for transaction with reference " + getF_IN_RetrievalReferenceNumber() + " are " + result);
		if (result.equals(BigDecimal.ZERO)) {
			setF_OUT_IN_CTYPE_ATMPostingMsg(getF_IN_IN_CTYPE_AtmPostingMsg());
		} else {
			ChargeWaiverConfigRs chargeWaiverConfigRs = getChargeWaiverConfig(accountId, txnCode);
			boolean isChargeWaivedBasedOnCounter = chargeWaiverConfigRs.isChargeWaived();
			String waiveChargeExpenseAcct = chargeWaiverConfigRs.getExpenseAccountId();
			if (isChargeWaivedBasedOnCounter||isChargeWaivedDuringBlock) {
			  if(isChargeWaivedDuringBlock && StringUtils.isBlank(waiveChargeExpenseAcct)) {
			      updateChargeCounter(accountId, "D", txnCode, SystemInformationManager.getInstance().getBFBusinessDate());
			      chargeWaiverConfigRs = getChargeWaiverConfig(accountId, txnCode);
			      waiveChargeExpenseAcct = chargeWaiverConfigRs.getExpenseAccountId();
			      updateChargeCounter(accountId, "I", txnCode, SystemInformationManager.getInstance().getBFBusinessDate());
			  }
				chargeFundingAcc = getWaiverChargeFundingAccount(waiveChargeExpenseAcct, branchCode, isoCurrency);
				prepareChargeAndTaxPostingMessage(calcResult, chargeFundingAcc);
				if(isChargeWaivedDuringBlock) {
				    setF_OUT_isChargeWaivedBasedOnCounter(Boolean.FALSE);
				} else {
				    setF_OUT_isChargeWaivedBasedOnCounter(isChargeWaivedBasedOnCounter);
				}
				setF_OUT_AccountId(accountId);
				setF_OUT_TransactionCode(txnCode);

			} else {
				// readModuleConfiguration to get consolidatedPosting from ATM module.
				Boolean isPostConsolidated = Boolean
						.valueOf(getModuleConfiguration(IS_CHG_POSTING_CONSOLIDATED, ATM).getModuleConfigDetails()
								.getValue());
				if (isPostConsolidated) {
					prepareConsolidatedPostingMessage(calcResult, chargeFundingAcc);
				} else {
					prepareChargeAndTaxPostingMessage(calcResult, chargeFundingAcc);
				}
			}
		}
		setF_OUT_RESULT(calcResult);
		logger.debug("End of charge calculation process for ATM/POS transaction with reference " + getF_IN_RetrievalReferenceNumber());
	}

	private boolean updateChargeCounter(String accountId, String action, String misTxnCode, Date txnDate) {
      UpdateAccountChargeThresholdCounter upDateAccChargeThresholdCounter =
          new UpdateAccountChargeThresholdCounter(BankFusionThreadLocal.getBankFusionEnvironment());
      ChargeThresholdCounterUpdateRq param = new ChargeThresholdCounterUpdateRq();
      param.setAccountId(accountId);
      param.setAction(action);
      param.setMisTxnCode(misTxnCode);
      param.setTxnDate(SystemInformationManager.getInstance().getBFBusinessDate());
      upDateAccChargeThresholdCounter.setF_IN_chargeThresholdCounterUpdateRq(param);
      upDateAccChargeThresholdCounter.process(BankFusionThreadLocal.getBankFusionEnvironment());
      return upDateAccChargeThresholdCounter.getF_OUT_chargeThresholdCounterUpdateRs().getStatus();
  }
	
    private boolean checkIsChargeWaivedDuringBlock(String referenceNo, IPersistenceObjectsFactory persistanceFactory) {
        // TODO Auto-generated method stub
        ArrayList param = new ArrayList();
        boolean isChargeWaived = false;
        param.add(referenceNo);
        List<IBOATMActivityDetail> atmActDtl = null;
        try {
            atmActDtl = (List<IBOATMActivityDetail>) persistanceFactory.findByQuery(IBOATMActivityDetail.BONAME,
                "where " + IBOATMActivityDetail.TRANSACTIONREFERENCE + " = ?", param, null);

            if (null != atmActDtl && !atmActDtl.isEmpty()) {
                isChargeWaived = atmActDtl.get(0).isF_UBISCHARGEWAIVED();
                return isChargeWaived;
            }
            else {
                return false;
            }

        } catch (Exception e) {
            return false;
        }

    }

    private ChargeWaiverConfigRs getChargeWaiverConfig(String accountId, String txnCode) {
		HashMap<String, Object> inputParams = new HashMap<>();
		ChargeWaiverConfigRq chargeWaiverConfigRq = new ChargeWaiverConfigRq();
		chargeWaiverConfigRq.setAccountId(accountId);
		chargeWaiverConfigRq.setMisTxnCode(txnCode);
		inputParams.put(CHARGE_WAIVER_RQ, chargeWaiverConfigRq);
		Map<String, Object> resultMap = MFExecuter.executeMF(GET_CHARGE_WAIVER_MF,
				BankFusionThreadLocal.getBankFusionEnvironment(), inputParams);
		return (ChargeWaiverConfigRs) resultMap.get(CHARGE_WAIVER_RS);
	}

	private String getWaiverChargeFundingAccount(String waiveChargeExpenseAcct, String branchCode, String currency) {
		ReadAccountRq readAccount = new ReadAccountRq();
		AccountKeys accountKey = new AccountKeys();
		Pseudonym pseudonym = new Pseudonym();

		pseudonym.setPseudonymId(waiveChargeExpenseAcct);
		pseudonym.setBranchCode(branchCode);
		pseudonym.setContextType(BRANCH);
		pseudonym.setContextValue(branchCode);
		pseudonym.setIsoCurrencyCode(currency);

		accountKey.setPseudonym(pseudonym);
		readAccount.setAccountKeys(accountKey);

		Map<String, Object> inputMap = new HashMap<>();
		inputMap.put(READ_ACCT_RQ, readAccount);
		Map outputMap = MFExecuter.executeMF(GET_ACCT_IDENTIFIER_MF,
				BankFusionThreadLocal.getBankFusionEnvironment(), inputMap);
		ReadAccountRs readAccountRs = (ReadAccountRs) outputMap.get(READ_ACCT_RS);
		return readAccountRs.getAccountDetails().getAccountInfo().getAcctBasicDetails()
				.getAccountKeys().getStandardAccountId();
	}
	// conditionalStep1
	public boolean isForcePost() {
		boolean forcePost = false;
		if(CASHDEP_TXN_CODE.equals(getF_OUT_TransactionCode())) {
			forcePost=  true;
		}
		return forcePost;
	}

	public Map<String, Object> fetchAtmTxnCodeDtls() {
		Map<String, Object> inputMap = new HashMap<>();
		inputMap.put(ATM_TRANS_TYPE, getF_IN_AtmTransType());
		inputMap.put(ATM_TXN_CODE, getF_IN_ATMTransactionCode());
		Map outputMap = MFExecuter.executeMF(FETCH_ATM_TXN_CODE_MF,
				BankFusionThreadLocal.getBankFusionEnvironment(), inputMap);
		return outputMap;
	}

	public Map<String, Object> fetchAccountService(String accountNo) {
		Map<String, Object> inputMap = new HashMap<>();
		inputMap.put(ACCOUNTNUM, accountNo);
		Map outputMap = MFExecuter.executeMF(FETCH_ACCOUNT_SERVICE_MF,
				BankFusionThreadLocal.getBankFusionEnvironment(), inputMap);
		return outputMap;
	}

	public ReadModuleConfigurationRs getModuleConfiguration(String key, String moduleId) {
		Map<String, Object> inputMap = new HashMap<>();
		ReadModuleConfigurationRq readModuleConfigurationRq = new ReadModuleConfigurationRq();
		ModuleKeyRq moduleKeyRq = new ModuleKeyRq();
		moduleKeyRq.setKey(key);
		moduleKeyRq.setModuleId(moduleId);
		readModuleConfigurationRq.setModuleKeyRq(moduleKeyRq); // object-inside object
		inputMap.put(READ_MODULE_CONFIG_RQ, readModuleConfigurationRq);
		Map outputMap = MFExecuter.executeMF(READ_MODULE_CONFIG_MF,
				BankFusionThreadLocal.getBankFusionEnvironment(), inputMap);
		return (ReadModuleConfigurationRs) outputMap.get(READ_MODULE_CONFIG_RS);
	}

	public Map<String, Object> getCalculateOnlineCharges(String accountId, String currencyCode, String transactionCode,
			String branchCode) {
		Map<String, Object> inputMap = new HashMap<>();
		inputMap.put(CONTRA_ACCOUNT_TAG_NAME, getF_IN_contraAccount());
		inputMap.put(TXN_AMOUNT_TAG_NAME, getF_IN_TransactionAmount());
		inputMap.put(TXN_CURRENCY_TAG_NAME, getF_IN_TransactionCurrencyCode());
		inputMap.put(BRANCH_CODE_ATM_TAG_NAME, branchCode);
		inputMap.put(TXN_CODE_TAG_NAME, transactionCode);
		inputMap.put(ISO_CURRENCY_CODE_TAG_NAME, currencyCode);
		inputMap.put(ACCOUNT_ID_TAG_NAME, accountId);

		Map outputMap = MFExecuter.executeMF(CALCULATE_ONLINE_CHARGE_MF,
				BankFusionThreadLocal.getBankFusionEnvironment(), inputMap);
		return outputMap;
	}

	// ConditionalStep2
	public String getBranchCode() {
		String branchCode = "";
		ReadModuleConfigurationRs readModuleConfigurationRs = getModuleConfiguration(CHARGESELECT, ATM);
		if (readModuleConfigurationRs.getModuleConfigDetails().getValue().equals(DEFAULT) || getF_IN_AtmTransType().equals(ATM_TRANSTYPE_02)) {
			ReadModuleConfigurationRs readModuleConfigurationRs2 = getModuleConfiguration(HEADOFFICEBRANCH, ATM);
			branchCode = readModuleConfigurationRs2.getModuleConfigDetails().getValue();
		} else {
			IBOATMSettlementAccount iboatmSettlementAccount = (IBOATMSettlementAccount) BankFusionThreadLocal
					.getPersistanceFactory().findByPrimaryKey(IBOATMSettlementAccount.BONAME,
							getF_IN_CardAcceptorId(), true);
			branchCode = iboatmSettlementAccount.getF_UBBRANCH();
		}
		return branchCode;
	}
	// ConditionalStep3 - tableLoop

	private UB_Atm_PostingMessages populatePostingMessage(BigDecimal amount, String account, String narrative,
			String sign, String transactionCode, String branchCode, String currencyCode) {
		UB_Atm_PostingMessages postingMessage = new UB_Atm_PostingMessages();
		setDefaultValues(postingMessage);
		postingMessage.setAMOUNT(amount);
		postingMessage.setBRANCHSORTCODE(branchCode);
		postingMessage.setACCTCURRENCYCODE(currencyCode);
		postingMessage
				.setCHANNELID(getF_IN_ProductIndicator());
		postingMessage.setCROSSCURRENCY(false);
		postingMessage.setFORCEDNOTICE(false);
		postingMessage.setFORCEPOST(isForcePost());
		postingMessage.setMESSAGEID(GUIDGen.getNewGUID());
		postingMessage.setMESSAGETYPE(FINANCIAL_POSTING_MESSAGE);
		postingMessage.setNARRATIVE(narrative);
		postingMessage.setPRIMARYID(account);
		postingMessage.setREVERSAL(false);
		postingMessage.setSIGN(sign);
		postingMessage.setTRANSACTIONCODE(transactionCode);
		postingMessage.setTRANSACTIONDATE(SystemInformationManager.getInstance().getBFBusinessDateTime());
		postingMessage.setTRANSACTIONID(GUIDGen.getNewGUID());
		postingMessage.setTRANSACTIONREF(getF_IN_RetrievalReferenceNumber());
		postingMessage.setTXNCURRENCYCODE(currencyCode);
		postingMessage.setVALUEDATE(getF_IN_TransactionDateTime());
		return postingMessage;
	}

	private void prepareChargeAndTaxPostingMessage(VectorTable results, String chargeFundingAcc) {
		Map fetchChargeFundingAcc = fetchAccountService(chargeFundingAcc);
		for (int i = 0; i < results.size(); i++) {
			HashMap map = results.getRowTags(i);
			Map receivingAccountDetails = fetchAccountService((String) map.get(IfmConstants.CHARGERECIEVINGACCOUNT));
			UB_Atm_PostingMessages chargeCreditMessage = populatePostingMessage(
					(BigDecimal) map.get(IfmConstants.CHARGEAMOUNT),
					(String) map.get(IfmConstants.CHARGERECIEVINGACCOUNT),
					(String) map.get(IfmConstants.CHARGENARRATIVE), CREDIT_SIGN,
					(String) map.get(IfmConstants.CHARGEPOSTINGTXNCODE),
					(String) receivingAccountDetails.get(BRANCHSORTCODE),
					(String) receivingAccountDetails.get(ISOCURRENCYCODE));
			getF_IN_IN_CTYPE_AtmPostingMsg().addUB_Atm_PostingMessages(chargeCreditMessage);

			UB_Atm_PostingMessages chargeDebitMessage = populatePostingMessage(
					(BigDecimal) map.get(IfmConstants.CHARGEAMOUNT_IN_FUND_ACC_CURRENCY), chargeFundingAcc,
					(String) map.get(IfmConstants.CHARGENARRATIVE), DEBIT_SIGN,
					(String) map.get(IfmConstants.CHARGEPOSTINGTXNCODE),
					(String) fetchChargeFundingAcc.get(BRANCHSORTCODE),
					(String) fetchChargeFundingAcc.get(ISOCURRENCYCODE));
			getF_IN_IN_CTYPE_AtmPostingMsg().addUB_Atm_PostingMessages(chargeDebitMessage);

			if (!map.get(IfmConstants.TAXAMOUNT).equals(BigDecimal.ZERO)) {

				// for Placeholder76 Cr_TaxPostingMessage
				String taxRecievingAccount = (String) map.get(IfmConstants.TAXRECIEVINGACCOUNT);
				Map taxRecievingAccountDetails = fetchAccountService(taxRecievingAccount);
				UB_Atm_PostingMessages taxCreditMessage = populatePostingMessage(
						(BigDecimal) map.get(IfmConstants.TAXAMOUNT), taxRecievingAccount,
						(String) map.get(IfmConstants.TAXNARRATIVE), CREDIT_SIGN,
						(String) map.get(IfmConstants.TAXCODE), (String) taxRecievingAccountDetails.get(BRANCHSORTCODE),
						(String) taxRecievingAccountDetails.get(ISOCURRENCYCODE));
				getF_IN_IN_CTYPE_AtmPostingMsg().addUB_Atm_PostingMessages(taxCreditMessage);

				// for Placeholder76 Dr_TaxPostingMessage
				UB_Atm_PostingMessages taxDebitMessage = populatePostingMessage(
						(BigDecimal) map.get(IfmConstants.TAXAMOUNT_IN_FUND_ACC_CURRENCY), chargeFundingAcc,
						(String) map.get(IfmConstants.TAXNARRATIVE), DEBIT_SIGN, (String) map.get(IfmConstants.TAXCODE),
						(String) fetchChargeFundingAcc.get(BRANCHSORTCODE),
						(String) fetchChargeFundingAcc.get(ISOCURRENCYCODE));
				getF_IN_IN_CTYPE_AtmPostingMsg().addUB_Atm_PostingMessages(taxDebitMessage);

			}
		} // end-of-loop
		setF_OUT_IN_CTYPE_ATMPostingMsg(getF_IN_IN_CTYPE_AtmPostingMsg());
		setF_OUT_accountCurrency((String) fetchChargeFundingAcc.get(ISOCURRENCYCODE));
	}

	private void prepareConsolidatedPostingMessage(VectorTable results, String chargeFundingAcc) {
		BigDecimal consolidatedChargeDebitAmount = BigDecimal.ZERO;
		BigDecimal consolidatedTaxDebitAmount = BigDecimal.ZERO;
		Map<String, Object> fetchChargeFundingAcc = fetchAccountService(chargeFundingAcc);
		for (int i = 0; i < results.size(); i++) {
			HashMap map = results.getRowTags(i);
			Map receivingAccountDetails = fetchAccountService((String) map.get(IfmConstants.CHARGERECIEVINGACCOUNT));
			UB_Atm_PostingMessages chargeCreditMessage = populatePostingMessage(
					(BigDecimal) map.get(IfmConstants.CHARGEAMOUNT),
					(String) map.get(IfmConstants.CHARGERECIEVINGACCOUNT),
					(String) map.get(IfmConstants.CHARGENARRATIVE), CREDIT_SIGN,
					(String) map.get(IfmConstants.CHARGEPOSTINGTXNCODE),
					(String) receivingAccountDetails.get(BRANCHSORTCODE),
					(String) receivingAccountDetails.get(ISOCURRENCYCODE));
			getF_IN_IN_CTYPE_AtmPostingMsg().addUB_Atm_PostingMessages(chargeCreditMessage);
			consolidatedChargeDebitAmount = consolidatedChargeDebitAmount
					.add((BigDecimal) map.get(IfmConstants.CHARGEAMOUNT_IN_FUND_ACC_CURRENCY));

			if (!map.get(IfmConstants.TAXAMOUNT).equals(BigDecimal.ZERO)) {
				String taxRecievingAccount = (String) map.get(IfmConstants.TAXRECIEVINGACCOUNT);
				Map taxRecievingAccountDetails = fetchAccountService(taxRecievingAccount);
				UB_Atm_PostingMessages taxCreditMessage = populatePostingMessage(
						(BigDecimal) map.get(IfmConstants.TAXAMOUNT), taxRecievingAccount,
						(String) map.get(IfmConstants.TAXNARRATIVE), CREDIT_SIGN,
						(String) map.get(IfmConstants.TAXCODE), (String) taxRecievingAccountDetails.get(BRANCHSORTCODE),
						(String) taxRecievingAccountDetails.get(ISOCURRENCYCODE));
				getF_IN_IN_CTYPE_AtmPostingMsg().addUB_Atm_PostingMessages(taxCreditMessage);
				consolidatedTaxDebitAmount = consolidatedTaxDebitAmount
						.add((BigDecimal) map.get(IfmConstants.TAXAMOUNT_IN_FUND_ACC_CURRENCY));

			}
		}
		// outside-loop : Charge and Tax Debit
		BigDecimal consolidatedChargeAndTaxAmt = consolidatedChargeDebitAmount.add(consolidatedTaxDebitAmount);
		String chargeTxnCode = getModuleConfiguration(TXN_CODE_CHG_CONSOLIDATED_POSTING, ATM).getModuleConfigDetails()
				.getValue();
		UB_Atm_PostingMessages chargeDebitMessage = populatePostingMessage(consolidatedChargeAndTaxAmt,
				chargeFundingAcc, "", DEBIT_SIGN, chargeTxnCode, (String) fetchChargeFundingAcc.get(BRANCHSORTCODE),
				(String) fetchChargeFundingAcc.get(ISOCURRENCYCODE));
		getF_IN_IN_CTYPE_AtmPostingMsg().addUB_Atm_PostingMessages(chargeDebitMessage);

		setF_OUT_IN_CTYPE_ATMPostingMsg(getF_IN_IN_CTYPE_AtmPostingMsg());

	}

	private void setDefaultValues(UB_Atm_PostingMessages postingMessage) {
		postingMessage.setFORCEDNOTICE(false);
		postingMessage.setREVERSAL(false);
		postingMessage.setCROSSCURRENCY(false);
		postingMessage.setEXCHRATE(BigDecimal.ZERO);
		postingMessage.setBASEEQUIVALENT(BigDecimal.ZERO);
		postingMessage.setTRANSACTIONCOUNTER(0);
		postingMessage.setDRAWERNUMBER(CommonConstants.EMPTY_STRING);
		postingMessage.setTRANSACTIONCROSSREFID(CommonConstants.EMPTY_STRING);
		postingMessage.setEXCHRATETYPE(CommonConstants.EMPTY_STRING);
		postingMessage.setACTUALAMOUNT(BigDecimal.ZERO);
		postingMessage.setAMOUNTCREDIT(BigDecimal.ZERO);
		postingMessage.setAMOUNTDEBIT(BigDecimal.ZERO);
		postingMessage.setNARRATIVE(CommonConstants.EMPTY_STRING);
	}
}
