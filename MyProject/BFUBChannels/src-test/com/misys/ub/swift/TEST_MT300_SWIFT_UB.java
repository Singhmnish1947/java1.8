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

public class TEST_MT300_SWIFT_UB{

	
	private String InputData1 = null;
	private String InputData2 = null;
	private String InputData3 = null;
	private String InputData4 = null;
	private String InputData5 = null;
	private String InputData6 = null;
	private String ExpectedOutput1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><MeridianMessage MessageFormat=\"StandardXML\" MessageType=\"SWIFT_MT300\" System=\"SWIFT\" Timestamp=\"2017-03-09 13:05:16.226\"><SenderReference>FX000001</SenderReference><B2ReceivingAgentD>UNKNOWN</B2ReceivingAgentD><TradeDate>20170801</TradeDate><B2CurrencyAmount>EUR107,75</B2CurrencyAmount><CommonReference>SBSAJX0775SBZAJJ</CommonReference><ExchangeRate>1,07750000</ExchangeRate><B1ReceivingAgentA>ABNGGB2LXXX</B1ReceivingAgentA><PartyA_A>SBZAZAJJXXX</PartyA_A><PartyB_A>SBSASAJX999</PartyB_A><B4ValueDate>20170803</B4ValueDate><B2DeliveryAgentA>ABNGGB2LXXX</B2DeliveryAgentA><B1CurrencyAmount>GBP100,00</B1CurrencyAmount><RelatedReference>FX000001</RelatedReference><TypeOfOperation>NEWT</TypeOfOperation><HostReference>6</HostReference><SenderAddress>SBZAZAJJXXX</SenderAddress><messageId>0</messageId><ExternalMessageType>MT300</ExternalMessageType><DestinationAddress>SBSASAJX999</DestinationAddress><MeridianMessageType>SWIFT_MT300</MeridianMessageType><InternalMessageType>C</InternalMessageType><HostType>UB</HostType><HostID>UB</HostID><Direction>O</Direction><Priority>N</Priority><Cancel>N</Cancel><SystemArrivalTime>2017/08/07 17:18:51</SystemArrivalTime><Network>SWIFT</Network><LineOfBusiness>UB</LineOfBusiness><MultipleMessageStatus>I</MultipleMessageStatus><TransactionReference>FX000001</TransactionReference><NewSequenceA>@</NewSequenceA><NewSequenceB>@</NewSequenceB><NonDeliverableIndicator>Y</NonDeliverableIndicator><ValuationDate>20170731</ValuationDate><NDFOpenIndicator>Y</NDFOpenIndicator><SettlementCurrency>Y</SettlementCurrency><SettlementRateSource><ASettlementRateSource>ABCD</ASettlementRateSource></SettlementRateSource><RefOpeningConfirmation>E</RefOpeningConfirmation><ClearingSettlementSession>F</ClearingSettlementSession></MeridianMessage>";
	private String ExpectedOutput2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><MeridianMessage MessageFormat=\"StandardXML\" MessageType=\"SWIFT_MT300\" System=\"SWIFT\" Timestamp=\"2017-03-09 13:05:16.226\"><SenderReference>FX000001</SenderReference><B2ReceivingAgentD>UNKNOWN</B2ReceivingAgentD><TradeDate>20170801</TradeDate><B2CurrencyAmount>EUR107,75</B2CurrencyAmount><CommonReference>SBSAJX0775SBZAJJ</CommonReference><ExchangeRate>1,07750000</ExchangeRate><B1ReceivingAgentA>ABNGGB2LXXX</B1ReceivingAgentA><PartyA_A>SBZAZAJJXXX</PartyA_A><PartyB_A>SBSASAJX999</PartyB_A><B4ValueDate>20170803</B4ValueDate><B2DeliveryAgentA>ABNGGB2LXXX</B2DeliveryAgentA><B1CurrencyAmount>GBP100,00</B1CurrencyAmount><RelatedReference>FX000001</RelatedReference><TypeOfOperation>NEWT</TypeOfOperation><HostReference>6</HostReference><SenderAddress>SBZAZAJJXXX</SenderAddress><messageId>0</messageId><ExternalMessageType>MT300</ExternalMessageType><DestinationAddress>SBSASAJX999</DestinationAddress><MeridianMessageType>SWIFT_MT300</MeridianMessageType><InternalMessageType>C</InternalMessageType><HostType>UB</HostType><HostID>UB</HostID><Direction>O</Direction><Priority>N</Priority><Cancel>N</Cancel><SystemArrivalTime>2017/08/07 17:18:51</SystemArrivalTime><Network>SWIFT</Network><LineOfBusiness>UB</LineOfBusiness><MultipleMessageStatus>I</MultipleMessageStatus><TransactionReference>FX000001</TransactionReference><NewSequenceA>@</NewSequenceA><NewSequenceB>@</NewSequenceB><NonDeliverableIndicator>Y</NonDeliverableIndicator><ValuationDate>20170731</ValuationDate><NDFOpenIndicator>Y</NDFOpenIndicator><SettlementCurrency>Y</SettlementCurrency><SettlementRateSource><ASettlementRateSource>ABCD</ASettlementRateSource></SettlementRateSource><RefOpeningConfirmation>E</RefOpeningConfirmation><ClearingSettlementSession>F</ClearingSettlementSession></MeridianMessage>";
	private String ExpectedOutput3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><MeridianMessage MessageFormat=\"StandardXML\" MessageType=\"SWIFT_MT300\" System=\"SWIFT\" Timestamp=\"2017-03-09 13:05:16.226\"><SenderReference>FX000001</SenderReference><B2ReceivingAgentD>UNKNOWN</B2ReceivingAgentD><TradeDate>20170801</TradeDate><B2CurrencyAmount>EUR107,75</B2CurrencyAmount><CommonReference>SBSAJX0775SBZAJJ</CommonReference><ExchangeRate>1,07750000</ExchangeRate><B1ReceivingAgentA>ABNGGB2LXXX</B1ReceivingAgentA><PartyA_A>SBZAZAJJXXX</PartyA_A><PartyB_A>SBSASAJX999</PartyB_A><B4ValueDate>20170803</B4ValueDate><B2DeliveryAgentA>ABNGGB2LXXX</B2DeliveryAgentA><B1CurrencyAmount>GBP100,00</B1CurrencyAmount><RelatedReference>FX000001</RelatedReference><TypeOfOperation>NEWT</TypeOfOperation><HostReference>6</HostReference><SenderAddress>SBZAZAJJXXX</SenderAddress><messageId>0</messageId><ExternalMessageType>MT300</ExternalMessageType><DestinationAddress>SBSASAJX999</DestinationAddress><MeridianMessageType>SWIFT_MT300</MeridianMessageType><InternalMessageType>C</InternalMessageType><HostType>UB</HostType><HostID>UB</HostID><Direction>O</Direction><Priority>N</Priority><Cancel>N</Cancel><SystemArrivalTime>2017/08/07 17:18:51</SystemArrivalTime><Network>SWIFT</Network><LineOfBusiness>UB</LineOfBusiness><MultipleMessageStatus>I</MultipleMessageStatus><TransactionReference>FX000001</TransactionReference><NewSequenceA>@</NewSequenceA><NewSequenceB>@</NewSequenceB><NonDeliverableIndicator>Y</NonDeliverableIndicator><ValuationDate>20170731</ValuationDate><NDFOpenIndicator>Y</NDFOpenIndicator><SettlementCurrency>Y</SettlementCurrency><SettlementRateSource><ASettlementRateSource>ABCD</ASettlementRateSource></SettlementRateSource><RefOpeningConfirmation>E</RefOpeningConfirmation><ClearingSettlementSession>F</ClearingSettlementSession></MeridianMessage>";
	private String ExpectedOutput4 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><MeridianMessage MessageFormat=\"StandardXML\" MessageType=\"SWIFT_MT300\" System=\"SWIFT\" Timestamp=\"2017-03-09 13:05:16.226\"><SenderReference>FX000001</SenderReference><B2ReceivingAgentD>UNKNOWN</B2ReceivingAgentD><TradeDate>20170801</TradeDate><B2CurrencyAmount>EUR107,75</B2CurrencyAmount><CommonReference>SBSAJX0775SBZAJJ</CommonReference><ExchangeRate>1,07750000</ExchangeRate><B1ReceivingAgentA>ABNGGB2LXXX</B1ReceivingAgentA><PartyA_A>SBZAZAJJXXX</PartyA_A><PartyB_A>SBSASAJX999</PartyB_A><B4ValueDate>20170803</B4ValueDate><B2DeliveryAgentA>ABNGGB2LXXX</B2DeliveryAgentA><B1CurrencyAmount>GBP100,00</B1CurrencyAmount><RelatedReference>FX000001</RelatedReference><TypeOfOperation>NEWT</TypeOfOperation><HostReference>6</HostReference><SenderAddress>SBZAZAJJXXX</SenderAddress><messageId>0</messageId><ExternalMessageType>MT300</ExternalMessageType><DestinationAddress>SBSASAJX999</DestinationAddress><MeridianMessageType>SWIFT_MT300</MeridianMessageType><InternalMessageType>C</InternalMessageType><HostType>UB</HostType><HostID>UB</HostID><Direction>O</Direction><Priority>N</Priority><Cancel>N</Cancel><SystemArrivalTime>2017/08/07 17:18:51</SystemArrivalTime><Network>SWIFT</Network><LineOfBusiness>UB</LineOfBusiness><MultipleMessageStatus>I</MultipleMessageStatus><TransactionReference>FX000001</TransactionReference><NewSequenceA>@</NewSequenceA><NewSequenceB>@</NewSequenceB><NonDeliverableIndicator>Y</NonDeliverableIndicator><ValuationDate>20170731</ValuationDate><NDFOpenIndicator>Y</NDFOpenIndicator><SettlementCurrency>Y</SettlementCurrency><SettlementRateSource><ASettlementRateSource>ABCD</ASettlementRateSource></SettlementRateSource><RefOpeningConfirmation>E</RefOpeningConfirmation><ClearingSettlementSession>F</ClearingSettlementSession></MeridianMessage>";
	private String ExpectedOutput5 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><MeridianMessage MessageFormat=\"StandardXML\" MessageType=\"SWIFT_MT300\" System=\"SWIFT\" Timestamp=\"2017-03-09 13:05:16.226\"><SenderReference>FX000001</SenderReference><B2ReceivingAgentD>UNKNOWN</B2ReceivingAgentD><TradeDate>20170801</TradeDate><B2CurrencyAmount>EUR107,75</B2CurrencyAmount><CommonReference>SBSAJX0775SBZAJJ</CommonReference><ExchangeRate>1,07750000</ExchangeRate><B1ReceivingAgentA>ABNGGB2LXXX</B1ReceivingAgentA><PartyA_A>SBZAZAJJXXX</PartyA_A><PartyB_A>SBSASAJX999</PartyB_A><B4ValueDate>20170803</B4ValueDate><B2DeliveryAgentA>ABNGGB2LXXX</B2DeliveryAgentA><B1CurrencyAmount>GBP100,00</B1CurrencyAmount><RelatedReference>FX000001</RelatedReference><TypeOfOperation>NEWT</TypeOfOperation><HostReference>6</HostReference><SenderAddress>SBZAZAJJXXX</SenderAddress><messageId>0</messageId><ExternalMessageType>MT300</ExternalMessageType><DestinationAddress>SBSASAJX999</DestinationAddress><MeridianMessageType>SWIFT_MT300</MeridianMessageType><InternalMessageType>C</InternalMessageType><HostType>UB</HostType><HostID>UB</HostID><Direction>O</Direction><Priority>N</Priority><Cancel>N</Cancel><SystemArrivalTime>2017/08/07 17:18:51</SystemArrivalTime><Network>SWIFT</Network><LineOfBusiness>UB</LineOfBusiness><MultipleMessageStatus>I</MultipleMessageStatus><TransactionReference>FX000001</TransactionReference><NewSequenceA>@</NewSequenceA><NewSequenceB>@</NewSequenceB><NonDeliverableIndicator>Y</NonDeliverableIndicator><ValuationDate>20170731</ValuationDate><NDFOpenIndicator>Y</NDFOpenIndicator><SettlementCurrency>Y</SettlementCurrency><SettlementRateSource><ASettlementRateSource>ABCD</ASettlementRateSource></SettlementRateSource><RefOpeningConfirmation>E</RefOpeningConfirmation><ClearingSettlementSession>F</ClearingSettlementSession></MeridianMessage>";
	private String ExpectedOutput6 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><MeridianMessage MessageFormat=\"StandardXML\" MessageType=\"SWIFT_MT300\" System=\"SWIFT\" Timestamp=\"2017-03-09 13:05:16.226\"><SenderReference>FX000001</SenderReference><B2ReceivingAgentD>UNKNOWN</B2ReceivingAgentD><TradeDate>20170801</TradeDate><B2CurrencyAmount>EUR107,75</B2CurrencyAmount><CommonReference>SBSAJX0775SBZAJJ</CommonReference><ExchangeRate>1,07750000</ExchangeRate><B1ReceivingAgentA>ABNGGB2LXXX</B1ReceivingAgentA><PartyA_A>SBZAZAJJXXX</PartyA_A><PartyB_A>SBSASAJX999</PartyB_A><B4ValueDate>20170803</B4ValueDate><B2DeliveryAgentA>ABNGGB2LXXX</B2DeliveryAgentA><B1CurrencyAmount>GBP100,00</B1CurrencyAmount><RelatedReference>FX000001</RelatedReference><TypeOfOperation>NEWT</TypeOfOperation><HostReference>6</HostReference><SenderAddress>SBZAZAJJXXX</SenderAddress><messageId>0</messageId><ExternalMessageType>MT300</ExternalMessageType><DestinationAddress>SBSASAJX999</DestinationAddress><MeridianMessageType>SWIFT_MT300</MeridianMessageType><InternalMessageType>C</InternalMessageType><HostType>UB</HostType><HostID>UB</HostID><Direction>O</Direction><Priority>N</Priority><Cancel>N</Cancel><SystemArrivalTime>2017/08/07 17:18:51</SystemArrivalTime><Network>SWIFT</Network><LineOfBusiness>UB</LineOfBusiness><MultipleMessageStatus>I</MultipleMessageStatus><TransactionReference>FX000001</TransactionReference><NewSequenceA>@</NewSequenceA><NewSequenceB>@</NewSequenceB><NonDeliverableIndicator>Y</NonDeliverableIndicator><ValuationDate>20170731</ValuationDate><NDFOpenIndicator>Y</NDFOpenIndicator><SettlementCurrency>Y</SettlementCurrency><SettlementRateSource><ASettlementRateSource>ABCD</ASettlementRateSource></SettlementRateSource><RefOpeningConfirmation>E</RefOpeningConfirmation><ClearingSettlementSession>F</ClearingSettlementSession></MeridianMessage>";

	
			
