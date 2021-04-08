
/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.datacenter.DataCenterCommonUtils;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_ValidateInputData;

public class UB_SWT_ValidateInputData extends AbstractUB_SWT_ValidateInputData {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private static final String MESSAGE_TYPE = "Message Type";
    private static final String CHARGE_AMOUNT = "Charge Amount";
    private static final String DEAL_NUMBER = "Deal Number";
    private static final String INTEREST_AMOUNT = "Interest Amount";
    private static final String RELATED_DEAL_NUMBER = "Related Deal Number";
    private static final String DRAFT_NUMBER = "Draft Number";
    private static final String SETTLEMENT_INSTRUCTION_NUMBER = "Settlement Instruction Number";
    private static final String TRANSACTION_AMOUNT = "Transation Amount";
    private static final String FIFTEEN = "15";
    private static final String THREE = "3";
    private static final String SIXTEEN = "16";
    private static final String TERM = "Term";
    private static final String TEN = "10";
    private static final int INT_FIFTEEN = 15;
    private static final int INT_THREE = 3;
    private static final int INT_SIXTEEN = 16;
    private static final int INT_TEN = 10;

    public UB_SWT_ValidateInputData(BankFusionEnvironment env) {
        super(env);
        // TODO Auto-generated constructor stub
    }

    public void process(BankFusionEnvironment env) {

        setF_OUT_messagePreference(
                getMessagePreference(getF_IN_messagePreference() != null ? getF_IN_messagePreference() : StringUtils.EMPTY));
        validateInputTagLength(env);

    }

    /**
     * @param env
     */
    @SuppressWarnings("deprecation")
    private void validateInputTagLength(BankFusionEnvironment env) {
        String eMessage = null;
        String dealNumber = getF_IN_DealNumber();
        if (getF_IN_MessageType() != null) {
            if (getF_IN_MessageType().toString().length() > INT_THREE) {
                EventsHelper.handleEvent(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS,
                        new Object[] { MESSAGE_TYPE, THREE }, new HashMap(), env);
            }
        }

        if (getF_IN_SettlementInstructionNumber() != null) {
            if (getF_IN_SettlementInstructionNumber().toString().length() > INT_TEN) {
                EventsHelper.handleEvent(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS,
                        new Object[] { SETTLEMENT_INSTRUCTION_NUMBER, TEN }, new HashMap(), env);
            }
        }
        if (getF_IN_RelatedDealNumber() != null) {
            if (getF_IN_RelatedDealNumber().length() > INT_SIXTEEN) {
                EventsHelper.handleEvent(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS,
                        new Object[] { RELATED_DEAL_NUMBER, SIXTEEN }, new HashMap(), env);
            }
        }

        if (getF_IN_Term() != null) {
            if (getF_IN_Term().toString().length() > INT_THREE) {
                EventsHelper.handleEvent(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS,
                        new Object[] { TERM, THREE }, new HashMap(), env);
            }
        }
        if (getF_IN_InterestAmount() != null) {
            if (getF_IN_InterestAmount().toString().length() > INT_FIFTEEN) {
                EventsHelper.handleEvent(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS,
                        new Object[] { INTEREST_AMOUNT, FIFTEEN }, new HashMap(), env);
            }
        }

        if (getF_IN_DealNumber() != null) {
            if (!(dealNumber.toUpperCase().startsWith("FX") || dealNumber.toUpperCase().startsWith("MM"))) {
                if (getF_IN_MessageType().equals("300") || getF_IN_MessageType().equals("900")
                        || getF_IN_MessageType().equals("910")) {
                    setF_OUT_dealNumber("FX" + dealNumber);
                }
                else if (getF_IN_MessageType().equals("320") || getF_IN_MessageType().equals("330")
                        || getF_IN_MessageType().equals("350")) {
                    setF_OUT_dealNumber("MM" + dealNumber);
                }
                else {
                    setF_OUT_dealNumber(dealNumber);
                }
            }
            else {
                setF_OUT_dealNumber(dealNumber);
            }
            if (getF_OUT_dealNumber().length() > INT_SIXTEEN) {
                EventsHelper.handleEvent(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS,
                        new Object[] { DEAL_NUMBER, SIXTEEN }, new HashMap(), env);
            }
        }
        if (getF_IN_TransactionAmount() != null) {
            if (getF_IN_TransactionAmount().toString().length() > INT_FIFTEEN) {
                EventsHelper.handleEvent(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS,
                        new Object[] { TRANSACTION_AMOUNT, FIFTEEN }, new HashMap(), env);
            }
        }
        if (getF_IN_ChargeAmount() != null) {
            if (getF_IN_ChargeAmount().toString().length() > INT_FIFTEEN) {
                EventsHelper.handleEvent(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS,
                        new Object[] { CHARGE_AMOUNT, FIFTEEN }, new HashMap(), env);
            }
        }
        if (getF_IN_DraftNumber() != null) {
            if (getF_IN_DraftNumber().toString().length() > INT_SIXTEEN) {
                EventsHelper.handleEvent(ChannelsEventCodes.E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS,
                        new Object[] { DRAFT_NUMBER, SIXTEEN }, new HashMap(), env);
            }
        }

        if (getF_IN_debtoracctId() != null) {
            String customerCode = DataCenterCommonUtils.readAccount(getF_IN_debtoracctId()).getAccountDetails().getAccountInfo()
                    .getAcctBasicDetails().getCustomerShortDetails().getCustomerId();
            IBOSwtCustomerDetail swtCustDtl = (IBOSwtCustomerDetail) BankFusionThreadLocal.getPersistanceFactory()
                    .findByPrimaryKey(IBOSwtCustomerDetail.BONAME, customerCode, true);
            if (swtCustDtl == null || swtCustDtl.getF_SWTACTIVE().equals("N")) {
                EventsHelper.handleEvent(ChannelsEventCodes.E_INACTIVE_SWIFT_CUSTOMER, new Object[] { customerCode }, new HashMap(),
                        env);
            }

        }

    }

    /**
     * @param msgPreference
     * @return
     */
    @SuppressWarnings("unused")
    private String getMessagePreference(String msgPreference) {
        PaymentSwiftUtils utils = new PaymentSwiftUtils();
      if (StringUtils.isEmpty(msgPreference)) {
            msgPreference = utils.getModuleConfigValue(PaymentSwiftConstants.DEFAULT_SWIFT_MSG_PREFERENCE,
                    PaymentSwiftConstants.CHANNELID_SWIFT);
        }
        return msgPreference;
    }
}
