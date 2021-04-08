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
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOBicCodes;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_ValidateSettlementInstructionThird;

public class SWT_ValidateSettlementInstructionThirdFatom extends AbstractSWT_ValidateSettlementInstructionThird {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	
	BankFusionEnvironment env;

	int slases[] = new int[4];

	private transient final static Log logger = LogFactory.getLog(SWT_ValidateSettlementInstructionSecondFatom.class
			.getName());

	public SWT_ValidateSettlementInstructionThirdFatom(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) {
		this.env = env;
		Iterator resultItr = null;	
		if(getF_IN_MessageType().equals("103") && getF_IN_BankOpCode().equals("")){
			String[] err = { "Bank Operation Code for MT103"};
			//displayMessifError(9407, err, logger, env);
			displayMessifError(ChannelsEventCodes.E_CANNOT_BE_BLANK, err, logger, env);
		}
		/*
		 * ArrayList instructionList = new ArrayList();
		 * instructionList.add("ARNU/"); instructionList.add("CCPT/");
		 * instructionList.add("CUST/"); instructionList.add("DRLC/");
		 * instructionList.add("EMPL/"); instructionList.add("IBEI/");
		 * instructionList.add("NIDN/"); instructionList.add("SOSE/");
		 * instructionList.add("TXID/"); instructionList.add("/");
		 */

		ArrayList instructionList1 = new ArrayList();
		instructionList1.add("PHON");
		instructionList1.add("PHOB");
		instructionList1.add("PHOI");
		instructionList1.add("TELE");
		instructionList1.add("TELB");
		instructionList1.add("TELI");
		instructionList1.add("HOLD");

		/*
		 * BnkAddlInstructionCode should be entered only if the Instruction are
		 * any one of these: 'PHON','PHOB','PHOI','TELE','TELB','TELI','HOLD'
		 */

		if ((!instructionList1.contains(getF_IN_BnkInstructionCode()))
				& (getF_IN_BnkAddlInstructionCode().length() > 0)) {
			String[] obj = { getF_IN_BnkAddlInstructionCode() };
			//displayMessifError(9402, obj, logger, env);
			displayMessifError(ChannelsEventCodes.E_VALUE_NOT_ALLOWED_FOR_THE_BANK_INSTRUCTION_CODE, obj, logger, env);
		}
		if (getF_IN_PartyIdentifierCombo() != null && getF_IN_PartyIdentifier() != null) {
			if (getF_IN_PartyIdentifier().length() > 35) {
				String[] err = { "PartyIdentifier", " " };
				//throw new BankFusionException(9401, err, logger, env);
				EventsHelper.handleEvent(ChannelsEventCodes.E_LENGTH_IS_MORE_THAN_35_CHARACTERS, err, new HashMap(), env);
			}
			if (getF_IN_PartyIdentifierCombo().trim().length() <= 0 && getF_IN_PartyIdentifier().trim().length() > 0) {

				//throw new BankFusionException(9441, null, logger, env);
				EventsHelper.handleEvent(ChannelsEventCodes.E_INCORRECT_PARTY_ID_TEXT, new Object[]{}, new HashMap(), env);
			}
		}
		try {
			getSlases();
			if (getF_IN_PartyIdentifier().trim().length() == 0) {
				checkAllEmpty();
			}
			if (getF_IN_PartyIdentifier().trim().length() > 0 && slases[0] == -1) {

				String[] err = { "Party address line 1" };
				//displayMessifError(9407, err, logger, env);
				displayMessifError(ChannelsEventCodes.E_CANNOT_BE_BLANK, err, logger, env);
			}
			ValidSlases();
			isSlasesOrder();
			checkForPair(2, 3);
			checkForPair(4, 5);
			boolean valid = validateNumberSlases(getF_IN_PartyAddress1());
			if (!valid) {
				String[] err = { "The format of  Party Address line 1 is Invalid" };
				//throw new BankFusionException(9436, err, logger, env);
				EventsHelper.handleEvent(ChannelsEventCodes.E_ERROR, err, new HashMap(), env);
			}
			valid = validateNumberSlases(getF_IN_PartyAddress2());
			if (!valid) {
				String[] err = { "The format of  Party Address line 2 is Invalid" };
				//throw new BankFusionException(9436, err, logger, env);
				EventsHelper.handleEvent(ChannelsEventCodes.E_ERROR, err, new HashMap(), env);
			}
			valid = validateNumberSlases(getF_IN_PartyAddress3());
			if (!valid) {
				String[] err = { "The format of  Party Address line 3 is Invalid" };
				//throw new BankFusionException(9436, err, logger, env);
				EventsHelper.handleEvent(ChannelsEventCodes.E_ERROR, err, new HashMap(), env);
			}
			valid = validateNumberSlases(getF_IN_PartyAddress4());
			if (!valid) {
				String[] err = { "The format of  Party Address line 4 is Invalid" };
				//throw new BankFusionException(9436, err, logger, env);
				EventsHelper.handleEvent(ChannelsEventCodes.E_ERROR, err , new HashMap(), env);
			}
		}
			catch (NumberFormatException nfe) {
						//throw new BankFusionException(9435, null, null, env);
				EventsHelper.handleEvent(ChannelsEventCodes.E_FIRST_CHARACTER_NOT_INTEGER_IN_ADDRESS, new Object[]{}, new HashMap(), env);
		}

//		 Ordering Institution validation
		if (getF_IN_OrderingInstitution().length() > 0) {
			resultItr = validBICCode(getF_IN_OrderingInstitution());
			if (!resultItr.hasNext()) {
				String[] err = { getF_IN_OrderingInstitution() };
				//displayMessifError(9404,err, logger,env);
				displayMessifError(ChannelsEventCodes.E_IDENTIFIER_CODE_CANNOT_BE_FOUND,err, logger,env);
			}
		}

	}
	private Iterator validBICCode(String bICCode) {
		String whereClause = "where " + IBOBicCodes.BICCODE + " = ? ";
		ArrayList params1 = new ArrayList();
		Iterator sourceIt = null;
		params1.add(bICCode);
		
		
			sourceIt = env.getFactory().findByQuery(IBOBicCodes.BONAME,
					whereClause, params1, null).iterator();

		

		return sourceIt;
	}
	/*This method is  for checking messege type (2,3) and (4,5) */
	private void checkForPair(int j, int k) {
		if (!((check(j) && check(k)) || (!check(j) && !check(k)))) {
			String[] err = { "Address " + j + " is not allowed without address " + k + " and vice versa" };
			//displayMessifError(9436, err, logger, env);
			displayMessifError(ChannelsEventCodes.E_ERROR, err, logger, env);
		}

	}

