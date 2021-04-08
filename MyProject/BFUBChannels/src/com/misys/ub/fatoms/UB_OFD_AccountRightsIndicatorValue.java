/**
 * 
 */
package com.misys.ub.fatoms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_OFD_AccountRightsIndicatorValue;

/**
 * @author Ram.Pandey
 * 
 */

public class UB_OFD_AccountRightsIndicatorValue extends
		AbstractUB_OFD_AccountRightsIndicatorValue {

	/** */
	private static final long serialVersionUID = 1L;

	private transient final static Log logger = LogFactory
			.getLog(UB_OFD_AccountRightsIndicatorValue.class);

	public UB_OFD_AccountRightsIndicatorValue() {
		super();
	}

	public UB_OFD_AccountRightsIndicatorValue(BankFusionEnvironment env) {
		super();
	}
	private static int accountRightsIndicator = 100;
	private static boolean trueConstant = true;
	private static boolean hasCreditInterest = false;
	private static boolean hasDebitInterest = false;
	private static String customerStatusIsBlocked = "BLOCKED";
	private static String customerStatus = CommonConstants.EMPTY_STRING;
	public void process(BankFusionEnvironment env) throws BankFusionException {
		customerStatus = getF_IN_customerStatus();
		hasCreditInterest = isF_IN_hasCredit();
		hasDebitInterest = isF_IN_hasDebit();
		boolean falseConstant = false;
		if(hasCreditInterest==falseConstant && hasDebitInterest == falseConstant)
		{
			setF_OUT_isInternalAccount(trueConstant);
		}
		if(customerStatus == customerStatusIsBlocked)
		{
			setF_OUT_isDecesedOrLiquidated(trueConstant);
		}
		accountRightsIndicator = getF_IN_accountRightsIndicator();
		 switch (accountRightsIndicator) {
			case 0:
				setF_OUT_enquiryAllowed(trueConstant);
				logger.info("Account Right Indicator is 0 and 'Enquiry is Allowed'");
				break;
			case 1:
				setF_OUT_passwordForPosting(trueConstant);
				logger.info("Account Right Indicator is 1 and 'Posting with Password is allowed'");
				break;
			case 2:
				setF_OUT_allTxnBlocked(trueConstant);
				logger.info("Account Right Indicator is 2 and 'Posting is Blocked'");
				break;
			case 3:
				break;
			case 4:
				setF_OUT_allDebitTransactionBlocked(trueConstant);
				logger.info("Account Right Indicator is 4 and ' All Debit Transaction is Blocked'");
				break;
			case 5:
				setF_OUT_allDebitTransactionRefered(trueConstant);
				setF_OUT_passwordForDebit(trueConstant);
				logger.info("Account Right Indicator is 5 and ' All Debit Transaction is Refered ' and For ' Password required for Debit Transaction '");
				break;
			case 6:
				setF_OUT_allCreditTransactionBlocked(trueConstant);
				logger.info("Account Right Indicator is 6 and ' All Credit Transaction is Blocked'");
				break;
			case 7:
				 setF_OUT_allCreditTransactionRefered(trueConstant);
				 setF_OUT_passwordForCredit(trueConstant);
					logger.info("Account Right Indicator is 7 and ' All Credit Transaction is Refered ' and For ' Password required for Credit Transaction '");
				break;
			case 8:
				setF_OUT_passwordForEnquiry(trueConstant);
				logger.info("Account Right Indicator is 8 and 'Password based Enquiry is Allowed'");
				break;
			case 9:
				break;
			case 10:
				break;
			case 11:
				break;
			}
	}
}
