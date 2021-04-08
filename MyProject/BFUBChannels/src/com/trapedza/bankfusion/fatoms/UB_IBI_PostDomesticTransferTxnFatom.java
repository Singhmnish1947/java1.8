package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.microflow.runtime.exception.MicroflowIsParkedException;
import com.misys.bankfusion.subsystem.task.runtime.eventcode.TasksEventCodes;
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
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_IBI_PostDomesticTransferTxnFatom;
import com.trapedza.bankfusion.utils.GUIDGen;

import bf.com.misys.compliance.types.PaymentsComplianceCheckRequest;

public class UB_IBI_PostDomesticTransferTxnFatom extends AbstractUB_IBI_PostDomesticTransferTxnFatom {

	private transient final static Log logger = LogFactory.getLog(UB_IBI_PostDomesticTransferTxnFatom.class.getName());
	private String creditAccountNumber = CommonConstants.EMPTY_STRING;
	private String creditCurrency = CommonConstants.EMPTY_STRING;
    private static Map<String, Map> accountLockMap = new ConcurrentHashMap<String, Map>();
	public static final String UPDATE_CHARGES_MFID = "UB_CHG_UpdateChargeHistory_SRV";
	public static final String TRANSACTIONWHERECLAUSE = " WHERE " + IBOTransaction.REFERENCE + " = ?";
	boolean error = false;
	private boolean referral = false;
	private boolean collectedReferral = false;

