package com.misys.ub.payment.swift.utils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Maps;
import com.misys.bankfusion.common.BankFusionMessages;
import com.misys.bankfusion.common.runtime.service.ServiceManagerFactory;
import com.misys.bankfusion.events.IBusinessEventsService;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.bankfusion.subsystem.persistence.SimplePersistentObject;
import com.misys.cbs.exchangerate.ExchangeRateConstants;
import com.misys.ub.common.constants.GeneralConstants;
import com.misys.ub.datacenter.DataCenterCommonConstants;
import com.misys.ub.payment.posting.SWTPostingUtils;
import com.misys.ub.swift.RemittanceIdGenerator;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOTransactionScreenControl;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_RemittanceTable;
import com.trapedza.bankfusion.core.BFCurrencyValue;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;

import bf.com.misys.cbs.msgs.v1r0.RetrievePsydnymAcctIdRq;
import bf.com.misys.cbs.msgs.v1r0.RetrievePsydnymAcctIdRs;
import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.services.CalcExchangeRateRq;
import bf.com.misys.cbs.services.CalcExchangeRateRs;
import bf.com.misys.cbs.services.ReadModuleConfigurationRq;
import bf.com.misys.cbs.services.ReadModuleConfigurationRs;
import bf.com.misys.cbs.types.CalcExchRateDetails;
import bf.com.misys.cbs.types.Currency;
import bf.com.misys.cbs.types.ExchangeRateDetails;
import bf.com.misys.cbs.types.ModuleKeyRq;
import bf.com.misys.cbs.types.PseudonymBasicDetails;
import bf.com.misys.cbs.types.events.Event;
import bf.com.misys.cbs.types.header.EventParameters;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.Orig;
import bf.com.misys.cbs.types.header.RqHeader;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;
import bf.com.misys.cbs.types.swift.TextLines4;
import bf.com.misys.cbs.types.swift.TextLines6;

public class PaymentSwiftUtils {
	/**
	 * @param accountId
	 * @return
	 */
	public String getAccountRightsInd(String accountId) {
		Map<String, String> map = new HashMap<String, String>();
		Map outPutMap = new HashMap();
		String accRightsInd = null;
		map.put("AccountID", accountId);
		outPutMap = MFExecuter.executeMF(PaymentSwiftConstants.GetAccountDetailsMF, BankFusionThreadLocal.getBankFusionEnvironment(), map);
		accRightsInd = outPutMap.get("ACCRIGHTSINDICATOR").toString();
		return accRightsInd;
	}

	/**
	 * @param accountId
	 * @param currency
	 * @return
	 */
	public BigDecimal getAvailableBalance(String accountId, String currency) {
		Map<String, String> map = new HashMap<String, String>();
		Map outPutMap = new HashMap();
		BigDecimal availableBal = BigDecimal.ZERO;
		map.put("AccountId", accountId);
		map.put("CurrencyCode", currency);
		outPutMap = MFExecuter.executeMF(PaymentSwiftConstants.GetAvailableBalanceMF, BankFusionThreadLocal.getBankFusionEnvironment(), map);
		availableBal = (BigDecimal) outPutMap.get("AvailableBalance");
		return availableBal;
	}

	/**
	 * @param value
	 * @param channelID
	 * @return
	 */
	public String getModuleConfigValue(String value, String channelID) {
		String valueRs = "";
		HashMap<String, ReadModuleConfigurationRq> moduleParams = new HashMap<String, ReadModuleConfigurationRq>();
		ModuleKeyRq module = new ModuleKeyRq();
		ReadModuleConfigurationRq read = new ReadModuleConfigurationRq();
		module.setModuleId(channelID);
		module.setKey(value);
		read.setModuleKeyRq(module);
		moduleParams.put("ReadModuleConfigurationRq", read);
		HashMap valueFromModuleConfiguration = MFExecuter.executeMF(PaymentSwiftConstants.READ_MODULE_CONFIGURATION, BankFusionThreadLocal.getBankFusionEnvironment(), moduleParams);
		if (valueFromModuleConfiguration != null) {
			ReadModuleConfigurationRs rs = (ReadModuleConfigurationRs) valueFromModuleConfiguration.get("ReadModuleConfigurationRs");
			valueRs = rs.getModuleConfigDetails().getValue().toString();
		}
		return valueRs;
	}

