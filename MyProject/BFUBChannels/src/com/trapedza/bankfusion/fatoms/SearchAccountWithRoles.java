package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.finastra.api.account.persistence.AccountFinderMethods;
import com.finastra.fbe.helper.FetchBeneficiaryDetails;
import com.misys.bankfusion.common.MetaDataEnum;
import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.exception.BankFusionException;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.microflow.runtime.impl.MFExecuter;
import com.misys.bankfusion.subsystem.persistence.SimplePersistentObject;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.cbs.common.util.DateUtil;
import com.misys.fbe.common.util.CommonUtil;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOChequeBookType;
import com.trapedza.bankfusion.bo.refimpl.IBOCreditInterestFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.bo.refimpl.IBOFixtureFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOPseudonymAccountMap;
import com.trapedza.bankfusion.bo.refimpl.IBOUBTB_PRODUCTFEATURE;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_CNF_ACCTMANDATE;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_CNF_JOINTACCOUNT;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_CNF_SurplusTxnDtls;
import com.trapedza.bankfusion.bo.refimpl.UBTB_PRODUCTFEATUREID;
import com.trapedza.bankfusion.core.FinderMethods;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.AvailableBalanceFunction;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.servercommon.services.cache.ICacheService;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ACC_SearchAccountWithRoles;

import bf.com.misys.bankfusion.attributes.BFCurrencyAmount;
import bf.com.misys.bankfusion.attributes.PagedQuery;
import bf.com.misys.bankfusion.attributes.PagingRequest;
import bf.com.misys.cbs.msgs.v1r0.SearchAccountRq;
import bf.com.misys.cbs.msgs.v1r0.SearchAccountRs;
import bf.com.misys.cbs.msgs.v1r0.SearchAcctRs;
import bf.com.misys.cbs.services.ReadModuleConfigurationRq;
import bf.com.misys.cbs.services.ReadModuleConfigurationRs;
import bf.com.misys.cbs.types.AccountBasicDetails;
import bf.com.misys.cbs.types.AccountCharacteristics;
import bf.com.misys.cbs.types.AccountInfo;
import bf.com.misys.cbs.types.AccountKeys;
import bf.com.misys.cbs.types.AccountMandateDetails;
import bf.com.misys.cbs.types.AccountSearchForCustomers;
import bf.com.misys.cbs.types.AcctBalance;
import bf.com.misys.cbs.types.AcctBalances;
import bf.com.misys.cbs.types.AcctCharacteristics;
import bf.com.misys.cbs.types.AcctInfo;
import bf.com.misys.cbs.types.CbsBfCurrencyAmount;
import bf.com.misys.cbs.types.CustomerShortDetails;
import bf.com.misys.cbs.types.InputAccount;
import bf.com.misys.cbs.types.ListAccountDetails;
import bf.com.misys.cbs.types.ListAccountDtls;
import bf.com.misys.cbs.types.ListAccounts;
import bf.com.misys.cbs.types.ListAcct;
import bf.com.misys.cbs.types.ListMandateDetails;
import bf.com.misys.cbs.types.ModuleKeyRq;
import bf.com.misys.cbs.types.OtherBankDetails;
import bf.com.misys.cbs.types.PayAwayAccountDetails;
import bf.com.misys.cbs.types.PaymentDetails;
import bf.com.misys.cbs.types.Pseudonym;
import bf.com.misys.cbs.userexit.types.AccountListRestrictionDtls;
import bf.com.misys.paymentmessaging.types.BeneficiaryDetails;

@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
public class SearchAccountWithRoles extends AbstractUB_ACC_SearchAccountWithRoles {