	// this is return a adress is there or not
	private boolean check(int val) {
		for (int i = 0; i < 4; i++) {
			if (slases[i] == val)
				return true;
		}
		return false;
	}

	//checking order of slases
	private void isSlasesOrder() {
		int prev = -1;
		for (int i = 0; i < 4; i++) {
			if (slases[i] != -1 && prev >= slases[i]) {
				//displayMessifError(9411, null, logger, env);
				displayMessifError(ChannelsEventCodes.E_ADDRESS_SHOULD_BE_IN_INCREASING_ORDER, null, logger, env);
			}
			else {
				prev = slases[i];
			}
		}
	}

	/*Checking the vaue between 1 and 8 and also it will check the order of the address*/
	private void ValidSlases() {

		boolean flag = false;
		for (int k = 0; k < 4; k++) {
			if (slases[k] == 1)
				continue;
			if ((slases[k] < 1 && slases[k] > 8)) {
				//displayMessifError(9409, null, logger, env);
				displayMessifError(ChannelsEventCodes.E_NUMERIC_VALUE_SHOULD_BE_BETWEEN_1_TO_8, null, logger, env);
			}

		}
		int i = 0;
		for (i = 3; i >= 0; i--) {
			if (slases[i] != -1) {
				flag = true;
				break;
			}
		}
		for (int j = 0; j <= i; j++) {
			if (slases[j] == -1 && flag) {
				int r = j + 1;
				int s = i + 1;
				String[] err = { "Party address line " + s + " not allowed with out Party address line " + r };
				//displayMessifError(9436, err, logger, env);
				displayMessifError(ChannelsEventCodes.E_ERROR, err, logger, env);
			}
		}

	}

