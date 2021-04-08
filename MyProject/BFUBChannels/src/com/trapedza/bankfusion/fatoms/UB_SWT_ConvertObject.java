package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.cbs.common.constants.CBSConstants;
import com.misys.ub.fatoms.batch.BatchUtil;
import com.misys.ub.swift.InstructionCode;
import com.misys.ub.swift.SendersCharges;
import com.misys.ub.swift.UB_MT103;
import com.misys.ub.swift.UB_MT200;
import com.misys.ub.swift.UB_MT202;
import com.misys.ub.swift.UB_MT205;
import com.misys.ub.swift.UB_SWT_Util;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.events.ErrorEvent;
import com.trapedza.bankfusion.events.IEvent;
import com.trapedza.bankfusion.exceptions.CollectedEventsDialogException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.StringToDate;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_ConvertObject;

import bf.com.misys.cbs.types.header.EventParameters;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.SubCode;
import bf.com.misys.ub.types.interfaces.MessageHeader;
import bf.com.misys.ub.types.interfaces.Mt201Detail;
import bf.com.misys.ub.types.interfaces.Mt203Detail;
import bf.com.misys.ub.types.interfaces.SwiftMT103;
import bf.com.misys.ub.types.interfaces.SwiftMT200;
import bf.com.misys.ub.types.interfaces.SwiftMT201;
import bf.com.misys.ub.types.interfaces.SwiftMT202;
import bf.com.misys.ub.types.interfaces.SwiftMT203;
import bf.com.misys.ub.types.interfaces.SwiftMT205;
import bf.com.misys.ub.types.interfaces.Ub_MT103;
import bf.com.misys.ub.types.interfaces.Ub_MT200;
import bf.com.misys.ub.types.interfaces.Ub_MT201;
import bf.com.misys.ub.types.interfaces.Ub_MT202;
import bf.com.misys.ub.types.interfaces.Ub_MT203;
import bf.com.misys.ub.types.interfaces.Ub_MT205;
/*@Itesh Kumar
 *@28 July 2009 
 * Description:: This class will convert New Message Object (xsd related) to Old Message Object
 *               and it will pass all respective values to respective Message's Service for further processing.  
 */
public class UB_SWT_ConvertObject extends AbstractUB_SWT_ConvertObject {
	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	String messageStatus ;
	Integer errorNumber =new Integer(0);
	String errorCode;
	String transactionId="";
	String creditAcntNumber;
	String debitAcntNumber;

