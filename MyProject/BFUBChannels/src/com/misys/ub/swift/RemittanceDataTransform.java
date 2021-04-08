package com.misys.ub.swift;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;

import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_SWTMessageDetail;
import com.trapedza.bankfusion.boundary.outward.BankFusionIOSupport;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

import bf.com.misys.ub.types.interfaces.MessageHeader;
import bf.com.misys.ub.types.interfaces.SenderCharge;
import bf.com.misys.ub.types.interfaces.SwiftMT103;
import bf.com.misys.ub.types.interfaces.SwiftMT200;
import bf.com.misys.ub.types.interfaces.SwiftMT202;
import bf.com.misys.ub.types.interfaces.SwiftMT205;
import bf.com.misys.ub.types.interfaces.Ub_MT103;
import bf.com.misys.ub.types.interfaces.Ub_MT200;
import bf.com.misys.ub.types.interfaces.Ub_MT202;
import bf.com.misys.ub.types.interfaces.Ub_MT205;
import bf.com.misys.ub.types.remittanceprocess.UB_SWT_RemittanceProcessRq;

public class RemittanceDataTransform {

    private static String SWTCUSTOMERWHERECLAUSE = " WHERE " + IBOSwtCustomerDetail.BICCODE + " = ?";

    IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

    private IBOUB_INF_SWTMessageDetail getMessageDetail(String messageNumber) {
        return (IBOUB_INF_SWTMessageDetail) (factory
                .findByPrimaryKey(IBOUB_INF_SWTMessageDetail.BONAME, messageNumber, true));
    }
    