	public UB_IBI_PostDomesticTransferTxnFatom(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) {
	    IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory.getInstance()
                .getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
	    Map accDetails = new HashMap();
		String ifmId = getF_IN_IFMID();
		String txnReference = CommonConstants.EMPTY_STRING;
		String branchSortCode = CommonConstants.EMPTY_STRING;
		String chgFundingAccount = CommonConstants.EMPTY_STRING;
		String chgFundingAccountCurrency = CommonConstants.EMPTY_STRING;
		String debitCurrency = CommonConstants.EMPTY_STRING;
		String txnCurrency = CommonConstants.EMPTY_STRING;
		BigDecimal txnAmtInCreditAccCurrency = CommonConstants.BIGDECIMAL_ZERO;
		BigDecimal debitAmount = CommonConstants.BIGDECIMAL_ZERO;
		BigDecimal txnAmtInTxnCurrency = new BigDecimal(getF_IN_TRANSACTIONAMOUNT());
		String spotPseudonym = CommonConstants.EMPTY_STRING;
		String positionAccountContext = CommonConstants.EMPTY_STRING;
		String debitAccBranchSortCode = CommonConstants.EMPTY_STRING;
		String creditAccBranchSortCode = CommonConstants.EMPTY_STRING;
		String chgRecievingAccountCurrency = CommonConstants.EMPTY_STRING;
		/*
		 * Checking if credit account is passed as part of the message
		 */
		int numOfRecsFound = 0;
		numOfRecsFound = (Integer) UB_IBI_PaymentsHelper.getAccountDetails(getF_IN_CREDITACCOUNT()).get("NOOFROWS");

		IAutoNumberService autoNumService = (IAutoNumberService) ServiceManager.getService(ServiceManager.AUTO_NUMBER_SERVICE);
		VectorTable vector = null;

		try {
			vector = getF_IN_CHARGEVECTOR();
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
			 * Validaing value date
			 */

			if (!UB_IBI_PaymentsHelper.validateDate(getF_IN_VALUEDATE())) {
				// TODO Send NAK message
				if (logger.isInfoEnabled())
					logger.info("The Value date is Invalid");
				setF_OUT_ERRORMESSAGE(IfmConstants.NAKVALUEDATEINV);
				return;
			}

			/*
			 * Validating transaction amount
			 */

			if (!UB_IBI_PaymentsHelper.validateAmount(getF_IN_TRANSACTIONAMOUNT())) {
				setF_OUT_ERRORMESSAGE("NAKAMOUNTINV");
				return;
			}

			/*
			 * Checking if credit account is passed in the message
			 */
			if (getF_IN_CREDITSORTCODE().equalsIgnoreCase(CommonConstants.EMPTY_STRING) || numOfRecsFound > 0) {
				creditAccountNumber = getF_IN_CREDITACCOUNT();

			} else {
				/*
				 * Getting settlement/suspense account if credit account is not
				 * found
				 */

				String context = ubInformationService.getBizInfo().getModuleConfigurationValue(
						IfmConstants.MODULE_NAME, IfmConstants.SETTLEMENTACCT_CONTEXT_KEY, env).toString();

				String value = ubInformationService.getBizInfo().getModuleConfigurationValue(
						IfmConstants.MODULE_NAME, IfmConstants.domesticSettlementPseudoName, env).toString();
				creditAccountNumber = UB_IBI_PaymentsHelper.getAccountFromPuedoNymes(value, creditCurrency, context, branchSortCode);

				if (creditAccountNumber == null
						|| creditAccountNumber.equals(CommonConstants.EMPTY_STRING)
						|| (UB_IBI_PaymentsHelper.validateAccount(creditAccountNumber, IfmConstants.CR)
								.equalsIgnoreCase(IfmConstants.NAKCRACCTINV))
						|| UB_IBI_PaymentsHelper.isAccountDormant(creditAccountNumber, getF_IN_CREDITTRANSACTIONCODE())) {
					value = ubInformationService.getBizInfo().getModuleConfigurationValue(
							IfmConstants.MODULE_NAME, IfmConstants.SUSPENSEACCOUNT, env).toString();
					creditAccountNumber = UB_IBI_PaymentsHelper.getAccountFromPuedoNymes(value, creditCurrency, context, branchSortCode);
					if (creditAccountNumber == null || creditAccountNumber.equals(CommonConstants.EMPTY_STRING)) {
						setF_OUT_ERRORMESSAGE(IfmConstants.NAKCRACCTINV);
						return;
					}

				}

			}
			/*
			 * Applying locks on both the accounts
			 */
			lockAccounts(getF_IN_DEBITACCOUNT(), creditAccountNumber);
			/*
			 * ending previous transaction and opening new
			 */
			BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
			BankFusionThreadLocal.getPersistanceFactory().beginTransaction();

			/*
			 * Validating credit account
			 */

			if ((UB_IBI_PaymentsHelper.validateAccount(creditAccountNumber, IfmConstants.CR).equalsIgnoreCase(IfmConstants.NAKCRACCTINV))
					|| UB_IBI_PaymentsHelper.isAccountDormant(creditAccountNumber, getF_IN_CREDITTRANSACTIONCODE())) {
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

			if ((UB_IBI_PaymentsHelper.validateAccount(getF_IN_DEBITACCOUNT(), IfmConstants.DR).equalsIgnoreCase(IfmConstants.NAKDRACCTINV))) {
				setF_OUT_ERRORMESSAGE(IfmConstants.NAKDRACCTINV);
				return;
			}

			/*
			 * Cross currency stuff between transaction currency and debit
			 * account currency
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
			// Call for the Fircocheck

			Boolean checkRequiredForFirco = (Boolean) ((IBusinessInformation) ubInformationService.getBizInfo())
			.getModuleConfigurationValue(IfmConstants.MODULE_KYC, IfmConstants.IBI_PAYMENTS_PARAM, env);

			if(checkRequiredForFirco){

				InternetBankingComplianceCheckHelper mpfpPostCheck = new InternetBankingComplianceCheckHelper();
				PaymentsComplianceCheckRequest paymentsComplianceCheckRequest = new PaymentsComplianceCheckRequest();
				paymentsComplianceCheckRequest.setDomesticPaymentDetails(getF_IN_DomesticPaymentDetails());
				mpfpPostCheck.checkComplianceForPostingDetails(getF_IN_DEBITACCOUNT(), debitAmount, debitCurrency, false,
						paymentsComplianceCheckRequest);
			}

			/*
			 * mpfpPostCheck.checkComplianceForPostingDetails(creditAccountNumber
			 * , txnAmtInCreditAccCurrency, getF_IN_CREDITCURRENCY(), false, new
			 * PaymentsComplianceCheckRequest());
			 */

			txnReference = ifmId + ":" + autoNumService.getNextAutoRef(getF_IN_DEBITTRANSACTIONCODE(), debitAccBranchSortCode);
			String transactionID = GUIDGen.getNewGUID();
			/*
			 * Main payment transfer transaction
			 */

			UB_IBI_PaymentsHelper.posting(getF_IN_DEBITTRANSACTIONCODE(), getF_IN_EXCHANGERATETYPE(), creditAccountNumber, debitAmount,
					getF_IN_CREDITTRANSACTIONCODE(), getF_IN_DEBITACCOUNT(), getF_IN_DEBITTRANSACTIONNARRATIVE(),
					getF_IN_CREDITTRANSACTIONNARRATIVE(), debitCurrency, getF_IN_CREDITCURRENCY(), txnAmtInCreditAccCurrency, txnReference,
					transactionID);

			HashMap map = new HashMap();
			for (int i = 0; i < vector.size(); i++) {
				if (error)
					return;
				map = vector.getRowTags(i);

				chgRecievingAccountCurrency = (String) map.get("CHARGECURRENCY");

				if (!(chgFundingAccountCurrency.equalsIgnoreCase(chgRecievingAccountCurrency))) {
					if (UB_IBI_PaymentsHelper.arePositionAccountsAvailable(chgFundingAccountCurrency, chgRecievingAccountCurrency,
							spotPseudonym, positionAccountContext, debitAccBranchSortCode, creditAccBranchSortCode) == false) {
						setF_OUT_ERRORMESSAGE("NAK");
						return;
					}
				}

				/* Online charge posting */

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
			}
			HashMap<String, Object> MfParams = new HashMap<String, Object>();
			MfParams.put("ChargeDetailsVector", getF_IN_CHARGEVECTOR());
			MfParams.put("AccountId", getF_IN_DEBITACCOUNT());
			MfParams.put("TransactionId", transactionID);
			MfParams.put("CHARGEFUNDINGACCOUNTID", chgFundingAccount);
			MfParams.put("CHARGEINDICATOR", CommonConstants.INTEGER_ZERO.intValue());
			MfParams.put("TRANSACTIONCODE", getF_IN_DEBITTRANSACTIONCODE());
			MFExecuter.executeMF(UPDATE_CHARGES_MFID, env, MfParams);
			if (error)
				return;
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
		} catch (Exception exception) {
			error = true;
			setF_OUT_ERRORMESSAGE("NAK");
			
		} finally {
			unlockAccounts(getF_IN_DEBITACCOUNT(),creditAccountNumber);
			if (error) {
				BankFusionThreadLocal.getPersistanceFactory().rollbackTransaction();
			} else if(!collectedReferral && !referral){
				BankFusionThreadLocal.getPersistanceFactory().commitTransaction();
			}
			BankFusionThreadLocal.getPersistanceFactory().beginTransaction();
			error = false;
		}

	}

    private void lockAccounts(String account1, String account2) {

        if (accountLockMap.get(BankFusionThreadLocal.getUserZone()) == null) {

            accountLockMap.putIfAbsent(BankFusionThreadLocal.getUserZone(), new HashMap<String, String>());

        }

        synchronized (accountLockMap.get(BankFusionThreadLocal.getUserZone())) {
            if (accountLockMap.get(BankFusionThreadLocal.getUserZone()).containsKey(account1)
                    || accountLockMap.get(BankFusionThreadLocal.getUserZone()).containsKey(account2)) {
                if (logger.isInfoEnabled()) {
                    logger.debug(Thread.currentThread().getName() + ": " + "THE Account " + account1
                            + " IS LOCKED -> SENDING TO RETRY QUEUE");
                }
                try {
                    accountLockMap.get(BankFusionThreadLocal.getUserZone()).wait();
                }
                catch (InterruptedException exception) {
                    logger.error(Thread.currentThread().getName() + ": " + " Error while executing. ");
                    Thread.currentThread().interrupt();
                }
                lockAccounts(account1, account2);

            }
            else {
                if (logger.isDebugEnabled()) {
                    logger.debug(Thread.currentThread().getName() + ": " + "ENTERED LOCK FOR / " + account1 + " AND " + account2);
                }
                accountLockMap.get(BankFusionThreadLocal.getUserZone()).put(account1, null);
                accountLockMap.get(BankFusionThreadLocal.getUserZone()).put(account2, null);
                return;
            }
        }

    }

	private void unlockAccounts(String account1, String account2) {
		if (logger.isDebugEnabled()) {
			logger.debug(Thread.currentThread().getName() + ": " + "RELEASING LOCK FOR / " + creditCurrency);
		}
        HashMap<String, String> lockMap = (HashMap<String, String>) accountLockMap.get(BankFusionThreadLocal.getUserZone());
		synchronized (lockMap) {
			lockMap.remove(account1);
			lockMap.remove(account2);
			lockMap.notifyAll();
		}
	}

}
