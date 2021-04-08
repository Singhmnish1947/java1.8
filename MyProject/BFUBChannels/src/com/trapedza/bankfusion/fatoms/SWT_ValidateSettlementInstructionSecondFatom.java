
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
import com.trapedza.bankfusion.bo.refimpl.IBOSwtMaskConfig;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_ValidateSettlementInstructionSecond;

public class SWT_ValidateSettlementInstructionSecondFatom extends AbstractSWT_ValidateSettlementInstructionSecond {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	BankFusionEnvironment env;

	private transient final static Log logger = LogFactory.getLog(SWT_ValidateSettlementInstructionSecondFatom.class
			.getName());

	public SWT_ValidateSettlementInstructionSecondFatom(BankFusionEnvironment env) {
		super(env);

	}

	public void process(BankFusionEnvironment env) {
		this.env = env;
		ArrayList instructionList = new ArrayList();
		instructionList.add("PHON");
		instructionList.add("PHOB");
		instructionList.add("PHOI");
		instructionList.add("TELE");
		instructionList.add("TELB");
		instructionList.add("TELI");
		instructionList.add("HOLD");

		
		
		// Validations for Account description
		if (getF_IN_ForAccountInfo().length() > 35) {
			String[] obj = { "For Account Info ", getF_IN_ForAccountInfo() };
			//displayMessifError(9401, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, null, logger, env);

		}
		if (!validMaskText(getF_IN_ForAccountInfo())) {
			String[] obj = { "For Account Info ", getF_IN_ForAccountInfo() };
			//displayMessifError(9403, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_MASK_FORMAT_IS_INCORRECT, null, logger, env);

		}
		
		if(getF_IN_ForAccountDesc1().length()>35) {
				String[] obj = { "For Account Description 1 ", getF_IN_ForAccountDesc1() };
				//displayMessifError(9401, obj, logger, env);
				displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, null, logger, env);

				}
		
		if(getF_IN_ForAccountDesc2().length()>35) {
			String[] obj = { "For Account Description 2 ", getF_IN_ForAccountDesc2() };
		//	displayMessifError(9401, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, null, logger, env);

			}
		
		if(getF_IN_ForAccountDesc3().length()>35) {
			String[] obj = { "For Account Description 3 ", getF_IN_ForAccountDesc3() };
			//displayMessifError(9401, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, null, logger, env);

			}
		
		
		// Validations for Bank to Bank Information
		if (getF_IN_BnkToBnkInfo1().length() > 35) {
			String[] obj = { "Bank To Bank Information 1 ", getF_IN_BnkToBnkInfo1() };
			//displayMessifError(9401, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, null, logger, env);

		}
		if (!validTextForbankInfo1(getF_IN_BnkToBnkInfo1())) {
			String[] obj = { "Bank To Bank Information 1 " };
			//displayMessifError(9442, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_INCORRECT_FORMAT, null, logger, env);

		}

		if (getF_IN_BnkToBnkInfo2().length() > 35) {
			String[] obj = { "Bank To Bank Information 2 ", getF_IN_BnkToBnkInfo2() };
			//displayMessifError(9401, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, null, logger, env);

		}
		if (!validText(getF_IN_BnkToBnkInfo2())) {
			String[] obj = { "Bank To Bank Information 2 " };
			//displayMessifError(9442, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_INCORRECT_FORMAT, null, logger, env);

		}

		if (getF_IN_BnkToBnkInfo3().length() > 35) {
			String[] obj = { "Bank To Bank Information 3 ", getF_IN_BnkToBnkInfo3() };
			//displayMessifError(9401, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, null, logger, env);

		}
		if (!validText(getF_IN_BnkToBnkInfo3())) {
			String[] obj = { "Bank To Bank Information 3 " };
			//displayMessifError(9442, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_INCORRECT_FORMAT, null, logger, env);

		}

		if (getF_IN_BnkToBnkInfo4().length() > 35) {
			String[] obj = { "Bank To Bank Information 4 ", getF_IN_BnkToBnkInfo4() };
			//displayMessifError(9401, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, null, logger, env);

		}
		if (!validText(getF_IN_BnkToBnkInfo4())) {
			String[] obj = { "Bank To Bank Information 4 " };
		//	displayMessifError(9442, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_INCORRECT_FORMAT, null, logger, env);

		}

		if (getF_IN_BnkToBnkInfo5().length() > 35) {
			String[] obj = { "Bank To Bank Information 5 ", getF_IN_BnkToBnkInfo5() };
		//	displayMessifError(9401, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, null, logger, env);

		}
		if (!validText(getF_IN_BnkToBnkInfo5())) {
			String[] obj = { "Bank To Bank Information 5 " };
		//	displayMessifError(9442, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_INCORRECT_FORMAT, null, logger, env);

		}

		if (getF_IN_BnkToBnkInfo6().length() > 35) {
			String[] obj = { "Bank To Bank Information 6 ", getF_IN_BnkToBnkInfo6() };
			//displayMessifError(9401, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, null, logger, env);

		}
		if (!validText(getF_IN_BnkToBnkInfo6())) {
			String[] obj = { "Bank To Bank Information 6 " };
		//	displayMessifError(9442, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_INCORRECT_FORMAT, null, logger, env);

		}

		
		// validations for Payment Details
		if (getF_IN_PayDetails1().length() > 35) {
			String[] obj = { "Payment Details 1 ", getF_IN_PayDetails1() };
			//displayMessifError(9401, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, null, logger, env);

		}
		if (!validText(getF_IN_PayDetails1())) {
			String[] obj = { "Payment Details 1 " };
			//displayMessifError(9442, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_INCORRECT_FORMAT, null, logger, env);
		}

