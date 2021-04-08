/*
 * ******************************************************************************
 * Copyright (c) 2018 Finastra Software Solutions Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Finastra Software Solutions Ltd.
 * Use is subject to license terms.
 * ******************************************************************************
 */
package com.misys.ub.swift.remittance.process;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.cbs.common.util.DateUtil;
import com.misys.ub.datacenter.DataCenterCommonUtils;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.misys.ub.swift.remittance.dto.ExchangeRateDto;
import com.misys.ub.swift.remittance.dto.RemittanceProcessDto;
import com.misys.ub.swift.remittance.validation.MT103Validator;
import com.misys.ub.swift.remittance.validation.ValidationHelper;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOTransactionScreenControl;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;

import bf.com.misys.cbs.msgs.v1r0.ReadAccountRs;
import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRs;
import bf.com.misys.cbs.types.Charges;
import bf.com.misys.cbs.types.Currency;
import bf.com.misys.cbs.types.header.EventParameters;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;
import bf.com.misys.cbs.types.swift.BankToBankInfoDtls;
import bf.com.misys.cbs.types.swift.BeneficiaryCustomer;
import bf.com.misys.cbs.types.swift.BeneficiaryCustomerAndInstitution;
import bf.com.misys.cbs.types.swift.DocumentUploadDtls;
import bf.com.misys.cbs.types.swift.IntermediaryDetails;
import bf.com.misys.cbs.types.swift.OrderingCustomer;
import bf.com.misys.cbs.types.swift.OrderingCustomerAndInstitution;
import bf.com.misys.cbs.types.swift.OrderingInstitutionDtl;
import bf.com.misys.cbs.types.swift.PayDtlsText;
import bf.com.misys.cbs.types.swift.RemittanceDetails;
import bf.com.misys.cbs.types.swift.TermsAndConditionsInfo;
import bf.com.misys.cbs.types.swift.TextLines4;
import bf.com.misys.cbs.types.swift.TextLines6;

/**
 * @author hargupta
 *
 */
public class SimplePopulateSwiftRemittanceRequest implements Command {
    public static final String SENDER_REF_PREFIX = "SW";
    private transient final static Log LOGGER = LogFactory.getLog(SimplePopulateSwiftRemittanceRequest.class);

    @Override
    public boolean execute(Context context) throws Exception {
        SwiftRemittanceRq swiftRemittanceRq = (SwiftRemittanceRq) context.get("swtRemitanceReq");
        RemittanceProcessDto remittanceDto = (RemittanceProcessDto) context.get("remittanceDto");
        SwiftRemittanceRs swtRemitterResp = (SwiftRemittanceRs) context.get("swtRemitterResp");
        RsHeader rsHeader;
        Boolean endOfChain;
        if (LOGGER.isInfoEnabled())
            LOGGER.info("START SimplePopulateSwiftRemittanceRequest" +   BankFusionThreadLocal.getUserSession().getUserId());

        rsHeader = validateMandatoryFields(swiftRemittanceRq);
        swtRemitterResp.setRsHeader(rsHeader);
        if(null != rsHeader.getStatus() && PaymentSwiftConstants.ERROR_STATUS.equals(rsHeader.getStatus().getOverallStatus()))
            {
                return true;
            }

        String transactionCode = populateChannelId(swiftRemittanceRq);
        populateDebitExchangeRateType(transactionCode, swiftRemittanceRq);
        populateCreditTransactionCodeAndAccount(swiftRemittanceRq, remittanceDto, transactionCode);
        populateCreditExchangeRateType(remittanceDto.getCreditTransactionCode(), swiftRemittanceRq);
        populateCreditCurrencyAndAmount(swiftRemittanceRq);
        if (null != swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails()) {
        	populatePayToPartyIdentifier(swiftRemittanceRq);
        }
        populateMessageDetails(swiftRemittanceRq);
        endOfChain = populateDebitCustomerCode(swiftRemittanceRq, swtRemitterResp, remittanceDto.getEnv());
        populateExchangeRates(swiftRemittanceRq);
        populateDebitAmount(swiftRemittanceRq);
        
        clearIncomingEmptyChargeAndDocDtl(swiftRemittanceRq);
        String txnType = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getTransactionType();
        // the SSI ID blank check is done since the searchSSI will take care not to set the
        // custToCust fileds id SSI-ID is present
        if (!OutRemScreenEventHandler.MT103.equals(txnType)) {
            Currency currency = new Currency();
            currency.setAmount(BigDecimal.ZERO);
            currency.setIsoCurrencyCode(CommonConstants.EMPTY_STRING);
            swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails().setChargeDetails(currency);
            if (StringUtils
                    .isBlank(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getSettlementInstrId())) {
                clearCustToCustFields(swiftRemittanceRq);
            }
        }
        swiftRemittanceRq = truncateTextFieldsTo35Char(swiftRemittanceRq);
        context.put("swtRemitanceReq", swiftRemittanceRq);
        context.put("remittanceDto", remittanceDto);
        context.put("swtRemitterResp", swtRemitterResp);
        return endOfChain;
    }

