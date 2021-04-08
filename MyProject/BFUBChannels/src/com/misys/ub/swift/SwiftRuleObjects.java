package com.misys.ub.swift;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.FinderMethods;
import com.trapedza.bankfusion.fatoms.UB_SWT_ReadMsgDetailsFromBLOB;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

import bf.com.misys.ub.types.interfaces.SWIFT_MT103_Rule;
import bf.com.misys.ub.types.interfaces.SWIFT_MT200_Rule;
import bf.com.misys.ub.types.interfaces.SWIFT_MT202Cov_Rule;
import bf.com.misys.ub.types.interfaces.SWIFT_MT202_Rule;
import bf.com.misys.ub.types.interfaces.SWIFT_MT205Cov_Rule;
import bf.com.misys.ub.types.interfaces.SWIFT_MT205_Rule;
import bf.com.misys.ub.types.interfaces.SwiftMT103;
import bf.com.misys.ub.types.interfaces.SwiftMT200;
import bf.com.misys.ub.types.interfaces.SwiftMT202;
import bf.com.misys.ub.types.interfaces.SwiftMT205;

public class SwiftRuleObjects {

	private transient final static Log logger = LogFactory.getLog(SwiftRuleObjects.class.getName());

	private SWIFT_MT103_Rule swiftMT103Rule = null;
	private SWIFT_MT200_Rule swiftMT200Rule = null;
	private SWIFT_MT202_Rule swiftMT202Rule = null;
	private SWIFT_MT205_Rule swiftMT205Rule = null;
	private SWIFT_MT202Cov_Rule swiftMT202CovRule = null;
	private SWIFT_MT205Cov_Rule swiftMT205CovRule = null;
	
	IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

