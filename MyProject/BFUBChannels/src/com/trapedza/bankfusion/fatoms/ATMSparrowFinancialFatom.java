/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 * $Log: ATMSparrowFinancialFatom.java,v $
 * Revision 1.11  2008/08/12 20:14:20  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.9.4.2  2008/08/01 04:07:35  prashantk
 * For Bug#11563. Changes made to the return of Authorized Flad in case of force post Messages
 *
 * Revision 1.9.4.1  2008/07/03 17:55:36  vivekr
 * Moved from Dublin CVS Head (For BF1.01 package restructuring)
 *
 * Revision 1.5  2008/06/16 15:21:34  arun
 * UB Refactoring -
 * 1. Formatted/Organized imports
 * 2. BODefinitionException, InvalidExtensionPointException and references removed
 * 3. Removed ServerManager deprecated methods/variables
 * 4. BankfusionPropertyAccess removed - changed to BankfusionPropertySupport
 * 5. Exception Handling refactoring
 * 6. General Refactoring
 *
 * Revision 1.4  2008/06/12 10:51:53  arun
 *  RIO on Head
 *
 * Revision 1.9  2008/02/22 07:19:32  sushmax
 * Added sign, default values for amounts in the response message.
 *
 * Revision 1.11  2008/02/19 12:46:53  sushmax
 * *** empty log message ***
 *
 * Revision 1.10  2008/02/14 06:22:29  varap
 * added sign to bookbalance
 *
 * Revision 1.9  2008/02/06 12:04:49  varap
 * *** empty log message ***
 *
 * Revision 1.8  2008/02/06 12:02:03  varap
 * *** empty log message ***
 *
 * Revision 1.7  2008/02/04 12:45:00  prashantk
 * Bug Fixes
 *
 * Revision 1.7  2008/01/28 07:38:18  sushmax
 * ATM Financial Fatom.
 *
 * Revision 1.6  2008/01/22 10:52:21  varap
 * *** empty log message ***
 *
 * Revision 1.4  2008/01/16 14:43:22  sushmax
 * *** empty log message ***
 *
 * Revision 1.3  2008/01/16 13:23:04  sushmax
 * Bug fix for 6685
 *
 * Revision 1.2  2008/01/16 07:26:19  sushmax
 * *** empty log message ***
 *
 * Revision 1.1  2008/01/14 08:40:44  sushmax
 * *** empty log message ***
 *
 * Revision 1.6  2007/12/07 11:28:03  sushmax
 * SystemInformationManager called to load the currencies
 *
 * Revision 1.5  2007/11/30 09:53:12  sushmax
 * calls to ATMCache methods changed to call ATMHelper methods
 *
 * Revision 1.4  2007/11/30 06:26:24  sushmax
 * ATM Error code added 7543
 *
 * Revision 1.3  2007/11/21 11:06:33  sushmax
 * Added transaction reference entry to ATM Activity log
 *
 * Revision 1.2  2007/11/14 11:07:42  prashantk
 * ATM Related Activity Steps
 *
 * Revision 1.7  2007/10/29 06:54:12  prashantk
 * Updated
 *
 * Revision 1.1.2.1  2007/08/08 18:43:38  prashantk
 * Fatom for all Financial Messages
 *
 * Revision 1.5  2007/06/22 10:26:00  sushmax
 * *** empty log message ***
 *
 *
 */
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMConstants;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMHelper;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMMessageFactory;
import com.trapedza.bankfusion.atm.sparrow.message.ATMExNwMessage;
import com.trapedza.bankfusion.atm.sparrow.message.ATMLocalMessage;
import com.trapedza.bankfusion.atm.sparrow.message.ATMPOSMessage;
import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowFinancialMessage;
import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowMessage;
import com.trapedza.bankfusion.atm.sparrow.message.processor.IATMMessageProcessor;
import com.trapedza.bankfusion.bo.refimpl.IBOATMAccountDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractATMSparrowFinancialFatom;
import com.trapedza.bankfusion.utils.BankFusionMessages;

