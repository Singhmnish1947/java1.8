package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.compliance.types.PaymentsComplianceCheckRequest;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.microflow.runtime.exception.MicroflowIsParkedException;
import com.misys.bankfusion.subsystem.task.runtime.eventcode.TasksEventCodes;
import com.misys.ub.charges.ChargeConstants;
import com.misys.ub.compliance.postings.InternetBankingComplianceCheckHelper;
import com.misys.ub.forex.configuration.ForexModuleConfiguration;
import com.misys.ub.interfaces.IfmConstants;
import com.misys.ub.interfaces.UB_IBI_PaymentsHelper;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.events.ErrorEvent;
import com.trapedza.bankfusion.exceptions.CollectedEventsDialogException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.servercommon.services.autonumber.IAutoNumberService;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_IBI_PostSwiftTransferTxnFatom;
import com.trapedza.bankfusion.utils.GUIDGen;

public class UB_IBI_PostSwiftTransferTxnFatom extends AbstractUB_IBI_PostSwiftTransferTxnFatom {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private transient final static Log logger = LogFactory.getLog(UB_IBI_PostSwiftTransferTxnFatom.class.getName());
	private String CreditAccountNumber = CommonConstants.EMPTY_STRING;
	private String creditCurrency = CommonConstants.EMPTY_STRING;
	boolean error = false;
	private boolean referral = false;
	private boolean collectedReferral = false;
	public static final String UPDATE_CHARGES_MFID = "UB_CHG_UpdateChargeHistory_SRV";
	public static final String TRANSACTIONWHERECLAUSE = " WHERE " + IBOTransaction.REFERENCE + " = ?";

	public UB_IBI_PostSwiftTransferTxnFatom(BankFusionEnvironment env) {
		super();
	}

