/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 */

package com.trapedza.bankfusion.fatoms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.ExtensionPointHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.extensionpoints.ExtensionPointUtils;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractATMCompletionFatom;
import com.trapedza.bankfusion.utils.BankFusionMessages;

public class ATMCompletionFatom extends AbstractATMCompletionFatom {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	/**
	 */

	private static final String REGISTRATION_MESSAGE = "0000000000000000000000997000000000000000000000000000000000000000000";

	private static final String DEREGISTRATION_MESSAGE = "0000000000000000000000930000000000000000000000000000000000000000000";

	private static final char MESSAGE_START = 2;

	private static final char MESSAGE_END = 3;

	private static final int OFFSET_FOR_MESSGAE_NUMBER = 22;

	private static final int LENGTH_OF_MESSGAE_NUMBER = 3;

	private static final String REGISTRATION_MESSAGE_NUMBER = "997";

	private static final String ATM = "A";

	private static final int OFFSET_FOR_AUTHORIZED_FLAG = 66;

	private static final char AUTHORIZED_FLAG_INDICATOR = '0';

	private static final String txnHistoryWhereClause = "WHERE "
			+ IBOTransaction.REFERENCE + "=?";

	private static final String findTxnCount = "SELECT COUNT(*) AS COUNTTXN FROM "
			+ IBOTransaction.BONAME + " where " + IBOTransaction.REFERENCE
			+ " = ? ";

	private BankFusionEnvironment environment;

	/**
	 * Holds the reference for logger object
	 */
	private transient final static Log logger = LogFactory
			.getLog(ATMCompletionFatom.class.getName());

