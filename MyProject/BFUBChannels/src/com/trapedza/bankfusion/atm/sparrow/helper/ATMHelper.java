/*
 * Copyright (c) 2003 MISYS Financial Systems Limited. All Rights Reserved.
 *
 * This software is the proprietary information of MISYS Financial Systems Limited.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 *
 */
package com.trapedza.bankfusion.atm.sparrow.helper;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.CharacterIterator;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.persistence.SimplePersistentObject;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.CacheConstants;
import com.misys.ub.common.MISTransactionCodeDetails;
import com.misys.ub.common.events.CommonsEventCodes;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.atm.sparrow.configuration.ATMConfigCache;
import com.trapedza.bankfusion.atm.sparrow.configuration.ATMControlDetails;
import com.trapedza.bankfusion.atm.sparrow.message.ATMExNwMessage;
import com.trapedza.bankfusion.atm.sparrow.message.ATMLocalMessage;
import com.trapedza.bankfusion.atm.sparrow.message.ATMPOSMessage;
import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowFinancialMessage;
import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOATMAccountDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOATMTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOCurrency;
import com.trapedza.bankfusion.bo.refimpl.IBODormancyFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.BusinessValidatorBean;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.FinderMethods;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.fatoms.CB_CMN_FetchCurrencyDetails;
import bf.com.misys.cbs.types.CurrencyCodeDetails;
import bf.com.misys.cbs.types.CurrencyDetails;
import com.trapedza.bankfusion.features.extensionpoints.PasswordProtectedConstants;
import com.trapedza.bankfusion.features.extensionpoints.ValidateCustomer;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.ConvertToString;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.utils.BankFusionMessages;

public class ATMHelper {

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
     * Where clause for account record retrieval on PSEUDONYM.
     */
    private static final String pseudoNameWhereClause = "WHERE " + IBOAccount.PSEUDONAME + "=?";
    private static String findNumericCurrencyCode = "WHERE " + IBOCurrency.NumericISOCurrencyCode + "= ?";
    private static final String findBranchSortCode = "WHERE " + IBOBranch.BMBRANCH + "=?";
    private static final String FindBytransactionCode="WHERE " + IBOATMTransactionCodes.ATMTRANSACTIONCODE + "=?";

    /** The Constant findDormanyFeature. */
    private static final String findDormanyFeature = "WHERE " + IBODormancyFeature.ACCOUNTID + " = ?";

    private String UBTransCode = CommonConstants.EMPTY_STRING;
    private static final transient Log logger = LogFactory.getLog(ATMHelper.class.getName());

    public BigDecimal getAvailableBalance(String accountID, BankFusionEnvironment env) {
        BigDecimal availableBalance = BigDecimal.ZERO;
        try {
            HashMap hashMap = new HashMap();
            hashMap.put("AccountID", accountID);
            HashMap output = MFExecuter.executeMF("GetAvailableBalance", env, hashMap);
            availableBalance = (BigDecimal) output.get("AvailableBalance");
        }
        catch (Exception exception) {
            availableBalance = BigDecimal.ZERO;
        }
        return availableBalance;

    }

    public int getStatus(String accountID, BankFusionEnvironment env) {
        String currentStatus = "0";
        int atmStatus = 0;
        ATMControlDetails controlDetails = ATMConfigCache.getInstance().getInformation(env);
        BusinessValidatorBean accountValidator = new BusinessValidatorBean();
        IBOAttributeCollectionFeature accountValues = (IBOAttributeCollectionFeature) env.getFactory().findByPrimaryKey(
                IBOAttributeCollectionFeature.BONAME, accountID);
        IBOATMAccountDetails atmAccountDetails = (IBOATMAccountDetails) env.getFactory().findByPrimaryKey(
                IBOATMAccountDetails.BONAME, accountID);
        ArrayList params = new ArrayList();
        params.add(accountID);
        List dormancyFeatureList = env.getFactory().findByQuery(IBODormancyFeature.BONAME, findDormanyFeature, params, null);
        boolean isAccountStopped = accountValidator.validateAccountStopped(accountValues, env);

        if (isAccountStopped) {
            currentStatus = controlDetails.getStopped();
        }

        Iterator dormancyFeatureIterator = dormancyFeatureList.iterator();
        if (dormancyFeatureIterator.hasNext()) {
            IBODormancyFeature dormancyFeature = (IBODormancyFeature) dormancyFeatureIterator.next();
            if (dormancyFeature.isF_DORMANCYSTATUS()) {
                atmStatus = currentStatus.compareTo(controlDetails.getInactiveAccount());
                if (atmStatus < 0) {
                    currentStatus = controlDetails.getInactiveAccount();
                }
            }
        }
        String tempCurrentStatus = currentStatus;

        IBOAccount accountItem = (IBOAccount) env.getFactory().findByPrimaryKey(IBOAccount.BONAME, accountID);
        int accountStatus = accountItem.getF_ACCRIGHTSINDICATOR();
        if (accountStatus == PasswordProtectedConstants.PASSWD_NOT_REQ) {
            currentStatus = controlDetails.getNoPasswordRequired();
        }
        else if (accountStatus == PasswordProtectedConstants.ACCOUNT_STOPPED_NO_POSTING_ENQUIRY) {
            currentStatus = controlDetails.getStopped();
        }
        else if (accountStatus == PasswordProtectedConstants.ACCOUNT_STOPPED_PASSWD_REQ_FOR_POSTING_ENQUIRY) {
            currentStatus = controlDetails.getStoppedPwdReqForPosAndEnq();
        }
        else if (accountStatus == PasswordProtectedConstants.CREDITS_NOT_ALLOWED) {
            currentStatus = controlDetails.getNoCrTxnAllowed();
        }
        else if (accountStatus == PasswordProtectedConstants.CREDITS_NOT_ALLOWED_PASSWD_REQ_TO_OVERRIDE) {
            currentStatus = controlDetails.getPasswordRequiredForCrTxn();
        }
        else if (accountStatus == PasswordProtectedConstants.DEBITS_NOT_ALLOWED) {
            currentStatus = controlDetails.getNoDrTxnAllowed();
        }
        else if (accountStatus == PasswordProtectedConstants.DEBITS_NOT_ALLOWED_PASSWD_REQ_TO_OVERRIDE) {
            currentStatus = controlDetails.getPasswordRequiredForDrTxn();
        }
        else if (accountStatus == PasswordProtectedConstants.PASSWD_REQ_FOR_ENQUIRY) {
            currentStatus = controlDetails.getPasswordRequiredForEnq();
        }
        else if (accountStatus == PasswordProtectedConstants.PASSWD_REQ_FOR_POSTING) {
            currentStatus = controlDetails.getPasswordRequiredForPosting();
        }
        else if (accountStatus == PasswordProtectedConstants.PASSWD_REQ_FOR_POSTING_ENQUIRY) {
            currentStatus = controlDetails.getPasswordRequiredForAllTxn();
        }

        atmStatus = tempCurrentStatus.compareTo(currentStatus);
        if (atmStatus > 0) {
            currentStatus = tempCurrentStatus;
        }
        // Checking for HOT Card Status.
        int tempStatus = new Integer(currentStatus).intValue();

        if (atmAccountDetails.getF_ACCOUNTSTATUS() > tempStatus) {
            tempStatus = atmAccountDetails.getF_ACCOUNTSTATUS();
        }
        return tempStatus;
    }

