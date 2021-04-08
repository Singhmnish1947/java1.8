package com.misys.ub.payment.swift.posting;

import org.apache.commons.lang.StringUtils;

import bf.com.misys.cbs.msgs.v1r0.OutwardSwtRemittanceRq;
import bf.com.misys.cbs.types.LocalCashDetails;
import bf.com.misys.cbs.types.LocalChargeDetails;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.ub.types.core.financialpostingvector.PostingEngineMessage;
import bf.com.misys.ub.types.core.financialpostingvector.PostingEngineMessageInput;
import bf.com.misys.ub.types.remittanceprocess.UB_SWT_RemittanceProcessRq;

import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.ub.payment.posting.SWTPostingUtils;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_RemittanceTable;
import com.trapedza.bankfusion.core.VectorTable;

public class PostNonStpCashTransaction extends AbstractPostTransaction {
	/**
	 * @param args
	 */
	public RsHeader postCashTxn(UB_SWT_RemittanceProcessRq remittanceRq, IBOUB_SWT_RemittanceTable remittanceDtls) {
		RsHeader rsHeader = new RsHeader();
		PostingDto postingDto = new PostingDto();
		//debit internal account from module config  and credit nostro account
		postingDto.setManualValueDate(remittanceRq.getTRANSACTIONDETAISINFO().getDATEOFPROCESSING());
		postingDto.setChannelID(remittanceRq.getCHANNELID());
		postingDto.setBranchCode(BankFusionThreadLocal.getUserSession().getBranchSortCode());
		postingDto.setPayReceiveFlag(Boolean.FALSE);
		postingDto.setTransactionID(remittanceDtls.getF_UBTRANSACTIONID());
		postingDto.setOriginalTxnReference(remittanceDtls.getF_UBTRANSACTIONID());
		PostingEngineMessageInput postingMsgInput = new PostingEngineMessageInput();
		postingMsgInput.setChannelID(remittanceRq.getCHANNELID() != null ? remittanceRq.getCHANNELID() : "UXP");
		postingMsgInput.setPostingEngineMessage(preparePostingArray(remittanceRq, remittanceDtls));
		postingDto.setPostingMsgInput(postingMsgInput);
		//set charge list
		postingDto.setChargesList(SWTPostingUtils.getChargeVector(remittanceDtls.getF_UBMESSAGEREFID()));
		OutwardSwtRemittanceRq outwardRq = UnblobOutwardRemittanceRq.run("UB_INF_MessageHeader", "INMESSAGEID1PK", remittanceDtls.getF_UBMESSAGEREFID(), "DATAMESSAGE");
		if (outwardRq != null) {
			//set local charge details
			postingDto = setLocalChargePostingDtls(remittanceDtls, postingDto, outwardRq);
		}
		//post Cash transaction by calling posting engine
		rsHeader = postTxn(postingDto);
		return rsHeader;
	}

	/**
	 * Credit Posting Leg
	 * @param remittanceRq
	 * @param remittanceDtls
	 * @return
	 */
	private PostingEngineMessage prepareCreditLegMessage(UB_SWT_RemittanceProcessRq remittanceRq, IBOUB_SWT_RemittanceTable remittanceDtls) {
		PostingEngineMessage crLeg = new PostingEngineMessage();
		//credit Nostro account
		//credit leg
		crLeg.setPostingLegNumber(1);
		crLeg.setPostingMessageAccountId(remittanceRq.getCREDITORDTL().getCREDITACCOUNTID());
		crLeg.setPostingMessageAccountPostingAction(PaymentSwiftConstants.CREDIT);
		crLeg.setPostingMessageExchangeRateType(remittanceRq.getExchangeRateTypeIN());
		crLeg.setPostingMessageISOCurrencyCode(remittanceRq.getCrAccountCurrency());
		crLeg.setPostingMessageExchangeRate(remittanceRq.getTRANSACTIONDETAISINFO().getEXCHANGERATEFORINCOMING());
		//TODO:replace with expectedCreditAmt
		crLeg.setPostingMessageTransactionAmount(remittanceRq.getCREDITORDTL().getEXPECTEDCREDITAMOUNT());
		crLeg.setPostingMessageTransactionCode(remittanceDtls.getF_UBCRTXNCODE());
		crLeg.setPostingMessageTransactionNarrative(remittanceRq.getTRANSACTIONDETAISINFO().getNARRATION());
		crLeg.setPostingMessageTransactionReference(remittanceRq.getTRANSACTIONDETAISINFO().getTRANSACTIONREFERENCE());
		return crLeg;
	}

