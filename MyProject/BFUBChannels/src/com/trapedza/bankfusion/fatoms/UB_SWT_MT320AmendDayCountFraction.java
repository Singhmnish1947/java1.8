package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.swift.UB_SWT_MT320Amend;
import com.trapedza.bankfusion.bo.refimpl.IBOSWTDisposal;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_MT320AmendDayCountFraction;


public class UB_SWT_MT320AmendDayCountFraction extends AbstractUB_SWT_MT320AmendDayCountFraction{

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	public static final String MICROFLOW_NAME_320 = "UB_SWT_MT320Amend_SRV";
	public static final String MICROFLOW_NAME_320DayCountFraction = "UB_SWT_MT320AmendDayCountFraction_SRV";
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
	public static final String DAYCOUNTFRACTION = "DayCountFraction";
	
	public static final String ACCOUNTID = "AccountId";
	public static final String AMOUNT = "Amount";
	public static final String FROMDATE = "FromDate";
	public static final String TODATE = "ToDate";
	public static final String MATURITYDATE = "MaturityDate";
	public static final String ISOCURRENCYCODE = "ISOCurrencyCode";
	public static final String YEARDAYS = "YearDays";
	public static final String INTRATE = "IntRate";
	public static final String DRCRTYPE = "DRCRType";
	
	private transient final static Log logger = LogFactory.getLog(UB_SWT_MT320Amend.class.getName());
	
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
			+IBOSWTDisposal.DAYCOUNTFRACTION+" AS DAYCOUNTFRACTION ,"
			+IBOSWTDisposal.DEALORIGINATOR+" AS DEALORIGINATOR,"
			+IBOSWTDisposal.VALUEDATE+" AS VALUEDATE,"
			+IBOSWTDisposal.MESSAGESTATUS+" AS MESSAGESTATUS FROM "
			+ IBOSWTDisposal.BONAME+" WHERE "
			+IBOSWTDisposal.CUSTACCOUNTID+"=? AND "
			+IBOSWTDisposal.MESSAGETYPE+"='320'  ORDER BY  "
			+ IBOSWTDisposal.DEALORIGINATOR+" , "
			+IBOSWTDisposal.VALUEDATE+" DESC" ;					


	
	public UB_SWT_MT320AmendDayCountFraction(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}
	
