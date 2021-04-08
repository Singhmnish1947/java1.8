package com.misys.ub.fatoms;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.runtime.util.CurrencyFormatter;
import com.misys.bankfusion.common.util.BankFusionPropertySupport;
import com.misys.bankfusion.subsystem.microflow.runtime.impl.MFExecuter;
import com.misys.fbe.accountalerts.common.AccountAlertConstants;
import com.misys.fbe.common.constant.FeatureConstants;
import com.misys.fbe.common.util.CommonUtil;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOFinancialPostingMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.core.BFCurrencyValue;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractAlertMessageFormatter;

import bf.com.misys.cbs.msgs.v1r0.CreateAlertMessageRq;
import bf.com.misys.cbs.services.ReadCodeValueRq;
import bf.com.misys.cbs.services.ReadCodeValueRs;
import bf.com.misys.cbs.types.InputReadCodeValueRq;

/**
 * @author Surya Maroju
 * @date 25-Jul-17
 * @Description: AlertMessageFormatter will format the alert messages based on the parameters
 */
@SuppressWarnings({ "deprecation", "rawtypes", "unchecked" })
public class AlertMessageFormatter extends AbstractAlertMessageFormatter {

    private static final long serialVersionUID = -2400941255360892122L;

    private static final String READ_GENERICCODE_LOOKUPSRV = "CB_GCD_ReadGenericCodes_SRV";

    private static final String READ_GENERICCODE_REQUEST = "readCodeValueRq";

    private static final String READ_GENERICCODE_RESPONSE = "readCodeValueRs";

    private static final Log LOGGER = LogFactory.getLog(AlertMessageFormatter.class.getName());

    private static final String AVAILABLE_BALANCE = "AVAILABLEBALANCE";

    public AlertMessageFormatter(BankFusionEnvironment env) {
        super(env);
    }

