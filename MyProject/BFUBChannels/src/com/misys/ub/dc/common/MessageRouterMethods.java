package com.misys.ub.dc.common;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.misys.ub.dc.common.LocaleHelp;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.misys.bankfusion.common.BankFusionMessages;
import com.misys.bankfusion.common.ComplexTypeConvertor;
import com.misys.bankfusion.microservices.constants.MicroServicesConstants;
import com.misys.bankfusion.microservices.impl.AccountMicroServiceImpl;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.bankfusion.subsystem.persistence.SimplePersistentObject;
import com.misys.fbe.common.util.CommonUtil;
import com.misys.ub.dc.handler.DedupHandler;
import com.misys.ub.dc.handler.DocumentUploadHandler;
import com.misys.ub.dc.handler.KYCHandler;
import com.misys.ub.dc.handler.PartyCreateHandler;
import com.misys.ub.dc.handler.PartyLOBHandler;
import com.misys.ub.dc.handler.helper.OpenAccountHelper;
import com.misys.ub.dc.service.TermDepositService;
import com.misys.ub.dc.sql.constants.SqlSelectStatements;
import com.misys.ub.dc.types.CreatePartyAndAccountRq;
import com.misys.ub.fatoms.OpenAccountEventHandler;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.bo.refimpl.IBOProductInheritance;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_OPENACCOUNTDETAILS;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.persistence.services.IPersistenceService;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.FBPMicroflowServiceInvoker;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.IServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;

import bf.com.misys.cbs.msgs.v1r0.TransferResponse;
import bf.com.misys.cbs.services.CalculatePaymentChargeRq;
import bf.com.misys.cbs.services.CalculatePaymentChargeRs;
import bf.com.misys.cbs.types.CreateAccountRq;
import bf.com.misys.cbs.types.CreateAccountRs;
import bf.com.misys.cbs.types.Currency;
import bf.com.misys.cbs.types.InstructionUpdate;
import bf.com.misys.cbs.types.InstructionUpdateItem;
import bf.com.misys.cbs.types.TransactionEvent;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.Orig;
import bf.com.misys.cbs.types.header.RqHeader;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;
import bf.com.misys.party.ws.RqPayload;
import bf.com.misys.party.ws.RsPayload;
import bf.com.misys.paymentmessaging.api.types.ListPaymentNetworksRq;
import bf.com.misys.paymentmessaging.api.types.ListPaymentNetworksRs;
import bf.com.misys.paymentmessaging.api.types.OutwardCustomerCreditTransferRq;
import bf.com.misys.paymentmessaging.api.types.OutwardCustomerCreditTransferRs;
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
import bf.com.misys.paymentmessaging.types.PaymentCharges;
import bf.com.misys.paymentmessaging.types.PaymentIdentification2;
import bf.com.misys.paymentmessaging.types.PaymentNetworkInput;
import bf.com.misys.paymentmessaging.types.PaymentTxnData;
import bf.com.misys.paymentmessaging.types.PaymentTypeInformation3;
import bf.com.misys.paymentmessaging.types.PersonIdentification3;
import bf.com.misys.paymentmessaging.types.PostalAddress1;
import bf.com.misys.paymentmessaging.types.RemittanceInformation1;
import bf.com.misys.paymentmessaging.types.SettlementDateTimeIndication1;
import bf.com.misys.paymentmessaging.types.SettlementInformation1;
import bf.com.misys.paymentmessaging.types.SimpleIdentificationInformation2;
import bf.com.misys.ub.types.mmk.FixtureBreakageRq;
import bf.com.misys.ub.types.mmk.FixtureBreakageRs;

public class MessageRouterMethods {
    private transient final static Log LOGGER = LogFactory.getLog(MessageRouterMethods.class.getName());
    public static final String CAMEL_RESPONSE_ENDPOINT = "QM_BFDC_UB_Response";
    public static final String  INITIATE_CT_MF = "MP_R_MP_INI_InitiateCreditTransfer_API";
    private String maintainPartyRqParam;

    public FixtureBreakageRs terminateTermDeposit(JsonObject jsonObject, boolean isTxnStarted) {
        
        LOGGER.info("***********Inside Terminate Termdeposit proccess***********");
        String msgId = null;
       
        String termDepositId =null;
        BigDecimal breakageAmount = BigDecimal.ZERO ;
        String modeOfPayment = null;
        String payAwayAccount = null;
        String channelId = null;
        Map<String, Object> inputParams = new HashMap<String, Object>();
        FixtureBreakageRq fixtureBreakageRq = new FixtureBreakageRq();
        RqHeader rqHeader = new RqHeader();
        Orig orig = new Orig();
        if(jsonObject.get("channelId")!=null)
        {
        channelId = jsonObject.get("channelId").getAsString();
        orig.setChannelId(channelId);
        orig.setOrigCtxtId(channelId);
        }

        rqHeader.setOrig(orig);
        fixtureBreakageRq.setRqHeader(rqHeader);
        if(jsonObject.get("termDepositId")!=null)
        {
        	termDepositId = jsonObject.get("termDepositId").getAsString();
        fixtureBreakageRq.setAccountId(termDepositId);
        }
        if(jsonObject.get("breakageAmount")!=null)
        {
            breakageAmount = jsonObject.get("breakageAmount").getAsBigDecimal();
        fixtureBreakageRq.setBreakageAmount(breakageAmount);
        }
        
        if(jsonObject.get("modeOfPayment")!=null)
        {
        	modeOfPayment = jsonObject.get("modeOfPayment").getAsString();
        	 fixtureBreakageRq.setModeOfPayment(modeOfPayment);
        }
       
        if(jsonObject.get("payAwayAccount")!=null)
        {
            payAwayAccount = jsonObject.get("payAwayAccount").getAsString();
        fixtureBreakageRq.setPayAwayAccountId(payAwayAccount);
        }
        fixtureBreakageRq.setBreakageInterestRate(BigDecimal.ZERO);
        fixtureBreakageRq.setBreakagePenaltyInterest(BigDecimal.ZERO);
        fixtureBreakageRq.setBreakageCharges(BigDecimal.ZERO);
        fixtureBreakageRq.setInterestRate(BigDecimal.ZERO);
        fixtureBreakageRq.setCustomerMargin(BigDecimal.ZERO);

        fixtureBreakageRq.setPostingNarrative("");
        fixtureBreakageRq.setPostingReference("");

        LOGGER.info("\n\n Setting MFID UB_MMK_FixtureBreakage_SRV and Channel \n\n");

        BankFusionThreadLocal.setChannel(channelId);
		BankFusionThreadLocal.setSourceId(channelId);
        BankFusionThreadLocal.setMFId("UB_MMK_FixtureBreakage_SRV");

        LOGGER.info("Calling TDBreakage MF");

        inputParams.put("fixtureBreakageRq", fixtureBreakageRq);
        HashMap outputParams = MFExecuter.executeMF("UB_MMK_FixtureBreakage_SRV", BankFusionThreadLocal.getBankFusionEnvironment(),
                inputParams);

        LOGGER.info("Got Response from TDBreakage MF");

        FixtureBreakageRs fixtureBreakageRs = (FixtureBreakageRs) outputParams.get("fixtureBreakageRs");
        RsHeader rsHeader = fixtureBreakageRs.getRsHeader();
        rsHeader.setOrigCtxtId(channelId);
        if(jsonObject.get("msgId")!=null && jsonObject.get("msgId").getAsString()!="")
        {
        msgId=jsonObject.get("msgId").getAsString();
        rsHeader.setMessageType(msgId);
        }
        fixtureBreakageRs.setRsHeader(rsHeader);

        if (isTxnStarted) {
            if ("S".equalsIgnoreCase(rsHeader.getStatus().getOverallStatus())) {
                BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
                BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
            } ///
            else {
                LOGGER.warn("CorrelationId: " + BankFusionThreadLocal.getCorrelationID());
                BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
                BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
            } ////
        }
        if (InstructionStatusUpdateNotification.getModuleConfigurationValue("IS_FFC_JMS_CONFIGURED", "IBI",
                BankFusionThreadLocal.getBankFusionEnvironment()))
        {
            InstructionStatusUpdateNotification inst = new InstructionStatusUpdateNotification();
            inst.postResponseToQueue(fixtureBreakageRs, "bf.com.misys.ub.types.mmk.FixtureBreakageRs");
    }
        return fixtureBreakageRs;
    }

