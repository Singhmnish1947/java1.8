package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.services.CalcExchangeRateRq;
import bf.com.misys.cbs.services.CalcExchangeRateRs;
import bf.com.misys.cbs.services.ReadModuleConfigurationRq;
import bf.com.misys.cbs.services.ReadModuleConfigurationRs;
import bf.com.misys.cbs.types.CalcExchRateDetails;
import bf.com.misys.cbs.types.ExchangeRateDetails;
import bf.com.misys.cbs.types.ModuleKeyRq;
import bf.com.misys.cbs.types.header.Orig;
import bf.com.misys.cbs.types.header.RqHeader;
import bf.com.misys.ub.types.atm.postingvector.UB_Atm_PostingMessage;
import bf.com.misys.ub.types.atm.postingvector.UB_Atm_PostingMessages;

import com.misys.ub.interfaces.UB_IBI_PaymentsHelper;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractATMJoinAccountingEntries;
import com.trapedza.bankfusion.steps.refimpl.IATMJoinAccountingEntries;

public class ATMJoinAccountingEntries extends AbstractATMJoinAccountingEntries	implements IATMJoinAccountingEntries {

	private static final long serialVersionUID = -6809288218811978005L;
	final String MODULEID = "ATM";
	final String READ_MODULE_CONFIGURATION = "CB_CMN_ReadModuleConfiguration_SRV";
	final String moduleConfigParam = "ATMCASHWTDNETTING";
	private transient final static Log logger = LogFactory.getLog(ATMJoinAccountingEntries.class.getName());
	private UB_Atm_PostingMessage outputPostingMessage = new UB_Atm_PostingMessage();
	private UB_Atm_PostingMessages combinedMessage = new UB_Atm_PostingMessages();
	private UB_Atm_PostingMessage inputPostingMessge;
	private UB_Atm_PostingMessages[] postingMessages;
	private String accountNo;
	private int count;
	private boolean skipJoin = true;

	public ATMJoinAccountingEntries() {
		super();
	}

	public ATMJoinAccountingEntries(BankFusionEnvironment env) {
		super(env);
	}

	@Override
	public void process(BankFusionEnvironment env) throws BankFusionException {
		super.process(env);
		
		outputPostingMessage = new UB_Atm_PostingMessage();
		combinedMessage = new UB_Atm_PostingMessages();
		inputPostingMessge = getF_IN_inputPostingMsg();
		postingMessages = inputPostingMessge.getUB_Atm_PostingMessages();
		accountNo = getF_IN_accountNumber();
		count = 0;
		skipJoin = true;

		if (!getModuleConfigValue(moduleConfigParam)) {
			setF_OUT_outputPostingMsg(inputPostingMessge);
			return;
		}

		if (!getF_IN_transType().equals("01")) {
			setF_OUT_outputPostingMsg(inputPostingMessge);
			return;
		}

		if (getF_IN_txnCode().equals("CSHWTD")|| getF_IN_txnCode().equals("ATMREV")|| getF_IN_txnCode().equals("ATMREP")|| getF_IN_txnCode().equals("REATMREV")) {
			skipJoin = false;
		}

		if (skipJoin) {
			setF_OUT_outputPostingMsg(inputPostingMessge);
			return;
		}

		if (postingMessages == null) {
			setF_OUT_outputPostingMsg(inputPostingMessge);
			return;
		}
		logger.info("ATM transaction Netting Module Configuration Enabled");
		int postingMsgslength = postingMessages.length;
		for (int i = 0; i < postingMsgslength; i++) {
			String accountIdFromMsg = postingMessages[i].getPRIMARYID().trim();
			String txnCurrencyCode = postingMessages[i].getTXNCURRENCYCODE().trim();
			String accountCurrencyCode = postingMessages[i].getACCTCURRENCYCODE().trim();

			if (count == 0 && accountIdFromMsg.equals(accountNo)) {
				count++;
				if (txnCurrencyCode.equals(accountCurrencyCode)) {
					combinedMessage = postingMessages[i];
				} else {
					combinedMessage = convertToTxnCurrencyMessage(postingMessages[i]);
				}
				txnCurrencyCode = postingMessages[i].getTXNCURRENCYCODE();
				continue;
			} else if (count > 0 && accountIdFromMsg.equals(accountNo)) {
				UB_Atm_PostingMessages currentMessage = new UB_Atm_PostingMessages();
				if (txnCurrencyCode.equals(accountCurrencyCode)) {
					currentMessage = postingMessages[i];
				} else {
					currentMessage = convertToTxnCurrencyMessage(postingMessages[i]);
				}
				BigDecimal amount = combinedMessage.getAMOUNT().add(currentMessage.getAMOUNT());
				BigDecimal actualAmount = combinedMessage.getACTUALAMOUNT().add(currentMessage.getACTUALAMOUNT());
				BigDecimal amountCredit = combinedMessage.getAMOUNTCREDIT().add(currentMessage.getAMOUNTCREDIT());
				BigDecimal amountDebit = combinedMessage.getAMOUNTDEBIT().add(currentMessage.getAMOUNTDEBIT());
				combinedMessage.setAMOUNT(amount);
				combinedMessage.setACTUALAMOUNT(actualAmount);
				combinedMessage.setAMOUNTCREDIT(amountCredit);
				combinedMessage.setAMOUNTDEBIT(amountDebit);
			} else {
				outputPostingMessage.addUB_Atm_PostingMessages(postingMessages[i]);
			}
		}

		outputPostingMessage.addUB_Atm_PostingMessages(combinedMessage);
		setF_OUT_outputPostingMsg(outputPostingMessage);
	}