	private static final long serialVersionUID = 1L;
	private static final IBusinessInformation bizInfo = ((IBusinessInformationService) ServiceManagerFactory
			.getInstance().getServiceManager()
			.getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();

	private static final String ACCT_ID = "ACCOUNTID";
	private static final String ACCOUNTDESCRIPTION = "ACCOUNTDESCRIPTION";
	private static final String ACCOUNTNAME = "ACCOUNTNAME";
	private static final String ISOCURRENCYCODE = "ISOCURRENCYCODE";
	private static final String CLEAREDBALANCE = "CLEAREDBALANCE";
	private static final String BOOKEDBALANCE = "BOOKEDBALANCE";
	private static final String BLOCKEDBALANCE = "BLOCKEDBALANCE";
	private static final String PRODUCTCONTEXTCODE = "PRODUCTCONTEXTCODE";
	private static final String OPENDATE = "OPENDATE";
	private static final String PRODUCTID = "PRODUCTID";
	private static final String ACCRIGHTSINDICATOR = "ACCRIGHTSINDICATOR";
	private static final String DEBITLIMIT = "DEBITLIMIT";
	private static final String TEMPACCOUNTLIMIT = "TEMPACCOUNTLIMIT";
	private static final String TEMPLIMEXPIRYDATE = "TEMPLIMEXPIRYDATE";
	private static final String CUSTOMERCODE = "CUSTOMERCODE";
	private static final String SHORTNAME = "SHORTNAME";
	private static final String CUSTOMERTYPE = "CUSTOMERTYPE";
	private static final String LIMITEXPIRYDATE = "LIMITEXPIRYDATE";
	private static final String PRODUCTDESCRIPTION = "PRODUCTDESCRIPTION";
	private static final String TAXINDICATORCR = "TAXINDICATORCR";
	private static final String CREDITLIMIT = "CREDITLIMIT";
	private static final String BRANCHSORTCODE = "BRANCHSORTCODE";
	private static final String STATUS_NORMAL = "NORMAL";
	private static final String CLOSED = "CLOSED";
	private static final String JOINTACCOUNT = "JOINTACCOUNT";
	private static final String CLOSUREDATE = "CLOSUREDATE";
	private static final String DORMANTSTATUS = "DORMANTSTATUS";
	private static final String DATEOFDORMANCY = "DATEOFDORMANCY";
	private static final String STATUS_ACTIVE = "ACTIVE";
	private static final String STATUS_DORMANT = "DORMANT";
	private static final String STATUS_RESTRICTED = "RESTRICTED";
	private static final String STATUS_ARREARS = "ARREARS";
	private static final String STATUS_DELINQUENT = "DELINQUENT";
	private static final String STOPPED = "STOPPED";
	private static final String TAXINDICATORDR = "TAXINDICATORDR";
	private static final String BT_ALTERNATE = "BT_ALTERNATE";
	private static final String IBANACCOUNT = "IBANACCOUNT";
	private static final String DELEQUENT_ID = "D";
	private static final String ARREAR_ID = "A";
	private static final String STRING_N = "N";
	private static final String STRING_Y = "Y";
	private static final String INTERNAL_CUSTOMER = "I";
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
	private static final String ACCT_RIGHTS_IND_MF_NAME = "CB_ACC_AccountRightsIndUbtoCBS_SRV";
	private static final String CHANNEL_IBI = "IBI";
	private static final String CHANNEL_MOB = "MOB";
	private static final String CHANNEL_UXP = "UXP";
	private boolean isIBIOrMobChannel = false;
	private boolean isUXPChannel = false;

	private static final String WILDCARD = "%";
	private static final String FD_STATUS_MATURED = "4";
	private static final String FD_STATUS_EXPIRED = "16";
	private static final String FD_STATUS_REVERSED = "8";
	private static final String FD_STATUS_FULLY_BROKEN = "9";
	private static final String LOAN_STATUS_SETTLED = "2412";
	private static final String LOAN_STATUS_COMPLETED = "2413";
	private static final String LOAN_STATUS_REVERSED = "2429";
	private static final int ACCESS_DENIED_ERROR = 11400018;
	
    private static final String STATUS_INACTIVE = "006";

	private ArrayList params = new ArrayList();


	private static final String CHEQUE_BOOK_AVAILABLE = "SELECT " + IPersistenceObjectsFactory.COUNT_FUNCTION_CODE
			+ "( " + IBOChequeBookType.CHEQUEBOOKID + " ) AS CHQ_BOOK_COUNT FROM " + IBOChequeBookType.BONAME
			+ " WHERE " + IBOChequeBookType.CHEQUETYPEID + " = ? ";

	private static final String QUERY = "SELECT * FROM (SELECT A.ACCOUNTID, A.ACCOUNTDESCRIPTION ,A.ACCOUNTNAME ,A.ISOCURRENCYCODE ,"
			+ "A.DORMANTSTATUS ,A.CLOSED ,A.CLEAREDBALANCE,A.BOOKEDBALANCE , A.PRODUCTCONTEXTCODE ,A.OPENDATE , A.BRANCHSORTCODE ,A.PRODUCTID ,"
			+ "A.ACCRIGHTSINDICATOR ,A.DEBITLIMIT, A.TEMPACCOUNTLIMIT ,A.TEMPLIMEXPIRYDATE , C.CUSTOMERCODE ,C.SHORTNAME, C.CUSTOMERTYPE ,A.LIMITEXPIRYDATE ,"
			+ "P.PRODUCTDESCRIPTION , A.BLOCKEDBALANCE ,A.DATEOFDORMANCY ,A.TAXINDICATORCR , A.TAXINDICATORDR ,A.CREDITLIMIT ,"
			+ "A.STOPPED ,A.JOINTACCOUNT ,A.CLOSUREDATE ,COALESCE(A.UBMODEOFOPERATION ,'SINGLE'),ROW_NUMBER() OVER(ORDER BY A.ACCOUNTID) AS ROWSEQ ";

	private String countQuery = "SELECT COUNT(*) AS CNT ";
	private static final String CLOSEDBRACKET = ")";
	private static final String PAGINATEDQUERY = CLOSEDBRACKET + " WHERE ROWSEQ >= ? AND ROWSEQ  <= ?";

	private static final String EXTERNAL_ACCID = "SELECT " + IBOPseudonymAccountMap.PSEUDONAME + " AS "
			+ IBOPseudonymAccountMap.PSEUDONAME + " , " + IBOPseudonymAccountMap.SORTCONTEXTVALUE + " AS "
			+ IBOPseudonymAccountMap.SORTCONTEXTVALUE + " FROM " + IBOPseudonymAccountMap.BONAME + " WHERE "
			+ IBOPseudonymAccountMap.ACCOUNTID + " = ? AND ( " + IBOPseudonymAccountMap.SORTCONTEXTVALUE + "= ? OR "
			+ IBOPseudonymAccountMap.SORTCONTEXTVALUE + " = ? )";

	private static final String FIND_SURPLUS_AMOUNT_BY_ACCOUNT_WHERECLAUSE = "WHERE "
			+ IBOUB_CNF_SurplusTxnDtls.UBACCOUNTID + " = ?";
	private static final String FIND_MANDATE_DETAILS = " WHERE " + IBOUB_CNF_ACCTMANDATE.UBACCOUNTID + " = ? ";
	private static final String FIND_ACCOUNT_OWNERSHIP = " WHERE " + IBOUB_CNF_JOINTACCOUNT.ACCOUNTID + " = ? AND "
			+ IBOUB_CNF_JOINTACCOUNT.CUSTOMERCODE + " = ? ";

	private Map<String, BeneficiaryDetails> beneficiaryCustDetailsMap = new HashMap<>();
	private String segment;
	private static final Log LOGGER = LogFactory.getLog(SearchAccountWithRoles.class.getName());

	/**
	 * @param env
	 */
	public SearchAccountWithRoles(BankFusionEnvironment env) {
		super(env);
	}

	private IPersistenceObjectsFactory getFactory() {
		return BankFusionThreadLocal.getPersistanceFactory();
	}

	
	@Override
	public void process(BankFusionEnvironment env) {

		Boolean isValidateSuccess = validateUserInput();
		if (!isValidateSuccess) {
			setF_OUT_searchAcctRs(null);
			setF_OUT_searchAccountRs(null);
			CommonUtil.handleUnParameterizedEvent(20020040);

			return;
		}
		SearchAccountRq reqObj = getF_IN_searchAccountRq();
		SearchAccountRs resObj = getF_OUT_searchAccountRs();
		String status = reqObj.getAccountSearch().getAccountStatus();
		ResultSet rs = null;

		String singleAccQuery = "";
		if (reqObj.getAccountSearch() instanceof AccountSearchForCustomers) {

			AccountSearchForCustomers searchCustomers = (AccountSearchForCustomers) reqObj.getAccountSearch();

			HashSet<String> normalParties = new HashSet<>();
			for (String partyId : searchCustomers.getPartyId()) {
				normalParties.add(partyId);

			}
			singleAccQuery = prepareQuery(status, normalParties);

		} else {
			singleAccQuery = prepareQuery(status, null);

		}
		if (CommonUtil.checkIfNotNullOrEmpty(reqObj.getAccountSearch().getCustomerId())) {
			beneficiaryCustDetailsMap = FetchBeneficiaryDetails
					.listBeneficiaryDetailsByCustomerId(reqObj.getAccountSearch().getCustomerId(), env);

		}
		int index = 0;
		ArrayList param = new ArrayList();
		boolean flag = false;

		String extAccId = null;
		String ibanAccountId = null;
		PreparedStatement ps = null;

		SearchAcctRs respObj = new SearchAcctRs();

		try {
			ps = getPreparedStmnt(singleAccQuery);

			rs = executeQuery(ps);

			ArrayList<ListAccountDetails> listAccountDetailsArray = new ArrayList<>();
			ListAccountDetails[] vListAccountDetailsArray = new ListAccountDetails[reqObj.getPagedQuery()
					.getPagingRequest().getNumberOfRows()];

			while (rs.next()) {
				try {
				String accId = rs.getString(ACCT_ID);
				String accountDescription = rs.getString(ACCOUNTDESCRIPTION);
				String accountname = rs.getString(ACCOUNTNAME);

				String cncyCode = rs.getString(ISOCURRENCYCODE);
				Boolean dormantStatus = ((rs.getString(DORMANTSTATUS)).equals("Y") ? Boolean.TRUE : Boolean.FALSE);
				Boolean closed = ((rs.getString(CLOSED)).equals("Y") ? Boolean.TRUE : Boolean.FALSE);

				BigDecimal clearBalance = rs.getBigDecimal(CLEAREDBALANCE);
				BigDecimal bookBalance = rs.getBigDecimal(BOOKEDBALANCE);

				String subProductId = rs.getString(PRODUCTCONTEXTCODE);
				Date openDate = rs.getDate(OPENDATE);
				String branchShortCode = rs.getString(BRANCHSORTCODE);
				String productId = rs.getString(PRODUCTID);
				Integer accIndicator = rs.getInt(ACCRIGHTSINDICATOR);

				BigDecimal overdraftLimit = rs.getBigDecimal(DEBITLIMIT);
				BigDecimal overdraftTempLimit = rs.getBigDecimal(TEMPACCOUNTLIMIT);
				Timestamp overdraftTempExpiryDate = rs.getTimestamp(TEMPLIMEXPIRYDATE);
				String custCode = rs.getString(CUSTOMERCODE);
				String shortName = rs.getString(SHORTNAME);
				String partyType = rs.getString(CUSTOMERTYPE);
				Timestamp overdraftExpiryDate = rs.getTimestamp(LIMITEXPIRYDATE);
				String productDescription = rs.getString(PRODUCTDESCRIPTION);
				BigDecimal blockBalance = rs.getBigDecimal(BLOCKEDBALANCE);
				Date dormancyDate = rs.getDate(DATEOFDORMANCY);
				String taxCRIndicator = rs.getString(TAXINDICATORCR);
				String taxDRIndicator = rs.getString(TAXINDICATORDR);
				BigDecimal creditLimit = rs.getBigDecimal(CREDITLIMIT);
				Boolean jointAcc = ((rs.getString(JOINTACCOUNT)).equals("Y") ? Boolean.TRUE : Boolean.FALSE);

				Date closingDate = rs.getDate(CLOSUREDATE);

				Boolean stopped = (((rs.getString(STOPPED)).equals("Y") || accIndicator == 2) ? Boolean.TRUE :  Boolean.FALSE);
				ListAccountDetails listAcctDtls = new ListAccountDetails();
				BFCurrencyAmount overdraftLimitFormatted = new BFCurrencyAmount();
				CustomerShortDetails customerShortDetails = new CustomerShortDetails();
				AccountInfo accountInfo = new AccountInfo();

				AcctBalances acctBalances = new AcctBalances();
				AcctBalance bookedBalance = new AcctBalance();
				AcctBalance blockedBalance = new AcctBalance();
				BFCurrencyAmount blockedBalanceFormatted = new BFCurrencyAmount();
				AcctBalance creditlimit = new AcctBalance();
				BFCurrencyAmount creditLimitFormatted = new BFCurrencyAmount();
				int scaleVal = 0;

				Map currencyPoperties = bizInfo.getCurrencyProperties(cncyCode,
						BankFusionThreadLocal.getBankFusionEnvironment());
				scaleVal = ((Integer) currencyPoperties.get(MetaDataEnum.PROP_CURRENCYSCALE)).intValue();

				bookedBalance.setAccountBalance(bookBalance.setScale(scaleVal, RoundingMode.DOWN));

				bookedBalance.setBalanceCurrency(cncyCode);
				acctBalances.setBookedBalance(bookedBalance);

				BFCurrencyAmount bookedBalanceFormatted = new BFCurrencyAmount();
				bookedBalanceFormatted.setCurrencyAmount(bookBalance.setScale(scaleVal, RoundingMode.DOWN));
				bookedBalanceFormatted.setCurrencyCode(cncyCode);
				CbsBfCurrencyAmount cbsBookedBalanceFormatted = new CbsBfCurrencyAmount();
				cbsBookedBalanceFormatted.setBfCurrencyAmount(bookedBalanceFormatted);
				acctBalances.setBookedBalanceFormatted(cbsBookedBalanceFormatted);
				if (blockBalance != null) {
					blockedBalance.setAccountBalance(blockBalance.setScale(scaleVal, RoundingMode.DOWN));
					blockedBalance.setBalanceCurrency(cncyCode);
					acctBalances.setHoldAmount(blockedBalance);
					blockedBalanceFormatted.setCurrencyAmount(blockBalance.setScale(scaleVal, RoundingMode.DOWN));
					blockedBalanceFormatted.setCurrencyCode(cncyCode);
					CbsBfCurrencyAmount cbsHoldAmountFormatted = new CbsBfCurrencyAmount();
					cbsHoldAmountFormatted.setBfCurrencyAmount(blockedBalanceFormatted);
					acctBalances.setHoldAmountFormatted(cbsHoldAmountFormatted);
				}
				creditlimit.setAccountBalance(setBalanceScale(creditLimit, scaleVal));
				creditlimit.setBalanceCurrency(cncyCode);
				acctBalances.setShadowCredits(creditlimit);

				creditLimitFormatted.setCurrencyAmount(setBalanceScale(creditLimit, scaleVal));
				creditLimitFormatted.setCurrencyCode(cncyCode);
				CbsBfCurrencyAmount cbsCreditLimitFormatted = new CbsBfCurrencyAmount();
				cbsCreditLimitFormatted.setBfCurrencyAmount(creditLimitFormatted);
				acctBalances.setShadowCreditsFormatted(cbsCreditLimitFormatted);

				AcctBalance clearBalanceObj = new AcctBalance();
				clearBalanceObj.setAccountBalance(clearBalance.setScale(scaleVal, RoundingMode.DOWN));
				clearBalanceObj.setBalanceCurrency(cncyCode);
				acctBalances.setClearedBalance(clearBalanceObj);

				BFCurrencyAmount clearBalanceFormattedObj = new BFCurrencyAmount();
				clearBalanceFormattedObj.setCurrencyAmount(clearBalance.setScale(scaleVal, RoundingMode.DOWN));
				clearBalanceFormattedObj.setCurrencyCode(cncyCode);
				CbsBfCurrencyAmount cbsClearBalanceFormattedObj = new CbsBfCurrencyAmount();
				cbsClearBalanceFormattedObj.setBfCurrencyAmount(clearBalanceFormattedObj);
				acctBalances.setClearedBalanceFormatted(cbsClearBalanceFormattedObj);

				String skipAvailableBalance = getF_IN_SkipAvailableBalance();
				if (skipAvailableBalance == null || !skipAvailableBalance.equals(CommonConstants.Y)) {
					HashMap inputParams = new HashMap();
					inputParams.put(AVAILABLEBALANCE_IN_PARAM, accId);
					HashMap outputParams = AvailableBalanceFunction.run(accId);
					

					BigDecimal avaiableBal = (BigDecimal) (outputParams.get(AVAILABLEBALANCE_OUT_PARAM));

					AcctBalance availableBalanceObj = new AcctBalance();
					availableBalanceObj.setAccountBalance(avaiableBal.setScale(scaleVal, RoundingMode.DOWN));
					availableBalanceObj.setBalanceCurrency(cncyCode);
					BFCurrencyAmount availableBalanceFormattedObj = new BFCurrencyAmount();
					availableBalanceFormattedObj.setCurrencyAmount(avaiableBal.setScale(scaleVal, RoundingMode.DOWN));
					availableBalanceFormattedObj.setCurrencyCode(cncyCode);

					acctBalances.setAvailableBalance(availableBalanceObj);
					CbsBfCurrencyAmount cbsAvailableBalanceFormattedObj = new CbsBfCurrencyAmount();
					cbsAvailableBalanceFormattedObj.setBfCurrencyAmount(availableBalanceFormattedObj);
					acctBalances.setAvailableBalanceFormatted(cbsAvailableBalanceFormattedObj);
				}

				// setting surplus amount
				if (CommonUtil.isLoanOrFixtureProduct(productId)) {
					AcctBalance surplusAmount = new AcctBalance();
					surplusAmount.setAccountBalance(
							getSurplusAmountforLoanAccount(accId).setScale(scaleVal, RoundingMode.DOWN));
					surplusAmount.setBalanceCurrency(cncyCode);
					acctBalances.setSurplusAmount(surplusAmount);
				}
				accountInfo.setAcctBalances(acctBalances);

				AccountBasicDetails accountBasicDetails = new AccountBasicDetails();
				ibanAccountId = null;
				extAccId = null;
				AccountKeys kys = new AccountKeys();
				param.clear();
				param.add(accId);
				param.add(BT_ALTERNATE);
				param.add(IBANACCOUNT);
				List<SimplePersistentObject> extAccIdList = BankFusionThreadLocal.getPersistanceFactory()
						.executeGenericQuery(EXTERNAL_ACCID, param, null, false);

				for (SimplePersistentObject extAccountId : extAccIdList) {
					Map<String, Object> map = extAccountId.getDataMap();
					if ((map.get(IBOPseudonymAccountMap.SORTCONTEXTVALUE).toString()).equals(BT_ALTERNATE)) {
						extAccId = map.get(IBOPseudonymAccountMap.PSEUDONAME).toString();
					} else {
						ibanAccountId = map.get(IBOPseudonymAccountMap.PSEUDONAME).toString();
					}
				}

				kys.setExternalAccountId(extAccId);
				InputAccount inAcc = new InputAccount();
				inAcc.setInputAccountId(accId);
				kys.setInputAccount(inAcc);
				Pseudonym pseudonym = new Pseudonym();
				pseudonym.setIsoCurrencyCode(cncyCode);
				kys.setPseudonym(pseudonym);
				kys.setStandardAccountId(accId);
				kys.setIBAN(ibanAccountId);
				accountBasicDetails.setAccountKeys(kys);
				accountBasicDetails.setAccountName(accountname);
				accountBasicDetails.setCurrency(cncyCode);
				accountBasicDetails.setEquivalentAccountName(accountDescription);

				customerShortDetails.setPartyType(partyType);
				customerShortDetails.setCustomerId(custCode);
				customerShortDetails.setCustomerName(shortName);
				accountBasicDetails.setCustomerShortDetails(customerShortDetails);
				accountBasicDetails.setDateOpened(new Timestamp(openDate.getTime()));
				accountBasicDetails.setDebitLimit(overdraftLimit);
				accountBasicDetails.setLimitExpiryDate(new Timestamp(overdraftExpiryDate.getTime()));
				accountBasicDetails.setTempLimitExpiryDate(new Timestamp(overdraftTempExpiryDate.getTime()));

				if (!STATUS_NORMAL.equals(status)) {

					accountBasicDetails.setClosingDate(new Timestamp(closingDate.getTime()));
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
				accountBasicDetails.setSegment(findSegment(custCode));
				accountBasicDetails.setOverdraftLimit(overdraftLimit.add(overdraftTempLimit).setScale(scaleVal));
				overdraftLimitFormatted.setCurrencyAmount(overdraftLimit.add(overdraftTempLimit).setScale(scaleVal));
				overdraftLimitFormatted.setCurrencyCode(cncyCode);
				CbsBfCurrencyAmount cbsOverdraftLimitFormatted = new CbsBfCurrencyAmount();
				cbsOverdraftLimitFormatted.setBfCurrencyAmount(overdraftLimitFormatted);
				accountBasicDetails.setOverdraftLimitFormatted(cbsOverdraftLimitFormatted);

				if (overdraftTempExpiryDate.after(SystemInformationManager.getInstance().getBFBusinessDateTime())) {
					if (overdraftTempExpiryDate.after(overdraftExpiryDate)) {
						accountBasicDetails.setOverdraftExpiryDate(overdraftExpiryDate);
					} else {
						accountBasicDetails.setOverdraftExpiryDate(overdraftTempExpiryDate);
					}
				} else {
					accountBasicDetails.setOverdraftExpiryDate(overdraftExpiryDate);
				}

				accountInfo.setAcctBasicDetails(accountBasicDetails);
				Map<String, Object> inputParams = new HashMap<>();
				inputParams.put("acntRightsIndc", accIndicator);

				HashMap<String, Object> outputParams = MFExecuter.executeMF(ACCT_RIGHTS_IND_MF_NAME, env, inputParams);

				AccountCharacteristics accountCharacteristics = new AccountCharacteristics();
				accountCharacteristics.setIsClosed(closed);
				accountCharacteristics.setIsDormant(dormantStatus);
				accountCharacteristics.setIsStoped(stopped);
				accountCharacteristics.setIsJoint(jointAcc);
				accountCharacteristics.setAllCreditsBlocked((boolean) outputParams.get("allCreditsBlocked"));
				accountCharacteristics.setAllDebitsBlocked((boolean) outputParams.get("allDebitsBlocked"));
				accountCharacteristics.setAllTransactionsBlocked((boolean) outputParams.get("allTranBlocked"));
				accountCharacteristics.setEnquiryallowed((boolean) outputParams.get("enquiryAllowed"));
				accountCharacteristics.setPswdForCredit((boolean) outputParams.get("pwdForCredit"));
				accountCharacteristics.setPswdForDebit((boolean) outputParams.get("pwdForDebit"));
				accountCharacteristics.setPswdForEnquiry((boolean) outputParams.get("pwdForEnquiry"));
				accountCharacteristics.setPswdForPosting((boolean) outputParams.get("pwdForPosting"));

				accountCharacteristics.setIsMinor(0 != accIndicator);
				if (dormancyDate != null) {
					if (dormancyDate.compareTo(openDate) < 0) {
						Timestamp blankDate = null;
						accountCharacteristics.setDormancyDate(blankDate);
					} else {
						accountCharacteristics.setDormancyDate(new Timestamp(dormancyDate.getTime()));
					}
				}
				ConcurrentHashMap<String, Boolean> prodFtrMatrix = retrieveFeatureAvailability(productId);

				accountCharacteristics.setIsStatementAvailable(prodFtrMatrix.get(BANKSTATEMENTFTR));

				if (!prodFtrMatrix.get(CHQDTLSFTR)) {
					accountCharacteristics.setIsChequeBookAvailable(prodFtrMatrix.get(CHQDTLSFTR));
				} else {
					accountCharacteristics.setIsStatementAvailable(prodFtrMatrix.get(CHQDTLSFTR));
					ArrayList chqBookAvlparams = new ArrayList();
					chqBookAvlparams.add(productId);

					List<SimplePersistentObject> result = getFactory().executeGenericQuery(CHEQUE_BOOK_AVAILABLE,
							chqBookAvlparams, null, true);
					long chqBookCount = 0;
					if (null != result && !result.isEmpty()) {
						SimplePersistentObject persistentObject =  result.get(0);
						chqBookCount = (Long) persistentObject.getDataMap().get("CHQ_BOOK_COUNT");
						if (chqBookCount > 0) {
							accountCharacteristics.setIsChequeBookAvailable(Boolean.TRUE);
						} else {
							accountCharacteristics.setIsChequeBookAvailable(Boolean.FALSE);
						}
					} else {
						accountCharacteristics.setIsChequeBookAvailable(Boolean.FALSE);
					}
				}

				accountInfo.setAcctCharacteristics(accountCharacteristics);

				listAcctDtls.setAccountInfo(accountInfo);
				listAcctDtls.setSelect(index == 0);

				listAccountDetailsArray.add(index, listAcctDtls);

				vListAccountDetailsArray = listAccountDetailsArray.toArray(vListAccountDetailsArray);
				index = index + 1;
				flag = true;
				
			} catch (BankFusionException exception) {
				if (exception.getMessageNumber() != ACCESS_DENIED_ERROR) {
					throw exception;
				}
				LOGGER.error("Error While Fetching Details , Will Continue for next record ", exception);
			}

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

			HashMap result = MFExecuter.executeMF("CB_CMN_UserExitInvoker_SRV", env, retryQueueParams);

			AccountListRestrictionDtls txnList = (AccountListRestrictionDtls) result.get("outputObject");

			acc.setListAccountDetails(txnList.getAccountListRestriction());

			ListAccounts listAccounts = new ListAccounts();

			for (ListAccountDetails accountDetails : acc.getListAccountDetails()) {
				if (accountDetails != null) {
					listAccounts.addListAccountDetails(accountDetails);
				}
			}

			resObj.setSearchAccountDetails(listAccounts);

			if (acc.getListAccountDetailsCount() > 0) {
				PagingRequest pagingRequest = new PagingRequest();
				pagingRequest.setNumberOfRows(reqObj.getPagedQuery().getPagingRequest().getNumberOfRows());
				pagingRequest.setRequestedPage(reqObj.getPagedQuery().getPagingRequest().getRequestedPage());
				pagingRequest.setTotalPages(reqObj.getPagedQuery().getPagingRequest().getTotalPages());
				PagedQuery pagedQuery = new PagedQuery();
				pagedQuery.setPagingRequest(pagingRequest);
				resObj.setPagingInfo(pagedQuery);
				resObj.getRsHeader().getStatus().setOverallStatus("S");
			}

			respObj.setSearchAccountDetails(populateSearchAcct(listAccounts));
			respObj.setPagingInfo(resObj.getPagingInfo());
			respObj.setRsHeader(resObj.getRsHeader());

			if (acc.getListAccountDetailsCount() == 0) {
				LOGGER.warn("CorrelationId: " + BankFusionThreadLocal.getCorrelationID());
			}

			if (!flag) {
				CommonUtil.handleUnParameterizedEvent(20020040);
				return;

			}
			setF_OUT_searchAcctRs(respObj);
			setF_OUT_searchAccountRs(resObj);

		} catch (SQLException e) {
			LOGGER.error(ExceptionUtil.getExceptionAsString(e));
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					LOGGER.error(ExceptionUtil.getExceptionAsString(e));
				}
			}
		}

	}// end of process method

	private int getTotalNumberOfRecords() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		int totalRecord = 0;
		try {
			ps = getPreparedStmnt(countQuery);
			rs = executeQuery(ps);
			if (rs.next()) {
				totalRecord = Integer.valueOf(rs.getString("CNT"));
			}
		} catch (SQLException e) {
			LOGGER.error(ExceptionUtil.getExceptionAsString(e));
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					LOGGER.error(ExceptionUtil.getExceptionAsString(e));
				}
			}
			if (null != rs) {
				try {
					rs.close();
				} catch (SQLException e) {
					LOGGER.error(ExceptionUtil.getExceptionAsString(e));
				}
			}
		}
		return totalRecord;
	}

	private ConcurrentHashMap<String, Boolean> retrieveFeatureAvailability(String productId) {
		if (cache.cacheGet(PROD_FTR_CACHE_KEY, PROD_FTR_KEY) != null) {
			if (((ConcurrentHashMap) cache.cacheGet(PROD_FTR_CACHE_KEY, PROD_FTR_KEY)).containsKey(productId)) {
				return (ConcurrentHashMap) ((ConcurrentHashMap) cache.cacheGet(PROD_FTR_CACHE_KEY, PROD_FTR_KEY))
						.get(productId);
			} else {
				loadProductFeatureValues(productId);
			}
		} else {
			loadProductFeatureValues(productId);
		}
		return (ConcurrentHashMap) ((ConcurrentHashMap) cache.cacheGet(PROD_FTR_CACHE_KEY, PROD_FTR_KEY))
				.get(productId);
	}

	private void loadProductFeatureValues(String productId) {
		ConcurrentHashMap<String, Boolean> prodFtrDetails = new ConcurrentHashMap<>();
		ArrayList prodFtrParams = new ArrayList();
		prodFtrParams.add(productId);
		prodFtrParams.add(BANKSTATEMENTFTR);
		prodFtrParams.add(CHQDTLSFTR);
		List<IBOUBTB_PRODUCTFEATURE> prodFtrList = BankFusionThreadLocal.getPersistanceFactory()
				.findByQuery(IBOUBTB_PRODUCTFEATURE.BONAME,
						" WHERE " + IBOUBTB_PRODUCTFEATURE.UBPRODUCTPK + " = ? AND ("
								+ IBOUBTB_PRODUCTFEATURE.UBFEATUREPK + " = ? OR " + IBOUBTB_PRODUCTFEATURE.UBFEATUREPK
								+ " = ? )",
						prodFtrParams, null);
		if (prodFtrList != null && !prodFtrList.isEmpty()) {
			for (IBOUBTB_PRODUCTFEATURE prodFtr : prodFtrList) {
				UBTB_PRODUCTFEATUREID id = (UBTB_PRODUCTFEATUREID) prodFtr.getUBTB_PRODUCTFEATUREID();
				prodFtrDetails.put(BANKSTATEMENTFTR, false);
				prodFtrDetails.put(CHQDTLSFTR, false);
				if (id.getF_UBFEATURE().equals(BANKSTATEMENTFTR) && prodFtr.isF_UBISFEATUREAVAILABLE()) {
						prodFtrDetails.put(BANKSTATEMENTFTR, true);
				}

				if (id.getF_UBFEATURE().equals(CHQDTLSFTR) && prodFtr.isF_UBISFEATUREAVAILABLE()) {
						prodFtrDetails.put(CHQDTLSFTR, true);
				}
			}
		} else {
			prodFtrDetails.put(BANKSTATEMENTFTR, true);
			prodFtrDetails.put(CHQDTLSFTR, false);
		}
		if (cache.cacheGet(PROD_FTR_CACHE_KEY, PROD_FTR_KEY) != null) {
			ConcurrentHashMap<String, ConcurrentHashMap<String, Boolean>> values = (ConcurrentHashMap) cache
					.cacheGet(PROD_FTR_CACHE_KEY, PROD_FTR_KEY);
			values.put(productId, prodFtrDetails);
			cache.cachePut(PROD_FTR_CACHE_KEY, PROD_FTR_KEY, values);
		} else {
			ConcurrentHashMap<String, ConcurrentHashMap<String, Boolean>> values = new ConcurrentHashMap<>();
			values.put(productId, prodFtrDetails);
			cache.cachePut(PROD_FTR_CACHE_KEY, PROD_FTR_KEY, values);
		}
	}

	private Boolean validateUserInput() {
		Boolean validateSuccess = Boolean.FALSE;

		setChannelID(getF_IN_searchAccountRq());

		String acctId = getF_IN_searchAccountRq().getAccountSearch().getAccountId();
		String acctFrmtType = getF_IN_searchAccountRq().getAccountSearch().getAccountFormatType();
		String acctName = getF_IN_searchAccountRq().getAccountSearch().getAccountName();
		String acctStatus = getF_IN_searchAccountRq().getAccountSearch().getAccountStatus();
		String acctRelnshp = getF_IN_searchAccountRq().getAccountSearch().getAcctRelationship();
		String branch = getF_IN_searchAccountRq().getAccountSearch().getBranch();
		String custID = null;
		String ccy = getF_IN_searchAccountRq().getAccountSearch().getCurrency();
		String product = getF_IN_searchAccountRq().getAccountSearch().getProduct();
		String subPrdID = getF_IN_searchAccountRq().getAccountSearch().getSubProductId();
		Date fromDate = getF_IN_searchAccountRq().getAccountSearch().getDateAccountOpenedFrom();
		Date toDate = getF_IN_searchAccountRq().getAccountSearch().getDateAccountOpenedTo();

		IBOAttributeCollectionFeature accountInfo = null;
		if (!StringUtils.isEmpty(acctId)) {
			accountInfo = AccountFinderMethods.findAccountInfoByAccountID(acctId);
		}

		if (accountInfo != null && StringUtils.isEmpty(getF_IN_searchAccountRq().getAccountSearch().getCustomerId())
				&& (CommonUtil.isCASAProduct(accountInfo.getF_PRODUCTID())
						|| CommonUtil.isLoanOrFixtureProduct(accountInfo.getF_PRODUCTID()))) {
			custID = accountInfo.getF_CUSTOMERCODE();
			getF_IN_searchAccountRq().getAccountSearch().setCustomerId(custID);
		} else {
			custID = getF_IN_searchAccountRq().getAccountSearch().getCustomerId();
		}

		if (acctId != null && !acctId.isEmpty()) {
			validateSuccess = Boolean.TRUE;
		}
		if (acctFrmtType != null && !acctFrmtType.isEmpty()) {
			validateSuccess = Boolean.TRUE;
		}
		if (acctName != null && !acctName.isEmpty()) {
			validateSuccess = Boolean.TRUE;
		}

		if (acctStatus != null && !acctStatus.isEmpty()) {
			validateSuccess = Boolean.TRUE;
		}

		if (acctRelnshp != null && !acctRelnshp.isEmpty()) {
			validateSuccess = Boolean.TRUE;
		}
		if (branch != null && !branch.isEmpty()) {
			validateSuccess = Boolean.TRUE;
		}
		if (CommonUtil.checkIfNullOrEmpty(custID) && !isUXPChannel) {
			CommonUtil.handleUnParameterizedEvent(20020032);
		}
		if (custID != null && !custID.isEmpty() && !custID.contains(WILDCARD)) {
			IBOCustomer customer = FinderMethods.findCustomerByCustCode(custID, true);
			if (null == customer) {
				CommonUtil.handleParameterizedEvent(20020011, new String[] { custID });
			} else if (null != customer && INTERNAL_CUSTOMER.equals(customer.getF_CUSTOMERTYPE())) {
				CommonUtil.handleUnParameterizedEvent(20600273);
			} else {
				validateSuccess = Boolean.TRUE;
			}
		}

		if (ccy != null && !ccy.isEmpty()) {
			validateSuccess = Boolean.TRUE;
		}
		if (product != null && !product.isEmpty()) {
			validateSuccess = Boolean.TRUE;
		}
		if (subPrdID != null && !subPrdID.isEmpty()) {
			validateSuccess = Boolean.TRUE;
		}

		if (fromDate != null) {
			if (fromDate.compareTo(new Date(1970 - 01 - 01))> -1) {
				validateSuccess = Boolean.TRUE;
			}
			if (fromDate.compareTo(SystemInformationManager.getInstance().getBFBusinessDate()) > 0) {
				CommonUtil.handleUnParameterizedEvent(40000401);
			}
		}

		if (toDate != null) {
			if (toDate.compareTo(new Date(1970 - 01 - 01)) > -1) {
				validateSuccess = Boolean.TRUE;
			}
		}
		return validateSuccess;

	}

	private ListAcct populateSearchAcct(ListAccounts listAccounts) {
		ListAcct acctlist = new ListAcct();
		ListAccountDetails[] listAccountDetails = listAccounts.getListAccountDetails();
		if (listAccountDetails != null && listAccountDetails.length > 0) {
			for (ListAccountDetails accountDetails : listAccountDetails) {
				if (accountDetails != null) {
					acctlist.addListAccountDtls(populateListAccountDtlsObject(accountDetails.getAccountInfo()));
				}
			}
		}

		return acctlist;
	}

	private ListMandateDetails populateMandateDetails(String accountId) {
		ListMandateDetails listMandateDetails = new ListMandateDetails();
		ArrayList<String> lParams = new ArrayList<>();
		lParams.add(accountId);
		List<IBOUB_CNF_ACCTMANDATE> mandateDetails = getFactory().findByQuery(IBOUB_CNF_ACCTMANDATE.BONAME,
				FIND_MANDATE_DETAILS, lParams, null, false);

		if (CommonUtil.checkIfNotNullOrEmpty(mandateDetails)) {
			for (IBOUB_CNF_ACCTMANDATE acctMandate : mandateDetails) {
				AccountMandateDetails accountMandateDetails = new AccountMandateDetails();
				accountMandateDetails.setCustID(acctMandate.getF_UBCUSTOMERCODE());
				if (!isIBIOrMobChannel) {
					accountMandateDetails.setOwnershipPercentage(
							getOwnershipPercentage(accountId, acctMandate.getF_UBCUSTOMERCODE()));
				} else {
					accountMandateDetails.setOwnershipPercentage(BigDecimal.ZERO);
				}
				accountMandateDetails.setRole(acctMandate.getF_UBROLE());
				listMandateDetails.addAccountMandateDtls(accountMandateDetails);
			}
		}

		return listMandateDetails;
	}

	private BigDecimal getOwnershipPercentage(String accountId, String customerCode) {
		BigDecimal ownershipPercentage = BigDecimal.ZERO;
		ArrayList<String> lParams = new ArrayList<>();
		lParams.add(accountId);
		lParams.add(customerCode);

		List<IBOUB_CNF_JOINTACCOUNT> jointAccountDtls = getFactory().findByQuery(IBOUB_CNF_JOINTACCOUNT.BONAME,
				FIND_ACCOUNT_OWNERSHIP, lParams, null, false);

		if (CommonUtil.checkIfNotNullOrEmpty(jointAccountDtls)) {
			ownershipPercentage = jointAccountDtls.get(0).getF_OWNERSHIPPERCENT();
		}

		return ownershipPercentage;
	}

	private ListAccountDtls populateListAccountDtlsObject(AccountInfo accountInfo) {
		AcctInfo acctInfo = new AcctInfo();
		acctInfo.setAcctBalances(accountInfo.getAcctBalances());
		acctInfo.setAcctBasicDetails(accountInfo.getAcctBasicDetails());
		AccountCharacteristics accountCharacteristics = accountInfo.getAcctCharacteristics();

		AcctCharacteristics acctCharacteristics = new AcctCharacteristics();
		String accountId = accountInfo.getAcctBasicDetails().getAccountKeys().getStandardAccountId();
		acctCharacteristics.setModeOfOperation(getModeOfOperation(accountId));
		acctCharacteristics.setIsClosed(accountCharacteristics.getIsClosed());
		acctCharacteristics.setIsDormant(accountCharacteristics.getIsDormant());
		acctCharacteristics.setIsStoped(accountCharacteristics.getIsStoped());
		acctCharacteristics.setIsJoint(accountCharacteristics.getIsJoint());
		acctCharacteristics.setIsChequeBookAvailable(accountCharacteristics.getIsChequeBookAvailable());
		acctCharacteristics.setDormancyDate(accountCharacteristics.getDormancyDate());
		acctCharacteristics.setIsMinor(accountCharacteristics.getIsMinor());
		acctCharacteristics.setIsStatementAvailable(accountCharacteristics.getIsStatementAvailable());
		acctCharacteristics.setAllCreditsBlocked(accountCharacteristics.getAllCreditsBlocked());
		acctCharacteristics.setAllCreditsReferred(accountCharacteristics.getAllCreditsReferred());
		acctCharacteristics.setAllDebitsBlocked(accountCharacteristics.getAllDebitsBlocked());
		acctCharacteristics.setAllDebitsReferred(accountCharacteristics.getAllDebitsReferred());
		acctCharacteristics.setAllTransactionsBlocked(accountCharacteristics.getAllTransactionsBlocked());
		acctCharacteristics.setDeceasedLiquidaatedDt(accountCharacteristics.getDeceasedLiquidaatedDt());
		acctCharacteristics.setEnquiryallowed(accountCharacteristics.getEnquiryallowed());
		acctCharacteristics.setHostExtension(accountCharacteristics.getHostExtension());
		acctCharacteristics.setIsChargeWaived(accountCharacteristics.getIsChargeWaived());
		acctCharacteristics.setIsDeceasedLiquidated(accountCharacteristics.getIsDeceasedLiquidated());
		acctCharacteristics.setIsInternalAccount(accountCharacteristics.getIsInternalAccount());
		acctCharacteristics.setIsNarrativeMandatory(accountCharacteristics.getIsNarrativeMandatory());
		acctCharacteristics.setIsPassbook(accountCharacteristics.isIsPassbook());
		acctCharacteristics.setPswdForCredit(accountCharacteristics.getPswdForCredit());
		acctCharacteristics.setPswdForDebit(accountCharacteristics.getPswdForDebit());
		acctCharacteristics.setPswdForEnquiry(accountCharacteristics.getPswdForEnquiry());
		acctCharacteristics.setPswdForPosting(accountCharacteristics.getPswdForPosting());
		acctCharacteristics.setUserExtension(accountCharacteristics.getUserExtension());
		acctInfo.setAccountCharacteristics(acctCharacteristics);

		acctInfo.setListMandateDetails(populateMandateDetails(accountId));

		IBOCreditInterestFeature creditIntDtls = FinderMethods.getCreditIntFeatureBO(accountId);
		IBOFixtureFeature fixtureFeatureDtls = FinderMethods.getFixtureAccountBO(accountId);

		boolean isOtherBankPayAwayAccount = isOtherBankPayAwayAccount(creditIntDtls);

		acctInfo.setAllowPayAwayToOtherBankAccount(isOtherBankPayAwayAccount);

		acctInfo.setOtherBankPaymentDetails(populateOtherBankDetails(isOtherBankPayAwayAccount,
				accountInfo.getAcctBasicDetails().getCustomerShortDetails().getCustomerId(), creditIntDtls,
				fixtureFeatureDtls));

		acctInfo.setPayAwayAccountDetails(
				populatePayAwayAccountDetails(isOtherBankPayAwayAccount, creditIntDtls, fixtureFeatureDtls));

		ListAccountDtls listAccountDtls = new ListAccountDtls();
		listAccountDtls.setAcctInfo(acctInfo);

		return listAccountDtls;
	}

	private PayAwayAccountDetails populatePayAwayAccountDetails(boolean isOtherBankPayAwayAcc,
			IBOCreditInterestFeature creditIntDtls, IBOFixtureFeature fixtureFeatureDtls) {
		PayAwayAccountDetails payAwayAccDtls = new PayAwayAccountDetails();
		String interestPayAwayAcc = CommonConstants.EMPTY_STRING;
		String capitalPayAwayAcc = CommonConstants.EMPTY_STRING;

		if (null != creditIntDtls && !isOtherBankPayAwayAcc) {
			interestPayAwayAcc = creditIntDtls.getF_PAYAWAYCRACCOUNT();
		}

		if (null != fixtureFeatureDtls && !isOtherBankPayAwayAcc) {
			capitalPayAwayAcc = fixtureFeatureDtls.getF_CAPITALISETOACCOUNT();
		}
		payAwayAccDtls.setInterestPayawayAccount(interestPayAwayAcc);
		payAwayAccDtls.setCapitalPayawayAccount(capitalPayAwayAcc);
		return payAwayAccDtls;
	}

	private OtherBankDetails populateOtherBankDetails(boolean isOtherBankPayAwayAcc, String customerId,
			IBOCreditInterestFeature creditIntDtls, IBOFixtureFeature fixtureFeatureDtls) {
		PaymentDetails capitalAccPaymentDtls = new PaymentDetails();
		PaymentDetails interestAccPaymentDtls = new PaymentDetails();
		OtherBankDetails otherBankDtls = new OtherBankDetails();
		String intBenListDtlsId = null;
		String capBenListDtlsId = null;

		if (null != creditIntDtls) {
			intBenListDtlsId = creditIntDtls.getF_UBINTBENLISTDETAILSID();
			if (CommonUtil.checkIfNotNullOrEmpty(intBenListDtlsId) && isOtherBankPayAwayAcc) {
				if (CommonUtil.checkIfNotNullOrEmpty(getF_IN_searchAccountRq().getAccountSearch().getCustomerId())) {
					interestAccPaymentDtls = getPayAwayDtls(intBenListDtlsId, beneficiaryCustDetailsMap);
				} else {
					Map<String, BeneficiaryDetails> beneficiaryMap =  FetchBeneficiaryDetails.listBeneficiaryDetailsByCustomerId(customerId,
							BankFusionThreadLocal.getBankFusionEnvironment());
					interestAccPaymentDtls = getPayAwayDtls(intBenListDtlsId, beneficiaryMap);
				}
			}

		}
		if (null != fixtureFeatureDtls) {

			capBenListDtlsId = fixtureFeatureDtls.getF_UBCAPBENLISTDETAILSID();
			if (CommonUtil.checkIfNotNullOrEmpty(capBenListDtlsId) && isOtherBankPayAwayAcc) {
				if (CommonUtil.checkIfNotNullOrEmpty(getF_IN_searchAccountRq().getAccountSearch().getCustomerId())) {
					capitalAccPaymentDtls = getPayAwayDtls(capBenListDtlsId, beneficiaryCustDetailsMap);
				} else {
					Map<String, BeneficiaryDetails> beneficiaryMap = FetchBeneficiaryDetails.listBeneficiaryDetailsByCustomerId(customerId,
							BankFusionThreadLocal.getBankFusionEnvironment());
					capitalAccPaymentDtls = getPayAwayDtls(intBenListDtlsId, beneficiaryMap);
				}

			}
		}
		otherBankDtls.setPayAwayInterestDetails(interestAccPaymentDtls);
		otherBankDtls.setPayAwayCapitalDetails(capitalAccPaymentDtls);
		return otherBankDtls;
	}

	private PaymentDetails getPayAwayDtls(String benListDtlsId, Map<String, BeneficiaryDetails> beneficiaryMap) {

		PaymentDetails otherBankPaymentDetails = new PaymentDetails();

		BeneficiaryDetails beneficiaryDetails = beneficiaryMap.get(benListDtlsId);
		if (beneficiaryDetails != null) {
			otherBankPaymentDetails.setPayawayAccount(beneficiaryDetails.getAccountId());
			otherBankPaymentDetails.setBeneficiaryBankSortCode(beneficiaryDetails.getBankIdentifier());
			otherBankPaymentDetails.setPaymentSystem(beneficiaryDetails.getPaymentNetwork());
		}

		return otherBankPaymentDetails;

	}

	private boolean isOtherBankPayAwayAccount(IBOCreditInterestFeature creditIntDtls) {
		boolean isOtherBankPayAwayAcc = false;
		if (null != creditIntDtls) {
			isOtherBankPayAwayAcc = creditIntDtls.isF_UBISOTHERBANKPAYAWAYACC();

		}

		return isOtherBankPayAwayAcc;

	}

	private String getModeOfOperation(String accountId) {
		String modeOfOperation = null;
		IBOAttributeCollectionFeature attributeCollectionFeature = (IBOAttributeCollectionFeature) getFactory()
				.findByPrimaryKey(IBOAttributeCollectionFeature.BONAME, accountId, true);
		if (attributeCollectionFeature != null) {
			modeOfOperation = attributeCollectionFeature.getF_UBMODEOFOPERATION();
		}

		return modeOfOperation;
	}

	private String prepareQuery(String status, Set parties) {
		StringBuilder whereClause = new StringBuilder();
		String custType = INTERNAL_CUSTOMER;
		params.clear();

		StringBuilder fromClause = new StringBuilder(
				" from CUSTOMER C JOIN UBTB_ACCTMANDATE M ON C.CUSTOMERCODE = M.UBCUSTOMERCODE "
						+ " JOIN ACCOUNT A ON A.ACCOUNTID = M.UBACCOUNTID "
						+ " JOIN ProductInheritance P ON A.PRODUCTCONTEXTCODE = P.PRODUCTCONTEXTCODE "
						+ " LEFT OUTER JOIN FIXTUREFEATURE FIX ON A.ACCOUNTID = FIX.ACCOUNTID "
						+ " LEFT OUTER JOIN LOANDETAILS LND on A.ACCOUNTID = LND.ACCOUNTID ");

		StringBuilder fromClauseNormal = new StringBuilder(
				" FROM UBVW_NORMALACCOUNTS N , Account A , Customer C, PRODUCTINHERITANCE P ");

		whereClause.append(" WHERE C.CUSTOMERTYPE <> ? ");
		params.add(custType);
		SearchAccountRq reqObj = getF_IN_searchAccountRq();
		whereClause = constructWhereClause(whereClause, fromClause, parties, reqObj);

		if (STATUS_NORMAL.equals(status)) {
			whereClause.append(" AND A.UBCUSTOMERCODE  =  C.CUSTOMERCODE ");
			whereClause.append(" AND A.PRODUCTCONTEXTCODE  = P.PRODUCTCONTEXTCODE ");
			whereClause.append(" AND A.ACCOUNTID = N.ACCOUNTID");
		}
		String reqFromClause = fromClause.toString();
		if (STATUS_NORMAL.equals(status)) {
			reqFromClause = fromClauseNormal.toString();
		}

		return preparePagedQuery(whereClause, reqObj, reqFromClause);

	}

	private String preparePagedQuery(StringBuilder whereClause, SearchAccountRq reqObj, String reqFromClause) {
		int pageToProcess = reqObj.getPagedQuery().getPagingRequest().getRequestedPage();
		int pageSize = reqObj.getPagedQuery().getPagingRequest().getNumberOfRows();
		int totalPages = reqObj.getPagedQuery().getPagingRequest().getTotalPages();

		if (pageToProcess > 0 && pageSize > 0) {

			if (totalPages == 0) {
				countQuery = countQuery + reqFromClause + whereClause.toString();
				int count = getTotalNumberOfRecords();
				count = count % pageSize == 0 ? count / pageSize : count / pageSize + 1;
				reqObj.getPagedQuery().getPagingRequest().setTotalPages(count);
			}
			int fromValue = ((pageToProcess - 1) * pageSize) + 1;
			int toValue = pageToProcess * pageSize;

			params.add(fromValue);
			params.add(toValue);

			String query = QUERY + reqFromClause + whereClause.toString() + PAGINATEDQUERY;
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("SearchAccountWithRoles - query: " + query);
			}
			return query;
		} else {
			String query = QUERY + reqFromClause + whereClause.toString() + CLOSEDBRACKET;
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("SearchAccountWithRoles - query: " + query);
			}
			return QUERY + reqFromClause + whereClause.toString() + CLOSEDBRACKET;
		}
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
	 * Method Description: The purpose of this method is to round of balance amounts
	 * to proper scale.
	 * 
	 * @param BigDecimal
	 *            amountToBeScaled,
	 * @param int
	 *            scale
	 * @return BigDecimal
	 */
	private BigDecimal setBalanceScale(BigDecimal amountToBeScaled, int scaleVal) {
		String roundingOption;
		if (!ROUNDING_OPTION_AVAILABLE) {
			roundingOption = getModuleConfigValue(BANK_POSTING_MODULE, BANK_POSTING_ROUNDING_PARAM);
			if (null != roundingOption && !roundingOption.equals(CommonConstants.EMPTY_STRING)) {
				ROUNDING_OPTION_AVAILABLE = Boolean.TRUE;
				ROUNDING_OPTION = Integer.parseInt(roundingOption);
			} else {
				ROUNDING_OPTION_AVAILABLE = Boolean.TRUE;
				ROUNDING_OPTION = DEFAULT_ROUNDING_OPTION;
			}
			try {
				return amountToBeScaled.setScale(scaleVal, ROUNDING_OPTION);
			} catch (Exception E) {
				LOGGER.error(ExceptionUtil.getExceptionAsString(E));
				return amountToBeScaled.setScale(scaleVal, BigDecimal.ROUND_HALF_EVEN);

			}
		} else {
			try {
				return amountToBeScaled.setScale(scaleVal, ROUNDING_OPTION);
			} catch (Exception E) {
				LOGGER.error(ExceptionUtil.getExceptionAsString(E));
				return amountToBeScaled.setScale(scaleVal, BigDecimal.ROUND_HALF_EVEN);
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
	private String getModuleConfigValue(String moduleName, String paramName) {
		HashMap<String, ReadModuleConfigurationRq> moduleParams = new HashMap<>();
		ModuleKeyRq module = new ModuleKeyRq();
		ReadModuleConfigurationRq read = new ReadModuleConfigurationRq();
		module.setModuleId(moduleName);
		module.setKey(paramName);
		read.setModuleKeyRq(module);
		moduleParams.put("ReadModuleConfigurationRq", read);
		HashMap valueFromModuleConfiguration = MFExecuter.executeMF(READ_MODULE_CONFIGURATION_MFID,
				BankFusionThreadLocal.getBankFusionEnvironment(), moduleParams);
		if (valueFromModuleConfiguration != null) {
			ReadModuleConfigurationRs rs = (ReadModuleConfigurationRs) valueFromModuleConfiguration
					.get("ReadModuleConfigurationRs");
			return rs.getModuleConfigDetails().getValue();
		} else {
			return null;
		}
	}

	private StringBuilder constructWhereClause(StringBuilder whereClause, StringBuilder fromClause, Set parties,
			SearchAccountRq reqObj) {
		String status = getF_IN_searchAccountRq().getAccountSearch().getAccountStatus();

		if (isValidString(status)) {
			if (STATUS_NORMAL.equals(status)) {
				whereClause.append(" AND N.DORMANTSTATUS  = ? ");
				params.add(STRING_N);

				whereClause.append(" AND A.DORMANTSTATUS  = ? ");
				params.add(STRING_N);

				whereClause.append(" AND N.CLOSED  = ? ");
				params.add(STRING_N);

				whereClause.append(" AND N.ACCRIGHTSINDICATOR  = ? ");
				params.add(Integer.valueOf(0));
				whereClause.append(" AND N.UBACCOUNTSTATUS  <> ? ");
				params.add(ARREAR_ID);
				whereClause.append(" AND N.UBACCOUNTSTATUS  <> ? ");
				params.add(DELEQUENT_ID);
			} else if (CLOSED.equals(status)) {
				whereClause.append(" AND A.CLOSED = ? ");
				params.add(STRING_Y);
			} else if (STATUS_ACTIVE.equals(status)) {
				whereClause.append(" AND A.CLOSED = ? and COALESCE( FIX.STATUS,'0') NOT IN (?,?,?,?) "
						+ "and COALESCE( LND.LOANSTATUS,'0') NOT IN (?,?,?)");
				params.add(STRING_N);

				params.add(FD_STATUS_MATURED);
				params.add(FD_STATUS_EXPIRED);
				params.add(FD_STATUS_REVERSED);
				params.add(FD_STATUS_FULLY_BROKEN);
				params.add(LOAN_STATUS_SETTLED);
				params.add(LOAN_STATUS_COMPLETED);
				params.add(LOAN_STATUS_REVERSED);

			} else if (STATUS_DORMANT.equals(status)) {
				whereClause.append(" AND A.DORMANTSTATUS  = ? ");

				params.add(STRING_Y);
			} else if (STATUS_RESTRICTED.equals(status)) {
				whereClause.append(" AND A.ACCRIGHTSINDICATOR  <> ? ");

				params.add(Integer.valueOf(0));
			} else if (STATUS_DELINQUENT.equals(status)) {
				fromClause.append(" , UBTB_MOVEMENTACCSTATE MA ");
				whereClause.append(" AND A.ACCOUNTID  = MA.UBAccountID ");
				whereClause.append(" AND MA.UBArrearsStatus  = ? ");

				params.add(DELEQUENT_ID);
			} else if (STATUS_ARREARS.equals(status)) {
				fromClause.append(" , UBTB_MOVEMENTACCSTATE MA ");
				whereClause.append(" AND A.ACCOUNTID  = MA.UBAccountID ");
				whereClause.append(" AND MA.UBArrearsStatus  <> ? ");
				params.add(DELEQUENT_ID);
            }
            else if (STATUS_INACTIVE.equals(status)) {
                whereClause.append(" AND A.UBACCOUNTSTATUS  = ? ");
                params.add(status);
			}

		}
		if (reqObj.getAccountSearch() instanceof AccountSearchForCustomers) {
			whereClause.append(" AND C.CUSTOMERCODE in ( ");

			Object[] partyArray = parties.toArray();
			for (int i = 0, n = partyArray.length; i < n; i++) {
				if (i == (n - 1)) {
					whereClause.append("?)");
				} else {
					whereClause.append("?,");
				}
				params.add(partyArray[i]);
			}
		} else if (!isUXPChannel) {
			whereClause.append(" AND C.CUSTOMERCODE = ? ");
			params.add(reqObj.getAccountSearch().getCustomerId());
		} else if (isValidString(reqObj.getAccountSearch().getCustomerId())) {
			whereClause.append(" AND C.CUSTOMERCODE LIKE ? ");
			params.add(reqObj.getAccountSearch().getCustomerId() + '%');
		}

		if (isValidString(reqObj.getAccountSearch().getAccountId())) {
			whereClause.append(" AND A.ACCOUNTID  = ? ");
			params.add(reqObj.getAccountSearch().getAccountId());
		}

		if (isValidString(reqObj.getAccountSearch().getAccountName())) {
			if (isUXPChannel) {
				whereClause.append(" AND A.ACCOUNTNAME  LIKE ? ");
			} else {
				whereClause.append(" AND A.ACCOUNTNAME  = ? ");
			}
			params.add(reqObj.getAccountSearch().getAccountName());
		}

		if (isValidString(reqObj.getAccountSearch().getBranch())) {
			whereClause.append(" AND A.BRANCHSORTCODE = ? ");
			params.add(reqObj.getAccountSearch().getBranch());
		}

		if (isValidString(reqObj.getAccountSearch().getCurrency())) {
			whereClause.append(" AND A.ISOCURRENCYCODE  = ? ");
			params.add(reqObj.getAccountSearch().getCurrency());
		}

		if (isValidString(reqObj.getAccountSearch().getProduct())) {
			whereClause.append(" AND A.PRODUCTID " + " LIKE ? ");
			params.add(reqObj.getAccountSearch().getProduct() + '%');
		}

		if (isValidString(reqObj.getAccountSearch().getSubProductId())) {

			whereClause.append(" AND P.PRODUCTCONTEXTCODE " + " LIKE ? ");
            params.add(reqObj.getAccountSearch().getSubProductId() + '%');
        }
        
        if(reqObj.getAccountSearch().getJointAccountRequired() != null && !CommonConstants.EMPTY_STRING.equals(reqObj.getAccountSearch().getJointAccountRequired())) {
            whereClause.append(" AND A.JOINTACCOUNT  = ?");
            params.add(reqObj.getAccountSearch().getJointAccountRequired());
        }

		Date fromDate = reqObj.getAccountSearch().getDateAccountOpenedFrom();
		if (!CommonUtil.checkIfInvalidDate(fromDate)) {

			whereClause.append(" AND A.OPENDATE" + " >= ? ");
			params.add(fromDate);
		}
		Date toDate = reqObj.getAccountSearch().getDateAccountOpenedTo();
		if (!CommonUtil.checkIfInvalidDate(toDate)) {

			toDate = DateUtil.getStaticDateForDate(toDate);
			Date businessDt = DateUtil.getBusinessStaticDate();

			if (toDate.before(businessDt)) {
				whereClause.append(" AND A.OPENDATE " + " <= ? ");
				params.add(toDate);
			}
		}

		return whereClause;
	}

	private PreparedStatement getPreparedStmnt(String query) throws SQLException {

		Connection jdbcConnection = BankFusionThreadLocal.getPersistanceFactory().getJDBCConnection();
		BankFusionThreadLocal.getPersistanceFactory();
		PreparedStatement ps = jdbcConnection.prepareStatement(query);
		for (int i = 1; i <= params.size(); i++) {
			ps.setObject(i, params.get(i - 1));
		}
		return ps;
	}

	private ResultSet executeQuery(PreparedStatement ps) throws SQLException {
		ResultSet rs = ps.executeQuery();
		return rs;
	}

	private BigDecimal getSurplusAmountforLoanAccount(String accountID) {
		BigDecimal surplusAmount = BigDecimal.ZERO;
		ArrayList<String> lParams = new ArrayList<>();
		lParams.add(accountID);
		List<IBOUB_CNF_SurplusTxnDtls> surplusTxnDetails = (getFactory().findByQuery(IBOUB_CNF_SurplusTxnDtls.BONAME,
				FIND_SURPLUS_AMOUNT_BY_ACCOUNT_WHERECLAUSE, lParams, null, false));
		if (surplusTxnDetails != null && !surplusTxnDetails.isEmpty()) {
			for (int i = 0; i < surplusTxnDetails.size(); i++) {
				surplusAmount = surplusAmount.add(surplusTxnDetails.get(i).getF_SURPLUSAMOUNT());
			}
		}
		return surplusAmount;
	}

	private String findSegment(String partyID) {
		IBOCustomer customerBO = FinderMethods.findCustomerByCustCode(partyID, true);
		if (null != customerBO) {
			segment = customerBO.getF_CUSTSEGMENTID();
		}
		return segment;
	}

	private void setChannelID(SearchAccountRq reqObj) {
		if (null != reqObj.getRqHeader() && null != reqObj.getRqHeader().getOrig()) {
			String channelID = reqObj.getRqHeader().getOrig().getChannelId();
			if (CommonUtil.checkIfNullOrEmpty(channelID)) {
				channelID = BankFusionThreadLocal.getChannel();
			}

			if (CHANNEL_IBI.equals(channelID) || CHANNEL_MOB.equals(channelID)) {
				isIBIOrMobChannel = true;
			} else if (CHANNEL_UXP.equals(channelID)) {
				isUXPChannel = true;
			}
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("SearchAccountWithRoles - ChannelID: " + channelID);
			}
		}
	}
}