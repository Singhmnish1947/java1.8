/* **********************************************************
 * Copyright (c) 2008 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.XMLContext;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.common.util.BankFusionPropertySupport;
import com.misys.ub.common.GetUBConfigLocation;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.swift.SendersCharges;
import com.misys.ub.swift.UB_MT103;
import com.misys.ub.swift.UB_MT200;
import com.misys.ub.swift.UB_MT202;
import com.misys.ub.swift.UB_MT205;
import com.trapedza.bankfusion.bo.refimpl.IBOSWTDisposal;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_IncomingMT103DisposalUpdation;
import com.trapedza.bankfusion.steps.refimpl.IUB_SWT_IncomingMT103DisposalUpdation;

/**
 * @author Vipesh
 *
 */
public class UB_SWT_IncomingMT103DisposalUpdation extends AbstractUB_SWT_IncomingMT103DisposalUpdation implements
		IUB_SWT_IncomingMT103DisposalUpdation {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private transient final static Log logger = LogFactory.getLog(UB_SWT_MessageReaderMQ.class.getName());
	String generate103Plus = "N";
	private String end2EndTxnRef = CommonConstants.EMPTY_STRING;
    private static final String SWIFT_PROPERTY_FILENAME = "SWIFT.properties";
    private static final String SWIFT_MESSAGE_MAPPING = "SwiftMessageMapping.xml";
    private static final String SWIFT_CONF_LOCATION = "conf/swift/";
    private static final String SERVICE_TYPE_ID = "ServiceTypeId";
    private static final String UB_SWT_FIND_IDENTIFIER_CUSTOMER = "UB_SWT_FindIdentifierCustomer_SRV";

	public UB_SWT_IncomingMT103DisposalUpdation(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) {
		String messageType = getF_IN_IncomingMessageType();
		String CustomerCode = CommonConstants.EMPTY_STRING;
        String Receiver103BIC =getF_IN_103Reciever();
        String Receiver202BIC = getF_IN_202Reciever();
        String Receiver205BIC = getF_IN_205Reciever();

		if (messageType.equals("MT103")) {
			if (this.getF_IN_IncomingMessageObject().size() > 0) {
				ArrayList messageList = this.getF_IN_IncomingMessageObject();
				Object messageObject = messageList.get(0);
				UB_MT103 messageobj = (UB_MT103) messageObject;
				
				BigDecimal sendersCharge = getF_IN_SendersCharge();
				BigDecimal amount = getF_IN_TransactionAmount();
				String transactionAmount = amount.toString();
				generate103Plus = messageobj.getStp();
				end2EndTxnRef = messageobj.getEndtoendTxnRef();
                if((Receiver103BIC.length()) == 8)
                {
                    Receiver103BIC = Receiver103BIC+"XXX";
                }

                HashMap hashmap = new HashMap();
                hashmap.put("IDENTIFIERCODE", Receiver103BIC);
				HashMap hashmapout = new HashMap();
                hashmapout = MFExecuter.executeMF(UB_SWT_FIND_IDENTIFIER_CUSTOMER, env, hashmap);
				if (((String) hashmapout.get("PAYMSGREQUIRED")).equals("Y")) {
					setF_IN_ConfirmationFlag(0);
				}

				messageobj.setSendersCorrespondent(getF_IN_SenderCorrespondent());
				messageobj.setSendersCorrespOption(getF_IN_SendersCorrespondentOption());
				messageobj.setIntermediaryInstitution(getF_IN_Intermediary());
				messageobj.setIntermediaryInstOption(getF_IN_IntermediaryOption());
				messageobj.setBeneficiaryCustomer(getF_IN_BeneficiaryCustomer());
				messageobj.setBeneficiaryCustOption(getF_IN_BeneficiaryCustomerOption());
				messageobj.setAccountWithInstitution(getF_IN_AccountWith());
				messageobj.setAccountWithInstOption(getF_IN_AccountWithOption());
				messageobj.setReceiver(Receiver103BIC);
				messageobj.setTransactionID(getF_IN_TransactionID());
				if (null==messageobj.getInstructedAmount() || CommonConstants.EMPTY_STRING.equals(messageobj.getInstructedAmount())) {
					messageobj.setInstructedAmount(messageobj.getTdAmount());
					messageobj.setInstructedCurrency(messageobj.getTdCurrencyCode());
				}
				messageobj.setTdAmount(transactionAmount);
				messageobj.setReceiversCharges("");
				if (sendersCharge.compareTo(new BigDecimal(0)) > 0) {
					SendersCharges senderChargeDetails = new SendersCharges();
					senderChargeDetails.setSenderCharge(getF_IN_ChargeCurrency() + getF_IN_SendersCharge());
					messageobj.addCharges(senderChargeDetails);
				}

				hashmap.clear();
				hashmap.put("IDENTIFIERCODE",Receiver202BIC);
				HashMap hashmapout1 = new HashMap();
                hashmapout1 = MFExecuter.executeMF(UB_SWT_FIND_IDENTIFIER_CUSTOMER, env, hashmap);
				if (((String) hashmapout1.get("PAYMSGREQUIRED")).equals("Y") && !Receiver202BIC.equals("")) {
					setF_IN_PaymentFlag(0);
				} else {
					setF_IN_PaymentFlag(9);
				}
                generateXML(messageobj, env, (String) hashmapout.get("CUSTOMERCODE"));

			}
		} else if (messageType.equals("MT202")) {
			if (this.getF_IN_IncomingMessageObject().size() > 0) {
				ArrayList messageList = this.getF_IN_IncomingMessageObject();
				Object messageObject = messageList.get(0);
				UB_MT202 messageobj = (UB_MT202) messageObject;
				BigDecimal amount = getF_IN_TransactionAmount();
				String transactionAmount = amount.toString();
                end2EndTxnRef = messageobj.getEnd2EndTxnRef();

				if (getF_IN_OutgoingMessageType().equals("MT205")) {

					UB_MT205 obj205 = new UB_MT205();
                    if((Receiver205BIC.length()) == 8)
                    {
                        Receiver205BIC = Receiver205BIC+"XXX";
                    }


					HashMap hashmap = new HashMap();
					hashmap.put("IDENTIFIERCODE",Receiver205BIC);
					HashMap hashmapout = new HashMap();
                    hashmapout = MFExecuter.executeMF(UB_SWT_FIND_IDENTIFIER_CUSTOMER, env, hashmap);
					obj205.setAccountWithInstitution(getF_IN_AccountWith());
					obj205.setAccountWithInstOption(getF_IN_AccountWithOption());
					obj205.setIntermediary(getF_IN_Intermediary());
					obj205.setIntermediaryOption(getF_IN_IntermediaryOption());
					obj205.setSendersCorrespondent(getF_IN_SenderCorrespondent());
					obj205.setSendersCorresOption(getF_IN_SendersCorrespondentOption());
					obj205.setSenderToReceiverInformation(messageobj.getSendertoReceiverInformation());
					obj205.setReceiver(Receiver205BIC);
					obj205.setTdcurrencyCode(messageobj.getTdCurrencyCode());
					obj205.setTdamount(transactionAmount);
					obj205.setMessageType("MT205");
                    end2EndTxnRef = StringUtils.isNotBlank(messageobj.getEnd2EndTxnRef()) ? messageobj.getEnd2EndTxnRef()
                            : getUETR(PaymentSwiftConstants.CHANNELID_SWIFT, PaymentSwiftConstants.MT205,
                                    messageobj.getTransactionReferenceNumber());
                    obj205.setEnd2EndTxnRef(end2EndTxnRef);
                    obj205.setServiceTypeId(getServiceTypeId());
					obj205.setBeneficiaryInstitute(messageobj.getBeneficiary());
					obj205.setBeneficiaryInstOption(messageobj.getBeneficiaryOption());
					obj205.setTransactionReferenceNumber(messageobj.getTransactionReferenceNumber());
					obj205.setSender(messageobj.getSender());
					obj205.setRelatedReference(messageobj.getRelatedReference());
					obj205.setTdvalueDate(messageobj.getTdValueDate());
                    generateXML(obj205, env, (String) hashmapout.get("CUSTOMERCODE"));
				} else {
                        if((Receiver202BIC.length()) == 8)
                        {
                            Receiver202BIC = Receiver202BIC+"XXX";
                        }
					messageobj.setAccountWithInstitution(getF_IN_AccountWith());
					messageobj.setAccountWithInstitutionOption(getF_IN_AccountWithOption());
					messageobj.setIntermediary(getF_IN_Intermediary());
					messageobj.setIntermediaryOption(getF_IN_IntermediaryOption());
					messageobj.setSendersCorrespondent(getF_IN_SenderCorrespondent());
					messageobj.setSendersCorrespondentOption(getF_IN_SendersCorrespondentOption());
					messageobj.setMessageType("MT202");
					messageobj.setReceiver(Receiver202BIC);
                    generateXML(messageobj, env, CustomerCode);
				}
			}
		} else if (messageType.equals("MT205")) {
			if (this.getF_IN_IncomingMessageObject().size() > 0) {
				ArrayList messageList = this.getF_IN_IncomingMessageObject();
				Object messageObject = messageList.get(0);
				UB_MT205 messageobj = (UB_MT205) messageObject;
                end2EndTxnRef = messageobj.getEnd2EndTxnRef();

				if (getF_IN_OutgoingMessageType().equals("MT202")) {
					UB_MT202 obj202 = new UB_MT202();
                    if((Receiver202BIC.length()) == 8)
                    {
                        Receiver202BIC = Receiver202BIC+"XXX";
                    }
					obj202.setMessageType("MT202");
                    end2EndTxnRef = StringUtils.isNotBlank(messageobj.getEnd2EndTxnRef()) ? messageobj.getEnd2EndTxnRef()
                            : getUETR(PaymentSwiftConstants.CHANNELID_SWIFT, PaymentSwiftConstants.MT202,
                                    messageobj.getTransactionReferenceNumber());
					obj202.setTransactionReferenceNumber(messageobj.getTransactionReferenceNumber());
					obj202.setRelatedReference(messageobj.getRelatedReference());
					obj202.setBeneficiary(getF_IN_BeneficiaryInstitution());
					obj202.setBeneficiaryOption(getF_IN_BeneficiaryInstitutionOption());
					obj202.setTdValueDate(messageobj.getTdvalueDate());
					obj202.setTdAmount(messageobj.getTdamount());
					obj202.setTdCurrencyCode(messageobj.getTdcurrencyCode());
					obj202.setOrderingInstitution(messageobj.getOrderingInstitution());
					obj202.setOrderingInstitutionOption(messageobj.getOrderingInstOption());
					obj202.setSendersCorrespondent(messageobj.getSendersCorresOption());
					obj202.setReceiversCorrespondent(messageobj.getReceiver());
					obj202.setReceiver(Receiver202BIC);
					obj202.setAccountWithInstitution(getF_IN_AccountWith());
					obj202.setAccountWithInstitutionOption(getF_IN_AccountWithOption());
					obj202.setIntermediary(getF_IN_Intermediary());
					obj202.setIntermediaryOption(getF_IN_IntermediaryOption());
					obj202.setSendertoReceiverInformation(messageobj.getSenderToReceiverInformation());
                    obj202.setEnd2EndTxnRef(end2EndTxnRef);
                    obj202.setServiceTypeId(getServiceTypeId());

                    generateXML(obj202, env, CustomerCode);
				} else {

                    if((Receiver205BIC.length()) == 8)
                    {
                        Receiver205BIC = Receiver205BIC+"XXX";
                    }    				
					messageobj.setAccountWithInstitution(getF_IN_AccountWith());
					messageobj.setAccountWithInstOption(getF_IN_AccountWithOption());
					messageobj.setIntermediary(getF_IN_Intermediary());
					messageobj.setIntermediaryOption(getF_IN_IntermediaryOption());
					/* fix #14737 */
					messageobj.setReceiver(Receiver205BIC);
					/* fix #14737 */
                    generateXML(messageobj, env, CustomerCode);
				}
			}
		} else if (messageType.equals("MT200")) {
			if (this.getF_IN_IncomingMessageObject().size() > 0) {
				ArrayList messageList = this.getF_IN_IncomingMessageObject();
				Object messageObject = messageList.get(0);
				UB_MT200 messageobj = (UB_MT200) messageObject;
				BigDecimal amount = getF_IN_TransactionAmount();
				String transactionAmount = amount.toString();
				UB_MT205 obj205 = new UB_MT205();

				HashMap hashmap = new HashMap();
				hashmap.put("IDENTIFIERCODE",Receiver205BIC);
				HashMap hashmapout = new HashMap();
                hashmapout = MFExecuter.executeMF(UB_SWT_FIND_IDENTIFIER_CUSTOMER, env, hashmap);
                if((Receiver205BIC.length()) == 8)
                {
                    Receiver205BIC = Receiver205BIC+"XXX";
                }

				obj205.setAccountWithInstitution(getF_IN_AccountWith());
				obj205.setAccountWithInstOption(getF_IN_AccountWithOption());
				obj205.setIntermediary(getF_IN_Intermediary());
				obj205.setIntermediaryOption(getF_IN_IntermediaryOption());
				obj205.setSendersCorrespondent(getF_IN_SenderCorrespondent());
				obj205.setSendersCorresOption(getF_IN_SendersCorrespondentOption());
				obj205.setSenderToReceiverInformation(messageobj.getSenderToReceiverInformation());
				obj205.setReceiver(Receiver205BIC);
				obj205.setTdamount(transactionAmount);
				obj205.setMessageType("MT205");
				obj205.setTransactionReferenceNumber(messageobj.getTransactionReferenceNumber());
				obj205.setTdvalueDate(messageobj.getTdvalueDate());
				obj205.setRelatedReference(messageobj.getTransactionReferenceNumber());
				obj205.setTdcurrencyCode(messageobj.getTdcurrencyCode());
				obj205.setSender(messageobj.getSender());
				obj205.setBeneficiaryInstitute(getF_IN_BeneficiaryInstitution());
				obj205.setBeneficiaryInstOption(getF_IN_BeneficiaryInstitutionOption());
                end2EndTxnRef = getUETR(PaymentSwiftConstants.CHANNELID_SWIFT, PaymentSwiftConstants.MT205,
                        messageobj.getTransactionReferenceNumber());
                obj205.setEnd2EndTxnRef(end2EndTxnRef);
                obj205.setServiceTypeId(getServiceTypeId());

                generateXML(obj205, env, (String) hashmapout.get("CUSTOMERCODE"));
			}

		}
	}

	public String generateXML(Object messageObject, BankFusionEnvironment environment, String CustomerCode) {
		StringWriter xmlWriter = new StringWriter();
		try {
			XMLContext xmlContext = new XMLContext();
			xmlContext.setProperty("org.exolab.castor.indent", "false");

			Marshaller marshaller = new Marshaller(xmlWriter);
			Mapping mapping = new Mapping(getClass().getClassLoader());
            StringBuilder path = new StringBuilder();
            path.append(GetUBConfigLocation.getUBConfigLocation()).append(SWIFT_CONF_LOCATION).append(SWIFT_MESSAGE_MAPPING);
            mapping.loadMapping(path.toString());
			marshaller.setMapping(mapping);
			marshaller.marshal(messageObject);
			logger.info("MessageObject" + messageObject);
			IBOSWTDisposal xmlRecordBO;
			xmlRecordBO = (IBOSWTDisposal) environment.getFactory().getStatelessNewInstance(IBOSWTDisposal.BONAME);
			String XmlString = xmlWriter.toString();
			byte[] messXml = XmlString.getBytes();
			String beneficiaryInst = getF_IN_BeneficiaryInstitution();
			String beneficiaryInstitution[] = null;
			beneficiaryInstitution = beneficiaryInst.split("[$]");
			String beneficiaryInstitution1[] = new String[4];
			for (int j = 0; j <= 3; j++) {
				if (j < beneficiaryInstitution.length)
					beneficiaryInstitution1[j] = beneficiaryInstitution[j];
				else
					beneficiaryInstitution1[j] = "";

			}
			xmlRecordBO.setF_MESSAGEXML(messXml);
			xmlRecordBO.setF_MESSAGETYPE(getF_IN_OutgoingMessageType().substring(2));
			xmlRecordBO.setF_PAYMENTFLAG(getF_IN_PaymentFlag());
			xmlRecordBO.setF_DEALNO(getF_IN_DealNo());
			xmlRecordBO.setF_RECEIPTFLAG(9);
			xmlRecordBO.setF_CONFIRMATIONFLAG(getF_IN_ConfirmationFlag());
			xmlRecordBO.setF_CRDRCONFIRMATIONFLAG(getF_IN_CRDRConfirmationFlag());
			xmlRecordBO.setF_CANCELFLAG(9);
			xmlRecordBO.setF_VERIFYFLAG("1");
			xmlRecordBO.setF_CONTRAACCOUNTID(getF_IN_CreditAccount());
			xmlRecordBO.setF_CUSTACCOUNTID(getF_IN_DebitAccount());
			xmlRecordBO.setF_BENEFICIARY_TEXT1(beneficiaryInstitution1[0]);
			xmlRecordBO.setF_BENEFICIARY_TEXT2(beneficiaryInstitution1[1]);
			xmlRecordBO.setF_BENEFICIARY_TEXT3(beneficiaryInstitution1[2]);
			xmlRecordBO.setF_BENEFICIARY_TEXT4(beneficiaryInstitution1[3]);
			xmlRecordBO.setF_CUSTOMERCODE(CustomerCode);
			xmlRecordBO.setF_VALUEDATE(getF_IN_ValueDate());
			xmlRecordBO.setF_POSTDATE(getF_IN_PostDate());
			xmlRecordBO.setF_MATURITYDATE(getF_IN_MaturityDate());
			xmlRecordBO.setF_DEALORIGINATOR("I");
			xmlRecordBO.setF_TRANSACTIONID(getF_IN_TransactionID());
			xmlRecordBO.setF_UBEND2ENDTXNREF(end2EndTxnRef);
	
			/*
			 * This file was giving null pointer exception without this line
			 * This below line is part of JMS implementation
			 */
			xmlRecordBO.setF_GENERATE103PLUSIND(generate103Plus);
			environment.getFactory().create(IBOSWTDisposal.BONAME, xmlRecordBO);
		} catch (Exception e) {
			logger.error(ExceptionUtil.getExceptionAsString(e));
		}
		return xmlWriter.toString();

	}

    /**
     * @param channelId
     * @param messageType
     * @param txnReference
     * @return
     */
    private String getUETR(String channelId, String messageType, String txnReference) {
        UB_SWT_GenerateUETR uetrFatom = new UB_SWT_GenerateUETR();
        uetrFatom.setF_IN_Channel(channelId);
        uetrFatom.setF_IN_MessageType(messageType);
        uetrFatom.setF_IN_TxnReference(txnReference);
        uetrFatom.process(BankFusionThreadLocal.getBankFusionEnvironment());
        return uetrFatom.getF_OUT_UETR();
    }

    /**
     * Method Description:Method to get the service type id
     * 
     * @param ubConfigLocation
     * @return
     */
    private String getServiceTypeId() {
        StringBuilder path = new StringBuilder();
        path.append(GetUBConfigLocation.getUBConfigLocation()).append(SWIFT_CONF_LOCATION).append(SWIFT_PROPERTY_FILENAME);
        return BankFusionPropertySupport.getProperty(path.toString(), SERVICE_TYPE_ID, CommonConstants.EMPTY_STRING);
    }

}