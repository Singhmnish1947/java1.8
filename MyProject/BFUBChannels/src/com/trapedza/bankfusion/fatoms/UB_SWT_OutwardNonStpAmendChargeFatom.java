package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.payment.swift.DBUtils.SwiftNonStpChargeTable;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_OutwardNonStpAmendChargeFatom;

public class UB_SWT_OutwardNonStpAmendChargeFatom extends AbstractUB_SWT_OutwardNonStpAmendChargeFatom {
	/**
	 * @param args
	 */
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final transient Log LOGGER = LogFactory.getLog(UB_SWT_OutwardNonStpAmendChargeFatom.class.getName());
	private String chgAmount = "CHARGEAMOUNT";
	private String chgCalCode = "CHARGECALCULATIONCODE";
	private String chgCode = "CHARGECODE";
	private String chgCurrency = "CHARGECURRENCY";
	private String chgDescription = "CHARGEDESCRIPTION";
	private String chgNarrative = "CHARGENARRATIVE";
	private String chgRecAcc = "CHARGERECIEVINGACCOUNT";
	private String taxAmt = "TAXAMOUNT";
	private String taxCode = "TAXCODE";
	private String taxCurr = "TAXCURRENCY";
	private String taxNarative = "TAXNARRATIVE";
	private String taxRecAcc = "TAXRECIEVINGACCOUNT";
	private String chgPosTxnCode = "CHARGEPOSTINGTXNCODE";
	private String taxCodeKey = "TAXCODEVALUE";
	private String sel = "SELECT";
	private final String chgAmountIn_Acc_Curr = "CHARGEAMOUNT_IN_ACC_CURRENCY";
	private final String chgAmountIn_Txn_Curr = "CHARGEAMOUNT_IN_TXN_CURRENCY";
	private final String chgAmountIn_Fun_Acc_Curr = "CHARGEAMOUNT_IN_FUND_ACC_CURRENCY";
	private final String taxAmountIn_Acc_Curr = "TAXAMOUNT_IN_ACC_CURRENCY";
	private final String taxAmountIn_Txn_Curr = "TAXAMOUNT_IN_TXN_CURRENCY";
	private final String taxAmountIn_Fun_Acc_Curr = "TAXAMOUNT_IN_FUND_ACC_CURRENCY";
	private String funAccID = "FUNDINGACCOUNTID";
	private String funAccCurrency = "FUND_ACC_CURRENCY";
	private String accCurrency = "ACC_CURRENCY";
	private String chgExchangeRateType = "CHARGEEXCHANGERATETYPE";
	private String taxExchangeRateType = "TAXEXCHANGERATETYPE";
	private String chgExchangeRate = "CHARGEEXCHANGERATE";
	private String taxExchangerate = "TAXEXCHANGERATE";
	private String chargeAmendAllowed = "CHARGEAMENDALLOWED";
	private String transactionCurrency = "TXNCURRENCY";
	private String nettingApplicable = "NETTINGAPPLICABLE";
	private String transactionAmountKey = "TRANSACTIONAMOUNT";
	private String chargeAmountWaived = "CHARGEAMOUNTWAIVED";
	private String chargeWaived = "CHARGEWAIVED";
	private String chargeWaiveLevel = "CHARGEWAIVELEVEL";
	private String chargeWaiveReasonKey = "CHARGEWAIVEREASON";
	private String taxDescriptionKey = "TAXDESCRIPTION";
	private String calculatedChargeAmount = "CALCULATEDCHARGEAMOUNT";
	private String accoutIDKey = "ACCOUNTID";
	private String maxChargeAmountKey = "MAXCHARGEAMOUNT";
	private String minChargeAmountKey = "MINCHARGEAMOUNT";
	private final String CHARGEAMOUNT_CurrCode = "CHARGEAMOUNT_CurrCode";
	private final String CHARGEAMOUNT_IN_ACC_CURRENCY_CurrCode = "CHARGEAMOUNT_IN_ACC_CURRENCY_CurrCode";
	private final String CHARGEAMOUNT_IN_TXN_CURRENCY_CurrCode = "CHARGEAMOUNT_IN_TXN_CURRENCY_CurrCode";
	private final String CHARGEAMOUNT_IN_FUND_ACC_CURRENCY_CurrCode = "CHARGEAMOUNT_IN_FUND_ACC_CURRENCY_CurrCode";
	private final String TAXAMOUNT_CurrCode = "TAXAMOUNT_CurrCode";
	private final String TAXAMOUNT_IN_ACC_CURRENCY_CurrCode = "TAXAMOUNT_IN_ACC_CURRENCY_CurrCode";
	private final String TAXAMOUNT_IN_TXN_CURRENCY_CurrCode = "TAXAMOUNT_IN_TXN_CURRENCY_CurrCode";
	private final String TAXAMOUNT_IN_FUND_ACC_CURRENCY_CurrCode = "TAXAMOUNT_IN_FUND_ACC_CURRENCY_CurrCode";
	private final String CHARGEAMOUNTWAIVED_CurrCode = "CHARGEAMOUNTWAIVED_CurrCode";
	private final String CALCULATEDCHARGEAMOUNT_CurrCode = "CALCULATEDCHARGEAMOUNT_CurrCode";
	private static final String ORIGINAL_CHGAMOUNT = "ORIGINAL_CHARGEAMOUNT";
	private static final String ORIGINAL_CHGAMOUNTIN_ACC_CURR = "ORIGINAL_CHARGEAMOUNT_IN_ACC_CURRENCY";
	private static final String ORIGINAL_CHGAMOUNTIN_TXN_CURR = "ORIGINAL_CHARGEAMOUNT_IN_TXN_CURRENCY";
	private static final String ORIGINAL_CHGAMOUNTIN_FUN_ACC_CURR = "ORIGINAL_CHARGEAMOUNT_IN_FUND_ACC_CURRENCY";
	private static final String ORIGINAL_TAXAMT = "ORIGINAL_TAXAMOUNT";
	private static final String ORIGINAL_TAXAMOUNTIN_ACC_CURR = "ORIGINAL_TAXAMOUNT_IN_ACC_CURRENCY";
	private static final String ORIGINAL_TAXAMOUNTIN_TXN_CURR = "ORIGINAL_TAXAMOUNT_IN_TXN_CURRENCY";
	private static final String ORIGINAL_TAXAMOUNTIN_FUN_ACC_CURR = "ORIGINAL_TAXAMOUNT_IN_FUND_ACC_CURRENCY";
	String fundingAccountId = StringUtils.EMPTY;
	String fundingAccountCcy = StringUtils.EMPTY;
	BigDecimal consolidatedChargeAmount = CommonConstants.BIGDECIMAL_ZERO;
	BigDecimal chargeAmountFunAccCurr = CommonConstants.BIGDECIMAL_ZERO;
	BigDecimal chargeAmountInAccCurr = CommonConstants.BIGDECIMAL_ZERO;
	String chargeAmountInAccCurr_CurrencyCode = StringUtils.EMPTY;

