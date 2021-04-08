package com.trapedza.bankfusion.fatoms;

import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_CMS_CodeMapping;

public class UB_CMS_CodeMapping extends AbstractUB_CMS_CodeMapping {

	private static final String CREDIT_CARD = "001";
	private static final String DEBIT_CARD = "002";
	private static final String ELECTYRONIC_MONEY_CARD = "003";
	private static final String SECURED_CREDIT_CARD = "004";
	private static final String UB_DEBIT_CARD = "D";
	private static final String UB_CREDIT_CARD = "C";
	private static final String UB_ELECTYRONIC_MONEY_CARD = "E";
	private static final String UB_SECURED_CREDIT_CARD = "S";	
	private static final String ATMCARDSTATUS_ACTIVE = "ACTIVE";
	private static final String ATMCARDSTATUS_BLOCKED = "BLOCKED";
	private static final String ATMCARDSTATUS_CANCELLED = "CANCELLED";
	private static final String ATMCARDSTATUS_HOTLSTD = "HOTLSTD";
	private static final String ATMCARDSTATUS_LOST = "LOST";

	private static final String CBSCARDSTATUS_Requested = "001";
	private static final String CBSCARDSTATUS_Ordered = "002";
	private static final String CBSCARDSTATUS_Issued = "003";
	private static final String CBSCARDSTATUS_ACTIVE = "004";
	private static final String CBSCARDSTATUS_Blocked = "005";
	private static final String CBSCARDSTATUS_Closed = "006";
	private static final String CBSCARDSTATUS_Expired = "007";
	private static final String CBSCARDSTATUS_Rejected = "008";

	private static final String E_INVALID_CARD_STATUS = "40422501";
	private static final String E_INVALID_CARD_TYPE = "40422502";

	public UB_CMS_CodeMapping() {
		// TODO Auto-generated constructor stub
	}

	public UB_CMS_CodeMapping(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void process(BankFusionEnvironment env) throws BankFusionException {
		super.process(env);
		String cardStatus = getF_IN_cardStatus();
		String errorCode = CommonConstants.EMPTY_STRING;
		String cardType = getF_IN_cardType();
		if (cardType.equals(DEBIT_CARD)) {
			setF_OUT_UBCardType(UB_DEBIT_CARD);
			setF_OUT_errorCode(errorCode);
		} 
		/*
		 * These code are only for checking purpose this has not to be implemented right now. this 
		 * should be implement when UB will support the electronic and secure card.
		 */
		else if (cardType.equals(CREDIT_CARD)) {
			setF_OUT_UBCardType(UB_CREDIT_CARD);
			setF_OUT_errorCode(errorCode);
		} else if (cardType.equals(ELECTYRONIC_MONEY_CARD)) {
			setF_OUT_UBCardType(UB_ELECTYRONIC_MONEY_CARD);
			setF_OUT_errorCode(errorCode);
		}else if (cardType.equals(SECURED_CREDIT_CARD)) {
			setF_OUT_UBCardType(UB_SECURED_CREDIT_CARD);
			setF_OUT_errorCode(errorCode);
		}else{
			setF_OUT_UBCardType(CommonConstants.EMPTY_STRING);
			setF_OUT_errorCode(E_INVALID_CARD_TYPE);
			errorCode = E_INVALID_CARD_TYPE;
		}
	
		if (errorCode.equals(CommonConstants.EMPTY_STRING)) {
			if (cardStatus.equals(CBSCARDSTATUS_ACTIVE)) {
				setF_OUT_refrencedCode(ATMCARDSTATUS_ACTIVE);
				setF_OUT_errorCode(errorCode);
			} else if (cardStatus.equals(CBSCARDSTATUS_Blocked)) {
				setF_OUT_refrencedCode(ATMCARDSTATUS_BLOCKED);
				setF_OUT_errorCode(errorCode);
			} else if (cardStatus.equals(CBSCARDSTATUS_Closed) || cardStatus.equals(CBSCARDSTATUS_Expired)) {
				setF_OUT_refrencedCode(ATMCARDSTATUS_CANCELLED);
				setF_OUT_errorCode(errorCode);
			}else {
				setF_OUT_refrencedCode(CommonConstants.EMPTY_STRING);
				setF_OUT_errorCode(E_INVALID_CARD_STATUS);
				errorCode = E_INVALID_CARD_STATUS;
			}
		}
	}
}
