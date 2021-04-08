/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: SomeEventCodes.java,v.1.0,Aug 21, 2008 10:47:16 AM user
 *
 */
package com.misys.ub.swift.remittance.process;

import com.trapedza.bankfusion.utils.AbstractEventCodes;

/**
 * @author user
 * @date Aug 21, 2008
 * @project Universal Banking
 * @Description:
 */

public class SwiftEventCodes extends AbstractEventCodes {

    public static final String E_CB_CMN_MANDATORY_ENTRY_CB05 = "20020036";
    public static final String E_REMITTANCE_PROCESS_FAILED = "40280129";
    public static final String E_REM_INITITATION_FAILED = "40280130";
    public static final String E_SETTLEMENT_INSTRUCTION_NOT_CONFIGURED_PROPERLY = "40409465";
    public static final String E_AN_ERROR_EXECUTING_THE_PROCESS_EXECUTION_FAILED = "40000364";
    public static final int E_CB_ALLREADY_EXIST_CB = 20020329;
    public static final int E_INVALID_CREDIT_TRANSACTION_CODE = 40410002;
    public static final int E_BT_FEX_EXGRATE_TYPE_NOTFOUND_CB05 = 20020147;
    public static final int E_INVALID_DATA_MP = 20600092;
    public static final int E_CB_CMN_SWIFT_VALIDATION_EVENT_CODE = 40224047;
    public static final int E_CB_CMN_MANDATORY_ENTRY_CB05_INT = 20020036;
    public static final int E_CHARGE_AMT_LESS_THAN_TRANSACTION_AMOUNT_UB = 40218030;
    public static final int E_EXCHG_RATE_NOT_WITHIN_TOL = 40580008;
    public static final int E_SSI_ID_NOT_CORRECT = 40507129;
    public static final int E_SWT_MASK_VALIDATION_UB = 40280131;
    public static final int E_SWT_INVALIDPARTYIDENTIFIER_UB = 40409482;
    public static final int E_SWIFT_CHARGE_MANDTRY_WITH_BEN = 40280132;
    public static final int E_SWIFT_REMITTANCE_5 = 40280133;
    public static final int E_SWIFT_REMITTANCE_6 = 40280134;
    public static final int E_CCY_ENTERED_NOT_ACCT_CCY = 20020324;
    public static final int E_INVALID_DEBIT_TRANS_CODE = 40411001;
    public static final int E_ES_CHARGE_MANDATORY_FOR_BEN = 40280135;
    public static final int REMITTANCE_PROSESSING_REFERRAL = 40430043;
    public static final int E_LEIC_CODE_VALUE_FBE = 40430071;
    public static final int E_LENGTH_OPTIONJ_EXCEEDED = 40280186;
    public static final int E_MIS_TXNCODE_PRE_CHARGE_INVALID = 40280184;
    public static final int E_CONTRA_PSEUDONYM_PRE_CHARGE_NOT_CONFIGURED = 40280187;
    public static final int E_EXCAHNGE_RATE_TYPE_PRE_CHARGE_NOT_CONFIGURED = 40280188;
    public static final int E_DOESNT_EXIST_CB05 = 20000013;
    public static final int E_ACCT_FOR_PSEUDONYM_WITH_ISOCURRCODE_NOT_FOUND = 40507080;
    public static final int E_CHARGE_BEARER_OPTION_NOT_SUPORTED = 40280189;
    public static final int E_EQUIVALENT_AMT_DOES_NOT_MATCH_CALCULATED_VALUE = 40280190;
    public static final String E_TRANSACTION_IS_CANCELLED = "47100007";
    public static final int I_SWT_FROMDATE_GREATER_TODATE_UB15 = 40401040;
    public static final int E_CB_DATE_RANGE_VALIDATION = 20020916;
}