    public String getSign(BigDecimal amount) {
        String sign = CommonConstants.EMPTY_STRING;
        if (amount.abs() == amount)
            sign = "+";
        else sign = "-";
        return sign;
    }

    public String getScaledAmount(BigDecimal amount, String currencyCode, int size) {
        amount = amount.abs();
        int scale = SystemInformationManager.getInstance().getCurrencyScale(currencyCode);
        return formatAmount(amount, scale, size);
    }

    private String formatAmount(BigDecimal amount, int scale, int size) {
        StringBuffer formattedAmount = new StringBuffer(ConvertToString.run(amount, scale));
        formattedAmount.lastIndexOf(".");
        int index = formattedAmount.lastIndexOf(".");
        if (index > -1) {
            formattedAmount.delete(index, index + 1);
        }
        return leftPad(formattedAmount.toString(), "0", size);
    }

    /**
     * method to left pad a string with a given string to a given size. This method will repeat the
     * padder string as many times as is necessary until the exact specified size is reached. If the
     * specified size is less than the size of the original string then the original string is
     * returned unchanged.
     */

    public String leftPad(String stringToPad, String padder, int size) {
        if (padder.length() == 0) {
            return stringToPad;
        }
        StringBuffer strb;
        StringCharacterIterator sci;
        strb = new StringBuffer(size);
        sci = new StringCharacterIterator(padder);

        while (strb.length() < (size - stringToPad.length())) {
            for (char ch = sci.first(); ch != CharacterIterator.DONE; ch = sci.next()) {
                if (strb.length() < size - stringToPad.length()) {
                    strb.insert(strb.length(), String.valueOf(ch));
                }
            }
        }
        return strb.append(stringToPad).toString();
    }

    public String getDateinStringFormat(Timestamp timeStamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        simpleDateFormat.applyPattern(ATMConstants.ATM_TIMESTAMP_PATTERN);
        String formattedDate = simpleDateFormat.format(timeStamp);
        return formattedDate;

    }

    public String getTransactionReference(ATMSparrowFinancialMessage message) {
        String reference = message.getSourceCountryCode() + message.getSourceIMD() + message.getSourceBranchCode()
                + message.getDeviceId() + message.getTxnSequenceNo() + getDateinStringFormat(message.getDateTimeofTxn());
        return reference;
    }

    /**
     * This method gets the currency code
     *
     * @returns String
     */
    public String getCurrencyCode(String numericCurrencyCode, BankFusionEnvironment env) {
        String currency = null;
        currency = getAlphaCurrencyCode(numericCurrencyCode, env);
        return currency;
    }

    /**
     * This method returns the accountId based on the pseudoname passed.
     *
     * @returns String
     */
    public String getAccountId(String pseudoName, BankFusionEnvironment env) {
        String accountId = null;
        ArrayList params = new ArrayList();
        Iterator accountList = null;
        IBOAccount account = null;
        params.add(pseudoName);
        accountList = env.getFactory().findByQuery(IBOAccount.BONAME, pseudoNameWhereClause, params, 1);
        if (accountList.hasNext()) {
            account = (IBOAccount) accountList.next();
            accountId = account.getBoID();
        }
        return accountId;
    }

    public String getBankTransactionCode(String transCode, BankFusionEnvironment env) {
        // String UBTransCode = CommonConstants.EMPTY_STRING;
    	ArrayList params = new ArrayList();
        if (UBTransCode == CommonConstants.EMPTY_STRING) {
            try {
            	params.add(transCode);
                IBOATMTransactionCodes atmTransactionCodes = (IBOATMTransactionCodes)env.getFactory().findFirstByQuery(IBOATMTransactionCodes.BONAME, FindBytransactionCode, params, false);
                if (atmTransactionCodes != null) {
                    UBTransCode = atmTransactionCodes.getF_MISTRANSACTIONCODE();
                }
            }
            catch (BankFusionException exception) {
                         logger.info(exception);
            }
        }
        return UBTransCode;
    }

    public int getSupportLevel(String transCode, BankFusionEnvironment env) {
        //Modified for artf795767 to handle the null pointer exception
        int supportLevel = ATMConstants.NOTSUPPORTED_TRANSACTION_FLAG;
        try {
        	ArrayList params = new ArrayList();
        	params.add(transCode);
        	IBOATMTransactionCodes atmTransactionCodes  = (IBOATMTransactionCodes)env.getFactory().findFirstByQuery(IBOATMTransactionCodes.BONAME, FindBytransactionCode, params, false);
        	if (atmTransactionCodes == null){
        	    supportLevel = ATMConstants.NOTSUPPORTED_TRANSACTION_FLAG;
        	    return supportLevel;
        	}
            supportLevel = atmTransactionCodes.getF_LEVELOFSUPPORT();
        }
        catch (BankFusionException exception) {
            supportLevel = ATMConstants.NOTSUPPORTED_TRANSACTION_FLAG;
        }

        return supportLevel;
    }

