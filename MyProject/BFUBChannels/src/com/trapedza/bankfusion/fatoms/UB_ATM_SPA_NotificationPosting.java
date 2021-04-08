package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.MISTransactionCodeDetails;
import com.misys.ub.common.events.CommonsEventCodes;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMConstants;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMHelper;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.BusinessValidatorBean;
import com.trapedza.bankfusion.features.extensionpoints.PasswordProtectedConstants;
import com.trapedza.bankfusion.persistence.exceptions.FinderException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ATM_SPA_NotificationPosting;
import com.trapedza.bankfusion.utils.BankFusionMessages;

public class UB_ATM_SPA_NotificationPosting extends AbstractUB_ATM_SPA_NotificationPosting {
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
    private transient final static Log logger = LogFactory.getLog(UB_ATM_SPA_NotificationPosting.class.getName());

    /**
     * Holds shared switch value
     */
    public UB_ATM_SPA_NotificationPosting(BankFusionEnvironment env) {
        super(env);
    }

    public void process(BankFusionEnvironment env) {
        if (getF_IN_ProcessId() != null && getF_IN_ProcessId() == "") {
            if (isF_IN_ChargePosting() == false) {
                chargePosting(env);
                if (getF_OUT_AuthorizedFlag() == 1)
                    return;
            }
            else if (isF_IN_CommissionPosting() == false) {
                commissionPosting(env);
            }
            else {
                setF_OUT_AuthorizedFlag(getF_IN_Authorization());
                setF_OUT_ErrorMessage(getF_IN_ErrorMessage());
                setF_OUT_ErrorStatus(getF_IN_ErrorStatus());
            }
        }
        // artf46960 Changes start
        else if (getF_IN_ProcessId() != null && getF_IN_ProcessId() == "I") {
            chargePostingForInquiry(env);
            if (getF_OUT_AuthorizedFlag() == 1) {
                return;
            }
            else {
                commissionPostingForInquiry(env);
            }
        }
        else if (getF_IN_ProcessId() != null && getF_IN_ProcessId() == "V") {
            String accountId = getF_IN_MainAccountId();
            accountValidations(accountId, env);
        }
        // artf46960 Changes end

        // changes for artf46961 [start]
        else if (getF_IN_ProcessId() != null && getF_IN_ProcessId() == "M") {
            chargePostingForMiniStatement(env);
            if (getF_OUT_AuthorizedFlag() == 1) {
                return;
            }
            else {
                commissionPostingForMiniStatement(env);
            }
        }
        // changes for artf46961 [end]
    }

    public void chargePosting(BankFusionEnvironment env) {
        HashMap inputmap = new HashMap();
        inputmap.put("1_POSTINGMESSAGEACCOUNTID", getF_IN_ChargeAccountId());
        inputmap.put("1_POSTINGMESSAGEISOCURRENCYCODE", getF_IN_ISOCurrencyCode());
        inputmap.put("1_POSTINGMESSAGETRANSACTIONAMOUNT", (BigDecimal) getF_IN_ChargeAmount());
        inputmap.put("1_POSTINGMESSAGETRANSACTIONCODE", getF_IN_MISTransactionCode());
        inputmap.put("1_POSTINGMESSAGETRANSACTIONNARRATIVE", getF_IN_Narrative());
        inputmap.put("1_POSTINGMESSAGETRANSACTIONREFERENCE", getF_IN_TXRef());
        inputmap.put("2_POSTINGMESSAGEACCOUNTID", getF_IN_ChargeCreditAccountId());
        inputmap.put("2_POSTINGMESSAGEISOCURRENCYCODE", getF_IN_ISOCurrencyCode());
        inputmap.put("3_POSTINGMESSAGETRANSACTIONREFERENCE", getF_IN_TXRef());
        inputmap.put("1_POSTINGMESSAGEACCOUNTPOSTINGACTION", "D");
        inputmap.put("2_POSTINGMESSAGEACCOUNTPOSTINGACTION", "C");
        inputmap.put("FORCEPOST", (Boolean) isF_IN_ForcePost());
        inputmap.put("MAINACCOUNTID", getF_IN_MainAccountId());
        inputmap.put("MessageNumber", getF_IN_MessageNumber());
        inputmap.put("TRANSACTIONID", getF_IN_UniqueId());
        try {
            HashMap output = MFExecuter.executeMF("UB_Interfaces_ATM_SPA_ChargesPosting_SRV", env, inputmap);
            String auth = output.get("AuthorisationFlag").toString().trim();
            int Authorization = 0;
            if (auth != null && auth.length() > 0) {
                Authorization = (Integer) output.get("AuthorisationFlag");
            }
            else {
                Authorization = 0;
            }
            String ErrorMessage = output.get("MESSAGE").toString();
            String ErrorStatus = output.get("ErrorStatus").toString();
            if (Authorization == 1) {
                setF_OUT_AuthorizedFlag(Authorization);
                setF_OUT_ErrorMessage(ErrorMessage);
                setF_OUT_ErrorStatus(ErrorStatus);
            }
        }
        catch (BankFusionException e) {
            logger.error(e);
            setF_OUT_AuthorizedFlag(1);
            setF_OUT_ErrorMessage(e.getMessage());
            setF_OUT_ErrorStatus(ATMConstants.ERROR);
        }
    }