	private String creditAccountCurr;
	private String creditAccount;
	private Boolean isAccountPresent;

	
	public SWIFT_MT103_Rule generateMT103RuleObject(SwiftMT103 swiftMT103){
		
		swiftMT103Rule = new SWIFT_MT103_Rule();
		swiftMT103Rule.setSendersReference(swiftMT103.getSendersReference());
		swiftMT103Rule.setTimeIndicationCode((swiftMT103.getTimeIndicationCount()>0 && null!=swiftMT103.getTimeIndication(0) && swiftMT103.getTimeIndication(0).length()>=8)?swiftMT103.getTimeIndication(0).substring(0, 8):"");
		swiftMT103Rule.setBankOperationCode(swiftMT103.getBankOperationCode());
		if(null!=swiftMT103.getInstruction() && swiftMT103.getInstruction().getInstructionCode().length>=1)
			swiftMT103Rule.setInstructionCode(swiftMT103.getInstruction().getInstructionCode(0).substring(0, 4));

		swiftMT103Rule.setBusinessDate(getFormattedDate(SystemInformationManager.getInstance().getBFBusinessDateAsString()));
		swiftMT103Rule.setCurrency(swiftMT103.getTdCurrencyCode());
		//added-FBPY-5046
		creditAccount = getAccountNumberFromText(swiftMT103.getBeneficiaryCustomer());
		isAccountPresent = isAccountExist(creditAccount);
		if(isAccountPresent == true) {
			if (null != creditAccount && !creditAccount.equals(CommonConstants.EMPTY_STRING)) {
				creditAccountCurr = getAccountCurrency(creditAccount);
				}
			}
		swiftMT103Rule.setCreditAccountCurrency(creditAccountCurr); //creditor-currency
		swiftMT103Rule.setValueDate(getFormattedDate(swiftMT103.getTdValueDate()));  //YYYY-MM-DD
		swiftMT103Rule.setAmount(new BigDecimal(swiftMT103.getTdAmount()));
		swiftMT103Rule.setInstructedCurrency(swiftMT103.getInstructedCurrency());
		String instructedAmount = (null!=swiftMT103.getInstructedAmount() && !swiftMT103.getInstructedAmount().equals(""))?swiftMT103.getInstructedAmount():"0";
		swiftMT103Rule.setInstructedAmount(new BigDecimal(instructedAmount));
		String exchangeRate = (null!=swiftMT103.getExchangeRate() && !swiftMT103.getExchangeRate().equals(""))?swiftMT103.getExchangeRate():"0";
		swiftMT103Rule.setExchangeRate(new BigDecimal(exchangeRate));
		populateOrderingCustomer(swiftMT103Rule, getLineValues(swiftMT103.getOrderingCustomer()));
		swiftMT103Rule.setOrderingCustomerOption(swiftMT103.getOrderingCustomerOption());
		swiftMT103Rule.setSendingInstitution(swiftMT103.getSendingInstitution());
		populateOrderingInst(swiftMT103Rule, getLineValues(swiftMT103.getOrderingInstitution()));
		swiftMT103Rule.setOrderInstitutionOption(swiftMT103.getOrderInstitutionOption());
		populateSenderCorrespondent(swiftMT103Rule, getLineValues(swiftMT103.getSendersCorrespondent()));
		swiftMT103Rule.setSendersCorrespOption(swiftMT103.getSendersCorrespOption());
		populateReceiverCorrespondent(swiftMT103Rule, getLineValues(swiftMT103.getReceiversCorrespondent()));
		swiftMT103Rule.setReceiversCorrespOption(swiftMT103.getReceiversCorrespOption());
		populateThirdReimbInst(swiftMT103Rule, getLineValues(swiftMT103.getThirdReimbursementInstitution()));
		swiftMT103Rule.setThirdReimbursementInstOption(swiftMT103.getThirdReimbursementInstOption());
		populateIntermediaryInstitution(swiftMT103Rule, getLineValues(swiftMT103.getIntermediaryInstitution()));
		swiftMT103Rule.setIntermediaryInstOption(swiftMT103.getIntermediaryInstOption());
		populateAccountWithInstitution(swiftMT103Rule, getLineValues(swiftMT103.getAccountWithInstitution()));
		swiftMT103Rule.setAccountWithInstOption(swiftMT103.getAccountWithInstOption());
		populateBeneficiaryCustomer(swiftMT103Rule, getLineValues(swiftMT103.getBeneficiaryCustomer()));
		swiftMT103Rule.setBeneficiaryCustOption(swiftMT103.getBeneficiaryCustOption());
		if(null!=swiftMT103.getRemittanceInfo()){
			String remitInfo [] = swiftMT103.getRemittanceInfo().split("\\$");
			swiftMT103Rule.setRemittanceInfoLine1(remitInfo.length>=1?remitInfo[0]:"");
			swiftMT103Rule.setRemittanceInfoLine2(remitInfo.length>=2?remitInfo[1]:"");
			swiftMT103Rule.setRemittanceInfoLine3(remitInfo.length>=3?remitInfo[2]:"");
			swiftMT103Rule.setRemittanceInfoLine4(remitInfo.length>=4?remitInfo[3]:"");
		}
		
		swiftMT103Rule.setDetailsOfCharges(swiftMT103.getDetailsOfCharges());
		if(null!=swiftMT103.getCharges() && swiftMT103.getCharges().getSenderChargeCount()>=1){
			swiftMT103Rule.setSendersChargeCurrency(swiftMT103.getCharges().getSenderCharge(0).substring(0, 3));
			swiftMT103Rule.setSendersChargeAmount(new BigDecimal(swiftMT103.getCharges().getSenderCharge(0).substring(3)));
		}
		if(null!=swiftMT103.getReceiversCharges() && swiftMT103.getReceiversCharges().length()>=1){
			swiftMT103Rule.setReceiversChargeCurrency(swiftMT103.getReceiversCharges().substring(0, 3));
			swiftMT103Rule.setReceiversChargeAmount(new BigDecimal(swiftMT103.getReceiversCharges().substring(3)));
		}
		swiftMT103Rule.setSenderToReceiverInfoCode(getSenderToReceiverInfo(swiftMT103.getSenderToReceiverInfo()));//Only line 1 to be populated
		if(null!=swiftMT103.getRegulatoryReporting() && swiftMT103.getRegulatoryReporting().length()>14){
			swiftMT103Rule.setRegulatoryReportingCode(swiftMT103.getRegulatoryReporting().replaceAll("\\$", "").substring(1, 9));
			swiftMT103Rule.setRegulatoryReportingCountry(swiftMT103.getRegulatoryReporting().replaceAll("\\$", "").substring(10, 12));
			swiftMT103Rule.setRegulatoryReportingNarrative(swiftMT103.getRegulatoryReporting().replaceAll("\\$", "").substring(14));
		}
		swiftMT103Rule.setEnvelopeContents(swiftMT103.getEnvelopeContents());
		swiftMT103Rule.setSenderCountry(swiftMT103.getSender().substring(4, 6));
		swiftMT103Rule.setSenderBIC(swiftMT103.getSender());
		swiftMT103Rule.setReceiverBIC(swiftMT103.getReceiver());

		return swiftMT103Rule;
		
	}


