package com.misys.ub.swift;

import com.trapedza.bankfusion.core.CommonConstants;

public class ForwardBalanceInfo {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private String forwardBalance = CommonConstants.EMPTY_STRING;

	public ForwardBalanceInfo() {
		// TODO Auto-generated constructor stub
	}

	public String getForwardBalance() {
		return forwardBalance;
	}

	public void setForwardBalance(String forwardBalance) {
		this.forwardBalance = forwardBalance;
	}

}