	/**
	 * Method Description:Populate the debit amount in case of zero.
	 * 
	 * @param swiftRemittanceRq
	 */
	private void populateDebitAmount(SwiftRemittanceRq swiftRemittanceRq) {
		BigDecimal instructedAmountValue = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
				.getInstructedAmount().getAmount();
		String instructedAmtCurrency = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
				.getInstructedAmount().getIsoCurrencyCode();
		if (null != swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls()
				.getDebitAmount()) {
			Currency debitAmount = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
					.getDebitPostingDtls().getDebitAmount();

			// if instructed amount currency is same as debit amount currency
			if (null != debitAmount.getAmount() && debitAmount.getAmount().compareTo(BigDecimal.ZERO) == 0) {

				if (instructedAmtCurrency.equals(debitAmount.getIsoCurrencyCode())) {
					swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls()
							.getDebitAmount().setAmount(instructedAmountValue);
					swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls()
							.setDebitExchangeRate(BigDecimal.ONE);

				} else {
					OutRemScreenEventHandler outRem = new OutRemScreenEventHandler();
					ExchangeRateDto debitExchDtls = outRem.getDebitExchRateDetails(instructedAmtCurrency,
							debitAmount.getIsoCurrencyCode(),
							swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
									.getDebitPostingDtls().getDebitExchangeRateType(),
							instructedAmountValue, BigDecimal.ZERO);
					BigDecimal drAmt = debitExchDtls.getDebitAmount();
					BigDecimal drExchgRt = debitExchDtls.getDebitExchangeRate();
					debitAmount.setAmount(drAmt);
					swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls()
							.setDebitExchangeRate(drExchgRt);
				}

			}
		}

	}


    private void clearCustToCustFields(SwiftRemittanceRq swiftRemittanceRq) {
        OrderingCustomer orderingCustomer = (OrderingCustomer) PaymentSwiftUtils.intializeDefaultvalues(new OrderingCustomer());
        swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution().setOrderingCustomer(orderingCustomer);

        BeneficiaryCustomer beneficiaryCustomer = (BeneficiaryCustomer) PaymentSwiftUtils
                .intializeDefaultvalues(new BeneficiaryCustomer());
        swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()
                .setBeneficiaryCustomer(beneficiaryCustomer);

        RemittanceDetails remittanceDtls = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails();
        remittanceDtls.setChargeCode(CommonConstants.EMPTY_STRING);
        remittanceDtls.setTxnTypeCode_tag26(CommonConstants.EMPTY_STRING);
        remittanceDtls.setBankToBankInfo((BankToBankInfoDtls) PaymentSwiftUtils.intializeDefaultvalues(new BankToBankInfoDtls()));
        remittanceDtls.setRemittanceInfo((TextLines4) PaymentSwiftUtils.intializeDefaultvalues(new TextLines4()));
        remittanceDtls.setTermsAndConditionsInfo(
                (TermsAndConditionsInfo) PaymentSwiftUtils.intializeDefaultvalues(new TermsAndConditionsInfo()));

        swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().setMessagePreference(CommonConstants.EMPTY_STRING);
    }

