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
package com.misys.ub.channels.events;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.utils.AbstractEventCodes;

/**
 * @author user
 * @date Aug 21, 2008
 * @project Universal Banking
 * @Description:
 */

public class ChannelsEventCodes extends AbstractEventCodes {
   	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

    transient final static Log logger = LogFactory.getLog(ChannelsEventCodes.class.getName());

    private static final int SUB_SYSTEM_ID = 404;

    /**
     * Private constructor
     */
    public ChannelsEventCodes() {
        subsystemId = SUB_SYSTEM_ID;
        baseName = CommonConstants.EMPTY_STRING;
    }

    /**
     * <code>E_____FORMAT_IS_INCORRECT_SHOULD_CONTAIN_TWO_FORWARD_SLASH</code> = {0} format is incorrect:Should contain two forward slash
     */

    public static final int E_INCORRECT_FORMAT = 40409442;

    /**
     * <code>E_PARTY_IDENTIFIER_TEXT_SHOULD_BE_BLANK__WHEN_PARTY_IDENTIFIER_COMBO_IS_BLANK__</code> = Party Identifier Text should be Blank  When Party Identifier Combo is Blank .
     */

    public static final int E_INCORRECT_PARTY_ID_TEXT = 40409441;

    /**
     * <code>E_PAY_OR_RECEIVE_SHOULD_BE_BLANK_FOR_THE_MESSAGE_TYPE_____</code> = Pay or Receive should be Blank for the Message Type {0}.
     */

    public static final int E_PAY_OR_RECEIVE_NOT_BLANK_FOR_THE_MESSAGE_TYPE = 40409440;


    /**
     * <code>E_COULD_NOT_ESTABLISH_CONNECTION_WITH_THE_HOST_</code> = Could Not Establish Connection With The Host.
     */
    public static final int E_COULD_NOT_ESTABLISH_CONNECTION_WITH_THE_HOST = 40407549;

    /**
     * <code>E_FILE_NAME_SHOULD_NOT_BE_NULL____</code> = File name Should not be null {0}
     */
    public static final int E_FILE_NAME_SHOULD_NOT_BE_NULL = 40407548;

    /**
     * <code>E_INVALID_DEVICE_ID____</code> = Invalid Device Id {0}
     */
    public static final int E_INVALID_DEVICE_ID = 40407547;

    /**
     * <code>E_INVALID_DESTINATION_ACCOUNT____</code> = Invalid Destination Account {0}
     */
    public static final int E_INVALID_DESTINATION_ACCOUNT = 40407546;
    
    /**
     * <code>E_INVALID_DESTINATION_CURRENCY____</code> = Invalid Destination Currency {0}
     */
    public static final int E_INVALID_DESTINATION_CURRENCY = 40407544;

    /**
     * <code>E_ERROR_ON_RETRIEVAL_OF_VALUES_FROM_ACCOUNTDETAILS_FOR_ACCOUNTID____</code> = Error on retrieval of values from AccountDetails for accountId {0}
     */

    public static final int E_RETRIEVAL_ERROR_IN_ACCOUNTDETAILS_FOR_ACCOUNTID = 40407543;

    /**
     * <code>E_ERROR_ON_UPDATING_ATMACCOUNTDETAILS_FOR_ACCOUNTID____</code> = Error on updating ATMAccountDetails for accountId {0}
     */

    public static final int E_ERROR_ON_UPDATING_ATM_ACCOUNTDETAILS = 40407542;

    /**
     * <code>E_ERROR_ON_RETRIEVAL_OF_AVAILABLE_BALANCE_AND_BOOK_BALANCE</code> = Error on retrieval of available balance and book balance
     */
    public static final int E_AVAILABLE_AND_BOOK_BALANCE_RETRIEVAL_ERROR = 40407541;

    /**
     * <code>E_____IS_NOT_A_ACTIVE_SWIFT_CUSTOMER_</code> = {0} is not a Active SWIFT Customer.
     */
    public static final int E_INACTIVE_SWIFT_CUSTOMER = 40409439;

    /**
     * <code>E_ERROR_IN_PROCESSING_ATM_MESSAGE____</code> = Error in processing ATM Message {0}
     */
    public static final int E_ERROR_IN_PROCESSING_ATM_MESSAGE = 40407540;

    /**
     * <code>E_____IS_NOT_A_SWIFT_CURRENCY_</code> = {0} is not a SWIFT Currency.
     */
    public static final int E_IS_NOT_A_SWIFT_CURRENCY = 40409438;

    /**
     * <code>E_____IS_NOT_A_ACTIVE_CURRENCY_</code> = {0} is not a Active Currency.
     */
    public static final int E_INACTIVE_CURRENCY = 40409437;

    /**
     * <code>E_____</code> = ={0}
     */
    public static final int E_PARTY_ADDRESS_INVALID = 40409436;

    /**
     * <code>E_IN_ADDRESS_THE_FIRST_CHARACTER_SHOULD_BE_INTEGER_</code> = In Address The First Character Should be Integer.
     */

    public static final int E_FIRST_CHARACTER_NOT_INTEGER_IN_ADDRESS = 40409435;

    /**
     * <code>E_THE_ISO_CURRENCY_FOR_ACCOUNT_____IS_NOT_____</code> = The ISO Currency For Account {0} is not {1}.
     */
    public static final int E_NOT_ISO_CURRENCY_FOR_ACCOUNT = 40409434;

    /**
     * <code>E_IDENTIFIERCODE_DOES_NOT_EXIST_</code> = Identifiercode does not exist.
     */
    public static final int E_IDENTIFIERCODE_DOES_NOT_EXIST = 40409433;

    /**
     * <code>E_DEAL_NUMBER_____IS_NOT_FOUND_</code> = Deal Number {0} is not found.
     */
    public static final int E_DEAL_NUMBER_IS_NOT_FOUND = 40409432;

    /**
     * <code>E_IDENTIFIER_CODE_WITH_____ALREADY_EXIST__</code> = Identifier Code with {0} Already Exist .
     */
    public static final int E_IDENTIFIER_CODE_WITH_ALREADY_EXIST = 40409431;

    /**
     * <code>E_IDENTIFIER_CODE_SHOULD_BE___OR____CHARACTERS_LENGTH__</code> = Identifier Code Should be 8 or 11 Characters Length .
     */

    public static final int E_CHARACTERS_IN_IDENTIFIER_CODE_IS_NOT_8_OR_11 = 40409430;

    /**
     * <code>E_CARD_AND_ACCT_NUM_UNMAPPED_NOTPOSTED_FORCE_POST</code> = Card Number {0} and Account Number {1} not mapped,force Post not posted
     */

    public static final int E_CARD_AND_ACCT_NUM_UNMAPPED_NOTPOSTED_FORCE_POST = 40407539;

    /**
     * <code>E_ERROR_ON_INSERTION_OF_RECORD_TO_ACTIVITY_LOG_TABLE</code> = Error on insertion of record to Activity log table
     */

    public static final int E_ACTIVITY_LOG_TABLE_INSERTION_ERROR = 40407538;

    /**
     * <code>E_CARD_NUMBER_____AND_ACCOUNT_NUMBER_____NOT_MAPPED</code> = Card Number {0} and Account Number {1} not mapped
     */

    public static final int E_CARD_NUMBER_AND_ACCOUNT_NUMBER_NOT_MAPPED = 40407537;
    
    
    /**
     * <code>W_CARD_AND _ACCOUNT_NUMBER_NOT_MAPPED_AND_POSTED_TO_SUSPENSE_ACCOUNT</code>=Card Number {0} and Account Number {1} not mapped,Posted to Suspense Account
     */
    public static final int W_CARD_NUM_ACC_NUM_NOT_MAP_POST_TO_SUSPENSE_ACC = 40400069;

    /**
     * <code>E_ATM_CONFIGURATION_DETAILS_COULD_NOT_BE_RETRIEVED</code> = ATM Configuration details could not be retrieved
     */
    public static final int E_ATM_CONFIGURATION_DETAILS_COULD_NOT_BE_RETRIEVED = 40407536;

    /**
     * <code>E_INVALID_FORCE_POST_VALUE_____FOR_LOCAL_FINANCIAL_MESSAGE</code> = Invalid Force Post value {0} for local financial message
     */

    public static final int E_INVALID_FORCE_POST_VALUE_FOR_LOCAL_FIN_MESG = 40407535;

    /**
     * <code>E_______LENGTH_EXCEEDS__MAXIMUM_NUMBER_OF______CHARACTERS__</code> = {0} : Length Exceeds  Maximum Number Of  {1} Characters .
     */

    public static final int E_LENGTH_EXCEEDS_MAXIMUM_NUMBER_OF_CHARACTERS = 40409429;

    /**
     * <code>E_MODULE_LEVEL_FALG_FOR_____IS_SET_AS_NO_NOT_AUTHORIZED_TO_CHANGE_AT_CUSTOMER_LEVEL_</code> = Module level falg for {0} is set as No,Not authorized to change at customer level.
     */

    public static final int E_DOESNOT_AUTHORIZE_TO_CHANGE_AT_CUST_LEVEL = 40409428;

    /**
     * <code>E_PARAM_VALUE_MUST_BE_EITHER__Y__OR__N__</code> = Param value must be either "Y" or "N".
     */
    public static final int E_PARAM_VALUE_MUST_BE_EITHER_Y_OR_N = 40409427;

    /**
     * <code>E_PARAM_VALUE_SHOULD_BE_BETWEEN___TO____</code> = Param value should be between 0 to 99.
     */
    public static final int E_PARAM_VALUE_NOT_IN_RANGE = 40409426;

    /**
     * <code>E_THIS_RECORD_WILL_NOT_BE_STORED__UNLESS_THERE_IS_A_VALID_FREQUENCY_</code> = This record will not be stored  unless there is a valid frequency.
     */

    public static final int E_RECORD_WILL_NOT_BE_STORED_TILL_VALID_FREQUENCY = 40409425;

    /**
     * <code>E_STATEMENT_DAY_FOR_____SHOULD_BE_BETWEEN___AND____</code> = Statement day for {0} Should be between 1 and 31.
     */

    public static final int E_STATEMENT_DAY_FOR_SHOULD_FALL_IN_RANGE = 40409424;

    /**
     * <code>E_STATEMENT_DAY_FOR_WEEKLY_SHOULD_BE_BETWEEN___AND___</code> = Statement day for Weekly Should be between 1 and 7.
     */

    public static final int E_STATEMENT_DAY_FOR_WEEKLY_SHOULD_FALL_IN_RANGE = 40409423;

    /**
     * <code>E_STATEMENT_DAY_FOR_DAILY_SHOULD_BE___</code> = Statement day for Daily Should be 0.
     */
    public static final int E_STATEMENT_DAY_FOR_DAILY_SHOULD_BE = 40409422;

    /**
     * <code>E_____NOT_FOUND_AS_FILE_</code> = {0} not found as file.
     */
    public static final int E_NOT_FOUND_AS_FILE = 40409421;

    /**
     * <code>E_INVALID_IDENTIFIERCODE_FILE_FORMAT</code> = Invalid IdentifierCode File Format
     */
    public static final int E_INVALID_IDENTIFIERCODE_FILE_FORMAT = 40409420;

    /**
     * <code>E_TRANSACTION_TYPE________HAS_NUMERIC_CODE________NUMERIC_CODE_FOR_THE_TRANSACTION_TYPE_SELECTED_MUST_BE_OF___DIGITS_LENGTH_</code> = Transaction type - {0}, has Numeric code - {1}. Numeric code for the transaction type selected must be of 2 digits length.
     */

    public static final int E_INVALID_NUMERIC_CODE_FOR_THE_TRANSACTION_TYPE = 40409022;

    /**
     * <code>E_EXCEPTION_OCCURED______</code> = Exception occured : {0}
     */
    public static final int E_EXCEPTION_OCCURED = 40409021;

    /**
     * <code>E_EFT_FILE_ALREADY_EXISTS__PLEASE_RENAME_IT_OR_MOVE_IT_TO_ANOTHER_DIRECTORY_</code> = EFT file already exists. Please rename it or move it to another directory.
     */

    public static final int E_EFT_FILE_ALREADY_EXISTS = 40409020;

    /**
     * <code>E_ATM_CONFIGURATION_FILE_NOT_FOUND</code> = ATM Configuration file not found
     */
    public static final int E_ATM_CONFIGURATION_FILE_NOT_FOUND = 40407522;

    /**
     * <code>E_COULD_NOT_ESTABLISH_CONNECTION_WITH_FIRCOSOFT_SERVICE_</code> = Could not establish connection with fircosoft Service.
     */

    public static final int E_COULD_NOT_ESTABLISH_CONNECTION_WITH_FIRCOSOFT = 40407328;

    /**
     * <code>E_ERROR_WHILE_PUBLISHING_THE_MESSAGE</code> = Error while publishing the message
     */
    public static final int E_ERROR_WHILE_PUBLISHING_THE_MESSAGE = 40409419;

    /**
     * <code>E_INVALID_SETTLEMENT_ACCOUNT____</code> = Invalid settlement account {0}
     */
    public static final int E_INVALID_SETTLEMENT_ACCOUNT = 40407520;

    /**
     * <code>E_SWIFT_PROPERTIES_FILE_NOT_FOUND</code> = SWIFT.PROPERTIES file not found
     */
    public static final int E_SWIFT_PROPERTIES_FILE_NOT_FOUND = 40409418;

    /**
     * <code>E_NO_ACCOUNT_EXIST_WITHIN_THE_SPECIFIED_RANGE</code> = No Account exist within the specified range
     */
    public static final int E_NO_ACCOUNT_EXIST_WITHIN_THE_SPECIFIED_RANGE = 40409417;

    /**
     * <code>E_MASK_FORMAT_____SUPPORTS_ONLY_A_FIXED_ALLOWED_FORMAT</code> = Mask format {0} supports only a fixed allowed format
     */

    public static final int E_MASK_FORMAT_NOT_SUPPORTED = 40409416;

    /**
     * <code>E_IDENTIFIER_CODE_NOT_AVAILABLE_WITH_RESPECT_ALTERNATE_BICCODE_____</code> = Identifier Code not available with respect alternate BicCode {0}.
     */

    public static final int E_IDENTIFIER_CODE_NOT_AVAILABLE_RESPECT_TO_BICCODE = 40409415;

    /**
     * <code>E_NO_FILE_TO_READ_WITH__SWT_EXTENSION</code> = No file to read with .SWT extension
     */
    public static final int E_NO_FILE_TO_READ_WITH_SWT_EXTENSION = 40409414;

    /**
     * <code>E_SWIFT_DIRECTORY_IS_EMPTY_</code> = Swift directory is Empty.
     */
    public static final int E_SWIFT_DIRECTORY_IS_EMPTY = 40409413;

    /**
     * <code>E_MESSAGE_TYPE_____CANNOT_BE_FOUND_</code> = Message Type {0} cannot be found.
     */
    public static final int E_MESSAGE_TYPE_CANNOT_BE_FOUND = 40409412;

