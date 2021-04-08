package com.misys.ub.swift.remittance.process;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.BankFusionMessages;
import com.misys.bankfusion.paymentmessaging.controller.commons.PaymentConstants;
import com.misys.bankfusion.subsystem.microflow.runtime.impl.MFExecuter;
import com.misys.ub.interfaces.IfmConstants;
import com.misys.ub.payment.swift.utils.MFInputOutPutKeys;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOPseudonymAccountMap;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOTransactionScreenControl;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

import bf.com.misys.cbs.services.RateToleranceRq;
import bf.com.misys.cbs.services.RateToleranceRs;
import bf.com.misys.cbs.services.ReadModuleConfigurationRq;
import bf.com.misys.cbs.services.ReadModuleConfigurationRs;
import bf.com.misys.cbs.types.ModuleKeyRq;
import bf.com.misys.cbs.types.RateToleranceDetails;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;

/**
 * @author hargupta
 *
 */
public class SwiftRemittanceMessageHelper {
    private SwiftRemittanceMessageHelper() {

    }

    public static final String READ_MODULE_CONFIGURATION = "CB_CMN_ReadModuleConfiguration_SRV";
    private static final String EXCHANGE_RATE_TOLERANCE_SERVICE = "CB_FEX_CheckRateTolerance_SRV";
    private static final String MSGSTATUSRQ = "MessageStatus";
    private static final String CBS_RAISE_EVENTS = "CB_CMN_RaiseEvents_SRV";
    private static final String RATE_TOLERANCE_RQ = "RateToleranceRq";
    private static final String RATE_TOLERANCE_RS = "RateToleranceRS";
    private transient final static Log LOGGER = LogFactory.getLog(SwiftRemittanceMessageHelper.class);
    /**
     * @param env
     * @param transferCurrency
     * @param creditCurrency
     * @param pseudoname
     * @return
     */
    public static String getNostroAcc(BankFusionEnvironment env, String transferCurrency, String creditCurrency,
            String pseudoname) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN getNostroAcc");
        
