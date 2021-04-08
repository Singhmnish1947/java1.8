
package com.trapedza.bankfusion.fatoms;

/* ***********************************************************************************
 * Copyright (c) 2003,2004 Trapedza Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Trapedza Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 * $Log: ATMReadPropertiesFatom.java,v $
 * Revision 1.3  2008/08/12 20:14:01  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.1.2.5  2008/07/31 20:55:28  manohart
 * Modified for bug fix 10870
 *
 * Revision 1.1.2.4  2008/07/21 20:51:19  manohart
 * Modified for bug fix 10751
 *
 * Revision 1.1.2.3  2008/07/16 16:13:01  manohart
 * Code cleanup - CVS revision tag added.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.util.BankFusionPropertySupport;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.GetUBConfigLocation;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractATMReadPropertiesFatom;
import com.trapedza.bankfusion.steps.refimpl.IATMReadPropertiesFatom;

public class ATMReadPropertiesFatom extends AbstractATMReadPropertiesFatom
		implements IATMReadPropertiesFatom {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	/**
	 */

	/**
	 * Logger instance
	 */
	private transient final static Log logger = LogFactory
			.getLog(ATMReadPropertiesFatom.class.getName());

	private final static String ATM_FILE_NAME = "atm.properties";

	private final static String ATM_DIRECTORY_NAME = "conf/business/atm";

	Properties atmProperties = new Properties();

	public ATMReadPropertiesFatom(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {
		try {
			// Call method to populate from atm.properties
			getATMProperties();

			// Set input file(BatchCompletionFile) path
			setF_OUT_atmFileUploadDir(atmProperties.getProperty(this.getF_IN_atmPropertyName()));
			setF_OUT_mergedFileName(atmProperties.getProperty(this.getF_IN_mergedFileNameProperty()));
			setF_OUT_batchFileName(atmProperties.getProperty(this.getF_IN_batchFileName()));
            setF_OUT_ipAddress(atmProperties.getProperty(this.getF_IN_ipAdress()));
            setF_OUT_crystalReportFatom(atmProperties.getProperty(this.getF_IN_crystalReportFatom()));

			if (getF_OUT_atmFileUploadDir() == null
					|| getF_OUT_batchFileName() == null
					|| getF_OUT_mergedFileName() == null) {
				/*throw new BankFusionException(7551,
						"Failed to load the ATM properties file. Please verify it's in correct path & contains all the required parameters.");*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_FAILED_TO_LOAD_THE_ATM_PROPERTIES_FILE, new Object[]{}, new HashMap(), env);
			}
		} catch (Exception e) {
			/*throw new BankFusionException(7551,
					"Failed to load the ATM properties file. Please verify it's in correct path & contains all the required parameters.");*/
			EventsHelper.handleEvent(ChannelsEventCodes.E_FAILED_TO_LOAD_THE_ATM_PROPERTIES_FILE, new Object[]{}, new HashMap(), env);
		logger.error(e);
		}

		// For displaying files list onto grid for files selection and upload.
		VectorTable fileList = new VectorTable();
		try{
            File dir = new File(getF_OUT_atmFileUploadDir());
            File[] files = dir.listFiles();
		HashMap map = new HashMap();
		String atmProperty = getF_IN_filePropertyATM();
		String spaProperty = getF_IN_filePropertySPA();

		String fileExtention = CommonConstants.EMPTY_STRING;
		if (atmProperty != null
				&& !atmProperty.equals(CommonConstants.EMPTY_STRING))
			fileExtention = atmProperties.getProperty(atmProperty);
		else if (spaProperty != null
				&& !spaProperty.equals(CommonConstants.EMPTY_STRING))
			fileExtention = atmProperties.getProperty(spaProperty);
		setF_OUT_fileExtension(fileExtention);
		int j = 0;
		for (int i = 0, n = files.length; i < n; i++) {
			if (files[i].isFile()
					&& (files[i].getName().endsWith(fileExtention))) {
				map.put("FileName", files[i].getName());
				map.put("DateReceived", new Timestamp(files[i].lastModified()));
				if (j == 0)
					map.put("Select", new Boolean(true));
				else
					map.put("Select", new Boolean(false));

				fileList.addAll(new VectorTable(map));
				j++;
			}
		}
		setF_OUT_fileList(fileList);
		setF_OUT_noOfUnProcessedFiles(new Integer(fileList.size()));
        }
		catch(Exception e){
           /* throw new BankFusionException(7551,
            "Failed to load the ATM properties file. Please verify it's in correct path & contains all the required parameters.");
           */
			EventsHelper.handleEvent(ChannelsEventCodes.E_FAILED_TO_LOAD_THE_ATM_PROPERTIES_FILE, new Object[]{}, new HashMap(), env);
        logger.error(e);
		}
	}

	/**
	 * Reads atm.properties & loads into Properties instance from
	 * BFconfigLocation or fontis/BFconfigLocation.
	 *  @
	 */
	private void getATMProperties() {
		/**
		 * for reading atm.properties
		 */
		InputStream is = null;

		/**
		 * dorectory of atm.properties file
		 */
		String configLocation = null;

		try {
			// Get file path from system properties & load into atmProperties
			// object
			/*configLocation = System.getProperty("BFconfigLocation",
					CommonConstants.EMPTY_STRING);*/
			configLocation = GetUBConfigLocation.getUBConfigLocation();

			is = new FileInputStream(configLocation + ATM_DIRECTORY_NAME
					+ File.separator + ATMReadPropertiesFatom.ATM_FILE_NAME);

			//Unreachable Code
			/*if (is == null) {
				is = new FileInputStream(configLocation + File.separator
						+ ATM_DIRECTORY_NAME + File.separator
						+ ATMReadPropertiesFatom.ATM_FILE_NAME);
			}

			if (is == null) {
				if (logger.isDebugEnabled())
					logger.debug(configLocation
							+ ATMReadPropertiesFatom.ATM_FILE_NAME
							+ " not found as file, trying as resource");
				is = this.getClass().getClassLoader().getResourceAsStream(
						"conf/business/atm/"
								+ ATMReadPropertiesFatom.ATM_FILE_NAME);
			}*/

			atmProperties.load(is);
		} catch (Exception ex) {
			// If not found then look into atm/BFconfigLocation for
			// atm.properties file
			if (is == null) {
				if (logger.isDebugEnabled())
					logger.debug(configLocation
							+ ATMReadPropertiesFatom.ATM_FILE_NAME
							+ " not found as file, trying as resource");
				is = this.getClass().getClassLoader().getResourceAsStream(
						"conf/business/atm/"
								+ ATMReadPropertiesFatom.ATM_FILE_NAME);
				logger.error(ex);
			}
		}
	}

}
