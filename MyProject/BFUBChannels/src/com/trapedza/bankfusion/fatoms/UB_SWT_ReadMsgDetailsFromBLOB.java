package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.BankFusionMessages;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.common.runtime.toolkit.expression.function.RoundToScale;
import com.misys.ub.datacenter.DataCenterCommonUtils;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.misys.ub.swift.GetExchangeRateSwiftRemittance;
import com.misys.ub.swift.RemittanceIdGenerator;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOPersonDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_MessageHeader;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_SWTMessageDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_RemittanceTable;
import com.trapedza.bankfusion.boundary.outward.BankFusionIOSupport;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.FinderMethods;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_ReadMsgDetailsFromBLOB;

import bf.com.misys.cbs.services.ReadModuleConfigurationRq;
import bf.com.misys.cbs.services.ReadModuleConfigurationRs;
import bf.com.misys.cbs.types.ModuleKeyRq;
import bf.com.misys.ub.types.interfaces.Instruction;
import bf.com.misys.ub.types.interfaces.SenderCharge;
import bf.com.misys.ub.types.interfaces.SwiftMT103;
import bf.com.misys.ub.types.interfaces.SwiftMT200;
import bf.com.misys.ub.types.interfaces.SwiftMT202;
import bf.com.misys.ub.types.interfaces.SwiftMT205;
import bf.com.misys.ub.types.interfaces.Ub_MT103;
import bf.com.misys.ub.types.interfaces.Ub_MT200;
import bf.com.misys.ub.types.interfaces.Ub_MT202;
import bf.com.misys.ub.types.interfaces.Ub_MT205;
import bf.com.misys.ub.types.remittanceprocess.BANKTOBANKINFO;
import bf.com.misys.ub.types.remittanceprocess.BENEFICIARYCUSTOMERINFO;
import bf.com.misys.ub.types.remittanceprocess.BENEFICIARYINSTDETIALS;
import bf.com.misys.ub.types.remittanceprocess.CHARGERELATEDINFO;
import bf.com.misys.ub.types.remittanceprocess.CREDITORDTL;
import bf.com.misys.ub.types.remittanceprocess.DEBITORDTL;
import bf.com.misys.ub.types.remittanceprocess.INTERMEDIARYDETAILS;
import bf.com.misys.ub.types.remittanceprocess.KYCDETAILS;
import bf.com.misys.ub.types.remittanceprocess.ORDERINGICUSTINFO;
import bf.com.misys.ub.types.remittanceprocess.ORDERINGINSTITUTIONDTL;
import bf.com.misys.ub.types.remittanceprocess.PAYTOPARTYDETAILS;
import bf.com.misys.ub.types.remittanceprocess.RemittanceINFO;
import bf.com.misys.ub.types.remittanceprocess.SENDERTORECEIVERINFO;
import bf.com.misys.ub.types.remittanceprocess.TERMSCONDITIONSINFO;
import bf.com.misys.ub.types.remittanceprocess.TRANSACTIONDETAISINFO;
import bf.com.misys.ub.types.remittanceprocess.UB_SWT_RemittanceProcessRq;

public class UB_SWT_ReadMsgDetailsFromBLOB extends AbstractUB_SWT_ReadMsgDetailsFromBLOB {
	static final String query3 = " WHERE " + IBOUB_SWT_RemittanceTable.UBMESSAGEREFID + " = ? ";
	
	final static String READ_MODULE_CONFIGURATION = "CB_CMN_ReadModuleConfiguration_SRV";

	private static String SWTCUSTOMERWHERECLAUSE = " WHERE " + IBOSwtCustomerDetail.BICCODE + " = ?";

	BankFusionEnvironment env = null;
	
	IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
	
	static String flag = "";
	
	private transient final static Log logger = LogFactory.getLog(UB_SWT_ReadMsgDetailsFromBLOB.class.getClass());

	public UB_SWT_ReadMsgDetailsFromBLOB(BankFusionEnvironment env) {
		super(env);
	}

	public UB_SWT_ReadMsgDetailsFromBLOB() {
	}

    @Override
	public void process(BankFusionEnvironment env) throws BankFusionException {

		ArrayList param = new ArrayList();
		param.add(getF_IN_MessageID());

		IBOUB_INF_MessageHeader messageHeader = (IBOUB_INF_MessageHeader) factory.findByPrimaryKey(IBOUB_INF_MessageHeader.BONAME, getF_IN_MessageID(), true);
		
		if (PaymentSwiftConstants.INWARD.equals(getF_IN_directionFromScreen())) {
			IBOUB_INF_SWTMessageDetail 	msgDtl= (IBOUB_INF_SWTMessageDetail)factory.findByPrimaryKey(IBOUB_INF_SWTMessageDetail.BONAME, getF_IN_MessageID(), true);
			if (null != msgDtl) {
				String messageType = messageHeader.getF_MESSAGETYPE();
				String dfs = messageHeader.getF_MESSAGEID2();
				if (messageType != null && messageType.equalsIgnoreCase("MT103")) {
					Ub_MT103 msg = (Ub_MT103) BankFusionIOSupport.convertFromBytes(msgDtl.getF_MESSAGEOBJECT());
					UB_SWT_RemittanceProcessRq RemittanceMsgOUT = MT103FieldMapping(msg, messageHeader, env);
					RemittanceMsgOUT.setREMITTANCE_ID(getF_IN_MessageID());
					setF_OUT_RemittanceProObject(RemittanceMsgOUT);
				}
				if (messageType != null && messageType.equalsIgnoreCase("MT202")) {
					Ub_MT202 msg = (Ub_MT202) BankFusionIOSupport.convertFromBytes(msgDtl.getF_MESSAGEOBJECT());
					UB_SWT_RemittanceProcessRq RemittanceMsgOUT = MT202FieldMapping(msg, messageHeader, env);
					RemittanceMsgOUT.setREMITTANCE_ID(getF_IN_MessageID());
					setF_OUT_RemittanceProObject(RemittanceMsgOUT);
				}
				if (messageType != null && messageType.equalsIgnoreCase("MT205")) {
					Ub_MT205 msg = (Ub_MT205) BankFusionIOSupport.convertFromBytes(msgDtl.getF_MESSAGEOBJECT());
					UB_SWT_RemittanceProcessRq RemittanceMsgOUT = MT205FieldMapping(msg, messageHeader, env);
					RemittanceMsgOUT.setREMITTANCE_ID(getF_IN_MessageID());
					setF_OUT_RemittanceProObject(RemittanceMsgOUT);
				}
				if (messageType != null && messageType.equalsIgnoreCase("MT200")) {
					Ub_MT200 msg = (Ub_MT200) BankFusionIOSupport.convertFromBytes(msgDtl.getF_MESSAGEOBJECT());
					UB_SWT_RemittanceProcessRq RemittanceMsgOUT = MT200FieldMapping(msg, messageHeader, env);
					RemittanceMsgOUT.setREMITTANCE_ID(getF_IN_MessageID());
					setF_OUT_RemittanceProObject(RemittanceMsgOUT);
				}
			} else {
				EventsHelper.handleEvent(PaymentSwiftConstants.E_NO_DETAILS_FOUND_CBS, new Object[] {}, null, env);
			}

		} else {
			List details = factory.findByQuery(IBOUB_SWT_RemittanceTable.BONAME, query3, param, null);
			if (details.size() > 0) {
				IBOUB_SWT_RemittanceTable remittanceDetail = (IBOUB_SWT_RemittanceTable) details.get(0);
				UB_SWT_RemittanceProcessRq remittanceMsgOUT = getOutwardRemittanceForScreen(remittanceDetail,
						messageHeader);
				remittanceMsgOUT.setREMITTANCE_ID(getF_IN_MessageID());
				setF_OUT_RemittanceProObject(remittanceMsgOUT);
			} else {
				EventsHelper.handleEvent(PaymentSwiftConstants.E_NO_DETAILS_FOUND_CBS, new Object[] {}, null, env);
			}
		}
	}