/**
 * The ATMSparrowFinancialFatom calls the factory to process the messages.
 */
public class ATMSparrowFinancialFatom extends AbstractATMSparrowFinancialFatom {

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
    private transient final static Log logger = LogFactory.getLog(ATMSparrowFinancialFatom.class.getName());

    /**
     * The constructor that indicates we're in a runtime environment and we should initialise the
     * Fatom with only those attributes necessary.
     * 
     * @param env
     *            The BankFusion Environment
     */
    public ATMSparrowFinancialFatom(BankFusionEnvironment env) {
        super(env);
    }

    private static int LENGTH_OF_AMOUNT = 14;
    private ATMHelper atmHelper = new ATMHelper();;

    /**
     * This method invokes the message processors, updates the ATM activity log table and sends back
     * the response message.
     * 
     * @param env
     *            The BankFusion Environment
     */
    String accountNumber = null;
    String atmMessageType = CommonConstants.EMPTY_STRING;

    public void process(BankFusionEnvironment env) {

        // SystemInformationManager.getInstance().transformCurrencyCode(getF_IN_CURRENCYDESTDISPENSED(),
        // true);
        // env.getFactory().startPrivateSession(true);
        env.getFactory().beginTransaction();
        String variableDataType = getF_IN_VARIABLEDATATYPE();
        ATMSparrowFinancialMessage message = null;

        message = createMessage(variableDataType, env);
        atmMessageType = message.getMessageType() + message.getTransactionType();
        accountNumber = message.getAccount();
        if (message.getAuthorisedFlag().equals(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG)) {
            updateActivityLog(env, message);
            setReturnValues(getF_IN_VARIABLEDATATYPE(), message, env);
            return;
        }
        if (message != null) {
            invokeMessageProcessor(env, message);
            updateActivityLog(env, message);
            setReturnValues(getF_IN_VARIABLEDATATYPE(), message, env);
        }
    }

    /**
     * This method creates the message based on the variableDataType in the message.
     * 
     * @param env
     *            The BankFusion Environment
     */
    public ATMSparrowFinancialMessage createMessage(String variableDataType, BankFusionEnvironment env) {
        ATMSparrowFinancialMessage message = null;
        try {
            if ("A".equals(variableDataType)) {
                message = new ATMLocalMessage();
                createFinancialMessage(message, env);
                createLocalMessage((ATMLocalMessage) message);
            }
            else if ("P".equals(variableDataType)) {
                message = new ATMPOSMessage();
                createFinancialMessage(message, env);
                createPOSMessage((ATMPOSMessage) message);
            }
            else if ("E".equals(variableDataType)) {
                message = new ATMExNwMessage();
                createFinancialMessage(message, env);
                createExNwMessage((ATMExNwMessage) message);
            }
        }
        catch (BankFusionException exception) {
        	logger.error(exception);

        }
        return message;
    }

