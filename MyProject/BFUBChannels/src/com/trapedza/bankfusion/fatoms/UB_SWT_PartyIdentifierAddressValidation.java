/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 * $Id: UB_SWT_PartyIdentifierAddressValidation.java,v 1.3 2008/10/12 12:20:22 gaurava Exp $
 **/
package com.trapedza.bankfusion.fatoms;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_PartyIdentifierAddressValidation;
@SuppressWarnings("PMD")
public class UB_SWT_PartyIdentifierAddressValidation extends
		AbstractUB_SWT_PartyIdentifierAddressValidation {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}
	private transient final static Log logger = LogFactory
			.getLog(UB_SWT_PartyIdentifierAddressValidation.class.getName());

	public static final String Default103Plus="Yes";
	public String Errormsg = CommonConstants.EMPTY_STRING;
	public Boolean result = false;
	public String validationStatus = "false";
	public static final String MASK_PREFIX= "//";
	public int flagcount=11;
	

	/**
	 * @param env
	 *            constructor for bankfusion environment.
	 */
	public UB_SWT_PartyIdentifierAddressValidation(BankFusionEnvironment env) {
		super(env);
	}

	int slases[] = new int[4];

	BankFusionEnvironment env;



	public void process(BankFusionEnvironment env) {
		this.env = env;
		// Swift 2009 changes

		this.FundFlowValidation();
		this.IBANValidation();
		this.GeneralValidation();
		this.AddressValidation();
		this.FirstValidation();
		if (Errormsg.equals(CommonConstants.EMPTY_STRING))
		{ this.FirstValidationFor103Plus();
		}
		this.SetOutputParams();
	}


	public void FundFlowValidation() {

		if (getF_IN_orderingCustomerAccountNumber().contains("/")) {
			String[] Params = { "Ordering Customer" };
			displayMessifError(
					ChannelsEventCodes.E_SI_SLASHES_NOT_ALLOWED, Params,
					logger, env);

		}

		Map hmpParams = new HashMap();
		hmpParams.put("IdentifierCode",
				getF_IN_orderingCustomerIdentifierCode());
		hmpParams.put("NameAndAddress1", "");
		hmpParams.put("NameAndAddress2", "");
		hmpParams.put("NameAndAddress3", "");
		hmpParams.put("NameAndAddress4", "");
		Map result = MFExecuter.executeMF(
				"UB_SWT_IdentifierCodeToValidate_SRV", env, hmpParams);
        if (getF_IN_OrderingCustomerIdentifierCombo().equalsIgnoreCase("P")
                && !getF_IN_orderingCustomerIdentifierCode().isEmpty()) {

					Map hpParams = new HashMap();
					hpParams.put("IdentifierCode",
							getF_IN_orderingCustomerIdentifierCode());
					Map result1 = MFExecuter.executeMF(
							"UB_SWT_IdentifierCodeRead_SRV", env, hpParams);
					Integer NoOfRows = (Integer) result1.get("NoOfRows");
					if (NoOfRows == 0) {
						String[] Params = { "" };
						EventsHelper.handleEvent(
								ChannelsEventCodes.I_SWT_ORDER_CUST_IDENTIFIER_UB15, Params,
								new HashMap(), env);

				}
			}

		}


	public void IBANValidation() {

		if (!((!getF_IN_OrderingCustomerIdentifierCombo().isEmpty() && !getF_IN_orderingCustomerAccountNumber()
				.isEmpty()) || (getF_IN_OrderingCustomerIdentifierCombo()
				.isEmpty() && getF_IN_orderingCustomerAccountNumber().isEmpty()))) {
			String[] Params = { "Ordering Customer" };
			EventsHelper.handleEvent(ChannelsEventCodes.E_SI_PARTY_IDENTIFIER_CANNOT_BE_BLANK,
					Params, new HashMap(), env);
		}

		if (getF_IN_OrderingCustomerIdentifierCombo().equalsIgnoreCase("I")) {
			String iban = getF_IN_orderingCustomerAccountNumber();
			int ibanLength = getF_IN_orderingCustomerAccountNumber().length();
			Map hmpParams = new HashMap();
			hmpParams.put("CountryCode2Char", iban.substring(0, 2));
			Map result = MFExecuter.executeMF(
					"UB_SWT_Validate2CharCountryCode_SRV", env, hmpParams);
			Boolean isCountryCodeInvalid = (Boolean) result
					.get("IsShortCountryCodeValid");
			if (!isCountryCodeInvalid.booleanValue()) {
				EventsHelper.handleEvent(
						ChannelsEventCodes.E_SI_INVALID_COUNTRY_CODE,
						new Object[] {}, new HashMap(), env);
			}
			if ((ibanLength) != (Integer) (result.get("IBANLength"))) {
				EventsHelper
						.handleEvent(
								ChannelsEventCodes.E_SI_INVALID_IBAN_LENGTH,
								new Object[] {}, new HashMap(), env);
			}

		}

	}

	public void GeneralValidation() {
		if (getF_IN_PartyIdentifierCombo().trim().length() > 1) {
			// Validating for Country code mandatory
			if (getF_IN_PartyIdentifierCountryCode().length() == 0) {
				String Params[] = { "Country" };
				EventsHelper.handleEvent(ChannelsEventCodes.E_CANNOT_BE_BLANK,
						Params, new HashMap(), env);
			}

		}
		// else if(getF_IN_PartyIdentifierCombo().trim().length()==1){

		int countryLength = (getF_IN_PartyIdentifierCountryCode().trim()
				.length() > 0 ? 3 : 0);
		int partyIdentiferLength = getF_IN_PartyIdentifierCombo().length() == 4 ? 5
				: getF_IN_PartyIdentifierCombo().trim().length();
		if (getF_IN_PartyIdentifier().length() + partyIdentiferLength
				+ countryLength > 35) {

			String[] err = { "PartyIdentifier" };

			EventsHelper.handleEvent(
					ChannelsEventCodes.E_PARTYIDENTIFIER_EXCEED_34_CHAR, err,
					new HashMap(), env);
		}

		// }
		/*
		 * if (getF_IN_PartyIdentifier().length() > 35) { String[] err = {
		 * "PartyIdentifier", " " }; //throw new BankFusionException(9401, err,
		 * logger, env);EventsHelper.handleEvent(ChannelsEventCodes.
		 * E_LENGTH_IS_MORE_THAN_35_CHARACTERS, new Object[]{}, new HashMap(),
		 * env); }
		 */
		if (getF_IN_PartyIdentifierCombo().trim().length() <= 0
				&& getF_IN_PartyIdentifier().trim().length() > 0) {

			// throw new BankFusionException(9441, null, logger, env);
			EventsHelper.handleEvent(
					ChannelsEventCodes.E_INCORRECT_PARTY_ID_TEXT,
					new Object[] {}, new HashMap(), env);
		}

		// MT103+ addition starts

		/**
		 * to check ordering customer account number and party identifier both
		 * are not populated.
		 */

		if (getF_IN_orderingCustomerAccountNumber().length() != 0
				&& (getF_IN_PartyIdentifier().length() != 0 || getF_IN_PartyIdentifierCombo()
						.trim().length() != 0)) {
			displayMessifError(
					ChannelsEventCodes.E_ORDERING_CUST_ACCT_NUM_AND_PARTY_ID_EXISTS_SIMUL,
					null, logger, env);
		}
		/**
		 * to check when the ordering customer account number is populated then
		 * address line 1 to 4 or ordering customer identifier code is mandatory
		 */
		if (getF_IN_orderingCustomerAccountNumber().length() != 0) {
			if ((getF_IN_PartyAddress1().length() != 0
					&& getF_IN_PartyAddress2().length() != 0
					&& getF_IN_PartyAddress3().length() != 0 && getF_IN_PartyAddress4()
					.length() != 0)
					&& getF_IN_orderingCustomerIdentifierCode().length() != 0) {
				displayMessifError(
						ChannelsEventCodes.E_ADDR_LINE_TO_AND_ORD_CUST_ID_CODE_EXISTS_SIMUL,
						null, logger, env);
			} else if (getF_IN_orderingCustomerIdentifierCode().length() == 0
					&& getF_IN_PartyAddress1().length() == 0
					&& getF_IN_PartyAddress2().length() == 0) {
				displayMessifError(
						ChannelsEventCodes.E_REQUIRE_CUST_NAME_ADDR_OR_ORDERING_CUST_ID_CODE,
						null, logger, env);
			}
		}

		/**
		 * to check if party identifier is given then address line1 is mandatory
		 * and Ordering Account number and Ordering customer Identifier code
		 * should be blank.
		 */
		if ((getF_IN_PartyIdentifier().length() != 0 || getF_IN_PartyIdentifierCombo()
				.trim().length() != 0)) {
			if (getF_IN_PartyAddress1().length() == 0)
				displayMessifError(
						ChannelsEventCodes.E_ADDRESS_LINE_IS_MANDATORY_WITH_PARTY_IDENTIFIER,
						null, logger, env);
			else if (getF_IN_orderingCustomerAccountNumber().length() != 0)
				displayMessifError(
						ChannelsEventCodes.E_ORDERING_CUST_ACCT_NUM_AND_PARTY_ID_EXISTS_SIMUL,
						null, logger, env);
			else if (getF_IN_orderingCustomerIdentifierCode().length() != 0)
				displayMessifError(
						ChannelsEventCodes.E_CUST_ID_CODE_NOT_BLANK_WHEN_PARTY_ID_CODE_EXISTS,
						null, logger, env);
		}

		/**
		 * to check if address line 1 is populated then ordering customer
		 * account number and party identifier code both should not be given
		 * simultaneously and Ordering customer identifier code should not be
		 * given.
		 */
		if (getF_IN_PartyAddress1().length() != 0) {
			if (getF_IN_orderingCustomerAccountNumber().length() != 0
					&& (getF_IN_PartyIdentifier().length() != 0 || getF_IN_PartyIdentifierCombo()
							.trim().length() != 0))
				displayMessifError(
						ChannelsEventCodes.E_ORDERING_CUST_ACCT_NUM_AND_PARTY_ID_CODE_SIMUL,
						null, logger, env);
			else if (getF_IN_orderingCustomerIdentifierCode().length() != 0)
				displayMessifError(
						ChannelsEventCodes.E_ORDERING_CUSTOMER_IDENTIFIER_CODE_REQUIRED,
						null, logger, env);
		}

		/**
		 * to check if ordering customer identifier code is populated then party
		 * identifier and address lines should not be populated
		 */
		if (getF_IN_orderingCustomerIdentifierCode().length() != 0) {
			if (getF_IN_PartyIdentifier().length() != 0
					|| getF_IN_PartyIdentifierCombo().trim().length() != 0)
				displayMessifError(
						ChannelsEventCodes.E_PARTY_IDENTIFIER_SHOULD_NOT_BE_POPULATED,
						null, logger, env);
			else if (getF_IN_PartyAddress1().length() != 0
					&& getF_IN_PartyAddress2().length() != 0
					&& getF_IN_PartyAddress3().length() != 0
					&& getF_IN_PartyAddress4().length() != 0)
				displayMessifError(
						ChannelsEventCodes.E_ORDERING_CUSTOMER_IDENTIFIER_CODE_REQUIRED,
						null, logger, env);
		}
		if (getF_IN_MessageType().equals("103")
				|| getF_IN_Generate103Plus().equals("Y")) {
			/**
			 * to check if message type is 205 then ordering institute
			 * identifier code is mandatory
			 */
			if (getF_IN_MessageType().equals("205")) {
				if (getF_IN_orderingInstituteIdentifierCode().length() == 0) {
					displayMessifError(
							ChannelsEventCodes.E_ORDERING_INSTITUTE_IDENTIFIER_CODE_IS_MANDATORY,
							null, logger, env);
				}
			}

			/**
			 * to check Ordering Customer Identifier Code, Ordering Customer
			 * Account Number and Party Address Line1 could not be blank
			 * simultaneously
			 */
			if (getF_IN_orderingCustomerIdentifierCode().length() == 0
					&& getF_IN_orderingCustomerAccountNumber().length() == 0
					&& getF_IN_PartyAddress1().length() == 0) {
				displayMessifError(
						ChannelsEventCodes.E_CUST_ID_CODE_ACCT_NUM_PARTY_ADDR_NOT_BLANK_SIMUL,
						null, logger, env);
			}

		}
	}

	/**
	 * to check if Address line 1 is populated alongwith party identifier then
	 * validations for ordering customer option F needs to be done else the
	 * length of address line will be checked for length of 35 characters.
	 */

	public void AddressValidation() {
		if (getF_IN_PartyAddress1().length() != 0) {
			if (getF_IN_PartyIdentifier().length() != 0
					|| getF_IN_PartyIdentifierCombo().trim().length() != 0
                    || getF_IN_PartyIdentifierCountryCode().length() != 0
                    || getF_IN_orderingCustomerAccountNumber().length() != 0 && !getF_IN_PartyAddress1().isEmpty()
                            && (getF_IN_PartyAddress1().length() >= 2)
                            && getF_IN_PartyAddress1().substring(0, 2).equalsIgnoreCase("1/")) {
				// MT103+ addition ends
				try {
					getSlases();
					if (getF_IN_PartyIdentifier().trim().length() == 0&&getF_IN_orderingCustomerAccountNumber().length() == 0) {
						checkAllEmpty();
					}
					if (getF_IN_PartyIdentifier().trim().length() > 0
							&& slases[0] == -1&&getF_IN_orderingCustomerAccountNumber().length() == 0) {

						String[] err = { "Party address line 1" };
						displayMessifError(
								ChannelsEventCodes.E_CANNOT_BE_BLANK, err,
								logger, env);
					}

					if (slases[0] == 1) {
						ValidSlases();
						isSlasesOrder();
				
					
						if((!getF_IN_PartyAddress2().isEmpty()&&getF_IN_PartyAddress2().substring(0, 1).equalsIgnoreCase("2")||!getF_IN_PartyAddress2().isEmpty()&&getF_IN_PartyAddress2().substring(0, 1).equalsIgnoreCase("3"))
							||!getF_IN_PartyAddress3().isEmpty()&&(getF_IN_PartyAddress3().substring(0, 1).equalsIgnoreCase("2")||!getF_IN_PartyAddress3().isEmpty()&&getF_IN_PartyAddress3().substring(0, 1).equalsIgnoreCase("3"))
								||!getF_IN_PartyAddress4().isEmpty()&&(getF_IN_PartyAddress4().substring(0, 1).equalsIgnoreCase("2")||!getF_IN_PartyAddress4().isEmpty()&&getF_IN_PartyAddress4().substring(0, 1).equalsIgnoreCase("3")))

						checkForPairNonViceVersa(2, 3);
						else
						checkForPair(4, 5);
						boolean valid = validateNumberSlases(
								getF_IN_PartyAddress1(), env);
						if (!valid) {
							String[] err = { "1" };
							/*
							 * throw new BankFusionException(9449, err, logger,
							 * env);
							 */
							EventsHelper
									.handleEvent(
											ChannelsEventCodes.E_FORMAT_OF_PARTY_ADDRESS_LINE_IS_INVALID,
											err, new HashMap(), env);
						}
						valid = validateNumberSlases(getF_IN_PartyAddress2(),
								env);
						if (!valid) {
							String[] err = { "2" };
							/*
							 * throw new BankFusionException(9449, err, logger,
							 * env);
							 */
							EventsHelper
									.handleEvent(
											ChannelsEventCodes.E_FORMAT_OF_PARTY_ADDRESS_LINE_IS_INVALID,
											err, new HashMap(), env);
						}
						valid = validateNumberSlases(getF_IN_PartyAddress3(),
								env);
						if (!valid) {
							String[] err = { "3" };
							/*
							 * throw new BankFusionException(9449, err, logger,
							 * env);
							 */
							EventsHelper
									.handleEvent(
											ChannelsEventCodes.E_FORMAT_OF_PARTY_ADDRESS_LINE_IS_INVALID,
											err, new HashMap(), env);
						}
						valid = validateNumberSlases(getF_IN_PartyAddress4(),
								env);
						if (!valid) {
							String[] err = { "4" };
							/*
							 * throw new BankFusionException(9449, err, logger,
							 * env);
							 */
							EventsHelper
									.handleEvent(
											ChannelsEventCodes.E_FORMAT_OF_PARTY_ADDRESS_LINE_IS_INVALID,
											err, new HashMap(), env);
						}
					} else if (!((getF_IN_PartyIdentifier().trim().length() == 0)
							&& slases[0] == -1
							&& slases[1] == -1
							&& slases[2] == -1 && slases[3] == -1)) {

						// throw new BankFusionException(9452, err, logger,
						// env);
						EventsHelper
								.handleEvent(
										ChannelsEventCodes.E_FORMAT_OF_PARTY_ADDR_LINE_IS_INVALID,
										new Object[] {}, new HashMap(), env);
					}
				} catch (NumberFormatException nfe) {
					// throw new BankFusionException(9435, null, null, env);
					EventsHelper
							.handleEvent(
									ChannelsEventCodes.E_FIRST_CHARACTER_NOT_INTEGER_IN_ADDRESS,
									new Object[] {}, new HashMap(), env);
				}
				// MT103+ addition starts
			}
			/*
			 * Issue Fix #artf763067 Condition commented since validation is
			 * done at client side. else if (getF_IN_PartyIdentifier().length()
			 * == 0) { if (getF_IN_PartyAddress1().length() >= 35 &&
			 * getF_IN_PartyAddress2().length() >= 35 &&
			 * getF_IN_PartyAddress3().length() >= 35 &&
			 * getF_IN_PartyAddress4().length() >= 35) {
			 * displayMessifError(ChannelsEventCodes
			 * .E_ADDRESS_FIELD_LENGTH_IS_MORE_THEN_EXPECTED, null, logger,
			 * env); } }
			 */
		}

		// MT103+ addition ends

		if (getF_IN_PartyIdentifierCombo().trim().length() != 0) {
			if (getF_IN_PartyAddress1().length() == 0) {
				String[] err = { "Party address line 1" };
				displayMessifError(ChannelsEventCodes.E_CANNOT_BE_BLANK, err,
						logger, env);
			}

		} else if (isAddressOfFormat1N33X(getF_IN_PartyAddress1())) {
			if (getF_IN_PartyIdentifierCombo().trim().length() == 0
					&& getF_IN_PartyIdentifierCountryCode().length() == 0&&getF_IN_orderingCustomerAccountNumber().isEmpty()) {
				String Params[] = { "Party Identifier Code" };
				EventsHelper.handleEvent(ChannelsEventCodes.E_CANNOT_BE_BLANK,
						Params, new HashMap(), env);
			}
		}

		if (getF_IN_PartyIdentifierCombo().trim().length() == 0
				&& (getF_IN_orderingCustomerAccountNumber().length() != 0)) {
			if (getF_IN_PartyAddress1().length() == 0
					&& getF_IN_orderingCustomerIdentifierCode().length() == 0) {
				String[] err = { "Oredering Customer Name and Address or Identifier Code" };
				displayMessifError(ChannelsEventCodes.E_MUSTBEENTERED, err,
						logger, env);
			}
		}

	}


	public void FirstValidation()
	{

		if(getF_IN_Generate103Plus().equalsIgnoreCase(Default103Plus))
		{
			PartyIdentifier_Isempty();
			result = false;
		}
		else{
			if(!getF_IN_BankInstructionCode().isEmpty())
			{
			String INPUT_String_0 = getF_IN_BankInstructionCode();
			String INPUT_String_1 = getF_IN_MessageType();
			result = (INPUT_String_1 == "103") ? (INPUT_String_0  == "PHON" || INPUT_String_0 == "PHOB" || INPUT_String_0  == "PHOI" ||INPUT_String_0 == "TELE" || INPUT_String_0 == "TELB" || INPUT_String_0 == "TELI" || INPUT_String_0 == "HOLD" || INPUT_String_0 == "REPA") : (INPUT_String_0  == "PHON" || INPUT_String_0 == "PHOB" || INPUT_String_0  == "PHOI" ||INPUT_String_0 == "TELE" || INPUT_String_0 == "TELB" || INPUT_String_0 == "TELI" || INPUT_String_0 == "HOLD");
			}
			else { result = false; }
		    }

		if(!result.booleanValue())
			{
			if (!getF_IN_PartyIdentifierCountryCode().isEmpty()&&getF_IN_BankAdditionalInstructionCode().isEmpty())
			{
				validationStatus="true";

			}
			else if(getF_IN_BankAdditionalInstructionCode().isEmpty())
				{
					PartyIdentifier_Isempty();
				}
				else{
					Errormsg="40401034";
					validationStatus = "false";

				}
			}
			else
			{
				PartyIdentifier_Isempty();
			}

	}
	public void FirstValidationFor103Plus()
	{

	if(getF_IN_Generate103Plus().equalsIgnoreCase(Default103Plus))
	{
		if(!getF_IN_BankOperationCode().isEmpty())
		{
			if(getF_IN_BankInstructionCode().isEmpty())
			{
				BankInstructionCode_IsEmpty();
			}
			else{
				HashMap expParam = new HashMap();
				String INPUT_String_0 = getF_IN_BankInstructionCode();
				Boolean result = INPUT_String_0  == "SDVA" ||INPUT_String_0 == "INTC" ||INPUT_String_0 == "REPA" || INPUT_String_0 == "CORT";
			if(result.booleanValue())
			{
				if(!getF_IN_BankInstructionCode().equalsIgnoreCase("REPA"))
				{
					if(!getF_IN_BankAdditionalInstructionCode().isEmpty())
					{
						Errormsg = "40401038";
					}
					else{
						BankInstructionCode_IsEmpty();
					}
				}
				else{
					BankInstructionCode_IsEmpty();
				}
			}
			else
			{
				Errormsg = "40401037";
			}

	}
		}
		else{
			Errormsg = "40401036";
		}

	}
	else{
		validationStatus="true";
	    }
	}

	public void SetOutputParams()
	{
		setF_OUT_ValidationStatus(validationStatus);
		setF_OUT_ErrorMessage(Errormsg);
	}

	/**
	 * method is for checking messege type (2,3) and (4,5)
	 *
	 * @param j
	 * @param k
	 *
	 */


	private void checkForPair(int j, int k) {
		if (!((check(j) && check(k)) || (!check(j) && !check(k)))) {
			String[] err = { String.valueOf(j), String.valueOf(k) };
			displayMessifError(ChannelsEventCodes.E_ADDRESS_IS_NOT_ALLOWED,
					err, logger, env);
		}

	}
	private void checkForPairNonViceVersa(int j, int k) {
		if (((!check(k) && check(j)))) {
			String[] err = { String.valueOf(j), String.valueOf(k) };
			displayMessifError(ChannelsEventCodes.E_ADDRESS_IS_NOT_ALLOWED,
					err, logger, env);
		}

	}

	public void PartyIdentifier_Isempty()
	{
		if(!getF_IN_PartyIdentifier().isEmpty())
		{
	      if(!getF_IN_PartyIdentifierCombo().isEmpty())
		   {
	        if(!getF_IN_PartyIdentifierCountryCode().isEmpty())
				{ Errormsg = "40401035"; }
		    else { validationStatus = "true"; }
		   }
	      else { validationStatus = "true"; }
		} else { validationStatus = "true"; }
	}



	public void BankInstructionCode_IsEmpty()
	{
		if(!getF_IN_orderingInstituteAccountNumber().isEmpty())
		{
			if(!getF_IN_orderingInstituteIdentifierCode().isEmpty())
			{
				validationStatus = "true";
			}
			else{
				Errormsg = "40401039";
			}
		}
		else
		{
			validationStatus = "true";
		}
	}

	/**
	 * @return true or false based on whether the address is there or not.
	 */
	private boolean check(int val) {
		for (int i = 0; i < 4; i++) {
			if (slases[i] == val)
				return true;
		}
		return false;
	}

	/**
	 * Method for checking order of slases
	 */

	private void isSlasesOrder() {
		int prev = -1;
		for (int i = 0; i < 4; i++) {
			if (slases[i] != -1 && prev >= slases[i]) {
				if(slases[i]!=1&&slases[i]!=2&&slases[i]!=3||slases[i]<prev)
				{
				displayMessifError(
						ChannelsEventCodes.E_ADDRESS_SHOULD_BE_IN_INCREASING_ORDER,
						null, logger, env);
			} }else {
				prev = slases[i];
			}
		}
	}

	/**
	 * Checking the value between 1 and 8 and also it will check the order of
	 * the address
	 */

	private void ValidSlases() {
		int field2 = 0;
		int field3 = 0;
		int field4 = 0;

		if (!getF_IN_PartyAddress2().isEmpty())
		 field2=Integer.parseInt(getF_IN_PartyAddress2().substring(0, 1));
		if (!getF_IN_PartyAddress3().isEmpty())
		 field3=Integer.parseInt(getF_IN_PartyAddress3().substring(0, 1));
		if (!getF_IN_PartyAddress4().isEmpty())
		 field4=Integer.parseInt(getF_IN_PartyAddress4().substring(0,1));


		boolean flag = false;
		for (int k = 0; k < 4; k++) {
			if (slases[k] == 1)
				continue;
			if ((slases[k] < 1 && slases[k] > 8)||field2>8||field3>8||field4>8) {
				displayMessifError(
						ChannelsEventCodes.E_NUMERIC_VALUE_SHOULD_BE_BETWEEN_1_TO_8,
						null, logger, env);
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
				String[] err = { String.valueOf(s), String.valueOf(r) };
				displayMessifError(
						ChannelsEventCodes.E_PARTY_ADDRESS_LINE_NOT_ALLOWED,
						err, logger, env);
			}
		}

	}

	/**
	 * Method to check is all are empty when party identifier is empty
	 */
	private void checkAllEmpty() {
		for (int i = 0; i < 4; i++) {
			int k = i + 1;
			if (slases[i] != -1) {
				String[] err = { String.valueOf(k) };
				displayMessifError(
						ChannelsEventCodes.E_PARTY_ADDR_LINE_NOT_ALLOWED_IF_PARTY_ID_IS_BLANK,
						err, logger, env);
			}
		}
	}

	/**
	 * Method for assigning all the number in an array if one address is not
	 * there -1 will be stored
	 */
	private void getSlases() {
		slases[0] = getF_IN_PartyAddress1().trim().length() <= 0 ? -1 : Integer
				.parseInt(getF_IN_PartyAddress1().trim().substring(0, 1));
		slases[1] = getF_IN_PartyAddress2().trim().length() <= 0 ? -1 : Integer
				.parseInt(getF_IN_PartyAddress2().trim().substring(0, 1));
		slases[2] = getF_IN_PartyAddress3().trim().length() <= 0 ? -1 : Integer
				.parseInt(getF_IN_PartyAddress3().trim().substring(0, 1));
		slases[3] = getF_IN_PartyAddress4().trim().length() <= 0 ? -1 : Integer
				.parseInt(getF_IN_PartyAddress4().trim().substring(0, 1));
	}

	/**
	 * Method to check for the number of slases
	 *
	 * @param Address
	 * @return boolean
	 */

	private boolean validateNumberSlases(String Address,
			BankFusionEnvironment env) {

		Address = Address.trim();
		if (Address.length() == 0)
			return true;
		StringTokenizer tokenAddress = new StringTokenizer(Address, "/");
		int count = tokenAddress.countTokens() - 1;
		if (Integer.parseInt(Address.substring(0, 1)) == 1
				|| Integer.parseInt(Address.substring(0, 1)) == 2
				|| Integer.parseInt(Address.substring(0, 1)) == 8) {
			if (count == 1 && Address.indexOf('/') == 1)
				return true;
			else
				return false;
		}
	
		if (Integer.parseInt(Address.substring(0, 1)) == 5
				|| Integer.parseInt(Address.substring(0, 1)) == 7) {
			if (count == 2 && Address.indexOf('/') == 1
					&& Address.lastIndexOf('/') != 2) {
				Map hmpParams = new HashMap();
				hmpParams.put("CountryCode2Char", Address.substring(Address
						.indexOf('/') + 1, Address.lastIndexOf('/')));
				Map result = MFExecuter.executeMF(
						"UB_SWT_Validate2CharCountryCode_SRV", env, hmpParams);
				Boolean isCountryCodeInvalid = (Boolean) result
						.get("IsCountryCodeValid");
				if (!isCountryCodeInvalid.booleanValue()) {
					EventsHelper.handleEvent(
							ChannelsEventCodes.E_COUNTRYCODEINVALID,
							new Object[] {}, new HashMap(), env);
				}
				return true;
			} else
				return false;
		}
		if ( Integer.parseInt(Address.substring(0, 1)) == 6) {
			if (count == 3 && Address.indexOf('/') == 1
					&& Address.lastIndexOf('/') != 2) {
				Map hmpParams = new HashMap();
				hmpParams.put("CountryCode2Char", Address.substring(Address
						.indexOf('/') + 1, Address.indexOf("/", Address.indexOf("/") + 1)));
				
				Map result = MFExecuter.executeMF(
						"UB_SWT_Validate2CharCountryCode_SRV", env, hmpParams);
				Boolean isCountryCodeInvalid = (Boolean) result
						.get("IsCountryCodeValid");
				if (!isCountryCodeInvalid.booleanValue()) {
					EventsHelper.handleEvent(
							ChannelsEventCodes.E_COUNTRYCODEINVALID,
							new Object[] {}, new HashMap(), env);
				}
				return true;
			} else
				return false;
		}
		
		//change for 2015 support
	
		
		if ( Integer.parseInt(Address.substring(0, 1)) == 3) {
		    
			if ((count == 1 && Address.indexOf('/') == 1
					&& Address.lastIndexOf('/') != 2)||(count == 2 && Address.indexOf('/') == 1
							&& Address.lastIndexOf('/') != 2)) {
			if(flagcount!=-1)
			{
				
				Map hmpParams = new HashMap();
				if(count==2)
				{
				hmpParams.put("CountryCode2Char", Address.substring(Address
						.indexOf('/') + 1, Address.lastIndexOf('/')));
				}
				else
				{
					hmpParams.put("CountryCode2Char", Address.substring(Address
							.indexOf('/') + 1));
				}
				Map result = MFExecuter.executeMF(
						"UB_SWT_Validate2CharCountryCode_SRV", env, hmpParams);
				Boolean isCountryCodeInvalid = (Boolean) result
						.get("IsCountryCodeValid");
				if (!isCountryCodeInvalid.booleanValue()) {
					EventsHelper.handleEvent(
							ChannelsEventCodes.E_COUNTRYCODEINVALID,
							new Object[] {}, new HashMap(), env);
				}
			}
			flagcount=-1;
				return true;
			} else
				return false;
		}
		

		if (Integer.parseInt(Address.substring(0, 1)) == 4) {
			if (count == 1 && Address.indexOf('/') == 1
					&& Address.length() == 10) {
                Date currentDate = SystemInformationManager.getInstance().getBFSystemDate();
				SimpleDateFormat dateformatyyyyMMdd = new SimpleDateFormat("yyyyMMdd");
				String date_Sent = dateformatyyyyMMdd.format(currentDate);
				String input=Address.substring(2, 10);
				
				
				if (input.matches("([0-9]{4})([0-9]{2})([0-9]{2})")&&input.compareTo(date_Sent)<=0)
				    return true;
				else
				   return false;
			} else
				return false;
		}
		return true;
	}

	/**
	 * Method for displaying the error.
	 *
	 * @param val
	 *            - error message number
	 * @param obj
	 * @param logger
	 * @param env
	 */
	private void displayMessifError(int val, String[] obj, Log logger,
			BankFusionEnvironment env) {
		// try {
		// throw new BankFusionException(val, obj, logger, env);
		EventsHelper.handleEvent(val, obj, new HashMap(), env);

		// }
		// catch (Exception e) {
		// throw new BankFusionException(val, obj, logger, env);
		// EventsHelper.handleEvent(val, obj, new HashMap(), env);
		//
		// }
	}

	/**
	 * The method determines wether the passed String Argument is of the format
	 * 1!n/33x
	 *
	 * @param Address
	 * @return
	 */

	private boolean isAddressOfFormat1N33X(String Address) {
		try {
			if (Address.length() == 0) {
				return false;
			}
			StringTokenizer stkAddressToken = new StringTokenizer(Address, "/");
			if (stkAddressToken.countTokens() == 2 && Address.indexOf('/') == 1) {
				Integer.parseInt(stkAddressToken.nextToken());
				return true;
			} else
				return false;
		} catch (NumberFormatException ne) {
			return false;
		}
	}
	/**
	 * The method determines whether the passed String Argument is of the format
	 * 1!n/33x
	 *
	 * @param Address
	 * @return
	 */

}