	//check is all are empty when party identifier is empty
	private void checkAllEmpty() {
		for (int i = 0; i < 4; i++) {
			int k = i + 1;
			if (slases[i] != -1) {
				String[] err = { "Party address line " + k };
				//displayMessifError(9445, err, logger, env);
				displayMessifError(ChannelsEventCodes.E_PARTY_ADDR_LINE_NOT_ALLOWED_IF_PARTY_ID_IS_BLANK, err, logger, env);
			}
		}

	}

	//assigning all the number in a array if one address is not there -1 will be stored
	private void getSlases() {
		slases[0] = getF_IN_PartyAddress1().trim().length() <= 0 ? -1 : Integer.parseInt(getF_IN_PartyAddress1().trim()
				.substring(0, 1));
		slases[1] = getF_IN_PartyAddress2().trim().length() <= 0 ? -1 : Integer.parseInt(getF_IN_PartyAddress2().trim()
				.substring(0, 1));
		slases[2] = getF_IN_PartyAddress3().trim().length() <= 0 ? -1 : Integer.parseInt(getF_IN_PartyAddress3().trim()
				.substring(0, 1));
		slases[3] = getF_IN_PartyAddress4().trim().length() <= 0 ? -1 : Integer.parseInt(getF_IN_PartyAddress4().trim()
				.substring(0, 1));
	}

	/*
	 * private void validateAddress1() { if
	 * (getF_IN_PartyIdentifier().trim().length() > 0 &&
	 * getF_IN_PartyAddress1().trim().length() == 0){
	 *
	 * String[] err = { "Party address line 1" }; displayMessifError(9407, err,
	 * logger, env); } if (getF_IN_PartyAddress1().trim().length() > 0){ if
	 * (getF_IN_PartyIdentifier().trim().length() == 0){ String[] err = { "Party
	 * identifier" }; displayMessifError(9407, err, logger, env); }
	 *
	 * if (Integer.parseInt(getF_IN_PartyAddress1().trim().substring(0, 1)) > 0 &&
	 * Integer.parseInt(getF_IN_PartyAddress1().trim().substring(0, 1)) < 9){ if
	 * (Integer.parseInt(getF_IN_PartyAddress1().trim().substring(0, 1)) ==3 ||
	 * Integer.parseInt(getF_IN_PartyAddress1().trim().substring(0, 1)) == 5){
	 * String[] err = { }; displayMessifError(9408, err, logger, env); } } else {
	 * String[] err = { }; throw new BankFusionException(9409, err,
	 * logger, env); } boolean valid =
	 * validateNumberSlases(getF_IN_PartyAddress1());
	 *
	 * if (!valid){ String[] err = { "The format of Party Address line 1 is
	 * Invalid" }; throw new BankFusionException(9436, err, logger,
	 * env); } } else { if (getF_IN_PartyAddress1().trim().length() > 0){
	 * String[] err = { "Party identifier"}; displayMessifError(9407, err,
	 * logger, env); } } } private void validateAddress2() throws
	 * BankFusionException{ if (getF_IN_PartyAddress2().trim().length() > 0){ if
	 * (getF_IN_PartyIdentifier().trim().length() == 0){ String[] err = { "Party
	 * identifier"}; displayMessifError(9407, err, logger, env); } } if
	 * (getF_IN_PartyAddress2().trim().length() > 0){ if
	 * (Integer.parseInt(getF_IN_PartyAddress2().trim().substring(0, 1)) < 1 &&
	 * Integer.parseInt(getF_IN_PartyAddress2().trim().substring(0, 1)) > 8){
	 * String[] err = { }; displayMessifError(9409, err, logger, env); } if
	 * (Integer.parseInt(getF_IN_PartyAddress2().trim().substring(0, 1)) <
	 * Integer.parseInt(getF_IN_PartyAddress1().trim().substring(0, 1))){
	 * String[] err = { }; displayMessifError(9411, err, logger, env); }
	 * if(getF_IN_PartyAddress1().trim().length()<=0){ String[] err =
	 * {"PartyAddress line 1" }; displayMessifError(9407, err, logger, env); } /*
	 * if ((Integer.parseInt(getF_IN_PartyAddress2().trim().substring(0, 1)) ==
	 * 3 && Integer.parseInt(getF_IN_PartyAddress1().trim().substring(0, 1)) !=
	 * 2) || (Integer.parseInt(getF_IN_PartyAddress2().trim().substring(0, 1)) ==
	 * 5 && Integer.parseInt(getF_IN_PartyAddress1().trim().substring(0, 1)) !=
	 * 4) || (Integer.parseInt(getF_IN_PartyAddress1().trim().substring(0, 1)) ==
	 * 2 && Integer.parseInt(getF_IN_PartyAddress2().trim().substring(0, 1)) !=
	 * 3) || (Integer.parseInt(getF_IN_PartyAddress1().trim().substring(0, 1)) ==
	 * 4 && Integer.parseInt(getF_IN_PartyAddress2().trim().substring(0, 1)) !=
	 * 5)){ String[] err = { }; displayMessifError(9410, err, logger, env); }
	 */
	/*
	 * boolean valid = validateNumberSlases(getF_IN_PartyAddress2()); if
	 * (!valid){ String[] err = { "The format of Party Address line 2 is
	 * Invalid" }; throw new BankFusionException(9436, err, logger,
	 * env); } } }
	 */