	public SWIFT_MT200_Rule generateMT200RuleObject(SwiftMT200 swiftMT200){
		
		swiftMT200Rule = new SWIFT_MT200_Rule();
		swiftMT200Rule.setTransactionReferenceNumber(swiftMT200.getTransactionReferenceNumber());
		swiftMT200Rule.setCurrency(swiftMT200.getTdcurrencyCode());
		swiftMT200Rule.setValueDate(getFormattedDate(swiftMT200.getTdvalueDate()));  //YYYY-MM-DD
		swiftMT200Rule.setBusinessDate(getFormattedDate(SystemInformationManager.getInstance().getBFBusinessDateAsString()));
		swiftMT200Rule.setAmount(new BigDecimal(swiftMT200.getTdamount()));
		populateSenderCorrespondent(swiftMT200Rule, getLineValues(swiftMT200.getSendersCorrespondent()));
		swiftMT200Rule.setSendersCorrespOption(swiftMT200.getSendersCorresOption());
		populateIntermediary(swiftMT200Rule, getLineValues(swiftMT200.getIntermediary()));
		swiftMT200Rule.setIntermediaryOption(swiftMT200.getIntermediaryOption());
		populateAccountWithInstitution(swiftMT200Rule, getLineValues(swiftMT200.getAccountWithInstitution()));
		swiftMT200Rule.setAccountWithInstOption(swiftMT200.getAccountWithInstOption());
		swiftMT200Rule.setSenderToReceiverInfoCode(getSenderToReceiverInfo(swiftMT200.getSenderToReceiverInformation()));//Only line 1 to be populated
		swiftMT200Rule.setSenderCountry(swiftMT200.getSender().substring(4, 6));
		swiftMT200Rule.setSenderBIC(swiftMT200.getSender());
		swiftMT200Rule.setReceiverBIC(swiftMT200.getReceiver());

		return swiftMT200Rule;
	}

	public SWIFT_MT202_Rule generateMT202RuleObject(SwiftMT202 swiftMT202){
		
		swiftMT202Rule = new SWIFT_MT202_Rule();
		swiftMT202Rule.setTransactionReferenceNumber(swiftMT202.getTransactionReferenceNumber());
		swiftMT202Rule.setRelatedReference(swiftMT202.getRelatedReference());
		swiftMT202Rule.setTimeIndicationCode((swiftMT202.getTimeIndicationCount()>0 &&  null!=swiftMT202.getTimeIndication(0) && swiftMT202.getTimeIndication(0).length()>=8)?swiftMT202.getTimeIndication(0).substring(0, 8):"");
		swiftMT202Rule.setCurrency(swiftMT202.getTdCurrencyCode());
		swiftMT202Rule.setValueDate(getFormattedDate(swiftMT202.getTdValueDate()));  //YYYY-MM-DD
		swiftMT202Rule.setBusinessDate(getFormattedDate(SystemInformationManager.getInstance().getBFBusinessDateAsString()));
		swiftMT202Rule.setAmount(new BigDecimal(swiftMT202.getTdAmount()));
		populateOrderingInst(swiftMT202Rule, getLineValues(swiftMT202.getOrderingInstitution()));
		swiftMT202Rule.setOrderInstitutionOption(swiftMT202.getOrderingInstitutionOption());
		populateSenderCorrespondent(swiftMT202Rule, getLineValues(swiftMT202.getSendersCorrespondent()));
		swiftMT202Rule.setSendersCorrespOption(swiftMT202.getSendersCorrespondentOption());
		populateReceiverCorrespondent(swiftMT202Rule, getLineValues(swiftMT202.getReceiversCorrespondent()));
		swiftMT202Rule.setReceiversCorrespOption(swiftMT202.getReceiversCorrespondentOption());
		populateIntermediary(swiftMT202Rule, getLineValues(swiftMT202.getIntermediary()));
		swiftMT202Rule.setIntermediaryOption(swiftMT202.getIntermediaryOption());
		populateAccountWithInstitution(swiftMT202Rule, getLineValues(swiftMT202.getAccountWithInstitution()));
		swiftMT202Rule.setAccountWithInstOption(swiftMT202.getAccountWithInstitutionOption());
		populateBeneficiaryInst(swiftMT202Rule, getLineValues(swiftMT202.getBeneficiary()));
		swiftMT202Rule.setBeneficiaryInstOption(swiftMT202.getBeneficiaryOption());
		swiftMT202Rule.setSenderToReceiverInfoCode(getSenderToReceiverInfo(swiftMT202.getSendertoReceiverInformation()));//Only line 1 to be populated
		swiftMT202Rule.setSenderCountry(swiftMT202.getSender().substring(4, 6));
		swiftMT202Rule.setSenderBIC(swiftMT202.getSender());
		swiftMT202Rule.setReceiverBIC(swiftMT202.getReceiver());

		return swiftMT202Rule;
	}