    /**
     * <code>E_ADDRESS_SHOULD_BE_IN_INCREASING_ORDER_</code> = Address should be in increasing order.
     */
    public static final int E_ADDRESS_SHOULD_BE_IN_INCREASING_ORDER = 40409411;

    /**
     * <code>E_ADDRESS___OR___CANNOT_EXIST_WITHOUT_ADDRESS___OR___RESPECTIVELY_</code> = Address 3 OR 5 cannot exist without address 2 or 4 respectively.
     */

    public static final int E_ADDRESS_3R5_CANNOT_EXIST_WITHOUT_ADDR_2R4_RESPLY = 40409410;

    /**
     * <code>E_ERROR_OCCURED_WHILE_CREATING_EFT_FILE______</code> = Error occured while creating EFT File : {0}
     */
    public static final int E_ERROR_OCCURED_WHILE_CREATING_EFT_FILE = 40409019;



    /**
     * <code>E_DESTINATION_BRANCH_SORT_CODE_MUST_BE_ENTERED_</code> = Destination Branch Sort Code must be entered.
     */
    public static final int E_DESTINATION_BRANCH_SORT_CODE_MUST_BE_ENTERED = 40409016;

    /**
     * <code>E_EXCEPTION_OCCURED_WHILE_TRYING_TO_FETCH_DETAILS_OF_THE_GIVEN_BRANCH_SORT_CODE________</code> = Exception occured while trying to fetch details of the given Branch Sort Code : "{0}"
     */

    public static final int E_ERROR_WHILE_FETCHING_DETAILS_OF_BRANCH_SORT_CODE = 40409015;

    /**
     * <code>E_IDENTIFIER_CODE_IS_NOT_SPECIFIED_FOR_THE_GIVEN_BRANCH_SORT_CODE__________PROCESSING_IS_STOPPED_</code> = Identifier Code is not specified for the given Branch Sort Code : "{0}". Processing is stopped.
     */

    public static final int E_IDENTIFIER_CODE_UNSPECIFIED_FOR_BRANCH_SORT_CODE = 40409014;

    /**
     * <code>E_BRANCH_OBJECT_IS_NOT_FOUND_FOR_THE_GIVEN_BRANCH_SORT_CODE__________PROCESSING_IS_STOPPED_</code> = Branch object is not found for the given Branch Sort Code : "{0}". Processing is stopped.
     */

    public static final int E_BRANCH_OBJECT_NOT_FOUND_FOR_BRANCH_SORT_CODE = 40409013;


    /**
     * <code>E_ATLEAST___SUPERVISOR___AUTHORIZATION_LEVEL_MUST_BE_SPECIFIED_</code> = Atleast 1 Supervisor & Authorization Level must be specified.
     */

    public static final int E_SUPERVISOR_AUTHORIZATION_LEVEL_MUST_BE_SPECIFIED = 40409011;

    /**
     * <code>E_ERROR_OCCURED_WHILE_CREATING_DEBIT_RECORD_OF_FONTIS_TRANSACTION_</code> = Error occured while creating Debit Record of Fontis transaction.
     */

    public static final int E_CANNOT_CREATE_DEBIT_RECORD_OF_FONTIS_TRANSACTION = 40409010;

    /**
     * <code>E_INVALID_CASH_ACCOUNT____</code> = Invalid cash account {0}
     */
    public static final int E_INVALID_CASH_ACCOUNT = 40407518;

    /**
     * <code>E_INVALID_ACCOUNT____</code> = Invalid account {0}
     */
    public static final int E_INVALID_ACCOUNT = 40407516;

    /**
     * <code>E_INVALID_DISPENSED_CURRENCY______FORCE_POST_NOT_POSTED</code> = Invalid Dispensed Currency {0}, force Post not posted
     */

    public static final int E_INVALID_DISPENSED_CURRENCY_FORCE_POST_NOT_POSTED = 40407515;

    /**
     * <code>E_INVALID_DISPENSED_CURRENCY____</code> = Invalid Dispensed Currency {0}
     */
    public static final int E_INVALID_DISPENSED_CURRENCY = 40407514;

    /**
     * <code>E_INVALID_CURRENCY_CODE_____FORCE_POST_NOT_POSTED</code> = Invalid Currency Code{0}, force Post not posted
     */
    public static final int E_INVALID_CURRENCY_CODE_FORCE_POST_NOT_POSTED = 40407513;

    /**
     * <code>E_INVALID_CURRENCY_CODE___</code> = Invalid Currency Code{0}
     */
    public static final int E_INVALID_CURRENCY_CODE = 40407512;

    /**
     * <code>E_INVALID_CARD______FORCE_POST_NOT_POSTED</code> = Invalid Card {0}, force Post not posted
     */
    public static final int E_INVALID_CARD_FORCE_POST_NOT_POSTED = 40407511;

    /**
     * <code>E_NUMERIC_VALUE_SHOULD_BE_BETWEEN___TO___</code> = Numeric value should be between 1 to 8.
     */
    public static final int E_NUMERIC_VALUE_SHOULD_BE_BETWEEN_1_TO_8 = 40409409;

    /**
     * <code>E_INVALID_CARD____</code> = Invalid Card {0}
     */
    public static final int E_INVALID_CARD = 40407510;

    /**
     * <code>E_ADDRESS_CANNOT_START_FROM___OR__</code> = Address cannot start from 3 or 5
     */
    public static final int E_ADDRESS_CANNOT_START_FROM_3OR5 = 40409408;

    /**
     * <code>E_____CANNOT_BE_BLANK_</code> = {0} cannot be blank.
     */
    public static final int E_CANNOT_BE_BLANK = 40409407;

    /**
     * <code>E_PAY_OR_RECEIVE_SHOULD_BE_SELECED_FOR_THE_MESSAGE_TYPE_</code> = Pay or Receive should be seleced for the Message Type.
     */

    public static final int E_PAY_OR_RECEIVE_NOT_SELECED_FOR_THE_MESSAGE_TYPE = 40409406;

    /**
     * <code>E_MESSAGE_NUMBER_SHOULD_BE_BETWEEN___TO____</code> = Message number should be between 1 to 999
     */
    public static final int E_MESSAGE_NUMBER_SHOULD_BE_BETWEEN_1_TO_999 = 40409405;

    /**
     * <code>E_IDENTIFIER_CODE_____CANNOT_BE_FOUND_</code> = Identifier Code {0} cannot be found.
     */
    public static final int E_IDENTIFIER_CODE_CANNOT_BE_FOUND = 40409404;

    /**
     * <code>E_____MASK_FORMAT_IS_INCORRECT_</code> = {0} Mask format is incorrect.
     */
    public static final int E_MASK_FORMAT_IS_INCORRECT = 40409403;

    /**
     * <code>E_FOR_SELECTED_BANK_INSTRUCTION_CODE_THE_ENTERED_VALUE_____IS_NOT_ALLOWED_</code> = For selected Bank Instruction Code the entered value {0} is not allowed.
     */

    public static final int E_VALUE_NOT_ALLOWED_FOR_THE_BANK_INSTRUCTION_CODE = 40409402;

    /**
     * <code>E_________LENGTH_IS_MORE_THE____CHARACTERS_</code> = {0} {1} length is more the 35 characters.
     */
    public static final int E_LENGTH_IS_MORE_THAN_35_CHARACTERS = 40409401;

    /**
     * <code>E_________FORMAT_IS_INCORRECT_</code> = {0} {1} format is incorrect.
     */
    public static final int E_FORMAT_IS_INCORRECT = 40409400;

    /**
     * <code>E_FONTIS_CONFIGURATION_DOES_NOT_EXIST_</code> = Fontis Configuration does not exist.
     */
    public static final int E_FONTIS_CONFIGURATION_DOES_NOT_EXIST = 40409009;

    /**
     * <code>E_SUB_PRODUCT_________HAS_BEEN_SELECTED_MORE_THAN_ONCE__PLEASE_DESELECT_THE_ENTRIES_WHICH_ARE_APPEARING_MORE_THAN_ONCE_</code> = Sub Product - "{0}" has been selected more than once. Please deselect the entries which are appearing more than once.
     */

    public static final int E_SUB_PRODUCT_HAS_BEEN_SELECTED_MORE_THAN_ONCE = 40409008;

    /**
     * <code>E_NO_AUTHORIZATION_LEVEL_HAS_BEEN_SELECTED_FOR_THE_USER__________EACH_SUPERVISOR_MUST_HAVE_AN_AUTHORIZATION_LEVEL_</code> = No authorization level has been selected for the user - "{0}". Each Supervisor must have an authorization level.
     */

    public static final int E_AUTHORIZATION_LEVEL_NOT_SELECTED_FOR_SUPERVISOR = 40409007;

    /**
     * <code>E_USER_________HAS_BEEN_ASSIGNED_WITH_MORE_THAN___AUTHORIZATION_LEVEL__A_SUPERVISOR_CAN_HAVE_ONLY_ONE_AUTHORIZATION_LEVEL_</code> = User - "{0}" has been assigned with more than 1 authorization level. A Supervisor can have only one authorization level.
     */

    public static final int E_SUPERVISOR_HAS_MORE_THAN_ONE_AUTHORIZATION_LEVEL = 40409006;

    /**
     * <code>E_UNKNOWN_EXCEPTION_HAS_OCCURED_</code> = Unknown Exception has occured.
     */
    public static final int E_UNKNOWN_EXCEPTION_HAS_OCCURED = 40409005;

    /**
     * <code>E_INVALID_AUTHORIZATION_LEVEL__PLEASE_CORRECT_THE_FONTIS_CONFIGURATION_DETAILS___START_THIS_PROGRAM_</code> = Invalid Authorization Level. Please correct the fontis configuration details & start this program.
     */

    public static final int E_INVALID_AUTHORIZATION_LEVEL_IN_FONTIS_CONFIG = 40409004;

    /**
     * <code>E_YOU_DO_NOT_HAVE_PERMISSIONS_TO_APPROVE_OR_REJECT_FONTIS_TRANSACTIONS_</code> = You do not have permissions to Approve or Reject fontis transactions.
     */

    public static final int E_NOT_PERMITED_TO_APPROVE_OR_REJECT_FONTIS_TRANS = 40409003;

    /**
     * <code>E_SERVICE_EXCEPTION_HAS_OCCURED_WHILE_ACCESSING_THE_FONTIS_CONFIGURATION_DETAILS_</code> = Service Exception has occured while accessing the Fontis Configuration details.
     */

    public static final int E_ERROR_IN_ACCESSING_FONTIS_CONFIGURATION_DETAILS = 40409002;

    /**
     * <code>E_ACCESS_DENIED_FOR_THIS_PROGRAM</code> = Access denied : You do not have permissions to execute this program.
     */

    public static final int E_ACCESS_DENIED_FOR_THIS_PROGRAM = 40409001;

    /**
     * <code>E_ATM_TRANSACTION_____NOT_MAPPED_TO_UBTRANSACTION__FORCE_POST_NOT_POSTED</code> = ATM Transaction {0} not mapped to UBTransaction, force post not posted
     */

    public static final int E_ATM_TRANS_UNMAPPED_TO_UB_FORCE_POST_NOT_POSTED = 40407509;

    /**
     * <code>E_FONTIS_CONFIGURATION_RECORD_NOT_FOUND__PLEASE_CREATE_A_CONFIGURATION_RECORD_THEN_START_AUTHORIZATION_PROCESS_</code> = Fontis Configuration record not found. Please create a configuration record then start Authorization process.
     */

    public static final int E_FONTIS_CONFIGURATION_RECORD_NOT_FOUND = 40409000;

    /**
     * <code>E_ATM_TRANSACTION_____NOT_MAPPED_TO_UBTRANSACTION</code> = ATM Transaction {0} not mapped to UBTransaction
     */
    public static final int E_ATM_TRANSACTION_UNMAPPED_TO_UBTRANSACTION = 40407508;

    /**
     * <code>E_ATM_TRANS_NOT_SUPPORTED_FORCE_POST_NOT_POSTED</code> = ATM Transaction {0} not supported, force post not posted
     */

    public static final int E_ATM_TRANS_NOT_SUPPORTED_FORCE_POST_NOT_POSTED = 40407507;

    /**
     * <code>E_ATM_TRANSACTION_____NOT_SUPPORTED</code> = ATM Transaction {0} not supported
     */
    public static final int E_ATM_TRANSACTION_NOT_SUPPORTED = 40407506;

    /**
     * <code>E_ATM_TRANSACTION_____NOT_FOUND__FORCE_POST_NOT_POSTED</code> = ATM Transaction {0} not found, force post not posted
     */

    public static final int E_ATM_TRANSACTION_NOT_FOUND_FORCE_POST_NOT_POSTED = 40407505;

    /**
     * <code>E_ATM_TRANSACTION_____NOT_FOUND</code> = ATM Transaction {0} not found
     */
    public static final int E_ATM_TRANSACTION_NOT_FOUND = 40407504;

    /**
     * <code>E_DEST_COUNTRY_______IMD_______BRANCH_____NOT_MAPPED__FORCE_POST_NOT_POSTED</code> = DEST:Country {0} + IMD {1} + Branch {2} not mapped, force post not posted
     */

    public static final int E_DEST_COUNTRY_IMD_BRANCH_UNMAPPED_POST_NOT_POSTED = 40407503;

    /**
     * <code>E_DEST_COUNTRY_______IMD_______BRANCH_____NOT_MAPPED</code> = DEST:Country {0} + IMD {1} + Branch {2} not mapped
     */

    public static final int E_DEST_COUNTRY_IMD_BRANCH_NOT_MAPPED = 40407502;

    /**
     * <code>E_SRCE_COUNTRY_______IMD_______BRANCH_____NOT_MAPPED__FORCE_POST_NOT_POSTED</code> = SRCE:Country {0} + IMD {1} + Branch {2} not mapped, force post not posted
     */

    public static final int E_SRCE_COUNTRY_IMD_BRANCH_UNMAPPED_POST_NOT_POSTED = 40407501;

    /**
     * <code>E_SRCE_COUNTRY_______IMD_______BRANCH_____NOT_MAPPED</code> = SRCE:Country {0} + IMD {1} + Branch {2} not mapped
     */
     //TODO REFACTOR THIS ID TO LESS THAN 50 CHARS
    public static final int E_SRCE_COUNTRY_IMD_BRANCH_NOT_MAPPED = 40407500;

    /**
     * <code>E_THE_PATH_SPECIFIED_IS_NOT_CORRECT_OR_THE_FILES_ARE_NOT_PRESENT_IN_THE_SPECIFIED_PATH_</code> = The path Specified is not correct Or the files are not present in the specified path.
     */

    public static final int E_INCORRECT_PATH_OR_FILES_NOT_PRESENT_IN_THE_PATH = 40407586;

    /**
     * <code>E_UPLOAD_FILES_PATH_IS_NOT_CONFIGURED</code> = Upload files path is not configured
     */
    public static final int E_UPLOAD_FILES_PATH_IS_NOT_CONFIGURED = 40407585;

