package com.misys.ub.interfaces;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.msgs.v1r0.TransferForecastOrCreateRequest;
import bf.com.misys.cbs.services.CalcExchangeRateRq;
import bf.com.misys.cbs.services.CalcExchangeRateRs;
import bf.com.misys.cbs.services.ReadModuleConfigurationRq;
import bf.com.misys.cbs.services.ReadModuleConfigurationRs;
import bf.com.misys.cbs.types.CalcExchRateDetails;
import bf.com.misys.cbs.types.ExchangeRateDetails;
import bf.com.misys.cbs.types.ModuleKeyRq;
import bf.com.misys.cbs.types.NameAndAddress;
import bf.com.misys.cbs.types.TransferForecastInpDetails;
import bf.com.misys.financialposting.types.AccountPseudonym;
import bf.com.misys.financialposting.types.FxInfo;
import bf.com.misys.financialposting.types.PostingLeg;
import bf.com.misys.financialposting.types.TxnDetails;

import com.google.gson.JsonObject;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.bankfusion.subsystem.security.runtime.impl.BranchUtil;
import com.misys.cbs.config.ModuleConfiguration;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOProductInheritance;
import com.trapedza.bankfusion.bo.refimpl.IBOPseudonymAccountMap;
import com.trapedza.bankfusion.bo.refimpl.IBOSWTSettlementInstructionDetail;
import com.trapedza.bankfusion.core.BFCurrencyValue;
import com.trapedza.bankfusion.core.BankFusionException;
import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.trapedza.bankfusion.core.FinderMethods;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.fatoms.ExchangeRateFatom;
import com.trapedza.bankfusion.fatoms.UB_IND_PaymentPostingFatom;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.AvailableBalanceFunction;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;

public class UB_IBI_PaymentsHelper {

    private static final transient Log logger = LogFactory.getLog(com.misys.ub.interfaces.UB_IBI_PaymentsHelper.class);

    private static boolean limitExcessAction;
    public static final String BRANCH = "BRANCH";
    public static final String CURRENCY = "CURRENCY";

    public IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

    private static BigDecimal getAvailableBalance(String accountId) {
        
        Date valueDate = SystemInformationManager.getInstance().getBFBusinessDate();

        HashMap hashmapout = AvailableBalanceFunction.run(accountId, valueDate);
        BigDecimal availableBalance = (BigDecimal) hashmapout.get("AvailableBalance");
        limitExcessAction = (Boolean) hashmapout.get("IgnoreAvailableBalance");
        if (availableBalance == null)
            return CommonConstants.BIGDECIMAL_ZERO;
        else
            return availableBalance;
    }
      public static  String getNostroAcc(BankFusionEnvironment env, String branchSortCode, String transferCurrency,
			String creditCurrency, String pseudoname) {
    	String creditAccountNumber = null;
		List<SimplePersistentObject> dbRows = findAccountByPseudonameAndContext(env, BRANCH, branchSortCode,
				creditCurrency, pseudoname);
		if (dbRows != null && dbRows.size() > 0) {
			creditAccountNumber = dbRows.get(0).getDataMap().get("f_ACCOUNTID").toString();
		} else {
			dbRows = findAccountByPseudonameAndContext(env, CURRENCY, transferCurrency, creditCurrency, pseudoname);
			if (dbRows != null && dbRows.size() > 0) {
				creditAccountNumber = dbRows.get(0).getDataMap().get("f_ACCOUNTID").toString();
			}
		}
		return creditAccountNumber;
	}

	/**
	 * @param env
	 * @param sortContext
	 * @param sortContextValue
	 * @param isoCurrencyCode
	 * @param pseudoname
	 * @return
	 */
	private static List<SimplePersistentObject> findAccountByPseudonameAndContext(BankFusionEnvironment env,
			String sortContext, String sortContextValue, String isoCurrencyCode, String pseudoname) {
		ArrayList params = new ArrayList();
		params.add(false);
		params.add(pseudoname);
		params.add(sortContext);
		params.add(sortContextValue);
		params.add(isoCurrencyCode);
		final String findAccountByPseudonameCurrencyAndContext = " WHERE " + IBOPseudonymAccountMap.ISDELETED
				+ " = ? AND " + IBOPseudonymAccountMap.PSEUDONAME + " = ? AND " + IBOPseudonymAccountMap.SORTCONTEXT
				+ " = ? AND " + IBOPseudonymAccountMap.SORTCONTEXTVALUE + " = ? AND "
				+ IBOPseudonymAccountMap.ISOCURRENCYCODE + " = ?";
		List<SimplePersistentObject> dbRows = env.getFactory().findByQuery(IBOPseudonymAccountMap.BONAME,
				findAccountByPseudonameCurrencyAndContext, params, null, false);
		return dbRows;
	}
    public static boolean validateDate(String inDate) {
        boolean validDate = true;
        if (inDate == null) {
            validDate = false;
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            if (inDate.trim().length() != dateFormat.toPattern().length()) {
                validDate = false;
            } else {
                dateFormat.setLenient(false);
                try {
                    dateFormat.parse(inDate.trim());
                } catch (ParseException pe) {
                    validDate = false;
                    logger.error(ExceptionUtil.getExceptionAsString(pe));
                }
            }
        }
        return validDate;
    }

    public static boolean validateAmount(String amount) {
        if (amount.indexOf("-") != -1 || amount.indexOf("+") != -1)
            return false;
        try {
            new Float(Float.valueOf(Float.parseFloat(amount)));
        } catch (Exception e) {
        	logger.error(ExceptionUtil.getExceptionAsString(e));
            return false;
        }
        return true;
    }

