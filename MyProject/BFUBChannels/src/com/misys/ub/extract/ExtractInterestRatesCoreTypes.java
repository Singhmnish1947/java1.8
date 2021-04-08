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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.types.ExtractIntRateCodesOutput;
import bf.com.misys.cbs.types.ExtractInterestRateDetail;
import bf.com.misys.cbs.types.RateCodeBasicDtls;
import bf.com.misys.cbs.types.RateCodeDetails;
import bf.com.misys.cbs.types.RateCodeTieredDtls;
import bf.com.misys.cbs.types.RateTierDtls;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractIntRateCodesRs;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.bo.refimpl.IBOInterestBaseCode;
import com.trapedza.bankfusion.bo.refimpl.IBOTieredInterestRate;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_ExtractInterestRatesCoreTypes;

public class ExtractInterestRatesCoreTypes extends
		AbstractUB_TIP_ExtractInterestRatesCoreTypes {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";

	private static final Log logger = LogFactory
			.getLog(ExtractInterestRatesCoreTypes.class.getName());
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private IPersistenceObjectsFactory factory;

	String FECTH_InterestRates_DETAILS = " SELECT IIBC."
			+ IBOInterestBaseCode.BASECODE + " AS "
			+ IBOInterestBaseCode.BASECODE + " , IIBC."
			+ IBOInterestBaseCode.ISOCURRENCYCODE + " AS "
			+ IBOInterestBaseCode.ISOCURRENCYCODE + " , IIBC."
			+ IBOInterestBaseCode.TIEREDINTEREST + " AS "
			+ IBOInterestBaseCode.TIEREDINTEREST + " , IIBC."
			+ IBOInterestBaseCode.BASEYEARDAYS + " AS "
			+ IBOInterestBaseCode.BASEYEARDAYS + " , IIBC."
			+ IBOInterestBaseCode.DESCRIPTION + " AS "
			+ IBOInterestBaseCode.DESCRIPTION + " , IIBC."
			+ IBOInterestBaseCode.INTRATE + " AS "
			+ IBOInterestBaseCode.INTRATE + " , ITIR."
			+ IBOTieredInterestRate.BASECODE + " AS "
			+ IBOTieredInterestRate.BASECODE + " FROM "
			+ IBOTieredInterestRate.BONAME + " ITIR , "
			+ IBOInterestBaseCode.BONAME + " IIBC " + " WHERE IIBC."
			+ IBOInterestBaseCode.BASECODE + " = " + "ITIR."
			+ IBOTieredInterestRate.BASECODE + " AND ITIR."
			+ IBOTieredInterestRate.TIEREDINTERESTRATEID + " = ? ";

	String tieredInterestRate = CommonConstants.EMPTY_STRING;
	String crudMode = CommonConstants.EMPTY_STRING;

	public ExtractInterestRatesCoreTypes(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {

		try {
			factory = BankFusionThreadLocal.getPersistanceFactory();

			ExtractIntRateCodesRs extractIntRateCodesRs = new ExtractIntRateCodesRs();
			extractIntRateCodesRs.setRsHeader(new RsHeader());
			extractIntRateCodesRs.getRsHeader().setMessageType("IntrestRates");

			ExtractIntRateCodesOutput[] extractIntRateCodesOutput = new ExtractIntRateCodesOutput[1];
			extractIntRateCodesOutput[0] = new ExtractIntRateCodesOutput();

			ExtractInterestRateDetail[] extractInterestRateDetail = new ExtractInterestRateDetail[1];
			extractInterestRateDetail[0] = new ExtractInterestRateDetail();

			RateCodeDetails[] rateCodeDetails = new RateCodeDetails[1];
			rateCodeDetails[0] = new RateCodeDetails();

			RateCodeBasicDtls rateCodeBasicDtls = new RateCodeBasicDtls();

			RateCodeTieredDtls[] rateCodeTieredDtls = new RateCodeTieredDtls[1];
			rateCodeTieredDtls[0] = new RateCodeTieredDtls();

			RateTierDtls[] rateTierDtls = new RateTierDtls[1];
			rateTierDtls[0] = new RateTierDtls();

			crudMode = getF_IN_mode();
			tieredInterestRate = getF_IN_extractIntRateCodesRq()
					.getExtractIntRateCodesInput().getRateCode();

			List<SimplePersistentObject> fetchInterestRateData = fetchInterestRateDetails();

			if (fetchInterestRateData != null
					&& fetchInterestRateData.size() > 0) {
				for (SimplePersistentObject exhangeInterestRate : fetchInterestRateData) {

					rateCodeBasicDtls.setCurrency((String) exhangeInterestRate
							.getDataMap().get(
									IBOInterestBaseCode.ISOCURRENCYCODE));
					rateCodeBasicDtls
							.setIntDaysBasis(CommonConstants.EMPTY_STRING
									+ exhangeInterestRate.getDataMap().get(
											IBOInterestBaseCode.BASEYEARDAYS));
					rateCodeBasicDtls
							.setInterestRate((BigDecimal) exhangeInterestRate
									.getDataMap().get(
											IBOInterestBaseCode.INTRATE));
					rateCodeBasicDtls.setIsTiered((Boolean) exhangeInterestRate
							.getDataMap().get(
									IBOInterestBaseCode.TIEREDINTEREST));
					rateCodeBasicDtls
							.setRateDescription((String) exhangeInterestRate
									.getDataMap().get(
											IBOInterestBaseCode.DESCRIPTION));

					rateTierDtls[0]
							.setBaseRateCode((String) exhangeInterestRate
									.getDataMap().get(
											IBOInterestBaseCode.BASECODE));
					rateTierDtls[0].setRate((BigDecimal) exhangeInterestRate
							.getDataMap().get(IBOInterestBaseCode.INTRATE));

				}
			}

			rateCodeTieredDtls[0].setRateTierDtls(rateTierDtls);

			rateCodeDetails[0].setRateCodeBasicDtls(rateCodeBasicDtls);
			rateCodeDetails[0].setRateCodeTieredDtls(rateCodeTieredDtls[0]);

			extractInterestRateDetail[0].setCrudMode(crudMode);
			if (fetchInterestRateData != null) {
				extractInterestRateDetail[0].setRateCode(
						(String) fetchInterestRateData.get(0).getDataMap().get(IBOInterestBaseCode.BASECODE));
			}		
			extractInterestRateDetail[0].setRateCodeDetails(rateCodeDetails[0]);

			extractIntRateCodesOutput[0]
					.setExtractInterestRateDetail(extractInterestRateDetail);

			extractIntRateCodesRs
					.setExtractIntRateCodesOutput(extractIntRateCodesOutput);

			setF_OUT_extractIntRateCodesRs(extractIntRateCodesRs);
		} catch (Exception e) {
			logger
					.error("Error in ExtractInterestRatesCoreTypes.java for Primary Key "
							+ tieredInterestRate + " Error is " + ExceptionUtil.getExceptionAsString(e));

			throw new BankFusionException(e);

		}

	}

	private List<SimplePersistentObject> fetchInterestRateDetails() {
		ArrayList<String> params = new ArrayList<String>();
		params.add(tieredInterestRate);
		return factory.executeGenericQuery(FECTH_InterestRates_DETAILS, params,
				null, true);

	}
}
