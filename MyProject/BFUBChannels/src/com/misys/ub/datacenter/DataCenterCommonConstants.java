/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: BranchPowerAccountRefreshAccumulator.java,v.1.1.2.3,Aug 21, 2008 2:58:06 PM KrishnanRR
 * 
 * $Log: BranchPowerAccountRefreshAccumulator.java,v $
 * Revision 1.1.2.4  2008/08/21 23:24:57  krishnanr
 * Updated CVS Header Log
 *
 *
 */
package com.misys.ub.datacenter;

/**
 * @author amanjuna
 *
 */
public class DataCenterCommonConstants {
	/**
	 *  <code> READ_MODULE_CONFIG </code> = CB_CMN_ReadModuleConfiguration_SRV
	 */
	public static final String READ_MODULE_CONFIG = "CB_CMN_ReadModuleConfiguration_SRV";
	
	public static final String RETRIEVE_PSEUDONYM_ACCT_ID = "UB_R_CB_ACC_RetrievePsydnymAcctId_SRV";
	
	public static final String READ_ACCOUNT_DETAILS="UB_R_CB_ACC_ReadAccount_SRV";
	
	public static final String READ_CUSTOMER_DETAILS="CB_PRT_ReadCustomer_SRV";
	
	public static final String READ_KYC_STATUS="UB_CNF_ReadKYCStatus_SRV";
	
	public static final String ACCOUNT_FORMAT_TYPE="ST";
	
	public static final String WHAT_PRODUCT_DEFAULTS="WhatProductDefaults";
	
	public static final String MODULE_ID="SYS";
	
	public static final String PSEDONYM_KEY="TELLERSUSPENSEACCT";
	
	public static final String PSEDONYM_CONTEXT_KEY="TELLERSUSPENSEACCTCONTEXT";
	
	public static final String POSTING_ACTION_CREDIT="C";
	
	public static final String POSTING_ACTION_DEBIT="D";
	
	public static final String ACCOUNT_RIGHTS_INDICATOR_MINUS_ONE="-1";
	
	public static final String ACCOUNT_RIGHTS_INDICATOR_0="0";
	
	public static final String ACCOUNT_RIGHTS_INDICATOR_1="1";
	
	public static final String ACCOUNT_RIGHTS_INDICATOR_2="2";
	
	public static final String ACCOUNT_RIGHTS_INDICATOR_3="3";
	
	public static final String ACCOUNT_RIGHTS_INDICATOR_4="4";
	
	public static final String ACCOUNT_RIGHTS_INDICATOR_5="5";
	
	public static final String ACCOUNT_RIGHTS_INDICATOR_6="6";
	
	public static final String ACCOUNT_RIGHTS_INDICATOR_7="7";
	
	public static final String ACCOUNT_RIGHTS_INDICATOR_8="8";
	
	public static final String ACCOUNT_RIGHTS_INDICATOR_9="9";
	
	public static final String ALLOW_WITHOUT_CHECKING_FOR_DORMANCY="0";
	
	public static final String REJECT_TRANSACTION="1";
	
	public static final String REFER_TO_SUPERVISOR_AND_RECATIVATE="2";
	
	public static final String ALLOW_AND_LOG_EXCEPTION="3";
	
	public static final String DORMANCY_POSTING_ACTION="212";
	
	public static final String ACCOUNT_STATUS="ACCOUNTSTATUS";
	
	public static final String ACCOUNT_RIGHTS_INDICATOR="257";
	
	public static final String PARTY_STATUS="BANKSTATUS";
	
	public static final String BLACK_LISTED="BLOCKED";
	
	public static final String ACCOUNT_CLOSED="CLOSED";
	
	public static final String ACCOUNT_NOT_FOUND="Account not found";
	
    public static final String  E_ACCOUNT_CLOSED = "40000132";
    
    public static final String E_ACCOUNT_STOPPED = "40000133";
    
    public static final String E_PASSWORD_REQD_FOR_POSTING_AND_ENQUIRY = "40112171";
    
    public static final String E_ACCOUNT_STOPPED_ENQUIRY_ALLOWED_WITH_PASSWORD = "40007317";
    
    public static final String E_ACCOUNT_PASSWORD_REQUIRED_FOR_POSTING = "40007319";
    
    public static final String E_ACCOUNT_STOPPED_NO_POSTING_IS_ALLOWED = "40007321";
    
    public static final String E_ACCOUNT_NO_DEBITS_ALLOWED = "40007322";
    
    public static final String E_ACCOUNT_NO_CREDITS_ALLOWED = "40007324";
    
    public static final String E_ACCOUNT_IS_DORMANT="70009703";
    
    public static final String E_CUSTOMER_IS_BLACKLISTED="40000100";
    
    public static final String POSTING_MESSAGE_SERVICE="UB_TXN_PostingEngine_SRV";
    
    public static final String E_ACCOUNT_NOT_FOUND="20020000";
    
    public static final String R_ACCOUNT_REFERRAL_REQUIRED_FOR_POSTING="40007319";
    
    public static final String R_ACCOUNT_NO_DEBITS_REFERRAL_REQUIRED_OVERRIDING="40007323";
    
    public static final String R_ACCOUNT_NO_CREDITS_REFERRAL_REQUIRED_OVERRIDING="40007325";
    
    public static final String R_REFEERAL_REQ_POSTING_NO_REFERRAL_ENQUIRY="40007327";
    
	public static final String CHEQUE_STATUS = "ENQUIRECHEQUESTATUS";
	
	public static final String CHEQUE_STOPPED = "STOPPED";
	
	public static final String CHEQUE_CANCELLED = "CANCELLED";
	
	public static final String CHEQUE_PRESENTED = "PRESENTED";
	
	public static final String CHEQUE_HAS_NOT_BEEN_ISSUED = "NOTISSUED";
	
	public static final String CHEQUEBOOK_ISSUE_STATUS = "CHEQUEBOOK_ISSUE_STATUS";
	
	public static final String CHEQUE_DRAFT_NOT_WITHIN_A_VALID_RANGE = "NOTWITHINRANGE";
	
	public static final String SWT_ADDRESS_TYPE = "SWIFT";
    
    
    
	
	
	
	
	
	
	
	
	
	

}