	/**
	 * @param channel
	 * @param creditTxnCode
	 * @param debitTxnCode
	 * @param type
	 * @return
	 */
	public String getTransactionCode(String channel, String creditTxnCode, String debitTxnCode, String type) {
		String transactionCode = CommonConstants.EMPTY_STRING;
		if (type.equalsIgnoreCase("debit")) {
			if (debitTxnCode != null && debitTxnCode != CommonConstants.EMPTY_STRING) {
				transactionCode = debitTxnCode;
			}
			else if (PaymentSwiftConstants.CHANNELID_CCI.equals(channel)) {
				// INTNAT :  Corporate channel SWIFT Payment
				transactionCode = getModuleConfigValue(PaymentSwiftConstants.FOREIGNPYMTDR_CCI, channel);
			}
			else {
				transactionCode = getModuleConfigValue(PaymentSwiftConstants.FOREIGNPYMT, channel);
			}
		}
		else if (type.equalsIgnoreCase("credit")) {
			if (creditTxnCode != null && creditTxnCode != CommonConstants.EMPTY_STRING) {
				transactionCode = creditTxnCode;
			}
			else if (PaymentSwiftConstants.CHANNELID_CCI.equals(channel)) {
				transactionCode = getModuleConfigValue(PaymentSwiftConstants.FOREIGNPYMTCR_CCI, channel);
			}
			else {
				transactionCode = getModuleConfigValue(PaymentSwiftConstants.FOREIGNPYMTCR, channel);
			}
		}
		return transactionCode;
	}

	/**
	 * @param eventNumber
	 * @param args
	 */
	public static void handleEvent(Integer eventNumber, String[] args) {
		if (args == null) {
			args = new String[] { CommonConstants.EMPTY_STRING };
		}
		Event event = new Event();
		event.setEventNumber(eventNumber);
		event.setMessageArguments(args);
		IBusinessEventsService businessEventsService = (IBusinessEventsService) ServiceManagerFactory.getInstance().getServiceManager().getServiceForName(IBusinessEventsService.SERVICE_NAME);
		businessEventsService.handleEvent(event);
	}

	/**
	 * @param currency
	 * @param direction
	 * @return
	 */
	public static String getRemittanceId(String currency, String direction) {
		String remittanceId = null;
		SimpleDateFormat sdfDate = new SimpleDateFormat("ddMMYYYYHHmmss");
        Date now = SystemInformationManager.getInstance().getBFSystemDate();
		String strDate = sdfDate.format(now);
		if(null != direction && direction.equalsIgnoreCase(PaymentSwiftConstants.REMITTACNCE_OUTWARD)) {
			remittanceId = direction.concat(currency).concat(strDate).concat(String.valueOf(
					RemittanceIdGenerator.generateRateTypeSequenceId(GeneralConstants.SWIFT_OUTWR_REMITTANCE_ID)));
		}else if(null != direction){
			remittanceId = direction.concat(currency).concat(strDate).concat(String.valueOf(
					RemittanceIdGenerator.generateRateTypeSequenceId(GeneralConstants.SWIFT_INWR_REMITTANCE_ID)));
		}
		
		return remittanceId;
	}

	/**
	 * @param eventCode
	 * @return
	 */
	public static SubCode addEventCode(String eventCode, SubCode code) {
		code.setCode(eventCode);
		code.setSeverity(PaymentSwiftConstants.ERROR);
		return code;
	}

	/**
	 * return request header object
	 * 
	 * @return
	 */
	public static RqHeader rqHeaderInput() {
		RqHeader rqHeader = new RqHeader(); // need to check with Machamma
		Orig orig = new Orig();
		orig.setOrigCtxtId(StringUtils.EMPTY);
		orig.setAppId(StringUtils.EMPTY);
		orig.setAppVer(StringUtils.EMPTY);
		orig.setChannelId(BankFusionThreadLocal.getChannel());
		orig.setOfflineMode(false);
		orig.setOrigBranchCode(BankFusionThreadLocal.getUserSession().getBranchSortCode());
		orig.setOrigId(StringUtils.EMPTY);
		orig.setOrigLocale(StringUtils.EMPTY);
		orig.setZoneId(StringUtils.EMPTY);
		rqHeader.setOrig(orig);
		return rqHeader;
	}

