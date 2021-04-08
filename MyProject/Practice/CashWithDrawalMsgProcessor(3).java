package com.misys.ub.atm;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.axiom.om.util.UUIDGenerator;

import com.misys.bankfusion.subsystem.microflow.runtime.impl.MFExecuter;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.bo.refimpl.IBOATMSettlementAccount;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ATM_CashWithDrawalMsgProcessor;

import bf.com.misys.ub.types.atm.UB_ATM_Financial_Details;
import bf.com.misys.ub.types.atm.postingvector.UB_Atm_PostingMessage;
import bf.com.misys.ub.types.atm.postingvector.UB_Atm_PostingMessages;
import bf.com.misys.ub.types.iso8583.UB_Financial_Details;

public class CashWithDrawalMsgProcessor extends AbstractUB_ATM_CashWithDrawalMsgProcessor {

	public CashWithDrawalMsgProcessor(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) {
		// Variables of Start Fatom

		UB_ATM_Financial_Details atmPosting = getF_IN_AtmPosting();
		UB_Financial_Details financialDetails = atmPosting.getFinancialDetails();
		String AccountId = getF_IN_AccountId();
		boolean ForcePost = isF_IN_ForcePost();
		Timestamp DateTime = getF_IN_DATETIME();
		UB_Atm_PostingMessages AtmPostingMsgValues = getF_IN_AtmPostingMsgValues();// SRV58
		UB_Atm_PostingMessages AtmPostingMsgValues1 = getF_IN_AtmPostingMsgValues1();// SRV57
		UB_Atm_PostingMessages Cr_AtmMessage_P4 = getF_IN_Cr_AtmMessage();// P4
		UB_Atm_PostingMessages Cr_AtmMessages = getF_IN_Cr_AtmMessages();// P91
		UB_Atm_PostingMessages Dr_AtmMessages = getF_IN_Dr_AtmMessages();// P91
		UB_Atm_PostingMessages Dr_AtmMessage_P4 = getF_IN_Dr_AtmMessage();// P4
		UB_Atm_PostingMessages Cr_Message_P89 = getF_IN_Cr_Message();// P89
		UB_Atm_PostingMessages Dr_Message_P89 = getF_IN_Dr_Message();// P89
		UB_Atm_PostingMessage RepeatingComplexTypeInput = getF_IN_RepeatingComplexTypeInput();// SRV58

		// Variables :

		// Placeholder 44 Variables :
		String AtmTransType_P44 = "";
		String CrAccount_P44 = "";
		String DrAccount_P44 = "";
		String ErrorCode_P44 = "";
		String ErrorMessage_P44 = "";
		boolean flag_P44 = false;
		boolean isErrorExists_P44 = false;
		boolean ErrorWithOutForcePost_P44 = false;
		String getAtmTransactionType_P44 = "";
		String ErrorCode_SRV52 = "";

		// P 84
		boolean processRecon_P84 = false;

		// Placeholder 86 Variables
		String Currency_P86 = "";
		String Currency_P84 = "";

		String ErrorCode_SRV59 = "";

		// PlaceHolder Vars:
		BigDecimal amountRecon_P117 = new BigDecimal(0);
		String reconCurrency_P117 = "";

		String cardHolderBillingCurrency_P118 = "";
		BigDecimal cardHolerBillingAmount_P118 = new BigDecimal(0);
		String ISOCURRENCYCODE_SRV82 = "";
		String Narrative_SRV79 = "";
		String FromCurrencyCode_SRV56 = "";
		String TxnCode_SRV56 = "";
		String ToCurrencyCode_SRV56 = "";
		String toCurrencyCode_P93 = "";
		String toCurrencyCode_P94 = "";
		String ACCOUNTNUM_SRV55 = "";
		String ACCOUNTNUM_SRV54 = "";
		String AtmTransType_SRV53 = "";
		String errorCode_P73 = "";
		String errorMsg_P73 = "";
		String AtmTransType_SRV59 = "";
		String AtmTxnCode_SRV59 = atmPosting.getTxnCode();// data from atmPosting--txnCode
		String CreditAcc_P18 = "";
		String DebitAcc_P18 = "";
		String CreditAcc_P19 = "";
		String DebitAcc_P19 = "";
		String CreditAcc_P21 = "";
		String DebitAcc_P21 = atmPosting.getAccountIdentification1().getAccountNumber1();// data from atmPosting --
																							// accIdent1--accnum1
		String CrAccount_P78 = "";
		String DebitAcc_P78 = atmPosting.getAccountIdentification1().getAccountNumber1();// data from atmPosting --
																							// accIdent1--accnum1
		String CurrCode_SRV22 = "";
		String Id_SRV22 = "";
		String CurrCode_SRV23 = "";
		String Id_SRV23 = financialDetails.getAcquiringInstitutionId();// data from
																		// atmPosting--fin--acquiringInstitutionId
		String CurrCode_SRV99 = "";
		String cardIssuerID_SRV15 = "";
		String cardIssuerFIID_P60 = atmPosting.getCardIssuerData().getCardIssuerFIID();// atm-cardIssuerData--cardIssuerFIID
		String cardLogicalNetwork_P60 = atmPosting.getCardIssuerData().getCardLogicalNetwork();// atm-cardIssuerData--cardLogicalNetwork
		boolean isErrorExists_P60 = false;
		String cardIssuerId_P60 = "";
		boolean isErrorWithOutForcePost_P60 = false;

		String DebitAccount_P78 = atmPosting.getAccountIdentification1().getAccountNumber1();

		String CurrCode_SRV52 = financialDetails.getCurrencyCode();// atm-fin-currencyCode
		String ID_SRV52 = financialDetails.getCardAcceptorTerminalId();// atm-fin-cardAccetorTerminalId
		boolean IsAtmTxn_SRV52 = false;

		String CurrCode_SRV77 = financialDetails.getCurrencyCode();// atm-fin-currencyCode
		String Id_SRV77 = financialDetails.getCardAcceptorId();// atm-fin-cardAccetorId

		String AccountID_SRV96 = AccountId;// Strt stp - AccountID
		String NessageType_SRV96 = financialDetails.getMessageHeader().getMessageType();// atm-fin-messageheader-messagetype
		String ErrorCode_SRV96 = "";
		String ErrorMessage_SRV96 = "";
		String RepeatAdviceMsg_P84 = "";
		boolean flag_P84 = true;
		String reconCrr_P123 = "";
		String reconCrr_P124 = "";
		String Currency_P85 = financialDetails.getCurrencyCode();// atmPosting.finDtls.currencyCode()
		boolean processRecon_P110 = true;
		boolean processRecon_P121 = false;

		String NUMERICCURRENCYCODE_SRV82 = financialDetails.getCardHoldrBillingCurrency();// atm-fin-cardholdrbillingcurrency
		String ISOCURRENCYCODE_P110 = "";
		String NUMERICCURRENCYCODE_SRV111 = financialDetails.getAmountReconCurrency();// atm-fin-amountReconcurrency
		String ATMDEVICEID_75 = financialDetails.getCardAcceptorTerminalId();// atm-fin-cardAccetorTerminalId
		boolean output_P65 = false;
		String acquirerId_SRV12 = financialDetails.getAcquiringInstitutionId();// atm-fin-acquiringInstitutionId

		// PlaceHolder 41
		String AccountType_P41 = financialDetails.getProcessingCode().getAccountType();// atm-fin-proccessingCode-accountType
		boolean booleanTrue_P41 = true;
		String CreditSign_P41 = "+";
		String DebitSign_P41 = "-";
		String FinancialPosting_P41 = "N";
		String MessageType_P41 = financialDetails.getMessageHeader().getMessageType();// atm-fin-messageheader-msgtype
		String productIndicator_P41 = financialDetails.getMessageHeader().getProductIndicator();// atm-fin-messageheader-productIndicator

		// End fatom variables:
		String AtmTransType_Ef = "";
		String AtmTxnCode_Ef = "";
		String AtmTxnDesc_Ef = "";
		String ErrorCode_Ef = "";
		String ErrorMessage_Ef = "";
		String ExternalAccountId_Ef = "";
		boolean ExternalFlag_Ef = false;
		String External_Cr_Ef = "";
		String External_Dr_Ef = "";
		String MisTxnCode_Ef = "";
		String accountCurrency_Ef = "";
		String contraAccount_Ef = "";
		boolean processRecon_Ef = false;

		UUIDGenerator abc = new UUIDGenerator();
		abc.getUUID();

		// All Maps References used :

		Map inputParamsAquirerIdBelongsToOwnBankIMD_SRV12 = new HashMap(); // 52
		Map<String, Object> outputParamsAquirerIdBelongsToOwnBankIMD_SRV12; // 54

		Map inputParamsGetTransactionNarrative_SRV79 = new HashMap();
		Map<String, Object> outputParamsGetTransactionNarrative_SRV79;

		Map inputParamsGetExternalSettlementAcc77 = new HashMap<>();// 80
		Map<String, Object> outputParamsGetExternalSettlementAcc77; // 87

		Map inputParamsGetATMSettlementCashACC52 = new HashMap<>();// 101
		Map<String, Object> outputParamsGetATMSettlementCashACC52; // 109

		Map inputParamsCardIssuerIdBelongsToOwnBankIMD_S15 = new HashMap<>();// 131
		Map<String, Object> outputParamsCardIssuerIdBelongsToOwnBankIMD_S15;// 135

		Map inputParamsGetExternalSettlementAcc22 = new HashMap<>();// 153
		Map<String, Object> outputParamsGetExternalSettlementAcc22;// 161

		Map inputParamsEnquireCurrencyCodeFromNumericCode111 = new HashMap<>();// 192
		Map<String, Object> outputParamsEnquireCurrencyCodeFromNumericCode111;// 195

		Map inputParamsEnquireCurrencyCodeFromNumericCode82 = new HashMap<>();// 211
		Map<String, Object> outputParamsEnquireCurrencyCodeFromNumericCode82;// 218

		Map inputParamsValidateAccount_SRV96 = new HashMap<>();// 261
		Map<String, Object> outputParamsValidateAccount_SRV96;// 268

		Map inputParamsGetATMDebitSuspAcc99 = new HashMap<>();// 288
		Map<String, Object> outputParamsGetATMDebitSuspAcc99;// 292

		Map inputParamsGetExternalSettlementAcc23 = new HashMap<>();// 315
		Map<String, Object> outputParamsGetExternalSettlementAcc23;// 325

		Map inputParamsFetchAtmTxnCodeDtls_SRV59 = new HashMap<>();// 325
		Map<String, Object> outputParamsFetchAtmTxnCodeDtls_SRV59;// 360

		Map inputParamsTransactionNarrative53 = new HashMap<>();// 376
		Map<String, Object> outputParamsTransactionNarrative53;// 383

		Map inputParamsFetchAccountService54 = new HashMap<>();// 392 //54
		Map<String, Object> outputParamsFetchAccountService54;// 396 54

		Map inputParamsFetchAccountService55 = new HashMap<>();// 398 55
		Map<String, Object> outputParamsFetchAccountService55;// 400 55

		Map inputParamsGetExcahgeRateDtlsOnTxnCode56 = new HashMap<>();// 425
		Map<String, Object> outputParamsGetExcahgeRateDtlsOnTxnCode56;// 430

		Map inputParamsGetTransactionNarrative_SRV53 = new HashMap<>();// 441
		Map<String, Object> outputParamsGetTransactionNarrative_SRV53;// 445

		Map inputParamsCreateRepeatingComplexType_SRV57 = new HashMap<>();// 479
		Map<String, Object> outputParamsCreateRepeatingComplexType_SRV57;// 480

		Map inputParamsCreateRepeatingComplexType_SRV58 = new HashMap<>();// 485
		Map<String, Object> outputParamsCreateRepeatingComplexType_SRV58;// 486

		// main code :

		// PlaceHolder 41
		IsAtmTxn_SRV52 = booleanTrue_P41;
		Cr_Message_P89.setMESSAGEID(getUniqueID());
		Cr_Message_P89.setMESSAGETYPE(FinancialPosting_P41);
		Cr_Message_P89.setSIGN(CreditSign_P41);
		Cr_Message_P89.setTRANSACTIONID(getUniqueID());
		Dr_Message_P89.setMESSAGEID(getUniqueID());
		Dr_Message_P89.setMESSAGETYPE(FinancialPosting_P41);
		Dr_Message_P89.setSIGN(DebitSign_P41);
		Dr_Message_P89.setTRANSACTIONID(getUniqueID());

		Cr_AtmMessage_P4.setMESSAGEID(getUniqueID());
		Cr_AtmMessage_P4.setMESSAGETYPE(FinancialPosting_P41);
		Cr_AtmMessage_P4.setSIGN(CreditSign_P41);
		Cr_AtmMessage_P4.setTRANSACTIONID(getUniqueID());
		Dr_AtmMessage_P4.setMESSAGEID(getUniqueID());
		Dr_AtmMessage_P4.setMESSAGETYPE(FinancialPosting_P41);
		Dr_AtmMessage_P4.setSIGN(DebitSign_P41);
		Dr_AtmMessage_P4.setTRANSACTIONID(getUniqueID());

		// UB_ATM_AquirerIdBelongsToOwnBankIMD_SRV12

		inputParamsAquirerIdBelongsToOwnBankIMD_SRV12.put("acquirerId", acquirerId_SRV12);
		outputParamsAquirerIdBelongsToOwnBankIMD_SRV12 = MFExecuter.executeMF("UB_ATM_AquirerIdBelongsToOwnBankIMD_SRV",
				env, inputParamsAquirerIdBelongsToOwnBankIMD_SRV12);
		output_P65 = (boolean)outputParamsAquirerIdBelongsToOwnBankIMD_SRV12.get("finalAcquirerIdStatus");

		// PlaceHolder 65

		// ConditionalStep 13
		if (true == output_P65) // CS 13
		{
			// SettlementAccount BOD Call
			IBOATMSettlementAccount outputATMSettlementAccount = (IBOATMSettlementAccount) BankFusionThreadLocal
					.getPersistanceFactory().findByPrimaryKey(IBOATMSettlementAccount.BONAME, ATMDEVICEID_75, false);

			// ConditionalStep 76
			if (("").equals(ATMDEVICEID_75)) // CS 76
			{
				// INP = ""; true
				// UB_ATM_GetExternalSettlementAcc77
				inputParamsGetExternalSettlementAcc77.put("CurrCode", financialDetails.getCreditCurrencyCode());
				inputParamsGetExternalSettlementAcc77.put("ForcePost", ForcePost);
				inputParamsGetExternalSettlementAcc77.put("Id", financialDetails.getCardAcceptorId());

				outputParamsGetExternalSettlementAcc77 = MFExecuter.executeMF("UB_ATM_GetExternalSettlementAcc", env,
						inputParamsGetExternalSettlementAcc77);
				CrAccount_P78 = (String) outputParamsGetExternalSettlementAcc77.get("ExternalSettlementAcc");

				// PlaceHolder 78
				String AtmTransType_P78 = "02";
				AtmTransType_P44 = AtmTransType_P78;
				CrAccount_P44 = CrAccount_P78;
				DrAccount_P44 = DebitAccount_P78;

			} else // CS 76
			{
				// INP != ""; false
				// UB_ATM_GetATMSettlementCashACC52
				inputParamsGetATMSettlementCashACC52.put("CurrCode", financialDetails.getCurrencyCode());
				inputParamsGetATMSettlementCashACC52.put("ForcePost", ForcePost);
				inputParamsGetATMSettlementCashACC52.put("ID", financialDetails.getCardAcceptorTerminalId());
				inputParamsGetATMSettlementCashACC52.put("IsATMTxn", IsAtmTxn_SRV52);
				inputParamsGetATMSettlementCashACC52.put("Credit", true);
				inputParamsGetATMSettlementCashACC52.put("debit", false);

				outputParamsGetATMSettlementCashACC52 = MFExecuter.executeMF("UB_ATM_GetATMSettlementCashACC", env,
						inputParamsGetATMSettlementCashACC52);
				ErrorCode_Ef = (String) outputParamsGetATMSettlementCashACC52.get("ErrorCode");
				ErrorCode_SRV52 = ErrorCode_Ef;
				ErrorMessage_Ef = (String) outputParamsGetATMSettlementCashACC52.get("ErrorMessage");
				CreditAcc_P19 = (String) outputParamsGetATMSettlementCashACC52.get("ATMCashAcc");
				CreditAcc_P21 = (String) outputParamsGetATMSettlementCashACC52.get("ATMCashAcc");

				// Placeholder60
				isErrorExists_P60 = (ErrorCode_SRV52 != "") ? true : false;
				cardIssuerId_P60 = cardIssuerFIID_P60 + cardLogicalNetwork_P60;
				isErrorWithOutForcePost_P60 = ((isErrorExists_P60 == true) && (ForcePost == false)) ? true : false;
				Id_SRV22 = cardIssuerId_P60;
				cardIssuerID_SRV15 = cardIssuerId_P60;

				if (false == isErrorWithOutForcePost_P60) // CS 63
				{
					// INP == false;
					// UB_ATM_CardIssuerIdBelongsToOwnBankIMD_S15
					inputParamsCardIssuerIdBelongsToOwnBankIMD_S15.put("cardIssuerID", cardIssuerID_SRV15);
					inputParamsCardIssuerIdBelongsToOwnBankIMD_S15.put("ExternalLoroIndicator", "");

					outputParamsCardIssuerIdBelongsToOwnBankIMD_S15 = MFExecuter.executeMF(
							"UB_ATM_CardIssuerIdBelongsToOwnBankIMD_SRV", env,
							inputParamsCardIssuerIdBelongsToOwnBankIMD_S15);
					boolean issuerIDFlag_SRV15 = (boolean) outputParamsCardIssuerIdBelongsToOwnBankIMD_S15
							.get("issuerIDFlag");

					// ConditionStep 16
					if (true == issuerIDFlag_SRV15) // CS 16
					{
						// INP == true;
						// PlaceHolder 21
						// --> P44
						String AtmTransType_P21 = "01";
						AtmTransType_P44 = AtmTransType_P21;
						CrAccount_P44 = CreditAcc_P21;
						DrAccount_P44 = DebitAcc_P21;
					}

					else // CS16
					{
						// INP == false;
						// UB_ATM_GetExternalSettlementAcc22
						inputParamsGetExternalSettlementAcc22.put("CurrCode", financialDetails.getCurrencyCode());
						inputParamsGetExternalSettlementAcc22.put("ForcePost", ForcePost);
						inputParamsGetExternalSettlementAcc22.put("Id", cardIssuerId_P60);
						inputParamsGetExternalSettlementAcc22.put("credit", false);
						inputParamsGetExternalSettlementAcc22.put("debit", true);

						outputParamsGetExternalSettlementAcc22 = MFExecuter.executeMF("UB_ATM_GetExternalSettlementAcc",
								env, inputParamsGetExternalSettlementAcc22);

						ErrorCode_P44 = (String) inputParamsGetExternalSettlementAcc22.get("ErrorCode");
						ErrorMessage_P44 = (String) inputParamsGetExternalSettlementAcc22.get("ErrorMessage");
						External_Dr_Ef = (String) inputParamsGetExternalSettlementAcc22.get("ExternalSettlementAcc");
						DebitAcc_P19 = (String) inputParamsGetExternalSettlementAcc22.get("ExternalSettlementAcc");

						// PlaceHolder 19
						// --> P44
						String AtmTransType_P19 = "03";
						AtmTransType_P44 = AtmTransType_P19;
						CrAccount_P44 = CreditAcc_P19;
						DrAccount_P44 = DebitAcc_P19;

					}
				}

				else // CS 63
				{
					// End Fatom
				}
			}
		}

		else // CS 13
		{
			// false

			// ConditionStep 109
			if (financialDetails.getAmountRecon().equals(0)) // CS 109
			{
				// BigDecimal INP != 0;
				// EnquireCurrencyCodeFromNumericCode111
				inputParamsEnquireCurrencyCodeFromNumericCode111.put("NUMERICCURRENCYCODE",
						financialDetails.getAmountReconCurrency());
				outputParamsEnquireCurrencyCodeFromNumericCode111 = MFExecuter.executeMF(
						"EnquireCurrencyCodeFromNumericCode", env, inputParamsEnquireCurrencyCodeFromNumericCode111);

				ISOCURRENCYCODE_P110 = (String) outputParamsEnquireCurrencyCodeFromNumericCode111
						.get("ISOCURRENCYCODE");
				reconCrr_P123 = (String) outputParamsEnquireCurrencyCodeFromNumericCode111.get("ISOCURRENCYCODE");
				reconCurrency_P117 = (String) outputParamsEnquireCurrencyCodeFromNumericCode111.get("ISOCURRENCYCODE");
				// PlaceHolder 110
				processRecon_P121 = processRecon_P110;
				processRecon_P84 = processRecon_P110;
			}

			// else CS 109
			// BigDecimal INP == 0;
			// EnquireCurrencyCodeFromNumericCode82
			inputParamsEnquireCurrencyCodeFromNumericCode82.put("NUMERICCURRENCYCODE",
					financialDetails.getCardHoldrBillingCurrency());

			outputParamsEnquireCurrencyCodeFromNumericCode82 = MFExecuter.executeMF(
					"EnquireCurrencyCodeFromNumericCode", env, inputParamsEnquireCurrencyCodeFromNumericCode82);
			toCurrencyCode_P93 = (String) outputParamsEnquireCurrencyCodeFromNumericCode82.get("ISOCURRENCYCODE");
			reconCrr_P124 = (String) outputParamsEnquireCurrencyCodeFromNumericCode82.get("ISOCURRENCYCODE");
			CurrCode_SRV99 = (String) outputParamsEnquireCurrencyCodeFromNumericCode82.get("ISOCURRENCYCODE");
			ISOCURRENCYCODE_SRV82 = (String) outputParamsEnquireCurrencyCodeFromNumericCode82.get("ISOCURRENCYCODE");
			cardHolderBillingCurrency_P118 = (String) outputParamsEnquireCurrencyCodeFromNumericCode82
					.get("ISOCURRENCYCODE");
			Cr_Message_P89.setTXNCURRENCYCODE(
					(String) outputParamsEnquireCurrencyCodeFromNumericCode82.get("ISOCURRENCYCODE"));

			// ConditionStep 83
			if (ISOCURRENCYCODE_SRV82 == financialDetails.getCreditCurrencyCode()) // CS 83
			{
				// INP1 == INP2 --> true
				// PlaceHolder 85
				Currency_P86 = Currency_P85;
				// --> P86
			}

			else // CS 83
			{
				// INP1 != INP2 --> false
				// PlaceHolder 121

				// ConditionStep 122
				if (processRecon_P121 == true) // CS 122
				{
					// INP == true
					// PlaceHolder 123
					Currency_P84 = reconCrr_P123;
				} else // 122
				{
					// INP ==false
					// PlaceHolder 124
					Currency_P84 = reconCrr_P124;
				}

				// PlaceHolder 84
				String Advice_P84 = "Advice";
				RepeatAdviceMsg_P84 = "RepeatAdvice";
				boolean IsAdviceMsg_P84 = (atmPosting.getMsgFunction() == Advice_P84) ? true : false;
				boolean IsRepeatAdvice_P84 = (atmPosting.getMsgFunction() == RepeatAdviceMsg_P84) ? true : false;
				flag_P44 = flag_P84;
				Currency_P86 = Currency_P84;
				processRecon_Ef = processRecon_P84;

				// ConditionStep 95
				if ((IsAdviceMsg_P84 == true) || (IsRepeatAdvice_P84 == true)) // CS 95
				{
					// ( INPUT_boolean_0 == true ) || ( INPUT_boolean_1 == true )
					// UB_ATM_ValidateAccount_SRV96
					inputParamsValidateAccount_SRV96.put("AccountID", AccountID_SRV96);
					inputParamsValidateAccount_SRV96.put("NessageType", NessageType_SRV96);

					outputParamsValidateAccount_SRV96 = MFExecuter.executeMF("UB_ATM_ValidateAccount_SRV", env,
							inputParamsValidateAccount_SRV96);
					ErrorCode_SRV96 = (String) outputParamsValidateAccount_SRV96.get("ErrorCode");
					ErrorMessage_SRV96 = (String) outputParamsValidateAccount_SRV96.get("ErrorMessage");
					// PlaceHolder 97
					String Closed_error = "40200284";
					String NoAccount = "20020000";

					// ConditionStep 100
					if (ErrorCode_SRV96 != "") // CS 100
					{
						// INPUT_String_0 !=""

						// ConditionStep 98
						if ((ErrorCode_SRV96 == Closed_error) || (ErrorCode_SRV96 == NoAccount)) // CS 98
						{
							// (INPUT_bf_ATTR_STRING_0==INPUT_bf_ATTR_STRING_1) ||
							// (INPUT_bf_ATTR_STRING_2==INPUT_bf_ATTR_STRING_3)
							// PlaceHolder 86 --> mapped
						}

						else // CS 98
						{
							// (INPUT_bf_ATTR_STRING_0 !=INPUT_bf_ATTR_STRING_1 && INPUT_bf_ATTR_STRING_2 !=
							// INPUT_bf_ATTR_STRING_3)
							// UB_ATM_GetATMDebitSuspAcc99
							inputParamsGetATMDebitSuspAcc99.put("CurrCode", CurrCode_SRV99);

							outputParamsGetATMDebitSuspAcc99 = MFExecuter.executeMF("UB_ATM_GetATMDebitSuspAcc", env,
									inputParamsGetATMDebitSuspAcc99);

							ExternalAccountId_Ef = (String) outputParamsGetATMDebitSuspAcc99.get("AccountID");
							DebitAcc_P18 = (String) outputParamsGetATMDebitSuspAcc99.get("AccountID");
							// --> P86
						}
					}

					else // CS 100
					{
						// INPUT_String_0 == ""
						// PlaceHolder 86 --> mapped
					}
				}

				else // CS 95
				{
					// (INPUT_boolean_0==false)&&(INPUT_boolean_1==false)
					// PlaceHolder 86
				}
			}
			// PlaceHolder 86 -- main
			CurrCode_SRV23 = Currency_P86;

			// UB_ATM_GetExternalSettlementAcc23
			inputParamsGetExternalSettlementAcc23.put("CurrCode", CurrCode_SRV23);
			inputParamsGetExternalSettlementAcc23.put("ForcePost", ForcePost);
			inputParamsGetExternalSettlementAcc23.put("Id", Id_SRV23);
			inputParamsGetExternalSettlementAcc23.put("credit", true);
			inputParamsGetExternalSettlementAcc23.put("debit", false);
			inputParamsGetExternalSettlementAcc23.put("MICROFLOW_ID", "UB_ATM_GetExternalSettlementAcc");
			inputParamsGetExternalSettlementAcc23.put("SUBSTITUTION_REQUIRED", true);

			outputParamsGetExternalSettlementAcc23 = MFExecuter.executeMF("UB_ATM_GetExternalSettlementAcc", env,
					inputParamsGetExternalSettlementAcc23);

			ErrorCode_P44 = (String) outputParamsGetExternalSettlementAcc23.get("ErrorCode");
			ErrorMessage_P44 = (String) outputParamsGetExternalSettlementAcc23.get("ErrorMessage");
			External_Cr_Ef = (String) outputParamsGetExternalSettlementAcc23.get("ExternalSettlementAcc");
			CreditAcc_P18 = (String) outputParamsGetExternalSettlementAcc23.get("ExternalSettlementAcc");
			// PlaceHolder 18
			String AtmTransType_P18 = "02";
			AtmTransType_P44 = AtmTransType_P18;
			DebitAcc_P18 = atmPosting.getAccountIdentification1().getAccountNumber1();
			CrAccount_P44 = CreditAcc_P18;
			DrAccount_P44 = DebitAcc_P18;

		}
		// PlaceHolder 44 main
		AtmTransType_Ef = AtmTransType_P44;
		ErrorCode_Ef = ErrorCode_P44;
		ErrorMessage_Ef = ErrorMessage_P44;
		ExternalFlag_Ef = flag_P44;
		contraAccount_Ef = CrAccount_P44;
		ACCOUNTNUM_SRV55 = DrAccount_P44;
		ACCOUNTNUM_SRV54 = CrAccount_P44;
		AtmTransType_SRV53 = AtmTransType_P44;
		AtmTransType_SRV59 = AtmTransType_P44;
		isErrorExists_P44 = (ErrorCode_P44 != "") ? true : false;
		ErrorWithOutForcePost_P44 = ((isErrorExists_P44 == true) && (ForcePost == false)) ? true : false;
		getAtmTransactionType_P44 = (productIndicator_P41 + MessageType_P41 + AccountType_P41);
		Cr_Message_P89.setPRIMARYID(CrAccount_P44);
		Dr_Message_P89.setMESSAGEID(getUniqueID());
		Dr_Message_P89.setPRIMARYID(DrAccount_P44);
		Cr_AtmMessage_P4.setPRIMARYID(CrAccount_P44);
		Dr_AtmMessage_P4.setPRIMARYID(CrAccount_P44);

		// ConditionStep 61
		if (ErrorWithOutForcePost_P44 == (true)) // CS 61
		{
			// INPUT_boolean_0 == true
			// End Fatom --> mapped
		}

		else // CS 61
		{
			//// INPUT_boolean_0 == false
			// UB_ATM_FetchAtmTxnCodeDtls_SRV59
			inputParamsFetchAtmTxnCodeDtls_SRV59.put("AtmTransType", AtmTransType_SRV59);
			inputParamsFetchAtmTxnCodeDtls_SRV59.put("AtmTxnCode", AtmTxnCode_SRV59);

			outputParamsFetchAtmTxnCodeDtls_SRV59 = MFExecuter.executeMF("UB_ATM_FetchAtmTxnCodeDtls_SRV", env,
					inputParamsFetchAtmTxnCodeDtls_SRV59);

			errorCode_P73 = (String) outputParamsFetchAtmTxnCodeDtls_SRV59.get("errorCode");// errorCode SRV59 o/p
			errorMsg_P73 = (String) outputParamsFetchAtmTxnCodeDtls_SRV59.get("errorMsg"); // errorMsg_P73 SRV59 0/p
			AtmTxnCode_Ef = (String) outputParamsFetchAtmTxnCodeDtls_SRV59.get("AtmTxnCode");// outPut of SRV59
			AtmTxnDesc_Ef = (String) outputParamsFetchAtmTxnCodeDtls_SRV59.get("Description");// outPut 0f SRV59
			ErrorCode_Ef = (String) outputParamsFetchAtmTxnCodeDtls_SRV59.get("errorCode");// outPut of SRV59
			ErrorMessage_Ef = (String) outputParamsFetchAtmTxnCodeDtls_SRV59.get("errorMsg");// SRV59
			MisTxnCode_Ef = (String) outputParamsFetchAtmTxnCodeDtls_SRV59.get("MISTxnCode");// SRV 59 o/p
			TxnCode_SRV56 = (String) outputParamsFetchAtmTxnCodeDtls_SRV59.get("MISTxnCode");// SRV 59 o/p
			ErrorCode_SRV59 = (String) outputParamsFetchAtmTxnCodeDtls_SRV59.get("errorCode");
			Cr_Message_P89.setTRANSACTIONCODE((String) outputParamsFetchAtmTxnCodeDtls_SRV59.get("MISTxnCode"));
			Dr_Message_P89.setTRANSACTIONCODE((String) outputParamsFetchAtmTxnCodeDtls_SRV59.get("MISTxnCode"));
			Cr_AtmMessage_P4.setTRANSACTIONCODE((String) outputParamsFetchAtmTxnCodeDtls_SRV59.get("MISTxnCode"));
			Dr_AtmMessage_P4.setTRANSACTIONCODE((String) outputParamsFetchAtmTxnCodeDtls_SRV59.get("MISTxnCode"));

			// ConditionStep 68
			if ("".equals(ErrorCode_SRV59)) // CS 68
			{
				// INPUT_String_0 == ""
				// UB_ATM_TransactionNarrative53
				inputParamsTransactionNarrative53.put("ATMTRANSACTIONCODE", atmPosting.getTxnCode());
				inputParamsTransactionNarrative53.put("AtmPosting", atmPosting);
				inputParamsTransactionNarrative53.put("AtmTransType", AtmTransType_P44);

				outputParamsTransactionNarrative53 = MFExecuter.executeMF("UB_ATM_TransactionNarrative_SRV", env,
						inputParamsTransactionNarrative53);
				Narrative_SRV79 = (String) outputParamsTransactionNarrative53.get("Narrative");

				// UB_CMN_FetchAccountService as on MF Fetch Account Details by AccountNumber54
				inputParamsFetchAccountService54.put("ACCOUNTNUM", ACCOUNTNUM_SRV54);

				outputParamsFetchAccountService54 = MFExecuter.executeMF("UB_CMN_FetchAccountService", env,
						inputParamsFetchAccountService54);
				FromCurrencyCode_SRV56 = (String) outputParamsFetchAccountService54.get("ISOCURRENCYCODE");// Output of
																											// SRV54
																											// ISOCURRENCYCODE
				Cr_Message_P89.setACCTCURRENCYCODE((String) outputParamsFetchAccountService54.get("ISOCURRENCYCODE"));
				Cr_Message_P89.setBRANCHSORTCODE((String) outputParamsFetchAccountService54.get("BRANCHSORTCODE"));
				Cr_AtmMessage_P4.setACCTCURRENCYCODE((String) outputParamsFetchAccountService54.get("ISOCURRENCYCODE"));
				Cr_AtmMessage_P4.setBRANCHSORTCODE((String) outputParamsFetchAccountService54.get("BRANCHSORTCODE"));

				// UB_CMN_FetchAccountService as on MF Fetch Account Details by AccountNumber55
				inputParamsFetchAccountService55.put("ACCOUNTNUM", ACCOUNTNUM_SRV55);

				outputParamsFetchAccountService55 = MFExecuter.executeMF("UB_CMN_FetchAccountService", env,
						inputParamsFetchAccountService55);
				accountCurrency_Ef = (String) outputParamsFetchAccountService55.get("ISOCURRENCYCODE");// SRV55 o/p
				Dr_Message_P89.setACCTCURRENCYCODE((String) outputParamsFetchAccountService55.get("ISOCURRENCYCODE"));
				Dr_Message_P89.setBRANCHSORTCODE((String) outputParamsFetchAccountService55.get("BRANCHSORTCODE"));
				Dr_AtmMessage_P4.setACCTCURRENCYCODE((String) outputParamsFetchAccountService55.get("ISOCURRENCYCODE"));
				Dr_AtmMessage_P4.setBRANCHSORTCODE((String) outputParamsFetchAccountService55.get("BRANCHSORTCODE"));

				// ConditionStep 92
				if (true == flag_P44) // CS 92
				{
					// INPUT_SELECT_0 == true
					// PlaceHolder 93
					ToCurrencyCode_SRV56 = toCurrencyCode_P93;

				}

				else // CS 92
				{
					// INPUT_SELECT_0 == false
					// PlaceHolder 94
					toCurrencyCode_P94 = financialDetails.getCurrencyCode();// data currencyCode
					ToCurrencyCode_SRV56 = toCurrencyCode_P94;
				}

				// UB_ATM_GetExcahgeRateDtlsOnTxnCode56
				inputParamsGetExcahgeRateDtlsOnTxnCode56.put("ToCurrencyCode", ToCurrencyCode_SRV56); // P93 or P94
				inputParamsGetExcahgeRateDtlsOnTxnCode56.put("TxnCode", TxnCode_SRV56);
				inputParamsGetExcahgeRateDtlsOnTxnCode56.put("FromCurrencyCode", FromCurrencyCode_SRV56);

				outputParamsGetExcahgeRateDtlsOnTxnCode56 = MFExecuter.executeMF("UB_ATM_GetExcahgeRateDtlsOnTxnCode",
						env, inputParamsGetExcahgeRateDtlsOnTxnCode56);
				Cr_Message_P89
						.setEXCHRATETYPE((String) outputParamsGetExcahgeRateDtlsOnTxnCode56.get("ExchangeRateType"));
				Dr_Message_P89
						.setCROSSCURRENCY((boolean) outputParamsGetExcahgeRateDtlsOnTxnCode56.get("CrossCurrency"));
				Dr_Message_P89
						.setEXCHRATETYPE((String) outputParamsGetExcahgeRateDtlsOnTxnCode56.get("ExchangeRateType"));
				Cr_AtmMessage_P4
						.setEXCHRATETYPE((String) outputParamsGetExcahgeRateDtlsOnTxnCode56.get("ExchangeRateType"));
				Dr_AtmMessage_P4
						.setCROSSCURRENCY((boolean) outputParamsGetExcahgeRateDtlsOnTxnCode56.get("CrossCurrency"));
				Dr_AtmMessage_P4
						.setEXCHRATETYPE((String) outputParamsGetExcahgeRateDtlsOnTxnCode56.get("ExchangeRateType"));

				// UB_ATM_GetTransactionNarrative_SRV79
				inputParamsGetTransactionNarrative_SRV79.put("Narrative", Narrative_SRV79);
				inputParamsGetTransactionNarrative_SRV79.put("UB_ATM_FinancialDetails", atmPosting);

				outputParamsGetTransactionNarrative_SRV79 = MFExecuter.executeMF("UB_ATM_GetTransactionNarrative_SRV",
						env, inputParamsGetTransactionNarrative_SRV79);
				Cr_Message_P89.setNARRATIVE((String) outputParamsGetTransactionNarrative_SRV79.get("narrativeMain"));
				Dr_Message_P89.setNARRATIVE((String) outputParamsGetTransactionNarrative_SRV79.get("narrativeMain"));
				Cr_AtmMessage_P4.setNARRATIVE((String) outputParamsGetTransactionNarrative_SRV79.get("narrativeMain"));
				Dr_AtmMessage_P4.setNARRATIVE((String) outputParamsGetTransactionNarrative_SRV79.get("narrativeMain"));

				// ConditionStep 88
				if (true == flag_P44) // CS 88
				{
					// INPUT_SELECT_0 == true

					// ConditionStep 114
					if (true == processRecon_P84) // CS 114
					{
						// INPUT_SELECT_0 == true
						// PlaceHolder 117
						amountRecon_P117 = financialDetails.getAmountRecon();// data
						Cr_Message_P89.setACTUALAMOUNT(amountRecon_P117);
						Cr_Message_P89.setAMOUNT(amountRecon_P117);
						Cr_Message_P89.setAMOUNTCREDIT(amountRecon_P117);
						Cr_Message_P89.setAMOUNTDEBIT(amountRecon_P117);
						Cr_Message_P89.setTXNCURRENCYCODE(reconCurrency_P117);
						Dr_Message_P89.setACTUALAMOUNT(amountRecon_P117);
						Dr_Message_P89.setAMOUNT(amountRecon_P117);
						Dr_Message_P89.setAMOUNTCREDIT(amountRecon_P117);
						Dr_Message_P89.setAMOUNTDEBIT(amountRecon_P117);
						Dr_Message_P89.setTXNCURRENCYCODE(reconCurrency_P117);

					}

					else // CS 114
					{
						// INPUT_SELECT_0 == false
						// PlaceHolder 118
						cardHolerBillingAmount_P118 = financialDetails.getCardHolderBillingAmount();// atmPosting
																									// -findtls-
																									// cardHolerBillingAmount
						Cr_Message_P89.setACTUALAMOUNT(cardHolerBillingAmount_P118);
						Cr_Message_P89.setAMOUNT(cardHolerBillingAmount_P118);
						Cr_Message_P89.setAMOUNTCREDIT(cardHolerBillingAmount_P118);
						Cr_Message_P89.setAMOUNTDEBIT(cardHolerBillingAmount_P118);
						Cr_Message_P89.setTXNCURRENCYCODE(cardHolderBillingCurrency_P118);
						Dr_Message_P89.setACTUALAMOUNT(cardHolerBillingAmount_P118);
						Dr_Message_P89.setAMOUNT(cardHolerBillingAmount_P118);
						Dr_Message_P89.setAMOUNTCREDIT(cardHolerBillingAmount_P118);
						Dr_Message_P89.setAMOUNTDEBIT(cardHolerBillingAmount_P118);
						Dr_Message_P89.setTXNCURRENCYCODE(cardHolderBillingCurrency_P118);
					}

					// PlaceHolder 89
					Cr_Message_P89.setCHANNELID(financialDetails.getMessageHeader().getProductIndicator());
					Cr_Message_P89.setEXCHRATE(atmPosting.getAdditionalResponseData().getAvailableBalance());
					Cr_Message_P89.setFORCEPOST(ForcePost);
					Cr_Message_P89.setTRANSACTIONDATE(DateTime);
					Cr_Message_P89.setTRANSACTIONREF(financialDetails.getRetrievalReferenceNo());
					Cr_Message_P89.setVALUEDATE(financialDetails.getTransmissionDateTime());
					Dr_Message_P89.setVALUEDATE(financialDetails.getTransmissionDateTime());
					Dr_Message_P89.setTRANSACTIONREF(financialDetails.getRetrievalReferenceNo());
					Dr_Message_P89.setTRANSACTIONDATE(DateTime);
					Dr_Message_P89.setCHANNELID(financialDetails.getMessageHeader().getProductIndicator());
					Dr_Message_P89.setEXCHRATE(atmPosting.getAdditionalResponseData().getAvailableBalance());
					Dr_Message_P89.setFORCEPOST(ForcePost);

					setF_IN_Cr_AtmMessages(Cr_Message_P89);
					setF_IN_Dr_AtmMessages(Dr_Message_P89);

				}

				else // CS 88
				{
					// INPUT_SELECT_0 == false
					// PlaceHolder 4
					Cr_AtmMessage_P4.setACTUALAMOUNT(financialDetails.getTransactionAmount());
					Cr_AtmMessage_P4.setAMOUNT(financialDetails.getTransactionAmount());
					Cr_AtmMessage_P4.setAMOUNTCREDIT(financialDetails.getTransactionAmount());
					Cr_AtmMessage_P4.setAMOUNTDEBIT(financialDetails.getTransactionAmount());
					Cr_AtmMessage_P4.setCHANNELID(financialDetails.getMessageHeader().getProductIndicator());
					Cr_AtmMessage_P4.setEXCHRATE(atmPosting.getAdditionalResponseData().getAvailableBalance());
					Cr_AtmMessage_P4.setFORCEPOST(ForcePost);
					Cr_AtmMessage_P4.setTRANSACTIONDATE(DateTime);
					Cr_AtmMessage_P4.setTRANSACTIONREF(financialDetails.getRetrievalReferenceNo());
					Cr_AtmMessage_P4.setTXNCURRENCYCODE(financialDetails.getCurrencyCode());
					Cr_AtmMessage_P4.setVALUEDATE(financialDetails.getTransmissionDateTime());

					Dr_AtmMessage_P4.setACTUALAMOUNT(financialDetails.getTransactionAmount());
					Dr_AtmMessage_P4.setAMOUNT(financialDetails.getTransactionAmount());
					Dr_AtmMessage_P4.setAMOUNTCREDIT(financialDetails.getTransactionAmount());
					Dr_AtmMessage_P4.setAMOUNTDEBIT(financialDetails.getTransactionAmount());
					Dr_AtmMessage_P4.setCHANNELID(financialDetails.getMessageHeader().getProductIndicator());
					Dr_AtmMessage_P4.setEXCHRATE(atmPosting.getAdditionalResponseData().getAvailableBalance());
					Dr_AtmMessage_P4.setFORCEPOST(ForcePost);
					Dr_AtmMessage_P4.setTRANSACTIONDATE(DateTime);
					Dr_AtmMessage_P4.setTRANSACTIONREF(financialDetails.getRetrievalReferenceNo());
					Dr_AtmMessage_P4.setTXNCURRENCYCODE(financialDetails.getCurrencyCode());
					Dr_AtmMessage_P4.setVALUEDATE(financialDetails.getTransmissionDateTime());

					setF_IN_Cr_AtmMessages(Cr_AtmMessage_P4);
					setF_IN_Dr_AtmMessages(Dr_AtmMessage_P4);

				}

				// PlaceHolder 91

				setF_IN_AtmPostingMsgValues(Cr_AtmMessage_P4);// map to 57
				setF_IN_AtmPostingMsgValues1(Dr_AtmMessage_P4); // map to 58

				// UB_ATM_CreateRepeatingComplexType_SRV57
				// Map inputParamsCreateRepeatingComplexType_SRV57 = new HashMap<>();
				inputParamsCreateRepeatingComplexType_SRV57.put("AtmPostingMsgValues", Cr_AtmMessage_P4);

				// UB_ATM_CreateRepeatingComplexType_SRV58
				// Map<String, Object> outputParamsCreateRepeatingComplexType_SRV57;
				outputParamsCreateRepeatingComplexType_SRV57 = MFExecuter.executeMF(
						"UB_ATM_CreateRepeatingComplexType_SRV", env, inputParamsCreateRepeatingComplexType_SRV57);
				

				// Map inputParamsCreateRepeatingComplexType_SRV58 = new HashMap<>();
				inputParamsCreateRepeatingComplexType_SRV58.put("AtmPostingMsgValues", (UB_Atm_PostingMessages)Dr_AtmMessage_P4);
				inputParamsCreateRepeatingComplexType_SRV58.put("RepeatingComplexTypeInput", (UB_Atm_PostingMessage) outputParamsCreateRepeatingComplexType_SRV57
						.get("IN_CTYPE_AtmPostingMsg"));


				outputParamsCreateRepeatingComplexType_SRV58 = MFExecuter.executeMF(
						"UB_ATM_CreateRepeatingComplexType_SRV", env, inputParamsCreateRepeatingComplexType_SRV58);

				// UB_ATM_CreateRepeatingComplexType_SRV58
				setF_OUT_IN_CTYPE_AtmPostingMsg((UB_Atm_PostingMessage) outputParamsCreateRepeatingComplexType_SRV58
						.get("IN_CTYPE_AtmPostingMsg")); // EndFatom

			}

			else // CS 68
			{
				// INPUT_String_0 !=""
				String error_73 = errorCode_P73;
				String msg_73 = errorMsg_P73;
				// PlaceHolder 73 --> EndFatom
			}

		}

		// EndFatom -->
		setF_OUT_AtmTransType(AtmTransType_Ef);
		setF_OUT_AtmTxnCode(AtmTxnCode_Ef);
		setF_OUT_AtmTxnDesc(AtmTxnDesc_Ef);
		setF_OUT_ErrorCode(ErrorCode_Ef);
		setF_OUT_ErrorMessage(ErrorMessage_Ef);
		setF_OUT_ExternalAccountId(ExternalAccountId_Ef);
		setF_OUT_ExternalFlag(ExternalFlag_Ef);
		setF_OUT_External_Cr(External_Cr_Ef);
		setF_OUT_External_Dr(External_Dr_Ef);
		setF_OUT_MisTxnCode(MisTxnCode_Ef);
		setF_OUT_accountCurrency(accountCurrency_Ef);
		setF_OUT_contraAccount(contraAccount_Ef);
		setF_OUT_processRecon(processRecon_Ef);

	}

	static String getUniqueID() {
		UUID abc = null;
		String getUniqueID = ((String.valueOf(abc.randomUUID())).substring(0, 18)).replace("-", "");
		return getUniqueID;
	}

}
