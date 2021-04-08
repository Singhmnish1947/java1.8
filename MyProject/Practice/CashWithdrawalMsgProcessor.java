import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;

import bf.com.misys.ub.types.atm.UB_ATM_Financial_Details;
import bf.com.misys.ub.types.iso8583.UB_Financial_Details;

public class CashWithdrawalMsgProcessor {

	// Variable of End Fatom
	String FinalErrorCode = "";
	
	String retrievalReferenceNumber_37 = getF_IN_RetrievalReferenceNumber_37();
	UB_ATM_Financial_Details  atmPosting = getF_IN_AtmPosting();
	UB_Financial_Details _financialDetails = atmPosting.getFinancialDetails();
	String acquirerIdentificationCode_32 = getF_IN_acquiredIdentificationCode_32();
	Timestamp dateTime = getF_IN_dateAndTime_12();
	Timestamp orgDateTime = getF_IN_orgDateTime_56_3();
	String messageFunction = getF_IN_messageFunction();
	String uniqueEndTransactionReference = getF_IN_uniqueEndTransactionReference_48_031();
	String txnType = getF_IN_txnType();
	String orgAcIDC = getF_IN_orgAcIDC_56_4();
	String atmTxnCode = getF_IN_atmTransactionCode();
	String msgType = getF_IN_msgType();
	String accountId = getF_IN_AccountId();

	// Start Step Input

	// PlaceHolder41 Variable

	Map inputParams = new HashMap();
	inputParams.put("aquirerId ",);
	Map outputParams = MFExecuter.executeMF("UB_ATM_AquirerIdBelongsToOwnBankIMD_SRV", env, inputParams);
	FinalErrorCode = outputParams.get("ErrorCode");
	FinalCurrency = outputParams.get("Currency");

	// PlaceHolder Variable

	if(outputParams.get("finalAquirerIdStatus"))
	{
		// SettlementAccount BOD Call

		if (("").equals(ATMDEVICEID)) {
			Map inputParams = new HashMap<>();
			Map outputParams = MFExecuter.executeMF("UB_ATM_GetExternalSettlementAcc", env, inputParams);

			// Placeholder 78 variable

		} else {
			Map inputParams = new HashMap<>();
			Map outputParams = MFExecuter.executeMF("UB_ATM_GetATMSettlementCashACC", env, inputParams);
			// PlaceHolder 60 Variable 1)isErrorWithoutForcePost

			if (isErrorWithoutForcePost) {
				Map inputParams1 = new HashMap<>();
				Map outputParams1 = MFExecuter.executeMF("UB_ATM_CardIssuerIdBelongsToOwnBankIMD", env, inputParams);
				// issuerIdFlag

				if (issuerIdFlag) {
					// Placeholder21 variable
				} else {
					Map inputParams2 = new HashMap<>();
					Map outputParams2 = MFExecuter.executeMF("UB_ATM_GetExternalSettlementAcc", env, inputParams);
					// PlaceHolder19 Variable
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
		if (0 == amountRecon) {
			//amountReconCurrency to be fetched from Startstep>> FinancialDetail
			Map inputParams2 = new HashMap<>();
			inputParams2.put(amountReconCurrency)
			Map outputParams2 = MFExecuter.executeMF("EnquireCurrencyCodeFromNumericCode", env, inputParams);
			//ISOCURRENCYCODE from outputParams2
			//PlaceHolder 110 Variable
			
			
		} else {
			
			//amountCardHolderBillingCurrency to be fetched from Startstep>> FinancialDetail
			Map inputParams2 = new HashMap<>();
			inputParams2.put(amountCardHolderBillingCurrency)
			Map outputParams3 = MFExecuter.executeMF("EnquireCurrencyCodeFromNumericCode", env, inputParams);
			//ISOCURRENCYCODE from outputParams3
			
		}
		// currencyCode to be fetched from Startstep>> FinancialDetail
		if(currencyCode == ISOCURRENCYCODE) {
			//Variable PlaceHolder 85 -- CurrencyCode >>  endFatom{FinalCurrencyCode=CurrencyCode}
		}
		else {
			//PlaceHolder121 Variable {processRecon}
			
			if(processRecon == true) {
				//placeholder123 reconCurr to be mapped to Placeholder 84
			}
			else {
				//placeholder124 reconCurr to be mapped to Placeholder 84
			}
			//Placeholder 84 variable to be mapped to Placeholder 86
			
			if ((isMessageAdvice == true)||(isMessageRepeatAdvice== true)){
				
				Map inputParams2 = new HashMap<>();
				Map outputParams3 = MFExecuter.executeMF("UB_ATM_ValidateAccount_SRV", env, inputParams);
				
				//PlaceHolder97 Variable  {Closed_Error and No_Account_Error}
				
				//ErrorCode to be fetched from outputParams3
				if (ErrorCode!="") {
					
					if (ErrorCode==Closed_Error||ErrorCode==No_Account_Error) {
						//to be mapped to PlaceHolder86{ErrorCode}
						
					}
					else {
						Map inputParams4 = new HashMap<>();
						Map outputParams4 = MFExecuter.executeMF("UB_ATM_GetATMDebitSuspAcc99", env, inputParams);
						
					}
				}
				else {
					//Mapping to placeHolder86
				}
				
			}
			else {
				//Mapping to placeHolder86
			}
			Map inputParams4 = new HashMap<>();
			Map outputParams4 = MFExecuter.executeMF("UB_ATM_GetExternalSettlementAcc23", env, inputParams);
		
			//Placeholder18 Variable
		}
		// Placeholder 44 variable  {errorWithoutForcePost}
		 
		if (errorWithoutForcePost) {
			//Set Error code to end fatom Variable
			FinalErrorCode = ErrorCode; //Errorcode of Placeholdr44
		}
		else {
			Map inputParams4 = new HashMap<>();
			Map outputParams4 = MFExecuter.executeMF("UB_ATM_FetchAtmTxnCodeDtls_SRV59", env, inputParams);
		
			FinalErrorCode = (String)outputParams4.get("errorCode");
			
			
			if(FinalErrorCode.equals("")) {
				Map inputParams5 = new HashMap<>();
				Map outputParams5 = MFExecuter.executeMF("UB_ATM_TransactionNarrative53", env, inputParams);
				
				Map inputParams6 = new HashMap<>();
				inputParams6.put(AccountNo1)
				Map outputParams6 = MFExecuter.executeMF("UB_CMN_FetchAccountService", env, inputParams);
				
				
				Map inputParams7 = new HashMap<>();
				inputParams6.put(AccountNo2)
				Map outputParams7 = MFExecuter.executeMF("UB_CMN_FetchAccountService", env, inputParams);
				
				//conditional Step 92
				if (flag) {
					//placeholder 93 {toCurrencyCode}
					
				}
				else {
					//placeholder 94 {toCurrencyCode}
				}
				
				Map inputParams8 = new HashMap<>();
				inputParams6.put(toCurrencyCode);
				Map outputParams8 = MFExecuter.executeMF("UB_ATM_GetExcahgeRateDtlsOnTxnCode56", env, inputParams);
				
				Map inputParams8 = new HashMap<>();
				Map outputParams8 = MFExecuter.executeMF("UB_ATM_GetTransactionNarrative_SRV79", env, inputParams);
				
				
				if(flag) {
					//processRecon from Placeholder 84
					if (processRecon){
						//Placeholder 117, to be mapper to placeholder 89 variable
					}
					else {
						//Placeholder 118 , to be mapped to placeholder 89 variable
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
}}