    /**
     * Creates a ATM Sparrow Financial -Header Message object.
     * 
     */
    private void createFinancialMessage(ATMSparrowFinancialMessage atmSparrowMessage, BankFusionEnvironment env) {
        atmSparrowMessage.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
        atmSparrowMessage.setCardDestBranchCode(getF_IN_CARDDESTBRANCHCODE());
        atmSparrowMessage.setCardDestCountryCode(getF_IN_CARDDESTCOUNTRYCODE());
        atmSparrowMessage.setCardDestinationIMD(getF_IN_CARDDESTINATIONIMD());
        atmSparrowMessage.setCardNumber(getF_IN_CARDNUMBER());
        atmSparrowMessage.setCardSequenceNo(getF_IN_CARDSEQUENCENO());
        atmSparrowMessage.setDestinationMailBox(getF_IN_DESTINATIONMAILBOX());
        atmSparrowMessage.setDeviceId(getF_IN_DEVICEID());
        atmSparrowMessage.setErrorCode(getF_IN_ERRORCODE());
        atmSparrowMessage.setErrorDescription(getF_IN_ERRORDESCRIPTION());
        atmSparrowMessage.setForcePost(getF_IN_FORCEPOST());
        atmSparrowMessage.setMessageType(getF_IN_MESSAGETYPE());
        atmSparrowMessage.setSourceBranchCode(getF_IN_SOURCEBRANCHCODE());
        atmSparrowMessage.setSourceCountryCode(getF_IN_SOURCECOUNTRYCODE());
        atmSparrowMessage.setSourceIMD(getF_IN_SOURCEIMD());
        atmSparrowMessage.setSourceMailBox(getF_IN_SOURCEMAILBOX());
        atmSparrowMessage.setTransactionType(getF_IN_TRANSACTIONTYPE());
        atmSparrowMessage.setTxnSequenceNo(getF_IN_TXNSEQUENCENUMBER());
        atmSparrowMessage.setAccount(getF_IN_ACCOUNT());
        atmSparrowMessage.setActionCode(getF_IN_ACTIONCODE());
        atmSparrowMessage.setCurrencyDestDispensed(getF_IN_CURRENCYDESTDISPENSED());
        atmSparrowMessage.setCurrencySourceAccount(getF_IN_CURRENCYSOURCEACCOUNT());
        atmSparrowMessage.setDescSourceAcc(getF_IN_DESCSOURCEACC());
        atmSparrowMessage.setDestAccountNumber(getF_IN_DESTACCOUNTNUMBER());
        atmSparrowMessage.setLoroMailbox(getF_IN_DESTACCOUNTNUMBER().substring(16, 17)); // TODO
                                                                                         // change
                                                                                         // after
                                                                                         // testing
                                                                                         // -
                                                                                         // include
                                                                                         // in
                                                                                         // template
        atmSparrowMessage.setLocalCurrencyCode(getF_IN_LOCALCURRENCYCODE());
        atmSparrowMessage.setSubIndex(getF_IN_SUBINDEX());
        atmSparrowMessage.setVariableDataType(getF_IN_VARIABLEDATATYPE());
        atmSparrowMessage.setDescDestAcc(getF_IN_DESCDESTACC());
        atmSparrowMessage.setDateTimeofTxn(getF_IN_POSTDATETIME());
        atmSparrowMessage.setExtVersion(getF_IN_EXTENTIONVERSION());
        BigDecimal amount1 = CommonConstants.BIGDECIMAL_ZERO;
        BigDecimal amount2 = CommonConstants.BIGDECIMAL_ZERO;
        BigDecimal amount3 = CommonConstants.BIGDECIMAL_ZERO;
        BigDecimal amount4 = CommonConstants.BIGDECIMAL_ZERO;
        String dispensedCurrencyCode = CommonConstants.EMPTY_STRING;
        try {
            String accountCurrencyCode = SystemInformationManager.getInstance().transformCurrencyCode(
                    getF_IN_CURRENCYSOURCEACCOUNT(), true);
            /*
             * If the transaction is external network , the dispensed currency may not be existing
             * in the system. Hence the accountCurrencyCode is taken for the scaling.
             */
            if ("E".equals(getF_IN_VARIABLEDATATYPE())) {

                dispensedCurrencyCode = accountCurrencyCode;
            }
            else {
                dispensedCurrencyCode = SystemInformationManager.getInstance().transformCurrencyCode(
                        getF_IN_CURRENCYDESTDISPENSED(), true);
            }
            String baseEquivalentCurrency = SystemInformationManager.getInstance().getBaseCurrencyCode();

            int scale1 = SystemInformationManager.getInstance().getCurrencyScale(accountCurrencyCode);
            int scale2 = SystemInformationManager.getInstance().getCurrencyScale(dispensedCurrencyCode);
            int scale3 = SystemInformationManager.getInstance().getCurrencyScale(baseEquivalentCurrency);

            amount1 = (new BigDecimal(getF_IN_AMOUNT1())).movePointLeft(scale1);
            amount2 = (new BigDecimal(getF_IN_AMOUNT2())).movePointLeft(scale2);
            amount3 = (new BigDecimal(getF_IN_AMOUNT3())).movePointLeft(scale3);
            amount4 = (new BigDecimal(getF_IN_AMOUNT4())).movePointLeft(scale3);
            atmSparrowMessage.setAmount1(amount1);
            atmSparrowMessage.setAmount2(amount2);
            atmSparrowMessage.setAmount3(amount3);
            atmSparrowMessage.setAmount4(amount4);
        }
        catch (BankFusionException exception) {
            atmSparrowMessage.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
            atmSparrowMessage.setErrorCode(ATMConstants.CRITICAL);
            atmSparrowMessage.setErrorDescription("The Currency Code provided is invalid");
            atmSparrowMessage.setAmount1(amount1);
            atmSparrowMessage.setAmount2(amount2);
            atmSparrowMessage.setAmount3(amount3);
            atmSparrowMessage.setAmount4(amount4);
            // throw new BankFusionException(1000, "The Currency Code provided is invalid");
            EventsHelper.handleEvent(ChannelsEventCodes.E_ERROR_CONVERTING_AMOUNT, new Object[] {}, new HashMap(), env);
        logger.error(exception);
        }

    }

