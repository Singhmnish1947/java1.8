package com.misys.ub.payment.swift.utils;

public class PaymentSwiftConstants {
    private PaymentSwiftConstants() {

    }

    /**
     * @param args
     */
    /**
     * <code> CB_ACC_ADD_ACCOUNT_BLOCKS </code> = CB_ACC_AddAccountBlocks_SRV
     */
    public static final String CB_ACC_ADD_ACCOUNT_BLOCKS = "CB_ACC_AddAccountBlocks_SRV";
    public static final String GetAccountDetailsMF = "UB_CNF_GetAccountDetails_SRV";
    public static final String GetAvailableBalanceMF = "UB_CMN_GetAvailableBalance_SRV";
    public static final String CB_ACC_DEL_ACCOUNT_BLOCKS = "CB_ACC_DeleteAccountBlocks_SRV";
    public static final String CHANNELID_IBI = "IBI";
    public static final String CHANNELID_CCI = "CCI";
    public static final String CHANNELID_TELLER = "BranchTeller";
    public static final String CHANNELID_SWIFT = "SWIFT";
    public static final String FOREIGNPYMTCR = "FOREIGN_MISCR";
    public static final String FOREIGNPYMT = "FOREIGN_MISDR";
    public static final String FOREIGNPYMTCR_CCI = "FOREIGN_MISC";
    public static final String FOREIGNPYMTDR_CCI = "FOREIGN_MISD";
    public static final String READ_MODULE_CONFIGURATION = "CB_CMN_ReadModuleConfiguration_SRV";
    public static final String MODULE_VALUE_DC = "OUTGOING_DC_NON_STP";
    public static final String MODULE_VALUE_CC = "OUTGOING_CC_NON_STP";
    public static final String MODULE_VALUE_TELLER = "OUTGOING_TELLER_NON_STP";
    public static final String RULECATEGORY_IBI = "SWTOIBI";
    public static final String RULECATEGORY_CCI = "SWTOCCI";
    public static final String RULECATEGORY_TELLER = "SWTOTELLER";
    public static final String REMITTER_WAIT = "W";
    public static final String FAIL = "F";
    public static final String PROCESSED = "P";
    public static final String RECEIVED = "R";
    public static final String CANCELLED = "C";
    public static final String INWARD = "I";
    public static final String OUTWARD = "O";
    public static final String YES = "Y";
    public static final String NO = "N";
    public static final String OK = "OK";
    public static final String IPAY = "IPAY";
    public static final String FAILURE = "FAILURE";
    public static final String SUCCESS = "S";
    public static final String ERROR = "E";
    public static final String REMITTACNCE_OUTWARD = "OUTW";
    public static final String BackOfficeAccountPostingMF = "CB_TXN_BackOfficeAccountPosting_SRV";
    public static final String OVERALL_SUCCESS_STATUS = "S";
    public static final String ERROR_STATUS = "E";
    public static final String MODULE_ID = "SWIFT";
    public static final String PSEDONYM_KEY = "PSEUDONYM_TELLER_NON_STP";
    public static final String PSEDONYM_DEFAULT_BRANCHID = "DEFAULT_BRANCHID_NON_STP";
    public static final String PSEDONYM_CONTEXT = "BRANCH";
    public static final String CURRENCY_PSEDONYM_CONTEXT = "CURRENCY";
    public static final String CHARGE_CODE_OUR = "OUR";
    public static final String CHARGE_CODE_BEN = "BEN";
    public static final String CHARGE_CODE_SHA = "SHA";
    public static final String MT_103_MESSAGE_TYPE = "MT103";
    public static final String DEBIT = "D";
    public static final String CREDIT = "C";
    public static final String OCV_CHARGEAMOUNT_IN_FUND_ACC_CURRENCY = "CHARGEAMOUNT_IN_FUND_ACC_CURRENCY";
    public static final String OCV_TAXAMOUNT_IN_FUND_ACC_CURRENCY = "TAXAMOUNT_IN_FUND_ACC_CURRENCY";
    public static final String OCV_FUNDINGACCOUNTID = "FUNDINGACCOUNT";
    public static final String OCV_FUND_ACC_CURRENCY = "ACC_CURRENCY";

    public static final String MULTIPLY_CONSTANT = "M";
    public static final String DIVIDE_CONSTANT = "D";
    public static final String CALCULATE_EXCHANGE_RATE_RQ = "CalcExchangeRateRq";

    public static final String CALCULATE_EXCHANGE_RATE_RS = "CalcExchangeRateRs";
    public static final String CUSTOMER_MARGIN_APPLIED_FLAG = "IsCustMarginApplied";
    public static final String ALLOW_CURRENCY_CODE_EMPTY = "AllowCurrCodeEmpty";
    public static final String CUSTOMER_ID = "CustomerID";