	public Ub_MT103 getMT103FromRemittance(UB_SWT_RemittanceProcessRq remittanceProcessRq){


		IBOUB_INF_SWTMessageDetail msgDtl = getMessageDetail(remittanceProcessRq.getREMITTANCE_ID());
		Ub_MT103 msg = (Ub_MT103) BankFusionIOSupport.convertFromBytes(msgDtl.getF_MESSAGEOBJECT());

		Ub_MT103 ubMT103 = new Ub_MT103();
		MessageHeader messageHeader = new MessageHeader();
		SwiftMT103 swiftMT103 = new SwiftMT103();
		
		messageHeader.setMessageId1(msg.getHeader().getMessageId1());
		messageHeader.setMessageType(msg.getHeader().getMessageType());
		ubMT103.setHeader(messageHeader);
		
		swiftMT103.setSender(msg.getDetails().getSender());
		swiftMT103.setReceiver(msg.getDetails().getReceiver());
		swiftMT103.setAction(msg.getDetails().getAction());
		swiftMT103.setSendersReference(msg.getDetails().getSendersReference());
		swiftMT103.setTdValueDate(convertDateFormat(remittanceProcessRq.getTRANSACTIONDETAISINFO().getDATEOFPROCESSING()));
		swiftMT103.setTdAmount(String.valueOf(remittanceProcessRq.getDEBITORDTL().getDEBITAMOUNT()));
		swiftMT103.setTdCurrencyCode(remittanceProcessRq.getTRANSACTIONDETAISINFO().getCURRENCY());

		swiftMT103.setBankOperationCode(remittanceProcessRq.getBANKTOBANKINFO().getBANKOPERATIONCODE());
		swiftMT103.setDetailsOfCharges(remittanceProcessRq.getRemittanceINFO().getCHARGECODE());
		if(null!=msg.getDetails().getReceiversCharges() && msg.getDetails().getReceiversCharges().length()>0){
			String receiversCharge = msg.getDetails().getReceiversCharges().substring(3);
			swiftMT103.setReceiversCharges(receiversCharge);
		}
		if("OUR".equals(remittanceProcessRq.getRemittanceINFO().getCHARGECODE()) && null==msg.getDetails().getReceiversCharges() && remittanceProcessRq.getCHARGERELATEDINFO().getChargeAmount().compareTo(BigDecimal.ZERO)==1){
			String receiversCharge = String.valueOf(remittanceProcessRq.getCHARGERELATEDINFO().getChargeAmount());
			swiftMT103.setReceiversCharges(receiversCharge);
		}
		
		SenderCharge senderCharge = new SenderCharge();
		String [] senderChargeArray = new String[1];
		senderChargeArray[0] = String.valueOf(remittanceProcessRq.getCHARGERELATEDINFO().getSendersCharge());
		senderCharge.setSenderCharge(senderChargeArray);
		swiftMT103.setCharges(senderCharge);
		swiftMT103.setInstructedAmount(String.valueOf(remittanceProcessRq.getCREDITORDTL().getEXPECTEDCREDITAMOUNT()));
		swiftMT103.setInstructedCurrency(msg.getDetails().getInstructedCurrency());

		String orderingCustomer = "";
		if(null!=remittanceProcessRq.getORDERINGICUSTINFO().getORDCUSTPTYIDCODE() && remittanceProcessRq.getORDERINGICUSTINFO().getORDCUSTPTYIDCODE().length()>0){
			orderingCustomer = "/"+remittanceProcessRq.getORDERINGICUSTINFO().getORDCUSTPTYIDCODE();
		}
		if(null!=remittanceProcessRq.getORDERINGICUSTINFO().getORDCUSTIDENBIC() && remittanceProcessRq.getORDERINGICUSTINFO().getORDCUSTIDENBIC().length()>0){
			orderingCustomer = orderingCustomer.length()>0?(orderingCustomer+"$"+remittanceProcessRq.getORDERINGICUSTINFO().getORDCUSTIDENBIC()):remittanceProcessRq.getORDERINGICUSTINFO().getORDCUSTIDENBIC();
		}
		orderingCustomer = orderingCustomer+getDollarSeparatedText(remittanceProcessRq.getORDERINGICUSTINFO().getORDERINGICUSTINFO1(), remittanceProcessRq.getORDERINGICUSTINFO().getORDERINGICUSTINFO2(), remittanceProcessRq.getORDERINGICUSTINFO().getORDERINGICUSTINFO3(), remittanceProcessRq.getORDERINGICUSTINFO().getORDERINGICUSTINFO4());
		swiftMT103.setOrderingCustomer(orderingCustomer);
		swiftMT103.setOrderingCustomerOption(msg.getDetails().getOrderingCustomerOption());
		String orderingInstitution = "";
		if(null!=remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTPARTYIDENTCODE() && remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTPARTYIDENTCODE().length()>0){
			orderingInstitution = "/"+remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTPARTYIDENTCODE();
		}
		if(null!=remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONIDENTCODE() && remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONIDENTCODE().length()>0){
			orderingInstitution = orderingInstitution.length()>0?(orderingInstitution+"$"+remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONIDENTCODE()):remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONIDENTCODE();
		}
		if(!"A".equals(msg.getDetails().getOrderInstitutionOption())){
			orderingInstitution = orderingInstitution+getDollarSeparatedText(remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONDTL1(), remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONDTL2(), remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONDTL3(), remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONDTL4());
		}
		swiftMT103.setOrderingInstitution(orderingInstitution);
		swiftMT103.setOrderInstitutionOption(msg.getDetails().getOrderInstitutionOption());
		
		swiftMT103.setSendingInstitution(msg.getDetails().getSendingInstitution());
	
		String senderCorrespondent = "";
		String receiverCorrespondent = "";
		swiftMT103.setThirdReimbursementInstitution(msg.getDetails().getThirdReimbursementInstitution());
		swiftMT103.setThirdReimbursementInstOption(msg.getDetails().getThirdReimbursementInstOption());

		String accountWithInst = "";
		if(remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARINSTYPARTYIDENTIFIER().length()>0){
			accountWithInst = "/"+remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARINSTYPARTYIDENTIFIER();
		}
		if(remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTIDENTCODE().length()>0){
			accountWithInst = accountWithInst.length()>0?(accountWithInst+"$"+remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTIDENTCODE()):remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTIDENTCODE();
			//accountWithInst = "/"+remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARINSTYPARTYIDENTIFIER();
		}
		if("D".equals(msg.getDetails().getAccountWithInstOption())){
			accountWithInst = accountWithInst+getDollarSeparatedText(remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTTEXT1(), remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTTEXT2(), remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTTEXT3(), remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTTEXT4());
		}
		
		swiftMT103.setAccountWithInstitution(accountWithInst);
		swiftMT103.setAccountWithInstOption(msg.getDetails().getAccountWithInstOption());

		String beneficiary = "";
		if(null!=remittanceProcessRq.getBENEFICIARYCUSTOMERINFO().getBENEFICIARYCUSTPARTYIDENTIFIER() && remittanceProcessRq.getBENEFICIARYCUSTOMERINFO().getBENEFICIARYCUSTPARTYIDENTIFIER().length()>0){
			beneficiary = "/"+remittanceProcessRq.getBENEFICIARYCUSTOMERINFO().getBENEFICIARYCUSTPARTYIDENTIFIER();
		}
		if(null!=remittanceProcessRq.getBENEFICIARYCUSTOMERINFO().getBENEFICIARYCUSTIDENTCODE() && remittanceProcessRq.getBENEFICIARYCUSTOMERINFO().getBENEFICIARYCUSTIDENTCODE().length()>0){
			beneficiary = beneficiary.length()>0?(beneficiary+"$"+remittanceProcessRq.getBENEFICIARYCUSTOMERINFO().getBENEFICIARYCUSTIDENTCODE()):remittanceProcessRq.getBENEFICIARYCUSTOMERINFO().getBENEFICIARYCUSTIDENTCODE();
		}
		if(!"A".equals(msg.getDetails().getBeneficiaryCustOption())){
			beneficiary = beneficiary+getDollarSeparatedText(remittanceProcessRq.getBENEFICIARYCUSTOMERINFO().getBENEFICIARYCUSTTEXT1(), remittanceProcessRq.getBENEFICIARYCUSTOMERINFO().getBENEFICIARYCUSTTEXT2(), remittanceProcessRq.getBENEFICIARYCUSTOMERINFO().getBENEFICIARYCUSTTEXT3(), remittanceProcessRq.getBENEFICIARYCUSTOMERINFO().getBENEFICIARYCUSTTEXT4());
		}
		
		swiftMT103.setBeneficiaryCustomer(beneficiary);
		swiftMT103.setBeneficiaryCustOption(msg.getDetails().getBeneficiaryCustOption());
		
		String intermediaryInstitution = "";
		if(isBICExistInSwtCutDtls(msg.getDetails().getSender())){
			if(msg.getDetails().getSendersCorrespondent() != null){
				if(null!=remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOPARTYIDENTIFIER() && remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOPARTYIDENTIFIER().length()>0){
					senderCorrespondent = "/"+remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOPARTYIDENTIFIER();
				}
				if(null!=remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOIDENTCODE() && remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOIDENTCODE().length()>0){
					senderCorrespondent = senderCorrespondent.length()>0?(senderCorrespondent+"$"+remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOIDENTCODE()):remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOIDENTCODE();
				}
				senderCorrespondent = senderCorrespondent+getDollarSeparatedText(remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOTEXT1(), remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOTEXT2(), remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOTEXT3(), remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOTEXT4());
			}
			if(msg.getDetails().getReceiversCorrespondent() != null){
				if(null!=remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR() && remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR().length()>0){
					receiverCorrespondent = "/"+remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR();
				}
				if(null!=remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE() && remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE().length()>0){
					receiverCorrespondent = receiverCorrespondent.length()>0?(receiverCorrespondent+"$"+remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE()):remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE();
				}
				receiverCorrespondent = receiverCorrespondent+getDollarSeparatedText(remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT1(), remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT2(), remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT3(), remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT4());
				intermediaryInstitution = msg.getDetails().getIntermediaryInstitution();
			}else if(msg.getDetails().getIntermediaryInstitution() != null){
				if(null!=remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR() && remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR().length()>0){
					intermediaryInstitution = "/"+remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR();
				}
				if(null!=remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE() && remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE().length()>0){
					intermediaryInstitution = intermediaryInstitution.length()>0?(receiverCorrespondent+"$"+remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE()):remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE();
				}
				if(("D".equals(msg.getDetails().getIntermediaryInstOption())) ){
					intermediaryInstitution = intermediaryInstitution+getDollarSeparatedText(remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT1(), remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT2(), remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT3(), remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT4());
				}
				
			}
		}
		else{
			if(msg.getDetails().getReceiversCorrespondent() != null){
				if(null!=remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOPARTYIDENTIFIER() && remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOPARTYIDENTIFIER().length()>0){
					receiverCorrespondent = "/"+remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOPARTYIDENTIFIER();
				}
				if(null!=remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOIDENTCODE() && remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOIDENTCODE().length()>0){
					receiverCorrespondent = receiverCorrespondent.length()>0?(receiverCorrespondent+"$"+remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOIDENTCODE()):remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOIDENTCODE();
				}
				receiverCorrespondent = receiverCorrespondent+getDollarSeparatedText(remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOTEXT1(), remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOTEXT2(), remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOTEXT3(), remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOTEXT4());
				
			}
			if(msg.getDetails().getSendersCorrespondent() != null){
				if(null!=remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR() && remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR().length()>0){
					senderCorrespondent = "/"+remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR();
				}
				if(null!=remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE() && remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE().length()>0){
					senderCorrespondent = senderCorrespondent.length()>0?(receiverCorrespondent+"$"+remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE()):remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE();
				}
				senderCorrespondent = senderCorrespondent+getDollarSeparatedText(remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT1(), remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT2(), remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT3(), remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT4());
				intermediaryInstitution = msg.getDetails().getIntermediaryInstitution();
				
			}
			else if(msg.getDetails().getIntermediaryInstitution() != null){
				if(null!=remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR() && remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR().length()>0){
					intermediaryInstitution = "/"+remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR();
				}
				if(null!=remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE() && remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE().length()>0){
					intermediaryInstitution = intermediaryInstitution.length()>0?(intermediaryInstitution+"$"+remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE()):remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE();
				}
				if(("D".equals(msg.getDetails().getIntermediaryInstOption())) ){
					intermediaryInstitution = intermediaryInstitution+getDollarSeparatedText(remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT1(), remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT2(), remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT3(), remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT4());
				}
				
			}
		}
		
		swiftMT103.setSendersCorrespondent(senderCorrespondent);
		swiftMT103.setSendersCorrespOption(msg.getDetails().getSendersCorrespOption());
		swiftMT103.setReceiversCorrespondent(receiverCorrespondent);
		swiftMT103.setReceiversCorrespOption(msg.getDetails().getReceiversCorrespOption());
		if(intermediaryInstitution!=null && intermediaryInstitution.length()>0){
			swiftMT103.setIntermediaryInstitution(intermediaryInstitution);
			swiftMT103.setIntermediaryInstOption(msg.getDetails().getIntermediaryInstOption());
		}
		
			
		String remittanceInfo = "";
		remittanceInfo = remittanceProcessRq.getRemittanceINFO().getREMITTANCEINFO1();
		remittanceInfo = (remittanceProcessRq.getRemittanceINFO().getREMITTANCEINFO2().length()>0)?remittanceInfo+"$"+remittanceProcessRq.getRemittanceINFO().getREMITTANCEINFO2():remittanceInfo;
		remittanceInfo = remittanceProcessRq.getRemittanceINFO().getREMITTANCEINFO3().length()>0?remittanceInfo+"$"+remittanceProcessRq.getRemittanceINFO().getREMITTANCEINFO3():remittanceInfo;
		remittanceInfo = remittanceProcessRq.getRemittanceINFO().getREMITTANCEINFO4().length()>0?remittanceInfo+"$"+remittanceProcessRq.getRemittanceINFO().getREMITTANCEINFO4():remittanceInfo;
		swiftMT103.setRemittanceInfo(remittanceInfo);

		String senderToReceiverInfo = "";
		senderToReceiverInfo = remittanceProcessRq.getSENDERTORECEIVERINFO().getSENDERTORECEIVERINFO1();
		senderToReceiverInfo = remittanceProcessRq.getSENDERTORECEIVERINFO().getSENDERTORECEIVERINFO2().length()>0?senderToReceiverInfo+"$"+remittanceProcessRq.getSENDERTORECEIVERINFO().getSENDERTORECEIVERINFO2():senderToReceiverInfo;
		senderToReceiverInfo = remittanceProcessRq.getSENDERTORECEIVERINFO().getSENDERTORECEIVERINFO3().length()>0?senderToReceiverInfo+"$"+remittanceProcessRq.getSENDERTORECEIVERINFO().getSENDERTORECEIVERINFO3():senderToReceiverInfo;
		senderToReceiverInfo = remittanceProcessRq.getSENDERTORECEIVERINFO().getSENDERTORECEIVERINFO4().length()>0?senderToReceiverInfo+"$"+remittanceProcessRq.getSENDERTORECEIVERINFO().getSENDERTORECEIVERINFO4():senderToReceiverInfo;
		senderToReceiverInfo = remittanceProcessRq.getSENDERTORECEIVERINFO().getSENDERTORECEIVERINFO5().length()>0?senderToReceiverInfo+"$"+remittanceProcessRq.getSENDERTORECEIVERINFO().getSENDERTORECEIVERINFO5():senderToReceiverInfo;
		senderToReceiverInfo = remittanceProcessRq.getSENDERTORECEIVERINFO().getSENDERTORECEIVERINFO6().length()>0?senderToReceiverInfo+"$"+remittanceProcessRq.getSENDERTORECEIVERINFO().getSENDERTORECEIVERINFO6():senderToReceiverInfo;

		swiftMT103.setSenderToReceiverInfo(senderToReceiverInfo);
		swiftMT103.setStp("N");
		swiftMT103.setTransactionTypeCode(msg.getDetails().getTransactionTypeCode());
		
		ubMT103.setDetails(swiftMT103);

		return ubMT103;
	}



