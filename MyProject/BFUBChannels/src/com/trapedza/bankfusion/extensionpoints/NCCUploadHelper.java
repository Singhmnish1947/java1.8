package com.trapedza.bankfusion.extensionpoints;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import bf.com.misys.ub.types.NCCCodes;

import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_NCCCODES;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_NCCCODESLOG;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.utils.GUIDGen;

public class NCCUploadHelper {

	public static final String NCC_UPLOADFILENAME = "UploadFileName";

	public static final String NCC_UPLOADSTATUS = "UploadStatus";

	public static final String NCC_DELETECODES = " WHERE "
			+ IBOUB_SWT_NCCCODES.CLEARINGCODE + " = ?";

	public static final String NCC_DELETELOG = " WHERE "
			+ IBOUB_SWT_NCCCODESLOG.CLEARINGCODE + " = ?";

	ArrayList<String> uniqueIdentifiers = new ArrayList<String>();

	public static final String NCC_IDENTIFIER_CODE_EMPTY = "40409485";

	public static final String NCC_DUPLICATE_UNIQUE_IDENTIFIER = "40409486";

	public static final String NCC_FW_DATEFORMAT = "yyyyMMdd";

	public static final String NCC_SC_DATEFORMAT = "dd/MM/yyyy";

	public void deleteNCCCRecords(String nationalClearingCode) {

		deleteEntriesForNCCODE(nationalClearingCode);
		deleteEntriesForNCCLOG(nationalClearingCode);
	}

	/**
	 * Delete the entries of the NCCCODES TABLE
	 * 
	 * @param nccChoosen
	 * 
	 * 
	 * 
	 */
	private void deleteEntriesForNCCODE(String nationalClearingCode) {

		ArrayList<String> queryParams = new ArrayList<String>();
		queryParams.clear();
		queryParams.add(nationalClearingCode);

		IPersistenceObjectsFactory persistenceObjectfactory = null;
		persistenceObjectfactory = BankFusionThreadLocal
				.getPersistanceFactory();

		persistenceObjectfactory.bulkDelete(IBOUB_SWT_NCCCODES.BONAME,
				NCC_DELETECODES, queryParams);
	}

	/**
	 * Delete the entries of the NCCLOG TABLE
	 * 
	 * @param nccChoosen
	 * 
	 * 
	 * 
	 */
	private void deleteEntriesForNCCLOG(String nationalClearingCode) {

		ArrayList<String> queryParams = new ArrayList<String>();
		queryParams.clear();
		queryParams.add(nationalClearingCode);

		IPersistenceObjectsFactory persistenceObjectfactory = null;
		persistenceObjectfactory = BankFusionThreadLocal
				.getPersistanceFactory();

		persistenceObjectfactory.bulkDelete(IBOUB_SWT_NCCCODESLOG.BONAME,
				NCC_DELETELOG, queryParams);
	}

	private String checkDuplicateRecord(ArrayList<String> uniqueIdentifiers,
			String uniqueIdentifier) {
		String errorCode = "";
		if (uniqueIdentifiers.contains(uniqueIdentifier)) {
			errorCode = NCC_DUPLICATE_UNIQUE_IDENTIFIER;
		}

		return errorCode;
	}

	/**
	 * validate the NCC details and store
	 * 
	 * @param uniqueIdentifiers
	 * @param uniqueIdentifier
	 * 
	 * 
	 */
	public boolean validateAndStoreNCCCodes(
			HashMap<NCCUploadFields, String> mapOfNCCFlds, int lineNum) {

		String errorCode = validateNCCCodes(mapOfNCCFlds);
		String clearingCode = mapOfNCCFlds.get(NCCUploadFields.CLEARINGCODE);
		String identifierCode = mapOfNCCFlds
				.get(NCCUploadFields.IDENTIFIERCODE);
		String subBranchsuffix = mapOfNCCFlds
				.get(NCCUploadFields.SUBBRANCHSUFFIX);
		String uniqueIdentifier = clearingCode + identifierCode
				+ subBranchsuffix;
		boolean status = false;

		if (isEmptyOrNull(errorCode)) {
			NCCCodes nccCodes = prepareNCCComplexTypeObj(mapOfNCCFlds);
			status = storeNCCCodes(nccCodes);
			uniqueIdentifiers.add(uniqueIdentifier);
		} else {
			logException(errorCode, clearingCode, lineNum);
			status = false;
		}

		return status;

	}

