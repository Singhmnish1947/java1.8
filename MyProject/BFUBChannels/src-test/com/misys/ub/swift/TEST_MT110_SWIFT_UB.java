package com.misys.ub.swift;

import static org.junit.Assert.assertTrue;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class TEST_MT110_SWIFT_UB {


	private StringBuilder ExpectedOpt = new StringBuilder("");
	private String InputData1 = null;
	private String InputData2 = null;
	private String InputData3 = null;
	private String InputData4 = null;
	private String InputData5 = null;
	private String InputData6 = null;
	private String InputData7 = null;
		
	private String ExpectedOutput1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><MeridianMessage MessageFormat=\"StandardXML\" MessageType=\"SWIFT_MT110\"    System=\"SWIFT\" Timestamp=\"2017-07-27 12:02:46.264\"><Cheque><ChequeNumber>000001003</ChequeNumber><AmountB>EUR11,00</AmountB><DateOfIssue>110928</DateOfIssue><PayeeF>1/ aaa\n2/ ssss\n3/ ssd</PayeeF><PayerK>/0121200000300\nNAME</PayerK></Cheque><SenderReference>000001003</SenderReference><SenderCorrespondentA>SBZAZAJJXXX</SenderCorrespondentA><HostReference>000001003</HostReference><SenderAddress>SBZAZAJJXXX</SenderAddress><messageId>0</messageId><ExternalMessageType>MT110</ExternalMessageType><DestinationAddress>SBZAZAJJXXX</DestinationAddress><MeridianMessageType>SWIFT_MT110</MeridianMessageType><InternalMessageType>A</InternalMessageType><HostType>UB</HostType><HostID>UB</HostID><TransactionReference>000001003</TransactionReference><Priority>N</Priority><Cancel>N</Cancel><SystemArrivalTime>2017/07/27 12:02:46</SystemArrivalTime><ValueDate>20110928</ValueDate><Network>SWIFT</Network><LineOfBusiness>UB</LineOfBusiness><MultipleMessageStatus>I</MultipleMessageStatus><Direction>O</Direction><PaymentPriority>9999</PaymentPriority><B2IDeliveryMonitoring>2</B2IDeliveryMonitoring><B2IObsolescencePeriod>020</B2IObsolescencePeriod></MeridianMessage>";            
	private String ExpectedOutput2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><MeridianMessage MessageFormat=\"StandardXML\" MessageType=\"SWIFT_MT110\"    System=\"SWIFT\" Timestamp=\"2017-07-27 12:02:46.264\"><Cheque><ChequeNumber>000001003</ChequeNumber><AmountB>EUR11,00</AmountB><DateOfIssue>110928</DateOfIssue><PayeeF>1/ aaa\n2/ ssss\n3/ ssd</PayeeF><PayerK>/0121200000300\nNAME</PayerK></Cheque><SenderReference>000001003</SenderReference><SenderCorrespondentA>SBZAZAJJXXX</SenderCorrespondentA><HostReference>000001003</HostReference><SenderAddress>SBZAZAJJXXX</SenderAddress><messageId>0</messageId><ExternalMessageType>MT110</ExternalMessageType><DestinationAddress>SBZAZAJJXXX</DestinationAddress><MeridianMessageType>SWIFT_MT110</MeridianMessageType><InternalMessageType>A</InternalMessageType><HostType>UB</HostType><HostID>UB</HostID><TransactionReference>000001003</TransactionReference><Priority>N</Priority><Cancel>N</Cancel><SystemArrivalTime>2017/07/27 12:02:46</SystemArrivalTime><ValueDate>20110928</ValueDate><Network>SWIFT</Network><LineOfBusiness>UB</LineOfBusiness><MultipleMessageStatus>I</MultipleMessageStatus><Direction>O</Direction><PaymentPriority>9999</PaymentPriority><B2IDeliveryMonitoring>2</B2IDeliveryMonitoring><B2IObsolescencePeriod>020</B2IObsolescencePeriod></MeridianMessage>";            
	private String ExpectedOutput3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><MeridianMessage MessageFormat=\"StandardXML\" MessageType=\"SWIFT_MT110\"    System=\"SWIFT\" Timestamp=\"2017-07-27 12:02:46.264\"><Cheque><ChequeNumber>000001003</ChequeNumber><AmountB>EUR11,00</AmountB><DateOfIssue>110928</DateOfIssue><PayeeF>1/ aaa\n2/ ssss\n3/ ssd</PayeeF><PayerK>/0121200000300\nNAME</PayerK></Cheque><SenderReference>000001003</SenderReference><SenderCorrespondentA>SBZAZAJJXXX</SenderCorrespondentA><HostReference>000001003</HostReference><SenderAddress>SBZAZAJJXXX</SenderAddress><messageId>0</messageId><ExternalMessageType>MT110</ExternalMessageType><DestinationAddress>SBZAZAJJXXX</DestinationAddress><MeridianMessageType>SWIFT_MT110</MeridianMessageType><InternalMessageType>A</InternalMessageType><HostType>UB</HostType><HostID>UB</HostID><TransactionReference>000001003</TransactionReference><Priority>N</Priority><Cancel>N</Cancel><SystemArrivalTime>2017/07/27 12:02:46</SystemArrivalTime><ValueDate>20110928</ValueDate><Network>SWIFT</Network><LineOfBusiness>UB</LineOfBusiness><MultipleMessageStatus>I</MultipleMessageStatus><Direction>O</Direction><PaymentPriority>9999</PaymentPriority><B2IDeliveryMonitoring>2</B2IDeliveryMonitoring><B2IObsolescencePeriod>020</B2IObsolescencePeriod></MeridianMessage>";            
	private String ExpectedOutput4 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><MeridianMessage MessageFormat=\"StandardXML\" MessageType=\"SWIFT_MT110\"    System=\"SWIFT\" Timestamp=\"2017-07-27 12:02:46.264\"><Cheque><ChequeNumber>000001003</ChequeNumber><AmountB>EUR11,00</AmountB><DateOfIssue>110928</DateOfIssue><PayeeF>1/ aaa\n2/ ssss\n3/ ssd</PayeeF><PayerK>/0121200000300\nNAME</PayerK></Cheque><SenderReference>000001003</SenderReference><SenderCorrespondentA>SBZAZAJJXXX</SenderCorrespondentA><HostReference>000001003</HostReference><SenderAddress>SBZAZAJJXXX</SenderAddress><messageId>0</messageId><ExternalMessageType>MT110</ExternalMessageType><DestinationAddress>SBZAZAJJXXX</DestinationAddress><MeridianMessageType>SWIFT_MT110</MeridianMessageType><InternalMessageType>A</InternalMessageType><HostType>UB</HostType><HostID>UB</HostID><TransactionReference>000001003</TransactionReference><Priority>N</Priority><Cancel>N</Cancel><SystemArrivalTime>2017/07/27 12:02:46</SystemArrivalTime><ValueDate>20110928</ValueDate><Network>SWIFT</Network><LineOfBusiness>UB</LineOfBusiness><MultipleMessageStatus>I</MultipleMessageStatus><Direction>O</Direction><PaymentPriority>9999</PaymentPriority><B2IDeliveryMonitoring>2</B2IDeliveryMonitoring><B2IObsolescencePeriod>020</B2IObsolescencePeriod></MeridianMessage>";            
	private String ExpectedOutput5 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><MeridianMessage MessageFormat=\"StandardXML\" MessageType=\"SWIFT_MT110\"    System=\"SWIFT\" Timestamp=\"2017-07-27 12:02:46.264\"><Cheque><ChequeNumber>000001003</ChequeNumber><AmountB>EUR11,00</AmountB><DateOfIssue>110928</DateOfIssue><PayeeF>1/ aaa\n2/ ssss\n3/ ssd</PayeeF><PayerK>/0121200000300\nNAME</PayerK></Cheque><SenderReference>000001003</SenderReference><SenderCorrespondentA>SBZAZAJJXXX</SenderCorrespondentA><HostReference>000001003</HostReference><SenderAddress>SBZAZAJJXXX</SenderAddress><messageId>0</messageId><ExternalMessageType>MT110</ExternalMessageType><DestinationAddress>SBZAZAJJXXX</DestinationAddress><MeridianMessageType>SWIFT_MT110</MeridianMessageType><InternalMessageType>A</InternalMessageType><HostType>UB</HostType><HostID>UB</HostID><TransactionReference>000001003</TransactionReference><Priority>N</Priority><Cancel>N</Cancel><SystemArrivalTime>2017/07/27 12:02:46</SystemArrivalTime><ValueDate>20110928</ValueDate><Network>SWIFT</Network><LineOfBusiness>UB</LineOfBusiness><MultipleMessageStatus>I</MultipleMessageStatus><Direction>O</Direction><PaymentPriority>9999</PaymentPriority><B2IDeliveryMonitoring>2</B2IDeliveryMonitoring><B2IObsolescencePeriod>020</B2IObsolescencePeriod></MeridianMessage>";            
	private String ExpectedOutput6 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><MeridianMessage MessageFormat=\"StandardXML\" MessageType=\"SWIFT_MT110\"    System=\"SWIFT\" Timestamp=\"2017-07-27 12:02:46.264\"><Cheque><ChequeNumber>000001003</ChequeNumber><AmountB>EUR11,00</AmountB><DateOfIssue>110928</DateOfIssue><PayeeF>1/ aaa\n2/ ssss\n3/ ssd</PayeeF><PayerK>/0121200000300\nNAME</PayerK></Cheque><SenderReference>000001003</SenderReference><SenderCorrespondentA>SBZAZAJJXXX</SenderCorrespondentA><HostReference>000001003</HostReference><SenderAddress>SBZAZAJJXXX</SenderAddress><messageId>0</messageId><ExternalMessageType>MT110</ExternalMessageType><DestinationAddress>SBZAZAJJXXX</DestinationAddress><MeridianMessageType>SWIFT_MT110</MeridianMessageType><InternalMessageType>A</InternalMessageType><HostType>UB</HostType><HostID>UB</HostID><TransactionReference>000001003</TransactionReference><Priority>N</Priority><Cancel>N</Cancel><SystemArrivalTime>2017/07/27 12:02:46</SystemArrivalTime><ValueDate>20110928</ValueDate><Network>SWIFT</Network><LineOfBusiness>UB</LineOfBusiness><MultipleMessageStatus>I</MultipleMessageStatus><Direction>O</Direction><PaymentPriority>9999</PaymentPriority><B2IDeliveryMonitoring>2</B2IDeliveryMonitoring><B2IObsolescencePeriod>020</B2IObsolescencePeriod></MeridianMessage>";            
	private String ExpectedOutput7 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><MeridianMessage MessageFormat=\"StandardXML\" MessageType=\"SWIFT_MT110\"    System=\"SWIFT\" Timestamp=\"2017-07-27 12:02:46.264\"><Cheque><ChequeNumber>000001003</ChequeNumber><AmountB>EUR11,00</AmountB><DateOfIssue>110928</DateOfIssue><PayeeF>1/ aaa\n2/ ssss\n3/ ssd</PayeeF><PayerK>/0121200000300\nNAME</PayerK></Cheque><SenderReference>000001003</SenderReference><SenderCorrespondentA>SBZAZAJJXXX</SenderCorrespondentA><HostReference>000001003</HostReference><SenderAddress>SBZAZAJJXXX</SenderAddress><messageId>0</messageId><ExternalMessageType>MT110</ExternalMessageType><DestinationAddress>SBZAZAJJXXX</DestinationAddress><MeridianMessageType>SWIFT_MT110</MeridianMessageType><InternalMessageType>A</InternalMessageType><HostType>UB</HostType><HostID>UB</HostID><TransactionReference>000001003</TransactionReference><Priority>N</Priority><Cancel>N</Cancel><SystemArrivalTime>2017/07/27 12:02:46</SystemArrivalTime><ValueDate>20110928</ValueDate><Network>SWIFT</Network><LineOfBusiness>UB</LineOfBusiness><MultipleMessageStatus>I</MultipleMessageStatus><Direction>O</Direction><PaymentPriority>9999</PaymentPriority><B2IDeliveryMonitoring>2</B2IDeliveryMonitoring><B2IObsolescencePeriod>020</B2IObsolescencePeriod></MeridianMessage>";            
	

			
	@Before
	public void setUp() throws Exception {
		
		InputData1=	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<UB_MT110>"
			   +"<receiversCorrespOption></receiversCorrespOption>"
			   +"<receiversCorrespondent></receiversCorrespondent>"
			   +"<SenderToReceiverInfo></SenderToReceiverInfo>"
			   +"<sendersCorrespOption>A</sendersCorrespOption>"
			   +" <Cheque>"
			   +"     <ChequeNumber>000001003</ChequeNumber>"
			   +"  	  <amount>EUR11.00</amount>"
			   +"     <amountOption>B</amountOption>"
			   +"     <DateOfIssue>2011-09-28</DateOfIssue>"
			   +"     <Payee>1/ aaa$2/ ssss$3/ ssd$</Payee>"
			   +"     <drawerBank></drawerBank>"
			   +"     <drawerBankOption> </drawerBankOption>"
			   +" </Cheque>"
			   +"<payeeOption>F</payeeOption>"
			   +"<orderingCustomer>/0121200000300$NAME$$$</orderingCustomer>"
			    +"<orderingCustomerOption>K</orderingCustomerOption>"
			    +"<SenderReference>000001003</SenderReference>"
			    +"<sendersCorrespondent>SBZAZAJJXXX</sendersCorrespondent>"
			    +"<HostReference>000001003</HostReference>"
			    +"<verificationRequired></verificationRequired>"
			    +"<multipleHold></multipleHold>"
			    +"<SenderAddress>SBZAZAJJXXX</SenderAddress>"
			    +"<internalRef></internalRef>"
			    +"<messageId>0</messageId>"
			    +"<ExternalMessageType>MT110</ExternalMessageType>"
			    +"<branch></branch>"
			    +"<DestinationAddress>SBZAZAJJXXX</DestinationAddress>"
			    +"<CancellationAction></CancellationAction>"
			+"</UB_MT110>";
		
		
		InputData2=	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<UB_MT110>"
			   +"<receiversCorrespOption></receiversCorrespOption>"
			   +"<receiversCorrespondent></receiversCorrespondent>"
			   +"<SenderToReceiverInfo></SenderToReceiverInfo>"
			   +"<sendersCorrespOption>A</sendersCorrespOption>"
			   +" <Cheque>"
			   +"     <ChequeNumber>000001003</ChequeNumber>"
			   +"  	  <amount>EUR11.00</amount>"
			   +"     <amountOption>B</amountOption>"
			   +"     <DateOfIssue>2011-09-28</DateOfIssue>"
			   +"     <Payee>1/ aaa$2/ ssss$3/ ssd$</Payee>"
			   +"     <drawerBank></drawerBank>"
			   +"     <drawerBankOption> </drawerBankOption>"
			   +" </Cheque>"
			   +"<payeeOption>F</payeeOption>"
			   +"<orderingCustomer>/0121200000300$NAME$$$</orderingCustomer>"
			    +"<orderingCustomerOption>K</orderingCustomerOption>"
			    +"<SenderReference>000001003</SenderReference>"
			    +"<sendersCorrespondent>SBZAZAJJXXX</sendersCorrespondent>"
			    +"<HostReference>000001003</HostReference>"
			    +"<verificationRequired></verificationRequired>"
			    +"<multipleHold></multipleHold>"
			    +"<SenderAddress>SBZAZAJJXXX</SenderAddress>"
			    +"<internalRef></internalRef>"
			    +"<messageId>0</messageId>"
			    +"<ExternalMessageType>MT110</ExternalMessageType>"
			    +"<branch></branch>"
			    +"<DestinationAddress>SBZAZAJJXXX</DestinationAddress>"
			    +"<CancellationAction></CancellationAction>"
			+"</UB_MT110>";
		
		InputData3=	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<UB_MT110>"
			   +"<receiversCorrespOption></receiversCorrespOption>"
			   +"<receiversCorrespondent></receiversCorrespondent>"
			   +"<SenderToReceiverInfo></SenderToReceiverInfo>"
			   +"<sendersCorrespOption>A</sendersCorrespOption>"
			   +" <Cheque>"
			   +"     <ChequeNumber>000001003</ChequeNumber>"
			   +"  	  <amount>EUR11.00</amount>"
			   +"     <amountOption>B</amountOption>"
			   +"     <DateOfIssue>2011-09-28</DateOfIssue>"
			   +"     <Payee>1/ aaa$2/ ssss$3/ ssd$</Payee>"
			   +"     <drawerBank></drawerBank>"
			   +"     <drawerBankOption> </drawerBankOption>"
			   +" </Cheque>"
			   +"<payeeOption>F</payeeOption>"
			   +"<orderingCustomer>/0121200000300$NAME$$$</orderingCustomer>"
			    +"<orderingCustomerOption>K</orderingCustomerOption>"
			    +"<SenderReference>000001003</SenderReference>"
			    +"<sendersCorrespondent>SBZAZAJJXXX</sendersCorrespondent>"
			    +"<HostReference>000001003</HostReference>"
			    +"<verificationRequired></verificationRequired>"
			    +"<multipleHold></multipleHold>"
			    +"<SenderAddress>SBZAZAJJXXX</SenderAddress>"
			    +"<internalRef></internalRef>"
			    +"<messageId>0</messageId>"
			    +"<ExternalMessageType>MT110</ExternalMessageType>"
			    +"<branch></branch>"
			    +"<DestinationAddress>SBZAZAJJXXX</DestinationAddress>"
			    +"<CancellationAction></CancellationAction>"
			+"</UB_MT110>";
		
		InputData4=	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<UB_MT110>"
			   +"<receiversCorrespOption></receiversCorrespOption>"
			   +"<receiversCorrespondent></receiversCorrespondent>"
			   +"<SenderToReceiverInfo></SenderToReceiverInfo>"
			   +"<sendersCorrespOption>A</sendersCorrespOption>"
			   +" <Cheque>"
			   +"     <ChequeNumber>000001003</ChequeNumber>"
			   +"  	  <amount>EUR11.00</amount>"
			   +"     <amountOption>B</amountOption>"
			   +"     <DateOfIssue>2011-09-28</DateOfIssue>"
			   +"     <Payee>1/ aaa$2/ ssss$3/ ssd$</Payee>"
			   +"     <drawerBank></drawerBank>"
			   +"     <drawerBankOption> </drawerBankOption>"
			   +" </Cheque>"
			   +"<payeeOption>F</payeeOption>"
			   +"<orderingCustomer>/0121200000300$NAME$$$</orderingCustomer>"
			    +"<orderingCustomerOption>K</orderingCustomerOption>"
			    +"<SenderReference>000001003</SenderReference>"
			    +"<sendersCorrespondent>SBZAZAJJXXX</sendersCorrespondent>"
			    +"<HostReference>000001003</HostReference>"
			    +"<verificationRequired></verificationRequired>"
			    +"<multipleHold></multipleHold>"
			    +"<SenderAddress>SBZAZAJJXXX</SenderAddress>"
			    +"<internalRef></internalRef>"
			    +"<messageId>0</messageId>"
			    +"<ExternalMessageType>MT110</ExternalMessageType>"
			    +"<branch></branch>"
			    +"<DestinationAddress>SBZAZAJJXXX</DestinationAddress>"
			    +"<CancellationAction></CancellationAction>"
			+"</UB_MT110>";	
		InputData5=	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<UB_MT110>"
			   +"<receiversCorrespOption></receiversCorrespOption>"
			   +"<receiversCorrespondent></receiversCorrespondent>"
			   +"<SenderToReceiverInfo></SenderToReceiverInfo>"
			   +"<sendersCorrespOption>A</sendersCorrespOption>"
			   +" <Cheque>"
			   +"     <ChequeNumber>000001003</ChequeNumber>"
			   +"  	  <amount>EUR11.00</amount>"
			   +"     <amountOption>B</amountOption>"
			   +"     <DateOfIssue>2011-09-28</DateOfIssue>"
			   +"     <Payee>1/ aaa$2/ ssss$3/ ssd$</Payee>"
			   +"     <drawerBank></drawerBank>"
			   +"     <drawerBankOption> </drawerBankOption>"
			   +" </Cheque>"
			   +"<payeeOption>F</payeeOption>"
			   +"<orderingCustomer>/0121200000300$NAME$$$</orderingCustomer>"
			    +"<orderingCustomerOption>K</orderingCustomerOption>"
			    +"<SenderReference>000001003</SenderReference>"
			    +"<sendersCorrespondent>SBZAZAJJXXX</sendersCorrespondent>"
			    +"<HostReference>000001003</HostReference>"
			    +"<verificationRequired></verificationRequired>"
			    +"<multipleHold></multipleHold>"
			    +"<SenderAddress>SBZAZAJJXXX</SenderAddress>"
			    +"<internalRef></internalRef>"
			    +"<messageId>0</messageId>"
			    +"<ExternalMessageType>MT110</ExternalMessageType>"
			    +"<branch></branch>"
			    +"<DestinationAddress>SBZAZAJJXXX</DestinationAddress>"
			    +"<CancellationAction></CancellationAction>"
			+"</UB_MT110>";
		InputData6=	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<UB_MT110>"
			   +"<receiversCorrespOption></receiversCorrespOption>"
			   +"<receiversCorrespondent></receiversCorrespondent>"
			   +"<SenderToReceiverInfo></SenderToReceiverInfo>"
			   +"<sendersCorrespOption>A</sendersCorrespOption>"
			   +" <Cheque>"
			   +"     <ChequeNumber>000001003</ChequeNumber>"
			   +"  	  <amount>EUR11.00</amount>"
			   +"     <amountOption>B</amountOption>"
			   +"     <DateOfIssue>2011-09-28</DateOfIssue>"
			   +"     <Payee>1/ aaa$2/ ssss$3/ ssd$</Payee>"
			   +"     <drawerBank></drawerBank>"
			   +"     <drawerBankOption> </drawerBankOption>"
			   +" </Cheque>"
			   +"<payeeOption>F</payeeOption>"
			   +"<orderingCustomer>/0121200000300$NAME$$$</orderingCustomer>"
			    +"<orderingCustomerOption>K</orderingCustomerOption>"
			    +"<SenderReference>000001003</SenderReference>"
			    +"<sendersCorrespondent>SBZAZAJJXXX</sendersCorrespondent>"
			    +"<HostReference>000001003</HostReference>"
			    +"<verificationRequired></verificationRequired>"
			    +"<multipleHold></multipleHold>"
			    +"<SenderAddress>SBZAZAJJXXX</SenderAddress>"
			    +"<internalRef></internalRef>"
			    +"<messageId>0</messageId>"
			    +"<ExternalMessageType>MT110</ExternalMessageType>"
			    +"<branch></branch>"
			    +"<DestinationAddress>SBZAZAJJXXX</DestinationAddress>"
			    +"<CancellationAction></CancellationAction>"
			+"</UB_MT110>";
		InputData7=	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<UB_MT110>"
			   +"<receiversCorrespOption></receiversCorrespOption>"
			   +"<receiversCorrespondent></receiversCorrespondent>"
			   +"<SenderToReceiverInfo></SenderToReceiverInfo>"
			   +"<sendersCorrespOption>A</sendersCorrespOption>"
			   +" <Cheque>"
			   +"     <ChequeNumber>000001003</ChequeNumber>"
			   +"  	  <amount>EUR11.00</amount>"
			   +"     <amountOption>B</amountOption>"
			   +"     <DateOfIssue>2011-09-28</DateOfIssue>"
			   +"     <Payee>1/ aaa$2/ ssss$3/ ssd$</Payee>"
			   +"     <drawerBank></drawerBank>"
			   +"     <drawerBankOption> </drawerBankOption>"
			   +" </Cheque>"
			   +"<payeeOption>F</payeeOption>"
			   +"<orderingCustomer>/0121200000300$NAME$$$</orderingCustomer>"
			    +"<orderingCustomerOption>K</orderingCustomerOption>"
			    +"<SenderReference>000001003</SenderReference>"
			    +"<sendersCorrespondent>SBZAZAJJXXX</sendersCorrespondent>"
			    +"<HostReference>000001003</HostReference>"
			    +"<verificationRequired></verificationRequired>"
			    +"<multipleHold></multipleHold>"
			    +"<SenderAddress>SBZAZAJJXXX</SenderAddress>"
			    +"<internalRef></internalRef>"
			    +"<messageId>0</messageId>"
			    +"<ExternalMessageType>MT110</ExternalMessageType>"
			    +"<branch></branch>"
			    +"<DestinationAddress>SBZAZAJJXXX</DestinationAddress>"
			    +"<CancellationAction></CancellationAction>"
			+"</UB_MT110>";
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMT110() {
		try {
			
			String ActualOutput1 = UB_MT110_FileCreator.MT110_Transform(InputData1);
			String ActualOutput2 = UB_MT110_FileCreator.MT110_Transform(InputData2);
			String ActualOutput3 = UB_MT110_FileCreator.MT110_Transform(InputData3);
			String ActualOutput4 = UB_MT110_FileCreator.MT110_Transform(InputData4);
			String ActualOutput5 = UB_MT110_FileCreator.MT110_Transform(InputData5);
			String ActualOutput6 = UB_MT110_FileCreator.MT110_Transform(InputData6);
			String ActualOutput7 = UB_MT110_FileCreator.MT110_Transform(InputData7);
			
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			
			InputSource exp1 = new InputSource(new StringReader(ExpectedOutput1));
			InputSource act1 = new InputSource(new StringReader(ActualOutput1));
			Document ExpDoc1 = dBuilder.parse(exp1);
			Document ActDoc1 = dBuilder.parse(act1);
			
			InputSource exp2 = new InputSource(new StringReader(ExpectedOutput2));
			InputSource act2 = new InputSource(new StringReader(ActualOutput2));
			Document ExpDoc2 = dBuilder.parse(exp2);
			Document ActDoc2 = dBuilder.parse(act2);
			
			InputSource exp3 = new InputSource(new StringReader(ExpectedOutput3));
			InputSource act3 = new InputSource(new StringReader(ActualOutput3));
			Document ExpDoc3 = dBuilder.parse(exp3);
			Document ActDoc3 = dBuilder.parse(act3);
			
			InputSource exp4 = new InputSource(new StringReader(ExpectedOutput4));
			InputSource act4 = new InputSource(new StringReader(ActualOutput4));
			Document ExpDoc4 = dBuilder.parse(exp4);
			Document ActDoc4 = dBuilder.parse(act4);
			
			InputSource exp5 = new InputSource(new StringReader(ExpectedOutput5));
			InputSource act5 = new InputSource(new StringReader(ActualOutput5));
			Document ExpDoc5 = dBuilder.parse(exp5);
			Document ActDoc5 = dBuilder.parse(act5);

			InputSource exp7 = new InputSource(new StringReader(ExpectedOutput5));
			InputSource act7 = new InputSource(new StringReader(ActualOutput5));
			Document ExpDoc7 = dBuilder.parse(exp7);
			Document ActDoc7 = dBuilder.parse(act7);
			
			//case 1
			
			assertTrue(ExpDoc1.getElementsByTagName("SenderReference").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("SenderReference").item(0).getTextContent()));
			assertTrue(ExpDoc1.getElementsByTagName("SenderAddress").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("SenderAddress").item(0).getTextContent()));
			assertTrue(ExpDoc1.getElementsByTagName("ExternalMessageType").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("ExternalMessageType").item(0).getTextContent()));
			assertTrue(ExpDoc1.getElementsByTagName("DestinationAddress").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("DestinationAddress").item(0).getTextContent()));
			assertTrue(ExpDoc1.getElementsByTagName("TransactionReference").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("TransactionReference").item(0).getTextContent()));
		
			
			
			//case 2
			
			assertTrue(ExpDoc1.getElementsByTagName("SenderReference").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("SenderReference").item(0).getTextContent()));
			assertTrue(ExpDoc1.getElementsByTagName("SenderAddress").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("SenderAddress").item(0).getTextContent()));
			assertTrue(ExpDoc1.getElementsByTagName("ExternalMessageType").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("ExternalMessageType").item(0).getTextContent()));
			assertTrue(ExpDoc1.getElementsByTagName("DestinationAddress").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("DestinationAddress").item(0).getTextContent()));
			assertTrue(ExpDoc1.getElementsByTagName("TransactionReference").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("TransactionReference").item(0).getTextContent()));
		
			
			
			//case 3
			
			assertTrue(ExpDoc1.getElementsByTagName("SenderReference").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("SenderReference").item(0).getTextContent()));
			assertTrue(ExpDoc1.getElementsByTagName("SenderAddress").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("SenderAddress").item(0).getTextContent()));
			assertTrue(ExpDoc1.getElementsByTagName("ExternalMessageType").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("ExternalMessageType").item(0).getTextContent()));
			assertTrue(ExpDoc1.getElementsByTagName("DestinationAddress").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("DestinationAddress").item(0).getTextContent()));
			assertTrue(ExpDoc1.getElementsByTagName("TransactionReference").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("TransactionReference").item(0).getTextContent()));
		
			
			
			//case 4
			
			assertTrue(ExpDoc1.getElementsByTagName("SenderReference").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("SenderReference").item(0).getTextContent()));
			assertTrue(ExpDoc1.getElementsByTagName("SenderAddress").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("SenderAddress").item(0).getTextContent()));
			assertTrue(ExpDoc1.getElementsByTagName("ExternalMessageType").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("ExternalMessageType").item(0).getTextContent()));
			assertTrue(ExpDoc1.getElementsByTagName("DestinationAddress").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("DestinationAddress").item(0).getTextContent()));
			assertTrue(ExpDoc1.getElementsByTagName("TransactionReference").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("TransactionReference").item(0).getTextContent()));
		
			
			
			//case 5
			
			assertTrue(ExpDoc1.getElementsByTagName("SenderReference").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("SenderReference").item(0).getTextContent()));
			assertTrue(ExpDoc1.getElementsByTagName("SenderAddress").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("SenderAddress").item(0).getTextContent()));
			assertTrue(ExpDoc1.getElementsByTagName("ExternalMessageType").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("ExternalMessageType").item(0).getTextContent()));
			assertTrue(ExpDoc1.getElementsByTagName("DestinationAddress").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("DestinationAddress").item(0).getTextContent()));
			assertTrue(ExpDoc1.getElementsByTagName("TransactionReference").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("TransactionReference").item(0).getTextContent()));
		
			
			
			//case 7
			
			assertTrue(ExpDoc7.getElementsByTagName("SenderReference").item(0).getTextContent().equalsIgnoreCase(ActDoc7.getElementsByTagName("SenderReference").item(0).getTextContent()));
			assertTrue(ExpDoc7.getElementsByTagName("SenderAddress").item(0).getTextContent().equalsIgnoreCase(ActDoc7.getElementsByTagName("SenderAddress").item(0).getTextContent()));
			assertTrue(ExpDoc7.getElementsByTagName("ExternalMessageType").item(0).getTextContent().equalsIgnoreCase(ActDoc7.getElementsByTagName("ExternalMessageType").item(0).getTextContent()));
			assertTrue(ExpDoc7.getElementsByTagName("DestinationAddress").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("DestinationAddress").item(0).getTextContent()));
			assertTrue(ExpDoc7.getElementsByTagName("TransactionReference").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("TransactionReference").item(0).getTextContent()));
		
			
			
			
			
			
			//	
		} catch (Exception e) {
			System.out.println("Test case failed for MT110");
		}
	}

	
	
	
	
	
	
	
	
}