    public void postResponseToQueue(Object obj, String objType) {
        ComplexTypeConvertor converter = new ComplexTypeConvertor(this.getClass().getClassLoader());
        String responseToDC = converter.getXmlFromJava(objType, obj);
        postToQueue(responseToDC, MessageRouterMethods.CAMEL_RESPONSE_ENDPOINT);
    }

    private void postToQueue(String msg) {
        postToQueue(msg, CAMEL_RESPONSE_ENDPOINT);
    }

    private void postToQueue(String message, String queueEndpoint) {
        LOGGER.info("message sent from Essence is \n" + message);
        LOGGER.info("---- Posting the message in the following queue " + queueEndpoint);
        MessageProducerUtil.sendMessage(message, queueEndpoint);
    }

    public void pushPasswordRequest(JsonObject jsonObject) {
        String partyId = jsonObject.get("partyId").getAsString();

        // Pushing accounts
        AccountMicroServiceImpl accountMS = new AccountMicroServiceImpl();
        String accountsPayload = accountMS.retrieveAccountIDs("",
                "CustomerId=" + partyId + ";productType=TERM_DEPOSIT_PRODUCT_LIST");
        String[] accountKVpair = accountsPayload.split(MicroServicesConstants.KEY_VALUE_SEPARATOR);
        if (accountKVpair.length > 1)
            accountsPayload = parseAccount(accountKVpair[1].trim());
        else if (accountKVpair[0].contains(";"))
            accountsPayload = parseAccount(accountKVpair[0].trim());
        else accountsPayload = accountKVpair[0].replace("{", "").replace("}", "").trim();
        String[] accounts = accountsPayload.split(",");
        pushAccountforDC(Arrays.asList(accounts));

        // Pushing Transactions
        // Raise

    }

    public void pushAccountforDC(List<String> accounts) {
        LOGGER.info("\n\n Account being pushed for DC customer enablement \n");

        for (String account : accounts) {
            StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                    .append("<ubintcf:AccAndLobDtlsListRq xmlns:ubintcf=\"http://www.misys.com/ub/types/ubintfc\">")
                    .append("<ubintcf:accIdList>").append("<ubintcf:accId>").append(account).append("</ubintcf:accId>")
                    .append("<ubintcf:accOperation>").append("U").append("</ubintcf:accOperation>").append("</ubintcf:accIdList>")
                    .append("<ubintcf:lob>").append("DIGICHANNELS").append("</ubintcf:lob>")
                    .append("</ubintcf:AccAndLobDtlsListRq>");
            String xmlString = sb.toString();
            LOGGER.info("XML String: " + xmlString);
            MessageProducerUtil.sendMessage(xmlString, "ACCOUNT_DETAIL_REQUEST");
        }
        LOGGER.info("\n --- \n");
    }

    static String parseAccount(String accountList) {
        try {
            String accounts = accountList.replace("{", "").replace("}", "").replace(";", ",").replace(" ", "").trim();
            return accounts;
        }
        catch (Exception e) {
            return "";
        }
    }

