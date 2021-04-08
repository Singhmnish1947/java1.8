package com.misys.ub.swift;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Global functions
 */
public class SWT_Outgoing_Globals {
	private static final transient Log LOGGER = LogFactory.getLog(SWT_Outgoing_Globals.class.getName());
	
	public static String convertBIC(String inputBIC) {
		if (inputBIC != null && inputBIC.length() > 11) {
			String firstNineCharacterOfBIC = inputBIC.substring(0, 8);
			String lastThreeCharacterOfBIC = inputBIC.substring(9,
					inputBIC.length());
			String finalBIC = firstNineCharacterOfBIC
					.concat(lastThreeCharacterOfBIC);
			LOGGER.info("*** Destination Bic after removal of 9th Char::" + finalBIC);
			return finalBIC;
		} else {
			return inputBIC;
		}
	}

	public static String formatDateForUB(String inputDate) {
		String returnUBDate = null;
		String year = inputDate.substring(0, 2);
		String month = inputDate.substring(2, 4);
		String day = inputDate.substring(4, 6);
		DateFormat df = new SimpleDateFormat("yy-MM-dd");
		DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
		Date valDate = null;
		try {
			valDate = df.parse(year + "-" + month + "-" + day);
			returnUBDate = df1.format(valDate);
		} catch (Exception e) {
		}
		return returnUBDate;
	}

	public static String AddUBDelimiter(String inputString) {
		return inputString.replaceAll("\\\\n", "\\$");
	}

	public static String formatDateForStatementLine(String inputString) {
		String resultString = "";
		String semiFinalString = "";
		String finalString = "";
		if (inputString != null && inputString.trim().length() > 0) {
			resultString = inputString.substring(2);
			semiFinalString = new String(resultString.replaceAll("-", ""));
			finalString = semiFinalString.substring(0, 6)
					+ semiFinalString.substring(8);
			if (finalString.indexOf(".") != -1) {
				finalString = finalString.replace('.', ',');
			} else
				finalString = finalString.concat(",");
			return finalString;
		} else
			return "";
	}

	public static String formatDateForAmount(String inputString) {
		String resultString = "";
		String semiFinalString = "";
		String finalString = "";
		if (inputString != null && inputString.trim().length() > 0) {
			resultString = new String(inputString.replaceAll("-", ""));
			semiFinalString = resultString.substring(0, 1)
					+ resultString.substring(3);
			if (semiFinalString.indexOf(".") != -1) {
				finalString = semiFinalString.replace('.', ',');
			} else
				finalString = semiFinalString.concat(",");
			return finalString;
		} else
			return "";
	}

	public static String RemoveUBDelimiter(String inputString) {
		String resultString = "";
		boolean returnDelims = false;
		StringTokenizer tempStringTokenizer = new StringTokenizer(inputString,
				"$", returnDelims);
		if (tempStringTokenizer.hasMoreTokens()) {
			resultString = tempStringTokenizer.nextToken();
			while (tempStringTokenizer.hasMoreTokens()) {
				resultString += "\\n" + tempStringTokenizer.nextToken();
			}
		}
		return resultString;
	}

	public static String formatDateTo6Digits(String inputString) {
		String resultString = "";
		String finalString = "";
		if (inputString != null && inputString.trim().length() > 0)
			resultString = new String(inputString.replaceAll("-", ""));
		finalString = resultString.substring(2);
		return finalString;
	}

	public static String formatDate(String inputString) {
		String resultString = "";
		if (inputString != null && inputString.trim().length() > 0)
			resultString = new String(inputString.replaceAll("-", ""));
		return resultString;
	}

	public static String AddReceivingLT(String address) {
		return address.substring(0, 8) + "X" + address.substring(8, 11);
	}

	public static String AddSendingLT(String address) {
		return address.substring(0, 8) + "A" + address.substring(8, 11);
	}

	public static String RemoveUBDelimiterWithBlank(String inputString) {
		String resultString = "";
		if (inputString != null && inputString.trim().length() > 0)
			resultString = new String(inputString.replace('$', ' '));
		if (resultString != null) {
			if (resultString.indexOf(".") != -1)
				return resultString.replace('.', ',');
			else
				resultString.concat(",");
		}
		return resultString;
	}

	public static String ReplacePeriodWithComma(String inputString) {
		String resultString = null;
		if (inputString != null && inputString.trim().length() > 0)
			resultString = new String(inputString.replace('$', ' '));
		if (resultString != null) {
			if (resultString.indexOf(".") != -1) {
				return resultString.replace('.', ',');
			} else {
				return resultString.concat(",");
			}
		}
		return resultString;
	}
}