	/**
	 * @param isoCcyCode
	 * @param branchCode
	 * @param context
	 * @param pseudonymId
	 * @return
	 */
	public static String retrievePsuedonymAcctId(String isoCcyCode, String branchCode, String context, String pseudonymId) {
		String accountId = CommonConstants.EMPTY_STRING;
		HashMap result = null;
		HashMap map = new HashMap();
		RetrievePsydnymAcctIdRq pseudonymRq = new RetrievePsydnymAcctIdRq();
		PseudonymBasicDetails pseudonymDetails = new PseudonymBasicDetails();
		pseudonymDetails.setBranchCode(branchCode);
		pseudonymDetails.setIsoCurrencyCode(isoCcyCode);
		pseudonymDetails.setPseudonymID(pseudonymId);
		pseudonymDetails.setPseudonymType(StringUtils.EMPTY);
		pseudonymDetails.setContextType(context);
		pseudonymDetails.setContextValue(StringUtils.EMPTY);
		pseudonymRq.setPseudonymBasicDetails(pseudonymDetails);
		pseudonymRq.setRqHeader(rqHeaderInput());
		map.put("retrievePseudonymRq", pseudonymRq);
		result = MFExecuter.executeMF(DataCenterCommonConstants.RETRIEVE_PSEUDONYM_ACCT_ID, BankFusionThreadLocal.getBankFusionEnvironment(), map);
		if (result != null) {
			RetrievePsydnymAcctIdRs psedonymRs = null;
			psedonymRs = (RetrievePsydnymAcctIdRs) result.get("retrievePseudonymRs");
			accountId = psedonymRs.getPseudonymDetails().getPseudonymAcctId().getStandardAccountId();
		}
		return accountId;
	}

	/**
	 * @param accountId
	 * @return
	 */
	public IBOAttributeCollectionFeature getAccountDetails(String accountId) {
		IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
        return (IBOAttributeCollectionFeature) factory.findByPrimaryKey(IBOAttributeCollectionFeature.BONAME, accountId, true);
	}
	/**
	 * @param rsHeader
	 * @return
	 */
	public static String getEventParameter(RsHeader rsHeader) {
		String eventParameter = StringUtils.EMPTY;
		MessageStatus messageStatus = rsHeader.getStatus();
		SubCode subCode = messageStatus.getCodes(0);
		EventParameters[] eventParamsArray = subCode.getParameters();
		List<EventParameters> eventParamsList = Arrays.asList(eventParamsArray);
		String code = subCode.getCode();
		
		if (null != code && !code.equals(CommonConstants.EMPTY_STRING)) {
			for (EventParameters eventParam : eventParamsList) {
				if (null != eventParam) {
					eventParameter = eventParam.getEventParameterValue();
				}
			}
		}
		return eventParameter;
	}

	/**
	 * @param exchangeRateType
	 * @param buyAmount
	 * @param buyCcy
	 * @param sellCcy
	 * @param sellAmount
	 * @param exchangeRate
	 * @return
	 */
	public CalcExchangeRateRs getExchangeRateAmount(String exchangeRateType, BigDecimal buyAmount, String buyCcy, String sellCcy, BigDecimal sellAmount, BigDecimal exchangeRate) {
		CalcExchangeRateRq rq = new CalcExchangeRateRq();
		CalcExchRateDetails calExchgRateDetails = new CalcExchRateDetails();
		ExchangeRateDetails details = new ExchangeRateDetails();
		details.setExchangeRateType(exchangeRateType);
		details.setExchangeRate(exchangeRate);
		details.setMultiplyDivide(getMultipleDevideConstant(Boolean.TRUE));
		calExchgRateDetails.setBuyAmount(buyAmount);
		calExchgRateDetails.setBuyCurrency(buyCcy);
		calExchgRateDetails.setSellAmount(sellAmount);
		calExchgRateDetails.setSellCurrency(sellCcy);
		calExchgRateDetails.setExchangeRateDetails(details);
		rq.setCalcExchRateDetails(calExchgRateDetails);
		rq.setRqHeader(rqHeaderInput());
		return getExchangeRateDetails(rq, StringUtils.EMPTY, false, false);
	}