    /**
     * <code>E_MAIN_ACCOUNT_IS_STOPPED______SUSPENSE_ACCOUNT_WILL_BE_UPDATED</code> = Main Account is Stopped {0}, Suspense Account will be updated
     */

    public static final int E_MAIN_ACCT_STOPPED_SUSPENSE_ACCT_WILL_BE_UPDATED = 40407584;

    /**
     * <code>E_SUSPENSE_ACCOUNT_NOT_FOUND_FOR_THE_PSEUDONAME______AND_CURRENCY____</code> = Suspense Account Not found for the pseudoName: {0} and Currency:{1}
     */

    public static final int E_SUSPENSE_ACCT_NOT_FOUND_FOR_PSEUDONAME_AND_CURR = 40407583;

    /**
     * <code>E_ACCOUNT_____IS_PASSWORD_PROTECTED</code> = Account {0} is Password Protected
     */
    public static final int E_ACCOUNT_____IS_PASSWORD_PROTECTED = 40407582;

    /**
     * <code>E_BILLING_CURRENCY_AND_ACCOUNT_CURRENCY_DO_NOT_MATCH__TRANSACTION_SENT_TO_REPAIR_QUEUE_</code> = Billing currency and account currency do not match. Transaction sent to repair queue.
     */

    public static final int E_BILLING_CURRENCY_AND_ACCT_CURRENCY_DO_NOT_MATCH = 40407581;

    /**
     * <code>E_ERROR_WHILE_WRITING_TO_THE_ATM_DOWNLOAD_FILE</code> = Error while writing to the ATM download file
     */
    public static final int E_ERROR_WHILE_WRITING_TO_THE_ATM_DOWNLOAD_FILE = 40407580;

    /**
     * <code>E_CONTRA_ACCOUNT_CUSTOMER_SHOULD_BE_SWIFT_ACTIVE_</code> = Contra Account Customer should be Swift Active.
     */
    public static final int E_CONTRA_ACCOUNT_CUSTOMER_SHOULD_BE_SWIFT_ACTIVE = 40409466;
    /**
     * <code>E_PARTYIDENTIFIER_EXCEED_34_CHAR</code>=Party Identifier is exceeding 34 characters.
     */
    public static final int E_PARTYIDENTIFIER_EXCEED_34_CHAR=40409467;
    /**
     * <code>E_PARTYIDENTIFIER_EXCEED_27_CHAR</code>=Party Identifier is exceeding 27 characters.
     */
    public static final int E_PARTYIDENTIFIER_EXCEED_27_CHAR=40409468;
    
    /**
     * <code>E_MUSTNOTBEENTERED</code>={0} Must not be entered.
     */
    public static final int E_MUSTNOTBEENTERED=40409469;
    /**
     * <code>E_MUSTBEENTERED</code>={0} Must be entered.
     */
    public static final int E_MUSTBEENTERED=40409470;
    /**
     * <code>E_COUNTRYCODEINVALID</code>=Country code entered for Name and Address is invalid.
     */
    public static final int E_COUNTRYCODEINVALID=40409471;
    /**
     * <code>E_SETTLEMENT_INSTRUCTION_NOT_CONFIGURED_PROPERLY_</code> = Settlement Instruction not configured properly.
     */
   
    public static final int E_SETTLEMENT_INSTRUCTION_NOT_CONFIGURED_PROPERLY = 40409465;

    /**
     * <code>E_COULD_NOT_ESTABLISH_CONNECTION_WITH_THE_MQ</code> = Could Not Establish Connection With The MQ
     */
    public static final int E_COULD_NOT_ESTABLISH_CONNECTION_WITH_THE_MQ = 40409464;

    /**
     * <code>E_PLEASE_ENTER_EITHER_CUSTOMER_NAME_AND_ADDRESS_OR_ORDERING_CUSTOMER_IDENTIFIER_CODE_IF_ORDERING_CUSTOMER_ACCOUNT_NUMBER_IS_POPULATED_</code> = Please enter either customer name and address or ordering customer identifier code if Ordering customer account number is populated.
     */

    public static final int E_REQUIRE_CUST_NAME_ADDR_OR_ORDERING_CUST_ID_CODE = 40409463;

    /**
     * <code>E_ADDRESS_FIELD_LENGTH_SHOULD_NOT_BE_MORE_THEN____CHARACTERS_</code> = Address field length should not be more then 35 characters.
     */

    public static final int E_ADDRESS_FIELD_LENGTH_IS_MORE_THEN_EXPECTED = 40409462;

    /**
     * <code>E_ORDERING_CUSTOMER_IDENTIFIER_CODE_ORDERING_CUSTOMER_ACCOUNT_NUMBER_AND_PARTY_ADDRESS_LINE__SHOULD_NOT_BE_BLANK_SIMULTANEOUSLY_</code> = Ordering Customer Identifier Code,Ordering Customer Account Number and Party Address Line1 should not be blank simultaneously.
     */

    public static final int E_CUST_ID_CODE_ACCT_NUM_PARTY_ADDR_NOT_BLANK_SIMUL = 40409461;

    /**
     * <code>E_IF_MESSAGE_TYPE_IS_____THEN_ORDERING_INSTITUTE_IDENTIFIER_CODE_IS_MANDATORY_</code> = If message type is 205 then ordering institute identifier code is mandatory.
     */

    public static final int E_ORDERING_INSTITUTE_IDENTIFIER_CODE_IS_MANDATORY = 40409460;



    /**
     * <code>E_INVALID_UTILITY_BILL_ACCOUNT_</code> = Invalid Utility Bill Account.
     */
    public static final int E_INVALID_UTILITY_BILL_ACCOUNT = 40407565;

    /**
     * <code>E_UTILITY_BILL_AND_BILL_NUMBER_HAVE_NOT_BEEN_CONFIGURED_</code> = Utility Bill and Bill Number have not been configured.
     */

    public static final int E_UTILITY_BILL_AND_BILL_NUMBER_NOT_CONFIGURED = 40407564;

    /**
     * <code>E_UNABLE_TO_PROCESS_STOPPED_UTILITY_BILL_</code> = Unable to process Stopped Utility Bill.
     */
    public static final int E_UNABLE_TO_PROCESS_STOPPED_UTILITY_BILL = 40407563;

    /**
     * <code>E_INVALID_UTILITY_BILL_ACCOUNT__ATM_CREDIT_SUSPENSE_ACCOUNT_WILL_BE_UPDATED</code> = Invalid Utility Bill Account, ATM Credit Suspense account will be updated
     */

    public static final int E_INVALID_UTILITY_BILL_ACC = 40407562;

    /**
     * <code>E_PARTY_IDENTIFIER_SHOULD_NOT_BE_POPULATED</code> = If ordering customer identifier code is populated then party identifier should not be populated.
     */

    public static final int E_PARTY_IDENTIFIER_SHOULD_NOT_BE_POPULATED = 40409459;

    /**
     * <code>E_IF_ADDRESS_LINE___IS_POPULATED_THEN_ORDERING_CUSTOMER_IDENTIFIER_CODE_SHOULD_NOT_BE_GIVEN_</code> = If address line 1 is populated then Ordering customer identifier code should not be given.
     */

    public static final int E_ORDERING_CUSTOMER_IDENTIFIER_CODE_REQUIRED = 40409458;

    /**
     * <code>E_IF_ADDRESS_LINE___IS_POPULATED_THEN_ORDERING_CUSTOMER_ACCOUNT_NUMBER_AND_PARTY_IDENTIFIER_CODE_BOTH_SHOULD_NOT_BE_GIVEN_SIMULTANEOUSLY_</code> = If address line 1 is populated then ordering Customer account number and Party identifier	code both should not be given simultaneously.
     */

    public static final int E_ORDERING_CUST_ACCT_NUM_AND_PARTY_ID_CODE_SIMUL
    = 40409457;

    /**
     * <code>E_ORDERING_CUSTOMER_IDENTIFIER_CODE_SHOULD_BE_BLANK_WHEN_PARTY_IDENTIFIER_CODE_FIELD_IS_POPULATED_</code> = Ordering Customer Identifier code should be blank when party identifier code field is populated.
     */

    public static final int E_CUST_ID_CODE_NOT_BLANK_WHEN_PARTY_ID_CODE_EXISTS = 40409456;

    /**
     * <code>E_ADDRESS_LINE___IS_MANDATORY_WITH_PARTY_IDENTIFIER_</code> = Address line 1 is mandatory with party identifier.
     */

    public static final int E_ADDRESS_LINE_IS_MANDATORY_WITH_PARTY_IDENTIFIER = 40409455;

    /**
     * <code>E_ADDRESS_LINE___TO___AND_ORDERING_CUSTOMER_IDENTIFIER_CODE_BOTH_CANNOT_BE_PRESENT_SIMULTANEOUSLY_</code> = Address line 1 to 4 and ordering customer identifier code both cannot be present simultaneously.
     */

    public static final int E_ADDR_LINE_TO_AND_ORD_CUST_ID_CODE_EXISTS_SIMUL = 40409454;

    /**
     * <code>E_ORDERING_CUSTOMER_ACCOUNT_NUMBER_AND_PARTY_IDENTIFIER_BOTH_CANNOT_BE_PRESENT_SIMULTANEOUSLY_</code> = Ordering Customer Account Number and Party Identifier both cannot be present simultaneously.
     */

    public static final int E_ORDERING_CUST_ACCT_NUM_AND_PARTY_ID_EXISTS_SIMUL = 40409453;

    /**
     * <code>E_THE_FORMAT_OF__PARTY_ADDRESS_LINE___IS_INVALID__SHOULD_START_WITH__</code> = The format of  Party Address line 1 is Invalid, Should start with 1
     */

    public static final int E_FORMAT_OF_PARTY_ADDR_LINE_IS_INVALID = 40409452;

    /**
     * <code>E_PARTY_ADDRESS_LINE_____NOT_ALLOWED_WITH_OUT_PARTY_ADDRESS_LINE____</code> = Party address line {0} not allowed with out Party address line {1}
     */

    public static final int E_PARTY_ADDRESS_LINE_NOT_ALLOWED = 40409451;

    /**
     * <code>E_ADDRESS____IS_NOT_ALLOWED_WITHOUT_ADDRESS_____AND_VICE_VERSA_</code> = Address {0}is not allowed without address {1} and vice versa.
     */

    public static final int E_ADDRESS_IS_NOT_ALLOWED = 40409450;

    /**
     * <code>E_ERROR_IN_DATABASE_COMMIT_PLESE_CHECK_DATABASE</code> = Error in Database Commit.Plese check Database
     */
    public static final int E_ERROR_IN_DATABASE_COMMIT = 40409059;

    /**
     * <code>E_ERROR_WHILE_UPDATING_TRANSACTIONREFERENCE_IN_FONTISCONFIG_</code> = Error while updating TransactionReference in FontisConfig.
     */

    public static final int E_UPDATE_ERROR_TRANSACTION_REF_IN_FONTISCONFIG = 40409058;

    /**
     * <code>E_FAILED_TO_LOAD_FONTIS_PROPERTIES_FILE</code> = Fontis properties file failed to load. Please verify it's in correct path & contains all the required parameters.
     */

    public static final int E_FAILED_TO_LOAD_FONTIS_PROPERTIES_FILE = 40409057;

    /**
     * <code>E_NO_FONTIS_FILE_AVAILABLE_FOR_READING__DIRECTORY_IS_EMPTY_</code> = No Fontis file available for reading. Directory is empty.
     */

    public static final int E_FONTIS_FILE_UNAVAILABLE_FOR_READING = 40409056;

    /**
     * <code>E_UNABLE_TO_RETRIEVE_XML_BATCH_FILE__BATCH_LIST_IS_EMPTY_</code> = Unable to retrieve XML batch file, Batch list is empty.
     */

    public static final int E_UNABLE_TO_RETRIEVE_XML_BATCH_FILE = 40409055;

    /**
     * <code>E_EXCEPTION_OCCURED_WHILE_READING_THE_FILE_PLEASE_CHECK_GIVEN_FILE_FORMAT_IS_CORRECT_OR_NOT_</code> = Exception Occured while reading the file.Please check given file format is correct or not.
     */

    public static final int E_EXCEPTION_OCCURED_WHILE_READING_THE_FILE = 40409054;

    /**
     * <code>E_ERROR_IN_FILE_I_O__CHECK_WHETHER_GIVEN_BATCH_FILE_OR_FOLDER_IS_EXIST_OR_NOT_</code> = Error in File I/O. Check whether given batch File OR Folder is exist or not.
     */

    public static final int E_BATCH_FILE_OR_FOLDER_IS_MAY_NOT_EXIST = 40409053;

    /**
     * <code>E_ERROR_WHILE_READING_TPP_BATCH_FILE__PLEASE_CHECK_THAT_TPP_FILE_MAY_HAVE_INCORRECT_FORMAT_</code> = Error while reading TPP batch file. Please check that TPP file may have incorrect format.
     */

    public static final int E_ERROR_WHILE_READING_TPP_BATCH_FILE = 40409052;

    /**
     * <code>E_ERROR_WHILE_READING_IAT_BATCH_FILE__PLEASE_CHECK_THAT_IAT_FILE_MAY_HAVE_INCORRECT_FORMAT_</code> = Error while reading IAT batch file. Please check that IAT file may have incorrect format.
     */

    public static final int E_ERROR_WHILE_READING_IAT_BATCH_FILE = 40409051;

    /**
     * <code>E_INVALID_TRAVELLERS_CHEQUE_ACCOUNT__ATM_CREDIT_SUSPENSE_ACCOUNT_WILL_BE_UPDATED</code> = Invalid Travellers Cheque account, ATM Credit Suspense account will be updated
     */

    public static final int E_INVALID_TRAVELLERS_CHEQUE_ACCOUNT = 40407556;

    /**
     * <code>E_INVALID_DEVICE_ID______ATM_CREDIT_SUSPENSE_ACCOUNT_WILL_BE_UPDATED</code> = Invalid Device Id {0}, ATM Credit Suspense account will be updated
     */

    public static final int E_INVALID_DEVICE_ID_ATM_CR_SUSPENSE_ACCT_UPDATED = 40407555;

    /**
     * <code>E_MAIN_ACCOUNT_IS_STOPPED______SUSPENSE_ACCOUNT_WILL_BE_UPDATED</code> = Main Account is Stopped {0}, Suspense Account will be updated
     */

    public static final int E_MAIN_ACCT_STOPPED_SUSPENSE_ACC_WILL_BE_UPDATED = 40407554;

    /**
     * <code>E_PROCESSED_FILE_RENAMING_FAILED__PLEASE_CHECK_FOLDER_PERMISSIONS_</code> = Processed file renaming failed: Please check folder permissions.
     */

    public static final int E_PROCESSED_FILE_RENAMING_FAILED = 40407553;

    /**
     * <code>E_MULTIPLE_FILE_UPLOADING_FAILED__PLEASE_CHECK_INPUT_PARAMETERS_OF_SHELL_SCRIPT_</code> = Multiple file uploading failed: Please check input parameters of shell script.
     */

