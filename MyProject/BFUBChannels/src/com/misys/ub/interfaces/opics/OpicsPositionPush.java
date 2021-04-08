package com.misys.ub.interfaces.opics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.bankfusion.subsystem.microflow.runtime.impl.MFExecuter;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_OPX_PositionPush;

public class OpicsPositionPush extends AbstractUB_OPX_PositionPush {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OpicsPositionPush(BankFusionEnvironment env) {
		super(env);
	}

	BigDecimal exRate;
	String purSell = null;
	String contraCurr;
	String curr;
	BigDecimal contraAmt = BigDecimal.ZERO;
	BigDecimal amt = BigDecimal.ZERO;
	String purSellInd;
	String multiplyDiv;
	String dealno;
	String frontEndDealno;
	Date frontEndDate;
	Date valueDate;
	Date dealDate;
	String currPremDisc = "0";
	String dealText;
	BigDecimal baseeqAmt;

	public void process(BankFusionEnvironment env) {
		BigDecimal buyAmt = getF_IN_BuyAmount();
		BigDecimal sellAmt = getF_IN_SellAmount();
		String buyCurr = getF_IN_BuyCurrencyCode();
		dealno = getF_IN_DealNo();
		frontEndDealno = getF_IN_FrontendDealNo();
		String sellCurr = getF_IN_SellCurrencyCode();
		String trType = getF_IN_UbTransactionType();
		baseeqAmt = getF_IN_BuyBaseEquivalentAmount();
		String baseCurrCode = getF_IN_Currencypremiumanddiscount();
		dealDate = getF_IN_SysDate();
		valueDate = getF_IN_OptionEndOrSellMaturityDate();
		frontEndDate = getF_IN_FrontEndDate();
		dealText = getF_IN_FXDealID();
		if (trType.equalsIgnoreCase("BASESELL")) {
			curr = buyCurr;
			contraCurr = sellCurr;
			contraAmt = sellAmt;
			amt = buyAmt;
			purSellInd = "P";
			multiplyDiv = "M";
			exRate = contraAmt.divide(amt, 8, RoundingMode.CEILING);
			prepareMessage(curr, amt, contraCurr, contraAmt, purSellInd, exRate, multiplyDiv, dealno, frontEndDealno,
					dealDate, valueDate, frontEndDate, dealText, currPremDisc, baseeqAmt);

		} else if (trType.equalsIgnoreCase("BASEBUY")) {
			curr = sellCurr;
			contraCurr = buyCurr;
			contraAmt = buyAmt;
			amt = sellAmt;
			purSellInd = "S";
			multiplyDiv = "M";
			exRate = contraAmt.divide(amt, 8, RoundingMode.CEILING);
			prepareMessage(curr, amt, contraCurr, contraAmt, purSellInd, exRate, multiplyDiv, dealno, frontEndDealno,
					dealDate, valueDate, frontEndDate, dealText, currPremDisc, baseeqAmt);

		} else if (trType.equalsIgnoreCase("CROSS")) {
			pushBuyBaseData(buyCurr, buyAmt, baseCurrCode, baseeqAmt);
			pushSellBaseData(sellCurr, sellAmt, baseCurrCode, baseeqAmt);

		}

	}

	private void pushSellBaseData(String sellCurr, BigDecimal sellAmt, String baseCurrCode, BigDecimal baseeqAmt) {
		curr = sellCurr;
		amt = sellAmt;
		contraCurr = baseCurrCode;
		contraAmt = baseeqAmt;
		purSellInd = "S";
		exRate = contraAmt.divide(amt, 8, RoundingMode.CEILING);
		multiplyDiv = "M";
		dealno = getF_IN_DealNo();
		frontEndDealno = getF_IN_FrontendDealNo();
		dealDate = getF_IN_SysDate();
		valueDate = getF_IN_OptionEndOrSellMaturityDate();
		frontEndDate = getF_IN_FrontEndDate();
		dealText = getF_IN_FXDealID();
		currPremDisc = "0";
		prepareMessage(curr, amt, contraCurr, contraAmt, purSellInd, exRate, multiplyDiv, dealno, frontEndDealno,
				dealDate, valueDate, frontEndDate, dealText, currPremDisc, baseeqAmt);

	}

