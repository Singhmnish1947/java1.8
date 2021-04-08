package com.misys.ub.swift.tellerRemittance.utils;

public class RemittanceConstants {
    public static final String MODULE_CBS = "CBS";
    public static final String FEX_MODULE_ID = "FEX";
    public static final String PSEUDONYM_NOSTRO_ACCOUNT_GPP = "PSEUDONYM_NOSTRO_ACCOUNT_GPP";
    public static final String CASH_FUNDING_MODE = "CASH";
    public static final String PSEUDONYM_INTERNAL_CASH_ACCOUNT_GPP = "PSEUDONYM_INTERNAL_CASH_ACCOUNT_GPP";
    public static final String PSEUDONYM_CHARGE_ACCOUNT_GPP = "PSEUDONYM_CHARGE_ACCOUNT_GPP";
    public static final String PSEUDONYM_TAX_ACCOUNT_GPP = "PSEUDONYM_TAX_ACCOUNT_GPP";
    public static final String MIS_TXN_CODE_CHARGE_TAX_GPP = "MIS_TXN_CODE_CHARGE_TAX_GPP";
    public static final String GPP_CONNECTED = "GPP_CONNECTED";
    public static final String MSGPREFERENCE_GC = "MSGPREFERENCE";
    public static final String TELREM_PAYMETHOD_GC = "TELREM_PAYMETHOD";
    public static final String TELREM_REMIT_STATUS_GC = "TELREM_REMITSTATUS";
    public static final String TELREM_FUNDMODE_GC = "TELREM_FUNDMODE";
    public static final String DOCTYPE_GC = "DOCTYPE";
    public static final String SWTBNKOPERATIONCODE_GC = "SWTBNKOPERATIONCODE";
    public static final String ORDERINGCUSTOMER_GC = "OrderingCustomer";
    public static final String SWTBNKINSTRUCTIONCD_GC = "SWTBNKINSTRUCTIONCD";
    public static final String BENEFICIARYCUSTOMER_GC = "BeneficiaryCustomer";
    public static final String NCC_GC = "NCC";
    public static final String PURPOSEOFREMITTANCE_GC = "PURPOSEOFREMITTANCE";
    public static final String CHARGE_CODE_TYPE_GC = "234";
    public static final String BRANCH_TELLER_CHANNEL_ID = "BranchTeller";
    // TODO:change this
    public static final String ORDERING_CUST_PARTY_IDENT_GC = "234";
    public static final String ORDERING_CUST_COUNTRY_CODE_GC = "234";

    public static final String REJECTED_STATUS = "RJCT";
    public static final String DUPLICATE_STATUS = "DUPL";

    // eventcodes
    public static final int E_FBE_GPP_TRANSACTION_REJECTED = 40430076;
    public static final int E_BIC_NOT_FOUND = 40430061;
    public static final int E_CB_CMN_MANDATORY_ENTRY_CB05=20020036;
    public static final int E_FBE_GPP_TRANSACTION_DUPLICATE = 40430077;
	public static final int E_GPP_API_SCHEMA_VALIDATION_ERROR = 20020062;
	public static final int E_FBE_GPP_API_REQ_ERROR = 40430075;
}
