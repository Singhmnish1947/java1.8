/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 */
package com.trapedza.bankfusion.atm.sparrow.helper;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.atm.sparrow.configuration.ATMConfigCache;
import com.trapedza.bankfusion.atm.sparrow.configuration.ATMControlDetails;
import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowFinancialMessage;
import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOATMCIB;
import com.trapedza.bankfusion.bo.refimpl.IBOATMCardAccountMap;
import com.trapedza.bankfusion.bo.refimpl.IBOATMCardDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOCurrency;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.BusinessValidatorBean;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.features.extensionpoints.PasswordProtectedConstants;
import com.trapedza.bankfusion.persistence.exceptions.FinderException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.utils.BankFusionMessages;

/**
 * The ATMMessageValidator contains common validations for the ATM Sparrow messages.
 */
public class ATMMessageValidator {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    /**
	 */
    /**
     * This is used to return the transaction details
     */
    // private static HashMap validTxnDetails = new HashMap();
    /**
     * Holds the reference for logger object
     */
    private transient final static Log logger = LogFactory.getLog(ATMMessageValidator.class.getName());
    /**
     * Where clause to validate the ATM source
     */
    private static final String atmSourceWhereClause = "WHERE " + IBOATMCIB.BRANCHCODE + "=? AND " + IBOATMCIB.ISOCOUNTRYCODE
            + "=? AND " + IBOATMCIB.IMDCODE + "=?";
    /**
     * Where clause to validate card account mapping
     */
    private static final String cardAccMapWhereClause = "WHERE " + IBOATMCardAccountMap.ATMCARDNUMBER + "=? AND "
            + IBOATMCardAccountMap.ACCOUNTID + "=?";

    /**
     * Where clause to validate currency
     */
    private static final String currencyWhereClause = "WHERE " + IBOCurrency.NumericISOCurrencyCode + "=?";

    /**
     * holds the shared switch value
     */
    private boolean sharedSwitch = false;

    ATMControlDetails controlDetails = null;

    public static final String LOCAL_MESSGE_TYPE = "local";

    public static final String EXTERNAL_MESSAGE_TYPE = "external";
    private static final String External_Sale_REQUEST = "622";
    private static final String External_CASH_REQUEST = "621";
    private static final String External_CASHREQUEST = "624";
    private static final String POS_REFUND = "623";
    private String errorMessage = CommonConstants.EMPTY_STRING;
    /**
     * Instance of ATMHelper
     */
    ATMHelper atmHelper = new ATMHelper();

    /**
     * Constructor
     */
    public ATMMessageValidator() {

    }

    /**
     * This method provides common validation for all the ATM sparrow messages. This includes
     * validation of ATM source/destination, ATM transaction code validation and ATM Card Number
     * validation. The transaction narrative is returned for use in notification messages.
     */
    public void validateMessage(ATMSparrowMessage atmSparrowMessage, BankFusionEnvironment env, String localOrExternal) {
        // get atm config details
        getConfigurationInfo(env);
        // ATMSource Validation
        String messageType = atmSparrowMessage.getMessageType() + atmSparrowMessage.getTransactionType();
        // artf46639 changes started
        if ((messageType.equals(POS_REFUND) || messageType.equals(External_CASH_REQUEST)
                || messageType.equals(External_CASHREQUEST) || messageType.equals(External_Sale_REQUEST))
                && (!isCardNumberValid(atmSparrowMessage.getCardNumber(), env))) {
            validateSource(atmSparrowMessage, env);
        }
        else if ((messageType.equals(POS_REFUND) || messageType.equals(External_CASH_REQUEST)
                || messageType.equals(External_CASHREQUEST) || messageType.equals(External_Sale_REQUEST))
                && (isCardNumberValid(atmSparrowMessage.getCardNumber(), env))) {
            validateSource(atmSparrowMessage, env);
            if (!atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
                validateDestination(atmSparrowMessage, env);
            }
        }
        // artf46639 changes end
        else if (localOrExternal.equals(LOCAL_MESSGE_TYPE)) {
            if (checkForValidForcePost(atmSparrowMessage, env)) {
                if (!sharedSwitch) {
                    validateSource(atmSparrowMessage, env);
                }
                else if (sharedSwitch) {
                    // ATM Destination Validation
                    if (!((atmSparrowMessage.getMessageType() + atmSparrowMessage.getTransactionType())
                            .equals(ATMConstants.LOCAL_LORO)))
                        validateDestination(atmSparrowMessage, env);
                    // ATM Card Issuer Id validation
                    if (!((atmSparrowMessage.getMessageType() + atmSparrowMessage.getTransactionType())
                            .equals(ATMConstants.LOCAL_LORO)))
                        if (!atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
                            validateCardIssuerId(atmSparrowMessage, env);
                        }
                }
            }
        }
        else if (localOrExternal.equals(EXTERNAL_MESSAGE_TYPE)) {
            // ATM Destination Validation
            validateDestination(atmSparrowMessage, env);
            // ATM Card Issuer Id validation
            if (!atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
                validateCardIssuerId(atmSparrowMessage, env);
            }
        }
        if (atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            return;
        }
        // ATM Transaction code validation
        validateTransactionCode(atmSparrowMessage, env);
        if (atmSparrowMessage.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            return;
        }
        // Updated for artf40294 starts;
        String message = atmSparrowMessage.getMessageType() + atmSparrowMessage.getTransactionType();
        boolean isOffUsTransaction = false;
        ATMMessageValidator atmMessageValidator = new ATMMessageValidator();
        boolean isCardValid = atmMessageValidator.isCardNumberValid(atmSparrowMessage.getCardNumber(), env);

        if (!isCardValid) {
            isOffUsTransaction = true;
        }
        if (message.equals("623") || isOffUsTransaction) {
            atmSparrowMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
            return;
        }
        // Updated for artf40294 ends;
        if (ignoreCardvalidations(atmSparrowMessage)) {
            return;
        }
        validateCardId(atmSparrowMessage, env);
    }

