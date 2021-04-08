/* **********************************************************
 * Copyright (c) 2008 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/package com.misys.ub.swift;

import java.io.Serializable;

import com.trapedza.bankfusion.core.CommonConstants;

/**
 * @author Gaurav Aggarwal
 * 
 */
public class SendersCharges implements Serializable  {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private String senderCharge = CommonConstants.EMPTY_STRING;

	public SendersCharges() {
		// TODO Auto-generated constructor stub
	}

	public String getSenderCharge() {
		return senderCharge;
	}

	public void setSenderCharge(String senderCharge) {
		this.senderCharge = senderCharge;
	}

}