    public static boolean availableBalanceCheck(VectorTable chargeAndTaxVector, BigDecimal txnAmount, String accountId,
        String chgFundingAccount) {
    	logger.info("Inside availableBalanceCheck");
        BigDecimal chgAndTaxAmt = CommonConstants.BIGDECIMAL_ZERO;
        boolean sufficentFunds = false;
        Map map = new HashMap();
        for (int i = 0; i < chargeAndTaxVector.size(); i++) {
            map = chargeAndTaxVector.getRowTags(i);
            chgAndTaxAmt = chgAndTaxAmt.add((BigDecimal) map.get("CHARGEAMOUNT_IN_FUND_ACC_CURRENCY"));
            chgAndTaxAmt = chgAndTaxAmt.add((BigDecimal) map.get("TAXAMOUNT_IN_FUND_ACC_CURRENCY"));
        }
        BigDecimal availableBalance = getAvailableBalance(accountId);
        sufficentFunds = (availableBalance.compareTo(txnAmount) >= 0) || (limitExcessAction == true) ? true : false;
        if (sufficentFunds == false)
            return false;
        availableBalance = getAvailableBalance(chgFundingAccount);
        sufficentFunds = (availableBalance.compareTo(chgAndTaxAmt) >= 0) || (limitExcessAction == true) ? true : false;
        return sufficentFunds;

    }

    public static String validateAccount(String accountNumber, String accountType) {
        Map accountDetails = getAccountDetails(accountNumber);
        if ((Boolean) accountDetails.get("Closed"))
            return "40000132";
        else if ((Boolean) accountDetails.get("Stopped"))
            return "40000133";
        return "OK";
    }

    /**
     * @param accountId
     * @param misTransactionCode
     * @return
     */
    public static boolean isAccountDormant(String accountId, String misTransactionCode) {
        Map accDetails = new HashMap();
        Map misMap = new HashMap();
        Map hashmapout = new HashMap();
        boolean dormancyFlag = false;
        misMap.put("TXNCODE", misTransactionCode);
        accDetails = getAccountDetails(accountId);
        dormancyFlag = ((Boolean) accDetails.get("DormantStatus")).booleanValue();
        if (dormancyFlag == true) {
        	logger.info("Calling UB_IBI_EnquireMISTxnCode_SRV - Microflow");
            hashmapout = MFExecuter.executeMF("UB_IBI_EnquireMISTxnCode_SRV", BankFusionThreadLocal.getBankFusionEnvironment(), misMap);
            if (((String) hashmapout.get("DORMANCYPOSTINGACTION")).equals("1")) {
                return true;
            } else {
                return false;
            }

        }
        return false;
    }

    public static void posting(String debitTxnCode, String exgRateType, String creditAccNum, BigDecimal debitTxnAmt, String creditTxnCode,
        String debitAccount, String debitTxnNarrative, String creditTxnNarrative, String debitTxnCurrencyCode,
        String creditTxnCurrencyCode, BigDecimal creditTxnAmount, String txnRef, String transactionID) {
        Map inputMap = new HashMap();

        inputMap.put(IfmConstants.DEBITTRANSACTIONCODE, debitTxnCode);
        inputMap.put(IfmConstants.EXCHANGERATETYPE, exgRateType);
        inputMap.put(IfmConstants.SETTLEMENTACCOUNTID, creditAccNum);
        inputMap.put(IfmConstants.DEBITTRANSACTIONAMOUNT, debitTxnAmt);
        inputMap.put(IfmConstants.CREDITPOSTINGACTION, IfmConstants.CREDIT);
        inputMap.put(IfmConstants.DEBITPOSTINGACTION, IfmConstants.DEBIT);
        inputMap.put(IfmConstants.CREDITTRANSACTIONCODE, creditTxnCode);
        inputMap.put(IfmConstants.MAINACCOUNTID, debitAccount);
        inputMap.put(IfmConstants.DEBITTRANSACTIONNARRATIVE, debitTxnNarrative);
        inputMap.put(IfmConstants.CREDITTRANSACTIONNARRATIVE, creditTxnNarrative);
        inputMap.put(IfmConstants.DEBITTXNCURRENCYCODE, debitTxnCurrencyCode);
        inputMap.put(IfmConstants.CREDITTXNCURRENCYCODE, creditTxnCurrencyCode);
        inputMap.put(IfmConstants.CREDITTRANSACTIONAMOUNT, creditTxnAmount);
        inputMap.put(IfmConstants.CHANNELID, IfmConstants.IFMCHANNEL);
        inputMap.put(IfmConstants.TRANSACTION_REFERENCE, txnRef);
        inputMap.put(IfmConstants.TRANSACTION_ID, transactionID);

        if (debitTxnAmt.compareTo(CommonConstants.BIGDECIMAL_ZERO) > 0) {
        	logger.info("Calling UB_CMN_FinancialPosting_SRV - Microflow");
            MFExecuter.executeMF("UB_CMN_FinancialPosting_SRV", BankFusionThreadLocal.getBankFusionEnvironment(), inputMap);
        }
    }