	/**
	 * Calculates Exchange rate amount by calling <code>MFName.BT_FEX_CalculateExchangeRateAmount_SRV</code>
	 * 
	 * @param calcExchangeRateRq - {@code CalcExchangeRateRq}
	 * @return calcExchangeRateRs - {@code calcExchangeRateRs}
	 */
	@SuppressWarnings("unchecked")
	private CalcExchangeRateRs getExchangeRateDetails(CalcExchangeRateRq calcExchangeRateRq, String customerId, boolean isCustMargin, boolean allowCurCodeEmpty) {
		CalcExchangeRateRs calcExchangeRateRs = new CalcExchangeRateRs();
		Map<String, Object> map = Maps.newHashMap();
		map.put(PaymentSwiftConstants.CALCULATE_EXCHANGE_RATE_RQ, calcExchangeRateRq);
		map.put(PaymentSwiftConstants.CUSTOMER_ID, customerId);
		map.put(PaymentSwiftConstants.CUSTOMER_MARGIN_APPLIED_FLAG, isCustMargin);
		map.put(PaymentSwiftConstants.ALLOW_CURRENCY_CODE_EMPTY, allowCurCodeEmpty);
		Map<String, Object> result = MFExecuter.executeMF("CB_FEX_CalculateExchangeRateAmount_SRV", BankFusionThreadLocal.getBankFusionEnvironment(), map);
		if (result != null) {
			calcExchangeRateRs = (CalcExchangeRateRs) result.get(PaymentSwiftConstants.CALCULATE_EXCHANGE_RATE_RS);
		}
		return calcExchangeRateRs;
	}

	/**
	 * @param multiplyDevide
	 * @return
	 */
	private String getMultipleDevideConstant(boolean multiplyDevide) {
		return multiplyDevide == true ? PaymentSwiftConstants.MULTIPLY_CONSTANT : PaymentSwiftConstants.DIVIDE_CONSTANT;
	}

	/**
	 * Raises Business Events
	 * @param eventMap
	 * @param eventCode
	 */
	public void raiseEvent(Map<String, Object> eventMap, String eventCode) {
		if (null != eventCode && !eventCode.isEmpty()) {
			Event statusEvent = new Event();
			statusEvent.setEventNumber(Integer.parseInt(eventCode));
			statusEvent.setMessageArguments(new String[0]);
			IBusinessEventsService businessEventsService = (IBusinessEventsService) ServiceManagerFactory.getInstance().getServiceManager().getServiceForName("BusinessEventsService");
			businessEventsService.handleEvent(statusEvent, eventMap);
		}
	}
	
	/**
	 * get exchangerate Type
	 * 
	 * @param msgRefId
	 */
	public String getExchangeRateType(String msgRefId) {
		String exchangeRateType = "";
		String exchangeRateQuery = "SELECT t1." + IBOUB_SWT_RemittanceTable.UBEXCHANGERATETYPEDR
				+ " AS EXCHANGERATETYPE " + " FROM " + IBOUB_SWT_RemittanceTable.BONAME + " t1 WHERE t1."
				+ IBOUB_SWT_RemittanceTable.UBMESSAGEREFID + " = ? ";
		IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
		ArrayList<String> arr = new ArrayList<String>();
		arr.add(msgRefId);
		List<SimplePersistentObject> result = factory.executeGenericQuery(exchangeRateQuery, arr, null, false);

		if (null != result && !result.isEmpty()) {
			SimplePersistentObject persistentObject = (SimplePersistentObject) result.get(0);
			String exType = (String) persistentObject.getDataMap().get("EXCHANGERATETYPE");
			exchangeRateType = exType;
		}

		return exchangeRateType;
	}
	