    /**
     * Creates a ATM Sparrow Financial Local -Message object.
     * 
     */
    private void createLocalMessage(ATMLocalMessage atmLocalMessage) {
        atmLocalMessage.setActualBalance(getF_IN_ACTUALBALANCE());
        atmLocalMessage.setAvailableBalance(getF_IN_AVAILABLEBALANCE());
        atmLocalMessage.setBranchName(getF_IN_BRANCHNAME());
        atmLocalMessage.setExtensionVersion(getF_IN_EXTENTIONVERSION());
    }

    /**
     * Creates a ATM Sparrow Financial POS-Message object.
     * 
     */
    private void createPOSMessage(ATMPOSMessage atmPOSMessage) {
        atmPOSMessage.setAuthorisationCode(getF_IN_EXTENTIONVERSION());
        atmPOSMessage.setCashBackAmount(getF_IN_CASHBACKAMOUNT());
        atmPOSMessage.setExtensionVersion(getF_IN_EXTENTIONVERSION());
        atmPOSMessage.setExternalTerminalID(getF_IN_EXTERNALTERMINALID());
        atmPOSMessage.setMerchantCategoryCode(getF_IN_MERCHANTCATEGORYCODE());
        atmPOSMessage.setMerchantID(getF_IN_MERCHANTID());
        atmPOSMessage.setMerchantLocation(getF_IN_MERCHANTLOCATION());
        atmPOSMessage.setMerchantName(getF_IN_MERCHANTNAME());
        atmPOSMessage.setSettlementIdentifier(getF_IN_SETTLEMENTIDENTIFIER());
    }

