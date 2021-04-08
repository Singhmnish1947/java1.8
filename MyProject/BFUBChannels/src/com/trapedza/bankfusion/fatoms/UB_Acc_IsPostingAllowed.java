package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.HashMap;

import bf.com.misys.cbs.services.CalcExchangeRateRq;
import bf.com.misys.cbs.services.CalcExchangeRateRs;
import bf.com.misys.cbs.services.ReadRetailExRtRq;
import bf.com.misys.cbs.services.ReadRetailExRtRs;
import bf.com.misys.cbs.types.CalcExchRateDetails;
import bf.com.misys.cbs.types.ExchangeRateDetails;
import bf.com.misys.cbs.types.RetailExRtShrtDetails;
import bf.com.misys.cbs.types.header.Orig;
import bf.com.misys.cbs.types.header.RqHeader;

import com.misys.bankfusion.subsystem.microflow.runtime.impl.MFExecuter;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ACC_IsPostingAllowedCheck;

public class UB_Acc_IsPostingAllowed extends
		AbstractUB_ACC_IsPostingAllowedCheck {

	/**
	 * 
	 */
	private static final long serialVersionUID = -477363761002571124L;

	private static final String MFID_TOGET_AVAILABLEBALANCE = "AvailableBalance0";
	private static final String AVAILABLEBALANCE_IN_PARAM = "AccountID";
	private static final String AVAILABLEBALANCE_OUT_PARAM = "AvailableBalance";

	/**
	 * @param env
	 */
	@SuppressWarnings("deprecation")
	public UB_Acc_IsPostingAllowed(BankFusionEnvironment env) {
		super(env);
	}

	@Override
	public void process(BankFusionEnvironment env) {

		String accountCurrency = getF_IN_accountCurrency();
		String amountCurrency = getF_IN_amountCurrency();
		String account = getF_IN_accountId();
		Integer limitExcess = getF_IN_limitExcessAction();
		BigDecimal amountPosted = getF_IN_amountPosted();
		BigDecimal originalAmount = BigDecimal.ZERO;
		Boolean canPost;

		HashMap inputParams = new HashMap();
		inputParams.put(AVAILABLEBALANCE_IN_PARAM, account);
		HashMap outputParams = MFExecuter.executeMF(
				MFID_TOGET_AVAILABLEBALANCE,
				BankFusionThreadLocal.getBankFusionEnvironment(), inputParams);

		BigDecimal availableBal = (BigDecimal) (outputParams
				.get(AVAILABLEBALANCE_OUT_PARAM));

		if (!accountCurrency.equals(amountCurrency)) {
			originalAmount = calculateExchangeRateAmt(amountCurrency,
					amountPosted);
		} else {
			originalAmount = amountPosted;
		}

		if (originalAmount.compareTo(availableBal) == -1
				|| originalAmount.compareTo(availableBal) == 0) {
			canPost = true;

		} else {
			if (limitExcess == 1) {
				canPost = true;
			} else {
				canPost = false;
			}
		}

		setF_OUT_canPost(canPost);
		setF_OUT_availableBalance(availableBal);
		setF_OUT_accountCurrency(accountCurrency);

	}

	private BigDecimal calculateExchangeRateAmt(String buyCurrency,
			BigDecimal buyAmount) {

		String sellCurrency = getF_IN_accountCurrency();

		// For CB_FEX_ReadRetailExchangeRate_SRV.bfg
		ReadRetailExRtRq exRtRq = new ReadRetailExRtRq();
		RetailExRtShrtDetails retailExRtShrtDetails = new RetailExRtShrtDetails();
		RqHeader rqHeader = new RqHeader();
		Orig orig = new Orig();
		orig.setChannelId("MPM");
		rqHeader.setOrig(orig);
		exRtRq.setRqHeader(rqHeader);
		retailExRtShrtDetails.setExchangeRateType("SPOT");
		retailExRtShrtDetails.setExRateCat("SPOT");
		retailExRtShrtDetails.setFromCurrency(buyCurrency);
		retailExRtShrtDetails.setToCurrency(sellCurrency);
		exRtRq.setRetailExRtShrtDetails(retailExRtShrtDetails);
		BankFusionEnvironment env = new BankFusionEnvironment(null);

		HashMap ipMap = new HashMap();
		ipMap.put("ReadRetailExRtRq", exRtRq);
		env.setData(new HashMap());
		HashMap opParams = MFExecuter.executeMF(
				"CB_FEX_ReadRetailExchangeRate_SRV", env, ipMap);

		ReadRetailExRtRs readRetailExRtRs = (ReadRetailExRtRs) opParams
				.get("ReadRetailExRtRs");
		BigDecimal exchRate = readRetailExRtRs.getRetailExRtDetail()
				.getExchangeRate();

		// For CB_FEX_CalculateExchangeRateAmount_SRV
		CalcExchangeRateRq exchRq = new CalcExchangeRateRq();
		CalcExchRateDetails exchangeDtls = new CalcExchRateDetails();
		exchangeDtls.setSellAmount(BigDecimal.ZERO);
		if (buyAmount.signum() < 0) {
			exchangeDtls.setBuyAmount(buyAmount.abs());
		} else {
			exchangeDtls.setBuyAmount(buyAmount);
		}

		exchangeDtls.setBuyCurrency(buyCurrency);
		exchangeDtls.setSellCurrency(sellCurrency);
		exchRq.setCalcExchRateDetails(exchangeDtls);
		ExchangeRateDetails exchangeRateDetails = new ExchangeRateDetails();
		exchangeRateDetails.setExchangeRate(exchRate);
		exchangeRateDetails.setExchangeRateType("SPOT");
		exchangeDtls.setExchangeRateDetails(exchangeRateDetails);
		exchRq.setRqHeader(rqHeader);

		HashMap inputMap = new HashMap();
		inputMap.put("CalcExchangeRateRq", exchRq);
		env.setData(new HashMap());
		HashMap outputParams = MFExecuter.executeMF(
				"CB_FEX_CalculateExchangeRateAmount_SRV", env, inputMap);

		CalcExchangeRateRs calcExchangeRateRs = (CalcExchangeRateRs) outputParams
				.get("CalcExchangeRateRs");
		BigDecimal equivalentAmount = calcExchangeRateRs
				.getCalcExchRateResults().getSellAmountDetails().getAmount();
		if (buyAmount.signum() < 0) {
			equivalentAmount = BigDecimal.ZERO.subtract(equivalentAmount);
		}
		return equivalentAmount;

	}
}
