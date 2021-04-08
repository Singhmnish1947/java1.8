package com.misys.ub.swift.remittance.process;

import java.math.BigDecimal;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.bankfusion.subsystem.microflow.runtime.impl.MFExecuter;
import com.misys.ub.datacenter.DataCenterCommonConstants;
import com.misys.ub.datacenter.DataCenterCommonUtils;
import com.misys.ub.payment.swift.utils.ChargeNonStpProcess;
import com.misys.ub.payment.swift.utils.ChargesDto;
import com.misys.ub.payment.swift.utils.MFInputOutPutKeys;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.misys.ub.swift.remittance.dto.ExchangeRateDto;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOTransactionScreenControl;
import com.trapedza.bankfusion.core.BFCurrencyValue;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.fatoms.GetExchangeRateDetails;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_OutRemScreenEventHandler;

import bf.com.misys.cbs.msgs.v1r0.ReadAccountRs;
import bf.com.misys.cbs.msgs.v1r0.ReadCustomerRs;
import bf.com.misys.cbs.services.CalcExchangeRateRs;
import bf.com.misys.cbs.types.Address;
import bf.com.misys.cbs.types.Currency;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;
import bf.com.misys.ub.types.remittanceprocess.CREDITORDTL;
import bf.com.misys.ub.types.remittanceprocess.DEBITORDTL;
import bf.com.misys.ub.types.remittanceprocess.ORDERINGICUSTINFO;
import bf.com.misys.ub.types.remittanceprocess.TRANSACTIONDETAISINFO;
import bf.com.misys.ub.types.remittanceprocess.UB_SWT_RemittanceProcessRq;

