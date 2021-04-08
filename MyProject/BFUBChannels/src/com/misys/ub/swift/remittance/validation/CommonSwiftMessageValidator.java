/*
 * ******************************************************************************
 * Copyright (c) 2018 Finastra Software Solutions Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Finastra Software Solutions Ltd.
 * Use is subject to license terms.
 * ******************************************************************************
 */
package com.misys.ub.swift.remittance.validation;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.misys.bankfusion.calendar.IBusinessDate;
import com.misys.bankfusion.calendar.ICalendarService;
import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.bankfusion.subsystem.persistence.IPersistenceObjectsFactory;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.cbs.common.constants.CBSConstants;
import com.misys.cbs.refresh.exception.SyncException;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.datacenter.DataCenterCommonUtils;
import com.misys.ub.payment.swift.DBUtils.DBUtils;
import com.misys.ub.payment.swift.utils.MFInputOutPutKeys;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.swift.remittance.dto.RemittanceProcessDto;
import com.misys.ub.swift.remittance.process.SwiftEventCodes;
import com.misys.ub.swift.remittance.process.SwiftRemittanceMessageHelper;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_RemittanceTable;
import com.trapedza.bankfusion.exceptions.CollectedEventsDialogException;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.types.header.EventParameters;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;

/**
 * @author hargupta
 *
 */
/**
 * @author machamma.devaiah
 *
 */
public class CommonSwiftMessageValidator {

    private transient final static Log LOGGER = LogFactory.getLog(CommonSwiftMessageValidator.class);
    private ValidationHelper helper = new ValidationHelper();

