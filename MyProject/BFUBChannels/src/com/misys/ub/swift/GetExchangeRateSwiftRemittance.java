package com.misys.ub.swift;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.common.GetUBConfigLocation;
import com.misys.ub.common.events.CommonsEventCodes;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_GetExchangeRate;

public class GetExchangeRateSwiftRemittance extends AbstractUB_SWT_GetExchangeRate {



	private transient final static Log logger = LogFactory
			.getLog(GetExchangeRateSwiftRemittance.class.getName());

	String fromCurrencyCode;
	String toCurrencyCode;
	String exchangeRateType;
	String STP;
	BigDecimal amount = BigDecimal.ZERO;
	BigDecimal exchangeRate = BigDecimal.ZERO;
	BigDecimal screenExchangeRate = BigDecimal.ZERO;
	String baseCurr;
	private static Properties SwiftExchangeRateProperties;
	BankFusionEnvironment env = null;

	private synchronized void loadProperties() {
		InputStream SwiftPropertiesInputStream = null;
		try {
			String configLocation = GetUBConfigLocation.getUBConfigLocation();
			SwiftPropertiesInputStream = new FileInputStream(
					configLocation
							.concat("conf/swift/SWT_ExchangeRatePreference.properties"));
			SwiftExchangeRateProperties = new Properties();
			SwiftExchangeRateProperties.load(SwiftPropertiesInputStream);
		} catch (IOException e) {
			logger.info("Could not load Swift Exchange Rate Properties file");
		} finally {
			if (SwiftPropertiesInputStream != null)
				try {
					SwiftPropertiesInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}


	public Map<String, Object> getExchangeRate(String fromCcyCode, String toCcyCode, BigDecimal inputAmount, String stp) {
		env = BankFusionThreadLocal.getBankFusionEnvironment();
		fromCurrencyCode = fromCcyCode;
		toCurrencyCode = toCcyCode;
		//exchangeRateType = getF_IN_exchangeRateType();
		amount = inputAmount;
		STP= stp;
		//screenExchangeRate = getF_IN_screenExchangeRate();
		Map<String, Object> outMap = new HashMap<String, Object>();
		String buyOrSell = CommonConstants.EMPTY_STRING;
		String txnType = CommonConstants.EMPTY_STRING;
		baseCurr = SystemInformationManager.getInstance().getBaseCurrencyCode();
		if (null != SwiftExchangeRateProperties) {
			logger.info("Swift ExchangeRate Properties\n"
					+ SwiftExchangeRateProperties.toString());
		} else {
			loadProperties();
		}

		if(fromCurrencyCode.equalsIgnoreCase(baseCurr))
		{
			if(toCurrencyCode.equalsIgnoreCase(baseCurr))
				exchangeRateType ="SPOT";
			else
				exchangeRateType ="BUY";
		}
		else
		{
			if(toCurrencyCode.equalsIgnoreCase(baseCurr))
				exchangeRateType ="BUY";
			else
				exchangeRateType ="BUY";
		}

		if (!fromCurrencyCode.equals(toCurrencyCode)) {

			if (exchangeRateType.equalsIgnoreCase("SPOT")) {
				buyOrSell = "SPOT";
				txnType = CommonConstants.EMPTY_STRING;
			} else {
				buyOrSell = exchangeRateType.substring(0, 3);
			}
			String exchPref = SwiftExchangeRateProperties.getProperty(buyOrSell);

			if(STP.equalsIgnoreCase("Y"))
			{
				exchangeRate = screenExchangeRate;}
			else{
				if(exchPref == null || exchPref.equals(CommonConstants.EMPTY_STRING)){
					exchangeRate = computeExchangeRate(fromCurrencyCode, toCurrencyCode, exchangeRateType,amount, env);
					if(exchangeRate!=null && exchangeRate.equals(CommonConstants.BIGDECIMAL_ZERO)){
						exchangeRate = computeExchangeRate(fromCurrencyCode, toCurrencyCode, "SPOT",amount, env);
					}
				}else {
					String[] exchPrefrences = exchPref.split(",");
					for (String pref : exchPrefrences) {
						pref = pref.trim();
						String exctype=pref.substring(3, 6);
						txnType=SwiftExchangeRateProperties.getProperty(exctype.concat("TxnType"));
						// If the preference contains "/" character in the string
						if (pref.contains("/") || pref.contains("*")) {
							String[] subPref;
							if (pref.contains("/")){
								subPref = pref.split("/");
							}else { 
								subPref = pref.split("\\*");
							}

							BigDecimal exchangeRate1 = BigDecimal.ZERO;


							if(subPref[0].equals("1"))
							{
								exchangeRate1= BigDecimal.ONE;
							} else{
								String exctype1=subPref[0].substring(3, 6);

								txnType=SwiftExchangeRateProperties.getProperty(exctype1.concat("TxnType"));
								exchangeRateType = subPref[0] + txnType;
								exchangeRate1 = getExchangeRate(subPref[0] + txnType, env);
							}
							String exctype2=subPref[1].substring(3, 6);
							String txnType2=SwiftExchangeRateProperties.getProperty(exctype2.concat("TxnType"));
							exchangeRateType = (subPref[1] + txnType2).substring(3);
							BigDecimal exchangeRate2 = getExchangeRate(subPref[1] + txnType2, env);

							if (exchangeRate1 !=null && exchangeRate2 !=null && !exchangeRate1.equals(CommonConstants.BIGDECIMAL_ZERO) && !exchangeRate2.equals(CommonConstants.BIGDECIMAL_ZERO)) {
								if (pref.contains("/")){
									exchangeRate = exchangeRate1.divide(exchangeRate2,new MathContext(10, RoundingMode.UP));}
								else{
									exchangeRate = exchangeRate1.multiply(exchangeRate2);
								}
								exchangeRate = exchangeRate.setScale(12,BigDecimal.ROUND_UP);
								break;
							}
						}
						else {
							exchangeRateType = (pref + txnType).substring(3);
							exchangeRate = getExchangeRate(pref + txnType, env);
							if (exchangeRate != null && !exchangeRate.equals(CommonConstants.BIGDECIMAL_ZERO)) {
								break;
							}
						}
					}
				}
			}
		} else {
			exchangeRate = BigDecimal.ONE;
			exchangeRateType ="SPOT";
		}
		// Setting exchange rate to output.
		java.text.DecimalFormat formatter = new java.text.DecimalFormat("##.########");	
		formatter.setMaximumFractionDigits(8);
		formatter.setMinimumFractionDigits(2);
		exchangeRate = new BigDecimal(formatter.format(exchangeRate));
		BigDecimal convertedAmount = exchangeRate.multiply(amount).setScale(2,BigDecimal.ROUND_HALF_UP);
		outMap.put("exchangeRateType", exchangeRateType);
		outMap.put("exchangeRate", exchangeRate);
		outMap.put("convertedAmount", convertedAmount);
		return outMap;
	}



	private BigDecimal getExchangeRate(String rateCode, BankFusionEnvironment env) {
		String ccyCode = rateCode.substring(0, 2);
		String rateType = rateCode.substring(3);
		String fromCurrency = null;
		String toCurrency = null;

		if (ccyCode.equals("NA")) {
			fromCurrency = fromCurrencyCode;
			toCurrency = toCurrencyCode;
		} else if (ccyCode.equals("AN")) {
			fromCurrency = toCurrencyCode;
			toCurrency = fromCurrencyCode;
		} else if(ccyCode.equals("NB")) {
			fromCurrency = fromCurrencyCode;
			toCurrency = baseCurr;
		} else if (ccyCode.equals("BA")) {
			fromCurrency = baseCurr;
			toCurrency = toCurrencyCode;
		} else if (ccyCode.equals("BN")) {
			fromCurrency = baseCurr;
			toCurrency = fromCurrencyCode;
		} else if (ccyCode.equals("AB")) {
			fromCurrency = toCurrencyCode;
			toCurrency = baseCurr;
		}

		return computeExchangeRate(fromCurrency, toCurrency, rateType, amount, env);
	}

	private BigDecimal computeExchangeRate(String fromCurrency,
			String toCurrency, String rateType,
			BigDecimal amount, BankFusionEnvironment env) {
		BigDecimal exchangeRate = null;
		try {
			IBusinessInformation bizInformation = ((IBusinessInformationService) ServiceManagerFactory.getInstance()
					.getServiceManager().getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();
			exchangeRate = bizInformation.getExchangeRateDetail(fromCurrency, toCurrency, rateType, amount, env);

		} catch (Exception e) {
				if(logger.isInfoEnabled()){
					logger.info(e.getLocalizedMessage());
				}
			   EventsHelper.handleEvent(PaymentSwiftConstants.E_THE_EXCHANGE_RATE_TO_FOR_THE_RATE, new Object[] {}, null, env);
		}

		return exchangeRate;
	}
}