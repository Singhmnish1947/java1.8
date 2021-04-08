/* ***********************************************************************************
 * Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 * $Id: MGM_RefreshRequestXMLGenerator.java,v 1.3 2008/08/12 20:13:43 vivekr Exp $
 *
 */

package com.trapedza.bankfusion.fatoms;

import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.steps.refimpl.AbstractMGM_RefreshRequestXMLGenerator;

public class MGM_RefreshRequestXMLGenerator extends AbstractMGM_RefreshRequestXMLGenerator {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	public MGM_RefreshRequestXMLGenerator(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	public void process(BankFusionEnvironment env) {
		String refreshXmlRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" + "<soapenv:Body>"
				+ "<"
				+ this.getF_IN_requestXMLElement()
				+ " xmlns=\"http://www.moneygram.com/AgentConnect40\">"
				+ "<agentID>"
				+ this.getF_IN_agentID()
				+ "</agentID>"
				+ "<agentSequence>"
				+ this.getF_IN_agentSequence()
				+ "</agentSequence>"
				+ "<token>"
				+ this.getF_IN_token()
				+ "</token>"
				+ "<language>"
				+ this.getF_IN_language()
				+ "</language>"
				+ "<timeStamp>"
				+ SystemInformationManager.getInstance().getBFBusinessDateAsString()
				+ "T"
				+ SystemInformationManager.getInstance().getBFBusinessTimeAsString()
				+ "Z"
				+ "</timeStamp>"
				+ "<apiVersion>"
				+ this.getF_IN_apiVersion()
				+ "</apiVersion>"
				+ "<clientSoftwareVersion>"
				+ this.getF_IN_clientSoftwareVersion()
				+ "</clientSoftwareVersion>"
				+ "</"
				+ this.getF_IN_requestXMLElement() + ">" + "</soapenv:Body>" + "</soapenv:Envelope>";

		this.setF_OUT_MG_RequestXML(refreshXmlRequest);
	}
}