    public static boolean isAccountPasswordProtected(String accountId, String accountType) {
        int accRightIndicator = 0;
        Map<String, String> map = new HashMap<String, String>();
        Map outPutMap = new HashMap();
        map.put("AccountID", accountId);
        outPutMap = getAccountDetails(accountId);
        // outPutMap = MFExecuter.executeMF("UB_CNF_GetAccountDetails_SRV",
        // BankFusionThreadLocal.getBankFusionEnvironment(), map);
        if (accountType.equalsIgnoreCase(IfmConstants.DR)) {
            if (outPutMap.get("ACCRIGHTSINDICATOR") != null) {
                accRightIndicator = (Integer) outPutMap.get("ACCRIGHTSINDICATOR");
                if (accRightIndicator == 1 || accRightIndicator == -1 || accRightIndicator == 5 || accRightIndicator == 9) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        } else {

            if (outPutMap.get("ACCRIGHTSINDICATOR") != null) {
                accRightIndicator = (Integer) outPutMap.get("ACCRIGHTSINDICATOR");
                if (accRightIndicator == 1 || accRightIndicator == -1 || accRightIndicator == 7 || accRightIndicator == 9) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }

        }

    }

    /**
     * @param context
     * @param psuedoName
     * @param currency
     * @return
     */
    public static String getAccountFromPuedoNymes(String psuedoName, String currency, String context, String contextValue) {
        IBOAttributeCollectionFeature accDetails = null;
        
        logger.info(" Inside getAccountFromPuedoNymes method ");

        try {
            if (context.equalsIgnoreCase(IfmConstants.PAYMENT_IDENTIFIER_CONTEXT)) {

                accDetails =
                    FinderMethods.findAccountByPseudonameAndContextValue("%" + context + "%" + contextValue + "%" + psuedoName.trim(),
                        currency, Boolean.TRUE, BankFusionThreadLocal.getBankFusionEnvironment(), null);
            } else if (context.equalsIgnoreCase(IfmConstants.CURRENCY_CONTEXT)) {

                accDetails =
                    FinderMethods.findAccountByPseudonameAndContextValue("%" + context + "%" + currency + "%" + psuedoName.trim(),
                        currency, Boolean.TRUE, BankFusionThreadLocal.getBankFusionEnvironment(), null);

            } else {
                accDetails =
                    FinderMethods.findAccountByPseudonameAndContextValue("%" + context + "%" + contextValue + "%" + psuedoName.trim(),
                        currency, Boolean.TRUE, BankFusionThreadLocal.getBankFusionEnvironment(), null);

            }

            return accDetails.getBoID();
        } catch (BankFusionException exp) {
        	logger.error("Exception occured during getAccountFromPuedoNymes operation ", exp);
            return null;
        }

    }

    /**
     * @param accountId
     * @return
     */
    public static Map getAccountDetails(String accountId) {
        Map<String, String> map = new HashMap<String, String>();
        Map outPutMap = new HashMap();
        map.put("AccountID", accountId);
        logger.info(" Calling UB_CNF_GetAccountDetails_SRV - Microflow ");
        outPutMap = MFExecuter.executeMF("UB_CNF_GetAccountDetails_SRV", BankFusionThreadLocal.getBankFusionEnvironment(), map);
        return outPutMap;
    }

    /**
     * @param fromCurrency
     * @param toCurrency
     * @param amount
     * @param exchangeRateType
     * @param roundFlag
     * @return
     */
    public static BigDecimal currencyConversion(String fromCurrency, String toCurrency, BigDecimal amount, String exchangeRateType,
        boolean roundFlag) {
        BFCurrencyValue currValue = null;
        BigDecimal newAmount = new BigDecimal(amount.toString());
        currValue = new BFCurrencyValue(fromCurrency, newAmount, BankFusionThreadLocal.getBankFusionEnvironment().getUserID());
        if (!(exchangeRateType == null || exchangeRateType.equalsIgnoreCase(CommonConstants.EMPTY_STRING))) {
            currValue.setDefaultExchangeRateType(exchangeRateType);
        }
        if (roundFlag) {
            newAmount = currValue.getRoundedAmount(toCurrency);
        } else
            newAmount = currValue.getAmount(toCurrency);
        return newAmount;

    }

    /**
     * @param exchangeType
     * @param buyCurrency
     * @param sellCurrency
     * @param amount
     * @return
     */
    public static BigDecimal getExchangeRate(String exchangeType, String buyCurrency, String sellCurrency, BigDecimal amount) {

        ExchangeRateFatom exchangeRateFatom = new ExchangeRateFatom(BankFusionThreadLocal.getBankFusionEnvironment());
        BigDecimal chargeExchangeRate = null;
        if (exchangeType == null || exchangeType.equals(CommonConstants.EMPTY_STRING)) {
            exchangeType = "SPOT";
        }
        exchangeRateFatom.setF_IN_EXCHRATETYPE(exchangeType);
        exchangeRateFatom.setF_IN_BUYCURRENCYCODE(buyCurrency);
        exchangeRateFatom.setF_IN_BUYAMOUNT(amount);
        exchangeRateFatom.setF_IN_SELLCURRENCYCODE(sellCurrency);
        try {
            exchangeRateFatom.process(BankFusionThreadLocal.getBankFusionEnvironment());
            chargeExchangeRate = exchangeRateFatom.getF_OUT_EXCHANGERATE();
        } catch (Exception e) {
        	logger.error("exchange rate not found", e);
        }
        return chargeExchangeRate;
    }

    /**
     * @param currency
     * @param branch
     * @param positionContext
     * @return
     */

    public static boolean isPositionAccountAvailable(String currency, String branch, String positionContext, String spotPseudonym) {
        Map<String, String> contextValMap = new HashMap<String, String>();
        Iterator<String> iterator = null;
        String contextKey = CommonConstants.EMPTY_STRING;
        IBOAttributeCollectionFeature positionAccountItem = null;
        contextValMap.put(IfmConstants.CURRENCY_CONTEXT, currency);
        contextValMap.put(IfmConstants.BRANCH_CONTEXT, branch);
        iterator = contextValMap.keySet().iterator();

        while (iterator.hasNext()) {

            contextKey = (String) iterator.next();
            // if(positionBothContext || contextKey.equals(ChargeConstants.CHARGE_BRANCH_CONTEXT) ){
            if (contextKey.equals(positionContext)) {
                positionAccountItem =
                    FinderMethods.findAccountByPseudonameAndContextValue("%" + contextKey + "%" + contextValMap.get(contextKey) + "%"
                        + spotPseudonym, currency, Boolean.TRUE, BankFusionThreadLocal.getBankFusionEnvironment(), null);
                break;
            }

        }
        if (positionAccountItem == null) {
            return false;
        }

        return true;
    }

    /**
     * @param debitCurrency
     * @param creditCurrency
     * @param spotPseudonym
     * @param positionAccountContext
     * @param debitAccBranchSortCode
     * @param creditAccBranchSortCode
     * @return
     */
    public static boolean arePositionAccountsAvailable(String debitCurrency, String creditCurrency, String spotPseudonym,
        String positionAccountContext, String debitAccBranchSortCode, String creditAccBranchSortCode) {
        if (spotPseudonym.equals(CommonConstants.EMPTY_STRING) || positionAccountContext.equals(CommonConstants.EMPTY_STRING)) {

            return false;
        }
        if (!isPositionAccountAvailable(debitCurrency, debitAccBranchSortCode, positionAccountContext, spotPseudonym)) {

            return false;
        }

        if (!isPositionAccountAvailable(creditCurrency, creditAccBranchSortCode, positionAccountContext, spotPseudonym)) {

            return false;
        }

        return true;

    }

    @SuppressWarnings("unchecked")
    public static HashMap fetchOnlinecharges(String transferCurrencyISOCode, String fromAccount, String chgFundingAccount,
        BigDecimal txnAmount, String txnType, String debitTxnCode, String creditTxnCode, String contraAccount, boolean isDebit) {
    	logger.info("Inside fetchOnlinecharges");
        String transactionCode = getTransactionCode(txnType, debitTxnCode, creditTxnCode, isDebit);
        HashMap inputParams = new HashMap();
        Map accDetails = new HashMap();
        accDetails = getAccountDetails(fromAccount);
        String acctCurrency = (String) accDetails.get("ISOCURRENCYCODE");
        BigDecimal exchangeAmountValue = getExchangeAmount(transferCurrencyISOCode, acctCurrency, txnAmount);
        if (null != chgFundingAccount && !chgFundingAccount.isEmpty())
            inputParams.put("FUNDINGACCOUNT", chgFundingAccount);
        inputParams.put("1_postingMessageAccountId", fromAccount);
        inputParams.put("1_postingMessageISOCurrencyCode", acctCurrency);
        inputParams.put("1_postingMessageTransactionAmount", exchangeAmountValue);
        inputParams.put("TxnCurrency", transferCurrencyISOCode);
        inputParams.put("1_postingMessageTransactionCode", transactionCode);
        inputParams.put("1_contraAccountNumber", contraAccount);
        logger.info("Calling UB_CHG_CalculateOnlineCharges_SRV - Microflow");
        HashMap outputParams =
            MFExecuter.executeMF("UB_CHG_CalculateOnlineCharges_SRV", BankFusionThreadLocal.getBankFusionEnvironment(), inputParams);
        return outputParams;
    }

    @SuppressWarnings("unchecked")
    public static HashMap fetchOnlinecharges(TransferForecastOrCreateRequest transferForecastOrCreateRequest) {
        TransferForecastInpDetails txnInput = transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransferForecastInp();
        String chgFundingAccount =
            transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransferForecastInp().getChargeFundingAccount();
        String fromAccount = txnInput.getFromMyAccount();

        String transactionType = transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalType();
        String debitTxnCode = transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransferForecastInp().getDebitTxnCode();
        String creditTxnCode =
            transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransferForecastInp().getCreditTxnCode();
        boolean isDebit = true;

        return fetchOnlinecharges(txnInput.getTransferCurrency().getIsoCurrencyCode(), fromAccount, chgFundingAccount, txnInput.getAmount()
            .getAmount(), transactionType, debitTxnCode, creditTxnCode,"", isDebit);
    }

    public static String getTransactionCode(String txnType, String debitTxnCode, String creditTxnCode, boolean isDebit) {
        String transactionCode = "";
        if (isDebit) {
            if (debitTxnCode != null && debitTxnCode != CommonConstants.EMPTY_STRING) {
                transactionCode = debitTxnCode;
            } else {
                if (txnType.equalsIgnoreCase("INTNAT")) {
                    transactionCode = getModuleConfigValue(UB_IND_PaymentPostingFatom.FOREIGNPYMT, UB_IND_PaymentPostingFatom.MODULEID);
                } else if (txnType.equalsIgnoreCase("INTRAPYMT")) {
                    transactionCode = getModuleConfigValue(UB_IND_PaymentPostingFatom.INTRAPYMT, UB_IND_PaymentPostingFatom.MODULEID);
                } else {
                    transactionCode = getModuleConfigValue(UB_IND_PaymentPostingFatom.INTERNALPYMT, UB_IND_PaymentPostingFatom.MODULEID);
                }
            }
        } else {
            if (creditTxnCode != null && creditTxnCode != CommonConstants.EMPTY_STRING) {
                transactionCode = creditTxnCode;
            } else {
                if (txnType.equalsIgnoreCase("INTNAT")) {
                    transactionCode = getModuleConfigValue(UB_IND_PaymentPostingFatom.FOREIGNPYMTCR, UB_IND_PaymentPostingFatom.MODULEID);
                } else if (txnType.equalsIgnoreCase("INTRAPYMT")) {
                    transactionCode = getModuleConfigValue(UB_IND_PaymentPostingFatom.INTRAPYMTCR, UB_IND_PaymentPostingFatom.MODULEID);
                } else {
                    transactionCode = getModuleConfigValue(UB_IND_PaymentPostingFatom.INTERNALPYMTCR, UB_IND_PaymentPostingFatom.MODULEID);
                }
            }
        }
        return transactionCode;
    }

    public static String getTransactionCode(TransferForecastOrCreateRequest transferForecastOrCreateRequest, String type) {
        String transactionType = transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransactionalType();
        String debitTxnCode = transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransferForecastInp().getDebitTxnCode();
        String creditTxnCode =
            transferForecastOrCreateRequest.getTransferForecastOrCreateInput().getTransferForecastInp().getCreditTxnCode();
        boolean isDebit = type.equalsIgnoreCase("debit") ? true : false;
        return getTransactionCode(transactionType, debitTxnCode, creditTxnCode, isDebit);
    }

    @SuppressWarnings("unchecked")
    public static BigDecimal getExchangeAmount(String fromCurr, String toCurr, BigDecimal txnAmt) {
        BigDecimal exchangeAmount = BigDecimal.ZERO;
        HashMap inputParams = new HashMap();
        String DEFAULT_EXCHANGE_RATE_TYPE = "DefaultExRateType";
        String MODULE_NAME_CBS = "CBS";
        String chargeExchangeRateTypeVal =
            ModuleConfiguration.getInstance().getModuleConfigurationValue(MODULE_NAME_CBS, DEFAULT_EXCHANGE_RATE_TYPE).toString();
        CalcExchangeRateRq rq = new CalcExchangeRateRq();
        CalcExchRateDetails calcExchRateDetails = new CalcExchRateDetails();
        calcExchRateDetails.setBuyAmount(txnAmt);
        calcExchRateDetails.setBuyCurrency(fromCurr);
        calcExchRateDetails.setSellCurrency(toCurr);
        ExchangeRateDetails exchangeRateDetails = new ExchangeRateDetails();
        exchangeRateDetails.setExchangeRateType(chargeExchangeRateTypeVal);
        calcExchRateDetails.setExchangeRateDetails(exchangeRateDetails);
        rq.setCalcExchRateDetails(calcExchRateDetails);
        inputParams.put("CalcExchangeRateRq", rq);
        logger.info("Calling CB_FEX_CalculateExchangeRateAmount_SRV - Microflow");
        HashMap outputParams =
            MFExecuter.executeMF("CB_FEX_CalculateExchangeRateAmount_SRV", BankFusionThreadLocal.getBankFusionEnvironment(), inputParams);
        CalcExchangeRateRs response = (CalcExchangeRateRs) outputParams.get("CalcExchangeRateRs");
        exchangeAmount = response.getCalcExchRateResults().getSellAmountDetails().getAmount();
        return exchangeAmount;
    }

    @SuppressWarnings("unchecked")
    public static String getModuleConfigValue(String Value, String MODULEID) {
    	logger.info(" Inside getModuleConfigValue ");
        String value = "";
        HashMap<String, ReadModuleConfigurationRq> moduleParams = new HashMap<String, ReadModuleConfigurationRq>();
        ModuleKeyRq module = new ModuleKeyRq();
        ReadModuleConfigurationRq read = new ReadModuleConfigurationRq();
        module.setModuleId(MODULEID);
        module.setKey(Value);
        read.setModuleKeyRq(module);
        moduleParams.put("ReadModuleConfigurationRq", read);
        logger.info(" Calling CB_CMN_ReadModuleConfiguration_SRV - Microflow ");
        HashMap valueFromModuleConfiguration =
            MFExecuter.executeMF("CB_CMN_ReadModuleConfiguration_SRV", BankFusionThreadLocal.getBankFusionEnvironment(), moduleParams);
        if (valueFromModuleConfiguration != null) {
            ReadModuleConfigurationRs rs = (ReadModuleConfigurationRs) valueFromModuleConfiguration.get("ReadModuleConfigurationRs");
            value = rs.getModuleConfigDetails().getValue().toString();
        }
        return value;
    }

    public static String generateSwiftMessage(JsonObject txnInput, HashMap onlineCharges, String customerId, String settlDtlId,
        BigDecimal debitAmount, String txnReference, String paymentReference, String transactionID, String creditAccountNumber) {

        IPersistenceObjectsFactory factory = new UB_IBI_PaymentsHelper().factory;

        HashMap inputParams = new HashMap();
        String IbanOrAcc = txnInput.getAsJsonObject("transferrecipientDtls").get("otherAccount").getAsString();

        if (txnInput.getAsJsonObject("transferrecipientDtls").get("iBANAccount") != null
            && !"".equalsIgnoreCase(txnInput.getAsJsonObject("transferrecipientDtls").get("iBANAccount").getAsString())) {
            IbanOrAcc = txnInput.getAsJsonObject("transferrecipientDtls").get("iBANAccount").getAsString();
        }
        StringBuilder iBanOrOtherAcc = new StringBuilder();
        iBanOrOtherAcc.append("/").append(IbanOrAcc);
        BigDecimal totalCharge = (BigDecimal) onlineCharges.get("CONSOLIDATEDCHARGEAMT");
        BigDecimal totalTax = (BigDecimal) onlineCharges.get("CONSOLIDATEDTAXAMT");
        String chargeCurrencyCode = (String) onlineCharges.get("FUNDINGACCCURRENCY");
        BigDecimal TotalCharge = CommonConstants.BIGDECIMAL_ZERO;
        TotalCharge = totalCharge.add(totalTax);
        SimplePersistentObject custDtls = factory.findByPrimaryKey(IBOCustomer.BONAME, customerId, false);
        SimplePersistentObject swtCustDtls = null;
        if(StringUtils.isNotEmpty(custDtls.getDataMap().get("f_BRANCHSORTCODE").toString())) {
            swtCustDtls = BranchUtil.getBranchDetailsInCurrentZone(custDtls.getDataMap().get("f_BRANCHSORTCODE").toString());
        }
        String custBICCode = CommonConstants.EMPTY_STRING;
        if (swtCustDtls!=null && swtCustDtls.getDataMap().size() > 0) {
            custBICCode = swtCustDtls.getDataMap().get("f_BICCODE").toString();
        }
        String tablePayRef = UB_IBI_PaymentsHelper.actionSettlemInsDtlPayDtls(settlDtlId, paymentReference);
        IBOSWTSettlementInstructionDetail settlDtl =
            (IBOSWTSettlementInstructionDetail) BankFusionThreadLocal.getPersistanceFactory().findByPrimaryKey(
                IBOSWTSettlementInstructionDetail.BONAME, settlDtlId, true);
        int messageNumber = settlDtl.getF_MESSAGE_NUMBER();
        String transactionCode =
            getTransactionCode("INTNAT", txnInput.get("debitTxnCode").getAsString(), txnInput.get("creditTxnCode").getAsString(), true);
        String exchangeRateType = getExchangeRateType(transactionCode);
        Map accDetails = new HashMap();
        accDetails = UB_IBI_PaymentsHelper.getAccountDetails(txnInput.get("fromMyAccount").getAsString());
        String debitCurrency = (String) accDetails.get("ISOCURRENCYCODE");
        BigDecimal exchangeRate =
            UB_IBI_PaymentsHelper.getExchangeRate(exchangeRateType, debitCurrency,
                txnInput.getAsJsonObject("transferCurrency").get("isoCurrencyCode").getAsString(),
                txnInput.getAsJsonObject("amount").get("amount").getAsBigDecimal());

        inputParams.put("BeneficairyCustomerPartyIdentifier", iBanOrOtherAcc.toString());
        inputParams.put("BankPostingCurrencyCode", txnInput.getAsJsonObject("transferCurrency").get("isoCurrencyCode").getAsString());
        inputParams.put("Deal_Number", txnReference);
        inputParams.put("FXTransaction", 7);
        inputParams.put("TransactionID", transactionID);
        inputParams.put("Generate103Plus", "N");
        String generate900910 = "9";
        SimplePersistentObject generate900Or910 = factory.findByPrimaryKey(IBOMisTransactionCodes.BONAME, transactionCode, false);
        if (generate900Or910.getDataMap().get("f_SWTDRCRCONFIRMATION") != null
            && !generate900Or910.getDataMap().get("f_SWTDRCRCONFIRMATION").equals(CommonConstants.EMPTY_STRING)) {
            generate900910 = generate900Or910.getDataMap().get("f_SWTDRCRCONFIRMATION").toString();
        }
        inputParams.put("DRCRConfirmFlag", generate900910);
        inputParams.put("OrderingCustomerIdentifierCode", custBICCode);
        inputParams.put("Settl_Instruction_Number", messageNumber);

        if (txnInput.getAsJsonObject("beneficiaryBank").get("bankSWIFTorBIC") != null) {
            inputParams.put("BeneficairyInstituteIdentifierCode", txnInput.getAsJsonObject("beneficiaryBank").get("bankSWIFTorBIC")
                .getAsString());
        }

        inputParams.put("BeneficiaryCustomerText1", txnInput.getAsJsonObject("transferrecipientDtls").get("beneficiaryName").getAsString());
        inputParams.put("BeneficiaryCustomerText2", txnInput.getAsJsonObject("transferrecipientDtls").get("beneficiaryAddress")
            .getAsString());
        inputParams.put("BeneficiaryCustomerText3", txnInput.getAsJsonObject("transferrecipientDtls").get("beneficiaryCountry")
            .getAsString());
        if (txnInput.getAsJsonObject("beneficiaryBank").get("bankNameAndAddress") != null) {
            inputParams.put("BeneficiaryInstituteText1", txnInput.getAsJsonObject("beneficiaryBank").getAsJsonObject("bankNameAndAddress")
                .get("bankName").getAsString());
            inputParams.put("BeneficiaryInstituteText2", txnInput.getAsJsonObject("beneficiaryBank").getAsJsonObject("bankNameAndAddress")
                .get("city").getAsString());
            inputParams.put("BeneficiaryInstituteText3", txnInput.getAsJsonObject("beneficiaryBank").getAsJsonObject("bankNameAndAddress")
                .get("country").getAsString());
        }

        BigDecimal TotalChargeInMainAccCurr = CommonConstants.BIGDECIMAL_ZERO;
        VectorTable nvector = (VectorTable) onlineCharges.get("RESULT");
        String chgAmtInFundingAccountCurrency = null, chgAmtInMainAccountCurrency = null, chargeExchangeRate = null;
        HashMap nmap = new HashMap();
        for (int i = 0; i < nvector.size(); i++) {
            nmap = nvector.getRowTags(i);
            chgAmtInFundingAccountCurrency = (String) nmap.get("CHARGEAMOUNT_IN_FUND_ACC_CURRENCY_CurrCode");
            chgAmtInMainAccountCurrency = (String) nmap.get("CHARGEAMOUNT_IN_ACC_CURRENCY_CurrCode");
            chargeExchangeRate = (String) nmap.get("CHARGEEXCHANGERATETYPE");
        }
        TotalChargeInMainAccCurr =
            UB_IBI_PaymentsHelper.currencyConversion(chgAmtInFundingAccountCurrency, chgAmtInMainAccountCurrency, TotalCharge,
                chargeExchangeRate, true);

        inputParams.put("ChargeAmount", TotalChargeInMainAccCurr);
        inputParams.put("ChargeCode", txnInput.get("charges").getAsString());
        inputParams.put("ChargeCurrency", chargeCurrencyCode);
        inputParams.put("ContraAmount", txnInput.getAsJsonObject("amount").get("amount").getAsBigDecimal());
        inputParams.put("Contra_Account", creditAccountNumber);
        inputParams.put("Customer_Number", customerId);
        inputParams.put("ExchangeRate", exchangeRate);
        inputParams.put("FundingAmount", debitAmount);
        inputParams.put("ReceiverChargeAmount", CommonConstants.BIGDECIMAL_ZERO);
        inputParams.put("Main_account", txnInput.get("fromMyAccount").getAsString());
        inputParams.put("MessageType", "103");
        inputParams.put("Post_Date", getDate(txnInput.get("transferDate").getAsString()));
        inputParams.put("Value_Date", getDate(txnInput.get("transferDate").getAsString()));

        inputParams.put("Transaction_Amount", txnInput.getAsJsonObject("amount").get("amount").getAsBigDecimal());
        inputParams.put("WalkInCustomer", false);
        inputParams.put("code_word", "NEW");
        logger.info("Calling UB_SWT_MessageValidator_SRV - Microflow");
        HashMap outputParams =
            MFExecuter.executeMF("UB_SWT_MessageValidator_SRV", BankFusionThreadLocal.getBankFusionEnvironment(), inputParams);
        // TODO
        // insertSwiftMessageDtls(custBICCode);
        tablePayRef = UB_IBI_PaymentsHelper.actionSettlemInsDtlPayDtls(settlDtlId, tablePayRef);
        logger.info("====Remittance information sent is " + tablePayRef + " =====");
        return outputParams.get("Status_Flag").toString();
    }

    public static String actionSettlemInsDtlPayDtls(String detailId, String paymentRefernce) {
        String prevPaymentRef = "";

        IBOSWTSettlementInstructionDetail settlementInstructionsDetail =
            (IBOSWTSettlementInstructionDetail) BankFusionThreadLocal.getPersistanceFactory().findByPrimaryKey(
                IBOSWTSettlementInstructionDetail.BONAME, detailId, true);
        prevPaymentRef = UB_IBI_PaymentsHelper.getPrevPaymentRef(settlementInstructionsDetail);

        String[] payDetailLines = UB_IBI_PaymentsHelper.formatPaymentReference(paymentRefernce);
        settlementInstructionsDetail.setF_PAY_DETAILS1(payDetailLines[0]);
        settlementInstructionsDetail.setF_PAY_DETAILS2(payDetailLines[1]);
        settlementInstructionsDetail.setF_PAY_DETAILS3(payDetailLines[2]);
        settlementInstructionsDetail.setF_PAY_DETAILS4(payDetailLines[3]);

        return prevPaymentRef;
    }

    private static String getPrevPaymentRef(IBOSWTSettlementInstructionDetail settlementInstructionsDetail) {
        String prevPaymentRef = "";

        prevPaymentRef += settlementInstructionsDetail.getF_PAY_DETAILS1();
        prevPaymentRef += settlementInstructionsDetail.getF_PAY_DETAILS2();
        prevPaymentRef += settlementInstructionsDetail.getF_PAY_DETAILS3();
        prevPaymentRef += settlementInstructionsDetail.getF_PAY_DETAILS4();

        return prevPaymentRef;
    }

    private static String[] formatPaymentReference(String paymentReference) {
        String line1 = "";
        String line2 = "";
        String line3 = "";
        String line4 = "";

        int payRefStrLength = paymentReference.length();

        if (payRefStrLength <= 35) {
            line1 = paymentReference;
        } else if (payRefStrLength <= 70) {
            line1 = paymentReference.substring(0, 35);
            line2 = paymentReference.substring(35, payRefStrLength);
        } else if (payRefStrLength <= 105) {
            line1 = paymentReference.substring(0, 35);
            line2 = paymentReference.substring(35, 70);
            line3 = paymentReference.substring(70, payRefStrLength);
        } else if (payRefStrLength > 105) {
            line1 = paymentReference.substring(0, 35);
            line2 = paymentReference.substring(35, 70);
            line3 = paymentReference.substring(70, 105);
            if (payRefStrLength <= 140) {
                line4 = paymentReference.substring(70, payRefStrLength);
            } else {
                line4 = paymentReference.substring(70, 140);
            }
        }

        String[] payDetailLines = { line1, line2, line3, line4 };
        return payDetailLines;
    }

    public static java.sql.Date getDate(String dateAsString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date parsedDate = null;
        try {
            parsedDate = format.parse(dateAsString);
        } catch (ParseException e) {
        	logger.error(" Error while converting to date -> " + dateAsString);
        	logger.error(ExceptionUtil.getExceptionAsString(e));
        }
        if (parsedDate != null) {
            java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());
            return sqlDate;
        } else {
            return null;
        }
    }