	private UB_Atm_PostingMessages convertToTxnCurrencyMessage(	UB_Atm_PostingMessages ub_Atm_PostingMessages) {
		UB_Atm_PostingMessages atm_PostingMessages = ub_Atm_PostingMessages;
		BigDecimal convertedAmount = BigDecimal.ZERO;
		String channelId = ub_Atm_PostingMessages.getCHANNELID();
		String exchangeRateType = ub_Atm_PostingMessages.getEXCHRATETYPE();
		convertedAmount = convertAmtToAccountCcy(ub_Atm_PostingMessages.getAMOUNT(), ub_Atm_PostingMessages.getTXNCURRENCYCODE(), ub_Atm_PostingMessages.getACCTCURRENCYCODE(), channelId, exchangeRateType );
		atm_PostingMessages.setAMOUNT(convertedAmount);
		atm_PostingMessages.setACTUALAMOUNT(convertedAmount);
		atm_PostingMessages.setAMOUNTCREDIT(convertedAmount);
		atm_PostingMessages.setAMOUNTDEBIT(convertedAmount);
		atm_PostingMessages.setTXNCURRENCYCODE(ub_Atm_PostingMessages.getACCTCURRENCYCODE());
		return atm_PostingMessages;
	}

	private BigDecimal convertAmtToAccountCcy(BigDecimal amount, String txnCurrencyCode, String acctCurrencyCode, String channelId, String exchangeRateType) {
		BigDecimal exchangeRate = UB_IBI_PaymentsHelper.getExchangeRate(exchangeRateType, txnCurrencyCode, acctCurrencyCode, amount);
		BigDecimal convertedAmount = calculateExchRateAmt(txnCurrencyCode, acctCurrencyCode, exchangeRate, amount, channelId, exchangeRateType);
		return convertedAmount;
	}

	private BigDecimal calculateExchRateAmt(String buyCurrency,	String sellCurrency, BigDecimal exchangeRate, BigDecimal buyAmount,	String channelId, String exchangeRateType) {
		RqHeader rqHeader = new RqHeader();
		Orig orig = new Orig();
		orig.setChannelId(channelId);
		rqHeader.setOrig(orig);
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
		exchangeRateDetails.setExchangeRate(exchangeRate);
		exchangeRateDetails.setExchangeRateType(exchangeRateType);
		exchangeDtls.setExchangeRateDetails(exchangeRateDetails);
		exchRq.setRqHeader(rqHeader);
		BankFusionEnvironment env = new BankFusionEnvironment(null);
		HashMap inputMap = new HashMap();
		inputMap.put("CalcExchangeRateRq", exchRq);
		env.setData(new HashMap());
		HashMap outputParams = MFExecuter.executeMF("CB_FEX_CalculateExchangeRateAmount_SRV", env, inputMap);
		CalcExchangeRateRs calcExchangeRateRs = (CalcExchangeRateRs) outputParams.get("CalcExchangeRateRs");
		BigDecimal equivalentAmount = calcExchangeRateRs.getCalcExchRateResults().getSellAmountDetails().getAmount();
		if (buyAmount.signum() < 0) {
			equivalentAmount = BigDecimal.ZERO.subtract(equivalentAmount);
		}
		return equivalentAmount;
	}

	private boolean getModuleConfigValue(String moduleValue) {
		boolean value = false;
		HashMap<String, ReadModuleConfigurationRq> moduleParams = new HashMap<String, ReadModuleConfigurationRq>();
		ModuleKeyRq module = new ModuleKeyRq();
		ReadModuleConfigurationRq read = new ReadModuleConfigurationRq();
		module.setModuleId(MODULEID);
		module.setKey(moduleValue);
		read.setModuleKeyRq(module);
		moduleParams.put("ReadModuleConfigurationRq", read);
		HashMap valueFromModuleConfiguration = MFExecuter.executeMF(READ_MODULE_CONFIGURATION, BankFusionThreadLocal.getBankFusionEnvironment(), moduleParams);
		if (valueFromModuleConfiguration != null) {
			ReadModuleConfigurationRs rs = (ReadModuleConfigurationRs) valueFromModuleConfiguration.get("ReadModuleConfigurationRs");
			if (rs.getModuleConfigDetails().getValue().equals("true")) {
				value = true;
			} else {
				value = false;
			}
		}
		return value;
	}
}