	@Before
	public void setUp() throws Exception {
		
		InputData1=	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		+"<UB_MT300>"
		+"<BlockTradeIndicator/>"
		+"<b2DeliveryAgent>ABNGGB2LXXX</b2DeliveryAgent>"
		+"<b2IntermediaryOption/>"
		+"<ContactInformation/>"
		+"<dealingBranchPartyB/>"
		+"<CounterPartysReference/>"
		+"<beneficiaryOption/>"
		+"<dealingBranchPartyA/>"
		+"<SenderReference>FX000001</SenderReference>"
		+"<b2ReceivingAgentOption>D</b2ReceivingAgentOption>"
		+"<b2ReceivingAgent>UNKNOWN</b2ReceivingAgent>"
		+"<DBeneficiaryD/>"
		+"<DealingMethod/>"
		+"<BrokersReference/>"
		+"<TradeDate>2017-08-01</TradeDate>"
		+"<B2CurrencyAmount>EUR107.75</B2CurrencyAmount>"
		+"<b1DeliveryAgent/>"
		+"<dealingBranchPartyBOption/>"
		+"<CommonReference>SBSAJX0775SBZAJJ</CommonReference>"
		+"<dealingBranchPartyAOption/>"
		+"<fundOrBenCustOption/>"
		+"<b2Intermediary/>"
		+"<partyB>SBSASAJX999</partyB>"
		+"<NumberOfSettlements/>"
		+"<partyA>SBZAZAJJXXX</partyA>"
		+"<b1IntermediaryOption/>"
		+"<fundOrBeneficaryCustomer/>"
		+"<brokerIDOption/>"
		+"<BrokersCommission/>"
		+"<ExchangeRate>1.07750000</ExchangeRate>"
		+"<b1ReceivingAgentOption>A</b1ReceivingAgentOption>"
		+"<b1ReceivingAgent>$ABNGGB2LXXX</b1ReceivingAgent>"
		+"<partyAOption>A</partyAOption>"
		+"<partyBOption>A</partyBOption>"
		+"<B4ValueDate>2017-08-03</B4ValueDate>"
		+"<b1Intermediary/>"
		+"<TermsAndConditions/>"
		+"<SplitSettlementIndicator/>"
		+"<ScopeOfOperation/>"
		+"<brokerID/>"
		+"<b2DeliveryAgentOption>A</b2DeliveryAgentOption>"
		+"<B1CurrencyAmount>GBP100.00</B1CurrencyAmount>"
		+"<RelatedReference>FX000001</RelatedReference>"
		+"<TypeOfOperation>NEWT</TypeOfOperation>"
		+"<b1DeliveryAgentOption/>"
		+"<SendersToReceiversInfo/>"
		+"<HostReference>6</HostReference>"
		+"<verificationRequired/>"
		+"<multipleHold/>"
		+"<SenderAddress>SBZAZAJJXXX</SenderAddress>"
		+"<messageId>0</messageId>"
		+"<internalRef/>"
		+"<ExternalMessageType>MT300</ExternalMessageType>"
		+"<BusinessEntity/>"
		+"<DestinationAddress>SBSASAJX999</DestinationAddress>"
		+"<CancellationAction/>"
		+"<NonDeliverableIndicator>Y</NonDeliverableIndicator>"
		+"<valuationDate>2017-07-31</valuationDate>"
		+"<NDFOpenIndicator>Y</NDFOpenIndicator>"
		+"<settlementCurrency>Y</settlementCurrency>"
		+"<ASettlementRateSource>ABCD</ASettlementRateSource>"
		+"<refOpeningConfirmation>E</refOpeningConfirmation>"
		+"<clearingSettlementSession>F</clearingSettlementSession>"
		+"</UB_MT300>";

		
		InputData2=	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<UB_MT300>"
				+"<BlockTradeIndicator/>"
				+"<b2DeliveryAgent>ABNGGB2LXXX</b2DeliveryAgent>"
				+"<b2IntermediaryOption/>"
				+"<ContactInformation/>"
				+"<dealingBranchPartyB/>"
				+"<CounterPartysReference/>"
				+"<beneficiaryOption/>"
				+"<dealingBranchPartyA/>"
				+"<SenderReference>FX000001</SenderReference>"
				+"<b2ReceivingAgentOption>D</b2ReceivingAgentOption>"
				+"<b2ReceivingAgent>UNKNOWN</b2ReceivingAgent>"
				+"<DBeneficiaryD/>"
				+"<DealingMethod/>"
				+"<BrokersReference/>"
				+"<TradeDate>2017-08-01</TradeDate>"
				+"<B2CurrencyAmount>EUR107.75</B2CurrencyAmount>"
				+"<b1DeliveryAgent/>"
				+"<dealingBranchPartyBOption/>"
				+"<CommonReference>SBSAJX0775SBZAJJ</CommonReference>"
				+"<dealingBranchPartyAOption/>"
				+"<fundOrBenCustOption/>"
				+"<b2Intermediary/>"
				+"<partyB>SBSASAJX999</partyB>"
				+"<NumberOfSettlements/>"
				+"<partyA>SBZAZAJJXXX</partyA>"
				+"<b1IntermediaryOption/>"
				+"<fundOrBeneficaryCustomer/>"
				+"<brokerIDOption/>"
				+"<BrokersCommission/>"
				+"<ExchangeRate>1.07750000</ExchangeRate>"
				+"<b1ReceivingAgentOption>A</b1ReceivingAgentOption>"
				+"<b1ReceivingAgent>$ABNGGB2LXXX</b1ReceivingAgent>"
				+"<partyAOption>A</partyAOption>"
				+"<partyBOption>A</partyBOption>"
				+"<B4ValueDate>2017-08-03</B4ValueDate>"
				+"<b1Intermediary/>"
				+"<TermsAndConditions/>"
				+"<SplitSettlementIndicator/>"
				+"<ScopeOfOperation/>"
				+"<brokerID/>"
				+"<b2DeliveryAgentOption>A</b2DeliveryAgentOption>"
				+"<B1CurrencyAmount>GBP100.00</B1CurrencyAmount>"
				+"<RelatedReference>FX000001</RelatedReference>"
				+"<TypeOfOperation>NEWT</TypeOfOperation>"
				+"<b1DeliveryAgentOption/>"
				+"<SendersToReceiversInfo/>"
				+"<HostReference>6</HostReference>"
				+"<verificationRequired/>"
				+"<multipleHold/>"
				+"<SenderAddress>SBZAZAJJXXX</SenderAddress>"
				+"<messageId>0</messageId>"
				+"<internalRef/>"
				+"<ExternalMessageType>MT300</ExternalMessageType>"
				+"<BusinessEntity/>"
				+"<DestinationAddress>SBSASAJX999</DestinationAddress>"
				+"<CancellationAction/>"
				+"<NonDeliverableIndicator>Y</NonDeliverableIndicator>"
				+"<valuationDate>2017-07-31</valuationDate>"
				+"<NDFOpenIndicator>Y</NDFOpenIndicator>"
				+"<settlementCurrency>Y</settlementCurrency>"
				+"<ASettlementRateSource>ABCD</ASettlementRateSource>"
				+"<refOpeningConfirmation>E</refOpeningConfirmation>"
				+"<clearingSettlementSession>F</clearingSettlementSession>"
				+"</UB_MT300>";
		
		InputData3=	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<UB_MT300>"
				+"<BlockTradeIndicator/>"
				+"<b2DeliveryAgent>ABNGGB2LXXX</b2DeliveryAgent>"
				+"<b2IntermediaryOption/>"
				+"<ContactInformation/>"
				+"<dealingBranchPartyB/>"
				+"<CounterPartysReference/>"
				+"<beneficiaryOption/>"
				+"<dealingBranchPartyA/>"
				+"<SenderReference>FX000001</SenderReference>"
				+"<b2ReceivingAgentOption>D</b2ReceivingAgentOption>"
				+"<b2ReceivingAgent>UNKNOWN</b2ReceivingAgent>"
				+"<DBeneficiaryD/>"
				+"<DealingMethod/>"
				+"<BrokersReference/>"
				+"<TradeDate>2017-08-01</TradeDate>"
				+"<B2CurrencyAmount>EUR107.75</B2CurrencyAmount>"
				+"<b1DeliveryAgent/>"
				+"<dealingBranchPartyBOption/>"
				+"<CommonReference>SBSAJX0775SBZAJJ</CommonReference>"
				+"<dealingBranchPartyAOption/>"
				+"<fundOrBenCustOption/>"
				+"<b2Intermediary/>"
				+"<partyB>SBSASAJX999</partyB>"
				+"<NumberOfSettlements/>"
				+"<partyA>SBZAZAJJXXX</partyA>"
				+"<b1IntermediaryOption/>"
				+"<fundOrBeneficaryCustomer/>"
				+"<brokerIDOption/>"
				+"<BrokersCommission/>"
				+"<ExchangeRate>1.07750000</ExchangeRate>"
				+"<b1ReceivingAgentOption>A</b1ReceivingAgentOption>"
				+"<b1ReceivingAgent>$ABNGGB2LXXX</b1ReceivingAgent>"
				+"<partyAOption>A</partyAOption>"
				+"<partyBOption>A</partyBOption>"
				+"<B4ValueDate>2017-08-03</B4ValueDate>"
				+"<b1Intermediary/>"
				+"<TermsAndConditions/>"
				+"<SplitSettlementIndicator/>"
				+"<ScopeOfOperation/>"
				+"<brokerID/>"
				+"<b2DeliveryAgentOption>A</b2DeliveryAgentOption>"
				+"<B1CurrencyAmount>GBP100.00</B1CurrencyAmount>"
				+"<RelatedReference>FX000001</RelatedReference>"
				+"<TypeOfOperation>NEWT</TypeOfOperation>"
				+"<b1DeliveryAgentOption/>"
				+"<SendersToReceiversInfo/>"
				+"<HostReference>6</HostReference>"
				+"<verificationRequired/>"
				+"<multipleHold/>"
				+"<SenderAddress>SBZAZAJJXXX</SenderAddress>"
				+"<messageId>0</messageId>"
				+"<internalRef/>"
				+"<ExternalMessageType>MT300</ExternalMessageType>"
				+"<BusinessEntity/>"
				+"<DestinationAddress>SBSASAJX999</DestinationAddress>"
				+"<CancellationAction/>"
				+"<NonDeliverableIndicator>Y</NonDeliverableIndicator>"
				+"<valuationDate>2017-07-31</valuationDate>"
				+"<NDFOpenIndicator>Y</NDFOpenIndicator>"
				+"<settlementCurrency>Y</settlementCurrency>"
				+"<ASettlementRateSource>ABCD</ASettlementRateSource>"
				+"<refOpeningConfirmation>E</refOpeningConfirmation>"
				+"<clearingSettlementSession>F</clearingSettlementSession>"
				+"</UB_MT300>";
		
		InputData4=	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<UB_MT300>"
				+"<BlockTradeIndicator/>"
				+"<b2DeliveryAgent>ABNGGB2LXXX</b2DeliveryAgent>"
				+"<b2IntermediaryOption/>"
				+"<ContactInformation/>"
				+"<dealingBranchPartyB/>"
				+"<CounterPartysReference/>"
				+"<beneficiaryOption/>"
				+"<dealingBranchPartyA/>"
				+"<SenderReference>FX000001</SenderReference>"
				+"<b2ReceivingAgentOption>D</b2ReceivingAgentOption>"
				+"<b2ReceivingAgent>UNKNOWN</b2ReceivingAgent>"
				+"<DBeneficiaryD/>"
				+"<DealingMethod/>"
				+"<BrokersReference/>"
				+"<TradeDate>2017-08-01</TradeDate>"
				+"<B2CurrencyAmount>EUR107.75</B2CurrencyAmount>"
				+"<b1DeliveryAgent/>"
				+"<dealingBranchPartyBOption/>"
				+"<CommonReference>SBSAJX0775SBZAJJ</CommonReference>"
				+"<dealingBranchPartyAOption/>"
				+"<fundOrBenCustOption/>"
				+"<b2Intermediary/>"
				+"<partyB>SBSASAJX999</partyB>"
				+"<NumberOfSettlements/>"
				+"<partyA>SBZAZAJJXXX</partyA>"
				+"<b1IntermediaryOption/>"
				+"<fundOrBeneficaryCustomer/>"
				+"<brokerIDOption/>"
				+"<BrokersCommission/>"
				+"<ExchangeRate>1.07750000</ExchangeRate>"
				+"<b1ReceivingAgentOption>A</b1ReceivingAgentOption>"
				+"<b1ReceivingAgent>$ABNGGB2LXXX</b1ReceivingAgent>"
				+"<partyAOption>A</partyAOption>"
				+"<partyBOption>A</partyBOption>"
				+"<B4ValueDate>2017-08-03</B4ValueDate>"
				+"<b1Intermediary/>"
				+"<TermsAndConditions/>"
				+"<SplitSettlementIndicator/>"
				+"<ScopeOfOperation/>"
				+"<brokerID/>"
				+"<b2DeliveryAgentOption>A</b2DeliveryAgentOption>"
				+"<B1CurrencyAmount>GBP100.00</B1CurrencyAmount>"
				+"<RelatedReference>FX000001</RelatedReference>"
				+"<TypeOfOperation>NEWT</TypeOfOperation>"
				+"<b1DeliveryAgentOption/>"
				+"<SendersToReceiversInfo/>"
				+"<HostReference>6</HostReference>"
				+"<verificationRequired/>"
				+"<multipleHold/>"
				+"<SenderAddress>SBZAZAJJXXX</SenderAddress>"
				+"<messageId>0</messageId>"
				+"<internalRef/>"
				+"<ExternalMessageType>MT300</ExternalMessageType>"
				+"<BusinessEntity/>"
				+"<DestinationAddress>SBSASAJX999</DestinationAddress>"
				+"<CancellationAction/>"
				+"<NonDeliverableIndicator>Y</NonDeliverableIndicator>"
				+"<valuationDate>2017-07-31</valuationDate>"
				+"<NDFOpenIndicator>Y</NDFOpenIndicator>"
				+"<settlementCurrency>Y</settlementCurrency>"
				+"<ASettlementRateSource>ABCD</ASettlementRateSource>"
				+"<refOpeningConfirmation>E</refOpeningConfirmation>"
				+"<clearingSettlementSession>F</clearingSettlementSession>"
				+"</UB_MT300>";
		
		InputData5=	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<UB_MT300>"
				+"<BlockTradeIndicator/>"
				+"<b2DeliveryAgent>ABNGGB2LXXX</b2DeliveryAgent>"
				+"<b2IntermediaryOption/>"
				+"<ContactInformation/>"
				+"<dealingBranchPartyB/>"
				+"<CounterPartysReference/>"
				+"<beneficiaryOption/>"
				+"<dealingBranchPartyA/>"
				+"<SenderReference>FX000001</SenderReference>"
				+"<b2ReceivingAgentOption>D</b2ReceivingAgentOption>"
				+"<b2ReceivingAgent>UNKNOWN</b2ReceivingAgent>"
				+"<DBeneficiaryD/>"
				+"<DealingMethod/>"
				+"<BrokersReference/>"
				+"<TradeDate>2017-08-01</TradeDate>"
				+"<B2CurrencyAmount>EUR107.75</B2CurrencyAmount>"
				+"<b1DeliveryAgent/>"
				+"<dealingBranchPartyBOption/>"
				+"<CommonReference>SBSAJX0775SBZAJJ</CommonReference>"
				+"<dealingBranchPartyAOption/>"
				+"<fundOrBenCustOption/>"
				+"<b2Intermediary/>"
				+"<partyB>SBSASAJX999</partyB>"
				+"<NumberOfSettlements/>"
				+"<partyA>SBZAZAJJXXX</partyA>"
				+"<b1IntermediaryOption/>"
				+"<fundOrBeneficaryCustomer/>"
				+"<brokerIDOption/>"
				+"<BrokersCommission/>"
				+"<ExchangeRate>1.07750000</ExchangeRate>"
				+"<b1ReceivingAgentOption>A</b1ReceivingAgentOption>"
				+"<b1ReceivingAgent>$ABNGGB2LXXX</b1ReceivingAgent>"
				+"<partyAOption>A</partyAOption>"
				+"<partyBOption>A</partyBOption>"
				+"<B4ValueDate>2017-08-03</B4ValueDate>"
				+"<b1Intermediary/>"
				+"<TermsAndConditions/>"
				+"<SplitSettlementIndicator/>"
				+"<ScopeOfOperation/>"
				+"<brokerID/>"
				+"<b2DeliveryAgentOption>A</b2DeliveryAgentOption>"
				+"<B1CurrencyAmount>GBP100.00</B1CurrencyAmount>"
				+"<RelatedReference>FX000001</RelatedReference>"
				+"<TypeOfOperation>NEWT</TypeOfOperation>"
				+"<b1DeliveryAgentOption/>"
				+"<SendersToReceiversInfo/>"
				+"<HostReference>6</HostReference>"
				+"<verificationRequired/>"
				+"<multipleHold/>"
				+"<SenderAddress>SBZAZAJJXXX</SenderAddress>"
				+"<messageId>0</messageId>"
				+"<internalRef/>"
				+"<ExternalMessageType>MT300</ExternalMessageType>"
				+"<BusinessEntity/>"
				+"<DestinationAddress>SBSASAJX999</DestinationAddress>"
				+"<CancellationAction/>"
				+"<NonDeliverableIndicator>Y</NonDeliverableIndicator>"
				+"<valuationDate>2017-07-31</valuationDate>"
				+"<NDFOpenIndicator>Y</NDFOpenIndicator>"
				+"<settlementCurrency>Y</settlementCurrency>"
				+"<ASettlementRateSource>ABCD</ASettlementRateSource>"
				+"<refOpeningConfirmation>E</refOpeningConfirmation>"
				+"<clearingSettlementSession>F</clearingSettlementSession>"
				+"</UB_MT300>";
		
		InputData6=	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<UB_MT300>"
				+"<BlockTradeIndicator/>"
				+"<b2DeliveryAgent>ABNGGB2LXXX</b2DeliveryAgent>"
				+"<b2IntermediaryOption/>"
				+"<ContactInformation/>"
				+"<dealingBranchPartyB/>"
				+"<CounterPartysReference/>"
				+"<beneficiaryOption/>"
				+"<dealingBranchPartyA/>"
				+"<SenderReference>FX000001</SenderReference>"
				+"<b2ReceivingAgentOption>D</b2ReceivingAgentOption>"
				+"<b2ReceivingAgent>UNKNOWN</b2ReceivingAgent>"
				+"<DBeneficiaryD/>"
				+"<DealingMethod/>"
				+"<BrokersReference/>"
				+"<TradeDate>2017-08-01</TradeDate>"
				+"<B2CurrencyAmount>EUR107.75</B2CurrencyAmount>"
				+"<b1DeliveryAgent/>"
				+"<dealingBranchPartyBOption/>"
				+"<CommonReference>SBSAJX0775SBZAJJ</CommonReference>"
				+"<dealingBranchPartyAOption/>"
				+"<fundOrBenCustOption/>"
				+"<b2Intermediary/>"
				+"<partyB>SBSASAJX999</partyB>"
				+"<NumberOfSettlements/>"
				+"<partyA>SBZAZAJJXXX</partyA>"
				+"<b1IntermediaryOption/>"
				+"<fundOrBeneficaryCustomer/>"
				+"<brokerIDOption/>"
				+"<BrokersCommission/>"
				+"<ExchangeRate>1.07750000</ExchangeRate>"
				+"<b1ReceivingAgentOption>A</b1ReceivingAgentOption>"
				+"<b1ReceivingAgent>$ABNGGB2LXXX</b1ReceivingAgent>"
				+"<partyAOption>A</partyAOption>"
				+"<partyBOption>A</partyBOption>"
				+"<B4ValueDate>2017-08-03</B4ValueDate>"
				+"<b1Intermediary/>"
				+"<TermsAndConditions/>"
				+"<SplitSettlementIndicator/>"
				+"<ScopeOfOperation/>"
				+"<brokerID/>"
				+"<b2DeliveryAgentOption>A</b2DeliveryAgentOption>"
				+"<B1CurrencyAmount>GBP100.00</B1CurrencyAmount>"
				+"<RelatedReference>FX000001</RelatedReference>"
				+"<TypeOfOperation>NEWT</TypeOfOperation>"
				+"<b1DeliveryAgentOption/>"
				+"<SendersToReceiversInfo/>"
				+"<HostReference>6</HostReference>"
				+"<verificationRequired/>"
				+"<multipleHold/>"
				+"<SenderAddress>SBZAZAJJXXX</SenderAddress>"
				+"<messageId>0</messageId>"
				+"<internalRef/>"
				+"<ExternalMessageType>MT300</ExternalMessageType>"
				+"<BusinessEntity/>"
				+"<DestinationAddress>SBSASAJX999</DestinationAddress>"
				+"<CancellationAction/>"
				+"<NonDeliverableIndicator>Y</NonDeliverableIndicator>"
				+"<valuationDate>2017-07-31</valuationDate>"
				+"<NDFOpenIndicator>Y</NDFOpenIndicator>"
				+"<settlementCurrency>Y</settlementCurrency>"
				+"<ASettlementRateSource>ABCD</ASettlementRateSource>"
				+"<refOpeningConfirmation>E</refOpeningConfirmation>"
				+"<clearingSettlementSession>F</clearingSettlementSession>"
				+"</UB_MT300>";
		
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMT300() {
		try {
			
			String ActualOutput1 = UB_MT300FileCreator.MT300_Transform(InputData1);
			String ActualOutput2 = UB_MT300FileCreator.MT300_Transform(InputData2);
			String ActualOutput3 = UB_MT300FileCreator.MT300_Transform(InputData3);
			String ActualOutput4 = UB_MT300FileCreator.MT300_Transform(InputData4);
			String ActualOutput5 = UB_MT300FileCreator.MT300_Transform(InputData5);
			
						
			
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
			
			
//case 1
			
			assertTrue(ExpDoc1.getElementsByTagName("NonDeliverableIndicator").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("NonDeliverableIndicator").item(0).getTextContent()));
			assertTrue(ExpDoc1.getElementsByTagName("ValuationDate").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("ValuationDate").item(0).getTextContent()));
			assertTrue(ExpDoc1.getElementsByTagName("NDFOpenIndicator").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("NDFOpenIndicator").item(0).getTextContent()));
			assertTrue(ExpDoc1.getElementsByTagName("RefOpeningConfirmation").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("RefOpeningConfirmation").item(0).getTextContent()));
			assertTrue(ExpDoc1.getElementsByTagName("SettlementCurrency").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("SettlementCurrency").item(0).getTextContent()));
			assertTrue(ExpDoc1.getElementsByTagName("ClearingSettlementSession").item(0).getTextContent().equalsIgnoreCase(ActDoc1.getElementsByTagName("ClearingSettlementSession").item(0).getTextContent()));
			
			
			
