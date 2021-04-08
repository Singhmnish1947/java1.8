/* ***********************************************************************************
 * Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 * $Id: SWT_Util.java,v 1.3 2008/08/12 20:13:08 vivekr Exp $
 *
 */
package com.misys.ub.swift;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.security.runtime.impl.BranchUtil;
import com.misys.bankfusion.subsystem.security.runtime.impl.CurrencyUtil;
import com.misys.cbs.config.ModuleConfiguration;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOCB_CMN_ModuleConfiguration;
import com.trapedza.bankfusion.bo.refimpl.IBOCurrency;
import com.trapedza.bankfusion.bo.refimpl.IBOPseudonymAccountMap;
import com.trapedza.bankfusion.bo.refimpl.IBOSWTDisposal;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.IsWorkingDay;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.PreviousWorkingDateForDate;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;

/**
 * @author Girish
 * 
 */
public class SWT_Util {
	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}
	private transient final static Log logger = LogFactory.getLog(SWT_Util.class.getName());
	private static final String ModuleName = "FEX";
	private static final String VOSTROPARAMMANE = "DEFAULT_VOSTRO";
	private static final String whereClause2 = " WHERE " + IBOCB_CMN_ModuleConfiguration.MODULENAME + "='" + ModuleName + "' AND " + IBOCB_CMN_ModuleConfiguration.PARAMNAME + " = '" + VOSTROPARAMMANE + "'";
	private static final String whereClause1 = " WHERE " + IBOPseudonymAccountMap.PSEUDONAME + " like ? AND " + IBOPseudonymAccountMap.ACCOUNTID + " = ?";

	/**
	 * Method to check whether a given value is null or not
	 * 
	 * @param inputString
	 * @return String if not null return inputValue otherwise blank
	 */
	public String verifyForNull(String inputString) {
		if (inputString != null && inputString.trim().length() > 0) {
			return inputString.trim();
		}
		return CommonConstants.EMPTY_STRING;
	}

	public HashMap populateBranch_BICCodeMap(BankFusionEnvironment env) {
		List branchList = new ArrayList();
		HashMap Branch_BICCodeMap = new HashMap();
		try {
            branchList = BranchUtil.getListOfBranchDetailsInCurrentZone();
		}
		catch (BankFusionException ex) {
			new BankFusionException(40507007, new Object[] { "populate Branch & BIC Codes." }, logger, env);
		}
		
		
		if (branchList == null) {
			new BankFusionException(40507007, new Object[] { "populate Branch & BIC Codes." }, logger, env);
		}
		else {
			for (int i = 0; i < branchList.size(); i++) {
				IBOBranch branchObj = (IBOBranch) branchList.get(i);
				if (branchObj.getF_BICCODE() != null) {
					Branch_BICCodeMap.put(branchObj.getBoID(), branchObj.getF_BICCODE().toString());
				}
				else {
					Branch_BICCodeMap.put(branchObj.getBoID(), CommonConstants.EMPTY_STRING);
				}
			}
		}
		return Branch_BICCodeMap;
	}

	/**
	 * This method returns date&time String where date as SWIFT format(YYDDMM) and time as 'HHMMSS'
	 * with " "(space) as delimiter.
	 * 
	 * @param timeStamp
	 * @return
	 */
	public String getSwiftDateTimeString(Timestamp timeStamp) {
		StringBuffer dateTimeBuffer = new StringBuffer();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timeStamp.getTime());
		int date = cal.get(Calendar.DATE);
		int month = cal.get(Calendar.MONTH);
		int year = cal.get(Calendar.YEAR);
		StringBuffer dateBuffer = new StringBuffer();
		dateBuffer.append(Integer.toString(year).substring(2));
		month++;
		if (month < 10)
			dateBuffer.append("0" + Integer.toString(month));
		else
			dateBuffer.append(Integer.toString(month));
		if (date < 10)
			dateBuffer.append("0" + Integer.toString(date));
		else
			dateBuffer.append(Integer.toString(date));
		dateTimeBuffer.append(dateBuffer.toString() + " ");
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);
		StringBuffer timeBuffer = new StringBuffer();
		if (hour < 10)
			timeBuffer.append("0" + Integer.toString(hour));
		else
			timeBuffer.append(Integer.toString(hour));
		if (minute < 10)
			timeBuffer.append("0" + Integer.toString(minute));
		else
			timeBuffer.append(Integer.toString(minute));
		if (second < 10)
			timeBuffer.append("0" + Integer.toString(second));
		else
			timeBuffer.append(Integer.toString(second));
		dateTimeBuffer.append(timeBuffer.toString());
		return dateTimeBuffer.toString();
	}

	/**
	 * This method returns date&time String where date as SWIFT format(YYDDMM) and time as 'HHMMSS'
	 * with " "(space) as delimiter.
	 * 
	 * @param timeStamp
	 * @return
	 */
	public String createSwiftTagString(String bicCode, String accInfo, String nationalCLRCode, String text1, String text2, String text3) {
		String BIC_code = verifyForNull(bicCode);
		String AccountInformation = verifyForNull(accInfo);
		String Narrative1 = verifyForNull(text1);
		String Narrative2 = verifyForNull(text2);
		String Narrative3 = verifyForNull(text3);
		String NationClearingCode = verifyForNull(nationalCLRCode);
		return getTagString(BIC_code, AccountInformation, NationClearingCode, Narrative1, Narrative2, Narrative3);
	}

	public String IsPublish(String messageType, int confirmflag, int cancelflag) {
		if ((messageType.charAt(0) == '3') && confirmflag == 2 && cancelflag == 0) {
			return "N";
		}
		return "Y";
	}

	/**
	 * Method to Create a tag based on input tags
	 * 
	 * @param bic_code
	 *            BIC Code
	 * @param accountInformation
	 *            Account Information
	 * @param nationClearingCode
	 *            National Claring Code
	 * @param narrative1
	 *            Narrative line 1
	 * @param narrative2
	 *            Narrative Line 2
	 * @param narrative3
	 *            Narrative Line 3
	 * @return String 52Tag value
	 */
	private String getTagString(String bic_code, String accountInformation, String nationClearingCode, String narrative1, String narrative2, String narrative3) {
		StringBuffer tmp_tag52 = new StringBuffer();
		if (bic_code != null && !bic_code.equals(CommonConstants.EMPTY_STRING)) {
			// it is going to be 52A
			if (!nationClearingCode.equals(CommonConstants.EMPTY_STRING)) {
				tmp_tag52.append(nationClearingCode);
				tmp_tag52.append(SWT_Constants.delimiter);
			}
			if (!accountInformation.equals(CommonConstants.EMPTY_STRING)) {
				tmp_tag52.append(accountInformation);
				tmp_tag52.append(SWT_Constants.delimiter);
			}
			tmp_tag52.append(bic_code);
			if (!tmp_tag52.toString().equals(CommonConstants.EMPTY_STRING))
				tmp_tag52.append("A");
			else {
				tmp_tag52.append(" ");
			}
		}
		else {
			tmp_tag52.append(nationClearingCode);
			if (tmp_tag52.length() > 0)
				tmp_tag52.append(SWT_Constants.delimiter);
			tmp_tag52.append(accountInformation);
			if (tmp_tag52.length() > 0)
				tmp_tag52.append(SWT_Constants.delimiter);
			tmp_tag52.append(narrative1);
			if (tmp_tag52.length() > 0)
				tmp_tag52.append(SWT_Constants.delimiter);
			tmp_tag52.append(narrative2);
			if (tmp_tag52.length() > 0)
				tmp_tag52.append(SWT_Constants.delimiter);
			tmp_tag52.append(narrative3);
			if (tmp_tag52.length() > 0)
				tmp_tag52.append(SWT_Constants.delimiter);
			if (!tmp_tag52.toString().equals(CommonConstants.EMPTY_STRING)) {
				tmp_tag52.append("D");
			}
			else {
				tmp_tag52.append(" ");
			}
		}
		return tmp_tag52.toString().trim();
	}

	/**
	 * @param disposalObject
	 * @return
	 */
	public boolean intermedaitoryDetailsExists(SWT_DisposalObject disposalObject) {
		boolean detailsExists = true;
		String interBICCode = verifyForNull(disposalObject.getSI_IntermediatoryCode());
		String interAccInfo = verifyForNull(disposalObject.getSI_IntermediatoryAccInfo());
		String interText1 = verifyForNull(disposalObject.getSI_IntermediatoryText1());
		String interText2 = verifyForNull(disposalObject.getSI_IntermediatoryText2());
		String interText3 = verifyForNull(disposalObject.getSI_IntermediatoryText3());
		if (interBICCode.equals(CommonConstants.EMPTY_STRING) && interAccInfo.equals(CommonConstants.EMPTY_STRING) && interText1.equals(CommonConstants.EMPTY_STRING) && interText2.equals(CommonConstants.EMPTY_STRING) && interText3.equals(CommonConstants.EMPTY_STRING)) {
			detailsExists = false;
		}
		return detailsExists;
	}

	/**
	 * @param disposalObject
	 * @return
	 */
	public boolean accountWithDetailsExists(SWT_DisposalObject disposalObject) {
		boolean detailsExists = true;
		String accWithBICCode = verifyForNull(disposalObject.getSI_AccWithCode());
		String accWithText1 = verifyForNull(disposalObject.getSI_AccWithText1());
		String accWithText2 = verifyForNull(disposalObject.getSI_AccWithText2());
		String accWithText3 = verifyForNull(disposalObject.getSI_AccWithText3());
		String accWithAccInfo = verifyForNull(disposalObject.getSI_AccWithAccInfo());
		if (accWithBICCode.equals(CommonConstants.EMPTY_STRING) && accWithAccInfo.equals(CommonConstants.EMPTY_STRING) && accWithText1.equals(CommonConstants.EMPTY_STRING) && accWithText2.equals(CommonConstants.EMPTY_STRING) && accWithText3.equals(CommonConstants.EMPTY_STRING)) {
			detailsExists = false;
		}
		return detailsExists;
	}

	/**
	 * @param disposalObject
	 * @return
	 */
	public boolean orderingInstituteDetailsExists(SWT_DisposalObject disposalObject) {
		boolean detailsExists = true;
		String accWithBICCode = verifyForNull(disposalObject.getSI_OrdInstBICCode());
		String accWithText1 = verifyForNull(disposalObject.getSI_OrdInstText1());
		String accWithText2 = verifyForNull(disposalObject.getSI_OrdInstText2());
		String accWithText3 = verifyForNull(disposalObject.getSI_OrdInstText3());
		String accWithAccInfo = verifyForNull(disposalObject.getSI_OrdInstAccInfo());
		if (accWithBICCode.equals(CommonConstants.EMPTY_STRING) && accWithAccInfo.equals(CommonConstants.EMPTY_STRING) && accWithText1.equals(CommonConstants.EMPTY_STRING) && accWithText2.equals(CommonConstants.EMPTY_STRING) && accWithText3.equals(CommonConstants.EMPTY_STRING)) {
			detailsExists = false;
		}
		return detailsExists;
	}

	/**
	 * @param disposalObject
	 * @return
	 */
	public boolean forAccountDetailsExist(SWT_DisposalObject disposalObject) {
		boolean detailsExists = true;
		String forAccInfo = verifyForNull(disposalObject.getSI_ForAccountInfo());
		String forAccText1 = verifyForNull(disposalObject.getSI_ForAccountText1());
		String forAccText2 = verifyForNull(disposalObject.getSI_ForAccountText2());
		String forAccText3 = verifyForNull(disposalObject.getSI_ForAccountText3());
		if (forAccInfo.equals(CommonConstants.EMPTY_STRING) && forAccText1.equals(CommonConstants.EMPTY_STRING) && forAccText2.equals(CommonConstants.EMPTY_STRING) && forAccText3.equals(CommonConstants.EMPTY_STRING)) {
			detailsExists = false;
		}
		return detailsExists;
	}

	/**
	 * @param disposalObject
	 * @return
	 */
	public boolean payToDetailsExists(SWT_DisposalObject disposalObject) {
		boolean detailsExists = true;
		String accWithBICCode = verifyForNull(disposalObject.getSI_PayToBICCode());
		String accWithText1 = verifyForNull(disposalObject.getSI_PayToText1());
		String accWithText2 = verifyForNull(disposalObject.getSI_PayToText2());
		String accWithText3 = verifyForNull(disposalObject.getSI_PayToText3());
		String accWithAccInfo = verifyForNull(disposalObject.getSI_PayToAccInfo());
		if (accWithBICCode.equals(CommonConstants.EMPTY_STRING) && accWithAccInfo.equals(CommonConstants.EMPTY_STRING) && accWithText1.equals(CommonConstants.EMPTY_STRING) && accWithText2.equals(CommonConstants.EMPTY_STRING) && accWithText3.equals(CommonConstants.EMPTY_STRING)) {
			detailsExists = false;
		}
		return detailsExists;
	}

	/**
	 * @param settlementDetail
	 * @return
	 */
	public String getBankToBankInfo(SWT_DisposalObject disposalObject) {
		StringBuffer temp57 = new StringBuffer();
		if (!verifyForNull(disposalObject.getSI_BankToBankInfo1()).equals(CommonConstants.EMPTY_STRING)) {
			temp57.append(disposalObject.getSI_BankToBankInfo1());
			temp57.append(SWT_Constants.delimiter);
		}
		if (!verifyForNull(disposalObject.getSI_BankToBankInfo2()).equals(CommonConstants.EMPTY_STRING)) {
			temp57.append(disposalObject.getSI_BankToBankInfo2());
			temp57.append(SWT_Constants.delimiter);
		}
		if (!verifyForNull(disposalObject.getSI_BankToBankInfo3()).equals(CommonConstants.EMPTY_STRING)) {
			temp57.append(disposalObject.getSI_BankToBankInfo3());
			temp57.append(SWT_Constants.delimiter);
		}
		if (!verifyForNull(disposalObject.getSI_BankToBankInfo4()).equals(CommonConstants.EMPTY_STRING)) {
			temp57.append(disposalObject.getSI_BankToBankInfo4());
			temp57.append(SWT_Constants.delimiter);
		}
		if (!verifyForNull(disposalObject.getSI_BankToBankInfo5()).equals(CommonConstants.EMPTY_STRING)) {
			temp57.append(disposalObject.getSI_BankToBankInfo5());
			temp57.append(SWT_Constants.delimiter);
		}
		if (!verifyForNull(disposalObject.getSI_BankToBankInfo6()).equals(CommonConstants.EMPTY_STRING)) {
			temp57.append(disposalObject.getSI_BankToBankInfo6());
		}
		return temp57.toString();
	}

	/**
	 * @param settlementDetail
	 * @return
	 */
	public String getTag70String(SWT_DisposalObject disposalObject) {
		StringBuffer temp57 = new StringBuffer();
		if (!verifyForNull(disposalObject.getSI_PayDetails1()).equals(CommonConstants.EMPTY_STRING)) {
			temp57.append(disposalObject.getSI_PayDetails1());
			temp57.append(SWT_Constants.delimiter);
		}
		if (!verifyForNull(disposalObject.getSI_PayDetails2()).equals(CommonConstants.EMPTY_STRING)) {
			temp57.append(disposalObject.getSI_PayDetails2());
			temp57.append(SWT_Constants.delimiter);
		}
		if (!verifyForNull(disposalObject.getSI_PayDetails3()).equals(CommonConstants.EMPTY_STRING)) {
			temp57.append(disposalObject.getSI_PayDetails3());
			temp57.append(SWT_Constants.delimiter);
		}
		if (!verifyForNull(disposalObject.getSI_PayDetails4()).equals(CommonConstants.EMPTY_STRING)) {
			temp57.append(disposalObject.getSI_PayDetails4());
		}
		return temp57.toString();
	}

	/**
	 * @param settlementDetail
	 * @param isCHQB
	 * @return
	 */
	public String getForAccountInfoString(SWT_DisposalObject disposalObject, boolean isCHQB) {
		StringBuffer temp57 = new StringBuffer();
		if (isCHQB) {
			if (!verifyForNull(disposalObject.getSI_ForAccountText1()).equals(CommonConstants.EMPTY_STRING)) {
				temp57.append(disposalObject.getSI_ForAccountText1());
				temp57.append(SWT_Constants.delimiter);
			}
			if (!verifyForNull(disposalObject.getSI_ForAccountText2()).equals(CommonConstants.EMPTY_STRING)) {
				temp57.append(disposalObject.getSI_ForAccountText2());
				temp57.append(SWT_Constants.delimiter);
			}
			if (!verifyForNull(disposalObject.getSI_ForAccountText3()).equals(CommonConstants.EMPTY_STRING)) {
				temp57.append(disposalObject.getSI_ForAccountText3());
			}
		}
		else {
			if (!verifyForNull(disposalObject.getSI_ForAccountInfo()).equals(CommonConstants.EMPTY_STRING)) {
				temp57.append(disposalObject.getSI_ForAccountInfo());
				temp57.append(SWT_Constants.delimiter);
			}
			if (!verifyForNull(disposalObject.getSI_ForAccountText1()).equals(CommonConstants.EMPTY_STRING)) {
				temp57.append(disposalObject.getSI_ForAccountText1());
				temp57.append(SWT_Constants.delimiter);
			}
			if (!verifyForNull(disposalObject.getSI_ForAccountText2()).equals(CommonConstants.EMPTY_STRING)) {
				temp57.append(disposalObject.getSI_ForAccountText2());
				temp57.append(SWT_Constants.delimiter);
			}
			if (!verifyForNull(disposalObject.getSI_ForAccountText3()).equals(CommonConstants.EMPTY_STRING)) {
				temp57.append(disposalObject.getSI_ForAccountText3());
			}
		}
		return temp57.toString();
	}

	public boolean generateCategory2Message(Date ValueDate, BankFusionEnvironment env, String currency, Date bankfusionSystemDate, String messageType) {
		Date newDate = null;
		Date maturityDate = null;
		try {
			maturityDate = calDate(ValueDate.toString(), currency, env, messageType);
			newDate = maturityDate;
			Boolean isWorking = IsWorkingDay.run("CURRENCY", currency, new Integer(0), maturityDate, env);
			if (!isWorking.booleanValue()) {
				newDate = PreviousWorkingDateForDate.run("CURRENCY", currency, new Integer(0), maturityDate, env);
			}
			else
				newDate = maturityDate;
		}
		catch (BankFusionException e) {
			logger.error("Exception in checkWorkingDay method: ", e);
		}
		if (newDate == bankfusionSystemDate || (null != newDate && newDate.before(bankfusionSystemDate)))
			return true;
		else
			return false;
	}

	private Date calDate(String aDate, String Currency, BankFusionEnvironment env, String messageType) {
        IBOCurrency contraCurrency = CurrencyUtil.getCurrencyDetailsOfCurrentZone(Currency);
		int aDays = 0;
		Calendar cal = null;
		String[] date;
		if(null != contraCurrency) {
			aDays = contraCurrency.getF_SWTADVICEDAYS();
			if (messageType != null && messageType.equals("103")) {
				aDays += 1;
			}
		}
        else {
            logger.error("Currency not available on this " + Currency);
			// throw new BankFusionException(9455, new Object[] { params.get(0) }, logger, env);
            EventsHelper.handleEvent(ChannelsEventCodes.E_ADDRESS_LINE_IS_MANDATORY_WITH_PARTY_IDENTIFIER,
                    new Object[] { Currency }, new HashMap(), env);
		}
		cal = Calendar.getInstance();
		date = aDate.split("-");
		cal.clear();
		cal.set(Integer.parseInt(date[0]), Integer.parseInt(date[1]) - 1, Integer.parseInt(date[2]));
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 1);
		aDays *= -1;
		cal.add(cal.DATE, aDays);
		return new Date(cal.getTimeInMillis());
	}

	public String DecimalRounding(String Amount, int noDecimalPoints) {
		if (Amount.indexOf(".") == -1)
			return Amount;
		if (noDecimalPoints > 1)
			return Amount.substring(0, Amount.indexOf(".") + 1 + noDecimalPoints);
		else
			return Amount.substring(0, Amount.indexOf("."));
	}

	public int noDecimalPlaces(String currency, BankFusionEnvironment env) {
        IBOCurrency currencyDetails = CurrencyUtil.getCurrencyDetailsOfCurrentZone(currency);
		int decimalPlace = 0;
        if (null != currencyDetails) {
			decimalPlace = currencyDetails.getF_CURRENCYSCALE();
		}
        else {
            logger.error("Currency not available on this " + currency);
			// throw new BankFusionException(9455, new Object[] { params.get(0) }, logger, env);
            EventsHelper.handleEvent(ChannelsEventCodes.E_ADDRESS_LINE_IS_MANDATORY_WITH_PARTY_IDENTIFIER,
                    new Object[] { currency }, new HashMap(), env);
		}
		return decimalPlace;
	}

	public String convertBicCheck(String bicCheck) {
		String updateBicCheck = bicCheck;
		updateBicCheck = updateBicCheck.replace('0', 'a');
		updateBicCheck = updateBicCheck.replace('1', 'b');
		updateBicCheck = updateBicCheck.replace('2', 'c');
		updateBicCheck = updateBicCheck.replace('3', 'd');
		updateBicCheck = updateBicCheck.replace('4', 'e');
		updateBicCheck = updateBicCheck.replace('5', 'f');
		updateBicCheck = updateBicCheck.replace('6', 'g');
		updateBicCheck = updateBicCheck.replace('7', 'h');
		updateBicCheck = updateBicCheck.replace('8', 'i');
		updateBicCheck = updateBicCheck.replace('9', 'j');
		return updateBicCheck;
	}

	public String nonZeroValues(String value) {
		String nonZerovalue = " ";
		int j = value.length();
		int firstValuefetched = 0;
		for (int i = value.length() - 1; i >= 0 && nonZerovalue.trim().length() < 4; i--) {
			if ((value.substring(i, j).compareTo("0") != 0 || firstValuefetched == 1) && (value.substring(i, j).compareTo(".") != 0)) {
				nonZerovalue = value.substring(i, j).concat(nonZerovalue);
				firstValuefetched = 1;
			}
			j--;
		}
		return concateZeroBefore(nonZerovalue.trim());
	}

	private String concateZeroBefore(String nonZeroValue) {
		if (nonZeroValue.trim().length() == 4)
			return nonZeroValue;
		else if (nonZeroValue.trim().length() == 3)
			return "0" + nonZeroValue;
		else if (nonZeroValue.trim().length() == 2)
			return "00" + nonZeroValue;
		else if (nonZeroValue.trim().length() == 1)
			return "000" + nonZeroValue;
		else
			return "0000";
	}

	public BigDecimal BigDecimalSubtract(BigDecimal Amount1, BigDecimal Amount2) {
		// return new BigDecimal(Amount1.doubleValue() - Amount2.doubleValue());
		return Amount1.subtract(Amount2);
	}

	public int updateFlagValues(BankFusionEnvironment env, int msgType, String disposalId) {
		int msgStatus = 0;
		IBOSWTDisposal swtDisposal = (IBOSWTDisposal) env.getFactory().findByPrimaryKey(IBOSWTDisposal.BONAME, disposalId);
		if (msgType == 202) {
			msgStatus = updateMsgStatusFlag(swtDisposal.getF_CONFIRMATIONFLAG(), swtDisposal.getF_RECEIPTFLAG(), swtDisposal.getF_CRDRCONFIRMATIONFLAG());
		}
		else if (msgType == 210) {
			msgStatus = updateMsgStatusFlag(swtDisposal.getF_CONFIRMATIONFLAG(), swtDisposal.getF_PAYMENTFLAG(), swtDisposal.getF_CRDRCONFIRMATIONFLAG());
		}
		else if (msgType == 900910) {
			msgStatus = updateMsgStatusFlagfor900(swtDisposal.getF_CONFIRMATIONFLAG(), swtDisposal.getF_PAYMENTFLAG(), swtDisposal.getF_RECEIPTFLAG());
		}
		else {
			msgStatus = updateMsgStatusFlag(swtDisposal.getF_RECEIPTFLAG(), swtDisposal.getF_PAYMENTFLAG(), swtDisposal.getF_CRDRCONFIRMATIONFLAG());
		}
		return msgStatus;
	}

	private int updateMsgStatusFlag(int flag1, int flag2, int flag3) {
		int msgStatus = 0;
		if (flag1 == 0 || flag2 == 0 || flag3 == 0 || flag1 == 2 || flag2 == 2 || flag3 == 1 || flag3 == 4 || flag3 == 5)
			msgStatus = 1;
		else
			msgStatus = 2;
		return msgStatus;
	}

	private int updateMsgStatusFlagfor900(int flag1, int flag2, int flag3) {
		int msgStatus = 0;
		if (flag1 == 0 || flag2 == 0 || flag3 == 0 || flag1 == 2 || flag2 == 2 || flag3 == 2)
			msgStatus = 1;
		else
			msgStatus = 2;
		return msgStatus;
	}

	private int CancelFlag(int flag1, int flag2, int flag3, int flag4) {
		if ((flag1 == 3 || flag1 == 9) && (flag2 == 3 || flag2 == 9) && (flag3 == 9 || flag3 == 6 || flag3 == 7))
			flag4 = 1;
		else
			flag4 = 0;
		return flag4;
	}

	private int CancelFlag900910(int flag1, int flag2, int flag3, int flag4) {
		if ((flag1 == 3 || flag1 == 9) && (flag2 == 3 || flag2 == 9) && (flag3 == 3 || flag3 == 9))
			flag4 = 1;
		else
			flag4 = 0;
		return flag4;
	}

	public boolean isSwiftNostro(String accountno, BankFusionEnvironment env) {
		List list = null;
		String paramsValue = "";
		String moduleName = ModuleName;
		String paramsName = VOSTROPARAMMANE;
		IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory.getInstance().getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
		paramsValue = (String) ((IBusinessInformation) ubInformationService.getBizInfo()).getModuleConfigurationValue(moduleName, paramsName, env);
		if (paramsValue != null) {
			// SimplePersistentObject spo= (SimplePersistentObject) list.get(0);
			String psedonyme = paramsValue;
			ArrayList params = new ArrayList();
			params.add("%" + psedonyme + "%");
			params.add(accountno);
			list = env.getFactory().findByQuery(IBOPseudonymAccountMap.BONAME, whereClause1, params, null);
			if (list.size() > 0) {
				return true;
			}
			return false;
		}
		return false;
	}

	public boolean isSwiftVostro(String accountno, BankFusionEnvironment env) {
		List list = null;
        String psedonyme = (String) ModuleConfiguration.getInstance().getModuleConfigurationValue(ModuleName, VOSTROPARAMMANE);
        ArrayList params = new ArrayList();
        params.add("%" + psedonyme + "%");
        params.add(accountno);
        list = env.getFactory().findByQuery(IBOPseudonymAccountMap.BONAME, whereClause1, params, null);
		if (list.size() > 0) {
            return true;
		}
		return false;
	}

	public int updateCancelFlag(BankFusionEnvironment env, int msgType, String disposalId) {
		SWT_DisposalObject disposalObject = new SWT_DisposalObject();
		int cancelStatus = disposalObject.getCancelFlag();
		IBOSWTDisposal swtDisposal = (IBOSWTDisposal) env.getFactory().findByPrimaryKey(IBOSWTDisposal.BONAME, disposalId);
		// int cancelStatus = swtDisposal.getF_CANCELFLAG();
		if (msgType == 202292) {
			cancelStatus = CancelFlag(swtDisposal.getF_CONFIRMATIONFLAG(), swtDisposal.getF_RECEIPTFLAG(), swtDisposal.getF_CRDRCONFIRMATIONFLAG(), swtDisposal.getF_CANCELFLAG());
		}
		else if (msgType == 210292) {
			cancelStatus = CancelFlag(swtDisposal.getF_CONFIRMATIONFLAG(), swtDisposal.getF_PAYMENTFLAG(), swtDisposal.getF_CRDRCONFIRMATIONFLAG(), swtDisposal.getF_CANCELFLAG());
		}
		else if (msgType == 900910) {
			cancelStatus = CancelFlag900910(swtDisposal.getF_CONFIRMATIONFLAG(), swtDisposal.getF_PAYMENTFLAG(), swtDisposal.getF_RECEIPTFLAG(), swtDisposal.getF_CANCELFLAG());
		}
		else {
			cancelStatus = CancelFlag(swtDisposal.getF_RECEIPTFLAG(), swtDisposal.getF_PAYMENTFLAG(), swtDisposal.getF_CRDRCONFIRMATIONFLAG(), swtDisposal.getF_CANCELFLAG());
		}
		return cancelStatus;
	}
}