    public String getAccountIDfromPseudoName(String pseudoName, String ISONumericCurrencyCode, String branchSortCode,
            BankFusionEnvironment env) {

        String alphaCurrencyCode = getAlphaCurrencyCode(ISONumericCurrencyCode, env);

        // pseudoName = pseudoName + alphaCurrencyCode;
        if (logger.isInfoEnabled())
            logger.info(pseudoName);
        String accountNumber = CommonConstants.EMPTY_STRING;
        try {
            IBOAttributeCollectionFeature accountValues = FinderMethods.findAccountByPseudonameAndContextValue("%CURRENCY%"
                    + alphaCurrencyCode + "%" + pseudoName, alphaCurrencyCode, Boolean.TRUE, env, null);// PostingHelper.retrieveAccount(pseudoName,
            // branchSortCode,
            // alphaCurrencyCode,
            // env);
            if (!(accountValues == null)) {
                accountNumber = accountValues.getBoID();
            }
        }
        catch (BankFusionException exception) {
            logger.info(exception.getLocalizedMessage());
        }
        return accountNumber;
    }

    /**
     * This method gets the currency code
     *
     * @returns String
     */
    public String getAccountCurrency(String accountId, BankFusionEnvironment env) {
        String currency = " ";
        try {
            IBOAttributeCollectionFeature atmAccount = (IBOAttributeCollectionFeature) env.getFactory().findByPrimaryKey(
                    IBOAttributeCollectionFeature.BONAME, accountId);
            if (atmAccount != null) {
                currency = atmAccount.getF_ISOCURRENCYCODE();
            }
        }
        catch (BankFusionException exception) {
            logger.info(exception.getLocalisedMessage());
        }
        return currency;
    }

    public String getBranchSortCode(String brancNumber, BankFusionEnvironment env) {
        String branchSortCode = null;
        try {
            ArrayList params = new ArrayList();
            params.add(brancNumber);
            @SuppressWarnings("FBPE")
            List branchList = env.getFactory().findByQuery(IBOBranch.BONAME, findBranchSortCode, params, null);
            if (branchList.size() > 0) {
                IBOBranch branch = (IBOBranch) branchList.get(0);
                branchSortCode = branch.getBoID();
            }
        }
        catch (BankFusionException exception) {
        	logger.info(exception);
        }
        return branchSortCode;
    }

    public String getAlphaCurrencyCode(String ISONumericCurrencuCode, BankFusionEnvironment env) {
        String alphaCode = CommonConstants.EMPTY_STRING;
        try {
        	CB_CMN_FetchCurrencyDetails fetchCurrencyDetails = new CB_CMN_FetchCurrencyDetails(env); 
        	fetchCurrencyDetails.setF_IN_ISOCurrencyCode(ISONumericCurrencuCode);
        	fetchCurrencyDetails.process(env);
        	CurrencyDetails cd = fetchCurrencyDetails.getF_OUT_CurrencyInfo();
        	alphaCode = cd.getCurrencyCodeDetails().getIsoCurrencyCode();
        }
        catch (BankFusionException exception) {
        	logger.error("Exception occurred while fecthing currency details", exception);
        }
        return alphaCode;
    }

    public String getTransactionDescription(String transCode, BankFusionEnvironment env) {
        //Modified for artf795767 to handle the null pointer exception
        String transDescription = CommonConstants.EMPTY_STRING;
        try {
        	 ArrayList params = new ArrayList();
        	  params.add(transCode);

            IBOATMTransactionCodes atmTransactionCodes = (IBOATMTransactionCodes)env.getFactory().findFirstByQuery(IBOATMTransactionCodes.BONAME, FindBytransactionCode, params, false);
            if (atmTransactionCodes == null){
                return transDescription;
            }
            transDescription = atmTransactionCodes.getF_DESCRIPTION();
        }
        catch (BankFusionException exception) {
        	logger.info(exception);
        }
        return transDescription;
    }