	public void process(BankFusionEnvironment env) {
		MT320AmendDCFraction(env, getF_IN_AccountId(), getF_IN_ISOCurrencyCode(), getF_IN_DRCRType(), getF_IN_YearDays(), getF_IN_OriginalPrincipal(), getF_IN_intRate());
		super.process(env);
	}
	
	
	public void MT320AmendDCFraction(BankFusionEnvironment env, String accountId, String ISOCurrencyCode, String DRCRType, 
			int yearDays, BigDecimal originalPrincipal, BigDecimal intRate)	{

		SimplePersistentObject orginationDisposal = null;
		SimplePersistentObject maturityDisposal = null;
		SimplePersistentObject confDisposal = null;
		HashMap map = new HashMap();
		HashMap inputmap = new HashMap();
		HashMap outputmap = new HashMap();
		ArrayList param = new ArrayList();
		List disposalRecord = null;
		String dealOrginator=null;
		String prevdealNo = null;
		String confdealNo = null;
		String maturitydealNo = null;
		String prevTransactionStatus = "";
		String confTransactionStatus = "";
		String maturityTransactionStatus = "";
		String PayawayBoth = "No";
		String DayCountFraction = "";
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
		BigDecimal orginationInterest = new BigDecimal(0.00);
		BigDecimal maturityInterest = new BigDecimal(0.00);
		BigDecimal confInterest = new BigDecimal(0.00);
		BigDecimal principleOnMaturity = new BigDecimal(0.00);
		BigDecimal maturityTransactionAmount=new BigDecimal(0.00);
		BigDecimal confTransactionAmount=new BigDecimal(0.00);
		BigDecimal confContraAmount=new BigDecimal(0.00);
		param.add(accountId);
		//Iterator SWTIterator = BankFusionThreadLocal.getPersistanceFactory().findByQuery(IBOSWTDisposal.BONAME, dealquery, params, null).iterator();
		disposalRecord = env.getFactory().executeGenericQuery(dealquery, param, null);
		/*
		 * Added from Line 150 to 151 for the DE6609
		 */
		
		if(disposalRecord.isEmpty())
			 return;
		if(!disposalRecord.isEmpty()){
			maturityDisposal=(SimplePersistentObject) disposalRecord.get(0)	;
			dealOrginator = (String) maturityDisposal.getDataMap().get("DEALORIGINATOR");
			if(dealOrginator.equals("6")){
				orginationDisposal=(SimplePersistentObject) disposalRecord.get(0);
				confDisposal = (SimplePersistentObject) disposalRecord.get(1);
				maturityDisposal=(SimplePersistentObject) disposalRecord.get(2)	;
			}else{
				orginationDisposal=(SimplePersistentObject) disposalRecord.get(1);
				maturityDisposal=(SimplePersistentObject) disposalRecord.get(0)	;
			}
			if (dealOrginator.equals("5") && (disposalRecord.size()>2)){
				confDisposal = (SimplePersistentObject) disposalRecord.get(2);
			}
		}
		
		if (!(orginationDisposal.getDataMap().get("DAYCOUNTFRACTION").toString().equals(String.valueOf(yearDays)))){
		//Captilization Method
		if (orginationDisposal.getDataMap().get("DEALORIGINATOR").toString().equals("3")){
			if (maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString().equals("MATDEP")){
				if (((Integer)maturityDisposal.getDataMap().get("MESSAGESTATUS"))!=0){
					orginationInterest = (BigDecimal) orginationDisposal.getDataMap().get("INTERESTAMOUNT");
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					principleOnMaturity = (BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT");
					/*maturityInterest=ff.findInterestAmount((java.sql.Date) maturityDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),
							intRate,loanOrDeposit,principleOnMaturity,AccountId,ISOCurrencyCode);*/
					maturityInterest = findInterest(env, (java.sql.Date) maturityDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"), 
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"), 
							principleOnMaturity, intRate, accountId, DRCRType, ISOCurrencyCode, yearDays);
				    maturityTransactionAmount = (BigDecimal) maturityDisposal.getDataMap().get("TRANSACTIONAMOUNT");
				    prevConfirmationFlag =(Integer)orginationDisposal.getDataMap().get("CONFIRMATIONFLAG");
				    prevMessageStatus =(Integer)orginationDisposal.getDataMap().get("MESSAGESTATUS");
				    maturityConfirmationFlag=0;
				    maturityMessageStatus=0;
				    maturityTransactionStatus = "AM".concat(maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString());
				    prevTransactionStatus = orginationDisposal.getDataMap().get("TRANSACTIONSTATUS").toString();
				}else {
					/*orginationInterest = ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) orginationDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);*/
					orginationInterest = findInterest(env, (java.sql.Date) orginationDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"), 
							(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"), 
							intRate, accountId, DRCRType, ISOCurrencyCode, yearDays);
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					principleOnMaturity = (BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT");
					/*maturityInterest=ff.findInterestAmount((java.sql.Date) maturityDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);*/
					maturityInterest  = findInterest(env, (java.sql.Date) maturityDisposal.getDataMap().get("POSTDATE"), 
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"), 
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT"), 
							intRate, accountId, DRCRType, ISOCurrencyCode, yearDays);
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
					orginationInterest = (BigDecimal) orginationDisposal.getDataMap().get("INTERESTAMOUNT");
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
				/*	maturityInterest=ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("VALUEDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);*/
					maturityInterest = findInterest(env, (java.sql.Date) orginationDisposal.getDataMap().get("VALUEDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"), 
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"), 
							(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"), 
							intRate, accountId, DRCRType, ISOCurrencyCode, yearDays);
				    principleOnMaturity = ((BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT"));
					maturityTransactionAmount = (BigDecimal) maturityDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					prevConfirmationFlag =(Integer)orginationDisposal.getDataMap().get("CONFIRMATIONFLAG");
				    prevMessageStatus =(Integer)orginationDisposal.getDataMap().get("MESSAGESTATUS");
				    maturityConfirmationFlag=0;
				    maturityMessageStatus=0;
				    maturityTransactionStatus = "AM".concat(maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString());
				    prevTransactionStatus = orginationDisposal.getDataMap().get("TRANSACTIONSTATUS").toString();
				}else {
					/*orginationInterest = ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) orginationDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);*/
					orginationInterest = findInterest(env, (java.sql.Date) orginationDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"), 
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"), 
							(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"),
							intRate, accountId, DRCRType, ISOCurrencyCode, yearDays);
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
				/*	maturityInterest=ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);*/
					maturityInterest = findInterest(env, (java.sql.Date) orginationDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"), 
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"), 
							(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"), 
							intRate, accountId, DRCRType, ISOCurrencyCode, yearDays);
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
		else if (orginationDisposal.getDataMap().get("DEALORIGINATOR").toString().equals("4")){
			if (maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString().equals("MATDEP")){
				if ((((Integer)maturityDisposal.getDataMap().get("MESSAGESTATUS"))!=0 &&
						((Integer)maturityDisposal.getDataMap().get("CONFIRMATIONFLAG"))!=0)){
					orginationInterest = (BigDecimal) orginationDisposal.getDataMap().get("INTERESTAMOUNT");
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					principleOnMaturity = (BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT");
					/*maturityInterest=ff.findInterestAmount((java.sql.Date) maturityDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),
							intRate,loanOrDeposit,principleOnMaturity,AccountId,ISOCurrencyCode);*/
					maturityInterest = findInterest(env, (java.sql.Date) maturityDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"), 
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"), 
							principleOnMaturity, intRate, accountId, DRCRType, ISOCurrencyCode, yearDays);
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
					/*orginationInterest = ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) orginationDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);*/
					orginationInterest = findInterest(env, (java.sql.Date) orginationDisposal.getDataMap().get("POSTDATE"), 
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"), 
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"), 
							(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"), 
							intRate, accountId, DRCRType, ISOCurrencyCode, yearDays);
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					principleOnMaturity = (BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT");
					/*maturityInterest=ff.findInterestAmount((java.sql.Date) maturityDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);*/
					maturityInterest = findInterest(env, (java.sql.Date) maturityDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"), 
							(BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT"),
							intRate, accountId, DRCRType, ISOCurrencyCode, yearDays);
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
					orginationInterest = (BigDecimal) orginationDisposal.getDataMap().get("INTERESTAMOUNT");
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					/*maturityInterest=ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("VALUEDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);*/
					maturityInterest = findInterest(env, (java.sql.Date) orginationDisposal.getDataMap().get("VALUEDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"), 
							intRate, accountId, DRCRType, ISOCurrencyCode, yearDays);
				    principleOnMaturity = ((BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT"));
					maturityTransactionAmount = maturityInterest;
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
					/*orginationInterest = ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("VALUEDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) orginationDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);*/
					orginationInterest = findInterest(env, (java.sql.Date) orginationDisposal.getDataMap().get("VALUEDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"), 
							intRate, accountId, DRCRType, ISOCurrencyCode, yearDays);
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					/*maturityInterest=ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("VALUEDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);*/
					maturityInterest = findInterest(env, (java.sql.Date) orginationDisposal.getDataMap().get("VALUEDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"), 
							(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"), 
							intRate, accountId, DRCRType, ISOCurrencyCode, yearDays);
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
		else if (orginationDisposal.getDataMap().get("DEALORIGINATOR").toString().equals("5")){
			if (maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString().equals("MATDEP")){
				if ((((Integer)maturityDisposal.getDataMap().get("MESSAGESTATUS"))!=0 &&
						((Integer)maturityDisposal.getDataMap().get("CONFIRMATIONFLAG"))!=0)){
					orginationInterest = (BigDecimal) orginationDisposal.getDataMap().get("INTERESTAMOUNT");
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					principleOnMaturity = (BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT");
					/*maturityInterest=ff.findInterestAmount((java.sql.Date) maturityDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),
							intRate,loanOrDeposit,principleOnMaturity,AccountId,ISOCurrencyCode);*/
					maturityInterest = findInterest(env, (java.sql.Date) maturityDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"), 
							principleOnMaturity, intRate, accountId, DRCRType, ISOCurrencyCode, yearDays);
				    maturityTransactionAmount = (BigDecimal) maturityDisposal.getDataMap().get("TRANSACTIONAMOUNT");
				    prevConfirmationFlag =(Integer)orginationDisposal.getDataMap().get("CONFIRMATIONFLAG");
				    prevMessageStatus =(Integer)orginationDisposal.getDataMap().get("TRANSACTIONSTATUS");
				    maturityConfirmationFlag=0;
				    maturityMessageStatus=0;
				    maturityTransactionStatus = "AM".concat(maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString());
				    prevTransactionStatus = orginationDisposal.getDataMap().get("TRANSACTIONSTATUS").toString();
				}else {
					/*orginationInterest = ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) orginationDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);*/
					orginationInterest = findInterest(env, (java.sql.Date) orginationDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"), 
							intRate, accountId, DRCRType, ISOCurrencyCode, yearDays);
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					principleOnMaturity = (BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT");
					/*maturityInterest=ff.findInterestAmount((java.sql.Date) maturityDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);*/
					maturityInterest = findInterest(env, (java.sql.Date) maturityDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT"),
							intRate, accountId, DRCRType, ISOCurrencyCode, yearDays);
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
					orginationInterest = (BigDecimal) orginationDisposal.getDataMap().get("INTERESTAMOUNT");
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					/*maturityInterest=ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("VALUEDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);*/
					maturityInterest = findInterest(env, (java.sql.Date) orginationDisposal.getDataMap().get("VALUEDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT"), 
							intRate, accountId, DRCRType, ISOCurrencyCode, yearDays);
				    principleOnMaturity = ((BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT"));
					maturityTransactionAmount = (BigDecimal) maturityDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					prevConfirmationFlag =(Integer)orginationDisposal.getDataMap().get("CONFIRMATIONFLAG");
				    prevMessageStatus =(Integer)orginationDisposal.getDataMap().get("MESSAGESTATUS");
				    maturityConfirmationFlag=0;
				    maturityMessageStatus=0;
				    maturityTransactionStatus = "AM".concat(maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString());
				    prevTransactionStatus = orginationDisposal.getDataMap().get("TRANSACTIONSTATUS").toString();
				}else {
					/*orginationInterest = ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("VALUEDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) orginationDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);*/
					orginationInterest = findInterest(env, (java.sql.Date) orginationDisposal.getDataMap().get("VALUEDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"),
							intRate, accountId, DRCRType, ISOCurrencyCode, yearDays);
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					/*maturityInterest=ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("VALUEDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);*/
					maturityInterest = findInterest(env, (java.sql.Date) orginationDisposal.getDataMap().get("VALUEDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT"),
							intRate, accountId, DRCRType, ISOCurrencyCode, yearDays);
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
		else if (orginationDisposal.getDataMap().get("DEALORIGINATOR").toString().equals("6")){
			if (maturityDisposal.getDataMap().get("TRANSACTIONSTATUS").toString().equals("MATDEP")){
				if ((((Integer)confDisposal.getDataMap().get("MESSAGESTATUS"))>1 &&
						((Integer)confDisposal.getDataMap().get("CONFIRMATIONFLAG"))!=0)){
					orginationInterest = (BigDecimal) orginationDisposal.getDataMap().get("INTERESTAMOUNT");
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					principleOnMaturity = (BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT");
					/*maturityInterest=ff.findInterestAmount((java.sql.Date) maturityDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),
							intRate,loanOrDeposit,principleOnMaturity,AccountId,ISOCurrencyCode);*/
					maturityInterest = findInterest(env, (java.sql.Date) maturityDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"),
							principleOnMaturity, intRate, accountId, DRCRType, ISOCurrencyCode, yearDays);
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
					/*orginationInterest = ff.findInterestAmount((java.sql.Date) orginationDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) orginationDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);*/
					orginationInterest = findInterest(env, (java.sql.Date) orginationDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"), 
							(java.sql.Date) orginationDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"),
							intRate, accountId, DRCRType, ISOCurrencyCode, yearDays);
					orginationTransactionAmount = (BigDecimal) orginationDisposal.getDataMap().get("TRANSACTIONAMOUNT");
					principleOnMaturity = (BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT");
					/*maturityInterest=ff.findInterestAmount((java.sql.Date) maturityDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("INTERESTRATE"),intRate,
							loanOrDeposit,(BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT"),AccountId,ISOCurrencyCode);*/
					maturityInterest = findInterest(env, (java.sql.Date) maturityDisposal.getDataMap().get("POSTDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"),
							(java.sql.Date) maturityDisposal.getDataMap().get("MATURITYDATE"),
							(BigDecimal) maturityDisposal.getDataMap().get("CONTRACTAMOUNT"),
							intRate, accountId, DRCRType, ISOCurrencyCode, yearDays);
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
		DayCountFraction = String.valueOf(yearDays); 
		inputmap.put(PREVIOUSCONTRACTAMOUNT, (BigDecimal) orginationDisposal.getDataMap().get("CONTRACTAMOUNT"));
	    inputmap.put(PREVIOUSINTRESTAMOUNT, orginationInterest);
	    inputmap.put(PREVIOUSTRANSACTIONAMOUNT, orginationTransactionAmount);
	    inputmap.put(CONTRACTAMOUNT, principleOnMaturity);
	    inputmap.put(INTRESTAMOUNT, maturityInterest);
	    inputmap.put(TRANSACTIONAMOUNT, maturityTransactionAmount);
	    inputmap.put(INTERESTRATE, intRate);
	    inputmap.put(MAINACCOUNTID, accountId);
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
	    inputmap.put(DAYCOUNTFRACTION, DayCountFraction );
	    
	    HashMap output = MFExecuter.executeMF(MICROFLOW_NAME_320, env,
	    		inputmap);
			   
	}
	}
	
	public BigDecimal findInterest(BankFusionEnvironment env, Date FromDate, Date ToDate, Date MaturityDate, 
			BigDecimal Amount, BigDecimal intRate, String AccountId, String DRCRType, String ISOCurrencyCode, int YearDays){
		HashMap inputmap = new HashMap();
		HashMap outputmap = new HashMap();
		inputmap.put(ACCOUNTID, AccountId);
	    inputmap.put(AMOUNT, (BigDecimal) Amount);
	    inputmap.put(FROMDATE, FromDate);
	    inputmap.put(TODATE, ToDate);
	    inputmap.put(MATURITYDATE, MaturityDate);
	    inputmap.put(ISOCURRENCYCODE, ISOCurrencyCode);
	    inputmap.put(INTRATE, intRate);
	    inputmap.put(DRCRTYPE, DRCRType);
	    inputmap.put(YEARDAYS, YearDays );
	    HashMap output = MFExecuter.executeMF(MICROFLOW_NAME_320DayCountFraction, env,
	    		inputmap);
	    return ((BigDecimal) output.get("InterestAmount"));
			   
	}
	
	
}
