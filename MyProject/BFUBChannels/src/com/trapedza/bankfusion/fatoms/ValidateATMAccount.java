package com.trapedza.bankfusion.fatoms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.BankFusionMessages;
import com.misys.bankfusion.common.constant.CommonConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ATM_ValidateATMAccount;

public class ValidateATMAccount extends AbstractUB_ATM_ValidateATMAccount {
	/**
	 * @author Anand Pandey
	 */
	private static final long serialVersionUID = 1L;
	private static final String CASHWITHDRAWAL = "ATMCashWithdrawal";
	private static final String CASHDEPOSIT = "ATMCashDeposit";
	private static final String FUNDTRANSFER = "ATMFundTransfer";
	private static final String BALANCEENQUIRY = "ATMBalanceEnquiry";
	private static final String BILLPAYMENTCASH = "ATMBillPaymentCash";
	private static final String POSREQUEST = "ATMAccPOS";
	private static final String DUALPOSREQUEST = "ATMDualAccPOS";
	private static final String BILLPAYMENTACCOUNT = "ATMBillPaymentAccount";
	private static final String MINISTATEMENT = "ATMMiniStatement";
	private static final transient Log LOGGER = LogFactory.getLog(ValidateATMAccount.class.getName());

	@SuppressWarnings("deprecation")
	public ValidateATMAccount(BankFusionEnvironment env) {
		super(env);
	}

	@SuppressWarnings("deprecation")
	public ValidateATMAccount() {
		super(BankFusionThreadLocal.getBankFusionEnvironment());
	}

	@Override
	public void process(BankFusionEnvironment env) {

		int errorCode = 0;
		String eventMsg = null;
		String accountId1 = getF_IN_accountId1();
		String accountId2 = getF_IN_accountId2();
		String messagetype = getF_IN_messageType();
		errorCode = validateAccount(accountId1, accountId2, messagetype);
		if (errorCode != 0) {
			eventMsg = BankFusionMessages.getFormattedMessage(errorCode, new String[] { accountId1 });
			LOGGER.error(eventMsg);
			setF_OUT_errCode(String.valueOf(errorCode));
			setF_OUT_errorCode(String.valueOf(errorCode));
			setF_OUT_errorMessage(eventMsg);

		}

	}

	private int validateAccount(String accountId1, String accountId2, String messageType) {
		int errorCode = 0;

		IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
		IBOAccount account = (IBOAccount) factory.findByPrimaryKey(IBOAccount.BONAME, accountId1, true);

		if (account == null) {

			errorCode = 20020000;// noAccountError
			return errorCode;
		} else {

			if (account.isF_CLOSED()) {
				return 40200284;
			} else if (account.isF_DORMANTSTATUS()) {
				return 40409528;
			} else if (account.isF_STOPPED()) {
				return 40200485;
			}
			if (account.getF_ACCRIGHTSINDICATOR() != 0)
				errorCode = validateAccountRightIndicator(accountId1, accountId2, account.getF_ACCRIGHTSINDICATOR(),
						messageType);
		}
		return errorCode;
	}

	private int validateAccountRightIndicator(String toAccount, String fromAccount, int accountRightIndicator,
			String messageType) {

		int errorCode = 0;

		switch (accountRightIndicator) {
		case -1:
			if (!messageType.equals(BALANCEENQUIRY))
				errorCode = 40112171;
			break;
		case 1:
			errorCode = 40007319;
			break;
		case 2:
			errorCode = 40007321;
			break;
		case 3:
			errorCode = 40112172;
			break;
		case 4:
			if (messageType.equals(CASHWITHDRAWAL) || messageType.equals(BILLPAYMENTCASH)
					|| messageType.equals(DUALPOSREQUEST))
				errorCode = 40180194;
			else if ((messageType.equals(FUNDTRANSFER) || messageType.equals(BILLPAYMENTACCOUNT))
					&& ((toAccount != fromAccount) && (fromAccount != CommonConstants.EMPTY_STRING)))
				errorCode = 40180194;
			break;
		case 5:
			if (messageType.equals(CASHWITHDRAWAL) || messageType.equals(BILLPAYMENTCASH)
					|| messageType.equals(POSREQUEST) || messageType.equals(DUALPOSREQUEST))
				errorCode = 40007323;
			else if ((messageType.equals(FUNDTRANSFER) || messageType.equals(BILLPAYMENTACCOUNT))
					&& ((toAccount != fromAccount) && (fromAccount != CommonConstants.EMPTY_STRING)))
				errorCode = 40007323;
			break;
		case 6:
			if (messageType.equals(CASHDEPOSIT) || messageType.equals(BILLPAYMENTCASH))
				/*errorCode = 40205204;*/
				LOGGER.info("ARI FLAG 6");
			else if ((messageType.equals(FUNDTRANSFER) || messageType.equals(BILLPAYMENTACCOUNT))
					&& (toAccount != CommonConstants.EMPTY_STRING))
				/*errorCode = 40205204;*/
				LOGGER.info("ARI FLAG 6");
			break; 
		case 7:

			if (messageType.equals(CASHDEPOSIT) || messageType.equals(BILLPAYMENTCASH))
				/*errorCode = 40007325;*/
				LOGGER.info("ARI FLAG 7");
			else if ((messageType.equals(FUNDTRANSFER) || messageType.equals(BILLPAYMENTACCOUNT))
					&& (toAccount != CommonConstants.EMPTY_STRING))
				LOGGER.info("ARI FLAG 7");
				/*errorCode = 40007325;*/
			break;
		case 8:
			errorCode = 0;
			break;
		case 9:

			if (!messageType.equals(BALANCEENQUIRY) && !messageType.equals(MINISTATEMENT))
				errorCode = 40007327;
			break;

		default:
			errorCode = 0;
			break;
		}
		return errorCode;

	}
}