    public void commissionPosting(BankFusionEnvironment env) {
        HashMap inputmap = new HashMap();
        inputmap.put("1_POSTINGMESSAGEACCOUNTID", getF_IN_CommissionAccount());
        inputmap.put("1_POSTINGMESSAGEISOCURRENCYCODE", getF_IN_ISOCurrencyCode());
        inputmap.put("1_POSTINGMESSAGETRANSACTIONAMOUNT", (BigDecimal) getF_IN_CommissionAmount());
        inputmap.put("1_POSTINGMESSAGETRANSACTIONCODE", getF_IN_MISTransactionCode());
        inputmap.put("1_POSTINGMESSAGETRANSACTIONNARRATIVE", getF_IN_Narrative());
        inputmap.put("1_POSTINGMESSAGETRANSACTIONREFERENCE", getF_IN_TXRef());
        inputmap.put("2_POSTINGMESSAGEACCOUNTID", getF_IN_ChargeCreditAccountId());
        inputmap.put("2_POSTINGMESSAGEISOCURRENCYCODE", getF_IN_ISOCurrencyCode());
        inputmap.put("1_POSTINGMESSAGEACCOUNTPOSTINGACTION", "D");
        inputmap.put("2_POSTINGMESSAGEACCOUNTPOSTINGACTION", "C");
        inputmap.put("FORCEPOST", (Boolean) isF_IN_ForcePost());
        inputmap.put("MAINACCOUNTID", getF_IN_MainAccountId());
        inputmap.put("MessageNumber", getF_IN_MessageNumber());
        inputmap.put("TRANSACTIONID", getF_IN_UniqueId());
        try {
            HashMap output = MFExecuter.executeMF("UB_Interfaces_ATM_SPA_CommissionsPosting_SRV", env, inputmap);
            String auth = output.get("AuthorisationFlag").toString().trim();
            int Authorization = 0;
            if (auth != null && auth.length() > 0) {
                Authorization = (Integer) output.get("AuthorisationFlag");
            }
            else {
                Authorization = 0;
            }
            String ErrorMessage = output.get("Error").toString();
            String ErrorStatus = output.get("ErrorStatus").toString();
            if (Authorization == 1) {
                setF_OUT_AuthorizedFlag(Authorization);
                setF_OUT_ErrorMessage(ErrorMessage);
                setF_OUT_ErrorStatus(ErrorStatus);
            }
            else {
                setF_OUT_AuthorizedFlag(getF_IN_Authorization());
                setF_OUT_ErrorMessage(getF_IN_ErrorMessage());
                setF_OUT_ErrorStatus(getF_IN_ErrorStatus());
            }
            setF_OUT_IsChargeWaived((Boolean) output.get("IsChargeWaived"));
        }
        catch (BankFusionException e) {
            logger.error(e);
            setF_OUT_AuthorizedFlag(1);
            setF_OUT_ErrorMessage(e.getMessage());
            setF_OUT_ErrorStatus(ATMConstants.ERROR);
        }
    }

