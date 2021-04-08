package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.bankfusion.attributes.BFCurrencyAmount;
import bf.com.misys.bankfusion.attributes.PagedQuery;
import bf.com.misys.bankfusion.attributes.PagingRequest;
import bf.com.misys.cbs.msgs.v1r0.SearchAccountRq;
import bf.com.misys.cbs.msgs.v1r0.SearchAccountRs;
import bf.com.misys.cbs.services.ReadModuleConfigurationRq;
import bf.com.misys.cbs.services.ReadModuleConfigurationRs;
import bf.com.misys.cbs.types.AccountBasicDetails;
import bf.com.misys.cbs.types.AccountCharacteristics;
import bf.com.misys.cbs.types.AccountInfo;
import bf.com.misys.cbs.types.AccountKeys;
import bf.com.misys.cbs.types.AccountSearchForCustomers;
import bf.com.misys.cbs.types.AcctBalance;
import bf.com.misys.cbs.types.AcctBalances;
import bf.com.misys.cbs.types.CbsBfCurrencyAmount;
import bf.com.misys.cbs.types.CustomerShortDetails;
import bf.com.misys.cbs.types.InputAccount;
import bf.com.misys.cbs.types.ListAccountDetails;
import bf.com.misys.cbs.types.ListAccounts;
import bf.com.misys.cbs.types.ModuleKeyRq;
import bf.com.misys.cbs.types.Pseudonym;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.SubCode;
import bf.com.misys.cbs.userexit.types.AccountListRestrictionDtls;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.microflow.runtime.impl.MFExecuter;
import com.misys.bankfusion.subsystem.persistence.SimplePersistentObject;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.PagingData;
import com.misys.ub.common.events.CommonsEventCodes;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAccountLimitFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOCB_CUR_CurrencyView;
import com.trapedza.bankfusion.bo.refimpl.IBOChequeBookType;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.bo.refimpl.IBOProductInheritance;
import com.trapedza.bankfusion.bo.refimpl.IBOPseudonymAccountMap;
import com.trapedza.bankfusion.bo.refimpl.IBOUBTB_PRODUCTFEATURE;
import com.trapedza.bankfusion.bo.refimpl.IBOUBVW_ACCOUNTCUSTOMERS;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_ACC_NormalAccounts;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_CNF_JOINTACCOUNT;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INT_MovementArrears;
import com.trapedza.bankfusion.bo.refimpl.UBTB_PRODUCTFEATUREID;
import com.trapedza.bankfusion.core.MetaDataEnum;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.AvailableBalanceFunction;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.servercommon.services.cache.ICacheService;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ACC_SearchAccountFatom_SRV;

