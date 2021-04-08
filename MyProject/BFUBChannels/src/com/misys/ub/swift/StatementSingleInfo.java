/* **********************************************************
 * Copyright (c) 2008 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/

package com.misys.ub.swift;

import java.io.Serializable;

import com.trapedza.bankfusion.core.CommonConstants;
/**
 * @author Vipesh
 * 
 */

public class StatementSingleInfo  implements Serializable{

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private String StatementLine = CommonConstants.EMPTY_STRING;

	public StatementSingleInfo() {
		// TODO Auto-generated constructor stub
	}

	public String getStatementLine() {
		return StatementLine;
	}

	public void setStatementLine(String statementLine) {
		StatementLine = statementLine;
	}

}
