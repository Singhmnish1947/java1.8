/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.common.events.CommonsEventCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_CorresACValidate;

/**
 * @author prasanthj
 * 
 */
public class SWT_CorresAccountValidate extends AbstractSWT_CorresACValidate {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	private static final String FALSE = "N";

	private static final String TRUE = "Y";

	private static final String EMPTY_STRING = CommonConstants.EMPTY_STRING;

	private transient final static Log logger = LogFactory.getLog(SWT_CorresAccountValidate.class.getName());

	private static final String whereClause = "where " + IBOAccount.ACCOUNTID + " = ? ";
	/**
	 * @param env
	 */
	public SWT_CorresAccountValidate(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	public void process(BankFusionEnvironment env) {

		super.process(env);

		final String default_nostro_string = getF_IN_default_nostro_string();
		final Boolean default_nostro_boolean = isF_IN_default_nostro_boolean();

		// When mapped to SWTCorresAccount_Find BO, this Fatom is used just to
		// convert the default nostro string value from database to boolean on
		// screen.
		// Don't do any user validation in that case.
		if (default_nostro_string != null && !default_nostro_string.trim().equals(EMPTY_STRING)) {

			setF_IN_default_nostro_boolean(default_nostro_string.equals(TRUE) ? new Boolean(true) : new Boolean(false));

		}
		else {

			setF_IN_default_nostro_string(default_nostro_boolean.booleanValue() == true ? TRUE : FALSE);

			validateUserInputs(env);
		}

	}

	private void validateUserInputs(BankFusionEnvironment env) {
		final String internal_ac_id = getF_IN_internal_account_number();

		if (internal_ac_id == null || internal_ac_id.trim().equals(EMPTY_STRING)) {
			return;
		}

		ArrayList params = new ArrayList();
		params.add(internal_ac_id);

		List accounts = env.getFactory().findByQuery(IBOAccount.BONAME, whereClause, params, null);

		if (accounts == null || accounts.size() == 0) {
			//throw new BankFusionException(442, new String[] { internal_ac_id }, logger, env);
			EventsHelper.handleEvent(CommonsEventCodes.E_RESOURCE_ALREADY_EXISTS_OVERWRITE, new String[] { internal_ac_id }, new HashMap(), env);
		}

	}

}
