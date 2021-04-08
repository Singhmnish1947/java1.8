/* **********************************************************
 * Copyright (c) 2008 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

/**
 * @author Gaurav Aggarwal
 * 
 */
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.applicationjms.JMSHelper;
import com.trapedza.bankfusion.boundary.outward.BankFusionResourceSupport;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_KYC_FIC_FircosoftRequest;
import com.trapedza.bankfusion.utils.GUIDGen;

public class UB_KYC_FIC_FircosoftRequest extends AbstractUB_KYC_FIC_FircosoftRequest {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	private transient final static Log logger = LogFactory.getLog(UB_KYC_FIC_FircosoftRequest.class.getName());

	private String space = " ";

	InputStream fircosoftProp = BankFusionResourceSupport.getResourceLoader().getInputStreamResource("conf/messaging/jndi.properties");

	Properties props = new Properties();

	public UB_KYC_FIC_FircosoftRequest(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	public String stringToHex(String strToConvrt) {
		String hexString = "";
		byte[] strToHex = strToConvrt.getBytes();
		for (int i = 0; i < strToHex.length; i++) {
			hexString += Integer.toHexString(strToHex[i]);
		}
		return "JMSMessageID LIKE 'ID:" + hexString + "%'";
	}

	public String concatMessageDetails() {
		String totalMessage = getF_IN_message1() + space + getF_IN_message2() + space + getF_IN_message3() + space + getF_IN_message4() + space
				+ getF_IN_message5() + space + getF_IN_message6() + space + getF_IN_message7() + space + getF_IN_message8() + space
				+ getF_IN_message9() + space + getF_IN_message10() + space + getF_IN_message11() + space + getF_IN_message12() + space
				+ getF_IN_message13() + space + getF_IN_message14() + space + getF_IN_message15() + space + getF_IN_message16() + space
				+ getF_IN_message17() + space + getF_IN_message18() + space + getF_IN_message19() + space + getF_IN_message20() + space
				+ getF_IN_message21() + space + getF_IN_message22() + space + getF_IN_message23() + space + getF_IN_message24() + space
				+ getF_IN_message25() + space + getF_IN_message26() + space + getF_IN_message27() + space + getF_IN_message28() + space
				+ getF_IN_message29() + space + getF_IN_message30() + space + getF_IN_message31() + space + getF_IN_message32() + space
				+ getF_IN_message33() + space + getF_IN_message34() + space + getF_IN_message35() + space + getF_IN_message36() + space
				+ getF_IN_message37() + space + getF_IN_message38() + space + getF_IN_message39() + space + getF_IN_message40() + space
				+ getF_IN_message41() + space + getF_IN_message42() + space + getF_IN_message43() + space + getF_IN_message44() + space
				+ getF_IN_message45() + space + getF_IN_message46() + space + getF_IN_message47() + space + getF_IN_message48() + space
				+ getF_IN_message49() + space + getF_IN_message50() + space + getF_IN_message51() + space + getF_IN_message52() + space
				+ getF_IN_message53() + space + getF_IN_message54() + space + getF_IN_message55() + space + getF_IN_message56() + space
				+ getF_IN_message57() + space + getF_IN_message58() + space + getF_IN_message59() + space + getF_IN_message60();

		return totalMessage.trim();
	}

	public String fircosoftMessageGenerator() {
		String messageId = getF_IN_MESSAGEID();
		String entity = getF_IN_ENTITY();
		String byPassReview = getF_IN_BYPASSREVIEW();
		String command = getF_IN_COMMAND();
		String businessType = getF_IN_BUSINESSTYPE();
		String messageType = getF_IN_MESSAGETYPE();
		String ioIndicator = getF_IN_IOINDICATOR();
		String senderReference = getF_IN_SENDERREFERENCE();
		String currency = getF_IN_CURRENCY();
		String amount = getF_IN_AMOUNT();
		String appCode = getF_IN_APPCODE();
		String userCode = getF_IN_USERCODE();
		String format = getF_IN_FORMAT();
		String currency2 = getF_IN_CURRENCY2();
		String sender = getF_IN_SENDER();
		String receiver = getF_IN_RECEIVER();
		String message = concatMessageDetails();
		// for message ID length=64
		if (messageId != CommonConstants.EMPTY_STRING) {
			int messageIdLength = messageId.length();
			if (messageId.length() != 64) {
				for (int i = 0; i < 64 - messageIdLength; i++) {
					messageId += space;
				}
			}
		} else {
			int messageIdLength = messageId.length();
			if (messageId.length() != 64) {
				for (int i = 0; i < 64 - messageIdLength; i++) {
					messageId += space;
				}
			}
		}
		// for entity length = 15+17
		if (entity != CommonConstants.EMPTY_STRING && entity.length() < 16) {
			int entityLength = entity.length();
			if (entity.length() != 32) {
				for (int i = 0; i < 32 - entityLength; i++) {
					entity += space;
				}
			}
		} else {
			entity = "UB";
			int entityLength = entity.length();
			if (entity.length() != 32) {
				for (int i = 0; i < 32 - entityLength; i++) {
					entity += space;
				}
			}

		}

		// For By Pass Review length=1

		if (byPassReview == CommonConstants.EMPTY_STRING) {
			byPassReview = "0";
		} else {
			byPassReview = byPassReview.substring(0);
		}

		// For Command length=1

		if (command == CommonConstants.EMPTY_STRING) {
			command = "0";
		} else {
			command = command.substring(0);
		}
		// For Business Type
		if (businessType != CommonConstants.EMPTY_STRING) {
			int businessTypeLength = businessType.length();
			if (businessType.length() != 64) {
				for (int i = 0; i < 64 - businessTypeLength; i++) {
					businessType += space;
				}
			}
		} else {
			businessType = "";
			int businessTypeLength = businessType.length();
			if (businessType.length() != 64) {
				for (int i = 0; i < 64 - businessTypeLength; i++) {
					businessType += space;
				}
			}
		}
		// For Message Type
		if (messageType != CommonConstants.EMPTY_STRING) {
			int messageTypeLength = messageType.length();
			if (messageType.length() != 8) {
				for (int i = 0; i < 8 - messageTypeLength; i++) {
					messageType += space;
				}
			}
		} else {
			messageType = "";
			int messageTypeLength = messageType.length();
			if (messageType.length() != 8) {
				for (int i = 0; i < 8 - messageTypeLength; i++) {
					messageType += space;
				}
			}
		}

		// for I/O Indicator length=1

		if (ioIndicator == CommonConstants.EMPTY_STRING) {
			ioIndicator = "I";
		} else {
			ioIndicator = ioIndicator.substring(0);
		}

		// for Senders Reference length=32

		if (senderReference != CommonConstants.EMPTY_STRING) {
			int senderReferenceLength = senderReference.length();
			if (senderReference.length() != 32) {
				for (int i = 0; i < 32 - senderReferenceLength; i++) {
					senderReference += space;
				}
			}
		} else {
			senderReference = "";
			int senderReferenceLength = senderReference.length();
			if (senderReference.length() != 32) {
				for (int i = 0; i < 32 - senderReferenceLength; i++) {
					senderReference += space;
				}
			}
		}

		// for currency length=3

		if (currency != CommonConstants.EMPTY_STRING) {
			int currencyLength = currency.length();
			if (currency.length() != 3) {
				for (int i = 0; i < 3 - currencyLength; i++) {
					currency += space;
				}
			}
		} else {
			currency = "";
			int currencyLength = currency.length();
			if (currency.length() != 3) {
				for (int i = 0; i < 3 - currencyLength; i++) {
					currency += space;
				}
			}
		}

		// for Amount length=16

		if (amount != CommonConstants.EMPTY_STRING) {
			int amountLength = amount.length();
			if (amount.length() != 16) {
				for (int i = 0; i < 16 - amountLength; i++) {
					amount += space;
				}
			}
		} else {
			amount = "";
			int amountLength = amount.length();
			if (amount.length() != 16) {
				for (int i = 0; i < 16 - amountLength; i++) {
					amount += space;
				}
			}
		}

		// for App code length=8

		if (appCode != CommonConstants.EMPTY_STRING) {
			int appCodeLength = appCode.length();
			if (appCode.length() != 8) {
				for (int i = 0; i < 8 - appCodeLength; i++) {
					appCode += space;
				}
			}
		} else {
			appCode = "UB";
			int appCodeLength = appCode.length();
			if (appCode.length() != 8) {
				for (int i = 0; i < 8 - appCodeLength; i++) {
					appCode += space;
				}
			}

		}
		// for user code length = 8
		if (userCode != CommonConstants.EMPTY_STRING) {
			int userCodeLength = userCode.length();
			if (userCode.length() != 8) {
				for (int i = 0; i < 8 - userCodeLength; i++) {
					userCode += space;
				}
			}
		} else {
			userCode = "";
			int userCodeLength = userCode.length();
			if (userCode.length() != 8) {
				for (int i = 0; i < 8 - userCodeLength; i++) {
					userCode += space;
				}
			}
		}

		// for format length = 16

		if (format != CommonConstants.EMPTY_STRING) {
			int formatLength = format.length();
			if (format.length() != 16) {
				for (int i = 0; i < 16 - formatLength; i++) {
					format += space;
				}
			}
		} else {
			format = "ALL";
			int formatLength = format.length();
			if (format.length() != 16) {
				for (int i = 0; i < 16 - formatLength; i++) {
					format += space;
				}
			}

		}

		// for currency2 length = 3

		if (currency2 != CommonConstants.EMPTY_STRING) {
			int currency2Length = currency2.length();
			if (currency2.length() != 3) {
				for (int i = 0; i < 3 - currency2Length; i++) {
					currency2 += space;
				}
			}
		} else {
			currency2 = "";
			int currency2Length = currency2.length();
			if (currency2.length() != 3) {
				for (int i = 0; i < 3 - currency2Length; i++) {
					currency2 += space;
				}
			}
		}

		// for senders reference

		if (sender != CommonConstants.EMPTY_STRING) {
			int senderLength = sender.length();
			if (sender.length() != 32) {
				for (int i = 0; i < 32 - senderLength; i++) {
					sender += space;
				}
			}
		} else {
			sender = "FIRCO";
			int senderLength = sender.length();
			if (sender.length() != 32) {
				for (int i = 0; i < 32 - senderLength; i++) {
					sender += space;
				}
			}
		}

		// for receiver length =32
		if (receiver != CommonConstants.EMPTY_STRING) {
			int receiverLength = receiver.length();
			if (receiver.length() != 32) {
				for (int i = 0; i < 32 - receiverLength; i++) {
					receiver += space;
				}
			}
		} else {
			receiver = "FIRCO";
			int receiverLength = receiver.length();
			if (receiver.length() != 32) {
				for (int i = 0; i < 32 - receiverLength; i++) {
					receiver += space;
				}
			}
		}
		String finalMessage = messageId + entity + byPassReview + command + businessType + messageType + ioIndicator + senderReference + currency
				+ amount + appCode + userCode + format + currency2 + sender + receiver + message;
		logger.info("Message Generated::" + finalMessage);

		return finalMessage;
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {

		JMSHelper jmsHelper = new JMSHelper(env);

		String id = GUIDGen.getNewGUID();
		String garbageMsg = "STAT";
		String noResultGarbage = "AMQ";
		String refNo = getF_IN_MESSAGEID();
		String hitDetails = "";
		String finalMessage = fircosoftMessageGenerator();
		String inputMessageID = jmsHelper.sendRequest(finalMessage, id);
		hitDetails = jmsHelper.receiveResponse(stringToHex(refNo.trim()));
		jmsHelper.consumeDummyMessage(stringToHex(garbageMsg));
		if (hitDetails != "" && hitDetails != null) {
			logger.info("Hit Details:::" + hitDetails);
			setF_OUT_Status("1");
			setF_OUT_ReqMsg(getF_IN_MESSAGE());
			setF_OUT_RespMsg(hitDetails);
		} else {
			logger.info("No Hits Details Found");
			setF_OUT_Status("0");
			jmsHelper.consumeDummyMessage(stringToHex(noResultGarbage));
		}

	}
}
