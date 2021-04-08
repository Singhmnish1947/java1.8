/**
 * 
 */
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.ub.types.UB_IBI_Message;
import bf.com.misys.ub.types.UB_IBI_MultiFundingAccount;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.core.BFCurrencyValue;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.events.IEvent;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.management.gateway.interfaces.IServerBusinessInfo;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_IBI_GetFundingAccountInfo;

/**
 * @author Vipul.Sharma
 * 
 */
@SuppressWarnings("serial")
public class FundingAccountTypeToVector extends AbstractUB_IBI_GetFundingAccountInfo {

    // private BankFusionEnvironment environment;
    private static final String FUNDING_ACCOUNT_ID = "FUNDINGACCOUNTID";
    private static final String FUNDING_AMOUNT = "FUNDINGAMOUNT";
    private static final String FUNDING_CURRENCYCODE = "ISOCURRENCYCODE";
    private static final String FUNDING_EXCHANGERATE = "FUNDINGEXCHANGERATE";
    private static final String CONTRA_ACCOUNT = "CONTRAACCOUNT";
    private static final String TransferMode = "TRF";
    private static final String exchangeRateType = "SPOT";
    private static final String TransferType = "CUSTOMERAC";
    private BigDecimal calcAmount = CommonConstants.BIGDECIMAL_ZERO;
    private String accNum = CommonConstants.EMPTY_STRING;
    private BankFusionEnvironment env = null;
    
