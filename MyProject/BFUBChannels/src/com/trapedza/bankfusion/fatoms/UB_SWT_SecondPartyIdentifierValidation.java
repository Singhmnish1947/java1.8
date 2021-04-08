/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 * 
 * **********************************************************************************
 * 
 * $Id: UB_SWT_SecondPartyIdentifierValidation.java,v 1.0 2013/06/25 12:20:22 Paramveer Exp $
 **/
package com.trapedza.bankfusion.fatoms;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_SecondPartyIdentifierValidation;



public class UB_SWT_SecondPartyIdentifierValidation extends AbstractUB_SWT_SecondPartyIdentifierValidation {
	
	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}
	
	private transient final static Log logger = LogFactory
	.getLog(UB_SWT_SecondPartyIdentifierValidation.class.getName());
	

	/**
	 * @param env
	 * constructor for bankfusion environment.
	 */
	public UB_SWT_SecondPartyIdentifierValidation(BankFusionEnvironment env) {
		super(env);
	}
	
	BankFusionEnvironment env;
	public void process(BankFusionEnvironment env) {
		this.env = env;
		this.BeneficiaryCustomerValidation();
	}
	
	public void BeneficiaryCustomerValidation() {

		if (getF_IN_BeneficiaryCustomerPartyIdentifier().contains("/")) {
			String[] Params = { "Beneficiary Customer" };
			EventsHelper.handleEvent(ChannelsEventCodes.E_SI_SLASHES_NOT_ALLOWED,
					Params, new HashMap(), env);
		}
		if (!((!getF_IN_BeneficiaryCustomerIdentifierCombo().isEmpty() && !getF_IN_BeneficiaryCustomerPartyIdentifier()
				.isEmpty()) || (getF_IN_BeneficiaryCustomerIdentifierCombo()
				.isEmpty() && getF_IN_BeneficiaryCustomerPartyIdentifier().isEmpty()))) {
			String[] Params = { "Beneficiary Customer" };
			EventsHelper.handleEvent(ChannelsEventCodes.E_SI_PARTY_IDENTIFIER_CANNOT_BE_BLANK,
					Params, new HashMap(), env);
		}

		if (getF_IN_BeneficiaryCustomerIdentifierCombo().equalsIgnoreCase("I")) {
			String iban = getF_IN_BeneficiaryCustomerPartyIdentifier();
			int ibanLength = getF_IN_BeneficiaryCustomerPartyIdentifier().length();
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
	
	
}