	/*
	 * private void validateAddress3() { if
	 * (getF_IN_PartyAddress3().trim().length() > 0){ if
	 * (getF_IN_PartyIdentifier().trim().length() == 0){ String[] err = { "Party
	 * identifier"}; displayMessifError(9407, err, logger, env); } } if
	 * (getF_IN_PartyAddress3().trim().length() > 0){ if
	 * (Integer.parseInt(getF_IN_PartyAddress3().trim().substring(0, 1)) < 1 &&
	 * Integer.parseInt(getF_IN_PartyAddress3().trim().substring(0, 1)) > 8){
	 * String[] err = { }; displayMessifError(9409, err, logger, env); } if
	 * (Integer.parseInt(getF_IN_PartyAddress3().trim().substring(0, 1)) <
	 * Integer.parseInt(getF_IN_PartyAddress2().trim().substring(0, 1))){
	 * String[] err = { }; displayMessifError(9411, err, logger, env); }
	 *
	 * /*if ((Integer.parseInt(getF_IN_PartyAddress3().trim().substring(0, 1)) ==
	 * 3 && Integer.parseInt(getF_IN_PartyAddress2().trim().substring(0, 1)) !=
	 * 2) || (Integer.parseInt(getF_IN_PartyAddress3().trim().substring(0, 1)) ==
	 * 5 && Integer.parseInt(getF_IN_PartyAddress2().trim().substring(0, 1)) !=
	 * 4) || (Integer.parseInt(getF_IN_PartyAddress2().trim().substring(0, 1)) ==
	 * 2 && Integer.parseInt(getF_IN_PartyAddress3().trim().substring(0, 1)) !=
	 * 3) || (Integer.parseInt(getF_IN_PartyAddress2().trim().substring(0, 1)) ==
	 * 4 && Integer.parseInt(getF_IN_PartyAddress3().trim().substring(0, 1)) !=
	 * 5)){ String[] err = { }; displayMessifError(9410, err, logger, env); }
	 */