    private void clearIncomingEmptyChargeAndDocDtl(SwiftRemittanceRq swiftRemittanceRq) {
        DocumentUploadDtls[] docDtls = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getDocumentUpload();
        if (null != docDtls) {
            ArrayList<DocumentUploadDtls> docDtlsList = new ArrayList<DocumentUploadDtls>(Arrays.asList(docDtls));
            Iterator<DocumentUploadDtls> docDtlsListIterator = docDtlsList.iterator();
            while (docDtlsListIterator.hasNext()) {
                DocumentUploadDtls documentUploadDtl = docDtlsListIterator.next();
                if (StringUtils.isBlank(documentUploadDtl.getDocumentSavedId())
                        && StringUtils.isBlank(documentUploadDtl.getDocumentType())) {
                    docDtlsListIterator.remove();
                }
            }
            swiftRemittanceRq.getInitiateSwiftMessageRqDtls()
                    .setDocumentUpload(docDtlsList.toArray((DocumentUploadDtls[]) new DocumentUploadDtls[docDtlsList.size()]));
        }

        Charges[] chargeDtls = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getCharges();
        if (null != chargeDtls) {
            ArrayList<Charges> chargeDtlsList = new ArrayList<Charges>(Arrays.asList(chargeDtls));
            Iterator<Charges> chargeDtlsListIterator = chargeDtlsList.iterator();
            while (chargeDtlsListIterator.hasNext()) {
                Charges charge = chargeDtlsListIterator.next();
                if (null == charge.getCharge() || StringUtils.isBlank(charge.getCharge().getChargeCode())) {
                    chargeDtlsListIterator.remove();
                }
            }
            swiftRemittanceRq.getInitiateSwiftMessageRqDtls()
                    .setCharges(chargeDtlsList.toArray((Charges[]) new Charges[chargeDtlsList.size()]));
        }
    }

    private void populateExchangeRates(SwiftRemittanceRq swiftRemittanceRq) {
        OutRemScreenEventHandler outRemScreenEventHandler = new OutRemScreenEventHandler();
        Currency instAmount = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getInstructedAmount();

        if (BigDecimal.ZERO.compareTo(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                .getDebitPostingDtls().getDebitExchangeRate()) >= 0) {
            ExchangeRateDto exchangeRateDtoDr = outRemScreenEventHandler.getDebitExchRateDetails(instAmount.getIsoCurrencyCode(),
                    swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls().getDebitAmount()
                            .getIsoCurrencyCode(),
                    swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls()
                            .getDebitExchangeRateType(),
                    instAmount.getAmount(), BigDecimal.ZERO);
            swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls()
                    .setDebitExchangeRate(exchangeRateDtoDr.getDebitExchangeRate());
        }

        if (BigDecimal.ZERO.compareTo(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                .getCreditPostingDtls().getCreditExchangeRate()) >= 0) {
            ExchangeRateDto exchangeRateDtoCr = outRemScreenEventHandler.getCreditExchRateDetails(instAmount.getIsoCurrencyCode(),
                    swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                            .getCreditAmount().getIsoCurrencyCode(),
                    swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                            .getCreditExchangeRateType(),
                    instAmount.getAmount(), BigDecimal.ZERO);
            swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                    .setCreditExchangeRate(exchangeRateDtoCr.getCreditExchangeRate());
        }
    }