public class OutRemScreenEventHandler extends AbstractUB_SWT_OutRemScreenEventHandler {
	private transient final static Log LOGGER = LogFactory.getLog(OutRemScreenEventHandler.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -2785883957524395158L;
	public static final String MT103 = "103";
	public static final String SENDER_REF_PREFIX = "SW";
	public static final String NEW_BUTTON_CLICK_CONTEXT = "NEW";
	public static final String TXN_TYPE_ITEM_SELECT_CONTEXT = "TXNTYPE";
	public static final String DEBIT_ACC_INPUT_CONTEXT = "DEBITACC";
	public static final String CREBIT_ACC_INPUT_CONTEXT = "CREDITACC";
	public static final String AMOUNT_RATE_FIELDS_INPUT_CONTEXT = "POPULATEAMTS";
	public static final String INST_AMT_CURR_CHANGE_CONTEXT = "INSTCUR";
	public static final String EXT_EXCHG_RATE_CONTEXT = "GETRATE";
	public static final String GET_DEFAULT_EXCNG_RATE_GET_RATE_CONTEXT = "GETDEFAULTEXCNGRT";
	private static final String PERSONAL = "1062";
	private static final String ENTERPRISE = "1063";

	public OutRemScreenEventHandler() {

	}

	public OutRemScreenEventHandler(BankFusionEnvironment env) {
		super(env);
	}

	@Override
	public void process(BankFusionEnvironment env) {
		String context = getF_IN_screenEventContext();
		switch (context) {
		case NEW_BUTTON_CLICK_CONTEXT:
			populateValuesForNewClickEvent(env);
			break;
		case TXN_TYPE_ITEM_SELECT_CONTEXT:
			populateValuesForTXNTYPESelectEvent(env);
			break;
		case DEBIT_ACC_INPUT_CONTEXT:
			populateOrderingCustDetails(env);
			break;
		case CREBIT_ACC_INPUT_CONTEXT:
			populateDetailsOnCrAccInput(env);
			break;
		case AMOUNT_RATE_FIELDS_INPUT_CONTEXT:
			populateAmts(env);
			break;
		case INST_AMT_CURR_CHANGE_CONTEXT:
			populateAmts(env);
			break;
		/*
		 * case EXT_EXCHG_RATE_CONTEXT: populateAmtsOnExtExchgRate(env); break;
		 */
		case GET_DEFAULT_EXCNG_RATE_GET_RATE_CONTEXT:
			arriveSystemLevelExchangeRate();
			break;
		default:
			break;
		}
	}

	private void arriveSystemLevelExchangeRate() {
		UB_SWT_RemittanceProcessRq swtRqIncomingObj = getF_IN_swtRemProceesRqInput();
		PaymentSwiftUtils utils = new PaymentSwiftUtils();
		CalcExchangeRateRs calcExchgRateRs = utils.getExchangeRateAmount(swtRqIncomingObj.getExchangeRateTypeOUT(),
				BigDecimal.ZERO, swtRqIncomingObj.getInstructedAmountCcy(), swtRqIncomingObj.getDrAccountCurrency(),
				BigDecimal.ZERO, BigDecimal.ZERO);
		if (null != calcExchgRateRs) {
			TRANSACTIONDETAISINFO txnDetailInfo = swtRqIncomingObj.getTRANSACTIONDETAISINFO();
			txnDetailInfo.setEXCHANGERATEFOROUTGOING(
					calcExchgRateRs.getCalcExchRateResults().getExchangeRateDetails().getExchangeRate());
		}
		setF_OUT_swtRemProcessOutput(swtRqIncomingObj);
	}

	/*
	 * private void populateAmtsOnExtExchgRate(BankFusionEnvironment env) {
	 * UB_SWT_RemittanceProcessRq swtMessageDtls = getF_IN_swtRemProceesRqInput();
	 * 
	 * //the below piece of code is make sure that the flag which triggered this
	 * service is unchecked so that if the //user clicks on get rate again, this
	 * service call would be made. and to set the context for exchangeRate amount
	 * calculation String drExchgRtMDIndtcr = swtMessageDtls.getEXTENTION1(); String
	 * crExchgRtMDIndtcr = swtMessageDtls.getEXTENTION2(); boolean crReCalcExchgRate
	 * = false; boolean drReCalcExchgRate = false; if
	 * (!StringUtils.isBlank(crExchgRtMDIndtcr)) {
	 * setF_OUT_disableCrExchgRtDtls(false); crReCalcExchgRate = true; }
	 * 
	 * if (!StringUtils.isBlank(drExchgRtMDIndtcr)) {
	 * setF_OUT_disableDrExchgRtDtls(false); drReCalcExchgRate = true; }
	 * 
	 * 
	 * populateAmountFieldsAndExchangeRates(swtMessageDtls, env, crReCalcExchgRate,
	 * drReCalcExchgRate, true);
	 * 
	 * // SHA BEN OUR charge computation if (null !=
	 * swtMessageDtls.getDEBITORDTL().getEXPECTEDDEBITAMOUNT() && null !=
	 * swtMessageDtls.getCREDITORDTL().getEXPECTEDCREDITAMOUNT()) { ChargesDto
	 * chargeDto = prepareChargeData(swtMessageDtls);
	 * swtMessageDtls.getDEBITORDTL().setEXPECTEDDEBITAMOUNT(chargeDto.
	 * getDebitAmount().getAmount());
	 * swtMessageDtls.getCREDITORDTL().setEXPECTEDCREDITAMOUNT(chargeDto.
	 * getCreditAmount().getAmount()); BigDecimal payChrgAmt =
	 * chargeDto.getPayingBankChg().getAmount();
	 * swtMessageDtls.getRemittanceINFO().setChargeDetailAmount(payChrgAmt);
	 * swtMessageDtls.setChargeCurrency(chargeDto.getPayingBankChg().
	 * getIsoCurrencyCode()); }
	 * 
	 * setF_OUT_swtRemProcessOutput(swtMessageDtls);
	 * 
	 * }
	 */

	/**
	 * Populate amounts
	 */
	private void populateAmts(BankFusionEnvironment env) {
		UB_SWT_RemittanceProcessRq swtMessageDtls = getF_IN_swtRemProceesRqInput();
		// UB_SWT_RemittanceProcessRq swtMessageDtlsOut = new
		// UB_SWT_RemittanceProcessRq();
		boolean reCalcExchgRate = true;
		if (!INST_AMT_CURR_CHANGE_CONTEXT.equals(getF_IN_screenEventContext())) {
			checkForExchgTolBreach(swtMessageDtls, env);
			reCalcExchgRate = false;
		}
		populateAmountFieldsAndExchangeRates(swtMessageDtls, env, reCalcExchgRate, reCalcExchgRate, false);

		// SHA BEN OUR charge computation
		if (null != swtMessageDtls.getDEBITORDTL().getEXPECTEDDEBITAMOUNT()
				&& null != swtMessageDtls.getCREDITORDTL().getEXPECTEDCREDITAMOUNT()) {
			ChargesDto chargeDto = prepareChargeData(swtMessageDtls);
			swtMessageDtls.getDEBITORDTL().setEXPECTEDDEBITAMOUNT(chargeDto.getDebitAmount().getAmount());
			swtMessageDtls.getCREDITORDTL().setEXPECTEDCREDITAMOUNT(chargeDto.getCreditAmount().getAmount());
			BigDecimal payChrgAmt = chargeDto.getPayingBankChg().getAmount();
			swtMessageDtls.getRemittanceINFO().setChargeDetailAmount(payChrgAmt);
			swtMessageDtls.setChargeCurrency(chargeDto.getPayingBankChg().getIsoCurrencyCode());
		}

		String instAmountCurr = swtMessageDtls.getInstructedAmountCcy();
		String creditAccCurr = swtMessageDtls.getCrAccountCurrency();
		String debitAccCurr = swtMessageDtls.getDrAccountCurrency();
		if (StringUtils.isBlank(instAmountCurr) || instAmountCurr.equals(creditAccCurr)) {
			setF_OUT_disableCrExchgRtDtls(true);
		} else {
			setF_OUT_disableCrExchgRtDtls(false);
		}

		if (StringUtils.isBlank(instAmountCurr) || instAmountCurr.equals(debitAccCurr)) {
			setF_OUT_disableDrExchgRtDtls(true);
		} else {
			setF_OUT_disableDrExchgRtDtls(false);
		}

		setF_OUT_swtRemProcessOutput(swtMessageDtls);

	}

	private void checkForExchgTolBreach(UB_SWT_RemittanceProcessRq swtMessageDtls, BankFusionEnvironment env) {
		// TODO Auto-generated method stub
		if (!StringUtils.isBlank(swtMessageDtls.getDrAccountCurrency())) {
			GetExchangeRateDetails exchangeRateDetails = new GetExchangeRateDetails();
			boolean isWithinTolerance = exchangeRateDetails.isExchangeRateWithinCurrencyTolerance(
					getF_IN_origDrExchgRt(), swtMessageDtls.getTRANSACTIONDETAISINFO().getEXCHANGERATEFOROUTGOING(),
					null, swtMessageDtls.getInstructedAmountCcy(), null, null, env);
			if (!isWithinTolerance) {
				String[] args = {};
				PaymentSwiftUtils.handleEvent(SwiftEventCodes.E_EXCHG_RATE_NOT_WITHIN_TOL, args);
			}
		}
		if (!StringUtils.isBlank(swtMessageDtls.getCrAccountCurrency())) {
			GetExchangeRateDetails exchangeRateDetails = new GetExchangeRateDetails();
			boolean isWithinTolerance = exchangeRateDetails.isExchangeRateWithinCurrencyTolerance(
					getF_IN_origCrExchgRt(), swtMessageDtls.getTRANSACTIONDETAISINFO().getEXCHANGERATEFORINCOMING(),
					null, swtMessageDtls.getInstructedAmountCcy(), null, null, env);
			if (!isWithinTolerance) {
				String[] args = {};
				PaymentSwiftUtils.handleEvent(SwiftEventCodes.E_EXCHG_RATE_NOT_WITHIN_TOL, args);
			}
		}

	}

	/**
	 * @param b
	 * @param swtMessageDtlsOut
	 * @param swtMessageDtlsInput
	 */
	private void populateAmountFieldsAndExchangeRates(UB_SWT_RemittanceProcessRq swtMessageDtls,
			BankFusionEnvironment env, boolean calcCrExchgRate, boolean calcDrExchgrate, boolean isFromExtExchgRt) {
		Currency instructedAmt = new Currency();
		instructedAmt.setAmount(swtMessageDtls.getInstructedAmount());
		instructedAmt.setIsoCurrencyCode(swtMessageDtls.getInstructedAmountCcy());

		// credit exchangeRate details
		CREDITORDTL creditDtls = (null == swtMessageDtls.getCREDITORDTL()) ? new CREDITORDTL()
				: swtMessageDtls.getCREDITORDTL();
		if (StringUtils.isNotEmpty(swtMessageDtls.getCrAccountCurrency())) {

			BigDecimal crExchgRt, crAmt;
			ExchangeRateDto creditExchDtls;
			BigDecimal crExchgRtScreen = swtMessageDtls.getTRANSACTIONDETAISINFO().getEXCHANGERATEFORINCOMING();

			if (calcCrExchgRate && isFromExtExchgRt) {
				// get the exchange rate
				crAmt = PaymentSwiftUtils.calcExchgAmt(instructedAmt.getAmount(), crExchgRtScreen,
						swtMessageDtls.getEXTENTION2(), swtMessageDtls.getCrAccountCurrency());
				crExchgRt = crExchgRtScreen;
			} else if (BigDecimal.ZERO.compareTo(crExchgRtScreen) >= 0 || calcCrExchgRate) {
				creditExchDtls = getCreditExchRateDetails(instructedAmt.getIsoCurrencyCode(),
						swtMessageDtls.getCrAccountCurrency(), swtMessageDtls.getExchangeRateTypeIN(),
						instructedAmt.getAmount(), BigDecimal.ZERO);
				crAmt = creditExchDtls.getCreditAmount();
				crExchgRt = creditExchDtls.getCreditExchangeRate();
			} else {
				// get the exchange rate
				creditExchDtls = getCreditExchRateDetails(instructedAmt.getIsoCurrencyCode(),
						swtMessageDtls.getCrAccountCurrency(), swtMessageDtls.getExchangeRateTypeIN(),
						instructedAmt.getAmount(), crExchgRtScreen);
				crAmt = creditExchDtls.getCreditAmount();
				crExchgRt = crExchgRtScreen;
			}

			//new transaction
			if(StringUtils.isBlank(swtMessageDtls.getREMITTANCE_ID())) {
			    swtMessageDtls.getTRANSACTIONDETAISINFO().setEXCHANGERATEFORINCOMING(crExchgRt);
			}else {
			    swtMessageDtls.getTRANSACTIONDETAISINFO().setEXCHANGERATEFORINCOMING(crExchgRtScreen);
			}

			creditDtls.setEXPECTEDCREDITAMOUNT(crAmt);
			swtMessageDtls.setExpctCrAmountCurrency(swtMessageDtls.getCrAccountCurrency());
		}

		// debit exchangerate details
		DEBITORDTL debitDtls = (null == swtMessageDtls.getDEBITORDTL()) ? new DEBITORDTL()
				: swtMessageDtls.getDEBITORDTL();
		if (StringUtils.isNotEmpty(swtMessageDtls.getDrAccountCurrency())) {

			BigDecimal drExchgRt, drAmt;
			ExchangeRateDto debitExchDtls;
			BigDecimal drExchgRtScreen = swtMessageDtls.getTRANSACTIONDETAISINFO().getEXCHANGERATEFOROUTGOING();

			if (calcDrExchgrate && isFromExtExchgRt) {
				// get the exchange rate
				/*
				 * debitExchDtls = getDebitExchRateDetails(instructedAmt.getIsoCurrencyCode(),
				 * swtMessageDtls.getDrAccountCurrency(),
				 * swtMessageDtls.getExchangeRateTypeOUT(), instructedAmt.getAmount(),
				 * drExchgRtScreen,swtMessageDtls.getEXTENTION1());
				 */
				drAmt = PaymentSwiftUtils.calcExchgAmt(instructedAmt.getAmount(), drExchgRtScreen,
						swtMessageDtls.getEXTENTION1(), swtMessageDtls.getDrAccountCurrency());
				drExchgRt = drExchgRtScreen;
			} else if (BigDecimal.ZERO.compareTo(drExchgRtScreen) >= 0 || calcDrExchgrate) {
				debitExchDtls = getDebitExchRateDetails(instructedAmt.getIsoCurrencyCode(),
						swtMessageDtls.getDrAccountCurrency(), swtMessageDtls.getExchangeRateTypeOUT(),
						instructedAmt.getAmount(), BigDecimal.ZERO);
				drAmt = debitExchDtls.getDebitAmount();
				drExchgRt = debitExchDtls.getDebitExchangeRate();
			} else {
				// get the exchange rate
				debitExchDtls = getDebitExchRateDetails(instructedAmt.getIsoCurrencyCode(),
						swtMessageDtls.getDrAccountCurrency(), swtMessageDtls.getExchangeRateTypeOUT(),
						instructedAmt.getAmount(), drExchgRtScreen);
				drAmt = debitExchDtls.getDebitAmount();
				drExchgRt = drExchgRtScreen;
			}

			swtMessageDtls.getTRANSACTIONDETAISINFO().setEXCHANGERATEFOROUTGOING(drExchgRt);

			debitDtls.setEXPECTEDDEBITAMOUNT(drAmt);
			// swtMessageDtlsOut.setDrAmountCurrency(swtMessageDtlsInput.getDrAccountCurrency());
			swtMessageDtls.setExpctDrAmountCurrency(swtMessageDtls.getDrAccountCurrency());
		}

		// swtMessageDtls.setTRANSACTIONDETAISINFO(txnDtls);
		swtMessageDtls.setCREDITORDTL(creditDtls);
		swtMessageDtls.setDEBITORDTL(debitDtls);
	}

	/**
	 * @param env
	 */
	private void populateDetailsOnCrAccInput(BankFusionEnvironment env) {
		UB_SWT_RemittanceProcessRq swtMessageDtls = getF_IN_swtRemProceesRqInput();
	    BigDecimal chargeAmt = BigDecimal.ZERO;

		// getting the account currency for the entered credit account
		String creditAcc = swtMessageDtls.getCREDITORDTL().getCREDITACCOUNTID();
		ReadAccountRs readAccountRs = DataCenterCommonUtils.readAccount(creditAcc);
		RsHeader rsHeader = readAccountRs.getRsHeader();
		if (rsHeader != null && rsHeader.getStatus() != null) {
			MessageStatus status = rsHeader.getStatus();
			if (status.getOverallStatus().equals(PaymentSwiftConstants.ERROR_STATUS)) {
				raiseError(rsHeader, env);
			}
		}
		String creditAccCurr = readAccountRs.getAccountDetails().getAccountInfo().getAcctBasicDetails().getCurrency();

		// setting the account currency
		swtMessageDtls.setCrAccountCurrency(creditAccCurr);

		// calling the method to populate the amount values
		swtMessageDtls.setCrAccountCurrency(creditAccCurr);
		populateAmountFieldsAndExchangeRates(swtMessageDtls, env, true, false, false);

		// code to populate the ub charges based on the transaction currency and debit
		// account
		String txnType = swtMessageDtls.getTRANSACTIONDETAISINFO().getTRANSACTIONTYPE();
		String debitTxnCode = SwiftRemittanceMessageHelper.getTransactioncodeFromModuleConfig(txnType);
		String debitAcc = swtMessageDtls.getDEBITORDTL().getDEBITACCOUNTID();
		//new transaction
        if (StringUtils.isBlank(swtMessageDtls.getREMITTANCE_ID())) {
            HashMap onlineCharges = fetchOnlinecharges(debitAcc, creditAccCurr, debitTxnCode,
                    swtMessageDtls.getDEBITORDTL().getEXPECTEDDEBITAMOUNT(), env);
        
            if (onlineCharges != null) {
                // charge amount
                chargeAmt = (BigDecimal) onlineCharges.get("CONSOLIDATEDCHARGEAMT");
                chargeAmt = chargeAmt.add((BigDecimal) onlineCharges.get("CONSOLIDATEDTAXAMT"));
                VectorTable vector = (VectorTable) onlineCharges.get("RESULT");
                if (vector.size() != 0) {
                    setF_OUT_ChargeVector(vector);
                }
            }
        }else {
            chargeAmt=swtMessageDtls.getTRANSACTIONDETAISINFO().getAPPLIEDCHARGES();
        }
		// essence charges
		swtMessageDtls.getTRANSACTIONDETAISINFO().setAPPLIEDCHARGES(chargeAmt);
		// SHA BEN OUR charge computation
		if (null != swtMessageDtls.getCREDITORDTL().getEXPECTEDCREDITAMOUNT()) {
			ChargesDto chargeDto = prepareChargeData(swtMessageDtls);
			swtMessageDtls.getDEBITORDTL().setEXPECTEDDEBITAMOUNT(chargeDto.getDebitAmount().getAmount());
			swtMessageDtls.getCREDITORDTL().setEXPECTEDCREDITAMOUNT(chargeDto.getCreditAmount().getAmount());
			BigDecimal payChrgAmt = chargeDto.getPayingBankChg().getAmount();
			swtMessageDtls.getRemittanceINFO().setChargeDetailAmount(payChrgAmt);
			swtMessageDtls.setChargeCurrency(chargeDto.getPayingBankChg().getIsoCurrencyCode());
		}

		String instAmountCurr = swtMessageDtls.getInstructedAmountCcy();
		if (StringUtils.isBlank(instAmountCurr) || instAmountCurr.equals(creditAccCurr)) {
			setF_OUT_disableCrExchgRtDtls(true);
		} else {
			setF_OUT_disableCrExchgRtDtls(false);
		}
		setF_OUT_swtRemProcessOutput(swtMessageDtls);
	}

	/**
	 * @param env
	 */
	private void populateOrderingCustDetails(BankFusionEnvironment env) {
		UB_SWT_RemittanceProcessRq swtMessageDtls = getF_IN_swtRemProceesRqInput();
		// UB_SWT_RemittanceProcessRq swtMessageDtlsOut = new
		// UB_SWT_RemittanceProcessRq();
		// RemittanceINFO remittanceINFO = swtMessageDtls.getRemittanceINFO();
		// swtMessageDtlsOut.setRemittanceINFO(remittanceINFO);
		String txnType = swtMessageDtls.getTRANSACTIONDETAISINFO().getTRANSACTIONTYPE();
		// getting customerID and customer name and accCurrency
		String debitAcc = swtMessageDtls.getDEBITORDTL().getDEBITACCOUNTID();
		ReadAccountRs readAccountRs = DataCenterCommonUtils.readAccount(debitAcc);
		String actualBenName="";
		RsHeader rsHeader = readAccountRs.getRsHeader();
		if (rsHeader != null && rsHeader.getStatus() != null) {
			MessageStatus status = rsHeader.getStatus();
			if (status.getOverallStatus().equals(PaymentSwiftConstants.ERROR_STATUS)) {
				raiseError(rsHeader, env);
			}
		}
		String custID = readAccountRs.getAccountDetails().getAccountInfo().getAcctBasicDetails()
				.getCustomerShortDetails().getCustomerId();
		HashMap<String,Object> custAddMap=getCustomerName(custID);
		if(custAddMap != null && custAddMap.get("CUSTNAME")!=null) {
			actualBenName=custAddMap.get("CUSTNAME").toString();
		}
		
		String debitAccCurr = readAccountRs.getAccountDetails().getAccountInfo().getAcctBasicDetails().getCurrency();

		// setting the values
		ORDERINGICUSTINFO ordCustInfo = new ORDERINGICUSTINFO();
		if (MT103.equals(txnType)) {
			ordCustInfo.setORDCUSTPTYIDENACC("A");
			ordCustInfo.setORDCUSTPTYIDENACCVALUE(debitAcc);
			Address readCustAddRs = DataCenterCommonUtils.readAddressDetails(custID,DataCenterCommonConstants.SWT_ADDRESS_TYPE);
			if (null != readCustAddRs
					&& null != readCustAddRs.getAddressLine1() && !readCustAddRs.getAddressLine1().isEmpty()) {
				ordCustInfo.setORDERINGICUSTINFO1(readCustAddRs.getAddressLine1());
				ordCustInfo.setORDERINGICUSTINFO2(readCustAddRs.getAddressLine2());
				ordCustInfo.setORDERINGICUSTINFO3(readCustAddRs.getAddressLine3());
				ordCustInfo.setORDERINGICUSTINFO4(readCustAddRs.getAddressLine4());

			} else {
				
					if (custAddMap != null && custAddMap.get("ADDRESSDTLS")!=null) {
						ordCustInfo.setORDERINGICUSTINFO1(actualBenName);
						ordCustInfo.setORDERINGICUSTINFO2(((Address) custAddMap.get("ADDRESSDTLS")).getAddressLine1());
						ordCustInfo.setORDERINGICUSTINFO3(((Address) custAddMap.get("ADDRESSDTLS")).getAddressLine2());
						ordCustInfo.setORDERINGICUSTINFO4(((Address) custAddMap.get("ADDRESSDTLS")).getAddressLine3());

						
					}

				}
			
		}
		swtMessageDtls.setORDERINGICUSTINFO(ordCustInfo);
		swtMessageDtls.setDrAccountCurrency(debitAccCurr);
		swtMessageDtls.setCUSTOMERNAME(actualBenName);

		// calling the method to populate the amount values
		// swtMessageDtls.setDrAccountCurrency(debitAccCurr);
		populateAmountFieldsAndExchangeRates(swtMessageDtls, env, false, true, false);
		// swtMessageDtlsOut.setInstructedAmount(swtMessageDtls.getInstructedAmount());
		// swtMessageDtlsOut.setInstructedAmountCcy(swtMessageDtls.getInstructedAmountCcy());

		// charges
		BigDecimal chargeAmt = BigDecimal.ZERO;
		String crAccountCurr = swtMessageDtls.getCrAccountCurrency();
		if (!StringUtils.isBlank(crAccountCurr)) {
			String debitTxnCode = SwiftRemittanceMessageHelper.getTransactioncodeFromModuleConfig(txnType);
			HashMap onlineCharges = fetchOnlinecharges(debitAcc, crAccountCurr, debitTxnCode,
					swtMessageDtls.getDEBITORDTL().getEXPECTEDDEBITAMOUNT(), env);

			if (onlineCharges != null) {
				// charge amount
				chargeAmt = (BigDecimal) onlineCharges.get("CONSOLIDATEDCHARGEAMT");
				chargeAmt = chargeAmt.add((BigDecimal) onlineCharges.get("CONSOLIDATEDTAXAMT"));
				VectorTable vector = (VectorTable) onlineCharges.get("RESULT");
				if (vector.size() != 0) {
					setF_OUT_ChargeVector(vector);
				}
			}
		}
		// essence charges
		swtMessageDtls.getTRANSACTIONDETAISINFO().setAPPLIEDCHARGES(chargeAmt);
		// swtMessageDtlsOut.setCrAccountCurrency(swtMessageDtls.getCrAccountCurrency());

		// SHA BEN OUR charge computation - this cannot be moved to
		// populateAmountFieldsAndExchangeRates as UBCharge calculation is prerequisite
		// for the
		// below code
		if (null != swtMessageDtls.getDEBITORDTL().getEXPECTEDDEBITAMOUNT()
				&& null != swtMessageDtls.getCREDITORDTL().getEXPECTEDCREDITAMOUNT()) {
			ChargesDto chargeDto = prepareChargeData(swtMessageDtls);
			swtMessageDtls.getDEBITORDTL().setEXPECTEDDEBITAMOUNT(chargeDto.getDebitAmount().getAmount());
			swtMessageDtls.getCREDITORDTL().setEXPECTEDCREDITAMOUNT(chargeDto.getCreditAmount().getAmount());
			BigDecimal payingBankChgrAmt = chargeDto.getPayingBankChg().getAmount();
			swtMessageDtls.getRemittanceINFO().setChargeDetailAmount(payingBankChgrAmt);
			BigDecimal ubChargeAmt = chargeDto.getUbCharges().getAmount();
			swtMessageDtls.getTRANSACTIONDETAISINFO().setAPPLIEDCHARGES(ubChargeAmt);
			swtMessageDtls.setChargeCurrency(chargeDto.getPayingBankChg().getIsoCurrencyCode());
		}
		String instAmountCurr = swtMessageDtls.getInstructedAmountCcy();
		if (StringUtils.isBlank(instAmountCurr) || instAmountCurr.equals(debitAccCurr)) {
			setF_OUT_disableDrExchgRtDtls(true);
		} else {
			setF_OUT_disableDrExchgRtDtls(false);
		}
		setF_OUT_swtRemProcessOutput(swtMessageDtls);
	}

	/**
	 * @param rsHeader
	 * @param env
	 */
	private void raiseError(RsHeader rsHeader, BankFusionEnvironment env) {

		SubCode[] codes = rsHeader.getStatus().getCodes();
		if (codes != null) {
			for (SubCode code : codes) {
				Integer eventCode = Integer.parseInt(code.getCode());
				String eventParam = PaymentSwiftUtils.getEventParameter(rsHeader);
				Object[] eventParams = new Object[] { eventParam };
				EventsHelper.handleEvent(eventCode, eventParams, null, env);
			}
		}

	}

	/**
	 * @param env
	 */
	private void populateValuesForTXNTYPESelectEvent(BankFusionEnvironment env) {
		UB_SWT_RemittanceProcessRq swtMessageDtls = getF_IN_swtRemProceesRqInput();
		// UB_SWT_RemittanceProcessRq swtMessageDtlsOut = new
		// UB_SWT_RemittanceProcessRq();

		// deciding the value for msgPref
		String txnType = swtMessageDtls.getTRANSACTIONDETAISINFO().getTRANSACTIONTYPE();
		TRANSACTIONDETAISINFO txnDtls = swtMessageDtls.getTRANSACTIONDETAISINFO();
		if (MT103.equals(txnType)) {
			txnDtls.setMessagePreference("SERIAL");
			setF_OUT_disableCustToCustFields(false);
		} else {
			txnDtls.setMessagePreference("");
			setF_OUT_disableCustToCustFields(true);
			swtMessageDtls.setGENERATE103PLUSIND(false);
			// added to set the Suppress Instructed Amount field value to false on selection
			// of Bank to Bank transfer
			txnDtls.setShowAsInstructed(false);
		}
		swtMessageDtls.setTRANSACTIONDETAISINFO(txnDtls);
		// setting the exchange rate type for both Dr and Cr
		setExchangeRateTypes(env, txnType, swtMessageDtls);

		// setting the fatom output
		setF_OUT_swtRemProcessOutput(swtMessageDtls);

	}

	/**
	 * @param env
	 */
	private void populateValuesForNewClickEvent(BankFusionEnvironment env) {
		UB_SWT_RemittanceProcessRq swtMessageDtlsInput = getF_IN_swtRemProceesRqInput();

		// only performing the fields value population if the direction is Outwards
		if (null != swtMessageDtlsInput && null != swtMessageDtlsInput.getDIRECTION()
				&& "O".equals(swtMessageDtlsInput.getDIRECTION())) {
			UB_SWT_RemittanceProcessRq swtMessageDtlsOut = new UB_SWT_RemittanceProcessRq();

			// generating the sender reference
			long time = SystemInformationManager.getInstance().getBFBusinessDateTime().getTime();
			String senderRef = SENDER_REF_PREFIX + time;

			// setting the transaction details
			TRANSACTIONDETAISINFO txnDtls = new TRANSACTIONDETAISINFO();
			txnDtls.setDATEOFPROCESSING(SystemInformationManager.getInstance().getBFBusinessDate());
			// txnDtls.setEXCHANGERATEFORINCOMING(CommonConstants.BIGDECIMAL_ZERO);
			// txnDtls.setEXCHANGERATEFOROUTGOING(CommonConstants.BIGDECIMAL_ZERO);
			txnDtls.setMessagePreference("SERIAL");
			txnDtls.setTRANSACTIONTYPE(MT103);
			txnDtls.setTRANSACTIONREFERENCE(senderRef);
			txnDtls.setNARRATION(senderRef);
			swtMessageDtlsOut.setTRANSACTIONDETAISINFO(txnDtls);
			swtMessageDtlsOut.setMESSAGENUMBER(senderRef);

			// defaulting the instructed amount and currency code
			BFCurrencyValue scaledAmount = new BFCurrencyValue(getF_IN_baseCurrCode(), CommonConstants.BIGDECIMAL_ZERO,
					BankFusionThreadLocal.getUserId());
			swtMessageDtlsOut.setInstructedAmount(scaledAmount.getAmount());
			swtMessageDtlsOut.setInstructedAmountCcy(getF_IN_baseCurrCode());

			// setting the exchange rate type for both Dr and Cr
			setExchangeRateTypes(env, MT103, swtMessageDtlsOut);

			setF_OUT_disableDrExchgRtDtls(true);
			setF_OUT_disableCrExchgRtDtls(true);
			// setting the fatom output
			setF_OUT_swtRemProcessOutput(swtMessageDtlsOut);
		}

	}

	/**
	 * @param env
	 * @param msgType
	 * @param swtMessageDtlsOut
	 */
	private void setExchangeRateTypes(BankFusionEnvironment env, String msgType,
			UB_SWT_RemittanceProcessRq swtMessageDtlsOut) {

		// getting the debit MIS txn code
		String debitMISTxnCode = SwiftRemittanceMessageHelper.getTransactioncodeFromModuleConfig(msgType);
		String creditMISTxnCode = StringUtils.EMPTY;
		String drExRateType = CommonConstants.EMPTY_STRING;

		// setting the debit exchange rate type
		if (!StringUtils.isBlank(debitMISTxnCode)) {
			drExRateType = getExchangeRateType(debitMISTxnCode);
		} else {
			Integer eventNumber = 20020354;
			String paramString = "Transaction code for MT" + msgType;
			Object[] eventParams = new Object[] { paramString };
			EventsHelper.handleEvent(eventNumber, eventParams, null, env);
		}
		swtMessageDtlsOut.setExchangeRateTypeOUT(StringUtils.isEmpty(drExRateType) ? "SPOT" : drExRateType);

		// getting the credit MIS txn code
		IBOTransactionScreenControl txnScreenCtrl = SwiftRemittanceMessageHelper
				.getTransactionScreenControl(debitMISTxnCode);
		if (txnScreenCtrl != null && !StringUtils.isBlank(txnScreenCtrl.getF_CONTRATRANSACTIONCODE())) {
			creditMISTxnCode = txnScreenCtrl.getF_CONTRATRANSACTIONCODE();
		} else {
			EventsHelper.handleEvent(SwiftEventCodes.E_INVALID_CREDIT_TRANSACTION_CODE, new Object[] {}, null, env);
		}

		// setting the credit exchange rate type
		String crExRateType = getExchangeRateType(creditMISTxnCode);
		swtMessageDtlsOut.setExchangeRateTypeIN(StringUtils.isEmpty(crExRateType) ? "SPOT" : crExRateType);
	}

	/**
	 * @param transactionCode
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private String getExchangeRateType(String transactionCode) {
		String exchangeRateType = StringUtils.EMPTY;
		IBOMisTransactionCodes misTransactionCodes = SwiftRemittanceMessageHelper
				.getMisTransactionCodes(transactionCode);
		if (misTransactionCodes != null && !StringUtils.isBlank(misTransactionCodes.getF_EXCHANGERATETYPE())) {
			exchangeRateType = misTransactionCodes.getF_EXCHANGERATETYPE();
		} else {
			EventsHelper.handleEvent(SwiftEventCodes.E_BT_FEX_EXGRATE_TYPE_NOTFOUND_CB05, new Object[] {}, null,
					BankFusionThreadLocal.getBankFusionEnvironment());
		}

		return exchangeRateType;
	}

	/**
	 * @param debitAccountId
	 * @param debitAccountCcy
	 * @param debitTxnCode
	 * @param debitExchangeRate
	 * @param paymentCcy
	 * @param env
	 * @return
	 */
	private HashMap fetchOnlinecharges(String debitAccountId, String debitAccountCcy, String debitTxnCode,
			BigDecimal txnAmount, BankFusionEnvironment env) {
		HashMap inputParams = new HashMap();
		inputParams.put(MFInputOutPutKeys.UB_CHG_FUNDINGACCOUNT, debitAccountId);
		inputParams.put(MFInputOutPutKeys.postingMessageAccountId_1, debitAccountId);
		inputParams.put(MFInputOutPutKeys.postingMessageISOCurrencyCode_1, debitAccountCcy);
		inputParams.put(MFInputOutPutKeys.postingMessageTransactionAmount_1, txnAmount);
		inputParams.put(MFInputOutPutKeys.UB_CHG_TxnCurrency, debitAccountCcy);
		inputParams.put(MFInputOutPutKeys.postingMessageTransactionCode_1, debitTxnCode);
		HashMap outputParams = MFExecuter.executeMF(MFInputOutPutKeys.UB_CHG_CalculateOnlineCharges_SRV,
				BankFusionThreadLocal.getBankFusionEnvironment(), inputParams);
		return outputParams;
	}

	/**
	 * logic to get the debit exchange rate details
	 * 
	 * @param instructedCcy
	 * @param debitCcy
	 * @param debitExchRateType
	 * @param inputAmount
	 * @param exchangeRate
	 * @return
	 */
	@SuppressWarnings("unused")
	public ExchangeRateDto getDebitExchRateDetails(String instructedCcy, String debitCcy, String debitExchRateType,
			BigDecimal inputAmount, BigDecimal exchangeRate) {
		ExchangeRateDto exchgDto = new ExchangeRateDto();
		PaymentSwiftUtils utils = new PaymentSwiftUtils();
		if (LOGGER.isInfoEnabled())
			LOGGER.info("IN getDebitExchRateDetails ::::" + BankFusionThreadLocal.getUserSession().getUserId());
		// if instructedccy same as debit ccy then set the debit exchange rate)
		if (instructedCcy.equals(debitCcy)) {
			exchgDto.setDebitExchangeRate(BigDecimal.ONE);
			exchgDto.setDebitAmount(inputAmount);
		} else {
			CalcExchangeRateRs calcExchgRateRs = utils.getExchangeRateAmount(debitExchRateType, inputAmount,
					instructedCcy, debitCcy, BigDecimal.ZERO, exchangeRate);
			if (calcExchgRateRs != null) {
				exchgDto.setDebitExchangeRate(
						calcExchgRateRs.getCalcExchRateResults().getExchangeRateDetails().getExchangeRate());
				exchgDto.setDebitAmount(calcExchgRateRs.getCalcExchRateResults().getSellAmountDetails().getAmount());
			}
		}
		exchgDto.setDebitCcy(debitCcy);
		exchgDto.setDebitExchangeType(debitExchRateType);
		exchgDto.setInstructedCcy(instructedCcy);
		return exchgDto;
	}

	/**
	 * 
	 * logic to get the credit exchange rate details
	 * 
	 * @param instructedCcy
	 * @param creditCcy
	 * @param creditExchRateType
	 * @param inputAmount
	 * @param exchangeRate
	 * @return
	 */
	@SuppressWarnings("unused")
	public ExchangeRateDto getCreditExchRateDetails(String instructedCcy, String creditCcy, String creditExchRateType,
			BigDecimal inputAmount, BigDecimal exchangeRate) {
		ExchangeRateDto exchgDto = new ExchangeRateDto();
		PaymentSwiftUtils utils = new PaymentSwiftUtils();

		// if instructedccy same as credit ccy then set the credit exchange rate
		if (instructedCcy.equals(creditCcy)) {
			exchgDto.setCreditExchangeRate(BigDecimal.ONE);
			exchgDto.setCreditAmount(inputAmount);
		} else {
			CalcExchangeRateRs calcExchgRateRs = utils.getExchangeRateAmount(creditExchRateType, inputAmount,
					instructedCcy, creditCcy, BigDecimal.ZERO, exchangeRate);
			if (calcExchgRateRs != null) {
				exchgDto.setCreditExchangeRate(
						calcExchgRateRs.getCalcExchRateResults().getExchangeRateDetails().getExchangeRate());
				exchgDto.setCreditAmount(calcExchgRateRs.getCalcExchRateResults().getSellAmountDetails().getAmount());
			}
		}
		exchgDto.setCreditCcy(creditCcy);
		exchgDto.setCreditExchangeType(creditExchRateType);
		exchgDto.setInstructedCcy(instructedCcy);
		return exchgDto;
	}

	/**
	 * @param swtMessageDtls
	 * @return
	 */
	private ChargesDto prepareChargeData(UB_SWT_RemittanceProcessRq swtMessageDtls) {
		ChargeNonStpProcess nonStpCharge = new ChargeNonStpProcess();
		PaymentSwiftUtils utils = new PaymentSwiftUtils();
		ChargesDto chargesDto = new ChargesDto();

		// instructed amount
		Currency instructedAmount = new Currency();
		instructedAmount.setAmount(swtMessageDtls.getInstructedAmount());
		instructedAmount.setIsoCurrencyCode(swtMessageDtls.getInstructedAmountCcy());
		chargesDto.setInstructedAmount(instructedAmount);

		Currency debitAmount = new Currency();
		// if instructed amt currency same as debit account currency
		if (swtMessageDtls.getInstructedAmountCcy().equals(swtMessageDtls.getDrAccountCurrency())) {
			debitAmount.setAmount(swtMessageDtls.getInstructedAmount());
			debitAmount.setIsoCurrencyCode(!StringUtils.isBlank(swtMessageDtls.getInstructedAmountCcy())
					? swtMessageDtls.getInstructedAmountCcy()
					: StringUtils.EMPTY);
		} else {
			CalcExchangeRateRs calcExchgRateRs = utils.getExchangeRateAmount(swtMessageDtls.getExchangeRateTypeOUT(),
					swtMessageDtls.getInstructedAmount(), swtMessageDtls.getInstructedAmountCcy(),
					swtMessageDtls.getDrAccountCurrency(), BigDecimal.ZERO,
					swtMessageDtls.getTRANSACTIONDETAISINFO().getEXCHANGERATEFOROUTGOING());
			if (calcExchgRateRs != null) {
				debitAmount.setAmount(calcExchgRateRs.getCalcExchRateResults().getSellAmountDetails().getAmount());
				debitAmount.setIsoCurrencyCode(
						calcExchgRateRs.getCalcExchRateResults().getSellAmountDetails().getIsoCurrencyCode());
			}

		}
		chargesDto.setDebitAmount(debitAmount);

		// credit amount
		Currency creditAmount = new Currency();
		creditAmount.setAmount(swtMessageDtls.getCREDITORDTL().getEXPECTEDCREDITAMOUNT().compareTo(BigDecimal.ZERO) > 0
				? swtMessageDtls.getCREDITORDTL().getEXPECTEDCREDITAMOUNT()
				: BigDecimal.ZERO);
		creditAmount.setIsoCurrencyCode(
				!StringUtils.isBlank(swtMessageDtls.getCrAccountCurrency()) ? swtMessageDtls.getCrAccountCurrency()
						: StringUtils.EMPTY);
		chargesDto.setCreditAmount(creditAmount);

		// exchnage rate
		chargesDto.setExchangeRate(
				swtMessageDtls.getTRANSACTIONDETAISINFO().getEXCHANGERATEFORINCOMING().compareTo(BigDecimal.ZERO) > 0
						? swtMessageDtls.getTRANSACTIONDETAISINFO().getEXCHANGERATEFORINCOMING()
						: BigDecimal.ZERO);
		chargesDto.setExchangeRateType(
				!StringUtils.isBlank(swtMessageDtls.getExchangeRateTypeIN()) ? swtMessageDtls.getExchangeRateTypeIN()
						: StringUtils.EMPTY);
		chargesDto.setDebitExchangeRateType(
				!StringUtils.isBlank(swtMessageDtls.getExchangeRateTypeOUT()) ? swtMessageDtls.getExchangeRateTypeOUT()
						: StringUtils.EMPTY);
		// channelId
		chargesDto.setChannelId(PaymentSwiftConstants.CHANNEL_UXP);

		// SHA BEN OUR
		chargesDto.setChargeType(!StringUtils.isBlank(swtMessageDtls.getRemittanceINFO().getCHARGECODE())
				? swtMessageDtls.getRemittanceINFO().getCHARGECODE()
				: StringUtils.EMPTY);
		chargesDto.setChargeFundingAccountId(swtMessageDtls.getDEBITORDTL().getDEBITACCOUNTID());

		// ubCharges
		Currency ubCharge = new Currency();
		ubCharge.setAmount(swtMessageDtls.getTRANSACTIONDETAISINFO().getAPPLIEDCHARGES().compareTo(BigDecimal.ZERO) > 0
				? swtMessageDtls.getTRANSACTIONDETAISINFO().getAPPLIEDCHARGES()
				: BigDecimal.ZERO);
		ubCharge.setIsoCurrencyCode(debitAmount.getIsoCurrencyCode());
		chargesDto.setUbCharges(ubCharge);

		// paying bank charge
		Currency payingBankChg = new Currency();
		payingBankChg
				.setAmount(swtMessageDtls.getRemittanceINFO().getChargeDetailAmount().compareTo(BigDecimal.ZERO) > 0
						? swtMessageDtls.getRemittanceINFO().getChargeDetailAmount()
						: BigDecimal.ZERO);
		payingBankChg.setIsoCurrencyCode(utils.getChargeDetailCcy(chargesDto));
		chargesDto.setPayingBankChg(payingBankChg);

		return nonStpCharge.getAmountBasedOnChargeOption(chargesDto);
	}
	
	
    public HashMap<String, Object> getCustomerName(String custId) {
		HashMap<String,Object> custAddMap=new HashMap<String,Object>();
		String custName="";	
		ReadCustomerRs readCustomerRs = DataCenterCommonUtils.readCustomerDetails(custId);
		String firstName, middleName, lastName;
		StringBuilder nameBuilder;
		if (readCustomerRs.getCustomerDetails() != null
				&& readCustomerRs.getCustomerDetails().getCustBasicDetails() != null && readCustomerRs
						.getCustomerDetails().getCustBasicDetails().getCustomerId().trim().length() > 0) {

			if (readCustomerRs.getCustomerDetails().getCustBasicDetails().getPartyType().equals(PERSONAL)) {
				firstName = readCustomerRs.getCustomerDetails().getCustBasicDetails().getName();
				if (firstName == null || firstName.isEmpty()) {
					nameBuilder = new StringBuilder();
				} else {
					nameBuilder = new StringBuilder(firstName);
				}
				middleName = readCustomerRs.getCustomerDetails().getPersonDetails().getMiddleName();
				if (middleName != null && !middleName.isEmpty()) {
					nameBuilder.append(CommonConstants.SPACE);
					nameBuilder.append(middleName);
				}

				lastName = readCustomerRs.getCustomerDetails().getPersonDetails().getLastName();
				if (lastName != null && !lastName.isEmpty()) {
					nameBuilder.append(CommonConstants.SPACE);
					nameBuilder.append(lastName);
				}
				custName = nameBuilder.toString();
			} else if (readCustomerRs.getCustomerDetails().getCustBasicDetails().getPartyType()
					.equals(ENTERPRISE)) {

				custName = readCustomerRs.getCustomerDetails().getEnterpriseDetails().getTradeName();

			}
			Address addressDtls = readCustomerRs.getCustomerDetails().getCustBasicDetails().getAddress();
			custAddMap.put("CUSTNAME", custName);
			custAddMap.put("ADDRESSDTLS", addressDtls);
		}
		return custAddMap;
	
	}
}
