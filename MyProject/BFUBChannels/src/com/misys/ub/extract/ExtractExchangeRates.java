/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: UB_TIP_ValidateModuleConfiguration.java,v.1.0,May 18, 2009 11:35:34 AM Apoorva
 *
 */
package com.misys.ub.extract;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.types.ExRtDetails;
import bf.com.misys.cbs.types.ExtractExchangeRatesOutput;
import bf.com.misys.cbs.types.RetailExRtDetails;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractExchangeRatesRs;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.bo.refimpl.IBOExchangeRates;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_ExchangeRates;

public class ExtractExchangeRates extends AbstractUB_TIP_ExchangeRates {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private static final Log logger = LogFactory
			.getLog(ExtractExchangeRates.class.getName());
	String currencyRATEID = CommonConstants.EMPTY_STRING;
	String crudMode = CommonConstants.EMPTY_STRING;

	private IPersistenceObjectsFactory factory;

	String FECTH_EXCHANGERATE_DETAILS = " SELECT IER."
			+ IBOExchangeRates.CURRENCYRATEID + " AS "
			+ IBOExchangeRates.CURRENCYRATEID + " , IER."
			+ IBOExchangeRates.EXCHANGERATETYPE + " AS "
			+ IBOExchangeRates.EXCHANGERATETYPE + " , IER."
			+ IBOExchangeRates.FROMCURRENCYCODE + " AS "
			+ IBOExchangeRates.FROMCURRENCYCODE + " , IER."
			+ IBOExchangeRates.MULTIPLYDIVIDE + " AS "
			+ IBOExchangeRates.MULTIPLYDIVIDE + " , IER."
			+ IBOExchangeRates.TOCURRENCYCODE + " AS "
			+ IBOExchangeRates.TOCURRENCYCODE + " , IER."
			+ IBOExchangeRates.TOLERANCE + " AS " + IBOExchangeRates.TOLERANCE
			+ " , IER." + IBOExchangeRates.RATE + " AS "
			+ IBOExchangeRates.RATE + " FROM " + IBOExchangeRates.BONAME
			+ " IER " + " WHERE IER." + IBOExchangeRates.CURRENCYRATEID
			+ " = ?";

	public ExtractExchangeRates(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {

		try {
			ExtractExchangeRatesRs exchangeRatesRs = new ExtractExchangeRatesRs();
			ExtractExchangeRatesOutput exchangeRatesOutput = new ExtractExchangeRatesOutput();
			exchangeRatesRs.setRsHeader(new RsHeader());
			exchangeRatesRs.getRsHeader().setMessageType("ExchangeRate");
			ExRtDetails[] exRtDetails = new ExRtDetails[1];
			exRtDetails[0] = new ExRtDetails();
			RetailExRtDetails retailExRtDetails = new RetailExRtDetails();
			factory = BankFusionThreadLocal.getPersistanceFactory();
			currencyRATEID = getF_IN_extractExchangeRatesRq()
					.getExtractExchangeRatesInput().getExtRateId();
			crudMode = getF_IN_mode();

			List<SimplePersistentObject> exchangeRateData = fetchExchangeRateDetails();
			if (exchangeRateData != null && exchangeRateData.size() > 0) {
				for (SimplePersistentObject exhangeRate : exchangeRateData) {

					/* 
					 * ******* NEED CALRIFICATION FOR SETTING BELOW VALUES
					 * retailExRtDetails.setActiveFlag(activeFlag);
					 * retailExRtDetails.setDateTime(dateTime);
					 * retailExRtDetails.setExRateCat(exRateCat);
					 * retailExRtDetails.setExchangeRate(exchangeRates);
					 * retailExRtDetails.setUnit(unit);
					 */
				
					retailExRtDetails.setExchangeRate((BigDecimal) exhangeRate
							.getDataMap()
							.get(IBOExchangeRates.RATE));
					retailExRtDetails.setExRateCat((String) exhangeRate
							.getDataMap()
							.get(IBOExchangeRates.CURRENCYRATEID));
					retailExRtDetails.setExchangeRateType((String) exhangeRate
							.getDataMap()
							.get(IBOExchangeRates.EXCHANGERATETYPE));
					retailExRtDetails.setFromCurrency((String) exhangeRate
							.getDataMap()
							.get(IBOExchangeRates.FROMCURRENCYCODE));
					retailExRtDetails
							.setToCurrency((String) (exhangeRate.getDataMap()
									.get(IBOExchangeRates.TOCURRENCYCODE)));
					retailExRtDetails
							.setTolerancePercenatge((BigDecimal) (exhangeRate
									.getDataMap()
									.get(IBOExchangeRates.TOLERANCE)));
					retailExRtDetails
							.setMultiplyOrDivide((String) (exhangeRate
									.getDataMap()
									.get(IBOExchangeRates.MULTIPLYDIVIDE)));

				}
			}else{
				//Here ExRateCat is considered as Primary Key which may not be correct but there is no other field.
				retailExRtDetails.setExRateCat(currencyRATEID);
			}

			/*		
			 * ******* NEED CALRIFICATION FOR SETTING BELOW VALUES
			 * 
			 * exRtDetails[0].setBankCode(bankCode);
			 * exRtDetails[0].setMargin(margin);
			 * exRtDetails[0].setMarginContextType(marginContextType);
			 * exRtDetails[0].setMarginContextValue(marginContextValue);
			 * exRtDetails[0].setSpreadPercentage(spreadPercentage);
			 * exRtDetails[0].setUseMargin(useMargin);
			 * exRtDetails[0].setUseSpreadPercentage(useSpreadPercentage);
			 */

			exRtDetails[0].setCrudMode(crudMode);
			exRtDetails[0].setExRtDetail(retailExRtDetails);
			exchangeRatesOutput.setExchangeRateDtls(exRtDetails);
			exchangeRatesRs.setExtractExchangeRatesOutput(exchangeRatesOutput);
			setF_OUT_extractExchangeRatesRs(exchangeRatesRs);
		} catch (Exception e) {
			logger.error("Error in ExtractExchangeRates.java for Primary Key "
					+ currencyRATEID + " Error is " + ExceptionUtil.getExceptionAsString(e));

			throw new BankFusionException(e);

		}
	}

	private List<SimplePersistentObject> fetchExchangeRateDetails() {
		ArrayList<String> params = new ArrayList<String>();
		params.add(currencyRATEID);
		return factory.executeGenericQuery(FECTH_EXCHANGERATE_DETAILS, params,
				null, true);
	}
}
