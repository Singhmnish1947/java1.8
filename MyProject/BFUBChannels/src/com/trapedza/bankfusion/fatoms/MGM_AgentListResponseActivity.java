/* ********************************************************************************
 *  Copyright(c)2007  Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 *
 * $Id: MGM_AgentListResponseActivity.java,v 1.6 2008/08/12 20:14:23 vivekr Exp $
 *
 * $Log: MGM_AgentListResponseActivity.java,v $
 * Revision 1.6  2008/08/12 20:14:23  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.4.4.1  2008/07/03 17:55:53  vivekr
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
 * Revision 1.4  2008/02/08 05:51:37  nileshk
 * Catching unknown MoneyGram exception
 *
 * Revision 1.3  2007/10/17 07:38:22  nileshk
 * Code added to handle exception in absence of AgentList
 *
 * Revision 1.2  2007/10/03 09:28:38  nileshk
 * Code added to handle exception in absence of ErrorCode from MoneyGram
 *
 * Revision 1.1  2007/09/27 11:58:03  nileshk
 * Added for 3.3a MoneyGram release
 *
 *
 */

package com.trapedza.bankfusion.fatoms;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.misys.ub.moneygram.MGM_Constants;
import com.misys.ub.moneygram.MGM_ExceptionHelper;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractMGM_AgentListResponseActivity;
import com.trapedza.bankfusion.steps.refimpl.IMGM_AgentListResponseActivity;

/**
 * This fatom takes the AgentList xml response from MoneyGram and generates the required BankFusion tag.
 * @author nileshk
 */

