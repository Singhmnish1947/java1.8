/* ***********************************************************************************
 * Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 * $Id: MGM_ParseCodeTableRefreshResponse.java,v 1.6 2008/08/12 20:14:26 vivekr Exp $
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
import com.trapedza.bankfusion.steps.refimpl.AbstractMGM_ParseCodeTableRefreshResponse;

public class MGM_ParseCodeTableRefreshResponse extends AbstractMGM_ParseCodeTableRefreshResponse {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	private transient final static Log logger = LogFactory.getLog(MGM_ParseCodeTableRefreshResponse.class.getName());

	public MGM_ParseCodeTableRefreshResponse(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	public void process(BankFusionEnvironment env) {
		FileWriter bcodccuFileWriter = null;
		FileWriter bcodcouFileWriter = null;
		FileWriter bcodcurFileWriter = null;
		FileWriter bcodstatFileWriter = null;
		
		MGM_ExceptionHelper exceptionHelper = new MGM_ExceptionHelper();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(getF_IN_CodeTableResponseXML())));
			Node rootnode = document.getDocumentElement();
			Node bodyElement = rootnode.getFirstChild();
			Node codeTableResponseElement = bodyElement.getFirstChild();

			if (codeTableResponseElement.getNodeName().equalsIgnoreCase(MGM_Constants.SOAPENV_FAULT)) {
				logger.info("Error in Response");

				String errorCodeString = codeTableResponseElement.getFirstChild().getNextSibling().getNextSibling()
						.getFirstChild().getFirstChild().getFirstChild().getNodeValue();
				int errorCode = Integer.parseInt(errorCodeString);
				exceptionHelper.throwMoneyGramException(errorCode, env);
			}

			NodeList codeTableItemList = codeTableResponseElement.getChildNodes();
			StringHelper strHelper = new StringHelper();
			String writeString = null;
			MGM_ReadProperties readProperties = new MGM_ReadProperties();
			String path = readProperties.getDestinationPath(env);

			//			String outPutPath = "D:" + File.separator + "breccou.sav";
			//			File bcodccuFile = new File(FileSystemView.getFileSystemView().getRoots()[0] + File.separator + MGM_Constants.CODE_COUNTRY_CURR_REFRESH_FILE);
			File bcodccuFile = new File(path + File.separator + MGM_Constants.CODE_COUNTRY_CURR_REFRESH_FILE);
			if (bcodccuFile.exists()) {
				bcodccuFile.delete();
			}
			bcodccuFile.createNewFile();
			 bcodccuFileWriter = new FileWriter(bcodccuFile, true);

			//			File bcodcouFile = new File(FileSystemView.getFileSystemView().getRoots()[0] + File.separator + MGM_Constants.CODE_COUNTRY_REFRESH_FILE);
			File bcodcouFile = new File(path + File.separator + MGM_Constants.CODE_COUNTRY_REFRESH_FILE);
			if (bcodcouFile.exists()) {
				bcodcouFile.delete();
			}
			bcodcouFile.createNewFile();
			 bcodcouFileWriter = new FileWriter(bcodcouFile, true);
			 bcodcouFileWriter.close();

			//			File bcodcurFile = new File(FileSystemView.getFileSystemView().getRoots()[0] + File.separator + MGM_Constants.CODE_CURRENCY_REFRESH_FILE);
			File bcodcurFile = new File(path + File.separator + MGM_Constants.CODE_CURRENCY_REFRESH_FILE);
			if (bcodcurFile.exists()) {
				bcodcurFile.delete();
			}
			bcodcurFile.createNewFile();
			 bcodcurFileWriter = new FileWriter(bcodcurFile, true);
			 bcodcurFileWriter.close();

			//			File bcodstatFile = new File(FileSystemView.getFileSystemView().getRoots()[0] + File.separator + MGM_Constants.CODE_STATE_REFRESH_FILE);
			File bcodstatFile = new File(path + File.separator + MGM_Constants.CODE_STATE_REFRESH_FILE);
			if (bcodstatFile.exists()) {
				bcodstatFile.delete();
			}
			bcodstatFile.createNewFile();
			 bcodstatFileWriter = new FileWriter(bcodstatFile, true);
			 bcodstatFileWriter.close();

			/*File file = new File(outPutPath);
			file.mkdirs();*/

			for (int i = 0; i < codeTableItemList.getLength(); i++) {
				Node recvCountryItem = codeTableItemList.item(i);
				writeString = "64,";

				//				********** 1
				if (recvCountryItem.getNodeName().equals(MGM_Constants.AC_STATEPROVINCEINFO)) {
					NodeList items = recvCountryItem.getChildNodes();
					for (int j = 0; j < items.getLength(); j++) {
						Node node = items.item(j);

						if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_COUNTRYCODE)) {
							writeString = writeString + strHelper.rightPad(node.getFirstChild().getNodeValue(), " ", 3);
						}
						else if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_STATEPROVINCECODE)) {
							writeString = writeString + strHelper.rightPad(node.getFirstChild().getNodeValue(), " ", 2);
						}
						else if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_STATEPROVINCENAME)) {
							writeString = writeString
									+ strHelper.rightPad(node.getFirstChild().getNodeValue(), " ", 40);
						}
						else {

						}
					}
					if (writeString.length() > 3) {
						try {
							writeString = strHelper.rightPad(writeString, " ", 67);
							bcodstatFileWriter.write(writeString.toString() + "\r\n");
						}
						catch (Exception ex) {
							logger.error(ex);
						}
					}

				}

				//				********** 2
				else if (recvCountryItem.getNodeName().equals(MGM_Constants.AC_COUNTRYINFO)) {
					NodeList items = recvCountryItem.getChildNodes();

					for (int j = 0; j < items.getLength(); j++) {
						Node node = items.item(j);

						if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_COUNTRYCODE)) {
							writeString = writeString + strHelper.rightPad(node.getFirstChild().getNodeValue(), " ", 3);
						}
						else if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_COUNTRYNAME)) {
							writeString = writeString
									+ strHelper.rightPad(node.getFirstChild().getNodeValue(), " ", 40);
						}
						else if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_COUNTRYLEGACYCODE)) {
							writeString = writeString + strHelper.rightPad(node.getFirstChild().getNodeValue(), " ", 2);
						}
						else if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_SENDACTIVE)) {
							writeString = writeString + strHelper.rightPad(node.getFirstChild().getNodeValue(), " ", 5);
						}
						else if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_RECEIVEACTIVE)) {
							writeString = writeString + strHelper.rightPad(node.getFirstChild().getNodeValue(), " ", 5);
						}
						else {

						}
					}
					if (writeString.length() > 3) {
						try {
							writeString = strHelper.rightPad(writeString, " ", 67);
							bcodcouFileWriter.write(writeString.toString() + "\r\n");
						}
						catch (Exception ex) {
							logger.error(ex);
						}
					}

				}

				//				********** 3
				else if (recvCountryItem.getNodeName().equals(MGM_Constants.AC_CURRENCYINFO)) {
					NodeList items = recvCountryItem.getChildNodes();

					for (int j = 0; j < items.getLength(); j++) {
						Node node = items.item(j);

						if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_CURRENCYCODE)) {
							writeString = writeString + strHelper.rightPad(node.getFirstChild().getNodeValue(), " ", 3);
						}
						else if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_CURRENCYNAME)) {
							writeString = writeString
									+ strHelper.rightPad(node.getFirstChild().getNodeValue(), " ", 40);
						}
						else if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_CURRENCYPRECISION)) {
							writeString = writeString + strHelper.rightPad(node.getFirstChild().getNodeValue(), " ", 1);
						}
						else {

						}
					}
					if (writeString.length() > 3) {
						try {
							writeString = strHelper.rightPad(writeString, " ", 67);
							bcodcurFileWriter.write(writeString.toString() + "\r\n");
						}
						catch (Exception ex) {
							logger.error(ex);
						}
					}

				}

				//				********** 4
				else if (recvCountryItem.getNodeName().equals(MGM_Constants.AC_COUNTRYCURRENCYINFO)) {
					NodeList items = recvCountryItem.getChildNodes();

					for (int j = 0; j < items.getLength(); j++) {
						Node node = items.item(j);

						if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_COUNTRYCODE)) {
							writeString = writeString + strHelper.rightPad(node.getFirstChild().getNodeValue(), " ", 3);
						}
						else if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_TRANSACTIONCURRENCY)) {
							writeString = writeString + strHelper.rightPad(node.getFirstChild().getNodeValue(), " ", 3);
						}
						else if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_PAYOUTCURRENCY)) {
							writeString = writeString + strHelper.rightPad(node.getFirstChild().getNodeValue(), " ", 3);
						}
						else if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_INDICATIVERATEAVAILABLE)) {
							writeString = writeString + strHelper.rightPad(node.getFirstChild().getNodeValue(), " ", 5);
						}
						else if (node.getNodeName().equalsIgnoreCase(MGM_Constants.AC_DELIVERYOPTION)) {
							writeString = writeString
									+ strHelper.rightPad(node.getFirstChild().getNodeValue(), " ", 10);
						}
						else {

						}
					}
					if (writeString.length() > 3) {
						try {
							writeString = strHelper.rightPad(writeString, " ", 67);
							bcodccuFileWriter.write(writeString.toString() + "\r\n");
						}
						catch (Exception ex) {
							logger.error(ex);
						}
					}

				}
				else {

				}
			}
			bcodstatFileWriter.write("\u001A");
			bcodcouFileWriter.write("\u001A");
			bcodcurFileWriter.write("\u001A");
			bcodccuFileWriter.write("\u001A");
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
			try{
				bcodccuFileWriter.close();
				bcodcouFileWriter.close();
				bcodcurFileWriter.close();
				bcodstatFileWriter.close();
				
			}
			catch(Exception e){
				logger.error(e);
			}
		}
	}
}