    IBusinessInformation ubBusinessInfo = ((IBusinessInformationService) ServiceManagerFactory.getInstance()
            .getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();

    private transient final static Log logger = LogFactory.getLog(FundingAccountTypeToVector.class.getName());

    @SuppressWarnings("deprecation")
    public FundingAccountTypeToVector(BankFusionEnvironment env) {
        super(env);
    }

    public void process(BankFusionEnvironment env) {
        // this.environment = env;
    	env = BankFusionThreadLocal.getBankFusionEnvironment();
        processData(env);
        setF_OUT_CalculatedFDAmount(calcAmount);
        setF_OUT_AcNum(accNum);
    }

    private void processData(BankFusionEnvironment env) throws BankFusionException{
    	
        VectorTable accountListVector = new VectorTable();
        VectorTable accountListVectorWithString = new VectorTable();
        String amount = CommonConstants.EMPTY_STRING;
        String exchageRate = CommonConstants.EMPTY_STRING;
        BigDecimal amountInBigDecimal = CommonConstants.BIGDECIMAL_ZERO;
        String fundCurrency = CommonConstants.EMPTY_STRING;
        String fdCurrency = CommonConstants.EMPTY_STRING;
        String dateString =CommonConstants.EMPTY_STRING;
        String contraAccount =CommonConstants.EMPTY_STRING;
        DateFormat formatter ; 
        Date date = null ;
        BigDecimal exchangeRateInBigDecimal = CommonConstants.BIGDECIMAL_ZERO;
        
        UB_IBI_Message messageIBI = getF_IN_Message();
        WeakHashMap<String, Object> fundingAccountMap = new WeakHashMap<String, Object>();
        WeakHashMap<String, String> fundingAccountMapWithString = new WeakHashMap<String, String>();
        fdCurrency = messageIBI.getContent().getAccount().getAcctDetail().getFixedDepositAcctDetail().getFixedDepositISOCurrencyCode();
        UB_IBI_MultiFundingAccount[] array = messageIBI.getContent().getAccount().getAcctDetail().getFundingAccountDetail();
        dateString = messageIBI.getContent().getAccount().getAcctDetail().getFixedDepositAcctDetail().getStartDate();
        
        for (int i = 0; i < array.length; i++) {
            amount = messageIBI.getContent().getAccount().getAcctDetail().getFundingAccountDetail(i).getFundingAmount();
            amountInBigDecimal = new BigDecimal(amount);
            
           // exchageRate = messageIBI.getContent().getAccount().getAcctDetail().getFundingAccountDetail(i).getFundingExchangeRate();
           // exchangeRateInBigDecimal = new BigDecimal(exchageRate);
            
            if(messageIBI.getContent().getAccount().getAcctDetail().getFundingAccountDetail(i).getFundingCurrencyCode().compareTo(fdCurrency)!=0){
            	fundCurrency = messageIBI.getContent().getAccount().getAcctDetail().getFundingAccountDetail(i).getFundingCurrencyCode();
            }else
            {
            	fundCurrency = fdCurrency;
            }	
            
            exchangeRateInBigDecimal = getExchangeRate("SPOT", fundCurrency, fdCurrency, amountInBigDecimal);
            exchageRate = exchangeRateInBigDecimal.toString();
            
            if(exchangeRateInBigDecimal.compareTo(CommonConstants.BIGDECIMAL_ZERO)<=0){
            	exchangeRateInBigDecimal = new BigDecimal(1);
            }
            {
                fundingAccountMapWithString.put(FUNDING_ACCOUNT_ID, messageIBI.getContent().getAccount().getAcctDetail()
                        .getFundingAccountDetail(i).getFundingAccountID());
                accNum = messageIBI.getContent().getAccount().getAcctDetail().getFundingAccountDetail(i).getFundingAccountID();
                fundingAccountMapWithString.put(FUNDING_AMOUNT, amount);
                fundingAccountMapWithString.put(FUNDING_CURRENCYCODE, messageIBI.getContent().getAccount().getAcctDetail()
                        .getFundingAccountDetail(i).getFundingCurrencyCode());
                fundingAccountMapWithString.put(FUNDING_EXCHANGERATE, exchageRate);
            }
          /*  if(messageIBI.getContent().getAccount().getAcctDetail().getFundingAccountDetail(i).getFundingCurrencyCode().compareTo(fdCurrency)!=0){
            	fdCurrency = messageIBI.getContent().getAccount().getAcctDetail().getFundingAccountDetail(i).getFundingCurrencyCode();
            } */
            fundingAccountMap.put(FUNDING_ACCOUNT_ID, messageIBI.getContent().getAccount().getAcctDetail().getFundingAccountDetail(
                    i).getFundingAccountID());
            fundingAccountMap.put(FUNDING_AMOUNT, amountInBigDecimal);
            fundingAccountMap.put(FUNDING_CURRENCYCODE, messageIBI.getContent().getAccount().getAcctDetail()
                    .getFundingAccountDetail(i).getFundingCurrencyCode());
            fundingAccountMap.put(FUNDING_EXCHANGERATE, exchangeRateInBigDecimal);
            fundingAccountMap.put(CONTRA_ACCOUNT, messageIBI.getContent().getAccount().getAcctDetail()
                    .getFundingAccountDetail(i).getFundingAccountID());
            accountListVector.addAll(new VectorTable(fundingAccountMap));
            
             
             formatter = new SimpleDateFormat("yyyyMMdd");
             try {
				date = (Date)formatter.parse(dateString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				logger.error(ExceptionUtil.getExceptionAsString(e));
			}  

             
            if(exchangeRateInBigDecimal.compareTo(CommonConstants.BIGDECIMAL_ZERO)==0){
            	calcAmount = calcAmount.add(amountInBigDecimal);
            }
            else{
            	// calcAmount = calcAmount.add(amountInBigDecimal.multiply(exchangeRateInBigDecimal));
            	calcAmount = calcAmount.add(convertCurrencyAmount(amountInBigDecimal, exchangeRateInBigDecimal,exchangeRateType, fundCurrency, fdCurrency));
            }
            accountListVectorWithString.addAll(new VectorTable(fundingAccountMapWithString));
        }
        setF_OUT_ConveretedDate(new java.sql.Date(GetDateWithZeroTime(date).getTime()));
        setF_OUT_MultiFundingAccountVector(accountListVector);
        setF_OUT_MultiFundingAccountVectorForString(accountListVectorWithString);

    }



private BigDecimal getExchangeRate(String exchangeType, String fromCurrency, String toCurrency, BigDecimal amount) {
        
        ExchangeRateFatom exchangeRateFatom = new ExchangeRateFatom(env);
        exchangeRateFatom.setF_IN_EXCHRATETYPE(exchangeType);
        exchangeRateFatom.setF_IN_BUYCURRENCYCODE(fromCurrency);
        exchangeRateFatom.setF_IN_BUYAMOUNT(amount);
        exchangeRateFatom.setF_IN_SELLCURRENCYCODE(toCurrency);
        exchangeRateFatom.process(env);
        BigDecimal fdExchangeRate = exchangeRateFatom.getF_OUT_EXCHANGERATE();
        
        return fdExchangeRate;
    }
	public static java.util.Date GetDateWithZeroTime(java.util.Date date) {
		Calendar cDate = Calendar.getInstance();
		cDate.setTime(date);
		cDate.set(Calendar.HOUR, 0);
		cDate.set(Calendar.MINUTE, 0);
		cDate.set(Calendar.SECOND, 0);
		cDate.set(Calendar.MILLISECOND, 0);
		cDate.set(Calendar.AM_PM, Calendar.AM);
		cDate.set(Calendar.DST_OFFSET, 0);
		return (cDate.getTime());
	}
	
    protected BigDecimal convertCurrencyAmount(BigDecimal amount, BigDecimal exchRate, String exchRateType, String fromCurrCode,
            String toCurrCode) {

        BigDecimal returnAmount = null;
        if ((ubBusinessInfo.isMultiply(fromCurrCode, toCurrCode, exchRateType)))
            returnAmount = amount.multiply(exchRate);
        else returnAmount = amount.divide(exchRate, 12, BigDecimal.ROUND_UP);

        BFCurrencyValue bfCurrencyReturnValue = new BFCurrencyValue(toCurrCode, returnAmount, null);

        returnAmount = bfCurrencyReturnValue.getRoundedAmount();
        return returnAmount;
    }
	
}
