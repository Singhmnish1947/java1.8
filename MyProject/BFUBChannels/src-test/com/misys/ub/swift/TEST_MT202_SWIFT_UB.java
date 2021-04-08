package com.misys.ub.swift;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import com.misys.ub.swift.MT202_SWIFT_UB;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class TEST_MT202_SWIFT_UB {

	private StringBuilder InMsg = new StringBuilder("");
	private String InputData = null;
	private String ExpectedOutput = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<UB_MT202 xmlns=\"http://www.misys.com/ub/types/interfaces\">\n<header><messageType>MT202</messageType><messageId1>2067</messageId1></header><details><sender>ABNANL2AXXX</sender><receiver>SBZAZAJJXXX</receiver><transactionReferenceNumber>MT011</transactionReferenceNumber><relatedReference>MT011</relatedReference><tdValueDate>2017-06-25</tdValueDate><tdCurrencyCode>EUR</tdCurrencyCode><tdAmount>200.00</tdAmount><sendersCorrespondent>/0121000000800</sendersCorrespondent><sendersCorrespondentOption>B</sendersCorrespondentOption><accountWithInstitution>CITICATTXXX</accountWithInstitution><accountWithInstitutionOption>A</accountWithInstitutionOption><beneficiary>/0100590000800$AXABDE31XXX</beneficiary><beneficiaryOption>A</beneficiaryOption><cover>COV</cover></details></UB_MT202>";

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		InMsg.append("<MeridianMessage MessageType=\"SWIFT_MT202\" MessageFormat=\"StandardXML\" System=\"SWIFT\" Timestamp=\"2017-06-27 14:47:31.918\">");
		InMsg.append("<MessageID>2067</MessageID>");
		InMsg.append("<CustomerID>1</CustomerID>");
		InMsg.append("<MeridianMessageType>SWIFT_MT202</MeridianMessageType>");
		InMsg.append("<ExternalMessageType>MT202</ExternalMessageType>");
		InMsg.append("<InternalMessageType>P</InternalMessageType>");
		InMsg.append("<HostID>MM</HostID>");
		InMsg.append("<HostReference>970103ABNANL2AXXXX2222123456</HostReference>");
		InMsg.append("<TRNO>MT011</TRNO>>");
		InMsg.append("<Direction>I</Direction>");
		InMsg.append("<Priority>N</Priority>");
		InMsg.append("<Queue>HostRoutingDecision</Queue>");
		InMsg.append("<MessageStatus>U</MessageStatus>>");
		InMsg.append("<SystemArrivalTime>2017/06/27 14:47:31</SystemArrivalTime>");
		InMsg.append("<ValueDate>20170625</ValueDate>");
		InMsg.append("<CurrencyCode>EUR</CurrencyCode>");
		InMsg.append("<Amount>20000</Amount>");
		InMsg.append("<Network>SWIFT</Network>");
		InMsg.append("<SenderAddress>ABNANL2AXXXX</SenderAddress>");
		InMsg.append("<DestinationAddress>SBZAZAJJAXXX</DestinationAddress>");
		InMsg.append("<PaymentPriority>9999</PaymentPriority>");
		InMsg.append("<B1AppID>F</B1AppID>");
		InMsg.append("<B1ServiceID>01</B1ServiceID>");
		InMsg.append("<B1Session>9999</B1Session>");
		InMsg.append("<B1Sequence>999999</B1Sequence>");
		InMsg.append("<B2OInputTime>1200</B2OInputTime>");
		InMsg.append("<B2OMIR>970103ABNANL2AXXXX2222123456</B2OMIR>");
		InMsg.append("<B2OOutputDate>970103</B2OOutputDate>");
		InMsg.append("<B2OOutputTime>1201</B2OOutputTime>");
		InMsg.append("<B3Validation>COV</B3Validation>");
		InMsg.append("<B3ServiceTypeId>123</B3ServiceTypeId>");
		InMsg.append("<B3End2EndTxnRef>abcdef12-abcd-4eda-9eda-ab01bcedcde</B3End2EndTxnRef>");
		InMsg.append("<TRN>MT011</TRN>");
		InMsg.append("<RelatedReference>MT011</RelatedReference>");
		InMsg.append("<ValueDateCcyAmount>170625EUR200,00</ValueDateCcyAmount>");
		InMsg.append("<SendersCorrespondentB>/0121000000800</SendersCorrespondentB>");
		InMsg.append("<AccountWithInstitutionA>CITICATTXXX</AccountWithInstitutionA>");
		InMsg.append("<BeneficiaryA>/0100590000800$AXABDE31XXX</BeneficiaryA>");
		InMsg.append("</MeridianMessage>");
		InputData = InMsg.toString();
	
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMT202() {
		
		try {
			String ActualOutput = MT202_SWIFT_UB.MT202_Transform(InputData);
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			
			InputSource exp = new InputSource(new StringReader(ExpectedOutput));
			InputSource act = new InputSource(new StringReader(ActualOutput));
			Document ExpDoc = dBuilder.parse(exp);
			Document ActDoc = dBuilder.parse(act);
			
			assertTrue(ExpDoc.getElementsByTagName("messageType").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("messageType").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("transactionReferenceNumber").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("transactionReferenceNumber").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("tdValueDate").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("tdValueDate").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("tdCurrencyCode").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("tdCurrencyCode").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("sendersCorrespondent").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("sendersCorrespondent").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("accountWithInstitution").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("accountWithInstitution").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("beneficiary").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("beneficiary").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("beneficiaryOption").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("beneficiaryOption").item(0).getTextContent()));
		//	
		} catch (Exception e) {
			System.out.println("Test case failed for MT202");
		}
	}

}