    /**
     * Creates a ATM Sparrow Financial External Network -Message object.
     * 
     */
    private void createExNwMessage(ATMExNwMessage atmExNwMessage) {
        atmExNwMessage.setAcquiringInstitutionID(getF_IN_ACQUIRINGINSTITUTIONID());
        atmExNwMessage.setActualBalance(getF_IN_ACTUALBALANCE());
        atmExNwMessage.setAuthorisationCode(getF_IN_AUTHORISATIONCODE());
        atmExNwMessage.setAvailableBalance(getF_IN_AVAILABLEBALANCE());
        atmExNwMessage.setCardAcceptorID(getF_IN_CARDACCEPTORID());
        atmExNwMessage.setCardAcceptorName(getF_IN_CARDACCEPTORNAME());
        atmExNwMessage.setCardAcceptorTerminalID(getF_IN_CARDACCEPTORTERMINALID());
        atmExNwMessage.setCashBackAccount(getF_IN_CASHBACKACCOUNT());
        atmExNwMessage.setCashBackAmount(getF_IN_CASHBACKAMOUNT());
        atmExNwMessage.setCashBackDevice(getF_IN_CASHBACKDEVICE());
        atmExNwMessage.setConversionRate(getF_IN_CONVERSIONRATE());
        atmExNwMessage.setCurrencyCode(getF_IN_CURRENCYCODE());
        atmExNwMessage.setExtensionNumber(getF_IN_EXTENTIONVERSION());
        atmExNwMessage.setExternalNetworkID(getF_IN_EXTERNALNETWORKID());
        atmExNwMessage.setForwardingInstitutionID(getF_IN_FORWARDINGINSTITUTIONID());
        atmExNwMessage.setMerchantCategoryCode(getF_IN_MERCHANTCATEGORYCODE());
        atmExNwMessage.setSettlementAmount(getF_IN_SETTLEMENTAMOUNT());
        atmExNwMessage.setSettlementConvRate(getF_IN_SETTLEMENTCONVRATE());
        atmExNwMessage.setSettlementCurrency(getF_IN_SETTLEMENTCURRENCY());
        atmExNwMessage.setTransactionAmount(getF_IN_TRANSACTIONAMOUNT());
    }

    /**
     * This method invokes the message processor for each type of message.
     * 
     * @param env
     *            The BankFusion Environment, ATMLocalMessage, ATMLocalMessage.
     */
    private void invokeMessageProcessor(BankFusionEnvironment env, ATMSparrowFinancialMessage atmSparrowMessage) {
        ATMMessageFactory factory = new ATMMessageFactory();
        try {
            IATMMessageProcessor messageProcessor = factory.getMessageProcessor(atmSparrowMessage, env);
            messageProcessor.execute(atmSparrowMessage, env);
        }
        catch (BankFusionException bfe) {
            Object[] field = new Object[] { getF_IN_MESSAGETYPE() + getF_IN_TRANSACTIONTYPE() };
            /*
             * populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
             * 7540, BankFusionMessages.ERROR_LEVEL, atmSparrowMessage, field, env);
             */
            populateErrorDetails(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG, ATMConstants.CRITICAL,
                    ChannelsEventCodes.E_ERROR_IN_PROCESSING_ATM_MESSAGE, BankFusionMessages.ERROR_LEVEL, atmSparrowMessage, field,
                    env);
        logger.error(bfe);
        }
    }

    /**
     * Update the Activity log table with details from the message.
     * 
     * @param env
     *            The BankFusion Environment, ATMLocalMessage, ATMLocalMessage.
     */

