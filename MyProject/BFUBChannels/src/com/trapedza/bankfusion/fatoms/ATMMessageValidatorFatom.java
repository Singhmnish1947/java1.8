/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 * $Log: ATMMessageValidatorFatom.java,v $
 * Revision 1.8  2008/08/12 20:13:44  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.6.4.1  2008/07/03 17:55:35  vivekr
 * Moved from Dublin CVS Head (For BF1.01 package restructuring)
 *
 * Revision 1.5  2008/06/16 15:24:07  arun
 * UB Refactoring -
 * 1. Formatted/Organized imports
 * 2. BODefinitionException, InvalidExtensionPointException and references removed
 * 3. Removed ServerManager deprecated methods/variables
 * 4. BankfusionPropertyAccess removed - changed to BankfusionPropertySupport
 * 5. Exception Handling refactoring
 * 6. General Refactoring
 *
 * Revision 1.4  2008/06/12 10:51:54  arun
 *  RIO on Head
 *
 * Revision 1.6  2008/02/08 15:22:03  sushmax
 * Checkins after Sprint Cycle 5 & 6.
 *
 * Revision 1.3  2008/02/04 12:45:31  prashantk
 * Bug Fixes
 *
 * Revision 1.5  2008/01/17 13:20:37  sushmax
 * Updated files
 *
 * Revision 1.2  2008/01/11 08:15:53  prashantk
 * Fuxed for Bug #6509.
 *
 * Revision 1.1  2008/01/10 14:53:07  sushmax
 * Included accountcurrency and transactionreference to be included in the activity table.
 *
 * Revision 1.4  2007/11/19 10:14:12  sushmax
 * ATM Balance Inquiry
 *
 * Revision 1.3  2007/11/19 08:41:08  prashantk
 * Updated as the ATM Account Does not get Updated for Balances on sending Balance Inquiry and Notifications
 *
 * Revision 1.2  2007/11/14 11:07:43  prashantk
 * ATM Related Activity Steps
 *
 *
 */
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMConstants;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMHelper;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMMessageValidator;
import com.trapedza.bankfusion.bo.refimpl.IBOATMAccountDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractATMMessageValidatorFatom;
import com.trapedza.bankfusion.utils.BankFusionMessages;

/**
 * The ATMConfigDetails returns through the output tags the ATM Configuration Details from
 * ATMConfig.xml
 */
public class ATMMessageValidatorFatom extends AbstractATMMessageValidatorFatom {

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
     * Holds the reference for logger object
     */
    private transient final static Log logger = LogFactory.getLog(ATMMessageValidatorFatom.class.getName());

    private static final int SIZE_OF_AMOUNT = 15;
    private String authorizedFlag = CommonConstants.EMPTY_STRING;
    private String errorMessage = CommonConstants.EMPTY_STRING;
    private String errorStatus = CommonConstants.EMPTY_STRING;
    private String transactionReference = CommonConstants.EMPTY_STRING;
    private String accountCurrency = CommonConstants.EMPTY_STRING;
    private static final String EXTERNAL_MESSAGE = "6";

    /**
     * The constructor that indicates we're in a runtime environment and we should initialise the
     * Fatom with only those attributes necessary.
     * 
     * @param env
     *            The BankFusion Environment
     */
    public ATMMessageValidatorFatom(BankFusionEnvironment env) {
        super(env);
    }

    /**
     * @see com.trapedza.bankfusion.core.ExtensionPoint#process(BankFusionEnvironment)
     * @param env
     *            The BankFusion Environment @
     */
    public void process(BankFusionEnvironment env) {
        validateMessage(env);
        try {
            if (ATMConstants.AUTHORIZED_MESSAGE_FLAG.equals(authorizedFlag)) {
                ATMHelper atmHelper = new ATMHelper();
                BigDecimal availableBalance = atmHelper.getAvailableBalance(getF_IN_ACCOUNT(), env);
                IBOAttributeCollectionFeature accountItems = (IBOAttributeCollectionFeature) env.getFactory().findByPrimaryKey(
                        IBOAttributeCollectionFeature.BONAME, getF_IN_ACCOUNT());
                BigDecimal bookBalance = accountItems.getF_BOOKEDBALANCE();
                String currencyCode = accountItems.getF_ISOCURRENCYCODE();
                String availBalanceString = atmHelper.getSign(availableBalance)
                        + atmHelper.getScaledAmount(availableBalance, currencyCode, SIZE_OF_AMOUNT - 1);
                String bookBalanceString = atmHelper.getSign(bookBalance)
                        + atmHelper.getScaledAmount(bookBalance, currencyCode, SIZE_OF_AMOUNT - 1);
                setF_OUT_AVAILABLEBALANCE(availBalanceString);
                setF_OUT_BOOKBALANCE(bookBalanceString);

                // Update ATM Accounts Table.
                IBOATMAccountDetails atmAccountDetails = (IBOATMAccountDetails) env.getFactory().findByPrimaryKey(
                        IBOATMAccountDetails.BONAME, getF_IN_ACCOUNT());
                atmAccountDetails.setF_ATMBOOKBALANCE(bookBalance);
                atmAccountDetails.setF_ATMCLEAREDBALANCE(availableBalance);
            }
        }
        catch (BankFusionException exception) {
            logger.error(exception);
        }

        setF_OUT_AUTHORISEDFLAG(authorizedFlag);
        setF_OUT_ERRORDESCRIPTION(errorMessage);
        setF_OUT_ERRORSTATUS(errorStatus);
        setF_OUT_TRANSACTIONREFERENCE(transactionReference);
        setF_OUT_ACCOUNTCURRENCY(accountCurrency);

    }