	public SWIFT_MT202Cov_Rule generateMT202CovRuleObject(SwiftMT202 swiftMT202){
		
		swiftMT202CovRule = new SWIFT_MT202Cov_Rule();
		swiftMT202CovRule.setTransactionReferenceNumber(swiftMT202.getTransactionReferenceNumber());
		swiftMT202CovRule.setRelatedReference(swiftMT202.getRelatedReference());
		swiftMT202CovRule.setTimeIndicationCode((swiftMT202.getTimeIndicationCount()>0 &&  null!=swiftMT202.getTimeIndication(0) && swiftMT202.getTimeIndication(0).length()>=8)?swiftMT202.getTimeIndication(0).substring(0, 8):"");
		swiftMT202CovRule.setCurrency(swiftMT202.getTdCurrencyCode());
		swiftMT202CovRule.setValueDate(getFormattedDate(swiftMT202.getTdValueDate()));  //YYYY-MM-DD
		swiftMT202CovRule.setBusinessDate(getFormattedDate(SystemInformationManager.getInstance().getBFBusinessDateAsString()));
		swiftMT202CovRule.setAmount(new BigDecimal(swiftMT202.getTdAmount()));
		populateOrderingInst(swiftMT202CovRule, getLineValues(swiftMT202.getOrderingInstitution()));
		swiftMT202CovRule.setOrderInstitutionOption(swiftMT202.getOrderingInstitutionOption());
		populateSenderCorrespondent(swiftMT202CovRule, getLineValues(swiftMT202.getSendersCorrespondent()));
		swiftMT202CovRule.setSendersCorrespOption(swiftMT202.getSendersCorrespondentOption());
		populateReceiverCorrespondent(swiftMT202CovRule, getLineValues(swiftMT202.getReceiversCorrespondent()));
		swiftMT202CovRule.setReceiversCorrespOption(swiftMT202.getReceiversCorrespondentOption());
		populateIntermediary(swiftMT202CovRule, getLineValues(swiftMT202.getIntermediary()));
		swiftMT202CovRule.setIntermediaryOption(swiftMT202.getIntermediaryOption());
		populateAccountWithInstitution(swiftMT202CovRule, getLineValues(swiftMT202.getAccountWithInstitution()));
		swiftMT202CovRule.setAccountWithInstOption(swiftMT202.getAccountWithInstitutionOption());
		populateBeneficiaryInst(swiftMT202CovRule, getLineValues(swiftMT202.getBeneficiary()));
		swiftMT202CovRule.setBeneficiaryInstOption(swiftMT202.getBeneficiaryOption());
		swiftMT202CovRule.setSenderToReceiverInfoCode(getSenderToReceiverInfo(swiftMT202.getSendertoReceiverInformation()));//Only line 1 to be populated
		swiftMT202CovRule.setSenderCountry(swiftMT202.getSender().substring(4, 6));
		swiftMT202CovRule.setSenderBIC(swiftMT202.getSender());
		swiftMT202CovRule.setReceiverBIC(swiftMT202.getReceiver());
		
		return swiftMT202CovRule;
	}