    private void updateActivityLog(BankFusionEnvironment env, ATMSparrowFinancialMessage atmMessage) {
        HashMap params = new HashMap();
        Date TRANSACTIONDTTM = SystemInformationManager.getInstance().getBFBusinessDateTime();
        if (atmMessageType.equals("599") || atmMessageType.equals("899") || atmMessageType.equals("099")) {
            String currCode = SystemInformationManager.getInstance().transformCurrencyCode(getF_IN_CURRENCYSOURCEACCOUNT(), true);
            params.put("ACCOUNTCURRENCY", currCode);
        }
        else {
            params.put("ACCOUNTCURRENCY", atmHelper.getAccountCurrency(atmMessage.getAccount(), env));
        }
        if (atmMessage.getAccount() != null && atmMessage.getAccount().trim().length() > 0) {
            // changes for artf52970 start
            params.put("ACCOUNTID", accountNumber);
            // changes for artf52970 end
        }
        else {
            params.put("ACCOUNTID", accountNumber);
        }
        params.put("AMOUNTDISPENSED", atmMessage.getAmount1());
        params.put("ATMCARDNUMBER", atmMessage.getCardNumber());
        params.put("ATMDEVICEID", atmMessage.getDeviceId());
        params.put("ATMTRANSACTIONCODE", atmMessage.getMessageType() + atmMessage.getTransactionType());
        params.put("ATMTRANDESC",
                atmHelper.getTransactionDescription(atmMessage.getMessageType() + atmMessage.getTransactionType(), env));
        params.put("BASEEQUIVALENT", atmMessage.getAmount3());
        params.put("CARDSEQUENCENUMBER", new Integer(atmMessage.getCardSequenceNo()));
        params.put("COMMAMOUNT", atmMessage.getAmount4());
        params.put("DESTACCOUNTID", atmMessage.getDestAccountNumber());
        // params.put("DESTCOUNTRY", atmMessage.getCardDestCountryCode());
        // params.put("DESTIMD", atmMessage.getCardDestinationIMD());
        // params.put("DESTBRANCH", atmMessage.getCardDestBranchCode());
        params.put("DESTCIB",
                atmMessage.getCardDestCountryCode() + atmMessage.getCardDestinationIMD() + atmMessage.getCardDestBranchCode());
        params.put("ERRORDESCRIPTION", atmMessage.getErrorDescription());
        params.put("ERRORSTATUS", atmMessage.getErrorCode());
        params.put("FORCEPOST", new Integer(atmMessage.getForcePost()));
        params.put("MISTRANSACTIONCODE",
                atmHelper.getBankTransactionCode((atmMessage.getMessageType() + atmMessage.getTransactionType()), env));
        params.put("POSTDATETIME", atmMessage.getDateTimeofTxn());
        // params.put("SOURCEBRANCH", atmMessage.getSourceBranchCode());
        // params.put("SOURCECOUNTRY", atmMessage.getSourceCountryCode());
        // params.put("SOURCEIMD", atmMessage.getSourceIMD());
        params.put("SOURCECIB", atmMessage.getSourceCountryCode() + atmMessage.getSourceIMD() + atmMessage.getSourceBranchCode());
        params.put("TRANSNARRATION", atmMessage.getTxnCustomerNarrative());
        params.put("TRANSSEQ", new Integer(atmMessage.getTxnSequenceNo()));
        params.put("AUTHORIZEDFLAG", new Integer(atmMessage.getAuthorisedFlag()));
        params.put("TRANSACTIONREFERENCE", atmHelper.getTransactionReference(atmMessage));
        params.put("TRANSACTIONDTTM", TRANSACTIONDTTM);
        params.put("TRANSACTIONAMOUNT", atmMessage.getAmount2());
        params.put("SUBINDEX", atmMessage.getSubIndex());
        params.put("EXT_VERSION", atmMessage.getExtVersion());
        try {
            MFExecuter.executeMF(ATMConstants.ACTIVITY_LOG_UPDATE_MICROFLOW_NAME, env, params);
        }
        catch (Exception exception) {
            /*
             * String localErrormessage =
             * BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 7538, env, new
             * Object[] {});
             */
            String localErrormessage = BankFusionMessages.getInstance().getFormattedEventMessage(40407538, new Object[] {},
                    BankFusionThreadLocal.getUserSession().getUserLocale());

            logger.error(localErrormessage);
            logger.error(exception);
        }
    }