    public static final int E_MULTIPLE_FILE_UPLOADING_FAILED = 40407552;

    /**
     * <code>E_FAILED_TO_LOAD_THE_ATM_PROPERTIES_FILE</code> = Failed to load the ATM properties file. Please verify it's in correct path & contains all the required parameters.
     */

    public static final int E_FAILED_TO_LOAD_THE_ATM_PROPERTIES_FILE = 40407551;

    /**
     * <code>E_TCPCONNECTION_REFUSED_TO_CONNECT_TO_ATM_PORT__MAXIMUM_NUMBER_OF_CONNECTIONS_REACHED_</code> = TCPConnection refused to connect to ATM port: Maximum number of connections reached.
     */

    public static final int E_TCPCONN_REFUSED_TO_CONNECT_TO_ATM_PORT = 40407550;

    /**
     * <code>E_THE_FORMAT_OF__PARTY_ADDRESS_LINE_____IS_INVALID_</code> = The format of  Party Address line {0} is Invalid.
     */

    public static final int E_FORMAT_OF_PARTY_ADDRESS_LINE_IS_INVALID = 40409449;

    /**
     * <code>E_ACCOUNT_NO__WITH_____OR_BANK_CODE_WITH______NEED_TO_BE_ENTERED_AND_NOT_BOTH_</code> = Account No. with "/" or bank code with "//" need to be entered and not both.
     */

    public static final int E_ACCT_NUM_OR_BANK_CODE_WITH_SLASH_IS_REQ_NOT_BOTH = 40409448;

    /**
     * <code>E_____DOES_NOT_SATISFY_ISO______STANDARDS_</code> = {0} does not satisfy ISO 9362 standards.
     */
    public static final int E_DOES_NOT_SATISFY_ISO_STANDARDS = 40409447;

    /**
     * <code>E_MESSAGE_NUMBER_SHOULD_BE_EQUAL_OR_LESS_THAN_____FOR_THIS_SELECTION_</code> = message number should be equal or less than {0} for this selection.
     */

    public static final int E_MESSAGE_NUMBER_NOT_EQUAL_OR_GREATER = 40409446;

    /**
     * <code>E_PARTY_ADDRESS_LINE_____IS_NOT_ALLOWED_WHEN_PARTY_IDENTIFIER_IS_BLANK_</code> = Party address line {0} is not allowed when Party Identifier is blank.
     */

    public static final int E_PARTY_ADDR_LINE_NOT_ALLOWED_IF_PARTY_ID_IS_BLANK = 40409445;

    /**
     * <code>E_CUSTOMER_NOT_A_FINANCIAL_INSTITUTION__HENCE_CAN_NOT_ENTER_SWIFT_ACCOUNT_NUMBER_</code> = Customer Not a Financial Institution, hence can not enter Swift Account Number.
     */

    public static final int E_CAN_NOT_ENTER_SWIFT_ACCT_NUM_AS_NOT_FIN_INSTIT = 40409444;

    /**
     * <code>E_SWIFT_ACCOUNT_NUMBER_SHOULD_START_WITH_____</code> = Swift Account Number should start with "/".
     */
    public static final int E_SWIFT_ACCOUNT_NUMBER_SHOULD_START_WITH = 40409443;




    /**
     * <code>E_DATA_INVALID_OR_NOT_FOUND_</code> = DATA INVALID OR NOT FOUND.
     */
    public static final int E_DATA_INVALID_OR_NOT_FOUND = 40409249;

    /**
     * <code>E_INVALID_AGENT_ID_OR_POS_NUMBER_</code> = INVALID AGENT ID OR POS NUMBER.
     */
    public static final int E_INVALID_AGENT_ID_OR_POS_NUMBER = 40409248;

    /**
     * <code>E_INVALID_POS_TYPE_</code> = INVALID POS TYPE.
     */
    public static final int E_INVALID_POS_TYPE = 40409247;

    /**
     * <code>E_INVALID_POS_</code> = INVALID POS.
     */
    public static final int E_INVALID_POS = 40409246;

    /**
     * <code>E_INITIAL_SETUP_PASSWORD_FAILURE_</code> = INITIAL SETUP PASSWORD FAILURE.
     */
    public static final int E_INITIAL_SETUP_PASSWORD_FAILURE = 40409245;

    /**
     * <code>E_PROFILE_SETUP_IS_INCOMPLETE_</code> = PROFILE SETUP IS INCOMPLETE.
     */
    public static final int E_PROFILE_SETUP_IS_INCOMPLETE = 40409244;


    /**
     * <code>E_ERROR_GETTING_POS_DATA_</code> = ERROR GETTING POS DATA.
     */
    public static final int E_ERROR_GETTING_POS_DATA = 40409243;

    /**
     * <code>E_GETINITIALSETUP_PROFILESETUP_FAILURE_</code> = GETINITIALSETUP/PROFILESETUP FAILURE.
     */
    public static final int E_GETINITIALSETUP_PROFILESETUP_FAILURE = 40409242;

    /**
     * <code>E_INVALID_PASSWORD_</code> = INVALID PASSWORD.
     */
    public static final int E_INVALID_PASSWORD = 40409241;
    /**
     * <code>E_TRANSACTION_NOT_ALLOWED_</code> = TRANSACTION NOT ALLOWED.
     */
    public static final int E_TRANSACTION_NOT_ALLOWED = 40409240;

    /**
     * <code>E_MUST_DO_CHECK_IN_OR_INITIAL_SETUP_</code> = MUST DO CHECK-IN OR INITIAL SETUP.
     */
    public static final int E_MUST_DO_CHECK_IN_OR_INITIAL_SETUP = 40409239;

    /**
     * <code>E_SECURITY_CHECK_FAILED_POS_DE_AUTHORIZED_</code> = SECURITY CHECK FAILED,POS DE-AUTHORIZED.
     */
    public static final int E_SECURITY_CHECK_FAILED_AUTHORIZED = 40409238;

    /**
     * <code>E_UNIT_HAS_BEEN_DE_AUTHORIZED_</code> = UNIT HAS BEEN DE-AUTHORIZED.
     */
    public static final int E_UNIT_HAS_BEEN_DE_AUTHORIZED = 40409237;

    /**
     * <code>E_BAD_TOKEN_</code> = BAD TOKEN.
     */
    public static final int E_BAD_TOKEN = 40409236;

    /**
     * <code>E_BAD_UNIT_PROFILE_ID_</code> = BAD UNIT PROFILE ID.
     */
    public static final int E_BAD_UNIT_PROFILE_ID = 40409235;

    /**
     * <code>E_SECURITY_FAILURE_</code> = SECURITY FAILURE.
     */
    public static final int E_SECURITY_FAILURE = 40409234;

    /**
     * <code>E_INVALID_TIME_ZONE_NAME_</code> = INVALID TIME ZONE NAME.
     */
    public static final int E_INVALID_TIME_ZONE_NAME = 40409233;

    /**
     * <code>E_CUSTOMER_IS_NOT_FOUND_</code> = CUSTOMER IS NOT FOUND.
     */


    /**
     * <code>E_TRANSACTION_TYPE_NOT_ALLOWED_FOR_RECEIVE_COUNTRY_</code> = TRANSACTION TYPE NOT ALLOWED FOR RECEIVE COUNTRY.
     */

    public static final int E_TRANSACTION_TYPE_NOT_COUNTRY = 40409231;

    /**
     * <code>E_INVALID_RECEIVE_COUNTRY_</code> = INVALID RECEIVE COUNTRY.
     */
    public static final int E_INVALID_RECEIVE_COUNTRY = 40409230;

    /**
     * <code>E_MESSAGE_US_ONLY_</code> = MESSAGE US ONLY.
     */
    public static final int E_MESSAGE_US_ONLY = 40409229;

    /**
     * <code>E_INVALID_CURRENCY_CODE_</code> = INVALID CURRENCY CODE.
     */
    public static final int E_INVALID_CURRENCY_CODE_TR = 40409228;

    /**
     * <code>E_WRONG_API_VERSION_</code> = WRONG API VERSION.
     */
    public static final int E_WRONG_API_VERSION = 40409227;

    /**
     * <code>E_WRONG_MONEYGRAM_TYPE_</code> = WRONG MONEYGRAM TYPE.
     */
    public static final int E_WRONG_MONEYGRAM_TYPE = 40409226;

    /**
     * <code>E_FEE_NOT_AVAILABLE_</code> = FEE NOT AVAILABLE.
     */
    public static final int E_FEE_NOT_AVAILABLE = 40409225;

    /**
     * <code>E_PRODUCT_NOT_AVAILABLE_</code> = PRODUCT NOT AVAILABLE.
     */
    public static final int E_PRODUCT_NOT_AVAILABLE = 40409224;

    /**
     * <code>E_INVALID_COUNTRY_CODE_</code> = INVALID COUNTRY CODE.
     */
    public static final int E_INVALID_COUNTRY_CODE = 40409223;

    /**
     * <code>E_OUTSIDE_OF_STORE_BUSINESS_HOURS_</code> = OUTSIDE OF STORE BUSINESS HOURS.
     */
    public static final int E_OUTSIDE_OF_STORE_BUSINESS_HOURS = 40409222;

    /**
     * <code>E_INVALID_STATE_FOR_GIVEN_COUNTRY_</code> = INVALID STATE FOR GIVEN COUNTRY.
     */
    public static final int E_INVALID_STATE_FOR_GIVEN_COUNTRY = 40409221;

    /**
     * <code>E_DAILY_LIMIT_EXCEEDED_</code> = DAILY LIMIT EXCEEDED.
     */
    public static final int E_DAILY_LIMIT_EXCEEDED = 40409220;

    /**
     * <code>E_MAX_AMOUNT_EXCEEDED_</code> = MAX AMOUNT EXCEEDED.
     */
    public static final int E_MAX_AMOUNT_EXCEEDED = 40409219;

    /**
     * <code>E_LIMIT_EDIT_RULE_FAILURE_</code> = LIMIT/EDIT RULE FAILURE.
     */
    public static final int E_LIMIT_EDIT_RULE_FAILURE = 40409218;

    /**
     * <code>E_ONLY_ONE_AMOUNT_FIELD_IS_ALLOWED_</code> = ONLY ONE AMOUNT FIELD IS ALLOWED.
     */
    public static final int E_ONLY_ONE_AMOUNT_FIELD_IS_ALLOWED = 40409217;

    /**
     * <code>E_RECEIVER_ADDRESS_NOT_ALLOWED_</code> = RECEIVER ADDRESS NOT ALLOWED.
     */
    public static final int E_RECEIVER_ADDRESS_NOT_ALLOWED = 40409216;

    /**
     * <code>E_DIRECTIONS_FIELDS_NOT_ALLOWED_</code> = DIRECTIONS FIELDS NOT ALLOWED.
     */
    public static final int E_DIRECTIONS_FIELDS_NOT_ALLOWED = 40409215;

    /**
     * <code>E_MESSAGEFIELD__OR_MESSAGEFIELD__NOT_ALLOWED_</code> = MESSAGEFIELD1 OR MESSAGEFIELD2 NOT ALLOWED.
     */
    public static final int E_MESSAGEFIELD_NOT_ALLOWED = 40409214;

    /**
     * <code>E_TEST_QUESTION_ANSWER_NOT_ALLOWED_</code> = TEST QUESTION/ANSWER NOT ALLOWED.
     */
    public static final int E_TEST_NOT_ALLOWED = 40409213;

    /**
     * <code>E_DISALLOWED_FIELD_SET_</code> = DISALLOWED FIELD SET.
     */
    public static final int E_DISALLOWED_FIELD_SET = 40409212;

    /**
     * <code>E_RECEIVER_S_NAME_IS_REQUIRED_</code> = RECEIVER'S NAME IS REQUIRED.
     */
    public static final int E_RECEIVER_S_NAME_IS_REQUIRED = 40409211;

    /**
     * <code>E_A_STATE_OR_COUNTRY_CODE_IS_REQUIRED_</code> = A STATE OR COUNTRY CODE IS REQUIRED.
     */
    public static final int E_A_STATE_OR_COUNTRY_REQUIRED = 40409210;

    /**
     * <code>E_INVALID_REFERENCE_NUMBER_</code> = INVALID REFERENCE NUMBER.
     */
    public static final int E_INVALID_REFERENCE_NUMBER = 40409253;

    /**
     * <code>E_INVALID_CHECK_NUMBER_</code> = INVALID CHECK NUMBER.
     */
    public static final int E_INVALID_CHECK_NUMBER = 40409252;

    /**
     * <code>E_FEE_OR_RATE_HAS_CHANGED_SINCE_QUOTE_WAS_ISSUED_</code> = FEE OR RATE HAS CHANGED SINCE QUOTE WAS ISSUED.
     */
    public static final int E_FEE_OR_RATE_HAS_CHANGED = 40409251;

    /**
     * <code>E_NOT_VALID_VALUE</code> = INVALID VALUE.
     */
    public static final int E_NOT_VALID_VALUE = 40409250;

    /**
     * <code>E_SEND_REVERSAL_CANCEL_MUST_BE_REQUESTED_SAME_DAY_</code> = SEND REVERSAL/CANCEL MUST BE REQUESTED SAME DAY.
     */
    public static final int E_SEND_REVERSAL_CANCEL_MUST_BE_REQUESTED_SAME_DAY = 40409254;


    /**
     * <code>E_SEND_REVL_CANC_IS_ONLY_ALLWD_FOR_STD_MG_DELIV_OPT_</code> = SEND REVL/CANC IS ONLY ALLWD FOR STD MG DELIV OPT.
     */

    public static final int E_SEND_REVL_CANC_IS_ONLY_ALLWD = 40409255;

    /**
     * <code>E_TRANS_IS_EXPRESSPAYMENT_AND_CANNOT_BE_REVERSED</code> = TRANS IS AN EXPRESSPAYMENT AND CANNOT BE REVERSED.
     */

    public static final int E_TRANS_IS_EXPRESSPAYMENT_AND_CANNOT_BE_REVERSED= 40409256;

    /**
     * <code>E_REVERSING_AGENT_IS_NOT_THE_SAME_AS_SEND_AGENT_</code> = REVERSING AGENT IS NOT THE SAME AS SEND AGENT.
     */
    public static final int E_REVERSING_AGENT_IS_NOT_THE_SAME_AS_SEND_AGENT_ = 40409257;
    /**
     * <code>E_TRANSACTION_NOT_IN_SEND_STATUS_</code> = TRANSACTION NOT IN SEND STATUS.
     */
    public static final int E_TRANSACTION_NOT_IN_SEND_STATUS = 40409258;

    /**
     * <code>E_REVERSL_AMT_AND_FEE_MUST_EQUAL_SEND_AMT_AND_FEE_</code> = REVERSL AMT AND FEE MUST EQUAL SEND AMT AND FEE.
     */
    public static final int E_REVERSL_AMT_AND_FEE_MUST_EQUAL = 40409259;

