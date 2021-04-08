/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.security.runtime.impl.CurrencyUtil;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.events.CommonsEventCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOCurrency;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_ValidateSettlementInstructionkeys;

public class SWT_ValidateSettlementInstructionkeysFatom extends AbstractSWT_ValidateSettlementInstructionkeys {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	private transient final static Log logger = LogFactory.getLog(SWT_ValidateSettlementInstructionkeysFatom.class
			.getName());

	public SWT_ValidateSettlementInstructionkeysFatom(BankFusionEnvironment env) {
		super(env);

	}

	public void process(BankFusionEnvironment env) {
		ArrayList messageTypeList = new ArrayList();
		messageTypeList.add("320");
		messageTypeList.add("330");

		validateCustomerNumber(env);
		validateCurrencyCode(env);

		if ((getF_IN_MessageNumber().intValue() < 1) || (getF_IN_MessageNumber().intValue() > 999)) {
			//displayMessifError(9405, null, logger, env);
			displayMessifError(ChannelsEventCodes.E_MESSAGE_NUMBER_SHOULD_BE_BETWEEN_1_TO_999, null, logger, env);
			
		}

		if (messageTypeList.contains(getF_IN_MessageType())) {

			if ((getF_IN_PayOrReceive().trim().length() <= 0)) {
				String[] err = { getF_IN_MessageType() };
				//displayMessifError(9406, err, logger, env);
				displayMessifError(ChannelsEventCodes.E_PAY_OR_RECEIVE_NOT_SELECED_FOR_THE_MESSAGE_TYPE, null, logger, env);
				
			}
		}
		else {
			if (!(getF_IN_PayOrReceive().trim().length() <= 0)) {
				String[] err = { getF_IN_MessageType() };
//				displayMessifError(9440, err, logger, env);
				displayMessifError(ChannelsEventCodes.E_PAY_OR_RECEIVE_NOT_BLANK_FOR_THE_MESSAGE_TYPE, null, logger, env);

				
			}
		}

	}

	private void validateCurrencyCode(BankFusionEnvironment env) {
		String currencyCode = getF_IN_CurrencyCode();
		String MessageType = getF_IN_MessageType();
        IBOCurrency currency = CurrencyUtil.getCurrencyDetailsOfCurrentZone(currencyCode);
        if (null != currency) {
            boolean Active = currency.isF_ISACTIVE();
            String SwiftActive = currency.getF_SWTCURRENCYINDICATOR();
			if ((!Active) && (MessageType.equals("300"))) {
				//displayMessifError(9437, new String[] { currencyCode }, logger, env);
				
				displayMessifError(ChannelsEventCodes.E_INACTIVE_CURRENCY, null, logger, env);

			}
			else if (!(SwiftActive.equalsIgnoreCase("Y"))) {
				//displayMessifError(9438, new String[] { currencyCode }, logger, env);
				displayMessifError(ChannelsEventCodes.E_IS_NOT_A_SWIFT_CURRENCY, null, logger, env);

				
			}
		}
		else {
			//displayMessifError(7512, new String[] { " :" + currencyCode }, logger, env);
			displayMessifError(ChannelsEventCodes.E_INVALID_CURRENCY_CODE, null, logger, env);

			
		}
	}

	private void validateCustomerNumber(BankFusionEnvironment env) {
		String customerNo = getF_IN_CustomerNumber();
        IBOSwtCustomerDetail swtCustDtls = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(IBOSwtCustomerDetail.BONAME,
                customerNo, true);
        if (null != swtCustDtls) {
            String Active = swtCustDtls.getF_SWTACTIVE();
			//String SwiftActive=(String)CurrencyDetails.getDataMap().get(IBOCurrency.SWTCURRENCYINDICATOR);
			if (Active.equalsIgnoreCase("N")) {
				//displayMessifError(9439, new String[] { customerNo }, logger, env);
				displayMessifError(ChannelsEventCodes.E_INACTIVE_SWIFT_CUSTOMER, null, logger, env);

			}
		}
		else {
			//displayMessifError(7049, new String[] { customerNo }, logger, env);
			displayMessifError(CommonsEventCodes.E_CUSTOMER_NUMBER_DOES_NOT_EXIST, null, logger, env);

			
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
