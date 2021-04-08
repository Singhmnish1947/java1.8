/**
 *
 */
package com.misys.ub.swift;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOSWTDisposal;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.features.FixtureFeature;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;

/**
 * @author Gaurav.Aggarwal
 *
 */
public class UB_SWT_MT320Amend {


	public static final String MICROFLOW_NAME_320 = "UB_SWT_MT320Amend_SRV";
	public static final String CONTRACTAMOUNT = "ContractAmount";
	public static final String CONFCONTRACTAMOUNT = "ConfContractAmount";
	public static final String TRANSACTIONAMOUNT = "TransactionAmount";
	public static final String CONFTRANSACTIONAMOUNT = "ConfTransactionAmount";
	public static final String INTERESTRATE = "InterestRate";
	public static final String PREVDEALNUMBER = "PrevDealNumber";
	public static final String CONFDEALNUMBER = "ConfDealNumber";
	public static final String MATURITYDEALNUMBER = "MaturityDealNumber";
	public static final String PREVMESSAGESTATUS = "PrevMessageStatus";
	public static final String CONFMESSAGESTATUS = "ConfMessageStatus";
	public static final String PREVCONFIRMATIONFLAG = "PrevConfirmationFlag";
	public static final String CONFCONFIRMATIONFLAG = "ConfConfirmationFlag";
	public static final String PREVTRANSACTIONSTATUS = "PrevTransactionStatus";
	public static final String CONFTRANSACTIONSTATUS = "ConfTransactionStatus";
	public static final String MATURITYTRANSACTIONSTATUS = "MaturityTransactionStatus";
	public static final String MATURITYMESSAGESTATUS = "MaturityMessageStatus";
	public static final String MATURITYPAYMENTFLAG = "MaturityPaymentFlag";
	public static final String PREVPAYMENTFLAG = "PrevPaymentFlag";
	public static final String CONFPAYMENTFLAG = "ConfPaymentFlag";
	public static final String MATURITYCONFIRMATIONFLAG = "MaturityConfirmationFlag";
	public static final String MAINACCOUNTID = "MainAccountID";
	public static final String PREVIOUSCONTRACTAMOUNT = "PreviousContractAmount";
	public static final String PREVIOUSTRANSACTIONAMOUNT = "PreviousTransactionAmount";
	public static final String INTRESTAMOUNT = "InterestAmount";
	public static final String PREVIOUSINTRESTAMOUNT = "PreviousInterestAmount";
	public static final String CONFINTRESTAMOUNT = "ConfInterestAmount";
	public static final String PAYAWAYBOTH = "PayawayBoth";
	private transient final static Log logger = LogFactory.getLog(UB_SWT_MT320Amend.class.getName());

/*	private static final String dealquery="WHERE "
		+ IBOSWTDisposal.CUSTACCOUNTID +" = ? AND "
		+ IBOSWTDisposal.MESSAGETYPE +" ='320'  "
		+ "ORDER BY  " + IBOSWTDisposal.DEALORIGINATOR
		+ " , " + IBOSWTDisposal.VALUEDATE
		+ " DESC";		*/
	private static final String dealquery="SELECT "+ IBOSWTDisposal.TRANSACTIONAMOUNT+" AS TRANSACTIONAMOUNT ,"
						+IBOSWTDisposal.CONTRACTAMOUNT+" AS CONTRACTAMOUNT,"
						+IBOSWTDisposal.POSTDATE+" AS POSTDATE,"
						+IBOSWTDisposal.MATURITYDATE+" AS MATURITYDATE,"
						+IBOSWTDisposal.SWTDISPOSALID+" AS SWTDISPOSALID,"
						+IBOSWTDisposal.INTERESTRATE+" AS INTERESTRATE,"
						+IBOSWTDisposal.TRANSACTIONSTATUS+" AS TRANSACTIONSTATUS,"
						+IBOSWTDisposal.CONFIRMATIONFLAG+" AS CONFIRMATIONFLAG,"
						+IBOSWTDisposal.INTERESTAMOUNT+" AS INTERESTAMOUNT ,"
						+IBOSWTDisposal.PAYMENTFLAG+" AS PAYMENTFLAG ,"
						+IBOSWTDisposal.DEALORIGINATOR+" AS DEALORIGINATOR,"
						+IBOSWTDisposal.VALUEDATE+" AS VALUEDATE,"
						+IBOSWTDisposal.MESSAGESTATUS+" AS MESSAGESTATUS FROM "
						+ IBOSWTDisposal.BONAME+" WHERE "
						+IBOSWTDisposal.CUSTACCOUNTID+"=? AND "
						+IBOSWTDisposal.MESSAGETYPE+"='320'  ORDER BY  "
						+ IBOSWTDisposal.DEALORIGINATOR+" , "
						+IBOSWTDisposal.VALUEDATE+" DESC" ;



