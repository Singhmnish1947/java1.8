package com.misys.ub.dc.common;

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.trapedza.bankfusion.servercommon.microflow.FBPMicroflowServiceInvoker;

import bf.com.misys.party.ws.RsPayload;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FBPMicroflowServiceInvoker.class, MessageRouterMethods.class, MessageProducerUtil.class })
public class MessageRouterMethodsTest {
	private MessageRouterMethods messageRouterMethods;
	private String jsonInputString, jsonInputStringSupport;
	private String expectRqParam, expectRqParamSupport;
	private RsPayload rsPayload;
	private FBPMicroflowServiceInvoker invoker;
	private HashMap<String, Object> outMap;
	private Log logger;

	@Before
	public void setUp() throws Exception {
		logger = LogFactory.getLog(MessageRouterMethodsTest.class.getName());
		messageRouterMethods = new MessageRouterMethods();
		jsonInputString = "{\"msgType\":\"CHANGE_PERSONAL_DETAILS\",\"CustomerId\":\"101\",\"msgId\":\"111\",\"Type\":\"\",\"Country\":\"\",\"PostalCode\":\"\",\"City\":\"\",\"Address\":\"\",\"State\":\"\",\"Addresses\":[{\"Type\":\"3\",\"IsDefaultAddress\":\"true\",\"Country\":\"Hungary\",\"PostalCode\":\"H-3530\",\"City\":\"Miskolc\",\"Address\":\"Szechenyi Str. 70.\",\"State\":\"\"},{\"Type\":\"HM\",\"IsDefaultAddress\":\"false\",\"Country\":\"Hungary\",\"PostalCode\":\"H-3530\",\"City\":\"Miskolc\",\"Address\":\"Hosok Sqr. 70.\",\"State\":\"\"}],\"PhoneNumber\":\"\",\"SecondaryPhoneNumber\":\"\",\"DialNumbers\":[{\"DialNumberType\":\"MOBILE_NUMBER\",\"ISDCode\":\"36\",\"PhoneNumber\":\"441234567\"},{\"DialNumberType\":\"PHONE_NUMBER\",\"ISDCode\":\"36\",\"PhoneNumber\":\"441234568\"}],\"Email\":\"luke@skywalker.net\",\"TelephoneContactType\":\"DAYTIME\",\"SMSContactType\":\"BUSINESSMOBILE\",\"FaxContactType\":\"FAXPERSONAL\",\"EmailContactType\":\"BUSINESS\",\"ISDCode\":\"+91\",\"CountryCode\":\"IND\",\"AddressType\":\"DEF\"}";
		jsonInputStringSupport = "{\"msgType\":\"CHANGE_PERSONAL_DETAILS\",\"CustomerId\":\"101\",\"msgId\":\"111\",\"Type\":\"permanent\",\"Country\":\"Hungary\",\"PostalCode\":\"H-1062\",\"City\":\"Budapest\",\"Address\":\"8-10. Aradi str.\",\"State\":\"Pest\",\"Addresses\":[],\"PhoneNumber\":\"3613730623\",\"SecondaryPhoneNumber\":\"3613730625\",\"DialNumbers\":[],\"Email\":\"DCTestingMain@misys.com\",\"TelephoneContactType\":\"DAYTIME\",\"SMSContactType\":\"BUSINESSMOBILE\",\"FaxContactType\":\"FAXPERSONAL\",\"EmailContactType\":\"BUSINESS\",\"ISDCode\":\"+91\",\"CountryCode\":\"IND\",\"AddressType\":\"DEF\"}";
		expectRqParam = "UNIQUE_ID=First13;PT_PFN_Party#PARTYID=101;{PT_PFN_AddressLink#ADDRESSTYPE=3;PT_PFN_Address#COUNTRYCODE=Hungary;PT_PFN_Address#POSTALCODE=H-3530;PT_PFN_Address#TOWNORCITY=Miskolc;PT_PFN_Address#ADDRESSLINE1=Szechenyi Str. 70.;PT_PFN_Address#ADDRESSLINE10=;PT_PFN_AddressLink#ISDEAFULTADDRESS=Y};{PT_PFN_AddressLink#ADDRESSTYPE=HM;PT_PFN_Address#COUNTRYCODE=Hungary;PT_PFN_Address#POSTALCODE=H-3530;PT_PFN_Address#TOWNORCITY=Miskolc;PT_PFN_Address#ADDRESSLINE1=Hosok Sqr. 70.;PT_PFN_Address#ADDRESSLINE10=;PT_PFN_AddressLink#ISDEAFULTADDRESS=N};{PT_PFN_PartyContactDetails#ISDCODE=36;PT_PFN_PartyContactDetails#CONTACTTYPE=DAYTIME;PT_PFN_PartyContactDetails#CONTACTVALUE=441234568;PT_PFN_PartyContactDetails#CONTACTMETHOD=TELEPHONE};{PT_PFN_PartyContactDetails#ISDCODE=36;PT_PFN_PartyContactDetails#CONTACTTYPE=BUSINESSMOBILE;PT_PFN_PartyContactDetails#CONTACTVALUE=441234567;PT_PFN_PartyContactDetails#CONTACTMETHOD=SMS};{PT_PFN_PartyContactDetails#CONTACTTYPE=BUSINESS;PT_PFN_PartyContactDetails#CONTACTVALUE=luke@skywalker.net;PT_PFN_PartyContactDetails#CONTACTMETHOD=EMAIL}";
		expectRqParamSupport = "UNIQUE_ID=First13;PT_PFN_Party#PARTYID=101;{PT_PFN_AddressLink#ADDRESSTYPE=DEF;PT_PFN_Address#COUNTRYCODE=IND;PT_PFN_Address#POSTALCODE=H-1062;PT_PFN_Address#TOWNORCITY=Budapest;PT_PFN_Address#ADDRESSLINE1=8-10. Aradi str.;PT_PFN_Address#ADDRESSLINE10=Pest;PT_PFN_AddressLink#ISDEAFULTADDRESS=Y};{PT_PFN_PartyContactDetails#ISDCODE=+91;PT_PFN_PartyContactDetails#CONTACTTYPE=DAYTIME;PT_PFN_PartyContactDetails#CONTACTVALUE=3613730623;PT_PFN_PartyContactDetails#CONTACTMETHOD=TELEPHONE};{PT_PFN_PartyContactDetails#ISDCODE=+91;PT_PFN_PartyContactDetails#CONTACTTYPE=BUSINESSMOBILE;PT_PFN_PartyContactDetails#CONTACTVALUE=3613730625;PT_PFN_PartyContactDetails#CONTACTMETHOD=SMS};{PT_PFN_PartyContactDetails#CONTACTTYPE=BUSINESS;PT_PFN_PartyContactDetails#CONTACTVALUE=DCTestingMain@misys.com;PT_PFN_PartyContactDetails#CONTACTMETHOD=EMAIL}";
		rsPayload = new RsPayload();
		rsPayload.setRsParam("UNIQUE_ID=First13;PARTYID=PTY001;STATUS=Success");
		outMap = new HashMap<>();
		outMap.put("responsePayload", rsPayload);
		ClassLoader.getSystemClassLoader()
				.loadClass("com.trapedza.bankfusion.servercommon.microflow.FBPMicroflowServiceInvoker");
		invoker = createMock(FBPMicroflowServiceInvoker.class);
	}