    /**
     * <code>E_AMOUNT_MUST_NOT_EXCEED_SENDING_LIMIT_</code> = AMOUNT MUST NOT EXCEED SENDING LIMIT.
     */
    public static final int E_AMOUNT_MUST_NOT_EXCEED_SENDING_LIMIT = 40409263;

    /**
     * <code>E_AMOUNT_MUST_NOT_EXCEED_COUNTRY_RECEIVE_LIMIT_</code> = AMOUNT MUST NOT EXCEED COUNTRY RECEIVE LIMIT.
     */
    public static final int E_AMOUNT_MUST_NOT_EXCEED_COUNTRY_LIMIT = 40409262;

    /**
     * <code>E_AMOUNT_MUST_BE_GREATER_THAN___</code> = AMOUNT MUST BE GREATER THAN ZERO.
     */
    public static final int E_AMOUNT_MUST_BE_GREATER_THAN = 40409261;

    /**
     * <code>E_INVALID_CONFIRMATION_NUMBER_</code> = INVALID CONFIRMATION NUMBER.
     */
    public static final int E_INVALID_CONFIRMATION_NUMBER = 40409260;


    /**
     * <code>E_ACCOUNT_NUMBER_INVALID</code> = INVALID ACCOUNT NUMBER.
     */
    public static final int E_ACCOUNT_NUMBER_INVALID = 40409264;
    /**
     * <code>E_CUSTOMER_IS_NOT_FOUND_</code> = CUSTOMER IS NOT FOUND.
     */
    public static final int E_CUSTOMER_IS_NOT_FOUND = 40409265;

    /**
     * <code>E_NO_AGENTS_FOUND_</code> = NO AGENTS FOUND.
     */
    public static final int E_NO_AGENTS_FOUND = 40409266;

    /**
     * <code>E_NO_CITIES_WITH_AGENTS_FOUND_</code> = NO CITIES WITH AGENTS FOUND.
     */
    public static final int E_NO_CITIES_WITH_AGENTS_FOUND = 40409267;
    /**
     * <code>E_INVALID_AREA_CODE_AND_PREFIX_</code> = INVALID AREA CODE AND PREFIX.
     */
    public static final int E_INVALID_AREA_CODE_AND_PREFIX = 40409268;

    /**
     * <code>E_TAX_ID___SHOULD_BE___DIGITS_</code> = TAX ID # SHOULD BE 9 DIGITS.
     */
    public static final int E_TAX_ID_SHOULD_BE_DIGITS_ = 40409269;

    /**
     * <code>E_EMPTY_RESPONSE_RECEIVED_FROM_MONEYGRAM_HOST_</code> = EMPTY RESPONSE RECEIVED FROM MONEYGRAM HOST.
     */
    public static final int E_EMPTY_RESPONSE_RECEIVED_FROM_MONEYGRAM_HOST = 40409273;

    /**
     * <code>E_MONEYGRAM_SERVER_ERROR_</code> = MONEYGRAM SERVER ERROR.
     */
    public static final int E_MONEYGRAM_SERVER_ERROR = 40409272;

    /**
     * <code>E_INVALID_ID_</code> = INVALID ID.
     */
    public static final int E_INVALID_ID = 40409271;

    /**
     * <code>E_US_SOCIAL_SECURITY_NUMBER_SHOULD_BE___DIGITS_</code> = US SOCIAL SECURITY NUMBER SHOULD BE 9 DIGITS.
     */
    public static final int E_SOCIAL_SECURITY_NUMBER_SHOULD_BE_DIGITS = 40409270;


    /**
     * <code>E_ERROR_RECEIVED_FROM_MONEYGRAM_HOST_</code> = ERROR RECEIVED FROM MONEYGRAM HOST.
     */
    public static final int E_ERROR_RECEIVED_FROM_MONEYGRAM_HOST = 40409274;
    /**
     * <code>E_COUNTRY_NOT_PARTICIPATING_WITH_THE_MG_NETWORK_</code> = COUNTRY NOT PARTICIPATING WITH THE MG NETWORK.
     */
    public static final int E_COUNTRY_NOT_PARTICIPATING_WITH_THE_MG = 40409275;

    /**
     * <code>E_SENDS_TO_CUBA_NOT_ALLOWED_</code> = SENDS TO CUBA NOT ALLOWED.
     */
    public static final int E_SENDS_TO_CUBA_NOT_ALLOWED = 40409283;

    /**
     * <code>E_AGENT_CURRENCY_IS_INCORRECT_</code> = AGENT CURRENCY IS INCORRECT.
     */
    public static final int E_AGENT_CURRENCY_IS_INCORRECT = 40409282;

    /**
     * <code>E_AGENT_UNABLE_TO_RECEIVE_THIS_TRANSACTION_</code> = AGENT UNABLE TO RECEIVE THIS TRANSACTION.
     */
    public static final int E_AGENT_UNABLE_TO_RECEIVE_THIS_TRANSACTION = 40409281;

    /**
     * <code>E_POSSIBLE_DUPLICATE_TRANSACTION_</code> = POSSIBLE DUPLICATE TRANSACTION.
     */
    public static final int E_POSSIBLE_DUPLICATE_TRANSACTION = 40409284;

    /**
     * <code>E_REQUESTED_ALLOWED_FOR_US_ONLY_</code> = REQUESTED ALLOWED FOR US ONLY.
     */
    public static final int E_REQUESTED_ALLOWED_FOR_US_ONLY_ = 40409285;

    /**
     * <code>E_MONEYGRAM_INTERNAL_SYSTEM_ERROR_</code> = MONEYGRAM INTERNAL SYSTEM ERROR.
     */
    public static final int E_MONEYGRAM_INTERNAL_SYSTEM_ERROR = 40409286;

    /**
     * <code>E_COUNTRY_IS_TEMPORARILY_OUT_OF_SERVICE_</code> = COUNTRY IS TEMPORARILY OUT OF SERVICE.
     */
    public static final int E_COUNTRY_IS_TEMPORARILY_OUT_OF_SERVICE = 40409276;

    /**
     * <code>E_THERE_ARE_NO_AGENTS_IN_THE_DESTINATION_COUNTRY_</code> = THERE ARE NO AGENTS IN THE DESTINATION COUNTRY.
     */
    public static final int E_THERE_ARE_NO_AGENTS_IN_THE_DESTINATION_COUNTRY = 40409277;

    /**
     * <code>E_PLZ_CALL_CUST_SERV_CENTR_TO_COMP_THIS_TRANS_</code> = PLZ CALL CUST SERV CENTR TO COMP THIS TRANS.
     */
    public static final int E_PLZ_CALL_CUST_SERV_CENTR = 40409278;

    /**
     * <code>E_INTERNAL_ERROR_</code> = INTERNAL ERROR.
     */
    public static final int E_INTERNAL_ERROR = 40409287;

    /**
     * <code>E_EXCEPTION_OCCURED_WHILE_READING_PROPERTY_FILE_FOR_MONEYGRAM_REFRESH_</code> = EXCEPTION OCCURED WHILE READING PROPERTY FILE FOR MONEYGRAM REFRESH.
     */

    public static final int E_STATE_CODE_IS_MANDATORY = 40409292;

    /**
     * <code>E_ERROR_DURING_PARSING_XML_</code> = ERROR DURING PARSING XML.
     */
    public static final int E_ERROR_DURING_PARSING_XML = 40409291;

    /**
     * <code>E_ERROR_CONNECTING_TO_MONEYGRAM_SERVER_</code> = ERROR CONNECTING TO MONEYGRAM SERVER.
     */
    public static final int E_ERROR_CONNECTING_TO_MONEYGRAM_SERVER = 40409290;

    /**
     * <code>E_INVALID_SCENARIO____FEE_AMT_GREATER_THAN_SEND_AMT_</code> = INVALID SCENARIO -- FEE AMT GREATER THAN SEND AMT.
     */

    public static final int E_INVALID_SCENARIO_FEE_AMT = 40409296;

    /**
     * <code>E_PASSWORD_REQD_FOR_POSTING</code> = PASSWORD REQUIRED FOR POSTING
     */
    public static final int E_PASSWORD_REQD_FOR_POSTING = 40409351;

    /**
     * <code>E_STOPPED___PASSWORD_REQUIRED</code> = STOPPED - PASSWORD REQUIRED
     */
    public static final int E_STOPPED_PASSWORD_REQUIRED = 40409353;
    /**
     * <code>E_NO_DEBIT_POSTINGS_ALLOWED</code> = NO DEBIT POSTINGS ALLOWED
     */
    public static final int E_NO_DEBIT_POSTINGS_ALLOWED = 40409354;
    /**
     * <code>E_NO_DEBIT_POSTINGS_ALLOWED___PASSWORD_REQUIRED</code> = NO DEBIT POSTINGS ALLOWED - PASSWORD REQUIRED
     */
    public static final int E_NO_DEBIT_POSTINGS_ALLOWED_PASSWORD_REQUIRED = 40409355;
    /**
     * <code>E_NO_CREDIT_POSTINGS_ALLOWED</code> = NO CREDIT POSTINGS ALLOWED
     */
    public static final int E_NO_CREDIT_POSTINGS_ALLOWED_HERE = 40409356;
    /**
     * <code>E_NO_CREDIT_POSTINGS_ALLOWED___PASSWORD_REQUIRED</code> = NO CREDIT POSTINGS ALLOWED - PASSWORD REQUIRED
     */
    public static final int E_NO_CREDIT_POSTINGS_ALLOWED = 40409357;

    /**
     * <code>E_ACCOUNT_PASSWORD_RESTRICTED</code> = ACCOUNT PASSWORD RESTRICTED
     */
    public static final int E_ACCOUNT_PASSWORD_RESTRICTED = 40409358;


    /**
     * <code>E_A_C_DORMANT</code> = A/C DORMANT
     */
    public static final int E_A_C_DORMANT = 40409359;

    /**
     * <code>E_INVALID_TRANSACTION___CURRENT_STATUS_IS_DORMANT</code> = INVALID TRANSACTION - CURRENT STATUS IS DORMANT
     */
    public static final int E_INVALID_TRANSACTION_CURRENT_STATUS = 40409360;

    /**
     * <code>ERRORS_SETTING_ID_STRUCTURE</code> =  ERRORS_SETTING_ID_STRUCTURE
     */
    public static final int ERRORS_SETTING_ID_STRUCTURE = 40415001;

    /**
     * <code>E_INVALID_CHECK_SUM</code> =  check sum is invalid.
     */
    public static final int E_INVALID_CHECK_SUM= 40415009;

    /**
     * <code>E_NOT_SWIFT_FORMAT</code> =  Swift format is invalid.
     */
    public static final int E_NOT_SWIFT_FORMAT= 40419303;

    /**
     * <code>E_BASEEQUIVAL_WRONGLY_CAL</code> =  Base equivalent not calculated properly.
     */
    public static final int E_BASEEQUIVAL_WRONGLY_CAL = 40419311;

    /**
     * <code>E_UNABLE_RETRIEVE_MOD_CONFIG</code> =  Unable to find Module configuration.
     */
    public static final int E_UNABLE_RETRIEVE_MOD_CONFIG = 40417301;

    /**
     * <code>E_FILE_NOT_FOUND</code> =  File not found.
     */
    public static final int E_FILE_NOT_FOUND = 40418301;

    /**
     * <code>E_MULTIPLY_DIVIDE_FLAG_INVALID</code> =  Multiply divide flag is invalid.
     */
    public static final int E_MULTIPLY_DIVIDE_FLAG_INVALID = 40417050;


    public static final int E_DESTINATION_PATH_IS_NOT_SPECIFIED= 40409293;

    /**
     * <code>E_STATE_CODE_IS_MANDATORY_</code> = STATE CODE IS MANDATORY.
     */


    public static final int E_EXCEPTION_OCCURED_WHILE_READING_PROPERTY_FILE = 40409294;

    public static final int E_INVALID_DESTINATION_PATH = 40409295;

    public static final int E_REQUIRED_FIELD_MISSING = 40409200;
    public static final int E_RECEIVER_ADDRESS_REQUIRED = 40409201;
    public static final int E_PHOTO_ID_REQUIRED = 40409202;
    public static final int E_LEGAL_ID_REQUIRED = 40409203;
    public static final int E_RECEIVER_ND_LAST_NAME_REQUIRED = 40409204;
    public static final int E_RECEIVER_PHONE_REQUIRED = 40409205;
    public static final int E_FREQUENT_NUMBER_REQUIRED = 40409206;
    public static final int E_AGENCY_ID_REQUIRED = 40409207;
    public static final int E_AN_AMOUNT_FIELD_IS_REQUIRED = 40409208;
    public static final int E_THIRD_PARTY_INFORMATION_IS_REQUIRED = 40409209;
    public static final int THE_MODULE_PARAM_ALREADY_EXISTS = 40409522;

    /**
     * <code>W_HIT_FOUND_IN_WATCH_LIST_</code> = Hit found in Watch List.
     */
    public static final int W_HIT_FOUND_IN_WATCH_LIST = 40407336;

    /**
     * <code>E_POS_Completion_File_Not_Found</code> = POS Completion File Not Found.
     */
    public static final int E_POS_Completion_File_Not_Found = 40407338;

    /**
     * <code>E_______NUMBERIC_CODE_DOESN_T_EXIST_</code> = "{0}" Numberic code doesn't exist.
     */
    public static final int E_NUMBERIC_CODE_DOESN_T_EXIST = 40409309;

    /**
     * <code>E_______DEBIT_SUSPENSE_PSEUDO_CODE_DOES_NOT_BELONGS_TO_______BRANCH_</code> = "{0}" Debit suspense pseudo Code does not belongs to "{1}" branch.
     */

    public static final int E_DR_SUSPENSE_PSEUDO_CODE_NOT_BELONGS_TO_BRANCH = 40409308;


    /**
     * <code>E_______CREDIT_SUSPENSE_PSEUDO_CODE_DOES_NOT_BELONGS_TO_______BRANCH_</code> = "{0}" Credit suspense pseudo Code does not belongs to "{1}" branch.
     */

    public static final int E_CR_SUSPENSE_PSEUDO_CODE_NOT_BELONGS_TO_BRANCH = 40409307;

    /**
     * <code>E_PLEASE_SELECT_SUPERVISOR______OR_ELSE_SET_AUTHORIZATION_LEVEL_TO_____NONE__</code> = Please select Supervisor-{0}. Or else set Authorization Level to "...None".
     */

    public static final int E_SET_SUPERVISOR_OR_ELSE_AUTHORIZATION_LEVEL_NONE = 40409018;

    /**
     * <code>E_______TRANSACTION_CODE_DOESN_T_EXIST_</code> = "{0}" Transaction code does not exist.
     */
    public static final int E_TRANSACTION_CODE_DOESN_T_EXIST = 40409306;

    /**
     * <code>E_______BRANCH_CODE_DOESN_T_EXIST_</code> = "{0}" Branch code does not exist.
     */
    public static final int E_BRANCH_CODE_DOESN_T_EXIST = 40409305;

    /**
     * <code>E_PLEASE_SELECT_AN_SUSPENSE_ACCOUNT_PSEUDONYM_</code> = Please select an Suspense Account Pseudonym.
     */
    public static final int E_SELECT_AN_SUSPENSE_ACCOUNT_PSEUDONYM = 40409017;