    /**
     * @param swiftRemittanceRq
     * @param remittanceDto
     * @return
     * @throws SyncException
     */
    public RsHeader validateSwiftRemittanceRquest(SwiftRemittanceRq swiftRemittanceRq, RemittanceProcessDto remittanceDto) {
        RsHeader rsHeader = new RsHeader();
        MessageStatus status = new MessageStatus();
        status.setOverallStatus(PaymentSwiftConstants.OVERALL_SUCCESS_STATUS);
        rsHeader.setStatus(status);
        if (LOGGER.isInfoEnabled())
            LOGGER.info("START CommonSwiftMessageValidator");

        // validateMessageReferenceExists
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.OVERALL_SUCCESS_STATUS)) {
            rsHeader = validateMessageReferenceExists(
                    swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getMessageReference(), remittanceDto,
                    rsHeader);
        }

        // validateSwiftCoreMandatoryElements
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.OVERALL_SUCCESS_STATUS)) {
            rsHeader = validateSwiftCoreMandatoryElements(swiftRemittanceRq, remittanceDto, rsHeader);
        }

        // validateSwiftRemittanceData
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.OVERALL_SUCCESS_STATUS)) {
            rsHeader = validateSwiftRemittanceData(swiftRemittanceRq, remittanceDto, rsHeader);
        }

        // validateSwtMsgNostroAccount
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.OVERALL_SUCCESS_STATUS)) {
            rsHeader = validateSwtMsgNostroAccount(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                    .getCreditPostingDtls().getCreditAccountId(), remittanceDto, rsHeader);
        }

        return rsHeader;

    }

    /**
     * @param swiftRemittanceRq
     * @param remittanceDto
     * @return
     * @throws SyncException
     */
    private RsHeader validateSwiftCoreMandatoryElements(SwiftRemittanceRq swiftRemittanceRq, RemittanceProcessDto remittanceDto,
            RsHeader rsHeader) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN  validateSwiftCoreMandatoryElements");

        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS) && StringUtils.isEmpty(swiftRemittanceRq
                .getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls().getCreditAccountId())) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_MANDATORY_ENTRY_CB05_INT, "Credit AccountId ");
        }
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)
                && StringUtils.isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                        .getCreditPostingDtls().getCreditAmount().getIsoCurrencyCode())) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_MANDATORY_ENTRY_CB05_INT, "Credit Currency ");
        }
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)
                && (swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                        .getCreditAmount().getAmount()).compareTo(BigDecimal.ZERO) <= 0) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_MANDATORY_ENTRY_CB05_INT, "Credit Amount ");
        }
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS) && StringUtils.isEmpty(swiftRemittanceRq
                .getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls().getCreditExchangeRateType())) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_MANDATORY_ENTRY_CB05_INT, "Credit ExchangeRateType ");
        }

        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)
                && StringUtils.isEmpty(remittanceDto.getCreditTransactionCode())) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_MANDATORY_ENTRY_CB05_INT, "Credit TransactionCode ");
        }

        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS) && StringUtils.isEmpty(swiftRemittanceRq
                .getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls().getDebitAccountId())) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_MANDATORY_ENTRY_CB05_INT, "Debit AccountId ");
        }

        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)
                && StringUtils.isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                        .getDebitPostingDtls().getDebitAmount().getIsoCurrencyCode())) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_MANDATORY_ENTRY_CB05_INT, "Debit Currency ");
        }

        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS) && StringUtils.isEmpty(swiftRemittanceRq
                .getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls().getDebitExchangeRateType())) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_MANDATORY_ENTRY_CB05_INT, "Debit ExchangeRateType ");
        }

        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS) && StringUtils
                .isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getTransactionCode())) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_MANDATORY_ENTRY_CB05_INT, "Debit TransactionCode ");
        }

        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)
                && StringUtils.isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo()
                        .getPayToIdentifierCode())
                && (StringUtils.isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo()
                        .getPayToDetails().getPayDtls1()))
                && (StringUtils
                        .isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo()
                                .getPayToPartyIdentifier())
                        || StringUtils.isEmpty(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getIntermediaryDetails().getPayTo()
                                .getPayToPartyIdentifierClearingCode()))) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_CMN_MANDATORY_ENTRY_CB05_INT, "Pay To Identifier Detail");

        }

        // debit account ccy entered not same as account ccy
        String debitAcctCcy = getAccountCurrency(
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls().getDebitAccountId(),
                remittanceDto, rsHeader);
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS) && !debitAcctCcy.equals(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls()
                .getDebitAmount().getIsoCurrencyCode())) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CCY_ENTERED_NOT_ACCT_CCY, StringUtils.EMPTY);
        }

        // credit account ccy entered not same as account ccy
        String creditAcctCcy = getAccountCurrency(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                .getCreditPostingDtls().getCreditAccountId(), remittanceDto, rsHeader);
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS) && !creditAcctCcy.equals(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                .getCreditAmount().getIsoCurrencyCode())) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CCY_ENTERED_NOT_ACCT_CCY, StringUtils.EMPTY);
        }
        
        //invalid debit transaction code
        if(null == getMisTransactionCodes(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getTransactionCode()) ) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_INVALID_DEBIT_TRANS_CODE, StringUtils.EMPTY);
        }

        return rsHeader;

    }

    /**
     * @param swiftRemittanceRq
     * @param remittanceDto
     * @return
     */
    private RsHeader validateSwiftRemittanceData(SwiftRemittanceRq swiftRemittanceRq, RemittanceProcessDto remittanceDto,
            RsHeader rsHeader) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN  validateSwiftRemittanceData");

        SwiftMessageValidatorFactory swtMsgValidatorFactory = new SwiftMessageValidatorFactory();
        // validateRemittanceAmount
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)) {
            rsHeader = validateRemittanceAmount(swiftRemittanceRq, remittanceDto, rsHeader);
        }

        // validateSwiftMessage
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)) {
            rsHeader = swtMsgValidatorFactory.validateSwiftMessage(swiftRemittanceRq).validate(swiftRemittanceRq, remittanceDto);
        }

        // validateCreditAccountNumber
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)) {
            rsHeader = validateCreditAccountNumber(swiftRemittanceRq, remittanceDto, rsHeader);
        }

        // validate credit account customer for SWIFT enabled
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)) {
            rsHeader = checkIfSwiftEnabled(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                    .getCreditPostingDtls().getCreditAccountId(), rsHeader, remittanceDto);
        }

        // validateDebitAccountNumber
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)) {
            rsHeader = validateDebitAccountNumber(swiftRemittanceRq, remittanceDto, rsHeader);
        }

        // validate debit account customer for SWIFT enabled
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)) {
            rsHeader = checkIfSwiftEnabled(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                    .getDebitPostingDtls().getDebitAccountId(), rsHeader, remittanceDto);
        }

        // validateValueDate
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)) {
            rsHeader = validateValueDate(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getMessageDetails().getValueDate(),
                    rsHeader);
        }
        return rsHeader;
    }

    private RsHeader checkIfSwiftEnabled(String accountID, RsHeader rsHeader, RemittanceProcessDto remittanceDto) {
        if (accountID != null) {
            String customerCode = DataCenterCommonUtils.readAccount(accountID).getAccountDetails().getAccountInfo()
                    .getAcctBasicDetails().getCustomerShortDetails().getCustomerId();
            IBOSwtCustomerDetail swtCustDtl = (IBOSwtCustomerDetail) BankFusionThreadLocal.getPersistanceFactory()
                    .findByPrimaryKey(IBOSwtCustomerDetail.BONAME, customerCode, true);
            if (swtCustDtl == null || swtCustDtl.getF_SWTACTIVE().equals("N")) {
                SubCode subCode = new SubCode();
                MessageStatus status = new MessageStatus();
                subCode.setCode(String.valueOf(ChannelsEventCodes.E_INACTIVE_SWIFT_CUSTOMER));
                ArrayList<Object> paramsList = new ArrayList<>();
                EventParameters parameter = new EventParameters();
                parameter.setEventParameterValue(customerCode);
                paramsList.add(customerCode);
                subCode.addParameters(parameter);
                subCode.setDescription(helper.getErrorDescription(subCode.getCode(), paramsList.toArray(), remittanceDto.getEnv()));
                subCode.setFieldName(CommonConstants.EMPTY_STRING);
                subCode.setSeverity(CBSConstants.ERROR);
                status.addCodes(subCode);
                status.setOverallStatus(PaymentSwiftConstants.ERROR_STATUS);
                rsHeader.setStatus(status);
            }

        }
        return rsHeader;
    }

    /**
     * @param valueDate
     */
    private RsHeader validateValueDate(Date valueDate, RsHeader rsHeader) {
        ICalendarService calendarService = (ICalendarService) ServiceManagerFactory.getInstance().getServiceManager()
                .getServiceForName(ICalendarService.SERVICE_NAME);
        IBusinessDate valueDateWOTP = calendarService.getBusinessDate(valueDate).withoutTimeParts();
        IBusinessDate businessDateWOTP = calendarService.getBusinessDate(SystemInformationManager.getInstance().getBFBusinessDate())
                .withoutTimeParts();
        if (businessDateWOTP.isAfterDate(valueDateWOTP)) {
            rsHeader = helper.setErrorResponse(40015085, StringUtils.EMPTY);

        }
        return rsHeader;
    }

    /**
     * @param swiftRemittanceRq
     * @param remittanceDto
     */
    private RsHeader validateRemittanceAmount(SwiftRemittanceRq swiftRemittanceRq, RemittanceProcessDto remittanceDto,
            RsHeader rsHeader) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN  validateRemittanceAmount");

        rsHeader = validateCreditExchangeRateTolerance(swiftRemittanceRq, remittanceDto);
        if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.SUCCESS)) {
            rsHeader = validateDebitExchangeRateTolerance(swiftRemittanceRq, remittanceDto);
        }
        return rsHeader;
    }

    /**
     * @param swiftRemittanceRq
     * @param remittanceDto
     */
    private RsHeader validateCreditExchangeRateTolerance(SwiftRemittanceRq swiftRemittanceRq, RemittanceProcessDto remittanceDto) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN  validateCreditExchangeRateTolerance");

        return SwiftRemittanceMessageHelper.checkExchangeRateTolerance(
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                        .getCreditExchangeRate(),
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getInstructedAmount()
                        .getIsoCurrencyCode(),
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls().getCreditAmount()
                        .getIsoCurrencyCode(),
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                        .getCreditExchangeRateType(),
                remittanceDto.getEnv());

    }

    /**
     * @param swiftRemittanceRq
     * @param remittanceDto
     */
    private RsHeader validateDebitExchangeRateTolerance(SwiftRemittanceRq swiftRemittanceRq, RemittanceProcessDto remittanceDto) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN  validateDebitExchangeRateTolerance");

        return SwiftRemittanceMessageHelper.checkExchangeRateTolerance(
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls()
                        .getDebitExchangeRate(),
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getInstructedAmount()
                        .getIsoCurrencyCode(),
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls().getDebitAmount()
                        .getIsoCurrencyCode(),
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls()
                        .getDebitExchangeRateType(),
                remittanceDto.getEnv());

    }

    /**
     * @param swiftRemittanceRq
     * @param remittanceDto
     */
    private RsHeader validateCreditAccountNumber(SwiftRemittanceRq swiftRemittanceRq, RemittanceProcessDto remittanceDto,
            RsHeader rsHeader) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN  validateCreditAccountNumber");

        return swtMsgAccountValidation(swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails()
                .getCreditPostingDtls().getCreditAccountId(), remittanceDto, rsHeader);

    }

    /**
     * @param swiftRemittanceRq
     * @param remittanceDto
     */
    private RsHeader validateDebitAccountNumber(SwiftRemittanceRq swiftRemittanceRq, RemittanceProcessDto remittanceDto,
            RsHeader rsHeader) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN  validateDebitAccountNumber");

        return swtMsgAccountValidation(
                swiftRemittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getDebitPostingDtls().getDebitAccountId(),
                remittanceDto, rsHeader);
    }

    /**
     * @param accountId
     * @param remittanceDto
     */
    private RsHeader swtMsgAccountValidation(String accountId, RemittanceProcessDto remittanceDto, RsHeader rsHeader) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN  swtMsgAccountValidation");
        Map map = Maps.newHashMap();
        map.put(MFInputOutPutKeys.VALIDATE_ACCOUNTID, accountId);
        try {
            MFExecuter.executeMF(PaymentSwiftConstants.UB_SWT_REMITTACEACCOUNTVALIDATE_SRV, remittanceDto.getEnv(), map);
        }
        catch (CollectedEventsDialogException collectedEventexc) {
            SubCode subCode = new SubCode();
            MessageStatus status = new MessageStatus();
            subCode.setCode(String.valueOf(collectedEventexc.getEvents().iterator().next().getEventNumber()));
            Object param = new Object();
            ArrayList<Object> paramsList = new ArrayList<>();
            for (int j = 0; j < collectedEventexc.getEvents().iterator().next().getDetails().length; j++) {
                EventParameters parameter = new EventParameters();
                param = collectedEventexc.getEvents().iterator().next().getDetails()[j];
                if (null != param) {
                    parameter.setEventParameterValue(param.toString());
                    paramsList.add(param.toString());
                }
                subCode.addParameters(parameter);
            }
            subCode.setDescription(helper.getErrorDescription(subCode.getCode(), paramsList.toArray(), remittanceDto.getEnv()));
            subCode.setFieldName(CommonConstants.EMPTY_STRING);
            subCode.setSeverity(CBSConstants.ERROR);
            status.addCodes(subCode);
            status.setOverallStatus(PaymentSwiftConstants.ERROR_STATUS);
            rsHeader.setStatus(status);
        }

        return rsHeader;
    }

    /**
     * @param accountId
     * @param remittanceDto
     */
    private RsHeader validateSwtMsgNostroAccount(String accountId, RemittanceProcessDto remittanceDto, RsHeader rsHeader) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN  validateSwtMsgNostroAccount");
        Map map = Maps.newHashMap();
        map.put(MFInputOutPutKeys.NOSTRO_ACCOUNTID, accountId);

        HashMap result = MFExecuter.executeMF(PaymentSwiftConstants.CB_TXN_IDENTIFYNOSTROACCOUNT_SRV, remittanceDto.getEnv(), map);
        if (null != result) {
            rsHeader = (RsHeader) result.get("rsHeader");
        }

        return rsHeader;
    }

    /**
     * @param senderReference
     * @param remittanceDto
     * @return
     */
    private RsHeader validateMessageReferenceExists(String messageReference, RemittanceProcessDto remittanceDto,
            RsHeader rsHeader) {
        StringBuilder query = new StringBuilder();
        final String[] input = new String[] { DBUtils.WHERE, IBOUB_SWT_RemittanceTable.UBTRANSACTIONREFERENCE,
                DBUtils.QUERY_PARAM };
        query = Joiner.on(DBUtils.SPACE).appendTo(query, input);
        ArrayList queryParams = new ArrayList();
        queryParams.add(messageReference);
        IBOUB_SWT_RemittanceTable result = (IBOUB_SWT_RemittanceTable) remittanceDto.getEnv().getFactory()
                .findFirstByQuery(IBOUB_SWT_RemittanceTable.BONAME, query.toString(), queryParams, true);
        if (null != result && !result.getDataMap().isEmpty()) {
            rsHeader = helper.setErrorResponse(SwiftEventCodes.E_CB_ALLREADY_EXIST_CB, "Message Reference");
        }
        return rsHeader;
    }

    /**
     * @param accountID
     * @param remittanceDto
     * @param rsHeader
     * @return
     */
    private String getAccountCurrency(String accountID, RemittanceProcessDto remittanceDto, RsHeader rsHeader) {
        IPersistenceObjectsFactory factory = remittanceDto.getEnv().getFactory();
        IBOAttributeCollectionFeature accountBO = (IBOAttributeCollectionFeature) factory
                .findByPrimaryKey(IBOAttributeCollectionFeature.BONAME, accountID, true);
        String isoCurrencyCode = StringUtils.EMPTY;
        if (accountBO != null) {
            isoCurrencyCode = accountBO.getF_ISOCURRENCYCODE();
        }
        return isoCurrencyCode;
    }
    
    

    /**
     * @param misCode
     * @return
     */
    private IBOMisTransactionCodes getMisTransactionCodes(String misCode) {
        IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
        IBOMisTransactionCodes misCodeBO = (IBOMisTransactionCodes) factory.findByPrimaryKey(IBOMisTransactionCodes.BONAME, misCode, true);
        return misCodeBO;
    }
}