	@Test
	public void testChangePersonalPartyDtls() {
		JsonObject jsonObject = new JsonParser().parse(jsonInputString).getAsJsonObject();
		try {
			PowerMock.expectNew(FBPMicroflowServiceInvoker.class, "CB_PTY_MaintainPartyWS_SRV").andReturn(invoker);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getStackTrace());
		}
		PowerMock.mockStatic(MessageProducerUtil.class);
		PowerMock.suppress(PowerMock.method(MessageProducerUtil.class, "sendMessage", String.class, String.class));
		expect(invoker.invokeMicroflow(anyObject(HashMap.class), anyBoolean())).andStubReturn(outMap);
		PowerMock.replay(FBPMicroflowServiceInvoker.class);
		replay(invoker);
		messageRouterMethods.changePersonalPartyDtls(jsonObject, false);
		String rqParam = Whitebox.getInternalState(messageRouterMethods, "maintainPartyRqParam");
		EasyMock.verify(invoker);
		assertEquals(expectRqParam, rqParam);
	}

	@Test
	public void testChangePersonalPartyDtlsSupport() {
		JsonObject jsonObject = new JsonParser().parse(jsonInputStringSupport).getAsJsonObject();
		try {
			PowerMock.expectNew(FBPMicroflowServiceInvoker.class, "CB_PTY_MaintainPartyWS_SRV").andReturn(invoker);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getStackTrace());
		}
		PowerMock.mockStatic(MessageProducerUtil.class);
		PowerMock.suppress(PowerMock.method(MessageProducerUtil.class, "sendMessage", String.class, String.class));
		expect(invoker.invokeMicroflow(anyObject(HashMap.class), anyBoolean())).andStubReturn(outMap);
		PowerMock.replay(FBPMicroflowServiceInvoker.class);
		replay(invoker);
		messageRouterMethods.changePersonalPartyDtls(jsonObject, false);
		String rqParam = Whitebox.getInternalState(messageRouterMethods, "maintainPartyRqParam");
		EasyMock.verify(invoker);
		assertEquals(expectRqParamSupport, rqParam);
	}

	@Test
	public void testChangePersonalPartyDtlsException() {
		JsonObject jsonObject = new JsonParser().parse(jsonInputStringSupport).getAsJsonObject();
		try {
			PowerMock.expectNew(FBPMicroflowServiceInvoker.class, "CB_PTY_MaintainPartyWS_SRV")
					.andThrow(new Exception());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getStackTrace());
		}
		PowerMock.mockStatic(MessageProducerUtil.class);
		PowerMock.suppress(PowerMock.method(MessageProducerUtil.class, "sendMessage", String.class, String.class));
		expect(invoker.invokeMicroflow(anyObject(HashMap.class), anyBoolean())).andStubReturn(outMap);
		PowerMock.replay(FBPMicroflowServiceInvoker.class);
		replay(invoker);
		messageRouterMethods.changePersonalPartyDtls(jsonObject, false);
		String rqParam = Whitebox.getInternalState(messageRouterMethods, "maintainPartyRqParam");
		EasyMock.verify(invoker);
		assertEquals(expectRqParamSupport, rqParam);
	}

}
