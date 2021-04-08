package com.misys.ub.extract;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.types.CurrencyCodeDetails;
import bf.com.misys.cbs.types.CurrencyDetails;
import bf.com.misys.cbs.types.CurrencyLongDetails;
import bf.com.misys.cbs.types.CurrencyShortDetails;
import bf.com.misys.cbs.types.ExtractCurrenciesDetail;
import bf.com.misys.cbs.types.ExtractCurrenciesOutput;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractCurrenciesRs;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.bo.refimpl.IBOCurrency;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_ExtractCurrency;

public class ExtractCurrency extends AbstractUB_TIP_ExtractCurrency {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";

	private static final Log logger = LogFactory.getLog(ExtractCurrency.class
			.getName());
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private IPersistenceObjectsFactory factory;

	String FECTH_CURRENCY_DETAILS = " SELECT IC." + IBOCurrency.COINSDENOMDESC1
			+ " AS " + IBOCurrency.COINSDENOMDESC1 + " , IC."
			+ IBOCurrency.COINSDENOMDESC10 + " AS "
			+ IBOCurrency.COINSDENOMDESC10 + " , IC."
			+ IBOCurrency.COINSDENOMDESC2 + " AS "
			+ IBOCurrency.COINSDENOMDESC2 + " , IC."
			+ IBOCurrency.COINSDENOMDESC3 + " AS "
			+ IBOCurrency.COINSDENOMDESC3 + " , IC."
			+ IBOCurrency.COINSDENOMDESC4 + " AS "
			+ IBOCurrency.COINSDENOMDESC4 + " , IC."
			+ IBOCurrency.COINSDENOMDESC5 + " AS "
			+ IBOCurrency.COINSDENOMDESC5 + " , IC."
			+ IBOCurrency.COINSDENOMDESC6 + " AS "
			+ IBOCurrency.COINSDENOMDESC6 + " , IC."
			+ IBOCurrency.COINSDENOMDESC7 + " AS "
			+ IBOCurrency.COINSDENOMDESC7 + " , IC."
			+ IBOCurrency.COINSDENOMDESC8 + " AS "
			+ IBOCurrency.COINSDENOMDESC8 + " , IC."
			+ IBOCurrency.COINSDENOMDESC9 + " AS "
			+ IBOCurrency.COINSDENOMDESC9 + " , IC." + IBOCurrency.COUNTRYNAME
			+ " AS " + IBOCurrency.COUNTRYNAME + " , IC."
			+ IBOCurrency.CREDITMULTIPLEOFCONSTANT + " AS "
			+ IBOCurrency.CREDITMULTIPLEOFCONSTANT + " , IC."
			+ IBOCurrency.CREDITROUNDINGMETHOD + " AS "
			+ IBOCurrency.CREDITROUNDINGMETHOD + " , IC."
			+ IBOCurrency.DEBITROUNDINGMETHOD + " AS "
			+ IBOCurrency.DEBITROUNDINGMETHOD + " , IC."
			+ IBOCurrency.CURRENCYNAME + " AS " + IBOCurrency.CURRENCYNAME
			+ " , IC." + IBOCurrency.CURRENCYSCALE + " AS "
			+ IBOCurrency.CURRENCYSCALE + " , IC."
			+ IBOCurrency.DEBITMULTIPLEOFCONSTANT + " AS "
			+ IBOCurrency.DEBITMULTIPLEOFCONSTANT + " , IC."
			+ IBOCurrency.DECIMALCHAR + " AS " + IBOCurrency.DECIMALCHAR
			+ " , IC." + IBOCurrency.DECIMALSTITLE + " AS "
			+ IBOCurrency.DECIMALSTITLE + " , IC." + IBOCurrency.DIGITTITLE01
			+ " AS " + IBOCurrency.DIGITTITLE01 + " , IC."
			+ IBOCurrency.DIGITTITLE02 + " AS " + IBOCurrency.DIGITTITLE02
			+ " , IC." + IBOCurrency.DIGITTITLE03 + " AS "
			+ IBOCurrency.DIGITTITLE03 + " , IC." + IBOCurrency.DIGITTITLE04
			+ " AS " + IBOCurrency.DIGITTITLE04 + " , IC."
			+ IBOCurrency.DIGITTITLE05 + " AS " + IBOCurrency.DIGITTITLE05
			+ " , IC." + IBOCurrency.DIGITTITLE06 + " AS "
			+ IBOCurrency.DIGITTITLE06 + " , IC." + IBOCurrency.DIGITTITLE07
			+ " AS " + IBOCurrency.DIGITTITLE07 + " , IC."
			+ IBOCurrency.DIGITTITLE08 + " AS " + IBOCurrency.DIGITTITLE08
			+ " , IC." + IBOCurrency.DIGITTITLE09 + " AS "
			+ IBOCurrency.DIGITTITLE09 + " , IC." + IBOCurrency.DIGITTITLE10
			+ " AS " + IBOCurrency.DIGITTITLE10 + " , IC."
			+ IBOCurrency.DIGITTITLE11 + " AS " + IBOCurrency.DIGITTITLE11
			+ " , IC." + IBOCurrency.DIGITTITLE12 + " AS "
			+ IBOCurrency.DIGITTITLE12 + " , IC." + IBOCurrency.DIGITTITLE13
			+ " AS " + IBOCurrency.DIGITTITLE13 + " , IC."
			+ IBOCurrency.DIGITTITLE14 + " AS " + IBOCurrency.DIGITTITLE14
			+ " , IC." + IBOCurrency.DIGITTITLE15 + " AS "
			+ IBOCurrency.DIGITTITLE15 + " , IC." + IBOCurrency.DIGITTITLE16
			+ " AS " + IBOCurrency.DIGITTITLE16 + " , IC."
			+ IBOCurrency.DIGITTITLE17 + " AS " + IBOCurrency.DIGITTITLE17
			+ " , IC." + IBOCurrency.DIGITTITLE18 + " AS "
			+ IBOCurrency.DIGITTITLE18 + " , IC." + IBOCurrency.ESTDATE
			+ " AS " + IBOCurrency.ESTDATE + " , IC." + IBOCurrency.INPUTMASK
			+ " AS " + IBOCurrency.INPUTMASK + " , IC." + IBOCurrency.ISACTIVE
			+ " AS " + IBOCurrency.ISACTIVE + " , IC."
			+ IBOCurrency.ISOCURRENCYCODE + " AS "
			+ IBOCurrency.ISOCURRENCYCODE + " , IC."
			+ IBOCurrency.ISTOURISTCURRENCY + " AS "
			+ IBOCurrency.ISTOURISTCURRENCY + " , IC."
			+ IBOCurrency.MINIMUMUNIT + " AS " + IBOCurrency.MINIMUMUNIT
			+ " , IC." + IBOCurrency.MULTIPLEOFCONSTANT + " AS "
			+ IBOCurrency.MULTIPLEOFCONSTANT + " , IC."
			+ IBOCurrency.NUMERICCODE + " AS " + IBOCurrency.NUMERICCODE
			+ " , IC." + IBOCurrency.NumericISOCurrencyCode + " AS "
			+ IBOCurrency.NumericISOCurrencyCode + " , IC."
			+ IBOCurrency.NOTESDENOMDESC1 + " AS "
			+ IBOCurrency.NOTESDENOMDESC1 + " , IC."
			+ IBOCurrency.NOTESDENOMDESC2 + " AS "
			+ IBOCurrency.NOTESDENOMDESC2 + " , IC."
			+ IBOCurrency.NOTESDENOMDESC3 + " AS "
			+ IBOCurrency.NOTESDENOMDESC3 + " , IC."
			+ IBOCurrency.NOTESDENOMDESC4 + " AS "
			+ IBOCurrency.NOTESDENOMDESC4 + " , IC."
			+ IBOCurrency.NOTESDENOMDESC5 + " AS "
			+ IBOCurrency.NOTESDENOMDESC5 + " , IC."
			+ IBOCurrency.NOTESDENOMDESC6 + " AS "
			+ IBOCurrency.NOTESDENOMDESC6 + " , IC."
			+ IBOCurrency.NOTESDENOMDESC7 + " AS "
			+ IBOCurrency.NOTESDENOMDESC7 + " , IC."
			+ IBOCurrency.NOTESDENOMDESC8 + " AS "
			+ IBOCurrency.NOTESDENOMDESC8 + " , IC."
			+ IBOCurrency.NOTESDENOMDESC9 + " AS "
			+ IBOCurrency.NOTESDENOMDESC9 + " , IC."
			+ IBOCurrency.NOTESDENOMDESC10 + " AS "
			+ IBOCurrency.NOTESDENOMDESC10 + " , IC." + IBOCurrency.OUTPUTMASK
			+ " AS " + IBOCurrency.OUTPUTMASK + " , IC."
			+ IBOCurrency.REPORTMASK + " AS " + IBOCurrency.REPORTMASK
			+ " , IC." + IBOCurrency.ROUNDINGMETHOD + " AS "
			+ IBOCurrency.ROUNDINGMETHOD + " , IC." + IBOCurrency.SEPARATORCHAR
			+ " AS " + IBOCurrency.SEPARATORCHAR + " , IC."
			+ IBOCurrency.SHORTDESCRIPTION + " AS "
			+ IBOCurrency.SHORTDESCRIPTION + " , IC."
			+ IBOCurrency.SWTADVICEDAYS + " AS " + IBOCurrency.SWTADVICEDAYS
			+ " , IC." + IBOCurrency.SWTCURRENCYINDICATOR + " AS "
			+ IBOCurrency.SWTCURRENCYINDICATOR + " , IC."
			+ IBOCurrency.THRESHOLDAMT + " AS " + IBOCurrency.THRESHOLDAMT
			+ " , IC." + IBOCurrency.TOLERANCE + " AS " + IBOCurrency.TOLERANCE
			+ " , IC." + IBOCurrency.YEARDAYS + " AS " + IBOCurrency.YEARDAYS
			+ " , IC." + IBOCurrency.ZONE + " AS " + IBOCurrency.ZONE
			+ " FROM " + IBOCurrency.BONAME + " IC " + " WHERE IC."
			+ IBOCurrency.ISOCURRENCYCODE + " = ?";

