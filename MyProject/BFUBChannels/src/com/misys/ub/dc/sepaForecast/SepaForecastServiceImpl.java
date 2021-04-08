package com.misys.ub.dc.sepaForecast;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import com.misys.bankfusion.paymentmessaging.domain.outward.credittransfer.forecast.PaymentsForecastResponse;
import com.misys.bankfusion.paymentmessaging.domain.outward.credittransfer.forecast.PaymentsForecastService;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.fbp.common.util.FBPService;
import com.trapedza.bankfusion.core.CommonConstants;

import bf.com.misys.paymentmessaging.api.types.OutwardCustomerCreditTransferRq;
import bf.com.misys.paymentmessaging.types.AccountIdentification3Choice;
import bf.com.misys.paymentmessaging.types.BranchAndFinancialInstitutionIdentification3;
import bf.com.misys.paymentmessaging.types.CashAccount7;
import bf.com.misys.paymentmessaging.types.ClearingSystemIdentification1Choice;
import bf.com.misys.paymentmessaging.types.CreditTransferTransactionInformation2;
import bf.com.misys.paymentmessaging.types.CurrencyAndAmount;

import bf.com.misys.paymentmessaging.types.FinancialInstitutionIdentification5Choice;
import bf.com.misys.paymentmessaging.types.LocalInstrument1Choice;
import bf.com.misys.paymentmessaging.types.Party2Choice;
import bf.com.misys.paymentmessaging.types.PartyIdentification8;
import bf.com.misys.paymentmessaging.types.PaymentIdentification2;
import bf.com.misys.paymentmessaging.types.PaymentTxnData;
import bf.com.misys.paymentmessaging.types.PaymentTypeInformation3;
import bf.com.misys.paymentmessaging.types.PersonIdentification3;
import bf.com.misys.paymentmessaging.types.PostalAddress1;
import bf.com.misys.paymentmessaging.types.RemittanceInformation1;
import bf.com.misys.paymentmessaging.types.SettlementDateTimeIndication1;
import bf.com.misys.paymentmessaging.types.SettlementInformation1;
import bf.com.misys.paymentmessaging.types.SimpleIdentificationInformation2;
import jxl.common.Logger;

@FBPService(serviceId = "sepeForecastService")
public class SepaForecastServiceImpl implements SepaForecastService {

	private static final Logger LOGGER = Logger.getLogger(SepaForecastServiceImpl.class);
	private static final String CLRG = "CLRG";
	

	@Override
	public SepaForecastResponse getPaymentForecast(SepaForecastRequest sepaForecasteRequest) {

		SepaForecastResponse sepaForecastResponse = new SepaForecastResponse();
		LOGGER.info("========Entered into SepaForecast================");
		OutwardCustomerCreditTransferRq creditRq = createCreditTrfReq(sepaForecasteRequest);
		PaymentsForecastService forecastSrv = new PaymentsForecastService();
		PaymentsForecastResponse forecastResponse = forecastSrv.getPaymentForecast(creditRq);
		if (forecastResponse.getErrorResponse() != null
				&& forecastResponse.getErrorResponse().getEventCollection() != null) {
			sepaForecastResponse.setErrorResponse(forecastResponse.getErrorResponse());

		} else {
			sepaForecastResponse.setChargeAmt(forecastResponse.getChargeAmt());
			sepaForecastResponse.setChargeCC(forecastResponse.getChargeCC());
			sepaForecastResponse.setChargeFundingAcc(forecastResponse.getChargeFundingAcc());
			sepaForecastResponse.setChargeTaxAmt(forecastResponse.getChargeTaxAmt());
			sepaForecastResponse.setChargeTaxCC(forecastResponse.getChargeTaxCC());
			sepaForecastResponse.setExchangeRate(forecastResponse.getExchangeRate());
			sepaForecastResponse.setFromCCY(sepaForecasteRequest.getFromMyAccountCurrency());
			sepaForecastResponse.setToCCY(sepaForecasteRequest.getTransferCurrency());
			sepaForecastResponse.setSettelmentDate(forecastResponse.getSettlementDate());
			LOGGER.info("======Got response from SepaForecast=========");
		}

		return sepaForecastResponse;

	}

