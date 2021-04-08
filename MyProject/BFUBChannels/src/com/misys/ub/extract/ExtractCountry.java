package com.misys.ub.extract;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.types.ExtractCountriesDetail;
import bf.com.misys.cbs.types.ExtractCountriesOutput;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractCountriesRs;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.bo.refimpl.IBOCountry;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_ExtractCountry;

public class ExtractCountry extends AbstractUB_TIP_ExtractCountry {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}
	private static final Log logger = LogFactory.getLog(ExtractCountry.class
			.getName());
	private IPersistenceObjectsFactory factory;

	String FECTH_COUNTRY_DETAILS = " SELECT IC." + IBOCountry.CAPITALCITY
			+ " AS " + IBOCountry.CAPITALCITY + " , IC."
			+ IBOCountry.COUNTRYEXPOSURE1 + " AS "
			+ IBOCountry.COUNTRYEXPOSURE1 + " , IC."
			+ IBOCountry.COUNTRYEXPOSURE2 + " AS "
			+ IBOCountry.COUNTRYEXPOSURE2 + " , IC."
			+ IBOCountry.COUNTRYEXPOSURE3 + " AS "
			+ IBOCountry.COUNTRYEXPOSURE3 + " , IC." + IBOCountry.COUNTRYID
			+ " AS " + IBOCountry.COUNTRYID + " , IC."
			+ IBOCountry.COUNTRYLIMIT1 + " AS " + IBOCountry.COUNTRYLIMIT1
			+ " , IC." + IBOCountry.COUNTRYLIMIT2 + " AS "
			+ IBOCountry.COUNTRYLIMIT2 + " , IC." + IBOCountry.COUNTRYLIMIT3
			+ " AS " + IBOCountry.COUNTRYLIMIT3 + " , IC."
			+ IBOCountry.COUNTRYNAME + " AS " + IBOCountry.COUNTRYNAME
			+ " , IC." + IBOCountry.CURRENCYDESCRIPTION + " AS "
			+ IBOCountry.CURRENCYDESCRIPTION + " , IC." + IBOCountry.IBANLENGTH
			+ " AS " + IBOCountry.IBANLENGTH + " , IC."
			+ IBOCountry.ISOCOUNTRYCODE + " AS " + IBOCountry.ISOCOUNTRYCODE
			+ " , IC." + IBOCountry.ISOCURRENCYCODE1 + " AS "
			+ IBOCountry.ISOCURRENCYCODE1 + " , IC."
			+ IBOCountry.ISOCURRENCYCODE2 + " AS "
			+ IBOCountry.ISOCURRENCYCODE2 + " , IC."
			+ IBOCountry.ISOCURRENCYCODE3 + " AS "
			+ IBOCountry.ISOCURRENCYCODE3 + " , IC." + IBOCountry.ISPRESCRIBED
			+ " AS " + IBOCountry.ISPRESCRIBED + " , IC."
			+ IBOCountry.ISREPORTABLE + " AS " + IBOCountry.ISREPORTABLE
			+ " , IC." + IBOCountry.PEOPLE + " AS " + IBOCountry.PEOPLE
			+ " , IC." + IBOCountry.SHORTCOUNTRY3CHR + " AS "
			+ IBOCountry.SHORTCOUNTRY3CHR + " , IC."
			+ IBOCountry.SHORTCOUNTRYCODE2CHR + " AS "
			+ IBOCountry.SHORTCOUNTRYCODE2CHR + " FROM " + IBOCountry.BONAME
			+ " IC " + " WHERE IC." + IBOCountry.COUNTRYID + " = ?";

	String crudMode = CommonConstants.EMPTY_STRING;
	String countryId = CommonConstants.EMPTY_STRING;

	public ExtractCountry(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {

		try {
			factory = BankFusionThreadLocal.getPersistanceFactory();
			int a=0;
			ExtractCountriesRs extractCountriesRs = new ExtractCountriesRs();
			extractCountriesRs.setRsHeader(new RsHeader());
			extractCountriesRs.getRsHeader().setMessageType("Country"); 
			ExtractCountriesOutput extractCountriesOutput = new ExtractCountriesOutput();
			ExtractCountriesDetail extractCountriesDetail = new ExtractCountriesDetail();
			ExtractCountriesDetail[] extractCountryDetail = new ExtractCountriesDetail[1];
			crudMode = getF_IN_mode();
			countryId = getF_IN_extractCountryRq().getExtractCountriesInput()
					.getCountryID();
			List<SimplePersistentObject> countryDataList = fetchCountryDetails();
			if (countryDataList != null && countryDataList.size() > 0) {
				for (SimplePersistentObject countryData : countryDataList) {
					extractCountriesDetail.setCountryCode(Integer
							.parseInt((String) countryData.getDataMap().get(
									IBOCountry.ISOCOUNTRYCODE)));
					extractCountriesDetail.setCountryID(((String) countryData
							.getDataMap().get(IBOCountry.SHORTCOUNTRYCODE2CHR)));
					extractCountriesDetail.setCountryName((String) countryData
							.getDataMap().get(IBOCountry.COUNTRYNAME));
					extractCountriesDetail.setCrudMode(crudMode);
					/* need to check for isocurrencyCode 1,2,3 */
					extractCountriesDetail.setCurrencyCode((String) countryData
							.getDataMap().get(IBOCountry.ISOCURRENCYCODE1));
					extractCountriesDetail.setIbanLength((Integer) countryData
							.getDataMap().get(IBOCountry.IBANLENGTH));
					// extractCountriesDetail.setNationality();

				}
			}
			extractCountryDetail[0] = extractCountriesDetail;
			extractCountriesOutput
					.setExtractCountriesDetails(extractCountryDetail);
			extractCountriesOutput.setExtractMode(crudMode);
			extractCountriesRs
					.setExtractCountriesOutput(extractCountriesOutput);

			setF_OUT_extractCountryRs(extractCountriesRs);
		} catch (Exception e) {

			logger.error("Error in ExtractCountry.java for Primary Key "
					+ countryId + " Error is " + ExceptionUtil.getExceptionAsString(e));

			throw new BankFusionException(e);

		}

	}

	private List<SimplePersistentObject> fetchCountryDetails() {
		ArrayList<String> params = new ArrayList<String>();
		params.add(countryId);
		return factory.executeGenericQuery(FECTH_COUNTRY_DETAILS, params, null,
				true);

	}
}