    public void initiateSepaPayment(JsonObject jsonObject, boolean isTxnStarted) {

        String msgId = jsonObject.get("msgId").getAsString();
        String customerId = jsonObject.get("customerId").getAsString();
        BigDecimal amount = jsonObject.get("amount").getAsBigDecimal();
        String fromMyAccount = jsonObject.get("fromMyAccount").getAsString();
        String transferCurrency = jsonObject.get("transferCurrency").getAsString();
        String transDate = jsonObject.get("transDate").getAsString();
        String channelId = jsonObject.get("channelId").getAsString();
        String paymentReference = jsonObject.get("paymentReference").getAsString();
        String beneficiaryName = jsonObject.get("beneficiaryName").getAsString();
        String IBANAccount = jsonObject.get("IBANAccount").getAsString();
        String bankSWIFTorBIC = jsonObject.get("bankSWIFTorBIC").getAsString();
        String bankName = jsonObject.get("bankName").getAsString();
        String bankCountry = jsonObject.get("bankCountry").getAsString();
        String chargeCode = jsonObject.get("chargeCode").getAsString();
        String chargeAccount = jsonObject.get("chargeAccount").getAsString();
        String paymentSystem = jsonObject.get("paymentSystem").getAsString();
        String paymentType = jsonObject.get("paymentType").getAsString();

        try {

            // Calculating charges
            Map<String, Object> inputParams = new HashMap<String, Object>();
            CalculatePaymentChargeRq calculatePaymentChargeRq = new CalculatePaymentChargeRq();
            Currency transacamount = new Currency();
            transacamount.setAmount(amount);
            transacamount.setIsoCurrencyCode(transferCurrency);

            calculatePaymentChargeRq.setAccount(fromMyAccount);
            calculatePaymentChargeRq.setAccountType("ST");
            calculatePaymentChargeRq.setChannel(channelId);
            calculatePaymentChargeRq.setAmount(transacamount);
            calculatePaymentChargeRq.setPaymentSystem(paymentSystem);
            calculatePaymentChargeRq.setPaymentType(paymentType);
            calculatePaymentChargeRq.setEventSubCategory("20600018");

            LOGGER.info("Calling Payment Charge Forecast MF");
            inputParams.put("calculatePaymentChargeRq", calculatePaymentChargeRq);
            HashMap outputPaymentChargeForecastParams = MFExecuter.executeMF("MP_R_CB_MPM_PaymentChargeForecast_SRV",
                    BankFusionThreadLocal.getBankFusionEnvironment(), inputParams);
            CalculatePaymentChargeRs calculatePaymentChargeRs = (CalculatePaymentChargeRs) outputPaymentChargeForecastParams
                    .get("calculatePaymentChargeRs");
            LOGGER.info("Got Response from Payment Charge Forecast MF");
            // charge calculation done

            Date currentDate = new Date();

            // Fetching network list
            ListPaymentNetworksRq listPaymentNetworksRq = new ListPaymentNetworksRq();
            PaymentNetworkInput paymentNetworkInput = new PaymentNetworkInput();
            paymentNetworkInput.setAmount(amount);
            paymentNetworkInput.setCountry(bankCountry);
            paymentNetworkInput.setCurrency(transferCurrency);
            paymentNetworkInput.setExecutionDate(new java.sql.Date(currentDate.getTime()));
            listPaymentNetworksRq.setPaymentNetworkInput(paymentNetworkInput);

            LOGGER.info("Calling List Payment Networks MF");
            inputParams.clear();
            inputParams.put("getPaymentNetworkListRq", listPaymentNetworksRq);
            HashMap outputListPaymentNetworksParams = MFExecuter.executeMF("MP_INI_GetPaymentNetworkList_API",
                    BankFusionThreadLocal.getBankFusionEnvironment(), inputParams);
            ListPaymentNetworksRs listPaymentNetworksRs = (ListPaymentNetworksRs) outputListPaymentNetworksParams
                    .get("getPaymentNetworkListRs");
            LOGGER.info("Got Response from List Payment Networks MF");
            // Network list fetched

            OutwardCustomerCreditTransferRq outwardCustomerCreditTransferRq = new OutwardCustomerCreditTransferRq();
            outwardCustomerCreditTransferRq.setMode("INIT");
            SettlementInformation1 sttlmInf = new SettlementInformation1();
            ClearingSystemIdentification1Choice ClrSys = new ClearingSystemIdentification1Choice();
            ClrSys.setClrSysId(paymentSystem);
            sttlmInf.setClrSys(ClrSys);
            outwardCustomerCreditTransferRq.setSttlmInf(sttlmInf);
            CreditTransferTransactionInformation2 cdtTrfTxInf = new CreditTransferTransactionInformation2();
            PaymentIdentification2 PmtId = new PaymentIdentification2();
            PmtId.setEndToEndId(msgId);
            PmtId.setInstrId(msgId);
            PmtId.setTxId(msgId);
            cdtTrfTxInf.setPmtId(PmtId);
            BranchAndFinancialInstitutionIdentification3 dbtrAgt = new BranchAndFinancialInstitutionIdentification3();
            FinancialInstitutionIdentification5Choice finInstnId = new FinancialInstitutionIdentification5Choice();
            dbtrAgt.setFinInstnId(finInstnId);
            cdtTrfTxInf.setDbtrAgt(dbtrAgt);
            PaymentTypeInformation3 PmtTpInf = new PaymentTypeInformation3();
            LocalInstrument1Choice LclInstrm = new LocalInstrument1Choice();
            LclInstrm.setCd(paymentType);
            PmtTpInf.setLclInstrm(LclInstrm);
            cdtTrfTxInf.setPmtTpInf(PmtTpInf);
            if (!paymentReference.isEmpty()) {
                RemittanceInformation1 RmtInf = new RemittanceInformation1();
                RmtInf.addUstrd(0, paymentReference);
                cdtTrfTxInf.setRmtInf(RmtInf);
            }

            CurrencyAndAmount IntrBkSttlmAmt = new CurrencyAndAmount();
            IntrBkSttlmAmt.setCcy(transferCurrency);
            IntrBkSttlmAmt.setContent(amount);
            cdtTrfTxInf.setIntrBkSttlmAmt(IntrBkSttlmAmt);
            Date settlementDate = null, transmissionDate = null;
            String accDateTime = "";
            if (listPaymentNetworksRs.getPaymentNetworksOutput().getPaymentNetworkDetails(0).getPaymentNetwork()
                    .equalsIgnoreCase(paymentSystem)) {
                settlementDate = listPaymentNetworksRs.getPaymentNetworksOutput().getPaymentNetworkDetails(0)
                        .getExpectedSettlementDate();
                transmissionDate = listPaymentNetworksRs.getPaymentNetworksOutput().getPaymentNetworkDetails(0)
                        .getPaymentReleaseDate();
            }
            else {
                settlementDate = null;
                transmissionDate = null;
            }
            SimpleDateFormat frmt1 = new SimpleDateFormat("yyyy-MM-dd");
            if (!transDate.isEmpty()) {
                accDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(frmt1.parse(transDate));
            }
            else {
                accDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            }

            SettlementDateTimeIndication1 SttlmTmIndctn = new SettlementDateTimeIndication1();
            if (settlementDate != null) {
                String setDate = null;
                setDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(frmt1.parse(settlementDate.toString()));
                SttlmTmIndctn.setDbtDtTm(Timestamp.valueOf(setDate));
                SttlmTmIndctn.setCdtDtTm(Timestamp.valueOf(setDate));
            }
            else {
                SttlmTmIndctn.setDbtDtTm(Timestamp.valueOf(accDateTime));
                SttlmTmIndctn.setCdtDtTm(Timestamp.valueOf(accDateTime));
            }

            cdtTrfTxInf.setSttlmTmIndctn(SttlmTmIndctn);
            cdtTrfTxInf.setAccptncDtTm(Timestamp.valueOf(accDateTime));
            CurrencyAndAmount InstdAmt = new CurrencyAndAmount();
            InstdAmt.setCcy(transferCurrency);
            InstdAmt.setContent(amount);
            cdtTrfTxInf.setInstdAmt(InstdAmt);
            PartyIdentification8 Dbtr = new PartyIdentification8();
            Party2Choice Id = new Party2Choice();
            PersonIdentification3 PrvtId = new PersonIdentification3();
            PrvtId.setCstmrNb(customerId);
            Id.addPrvtId(0, PrvtId);
            Dbtr.setId(Id);
            cdtTrfTxInf.setDbtr(Dbtr);
            CashAccount7 DbtrAcct = new CashAccount7();
            AccountIdentification3Choice aId = new AccountIdentification3Choice();
            SimpleIdentificationInformation2 PrtryAcct = new SimpleIdentificationInformation2();
            PrtryAcct.setId(fromMyAccount);
            aId.setPrtryAcct(PrtryAcct);
            DbtrAcct.setId(aId);
            cdtTrfTxInf.setDbtrAcct(DbtrAcct);
            BranchAndFinancialInstitutionIdentification3 CdtrAgt = new BranchAndFinancialInstitutionIdentification3();
            FinancialInstitutionIdentification5Choice FinInstnId = new FinancialInstitutionIdentification5Choice();
            FinInstnId.setBIC(bankSWIFTorBIC);
            CdtrAgt.setFinInstnId(FinInstnId);
            cdtTrfTxInf.setCdtrAgt(CdtrAgt);
            PartyIdentification8 Cdtr = new PartyIdentification8();
            Cdtr.setNm(beneficiaryName);

            if (!bankCountry.isEmpty()) {
                PostalAddress1 PstlAdr = new PostalAddress1();
                PstlAdr.setCtry(bankCountry);
                Cdtr.setPstlAdr(PstlAdr);
                Cdtr.setCtryOfRes(bankCountry);
            }

            cdtTrfTxInf.setCdtr(Cdtr);
            CashAccount7 CdtrAcct = new CashAccount7();
            AccountIdentification3Choice Id1 = new AccountIdentification3Choice();
            Id1.setIBAN(IBANAccount);
            CdtrAcct.setId(Id1);
            cdtTrfTxInf.setCdtrAcct(CdtrAcct);
            outwardCustomerCreditTransferRq.setCdtTrfTxInf(cdtTrfTxInf);
            PaymentTxnData paymentTxnData = new PaymentTxnData();
            paymentTxnData.setSaveBeneficiary(false);
            paymentTxnData.setIsChargeAmended(false);
            if (transmissionDate != null) {
                String trnsDate = null;
                trnsDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(frmt1.parse(transmissionDate.toString()));
                Date trfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(trnsDate);
                paymentTxnData.setPaymentTransmissionDate(new java.sql.Date(trfDate.getTime()));
            }
            else {
                Date trfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(accDateTime);
                paymentTxnData.setPaymentTransmissionDate(new java.sql.Date(trfDate.getTime()));
            }

            paymentTxnData.setChannelID(channelId);
            PaymentCharges chargeDetail = new PaymentCharges();
            chargeDetail.setIsChargeWaived(false);
            chargeDetail.setChargeFundingAccountId(chargeAccount);
            CurrencyAndAmount chargeAmount = new CurrencyAndAmount();

            if (calculatePaymentChargeRs != null) {
                chargeAmount.setCcy(calculatePaymentChargeRs.getChargeAmount().getIsoCurrencyCode());
                chargeAmount.setContent(calculatePaymentChargeRs.getChargeAmount().getAmount());
                chargeDetail.setChargeAmount(chargeAmount);
            }

            paymentTxnData.addChargeDetail(0, chargeDetail);
            outwardCustomerCreditTransferRq.setPaymentTxnData(paymentTxnData);
            LOGGER.info("Calling Initiate Payment service");
            inputParams.clear();
            inputParams.put("customerCreditTransferRq", outwardCustomerCreditTransferRq);
             BankFusionThreadLocal.setSourceId(channelId);
            HashMap outputCreditTransferParams = MFExecuter.executeMF(INITIATE_CT_MF,
                    BankFusionThreadLocal.getBankFusionEnvironment(), inputParams);

            OutwardCustomerCreditTransferRs outputCreditTransferRs = (OutwardCustomerCreditTransferRs) outputCreditTransferParams
                    .get("customerCreditTransferRs");
            LOGGER.info("Got Response from Initiate Payment Service \n\n");

            if (isTxnStarted)
                BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
            
            /*
             * ComplexTypeConvertor converter = new
             * ComplexTypeConvertor(this.getClass().getClassLoader());
             * logger.info(converter.getXmlFromJava(
             * "bf.com.misys.paymentmessaging.api.types.OutwardCustomerCreditTransferRs",
             * outputCreditTransferRs));
             */

            /*
             * TransferResponse trfResponse = new TransferResponse();
             * 
             * RsHeader rsHeader = new RsHeader(); rsHeader.setOrigCtxtId(channelId);
             * 
             * InstructionUpdate instUpdate = new InstructionUpdate(); InstructionUpdateItem
             * insUpdItem = new InstructionUpdateItem();
             * if(msgId.equalsIgnoreCase(outputCreditTransferRs.getEndToEndId())){
             * insUpdItem.setNewStatus("PROCESS_SUCCESSFULLY");
             * insUpdItem.setTransactionalItem(msgId); insUpdItem.setTransactionId(msgId);
             * insUpdItem.setNotificationSequence((long) 0); instUpdate.addInstructionUpdateItem(0,
             * insUpdItem); }else{ insUpdItem.setNewStatus("REJECTED");
             * insUpdItem.setTransactionalItem(msgId); insUpdItem.setTransactionId(msgId);
             * insUpdItem.setNotificationSequence((long) 0);
             * 
             * String eventMsg=BankFusionMessages.getFormattedMessage(Integer.parseInt("40000127"),
             * new String[] {});
             * 
             * TransactionEvent txnEvent = new TransactionEvent();
             * txnEvent.setReasonCode("40000127"); txnEvent.setDefaultMessage(eventMsg);
             * txnEvent.setFormattedMessage(eventMsg);
             * 
             * insUpdItem.setTransactionEvent(txnEvent);
             * 
             * instUpdate.addInstructionUpdateItem(0, insUpdItem); }
             * trfResponse.setInstructionStatusUpdateNotification(instUpdate);
             * trfResponse.setRsHeader(rsHeader);
             * 
             * postResponseToQueue(trfResponse, "bf.com.misys.cbs.msgs.v1r0.TransferResponse");
             */
        }
        catch (Exception e) {
            if (isTxnStarted)

                LOGGER.info("\n\n SEPA payment failed \n\n");
            e.printStackTrace();
            BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
            LOGGER.warn("CorrelationId: " + BankFusionThreadLocal.getCorrelationID());

            TransferResponse trfResponse = new TransferResponse();

            RsHeader rsHeader = new RsHeader();
            rsHeader.setOrigCtxtId(channelId);

            InstructionUpdate instUpdate = new InstructionUpdate();
            InstructionUpdateItem insUpdItem = new InstructionUpdateItem();
            insUpdItem.setNewStatus("REJECTED");
            insUpdItem.setTransactionalItem(msgId);
            insUpdItem.setTransactionId(msgId);
            insUpdItem.setNotificationSequence((long) 0);

            String eventMsg = BankFusionMessages.getFormattedMessage(Integer.parseInt("40000127"), new String[] {});

            TransactionEvent txnEvent = new TransactionEvent();
            txnEvent.setReasonCode("40000127");
            txnEvent.setDefaultMessage(eventMsg);
            txnEvent.setFormattedMessage(eventMsg);

            insUpdItem.setTransactionEvent(txnEvent);

            instUpdate.addInstructionUpdateItem(0, insUpdItem);

            trfResponse.setInstructionStatusUpdateNotification(instUpdate);
            trfResponse.setRsHeader(rsHeader);

            postResponseToQueue(trfResponse, "bf.com.misys.cbs.msgs.v1r0.TransferResponse");
        }
    }

