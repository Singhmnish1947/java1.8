package com.misys.ub.fatoms;

import java.util.ArrayList;
import java.util.List;

import bf.com.misys.cbs.types.eventdetails.ExchRateDetails;
import bf.com.misys.cbs.types.eventdetails.ExchangeRateList;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOExchangeRates;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_CMN_ExchangeRatePushHandler;

public class UB_CMN_ExchangeRatePushHandler extends AbstractUB_CMN_ExchangeRatePushHandler {

	private IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
	
	private static final String FIND_ALL_RECORDS_FOR_CURRENCY_PAIR = " where " + IBOExchangeRates.FROMCURRENCYCODE + " = ? AND " + IBOExchangeRates.TOCURRENCYCODE + " = ?";
	
	public UB_CMN_ExchangeRatePushHandler(BankFusionEnvironment env) {
		super(env);
	}

	public UB_CMN_ExchangeRatePushHandler() {
		super();
	}

	@Override
	public void process(BankFusionEnvironment env) throws BankFusionException {
		
		String exchangeRateRecordId = getF_IN_ExchangeRateRecordId();
		IBOExchangeRates iboExchangeRates = (IBOExchangeRates)factory.findByPrimaryKey(IBOExchangeRates.BONAME,exchangeRateRecordId , true);
		if(iboExchangeRates != null) {
			String fromCurrency = iboExchangeRates.getF_FROMCURRENCYCODE();
			String toCurrency = iboExchangeRates.getF_TOCURRENCYCODE();
			ArrayList<String> params = new ArrayList<>();
			params.add(fromCurrency);
			params.add(toCurrency);
			List<IBOExchangeRates> exchangeRates = factory.findByQuery(IBOExchangeRates.BONAME, FIND_ALL_RECORDS_FOR_CURRENCY_PAIR, params, null, true);
			if(exchangeRates != null && !exchangeRates.isEmpty()) {
				ExchangeRateList exchangeRateList = new ExchangeRateList();
				for(IBOExchangeRates iboRates : exchangeRates) {
					ExchRateDetails exchRateDetails = new ExchRateDetails();
					exchRateDetails.setCURRENCYRATEID(iboRates.getBoID());
					exchRateDetails.setEXCHANGERATETYPE(iboRates.getF_EXCHANGERATETYPE());
					exchRateDetails.setFROMCURRENCYCODE(iboRates.getF_FROMCURRENCYCODE());
					exchRateDetails.setLIMIT1(iboRates.getF_LIMIT1());
					exchRateDetails.setLIMIT2(iboRates.getF_LIMIT2());
					exchRateDetails.setLIMIT3(iboRates.getF_LIMIT3());
					exchRateDetails.setLIMIT4(iboRates.getF_LIMIT4());
					exchRateDetails.setLIMIT5(iboRates.getF_LIMIT5());
					exchRateDetails.setLIMIT6(iboRates.getF_LIMIT6());
					exchRateDetails.setMULTIPLYDIVIDE(iboRates.getF_MULTIPLYDIVIDE());
					exchRateDetails.setRATE(iboRates.getF_RATE());
					exchRateDetails.setRATE2(iboRates.getF_RATE2());
					exchRateDetails.setRATE3(iboRates.getF_RATE3());
					exchRateDetails.setRATE4(iboRates.getF_RATE4());
					exchRateDetails.setRATE5(iboRates.getF_RATE5());
					exchRateDetails.setRATE6(iboRates.getF_RATE6());
					exchRateDetails.setREFER2(iboRates.isF_REFER2());
					exchRateDetails.setREFER3(iboRates.isF_REFER3());
					exchRateDetails.setREFER4(iboRates.isF_REFER4());
					exchRateDetails.setREFER5(iboRates.isF_REFER5());
					exchRateDetails.setREFER6(iboRates.isF_REFER6());
					exchRateDetails.setSPOTPERIODINDAYS(iboRates.getF_SPOTPERIODINDAYS());
					exchRateDetails.setTOCURRENCYCODE(iboRates.getF_TOCURRENCYCODE());
					exchRateDetails.setTOLERANCE(iboRates.getF_TOLERANCE());
					exchRateDetails.setUPDATEDATE(iboRates.getF_UPDATEDATE());
					exchRateDetails.setVERSIONNUM(iboRates.getVersionNum());
					exchangeRateList.addEXCHRATEDETAILS(exchRateDetails);
				}
				
				setF_OUT_ExchangeRateList(exchangeRateList);
			}
		}
	
	}

}