	public void process(BankFusionEnvironment env) {
	    IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory.getInstance()
                .getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
		Map accDetails = new HashMap();
		String ifmId = getF_IN_IFMID();
		String txnReference = CommonConstants.EMPTY_STRING;
		IAutoNumberService autoNumService = (IAutoNumberService) ServiceManager.getService(ServiceManager.AUTO_NUMBER_SERVICE);
		String branchSortCode = CommonConstants.EMPTY_STRING;
		String chgFundingAccount = CommonConstants.EMPTY_STRING;
		String chgFundingAccountCurrency = CommonConstants.EMPTY_STRING;
		String debitCurrency = CommonConstants.EMPTY_STRING;
		String txnCurrency = CommonConstants.EMPTY_STRING;
		BigDecimal txnAmtInCreditAccCurrency = CommonConstants.BIGDECIMAL_ZERO;
		BigDecimal totalChargeDebitAccCurrency = CommonConstants.BIGDECIMAL_ZERO;
		BigDecimal totalChargeCreditAccCurrency = CommonConstants.BIGDECIMAL_ZERO;
		BigDecimal TotalCharge = CommonConstants.BIGDECIMAL_ZERO;
		BigDecimal debitAmount = CommonConstants.BIGDECIMAL_ZERO;
		BigDecimal txnAmtInTxnCurrency = new BigDecimal(getF_IN_TRANSACTIONAMOUNT());
		String spotPseudonym = CommonConstants.EMPTY_STRING;
		String positionAccountContext = CommonConstants.EMPTY_STRING;
		String debitAccBranchSortCode = CommonConstants.EMPTY_STRING;
		String creditAccBranchSortCode = CommonConstants.EMPTY_STRING;
		String moduleName = "KYC";
	    String paramsName = "IBI_PAYMENTS";
		VectorTable vector = null;

		try {
			vector = getF_IN_CHARGEVECTOR();
			txnReference = ifmId + ":" + autoNumService.getNextAutoRef(getF_IN_DEBITTRANSACTIONCODE(), branchSortCode);
			spotPseudonym = ForexModuleConfiguration.getSpotPositionPseudonym();
			positionAccountContext = (String) ubInformationService.getBizInfo().getModuleConfigurationValue(
					IfmConstants.SYS_MODULE_CONFIG_KEY, IfmConstants.SYS_POSITION_CONTEXT, null);

			/*
			 * Fetch debit account details
			 */
			accDetails = UB_IBI_PaymentsHelper.getAccountDetails(getF_IN_DEBITACCOUNT());
			branchSortCode = (String) accDetails.get("BRANCHSORTCODE");
			chgFundingAccount = (String) accDetails.get("CHARGEFUNDINGACCOUNT");
			debitCurrency = (String) accDetails.get("ISOCURRENCYCODE");
			creditCurrency = getF_IN_CREDITCURRENCY();
			// In case of IBI, txn currency and debit currency are always same
			txnCurrency = getF_IN_DEBITCURRENCY();
			debitAccBranchSortCode = (String) accDetails.get("BRANCHSORTCODE");
			creditAccBranchSortCode = (String) UB_IBI_PaymentsHelper.getAccountDetails(getF_IN_CREDITACCOUNT()).get("BRANCHSORTCODE");

			/*
			 * Finding charge funding account and charge funding account
			 * currency
			 */
			if (chgFundingAccount == null || chgFundingAccount.equals(CommonConstants.EMPTY_STRING)) {
				chgFundingAccount = getF_IN_DEBITACCOUNT();
				chgFundingAccountCurrency = debitCurrency;

			} else {
				chgFundingAccountCurrency = (String) UB_IBI_PaymentsHelper.getAccountDetails(chgFundingAccount).get("ISOCURRENCYCODE");
			}
			/*
			 * / Validating transaction amount
			 */

			if (!UB_IBI_PaymentsHelper.validateAmount(getF_IN_TRANSACTIONAMOUNT())) {
				setF_OUT_ERRORMESSAGE("NAKAMOUNTINV");
				return;
			}

			/*
			 * Getting settlement/suspense account if credit account is not
			 * found
			 */

			String context = ubInformationService.getBizInfo().getModuleConfigurationValue(
					IfmConstants.MODULE_NAME, IfmConstants.SETTLEMENTACCT_CONTEXT_KEY, env).toString();

			String value = ubInformationService.getBizInfo().getModuleConfigurationValue(
					IfmConstants.MODULE_NAME, IfmConstants.nostroPseudoName, env).toString();
			CreditAccountNumber = UB_IBI_PaymentsHelper.getAccountFromPuedoNymes(value, creditCurrency, context, branchSortCode);

			if (CreditAccountNumber == null
					|| CreditAccountNumber.equals(CommonConstants.EMPTY_STRING)
					|| (UB_IBI_PaymentsHelper.validateAccount(CreditAccountNumber, IfmConstants.CR)
							.equalsIgnoreCase(IfmConstants.NAKCRACCTINV))
					|| UB_IBI_PaymentsHelper.isAccountDormant(CreditAccountNumber, getF_IN_CREDITTRANSACTIONCODE())) {
				value = ubInformationService.getBizInfo().getModuleConfigurationValue(IfmConstants.MODULE_NAME,
						IfmConstants.SUSPENSEACCOUNT, env).toString();
				CreditAccountNumber = UB_IBI_PaymentsHelper.getAccountFromPuedoNymes(value, creditCurrency, context, branchSortCode);
				if (CreditAccountNumber == null || CreditAccountNumber.equals(CommonConstants.EMPTY_STRING)) {
					setF_OUT_ERRORMESSAGE(IfmConstants.NAKCRACCTINV);
					return;
				}
			}
			/*
			 * Validating credit account
			 */

			if ((UB_IBI_PaymentsHelper.validateAccount(CreditAccountNumber, IfmConstants.CR).equalsIgnoreCase(IfmConstants.NAKCRACCTINV))
					|| UB_IBI_PaymentsHelper.isAccountDormant(CreditAccountNumber, getF_IN_CREDITTRANSACTIONCODE())) {
				setF_OUT_ERRORMESSAGE(IfmConstants.NAKCRACCTINV);
				return;
			}

			/*
			 * Cross currency stuff between transaction currency and credit
			 * account currency
			 */
			if (!(getF_IN_CREDITCURRENCY().equalsIgnoreCase(txnCurrency))) {
				if ((UB_IBI_PaymentsHelper.getExchangeRate(getF_IN_EXCHANGERATETYPE(), txnCurrency, getF_IN_CREDITCURRENCY(),
						txnAmtInTxnCurrency)) == null) {
					setF_OUT_ERRORMESSAGE("NAKNORATES");
					return;
				}

				txnAmtInCreditAccCurrency = UB_IBI_PaymentsHelper.currencyConversion(txnCurrency, getF_IN_CREDITCURRENCY(),
						txnAmtInTxnCurrency, getF_IN_EXCHANGERATETYPE(), true);
			} else {
				txnAmtInCreditAccCurrency = txnAmtInTxnCurrency;
			}
			/*
			 * Validating debit account for dormancy/close/password flag
			 */
			if (UB_IBI_PaymentsHelper.isAccountDormant(getF_IN_DEBITACCOUNT(), getF_IN_DEBITTRANSACTIONCODE()) == true) {
				setF_OUT_ERRORMESSAGE(IfmConstants.NAKDRACCTINV);
				return;
			}

			if (UB_IBI_PaymentsHelper.validateAccount(getF_IN_DEBITACCOUNT(), IfmConstants.DR).equalsIgnoreCase(IfmConstants.NAKDRACCTINV)) {
				setF_OUT_ERRORMESSAGE(IfmConstants.NAKDRACCTINV);
				return;
			}
			/*
			 * Cross currency stuff between transaction currency and debit
			 * account currency but debit currency and transaction currency will
			 * be same in this case
			 */
			if (!(debitCurrency.equalsIgnoreCase(txnCurrency))) {
				if (UB_IBI_PaymentsHelper.getExchangeRate(getF_IN_EXCHANGERATETYPE(), debitCurrency, getF_IN_DEBITCURRENCY(),
						txnAmtInTxnCurrency) == null) {
					setF_OUT_ERRORMESSAGE("NAKNORATES");
					return;
				}
				debitAmount = UB_IBI_PaymentsHelper.currencyConversion(txnCurrency, debitCurrency, txnAmtInTxnCurrency,
						getF_IN_EXCHANGERATETYPE(), true);

			} else {
				debitAmount = txnAmtInTxnCurrency;
			}

			/*
			 * Validating charge funding account if charges are applicable and
			 * charge funding account is not same as main account
			 */
			if (vector.size() > 0) {
				if (!(chgFundingAccount.equalsIgnoreCase(getF_IN_DEBITACCOUNT()))) {
					if ((UB_IBI_PaymentsHelper.validateAccount(chgFundingAccount, IfmConstants.DR)
							.equalsIgnoreCase(IfmConstants.NAKDRACCTINV))
							|| UB_IBI_PaymentsHelper.isAccountDormant(chgFundingAccount, getF_IN_DEBITTRANSACTIONCODE())) {
						setF_OUT_ERRORMESSAGE(IfmConstants.NAKDRACCTINV);
						return;
					}
				}
			}

			/*
			 * Checking the availability of position accounts
			 */
			if (!(debitCurrency.equalsIgnoreCase(getF_IN_CREDITCURRENCY()))) {
				if (UB_IBI_PaymentsHelper.arePositionAccountsAvailable(debitCurrency, getF_IN_CREDITCURRENCY(), spotPseudonym,
						positionAccountContext, debitAccBranchSortCode, creditAccBranchSortCode) == false) {
					setF_OUT_ERRORMESSAGE("NAK");
					return;
				}
			}

			/*
			 * Available balance check for debit account and charge funding
			 * account
			 */

			if (!(UB_IBI_PaymentsHelper.availableBalanceCheck(vector, debitAmount, getF_IN_DEBITACCOUNT(), chgFundingAccount))) {
				setF_OUT_ERRORMESSAGE(IfmConstants.NAKNOFUNDS);
				return;
			}
			String transactionID = GUIDGen.getNewGUID();

			/*
			 * artf731655 - Changes Starts If ChargeOption is 'BEN' the Credit
			 * Amount to nostro should be after detecting the respective charge
			 * amount
			 */
			TotalCharge = getF_IN_TOTALCHARGE().add(getF_IN_TOTALTAX());

			if (getF_IN_CHARGEOPTION().equals("BEN") && getF_IN_ISINCLUSSIVE().equals("1")) {
				if (!(chgFundingAccountCurrency.equals(debitCurrency))) {
					totalChargeDebitAccCurrency = UB_IBI_PaymentsHelper.currencyConversion(chgFundingAccountCurrency, debitCurrency,
							TotalCharge, getF_IN_EXCHANGERATETYPE(), true);
					debitAmount = debitAmount.subtract(totalChargeDebitAccCurrency);
				} else {
					debitAmount = debitAmount.subtract(TotalCharge);
				}
				if (!(chgFundingAccountCurrency.equals(txnCurrency))) {
					totalChargeCreditAccCurrency = UB_IBI_PaymentsHelper.currencyConversion(chgFundingAccountCurrency, txnCurrency,
							TotalCharge, getF_IN_EXCHANGERATETYPE(), true);
					txnAmtInCreditAccCurrency = txnAmtInCreditAccCurrency.subtract(totalChargeCreditAccCurrency);
					TotalCharge = totalChargeCreditAccCurrency;
				} else {
					txnAmtInCreditAccCurrency = txnAmtInCreditAccCurrency.subtract(TotalCharge);
				}
			} // artf731655 - Changes End


			// Call to fircosoft here
			Boolean checkRequiredForFirco = (Boolean) ((IBusinessInformation) ubInformationService.getBizInfo())
			.getModuleConfigurationValue(IfmConstants.MODULE_KYC, IfmConstants.IBI_PAYMENTS_PARAM, env);

			if(checkRequiredForFirco)
			{
				InternetBankingComplianceCheckHelper mpfpPostCheck = new InternetBankingComplianceCheckHelper();
				PaymentsComplianceCheckRequest paymentsComplianceCheckRequest = new PaymentsComplianceCheckRequest();
				paymentsComplianceCheckRequest.setSwiftPaymentDetails(getF_IN_SwiftPaymentDetails());
				mpfpPostCheck.checkComplianceForPostingDetails(getF_IN_DEBITACCOUNT(), debitAmount, debitCurrency, false,
						paymentsComplianceCheckRequest);
			}
			/*
			 * mpfpPostCheck.checkComplianceForPostingDetails(CreditAccountNumber
			 * , txnAmtInCreditAccCurrency, getF_IN_CREDITCURRENCY(), false, new
			 * PaymentsComplianceCheckRequest());
			 */

			/*
			 * Main payment transfer transaction
			 */

			UB_IBI_PaymentsHelper.posting(getF_IN_DEBITTRANSACTIONCODE(), getF_IN_EXCHANGERATETYPE(), CreditAccountNumber, debitAmount,
					getF_IN_CREDITTRANSACTIONCODE(), getF_IN_DEBITACCOUNT(), getF_IN_DEBITTRANSACTIONNARRATIVE(),
					getF_IN_CREDITTRANSACTIONNARRATIVE(), debitCurrency, getF_IN_CREDITCURRENCY(), txnAmtInCreditAccCurrency, txnReference,
					transactionID);

			HashMap map = new HashMap();

			for (int i = 0; i < vector.size(); i++) {
				if (error)
					return;
				map = vector.getRowTags(i);
				/*
				 * Online charge posting
				 */
				UB_IBI_PaymentsHelper.posting((String) map.get(IfmConstants.CHARGEPOSTINGTXNCODE), (String) map
						.get(IfmConstants.CHARGEEXCHANGERATETYPE), (String) map.get(IfmConstants.CHARGERECIEVINGACCOUNT), (BigDecimal) map
						.get(IfmConstants.CHARGEAMOUNT_IN_FUND_ACC_CURRENCY), (String) map.get(IfmConstants.CHARGEPOSTINGTXNCODE),
						chgFundingAccount, (String) map.get(IfmConstants.CHARGENARRATIVE), (String) map.get(IfmConstants.CHARGENARRATIVE),
						chgFundingAccountCurrency, (String) map.get(IfmConstants.CHARGECURRENCY), (BigDecimal) map
								.get(IfmConstants.CHARGEAMOUNT), txnReference, transactionID);

				if (error)
					return;

				/* Tax posting for charges */

				UB_IBI_PaymentsHelper.posting((String) map.get(IfmConstants.TAXPOSTINGTXNCODE), (String) map
						.get(IfmConstants.TAXEXCHANGERATETYPE), (String) map.get(IfmConstants.TAXRECIEVINGACCOUNT), (BigDecimal) map
						.get(IfmConstants.TAXAMOUNT), (String) map.get(IfmConstants.TAXPOSTINGTXNCODE), chgFundingAccount, (String) map
						.get(IfmConstants.TAXNARRATIVE), (String) map.get(IfmConstants.TAXNARRATIVE), chgFundingAccountCurrency, (String) map
						.get(IfmConstants.TAXCURRENCY), (BigDecimal) map.get(IfmConstants.TAXAMOUNT), txnReference, transactionID);
				if (error)
					return;

			}
			HashMap<String, Object> MfParams = new HashMap<String, Object>();
			MfParams.put("ChargeDetailsVector", getF_IN_CHARGEVECTOR());
			MfParams.put("AccountId", getF_IN_DEBITACCOUNT());
			MfParams.put("TransactionId", transactionID);
			MfParams.put("CHARGEFUNDINGACCOUNTID", chgFundingAccount);
			MfParams.put("CHARGEINDICATOR", ChargeConstants.ONLINE_CHARGE_INDICATOR.intValue());
			MfParams.put("TRANSACTIONCODE", getF_IN_DEBITTRANSACTIONCODE());
			MFExecuter.executeMF(UPDATE_CHARGES_MFID, env, MfParams);
			if (error)
				return;

			setF_OUT_CREDITACCOUNTNUMBER(CreditAccountNumber);
			setF_OUT_CHARGEAMOUNT(TotalCharge);
			setF_OUT_INSTRUCTEDAMOUNT(new BigDecimal(getF_IN_TRANSACTIONAMOUNT())); // artf731655
			// -
			// Actual
			// Transaction
			// Amount
			// to
			// be
			// populated.
			setF_OUT_TRASACTIONAMOUNT(txnAmtInCreditAccCurrency); // artf731655
			// - Actual
			// Credit
			// Amount to
			// Nostro
			// Account
			setF_OUT_EXCHANGERATE(UB_IBI_PaymentsHelper.getExchangeRate(getF_IN_EXCHANGERATETYPE(), debitCurrency, getF_IN_DEBITCURRENCY(),
					txnAmtInTxnCurrency));
		} catch (CollectedEventsDialogException collectedEventsDialogException) {
            List<ErrorEvent> errors = collectedEventsDialogException.getErrors();
			error = true;
			setF_OUT_ERRORMESSAGE("NAK");
            for (ErrorEvent runTimeError : errors) {
                if (runTimeError.getEventNumber() == TasksEventCodes.REFERRAL_WAS_REJECTED) {
                	error = false;
					collectedReferral = true;
                	throw collectedEventsDialogException;
                }
            }
		} catch (BankFusionException e) {
			error = true;
			setF_OUT_ERRORMESSAGE("NAK");
            logger.error(ExceptionUtil.getExceptionAsString(e));
		} catch(MicroflowIsParkedException microflowIsParkedException) {
			referral = true;
			throw microflowIsParkedException;
		}
		catch (Exception exception) {
				error = true;
				setF_OUT_ERRORMESSAGE("NAK");
		} finally {
			if (error) {
				BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
			} else if(!collectedReferral && !referral){
				BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
			}
			BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
			error = false;
		}

	}

}
