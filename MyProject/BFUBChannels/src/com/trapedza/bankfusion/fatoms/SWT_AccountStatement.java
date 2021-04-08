/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOLimit;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_AccountStatement;
import com.trapedza.bankfusion.steps.refimpl.ISWT_AccountStatement;

public class SWT_AccountStatement extends AbstractSWT_AccountStatement implements ISWT_AccountStatement {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	public BankFusionEnvironment env;

	private transient final static Log logger = LogFactory.getLog(SWT_AccountStatement.class.getName());
	public SWT_AccountStatement(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	public void process(BankFusionEnvironment environment) {
		validitingAccTable(environment);
	}

	private void validitingAccTable(BankFusionEnvironment environment) {
		String accNo = getF_IN_ACCOUNTID();
		String whereClause = "where " + IBOAccount.ACCOUNTID + " = ? ";
		ArrayList params = new ArrayList();
		ArrayList list = new ArrayList();
		IBOLimit branchRefValues = null;
		params.add(accNo);
		setF_OUT_AccountExists(CommonConstants.NO);
		list = (ArrayList) environment.getFactory().findByQuery(IBOAccount.BONAME, whereClause, params, null, false);
		setF_OUT_AccountExists(CommonConstants.YES);
	}

	

	//TODO - UB_REFACTOR - Statics to be used; Ignore BODefinitionException; loopy exception handling
}