    private RsHeader validateMandatoryFields(SwiftRemittanceRq swiftRemittanceRq) {
        ValidationHelper helper = new ValidationHelper();
        RsHeader rsHeader = new RsHeader();
        // txn type validation
        if (StringUtils.isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getTransactionType())) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_MANDATORY_ENTRY_CB05_INT, "Transaction Type");
            return rsHeader;
        }

        // msg currency validation
        if (StringUtils.isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getCurrencyCode())) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_MANDATORY_ENTRY_CB05_INT, "Currency");
            return rsHeader;
        }

        // value date validation
        Date valueDate = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getValueDate();
        if (null == valueDate || DateUtil.isDefaultDate(valueDate)) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_MANDATORY_ENTRY_CB05_INT, "Value Date");
            return rsHeader;
        }

        // instructed amount
        Currency instAmount = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getInstructedAmount();
        if (null == instAmount || BigDecimal.ZERO.compareTo(instAmount.getAmount()) >= 0) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_MANDATORY_ENTRY_CB05_INT, "Instructed Amount");
            return rsHeader;
        }

        // instructed currency
        if (StringUtils.isEmpty(instAmount.getIsoCurrencyCode())) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_MANDATORY_ENTRY_CB05_INT, "Instructed Currency");
            return rsHeader;
        }

        // debit account
        if (StringUtils.isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls()
                .getDebitAccountId())) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_MANDATORY_ENTRY_CB05_INT, "Debit Account");
            return rsHeader;
        }

        // channel ID
        if (StringUtils.isEmpty(swiftRemittanceRq.getRqHeader().getOrig().getChannelId())) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_MANDATORY_ENTRY_CB05_INT, "Channel ID");
            return rsHeader;
        }

        return rsHeader;
    }

    private String populateChannelId(SwiftRemittanceRq swiftRemittanceRq) {
        String transactionCode = populateDebitTransactionCode(swiftRemittanceRq);
        if (StringUtils.isBlank(swiftRemittanceRq.getRqHeader().getOrig().getChannelId())) {
            swiftRemittanceRq.getRqHeader().getOrig().setChannelId(PaymentSwiftConstants.CHANNEL_UXP);
        }
        return transactionCode;
    }

    private Boolean populateDebitCustomerCode(SwiftRemittanceRq swiftRemittanceRq, SwiftRemittanceRs swtRemitterResp,
            BankFusionEnvironment env) {
        // debit customer id
        if (StringUtils.isBlank(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getCustomerID())) {
            ReadAccountRs readAccountRs = DataCenterCommonUtils.readAccount(swiftRemittanceRq.getInitiateSwiftMessageRqDtls()
                    .getTransactionDetails().getDebitPostingDtls().getDebitAccountId());
            RsHeader rsHeader = readAccountRs.getRsHeader();
            if (rsHeader != null && rsHeader.getStatus() != null) {
                MessageStatus status = rsHeader.getStatus();
                if (status.getOverallStatus().equals(PaymentSwiftConstants.ERROR_STATUS)) {
                    if (null != status.getCodes()) {
                        for (SubCode code : status.getCodes()) {
                            ArrayList<Object> params = new ArrayList<>();
                            if (null != code.getParameters()) {
                                for (EventParameters param : code.getParameters()) {
                                    params.add(param.getEventParameterValue());
                                }
                            }
                            code.setDescription(PaymentSwiftUtils.getErrorDescription(code.getCode(), params.toArray(), env));
                        }
                    }
                    swtRemitterResp.setRsHeader(rsHeader);
                    return true;
                }
            }
            String custID = readAccountRs.getAccountDetails().getAccountInfo().getAcctBasicDetails().getCustomerShortDetails()
                    .getCustomerId();
            swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().setCustomerID(custID);
            String debitAccCurr = readAccountRs.getAccountDetails().getAccountInfo().getAcctBasicDetails().getCurrency();
            swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls().getDebitAmount()
                    .setIsoCurrencyCode(debitAccCurr);
        }
        return false;
    }

    private void populateMessageDetails(SwiftRemittanceRq swiftRemittanceRq) {

        // if message reference is empty generated it
        if (StringUtils.isBlank(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getMessageReference())) {
            long time = SystemInformationManager.getInstance().getBFBusinessDateTime().getTime();
            String senderReference = SENDER_REF_PREFIX + time;
            swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().setMessageReference(senderReference);
            swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().setSenderReference(senderReference);
        }
        
        // default senders reference if Narration is empty
        if (StringUtils.isBlank(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getNarration())) {
            swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().setNarration(
                    swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getSenderReference());
        }
        
        //message preference
        if(StringUtils.isBlank(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getMessagePreference())){
            PaymentSwiftUtils utils = new PaymentSwiftUtils();
            String msgPreference = utils.getModuleConfigValue(PaymentSwiftConstants.DEFAULT_SWIFT_MSG_PREFERENCE,
                          PaymentSwiftConstants.CHANNELID_SWIFT);
            swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().setMessagePreference(msgPreference);
        }
       
    }

    /**
     * @param swiftRemittanceRq
     */
    private void populatePayToPartyIdentifier(SwiftRemittanceRq swiftRemittanceRq) {
        PaymentSwiftUtils utils = new PaymentSwiftUtils();
        if (StringUtils.isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo().getPayToIdentifierCode())
        		&& StringUtils.isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo().getPayToDetails().getPayDtls1())
                && (StringUtils.isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo().getPayToPartyIdentifier())
                || StringUtils.isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo().getPayToPartyIdentifierClearingCode()))) {
            IBOAttributeCollectionFeature accountIbo = utils.getAccountDetails(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
            		.getCreditPostingDtls().getCreditAccountId());
            if (null != accountIbo) {
                IBOSwtCustomerDetail swtCustomerDetails = SwiftRemittanceMessageHelper
                        .getSwiftCustomerDetails(accountIbo.getF_CUSTOMERCODE());
                if (null != swtCustomerDetails) {
                    swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo()
                            .setPayToIdentifierCode(swtCustomerDetails.getF_BICCODE());
                }
            }
        }
    }

    /**
     * @param creditTransactionCode
     * @param swiftRemittanceRq
     */
    private void populateCreditExchangeRateType(String creditTransactionCode, SwiftRemittanceRq swiftRemittanceRq) {
        // The exchange rate used to calculate the Credit Amount from Debit Amount (In
        // case of same
        // currency, it will be 1)
        if (StringUtils.isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                .getCreditExchangeRateType())) {
            swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                    .setCreditExchangeRateType(getExchangeRateType(creditTransactionCode));
        }
    }

    /**
     * @param debitTransactionCode
     * @param swiftRemittanceRq
     */
    private void populateDebitExchangeRateType(String debitTransactionCode, SwiftRemittanceRq swiftRemittanceRq) {
        // The exchange rate used to calculate the Credit Amount from Debit Amount (In
        // case of same
        // currency, it will be 1)
        if (StringUtils.isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls()
                .getDebitExchangeRateType())) {
            swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls()
                    .setDebitExchangeRateType(getExchangeRateType(debitTransactionCode));
        }

    }

    /**
     * @param swiftRemittanceRq
     * @param remittanceDto
     * @param transactionCode
     */
    private void populateCreditTransactionCodeAndAccount(SwiftRemittanceRq swiftRemittanceRq, RemittanceProcessDto remittanceDto,
            String transactionCode) {
        IBOTransactionScreenControl transactionScreenControl = SwiftRemittanceMessageHelper
                .getTransactionScreenControl(transactionCode);
        if (null != transactionScreenControl) {
            String contraTransactioncode = transactionScreenControl.getF_CONTRATRANSACTIONCODE();
            remittanceDto.setCreditTransactionCode(contraTransactioncode);
            if (StringUtils.isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                    .getCreditAccountId())) {
                String contraPseudonym = transactionScreenControl.getF_CONTRAFIRSTPSEUDONYM();
                if (!StringUtils.isBlank(contraPseudonym)) {
                    String creditAccountNumber = PaymentSwiftUtils.retrievePsuedonymAcctId(
                            swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getCurrencyCode(),
                            remittanceDto.getBranchSortCode(), "CURRENCY", contraPseudonym);
                    swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                            .setCreditAccountId(creditAccountNumber);
                }

            }
        }

    }

    /**
     * @param swiftRemittanceRq
     * @return
     */
    private String populateDebitTransactionCode(SwiftRemittanceRq swiftRemittanceRq) {
        String transactionCode;
        if (!StringUtils.isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getTransactionCode())) {
            transactionCode = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getTransactionCode();
        }
        else {
            transactionCode = SwiftRemittanceMessageHelper.getTransactioncodeFromModuleConfig(
                    swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getTransactionType());
            swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().setTransactionCode(transactionCode);
        }
        return transactionCode;
    }

    /**
     * @param transactionCode
     * @return
     */
    private String getExchangeRateType(String transactionCode) {
        IBOMisTransactionCodes misTransactionCodes = SwiftRemittanceMessageHelper.getMisTransactionCodes(transactionCode);
        return misTransactionCodes.getF_EXCHANGERATETYPE();
    }

    /**
     * @param swiftRemittanceRq
     */
    private void populateCreditCurrencyAndAmount(SwiftRemittanceRq swiftRemittanceRq) {
        if (StringUtils.isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                .getCreditAmount().getIsoCurrencyCode())) {
            swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls().getCreditAmount()
                    .setIsoCurrencyCode(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getCurrencyCode());
        }
        // Need to derive CreditAmount from ExchangeRate
    }
    
    /**
     * @param str
     * @return
     */
    private String get35CharacterTextLine(String str) {
        String spaceConstant = " ";
        String output;
        if (!StringUtils.isBlank(str) && !str.equals(spaceConstant) && str.length() > 35) {
            output = str.substring(0, 35);
            return output;
        }
        else
        {
            return str;
        }
        
    }
    

    /**
     * Method Description:This method truncated the text fields passed
     * 
     * @param swiftRemittanceRq
     * @return
     */
    private SwiftRemittanceRq truncateTextFieldsTo35Char(SwiftRemittanceRq swiftRemittanceRq) {
        swiftRemittanceRq.getInitiateSwiftMessageRqDtls()
                .setBeneficiaryCustomerAndInstitution(trunctTextFieldsOfBeneficiaryCust(swiftRemittanceRq));

        swiftRemittanceRq.getInitiateSwiftMessageRqDtls()
                .setOrderingCustomerAndInstitution(trunctTextFieldsOfOrderingCustomer(swiftRemittanceRq));

        swiftRemittanceRq.getInitiateSwiftMessageRqDtls()
                .setIntermediaryDetails(trunctTextFieldsOfIntermediaryDetails(swiftRemittanceRq));

        swiftRemittanceRq.getInitiateSwiftMessageRqDtls()
                .setRemittanceDetails(trunctTextFieldsOfRemittanceDetails(swiftRemittanceRq));

        return swiftRemittanceRq;
    }

    private BeneficiaryCustomerAndInstitution trunctTextFieldsOfBeneficiaryCust(SwiftRemittanceRq swiftRemittanceRq) {
        BeneficiaryCustomerAndInstitution beneInfo = new BeneficiaryCustomerAndInstitution();
        if (null != swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution()) {
            beneInfo = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getBeneficiaryCustomerAndInstitution();
            if (null != beneInfo.getBeneficiaryCustomer()
                    && null != beneInfo.getBeneficiaryCustomer().getBeneficiaryCustDetails()) {
                TextLines4 beneCustInfo = beneInfo.getBeneficiaryCustomer().getBeneficiaryCustDetails();
                beneCustInfo.setTextLine1(get35CharacterTextLine(beneCustInfo.getTextLine1()));
                beneCustInfo.setTextLine2(get35CharacterTextLine(beneCustInfo.getTextLine2()));
                beneCustInfo.setTextLine3(get35CharacterTextLine(beneCustInfo.getTextLine3()));
                beneCustInfo.setTextLine4(get35CharacterTextLine(beneCustInfo.getTextLine4()));
                beneInfo.getBeneficiaryCustomer().setBeneficiaryCustDetails(beneCustInfo);
            }

            if (null != beneInfo.getBeneficiaryInstitution()
                    && (null != beneInfo.getBeneficiaryInstitution().getBeneficiaryInstDetails())) {
                TextLines4 beneInstInfo = beneInfo.getBeneficiaryInstitution().getBeneficiaryInstDetails();
                beneInstInfo.setTextLine1(get35CharacterTextLine(beneInstInfo.getTextLine1()));
                beneInstInfo.setTextLine2(get35CharacterTextLine(beneInstInfo.getTextLine2()));
                beneInstInfo.setTextLine3(get35CharacterTextLine(beneInstInfo.getTextLine3()));
                beneInstInfo.setTextLine4(get35CharacterTextLine(beneInstInfo.getTextLine4()));
                beneInfo.getBeneficiaryInstitution().setBeneficiaryInstDetails(beneInstInfo);
            }

        }
        return beneInfo;
    }

    private OrderingCustomerAndInstitution trunctTextFieldsOfOrderingCustomer(SwiftRemittanceRq swiftRemittanceRq) {
        OrderingCustomerAndInstitution orderingInfo = new OrderingCustomerAndInstitution();
        if (null != swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution()) {
            orderingInfo = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getOrderingCustomerAndInstitution();
            if (null != orderingInfo.getOrderingCustomer() && null != orderingInfo.getOrderingCustomer().getOrderingCustDetails()) {
                TextLines4 ordCustInfo = orderingInfo.getOrderingCustomer().getOrderingCustDetails();
                ordCustInfo.setTextLine1(get35CharacterTextLine(ordCustInfo.getTextLine1()));
                ordCustInfo.setTextLine2(get35CharacterTextLine(ordCustInfo.getTextLine2()));
                ordCustInfo.setTextLine3(get35CharacterTextLine(ordCustInfo.getTextLine3()));
                ordCustInfo.setTextLine4(get35CharacterTextLine(ordCustInfo.getTextLine4()));
                orderingInfo.getOrderingCustomer().setOrderingCustDetails(ordCustInfo);
            }

            if (null != orderingInfo.getOrderingInstitution()
                    && null != orderingInfo.getOrderingInstitution().getOrderingInstitutionDtl()) {
                OrderingInstitutionDtl ordInstInfo = orderingInfo.getOrderingInstitution().getOrderingInstitutionDtl();
                ordInstInfo.setOrderingInstitutionDtl1(get35CharacterTextLine(ordInstInfo.getOrderingInstitutionDtl1()));
                ordInstInfo.setOrderingInstitutionDtl2(get35CharacterTextLine(ordInstInfo.getOrderingInstitutionDtl2()));
                ordInstInfo.setOrderingInstitutionDtl3(get35CharacterTextLine(ordInstInfo.getOrderingInstitutionDtl3()));
                ordInstInfo.setOrderingInstitutionDtl4(get35CharacterTextLine(ordInstInfo.getOrderingInstitutionDtl4()));
                orderingInfo.getOrderingInstitution().setOrderingInstitutionDtl(ordInstInfo);
            }
        }

        return orderingInfo;
    }

    private IntermediaryDetails trunctTextFieldsOfIntermediaryDetails(SwiftRemittanceRq swiftRemittanceRq) {
        IntermediaryDetails payToIntMediaryDtls = new IntermediaryDetails();
        if (null != swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails()) {
            payToIntMediaryDtls = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails();
            if (null != swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails()
                    && null != payToIntMediaryDtls.getPayTo().getPayToDetails()) {
                PayDtlsText payToDtls = payToIntMediaryDtls.getPayTo().getPayToDetails();
                payToDtls.setPayDtls1(get35CharacterTextLine(payToDtls.getPayDtls1()));
                payToDtls.setPayDtls2(get35CharacterTextLine(payToDtls.getPayDtls2()));
                payToDtls.setPayDtls3(get35CharacterTextLine(payToDtls.getPayDtls3()));
                payToDtls.setPayDtls4(get35CharacterTextLine(payToDtls.getPayDtls4()));
                payToIntMediaryDtls.getPayTo().setPayToDetails(payToDtls);
            }

            if (null != payToIntMediaryDtls.getIntermediary()
                    && null != payToIntMediaryDtls.getIntermediary().getIntermediaryDetails()) {
                TextLines4 intermediaryTxt = payToIntMediaryDtls.getIntermediary().getIntermediaryDetails();
                intermediaryTxt.setTextLine1(get35CharacterTextLine(intermediaryTxt.getTextLine1()));
                intermediaryTxt.setTextLine2(get35CharacterTextLine(intermediaryTxt.getTextLine2()));
                intermediaryTxt.setTextLine3(get35CharacterTextLine(intermediaryTxt.getTextLine3()));
                intermediaryTxt.setTextLine4(get35CharacterTextLine(intermediaryTxt.getTextLine4()));
                payToIntMediaryDtls.getIntermediary().setIntermediaryDetails(intermediaryTxt);
            }
        }

        return payToIntMediaryDtls;
    }

    private RemittanceDetails trunctTextFieldsOfRemittanceDetails(SwiftRemittanceRq swiftRemittanceRq) {
        RemittanceDetails remDtsl = new RemittanceDetails();
        if (null != swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails()) {
            remDtsl = swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getRemittanceDetails();
            if (null != remDtsl.getBankToBankInfo()) {
                BankToBankInfoDtls b2bInfo = remDtsl.getBankToBankInfo();
                b2bInfo.setBankToBankInfo1(get35CharacterTextLine(b2bInfo.getBankToBankInfo1()));
                b2bInfo.setBankToBankInfo2(get35CharacterTextLine(b2bInfo.getBankToBankInfo2()));
                b2bInfo.setBankToBankInfo3(get35CharacterTextLine(b2bInfo.getBankToBankInfo3()));
                b2bInfo.setBankToBankInfo4(get35CharacterTextLine(b2bInfo.getBankToBankInfo4()));
                b2bInfo.setBankToBankInfo5(get35CharacterTextLine(b2bInfo.getBankToBankInfo5()));
                b2bInfo.setBankToBankInfo6(get35CharacterTextLine(b2bInfo.getBankToBankInfo6()));
                remDtsl.setBankToBankInfo(b2bInfo);
            }

            if (null != remDtsl.getSenderToReceiverInfo()) {
                TextLines6 senderReceiverInfo = remDtsl.getSenderToReceiverInfo();
                senderReceiverInfo.setTextLine1(get35CharacterTextLine(senderReceiverInfo.getTextLine1()));
                senderReceiverInfo.setTextLine2(get35CharacterTextLine(senderReceiverInfo.getTextLine2()));
                senderReceiverInfo.setTextLine3(get35CharacterTextLine(senderReceiverInfo.getTextLine3()));
                senderReceiverInfo.setTextLine4(get35CharacterTextLine(senderReceiverInfo.getTextLine4()));
                senderReceiverInfo.setTextLine5(get35CharacterTextLine(senderReceiverInfo.getTextLine5()));
                senderReceiverInfo.setTextLine6(get35CharacterTextLine(senderReceiverInfo.getTextLine6()));
                remDtsl.setSenderToReceiverInfo(senderReceiverInfo);
            }

            if (null != remDtsl.getTermsAndConditionsInfo()) {
                TermsAndConditionsInfo termsAConditions = remDtsl.getTermsAndConditionsInfo();
                termsAConditions.setTAndCInfoLine1(get35CharacterTextLine(termsAConditions.getTAndCInfoLine1()));
                termsAConditions.setTAndCInfoLine2(get35CharacterTextLine(termsAConditions.getTAndCInfoLine2()));
                termsAConditions.setTAndCInfoLine3(get35CharacterTextLine(termsAConditions.getTAndCInfoLine3()));
                termsAConditions.setTAndCInfoLine4(get35CharacterTextLine(termsAConditions.getTAndCInfoLine4()));
                termsAConditions.setTAndCInfoLine5(get35CharacterTextLine(termsAConditions.getTAndCInfoLine5()));
                termsAConditions.setTAndCInfoLine6(get35CharacterTextLine(termsAConditions.getTAndCInfoLine6()));
                remDtsl.setTermsAndConditionsInfo(termsAConditions);
            }

            if (null != remDtsl.getRemittanceInfo()) {
                TextLines4 remInfoTxt = remDtsl.getRemittanceInfo();
                remInfoTxt.setTextLine1(get35CharacterTextLine(remInfoTxt.getTextLine1()));
                remInfoTxt.setTextLine2(get35CharacterTextLine(remInfoTxt.getTextLine2()));
                remInfoTxt.setTextLine3(get35CharacterTextLine(remInfoTxt.getTextLine3()));
                remInfoTxt.setTextLine4(get35CharacterTextLine(remInfoTxt.getTextLine4()));
                remDtsl.setRemittanceInfo(remInfoTxt);
            }
        }

        return remDtsl;

    }


}