    // artf46960 Changes start
    public void chargePostingForInquiry(BankFusionEnvironment env) {
        HashMap inputmap = new HashMap();
        inputmap.put("EXTERNAL_BRANCH_CODE", getF_IN_ExternalBranchCode());
        inputmap.put("1_POSTINGMESSAGEACCOUNTID", getF_IN_ChargeAccountId());
        inputmap.put("1_POSTINGMESSAGEISOCURRENCYCODE", getF_IN_ISOCurrencyCode());
        inputmap.put("1_POSTINGMESSAGETRANSACTIONAMOUNT", (BigDecimal) getF_IN_ChargeAmount());
        inputmap.put("1_POSTINGMESSAGETRANSACTIONCODE", getF_IN_MISTransactionCode());
        inputmap.put("1_POSTINGMESSAGETRANSACTIONNARRATIVE", getF_IN_Narrative());
        inputmap.put("1_POSTINGMESSAGETRANSACTIONREFERENCE", getF_IN_TXRef());
        inputmap.put("2_POSTINGMESSAGEISOCURRENCYCODE", getF_IN_ISOCurrencyCode());
        inputmap.put("3_POSTINGMESSAGETRANSACTIONREFERENCE", getF_IN_TXRef());
        inputmap.put("1_POSTINGMESSAGEACCOUNTPOSTINGACTION", "D");
        inputmap.put("2_POSTINGMESSAGEACCOUNTPOSTINGACTION", "C");
        inputmap.put("FORCEPOST", (Boolean) isF_IN_ForcePost());
        inputmap.put("MAINACCOUNTID", getF_IN_MainAccountId());
        inputmap.put("MessageNumber", getF_IN_MessageNumber());
        inputmap.put("TRANSACTIONID", getF_IN_UniqueId());
        inputmap.put("CHARGESTRANSACTIONCODE", "CHARGES_TRANSACTION_CODE");
        inputmap.put("MODULENAME", "ATM");
        try {
            HashMap output = MFExecuter.executeMF("UB_Interfaces_ATM_SPA_ChargesPosting_SRV", env, inputmap);
            String auth = output.get("AuthorisationFlag").toString().trim();
            int Authorization = 0;
            if (auth != null && auth.length() > 0) {
                Authorization = (Integer) output.get("AuthorisationFlag");
            }
            else {
                Authorization = 0;
            }
            String ErrorMessage = output.get("MESSAGE").toString();
            String ErrorStatus = output.get("ErrorStatus").toString();
            if (Authorization == 1) {
                setF_OUT_AuthorizedFlag(0);
                setF_OUT_ErrorMessage(ErrorMessage);
                setF_OUT_ErrorStatus(ATMConstants.WARNING);
            }
        }
        catch (BankFusionException e) {
            logger.error(e);
            setF_OUT_AuthorizedFlag(0);
            setF_OUT_ErrorMessage(e.getMessage());
            setF_OUT_ErrorStatus(ATMConstants.WARNING);
        }
    }

    public void commissionPostingForInquiry(BankFusionEnvironment env) {
        HashMap inputmap = new HashMap();
        inputmap.put("EXTERNAL_BRANCH_CODE", getF_IN_ExternalBranchCode());
        inputmap.put("1_POSTINGMESSAGEACCOUNTID", getF_IN_CommissionAccount());
        inputmap.put("1_POSTINGMESSAGEISOCURRENCYCODE", getF_IN_CommissionISOCurrencyCode());
        inputmap.put("1_POSTINGMESSAGETRANSACTIONAMOUNT", (BigDecimal) getF_IN_CommissionAmount());
        inputmap.put("1_POSTINGMESSAGETRANSACTIONCODE", getF_IN_MISTransactionCode());
        inputmap.put("1_POSTINGMESSAGETRANSACTIONNARRATIVE", getF_IN_Narrative());
        inputmap.put("1_POSTINGMESSAGETRANSACTIONREFERENCE", getF_IN_TXRef());
        inputmap.put("2_POSTINGMESSAGEACCOUNTID", getF_IN_ChargeCreditAccountId());
        inputmap.put("2_POSTINGMESSAGEISOCURRENCYCODE", getF_IN_CommissionISOCurrencyCode());
        inputmap.put("1_POSTINGMESSAGEACCOUNTPOSTINGACTION", "D");
        inputmap.put("2_POSTINGMESSAGEACCOUNTPOSTINGACTION", "C");
        inputmap.put("FORCEPOST", (Boolean) isF_IN_ForcePost());
        inputmap.put("MAINACCOUNTID", getF_IN_MainAccountId());
        inputmap.put("MessageNumber", getF_IN_MessageNumber());
        inputmap.put("TRANSACTIONID", getF_IN_UniqueId());
        inputmap.put("COMMISIONTRANSACTIONCODE", "COMMISSION_TRANSACTION_CODE");
        inputmap.put("FEESTRANSACTIONCODE", "FEES_TRANSACTION_CODE");
        inputmap.put("MODULENAME", "ATM");
        try {
            HashMap output = MFExecuter.executeMF("UB_Interfaces_ATM_SPA_CommissionsPosting_SRV", env, inputmap);
            String auth = output.get("AuthorisationFlag").toString().trim();
            int Authorization = 0;
            if (auth != null && auth.length() > 0) {
               // Authorization = (Integer) output.get("AuthorisationFlag");
            	Authorization =Integer.parseInt(auth);
            }
            else {
                Authorization = 0;
            }
            String ErrorMessage = output.get("Error").toString();
            String ErrorStatus = output.get("ErrorStatus").toString();
            if (Authorization == 1) {
                setF_OUT_AuthorizedFlag(Authorization);
                setF_OUT_ErrorMessage(ErrorMessage);
                setF_OUT_ErrorStatus(ErrorStatus);
            }
            else {
                setF_OUT_AuthorizedFlag(getF_IN_Authorization());
                setF_OUT_ErrorMessage(getF_IN_ErrorMessage());
                setF_OUT_ErrorStatus(getF_IN_ErrorStatus());
            }
            setF_OUT_IsChargeWaived((Boolean) output.get("IsChargeWaived"));
        }
        catch (BankFusionException e) {
            logger.error(e);
            setF_OUT_AuthorizedFlag(1);
            setF_OUT_ErrorMessage(e.getMessage());
            setF_OUT_ErrorStatus(ATMConstants.ERROR);
        }
    }