	public SWIFT_MT205_Rule generateMT205RuleObject(SwiftMT205 swiftMT205){
		
		swiftMT205Rule = new SWIFT_MT205_Rule();
		swiftMT205Rule.setTransactionReferenceNumber(swiftMT205.getTransactionReferenceNumber());
		swiftMT205Rule.setRelatedReference(swiftMT205.getRelatedReference());
		swiftMT205Rule.setTimeIndicationCode((swiftMT205.getTimeIndicationCount()>0 &&  null!=swiftMT205.getTimeIndication(0) && swiftMT205.getTimeIndication(0).length()>=8)?swiftMT205.getTimeIndication(0).substring(0, 8):"");
		swiftMT205Rule.setValueDate(getFormattedDate(swiftMT205.getTdvalueDate()));  //YYYY-MM-DD
		swiftMT205Rule.setBusinessDate(getFormattedDate(SystemInformationManager.getInstance().getBFBusinessDateAsString()));
		swiftMT205Rule.setAmount(new BigDecimal(swiftMT205.getTdamount()));
		swiftMT205Rule.setCurrency(swiftMT205.getTdcurrencyCode());
		populateOrderingInst(swiftMT205Rule, getLineValues(swiftMT205.getOrderingInstitute()));
		swiftMT205Rule.setOrderInstitutionOption(swiftMT205.getOrderingInstitutionOption());
		populateSenderCorrespondent(swiftMT205Rule, getLineValues(swiftMT205.getSendersCorrespondent()));
		swiftMT205Rule.setSendersCorrespOption(swiftMT205.getSendersCorresOption());
		populateIntermediary(swiftMT205Rule, getLineValues(swiftMT205.getIntermediary()));
		swiftMT205Rule.setIntermediaryOption(swiftMT205.getIntermediaryOption());
		populateAccountWithInstitution(swiftMT205Rule, getLineValues(swiftMT205.getAccountWithInstitution()));
		swiftMT205Rule.setAccountWithInstOption(swiftMT205.getAccountWithInstOption());
		populateBeneficiaryInst(swiftMT205Rule, getLineValues(swiftMT205.getBeneficiaryInstitute()));
		swiftMT205Rule.setBeneficiaryInstOption(swiftMT205.getBeneficiaryInstOption());
		swiftMT205Rule.setSenderToReceiverInfoCode(getSenderToReceiverInfo(swiftMT205.getSenderToReceiverInformation()));//Only line 1 to be populated
		swiftMT205Rule.setSenderCountry(swiftMT205.getSender().substring(4, 6));
		swiftMT205Rule.setSenderBIC(swiftMT205.getSender());
		swiftMT205Rule.setReceiverBIC(swiftMT205.getReceiver());
		
		return swiftMT205Rule;
	}

	public SWIFT_MT205Cov_Rule generateMT205CovRuleObject(SwiftMT205 swiftMT205){
		
		swiftMT205CovRule = new SWIFT_MT205Cov_Rule();
		swiftMT205CovRule.setTransactionReferenceNumber(swiftMT205.getTransactionReferenceNumber());
		swiftMT205CovRule.setRelatedReference(swiftMT205.getRelatedReference());
		swiftMT205CovRule.setTimeIndicationCode((swiftMT205.getTimeIndicationCount()>0 &&  null!=swiftMT205.getTimeIndication(0) && swiftMT205.getTimeIndication(0).length()>=8)?swiftMT205.getTimeIndication(0).substring(0, 8):"");
		swiftMT205CovRule.setValueDate(getFormattedDate(swiftMT205.getTdvalueDate()));  //YYYY-MM-DD
		swiftMT205CovRule.setBusinessDate(getFormattedDate(SystemInformationManager.getInstance().getBFBusinessDateAsString()));
		swiftMT205CovRule.setAmount(new BigDecimal(swiftMT205.getTdamount()));
		swiftMT205CovRule.setCurrency(swiftMT205.getTdcurrencyCode());
		populateOrderingInst(swiftMT205CovRule, getLineValues(swiftMT205.getOrderingInstitute()));
		swiftMT205CovRule.setOrderInstitutionOption(swiftMT205.getOrderingInstitutionOption());
		populateSenderCorrespondent(swiftMT205CovRule, getLineValues(swiftMT205.getSendersCorrespondent()));
		swiftMT205CovRule.setSendersCorrespOption(swiftMT205.getSendersCorresOption());
		populateIntermediary(swiftMT205CovRule, getLineValues(swiftMT205.getIntermediary()));
		swiftMT205CovRule.setIntermediaryOption(swiftMT205.getIntermediaryOption());
		populateAccountWithInstitution(swiftMT205CovRule, getLineValues(swiftMT205.getAccountWithInstitution()));
		swiftMT205CovRule.setAccountWithInstOption(swiftMT205.getAccountWithInstOption());
		populateBeneficiaryInst(swiftMT205CovRule, getLineValues(swiftMT205.getBeneficiaryInstitute()));
		swiftMT205CovRule.setBeneficiaryInstOption(swiftMT205.getBeneficiaryInstOption());
		swiftMT205CovRule.setSenderToReceiverInfoCode(getSenderToReceiverInfo(swiftMT205.getSenderToReceiverInformation()));//Only line 1 to be populated
		swiftMT205CovRule.setSenderCountry(swiftMT205.getSender().substring(4, 6));
		swiftMT205CovRule.setSenderBIC(swiftMT205.getSender());
		swiftMT205CovRule.setReceiverBIC(swiftMT205.getReceiver());
		
		return swiftMT205CovRule;
	}

