/* ********************************************************************************
 *  Copyright(c)2007  Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 * 
 * $Id: MGM_QueryFeeRequestActivity.java,v 1.3 2008/08/12 20:14:27 vivekr Exp $
 *  
 * $Log: MGM_QueryFeeRequestActivity.java,v $
 * Revision 1.3  2008/08/12 20:14:27  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.1.4.1  2008/07/03 17:55:55  vivekr
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

import java.math.BigDecimal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractMGM_QueryFeeRequestActivity;
import com.trapedza.bankfusion.steps.refimpl.IMGM_QueryFeeRequestActivity;

/**
 * This fatom is used to generate the xml request for Query Fee Request.
 * @author nileshk
 *
 */

public class MGM_QueryFeeRequestActivity extends AbstractMGM_QueryFeeRequestActivity implements
		IMGM_QueryFeeRequestActivity {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	/**
	 * The Logger defined.
	 */
	private transient final static Log logger = LogFactory.getLog(MGM_QueryFeeRequestActivity.class.getName());

	/**
	 * Constructor
	 * @param env
	 */
	public MGM_QueryFeeRequestActivity(BankFusionEnvironment env) {
		super(env);
	}

	/**
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractMGM_QueryFeeRequestActivity#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 * @param environment The BankFusion Environment
	 * @
	 */
	public void process(BankFusionEnvironment environment) {
		StringBuffer queryFeeXmlRequest = new StringBuffer();
		queryFeeXmlRequest.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" + "<soapenv:Body>"
				+ "<feeLookupRequest xmlns=\"http://www.moneygram.com/AgentConnect40\">" + "<unitProfileID>"
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
				+ "<productType>"
				+ this.getF_IN_productType() + "</productType>");

		if (this.getF_IN_amountExcludingFee().compareTo(BigDecimal.valueOf(0.00)) > 0)
			queryFeeXmlRequest.append("<amountExcludingFee>" + this.getF_IN_amountExcludingFee()
					+ "</amountExcludingFee>");

		else if (this.getF_IN_amountIncludingFee().compareTo(BigDecimal.valueOf(0.00)) > 0)
			queryFeeXmlRequest.append("<amountIncludingFee>" + this.getF_IN_amountIncludingFee()
					+ "</amountIncludingFee>");
		else
			queryFeeXmlRequest.append("<receiveAmount>" + this.getF_IN_receiveAmount() + "</receiveAmount>");

		queryFeeXmlRequest.append("<receiveCountry>" + this.getF_IN_receiveCountry() + "</receiveCountry>"
				+ "<deliveryOption>" + this.getF_IN_deliveryOption() + "</deliveryOption>" + "</feeLookupRequest>"
				+ "</soapenv:Body>" + "</soapenv:Envelope>");

		this.setF_OUT_queryFeeXmlRequest(queryFeeXmlRequest.toString());
		logger.info("QueryFee request received");
		logger.debug("QueryFee Request:" + queryFeeXmlRequest);
	}

}