	/**
	 * Debit Posting Leg
	 * @param remittanceRq
	 * @param remittanceDtls
	 * @return
	 */
	private PostingEngineMessage prepareDebitLegMessage(UB_SWT_RemittanceProcessRq remittanceRq, IBOUB_SWT_RemittanceTable remittanceDtls) {
		//debit internal account
		PostingEngineMessage drLeg = new PostingEngineMessage();
		String branchCode = SWTPostingUtils.getAccountBranchSortCode(remittanceRq.getDEBITORDTL().getDEBITACCOUNTID());
		String debitAmtCcy = remittanceRq.getDrAccountCurrency() != null ? remittanceRq.getDrAccountCurrency() : StringUtils.EMPTY;
		//find the internal account from module configuration
		String internalAccount = SWTPostingUtils.getSuspenseAccountFromModuleConfig(debitAmtCcy, branchCode);
		drLeg.setPostingLegNumber(2);
		drLeg.setPostingMessageAccountId(internalAccount);
		drLeg.setPostingMessageAccountPostingAction(PaymentSwiftConstants.DEBIT);
		//	crLeg.setPostingMessageChequeDraftNumber(postingMessageChequeDraftNumber);
		drLeg.setPostingMessageExchangeRateType(remittanceRq.getExchangeRateTypeOUT());
		drLeg.setPostingMessageISOCurrencyCode(debitAmtCcy);
		drLeg.setPostingMessageExchangeRate(remittanceRq.getTRANSACTIONDETAISINFO().getEXCHANGERATEFOROUTGOING());
		drLeg.setPostingMessageTransactionAmount(remittanceRq.getDEBITORDTL().getEXPECTEDDEBITAMOUNT());
		drLeg.setPostingMessageTransactionCode(remittanceDtls.getF_UBDRTXNCODE());
		drLeg.setPostingMessageTransactionNarrative(remittanceRq.getTRANSACTIONDETAISINFO().getNARRATION());
		drLeg.setPostingMessageTransactionReference(remittanceRq.getTRANSACTIONDETAISINFO().getTRANSACTIONREFERENCE());
		return drLeg;
	}

	/**
	 * @param remittanceRq
	 * @param remittanceDtls
	 * @return
	 */
	private PostingEngineMessage[] preparePostingArray(UB_SWT_RemittanceProcessRq remittanceRq, IBOUB_SWT_RemittanceTable remittanceDtls) {
		PostingEngineMessage[] postingArray = new PostingEngineMessage[2];
		postingArray[0] = prepareCreditLegMessage(remittanceRq, remittanceDtls);
		postingArray[1] = prepareDebitLegMessage(remittanceRq, remittanceDtls);
		return postingArray;
	}

	/**
	 * @param remittanceDtls
	 * @param postingDto
	 * @param outwardRq
	 * @return
	 */
	private PostingDto setLocalChargePostingDtls(IBOUB_SWT_RemittanceTable remittanceDtls, PostingDto postingDto, OutwardSwtRemittanceRq outwardRq) {
		if (getLocalCashDetails(outwardRq) != null) {
			postingDto.setLocalAmountAccountID(outwardRq.getIntlPmtInputRq().getTxnFXData().getLocalRoundingDetails().getLocalCashDetails().getLclCashAcctDetails().getStandardAccountId());
			postingDto.setLocalAmountAmount(outwardRq.getIntlPmtInputRq().getTxnFXData().getLocalRoundingDetails().getLocalCashDetails().getLclCashAmtDetails().getAmount());
			postingDto.setLocalAmountAmount_CurrCode(outwardRq.getIntlPmtInputRq().getTxnFXData().getLocalRoundingDetails().getLocalCashDetails().getLclCashAmtDetails().getIsoCurrencyCode());
			postingDto.setLocalAmountISOCurrencyCode(outwardRq.getIntlPmtInputRq().getTxnFXData().getLocalRoundingDetails().getLocalCashDetails().getLclCashAmtDetails().getIsoCurrencyCode());
		}
		if (getLocalChargedDetails(outwardRq) != null) {
			postingDto.setLocalChargeAmountAccountID(outwardRq.getIntlPmtInputRq().getTxnFXData().getLocalRoundingDetails().getLclChargeDetails().getLclChgChgAcct().getStandardAccountId());
			postingDto.setLocalChargeAmountAmount(outwardRq.getIntlPmtInputRq().getTxnFXData().getLocalRoundingDetails().getLclChargeDetails().getLclChgAmtDetails().getAmount());
			postingDto.setLocalChargeAmountAmount_CurrCode(outwardRq.getIntlPmtInputRq().getTxnFXData().getLocalRoundingDetails().getLclChargeDetails().getLclChgAmtDetails().getIsoCurrencyCode());
			postingDto.setLocalChargeAmountISOCurrecny(outwardRq.getIntlPmtInputRq().getTxnFXData().getLocalRoundingDetails().getLclChargeDetails().getLclChgAmtDetails().getIsoCurrencyCode());
		}
		return postingDto;
	}

	/**
	 * @param outwardRq
	 * @return
	 */
	private LocalCashDetails getLocalCashDetails(OutwardSwtRemittanceRq outwardRq) {
		if (outwardRq != null && outwardRq.getIntlPmtInputRq().getTxnFXData() != null && outwardRq.getIntlPmtInputRq().getTxnFXData().getLocalRoundingDetails() != null && outwardRq.getIntlPmtInputRq().getTxnFXData().getLocalRoundingDetails().getLocalCashDetails() != null) {
			LocalCashDetails localCashDetails = outwardRq.getIntlPmtInputRq().getTxnFXData().getLocalRoundingDetails().getLocalCashDetails();
			return localCashDetails;
		}
		return null;
	}

	/**
	 * @param outwardRq
	 * @return
	 */
	private LocalChargeDetails getLocalChargedDetails(OutwardSwtRemittanceRq outwardRq) {
		if (outwardRq != null && outwardRq.getIntlPmtInputRq().getTxnFXData() != null && outwardRq.getIntlPmtInputRq().getTxnFXData().getLocalRoundingDetails() != null && outwardRq.getIntlPmtInputRq().getTxnFXData().getLocalRoundingDetails().getLclChargeDetails() != null) {
			LocalChargeDetails lclChargeDetails = outwardRq.getIntlPmtInputRq().getTxnFXData().getLocalRoundingDetails().getLclChargeDetails();
			return lclChargeDetails;
		}
		return null;
	}
}
