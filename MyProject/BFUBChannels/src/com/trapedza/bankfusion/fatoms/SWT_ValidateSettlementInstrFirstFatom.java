/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOBicCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtMaskConfig;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_ValidateSettlementInstrFirst;

public class SWT_ValidateSettlementInstrFirstFatom extends AbstractSWT_ValidateSettlementInstrFirst {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	private transient final static Log logger = LogFactory
			.getLog(SWT_ValidateSettlementInstrFirstFatom.class.getName());

	BankFusionEnvironment env;

	public SWT_ValidateSettlementInstrFirstFatom(BankFusionEnvironment env) {
		super(env);

	}

	public void process(BankFusionEnvironment env) {
		Iterator resultItr = null;

		IBOBicCodes bicCodes = null;

		this.env = env;
		setF_OUT_PayToText1(getF_IN_PayToDetails1());
		setF_OUT_PayToText2(getF_IN_PayToDetails2());
		setF_OUT_PayToText3(getF_IN_PayToDetails3());
		// Pay to BIC code validation
		if (getF_IN_PayToBICCode().length() > 0) {
			resultItr = validBICCode(getF_IN_PayToBICCode());
			if (!resultItr.hasNext()) {
				String[] err = { getF_IN_PayToBICCode() };
				displayMessifError(ChannelsEventCodes.E_IDENTIFIER_CODE_CANNOT_BE_FOUND, err, logger, env);
			}
			else {
				bicCodes = (IBOBicCodes) resultItr.next();
				setF_OUT_PayToText1(bicCodes.getF_NAME());
				setF_OUT_PayToText2(bicCodes.getF_CITY());
				setF_OUT_PayToText3(bicCodes.getF_LOCATION());
			}
		}
		// Validations for payTo
		
		if (getF_IN_PayToAccInfo().length() > 35) {
			String[] obj = { "PayTo Account Info ", getF_IN_PayToAccInfo() };
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, obj, logger, env);
		}
		if (!validText(getF_IN_PayToAccInfo())) {
			String[] err = { "PayToAccountInfo" };
			displayMessifError(ChannelsEventCodes.E_MASK_FORMAT_IS_INCORRECT, err, logger, env);
		}
		if (getF_IN_PayToDetails1().length() > 35)
		{
			String[] obj = { "PayTo Name ", getF_IN_PayToDetails1() };
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, obj, logger, env);
		}
			//setF_IN_PayToDetails1(getF_IN_PayToDetails1().substring(0, 35));
		if (getF_IN_PayToDetails2().length() > 35)
		{
			String[] obj = { "PayTo Address 1 ", getF_IN_PayToDetails2() };
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, obj, logger, env);
		}
			//setF_IN_PayToDetails2(getF_IN_PayToDetails2().substring(0, 35));
		if (getF_IN_PayToDetails3().length() > 35)
		{
			String[] obj = { "PayTo Address 2", getF_IN_PayToDetails3() };
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, obj, logger, env);
		}
			//setF_IN_PayToDetails3(getF_IN_PayToDetails3().substring(0, 35));
		
		
		
		// Validations for Beneficiary
		if (getF_IN_BeneAccInfo().length() > 35) {
		String[] err = { "Beneficiary Account Info ", getF_IN_BeneAccInfo()};
		displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, err, logger, env);
		}
		if (!validText(getF_IN_BeneAccInfo())) {
			String[] err = { "Beneficiary Account Info", getF_IN_BeneAccInfo()};
			displayMessifError(ChannelsEventCodes.E_MASK_FORMAT_IS_INCORRECT, err, logger, env);
		}

		if (getF_IN_BeneText1().length() > 35)
		{
			String[] obj = { "Beneficiary Name ", getF_IN_BeneText1() };
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, obj, logger, env);
		}
			//setF_IN_BeneText1(getF_IN_BeneText1().substring(0, 35));
		if (getF_IN_BeneText2().length() > 35)
		{
			String[] obj = { "Beneficiary Address 1 ", getF_IN_BeneText2() };
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, obj, logger, env);
		}
			//setF_IN_BeneText2(getF_IN_BeneText2().substring(0, 35));
		if (getF_IN_BeneText3().length() > 35)
		{
			String[] obj = { "Beneficiary Address 2 ", getF_IN_BeneText3() };
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, obj, logger, env);
		}
			//setF_IN_BeneText1(getF_IN_BeneText3().substring(0, 35));
		
		
		
		//Validations for Intermediary
		if (getF_IN_InterAccInfo().length() > 35) {
			String[] err = { "Intermediary Account Info", getF_IN_InterAccInfo() };
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, err, logger, env);
		}
		if (!validText(getF_IN_InterAccInfo())) {
			String[] err = { "Intermediary Account Info", getF_IN_InterAccInfo() };
			displayMessifError(ChannelsEventCodes.E_MASK_FORMAT_IS_INCORRECT, err, logger, env);
		}
		if (getF_IN_InterText1().length() > 35)
		{
			String[] obj = { "Intermediary Name ", getF_IN_InterText1() };
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, obj, logger, env);
		}
			//setF_IN_InterText1(getF_IN_InterText1().substring(0, 35));
		if (getF_IN_InterText2().length() > 35)
		{
			String[] obj = { "Intermediary Address 1 ", getF_IN_InterText2() };
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, obj, logger, env);
		}
			//setF_IN_InterText2(getF_IN_InterText2().substring(0, 35));
		if (getF_IN_InterText3().length() > 35)
		{
			String[] obj = { "Intermediary Address 2 ", getF_IN_InterText3() };
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, obj, logger, env);
		}
			//setF_IN_InterText3(getF_IN_InterText3().substring(0, 35));
		if (!validText(getF_IN_PayToAccInfo())) {
			String[] err = { "PayToAccountInfo" };
			displayMessifError(ChannelsEventCodes.E_MASK_FORMAT_IS_INCORRECT,err, logger,env);
		}

		

		setF_OUT_Bene_Text1(getF_IN_BeneText1());
		setF_OUT_Bene_Text2(getF_IN_BeneText1());
		setF_OUT_Bene_Text3(getF_IN_BeneText1());
		// Beneficiary BIC code validation
		if (getF_IN_BeneBICCode().length() > 0) {
			resultItr = validBICCode(getF_IN_BeneBICCode());
			if (!resultItr.hasNext()) {
				String[] err = { getF_IN_BeneBICCode() };
				displayMessifError(ChannelsEventCodes.E_IDENTIFIER_CODE_CANNOT_BE_FOUND, err, logger, env);
			}
			else {
				bicCodes = (IBOBicCodes) resultItr.next();
				setF_OUT_Bene_Text1(bicCodes.getF_NAME());
				setF_OUT_Bene_Text2(bicCodes.getF_CITY());
				setF_OUT_Bene_Text3(bicCodes.getF_LOCATION());
			}
		}

		if (getF_IN_BeneAccInfo().length() > 35) {
			String[] err = { "BeneficiaryAccountInfo" };
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS,err, logger,env);
		}

		if (!validText(getF_IN_BeneAccInfo())) {
			String[] err = { "BeneficiaryAccountInfo" };
			displayMessifError(ChannelsEventCodes.E_MASK_FORMAT_IS_INCORRECT,err, logger,env);
		}
		setF_OUT_Inter_Text1(getF_IN_InterText1());
		setF_OUT_Inter_Text2(getF_IN_InterText2());
		setF_OUT_Inter_Text3(getF_IN_InterText3());
		// Intermidiary BIC code validation
		if (getF_IN_InterBICCode().length() > 0) {
			resultItr = validBICCode(getF_IN_InterBICCode());
			if (!resultItr.hasNext()) {
				String[] err = { getF_IN_InterBICCode() };
				displayMessifError(ChannelsEventCodes.E_MASK_FORMAT_IS_INCORRECT, err, logger, env);
			}
			else {
				bicCodes = (IBOBicCodes) resultItr.next();
				setF_OUT_Inter_Text1(bicCodes.getF_NAME());
				setF_OUT_Inter_Text2(bicCodes.getF_CITY());
				setF_OUT_Inter_Text3(bicCodes.getF_LOCATION());
			}
		}

		if (getF_IN_InterAccInfo().length() > 35) {
			String[] err = { "Intermediary Account Info" };
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS,err, logger,env);
		}
		if (!validText(getF_IN_InterAccInfo())) {
			String[] err = { "Intermediary Account Info" };
			displayMessifError(ChannelsEventCodes.E_MASK_FORMAT_IS_INCORRECT,err, logger,env);
		}
		String debitAccountId = getF_IN_DebitAccountId();

		if (debitAccountId.length() > 0 && debitAccountId != null) {
			//debitAccountId="Debit Account ID "+debitAccountId;
			if (getF_IN_MessageType().equalsIgnoreCase("300"))
				validatingAccounts(env, debitAccountId, null);
			else
				validatingAccounts(env, debitAccountId, getF_IN_CURRENCYCODE());
		}
		String creditAccountId = getF_IN_CreditAccountId();
		if (creditAccountId.length() > 0 && creditAccountId != null) {
			//creditAccountId="Credit Account ID "+creditAccountId;
			validatingAccounts(env, creditAccountId, getF_IN_CURRENCYCODE());
		}
	}

	private void validatingAccounts(BankFusionEnvironment environment, String accountId, String CurrencyCode) {
		String whereClause = null;
		whereClause = "where " + IBOAccount.ACCOUNTID + " = ? ";
		ArrayList params = new ArrayList();
		ArrayList list = new ArrayList();
		params.add(accountId);
		list = (ArrayList) environment.getFactory().findByQuery(IBOAccount.BONAME, whereClause, params, null, false);
		if (CurrencyCode != null) {
			SimplePersistentObject simpleObject = (SimplePersistentObject) list.get(0);
			String Currency = (String) simpleObject.getDataMap().get(IBOAccount.ISOCURRENCYCODE);
			if (!(Currency.equals(CurrencyCode))) {
				//throw new BankFusionException(9434, new String[] { accountId, CurrencyCode }, logger, env);
				EventsHelper.handleEvent(ChannelsEventCodes.E_NOT_ISO_CURRENCY_FOR_ACCOUNT,  new String[] { accountId, CurrencyCode }, new HashMap(), environment);
			}
		}
	}

	private Iterator validBICCode(String bICCode) {
		String whereClause = "where " + IBOBicCodes.BICCODE + " = ? ";
		ArrayList params1 = new ArrayList();
		params1.add(bICCode);

		Iterator sourceIt = env.getFactory().findByQuery(IBOBicCodes.BONAME, whereClause, params1, null, false)
				.iterator();

		return sourceIt;
	}

	private boolean validText(String text) {
		
		int count =0;
		
		if(!text.equals("") && !text.substring(0, 1).equals("/"))
            displayMessifError(ChannelsEventCodes.E_ACCT_NUM_OR_BANK_CODE_WITH_SLASH_IS_REQ_NOT_BOTH, null, logger, env);
		
		for (int i = 2; i < text.length(); i++) {
			if (text.substring(i,i+1).equals("/")){
				displayMessifError(ChannelsEventCodes.E_ACCT_NUM_OR_BANK_CODE_WITH_SLASH_IS_REQ_NOT_BOTH, null, logger, env);
			}			
		}
		// Pay to Mask validation
		String whereClause = "where " + IBOSwtMaskConfig.MASKCODE + " = ? ";
		ArrayList params1 = new ArrayList();

		if (text.length() < 4) {
			return true;
		}
		String maskCode = text.substring(0, 4);
		params1.add(maskCode.trim());

		Iterator sourceIt = env.getFactory().findByQuery(IBOSwtMaskConfig.BONAME, whereClause, params1, null, false)
				.iterator();
		if (sourceIt.hasNext()) {
			IBOSwtMaskConfig maskDetails = (IBOSwtMaskConfig) sourceIt.next();
			if (!validateText(maskDetails.getF_MASKFORMAT(), text.substring(4))) {
				return false;
			}
			else {
				return true;
			}
		}
		else {
			return true;
		}
	}

	private boolean validateText(String f_mask_format, String inputString) {
		char c = ' ';
		String pattern = "(";
		for (int i = 0; i < f_mask_format.length(); i++) {
			c = f_mask_format.charAt(i);

			
			if (c == 'X') {
				pattern = pattern + "[(A-Z)]";
			} else if (c == 'x') {
				pattern = pattern + "[(a-z)|| (\\s)]";
			} else if (c == 'n') {
				pattern = pattern + "[(0-9)|| (\\s)]";
			} else if (c == 'N') {
				pattern = pattern + "[(0-9)]";
			}
			
		}
		pattern = pattern + ")";

		Pattern patternMatch = Pattern.compile(pattern);
		Matcher matcher = patternMatch.matcher(inputString);
		if (matcher.matches()) {
			return true;
		} else {
			return false;
		}
	}	
	/**
	 * Generic method to display errors
	 * @param val
	 * @param obj
	 * @param logger
	 * @param env
	 * @
	 */
	private void displayMessifError(int val, String[] obj, Log logger, BankFusionEnvironment env) {
		//throw new BankFusionException(val, obj, logger, env);
		EventsHelper.handleEvent(val, obj, new HashMap(), env);
	}
}