	private UB_SWT_RemittanceProcessRq MT103FieldMapping(Ub_MT103 MT103, IBOUB_INF_MessageHeader MsgHeader,
			BankFusionEnvironment env) {
		SwiftMT103 mt103Details = MT103.getDetails();
		UB_SWT_RemittanceProcessRq remittanceMsgDetails = new UB_SWT_RemittanceProcessRq();

		BENEFICIARYINSTDETIALS beneficiaryInstDetails = new BENEFICIARYINSTDETIALS();
		remittanceMsgDetails.setBENEFICIARYINSTDETIALS(beneficiaryInstDetails);
		BENEFICIARYCUSTOMERINFO beneficiaryCustomerInfo = new BENEFICIARYCUSTOMERINFO();
		remittanceMsgDetails.setBENEFICIARYCUSTOMERINFO(beneficiaryCustomerInfo);
		ORDERINGICUSTINFO orderingCustInfo = new ORDERINGICUSTINFO();
		remittanceMsgDetails.setORDERINGICUSTINFO(orderingCustInfo);
		ORDERINGINSTITUTIONDTL orderingInstDetail = new ORDERINGINSTITUTIONDTL();
		remittanceMsgDetails.setORDERINGINSTITUTIONDTL(orderingInstDetail);
		PAYTOPARTYDETAILS payToPartyDetails = new PAYTOPARTYDETAILS();
		remittanceMsgDetails.setPAYTOPARTYDETAILS(payToPartyDetails);
		INTERMEDIARYDETAILS intermediaryDetails = new INTERMEDIARYDETAILS();
		remittanceMsgDetails.setINTERMEDIARYDETAILS(intermediaryDetails);
		SENDERTORECEIVERINFO senderToReceiverInfo = new SENDERTORECEIVERINFO();
		remittanceMsgDetails.setSENDERTORECEIVERINFO(senderToReceiverInfo);
		BANKTOBANKINFO b2bInfo = new BANKTOBANKINFO();
		remittanceMsgDetails.setBANKTOBANKINFO(b2bInfo);
		TERMSCONDITIONSINFO termConditionsInfo = new TERMSCONDITIONSINFO();
		remittanceMsgDetails.setTERMSCONDITIONSINFO(termConditionsInfo);
		KYCDETAILS kycDetails = new KYCDETAILS();
		remittanceMsgDetails.setKYCDETAILS(kycDetails);
		RemittanceINFO remittanceInfo = new RemittanceINFO();
		remittanceMsgDetails.setRemittanceINFO(remittanceInfo);
		CHARGERELATEDINFO chargeRelatedInfo = new CHARGERELATEDINFO();
		remittanceMsgDetails.setCHARGERELATEDINFO(chargeRelatedInfo);
		remittanceMsgDetails.setDIRECTION(PaymentSwiftConstants.INWARD);
		BigDecimal intructedAmount = (null != mt103Details.getInstructedAmount())
				? new BigDecimal(mt103Details.getInstructedAmount())
				: BigDecimal.ZERO;
		String instructedCcy = !StringUtils.isBlank(mt103Details.getInstructedCurrency())
				? mt103Details.getInstructedCurrency()
				: StringUtils.EMPTY;
		if (BigDecimal.ZERO.equals(intructedAmount)) {
			remittanceMsgDetails.setInstructedAmount(intructedAmount);			
		} else {
			remittanceMsgDetails.setInstructedAmount(RoundToScale.run(intructedAmount, instructedCcy));
		}
		remittanceMsgDetails.setInstructedAmountCcy(instructedCcy);
		remittanceMsgDetails.setEnd2EndTxnRef(mt103Details.getEnd2EndTxnRef());

		if (mt103Details.getSenderToReceiverInfo() != null) {
			SENDERTORECEIVERINFO sdrToRcvrInfo = setSenderToRecInfo(mt103Details.getSenderToReceiverInfo());
			try {
				b2bInfo.setBANKTOBANKINFO1(sdrToRcvrInfo.getSENDERTORECEIVERINFO1());
				b2bInfo.setBANKTOBANKINFO2(sdrToRcvrInfo.getSENDERTORECEIVERINFO2());
				b2bInfo.setBANKTOBANKINFO3(sdrToRcvrInfo.getSENDERTORECEIVERINFO3());
				b2bInfo.setBANKTOBANKINFO4(sdrToRcvrInfo.getSENDERTORECEIVERINFO4());
				b2bInfo.setBANKTOBANKINFO5(sdrToRcvrInfo.getSENDERTORECEIVERINFO5());
				b2bInfo.setBANKTOBANKINFO6(sdrToRcvrInfo.getSENDERTORECEIVERINFO6());
			} catch (IndexOutOfBoundsException E) {
				logger.error(ExceptionUtil.getExceptionAsString(E));
			}

		}
		b2bInfo.setBANKOPERATIONCODE(mt103Details.getBankOperationCode());
		remittanceMsgDetails.setSENDERTORECEIVERINFO(senderToReceiverInfo);

		Instruction instrc = mt103Details.getInstruction();
		if (instrc != null && instrc.getInstructionCodeCount() > 0) {
			b2bInfo.setBANKINSTRUCTIONCODE(instrc.getInstructionCode(0));
		}
		remittanceMsgDetails.setBANKTOBANKINFO(b2bInfo);
		remittanceMsgDetails.setMESSAGENUMBER(MsgHeader.getF_MESSAGEID2()); // senders Ref is using
																			// id

		remittanceInfo.setCHARGECODE(mt103Details.getDetailsOfCharges());
		remittanceInfo.setTRANSACTIONTYPECODE(mt103Details.getTransactionTypeCode());
		if (mt103Details.getRemittanceInfo() != null) {
			String temp[] = new String[4];
			temp = mt103Details.getRemittanceInfo().split("[$]");
			try {
				remittanceInfo.setREMITTANCEINFO1(temp[0]);
				remittanceInfo.setREMITTANCEINFO2(temp[1]);
				remittanceInfo.setREMITTANCEINFO3(temp[2]);
				remittanceInfo.setREMITTANCEINFO4(temp[3]);
			} catch (IndexOutOfBoundsException E) {
				logger.error(ExceptionUtil.getExceptionAsString(E));
			}
		}
		// charge detail
		remittanceInfo.setChargeDetailAmount(getSenderChargeForDisplay(mt103Details.getCharges()));
		// remittance description
		remittanceInfo.setREMITTANCEDESCRIPTION(getDescriptionForFailedMessages(MsgHeader, env));
		remittanceMsgDetails.setRemittanceINFO(remittanceInfo);

		TRANSACTIONDETAISINFO tnxInfo = new TRANSACTIONDETAISINFO();
		String ValueDate = mt103Details.getTdValueDate();
		try {
			tnxInfo.setDATEOFPROCESSING(getFormattedDate(ValueDate));

		} catch (Exception E) {
			logger.error(ExceptionUtil.getExceptionAsString(E));
		}
		tnxInfo.setCURRENCY(mt103Details.getTdCurrencyCode());
		tnxInfo.setEXCHANGERATEFORINCOMING(getBigDecimalfromString(mt103Details.getExchangeRate()));
		tnxInfo.setEXCHANGERATEFOROUTGOING(getBigDecimalfromString(mt103Details.getExchangeRate()));
		tnxInfo.setNARRATION(mt103Details.getSendersReference());
		tnxInfo.setTRANSACTIONREFERENCE(mt103Details.getSendersReference());
		String transactionType = getSwifttransactionDesc("103");
		tnxInfo.setTRANSACTIONTYPE(transactionType);
		remittanceMsgDetails.setChargeCurrency(mt103Details.getTdCurrencyCode());

		if (mt103Details.getBeneficiaryCustomer() != null)
			remittanceMsgDetails.setBENEFICIARYCUSTOMERINFO(
					setBeneCustDetails(mt103Details.getBeneficiaryCustOption(), mt103Details.getBeneficiaryCustomer()));
		
		

		String creditAccount = CommonConstants.EMPTY_STRING;
		flag = "C";
		creditAccount = getAccountNumberFromText(mt103Details.getBeneficiaryCustomer(),
				mt103Details.getBeneficiaryCustOption(), mt103Details.getTdCurrencyCode());
		CREDITORDTL crdTls = new CREDITORDTL();
		crdTls.setCREDITACCOUNTID(creditAccount);
		
		String debitAccount = CommonConstants.EMPTY_STRING;
		if (mt103Details.getThirdReimbursementInstitution() != null) {
			flag = "D";
			debitAccount = getAccountNumberFromText(mt103Details.getThirdReimbursementInstitution(),
					mt103Details.getThirdReimbursementInstOption(), mt103Details.getTdCurrencyCode());
		}

		if (debitAccount.equals(CommonConstants.EMPTY_STRING) && mt103Details.getReceiversCorrespondent() != null) {
			flag = "D";
			debitAccount = getAccountNumberFromText(mt103Details.getReceiversCorrespondent(),
					mt103Details.getReceiversCorrespOption(), mt103Details.getTdCurrencyCode());
		}

		if (debitAccount.equals(CommonConstants.EMPTY_STRING) && mt103Details.getSendersCorrespondent() != null) {
			flag = "D";
			debitAccount = getAccountNumberFromText53(mt103Details.getSendersCorrespondent(),
					mt103Details.getSendersCorrespOption(), mt103Details.getTdCurrencyCode());
		}

		if (debitAccount.equals(CommonConstants.EMPTY_STRING) && mt103Details.getSender() != null) {
			flag = "D";
			debitAccount = getAccountNumberFromBIC(mt103Details.getSender(), mt103Details.getTdCurrencyCode());
		}
		
		String invalidAccountNumber = debitAccount;
		setF_OUT_invalidAccountNumber(invalidAccountNumber);
		
		boolean isDebitAccountUpdated = false;
		if(StringUtils.isEmpty(debitAccount) || (!isAccountExist(debitAccount)))
		{
			debitAccount = CommonConstants.EMPTY_STRING;
			isDebitAccountUpdated = true;
			setF_OUT_isDebitAccountUpdated(isDebitAccountUpdated);
			
			
			//EventsHelper.handleEvent(PaymentSwiftConstants.W_INVALID_DEBIT_ACCOUNT, new Object[] {}, null, env);
		}

        if (StringUtils.isEmpty(debitAccount) && isDefaultNostroConfigured()) {
        
        	
            debitAccount = getNostroAccountFromModuleConfig(mt103Details.getTdCurrencyCode(),
                    BankFusionThreadLocal.getUserSession().getBranchSortCode());
        }

		BigDecimal debitAmt = BigDecimal.ZERO;
		DEBITORDTL debtr = new DEBITORDTL();
		debtr.setDEBITACCOUNTID(debitAccount);
		debitAmt = getBigDecimalfromString(mt103Details.getTdAmount());
		debtr.setDEBITAMOUNT(debitAmt);
		
		// Charge Calculation begin
		CHARGERELATEDINFO chargeDetail = setChargeDetails(mt103Details, creditAccount, debitAccount);
		BigDecimal creditAmt = BigDecimal.ZERO;

		if (mt103Details.getDetailsOfCharges().equals("OUR")
				&& (mt103Details.getReceiversCharges() != null && !mt103Details.getReceiversCharges().equals(""))) {
			creditAmt = new BigDecimal(mt103Details.getTdAmount()).subtract(chargeDetail.getChargeAmount());
			tnxInfo.setAPPLIEDCHARGES(chargeDetail.getChargeAmount());
		}
		if (mt103Details.getDetailsOfCharges().equals("OUR")
				&& (mt103Details.getReceiversCharges() == null || mt103Details.getReceiversCharges().equals(""))) {
			creditAmt = new BigDecimal(mt103Details.getTdAmount());
			tnxInfo.setAPPLIEDCHARGES(BigDecimal.ZERO);
			chargeDetail.setChargeAmount(BigDecimal.ZERO);
			chargeDetail.setInterBankSettledAmount(creditAmt);
			chargeDetail.setReceiversCharge(BigDecimal.ZERO);
			chargeDetail.setSendersCharge(BigDecimal.ZERO);
			chargeDetail.setTaxAmount(BigDecimal.ZERO);
			chargeDetail.setTransactionAmount(creditAmt);
		}
		if (mt103Details.getDetailsOfCharges().equals("SHA") || mt103Details.getDetailsOfCharges().equals("BEN")) {
			creditAmt = new BigDecimal(mt103Details.getTdAmount()).subtract(chargeDetail.getChargeAmount());
			tnxInfo.setAPPLIEDCHARGES(chargeDetail.getChargeAmount());
		}

		crdTls.setCREDITAMOUNT(creditAmt);
		remittanceMsgDetails.setTRANSACTIONDETAISINFO(tnxInfo);
		remittanceMsgDetails.setCHARGERELATEDINFO(chargeDetail);

		// charge calculation ends

		if (!creditAccount.equalsIgnoreCase(CommonConstants.EMPTY_STRING)) {
			remittanceMsgDetails.setCUSTOMERNAME(getCustomerShortName(creditAccount));
		}
		

		String creditAccountCurr = CommonConstants.EMPTY_STRING;
		String debitAccountCurr = CommonConstants.EMPTY_STRING;
		GetExchangeRateSwiftRemittance exchangerate = new GetExchangeRateSwiftRemittance();
		BigDecimal expectedCreditAmt = BigDecimal.ZERO;
		BigDecimal creditExchangeRate = BigDecimal.ZERO;
		String creditExchRateType = CommonConstants.EMPTY_STRING;
		String debitExchRateType = CommonConstants.EMPTY_STRING;

		BigDecimal expectedDebitAmt = BigDecimal.ZERO;
		BigDecimal debitExchangeRate = BigDecimal.ZERO;
		
		if (null != creditAccount && !creditAccount.equals(CommonConstants.EMPTY_STRING)) {
			creditAccountCurr = getAccountCurrency(creditAccount);
			Map<String, Object> exchangeRateMap = exchangerate.getExchangeRate(mt103Details.getTdCurrencyCode(),
					creditAccountCurr, creditAmt, "N");
			expectedCreditAmt = (BigDecimal) exchangeRateMap.get("convertedAmount");
			creditExchangeRate = (BigDecimal) exchangeRateMap.get("exchangeRate");
			creditExchRateType = (String) exchangeRateMap.get("exchangeRateType");
		}
		
		
		if (null != debitAccount && !debitAccount.equals(CommonConstants.EMPTY_STRING)) {
			
			if(isAccountExist(debitAccount)) 
			{
				
			debitAccountCurr = getAccountCurrency(debitAccount);
			Map<String, Object> exchangeRateMap = exchangerate.getExchangeRate(mt103Details.getTdCurrencyCode(),
					debitAccountCurr, debitAmt, "N");
			expectedDebitAmt = (BigDecimal) exchangeRateMap.get("convertedAmount");
			debitExchangeRate = (BigDecimal) exchangeRateMap.get("exchangeRate");
			debitExchRateType = (String) exchangeRateMap.get("exchangeRateType");
			
		    }
		
			else
			{
				
				debitAccountCurr = "EUR";
				//EventsHelper.handleEvent(PaymentSwiftConstants.W_INVALID_DEBIT_ACCOUNT, new Object[] {}, null, env);
				
			}

		
		}
		
		
		crdTls.setEXPECTEDCREDITAMOUNT(expectedCreditAmt);
		remittanceMsgDetails.setCREDITORDTL(crdTls);
		debtr.setEXPECTEDDEBITAMOUNT(expectedDebitAmt);
		remittanceMsgDetails.setDEBITORDTL(debtr);

		remittanceMsgDetails.setExchangeRateTypeOUT(creditExchRateType);
		remittanceMsgDetails.setExchangeRateTypeIN(creditExchRateType);

		tnxInfo.setEXCHANGERATEFORINCOMING(creditExchangeRate);
		tnxInfo.setEXCHANGERATEFOROUTGOING(creditExchangeRate);

		remittanceMsgDetails.setTRANSACTIONDETAISINFO(tnxInfo);
		remittanceMsgDetails.setCrAccountCurrency(creditAccountCurr);
		remittanceMsgDetails.setDrAccountCurrency(debitAccountCurr);
		remittanceMsgDetails.setExpctCrAmountCurrency(creditAccountCurr);
		remittanceMsgDetails.setExpctDrAmountCurrency(debitAccountCurr);
		remittanceMsgDetails.setCrAmountCurrency(mt103Details.getTdCurrencyCode());
		remittanceMsgDetails.setDrAmountCurrency(mt103Details.getTdCurrencyCode());

		if (mt103Details.getBeneficiaryCustomer() != null) {
			beneficiaryCustomerInfo = setBeneCustDetails(mt103Details.getBeneficiaryCustOption(),
					mt103Details.getBeneficiaryCustomer());
		}
		remittanceMsgDetails.setBENEFICIARYCUSTOMERINFO(beneficiaryCustomerInfo);
		if (mt103Details.getAccountWithInstitution() != null) {
			beneficiaryInstDetails = setBeneInstDetails(mt103Details.getAccountWithInstOption(),
					mt103Details.getAccountWithInstitution());
		}
		remittanceMsgDetails.setBENEFICIARYINSTDETIALS(beneficiaryInstDetails);
		if (mt103Details.getOrderingCustomer() != null) {
			orderingCustInfo = setOrdCustDetails(mt103Details.getOrderingCustomerOption(),
					mt103Details.getOrderingCustomer());
		}
		remittanceMsgDetails.setORDERINGICUSTINFO(orderingCustInfo);

		if (mt103Details.getOrderingInstitution() != null) {
			orderingInstDetail = setOrdInstDetails(mt103Details.getOrderInstitutionOption(),
					mt103Details.getOrderingInstitution());
		}
		remittanceMsgDetails.setORDERINGINSTITUTIONDTL(orderingInstDetail);
		if (mt103Details.getSender() != null) {
			if (isBICExistInSwtCutDtls(mt103Details.getSender())) {
				if (mt103Details.getSendersCorrespondent() != null) {
					payToPartyDetails = setPayToDetails(mt103Details.getSendersCorrespOption(),
							mt103Details.getSendersCorrespondent());
				}
				if (mt103Details.getReceiversCorrespondent() != null) {
					intermediaryDetails = setIntrmediaryDetails(mt103Details.getReceiversCorrespOption(),
							mt103Details.getReceiversCorrespondent());
				} else {
					if (mt103Details.getIntermediaryInstitution() != null) {
						intermediaryDetails = setIntrmediaryDetails(mt103Details.getIntermediaryInstOption(),
								mt103Details.getIntermediaryInstitution());
					}
				}
			} else {
				if (mt103Details.getReceiversCorrespondent() != null) {
					payToPartyDetails = setPayToDetails(mt103Details.getReceiversCorrespOption(),
							mt103Details.getReceiversCorrespondent());
				}
				if (mt103Details.getSendersCorrespondent() != null) {
					intermediaryDetails = setIntrmediaryDetails(mt103Details.getSendersCorrespOption(),
							mt103Details.getSendersCorrespondent());
				} else {
					if (mt103Details.getIntermediaryInstitution() != null) {
						intermediaryDetails = setIntrmediaryDetails(mt103Details.getIntermediaryInstOption(),
								mt103Details.getIntermediaryInstitution());
					}
				}
			}
		}
		remittanceMsgDetails.setPAYTOPARTYDETAILS(payToPartyDetails);
		remittanceMsgDetails.setINTERMEDIARYDETAILS(intermediaryDetails);
		remittanceMsgDetails.setKYCDETAILS(kycDetails);
		remittanceMsgDetails.setTERMSCONDITIONSINFO(termConditionsInfo);
		RemittanceIdGenerator remitIdGenerator = new RemittanceIdGenerator();
		remittanceMsgDetails.setREMITTANCEIDPK(remitIdGenerator.getRemittanceId(mt103Details.getTdCurrencyCode()));

		return remittanceMsgDetails;

	}