    /**
     * This method sets return values for the response message. It retrieves the book balance and
     * available balance for the account in the message. The ATMAccountdetails table is also updated
     * with the account balances.
     * 
     * @param variableDataType
     * @param message
     */
    public void setReturnValues(String variableDataType, ATMSparrowFinancialMessage message, BankFusionEnvironment env) {

        BigDecimal availableBalance = CommonConstants.BIGDECIMAL_ZERO;
        BigDecimal bookBalance = CommonConstants.BIGDECIMAL_ZERO;
        String accountID = message.getAccount();
        String currCode = SystemInformationManager.getInstance().transformCurrencyCode(getF_IN_CURRENCYSOURCEACCOUNT(), true);
        String atmMessageType = message.getMessageType() + message.getTransactionType();
        /*
         * The 599 , 899 and 099 would not have account with us
         */
        if (atmMessageType.equals("599") || atmMessageType.equals("899") || atmMessageType.equals("099")) {
            availableBalance = CommonConstants.BIGDECIMAL_ZERO;
            bookBalance = CommonConstants.BIGDECIMAL_ZERO;
        }
        else {
            if ("A".equals(variableDataType) || "E".equals(variableDataType)) {
                if (ATMConstants.AUTHORIZED_MESSAGE_FLAG.equals(message.getAuthorisedFlag())) {
                    IBOAttributeCollectionFeature attributeCollectionFeature = null;
                    try {
                        availableBalance = atmHelper.getAvailableBalance(accountID, env);
                        attributeCollectionFeature = (IBOAttributeCollectionFeature) env.getFactory().findByPrimaryKey(
                                IBOAttributeCollectionFeature.BONAME, accountID);
                        bookBalance = attributeCollectionFeature.getF_BOOKEDBALANCE();
                        currCode = attributeCollectionFeature.getF_ISOCURRENCYCODE();
                    }
                    catch (BankFusionException exception) {
                        /*
                         * String localErrormessage =
                         * BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL,
                         * 7543, env, new Object[] { message.getAccount() });
                         */
                        String localErrormessage = BankFusionMessages.getFormattedMessage(
                                ChannelsEventCodes.E_RETRIEVAL_ERROR_IN_ACCOUNTDETAILS_FOR_ACCOUNTID,
                                new Object[] { message.getAccount() });
                        logger.error(localErrormessage);
                        logger.error(exception);
                    }
                }
            }
        }
        String availableBalanceSign = atmHelper.getSign(availableBalance);
        String tempAvailableBalance = atmHelper.getScaledAmount(availableBalance, currCode, LENGTH_OF_AMOUNT);
        String bookBalanceSign = atmHelper.getSign(bookBalance);
        String tempBookBalance = atmHelper.getScaledAmount(bookBalance, currCode, LENGTH_OF_AMOUNT);
        setF_OUT_BOOKBALANCE(bookBalanceSign + tempBookBalance);
        setF_OUT_AVAILABLEBALANCE(availableBalanceSign + tempAvailableBalance);

        if (message.getForcePost().equals(ATMConstants.FORCEPOST_0) || message.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
            setF_OUT_AUTHORISEDFLAG(message.getAuthorisedFlag());
        }
        else {
            setF_OUT_AUTHORISEDFLAG(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
        }

        if (message.getAuthorisedFlag().equals(ATMConstants.AUTHORIZED_MESSAGE_FLAG) && !atmMessageType.equals("599")
                && !atmMessageType.equals("899") && !atmMessageType.equals("099")) {
            IBOATMAccountDetails atmAccountDetails = null;
            try {
                atmAccountDetails = (IBOATMAccountDetails) env.getFactory().findByPrimaryKey(IBOATMAccountDetails.BONAME,
                        message.getAccount());
                if (atmAccountDetails != null) {
                    atmAccountDetails.setF_ATMBOOKBALANCE(bookBalance);
                    atmAccountDetails.setF_ATMCLEAREDBALANCE(availableBalance);
                }
            }
            catch (Exception bfe) {
                /*
                 * String localErrormessage =
                 * BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 7542, env,
                 * new Object[] { message.getAccount() });
                 */
                String localErrormessage = BankFusionMessages.getInstance().getFormattedEventMessage(40407542,
                        new Object[] { message.getAccount() }, BankFusionThreadLocal.getUserSession().getUserLocale());

                logger.error(localErrormessage);
                logger.error(bfe);
            }
        }
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

    // private String getMappedAccount (String accountID) {
    //
    // return accountID;
    // }

}