	private String[] getLineValues(String dollarSeparatedText){
		//String sampleStringText = dollarSeparatedText;
		//String sampleStringOption = option;
		String lines[] = new String[5];
		String stringSplit[] = null;
		if(null!=dollarSeparatedText && !dollarSeparatedText.equals("")){

			stringSplit = dollarSeparatedText.split("\\$");
			int lengthString = stringSplit.length;

			if(/*null!=sampleStringOption*/ /*&& "A".equalsIgnoreCase(sampleStringOption)*/ /*&&*/ lengthString>=1){
				
				if(stringSplit[0].contains("/")){
					lines[1] = lengthString>=1?stringSplit[0].replaceAll("/", ""):"";
					lines[0] = lengthString>=2?stringSplit[1]:"";
					lines[2] = lengthString>=2?stringSplit[1]:"";
					lines[3] = lengthString>=3?stringSplit[2]:"";
					lines[4] = lengthString>=4?stringSplit[3]:"";
				}
				else{
					lines[1] = "";
					lines[0] = lengthString>=1?stringSplit[0]:"";
					lines[2] = lengthString>=1?stringSplit[0]:"";
					lines[3] = lengthString>=2?stringSplit[1]:"";
					lines[4] = lengthString>=3?stringSplit[2]:"";
				}
				//lines[3] = lengthString>=3?stringSplit[2]:"";
				//lines[4] = lengthString>=4?stringSplit[3]:"";
			}
			/*else{
				lines[0] = "";
				lines[1] = lengthString>=1?(stringSplit[0].contains("/")?stringSplit[0].replace("/", ""):stringSplit[0]):"";
				lines[2] = lengthString>=2?stringSplit[1]:"";
				lines[3] = lengthString>=3?stringSplit[2]:"";
				lines[4] = lengthString>=4?stringSplit[3]:"";
			}*/
		}
		return lines;
	}
	
	private String getSenderToReceiverInfo(String senderToReceiverInfo){
		if(null!=senderToReceiverInfo){
			String splitString[] = senderToReceiverInfo.split("\\$");
			if(splitString.length>=1){
				int indexEnd = splitString[0].indexOf("/", splitString[0].indexOf("/")+1);
				if(indexEnd!=-1){
					return splitString[0].substring(1, indexEnd);
				}
			}
		}
		return "";
	}
	
	private Date getFormattedDate(String dateString){
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
		java.util.Date date = null;
		java.sql.Date sqlDate = null;
		try {
			date = dateFormatter.parse(dateString);
		} catch (ParseException e) {
			logger.error(ExceptionUtil.getExceptionAsString(e));
		}
		if(date != null)
		sqlDate = new java.sql.Date(date.getTime()); 
		
		return sqlDate;
	}


	private void populateAccountWithInstitution(Object obj, String[] accountWithInst){
		try {
			obj.getClass().getDeclaredMethod("setAccountWithInstitutionCode", String.class).invoke(obj, accountWithInst[0]);
			obj.getClass().getDeclaredMethod("setAccountWithInstitutionLine1", String.class).invoke(obj, accountWithInst[1]);
			obj.getClass().getDeclaredMethod("setAccountWithInstitutionLine2", String.class).invoke(obj, accountWithInst[2]);
			obj.getClass().getDeclaredMethod("setAccountWithInstitutionLine3", String.class).invoke(obj, accountWithInst[3]);
			obj.getClass().getDeclaredMethod("setAccountWithInstitutionLine4", String.class).invoke(obj, accountWithInst[4]);
		} catch(Exception e){
			logger.error(ExceptionUtil.getExceptionAsString(e));
		}
	}

	private void populateSenderCorrespondent(Object obj, String[] senderCorres){
		try {
			obj.getClass().getDeclaredMethod("setSendersCorrespondentCode", String.class).invoke(obj, senderCorres[0]);
			obj.getClass().getDeclaredMethod("setSendersCorrespondentLine1", String.class).invoke(obj, senderCorres[1]);
			obj.getClass().getDeclaredMethod("setSendersCorrespondentLine2", String.class).invoke(obj, senderCorres[2]);
			obj.getClass().getDeclaredMethod("setSendersCorrespondentLine3", String.class).invoke(obj, senderCorres[3]);
			obj.getClass().getDeclaredMethod("setSendersCorrespondentLine4", String.class).invoke(obj, senderCorres[4]);
		} catch(Exception e){
			logger.error(ExceptionUtil.getExceptionAsString(e));
		}
	}
	