public class UB_ACC_SearchAccountFatom_SRV extends
		AbstractUB_ACC_SearchAccountFatom_SRV {
	private transient final static Log logger = LogFactory
			.getLog(UB_ACC_SearchAccountFatom_SRV.class.getName());
	/**
     *
     */
	private static final long serialVersionUID = 1L;
	private static final IBusinessInformation bizInfo = ((IBusinessInformationService) ServiceManagerFactory.getInstance()
            .getServiceManager().getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();
	
	
	private static final String ACCT_ID = "ACCOUNTID";
	private static final String STATUS_NORMAL = "NORMAL";
	private static final String CHANNEL_ID = "UXP";
	private static final String STATUS_CLOSED = "CLOSED";
	private static final String STATUS_INACTIVE = "006";
	private static final String STATUS_ACTIVE = "ACTIVE";
	private static final String STATUS_DORMANT = "DORMANT";
	private static final String STATUS_RESTRICTED = "RESTRICTED";
	private static final String STATUS_ARREARS = "ARREARS";
	private static final String STATUS_DELINQUENT = "DELINQUENT";
	private static final String BT_ALTERNATE = "BT_ALTERNATE";
	private static final String IBANACCOUNT = "IBANACCOUNT";
	private static final String DELEQUENT_ID = "D";
	private static final String ARREAR_ID = "A";
	private static final String STRING_N = "N";
	private static final String INTERNAL_CUSTOMER = "I";
	// private static final String MFID_TOGET_AVAILABLEBALANCE =
	// "AvailableBalance0";
	private static final String READ_MODULE_CONFIGURATION_MFID = "CB_CMN_ReadModuleConfiguration_SRV";
	private static final String AVAILABLEBALANCE_IN_PARAM = "AccountID";
	private static final String AVAILABLEBALANCE_OUT_PARAM = "AvailableBalance";
	private static final String BANK_POSTING_MODULE = "BANKPOSTING";
	private static final String BANK_POSTING_ROUNDING_PARAM = "RoundingOperation";
	private static final int DEFAULT_ROUNDING_OPTION = BigDecimal.ROUND_HALF_EVEN;
	private static int ROUNDING_OPTION;
	private static Boolean ROUNDING_OPTION_AVAILABLE = Boolean.FALSE;
	private final ICacheService cache = (ICacheService) ServiceManager.getService(ServiceManager.CACHE_SERVICE);
    public static final String PROD_FTR_KEY = "PROD_FTR_KEY";
    public static final String PROD_FTR_CACHE_KEY = "PROD_FTR_CACHE_KEY";
    private static final String BANKSTATEMENTFTR = "BANKSTATEMENTFTR";
    private static final String CHQDTLSFTR = "CHQDTLSFTR";
   
	private IPersistenceObjectsFactory getFactory() {
		IPersistenceObjectsFactory objIPersistenceObjectsFactory = BankFusionThreadLocal
				.getPersistanceFactory();
		return objIPersistenceObjectsFactory;
	}
	private String countQuery = "SELECT COUNT(*) AS CNT ";
	private static final String CHEQUE_BOOK_AVAILABLE = "SELECT "
			+ IPersistenceObjectsFactory.COUNT_FUNCTION_CODE + "( "
			+ IBOChequeBookType.CHEQUEBOOKID + " ) AS CHQ_BOOK_COUNT FROM "
			+ IBOChequeBookType.BONAME + " WHERE "
			+ IBOChequeBookType.CHEQUETYPEID + " = ? ";

	private static final String QUERY = "SELECT A." + IBOAccount.ACCOUNTID
			+ " AS " + ACCT_ID + " ,A." + IBOAccount.ACCOUNTDESCRIPTION
			+ " AS " + IBOAccount.ACCOUNTDESCRIPTION + " ,A."
			+ IBOAccount.ACCOUNTNAME + " AS " + IBOAccount.ACCOUNTNAME + " ,A."
			+ IBOAccount.ISOCURRENCYCODE + " AS " + IBOAccount.ISOCURRENCYCODE
			+ " ,A." + IBOAccount.DORMANTSTATUS + " AS "
			+ IBOAccount.DORMANTSTATUS + " ,A." + IBOAccount.CLOSED + " AS "
			+ IBOAccount.CLOSED + " ,A." + IBOAccount.CLEAREDBALANCE + " AS "
			+ IBOAccount.CLEAREDBALANCE + " ,A." + IBOAccount.BOOKEDBALANCE
			+ " AS " + IBOAccount.BOOKEDBALANCE + " ,A."
			+ IBOAccount.PRODUCTCONTEXTCODE + " AS "
			+ IBOAccount.PRODUCTCONTEXTCODE + " ,A." + IBOAccount.OPENDATE
			+ " AS " + IBOAccount.OPENDATE + " ,A." + IBOAccount.BRANCHSORTCODE
			+ " AS " + IBOAccount.BRANCHSORTCODE + " ,A."
			+ IBOAccount.PRODUCTID + " AS " + IBOAccount.PRODUCTID + " ,A."
			+ IBOAccount.ACCRIGHTSINDICATOR + " AS "
			+ IBOAccount.ACCRIGHTSINDICATOR + " ,A." + IBOAccount.DEBITLIMIT
			+ " AS " + IBOAccount.DEBITLIMIT + " ,AL."
			+ IBOAccountLimitFeature.TEMPACCOUNTLIMIT + " AS "
			+ IBOAccountLimitFeature.TEMPACCOUNTLIMIT + " ,AL."
			+ IBOAccountLimitFeature.TEMPLIMEXPIRYDATE + " AS "
			+ IBOAccountLimitFeature.TEMPLIMEXPIRYDATE + " ,A."
			+ IBOAccount.CREDITLIMIT + " AS " + IBOAccount.CREDITLIMIT + " ,A."
			+ IBOAccount.STOPPED + " AS " + IBOAccount.STOPPED + " ,A."
			+ IBOAccount.JOINTACCOUNT + " AS " + IBOAccount.JOINTACCOUNT
			+ " ,C." + IBOCustomer.CUSTOMERCODE + " AS "
			+ IBOCustomer.CUSTOMERCODE + " ,C." + IBOCustomer.SHORTNAME
			+ " AS " + IBOCustomer.SHORTNAME + ", C."
			+ IBOCustomer.CUSTOMERTYPE + " AS " + IBOCustomer.CUSTOMERTYPE
			+ " ,AL." + IBOAccountLimitFeature.LIMITEXPIRYDATE + " AS "
			+ IBOAccountLimitFeature.LIMITEXPIRYDATE + " ,P."
			+ IBOProductInheritance.PRODUCTDESCRIPTION + " AS "
			+ IBOProductInheritance.PRODUCTDESCRIPTION;

	private static final String EXTERNAL_ACCID = "SELECT " + IBOPseudonymAccountMap.PSEUDONAME + " AS "
            + IBOPseudonymAccountMap.PSEUDONAME + " , " + IBOPseudonymAccountMap.SORTCONTEXTVALUE + " AS "
            + IBOPseudonymAccountMap.SORTCONTEXTVALUE + " FROM " + IBOPseudonymAccountMap.BONAME + " WHERE "
            + IBOPseudonymAccountMap.ACCOUNTID + " = ? AND ( " + IBOPseudonymAccountMap.SORTCONTEXTVALUE + "= ? OR "
            + IBOPseudonymAccountMap.SORTCONTEXTVALUE + " = ? )";

	private static final String IBAN = "SELECT "
			+ IBOPseudonymAccountMap.PSEUDONAME + " AS "
			+ IBOPseudonymAccountMap.PSEUDONAME + " FROM "
			+ IBOPseudonymAccountMap.BONAME + " WHERE "
			+ IBOPseudonymAccountMap.ACCOUNTID + " = ? AND "
			+ IBOPseudonymAccountMap.SORTCONTEXTVALUE + "=?";

	private static final String WILDCARD = "%";

	private static final String FIND_JOINTACCOUNT_WHERECLAUSE = " WHERE "
			+ IBOUB_CNF_JOINTACCOUNT.CUSTOMERCODE + " =?"; // change
															// this
															// to
															// IN
															// query
															// so
															// that
															// it
															// accepts
															// multiple
															// customer.

	/**
	 * @param env
	 */
	@SuppressWarnings("deprecation")
	public UB_ACC_SearchAccountFatom_SRV(BankFusionEnvironment env) {
		super(env);
	}

	@Override
	public void process(BankFusionEnvironment env) {
		SearchAccountRq reqObj = getF_IN_searchAccountRq(); // in request
															// changes based on
															// isMultipleCustomer
		SearchAccountRs resObj = getF_OUT_searchAccountRs();

		String status = reqObj.getAccountSearch().getAccountStatus();
		String custType = INTERNAL_CUSTOMER;

		PagingData pdate = new PagingData(reqObj.getPagedQuery()
				.getPagingRequest().getRequestedPage(), reqObj.getPagedQuery()
				.getPagingRequest().getNumberOfRows());
		pdate.setRequiresTotalPages(true);

		boolean include = false;
		ArrayList<String> params = new ArrayList<String>();
		String generatedQuery = "";
		if (reqObj.getAccountSearch() instanceof AccountSearchForCustomers) {

			AccountSearchForCustomers searchCustomers = (AccountSearchForCustomers) reqObj
					.getAccountSearch();

			HashSet<String> normalParties = new HashSet<String>();// non joint
																	// parties
			for (String partyId : searchCustomers.getPartyId()) {
				normalParties.add(partyId);
			}
			generatedQuery = prepareQuery(reqObj, status, custType, params,
					false, normalParties);// All
											// parties
											// with
											// primary
											// customer

		} else {

			include = includeJointAccountClause(reqObj.getAccountSearch()
					.getCustomerId());

			generatedQuery = prepareQuery(reqObj, status, custType, params,
					include, null);
		}
		  pdate.setTotalRecordsCountQuery(countQuery);
		// this is being called for a single customer to check whether it has
		// joint accounts or not. modify includeJointAccountClause to handle
		// multiple customers at once.
		List<SimplePersistentObject> dbRows = BankFusionThreadLocal
				.getPersistanceFactory().executeGenericQuery(generatedQuery,
						params, pdate, false);
		if (dbRows == null || dbRows.size() <= 0) {
			SubCode code = new SubCode();
			code.setCode(CommonsEventCodes.E_CB_CMN_NO_DETAILS_FOUND_CB05 + " ");
			resObj.getRsHeader().getStatus().setOverallStatus("E");
			resObj.getRsHeader().getStatus().setCodes(0, code);
			resObj.getPagingInfo().getPagingRequest().setTotalPages(0);

			MessageStatus mssg = new MessageStatus();
			mssg.addCodes(code);
			setF_OUT_messageStatus(mssg);
			resObj.getSearchAccountDetails().removeAllListAccountDetails();
			logger.warn("CorrelationId: "+BankFusionThreadLocal.getCorrelationID());
			return;
		}
		int index = 0;
		ArrayList param = new ArrayList();

		ListAccountDetails[] vListAccountDetailsArray = new ListAccountDetails[dbRows
				.size()];
		for (final Iterator<SimplePersistentObject> iterator = dbRows
				.iterator(); iterator.hasNext();) {
			final SimplePersistentObject oPersistentObject = iterator.next();
			final Map dataMap = oPersistentObject.getDataMap();
			final String accountname = (String) dataMap
					.get(IBOAccount.ACCOUNTNAME);
			final String accId = (String) dataMap.get(ACCT_ID);
			final String cncyCode = (String) dataMap
					.get(IBOAccount.ISOCURRENCYCODE);
			final Boolean dormantStatus = (Boolean) dataMap
					.get(IBOAccount.DORMANTSTATUS);
			final Boolean closed = (Boolean) dataMap.get(IBOAccount.CLOSED);
			final Boolean stopped = (Boolean) dataMap.get(IBOAccount.STOPPED);
			final BigDecimal clearBalance = (BigDecimal) dataMap
					.get(IBOAccount.CLEAREDBALANCE);
			final BigDecimal bookBalance = (BigDecimal) dataMap
					.get(IBOAccount.BOOKEDBALANCE);
			final BigDecimal blockBalance = (BigDecimal) dataMap
					.get(IBOAccount.BLOCKEDBALANCE);
			final Date openDate = (Date) dataMap.get(IBOAccount.OPENDATE);
			final String branchShortCode = (String) dataMap
					.get(IBOAccount.BRANCHSORTCODE);
			final String taxCRIndicator = (String) dataMap
					.get(IBOAccount.TAXINDICATORCR);
			final String taxDRIndicator = (String) dataMap
					.get(IBOAccount.TAXINDICATORDR);
			final String productId = (String) dataMap.get(IBOAccount.PRODUCTID);
			final Boolean jointAcc = (Boolean) dataMap
					.get(IBOAccount.JOINTACCOUNT);
			final Integer accIndicator = (Integer) dataMap
					.get(IBOAccount.ACCRIGHTSINDICATOR);

			// comment out the below statements
			CustomerShortDetails customerShortDetails = new CustomerShortDetails();

			final String custCode = (String) dataMap
					.get(IBOCustomer.CUSTOMERCODE);
			final String shortName = (String) dataMap
					.get(IBOCustomer.SHORTNAME);

			customerShortDetails.setCustomerId(custCode);
			customerShortDetails.setCustomerName(shortName);

			// comment ended here
			final BigDecimal overdraftLimit = (BigDecimal) dataMap
					.get(IBOAccount.DEBITLIMIT);
			BFCurrencyAmount overdraftLimitFormatted = new BFCurrencyAmount();
			final BigDecimal overdraftTempLimit = (BigDecimal) dataMap
					.get(IBOAccount.TEMPACCOUNTLIMIT);
			final BigDecimal creditLimit = (BigDecimal) dataMap
					.get(IBOAccount.CREDITLIMIT);
			final String accountDescription = (String) dataMap
					.get(IBOAccount.ACCOUNTDESCRIPTION);
			final String partyType = (String) dataMap
					.get(IBOCustomer.CUSTOMERTYPE);
			final String subProductId = (String) dataMap
					.get(IBOAccount.PRODUCTCONTEXTCODE);
			final String productDescription = (String) dataMap
					.get(IBOProductInheritance.PRODUCTDESCRIPTION);
			final Timestamp overdraftExpiryDate = (Timestamp) dataMap
					.get(IBOAccountLimitFeature.LIMITEXPIRYDATE);
			final Timestamp overdraftTempExpiryDate = (Timestamp) dataMap
					.get(IBOAccountLimitFeature.TEMPLIMEXPIRYDATE);
			String extAccId = null;
			String IBANAccountId = null;
			ListAccountDetails vListAccountDetails = new ListAccountDetails();
			AccountInfo accountInfo = new AccountInfo();
			AcctBalances acctBalances = new AcctBalances();
			AcctBalance bookedBalance = new AcctBalance();
			AcctBalance blockedBalance = new AcctBalance();
			BFCurrencyAmount blockedBalanceFormatted = new BFCurrencyAmount();
			AcctBalance creditlimit = new AcctBalance();
			BFCurrencyAmount creditLimitFormatted = new BFCurrencyAmount();
			int scaleVal = 0;
			List<IBOCB_CUR_CurrencyView> resultSet = null;
			//
//			          String query = " WHERE " + IBOCB_CUR_CurrencyView.ISOCURRENCYCODEPK + " = ? ";
			//
//			          ArrayList queryParams = new ArrayList();
//			          queryParams.add(cncyCode);
			//
//			          resultSet = getFactory().findByQuery(IBOCB_CUR_CurrencyView.BONAME, query, queryParams, null, false);
			//
//			          for (int j = 0; j < resultSet.size(); j++) {
//			              scaleVal = resultSet.get(j).getF_CURRENCYSCALE();
//			          }
			          Map currencyPoperties = bizInfo.getCurrencyProperties(cncyCode, BankFusionThreadLocal.getBankFusionEnvironment());
			          scaleVal = ((Integer) currencyPoperties.get(MetaDataEnum.PROP_CURRENCYSCALE)).intValue();
			           bookedBalance.setAccountBalance(setBalanceScale(bookBalance,
			                    scaleVal));
			           bookedBalance.setBalanceCurrency(cncyCode);
			acctBalances.setBookedBalance(bookedBalance);

			BFCurrencyAmount bookedBalanceFormatted = new BFCurrencyAmount();
			bookedBalanceFormatted.setCurrencyAmount(setBalanceScale(
					bookBalance, scaleVal));
			bookedBalanceFormatted.setCurrencyCode(cncyCode);
			CbsBfCurrencyAmount cbsBookedBalanceFormatted = new CbsBfCurrencyAmount();
			cbsBookedBalanceFormatted
					.setBfCurrencyAmount(bookedBalanceFormatted);
			acctBalances.setBookedBalanceFormatted(cbsBookedBalanceFormatted);
			if (blockBalance != null) {
				blockedBalance.setAccountBalance(setBalanceScale(blockBalance,
						scaleVal));
				blockedBalance.setBalanceCurrency(cncyCode);
				acctBalances.setHoldAmount(blockedBalance);
				blockedBalanceFormatted.setCurrencyAmount(setBalanceScale(
						blockBalance, scaleVal));
				blockedBalanceFormatted.setCurrencyCode(cncyCode);
				CbsBfCurrencyAmount cbsHoldAmountFormatted = new CbsBfCurrencyAmount();
				cbsHoldAmountFormatted
						.setBfCurrencyAmount(blockedBalanceFormatted);
				acctBalances.setHoldAmountFormatted(cbsHoldAmountFormatted);
			}
			creditlimit
					.setAccountBalance(setBalanceScale(creditLimit, scaleVal));
			creditlimit.setBalanceCurrency(cncyCode);
			acctBalances.setShadowCredits(creditlimit);

			creditLimitFormatted.setCurrencyAmount(setBalanceScale(creditLimit,
					scaleVal));
			creditLimitFormatted.setCurrencyCode(cncyCode);
			CbsBfCurrencyAmount cbsCreditLimitFormatted = new CbsBfCurrencyAmount();
			cbsCreditLimitFormatted.setBfCurrencyAmount(creditLimitFormatted);
			acctBalances.setShadowCreditsFormatted(cbsCreditLimitFormatted);

			AcctBalance clearBalanceObj = new AcctBalance();
			clearBalanceObj.setAccountBalance(setBalanceScale(clearBalance,
					scaleVal));
			clearBalanceObj.setBalanceCurrency(cncyCode);
			acctBalances.setClearedBalance(clearBalanceObj);

			BFCurrencyAmount clearBalanceFormattedObj = new BFCurrencyAmount();
			clearBalanceFormattedObj.setCurrencyAmount(setBalanceScale(
					clearBalance, scaleVal));
			clearBalanceFormattedObj.setCurrencyCode(cncyCode);
			CbsBfCurrencyAmount cbsClearBalanceFormattedObj = new CbsBfCurrencyAmount();
			cbsClearBalanceFormattedObj
					.setBfCurrencyAmount(clearBalanceFormattedObj);
			acctBalances
					.setClearedBalanceFormatted(cbsClearBalanceFormattedObj);

			String skipAvailableBalance = getF_IN_SkipAvailableBalance();
			if (skipAvailableBalance == null
					|| !skipAvailableBalance.equals(CommonConstants.Y)) {
				HashMap inputParams = new HashMap();
				inputParams.put(AVAILABLEBALANCE_IN_PARAM, accId);
				HashMap outputParams = AvailableBalanceFunction.run(accId);
				;

				BigDecimal avaiableBal = (BigDecimal) (outputParams
						.get(AVAILABLEBALANCE_OUT_PARAM));

				AcctBalance availableBalanceObj = new AcctBalance();
				availableBalanceObj.setAccountBalance(setBalanceScale(avaiableBal, scaleVal));
				availableBalanceObj.setBalanceCurrency(cncyCode);
				BFCurrencyAmount availableBalanceFormattedObj = new BFCurrencyAmount();
				availableBalanceFormattedObj.setCurrencyAmount(setBalanceScale(avaiableBal, scaleVal));
				availableBalanceFormattedObj.setCurrencyCode(cncyCode);
				// BigDecimal availableBal = ConvertToCurrency.run(avaiableBal,
				// cncyCode);
				// availableBalanceObj.setAccountBalance(availableBal);

				acctBalances.setAvailableBalance(availableBalanceObj);
				CbsBfCurrencyAmount cbsAvailableBalanceFormattedObj = new CbsBfCurrencyAmount();
				cbsAvailableBalanceFormattedObj
						.setBfCurrencyAmount(availableBalanceFormattedObj);
				acctBalances
						.setAvailableBalanceFormatted(cbsAvailableBalanceFormattedObj);
			}
			accountInfo.setAcctBalances(acctBalances);

			AccountBasicDetails accountBasicDetails = new AccountBasicDetails();
			AccountKeys kys = new AccountKeys();
			IBANAccountId=null;
			param.clear();
			param.add(accId);
			param.add(BT_ALTERNATE);
			param.add(IBANACCOUNT);
            List<SimplePersistentObject> extAccIdList = BankFusionThreadLocal.getPersistanceFactory().executeGenericQuery(
                   EXTERNAL_ACCID, param, null, false);

           for (SimplePersistentObject extAccountId : extAccIdList) {
               Map<String, Object> map = extAccountId.getDataMap();
               if ((map.get(IBOPseudonymAccountMap.SORTCONTEXTVALUE).toString()).equals(BT_ALTERNATE)) {
                   extAccId = map.get(IBOPseudonymAccountMap.PSEUDONAME).toString();
               }
               else {
                   IBANAccountId = map.get(IBOPseudonymAccountMap.PSEUDONAME).toString();
               }
           }
//           param.clear();
//           param.add(accId);
//           param.add(IBANACCOUNT);
//           List<SimplePersistentObject> IBANAccIdList = BankFusionThreadLocal.getPersistanceFactory().executeGenericQuery(
//                   IBAN, param, null, false);
//
//           for (SimplePersistentObject IBANAccId : IBANAccIdList) {
//               Map<String, Object> map = IBANAccId.getDataMap();
//               IBANAccountId = map.get(IBOPseudonymAccountMap.PSEUDONAME).toString();
//           }

			kys.setExternalAccountId(extAccId);
			InputAccount inAcc = new InputAccount();
			inAcc.setInputAccountId(accId);
			kys.setInputAccount(inAcc);
			Pseudonym pseudonym = new Pseudonym();
			pseudonym.setIsoCurrencyCode(cncyCode);
			kys.setPseudonym(pseudonym);
			kys.setStandardAccountId(accId);
			kys.setIBAN(IBANAccountId);
			accountBasicDetails.setAccountKeys(kys);
			accountBasicDetails.setAccountName(accountname);
			accountBasicDetails.setCurrency(cncyCode);
			accountBasicDetails.setEquivalentAccountName(accountDescription);
			// -- code moved little abve
			customerShortDetails.setPartyType(partyType);
			accountBasicDetails.setCustomerShortDetails(customerShortDetails);
			accountBasicDetails
					.setDateOpened(new Timestamp(openDate.getTime()));
			accountBasicDetails.setDebitLimit(overdraftLimit);
			accountBasicDetails.setLimitExpiryDate(new Timestamp(
					overdraftExpiryDate.getTime()));
			accountBasicDetails.setTempLimitExpiryDate(new Timestamp(
					overdraftTempExpiryDate.getTime()));

			// closing date is set only if the status is other than Normal
			if (!STATUS_NORMAL.equals(status)) {
				final Date closingDate = (Date) dataMap
						.get(IBOAccount.CLOSUREDATE);
				accountBasicDetails.setClosingDate(new Timestamp(closingDate
						.getTime()));
				if (closingDate.compareTo(openDate) < 0) {
					Timestamp blankDate = null;
					accountBasicDetails.setClosingDate(blankDate);
				}
			}

			accountBasicDetails.setSubProductDescription(productDescription);
			accountBasicDetails.setTempOverdraftLimit(overdraftTempLimit);
			accountBasicDetails.setHostBranchCode(branchShortCode);
			accountBasicDetails.setTaxCrIndicator(taxCRIndicator);
			accountBasicDetails.setTaxDrIndicator(taxDRIndicator);
			accountBasicDetails.setProductId(productId);
			accountBasicDetails.setSubProductId(subProductId);
			accountBasicDetails.setOverdraftLimit(overdraftLimit.add(
					overdraftTempLimit).setScale(scaleVal));
			overdraftLimitFormatted.setCurrencyAmount(overdraftLimit.add(
					overdraftTempLimit).setScale(scaleVal));
			overdraftLimitFormatted.setCurrencyCode(cncyCode);
			CbsBfCurrencyAmount cbsOverdraftLimitFormatted = new CbsBfCurrencyAmount();
			cbsOverdraftLimitFormatted
					.setBfCurrencyAmount(overdraftLimitFormatted);
			accountBasicDetails
					.setOverdraftLimitFormatted(cbsOverdraftLimitFormatted);
			// accountBasicDetails.setOverdraftLimit(ConvertToCurrency.run(overdraftLimit,
			// cncyCode));
			if (overdraftTempExpiryDate.after(SystemInformationManager
					.getInstance().getBFBusinessDateTime())) {
				if (overdraftTempExpiryDate.after(overdraftExpiryDate)) {
					accountBasicDetails
							.setOverdraftExpiryDate(overdraftExpiryDate);
				} else {
					accountBasicDetails
							.setOverdraftExpiryDate(overdraftTempExpiryDate);
				}
			} else {
				accountBasicDetails.setOverdraftExpiryDate(overdraftExpiryDate);
			}

			accountInfo.setAcctBasicDetails(accountBasicDetails);
			final Date dormancyDate = (Date) dataMap
					.get(IBOAccount.DATEOFDORMANCY);
			AccountCharacteristics accountCharacteristics = new AccountCharacteristics();
			accountCharacteristics.setIsClosed(closed);
			accountCharacteristics.setIsDormant(dormantStatus);
			accountCharacteristics.setIsStoped(stopped || accIndicator == 2);
			accountCharacteristics.setIsJoint(jointAcc);
			accountCharacteristics.setIsMinor(0 != accIndicator);
			if (dormancyDate != null) {
				if (dormancyDate.compareTo(openDate) < 0) {
					Timestamp blankDate = null;
					accountCharacteristics.setDormancyDate(blankDate);
				} else {
					accountCharacteristics.setDormancyDate(new Timestamp(
							dormancyDate.getTime()));
				}
			}
			 ConcurrentHashMap<String, Boolean> prodFtrMatrix = retrieveFeatureAvailability(productId);
	            
		       //             CheckProductFeatures checkFeatureAttached = new CheckProductFeatures(env);
		      //      checkFeatureAttached.setF_IN_PRODUCTID(productId);
		        //    checkFeatureAttached.process(env);
		            accountCharacteristics.setIsStatementAvailable(prodFtrMatrix.get(BANKSTATEMENTFTR));

		            if (!prodFtrMatrix.get(CHQDTLSFTR)) {
		                accountCharacteristics.setIsChequeBookAvailable(prodFtrMatrix.get(CHQDTLSFTR));
		           }
		           else{
		                accountCharacteristics.setIsStatementAvailable(true);
		               ArrayList chqBookAvlparams = new ArrayList();
				chqBookAvlparams.add(productId);

				List<SimplePersistentObject> result = getFactory()
						.executeGenericQuery(CHEQUE_BOOK_AVAILABLE,
								chqBookAvlparams, null, true);
				long chqBookCount = 0;
				if (null != result && !result.isEmpty()) {
					SimplePersistentObject persistentObject = (SimplePersistentObject) result
							.get(0);
					chqBookCount = (Long) persistentObject.getDataMap().get(
							"CHQ_BOOK_COUNT");
					if (chqBookCount > 0) {
						accountCharacteristics
								.setIsChequeBookAvailable(Boolean.TRUE);
					} else {
						accountCharacteristics
								.setIsChequeBookAvailable(Boolean.FALSE);
					}
				} else {
					accountCharacteristics
							.setIsChequeBookAvailable(Boolean.FALSE);
				}
			}

			accountInfo.setAcctCharacteristics(accountCharacteristics);
			vListAccountDetails.setAccountInfo(accountInfo);
			vListAccountDetails.setSelect(index == 0);

			vListAccountDetailsArray[index] = vListAccountDetails;
			index = index + 1;
		}
		ListAccounts acc = new ListAccounts();
		AccountListRestrictionDtls accountList = new AccountListRestrictionDtls();
		accountList.setAccountListRestriction(vListAccountDetailsArray);

		// Calling UserExit
		accountList.setBranchSortCode(env.getUserBranch());

		accountList.setUserId(env.getUserID());
		HashMap retryQueueParams = new HashMap();
		retryQueueParams.put("beanId", "accountRestrictionList");
		retryQueueParams.put("inputObject", accountList);

		HashMap result = MFExecuter.executeMF("CB_CMN_UserExitInvoker_SRV",
				env, retryQueueParams);

		AccountListRestrictionDtls txnList = (AccountListRestrictionDtls) result
				.get("outputObject");

		acc.setListAccountDetails(txnList.getAccountListRestriction());

		resObj.setSearchAccountDetails(acc);

		if (acc.getListAccountDetailsCount() > 0) {
			PagingRequest pagingRequest = new PagingRequest();
			pagingRequest.setNumberOfRows(reqObj.getPagedQuery()
					.getPagingRequest().getNumberOfRows());
			pagingRequest.setRequestedPage(reqObj.getPagedQuery()
					.getPagingRequest().getRequestedPage());
			pagingRequest.setTotalPages(pdate.getTotalPages());
			PagedQuery pagedQuery = new PagedQuery();
			pagedQuery.setPagingRequest(pagingRequest);
			resObj.setPagingInfo(pagedQuery);
			resObj.getRsHeader().getStatus().setOverallStatus("S");
		} else {
		    logger.warn("CorrelationId: "+BankFusionThreadLocal.getCorrelationID());
		}
	}

	@SuppressWarnings("unchecked")
	private String prepareQuery(SearchAccountRq reqObj, String status,
			String custType, ArrayList params, boolean include, Set parties) {
		StringBuffer fromClause = new StringBuffer();
		StringBuffer fromClauseNormal = new StringBuffer();
		StringBuffer whereClause = new StringBuffer();
		String channelId = "";
		if (BankFusionThreadLocal.getSourceId() != null)
			channelId = BankFusionThreadLocal.getSourceId();

		if (include) {
			fromClause = new StringBuffer(" FROM " + IBOAccount.BONAME + " A, "
					+ IBOCustomer.BONAME + " C, "
					+ IBOAccountLimitFeature.BONAME + " AL, "
					+ IBOProductInheritance.BONAME + " P, "
					+ IBOUBVW_ACCOUNTCUSTOMERS.BONAME + " AC ");

			fromClauseNormal = new StringBuffer(" FROM "
					+ IBOUB_ACC_NormalAccounts.BONAME + " A, "
					+ IBOCustomer.BONAME + " C, "
					+ IBOAccountLimitFeature.BONAME + " AL, "
					+ IBOProductInheritance.BONAME + " P, "
					+ IBOUBVW_ACCOUNTCUSTOMERS.BONAME + " AC ");

			// For NORMAL status take normal account view instead of account
			// table.
			if (STATUS_NORMAL.equals(status)) {
				whereClause.append(" WHERE AC."
						+ IBOUBVW_ACCOUNTCUSTOMERS.CUSTOMERCODE + " = C."
						+ IBOCustomer.CUSTOMERCODE); // change
														// this
														// part
														// to
														// an
														// IN
														// clause.
				whereClause.append("  AND AC."
						+ IBOUBVW_ACCOUNTCUSTOMERS.ACCOUNTID + " = A."
						+ IBOUB_ACC_NormalAccounts.ACCOUNTID);
				whereClause.append(" AND A."
						+ IBOUB_ACC_NormalAccounts.PRODUCTCONTEXTCODE + " = P."
						+ IBOProductInheritance.PRODUCTCONTEXTCODE);
				whereClause.append(" AND A."
						+ IBOUB_ACC_NormalAccounts.ACCOUNTID + " = AL."
						+ IBOAccountLimitFeature.ACCOUNTID);
			} else {
				whereClause.append(" WHERE AC."
						+ IBOUBVW_ACCOUNTCUSTOMERS.CUSTOMERCODE + " = C."
						+ IBOCustomer.CUSTOMERCODE); // change
														// this
														// part
														// to
														// an
														// IN
														// clause
														// (why
														// line
														// be
														// a
														// problem)
				whereClause.append(" AND AC."
						+ IBOUBVW_ACCOUNTCUSTOMERS.ACCOUNTID + " = A."
						+ IBOAccount.ACCOUNTID);
				whereClause.append(" AND A." + IBOAccount.PRODUCTCONTEXTCODE
						+ " = P." + IBOProductInheritance.PRODUCTCONTEXTCODE);
				whereClause.append(" AND A." + IBOAccount.ACCOUNTID + " = AL."
						+ IBOAccountLimitFeature.ACCOUNTID);

			}

		} else {
			fromClause = new StringBuffer(" FROM " + IBOAccount.BONAME + " A, "
					+ IBOCustomer.BONAME + " C, "
					+ IBOAccountLimitFeature.BONAME + " AL, "
					+ IBOProductInheritance.BONAME + " P ");

			fromClauseNormal = new StringBuffer(" FROM "
					+ IBOUB_ACC_NormalAccounts.BONAME + " A, "
					+ IBOCustomer.BONAME + " C, "
					+ IBOAccountLimitFeature.BONAME + " AL, "
					+ IBOProductInheritance.BONAME + " P ");

			// For NORMAL status take normal account view instead of account
			// table.
			if (STATUS_NORMAL.equals(status)) {
				whereClause.append(" WHERE A."
						+ IBOUB_ACC_NormalAccounts.UBCUSTOMERCODE + " = C."
						+ IBOCustomer.CUSTOMERCODE); // chnage
														// this
														// to
														// an
														// IN
														// clause.
														// (why
														// line
														// be
														// a
														// problem)
				whereClause.append(" AND A."
						+ IBOUB_ACC_NormalAccounts.PRODUCTCONTEXTCODE + " = P."
						+ IBOProductInheritance.PRODUCTCONTEXTCODE);
				whereClause.append(" AND A."
						+ IBOUB_ACC_NormalAccounts.ACCOUNTID + " = AL."
						+ IBOAccountLimitFeature.ACCOUNTID);
			} else {
				whereClause.append(" WHERE A." + IBOAccount.CUSTOMERCODE
						+ " = C." + IBOCustomer.CUSTOMERCODE); // change
																// this
																// to
																// an
																// IN
																// clause.
																// (why
																// line
																// be
																// a
																// problem)
				whereClause.append(" AND A." + IBOAccount.PRODUCTCONTEXTCODE
						+ " = P." + IBOProductInheritance.PRODUCTCONTEXTCODE);
				whereClause.append(" AND A." + IBOAccount.ACCOUNTID + " = AL."
						+ IBOAccountLimitFeature.ACCOUNTID);
			}

		}

		if (isValidString(status)) {

			if (STATUS_NORMAL.equals(status)) {
				whereClause.append(" AND A."
						+ IBOUB_ACC_NormalAccounts.DORMANTSTATUS + " = ? ");
				params.add(STRING_N);
				whereClause.append(" AND A." + IBOUB_ACC_NormalAccounts.CLOSED
						+ " = ? ");
				params.add(STRING_N);
				whereClause
						.append(" AND A."
								+ IBOUB_ACC_NormalAccounts.ACCRIGHTSINDICATOR
								+ " = ? ");
				params.add(Integer.valueOf(0));
				whereClause.append(" AND A."
						+ IBOUB_ACC_NormalAccounts.UBACCOUNTSTATUS + " <> ? ");
				params.add(ARREAR_ID);
				whereClause.append(" AND A."
						+ IBOUB_ACC_NormalAccounts.UBACCOUNTSTATUS + " <> ? ");
				params.add(DELEQUENT_ID);
			} else if (STATUS_CLOSED.equals(status)) {
				whereClause.append(" AND A." + IBOAccount.CLOSED + " = ? ");
				params.add(Boolean.TRUE);
			} else if (STATUS_INACTIVE.equals(status)) {
				whereClause.append(" AND A." + IBOAccount.UBACCOUNTSTATUS
						+ " = ? ");
				params.add(reqObj.getAccountSearch().getAccountStatus());
			} else if (STATUS_ACTIVE.equals(status)) {
				whereClause.append(" AND A." + IBOAccount.CLOSED + " = ? ");
				params.add(Boolean.FALSE);
			} else if (STATUS_DORMANT.equals(status)) {
				whereClause.append(" AND A." + IBOAccount.DORMANTSTATUS
						+ " = ? ");
				params.add(Boolean.TRUE);
			} else if (STATUS_RESTRICTED.equals(status)) {
				whereClause.append(" AND A." + IBOAccount.ACCRIGHTSINDICATOR
						+ " <> ? ");
				params.add(Integer.valueOf(0));
			} else if (STATUS_DELINQUENT.equals(status)) {
				fromClause.append(" , " + IBOUB_INT_MovementArrears.BONAME
						+ " MA ");
				whereClause.append(" AND A." + IBOAccount.ACCOUNTID + " = MA. "
						+ IBOUB_INT_MovementArrears.UBAccountID);
				whereClause.append(" AND MA."
						+ IBOUB_INT_MovementArrears.UBArrearsStatus + " = ? ");
				params.add(DELEQUENT_ID);
			} else if (STATUS_ARREARS.equals(status)) {
				fromClause.append(" , " + IBOUB_INT_MovementArrears.BONAME
						+ " MA ");
				whereClause.append(" AND A." + IBOAccount.ACCOUNTID + " = MA. "
						+ IBOUB_INT_MovementArrears.UBAccountID);
				whereClause.append(" AND MA."
						+ IBOUB_INT_MovementArrears.UBArrearsStatus + " <> ? ");
				params.add(DELEQUENT_ID);
			}
		}

		whereClause.append(" AND C." + IBOCustomer.CUSTOMERTYPE + " <> ? ");
		params.add(custType);

		if (reqObj.getAccountSearch() instanceof AccountSearchForCustomers) {
			whereClause.append(" AND C." + IBOCustomer.CUSTOMERCODE // amend
																	// where
																	// clause to
																	// use IN
																	// query.
																	// --done
					+ " in ( ");

			Object[] partyArray = parties.toArray();
			for (int i = 0, n = partyArray.length; i < n; i++) {
				if (i == (n - 1)) {
					whereClause.append("?)");
				} else {
					whereClause.append("?,");
				}
				params.add(partyArray[i]);
			}
		} else if (isValidString(reqObj.getAccountSearch().getCustomerId())) { // this
																				// checks
																				// wherether
																				// customer
																				// filter
																				// is
																				// set
																				// or
																				// not,
																				// so
																				// write
																				// a
																				// different
																				// method
																				// to
																				// verify
																				// this.
			if (channelId.equals(CHANNEL_ID)) {
				whereClause.append(" AND C." + IBOCustomer.CUSTOMERCODE
						+ " LIKE ? ");
				params.add(reqObj.getAccountSearch().getCustomerId());
			} else {
				whereClause.append(" AND C." + IBOCustomer.CUSTOMERCODE // amend
																		// where
																		// clause
																		// to
																		// use
																		// IN
																		// query.
						+ " = ? ");
				params.add(reqObj.getAccountSearch().getCustomerId()); // loop
																		// to
																		// add
																		// all
																		// customers
																		// instead
																		// of
																		// one.
			}
		}

		if (isValidString(reqObj.getAccountSearch().getAccountId())) {
			if (channelId.equals(CHANNEL_ID)) {
				whereClause.append(" AND A." + IBOAccount.ACCOUNTID
						+ " LIKE ? ");
				params.add(reqObj.getAccountSearch().getAccountId());
			} else {
				whereClause.append(" AND A." + IBOAccount.ACCOUNTID + " = ? ");
				params.add(reqObj.getAccountSearch().getAccountId());
			}
		}

		if (isValidString(reqObj.getAccountSearch().getAccountName())) {
			if (channelId.equals(CHANNEL_ID)) {
				whereClause.append(" AND A." + IBOAccount.ACCOUNTNAME
						+ " LIKE ? ");
				params.add(reqObj.getAccountSearch().getAccountName());
			} else {
				whereClause
						.append(" AND A." + IBOAccount.ACCOUNTNAME + " = ? ");
				params.add(reqObj.getAccountSearch().getAccountName());
			}

		}

		if (isValidString(reqObj.getAccountSearch().getBranch())) {
			whereClause.append(" AND A." + IBOAccount.BRANCHSORTCODE + " = ? ");
			params.add(reqObj.getAccountSearch().getBranch());
		}

		if (isValidString(reqObj.getAccountSearch().getCurrency())) {
			whereClause
					.append(" AND A." + IBOAccount.ISOCURRENCYCODE + " = ? ");
			params.add(reqObj.getAccountSearch().getCurrency());
		}
		// if (isValidString((String)
		// reqObj.getAccountSearch().getHostExtension())) {
		// whereClause.append(" AND A." + IBOAccount.PRODUCTID + " = ? ");
		// params.add(reqObj.getAccountSearch().getHostExtension());
		// }

		if (isValidString(reqObj.getAccountSearch().getProduct())) {
			whereClause.append(" AND A." + IBOAccount.PRODUCTID + " like ? ");
			params.add(reqObj.getAccountSearch().getProduct() + '%');
		}

		if (isValidString(reqObj.getAccountSearch().getSubProductId())) {
			whereClause.append(" AND P."
					+ IBOProductInheritance.PRODUCTCONTEXTCODE + " like ? ");
			params.add(reqObj.getAccountSearch().getSubProductId() + '%');
		}

		Date fromDate = reqObj.getAccountSearch().getDateAccountOpenedFrom();
		if (fromDate != null) {
			whereClause.append(" AND A." + IBOAccount.OPENDATE + " >= ? ");
			params.add(fromDate);
		}
		Date toDate = reqObj.getAccountSearch().getDateAccountOpenedTo();
		if (toDate != null) {
			int compareDate = toDate.compareTo(new Date(1970 - 01 - 01));
			if (compareDate == -1) {
				toDate = SystemInformationManager.getInstance()
						.getBFBusinessDate();
			}
			whereClause.append(" AND A." + IBOAccount.OPENDATE + " <= ? ");
			params.add(toDate);
		}

		// For NORMAL status take normalaccount view instead of account table.
		String reqFromClause = fromClause.toString();
		String selectQuery = QUERY;
		if (STATUS_NORMAL.equals(status)) {
			reqFromClause = fromClauseNormal.toString();

		} else {
			// if the status is other than normal then only closure date is
			// added to the select
			// query
			selectQuery = QUERY + " ,A." + IBOAccount.BLOCKEDBALANCE + " AS "
					+ IBOAccount.BLOCKEDBALANCE + " ,A."
					+ IBOAccount.DATEOFDORMANCY + " AS "
					+ IBOAccount.DATEOFDORMANCY + " ,A."
					+ IBOAccount.TAXINDICATORCR + " AS "
					+ IBOAccount.TAXINDICATORCR + " ,A."
					+ IBOAccount.TAXINDICATORDR + " AS "
					+ IBOAccount.TAXINDICATORDR + " ,A."
					+ IBOAccount.CLOSUREDATE + " AS " + IBOAccount.CLOSUREDATE;
		}

		String generatedQuery = selectQuery + reqFromClause
				+ whereClause.toString();
		countQuery=countQuery+reqFromClause
                + whereClause.toString();
		return generatedQuery;
	}

	/**
	 * Method Description: This Will check for valid string
	 *
	 * @param str
	 * @return Boolean
	 */
	private boolean isValidString(String str) {
		if (null == str || str.trim().isEmpty() || str.equals(WILDCARD)) {
			return Boolean.FALSE;
		} else {
			return Boolean.TRUE;
		}
	}

	/**
	 * Method Description: This method is added for the issue fix
	 * reference-BFUB-33102 The purpose of this method is to decide if an
	 * additional where clause is needed in the process()to find the accounts
	 * belonging to a subsidiary customer based on the customer code entered.
	 *
	 * @param customerCode
	 * @return Boolean
	 */
	private boolean includeJointAccountClause(String customerCode) {
		if (isValidString(customerCode)) {
			ArrayList<String> values = new ArrayList<String>();
			values.add(customerCode);
			List<IBOUB_CNF_JOINTACCOUNT> jointAcctList = (List<IBOUB_CNF_JOINTACCOUNT>) BankFusionThreadLocal
					.getPersistanceFactory().findByQuery(
							IBOUB_CNF_JOINTACCOUNT.BONAME,
							FIND_JOINTACCOUNT_WHERECLAUSE, values, null, false);
			if (jointAcctList != null && !jointAcctList.isEmpty()
					&& jointAcctList.get(0) != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Method Description: The purpose of this method is to round of balance
	 * amounts to proper scale.
	 *
	 * @param BigDecimal
	 *            amountToBeScaled,
	 * @param int scale
	 * @return BigDecimal
	 */
	private BigDecimal setBalanceScale(BigDecimal amountToBeScaled, int scaleVal) {
		String roundingOption;
		if (!ROUNDING_OPTION_AVAILABLE) {
			roundingOption = getModuleConfigValue(BANK_POSTING_MODULE,
					BANK_POSTING_ROUNDING_PARAM);
			if (null != roundingOption
					&& !roundingOption.equals(CommonConstants.EMPTY_STRING)) {
				ROUNDING_OPTION_AVAILABLE = Boolean.TRUE;
				ROUNDING_OPTION = Integer.parseInt(roundingOption);
			} else {
				ROUNDING_OPTION_AVAILABLE = Boolean.TRUE;
				ROUNDING_OPTION = DEFAULT_ROUNDING_OPTION;
			}
			try {
				return amountToBeScaled.setScale(scaleVal, ROUNDING_OPTION);
			} catch (Exception E) {
				 logger.error(ExceptionUtil.getExceptionAsString(E));
				return amountToBeScaled.setScale(scaleVal,
						BigDecimal.ROUND_HALF_EVEN);
                 
			}
		} else {
			try {
				return amountToBeScaled.setScale(scaleVal, ROUNDING_OPTION);
			} catch (Exception E) {
				 logger.error(ExceptionUtil.getExceptionAsString(E));
				return amountToBeScaled.setScale(scaleVal,
						BigDecimal.ROUND_HALF_EVEN);
			}
		}
	}

	/**
	 * Method Description: The purpose of this method is to fetch Module
	 * Configuration value for a given moduleName & paramName.
	 *
	 * @param String
	 *            moduleName,
	 * @param String
	 *            paramName
	 * @return String
	 */

	@SuppressWarnings("unchecked")
	private String getModuleConfigValue(String moduleName, String paramName) {
		HashMap<String, ReadModuleConfigurationRq> moduleParams = new HashMap<String, ReadModuleConfigurationRq>();
		ModuleKeyRq module = new ModuleKeyRq();
		ReadModuleConfigurationRq read = new ReadModuleConfigurationRq();
		module.setModuleId(moduleName);
		module.setKey(paramName);
		read.setModuleKeyRq(module);
		moduleParams.put("ReadModuleConfigurationRq", read);
		HashMap valueFromModuleConfiguration = MFExecuter.executeMF(
				READ_MODULE_CONFIGURATION_MFID,
				BankFusionThreadLocal.getBankFusionEnvironment(), moduleParams);
		if (valueFromModuleConfiguration != null) {
			ReadModuleConfigurationRs rs = (ReadModuleConfigurationRs) valueFromModuleConfiguration
					.get("ReadModuleConfigurationRs");
			return rs.getModuleConfigDetails().getValue();
		} else {
			return null;
		}
	}
	 private ConcurrentHashMap<String, Boolean> retrieveFeatureAvailability(String productId) {
	        if(cache.cacheGet(PROD_FTR_CACHE_KEY, PROD_FTR_KEY) !=null){
	             if(((ConcurrentHashMap) cache.cacheGet(PROD_FTR_CACHE_KEY, PROD_FTR_KEY)).containsKey(productId)){
	                 return (ConcurrentHashMap)((ConcurrentHashMap) cache.cacheGet(PROD_FTR_CACHE_KEY, PROD_FTR_KEY)).get(productId);
	             }else{
	                 loadProductFeatureValues(productId);
	             }
	         }else{
	             loadProductFeatureValues(productId);
	         }
	         return (ConcurrentHashMap)((ConcurrentHashMap) cache.cacheGet(PROD_FTR_CACHE_KEY, PROD_FTR_KEY)).get(productId);
	     }

	     private void loadProductFeatureValues(String productId) {
	         ConcurrentHashMap<String, Boolean> prodFtrDetails = new ConcurrentHashMap<String, Boolean>();
	         ArrayList prodFtrParams = new ArrayList();
	         prodFtrParams.add(productId);
	         prodFtrParams.add(BANKSTATEMENTFTR);
	         prodFtrParams.add(CHQDTLSFTR);
	         Boolean isChequeFeatureAvail = false;
	         List<IBOUBTB_PRODUCTFEATURE> prodFtrList = BankFusionThreadLocal.getPersistanceFactory().findByQuery(
	                 IBOUBTB_PRODUCTFEATURE.BONAME,
	                 " WHERE " + IBOUBTB_PRODUCTFEATURE.UBPRODUCTPK + " = ? AND (" + IBOUBTB_PRODUCTFEATURE.UBFEATUREPK + " = ? OR "
	                         + IBOUBTB_PRODUCTFEATURE.UBFEATUREPK + " = ? )", prodFtrParams, null);
	         if (prodFtrList != null && !prodFtrList.isEmpty()) {
	             for (IBOUBTB_PRODUCTFEATURE prodFtr : prodFtrList) {
	                 UBTB_PRODUCTFEATUREID id = (UBTB_PRODUCTFEATUREID) prodFtr.getUBTB_PRODUCTFEATUREID();
	                 if (id.getF_UBFEATURE().equals(BANKSTATEMENTFTR)) {
	                     if (prodFtr.isF_UBISFEATUREAVAILABLE()) {
	                         prodFtrDetails.put(BANKSTATEMENTFTR, true);
	                     }
	                     else {
	                         prodFtrDetails.put(BANKSTATEMENTFTR, false);
	                     }
	                 }
	                 if (id.getF_UBFEATURE().equals(CHQDTLSFTR)) {
	                     if (prodFtr.isF_UBISFEATUREAVAILABLE()) {
	                         prodFtrDetails.put(CHQDTLSFTR, true);
	                     }
	                     else {
	                         prodFtrDetails.put(CHQDTLSFTR, false);
	                     }
	                 }
	             }
	         }
	         else {
	             prodFtrDetails.put(BANKSTATEMENTFTR, true);
	             prodFtrDetails.put(CHQDTLSFTR, false);
	         }
	         if (cache.cacheGet(PROD_FTR_CACHE_KEY, PROD_FTR_KEY) != null) {
	             ConcurrentHashMap<String, ConcurrentHashMap<String, Boolean>> values = (ConcurrentHashMap) cache.cacheGet(
	                     PROD_FTR_CACHE_KEY, PROD_FTR_KEY);
	             values.put(productId, prodFtrDetails);
	              cache.cachePut(PROD_FTR_CACHE_KEY,PROD_FTR_KEY,values);
	         }
	         else {
	             ConcurrentHashMap<String, ConcurrentHashMap<String, Boolean>> values = new ConcurrentHashMap<String, ConcurrentHashMap<String, Boolean>>();
	             values.put(productId, prodFtrDetails);
	              cache.cachePut(PROD_FTR_CACHE_KEY,PROD_FTR_KEY,values);
	         }
	     }
}