    /**
     * <code>E_THE_______MUST_BE_ENTERED_</code> = The "{0}" must be entered.
     */
    public static final int E_THE_MUST_BE_ENTERED_ = 40409304;


    /**
     * <code>E_THE_______NOT_IN_SWIFT_FORMAT_</code> = The "{0}" not in SWIFT format.
     */
    public static final int E_THE_NOT_IN_SWIFT_FORMAT = 40409303;



    /**
     * <code>E_THE_______MUST_BE___OR__</code> = The "{0}" must be + or -
     */
    public static final int E_THE_MUST_BE_PLUS_OR_MINUS = 40409302;



    /**
     * <code>E_MULTIPYDEVIDEFLAG_SHOULD_BE_EITHTER__M__OR__D__OR_SPACE_</code> = MultipyDevideFlag should be eithter 'M' or 'D' or Space.
     */

    public static final int E_MULTIPYDEVIDEFLAG_NOT_M_OR_D_OR_SPACE_ = 40409301;



    /**
     * <code>E_ERROR_IN_POPULATING_SYS_ID_CODE</code> = Exception occured while populating System Identifier code using Branch Sort Code : "{0}". Processing is stopped.
     */

    public static final int E_ERROR_IN_POPULATING_SYS_ID_CODE = 40409012;

    /**
     * <code>E_______IBSNET_TRANSACTION_REFERENCE_IS_MANDATORY_</code> = "{0}" IBSNET transaction reference is mandatory.
     */
    public static final int E_IBSNET_TRANSACTION_REFERENCE_IS_MANDATORY = 40409316;

    /**
     * <code>E_IBSNET_FATAL_ERROR_</code> = IBSNET fatal error.
     */
    public static final int E_IBSNET_FATAL_ERROR = 40409315;

    /**
     * <code>E_BACK_VALUE_DATE_POSTING_NOT_ALLOWED</code> = "{0}" posting on business date not allowed for back value dated transaction.
     */

    public static final int E_BACK_VALUE_DATE_POSTING_NOT_ALLOWED = 40409314;

    /**
     * <code>E_______VALUE_DATE_NOT_IN_PROPER_FORMAT_</code> = "{0}" value date not in proper format.
     */
    public static final int E_VALUE_DATE_NOT_IN_PROPER_FORMAT = 40409313;

    /**
     * <code>E_DUPLICATE_MESSAGES_AND_CAN_T_BE_PROCESSED</code> = "{0}" and "{1}" are duplicate messages and can't be processed.
     */

    public static final int E_DUPLICATE_MESSAGES_AND_CAN_T_BE_PROCESSED = 40409312;

    /**
     * <code>E_______BASEEQUIVAL_WRONGLY_CALCULATED_</code> = "{0}" BaseEquival wrongly calculated.
     */
    public static final int E_BASEEQUIVAL_WRONGLY_CALCULATED = 40409311;

    /**
     * <code>E_______ACCCOUNT_CAN_NOT_BE_FOUND_</code> = "{0}" acccount can not be found.
     */
    public static final int E_ACCCOUNT_CAN_NOT_BE_FOUND_ = 40409310;


    /**
     * <code>W_TRANSACTION_ALREADY_REVERSED______NOT_REVERSED</code> = Transaction already reversed {0}, Not reversed
     */
    public static final int W_TRANSACTION_ALREADY_REVERSED_NOT_REVERSED = 40407525;

    /**
     * <code>W_TRANSACTION_ALREADY_REVERSED____</code> = Transaction already reversed {0}
     */
    public static final int W_TRANSACTION_ALREADY_REVERSED = 40407524;

    /**
     * <code>W_INVALID_DESTINATION_ACCOUNT_____ATM_CREDIT_SUSPENSE_ACCOUNT_WILL_BE_UPDATED</code> = Invalid Destination Account {0},ATM Credit Suspense account will be updated
     */
     //Use duplicate
   // public static final int W_INVALID_DESTINATION_ACCOUNT_____ATM_CREDIT_SUSPENSE_ACCOUNT_WILL_BE_UPDATED = 40407554;

    /**
     * <code>W_TRANSACTION_ALREADY_POSTED____</code> = Transaction already posted {0}
     */
    public static final int W_TRANSACTION_ALREADY_POSTED = 40407523;

    /**
     * <code>W_INVALID_SETTLEMENT_ACCOUNT______ENW_CREDIT_SUSPENSE_ACCOUNT_WILL_BE_UPDATED</code> = Invalid settlement account {0}, ENW Credit Suspense Account will be updated
     */
  //Use duplicate
   public static final int W_INVALID_SETTLNT_ACCT_ENW_CR_SUS_ACCT = 40407521;

    /**
     * <code>W_THE_CONFIGURATION_XML_____WAS_NOT_CLOSED_CORRECTLY_BECAUSE_OF_ERROR____</code> = The Configuration XML {0} was not closed correctly because of error {1}
     */

    public static final int W_CONFIG_XML_NOT_CLOSED_CORRECTLY_BECAUSE_OF_ERROR = 40407534;

    /**
     * <code>W_INVALID_SETTLEMENT_ACCOUNT______ATM_CREDIT_SUSPENSE_ACCOUNT_WILL_BE_UPDATED</code> = Invalid settlement account {0}, ATM Credit Suspense Account will be updated
     */

    public static final int W_INVALID_SETTLEMT_ACCT_ATM_CR_SUS_ACCT_UPDATED = 40407533;

    /**
     * <code>W_INVALID_SETTLEMENT_ACCOUNT______ATM_DEBIT_SUSPENSE_ACCOUNT_WILL_BE_UPDATED</code> = Invalid settlement account {0}, ATM Debit Suspense Account will be updated
     */

    public static final int W_INVALID_SETTLEMT_ACCT_ATM_DR_SUSE_ACCT_UPDATED = 40407532;

    /**
     * <code>W_INVALID_SECOND_CURRENCY_CASH_ACCOUNT______ATM_DEBIT_SUSPENSE_ACCOUNT_WILL_BE_UPDATED</code> = Invalid second currency cash account {0}, ATM Debit Suspense account will be updated
     */

    public static final int W_INVALID_2ND_CUR_CASH_ACCT_UPDATE_ATM_DR_SUS_ACCT = 40407531;

    /**
     * <code>W_INVALID_SECOND_CURRENCY_SETTLEMENT_ACCOUNT______ENW_CREDIT_SUSPENSE_ACCOUNT_WILL_BE_UPDATED</code> = Invalid second currency settlement account {0}, ENW Credit Suspense Account will be updated
     */

    public static final int W_INVALID_2ND_CUR_SETLMT_UPDT_ACCT_ENW_CR_SUS_ACCT = 40407530;

    /**
     * <code>W_INVALID_CASH_ACCOUNT______ATM_CREDIT_SUSPENSE_ACCOUNT_WILL_BE_UPDATED</code> = Invalid cash account {0}, ATM Credit Suspense account will be updated
     */

    public static final int W_INVALID_CASH_ACCOUNT_ATM_CR_SUS_ACCT_UPDATED = 40407519;


    /**
     * <code>W_ACCOUNT_____IS_EITHER_STOPPED_OR_CLOSED__SUSPENSE_ACCOUNT_WILL_BE_UPDATED</code> = Account {0} is Either Stopped or Closed, Suspense Account will be updated
     */

    public static final int W_ACCT_EITHER_STOPPED_OR_CLOSED_SUS_ACCT_UPDATED = 40407517;

    /**
     * <code>W_INVALID_DESTINATION_CURRENCY______FORCE_POST_NOT_POSTED</code> = Invalid Destination Currency {0}, force Post not posted
     */

    public static final int W_INVALID_DEST_CURR_FORCE_POST_NOT_POSTED = 40407545;

    /**
     * <code>W_INVALID_SECOND_CURRENCY_CASH_ACCOUNT______ATM_CREDIT_SUSPENSE_ACCOUNT_WILL_BE_UPDATED</code> = Invalid second currency cash account {0}, ATM Credit Suspense account will be updated
     */

    public static final int W_INVALID_2ND_CURR_CASH_ACCT_ATM_CR_SUS_ACCT_UPDTD = 40407529;

    /**
     * <code>W_INVALID_SETTLEMENT_ACCOUNT______ENW_DEBIT_SUSPENSE_ACCOUNT_WILL_BE_UPDATED</code> = Invalid settlement account {0}, ENW Debit Suspense account will be updated
     */

    public static final int W_INVALID_SETLMT_ACCT_ENW_DR_SUS_ACCT_UPDATED = 40407528;

    /**
     * <code>W_INVALID_CASH_ACCOUNT______ATM_DEBIT_SUSPENSE_ACCOUNT_WILL_BE_UPDATED</code> = Invalid cash account {0}, ATM Debit Suspense account will be updated
     */

    public static final int W_INVALID_CASH_ACCT_ATM_DR_SUS_ACCT_UPDATED = 40407527;

    /**
     * <code>W_REVERSAL___ORIGINAL_TRANSACTION_NOT_FOUND__CANNOT_BE_REVERSED</code> = Reversal - Original Transaction not found, cannot be reversed
     */

    public static final int W_REVERSAL_ORIGINAL_TRANS_NOT_FOUND_CANNT_REVERSED = 40407526;

    /**
     * <code>E_ERROR_CONVERTING_AMOUNT</code> = Error converting the amount
     */

    public static final int E_ERROR_CONVERTING_AMOUNT = 40401000;
    /**
     * <code>E_SETTLEMENT_ACCOUNT_CLOSED</code> = Settlement Account is Closed
     */
    public static final int E_SETTLEMENT_ACCOUNT_CLOSED = 40407592;
    /**
     * <code>E_SETTLEMENT_ACCOUNT_STOPED </code> = Settlement Account is Stoped
     */
    public static final int E_SETTLEMENT_ACCOUNT_STOPED = 40407593;
    /**
     * <code>E_CASH_ACCOUNT_CLOSED_SUSPENSE_ACCOUNT_UPDATED</code> = Cash Account is Closed ,Suspense account will be updated
     */
    public static final int E_CASH_ACCOUNT_CLOSED_SUSPENSE_ACCOUNT_UPDATED = 40407594;
    /**
     * <code>E_SETTLEMENT_ACCOUNT_CLOSED</code>= Cash Account is stopped {0}, Suspense account will be updated
     */
    public static final int E_CASH_ACCOUNT_STOPPED_SUSPENSE_ACCOUNT_UPDATED = 40407595;
    /**
     * <code>E_CASH_ACCOUNT_CLOSED</code> = Cash Account is Closed
     */
    public static final int E_CASH_ACCOUNT_CLOSED= 40407576;
    /**
     * <code>E_CASH_ACCOUNT_STOPPED</code> = 7577Cash Account is stopped
     */
    public static final int E_CASH_ACCOUNT_STOPPED = 40407577;
    /**
     * <code>E_INVALID_CASH_ACCOUNT_SUSPENSE_ACCOUNT_UPDATED</code> = Invalid Cash Account {0}, Suspense account will be updated
     */
    public static final int E_INVALID_CASH_ACCOUNT_SUSPENSE_ACCOUNT_UPDATED = 40407578;
    /**
     * <code>E_ACCOUNT_CLOSED</code> = Account is closed {0}
     */
    public static final int E_ACC_CLOSED =  40407566;
    /**
     * <code>E_ACCOUNT_STOPPED</code> = Account is stopped {0}
     */
    public static final int E_ACCOUNT_STOPPED = 40407567;
    /**
     * <code>W_INVALID_CARD_HOLDER_ACCOUNT_SUSP_ACCOUNT_UPDATED</code> = Invalid card holders account {0}, Suspense account will be updated
     */
    public static final int W_INVALID_CARD_HOLDER_ACCOUNT_SUSP_ACCOUNT_UPDATED = 40409523;    
    /**
     * <code>W_INVALID_PURSE_POOL_ACCOUNT_SUSPENSE_ACCOUNT_UPDATED</code> = Invalid Purse account, Suspense account will be updated.
     */
    public static final int W_INVALID_PURSE_POOL_ACC_SUSP_ACC_UPDATED = 40408553;
    /**
     * <code>W_INVALID_SETTL_ACC_SUSP_ACC_UPDATED </code> = Invalid settlement account {0}, Suspense account will be updated
     */
    public static final int W_INVALID_SETTL_ACC_SUSP_ACC_UPDATED  = 40408554;
    /**
     *<code>W_INVALID_CASH_ACC_SUSP_ACC_UPDATED</code> = Invalid cash account {0}, Suspense account will be updated
     */
    public static final int W_INVALID_CASH_ACC_SUSP_ACC_UPDATED = 40408555;
    /**
     * <code>W_INVALID_CARD_HOLDR_ACC_TRANS_POSTED</code> = Invalid card holders account {0},Transaction will be posted
     */
    public static final int W_INVALID_CARD_HOLDR_ACC_TRANS_POSTED = 40408556;
    /**
     * <code>W_SETTL_ACC_CLOSED_SUSP_ACC_UPDATED</code> = Settlement Account is Closed {0}, Suspense Account will be updated.
     */
    public static final int W_SETTL_ACC_CLOSED_SUSP_ACC_UPDATED = 40408557;
    /**
     * <code>W_SETTL_ACC_STOPPED_SUSP_ACC_UPDATED</code> = Settlement Account is Stopped {0}, Suspense Account will be updated.
     */
    public static final int W_SETTL_ACC_STOPPED_SUSP_ACC_UPDATED = 40408558;
    /**
     * <code>E_INVALID_SMARTPURSEPOOL_ACCOUNT</code> = Invalid SmartPursePoolAccount {0)
     */

    public static final int E_INVALID_SMARTPURSEPOOL_ACCOUNT = 40407571;

    /**
     * <code> E_INVALID_CARDHOLDERS_ACCT_SUS_ACCT_UPDATED</code> = Invalid CardHolders Account {0} Suspense Account will be Updated
     */

    public static final int E_INVALID_CARDHOLDERS_ACCT_SUS_ACCT_UPDATED = 40407572;

    /**
     * <code>E_INVALID_SMARTPURSEPOOL_ACCT_SUS_ACCT_UPDATED</code> = Invalid SmartPursePoolAccount {0}, Suspense Account will be updated.
     */

    public static final int E_INVALID_SMARTPURSEPOOL_ACCT_SUS_ACCT_UPDATED = 40407573;

    /**
     * <code> E_INVALID_CARDHOLDERS_ACCOUNT</code> = Invalid CardHolders Account {0}
     */

    public static final int E_INVALID_CARDHOLDERS_ACCOUNT = 40407574;

    /**
     * <code> E_INTERNAL_ACCOUNT_FOR_PSEUDONYM</code> = Invalid Account {0} for Pseudonym.
     */