	public Ub_MT202 getMT202FromRemittance(UB_SWT_RemittanceProcessRq remittanceProcessRq){

		IBOUB_INF_SWTMessageDetail msgDtl = getMessageDetail(remittanceProcessRq.getREMITTANCE_ID());
		Ub_MT202 msg = (Ub_MT202) BankFusionIOSupport.convertFromBytes(msgDtl.getF_MESSAGEOBJECT());

		Ub_MT202 ubMT202 = new Ub_MT202();
		MessageHeader messageHeader = new MessageHeader();
		SwiftMT202 swiftMT202 = new SwiftMT202();
		
		messageHeader.setMessageId1(msg.getHeader().getMessageId1());
		messageHeader.setMessageType(msg.getHeader().getMessageType());
		ubMT202.setHeader(messageHeader);
		
		swiftMT202.setSender(msg.getDetails().getSender());
		swiftMT202.setReceiver(msg.getDetails().getReceiver());
		swiftMT202.setAction(msg.getDetails().getAction());
		swiftMT202.setTransactionReferenceNumber(msg.getDetails().getTransactionReferenceNumber());
		swiftMT202.setRelatedReference(msg.getDetails().getRelatedReference());
		swiftMT202.setTdValueDate(convertDateFormat(remittanceProcessRq.getTRANSACTIONDETAISINFO().getDATEOFPROCESSING()));
		swiftMT202.setTdCurrencyCode(remittanceProcessRq.getTRANSACTIONDETAISINFO().getCURRENCY());
		swiftMT202.setTdAmount(String.valueOf(remittanceProcessRq.getDEBITORDTL().getDEBITAMOUNT()));
		String orderingInstitution = "";
		if(null!=remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTPARTYIDENTCODE() && remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTPARTYIDENTCODE().length()>0){
			orderingInstitution = "/"+remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTPARTYIDENTCODE();
		}
		else if(null!=remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONIDENTCODE() && remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONIDENTCODE().length()>0){
			orderingInstitution = orderingInstitution.length()>0?(orderingInstitution+"$"+remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTPARTYIDENTCODE()):remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTPARTYIDENTCODE();
		}
		if(!"A".equals(msg.getDetails().getOrderingInstitutionOption())){
			orderingInstitution = orderingInstitution+getDollarSeparatedText(remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONDTL1(), remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONDTL2(), remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONDTL3(), remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONDTL4());
		}
		
		swiftMT202.setOrderingInstitution(orderingInstitution);
		swiftMT202.setOrderingInstitutionOption(msg.getDetails().getOrderingInstitutionOption());

			String senderCorrespondent = "";
			String receiverCorrespondent = "";
			if(null!=msg.getDetails().getSendersCorrespondent()){
				if(null!=remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOPARTYIDENTIFIER() && remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOPARTYIDENTIFIER().length()>0){
					senderCorrespondent = "/"+remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOPARTYIDENTIFIER();

				}
				if(null!=remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOIDENTCODE() && remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOIDENTCODE().length()>0){
					senderCorrespondent = senderCorrespondent.length()>0?(senderCorrespondent+"$"+remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOIDENTCODE()):remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOIDENTCODE();
				}
				if(!"A".equals(msg.getDetails().getSendersCorrespondentOption())){
					senderCorrespondent = senderCorrespondent+getDollarSeparatedText(remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOTEXT1(), remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOTEXT2(), remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOTEXT3(), remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOTEXT4());
				}
				receiverCorrespondent = msg.getDetails().getReceiversCorrespondent();
			}
			else if(null!=msg.getDetails().getReceiversCorrespondent()){
				if(null!=remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOPARTYIDENTIFIER() && remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOPARTYIDENTIFIER().length()>0){
					receiverCorrespondent = "/"+remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOPARTYIDENTIFIER();
				}
				if(null!=remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOIDENTCODE() && remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOIDENTCODE().length()>0){
					receiverCorrespondent = receiverCorrespondent.length()>0?(senderCorrespondent+"$"+remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOIDENTCODE()):remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOIDENTCODE();
				}
				if(!"A".equals(msg.getDetails().getReceiversCorrespondentOption())){
					receiverCorrespondent = receiverCorrespondent+getDollarSeparatedText(remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOTEXT1(), remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOTEXT2(), remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOTEXT3(), remittanceProcessRq.getPAYTOPARTYDETAILS().getPAYTOTEXT4());
				}
				
			}
			swiftMT202.setSendersCorrespondent(senderCorrespondent);
			swiftMT202.setSendersCorrespondentOption(msg.getDetails().getSendersCorrespondentOption());
			swiftMT202.setReceiversCorrespondent(receiverCorrespondent);
			swiftMT202.setReceiversCorrespondentOption(msg.getDetails().getReceiversCorrespondentOption());

			String intermediary = "";
			String accountWithInstitution = "";
			if(msg.getDetails().getAccountWithInstitution()!=null){
				if(remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR() != null && remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR().length()>0){
					accountWithInstitution = "/"+remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR();
				}
				if(null!=remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE() && remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE().length()>0){
					accountWithInstitution = accountWithInstitution.length()>0?(accountWithInstitution+"$"+remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE()):remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE();
				}
				if(!"A".equals(msg.getDetails().getAccountWithInstitutionOption())){
					accountWithInstitution = accountWithInstitution+getDollarSeparatedText(remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT1(), remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT2(), remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT3(), remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT4());
				}
				intermediary = msg.getDetails().getIntermediary();
			}
			else{
				if(remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR() != null && remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR().length()>0){
					intermediary = "/"+remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR();
				}
				if(null!=remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE() && remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE().length()>0){
					intermediary = intermediary.length()>0?(intermediary+"$"+remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE()):remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE();
				}
				if(!"A".equals(msg.getDetails().getIntermediaryOption())){
					intermediary = intermediary+getDollarSeparatedText(remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT1(), remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT2(), remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT3(), remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT4());
				}
        }
        swiftMT202.setIntermediary(intermediary);
        swiftMT202.setIntermediaryOption(msg.getDetails().getIntermediaryOption());
        swiftMT202.setAccountWithInstitution(accountWithInstitution);
        swiftMT202.setAccountWithInstitutionOption(msg.getDetails().getAccountWithInstitutionOption());

        String beneficiary = "";
        if (null != remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARINSTYPARTYIDENTIFIER()
                && remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARINSTYPARTYIDENTIFIER().length() > 0) {
            beneficiary = "/" + remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARINSTYPARTYIDENTIFIER();
        }
        if (null != remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTIDENTCODE()
                && remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTIDENTCODE().length() > 0) {
            beneficiary = beneficiary.length() > 0
                    ? (beneficiary + "$" + remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTIDENTCODE())
                    : remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTIDENTCODE();
        }
 
        // beneficiary institution Text Fields
        String benInstTextFields = getDollarSeparatedText(remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTTEXT1(),
                remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTTEXT2(),
                remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTTEXT3(),
                remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTTEXT4());

        beneficiary = beneficiary + benInstTextFields;
        // account with option
        String beneficiaryInstOption = getOptionValue(benInstTextFields,
                remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENPSRTYIDENTCLRCODE(),
                remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARINSTYPARTYIDENTIFIER(),
                remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTIDENTCODE());
        
        swiftMT202.setBeneficiary(beneficiary);
        swiftMT202.setBeneficiaryOption(beneficiaryInstOption);
        swiftMT202.setCover(msg.getDetails().getCover());

        ubMT202.setDetails(swiftMT202);

        return ubMT202;
    }



