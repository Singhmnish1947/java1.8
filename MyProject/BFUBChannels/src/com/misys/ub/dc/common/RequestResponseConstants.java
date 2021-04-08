/* ********************************************************************************
 *  Copyright(c)2018  Finastra Financial Software Solutions. All Rights Reserved.
 *
 *  This software is the proprietary information of Finastra Financial Software Solutions.
 *  Use is subject to license terms.
 * ********************************************************************************
 * 
 */

package com.misys.ub.dc.common;

/**
 * Class file containing all the constants used while sending the request and response to Digital
 * Channels
 *
 * @author Nisha Kumari
 */
public class RequestResponseConstants {

    public static final String STATUS_SUCCESS = "S";

    public static final String STATUS_FAILURE = "E";

    public static final String PARTY_ID = "partyId";

    public static final String LINE_OF_BUSINESS = "lineOfBusiness";

    public static final String ENABLE_LOB = "enableLOB";

    public static final String MESSAGE_ID = "msgId";

    public static final String MESSAGE_TYPE = "msgType";

    public static final String ORIG_CONTEXT_ID = "origCtxtId";

    public static final String REASON_CODE = "reasonCode";

    public static final String STATUS = "status";

    public static final String DOCUMENT_NAME = "DocumentName";

    public static final String DOCUMENT_CONTENTS = "DocumentContents";

    public static final String REPEATABLE_BLOCKS = "REPEATABLE_BLOCKS";

    public static final String DOCUMENTS = "DOCUMENTS";

    public static final String PARTY_DOCUMENT_ID = "PT_PFN_PartyDocumentData#IMAGEID";

    public static final String BRANCH_CODE = "BranchCode";

    public static final String ACCOUNT_FIELDS = "ACCOUNT_FIELDS";

    public static final String TD_ACCOUNT_FIELDS = "TD_ACCOUNT_FIELDS";

    public static final String TERM_DEPOSIT_REQ = "openFixedDepositRq";

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String TD_ACCOUNT_NUMBER = "accountNumber";

    public static final String USER_TYPE_CONTRACT = "contracting";

    public static final String PROSPECT_CUSTOMER = "prospect";

    public static final String EXISTING_CUSTOMER = "existing";

    public static final String PRODUCT_GROUP = "productGroup";

    public static final String TERM_DEPOSIT_PRODUCT_GROUP = "TD";
    
    public static final String CA_PRODUCT_GROUP = "CA";
    
    public static final String SA_PRODUCT_GROUP = "SA";

    public static final String USER_TYPE = "userType";

    public static final String CASA_ACCOUNT_FIELDS = "CASA_ACCOUNT_FIELDS";

    public static final String CREATE_ACCOUNT_REQ = "createAccountRq";

    public static final String CHANNEL_FOR_CREATE_ACCOUNT = "IBI";

    public static final String CURRENCY = "_currency";

    public static final String MESSAGE_TYPE_VALUE = "PARTY_ONBOARD_ACCOUNT_OPEN_RES";

    public static final String ACCOUNT_NAME_JSON_KEY = "accountName";

    public static final String ACCOUNT_ID_JSON_KEY = "accountId";

    public static final String CASA_CUSTOMERNO = "_customerNo";
    
    public static final String TD_CUSTOMERNO = "_customerId";

    public static final String SUB_PRODUCT_ID = "_subProductId";
    
    public static final String ACC_OPENING_DATE = "_accountOpenDate";
    
    public static final String ACC_OPENING_DATE_FORMAT = "MMM dd, YYYY";

    public static final String PARTY_FIRST_NAME = "PT_PFN_PersonNames#FIRSTNAME";

    public static final String PARTY_LAST_NAME = "PT_PFN_PersonNames#LASTNAME";

    public static final String POSTAL_CODE = "PT_PFN_Address#POSTALCODE";

    public static final String TAX_ID = "PT_PFN_PartyTaxDetails#TAXIDNUMBER";

    public static final String PARTY_DATE_OF_BIRTH = "PT_PFN_PersonalDetails#DATEOFBIRTH";

    public static final String NATIONAL_ID = "PT_PFN_PersonalDetails#NATIONALID";

    public static final String CONTACT_METHOD = "PT_PFN_PartyContactDetails#CONTACTMETHOD";

    public static final String CONTACT_VALUE = "PT_PFN_PartyContactDetails#CONTACTVALUE";

    public static final String MESSAGE_TYPE_PARTY_ONBOARD_ACC_OPEN = "PARTY_ONBOARD_ACCOUNT_OPEN_RES";

    public static final String PARTY_DEFAULT_ADDRESS = "PT_PFN_AddressLink#ISDEAFULTADDRESS";

    public static final String PARTY_FIELDS = "PARTY_FIELDS";
    
    public static final String CASA_ACCOUNT_TYPE = "CASA";
    
    public static final String TD_ACCOUNT_TYPE = "TD";

}
