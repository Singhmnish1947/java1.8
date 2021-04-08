/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: CB_TIP_ExchangeRateTypes,v.1.0,April 20, 2012 11:35:34 AM Ayyappa
 *
 */
package com.misys.ub.extract;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.types.ExRtTypDetail;
import bf.com.misys.cbs.types.ExtractExRateTypesOutput;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractExRateTypesRs;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.bo.refimpl.IBOCBVW_GENERICCODE;
import com.trapedza.bankfusion.bo.refimpl.IBOExchangeRates;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_ExchangeRateTypes;

public class ExtractExchangeRateTypes extends AbstractUB_TIP_ExchangeRateTypes {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	private static final Log logger = LogFactory
			.getLog(ExtractExchangeRateTypes.class.getName());
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private IPersistenceObjectsFactory factory;

	String FECTH_EXCHANGERATETYPE_DETAILS = " SELECT IER."
			+ IBOExchangeRates.CURRENCYRATEID + " AS "
			+ IBOExchangeRates.CURRENCYRATEID + " , IER."
			+ IBOExchangeRates.EXCHANGERATETYPE + " AS "
			+ IBOExchangeRates.EXCHANGERATETYPE + " , IER."
			+ IBOExchangeRates.RATE + " AS " + IBOExchangeRates.RATE
			+ "	, ICG." + IBOCBVW_GENERICCODE.DESCRIPTION + " AS "
			+ IBOCBVW_GENERICCODE.DESCRIPTION + "	, ICG."
			+ IBOCBVW_GENERICCODE.CODETYPEID + " AS "
			+ IBOCBVW_GENERICCODE.CODETYPEID + " FROM "
			+ IBOExchangeRates.BONAME + " IER ," + " "
			+ IBOCBVW_GENERICCODE.BONAME + " ICG " + " WHERE ICG."
			+ IBOCBVW_GENERICCODE.SUBCODETYPE + " = IER."
			+ IBOExchangeRates.EXCHANGERATETYPE + " AND ICG."
			+ IBOCBVW_GENERICCODE.CODETYPE + " = ?" + " AND IER."
			+ IBOExchangeRates.CURRENCYRATEID + " = ?";

	String codeType = CommonConstants.EMPTY_STRING;
	String crudMode = CommonConstants.EMPTY_STRING;
	String curRateID = CommonConstants.EMPTY_STRING;

	public ExtractExchangeRateTypes(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {

		try {
			factory = BankFusionThreadLocal.getPersistanceFactory();
			ExtractExRateTypesRs extractExRateTypesRs = new ExtractExRateTypesRs();
			extractExRateTypesRs.setRsHeader(new RsHeader());
			extractExRateTypesRs.getRsHeader().setMessageType("ExchangeRateType");
			ExtractExRateTypesOutput extractExRateTypesOutput = new ExtractExRateTypesOutput();
			ExRtTypDetail exRtTypDetail = new ExRtTypDetail();

			crudMode = getF_IN_mode();
			codeType = "EXCHRATETYPE";
			curRateID = getF_IN_extractExRateTypesRq()
					.getExtractExRateTypesInput().getExRateTypeId();
			List<SimplePersistentObject> exchangeRateData = fetchExchangeRateTypeDetails();
			if (exchangeRateData != null && exchangeRateData.size() > 0) {
				for (SimplePersistentObject exhangeRate : exchangeRateData) {
					exRtTypDetail.setCrudMode(crudMode);
					exRtTypDetail.setExRateType((String) exhangeRate
							.getDataMap()
							.get(IBOExchangeRates.EXCHANGERATETYPE));
					exRtTypDetail.setExRateDesc((String) exhangeRate
							.getDataMap().get(IBOCBVW_GENERICCODE.DESCRIPTION));
					exRtTypDetail.setExRateTypeId((String) exhangeRate
							.getDataMap().get(IBOCBVW_GENERICCODE.CODETYPEID));

					/*
					 * exRtTypDetail.setExRateCat(exRateCat);
					 * exRtTypDetail.setExRateTypeId(exRateTypeId);
					 */
				}
			}

			extractExRateTypesOutput.setExchangeRateTypeDtls(exRtTypDetail);
			extractExRateTypesRs
					.setExtractExRateTypesOutput(extractExRateTypesOutput);
			setF_OUT_extractExRateTypesRs(extractExRateTypesRs);
		} catch (Exception e) {
			logger
					.error("Error in ExtractExchangeRateTypes.java for Primary Key "
							+ curRateID + " Error is " + ExceptionUtil.getExceptionAsString(e));

			throw new BankFusionException(e);
		}

	}

	private List<SimplePersistentObject> fetchExchangeRateTypeDetails() {
		ArrayList<String> params = new ArrayList<String>();
		params.add(codeType);
		params.add(curRateID);
		return factory.executeGenericQuery(FECTH_EXCHANGERATETYPE_DETAILS,
				params, null, true);

	}
}
