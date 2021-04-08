/* ***********************************************************************************
 * Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 * $Id: MGM_ParseProfileRefreshResponse.java,v 1.5 2008/08/12 20:14:23 vivekr Exp $
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
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractMGM_ParseProfileRefreshResponse;

public class MGM_ParseProfileRefreshResponse extends AbstractMGM_ParseProfileRefreshResponse {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private transient final static Log logger = LogFactory.getLog(MGM_ParseProfileRefreshResponse.class.getName());

	public MGM_ParseProfileRefreshResponse(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	public void process(BankFusionEnvironment env) {
		MGM_ExceptionHelper exceptionHelper = new MGM_ExceptionHelper();
		FileWriter outPutFile = null;
		try {
			int offset = 0;
			int fieldLength = 0;
			StringBuffer writeString = new StringBuffer(512);
			StringHelper stringHelper = new StringHelper();
			//														Offset-Len-Type-ID
			String AGENT_ID = "        "; // 0005 008 99 0
			String PRODUCT_STATUS = " "; // 0013 001 XX 5
			String PRODUCT_TYPE = "  "; // 0014 002 XX 5
			String DOCUMENT_NAME = "              "; // 0016 014 XX 5
			String MAX_AMOUNT_PER_ITEM = "               "; // 0030 015 99 5
			String FRAUD_LIMIT_TEST_QUESTION = "               "; // 0045 015 99 5
			String FRAUD_LIMIT_LEGAL_ID = "               "; // 0060 015 99 5
			String FRAUD_LIMIT_PHOTO_ID = "               "; // 0075 015 99 5
			String FRAUD_LIMIT_CASH_WARNING = "               "; // 0090 015 99 5
			String FRAUD_LIMIT_THIRD_PARTY_ID = "               "; // 0105 015 99 5
			String RECV_PRODUCT_STATUS = " "; // 0120 001 XX 6
			String DOCUMENT_NUMBER = " "; // 0121 001 XX 6
			String RECV_PRODUCT_TYPE = " "; // 0122 001 XX 6
			String RECV_DOCUMENT_NAME = "                 "; // 0123 017 XX 6
			String RECV_MAX_AMOUNT_PER_ITEM = "               "; // 0140 015 99 6
			String FRAUD_LIMIT_ADDRESS = "               "; // 0155 015 99 6
			String FRAUD_LIMIT_RECEIVER = "               "; // 0170 015 99 6
			String RECV_FRAUD_LIMIT_LEGAL_ID = "               "; // 0185 015 99 6
			String RECV_FRAUD_LIMIT_THIRD_PARTY_ID = "               ";// 0200 015 99 6
			String AGENT_NAME =  "                                        "; // 0215 040 XX 0

			//			set length of the record to 512 chars
			fieldLength = 4;
			writeString.insert(offset, "512,");
			offset += fieldLength;

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(getF_IN_ProfileResponseXML())));
			Node rootnode = document.getDocumentElement();
			Node bodyElement = rootnode.getFirstChild();
			Node profileResponseElement = bodyElement.getFirstChild();

			if (profileResponseElement.getNodeName().equalsIgnoreCase(MGM_Constants.SOAPENV_FAULT)) {
				logger.info("Error in Response");

				String errorCodeString = profileResponseElement.getFirstChild().getNextSibling().getNextSibling()
						.getFirstChild().getFirstChild().getFirstChild().getNodeValue();
				int errorCode = Integer.parseInt(errorCodeString);
				exceptionHelper.throwMoneyGramException(errorCode, env);
			}

			NodeList profileItemList = profileResponseElement.getChildNodes();

			for (int i = 0; i < profileItemList.getLength(); i++) {
				/*HashMap sendDetails = new HashMap();
				HashMap receiveDetails = new HashMap();*/
				String key = CommonConstants.EMPTY_STRING;
				String index = CommonConstants.EMPTY_STRING;
				String value = CommonConstants.EMPTY_STRING;
				String productId = CommonConstants.EMPTY_STRING;
				Node profileItem = profileItemList.item(i);

				if (profileItem.getNodeName().equals(MGM_Constants.AC_PROFILEITEM)) {
					NodeList items = profileItem.getChildNodes();
					for (int j = 0; j < items.getLength(); j++) {
						Node node = items.item(j);
						if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_INDEX)) {
							index = node.getFirstChild().getNodeValue();
						}
						else if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_KEY)) {
							key = node.getFirstChild().getNodeValue();
						}
						else if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_VALUE)) {
							value = node.getFirstChild().getNodeValue();
						}
						else {

						}
					}
					if (key.equalsIgnoreCase("AGENT_ID")) {
						AGENT_ID = stringHelper.leftPad(value, "0", 8);
					}
					else if (key.equalsIgnoreCase("AGENT_NAME")) {
						AGENT_NAME = stringHelper.rightPad(value, " ", 40);
					}
				}
				else if (profileItem.getNodeName().equals(MGM_Constants.AC_PRODUCTPROFILEITEM)) {
					NodeList items = profileItem.getChildNodes();
					for (int j = 0; j < items.getLength(); j++) {
						Node node = items.item(j);
						if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_INDEX)) {
							index = node.getFirstChild().getNodeValue();
						}
						else if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_KEY)) {
							key = node.getFirstChild().getNodeValue();
						}
						else if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_VALUE)) {
							value = node.getFirstChild().getNodeValue();
						}
						else if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_PRODUCTID)) {
							productId = node.getFirstChild().getNodeValue();
						}
					}

					if (key.equalsIgnoreCase("PRODUCT_STATUS") && productId.equalsIgnoreCase("5")) {
						PRODUCT_STATUS = stringHelper.rightPad(value, " ", 1);
					}
					else if (key.equalsIgnoreCase("PRODUCT_STATUS") && productId.equalsIgnoreCase("6")) {
						RECV_PRODUCT_STATUS = stringHelper.rightPad(value, " ", 1);
					}
					else if (key.equalsIgnoreCase("PRODUCT_TYPE") && productId.equalsIgnoreCase("5")) {
						PRODUCT_TYPE = stringHelper.rightPad(value, " ", 2);
					}
					else if (key.equalsIgnoreCase("PRODUCT_TYPE") && productId.equalsIgnoreCase("6")) {
						RECV_PRODUCT_TYPE = stringHelper.rightPad(value, " ", 1);
					}
					else if (key.equalsIgnoreCase("DOCUMENT_NAME") && productId.equalsIgnoreCase("5")) {
						DOCUMENT_NAME = stringHelper.rightPad(value, " ", 14);
					}
					else if (key.equalsIgnoreCase("DOCUMENT_NAME") && productId.equalsIgnoreCase("6")) {
						RECV_DOCUMENT_NAME = stringHelper.rightPad(value, " ", 17);
					}
					else if (key.equalsIgnoreCase("MAX_AMOUNT_PER_ITEM") && productId.equalsIgnoreCase("5")) {
						MAX_AMOUNT_PER_ITEM = stringHelper.leftPad(value, "0", 15);
					}
					else if (key.equalsIgnoreCase("MAX_AMOUNT_PER_ITEM") && productId.equalsIgnoreCase("6")) {
						RECV_MAX_AMOUNT_PER_ITEM = stringHelper.leftPad(value, "0", 15);
					}
					else if (key.equalsIgnoreCase("FRAUD_LIMIT_LEGAL_ID") && productId.equalsIgnoreCase("5")) {
						FRAUD_LIMIT_LEGAL_ID = stringHelper.leftPad(value, "0", 15);
					}
					else if (key.equalsIgnoreCase("FRAUD_LIMIT_LEGAL_ID") && productId.equalsIgnoreCase("6")) {
						RECV_FRAUD_LIMIT_LEGAL_ID = stringHelper.leftPad(value, "0", 15);
					}
					else if (key.equalsIgnoreCase("FRAUD_LIMIT_THIRD_PARTY_ID") && productId.equalsIgnoreCase("5")) {
						FRAUD_LIMIT_THIRD_PARTY_ID = stringHelper.leftPad(value, "0", 15);
					}
					else if (key.equalsIgnoreCase("FRAUD_LIMIT_THIRD_PARTY_ID") && productId.equalsIgnoreCase("6")) {
						RECV_FRAUD_LIMIT_THIRD_PARTY_ID = stringHelper.leftPad(value, "0", 15);
					}
					else if (key.equalsIgnoreCase("FRAUD_LIMIT_CASH_WARNING")) {
						FRAUD_LIMIT_CASH_WARNING = stringHelper.leftPad(value, "0", 15);
					}
					else if (key.equalsIgnoreCase("FRAUD_LIMIT_TEST_QUESTION")) {
						FRAUD_LIMIT_TEST_QUESTION = stringHelper.leftPad(value, "0", 15);
					}
					else if (key.equalsIgnoreCase("FRAUD_LIMIT_PHOTO_ID")) {
						FRAUD_LIMIT_PHOTO_ID = stringHelper.leftPad(value, "0", 15);
					}
					else if (key.equalsIgnoreCase("DOCUMENT_NUMBER")) {
						DOCUMENT_NUMBER = stringHelper.leftPad(value, "0", 1);
					}
					else if (key.equalsIgnoreCase("FRAUD_LIMIT_ADDRESS")) {
						FRAUD_LIMIT_ADDRESS = stringHelper.leftPad(value, "0", 15);
					}
					else if (key.equalsIgnoreCase("FRAUD_LIMIT_RECEIVER")) {
						FRAUD_LIMIT_RECEIVER = stringHelper.leftPad(value, "0", 15);
					}
					else {

					}
				}
			}

			writeString.append(stringHelper.rightPad(AGENT_ID + PRODUCT_STATUS + PRODUCT_TYPE + DOCUMENT_NAME
					+ MAX_AMOUNT_PER_ITEM + FRAUD_LIMIT_TEST_QUESTION + FRAUD_LIMIT_LEGAL_ID + FRAUD_LIMIT_PHOTO_ID
					+ FRAUD_LIMIT_CASH_WARNING + FRAUD_LIMIT_THIRD_PARTY_ID + RECV_PRODUCT_STATUS + DOCUMENT_NUMBER
					+ RECV_PRODUCT_TYPE + RECV_DOCUMENT_NAME + RECV_MAX_AMOUNT_PER_ITEM + FRAUD_LIMIT_ADDRESS
					+ FRAUD_LIMIT_RECEIVER + RECV_FRAUD_LIMIT_LEGAL_ID + RECV_FRAUD_LIMIT_THIRD_PARTY_ID + AGENT_NAME,
					" ", 512));
			try {
				MGM_ReadProperties readProperties = new MGM_ReadProperties();
				String path = readProperties.getDestinationPath(env);
				String currentProfileFile = MGM_Constants.CURRENT_PROFILE_REFRESH_FILE_NAME + "." + getF_IN_agentBranch();//Ex:- 'bcurpro.1000', 1000 is agent branch.
				//FileWriter outPutFile = new FileWriter(FileSystemView.getFileSystemView().getRoots()[0] + File.separator + MGM_Constants.CURRENT_PROFILE_REFRESH_FILE);
				outPutFile = new FileWriter(path + File.separator + currentProfileFile);
				outPutFile.write(writeString.toString() + "\r\n");
				outPutFile.write("\u001A");//appending control character
			}
			catch (Exception ex) {
				logger.error(ex);
			}
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
		}finally{
			if(outPutFile!=null)
			{	
			try{
				
				outPutFile.close();
			}catch(Exception e){
				logger.error(e);
			}
			}
		}
	}
}
