package com.trapedza.bankfusion.fatoms;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.types.events.Event;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.common.util.BankFusionPropertySupport;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.ub.common.GetUBConfigLocation;
import com.misys.ub.common.events.CommonsEventCodes;
import com.trapedza.bankfusion.core.BankFusionSystem;
import com.trapedza.bankfusion.core.EventsHelper;

public class UB_CMS_ReadPropertyFile {
	private transient final static Log logger = LogFactory.getLog(UB_CMS_ReadPropertyFile.class.getName());
	// public static final String absoluteFilePath =
	// "D:/BFUB164/Server/UBConf/conf/business/cms/cms.properties";
	public static final String CMS_DIRECTORY_NAME = "conf/business/cms/cms.properties";
	public static final String delim = "DELIM";
	public static final String sequence = "SEQUENCE";

	Properties cmsProperties = new Properties();

	public ArrayList createFieldsList() {
		loadPropertyFile();

		ArrayList fieldList = new ArrayList();

		String delimiter = cmsProperties.getProperty(delim);
		String sequenceOfData = cmsProperties.getProperty(sequence);
		AttributeMetaData fieldMetaData;
		StringTokenizer stk = new StringTokenizer(sequenceOfData, delimiter);

		while (stk.hasMoreTokens()) {
			fieldMetaData = new AttributeMetaData();
			String Name = stk.nextElement().toString();
			fieldMetaData.setNameOfTheField(Name);

			StringTokenizer fp = new StringTokenizer(cmsProperties
					.getProperty(Name), delimiter);

			fieldMetaData.setDataType(fp.nextToken());
			fieldMetaData.setLength(fp.nextToken());

			fieldList.add(fieldMetaData);
		}

		return fieldList;
	}

	public void loadPropertyFile() {
		StringBuilder path = new StringBuilder();
		try {
			// String configLocation = System.getProperty("BFconfigLocation",
			// CommonConstants.EMPTY_STRING);
			// String configLocation ="D:/BFUB164/Server/UBConf/";
			String configLocation = GetUBConfigLocation.getUBConfigLocation();
			path.append(configLocation);
			path.append(CMS_DIRECTORY_NAME);
			InputStream is = new FileInputStream(path.toString());
			cmsProperties.load(is);
		} catch (FileNotFoundException fnfe) {
			// TODO Auto-generated catch block
			Object stringArray[] = { path.toString() };
			EventsHelper.handleEvent(CommonsEventCodes.E_FILE_NOT_FOUND_UB14,
					"E", stringArray, new HashMap(), BankFusionThreadLocal
							.getBankFusionEnvironment());
          logger.error(ExceptionUtil.getExceptionAsString(fnfe));
		} catch (IOException ioe) {

			Object stringArray[] = { path.toString() };
			EventsHelper
					.handleEvent(
							CommonsEventCodes.E_THE_BATCH_XML_FILE_SPECIFIED_CANNOT_OR_CREATED,
							"E", stringArray, new HashMap(),
							BankFusionThreadLocal.getBankFusionEnvironment());
		logger.error(ExceptionUtil.getExceptionAsString(ioe));	
		
		}

	}

	/*
	 * Inner Class to define MetaData of the Fields.
	 */

}