//case 2
			
			assertTrue(ExpDoc2.getElementsByTagName("NonDeliverableIndicator").item(0).getTextContent().equalsIgnoreCase(ActDoc2.getElementsByTagName("NonDeliverableIndicator").item(0).getTextContent()));
			assertTrue(ExpDoc2.getElementsByTagName("ValuationDate").item(0).getTextContent().equalsIgnoreCase(ActDoc2.getElementsByTagName("ValuationDate").item(0).getTextContent()));
			assertTrue(ExpDoc2.getElementsByTagName("NDFOpenIndicator").item(0).getTextContent().equalsIgnoreCase(ActDoc2.getElementsByTagName("NDFOpenIndicator").item(0).getTextContent()));
			assertTrue(ExpDoc2.getElementsByTagName("RefOpeningConfirmation").item(0).getTextContent().equalsIgnoreCase(ActDoc2.getElementsByTagName("RefOpeningConfirmation").item(0).getTextContent()));
			assertTrue(ExpDoc2.getElementsByTagName("SettlementCurrency").item(0).getTextContent().equalsIgnoreCase(ActDoc2.getElementsByTagName("SettlementCurrency").item(0).getTextContent()));
			assertTrue(ExpDoc2.getElementsByTagName("ClearingSettlementSession").item(0).getTextContent().equalsIgnoreCase(ActDoc2.getElementsByTagName("ClearingSettlementSession").item(0).getTextContent()));
			
			
			
			
