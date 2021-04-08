package com.misys.ub.swift.tellerRemittance.charges;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.finastra.api.paymentInititation.CreditTransferTransaction20;
import com.finastra.api.paymentInititation.CustomerCreditTransferInitiationV06;
import com.finastra.api.paymentInititation.FeeCalculationRequest;
import com.finastra.api.paymentInititation.GroupHeader48;
import com.finastra.api.paymentInititation.PaymentInstruction16;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.misys.ub.swift.tellerRemittance.utils.RequestMapperUtil;

public class FeeRequestMapper {
	private transient final static Log LOGGER = LogFactory.getLog(FeeRequestMapper.class);

	private FeeRequestMapper() {
	}

	public static String mapFeeCalculationRequest(FeeCalculationRequest apiRequest) {

		Map<String, Object> mapObject = new LinkedHashMap<>();
		// 1st part
		mapObject.put("InitiationContext", prepareInitiationContext(apiRequest));

		// 2nd part
		mapObject.put("CstmrCdtTrfInitn", prepareCustomerCreditTransfer(apiRequest));
		// 3rd part
		mapObject.put("@xmlns", "urn:iso:std:iso:20022:tech:xsd:pain.001.001.06");
		Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
		String prettyJson = prettyGson.toJson(mapObject);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("FeeCalculationRequest to GPP :::::" + prettyJson);
		}

