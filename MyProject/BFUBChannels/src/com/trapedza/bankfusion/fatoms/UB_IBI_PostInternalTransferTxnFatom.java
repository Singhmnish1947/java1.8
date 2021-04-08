package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.compliance.types.PaymentsComplianceCheckRequest;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.microflow.runtime.exception.MicroflowIsParkedException;
import com.misys.bankfusion.subsystem.task.runtime.eventcode.TasksEventCodes;
import com.misys.ub.compliance.postings.InternetBankingComplianceCheckHelper;
import com.misys.ub.forex.configuration.ForexModuleConfiguration;
import com.misys.ub.interfaces.IfmConstants;
import com.misys.ub.interfaces.UB_IBI_PaymentsHelper;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.events.ErrorEvent;
import com.trapedza.bankfusion.exceptions.CollectedEventsDialogException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.servercommon.services.autonumber.IAutoNumberService;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_IBI_PostInternalTransferTxnFatom;
import com.trapedza.bankfusion.utils.GUIDGen;

public class UB_IBI_PostInternalTransferTxnFatom extends AbstractUB_IBI_PostInternalTransferTxnFatom {
    private transient final static Log logger = LogFactory.getLog(UB_IBI_PostInternalTransferTxnFatom.class.getName());
    private String creditAccountNumber = CommonConstants.EMPTY_STRING;
    private boolean error = false;
    private boolean collectedReferral = false;
    private boolean referral = false;
    public static final String UPDATE_CHARGES_MFID = "UB_CHG_UpdateChargeHistory_SRV";
    public static final String TRANSACTIONWHERECLAUSE = " WHERE " + IBOTransaction.REFERENCE + " = ?";

    public UB_IBI_PostInternalTransferTxnFatom(BankFusionEnvironment env) {
        super(env);
    }