    public static final String EVT_CANCELLATION_IPAY = "40430049";
    public static final int REMITTANCE_ALREADY_CANCELLED = 40410044;
    public static final String EVENTCODE_FLD = "EventCode";
    public static final String EVENTCODEDESC_FLD = "CANCELLED_IPAY";
    public static final String EVENTHOSTTRANSID_FLD = "HostTransactionId";
    public static final String EVENTCHANNEL_FLD = "OrigChannelId";
    public static final String EVENTCHANNELREF_FLD = "ChannelRef";
    public static final String EVENTREMITTANCEID = "RemittanceId";
    public static final String EVT_POSTING_FAILED = "20020870";
    public static final int E_THE_EXCHANGE_RATE_TO_FOR_THE_RATE = 40580022;
    public static final int E_RECEIVER_CHARGE_71G_IS_NOT_EQUAL_CHARGE_AMOUNT = 40009323;
    public static final String NO_RECORD_EXISTS = "40180171";
    public static final String FEX_MODULE_ID = "FEX";
    public static final String NOSTRO_PSEDONYM_KEY = "DEFAULT_NOSTRO";
    public static final String CHARGE_CODE_USED_FOR_OUR_71G_CHARGE_POSTING = "CHARGE_CODE_For_71G_OUR_CHARGES";
    public static final String BRANCH_CONTEXT = "CONTEXTFORFOREXACCOUNTS";
    public static final int E_NO_DETAILS_FOUND_CBS = 20020771;
    public static final String MT103 = "103";
    public static final String MT202 = "202";
    public static final String MT205 = "205";
    public static final String CHANNEL_UXP = "UXP";
    public static final String EVENTPAYMENTREF_FLD = "PaymentReference";
    public static final String UB_SWT_SETTLEMENTINSTRUCTIONFIRSTVALIDATION_SRV = "UB_SWT_SettlementInstructionFirstValidation_SRV";
    public static final String UB_SWT_SETTLEMENTINSTRUCTIONSECONDVALIDATION_SRV = "UB_SWT_SettlementInstructionSecondValidation_SRV";
    public static final String UB_SWT_SETTLEMENTINSTRUCTIONTHIRDVALIDATION_SRV = "UB_SWT_SettlementInstructionThirdValidation_SRV";
    public static final String UB_SWT_SINAMEANDADDRESSVALIDATION_SRV = "UB_SWT_SINameAndAddressValidation_SRV";
    public static final String UB_SWT_APPENDNCCCODES_SRV = "UB_SWT_AppendNCCCodes_SRV";
    public static final String UB_SWT_REMITTACEACCOUNTVALIDATE_SRV = "UB_SWT_RemittaceAccountValidate_SRV";
    public static final String CB_TXN_IDENTIFYNOSTROACCOUNT_SRV = "CB_TXN_IdentifyNostroAccount_SRV";
    public static final String UB_SWT_VALIDATE_NCC_CODES = "UB_SWT_ValidateNCCCodes_SRV";
    public static final String DEFAULT_SWIFT_MSG_PREFERENCE = "DEFAULT_SWIFT_MSG_PREFERENCE";
    public static final String PAYMENT_PREFERENCE_SERIAL = "SERIAL";
    public static final String SUSPENSE_ACCT_REMITTANCE = "SUSP_ACCT_REMITTANCE";
    public static final String ROOT_UB_TYPES_INTERFACES = "http://www.misys.com/ub/types/interfaces";
    public static final String ACCOUNT_WITH_INSTITUTION_OPTION = "accountWithInstitutionOption";
    public static final String ACCOUNT_WITH_INSTITUTION = "accountWithInstitution";
    public static final String RECEIVERS_CORRESPONDENT = "receiversCorrespondent";
    public static final String RECEIVERS_CORRESPONDENT_OPTION = "receiversCorrespondentOption";
    public static final String XMLNS = "xmlns";
    public static final String SENDERS_CORRESPONDENT_OPTION = "sendersCorrespondentOption";
    public static final String SENDERS_CORRESPONDENT = "sendersCorrespondent";
    public static final String ACCOUNT_WITH_INST_OPTION = "accountWithInstOption";
	 //SWIFT2019
    public static final String PARTY_IDENT_ABIC = "ABIC";
    public static final String PARTY_IDENT_CLRC = "CLRC";
    public static final String PARTY_IDENT_LEIC = "LEIC";
    public static final String PARTY_IDENT_ACCT = "ACCT";
    public static final String PARTY_IDENT_ADD1 = "ADD1";
    public static final String PARTY_IDENT_ADD2 = "ADD2";
    public static final String PARTY_IDENT_CITY = "CITY";
    public static final String PARTY_IDENT_NAME = "NAME";
    public static final String PARTY_IDENT_SVBY = "SVBY";
    public static final String PARTY_IDENT_TXID = "TXID";
    public static final String PARTY_OTHERS = "PARTY_OTHERS";
    public static final String MT_300 = "300";
    public static final String ACCT_CODE = "/ACCT/";
    public static final String NAME_CODE = "/NAME/";
    public static final String CITY_CODE = "/CITY/";
    public static final String ADD1_CODE = "/ADD1/";
    public static final String ADD2_CODE = "/ADD2/";
    
    public static final String BORNE_BY_CREDITOR = "BorneByCreditor";
    public static final String BORNE_BY_DEBTOR = "BorneByDebtor";
    public static final String FOLLOW_SERVICE_LEVEL = "FollowingServiceLevel";
    public static final String SHARED = "Shared";
    public static final String PAYMENT_INFO_ID = "Payment Information Id";
    public static final String DEBTOR_AGENT = "Debitor Agent";
    public static final String DEBTOR_ACCOUNT_ID = "Debitor Account Id";
    public static final String DEBTOR_ACCOUNT_NAME = "Debitor Account Name";
    public static final String CREDITOR_ACCOUNT_ID = "Creditor Account Id";
    public static final String INSTRUCTION_IDENTIFICATION = "Instruction Identification";
    public static final String INTERNATIONAL_PAYMENT_ID = "International Payment Id";
    public static final String MODULE_VALUE_PRE_CALCULATE_CHARGE = "MISTransactionCode_PRE_CALCULATE_CHARGE";
    /**
     * Default Date Format
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String SHOW_SENDER_CHARGE_SHA = "SHOWSENDERCHARGESHA";
}
