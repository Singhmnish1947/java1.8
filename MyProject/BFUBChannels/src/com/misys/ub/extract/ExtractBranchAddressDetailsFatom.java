package com.misys.ub.extract;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.types.BranchAddress;
import bf.com.misys.cbs.types.BranchDetailsMain;
import bf.com.misys.cbs.types.BranchDetailsShort;
import bf.com.misys.cbs.types.ExtractBranchDetail;
import bf.com.misys.cbs.types.ExtractBranchDetailsOutput;
import bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractBranchDetailsRs;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.IPersistenceObjectsFactory;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.documentManagment.dsx.SearchDocumentResults;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_Extract_Branch_Address_Details;

public class ExtractBranchAddressDetailsFatom extends
		AbstractUB_TIP_Extract_Branch_Address_Details {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String svnRevision = "$Revision: 1.0 $";
	private static final Log logger = LogFactory
			.getLog(ExtractBranchAddressDetailsFatom.class.getName());
	private IPersistenceObjectsFactory factory;
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}
	String branchShortCodePk = CommonConstants.EMPTY_STRING;
	String crudMode = CommonConstants.EMPTY_STRING;
	private static String queryForExtractBranchAddrDetails = "SELECT  brAddr."
			+ IBOBranch.BRANCHNAME + " AS BRANCHNAME, brAddr."
			+ IBOBranch.BANKNAME + " AS BANKNAME, brAddr." + IBOBranch.BICCODE
			+ " AS BICCODE, brAddr." + IBOBranch.BMBRANCH
			+ " AS BMBRANCH, brAddr." + IBOBranch.BRANCHSORTCODE
			+ " AS BRANCHSORTCODEPK, brAddr." + IBOBranch.BRANCHTIMEZONE
			+ " AS BRANCHTIMEZONE, brAddr." + IBOBranch.CLEARINGDAYS
			+ " AS CLEARINGDAYS, brAddr." + IBOBranch.CONTACTNUMBER
			+ " AS CONTACTNUMBER, brAddr." + IBOBranch.CONTACTPERSON
			+ " AS CONTACTPERSON, brAddr." + IBOBranch.CURRENTVALUE
			+ " AS CURRENTVALUE, brAddr." + IBOBranch.IMDCODE
			+ " AS IMDCODE, brAddr." + IBOBranch.ISOCOUNTRYCODE
			+ " AS ISOCOUNTRYCODE, brAddr." + IBOBranch.POSTZIPCODE
			+ " AS POSTZIPCODE, brAddr." + IBOBranch.RANGEFROM
			+ " AS RANGEFROM, brAddr." + IBOBranch.RANGETO
			+ " AS RANGETO, brAddr." + IBOBranch.ALPHACODE
			+ " AS ALPHACODE, brAddr." + IBOBranch.ADDRESSLINE1
			+ " AS ADDRESSLINE1, brAddr." + IBOBranch.ADDRESSLINE2
			+ " AS ADDRESSLINE2, brAddr." + IBOBranch.ADDRESSLINE3
			+ " AS ADDRESSLINE3, brAddr." + IBOBranch.ADDRESSLINE4
			+ " AS ADDRESSLINE4, brAddr." + IBOBranch.ADDRESSLINE5
			+ " AS ADDRESSLINE5, brAddr." + IBOBranch.ADDRESSLINE6
			+ " AS ADDRESSLINE6, brAddr." + IBOBranch.ADDRESSLINE7
			+ " AS ADDRESSLINE7, brAddr." + IBOBranch.ZONE + " AS ZONE FROM "
			+ IBOBranch.BONAME + " AS brAddr WHERE brAddr."
			+ IBOBranch.BRANCHSORTCODE + " = ? ";

	public ExtractBranchAddressDetailsFatom(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {
		try {
			factory = BankFusionThreadLocal.getPersistanceFactory();
			crudMode = getF_IN_CrudMode();
			branchShortCodePk = getF_IN_ExtractBranchAddrDetailsRq()
					.getExtractBranchDetailsInput().getBranchBusinessKey();

			ExtractBranchDetailsRs res = new ExtractBranchDetailsRs();
			int i = 0;
			ExtractBranchDetailsOutput branchDetailsOutput = new ExtractBranchDetailsOutput();
			BranchDetailsMain branchDetails = new BranchDetailsMain();
			BranchAddress address = new BranchAddress();
			BranchDetailsShort shortBranchDtls = new BranchDetailsShort();
			branchDetails.setBranchAddress(address);
			branchDetails.setBranchDetailsShort(shortBranchDtls);
			ExtractBranchDetail[] extractBranchDetailsArr = new ExtractBranchDetail[1];
			extractBranchDetailsArr[0] = new ExtractBranchDetail();
			extractBranchDetailsArr[0].setBranchDetailsMain(branchDetails);
			extractBranchDetailsArr[0].setCrudMode(crudMode);
			branchDetailsOutput
					.setExtractBranchDetails(extractBranchDetailsArr);
			res.setExtractBranchDetailsOutput(branchDetailsOutput);
			List<SimplePersistentObject> extractBrachDetails = ExtractBranchAddressDetails();

			if (extractBrachDetails != null && extractBrachDetails.size() > 0) {
				for (SimplePersistentObject branchDtls : extractBrachDetails) {
					branchDetails.setBicCode((String) branchDtls.getDataMap()
							.get("BICCODE"));
					branchDetails.getBranchAddress().setAddress1(
							(String) branchDtls.getDataMap()
									.get("ADDRESSLINE1"));
					branchDetails.getBranchAddress().setAddress2(
							(String) branchDtls.getDataMap()
									.get("ADDRESSLINE2"));
					branchDetails.getBranchAddress().setAddress3(
							(String) branchDtls.getDataMap()
									.get("ADDRESSLINE3"));
					branchDetails.getBranchAddress().setAddress4(
							(String) branchDtls.getDataMap()
									.get("ADDRESSLINE4"));
					branchDetails.getBranchAddress().setAddress5(
							(String) branchDtls.getDataMap()
									.get("ADDRESSLINE5"));
					branchDetails.getBranchAddress().setAddress6(
							(String) branchDtls.getDataMap()
									.get("ADDRESSLINE6"));
					branchDetails.getBranchAddress().setAddress7(
							(String) branchDtls.getDataMap()
									.get("ADDRESSLINE7"));
					branchDetails.getBranchDetailsShort().setBankName(
							(String) branchDtls.getDataMap().get("BANKNAME"));
					branchDetails.getBranchDetailsShort().setBranchCode(
							(String) branchDtls.getDataMap().get("BICCODE"));
					branchDetails.getBranchDetailsShort().setBranchName(
							(String) branchDtls.getDataMap().get("BRANCHNAME"));
					// branchDetails.getBranchDetailsShort().setHostBranchCode((String)branchDtls.getDataMap().get(IBOCB_BRN_BranchView.));
					branchDetails.getBranchDetailsShort().setImdCode(
							(String) branchDtls.getDataMap().get("IMDCODE"));
					branchDetails.getBranchDetailsShort().setIsoCountryCode(
							(String) branchDtls.getDataMap().get(
									"ISOCOUNTRYCODE"));
					branchDetails.getBranchDetailsShort()
							.setPostZipCode(
									(String) branchDtls.getDataMap().get(
											"POSTZIPCODE"));
					branchDetails.setBranchTimeZone((String) branchDtls
							.getDataMap().get("BRANCHTIMEZONE"));
					// String clearingVal = (String)
					// branchDtls.getDataMap().get(IBOBranch.CLEARINGDAYS);
					branchDetails.setClearingDays((Integer) branchDtls
							.getDataMap().get("CLEARINGDAYS"));
					branchDetails.setContactNumber((String) branchDtls
							.getDataMap().get("CONTACTNUMBER"));
					branchDetails.setContactPerson((String) branchDtls
							.getDataMap().get("CONTACTPERSON"));
					// String rangeFrom = (String)
					// branchDtls.getDataMap().get(IBOBranch.RANGEFROM);
					branchDetails.setRangeFrom((Integer) branchDtls
							.getDataMap().get("RANGEFROM"));
					// String rangeTo = (String)
					// branchDtls.getDataMap().get("RANGETO");
					branchDetails.setRangeTo((Integer) branchDtls.getDataMap()
							.get("RANGETO"));
				}
			}

			setF_OUT_ExtractBranchAddrDetailsRs(res);
		} catch (Exception e) {
			logger
					.error("Error in ExtractBranchAddressDetailsFatom.java for Primary Key "
							+ branchShortCodePk + " Error is "+ ExceptionUtil.getExceptionAsString(e));

			throw new BankFusionException();
		}
	}

	private List<SimplePersistentObject> ExtractBranchAddressDetails() {
		ArrayList<String> params = new ArrayList<String>();
		params.add(branchShortCodePk);
		return factory.executeGenericQuery(queryForExtractBranchAddrDetails,
				params, null, true);

	}
}