	public static final String svnRevision = "$Revision: 1.0 $";
	public static final String FAILED_STATUS = "F";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}             

	public UB_SWT_ConvertObject(BankFusionEnvironment env) {
		super(env);
	}
	private transient final static Log logger = LogFactory
			.getLog(UB_SWT_ConvertObject.class.getName());


	@Override
	public void process(BankFusionEnvironment env) throws BankFusionException {
		ArrayList list = new ArrayList();
		HashMap hashmapout = new HashMap();
		HashMap hashmap = new HashMap();
		String mt200MessageType="MT200";
		String mt202MessageType="MT202";
		Boolean STP;
		if(getF_IN_STP().equalsIgnoreCase("Y"))
			STP=true;
		else
			STP=false;
		/*
		 * For MT200
		 */

		 if (getF_IN_MessageType().equals("MT200")) {

			 Ub_MT200 MT200 = getF_IN_Ub_MT200();
			 SwiftMT200 MT200Details = MT200.getDetails();
			 MessageHeader MT200header = MT200.getHeader();

			 UB_MT200 Object200 = new UB_MT200();

			 /*
			  * For Object
			  */
			  Object200.setTransactionReferenceNumber(MT200Details.getTransactionReferenceNumber());
			  Object200.setTdvalueDate(MT200Details.getTdvalueDate());
			  Object200.setTdcurrencyCode(MT200Details.getTdcurrencyCode());
			  Object200.setTdamount(MT200Details.getTdamount());
			  Object200.setSendersCorrespondent(MT200Details.getSendersCorrespondent());
			  Object200.setSendersCorresOption(MT200Details.getSendersCorresOption());
			  Object200.setIntermediary(MT200Details.getIntermediary());
			  Object200.setIntermediaryOption(MT200Details.getIntermediaryOption());
			  Object200.setAccountWithInstitution(MT200Details.getAccountWithInstitution());
			  Object200.setAccountWithInstOption(MT200Details.getAccountWithInstOption());
			  Object200.setSenderToReceiverInformation(MT200Details.getSenderToReceiverInformation());                                           
			  Object200.setInternalRef(MT200Details.getInternalRef());
			  Object200.setMessageId(MT200header.getMessageId1());
			  Object200.setReceiver(MT200Details.getReceiver());
			  Object200.setSender(MT200Details.getSender());
			  Object200.setAction(MT200Details.getAction());
			  Object200.setBranch(MT200Details.getBranch());
			  Object200.setDisposalRef(MT200Details.getDisposalRef());
			  Object200.setMessageType(MT200header.getMessageType());
			  Object200.setMultipleHold(MT200Details.getMultipleHold());
			  Object200.setVerificationRequired(MT200Details.getVerificationRequired());

			  /*
			   * For Input tags of Services
			   */

			  hashmap.put("AccountWithOption", MT200Details
					  .getAccountWithInstOption());
			  hashmap.put("AccountWith", MT200Details.getAccountWithInstitution());
			  hashmap.put("DealReference", MT200Details
					  .getTransactionReferenceNumber());
			  hashmap.put("InterBankSettledAmount", new BigDecimal(MT200Details
					  .getTdamount()));
			  hashmap.put("InterBankSettledCurrency", MT200Details
					  .getTdcurrencyCode());
			  hashmap.put("Intermediary", MT200Details.getIntermediary());
			  hashmap.put("IntermediaryOption", MT200Details
					  .getIntermediaryOption());
			  hashmap.put("MessageType", getF_IN_MessageType());
			  hashmap.put("Sender", MT200Details.getSender());
			  hashmap.put("SendersCorrespondent", MT200Details
					  .getSendersCorrespondent());
			  hashmap.put("SendersCorrespondentOption", MT200Details
					  .getSendersCorresOption());
			  hashmap.put("relatedReference", MT200Details.getInternalRef());
			  hashmap.put("sendertoReceiverInformation", MT200Details
					  .getSenderToReceiverInformation());
			  Date valueDate = StringToDate.run(MT200Details.getTdvalueDate());
			  hashmap.put("ValueDate", valueDate);
			  hashmap.put("transactionReferenceNumber", MT200Details
					  .getTransactionReferenceNumber());

			  hashmap.put("CreditAccount", getF_IN_CreditAccount());
			  hashmap.put("DebitAccount", getF_IN_DebitAccount());
			  hashmap.put("FromRemitScreen", getF_IN_FromRemitScreen());
			  hashmap.put("exchangeRate", getF_IN_exchangeRate());
			  hashmap.put("DrNarrative", getF_IN_debitNarrativeDescription());
			  hashmap.put("CrNarrative", getF_IN_creditNarrativeDescription());

			  list.add(Object200);
			  hashmap.put("Object", list);

			  hashmapout = MFExecuter.executeMF(
					  "UB_SWT_Incoming_MT200_Process_SRV", env, hashmap);

			  messageStatus = (String)hashmapout.get("MessageStatus");
			  errorCode = (String) hashmapout.get("ErrorNumber");
			  if (errorCode != null && errorCode.length() != 0){
				  errorNumber = Integer.parseInt(errorCode);
			  }
			  
			  if(hashmapout.get("hostTxnId")!=null) {
					 transactionId=hashmapout.get("hostTxnId").toString();
				 }
				 setF_OUT_transactionId(transactionId);
		 }


		 /*
		  * For MT201
		  */

		 if (getF_IN_MessageType().equals("MT201")) {

			 Ub_MT201 MT201 = getF_IN_Ub_MT201();
			 SwiftMT201 MT201Details = MT201.getDetails();
			 //           MessageHeader MT201header = MT201.getHeader();
			 Mt201Detail mt201details= getF_IN_mt201Detail();

			 UB_MT200 Object200 = new UB_MT200();

			 /*
			  * For Object
			  */
			 Object200.setTransactionReferenceNumber(mt201details.getTransactionReferenceNumber());
			 Object200.setTdvalueDate(MT201Details.getValueDate());
			 Object200.setTdcurrencyCode(mt201details.getCurrency());
			 Object200.setTdamount(mt201details.getAmount());
			 Object200.setSendersCorrespondent(MT201Details.getSendersCorrespondent());
			 Object200.setSendersCorresOption(MT201Details.getSendersCorrespondentOption());
			 Object200.setIntermediary(mt201details.getIntermediary());
			 Object200.setIntermediaryOption(mt201details.getIntermediaryOption());
			 Object200.setAccountWithInstitution(mt201details.getAccountWithInstitution());
			 Object200.setAccountWithInstOption(mt201details.getAccountWithInstitutionOption());
			 Object200.setSenderToReceiverInformation(mt201details.getSenderToReceiverInformation());
			 Object200.setInternalRef(MT201Details.getInternalRef());
			 Object200.setReceiver(MT201Details.getReceiver());
			 Object200.setSender(MT201Details.getSender());
			 Object200.setAction(MT201Details.getAction());
			 Object200.setBranch(MT201Details.getBranch());
			 Object200.setDisposalRef(MT201Details.getDisposalRef());
			 Object200.setMessageType(mt200MessageType);
			 Object200.setMultipleHold(MT201Details.getMultipleHold());
			 Object200.setVerificationRequired(MT201Details.getVerificationRequired());

			 /*
			  * For Input tags of Services
			  */

			 hashmap.put("AccountWithOption", mt201details.getAccountWithInstitutionOption());
			 hashmap.put("AccountWith", mt201details.getAccountWithInstitution());
			 hashmap.put("DealReference", mt201details.getTransactionReferenceNumber());
			 hashmap.put("InterBankSettledAmount", new BigDecimal(mt201details.getAmount()));
			 hashmap.put("InterBankSettledCurrency", mt201details.getCurrency());
			 hashmap.put("Intermediary", mt201details.getIntermediary());
			 hashmap.put("IntermediaryOption", mt201details.getIntermediaryOption());
			 hashmap.put("MessageType", mt200MessageType);
			 hashmap.put("Sender", MT201Details.getSender());
			 hashmap.put("SendersCorrespondent", MT201Details
					 .getSendersCorrespondent());
			 hashmap.put("SendersCorrespondentOption",MT201Details.getSendersCorrespondentOption());
			 hashmap.put("relatedReference", MT201Details.getInternalRef());
			 hashmap.put("sendertoReceiverInformation", mt201details.getSenderToReceiverInformation());
			 Date valueDate = StringToDate.run(MT201Details.getValueDate());
			 hashmap.put("ValueDate", valueDate);
			 hashmap.put("transactionReferenceNumber", mt201details
					 .getTransactionReferenceNumber());
		
			 list.add(Object200);
			 hashmap.put("Object", list);

			 hashmapout = MFExecuter.executeMF(
					 "UB_SWT_Incoming_MT200_Process_SRV", env, hashmap);
			 messageStatus = (String)hashmapout.get("MessageStatus");
			 errorCode = (String) hashmapout.get("ErrorNumber");
			 if (errorCode != null && errorCode.length() != 0){
				 errorNumber = Integer.parseInt(errorCode);
			 }
			 
			 if(hashmapout.get("hostTxnId")!=null) {
				 transactionId=hashmapout.get("hostTxnId").toString();
			 }
			 setF_OUT_transactionId(transactionId);
		 }

		 /*
		  * FOR MT103
		  */

		 if (getF_IN_MessageType().equals("MT103")) {
			 Object subType = null;
			 Object subType1 = null;
			 Ub_MT103 MT103 = getF_IN_Ub_MT103();
			 SwiftMT103 MT103Details = MT103.getDetails();
			 MessageHeader MT103header = MT103.getHeader();

			 UB_MT103 Object103 = new UB_MT103();
			 Object103.setStp(MT103Details.getStp());
			 Object103.setAccountWithInstitution(MT103Details
					 .getAccountWithInstitution());
			 Object103.setAccountWithInstOption(MT103Details
					 .getAccountWithInstOption());
			 Object103.setIntermediaryInstitution(MT103Details
					 .getIntermediaryInstitution());
			 Object103.setIntermediaryInstOption(MT103Details
					 .getIntermediaryInstOption());
			 Object103.setInternalRef(MT103Details.getInternalRef());
			 Object103.setMessageId(MT103header.getMessageId1());
			 Object103.setReceiver(MT103Details.getReceiver());
			 Object103.setSender(MT103Details.getSender());
			 Object103.setSendersCorrespOption(MT103Details
					 .getSendersCorrespOption());
			 Object103.setSendersCorrespondent(MT103Details
					 .getSendersCorrespondent());
			 Object103.setTdAmount(MT103Details.getTdAmount());
			 Object103.setTdCurrencyCode(MT103Details.getTdCurrencyCode());
			 Object103.setTdValueDate(MT103Details.getTdValueDate());
			 Object103.setAction((MT103Details.getAction()));
			 Object103.setBankOperationCode((MT103Details.getBankOperationCode()));
			 Object103.setBeneficiaryCustomer((MT103Details.getBeneficiaryCustomer()));
			 Object103.setBeneficiaryCustOption((MT103Details.getBeneficiaryCustOption()));
			 Object103.setBranch((MT103Details.getBranch()));
			 Object103.setDetailsOfCharges((MT103Details.getDetailsOfCharges()));
			 Object103.setDisposalRef((MT103Details.getDisposalRef()));
			 Object103.setExchangeRate((MT103Details.getExchangeRate()));
			 Object103.setMessageType((MT103header.getMessageType()));
			 Object103.setOrderingCustomer((MT103Details.getOrderingCustomer()));
			 Object103.setInstructedAmount((MT103Details.getInstructedAmount()));
			 Object103.setInstructedCurrency((MT103Details.getInstructedCurrency()));
			 Object103.setMultipleHold((MT103Details.getMultipleHold()));
			 Object103.setVerificationRequired(((MT103Details.getVerificationRequired())));
			 Object103.setOrderingCustomerOption(((MT103Details.getOrderingCustomerOption())));
			 Object103.setOrderingInstitution(((MT103Details.getOrderingInstitution())));
			 Object103.setOrderInstitutionOption(((MT103Details.getOrderInstitutionOption())));
			 Object103.setThirdReimbursementInstitution(((MT103Details.getThirdReimbursementInstitution())));
			 Object103.setThirdReimbursementInstOption(((MT103Details.getThirdReimbursementInstOption())));
			 Object103.setSendersReference(MT103Details.getSendersReference());
			 Object103.setTransactionTypeCode(MT103Details.getTransactionTypeCode());
			 Object103.setSendingInstitution(MT103Details.getSendingInstitution());
			 Object103.setReceiversCorrespondent(MT103Details.getReceiversCorrespondent());
			 Object103.setReceiversCorrespOption(MT103Details.getReceiversCorrespOption());
			 Object103.setRemittanceInfo(MT103Details.getRemittanceInfo());
			 Object103.setReceiversCharges(MT103Details.getReceiversCharges());
			 Object103.setSenderToReceiverInfo(MT103Details.getSenderToReceiverInfo());
			 Object103.setRegulatoryReporting(MT103Details.getRegulatoryReporting());
			 Object103.setEnvelopeContents(MT103Details.getEnvelopeContents());
			 Object103.setEndtoendTxnRef(MT103Details.getEnd2EndTxnRef());
			 Object103.setServiceTypeId(MT103Details.getServiceTypeId());

			 JXPathContext context = JXPathContext.newContext(MT103);

			 subType = context.getValue("/details/charges");
			 if(subType!=null){
				 subType = context.getValue("/details/charges/senderCharge");

				 if (subType instanceof Object[]) {
					 Object[] array = (Object[]) subType;
					 for (int counter = 0; counter < array.length; counter++) {
						 SendersCharges senderChargeDetails = new SendersCharges();
						 senderChargeDetails.setSenderCharge((array[counter]).toString());
						 Object103.addCharges(senderChargeDetails);
					 }
				 }
			 }

			 subType1=null;
			 subType1 = context.getValue("/details/instruction");
			 if(subType1!=null){
				 subType1 = context.getValue("/details/instruction/instructionCode");

				 if (subType1 instanceof Object[]) {
					 Object[] array = (Object[]) subType1;
					 for (int counter = 0; counter < array.length; counter++) {
						 InstructionCode instructionCode = new InstructionCode();
						 instructionCode.setInstructionCode(array[counter].toString());
						 Object103.addInstruction(instructionCode);
					 }

				 }
			 }


			 hashmap
			 .put("AccountWith", MT103Details
					 .getAccountWithInstitution());
			 hashmap.put("AccountWithOption", MT103Details
					 .getAccountWithInstOption());
			 hashmap.put("BeneficiaryCustomer", MT103Details
					 .getBeneficiaryCustomer());
			 if (MT103Details.getBeneficiaryCustOption()!= null && MT103Details.getBeneficiaryCustOption()!="") {
				 hashmap.put("BeneficiaryCustomerOption", MT103Details
						 .getBeneficiaryCustOption());     
			 }else {
				 hashmap.put("BeneficiaryCustomerOption", " ");
			 }

			 // Added the Ordering customer and Ordering Institution fields
			 hashmap.put("OrderingCustomer", MT103Details.getOrderingCustomer());
			 hashmap.put("OrderingCustomerOption", MT103Details.getOrderingCustomerOption());
			 hashmap.put("OrderingInstitution", MT103Details.getOrderingInstitution());
			 hashmap.put("OrderInstitutionOption", MT103Details.getOrderInstitutionOption());
			 hashmap.put("SendersReference", MT103Details.getSendersReference());


			 hashmap.put("DealReference", MT103Details.getSendersReference());
			 hashmap.put("DetailsOfCharge", MT103Details.getDetailsOfCharges());
			 hashmap.put("TransactionReferenceNumber", MT103Details.getSendersReference());
			 hashmap.put("InterBankSettledAmount", new BigDecimal(
					 MT103Details.getTdAmount()));
			 hashmap.put("InterBankSettledCurrency", MT103Details
					 .getTdCurrencyCode());
			 hashmap.put("Intermediary", MT103Details
					 .getIntermediaryInstitution());
			 hashmap.put("IntermediaryOption", MT103Details
					 .getIntermediaryInstOption());
			 hashmap.put("MessageType", getF_IN_MessageType());

			 String ReceiversCharges = MT103Details.getReceiversCharges();
			 if(null==ReceiversCharges)
				 ReceiversCharges = "0.00";
			 
			 if("N".equals(getF_IN_FromRemitScreen())){
				 if (ReceiversCharges != null
						 && ReceiversCharges.length() > 0) {
					 ReceiversCharges = ReceiversCharges.substring(3);
					 ReceiversCharges = ReceiversCharges
							 .replaceAll(",", ".");
				 }else{
					 ReceiversCharges="0.00";
				 }
			 }
			 
			 
			 hashmap.put("ReceiversCharges", new BigDecimal(
					 ReceiversCharges));
			 hashmap.put("ReceiversCorrespondent", MT103Details
					 .getReceiversCorrespondent());
			 hashmap.put("ReceiversCorrespondentOption", MT103Details
					 .getReceiversCorrespOption());
			 hashmap.put("Sender", MT103Details.getSender());
			 hashmap.put("SendersCorrespondent", MT103Details
					 .getSendersCorrespondent());

			 hashmap.put("SendersCorrespondentOption", MT103Details
					 .getSendersCorrespOption());
			 hashmap.put("ThirdReimbursementInstitution", MT103Details
					 .getThirdReimbursementInstitution());
			 hashmap.put("ThirdReimbursementInstitutionOption", MT103Details
					 .getThirdReimbursementInstOption());
			 Date valueDate = StringToDate.run(MT103Details.getTdValueDate());
			 hashmap.put("ValueDate", valueDate);
			 hashmap.put("ChargeCurrency",MT103Details.getTdCurrencyCode()); 
			 hashmap.put("exchangeRate", getF_IN_exchangeRate());
			 hashmap.put("creditNarrativeDescription", getF_IN_creditNarrativeDescription());
			 hashmap.put("debitNarrativeDescription", getF_IN_debitNarrativeDescription());
			 list.add(Object103);
			 hashmap.put("Object", list);
			 hashmap.put("MT103NarrativeCodes", UB_SWT_Util.generateMT103ComplexType(Object103));
			 hashmap.put("STP", STP);

			 hashmap.put("CreditAccount", getF_IN_CreditAccount());
			 hashmap.put("DebitAccount", getF_IN_DebitAccount());
			 hashmap.put("FromRemitScreen", getF_IN_FromRemitScreen());
			 hashmap.put("ChargeAmount", getF_IN_ChargeAmount());
			 hashmap.put("TransactionAmount", getF_IN_TransactionAmount());
			 hashmap.put("TaxAmount", getF_IN_TaxAmount());
			 hashmap.put("ChargeCalculationCode", getF_IN_ChargeCalculationCode());
			 hashmap.put("ChargeCode", getF_IN_ChargeCode());
			 hashmap.put("ChargeReceivingAccount", getF_IN_ChargeReceivingAccount());
			 hashmap.put("TaxCode", getF_IN_TaxCode());
			 hashmap.put("TaxNarrative", getF_IN_TaxNarrative());
			 hashmap.put("TaxReceivingAccount", getF_IN_TaxReceivingAccount());
			 hashmap.put("TransactionCode", getF_IN_TransactionCode());
			 hashmap.put("ChargeInterBankSettledAmount", getF_IN_ChargeInterBankSettledAmount());
			 hashmap.put("ChargeReceiversCharges", getF_IN_ChargeReceiversCharges());
			 
			 if(getF_IN_NonStpOurChargeVector() != null) {
				 hashmap.put("NonStpOurChargeVector", getF_IN_NonStpOurChargeVector());
			 }

			 //Populate Ordering Customer Details for 910
			 PopulateOrderingCustomerDetails(hashmap,MT103Details.getOrderingCustomer(),MT103Details.getOrderingCustomerOption());
			 if(!StringUtils.isBlank(MT103Details.getOrderingInstitution())){
				 PopulateOrderingInstitutionDetails(hashmap,MT103Details.getOrderingInstitution(),MT103Details.getOrderInstitutionOption());
			 }
			 
			
			 try{
				 hashmapout = MFExecuter.executeMF(
						 "UB_SWT_Incoming_MT103_Process_SRV", env, hashmap);
				 messageStatus = hashmapout.get("MessageStatus").toString();

				 messageStatus = (String)hashmapout.get("MessageStatus");
				 errorCode = (String) hashmapout.get("ErrorNumber");
				 creditAcntNumber = (String) hashmapout.get("CreditAccountNumber");
				 debitAcntNumber = (String) hashmapout.get("DebitAccountNumber");
				 if (errorCode != null && errorCode.length() != 0){
					 errorNumber = Integer.parseInt(errorCode);
				 }
				 
				 if(hashmapout.get("hostTxnId")!=null) {
					 transactionId=hashmapout.get("hostTxnId").toString();
				 }
				 setF_OUT_transactionId(transactionId);
            }
            catch (Exception exception) {
                BatchUtil.getExceptionAsString(exception);
				 if (exception instanceof CollectedEventsDialogException) {
                    CollectedEventsDialogException collectedException = (CollectedEventsDialogException) exception;
                    buildErrorResponseForCollectedEvents(collectedException);
                    List<ErrorEvent> errors = collectedException.getErrors();
                    for (ErrorEvent error : errors) {
                        errorNumber = error.getEventNumber();
                    }
					 messageStatus=FAILED_STATUS;
					 logger.error("Collected event raised for MT 103 in UB_SWT_ConvertObject ");
							 BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
							 BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
				 } else if (exception instanceof BankFusionException) {
                    BankFusionException bfe = ((BankFusionException) exception);
                    buildErrorResponse(bfe);
					 messageStatus=FAILED_STATUS;
                    errorNumber = bfe.getMessageNumber();
					 logger.error("Bankfusion error raised for MT 103  in UB_SWT_ConvertObject ");
							 BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
							 BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
				 } else {
					 messageStatus=FAILED_STATUS;
					 logger.error("Unknown exception raised for MT 103  in UB_SWT_ConvertObject ");
					 BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
					 BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
				 }
			 }


		 }
		 /*
		  * FOR MT202
		  */

		 if (getF_IN_MessageType().equals("MT202")) {
			 Ub_MT202 MT202 = getF_IN_Ub_MT202();
			 SwiftMT202 MT202Details = MT202.getDetails();
			 MessageHeader MT202header = MT202.getHeader();

			 UB_MT202 Object202 = new UB_MT202();

			 Object202.setTransactionReferenceNumber(MT202Details.getTransactionReferenceNumber());
			 Object202.setRelatedReference(MT202Details.getRelatedReference());
			 Object202.setTdValueDate(MT202Details.getTdValueDate());
			 Object202.setTdCurrencyCode(MT202Details.getTdCurrencyCode());
			 Object202.setTdAmount(MT202Details.getTdAmount());
			 Object202.setOrderingInstitution(MT202Details.getOrderingInstitution());
			 Object202.setOrderingInstitutionOption(MT202Details.getOrderingInstitutionOption());
			 Object202.setSendersCorrespondent(MT202Details.getSendersCorrespondent());
			 Object202.setSendersCorrespondentOption(MT202Details.getSendersCorrespondentOption());
			 Object202.setReceiversCorrespondent(MT202Details.getReceiversCorrespondent());
			 Object202.setReceiversCorrespondentOption(MT202Details.getReceiversCorrespondentOption());
			 Object202.setIntermediary(MT202Details.getIntermediary());
			 Object202.setIntermediaryOption(MT202Details.getIntermediaryOption());
			 Object202.setAccountWithInstitution(MT202Details.getAccountWithInstitution());
			 Object202.setAccountWithInstitutionOption(MT202Details.getAccountWithInstitutionOption());
			 Object202.setBeneficiary(MT202Details.getBeneficiary());
			 Object202.setBeneficiaryOption(MT202Details.getBeneficiaryOption());
			 Object202.setSendertoReceiverInformation(MT202Details.getSendertoReceiverInformation());
			 Object202.setInternalRef(MT202Details.getInternalRef());            
			 Object202.setMessageId(MT202header.getMessageId1());
			 Object202.setReceiver(MT202Details.getReceiver());
			 Object202.setSender(MT202Details.getSender());
			 Object202.setCoverMessage(MT202Details.getCover());
			 Object202.setAction(MT202Details.getAction());
			 Object202.setBranch(MT202Details.getBranch());
			 Object202.setDisposalRef(MT202Details.getDisposalRef());
			 Object202.setMessageType(MT202header.getMessageType());
			 Object202.setMultipleHold(MT202Details.getMultipleHold());
			 Object202.setVerificationRequired(MT202Details.getVerificationRequired());
			 Object202.setEnd2EndTxnRef(MT202Details.getEnd2EndTxnRef());
			 Object202.setServiceTypeId(MT202Details.getServiceTypeId());

			 hashmap.put("AccountWith", MT202Details.getAccountWithInstitution());
			 hashmap.put("AccountWithOption", MT202Details.getAccountWithInstitutionOption());
			 hashmap.put("BeneficiaryCustomer", MT202Details.getBeneficiary());
			 hashmap.put("BeneficiaryCustomerOption", MT202Details.getBeneficiaryOption());
			 hashmap.put("BeneficiaryInstitution", MT202Details.getBeneficiary());
			 hashmap.put("BeneficiaryInstitutionOption", MT202Details.getBeneficiaryOption());
			 hashmap.put("DealReference", MT202Details.getTransactionReferenceNumber());
			 hashmap.put("InterBankSettledAmount", new BigDecimal(MT202Details.getTdAmount()));
			 hashmap.put("InterBankSettledCurrency", MT202Details.getTdCurrencyCode());
			 hashmap.put("Intermediary", MT202Details.getIntermediary());
			 hashmap.put("IntermediaryOption", MT202Details.getIntermediaryOption());
			 hashmap.put("MessageType", getF_IN_MessageType());
			 hashmap.put("ReceiversCorrespondent", MT202Details.getReceiversCorrespondent());
			 hashmap.put("ReceiversCorrespondentOption", MT202Details.getReceiversCorrespondentOption());

			 //Added for Narrative
			 hashmap.put("OrderingInstitution", MT202Details.getOrderingInstitution());
			 hashmap.put("OrderingInstitutionOption", MT202Details.getOrderingInstitutionOption());


			 hashmap.put("Sender", MT202Details.getSender());
			 hashmap.put("SendersCorrespondent", MT202Details.getSendersCorrespondent());
			 hashmap.put("SendersCorrespondentOption", MT202Details.getSendersCorrespondentOption());
			 hashmap.put("relatedReference",MT202Details.getRelatedReference());
			 hashmap.put("sendertoReceiverInformation",MT202Details.getSendertoReceiverInformation());
			 Date valueDate = StringToDate.run(MT202Details.getTdValueDate());
			 hashmap.put("ValueDate", valueDate);
			 hashmap.put("transactionReferenceNumber", MT202Details.getTransactionReferenceNumber());
			 list.add(Object202);
			 hashmap.put("MT202NarrativeCodes", UB_SWT_Util.generateMT202ComplexType(Object202));

			 hashmap.put("CreditAccount", getF_IN_CreditAccount());
			 hashmap.put("DebitAccount", getF_IN_DebitAccount());
			 hashmap.put("FromRemitScreen", getF_IN_FromRemitScreen());
			 
			 hashmap.put("exchangeRate", getF_IN_exchangeRate());
             hashmap.put("DrNarrative", getF_IN_debitNarrativeDescription());
             hashmap.put("CrNarrative", getF_IN_creditNarrativeDescription());

			 hashmap.put("Object", list);
			 hashmapout = MFExecuter.executeMF(
					 "UB_SWT_Incoming_MT202_Process_SRV", env, hashmap);
			 messageStatus = (String)hashmapout.get("MessageStatus");
			 errorCode = (String) hashmapout.get("ErrorNumber");
			 creditAcntNumber = (String) hashmapout.get("CreditAccountNumber");
			 debitAcntNumber = (String) hashmapout.get("DebitAccountNumber");
			 if (errorCode != null && errorCode.length() != 0){
				 errorNumber = Integer.parseInt(errorCode);
			 }
			 if(hashmapout.get("hostTxnId")!=null) {
				 transactionId=hashmapout.get("hostTxnId").toString();
			 }
			 setF_OUT_transactionId(transactionId);

		 }

		 /*
		  * For MT203
		  *  
		  */

		 if (getF_IN_MessageType().equals("MT203")){
			 Ub_MT203 MT203 = getF_IN_UB_MT203();
			 SwiftMT203 MT203Details = MT203.getDetails();
			 //                         MessageHeader MT203header = MT203.getHeader();
			 Mt203Detail mt203details= getF_IN_MT203Detail();
			 UB_MT202 Object202 = new UB_MT202();

			 Object202.setTransactionReferenceNumber(mt203details.getTransactionReferenceNumber());
			 Object202.setRelatedReference(mt203details.getRelatedReference());
			 Object202.setTdValueDate(MT203Details.getValueDate());                                           
			 Object202.setTdAmount(mt203details.getAmount());
			 Object202.setTdCurrencyCode(mt203details.getCurrencyCode());
			 Object202.setOrderingInstitution(MT203Details.getOrderingInstitute());
			 Object202.setOrderingInstitutionOption(MT203Details.getOrderingInstituteOption());
			 Object202.setSendersCorrespondent(MT203Details.getSendersCorrespondent());                                           
			 Object202.setSendersCorrespondentOption(MT203Details.getSendersCorrespondentOption());
			 Object202.setReceiversCorrespondent(MT203Details.getReceiversCorrespondent());
			 Object202.setReceiversCorrespondentOption(MT203Details.getReceiversCorrespondentOption());
			 Object202.setIntermediary(mt203details.getIntermediary());
			 Object202.setIntermediaryOption(mt203details.getIntermediaryOption());
			 Object202.setAccountWithInstitution(mt203details.getAccountWithInstitution());
			 Object202.setAccountWithInstitutionOption(mt203details.getAccountWithInstitutionOption());
			 Object202.setBeneficiary(mt203details.getBeneficiary());
			 Object202.setBeneficiaryOption(mt203details.getBeneficiaryOption());
			 Object202.setSendertoReceiverInformation(MT203Details.getSendertoReceiverInformation());
			 Object202.setInternalRef(MT203Details.getInternalRef());
			 Object202.setReceiver(MT203Details.getReceiver());
			 Object202.setSender(MT203Details.getSender());
			 Object202.setAction(MT203Details.getAction());
			 Object202.setBranch(MT203Details.getBranch());
			 Object202.setDisposalRef(MT203Details.getDisposalRef());
			 Object202.setMessageType(mt202MessageType);
			 Object202.setMultipleHold(MT203Details.getMultipleHold());
			 Object202.setVerificationRequired(MT203Details.getVerificationRequired());

			 /*
			  * For Input tags of Services
			  */

			 hashmap.put("AccountWith", mt203details.getAccountWithInstitution());
			 hashmap.put("AccountWithOption", mt203details.getAccountWithInstitutionOption());
			 hashmap.put("BeneficiaryCustomer", mt203details.getBeneficiary());
			 hashmap.put("BeneficiaryCustomerOption", mt203details.getBeneficiaryOption());
			 hashmap.put("BeneficiaryInstitution", mt203details.getBeneficiary());
			 hashmap.put("BeneficiaryInstitutionOption", mt203details.getBeneficiaryOption());
			 hashmap.put("DealReference", mt203details.getTransactionReferenceNumber());
			 hashmap.put("InterBankSettledAmount", new BigDecimal(mt203details.getAmount()));
			 hashmap.put("InterBankSettledCurrency", mt203details.getCurrencyCode());
			 hashmap.put("Intermediary", mt203details.getIntermediary());
			 hashmap.put("IntermediaryOption", mt203details.getIntermediaryOption());
			 hashmap.put("MessageType", mt202MessageType);
			 hashmap.put("ReceiversCorrespondent", MT203Details.getReceiversCorrespondent());
			 hashmap.put("ReceiversCorrespondentOption", MT203Details.getReceiversCorrespondentOption());

			 //Added for Narrative
			 hashmap.put("OrderingInstitution",MT203Details.getOrderingInstitute());
			 hashmap.put("OrderingInstitutionOption", MT203Details.getOrderingInstituteOption());


			 hashmap.put("Sender", MT203Details.getSender());
			 hashmap.put("SendersCorrespondent", MT203Details.getSendersCorrespondent());
			 hashmap.put("SendersCorrespondentOption", MT203Details.getSendersCorrespondentOption());
			 hashmap.put("relatedReference",mt203details.getRelatedReference());
			 hashmap.put("sendertoReceiverInformation",MT203Details.getSendertoReceiverInformation());
			 Date valueDate = StringToDate.run(MT203Details.getValueDate());
			 hashmap.put("ValueDate", valueDate);
			 hashmap.put("transactionReferenceNumber", mt203details.getTransactionReferenceNumber());
			 list.add(Object202);
			 hashmap.put("Object", list);

			 hashmapout = MFExecuter.executeMF(
					 "UB_SWT_Incoming_MT202_Process_SRV", env, hashmap);
			 messageStatus = (String)hashmapout.get("MessageStatus");
			 errorCode = (String) hashmapout.get("ErrorNumber");
			 if (errorCode != null && errorCode.length() != 0){
				 errorNumber = Integer.parseInt(errorCode);
			 }
			 if(hashmapout.get("hostTxnId")!=null) {
				 transactionId=hashmapout.get("hostTxnId").toString();
			 }
			 setF_OUT_transactionId(transactionId);
		 }




		 /*
		  * FOR MT205
		  */

		 if (getF_IN_MessageType().equals("MT205")) {
			 Ub_MT205 MT205 = getF_IN_Ub_MT205();
			 SwiftMT205 MT205Details = MT205.getDetails();
			 MessageHeader MT205header = MT205.getHeader();

			 UB_MT205 Object205 = new UB_MT205();

			 Object205.setTransactionReferenceNumber(MT205Details.getTransactionReferenceNumber());
			 Object205.setRelatedReference(MT205Details.getRelatedReference());
			 Object205.setTdvalueDate(MT205Details.getTdvalueDate());
			 Object205.setTdcurrencyCode(MT205Details.getTdcurrencyCode());                                   
			 Object205.setTdamount(MT205Details.getTdamount());
			 Object205.setOrderingInstitution(MT205Details.getOrderingInstitute());
			 Object205.setOrderingInstOption(MT205Details.getOrderingInstitutionOption());
			 Object205.setSendersCorrespondent(MT205Details.getSendersCorrespondent());
			 Object205.setSendersCorresOption(MT205Details.getSendersCorresOption());
			 Object205.setIntermediary(MT205Details.getIntermediary());
			 Object205.setIntermediaryOption(MT205Details.getIntermediaryOption());
			 Object205.setAccountWithInstitution(MT205Details.getAccountWithInstitution());
			 Object205.setAccountWithInstOption(MT205Details.getAccountWithInstOption());
			 Object205.setBeneficiaryInstitute(MT205Details.getBeneficiaryInstitute());
			 Object205.setBeneficiaryInstOption(MT205Details.getBeneficiaryInstOption());
			 Object205.setSenderToReceiverInformation(MT205Details.getSenderToReceiverInformation());
			 Object205.setInternalRef(MT205Details.getInternalRef());
			 Object205.setMessageId(MT205header.getMessageId1());
			 Object205.setReceiver(MT205Details.getReceiver());
			 Object205.setSender(MT205Details.getSender());
			 Object205.setAction(MT205Details.getAction());
			 Object205.setBranch(MT205Details.getBranch());
			 Object205.setDisposalRef(MT205Details.getDisposalRef());
			 Object205.setMessageType(MT205header.getMessageType());
			 Object205.setMultipleHold(MT205Details.getMultipleHold());
			 Object205.setVerificationRequired(MT205Details.getVerificationRequired());
			 Object205.setEnd2EndTxnRef(MT205Details.getEnd2EndTxnRef());
			 Object205.setServiceTypeId(MT205Details.getServiceTypeId());

			 hashmap
			 .put("AccountWith", MT205Details
					 .getAccountWithInstitution());
			 hashmap.put("AccountWithOption", MT205Details
					 .getAccountWithInstOption());
			 hashmap.put("BeneficiaryInstitution", MT205Details.getBeneficiaryInstitute());
			 hashmap.put("BeneficiaryInstitutionOption", MT205Details
					 .getBeneficiaryInstOption());
			 hashmap.put("DealReference", MT205Details.getTransactionReferenceNumber());
			 // hashmap.put("DetailsOfCharge", MT205Details
					 // .getDetailsOfCharges());
			 hashmap.put("TransactionReferenceNumber", MT205Details
					 .getTransactionReferenceNumber());
			 String InterBankSettledAmount = MT205Details.getTdamount();
			 InterBankSettledAmount = InterBankSettledAmount
					 .replaceAll(",", ".");
			 hashmap.put("InterBankSettledAmount", new BigDecimal(
					 InterBankSettledAmount));
			 hashmap.put("InterBankSettledCurrency", MT205Details
					 .getTdcurrencyCode());
			 hashmap.put("Intermediary", MT205Details.getIntermediary());
			 hashmap.put("IntermediaryOption", MT205Details
					 .getIntermediaryOption());
			 hashmap.put("MessageType", getF_IN_MessageType());

			 /*
			  * artf1002376 - The ReceiversCorrespondent is not required for MT205
			  * and MT205Cov, hence the below statement is commented
			  */
			 //hashmap.put("ReceiversCorrespondent", MT205Details
			 //                         .getReceiver());


			 //Added for Narrative
			 hashmap.put("OrderingInstitution", MT205Details.getOrderingInstitute());
			 hashmap.put("OrderingInstitutionOption", MT205Details.getOrderingInstitutionOption());

			 hashmap.put("Sender", MT205Details.getSender());
			 hashmap.put("SendersCorrespondent", MT205Details
					 .getSendersCorrespondent());

			 hashmap.put("SendersCorrespondentOption", MT205Details
					 .getSendersCorresOption());

			 Date valueDate = StringToDate.run(MT205Details.getTdvalueDate());
			 hashmap.put("ValueDate", valueDate);

			 hashmap.put("CreditAccount", getF_IN_CreditAccount());
			 hashmap.put("DebitAccount", getF_IN_DebitAccount());
			 hashmap.put("FromRemitScreen", getF_IN_FromRemitScreen());
			 hashmap.put("exchangeRate", getF_IN_exchangeRate());
             hashmap.put("DrNarrative", getF_IN_debitNarrativeDescription());
             hashmap.put("CrNarrative", getF_IN_creditNarrativeDescription());

			 list.add(Object205);
			 hashmap.put("Object", list);
			 hashmapout = MFExecuter.executeMF(
					 "UB_SWT_Incoming_MT205_Process_SRV", env, hashmap);

			 messageStatus = (String)hashmapout.get("MessageStatus");
			 errorCode = (String) hashmapout.get("ErrorNumber");
			 if (errorCode != null && errorCode.length() != 0){
				 errorNumber = Integer.parseInt(errorCode);
			 }
			 
			 if(hashmapout.get("hostTxnId")!=null) {
				 transactionId=hashmapout.get("hostTxnId").toString();
			 }
			
			 setF_OUT_transactionId(transactionId);
		 }
		 setF_OUT_ErrorNumber(errorNumber);
		 setF_OUT_MessageStatus(messageStatus);
		 setF_OUT_creditAcntNumber(creditAcntNumber);
		 setF_OUT_debitAcntNumber(debitAcntNumber);

	}

	void PopulateOrderingCustomerDetails(HashMap hashmap, String orderingCustomer, String orderingCustomerOption)
	{
		String[] orderingCustomerDetails = getSeperateTags(orderingCustomer, orderingCustomerOption);

		if (orderingCustomerOption.equalsIgnoreCase("A")) {
			if (orderingCustomerDetails.length == 2 ) {
				hashmap.put("OrderingCustAccount",orderingCustomerDetails[0]);
				hashmap.put("OrderingCustIdentifier",orderingCustomerDetails[1]);
			} else
			{
				hashmap.put("OrderingCustAccount",CommonConstants.EMPTY_STRING);
				hashmap.put("OrderingCustIdentifier",orderingCustomerDetails[0]);
			}
			hashmap.put("OrderingCustLine1",CommonConstants.EMPTY_STRING);
			hashmap.put("OrderingCustLine2",CommonConstants.EMPTY_STRING);
			hashmap.put("OrderingCustLine3",CommonConstants.EMPTY_STRING);
			hashmap.put("OrderingCustLine4",CommonConstants.EMPTY_STRING);
		}
		else if (orderingCustomerOption.equalsIgnoreCase("F") || orderingCustomerOption.equalsIgnoreCase("K")){
			hashmap.put("OrderingCustIdentifier",CommonConstants.EMPTY_STRING);
			hashmap.put("OrderingCustAccount",orderingCustomerDetails[0]);
			hashmap.put("OrderingCustLine1",orderingCustomerDetails[1]);
			hashmap.put("OrderingCustLine2",orderingCustomerDetails[2]);
			hashmap.put("OrderingCustLine3",orderingCustomerDetails[3]);
			hashmap.put("OrderingCustLine4",orderingCustomerDetails[4]);
		}
	}
	
	void PopulateOrderingInstitutionDetails(HashMap hashmap, String orderingInstitution, String orderingInstitutionOption)
	{ 
		String[] orderingInstitutionDetails = getSeperateTags(orderingInstitution, orderingInstitutionOption);
		if (orderingInstitution.equalsIgnoreCase("A")) {
           hashmap.put("OrderingInstitution", orderingInstitutionDetails[0]);	
		}
		else if (orderingInstitutionOption.equalsIgnoreCase("D")){
			hashmap.put("OrderingInstitution",CommonConstants.EMPTY_STRING);
		}
	}
	private static String[] getSeperateTags(String field, String option) {
		String[] array = field.split("[$]");
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < 5; i++) {
			list.add(array.length >= i + 1 ? array[i] : CommonConstants.EMPTY_STRING);
			 if ((option.equalsIgnoreCase("A")) && (array.length == i + 1)) {
			        break;
			      }
		}
		return list.toArray(array);
	}

    /**
     * Method Description:Build Error Response for bankfusion events
     * 
     * @param e
     */
    private void buildErrorResponse(BankFusionException e) {
        IEvent errors = e.getEvents().iterator().next();
        int error = e.getEvents().iterator().next().getEventNumber();
        String errorCode = Integer.toString(error);
        SubCode subCode = new SubCode();
        MessageStatus txnStatus = new MessageStatus();
        Object parameterList = new Object();
        if (errors.getDetails() != null && errors.getDetails().length != 0) {
            for (int i = 0; i < errors.getDetails().length; i++) {
                EventParameters parameter = new EventParameters();
                parameterList = errors.getDetails()[i];
                parameter.setEventParameterValue(parameterList.toString());
                subCode.addParameters(parameter);
            }
        }
        subCode.setCode(errorCode);
        subCode.setDescription(e.getEvents().iterator().next().getMessage());
        subCode.setFieldName(CommonConstants.EMPTY_STRING);
        subCode.setSeverity(CBSConstants.ERROR);
        txnStatus.addCodes(subCode);
        txnStatus.setOverallStatus("E");
        setF_OUT_ErrorMessageStatus(txnStatus);
    }

    /**
     * Method Description:Build Error Response for Collected Events
     * 
     * @param collectedException
     */
    private void buildErrorResponseForCollectedEvents(CollectedEventsDialogException collectedException) {
        List<ErrorEvent> errors = collectedException.getErrors();
        String eventCode = "";
        SubCode subCode = new SubCode();
        Object parameterList = new Object();
        MessageStatus txnStatus = new MessageStatus();
        if (!errors.isEmpty()) {
            for (IEvent event : errors) {
                String code = String.valueOf(event.getEventNumber());
                if (!code.isEmpty()) {
                    eventCode = code;
                    for (int j = 0; j < event.getDetails().length; j++) {
                        EventParameters parameter = new EventParameters();
                        parameterList = event.getDetails()[j];
                        parameter.setEventParameterValue(parameterList.toString());
                        subCode.addParameters(parameter);
                    }
                    break;
                }

            }
            subCode.setCode(eventCode);
            subCode.setDescription(collectedException.getEvents().iterator().next().getMessage());
            subCode.setFieldName(CommonConstants.EMPTY_STRING);
            subCode.setSeverity(CBSConstants.ERROR);
            txnStatus.addCodes(subCode);
            txnStatus.setOverallStatus("E");
            setF_OUT_ErrorMessageStatus(txnStatus);
        }
    }

}