	/*
	 * boolean valid = validateNumberSlases(getF_IN_PartyAddress3()); if
	 * (!valid){ String[] err = { "IThe format of Party Address line 3 is
	 * Invalid" }; throw new BankFusionException(9436, err, logger,
	 * env); } } else{ if (getF_IN_PartyAddress2().trim().length() != 0){ if
	 * (Integer.parseInt(getF_IN_PartyAddress2().trim().substring(0, 1)) == 2 ||
	 * Integer.parseInt(getF_IN_PartyAddress2().trim().substring(0, 1)) == 4){
	 * String[] err = { }; displayMessifError(9410, err, logger, env); } } } }
	 *
	 *
	 * private void validateAddress4() { if
	 * (getF_IN_PartyAddress4().trim().length() > 0){ if
	 * (getF_IN_PartyIdentifier().trim().length() == 0){ String[] err = { "Party
	 * identifier"}; displayMessifError(9407, err, logger, env); } } if
	 * (getF_IN_PartyAddress4().trim().length() > 0){ if
	 * (Integer.parseInt(getF_IN_PartyAddress4().trim().substring(0, 1)) < 1 &&
	 * Integer.parseInt(getF_IN_PartyAddress4().trim().substring(0, 1)) > 8){
	 * String[] err = { }; displayMessifError(9409, err, logger, env); } if
	 * (Integer.parseInt(getF_IN_PartyAddress4().trim().substring(0, 1)) <
	 * Integer.parseInt(getF_IN_PartyAddress3().trim().substring(0, 1))){
	 * String[] err = { }; displayMessifError(9411, err, logger, env); } /* if
	 * ((Integer.parseInt(getF_IN_PartyAddress4().trim().substring(0, 1)) == 3 &&
	 * Integer.parseInt(getF_IN_PartyAddress3().trim().substring(0, 1)) != 2) ||
	 * (Integer.parseInt(getF_IN_PartyAddress4().trim().substring(0, 1)) == 5 &&
	 * Integer.parseInt(getF_IN_PartyAddress3().trim().substring(0, 1)) != 4) ||
	 * (Integer.parseInt(getF_IN_PartyAddress3().trim().substring(0, 1)) == 2 &&
	 * Integer.parseInt(getF_IN_PartyAddress4().trim().substring(0, 1)) != 3) ||
	 * (Integer.parseInt(getF_IN_PartyAddress3().trim().substring(0, 1)) == 4 &&
	 * Integer.parseInt(getF_IN_PartyAddress4().trim().substring(0, 1)) != 5) ||
	 * Integer.parseInt(getF_IN_PartyAddress4().trim().substring(0, 1)) == 4 ||
	 * Integer.parseInt(getF_IN_PartyAddress4().trim().substring(0, 1)) == 2 ){
	 * String[] err = { }; displayMessifError(9410, err, logger, env); }
	 */
	/*
	 * boolean valid = validateNumberSlases(getF_IN_PartyAddress4()); if
	 * (!valid){ String[] err = { "The format of Party Address line 4 is
	 * Invalid" }; throw new BankFusionException(9436, err, logger,
	 * env); } } else{ if (getF_IN_PartyAddress3().trim().length() != 0){ if
	 * (Integer.parseInt(getF_IN_PartyAddress3().trim().substring(0, 1)) == 2 ||
	 * Integer.parseInt(getF_IN_PartyAddress3().trim().substring(0, 1)) == 4){
	 * String[] err = { }; displayMessifError(9410, err, logger, env); } } } }
	 */
	//validating the address
	private boolean validateNumberSlases(String Address) {

		Address = Address.trim();
		if (Address.length() == 0)
			return true;
		StringTokenizer tokenAddress = new StringTokenizer(Address, "/");
		int count = tokenAddress.countTokens() - 1;
		if (Integer.parseInt(Address.substring(0, 1)) == 1 || Integer.parseInt(Address.substring(0, 1)) == 2
				|| Integer.parseInt(Address.substring(0, 1)) == 8) {
			if (count == 1 && Address.indexOf('/') == 1)
				return true;
			else
				return false;
		}
		if (Integer.parseInt(Address.substring(0, 1)) == 3 || Integer.parseInt(Address.substring(0, 1)) == 5
				|| Integer.parseInt(Address.substring(0, 1)) == 6 || Integer.parseInt(Address.substring(0, 1)) == 7) {
			if (count == 2 && Address.indexOf('/') == 1 && Address.lastIndexOf('/') != 2)
				return true;
			else
				return false;
		}

		if (Integer.parseInt(Address.substring(0, 1)) == 4) {
			if (count == 1 && Address.indexOf('/') == 1 && Address.length() == 10) {
				try {
					int i = Integer.parseInt(Address.substring(2, 10));
				}
				catch (NumberFormatException nfe) {
					return false;
					// throw new BankFusionException(9435, null,
					// null, env);
				}
				return true;
			}
			else
				return false;
		}
		return true;
	}

	/**
	 * Generic method to display errors
	 *
	 * @param val
	 * @param obj
	 * @param logger
	 * @param env
	 * @
	 */
	private void displayMessifError(int val, String[] obj, Log logger, BankFusionEnvironment env) {
		try {
			//throw new BankFusionException(val, obj, logger, env);
			EventsHelper.handleEvent(val, obj, new HashMap(),env);

		}
		catch (Exception e) {
			//throw new BankFusionException(val, obj, logger, env);
			EventsHelper.handleEvent(val, obj, new HashMap(),env);
         logger.error(ExceptionUtil.getExceptionAsString(e));
		}
	}
}