		if (getF_IN_PayDetails2().length() > 35) {
			String[] obj = { "Payment Details 2 ", getF_IN_PayDetails2() };
			//displayMessifError(9401, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, null, logger, env);
		}
		if (!validText(getF_IN_PayDetails2())) {
			String[] obj = { "Payment Details 2 " };
			//displayMessifError(9442, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_INCORRECT_FORMAT, null, logger, env);
		
		}

		if (getF_IN_PayDetails3().length() > 35) {
			String[] obj = { "Payment Details 3 ", getF_IN_PayDetails3() };
			//displayMessifError(9401, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, null, logger, env);
		}
		if (!validText(getF_IN_PayDetails3())) {
			String[] obj = { "Payment Details 3 " };
			//displayMessifError(9442, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_INCORRECT_FORMAT, null, logger, env);

			
		}

		if (getF_IN_PayDetails4().length() > 35) {
			String[] obj = { "Payment Details 4 ", getF_IN_PayDetails4() };
			//displayMessifError(9401, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, null, logger, env);

			
		}
		if (!validText(getF_IN_PayDetails4())) {
			String[] obj = { "Payment Details 4 " };
			//displayMessifError(9442, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_INCORRECT_FORMAT, null, logger, env);

			
			
		}
		if (getF_IN_PartyIdentifier() != null && getF_IN_PartyIdentifier().trim().length() > 0) {
			if (getF_IN_PartyIdentifier().startsWith("/")) {
				setF_OUT_PartyIdentifierCombo(getF_IN_PartyIdentifier().trim().substring(0, 1));
				if (getF_IN_PartyIdentifier().trim().length() == 1)
					setF_OUT_PartyIdentifierText(CommonConstants.EMPTY_STRING);
				else
					setF_OUT_PartyIdentifierText(getF_IN_PartyIdentifier().trim().substring(1,
							getF_IN_PartyIdentifier().trim().length()));
			}
			else {
				setF_OUT_PartyIdentifierCombo(getF_IN_PartyIdentifier().trim().substring(0, 5));
				if (getF_IN_PartyIdentifier().trim().length() == 5)
					setF_OUT_PartyIdentifierText(CommonConstants.EMPTY_STRING);
				else
					setF_OUT_PartyIdentifierText(getF_IN_PartyIdentifier().trim().substring(5,
							getF_IN_PartyIdentifier().trim().length()));
			}
		}
		/*
		 * BnkAddlInstructionCode should be entered only if the Instruction are
		 * any one of these: 'PHON','PHOB','PHOI','TELE','TELB','TELI','HOLD'
		 */
		if ((!instructionList.contains(getF_IN_BnkInstructionCode())) & (getF_IN_BnkAddlInstructionCode().length() > 0)) {
			String[] obj = { getF_IN_PayDetails4() };
		//	displayMessifError(9402, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_VALUE_NOT_ALLOWED_FOR_THE_BANK_INSTRUCTION_CODE, null, logger, env);

			
		}
	}

	private boolean validText(String text) {
		int n = 0;
		if (text.length() == 0) {
			return true;
		}

		/*if(text.length()==0){
			return true;
		}*/
		int length = text.length();

		for (int i = 0; i < length; i++) {
			if (text.charAt(i) == '/') {
				n = n + 1;
			}

		}
		if (n != 2) {
			return false;
		}
		else {
			return true;
		}
	}

	private boolean validTextForbankInfo1(String text) {
		int n = 0;
		if (text.length() == 0) {
			return true;
		}

		/*if(text.length()==0){
			return true;
		}*/
		int length = text.length();

		for (int i = 0; i < length; i++) {
			if (text.charAt(i) == '/') {
				n = n + 1;
			}

		}
		if (n != 2) {
			return false;
		}
		else {
			if (text.lastIndexOf("/") < 10 && text.lastIndexOf("/") > 2)
				return true;
			else
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

	private boolean validMaskText(String text) {
		int count =0;
		if(!text.equals("") && !text.substring(0, 1).equals("/"))
            //displayMessifError(9448, null, logger, env);
			displayMessifError(ChannelsEventCodes.E_ACCT_NUM_OR_BANK_CODE_WITH_SLASH_IS_REQ_NOT_BOTH, null, logger, env);

		
		for (int i = 2; i < text.length(); i++) {
			if (text.substring(i,i+1).equals("/")){
			//	displayMessifError(9448, null, logger, env);
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
				pattern = pattern + "[(A-Z)||(0-9)]";
			}
			else if (c == 'x') {
				pattern = pattern + "[(a-z)||(0-9)]";
			}
			else if (c == 'n') {
				pattern = pattern + "[(0-9)|| (\\s)]";
			}
			else if (c == 'N') {
				pattern = pattern + "[(0-9)]";
			}

		}
		pattern = pattern + ")";

		Pattern patternMatch = Pattern.compile(pattern);
		Matcher matcher = patternMatch.matcher(inputString);
		if (matcher.matches()) {
			return true;
		}
		else {
			return false;
		}
	}
}
