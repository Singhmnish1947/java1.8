/* ********************************************************************************
 *  Copyright(c)2007  Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 * 
 * $Id: MGM_AgentListRequestActivity.java,v 1.4 2008/08/12 20:14:16 vivekr Exp $
 *  
 * $Log: MGM_AgentListRequestActivity.java,v $
 * Revision 1.4  2008/08/12 20:14:16  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.2.4.1  2008/07/03 17:55:53  vivekr
 * Moved from Dublin CVS Head (For BF1.01 package restructuring)
 *
 * Revision 1.5  2008/06/16 15:21:34  arun
 * UB Refactoring -
 * 1. Formatted/Organized imports
 * 2. BODefinitionException, InvalidExtensionPointException and references removed
 * 3. Removed ServerManager deprecated methods/variables
 * 4. BankfusionPropertyAccess removed - changed to BankfusionPropertySupport
 * 5. Exception Handling refactoring
 * 6. General Refactoring
 *
 * Revision 1.4  2008/06/12 10:51:53  arun
 *  RIO on Head
 *
 * Revision 1.2  2007/10/03 09:28:07  nileshk
 * Bug removed
 *
 * Revision 1.1  2007/09/27 11:58:03  nileshk
 * Added for 3.3a MoneyGram release
 *
 * 
 */

package com.trapedza.bankfusion.fatoms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractMGM_AgentListRequestActivity;
import com.trapedza.bankfusion.steps.refimpl.IMGM_AgentListRequestActivity;

/**
 * This fatom generates the xml request for directory of Agents by city for MoneyGram online AgentList Inquiry. 
 * @author nileshk
 *
 */
public class MGM_AgentListRequestActivity extends AbstractMGM_AgentListRequestActivity implements
		IMGM_AgentListRequestActivity {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	/**
	 * Logger defined.
	 */
	private transient final static Log logger = LogFactory.getLog(MGM_AgentListRequestActivity.class.getName());

	/**
	 * Constructor
	 * @param env
	 */
	public MGM_AgentListRequestActivity(BankFusionEnvironment env) {
		super(env);
	}

	/**
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractMGM_AgentListRequestActivity#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 * @param environment The BankFusion Environment
	 * @
	 */
	public void process(BankFusionEnvironment environment) {

		StringBuffer agentListXmlRequest = new StringBuffer();

		agentListXmlRequest.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" + "<soapenv:Body>"
				+ "<directoryOfAgentsByCityRequest xmlns=\"http://www.moneygram.com/AgentConnect40\">"
				+ "<unitProfileID>"
				+ this.getF_IN_UnitProfileId()
				+ "</unitProfileID>"
				+ "<agentID>"
				+ this.getF_IN_AgentId()
				+ "</agentID>"
				+ "<agentSequence>"
				+ this.getF_IN_AgentSequence()
				+ "</agentSequence>"
				+ "<token>"
				+ this.getF_IN_Token()
				+ "</token>"
				+ "<timeStamp>"
				+ this.getF_IN_TimeStamp()
				+ "</timeStamp>"
				+ "<apiVersion>"
				+ this.getF_IN_ApiVersion()
				+ "</apiVersion>"
				+ "<clientSoftwareVersion>"
				+ this.getF_IN_ClientVersion()
				+ "</clientSoftwareVersion>"
				+ "<country>"
				+ this.getF_IN_CountryCode()
				+ "</country>");

		//				xml request if state code is required i.e. for USA, Canada and Mexico
		if (this.getF_IN_StateCode().length() > 0)
			agentListXmlRequest.append("<state>" + this.getF_IN_StateCode() + "</state>");

		agentListXmlRequest.append("<city>" + this.getF_IN_CityChar() + "</city>" + "</directoryOfAgentsByCityRequest>"
				+ "</soapenv:Body>" + "</soapenv:Envelope>");

		this.setF_OUT_AgentXmlRequest(agentListXmlRequest.toString());
		logger.info("AgentList Request generated");
		logger.debug("AgentList request :" + agentListXmlRequest);
	}

}
