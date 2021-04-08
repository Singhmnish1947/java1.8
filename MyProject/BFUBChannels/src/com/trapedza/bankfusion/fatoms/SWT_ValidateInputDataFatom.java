
/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.util.HashMap;

import bf.com.misys.cbs.types.events.Event;

import com.misys.bankfusion.common.runtime.service.ServiceManagerFactory;
import com.misys.bankfusion.events.IBusinessEventsService;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_ValidateInputData;
import com.trapedza.bankfusion.utils.BankFusionMessages;

public class SWT_ValidateInputDataFatom extends AbstractSWT_ValidateInputData {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private static final String MESSAGE_TYPE = "Message Type";
	private static final String CHARGE_AMOUNT = "Charge Amount";
	private static final String DEAL_NUMBER = "Deal Number";
	private static final String INTEREST_AMOUNT = "Interest Amount";
	private static final String INTEREST_RATE = "Interest Rate";
	private static final String RELATED_DEAL_NUMBER = "Related Deal Number";
	private static final String DRAFT_NUMBER = "Draft Number";
	private static final String SETTLEMENT_INSTRUCTION_NUMBER = "Settlement Instruction Number";
	private static final String TRANSACTION_AMOUNT = "Transation Amount";
	private static final String FIFTEEN = "15";
	private static final String THREE = "3";
	private static final String TWELVE = "12";
	private static final String SIXTEEN = "16";
	private static final String TERM = "Term";
	private static final String NINE ="9";
	private static final int INT_FIFTEEN = 15;
	private static final int INT_THREE = 3;
	private static final int INT_TWELVE = 12;
	private static final int INT_SIXTEEN = 16;
	private static final int INT_NINE = 9;
	private static IBusinessEventsService businessEventsService = (IBusinessEventsService) ServiceManagerFactory
            .getInstance().getServiceManager().getServiceForName(
                         IBusinessEventsService.SERVICE_NAME);