//case 3
			
			assertTrue(ExpDoc3.getElementsByTagName("NonDeliverableIndicator").item(0).getTextContent().equalsIgnoreCase(ActDoc3.getElementsByTagName("NonDeliverableIndicator").item(0).getTextContent()));
			assertTrue(ExpDoc3.getElementsByTagName("ValuationDate").item(0).getTextContent().equalsIgnoreCase(ActDoc3.getElementsByTagName("ValuationDate").item(0).getTextContent()));
			assertTrue(ExpDoc3.getElementsByTagName("NDFOpenIndicator").item(0).getTextContent().equalsIgnoreCase(ActDoc3.getElementsByTagName("NDFOpenIndicator").item(0).getTextContent()));
			assertTrue(ExpDoc3.getElementsByTagName("RefOpeningConfirmation").item(0).getTextContent().equalsIgnoreCase(ActDoc3.getElementsByTagName("RefOpeningConfirmation").item(0).getTextContent()));
			assertTrue(ExpDoc3.getElementsByTagName("SettlementCurrency").item(0).getTextContent().equalsIgnoreCase(ActDoc3.getElementsByTagName("SettlementCurrency").item(0).getTextContent()));
			assertTrue(ExpDoc3.getElementsByTagName("ClearingSettlementSession").item(0).getTextContent().equalsIgnoreCase(ActDoc3.getElementsByTagName("ClearingSettlementSession").item(0).getTextContent()));
			
			
			
