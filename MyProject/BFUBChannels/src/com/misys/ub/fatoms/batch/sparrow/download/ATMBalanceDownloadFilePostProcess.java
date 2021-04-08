/* ***********************************************************************************
 * Copyright (c) 2003,2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 * $Id: ATMBalanceDownloadFilePostProcess.java,v 1.1 2008/11/26 09:00:40 bhavyag Exp $
 *
 * $Log: ATMBalanceDownloadFilePostProcess.java,v $
 * Revision 1.1  2008/11/26 09:00:40  bhavyag
 * merging 3-3B changes for bug 12581.
 *
 * Revision 1.1.4.2  2008/09/23 08:09:50  mangesh
 * BUGID - 12581 - new Batch process for processing ATM Balance Download.
 *
 *
 */
package com.misys.ub.fatoms.batch.sparrow.download;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.misys.cbs.common.util.log.CBSLogger;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMHelper;
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator;
import com.trapedza.bankfusion.batch.process.IBatchPostProcess;
import com.trapedza.bankfusion.batch.process.engine.IBatchStatus;
import com.trapedza.bankfusion.boundary.outward.BankFusionIOSupport;
import com.trapedza.bankfusion.boundary.outward.BankFusionPropertySupport;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
/**
 * This class gets the number of records downloaded, merges all the files created by the process and generates
 * end message with number of records downloaded in this process execution.
 * 
 * The number record processed is put in the environment which would then be picked up by the ATM download Fatom 
 * and set it in the output tag. This value is further used by the report in the microflow.
 * 
 * 
 * @author Mangesh Hagargi
 *
 */
public class ATMBalanceDownloadFilePostProcess implements IBatchPostProcess {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	
	/**
	 */

	private IBatchStatus batchStatus;
	private static final String CLASS_NAME = ATMBalanceDownloadFilePostProcess.class.getName();
	private static final transient CBSLogger logger = new CBSLogger(CLASS_NAME);
	

	private ATMBalanceDownloadFileContext downloadContext = null;
	
	private BufferedOutputStream atmDownloadFile = null;
	
	private File atmDownloadDir = null;
	
	private File[] fileList = null;

	
	private int numOfRecords = 0;
	
	BankFusionEnvironment environment = null;
	
	private static final String FILEDIR = "ATMDownload.FileDir";
	private static final String ATMDOWNLOADFILE = "ATMDownload.FileName";
	private static int LENGTH_OF_AMOUNT = 14;
		
	public ATMBalanceDownloadFilePostProcess() {
		// TODO Auto-generated constructor stub
	}

	public void init(BankFusionEnvironment env, AbstractFatomContext context, IBatchStatus status)  {
		this.batchStatus = status;
		this.downloadContext = (ATMBalanceDownloadFileContext)context;
		this.environment = env;
	}

	public IBatchStatus process(AbstractProcessAccumulator accumulator) {
		
		//Object accumulatedTotals[] = accumulator.getMergedTotals();
		// Merge all the files in one file and then generate the Last line
		int iRecords = 0;
		
		ATMHelper atmHelper = new ATMHelper();
		try{
			openDownloadFile();
			readAndAppend();
	        Object[] accumulatedTotals = accumulator.getMergedTotals();
	        Map totalRecords = (Map) accumulatedTotals[0];
            Integer numOfRecord = (Integer) totalRecords.get("TotalRecord");
            iRecords = numOfRecord.intValue();
			StringBuffer endMessage = new StringBuffer();
			endMessage.append("19999999999999999999999999");
			String totalNumRecord = atmHelper.leftPad((String.valueOf(iRecords)), "0", LENGTH_OF_AMOUNT);

			endMessage.append("+");
			// append total number of Records
			endMessage.append(totalNumRecord);
			endMessage.append("\n");
			
			// Write End Message
			atmDownloadFile.write(endMessage.toString().getBytes());
			atmDownloadFile.flush();

			
			
		} catch(IOException ioe){
			// Exception occured while writing End Message, As well write error in the report for download. as this process
			// will be executed in the EOD.
			//throw new BankFusionException(7580,ioe.getLocalizedMessage());
			logger.error("process()","IO Exception is:"+ioe.getLocalizedMessage());
			EventsHelper.handleEvent(ChannelsEventCodes.E_ERROR_WHILE_WRITING_TO_THE_ATM_DOWNLOAD_FILE, new Object[]{ioe.getLocalizedMessage()}, new HashMap(), environment);
		}
		finally{
			closeDownloadFile();
		}
		
		
		downloadContext.setNumRecordsProcessed(iRecords);
		environment.putObject("ATMdownloadRecord", iRecords);
		batchStatus.setStatus(true);
		
		return batchStatus;
	}
	
	private void readAndAppend() {
		
		// TODO Auto-generated method stub
		String path = BankFusionPropertySupport.getProperty(BankFusionPropertySupport.UB_PROPERTY_FILE_NAME, FILEDIR, "");
		atmDownloadDir = new File(path);
		fileList = atmDownloadDir.listFiles(new ATMDownloadFileFilter());
		BufferedReader readFile = null;
		String record = "";
		try{
			for (File inputFile : fileList){
				try{
					readFile = new BufferedReader(new FileReader(inputFile));
					record = "";
					while((record=readFile.readLine())!= null){
						record = record+"\n";
						atmDownloadFile.write(record.getBytes());
						atmDownloadFile.flush();
					    numOfRecords++;
					}
				
				}catch(IOException ioe){
					logger.error("readAndAppend()"," Error in reading and appending Files caused by: "+ioe.getMessage());
				    //throw new BankFusionException(7580,ioe.getLocalizedMessage());
					EventsHelper.handleEvent(ChannelsEventCodes.E_ERROR_WHILE_WRITING_TO_THE_ATM_DOWNLOAD_FILE, new Object[]{ioe.getLocalizedMessage()}, new HashMap(), environment);
				}finally{
					if (readFile != null){
						readFile.close();
					}
					inputFile.delete();
				}
			}
		}catch(IOException ioe){
			logger.error("readAndAppend()"," Error in Mering Files caused by: "+ioe.getMessage());
			//throw new BankFusionException(7580,ioe.getLocalizedMessage());
			EventsHelper.handleEvent(ChannelsEventCodes.E_ERROR_WHILE_WRITING_TO_THE_ATM_DOWNLOAD_FILE, new Object[]{ioe.getLocalizedMessage()}, new HashMap(), environment);
		}		
	}

	private void openDownloadFile() {
		// Get the file name from the properties file
		String fileName = BankFusionPropertySupport.getProperty(BankFusionPropertySupport.UB_PROPERTY_FILE_NAME, ATMDOWNLOADFILE, "");
		File fout = new File(fileName);
		if (fout.exists()){
			fout.delete();
		}
		BankFusionIOSupport.createNewFile(fout);
		atmDownloadFile = new BufferedOutputStream(BankFusionIOSupport.createBufferedOutputStream(fout, true));
	}

     private void closeDownloadFile() {
		// TODO Auto-generated method stub
		if (atmDownloadFile != null){
			try{
				atmDownloadFile.close();
			}catch(Exception e){
				// ignore exception if unable to close.
				
			}
		}

		
	}
    


}