public class MGM_AgentListResponseActivity extends AbstractMGM_AgentListResponseActivity implements
		IMGM_AgentListResponseActivity {

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
	private transient final static Log logger = LogFactory.getLog(MGM_AgentListResponseActivity.class.getName());

	/**
	 * Constructor
	 * @param env
	 */
	public MGM_AgentListResponseActivity(BankFusionEnvironment env) {
		super(env);
	}

	/**
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractMGM_AgentListResponseActivity#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 * @param environment The BankFusion Environment
	 * @
	 */
	public void process(BankFusionEnvironment environment) {
		MGM_ExceptionHelper exceptionHelper = new MGM_ExceptionHelper();
		String agentListXmlResponse = this.getF_IN_agentListXmlResponse();

		String doCheckIn = CommonConstants.EMPTY_STRING;
		String timeStamp = CommonConstants.EMPTY_STRING;
		String flags = CommonConstants.EMPTY_STRING;
		String agentList = CommonConstants.EMPTY_STRING;
		try {
			//loading and parsing the xml response
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(agentListXmlResponse)));

			Element soapEnvelope = document.getDocumentElement();
			Node soapBody = soapEnvelope.getFirstChild();
			Node agentLookUpResponse = soapBody.getFirstChild();
			if ((agentLookUpResponse.getNodeName().equalsIgnoreCase(MGM_Constants.AC_DIRECTORYOFAGENTSBYCITYRESPONSE))) {
				//Extracting the values of tags.
				Node firstChild = agentLookUpResponse.getFirstChild();
				Node doCheckInNode = firstChild.getFirstChild();
				if (doCheckInNode != null)
					doCheckIn = doCheckInNode.getNodeValue();

				Node nextChild = firstChild.getNextSibling();
				Node timeStampNode = nextChild.getFirstChild();
				if (timeStampNode != null)
					timeStamp = timeStampNode.getNodeValue();

				nextChild = nextChild.getNextSibling();
				Node flagsNode = nextChild.getFirstChild();
				if (flagsNode != null)
					flags = flagsNode.getNodeValue();
				//Extracting the values of agentInfo child tags.
				do {

					nextChild = nextChild.getNextSibling();
					if (nextChild == null) {
						exceptionHelper.throwMoneyGramException(9266, environment);
					} 
					else {
					Node agentInfoNextChild =nextChild.getFirstChild();
					char c = ' ';

					if (agentInfoNextChild.getNodeName().equals(MGM_Constants.AC_AGENTNAME)) {
						StringBuffer agentName = new StringBuffer(agentInfoNextChild.getFirstChild().getNodeValue());
						while (agentName.length() < 30) {
							agentName.append(c);
						}
						agentList = agentList + agentName;

					}
					else {
						agentInfoNextChild = agentInfoNextChild.getPreviousSibling();
						StringBuffer agentName = new StringBuffer(CommonConstants.EMPTY_STRING);
						while (agentName.length() < 30) {
							agentName.append(c);
						}
						agentList = agentList + agentName;
					}

					agentInfoNextChild = agentInfoNextChild.getNextSibling();
					if (agentInfoNextChild.getNodeName().equals(MGM_Constants.AC_ADDRESS)) {
						StringBuffer address = new StringBuffer(agentInfoNextChild.getFirstChild().getNodeValue());
						while (address.length() < 30) {
							address.append(c);
						}
						agentList = agentList + address;
					}
					else {
						agentInfoNextChild = agentInfoNextChild.getPreviousSibling();
						StringBuffer address = new StringBuffer(CommonConstants.EMPTY_STRING);
						while (address.length() < 30) {
							address.append(c);
						}
						agentList = agentList + address;
					}

					agentInfoNextChild = agentInfoNextChild.getNextSibling();
					if (agentInfoNextChild.getNodeName().equals(MGM_Constants.AC_CITY)) {
						StringBuffer city = new StringBuffer(agentInfoNextChild.getFirstChild().getNodeValue());
						while (city.length() < 20) {
							city.append(c);
						}
						agentList = agentList + city;
					}
					else {
						agentInfoNextChild = agentInfoNextChild.getPreviousSibling();
						StringBuffer city = new StringBuffer(CommonConstants.EMPTY_STRING);
						while (city.length() < 20) {
							city.append(c);
						}
						agentList = agentList + city;
					}

					agentInfoNextChild = agentInfoNextChild.getNextSibling();
					if (agentInfoNextChild.getNodeName().equals(MGM_Constants.AC_STATE)) {
						StringBuffer state = new StringBuffer(CommonConstants.EMPTY_STRING);
						if (!(agentInfoNextChild.getFirstChild() == null))
							state = new StringBuffer(agentInfoNextChild.getFirstChild().getNodeValue());
						while (state.length() < 2) {
							state.append(c);
						}
						agentList = agentList + state;
					}
					else {
						agentInfoNextChild = agentInfoNextChild.getPreviousSibling();
						StringBuffer state = new StringBuffer(CommonConstants.EMPTY_STRING);
						while (state.length() < 2) {
							state.append(c);
						}
						agentList = agentList + state;
					}

					agentInfoNextChild = agentInfoNextChild.getNextSibling();
					if (agentInfoNextChild.getNodeName().equals(MGM_Constants.AC_RECEIVECAPABILITY)) {
						StringBuffer receiveCapability = new StringBuffer(agentInfoNextChild.getFirstChild()
								.getNodeValue());
						while (receiveCapability.length() < 5) {
							receiveCapability.append(c);
						}
						agentList = agentList + receiveCapability;
					}
					else {
						agentInfoNextChild = agentInfoNextChild.getPreviousSibling();
						StringBuffer receiveCapability = new StringBuffer(CommonConstants.EMPTY_STRING);
						while (receiveCapability.length() < 5) {
							receiveCapability.append(c);
						}
						agentList = agentList + receiveCapability;
					}

					agentInfoNextChild = agentInfoNextChild.getNextSibling();
					if (agentInfoNextChild.getNodeName().equals(MGM_Constants.AC_SENDCAPABILITY)) {
						StringBuffer sendCapability = new StringBuffer(agentInfoNextChild.getFirstChild()
								.getNodeValue());
						while (sendCapability.length() < 5) {
							sendCapability.append(c);
						}
						agentList = agentList + sendCapability;
					}
					else {
						agentInfoNextChild = agentInfoNextChild.getPreviousSibling();
						StringBuffer sendCapability = new StringBuffer(CommonConstants.EMPTY_STRING);
						while (sendCapability.length() < 5) {
							sendCapability.append(c);
						}
						agentList = agentList + sendCapability;
					}

					agentInfoNextChild = agentInfoNextChild.getNextSibling();
					if (agentInfoNextChild.getNodeName().equals(MGM_Constants.AC_AGENTPHONE)) {
						StringBuffer agentPhone = new StringBuffer(CommonConstants.EMPTY_STRING);
						if (!(agentInfoNextChild.getFirstChild() == null))
							agentPhone = new StringBuffer(agentInfoNextChild.getFirstChild().getNodeValue());

						while (agentPhone.length() < 16) {
							agentPhone.append(c);
						}
						agentList = agentList + agentPhone;
					}
					else {
						agentInfoNextChild = agentInfoNextChild.getPreviousSibling();
						StringBuffer agentPhone = new StringBuffer(CommonConstants.EMPTY_STRING);
						while (agentPhone.length() < 16) {
							agentPhone.append(c);
						}
						agentList = agentList + agentPhone;
					}

					agentInfoNextChild = agentInfoNextChild.getNextSibling();
					if (agentInfoNextChild.getNodeName().equals(MGM_Constants.AC_MILESDIRECTIONS)) {
						StringBuffer milesDirections = new StringBuffer(CommonConstants.EMPTY_STRING);
						if (!(agentInfoNextChild.getFirstChild() == null))
							milesDirections = new StringBuffer(agentInfoNextChild.getFirstChild().getNodeValue());

						while (milesDirections.length() < 10) {
							milesDirections.append(c);
						}
						agentList = agentList + milesDirections;
					}
					else {
						agentInfoNextChild = agentInfoNextChild.getPreviousSibling();
						StringBuffer milesDirections = new StringBuffer(CommonConstants.EMPTY_STRING);
						while (milesDirections.length() < 10) {
							milesDirections.append(c);
						}
						agentList = agentList + milesDirections;
					}

					agentInfoNextChild = agentInfoNextChild.getNextSibling();
					if (agentInfoNextChild.getNodeName().equals(MGM_Constants.AC_STOREHOURS)) {
						Node storeHoursChildNode = agentInfoNextChild.getFirstChild();

						storeHoursChildNode = storeHoursChildNode.getNextSibling();
						StringBuffer openTime = new StringBuffer(storeHoursChildNode.getFirstChild().getNodeValue());
						if (storeHoursChildNode.getNodeName().equals(MGM_Constants.AC_OPENTIME)) {
							while (openTime.length() < 8) {
								openTime.append(c);
							}
							agentList = agentList + openTime;
						}
						else {
							openTime = new StringBuffer(CommonConstants.EMPTY_STRING);
							while (openTime.length() < 8) {
								openTime.append(c);
							}
							agentList = agentList + openTime;
							storeHoursChildNode = storeHoursChildNode.getPreviousSibling();
						}

						storeHoursChildNode = storeHoursChildNode.getNextSibling();
						StringBuffer closeTime = new StringBuffer(storeHoursChildNode.getFirstChild().getNodeValue());
						if (storeHoursChildNode.getNodeName().equals(MGM_Constants.AC_CLOSETIME)) {
							while (closeTime.length() < 8) {
								closeTime.append(c);
							}
							agentList = agentList + closeTime;
						}
						else {
							closeTime = new StringBuffer(CommonConstants.EMPTY_STRING);
							while (closeTime.length() < 8) {
								closeTime.append(c);
							}
							agentList = agentList + closeTime;
							storeHoursChildNode = storeHoursChildNode.getPreviousSibling();
						}

						storeHoursChildNode = storeHoursChildNode.getNextSibling();
						StringBuffer closed = new StringBuffer(storeHoursChildNode.getFirstChild().getNodeValue());
						if (storeHoursChildNode.getNodeName().equals(MGM_Constants.AC_CLOSED)) {
							while (closed.length() < 5) {
								closed.append(c);
							}
							agentList = agentList + closed;
						}
						else {
							closed = new StringBuffer(CommonConstants.EMPTY_STRING);
							while (closed.length() < 5) {
								closed.append(c);
							}
							agentList = agentList + closed;
							storeHoursChildNode = storeHoursChildNode.getPreviousSibling();
						}
					}

					agentInfoNextChild = agentInfoNextChild.getNextSibling();
					if (agentInfoNextChild.getNodeName().equals(MGM_Constants.AC_STOREHOURS)) {
						Node storeHoursChildNode = agentInfoNextChild.getFirstChild();

						storeHoursChildNode = storeHoursChildNode.getNextSibling();
						StringBuffer openTime = new StringBuffer(storeHoursChildNode.getFirstChild().getNodeValue());
						if (storeHoursChildNode.getNodeName().equals(MGM_Constants.AC_OPENTIME)) {
							while (openTime.length() < 8) {
								openTime.append(c);
							}
							agentList = agentList + openTime;
						}
						else {
							openTime = new StringBuffer(CommonConstants.EMPTY_STRING);
							while (openTime.length() < 8) {
								openTime.append(c);
							}
							agentList = agentList + openTime;
							storeHoursChildNode = storeHoursChildNode.getPreviousSibling();
						}

						storeHoursChildNode = storeHoursChildNode.getNextSibling();
						StringBuffer closeTime = new StringBuffer(storeHoursChildNode.getFirstChild().getNodeValue());
						if (storeHoursChildNode.getNodeName().equals(MGM_Constants.AC_CLOSETIME)) {
							while (closeTime.length() < 8) {
								closeTime.append(c);
							}
							agentList = agentList + closeTime;
						}
						else {
							closeTime = new StringBuffer(CommonConstants.EMPTY_STRING);
							while (closeTime.length() < 8) {
								closeTime.append(c);
							}
							agentList = agentList + closeTime;
							storeHoursChildNode = storeHoursChildNode.getPreviousSibling();
						}

						storeHoursChildNode = storeHoursChildNode.getNextSibling();
						StringBuffer closed = new StringBuffer(storeHoursChildNode.getFirstChild().getNodeValue());
						if (storeHoursChildNode.getNodeName().equals(MGM_Constants.AC_CLOSED)) {
							while (closed.length() < 5) {
								closed.append(c);
							}
							agentList = agentList + closed;
						}
						else {
							closed = new StringBuffer(CommonConstants.EMPTY_STRING);
							while (closed.length() < 5) {
								closed.append(c);
							}
							agentList = agentList + closed;
							storeHoursChildNode = storeHoursChildNode.getPreviousSibling();
						}
					}

					agentInfoNextChild = agentInfoNextChild.getNextSibling();
					if (agentInfoNextChild.getNodeName().equals(MGM_Constants.AC_STOREHOURS)) {
						Node storeHoursChildNode = agentInfoNextChild.getFirstChild();

						storeHoursChildNode = storeHoursChildNode.getNextSibling();
						StringBuffer openTime = new StringBuffer(storeHoursChildNode.getFirstChild().getNodeValue());
						if (storeHoursChildNode.getNodeName().equals(MGM_Constants.AC_OPENTIME)) {
							while (openTime.length() < 8) {
								openTime.append(c);
							}
							agentList = agentList + openTime;
						}
						else {
							openTime = new StringBuffer(CommonConstants.EMPTY_STRING);
							while (openTime.length() < 8) {
								openTime.append(c);
							}
							agentList = agentList + openTime;
							storeHoursChildNode = storeHoursChildNode.getPreviousSibling();
						}

						storeHoursChildNode = storeHoursChildNode.getNextSibling();
						StringBuffer closeTime = new StringBuffer(storeHoursChildNode.getFirstChild().getNodeValue());
						if (storeHoursChildNode.getNodeName().equals(MGM_Constants.AC_CLOSETIME)) {
							while (closeTime.length() < 8) {
								closeTime.append(c);
							}
							agentList = agentList + closeTime;
						}
						else {
							closeTime = new StringBuffer(CommonConstants.EMPTY_STRING);
							while (closeTime.length() < 8) {
								closeTime.append(c);
							}
							agentList = agentList + closeTime;
							storeHoursChildNode = storeHoursChildNode.getPreviousSibling();
						}

						storeHoursChildNode = storeHoursChildNode.getNextSibling();
						StringBuffer closed = new StringBuffer(storeHoursChildNode.getFirstChild().getNodeValue());
						if (storeHoursChildNode.getNodeName().equals(MGM_Constants.AC_CLOSED)) {
							while (closed.length() < 5) {
								closed.append(c);
							}
							agentList = agentList + closed;
						}
						else {
							closed = new StringBuffer(CommonConstants.EMPTY_STRING);
							while (closed.length() < 5) {
								closed.append(c);
							}
							agentList = agentList + closed;
							storeHoursChildNode = storeHoursChildNode.getPreviousSibling();
						}
					}

					agentInfoNextChild = agentInfoNextChild.getNextSibling();
					if (agentInfoNextChild.getNodeName().equals(MGM_Constants.AC_STOREHOURS)) {
						Node storeHoursChildNode = agentInfoNextChild.getFirstChild();

						storeHoursChildNode = storeHoursChildNode.getNextSibling();
						StringBuffer openTime = new StringBuffer(storeHoursChildNode.getFirstChild().getNodeValue());
						if (storeHoursChildNode.getNodeName().equals(MGM_Constants.AC_OPENTIME)) {
							while (openTime.length() < 8) {
								openTime.append(c);
							}
							agentList = agentList + openTime;
						}
						else {
							openTime = new StringBuffer(CommonConstants.EMPTY_STRING);
							while (openTime.length() < 8) {
								openTime.append(c);
							}
							agentList = agentList + openTime;
							storeHoursChildNode = storeHoursChildNode.getPreviousSibling();
						}

						storeHoursChildNode = storeHoursChildNode.getNextSibling();
						StringBuffer closeTime = new StringBuffer(storeHoursChildNode.getFirstChild().getNodeValue());
						if (storeHoursChildNode.getNodeName().equals(MGM_Constants.AC_CLOSETIME)) {
							while (closeTime.length() < 8) {
								closeTime.append(c);
							}
							agentList = agentList + closeTime;
						}
						else {
							closeTime = new StringBuffer(CommonConstants.EMPTY_STRING);
							while (closeTime.length() < 8) {
								closeTime.append(c);
							}
							agentList = agentList + closeTime;
							storeHoursChildNode = storeHoursChildNode.getPreviousSibling();
						}

						storeHoursChildNode = storeHoursChildNode.getNextSibling();
						StringBuffer closed = new StringBuffer(storeHoursChildNode.getFirstChild().getNodeValue());
						if (storeHoursChildNode.getNodeName().equals(MGM_Constants.AC_CLOSED)) {
							while (closed.length() < 5) {
								closed.append(c);
							}
							agentList = agentList + closed;
						}
						else {
							closed = new StringBuffer(CommonConstants.EMPTY_STRING);
							while (closed.length() < 5) {
								closed.append(c);
							}
							agentList = agentList + closed;
							storeHoursChildNode = storeHoursChildNode.getPreviousSibling();
						}
					}

					agentInfoNextChild = agentInfoNextChild.getNextSibling();
					if (agentInfoNextChild.getNodeName().equals(MGM_Constants.AC_STOREHOURS)) {
						Node storeHoursChildNode = agentInfoNextChild.getFirstChild();

						storeHoursChildNode = storeHoursChildNode.getNextSibling();
						StringBuffer openTime = new StringBuffer(storeHoursChildNode.getFirstChild().getNodeValue());
						if (storeHoursChildNode.getNodeName().equals(MGM_Constants.AC_OPENTIME)) {
							while (openTime.length() < 8) {
								openTime.append(c);
							}
							agentList = agentList + openTime;
						}
						else {
							openTime = new StringBuffer(CommonConstants.EMPTY_STRING);
							while (openTime.length() < 8) {
								openTime.append(c);
							}
							agentList = agentList + openTime;
							storeHoursChildNode = storeHoursChildNode.getPreviousSibling();
						}

						storeHoursChildNode = storeHoursChildNode.getNextSibling();
						StringBuffer closeTime = new StringBuffer(storeHoursChildNode.getFirstChild().getNodeValue());
						if (storeHoursChildNode.getNodeName().equals(MGM_Constants.AC_CLOSETIME)) {
							while (closeTime.length() < 8) {
								closeTime.append(c);
							}
							agentList = agentList + closeTime;
						}
						else {
							closeTime = new StringBuffer(CommonConstants.EMPTY_STRING);
							while (closeTime.length() < 8) {
								closeTime.append(c);
							}
							agentList = agentList + closeTime;
							storeHoursChildNode = storeHoursChildNode.getPreviousSibling();
						}

						storeHoursChildNode = storeHoursChildNode.getNextSibling();
						StringBuffer closed = new StringBuffer(storeHoursChildNode.getFirstChild().getNodeValue());
						if (storeHoursChildNode.getNodeName().equals(MGM_Constants.AC_CLOSED)) {
							while (closed.length() < 5) {
								closed.append(c);
							}
							agentList = agentList + closed;
						}
						else {
							closed = new StringBuffer(CommonConstants.EMPTY_STRING);
							while (closed.length() < 5) {
								closed.append(c);
							}
							agentList = agentList + closed;
							storeHoursChildNode = storeHoursChildNode.getPreviousSibling();
						}
					}

					agentInfoNextChild = agentInfoNextChild.getNextSibling();
					if (agentInfoNextChild.getNodeName().equals(MGM_Constants.AC_STOREHOURS)) {
						Node storeHoursChildNode = agentInfoNextChild.getFirstChild();

						storeHoursChildNode = storeHoursChildNode.getNextSibling();
						StringBuffer openTime = new StringBuffer(storeHoursChildNode.getFirstChild().getNodeValue());
						if (storeHoursChildNode.getNodeName().equals(MGM_Constants.AC_OPENTIME)) {
							while (openTime.length() < 8) {
								openTime.append(c);
							}
							agentList = agentList + openTime;
						}
						else {
							openTime = new StringBuffer(CommonConstants.EMPTY_STRING);
							while (openTime.length() < 8) {
								openTime.append(c);
							}
							agentList = agentList + openTime;
							storeHoursChildNode = storeHoursChildNode.getPreviousSibling();
						}

						storeHoursChildNode = storeHoursChildNode.getNextSibling();
						StringBuffer closeTime = new StringBuffer(storeHoursChildNode.getFirstChild().getNodeValue());
						if (storeHoursChildNode.getNodeName().equals(MGM_Constants.AC_CLOSETIME)) {
							while (closeTime.length() < 8) {
								closeTime.append(c);
							}
							agentList = agentList + closeTime;
						}
						else {
							closeTime = new StringBuffer(CommonConstants.EMPTY_STRING);
							while (closeTime.length() < 8) {
								closeTime.append(c);
							}
							agentList = agentList + closeTime;
							storeHoursChildNode = storeHoursChildNode.getPreviousSibling();
						}

						storeHoursChildNode = storeHoursChildNode.getNextSibling();
						StringBuffer closed = new StringBuffer(storeHoursChildNode.getFirstChild().getNodeValue());
						if (storeHoursChildNode.getNodeName().equals(MGM_Constants.AC_CLOSED)) {
							while (closed.length() < 5) {
								closed.append(c);
							}
							agentList = agentList + closed;
						}
						else {
							closed = new StringBuffer(CommonConstants.EMPTY_STRING);
							while (closed.length() < 5) {
								closed.append(c);
							}
							agentList = agentList + closed;
							storeHoursChildNode = storeHoursChildNode.getPreviousSibling();
						}
					}

					agentInfoNextChild = agentInfoNextChild.getNextSibling();
					if (agentInfoNextChild.getNodeName().equals(MGM_Constants.AC_STOREHOURS)) {
						Node storeHoursChildNode = agentInfoNextChild.getFirstChild();

						storeHoursChildNode = storeHoursChildNode.getNextSibling();
						StringBuffer openTime = new StringBuffer(storeHoursChildNode.getFirstChild().getNodeValue());
						if (storeHoursChildNode.getNodeName().equals(MGM_Constants.AC_OPENTIME)) {
							while (openTime.length() < 8) {
								openTime.append(c);
							}
							agentList = agentList + openTime;
						}
						else {
							openTime = new StringBuffer(CommonConstants.EMPTY_STRING);
							while (openTime.length() < 8) {
								openTime.append(c);
							}
							agentList = agentList + openTime;
							storeHoursChildNode = storeHoursChildNode.getPreviousSibling();
						}

						storeHoursChildNode = storeHoursChildNode.getNextSibling();
						StringBuffer closeTime = new StringBuffer(storeHoursChildNode.getFirstChild().getNodeValue());
						if (storeHoursChildNode.getNodeName().equals(MGM_Constants.AC_CLOSETIME)) {
							while (closeTime.length() < 8) {
								closeTime.append(c);
							}
							agentList = agentList + closeTime;
						}
						else {
							closeTime = new StringBuffer(CommonConstants.EMPTY_STRING);
							while (closeTime.length() < 8) {
								closeTime.append(c);
							}
							agentList = agentList + closeTime;
							storeHoursChildNode = storeHoursChildNode.getPreviousSibling();
						}

						storeHoursChildNode = storeHoursChildNode.getNextSibling();
						StringBuffer closed = new StringBuffer(storeHoursChildNode.getFirstChild().getNodeValue());
						if (storeHoursChildNode.getNodeName().equals(MGM_Constants.AC_CLOSED)) {
							while (closed.length() < 5) {
								closed.append(c);
							}
							agentList = agentList + closed;
						}
						else {
							closed = new StringBuffer(CommonConstants.EMPTY_STRING);
							while (closed.length() < 5) {
								closed.append(c);
							}
							agentList = agentList + closed;
							storeHoursChildNode = storeHoursChildNode.getPreviousSibling();
						}
					}
				}
				}
				while (!agentLookUpResponse.getLastChild().equals(nextChild));
				//setting the BankFusion tag values.
				this.setF_OUT_agentList(agentList);
				this.setF_OUT_doCheckIn(doCheckIn);
				this.setF_OUT_flags(flags);
				this.setF_OUT_timeStamp(timeStamp);
				logger.info("AgentList retreived successfully");
			}
			else {
				try {
					logger.info("Error in AgentList inquiry response");
					String errorCodeString = agentLookUpResponse.getFirstChild().getNextSibling().getNextSibling()
							.getFirstChild().getFirstChild().getFirstChild().getNodeValue();
					int errorCode = Integer.parseInt(errorCodeString);
					exceptionHelper.throwMoneyGramException(errorCode, environment);
				}
				catch (NumberFormatException e) {
					logger.error("NumberFormatException", e);
					exceptionHelper.throwMoneyGramException(900, environment);
				}
			}
		}
		catch (ParserConfigurationException e) {
			logger.error("Parsing Error", e);
			exceptionHelper.throwMoneyGramException(014, environment);
		}
		catch (SAXException e) {
			logger.error("SAXException", e);
			exceptionHelper.throwMoneyGramException(014, environment);
		}
		catch (IOException e) {
			logger.error("IOException", e);
			exceptionHelper.throwMoneyGramException(014, environment);
		}
		catch (NullPointerException e) {
			logger.error("NullPointerException", e);
			exceptionHelper.throwMoneyGramException(900, environment);
		}
	}

}
