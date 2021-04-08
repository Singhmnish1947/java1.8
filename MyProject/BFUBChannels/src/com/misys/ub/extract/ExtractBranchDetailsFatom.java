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
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractBranchDetailsRs;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.IPersistenceObjectsFactory;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.bo.refimpl.IBOCB_BRN_BranchView;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_BRANCH;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_Extract_Branch_Details;

public class ExtractBranchDetailsFatom extends
		AbstractUB_TIP_Extract_Branch_Details {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Log logger = LogFactory
			.getLog(ExtractBranchDetailsFatom.class.getName());
	public static final String svnRevision = "$Revision: 1.0 $";

	private IPersistenceObjectsFactory factory;
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}
	String branchCodePk = CommonConstants.EMPTY_STRING;
	String crudMode = CommonConstants.EMPTY_STRING;
	

	public ExtractBranchDetailsFatom(BankFusionEnvironment env) {
		super(env);

	}
	private static String queryForExtractBranchDetails="SELECT  branch."
		+ IBOCB_BRN_BranchView.BRANCHNAME + " AS BRANCHNAME, branch."
		+ IBOCB_BRN_BranchView.BANKNAME + " AS BANKNAME, branch."
		+ IBOCB_BRN_BranchView.BICCODE + " AS BICCODE, branch."
		+ IBOCB_BRN_BranchView.BMBRANCH + " AS BMBRANCH, branch."
		+ IBOCB_BRN_BranchView.BRANCHBUSINESSKEY
		+ " AS BRANCHBUSINESSKEY, branch."
		+ IBOCB_BRN_BranchView.BRANCHSORTCODEPK
		+ " AS BRANCHSORTCODEPK, branch."
		+ IBOCB_BRN_BranchView.BRANCHTIMEZONE
		+ " AS BRANCHTIMEZONE, branch." + IBOCB_BRN_BranchView.CLEARINGDAYS
		+ " AS CLEARINGDAYS, branch." + IBOCB_BRN_BranchView.CONTACTNUMBER
		+ " AS CONTACTNUMBER, branch." + IBOCB_BRN_BranchView.CONTACTPERSON
		+ " AS CONTACTPERSON, branch." + IBOCB_BRN_BranchView.CURRENTVALUE
		+ " AS CURRENTVALUE, branch." + IBOCB_BRN_BranchView.IMDCODE
		+ " AS IMDCODE, branch." + IBOCB_BRN_BranchView.ISOCOUNTRYCODE
		+ " AS ISOCOUNTRYCODE, branch." + IBOCB_BRN_BranchView.POSTZIPCODE
		+ " AS POSTZIPCODE, branch." + IBOCB_BRN_BranchView.RANGEFROM
		+ " AS RANGEFROM, branch." + IBOCB_BRN_BranchView.RANGETO
		+ " AS RANGETO, branch." + IBOCB_BRN_BranchView.ALPHACODE
		+ " AS ALPHACODE, branch." + IBOCB_BRN_BranchView.ADDRESSLINE1
		+ " AS ADDRESSLINE1, branch." + IBOCB_BRN_BranchView.ADDRESSLINE2
		+ " AS ADDRESSLINE2, branch." + IBOCB_BRN_BranchView.ADDRESSLINE3
		+ " AS ADDRESSLINE3, branch." + IBOCB_BRN_BranchView.ADDRESSLINE4
		+ " AS ADDRESSLINE4, branch." + IBOCB_BRN_BranchView.ADDRESSLINE5
		+ " AS ADDRESSLINE5, branch." + IBOCB_BRN_BranchView.ADDRESSLINE6
		+ " AS ADDRESSLINE6, branch." + IBOCB_BRN_BranchView.ADDRESSLINE7
		+ " AS ADDRESSLINE7 , branchInterface."+IBOUB_INF_BRANCH.BRANCHCODE
		+ " AS TIBRANCHCODE "
		+ " FROM " + IBOCB_BRN_BranchView.BONAME
		+ " branch , "+IBOUB_INF_BRANCH.BONAME +" branchInterface "
		+ "  WHERE branch." +IBOCB_BRN_BranchView.BRANCHSORTCODEPK +" = branchInterface."+IBOUB_INF_BRANCH.UBBRANCHSORTCODE
		+" AND branchInterface."+IBOUB_INF_BRANCH.CHANNELID +" = 'TI' AND "
		+"branch." + IBOCB_BRN_BranchView.BRANCHSORTCODEPK
		+ " = ? ";
	
	public void process(BankFusionEnvironment env) throws BankFusionException {
		
		try {
			factory = BankFusionThreadLocal.getPersistanceFactory();
			crudMode = getF_IN_CrudMode();
			branchCodePk = getF_IN_ExtractBranchDetailsRq()
					.getExtractBranchDetailsInput().getBranchBusinessKey();

			ExtractBranchDetailsRs res = new ExtractBranchDetailsRs();
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
			res.setRsHeader(new RsHeader());
			res.getRsHeader().setMessageType("Branch"); 
			List<SimplePersistentObject> extractBrachDetails = ExtractBranchDetails();

			if (extractBrachDetails != null && extractBrachDetails.size() > 0) {
				for (SimplePersistentObject branchDtls : extractBrachDetails) {
					branchDetails.getBranchDetailsShort().setBranchCode(
							(String) branchDtls.getDataMap()
							.get("TIBRANCHCODE"));
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
					// branchDtls.getDataMap().get("CLEARINGDAYS");
					branchDetails.setClearingDays((Integer) branchDtls
							.getDataMap().get("CLEARINGDAYS"));
					branchDetails.setContactNumber((String) branchDtls
							.getDataMap().get("CONTACTNUMBER"));
					branchDetails.setContactPerson((String) branchDtls
							.getDataMap().get("CONTACTPERSON"));
					// String rangeFrom = (String)
					// branchDtls.getDataMap().get("RANGEFROM");
					branchDetails.setRangeFrom((Integer) branchDtls
							.getDataMap().get("RANGEFROM"));
					// String rangeTo = (String)
					// branchDtls.getDataMap().get("RANGETO");
					branchDetails.setRangeTo((Integer) branchDtls.getDataMap()
							.get("RANGETO"));

				}
			}
			setF_OUT_extractBranchDetailsRs(res);
		} catch (Exception e) {

			logger
					.error("Error in ExtractBranchDetailsFatom.java for Primary Key "
							+ branchCodePk + " Error is " + ExceptionUtil.getExceptionAsString(e));

			throw new BankFusionException(e);
		}

	}

	private List<SimplePersistentObject> ExtractBranchDetails() {
		ArrayList<String> params = new ArrayList<String>();
		params.add(branchCodePk);
		return factory.executeGenericQuery(queryForExtractBranchDetails,
				params, null, true);

	}
}