    public void accountValidations(String accountId, BankFusionEnvironment env) {
        Object[] field = new Object[] { accountId };
        IBOAttributeCollectionFeature accountItem = null;
        // check for closed or stopped accounts
        try {
            accountItem = (IBOAttributeCollectionFeature) env.getFactory().findByPrimaryKey(IBOAttributeCollectionFeature.BONAME,
                    accountId);
        }
        catch (FinderException fe) {
            populateErrorDetails(1, ATMConstants.ERROR, ChannelsEventCodes.E_INVALID_ACCOUNT, BankFusionMessages.ERROR_LEVEL,
                    field, env);
            logger.error(fe);
            return;
        }
        if (accountItem != null) {
            setF_OUT_ChargeFundingAccount(accountItem.getF_CHARGEFUNDINGACCOUNTID());
            setF_OUT_IsoCurrencyCode(accountItem.getF_ISOCURRENCYCODE());
            BusinessValidatorBean validatorBean = new BusinessValidatorBean();
            if (validatorBean.validateAccountClosed(accountItem, env)) {
                populateErrorDetails(1, ATMConstants.ERROR, CommonsEventCodes.E_ACCOUNT_CLOSED, BankFusionMessages.ERROR_LEVEL,
                        field, env);
                if (logger.isDebugEnabled()) {
                    logger.error("Account : " + accountItem.getBoID() + " is Closed !");
                }
                setF_OUT_Closed(new Boolean(true));
                return;
            }
            else if (validatorBean.validateAccountStopped(accountItem, env)) {
                if (isF_IN_ForcePost() == false) {
                    populateErrorDetails(0, ATMConstants.WARNING, CommonsEventCodes.E_ACCOUNT_STOPPED,
                            BankFusionMessages.ERROR_LEVEL, field, env);
                }
                else {
                    populateErrorDetails(0, ATMConstants.WARNING, CommonsEventCodes.E_ACCOUNT_STOPPED,
                            BankFusionMessages.ERROR_LEVEL, field, env);
                }
                logger.error("Account : " + accountItem.getBoID() + " is Stopped !");
                setF_OUT_Stopped(new Boolean(true));
                return;
            }
            ATMHelper atmHelper = new ATMHelper();
            boolean isAccountDormant = atmHelper.isAccountDormant(accountId, env);
            if (isAccountDormant) {
                String bankTransCode = atmHelper.getBankTransactionCode(getF_IN_MessageNumber(), env);
                IBOMisTransactionCodes transDetails = null;
                try {
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
                	logger.error(exception);
                }
                if(transDetails ==null){
                	logger.error("Could not find MIS transaction codes");
                } else {
                String dormancyPostingAction = transDetails.getF_DORMANCYPOSTINGACTION();
                if (!("0".equals(dormancyPostingAction) || "3".equals(dormancyPostingAction))) {
                    if (isF_IN_ForcePost() == false) {
                        setF_OUT_AuthorizedFlag(0);
                        setF_OUT_ErrorStatus(ATMConstants.WARNING);
                        setF_OUT_ErrorMessage(BankFusionMessages.getFormattedMessage(ChannelsEventCodes.E_ACCOUNT_DORMANT, field));
                    }
                    else {
                        setF_OUT_AuthorizedFlag(0);
                        setF_OUT_ErrorStatus(ATMConstants.WARNING);
                        setF_OUT_ErrorMessage(BankFusionMessages.getFormattedMessage(ChannelsEventCodes.E_ACCOUNT_DORMANT, field));
                    }
                    setF_OUT_Dormant(new Boolean(true));
                    return;
                }
            }
            }
            int rightsIndicator = accountItem.getF_ACCRIGHTSINDICATOR();
            try {
                atmHelper.validatePasswordProtection(accountItem.getBoID(), rightsIndicator,
                        PasswordProtectedConstants.OPERATION_DEBIT, env);
            }
            catch (BankFusionException exception) {
            	logger.error(exception);
                if (isF_IN_ForcePost() == false) {
                    populateErrorDetails(0, ATMConstants.WARNING, ChannelsEventCodes.E_ACCOUNT_____IS_PASSWORD_PROTECTED,
                            BankFusionMessages.ERROR_LEVEL, field, env);
                }
                else {
                    populateErrorDetails(0, ATMConstants.WARNING, ChannelsEventCodes.E_ACCOUNT_____IS_PASSWORD_PROTECTED,
                            BankFusionMessages.ERROR_LEVEL, field, env);
                }
                setF_OUT_Password(new Boolean(true));
                return;
           
            }
        }
    }

