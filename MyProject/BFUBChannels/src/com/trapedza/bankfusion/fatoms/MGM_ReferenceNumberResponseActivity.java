/* ********************************************************************************
 *  Copyright(c)2007  Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 *
 * $Id: MGM_ReferenceNumberResponseActivity.java,v 1.5 2008/08/12 20:14:21 vivekr Exp $
 *
 * $Log: MGM_ReferenceNumberResponseActivity.java,v $
 * Revision 1.5  2008/08/12 20:14:21  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.3.4.1  2008/07/03 17:55:56  vivekr
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
 * Revision 1.3  2008/02/08 05:55:03  nileshk
 * Catching unknown MoneyGram exception
 *
 * Revision 1.2  2007/10/03 09:29:43  nileshk
 * Code added to handle exception in absence of ErrorCode from MoneyGram
 *
 * Revision 1.1  2007/09/28 09:44:12  nileshk
 * Added for 3.3a MoneyGram release
 *
 *
 */

package com.trapedza.bankfusion.fatoms;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;

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
import com.trapedza.bankfusion.steps.refimpl.AbstractMGM_ReferenceNumberResponseActivity;
import com.trapedza.bankfusion.steps.refimpl.IMGM_ReferenceNumberResponseActivity;

/**
 * This fatom is used to handle the xml response for Reference Number inquiry.
 * @author nileshk
 *
 */