        String branchSortCode = readModuleConfiguration("DFLNOSTRO", IfmConstants.MODULE_NAME,
                BankFusionThreadLocal.getBankFusionEnvironment());
        String creditAccountNumber = CommonConstants.EMPTY_STRING;
        List<SimplePersistentObject> dbRows = findAccountByPseudonameAndContext(env, "BRANCH", branchSortCode, creditCurrency,
                pseudoname);
        if (dbRows != null && dbRows.size() > 0) {
            creditAccountNumber = dbRows.get(0).getDataMap().get("f_ACCOUNTID").toString();
        }
        else {
            dbRows = findAccountByPseudonameAndContext(env, "CURRENCY", transferCurrency, creditCurrency, pseudoname);
            if (dbRows != null && dbRows.size() > 0) {
                creditAccountNumber = dbRows.get(0).getDataMap().get("f_ACCOUNTID").toString();
            }
        }
        return creditAccountNumber;
    }

    /**
     * @param env
     * @param sortContext
     * @param sortContextValue
     * @param isoCurrencyCode
     * @param pseudoname
     * @return
     */
    public static List<SimplePersistentObject> findAccountByPseudonameAndContext(BankFusionEnvironment env, String sortContext,
            String sortContextValue, String isoCurrencyCode, String pseudoname) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN findAccountByPseudonameAndContext");
        
        ArrayList params = new ArrayList();
        params.add(false);
        params.add(pseudoname);
        params.add(sortContext);
        params.add(sortContextValue);
        params.add(isoCurrencyCode);
        final String findAccountByPseudonameCurrencyAndContext = " WHERE " + IBOPseudonymAccountMap.ISDELETED + " = ? AND "
                + IBOPseudonymAccountMap.PSEUDONAME + " = ? AND " + IBOPseudonymAccountMap.SORTCONTEXT + " = ? AND "
                + IBOPseudonymAccountMap.SORTCONTEXTVALUE + " = ? AND " + IBOPseudonymAccountMap.ISOCURRENCYCODE + " = ?";
        List<SimplePersistentObject> dbRows = env.getFactory().findByQuery(IBOPseudonymAccountMap.BONAME,
                findAccountByPseudonameCurrencyAndContext, params, null, false);
        return dbRows;
    }

    /**
     * @param transactionType
     * @return
     */
    public static String getTransactioncodeFromModuleConfig(String transactionType) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN getTransactioncodeFromModuleConfig");
        
        String internationalContext = null;
        if (transactionType.contains("103")) {
            internationalContext = "DEFAULT_MIS_TXNCODE_OUT_MT103";
        }
        else if (transactionType.contains("202")) {
            internationalContext = "DEFAULT_MIS_TXNCODE_OUT_MT202";
        }
        else if (transactionType.contains("205")) {
            internationalContext = "DEFAULT_MIS_TXNCODE_OUT_MT205";
        }
        else if (transactionType.contains("200")) {
            internationalContext = "DEFAULT_MIS_TXNCODE_OUT_MT200";
        } // Read from PaymentSwiftUtils
        return readModuleConfiguration(internationalContext, "SWIFT", BankFusionThreadLocal.getBankFusionEnvironment());
    }

    /**
     * @param transactionCode
     * @return
     */
    public static IBOMisTransactionCodes getMisTransactionCodes(String transactionCode) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN getMisTransactionCodes");
        
        IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
        return (IBOMisTransactionCodes) factory.findByPrimaryKey(IBOMisTransactionCodes.BONAME, transactionCode, true);
    }

    /**
     * @param transactionCode
     * @return
     */
    public static IBOTransactionScreenControl getTransactionScreenControl(String transactionCode) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN getTransactionScreenControl");
        
        IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
        return (IBOTransactionScreenControl) factory.findByPrimaryKey(IBOTransactionScreenControl.BONAME, transactionCode, true);
    }

    /**
     * @param customerCode
     * @return
     */
    public static IBOSwtCustomerDetail getSwiftCustomerDetails(String customerCode) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN getSwiftCustomerDetails");
        
        IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
        return (IBOSwtCustomerDetail) factory.findByPrimaryKey(IBOSwtCustomerDetail.BONAME, customerCode, true);
    }

    /**
     * @param key
     * @param moduleId
     * @param env
     * @return
     */
    public static String readModuleConfiguration(String key, String moduleId, BankFusionEnvironment env) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN readModuleConfiguration");
        
        ReadModuleConfigurationRq readModuleConfigRq = new ReadModuleConfigurationRq();
        ReadModuleConfigurationRs readModuleConfigRs = new ReadModuleConfigurationRs();
        ModuleKeyRq moduleKeyRq = new ModuleKeyRq();
        moduleKeyRq.setKey(key);
        moduleKeyRq.setModuleId(moduleId);
        readModuleConfigRq.setModuleKeyRq(moduleKeyRq);
        HashMap inputParam = new HashMap();
        inputParam.put("ReadModuleConfigurationRq", readModuleConfigRq);
        HashMap outputParam = MFExecuter.executeMF(READ_MODULE_CONFIGURATION, env, inputParam);
        readModuleConfigRs = (ReadModuleConfigurationRs) (outputParam.get("ReadModuleConfigurationRs"));
        return readModuleConfigRs.getModuleConfigDetails().getValue();
    }

    /**
     * @param exchangeRate
     * @param fromCurrency
     * @param toCurrency
     * @param exchangeRateType
     * @param env
     */
    public static RsHeader checkExchangeRateTolerance(BigDecimal exchangeRate, String fromCurrency, String toCurrency,
            String exchangeRateType, BankFusionEnvironment env) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("IN checkExchangeRateTolerance");
        RsHeader rsHeader = new RsHeader();
        MessageStatus status = new MessageStatus();
        status.setOverallStatus(PaymentSwiftConstants.OVERALL_SUCCESS_STATUS);

        HashMap<String, Object> inputParams = new HashMap<String, Object>();
        RateToleranceRq rateToleranceRq = new RateToleranceRq();
        RateToleranceDetails rateToleranceDetails = new RateToleranceDetails();
        rateToleranceDetails.setExchangeRate(exchangeRate);
        rateToleranceDetails.setFromCurrency(fromCurrency);
        rateToleranceDetails.setToCurrency(toCurrency);
        rateToleranceDetails.setExchangeRateType(exchangeRateType);
        rateToleranceDetails.setHostExtension(null);
        rateToleranceDetails.setUserExtension(null);
        rateToleranceRq.setRqHeader(PaymentSwiftUtils.rqHeaderInput());
        rateToleranceRq.setRateToleranceDetails(rateToleranceDetails);
        inputParams.put(RATE_TOLERANCE_RQ, rateToleranceRq);
        Map<String, Object> outputParams = MFExecuter.executeMF(EXCHANGE_RATE_TOLERANCE_SERVICE, env, inputParams);
        if (outputParams != null) {
            RateToleranceRs toleranceResponse = (RateToleranceRs) outputParams.get(RATE_TOLERANCE_RS);
            if (toleranceResponse != null && toleranceResponse.getRsHeader().getStatus().getOverallStatus() != null
                    && toleranceResponse.getRsHeader().getStatus().getOverallStatus().equals(PaymentConstants.ERROR_STATUS)) {
                HashMap<String, Object> errorParams = new HashMap<String, Object>();

                status = toleranceResponse.getRsHeader().getStatus();
                rsHeader.setStatus(status);
            }
        }
        rsHeader.setStatus(status);
        return rsHeader;
    }
    
    
    /**
     * @param debitAccountId
     * @param debitAccountCcy
     * @param debitTxnCode
     * @param debitExchangeRate
     * @param paymentCcy
     * @param env
     * @return
     */
    public static HashMap fetchOnlinecharges(String debitAccountId, String debitAccountCcy, String debitTxnCode, BigDecimal txnAmount,
            BankFusionEnvironment env) {
        HashMap inputParams = new HashMap();
        inputParams.put(MFInputOutPutKeys.UB_CHG_FUNDINGACCOUNT, debitAccountId);
        inputParams.put(MFInputOutPutKeys.postingMessageAccountId_1, debitAccountId);
        inputParams.put(MFInputOutPutKeys.postingMessageISOCurrencyCode_1, debitAccountCcy);
        inputParams.put(MFInputOutPutKeys.postingMessageTransactionAmount_1, txnAmount);
        inputParams.put(MFInputOutPutKeys.UB_CHG_TxnCurrency, debitAccountCcy);
        inputParams.put(MFInputOutPutKeys.postingMessageTransactionCode_1, debitTxnCode);
        HashMap outputParams = MFExecuter.executeMF(MFInputOutPutKeys.UB_CHG_CalculateOnlineCharges_SRV,
                BankFusionThreadLocal.getBankFusionEnvironment(), inputParams);
        return outputParams;
    }
    
}