	public Ub_MT205 getMT205FromRemittance(UB_SWT_RemittanceProcessRq remittanceProcessRq){

		IBOUB_INF_SWTMessageDetail msgDtl = getMessageDetail(remittanceProcessRq.getREMITTANCE_ID());
		Ub_MT205 msg = (Ub_MT205) BankFusionIOSupport.convertFromBytes(msgDtl.getF_MESSAGEOBJECT());

		Ub_MT205 ubMT205 = new Ub_MT205();
		MessageHeader messageHeader = new MessageHeader();
		SwiftMT205 swiftMT205 = new SwiftMT205();
		
		messageHeader.setMessageId1(msg.getHeader().getMessageId1());
		messageHeader.setMessageType(msg.getHeader().getMessageType());
		ubMT205.setHeader(messageHeader);
		
		swiftMT205.setSender(msg.getDetails().getSender());
		swiftMT205.setReceiver(msg.getDetails().getReceiver());
		swiftMT205.setAction(msg.getDetails().getAction());
		swiftMT205.setDisposalRef(msg.getDetails().getDisposalRef());
		swiftMT205.setTransactionReferenceNumber(msg.getDetails().getTransactionReferenceNumber());
		swiftMT205.setSendersCorrespondent(msg.getDetails().getSendersCorrespondent());
		swiftMT205.setSendersCorresOption(msg.getDetails().getSendersCorresOption());

		String intermediary = "";
		String accountWithInstitution = "";
		if(msg.getDetails().getAccountWithInstitution()!=null){
			if(remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR() != null && remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR().length()>0){
				accountWithInstitution = "/"+remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR();
			}
			if(null!=remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE() && remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE().length()>0){
				accountWithInstitution = accountWithInstitution.length()>0?(accountWithInstitution+"$"+remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE()):remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE();
			}
			if(!"A".equals(msg.getDetails().getAccountWithInstOption())){
				accountWithInstitution = accountWithInstitution+getDollarSeparatedText(remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT1(), remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT2(), remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT3(), remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT4());
			}
			
			intermediary = msg.getDetails().getIntermediary();
		}
		else{
			if(remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR() != null && remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR().length()>0){
				intermediary = "/"+remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR();
			}
			if(null!=remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE() && remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE().length()>0){
				intermediary = intermediary.length()>0?(intermediary+"$"+remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE()):remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE();
			}
			if(!"A".equals(msg.getDetails().getIntermediaryOption())){
				intermediary = intermediary+getDollarSeparatedText(remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT1(), remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT2(), remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT3(), remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT4());
			}
			
			//intermediary = msg.getDetails().getIntermediary();
		}
		swiftMT205.setIntermediary(intermediary);
		swiftMT205.setIntermediaryOption(msg.getDetails().getIntermediaryOption());
		swiftMT205.setAccountWithInstitution(accountWithInstitution);
		swiftMT205.setAccountWithInstOption(msg.getDetails().getAccountWithInstOption());
		swiftMT205.setSenderToReceiverInformation(msg.getDetails().getSenderToReceiverInformation());
		swiftMT205.setTdvalueDate(convertDateFormat(remittanceProcessRq.getTRANSACTIONDETAISINFO().getDATEOFPROCESSING()));
		swiftMT205.setTdcurrencyCode(remittanceProcessRq.getTRANSACTIONDETAISINFO().getCURRENCY());
		swiftMT205.setTdamount(String.valueOf(remittanceProcessRq.getDEBITORDTL().getDEBITAMOUNT()));
		swiftMT205.setRelatedReference(msg.getDetails().getRelatedReference());

        String orderingInstitution = "";
        if (null != remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTPARTYIDENTCODE()
                && remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTPARTYIDENTCODE().length() > 0) {
            orderingInstitution = "/" + remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTPARTYIDENTCODE();
        }
        if (null != remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONIDENTCODE()
                && remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONIDENTCODE().length() > 0) {
            orderingInstitution = orderingInstitution.length() > 0
                    ? (orderingInstitution + "$"
                            + remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONIDENTCODE())
                    : remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONIDENTCODE();
        }

        String orderingInstitutionTextFields = getDollarSeparatedText(
                remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONDTL1(),
                remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONDTL2(),
                remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONDTL3(),
                remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONDTL4());
        orderingInstitution = orderingInstitution + orderingInstitutionTextFields;

        // account with option
        String orderingInstitutionOption = getOptionValue(orderingInstitutionTextFields,
                remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTPRTYIDNTCLRCODE(),
                remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTPARTYIDENTCODE(),
                remittanceProcessRq.getORDERINGINSTITUTIONDTL().getORDERINGINSTITUTIONIDENTCODE());

        swiftMT205.setOrderingInstitute(orderingInstitution);
        swiftMT205.setOrderingInstitutionOption(orderingInstitutionOption);

        String beneficiaryInstitute = "";
        if (remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARINSTYPARTYIDENTIFIER() != null
                && remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARINSTYPARTYIDENTIFIER().length() > 0) {
            beneficiaryInstitute = "/" + remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARINSTYPARTYIDENTIFIER();
        }

        if (null != remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTIDENTCODE()
                && remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTIDENTCODE().length() > 0) {
            beneficiaryInstitute = beneficiaryInstitute.length() > 0
                    ? (beneficiaryInstitute + "$" + remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTIDENTCODE())
                    : remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTIDENTCODE();
        }

        // beneficiary institution Text Fields
        String benInstTextFields = getDollarSeparatedText(remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTTEXT1(),
                remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTTEXT2(),
                remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTTEXT3(),
                remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTTEXT4());

        beneficiaryInstitute = beneficiaryInstitute + benInstTextFields;
        // account with option
        String beneficiaryInstOption = getOptionValue(benInstTextFields,
                remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENPSRTYIDENTCLRCODE(),
                remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARINSTYPARTYIDENTIFIER(),
                remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTIDENTCODE());

        swiftMT205.setBeneficiaryInstitute(beneficiaryInstitute);
        swiftMT205.setBeneficiaryInstOption(beneficiaryInstOption);
        swiftMT205.setCover(msg.getDetails().getCover());
        ubMT205.setDetails(swiftMT205);

		return ubMT205;
	}