    public void createPartyOpenAccount(JsonObject jsonObject, boolean isTxnStarted) throws Exception {

        LOGGER.info("Inside message router methods for party create and account opening");
        boolean isAccDtlsPresent = true;
        OfferJsonTuner jsonTuner = new OfferJsonTuner();
        OfferJsonReader jsonReader = new OfferJsonReader();
        Gson gson = new Gson();
        CreatePartyAndAccountRq createPartyAndAccountRq = gson.fromJson(jsonObject, CreatePartyAndAccountRq.class);
        String msgId = createPartyAndAccountRq.getMsgId();
        String partyId = "";
        LOGGER.info("Request to Essence\n\n " + jsonObject.toString() + "\n\n");

        // Call Dedup Handler
        DedupHandler dedupHandler = new DedupHandler(jsonObject);
        if (dedupHandler.isDuplicateParty()) {
            dedupHandler.sendFailedDedupRes();
            rollBackTxns(isTxnStarted);
            return;
        }

        // uploading the document to OpenKM
        List<String> documentIdList = new ArrayList<String>();
        if (jsonObject.get(RequestResponseConstants.DOCUMENTS) != null) {
            JsonObject documentsJsonObj = jsonObject.get(RequestResponseConstants.DOCUMENTS).getAsJsonObject();
            if (documentsJsonObj.get(RequestResponseConstants.REPEATABLE_BLOCKS) != null) {
                JsonArray jsonDocArray = documentsJsonObj.get(RequestResponseConstants.REPEATABLE_BLOCKS).getAsJsonArray();
                String documentId = null;
                JsonObject jsonObjectDocBlock = null;
                DocumentUploadHandler uploadHandler = new DocumentUploadHandler();

                for (int i = 0; i < jsonDocArray.size(); i++) {
                    jsonObjectDocBlock = jsonDocArray.get(i).getAsJsonObject();
                        if ( jsonObjectDocBlock.get(RequestResponseConstants.PARTY_DOCUMENT_ID).isJsonNull() &&
                                !jsonObjectDocBlock.get(RequestResponseConstants.DOCUMENT_NAME).isJsonNull()
                                && !jsonObjectDocBlock.get(RequestResponseConstants.DOCUMENT_CONTENTS).isJsonNull()) {
                            documentId = uploadHandler.uploadDocument(jsonObjectDocBlock,
                                    jsonObject.get(RequestResponseConstants.BRANCH_CODE).getAsString());
                            if (CommonUtil.checkIfNullOrEmpty(documentId)) {
                                uploadHandler.sendFailedResposnse(jsonObject);
                                rollBackTxns(isTxnStarted);
                                deleteDocument(documentIdList, jsonObject.get(RequestResponseConstants.BRANCH_CODE).getAsString());
                                return;
                            }
                            else {
                                jsonObjectDocBlock.addProperty(RequestResponseConstants.PARTY_DOCUMENT_ID, documentId);
                                documentIdList.add(documentId);
                            }
                        }
                    }
                }
            }

        removeDocumentContentFromJson(jsonObject);

        // Call Party Create Handler and create party
        PartyCreateHandler ptyCreateHandler = new PartyCreateHandler(jsonObject);
        ptyCreateHandler.createParty();

        if (!ptyCreateHandler.isPartyCreated()) {

            // Delete the uploaded document(s)
            deleteDocument(documentIdList, jsonObject.get(RequestResponseConstants.BRANCH_CODE).getAsString());

            ptyCreateHandler.sendFailedPartyCreationRs();
            rollBackTxns(isTxnStarted);
            return;
        }

        partyId = ptyCreateHandler.getPartyId();
        // Persist account open parameters if account label present
        if (isAccDtlsPresent) {
            JsonObject jsonCasaTDObject = null;

            if (null != (jsonCasaTDObject = jsonReader.checkIfCasaAccount(jsonObject))) {
                fetchAccOpeningParams(jsonCasaTDObject);
                jsonTuner.updateCustomerNoToAccount(jsonCasaTDObject, partyId, RequestResponseConstants.CASA_ACCOUNT_TYPE);
            }

            if (null != (jsonCasaTDObject = jsonReader.checkIfTDAccount(jsonObject))) {
                jsonTuner.updateCustomerNoToAccount(jsonCasaTDObject, partyId, RequestResponseConstants.TD_ACCOUNT_TYPE);
            }
            removePartyFieldsFromJson(jsonObject);
            /*boolean isAccOpParamPeristed = persistAccOpParams(jsonObject, partyId, isTxnStarted);
            if (!isAccOpParamPeristed) {
                sendFailedAccReply(msgId, partyId);
                rollBackTxns(isTxnStarted);
                return;
            }*/
        }

        // KYC flow if kyc was enabled during party creation
        if (ptyCreateHandler.isKYCenabled()) {
            KYCHandler kycHandler = new KYCHandler();
            kycHandler.doKYC(partyId);
            if ("Failure".equalsIgnoreCase(kycHandler.getKycExecutionStatus())) {
                kycHandler.sendFailedPartyCreationRs(msgId, partyId);
//                removeAccOpEntry(partyId);
                rollBackTxns(isTxnStarted);
                return;
            }

        }
        OpenAccountEventHandler openAccountEventHandler = new OpenAccountEventHandler();
        openAccountEventHandler.setF_IN_partyId(partyId);
        openAccountEventHandler.setJsonInputForOpenAccount(jsonObject);
        openAccountEventHandler.process(BankFusionThreadLocal.getBankFusionEnvironment());
        if (isTxnStarted) {
            BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
            // BankFusionThreadLocal.getPersistanceFactory().beginTransaction(); ////
        }
    }

