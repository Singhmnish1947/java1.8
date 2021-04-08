package com.misys.ub.payment.swift.posting;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.msgs.v1r0.OutwardSwtRemittanceRq;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.ub.types.core.financialpostingvector.PostingEngineMessage;
import bf.com.misys.ub.types.core.financialpostingvector.PostingEngineMessageInput;
import bf.com.misys.ub.types.remittanceprocess.UB_SWT_RemittanceProcessRq;

import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.ub.payment.posting.SWTPostingUtils;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.UnblockNonStpTransactions;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_RemittanceTable;
import com.trapedza.bankfusion.core.VectorTable;

/**
 * @author machamma.devaiah
 *
 */
public class PostNonStpAccountTranscation extends AbstractPostTransaction {
	/**
	 * @param remittanceRq
	 * @param remittanceDtls
	 * @return
	 */
	private static final transient Log LOGGER = LogFactory.getLog(PostNonStpAccountTranscation.class.getName());

	public RsHeader postAccountTxn(UB_SWT_RemittanceProcessRq remittanceRq, IBOUB_SWT_RemittanceTable remittanceDtls) {
		LOGGER.info("START ____PostNonStpAccountTranscation");
		RsHeader rsHeader = new RsHeader();
		UnblockNonStpTransactions unBlock = new UnblockNonStpTransactions();
		PostingDto postingDto = new PostingDto();
		String debitAccountId = remittanceRq.getDEBITORDTL().getDEBITACCOUNTID() != null ? remittanceRq.getDEBITORDTL().getDEBITACCOUNTID() : StringUtils.EMPTY;
		//unblock the transaction
		unBlock.unblockNonStpTransaction(remittanceDtls.getF_UBBLOCKINGREFERENCE(), debitAccountId);
		//post the account transaction
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
		//get ChargeDetails
		//unblob
		//OutwardSwtRemittanceRq outwardRq = UnblobOutwardRemittanceRq.run("UB_INF_MessageHeader", "INMESSAGEID1PK", remittanceDtls.getF_UBMESSAGEREFID(), "DATAMESSAGE");
		postingDto.setChargesList(SWTPostingUtils.getChargeVector(remittanceDtls.getF_UBMESSAGEREFID()));
		//call to posting engine
		rsHeader = postTxn(postingDto);
		LOGGER.info("END ____PostNonStpAccountTranscation");
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
		//TODO:handle ChequeOrDraftNumber
		//	crLeg.setPostingMessageChequeDraftNumber(postingMessageChequeDraftNumber);
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
		//debit customerAccount
		PostingEngineMessage drLeg = new PostingEngineMessage();
		String branchCode = SWTPostingUtils.getAccountBranchSortCode(remittanceRq.getDEBITORDTL().getDEBITACCOUNTID());
		String debitAmtCcy = remittanceRq.getDrAccountCurrency() != null ? remittanceRq.getDrAccountCurrency() : StringUtils.EMPTY;
		drLeg.setPostingLegNumber(2);
		//debit account
		drLeg.setPostingMessageAccountId(remittanceRq.getDEBITORDTL().getDEBITACCOUNTID());
		drLeg.setPostingMessageAccountPostingAction(PaymentSwiftConstants.DEBIT);
		//cheque number
		drLeg.setPostingMessageChequeDraftNumber(remittanceDtls.getF_UBCHEQUENUMBER());
		drLeg.setPostingMessageExchangeRateType(remittanceRq.getExchangeRateTypeOUT());
		drLeg.setPostingMessageISOCurrencyCode(debitAmtCcy);
		drLeg.setPostingMessageExchangeRate(remittanceRq.getTRANSACTIONDETAISINFO().getEXCHANGERATEFOROUTGOING());
		//debit amount
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
}
