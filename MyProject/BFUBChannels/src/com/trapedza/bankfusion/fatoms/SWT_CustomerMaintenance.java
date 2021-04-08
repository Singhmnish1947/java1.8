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

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOBicCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_CustomerMaintenance;
import com.trapedza.bankfusion.steps.refimpl.ISWT_CustomerMaintenance;

public class SWT_CustomerMaintenance extends AbstractSWT_CustomerMaintenance implements ISWT_CustomerMaintenance {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	public BankFusionEnvironment env;

	private transient final static Log logger = LogFactory.getLog(SWT_CustomerMaintenance.class.getName());

	public SWT_CustomerMaintenance(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment environment) {
		validitingCusNumber(environment);
		validitingFldsInTable(environment);
		validateSwiftAccountNo(getF_IN_SWTACCNUMNEW());
		if (getF_IN_Financial_Institute().equalsIgnoreCase("Y") && getF_IN_BIC_Code().trim().length() <= 0) {
			/*displayMessifError(9436, new String[] { " BIC code is mandatory for a  Financial Institution Customer" },
					logger, env);*/
			displayMessifError(ChannelsEventCodes.E_ERROR, new String[] { " BIC code is mandatory for a  Financial Institution Customer" },
					logger, env);
		}
	}

	private void validateSwiftAccountNo(String swiftacc) {
		if (!(swiftacc.trim().equals(CommonConstants.EMPTY_STRING))) {
			if (swiftacc.trim().indexOf(" ") != -1) {
				/*displayMessifError(9436, new String[] { " Blank space is not allowed in Swift Account Number " },
						logger, env);*/
				displayMessifError(ChannelsEventCodes.E_ERROR, new String[] { " Blank space is not allowed in Swift Account Number " },
						logger, env);
			}
			if (!swiftacc.startsWith("/")) {
				//displayMessifError(9436, new String[] { "Swift Account Number Should Start with / " }, logger, env);
				displayMessifError(ChannelsEventCodes.E_ERROR, new String[] { "Swift Account Number Should Start with / " }, logger, env);
				
			}
		}
	}

	private void validitingCusNumber(BankFusionEnvironment environment) {
		// Map inputTags = getInDataMap();
		String customerNo = getF_IN_Customer_Number();
		String whereClause = null;
		whereClause = "where " + IBOCustomer.CUSTOMERCODE + " = ? ";
		ArrayList params = new ArrayList();
		ArrayList list = new ArrayList();
		params.add(customerNo);
		list = (ArrayList) environment.getFactory().findByQuery(IBOCustomer.BONAME, whereClause, params, null, false);
	}

	private void validitingFldsInTable(BankFusionEnvironment environment) {
		String bicCode = getF_IN_BIC_Code();
		String finInst = getF_IN_Financial_Institute();
		if ((finInst != null) && (finInst.equalsIgnoreCase("Y"))) {
			String whereClause = null;
			whereClause = "where " + IBOBicCodes.BICCODE + " = ? ";
			ArrayList params = new ArrayList();
			ArrayList list = new ArrayList();
			params.add(bicCode);
			list = (ArrayList) environment.getFactory().findByQuery(IBOBicCodes.BONAME, whereClause, params, null,
					false);
		}
		if (bicCode != null && bicCode.length() > 0) {
			String whereClause = null;
			whereClause = "where " + IBOBicCodes.BICCODE + " = ? ";
			ArrayList params = new ArrayList();
			ArrayList list = new ArrayList();
			params.add(bicCode);
			list = (ArrayList) environment.getFactory().findByQuery(IBOBicCodes.BONAME, whereClause, params, null,
					false);
		}

		String altBICCode = getF_IN_Alternate_BIC_Code();
		if (altBICCode != null && altBICCode.length() > 0) {
			String whereClause = null;
			whereClause = "where " + IBOBicCodes.BICCODE + " = ? ";
			ArrayList params = new ArrayList();
			ArrayList list = new ArrayList();
			params.add(altBICCode);
			list = (ArrayList) environment.getFactory().findByQuery(IBOBicCodes.BONAME, whereClause, params, null,
					false);
		}
		/*
		 * String altACCNumber = getF_IN_Alternate_Account_Number(); if (altACCNumber !=
		 * null && altACCNumber.length() > 0) { String whereClause = null; whereClause =
		 * "where " + IBOAccount.ACCOUNTID + " = ? "; ArrayList params = new
		 * ArrayList(); ArrayList list = new ArrayList(); try {
		 * params.add(altACCNumber); list = (ArrayList)
		 * environment.getFactory().findByQuery( IBOAccount.BONAME, whereClause,
		 * params, null); if (list == null || list.size() <= 0) {
		 * displayMessifError(list, "Account number not available with respect to
		 * AltAccnumber", altACCNumber); } } catch (BODefinitionException d) { } }
		 * String creditACCNumber = getF_IN_Swift_CR_Account_Number(); if
		 * (creditACCNumber != null && creditACCNumber.length() > 0) { String
		 * whereClause = null; whereClause = "where " + IBOAccount.ACCOUNTID + " = ? ";
		 * ArrayList params = new ArrayList(); ArrayList list = new ArrayList(); try {
		 * params.add(creditACCNumber); list = (ArrayList)
		 * environment.getFactory().findByQuery( IBOAccount.BONAME, whereClause,
		 * params, null); if (list == null || list.size() <= 0) {
		 * displayMessifError(list, "Account number not available with respect to
		 * CrAccNo", creditACCNumber); } } catch (BODefinitionException d) { } }
		 * String denitACCNumber = getF_IN_Swift_DR_Account_Number(); if
		 * (denitACCNumber != null && denitACCNumber.length() > 0) { String
		 * whereClause = null; whereClause = "where " + IBOAccount.ACCOUNTID + " = ? ";
		 * ArrayList params = new ArrayList(); ArrayList list = new ArrayList(); try {
		 * params.add(denitACCNumber); list = (ArrayList)
		 * environment.getFactory().findByQuery( IBOAccount.BONAME, whereClause,
		 * params, null); if (list == null || list.size() <= 0) {
		 * displayMessifError(list, "Account number not available with respect to
		 * DrAccNo", denitACCNumber); } } catch (BODefinitionException d) { } }
		 */

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
		//throw new BankFusionException(val, obj, logger, env);
		EventsHelper.handleEvent(val, obj, new HashMap(), env);
	}
}
