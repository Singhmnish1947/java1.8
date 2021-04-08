/* ********************************************************************************
 *  Copyright(c)2007  Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 * 
 * $Id: MGM_ExceptionHelper.java,v 1.6 2008/08/12 20:15:26 vivekr Exp $
 *  
 * $Log: MGM_ExceptionHelper.java,v $
 * Revision 1.6  2008/08/12 20:15:26  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.4.4.1  2008/07/03 17:55:18  vivekr
 * Moved from Dublin CVS Head (For BF1.01 package restructuring)
 *
 * Revision 1.7  2008/06/19 09:26:33  arun
 * FatomUtils' usage of getBankFusionException changed to call BankFusionException directly
 *
 * Revision 1.6  2008/06/16 15:18:45  arun
 * UB Refactoring -
 * 1. Formatted/Organized imports
 * 2. BODefinitionException, InvalidExtensionPointException and references removed
 * 3. Removed ServerManager deprecated methods/variables
 * 4. BankfusionPropertyAccess removed - changed to BankfusionPropertySupport
 * 5. Exception Handling refactoring
 * 6. General Refactoring
 *
 * Revision 1.5  2008/06/12 10:50:10  arun
 *  RIO on Head
 *
 * Revision 1.4  2008/02/15 05:45:12  nileshk
 * Account validation exception code added
 *
 * Revision 1.3  2007/09/30 10:12:46  nileshk
 * Error code added
 *
 * Revision 1.2  2007/09/30 10:08:58  nileshk
 * Error code added
 *
 * Revision 1.1  2007/09/27 11:58:46  nileshk
 * Added for 3.3a MoneyGram release
 *
 * 
 */

package com.misys.ub.moneygram;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.events.CommonsEventCodes;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;

/**
 * This class takes the error code and throws the corresponding MoneyGram exception.
 * @author nileshk
 *
 */
public class MGM_ExceptionHelper {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	/**
	 * Logger defined.
	 */
	private transient final static Log logger = LogFactory.getLog(MGM_ExceptionHelper.class.getName());

	/**
	 * Constructor
	 * @param env
	 */
	public MGM_ExceptionHelper() {
	}

