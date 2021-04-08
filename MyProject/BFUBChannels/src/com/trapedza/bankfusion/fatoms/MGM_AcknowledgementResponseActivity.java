/* ********************************************************************************
 *  Copyright(c)2007  Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 *
 * $Id: MGM_AcknowledgementResponseActivity.java,v 1.5 2008/08/12 20:14:01 vivekr Exp $
 *
 * $Log: MGM_AcknowledgementResponseActivity.java,v $
 * Revision 1.5  2008/08/12 20:14:01  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.3.4.1  2008/07/03 17:55:53  vivekr
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
 * Revision 1.3  2008/02/08 05:51:23  nileshk
 * Catching unknown MoneyGram exception
 *
 * Revision 1.2  2007/10/03 09:28:21  nileshk
 * Code added to handle exception in absence of ErrorCode from MoneyGram
 *
 * Revision 1.1  2007/09/28 12:23:46  nileshk
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
import com.trapedza.bankfusion.steps.refimpl.AbstractMGM_AcknowledgementResponseActivity;
import com.trapedza.bankfusion.steps.refimpl.IMGM_AcknowledgementResponseActivity;

/**
 * This fatom creates the BankFusion tags for Acknowledgement response.
 * @author nileshk
 *
 */
public class MGM_AcknowledgementResponseActivity extends AbstractMGM_AcknowledgementResponseActivity implements
		IMGM_AcknowledgementResponseActivity {

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
	private transient final static Log logger = LogFactory.getLog(MGM_AcknowledgementResponseActivity.class.getName());

	/**
	 * Constructor
	 * @param env
	 */
	public MGM_AcknowledgementResponseActivity(BankFusionEnvironment env) {
		super(env);
	}

	/**
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractMGM_AcknowledgementResponseActivity#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 * @param environment The BankFusion Environment
	 * @
	 */
	public void process(BankFusionEnvironment environment) {

		String acknowledgementXmlResponse = this.getF_IN_acknowledgementXmlResponse();

		String doCheckIn = CommonConstants.EMPTY_STRING;
		String timeStamp = CommonConstants.EMPTY_STRING;
		String flags = CommonConstants.EMPTY_STRING;
		MGM_ExceptionHelper exceptionHelper = new MGM_ExceptionHelper();
		try {
			//			loading and parsing the xml response
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(acknowledgementXmlResponse)));

			Element soapEnvelope = document.getDocumentElement();
			Node soapBody = soapEnvelope.getFirstChild();
			Node acknowledgeResponse = soapBody.getFirstChild();
			if ((acknowledgeResponse.getNodeName().equalsIgnoreCase(MGM_Constants.AC_ACKNOWLEDGERESPONSE))) {

				//Extracting the values of tags.
				Node nextChild = acknowledgeResponse.getFirstChild();
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
					loopFlag++;
				}
				while (!acknowledgeResponse.getLastChild().equals(nextChild));

				this.setF_OUT_doCheckIn(doCheckIn);
				this.setF_OUT_timeStamp(timeStamp);
				this.setF_OUT_flags(flags);
				logger.info("Acknowledgement received successfully");
			}
			else {
				try {
					logger.info("Error receiving Acknowledgement");
					String errorCodeString = acknowledgeResponse.getFirstChild().getNextSibling().getNextSibling()
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