    private void deleteDocument(List<String> documentIdList, String branchCode) {
        DocumentUploadHandler uploadHandler = new DocumentUploadHandler();
        for (String documentId : documentIdList) {
            uploadHandler.deleteDocument(documentId, branchCode);
        }
    }

    public void updatePartyLOB(JsonObject jsonObject, boolean isTxnStarted) {

        LOGGER.info("Entering updateLOB() method");
        LOGGER.info("Request to Essence\n\n " + jsonObject.toString() + "\n\n");

        PartyLOBHandler partyLOBHandler = new PartyLOBHandler(jsonObject);

        boolean isLOBUpdated = partyLOBHandler.updateLOB();
        LOGGER.info("Returned value from party line of business handler is : " + isLOBUpdated);

        if (!isLOBUpdated) {
            rollBackTxns(isTxnStarted);
            return;
        }

        if (isTxnStarted) {
            BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
        }

        LOGGER.info("Exiting updateLOB() method");
    }

    /**
     * This method is used to create Term Deposit
     * 
     * @param jsonObject
     *            <code>JsonObject</code> containing the values which are required for creating the
     *            term deposit
     * @param isTxnStarted
     *            <code>true</code> If transaction is already started by the current thread
     *            otherwise <code>false</code>
     */
    public boolean createTermDeposit(JsonObject jsonObject, boolean isTxnStarted) {

        if (jsonObject.get(RequestResponseConstants.ACCOUNT_FIELDS) != null
                && jsonObject.get(RequestResponseConstants.ACCOUNT_FIELDS).getAsJsonArray() != null) {
            JsonArray accountFields = jsonObject.get(RequestResponseConstants.ACCOUNT_FIELDS).getAsJsonArray();
            for (int i = 0; i < accountFields.size(); i++) {
                JsonObject accFields = accountFields.get(i).getAsJsonObject();
                if (accFields.has(RequestResponseConstants.TD_ACCOUNT_FIELDS)
                        && accFields.get(RequestResponseConstants.TD_ACCOUNT_FIELDS) != null
                        && accFields.get(RequestResponseConstants.TD_ACCOUNT_FIELDS).getAsJsonObject() != null) {
                    JsonObject termDepositFields = accFields.get(RequestResponseConstants.TD_ACCOUNT_FIELDS).getAsJsonObject();
                    boolean isTermDepositCreated = false;
					BankFusionThreadLocal.setSourceId("IBI");
                    TermDepositService termDepositService = new TermDepositService(jsonObject);
                    isTermDepositCreated = termDepositService
                            .createTermDeposit(termDepositFields.get(RequestResponseConstants.TERM_DEPOSIT_REQ).getAsJsonObject());
                    LOGGER.info("createTermDeposit istxnstarted1:"+isTxnStarted);
                    LOGGER.info("createTermDeposit Current Thread running1:"+Thread.currentThread().getId());
                    if (!isTermDepositCreated) {
                    	LOGGER.info("createTermDeposit istxnstarted2:"+isTxnStarted);
                        LOGGER.info("createTermDeposit Current Thread running2:"+Thread.currentThread().getId());
                        rollBackTxns(isTxnStarted);
                        return false;
                    }
                }

            }
            if (isTxnStarted) {
            	LOGGER.info("createTermDeposit istxnstarted3:"+isTxnStarted);
                LOGGER.info("createTermDeposit Current Thread running3:"+Thread.currentThread().getId());
                BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
            }

        }
        return true;
    }
    
    /**
     * This method is used to create CASA account
     * 
     * @param jsonObject
     *            <code>JsonObject</code> containing the values which are required for creating the
     *            CASA account
     * @param isTxnStarted
     *            <code>true</code> If transaction is already started by the current thread
     *            otherwise <code>false</code>
     */
    public boolean createCasa(JsonObject jsonInput, boolean isTxnStarted) {
        
        String isCasaAccountCreated = null;
        OpenAccountHelper accHelper = new OpenAccountHelper();
        
        if (jsonInput.get(RequestResponseConstants.ACCOUNT_FIELDS) != null) {
            Iterator<JsonElement> jsonIterator = jsonInput.get(RequestResponseConstants.ACCOUNT_FIELDS).getAsJsonArray().iterator();
            JsonObject jsonAccountFields = new JsonObject();
            String msgId = jsonInput.get(RequestResponseConstants.MESSAGE_ID).getAsString();
            String partyId = jsonInput.get(RequestResponseConstants.PARTY_ID).getAsString();

            while (jsonIterator.hasNext()) {
                jsonAccountFields = jsonIterator.next().getAsJsonObject();

                if (jsonAccountFields.get(RequestResponseConstants.CASA_ACCOUNT_FIELDS) != null) {
					BankFusionThreadLocal.setSourceId("IBI");
                    isCasaAccountCreated = accHelper.createCasaAccount(jsonAccountFields.get(RequestResponseConstants.CASA_ACCOUNT_FIELDS).getAsJsonObject(),
                            partyId, msgId);
                    if (isCasaAccountCreated == null) {
                        rollBackTxns(isTxnStarted);
                        return false;
                    }
                }
            }
            if (isTxnStarted) {
                BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
            }
        }
        return true;
    }

    private boolean persistAccOpParams(JsonObject accOpParams, String partyId, boolean isTxnStarted) {
        boolean isAccOpParamsPersisted = false;
        IServiceManager serviceManager = ServiceManagerFactory.getInstance().getServiceManager();
        IPersistenceObjectsFactory privateFactory = ((IPersistenceService) serviceManager
                .getServiceForName(ServiceManager.PERSISTENCE_SERVICE)).getPrivatePersistenceFactory(false);
        try {
            privateFactory.beginTransaction();
            IBOUB_INF_OPENACCOUNTDETAILS openAccountDtls = (IBOUB_INF_OPENACCOUNTDETAILS) privateFactory
                    .getStatelessNewInstance(IBOUB_INF_OPENACCOUNTDETAILS.BONAME);
            openAccountDtls.setF_PARTYID(partyId);
            openAccountDtls.setF_MESSAGE(accOpParams.toString());
            privateFactory.create(IBOUB_INF_OPENACCOUNTDETAILS.BONAME, openAccountDtls);
            isAccOpParamsPersisted = true;
            privateFactory.commitTransaction();
        }
        catch (BankFusionException e) {
            LOGGER.info("BankFusion exception occured during persistence of Account Opening Details");
            privateFactory.rollbackTransaction();
        }
        catch (Exception e) {
            LOGGER.info("Exception occured during persistence of Account Opening Details");
            privateFactory.rollbackTransaction();
        }
        finally {
            privateFactory.closePrivateSession();
        }
        return isAccOpParamsPersisted;

    }

    private void removeAccOpEntry(String partyId) {
        ArrayList<String> params = new ArrayList<String>();
        String whereClause = " WHERE f_PARTYID = ?";
        List openAccountDtls = null;
        IServiceManager serviceManager = ServiceManagerFactory.getInstance().getServiceManager();
        IPersistenceObjectsFactory privateFactory = ((IPersistenceService) serviceManager
                .getServiceForName(ServiceManager.PERSISTENCE_SERVICE)).getPrivatePersistenceFactory(false);
        params.add(partyId);
        try {
            privateFactory.beginTransaction();
            openAccountDtls = privateFactory.findByQuery(IBOUB_INF_OPENACCOUNTDETAILS.BONAME, whereClause, params, null, false);
            if (openAccountDtls != null && (openAccountDtls.size() > 0)) {
                privateFactory.remove(IBOUB_INF_OPENACCOUNTDETAILS.BONAME, (IBOUB_INF_OPENACCOUNTDETAILS) openAccountDtls.get(0));
                privateFactory.commitTransaction();
            }
            else {
                privateFactory.rollbackTransaction();
            }
        }
        catch (Exception e) {
            LOGGER.info("Error in removing the entry from IBOUB_INF_OPENACCOUNTDETAILS for partyid" + partyId);
            privateFactory.rollbackTransaction();
            return;
        }
        finally {
            privateFactory.closePrivateSession();
        }

    }

