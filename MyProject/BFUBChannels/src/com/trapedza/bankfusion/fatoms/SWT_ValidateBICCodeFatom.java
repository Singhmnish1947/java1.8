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

import bf.com.misys.cbs.types.events.Event;

import com.misys.bankfusion.common.runtime.service.ServiceManagerFactory;
import com.misys.bankfusion.events.IBusinessEventsService;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOBicCodes;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_ValidateBICCode;
import com.trapedza.bankfusion.utils.BankFusionMessages;

public class SWT_ValidateBICCodeFatom extends AbstractSWT_ValidateBICCode {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private final String query = "SELECT "+IBOBicCodes.BICCODE+" FROM " + IBOBicCodes.BONAME + " WHERE "+IBOBicCodes.BICCODE+"=?";
	private final String query2 = "SELECT "+IBOBicCodes.BICCODE+" FROM " + IBOBicCodes.BONAME + " WHERE "+IBOBicCodes.BICCODE+" like ?";
	private final static String XXX="XXX";
	private final static String BIC_CODE="BicCode";
	private final static String PERCENTAGE="%";
	private final static String PATTERN1="[a-zA-Z]{6}+\\w{2}+";
	private final static String PATTERN2="[a-zA-Z]{6}+\\w{5}+";
	private static IBusinessEventsService businessEventsService = (IBusinessEventsService) ServiceManagerFactory
            .getInstance().getServiceManager().getServiceForName(
                         IBusinessEventsService.SERVICE_NAME);



	/**
	 * @param env
	 */
	public SWT_ValidateBICCodeFatom(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractSWT_ValidateBICCode#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 */
	public void process(BankFusionEnvironment env) {
		String BicCode = getF_IN_BICCode();
		if (BicCode != null) {
			int length = BicCode.length();
			if (!(length == 11 || length == 8)) {
				//String eMessage = BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 9430, env,
				//		null);
				//throw new BankFusionException(9430, eMessage);
//				EventsHelper.handleEvent(ChannelsEventCodes.E_CHARACTERS_IN_IDENTIFIER_CODE_IS_NOT_8_OR_11, new Object[]{}, new HashMap(), env);
				Event validateEvent = new Event();
                validateEvent.setEventNumber(ChannelsEventCodes.E_CHARACTERS_IN_IDENTIFIER_CODE_IS_NOT_8_OR_11);
                businessEventsService.handleEvent(validateEvent);
			}
			if (length == 8) {

				BicCode = BicCode + XXX;
				length = BicCode.length();
			}
			if (!IsValideBIC(BicCode)) {
				//String eMessage = BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 9447, env,
				//		new Object[] { BIC_CODE });
				//throw new BankFusionException(9447, eMessage);
//				EventsHelper.handleEvent(ChannelsEventCodes.E_DOES_NOT_SATISFY_ISO_STANDARDS, new Object[] { BIC_CODE }, new HashMap(), env);
				Event validateEvent = new Event();
                validateEvent.setEventNumber(ChannelsEventCodes.E_DOES_NOT_SATISFY_ISO_STANDARDS);
                validateEvent.addMessageArguments(0, BIC_CODE);
                businessEventsService.handleEvent(validateEvent);
			}

			if (length == 11) {

				ArrayList BicCodeParam = new ArrayList();
				String SubStr = BicCode.substring(8, length);
				if (SubStr.equalsIgnoreCase(XXX)) {
					SubStr = BicCode.substring(0, 8);
					BicCodeParam.add(SubStr);
					Iterator BicCodeIterator = env.getFactory().executeGenericQuery(query, BicCodeParam, null)
							.iterator();
					if (BicCodeIterator.hasNext()) {
//						String eMessage = BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 9431,
//								env, new Object[] { SubStr });
						//throw new BankFusionException(9431, eMessage);
//						EventsHelper.handleEvent(ChannelsEventCodes.E_IDENTIFIER_CODE_WITH_ALREADY_EXIST, new Object[] { SubStr }, new HashMap(), env);
						Event validateEvent = new Event();
		                validateEvent.setEventNumber(ChannelsEventCodes.E_IDENTIFIER_CODE_WITH_ALREADY_EXIST);
		                validateEvent.addMessageArguments(0, SubStr);
		                businessEventsService.handleEvent(validateEvent);
					}
				}
				else {
					BicCodeParam.add(BicCode);
					Iterator BicCodeIterator = env.getFactory().executeGenericQuery(query, BicCodeParam, null)
							.iterator();
					if (BicCodeIterator.hasNext()) {
//						String eMessage = BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 9431,
//								env, new Object[] { BicCode });
						//throw new BankFusionException(9431, eMessage);
//						EventsHelper.handleEvent(ChannelsEventCodes.E_IDENTIFIER_CODE_WITH_ALREADY_EXIST, new Object[] { BicCode }, new HashMap(), env);
						Event validateEvent = new Event();
		                validateEvent.setEventNumber(ChannelsEventCodes.E_IDENTIFIER_CODE_WITH_ALREADY_EXIST);
		                validateEvent.addMessageArguments(0, BicCode);
		                businessEventsService.handleEvent(validateEvent);
					}
				}
				setF_IN_BICCode(BicCode);
			}
			else if (length == 8) {
				ArrayList BicCodeParam = new ArrayList();

				BicCodeParam.add(BicCode + PERCENTAGE);
				Iterator BicCodeIterator = env.getFactory().executeGenericQuery(query, BicCodeParam, null).iterator();
				if (BicCodeIterator.hasNext()) {
//					String eMessage = BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 9431, env,
//							new Object[] { BicCode });
//					throw new BankFusionException(9431, eMessage);
//					EventsHelper.handleEvent(ChannelsEventCodes.E_IDENTIFIER_CODE_WITH_ALREADY_EXIST, new Object[] { BicCode }, new HashMap(), env);
					Event validateEvent = new Event();
	                validateEvent.setEventNumber(ChannelsEventCodes.E_IDENTIFIER_CODE_WITH_ALREADY_EXIST);
	                validateEvent.addMessageArguments(0, BicCode);
	                businessEventsService.handleEvent(validateEvent);

				}
				setF_IN_BICCode(BicCode + XXX);
			}

		}
	}

	/**
	 * This method will check validate BIC or not
	 * @param input
	 * @return
	 */
	private boolean IsValideBIC(String input) {
		String pattern = null;
		if (input.length() == 8)
			pattern = PATTERN1;
		else
			pattern = PATTERN2;

		Pattern patternMatch = Pattern.compile(pattern);
		Matcher matcher = patternMatch.matcher(input);
		if (matcher.matches()) {

			return true;
		}
		else {

			return false;
		}

	}

}
