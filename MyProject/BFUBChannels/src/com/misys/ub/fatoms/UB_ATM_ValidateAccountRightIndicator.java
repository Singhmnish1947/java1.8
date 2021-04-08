package com.misys.ub.fatoms;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ATM_ValidateAccountRightIndicator;

public class UB_ATM_ValidateAccountRightIndicator extends
		AbstractUB_ATM_ValidateAccountRightIndicator {

	/**
	 * The constructor that indicates we're in a runtime environment and we
	 * should initialize the Fatom with only those attributes necessary.
	 *
	 * @param env
	 *            BankFusionEnvironment
	 * @return void
	 */
	public UB_ATM_ValidateAccountRightIndicator(BankFusionEnvironment env) {
		super(env);
	}


	private final static String Minus = "-1";
	private final static String One = "1";
	private final static String Two = "2";
	private final static String Three = "3";
	private final static String Four = "4";
	private final static String Five = "5";
	private final static String Six = "6";
	private final static String Seven = "7";
	//private final static String Eight = "8";
	private final static String Nine = "9";
	private final static String cashWithdrawal = "ATMCashWithdrawal";
	private final static String cashDeposit = "ATMCashDeposit";
	private final static String fundTransfer = "ATMFundTransfer";
	private final static String balanceEnquiry = "ATMBalanceEnquiry";
	private final static String billPaymentCash = "ATMBillPaymentCash";
	private final static String posRequest = "ATMAccPOS";
	private final static String dualPosRequest = "ATMDualAccPOS";
	private final static String billPaymentAccount = "ATMBillPaymentAccount";
	private final static String miniStatement = "ATMMiniStatement";

	public void process(BankFusionEnvironment env) throws BankFusionException {

		String accRightIndicator = getF_IN_accountRightIndicator();
		String messageType = getF_IN_messageType();
		String toAccount = getF_IN_toAccount();
		String fromAccount = getF_IN_fromAccount();
		try {
			if (accRightIndicator.equals(Minus) && !messageType.equals(balanceEnquiry)) {
				setF_OUT_errorCode("40112171");
			} else if (accRightIndicator.equals(One)) {
				setF_OUT_errorCode("40007319");
			} else if (accRightIndicator.equals(Two)) {
				setF_OUT_errorCode("40007321");
			} else if (accRightIndicator.equals(Three)) {
				setF_OUT_errorCode("40112172");
			} else if (accRightIndicator.equals(Four)) {
				if (messageType.equals(cashWithdrawal)|| messageType.equals(billPaymentCash)|| messageType.equals(dualPosRequest))
				{
					setF_OUT_errorCode("40180194");
				}
				else if((messageType.equals(fundTransfer) || messageType.equals(billPaymentAccount)) && ((toAccount != fromAccount) && (fromAccount!=CommonConstants.EMPTY_STRING))) {

					setF_OUT_errorCode("40180194");
				}
				else
				{
				setF_OUT_errorCode(null);
				}
			} else if (accRightIndicator.equals(Five)) {
				if (messageType.equals(cashWithdrawal)|| messageType.equals(billPaymentCash)||messageType.equals(posRequest)|| messageType.equals(dualPosRequest)) {
					setF_OUT_errorCode("40007323");
				}
				else if((messageType.equals(fundTransfer) || messageType.equals(billPaymentAccount)) && ((toAccount != fromAccount) && (fromAccount!=CommonConstants.EMPTY_STRING))) {

					setF_OUT_errorCode("40007323");
				}
				else
				{
					setF_OUT_errorCode(null);
				}
			} else if (accRightIndicator.equals(Six)) {
				if (messageType.equals(cashDeposit)	|| messageType.equals(billPaymentCash)) {
					setF_OUT_errorCode("40205204");
				} else if((messageType.equals(fundTransfer) || messageType.equals(billPaymentAccount)) && (toAccount!=CommonConstants.EMPTY_STRING)) {

					setF_OUT_errorCode("40205204");
				}

				else{
					setF_OUT_errorCode(null);
				}
			} else if (accRightIndicator.equals(Seven)) {
				if (messageType.equals(cashDeposit)	|| messageType.equals(billPaymentCash)) {
					setF_OUT_errorCode("40007325");
				}else if((messageType.equals(fundTransfer) || messageType.equals(billPaymentAccount)) && (toAccount!=CommonConstants.EMPTY_STRING)) {

					setF_OUT_errorCode("40007325");
				}
				else {
					setF_OUT_errorCode(null);
				}
			}else if (accRightIndicator.equals(Nine)) {
				if (messageType.equals(balanceEnquiry)	|| messageType.equals(miniStatement)) {
					setF_OUT_errorCode(null);
				} else{
				setF_OUT_errorCode("40007327");}
			}

		}
		catch (BankFusionException e) {
			e.printStackTrace();
		}

	}
}