	String crudMode = CommonConstants.EMPTY_STRING;
	String currencyCode = CommonConstants.EMPTY_STRING;

	public ExtractCurrency(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {

		try {
			factory = BankFusionThreadLocal.getPersistanceFactory();
			ExtractCurrenciesRs extractCurrenciesRs = new ExtractCurrenciesRs();
			extractCurrenciesRs.setRsHeader(new RsHeader());
			extractCurrenciesRs.getRsHeader().setMessageType("Currency"); 
			ExtractCurrenciesOutput extractCurrenciesOutput = new ExtractCurrenciesOutput();
			ExtractCurrenciesDetail extractCurrenciesDetail = new ExtractCurrenciesDetail();
			ExtractCurrenciesDetail[] extractCurrencyDetail = new ExtractCurrenciesDetail[1];
			crudMode = getF_IN_crudMode();
			currencyCode = getF_IN_extractCurrencyRq()
					.getExtractCurrenciesInput().getIsoCurrencyCode();
			List<SimplePersistentObject> currencyDataList = fetchCurrencyDetails();
			if (currencyDataList != null && currencyDataList.size() > 0) {
				for (SimplePersistentObject currencyData : currencyDataList) {
					extractCurrenciesDetail.setActive((Boolean) currencyData
							.getDataMap().get(IBOCurrency.ISACTIVE));
					extractCurrenciesDetail
							.setCreditMultipleofconstant((Integer) currencyData
									.getDataMap()
									.get(IBOCurrency.CREDITMULTIPLEOFCONSTANT));
					/*extractCurrenciesDetail
							.setCreditRoundingMethod(CommonConstants.EMPTY_STRING
									+ currencyData.getDataMap().get(
											IBOCurrency.CREDITROUNDINGMETHOD));*/
					CurrencyCodeDetails currencyCodeDetails = new CurrencyCodeDetails();
					currencyCodeDetails
							.setIsoCurrencyCode((String) currencyData
									.getDataMap().get(
											IBOCurrency.ISOCURRENCYCODE));
					/*currencyCodeDetails.setNumericCode((Integer) currencyData
							.getDataMap().get(IBOCurrency.NUMERICCODE));*/
					CurrencyDetails currencyDetails = new CurrencyDetails();
					currencyDetails.setCurrencyCodeDetails(currencyCodeDetails);
					CurrencyLongDetails currencyLongDetails = new CurrencyLongDetails();
					currencyLongDetails
							.setCreditMultipleofconstant((Integer) currencyData
									.getDataMap()
									.get(IBOCurrency.CREDITMULTIPLEOFCONSTANT));
					/*currencyLongDetails
							.setCreditRoundingMethod(CommonConstants.EMPTY_STRING
									+ currencyData.getDataMap().get(
											IBOCurrency.CREDITROUNDINGMETHOD));*/
					currencyLongDetails.setCurrencyScale((Integer) currencyData
							.getDataMap().get(IBOCurrency.CURRENCYSCALE));
					currencyLongDetails
							.setDebitMultipleofConstant((Integer) currencyData
									.getDataMap()
									.get(IBOCurrency.DEBITMULTIPLEOFCONSTANT));
					/*currencyLongDetails
							.setDebitRoundingMethod(CommonConstants.EMPTY_STRING
									+ currencyData.getDataMap().get(
											IBOCurrency.DEBITROUNDINGMETHOD));*/
					currencyLongDetails
							.setDecimalTitle(CommonConstants.EMPTY_STRING
									+ currencyData.getDataMap().get(
											IBOCurrency.DECIMALSTITLE));
					BigDecimal bd = new BigDecimal((Integer) currencyData
							.getDataMap().get(IBOCurrency.MINIMUMUNIT));
					currencyLongDetails.setMinimumUnitsforCurrency(bd);

					currencyLongDetails
							.setSwiftAdviceDays((Integer) currencyData
									.getDataMap()
									.get(IBOCurrency.SWTADVICEDAYS));
					currencyLongDetails.setSwiftCurrencyIndicator(currencyData
							.getDataMap().get(IBOCurrency.SWTCURRENCYINDICATOR)
							.equals("Y") ? Boolean.TRUE : Boolean.FALSE);
					BigDecimal bd1 = new BigDecimal((Integer) currencyData
							.getDataMap().get(IBOCurrency.TOLERANCE));
					currencyLongDetails.setTolerancePercentage(bd1);

					currencyLongDetails
							.setTouristCurrency((Boolean) currencyData
									.getDataMap().get(
											IBOCurrency.ISTOURISTCURRENCY));
					currencyLongDetails.setYearDays((Integer) currencyData
							.getDataMap().get(IBOCurrency.YEARDAYS));
					extractCurrenciesDetail
							.setCurrencyCodeDetails(currencyCodeDetails);
					extractCurrenciesDetail
							.setCurrencyLongDetails(currencyLongDetails);
					extractCurrenciesDetail.setCurrencyDetails(currencyDetails);
					CurrencyShortDetails currencyShortDetails = new CurrencyShortDetails();
					currencyShortDetails.setActive((Boolean) currencyData
							.getDataMap().get(IBOCurrency.ISACTIVE));
					currencyShortDetails.setCurrencyName((String) currencyData
							.getDataMap().get(IBOCurrency.CURRENCYNAME));
					/*
					 * currencyShortDetails.setEnableForIP((String) currencyData
					 * .getDataMap().get(IBOCurrency.))
					 */
					currencyShortDetails
							.setShortDescription((String) currencyData
									.getDataMap().get(
											IBOCurrency.SHORTDESCRIPTION));
					extractCurrenciesDetail
							.setCurrencyShortDetails(currencyShortDetails);
					extractCurrenciesDetail
							.setCurrencyName((String) currencyData.getDataMap()
									.get(IBOCurrency.CURRENCYNAME));
					extractCurrenciesDetail
							.setCurrencyScale((Integer) currencyData
									.getDataMap()
									.get(IBOCurrency.CURRENCYSCALE));
					extractCurrenciesDetail
							.setDebitMultipleofConstant((Integer) currencyData
									.getDataMap()
									.get(IBOCurrency.DEBITMULTIPLEOFCONSTANT));
					/*extractCurrenciesDetail
							.setDebitRoundingMethod(CommonConstants.EMPTY_STRING
									+ currencyData.getDataMap().get(
											IBOCurrency.DEBITROUNDINGMETHOD));*/
					extractCurrenciesDetail
							.setDecimalTitle((String) currencyData.getDataMap()
									.get(IBOCurrency.DECIMALSTITLE));
					/*
					 * extractCurrenciesDetail.setEnableForIP((String)
					 * currencyData .getDataMap().get(IBOCurrency.));
					 */
					/*
					 * extractCurrenciesDetail.setEuroMember(((String)
					 * currencyData .getDataMap().get(IBOCurrency.));
					 */
					/*
					 * extractCurrenciesDetail.setExchangeRate((String)
					 * currencyData .getDataMap().get(IBOCurrency.));
					 */
					extractCurrenciesDetail
							.setIsoCurrencyCode((String) currencyData
									.getDataMap().get(
											IBOCurrency.ISOCURRENCYCODE));
					BigDecimal bd2 = new BigDecimal((Integer) currencyData
							.getDataMap().get(IBOCurrency.MINIMUMUNIT));
					extractCurrenciesDetail.setMinimumUnitsforCurrency(bd2);

					extractCurrenciesDetail
							.setNonSwiftAdvDays((Integer) currencyData
									.getDataMap()
									.get(IBOCurrency.SWTADVICEDAYS));
					/*extractCurrenciesDetail
							.setNumericCode((Integer) currencyData.getDataMap()
									.get(IBOCurrency.NUMERICCODE));*/
					extractCurrenciesDetail
							.setShortDescription((String) currencyData
									.getDataMap().get(
											IBOCurrency.SHORTDESCRIPTION));
					/*
					 * extractCurrenciesDetail.setSpotRateDetails((String)
					 * currencyData .getDataMap().get(IBOCurrency.));
					 */
					extractCurrenciesDetail
							.setSwiftAdviceDays((Integer) currencyData
									.getDataMap()
									.get(IBOCurrency.SWTADVICEDAYS));
					extractCurrenciesDetail
							.setSwiftCurrencyIndicator(currencyData
									.getDataMap().get(
											IBOCurrency.SWTCURRENCYINDICATOR)
									.equals("Y") ? Boolean.TRUE : Boolean.FALSE);
					/*
					 * extractCurrenciesDetail.setSwiftMnemonic((String)
					 * currencyData .getDataMap().get(IBOCurrency.));
					 */
					/*
					 * extractCurrenciesDetail
					 * .setTolerancePercentage((BigDecimal) currencyData
					 * .getDataMap().get(IBOCurrency.TOLERANCE));
					 */
					extractCurrenciesDetail
							.setTouristCurrency((Boolean) currencyData
									.getDataMap().get(
											IBOCurrency.ISTOURISTCURRENCY));
					extractCurrenciesDetail.setYearDays((Integer) currencyData
							.getDataMap().get(IBOCurrency.YEARDAYS));

				}
			} else {
				extractCurrenciesDetail.setIsoCurrencyCode(currencyCode);
			}
			extractCurrencyDetail[0] = extractCurrenciesDetail;
			extractCurrenciesOutput
					.setExtractCurrenciesDetails(extractCurrencyDetail);
			extractCurrenciesOutput.setExtractMode(crudMode);
			extractCurrenciesRs
					.setExtractCurrenciesOutput(extractCurrenciesOutput);

			setF_OUT_extractCurrencyRs(extractCurrenciesRs);
		} catch (Exception e) {

			logger.error("Error in ExtractCurrency.java for Primary Key "
					+ currencyCode + " Error is " + ExceptionUtil.getExceptionAsString(e));

			throw new BankFusionException(e);
		}

	}

	private List<SimplePersistentObject> fetchCurrencyDetails() {

		ArrayList<String> params = new ArrayList<String>();
		params.add(currencyCode);
		return factory.executeGenericQuery(FECTH_CURRENCY_DETAILS, params,
				null, true);

	}
}