    public static String getExchangeRateType(String transactionCode) {

        HashMap inputParams = new HashMap();
        inputParams.put("miscode", transactionCode);
        logger.info("Calling 100_CheckMISTransCode - Microflow");
        HashMap outputParams = MFExecuter.executeMF("100_CheckMISTransCode", BankFusionThreadLocal.getBankFusionEnvironment(), inputParams);
        return outputParams.get("ExchangeRateType").toString();
    }

    public static NameAndAddress formatBenBankDtls(String benBankName, String benBankCity, String benBankCountry) {
        NameAndAddress benBankDetails = new NameAndAddress();
        int nameLen = 0, cityLen = 0, countryLen = 0;

        if (benBankName != null)
            nameLen = benBankName.length();
        if (benBankCity != null)
            cityLen = benBankCity.length();
        if (benBankCountry != null)
            countryLen = benBankCountry.length();

        if (nameLen <= 35 && cityLen <= 35 && countryLen <= 35) {
            benBankDetails.setName(benBankName);
            benBankDetails.setTextLine1(benBankCity);
            benBankDetails.setTextLine2(benBankCountry);
        } else {
            String benBankDtlStr = "";
            if (nameLen != 0)
                benBankDtlStr += benBankName;
            if (cityLen != 0)
                benBankDtlStr = benBankDtlStr + " " + benBankCity;
            if (countryLen != 0)
                benBankDtlStr = benBankDtlStr + " " + benBankCountry;
            benBankDetails = UB_IBI_PaymentsHelper.formatBenBankDtls(benBankDtlStr);
        }

        return benBankDetails;
    }