	public ATMCompletionFatom(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {

		int portNumber = getF_IN_PortNumber().intValue();
		boolean proceed = true;
		long sent = 0;
		long recieved = 0;
		String ipAddress = getF_IN_IPAddress();
		String fileLocation = getF_IN_FileLocation();
		Boolean selectAll = isF_IN_SelectAll();
		String fileNameToProcess = null;
		String fileExtension = getF_IN_FileExtension();
		Timestamp startDateTime = SystemInformationManager.getInstance().getBFBusinessDateTime();
		setEnvironment(env);
		if (selectAll.booleanValue()) {
			String batchFile = getF_IN_BatchFileName();
			
			
					mergeAllFiles(batchFile);
				
			
			fileNameToProcess = getF_IN_MergedFileName();

		} else {
			fileNameToProcess = getF_IN_FileName();
		}

		Socket socket = null;
		FileInputStream inStream = null;
		PrintWriter toServer = null;
		BufferedReader bufInStrm =null;
		
		try {
			socket = new Socket(ipAddress, portNumber);
			inStream = new FileInputStream(fileLocation + fileNameToProcess);
			DataInputStream dataInStrm = new DataInputStream(inStream);
			bufInStrm = new BufferedReader(
					new InputStreamReader(dataInStrm));
			boolean tocontinue = true;
			toServer = new PrintWriter(socket.getOutputStream());
			final BufferedReader fromServer = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			final DataInputStream onServer = new DataInputStream(socket
					.getInputStream());
			VectorTable uploadSummary = new VectorTable();
			Map summaryMap = new HashMap();
			Map summaryReport = new HashMap();
			HashMap displayMap = new HashMap();
			StringBuffer regMessage = new StringBuffer();
			StringBuffer deRegMessage = new StringBuffer();
			String variableDataType = CommonConstants.EMPTY_STRING;
			String sourceCountryCode = CommonConstants.EMPTY_STRING;
			String sourceIMD = CommonConstants.EMPTY_STRING;
			String sourceBranchCode = CommonConstants.EMPTY_STRING;
			String deviceId = CommonConstants.EMPTY_STRING;
			String txnSequenceNo = CommonConstants.EMPTY_STRING;
			String dateTimeofTxn = CommonConstants.EMPTY_STRING;
			String messageStartsWith = CommonConstants.EMPTY_STRING;
			String reference = CommonConstants.EMPTY_STRING;
			char ext_Version = '1';
			
			if(fileExtension.equals(".spa")){
				ext_Version = '2';
			}
			else {
				ext_Version = '3';
			}
			

			// reg message
			regMessage.append(MESSAGE_START);
			regMessage.append(REGISTRATION_MESSAGE);
			regMessage.append(MESSAGE_END);
			// De-reg message
			deRegMessage.append(MESSAGE_START);
			deRegMessage.append(DEREGISTRATION_MESSAGE);
			deRegMessage.append(MESSAGE_END);

			toServer.print(regMessage.toString());
			toServer.flush();
			int temp = 0;
			String messageRecieved = CommonConstants.EMPTY_STRING;
			while ((temp = fromServer.read()) != MESSAGE_END) {
				if (temp != MESSAGE_START) {
					messageRecieved = messageRecieved + (char) temp;
				}
			}
			if (logger.isInfoEnabled()) {
				logger.info("ATM Transactions Uploading Started");
			}

			while (tocontinue) {
				String message = null;
				long results[] = { 0, 0, 1 };
				try {

					message = bufInStrm.readLine();// Reading message from the
													// file.
					if (message != null && !message.trim().equals("")
							&& !message.trim().equals("\n")) {
					
						String messageNumber = message.substring(
								OFFSET_FOR_MESSGAE_NUMBER,
								OFFSET_FOR_MESSGAE_NUMBER
										+ LENGTH_OF_MESSGAE_NUMBER);

						// start Code start for Duplicate transactions check for
						// ATM.

						variableDataType = message.substring(175, 176);
						sourceCountryCode = message.substring(7, 7 + 3);
						sourceIMD = message.substring(1, 1 + 6);
						sourceBranchCode = message.substring(10, 10 + 4);
						deviceId = message.substring(14, 14 + 4);
						txnSequenceNo = message.substring(18, 18 + 4);
						dateTimeofTxn = message.substring(26, 26 + 12);
						messageStartsWith = message.substring(
								OFFSET_FOR_MESSGAE_NUMBER,
								OFFSET_FOR_MESSGAE_NUMBER + 1);
						
						//Creating the Transaction reference
						reference = sourceCountryCode + sourceIMD
								+ sourceBranchCode + deviceId + txnSequenceNo
								+ dateTimeofTxn;
						if (!messageStartsWith.equals("0")
								&& !messageStartsWith.equals("8") && !messageStartsWith.equals("7")) {
							proceed = checkForDuplicates(env, reference);
							if (!proceed) {
								continue;
							}
						}
						// End Code End for Duplicate transactions check for
						// ATM.
						char[] chrArrayMessage = message.toCharArray();
						chrArrayMessage[177] = ext_Version;
						String modifiedMessage = new String(chrArrayMessage);
						
						StringBuffer requestMessage = new StringBuffer();
						requestMessage.append(MESSAGE_START);
						requestMessage.append(modifiedMessage);
						requestMessage.append(MESSAGE_END);
						toServer.print(requestMessage.toString()); // Request
																	// message
																	// to the
																	// server
						toServer.flush();
						Thread.sleep(4);
						int tmpNew = 0;
						sent = sent + 1;
						if (!REGISTRATION_MESSAGE_NUMBER.equals(messageNumber)) {
							if (summaryMap.containsKey(messageNumber)) {
								results = (long[]) summaryMap
										.get(messageNumber);
								results[2] = results[2] + 1;
							}
							summaryMap.put(messageNumber, results);
						}

						String responseMessage = CommonConstants.EMPTY_STRING;
						if (onServer.available() > 0) {
							while ((tmpNew = fromServer.read()) != MESSAGE_END) { // Response
																					// message
																					// from
																					// server
								if (tmpNew != MESSAGE_START) {
									responseMessage = responseMessage
											+ (char) tmpNew;
								}
							}
							recieved = recieved + 1;
						} else {
							continue;
						}

						char authFlag = responseMessage
								.charAt(OFFSET_FOR_AUTHORIZED_FLAG);
						String messageNo = responseMessage.substring(
								OFFSET_FOR_MESSGAE_NUMBER,
								OFFSET_FOR_MESSGAE_NUMBER
										+ LENGTH_OF_MESSGAE_NUMBER);

						if (!REGISTRATION_MESSAGE_NUMBER.equals(messageNo)) {
							if (summaryMap.containsKey(messageNo)) {
								results = (long[]) summaryMap.get(messageNo);
							}
							if (authFlag == AUTHORIZED_FLAG_INDICATOR) {
								results[0] = results[0] + 1;
							} else {
								results[1] = results[1] + 1;
							}
							summaryMap.put(messageNo, results);

						}

					} else {
						if (message == null) {
							tocontinue = false;
							break;
						} else if (message.trim().equals("")
								|| message.trim().equals("\n")) {
							continue;
						}
					}
				} catch (EOFException eof) {
					if (logger.isInfoEnabled()) {
						logger.info("Processing complete");
					}
					tocontinue = false;
					break;
				} catch (IOException e) {
					/*
					 * throw new BankFusionException(7553,
					 * "File reading failed: Check file permissions");
					 */
					EventsHelper
							.handleEvent(
									ChannelsEventCodes.E_PROCESSED_FILE_RENAMING_FAILED,
									new Object[] {}, new HashMap(), env);
				} catch (Exception e) {
					/*
					 * throw new BankFusionException(7553,
					 * "File reading failed: Check file permissions");
					 */
						logger.error("Error Processing Message cause :- ", e);
						logger.error("Error Processing for this Message :- " + message);

				}

			}
			
			int tmpNew = 0;
			boolean resultSentRec = false;
			boolean timeDiff = false;

			Timestamp currentDateTime;
			Timestamp endDateTime = SystemInformationManager.getInstance()
					.getBFSystemDateTime();
			//Calculating the maximum time for not received messages to send the response 
			long time = (sent - recieved) * 5000;
			long timeElapsed = endDateTime.getTime();
			timeElapsed = timeElapsed + time;
			endDateTime.setTime(timeElapsed);
			do {
				String responseMessage = CommonConstants.EMPTY_STRING;
				if (onServer.available() > 0) {
					long results[] = { 0, 0, 1 };
					while ((tmpNew = fromServer.read()) != MESSAGE_END) { // Response
																			// message
																			// from
																			// server
						if (tmpNew != MESSAGE_START) {
							responseMessage = responseMessage + (char) tmpNew;

						}
					}
					recieved = recieved + 1;
					char authFlag = responseMessage
							.charAt(OFFSET_FOR_AUTHORIZED_FLAG);
					String messageNo = responseMessage.substring(
							OFFSET_FOR_MESSGAE_NUMBER,
							OFFSET_FOR_MESSGAE_NUMBER
									+ LENGTH_OF_MESSGAE_NUMBER);

					if (!REGISTRATION_MESSAGE_NUMBER.equals(messageNo)) {
						if (summaryMap.containsKey(messageNo)) {
							results = (long[]) summaryMap.get(messageNo);
						}
						if (authFlag == AUTHORIZED_FLAG_INDICATOR) {
							results[0] = results[0] + 1;
						} else {
							results[1] = results[1] + 1;
						}
						summaryMap.put(messageNo, results);
					}
				}
				resultSentRec = sent == recieved;
				currentDateTime = SystemInformationManager.getInstance()
						.getBFSystemDateTime();
				long currTime = currentDateTime.getTime();

				timeDiff = currTime >= timeElapsed;

			} while (!resultSentRec && !timeDiff);
			Timestamp endDateTimeProcess = SystemInformationManager.getInstance().getBFBusinessDateTime(); 
			
			
			Iterator messageKeys = summaryMap.keySet().iterator();
			while (messageKeys.hasNext()) {
				Map summaryReportLoop = new HashMap();
				String key = (String) messageKeys.next();
				long[] resultArray = (long[]) summaryMap.get(key);
				if (logger.isInfoEnabled()) {
					logger.info("Message Number               :" + key);
					logger.info("No of Successful Transactions:"
							+ (resultArray[0]));
					logger.info("No of Failed Transactions    :"
							+ (resultArray[1]));
					logger.info("No of Total Transactions    :"
							+ (resultArray[2]));
				}

				displayMap.put("MessageNo", key);
				displayMap.put("success", "" + (resultArray[0]));
				displayMap.put("failed", "" + (resultArray[1]));
				uploadSummary.addAll(new VectorTable(displayMap));

				summaryReportLoop.put("Message Number", key);
				summaryReportLoop.put("No of Successful Transactions",
						new Integer((int) resultArray[0]));
				summaryReportLoop.put("No of Failed Transactions", new Integer(
						(int) (resultArray[1])));
				summaryReport.put("MessageNumber" + key, summaryReportLoop);
			}

			// Display upload summary
			setF_OUT_TransactionUploadSummary(uploadSummary);
			
			// Call crystal reports fatom
			generateSummaryReport(summaryReport, fileNameToProcess, env);

			generateFailedReport(endDateTimeProcess, startDateTime, fileExtension, env);

			toServer.print(deRegMessage.toString());
			if (logger.isInfoEnabled()) {
				logger.info("ATM Transactions Uploading Completed");
			}

			inStream.close();
			dataInStrm.close();
			bufInStrm.close();
			renameProcessedFiles(env);
		} catch (NullPointerException exception) {
			// throw new BankFusionException(7548,
			// "File name should not be null");
			EventsHelper.handleEvent(
					ChannelsEventCodes.E_FILE_NAME_SHOULD_NOT_BE_NULL,
					new Object[] {}, new HashMap(), env);
		} catch (UnknownHostException e) {
			// throw new BankFusionException(7549,
			// "Could Not Establish Connection With Host");
			EventsHelper
					.handleEvent(
							ChannelsEventCodes.E_COULD_NOT_ESTABLISH_CONNECTION_WITH_THE_HOST,
							new Object[] {}, new HashMap(), env);
		} catch (IOException e) {
			// throw new BankFusionException(7550,
			// "TCPConnection refused to connect to ATM port: Maximum number of connections reached");
			EventsHelper
					.handleEvent(
							ChannelsEventCodes.E_TCPCONN_REFUSED_TO_CONNECT_TO_ATM_PORT,
							new Object[] {}, new HashMap(), env);
		} finally {
			if (null != bufInStrm){
				try {
					bufInStrm.close();
				} catch (IOException e) {
					logger.error(ExceptionUtil.getExceptionAsString(e));
				}
			}
			if (toServer != null) {
				toServer.close();
			}
			if (socket != null)				
					try {
						socket.close();
					} catch (IOException e) {
							logger.error("Error Closing socket :-", e);
					}
				
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
						logger.error("Error Closing stream :-", e);
				}
			}
		}
	}

	// This process will call the renaming method based on the file uploading
	// selection.
	// If multiple file uploading selected it will rename all the processed
	// files
	private void renameProcessedFiles(BankFusionEnvironment env)
			throws BankFusionException {

		if (isF_IN_SelectAll().booleanValue()) {
			File dir = new File(getF_IN_FileLocation());
			File[] files = dir.listFiles();

			for (int i = 0, n = files.length; i < n; i++) {
				if (files[i].isFile()
						&& (files[i].getName()
								.endsWith(getF_IN_FileExtension()))) {
					renameFile(files[i].getName());
				}
			}
		} else {
			renameFile(getF_IN_FileName());
		}
	}

	// It will rename the file name with time stamp if the file uploading is
	// completed.
	private void renameFile(String fileName) throws BankFusionException {
		String newFileName = getF_IN_FileLocation() + fileName;
		String time = (SystemInformationManager.getInstance()
				.getBFBusinessDateTimeAsString()).replace(':', '-').replace(
				' ', '_');
		newFileName = newFileName + time + "_P";
		newFileName = newFileName.replace('\\', File.separatorChar);
		newFileName = newFileName.replace('/', File.separatorChar);
		File input = new File(getF_IN_FileLocation() + fileName);
		File newFile = new File(newFileName);
		try {
			input.renameTo(newFile);
		} catch (Exception e) {
			// throw new BankFusionException(7553,
			// "Processed file renaming failed: Please check folder permissions.");
			EventsHelper.handleEvent(
					ChannelsEventCodes.E_PROCESSED_FILE_RENAMING_FAILED,
					new Object[] {}, new HashMap(), getEnvironment());
		}
	}

	
	private void generateFailedReport(Timestamp endDateTimeProcess, Timestamp startDateTime, String fileExtension, BankFusionEnvironment env){
		Map params = new HashMap();
		params.put("EndDateTime", endDateTimeProcess);
		params.put("StartDateTime", startDateTime);
		params.put("FatFatom", true);
		if(fileExtension.equals(".atm")){
			params.put("POSORATM", "03");	
		}
		else{
			params.put("POSORATM", "02");
		}
		
		try {
			MFExecuter.executeMF(ATMConstants.BATCH_FAILED_REPORT, env, params);
		}catch (Exception exception) {
			/*String localErrormessage = BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 7538,
					env, new Object[] {});*/
			String localErrormessage = BankFusionMessages.getFormattedMessage(ChannelsEventCodes.E_ACTIVITY_LOG_TABLE_INSERTION_ERROR,
					 new Object[] {});
			logger.error(localErrormessage);
		}
	}
	
	
	
	// It will genarate Transaction Upload Summary Report.
	private void generateSummaryReport(Map summaryReport, String fileName,
			BankFusionEnvironment env) {
		String reportsFatom = getF_IN_CrystalReportFatom();
		ExtensionPointHelper reports = new ExtensionPointHelper("reports",
				reportsFatom, "reports");
		summaryReport.put(CrystalReportFatom.ReportId, getF_IN_ReportId());
		summaryReport.put(CrystalReportFatom.ReportName, getF_IN_ReportName());
		summaryReport
				.put(CrystalReportFatom.ReportTitle, getF_IN_ReportTitle());
		summaryReport.put(CrystalReportFatom.BranchName, env
				.getUserBranchName());
		summaryReport.put(CrystalReportFatom.DoSpool, Boolean.TRUE);
		summaryReport.put(CrystalReportFatom.DoPrint, isF_IN_Print());
		summaryReport.put(CrystalReportFatom.ViewReport, isF_IN_ViewSummary());
		summaryReport.put(CrystalReportFatom.IN_PARAMETER_1_NAME, fileName);

		// This extension point calls CrystalReportFatom for reports genaration.
		ExtensionPointUtils.executeExtensionPoint(env, summaryReport, reports);
	}

	// Execute the shell script for merging the all files into a new file for
	// multiple file uploading.
	public void mergeAllFiles(String script) throws BankFusionException {
		Runtime runtimeInfo = Runtime.getRuntime(); // Get runtime information
		Process child = null;
		try {
			child = runtimeInfo.exec(script); // Command to execute shell script
			BufferedWriter outCommand = new BufferedWriter(
					new OutputStreamWriter(child.getOutputStream()));
			outCommand.write("exit" + "\n");
			outCommand.flush();
			try {
				child.waitFor(); // Wait for command to complete
			} catch (InterruptedException e) {
				
				EventsHelper.handleEvent(
						ChannelsEventCodes.E_MULTIPLE_FILE_UPLOADING_FAILED,
						new Object[] {}, new HashMap(), getEnvironment());
			}
		} catch (IOException e) { // Handle exec failure
			
			EventsHelper
					.handleEvent(
							ChannelsEventCodes.E_FAILED_TO_LOAD_THE_ATM_PROPERTIES_FILE,
							new Object[] {}, new HashMap(), getEnvironment());
		} finally {
			if (child != null) {
				child.destroy();
			}
		}
	}

	private boolean checkForDuplicates(BankFusionEnvironment env,
			String reference) {
		boolean proceed = true;
		ArrayList params = new ArrayList();
		List transactionDetails = null;
		params.add(reference);
		long rowCount = 0;
		// find original transaction
		try {
			SimplePersistentObject persistentObject;
			List txnCount = BankFusionThreadLocal.getPersistanceFactory()
					.executeGenericQuery(findTxnCount, params, null, false);
			if (!txnCount.isEmpty()) {
				persistentObject = (SimplePersistentObject) txnCount.get(0);
				rowCount = (Long) persistentObject.getDataMap().get("COUNTTXN");
			}
			if (rowCount > 0) {
				proceed = false;
			} else {
				proceed = true;
			}
		} catch (BankFusionException bfe) {
			// if exception then not a duplicate message. Proceed to post.
			proceed = true;
		}

		return proceed;
	}
	
	
	
	public BankFusionEnvironment getEnvironment() {
		return environment;
	}

	public void setEnvironment(BankFusionEnvironment environment) {
		this.environment = environment;
	}

}
