/* ********************************************************************************
 *  Copyright(c)2007  Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 * 
 * $Id: MGM_SendMoneyGramRequestActivity.java,v 1.4 2008/08/12 20:13:56 vivekr Exp $
 *  
 * $Log: MGM_SendMoneyGramRequestActivity.java,v $
 * Revision 1.4  2008/08/12 20:13:56  vivekr
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
 * Revision 1.2  2008/01/21 12:19:07  nileshk
 * Send MoneyGram xml requests refactored
 *
 * Revision 1.1  2007/09/30 10:15:16  nileshk
 * Added for 3.3a MoneyGram release
 *
 * 
 */

package com.trapedza.bankfusion.fatoms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractMGM_SendMoneyGramRequestActivity;
import com.trapedza.bankfusion.steps.refimpl.IMGM_SendMoneyGramRequestActivity;

/**
 * This fatom is used to generate the xml request for SendMoneyGram.
 * @author nileshk
 *
 */
public class MGM_SendMoneyGramRequestActivity extends AbstractMGM_SendMoneyGramRequestActivity implements
		IMGM_SendMoneyGramRequestActivity {

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
	private transient final static Log logger = LogFactory.getLog(MGM_SendMoneyGramRequestActivity.class.getName());

	/**
	 * Constructor
	 * @param env
	 */
	public MGM_SendMoneyGramRequestActivity(BankFusionEnvironment env) {
		super(env);
	}

	/**
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractMGM_SendMoneyGramRequestActivity#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 * @param environment The BankFusion Environment
	 * @
	 */
	public void process(BankFusionEnvironment environment) {
		StringBuffer sendMoneyGramXmlRequest = new StringBuffer();
		sendMoneyGramXmlRequest.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" + "<soapenv:Body>"
				+ "<moneyGramSendRequest xmlns=\"http://www.moneygram.com/AgentConnect40\">" + "<unitProfileID>"
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
				+ "<amount>"
				+ this.getF_IN_amount()
				+ "</amount>"
				+ "<feeAmount>" + this.getF_IN_feeAmount() + "</feeAmount>");

		if (this.getF_IN_freqCustCardNumber().length() > 0)
			sendMoneyGramXmlRequest.append("<freqCustCardNumber>" + this.getF_IN_freqCustCardNumber()
					+ "</freqCustCardNumber>");

		sendMoneyGramXmlRequest.append("<receiveCountry>" + this.getF_IN_receiveCountry() + "</receiveCountry>"
				+ "<deliveryOption>" + this.getF_IN_deliveryOption() + "</deliveryOption>" + "<senderFirstName>"
				+ this.getF_IN_senderFirstName() + "</senderFirstName>");

		if (this.getF_IN_senderMiddleInitial().length() > 0)
			sendMoneyGramXmlRequest.append("<senderMiddleInitial>" + this.getF_IN_senderMiddleInitial()
					+ "</senderMiddleInitial>");

		sendMoneyGramXmlRequest.append("<senderLastName>" + this.getF_IN_senderLastName() + "</senderLastName>"
				+ "<senderAddress>" + this.getF_IN_senderAddress() + "</senderAddress>" + "<senderCity>"
				+ this.getF_IN_senderCity() + "</senderCity>");

		if (this.getF_IN_senderState().length() > 0)
			sendMoneyGramXmlRequest.append("<senderState>" + this.getF_IN_senderState() + "</senderState>");

		if (this.getF_IN_senderZipCode().length() > 0)
			sendMoneyGramXmlRequest.append("<senderZipCode>" + this.getF_IN_senderZipCode() + "</senderZipCode>");

		sendMoneyGramXmlRequest.append("<senderCountry>" + this.getF_IN_senderCountry() + "</senderCountry>"
				+ "<senderHomePhone>" + this.getF_IN_senderHomePhone() + "</senderHomePhone>" + "<receiverFirstName>"
				+ this.getF_IN_receiverFirstName() + "</receiverFirstName>");

		if (this.getF_IN_receiverMiddleInitial().length() > 0)
			sendMoneyGramXmlRequest.append("<receiverMiddleInitial>" + this.getF_IN_receiverMiddleInitial()
					+ "</receiverMiddleInitial>");

		sendMoneyGramXmlRequest.append("<receiverLastName>" + this.getF_IN_receiverLastName() + "</receiverLastName>");

		if (this.getF_IN_receiverLastName2().length() > 0)
			sendMoneyGramXmlRequest.append("<receiverLastName2>" + this.getF_IN_receiverLastName2()
					+ "</receiverLastName2>");

		sendMoneyGramXmlRequest.append("<receiverAddress>" + this.getF_IN_receiverAddress() + "</receiverAddress>"
				+ "<receiverColonia>" + this.getF_IN_receiverColonia() + "</receiverColonia>" + "<receiverMunicipio>"
				+ this.getF_IN_receiverMunicipio() + "</receiverMunicipio>" + "<direction1>"
				+ this.getF_IN_direction1() + "</direction1>" + "<direction2>" + this.getF_IN_direction2()
				+ "</direction2>" + "<direction3>" + this.getF_IN_direction3() + "</direction3>" + "<receiverCity>"
				+ this.getF_IN_receiverCity() + "</receiverCity>");

		if (this.getF_IN_receiverState().length() > 0)
			sendMoneyGramXmlRequest.append("<receiverState>" + this.getF_IN_receiverState() + "</receiverState>");

		if (this.getF_IN_receiverZipCode().length() > 0)
			sendMoneyGramXmlRequest.append("<receiverZipCode>" + this.getF_IN_receiverZipCode() + "</receiverZipCode>");

		sendMoneyGramXmlRequest.append("<receiverPhone>" + this.getF_IN_receiverPhone() + "</receiverPhone>");

		if (this.getF_IN_testQuestion().length() > 0)
			sendMoneyGramXmlRequest.append("<testQuestion>" + this.getF_IN_testQuestion() + "</testQuestion>"
					+ "<testAnswer>" + this.getF_IN_testAnswer() + "</testAnswer>");

		sendMoneyGramXmlRequest.append("<messageField1>" + this.getF_IN_messageField1() + "</messageField1>"
				+ "<messageField2>" + this.getF_IN_messageField2() + "</messageField2>");

		if (this.getF_IN_senderPhotoIdType().length() > 0) {
			sendMoneyGramXmlRequest.append("<senderPhotoIdType>" + this.getF_IN_senderPhotoIdType()
					+ "</senderPhotoIdType>" + "<senderPhotoIdNumber>" + this.getF_IN_senderPhotoIdNumber()
					+ "</senderPhotoIdNumber>");
			if (this.getF_IN_senderPhotoIdState().length() > 0)
				sendMoneyGramXmlRequest.append("<senderPhotoIdState>" + this.getF_IN_senderPhotoIdState()
						+ "</senderPhotoIdState>");
			if (this.getF_IN_senderPhotoIdCountry().length() > 0)
				sendMoneyGramXmlRequest.append("<senderPhotoIdCountry>" + this.getF_IN_senderPhotoIdCountry()
						+ "</senderPhotoIdCountry>");
		}

		if (this.getF_IN_senderLegalIdType().length() > 0) {
			sendMoneyGramXmlRequest.append("<senderLegalIdType>" + this.getF_IN_senderLegalIdType()
					+ "</senderLegalIdType>" + "<senderLegalIdNumber>" + this.getF_IN_senderLegalIdNumber()
					+ "</senderLegalIdNumber>");
		}

		if (this.getF_IN_senderDOB().length() >= 8)
			sendMoneyGramXmlRequest.append("<senderDOB>" + this.getF_IN_senderDOB() + "</senderDOB>");

		sendMoneyGramXmlRequest.append("<senderOccupation>" + this.getF_IN_senderOccupation() + "</senderOccupation>");

		if (this.getF_IN_thirdPartyFirstName().length() > 0) {
			sendMoneyGramXmlRequest.append("<thirdPartyFirstName>" + this.getF_IN_thirdPartyFirstName()
					+ "</thirdPartyFirstName>");

			if (this.getF_IN_thirdPartyMiddleInitial().length() > 0)
				sendMoneyGramXmlRequest.append("<thirdPartyMiddleInitial>" + this.getF_IN_thirdPartyMiddleInitial()
						+ "</thirdPartyMiddleInitial>");

			sendMoneyGramXmlRequest
					.append("<thirdPartyLastName>" + this.getF_IN_thirdPartyLastName() + "</thirdPartyLastName>"
							+ "<thirdPartyAddress>" + this.getF_IN_thirdPartyAddress() + "</thirdPartyAddress>"
							+ "<thirdPartyCity>" + this.getF_IN_thirdPartyCity() + "</thirdPartyCity>");

			if (this.getF_IN_thirdPartyState().length() > 0)
				sendMoneyGramXmlRequest.append("<thirdPartyState>" + this.getF_IN_thirdPartyState()
						+ "</thirdPartyState>");

			if (this.getF_IN_thirdPartyZipCode().length() > 0)
				sendMoneyGramXmlRequest.append("<thirdPartyZipCode>" + this.getF_IN_thirdPartyZipCode()
						+ "</thirdPartyZipCode>");

			if (this.getF_IN_thirdPartyCountry().length() > 0)
				sendMoneyGramXmlRequest.append("<thirdPartyCountry>" + this.getF_IN_thirdPartyCountry()
						+ "</thirdPartyCountry>");

			if (this.getF_IN_thirdPartyLegalIdType().length() > 0) {
				sendMoneyGramXmlRequest.append("<thirdPartyLegalIdType>" + this.getF_IN_thirdPartyLegalIdType()
						+ "</thirdPartyLegalIdType>" + "<thirdPartyLegalIdNumber>"
						+ this.getF_IN_thirdPartyLagelIdNumber() + "</thirdPartyLegalIdNumber>");
			}
			if (this.getF_IN_thirdPartyDOB().length() >= 8)
				sendMoneyGramXmlRequest.append("<thirdPartyDOB>" + this.getF_IN_thirdPartyDOB() + "</thirdPartyDOB>");

			sendMoneyGramXmlRequest
					.append("<thirdPartyOccupation>" + this.getF_IN_thirdPartyOccupation() + "</thirdPartyOccupation>"
							+ "<thirdPartyOrg>" + this.getF_IN_thirdPartyOrg() + "</thirdPartyOrg>");
		}

		sendMoneyGramXmlRequest.append("<senderBirthCity>" + this.getF_IN_senderBirthCity() + "</senderBirthCity>");

		if (this.getF_IN_senderBirthCountry().length() > 0)
			sendMoneyGramXmlRequest.append("<senderBirthCountry>" + this.getF_IN_senderBirthCountry()
					+ "</senderBirthCountry>");

		if (this.getF_IN_senderPassportIssueDate().length() >= 8)
			sendMoneyGramXmlRequest.append("<senderPassportIssueDate>" + this.getF_IN_senderPassportIssueDate()
					+ "</senderPassportIssueDate>");

		if (this.getF_IN_senderPassportIssueCity().length() > 0)
			sendMoneyGramXmlRequest.append("<senderPassportIssueCity>" + this.getF_IN_senderPassportIssueCity()
					+ "</senderPassportIssueCity>");

		if (this.getF_IN_senderPassportIssueCountry().length() > 0)
			sendMoneyGramXmlRequest.append("<senderPassportIssueCountry>" + this.getF_IN_senderPassportIssueCountry()
					+ "</senderPassportIssueCountry>");

		sendMoneyGramXmlRequest.append("</moneyGramSendRequest>" + "</soapenv:Body>" + "</soapenv:Envelope>");

		this.setF_OUT_sendMoneyGramXmlRequest(sendMoneyGramXmlRequest.toString());
		logger.info("SendMoneyGram request received");
		logger.debug("SendMoneyGram Request:" + sendMoneyGramXmlRequest);

	}

}