	public Ub_MT200 getMT200FromRemittance(UB_SWT_RemittanceProcessRq remittanceProcessRq){

		IBOUB_INF_SWTMessageDetail msgDtl = getMessageDetail(remittanceProcessRq.getREMITTANCE_ID());
		Ub_MT200 msg = (Ub_MT200) BankFusionIOSupport.convertFromBytes(msgDtl.getF_MESSAGEOBJECT());

		Ub_MT200 ubMT200 = new Ub_MT200();
		
		MessageHeader messageHeader = new MessageHeader();
		SwiftMT200 swiftMT200 = new SwiftMT200();
		
		messageHeader.setMessageId1(msg.getHeader().getMessageId1());
		messageHeader.setMessageType(msg.getHeader().getMessageType());
		ubMT200.setHeader(messageHeader);

		swiftMT200.setTransactionReferenceNumber(msg.getDetails().getTransactionReferenceNumber());
		String intermediary = "";
		if(remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR() != null && remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR().length()>0){
			intermediary = "/"+remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR();
		}
		if(null!=remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE() && remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE().length()>0){
			intermediary = intermediary.length()>0?(intermediary+"$"+remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE()):remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE();
		}
		
        String intermediaryTextFields = getDollarSeparatedText(remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT1(),
                remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT2(),
                remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT3(),
                remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYTEXT4());

        intermediary = intermediary + intermediaryTextFields;

        String intermediaryOption = getOptionValue(intermediaryTextFields,
                remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTCLRCODE(),
                remittanceProcessRq.getINTERMEDIARYDETAILS().getINTMDPRTYIDNTIFR(),
                remittanceProcessRq.getINTERMEDIARYDETAILS().getINTERMEDIARYIDENTCODE());
    
		swiftMT200.setIntermediary(intermediary);
		swiftMT200.setIntermediaryOption(intermediaryOption);
		swiftMT200.setSendersCorrespondent(msg.getDetails().getSendersCorrespondent());
		swiftMT200.setSendersCorresOption(msg.getDetails().getSendersCorresOption());
		swiftMT200.setSenderToReceiverInformation(msg.getDetails().getSenderToReceiverInformation());
		swiftMT200.setTdvalueDate(convertDateFormat(remittanceProcessRq.getTRANSACTIONDETAISINFO().getDATEOFPROCESSING()));
		swiftMT200.setTdcurrencyCode(remittanceProcessRq.getTRANSACTIONDETAISINFO().getCURRENCY());
		swiftMT200.setTdamount(String.valueOf(remittanceProcessRq.getCREDITORDTL().getCREDITAMOUNT()));
		
        String accountWithInstitution = "";
        if (remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARINSTYPARTYIDENTIFIER() != null
                && remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARINSTYPARTYIDENTIFIER().length() > 0) {
            accountWithInstitution = "/" + remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARINSTYPARTYIDENTIFIER();
        }
        if (null != remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTIDENTCODE()
                && remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTIDENTCODE().length() > 0) {
            accountWithInstitution = accountWithInstitution.length() > 0
                    ? (accountWithInstitution + "$" + remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTIDENTCODE())
                    : remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTIDENTCODE();
        }
        //beneficiary institution Text Fields 
        String benInstTextFields = getDollarSeparatedText(remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTTEXT1(),
                remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTTEXT2(),
                remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTTEXT3(),
                remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTTEXT4());
        
        //account with option
        String accountWithInstOption = getOptionValue(benInstTextFields,
                remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENPSRTYIDENTCLRCODE(),
                remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARINSTYPARTYIDENTIFIER(),
                remittanceProcessRq.getBENEFICIARYINSTDETIALS().getBENEFICIARYINSTIDENTCODE());
        
        accountWithInstitution = accountWithInstitution + benInstTextFields;
        swiftMT200.setAccountWithInstitution(accountWithInstitution);
        swiftMT200.setAccountWithInstOption(accountWithInstOption);

        swiftMT200.setSender(msg.getDetails().getSender());
        swiftMT200.setReceiver(msg.getDetails().getReceiver());
        swiftMT200.setAction(msg.getDetails().getAction());

        ubMT200.setDetails(swiftMT200);

		return ubMT200;
	}



