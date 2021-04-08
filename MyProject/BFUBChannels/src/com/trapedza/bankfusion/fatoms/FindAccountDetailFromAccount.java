package com.trapedza.bankfusion.fatoms;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import bf.com.misys.cbs.msgs.v1r0.TermDepositOverviewRequest;
import bf.com.misys.cbs.msgs.v1r0.TermDepositOverviewResponse;
import bf.com.misys.cbs.types.TermDepositOverview;
import bf.com.misys.cbs.types.TermDepositOverviewInput;

import com.google.gson.Gson;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.ub.cc.restServices.SearchAccountInterfaceController;
import com.misys.ub.cc.types.AccountDetailsOverview;
import com.misys.ub.cc.types.SearchAccountInterfaceRq;
import com.misys.ub.cc.types.SearchAccountInterfacesRs;
import com.misys.ub.cc.utils.SearchAccountInterfaceConstants;
import com.misys.ub.dc.common.AccountPushToDC;
import com.trapedza.bankfusion.servercommon.microflow.FBPMicroflowServiceInvoker;

/**
 * 
 * @author Binit
 * 
 *         file to get account details using account
 * 
 */

public class FindAccountDetailFromAccount {
	private transient final static Log LOGGER = LogFactory
			.getLog(FindAccountDetailFromAccount.class.getName());

	public static void executeFiles(Exchange exchange)
			throws ParserConfigurationException, SAXException, IOException {

		LOGGER.info("Start of FindAccountDetailFrom Account");
		Message params = exchange.getIn();
		String requestMsg = params.getBody().toString();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(requestMsg));
		Document doc = dBuilder.parse(is);
		ArrayList<String> lobArrayList = new ArrayList<String>();
		doc.getDocumentElement().normalize();
        AccountPushToDC pushAcc = new AccountPushToDC();

		NodeList nList = doc.getElementsByTagName("ubintcf:accIdList");
		NodeList lobList = doc.getElementsByTagName("ubintcf:lob");
		for (int i = 0; i < lobList.getLength(); i++) {
			Element lob = (Element) lobList.item(i);
			lobArrayList.add(lob.getFirstChild().getNodeValue());
			LOGGER.debug(lobArrayList.get(i));
		}

		String accountOperation = null;
		
		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				int loopcount = 0;
				Element xmElements = (Element) nNode;
				NodeList nodeList = xmElements
						.getElementsByTagName("ubintcf:accId");
				for (int i = 0; i < nodeList.getLength(); i++) {

					/*xmElements.getElementsByTagName("ubintcf:accId")
							.item(loopcount).getTextContent();*/
					accountOperation = xmElements.getElementsByTagName("ubintcf:accOperation").item(loopcount).getTextContent();
					SearchAccountInterfaceRq rq = new SearchAccountInterfaceRq();
					rq.setAccountId(xmElements
							.getElementsByTagName("ubintcf:accId")
							.item(loopcount).getTextContent());
					SearchAccountInterfaceController search = new SearchAccountInterfaceController();
					SearchAccountInterfacesRs rs = search.post(rq);
					rs.getAccountDetailsOverviewList().get(loopcount).getExtensiveAccountDetails().setAccountOperation(accountOperation);					
					fetchTermDepositDetails(rs);
					AccountDetailsWithLob accountDetailsWithLob = new AccountDetailsWithLob();
					accountDetailsWithLob.setAccountInterfacesRs(rs);
					accountDetailsWithLob.setLobList(lobArrayList);
					Gson gson = new Gson();
					String response = gson.toJson(accountDetailsWithLob);
					exchange.getOut().setBody(response);
					for (String lob : accountDetailsWithLob.getLobList()) {
                        if (lob.equals("DIGICHANNELS")) {
                            pushAcc.pushAccountToDCQueue(accountDetailsWithLob);
                        }
                        else {
						String endpointName = "ACCOUNT_DETAIL_RESPONSE_"
								.concat(lob);
						MessageProducerUtil.sendMessage(response, endpointName);
                        }
					}
					loopcount++;
				}
			}
		}
		LOGGER.info("End of FindAccountDetailFrom Account");
	}

	static void fetchTermDepositDetails(SearchAccountInterfacesRs rs) {

		TermDepositOverviewResponse termDepositOverviewRes = null;
		TermDepositOverviewRequest termDepOverviewReq = new TermDepositOverviewRequest();
		TermDepositOverviewInput termDepOverviewInput = new TermDepositOverviewInput();
		termDepOverviewInput.setCustomerId(rs.getAccountDetailsOverviewList()
				.get(0).getExtensiveAccountDetails().getAccountBasicDetails()
				.getCustomerShortDetails().getCustomerId());
		termDepOverviewReq.setTermDepositOverviewInput(termDepOverviewInput);

		HashMap<String, Object> inputParams1 = new HashMap<String, Object>();
		inputParams1.put(
				SearchAccountInterfaceConstants.TERM_DEP_OVERVIEW_REQUEST,
				termDepOverviewReq);
		FBPMicroflowServiceInvoker invoker1 = new FBPMicroflowServiceInvoker(
				SearchAccountInterfaceConstants.TERM_DEP_OVERVIEW_SERVICE);
		HashMap outputParams1 = invoker1.invokeMicroflow(inputParams1, false);
		termDepositOverviewRes = (TermDepositOverviewResponse) outputParams1
				.get(SearchAccountInterfaceConstants.TERM_DEP_OVERVIEW_RESPONSE);
		if (termDepositOverviewRes != null
				&& termDepositOverviewRes.getTermDepositOverviewResponse()
						.getTermDepositOverview().length != 0) {
			for (TermDepositOverview termDepOverview : termDepositOverviewRes
					.getTermDepositOverviewResponse().getTermDepositOverview()) {
				for (AccountDetailsOverview accDtlsOverview : rs
						.getAccountDetailsOverviewList()) {
					if (termDepOverview
							.getTermDepositAccount()
							.getExternalAccountNumber()
							.equals(accDtlsOverview
									.getExtensiveAccountDetails()
									.getAccountBasicDetails().getAccountKeys()
									.getStandardAccountId())) {
						accDtlsOverview.getExtensiveAccountDetails()
								.setTermDepositOverview(termDepOverview);
					}
				}
			}
		}

	}

}