    // changes for artf46961 [start]
    public void chargePostingForMiniStatement(BankFusionEnvironment env) {
        HashMap inputmap = new HashMap();
        // inputmap.put("EXTERNAL_BRANCH_CODE", getF_IN_ExternalBranchCode());
        inputmap.put("1_POSTINGMESSAGEACCOUNTID", getF_IN_ChargeAccountId());
        inputmap.put("1_POSTINGMESSAGEISOCURRENCYCODE", getF_IN_ISOCurrencyCode());
        inputmap.put("1_POSTINGMESSAGETRANSACTIONAMOUNT", (BigDecimal) getF_IN_ChargeAmount());
        inputmap.put("1_POSTINGMESSAGETRANSACTIONCODE", getF_IN_MISTransactionCode());
        inputmap.put("1_POSTINGMESSAGETRANSACTIONNARRATIVE", getF_IN_Narrative());
        inputmap.put("1_POSTINGMESSAGETRANSACTIONREFERENCE", getF_IN_TXRef());
        inputmap.put("2_POSTINGMESSAGEISOCURRENCYCODE", getF_IN_ISOCurrencyCode());
        inputmap.put("3_POSTINGMESSAGETRANSACTIONREFERENCE", getF_IN_TXRef());
        inputmap.put("1_POSTINGMESSAGEACCOUNTPOSTINGACTION", "D");
        inputmap.put("2_POSTINGMESSAGEACCOUNTPOSTINGACTION", "C");
        inputmap.put("FORCEPOST", (Boolean) isF_IN_ForcePost());
        inputmap.put("MAINACCOUNTID", getF_IN_MainAccountId());
        inputmap.put("MessageNumber", getF_IN_MessageNumber());
        inputmap.put("TRANSACTIONID", getF_IN_UniqueId());
        inputmap.put("CHARGESTRANSACTIONCODE", "CHARGES_TRANSACTION_CODE");
        inputmap.put("MODULENAME", "ATM");
        try {
            HashMap output = MFExecuter.executeMF("UB_Interfaces_ATM_SPA_ChargesPosting_SRV", env, inputmap);
            String auth = output.get("AuthorisationFlag").toString().trim();
            int Authorization = 0;
            if (auth != null && auth.length() > 0) {
                Authorization = (Integer) output.get("AuthorisationFlag");
            }
            else {
                Authorization = 0;
            }
            String ErrorMessage = output.get("MESSAGE").toString();
            String ErrorStatus = output.get("ErrorStatus").toString();
            if (Authorization == 1) {
                setF_OUT_AuthorizedFlag(0);
                setF_OUT_ErrorMessage(ErrorMessage);
                setF_OUT_ErrorStatus(ATMConstants.WARNING);
            }
        }
        catch (BankFusionException e) {
            logger.error(e);
            setF_OUT_AuthorizedFlag(0);
            setF_OUT_ErrorMessage(e.getMessage());
            setF_OUT_ErrorStatus(ATMConstants.WARNING);
        }
    }