    /**
     * This method whether the ATM sparrow message is a LORO message.
     * 
     */
    private boolean ignoreCardvalidations(ATMSparrowMessage atmSparrowMessage) {
        boolean isLoro = false;
        String message = atmSparrowMessage.getMessageType() + atmSparrowMessage.getTransactionType();
        if (message.equals("599") || message.equals("099") || message.equals("899") || message.equals("625")
                || message.equals("626") || message.equals("725") || message.equals("726")) {
            isLoro = true;
        }
        return isLoro;
    }

    /**
     * This method validates the ATM source in the ATM sparrow message.
     */
    private void validateSource(ATMSparrowMessage atmSparrowMessage, BankFusionEnvironment env) {

        ArrayList params = new ArrayList();
        params.add(atmSparrowMessage.getSourceBranchCode());
        params.add(atmSparrowMessage.getSourceCountryCode());
        params.add(atmSparrowMessage.getSourceIMD());
        List branchBO = null;
        try {
            branchBO = env.getFactory().findByQuery(IBOATMCIB.BONAME, atmSourceWhereClause, params, null);
        }
        catch (BankFusionException bfe) {
            logger.error("Error while find by query for Branch for bmbranch: " + atmSparrowMessage.getSourceBranchCode(), bfe);
        }
        if (branchBO == null || branchBO.isEmpty()) {
            Object[] field = new Object[] { atmSparrowMessage.getSourceCountryCode(), atmSparrowMessage.getSourceIMD(),
                    atmSparrowMessage.getSourceBranchCode() };
            atmSparrowMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
            if (atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_0) ||
            // or condition added for artf46639
                    atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
                // populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                // ATMConstants.WARNING, 7500, BankFusionMessages.ERROR_LEVEL,
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.E_SRCE_COUNTRY_IMD_BRANCH_NOT_MAPPED, BankFusionMessages.ERROR_LEVEL, atmSparrowMessage,
                        field, env);
            }
            else if (atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_2)
                    || atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)
                    // Conditions for forcepost 5, 7, 8 added for artf46639
                    || atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_5)
                    || atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_7)
                    || atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_8)) {
                // populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                // ATMConstants.CRITICAL, 7501, BankFusionMessages.ERROR_LEVEL,
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                        ChannelsEventCodes.E_SRCE_COUNTRY_IMD_BRANCH_UNMAPPED_POST_NOT_POSTED, BankFusionMessages.ERROR_LEVEL,
                        atmSparrowMessage, field, env);
            }
        }
        else {
            atmSparrowMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
        }
    }

    public boolean validateDestination(String branchCode, String countryCode, String IMDCode, BankFusionEnvironment env) {
        boolean result = true;

        ArrayList params = new ArrayList();
        params.add(branchCode);
        params.add(countryCode);
        params.add(IMDCode);
        List branchBO = null;
        try {
            branchBO = env.getFactory().findByQuery(IBOATMCIB.BONAME, atmSourceWhereClause, params, null);
        }
        catch (BankFusionException bfe) {
        }
        if (branchBO == null || branchBO.isEmpty()) {
        	result = false;
        }
        else {
        	result = true;
        }
        return result;

    }

    /**
     * This method validates the ATM destination in the ATM sparrow message.
     */
    private void validateDestination(ATMSparrowMessage atmSparrowMessage, BankFusionEnvironment env) {

        ArrayList params = new ArrayList();
        params.add(atmSparrowMessage.getCardDestBranchCode());
        params.add(atmSparrowMessage.getCardDestCountryCode());
        params.add(atmSparrowMessage.getCardDestinationIMD());
        List branchBO = null;
        try {
            branchBO = env.getFactory().findByQuery(IBOATMCIB.BONAME, atmSourceWhereClause, params, null);
        }
        catch (BankFusionException bfe) {
            logger.error("Error while find by query for Branch for bmbranch: " + atmSparrowMessage.getCardDestBranchCode(), bfe);
        }
        if (branchBO == null || branchBO.isEmpty()) {
            atmSparrowMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
            Object[] field = new Object[] { atmSparrowMessage.getCardDestCountryCode(), atmSparrowMessage.getCardDestinationIMD(),
                    atmSparrowMessage.getCardDestBranchCode() };
            if (atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)
                    || atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
                // populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                // ATMConstants.WARNING, 7502, BankFusionMessages.ERROR_LEVEL,
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.E_DEST_COUNTRY_IMD_BRANCH_NOT_MAPPED, BankFusionMessages.ERROR_LEVEL, atmSparrowMessage,
                        field, env);
            }
            else if (atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_2)
                    || atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)
                    || atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_7)
                    || atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_5)
                    || atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_8)) {
                // populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                // ATMConstants.CRITICAL, 7503, BankFusionMessages.ERROR_LEVEL,
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                        ChannelsEventCodes.E_DEST_COUNTRY_IMD_BRANCH_UNMAPPED_POST_NOT_POSTED, BankFusionMessages.ERROR_LEVEL,
                        atmSparrowMessage, field, env);
            }
        }
        else {
            atmSparrowMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
        }
    }

    /**
     * This method validates the ATM transaction code in the ATM sparrow message and returns the
     * transaction description.
     */
    private void validateTransactionCode(ATMSparrowMessage atmSparrowMessage, BankFusionEnvironment env) {
        boolean result = isTransactionSupported(atmSparrowMessage.getMessageType() + atmSparrowMessage.getTransactionType(), env);
        if (result) {
            atmSparrowMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
            // String ubTransCode =
            // atmHelper.getBankTransactionCode(atmSparrowMessage.getMessageType()
            // + atmSparrowMessage.getTransactionType(), env);
            // //UBTransaction type mapped or not?
            // try {
            // IBOMisTransactionCodes misTransactionCodes =
            // (IBOMisTransactionCodes)env.getFactory().findByPrimaryKey(IBOMisTransactionCodes.BONAME,
            // ubTransCode);
            // } catch (FinderException bfe) {
            // Object[] field = new Object[] { ubTransCode };
            // atmSparrowMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
            // if
            // (atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_0))
            // {
            // populateErrorDetails (ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
            // ATMConstants.WARNING, 7508, BankFusionMessages.ERROR_LEVEL,
            // atmSparrowMessage, field, env);
            // } else if
            // (atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
            // ||
            // atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_2)
            // ||
            // atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)
            // ||
            // atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_7))
            // {
            // populateErrorDetails (ATMConstants.AUTHORIZED_MESSAGE_FLAG,
            // ATMConstants.CRITICAL, 7508, BankFusionMessages.ERROR_LEVEL,
            // atmSparrowMessage, field, env);
            // if (controlDetails != null) {
            // ubTransCode = controlDetails.getAtmTransactionType();
            // }
            // }
            // }
        }
        else {
            atmSparrowMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
            /*
             * String errorMessage = BankFusionMessages.getFormattedMessage(BankFusionMessages
             * .ERROR_LEVEL, 7506, env, new Object[] { atmSparrowMessage .getMessageType() +
             * atmSparrowMessage.getTransactionType() });
             */
            String errorMessage = BankFusionMessages.getInstance().getFormattedEventMessage(40407506,
                    new Object[] { atmSparrowMessage.getMessageType() + atmSparrowMessage.getTransactionType() },
                    BankFusionThreadLocal.getUserSession().getUserLocale());
            String errorStatus = null;
            if (atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)
                    || atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
                errorStatus = ATMConstants.WARNING;
                logger.warn(errorMessage);
            }
            else {
                errorStatus = ATMConstants.CRITICAL;
                logger.error(errorMessage);
            }
            atmSparrowMessage.setErrorCode(errorStatus);
            atmSparrowMessage.setErrorDescription(errorMessage);
        }
    }

    /**
     * This method validates the ATM Card number in the ATM sparrow message.
     */
    private void validateCardIssuerId(ATMSparrowMessage atmSparrowMessage, BankFusionEnvironment env) {

        ArrayList params = new ArrayList();
        params.add(atmSparrowMessage.getCardDestBranchCode());
        params.add(atmSparrowMessage.getCardDestCountryCode());
        params.add(atmSparrowMessage.getCardDestinationIMD());
        List branchBO = null;
        try {
            branchBO = env.getFactory().findByQuery(IBOATMCIB.BONAME, atmSourceWhereClause, params, null);
        }
        catch (BankFusionException bfe) {
            logger.error("Error while find by query for Branch for bmbranch: " + atmSparrowMessage.getCardDestBranchCode(), bfe);
        }
        if (branchBO == null || branchBO.isEmpty()) {
            atmSparrowMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
            Object[] field = new Object[] { atmSparrowMessage.getCardDestCountryCode(), atmSparrowMessage.getCardDestinationIMD(),
                    atmSparrowMessage.getCardDestBranchCode() };
            if (atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                // populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                // ATMConstants.WARNING, 7502, BankFusionMessages.ERROR_LEVEL,
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.E_DEST_COUNTRY_IMD_BRANCH_NOT_MAPPED, BankFusionMessages.ERROR_LEVEL, atmSparrowMessage,
                        field, env);
            }
            else if (atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_2)
                    || atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)) {
                // populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                // ATMConstants.CRITICAL, 7503, BankFusionMessages.ERROR_LEVEL,
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                        ChannelsEventCodes.E_DEST_COUNTRY_IMD_BRANCH_UNMAPPED_POST_NOT_POSTED, BankFusionMessages.ERROR_LEVEL,
                        atmSparrowMessage, field, env);
            }
        }
        else {
            atmSparrowMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
        }
    }

    /**
     * This method validates the ATM Card number in the ATM sparrow message.
     */
    private void validateCardId(ATMSparrowMessage atmSparrowMessage, BankFusionEnvironment env) {

        try {
            IBOATMCardDetails atmCardDetails = (IBOATMCardDetails) env.getFactory().findByPrimaryKey(IBOATMCardDetails.BONAME,
                    atmSparrowMessage.getCardNumber());
        }
        catch (BankFusionException bfe) {
            atmSparrowMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
            Object[] field = new Object[] { atmSparrowMessage.getCardNumber() };
            if (atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)
                    || atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
                // populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                // ATMConstants.WARNING, 7510, BankFusionMessages.ERROR_LEVEL,
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.E_INVALID_CARD, BankFusionMessages.ERROR_LEVEL, atmSparrowMessage, field, env);
            }
            else if (atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_2)
                    || atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)
                    || atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_7)
                    || atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_5)
                    || atmSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_8)) {
                // populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                // ATMConstants.CRITICAL, 7511, BankFusionMessages.ERROR_LEVEL,
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                        ChannelsEventCodes.E_INVALID_CARD_FORCE_POST_NOT_POSTED, BankFusionMessages.ERROR_LEVEL, atmSparrowMessage,
                        field, env);
            }
        }
    }

    /**
     * This method validates the ATM Source currency in the ATM sparrow message.
     */
    public void validateSourceCurrency(ATMSparrowFinancialMessage atmFinancialSparrowMessage, BankFusionEnvironment env) {
        ArrayList params = new ArrayList();
        params.add(atmFinancialSparrowMessage.getCurrencySourceAccount());
        List currencyBO = null;
        try {
            currencyBO = env.getFactory().findByQuery(IBOCurrency.BONAME, currencyWhereClause, params, null);
        }
        catch (BankFusionException bfe) {
            logger.error("Error while find by query for currency: " + atmFinancialSparrowMessage.getCurrencySourceAccount(), bfe);
        }
        if (currencyBO == null || currencyBO.isEmpty()) {
            atmFinancialSparrowMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
            Object[] field = new Object[] { atmFinancialSparrowMessage.getCurrencySourceAccount() };
            if (atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)
                    || atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
                // populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                // ATMConstants.WARNING, 7512, BankFusionMessages.ERROR_LEVEL,
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.E_INVALID_CURRENCY_CODE, BankFusionMessages.ERROR_LEVEL, atmFinancialSparrowMessage,
                        field, env);
            }
            else if (atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_2)
                    || atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)
                    || atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_7)
                    || atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_5)
                    || atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_8)) {
                // populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                // ATMConstants.CRITICAL, 7513, BankFusionMessages.ERROR_LEVEL,
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                        ChannelsEventCodes.E_INVALID_CURRENCY_CODE_FORCE_POST_NOT_POSTED, BankFusionMessages.ERROR_LEVEL,
                        atmFinancialSparrowMessage, field, env);
            }
        }

        boolean isCurrencyValid = false;
        isCurrencyValid = isCurrencyValid(atmFinancialSparrowMessage.getAccount(),
                atmFinancialSparrowMessage.getCurrencySourceAccount(), env);
        if (!isCurrencyValid) {
            if (atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)
                    || atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
                atmFinancialSparrowMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
                atmFinancialSparrowMessage.setErrorCode("Warning");
                atmFinancialSparrowMessage.setErrorDescription("Invalid Currency Code");
            }
            else if (atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_7)
                    || atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)
                    || atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_5)
                    || atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_8)) {
                String errorMessge = "Invalid Currency Code. Force Post Not Posted";
                atmFinancialSparrowMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
                atmFinancialSparrowMessage.setErrorCode(ATMConstants.CRITICAL);
                atmFinancialSparrowMessage.setErrorDescription(errorMessge);

            }
            return;
        }

    }

    /**
     * This method validates the ATM dispensed/destination currency in the ATM sparrow message.
     */
    public void validateDispensedCurrency(ATMSparrowFinancialMessage atmFinancialSparrowMessage, BankFusionEnvironment env) {

        ArrayList params = new ArrayList();
        params.add(atmFinancialSparrowMessage.getCurrencyDestDispensed());
        List currencyBO = null;
        try {
            currencyBO = env.getFactory().findByQuery(IBOCurrency.BONAME, currencyWhereClause, params, null);
        }
        catch (BankFusionException bfe) {
            logger.error("Error while find by query for currency: " + atmFinancialSparrowMessage.getCurrencyDestDispensed(), bfe);
        }
        if (currencyBO ==null || currencyBO.isEmpty()) {
            atmFinancialSparrowMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
            Object[] field = new Object[] { atmFinancialSparrowMessage.getCurrencyDestDispensed() };
            if (atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)
                    || atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
                // populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                // ATMConstants.WARNING, 7514, BankFusionMessages.ERROR_LEVEL,
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.E_INVALID_DISPENSED_CURRENCY, BankFusionMessages.ERROR_LEVEL,
                        atmFinancialSparrowMessage, field, env);
            }
            else if (atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_2)
                    || atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)
                    || atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_7)
                    || atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_5)
                    || atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_8)) {
                // populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                // ATMConstants.CRITICAL, 7515, BankFusionMessages.ERROR_LEVEL,
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                        ChannelsEventCodes.E_INVALID_DISPENSED_CURRENCY_FORCE_POST_NOT_POSTED, BankFusionMessages.ERROR_LEVEL,
                        atmFinancialSparrowMessage, field, env);
            }
        }

        // boolean isCurrencyValid = false;
        // isCurrencyValid =
        // isCurrencyValid(atmFinancialSparrowMessage.getAccount(),
        // atmFinancialSparrowMessage.getCurrencySourceAccount(), env);
        // if (!isCurrencyValid)
        // {
        // if
        // (atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)
        // ||
        // atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)
        // ) {
        // atmFinancialSparrowMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
        // atmFinancialSparrowMessage.setErrorCode("Warning");
        // atmFinancialSparrowMessage.setErrorDescription("Invalid Currency Code");
        // }
        // else if
        // (atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
        // ||
        // atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_7)
        // ||
        // atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)
        // ||
        // atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_5)
        // ||
        // atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_8))
        // {
        // String errorMessge = "Invalid Currency Code. Force Post Not Posted";
        // atmFinancialSparrowMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
        // atmFinancialSparrowMessage.setErrorCode(ATMConstants.CRITICAL);
        // atmFinancialSparrowMessage.setErrorDescription(errorMessge);
        //
        // }
        // return;
        // }
    }

    /**
     * This method validates the Source Account in the ATM Sparrow message.
     */

    public void validateSourceAccount(ATMSparrowFinancialMessage atmFinancialSparrowMessage, BankFusionEnvironment env) {
        boolean isAccountValid = false;
        isAccountValid = isAccountValid(atmFinancialSparrowMessage.getAccount(), env);
        if (isAccountValid) {
            return;
        }
        else if (atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
            Object[] field = new Object[] { atmFinancialSparrowMessage.getAccount() };
            // populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
            // ATMConstants.WARNING, 7516, BankFusionMessages.ERROR_LEVEL,
            populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                    ChannelsEventCodes.E_INVALID_ACCOUNT, BankFusionMessages.ERROR_LEVEL, atmFinancialSparrowMessage, field, env);
        }
    }

    /**
     * This method retrieves the configuration details required for validation.
     */
    private void getConfigurationInfo(BankFusionEnvironment env) {
        controlDetails = ATMConfigCache.getInstance().getInformation(env);
        if (controlDetails != null) {
            sharedSwitch = controlDetails.isSharedSwitch();
        }
    }

    /**
     * This method validates the Force Post in ATM Sparrow message. returns true if it is valid for
     * local messages.
     */
    private boolean checkForValidForcePost(ATMSparrowMessage atmSparrowMessage, BankFusionEnvironment env) {
        boolean isValidForcePost = true;
        String transctionType = atmSparrowMessage.getMessageType() + atmSparrowMessage.getTransactionType();
        if (Integer.parseInt(atmSparrowMessage.getForcePost()) > 3 && !transctionType.equals("623")) {
            isValidForcePost = false;
            Object[] field = new Object[] { atmSparrowMessage.getForcePost() };
            // populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
            // ATMConstants.CRITICAL, 7535, BankFusionMessages.ERROR_LEVEL,
            populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                    ChannelsEventCodes.E_INVALID_FORCE_POST_VALUE_FOR_LOCAL_FIN_MESG, BankFusionMessages.ERROR_LEVEL,
                    atmSparrowMessage, field, env);
        }
        return isValidForcePost;
    }

    /**
     * This method validates the whether the account in the message is mapped to the card in the
     * message. returns true if it is mapped.
     */
    public boolean isAccountMappedToCard(String cardNo, String accountNo, BankFusionEnvironment env) {
        boolean isAccMapped = false;
        List cardAccMap = null;
        ArrayList params = new ArrayList();
        params.add(cardNo);
        params.add(accountNo);
        try {
            cardAccMap = env.getFactory().findByQuery(IBOATMCardAccountMap.BONAME, cardAccMapWhereClause, params, null);
        }
        catch (BankFusionException bfe) {
            isAccMapped = false;
        }
        if (cardAccMap != null && cardAccMap.size() > 0) {
            isAccMapped = true;
        }
        return isAccMapped;
    }

    /**
     * This method is to check if the card is Smart card or Debit Card.
     * 
     * @param cardNumber
     * @param env
     * @return boolean
     */
    public boolean isSmartCard(String cardNumber, BankFusionEnvironment env) {
        IBOATMCardDetails atmCardDetails = (IBOATMCardDetails) env.getFactory().findByPrimaryKey(IBOATMCardDetails.BONAME,
                cardNumber);
        String cardType = atmCardDetails.getF_UBCARDTYPE();
        if (cardType.equals("S")) {
            return true;
        }
        return false;
    }

    /**
     * This method populates the error details in the message
     * 
     * @returns String
     */
    private void populateErrorDetails(String authorisedFlag, String errorCode, int errorNo, String errorLevel,
            ATMSparrowMessage atmSparrowMessage, Object[] fields, BankFusionEnvironment env) {
        atmSparrowMessage.setAuthorisedFlag(authorisedFlag);
        atmSparrowMessage.setErrorCode(errorCode);
        // atmSparrowMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(errorLevel,
        // errorNo, env, fields));
        atmSparrowMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(errorNo, fields));
    }

    public boolean isCardIssuersIDValid() {
        return true;
    }

    public boolean isCardNumberValid(String cardNumber, BankFusionEnvironment env) {
        boolean isCardNumberValid = false;
        try {
            IBOATMCardDetails cardDetails = (IBOATMCardDetails) env.getFactory().findByPrimaryKey(IBOATMCardDetails.BONAME,
                    cardNumber);
            if (cardDetails.getBoID().equals(cardNumber)) {
                isCardNumberValid = true;
            }
        }
        catch (BankFusionException exception) {
            isCardNumberValid = false;
        }
        return isCardNumberValid;
    }

    /*
     * This Method Checks if the Given Account exists in the
     */
    public boolean isAccountValid(String accountNumber, BankFusionEnvironment env) {
        boolean isAccountValid = false;
        errorMessage = CommonConstants.EMPTY_STRING;
        try {
            IBOAttributeCollectionFeature accValues = (IBOAttributeCollectionFeature) env.getFactory().findByPrimaryKey(
                    IBOAttributeCollectionFeature.BONAME, accountNumber);
            isAccountValid = true;
            BusinessValidatorBean accountValidator = new BusinessValidatorBean();
            if (accountValidator.validateAccountClosed(accValues, env)) {
                isAccountValid = false;
                errorMessage = accountValidator.getErrorMessage().getLocalisedMessage();
            }
            else if (accountValidator.validateAccountStopped(accValues, env)) {
                isAccountValid = false;
                errorMessage = accountValidator.getErrorMessage().getLocalisedMessage();
            }

        }
        catch (FinderException exception) {
            errorMessage = "Invalid Main Account";
        }
        catch (BankFusionException exception) {
            errorMessage = "Invalid Main Account";
        }

        return isAccountValid;
    }

    public boolean doesCardExist(String cardNo, BankFusionEnvironment env) {
        boolean result = false;
        try {
            IBOATMCardDetails cardDetails = (IBOATMCardDetails) env.getFactory().findByPrimaryKey(IBOATMCardDetails.BONAME, cardNo);
            if (cardDetails == null)
                result = false;
            else result = true;
        }
        catch (BankFusionException exception) {
            result = true;
        }

        return result;
    }

    public boolean areCardandAccountMapped(String cardNo, String accountNumber, BankFusionEnvironment env) {

        boolean result = false;
        ArrayList paramList = new ArrayList();
        paramList.add(cardNo);
        paramList.add(accountNumber);
        try {
            List list = env.getFactory().findByQuery(IBOATMCardAccountMap.BONAME, cardAccMapWhereClause, paramList, null);
            if (list.size() > 0) {
                result = true;
            }
        }
        catch (BankFusionException exception) {

        }
        return result;
    }

    public boolean validateCIB(String branchCode, String countryCode, String IMDCode, BankFusionEnvironment env) {
        boolean result = true;

        ArrayList params = new ArrayList();
        params.add(branchCode);
        params.add(countryCode);
        params.add(IMDCode);
        List branchBO = null;
        try {
            branchBO = env.getFactory().findByQuery(IBOATMCIB.BONAME, atmSourceWhereClause, params, null);
        }
        catch (BankFusionException bfe) {
            logger.error("Invalid Details");
        }
        if (branchBO ==null || branchBO.isEmpty()) {
            result = false;
        }
        else {
            result = true;
        }
        return result;

    }

    public boolean isCurrencyValid(String accountNumber, String numericCurrencyCode, BankFusionEnvironment env) {
        boolean result = false;
        try {
            String currencyCode = atmHelper.getAlphaCurrencyCode(numericCurrencyCode, env);
            if ((currencyCode == null) || (CommonConstants.EMPTY_STRING.equals(currencyCode))) {
                result = false;
                return result;
            }
            IBOAttributeCollectionFeature accountValues = (IBOAttributeCollectionFeature) env.getFactory().findByPrimaryKey(
                    IBOAttributeCollectionFeature.BONAME, accountNumber);
            if (accountValues.getF_ISOCURRENCYCODE().equalsIgnoreCase(currencyCode)) {
                result = true;
            }
            else {
                result = false;
            }

        }
        catch (Exception exception) {
            result = false;
        }
        return result;

    }

    public boolean isTransactionSupported(String transCode, BankFusionEnvironment env) {
        boolean result = false;
        int supportLevel = atmHelper.getSupportLevel(transCode, env);
        if ((supportLevel == ATMConstants.SUPPORTED_TRANSACTION_FLAG)
                || (supportLevel == ATMConstants.NOTIFICATION_TRANSACTION_FLAG)) {
            result = true;
        }
        return result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isAccountPasswordProtected(String accountID, String messageType, BankFusionEnvironment env) {
        this.errorMessage = CommonConstants.EMPTY_STRING;
        boolean result = false;
        IBOAccount attributeCollectionFeature = null;
        try {
            attributeCollectionFeature = (IBOAccount) env.getFactory().findByPrimaryKey(IBOAccount.BONAME, accountID);
        }
        catch (BankFusionException exception) {
            errorMessage = exception.getLocalisedMessage();
            return true;
        }
        int passwordProtectionFlag = attributeCollectionFeature.getF_ACCRIGHTSINDICATOR();
        // Password Protection Flag 2
        if (passwordProtectionFlag == PasswordProtectedConstants.ACCOUNT_STOPPED_NO_POSTING_ENQUIRY) {
            this.errorMessage = "Account is Password Protected";
            result = true;
            return result;
        }
        // Password Protection Flag 3
        if (passwordProtectionFlag == PasswordProtectedConstants.ACCOUNT_STOPPED_PASSWD_REQ_FOR_POSTING_ENQUIRY) {
            this.errorMessage = "Account is Password Protected";
            result = true;
            return result;
        }
        // Password Protection Flag 8
        if (passwordProtectionFlag == PasswordProtectedConstants.PASSWD_REQ_FOR_ENQUIRY) {
            this.errorMessage = "Account is Password Protected";
            result = true;
            return result;
        }
        // Password Protection Flag -1
        if (passwordProtectionFlag == PasswordProtectedConstants.PASSWD_REQ_FOR_POSTING_ENQUIRY) {
            this.errorMessage = "Account is Password Protected";
            result = true;
            return result;
        }

        // T0DO Change This Once the Password Protection Constants gets Updates.
        if (passwordProtectionFlag == 9) {
            this.errorMessage = "Account is Password Protected";
            result = true;
            return result;
        }
        return result;
    }

    /**
     * This method validates the ATM dispensed/destination currency in the ATM sparrow message.
     */
    public void validateDispensedCurrencyForLoro(ATMSparrowFinancialMessage atmFinancialSparrowMessage, BankFusionEnvironment env) {

        ArrayList params = new ArrayList();
        params.add(atmFinancialSparrowMessage.getCurrencyDestDispensed());
        List currencyBO = null;
        try {
            currencyBO = env.getFactory().findByQuery(IBOCurrency.BONAME, currencyWhereClause, params, null);
        }
        catch (BankFusionException bfe) {
            logger.error("Error while find by query for currency: " + atmFinancialSparrowMessage.getCurrencyDestDispensed(), bfe);
        }
        if (currencyBO ==null || currencyBO.isEmpty()) {
            atmFinancialSparrowMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
            Object[] field = new Object[] { atmFinancialSparrowMessage.getCurrencyDestDispensed() };
            if (atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)
                    || atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
                // populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                // ATMConstants.WARNING, 7514, BankFusionMessages.ERROR_LEVEL,
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.E_INVALID_DISPENSED_CURRENCY, BankFusionMessages.ERROR_LEVEL,
                        atmFinancialSparrowMessage, field, env);
            }
            else if (atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)
                    || atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_2)
                    || atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_3)
                    || atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_7)
                    || atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_5)
                    || atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_8)) {
                // populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                // ATMConstants.CRITICAL, 7515, BankFusionMessages.ERROR_LEVEL,
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                        ChannelsEventCodes.E_INVALID_DISPENSED_CURRENCY_FORCE_POST_NOT_POSTED, BankFusionMessages.ERROR_LEVEL,
                        atmFinancialSparrowMessage, field, env);
            }
        }
    }

    public void validateDestCurrency(ATMSparrowFinancialMessage atmFinancialSparrowMessage, BankFusionEnvironment env) {

        ArrayList params = new ArrayList();
        params.add(atmFinancialSparrowMessage.getCurrencyDestDispensed());
        List currencyBO = null;
        try {
            currencyBO = env.getFactory().findByQuery(IBOCurrency.BONAME, currencyWhereClause, params, null);
        }
        catch (BankFusionException bfe) {
            logger.error("Error while find by query for currency: " + atmFinancialSparrowMessage.getCurrencyDestDispensed(), bfe);
        }
        if (currencyBO ==null || currencyBO.isEmpty()) {
            atmFinancialSparrowMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
            Object[] field = new Object[] { atmFinancialSparrowMessage.getCurrencyDestDispensed() };
            if (atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_0)) {
                // populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                // ATMConstants.WARNING, 7544, BankFusionMessages.ERROR_LEVEL,
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.WARNING,
                        ChannelsEventCodes.E_INVALID_DESTINATION_CURRENCY, BankFusionMessages.ERROR_LEVEL,
                        atmFinancialSparrowMessage, field, env);
            }
            else if (atmFinancialSparrowMessage.getForcePost().equals(ATMConstants.FORCEPOST_1)) {
                // populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG,
                // ATMConstants.CRITICAL, 7545, BankFusionMessages.ERROR_LEVEL,
                populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                        ChannelsEventCodes.W_INVALID_DEST_CURR_FORCE_POST_NOT_POSTED, BankFusionMessages.ERROR_LEVEL,
                        atmFinancialSparrowMessage, field, env);
            }
        }
    }
}
