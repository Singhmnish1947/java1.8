package com.misys.ub.atm;

import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractATMAccountFetchFatom;
/**
 * @author Prateek 
 * @date 14 Sep 2015
 * @project FBE
 * @Description This class file is used to get the Account number for the message types.
 * 
 */
public class ATMAccountFetchFatom extends AbstractATMAccountFetchFatom{

	public ATMAccountFetchFatom(BankFusionEnvironment env){
		super(env);
	}
	private String accountID;
	/**
	 * constants representing ATM and POS message types.
	 */
	private static enum ATMMessageType{
		
		CASH_DEPOSIT("ATMCashDeposit"),
		CASH_WITHDRAWAL("ATMCashWithdrawal"),
		DUAL_CASH_WITHDRAWAL("DualATMCashWithdrawal"),
		FUNDS_TRANSFER("ATMFundTransfer"),
		BILL_PAYMENT_ACCOUNT("ATMBillPaymentAccount"),
		BILL_PAYMENT_CHEQUE("ATMBillPaymentCheque"),
		BILL_PAYMENT_CASH("ATMBillPaymentCash"),
		CHEQUE_DEPOSIT("ATMChequeDeposit"),
		BALANCE_ENQUIRY("ATMBalanceEnquiry"),
		CHEQUE_BOOK_REQUEST("ATMChqBookRq"),
		MINI_STATEMENT("ATMMiniStatement"),
		CHEQUE_STATUS_ENQUIRY("ATMChqStatusEnq"),
		ATM_ACC_POS("ATMAccPOS"),
		ATM_DUAL_ACC_POS("ATMDualAccPOS");
		/**
		 * Message type used to get accountID
		 */
		private String msgType;
        /**
        * 
        * @param msgType
        */
		private ATMMessageType(String msgType){
			this.msgType=msgType;
		}
		
		/**
		 * 
		 * Method Description: returns the accountID associated with the respective enum
		 */
		public String getMsgType(){
			return msgType;
		}
	} 
	  /**
     * This method sets the accountID based on the message type input
     */
	public void process(BankFusionEnvironment env) {
		String messageType=getF_IN_messageType();
		if(messageType.equals(ATMMessageType.CASH_DEPOSIT.getMsgType())){
			accountID=getF_IN_accountIdentification2_103();
		}
		else if(messageType.equals(ATMMessageType.CASH_WITHDRAWAL.getMsgType()) || messageType.equals(ATMMessageType.DUAL_CASH_WITHDRAWAL.getMsgType()) ||
				messageType.equals(ATMMessageType.CHEQUE_DEPOSIT.getMsgType()) || messageType.equals(ATMMessageType.BALANCE_ENQUIRY.getMsgType()) || 
				messageType.equals(ATMMessageType.CHEQUE_BOOK_REQUEST.getMsgType()) || messageType.equals(ATMMessageType.MINI_STATEMENT.getMsgType()) ||  
				messageType.equals(ATMMessageType.CHEQUE_STATUS_ENQUIRY.getMsgType())){
			accountID=getF_IN_accountnumber1_102_2();
		}
		else if(messageType.equals(ATMMessageType.FUNDS_TRANSFER.getMsgType())){
			accountID=getF_IN_accountnumber1_102_2();
		}
		
		else if(messageType.equals(ATMMessageType.BILL_PAYMENT_ACCOUNT.getMsgType()) || messageType.equals(ATMMessageType.BILL_PAYMENT_CHEQUE.getMsgType())
    	|| messageType.equals(ATMMessageType.BILL_PAYMENT_CASH.getMsgType())){
			accountID=getF_IN_accountnumber1_102_2();
		}
		/*else if(messageType.equals(ATMMessageType.ATM_ACC_POS.getMsgType()) || messageType.equals(ATMMessageType.ATM_DUAL_ACC_POS.getMsgType())){
			accountID=getF_IN_accountnumber1_102_2();
		}*/
		setF_OUT_accountID(accountID);
		
	}
	
}