	/**
	 * 
	 */
	public UB_SWT_OutwardNonStpAmendChargeFatom() {
		super();
	}

	/**
	 * @param env
	 */
	@SuppressWarnings("deprecation")
	public UB_SWT_OutwardNonStpAmendChargeFatom(BankFusionEnvironment env) {
		super(env);
	}

	/* (non-Javadoc)
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_OutwardRemittanceProcess#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void process(BankFusionEnvironment env) throws BankFusionException {
		String messageId = getF_IN_MESSAGEID();
		VectorTable amendChargeVector = getF_IN_AmendedChargeVector();
		SwiftNonStpChargeTable swiftCharge = new SwiftNonStpChargeTable();
		VectorTable vectorList = swiftCharge.listChargeByMessageId(messageId);
		VectorTable chargeVector = new VectorTable();
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("UB_SWT_OutwardNonStpAmendChargeFatom:::" + messageId);
		}
		if (amendChargeVector.size() == 0) {
			for (int i = 0; i < vectorList.size(); i++) {
				HashMap chargeDetailsMap = new HashMap();
				Map<?, ?> paramValues = vectorList.getRowTags(i);
				chargeDetailsMap.put(sel, Boolean.TRUE);
				chargeDetailsMap.put(chgAmount, (BigDecimal) paramValues.get("UBCHARGEAMOUNT"));
				chargeDetailsMap.put(chgCurrency, (String) paramValues.get("UBCHARGECURRENCYCODE"));
				chargeDetailsMap.put(chgCalCode, (String) paramValues.get("UBCHARGECALCCODE"));
				chargeDetailsMap.put(chgCode, (String) paramValues.get("UBCHARGECODEID"));
				chargeDetailsMap.put(chgDescription, (String) paramValues.get("UBCHARGEDESCRIPTION"));
				chargeDetailsMap.put(chgNarrative, (String) paramValues.get("UBCHARGENARRATIVE"));
				chargeDetailsMap.put(chgRecAcc, (String) paramValues.get("UBCHARGERECEIVINGACCOUNTID"));
				chargeDetailsMap.put(chgPosTxnCode, (String) paramValues.get("UBCHARGEPOSTINGTXNCODE"));
				//charge amount
				chargeDetailsMap.put(chgAmountIn_Acc_Curr, (BigDecimal) paramValues.get("UBCHARGEAMTINFUNDINGACCTCCY"));
				chargeDetailsMap.put(CHARGEAMOUNT_IN_ACC_CURRENCY_CurrCode, (String) paramValues.get("UBCHARGEFUNDINGACCTIDCCYCODE"));
				chargeDetailsMap.put(chgAmountIn_Txn_Curr, (BigDecimal) paramValues.get("UBCHARGEAMTINFUNDINGACCTCCY"));
				chargeDetailsMap.put(CHARGEAMOUNT_IN_TXN_CURRENCY_CurrCode, (String) paramValues.get("UBCHARGEFUNDINGACCTIDCCYCODE"));
				chargeDetailsMap.put(chgAmountIn_Fun_Acc_Curr, (BigDecimal) paramValues.get("UBCHARGEAMTINFUNDINGACCTCCY"));
				chargeDetailsMap.put(CHARGEAMOUNT_IN_FUND_ACC_CURRENCY_CurrCode, (String) paramValues.get("UBCHARGEFUNDINGACCTIDCCYCODE"));
				fundingAccountId = (String) paramValues.get("UBCHARGEFUNDINGACCTID");
				fundingAccountCcy = (String) paramValues.get("UBCHARGEFUNDINGACCTIDCCYCODE");
				chargeAmountFunAccCurr = (BigDecimal) paramValues.get("UBCHARGEAMTINFUNDINGACCTCCY");
				chargeAmountInAccCurr = (BigDecimal) paramValues.get("UBCHARGEAMTINFUNDINGACCTCCY");
				chargeAmountInAccCurr_CurrencyCode = (String) paramValues.get("UBCHARGEFUNDINGACCTIDCCYCODE");
				//debit account id
				chargeDetailsMap.put(accoutIDKey, getF_IN_ACCOUNTID());
				chargeDetailsMap.put(accCurrency, getF_IN_TRANSACTIONCURRENCY());
				chargeDetailsMap.put(taxAmt, (BigDecimal) paramValues.get("UBTAXAMOUNT"));
				chargeDetailsMap.put(taxCode, (String) paramValues.get("UBTAXCODE"));
				chargeDetailsMap.put(taxCurr, (String) paramValues.get("UBTAXCURRENCYCODE"));
				chargeDetailsMap.put(taxCodeKey, (String) paramValues.get("UBTAXCODE"));
				chargeDetailsMap.put(taxNarative, (String) paramValues.get("UBTAXNARRATIVE"));
				chargeDetailsMap.put(taxRecAcc, (String) paramValues.get("UBTAXRECEIVINGACCOUNTID"));
				chargeDetailsMap.put(chgExchangeRateType, (String) paramValues.get("UBCHARGEEXCHANGERATETYPE"));
				chargeDetailsMap.put(chgExchangeRate, (BigDecimal) paramValues.get("UBCHARGEEXCHANGERATE"));
				chargeDetailsMap.put(taxExchangeRateType, (String) paramValues.get("UBTAXEXCHANGERATETYPE"));
				chargeDetailsMap.put(taxExchangerate, (BigDecimal) paramValues.get("UBTAXEXCHANGERATE"));
				chargeDetailsMap.put(taxAmountIn_Acc_Curr, (BigDecimal) paramValues.get("UBTAXAMTINACCTCCY"));
				chargeDetailsMap.put(TAXAMOUNT_IN_ACC_CURRENCY_CurrCode, (String) paramValues.get("UBTAXAMTINACCTCCYCODE"));
				chargeDetailsMap.put(taxAmountIn_Txn_Curr, (BigDecimal) paramValues.get("TAXAMTINFUNDINGACCTCCY"));
				chargeDetailsMap.put(TAXAMOUNT_IN_TXN_CURRENCY_CurrCode, (String) paramValues.get("UBTAXAMTINFUNDINGACCTCCYCODE"));
				chargeDetailsMap.put(taxAmountIn_Fun_Acc_Curr, (BigDecimal) paramValues.get("TAXAMTINFUNDINGACCTCCY"));
				chargeDetailsMap.put(TAXAMOUNT_IN_FUND_ACC_CURRENCY_CurrCode, (String) paramValues.get("UBTAXAMTINFUNDINGACCTCCYCODE"));
				chargeDetailsMap.put(taxDescriptionKey, (String) paramValues.get("UBTAXDESCRIPTION"));
				chargeDetailsMap.put(funAccID, (String) paramValues.get("UBCHARGEFUNDINGACCTID"));
				chargeDetailsMap.put(funAccCurrency, (String) paramValues.get("UBCHARGEFUNDINGACCTIDCCYCODE"));
				chargeDetailsMap.put(transactionCurrency, getF_IN_TRANSACTIONCURRENCY());
				chargeDetailsMap.put(minChargeAmountKey, BigDecimal.ZERO);
				chargeDetailsMap.put(maxChargeAmountKey, BigDecimal.ZERO);
				chargeDetailsMap.put(transactionAmountKey, getF_IN_TRANSACTIONAMOUNT());
				chargeDetailsMap.put(nettingApplicable, Boolean.FALSE);
				chargeDetailsMap.put(chargeAmendAllowed, Boolean.TRUE);
				chargeDetailsMap.put(chargeWaived, Boolean.FALSE);
				chargeDetailsMap.put(chargeWaiveLevel, StringUtils.EMPTY);
				chargeDetailsMap.put(chargeWaiveReasonKey, StringUtils.EMPTY);
				chargeDetailsMap.put(chargeAmountWaived, CommonConstants.BIGDECIMAL_ZERO);
				chargeDetailsMap.put(CHARGEAMOUNT_CurrCode, (String) paramValues.get("UBCHARGECURRENCYCODE"));
				chargeDetailsMap.put(TAXAMOUNT_CurrCode, (String) paramValues.get("UBTAXCURRENCYCODE"));
				chargeDetailsMap.put(CHARGEAMOUNTWAIVED_CurrCode, (String) paramValues.get("UBCHARGECURRENCYCODE"));
				chargeDetailsMap.put(CALCULATEDCHARGEAMOUNT_CurrCode, (String) paramValues.get("UBCHARGECURRENCYCODE"));
				chargeDetailsMap.put(calculatedChargeAmount, (BigDecimal) paramValues.get("UBCHARGEAMOUNT"));
				chargeDetailsMap.put(ORIGINAL_CHGAMOUNT, (BigDecimal) paramValues.get("UBCHARGEAMOUNT"));
				chargeDetailsMap.put(ORIGINAL_CHGAMOUNTIN_ACC_CURR, BigDecimal.ZERO);
				chargeDetailsMap.put(ORIGINAL_CHGAMOUNTIN_FUN_ACC_CURR, BigDecimal.ZERO);
				chargeDetailsMap.put(ORIGINAL_CHGAMOUNTIN_TXN_CURR, BigDecimal.ZERO);
				chargeDetailsMap.put(ORIGINAL_TAXAMT, BigDecimal.ZERO);
				chargeDetailsMap.put(ORIGINAL_TAXAMOUNTIN_ACC_CURR, BigDecimal.ZERO);
				chargeDetailsMap.put(ORIGINAL_TAXAMOUNTIN_FUN_ACC_CURR, BigDecimal.ZERO);
				chargeDetailsMap.put(ORIGINAL_TAXAMOUNTIN_TXN_CURR, BigDecimal.ZERO);
				chargeVector.addAll(new VectorTable(chargeDetailsMap));
			}
		}
		else {
			for (int i = 0; i < amendChargeVector.size(); i++) {
				Map<?, ?> paramValues = amendChargeVector.getRowTags(i);
				fundingAccountId = (String) paramValues.get("FUNDINGACCOUNTID");
				fundingAccountCcy = (String) paramValues.get("FUND_ACC_CURRENCY");
				chargeAmountFunAccCurr = (BigDecimal) paramValues.get("CHARGEAMOUNT_IN_FUND_ACC_CURRENCY");
				chargeAmountInAccCurr = (BigDecimal) paramValues.get("CHARGEAMOUNT_IN_ACC_CURRENCY");
				chargeAmountInAccCurr_CurrencyCode = (String) paramValues.get("CHARGEAMOUNT_IN_ACC_CURRENCY_CurrCode");
			}
			chargeVector.addAll(amendChargeVector);
		}
		consolidatedChargeAmount = consolidatedChargeAmount.add(chargeAmountFunAccCurr);
		setF_OUT_RESULT(chargeVector);
		setF_OUT_CHARGEFUNDINGACCOUNTID(fundingAccountId);
		setF_OUT_FUNDINGACCCURRENCY(fundingAccountCcy);
		setF_OUT_CONSOLIDATEDCHARGEAMOUNT(consolidatedChargeAmount);
		setF_OUT_CONSOLIDATEDACCCURRAMOUNT(chargeAmountInAccCurr);
		//isocurrency code
		setF_OUT_CONSOLIDATEDACCCURRAMOUNT_CurrCode(chargeAmountInAccCurr_CurrencyCode);
		setF_OUT_isWaived(Boolean.FALSE);
	}
}