	private String convertDateFormat(Date date){
		String stringDate = null;
	    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(PaymentSwiftConstants.DEFAULT_DATE_FORMAT);
		stringDate = simpleDateFormat.format(date);
		return stringDate;
	}
	
	
    @SuppressWarnings("FBPE")
	public boolean isBICExistInSwtCutDtls(String BIC){
		if ((BIC.length()) == 8) {
			BIC = BIC + "XXX";
		}
		ArrayList params = new ArrayList();
		Iterator customerList = null;
		IBOSwtCustomerDetail swtCustomerDetail = null;
		params.add(BIC);
		customerList = factory.findByQuery(IBOSwtCustomerDetail.BONAME, SWTCUSTOMERWHERECLAUSE, params, 1);
		if(customerList.hasNext())	
			return true;
		else
			return false;
	}
	
	public String getDollarSeparatedText(String text1, String text2, String text3, String text4){
		String dollarSeparatedString = "";
		dollarSeparatedString = text1.length()>0?"$"+text1:"";
		dollarSeparatedString = text2.length()>0?dollarSeparatedString+"$"+text2:dollarSeparatedString;
		dollarSeparatedString = text3.length()>0?dollarSeparatedString+"$"+text3:dollarSeparatedString;
		dollarSeparatedString = text4.length()>0?dollarSeparatedString+"$"+text4:dollarSeparatedString;
		
		return dollarSeparatedString;
	}
		