	private void pushBuyBaseData(String buyCurr, BigDecimal buyAmt, String baseCurrCode, BigDecimal baseeqAmt) {

		curr = buyCurr;
		amt = buyAmt;
		contraCurr = baseCurrCode;
		contraAmt = baseeqAmt;
		purSellInd = "P";
		exRate = contraAmt.divide(amt, 8, RoundingMode.CEILING);
		multiplyDiv = "M";
		dealno = getF_IN_DealNo();
		frontEndDealno = getF_IN_FrontendDealNo();
		dealDate = getF_IN_SysDate();
		valueDate = getF_IN_OptionEndOrSellMaturityDate();
		frontEndDate = getF_IN_FrontEndDate();
		dealText = getF_IN_FXDealID();
		currPremDisc = "0";
		int x;
		Map idgenfor=new HashMap<>();
		idgenfor.put("idGenerationFormula", "UB_OPX_Autonumber");
		
		HashMap fdealid = MFExecuter.executeMF("UB_OPX_FrontEndDealIdgeneration_SRV",
				BankFusionThreadLocal.getBankFusionEnvironment());
		HashMap dealid = MFExecuter.executeMF("UB_OPX_Idgeneration_SRV",
				BankFusionThreadLocal.getBankFusionEnvironment(),idgenfor);
		String frtDealId = (String) fdealid.get("FrontEndDealId");
		String uDealId = String.valueOf(dealid.get("UniqueID")) ;
		prepareMessage(curr, amt, contraCurr, contraAmt, purSellInd, exRate, multiplyDiv, uDealId, frtDealId, dealDate,
				valueDate, frontEndDate, dealText, currPremDisc, baseeqAmt);

	}

	private void prepareMessage(String curr2, BigDecimal amt2, String contraCurr2, BigDecimal contraAmt2,
			String purSellInd2, BigDecimal exRate2, String multiplyDiv2, String dealno2, String frontEndDealno2,
			Date dealDate2, Date valueDate2, Date frontEndDate2, String dealText2, String currPremDisc2,
			BigDecimal baseeqAmt2) {

		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bfub:UBTOOPICS_TRANSACTIONUPDATES xmlns:bfub=\"http://www.misys.com/ub/types\"><bfub:FRONTENDDEALNO>FRENDDEALNO</bfub:FRONTENDDEALNO><bfub:ERRORCODE/><bfub:FRONTENDDATE>FRENDDATE</bfub:FRONTENDDATE><bfub:FRONTENDTIME/><bfub:DEALNUMBER>DLNO</bfub:DEALNUMBER><bfub:SWAPDEALNUMBER/><bfub:BOODATE/><bfub:BOOTIME/><bfub:LSTMNTDATE/><bfub:VALUEDATE>VALDT</bfub:VALUEDATE><bfub:DEALDATE>DLDATE</bfub:DEALDATE><bfub:CURRENCYCODE>CURNCODE</bfub:CURRENCYCODE><bfub:CURRENCYAMOUNT>CURAMT</bfub:CURRENCYAMOUNT><bfub:TERMSOFTHERATE>MULDIV</bfub:TERMSOFTHERATE><bfub:DEALRATEASSOCIATEDWITHCURRENCY>EXCHRT</bfub:DEALRATEASSOCIATEDWITHCURRENCY><bfub:CURRENCYPREMIUMANDDISCOUNT>0</bfub:CURRENCYPREMIUMANDDISCOUNT><bfub:COUNTERCURRENCYCODE>COUNTERCURCODE</bfub:COUNTERCURRENCYCODE><bfub:COUNTERCURRENCYAMOUNT>COUNTERCURAMOUNT</bfub:COUNTERCURRENCYAMOUNT><bfub:COUNTRYBASEAMOUNT>BASEEQ</bfub:COUNTRYBASEAMOUNT><bfub:FIXEDRATEORNDFDEALINDICATOR/><bfub:PURCHASESALEINDICATOR>PURSELL</bfub:PURCHASESALEINDICATOR><bfub:DEALTEXT>UID</bfub:DEALTEXT></bfub:UBTOOPICS_TRANSACTIONUPDATES>";
		final String finxml = xml.replace("FRENDDEALNO", frontEndDealno2).replace("FRENDDATE", frontEndDate2.toString())
				.replace("DLNO", dealno2).replace("VALDT", valueDate2.toString())
				.replace("DLDATE", dealDate2.toString()).replace("CURNCODE", curr2).replace("CURAMT", amt2.toString())
				.replace("MULDIV", multiplyDiv2).replace("EXCHRT", exRate2.toString())
				.replace("COUNTERCURCODE", contraCurr2).replace("COUNTERCURAMOUNT", contraAmt2.toString())
				.replace("BASEEQ", baseeqAmt2.toString()).replace("PURSELL", purSellInd2).replace("UID", dealText2);
		MessageProducerUtil.sendMessage(finxml, "UB_to_OPICS_Queue");
	}

}