    /**
     * @param chargeType
     * @return
     */
    @SuppressWarnings("unused")
    public String getChargeDetailCcy(ChargesDto chargesDto) {
        String chargeDetailCcy=StringUtils.EMPTY;
        switch (chargesDto.getChargeType()) {
            case PaymentSwiftConstants.CHARGE_CODE_BEN:
            case PaymentSwiftConstants.CHARGE_CODE_SHA:
                chargeDetailCcy = getChargeDetailCcyCode(chargesDto.getUbCharges().getAmount(),chargesDto.getDebitAmount().getIsoCurrencyCode(),chargesDto.getCreditAmount().getIsoCurrencyCode()) ;
                break;
            case PaymentSwiftConstants.CHARGE_CODE_OUR:
                chargeDetailCcy = chargesDto.getCreditAmount().getIsoCurrencyCode();
                break;
            default:
                chargeDetailCcy = chargesDto.getCreditAmount().getIsoCurrencyCode();
        }
        return chargeDetailCcy;
    }
	
    /**
     * @param ubCharge
     * @param chargeCcy
     * @return
     */
    @SuppressWarnings("unused")
    private String getChargeDetailCcyCode(BigDecimal ubChargeAmt,String debitAccCcy,String creditAccCcy) {
        String chargeDetailCcy = StringUtils.EMPTY;
        if(ubChargeAmt.compareTo(BigDecimal.ZERO)>0) {
            chargeDetailCcy=debitAccCcy;
        }else {
            chargeDetailCcy=creditAccCcy;
        }
        return chargeDetailCcy;
        
    }
    
    /**
     * @param remittanceDetails
     * @param outwardRq
     * @return
     */
    @SuppressWarnings("unchecked")
    public ChargesDto getUBChargesFromChargeArray(SwiftRemittanceRq swtRemitanceReq) {
        Currency ubCharges = new Currency();
        ChargesDto chargesDto =new ChargesDto();
        BigDecimal chgAmountInFundingAccCurrency = BigDecimal.ZERO;
        BigDecimal taxAmountInFundingAccCurrency = BigDecimal.ZERO;
        String chargefundingAccCurrency = StringUtils.EMPTY;
        String chargeFundingAccountId=StringUtils.EMPTY;
        BigDecimal totalCharge = BigDecimal.ZERO;
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        BigDecimal totalTaxAndCharge = BigDecimal.ZERO;
        VectorTable chargeVector = SWTPostingUtils.getChargeDetails(swtRemitanceReq.getInitiateSwiftMessageRqDtls().getCharges());
        if (chargeVector.size() > 0) {
            for (int i = 0; i < chargeVector.size(); i++) {
                Map<String, Object> map = chargeVector.getRowTags(i);
                chgAmountInFundingAccCurrency = (BigDecimal) map
                        .get(PaymentSwiftConstants.OCV_CHARGEAMOUNT_IN_FUND_ACC_CURRENCY) != null
                                ? (BigDecimal) map.get(PaymentSwiftConstants.OCV_CHARGEAMOUNT_IN_FUND_ACC_CURRENCY)
                                : BigDecimal.ZERO;
                taxAmountInFundingAccCurrency = (BigDecimal) map
                        .get(PaymentSwiftConstants.OCV_TAXAMOUNT_IN_FUND_ACC_CURRENCY) != null
                                ? (BigDecimal) map.get(PaymentSwiftConstants.OCV_TAXAMOUNT_IN_FUND_ACC_CURRENCY)
                                : BigDecimal.ZERO;
                chargefundingAccCurrency = (String) map.get(PaymentSwiftConstants.OCV_FUND_ACC_CURRENCY) != null
                        ? (String) map.get(PaymentSwiftConstants.OCV_FUND_ACC_CURRENCY)
                        : StringUtils.EMPTY;
                chargeFundingAccountId = (String) map.get(PaymentSwiftConstants.OCV_FUNDINGACCOUNTID) != null
                        ? (String) map.get(PaymentSwiftConstants.OCV_FUNDINGACCOUNTID)
                        : StringUtils.EMPTY;
                totalCharge = chgAmountInFundingAccCurrency.add(totalCharge);
                totalTaxAmount = taxAmountInFundingAccCurrency.add(totalTaxAmount);
            }
            totalTaxAndCharge = totalCharge.add(totalTaxAmount);
        }
        ubCharges.setAmount(totalTaxAndCharge);
        ubCharges.setIsoCurrencyCode(chargefundingAccCurrency);
        chargesDto.setUbCharges(ubCharges);
        chargesDto.setChargeFundingAccountId(chargeFundingAccountId);
        return chargesDto;
    }
    