//case 4
			
			assertTrue(ExpDoc4.getElementsByTagName("NonDeliverableIndicator").item(0).getTextContent().equalsIgnoreCase(ActDoc4.getElementsByTagName("NonDeliverableIndicator").item(0).getTextContent()));
			assertTrue(ExpDoc4.getElementsByTagName("ValuationDate").item(0).getTextContent().equalsIgnoreCase(ActDoc4.getElementsByTagName("ValuationDate").item(0).getTextContent()));
			assertTrue(ExpDoc4.getElementsByTagName("NDFOpenIndicator").item(0).getTextContent().equalsIgnoreCase(ActDoc4.getElementsByTagName("NDFOpenIndicator").item(0).getTextContent()));
			assertTrue(ExpDoc4.getElementsByTagName("RefOpeningConfirmation").item(0).getTextContent().equalsIgnoreCase(ActDoc4.getElementsByTagName("RefOpeningConfirmation").item(0).getTextContent()));
			assertTrue(ExpDoc4.getElementsByTagName("SettlementCurrency").item(0).getTextContent().equalsIgnoreCase(ActDoc4.getElementsByTagName("SettlementCurrency").item(0).getTextContent()));
			assertTrue(ExpDoc4.getElementsByTagName("ClearingSettlementSession").item(0).getTextContent().equalsIgnoreCase(ActDoc4.getElementsByTagName("ClearingSettlementSession").item(0).getTextContent()));
			
			