	private void populateReceiverCorrespondent(Object obj, String[] receiverCorres){
		try {
			obj.getClass().getDeclaredMethod("setReceiversCorrespondentCode", String.class).invoke(obj, receiverCorres[0]);
			obj.getClass().getDeclaredMethod("setReceiversCorrespondentLine1", String.class).invoke(obj, receiverCorres[1]);
			obj.getClass().getDeclaredMethod("setReceiversCorrespondentLine2", String.class).invoke(obj, receiverCorres[2]);
			obj.getClass().getDeclaredMethod("setReceiversCorrespondentLine3", String.class).invoke(obj, receiverCorres[3]);
			obj.getClass().getDeclaredMethod("setReceiversCorrespondentLine4", String.class).invoke(obj, receiverCorres[4]);
		} catch(Exception e){
			logger.error(ExceptionUtil.getExceptionAsString(e));
		}
	}
	
	private void populateOrderingInst(Object obj, String[] orderingInst){
		try {
			obj.getClass().getDeclaredMethod("setOrderingInstitutionCode", String.class).invoke(obj, orderingInst[0]);
			obj.getClass().getDeclaredMethod("setOrderingInstitutionLine1", String.class).invoke(obj, orderingInst[1]);
			obj.getClass().getDeclaredMethod("setOrderingInstitutionLine2", String.class).invoke(obj, orderingInst[2]);
			obj.getClass().getDeclaredMethod("setOrderingInstitutionLine3", String.class).invoke(obj, orderingInst[3]);
			obj.getClass().getDeclaredMethod("setOrderingInstitutionLine4", String.class).invoke(obj, orderingInst[4]);
		} catch(Exception e){
			logger.error(ExceptionUtil.getExceptionAsString(e));
		}
	}
	
	private void populateIntermediary(Object obj, String[] intermediary){
		try {
			obj.getClass().getDeclaredMethod("setIntermediaryCode", String.class).invoke(obj, intermediary[0]);
			obj.getClass().getDeclaredMethod("setIntermediaryLine1", String.class).invoke(obj, intermediary[1]);
			obj.getClass().getDeclaredMethod("setIntermediaryLine2", String.class).invoke(obj, intermediary[2]);
			obj.getClass().getDeclaredMethod("setIntermediaryLine3", String.class).invoke(obj, intermediary[3]);
			obj.getClass().getDeclaredMethod("setIntermediaryLine4", String.class).invoke(obj, intermediary[4]);
		} catch(Exception e){
			logger.error(ExceptionUtil.getExceptionAsString(e));
		}
	}
	
	private void populateBeneficiaryInst(Object obj, String[] beneInst){
		try {
			obj.getClass().getDeclaredMethod("setBeneficiaryInstitutionCode", String.class).invoke(obj, beneInst[0]);
			obj.getClass().getDeclaredMethod("setBeneficiaryInstitutionLine1", String.class).invoke(obj, beneInst[1]);
			obj.getClass().getDeclaredMethod("setBeneficiaryInstitutionLine2", String.class).invoke(obj, beneInst[2]);
			obj.getClass().getDeclaredMethod("setBeneficiaryInstitutionLine3", String.class).invoke(obj, beneInst[3]);
			obj.getClass().getDeclaredMethod("setBeneficiaryInstitutionLine4", String.class).invoke(obj, beneInst[4]);
		} catch(Exception e){
			logger.error(ExceptionUtil.getExceptionAsString(e));
		}
	}
	
	private void populateThirdReimbInst(Object obj, String[] thirdReimb){
		try {
			obj.getClass().getDeclaredMethod("setThirdReimbursementInstitutionCode", String.class).invoke(obj, thirdReimb[0]);
			obj.getClass().getDeclaredMethod("setThirdReimbursementInstitutionLine1", String.class).invoke(obj, thirdReimb[1]);
			obj.getClass().getDeclaredMethod("setThirdReimbursementInstitutionLine2", String.class).invoke(obj, thirdReimb[2]);
			obj.getClass().getDeclaredMethod("setThirdReimbursementInstitutionLine3", String.class).invoke(obj, thirdReimb[3]);
			obj.getClass().getDeclaredMethod("setThirdReimbursementInstitutionLine4", String.class).invoke(obj, thirdReimb[4]);
		} catch(Exception e){
			logger.error(ExceptionUtil.getExceptionAsString(e));
		}
	}
	
