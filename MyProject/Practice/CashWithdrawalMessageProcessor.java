import java.util.Map;

public class CashWithdrawalMessageProcessor {
	
	// Variables of Start Fatom
	
	UB_ATM_Financial_Details atmPosting = getF_IN_AtmPosting();
	UB_Financial_Details financialDetails = atmPosting.getFinancialDetails();
	String AccountId = getF_IN_AccountId();
	Boolean ForcePost = getF_IN_ForcePost();
	
	// Variables of End Fatom 
	
	UB_ATM_Financial_Details atmPostingEnd = getF_IN_AtmPosting();
	UB_Financial_Details financialDetailsEnd = atmPostingEnd.getFinancialDetails();
	String AtmTransType = "";
	String AtmTxnCode = "";
	String AtmTxnDesc = "";
	String AtmtxnDesc = "";
	String ErrorCode = "";
	String ErrorMessage = "";
	String ExternalAccountId = "";
	Boolean ExternalFlag = false;
	String External_Cr = "";
	String External_Dr = "";
	
	String MisTxnCode = "";
	String accountCurrency = "";
	String contraAccount = "";
	Boolean processRecon = false;
	// PlaceHolder41 Variable
	Boolean BooleanTrue = true; 

		Map inputParams = new HashMap();
		inputParams.put("acquirerId", atmPosting.financialDetails.getAcquiringInstitutionId());
		Map<K, V> outputParams = MFExecuter.executeMF("UB_ATM_AquirerIdBelongsToOwnBankIMD_SRV", env, inputParams);
		
		// output of UB_ATM_AquirerIdBelongsToOwnBankIMD_SRV
		String FinalErrorCode = (String)outputParams.get("ErrorCode");
		String FinalErrorMessage = (String)outputParams.get("ErrorMessage");
		

		// PlaceHolder65 Variable
		Boolean output_65 = outputParams.get("finalAcquirerIdStatus");