	private UB_SWT_RemittanceProcessRq MT200FieldMapping(Ub_MT200 MT200, IBOUB_INF_MessageHeader MsgHeader,
			BankFusionEnvironment env) {

		SwiftMT200 mt200Details = MT200.getDetails();
		UB_SWT_RemittanceProcessRq remittanceMsgDetails = new UB_SWT_RemittanceProcessRq();//
		BENEFICIARYINSTDETIALS beneficiaryInstDetails = new BENEFICIARYINSTDETIALS();
		remittanceMsgDetails.setBENEFICIARYINSTDETIALS(beneficiaryInstDetails);
		BENEFICIARYCUSTOMERINFO beneficiaryCustInfo = new BENEFICIARYCUSTOMERINFO();
		remittanceMsgDetails.setBENEFICIARYCUSTOMERINFO(beneficiaryCustInfo);
		ORDERINGICUSTINFO orderingCustInfo = new ORDERINGICUSTINFO();
		remittanceMsgDetails.setORDERINGICUSTINFO(orderingCustInfo);
		ORDERINGINSTITUTIONDTL orderingInstDetails = new ORDERINGINSTITUTIONDTL();
		remittanceMsgDetails.setORDERINGINSTITUTIONDTL(orderingInstDetails);
		PAYTOPARTYDETAILS payToPartyDetails = new PAYTOPARTYDETAILS();
		remittanceMsgDetails.setPAYTOPARTYDETAILS(payToPartyDetails);
		INTERMEDIARYDETAILS intermediaryDetails = new INTERMEDIARYDETAILS();
		remittanceMsgDetails.setINTERMEDIARYDETAILS(intermediaryDetails);
		SENDERTORECEIVERINFO sendertoreceiverinfo = new SENDERTORECEIVERINFO();
		remittanceMsgDetails.setSENDERTORECEIVERINFO(sendertoreceiverinfo);
		BANKTOBANKINFO banktobankinfo = new BANKTOBANKINFO();
		remittanceMsgDetails.setBANKTOBANKINFO(banktobankinfo);
		TERMSCONDITIONSINFO termsconditionsinfo = new TERMSCONDITIONSINFO();
		remittanceMsgDetails.setTERMSCONDITIONSINFO(termsconditionsinfo);
		KYCDETAILS kycDetails = new KYCDETAILS();
		remittanceMsgDetails.setKYCDETAILS(kycDetails);
		RemittanceINFO remittanceInfo = new RemittanceINFO();
		remittanceInfo.setREMITTANCEDESCRIPTION(getDescriptionForFailedMessages(MsgHeader, env));
		remittanceMsgDetails.setRemittanceINFO(remittanceInfo);
		CHARGERELATEDINFO chargeRelatedInfo = new CHARGERELATEDINFO();
		remittanceMsgDetails.setCHARGERELATEDINFO(chargeRelatedInfo);

		remittanceMsgDetails.setMESSAGENUMBER(MsgHeader.getF_MESSAGEID2());
		remittanceMsgDetails.setDIRECTION(PaymentSwiftConstants.INWARD);

		TRANSACTIONDETAISINFO tnxInfo = new TRANSACTIONDETAISINFO();
		tnxInfo.setCURRENCY(mt200Details.getTdcurrencyCode());
		tnxInfo.setDATEOFPROCESSING(getFormattedDate(mt200Details.getTdvalueDate()));
		tnxInfo.setTRANSACTIONREFERENCE(mt200Details.getTransactionReferenceNumber());
		tnxInfo.setNARRATION(mt200Details.getTransactionReferenceNumber());
		tnxInfo.setEXCHANGERATEFORINCOMING(new BigDecimal(1));
		tnxInfo.setEXCHANGERATEFOROUTGOING(new BigDecimal(1));
		String transactionType = getSwifttransactionDesc("200");
		tnxInfo.setTRANSACTIONTYPE(transactionType);
		remittanceMsgDetails.setTRANSACTIONDETAISINFO(tnxInfo);
		remittanceMsgDetails.setChargeCurrency("");

		String creditAccount = CommonConstants.EMPTY_STRING;
		if (mt200Details.getAccountWithInstitution() != null) {
			flag = CommonConstants.EMPTY_STRING;
			creditAccount = getAccountNumberFromText(mt200Details.getAccountWithInstitution(),
					mt200Details.getAccountWithInstOption(), mt200Details.getTdcurrencyCode());
		}
		if (!creditAccount.equalsIgnoreCase(CommonConstants.EMPTY_STRING)) {
			remittanceMsgDetails.setCUSTOMERNAME(getCustomerShortName(creditAccount));
		}
		CREDITORDTL crdtdtl = new CREDITORDTL();
		crdtdtl.setCREDITACCOUNTID(creditAccount);
		BigDecimal creditAmt = getBigDecimalfromString(mt200Details.getTdamount());
		crdtdtl.setCREDITAMOUNT(creditAmt);
		crdtdtl.setEXPECTEDCREDITAMOUNT(getBigDecimalfromString(mt200Details.getTdamount()));

		String debitAccount = CommonConstants.EMPTY_STRING;
		if (mt200Details.getSendersCorrespondent() != null) {
			flag = "D";
			debitAccount = getAccountNumberFromText(mt200Details.getSendersCorrespondent(),
					mt200Details.getSendersCorresOption(), mt200Details.getTdcurrencyCode());
		} else if (mt200Details.getSender() != null) {
			flag = "D";
			debitAccount = getAccountNumberFromBIC(mt200Details.getSender(), mt200Details.getTdcurrencyCode());
		}
		DEBITORDTL debtr = new DEBITORDTL();
		debtr.setDEBITACCOUNTID(debitAccount);
		BigDecimal debitAmt = getBigDecimalfromString(mt200Details.getTdamount());
		debtr.setDEBITAMOUNT(debitAmt);// Field 59 beneficiary customer

		String creditAccountCurr = CommonConstants.EMPTY_STRING;
		String debitAccountCurr = CommonConstants.EMPTY_STRING;
		GetExchangeRateSwiftRemittance exchangerate = new GetExchangeRateSwiftRemittance();
		BigDecimal expectedCreditAmt = BigDecimal.ZERO;
		BigDecimal creditExchangeRate = BigDecimal.ZERO;
		String creditExchRateType = CommonConstants.EMPTY_STRING;
		String debitExchRateType = CommonConstants.EMPTY_STRING;

		BigDecimal expectedDebitAmt = BigDecimal.ZERO;
		BigDecimal debitExchangeRate = BigDecimal.ZERO;

		if (null != creditAccount && !creditAccount.equals(CommonConstants.EMPTY_STRING)) {
			creditAccountCurr = getAccountCurrency(creditAccount);
			Map<String, Object> exchangeRateMap = exchangerate.getExchangeRate(mt200Details.getTdcurrencyCode(),
					creditAccountCurr, creditAmt, "N");
			expectedCreditAmt = (BigDecimal) exchangeRateMap.get("convertedAmount");
			creditExchangeRate = (BigDecimal) exchangeRateMap.get("exchangeRate");
			creditExchRateType = (String) exchangeRateMap.get("exchangeRateType");
		}

		if (null != debitAccount && !debitAccount.equals("")) {
			debitAccountCurr = getAccountCurrency(debitAccount);
			Map<String, Object> exchangeRateMap = exchangerate.getExchangeRate(mt200Details.getTdcurrencyCode(),
					debitAccountCurr, debitAmt, "N");
			expectedDebitAmt = (BigDecimal) exchangeRateMap.get("convertedAmount");
			debitExchangeRate = (BigDecimal) exchangeRateMap.get("exchangeRate");
			debitExchRateType = (String) exchangeRateMap.get("exchangeRateType");
		}

		crdtdtl.setEXPECTEDCREDITAMOUNT(expectedCreditAmt);
		remittanceMsgDetails.setCREDITORDTL(crdtdtl);
		debtr.setEXPECTEDDEBITAMOUNT(expectedDebitAmt);
		remittanceMsgDetails.setDEBITORDTL(debtr);

		remittanceMsgDetails.setExchangeRateTypeOUT(debitExchRateType);
		remittanceMsgDetails.setExchangeRateTypeIN(creditExchRateType);

		tnxInfo.setEXCHANGERATEFORINCOMING(creditExchangeRate);
		tnxInfo.setEXCHANGERATEFOROUTGOING(creditExchangeRate);

		remittanceMsgDetails.setTRANSACTIONDETAISINFO(tnxInfo);
		remittanceMsgDetails.setCrAccountCurrency(creditAccountCurr);
		remittanceMsgDetails.setDrAccountCurrency(debitAccountCurr);
		remittanceMsgDetails.setExpctCrAmountCurrency(creditAccountCurr);
		remittanceMsgDetails.setExpctDrAmountCurrency(debitAccountCurr);
		remittanceMsgDetails.setCrAmountCurrency(mt200Details.getTdcurrencyCode());
		remittanceMsgDetails.setDrAmountCurrency(mt200Details.getTdcurrencyCode());

		if (mt200Details.getIntermediary() != null)
			remittanceMsgDetails.setINTERMEDIARYDETAILS(
					setIntrmediaryDetails(mt200Details.getIntermediaryOption(), mt200Details.getIntermediary()));

		if (mt200Details.getAccountWithInstitution() != null)
			remittanceMsgDetails.setBENEFICIARYINSTDETIALS(setBeneInstDetails(mt200Details.getAccountWithInstOption(),
					mt200Details.getAccountWithInstitution()));

		if (mt200Details.getSenderToReceiverInformation() != null)
			remittanceMsgDetails
					.setSENDERTORECEIVERINFO(setSenderToRecInfo(mt200Details.getSenderToReceiverInformation()));

		RemittanceIdGenerator remitIdGenerator = new RemittanceIdGenerator();
		remittanceMsgDetails.setREMITTANCEIDPK(remitIdGenerator.getRemittanceId(mt200Details.getTdcurrencyCode()));

		return remittanceMsgDetails;
	}