	/**
	 * This method prepares the NCCComplex object from the field values.
	 * 
	 * @param mapOfNCCFlds
	 * 
	 */
	private NCCCodes prepareNCCComplexTypeObj(
			HashMap<NCCUploadFields, String> mapOfNCCFlds) {

		NCCCodes nccCodes = new NCCCodes();

		nccCodes
				.setClearingCode(mapOfNCCFlds.get(NCCUploadFields.CLEARINGCODE));
		nccCodes.setIdentifierCode(mapOfNCCFlds
				.get(NCCUploadFields.IDENTIFIERCODE));
		nccCodes.setSubBranchSuffix(mapOfNCCFlds
				.get(NCCUploadFields.SUBBRANCHSUFFIX));
		nccCodes.setBankShortName(mapOfNCCFlds
				.get(NCCUploadFields.BANKSHORTNAME));
		nccCodes
				.setBankLongName(mapOfNCCFlds.get(NCCUploadFields.BANKLONGNAME));
		nccCodes.setBranchName(mapOfNCCFlds.get(NCCUploadFields.BRANCHNAME));
		nccCodes.setBankCode(mapOfNCCFlds.get(NCCUploadFields.BANKCODE));
		nccCodes
				.setAddressLine1(mapOfNCCFlds.get(NCCUploadFields.ADDRESSLINE1));
		nccCodes
				.setAddressLine2(mapOfNCCFlds.get(NCCUploadFields.ADDRESSLINE2));

		SimpleDateFormat dateFormat = new SimpleDateFormat(NCC_FW_DATEFORMAT);

		java.util.Date convertedDate = null;
		if (!(isEmptyOrNull(mapOfNCCFlds.get(NCCUploadFields.REVISIONDATE)))) {
			if (mapOfNCCFlds.get(NCCUploadFields.CLEARINGCODE).equals("SC")) {
				dateFormat = new SimpleDateFormat(NCC_SC_DATEFORMAT);
			}
			try {
				convertedDate = dateFormat.parse(mapOfNCCFlds.get(
						NCCUploadFields.REVISIONDATE).trim());

			} catch (ParseException e) {

				e.printStackTrace();
			}
		}
		nccCodes.setRevisionDate(convertedDate);
		return nccCodes;
	}

	/**
	 * store the NCC details to database
	 * 
	 * @param NCCCodes
	 * 
	 * 
	 */
	private boolean storeNCCCodes(NCCCodes ncccCodes) {

		Hashtable startFatomData = new Hashtable();
		BankFusionEnvironment env = BankFusionThreadLocal
				.getBankFusionEnvironment();
		boolean status = true;
		try {
			if (ncccCodes != null) {
				startFatomData.put("NCCodes", ncccCodes);
				startFatomData.put("NCCCode", ncccCodes.getClearingCode());
				MFExecuter.executeMF("UB_SWT_InsertNCCCode_SRV", env,
						startFatomData);
			}
		} catch (BankFusionException be) {
			status = false;
		}
		return status;
	}

	/**
	 * validate the NCC details
	 * 
	 * @param uniqueIdentifiers
	 * @param uniqueIdentifier
	 * 
	 * 
	 */
	private String validateNCCCodes(
			HashMap<NCCUploadFields, String> mapOfNCCFlds) {
		String errorCode = "";
		if (isEmptyOrNull(mapOfNCCFlds.get(NCCUploadFields.IDENTIFIERCODE))) {
			errorCode = NCC_IDENTIFIER_CODE_EMPTY;
			return errorCode;
		}
		String clearingCode = mapOfNCCFlds.get(NCCUploadFields.CLEARINGCODE);
		String identifierCode = mapOfNCCFlds
				.get(NCCUploadFields.IDENTIFIERCODE);
		String subBranchsuffix = mapOfNCCFlds
				.get(NCCUploadFields.SUBBRANCHSUFFIX);
		String uniqueIdentifier = clearingCode + identifierCode
				+ subBranchsuffix;
		errorCode = checkDuplicateRecord(uniqueIdentifiers, uniqueIdentifier);

		return errorCode;
	}

	private boolean isEmptyOrNull(String str) {
		if (str == null || str.trim().length() == 0 || "null".equals(str))
			return true;
		return false;
	}

	private void logException(String errorCode, String clearingCode, int lineNum) {

		IPersistenceObjectsFactory factory = BankFusionThreadLocal
				.getPersistanceFactory();
		createLogException(errorCode, clearingCode, factory, lineNum);
	}

	private void createLogException(String errorCode, String clearingCode,
			IPersistenceObjectsFactory factory, int lineNum) {

		IBOUB_SWT_NCCCODESLOG nccException = (IBOUB_SWT_NCCCODESLOG) factory
				.getStatelessNewInstance(IBOUB_SWT_NCCCODESLOG.BONAME);

		nccException.setBoID(GUIDGen.getNewGUID());
		nccException.setF_CLEARINGCODE(clearingCode);
		nccException.setF_ERRORCODE(errorCode);
		nccException.setF_ERRORLINE(lineNum);
		nccException.setF_UPLOADDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
		// nccException.setVersionNum(0);

		factory.create(IBOUB_SWT_NCCCODESLOG.BONAME, nccException);

	}

}
