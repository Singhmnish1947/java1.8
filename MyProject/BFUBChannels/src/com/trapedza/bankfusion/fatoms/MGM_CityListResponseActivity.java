/* ********************************************************************************
 *  Copyright(c)2007  Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 * 
 * $Id: MGM_CityListResponseActivity.java,v 1.6 2008/08/12 20:14:13 vivekr Exp $
 *  
 * $Log: MGM_CityListResponseActivity.java,v $
 * Revision 1.6  2008/08/12 20:14:13  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.4.4.1  2008/07/03 17:55:54  vivekr
 * Moved from Dublin CVS Head (For BF1.01 package restructuring)
 *
 * Revision 1.6  2008/06/16 15:19:22  arun
 * UB Refactoring -
 * 1. Formatted/Organized imports
 * 2. BODefinitionException, InvalidExtensionPointException and references removed
 * 3. Removed ServerManager deprecated methods/variables
 * 4. BankfusionPropertyAccess removed - changed to BankfusionPropertySupport
 * 5. Exception Handling refactoring
 * 6. General Refactoring
 *
 * Revision 1.5  2008/06/12 10:51:53  arun
 *  RIO on Head
 *
 * Revision 1.4  2008/02/08 05:52:32  nileshk
 * Catching unknown MoneyGram exception
 *
 * Revision 1.3  2007/10/17 07:38:02  nileshk
 * Code added to handle exception in absence of CityList
 *
 * Revision 1.2  2007/10/03 09:28:53  nileshk
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
import com.trapedza.bankfusion.steps.refimpl.AbstractMGM_CityListResponseActivity;
import com.trapedza.bankfusion.steps.refimpl.IMGM_CityListResponseActivity;

/**
 * This fatom takes the CityList xml response from MoneyGram and generates the required BankFusion tag. 
 * @author nileshk
 */

public class MGM_CityListResponseActivity extends AbstractMGM_CityListResponseActivity implements
		IMGM_CityListResponseActivity {

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
	private transient final static Log logger = LogFactory.getLog(MGM_CityListResponseActivity.class.getName());

	/**
	 * Constructor
	 * @param env
	 */
	public MGM_CityListResponseActivity(BankFusionEnvironment env) {
		super(env);
	}

	/**
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractMGM_CityListResponseActivity#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 * @param environment The BankFusion Environment
	 * @
	 */
	public void process(BankFusionEnvironment environment) {
		MGM_ExceptionHelper exceptionHelper = new MGM_ExceptionHelper();
		String cityListXmlResponse = this.getF_IN_CityListXmlResponse();

		String doCheckIn = CommonConstants.EMPTY_STRING;
		String timeStamp = CommonConstants.EMPTY_STRING;
		String flags = CommonConstants.EMPTY_STRING;
		String cityList = CommonConstants.EMPTY_STRING;
		try {
			//			loading and parsing the xml response
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(cityListXmlResponse)));

			Element soapEnvelope = document.getDocumentElement();
			Node soapBody = soapEnvelope.getFirstChild();
			Node cityLookUpResponse = soapBody.getFirstChild();
			if ((cityLookUpResponse.getNodeName().equalsIgnoreCase(MGM_Constants.AC_CITYLISTRESPONSE))) {

				//		Extracting the values of tags.
				Node firstChild = cityLookUpResponse.getFirstChild();
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

				//		Extracting the values of cityList child tags.
				do {

					nextChild = nextChild.getNextSibling();
					if (nextChild == null) {
						exceptionHelper.throwMoneyGramException(9267, environment);
					}
					
					Node nextNode = nextChild.getFirstChild();
					StringBuffer cityName = new StringBuffer(nextNode.getNodeValue());
					char c = ' ';
					while (cityName.length() < 20) {
						cityName.append(c);
					}

					cityList = cityList + cityName;

				}
				while (!cityLookUpResponse.getLastChild().equals(nextChild));
				//		setting the BankFusion tag values.
				this.setF_OUT_cityList(cityList);
				this.setF_OUT_doCheckIn(doCheckIn);
				this.setF_OUT_flags(flags);
				this.setF_OUT_timeStamp(timeStamp);
				logger.info("CityList retreived successfully");
			}
			else {
				try {
					logger.info("Error in CityList inquiry response");
					String errorCodeString = cityLookUpResponse.getFirstChild().getNextSibling().getNextSibling()
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
