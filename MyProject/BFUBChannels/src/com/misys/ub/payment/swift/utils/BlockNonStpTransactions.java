package com.misys.ub.payment.swift.utils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.calendar.functions.AddDaysToDate;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.bankfusion.subsystem.microflow.runtime.impl.MFExecuter;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.cbs.common.functions.CB_CMN_DateToTimeStampEnd;
import com.misys.ub.common.MISTransactionCodeDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;

import bf.com.misys.cbs.msgs.v1r0.CreateAccountHoldRq;
import bf.com.misys.cbs.msgs.v1r0.OutwardSwtRemittanceRq;
import bf.com.misys.cbs.types.AccountHoldBasicDtls;
import bf.com.misys.cbs.types.AccountHoldDetails;
import bf.com.misys.cbs.types.AccountKeys;
import bf.com.misys.cbs.types.Currency;
import bf.com.misys.cbs.types.InputAccount;
import bf.com.misys.cbs.types.Posting;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RqHeader;
import bf.com.misys.cbs.types.header.RsHeader;

public class BlockNonStpTransactions {
	private static final transient Log LOGGER = LogFactory.getLog(BlockNonStpTransactions.class.getName());

	/**
	 * This method blocks the transcation amount 
	 * @param amount
	 * @param isoCurrencyCode
	 * @param inputAccountId
	 * @param expiryDate
	 * @return CreateAccountHoldRs
	 */
	public RsHeader createBlockingAmount(OutwardSwtRemittanceRq outwardRq) {
		//unigque id 
		CreateAccountHoldRq createAccountHoldRq = new CreateAccountHoldRq();
		AccountHoldDetails accountHoldDetails = new AccountHoldDetails();
		AccountHoldBasicDtls accountHoldBasicDtls = new AccountHoldBasicDtls();
		Posting blockDtls = outwardRq.getIntlPmtInputRq().getFundingPosting();
		String inputAccountId = !StringUtils.isBlank(blockDtls.getAccount().getStandardAccountId()) ? blockDtls.getAccount().getStandardAccountId() : blockDtls.getAccount().getInputAccount().getInputAccountId();
		BigDecimal amount = blockDtls.getCurrency().getAmount();
		String isoCurrencyCode = blockDtls.getCurrency().getIsoCurrencyCode();
		Timestamp expiryDate = blockDtls.getValueDate();
		RqHeader rqHeader = new RqHeader();
		RsHeader rsHeader = new RsHeader();
		MessageStatus txnStatus = new MessageStatus();
		AccountKeys accountKeys = new AccountKeys();
		InputAccount inputAccount = new InputAccount();
		Currency heldAmount = new Currency();
		String holdReference = outwardRq.getIntlPmtInputRq().getTxnInputData().getOriginalHostTxnRef();
		//amount
		heldAmount.setAmount(amount);
		heldAmount.setIsoCurrencyCode(isoCurrencyCode);
		accountHoldBasicDtls.setHeldAmount(heldAmount);
		//expirydate
		accountHoldBasicDtls.setExpiryDate(getExpiryDate(outwardRq.getSwftAdditionalDetails().getDebitTxnCode(), expiryDate));
		//holdReference
		accountHoldBasicDtls.setHoldReference(outwardRq.getIntlPmtInputRq().getTxnInputData().getOriginalHostTxnRef());
		inputAccount.setInputAccountId(inputAccountId);
		accountKeys.setInputAccount(inputAccount);
		accountKeys.setStandardAccountId(inputAccountId);
		accountHoldDetails.setAccountHoldBasicDtls(accountHoldBasicDtls);
		accountHoldDetails.setHoldCategory(PaymentSwiftConstants.CHANNELID_SWIFT);
		accountHoldDetails.setAccountKeys(accountKeys);
		createAccountHoldRq.setRqHeader(rqHeader);
		createAccountHoldRq.setAccountHoldDetails(accountHoldDetails);
		HashMap<String, Object> startFatomData = new HashMap<>();
		startFatomData.put(MFInputOutPutKeys.CREATE_ACCT_HOLDER_REQ, createAccountHoldRq);
		Map outputParams = MFExecuter.executeMF(PaymentSwiftConstants.CB_ACC_ADD_ACCOUNT_BLOCKS, BankFusionThreadLocal.getBankFusionEnvironment(), startFatomData);
		txnStatus.setOverallStatus(PaymentSwiftConstants.OVERALL_SUCCESS_STATUS);
		rsHeader.setStatus(txnStatus);
		rsHeader.setOrigCtxtId(holdReference);
		return rsHeader;
	}

	private Timestamp getExpiryDate(String misTxnCode, Timestamp expiryDate) {
		MISTransactionCodeDetails mistransDetails;
        Date computedDate = SystemInformationManager.getInstance().getBFSystemDate();
		IBOMisTransactionCodes misTransactionCodes = getMisTransactionCodes(misTxnCode);
		if (misTransactionCodes != null) {
			LOGGER.info("BLOCKING DAYS" + misTransactionCodes.getF_BLOCKINGDAYS());
			LOGGER.info("BLOCKING CONTROL" + misTransactionCodes.getF_BLOCKINGCONTROL());
			//if financial blocking is enabled add days to date
			if (misTransactionCodes.getF_BLOCKINGCONTROL() == 1) {
				computedDate = AddDaysToDate.run(expiryDate, misTransactionCodes.getF_BLOCKINGDAYS());
				expiryDate = CB_CMN_DateToTimeStampEnd.run(computedDate);
			}
			else {
				PaymentSwiftUtils.handleEvent(Integer.parseInt("40010070"), new String[] {});
			}
		}
		LOGGER.info("New Computed ExpiryDate:::" + expiryDate);
		return expiryDate;
	}

	private IBOMisTransactionCodes getMisTransactionCodes(String misCode) {
		IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
		IBOMisTransactionCodes misCodeBO = (IBOMisTransactionCodes) factory.findByPrimaryKey(IBOMisTransactionCodes.BONAME, misCode, true);
		return misCodeBO;
	}
}