	private UB_SWT_RemittanceProcessRq MT202FieldMapping(Ub_MT202 MT202, IBOUB_INF_MessageHeader MsgHeader,
			BankFusionEnvironment env) {

		SwiftMT202 mt202Details = MT202.getDetails();
		UB_SWT_RemittanceProcessRq remittanceMsgDetails = new UB_SWT_RemittanceProcessRq();// field

		BENEFICIARYINSTDETIALS beneficiaryInstDetails = new BENEFICIARYINSTDETIALS();
		remittanceMsgDetails.setBENEFICIARYINSTDETIALS(beneficiaryInstDetails);
		BENEFICIARYCUSTOMERINFO beneficiaryCustInfo = new BENEFICIARYCUSTOMERINFO();
		remittanceMsgDetails.setBENEFICIARYCUSTOMERINFO(beneficiaryCustInfo);
		ORDERINGICUSTINFO orderingCustInfo = new ORDERINGICUSTINFO();
		remittanceMsgDetails.setORDERINGICUSTINFO(orderingCustInfo);
		ORDERINGINSTITUTIONDTL orderingInstDetails = new ORDERINGINSTITUTIONDTL();
		remittanceMsgDetails.setORDERINGINSTITUTIONDTL(orderingInstDetails);
		PAYTOPARTYDETAILS payToPartyDetails = new PAYTOPARTYDETAILS();
		remittanceMsgDetails.setPAYTOPARTYDETAILS(payToPartyDetails);
		INTERMEDIARYDETAILS intermediaryDetails = new INTERMEDIARYDETAILS();
		remittanceMsgDetails.setINTERMEDIARYDETAILS(intermediaryDetails);
		SENDERTORECEIVERINFO sendertoreceiverinfo = new SENDERTORECEIVERINFO();
		remittanceMsgDetails.setSENDERTORECEIVERINFO(sendertoreceiverinfo);
		BANKTOBANKINFO banktobankinfo = new BANKTOBANKINFO();
		remittanceMsgDetails.setBANKTOBANKINFO(banktobankinfo);
		TERMSCONDITIONSINFO termsconditionsinfo = new TERMSCONDITIONSINFO();
		remittanceMsgDetails.setTERMSCONDITIONSINFO(termsconditionsinfo);
		KYCDETAILS kycDetails = new KYCDETAILS();
		remittanceMsgDetails.setKYCDETAILS(kycDetails);
		RemittanceINFO remittanceInfo = new RemittanceINFO();
		remittanceInfo.setREMITTANCEDESCRIPTION(getDescriptionForFailedMessages(MsgHeader, env));
		remittanceMsgDetails.setRemittanceINFO(remittanceInfo);
		CHARGERELATEDINFO chargeRelatedInfo = new CHARGERELATEDINFO();
		remittanceMsgDetails.setCHARGERELATEDINFO(chargeRelatedInfo);

		remittanceMsgDetails.setMESSAGENUMBER(MsgHeader.getF_MESSAGEID2());// 20 field
		String RelatedRefer = mt202Details.getRelatedReference();// field 53

		remittanceMsgDetails.setDIRECTION(PaymentSwiftConstants.INWARD);

		String beneInstOpt = mt202Details.getBeneficiaryOption();
		if (mt202Details.getBeneficiary() != null) {
			BENEFICIARYINSTDETIALS BeneInsDetls = setBeneInstDetails(beneInstOpt, mt202Details.getBeneficiary());
			remittanceMsgDetails.setBENEFICIARYINSTDETIALS(BeneInsDetls);
		}
		String creditAccount = "";
		creditAccount = getAccountNumberFromText(mt202Details.getBeneficiary(), beneInstOpt,
				mt202Details.getTdCurrencyCode());
		CREDITORDTL crdtdtl = new CREDITORDTL();
		crdtdtl.setCREDITACCOUNTID(creditAccount);
		BigDecimal creditAmt = getBigDecimalfromString(mt202Details.getTdAmount());
		crdtdtl.setCREDITAMOUNT(creditAmt);
		crdtdtl.setEXPECTEDCREDITAMOUNT(getBigDecimalfromString(mt202Details.getTdAmount()));
		remittanceMsgDetails.setCREDITORDTL(crdtdtl);

		if (!creditAccount.equalsIgnoreCase("")) {
			remittanceMsgDetails.setCUSTOMERNAME(getCustomerShortName(creditAccount));
		}

		String debitAccount = "";
		if (debitAccount.isEmpty() && mt202Details.getReceiversCorrespondent() != null) {
			flag = "D";
			debitAccount = getAccountNumberFromText(mt202Details.getReceiversCorrespondent(),
					mt202Details.getReceiversCorrespondentOption(), mt202Details.getTdCurrencyCode());
		}
		if (mt202Details.getSendersCorrespondent() != null) {
			flag = "D";
			debitAccount = getAccountNumberFromText(mt202Details.getSendersCorrespondent(),
					mt202Details.getSendersCorrespondentOption(), mt202Details.getTdCurrencyCode());
		}
		if (debitAccount.isEmpty() && mt202Details.getSender() != null) {
			flag = "D";
			debitAccount = getAccountNumberFromBIC(mt202Details.getSender(), mt202Details.getTdCurrencyCode());
		}
		if (StringUtils.isEmpty(debitAccount) && isDefaultNostroConfigured()) {
            debitAccount = getNostroAccountFromModuleConfig(mt202Details.getTdCurrencyCode(),
                    BankFusionThreadLocal.getUserSession().getBranchSortCode());
        }
		

		DEBITORDTL debtr = new DEBITORDTL();
		debtr.setDEBITACCOUNTID(debitAccount);
		BigDecimal debitAmt = getBigDecimalfromString(mt202Details.getTdAmount());
		debtr.setDEBITAMOUNT(debitAmt);// Field 59 beneficiary customer
		remittanceMsgDetails.setDEBITORDTL(debtr);
		TRANSACTIONDETAISINFO tnxDtls = new TRANSACTIONDETAISINFO();
		tnxDtls.setDATEOFPROCESSING(getFormattedDate(mt202Details.getTdValueDate()));
		tnxDtls.setCURRENCY(mt202Details.getTdCurrencyCode());
		tnxDtls.setTRANSACTIONREFERENCE(mt202Details.getTransactionReferenceNumber());
		tnxDtls.setNARRATION(mt202Details.getTransactionReferenceNumber());
		String transactionType = getSwifttransactionDesc("202");
		tnxDtls.setTRANSACTIONTYPE(transactionType);

		String creditAccountCurr = CommonConstants.EMPTY_STRING;
		String debitAccountCurr = CommonConstants.EMPTY_STRING;
		GetExchangeRateSwiftRemittance exchangerate = new GetExchangeRateSwiftRemittance();
		BigDecimal expectedCreditAmt = BigDecimal.ZERO;
		BigDecimal creditExchangeRate = BigDecimal.ZERO;
		String creditExchRateType = "";
		String debitExchRateType = "";

		BigDecimal expectedDebitAmt = BigDecimal.ZERO;
		BigDecimal debitExchangeRate = BigDecimal.ZERO;
		if (null != creditAccount && !creditAccount.equals("")) {
			creditAccountCurr = getAccountCurrency(creditAccount);
			Map<String, Object> exchangeRateMap = exchangerate.getExchangeRate(mt202Details.getTdCurrencyCode(),
					creditAccountCurr, creditAmt, "N");
			expectedCreditAmt = (BigDecimal) exchangeRateMap.get("convertedAmount");
			creditExchangeRate = (BigDecimal) exchangeRateMap.get("exchangeRate");
			creditExchRateType = (String) exchangeRateMap.get("exchangeRateType");
		}

		if (null != debitAccount && !debitAccount.equals("")) {
			debitAccountCurr = getAccountCurrency(debitAccount);
			Map<String, Object> exchangeRateMap = exchangerate.getExchangeRate(mt202Details.getTdCurrencyCode(),
					debitAccountCurr, debitAmt, "N");
			expectedDebitAmt = (BigDecimal) exchangeRateMap.get("convertedAmount");
			debitExchangeRate = (BigDecimal) exchangeRateMap.get("exchangeRate");
			debitExchRateType = (String) exchangeRateMap.get("exchangeRateType");
		}
		crdtdtl.setEXPECTEDCREDITAMOUNT(expectedCreditAmt);
		remittanceMsgDetails.setCREDITORDTL(crdtdtl);
		debtr.setEXPECTEDDEBITAMOUNT(expectedDebitAmt);
		remittanceMsgDetails.setDEBITORDTL(debtr);

		remittanceMsgDetails.setExchangeRateTypeOUT(debitExchRateType);
		remittanceMsgDetails.setExchangeRateTypeIN(creditExchRateType);

		tnxDtls.setEXCHANGERATEFORINCOMING(creditExchangeRate);
		tnxDtls.setEXCHANGERATEFOROUTGOING(creditExchangeRate);

		// RemittanceMsgDetails.setTRANSACTIONDETAISINFO(tnxInfo);
		remittanceMsgDetails.setCrAccountCurrency(creditAccountCurr);
		remittanceMsgDetails.setDrAccountCurrency(debitAccountCurr);
		remittanceMsgDetails.setExpctCrAmountCurrency(creditAccountCurr);
		remittanceMsgDetails.setExpctDrAmountCurrency(debitAccountCurr);
		remittanceMsgDetails.setCrAmountCurrency(mt202Details.getTdCurrencyCode());
		remittanceMsgDetails.setDrAmountCurrency(mt202Details.getTdCurrencyCode());

		remittanceMsgDetails.setTRANSACTIONDETAISINFO(tnxDtls);
		remittanceMsgDetails.setChargeCurrency("");

		if (mt202Details.getOrderingInstitution() != null) {
			ORDERINGINSTITUTIONDTL OrdInstDtls = setOrdInstDetails(mt202Details.getOrderingInstitutionOption(),
					mt202Details.getOrderingInstitution());
			remittanceMsgDetails.setORDERINGINSTITUTIONDTL(OrdInstDtls);
		}
		if (mt202Details.getSendersCorrespondent() != null) {
			remittanceMsgDetails.setPAYTOPARTYDETAILS(setPayToDetails(mt202Details.getSendersCorrespondentOption(),
					mt202Details.getSendersCorrespondent()));
		} else if (mt202Details.getReceiversCorrespondent() != null) {
			remittanceMsgDetails.setPAYTOPARTYDETAILS(setPayToDetails(mt202Details.getReceiversCorrespondentOption(),
					mt202Details.getReceiversCorrespondent()));
		}

		if (mt202Details.getAccountWithInstitution() != null)
			remittanceMsgDetails.setINTERMEDIARYDETAILS(setIntrmediaryDetails(
					mt202Details.getAccountWithInstitutionOption(), mt202Details.getAccountWithInstitution()));
		else if (mt202Details.getIntermediary() != null)
			remittanceMsgDetails.setINTERMEDIARYDETAILS(
					setIntrmediaryDetails(mt202Details.getIntermediaryOption(), mt202Details.getIntermediary()));

		if (mt202Details.getSendertoReceiverInformation() != null)
			remittanceMsgDetails
					.setSENDERTORECEIVERINFO(setSenderToRecInfo(mt202Details.getSendertoReceiverInformation()));

		if (mt202Details.getSender() != null) {
			if (isBICExistInSwtCutDtls(mt202Details.getSender())) {
				if (mt202Details.getSendersCorrespondent() != null)
					remittanceMsgDetails.setPAYTOPARTYDETAILS(setPayToDetails(
							mt202Details.getSendersCorrespondentOption(), mt202Details.getSendersCorrespondent()));
			} else {
				if (mt202Details.getReceiversCorrespondent() != null)
					remittanceMsgDetails.setPAYTOPARTYDETAILS(setPayToDetails(
							mt202Details.getReceiversCorrespondentOption(), mt202Details.getReceiversCorrespondent()));
			}
		}
		if (mt202Details.getAccountWithInstitution() != null)
			remittanceMsgDetails.setINTERMEDIARYDETAILS(setIntrmediaryDetails(
					mt202Details.getAccountWithInstitutionOption(), mt202Details.getAccountWithInstitution()));

		RemittanceIdGenerator remitIdGenerator = new RemittanceIdGenerator();
		remittanceMsgDetails.setREMITTANCEIDPK(remitIdGenerator.getRemittanceId(mt202Details.getTdCurrencyCode()));
		remittanceMsgDetails.setEnd2EndTxnRef(mt202Details.getEnd2EndTxnRef());

		return remittanceMsgDetails;
	}

	private UB_SWT_RemittanceProcessRq MT205FieldMapping(Ub_MT205 MT205, IBOUB_INF_MessageHeader MsgHeader,
			BankFusionEnvironment env) {

		SwiftMT205 mt205Details = MT205.getDetails();
		UB_SWT_RemittanceProcessRq remittanceMsgDetails = new UB_SWT_RemittanceProcessRq();// field

		BENEFICIARYINSTDETIALS beneficiaryInstDetails = new BENEFICIARYINSTDETIALS();
		remittanceMsgDetails.setBENEFICIARYINSTDETIALS(beneficiaryInstDetails);
		BENEFICIARYCUSTOMERINFO beneficiaryCustInfo = new BENEFICIARYCUSTOMERINFO();
		remittanceMsgDetails.setBENEFICIARYCUSTOMERINFO(beneficiaryCustInfo);
		ORDERINGICUSTINFO orderingCustInfo = new ORDERINGICUSTINFO();
		remittanceMsgDetails.setORDERINGICUSTINFO(orderingCustInfo);
		PAYTOPARTYDETAILS payToPartyDetails = new PAYTOPARTYDETAILS();
		remittanceMsgDetails.setPAYTOPARTYDETAILS(payToPartyDetails);
		BANKTOBANKINFO banktobankinfo = new BANKTOBANKINFO();
		remittanceMsgDetails.setBANKTOBANKINFO(banktobankinfo);
		TERMSCONDITIONSINFO termsconditionsinfo = new TERMSCONDITIONSINFO();
		remittanceMsgDetails.setTERMSCONDITIONSINFO(termsconditionsinfo);
		KYCDETAILS kycDetails = new KYCDETAILS();
		remittanceMsgDetails.setKYCDETAILS(kycDetails);
		RemittanceINFO remittanceInfo = new RemittanceINFO();
		remittanceInfo.setREMITTANCEDESCRIPTION(getDescriptionForFailedMessages(MsgHeader, env));
		remittanceMsgDetails.setRemittanceINFO(remittanceInfo);
		CHARGERELATEDINFO chargeRelatedInfo = new CHARGERELATEDINFO();
		remittanceMsgDetails.setCHARGERELATEDINFO(chargeRelatedInfo);
		TRANSACTIONDETAISINFO tnxinfo = new TRANSACTIONDETAISINFO();
		ORDERINGINSTITUTIONDTL orderingInstDetails = new ORDERINGINSTITUTIONDTL();
		INTERMEDIARYDETAILS intermediaryDetails = new INTERMEDIARYDETAILS();
		SENDERTORECEIVERINFO senderToReceiverInfo = new SENDERTORECEIVERINFO();
		BENEFICIARYINSTDETIALS beneInstDtls = new BENEFICIARYINSTDETIALS();

		remittanceMsgDetails.setMESSAGENUMBER(MsgHeader.getF_MESSAGEID2());
		remittanceMsgDetails.setDIRECTION(PaymentSwiftConstants.INWARD);

		tnxinfo.setCURRENCY(mt205Details.getTdcurrencyCode());
		tnxinfo.setDATEOFPROCESSING(getFormattedDate(mt205Details.getTdvalueDate()));
		tnxinfo.setTRANSACTIONREFERENCE(mt205Details.getTransactionReferenceNumber());
		tnxinfo.setNARRATION(mt205Details.getTransactionReferenceNumber());
		tnxinfo.setEXCHANGERATEFORINCOMING(new BigDecimal(1));
		tnxinfo.setEXCHANGERATEFOROUTGOING(new BigDecimal(1));

		String transactionType = getSwifttransactionDesc("205");
		tnxinfo.setTRANSACTIONTYPE(transactionType);
		remittanceMsgDetails.setTRANSACTIONDETAISINFO(tnxinfo);

		String creditAccount = "";
		if (mt205Details.getBeneficiaryInstitute() != null)
			creditAccount = getAccountNumberFromText(mt205Details.getBeneficiaryInstitute(),
					mt205Details.getBeneficiaryInstOption(), mt205Details.getTdcurrencyCode());
		CREDITORDTL crdtdtl = new CREDITORDTL();
		crdtdtl.setCREDITACCOUNTID(creditAccount);
		BigDecimal creditAmt = getBigDecimalfromString(mt205Details.getTdamount());
		crdtdtl.setCREDITAMOUNT(creditAmt);
		remittanceMsgDetails.setCREDITORDTL(crdtdtl);

		if (!creditAccount.equalsIgnoreCase("")) {
			remittanceMsgDetails.setCUSTOMERNAME(getCustomerShortName(creditAccount));
		}

		String debitAccount = "";
		if (mt205Details.getSendersCorrespondent() != null) {
			debitAccount = getAccountNumberFromText(mt205Details.getSendersCorrespondent(),
					mt205Details.getSendersCorresOption(), mt205Details.getTdcurrencyCode());
		} else if (mt205Details.getSender() != null) {
			flag = "D";
			debitAccount = getAccountNumberFromBIC(mt205Details.getSender(), mt205Details.getTdcurrencyCode());
		}
		DEBITORDTL debtr = new DEBITORDTL();
		debtr.setDEBITACCOUNTID(debitAccount);
		BigDecimal debitAmt = getBigDecimalfromString(mt205Details.getTdamount());
		debtr.setDEBITAMOUNT(debitAmt);// Field 59 beneficiary customer

		String creditAccountCurr = CommonConstants.EMPTY_STRING;
		String debitAccountCurr = CommonConstants.EMPTY_STRING;
		GetExchangeRateSwiftRemittance exchangerate = new GetExchangeRateSwiftRemittance();
		BigDecimal expectedCreditAmt = BigDecimal.ZERO;
		BigDecimal creditExchangeRate = BigDecimal.ZERO;
		String creditExchRateType = "";
		String debitExchRateType = "";

		BigDecimal expectedDebitAmt = BigDecimal.ZERO;
		BigDecimal debitExchangeRate = BigDecimal.ZERO;
		if (null != creditAccount && !creditAccount.equals("")) {
			creditAccountCurr = getAccountCurrency(creditAccount);
			Map<String, Object> exchangeRateMap = exchangerate.getExchangeRate(mt205Details.getTdcurrencyCode(),
					creditAccountCurr, creditAmt, "N");
			expectedCreditAmt = (BigDecimal) exchangeRateMap.get("convertedAmount");
			creditExchangeRate = (BigDecimal) exchangeRateMap.get("exchangeRate");
			creditExchRateType = (String) exchangeRateMap.get("exchangeRateType");
		}
		if (null != debitAccount && !debitAccount.equals("")) {
			debitAccountCurr = getAccountCurrency(debitAccount);
			Map<String, Object> exchangeRateMap = exchangerate.getExchangeRate(mt205Details.getTdcurrencyCode(),
					debitAccountCurr, debitAmt, "N");
			expectedDebitAmt = (BigDecimal) exchangeRateMap.get("convertedAmount");
			debitExchangeRate = (BigDecimal) exchangeRateMap.get("exchangeRate");
			debitExchRateType = (String) exchangeRateMap.get("exchangeRateType");
		}
		crdtdtl.setEXPECTEDCREDITAMOUNT(expectedCreditAmt);
		remittanceMsgDetails.setCREDITORDTL(crdtdtl);
		debtr.setEXPECTEDDEBITAMOUNT(expectedDebitAmt);
		remittanceMsgDetails.setDEBITORDTL(debtr);
		remittanceMsgDetails.setExchangeRateTypeOUT(debitExchRateType);
		remittanceMsgDetails.setExchangeRateTypeIN(creditExchRateType);
		tnxinfo.setEXCHANGERATEFORINCOMING(creditExchangeRate);
		tnxinfo.setEXCHANGERATEFOROUTGOING(creditExchangeRate);
		remittanceMsgDetails.setCrAccountCurrency(creditAccountCurr);
		remittanceMsgDetails.setDrAccountCurrency(debitAccountCurr);
		remittanceMsgDetails.setExpctCrAmountCurrency(creditAccountCurr);
		remittanceMsgDetails.setExpctDrAmountCurrency(debitAccountCurr);
		remittanceMsgDetails.setCrAmountCurrency(mt205Details.getTdcurrencyCode());
		remittanceMsgDetails.setDrAmountCurrency(mt205Details.getTdcurrencyCode());
		remittanceMsgDetails.setChargeCurrency("");

		remittanceMsgDetails.setTRANSACTIONDETAISINFO(tnxinfo);
		String ordInstOption = mt205Details.getOrderingInstitutionOption();

		if (ordInstOption != null) {
			orderingInstDetails = setOrdInstDetails(ordInstOption, mt205Details.getOrderingInstitute());
		}
		remittanceMsgDetails.setORDERINGINSTITUTIONDTL(orderingInstDetails);

		if (mt205Details.getAccountWithInstitution() != null) {
			intermediaryDetails = setIntrmediaryDetails(mt205Details.getAccountWithInstOption(),
					mt205Details.getAccountWithInstitution());
		} else if (mt205Details.getIntermediary() != null) {
			intermediaryDetails = setIntrmediaryDetails(mt205Details.getIntermediaryOption(),
					mt205Details.getIntermediary());
		}
		remittanceMsgDetails.setINTERMEDIARYDETAILS(intermediaryDetails);

		if (mt205Details.getBeneficiaryInstitute() != null) {
			beneInstDtls = setBeneInstDetails(mt205Details.getBeneficiaryInstOption(),
					mt205Details.getBeneficiaryInstitute());
		}
		remittanceMsgDetails.setBENEFICIARYINSTDETIALS(beneInstDtls);

		if (mt205Details.getSenderToReceiverInformation() != null) {
			senderToReceiverInfo = setSenderToRecInfo(mt205Details.getSenderToReceiverInformation());
		}
		remittanceMsgDetails.setSENDERTORECEIVERINFO(senderToReceiverInfo);
		remittanceMsgDetails.setBANKTOBANKINFO(new BANKTOBANKINFO());
		remittanceMsgDetails.setBENEFICIARYCUSTOMERINFO(new BENEFICIARYCUSTOMERINFO());
		remittanceMsgDetails.setKYCDETAILS(new KYCDETAILS());
		remittanceMsgDetails.setORDERINGICUSTINFO(new ORDERINGICUSTINFO());
		remittanceMsgDetails.setPAYTOPARTYDETAILS(new PAYTOPARTYDETAILS());
		remittanceMsgDetails.setRemittanceINFO(new RemittanceINFO());
		remittanceMsgDetails.setTERMSCONDITIONSINFO(new TERMSCONDITIONSINFO());

		RemittanceIdGenerator remitIdGenerator = new RemittanceIdGenerator();
		remittanceMsgDetails.setREMITTANCEIDPK(remitIdGenerator.getRemittanceId(mt205Details.getTdcurrencyCode()));
		remittanceMsgDetails.setEnd2EndTxnRef(mt205Details.getEnd2EndTxnRef());
		return remittanceMsgDetails;
	}