    public static String getErrorDescription(String eventCode, Object[] params, BankFusionEnvironment env) {
        String errorRsn = StringUtils.EMPTY;
        if (!StringUtils.isBlank(eventCode)) {
                errorRsn = BankFusionMessages.getInstance().getFormattedEventMessage(Integer.parseInt(eventCode),
                        params, env.getUserSession().getUserLocale());
        }
        return errorRsn;

    }
    
    public static Object intializeDefaultvalues(Object intialObj) {
        Field[] fooFields = intialObj.getClass().getDeclaredFields();
        try {
            for (Field fooField : fooFields) {
                fooField.setAccessible(true);
                if (fooField.getType() == String.class) {
                    fooField.set(intialObj, CommonConstants.EMPTY_STRING);
                }
                if (fooField.getType() == Boolean.class) {
                    fooField.set(intialObj, Boolean.FALSE);
                }
                if (fooField.getType() == Date.class) {
                    fooField.set(intialObj, null);
                }
                if (fooField.getType() == Integer.class) {
                    fooField.set(intialObj, BigDecimal.ZERO.intValue());
                }
                if (fooField.getType() == BigDecimal.class) {
                    fooField.set(intialObj, BigDecimal.ZERO);
                }
                if (fooField.getType() == TextLines4.class) {
                    TextLines4 textLines4 = new TextLines4();
                    textLines4.setTextLine1(CommonConstants.EMPTY_STRING);
                    textLines4.setTextLine2(CommonConstants.EMPTY_STRING);
                    textLines4.setTextLine3(CommonConstants.EMPTY_STRING);
                    textLines4.setTextLine4(CommonConstants.EMPTY_STRING);
                    fooField.set(intialObj, textLines4);
                }
                if (fooField.getType() == TextLines6.class) {
                    TextLines6 textLines6 = new TextLines6();
                    textLines6.setTextLine1(CommonConstants.EMPTY_STRING);
                    textLines6.setTextLine2(CommonConstants.EMPTY_STRING);
                    textLines6.setTextLine3(CommonConstants.EMPTY_STRING);
                    textLines6.setTextLine4(CommonConstants.EMPTY_STRING);
                    textLines6.setTextLine5(CommonConstants.EMPTY_STRING);
                    textLines6.setTextLine6(CommonConstants.EMPTY_STRING);
                    fooField.set(intialObj, textLines6);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return intialObj;
    }
    
    public static BigDecimal calcExchgAmt(BigDecimal toBeCovertedamount, BigDecimal exchRate, String indctrMorD, String currCode) {
        BigDecimal convertedAmt;
        if (indctrMorD.equals(ExchangeRateConstants.EXCHANGE_RATE_DIVIDE)) {
            convertedAmt = toBeCovertedamount.divide(exchRate, 8, BigDecimal.ROUND_DOWN);
        }
        else {
            convertedAmt = toBeCovertedamount.multiply(exchRate);
            // multiplyDivide = ExchangeRateConstants.EXCHANGE_RATE_MULTIPLY;
        }

        convertedAmt = new BFCurrencyValue(currCode, convertedAmt, null).getRoundedAmount();
        return convertedAmt;
    }
    
    
    public static IBOMisTransactionCodes getMisTransactionCodes(String misCode) {
		IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
		IBOMisTransactionCodes misCodeBO = (IBOMisTransactionCodes) factory.findByPrimaryKey(IBOMisTransactionCodes.BONAME, misCode, true);
		return misCodeBO;
		
    }
    
    public static IBOSwtCustomerDetail getSwtCustomerDetail(String custCode) {
    	IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
    	IBOSwtCustomerDetail swtCustDetailBO = (IBOSwtCustomerDetail) factory.findByPrimaryKey(IBOSwtCustomerDetail.BONAME, custCode);
    	
    	return swtCustDetailBO;
    }
    
    public static String getCustomerCode(String accountId) {
		IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
		String custId = "";
		IBOAttributeCollectionFeature accountIbo =  (IBOAttributeCollectionFeature)factory.findByPrimaryKey(IBOAttributeCollectionFeature.BONAME, accountId, true);
		if (null != accountIbo) {
			custId = accountIbo.getF_CUSTOMERCODE();
		}
		return custId;
	}

}