	private void populateOrderingCustomer(Object obj, String[] orderingCust){
		try {
			obj.getClass().getDeclaredMethod("setOrderingCustomerCode", String.class).invoke(obj, orderingCust[0]);
			obj.getClass().getDeclaredMethod("setOrderingCustomerLine1", String.class).invoke(obj, orderingCust[1]);
			obj.getClass().getDeclaredMethod("setOrderingCustomerLine2", String.class).invoke(obj, orderingCust[2]);
			obj.getClass().getDeclaredMethod("setOrderingCustomerLine3", String.class).invoke(obj, orderingCust[3]);
			obj.getClass().getDeclaredMethod("setOrderingCustomerLine4", String.class).invoke(obj, orderingCust[4]);
		} catch(Exception e){
			logger.error(ExceptionUtil.getExceptionAsString(e));
		}
	}
	
	private void populateIntermediaryInstitution(Object obj, String[] intermediaryInst){
		try {
			obj.getClass().getDeclaredMethod("setIntermediaryInstitutionCode", String.class).invoke(obj, intermediaryInst[0]);
			obj.getClass().getDeclaredMethod("setIntermediaryInstitutionLine1", String.class).invoke(obj, intermediaryInst[1]);
			obj.getClass().getDeclaredMethod("setIntermediaryInstitutionLine2", String.class).invoke(obj, intermediaryInst[2]);
			obj.getClass().getDeclaredMethod("setIntermediaryInstitutionLine3", String.class).invoke(obj, intermediaryInst[3]);
			obj.getClass().getDeclaredMethod("setIntermediaryInstitutionLine4", String.class).invoke(obj, intermediaryInst[4]);
		} catch(Exception e){
			logger.error(ExceptionUtil.getExceptionAsString(e));
		}
	}
	
	private void populateBeneficiaryCustomer(Object obj, String[] beneficaryCust){
		try {
			obj.getClass().getDeclaredMethod("setBeneficiaryCustomerCode", String.class).invoke(obj, beneficaryCust[0]);
			obj.getClass().getDeclaredMethod("setBeneficiaryCustomerLine1", String.class).invoke(obj, beneficaryCust[1]);
			obj.getClass().getDeclaredMethod("setBeneficiaryCustomerLine2", String.class).invoke(obj, beneficaryCust[2]);
			obj.getClass().getDeclaredMethod("setBeneficiaryCustomerLine3", String.class).invoke(obj, beneficaryCust[3]);
			obj.getClass().getDeclaredMethod("setBeneficiaryCustomerLine4", String.class).invoke(obj, beneficaryCust[4]);
		} catch(Exception e){
			logger.error(ExceptionUtil.getExceptionAsString(e));
		}
	}
	//FBPY-5046
		public String getAccountNumberFromText(String text) {
			String[] arrays = splitpartyIdentifier(text);
			String accountNumber = "";
			boolean accountFromMsg = false;
			if (arrays.length > 0) {
	            if (arrays[0].startsWith("/")){
	                accountNumber = arrays[0].substring(1);
	                accountFromMsg = true;
	            }
			}
			return accountNumber; //returns the creditor-account from BeneficiaryCustomer
		}
		public Boolean isAccountExist(String accountId) {
			IBOAccount accountDtls = (IBOAccount) factory.findByPrimaryKey(IBOAccount.BONAME, accountId, true);
			if (null != accountDtls && !accountDtls.getBoID().isEmpty()) {
				return true;     
			} else {
				return false;
			}
		}
		public String[] splitpartyIdentifier(String text) {
			String[] arrays = new String[3];
			arrays = text.split("[$]");
			return arrays;
		}

		public String getAccountCurrency(String accountId) {
			PaymentSwiftUtils utils = new PaymentSwiftUtils();
			String accountCurrency = StringUtils.EMPTY;
			IBOAttributeCollectionFeature accountIbo = utils.getAccountDetails(accountId);
			if (null != accountIbo) {
				accountCurrency = accountIbo.getF_ISOCURRENCYCODE();
			}
			return accountCurrency; //returns the currency of creditor-account
		}


}