	public BENEFICIARYCUSTOMERINFO setBeneCustDetails(String benCustOpt, String beneCustomer) {
		BENEFICIARYCUSTOMERINFO beneCustDetls = new BENEFICIARYCUSTOMERINFO();
		String[] instDtl = new String[3];
		if (beneCustomer != null) {
			String[] temp = beneCustomer.split("[$]");
			if (benCustOpt != null && benCustOpt.equals("A")) {
				if (temp[0].startsWith("/") && beneCustomer.indexOf('$') != -1) {
					beneCustDetls.setBENEFICIARYCUSTPARTYIDENTIFIER(temp[0].substring(1));
					beneCustDetls.setBENEFICIARYCUSTIDENTCODE(temp[1]);
					instDtl = getIdentifierCodeDetail(temp[1]);
					int instDtlLength = instDtl.length;
					beneCustDetls.setBENEFICIARYCUSTTEXT1(instDtlLength >= 1 ? instDtl[0] : "");
					beneCustDetls.setBENEFICIARYCUSTTEXT2(instDtlLength >= 2 ? instDtl[1] : "");
					beneCustDetls.setBENEFICIARYCUSTTEXT3(instDtlLength >= 3 ? instDtl[2] : "");
				} else {
					beneCustDetls.setBENEFICIARYCUSTIDENTCODE(temp[0]);
					instDtl = getIdentifierCodeDetail(temp[0]);
					int instDtlLength = instDtl.length;
					beneCustDetls.setBENEFICIARYCUSTTEXT1(instDtlLength >= 1 ? instDtl[0] : "");
					beneCustDetls.setBENEFICIARYCUSTTEXT2(instDtlLength >= 2 ? instDtl[1] : "");
					beneCustDetls.setBENEFICIARYCUSTTEXT3(instDtlLength >= 3 ? instDtl[2] : "");
				}
			} else {
				if (temp[0].startsWith("/")) {
					beneCustDetls.setBENEFICIARYCUSTPARTYIDENTIFIER(temp[0].substring(1));
					int tempLength = temp.length;
					beneCustDetls.setBENEFICIARYCUSTTEXT1(tempLength >= 2 ? temp[1] : "");
					beneCustDetls.setBENEFICIARYCUSTTEXT2(tempLength >= 3 ? temp[2] : "");
					beneCustDetls.setBENEFICIARYCUSTTEXT3(tempLength >= 4 ? temp[3] : "");
					beneCustDetls.setBENEFICIARYCUSTTEXT4(tempLength >= 5 ? temp[4] : "");
				}
			}
		}
		return beneCustDetls;
	}

	public BENEFICIARYINSTDETIALS setBeneInstDetails(String beneInstOpt, String beneInst) {
		BENEFICIARYINSTDETIALS beneInsDetls = new BENEFICIARYINSTDETIALS();
		String[] instDtl = new String[3];

		if (beneInst != null) {
			String[] temp = beneInst.split("[$]");
			if (beneInstOpt != null && beneInstOpt.equalsIgnoreCase("A")) {
				if (temp[0].startsWith("/") && !temp[0].startsWith("//")) {
					if (beneInst.indexOf('$') != -1) {
						beneInsDetls.setBENEFICIARINSTYPARTYIDENTIFIER(temp[0].substring(1)); //
						beneInsDetls.setBENEFICIARYINSTIDENTCODE(temp[1]);
						instDtl = getIdentifierCodeDetail(temp[1]);
						int instDtlLength = instDtl.length;
						beneInsDetls.setBENEFICIARYINSTTEXT1(instDtlLength >= 1 ? instDtl[0] : "");
						beneInsDetls.setBENEFICIARYINSTTEXT2(instDtlLength >= 2 ? instDtl[1] : "");
						beneInsDetls.setBENEFICIARYINSTTEXT3(instDtlLength >= 3 ? instDtl[2] : "");
					}
				} else if (temp[0].startsWith("//")) {
					beneInsDetls.setBENPSRTYIDENTCLRCODE(temp[0].substring(2, 3));
					beneInsDetls.setBENEFICIARINSTYPARTYIDENTIFIER(temp[0].substring(5));
				} else {
					beneInsDetls.setBENEFICIARYINSTIDENTCODE(temp[0]);
					instDtl = getIdentifierCodeDetail(temp[0]);
					int instDtlLength = instDtl.length;
					beneInsDetls.setBENEFICIARYINSTTEXT1(instDtlLength >= 1 ? instDtl[0] : "");
					beneInsDetls.setBENEFICIARYINSTTEXT2(instDtlLength >= 2 ? instDtl[1] : "");
					beneInsDetls.setBENEFICIARYINSTTEXT3(instDtlLength >= 3 ? instDtl[2] : "");
				}
			}
			if (beneInstOpt != null && beneInstOpt.equalsIgnoreCase("D")) {
				if (temp[0].startsWith("//"))
					beneInsDetls.setBENPSRTYIDENTCLRCODE(temp[0].substring(2, 3));
				if (temp[0].startsWith("/") && !(temp[0].startsWith("/")))
					beneInsDetls.setBENEFICIARINSTYPARTYIDENTIFIER(temp[0].substring(1));
			}
		}
		return beneInsDetls;
	}

	public ORDERINGICUSTINFO setOrdCustDetails(String ordCustOption, String ordCust) {
		ORDERINGICUSTINFO ordCustDtls = new ORDERINGICUSTINFO();
		String[] ordCustInfo = new String[5];
		String[] instDtl = new String[3];
		ordCustInfo = ordCust.split("[$]");
		if (ordCustOption != null) {
			if (ordCustOption.equalsIgnoreCase("A")) {
				if (!ordCustInfo[0].startsWith("/")) {
					ordCustDtls.setORDCUSTIDENBIC(ordCustInfo[0]);
					instDtl = getIdentifierCodeDetail(ordCustInfo[0]);
				} else {
					ordCustDtls.setORDCUSTPTYIDENACCVALUE(ordCustInfo[0].substring(1));
					if (ordCust.indexOf('$') != -1) {
						ordCustDtls.setORDCUSTIDENBIC(ordCustInfo[1]);
						instDtl = getIdentifierCodeDetail(ordCustInfo[1]);
					}
				}
				int instDtlLength = instDtl.length;
				ordCustDtls.setORDERINGICUSTINFO1(instDtlLength >= 1 ? instDtl[0] : "");
				ordCustDtls.setORDERINGICUSTINFO2(instDtlLength >= 2 ? instDtl[1] : "");
				ordCustDtls.setORDERINGICUSTINFO3(instDtlLength >= 3 ? instDtl[2] : "");
			}
			if (ordCustOption.equalsIgnoreCase("F") || ordCustOption.equalsIgnoreCase("K")
					|| ordCustOption.equalsIgnoreCase("J")) {
				if (ordCustInfo[0].startsWith("/")) {
					ordCustDtls.setORDCUSTPTYIDENACCVALUE(ordCustInfo[0].substring(1));
					int ordCustInfoLength = ordCustInfo.length;
					ordCustDtls.setORDERINGICUSTINFO1(ordCustInfoLength >= 2 ? ordCustInfo[1] : "");
					ordCustDtls.setORDERINGICUSTINFO2(ordCustInfoLength >= 3 ? ordCustInfo[2] : "");
					ordCustDtls.setORDERINGICUSTINFO3(ordCustInfoLength >= 4 ? ordCustInfo[3] : "");
					ordCustDtls.setORDERINGICUSTINFO4(ordCustInfoLength >= 5 ? ordCustInfo[4] : "");
				} else {
					if (ordCustInfo[0].substring(1).indexOf("/") != -1) {
						String partyIdent[] = new String[4];
						partyIdent = ordCustInfo[0].split("[/]");
						ordCustDtls.setORDCUSTPTYIDCODE(partyIdent[0]);
						ordCustDtls.setORDCUSTPTYIDCONTRY(partyIdent[1]);
						ordCustDtls.setORDCUSTPTYIDVALUE(partyIdent[2]);
					}
				}
			}
		}

		return ordCustDtls;
	}

	public ORDERINGINSTITUTIONDTL setOrdInstDetails(String ordInstOption, String ordInstDetails) {
		ORDERINGINSTITUTIONDTL OrdInstDtls = new ORDERINGINSTITUTIONDTL();
		String temp[] = new String[5];
		String[] instDtl = new String[3];
		temp = ordInstDetails.split("[$]");
		if (ordInstOption != null) {
			if (ordInstOption.equalsIgnoreCase("A")) {
				if (temp[0].startsWith("/") && !temp[0].startsWith("//")) {
					if (ordInstDetails.indexOf('$') != -1) {
						OrdInstDtls.setORDERINGINSTPARTYIDENTCODE(temp[0].substring(1)); //
						OrdInstDtls.setORDERINGINSTITUTIONIDENTCODE(temp[1]);
						instDtl = getIdentifierCodeDetail(temp[1]);
						int instDtlLength = instDtl.length;
						OrdInstDtls.setORDERINGINSTITUTIONDTL1(instDtlLength >= 1 ? instDtl[0] : "");
						OrdInstDtls.setORDERINGINSTITUTIONDTL2(instDtlLength >= 2 ? instDtl[1] : "");
						OrdInstDtls.setORDERINGINSTITUTIONDTL3(instDtlLength >= 3 ? instDtl[2] : "");
					}
				} else if (temp[0].startsWith("//")) {
					//
				} else {
					OrdInstDtls.setORDERINGINSTITUTIONIDENTCODE(temp[0]);
					instDtl = getIdentifierCodeDetail(temp[0]);
					int instDtlLength = instDtl.length;
					OrdInstDtls.setORDERINGINSTITUTIONDTL1(instDtlLength >= 1 ? instDtl[0] : "");
					OrdInstDtls.setORDERINGINSTITUTIONDTL2(instDtlLength >= 2 ? instDtl[1] : "");
					OrdInstDtls.setORDERINGINSTITUTIONDTL3(instDtlLength >= 3 ? instDtl[2] : "");
				}
			}
			if (ordInstOption.equalsIgnoreCase("D")) {
				if (temp[0].startsWith("/"))
					OrdInstDtls.setORDERINGINSTPARTYIDENTCODE(temp[0].substring(1));
				int tempLength = temp.length;
				OrdInstDtls.setORDERINGINSTITUTIONDTL1(tempLength >= 2 ? temp[1] : "");
				OrdInstDtls.setORDERINGINSTITUTIONDTL1(tempLength >= 3 ? temp[2] : "");
				OrdInstDtls.setORDERINGINSTITUTIONDTL1(tempLength >= 4 ? temp[3] : "");
			}
		}
		return OrdInstDtls;
	}

	public PAYTOPARTYDETAILS setPayToDetails(String SendrCorOption, String SendrCorrpd) {
		PAYTOPARTYDETAILS PayToDetails = new PAYTOPARTYDETAILS();
		String[] instDtl = new String[3];
		instDtl = null;
		if (SendrCorOption != null) {
			String temp[] = SendrCorrpd.split("[$]");
			if (SendrCorOption.equalsIgnoreCase("A") || SendrCorOption.equalsIgnoreCase("B")
					|| SendrCorOption.equalsIgnoreCase("D")) {
				if (SendrCorrpd.startsWith("/")) {
					PayToDetails.setPAYTOPARTYIDENTIFIER(temp[0].substring(1));
					if (SendrCorrpd.indexOf('$') != -1) {
						PayToDetails.setPAYTOIDENTCODE(temp[1]);
						instDtl = getIdentifierCodeDetail(temp[1]);
					}
				} else {
					PayToDetails.setPAYTOIDENTCODE(SendrCorrpd);
					instDtl = getIdentifierCodeDetail(SendrCorrpd);
				}
			}
		}
		if (instDtl != null) {
			int instDtlLength = instDtl.length;
			PayToDetails.setPAYTOTEXT1(instDtlLength >= 1 ? instDtl[0] : "");
			PayToDetails.setPAYTOTEXT2(instDtlLength >= 2 ? instDtl[1] : "");
			PayToDetails.setPAYTOTEXT3(instDtlLength >= 3 ? instDtl[2] : "");
		}

		return PayToDetails;
	}

