/* ********************************************************************************
 *  Copyright(c)2007  Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 * 
 * $Id: MGM_ReceiveMoneyGramRequestActivity.java,v 1.4 2008/08/12 20:13:58 vivekr Exp $
 *  
 * $Log: MGM_ReceiveMoneyGramRequestActivity.java,v $
 * Revision 1.4  2008/08/12 20:13:58  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.2.4.1  2008/07/03 17:55:56  vivekr
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
 * Revision 1.2  2008/01/30 09:45:14  nileshk
 * Receive MoneyGram refactoring
 *
 * Revision 1.1  2007/09/28 12:24:23  nileshk
 * Added for 3.3a MoneyGram release
 *
 * 
 */

package com.trapedza.bankfusion.fatoms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractMGM_ReceiveMoneyGramRequestActivity;
import com.trapedza.bankfusion.steps.refimpl.IMGM_ReceiveMoneyGramRequestActivity;

/**
 * This fatom is used to generate xml request for ReceiveMoneyGram.
 * @author nileshk
 *
 */
public class MGM_ReceiveMoneyGramRequestActivity extends AbstractMGM_ReceiveMoneyGramRequestActivity implements
		IMGM_ReceiveMoneyGramRequestActivity {

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
	private transient final static Log logger = LogFactory.getLog(MGM_ReceiveMoneyGramRequestActivity.class.getName());

	/**
	 * Constructor
	 * @param env
	 */
	public MGM_ReceiveMoneyGramRequestActivity(BankFusionEnvironment env) {
		super(env);
	}

	/**
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractMGM_ReceiveMoneyGramRequestActivity#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 * @param environment The BankFusion Environment
	 * @
	 */
	public void process(BankFusionEnvironment environment) {
		StringBuffer receiveMoneyGramXmlRequest = new StringBuffer();
		receiveMoneyGramXmlRequest.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" + "<soapenv:Body>"
				+ "<moneyGramReceiveRequest xmlns=\"http://www.moneygram.com/AgentConnect40\">" + "<unitProfileID>"
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
				+ "<operatorName>"
				+ this.getF_IN_operatorName()
				+ "</operatorName>"
				+ "<referenceNumber>"
				+ this.getF_IN_referenceNumber()
				+ "</referenceNumber>"
				+ "<receiveCurrency>"
				+ this.getF_IN_receiveCurrency()
				+ "</receiveCurrency>"
				+ "<agentCheckAmount>"
				+ this.getF_IN_agentCheckAmount()
				+ "</agentCheckAmount>"
				+ "<receiverAddress>"
				+ this.getF_IN_receiverAddress()
				+ "</receiverAddress>"
				+ "<receiverCity>"
				+ this.getF_IN_receiverCity() + "</receiverCity>");

		if (this.getF_IN_receiverState().length() > 0)
			receiveMoneyGramXmlRequest.append("<receiverState>" + this.getF_IN_receiverState() + "</receiverState>");

		receiveMoneyGramXmlRequest.append("<receiverZipCode>" + this.getF_IN_receiverZipCode() + "</receiverZipCode>");

		if (this.getF_IN_receiverPhotoIdType().length() > 0) {
			receiveMoneyGramXmlRequest.append("<receiverPhotoIdType>" + this.getF_IN_receiverPhotoIdType()
					+ "</receiverPhotoIdType>" + "<receiverPhotoIdNumber>" + this.getF_IN_receiverPhotoIdNumber()
					+ "</receiverPhotoIdType>");
		}

		if (this.getF_IN_receiverPhotoIdState().length() > 0)
			receiveMoneyGramXmlRequest.append("<receiverPhotoIdState>" + this.getF_IN_receiverPhotoIdState()
					+ "</receiverPhotoIdState>");

		if (this.getF_IN_receiverPhotoIdCountry().length() > 0)
			receiveMoneyGramXmlRequest.append("<receiverPhotoIdCountry>" + this.getF_IN_receiverPhotoIdCountry()
					+ "</receiverPhotoIdCountry>");

		if (this.getF_IN_receiverLegalIdType().length() > 0) {
			receiveMoneyGramXmlRequest.append("<receiverLegalIdType>" + this.getF_IN_receiverLegalIdType()
					+ "</receiverLegalIdType>" + "<receiverLegalIdNumber>" + this.getF_IN_receiverLegalIdNumber()
					+ "</receiverLegalIdNumber>");
		}

		if (this.getF_IN_receiverDOB().length() >= 8)
			receiveMoneyGramXmlRequest.append("<receiverDOB>" + this.getF_IN_receiverDOB() + "</receiverDOB>");

		receiveMoneyGramXmlRequest.append("<receiverOccupation>" + this.getF_IN_receiverOccupation()
				+ "</receiverOccupation>");

		if (this.getF_IN_thirdPartyFirstName().length() > 0) {
			receiveMoneyGramXmlRequest.append("<thirdPartyFirstName>" + this.getF_IN_thirdPartyFirstName()
					+ "</thirdPartyFirstName>");

			if (this.getF_IN_thirdPartyMiddleInitial().length() > 0)
				receiveMoneyGramXmlRequest.append("<thirdPartyMiddleInitial>" + this.getF_IN_thirdPartyMiddleInitial()
						+ "</thirdPartyMiddleInitial>");

			receiveMoneyGramXmlRequest
					.append("<thirdPartyLastName>" + this.getF_IN_thirdPartyLastName() + "</thirdPartyLastName>"
							+ "<thirdPartyAddress>" + this.getF_IN_thirdPartyAddress() + "</thirdPartyAddress>"
							+ "<thirdPartyCity>" + this.getF_IN_thirdPartyCity() + "</thirdPartyCity>");

			if (this.getF_IN_thirdPartyState().length() > 0)
				receiveMoneyGramXmlRequest.append("<thirdPartyState>" + this.getF_IN_thirdPartyState()
						+ "</thirdPartyState>");

			receiveMoneyGramXmlRequest.append("<thirdPartyZipCode>" + this.getF_IN_thirdPartyZipCode()
					+ "</thirdPartyZipCode>");

			if (this.getF_IN_thirdPartyCountry().length() > 0)
				receiveMoneyGramXmlRequest.append("<thirdPartyCountry>" + this.getF_IN_thirdPartyCountry()
						+ "</thirdPartyCountry>");

			if (this.getF_IN_thirdPartyLegalIdType().length() > 0) {
				receiveMoneyGramXmlRequest.append("<thirdPartyLegalIdType>" + this.getF_IN_thirdPartyLegalIdType()
						+ "</thirdPartyLegalIdType>" + "<thirdPartyLegalIdNumber>"
						+ this.getF_IN_thirdPartyLegalIdNumber() + "</thirdPartyLegalIdNumber>");
			}

			if (this.getF_IN_thirdPartyDOB().length() >= 8)
				receiveMoneyGramXmlRequest
						.append("<thirdPartyDOB>" + this.getF_IN_thirdPartyDOB() + "</thirdPartyDOB>");

			receiveMoneyGramXmlRequest
					.append("<thirdPartyOccupation>" + this.getF_IN_thirdPartyOccupation() + "</thirdPartyOccupation>"
							+ "<thirdPartyOrg>" + this.getF_IN_thirdPartyOrg() + "</thirdPartyOrg>");
		}

		receiveMoneyGramXmlRequest.append("<receiverBirthCity>" + this.getF_IN_receiverBirthCity()
				+ "</receiverBirthCity>");

		if (this.getF_IN_receiverBirthCountry().length() > 0)
			receiveMoneyGramXmlRequest.append("<receiverBirthCountry>" + this.getF_IN_receiverBirthCountry()
					+ "</receiverBirthCountry>");

		if (this.getF_IN_receiverPassportIssueDate().length() >= 8)
			receiveMoneyGramXmlRequest.append("<receiverPassportIssueDate>" + this.getF_IN_receiverPassportIssueDate()
					+ "</receiverPassportIssueDate>");

		if (this.getF_IN_receiverPassportIssueCountry().length() > 0)
			receiveMoneyGramXmlRequest.append("<receiverPassportIssueCountry>"
					+ this.getF_IN_receiverPassportIssueCountry() + "</receiverPassportIssueCountry>");

		receiveMoneyGramXmlRequest.append("<agentUseReceiveData>" + this.getF_IN_agentUseReceiveData()
				+ "</agentUseReceiveData>" + "</moneyGramReceiveRequest>" + "</soapenv:Body>" + "</soapenv:Envelope>");

		this.setF_OUT_receiveMoneyGramXmlRequest(receiveMoneyGramXmlRequest.toString());
		logger.info("ReceiveMoneyGram request sent");
		logger.debug("ReceiveMoneyGram request :" + receiveMoneyGramXmlRequest);
	}

}
