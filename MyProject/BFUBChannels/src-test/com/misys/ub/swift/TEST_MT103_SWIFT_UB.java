package com.misys.ub.swift;

import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import com.misys.ub.swift.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class TEST_MT103_SWIFT_UB {


	private StringBuilder input = new StringBuilder("");
	private String InputData = null;
	private String ExpectedOutput = "<UB_MT103 xmlns=\"http://www.misys.com/ub/types/interfaces\"><header xmlns=\"http://www.misys.com/ub/types/interfaces\"><messageId1>2061</messageId1><messageType>MT103</messageType></header><details xmlns=\"http://www.misys.com/ub/types/interfaces\"><sender>CCRIES2A029</sender><receiver>FEMAMTMTXXX</receiver><sendersReference>TEST12123</sendersReference><bankOperationCode>CRTS</bankOperationCode><instruction><instructionCode>TELI/3226553478</instructionCode></instruction><transactionTypeCode>K90 - Fees</transactionTypeCode><tdValueDate>2017-05-02</tdValueDate><tdCurrencyCode>EUR</tdCurrencyCode><tdAmount>201.00</tdAmount><orderingCustomer>SBSASAJJ999</orderingCustomer><orderingCustomerOption>A</orderingCustomerOption><orderingInstitution>FUMAAU21XXX</orderingInstitution><orderInstitutionOption>A</orderInstitutionOption><sendersCorrespondent>MODLUS33XXX</sendersCorrespondent><sendersCorrespOption>A</sendersCorrespOption><receiversCorrespondent>/020201022222200\nNCCLBDDHXXX</receiversCorrespondent><receiversCorrespOption>A</receiversCorrespOption><thirdReimbursementInstitution>AAADFRP1XXX</thirdReimbursementInstitution><thirdReimbursementInstOption>A</thirdReimbursementInstOption><intermediaryInstitution>CITIBBBBXXX</intermediaryInstitution><intermediaryInstOption>A</intermediaryInstOption><accountWithInstitution>/020101022222200\nCCRIES2A029</accountWithInstitution><accountWithInstOption>A</accountWithInstOption><beneficiaryCustomer>/0121000000500</beneficiaryCustomer><remittanceInfo>/RFB/BET072$Sumit kumar$working$on swift</remittanceInfo><detailsOfCharges>OUR</detailsOfCharges><receiversCharges>USD25.00</receiversCharges><senderToReceiverInfo>Sender to Rec Info</senderToReceiverInfo><stp>Y</stp></details></UB_MT103>";
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
	
		input.append("<MeridianMessage MessageType=\"SWIFT_MT103\" MessageFormat=\"StandardXML\" System=\"SWIFT\" Timestamp=\"2017-06-27 12:39:42.233\">");
		input.append("<MessageID>2060</MessageID>");
		input.append("<CustomerID>1</CustomerID>");
		input.append("<MeridianMessageType>SWIFT_MT103</MeridianMessageType>");
		input.append("<ExternalMessageType>MT103</ExternalMessageType>");
		input.append("<InternalMessageType>P</InternalMessageType>");
		input.append("<HostID>MM</HostID>");
		input.append("<HostReference>970103CCRIES2AX0292222123456</HostReference>");
		input.append("<TRNO>TEST12123</TRNO>");
		input.append("<Direction>I</Direction>");
		input.append("<Priority>N</Priority>");
		input.append("<Queue>HostRoutingDecision</Queue>");
		input.append("<MessageStatus>U</MessageStatus>");
		input.append("<SystemArrivalTime>2017/06/27 12:39:41</SystemArrivalTime>");
		input.append("<ValueDate>20170502</ValueDate>");
		input.append("<CurrencyCode>EUR</CurrencyCode>");
		input.append("<Amount>20100</Amount>");
		input.append("<Network>SWIFT</Network>");
		input.append("<SenderAddress>CCRIES2AX029</SenderAddress>");
		input.append("<DestinationAddress>FEMAMTMTAXXX</DestinationAddress>");
		input.append("<PaymentBeneficiary>/0121000000500\nLionel Messi\nreceiving \nswift message</PaymentBeneficiary>");
		input.append("<PaymentPriority>9999</PaymentPriority>");
		input.append("<B1AppID>F</B1AppID>");
		input.append("<B1ServiceID>01</B1ServiceID>");
		input.append("<B1Session>0087</B1Session>");
		input.append("<B1Sequence>005328</B1Sequence>");
		input.append("<B2OInputTime>1200</B2OInputTime>");
		input.append("<B2OMIR>970103CCRIES2AX0292222123456</B2OMIR>");
		input.append("<B2OOutputDate>970103</B2OOutputDate>");
		input.append("<B2OOutputTime>1201</B2OOutputTime>");
		input.append("<B3Validation>STP</B3Validation>");
		input.append("<B3ServiceTypeId>123</B3ServiceTypeId>");
		input.append("<B3End2EndTxnRef>abcdef12-abcd-4eda-9eda-ab01bcedcdef</B3End2EndTxnRef>");
		input.append("<B3Validation>STP</B3Validation>");
		input.append("<B3ServiceTypeId>123</B3ServiceTypeId>");
		input.append("<B3End2EndTxnRef>abcdef12-abcd-4eda-9eda-ab01bcedcdef</B3End2EndTxnRef>");
		input.append("<SendersRef>TEST12123</SendersRef>");
		input.append("<BankOperationCode>CRTS</BankOperationCode>");
		input.append("<Instruction>");
		input.append("<InstructionCode>TELI/3226553478</InstructionCode></Instruction>");
		input.append("<TransactionTypeCode>K90 - Fees</TransactionTypeCode>");
		input.append("<ValueDateCcyInterbankSettledAmt>170502EUR201,00</ValueDateCcyInterbankSettledAmt>");
		input.append("<OrderingCustomerA>SBSASAJJ999</OrderingCustomerA>");
		input.append("<OrderingInstitutionA>FUMAAU21XXX</OrderingInstitutionA>");
		input.append("<SendersCorrespondentA>MODLUS33XXX</SendersCorrespondentA>");
		input.append("<ReceiversCorrespondentA>/020201022222200\nNCCLBDDHXXX</ReceiversCorrespondentA>");
		input.append("<ThirdReimbursementInstitutionA>AAADFRP1XXX</ThirdReimbursementInstitutionA>");
		input.append("<IntermediaryInstitutionA>CITIBBBBXXX</IntermediaryInstitutionA>");
		input.append("<AccountWithInstitutionA>/020101022222200\nCCRIES2A029</AccountWithInstitutionA>");
		input.append("<BeneficiaryCustomer>/0121000000500</BeneficiaryCustomer>");
		input.append("<RemittanceInfo>/RFB/BET072$Sumit kumar$working$on swift</RemittanceInfo>");
		input.append("<DetailsOfCharges>OUR</DetailsOfCharges>");
		input.append("<ReceiversCharges>USD25,00</ReceiversCharges>");
		input.append("<SenderToReceiverInfo>Sender to Rec Info</SenderToReceiverInfo></MeridianMessage>");
		InputData = input.toString();
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
			String ActualOutput = MT103_SWIFT_UB.MT103_SWIFT_Transform(InputData);
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			
			InputSource exp = new InputSource(new StringReader(ExpectedOutput));
			InputSource act = new InputSource(new StringReader(ActualOutput));
			Document ExpDoc = dBuilder.parse(exp);
			Document ActDoc = dBuilder.parse(act);
			
			
			//instructionCode
			
			assertTrue(ExpDoc.getElementsByTagName("sender").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("sender").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("receiver").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("receiver").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("sendersReference").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("sendersReference").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("bankOperationCode").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("bankOperationCode").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("transactionTypeCode").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("transactionTypeCode").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("tdValueDate").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("tdValueDate").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("tdCurrencyCode").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("tdCurrencyCode").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("tdAmount").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("tdAmount").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("orderingCustomer").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("orderingCustomer").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("orderingCustomerOption").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("orderingCustomerOption").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("orderingInstitution").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("orderingInstitution").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("orderInstitutionOption").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("orderInstitutionOption").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("sendersCorrespondent").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("sendersCorrespondent").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("sendersCorrespOption").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("sendersCorrespOption").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("receiversCorrespOption").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("receiversCorrespOption").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("thirdReimbursementInstitution").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("thirdReimbursementInstitution").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("thirdReimbursementInstOption").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("thirdReimbursementInstOption").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("intermediaryInstitution").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("intermediaryInstitution").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("intermediaryInstOption").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("intermediaryInstOption").item(0).getTextContent()));
			
			assertTrue(ExpDoc.getElementsByTagName("accountWithInstitution").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("accountWithInstitution").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("accountWithInstOption").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("accountWithInstOption").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("beneficiaryCustomer").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("beneficiaryCustomer").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("remittanceInfo").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("remittanceInfo").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("detailsOfCharges").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("detailsOfCharges").item(0).getTextContent()));
			
					
			assertTrue(ExpDoc.getElementsByTagName("receiversCharges").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("receiversCharges").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("senderToReceiverInfo").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("senderToReceiverInfo").item(0).getTextContent()));
			assertTrue(ExpDoc.getElementsByTagName("stp").item(0).getTextContent().equalsIgnoreCase(ActDoc.getElementsByTagName("stp").item(0).getTextContent()));
			
			
			
			
			
			
			
			
			
			
			
			//	
		} catch (Exception e) {
			System.out.println("Test case failed for MT201");
		}
	}


}