    public void updateTransactionNarration(ATMSparrowMessage atmMessage, BankFusionEnvironment env) {
        //Modified for artf795767 to handle the null pointer exception
        String transNarration = CommonConstants.EMPTY_STRING;
        String microflowName = CommonConstants.EMPTY_STRING;
        HashMap inputMap = new HashMap();
        try {
        	ArrayList params = new ArrayList();
      	  params.add(atmMessage.getMessageType() + atmMessage.getTransactionType());

        	IBOATMTransactionCodes atmTransactionCodes = (IBOATMTransactionCodes)env.getFactory().findFirstByQuery(IBOATMTransactionCodes.BONAME, FindBytransactionCode, params, false);
          //  IBOATMTransactionCodes atmTransactionCodes = (IBOATMTransactionCodes) env.getFactory().findByPrimaryKey(
               //     IBOATMTransactionCodes.BONAME, (atmMessage.getMessageType() + atmMessage.getTransactionType()));
        	if (atmTransactionCodes != null){
        	    transNarration = atmTransactionCodes.getF_NARRATIVE();
                microflowName = atmTransactionCodes.getF_NarrativeGenerator();
                inputMap.put(ATMConstants.FIXEDNARRATIVE, transNarration);
                inputMap.put(ATMConstants.CUSTOMERNARRATIVE, transNarration);
                inputMap.put(ATMConstants.CONTRANARRATIVE, transNarration);
                inputMap.put(ATMConstants.MICROFLOWID, microflowName);
                inputMap.put(ATMConstants.AUTHORISEDFLAG, atmMessage.getAuthorisedFlag().toString());
                inputMap.put(ATMConstants.CARDDESTBRANCHCODE, atmMessage.getCardDestBranchCode().toString());
                inputMap.put(ATMConstants.CARDDESTCOUNTRYCODE, atmMessage.getCardDestCountryCode().toString());
                inputMap.put(ATMConstants.CARDDESTINATIONIMD, atmMessage.getCardDestinationIMD().toString());
                inputMap.put(ATMConstants.CARDNUMBER, atmMessage.getCardNumber().toString());
                inputMap.put(ATMConstants.CARDSEQUENCENO, atmMessage.getCardSequenceNo().toString());
                inputMap.put(ATMConstants.DATETIMEOFTXN, atmMessage.getDateTimeofTxn().toString());
                logger.info(atmMessage.getDateTimeofTxn().toString());
                inputMap.put(ATMConstants.DESTINATIONMAILBOX, atmMessage.getDestinationMailBox().toString());
                inputMap.put(ATMConstants.DEVICEID, atmMessage.getDeviceId().toString());
                inputMap.put(ATMConstants.FORCEPOST, atmMessage.getForcePost().toString());
                inputMap.put(ATMConstants.MESSAGETYPE, atmMessage.getMessageType().toString());
                inputMap.put(ATMConstants.SOURCEBRANCHCODE, atmMessage.getSourceBranchCode().toString());
                inputMap.put(ATMConstants.SOURCECOUNTRYCODE, atmMessage.getSourceCountryCode().toString());
                inputMap.put(ATMConstants.SOURCEIMD, atmMessage.getSourceIMD().toString());
                inputMap.put(ATMConstants.SOURCEMAILBOX, atmMessage.getSourceMailBox().toString());
                inputMap.put(ATMConstants.TRANSACTIONTYPE, atmMessage.getTransactionType().toString());
                // inputMap.put(ATMConstants.TXNDESCRIPTION, atmMessage.getTxnDescription().toString());
                inputMap.put(ATMConstants.TXNSEQUENCENO, atmMessage.getTxnSequenceNo().toString());
                ATMSparrowFinancialMessage financialMessage = (ATMSparrowFinancialMessage) atmMessage;
                inputMap.put(ATMConstants.ACCOUNT, financialMessage.getAccount().toString());
                inputMap.put(ATMConstants.ACTIONCODE, financialMessage.getActionCode().toString());
                inputMap.put(ATMConstants.AMOUNT1, financialMessage.getAmount1().toString());
                inputMap.put(ATMConstants.AMOUNT2, financialMessage.getAmount2().toString());
                inputMap.put(ATMConstants.AMOUNT3, financialMessage.getAmount3().toString());
                inputMap.put(ATMConstants.AMOUNT4, financialMessage.getAmount4().toString());
                inputMap.put(ATMConstants.CURRENCYDESTDISPENSED, financialMessage.getCurrencyDestDispensed().toString());
                inputMap.put(ATMConstants.CURRENCYSOURCEACCOUNT, financialMessage.getCurrencySourceAccount().toString());
                inputMap.put(ATMConstants.DESCDESTACC, financialMessage.getDescDestAcc().toString());
                inputMap.put(ATMConstants.DESTACCOUNTNUMBER, financialMessage.getDestAccountNumber().toString());
                inputMap.put(ATMConstants.DESCSOURCEACC, financialMessage.getDescSourceAcc().toString());
                inputMap.put(ATMConstants.LOCALCURRENCYCODE, financialMessage.getLocalCurrencyCode().toString());
                inputMap.put(ATMConstants.LOROMAILBOX, financialMessage.getLoroMailbox().toString());
                inputMap.put(ATMConstants.SUBINDEX, financialMessage.getSubIndex().toString());
                inputMap.put(ATMConstants.VARIABLEDATATYPE, financialMessage.getVariableDataType().toString());
                if (atmMessage instanceof ATMLocalMessage) {
                    ATMLocalMessage localMessage = (ATMLocalMessage) atmMessage;
                    inputMap.put(ATMConstants.LOCALACTUALBALANCE, localMessage.getActualBalance().toString());
                    inputMap.put(ATMConstants.LOCALAVAILABLEBALANCE, localMessage.getAvailableBalance().toString());
                    inputMap.put(ATMConstants.BRANCHNAME, localMessage.getBranchName().toString());
                    inputMap.put(ATMConstants.LOCALEXTENSIONVERSION, localMessage.getExtensionVersion().toString());
                }
                if (atmMessage instanceof ATMPOSMessage) {
                    ATMPOSMessage posMessage = (ATMPOSMessage) atmMessage;
                    inputMap.put(ATMConstants.POSAUTHORISATIONCODE, posMessage.getAuthorisationCode().toString());
                    inputMap.put(ATMConstants.POSCASHBACKAMOUNT, posMessage.getCashBackAmount().toString());
                    inputMap.put(ATMConstants.EXTENSIONVERSION, posMessage.getExtensionVersion().toString());
                    inputMap.put(ATMConstants.EXTERNALTERMINALID, posMessage.getExternalTerminalID().toString());
                    inputMap.put(ATMConstants.POSMERCHANTCATEGORYCODE, posMessage.getMerchantCategoryCode().toString());
                    inputMap.put(ATMConstants.MERCHANTID, posMessage.getMerchantID().toString());
                    inputMap.put(ATMConstants.MERCHANTLOCATION, posMessage.getMerchantLocation().toString());
                    inputMap.put(ATMConstants.MERCHANTNAME, posMessage.getMerchantName().toString());
                    inputMap.put(ATMConstants.SETTLEMENTIDENTIFIER, posMessage.getSettlementIdentifier().toString());
                }
                if (atmMessage instanceof ATMExNwMessage) {
                    ATMExNwMessage externalMessage = (ATMExNwMessage) atmMessage;
                    inputMap.put(ATMConstants.ACQUIRINGINSTITUTIONID, externalMessage.getAcquiringInstitutionID().toString());
                    inputMap.put(ATMConstants.ACTUALBALANCE, externalMessage.getActualBalance().toString());
                    inputMap.put(ATMConstants.AUTHORISATIONCODE, externalMessage.getAuthorisationCode().toString());
                    inputMap.put(ATMConstants.AVAILABLEBALANCE, externalMessage.getAvailableBalance().toString());
                    inputMap.put(ATMConstants.CARDACCEPTORID, externalMessage.getCardAcceptorID().toString());
                    inputMap.put(ATMConstants.CARDACCEPTORNAME, externalMessage.getCardAcceptorName().toString());
                    inputMap.put(ATMConstants.CARDACCEPTORTERMINALID, externalMessage.getCardAcceptorTerminalID().toString());
                    inputMap.put(ATMConstants.CASHBACKACCOUNT, externalMessage.getCashBackAccount().toString());
                    inputMap.put(ATMConstants.CASHBACKAMOUNT, externalMessage.getCashBackAmount().toString());
                    inputMap.put(ATMConstants.CASHBACKDEVICE, externalMessage.getCashBackDevice().toString());
                    inputMap.put(ATMConstants.CONVERSIONRATE, externalMessage.getConversionRate().toString());
                    inputMap.put(ATMConstants.CURRENCYCODE, externalMessage.getCurrencyCode().toString());
                    inputMap.put(ATMConstants.EXTENSIONNUMBER, externalMessage.getExtensionNumber().toString());
                    inputMap.put(ATMConstants.EXTERNALNETWORKID, externalMessage.getExternalNetworkID().toString());
                    inputMap.put(ATMConstants.FORWARDINGINSTITUTIONID, externalMessage.getForwardingInstitutionID().toString());
                    inputMap.put(ATMConstants.MERCHANTCATEGORYCODE, externalMessage.getMerchantCategoryCode().toString());
                    inputMap.put(ATMConstants.SETTLEMENTAMOUNT, externalMessage.getSettlementAmount().toString());
                    inputMap.put(ATMConstants.SETTLEMENTCONVRATE, externalMessage.getSettlementConvRate().toString());
                    inputMap.put(ATMConstants.SETTLEMENTCURRENCY, externalMessage.getSettlementCurrency().toString());
                    inputMap.put(ATMConstants.TRANSACTIONAMOUNT, externalMessage.getTransactionAmount().toString());
                }
                HashMap outputParams = MFExecuter.executeMF(microflowName, env, inputMap);
                String tempContraNarrative = outputParams.get(ATMConstants.CONTRANARRATIVE).toString();
                String tempCustNarrative = outputParams.get(ATMConstants.CUSTOMERNARRATIVE).toString();
                if (atmMessage.getMessageType().equals("0") || atmMessage.getMessageType().equals("7")) {
                    IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory.getInstance()
                            .getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
                    String value = ubInformationService.getBizInfo().getModuleConfigurationValue("ATM",
                            "SUSPECT_REVERSAL_NARRATIVE", env).toString();
                    tempContraNarrative = value.concat(tempContraNarrative);
                    tempCustNarrative = value.concat(tempCustNarrative);
                }
    			if( tempContraNarrative.length()<=100) {

                atmMessage.setTxnContraNarrative(tempContraNarrative);
    			}
    			else{
    				atmMessage.setTxnContraNarrative(tempContraNarrative.substring(0, 99));
    			}
    			if(tempCustNarrative.length()<=100){
    				atmMessage.setTxnCustomerNarrative(tempCustNarrative);
    			}
    			else {
    				atmMessage.setTxnCustomerNarrative(tempCustNarrative.substring(0, 99));
    			}

                //atmMessage.setTxnCustomerNarrative(tempCustNarrative);
        	//}else{
              //  atmMessage.setTxnContraNarrative(CommonConstants.EMPTY_STRING);
                //atmMessage.setTxnCustomerNarrative(CommonConstants.EMPTY_STRING);
        	}

        }
        catch (BankFusionException exception) {
        	logger.info(exception);
        }
    }