    public static final int E_INTERNAL_ACCOUNT_FOR_PSEUDONYM = 40407575;
    /**
     * <code> E_CREDIT_SUSPENSE_ACCOUNT_PSEUDONYM</code> = Invalid  Pseudonym.
     */
    public static final int E_INVALID_PSEUDONYM = 40410001;
    /**
     * <code> E_DEBIT_SUSPENSE_ACCOUNT_PSEUDONYM</code> = Invalid  Pseudonym.
     */
    public static final int E_INVALID_CREDIT_TRANSACTION_CODE = 40410002;
    /**
     * <code> E_CREDIT_TRANSACTION_CODE</code> = Invalid  TRANSACTION CODE.
     */
    public static final int E_INVALID_DEBIT_TRANSACTION_CODE = 40410003;
    /**
     * <code> E_DEBIT_TRANSACTION_CODE</code> = Invalid  TRANSACTION CODE.
     */
    public static final int E_INVALID_VALUE_FOR_FORCE_POST = 40410004;
    /**
     * <code> E_VALUE_FOR_FORCE_POST</code> = Invalid value for force post.
     */
    public static final int E_INVALID_VALUE_FOR_WARNING_MSG_FOR_EOD_CHECK = 40410005;
    /**
     * <code> E_VALUE_FOR_WARNING_MSG_FOR_EOD_CHECK</code> = Invalid value for warning msg for EOD check.
     */

    /**
     * <code>E_INVALID_PSEUDONAME</code> = Invalid Pseudonym
     */
    public static final int E_INVALID_PSEUDONAME = 40412000;

    /**
     * <code>E_INVALID_DISPATCHMODE</code> = Invalid Dispatch Mode
     */
    public static final int E_INVALID_DISPATCHMODE = 40412001;// TODO Merge with 126

    /**
     * <code>E_INVALID_TYPE</code> = Invalid Data Type. Datatype has to be Numeric
     */
    public static final int E_INVALID_TYPE = 40412002;// TODO merge with existing

    /**
     * <code>E_INVALID_ACCOUNT_TO_SEND</code> = Invalid Account Type.Should be either Alternate or Account ID
     *
     */
    public static final int E_INVALID_ACCOUNT_TO_SEND = 40412003;

    /**
     * <code>E_INVALID_CRTXNCODE</code> = Invalid Credit Transaction Code
     *
     */
    public static final int E_INVALID_CRTXNCODE = 40412004;

    /**
     * <code>E_INVALID_DRTXNCODE</code> = Invalid Debit Transaction Code
     *
     */
    public static final int E_INVALID_DRTXNCODE = 40412005;

    /**
     * <code>E_NOTIFMENABLED</code> = Currency Not IFM enabled
     *
     */
    public static final int E_NOTIFMENABLED = 40412006;

    /**
     * <code>I_IFMCURRENCYUPDATED</code> = IFM Currency Updated
     *
     */
    public static final int I_IFMCURRENCYUPDATED = 40412007;

    public static final int E_INVALID_CREDIT_TRANS_CODE = 40411000;
    /**
     * <code> E_INVALID_CREDIT_TRANS_CODE</code> = Invalid  TRANSACTION CODE.
     */
    public static final int E_INVALID_DEBIT_TRANS_CODE = 40411001;
    /**
     * <code> E_INVALID_DEBIT_TRANS_CODE</code> = Invalid  TRANSACTION CODE.
     */
    public static final int E_INVALID_SUSPENSE_PSEUDONYM = 40411002;
    /**
     * <code> E_INVALID_SUSPENSE_PSEUDONYM</code> = Invalid  Pseudonym.
     */
    public static final int I_WARNING_MSG_FOR_PSEUDONYM_VALIDATION = 40411003;
    /**
     * <code> I_WARNING_MSG_FOR_PSEUDONYM_VALIDATION</code> =  Warning For Invalid  Pseudonym.
     */
    public static final int E_INVALID_VALUE_FOR_ALLOW_FORCE_POST = 40411004;
    /**
     * <code> E_INVALID_VALUE_FOR_ALLOW_FORCE_POST</code> = Invalid value for force post
     */
    public static final int E_INVALID_ENTRY_FOR_SUPERVISOR_AUTHORIZATION_REQD = 40411005;
    /**
     * <code> E_INVALID_ENTRY_FOR_SUPERVISOR_AUTHORIZATION_REQD</code> = Invalid Entry For Supervisor Authorization
     */
    public static final int E_INVALID_CREDIT_SUSPENSE_PSEUDONYM = 40411006;
    /**
     * <code> E_INVALID_CREDIT_SUSPENSE_PSEUDONYM</code> = Invalid  Pseudonym.
     */
    public static final int E_INVALID_DEBIT_SUSPENSE_PSEUDONYM = 40411007;
    /**
     * <code> E_INVALID_DEBIT_SUSPENSE_PSEUDONYM</code> = Invalid  Pseudonym.
     */
        public static final int E_WARN_USER_FOR_SELECTION = 40411008;

     /**
      * <code> E_WARN_USER_FOR_SELECTION</code> = Waring FOr Selecting User.
      */
        public static final int  E_CREDIT_POSTINGACTION_MISMATCH = 40411035;

        /**
         * <code> E_CREDIT_POSTINGACTION_MISMATCH  </code> = Posting action of Txn. code should be credit

         */
        public static final int E_DEBIT_POSTINGACTION_MISMATCH = 40411036;

        /**
         * <code> E_DEBIT_POSTINGACTIONMISMATCH </code> = Posting action of Txn. code should be dredit

         */
        public static final int E_INVALID_FORCEPOST_SUPERVISOR_AUTHORIZATION = 40411037;

        /**
         * <code> E_INVALID_FORCEPOST_SUPERVISOR_AUTHORIZATION </code> = Invalid Entry For Supervisor Authorization .
         */
        /**
         * <code>W_INVALID_SETTLEMENT_ACCOUNT______ENW_CREDIT_SUSPENSE_ACCOUNT_WILL_BE_UPDATED</code> = Invalid settlement account {0}, ENW Credit Suspense Account will be updated
         */

       
       /**
        * <code>E_THE_CUSTOMER_____DOES_NOT_HAVE_A_SWIFT_SETUP</code> = The customer {0} does not have a SWIFT setup
        */
       public static final int E_THE_CUSTOMER_DOES_NOT_HAVE_A_SWIFT_SETUP = 40407064;
       /**
        * <code>E_ERROR</code> = ={0}
        */
       public static final int E_ERROR = 40409436;
       /**
        * <code>E_TRAVELLERS_CQE_ACCT_CLOSED/code> = Travellers cheque account is closed.
        */
       public static final int E_TRAVELLERS_CQE_ACCT_CLOSED = 40407568;
       /**
        * <code>E_TRAVELLERS_CQE_ACCT_STOPPED/code> = Travellers cheque account is stopped.
        */
       public static final int E_TRAVELLERS_CQE_ACCT_STOPPED = 40407569;
       /**
        * <code>E_TRAVELLERS_CQE_ACCT_STOPPED/code> = Error while getting  SettlementDetails OR COntraAccCustDetails OR Main Acc CUsto Details.
        */
       public static final int E_ERROR_IN_SETTLEMT_OR_CONTRA_OR_MAIN_ACCT_DET = 40407400;





       /** ALMONDE EVENT CODES */

       /**
        * <code>E_ALD_INVALID_RATING_DETAILS/code> = Invalid Rating Details.
        */
       public static final int E_ALD_INVALID_RATING_DETAILS = 40413000;

       /**
        * <code>E_ALD_INVALID_RATING_TYPE/code> = Type of Rating "{0}" Is Not Valid.
        */
       public static final int E_ALD_INVALID_RATING_TYPE = 40413001;

       /**
        * <code>E_ALD_INVALID_CURRENCY/code> = Invalid Currency "{0}".
        */
       public static final int E_ALD_INVALID_CURRENCY = 40413002;

       /**
        * <code>E_ALD_INVALID_CUSTOMER/code> = Invalid Customer Code "{0}".
        */
       public static final int E_ALD_INVALID_CUSTOMER = 40413003;

       /**
        * <code>E_ALD_INVALID_COUNTRY/code> = Invalid Country Code "{0}".
        */
       public static final int E_ALD_INVALID_COUNTRY = 40413004;

       /**
        * <code>E_ALD_INVALID_MITIGANT_CODE/code> = Invalid Mitigants Code "{0}".
        */
       public static final int E_ALD_INVALID_MITIGANT_CODE = 40413005;

       /**
        * <code>E_ALD_INVALID_ASSET_CODE/code> = Invalid Contract Asset Code "{0}".
        */
       public static final int E_ALD_INVALID_ASSET_CODE = 40413006;

       /**
        * <code>E_ALD_INVALID_FX_DEALREF/code> = Invalid Contract FX Deal Ref "{0}".
        */
       public static final int E_ALD_INVALID_FX_DEALREF = 40413007;

       /**
        * <code>E_ALD_INVALID_AGENCY/code> = Invalid Rating Agency Code "{0}".
        */
       public static final int E_ALD_INVALID_AGENCY = 40413008;

       /**
        * <code>E_ALD_INVALID_RATING_TERM/code> = Invalid Rating Term "{0}" for Rating Agency Code "{1}".
        */
       public static final int E_ALD_INVALID_RATING_TERM = 40413009;

       /**
        * <code>E_ALD_INVALID_RATING_VALUE/code> = Invalid Rating Value "{0}" for Rating Agency Code "{1}" And Rating Term "{2}".
        */
       public static final int E_ALD_INVALID_RATING_VALUE = 40413010;

       /**
        * <code>E_ALD_INVALIDLEN_RATING_VALUE/code> = Rating Value cannot be longer than 15 Characters
        */
       public static final int E_ALD_INVALIDLEN_RATING_VALUE = 40413011;

       /**
        * <code>E_ALD_INVALID_RATINGVALUE/code> = Rating Value "{0}" is Invalid
        */
       public static final int E_ALD_INVALID_RATINGVALUE = 40413012;

       /**
        * <code>E_ALD_INVALIDPAIR_TERMVALUE/code> = Rating Value "{0}" for Term "{1}" already exist
        */
       public static final int E_ALD_INVALIDPAIR_TERMVALUE = 40413013;

       /**
        * <code>E_ALD_BLANK_RATINGVALUE/code> = Rating Value cannot be blank
        */
       public static final int E_ALD_BLANK_RATINGVALUE = 40413014;

       /**
        * <code>E_ALD_INVALID_REMOVAL_RATING/code> = Approved Rows cannot be Removed
        */
       public static final int E_ALD_INVALID_REMOVAL_RATING = 40413015;

       /**
        * <code>EVT_ALD_AUTH_DELETE_CUST_GRP/code> = Supervisor authorization required for Deleting Customer Groups.
        */
       public static final int EVT_ALD_AUTH_DELETE_CUST_GRP = 40413016;

       /**
        * <code>EVT_ALD_AUTH_AMEND_CUST_GRP/code> = Supervisor authorization required for Amending Customer Groups.
        */
       public static final int EVT_ALD_AUTH_AMEND_CUST_GRP = 40413017;

       /**
        * <code>EVT_ALD_AUTH_NEW_CUST_GRP/code> = Supervisor authorization required for Creating Customer Groups.
        */
       public static final int EVT_ALD_AUTH_NEW_CUST_GRP = 40413018;

       /**
        * <code>EVT_ALD_AUTH_NEW_RATING/code> = Supervisor authorization required to Add Term/Values to the Rating Agency
        */

       public static final int EVT_ALD_AUTH_NEW_RATING = 40413019;

       /**
        * <code>EVT_ALD_AUTH_AMND_RATING/code> = Supervisor authorization required to Amend Term/Values of the Rating Agency
        */
       public static final int EVT_ALD_AUTH_AMND_RATING = 40413020;

       /**
        * <code>EVT_ALD_AUTH_ALM_CONFIG/code> = Supervisor authorization required for Creating or Amending ALM Configuration
        */
       public static final int EVT_ALD_AUTH_ALM_CONFIG = 40413021;

       /**
        * <code>EVT_ALD_AUTH_BSL2_CONFIG/code> = Supervisor authorization required for Creating or Amending BASEL II Configuration
        */
       public static final int EVT_ALD_AUTH_BSL2_CONFIG = 40413022;

       /**
        * <code>EVT_ALD__AUTH_CAPTURE_RATING/code> = Supervisor authorization required for Capturing Ratings.
        */
       public static final int EVT_ALD__AUTH_CAPTURE_RATING = 40413023;

       /**
        * <code>E_ALD_LOADFAILD_PROP/code> = Failed to load the AlmondeExtract properties file from {0}.
        */
       public static final int E_ALD_LOADFAILD_PROP = 40413024;

       /**
        * <code>E_ALD_NO_ROW_SEL/code> = No Row selected
        */
       public static final int E_ALD_NO_ROW_SEL = 40413025;

       /**
        * <code>E_ALD_INVALID_ENTITY_RATING/code> = Entity code "{0}" can have only one "{1}" rating from "{2}" agency code.
        */
       public static final int E_ALD_INVALID_ENTITY_RATING = 40413026;
       /**
        *
        * <code>E_ALD_INVALID_ENTITY_CURR_RATING/code> = Entity code "{0}" for currency code "{1}" can have only one "{2}" rating from "{3}" agency code.
        */
       public static final int E_ALD_INVALID_ENTITY_CURR_RATING = 40413027;

    /**
     * <code>I_BALANCECHANGED</code> = Balance Changed
     *
     */
    public static final int I_BALANCECHANGED = 40412008;

    /**
     * <code>I_TRANSACTIONDETAILS</code> = Transactions Details
     *
     */
    public static final int I_ACCOUNT_AMEND_BUSINESS_EVENT = 40411020;

    /**
     * <code>I_CUSTOMER_ENABLE_BUSINESS_EVENT</code> = Customer Enablement
     *
     */
    public static final int I_CUSTOMER_ENABLE_BUSINESS_EVENT = 40411015;

    /**
     * <code>I_CUSTOMER_DISABLE_BUSINESS_EVENT</code> = Customer Disablement
     *
     */
    public static final int I_CUSTOMER_DISABLE_BUSINESS_EVENT = 40411016;

    /**
     * <code>EVT_TRANSACTION_DETAILS</code> = Transaction Details
     *
     */
    public static final int EVT_TRANSACTION_DETAILS = 40412009;

    /**
     * <code>I_IBIPRODUCTCURRENCYENABLEDSUCCESS</code> = Abstract Product Currency Enablement
     * Successful
     *
     */
    public static final int I_IBIPRODUCTCURRENCYENABLEDSUCCESS = 40412013;

    /**
     * <code>I_CUSTOMERBATCHSUCCESSFUL</code> = Customer Batch {0} Successful
     *
     */
    public static final int I_CUSTOMERBATCHSUCCESSFUL = 40412014;

    /**
     * <code>M_AUTORIZATION_FOR_IBICURRENCY_ENABLEMENT</code> = Supervisor Authorization required
     * for Currency Configuration
     *
     */
    public static final int M_AUTORIZATION_FOR_IBICURRENCY_ENABLEMENT = 40412015;

    /**
     * <code>I_NOTIBIPRODUCT</code> = Selected Product Not IBI Product
     *
     */
    public static final int I_NOTIBIPRODUCT = 40412016;