    private void validateMessage(BankFusionEnvironment env) {

        String ATMTransactionCode = getF_IN_MESSAGETYPE() + getF_IN_TRANSACTIONTYPE();
        String variableDateType = getF_IN_VARIABLEDATATYPE();
        String sourceBranchCode = getF_IN_SOURCEBRANCHCODE();
        String sourceIMDCode = getF_IN_SOURCEIMD();
        String sourceCountryCode = getF_IN_SOURCECOUNTRYCODE();
        String destBranchCode = getF_IN_CARDDESTBRANCHCODE();
        String destIMDCode = getF_IN_CARDDESTINATIONIMD();
        String destCountryCode = getF_IN_CARDDESTCOUNTRYCODE();
        String cardNumber = getF_IN_CARDNUMBER();
        String accountID = getF_IN_ACCOUNT();

        ATMHelper atmHelper = new ATMHelper();

        transactionReference = sourceCountryCode + sourceIMDCode + sourceBranchCode + getF_IN_DEVICEID()
                + getF_IN_TXNSEQUENCENUMBER() + getDateinStringFormat(getF_IN_POSTDATETIME());

        accountCurrency = atmHelper.getAccountCurrency(getF_IN_ACCOUNT(), env);

        boolean isLocalTransaction = isLocalMessage(variableDateType);
        boolean proceed = false;

        authorizedFlag = null;
        errorMessage = null;
        errorStatus = null;
        ATMMessageValidator messageValidator = new ATMMessageValidator();
        // Validate Country IMD and Branch Code.
        if (isLocalTransaction) {
            proceed = messageValidator.validateCIB(sourceBranchCode, sourceCountryCode, sourceIMDCode, env);
            if (!proceed) {
                authorizedFlag = ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG;
                /*
                 * errorMessage =
                 * BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 7500, env,
                 * new Object[] { sourceCountryCode, sourceIMDCode, sourceBranchCode });
                 */
                errorMessage = BankFusionMessages.getInstance().getFormattedEventMessage(40407500,
                        new Object[] { sourceCountryCode, sourceIMDCode, sourceBranchCode },
                        BankFusionThreadLocal.getUserSession().getUserLocale());

                errorStatus = ATMConstants.WARNING;
                logger.error(errorStatus + ": " + errorMessage);
                return;
            }
        }
        else {
            proceed = messageValidator.validateCIB(destBranchCode, destCountryCode, destIMDCode, env);
            if (!proceed) {
                authorizedFlag = ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG;
                errorMessage = BankFusionMessages.getFormattedMessage(ChannelsEventCodes.E_DEST_COUNTRY_IMD_BRANCH_NOT_MAPPED,
                        new Object[] { destCountryCode, destIMDCode, destBranchCode });
                errorStatus = ATMConstants.WARNING;
                logger.error(errorStatus + ": " + errorMessage);
                return;
            }
        }
        if (!messageValidator.isTransactionSupported(ATMTransactionCode, env)) {
            authorizedFlag = ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG;
            errorMessage = BankFusionMessages.getFormattedMessage(ChannelsEventCodes.E_ATM_TRANSACTION_NOT_SUPPORTED,
                    new Object[] { ATMTransactionCode });
            errorStatus = ATMConstants.WARNING;
            logger.error(errorStatus + ": " + errorMessage);
            return;
        }
        if (!messageValidator.isCardNumberValid(cardNumber, env)) {
            authorizedFlag = ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG;
            errorMessage = BankFusionMessages.getFormattedMessage(ChannelsEventCodes.E_INVALID_CARD, new Object[] { cardNumber });
            errorStatus = ATMConstants.WARNING;
            logger.error(errorStatus + ": " + errorMessage);
            return;
        }
        if (!messageValidator.isAccountValid(accountID, env)) {
            authorizedFlag = ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG;
            errorMessage = messageValidator.getErrorMessage();
            errorStatus = ATMConstants.WARNING;
            logger.error(errorStatus + ": " + errorMessage);
            return;
        }
        if (!messageValidator.areCardandAccountMapped(cardNumber, accountID, env)) {
            authorizedFlag = ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG;
            errorMessage = BankFusionMessages.getFormattedMessage(ChannelsEventCodes.E_CARD_NUMBER_AND_ACCOUNT_NUMBER_NOT_MAPPED,
                    new Object[] { cardNumber, accountID, });
            errorStatus = ATMConstants.WARNING;
            logger.error(errorStatus + ": " + errorMessage);
            return;
        }
        if (EXTERNAL_MESSAGE.equals(getF_IN_MESSAGETYPE())) {
            if (messageValidator.isAccountPasswordProtected(accountID, ATMConstants.EXTERNAL_MESSAGE_TYPE, env)) {
                authorizedFlag = ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG;
                errorMessage = messageValidator.getErrorMessage();
                errorStatus = ATMConstants.WARNING;
                logger.error(errorStatus + ": " + errorMessage);
                return;
            }
        }
        authorizedFlag = ATMConstants.AUTHORIZED_MESSAGE_FLAG;
        errorMessage = "Transaction Validated";
        errorStatus = ATMConstants.INFORMATION;
        logger.info(errorStatus + ": " + errorMessage);
    }

    private boolean isLocalMessage(String variableDataType) {
        boolean result = false;
        if (("A").equalsIgnoreCase(variableDataType)) {
            result = true;
        }
        return result;
    }

    public String getDateinStringFormat(Timestamp timeStamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        simpleDateFormat.applyPattern(ATMConstants.ATM_TIMESTAMP_PATTERN);
        String formattedDate = simpleDateFormat.format(timeStamp);
        return formattedDate;

    }
}
