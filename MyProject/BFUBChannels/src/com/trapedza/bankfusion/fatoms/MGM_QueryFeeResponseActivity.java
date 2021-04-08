/* ********************************************************************************
 *  Copyright(c)2007  Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 *
 * $Id: MGM_QueryFeeResponseActivity.java,v 1.5 2008/08/12 20:14:03 vivekr Exp $
 *
 * $Log: MGM_QueryFeeResponseActivity.java,v $
 * Revision 1.5  2008/08/12 20:14:03  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.3.4.1  2008/07/03 17:55:55  vivekr
 * Moved from Dublin CVS Head (For BF1.01 package restructuring)
 *
 * Revision 1.5  2008/06/16 15:24:07  arun
 * UB Refactoring -
 * 1. Formatted/Organized imports
 * 2. BODefinitionException, InvalidExtensionPointException and references removed
 * 3. Removed ServerManager deprecated methods/variables
 * 4. BankfusionPropertyAccess removed - changed to BankfusionPropertySupport
 * 5. Exception Handling refactoring
 * 6. General Refactoring
 *
 * Revision 1.4  2008/06/12 10:51:54  arun
 *  RIO on Head
 *
 * Revision 1.3  2008/02/08 05:54:26  nileshk
 * Catching unknown MoneyGram exception
 *
 * Revision 1.2  2007/10/03 06:30:28  nileshk
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
import com.trapedza.bankfusion.steps.refimpl.AbstractMGM_QueryFeeResponseActivity;
import com.trapedza.bankfusion.steps.refimpl.IMGM_QueryFeeResponseActivity;

/**
 * This fatom is used to receive the QueryFee xml response and create the BankFusion tags.
 * @author nileshk
 *
 */
public class MGM_QueryFeeResponseActivity extends AbstractMGM_QueryFeeResponseActivity implements
		IMGM_QueryFeeResponseActivity {

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
	private transient final static Log logger = LogFactory.getLog(MGM_QueryFeeResponseActivity.class.getName());

	/**
	 * Constructor
	 * @param env
	 */
	public MGM_QueryFeeResponseActivity(BankFusionEnvironment env) {
		super(env);
	}

	/**
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractMGM_QueryFeeResponseActivity#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 * @param environment The BankFusion Environment
	 * @
	 */
	public void process(BankFusionEnvironment environment) {

		String queryFeeXmlResponse = this.getF_IN_queryFeeXmlResponse();

		String doCheckIn = CommonConstants.EMPTY_STRING;
		String timeStamp = CommonConstants.EMPTY_STRING;
		String flags = CommonConstants.EMPTY_STRING;
		String feeAmount = CommonConstants.EMPTY_STRING;
		String sendAmount = CommonConstants.EMPTY_STRING;
		String sendCurrency = CommonConstants.EMPTY_STRING;
		String validReceiveAmount = CommonConstants.EMPTY_STRING;
		String validReceiveCurrency = CommonConstants.EMPTY_STRING;
		String validExchangeRate = CommonConstants.EMPTY_STRING;
		String validIndicator = CommonConstants.EMPTY_STRING;
		String estimatedReceiveAmount = CommonConstants.EMPTY_STRING;
		String estimatedExchangeRate = CommonConstants.EMPTY_STRING;
		String totalAmount = CommonConstants.EMPTY_STRING;
		String receiveCountry = CommonConstants.EMPTY_STRING;
		String deliveryOption = CommonConstants.EMPTY_STRING;
		String receiveAmountAltered = CommonConstants.EMPTY_STRING;
		MGM_ExceptionHelper exceptionHelper = new MGM_ExceptionHelper();
		try {
			//			loading and parsing the xml response
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(queryFeeXmlResponse)));

			Element soapEnvelope = document.getDocumentElement();
			Node soapBody = soapEnvelope.getFirstChild();
			Node queryFee = soapBody.getFirstChild();
			if ((queryFee.getNodeName().equalsIgnoreCase(MGM_Constants.AC_FEELOOKUPRESPONSE))) {

				//			Extracting the values of tags.
				Node nextChild = queryFee.getFirstChild();
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
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_FEEAMOUNT)) {
						if (!(nextChild.getFirstChild() == null))
							feeAmount = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_SENDAMOUNT)) {
						if (!(nextChild.getFirstChild() == null))
							sendAmount = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_SENDCURRENCY)) {
						if (!(nextChild.getFirstChild() == null))
							sendCurrency = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_VALIDRECEIVEAMOUNT)) {
						if (!(nextChild.getFirstChild() == null))
							validReceiveAmount = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_VALIDRECEIVECURRENCY)) {
						if (!(nextChild.getFirstChild() == null))
							validReceiveCurrency = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_VALIDEXCHANGERATE)) {
						if (!(nextChild.getFirstChild() == null))
							validExchangeRate = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_VALIDINDICATOR)) {
						if (!(nextChild.getFirstChild() == null))
							validIndicator = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_ESTIMATEDRECEIVEAMOUNT)) {
						if (!(nextChild.getFirstChild() == null))
							estimatedReceiveAmount = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_ESTIMATEDEXCHANGERATE)) {
						if (!(nextChild.getFirstChild() == null))
							estimatedExchangeRate = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_TOTALAMOUNT)) {
						if (!(nextChild.getFirstChild() == null))
							totalAmount = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_RECEIVECOUNTRY)) {
						if (!(nextChild.getFirstChild() == null))
							receiveCountry = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_DELIVERYOPTION)) {
						if (!(nextChild.getFirstChild() == null))
							deliveryOption = nextChild.getFirstChild().getNodeValue();
					}
					if (nextChild.getNodeName().equalsIgnoreCase(MGM_Constants.AC_RECEIVEAMOUNTALTERED)) {
						if (!(nextChild.getFirstChild() == null))
							receiveAmountAltered = nextChild.getFirstChild().getNodeValue();
					}
					loopFlag++;
				}
				while (!queryFee.getLastChild().equals(nextChild));

				this.setF_OUT_doCheckIn(doCheckIn);
				this.setF_OUT_timeStamp(timeStamp);
				this.setF_OUT_flags(flags);
				this.setF_OUT_feeAmount(new BigDecimal(feeAmount));
				this.setF_OUT_sendAmount(new BigDecimal(sendAmount));
				this.setF_OUT_sendCurrency(sendCurrency);
				this.setF_OUT_validReceiveAmount(new BigDecimal(validReceiveAmount));
				this.setF_OUT_validReceiveCurrency(validReceiveCurrency);
				this.setF_OUT_validExchangeRate(new BigDecimal(validExchangeRate));
				this.setF_OUT_validIndicator(validIndicator);
				this.setF_OUT_estimatedReceiveAmount(new BigDecimal(estimatedReceiveAmount));
				this.setF_OUT_estimatedExchangeRate(new BigDecimal(estimatedExchangeRate));
				this.setF_OUT_totalAmount(new BigDecimal(totalAmount));
				this.setF_OUT_receiveCountry(receiveCountry);
				this.setF_OUT_deliveryOption(deliveryOption);
				this.setF_OUT_receiveAmountAltered(receiveAmountAltered);
				logger.info("QueryFee response received successfully");
			}
			else {
				try {
					logger.info("Error in QueryFee response");
					String errorCodeString = queryFee.getFirstChild().getNextSibling().getNextSibling().getFirstChild()
							.getFirstChild().getFirstChild().getNodeValue();
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