    public void process(BankFusionEnvironment env) {
        CreateAlertMessageRq inputRq = getF_IN_CreateAlertMessageRq();
        String alertMsg = getFinalMessage(inputRq);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("AlertMessageFormatter: " + alertMsg);
        }
        setF_OUT_Message(alertMsg);
    }

    private String getCodeDescription(String codeType, String subCodeType) {
        HashMap inputParams = new HashMap();
        String codeDescription = CommonConstants.EMPTY_STRING;
        ReadCodeValueRq readCodeValueRq = new ReadCodeValueRq();
        InputReadCodeValueRq inputReadCodeValueRq = new InputReadCodeValueRq();
        inputReadCodeValueRq.setCbReference(codeType);
        inputReadCodeValueRq.setCodeReference(subCodeType);
        readCodeValueRq.setInputReadCodeValueRq(inputReadCodeValueRq);
        inputParams.put(READ_GENERICCODE_REQUEST, readCodeValueRq);
        HashMap outputParams = MFExecuter.executeMF(READ_GENERICCODE_LOOKUPSRV, BankFusionThreadLocal.getBankFusionEnvironment(),
                inputParams);
        ReadCodeValueRs readCodeValueRs = (ReadCodeValueRs) outputParams.get(READ_GENERICCODE_RESPONSE);
        codeDescription = readCodeValueRs.getReadCodeValueDetails().getGcCodeDetails().getCodeDescription();
        return codeDescription;
    }

    private String getFinalMessage(CreateAlertMessageRq inputRq) {
        IBOAttributeCollectionFeature accountInfo = null;
        String availableBalTag = CommonConstants.EMPTY_STRING;
        String bookBalanceTag = CommonConstants.EMPTY_STRING;
        String clearedBalanceTag = CommonConstants.EMPTY_STRING;
        String accountCurrencyCode = CommonConstants.EMPTY_STRING;
        String transactionref = CommonConstants.EMPTY_STRING;
        String transactionDate = CommonConstants.EMPTY_STRING;
        String transactionTime = CommonConstants.EMPTY_STRING;
        String transactionAmountTag = CommonConstants.EMPTY_STRING;
        String transactionSRID = CommonConstants.EMPTY_STRING;

        accountInfo = (IBOAttributeCollectionFeature) BankFusionThreadLocal.getPersistanceFactory().findByPrimaryKey(
                IBOAttributeCollectionFeature.BONAME, inputRq.getAlertMsgRequest().getAlertKeys().getAlertEntityId().toString(),
                true);
        accountCurrencyCode = accountInfo.getF_ISOCURRENCYCODE();
        HashMap inputParam = new HashMap();
        HashMap alertMsgDetails = (HashMap) inputRq.getAlertMsgRequest().getAlertMsgDetails();
        inputParam.put("AccountAlertId", inputRq.getAlertMsgRequest().getAlertKeys().getAlertId());
        HashMap outputParam = MFExecuter.executeMF("CB_ALR_GetAccountAlertConfigs_SRV",
                BankFusionThreadLocal.getBankFusionEnvironment(), inputParam);
        VectorTable results = (VectorTable) outputParam.get("Result");
        String thresholdAmount = (String) results.getRowTagsAsFields(0).get("f_THRESHOLDAMOUNT").toString();
        String currencyCode = (String) results.getRowTagsAsFields(0).get("f_THRESHOLDCURRENCY");
        String eventNumber = (String) results.getRowTagsAsFields(0).get("f_EVENTNUMBER");
        String alertType = (String) results.getRowTagsAsFields(0).get("f_ALERTTYPE");
        BFCurrencyValue thresholdAmt = new BFCurrencyValue(currencyCode, thresholdAmount, BankFusionThreadLocal.getUserId());
        Date txnDate;
        SimpleDateFormat dateFormatter = new SimpleDateFormat(
                BankFusionPropertySupport.getProperty("ALERT_DATE_FORMAT", "dd-MMM-yyyy"));
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

        if (alertType.equals(AccountAlertConstants.RUNTIME_ALERT_TYPE)) {
            BFCurrencyValue txnAmt = new BFCurrencyValue(accountCurrencyCode, inputRq.getAlertMsgRequest().getTransactionAmount(),
                    BankFusionThreadLocal.getUserId());
            transactionAmountTag = getFormattedCurrencyAmount(txnAmt, currencyCode);
            transactionref = inputRq.getAlertMsgRequest().getTransactionReference();
            transactionSRID = inputRq.getAlertMsgRequest().getTransactionSRID();

            boolean isFwdValued = false;
            boolean isFwdValueIntoValue = false;
            if (alertMsgDetails.get(FeatureConstants.FORWARD_VALUED_TXN) != null) {
                isFwdValued = (Boolean) alertMsgDetails.get(FeatureConstants.FORWARD_VALUED_TXN);
            }

            if (alertMsgDetails.get(FeatureConstants.FORWARD_VALUED_INTO_VALUE_TXN) != null) {
                isFwdValueIntoValue = (Boolean) alertMsgDetails.get(FeatureConstants.FORWARD_VALUED_INTO_VALUE_TXN);
            }

            if (isFwdValued && !isFwdValueIntoValue) {
                IBOFinancialPostingMessage finMsg = (IBOFinancialPostingMessage) BankFusionThreadLocal.getPersistanceFactory()
                        .findByPrimaryKey(IBOFinancialPostingMessage.BONAME, transactionSRID, false);
                txnDate = finMsg.getTransactionDate();
            }
            else {
                IBOTransaction transaction = (IBOTransaction) BankFusionThreadLocal.getPersistanceFactory()
                        .findByPrimaryKey(IBOTransaction.BONAME, transactionSRID, false);
                txnDate = transaction.getF_TRANSACTIONDATE();

            }
            transactionDate = dateFormatter.format(txnDate);
            transactionTime = timeFormatter.format(txnDate);
            BFCurrencyValue bookBalance = new BFCurrencyValue(accountCurrencyCode, inputRq.getAlertMsgRequest().getBookBalance(),
                    BankFusionThreadLocal.getUserId());
            BFCurrencyValue clearedBalance = new BFCurrencyValue(accountCurrencyCode,
                    inputRq.getAlertMsgRequest().getClearedBalance(), BankFusionThreadLocal.getUserId());
            bookBalanceTag = getFormattedCurrencyAmount(bookBalance, currencyCode);
            clearedBalanceTag = getFormattedCurrencyAmount(clearedBalance, currencyCode);
            if (alertMsgDetails.get(AVAILABLE_BALANCE) != null) {
                BFCurrencyValue availableBalance = new BFCurrencyValue(accountCurrencyCode,
                        alertMsgDetails.get(AVAILABLE_BALANCE).toString(), BankFusionThreadLocal.getUserId());
                availableBalTag = getFormattedCurrencyAmount(availableBalance, currencyCode);
            }
        }
        else {
            txnDate = SystemInformationManager.getInstance().getBFBusinessDate();
            transactionDate = dateFormatter.format(txnDate);
            bookBalanceTag = getFormattedCurrencyAmount(accountInfo.getF_BOOKEDBALANCE(), accountCurrencyCode);
            clearedBalanceTag = getFormattedCurrencyAmount(accountInfo.getF_CLEAREDBALANCE(), accountCurrencyCode);
            availableBalTag = CommonConstants.EMPTY_STRING;
        }
        String A = accountCurrencyCode;// 0
        String B = transactionAmountTag;// 1
        String C = bookBalanceTag;// 2
        String D = availableBalTag; // CommonConstants.EMPTY_STRING;// 3 availableBalance
        String E = clearedBalanceTag;// 4
        String F = transactionDate;// 5
        String G = transactionTime;// 6
        String H = accountInfo.getBoID();// UnMasked Account Number 7
        String I = getFormattedCurrencyAmount(thresholdAmt, currencyCode);// 8
        String J = currencyCode;// 9
        String K = CommonConstants.EMPTY_STRING;// 10 - Time with just hour and minutes
        if (CommonUtil.checkIfNotNullOrEmpty(G)) {
            K = G.substring(0, Math.min(G.length(), 6));
        }
        int startLength = getProperty("ALERT_UNMASK_STARTLEN", "5");
        int endLength = getProperty("ALERT_UNMASK_ENDLEN", "2");
        String L = maskAccountnumber(accountInfo.getBoID(), startLength, endLength, 'x');// 11

        String M = transactionref; // 12
        String N = transactionSRID; // 13 SRID

        return MessageFormat.format(getCodeDescription("ALERTMSG", eventNumber),
                new Object[] { A, B, C, D, E, F, G, H, I, J, K, L, M, N });
    }

    private static String maskAccountnumber(String strText, int start, int end, char maskChar) {
        if (strText == null || strText.equals("")) {
            return "";
        }

        if (start < 0) {
            start = 0;
        }

        if (end > strText.length()) {
            end = strText.length();
        }

        int maskLength = strText.length() - (end + start);

        if (maskLength == 0) {
            return strText;
        }

        StringBuilder sbMaskString = new StringBuilder(maskLength);

        for (int i = 0; i < maskLength; i++) {
            sbMaskString.append(maskChar);
        }

        return strText.substring(0, start) + sbMaskString.toString() + strText.substring(start + maskLength);
    }

    private int getProperty(String key, String defaultValue) {
        int propertyValue = 0;
        propertyValue = Integer.parseInt(BankFusionPropertySupport.getProperty(key, defaultValue));
        return propertyValue;
    }

    private String getFormattedCurrencyAmount(BigDecimal amount, String ccyCode) {
        return CurrencyFormatter.convertCurrencyForLocale(amount, ccyCode, BankFusionThreadLocal.getUserSession().getUserLocale());
    }

}