	public SWT_ValidateInputDataFatom(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	public void process(BankFusionEnvironment env) {

		validateInputTagLength(env);

	}

	private void validateInputTagLength(BankFusionEnvironment env) {
		String eMessage = null;
		String dealNumber = getF_IN_DealNumber();
		if (getF_IN_MessageType() != null) {
			if (getF_IN_MessageType().toString().length() > INT_THREE) {
//				eMessage = BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 40409429, env,
//						new Object[] { MESSAGE_TYPE, THREE });
//				throw new BankFusionException(9429, eMessage);
//				EventsHelper.handleEvent(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS, new Object[] { MESSAGE_TYPE, THREE }, new HashMap(), env);
				Event validateEvent = new Event();
                validateEvent.setEventNumber(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS);
                validateEvent.addMessageArguments(0, MESSAGE_TYPE);
                validateEvent.addMessageArguments(1, THREE);
                businessEventsService.handleEvent(validateEvent);
			}
		}
		if (getF_IN_SettlementInstructionNumber() != null) {
			if (getF_IN_SettlementInstructionNumber().toString().length() > INT_NINE) {
//				eMessage = BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 40409429, env,
//						new Object[] { SETTLEMENT_INSTRUCTION_NUMBER, THREE });
//				throw new BankFusionException(9429, eMessage);
//				EventsHelper.handleEvent(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS, new Object[] { SETTLEMENT_INSTRUCTION_NUMBER, NINE }, new HashMap(), env);
				Event validateEvent = new Event();
				validateEvent.setEventNumber(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS);
	            validateEvent.addMessageArguments(0, SETTLEMENT_INSTRUCTION_NUMBER);
	            validateEvent.addMessageArguments(1, NINE);
	            businessEventsService.handleEvent(validateEvent);

			}
		}
		if (getF_IN_RelatedDealNumber() != null) {
			if (getF_IN_RelatedDealNumber().length() > INT_SIXTEEN) {
//				eMessage = BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 40409429, env,
//						new Object[] { RELATED_DEAL_NUMBER, SIXTEEN });
				//throw new BankFusionException(9429, eMessage);
//				EventsHelper.handleEvent(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS, new Object[] { RELATED_DEAL_NUMBER, SIXTEEN }, new HashMap(), env);
				Event validateEvent = new Event();
				validateEvent.setEventNumber(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS);
	            validateEvent.addMessageArguments(0, RELATED_DEAL_NUMBER);
	            validateEvent.addMessageArguments(1, SIXTEEN);
	            businessEventsService.handleEvent(validateEvent);
			}
		}

		if (getF_IN_Term() != null) {
			if (getF_IN_Term().toString().length() > INT_THREE) {
//				eMessage = BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 40409429, env,
//						new Object[] { TERM, THREE });
//				throw new BankFusionException(9429, eMessage);
//				EventsHelper.handleEvent(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS, new Object[] { TERM, THREE }, new HashMap(), env);
				Event validateEvent = new Event();
				validateEvent.setEventNumber(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS);
	            validateEvent.addMessageArguments(0, TERM);
	            validateEvent.addMessageArguments(1, THREE);
	            businessEventsService.handleEvent(validateEvent);
			}
		}
		if (getF_IN_InterestAmount() != null) {
			if (getF_IN_InterestAmount().toString().length() > INT_FIFTEEN) {
//				eMessage = BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 40409429, env,
//						new Object[] { INTEREST_AMOUNT, FIFTEEN });
//				throw new BankFusionException(9429, eMessage);
//				EventsHelper.handleEvent(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS, new Object[] { INTEREST_AMOUNT, FIFTEEN }, new HashMap(), env);
				Event validateEvent = new Event();
				validateEvent.setEventNumber(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS);
	            validateEvent.addMessageArguments(0, INTEREST_AMOUNT);
	            validateEvent.addMessageArguments(1, FIFTEEN);
	            businessEventsService.handleEvent(validateEvent);
			}
		}
        /* The validation for the exchangerate is commented as the validation
         * would be taken care of in MMM
         */
//		if (getF_IN_InterestRate() != null) {
//			if (getF_IN_InterestRate().toString().length() > INT_TWELVE) {
//				eMessage = BankFusionMessages.getFormattedMessage(BankFusionMessages.INFO_LEVEL, 40409429, env,
//						new Object[] { INTEREST_RATE, TWELVE });
//				 throw new BankFusionException(9429, eMessage);
//			}
//		}
		if (getF_IN_DealNumber() != null) {
			if (!(dealNumber.toUpperCase().startsWith("FX") || dealNumber.toUpperCase().startsWith("MM"))) {
				if (getF_IN_MessageType().equals("300") || getF_IN_MessageType().equals("900")
						|| getF_IN_MessageType().equals("910")) {
					setF_OUT_dealNumber("FX" + dealNumber);
				}
				else if (getF_IN_MessageType().equals("320") || getF_IN_MessageType().equals("330")
						|| getF_IN_MessageType().equals("350")) {
					setF_OUT_dealNumber("MM" + dealNumber);
				}
				else {
					setF_OUT_dealNumber(dealNumber);
				}
			}
			else {
				setF_OUT_dealNumber(dealNumber);
			}
			if (getF_OUT_dealNumber().length() > INT_SIXTEEN) {
//				eMessage = BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 40409429, env,
//						new Object[] { DEAL_NUMBER, SIXTEEN });
				//throw new BankFusionException(9429, eMessage);
//				EventsHelper.handleEvent(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS, new Object[] { DEAL_NUMBER, SIXTEEN }, new HashMap(), env);
				Event validateEvent = new Event();
				validateEvent.setEventNumber(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS);
	            validateEvent.addMessageArguments(0, DEAL_NUMBER);
	            validateEvent.addMessageArguments(1, SIXTEEN);
	            businessEventsService.handleEvent(validateEvent);
			}
		}
		if (getF_IN_TransactionAmount() != null) {
			if (getF_IN_TransactionAmount().toString().length() > INT_FIFTEEN) {
//				eMessage = BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 40409429, env,
//						new Object[] { TRANSACTION_AMOUNT, FIFTEEN });
				//throw new BankFusionException(9429, eMessage);
//				EventsHelper.handleEvent(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS, new Object[] { TRANSACTION_AMOUNT, FIFTEEN }, new HashMap(), env);
				Event validateEvent = new Event();
				validateEvent.setEventNumber(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS);
	            validateEvent.addMessageArguments(0, TRANSACTION_AMOUNT);
	            validateEvent.addMessageArguments(1, FIFTEEN);
	            businessEventsService.handleEvent(validateEvent);
			}
		}
		if (getF_IN_ChargeAmount() != null) {
			if (getF_IN_ChargeAmount().toString().length() > INT_FIFTEEN) {
//				eMessage = BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 40409429, env,
//						new Object[] { CHARGE_AMOUNT, FIFTEEN });
				//throw new BankFusionException(9429, eMessage);
//				EventsHelper.handleEvent(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS, new Object[] { CHARGE_AMOUNT, FIFTEEN }, new HashMap(), env);
				Event validateEvent = new Event();
				validateEvent.setEventNumber(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS);
	            validateEvent.addMessageArguments(0, CHARGE_AMOUNT);
	            validateEvent.addMessageArguments(1, FIFTEEN);
	            businessEventsService.handleEvent(validateEvent);
			}
		}
		if (getF_IN_DraftNumber() != null) {
			if (getF_IN_DraftNumber().toString().length() > INT_SIXTEEN) {
//				eMessage = BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 40409429, env,
//						new Object[] { DRAFT_NUMBER, SIXTEEN });
				//throw new BankFusionException(9429, eMessage);
//				EventsHelper.handleEvent(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS, new Object[] { DRAFT_NUMBER, SIXTEEN }, new HashMap(), env);
				Event validateEvent = new Event();
				validateEvent.setEventNumber(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS);
	            validateEvent.addMessageArguments(0, DRAFT_NUMBER);
	            validateEvent.addMessageArguments(1, SIXTEEN);
	            businessEventsService.handleEvent(validateEvent);
			}
		}
	}
}