    /*
     * Checks if a Given Transaction can be Posted to the Main Account.
     */

    public boolean isAccountValid(ATMSparrowFinancialMessage message, int operationType, BankFusionEnvironment env) {
      //  BusinessValidatorBean validator = new BusinessValidatorBean();
        boolean isAccountValid = true;
        Object[] fields = new Object[1];
        // Is Account Password Protected.
        try {
          //   IBOAccount accountItem = (IBOAccount) env.getFactory().findByPrimaryKey(IBOAccount.BONAME, message.getAccount());
         //   int rightsIndicator = accountItem.getF_ACCRIGHTSINDICATOR();
            fields[0] = message.getAccount();
        }
        catch (BankFusionException exception) {
            if (message.getForcePost().equals(ATMConstants.FORCEPOST_0) || message.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
                message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
                isAccountValid = false;
                return isAccountValid;
            }
            else {
                message.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
                isAccountValid = false;
            }

        }
        // Is Account Dormant.
        boolean isAccountDormant = isAccountDormant(message.getAccount(), env);
        if (isAccountDormant) {
            String bankTransCode = getBankTransactionCode(message.getMessageType() + message.getTransactionType(), env);
            IBOMisTransactionCodes transDetails = null;
            try {
                if (CommonConstants.EMPTY_STRING.equals(bankTransCode)) {
                    boolean isPosTransaction = isPOSTransaction(message.getVariableDataType());
                    if (isPosTransaction)
                        bankTransCode = ATMConfigCache.getInstance().getInformation(env).getPosTxnType();
                    else bankTransCode = ATMConfigCache.getInstance().getInformation(env).getAtmTransactionType();
                }
                // Using the Cache of TransactionScreenControl Table for fetching the details.
                MISTransactionCodeDetails mistransDetails;
                IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory
                        .getInstance().getServiceManager().getServiceForName(
                                IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
                mistransDetails = ((IBusinessInformation) ubInformationService.getBizInfo())
                        .getMisTransactionCodeDetails(bankTransCode);

                transDetails = mistransDetails.getMisTransactionCodes();
            }
            catch (BankFusionException exception) {
            	logger.info(exception);
            }
            
            if(transDetails != null){
            String dormancyPostingAction = transDetails.getF_DORMANCYPOSTINGACTION();
            if (dormancyPostingAction.equals("0") || dormancyPostingAction.equals("3")) {
                isAccountValid = true;
            }
            else {
                if (message.getForcePost().equals(ATMConstants.FORCEPOST_0)
                        || message.getForcePost().equals(ATMConstants.FORCEPOST_6)
                        || message.getForcePost().equals(ATMConstants.FORCEPOST_7)) {
                    message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
                    message.setErrorCode(ATMConstants.ERROR);
                    message.setErrorDescription(BankFusionMessages
                            .getFormattedMessage(ChannelsEventCodes.E_ACCOUNT_DORMANT, fields));
                    isAccountValid = false;
                }
                else {
                    message.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
                    message.setErrorCode(ATMConstants.WARNING);
                    message.setErrorDescription(BankFusionMessages.getFormattedMessage(
                            ChannelsEventCodes.W_ACC_DORMANT_SUS_ACC_UPDTD, fields));
                    isAccountValid = false;
                	}
                return isAccountValid;
            	}
            }
        }
        // Check for Available Balance

        // Check for Group Limits.

        // Other Checks if Necessary.
        // Blacklist check changes for CRDB begins
        boolean isCustomerBlckListed = isCustomerBlacklisted(message.getAccount(), env);
        if (!isCustomerBlckListed) {
            if (message.getForcePost().equals(ATMConstants.FORCEPOST_0) || message.getForcePost().equals(ATMConstants.FORCEPOST_6)
                    || message.getForcePost().equals(ATMConstants.FORCEPOST_7)) {
                message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
                message.setErrorCode(ATMConstants.ERROR);
                message.setErrorDescription(BankFusionMessages.getFormattedMessage(
                        CommonsEventCodes.E_CUSTOMER_BLACKLISTED_COMMON_EVENT, fields));
                isAccountValid = false;
            }
            else {
                message.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
                message.setErrorCode(ATMConstants.ERROR);
                message.setErrorDescription("Customer is Blacklisted. Posting to Suspense Account");
                isAccountValid = false;
            }
            return isAccountValid;
        }
        // Blacklist check changes for CRDB ends

        return isAccountValid;
    }

    // Blacklist check changes for CRDB begins
    /**
     * Method to check whether the customer is a blacklisted customer
     *
     * @param accountNumber
     * @param env
     * @return
     */
    public boolean isCustomerBlacklisted(String accountNumber, BankFusionEnvironment env) {
        boolean isCustomerBlkListed = true;
        try {
            ValidateCustomer validateCustomer = new ValidateCustomer();
            isCustomerBlkListed = validateCustomer.customerStatus(accountNumber, env);
        }
        catch (BankFusionException exception) {
            return false;
        }
        return isCustomerBlkListed;
    }

    // Blacklist check changes for CRDB ends

    public boolean isAccountDormant(String accountID, BankFusionEnvironment env) {
        boolean result = false;
        ArrayList list = new ArrayList();
        list.add(accountID);
        try {
            IBODormancyFeature dormancyFeature = (IBODormancyFeature) env.getFactory().findFirstByQuery(IBODormancyFeature.BONAME,
                    findDormanyFeature, list);
            if (dormancyFeature != null) {
                result = dormancyFeature.isF_DORMANCYSTATUS();
            }
        }
        catch (BankFusionException exception) {
        	logger.info(exception);
        }

        return result;
    }

    private boolean isPOSTransaction(String varaibleDataType) {
        if ("P".equals(varaibleDataType))
            return true;
        else return false;
    }

    public boolean isAccountValid(ATMSparrowFinancialMessage message, String accountId, int PasswordOperation,
            BankFusionEnvironment env) {
      //  BusinessValidatorBean validator = new BusinessValidatorBean();
        boolean isAccountValid = true;
        Object[] fields = new Object[1];
        // Is Account Password Protected.
        try {
          //  IBOAccount accountItem = (IBOAccount) env.getFactory().findByPrimaryKey(IBOAccount.BONAME, accountId);
          //  int rightsIndicator = accountItem.getF_ACCRIGHTSINDICATOR();
            fields[0] = accountId;
        }
        catch (BankFusionException exception) {
            if (message.getForcePost().equals(ATMConstants.FORCEPOST_0) || message.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
                message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
                isAccountValid = false;
                return isAccountValid;
            }
            else {
                message.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
                isAccountValid = false;
            }

        }
        // Is Acoount Dormant.
        boolean isAccountDormant = isAccountDormant(accountId, env);
        if (isAccountDormant) {
            String bankTransCode = getBankTransactionCode(message.getMessageType() + message.getTransactionType(), env);
            IBOMisTransactionCodes transDetails = null;
            try {
                if (CommonConstants.EMPTY_STRING.equals(bankTransCode)) {
                    boolean isPosTransaction = isPOSTransaction(message.getVariableDataType());
                    if (isPosTransaction)
                        bankTransCode = ATMConfigCache.getInstance().getInformation(env).getPosTxnType();
                    else bankTransCode = ATMConfigCache.getInstance().getInformation(env).getAtmTransactionType();
                }
                // Using the Cache of TransactionScreenControl Table for fetching the details.
                MISTransactionCodeDetails mistransDetails;
                IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory
                        .getInstance().getServiceManager().getServiceForName(
                                IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
                mistransDetails = ((IBusinessInformation) ubInformationService.getBizInfo())
                        .getMisTransactionCodeDetails(bankTransCode);

                transDetails = mistransDetails.getMisTransactionCodes();
            }
            catch (BankFusionException exception) {
            	logger.info(exception);
            }
            
            if(transDetails != null){
            String dormancyPostingAction = transDetails.getF_DORMANCYPOSTINGACTION();
            if (dormancyPostingAction.equals("0") || dormancyPostingAction.equals("3")) {
                isAccountValid = true;
            }
            else {
                if (message.getForcePost().equals(ATMConstants.FORCEPOST_0)
                        || message.getForcePost().equals(ATMConstants.FORCEPOST_6)
                        || message.getForcePost().equals(ATMConstants.FORCEPOST_7)) {
                    message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
                    message.setErrorCode(ATMConstants.WARNING);
                    message.setErrorDescription(BankFusionMessages
                            .getFormattedMessage(ChannelsEventCodes.E_ACCOUNT_DORMANT, fields));
                    isAccountValid = false;
                }
                else {
                    message.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
                    message.setErrorCode(ATMConstants.ERROR);
                    message.setErrorDescription(BankFusionMessages.getFormattedMessage(
                            ChannelsEventCodes.W_ACC_DORMANT_SUS_ACC_UPDTD, fields));
                    isAccountValid = false;
                	}
                return isAccountValid;
            	}
            }
        }
        // Check for Available Balance

        // Check for Group Limits.

        // Other Checks if Necessary.
        // Blacklist check changes for CRDB begins
        boolean isCustomerBlckListed = isCustomerBlacklisted(message.getAccount(), env);
        if (!isCustomerBlckListed) {
            if (message.getForcePost().equals(ATMConstants.FORCEPOST_0) || message.getForcePost().equals(ATMConstants.FORCEPOST_6)
                    || message.getForcePost().equals(ATMConstants.FORCEPOST_7)) {
                message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
                message.setErrorCode(ATMConstants.ERROR);
                message.setErrorDescription(BankFusionMessages.getFormattedMessage(
                        CommonsEventCodes.E_CUSTOMER_BLACKLISTED_COMMON_EVENT, fields));
                isAccountValid = false;
            }
            else {
                message.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
                message.setErrorCode(ATMConstants.ERROR);
                message.setErrorDescription("Customer is Blacklisted. Posting to Suspense Account");
                isAccountValid = false;
            }
            return isAccountValid;
        }
        // Blacklist check changes for CRDB ends
        return isAccountValid;
    }

    /**
     *
     * Method Description:
     *
     * @param authorisedFlag
     *            it will be 0 or 1
     * @param errorCode
     *            it will be warning,critical or information.
     * @param errorNo
     *            like 75566
     * @param errorLevel
     *            Message_Level or Error_Level
     * @param atmLocalMessage
     *            it stores the ATM Sparrow Local Messages.
     * @param fields
     *            object reference.
     * @param env
     *            it holds the Session variables.
     */
    public void populateErrorDetails(String authorisedFlag, String errorCode, int errorNo, String errorLevel,
            ATMSparrowFinancialMessage atmLocalMessage, Object[] fields, BankFusionEnvironment env) {
        atmLocalMessage.setAuthorisedFlag(authorisedFlag);
        atmLocalMessage.setErrorCode(errorCode);
        atmLocalMessage.setErrorDescription(BankFusionMessages.getFormattedMessage(errorNo, fields));
    }

    // this method has to be deleted when the BF platform fix for
    // throwCollectedEventsDialogException is done.

    public boolean validatePasswordProtection(String accountID, int passwordProtectionFlag, int operationType,
            BankFusionEnvironment env) {
        Map params = new HashMap();
        params.put("ACCOUNTID", accountID);
        boolean isPasswordProtected = false;
        switch (passwordProtectionFlag) {
            case PasswordProtectedConstants.PASSWD_REQ_FOR_POSTING_ENQUIRY:
                // EventsHelper.handleEvent(7319, BankFusionMessages.ERROR_LEVEL, new Object[] {
                // accountID }, params, env);
                isPasswordProtected = true;
                EventsHelper.handleEvent(ChannelsEventCodes.E_ACCOUNT_____IS_PASSWORD_PROTECTED, new Object[] { accountID },
                        params, env);
                break;
            case PasswordProtectedConstants.PASSWD_REQ_FOR_POSTING:
                // EventsHelper.handleEvent(7319, BankFusionMessages.ERROR_LEVEL, new Object[] {
                // accountID }, params, env);
                isPasswordProtected = true;
                EventsHelper.handleEvent(ChannelsEventCodes.E_ACCOUNT_____IS_PASSWORD_PROTECTED, new Object[] { accountID },
                        params, env);
                break;
            case PasswordProtectedConstants.ACCOUNT_STOPPED_NO_POSTING_ENQUIRY:
                // EventsHelper.handleEvent(7321, BankFusionMessages.ERROR_LEVEL, new Object[] {
                // accountID }, params, env);
                isPasswordProtected = true;
                EventsHelper.handleEvent(ChannelsEventCodes.E_ACCOUNT_____IS_PASSWORD_PROTECTED, new Object[] { accountID },
                        params, env);
                break;
            case PasswordProtectedConstants.ACCOUNT_STOPPED_PASSWD_REQ_FOR_POSTING_ENQUIRY:
                // EventsHelper.handleEvent(7321, BankFusionMessages.ERROR_LEVEL, new Object[] {
                // accountID }, params, env);
                isPasswordProtected = true;
                EventsHelper.handleEvent(ChannelsEventCodes.E_ACCOUNT_____IS_PASSWORD_PROTECTED, new Object[] { accountID },
                        params, env);
                break;
            case PasswordProtectedConstants.DEBITS_NOT_ALLOWED:
                if (operationType == PasswordProtectedConstants.OPERATION_DEBIT) {
                    // EventsHelper.handleEvent(7322, BankFusionMessages.ERROR_LEVEL, new Object[] {
                    // accountID }, params, env);
                    isPasswordProtected = true;
                    EventsHelper.handleEvent(ChannelsEventCodes.E_ACCOUNT_____IS_PASSWORD_PROTECTED, new Object[] { accountID },
                            params, env);
                }
                break;
            case PasswordProtectedConstants.DEBITS_NOT_ALLOWED_PASSWD_REQ_TO_OVERRIDE:
                if (operationType == PasswordProtectedConstants.OPERATION_DEBIT) {
                    // EventsHelper.handleEvent(7323, BankFusionMessages.ERROR_LEVEL, new Object[] {
                    // accountID }, params, env);
                    isPasswordProtected = true;
                    EventsHelper.handleEvent(ChannelsEventCodes.E_ACCOUNT_____IS_PASSWORD_PROTECTED, new Object[] { accountID },
                            params, env);
                }
                break;
            case PasswordProtectedConstants.CREDITS_NOT_ALLOWED:
                if (operationType == PasswordProtectedConstants.OPERATION_CREDIT) {
                    // EventsHelper.handleEvent(7324, BankFusionMessages.ERROR_LEVEL, new Object[] {
                    // accountID }, params, env);
                    isPasswordProtected = true;
                    EventsHelper.handleEvent(ChannelsEventCodes.E_ACCOUNT_____IS_PASSWORD_PROTECTED, new Object[] { accountID },
                            params, env);
                }
                break;
            case PasswordProtectedConstants.CREDITS_NOT_ALLOWED_PASSWD_REQ_TO_OVERRIDE:
                if (operationType == PasswordProtectedConstants.OPERATION_CREDIT) {
                    // EventsHelper.handleEvent(7325, BankFusionMessages.ERROR_LEVEL, new Object[] {
                    // accountID }, params, env);
                    isPasswordProtected = true;
                    EventsHelper.handleEvent(ChannelsEventCodes.E_ACCOUNT_____IS_PASSWORD_PROTECTED, new Object[] { accountID },
                            params, env);
                }
                break;
            case PasswordProtectedConstants.PASSWD_REQ_FOR_POSTING_GLOBAL_BASE_RATE_CHANGES: {
                // EventsHelper.handleEvent(7327, BankFusionMessages.ERROR_LEVEL, new Object[] {
                // accountID }, params, env);
                isPasswordProtected = true;
                EventsHelper.handleEvent(ChannelsEventCodes.E_ACCOUNT_____IS_PASSWORD_PROTECTED, new Object[] { accountID },
                        params, env);
                break;
            }
            default:
                break;
        }
        return isPasswordProtected;
    }

    // Password Protection Ends

    public boolean isAccountPasswordProtected(ATMSparrowFinancialMessage message, int operationType, String accountType,
            BankFusionEnvironment env) {
        boolean isAccountValid = true;
        Object[] fields = new Object[1];
        IBOAccount accountItem = null;
        // Is Account Password Protected.
        try {
            if (accountType.equals(ATMConstants.SOURCEACCOUNTTYPE)) {
                accountItem = (IBOAccount) env.getFactory().findByPrimaryKey(IBOAccount.BONAME, message.getAccount());
            }
            else {
                ATMControlDetails moduleDetails = new ATMControlDetails(env);
                String tempDestAccountNumber = message.getDestAccountNumber().substring(0, moduleDetails.getDestAccountLength());
                accountItem = (IBOAccount) env.getFactory().findByPrimaryKey(IBOAccount.BONAME, tempDestAccountNumber);
            }
            int rightsIndicator = accountItem.getF_ACCRIGHTSINDICATOR();
            fields[0] = accountItem.getBoID();
            try {
                validatePasswordProtection(accountItem.getBoID(), rightsIndicator, operationType, env);
                message.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
                isAccountValid = true;
            }
            catch (BankFusionException exception) {
                if (message.getForcePost().equals(ATMConstants.FORCEPOST_0)
                        || message.getForcePost().equals(ATMConstants.FORCEPOST_6)
                        || message.getForcePost().equals(ATMConstants.FORCEPOST_7)) {
                    message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
                    message.setErrorCode(ATMConstants.ERROR);
                    message.setErrorDescription(BankFusionMessages.getFormattedMessage(
                            ChannelsEventCodes.E_ACCOUNT_____IS_PASSWORD_PROTECTED, fields));
                    isAccountValid = false;
                }
                else {
                    message.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
                    message.setErrorCode(ATMConstants.WARNING);
                    message.setErrorDescription(BankFusionMessages.getFormattedMessage(
                            ChannelsEventCodes.W_ACCT_PASSORD_PROTECTED_SUS_ACCT_UPDATED, fields));
                    isAccountValid = false;
                }
                return isAccountValid;
            }
        }
        catch (BankFusionException exception) {
            if (message.getForcePost().equals(ATMConstants.FORCEPOST_0) || message.getForcePost().equals(ATMConstants.FORCEPOST_6)) {
                message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
                isAccountValid = false;
                return isAccountValid;
            }
            else {
                message.setAuthorisedFlag(ATMConstants.AUTHORIZED_MESSAGE_FLAG);
                isAccountValid = false;
            }

        }
        return isAccountValid;
    }

	// Merger for artf905806 Start
	public Timestamp checkForwardValuedTime(ATMSparrowFinancialMessage message) {

		Timestamp manualValueDate = message.getDateTimeofTxn();
		Timestamp businessDateTime = SystemInformationManager.getInstance().getBFBusinessDateTime();
		Timestamp businessDate = businessDateTime;
	//	Date manualValueDate1 = dateConvert(manualValueDate);
	//	Date businessDate1 = dateConvert(businessDate);

		if (manualValueDate.getTime() > businessDate.getTime()) {

			return businessDate;
		} else
			return manualValueDate;

	}


	public Date dateConvert(Date changeDate) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(changeDate);
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(changeDate);
		cal.set(Calendar.HOUR_OF_DAY, cal1.get(Calendar.HOUR_OF_DAY));
		cal.set(Calendar.MINUTE, cal1.get(Calendar.MINUTE));
		cal.set(Calendar.SECOND, cal1.get(Calendar.SECOND));
		cal.set(Calendar.MILLISECOND, cal1.get(Calendar.MILLISECOND));
		changeDate = cal.getTime();
		return changeDate;
	}
	// Merger for artf905806 End

	// Merger for artf956781

	 public Map getATMTransactionCodeDetails(String transCode, String atmTransType, BankFusionEnvironment env) {
	        // String UBTransCode = CommonConstants.EMPTY_STRING;
	    	ArrayList params = new ArrayList();
	    	Map txnCodeDetails = new HashMap();
	        if (UBTransCode == CommonConstants.EMPTY_STRING) {
	            try {
	            	params.add(transCode);
	                //IBOATMTransactionCodes atmTransactionCodes = (IBOATMTransactionCodes)env.getFactory().findFirstByQuery(IBOATMTransactionCodes.BONAME, FindBytransactionCode, params, false);

	                IBOATMTransactionCodes atmTransactionCodes = null;

	        		for (SimplePersistentObject obj : getCachedEntity(
	        				CacheConstants.ATM_TRAN_CODES, IBOATMTransactionCodes.BONAME)) {
	        			atmTransactionCodes = (IBOATMTransactionCodes) obj;
	        			if ((atmTransactionCodes.getF_ATMTRANSACTIONCODE().equals(
	        					transCode)) && (atmTransactionCodes.getF_UBATMTRANSACTIONTYPE().equals(atmTransType)))
	        				break;
	        			atmTransactionCodes=null;
	        		}

	                if (atmTransactionCodes != null) {
	                    UBTransCode = atmTransactionCodes.getF_MISTRANSACTIONCODE();
	                    txnCodeDetails.put("UBTRANSCODE", atmTransactionCodes.getF_MISTRANSACTIONCODE());
	                    txnCodeDetails.put("DESCRIPTION", atmTransactionCodes.getF_DESCRIPTION());
	                    txnCodeDetails.put("LEVELOFSUPPORT", atmTransactionCodes.getF_LEVELOFSUPPORT());
	                    txnCodeDetails.put("ATMTRANSACTIONCODE", atmTransactionCodes.getF_ATMTRANSACTIONCODE());
	                    txnCodeDetails.put("NARRATIVE", atmTransactionCodes.getF_NARRATIVE());
	                    txnCodeDetails.put("NARRATIVE_GENERATOR", atmTransactionCodes.getF_NarrativeGenerator());
	                    txnCodeDetails.put("UBATMTRANSACTIONTYPE", atmTransactionCodes.getF_UBATMTRANSACTIONTYPE());
	                }
	            }
	            catch (BankFusionException exception) {
	            	logger.info(exception);
	            }
	        }
	        return txnCodeDetails;
	    }


	    public List<SimplePersistentObject> getCachedEntity(String entity,
				String BOName) {
			IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory
					.getInstance()
					.getServiceManager()
					.getServiceForName(
							IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);

			List<SimplePersistentObject> atmEntityObject = ((IBusinessInformation) ubInformationService
					.getBizInfo()).getATMCacheObject(entity, BOName);

			return atmEntityObject;
		}

}
