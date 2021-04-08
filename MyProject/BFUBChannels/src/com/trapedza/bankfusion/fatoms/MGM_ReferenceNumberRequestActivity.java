/* ********************************************************************************
 *  Copyright(c)2007  Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 * 
 * $Id: MGM_ReferenceNumberRequestActivity.java,v 1.3 2008/08/12 20:14:23 vivekr Exp $
 *  
 * $Log: MGM_ReferenceNumberRequestActivity.java,v $
 * Revision 1.3  2008/08/12 20:14:23  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.1.4.1  2008/07/03 17:55:56  vivekr
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
 * Revision 1.1  2007/09/28 09:44:12  nileshk
 * Added for 3.3a MoneyGram release
 *
 * 
 */

package com.trapedza.bankfusion.fatoms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractMGM_ReferenceNumberRequestActivity;
import com.trapedza.bankfusion.steps.refimpl.IMGM_ReferenceNumberRequestActivity;

/**
 * This fatom is used to generate xml request for Reference Number inquiry.
 * @author nileshk
 *
 */
public class MGM_ReferenceNumberRequestActivity extends AbstractMGM_ReferenceNumberRequestActivity implements
		IMGM_ReferenceNumberRequestActivity {

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
	private transient final static Log logger = LogFactory.getLog(MGM_ReferenceNumberRequestActivity.class.getName());

	/**
	 * Constructor
	 * @param env
	 */
	public MGM_ReferenceNumberRequestActivity(BankFusionEnvironment env) {
		super(env);
	}

	/**
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractMGM_ReferenceNumberRequestActivity#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 * @param environment The BankFusion Environment
	 * @
	 */
	public void process(BankFusionEnvironment environment) {
		StringBuffer referenceNumberRequestXml = new StringBuffer();
		referenceNumberRequestXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" + "<soapenv:Body>"
				+ "<referenceNumberRequest xmlns=\"http://www.moneygram.com/AgentConnect40\">" + "<unitProfileID>"
				+ this.getF_IN_unitProfileID()
				+ "</unitProfileID>"
				+ "<agentID>"
				+ this.getF_IN_agentID()
				+ "</agentID>"
				+ "<agentSequence>"
				+ this.getF_IN_agentSequence()
				+ "</agentSequence>"
				+ "<token>"
				+ this.getF_IN_token()
				+ "</token>"
				+ "<timeStamp>"
				+ this.getF_IN_timeStamp()
				+ "</timeStamp>"
				+ "<apiVersion>"
				+ this.getF_IN_apiVersion()
				+ "</apiVersion>"
				+ "<clientSoftwareVersion>"
				+ this.getF_IN_clientSoftwareVersion()
				+ "</clientSoftwareVersion>"
				+ "<referenceNumber>"
				+ this.getF_IN_referenceNumber()
				+ "</referenceNumber>"
				+ "</referenceNumberRequest>"
				+ "</soapenv:Body>" + "</soapenv:Envelope>");

		this.setF_OUT_referenceNumberRequestXml(referenceNumberRequestXml.toString());
		logger.info("ReferenceNumber request sent");
		logger.debug("ReferenceNumber request:" + referenceNumberRequestXml);
	}

}
