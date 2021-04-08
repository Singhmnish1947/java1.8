/*******************************************************************************
 * (c) 2020. Finastra Software Solutions. All Rights Reserved.
 ******************************************************************************/
package com.finastra.iso8583.atm.processes;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.cbs.common.GetConfigLocation;

/**
 * @author kumarbin
 * @version $Revision: 1.0 $
 */
public class ATMSettlementPseudonymResolver {
	/**
	 * Fatom to resolve the settlement account based on configuration for atm/pos
	 * and identify SMS/DMS
	 */
	private static Properties fileProp;
	private static Log logger = LogFactory.getLog(ATMSettlementPseudonymResolver.class.getName());
	private static String channeId = "";
	private static String smsDms = "";
	private static String incorrectKeyValue = "==================Incorrect Key/Value in IMDCodeMapping.properties ==================";
	static final String line = "==================";
	static final String logger_ERROR_DESCRIPTION = "  ErrorDescription : ";
	static final String logger_ERROR_CODE = "   ErrorCode : ";
	static final String TransactionType_properties = "TransactionType.properties";
	static final String path_TransactionType = "conf/business/atm/";

	public static String getSmsDms() {
		return smsDms;
	}

	public static void setSmsDms(String smsDms) {
		ATMSettlementPseudonymResolver.smsDms = smsDms;
	}

	public static String getChanneId() {
		return channeId;
	}

	public static void setChanneId(String channeId) {
		ATMSettlementPseudonymResolver.channeId = channeId;
	}

	public String getNarrative(String transactionTypeCode, String transactionAmount, String tranactionCurrency,
			String merchantId, String postingDate) {

		String transactionTypeDesc = ATMSettlementPseudonymResolver.getProperty(path_TransactionType,
				TransactionType_properties, transactionTypeCode);
		String narrative = merchantId + "_" + transactionTypeDesc + "_" + postingDate + "_" + tranactionCurrency + "_"
				+ transactionAmount + "_" + merchantId + "_" + merchantId;
		return narrative;
	}

	public String getImdCode(String channel, String transactionType) {
		String key;
		key = channel + "_" + transactionType;

		String returnArr = "";

		String value = getProperty("conf/business/atm/", "IMDCodeMapping.properties", key);
		if ((null != value)) {
			String[] valueArr = value.split("_");

			if (valueArr.length < 3) {

				ATMTransactionUtil.handleEvent(40000123, new String[] { key, " ' ' in IMDCodeMapping.properties" });
			} else {
				setChanneId(valueArr[0]);
				setSmsDms(valueArr[1]);
				returnArr = valueArr[2];
			}
			return returnArr;

		} else {

			if (logger.isErrorEnabled()) {
				logger.error(incorrectKeyValue);
			}
			return returnArr;
		}
	}

	public static String getProperty(String filePath, String fileName, String key) {

		String value = "";
		StringBuilder path = new StringBuilder();
		path.append(GetConfigLocation.getUBConfigLocation());
		path.append(filePath);
		path.append(fileName);
		InputStream input = null;
		try {
			input = new FileInputStream(path.toString());
			fileProp = new Properties();
			fileProp.load(input);
			input.close();
			if (null != fileProp.get(key))
				value = fileProp.get(key).toString();
		} catch (Exception exception) {
			logger.error("The input tag: " + key + " has an invalid value:  in " + fileName);
			logger.error(exception.getCause() != null ? ExceptionUtils.getStackTrace(exception.getCause())
					: ExceptionUtils.getStackTrace(exception));
		} finally {
			try {
				if (null != input) {
					input.close();
				}
			} catch (Exception exception) {
				logger.error(exception.getCause() != null ? ExceptionUtils.getStackTrace(exception.getCause())
						: ExceptionUtils.getStackTrace(exception));
			}
		}
		return value;
	}

}