    @SuppressWarnings("unchecked")
    private void fetchAccOpeningParams(JsonObject jsonCasaAccountFields) {
        List<SimplePersistentObject> subProductList = Lists.newArrayList();
        OfferJsonTuner offerJsonTuner = new OfferJsonTuner();
        OfferJsonReader jsonReader = new OfferJsonReader();
        ListIterator<SimplePersistentObject> subProducts = null;
        ArrayList<Object> params = new ArrayList<>();
        Map<String, Object> dataMap = new HashMap<>();
        String subProd = StringUtils.EMPTY;
        String subProdCur = StringUtils.EMPTY;

        String subProductId = jsonReader.getSubProductIdForCasa(jsonCasaAccountFields);
        params.add(subProductId);
        subProductList = fetchDataFromDB(params, SqlSelectStatements.SUB_PRODUCT_QUERY);
        if (subProductList != null) {
            subProducts = subProductList.listIterator();
            while (subProducts.hasNext()) {
                SimplePersistentObject subProdDetails = (SimplePersistentObject) subProducts.next();
                dataMap = subProdDetails.getDataMap();
                if (dataMap.get(IBOProductInheritance.UBSUBPRODUCTID) != null) {
                    subProd = (String) dataMap.get(IBOProductInheritance.UBSUBPRODUCTID);
                    subProdCur = (String) dataMap.get(IBOProductInheritance.ACC_ISOCURRENCYCODE);
                }

                LOGGER.info("SubProduct is " + subProd + " Sub Prod Currency " + subProdCur);

            }
        }
        offerJsonTuner.updateSubProdCurrToCasaAcc(jsonCasaAccountFields, subProdCur);
        offerJsonTuner.updateSubProductIdToCasaAcc(jsonCasaAccountFields, subProd);
        offerJsonTuner.updateAccountOpeningDateToCasaAcc(jsonCasaAccountFields);
    }

    @Deprecated
    public void openAccount(JsonObject jsonObject, boolean isTxnStarted) throws Exception {

        String msgId = jsonObject.get("msgId").getAsString();
        String customerId = jsonObject.get("customerId").getAsString();
        List<SimplePersistentObject> resultSet = null;
        ListIterator<SimplePersistentObject> resultIt = null;
        Map<String, Object> dataMap = new HashMap<String, Object>();
        String subProd = null;
        String subProdCur = null;
        String branchcode = null;

        // Fetching subProductId and currency
        IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
        IBOProductInheritance productInheritance = (IBOProductInheritance)factory.findByPrimaryKey(IBOProductInheritance.BONAME, jsonObject.get("productContextCode").getAsString());
        if(productInheritance != null) {
            subProd = productInheritance.getF_UBSUBPRODUCTID();
            subProdCur = productInheritance.getF_ACC_ISOCURRENCYCODE();
        }
        LOGGER.info("SubProduct is " + subProd + " Sub Prod Currency " + subProdCur);
        // Fetching branchcode for the customer
        IBOCustomer customer = (IBOCustomer) factory.findByPrimaryKey(IBOCustomer.BONAME, customerId);
        if(customer!=null) {
            branchcode = customer.getF_BRANCHSORTCODE();
        }
        LOGGER.info("Branchcode " + branchcode);
        if (branchcode != null && subProd != null && subProdCur != null) {
            jsonObject.addProperty("subProdCur", subProdCur);
            jsonObject.addProperty("subProd", subProd);
            jsonObject.addProperty("accBranchCode", branchcode);
            checkCustomerAndOpenAccount(jsonObject, isTxnStarted);
        }
        else {
            LOGGER.info("Account is not created ");
            LOGGER.debug("Missing Branchcode and/or subproductid and/or currencycode");
            sendRejectedOfferReply(msgId, customerId);
        }

    }

