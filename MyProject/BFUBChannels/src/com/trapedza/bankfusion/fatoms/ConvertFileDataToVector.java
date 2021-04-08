package com.trapedza.bankfusion.fatoms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.runtime.toolkit.expression.function.Substring;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.ub.common.events.CommonsEventCodes;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;

public class ConvertFileDataToVector {
	private static final String MODULE_CONFIG_KEY = "CMS";
	// public static final String absoluteFilePath =
	// "D:/BFUB164/Server/UBConf/conf/business/cms/CMSFile.txt";
	private transient final static Log logger = LogFactory.getLog(ConvertFileDataToVector.class.getName());
	private static final String PARAM_CARD_UPDATE_BATCH_FILE = "PARAM_CARD_UPDATE_BATCH_FILE";

	FileReader input ;
	String tempString;
	private String fileName ;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

//	private static IBusinessInformationService ubInformationService = null;
/*	static {
		IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory.getInstance()
                .getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
	}*/

	public VectorTable readFile(ArrayList fieldList) {

		VectorTable vectorOfFields = new VectorTable();
		String absoluteFilePath1 = CommonConstants.EMPTY_STRING;
		String absoluteFilePath = CommonConstants.EMPTY_STRING;
		BufferedReader fileReader =null;
		// String fileName = CommonConstants.EMPTY_STRING;
		try {
			IBusinessInformationService ubInformationService = ((IBusinessInformationService) ServiceManagerFactory.getInstance().getServiceManager()
                    .getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE));
			absoluteFilePath1 = ubInformationService.getBizInfo().getModuleConfigurationValue(
					MODULE_CONFIG_KEY, PARAM_CARD_UPDATE_BATCH_FILE, null)
					.toString();

			absoluteFilePath = absoluteFilePath1 + getFileName();
			input = new FileReader(absoluteFilePath);
			fileReader = new BufferedReader(input);

			while ((tempString = fileReader.readLine()) != null
					&& !(tempString.trim().equals(CommonConstants.EMPTY_STRING))) {

				vectorOfFields.addAll(new VectorTable(createVector(fieldList,
						tempString)));

			}
			File file = new File(absoluteFilePath);

			file.renameTo(new File(absoluteFilePath1
					+ SystemInformationManager.getInstance()
							.getBFBusinessDateTimeAsString() + "_"
					+ getFileName()));
			/*
			 * Need to write the code for re-name the file path
			 */
		} catch (FileNotFoundException fnfe) {

			Object stringArray[] = { getFileName() };
			EventsHelper.handleEvent(CommonsEventCodes.E_FILE_NOT_FOUND_UB14,
					"E", stringArray, new HashMap(), BankFusionThreadLocal
							.getBankFusionEnvironment());
			throw new RuntimeException (fnfe);

		} catch (IOException ioe) {

			Object stringArray[] = { absoluteFilePath };
			EventsHelper
					.handleEvent(
							CommonsEventCodes.E_THE_BATCH_XML_FILE_SPECIFIED_CANNOT_OR_CREATED,
							"E", stringArray, new HashMap(),
							BankFusionThreadLocal.getBankFusionEnvironment());
			throw new RuntimeException (ioe);
		}finally{			
			if(null!=fileReader){
				try {
					fileReader.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
		return vectorOfFields;
	}

	public HashMap createVector(ArrayList arrlist, String line) {
		String nameOfField;
		String dataType;
		String value;

		int fieldIndexFrom = 0;
		int fieldIndexTo = 0;

		AttributeMetaData metaDataOfField;
		HashMap mapOfFields = new HashMap();

		Iterator<AttributeMetaData> metaDataIterator = arrlist.iterator();
		while (metaDataIterator.hasNext()) {

			metaDataOfField = metaDataIterator.next();
			nameOfField = metaDataOfField.getNameOfTheField();
			dataType = metaDataOfField.getDataType();

			if (fieldIndexFrom == 0) {
				fieldIndexTo = (Integer.parseInt(metaDataOfField.getLength()) - 1)
						+ fieldIndexTo;
			} else {
				fieldIndexTo = (Integer.parseInt(metaDataOfField.getLength()))
						+ fieldIndexTo;
			}
			value = Substring.run(fieldIndexFrom, fieldIndexTo, line);

			if (dataType.equals("L")) {
				Long nameField = 0L;
				nameField = Long.parseLong(value);
				mapOfFields.put(nameOfField, nameField);
			}

			if (dataType.equals("N")) {
				int nameField = 0;
				nameField = Integer.parseInt(value);
				mapOfFields.put(nameOfField, nameField);
			}
			if (dataType.equals("D")) {

				DateFormat formatter;
				Date date;
				formatter = new SimpleDateFormat("ddMMyyyy");
				try {
					date = (Date) formatter.parse(value);
					mapOfFields.put(nameOfField, date);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					logger.error(e);
				}
			}
			if (dataType.equals("A")) {
				mapOfFields.put(nameOfField, value);
			}
			fieldIndexFrom = fieldIndexTo + 1;
		}
		return mapOfFields;

	}

}
