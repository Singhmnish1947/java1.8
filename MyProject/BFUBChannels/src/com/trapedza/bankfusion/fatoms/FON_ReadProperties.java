/* ***********************************************************************************
 * Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * $Id: FON_ReadProperties.java,v 1.6 2008/08/12 20:14:04 vivekr Exp $
 * **********************************************************************************
 *
 * Revision 1.14  2008/02/16 14:37:17  Vinayachandrakantha.B.K
 * JavaDoc Comments added : For all the attributes
 */

package com.trapedza.bankfusion.fatoms;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractFON_ReadProperties;
import com.trapedza.bankfusion.steps.refimpl.IFON_ReadProperties;
import com.trapedza.bankfusion.core.EventsHelper;
import com.misys.bankfusion.common.util.BankFusionPropertySupport;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.GetUBConfigLocation;

/**
 * This Class contains methods for reading fontis.properties file for input path & EFT file output path.
 * @author Vinayachandrakantha.B.K
 *
 */
public class FON_ReadProperties extends AbstractFON_ReadProperties implements IFON_ReadProperties {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	/**
	 * Logger instance
	 */
	private transient final static Log logger = LogFactory.getLog(FON_ReadProperties.class.getName());

	/**
	 * File name
	 */
	private final static String FONTIS_PROPERTY_FILE_NAME = "fontis.properties";

	/**
	 * Fontis directory
	 */
	private final static String FONTIS_DIRECTORY_NAME = "conf/fontis";

	/**
	 * Properties class instance
	 */
	Properties fontisProperties = new Properties();

	/**
	 * Constructor
	 * @param env
	 */
	public FON_ReadProperties(BankFusionEnvironment env) {
		super(env);
	}

	/**
	 * implements process(...) method in AbstractFON_ReadProperties
	 */
	public void process(BankFusionEnvironment env) {
		try {
			//			Call method to populate from fontis.properties
			getFontisProperties();

			//			Set input file(IAT/TPP) path
			setF_OUT_FontisPropertyValue(fontisProperties.getProperty(this.getF_IN_FontisPropertyName()));

			//			Set EFT file output path
			setF_OUT_EFTOutputPathPropertyValue(fontisProperties.getProperty(this.getF_IN_EFTOutputPathPropertyName()));

			if (getF_OUT_FontisPropertyValue() == null || getF_OUT_EFTOutputPathPropertyValue() == null) {
				/*throw new BankFusionException(9057, null, logger, env);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_FAILED_TO_LOAD_FONTIS_PROPERTIES_FILE,new Object[] {} , new HashMap(), env);
			}
		}
		catch (Exception e) {
			/*throw new BankFusionException(9057, new Object[] { e.getLocalizedMessage() }, logger, env);*/
			EventsHelper.handleEvent(ChannelsEventCodes.E_FAILED_TO_LOAD_FONTIS_PROPERTIES_FILE,new Object[] {e.getLocalizedMessage()} , new HashMap(), env);
		logger.error(e);
		}
	}

	/**
	 * Reads fontis.properties & loads into Properties instance from BFconfigLocation or fontis/BFconfigLocation.
	 * @
	 */
	private void getFontisProperties() {
		/**
		 * for reading fontis.properties
		 */
		InputStream is = null;

		/**
		 * dorectory of fontis.properties file
		 */
		String configLocation = null;

		try {
			//			Get file path from system properties & load into fontisProperties object
			//configLocation = System.getProperty("BFconfigLocation", CommonConstants.EMPTY_STRING);
			configLocation = GetUBConfigLocation.getUBConfigLocation();
			//			is = new FileInputStream(configLocation + FON_ReadProperties.FONTIS_PROPERTY_FILE_NAME);
			is = new FileInputStream(configLocation + FONTIS_DIRECTORY_NAME + File.separator
					+ FON_ReadProperties.FONTIS_PROPERTY_FILE_NAME);

			//Unreachable code
			/*if (is == null) {
				is = new FileInputStream(configLocation + File.separator + FONTIS_DIRECTORY_NAME + File.separator
						+ FON_ReadProperties.FONTIS_PROPERTY_FILE_NAME);
			}

			if (is == null) {
				if (logger.isDebugEnabled())
					logger.debug(configLocation + FON_ReadProperties.FONTIS_PROPERTY_FILE_NAME
							+ " not found as file, trying as resource");
				is = this.getClass().getClassLoader().getResourceAsStream(
						FONTIS_DIRECTORY_NAME + FON_ReadProperties.FONTIS_PROPERTY_FILE_NAME);
			}
			*/

			fontisProperties.load(is);
		}
		catch (Exception ex) {
			//			If not found then look into fontis/BFconfigLocation for fontis.properties file
			if (is == null) {
				if (logger.isDebugEnabled())
					logger.debug(configLocation + FON_ReadProperties.FONTIS_PROPERTY_FILE_NAME
							+ " not found as file, trying as resource");
				is = this.getClass().getClassLoader().getResourceAsStream(
						FONTIS_DIRECTORY_NAME + FON_ReadProperties.FONTIS_PROPERTY_FILE_NAME);
			logger.error(ex);
			}
		}
	}
}