    /**
     * <code>I_NOTIBICURRENCY</code> = Selected Currency Not IBI Currency
     *
     */
    public static final int I_NOTIBICURRENCY = 40412017;
    /**
     * <code>EVT_ACCOUNT_DETAILS</code> = Raised by the customer is enabled for Account details
     *
     */
    public static final int EVT_ACCOUNT_DETAILS = 40412008;
    /**
     * <code>E_LICENESE_COUNTER</code> = Raised when the LICENESE Counter exceeds.
     *
     */
    public static final int E_LICENESE_COUNTER = 40412020;

    /**
     * <code>EVT_BALANCE_CHANGED</code> = Raised by the Internet Banking feature when an account
     * balance is modified through posting engine
     *
     */
    public static final int EVT_BALANCE_CHANGED = 40412010;
    /**
     * <code>EVT_BULK_TRANSACTION_DETAILS</code> = Raised when bulk transaction details need to be
     * sent to external system
     *
     */
    public static final int EVT_BULK_TRANSACTION_DETAILS = 40205106;
    /**
     * <code>I_ACCOUNT_CLOSE_EVENT</code> = Raised after account closure is completed.
     *
     */
    public static final int I_ACCOUNT_CLOSE_EVENT = 40411039;
    /**
     * <code>EVT_ACCOUNT_CREATION</code> = Raised after an account is successfully created.
     *
     */
    public static final int EVT_ACCOUNT_CREATION = 40107317;

    /**
     * <code>I_AUTORIZATION_FOR_FMACCOUNTTYPE MAINTENANCE</code> = Supervisor Authorization Required For Insert and Delete IFM Account Type
     *
     */
    public static final int E_INV_DEVICE_ID_CASH_ACCT_NOT_FOUND = 40407560;

    public static final int I_AUTHORIZATION_INSERT_IFMACCOUNTTYPE = 40412039;

    public static final int I_AUTHORIZATION_DELETE_IFMACCOUNTTYPE = 40412040;
    public static final int I_AUTHORIZATION_AMEND_IFMACCOUNTTYPE = 40412041;
    public static final int I_ACCOUNTTYPE_EXIST = 40412042;
    public static final int I_ACCOUNTTYPE_NOTEXIST = 40412043;
    public static final int I_ACCOUNTTYPE_EXIST_FOR_PRODUCT = 40412044;

    public static final int E_DIRECTORY_PATH_NOT_FOUND=40412556;


    /**
     * <code>E_CHEQUEBOOKREQUEST_PROCESSED</code> = To Validate whether Cheque Book request is already processed.
     *
     */
    public static final int E_CHEQUEBOOKREQUEST_PROCESSED = 40412045;


    /**
     * <code>I_NO_RECORDS_FOUND</code> = This is used when no records found.
     *
     */
    public static final int I_NO_RECORDS_FOUND = 40412052;

       public static final int EVT_EXCHANGERATE_CREATE = 40411025;
       /**
        * <code> EVT_EXCHANGERATE_CREATE </code> = Create and Update of ExchangeRate  .
        */
       public static final int W_NO_ACCOUNT_OF_THIS_CUSTOMER = 40411023;
       /**
        * <code> W_NO_ACCOUNT_OF_THIS_CUSTOMER </code> = No account available for the customer .
        */
       public static final int EVT_POSITION_UPDATE_EVENT = 40410021;
       /**
        * <code> EVT_POSITION_UPDATE_EVENT</code> = Position Update Event Handeler.
        */
    public static final int E_INV_DEVICE_ID_SUS_ACCT_UPDATED = 40407561;
       
       
       public static final int EVT_NOSTRO_UPDATE_EVENT = 40410022;
       /**
        * <code> EVT_NOSTRO_UPDATE_EVENT</code> = Nostro Update Event Handeler.
        */
       public static final int I_ACCOUNT_NUMBER_REFFERED_FOR_AUTHORIZATION = 40410011;
       /**
        * <code> I_ACCOUNT_NUMBER_REFFERED_FOR_AUTHORIZATION</code> = Supervisor Approval for Nostro account creation/amendment.
        */
       public static final int EVT_ACCOUNT_NUMBER_REFERRED = 40410012;
       /**
        * <code>I_ACCOUNT_NUMBER_REFERRED</code> = Business event raise to generate nostro xml for creation/amendment
        */ 
       public static final int E_INVALID_NOSTRO_ACCOUNT = 40410013;
       
       /**
        * <code>E_INVALID_NOSTRO_ACCOUNT</code> = {0} not found
        */
       
       public static final int E_ACCOUNT_IS_ENABLED_FOR_OTHER_INTERFACE = 40410014;
       /**
        * <code>E_ACCOUNT_IS_ENABLED_FOR_OTHER_INTERFACE</code> = Disable account from interfaces in order to stop/close.
        */
      
       public static final int E_INVALID_CURRENCY = 40410023;
       /**
        * <code>E_INVALID_CURRENCY</code> = Invalid Currency Code
        */
       public static final int E_CUSTOMER_NOT_ENABLED = 40410024;
       /**
        * <code>E_CUSTOMER_NOT_ENABLED</code> = Customer Not Enabled for OPICS Interface
        */
       public static final int E_DEALFLAG_INVALID = 40410025;
       /**
        * <code>E_DEALFLAG_INVALID</code> = Deal Flag is not Purchase.
        */
       public static final int E_OVERALL_TREASURY_LIMIT_NOTEXIST = 40410026;
       /**
        * <code>E_OVERALL_TREASURY_LIMIT_NOTEXIST</code> = Overall Treasury Limit Does Not Exist.
        */ 
       public static final int E_CUSTOMER_TREASURY_LIMIT_NOTEXIST = 40410027;
       /**
        * <code>E_CUSTOMER_TREASURY_LIMIT_NOTEXIST</code> = Overall Treasury Limit Does Not Exist for Customer.
        */
       public static final int E_CLOSEOFF_KICKOFF = 40410037;
       /**
        * <code> E_CLOSEOFF_KICKOFF</code> = Approve the UB Closeoff/EOD Initiation.
        */
       public static final int E_OPX_EXCHANGERATE_AUTHORIZATION = 40410038;
       /**
        * <code> E_OPX_EXCHANGERATE_AUTHORIZATION</code> = Approve the UB Exchange Rate process.
        */

       /**
        * <code> E_OPX_OVERALL_TREASURY_LIMIT_AUTH</code> = Overall Treasury Limit Number has to be a number in the range 1 to 999999999.
        */
       public static final int E_OPX_OVERALL_TREASURY_LIMIT_AUTH = 40410039;
       /**
        * <code> E_INVALID_CONTRA_PSEUDONYM</code> = Invalid Contra Pseudonym.
        */
       public static final int E_INVALID_CONTRA_PSEUDONYM = 40410040;
       
       /**
        * <code>W_ACCT_PASSORD_PROTECTED_SUS_ACCT_UPDATED</code> = Account {0} is Password Protected. Suspense Account will be updated.
        */
       public static final int W_ACCT_PASSORD_PROTECTED_SUS_ACCT_UPDATED = 40407559;
       /**
        * <code>W_INV_MER_ACCT_SUSP_ACCT_UPDT</code> = Invalid Merchant account {0}, Posting to Merchant Credit Suspense account.
        */
       public static final int W_INV_MER_ACCT_SUSP_ACCT_UPDT = 40409526;
       /**
        * <code>W_INV_MER_ACCT_DEB_SUSP_ACCT_UPDT</code> = Invalid Merchant account {0}, Posting to Merchant Debit Suspense account.
        */
       public static final int W_INV_MER_ACCT_DEB_SUSP_ACCT_UPDT = 40409527;
       /**
        * <code> E_INVALID_CONTRA_SUSPENSE_PSEUDONYM</code> = Invalid Contra Suspense Account Pseudonym .
        * */
       public static final int E_INVALID_CONTRA_SUSPENSE_PSEUDONYM = 40411081;
       /**
        * <code>E_ACCOUNT_DORMANT</code> = Account {0} is Dormant.
        */
       public static final int E_ACCOUNT_DORMANT = 40409528;
       /**
        * <code>W_ACC_DORMANT_SUS_ACC_UPDTD</code> = Account {0} is Dormant. Suspense Account will be updated.
        */
       public static final int W_ACC_DORMANT_SUS_ACC_UPDTD = 40409529;
    
       /**
        * <code>E_INV_FILE_LOCATION_UB14</code> = Invalid file location.
        */
       public static final int E_INV_FILE_LOCATION_UB14 = 40409530;
       
       /**
        * <code>I_POSTED_TO_SUSPENSE_ACCOUNT</code> = Transaction posted to the Suspense Account configured.
        */
       public static final int I_POSTED_TO_SUSPENSE_ACCOUNT = 40411047;
       
       /**
        * <code>I_MISSEDLEG_SUSP_POST_UB14</code> = Transaction posted to the Suspense Account configured because of missing Legs.
        */
       public static final int I_MISSEDLEG_SUSP_POST_UB14 = 40422010;
       
       /**
        * <code>E_INVALID_BATCH_FILE_LOCATION_UB</code> = Invalid Batch File Location: {0}. Set the proper Batch File Location in MMK Module Config
        */
       public static final int E_INVALID_BATCH_FILE_LOCATION_UB = 40409472;
       
       /**
        * <code>E_STMT_DAY_MONTHLY_SHOULD_BE_BTW_1_AND_31_UB</code> = Statement day for Monthly Should be between 1 and 31.
        */

       public static final int E_STMT_DAY_MONTHLY_SHOULD_BE_BTW_1_AND_31_UB = 40420529;
       
       /**
        * <code>E_STMT_DAY_QUARTERLY_SHOULD_BE_BTW_1_AND_31_UB</code> = Statement day for Quarterly Should be between 1 and 31.
        */

       public static final int E_STMT_DAY_QUARTERLY_SHOULD_BE_BTW_1_AND_31_UB = 40420530;
       
       /**
        * <code>E_STMT_DAY_HALF_YEARLY_SHOULD_BE_BTW_1_AND_31_UB</code> = Statement day for Half-Yearly Should be between 1 and 31.
        */

       public static final int E_STMT_DAY_HALF_YEARLY_SHOULD_BE_BTW_1_AND_31_UB = 40420531;

       /**
        * <code>E_STMT_DAY_YEARLY_SHOULD_BE_BTW_1_AND_31_UB</code> = Statement day for Yearly Should be between 1 and 31.
        */

       public static final int E_STMT_DAY_YEARLY_SHOULD_BE_BTW_1_AND_31_UB = 40420532;

       /**
        * <code>E_FREQUENCY_IN_MINUTES_TO_BE_GREATER_THAN_30_UB</code> = Frequency in minutes to be greater than 30.
        */

       public static final int E_FREQUENCY_IN_MINUTES_TO_BE_GREATER_THAN_30_UB = 40420533;
       /**
        * <code>E_TXT_FILES_SUPPORTED_UB</code> = Only Text Files are Supported.
        */

       public static final int E_TXT_FILES_SUPPORTED_UB = 40409490;
       /**
        * <code>E_INVALID_NCCUPLOAD_FORMAT_UB</code> = File Format Error.
        */

       public static final int E_INVALID_NCCUPLOAD_FORMAT_UB = 40409491;
       
       /**
        * <code>E_INVALID_NCCUPLOAD_FORMAT_UB</code> = File Format Error.
        */

       public static final int E_CONNECTION_MERIDIAN_SERVER_FAILED_UB = 40409537;
       
       public static final int  E_CONFIGURATION_ALREADY_EXISTS_UB = 40409728;

       /**
        * <code>I_PROCESS_SUCCESS</code> = Card Status uploaded successfully.
        */
       public static final int I_PROCESS_SUCCESS = 40422504;
       
       /**
        * <code>E_INVALID_CARD_ACTION</code> = Invalid Card Action Provided.
        */
       public static final int E_INVALID_CARD_ACTION = 20010016;
       
       /**
   	 * <code>E_BIC_NOT_AVAILABLE_UB16</code> =Error while generating
   	 * the message
   	 */

	public static final int E_BIC_NOT_AVAILABLE_UB16 = 40409731;

    /**
	 * <code>I_SWT_ORDER_CUST_IDENTIFIER_UB15</code> =Ordering Customer Identifier Code is not correct
	 */
	
	public static final int I_SWT_ORDER_CUST_IDENTIFIER_UB15 = 40401057;
	
	/**
	 * <code>E_SI_INVALID_IBAN_LENGTH</code> =IBAN Length is incorrect for the Country
	 */
	   
	   public static final int E_SI_INVALID_IBAN_LENGTH = 40421541;
	   
	   /**
		 * <code>E_SI_INVALID_COUNTRY_CODE</code> =Not a valid ISO Country Code for given IBAN
		 */
	   
	   public static final int E_SI_INVALID_COUNTRY_CODE = 40421540;
	   
	   /**
		 * <code>E_SI_SLASHES_NOT_ALLOWED</code> =Slashes are not allowed in {0} Party Identifier
		 */
	   
	    public static final int E_SI_SLASHES_NOT_ALLOWED = 40421539;
	    
	    /**
		 * <code>E_SI_PARTY_IDENTIFIER_CANNOT_BE_BLANK</code> =Enter the value of the selected {0} Party Identifier; you cannot enter a value in the blank fields
		 */
	    
	   public static final int E_SI_PARTY_IDENTIFIER_CANNOT_BE_BLANK  = 40421542;
  
	   /**
		 * <code>E_IDENTIFIER_CODE_ALREADY_EXIST_UB</code> = Identifier Code already
		 * exist.
		 */
		public static final int E_IDENTIFIER_CODE_ALREADY_EXIST_UB = 40421551;

		/**
		 * <code>E_IDENTIFIER_CODE_DOES_NOT_EXIST_UB</code> = Identifier Code does
		 * not exist.
		 */
		public static final int E_IDENTIFIER_CODE_DOES_NOT_EXIST_UB = 40421552;

		public static final int E_FORMAT_OF_BENEFICIARY_ADDRESS_LINE_IS_INVALID = 40421557;
		
		public static final int E_NUMERIC_VALUE_SHOULD_BE_BETWEEN_1_TO_3 = 40421558;

		public static final int E_ADDRESS_3_CANNOT_EXIST_WITHOUT_ADDR_2 = 40421559;

		public static final int E_DATA_NOT_PRESENT_AFTER_SLASH = 40421560;
       
		public static final int I_TRANSACTION_EXTRENAL_PRODUCT = 40430030;
		
		public static final int I_TRANSACTION_NOSTRO = 40430031;
		
		public static final int I_TRANSACTION_CURRENCY_POSITION = 40430032;
		
		public static final int E_INTRADAY_SCHEDULE_CANNOT_BE_EMPTY = 40409735;
		
		public static final int E_INTRADAY_SCHEDULE_CROSSING_BUSINESSDAY = 40409736;
		
		public static final int E_PROPOSEDDATE_CANNOT_BE_PAST_OR_CURRENT_DATE = 40243029;
}

