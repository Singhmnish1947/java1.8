/* ********************************************************************************
 *  Copyright(c)2007  Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 *
 * $Id: MGM_SendMoneyGramResponseActivity.java,v 1.5 2008/08/12 20:14:26 vivekr Exp $
 *
 * $Log: MGM_SendMoneyGramResponseActivity.java,v $
 * Revision 1.5  2008/08/12 20:14:26  vivekr
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
 * Revision 1.3  2008/02/08 05:55:16  nileshk
 * Catching unknown MoneyGram exception
 *
 * Revision 1.2  2007/10/03 09:30:24  nileshk
 * Code added to handle exception in absence of ErrorCode from MoneyGram
 *
 * Revision 1.1  2007/09/30 10:15:16  nileshk
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
import com.trapedza.bankfusion.steps.refimpl.AbstractMGM_SendMoneyGramResponseActivity;
import com.trapedza.bankfusion.steps.refimpl.IMGM_SendMoneyGramResponseActivity;

/**
 * This fatom is used to receive the MoneyGram xml response and create the BankFusion tags.
 * @author nileshk
 *
 */
public class MGM_SendMoneyGramResponseActivity extends AbstractMGM_SendMoneyGramResponseActivity implements
		IMGM_SendMoneyGramResponseActivity {

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
	private transient final static Log logger = LogFactory.getLog(MGM_SendMoneyGramResponseActivity.class.getName());

	/**
	 * Constructor
	 * @param env
	 */
	public MGM_SendMoneyGramResponseActivity(BankFusionEnvironment env) {
		super(env);
	}

	/**
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractMGM_SendMoneyGramResponseActivity#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 * @param environment The BankFusion Environment
	 * @
	 */
	public void process(BankFusionEnvironment environment) {

		String sendMoneyGramXmlResponse = this.getF_IN_sendMoneyGramXmlResponse();

		String doCheckIn = CommonConstants.EMPTY_STRING;
		String timeStamp = CommonConstants.EMPTY_STRING;
		String flags = CommonConstants.EMPTY_STRING;
		String referenceNumber = CommonConstants.EMPTY_STRING;
		String freePhoneCallPIN = CommonConstants.EMPTY_STRING;
		String sendAmount = CommonConstants.EMPTY_STRING;
		String sendCurrency = CommonConstants.EMPTY_STRING;
		String receiveAmount = CommonConstants.EMPTY_STRING;
		String receiveCurrency = CommonConstants.EMPTY_STRING;
		String validIndicator = CommonConstants.EMPTY_STRING;
		String feeAmount = CommonConstants.EMPTY_STRING;
		String totalSendAmount = CommonConstants.EMPTY_STRING;
		String exchangeRateApplied = CommonConstants.EMPTY_STRING;
		String productType = CommonConstants.EMPTY_STRING;
		String token = CommonConstants.EMPTY_STRING;
		String bancomerConfirmationNumber = "0";
		String transactionDateTime = CommonConstants.EMPTY_STRING;
		String freqCustCardNumber = CommonConstants.EMPTY_STRING;
		String nonDiscountedFee = CommonConstants.EMPTY_STRING;
		String tollFreePhoneNumber = CommonConstants.EMPTY_STRING;
		String payoutCurrency = CommonConstants.EMPTY_STRING;
		MGM_ExceptionHelper exceptionHelper = new MGM_ExceptionHelper();
		try {
			//			loading and parsing the xml response
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(sendMoneyGramXmlResponse)));

			Element soapEnvelope = document.getDocumentElement();
			Node soapBody = soapEnvelope.getFirstChild();
			Node sendMoneyGram = soapBody.getFirstChild();
			if ((sendMoneyGram.getNodeName().equalsIgnoreCase(MGM_Constants.AC_MONEYGRAMSENDRESPONSE))) {

				//			Extracting the values of tags.
				Node nextChild = sendMoneyGram.getFirstChild();
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
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_REFERENCENUMBER)) {
						if (!(nextChild.getFirstChild() == null))
							referenceNumber = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_BANCOMERCONFIRMATIONNUMBER)) {
						if (!(nextChild.getFirstChild() == null))
							bancomerConfirmationNumber = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_FREEPHONECALLPIN)) {
						if (!(nextChild.getFirstChild() == null))
							freePhoneCallPIN = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_EXCHANGERATEAPPLIED)) {
						if (!(nextChild.getFirstChild() == null))
							exchangeRateApplied = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_PRODUCTTYPE)) {
						if (!(nextChild.getFirstChild() == null))
							productType = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_TOKEN)) {
						if (!(nextChild.getFirstChild() == null))
							token = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_SENDAMOUNT)) {
						if (!(nextChild.getFirstChild() == null))
							sendAmount = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_SENDCURRENCY)) {
						if (!(nextChild.getFirstChild() == null))
							sendCurrency = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_RECEIVEAMOUNT)) {
						if (!(nextChild.getFirstChild() == null))
							receiveAmount = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_RECEIVECURRENCY)) {
						if (!(nextChild.getFirstChild() == null))
							receiveCurrency = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_VALIDINDICATOR)) {
						if (!(nextChild.getFirstChild() == null))
							validIndicator = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_FEEAMOUNT)) {
						if (!(nextChild.getFirstChild() == null))
							feeAmount = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_TOTALSENDAMOUNT)) {
						if (!(nextChild.getFirstChild() == null))
							totalSendAmount = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_TRANSACTIONDATETIME)) {
						if (!(nextChild.getFirstChild() == null))
							transactionDateTime = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_FREQCUSTCARDNUMBER)) {
						if (!(nextChild.getFirstChild() == null))
							freqCustCardNumber = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_NONDISCOUNTEDFEE)) {
						if (!(nextChild.getFirstChild() == null))
							nonDiscountedFee = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_TOLLFREEPHONENUMBER)) {
						if (!(nextChild.getFirstChild() == null))
							tollFreePhoneNumber = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_PAYOUTCURRENCY)) {
						if (!(nextChild.getFirstChild() == null))
							payoutCurrency = nextChild.getFirstChild().getNodeValue();
					}
					loopFlag++;
				}
				while (!sendMoneyGram.getLastChild().equals(nextChild));

				this.setF_OUT_doCheckIn(doCheckIn);
				this.setF_OUT_timeStamp(timeStamp);
				this.setF_OUT_flags(flags);
				this.setF_OUT_referenceNumber(referenceNumber);
				this.setF_OUT_freePhoneCallPIN(freePhoneCallPIN);
				this.setF_OUT_sendAmount(new BigDecimal(sendAmount));
				this.setF_OUT_sendCurrency(sendCurrency);
				this.setF_OUT_receiveAmount(new BigDecimal(receiveAmount));
				this.setF_OUT_receiveCurrency(receiveCurrency);
				this.setF_OUT_validIndicator(validIndicator);
				this.setF_OUT_feeAmount(new BigDecimal(feeAmount));
				this.setF_OUT_totalSendAmount(new BigDecimal(totalSendAmount));
				this.setF_OUT_exchangeRateApplied(new BigDecimal(exchangeRateApplied));
				this.setF_OUT_productType(productType);
				this.setF_OUT_token(token);
				this.setF_OUT_bancomerConfirmationNumber(new Integer(bancomerConfirmationNumber));
				this.setF_OUT_transactionDateTime(transactionDateTime);
				this.setF_OUT_freqCustCardNumber(freqCustCardNumber);
				this.setF_OUT_nonDiscountedFee(new BigDecimal(nonDiscountedFee));
				this.setF_OUT_tollFreePhoneNumber(tollFreePhoneNumber);
				this.setF_OUT_payoutCurrency(payoutCurrency);
				logger.info("SendMoneyGram response received successfully");
			}
			else {
				try {
					logger.info("Error in SendMoneyGram response");
					String errorCodeString = sendMoneyGram.getFirstChild().getNextSibling().getNextSibling()
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