	/**
	 * This method takes the errorcode and throws the corresponding MoneyGram exception.
	 * @param errorCode
	 * @param environment
	 * @
	 */
	public void throwMoneyGramException(int errorCode, BankFusionEnvironment environment) {
		switch (errorCode) {
			case 100:
				/*throw new BankFusionException(9200, new Object[] { "REQUIRED FIELD MISSING" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_REQUIRED_FIELD_MISSING, new Object[] { "REQUIRED FIELD MISSING" }, new HashMap(), environment);
			case 101:
				/*throw new BankFusionException(9201, new Object[] { "RECEIVER ADDRESS REQUIRED" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_RECEIVER_ADDRESS_REQUIRED, new Object[] { "RECEIVER ADDRESS REQUIRED" }, new HashMap(), environment);
			case 102:
				//throw new BankFusionException(9202, new Object[] { "PHOTO ID REQUIRED" }, logger, environment);
				EventsHelper.handleEvent(ChannelsEventCodes.E_PHOTO_ID_REQUIRED , new Object[] { "PHOTO ID REQUIRED" }, new HashMap(), environment);
			case 103:
				//throw new BankFusionException(9203, new Object[] { "LEGAL ID REQUIRED" }, logger, environment);
				EventsHelper.handleEvent(ChannelsEventCodes.E_LEGAL_ID_REQUIRED  , new Object[] { "LEGAL ID REQUIRED" }, new HashMap(), environment);
				
			case 105:
				/*throw new BankFusionException(9204, new Object[] { "RECEIVER 2ND LAST NAME REQUIRED" },
						logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_RECEIVER_ND_LAST_NAME_REQUIRED , new Object[] { "RECEIVER 2ND LAST NAME REQUIRED" }, new HashMap(), environment);
			case 106:
				/*throw new BankFusionException(9205, new Object[] { "RECEIVER PHONE REQUIRED" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_RECEIVER_PHONE_REQUIRED ,new Object[] { "RECEIVER PHONE REQUIRED" }, new HashMap(), environment);
				
			case 107:
				/*throw new BankFusionException(9206,
						new Object[] { "FREQUENT CUSTOMER CARD NUMBER REQUIRED" }, logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_FREQUENT_NUMBER_REQUIRED ,new Object[] { "FREQUENT CUSTOMER CARD NUMBER REQUIRED" }, new HashMap(), environment);
			case 108:
				/*throw new BankFusionException(9207, new Object[] { "AGENCY ID REQUIRED" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_AGENCY_ID_REQUIRED  ,new Object[] { "AGENCY ID REQUIRED" }, new HashMap(), environment);
				
			case 109:
				/*throw new BankFusionException(9208, new Object[] { "AN AMOUNT FIELD IS REQUIRED" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_AN_AMOUNT_FIELD_IS_REQUIRED,new Object[] { "AN AMOUNT FIELD IS REQUIRED" }, new HashMap(), environment);
			case 110:
				/*throw new BankFusionException(9209, new Object[] { "THIRD PARTY INFORMATION IS REQUIRED" },
						logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_THIRD_PARTY_INFORMATION_IS_REQUIRED,new Object[] { "THIRD PARTY INFORMATION IS REQUIRED" }, new HashMap(), environment);
			case 111:
				/*throw new BankFusionException(9210, new Object[] { "A STATE OR COUNTRY CODE IS REQUIRED" },
						logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_A_STATE_OR_COUNTRY_REQUIRED, new Object[] { "A STATE OR COUNTRY CODE IS REQUIRED" }, new HashMap(), environment);
			case 112:
				/*throw new BankFusionException(9211, new Object[] { "RECEIVER'S NAME IS REQUIRED" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_RECEIVER_S_NAME_IS_REQUIRED, new Object[] { "RECEIVER'S NAME IS REQUIRED" }, new HashMap(), environment);
			case 200:
				/*throw new BankFusionException(9212, new Object[] { "DISALLOWED FIELD SET" }, logger,
						environment);*/
				
				EventsHelper.handleEvent(ChannelsEventCodes.E_DISALLOWED_FIELD_SET, new Object[] { "DISALLOWED FIELD SET" }, new HashMap(), environment);
			case 201:
				/*throw new BankFusionException(9213, new Object[] { "TEST QUESTION/ANSWER NOT ALLOWED" },
						logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_TEST_NOT_ALLOWED, new Object[] { "TEST QUESTION/ANSWER NOT ALLOWED" }, new HashMap(), environment);
			case 202:
				/*throw new BankFusionException(9214,
						new Object[] { "MESSAGEFIELD1 OR MESSAGEFIELD2 NOT ALLOWED" }, logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_MESSAGEFIELD_NOT_ALLOWED, new Object[] { "MESSAGEFIELD1 OR MESSAGEFIELD2 NOT ALLOWED" }, new HashMap(), environment);
			case 203:
				/*throw new BankFusionException(9215, new Object[] { "DIRECTIONS FIELDS NOT ALLOWED" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_DIRECTIONS_FIELDS_NOT_ALLOWED, new Object[] { "DIRECTIONS FIELDS NOT ALLOWED" }, new HashMap(), environment);
			case 204:
				/*throw new BankFusionException(9216, new Object[] { "RECEIVER ADDRESS NOT ALLOWED" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_RECEIVER_ADDRESS_NOT_ALLOWED,new Object[] { "RECEIVER ADDRESS NOT ALLOWED" }, new HashMap(), environment);
			case 205:
				/*throw new BankFusionException(9217, new Object[] { "ONLY ONE AMOUNT FIELD IS ALLOWED" },
						logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_ONLY_ONE_AMOUNT_FIELD_IS_ALLOWED, new Object[] { "ONLY ONE AMOUNT FIELD IS ALLOWED" }, new HashMap(), environment);
			case 300:
				/*throw new BankFusionException(9218, new Object[] { "LIMIT/EDIT RULE FAILURE" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_LIMIT_EDIT_RULE_FAILURE, new Object[] { "LIMIT/EDIT RULE FAILURE" }, new HashMap(), environment);
			case 301:
				/*throw new BankFusionException(9219, new Object[] { "MAX AMOUNT EXCEEDED" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_MAX_AMOUNT_EXCEEDED, new Object[] { "MAX AMOUNT EXCEEDED" }, new HashMap(), environment);
			case 302:
			/*	throw new BankFusionException(9220, new Object[] { "DAILY LIMIT EXCEEDED" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_DAILY_LIMIT_EXCEEDED, new Object[] { "DAILY LIMIT EXCEEDED" }, new HashMap(), environment);
			case 303:
				/*throw new BankFusionException(9221, new Object[] { "INVALID STATE FOR GIVEN COUNTRY" },
						logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_STATE_FOR_GIVEN_COUNTRY, new Object[] { "INVALID STATE FOR GIVEN COUNTRY" }, new HashMap(), environment);
			case 304:
				/*throw new BankFusionException(9222, new Object[] { "OUTSIDE OF STORE BUSINESS HOURS" },
						logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_OUTSIDE_OF_STORE_BUSINESS_HOURS, new Object[] { "OUTSIDE OF STORE BUSINESS HOURS" }, new HashMap(), environment);
			case 305:
				/*throw new BankFusionException(9223, new Object[] { "INVALID COUNTRY CODE" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_COUNTRY_CODE, new Object[] { "INVALID COUNTRY CODE" }, new HashMap(), environment);
			case 306:
				/*throw new BankFusionException(9224, new Object[] { "PRODUCT NOT AVAILABLE" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_PRODUCT_NOT_AVAILABLE, new Object[] { "PRODUCT NOT AVAILABLE" }, new HashMap(), environment);
			case 307:
				//throw new BankFusionException(9225, new Object[] { "FEE NOT AVAILABLE" }, logger, environment);
				EventsHelper.handleEvent(ChannelsEventCodes.E_FEE_NOT_AVAILABLE, new Object[] { "FEE NOT AVAILABLE" }, new HashMap(), environment);
			case 308:
				/*throw new BankFusionException(9226, new Object[] { "WRONG MONEYGRAM TYPE" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_WRONG_MONEYGRAM_TYPE, new Object[] { "WRONG MONEYGRAM TYPE" }, new HashMap(), environment);
			case 309:
				//throw new BankFusionException(9227, new Object[] { "WRONG API VERSION" }, logger, environment);
				EventsHelper.handleEvent(ChannelsEventCodes.E_WRONG_API_VERSION, new Object[] { "WRONG API VERSION" }, new HashMap(), environment);
			case 311:
				/*throw new BankFusionException(9228, new Object[] { "INVALID CURRENCY CODE" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_CURRENCY_CODE_TR, new Object[] { "INVALID CURRENCY CODE" }, new HashMap(), environment);
			case 313:
				//throw new BankFusionException(9229, new Object[] { "MESSAGE US ONLY" }, logger, environment);
				EventsHelper.handleEvent(ChannelsEventCodes.E_MESSAGE_US_ONLY,  new Object[] { "MESSAGE US ONLY" }, new HashMap(), environment);
			case 314:
				/*throw new BankFusionException(9230, new Object[] { "INVALID RECEIVE COUNTRY" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_RECEIVE_COUNTRY, new Object[] { "INVALID RECEIVE COUNTRY" }, new HashMap(), environment);
			case 315:
				/*throw new BankFusionException(9231,
						new Object[] { "TRANSACTION TYPE NOT ALLOWED FOR RECEIVE COUNTRY" }, logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_TRANSACTION_TYPE_NOT_COUNTRY, new Object[] { "TRANSACTION TYPE NOT ALLOWED FOR RECEIVE COUNTRY" }, new HashMap(), environment);
			case 316:
				/*throw new BankFusionException(9232, new Object[] { "CUSTOMER IS NOT FOUND" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_DISALLOWED_FIELD_SET, new Object[] { "CUSTOMER IS NOT FOUND" }, new HashMap(), environment);
			case 317:
				/*throw new BankFusionException(9233, new Object[] { "INVALID TIME ZONE NAME" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_TIME_ZONE_NAME, new Object[] { "INVALID TIME ZONE NAME" }, new HashMap(), environment);
			case 400:
				//throw new BankFusionException(9234, new Object[] { "SECURITY FAILURE" }, logger, environment);
				EventsHelper.handleEvent(ChannelsEventCodes.E_SECURITY_FAILURE, new Object[] { "SECURITY FAILURE" }, new HashMap(), environment);
			case 401:
				/*throw new BankFusionException(9235, new Object[] { "BAD UNIT PROFILE ID" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_BAD_UNIT_PROFILE_ID, new Object[] { "BAD UNIT PROFILE ID" }, new HashMap(), environment);
			case 402:
				//throw new BankFusionException(9236, new Object[] { "BAD TOKEN" }, logger, environment);
				EventsHelper.handleEvent(ChannelsEventCodes.E_BAD_TOKEN, new Object[] { "BAD TOKEN" }, new HashMap(), environment);
			case 403:
				/*throw new BankFusionException(9237, new Object[] { "UNIT HAS BEEN DE-AUTHORIZED" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_UNIT_HAS_BEEN_DE_AUTHORIZED, new Object[] { "UNIT HAS BEEN DE-AUTHORIZED" }, new HashMap(), environment);
			case 404:
				/*throw new BankFusionException(9238,
						new Object[] { "SECURITY CHECK FAILED,POS DE-AUTHORIZED" }, logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_SECURITY_CHECK_FAILED_AUTHORIZED, new Object[] { "SECURITY CHECK FAILED,POS DE-AUTHORIZED" }, new HashMap(), environment);
			case 405:
				/*throw new BankFusionException(9239, new Object[] { "MUST DO CHECK-IN OR INITIAL SETUP" },
						logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_MUST_DO_CHECK_IN_OR_INITIAL_SETUP, new Object[] { "MUST DO CHECK-IN OR INITIAL SETUP" }, new HashMap(), environment);
			case 406:
				/*throw new BankFusionException(9240, new Object[] { "TRANSACTION NOT ALLOWED" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_TRANSACTION_NOT_ALLOWED, new Object[] { "TRANSACTION NOT ALLOWED" }, new HashMap(), environment);
			case 407:
				//throw new BankFusionException(9241, new Object[] { "INVALID PASSWORD" }, logger, environment);
				EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_PASSWORD, new Object[] { "INVALID PASSWORD" }, new HashMap(), environment);
			case 500:
				/*throw new BankFusionException(9242, new Object[] { "GETINITIALSETUP/PROFILESETUP FAILURE" },
						logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_GETINITIALSETUP_PROFILESETUP_FAILURE, new Object[] { "GETINITIALSETUP/PROFILESETUP FAILURE" }, new HashMap(), environment);
			case 501:
				/*throw new BankFusionException(9243, new Object[] { "ERROR GETTING POS DATA" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_ERROR_GETTING_POS_DATA, new Object[] { "ERROR GETTING POS DATA" }, new HashMap(), environment);
			case 502:
				/*throw new BankFusionException(9244, new Object[] { "PROFILE SETUP IS INCOMPLETE" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_PROFILE_SETUP_IS_INCOMPLETE, new Object[] { "PROFILE SETUP IS INCOMPLETE" }, new HashMap(), environment);
			case 503:
				/*throw new BankFusionException(9245, new Object[] { "INITIAL SETUP PASSWORD FAILURE" },
						logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes. E_INITIAL_SETUP_PASSWORD_FAILURE , new Object[] { "INITIAL SETUP PASSWORD FAILURE" }, new HashMap(), environment);
			case 504:
				//throw new BankFusionException(9246, new Object[] { "INVALID POS" }, logger, environment);
				EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_POS, new Object[] { "INVALID POS" }, new HashMap(), environment);
			case 505:
				//throw new BankFusionException(9247, new Object[] { "INVALID POS TYPE" }, logger, environment);
				EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_POS_TYPE, new Object[] { "INVALID POS TYPE" }, new HashMap(), environment);
			case 506:
				/*throw new BankFusionException(9248, new Object[] { "INVALID AGENT ID OR POS NUMBER" },
						logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_AGENT_ID_OR_POS_NUMBER, new Object[] { "INVALID AGENT ID OR POS NUMBER" }, new HashMap(), environment);
			case 600:
				/*throw new BankFusionException(9249, new Object[] { "DATA INVALID OR NOT FOUND" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_DATA_INVALID_OR_NOT_FOUND, new Object[] { "DATA INVALID OR NOT FOUND" }, new HashMap(), environment);
			case 601:
				//throw new BankFusionException(9250, new Object[] { "INVALID VALUE" }, logger, environment);
				EventsHelper.handleEvent(ChannelsEventCodes.E_NOT_VALID_VALUE, new Object[] { "INVALID VALUE" }, new HashMap(), environment);
			case 602:
				/*throw new BankFusionException(9251,
						new Object[] { "FEE OR RATE HAS CHANGED SINCE QUOTE WAS ISSUED" }, logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_FEE_OR_RATE_HAS_CHANGED, new Object[] { "FEE OR RATE HAS CHANGED SINCE QUOTE WAS ISSUED" }, new HashMap(), environment);
			case 603:
				/*throw new BankFusionException(9252, new Object[] { "INVALID CHECK NUMBER" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_CHECK_NUMBER, new Object[] { "DISALLOWED FIELD SET" }, new HashMap(), environment);
			case 604:
				/*throw new BankFusionException(9253, new Object[] { "INVALID REFERENCE NUMBER" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_REFERENCE_NUMBER, new Object[] { "INVALID REFERENCE NUMBER" }, new HashMap(), environment);
			case 605:
				/*throw new BankFusionException(9254,
						new Object[] { "SEND REVERSAL/CANCEL MUST BE REQUESTED SAME DAY" }, logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_SEND_REVERSAL_CANCEL_MUST_BE_REQUESTED_SAME_DAY, new Object[] { "SEND REVERSAL/CANCEL MUST BE REQUESTED SAME DAY" }, new HashMap(), environment);
			case 606:
				/*throw new BankFusionException(9255,
						new Object[] { "SEND REVL/CANC IS ONLY ALLWD FOR STD MG DELIV OPT" }, logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_SEND_REVL_CANC_IS_ONLY_ALLWD, new Object[] { "SEND REVL/CANC IS ONLY ALLWD FOR STD MG DELIV OPT" }, new HashMap(), environment);
			case 607:
				/*throw new BankFusionException(9256,
						new Object[] { "TRANS IS AN EXPRESSPAYMENT AND CANNOT BE REVERSED" }, logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_DISALLOWED_FIELD_SET, new Object[] { "SEND REVL/CANC IS ONLY ALLWD FOR STD MG DELIV OPT" }, new HashMap(), environment);
			case 608:
				/*throw new BankFusionException(9257,
						new Object[] { "REVERSING AGENT IS NOT THE SAME AS SEND AGENT" }, logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_DISALLOWED_FIELD_SET, new Object[] { "REVERSING AGENT IS NOT THE SAME AS SEND AGENT" }, new HashMap(), environment);
			case 609:
				/*throw new BankFusionException(9258, new Object[] { "TRANSACTION NOT IN SEND STATUS" },
						logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_TRANSACTION_NOT_IN_SEND_STATUS, new Object[] { "TRANSACTION NOT IN SEND STATUS" }, new HashMap(), environment);
			case 610:
				/*throw new BankFusionException(9259,
						new Object[] { "REVERSL AMT AND FEE MUST EQUAL SEND AMT AND FEE" }, logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_REVERSL_AMT_AND_FEE_MUST_EQUAL, new Object[] { "REVERSL AMT AND FEE MUST EQUAL SEND AMT AND FEE" }, new HashMap(), environment);
			case 611:
				/*throw new BankFusionException(9260, new Object[] { "INVALID CONFIRMATION NUMBER" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_CONFIRMATION_NUMBER, new Object[] { "INVALID CONFIRMATION NUMBER" }, new HashMap(), environment);
			case 612:
				/*throw new BankFusionException(9261, new Object[] { "AMOUNT MUST BE GREATER THAN 0" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_AMOUNT_MUST_BE_GREATER_THAN, new Object[] { "AMOUNT MUST BE GREATER THAN 0" }, new HashMap(), environment);
			case 613:
				/*throw new BankFusionException(9262,
						new Object[] { "AMOUNT MUST NOT EXCEED COUNTRY RECEIVE LIMIT" }, logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_AMOUNT_MUST_NOT_EXCEED_COUNTRY_LIMIT, new Object[] { "AMOUNT MUST NOT EXCEED COUNTRY RECEIVE LIMIT" }, new HashMap(), environment);
			case 614:
				/*throw new BankFusionException(9263, new Object[] { "AMOUNT MUST NOT EXCEED SENDING LIMIT" },
						logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_AMOUNT_MUST_NOT_EXCEED_SENDING_LIMIT, new Object[] { "AMOUNT MUST NOT EXCEED SENDING LIMIT" }, new HashMap(), environment);
			case 615:
				/*throw new BankFusionException(9264, new Object[] { "INVALID ACCOUNT NUMBER" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_ACCOUNT_NUMBER_INVALID, new Object[] { "INVALID ACCOUNT NUMBER" }, new HashMap(), environment);
			case 616:
				/*throw new BankFusionException(9265, new Object[] { "CUSTOMER IS NOT FOUND" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_CUSTOMER_IS_NOT_FOUND,new Object[] { "CUSTOMER IS NOT FOUND" }, new HashMap(), environment);
			case 617:
				//throw new BankFusionException(9266, new Object[] { "NO AGENTS FOUND" }, logger, environment);
				EventsHelper.handleEvent(ChannelsEventCodes.E_NO_AGENTS_FOUND, new Object[] { "NO AGENTS FOUND" }, new HashMap(), environment);
			case 618:
				/*throw new BankFusionException(9267, new Object[] { "NO CITIES WITH AGENTS FOUND" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_NO_CITIES_WITH_AGENTS_FOUND, new Object[] { "NO CITIES WITH AGENTS FOUND" }, new HashMap(), environment);
			case 619:
				/*throw new BankFusionException(9268, new Object[] { "INVALID AREA CODE AND PREFIX" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_AREA_CODE_AND_PREFIX, new Object[] { "INVALID AREA CODE AND PREFIX" }, new HashMap(), environment);
			case 620:
				/*throw new BankFusionException(9269, new Object[] { "TAX ID # SHOULD BE 9 DIGITS" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_TAX_ID_SHOULD_BE_DIGITS_, new Object[] { "TAX ID # SHOULD BE 9 DIGITS" }, new HashMap(), environment);
			case 621:
				/*throw new BankFusionException(9270,
						new Object[] { "US SOCIAL SECURITY NUMBER SHOULD BE 9 DIGITS" }, logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_SOCIAL_SECURITY_NUMBER_SHOULD_BE_DIGITS, new Object[] { "US SOCIAL SECURITY NUMBER SHOULD BE 9 DIGITS" }, new HashMap(), environment);
			case 631:
				//throw new BankFusionException(9271, new Object[] { "INVALID ID" }, logger, environment);
				EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_ID, new Object[] { "INVALID ID" }, new HashMap(), environment);
			case 700:
				/*throw new BankFusionException(9272, new Object[] { "MONEYGRAM SERVER ERROR" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_MONEYGRAM_SERVER_ERROR,  new Object[] { "MONEYGRAM SERVER ERROR" }, new HashMap(), environment);
			case 701:
				/*throw new BankFusionException(9273,
						new Object[] { "EMPTY RESPONSE RECEIVED FROM MONEYGRAM HOST" }, logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_EMPTY_RESPONSE_RECEIVED_FROM_MONEYGRAM_HOST, new Object[] { "EMPTY RESPONSE RECEIVED FROM MONEYGRAM HOST" }, new HashMap(), environment);
			case 702:
				/*throw new BankFusionException(9274, new Object[] { "ERROR RECEIVED FROM MONEYGRAM HOST" },
						logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_ERROR_RECEIVED_FROM_MONEYGRAM_HOST, new Object[] { "ERROR RECEIVED FROM MONEYGRAM HOST" }, new HashMap(), environment);
			case 703:
				/*throw new BankFusionException(9275,
						new Object[] { "COUNTRY NOT PARTICIPATING WITH THE MG NETWORK" }, logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_COUNTRY_NOT_PARTICIPATING_WITH_THE_MG, new Object[] { "COUNTRY NOT PARTICIPATING WITH THE MG NETWORK" }, new HashMap(), environment);
			case 704:
				/*throw new BankFusionException(9276, new Object[] { "COUNTRY IS TEMPORARILY OUT OF SERVICE" },
						logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_COUNTRY_IS_TEMPORARILY_OUT_OF_SERVICE, new Object[] { "COUNTRY IS TEMPORARILY OUT OF SERVICE" }, new HashMap(), environment);
			case 705:
				/*throw new BankFusionException(9277,
						new Object[] { "THERE ARE NO AGENTS IN THE DESTINATION COUNTRY" }, logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_THERE_ARE_NO_AGENTS_IN_THE_DESTINATION_COUNTRY, new Object[] { "THERE ARE NO AGENTS IN THE DESTINATION COUNTRY" }, new HashMap(), environment);
			case 706:
				/*throw new BankFusionException(9278,
						new Object[] { "PLZ CALL CUST SERV CENTR TO COMP THIS TRANS" }, logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_PLZ_CALL_CUST_SERV_CENTR, new Object[] { "PLZ CALL CUST SERV CENTR TO COMP THIS TRANS" }, new HashMap(), environment);
			case 707:
				/*throw new BankFusionException(9279,
						new Object[] { "PLZ CALL CUST SERV CENTR TO COMP THIS TRANS" }, logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_PLZ_CALL_CUST_SERV_CENTR, new Object[] { "PLZ CALL CUST SERV CENTR TO COMP THIS TRANS" }, new HashMap(), environment);
			case 708:
				/*throw new BankFusionException(9280,
						new Object[] { "PLZ CALL CUST SERV CENTR TO COMP THIS TRANS" }, logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_PLZ_CALL_CUST_SERV_CENTR, new Object[] { "PLZ CALL CUST SERV CENTR TO COMP THIS TRANS" }, new HashMap(), environment);
			case 709:
				/*throw new BankFusionException(9281,
						new Object[] { "AGENT UNABLE TO RECEIVE THIS TRANSACTION" }, logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_AGENT_UNABLE_TO_RECEIVE_THIS_TRANSACTION, new Object[] { "AGENT UNABLE TO RECEIVE THIS TRANSACTION" }, new HashMap(), environment);
			case 710:
				/*throw new BankFusionException(9282, new Object[] { "AGENT CURRENCY IS INCORRECT" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_AGENT_CURRENCY_IS_INCORRECT, new Object[] { "AGENT CURRENCY IS INCORRECT" }, new HashMap(), environment);
			case 711:
			/*	throw new BankFusionException(9283, new Object[] { "SENDS TO CUBA NOT ALLOWED" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_SENDS_TO_CUBA_NOT_ALLOWED, new Object[] { "SENDS TO CUBA NOT ALLOWED" }, new HashMap(), environment);
			case 712:
				/*throw new BankFusionException(9284, new Object[] { "POSSIBLE DUPLICATE TRANSACTION" },
						logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_POSSIBLE_DUPLICATE_TRANSACTION, new Object[] { "POSSIBLE DUPLICATE TRANSACTION" }, new HashMap(), environment);
			case 713:
				/*throw new BankFusionException(9285, new Object[] { "REQUESTE ALLOWED FOR US ONLY" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_REQUESTED_ALLOWED_FOR_US_ONLY_, new Object[] { "REQUESTE ALLOWED FOR US ONLY" }, new HashMap(), environment);
			case 714:
				/*throw new BankFusionException(9286, new Object[] { "MONEYGRAM INTERNAL SYSTEM ERROR" },
						logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_MONEYGRAM_INTERNAL_SYSTEM_ERROR, new Object[] { "MONEYGRAM INTERNAL SYSTEM ERROR" }, new HashMap(), environment);
			case 900:
				//throw new BankFusionException(9287, new Object[] { "INTERNAL ERROR" }, logger, environment);
				EventsHelper.handleEvent(ChannelsEventCodes.E_INTERNAL_ERROR, new Object[] { "INTERNAL ERROR" }, new HashMap(), environment);
			case 901:
				//throw new BankFusionException(9288, new Object[] { "INTERNAL ERROR" }, logger, environment);
				EventsHelper.handleEvent(ChannelsEventCodes.E_INTERNAL_ERROR, new Object[] { "INTERNAL ERROR" }, new HashMap(), environment);
			case 902:
				/*throw new BankFusionException(9289, new Object[] { "INTERNAL ERROR" }, logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_INTERNAL_ERROR, new Object[] { "INTERNAL ERROR" }, new HashMap(), environment);
			case 006:
				/*throw new BankFusionException(9290, new Object[] { "ERROR CONNECTING TO MONEYGRAM SERVER" },
						logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_ERROR_CONNECTING_TO_MONEYGRAM_SERVER, new Object[] { "ERROR CONNECTING TO MONEYGRAM SERVER" }, new HashMap(), environment);
			case 014:
				/*throw new BankFusionException(9291, new Object[] { "ERROR DURING PARSING XML " }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_ERROR_DURING_PARSING_XML, new Object[] { "ERROR DURING PARSING XML " }, new HashMap(), environment);
			case 053:
				/*throw new BankFusionException(9292, new Object[] { "STATE CODE IS MANDATORY " }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_STATE_CODE_IS_MANDATORY, new Object[] { "STATE CODE IS MANDATORY " }, new HashMap(), environment);
			case 645:
				/*throw new BankFusionException(9296,
						new Object[] { "INVALID SCENARIO -- FEE AMT GREATER THAN SEND AMT" }, logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_SCENARIO_FEE_AMT, new Object[] { "INVALID SCENARIO -- FEE AMT GREATER THAN SEND AMT" }, new HashMap(), environment);
				//Account validation error code
			case 9351:
				/*throw new BankFusionException(9351, new Object[] { "REQUIRED FOR POSTING" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_PASSWORD_REQD_FOR_POSTING, new Object[] { "REQUIRED FOR POSTING" }, new HashMap(), environment);
			case 9352:
				/*throw new BankFusionException(9352, new Object[] { "ACCOUNT STOPPED" }, logger, environment);*/
				EventsHelper.handleEvent(CommonsEventCodes.E_ACCOUNT_STOPPED, new Object[] { "ACCOUNT STOPPED" }, new HashMap(), environment);
			case 9353:
				/*throw new BankFusionException(9353, new Object[] { "STOPPED - PASSWORD REQUIRED" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_STOPPED_PASSWORD_REQUIRED, new Object[] { "STOPPED - PASSWORD REQUIRED" }, new HashMap(), environment);
			case 9354:
				/*throw new BankFusionException(9354, new Object[] { "NO DEBIT POSTINGS ALLOWED" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_NO_DEBIT_POSTINGS_ALLOWED, new Object[] { "NO DEBIT POSTINGS ALLOWED" }, new HashMap(), environment);
			case 9355:
				/*throw new BankFusionException(9355,
						new Object[] { "NO DEBIT POSTINGS ALLOWED - PASSWORD REQUIRED" }, logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_NO_DEBIT_POSTINGS_ALLOWED_PASSWORD_REQUIRED, new Object[] { "NO DEBIT POSTINGS ALLOWED - PASSWORD REQUIRED" }, new HashMap(), environment);
			case 9356:
				/*throw new BankFusionException(9356, new Object[] { "NO CREDIT POSTINGS ALLOWED" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_NO_CREDIT_POSTINGS_ALLOWED_HERE, new Object[] { "NO CREDIT POSTINGS ALLOWED" }, new HashMap(), environment);
				
			case 9357:
				/*throw new BankFusionException(9357,
						new Object[] { "NO CREDIT POSTINGS ALLOWED - PASSWORD REQUIRED" }, logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_NO_CREDIT_POSTINGS_ALLOWED, new Object[] { "NO CREDIT POSTINGS ALLOWED - PASSWORD REQUIRED" }, new HashMap(), environment);
			case 9358:
			/*	throw new BankFusionException(9358, new Object[] { "ACCOUNT PASSWORD RESTRICTED" }, logger,
						environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_ACCOUNT_PASSWORD_RESTRICTED, new Object[] { "ACCOUNT PASSWORD RESTRICTED" }, new HashMap(), environment);
			case 9359:
				//throw new BankFusionException(9359, new Object[] { "A/C DORMANT" }, logger, environment);
				EventsHelper.handleEvent(ChannelsEventCodes.E_A_C_DORMANT,new Object[] { "A/C DORMANT" }, new HashMap(), environment);
				
			case 9360:
				/*throw new BankFusionException(9360,
						new Object[] { "INVALID TRANSACTION - CURRENT STATUS IS DORMANT" }, logger, environment);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_TRANSACTION_CURRENT_STATUS, new Object[] { "INVALID TRANSACTION - CURRENT STATUS IS DORMANT" }, new HashMap(), environment);

			default:
				/*throw new BankFusionException(9287, new Object[] { "INTERNAL ERROR" }, logger, environment);*/
			EventsHelper.handleEvent(ChannelsEventCodes.E_INTERNAL_ERROR, new Object[] { "INTERNAL ERROR" }, new HashMap(), environment);
		}
	}

}