    /**
     * Method Description:Get the Option Value
     * @param nameAndAddr
     * @param partyIdentifierClearingCode
     * @param value
     * @param identifierCode
     * @return
     */
    private String getOptionValue(String nameAndAddr, String partyIdentifierClearingCode, String value, String identifierCode) {
        String option = getOptionA(identifierCode);
        
        if (option.isEmpty()) {
            option = getOptionC(partyIdentifierClearingCode, value);
        }
        
        if (option.isEmpty()) {
            option = getOptionD(nameAndAddr, partyIdentifierClearingCode, value);
        }

        return option;
    }
	
	
    /**
     * Method Description:Option Value A
     * @param identifierCode
     * @return
     */
    private String getOptionA(String identifierCode) {
        return StringUtils.isNotBlank(identifierCode) ? "A" : StringUtils.EMPTY;
    }
    
    /**
     * Method Description:Option Value C
     * @param partyIdentifier
     * @param value
     * @return
     */
    private String getOptionC(String partyIdentifier, String value) {
        return (StringUtils.isNotBlank(partyIdentifier) && StringUtils.isNotBlank(value))? "C" : StringUtils.EMPTY;
    }

    /**
     * Method Description:Option Value D
     * @param nameAndAddr
     * @param partyIdentifier
     * @param value
     * @return
     */
    private String getOptionD(String nameAndAddr, String partyIdentifier, String value) {
        String option = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(nameAndAddr)) {
            option = "D";
            if (StringUtils.isNotBlank(partyIdentifier) && StringUtils.isNotBlank(value)) {
                option = "D";
            }
        }
        return option;
    }

    
}