    @Deprecated
    public void checkCustomerAndOpenAccount(final JsonObject jsonObject, boolean isTxnStarted) throws Exception {
        int CUST_CHECK_RETRY_DELAY = 10000;
        String customerId = jsonObject.get("customerId").getAsString();
        String subProdCur = jsonObject.get("subProdCur").getAsString();
        String accBranchCode = jsonObject.get("accBranchCode").getAsString();
        String subProd = jsonObject.get("subProd").getAsString();
        String msgId = jsonObject.get("msgId").getAsString();
        try {
            HashMap<String, Object> accParams = new HashMap<String, Object>();

            CreateAccountRq createAccountRq = new CreateAccountRq();
            RqHeader rqHeader = new RqHeader();
            Orig orig = new Orig();
            orig.setChannelId("IBI");
            java.sql.Date sqlDate = new java.sql.Date(SystemInformationManager.getInstance().getBFBusinessDate().getTime());
            createAccountRq.setRqHeader(rqHeader);
            createAccountRq.setCurrency(subProdCur);
            createAccountRq.setCustomerNo(customerId);
            createAccountRq.setBranchSortCode(accBranchCode);
            createAccountRq.setAccountOpenDate(sqlDate);
            createAccountRq.setSubProductId(subProd);

            accParams.put("createAccountRq", createAccountRq);

            BankFusionThreadLocal.setChannel("IBI");
            BankFusionThreadLocal.setMFId("CB_ACC_CreateAccount_SRV");

            LOGGER.info("-----------Account opening started-------------------");

            FBPMicroflowServiceInvoker invoker2 = new FBPMicroflowServiceInvoker("CB_ACC_CreateAccount_SRV");
            HashMap<String, Object> outputMap2 = invoker2.invokeMicroflow(accParams, false);
            CreateAccountRs rs2 = (CreateAccountRs) outputMap2.get("createAccountRs");
            RsHeader resHeader = rs2.getRsHeader();
            MessageStatus status = resHeader.getStatus();
            String accOpenStatus = status.getOverallStatus();
            SubCode[] subCode = status.getCodes();
            subCode.toString();
            String errorCode = subCode[0].getCode();
            LOGGER.info("\n\n Response from CreateAccount Service: \n" + getXML(rs2, "bf.com.misys.cbs.types.CreateAccountRs"));
            String accNum = rs2.getAccountNo();
            String accName = rs2.getAccountName();

            if (errorCode != null && (errorCode.equals("20020212") || errorCode.equals("40180074"))) {
                LOGGER.info("\n\nCustomer not yet created will retry again in " + CUST_CHECK_RETRY_DELAY + " msec\n\n");
                new java.util.Timer().schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        postToQueue(jsonObject.toString(), "QM_Interface_Receive");
                    }
                }, CUST_CHECK_RETRY_DELAY);
            }
            else {
                if (accOpenStatus != null && accOpenStatus.equalsIgnoreCase("S")) {
                    LOGGER.info("Account is created and the account Name is " + accName + " and account number is " + accNum);
                    // JsonObject successRes = new JsonObject();
                    // successRes.addProperty("status", "S");
                    // successRes.addProperty("msgId", msgId);
                    // successRes.addProperty("msgType", "PARTY_ONBOARD_ACCOUNT_OPEN_RES");
                    // successRes.addProperty("origCtxtId", "IBI");
                    // successRes.addProperty("partyId", customerId);
                    // successRes.addProperty("accountId", accNum);
                    // successRes.addProperty("accountName", accName);
                    // postToQueue(successRes.toString(), "QM_BFDC_UB_Response");
                    InstructionStatusUpdateNotification instr = new InstructionStatusUpdateNotification();
                    instr.sendFailOfferResponse(msgId, "S", "", "", "", customerId,null);

                }
                else {
                    LOGGER.info("Account is not created ");
                    sendRejectedOfferReply(msgId, customerId);
                }
            }
            if (isTxnStarted) {
                BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
                BankFusionThreadLocal.getPersistanceFactory().beginTransaction(); ////
            }

        }
        catch (Exception e) {

            if (isTxnStarted) {
                BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
                BankFusionThreadLocal.getPersistanceFactory().beginTransaction(); ////
            }
            LOGGER.info(" Error during Account creation");
            e.printStackTrace();
            BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
            BankFusionThreadLocal.getPersistanceFactory().beginTransaction(); ////
            LOGGER.info("Account is not created ");
            LOGGER.warn("CorrelationId: " + BankFusionThreadLocal.getCorrelationID());
            sendRejectedOfferReply(msgId, customerId);
        }

    }

    public void changePersonalPartyDtls(JsonObject jsonObject, boolean isTxnStarted) throws Exception {

        LOGGER.info("-----------Modifying Personal Party Details----------------");

        String msgId = jsonObject.get("msgId").getAsString();
       // String msgType = jsonObject.get("msgType").getAsString();
        String customerId = jsonObject.get("CustomerId").getAsString();
      //  String country = jsonObject.get("Country").getAsString();
      //  String postalCode = jsonObject.get("PostalCode").getAsString();
       // String city = jsonObject.get("City").getAsString();
      //  String address = jsonObject.get("Address").getAsString();
       // String state = jsonObject.get("State").getAsString();
        JsonObject addressesObj = jsonObject.getAsJsonObject("Addresses");
        JsonObject fatcaObj = jsonObject.getAsJsonObject("FatcaDetails");
        
        JsonArray addresses = null;
        if(addressesObj != null) {
            addresses = jsonObject.getAsJsonObject("Addresses").getAsJsonArray("REPEATABLE_BLOCKS");
        }
        JsonArray fatcadetails = null;
        if(fatcaObj != null) {
            fatcadetails = jsonObject.getAsJsonObject("FatcaDetails").getAsJsonArray("REPEATABLE_BLOCKS");
        }
        
        //String phoneNumber = jsonObject.get("PhoneNumber").getAsString();
       // String secondaryPhNo = jsonObject.get("SecondaryPhoneNumber").getAsString();
        JsonObject dialedNumsObj = jsonObject.getAsJsonObject("DialNumbers");
        JsonArray dialedNumbers = null;
        if(dialedNumsObj != null) {
        	dialedNumbers = jsonObject.getAsJsonObject("DialNumbers").getAsJsonArray("REPEATABLE_BLOCKS");
        }
        String email = jsonObject.get("Email").getAsString();
        String addressType = jsonObject.get("AddressType").getAsString();
        String emailContactType = jsonObject.get("EmailContactType").getAsString();
        String countryCode = jsonObject.get("CountryCode").getAsString();
        countryCode = LocaleHelp.getInstance().getIso3CountryFromIso2Country(countryCode);
        String isdCode = jsonObject.get("ISDCode").getAsString();
        String addressCountry = null;
        try {
            RqPayload rqPayload = new RqPayload();
            HashMap<String, Object> inputParams = new HashMap<String, Object>();

            String controlParam = "DEDUP_REQD=N;TXN_COMMIT_LEVEL=A;GEN_CODE_VALDN_REQ=Y;PARTY_ACTION=A";
            maintainPartyRqParam = new String("UNIQUE_ID=First13;PT_PFN_Party#PARTYID=");
            maintainPartyRqParam = maintainPartyRqParam.concat(customerId);
            if (addresses != null && addresses.size() != 0) {
                for (JsonElement addressElement : addresses) {
                    if (addressElement != null) {
                         Iterator iterator = addressElement.getAsJsonObject().entrySet().iterator();
                         maintainPartyRqParam = maintainPartyRqParam.concat(";{");
                         while (iterator.hasNext()) {
                            Map.Entry<Object, Object> entry = (Entry<Object, Object>) iterator.next();
                            String rqParam = entry.getKey().toString();
                            if (!((JsonElement) entry.getValue()).isJsonNull()) {
                                String rqParamValue = ((JsonPrimitive) entry.getValue()).getAsString();
                                if (rqParam.equals("PT_PFN_Address#COUNTRYCODE")) {
                                    rqParamValue = LocaleHelp.getInstance().getIso3CountryFromIso2Country(rqParamValue);
                                }
                                else if (rqParam.equals("PT_PFN_AddressLink#ISDEAFULTADDRESS")) {
                                    if ("true".equalsIgnoreCase(rqParamValue)) {
                                        rqParamValue = "Y";
                                    }
                                    else {
                                        rqParamValue = "N";
                                    }
                                }
                                maintainPartyRqParam = maintainPartyRqParam.concat(rqParam).concat("=").concat(rqParamValue);
                                if (iterator.hasNext()) {
                                    maintainPartyRqParam = maintainPartyRqParam.concat(";");
                                }
                            }
                        
                         }
                         maintainPartyRqParam = maintainPartyRqParam.concat("}");
                        
                    }
                }

//            } else {
//                if (address != null && !address.isEmpty()) {
//                    maintainPartyRqParam = maintainPartyRqParam.concat(";{PT_PFN_AddressLink#ADDRESSTYPE=").concat(addressType)
//                            .concat(";PT_PFN_Address#COUNTRYCODE=").concat(countryCode)
//                            .concat(";PT_PFN_Address#POSTALCODE=").concat(postalCode)
//                            .concat(";PT_PFN_Address#TOWNORCITY=").concat(city).concat(";PT_PFN_Address#ADDRESSLINE1=")
//                            .concat(address).concat(";PT_PFN_Address#ADDRESSLINE10=").concat(state)
//                            .concat(";PT_PFN_AddressLink#ISDEAFULTADDRESS=Y}");
//                }
            }
            
            if (fatcadetails != null && fatcadetails.size() != 0) {
                for (JsonElement fatcaElement : fatcadetails) {
                    if (fatcaElement != null) {
                         Iterator iterator = fatcaElement.getAsJsonObject().entrySet().iterator();
                         maintainPartyRqParam = maintainPartyRqParam.concat(";{");
                         while (iterator.hasNext()) {
                            Map.Entry<Object, Object> entry = (Entry<Object, Object>) iterator.next();
                            String rqParam = entry.getKey().toString();
                            if (!((JsonElement) entry.getValue()).isJsonNull()) {
                                String rqParamValue = ((JsonPrimitive) entry.getValue()).getAsString();

                                maintainPartyRqParam = maintainPartyRqParam.concat(rqParam).concat("=").concat(rqParamValue);
                                if (iterator.hasNext()) {
                                    maintainPartyRqParam = maintainPartyRqParam.concat(";");
                                }
                            }

                         }
                         maintainPartyRqParam = maintainPartyRqParam.concat("}");
                        
                    }
                }
            }

            if (dialedNumbers != null && dialedNumbers.size() != 0) {
                for (JsonElement dialedNumber : dialedNumbers) {
                    if (dialedNumber != null) {
                        Iterator iterator = dialedNumber.getAsJsonObject().entrySet().iterator();
                        maintainPartyRqParam = maintainPartyRqParam.concat(";{");
                        while (iterator.hasNext()) {
            				Map.Entry<Object, Object> entry = (Entry<Object, Object>) iterator.next();
            				String rqParam = entry.getKey().toString();
            				if(!((JsonElement)entry.getValue()).isJsonNull()) {
	            				String rqParamValue = ((JsonPrimitive)entry.getValue()).getAsString();
	            				if(!rqParam.equals("DialNumberType")) {
		            				if(rqParam.equals("PT_PFN_PartyContactDetails#ISDCODE") && rqParamValue != null && !rqParamValue.isEmpty()) {
		            					rqParamValue = "+" + rqParamValue;
		            				}
		            				maintainPartyRqParam = maintainPartyRqParam.concat(rqParam)
		             						.concat("=").concat(rqParamValue);
		            				if(iterator.hasNext()) {
		            					maintainPartyRqParam = maintainPartyRqParam.concat(";");
		            				}
	            				}
            				}
                        }
                        maintainPartyRqParam = maintainPartyRqParam.concat("}");
                    }
                }
//            } else {
//                if (phoneNumber != null && !phoneNumber.isEmpty()) {
//                    maintainPartyRqParam = maintainPartyRqParam.concat(";{PT_PFN_PartyContactDetails#ISDCODE=").concat(isdCode)
//                            .concat(";PT_PFN_PartyContactDetails#CONTACTTYPE=").concat("DAYTIME")
//                            .concat(";PT_PFN_PartyContactDetails#CONTACTVALUE=").concat(phoneNumber)
//                            .concat(";PT_PFN_PartyContactDetails#CONTACTMETHOD=TELEPHONE}");
//                }
//                if (secondaryPhNo != null && !secondaryPhNo.isEmpty()) {
//                    maintainPartyRqParam = maintainPartyRqParam.concat(";{PT_PFN_PartyContactDetails#ISDCODE=").concat(isdCode)
//                            .concat(";PT_PFN_PartyContactDetails#CONTACTTYPE=").concat("PERSONALMOBILE")
//                            .concat(";PT_PFN_PartyContactDetails#CONTACTVALUE=").concat(secondaryPhNo)
//                            .concat(";PT_PFN_PartyContactDetails#CONTACTMETHOD=SMS}");
//                }
            }
            
               
              if(email != null && !email.isEmpty())
              {
            maintainPartyRqParam = maintainPartyRqParam.concat(";{PT_PFN_PartyContactDetails#CONTACTTYPE=")
                               .concat(emailContactType).concat(";PT_PFN_PartyContactDetails#CONTACTVALUE=").concat(email)
                               .concat(";PT_PFN_PartyContactDetails#CONTACTMETHOD=EMAIL}");
              }
            rqPayload.setRqParam(maintainPartyRqParam);
            rqPayload.setControlParam(controlParam);
            inputParams.put("requestPayload", rqPayload);
            LOGGER.info("--------Invoking Microflow to change party details---------- ");
            LOGGER.info("\n\nPayload for change party details : \n" + maintainPartyRqParam);
            LOGGER.info("\n\nControl param for change party details : \n" + controlParam);
            BankFusionThreadLocal.setChannel("IBI");
            BankFusionThreadLocal.setMFId("CB_PTY_MaintainPartyWS_SRV");
            FBPMicroflowServiceInvoker invoker = new FBPMicroflowServiceInvoker("CB_PTY_MaintainPartyWS_SRV");
            HashMap<String, Object> outputMap = invoker.invokeMicroflow(inputParams, false);
            // invokeMicroflow(inputParams, false);
            RsPayload rs = (RsPayload) outputMap.get("responsePayload");
            String res = rs.getRsParam();
            String status = getPartyRsData("STATUS", res);

            if (status != null && status.equalsIgnoreCase("Success")) {

                LOGGER.info("Party Personal Details Modified");
                if (isTxnStarted) {
                    BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
                    BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
                }
                sendChangePersonalDtlRs(msgId, customerId, "S");

            }
            else {
                if (isTxnStarted) {
                    BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
                    BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
                }
                String errorMsg = getPartyRsData("ERRORMSG", res);
                LOGGER.info("\n Error response from Party service is  " + errorMsg);
                LOGGER.warn("CorrelationId: " + BankFusionThreadLocal.getCorrelationID());
                sendChangePersonalDtlRs(msgId, customerId, "E");

            }

        }
        catch (Exception e) {
            LOGGER.info(" Error during modification of personal party details");

            if (isTxnStarted) {
                BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
                BankFusionThreadLocal.getPersistanceFactory().beginTransaction(); ////
            }
            LOGGER.warn("CorrelationId: " + BankFusionThreadLocal.getCorrelationID());
            sendChangePersonalDtlRs(msgId, customerId, "E");
        }
    }

    @SuppressWarnings("unchecked")
    private List<SimplePersistentObject> fetchDataFromDB(ArrayList<Object> params, String query) {
        List<SimplePersistentObject> resultSet = null;
        try {
            IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
            resultSet = factory.executeGenericQuery(query, params, null, false);
            LOGGER.info("resultSet is " + resultSet);
        }
        catch (BankFusionException bfException) {
            LOGGER.info(bfException.getMessageNumber() + " : " + bfException.getLocalizedMessage());
            return null;
        }
        catch (Exception unexpectedError) {
            LOGGER.info("Error occured while fetching subproduct Records ", unexpectedError);
            return null;
        }
        return resultSet;
    }

    private void sendChangePersonalDtlRs(String msgId, String customerId, String status) throws Exception {
//        JsonObject errRes = new JsonObject();
//        errRes.addProperty("status", status);
//        errRes.addProperty("msgId", msgId);
//        errRes.addProperty("msgType", "CHANGE_PERSONAL_DETAILS_RES");
//        errRes.addProperty("origCtxtId", "I"BI");
//        errRes.addProperty("partyId", customerId);
//        postToQueue(errRes.toString(), "QM_BFDC_UB_Response");
        InstructionStatusUpdateNotification response = new InstructionStatusUpdateNotification();
        response.sendChangePersonalResponse(msgId, customerId, status);
    }

    private void sendRejectedOfferReply(String msgId, String customerId) throws Exception {
//        JsonObject errRes = new JsonObject();
//        errRes.addProperty("status", "E");
//        errRes.addProperty("msgId", msgId);
//        errRes.addProperty("msgType", "PARTY_ONBOARD_ACCOUNT_OPEN_RES");
//        errRes.addProperty("origCtxtId", "IBI");
//        errRes.addProperty("partyId", customerId);
//        postToQueue(errRes.toString(), "QM_BFDC_UB_Response");
//        LOGGER.warn("CorrelationId: " + BankFusionThreadLocal.getCorrelationID());
        InstructionStatusUpdateNotification response = new InstructionStatusUpdateNotification();
        response.sendFailOfferResponse(msgId, "E", "", "", "", customerId,null);
    }

    private void sendFailedAccReply(String msgId, String customerId) throws Exception {
//        JsonObject errRes = new JsonObject();
//        errRes.addProperty("status", "E");
//        errRes.addProperty("reasonCode", "40112483");
//        errRes.addProperty("msgId", msgId);
//        errRes.addProperty("msgType", "PARTY_ONBOARD_ACCOUNT_OPEN_RES");
//        errRes.addProperty("origCtxtId", "IBI");
//        errRes.addProperty("partyId", customerId);
//        postToQueue(errRes.toString(), "QM_BFDC_UB_Response");
//        LOGGER.warn("CorrelationId: " + BankFusionThreadLocal.getCorrelationID());
        InstructionStatusUpdateNotification response = new InstructionStatusUpdateNotification();
        response.sendFailOfferResponse(msgId, "E", "40112483", "", "", customerId,null);
    }

    private String getXML(Object obj, String objType) {
        @SuppressWarnings("deprecation")
        ComplexTypeConvertor converter = new ComplexTypeConvertor(this.getClass().getClassLoader());
        return converter.getXmlFromJava(objType, obj);
    }

    private String getPartyRsData(String key, String input) {
        String valueString = null;
        String[] resultSet = input.split(";");
        for (int i = 0; i < resultSet.length; i++) {
            if (resultSet[i].contains(key)) {
                valueString = resultSet[i].split("=")[1].trim();
            }
        }
        return valueString;
    }

    private void rollBackTxns(boolean isTxnStarted) {
        if (isTxnStarted) {
            BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
            // BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
        }
    }

    private void removeDocumentContentFromJson(JsonObject input) {
        JsonArray jsonArray = new JsonArray();
        JsonElement jsonDocElement = input.get(RequestResponseConstants.DOCUMENTS);
        if (jsonDocElement != null) {
            JsonObject jsonDoc = jsonDocElement.getAsJsonObject();
            JsonElement jsonRepeatBlockElement = jsonDoc.get(RequestResponseConstants.REPEATABLE_BLOCKS);
            if (jsonRepeatBlockElement != null) {
                jsonArray = jsonRepeatBlockElement.getAsJsonArray();
                for (JsonElement element : jsonArray) {
                    element.getAsJsonObject().remove(RequestResponseConstants.DOCUMENT_CONTENTS);
                }
            }
        }
    }

    private void removePartyFieldsFromJson(JsonObject input) {
        if (input != null) {
            input.remove("PARTY_FIELDS");
        }
    }

}