/* **********************************************************
 * Copyright (c) 2008 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/

package com.misys.ub.swift;

import java.io.Serializable;/**
 
 * @author Vipesh
 * 
 */

import com.trapedza.bankfusion.core.CommonConstants;

public class StatementInfo   implements Serializable{

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private String StatementLine = CommonConstants.EMPTY_STRING;
	private String InfoToOwner = CommonConstants.EMPTY_STRING;

	public StatementInfo() {
		// TODO Auto-generated constructor stub
	}

	public String getInfoToOwner() {
		return InfoToOwner;
	}

	public void setInfoToOwner(String infoToOwner) {
		InfoToOwner = infoToOwner;
	}

	public String getStatementLine() {
		return StatementLine;
	}

	public void setStatementLine(String statementLine) {
		StatementLine = statementLine;
	}

}