//case 5
			
			assertTrue(ExpDoc5.getElementsByTagName("NonDeliverableIndicator").item(0).getTextContent().equalsIgnoreCase(ActDoc5.getElementsByTagName("NonDeliverableIndicator").item(0).getTextContent()));
			assertTrue(ExpDoc5.getElementsByTagName("ValuationDate").item(0).getTextContent().equalsIgnoreCase(ActDoc5.getElementsByTagName("ValuationDate").item(0).getTextContent()));
			assertTrue(ExpDoc5.getElementsByTagName("NDFOpenIndicator").item(0).getTextContent().equalsIgnoreCase(ActDoc5.getElementsByTagName("NDFOpenIndicator").item(0).getTextContent()));
			assertTrue(ExpDoc5.getElementsByTagName("RefOpeningConfirmation").item(0).getTextContent().equalsIgnoreCase(ActDoc5.getElementsByTagName("RefOpeningConfirmation").item(0).getTextContent()));
			assertTrue(ExpDoc5.getElementsByTagName("SettlementCurrency").item(0).getTextContent().equalsIgnoreCase(ActDoc5.getElementsByTagName("SettlementCurrency").item(0).getTextContent()));
			assertTrue(ExpDoc5.getElementsByTagName("ClearingSettlementSession").item(0).getTextContent().equalsIgnoreCase(ActDoc5.getElementsByTagName("ClearingSettlementSession").item(0).getTextContent()));
			
		
			
			
			
			//	
		} catch (Exception e) {
			System.out.println("Test case failed for MT300");
		}
	}
}
	
	