    private static NameAndAddress formatBenBankDtls(String benBankDtlStr) {
        NameAndAddress benBankDetails = new NameAndAddress();
        int benBankDtlStrLen = benBankDtlStr.length();

        if (benBankDtlStrLen <= 70) {
            benBankDetails.setName(benBankDtlStr.substring(0, 35));
            benBankDetails.setTextLine1(benBankDtlStr.substring(35, benBankDtlStrLen));
            benBankDetails.setTextLine2("");
        } else if (benBankDtlStrLen <= 105) {
            benBankDetails.setName(benBankDtlStr.substring(0, 35));
            benBankDetails.setTextLine1(benBankDtlStr.substring(35, 70));
            benBankDetails.setTextLine2(benBankDtlStr.substring(70, benBankDtlStrLen));
        } else if (benBankDtlStrLen > 105) {
            benBankDetails.setName(benBankDtlStr.substring(0, 35));
            benBankDetails.setTextLine1(benBankDtlStr.substring(35, 70));
            benBankDetails.setTextLine2(benBankDtlStr.substring(70, 105));
        }

        return benBankDetails;
    }

    public static boolean isLoanAccount(String accountId) {
        Map accountMap = new HashMap();
        accountMap = getAccountDetails(accountId);
        String prodId = accountMap.get("PRODUCTCONTEXTCODE").toString();
        if(StringUtils.isBlank(accountId) || StringUtils.isBlank(prodId))
        {
        logger.error("===== In isLoanAccount method  ======"+ "AccountId is:" + accountId+ "\t" +"productId is:" +  prodId  +"\t" + "Accountmap:"+ accountMap);	
        }
        SimplePersistentObject prodDtls =
            BankFusionThreadLocal.getPersistanceFactory().findByPrimaryKey(IBOProductInheritance.BONAME, prodId, false);
        if (Boolean.parseBoolean(prodDtls.getDataMap().get(IBOProductInheritance.HAS_LOAN).toString()))
            return true;
        else
            return false;
    }

