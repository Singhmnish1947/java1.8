/* ***********************************************************************************
 * Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * $Id: FON_ValidateAuthorizer.java,v 1.7 2008/08/12 20:13:57 vivekr Exp $
 * **********************************************************************************
 * 
 * Revision 1.14  2008/02/16 14:37:17  Vinayachandrakantha.B.K
 * JavaDoc Comments added : For all the attributes
 */

package com.trapedza.bankfusion.fatoms;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOFontisConfig;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractFON_ValidateAuthorizer;
import com.trapedza.bankfusion.core.EventsHelper;

public class FON_ValidateAuthorizer extends AbstractFON_ValidateAuthorizer {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	/**
	 * Logger instance
	 */
	private transient final static Log log = LogFactory.getLog(FON_ValidateAuthorizer.class.getName());

	/**
	 * Constructor
	 * @param env
	 */
	public FON_ValidateAuthorizer(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	/**
	 * implements process(...) method in AbstractFON_ValidateSelectedSupervisors
	 */
	public void process(BankFusionEnvironment env) {
		List configList = null;

		//		Loading fontis configuration. Status '0' record is being read since Notification & Authorization (N&A)
		//		is not yet being used in configuration. Once N&A is implemented Status '1' record should be read.
		String whereClause = "WHERE " + IBOFontisConfig.STATUS + " = 0";

		configList = env.getFactory().findByQuery(IBOFontisConfig.BONAME, whereClause, null, false);

		//		Check whether fontis configuration exists or not
		if (configList.size() <= 0) {
			/*throw new BankFusionException(9000, null, log, env);*/
			EventsHelper.handleEvent(ChannelsEventCodes.E_FONTIS_CONFIGURATION_RECORD_NOT_FOUND,new Object[] {} , new HashMap(), env);	
		}

		//		Get the FontisConfig object from the List
		IBOFontisConfig config = (IBOFontisConfig) configList.get(0);

		//		Check whether the current user name is configured as supervisor in the configuration or not.
		if (getF_IN_UserName().equals(config.getF_SUPERVISOR_1())) {
			setCredentials(config.getF_SUPERVISOR_1_LEVEL(), env);
		}
		else if (getF_IN_UserName().equals(config.getF_SUPERVISOR_2())) {
			setCredentials(config.getF_SUPERVISOR_2_LEVEL(), env);
		}
		else if (getF_IN_UserName().equals(config.getF_SUPERVISOR_3())) {
			setCredentials(config.getF_SUPERVISOR_3_LEVEL(), env);
		}
		else if (getF_IN_UserName().equals(config.getF_SUPERVISOR_4())) {
			setCredentials(config.getF_SUPERVISOR_4_LEVEL(), env);
		}
		else if (getF_IN_UserName().equals(config.getF_SUPERVISOR_5())) {
			setCredentials(config.getF_SUPERVISOR_5_LEVEL(), env);
		}
		else if (getF_IN_UserName().equals(config.getF_SUPERVISOR_6())) {
			setCredentials(config.getF_SUPERVISOR_6_LEVEL(), env);
		}
		else if (getF_IN_UserName().equals(config.getF_SUPERVISOR_7())) {
			setCredentials(config.getF_SUPERVISOR_7_LEVEL(), env);
		}
		else if (getF_IN_UserName().equals(config.getF_SUPERVISOR_8())) {
			setCredentials(config.getF_SUPERVISOR_8_LEVEL(), env);
		}
		else if (getF_IN_UserName().equals(config.getF_SUPERVISOR_9())) {
			setCredentials(config.getF_SUPERVISOR_9_LEVEL(), env);
		}
		else if (getF_IN_UserName().equals(config.getF_SUPERVISOR_10())) {
			setCredentials(config.getF_SUPERVISOR_10_LEVEL(), env);
		}
		//		throw error message, if the current user name is not configured as supervisor in the fontis configuration.
		else {
			/*throw new BankFusionException(9001, null, log, env);*/
			EventsHelper.handleEvent(ChannelsEventCodes.E_ACCESS_DENIED_FOR_THIS_PROGRAM,new Object[] {} , new HashMap(), env);
		}
	}

	/**
	 * Set the credential fields to be used as Criteria to view validation failed batches
	 * 0 - only Local currency transactions
	 * 1 - only Foreign currency transactions 
	 * @param authorizerLevel
	 * @param env
	 * @
	 */
	private void setCredentials(int authorizerLevel, BankFusionEnvironment env) {
		//		Authorization level is set to '1'(LCY - Local currency transctions), select the batches whose foreign currency
		//		flag is set to '0'.
		if (authorizerLevel == 1) {
			setF_OUT_Criteria_1("0");
			setF_OUT_Criteria_2("0");
		}
		//		Authorization level is set to '2'(FCY - Foreign currency transctions), select the batches whose foreign currency
		//		flag is set to '1'.
		else if (authorizerLevel == 2) {
			setF_OUT_Criteria_1("1");
			setF_OUT_Criteria_2("1");
		}
		//		Authorization level is set to '3'(BOTH - Both Foreign & Local currency transctions), select the batches whose foreign currency
		//		flag is set to '0' OR '1'.
		else if (authorizerLevel == 3) {
			setF_OUT_Criteria_1("0");
			setF_OUT_Criteria_2("1");
		}
		//		Authorization level is set to '0'(NONE - None of Foreign & Local currency transctions), User is having no authorization level.
		else if (authorizerLevel == 0) {
			/*throw new BankFusionException(9003, null, log, env);*/
			EventsHelper.handleEvent(ChannelsEventCodes.E_NOT_PERMITED_TO_APPROVE_OR_REJECT_FONTIS_TRANS,new Object[] {} , new HashMap(), env);
		}
		//		User is not configured for execute the authorization process.
		else {
			/*throw new BankFusionException(9004, null, log, env);*/
			EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_AUTHORIZATION_LEVEL_IN_FONTIS_CONFIG,new Object[] {} , new HashMap(), env);
		}
	}
}