		if(outputParams.get("finalAquirerIdStatus"))
		{
			// SettlementAccount BOD Call
			String ATMDEVICEID = financialDetails.getcardAcceptorTerminalId();

			if (("").equals(ATMDEVICEID)) {
				Map inputParams1 = new HashMap<>();
				inputParams1.put("CurrCode", financialDetails.getCurrencyCode());
				inputParams1.put("ForcePost", ForcePost);
				inputParams1.put("Id", financialDetails.getCardAcceptorId());
				
				Map outputParams1 = MFExecuter.executeMF("UB_ATM_GetExternalSettlementAcc", env, inputParams1);
				String ErrorCode_GESAcc77 = outputParams1.get("ErrorCode");
				String ErrorMessage_GESAcc77 = outputParams1.get("ErrorMessage");
				String ExternalSettlementAcc_GESAcc77 = outputParams1.get("ExternalSettlementAcc");

				// Placeholder 78 variable
				String CrAccount_78 = ExternalSettlementAcc_GESAcc77;
				String DebitAccount_78 = atmPosting.getaccountIdentification1().getaccountNumber1();

			} else {
				Map inputParams2 = new HashMap<>();
				inputParams2.put("CurrCode", atmPosting.financialDetails.currencyCode);
				inputParams2.put("ForcePost", ForcePost);
				inputParams2.put("ID", atmPosting.financialDetails.getcardAcceptorTerminalId());
				inputParams2.put("IsATMTxn",BooleanTrue);
				
				Map outputParams2 = MFExecuter.executeMF("UB_ATM_GetATMSettlementCashACC", env, inputParams2);
				//output of UB_ATM_GetATMSettlementCashACC52
				String ATMCashAcc_GESCAcc52 = outputParams2.get("ATMCashAcc");
				String ErrorCode_GESCAcc52 = outputParams2.get("ErrorCode");
				String ErrorMessage_GESCAcc52 = outputParams2.get("ErrorMessage");
				
				// PlaceHolder 60 Variable 1)isErrorWithoutForcePost
				String cardIssuerFIID_60 = atmPosting.getcardIssuerData().getcardIssuerFIID();
				String cardLogicalNetwork_60 = atmPosting.getcardIssuerData().getcardLogicalNetwork();
				//Boolean isErrorWithoutForcePost_60 = 

				if (isErrorWithoutForcePost) {
					Map inputParams3 = new HashMap<>();
					inputParams3.put("cardIssuerID", cardIssuerID);
					
					
					Map outputParams3 = MFExecuter.executeMF("UB_ATM_CardIssuerIdBelongsToOwnBankIMD", env, inputParams3);
					// output of UB_ATM_CardIssuerIdBelongsToOwnBankIMD_S15
					String ErrorCode_CI12 = outputParams3.get("ErrorCode");
					String ErrorMessage_CI12 = outputParams3.get("ErrorMessage");
					Boolean issuerIDFlag_CI12 = outputParams3.get("issuerIDFlag");
					// issuerIdFlag

					if (issuerIdFlag) {
						// Placeholder21 variable
						String ATMTransType_21 =""; 
						String CreditAcc_21 = ATMCashAcc_GESCAcc52;
						String DebitAcc_21 = atmPosting.getaccountIdentification1().getaccountNumber1();
					} else {
						//input UB_ATM_GetExternalSettlementAcc22
						Map inputParams4 = new HashMap<>();
						inputParams4.put("CurrCode", atmPosting.financialDetails.getcurrencyCode());
						inputParams4.put("ForcePost",ForcePost);
						inputParams4.put("Id", cardIssuerId);
						
						Map outputParams4 = MFExecuter.executeMF("UB_ATM_GetExternalSettlementAcc", env, inputParams4);
						//output of UB_ATM_GetExternalSettlementAcc22
						String ErrorCode_GESAcc22 = outputParams4.get("ErrorCode");
						String ErrorMessage_GESAcc22 = outputParams4.get("ErrorMessage");
						String ExternalSettlement_GESAcc22 = outputParams4.get("ExternalSettlementAcc");
						
						// PlaceHolder19 Variable
						String CreditAcc_19 = ATMCashAcc_GESCAcc52;
						String DebitAcc_19 = ExternalSettlement_GESAcc22;
					}
				} else {

					// EndfatomVariable error =220202020
					
				}

				// Placeholder 44 variable (Values to be mapped from Placeholder 78
				// variable,Placeholder21, Placeholder 19)
			}
		}else
		{
			// amountRecon to be fetched from Startstep>> FinancialDetail
			String amount_Recon = finacialDetails.getamountRecon();
			if (0 == amount_Recon) {
				//amountReconCurrency to be fetched from Startstep>> FinancialDetail
				String amount_ReconCurrency = atmPosting.financialDetails.getamountReconCurrency();
				Map inputParams5 = new HashMap<>();
				inputParams5.put(amount_ReconCurrency);
				
				Map outputParams5 = MFExecuter.executeMF("EnquireCurrencyCodeFromNumericCode", env, inputParams5);
				//ISOCURRENCYCODE from outputParams2
				//output of EnquireCurrencyCodeFromNumericCode111
				String ISOCURRENCYCODE_ECCFNCode111 = outputParams5.get(ISOCURRENCYCODE);
				
				//PlaceHolder 110 Variable
				String ISOCURRENCYCODE_110 = ISOCURRENCYCODE_ECCFNCode111;
				
				
				
			} else {
				
				//amountCardHolderBillingCurrency to be fetched from Startstep>> FinancialDetail
				Map inputParams6 = new HashMap<>();
				// input of EnquireCurrencyCodeFromNumericCode82
				String NUMERICCURRENCYCODE = atmPosting.financialDetails.getcardHoldrBillingCurrency();
				inputParams6.put(NUMERICCURRENCYCODE);
				
				Map outputParams6 = MFExecuter.executeMF("EnquireCurrencyCodeFromNumericCode", env, inputParams6);
				//ISOCURRENCYCODE from outputParams3
				//OUTPUT OF EnquireCurrencyCodeFromNumericCode82
				String ISOCURRENCYCODE_ECCFN_82 = outputParams6.get("ISOCURRENCYCODE");
				
			}
			// currencyCode to be fetched from Startstep>> FinancialDetail
			if(financialDetails.getcurrencyCode() == ISOCURRENCYCODE_ECCFN_82) {
				//Variable PlaceHolder 85 -- CurrencyCode >>  endFatom{FinalCurrencyCode=CurrencyCode}
				String CurrencyCode_85 = financialDetails.getcurrencyCode();
			}
			else {
				//PlaceHolder121 Variable {processRecon}
				Boolean processRecon_121 = processRecon_110;
				
				if(processRecon_121 == true) {
					//placeholder123 reconCurr to be mapped to Placeholder 84
					String reconCrr_123 = ISOCURRENCYCODE_ECCFNCode111;
				}
				else {
					//placeholder124 reconCurr to be mapped to Placeholder 84
					String reconCrr_124 = ISOCURRENCYCODE_ECCFNCode111;
				}
				//Placeholder 84 variable to be mapped to Placeholder 86
				String Currency_84 = reconCrr_123;
				String processRecon_84 = processRecon_110;
				
				if ((isMessageAdvice == true)||(isMessageRepeatAdvice== true)){
					
					Map inputParams7 = new HashMap<>();
					//input of UB_ATM_ValidateAccount_SRV96
					inputParams7.put("AccountID", AccountId);
					inputParams7.put("NessageType", atmPosting.financialDetails.getmessageHeader().getmessageType());
					
					Map outputParams7 = MFExecuter.executeMF("UB_ATM_ValidateAccount_SRV", env, inputParams7);
					// outPut of ValidateAccount_SRV96
					String ErrorCode_VA96 = outputParams7.get(ErrorCode);
					String ErrorMessage_VA96 = outputParams7.get(ErrorMessage);
					
					//PlaceHolder97 Variable  {Closed_Error and No_Account_Error}
					String Closed_error_97 = "40200284";
					String NoAccount_97 = "20020000";
					
					//ErrorCode to be fetched from outputParams3
					if (ErrorCode_VA96 !="") {
						
						if (ErrorCode_VA96 == Closed_error_97 || ErrorCode_VA96 == NoAccount_97) {
							//to be mapped to PlaceHolder86{ErrorCode}
							
						}
						else {
							Map inputParams8 = new HashMap<>();
							//input of UB_ATM_GetATMDebitSuspAcc99
							inputParams8.put("CurrCode",ISOCURRENCYCODE_ECCFN_82 );
							
							Map outputParams8 = MFExecuter.executeMF("UB_ATM_GetATMDebitSuspAcc99", env, inputParams8);
							//output of UB_ATM_GetATMDebitSuspAcc99
							String AccountID_GADSAcc99 = outputParams8.get("AccountID");
							
						}
					}
					else {
						//Mapping to placeHolder86
						
						
					}
					
				}
				else {
					//Mapping to placeHolder86
				}
				
				Map inputParams9 = new HashMap<>();
				//input of UB_ATM_GetExternalSettlementAcc23
				inputParams9.put("CurrCode", CurrencyCode_86);
				inputParams9.put("ForcePost", ForcePost);
				inputParams9.put("Id", atmPosting.finncialDetails.getacquiringInstitutionId());
				
				Map outputParams9 = MFExecuter.executeMF("UB_ATM_GetExternalSettlementAcc23", env, inputParams9);
				//output of UB_ATM_GetExternalSettlementAcc23
				String ErrorCode_GESAcc23 = outputParams9.get("ErrorCode");
				String ErrorMessage_GESAcc23 = outputParams9.get("ErrorMessage");
				String ExternalSettlement_GESAcc23 = outputParams9.get("ExternalSettlementAcc");
			
				//Placeholder18 Variable
				String CreditAcc_18 = ExternalSettlement_GESAcc23;
				String DebitAcc_18 = AccountID_GADSAcc99;
			}
			// Placeholder 44 variable  {errorWithoutForcePost}
			 
			if (errorWithoutForcePost) {
				//Set Error code to end fatom Variable
				FinalErrorCode = ErrorCode; //Errorcode of Placeholdr44
			}
			else {
				Map inputParams10 = new HashMap<>();
				//input of UB_ATM_FetchAtmTxnCodeDtls_SRV59
				inputParams10.put("AtmTransType", AtmTransType_44);
				inputParams10.put("AtmTxnCode", atmPosting.gettxnCode());
				
				Map outputParams10 = MFExecuter.executeMF("UB_ATM_FetchAtmTxnCodeDtls_SRV", env, inputParams10);
				//output of UB_ATM_FetchAtmTxnCodeDtls_SRV59
				String AtmTxnCode_FATCDtls = outputParams10.get("AtmTxnCode");
				String Description_FATCDtls = outputParams10.get("Description");
				String MISTxnCode_FATCDtls = outputParams10.get("MISTxnCode");
				String TaxNarrative_FATCDtls = outputParams10.get("TaxNarrative");
				String errorCode_FATCDtls = outputParams10.get("errorCode");
				String errorMsg_FATCDtls = outputParams10.get("errorMsg");
			
				//FinalErrorCode = (String)outputParams4.get("errorCode");
				
				
				if(errorCode_FATCDtls.equals("")) {
					Map inputParams11 = new HashMap<>();
					//input of UB_ATM_TransactionNarrative
					inputParams11.put("ATMTRANSACTIONCODE", atmPosting.gettxnCode());
					inputParams11.put("AtmPosting", atmPosting);
					inputParams11.put("AtmTransType", AtmTransType_44);
					
					Map outputParams11 = MFExecuter.executeMF("UB_ATM_TransactionNarrative", env, inputParams11);
					//output of UB_ATM_TransactionNarrative
					String CONTRANARRATIVE_TNar = outputParams11.get("CONTRANARRATIVE");
					String CUSTOMERNARRATIVE_TNar = outputParams11.get("CUSTOMERNARRATIVE");
					String Narrative_TNar = outputParams11.get("Narrative");
					
					Map inputParams12 = new HashMap<>();
					//inut of UB_CMN_FetchAccountService54
					inputParams12.put(CrAccount_44)
					Map outputParams12 = MFExecuter.executeMF("UB_CMN_FetchAccountService", env, inputParams12);
					//output of UB_CMN_FetchAccountService54
					
					
					Map inputParams13 = new HashMap<>();
					//input of UB_CMN_FetchAccountService55
					inputParams13.put(DrAccount_44);
					
					Map outputParams13 = MFExecuter.executeMF("UB_CMN_FetchAccountService", env, inputParams13);
					//output of UB_CMN_FetchAccountService55
					
					//conditional Step 92
					if (flag_44) {
						//placeholder 93 {toCurrencyCode}
						String toCurrency_93 = ISOCURRENCYCODE_ECCFN_82;
						
					}
					else {
						//placeholder 94 {toCurrencyCode}
						String toCurrency_94 = atmPosting.financialDetails.getcurrencyCode();
					}
					
					Map inputParams14 = new HashMap<>();
					//input of UB_ATM_GetExcahgeRateDtlsOnTxnCode56
					inputParams6.put(toCurrencyCode);
					inputParams14.put("TxnCode", MISTxnCode_FATCDtls);
					inputParams14.put("FromCurrencyCode",);
					
					Map outputParams14 = MFExecuter.executeMF("UB_ATM_GetExcahgeRateDtlsOnTxnCode56", env, inputParams14);
					//output of UB_ATM_GetExcahgeRateDtlsOnTxnCode56
					String CrossCurrency_GERDtls56 = outputParams14.get("CrossCurrency");
					String ExchangeRate_GERDtls56 = outputParams14.get("ExchangeRate");
					String ExchangeRateType_GERDtls56 = outputParams14.get("ExchangeRateType");
					
					Map inputParams15 = new HashMap<>();
					//input of UB_ATM_GetTransactionNarrative_SRV79
					inputParams15.put("Narrative", Narrative_TNar);
					inputParams15.put("UB_ATM_FinancialDetails", atmPosting);
					
					Map outputParams15 = MFExecuter.executeMF("UB_ATM_GetTransactionNarrative_SRV", env, inputParams15);
					//output of UB_ATM_GetTransactionNarrative_SRV79
					String narrativeComm_GTN79 = outputParams15.get("narrativeComm");
					String narrativeMain_GTN79 = outputParams15.get("narrativeMain");
					
					
					if(flag_44) {
						//processRecon from Placeholder 84
						if (processRecon_84){
							//Placeholder 117, to be mapper to placeholder 89 variable
							String amountRecon_117 = atmPosting.financialDetails.getamountRecon();
							String reconCurrency_117 = ISOCURRENCYCODE_ECCFNCode111;
						}
						else {
							//Placeholder 118 , to be mapped to placeholder 89 variable
							String cardHolderBillingCurrency_118 = ISOCURRENCYCODE_ECCFNCode82;
							String cardHolerBillingAmount_118 = atmPosting.financialDetails.getcardHolderBillingAmount();
						}
						
						//placeholder 89 , to be mapped to place holder 91
					}
					else {
						//Placeholder4 Variable, to be mapped to place holder 91
					}
					// placeholder91 value
					
					Map inputParams9 = new HashMap<>();
					Map outputParams9 = MFExecuter.executeMF("UB_ATM_GetTransactionNarrative_SRV79", env, inputParams);
					
					
					Map inputParams10 = new HashMap<>();
					Map outputParams10 = MFExecuter.executeMF("UB_ATM_GetTransactionNarrative_SRV79", env, inputParams);
					
					//mapped to end fatom, values from outputParams9 & 10
					
				}
				else {
					//PlaceHolder 73
					//mapped to end fatom
					
				}		
				
			
			}

		}

		// EndFatomVariable error,
		// FinalCurrencyCode
	}