    public void commissionPostingForMiniStatement(BankFusionEnvironment env) {
        HashMap inputmap = new HashMap();
        // inputmap.put("EXTERNAL_BRANCH_CODE", getF_IN_ExternalBranchCode());
        inputmap.put("1_POSTINGMESSAGEACCOUNTID", getF_IN_CommissionAccount());
        inputmap.put("1_POSTINGMESSAGEISOCURRENCYCODE", getF_IN_CommissionISOCurrencyCode());
        inputmap.put("1_POSTINGMESSAGETRANSACTIONAMOUNT", (BigDecimal) getF_IN_CommissionAmount());
        inputmap.put("1_POSTINGMESSAGETRANSACTIONCODE", getF_IN_MISTransactionCode());
        inputmap.put("1_POSTINGMESSAGETRANSACTIONNARRATIVE", getF_IN_Narrative());
        inputmap.put("1_POSTINGMESSAGETRANSACTIONREFERENCE", getF_IN_TXRef());
        inputmap.put("2_POSTINGMESSAGEACCOUNTID", getF_IN_ChargeCreditAccountId());
        inputmap.put("2_POSTINGMESSAGEISOCURRENCYCODE", getF_IN_CommissionISOCurrencyCode());
        inputmap.put("1_POSTINGMESSAGEACCOUNTPOSTINGACTION", "D");
        inputmap.put("2_POSTINGMESSAGEACCOUNTPOSTINGACTION", "C");
        inputmap.put("FORCEPOST", (Boolean) isF_IN_ForcePost());
        inputmap.put("MAINACCOUNTID", getF_IN_MainAccountId());
        inputmap.put("MessageNumber", getF_IN_MessageNumber());
        inputmap.put("TRANSACTIONID", getF_IN_UniqueId());
        inputmap.put("COMMISIONTRANSACTIONCODE", "COMMISSION_TRANSACTION_CODE");
        inputmap.put("FEESTRANSACTIONCODE", "FEES_TRANSACTION_CODE");
        inputmap.put("MODULENAME", "ATM");
        try {
            HashMap output = MFExecuter.executeMF("UB_Interfaces_ATM_SPA_CommissionsPosting_SRV", env, inputmap);
            String auth = output.get("AuthorisationFlag").toString().trim();
            int Authorization = 0;
            if (auth != null && auth.length() > 0) {
                Authorization = (Integer) output.get("AuthorisationFlag");
            }
            else {
                Authorization = 0;
            }
            String ErrorMessage = output.get("Error").toString();
            String ErrorStatus = output.get("ErrorStatus").toString();
            if (Authorization == 1) {
                setF_OUT_AuthorizedFlag(Authorization);
                setF_OUT_ErrorMessage(ErrorMessage);
                setF_OUT_ErrorStatus(ErrorStatus);
            }
            else {
                setF_OUT_AuthorizedFlag(getF_IN_Authorization());
                setF_OUT_ErrorMessage(getF_IN_ErrorMessage());
                setF_OUT_ErrorStatus(getF_IN_ErrorStatus());
            }
            setF_OUT_IsChargeWaived((Boolean) output.get("IsChargeWaived"));
        }
        catch (BankFusionException e) {
            logger.error(e);
            setF_OUT_AuthorizedFlag(1);
            setF_OUT_ErrorMessage(e.getMessage());
            setF_OUT_ErrorStatus(ATMConstants.ERROR);
        }
    }

    // changes for artf46961 [end]

    private void populateErrorDetails(int authorisedFlag, String errorCode, int errorNo, String errorLevel, Object[] fields,
            BankFusionEnvironment env) {
        setF_OUT_AuthorizedFlag(authorisedFlag);
        setF_OUT_ErrorStatus(errorCode);
        setF_OUT_ErrorMessage(BankFusionMessages.getFormattedMessage(errorNo, fields));
    }
    // artf46960 Changes end
}