	public INTERMEDIARYDETAILS setIntrmediaryDetails(String inMedOp, String intermediary) {
		String[] instDtl = new String[3];
		INTERMEDIARYDETAILS intMedInsDetls = new INTERMEDIARYDETAILS();
		if (intermediary != null) {
			String[] interMdInst = intermediary.split("[$]");
			if (inMedOp != null && (inMedOp.equalsIgnoreCase("A") || inMedOp.equalsIgnoreCase("B"))) {
				if (interMdInst[0].startsWith("/")) {
					intMedInsDetls.setINTMDPRTYIDNTIFR(interMdInst[0].substring(1));
					if (intermediary.indexOf('$') != -1) {
						intMedInsDetls.setINTERMEDIARYIDENTCODE(interMdInst[1]);
						instDtl = getIdentifierCodeDetail(interMdInst[1]);
					}
				} else {
					intMedInsDetls.setINTERMEDIARYIDENTCODE(interMdInst[0]);
					instDtl = getIdentifierCodeDetail(interMdInst[0]);
				}
				int instDtlLength = instDtl.length;
				intMedInsDetls.setINTERMEDIARYTEXT1(instDtlLength >= 1 ? instDtl[0] : "");
				intMedInsDetls.setINTERMEDIARYTEXT2(instDtlLength >= 2 ? instDtl[1] : "");
				intMedInsDetls.setINTERMEDIARYTEXT3(instDtlLength >= 3 ? instDtl[2] : "");

			}
		}

		return intMedInsDetls;
	}

	public SENDERTORECEIVERINFO setSenderToRecInfo(String senderToRecInfo) {

		SENDERTORECEIVERINFO senToRecInfo = new SENDERTORECEIVERINFO();
		if ( null!= senderToRecInfo  && senderToRecInfo != "") {
			String[] Temp = senderToRecInfo.split("[$]");
			for (int i = 0; i < Temp.length; i++) {
				if (i == 0)
					senToRecInfo.setSENDERTORECEIVERINFO1((Temp[0] != null) ? Temp[0] : "");
				if (i == 1)
					senToRecInfo.setSENDERTORECEIVERINFO2((Temp[1] != null) ? Temp[1] : "");
				if (i == 2)
					senToRecInfo.setSENDERTORECEIVERINFO3((Temp[2] != null) ? Temp[2] : "");
				if (i == 3)
					senToRecInfo.setSENDERTORECEIVERINFO4((Temp[3] != null) ? Temp[3] : "");
				if (i == 4)
					senToRecInfo.setSENDERTORECEIVERINFO5((Temp[4] != null) ? Temp[4] : "");
				if (i == 5)
					senToRecInfo.setSENDERTORECEIVERINFO6((Temp[5] != null) ? Temp[5] : "");
			}
		}
		return senToRecInfo;
	}

