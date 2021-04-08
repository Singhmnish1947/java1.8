package com.misys.ub.fatoms;

import static org.junit.Assert.assertEquals;

import com.misys.ub.fatoms.NonSTPPaymentResponseHandler;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.misys.bankfusion.common.ComplexTypeConvertorFactory;
import com.misys.bankfusion.subsystem.microflow.IMFManager;
import com.trapedza.bankfusion.servercommon.microflow.MicroflowHelper;

import bf.com.misys.cbs.msgs.v1r0.TransferResponse;

import com.misys.bankfusion.common.BankFusionMessages;
import com.misys.bankfusion.common.ComplexTypeConvertor;

import org.easymock.EasyMock;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.easymock.annotation.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ com.misys.ub.fatoms.NonSTPPaymentResponseHandler.class,
		com.misys.bankfusion.common.BankFusionMessages.class, BankFusionEnvironment.class,
		com.trapedza.bankfusion.servercommon.microflow.MicroflowHelper.class,
		com.misys.bankfusion.subsystem.microflow.runtime.impl.MFManager.class, ComplexTypeConvertorFactory.class })
@PowerMockIgnore({ "javax.management.*", "javax.xml.*", "org.xml.sax.*", "org.w3c.dom.*",
		"org.springframework.context.*", "org.apache.log4j.*" })
public class NonSTPPaymentResponseHandlerTest {

	private NonSTPPaymentResponseHandler nonSTPPaymentResponseHandler;
	private String formatedMessage = "International Payment Cancellation Event";
	BankFusionEnvironment env;
	private IMFManager IMFManager;
	ComplexTypeConvertor complexTypeConvertor = new ComplexTypeConvertor();
	@Mock
	private MicroflowHelper microflowHelper;
	private TransferResponse transferResponse;
	private static String ExpectedOutput = "<transferResponse xmlns=\"http://www.misys.com/cbs/msgs/v1r0\"><rsHeader><ns1:origCtxtId xmlns:ns1=\"http://www.misys.com/cbs/types/header\">CCI</ns1:origCtxtId></rsHeader><instructionStatusUpdateNotification><ns2:instructionUpdateItem xmlns:ns2=\"http://www.misys.com/cbs/types\"><ns2:transactionalItem>ChannelRef</ns2:transactionalItem><ns2:newStatus>REJECTED</ns2:newStatus><ns2:transactionEvent><ns2:reasonCode>40430049</ns2:reasonCode><ns2:defaultMessage>International Payment Cancellation Event</ns2:defaultMessage><ns2:formattedMessage>International Payment Cancellation Event</ns2:formattedMessage></ns2:transactionEvent></ns2:instructionUpdateItem></instructionStatusUpdateNotification></transferResponse>";

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// env = new BankFusionEnvironment(null);
		env = EasyMock.createMock(BankFusionEnvironment.class);
		nonSTPPaymentResponseHandler = new NonSTPPaymentResponseHandler();
		nonSTPPaymentResponseHandler.setF_IN_ChannelRef("Test001");
		nonSTPPaymentResponseHandler.setF_IN_EventCode("40430049");
		nonSTPPaymentResponseHandler.setF_IN_OrigChannelId("CCI");
		nonSTPPaymentResponseHandler.setF_IN_PaymentReference("Test001");

		IMFManager = EasyMock.createMock(IMFManager.class);
		// mFManager = EasyMock.createMock(MFManager.class);
		microflowHelper = EasyMock.createMock(MicroflowHelper.class);
		transferResponse = EasyMock.createMock(TransferResponse.class);
		// rsHeader = EasyMock.createMock(RsHeader.class);

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testProcess() throws Exception {

		PowerMock.mockStatic(BankFusionMessages.class);

		EasyMock.expect(BankFusionMessages.getFormattedMessage(EasyMock.anyInt(), EasyMock.anyObject(String[].class)))
				.andReturn(formatedMessage).anyTimes();
		PowerMock.replayAll(BankFusionMessages.class);

		// PowerMock.expectNew(TransferResponse.class).andReturn(transferResponse);
		// PowerMock.replay(TransferResponse.class, transferResponse);
		// PowerMock.expectNew(RsHeader.class).andReturn(rsHeader);
		// PowerMock.replay(RsHeader.class, rsHeader);

		// PowerMock.expectNew(MicroflowHelper.class, env).andReturn(microflowHelper);
		// EasyMock.expect(microflowHelper.getMFManager()).andReturn(mFManager);
		// PowerMock.replay(MicroflowHelper.class, microflowHelper);

		// PowerMock.mockStatic(ComplexTypeConvertorFactory.class);
		// EasyMock.expect(ComplexTypeConvertorFactory.getComplexTypeConvertor(EasyMock.anyObject()))
		// .andReturn(complexTypeConvertor).anyTimes();
		// PowerMock.replayAll(ComplexTypeConvertorFactory.class);

		// transferResponse=new TransferResponse();
		PowerMock.mockStatic(NonSTPPaymentResponseHandler.class);

		EasyMock.expect(
				NonSTPPaymentResponseHandler.convertObjectToXMLString(EasyMock.anyObject(), EasyMock.anyObject()))
				.andReturn(ExpectedOutput).anyTimes();
		PowerMock.expectPrivate(NonSTPPaymentResponseHandler.class, "postToServiceProviderQueue", ExpectedOutput)
				.anyTimes();
		PowerMock.replay(NonSTPPaymentResponseHandler.class);

		nonSTPPaymentResponseHandler.process(env);
		String actualOutput = nonSTPPaymentResponseHandler.responseMessage;
		System.out.println(actualOutput);
		assertEquals(ExpectedOutput, actualOutput);
	}

}
