/* ***********************************************************************************
 * Copyright (c) 2003,2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 */
package com.misys.ub.fatoms.batch.bpRefresh.accountNotes;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.cbs.common.util.log.CBSLogger;
import com.misys.ub.common.events.CommonsEventCodes;
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.process.AbstractBatchProcess;
import com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator;
import com.trapedza.bankfusion.batch.process.BatchProcessException;
import com.trapedza.bankfusion.bo.refimpl.IBOCB_NOT_Note;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_CMN_BatchProcessLog;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.utils.GUIDGen;

public class BranchPowerAccountNotesRefreshProcess extends AbstractBatchProcess {
	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $"; //$NON-NLS-1$
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}
	private IPersistenceObjectsFactory factory;
	private static final String CLASS_NAME = BranchPowerAccountNotesRefreshProcess.class
			.getName();
	private static final  Log logger = LogFactory.getLog(BranchPowerAccountNotesRefreshProcess.class.getName());

	public String extractPath = "";
	public String AccNotesRefFlag = "";

	String strHdrDt = "";
	private BankFusionEnvironment env;

	private AbstractProcessAccumulator accumulator;

	
	final String queryAccountNote = "SELECT (T1." + IBOCB_NOT_Note.NOTETITLE
			+ ") AS " + IBOCB_NOT_Note.NOTETITLE + " ," + "(T1."
			+ IBOCB_NOT_Note.LASTUPDATEDDTTM + ") AS "
			+ IBOCB_NOT_Note.LASTUPDATEDDTTM + ", " + "(T1."
			+ IBOCB_NOT_Note.ENTITYCODE + ") AS " + IBOCB_NOT_Note.ENTITYCODE
			+ " , (T1." + IBOCB_NOT_Note.NOTEDETAIL + ") AS "
			+ IBOCB_NOT_Note.NOTEDETAIL + " FROM " + IBOCB_NOT_Note.BONAME
			+ " T1 WHERE " + IBOCB_NOT_Note.NOTETYPE + " = 'NOTE' AND ("
			+ IBOCB_NOT_Note.EXPIRYDTTM + ">=?  OR "
			+ IBOCB_NOT_Note.EXPIRYDTTM + " IS NULL )";
	// TODO add column name for Advise teller, once added
	StringBuffer fileData = new StringBuffer();

	/**
	 * <code>mcfaData</code> Account Detail Record Structure class
	 */
	String MSG1 = CommonConstants.EMPTY_STRING;;

	Boolean Status;

	private String dateInString;

	private String timeInString;

	/**
	 * @param environment
	 *            Used to get a handle on the BankFusion environment
	 * @param context
	 *            A set of data passed to the PreProcess, Process and
	 *            PostProcess classes
	 * @param priority
	 *            Thread priority
	 */
	public BranchPowerAccountNotesRefreshProcess(
			BankFusionEnvironment environment, AbstractFatomContext context,
			Integer priority) {
		super(environment, context, priority);
		this.context = context;
		env = environment;
	}

	/**
	 * Initialise parameters and the accumulator for the BalanceSheetCollection
	 * process
	 * 
	 * @see com.trapedza.bankfusion.batch.process.AbstractBatchProcess#init()
	 */
	public void init() {

		initialiseAccumulator();
	}

	/**
	 * Gets a reference to the accumulator
	 * 
	 * @return A reference to the accumulator
	 * @see com.trapedza.bankfusion.batch.process.AbstractBatchProcess#getAccumulator()
	 */
	public AbstractProcessAccumulator getAccumulator() {
		return accumulator;
	}

	/**
	 * @see com.trapedza.bankfusion.batch.process.AbstractBatchProcess#initialiseAccumulator()
	 */
	protected void initialiseAccumulator() {
		Object[] accumulatorArgs = new Object[0];
		accumulator = new BranchPowerAccountNotesRefreshAccumulator(
				accumulatorArgs);
	}

	@SuppressWarnings("deprecation")
	public AbstractProcessAccumulator process(int pageToProcess)
			throws IllegalArgumentException, BatchProcessException,
			BankFusionException {
		if (logger.isInfoEnabled()) {
			logger.info("Invoking Page: " + pageToProcess);
		}

		Object[] additionalParameters = context.getAdditionalProcessParams();
		// fileProp object modified to local variable used to store the property
		// file details.
		Properties fileProp = (Properties) additionalParameters[0];

		if (fileProp == null || fileProp.size() == 0) {
			EventsHelper.handleEvent(
					CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { "Error Reading Properties File" },
					new HashMap<String, Object>(), env);
		}

		if ((getProperty("EXTRACTPATH", fileProp)!=null&&getProperty("EXTRACTPATH", fileProp).equalsIgnoreCase(""))
				|| (getProperty("ACCOUNT-NOTES-REFRESH", fileProp)
						.equalsIgnoreCase(""))) {

			EventsHelper.handleEvent(
					CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { "Invalid Parameters passed" },
					new HashMap<String, Object>(), env);
		}

		extractPath = getProperty("EXTRACTPATH", fileProp);
		AccNotesRefFlag = getProperty("ACCOUNT-NOTES-REFRESH", fileProp);
		pagingData.setCurrentPageNumber(pageToProcess);

		try {

			fileProp = (Properties) additionalParameters[1];

			if (AccNotesRefFlag.equals("1")) {
				refreshAccountNotes(env, fileProp);

			}
		} catch (BankFusionException exception) {

			logger.error(exception.getMessage());
		}
		return accumulator;

	}

	/**
	 * performs account notes refresh reads the account notes table, formats and
	 * writes the details to nad0data.dat
	 * 
	 * @param env
	 * @throws BankFusionException
	 */
	@SuppressWarnings("deprecation")
	private void refreshAccountNotes(BankFusionEnvironment env,
			Properties fileProp) throws BankFusionException {
		List accountNoteDetails = null;
		Date businessDate = SystemInformationManager.getInstance()
				.getBFBusinessDate();
		ArrayList<Date> params = new ArrayList<Date>();
		params.add(businessDate);
		accountNoteDetails = env.getFactory().executeGenericQuery(
				queryAccountNote, params, null, null);
		SimplePersistentObject accNotePO = null;
		FileOutputStream fout = null;
		String accountId = null;

		try {
			fout = new FileOutputStream(extractPath + "nad0data.dat");
			for (int i = 0; i < accountNoteDetails.size(); i++) {
				accNotePO = (SimplePersistentObject) accountNoteDetails.get(i);
				accountId = (String) accNotePO.getDataMap().get(
						IBOCB_NOT_Note.ENTITYCODE);
				dateInString = CommonConstants.EMPTY_STRING;
				timeInString = CommonConstants.EMPTY_STRING;
				if (accNotePO.getDataMap().get(IBOCB_NOT_Note.LASTUPDATEDDTTM) != null)
					getDateAndTimeForRefresh(accNotePO.getDataMap().get(
							IBOCB_NOT_Note.LASTUPDATEDDTTM));
				if (accNotePO.getDataMap().get(IBOCB_NOT_Note.ENTITYCODE) != null) {
					fileData = new StringBuffer();
					fileData.append(setField(
							new Integer(getProperty("ACN-FILLER1", fileProp))
									.intValue(), "192,@", 'A'));
					fileData.append(setField(
							new Integer(getProperty("ACN-ACCNO", fileProp))
									.intValue(), (String) accNotePO
									.getDataMap()
									.get(IBOCB_NOT_Note.ENTITYCODE), 'A'));
					fileData.append(setField(
							new Integer(getProperty("ACN-HOLD-CODE", fileProp))
									.intValue(), "", 'A'));
					fileData.append(setField(
							new Integer(getProperty("ACN-SYS-REF-NO", fileProp))
									.intValue(),
							((accNotePO.getDataMap()
									.get(IBOCB_NOT_Note.NOTETITLE)).toString()
									.length() > 15 ? (String) (accNotePO
									.getDataMap().get(IBOCB_NOT_Note.NOTETITLE))
									.toString().substring(0, 15)
									: (String) (accNotePO.getDataMap()
											.get(IBOCB_NOT_Note.NOTETITLE))),
							'A'));
					/**
					 * condition added for description greater than 50
					 * characters.
					 */
					fileData.append(setField(
							new Integer(getProperty("ACN-NOTE", fileProp))
									.intValue(),
							(accNotePO.getDataMap()
									.get(IBOCB_NOT_Note.NOTEDETAIL).toString()
									.length() > 50 ? accNotePO.getDataMap()
									.get(IBOCB_NOT_Note.NOTEDETAIL).toString()
									.substring(0, 50) : (String) accNotePO
									.getDataMap()
									.get(IBOCB_NOT_Note.NOTEDETAIL)), 'A'));
					fileData.append(setField(
							new Integer(getProperty("ACN-AMT-HELD", fileProp))
									.intValue(), "", 'A'));
					fileData.append(setField(
							new Integer(getProperty("ACN-DATE-STAMP", fileProp))
									.intValue(), dateInString, 'A'));
					fileData.append(setField(
							new Integer(getProperty("ACN-TIME-STAMP", fileProp))
									.intValue(), timeInString, 'A'));
					// TODO add advise teller code from CBTB_NOTE
					fileData.append(setField(
							new Integer(getProperty("ACN-ADVISE-TELLER-CODE",
									fileProp)).intValue(), "1", 'A'));
					fileData.append(setField(
							new Integer(getProperty("ACN-FILLER2", fileProp))
									.intValue(), "", 'A'));
					fileData.append("\r\n");
					fout.write(fileData.toString().getBytes());
					fout.flush();
				}
			}
			fout.close();
		} catch (FileNotFoundException fnfExcpn) {
			createLogMessage(accountId, fnfExcpn.getLocalizedMessage(), "E",
					factory);
			EventsHelper.handleEvent(
					CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { fnfExcpn.getLocalizedMessage() },
					new HashMap<String, Object>(), env);
		} catch (IOException ioExcpn) {
			createLogMessage(accountId, ioExcpn.getLocalizedMessage(), "E",
					factory);
			EventsHelper.handleEvent(
					CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { ioExcpn.getLocalizedMessage() },
					new HashMap<String, Object>(), env);
		} finally {
			try {
				fout.close();
			} catch (IOException e) {
				logger.error(e.getStackTrace());
			}
		}

	}

	@SuppressWarnings("deprecation")
	private void getDateAndTimeForRefresh(Object object) {
		String ddInStr = CommonConstants.EMPTY_STRING;
		String mmInStr = CommonConstants.EMPTY_STRING;
		String hhInStr = CommonConstants.EMPTY_STRING;
		String minInStr = CommonConstants.EMPTY_STRING;
		String ssInStr = CommonConstants.EMPTY_STRING;

		Date date = (Date) object;
		Integer dd = date.getDate();
		if (dd < 10) {
			ddInStr = "0" + dd.toString();
		} else {
			ddInStr = dd.toString();
		}
		Integer mm = date.getMonth() + 1;
		if (mm < 10) {
			mmInStr = "0" + mm.toString();
		} else {
			mmInStr = mm.toString();
		}
		Integer yy = CommonConstants.INTEGER_ZERO;
		if (date.getYear() > 100) {
			yy = date.getYear() - 100;
		} else {
			yy = date.getYear();
		}
		Integer hh = date.getHours();
		if (hh < 10) {
			hhInStr = "0" + hh.toString();
		} else {
			hhInStr = hh.toString();

		}

		Integer min = date.getMinutes();
		if (min < 10) {
			minInStr = "0" + min.toString();
		} else {
			minInStr = min.toString();
		}

		Integer ss = date.getSeconds();
		if (ss < 10) {
			ssInStr = "0" + ss.toString();
		} else
			ssInStr = ss.toString();

		dateInString = transformYear(yy.toString()) + mmInStr + ddInStr;
		timeInString = hhInStr + minInStr + ssInStr;

	}

	/**
	 * 
	 * @param yyInString
	 * @return
	 */
	private String transformYear(String yyInString) {
		String yearTransaformed = yyInString;

		String firstStr = yyInString.substring(0, 1);
		String secondStr = yyInString.substring(1, 2);

		yearTransaformed = getCharacter(firstStr).concat(
				getCharacter(secondStr));

		return yearTransaformed;
	}

	/**
	 * 
	 * @param str
	 * @return
	 */
	private String getCharacter(String str) {

		String spStr = str;

		Integer strInt = Integer.parseInt(str);
		switch (strInt) {
		case 0:
			spStr = "P";
			break;
		case 1:
			spStr = "Q";
			break;
		case 2:
			spStr = "R";
			break;
		case 3:
			spStr = "S";
			break;
		case 4:
			spStr = "T";
			break;
		case 5:
			spStr = "U";
			break;
		case 6:
			spStr = "V";
			break;
		case 7:
			spStr = "W";
			break;
		case 8:
			spStr = "X";
			break;
		case 9:
			spStr = "Y";
			break;

		default:
			spStr = str;

		}

		return spStr;

	}

	/**
	 * reads the property for the key passed
	 * 
	 * @param sKey
	 * @return
	 * 
	 */
	private static String getProperty(String sKey, Properties fileProp) {
		String sValue = null;

		sValue = fileProp.get(sKey).toString();

		return sValue;
	}

	/**
	 * This method formats fields input using the type ('A' or 'N') and length
	 * values passed. returns the formatted string back to calling method
	 * 
	 * @param ind
	 * @param string
	 * @param type
	 * @return
	 */
	private static String setField(int ind, String string, char type) {
		int count = 0;
		if (null != string) {
			count = string.length();
		}
		final StringBuffer sbuff = new StringBuffer();
		if (type == 'A') {
			sbuff.append(string);
		}
		for (int index = count; index < ind; index++) {
			if (type == 'A') {
				sbuff.append(" ");
			} else {
				sbuff.append("0");
			}
		}
		if (type == 'N') {
			sbuff.append(string);
		}
		return sbuff.toString();
	}

	/**
	 * This method is used to create log error message
	 * 
	 * @param key
	 * @param message
	 * @param status
	 */
	private void createLogMessage(String key, String message, String status,
			IPersistenceObjectsFactory factory) {
		
		IBOUB_CMN_BatchProcessLog batchException = (IBOUB_CMN_BatchProcessLog) factory
				.getStatelessNewInstance(IBOUB_CMN_BatchProcessLog.BONAME);
		batchException.setBoID(GUIDGen.getNewGUID());
		batchException.setF_PROCESSNAME(this.context.getBatchProcessName());
		batchException.setF_RUNDATETIME(SystemInformationManager.getInstance()
				.getBFBusinessDateTime(
						BankFusionThreadLocal.getBankFusionEnvironment()
								.getRuntimeMicroflowID()));
		batchException.setF_RECORDID(key);

		if (status.equalsIgnoreCase("E") || status.equalsIgnoreCase("W")) {
			if (logger.isErrorEnabled()) {
				logger.error(
						"Error processing for Account [ " + key
								+ " ] Reason :- " + message);
			}
			if (null == message) {
				message = CommonConstants.EMPTY_STRING;
			}
			message = message.replaceAll(",", "");
			message = message.replaceAll(":", "");
			message = message.replaceAll("':", "");

			batchException.setF_ERRORMESSAGE(message);
			batchException.setF_STATUS(status);
		} else {
			if (logger.isInfoEnabled()) {
				logger.info( "Unprocessed Account [ "
						+ key + " ] ");
			}
			batchException.setF_STATUS(status);
		}
		factory.create(IBOUB_CMN_BatchProcessLog.BONAME, batchException);
		if (logger.isInfoEnabled()) {
			logger.info( " End");
		}
	}
}