public class MGM_ReferenceNumberResponseActivity extends AbstractMGM_ReferenceNumberResponseActivity implements
		IMGM_ReferenceNumberResponseActivity {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	/**
	 * Loggers defined.
	 */
	private transient final static Log logger = LogFactory.getLog(MGM_ReferenceNumberResponseActivity.class.getName());

	/**
	 * Constructor
	 * @param env
	 */
	public MGM_ReferenceNumberResponseActivity(BankFusionEnvironment env) {
		super(env);
	}

	/**
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractMGM_ReferenceNumberResponseActivity#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 * @param environment The BankFusion Environment
	 * @
	 */
	public void process(BankFusionEnvironment environment) {

		String referenceNumberXmlResponse = this.getF_IN_referenceNumberXmlResponse();

		String doCheckIn = CommonConstants.EMPTY_STRING;
		String timeStamp = CommonConstants.EMPTY_STRING;
		String flags = CommonConstants.EMPTY_STRING;
		String senderFirstName = CommonConstants.EMPTY_STRING;
		String senderMiddleInitial = CommonConstants.EMPTY_STRING;
		String senderLastName = CommonConstants.EMPTY_STRING;
		String senderHomePhone = CommonConstants.EMPTY_STRING;
		String receiverFirstName = CommonConstants.EMPTY_STRING;
		String receiverMiddleName = CommonConstants.EMPTY_STRING;
		String receiverLastName = CommonConstants.EMPTY_STRING;
		String receiverLastName2 = CommonConstants.EMPTY_STRING;
		String receiverAddress = CommonConstants.EMPTY_STRING;
		String direction1 = CommonConstants.EMPTY_STRING;
		String direction2 = CommonConstants.EMPTY_STRING;
		String direction3 = CommonConstants.EMPTY_STRING;
		String receiverColonia = CommonConstants.EMPTY_STRING;
		String receiverMunicipio = CommonConstants.EMPTY_STRING;
		String receiverCity = CommonConstants.EMPTY_STRING;
		String receiverState = CommonConstants.EMPTY_STRING;
		String receiverCountry = CommonConstants.EMPTY_STRING;
		String receiverZipCode = CommonConstants.EMPTY_STRING;
		String receiverPhone = CommonConstants.EMPTY_STRING;
		String testQuestion = CommonConstants.EMPTY_STRING;
		String testAnswer = CommonConstants.EMPTY_STRING;
		String messageField1 = CommonConstants.EMPTY_STRING;
		String messageField2 = CommonConstants.EMPTY_STRING;
		String deliveryOption = CommonConstants.EMPTY_STRING;
		String transactionStatus = CommonConstants.EMPTY_STRING;
		String dateTimeSent = CommonConstants.EMPTY_STRING;
		String receiveCurrency = CommonConstants.EMPTY_STRING;
		String receiveAmount = "0.00";
		String okForAgent = CommonConstants.EMPTY_STRING;
		String originatingCountry = CommonConstants.EMPTY_STRING;
		String redirectIndicator = CommonConstants.EMPTY_STRING;
		String originalReceiveCountry = CommonConstants.EMPTY_STRING;
		String originalSendCurrency = CommonConstants.EMPTY_STRING;
		String originalSendAmount = "0.00";
		String originalReceiveAmount = "0.00";
		MGM_ExceptionHelper exceptionHelper = new MGM_ExceptionHelper();
		try {
			//			loading and parsing the xml response
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(referenceNumberXmlResponse)));

			Element soapEnvelope = document.getDocumentElement();
			Node soapBody = soapEnvelope.getFirstChild();
			Node referenceNumber = soapBody.getFirstChild();
			if ((referenceNumber.getNodeName().equalsIgnoreCase(MGM_Constants.AC_REFERENCENUMBERRESPONSE))) {

				//Extracting the values of tags.
				Node nextChild = referenceNumber.getFirstChild();
				int loopFlag = 0;
				do {
					if (loopFlag > 0)
						nextChild = nextChild.getNextSibling();
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_DOCHECKIN)) {
						if (!(nextChild.getFirstChild() == null))
							doCheckIn = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_TIMESTAMP)) {
						if (!(nextChild.getFirstChild() == null))
							timeStamp = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_FLAGS)) {
						if (!(nextChild.getFirstChild() == null))
							flags = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_SENDERFIRSTNAME)) {
						if (!(nextChild.getFirstChild() == null))
							senderFirstName = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_SENDERMIDDLEINITIAL)) {
						if (!(nextChild.getFirstChild() == null))
							senderMiddleInitial = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_SENDERLASTNAME)) {
						if (!(nextChild.getFirstChild() == null))
							senderLastName = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_SENDERHOMEPHONE)) {
						if (!(nextChild.getFirstChild() == null))
							senderHomePhone = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_RECEIVERFIRSTNAME)) {
						if (!(nextChild.getFirstChild() == null))
							receiverFirstName = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_RECEIVERMIDDLENAME)) {
						if (!(nextChild.getFirstChild() == null))
							receiverMiddleName = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_RECEIVERLASTNAME)) {
						if (!(nextChild.getFirstChild() == null))
							receiverLastName = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_RECEIVERLASTNAME2)) {
						if (!(nextChild.getFirstChild() == null))
							receiverLastName2 = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_RECEIVERADDRESS)) {
						if (!(nextChild.getFirstChild() == null))
							receiverAddress = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_DIRECTION1)) {
						if (!(nextChild.getFirstChild() == null))
							direction1 = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_DIRECTION2)) {
						if (!(nextChild.getFirstChild() == null))
							direction2 = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_DIRECTION3)) {
						if (!(nextChild.getFirstChild() == null))
							direction3 = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_RECEIVERCOLONIA)) {
						if (!(nextChild.getFirstChild() == null))
							receiverColonia = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_RECEIVERMUNICIPIO)) {
						if (!(nextChild.getFirstChild() == null))
							receiverMunicipio = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_RECEIVERCITY)) {
						if (!(nextChild.getFirstChild() == null))
							receiverCity = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_RECEIVERSTATE)) {
						if (!(nextChild.getFirstChild() == null))
							receiverState = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_RECEIVERCOUNTRY)) {
						if (!(nextChild.getFirstChild() == null))
							receiverCountry = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_RECEIVERZIPCODE)) {
						if (!(nextChild.getFirstChild() == null))
							receiverZipCode = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_RECEIVERPHONE)) {
						if (!(nextChild.getFirstChild() == null))
							receiverPhone = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_TESTQUESTION)) {
						if (!(nextChild.getFirstChild() == null))
							testQuestion = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_TESTANSWER)) {
						if (!(nextChild.getFirstChild() == null))
							testAnswer = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_MESSAGEFIELD1)) {
						if (!(nextChild.getFirstChild() == null))
							messageField1 = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_MESSAGEFIELD2)) {
						if (!(nextChild.getFirstChild() == null))
							messageField2 = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_DELIVERYOPTION)) {
						if (!(nextChild.getFirstChild() == null))
							deliveryOption = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_TRANSACTIONSTATUS)) {
						if (!(nextChild.getFirstChild() == null))
							transactionStatus = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_DATETIMESENT)) {
						if (!(nextChild.getFirstChild() == null))
							dateTimeSent = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_RECEIVECURRENCY)) {
						if (!(nextChild.getFirstChild() == null))
							receiveCurrency = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_RECEIVEAMOUNT)) {
						if (!(nextChild.getFirstChild() == null))
							receiveAmount = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_OKFORAGENT)) {
						if (!(nextChild.getFirstChild() == null))
							okForAgent = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_ORIGINATINGCOUNTRY)) {
						if (!(nextChild.getFirstChild() == null))
							originatingCountry = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equals(MGM_Constants.AC_REDIRECTINDICATOR)) {
						if (!(nextChild.getFirstChild() == null))
							redirectIndicator = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_ORIGINALRECEIVECOUNTRY)) {
						if (!(nextChild.getFirstChild() == null))
							originalReceiveCountry = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_ORIGINALSENDCURRENCY)) {
						if (!(nextChild.getFirstChild() == null))
							originalSendCurrency = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_ORIGINALSENDAMOUNT)) {
						if (!(nextChild.getFirstChild() == null))
							originalSendAmount = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_ORIGINALRECEIVEAMOUNT)) {
						if (!(nextChild.getFirstChild() == null))
							originalReceiveAmount = nextChild.getFirstChild().getNodeValue();
					}

					loopFlag++;
				}
				while (!referenceNumber.getLastChild().equals(nextChild));

				this.setF_OUT_doCheckIn(doCheckIn);
				this.setF_OUT_timeStamp(timeStamp);
				this.setF_OUT_flags(flags);
				this.setF_OUT_senderFirstName(senderFirstName);
				this.setF_OUT_senderMiddleInitial(senderMiddleInitial);
				this.setF_OUT_senderLastName(senderLastName);
				this.setF_OUT_senderHomePhone(senderHomePhone);
				this.setF_OUT_receiverFirstName(receiverFirstName);
				this.setF_OUT_receiverMiddleName(receiverMiddleName);
				this.setF_OUT_receiverLastName(receiverLastName);
				this.setF_OUT_receiverLastName2(receiverLastName2);
				this.setF_OUT_receiverAddress(receiverAddress);
				this.setF_OUT_direction1(direction1);
				this.setF_OUT_direction2(direction2);
				this.setF_OUT_direction3(direction3);
				this.setF_OUT_receiverColonia(receiverColonia);
				this.setF_OUT_receiverMunicipio(receiverMunicipio);
				this.setF_OUT_receiverCity(receiverCity);
				this.setF_OUT_receiverState(receiverState);
				this.setF_OUT_receiverCountry(receiverCountry);
				this.setF_OUT_receiverZipCode(receiverZipCode);
				this.setF_OUT_receiverPhone(receiverPhone);
				this.setF_OUT_testQuestion(testQuestion);
				this.setF_OUT_testAnswer(testAnswer);
				this.setF_OUT_messageField1(messageField1);
				this.setF_OUT_messageField2(messageField2);
				this.setF_OUT_deliveryOption(deliveryOption);
				this.setF_OUT_transactionStatus(transactionStatus);
				this.setF_OUT_dateTimeSent(dateTimeSent);
				this.setF_OUT_receiveCurrency(receiveCurrency);
				this.setF_OUT_receiveAmount(new BigDecimal(receiveAmount));
				this.setF_OUT_okForAgent(okForAgent);
				this.setF_OUT_originatingCountry(originatingCountry);
				this.setF_OUT_redirectIndicator(redirectIndicator);
				this.setF_OUT_originalReceiveCountry(originalReceiveCountry);
				this.setF_OUT_originalSendCurrency(originalSendCurrency);
				this.setF_OUT_originalSendAmount(new BigDecimal(originalSendAmount));
				this.setF_OUT_originalReceiveAmount(new BigDecimal(originalReceiveAmount));
				logger.info("ReferenceNumber response received successfully");
			}
			else {
				try {
					logger.info("Error in Reference Number response");
					String errorCodeString = referenceNumber.getFirstChild().getNextSibling().getNextSibling()
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