		return prettyJson;

	}

	/**
	 * Method Description:Prepare the InitiationContext
	 * 
	 * @param remittanceRq
	 * @return
	 */
	private static Map<String, Object> prepareInitiationContext(FeeCalculationRequest apiRequest) {
		Map<String, Object> initiationContextMap = new LinkedHashMap<>();

		if (null != apiRequest.getInitiationContext().getId()) {
			initiationContextMap.put("id", apiRequest.getInitiationContext().getId());
		}

		if (null != apiRequest.getInitiationContext().getSubId()) {
			initiationContextMap.put("subId", apiRequest.getInitiationContext().getSubId());
		}

		// paymentMethod
		if (null != apiRequest.getInitiationContext().getSchmeNm()) {
			initiationContextMap.put("schmeNm", apiRequest.getInitiationContext().getSchmeNm());
		}

		initiationContextMap.put("saveOnError", apiRequest.getInitiationContext().isSaveOnError());
		// channelId
		if (apiRequest.getInitiationContext().getSourceId() != null) {
			initiationContextMap.put("sourceId", apiRequest.getInitiationContext().getSourceId());
		}

		return initiationContextMap;
	}

	private static Map<String, Object> prepareCustomerCreditTransfer(FeeCalculationRequest apiRequest) {
		Map<String, Object> customerCreditTransfertMap = new LinkedHashMap<>();
		Map<String, Object> grpHdrMap = new LinkedHashMap<>();

		CustomerCreditTransferInitiationV06 cstmrCdtTrfInitn = apiRequest.getCstmrCdtTrfInitn();
		GroupHeader48 grpHdr = cstmrCdtTrfInitn.getGrpHdr();

		if (cstmrCdtTrfInitn.getGrpHdr().getMsgId() != null) {
			grpHdrMap.put("MsgId", cstmrCdtTrfInitn.getGrpHdr().getMsgId());
		}

		if (cstmrCdtTrfInitn.getGrpHdr().getCreDtTm() != null) {
			grpHdrMap.put("CreDtTm", cstmrCdtTrfInitn.getGrpHdr().getCreDtTm());
		}

		if (cstmrCdtTrfInitn.getGrpHdr().getAuthstn() != null) {
			grpHdrMap.put("Authstn", cstmrCdtTrfInitn.getGrpHdr().getAuthstn());
		}

		if (cstmrCdtTrfInitn.getGrpHdr().getNbOfTxs() != null) {
			grpHdrMap.put("NbOfTxs", cstmrCdtTrfInitn.getGrpHdr().getNbOfTxs());
		}

		// InitgPty
		if (cstmrCdtTrfInitn.getGrpHdr().getInitgPty() != null) {
			grpHdrMap.put("InitgPty", RequestMapperUtil.buildInitgPty(grpHdr.getInitgPty()));
		}
		customerCreditTransfertMap.put("GrpHdr", grpHdrMap);

		// PmtInf
		if (cstmrCdtTrfInitn.getPmtInf() != null) {
			customerCreditTransfertMap.put("PmtInf", preparePaymentInstruction(cstmrCdtTrfInitn.getPmtInf()));
		}

		return customerCreditTransfertMap;
	}


	/**
	 * Method Description:
	 * 
	 * @param cstmrCdtTrfInitn
	 * @return
	 */
	private static List<Object> preparePaymentInstruction(List<PaymentInstruction16> pmtInfList) {
		List<Object> pmtInstructionList = new ArrayList<>();
		Map<String, Object> pmtInstructionMap = new LinkedHashMap<>();
		if (pmtInfList != null) {
			for (PaymentInstruction16 pmtInf : pmtInfList) {
				if (pmtInf.getPmtInfId() != null) {
					pmtInstructionMap.put("PmtInfId", pmtInf.getPmtInfId());
				}

				if (pmtInf.getBtchBookg()) {
					pmtInstructionMap.put("BtchBookg", pmtInf.getBtchBookg());
				}

				if (pmtInf.getPmtMtd() != null) {
					pmtInstructionMap.put("PmtMtd", pmtInf.getPmtMtd());
				}

				if (pmtInf.getChrgBr() != null) {
					pmtInstructionMap.put("ChrgBr", pmtInf.getChrgBr());
				}

				// SvcLvl
				if (pmtInf.getPmtTpInf() != null) {
					pmtInstructionMap.put("PmtTpInf", RequestMapperUtil.buildPmtTpInf(pmtInf.getPmtTpInf()));
				}

				if (pmtInf.getReqdExctnDt() != null) {
					pmtInstructionMap.put("ReqdExctnDt", pmtInf.getReqdExctnDt());
				}

				// 50 Ordering Customer
				if (pmtInf.getDbtr() != null) {
					pmtInstructionMap.put("Dbtr", RequestMapperUtil.buildPartyIdentification43(pmtInf.getDbtr()));
				}

				// 50 Ordering Customer
				if (pmtInf.getDbtrAcct() != null) {
					pmtInstructionMap.put("DbtrAcct", RequestMapperUtil.buildCashAccount24(pmtInf.getDbtrAcct()));
				}
				// 52 Ordering Institution
				if (pmtInf.getDbtrAgt() != null) {
					pmtInstructionMap.put("DbtrAgt", RequestMapperUtil.buildBranchFinancialInst(pmtInf.getDbtrAgt()));
				}

				// 52 Ordering Institution
				if (pmtInf.getDbtrAgtAcct() != null) {
					pmtInstructionMap.put("DbtrAgtAcct", RequestMapperUtil.buildCashAccount24(pmtInf.getDbtrAgtAcct()));
				}

				// CdtTrfTxInf
				if (pmtInf.getCdtTrfTxInf() != null) {
					pmtInstructionMap.put("CdtTrfTxInf",
							prepareCustomerCreditTransferInitiationV06(pmtInf.getCdtTrfTxInf()));
				}
			}

			pmtInstructionList.add(pmtInstructionMap);
		}

		return pmtInstructionList;
	}

	/**
	 * Method Description:Prepare CustomerCreditTransferInitiationV06
	 * 
	 * @param pmtInf
	 * @return
	 */
	private static List<Object> prepareCustomerCreditTransferInitiationV06(
			List<CreditTransferTransaction20> cdtTrfTxInfList) {
		Map<String, Object> cdtTrfTxInfMap = new LinkedHashMap<>();
		List<Object> cdtTrfTxInfSuperList = new ArrayList<>();

		// CdtTrfTxInf
		if (cdtTrfTxInfList != null) {
			for (CreditTransferTransaction20 cdtTrfTxInf : cdtTrfTxInfList) {

				if (cdtTrfTxInf.getPmtId() != null) {
					cdtTrfTxInfMap.put("PmtId", RequestMapperUtil.buildPmtId(cdtTrfTxInf.getPmtId()));
				}

				if (cdtTrfTxInf.getAmt() != null) {
					cdtTrfTxInfMap.put("Amt", RequestMapperUtil.buildAmount(cdtTrfTxInf.getAmt()));
				}

				// XchgRateInf
				if (cdtTrfTxInf.getXchgRateInf() != null) {
					cdtTrfTxInfMap.put("XchgRateInf", RequestMapperUtil.buildXchgRateInf(cdtTrfTxInf.getXchgRateInf()));
				}

				// 59
				// Beneficiary Customer
				if (cdtTrfTxInf.getCdtr() != null) {
					cdtTrfTxInfMap.put("Cdtr", RequestMapperUtil.buildPartyIdentification43(cdtTrfTxInf.getCdtr()));
				}

				// 59
				// Beneficiary Customer
				if (cdtTrfTxInf.getCdtrAcct() != null) {
					cdtTrfTxInfMap.put("CdtrAcct", RequestMapperUtil.buildCashAccount24(cdtTrfTxInf.getCdtrAcct()));
				}

				// TransactionTypeCode = CdtTrfTxInf/Purp/Prtry
				if (cdtTrfTxInf.getPurp() != null) {
					cdtTrfTxInfMap.put("Purp", RequestMapperUtil.buildPurpose2Choice(cdtTrfTxInf.getPurp()));
				}

				// 57
				// Account With Institution
				if (cdtTrfTxInf.getCdtrAgt() != null) {
					cdtTrfTxInfMap.put("CdtrAgt", RequestMapperUtil.buildBranchFinancialInst(cdtTrfTxInf.getCdtrAgt()));
				}

				// 57
				// Account With Institution
				if (cdtTrfTxInf.getCdtrAgtAcct() != null) {
					cdtTrfTxInfMap.put("CdtrAgtAcct", RequestMapperUtil.buildCashAccount24(cdtTrfTxInf.getCdtrAgtAcct()));
				}

				// 56
				// IntermediaryInstitution
				if (cdtTrfTxInf.getIntrmyAgt1() != null) {
					cdtTrfTxInfMap.put("IntrmyAgt1", RequestMapperUtil.buildBranchFinancialInst(cdtTrfTxInf.getIntrmyAgt1()));
				}
				// 56
				// IntermediaryInstitution
				if (cdtTrfTxInf.getIntrmyAgt1Acct() != null) {
					cdtTrfTxInfMap.put("IntrmyAgt1Acct", RequestMapperUtil.buildCashAccount24(cdtTrfTxInf.getIntrmyAgt1Acct()));
				}

				// senderToReciever information
				if (cdtTrfTxInf.getInstrForCdtrAgt() != null && !cdtTrfTxInf.getInstrForCdtrAgt().get(0).isEmpty()) {
					cdtTrfTxInfMap.put("InstrForCdtrAgt", RequestMapperUtil.buildInstrForCdtrAgt(cdtTrfTxInf.getInstrForCdtrAgt()));
				}

				if (cdtTrfTxInf.getRmtInf() != null) {
					cdtTrfTxInfMap.put("RmtInf", RequestMapperUtil.buildRemittanceInformation(cdtTrfTxInf.getRmtInf()));
				}

			}
			cdtTrfTxInfSuperList.add(cdtTrfTxInfMap);
		}
		return cdtTrfTxInfSuperList;
	}
}