	private static java.sql.Date getFormattedDate(String dateString) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
		java.util.Date date = null;
		try {
			date = dateFormatter.parse(dateString);
		} catch (ParseException e) {
		}
		if (date != null) {
			java.sql.Date sqlDate = new java.sql.Date(date.getTime());

			return sqlDate;
		} else {
			return null;
		}
	}

	private static BigDecimal getBigDecimalfromString(String strValue) {
		if (!StringUtils.isBlank(strValue)) {
			String stringtoparse = strValue.replaceAll(",", "");
			BigDecimal value = new BigDecimal(stringtoparse);
			return value;
		} else {
			return new BigDecimal(1);
		}
	}
	
	@SuppressWarnings("deprecation")
	private String getAccountNumberFromText53(String text, String option, String currency) {
		String[] arrays = splitpartyIdentifier(text);
		String accountNumber = "";
		boolean accountFromMsg = false;
		if (arrays.length > 0) {
            // when 53B comes in the message like - 53B:/D/21986201 - 21986201 is the account number
            if (arrays[0].startsWith("/") && arrays[0].startsWith("/", 2) && !arrays[0].startsWith("//")) {
                accountNumber = arrays[0].substring(3);
                accountFromMsg = true;
            }
            // when 53B comes in the message like - 53B:/21986201 - 21986201 is the account number
            else if (arrays[0].startsWith("/") && !arrays[0].startsWith("//")) {
                accountNumber = arrays[0].substring(1);
                accountFromMsg = true;
                
            }
            else {
				
            	accountNumber = getAccountNumberFromBIC(arrays[0], currency);
				
				
			}
		}
		return accountNumber;
	}

	private String getAccountNumberFromText(String text, String option, String currency) {
		String[] arrays = splitpartyIdentifier(text);
		String accountNumber = "";
		boolean accountFromMsg = false;
		if (arrays.length > 0) {
            // when 53B comes in the message like - 53B:/D/21986201 - 21986201 is the account number
            if (arrays[0].startsWith("/") && arrays[0].startsWith("/", 2) && !arrays[0].startsWith("//")) {
                accountNumber = arrays[0].substring(3);
                accountFromMsg = true;
            }
            // when 53B comes in the message like - 53B:/21986201 - 21986201 is the account number
            else if (arrays[0].startsWith("/") && !arrays[0].startsWith("//")) {
                accountNumber = arrays[0].substring(1);
                accountFromMsg = true;
            }
            else {
				accountNumber = getAccountNumberFromBIC(arrays[0], currency);
			}
		}

		if (!accountNumber.isEmpty() && accountFromMsg) {
			List AccountList = FinderMethods.findAccountInfoByAccountID(accountNumber,
					BankFusionThreadLocal.getBankFusionEnvironment(), null);
			if (!AccountList.isEmpty()) {
				IBOAttributeCollectionFeature accountInfo = (IBOAttributeCollectionFeature) AccountList.get(0);
				accountNumber = accountInfo.getBoID();
			} else {
				// derive the nostro account from module configuration moduleName=FEX and
				// paramName=DEFAULT_NOSTRO
				
				//user story FBPY-5433
				String invalidAccountNumber = accountNumber;
				setF_OUT_invalidCreditAccount(invalidAccountNumber);


				boolean isCreditAccountUpdated = false;
								if(StringUtils.isEmpty(accountNumber) || (!isAccountExist(accountNumber)))
								{
									accountNumber = CommonConstants.EMPTY_STRING;
									isCreditAccountUpdated = true;
									setF_OUT_isCreditAccountUpdated(isCreditAccountUpdated);
								}
				accountNumber = getNostroAccountFromModuleConfig(currency,
						BankFusionThreadLocal.getUserSession().getBranchSortCode());
			}
		}
		return accountNumber;
	}

	private String[] splitpartyIdentifier(String text) {
		String[] arrays = new String[3];
		arrays = text.split("[$]");
		return arrays;
	}

	private String getAccountNumberFromBIC(String BIC, String currency) {
		String accountId = CommonConstants.EMPTY_STRING;
		if ((BIC.length()) == 8) {
			BIC = BIC + "XXX";
		}
		ArrayList params = new ArrayList();
		Iterator customerList = null;
		IBOSwtCustomerDetail swtCustomerDetail = null;
		params.add(BIC);
		customerList = factory.findByQuery(IBOSwtCustomerDetail.BONAME, SWTCUSTOMERWHERECLAUSE, params, 1);
		if (customerList.hasNext()) {
			swtCustomerDetail = (IBOSwtCustomerDetail) customerList.next();
			if (flag.equals("D")) {
				String DRCCOUNTNUMBER = (String) swtCustomerDetail.getF_DRACCOUNTNUMBER();
				accountId = DRCCOUNTNUMBER;
				return DRCCOUNTNUMBER = getAccountFromAccountAndPseudonym(DRCCOUNTNUMBER, currency);
			} else {
				String CRACCOUNTNUMBER = (String) swtCustomerDetail.getF_CRACCOUNTNUMBER();
				accountId = CRACCOUNTNUMBER;
				return CRACCOUNTNUMBER = getAccountFromAccountAndPseudonym(CRACCOUNTNUMBER, currency);
			}
		}

		return accountId;
	}

	private String getAccountFromAccountAndPseudonym(String AccountNumber, String currency) {
		String accNumber = CommonConstants.EMPTY_STRING;
		if (!(AccountNumber.trim().length() == 0)) {
			if (isAccountExist(AccountNumber.trim())) {
				accNumber = AccountNumber.trim();
			} else {
				try {
					String txnCurrencyCode = currency;
					;
					IBOAttributeCollectionFeature accountValues = FinderMethods.findAccountByPseudonameAndContextValue(
							"%CURRENCY%" + txnCurrencyCode + "%" + AccountNumber.trim(), txnCurrencyCode, Boolean.TRUE,
							BankFusionThreadLocal.getBankFusionEnvironment(), null);
					if (!(accountValues == null)) {
						accNumber = accountValues.getBoID();
					}
				} catch (BankFusionException Bk) {
					logger.error(ExceptionUtil.getExceptionAsString(Bk));
				}
			}
		}
		return accNumber;
	}

	/**
	 * Method Description: Check if the account Exists
	 * @param accountId
	 * @return
	 */
	private Boolean isAccountExist(String accountId) {
		IBOAccount accountDtls = (IBOAccount) factory.findByPrimaryKey(IBOAccount.BONAME, accountId, true);
		if (null != accountDtls && !accountDtls.getBoID().isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isDefaultNostroConfigured() {
		String value = "";
		HashMap<String, ReadModuleConfigurationRq> moduleParams = new HashMap<String, ReadModuleConfigurationRq>();
		ModuleKeyRq module = new ModuleKeyRq();
		ReadModuleConfigurationRq read = new ReadModuleConfigurationRq();
		module.setModuleId("SWIFT");
		module.setKey("GenerateMessageToDefaultNostro");
		read.setModuleKeyRq(module);
		moduleParams.put("ReadModuleConfigurationRq", read);
		HashMap valueFromModuleConfiguration = MFExecuter.executeMF(READ_MODULE_CONFIGURATION,
				BankFusionThreadLocal.getBankFusionEnvironment(), moduleParams);
		if (valueFromModuleConfiguration != null) {
			ReadModuleConfigurationRs rs = (ReadModuleConfigurationRs) valueFromModuleConfiguration
					.get("ReadModuleConfigurationRs");
			value = rs.getModuleConfigDetails().getValue().toString();
		}
		if (value.equalsIgnoreCase("Y"))
			return true;
		else
			return false;
	}

	public String FindDefaultNostroAcc(String identifierCode) {
		HashMap inputMap = new HashMap();
		HashMap OutputMap = new HashMap();
		inputMap.put("IdentifierCode", identifierCode);
		OutputMap = MFExecuter.executeMF("UB_SWT_GetDefault_NOSTRO_SRV",
				BankFusionThreadLocal.getBankFusionEnvironment(), inputMap);
		String defaultNostroAccount = (String) OutputMap.get("DefaultNostroAccount");
		return defaultNostroAccount;
	}

	public String[] getCustomerDetails(String AccNumber, String BICCODE) {
		String[] custDetail = new String[5];
		List<IBOAttributeCollectionFeature> finderResult = null;
		if (!AccNumber.equalsIgnoreCase("")) {
			if (AccNumber != null && AccNumber.trim().length() > 0) {
				finderResult = FinderMethods.findAccountInfoByAccountID(AccNumber,
						BankFusionThreadLocal.getBankFusionEnvironment(), null);
			}
			if (finderResult != null && finderResult.size() > 0) {
				IBOAttributeCollectionFeature account = finderResult.get(0);
				IBOPersonDetails custDetails = (IBOPersonDetails) BankFusionThreadLocal.getPersistanceFactory()
						.findByPrimaryKey(IBOPersonDetails.BONAME, account.getF_CUSTOMERCODE(), true);
				if (null != custDetails) {
					custDetail[0] = (custDetails.getF_FORENAME());
					custDetail[1] = (custDetails.getF_MIDDLENAME());
					custDetail[2] = (custDetails.getF_SURNAME());
				}
			}
		}
		return custDetail;
	}

	public String[] getIdentifierCodeDetail(String identifierCode) {
		String[] Details = new String[3];
		HashMap inputMap = new HashMap();
		HashMap OutputMap = new HashMap();
		inputMap.put("IdentifierCode", identifierCode);
		OutputMap = MFExecuter.executeMF("UB_SWT_IdentifierCodeRead_SRV",
				BankFusionThreadLocal.getBankFusionEnvironment(), inputMap);
		try {
			Details[0] = (String) OutputMap.get("InstitutionName");
			Details[1] = (String) OutputMap.get("City");
			Details[2] = (String) OutputMap.get("Location");
		} catch (ArrayIndexOutOfBoundsException E) {
			logger.error(ExceptionUtil.getExceptionAsString(E));
		}
		return Details;
	}

	public String getCustomerShortName(String AccNumber) {
		List<IBOAttributeCollectionFeature> finderResult = null;
		String accountName = "";
		if (!AccNumber.equalsIgnoreCase(""))
			if (AccNumber != null && AccNumber.trim().length() > 0)
				finderResult = FinderMethods.findAccountInfoByAccountID(AccNumber,
						BankFusionThreadLocal.getBankFusionEnvironment(), null);
		if (finderResult != null && finderResult.size() > 0) {
			IBOAttributeCollectionFeature account = finderResult.get(0);
			accountName = account.getF_ACCOUNTNAME();
		}
		return accountName;
	}

	public boolean isBICExistInSwtCutDtls(String BIC) {
		if ((BIC.length()) == 8) {
			BIC = BIC + "XXX";
		}
		ArrayList params = new ArrayList();
		Iterator customerList = null;
		IBOSwtCustomerDetail swtCustomerDetail = null;
		params.add(BIC);
		customerList = factory.findByQuery(IBOSwtCustomerDetail.BONAME, SWTCUSTOMERWHERECLAUSE, params, 1);
		if (customerList.hasNext())
			return true;
		else
			return false;
	}

	public CHARGERELATEDINFO setChargeDetails(SwiftMT103 MT103Details, String creditAccount, String debitAccount) {
		CHARGERELATEDINFO chargeDetails = new CHARGERELATEDINFO();
		HashMap inputMap = new HashMap();
		HashMap OutputMap = new HashMap();
		inputMap.put("CreditAccount", creditAccount);
		inputMap.put("DetailsOfCharge", MT103Details.getDetailsOfCharges());
		inputMap.put("InterBankSettledAmount", getBigDecimalfromString(MT103Details.getTdAmount()));
		inputMap.put("InterBankSettledAmountCurrency", MT103Details.getTdCurrencyCode());
		inputMap.put("DebitAccount", debitAccount);
		inputMap.put("SendersBIC", MT103Details.getSender());

		String ReceiversCharges = MT103Details.getReceiversCharges();
		if (ReceiversCharges != null && ReceiversCharges.length() > 0) {
			ReceiversCharges = ReceiversCharges.substring(3);
			ReceiversCharges = ReceiversCharges.replaceAll(",", ".");
		} else {
			ReceiversCharges = "0.00";
		}

		inputMap.put("ReceiversCharge", new BigDecimal(ReceiversCharges));

		OutputMap = MFExecuter.executeMF("UB_SWT_IncomingAmountCalculation_SRV",
				BankFusionThreadLocal.getBankFusionEnvironment(), inputMap);
		valiateCharges(OutputMap);
		
		if (OutputMap.get("ChargeVector") != null) {
			setF_OUT_OurChargeVector((VectorTable)(OutputMap.get("ChargeVector")));
		}

		if (OutputMap.get("ChargeAmount") != null) {
			BigDecimal chargeAmount = new BigDecimal(OutputMap.get("ChargeAmount").toString());
			chargeDetails.setChargeAmount(chargeAmount);
		}
		if (OutputMap.get("TransactionAmount") != null) {
			BigDecimal TransactionAmount = new BigDecimal(OutputMap.get("TransactionAmount").toString());
			chargeDetails.setTransactionAmount(TransactionAmount);
		}
		if (OutputMap.get("InterBankSettledAmount") != null) {
			BigDecimal InterBankSettledAmount = new BigDecimal(OutputMap.get("InterBankSettledAmount").toString());
			chargeDetails.setInterBankSettledAmount(InterBankSettledAmount);
		}
		if (OutputMap.get("ReceiversCharge") != null) {
			BigDecimal ReceiversCharge = new BigDecimal(OutputMap.get("ReceiversCharge").toString());
			chargeDetails.setReceiversCharge(ReceiversCharge);
		}
		if (OutputMap.get("SendersCharge") != null) {
			BigDecimal SendersCharge = new BigDecimal(OutputMap.get("SendersCharge").toString());
			chargeDetails.setSendersCharge(SendersCharge);
		}
		if (OutputMap.get("taxAmount") != null) {
			BigDecimal TaxAmount = new BigDecimal(OutputMap.get("taxAmount").toString());
			chargeDetails.setTaxAmount(TaxAmount);
		}
		if (OutputMap.get("ChargeCalculationCode") != null) {
			String ChargeCalculationCode = (String) OutputMap.get("ChargeCalculationCode");
			chargeDetails.setChargeCalculationCode(ChargeCalculationCode);
		}
		if (OutputMap.get("ChargeCode") != null) {
			String ChargeCode = (String) OutputMap.get("ChargeCode");
			chargeDetails.setChargeCode(ChargeCode);
		}
		if (OutputMap.get("ChargeReceivingAccount") != null) {
			String ChargeReceivingAccount = (String) OutputMap.get("ChargeReceivingAccount");
			chargeDetails.setChargeReceivingAccount(ChargeReceivingAccount);
		}
		if (OutputMap.get("TaxCode") != null) {
			String TaxCode = (String) OutputMap.get("TaxCode");
			chargeDetails.setTaxCode(TaxCode);
		}
		if (OutputMap.get("TaxNarrative") != null) {
			String TaxNarrative = (String) OutputMap.get("TaxNarrative");
			chargeDetails.setTaxNarrative(TaxNarrative);
		}
		if (OutputMap.get("TaxReceivingAccount") != null) {
			String TaxReceivingAccount = (String) OutputMap.get("TaxReceivingAccount");
			chargeDetails.setTaxReceivingAccount(TaxReceivingAccount);
		}
		if (OutputMap.get("TransactionCode") != null) {
			String TransactionCode = (String) OutputMap.get("TransactionCode");
			chargeDetails.setTransactionCode(TransactionCode);
		}
		return chargeDetails;
	}

	public String getSwifttransactionDesc(String transactionType) {
		HashMap moduleParams = new HashMap<>();
		moduleParams.put("reference", "233");
		String transactionDescription = null;
		try {
			HashMap outMap = MFExecuter.executeMF("CB_GCD_GetCodeList_SRV",
					BankFusionThreadLocal.getBankFusionEnvironment(), moduleParams);
			// debitAccountCurr = (String)outMap.get("ISOCURRENCYCODE");
			VectorTable InputVectorTable = (VectorTable) outMap.get("CodeList");
			if (InputVectorTable != null && InputVectorTable.hasData()) {
				int Size = InputVectorTable.size();
				for (int i = 0; i < Size; i++) {
					HashMap attributes = (HashMap) InputVectorTable.getRowTagsAsFields(i);

					if (attributes != null) {
						String messageType = attributes.get("f_STRVALUE").toString();
						if (transactionType.equals(messageType)) {
							transactionDescription = attributes.get("f_STRVALUE").toString();
						}
					}
				}
			}

		} catch (Exception E) {
			logger.error(ExceptionUtil.getExceptionAsString(E));
		}

		return transactionDescription;
	}

	/**
	 * @param remittanceDetail
	 * @param messageHeader
	 * @return
	 */
	private UB_SWT_RemittanceProcessRq getOutwardRemittanceForScreen(IBOUB_SWT_RemittanceTable remittanceDetail,
			IBOUB_INF_MessageHeader messageHeader) {
		UB_SWT_RemittanceProcessRq remittanceMsgDetails = new UB_SWT_RemittanceProcessRq();
		BENEFICIARYINSTDETIALS beneficiaryInstDetails = new BENEFICIARYINSTDETIALS();
		remittanceMsgDetails.setBENEFICIARYINSTDETIALS(beneficiaryInstDetails);
		ORDERINGICUSTINFO orderingCustInfo = new ORDERINGICUSTINFO();
		remittanceMsgDetails.setORDERINGICUSTINFO(orderingCustInfo);
		ORDERINGINSTITUTIONDTL orderingInstDetail = new ORDERINGINSTITUTIONDTL();
		PAYTOPARTYDETAILS payToPartyDetails = new PAYTOPARTYDETAILS();
		SENDERTORECEIVERINFO senderToReceiverInfo = new SENDERTORECEIVERINFO();
		BANKTOBANKINFO b2bInfo = new BANKTOBANKINFO();
		remittanceMsgDetails.setBANKTOBANKINFO(b2bInfo);
		TERMSCONDITIONSINFO termConditionsInfo = new TERMSCONDITIONSINFO();
		KYCDETAILS kycDetails = new KYCDETAILS();
		remittanceMsgDetails.setKYCDETAILS(kycDetails);
		RemittanceINFO remittanceInfo = new RemittanceINFO();
		remittanceMsgDetails.setRemittanceINFO(remittanceInfo);
		CHARGERELATEDINFO chargeRelatedInfo = new CHARGERELATEDINFO();
		remittanceMsgDetails.setCHARGERELATEDINFO(chargeRelatedInfo);

		remittanceMsgDetails.setDIRECTION(PaymentSwiftConstants.OUTWARD);
		remittanceMsgDetails.setGENERATE103PLUSIND("Y".equals(remittanceDetail.getF_UBISGEN103PLUS()) ? true : false);

		b2bInfo.setBANKTOBANKINFO1(remittanceDetail.getF_UBBANKTOBANKINFO1());
		b2bInfo.setBANKTOBANKINFO2(remittanceDetail.getF_UBBANKTOBANKINFO2());
		b2bInfo.setBANKTOBANKINFO3(remittanceDetail.getF_UBBANKTOBANKINFO3());
		b2bInfo.setBANKTOBANKINFO4(remittanceDetail.getF_UBBANKTOBANKINFO4());
		b2bInfo.setBANKTOBANKINFO5(remittanceDetail.getF_UBBANKTOBANKINFO5());
		b2bInfo.setBANKTOBANKINFO6(remittanceDetail.getF_UBBANKTOBANKINFO6());
		b2bInfo.setBANKOPERATIONCODE(remittanceDetail.getF_UBBANKOPERATIONCODE());

		senderToReceiverInfo.setSENDERTORECEIVERINFO1(remittanceDetail.getF_UBSENDERTORECIEVERINFO1());
		senderToReceiverInfo.setSENDERTORECEIVERINFO2(remittanceDetail.getF_UBSENDERTORECIEVERINFO2());
		senderToReceiverInfo.setSENDERTORECEIVERINFO3(remittanceDetail.getF_UBSENDERTORECIEVERINFO3());
		senderToReceiverInfo.setSENDERTORECEIVERINFO4(remittanceDetail.getF_UBSENDERTORECIEVERINFO4());
		senderToReceiverInfo.setSENDERTORECEIVERINFO5(remittanceDetail.getF_UBSENDERTORECIEVERINFO5());
		senderToReceiverInfo.setSENDERTORECEIVERINFO6(remittanceDetail.getF_UBSENDERTORECIEVERINFO6());
		remittanceMsgDetails.setSENDERTORECEIVERINFO(senderToReceiverInfo);

		b2bInfo.setBANKINSTRUCTIONCODE(remittanceDetail.getF_UBBANKINSTRUCTIONCODE2());
		b2bInfo.setBANKINSTRUCTIONCODE2(remittanceDetail.getF_UBBANKINSTRUCTIONCODE1());
		remittanceMsgDetails.setBANKTOBANKINFO(b2bInfo);
		remittanceMsgDetails.setMESSAGENUMBER(remittanceDetail.getF_UBMESSAGENUMBER()); // senders Ref is using id

		remittanceInfo.setCHARGECODE(remittanceDetail.getF_UBCHARGECODETYPE());
		remittanceInfo.setTRANSACTIONTYPECODE(remittanceDetail.getF_UBTXNTYPECODETAG26());
		remittanceInfo.setREMITTANCEINFO1(remittanceDetail.getF_UBREMITTANCEINFO1());
		remittanceInfo.setREMITTANCEINFO2(remittanceDetail.getF_UBREMITTANCEINFO2());
		remittanceInfo.setREMITTANCEINFO3(remittanceDetail.getF_UBREMITTANCEINFO3());
		remittanceInfo.setREMITTANCEINFO4(remittanceDetail.getF_UBREMITTANCEINFO4());
		remittanceInfo.setChargeDetailAmount(
				RoundToScale.run(remittanceDetail.getF_UBPAYINGBANKCHARGE(), remittanceDetail.getF_UBCRACCCURRENCY()));
		remittanceInfo.setREMITTANCEDESCRIPTION(remittanceDetail.getF_UBREMITTANCEDESCRIPTION());
		remittanceMsgDetails.setRemittanceINFO(remittanceInfo);

		TRANSACTIONDETAISINFO tnxInfo = new TRANSACTIONDETAISINFO();
		tnxInfo.setDATEOFPROCESSING((remittanceDetail.getF_UBVALUEDATE()));
		tnxInfo.setCURRENCY(remittanceDetail.getF_UBCURRENCY());
		tnxInfo.setEXCHANGERATEFORINCOMING(remittanceDetail.getF_UBEXCHANGERATECR());
		tnxInfo.setEXCHANGERATEFOROUTGOING(remittanceDetail.getF_UBEXCHANGERATEDR());
		tnxInfo.setNARRATION(remittanceDetail.getF_UBNARRATION());
		tnxInfo.setTRANSACTIONREFERENCE(remittanceDetail.getF_UBTRANSACTIONREFERENCE());
		tnxInfo.setAPPLIEDCHARGES(
				RoundToScale.run(remittanceDetail.getF_UBCHARGES(), remittanceDetail.getF_UBDRACCCURRENCY()));
		tnxInfo.setMessagePreference(remittanceDetail.getF_UBMESSAGEPREFERENCE());

		String transactionType = getSwifttransactionDesc(remittanceDetail.getF_UBMESSAGETYPE().substring(2));
		tnxInfo.setTRANSACTIONTYPE(transactionType);
		remittanceMsgDetails.setMESSAGETYPE(remittanceDetail.getF_UBMESSAGETYPE());
		remittanceMsgDetails.setChargeCurrency(remittanceDetail.getF_UBCHARGECURRENCY());
		BENEFICIARYCUSTOMERINFO beneCustDetls = new BENEFICIARYCUSTOMERINFO();
		beneCustDetls.setBENEFICIARYCUSTIDENTCODE(remittanceDetail.getF_UBBENCUSTIDENTIFIERCODE());
		beneCustDetls.setBENEFICIARYCUSTPARTYIDENTIFIER(remittanceDetail.getF_UBBENCUSTPARTYIDENTIFIER());
		beneCustDetls.setBENEFICIARYCUSTPARTYIDENCODE(remittanceDetail.getF_UBBENCUSTPARTYIDENCLCODE());
		beneCustDetls.setBENEFICIARYCUSTTEXT1(remittanceDetail.getF_UBBENEFICIARYCUSTINFO1());
		beneCustDetls.setBENEFICIARYCUSTTEXT2(remittanceDetail.getF_UBBENEFICIARYCUSTINFO2());
		beneCustDetls.setBENEFICIARYCUSTTEXT3(remittanceDetail.getF_UBBENEFICIARYCUSTINFO3());
		beneCustDetls.setBENEFICIARYCUSTTEXT4(remittanceDetail.getF_UBBENEFICIARYCUSTINFO4());

		remittanceMsgDetails.setBENEFICIARYCUSTOMERINFO(beneCustDetls);

		String creditAccount = "";
		flag = "C";
		CREDITORDTL crdTls = new CREDITORDTL();
		crdTls.setCREDITACCOUNTID(remittanceDetail.getF_UBCREDITACCOUNT());

		CHARGERELATEDINFO chargeDetail = new CHARGERELATEDINFO();
		BigDecimal creditAmt = remittanceDetail.getF_UBCREDITAMOUNT();

		crdTls.setCREDITAMOUNT(creditAmt.setScale(2, BigDecimal.ROUND_HALF_UP));
		remittanceMsgDetails.setTRANSACTIONDETAISINFO(tnxInfo);
		remittanceMsgDetails.setCHARGERELATEDINFO(chargeDetail);

		remittanceMsgDetails.setCUSTOMERNAME(remittanceDetail.getF_UBCUSTOMERNAME());
		String debitAccount = remittanceDetail.getF_UBDEBITACCOUNT();

		BigDecimal debitAmt = BigDecimal.ZERO;
		DEBITORDTL debtr = new DEBITORDTL();
		debtr.setDEBITACCOUNTID(remittanceDetail.getF_UBDEBITACCOUNT());
		debitAmt = remittanceDetail.getF_UBDEBITAMOUNT();
		debtr.setDEBITAMOUNT(debitAmt.setScale(2, BigDecimal.ROUND_HALF_UP));

		String creditAccountCurr = CommonConstants.EMPTY_STRING;
		String debitAccountCurr = CommonConstants.EMPTY_STRING;
		GetExchangeRateSwiftRemittance exchangerate = new GetExchangeRateSwiftRemittance();
		BigDecimal expectedCreditAmt = BigDecimal.ZERO;
		BigDecimal creditExchangeRate = BigDecimal.ZERO;
		String creditExchRateType = "";
		String debitExchRateType = "";

		BigDecimal expectedDebitAmt = BigDecimal.ZERO;
		BigDecimal debitExchangeRate = BigDecimal.ZERO;
		creditAccount = remittanceDetail.getF_UBCREDITACCOUNT();

		creditAccountCurr = remittanceDetail.getF_UBCRACCCURRENCY();
		creditExchangeRate = remittanceDetail.getF_UBEXCHANGERATECR();
		creditExchRateType = remittanceDetail.getF_UBEXCHANGERATETYPECR();
		debitAccountCurr = remittanceDetail.getF_UBDRACCCURRENCY();
		debitExchangeRate = remittanceDetail.getF_UBEXCHANGERATEDR();
		debitExchRateType = remittanceDetail.getF_UBEXCHANGERATETYPEDR();
		expectedDebitAmt = debitAmt.multiply(debitExchangeRate);
		expectedCreditAmt = creditAmt.multiply(BigDecimal.ONE);
		crdTls.setEXPECTEDCREDITAMOUNT(expectedCreditAmt.setScale(2, BigDecimal.ROUND_HALF_UP));
		remittanceMsgDetails.setCREDITORDTL(crdTls);
		debtr.setDEBITAMOUNT(RoundToScale.run(expectedDebitAmt, remittanceDetail.getF_UBDRACCCURRENCY()));
		debtr.setEXPECTEDDEBITAMOUNT(RoundToScale.run(debitAmt, remittanceDetail.getF_UBDRACCCURRENCY()));
		remittanceMsgDetails.setDEBITORDTL(debtr);
		String instructedAmtCcy = !remittanceDetail.getF_UBINSTRUCTEDAMTCURRENCY().isEmpty()
				? remittanceDetail.getF_UBINSTRUCTEDAMTCURRENCY()
				: remittanceDetail.getF_UBDRACCCURRENCY();
		remittanceMsgDetails
				.setInstructedAmount(RoundToScale.run(remittanceDetail.getF_UBINSTRUCTEDAMT(), instructedAmtCcy));
		remittanceMsgDetails.setInstructedAmountCcy(instructedAmtCcy);

		remittanceMsgDetails.setExchangeRateTypeOUT(debitExchRateType);
		remittanceMsgDetails.setExchangeRateTypeIN(creditExchRateType);

		tnxInfo.setEXCHANGERATEFORINCOMING(creditExchangeRate);
		tnxInfo.setEXCHANGERATEFOROUTGOING(debitExchangeRate);

		remittanceMsgDetails.setSettlementInstrId(remittanceDetail.getF_UBSETTLEMENTINSTRUCTIONSID());

		remittanceMsgDetails.setTRANSACTIONDETAISINFO(tnxInfo);
		remittanceMsgDetails.setCrAccountCurrency(creditAccountCurr);
		remittanceMsgDetails.setDrAccountCurrency(debitAccountCurr);
		remittanceMsgDetails.setExpctCrAmountCurrency(creditAccountCurr);
		remittanceMsgDetails.setExpctDrAmountCurrency(debitAccountCurr);
		remittanceMsgDetails.setCrAmountCurrency(remittanceDetail.getF_UBCURRENCY());
		remittanceMsgDetails.setDrAmountCurrency(remittanceDetail.getF_UBCURRENCY());

		BENEFICIARYINSTDETIALS beneInsDetls = new BENEFICIARYINSTDETIALS();
		beneInsDetls.setBENEFICIARYINSTIDENTCODE(remittanceDetail.getF_UBBENINSTIDENTIFIERCODE());
		beneInsDetls.setBENEFICIARINSTYPARTYIDENTIFIER(remittanceDetail.getF_UBBENINSTPARTYIDENTIFIER());
		beneInsDetls.setBENEFICIARYINSTTEXT1(remittanceDetail.getF_UBBENEFICIARYINSTINFO1());
		beneInsDetls.setBENEFICIARYINSTTEXT2(remittanceDetail.getF_UBBENEFICIARYINSTINFO2());
		beneInsDetls.setBENEFICIARYINSTTEXT3(remittanceDetail.getF_UBBENEFICIARYINSTINFO3());
		beneInsDetls.setBENEFICIARYINSTTEXT4(remittanceDetail.getF_UBBENEFICIARYINSTINFO4());
		beneInsDetls.setBENPSRTYIDENTCLRCODE(remittanceDetail.getF_UBBENINSTPARTYIDENCLCODE());
		remittanceMsgDetails.setBENEFICIARYINSTDETIALS(beneInsDetls);

		orderingCustInfo.setORDERINGICUSTINFO1(remittanceDetail.getF_UBORDCUSTOMERINFO1());
		orderingCustInfo.setORDERINGICUSTINFO2(remittanceDetail.getF_UBORDCUSTOMERINFO2());
		orderingCustInfo.setORDERINGICUSTINFO3(remittanceDetail.getF_UBORDCUSTOMERINFO3());
		orderingCustInfo.setORDERINGICUSTINFO4(remittanceDetail.getF_UBORDCUSTOMERINFO4());
		orderingCustInfo.setORDCUSTIDENBIC(remittanceDetail.getF_UBORDCUSTINDENTIFER());
		orderingCustInfo.setORDCUSTPTYIDCODE(remittanceDetail.getF_UBORDCUSTPARTYIDENTCLCODE());
		orderingCustInfo.setORDCUSTPTYIDENACC(remittanceDetail.getF_UBORDCUSTPARTYIDENTACCTYPE());
		orderingCustInfo.setORDCUSTPTYIDENACCVALUE(remittanceDetail.getF_UBORDCUSTPARTYIDENTACC());
		orderingCustInfo.setORDCUSTPTYIDCONTRY(remittanceDetail.getF_UBORDCUSTPARTYCOUNTRY());
		orderingCustInfo.setORDCUSTPTYIDVALUE(remittanceDetail.getF_UBORDCUSTPARTYIDENTIFIER());
		remittanceMsgDetails.setORDERINGICUSTINFO(orderingCustInfo);

		orderingInstDetail.setORDERINGINSTITUTIONIDENTCODE(remittanceDetail.getF_UBORDINSTIDENTIFIER());
		orderingInstDetail.setORDERINGINSTPARTYIDENTCODE(remittanceDetail.getF_UBORDINSTPARTYIDENTIFIER());
		orderingInstDetail.setORDERINGINSTITUTIONDTL1(remittanceDetail.getF_UBORDINSTITUTEINFO1());
		orderingInstDetail.setORDERINGINSTITUTIONDTL2(remittanceDetail.getF_UBORDINSTITUTEINFO2());
		orderingInstDetail.setORDERINGINSTITUTIONDTL3(remittanceDetail.getF_UBORDINSTITUTEINFO3());
		orderingInstDetail.setORDERINGINSTITUTIONDTL4(remittanceDetail.getF_UBORDINSTITUTEINFO4());
		orderingInstDetail.setORDERINGINSTPRTYIDNTCLRCODE(remittanceDetail.getF_UBORDINSTPARTYIDENTCLCODE());
		remittanceMsgDetails.setORDERINGINSTITUTIONDTL(orderingInstDetail);

		payToPartyDetails.setPAYTOPARTYIDENTIFIER(remittanceDetail.getF_UBPAYTOPARTYIDENTIFIER());
		payToPartyDetails.setPAYTOIDENTCODE(remittanceDetail.getF_UBPAYTOIDENTIFIERCODE());
		payToPartyDetails.setPAYTOPRTYIDNTCLRCODE(remittanceDetail.getF_UBPAYTOPARTYIDENCLCODE());
		payToPartyDetails.setPAYTOTEXT1(remittanceDetail.getF_UBPAYTOINFO1());
		payToPartyDetails.setPAYTOTEXT2(remittanceDetail.getF_UBPAYTOINFO2());
		payToPartyDetails.setPAYTOTEXT3(remittanceDetail.getF_UBPAYTOINFO3());
		payToPartyDetails.setPAYTOTEXT4(remittanceDetail.getF_UBPAYTOINFO4());
		remittanceMsgDetails.setPAYTOPARTYDETAILS(payToPartyDetails);

		INTERMEDIARYDETAILS intMedInsDetls = new INTERMEDIARYDETAILS();
		intMedInsDetls.setINTERMEDIARYIDENTCODE(remittanceDetail.getF_UBINTERMDRYIDENTCODE());
		intMedInsDetls.setINTERMEDIARYTEXT1(remittanceDetail.getF_UBINTERMEDIARYINFO1());
		intMedInsDetls.setINTERMEDIARYTEXT2(remittanceDetail.getF_UBINTERMEDIARYINFO2());
		intMedInsDetls.setINTERMEDIARYTEXT3(remittanceDetail.getF_UBINTERMEDIARYINFO3());
		intMedInsDetls.setINTERMEDIARYTEXT4(remittanceDetail.getF_UBINTERMEDIARYINFO4());
		intMedInsDetls.setINTMDPRTYIDNTIFR(remittanceDetail.getF_UBINTERMDRYPARTYIDENTCODE());
		intMedInsDetls.setINTMDPRTYIDNTCLRCODE(remittanceDetail.getF_UBINTERMDRYPARTYIDENTCLCODE());
		remittanceMsgDetails.setINTERMEDIARYDETAILS(intMedInsDetls);
		remittanceMsgDetails.setKYCDETAILS(kycDetails);
		termConditionsInfo.setTERMSCONDITIONSINFO1(remittanceDetail.getF_UBTERMANDCONDITIONINF01());
		termConditionsInfo.setTERMSCONDITIONSINFO2(remittanceDetail.getF_UBTERMANDCONDITIONINF02());
		termConditionsInfo.setTERMSCONDITIONSINFO3(remittanceDetail.getF_UBTERMANDCONDITIONINF03());
		termConditionsInfo.setTERMSCONDITIONSINFO4(remittanceDetail.getF_UBTERMANDCONDITIONINF04());
		termConditionsInfo.setTERMSCONDITIONSINFO5(remittanceDetail.getF_UBTERMANDCONDITIONINF05());
		termConditionsInfo.setTERMSCONDITIONSINFO6(remittanceDetail.getF_UBTERMANDCONDITIONINF06());
		remittanceMsgDetails.setTERMSCONDITIONSINFO(termConditionsInfo);

		return remittanceMsgDetails;

	}

	/**
	 * @param accountId
	 * @return
	 */
	private String getAccountCurrency(String accountId) {
		PaymentSwiftUtils utils = new PaymentSwiftUtils();
		String accountCurrency = StringUtils.EMPTY;
		IBOAttributeCollectionFeature accountIbo = utils.getAccountDetails(accountId);
		if (null != accountIbo) {
			accountCurrency = accountIbo.getF_ISOCURRENCYCODE();
		}
		return accountCurrency;
	}

	/**
	 * @param OutputMap
	 */
	private void valiateCharges(HashMap OutputMap) {
		String errorNo = OutputMap.get("ErrorNo").toString();
		if (errorNo.equals("09718")) {
			EventsHelper.handleEvent(PaymentSwiftConstants.E_RECEIVER_CHARGE_71G_IS_NOT_EQUAL_CHARGE_AMOUNT,
					new Object[] {}, null, env);
		}
	}

	/**
	 * @param txnCurrencyCode
	 * @param branchCode
	 * @return
	 */
	private String getNostroAccountFromModuleConfig(String txnCurrencyCode, String branchCode) {
		String suspenseAccount = CommonConstants.EMPTY_STRING;
		String psuedonymName = CommonConstants.EMPTY_STRING;
		String psuedonymContext = CommonConstants.EMPTY_STRING;

		psuedonymName = DataCenterCommonUtils.readModuleConfiguration(PaymentSwiftConstants.FEX_MODULE_ID,
				PaymentSwiftConstants.NOSTRO_PSEDONYM_KEY);
		psuedonymContext = DataCenterCommonUtils.readModuleConfiguration(PaymentSwiftConstants.FEX_MODULE_ID,
                PaymentSwiftConstants.CURRENCY_PSEDONYM_CONTEXT);

		suspenseAccount = DataCenterCommonUtils.retrievePsuedonymAcctId(txnCurrencyCode, branchCode, psuedonymContext,
				psuedonymName);

		return suspenseAccount;

	}

	/**
	 * @param charge
	 * @return
	 */
	private BigDecimal getSenderChargeForDisplay(SenderCharge charge) {
		BigDecimal chargeDetailAmount = BigDecimal.ZERO;
		String senderCharge = StringUtils.EMPTY;
		String senderChargeCcy = StringUtils.EMPTY;
		if (charge != null && charge.getSenderChargeCount() != 0) {
			senderCharge = charge.getSenderCharge(0);
			if (senderCharge != null) {
				senderChargeCcy = senderCharge.substring(0, 3).trim();
				senderCharge = senderCharge.substring(3, senderCharge.length());
				chargeDetailAmount = new BigDecimal(senderCharge);
			}
		}
		if (logger.isInfoEnabled()) {
			logger.info("senderChargeCcy:::: " + senderChargeCcy + "senderCharge::::" + senderCharge);
		}
		return chargeDetailAmount;
	}

	/**
	 * 
	 * @param MsgHeader
	 * @return
	 */
	private String getDescriptionForFailedMessages(IBOUB_INF_MessageHeader MsgHeader, BankFusionEnvironment env) {
		String errorRsn = StringUtils.EMPTY;
        String params=StringUtils.EMPTY;
		if (MsgHeader.getF_MESSAGESTATUS().equals("F") && MsgHeader.getF_ERRORCODE() != 0) {
            String eventCode = String.valueOf(MsgHeader.getF_ERRORCODE());
            if (StringUtils.isNotBlank(MsgHeader.getF_ERRORCODEPARAM())) {
                String[] parameters = MsgHeader.getF_ERRORCODEPARAM().split(",");
              //remove brackets([) convert it to string
                params = Arrays.asList(parameters).toString().replace("[", "").replace("]", "");//remove brackets([) convert it to string
            }
	        
			if (eventCode.length() == 4) {
				eventCode = "7000" + String.valueOf(MsgHeader.getF_ERRORCODE());
			}
            errorRsn = BankFusionMessages.getInstance().getFormattedEventMessage(Integer.parseInt(eventCode),
                    new Object[] { params }, env.getUserSession().getUserLocale());
		}
		return errorRsn;

	}
}