    public void process(BankFusionEnvironment env) {
        try {
            String chgFundingAccountCurrency = CommonConstants.EMPTY_STRING;
            String txnCurrency = getF_IN_DEBITCURRENCY();
            String ifmId = getF_IN_IFMID();
            String txnReference = CommonConstants.EMPTY_STRING;
            String debitCurrency = CommonConstants.EMPTY_STRING;
            String debitAccBranchSortCode = CommonConstants.EMPTY_STRING;
            String creditAccBranchSortCode = CommonConstants.EMPTY_STRING;
            BigDecimal txmAmtInCreditAccCurrency = CommonConstants.BIGDECIMAL_ZERO;
            BigDecimal txnAmount = new BigDecimal(getF_IN_TRANSACTIONAMOUNT());
            BigDecimal debitAmount = CommonConstants.BIGDECIMAL_ZERO;
            IAutoNumberService autoNumService = (IAutoNumberService) ServiceManager.getService(ServiceManager.AUTO_NUMBER_SERVICE);
            Map debitAccDetails = null;
            VectorTable vector = getF_IN_CHARGEVECTOR();
            String spotPseudonym = ForexModuleConfiguration.getSpotPositionPseudonym();
            IBusinessInformation bizInfo = ((IBusinessInformationService) ServiceManagerFactory.getInstance().getServiceManager()
                    .getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();
            String positionAccountContext = (String) bizInfo.getModuleConfigurationValue(IfmConstants.SYS_MODULE_CONFIG_KEY,
                    IfmConstants.SYS_POSITION_CONTEXT, null);
            String chgFundingAccount = CommonConstants.EMPTY_STRING;
            String chgRecievingAccountCurrency = CommonConstants.EMPTY_STRING;

            /*
             * Fetching debit account details
             */
            debitAccDetails = UB_IBI_PaymentsHelper.getAccountDetails(getF_IN_DEBITACCOUNT());
            debitCurrency = (String) debitAccDetails.get("ISOCURRENCYCODE"); // Debit
            // account
            debitAccBranchSortCode = (String) debitAccDetails.get("BRANCHSORTCODE");
            chgFundingAccount = (String) debitAccDetails.get("CHARGEFUNDINGACCOUNT");

            /*
             * Finding charge funding account and charge funding account currency
             */
            if (chgFundingAccount == null || chgFundingAccount.equals(CommonConstants.EMPTY_STRING)) {
                chgFundingAccount = getF_IN_DEBITACCOUNT();
                chgFundingAccountCurrency = debitCurrency;
            }
            else {
                chgFundingAccountCurrency = (String) UB_IBI_PaymentsHelper.getAccountDetails(chgFundingAccount).get(
                        "ISOCURRENCYCODE");

            }

            creditAccBranchSortCode = (String) UB_IBI_PaymentsHelper.getAccountDetails(getF_IN_CREDITACCOUNT()).get(
                    "BRANCHSORTCODE");

            /*
             * Validating Value date
             */
            if (!UB_IBI_PaymentsHelper.validateDate(getF_IN_VALUEDATE())) {
                // TODO Send NAK message
                if (logger.isInfoEnabled())
                    logger.info("The Value date is Invalid");
                setF_OUT_ERRORMESSAGE(IfmConstants.NAKVALUEDATEINV);
                return;
            }

            /*
             * Validating transaction amount
             */

            if (!UB_IBI_PaymentsHelper.validateAmount(getF_IN_TRANSACTIONAMOUNT())) {
                setF_OUT_ERRORMESSAGE("NAKAMOUNTINV");
                return;
            }

            creditAccountNumber = getF_IN_CREDITACCOUNT();

            /*
             * Validating debit account, its should not be closed/dormant/password protected
             */
            if ((UB_IBI_PaymentsHelper.validateAccount(getF_IN_DEBITACCOUNT(), IfmConstants.DR)
                    .equalsIgnoreCase(IfmConstants.NAKDRACCTINV))
                    || UB_IBI_PaymentsHelper.isAccountDormant(getF_IN_DEBITACCOUNT(), getF_IN_DEBITTRANSACTIONCODE())) {
                setF_OUT_ERRORMESSAGE(IfmConstants.NAKDRACCTINV);
                return;
            }

            /*
             * Validating credit account, its should not be closed/dormant/password protected
             */
            if ((UB_IBI_PaymentsHelper.validateAccount(creditAccountNumber, IfmConstants.CR)
                    .equalsIgnoreCase(IfmConstants.NAKCRACCTINV))
                    || UB_IBI_PaymentsHelper.isAccountDormant(getF_IN_CREDITACCOUNT(), getF_IN_CREDITTRANSACTIONCODE())) {
                setF_OUT_ERRORMESSAGE(IfmConstants.NAKCRACCTINV);
                return;
            }

            /*
             * Validating charge funding account if charge is applicable and charge funding account
             * is not same as main account
             */
            if (vector.size() > 0) {
                if (!(chgFundingAccount.equalsIgnoreCase(getF_IN_DEBITACCOUNT()))) {
                    if ((UB_IBI_PaymentsHelper.validateAccount(chgFundingAccount, IfmConstants.DR)
                            .equalsIgnoreCase(IfmConstants.NAKDRACCTINV))
                            || UB_IBI_PaymentsHelper.isAccountDormant(chgFundingAccount, getF_IN_DEBITTRANSACTIONCODE())) {
                        setF_OUT_ERRORMESSAGE(IfmConstants.NAKDRACCTINV);
                        return;
                    }
                }
            }

            /*
             * Cross currency stuff between transaction currency and debit account currency
             */
            if (!(debitCurrency.equalsIgnoreCase(txnCurrency))) {
                if (UB_IBI_PaymentsHelper.getExchangeRate(getF_IN_EXCHANGERATETYPE(), txnCurrency, debitCurrency, new BigDecimal(
                        getF_IN_TRANSACTIONAMOUNT())) == null) {
                    setF_OUT_ERRORMESSAGE("NAKNORATES");
                    return;
                }
                debitAmount = UB_IBI_PaymentsHelper.currencyConversion(txnCurrency, debitCurrency, txnAmount,
                        getF_IN_EXCHANGERATETYPE(), true);

            }
            else {
                debitAmount = txnAmount;
            }

            /*
             * Cross currency stuff between transaction currency and credit account currency
             */
            if (txnCurrency.equalsIgnoreCase(getF_IN_CREDITCURRENCY())) {
                txmAmtInCreditAccCurrency = txnAmount;
            }
            else {
                if (UB_IBI_PaymentsHelper.getExchangeRate(getF_IN_EXCHANGERATETYPE(), txnCurrency, getF_IN_CREDITCURRENCY(),
                        new BigDecimal(getF_IN_TRANSACTIONAMOUNT())) == null) {
                    setF_OUT_ERRORMESSAGE("NAKNORATES");
                    return;
                }
                txmAmtInCreditAccCurrency = UB_IBI_PaymentsHelper.currencyConversion(txnCurrency, getF_IN_CREDITCURRENCY(),
                        txnAmount, getF_IN_EXCHANGERATETYPE(), true);
            }

            /*
             * Available balance check for debit account and charge funding account
             */
            if (!(UB_IBI_PaymentsHelper.availableBalanceCheck(vector, debitAmount, getF_IN_DEBITACCOUNT(), chgFundingAccount))) {
                setF_OUT_ERRORMESSAGE(IfmConstants.NAKNOFUNDS);
                return;
            }

            /*
             * Checking the availability of position accounts
             */
            if (!(debitCurrency.equalsIgnoreCase(getF_IN_CREDITCURRENCY()))) {

                if (UB_IBI_PaymentsHelper.arePositionAccountsAvailable(debitCurrency, getF_IN_CREDITCURRENCY(), spotPseudonym,
                        positionAccountContext, debitAccBranchSortCode, creditAccBranchSortCode) == false) {
                    setF_OUT_ERRORMESSAGE("NAK");
                    return;
                }
            }

            IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory.getInstance()
                    .getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
            Boolean checkRequiredForFirco = (Boolean) ((IBusinessInformation) ubInformationService.getBizInfo())
                    .getModuleConfigurationValue(IfmConstants.MODULE_KYC, IfmConstants.IBI_PAYMENTS_PARAM, env);

            if (checkRequiredForFirco) {
                InternetBankingComplianceCheckHelper mpfpPostCheck = new InternetBankingComplianceCheckHelper();
                PaymentsComplianceCheckRequest paymentsComplianceCheckRequest = new PaymentsComplianceCheckRequest();
                mpfpPostCheck.checkComplianceForPostingDetails(getF_IN_DEBITACCOUNT(), debitAmount, debitCurrency, false,
                        paymentsComplianceCheckRequest);
            }
            /*
             * mpfpPostCheck.checkComplianceForPostingDetails(creditAccountNumber ,
             * txmAmtInCreditAccCurrency, getF_IN_CREDITCURRENCY(), false, new
             * PaymentsComplianceCheckRequest());
             */

            txnReference = ifmId + ":" + autoNumService.getNextAutoRef(getF_IN_DEBITTRANSACTIONCODE(), debitAccBranchSortCode);
            String transactionID = GUIDGen.getNewGUID(); /*
                                                          * Main payment transfer transaction
                                                          */
            UB_IBI_PaymentsHelper.posting(getF_IN_DEBITTRANSACTIONCODE(), getF_IN_EXCHANGERATETYPE(), creditAccountNumber,
                    debitAmount, getF_IN_CREDITTRANSACTIONCODE(), getF_IN_DEBITACCOUNT(), getF_IN_DEBITTRANSACTIONNARRATIVE(),
                    getF_IN_CREDITTRANSACTIONNARRATIVE(), debitCurrency, getF_IN_CREDITCURRENCY(), txmAmtInCreditAccCurrency,
                    txnReference, transactionID);

            /*
             * Loop for online charges and taxes
             */

            for (int i = 0; i < vector.size(); i++) {
                HashMap map = vector.getRowTags(i);

                chgRecievingAccountCurrency = (String) map.get("CHARGECURRENCY");

                if (!(chgFundingAccountCurrency.equalsIgnoreCase(chgRecievingAccountCurrency))) {
                    if (UB_IBI_PaymentsHelper.arePositionAccountsAvailable(chgFundingAccountCurrency, chgRecievingAccountCurrency,
                            spotPseudonym, positionAccountContext, debitAccBranchSortCode, creditAccBranchSortCode) == false) {
                        setF_OUT_ERRORMESSAGE("NAK");
                        return;
                    }
                }

                UB_IBI_PaymentsHelper.posting((String) map.get(IfmConstants.CHARGEPOSTINGTXNCODE),
                        (String) map.get(IfmConstants.CHARGEEXCHANGERATETYPE),
                        (String) map.get(IfmConstants.CHARGERECIEVINGACCOUNT),
                        (BigDecimal) map.get(IfmConstants.CHARGEAMOUNT_IN_FUND_ACC_CURRENCY),
                        (String) map.get(IfmConstants.CHARGEPOSTINGTXNCODE), chgFundingAccount,
                        (String) map.get(IfmConstants.CHARGENARRATIVE), (String) map.get(IfmConstants.CHARGENARRATIVE),
                        chgFundingAccountCurrency, (String) map.get(IfmConstants.CHARGECURRENCY),
                        (BigDecimal) map.get(IfmConstants.CHARGEAMOUNT), txnReference, transactionID);

                if (error)
                    return;

                UB_IBI_PaymentsHelper.posting((String) map.get(IfmConstants.TAXPOSTINGTXNCODE),
                        (String) map.get(IfmConstants.TAXEXCHANGERATETYPE), (String) map.get(IfmConstants.TAXRECIEVINGACCOUNT),
                        (BigDecimal) map.get(IfmConstants.TAXAMOUNT), (String) map.get(IfmConstants.TAXPOSTINGTXNCODE),
                        chgFundingAccount, (String) map.get(IfmConstants.TAXNARRATIVE),
                        (String) map.get(IfmConstants.TAXNARRATIVE), chgFundingAccountCurrency,
                        (String) map.get(IfmConstants.TAXCURRENCY), (BigDecimal) map.get(IfmConstants.TAXAMOUNT), txnReference,
                        transactionID);
            }
            HashMap<String, Object> MfParams = new HashMap<String, Object>();
            MfParams.put("ChargeDetailsVector", getF_IN_CHARGEVECTOR());
            MfParams.put("AccountId", getF_IN_DEBITACCOUNT());
            MfParams.put("TransactionId", transactionID);
            MfParams.put("CHARGEFUNDINGACCOUNTID", chgFundingAccount);
            MfParams.put("CHARGEINDICATOR", CommonConstants.INTEGER_ZERO.intValue());
            MfParams.put("TRANSACTIONCODE", getF_IN_DEBITTRANSACTIONCODE());

            MFExecuter.executeMF(UPDATE_CHARGES_MFID, env, MfParams);
            if (error) {
                return;
            }
        }
        catch (CollectedEventsDialogException collectedEventsDialogException) {
            List<ErrorEvent> errors = collectedEventsDialogException.getErrors();
            error = true;
            setF_OUT_ERRORMESSAGE("NAK");
            for (ErrorEvent runTimeError : errors) {
                if (runTimeError.getEventNumber() == TasksEventCodes.REFERRAL_WAS_REJECTED) {
                    error = false;
                    collectedReferral = true;
                    throw collectedEventsDialogException;
                }
            }
        }
        catch (BankFusionException e) {
            error = true;
            setF_OUT_ERRORMESSAGE("NAK");
            logger.error(ExceptionUtil.getExceptionAsString(e));
        } catch (MicroflowIsParkedException microflowIsParkedException) {
        	referral = true;
        	throw microflowIsParkedException;
        }
        catch (Exception exception) {
            error = true;
            setF_OUT_ERRORMESSAGE("NAK");
        }
        finally {
            if (error) {
                BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
            } else if(!collectedReferral && !referral){
                BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
            }
            BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
            error = false;
        }
    }
}