    public static PostingLeg getBackOfficePostingLeg(String accountId, String txnCurrency, BigDecimal txnAmount, String crdtDbtIndicator,
        String txnCode, String narrative, String exchangeRateType, BigDecimal baseEquivalentAmount) {
        PostingLeg postingLeg = new PostingLeg();
        AccountPseudonym accPseduonym = new AccountPseudonym();

        FxInfo amountToBaseEquivalentFxDetail = new FxInfo();
        amountToBaseEquivalentFxDetail.setExchangeRateType(exchangeRateType);
        amountToBaseEquivalentFxDetail.setMultiplyDivide(CommonConstants.EMPTY_STRING);

        postingLeg.setAccountId(accountId);
        postingLeg.setAccountPseudonym(accPseduonym);
        postingLeg.setTransactionCurrency(txnCurrency);
        postingLeg.setAmount(txnAmount);
        postingLeg.setCreditDebitIndicator(crdtDbtIndicator);
        postingLeg.setTransactionCode(txnCode);
        postingLeg.setNarrative(narrative);
        postingLeg.setAmountToBaseEquivalentFxDetail(amountToBaseEquivalentFxDetail);
        postingLeg.setBaseEquivalentAmount(baseEquivalentAmount);

        return postingLeg;
    }

    public static TxnDetails getTxnDetails(String transactionId, String transactionReference, Timestamp valueDate, String branchSortCode,
        String channelId) {
        TxnDetails details = new TxnDetails();
        details.setTransactionId(transactionId);
        details.setTransactionReference(transactionReference);
        details.setValueDate(valueDate);
        details.setBranchSortCode(branchSortCode);
        details.setForcePost(false);
        details.setChannelId(channelId);

        return details;
    }

}