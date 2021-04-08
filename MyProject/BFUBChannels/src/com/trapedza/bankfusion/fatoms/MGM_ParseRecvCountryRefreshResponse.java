/* ***********************************************************************************
 * Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 * $Id: MGM_ParseRecvCountryRefreshResponse.java,v 1.5 2008/08/12 20:14:28 vivekr Exp $
 *
 */

package com.trapedza.bankfusion.fatoms;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.misys.ub.common.StringHelper;
import com.misys.ub.moneygram.MGM_Constants;
import com.misys.ub.moneygram.MGM_ExceptionHelper;
import com.misys.ub.moneygram.MGM_ReadProperties;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractMGM_ParseRecvCountryRefreshResponse;

public class MGM_ParseRecvCountryRefreshResponse extends AbstractMGM_ParseRecvCountryRefreshResponse {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private transient final static Log logger = LogFactory.getLog(MGM_ParseRecvCountryRefreshResponse.class.getName());

	public MGM_ParseRecvCountryRefreshResponse(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) {
		MGM_ExceptionHelper exceptionHelper = new MGM_ExceptionHelper();
		
		FileWriter outPutFile =null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(getF_IN_RecvCountryResponseXML())));
			Node rootnode = document.getDocumentElement();
			Node bodyElement = rootnode.getFirstChild();
			Node recvCountryResponseElement = bodyElement.getFirstChild();

			if (recvCountryResponseElement.getNodeName().equalsIgnoreCase(MGM_Constants.SOAPENV_FAULT)) {
				logger.info("Error in Response");

				String errorCodeString = recvCountryResponseElement.getFirstChild().getNextSibling().getNextSibling()
						.getFirstChild().getFirstChild().getFirstChild().getNodeValue();
				int errorCode = Integer.parseInt(errorCodeString);
				exceptionHelper.throwMoneyGramException(errorCode, env);
			}

			NodeList recvCountryItemList = recvCountryResponseElement.getChildNodes();
			StringHelper strHelper = new StringHelper();
			String writeString = null;

			MGM_ReadProperties readProperties = new MGM_ReadProperties();
			String path = readProperties.getDestinationPath(env);
			File file = new File(path + File.separator + MGM_Constants.RECIEVE_COUNTRY_REFRESH_FILE);

			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			outPutFile = new FileWriter(file, true);


			for (int i = 0; i < recvCountryItemList.getLength(); i++) {
				Node recvCountryItem = recvCountryItemList.item(i);
				writeString = "64,";
				if (recvCountryItem.getNodeName().equals(MGM_Constants.AC_RECEIVECOUNTRYREQUIREMENTSINFO)) {
					NodeList items = recvCountryItem.getChildNodes();
					for (int j = 0; j < items.getLength(); j++) {
						Node node = items.item(j);
						if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_RECEIVECOUNTRY)) {
							writeString = writeString + strHelper.rightPad(node.getFirstChild().getNodeValue(), " ", 3);
						}
						else if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_DELIVERYOPTION)) {
							writeString = writeString
									+ strHelper.rightPad(node.getFirstChild().getNodeValue(), " ", 10);
						}
						else if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_RECEIVERADDRESSREQUIRED)) {
							writeString = writeString + strHelper.rightPad(node.getFirstChild().getNodeValue(), " ", 5);
						}
						else if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_RECEIVER2NDLASTNAMEREQUIRED)) {
							writeString = writeString + strHelper.rightPad(node.getFirstChild().getNodeValue(), " ", 5);
						}
						else if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_QUESTIONRESTRICTED)) {
							writeString = writeString + strHelper.rightPad(node.getFirstChild().getNodeValue(), " ", 5);
						}
						else if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_QUESTIONREQUIRED)) {
							writeString = writeString + strHelper.rightPad(node.getFirstChild().getNodeValue(), " ", 5);
						}
						else if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_RECEIVEACTIVEFORAGENT)) {
							writeString = writeString + strHelper.rightPad(node.getFirstChild().getNodeValue(), " ", 5);
						}
						else {

						}
					}
					if (writeString.length() > 3) {
						try {
							writeString = strHelper.rightPad(writeString, " ", 67);
							outPutFile.write(writeString.toString() + "\r\n");
						}
						catch (Exception ex) {
							logger.error(ex.getStackTrace());
						}
					}

				}
			}
			outPutFile.write("\u001A");//appending control character

		}
		catch (ParserConfigurationException e) {
			logger.error("Parsing Error", e);
			exceptionHelper.throwMoneyGramException(014, env);
		}
		catch (SAXException e) {
			logger.error("SAXException", e);
			exceptionHelper.throwMoneyGramException(014, env);
		}
		catch (IOException e) {
			logger.error("IOException", e);
			exceptionHelper.throwMoneyGramException(014, env);
		}
		catch (NullPointerException e) {
			logger.error("NullPointerException", e);
			exceptionHelper.throwMoneyGramException(900, env);
		}
		finally{
			try {
				outPutFile.close();
			} catch (Exception e) {
			logger.error(e.getStackTrace());
			}
		}
	}
}