	public OutwardCustomerCreditTransferRq createCreditTrfReq(SepaForecastRequest sepaForecasteRequest) {

		OutwardCustomerCreditTransferRq custReq = new OutwardCustomerCreditTransferRq();

		SettlementInformation1 sttlmInf = new SettlementInformation1();
		ClearingSystemIdentification1Choice ClrSys = new ClearingSystemIdentification1Choice();
		ClrSys.setClrSysId(sepaForecasteRequest.getPaymentsys());
		sttlmInf.setClrSys(ClrSys);
		sttlmInf.setSttlmMtd(CLRG);
		custReq.setSttlmInf(sttlmInf);

		custReq.setMode("INIT");

		int randomIDNum = Math.abs(new SecureRandom().nextInt() % 800000000) + 100000000;
		String randomID = "DCFFC" + randomIDNum;

		CreditTransferTransactionInformation2 cdtTrfTxInf = new CreditTransferTransactionInformation2();
		PaymentIdentification2 pmtId = new PaymentIdentification2();
		pmtId.setEndToEndId(randomID);
		pmtId.setInstrId(randomID);
		pmtId.setTxId(randomID);
		cdtTrfTxInf.setPmtId(pmtId);

		PaymentTypeInformation3 pmtTpInf = new PaymentTypeInformation3();
		LocalInstrument1Choice lclInstrm = new LocalInstrument1Choice();
		lclInstrm.setCd(sepaForecasteRequest.getPaymenttyp());
		pmtTpInf.setLclInstrm(lclInstrm);
		cdtTrfTxInf.setPmtTpInf(pmtTpInf);

		BigDecimal xchgRate = CommonConstants.BIGDECIMAL_ZERO;

		cdtTrfTxInf.setXchgRate(xchgRate);

		if (!sepaForecasteRequest.getPaymentReference().isEmpty()) {
			RemittanceInformation1 RmtInf = new RemittanceInformation1();
			RmtInf.addUstrd(0, sepaForecasteRequest.getPaymentReference());
			cdtTrfTxInf.setRmtInf(RmtInf);
		}

		CurrencyAndAmount intrBkSttlmAmt = new CurrencyAndAmount();
		intrBkSttlmAmt.setCcy(sepaForecasteRequest.getTransferCurrency());
		intrBkSttlmAmt.setContent(sepaForecasteRequest.getAmount());
		cdtTrfTxInf.setIntrBkSttlmAmt(intrBkSttlmAmt);

		String accDateTime = null;
		String dateformat = "yyyy-MM-dd HH:mm:ss.SSS";

		if (sepaForecasteRequest.getTransDate() != null) {

			accDateTime = new SimpleDateFormat(dateformat).format(sepaForecasteRequest.getTransDate());

			LOGGER.info("Accept Date Time is SystemDateTime:" + accDateTime);
		} else {
			accDateTime = new SimpleDateFormat(dateformat)
					.format(SystemInformationManager.getInstance().getBFSystemDateTime());
			LOGGER.info("Accept Date Time is SystemDateTime:" + accDateTime);
		}

		SettlementDateTimeIndication1 sttlmTmIndctn = new SettlementDateTimeIndication1();

		sttlmTmIndctn.setDbtDtTm(Timestamp.valueOf(accDateTime));
		sttlmTmIndctn.setCdtDtTm(Timestamp.valueOf(accDateTime));

		cdtTrfTxInf.setSttlmTmIndctn(sttlmTmIndctn);
		cdtTrfTxInf.setAccptncDtTm(Timestamp.valueOf(accDateTime));

		CurrencyAndAmount instdAmt = new CurrencyAndAmount();
		instdAmt.setCcy(sepaForecasteRequest.getTransferCurrency());
		instdAmt.setContent(sepaForecasteRequest.getAmount());
		cdtTrfTxInf.setInstdAmt(instdAmt);

		PartyIdentification8 dbtr = new PartyIdentification8();
		Party2Choice id = new Party2Choice();
		PersonIdentification3 prvtId = new PersonIdentification3();
		prvtId.setCstmrNb(sepaForecasteRequest.getCustomerId());
		id.addPrvtId(0, prvtId);
		dbtr.setId(id);
		cdtTrfTxInf.setDbtr(dbtr);

		CashAccount7 dbtrAcct = new CashAccount7();
		AccountIdentification3Choice aId = new AccountIdentification3Choice();
		SimpleIdentificationInformation2 prtryAcct = new SimpleIdentificationInformation2();
		prtryAcct.setId(sepaForecasteRequest.getFromMyAccount());
		aId.setPrtryAcct(prtryAcct);
		dbtrAcct.setId(aId);
		cdtTrfTxInf.setDbtrAcct(dbtrAcct);

		BranchAndFinancialInstitutionIdentification3 cdtrAgt = new BranchAndFinancialInstitutionIdentification3();
		FinancialInstitutionIdentification5Choice finInstnId = new FinancialInstitutionIdentification5Choice();
		finInstnId.setBIC(sepaForecasteRequest.getBenBankSWIFTorBIC());
		cdtrAgt.setFinInstnId(finInstnId);
		cdtTrfTxInf.setCdtrAgt(cdtrAgt);
		PartyIdentification8 cdtr = new PartyIdentification8();
		cdtr.setNm(sepaForecasteRequest.getBeneficiaryName());

		if (!sepaForecasteRequest.getBenCountry().isEmpty()) {
			PostalAddress1 pstlAdr = new PostalAddress1();
			pstlAdr.setCtry(sepaForecasteRequest.getBenCountry());
			cdtr.setPstlAdr(pstlAdr);
			cdtr.setCtryOfRes(sepaForecasteRequest.getBenCountry());
		}
		cdtTrfTxInf.setCdtr(cdtr);

		CashAccount7 cdtrAcct = new CashAccount7();
		AccountIdentification3Choice id1 = new AccountIdentification3Choice();
		id1.setIBAN(sepaForecasteRequest.getIbanAccount());
		cdtrAcct.setId(id1);
		cdtTrfTxInf.setCdtrAcct(cdtrAcct);

		custReq.setCdtTrfTxInf(cdtTrfTxInf);
		PaymentTxnData Txndata=new PaymentTxnData();
		Txndata.setChannelID(sepaForecasteRequest.getChannelId());
		custReq.setPaymentTxnData(Txndata);
		
		return custReq;

	}

}