	public void MT320Amend(BankFusionEnvironment env, String AccountId, Boolean daycountFraction, BigDecimal intRate, String ISOCurrencyCode, BigDecimal OriginalPrincipal)	{
		FixtureFeature ff = new FixtureFeature(env);
		SimplePersistentObject orginationDisposal = null;
		SimplePersistentObject maturityDisposal = null;
		SimplePersistentObject confDisposal = null;
		HashMap map = new HashMap();
		HashMap inputmap = new HashMap();
		HashMap outputmap = new HashMap();
		ArrayList param = new ArrayList();
		List disposalRecord = null;
		String loanOrDeposit=null;
		String dealOrginator=null;
		String prevdealNo = null;
		String confdealNo = null;
		Boolean continue1 = false;
		String maturitydealNo = null;
		String prevTransactionStatus = "";
		String confTransactionStatus = "";
		String maturityTransactionStatus = "";
		String PayawayBoth = "No";
		int prevConfirmationFlag = 0;
		int prevPaymentFlag = 0;
		int confConfirmationFlag = 0;
		int confPaymentFlag = 0;
		int maturityPaymentFlag = 0;
		int prevMessageStatus = 0;
		int confMessageStatus = 0;
		int maturityConfirmationFlag = 0;
		int maturityMessageStatus = 0;
		BigDecimal orginationTransactionAmount=new BigDecimal(0.00);
		BigDecimal bd = new BigDecimal(0.00);
		BigDecimal orginationInterest = new BigDecimal(0.00);
		BigDecimal maturityInterest = new BigDecimal(0.00);
		BigDecimal confInterest = new BigDecimal(0.00);
		BigDecimal principleOnMaturity = new BigDecimal(0.00);
		BigDecimal maturityTransactionAmount=new BigDecimal(0.00);
		BigDecimal confTransactionAmount=new BigDecimal(0.00);
		BigDecimal previousContraAmount=new BigDecimal(0.00);
		BigDecimal confContraAmount=new BigDecimal(0.00);
		param.add(AccountId);
		//Iterator SWTIterator = BankFusionThreadLocal.getPersistanceFactory().findByQuery(IBOSWTDisposal.BONAME, dealquery, params, null).iterator();
		disposalRecord = env.getFactory().executeGenericQuery(dealquery, param, null);
		if(!disposalRecord.isEmpty()){
			maturityDisposal=(SimplePersistentObject) disposalRecord.get(0)	;
			dealOrginator = (String) maturityDisposal.getDataMap().get("DEALORIGINATOR");
			if(dealOrginator.equals("6") && disposalRecord.size()>2){
				orginationDisposal=(SimplePersistentObject) disposalRecord.get(0);
				confDisposal = (SimplePersistentObject) disposalRecord.get(1);
				maturityDisposal=(SimplePersistentObject) disposalRecord.get(2)	;
				continue1 = true;
			}else if (disposalRecord.size()>1){
				orginationDisposal=(SimplePersistentObject) disposalRecord.get(1);
				maturityDisposal=(SimplePersistentObject) disposalRecord.get(0)	;
				continue1 = true;
			}
			if (dealOrginator.equals("5") && (disposalRecord.size()>2)){
				confDisposal = (SimplePersistentObject) disposalRecord.get(2);
			}
		}else{
			return;
		}
		if (continue1){
		if(OriginalPrincipal.compareTo(bd) > 0 )
			loanOrDeposit="C";
		else
			loanOrDeposit="D";
		//Captilization Method
		if ((orginationDisposal.getDataMap().get("DEALORIGINATOR").toString().equals("3")) &&
				!(intRate.compareTo((BigDecimal) orginationDisposal.getDataMap().get("INTERESTRATE")) == 0)){
			if (maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString().equals("MATDEP")){
				if (((Integer)maturityDisposal.getDataMap().get("MESSAGESTATUS"))!=0){
					previousContraAmount = (BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT");
					orginationInterest = (BigDecimal) orginationDisposal.getDataMap().get("INTERESTAMOUNT");
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					principleOnMaturity = (BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT");
					maturityInterest=ff.findInterestAmount((java.sql.Date) maturityDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),
							intRate,loanOrDeposit,principleOnMaturity,AccountId,ISOCurrencyCode);
				    maturityTransactionAmount = (BigDecimal) maturityDisposal.getDataMap().get("TRANSACTIONAMOUNT");
				    prevConfirmationFlag =(Integer)orginationDisposal.getDataMap().get("CONFIRMATIONFLAG");
				    prevMessageStatus =(Integer)orginationDisposal.getDataMap().get("MESSAGESTATUS");
				    maturityConfirmationFlag=0;
				    maturityMessageStatus=0;
				    maturityTransactionStatus = "AM".concat(maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString());
				    prevTransactionStatus = orginationDisposal.getDataMap().get("TRANSACTIONSTATUS").toString();
				}else {
					previousContraAmount = (BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT");
					orginationInterest = ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) orginationDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					principleOnMaturity = (BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT");
					maturityInterest=ff.findInterestAmount((java.sql.Date) maturityDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);
				    maturityTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
				    prevConfirmationFlag =0;
				    prevMessageStatus =0;
				    maturityConfirmationFlag=0;
				    maturityMessageStatus=0;
				    maturityTransactionStatus = maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString();
				    prevTransactionStatus = "AM".concat(orginationDisposal.getDataMap().get("TRANSACTIONSTATUS").toString());
				}
			}else {
				if (((Integer)maturityDisposal.getDataMap().get("MESSAGESTATUS"))!=0){
					previousContraAmount = (BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT");
					orginationInterest = (BigDecimal) orginationDisposal.getDataMap().get("INTERESTAMOUNT");
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					maturityInterest=ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("VALUEDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);
				    principleOnMaturity = ((BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT"));
					maturityTransactionAmount = (BigDecimal) maturityDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					prevConfirmationFlag =(Integer)orginationDisposal.getDataMap().get("CONFIRMATIONFLAG");
				    prevMessageStatus =(Integer)orginationDisposal.getDataMap().get("MESSAGESTATUS");
				    maturityConfirmationFlag=0;
				    maturityMessageStatus=0;
				    maturityTransactionStatus = "AM".concat(maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString());
				    prevTransactionStatus = orginationDisposal.getDataMap().get("TRANSACTIONSTATUS").toString();
				}else {
					orginationInterest = ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) orginationDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);
					previousContraAmount = (BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT");
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					maturityInterest=ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);
				    principleOnMaturity = ((BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT")).add(maturityInterest);
					maturityTransactionAmount = (BigDecimal) maturityDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					prevConfirmationFlag =0;
				    prevMessageStatus =0;
				    maturityConfirmationFlag=0;
				    maturityMessageStatus=0;
				    maturityTransactionStatus = maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString();
				    prevTransactionStatus = "AM".concat(orginationDisposal.getDataMap().get("TRANSACTIONSTATUS").toString());
				}
			}
			prevPaymentFlag = (Integer)orginationDisposal.getDataMap().get("PAYMENTFLAG");
			maturityPaymentFlag = (Integer)maturityDisposal.getDataMap().get("PAYMENTFLAG");
		}

		//Payaway Interest Method
		else if ((orginationDisposal.getDataMap().get("DEALORIGINATOR").toString().equals("4")) &&
				!(intRate.compareTo((BigDecimal) orginationDisposal.getDataMap().get("INTERESTRATE")) == 0)){
			if (maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString().equals("MATDEP")){
				if ((((Integer)maturityDisposal.getDataMap().get("MESSAGESTATUS"))!=0 &&
						((Integer)maturityDisposal.getDataMap().get("CONFIRMATIONFLAG"))!=0)){
					previousContraAmount = (BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT");
					orginationInterest = (BigDecimal) orginationDisposal.getDataMap().get("INTERESTAMOUNT");
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					principleOnMaturity = (BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT");
					maturityInterest=ff.findInterestAmount((java.sql.Date) maturityDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),
							intRate,loanOrDeposit,principleOnMaturity,AccountId,ISOCurrencyCode);
				    maturityTransactionAmount = (BigDecimal) maturityDisposal.getDataMap().get("TRANSACTIONAMOUNT");
				    prevConfirmationFlag =(Integer)orginationDisposal.getDataMap().get("CONFIRMATIONFLAG");
				    prevMessageStatus =(Integer)orginationDisposal.getDataMap().get("MESSAGESTATUS");
				    maturityConfirmationFlag=0;
				    maturityMessageStatus=0;
				    maturityTransactionStatus = "AM".concat(maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString());
				    prevTransactionStatus = orginationDisposal.getDataMap().get("TRANSACTIONSTATUS").toString();
				    if (((Integer)maturityDisposal.getDataMap().get("PAYMENTFLAG")) != 0){
				    	maturityPaymentFlag = 0;
				    }else {
				    	maturityPaymentFlag = (Integer)maturityDisposal.getDataMap().get("PAYMENTFLAG");
				    }
				}else {
					previousContraAmount = (BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT");
					orginationInterest = ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) orginationDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					principleOnMaturity = (BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT");
					maturityInterest=ff.findInterestAmount((java.sql.Date) maturityDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);
				    maturityTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
				    prevConfirmationFlag =0;
				    prevMessageStatus =0;
				    maturityConfirmationFlag=0;
				    maturityMessageStatus=0;
				    maturityTransactionStatus = maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString();
				    prevTransactionStatus = "AM".concat(orginationDisposal.getDataMap().get("TRANSACTIONSTATUS").toString());
				    if (((Integer)maturityDisposal.getDataMap().get("PAYMENTFLAG")) != 0){
				    	maturityPaymentFlag = 0;
				    }else {
				    	maturityPaymentFlag = (Integer)maturityDisposal.getDataMap().get("PAYMENTFLAG");
				    }
				}
			}else {
				if ((((Integer)maturityDisposal.getDataMap().get("MESSAGESTATUS"))!=0 &&
						((Integer)maturityDisposal.getDataMap().get("CONFIRMATIONFLAG"))!=0)){
					previousContraAmount = (BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT");
					orginationInterest = (BigDecimal) orginationDisposal.getDataMap().get("INTERESTAMOUNT");
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					maturityInterest=ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("VALUEDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);
				    principleOnMaturity = ((BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT"));
					maturityTransactionAmount =maturityInterest ;
					prevConfirmationFlag =(Integer)orginationDisposal.getDataMap().get("CONFIRMATIONFLAG");
				    prevMessageStatus =(Integer)orginationDisposal.getDataMap().get("MESSAGESTATUS");
				    maturityConfirmationFlag=0;
				    maturityMessageStatus=0;
				    maturityTransactionStatus = "AM".concat(maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString());
				    prevTransactionStatus = orginationDisposal.getDataMap().get("TRANSACTIONSTATUS").toString();
				    if (((Integer)maturityDisposal.getDataMap().get("PAYMENTFLAG")) != 0){
				    	maturityPaymentFlag = 0;
				    }else {
				    	maturityPaymentFlag = (Integer)maturityDisposal.getDataMap().get("PAYMENTFLAG");
				    }
				}else {
					orginationInterest = ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("VALUEDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) orginationDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);
					previousContraAmount = ((BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT")).add(orginationInterest);
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					maturityInterest=ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("VALUEDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);
				    principleOnMaturity = ((BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT")).add(maturityInterest.subtract((BigDecimal) orginationDisposal.getDataMap().get("INTERESTAMOUNT")));
					maturityTransactionAmount = (BigDecimal) maturityDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					prevConfirmationFlag =0;
				    prevMessageStatus =0;
				    maturityConfirmationFlag=0;
				    maturityMessageStatus=0;
				    maturityTransactionStatus = maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString();
				    prevTransactionStatus = "AM".concat(orginationDisposal.getDataMap().get("TRANSACTIONSTATUS").toString());
				    if (((Integer)maturityDisposal.getDataMap().get("PAYMENTFLAG")) != 0){
				    	maturityPaymentFlag = 0;
				    }else {
				    	maturityPaymentFlag = (Integer)maturityDisposal.getDataMap().get("PAYMENTFLAG");
				    }
				}

			}
			prevPaymentFlag = (Integer)orginationDisposal.getDataMap().get("PAYMENTFLAG");
			//maturityPaymentFlag = (Integer)maturityDisposal.getDataMap().get("PAYMENTFLAG");
		}


		//Payaway Capital Method
		else if ((orginationDisposal.getDataMap().get("DEALORIGINATOR").toString().equals("5")) &&
				!(intRate.compareTo((BigDecimal) orginationDisposal.getDataMap().get("INTERESTRATE")) == 0)){
			if (maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString().equals("MATDEP")){
				if ((((Integer)maturityDisposal.getDataMap().get("MESSAGESTATUS"))!=0 &&
						((Integer)maturityDisposal.getDataMap().get("CONFIRMATIONFLAG"))!=0)){
					previousContraAmount = (BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT");
					orginationInterest = (BigDecimal) orginationDisposal.getDataMap().get("INTERESTAMOUNT");
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					principleOnMaturity = (BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT");
					IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory.getInstance()
			                .getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
					int value = (Integer)(ubInformationService.getBizInfo().getModuleConfigurationValue("MT320", "LOOKAHEADDAYS", env));
					if (value == 0){
						java.sql.Date maturityDate= ((java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"));
						Calendar cal= Calendar.getInstance();
						cal.setTime(maturityDate);
						cal.add(cal.DATE,value);
						//java.sql.Date toDate = (java.sql.Date) cal;
					}
					maturityInterest=ff.findInterestAmount((java.sql.Date) maturityDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),
							intRate,loanOrDeposit,principleOnMaturity,AccountId,ISOCurrencyCode);
				    maturityTransactionAmount = (BigDecimal) maturityDisposal.getDataMap().get("TRANSACTIONAMOUNT");
				    prevConfirmationFlag =(Integer)orginationDisposal.getDataMap().get("CONFIRMATIONFLAG");
				    prevMessageStatus =(Integer)orginationDisposal.getDataMap().get("MESSAGESTATUS");
				    maturityConfirmationFlag=0;
				    maturityMessageStatus=0;
				    maturityTransactionStatus = "AM".concat(maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString());
				    prevTransactionStatus = orginationDisposal.getDataMap().get("TRANSACTIONSTATUS").toString();
				}else {
					previousContraAmount = (BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT");
					orginationInterest = ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) orginationDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					principleOnMaturity = (BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT");
					maturityInterest=ff.findInterestAmount((java.sql.Date) maturityDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);
				    maturityTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
				    prevConfirmationFlag =0;
				    prevMessageStatus =0;
				    maturityConfirmationFlag=0;
				    maturityMessageStatus=0;
				    maturityTransactionStatus = maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString();
				    prevTransactionStatus = "AM".concat(orginationDisposal.getDataMap().get("TRANSACTIONSTATUS").toString());
				}
			}else {
				if ((((Integer)maturityDisposal.getDataMap().get("MESSAGESTATUS"))!=0 &&
						((Integer)maturityDisposal.getDataMap().get("CONFIRMATIONFLAG"))!=0)){
					previousContraAmount = (BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT");
					orginationInterest = (BigDecimal) orginationDisposal.getDataMap().get("INTERESTAMOUNT");
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					maturityInterest=ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("VALUEDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);
				    principleOnMaturity = ((BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT"));
					maturityTransactionAmount = (BigDecimal) maturityDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					prevConfirmationFlag =(Integer)orginationDisposal.getDataMap().get("CONFIRMATIONFLAG");
				    prevMessageStatus =(Integer)orginationDisposal.getDataMap().get("MESSAGESTATUS");
				    maturityConfirmationFlag=0;
				    maturityMessageStatus=0;
				    maturityTransactionStatus = "AM".concat(maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString());
				    prevTransactionStatus = orginationDisposal.getDataMap().get("TRANSACTIONSTATUS").toString();
				}else {
					orginationInterest = ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("VALUEDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) orginationDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);
					previousContraAmount = ((BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"));
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					maturityInterest=ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("VALUEDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);
				    principleOnMaturity = orginationInterest;
					maturityTransactionAmount = (BigDecimal) maturityDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					prevConfirmationFlag =0;
				    prevMessageStatus =0;
				    maturityConfirmationFlag=0;
				    maturityMessageStatus=0;
				    maturityTransactionStatus = maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString();
				    prevTransactionStatus = "AM".concat(orginationDisposal.getDataMap().get("TRANSACTIONSTATUS").toString());
				}

			}
			prevPaymentFlag = (Integer)orginationDisposal.getDataMap().get("PAYMENTFLAG");
			maturityPaymentFlag = (Integer)maturityDisposal.getDataMap().get("PAYMENTFLAG");
		}

		//PayAway Both Process
		else if ((orginationDisposal.getDataMap().get("DEALORIGINATOR").toString().equals("6")) &&
				!(intRate.compareTo((BigDecimal) orginationDisposal.getDataMap().get("INTERESTRATE")) == 0)){
			if (maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString().equals("MATDEP")){
				if ((((Integer)confDisposal.getDataMap().get("MESSAGESTATUS"))>1 &&
						((Integer)confDisposal.getDataMap().get("CONFIRMATIONFLAG"))!=0)){
					previousContraAmount = (BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT");
					orginationInterest = (BigDecimal) orginationDisposal.getDataMap().get("INTERESTAMOUNT");
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					principleOnMaturity = (BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT");
					maturityInterest=ff.findInterestAmount((java.sql.Date) maturityDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),
							intRate,loanOrDeposit,principleOnMaturity,AccountId,ISOCurrencyCode);
				    maturityTransactionAmount = (BigDecimal) maturityDisposal.getDataMap().get("TRANSACTIONAMOUNT");
				    prevConfirmationFlag =(Integer)orginationDisposal.getDataMap().get("CONFIRMATIONFLAG");
				    prevMessageStatus =(Integer)orginationDisposal.getDataMap().get("MESSAGESTATUS");
				    confContraAmount = (BigDecimal) confDisposal.getDataMap().get("CONTRACTAMOUNT");
				    confTransactionAmount = ((BigDecimal) confDisposal.getDataMap().get("CONTRACTAMOUNT")).add(orginationInterest);
				    confInterest = orginationInterest;
				    maturityConfirmationFlag=0;
				    maturityMessageStatus=0;
				    confConfirmationFlag=0;
				    confMessageStatus=0;
				    maturityTransactionStatus = "AM".concat(maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString());
				    prevTransactionStatus = orginationDisposal.getDataMap().get("TRANSACTIONSTATUS").toString();
				    if (((Integer)maturityDisposal.getDataMap().get("PAYMENTFLAG")) != 0){
				    	maturityPaymentFlag = 0;
				    	confPaymentFlag= 0;
				    }else {
				    	maturityPaymentFlag = (Integer)maturityDisposal.getDataMap().get("PAYMENTFLAG");
				    }
				}else {
					previousContraAmount = (BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT");
					orginationInterest = ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) orginationDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					principleOnMaturity = (BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT");
					maturityInterest=ff.findInterestAmount((java.sql.Date) maturityDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);
				    maturityTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
				    confContraAmount = (BigDecimal) confDisposal.getDataMap().get("CONTRACTAMOUNT");
				    confTransactionAmount = ((BigDecimal) confDisposal.getDataMap().get("CONTRACTAMOUNT")).add(orginationInterest);
				    confInterest = orginationInterest;
				    prevConfirmationFlag =(Integer)orginationDisposal.getDataMap().get("CONFIRMATIONFLAG");
				    prevMessageStatus =(Integer)orginationDisposal.getDataMap().get("MESSAGESTATUS");
				    maturityConfirmationFlag=0;
				    maturityMessageStatus=0;
				    confConfirmationFlag=0;
				    confMessageStatus=0;
				    maturityTransactionStatus = maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString();
				    prevTransactionStatus = orginationDisposal.getDataMap().get("TRANSACTIONSTATUS").toString();
				    confTransactionStatus = "AM".concat(confDisposal.getDataMap().get("TRANSACTIONSTATUS").toString());
				    if (((Integer)maturityDisposal.getDataMap().get("PAYMENTFLAG")) != 0){
				    	maturityPaymentFlag = 0;
				    	confPaymentFlag = 0;
				    }else {
				    	maturityPaymentFlag = (Integer)maturityDisposal.getDataMap().get("PAYMENTFLAG");
				    }
				}
				confdealNo = confDisposal.getDataMap().get("SWTDISPOSALID").toString();
				PayawayBoth = "Yes";
			}
		}
		prevdealNo = orginationDisposal.getDataMap().get("SWTDISPOSALID").toString();
		maturitydealNo = maturityDisposal.getDataMap().get("SWTDISPOSALID").toString();
		inputmap.put(PREVIOUSCONTRACTAMOUNT, (BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"));
	    inputmap.put(PREVIOUSINTRESTAMOUNT, orginationInterest);
	    inputmap.put(PREVIOUSTRANSACTIONAMOUNT, orginationTransactionAmount);
	    inputmap.put(CONTRACTAMOUNT, principleOnMaturity);
	    inputmap.put(INTRESTAMOUNT, maturityInterest);
	    inputmap.put(TRANSACTIONAMOUNT, maturityTransactionAmount);
	    inputmap.put(INTERESTRATE, intRate);
	    inputmap.put(MAINACCOUNTID, AccountId);
	    inputmap.put(PREVDEALNUMBER, prevdealNo );
	    inputmap.put(MATURITYDEALNUMBER, maturitydealNo );
	    inputmap.put(PREVMESSAGESTATUS, prevMessageStatus );
	    inputmap.put(MATURITYMESSAGESTATUS, maturityMessageStatus );
	    inputmap.put(PREVCONFIRMATIONFLAG, prevConfirmationFlag );
	    inputmap.put(MATURITYCONFIRMATIONFLAG, maturityConfirmationFlag );
	    inputmap.put(PREVPAYMENTFLAG, prevPaymentFlag );
	    inputmap.put(MATURITYPAYMENTFLAG, maturityPaymentFlag );
	    inputmap.put(PREVTRANSACTIONSTATUS, prevTransactionStatus );
	    inputmap.put(MATURITYTRANSACTIONSTATUS, maturityTransactionStatus );
	    inputmap.put(CONFCONFIRMATIONFLAG, confConfirmationFlag);
	    inputmap.put(CONFCONTRACTAMOUNT, confContraAmount );
	    inputmap.put(CONFDEALNUMBER, confdealNo );
	    inputmap.put(CONFMESSAGESTATUS, confMessageStatus );
	    inputmap.put(CONFPAYMENTFLAG, confPaymentFlag );
	    inputmap.put(CONFTRANSACTIONAMOUNT, confTransactionAmount );
	    inputmap.put(CONFTRANSACTIONSTATUS, confTransactionStatus );
	    inputmap.put(CONFINTRESTAMOUNT, confInterest);
		inputmap.put(PAYAWAYBOTH, PayawayBoth );
	    if (!(intRate.compareTo((BigDecimal) orginationDisposal.getDataMap().get("INTERESTRATE")) == 0)){
	    HashMap output = MFExecuter.executeMF(MICROFLOW_NAME_320, env,
	    		inputmap);
	    }
	}
	}


}
