package com.misys.ub.fatoms.batch.bpRefresh;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.common.events.CommonsEventCodes;
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.process.AbstractBatchProcess;
import com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator;
import com.trapedza.bankfusion.bo.refimpl.IBOAccPortMap;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAccountBundle;
import com.trapedza.bankfusion.bo.refimpl.IBOAccountNoteFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOAddress;
import com.trapedza.bankfusion.bo.refimpl.IBOAddressLinks;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOBundleDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOBundleDetailsTxnCodeMap;
import com.trapedza.bankfusion.bo.refimpl.IBOCreditInterestFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOCurrency;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.bo.refimpl.IBODebitInterestFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOLimit;
import com.trapedza.bankfusion.bo.refimpl.IBOLimitDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOProductInheritance;
import com.trapedza.bankfusion.bo.refimpl.IBOStoppedChq;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_CMN_BatchProcessLog;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.utils.GUIDGen;

public class BranchPowerRefreshProcess extends AbstractBatchProcess {
	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $"; //$NON-NLS-1$
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private static final Log logger = LogFactory.getLog(BranchPowerRefreshProcess.class.getName());

	private static final String BranchPowerWhereClause = " WHERE " + IBOBranch.BMBRANCH + " BETWEEN ? AND ? ";

	// Initialize variables to be read from properties file
	public String fromBranch = CommonConstants.EMPTY_STRING;
	public String toBranch = CommonConstants.EMPTY_STRING;
	public String extractPath = CommonConstants.EMPTY_STRING;
	public String custCategory = CommonConstants.EMPTY_STRING;
	public String branchCategory = CommonConstants.EMPTY_STRING;
	// PRJ511125-START
	public String CustRefFlag = CommonConstants.EMPTY_STRING;
	public String AccRefFlag = CommonConstants.EMPTY_STRING;
	public String CustExtRefFlag = CommonConstants.EMPTY_STRING;
	public String StopChequeRefFlag = CommonConstants.EMPTY_STRING;
	// PRJ511125-END
	// bundlepricing-start
	public String AccBundleFlag = CommonConstants.EMPTY_STRING;
	public String TransBundleFlag = CommonConstants.EMPTY_STRING;
	String strHdrDt = CommonConstants.EMPTY_STRING;
	ArrayList branchRange;
	Hashtable CurrencyHash = new Hashtable();
	// String path;

	private BankFusionEnvironment env = null;
	// private AbstractFatomContext context=null;

	private AbstractProcessAccumulator accumulator;

	static Properties fileBPRefreshProp;
	static Properties fileRefreshProp;

	// bundlepricing-end

	final String queryCustomer = "SELECT (T1." + IBOCustomer.CUSTOMERCODE + ") AS " + IBOCustomer.CUSTOMERCODE + ",(T1."
			+ IBOCustomer.SHORTNAME + ") AS " + IBOCustomer.SHORTNAME + ",(T2." + IBOBranch.BMBRANCH + ") AS "
			+ IBOBranch.BMBRANCH + ",(T1." + IBOCustomer.ALPHACODE + ") AS " + IBOCustomer.ALPHACODE + ",(T3."
			+ IBOLimit.LIMITINDICATOR + ") AS " + IBOLimit.LIMITINDICATOR + ",(T5." + IBOAddress.ADDRESSLINE1 + ") AS "
			+ IBOAddress.ADDRESSLINE1 + ",(T5." + IBOAddress.ADDRESSLINE2 + ") AS " + IBOAddress.ADDRESSLINE2 + ",(T5."
			+ IBOAddress.ADDRESSLINE3 + ") AS " + IBOAddress.ADDRESSLINE3 + ",(T5." + IBOAddress.ADDRESSLINE4 + ") AS "
			+ IBOAddress.ADDRESSLINE4 + ",(T5." + IBOAddress.ADDRESSLINE5 + ") AS " + IBOAddress.ADDRESSLINE5 + ",(T5."
			+ IBOAddress.ADDRESSLINE6 + ") AS " + IBOAddress.ADDRESSLINE6 + ",(T5." + IBOAddress.ADDRESSLINE7 + ") AS "
			+ IBOAddress.ADDRESSLINE7 + ",(T1." + IBOCustomer.REPORTINGCURRENCY + ") AS "
			+ IBOCustomer.REPORTINGCURRENCY + " FROM " + IBOCustomer.BONAME + " T1," + IBOBranch.BONAME + " T2,"
			+ IBOLimit.BONAME + " T3," + IBOAddressLinks.BONAME + " T4," + IBOAddress.BONAME + " T5" + " WHERE "
			+ " T2." + IBOBranch.BMBRANCH + " BETWEEN ? AND ?" + " AND T1." + IBOCustomer.BRANCHSORTCODE + "= T2."
			+ IBOBranch.BRANCHSORTCODE + " AND T1." + IBOCustomer.CUSTOMERCODE + "=T3." + IBOLimit.LIMITREF + " AND T1."
			+ IBOCustomer.CUSTOMERCODE + "=T4." + IBOAddressLinks.CUSTACC_KEY + " AND T4." + IBOAddressLinks.ADDRESSID
			+ "=T5." + IBOAddress.ADDRESSID + " order by T2." + IBOBranch.BMBRANCH;

	final String queryAccount = "SELECT (T1." + IBOAccount.ACCOUNTID + ") AS " + IBOAccount.ACCOUNTID + ",(T1."
			+ IBOAccount.ACCOUNTNAME + ") AS " + IBOAccount.ACCOUNTNAME + ",(T1." + IBOAccount.ACCRIGHTSINDICATOR
			+ ") AS " + IBOAccount.ACCRIGHTSINDICATOR + ",(T1." + IBOAccount.BOOKEDBALANCE + ") AS "
			+ IBOAccount.BOOKEDBALANCE + ",(T1." + IBOAccount.CLEAREDBALANCE + ") AS " + IBOAccount.CLEAREDBALANCE
			+ ",(T1." + IBOAccount.ISOCURRENCYCODE + ") AS " + IBOAccount.ISOCURRENCYCODE + ",(T1." + IBOAccount.STOPPED
			+ ") AS " + IBOAccount.STOPPED + ",(T2." + IBOBranch.BMBRANCH + ") AS " + IBOBranch.BMBRANCH + ",(T1."
			+ IBOAccount.BRANCHSORTCODE + ") AS " + IBOAccount.BRANCHSORTCODE + ",(T1." + IBOAccount.CREDITLIMIT
			+ ") AS " + IBOAccount.CREDITLIMIT + ",(T1." + IBOAccount.DEBITLIMIT + ") AS " + IBOAccount.DEBITLIMIT
			+ ",(T1." + IBOAccount.LIMITINDICATOR + ") AS " + IBOAccount.LIMITINDICATOR + ",(T1."
			+ IBOAccount.CHEQUEDEPOSITBALANCE + ") AS " + IBOAccount.CHEQUEDEPOSITBALANCE + ",(T1."
			+ IBOAccount.ACCOUNTDESCRIPTION + ") AS " + IBOAccount.ACCOUNTDESCRIPTION + ",(T1."
			+ IBOAccount.BLOCKEDBALANCE + ") AS " + IBOAccount.BLOCKEDBALANCE + ",(T1." + IBOAccount.LASTTRANSACTIONDATE
			+ ") AS " + IBOAccount.LASTTRANSACTIONDATE + ",(T3." + IBOAccPortMap.PORTFOLIOID + ") AS "
			+ IBOAccPortMap.PORTFOLIOID + ",(T4." + IBOProductInheritance.PRODUCT_NUMERICCODE + ") AS "
			+ IBOProductInheritance.PRODUCT_NUMERICCODE + ",(T4." + IBOProductInheritance.LIM_LIMITREF1 + ") AS "
			+ IBOProductInheritance.LIM_LIMITREF1 + ",(T4." + IBOProductInheritance.LIM_LIMITREF2 + ") AS "
			+ IBOProductInheritance.LIM_LIMITREF2 + ",(T4." + IBOProductInheritance.LIM_LIMITREF3 + ") AS "
			+ IBOProductInheritance.LIM_LIMITREF3 + ",(T4." + IBOProductInheritance.LIM_LIMITREF4 + ") AS "
			+ IBOProductInheritance.LIM_LIMITREF4 + ",(T4." + IBOProductInheritance.LIM_LIMITREF5 + ") AS "
			+ IBOProductInheritance.LIM_LIMITREF5

			+ " FROM " + IBOAccount.BONAME + " T1, " + IBOBranch.BONAME + " T2, " + IBOAccPortMap.BONAME + " T3,"
			+ IBOProductInheritance.BONAME + " T4"

			+ " WHERE " + " T2." + IBOBranch.BMBRANCH + " BETWEEN ? AND ?" + " AND T1." + IBOAccount.BRANCHSORTCODE
			+ " = T2." + IBOBranch.BRANCHSORTCODE + " AND T1." + IBOAccount.PRODUCTCONTEXTCODE + " = T4."
			+ IBOProductInheritance.PRODUCTCONTEXTCODE + " AND T1." + IBOAccount.ACCOUNTID + " = T3."
			+ IBOAccPortMap.ACCOUNTID + " ORDER BY T1." + IBOAccount.BRANCHSORTCODE;

	// SQL Query for Credit Interest (Used for Account Refresh)
	final String queryCreditInterest = "SELECT (T3." + IBOCreditInterestFeature.ACCOUNTID + ") AS "
			+ IBOCreditInterestFeature.ACCOUNTID + ", (T3." + IBOCreditInterestFeature.ACCDCRINTEREST + ") AS "
			+ IBOCreditInterestFeature.ACCDCRINTEREST + ", (T4." + IBOAccount.ACCOUNTID + ") AS " + IBOAccount.ACCOUNTID
			+ ", (T4." + IBOAccount.BRANCHSORTCODE + ") AS " + IBOAccount.BRANCHSORTCODE + " FROM "
			+ IBOCreditInterestFeature.BONAME + " T3, " + IBOAccount.BONAME + " T4" + " WHERE " + " T3."
			+ IBOCreditInterestFeature.ACCOUNTID + "=" + "T4." + IBOAccount.ACCOUNTID;

	// SQL Query for Debit Interest (Used for Account Refresh)
	final String queryDebitInterest = "SELECT (T4." + IBODebitInterestFeature.ACCOUNTID + ") AS "
			+ IBODebitInterestFeature.ACCOUNTID + ", (T4." + IBODebitInterestFeature.DEBITACCDINTEREST + ") AS "
			+ IBODebitInterestFeature.DEBITACCDINTEREST + ", (T5." + IBOAccount.ACCOUNTID + ") AS "
			+ IBOAccount.ACCOUNTID + ", (T5." + IBOAccount.BRANCHSORTCODE + ") AS " + IBOAccount.BRANCHSORTCODE
			+ " FROM " + IBODebitInterestFeature.BONAME + " T4, " + IBOAccount.BONAME + " T5" + " WHERE" + " T4."
			+ IBODebitInterestFeature.PENALTYINTEREST + " = 0 AND " + " T4." + IBODebitInterestFeature.ACCOUNTID + "="
			+ "T5." + IBOAccount.ACCOUNTID;

	// PRJ511125-END

	// SQL Query for Account Note
	final String queryAccountNote = "SELECT (T1." + IBOAccountNoteFeature.ACCOUNTID + ") AS "
			+ IBOAccountNoteFeature.ACCOUNTID + " , (T1." + IBOAccountNoteFeature.NOTE + ") AS "
			+ IBOAccountNoteFeature.NOTE + " FROM " + IBOAccountNoteFeature.BONAME + " T1";

	final String queryStoppedCheques = "SELECT" + " (T2." + IBOStoppedChq.STOPDATE + ") AS " + IBOStoppedChq.STOPDATE
			+ " ,(T2." + IBOStoppedChq.STOPREASON + ") AS " + IBOStoppedChq.STOPREASON + " ,(T2."
			+ IBOStoppedChq.ACCOUNTID + ") AS " + IBOStoppedChq.ACCOUNTID + " ,(T2." + IBOStoppedChq.TOSTOPCHQREF
			+ ") AS " + IBOStoppedChq.TOSTOPCHQREF + " ,(T2." + IBOStoppedChq.AMOUNT + ") AS " + IBOStoppedChq.AMOUNT
			+ " ,(T2." + IBOStoppedChq.STOPPEDSTATUS + ") AS " + IBOStoppedChq.STOPPEDSTATUS + " ,(T2."
			+ IBOStoppedChq.FROMSTOPCHQREF + ") AS " + IBOStoppedChq.FROMSTOPCHQREF + " ,(T2."
			+ IBOStoppedChq.ISOCURRENCYCODE + ") AS " + IBOStoppedChq.ISOCURRENCYCODE + " FROM " + IBOAccount.BONAME
			+ " T1, " + IBOStoppedChq.BONAME + " T2" + " WHERE " + "T1." + IBOAccount.ACCOUNTID + " = T2."
			+ IBOStoppedChq.ACCOUNTID;
	// bug-6261-end

	// SQL Query 1 for Customer Limits (limit details)
	final String queryLimit = "SELECT (T5." + IBOLimitDetails.LIMITREF + ") AS " + IBOLimitDetails.LIMITREF + ", (T5."
			+ IBOLimitDetails.CURRENCY + ") AS " + IBOLimitDetails.CURRENCY + ", (T5." + IBOLimitDetails.LIMIT + ") AS "
			+ IBOLimitDetails.LIMIT + ", (T5." + IBOLimitDetails.EXPOSURE + ") AS " + IBOLimitDetails.EXPOSURE
			+ ", (T6." + IBOLimit.LIMITINDICATOR + ") AS " + IBOLimit.LIMITINDICATOR + " FROM " + IBOLimitDetails.BONAME
			+ " T5," + IBOLimit.BONAME + " T6 where T5." + IBOLimitDetails.LIMITREF + "= T6." + IBOLimit.LIMITREF
			+ " AND T6." + IBOLimit.LIMITCATEGORY + " = ? order by T6." + IBOLimit.LIMITREF;

	// SQL Query 2 for Customer Limits (customer details)
	final String queryCustLimit = "SELECT (T1." + IBOCustomer.CUSTOMERCODE + ") AS " + IBOCustomer.CUSTOMERCODE
			+ ",(T2." + IBOBranch.BMBRANCH + ") AS " + IBOBranch.BMBRANCH + " FROM " + IBOCustomer.BONAME + " T1,"
			+ IBOBranch.BONAME + " T2" + " WHERE " + " T2." + IBOBranch.BMBRANCH + " BETWEEN ? AND ?" + " AND T1."
			+ IBOCustomer.BRANCHSORTCODE + "= T2." + IBOBranch.BRANCHSORTCODE;

	// SQL Query to read currency Scale for amount formatting
	final String queryCurrency = "SELECT (T1." + IBOCurrency.ISOCURRENCYCODE + ") AS " + IBOCurrency.ISOCURRENCYCODE
			+ ",(T1." + IBOCurrency.CURRENCYSCALE + ") AS " + IBOCurrency.CURRENCYSCALE + " FROM " + IBOCurrency.BONAME
			+ " T1";

	// bundle-start
	final String queryAccountBundle = "SELECT (T2." + IBOBundleDetails.BUNDLECODE + ") AS "
			+ IBOBundleDetails.BUNDLECODE + ",(T1." + IBOAccountBundle.BUNDLECODE + ") AS "
			+ IBOAccountBundle.BUNDLECODE + ",(T1." + IBOAccountBundle.ACCOUNTID + ") AS " + IBOAccountBundle.ACCOUNTID
			+ ",(T2." + IBOBundleDetails.ACCOUNTSTYLE + ") AS " + IBOBundleDetails.ACCOUNTSTYLE + ",(T2."
			+ IBOBundleDetails.THRESHOLDTXNCOUNT + ") AS " + IBOBundleDetails.THRESHOLDTXNCOUNT + " FROM "
			+ IBOAccountBundle.BONAME + " T1," + IBOBundleDetails.BONAME + " T2," + IBOAccount.BONAME + " T3"
			+ " WHERE " + " T1." + IBOAccountBundle.BUNDLECODE + "= T2." + IBOBundleDetails.BUNDLECODE + " AND T3."
			+ IBOAccount.ACCOUNTID + "= T1." + IBOAccountBundle.ACCOUNTID;

	final String queryTransBundle = "SELECT (T1." + IBOBundleDetails.BUNDLECODE + ") AS " + IBOBundleDetails.BUNDLECODE
			+ ",(T2." + IBOBundleDetailsTxnCodeMap.TXNCODE + ") AS " + IBOBundleDetailsTxnCodeMap.TXNCODE + ",(T2."
			+ IBOBundleDetailsTxnCodeMap.BUNDLECODE + ") AS " + IBOBundleDetailsTxnCodeMap.BUNDLECODE + ",(T2."
			+ IBOBundleDetailsTxnCodeMap.CHARGELEG + ") AS " + IBOBundleDetailsTxnCodeMap.CHARGELEG + " FROM "
			+ IBOBundleDetails.BONAME + " T1," + IBOBundleDetailsTxnCodeMap.BONAME + " T2" + " WHERE " + " T1."
			+ IBOBundleDetails.BUNDLECODE + "= T2." + IBOBundleDetailsTxnCodeMap.BUNDLECODE;

	// bundle-end

	TreeMap accHash = new TreeMap();
	TreeMap limitHash = new TreeMap();

	StringBuffer fileData = new StringBuffer();

	/**
	 * <code>mcfaData</code> Account Detail Record Structure class
	 */
	private class mcfaData {

		public String FA_DTL_REC_TYPE = CommonConstants.EMPTY_STRING;
		public String FA_DTL_ACC_ID = CommonConstants.EMPTY_STRING;
		public String FA_DTL_SHORTNAME1 = CommonConstants.EMPTY_STRING;
		public String FA_DTL_SHORTNAME2 = CommonConstants.EMPTY_STRING;
		public String FA_DTL_LED_SUBLED = CommonConstants.EMPTY_STRING;
		public String FA_DTL_CURR = CommonConstants.EMPTY_STRING;
		public String FA_DTL_ACC_TYPE = CommonConstants.EMPTY_STRING;
		public String FA_DTL_ACC_TYPE_DESC = CommonConstants.EMPTY_STRING;
		public String FA_DTL_CLIENT_NUMBER = CommonConstants.EMPTY_STRING;
		public String FA_DTL_STOP_BLK_IND = CommonConstants.EMPTY_STRING;
		public String FA_DTL_DEBIT_LIMIT = "0";
		public String FA_DTL_DEBIT_LIMIT_EXP = "0";
		public String FA_DTL_FILLER1 = CommonConstants.EMPTY_STRING;
		public String FA_DTL_CLEARED_BAL = "0";
		public String FA_DTL_BOOK_BAL = "0";
		public String FA_DTL_GARN_HOLD = "0";
		public String FA_DTL_LIEN_HOLD = "0";
		public String FA_DTL_BLOCK_BAL = "0";
		public String FA_DTL_SEC_RATING = "0";
		public String FA_DTL_STAFF_IND = CommonConstants.EMPTY_STRING;
		public String FA_DTL_CREDIT_LIMIT = "0";
		public String FA_DTL_CREDIT_LIMIT_EXP = "0";
		public String FA_DTL_ARREARS_BAL = "0";
		public String FA_DTL_DATE_LAST_EXT = "0";
		public String FA_DTL_ACCRUED_INT = "0";
		public String FA_DTL_1_DAYS_ACCD_INT = CommonConstants.EMPTY_STRING;
		public String FA_DTL_ACCD_INT_DATE = "0";
		public String FA_DTL_RATE_BASIS = CommonConstants.EMPTY_STRING;
		public String FA_DTL_TOTAL_LOAN = "0";
		public String FA_DTL_TOTAL_LOAN_DRAW = "0";
		public String FA_DTL_ALT_IDENTIFIER = CommonConstants.EMPTY_STRING;
		public String FA_DTL_ALT_ID_FILLER = CommonConstants.EMPTY_STRING;
		public String FA_DTL_PASS_PROT_FLAG = CommonConstants.EMPTY_STRING;
		public String FA_DTL_LIMIT_CHK_FLAG = CommonConstants.EMPTY_STRING;
		public String FA_DTL_LIMIT_NUMB_1 = CommonConstants.EMPTY_STRING;
		public String FA_DTL_LIMIT_NUMB_2 = CommonConstants.EMPTY_STRING;
		public String FA_DTL_LIMIT_NUMB_3 = CommonConstants.EMPTY_STRING;
		public String FA_DTL_LIMIT_NUMB_4 = CommonConstants.EMPTY_STRING;
		public String FA_DTL_LIMIT_NUMB_5 = CommonConstants.EMPTY_STRING;
		public String FA_DTL_SUM_CHQ_DEP_BAL = CommonConstants.EMPTY_STRING;
		public String FA_DTL_FILLER_2 = CommonConstants.EMPTY_STRING;
		public String FA_DTL_BRANCH = CommonConstants.EMPTY_STRING;
		public String FA_DTL_ACTION = CommonConstants.EMPTY_STRING;
		public String FA_DTL_CHECKSUM = CommonConstants.EMPTY_STRING;
		public String FA_DTL_FILLER_3 = CommonConstants.EMPTY_STRING;
	}

	/**
	 * <code>mcflData</code> Limit Detail Record Structure class
	 */
	private class mcflData {
		public String FL_DT2_RECORD_TYPE = CommonConstants.EMPTY_STRING;
		public String FL_DT2_CLT_NUMBER = CommonConstants.EMPTY_STRING;
		public String FL_DT2_CLT_LIMIT_CURR = CommonConstants.EMPTY_STRING;
		public String FL_DT2_LIMIT_CHK_FLAG = CommonConstants.EMPTY_STRING;
		public String FL_DT2_LMT_AMT1 = CommonConstants.EMPTY_STRING;
		public String FL_DT2_EXP_AMT1 = CommonConstants.EMPTY_STRING;
		public String FL_DT2_LMT_AMT2 = CommonConstants.EMPTY_STRING;
		public String FL_DT2_EXP_AMT2 = CommonConstants.EMPTY_STRING;
		public String FL_DT2_LMT_AMT3 = CommonConstants.EMPTY_STRING;
		public String FL_DT2_EXP_AMT3 = CommonConstants.EMPTY_STRING;
		public String FL_DT2_LMT_AMT4 = "0";
		public String FL_DT2_EXP_AMT4 = "0";
		public String FL_DT2_LMT_AMT5 = "0";
		public String FL_DT2_EXP_AMT5 = "0";
		public String FL_DT2_LMT_AMT6 = "0";
		public String FL_DT2_EXP_AMT6 = "0";
		public String FL_DT2_LMT_AMT7 = "0";
		public String FL_DT2_EXP_AMT7 = "0";
		public String FL_DT2_LMT_AMT8 = "0";
		public String FL_DT2_EXP_AMT8 = "0";
		public String FL_DT2_LMT_AMT9 = "0";
		public String FL_DT2_EXP_AMT9 = "0";
		public String FL_DT2_LMT_AMT10 = "0";
		public String FL_DT2_EXP_AMT10 = "0";
		public String FL_DT2_CLT_LMT_INFO = CommonConstants.EMPTY_STRING;
		public String FL_DT2_FILLER1 = CommonConstants.EMPTY_STRING;
		public String FL_DT2_BRANCH_CODE = CommonConstants.EMPTY_STRING;
		public String FL_DT2_ACTION = "A";
		public String FL_DT2_CHECKSUM = CommonConstants.EMPTY_STRING;
		public String FL_DT2_FILLER2 = CommonConstants.EMPTY_STRING;

	}

	String MSG1 = CommonConstants.EMPTY_STRING;;

	Boolean Status;

	/**
	 * @param environment
	 *            Used to get a handle on the BankFusion environment
	 * @param context
	 *            A set of data passed to the PreProcess, Process and PostProcess
	 *            classes
	 * @param priority
	 *            Thread priority
	 */
	public BranchPowerRefreshProcess(BankFusionEnvironment environment, AbstractFatomContext context,
			Integer priority) {
		super(environment, context, priority);
		this.context = context;
		env = environment;
	}

	/**
	 * Initialise parameters and the accumulator for the BalanceSheetCollection
	 * process
	 *
	 * @see com.trapedza.bankfusion.batch.process.AbstractBatchProcess#init()
	 */
	public void init() {

		initialiseAccumulator();
	}

	/**
	 * Gets a reference to the accumulator
	 *
	 * @return A reference to the accumulator
	 * @see com.trapedza.bankfusion.batch.process.AbstractBatchProcess#getAccumulator()
	 */
	public AbstractProcessAccumulator getAccumulator() {
		return accumulator;
	}

	/**
	 * @see com.trapedza.bankfusion.batch.process.AbstractBatchProcess#initialiseAccumulator()
	 */
	protected void initialiseAccumulator() {
		Object[] accumulatorArgs = new Object[0];
		accumulator = new BranchPowerRefreshAccumulator(accumulatorArgs);
	}

	/**
	 * Processes the branchpowerRefresh on the specified page, and accumulates the
	 * totals.
	 *
	 * @param pageToProcess
	 *            Page number of the page to be processed
	 * @return The accumulator
	 * @see com.trapedza.bankfusion.batch.process.AbstractBatchProcess#process(int) @
	 * Thrown if a BankFusionException occurs when processing the balance sheets and
	 * accumulating the totals, or if ServiceException or ErrorOnCommitException
	 * occur when commit or rolling back the transaction.
	 */
	public AbstractProcessAccumulator process(int pageToProcess) {

		logger.debug("Invoking Page: " + pageToProcess);

		Object[] additionalParameters = context.getAdditionalProcessParams();

		fileBPRefreshProp = (Properties) additionalParameters[0];

		if (fileBPRefreshProp == null || fileBPRefreshProp.size() == 0) {
			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { "Error Reading Properties File" }, new HashMap(), env);
		}

		if ((getBPRereshProperty("FROMBRANCH").equalsIgnoreCase(CommonConstants.EMPTY_STRING))
				|| (getBPRereshProperty("TOBRANCH").equalsIgnoreCase(CommonConstants.EMPTY_STRING))
				|| (getBPRereshProperty("EXTRACTPATH").equalsIgnoreCase(CommonConstants.EMPTY_STRING))
				|| (getBPRereshProperty("CUST-LIM-CATEGORY").equalsIgnoreCase(CommonConstants.EMPTY_STRING))
				|| (getBPRereshProperty("BRCH-LIM-CATEGORY").equalsIgnoreCase(CommonConstants.EMPTY_STRING))
				|| (getBPRereshProperty("CUSTOMER-REFRESH").equalsIgnoreCase(CommonConstants.EMPTY_STRING))
				|| (getBPRereshProperty("ACCOUNT-REFRESH").equalsIgnoreCase(CommonConstants.EMPTY_STRING))
				|| (getBPRereshProperty("CUSTOMER-EXT-REFRESH").equalsIgnoreCase(CommonConstants.EMPTY_STRING))
				|| (getBPRereshProperty("STOP-CHEQUE-REFRESH").equalsIgnoreCase(CommonConstants.EMPTY_STRING))
				|| (getBPRereshProperty("ACCOUNT-BUNDLE-REFRESH").equalsIgnoreCase(CommonConstants.EMPTY_STRING))
				|| (getBPRereshProperty("TRANS-BUNDLE-REFRESH").equalsIgnoreCase(CommonConstants.EMPTY_STRING)))
		// bundlepricing-end
		{
			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { "Invalid Parameters passed" }, new HashMap(), env);
		} else {
			fromBranch = getBPRereshProperty("FROMBRANCH");
			toBranch = getBPRereshProperty("TOBRANCH");
		}
		toBranch = getBPRereshProperty("TOBRANCH");
		branchRange = new ArrayList();
		branchRange.add(0, fromBranch);
		branchRange.add(1, toBranch);
		extractPath = getBPRereshProperty("EXTRACTPATH");
		custCategory = getBPRereshProperty("CUST-LIM-CATEGORY");
		branchCategory = getBPRereshProperty("BRCH-LIM-CATEGORY");
		// PRJ511125-Start
		CustRefFlag = getBPRereshProperty("CUSTOMER-REFRESH");
		AccRefFlag = getBPRereshProperty("ACCOUNT-REFRESH");
		CustExtRefFlag = getBPRereshProperty("CUSTOMER-EXT-REFRESH");
		StopChequeRefFlag = getBPRereshProperty("STOP-CHEQUE-REFRESH");
		// PRJ511125-END
		// bundlepricing-start
		AccBundleFlag = getBPRereshProperty("ACCOUNT-BUNDLE-REFRESH");
		TransBundleFlag = getBPRereshProperty("TRANS-BUNDLE-REFRESH");

		Date hdrDate = null; // date format = 2006-04-09
		try {
			hdrDate = new SimpleDateFormat("yyyy-MM-dd")
					.parse(SystemInformationManager.getInstance().getBFBusinessDate().toString());
		} catch (ParseException pExcpn) {
			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { pExcpn.getLocalizedMessage() }, new HashMap(), env);
		}
		strHdrDt = new SimpleDateFormat("yyyyMMdd").format(hdrDate);
		// Load Currncy details
		List currencyDetails = null;
		currencyDetails = env.getFactory().executeGenericQuery(queryCurrency, null, null);
		SimplePersistentObject currencyPO = null;
		for (int i = 0; i < currencyDetails.size(); i++) {
			currencyPO = (SimplePersistentObject) currencyDetails.get(i);
			CurrencyHash.put(currencyPO.getDataMap().get(IBOCurrency.ISOCURRENCYCODE),
					currencyPO.getDataMap().get(IBOCurrency.CURRENCYSCALE));
		}

		// Deepa
		Iterator Branch = null;

		pagingData.setCurrentPageNumber(pageToProcess);

		{
			try {

				ArrayList list = new ArrayList();
				list.add(fromBranch);
				list.add(toBranch);

				List branchList = BankFusionThreadLocal.getPersistanceFactory().findByQuery(IBOBranch.BONAME,
						BranchPowerWhereClause, list, pagingData, false);
				Branch = branchList.iterator();
				BankFusionThreadLocal.setCurrentPageRecordIDs(branchList);

			} catch (BankFusionException exception) {
				logger.error("Serious Error in processing Page Number: " + pageToProcess);
				return accumulator;
			}
		}

		while (Branch.hasNext()) {

			String branchCode = CommonConstants.EMPTY_STRING;
			try {

				IBOBranch branch = (IBOBranch) Branch.next();
				branchCode = branch.getBoID();
				// Set current record id in the thread local
				BankFusionThreadLocal.setCurrentRecordID(branchCode);

				if ((branchCode != null)) {
					// Deepa

					fileRefreshProp = (Properties) additionalParameters[1];

					if (CustRefFlag.equals("1")) {
						refreshCustomer(env);

					}

					if (AccRefFlag.equals("1")) {
						populateAcc(env);
						refreshAccount(env);

					}

					if (CustExtRefFlag.equals("1")) {
						populateLimits(env);

						refreshLimit(env);

					}

					if (StopChequeRefFlag.equals("1")) {
						refreshStoppedCheques(env);

					}

					// bundlepricing-start
					if (AccBundleFlag.equals("1")) {
						refreshAccountBundle(env);

					}

					if (TransBundleFlag.equals("1")) {
						refreshTransBundle(env);

					}

					// bundlepricing-end

				}
				// BPR
				// setF_OUT_Batch_Status(new Boolean (true));
				// BPR
				else {
					logger.debug(
							"BranchPowerRefresh cannot be processed as branch does not belong to specified branchRange");
				}

			}

			catch (BankFusionException exception) {
				logger.error(exception.getStackTrace());
			}
		}

		return accumulator;

	}

	/**
	 * populate customer limits for limits refresh
	 * 
	 * @param env
	 * @
	 */
	private synchronized void populateLimits(BankFusionEnvironment env) {
		List custlimDetails = null;
		custlimDetails = env.getFactory().executeGenericQuery(queryCustLimit, branchRange, null);
		// Bug#7222
		if (custlimDetails.size() == 0)
			return;
		// Bug#7222
		SimplePersistentObject custPO = null;
		Hashtable tmpCustHash = new Hashtable();
		for (int i = 0; i < custlimDetails.size(); i++) {
			custPO = (SimplePersistentObject) custlimDetails.get(i);
			tmpCustHash.put(custPO.getDataMap().get(IBOCustomer.CUSTOMERCODE),
					custPO.getDataMap().get(IBOBranch.BMBRANCH));
		}
		List limitDetails = null;
		ArrayList cusLimit = new ArrayList();
		cusLimit.add(0, custCategory);
		limitDetails = env.getFactory().executeGenericQuery(queryLimit, cusLimit, null);
		SimplePersistentObject cuslPO = null;
		mcflData mcfl = null;
		String cust = CommonConstants.EMPTY_STRING;
		int limitCtr = 0;
		String tempCustLimitRef = CommonConstants.EMPTY_STRING;
		for (int i = 0; i < limitDetails.size(); i++) {
			cuslPO = (SimplePersistentObject) limitDetails.get(i);
			tempCustLimitRef = (String) cuslPO.getDataMap().get(IBOLimitDetails.LIMITREF);
			if (!tmpCustHash.containsKey(tempCustLimitRef)) {
				continue;
			}
			if (!cust.equalsIgnoreCase((String) cuslPO.getDataMap().get(IBOLimitDetails.LIMITREF))) {
				limitCtr = 0;
				cust = CommonConstants.EMPTY_STRING;
			}
			if (limitCtr == 0) {
				mcfl = new mcflData();
				mcfl.FL_DT2_RECORD_TYPE = "03";
				if (cuslPO.getDataMap().get(IBOLimitDetails.LIMITREF).toString().length() > 9) {
					mcfl.FL_DT2_CLT_NUMBER = cuslPO.getDataMap().get(IBOLimitDetails.LIMITREF).toString().substring(0,
							8);
				} else {
					mcfl.FL_DT2_CLT_NUMBER = (String) cuslPO.getDataMap().get(IBOLimitDetails.LIMITREF);
				}
				mcfl.FL_DT2_CLT_LIMIT_CURR = (String) cuslPO.getDataMap().get(IBOLimitDetails.CURRENCY);
				mcfl.FL_DT2_LIMIT_CHK_FLAG = cuslPO.getDataMap().get(IBOLimit.LIMITINDICATOR).toString();
				mcfl.FL_DT2_LMT_AMT1 = cuslPO.getDataMap().get(IBOLimitDetails.LIMIT).toString();
				mcfl.FL_DT2_EXP_AMT1 = cuslPO.getDataMap().get(IBOLimitDetails.EXPOSURE).toString();
				mcfl.FL_DT2_BRANCH_CODE = (String) tmpCustHash.get(cuslPO.getDataMap().get(IBOLimitDetails.LIMITREF));
				limitHash.put(mcfl.FL_DT2_BRANCH_CODE + mcfl.FL_DT2_CLT_NUMBER, mcfl);
				cust = mcfl.FL_DT2_CLT_NUMBER;
				limitCtr++;
			} else if (limitCtr == 1) {
				if (cuslPO.getDataMap().get(IBOLimitDetails.LIMITREF).toString().length() > 9) {
					mcfl.FL_DT2_CLT_NUMBER = cuslPO.getDataMap().get(IBOLimitDetails.LIMITREF).toString().substring(0,
							8);
				} else {
					mcfl.FL_DT2_CLT_NUMBER = (String) cuslPO.getDataMap().get(IBOLimitDetails.LIMITREF);
				}
				mcfl.FL_DT2_BRANCH_CODE = (String) tmpCustHash.get(cuslPO.getDataMap().get(IBOLimitDetails.LIMITREF));
				mcfl.FL_DT2_LMT_AMT2 = cuslPO.getDataMap().get(IBOLimitDetails.LIMIT).toString();
				mcfl.FL_DT2_EXP_AMT2 = cuslPO.getDataMap().get(IBOLimitDetails.EXPOSURE).toString();
				limitHash.put(mcfl.FL_DT2_BRANCH_CODE + mcfl.FL_DT2_CLT_NUMBER, mcfl);
				cust = mcfl.FL_DT2_CLT_NUMBER;
				limitCtr++;
			} else if (limitCtr == 2) {
				mcfl.FL_DT2_LMT_AMT3 = cuslPO.getDataMap().get(IBOLimitDetails.LIMIT).toString();
				mcfl.FL_DT2_EXP_AMT3 = cuslPO.getDataMap().get(IBOLimitDetails.EXPOSURE).toString();
				limitHash.put(mcfl.FL_DT2_BRANCH_CODE + mcfl.FL_DT2_CLT_NUMBER, mcfl);
				cust = mcfl.FL_DT2_CLT_NUMBER;
				limitCtr++;
			} else if (limitCtr == 3) {
				mcfl.FL_DT2_LMT_AMT4 = cuslPO.getDataMap().get(IBOLimitDetails.LIMIT).toString();
				mcfl.FL_DT2_EXP_AMT4 = cuslPO.getDataMap().get(IBOLimitDetails.EXPOSURE).toString();
				limitHash.put(mcfl.FL_DT2_BRANCH_CODE + mcfl.FL_DT2_CLT_NUMBER, mcfl);
				cust = mcfl.FL_DT2_CLT_NUMBER;
				limitCtr++;
			} else if (limitCtr == 4) {
				mcfl.FL_DT2_LMT_AMT5 = cuslPO.getDataMap().get(IBOLimitDetails.LIMIT).toString();
				mcfl.FL_DT2_EXP_AMT5 = cuslPO.getDataMap().get(IBOLimitDetails.EXPOSURE).toString();
				limitHash.put(mcfl.FL_DT2_BRANCH_CODE + mcfl.FL_DT2_CLT_NUMBER, mcfl);
				cust = mcfl.FL_DT2_CLT_NUMBER;
				limitCtr++;
			} else if (limitCtr == 5) {
				mcfl.FL_DT2_LMT_AMT6 = cuslPO.getDataMap().get(IBOLimitDetails.LIMIT).toString();
				mcfl.FL_DT2_EXP_AMT6 = cuslPO.getDataMap().get(IBOLimitDetails.EXPOSURE).toString();
				limitHash.put(mcfl.FL_DT2_BRANCH_CODE + mcfl.FL_DT2_CLT_NUMBER, mcfl);
				cust = mcfl.FL_DT2_CLT_NUMBER;
				limitCtr++;
			} else if (limitCtr == 6) {
				mcfl.FL_DT2_LMT_AMT7 = cuslPO.getDataMap().get(IBOLimitDetails.LIMIT).toString();
				mcfl.FL_DT2_EXP_AMT7 = cuslPO.getDataMap().get(IBOLimitDetails.EXPOSURE).toString();
				limitHash.put(mcfl.FL_DT2_BRANCH_CODE + mcfl.FL_DT2_CLT_NUMBER, mcfl);
				cust = mcfl.FL_DT2_CLT_NUMBER;
				limitCtr++;
			} else if (limitCtr == 7) {
				mcfl.FL_DT2_LMT_AMT8 = cuslPO.getDataMap().get(IBOLimitDetails.LIMIT).toString();
				mcfl.FL_DT2_EXP_AMT8 = cuslPO.getDataMap().get(IBOLimitDetails.EXPOSURE).toString();
				limitHash.put(mcfl.FL_DT2_BRANCH_CODE + mcfl.FL_DT2_CLT_NUMBER, mcfl);
				cust = mcfl.FL_DT2_CLT_NUMBER;
				limitCtr++;
			} else if (limitCtr == 8) {
				mcfl.FL_DT2_LMT_AMT9 = cuslPO.getDataMap().get(IBOLimitDetails.LIMIT).toString();
				mcfl.FL_DT2_EXP_AMT9 = cuslPO.getDataMap().get(IBOLimitDetails.EXPOSURE).toString();
				limitHash.put(mcfl.FL_DT2_BRANCH_CODE + mcfl.FL_DT2_CLT_NUMBER, mcfl);
				cust = mcfl.FL_DT2_CLT_NUMBER;
				limitCtr++;
			} else if (limitCtr == 9) {
				mcfl.FL_DT2_LMT_AMT10 = cuslPO.getDataMap().get(IBOLimitDetails.LIMIT).toString();
				mcfl.FL_DT2_EXP_AMT10 = cuslPO.getDataMap().get(IBOLimitDetails.EXPOSURE).toString();
				limitHash.put(mcfl.FL_DT2_BRANCH_CODE + mcfl.FL_DT2_CLT_NUMBER, mcfl);
				cust = mcfl.FL_DT2_CLT_NUMBER;
				limitCtr++;
			}
		}
	}

	/**
	 * Method to perform customer limits refresh
	 * 
	 * @param env
	 * @
	 */
	private synchronized void refreshLimit(BankFusionEnvironment env) {
		FileOutputStream fout = null;

		int Branchctr = 0;
		try {
			String Branch = CommonConstants.EMPTY_STRING;
			boolean notfirstTime = false;
			// Bug#7222
			if (limitHash.size() == 0)
				return;
			// Bug#7222
			Iterator itr = limitHash.keySet().iterator();

			while (itr.hasNext()) {
				String key = (String) itr.next();
				mcflData mcfl = (mcflData) limitHash.get(key);
				if (mcfl.FL_DT2_BRANCH_CODE == null) {
					break;
				}

				if (!Branch.equalsIgnoreCase(mcfl.FL_DT2_BRANCH_CODE)) {
					if (notfirstTime) {
						Branchctr++;
						formatLimitTrail(String.valueOf(Branchctr), fout, env);
						Branchctr = 0;
					}
					fout = new FileOutputStream(extractPath + "mcfl" + mcfl.FL_DT2_BRANCH_CODE + ".dat");
					formatLimitHeader(mcfl.FL_DT2_BRANCH_CODE, fout, env);
					Branch = (String) mcfl.FL_DT2_BRANCH_CODE;
					formatLimitBranch(Branch, fout, env);
					notfirstTime = true;
				}
				fileData = new StringBuffer();
				fileData.append(setField(new Integer(getRereshProperty("FL-DT2-RECORD-TYPE")).intValue(),
						mcfl.FL_DT2_RECORD_TYPE, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FL-DT2-CLT-NUMBER")).intValue(),
						mcfl.FL_DT2_CLT_NUMBER, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FL-DT2-CLT-LIMIT-CURR")).intValue(),
						mcfl.FL_DT2_CLT_LIMIT_CURR, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FL-DT2-LIMIT-CHK-FLAG")).intValue(),
						mcfl.FL_DT2_LIMIT_CHK_FLAG, 'A'));

				if (mcfl.FL_DT2_LMT_AMT1 != CommonConstants.EMPTY_STRING) {
					setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT1), "FL-DT2-LMT-AMT1", fileData, 0);
				} else {
					mcfl.FL_DT2_LMT_AMT1 = "000000000000000000";
					setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT1), "FL-DT2-LMT-AMT1", fileData, 0);
				}

				if (mcfl.FL_DT2_EXP_AMT1 != CommonConstants.EMPTY_STRING) {
					setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT1), "FL-DT2-EXP-AMT1", fileData, 0);
				} else {
					mcfl.FL_DT2_EXP_AMT1 = "000000000000000000";
					setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT1), "FL-DT2-EXP-AMT1", fileData, 0);
				}
				if (mcfl.FL_DT2_LMT_AMT2 != CommonConstants.EMPTY_STRING) {
					setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT2), "FL-DT2-LMT-AMT2", fileData, 0);
				} else {
					mcfl.FL_DT2_LMT_AMT2 = "000000000000000000";
					setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT2), "FL-DT2-LMT-AMT2", fileData, 0);
				}
				if (mcfl.FL_DT2_EXP_AMT2 != CommonConstants.EMPTY_STRING) {
					setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT2), "FL-DT2-EXP-AMT2", fileData, 0);
				} else {
					mcfl.FL_DT2_EXP_AMT2 = "000000000000000000";
					setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT2), "FL-DT2-EXP-AMT2", fileData, 0);
				}
				if (mcfl.FL_DT2_LMT_AMT3 != CommonConstants.EMPTY_STRING) {
					setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT3), "FL-DT2-LMT-AMT3", fileData, 0);
				} else {
					mcfl.FL_DT2_LMT_AMT3 = "000000000000000000";
					setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT3), "FL-DT2-LMT-AMT3", fileData, 0);
				}
				if (mcfl.FL_DT2_EXP_AMT3 != CommonConstants.EMPTY_STRING) {
					setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT3), "FL-DT2-EXP-AMT3", fileData, 0);
				} else {
					mcfl.FL_DT2_EXP_AMT3 = "000000000000000000";
					setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT3), "FL-DT2-EXP-AMT3", fileData, 0);
				}
				if (mcfl.FL_DT2_LMT_AMT4 != CommonConstants.EMPTY_STRING) {
					setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT4), "FL-DT2-LMT-AMT4", fileData, 0);
				} else {
					mcfl.FL_DT2_LMT_AMT4 = "000000000000000000";
					setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT4), "FL-DT2-LMT-AMT4", fileData, 0);
				}
				if (mcfl.FL_DT2_EXP_AMT4 != CommonConstants.EMPTY_STRING) {
					setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT4), "FL-DT2-EXP-AMT4", fileData, 0);
				} else {
					mcfl.FL_DT2_EXP_AMT4 = "000000000000000000";
					setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT4), "FL-DT2-EXP-AMT4", fileData, 0);
				}
				if (mcfl.FL_DT2_LMT_AMT5 != CommonConstants.EMPTY_STRING) {
					setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT5), "FL-DT2-LMT-AMT5", fileData, 0);
				} else {
					mcfl.FL_DT2_LMT_AMT5 = "000000000000000000";
					setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT5), "FL-DT2-LMT-AMT5", fileData, 0);
				}
				if (mcfl.FL_DT2_EXP_AMT5 != CommonConstants.EMPTY_STRING) {
					setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT5), "FL-DT2-EXP-AMT5", fileData, 0);
				} else {
					mcfl.FL_DT2_EXP_AMT5 = "000000000000000000";
					setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT5), "FL-DT2-EXP-AMT5", fileData, 0);
				}
				if (mcfl.FL_DT2_LMT_AMT6 != CommonConstants.EMPTY_STRING) {
					setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT6), "FL-DT2-LMT-AMT6", fileData, 0);
				} else {
					mcfl.FL_DT2_LMT_AMT6 = "000000000000000000";
					setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT6), "FL-DT2-LMT-AMT6", fileData, 0);
				}
				if (mcfl.FL_DT2_EXP_AMT6 != CommonConstants.EMPTY_STRING) {
					setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT6), "FL-DT2-EXP-AMT6", fileData, 0);
				} else {
					mcfl.FL_DT2_EXP_AMT6 = "000000000000000000";
					setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT6), "FL-DT2-EXP-AMT6", fileData, 0);
				}
				if (mcfl.FL_DT2_LMT_AMT7 != CommonConstants.EMPTY_STRING) {
					setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT7), "FL-DT2-LMT-AMT7", fileData, 0);
				} else {
					mcfl.FL_DT2_LMT_AMT7 = "000000000000000000";
					setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT7), "FL-DT2-LMT-AMT7", fileData, 0);
				}
				if (mcfl.FL_DT2_EXP_AMT7 != CommonConstants.EMPTY_STRING) {
					setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT7), "FL-DT2-EXP-AMT7", fileData, 0);
				} else {
					mcfl.FL_DT2_EXP_AMT7 = "000000000000000000";
					setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT7), "FL-DT2-EXP-AMT7", fileData, 0);
				}
				if (mcfl.FL_DT2_LMT_AMT8 != CommonConstants.EMPTY_STRING) {
					setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT8), "FL-DT2-LMT-AMT8", fileData, 0);
				} else {
					mcfl.FL_DT2_LMT_AMT8 = "000000000000000000";
					setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT8), "FL-DT2-LMT-AMT8", fileData, 0);
				}
				if (mcfl.FL_DT2_EXP_AMT8 != CommonConstants.EMPTY_STRING) {
					setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT8), "FL-DT2-EXP-AMT8", fileData, 0);
				} else {
					mcfl.FL_DT2_EXP_AMT8 = "000000000000000000";
					setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT8), "FL-DT2-EXP-AMT8", fileData, 0);
				}
				if (mcfl.FL_DT2_LMT_AMT9 != CommonConstants.EMPTY_STRING) {
					setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT9), "FL-DT2-LMT-AMT9", fileData, 0);
				} else {
					mcfl.FL_DT2_LMT_AMT9 = "000000000000000000";
					setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT9), "FL-DT2-LMT-AMT9", fileData, 0);
				}
				if (mcfl.FL_DT2_EXP_AMT9 != CommonConstants.EMPTY_STRING) {
					setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT9), "FL-DT2-EXP-AMT9", fileData, 0);
				} else {
					mcfl.FL_DT2_EXP_AMT9 = "000000000000000000";
					setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT9), "FL-DT2-EXP-AMT9", fileData, 0);
				}
				if (mcfl.FL_DT2_LMT_AMT10 != CommonConstants.EMPTY_STRING) {
					setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT10), "FL-DT2-LMT-AMT10", fileData, 0);
				} else {
					mcfl.FL_DT2_LMT_AMT10 = "000000000000000000";
					setAmount(new BigDecimal(mcfl.FL_DT2_LMT_AMT10), "FL-DT2-LMT-AMT10", fileData, 0);
				}
				if (mcfl.FL_DT2_EXP_AMT10 != CommonConstants.EMPTY_STRING) {
					setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT10), "FL-DT2-EXP-AMT10", fileData, 0);
				} else {
					mcfl.FL_DT2_EXP_AMT10 = "000000000000000000";
					setAmount(new BigDecimal(mcfl.FL_DT2_EXP_AMT10), "FL-DT2-EXP-AMT10", fileData, 0);
				}

				fileData.append(setField(new Integer(getRereshProperty("FL-DT2-CLT-LMT-INFO")).intValue(),
						mcfl.FL_DT2_CLT_LMT_INFO, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FL-DT2-FILLER1")).intValue(),
						mcfl.FL_DT2_FILLER1, 'A'));
				if (mcfl.FL_DT2_BRANCH_CODE != null) {
					fileData.append(setField(new Integer(getRereshProperty("FL-DT2-BRANCH-CODE")).intValue(),
							mcfl.FL_DT2_BRANCH_CODE, 'A'));
				} else {
					break;
				}

				fileData.append(
						setField(new Integer(getRereshProperty("FL-DT2-ACTION")).intValue(), mcfl.FL_DT2_ACTION, 'A'));
				BigDecimal checksum = new BigDecimal(0).setScale(0);
				// ADD ONLY ABSOLUTE VALUES TO DERIVE THE CHECKSUM
				// .add(new
				// BigDecimal(mcfdet.FA_DTL_DEBIT_LIMIT).movePointRight(Integer.parseInt(scale)).abs())

				checksum = (new BigDecimal(mcfl.FL_DT2_LMT_AMT1).abs()).add(new BigDecimal(mcfl.FL_DT2_LMT_AMT2).abs())
						.add(new BigDecimal(mcfl.FL_DT2_LMT_AMT3).abs()).add(new BigDecimal(mcfl.FL_DT2_LMT_AMT4).abs())
						.add(new BigDecimal(mcfl.FL_DT2_LMT_AMT5).abs()).add(new BigDecimal(mcfl.FL_DT2_LMT_AMT6).abs())
						.add(new BigDecimal(mcfl.FL_DT2_LMT_AMT7).abs()).add(new BigDecimal(mcfl.FL_DT2_LMT_AMT8).abs())
						.add(new BigDecimal(mcfl.FL_DT2_LMT_AMT9).abs())
						.add(new BigDecimal(mcfl.FL_DT2_LMT_AMT10).abs())
						.add(new BigDecimal(mcfl.FL_DT2_EXP_AMT1).abs()).add(new BigDecimal(mcfl.FL_DT2_EXP_AMT2).abs())
						.add(new BigDecimal(mcfl.FL_DT2_EXP_AMT3).abs()).add(new BigDecimal(mcfl.FL_DT2_EXP_AMT4).abs())
						.add(new BigDecimal(mcfl.FL_DT2_EXP_AMT5).abs()).add(new BigDecimal(mcfl.FL_DT2_EXP_AMT6).abs())
						.add(new BigDecimal(mcfl.FL_DT2_EXP_AMT7).abs()).add(new BigDecimal(mcfl.FL_DT2_EXP_AMT8).abs())
						.add(new BigDecimal(mcfl.FL_DT2_EXP_AMT9).abs())
						.add(new BigDecimal(mcfl.FL_DT2_EXP_AMT10).abs())
						.add(new BigDecimal(mcfl.FL_DT2_BRANCH_CODE).abs());
				BigInteger cksum = checksum.toBigInteger().abs();
				fileData.append(
						setField(new Integer(getRereshProperty("FL-DT2-CHECKSUM")).intValue(), cksum.toString(), 'N'));
				fileData.append(setField(new Integer(getRereshProperty("FL-DT2-FILLER2")).intValue(),
						mcfl.FL_DT2_FILLER2, 'A'));
				Branchctr++;
				fileData.append("\r\n");
				fout.write(fileData.toString().getBytes());
				fout.flush();
				Branch = mcfl.FL_DT2_BRANCH_CODE;
			}
			Branchctr++;
			formatLimitTrail(String.valueOf(Branchctr), fout, env);
			if (fout != null) {
				fout.close();
			}
		} catch (FileNotFoundException fnfExcpn) {

			BranchPowerRefreshFatomContext.Status = Boolean.FALSE;

			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { fnfExcpn.getLocalizedMessage() }, new HashMap(), env);
		} catch (IOException ioExcpn) {
			// BPR

			BranchPowerRefreshFatomContext.Status = Boolean.FALSE;

			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
		}
		// BPR
		catch (Exception Excpn) {

			BranchPowerRefreshFatomContext.Status = Boolean.FALSE;
			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { Excpn.getLocalizedMessage() }, new HashMap(), env);

		}
		// BPR

	}

	/**
	 * formats Customer branch limit record
	 * 
	 * @param Branch
	 *            takes Branch as input to fetch,format the branch record
	 * @param fout
	 */
	private synchronized void formatLimitBranch(String Branch, FileOutputStream fout, BankFusionEnvironment env) {

		Integer scale1 = (Integer) (CurrencyHash.get(SystemInformationManager.getInstance().getBaseCurrencyCode()));
		int scale = scale1.intValue();

		fileData.append(setField(new Integer(getRereshProperty("FL-DT1-RECORD-TYPE")).intValue(), "02", 'A'));
		setAmount(new BigDecimal("0"), "FL-DT1-BRCH-NON-ZERO-1", fileData, scale);
		setAmount(new BigDecimal("0"), "FL-DT1-BRCH-NON-ZERO-2", fileData, scale);
		setAmount(new BigDecimal("0"), "FL-DT1-BRCH-ZERO-LMT-1", fileData, scale);
		setAmount(new BigDecimal("0"), "FL-DT1-BRCH-ZERO-LMT-2", fileData, scale);
		setAmount(new BigDecimal("0"), "FL-DT1-BRCH-PERCENT", fileData, scale);
		setAmount(new BigDecimal("0"), "FL-DT1-BRCH-PER-FILLER", fileData, scale);
		setAmount(new BigDecimal("0"), "FL-DT1-SYS-LIM-BAL", fileData, scale);
		setAmount(new BigDecimal("0"), "FL-DT1-SYS-LIM-EXP-CHK", fileData, scale);
		setAmount(new BigDecimal("0"), "FL-DT1-SYS-CUST-EXT-LMTS", fileData, scale);
		fileData.append(setField(new Integer(getRereshProperty("FL-DT1-FILLER1")).intValue(),
				CommonConstants.EMPTY_STRING, 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FL-DT1-BRANCH-CODE")).intValue(), Branch, 'N'));
		fileData.append(setField(new Integer(getRereshProperty("FL-DT1-ACTION")).intValue(), "A", 'A'));
		// COMPUTE WS01-CLT-LMT-BRN-CHECKSUM =
		// WS01-CLTLMT-NONZERO-LIMIT +
		// WS01-CLTLMT-ZERO-LIMIT +
		// WS01-CLTLMT-PERCENT-EX +
		// WL01-CLT-LMT-BRN-USE-CL-BK-BAL +
		// WL01-CLT-LMT-BRN-EXP-DATE-IND +
		// WL01-CLT-LMT-BRN-EXT-LIMIT-IND +
		// WL01-CLT-LMT-BRN-BRANCH.

		fileData.append(setField(new Integer(getRereshProperty("FL-DT1-CHECKSUM")).intValue(), Branch, 'N'));
		fileData.append(setField(new Integer(getRereshProperty("FL-DT1-FILLER2")).intValue(),
				CommonConstants.EMPTY_STRING, 'A'));
		fileData.append("\r\n");
		try {
			fout.write(fileData.toString().getBytes());
			fout.flush();
		} catch (IOException ioExcpn) {
			// throw new BankFusionException(127, new Object[] {
			// ioExcpn.getLocalizedMessage() }, logger, env);
			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
		}
	}

	/**
	 * formats and writes the Customer Limit Header record for the passed branch
	 * parameter
	 * 
	 * @param Branch
	 * @param fout
	 */
	private void formatLimitHeader(String Branch, FileOutputStream fout, BankFusionEnvironment env) {
		StringBuffer fileData = new StringBuffer();
		fileData.append(setField(new Integer(getRereshProperty("FL-HDR-RECORD-TYPE")).intValue(), "01", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FL-HDR-ACTION-FLAG")).intValue(), "R", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FL-HDR-SOURCE-SYSTEM")).intValue(), "MCAS", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FL-HDR-DEST-SYSTEM")).intValue(), "BPWR", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FL-HDR-BRANCH-CODE")).intValue(), Branch, 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FL-HDR-FILE-ID")).intValue(), "LM", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FL-HDR-PROCESS-DATE")).intValue(), strHdrDt, 'N'));
		fileData.append(setField(new Integer(getRereshProperty("FL-HDR-FILLER")).intValue(),
				CommonConstants.EMPTY_STRING, 'A'));
		fileData.append("\r\n");
		try {
			fout.write(fileData.toString().getBytes());
			fout.flush();
		} catch (IOException ioExcpn) {
			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
		}
	}

	/**
	 * populates the account object with fetched values
	 * 
	 * @param env
	 * @
	 */
	private synchronized void populateAcc(BankFusionEnvironment env) {
		List accountDetails = null;
		List creditInterestDetails = null;
		List debitInterestDetails = null;

		accountDetails = env.getFactory().executeGenericQuery(queryAccount, branchRange, null);
		creditInterestDetails = env.getFactory().executeGenericQuery(queryCreditInterest, null, null);
		debitInterestDetails = env.getFactory().executeGenericQuery(queryDebitInterest, null, null);

		SimplePersistentObject accPO = null;
		// Bug#7222
		if (accountDetails.size() == 0)
			return;
		// Bug#7222
		for (int i = 0; i < accountDetails.size(); i++) {
			accPO = (SimplePersistentObject) accountDetails.get(i);
			int accLength = accPO.getDataMap().get(IBOAccount.ACCOUNTID).toString().length();
			if (accLength > 10) {
				mcfaData mcfa = new mcfaData();
				mcfa.FA_DTL_REC_TYPE = "02";
				mcfa.FA_DTL_ACC_ID = (String) accPO.getDataMap().get(IBOAccount.ACCOUNTID);
				if (accPO.getDataMap().get(IBOAccount.ACCOUNTNAME).toString().length() > 30)
					mcfa.FA_DTL_SHORTNAME1 = accPO.getDataMap().get(IBOAccount.ACCOUNTNAME).toString().substring(0, 29);
				else
					mcfa.FA_DTL_SHORTNAME1 = (String) accPO.getDataMap().get(IBOAccount.ACCOUNTNAME);

				mcfa.FA_DTL_LED_SUBLED = mcfa.FA_DTL_ACC_ID.substring(2, 5);
				mcfa.FA_DTL_CURR = (String) accPO.getDataMap().get(IBOAccount.ISOCURRENCYCODE);
				/*
				 * PRJ51125-START if (mcfa.FA_DTL_ACC_ID.length() == 13){
				 * mcfa.FA_DTL_CLIENT_NUMBER=mcfa.FA_DTL_ACC_ID.substring(5,11);} else{
				 * mcfa.FA_DTL_CLIENT_NUMBER=mcfa.FA_DTL_ACC_ID.substring(5,12); PRJ511125-END
				 */
				if (accPO.getDataMap().get(IBOAccPortMap.PORTFOLIOID).toString().length() > 9)
					mcfa.FA_DTL_CLIENT_NUMBER = accPO.getDataMap().get(IBOAccPortMap.PORTFOLIOID).toString()
							.substring(0, 8);
				else
					mcfa.FA_DTL_CLIENT_NUMBER = (String) accPO.getDataMap().get(IBOAccPortMap.PORTFOLIOID);

				mcfa.FA_DTL_STOP_BLK_IND = "0";
				if (accPO.getDataMap().get(IBOAccount.CLEAREDBALANCE).toString().length() > 18)
					mcfa.FA_DTL_CLEARED_BAL = accPO.getDataMap().get(IBOAccount.CLEAREDBALANCE).toString().substring(0,
							17);
				else
					mcfa.FA_DTL_CLEARED_BAL = accPO.getDataMap().get(IBOAccount.CLEAREDBALANCE).toString();

				if (accPO.getDataMap().get(IBOAccount.BOOKEDBALANCE).toString().length() > 18)
					mcfa.FA_DTL_BOOK_BAL = accPO.getDataMap().get(IBOAccount.BOOKEDBALANCE).toString().substring(0, 17);
				else
					mcfa.FA_DTL_BOOK_BAL = accPO.getDataMap().get(IBOAccount.BOOKEDBALANCE).toString();

				mcfa.FA_DTL_ACCRUED_INT = "0";
				mcfa.FA_DTL_ALT_IDENTIFIER = (String) accPO.getDataMap().get(IBOAccount.PSEUDONAME);
				mcfa.FA_DTL_LIMIT_CHK_FLAG = CommonConstants.EMPTY_STRING;
				/*
				 * PRJ511125-START mcfa.FA_DTL_LIMIT_NUMB_1 =(String)
				 * accPO.getDataMap().get(IBOAccount.LIMITREF1); mcfa.FA_DTL_LIMIT_NUMB_2
				 * =(String) accPO.getDataMap().get(IBOAccount.LIMITREF2);
				 * mcfa.FA_DTL_LIMIT_NUMB_3 =(String)
				 * accPO.getDataMap().get(IBOAccount.LIMITREF3); mcfa.FA_DTL_LIMIT_NUMB_4
				 * =(String) accPO.getDataMap().get(IBOAccount.LIMITREF4);
				 * mcfa.FA_DTL_LIMIT_NUMB_5 =(String)
				 * accPO.getDataMap().get(IBOAccount.LIMITREF5);
				 */
				if (accPO.getDataMap().get(IBOProductInheritance.LIM_LIMITREF1).toString().length() > 2)
					mcfa.FA_DTL_LIMIT_NUMB_1 = accPO.getDataMap().get(IBOProductInheritance.LIM_LIMITREF1).toString()
							.substring(0, 2);
				else
					mcfa.FA_DTL_LIMIT_NUMB_1 = (String) accPO.getDataMap().get(IBOProductInheritance.LIM_LIMITREF1);

				if (accPO.getDataMap().get(IBOProductInheritance.LIM_LIMITREF2).toString().length() > 2)
					mcfa.FA_DTL_LIMIT_NUMB_2 = accPO.getDataMap().get(IBOProductInheritance.LIM_LIMITREF2).toString()
							.substring(0, 2);
				else
					mcfa.FA_DTL_LIMIT_NUMB_2 = (String) accPO.getDataMap().get(IBOProductInheritance.LIM_LIMITREF2);

				if (accPO.getDataMap().get(IBOProductInheritance.LIM_LIMITREF3).toString().length() > 2)
					mcfa.FA_DTL_LIMIT_NUMB_3 = accPO.getDataMap().get(IBOProductInheritance.LIM_LIMITREF3).toString()
							.substring(0, 2);
				else
					mcfa.FA_DTL_LIMIT_NUMB_3 = (String) accPO.getDataMap().get(IBOProductInheritance.LIM_LIMITREF3);

				if (accPO.getDataMap().get(IBOProductInheritance.LIM_LIMITREF4).toString().length() > 2)
					mcfa.FA_DTL_LIMIT_NUMB_4 = accPO.getDataMap().get(IBOProductInheritance.LIM_LIMITREF4).toString()
							.substring(0, 2);
				else
					mcfa.FA_DTL_LIMIT_NUMB_4 = (String) accPO.getDataMap().get(IBOProductInheritance.LIM_LIMITREF4);

				if (accPO.getDataMap().get(IBOProductInheritance.LIM_LIMITREF5).toString().length() > 2)
					mcfa.FA_DTL_LIMIT_NUMB_5 = accPO.getDataMap().get(IBOProductInheritance.LIM_LIMITREF5).toString()
							.substring(0, 2);
				else
					mcfa.FA_DTL_LIMIT_NUMB_5 = (String) accPO.getDataMap().get(IBOProductInheritance.LIM_LIMITREF5);

				if (accPO.getDataMap().get(IBOAccount.CHEQUEDEPOSITBALANCE).toString().length() > 18)
					mcfa.FA_DTL_SUM_CHQ_DEP_BAL = accPO.getDataMap().get(IBOAccount.CHEQUEDEPOSITBALANCE).toString()
							.substring(0, 17);
				else
					mcfa.FA_DTL_SUM_CHQ_DEP_BAL = accPO.getDataMap().get(IBOAccount.CHEQUEDEPOSITBALANCE).toString();

				if (accPO.getDataMap().get(IBOAccount.ACCOUNTDESCRIPTION).toString().length() > 30)
					mcfa.FA_DTL_ACC_TYPE_DESC = accPO.getDataMap().get(IBOAccount.ACCOUNTDESCRIPTION).toString()
							.substring(0, 29);
				else
					mcfa.FA_DTL_ACC_TYPE_DESC = (String) accPO.getDataMap().get(IBOAccount.ACCOUNTDESCRIPTION);

				mcfa.FA_DTL_ACC_TYPE = accPO.getDataMap().get(IBOProductInheritance.PRODUCT_NUMERICCODE).toString();

				if (accPO.getDataMap().get(IBOAccount.DEBITLIMIT).toString().length() > 18)
					mcfa.FA_DTL_DEBIT_LIMIT = accPO.getDataMap().get(IBOAccount.DEBITLIMIT).toString().substring(0, 17);
				else
					mcfa.FA_DTL_DEBIT_LIMIT = accPO.getDataMap().get(IBOAccount.DEBITLIMIT).toString();

				mcfa.FA_DTL_DEBIT_LIMIT_EXP = CommonConstants.EMPTY_STRING;

				if (accPO.getDataMap().get(IBOAccount.CREDITLIMIT).toString().length() > 18)
					mcfa.FA_DTL_CREDIT_LIMIT = accPO.getDataMap().get(IBOAccount.CREDITLIMIT).toString().substring(0,
							17);
				else
					mcfa.FA_DTL_CREDIT_LIMIT = accPO.getDataMap().get(IBOAccount.CREDITLIMIT).toString();

				mcfa.FA_DTL_CREDIT_LIMIT_EXP = CommonConstants.EMPTY_STRING;
				mcfa.FA_DTL_PASS_PROT_FLAG = accPO.getDataMap().get(IBOAccount.ACCRIGHTSINDICATOR).toString();
				mcfa.FA_DTL_LIMIT_CHK_FLAG = accPO.getDataMap().get(IBOAccount.LIMITINDICATOR).toString();

				Date dtTmp = null; // date format = 2006-04-09 20:17:55449
				try {
					String dtStr = accPO.getDataMap().get(IBOAccount.LASTTRANSACTIONDATE).toString();
					SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
					dtTmp = sf.parse(dtStr);
					String strLastTrnDt = new SimpleDateFormat("yyyyMMdd").format(dtTmp);
					mcfa.FA_DTL_DATE_LAST_EXT = strLastTrnDt;
				} catch (ParseException pExcpn) {
					/*
					 * throw new BankFusionException(127, new Object[] {
					 * pExcpn.getLocalizedMessage() }, logger, env);
					 */
					EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
							new Object[] { pExcpn.getLocalizedMessage() }, new HashMap(), env);
				}

				// PRJ511125-END
				mcfa.FA_DTL_BRANCH = (String) accPO.getDataMap().get(IBOBranch.BMBRANCH);
				mcfa.FA_DTL_ACTION = "A";
				accHash.put(accPO.getDataMap().get(IBOAccount.BRANCHSORTCODE).toString() + mcfa.FA_DTL_ACC_ID, mcfa);
			}
		}

		BigDecimal accdInt = new BigDecimal("0.00");

		for (int i = 0; i < creditInterestDetails.size(); i++) {
			accPO = (SimplePersistentObject) creditInterestDetails.get(i);
			String CrAc = (String) accPO.getDataMap().get(IBOCreditInterestFeature.ACCOUNTID);
			if (accHash.containsKey((String) accPO.getDataMap().get(IBOAccount.BRANCHSORTCODE) + CrAc)) {
				mcfaData mcfa = (mcfaData) accHash
						.get((String) accPO.getDataMap().get(IBOAccount.BRANCHSORTCODE) + CrAc);
				if (null != accPO.getDataMap().get(IBOCreditInterestFeature.ACCDCRINTEREST)) {
					accdInt.add(
							new BigDecimal(accPO.getDataMap().get(IBOCreditInterestFeature.ACCDCRINTEREST).toString()));
					if (accPO.getDataMap().get(IBOCreditInterestFeature.ACCDCRINTEREST).toString().length() > 18)
						mcfa.FA_DTL_ACCRUED_INT = accPO.getDataMap().get(IBOCreditInterestFeature.ACCDCRINTEREST)
								.toString().substring(0, 17);
					else
						mcfa.FA_DTL_ACCRUED_INT = accPO.getDataMap().get(IBOCreditInterestFeature.ACCDCRINTEREST)
								.toString();
				}
				accHash.put((String) accPO.getDataMap().get(IBOAccount.BRANCHSORTCODE) + CrAc, mcfa);
			}
		}

		for (int i = 0; i < debitInterestDetails.size(); i++) {
			accPO = (SimplePersistentObject) debitInterestDetails.get(i);
			String CrAc = (String) accPO.getDataMap().get(IBODebitInterestFeature.ACCOUNTID);
			if (accHash.containsKey((String) accPO.getDataMap().get(IBOAccount.BRANCHSORTCODE) + CrAc)) {
				mcfaData mcfa = (mcfaData) accHash
						.get((String) accPO.getDataMap().get(IBOAccount.BRANCHSORTCODE) + CrAc);
				if (null != accPO.getDataMap().get(IBODebitInterestFeature.DEBITACCDINTEREST)) {
					accdInt.add(
							new BigDecimal(accPO.getDataMap().get(IBODebitInterestFeature.DEBITACCDINTEREST).toString())
									.negate());
					if (accdInt.toString().length() > 18)
						mcfa.FA_DTL_ACCRUED_INT = accdInt.toString().substring(0, 17);
					else
						mcfa.FA_DTL_ACCRUED_INT = accdInt.toString();
				}
				accHash.put((String) accPO.getDataMap().get(IBOAccount.BRANCHSORTCODE) + CrAc, mcfa);
			}
		}
		/*
		 * PRJ511125-start for (int i = 0; i < AccountLimitDetails.size(); i++) { accPO
		 * = (SimplePersistentObject)AccountLimitDetails.get(i);
		 * 
		 * if
		 * (accHash.containsKey(accPO.getDataMap().get(IBOAccountLimitFeature.ACCOUNTID)
		 * .toString())){ if
		 * (accHash.containsKey(accPO.getDataMap().get(IBOAccountLimitFeature.ACCOUNTID)
		 * )){ String CrAc = (String)
		 * accPO.getDataMap().get(IBOAccountLimitFeature.ACCOUNTID); mcfaData mcfa =
		 * (mcfaData)accHash.get((String)
		 * accPO.getDataMap().get(IBOAccount.BRANCHSORTCODE)+CrAc);
		 * 
		 * mcfa.FA_DTL_DEBIT_LIMIT = (String)
		 * accPO.getDataMap().get(IBOAccountLimitFeature.DEBITLIMIT);
		 * mcfa.FA_DTL_DEBIT_LIMIT_EXP = CommonConstants.EMPTY_STRING;
		 * mcfa.FA_DTL_CREDIT_LIMIT =(String)
		 * accPO.getDataMap().get(IBOAccountLimitFeature.CREDITLIMIT);
		 * mcfa.FA_DTL_CREDIT_LIMIT_EXP =CommonConstants.EMPTY_STRING;
		 * mcfa.FA_DTL_LIMIT_CHK_FLAG =(String)
		 * accPO.getDataMap().get(IBOAccountLimitFeature.LIMITINDICATOR);
		 * 
		 * 
		 * accHash.put((String) accPO.getDataMap().get(IBOAccount.BRANCHSORTCODE)+CrAc,
		 * mcfa); } }PRJ511125-END
		 */
	}

	/**
	 * Method to perform the main account file refresh uses the populated account
	 * objects to format and write the details to mcfa9999.dat
	 * 
	 * @param env
	 * @
	 */
	private synchronized void refreshAccount(BankFusionEnvironment env) {

		FileOutputStream fout = null;

		int Branchctr = 0;
		try {
			String Branch = CommonConstants.EMPTY_STRING;
			boolean notfirstTime = false;
			// Bug#7222
			if (accHash.size() == 0)
				return;
			// Bug#7222
			Iterator itr = accHash.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				mcfaData mcfdet = (mcfaData) accHash.get(key);
				if (!Branch.equalsIgnoreCase(mcfdet.FA_DTL_BRANCH)) {
					if (notfirstTime) {
						formatAccountTrail(String.valueOf(Branchctr), fout, env);
						Branchctr = 0;
					}
					fout = new FileOutputStream(extractPath + "mcfa" + mcfdet.FA_DTL_BRANCH + ".dat");

					formatAccountHeader(mcfdet.FA_DTL_BRANCH, fout, env);
					Branch = (String) mcfdet.FA_DTL_BRANCH;
					notfirstTime = true;
				}
				Integer scale1 = (Integer) (CurrencyHash.get(mcfdet.FA_DTL_CURR));
				int scale = scale1.intValue();

				fileData = new StringBuffer();
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-REC-TYPE")).intValue(), "02", 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-ACC-ID")).intValue(),
						mcfdet.FA_DTL_ACC_ID, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-SHORTNAME1")).intValue(),
						mcfdet.FA_DTL_SHORTNAME1, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-SHORTNAME2")).intValue(),
						mcfdet.FA_DTL_SHORTNAME2, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-LED-SUBLED")).intValue(),
						mcfdet.FA_DTL_LED_SUBLED, 'A'));
				fileData.append(
						setField(new Integer(getRereshProperty("FA-DTL-CURR")).intValue(), mcfdet.FA_DTL_CURR, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-ACC-TYPE")).intValue(),
						mcfdet.FA_DTL_ACC_TYPE, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-ACC-TYPE-DESC")).intValue(),
						mcfdet.FA_DTL_ACC_TYPE_DESC, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-CLIENT-NUMBER")).intValue(),
						mcfdet.FA_DTL_CLIENT_NUMBER, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-STOP-BLK-IND")).intValue(),
						mcfdet.FA_DTL_STOP_BLK_IND, 'A'));

				// PRJ511125-START fileData.append(setField(new
				// Integer(getRereshProperty("FA-DTL-DEBIT-LIMIT")).intValue(),mcfdet.FA_DTL_DEBIT_LIMIT
				// , 'N'));
				setAmount(new BigDecimal(mcfdet.FA_DTL_DEBIT_LIMIT), "FA-DTL-DEBIT-LIMIT", fileData, scale);
				// PRJ511125-END
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-DEBIT-LIMIT-EXP")).intValue(),
						mcfdet.FA_DTL_DEBIT_LIMIT_EXP, 'N'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-FILLER1")).intValue(),
						CommonConstants.EMPTY_STRING, 'A'));
				setAmount(new BigDecimal(mcfdet.FA_DTL_CLEARED_BAL), "FA-DTL-CLEARED-BAL", fileData, scale);
				setAmount(new BigDecimal(mcfdet.FA_DTL_BOOK_BAL), "FA-DTL-BOOK-BAL", fileData, scale);
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-GARN-HOLD")).intValue(),
						CommonConstants.EMPTY_STRING, 'N'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-LIEN-HOLD")).intValue(),
						CommonConstants.EMPTY_STRING, 'N'));
				// fileData.append(setField(new
				// Integer(getRereshProperty("FA-DTL-PLEDGE-HOLD")).intValue(),
				// CommonConstants.EMPTY_STRING, 'N'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-BLOCK-BAL")).intValue(),
						CommonConstants.EMPTY_STRING, 'N'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-SEC-RATING")).intValue(),
						CommonConstants.EMPTY_STRING, 'N'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-STAFF-IND")).intValue(),
						CommonConstants.EMPTY_STRING, 'A'));
				// PRJ511125-START fileData.append(setField(new
				// Integer(getRereshProperty("FA-DTL-CREDIT-LIMIT")).intValue(),
				// mcfdet.FA_DTL_CREDIT_LIMIT, 'N'));
				setAmount(new BigDecimal(mcfdet.FA_DTL_CREDIT_LIMIT), "FA-DTL-CREDIT-LIMIT", fileData, scale);
				// PRJ511125-END
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-CREDIT-LIMIT-EXP")).intValue(),
						mcfdet.FA_DTL_CREDIT_LIMIT_EXP, 'N'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-ARREARS-BAL")).intValue(),
						CommonConstants.EMPTY_STRING, 'N'));
				/*
				 * PRJ511125-START fileData.append(setField(new
				 * Integer(getRereshProperty("FA-DTL-DATE-LAST-EXT")).intValue(), "0", 'N'));
				 */
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-DATE-LAST-EXT")).intValue(),
						mcfdet.FA_DTL_DATE_LAST_EXT, 'N'));
				// PRJ511125-END
				setAmount(new BigDecimal(mcfdet.FA_DTL_ACCRUED_INT), "FA-DTL-ACCRUED-INT", fileData, scale);
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-1-DAYS-ACCD-INT")).intValue(),
						CommonConstants.EMPTY_STRING, 'N'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-RATE-BASIS")).intValue(),
						CommonConstants.EMPTY_STRING, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-ACCD-INT-DATE")).intValue(),
						CommonConstants.EMPTY_STRING, 'N'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-TOTAL-LOAN")).intValue(),
						CommonConstants.EMPTY_STRING, 'N'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-TOTAL-LOAN-DRAW")).intValue(),
						CommonConstants.EMPTY_STRING, 'N'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-ALT-IDENTIFIER")).intValue(),
						CommonConstants.EMPTY_STRING, 'N'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-ALT-ID-FILLER")).intValue(),
						CommonConstants.EMPTY_STRING, 'A'));

				/*
				 * PRJ511125-START fileData.append(setField(new
				 * Integer(getRereshProperty("FA-DTL-PASS-PROT-FLAG")).intValue(),
				 * CommonConstants.EMPTY_STRING, 'A')); fileData.append(setField(new
				 * Integer(getRereshProperty("FA-DTL-LIMIT-CHK-FLAG")).intValue(),
				 * CommonConstants.EMPTY_STRING, 'A')); fileData.append(setField(new
				 * Integer(getRereshProperty("FA-DTL-LIMIT-NUMB-1")).intValue(),
				 * CommonConstants.EMPTY_STRING, 'A')); fileData.append(setField(new
				 * Integer(getRereshProperty("FA-DTL-LIMIT-NUMB-2")).intValue(),
				 * CommonConstants.EMPTY_STRING, 'A')); fileData.append(setField(new
				 * Integer(getRereshProperty("FA-DTL-LIMIT-NUMB-3")).intValue(),
				 * CommonConstants.EMPTY_STRING, 'A')); fileData.append(setField(new
				 * Integer(getRereshProperty("FA-DTL-LIMIT-NUMB-4")).intValue(),
				 * CommonConstants.EMPTY_STRING, 'A')); fileData.append(setField(new
				 * Integer(getRereshProperty("FA-DTL-LIMIT-NUMB-5")).intValue(),
				 * CommonConstants.EMPTY_STRING, 'A')); fileData.append(setField(new
				 * Integer(getRereshProperty("FA-DTL-SUM-CHQ-DEP-BAL")).intValue(),
				 * CommonConstants.EMPTY_STRING, 'N'));
				 */
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-PASS-PROT-FLAG")).intValue(),
						mcfdet.FA_DTL_PASS_PROT_FLAG, 'N'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-LIMIT-CHK-FLAG")).intValue(),
						mcfdet.FA_DTL_LIMIT_CHK_FLAG, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-LIMIT-NUMB-1")).intValue(),
						mcfdet.FA_DTL_LIMIT_NUMB_1, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-LIMIT-NUMB-2")).intValue(),
						mcfdet.FA_DTL_LIMIT_NUMB_2, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-LIMIT-NUMB-3")).intValue(),
						mcfdet.FA_DTL_LIMIT_NUMB_3, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-LIMIT-NUMB-4")).intValue(),
						mcfdet.FA_DTL_LIMIT_NUMB_4, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-LIMIT-NUMB-5")).intValue(),
						mcfdet.FA_DTL_LIMIT_NUMB_5, 'A'));
				setAmount(new BigDecimal(mcfdet.FA_DTL_SUM_CHQ_DEP_BAL), "FA-DTL-SUM-CHQ-DEP-BAL", fileData, scale);
				// PRJ511125-END
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-FILLER-2")).intValue(),
						CommonConstants.EMPTY_STRING, 'N'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-BRANCH")).intValue(),
						mcfdet.FA_DTL_BRANCH, 'N'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-ACTION")).intValue(), "A", 'A'));
				// CALCULATE CHECKSUM AND POPULATE
				// COMPUTE WM-VME-ACC-CHECKSUM =
				// ( (WM-VMA-DTL-BRANCH)
				// + (WM-VMA-DTL-CLIENT-NO-9) + (WM-VMA-DTL-DR-LIMIT)
				// + (WM-VMA-DTL-DR-LIMIT-EXP) + (WM-VMA-DTL-CR-LIMIT)
				// + (WM-VMA-DTL-CR-LIMIT-EXP) + (WM-VMA-DTL-CLEARED-BAL)
				// + (WM-VMA-DTL-BOOK-BAL) + (WM-VMA-DTL-ARREARS-BAL)
				// + (WM-VMA-DTL-DATE-LST-EXP) + (WM-VMA-DTL-ACCRUED-INT)
				// + (WM-VMA-DTL-ACR-INT-DATE) + (WM-VMA-DTL-TOTAL-LN)
				// + (WM-VMA-DTL-TOTAL-LN-DRW) + (WM-VMA-DTL-GARN-HOLD)
				// + (WM-VMA-DTL-LIEN-HOLD) + (WM-VMA-DTL-PLEDGE-HOLD)
				// + (WS01-LNA-DTL-SEC-RATING) + (WS01-LNA-DTL-CHQS-LODGED)).

				BigDecimal checksum = new BigDecimal(0).setScale(0);
				try {
					checksum = new BigDecimal(mcfdet.FA_DTL_BRANCH)
							// .add(new BigDecimal(mcfdet.FA_DTL_CLIENT_NUMBER))
							.add(new BigDecimal(mcfdet.FA_DTL_DEBIT_LIMIT).movePointRight(scale).abs())
							// .add(new
							// BigDecimal(mcfdet.FA_DTL_DEBIT_LIMIT_EXP).movePointRight(scale).abs())
							.add(new BigDecimal(mcfdet.FA_DTL_CREDIT_LIMIT).movePointRight(scale).abs())
							// .add(new
							// BigDecimal(mcfdet.FA_DTL_CREDIT_LIMIT_EXP).movePointRight(scale).abs())
							.add(new BigDecimal(mcfdet.FA_DTL_CLEARED_BAL).movePointRight(scale).abs())
							.add(new BigDecimal(mcfdet.FA_DTL_BOOK_BAL).movePointRight(scale).abs())
							.add(new BigDecimal(mcfdet.FA_DTL_ARREARS_BAL).movePointRight(scale).abs())
							// PRJ511125-START .add(new
							// BigDecimal(mcfdet.FA_DTL_DATE_LAST_EXT).movePointRight(scale).abs())
							.add(new BigDecimal(mcfdet.FA_DTL_DATE_LAST_EXT))
							// PRJ51125-END
							.add(new BigDecimal(mcfdet.FA_DTL_ACCRUED_INT).movePointRight(scale).abs())
							.add(new BigDecimal(mcfdet.FA_DTL_ACCD_INT_DATE))
							.add(new BigDecimal(mcfdet.FA_DTL_TOTAL_LOAN).movePointRight(scale).abs())
							.add(new BigDecimal(mcfdet.FA_DTL_TOTAL_LOAN_DRAW))
							.add(new BigDecimal(mcfdet.FA_DTL_GARN_HOLD).movePointRight(scale).abs())
							.add(new BigDecimal(mcfdet.FA_DTL_LIEN_HOLD)).add(new BigDecimal(mcfdet.FA_DTL_SEC_RATING))
							// PRJ511125-START
							.add(new BigDecimal(mcfdet.FA_DTL_SUM_CHQ_DEP_BAL).movePointRight(scale).abs());
					// PRJ511125-END

				} catch (NumberFormatException nfException) {
					continue;
				}
				BigInteger cksum = checksum.toBigInteger();
				fileData.append(
						setField(new Integer(getRereshProperty("FA-DTL-CHECKSUM")).intValue(), cksum.toString(), 'N'));
				fileData.append(setField(new Integer(getRereshProperty("FA-DTL-FILLER-3")).intValue(),
						CommonConstants.EMPTY_STRING, 'A'));
				Branchctr++;
				fileData.append("\r\n");
				fout.write(fileData.toString().getBytes());
				fout.flush();
				Branch = mcfdet.FA_DTL_BRANCH;
			}
			formatAccountTrail(String.valueOf(Branchctr), fout, env);

		} catch (FileNotFoundException fnfExcpn) {
			// BPR

			BranchPowerRefreshFatomContext.Status = Boolean.FALSE;

			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { fnfExcpn.getLocalizedMessage() }, new HashMap(), env);
		} catch (IOException ioExcpn) {

			BranchPowerRefreshFatomContext.Status = Boolean.FALSE;

			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
		} catch (Exception Excpn) {

			BranchPowerRefreshFatomContext.Status = Boolean.FALSE;

			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { Excpn.getLocalizedMessage() }, new HashMap(), env);
		}

	}

	/**
	 * Method to change amount from Bankmaster to regular format formats and
	 * populates the string buffer with the formatted amount takes bdAmt,fldName as
	 * input
	 * 
	 * @param bdAmt
	 * @param fldName
	 * @param fileData
	 */
	private static void setAmount(BigDecimal bdAmt, String fldName, StringBuffer fileData, int scale) {
		bdAmt = bdAmt.setScale(scale, BigDecimal.ROUND_DOWN);
		String Amount = bdAmt.unscaledValue().abs().toString();

		if (bdAmt.signum() == -1) {
			fileData.append(setField(new Integer(getRereshProperty(fldName)).intValue() - 1,
					Amount.substring(0, Amount.length() - 1), 'N'));
			char str = Amount.charAt(Amount.length() - 1);
			fileData.append(getRereshProperty(String.valueOf(str)));
		} else {
			fileData.append(setField(new Integer(getRereshProperty(fldName)).intValue(), Amount, 'N'));
		}
	}

	/**
	 * formats and writes the mcfa9999.dat header
	 * 
	 * @param Branch
	 * @param fout
	 */
	private void formatAccountHeader(String Branch, FileOutputStream fout, BankFusionEnvironment env) {
		StringBuffer fileData = new StringBuffer();
		fileData.append(setField(new Integer(getRereshProperty("FA-HDR-REC-TYPE")).intValue(), "01", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FA-HDR-ACTION")).intValue(), "A", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FA-HDR-SOURCE-SYSTEM")).intValue(), "MCAS", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FA-HDR-DEST-SYSTEM")).intValue(), "BPWR", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FA-HDR-BRANCH-CODE")).intValue(), Branch, 'N'));
		fileData.append(setField(new Integer(getRereshProperty("FA-HDR-FILE-ID")).intValue(), "AC", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FA-HDR-PROCESS-DATE")).intValue(), strHdrDt, 'N'));
		fileData.append(setField(new Integer(getRereshProperty("FA-HDR-FILLER")).intValue(), "0", 'N'));
		fileData.append("\r\n");
		try {
			fout.write(fileData.toString().getBytes());
			fout.flush();
		} catch (IOException ioExcpn) {
			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
		}
	}

	/**
	 * formats and writes the mcfa9999.dat trailer record
	 * 
	 * @param Branchctr
	 * @param fout
	 */
	private void formatAccountTrail(String Branchctr, FileOutputStream fout, BankFusionEnvironment env) {
		StringBuffer fileData = new StringBuffer();
		fileData.append(setField(new Integer(getRereshProperty("FA-TRL-REC-TYPE")).intValue(), "99", 'A'));
		fileData.append(
				setField(new Integer(getRereshProperty("FA-TRL-REC-COUNT")).intValue(), (String) Branchctr, 'N'));
		fileData.append(setField(new Integer(getRereshProperty("FA-TRL-FILLER")).intValue(), "0", 'N'));
		fileData.append("\r\n");
		try {
			fout.write(fileData.toString().getBytes());
			fout.flush();
			fout.close();
		}
		// Bug#7222
		catch (NullPointerException npExcpn) {
			return;
		}
		// Bug#7222
		catch (IOException ioExcpn) {
			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
		}
	}

	/**
	 * Performs the main stopped cheques file refresh
	 * 
	 * @param env
	 * @
	 */
	private synchronized void refreshStoppedCheques(BankFusionEnvironment env) {

		List StoppedChequesDetails = null;
		StoppedChequesDetails = env.getFactory().executeGenericQuery(queryStoppedCheques, null, null);
		SimplePersistentObject stpChqPO = null;
		FileOutputStream fout = null;
		Integer scale1 = null;
		int scale = 0;
		int stpchqCtr = 0;
		String StopStatus;
		// Bug#7222
		if (StoppedChequesDetails.size() == 0)
			return;
		// Bug#7222
		try {
			fout = new FileOutputStream(extractPath + "mcfstpch.dat");
			for (int i = 0; i < StoppedChequesDetails.size(); i++) {
				stpChqPO = (SimplePersistentObject) StoppedChequesDetails.get(i);
				if (i == 0) {
					formatStoppedChequesHeader(fout, env);
				}
				if (stpChqPO.getDataMap().get(IBOStoppedChq.ACCOUNTID) != null) {

					scale1 = (Integer) (CurrencyHash.get(stpChqPO.getDataMap().get(IBOStoppedChq.ISOCURRENCYCODE)));
					scale = scale1.intValue();

					fileData = new StringBuffer();
					fileData.append(
							setField(new Integer(getRereshProperty("FS-STP-CHQ-DTL-REC-TYPE")).intValue(), "02", 'N'));
					fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-DTL-ACCOUNT-NUM")).intValue(),
							(String) stpChqPO.getDataMap().get(IBOStoppedChq.ACCOUNTID), 'A'));
					if (stpChqPO.getDataMap().get(IBOStoppedChq.TOSTOPCHQREF).toString().length() > 12)
						fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-DTL-CHEQUE-NUM")).intValue(),
								stpChqPO.getDataMap().get(IBOStoppedChq.TOSTOPCHQREF).toString().substring(0, 11),
								'N'));
					else
						fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-DTL-CHEQUE-NUM")).intValue(),
								stpChqPO.getDataMap().get(IBOStoppedChq.TOSTOPCHQREF).toString(), 'N'));

					if (stpChqPO.getDataMap().get(IBOStoppedChq.AMOUNT).toString().length() > 17)
						setAmount(
								new BigDecimal(
										stpChqPO.getDataMap().get(IBOStoppedChq.AMOUNT).toString().substring(0, 17)),
								"FS-STP-CHQ-DTL-AMOUNT", fileData, scale);
					else
						setAmount(new BigDecimal(stpChqPO.getDataMap().get(IBOStoppedChq.AMOUNT).toString()),
								"FS-STP-CHQ-DTL-AMOUNT", fileData, scale);

					Date dtTmp = null; // date format = 2006-04-09 20:17:55449
					String dtStr = stpChqPO.getDataMap().get(IBOStoppedChq.STOPDATE).toString();
					SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
					dtTmp = sf.parse(dtStr);
					String strDtDrawn = new SimpleDateFormat("yyyyMMdd").format(dtTmp);
					fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-DTL-DATE-DRAWN")).intValue(),
							strDtDrawn, 'N'));
					if (stpChqPO.getDataMap().get(IBOStoppedChq.STOPREASON).toString().length() > 25)
						fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-DTL-PAYEE-NARR")).intValue(),
								stpChqPO.getDataMap().get(IBOStoppedChq.STOPREASON).toString().substring(0, 24), 'A'));
					else
						fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-DTL-PAYEE-NARR")).intValue(),
								(String) stpChqPO.getDataMap().get(IBOStoppedChq.STOPREASON), 'A'));

					if (stpChqPO.getDataMap().get(IBOStoppedChq.STOPPEDSTATUS).toString() == "true")
						StopStatus = "1";
					else
						StopStatus = "0";
					fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-DTL-STOP-FLAG")).intValue(),
							StopStatus, 'N'));
					if (stpChqPO.getDataMap().get(IBOStoppedChq.FROMSTOPCHQREF).toString().length() > 12)
						fileData.append(setField(
								new Integer(getRereshProperty("FS-STP-CHQ-DTL-FROM-CHQ-NO")).intValue(),
								stpChqPO.getDataMap().get(IBOStoppedChq.FROMSTOPCHQREF).toString().substring(0, 11),
								'N'));
					else
						fileData.append(
								setField(new Integer(getRereshProperty("FS-STP-CHQ-DTL-FROM-CHQ-NO")).intValue(),
										stpChqPO.getDataMap().get(IBOStoppedChq.FROMSTOPCHQREF).toString(), 'N'));

					fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-DTL-FILLER")).intValue(),
							CommonConstants.EMPTY_STRING, 'A'));
					fileData.append(
							setField(new Integer(getRereshProperty("FS-STP-CHQ-DTL-ACTION")).intValue(), "A", 'A'));
					// PRJ511125-Start BigDecimal checksum = new
					// BigDecimal(stpChqPO.getDataMap().get(IBOStoppedChq.STOPCHEQUEREF).toString()).add(new
					// BigDecimal(stpChqPO.getDataMap().get(IBOTransaction.AMOUNT).toString()).abs())
					BigDecimal checksum = new BigDecimal(
							stpChqPO.getDataMap().get(IBOStoppedChq.TOSTOPCHQREF).toString()).add(
									(new BigDecimal(stpChqPO.getDataMap().get(IBOStoppedChq.AMOUNT).toString()).abs())
											.movePointRight(scale))
									// PRJ511125-END
									.add(new BigDecimal(strDtDrawn)).add(new BigDecimal(
											stpChqPO.getDataMap().get(IBOStoppedChq.FROMSTOPCHQREF).toString()));
					checksum = checksum.setScale(0, BigDecimal.ROUND_CEILING);
					fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-DTL-CHECKSUM")).intValue(),
							checksum.abs().toString(), 'N'));
					fileData.append("\r\n");
					fout.write(fileData.toString().getBytes());
					fout.flush();
					stpchqCtr++;
				}
			}
			formatStoppedChequesTrail(String.valueOf(stpchqCtr), fout, env);
			fout.close();
		} catch (FileNotFoundException fnfExcpn) {
			// BPR

			BranchPowerRefreshFatomContext.Status = Boolean.FALSE;

			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { fnfExcpn.getLocalizedMessage() }, new HashMap(), env);
		} catch (IOException ioExcpn) {

			BranchPowerRefreshFatomContext.Status = Boolean.FALSE;

			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
		} catch (NumberFormatException nfExcpn) {

			BranchPowerRefreshFatomContext.Status = Boolean.FALSE;

			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { nfExcpn.getLocalizedMessage() }, new HashMap(), env);
		} catch (ParseException pExcpn) {
			// BPR

			BranchPowerRefreshFatomContext.Status = Boolean.FALSE;

			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { pExcpn.getLocalizedMessage() }, new HashMap(), env);
		}
		// BPR
		catch (Exception Excpn) {

			BranchPowerRefreshFatomContext.Status = Boolean.FALSE;

			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { Excpn.getLocalizedMessage() }, new HashMap(), env);
		}
		// BPR

	}

	/**
	 * formats and writes the stopped cheque file header to mcfstpch.dat
	 * 
	 * @param fout
	 */
	private void formatStoppedChequesHeader(FileOutputStream fout, BankFusionEnvironment env) {
		StringBuffer fileData = new StringBuffer();
		fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-HDR-REC-TYPE")).intValue(), "01", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-HDR-FILE-ACTION")).intValue(), "A", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-HDR-FILE-ID")).intValue(), "SC", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-HDR-FILLER")).intValue(),
				CommonConstants.EMPTY_STRING, 'A'));
		fileData.append("\r\n");
		try {
			fout.write(fileData.toString().getBytes());
			fout.flush();
		} catch (IOException ioExcpn) {
			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
		}
	}

	/**
	 * formats and writes the stopped cheque file trailer to mcfstpch.dat
	 * 
	 * @param chqCtr
	 * @param fout
	 */
	private void formatStoppedChequesTrail(String chqCtr, FileOutputStream fout, BankFusionEnvironment env) {
		StringBuffer fileData = new StringBuffer();
		fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-TRL-REC-TYPE")).intValue(), "99", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-TRL-REC-COUNT")).intValue(), chqCtr, 'N'));
		fileData.append(setField(new Integer(getRereshProperty("FS-STP-CHQ-TRL-FILLER")).intValue(),
				CommonConstants.EMPTY_STRING, 'A'));
		fileData.append("\r\n");
		try {
			fout.write(fileData.toString().getBytes());
			fout.flush();
			fout.close();
		}
		// Bug#7222
		catch (NullPointerException npExcpn) {
			return;
		}
		// Bug#7222
		catch (IOException ioExcpn) {
			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
		}
	}

	/**
	 * performs account notes refresh reads the account notes table, formats and
	 * writes the details to nad0data.dat
	 * 
	 * @param env
	 * @
	 */

	/**
	 * formats and wrtes customer limit trailer record
	 * 
	 * @param Branchctr
	 * @param fout
	 */
	private void formatLimitTrail(String Branchctr, FileOutputStream fout, BankFusionEnvironment env) {
		StringBuffer fileData = new StringBuffer();
		fileData.append(setField(new Integer(getRereshProperty("FL-TRL-RECORD-TYPE")).intValue(), "99", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FL-TRL-RECORD-COUNT")).intValue(), Branchctr, 'N'));
		fileData.append(setField(new Integer(getRereshProperty("FL-TRL-FILLER")).intValue(),
				CommonConstants.EMPTY_STRING, 'A'));
		fileData.append("\r\n");
		try {
			fout.write(fileData.toString().getBytes());
			fout.flush();
			fout.close();
		}
		// Bug#7222
		catch (NullPointerException ioExcpn) {
			return;
		}
		// Bug#7222
		catch (IOException ioExcpn) {
			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
		}
	}

	/**
	 * performs the main customer refresh outputs branchwise data to mcfc9999.dat
	 * 
	 * @param env
	 * @
	 */
	private synchronized void refreshCustomer(BankFusionEnvironment env) {

		List customerDetails = null;
		customerDetails = env.getFactory().executeGenericQuery(queryCustomer, branchRange, null);
		SimplePersistentObject custPO = null;
		FileOutputStream fout = null;
		int Branchctr = 0;
		boolean notfirstTime = false;
		// Bug#7222
		if (customerDetails.size() == 0)
			return;
		// Bug#7222
		try {
			String Branch = CommonConstants.EMPTY_STRING;
			for (int i = 0; i < customerDetails.size(); i++) {
				custPO = (SimplePersistentObject) customerDetails.get(i);
				if (!Branch.equalsIgnoreCase(custPO.getDataMap().get(IBOBranch.BMBRANCH).toString())) {
					if (notfirstTime) {
						formatCustomerTrail(String.valueOf(Branchctr), fout, env);
						Branchctr = 0;
					}
					fout = new FileOutputStream(
							extractPath + "mcfc" + custPO.getDataMap().get(IBOBranch.BMBRANCH).toString() + ".dat");
					formatCustomerHeader(custPO.getDataMap().get(IBOBranch.BMBRANCH).toString(), fout, env);
					Branch = (String) custPO.getDataMap().get(IBOBranch.BMBRANCH).toString();
					notfirstTime = true;
				}
				fileData = new StringBuffer();

				fileData.append(setField(new Integer(getRereshProperty("FC-DTL-REC-TYPE")).intValue(), "02", 'A'));
				if (custPO.getDataMap().get(IBOCustomer.CUSTOMERCODE).toString().length() > 9)
					fileData.append(setField(new Integer(getRereshProperty("FC-DTL-CLIENT-NUMBER")).intValue(),
							custPO.getDataMap().get(IBOCustomer.CUSTOMERCODE).toString().substring(0, 8), 'A'));
				else
					fileData.append(setField(new Integer(getRereshProperty("FC-DTL-CLIENT-NUMBER")).intValue(),
							(String) custPO.getDataMap().get(IBOCustomer.CUSTOMERCODE), 'A'));
				if (custPO.getDataMap().get(IBOCustomer.SHORTNAME).toString().length() > 30)
					fileData.append(setField(new Integer(getRereshProperty("FC-DTL-SHORTNAME1")).intValue(),
							custPO.getDataMap().get(IBOCustomer.SHORTNAME).toString().trim().substring(0, 29), 'A'));
				else
					fileData.append(setField(new Integer(getRereshProperty("FC-DTL-SHORTNAME1")).intValue(),
							(String) custPO.getDataMap().get(IBOCustomer.SHORTNAME).toString().trim(), 'A'));

				fileData.append(setField(new Integer(getRereshProperty("FC-DTL-SHORTNAME2")).intValue(),
						CommonConstants.EMPTY_STRING, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FC-DTL-LOCATION")).intValue(),
						CommonConstants.EMPTY_STRING, 'A'));
				if (custPO.getDataMap().get(IBOCustomer.ALPHACODE).toString().length() > 10) {
					fileData.append(setField(new Integer(getRereshProperty("FC-DTL-ALPHA-CODE")).intValue(),
							(String) custPO.getDataMap().get(IBOCustomer.ALPHACODE).toString().substring(0, 9), 'A'));
				} else {
					fileData.append(setField(new Integer(getRereshProperty("FC-DTL-ALPHA-CODE")).intValue(),
							(String) custPO.getDataMap().get(IBOCustomer.ALPHACODE).toString(), 'A'));
				}
				/*
				 * PRJ511125-START fileData.append(setField(new
				 * Integer(getRereshProperty("FC-DTL-MAIN-CURR")).intValue(),
				 * CommonConstants.EMPTY_STRING, 'A'));
				 */
				fileData.append(setField(new Integer(getRereshProperty("FC-DTL-MAIN-CURR")).intValue(),
						(String) custPO.getDataMap().get(IBOCustomer.REPORTINGCURRENCY).toString(), 'A'));
				// PRJ511125-END
				fileData.append(setField(new Integer(getRereshProperty("FC-DTL-SEC-RATING")).intValue(),
						CommonConstants.EMPTY_STRING, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FC-DTL-CLOSED-IND")).intValue(),
						CommonConstants.EMPTY_STRING, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FC-DTL-GARN-ORDER")).intValue(),
						CommonConstants.EMPTY_STRING, 'A'));

				/* PRJ511125-Start--- Address lines reading from address table */
				if (custPO.getDataMap().get(IBOAddress.ADDRESSLINE1).toString().length() > 45)
					fileData.append(setField(new Integer(getRereshProperty("FC-DTL-ADDRESS1")).intValue(),
							custPO.getDataMap().get(IBOAddress.ADDRESSLINE1).toString().trim().substring(0, 44), 'A'));
				else
					fileData.append(setField(new Integer(getRereshProperty("FC-DTL-ADDRESS1")).intValue(),
							(String) custPO.getDataMap().get(IBOAddress.ADDRESSLINE1).toString().trim(), 'A'));
				if (custPO.getDataMap().get(IBOAddress.ADDRESSLINE2).toString().length() > 45)
					fileData.append(setField(new Integer(getRereshProperty("FC-DTL-ADDRESS2")).intValue(),
							custPO.getDataMap().get(IBOAddress.ADDRESSLINE2).toString().trim().substring(0, 44), 'A'));
				else
					fileData.append(setField(new Integer(getRereshProperty("FC-DTL-ADDRESS2")).intValue(),
							(String) custPO.getDataMap().get(IBOAddress.ADDRESSLINE2).toString().trim(), 'A'));
				if (custPO.getDataMap().get(IBOAddress.ADDRESSLINE3).toString().length() > 45)
					fileData.append(setField(new Integer(getRereshProperty("FC-DTL-ADDRESS3")).intValue(),
							custPO.getDataMap().get(IBOAddress.ADDRESSLINE3).toString().trim().substring(0, 44), 'A'));
				else
					fileData.append(setField(new Integer(getRereshProperty("FC-DTL-ADDRESS3")).intValue(),
							(String) custPO.getDataMap().get(IBOAddress.ADDRESSLINE3).toString().trim(), 'A'));
				if (custPO.getDataMap().get(IBOAddress.ADDRESSLINE4).toString().length() > 45)
					fileData.append(setField(new Integer(getRereshProperty("FC-DTL-ADDRESS4")).intValue(),
							custPO.getDataMap().get(IBOAddress.ADDRESSLINE4).toString().trim().substring(0, 44), 'A'));
				else
					fileData.append(setField(new Integer(getRereshProperty("FC-DTL-ADDRESS4")).intValue(),
							(String) custPO.getDataMap().get(IBOAddress.ADDRESSLINE4).toString().trim(), 'A'));
				if (custPO.getDataMap().get(IBOAddress.ADDRESSLINE5).toString().length() > 45)
					fileData.append(setField(new Integer(getRereshProperty("FC-DTL-ADDRESS5")).intValue(),
							custPO.getDataMap().get(IBOAddress.ADDRESSLINE5).toString().trim().substring(0, 44), 'A'));
				else
					fileData.append(setField(new Integer(getRereshProperty("FC-DTL-ADDRESS5")).intValue(),
							(String) custPO.getDataMap().get(IBOAddress.ADDRESSLINE5).toString().trim(), 'A'));
				if (custPO.getDataMap().get(IBOAddress.ADDRESSLINE6).toString().length() > 45)
					fileData.append(setField(new Integer(getRereshProperty("FC-DTL-ADDRESS6")).intValue(),
							custPO.getDataMap().get(IBOAddress.ADDRESSLINE6).toString().trim().substring(0, 44), 'A'));
				else
					fileData.append(setField(new Integer(getRereshProperty("FC-DTL-ADDRESS6")).intValue(),
							(String) custPO.getDataMap().get(IBOAddress.ADDRESSLINE6).toString().trim(), 'A'));
				if (custPO.getDataMap().get(IBOAddress.ADDRESSLINE7).toString().length() > 45)
					fileData.append(setField(new Integer(getRereshProperty("FC-DTL-ADDRESS7")).intValue(),
							custPO.getDataMap().get(IBOAddress.ADDRESSLINE7).toString().trim().substring(0, 44), 'A'));
				else
					fileData.append(setField(new Integer(getRereshProperty("FC-DTL-ADDRESS7")).intValue(),
							(String) custPO.getDataMap().get(IBOAddress.ADDRESSLINE7).toString().trim(), 'A'));
				// PRJ511125-End

				fileData.append(setField(new Integer(getRereshProperty("FC-DTL-MNEMONIC")).intValue(),
						CommonConstants.EMPTY_STRING, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FC-DTL-STAFF-IND")).intValue(),
						CommonConstants.EMPTY_STRING, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FC-DTL-CLIENT-CLASS")).intValue(),
						CommonConstants.EMPTY_STRING, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FC-DTL-CLIENT-OCCUP")).intValue(),
						CommonConstants.EMPTY_STRING, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FC-DTL-BRANCH-RESP")).intValue(),
						CommonConstants.EMPTY_STRING, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FC-DTL-LANG-PREF")).intValue(),
						CommonConstants.EMPTY_STRING, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FC-DTL-LIMIT-CHK-FLAG")).intValue(),
						custPO.getDataMap().get(IBOLimit.LIMITINDICATOR).toString(), 'A'));
				// PRJ511125-START fileData.append(setField(new
				// Integer(getRereshProperty("FC-DTL-EXT-ALPHA-CODE")).intValue(),
				// CommonConstants.EMPTY_STRING, 'A'));
				if (custPO.getDataMap().get(IBOCustomer.ALPHACODE).toString().length() > 15)
					fileData.append(setField(new Integer(getRereshProperty("FC-DTL-EXT-ALPHA-CODE")).intValue(),
							(String) custPO.getDataMap().get(IBOCustomer.ALPHACODE).toString().substring(0, 14), 'A'));
				else
					fileData.append(setField(new Integer(getRereshProperty("FC-DTL-EXT-ALPHA-CODE")).intValue(),
							(String) custPO.getDataMap().get(IBOCustomer.ALPHACODE).toString(), 'A'));
				// PRJ511125-END

				fileData.append(setField(new Integer(getRereshProperty("FC-DTL-EXT-ALT-CUS-ID")).intValue(),
						CommonConstants.EMPTY_STRING, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FC-DTL-FILLER-1")).intValue(),
						CommonConstants.EMPTY_STRING, 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FC-DTL-BRANCH")).intValue(),
						(String) custPO.getDataMap().get(IBOBranch.BMBRANCH), 'A'));
				fileData.append(setField(new Integer(getRereshProperty("FC-DTL-ACTION")).intValue(), "A", 'A'));

				// CALCULATE CHECKSUM AND POPULATE
				/*
				 * ADD FC-DTL-SEC-RATING TO FC-DTL-CHECKSUM NOT AVAILABLE ADD FC-DTL-BRANCH TO
				 * FC-DTL-CHECKSUM ADD FC-DTL-GARN-ORDER TO FC-DTL-CHECKSUM NOT AVAILABLE ADD
				 * FC-DTL-BRANCH-RESP TO FC-DTL-CHECKSUM NOT AVAILABLE ADD FC-DTL-CLIENT-CLASS
				 * TO FC-DTL-CHECKSUM NOT AVAILABLE
				 */
				BigDecimal checksum = new BigDecimal(custPO.getDataMap().get(IBOBranch.BMBRANCH).toString());
				// .add(new
				// BigDecimal(custPO.getDataMap().get(IBOBranch.BMBRANCH).toString()).abs());
				checksum = checksum.setScale(0);
				fileData.append(setField(new Integer(getRereshProperty("FC-DTL-CHECKSUM")).intValue(),
						checksum.abs().toString(), 'N'));
				fileData.append(setField(new Integer(getRereshProperty("FC-DTL-FILLER-2")).intValue(),
						CommonConstants.EMPTY_STRING, 'A'));
				Branchctr++;
				fileData.append("\r\n");

				fout.write(fileData.toString().getBytes());
				fout.flush();

				Branch = (String) custPO.getDataMap().get(IBOBranch.BMBRANCH);
			}
			formatCustomerTrail(String.valueOf(Branchctr), fout, env);
			fout.close();
		} catch (FileNotFoundException fnfExcpn) {
			// BPR

			BranchPowerRefreshFatomContext.Status = Boolean.FALSE;

			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { fnfExcpn.getLocalizedMessage() }, new HashMap(), env);
		} catch (IOException ioExcpn) {
			// BPR

			BranchPowerRefreshFatomContext.Status = Boolean.FALSE;

			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
		}
		// BPR
		catch (Exception Excpn) {

			BranchPowerRefreshFatomContext.Status = Boolean.FALSE;

			logger.error(Excpn.getStackTrace());
			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { Excpn.getLocalizedMessage() }, new HashMap(), env);

		}

		// BPR
	}

	/**
	 * formats and writes the customer header record to mcfc9999.dat
	 * 
	 * @param Branch
	 * @param fout
	 * @
	 */
	private void formatCustomerHeader(String Branch, FileOutputStream fout, BankFusionEnvironment env) {
		StringBuffer fileData = new StringBuffer();
		fileData.append(setField(new Integer(getRereshProperty("FC-HDR-REC-TYPE")).intValue(), "01", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FC-HDR-ACTION")).intValue(), "A", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FC-HDR-SOURCE-SYSTEM")).intValue(), "MCAS", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FC-HDR-DEST-SYSTEM")).intValue(), "BPWR", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FC-HDR-BRANCH-CODE")).intValue(), Branch, 'N'));
		fileData.append(setField(new Integer(getRereshProperty("FC-HDR-FILE-ID")).intValue(), "CL", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FC-HDR-PROCESS-DATE")).intValue(), strHdrDt, 'N'));
		fileData.append(setField(new Integer(getRereshProperty("FC-HDR-FILLER-1")).intValue(),
				CommonConstants.EMPTY_STRING, 'A'));
		fileData.append("\r\n");
		try {
			fout.write(fileData.toString().getBytes());
			fout.flush();
		} catch (IOException ioExcpn) {
			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
		}
	}

	/**
	 * formats and writes the customer trailer record to mcfc9999.dat
	 * 
	 * @param branchCtr
	 * @param fout
	 */
	private void formatCustomerTrail(String branchCtr, FileOutputStream fout, BankFusionEnvironment env) {
		StringBuffer fileData = new StringBuffer();
		fileData.append(setField(new Integer(getRereshProperty("FC-TRL-REC-TYPE")).intValue(), "99", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FC-TRL-RECORD-COUNT")).intValue(), branchCtr, 'N'));
		fileData.append(setField(new Integer(getRereshProperty("FC-TRL-FILLER-1")).intValue(),
				CommonConstants.EMPTY_STRING, 'A'));
		fileData.append("\r\n");
		try {
			fout.write(fileData.toString().getBytes());
			fout.flush();
			fout.close();
		}
		// Bug#7222
		catch (NullPointerException npExcpn) {
			return;
		}
		// Bug#7222
		catch (IOException ioExcpn) {
			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
		}
	}

	// bundlepricing-start

	/**
	 * Performs the Account bundle file refresh
	 * 
	 * @param env
	 * @
	 */
	private synchronized void refreshAccountBundle(BankFusionEnvironment env) {

		List AccountBundleDetails = null;
		AccountBundleDetails = env.getFactory().executeGenericQuery(queryAccountBundle, null, null);
		SimplePersistentObject accBunPO = null;
		FileOutputStream fout = null;
		// Bug#7222
		if (AccountBundleDetails.size() == 0)
			return;
		// Bug#7222
		int accbundCtr = 0;
		try {
			fout = new FileOutputStream(extractPath + "mcfbacct.dat");
			for (int i = 0; i < AccountBundleDetails.size(); i++) {
				accBunPO = (SimplePersistentObject) AccountBundleDetails.get(i);
				if (i == 0) {
					formatAccountBundleHeader(fout, env);
				}
				if (accBunPO.getDataMap().get(IBOAccountBundle.ACCOUNTID) != null) {

					fileData = new StringBuffer();
					fileData.append(
							setField(new Integer(getRereshProperty("FS-BUN-ACC-DTL-REC-TYPE")).intValue(), "02", 'N'));
					fileData.append(setField(new Integer(getRereshProperty("FS-BUN-ACCOUNT-NUM")).intValue(),
							(String) accBunPO.getDataMap().get(IBOAccountBundle.ACCOUNTID), 'A'));
					fileData.append(setField(new Integer(getRereshProperty("FS-BUN-ACC-ACC-STYLE")).intValue(),
							(String) accBunPO.getDataMap().get(IBOBundleDetails.ACCOUNTSTYLE), 'A'));
					// Start_0
					if (accBunPO.getDataMap().get(IBOAccountBundle.BUNDLECODE).toString().length() > 4)
						// fileData.append(setField(new
						// Integer(getRereshProperty("FS-BUN-ACC-BUND-CODE")).intValue(), (String)
						// accBunPO.getDataMap().get(IBOAccountBundle.BUNDLECODE), 'A'));
						fileData.append(setField(
								new Integer(getRereshProperty("FS-BUN-ACC-BUND-CODE")).intValue(), (String) accBunPO
										.getDataMap().get(IBOAccountBundle.BUNDLECODE).toString().substring(0, 3),
								'A'));
					else
						fileData.append(setField(new Integer(getRereshProperty("FS-BUN-ACC-BUND-CODE")).intValue(),
								(String) accBunPO.getDataMap().get(IBOAccountBundle.BUNDLECODE), 'A'));
					// End_0
					fileData.append(setField(new Integer(getRereshProperty("FS-BUN-ACC-UPPER-LIMIT")).intValue(),
							accBunPO.getDataMap().get(IBOBundleDetails.THRESHOLDTXNCOUNT).toString(), 'N'));
					fileData.append(setField(new Integer(getRereshProperty("FS-BUN-ACC-ACTIVE-FLAG")).intValue(),
							CommonConstants.EMPTY_STRING, 'N'));
					fileData.append(
							setField(new Integer(getRereshProperty("FS-BUN-ACC-DTL-ACTION")).intValue(), "C", 'A'));
					fileData.append(setField(new Integer(getRereshProperty("FS-BUN-ACC-DTL-FILLER")).intValue(),
							CommonConstants.EMPTY_STRING, 'A'));
					BigDecimal checksum = new BigDecimal(
							accBunPO.getDataMap().get(IBOBundleDetails.THRESHOLDTXNCOUNT).toString());
					checksum = checksum.setScale(0, BigDecimal.ROUND_CEILING);
					fileData.append(setField(new Integer(getRereshProperty("FS-BUN-ACC-DTL-CHECKSUM")).intValue(),
							checksum.abs().toString(), 'N'));
					fileData.append("\r\n");
					fout.write(fileData.toString().getBytes());
					fout.flush();
					accbundCtr++;

				}
			}
			formatAccountBundleTrail(String.valueOf(accbundCtr), fout, env);
			fout.close();
		} catch (FileNotFoundException fnfExcpn) {
			// BPR

			BranchPowerRefreshFatomContext.Status = Boolean.FALSE;

			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { fnfExcpn.getLocalizedMessage() }, new HashMap(), env);
		} catch (IOException ioExcpn) {

			BranchPowerRefreshFatomContext.Status = Boolean.FALSE;

			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
		} catch (NumberFormatException nfExcpn) {
			// BPR

			BranchPowerRefreshFatomContext.Status = Boolean.FALSE;

			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { nfExcpn.getLocalizedMessage() }, new HashMap(), env);
		}
		// BPR
		catch (Exception Excpn) {

			BranchPowerRefreshFatomContext.Status = Boolean.FALSE;
			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { Excpn.getLocalizedMessage() }, new HashMap(), env);
		}
		// BPR

	}

	/**
	 * formats and writes the Account Bundle file header to mcfbacct.dat
	 * 
	 * @param fout
	 */
	private void formatAccountBundleHeader(FileOutputStream fout, BankFusionEnvironment env) {
		StringBuffer fileData = new StringBuffer();
		fileData.append(setField(new Integer(getRereshProperty("FS-BUN-ACC-HDR-REC-TYPE")).intValue(), "01", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FS-BUN-ACC-HDR-FILE-ACTION")).intValue(), "A", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FS-BUN-ACC-HDR-FILE-ID")).intValue(), "BA", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FS-BUN-ACC-HDR-FILLER")).intValue(),
				CommonConstants.EMPTY_STRING, 'A'));
		fileData.append("\r\n");
		try {
			fout.write(fileData.toString().getBytes());
			fout.flush();
		} catch (IOException ioExcpn) {
			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
		}
	}

	/**
	 * formats and writes the Account Bundle file trailer to mcfbacct.dat
	 * 
	 * @param accbundCtr
	 * @param fout
	 */
	private void formatAccountBundleTrail(String accbundCtr, FileOutputStream fout, BankFusionEnvironment env) {
		StringBuffer fileData = new StringBuffer();
		fileData.append(setField(new Integer(getRereshProperty("FS-BUN-ACC-TRL-REC-TYPE")).intValue(), "99", 'A'));
		fileData.append(
				setField(new Integer(getRereshProperty("FS-BUN-ACC-TRL-REC-COUNT")).intValue(), accbundCtr, 'N'));
		fileData.append(setField(new Integer(getRereshProperty("FS-BUN-ACC-TRL-REC-FILLER")).intValue(),
				CommonConstants.EMPTY_STRING, 'A'));
		fileData.append("\r\n");
		try {
			fout.write(fileData.toString().getBytes());
			fout.flush();
			fout.close();
		}
		// Bug#7222
		catch (NullPointerException npExcpn) {
			return;
		}
		// Bug#7222
		catch (IOException ioExcpn) {
			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
		}
	}

	/**
	 * Performs the Transaction bundle file refresh
	 * 
	 * @param env
	 * @
	 */
	private synchronized void refreshTransBundle(BankFusionEnvironment env) {

		List TransBundleDetails = null;
		TransBundleDetails = env.getFactory().executeGenericQuery(queryTransBundle, null, null);
		SimplePersistentObject transBunPO = null;
		FileOutputStream fout = null;
		// 7222
		if (TransBundleDetails.size() == 0)
			return;
		// 7222
		int transbundCtr = 0;
		try {
			fout = new FileOutputStream(extractPath + "mcfbtrans.dat");
			for (int i = 0; i < TransBundleDetails.size(); i++) {
				transBunPO = (SimplePersistentObject) TransBundleDetails.get(i);
				if (i == 0) {
					formatTransBundleHeader(fout, env);
				}
				if (transBunPO.getDataMap().get(IBOBundleDetails.BUNDLECODE) != null) {
					// Start_0
					if (transBunPO.getDataMap().get(IBOBundleDetailsTxnCodeMap.TXNCODE).toString().length() == 2) {
						// End_0
						fileData = new StringBuffer();
						fileData.append(setField(new Integer(getRereshProperty("FS-BUN-TXN-DTL-REC-TYPE")).intValue(),
								"02", 'N'));
						// Start_0
						if (transBunPO.getDataMap().get(IBOBundleDetailsTxnCodeMap.BUNDLECODE).toString().length() > 4)
							// fileData.append(setField(new
							// Integer(getRereshProperty("FS-BUN-BUND-CODE")).intValue(), (String)
							// transBunPO.getDataMap().get(IBOBundleDetailsTxnCodeMap.BUNDLECODE), 'A'));
							fileData.append(setField(new Integer(getRereshProperty("FS-BUN-BUND-CODE")).intValue(),
									(String) transBunPO.getDataMap().get(IBOBundleDetailsTxnCodeMap.BUNDLECODE)
											.toString().substring(0, 3),
									'A'));
						else
							fileData.append(setField(new Integer(getRereshProperty("FS-BUN-BUND-CODE")).intValue(),
									(String) transBunPO.getDataMap().get(IBOBundleDetailsTxnCodeMap.BUNDLECODE), 'A'));
						// End_0
						fileData.append(setField(new Integer(getRereshProperty("FS-BUN-TXN-TYPE")).intValue(),
								transBunPO.getDataMap().get(IBOBundleDetailsTxnCodeMap.TXNCODE).toString(), 'A'));

						fileData.append(setField(new Integer(getRereshProperty("FS-BUN-TXN-WTYPE")).intValue(),
								transBunPO.getDataMap().get(IBOBundleDetailsTxnCodeMap.CHARGELEG).toString(), 'A'));
						fileData.append(
								setField(new Integer(getRereshProperty("FS-BUN-TXN-DTL-ACTION")).intValue(), "C", 'A'));
						fileData.append(setField(new Integer(getRereshProperty("FS-BUN-TXN-DTL-FILLER")).intValue(),
								CommonConstants.EMPTY_STRING, 'A'));
						fileData.append(setField(new Integer(getRereshProperty("FS-BUN-TXN-DTL-CHECKSUM")).intValue(),
								CommonConstants.EMPTY_STRING, 'N'));

						fileData.append("\r\n");
						fout.write(fileData.toString().getBytes());
						fout.flush();
						transbundCtr++;
						// Start_0
					}
					// End_0
				}
			}
			formatTransBundleTrail(String.valueOf(transbundCtr), fout, env);
			fout.close();
		} catch (FileNotFoundException fnfExcpn) {
			// BPR

			BranchPowerRefreshFatomContext.Status = Boolean.FALSE;

			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { fnfExcpn.getLocalizedMessage() }, new HashMap(), env);
		} catch (IOException ioExcpn) {
			// BPR

			BranchPowerRefreshFatomContext.Status = Boolean.FALSE;

			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
		} catch (NumberFormatException nfExcpn) {
			// BPR

			BranchPowerRefreshFatomContext.Status = Boolean.FALSE;

			// BPR
			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { nfExcpn.getLocalizedMessage() }, new HashMap(), env);
		}
		// BPR
		catch (Exception Excpn) {

			BranchPowerRefreshFatomContext.Status = Boolean.FALSE;
			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { Excpn.getLocalizedMessage() }, new HashMap(), env);
		}
		// BPR

	}

	/**
	 * formats and writes the Transaction Bundle file header to mcfbtrans.dat
	 * 
	 * @param fout
	 */
	private void formatTransBundleHeader(FileOutputStream fout, BankFusionEnvironment env) {
		StringBuffer fileData = new StringBuffer();
		fileData.append(setField(new Integer(getRereshProperty("FS-BUN-TRN-HDR-REC-TYPE")).intValue(), "01", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FS-BUN-TRN-HDR-FILE-ACTION")).intValue(), "A", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FS-BUN-TRN-HDR-FILE-ID")).intValue(), "BT", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FS-BUN-TRN-HDR-FILLER")).intValue(),
				CommonConstants.EMPTY_STRING, 'A'));
		fileData.append("\r\n");
		try {
			fout.write(fileData.toString().getBytes());
			fout.flush();
		} catch (IOException ioExcpn) {
			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
		}
	}

	/**
	 * formats and writes the Transaction Bundle file trailer to mcfbtrans.dat
	 * 
	 * @param transCtr
	 * @param fout
	 */
	private void formatTransBundleTrail(String transCtr, FileOutputStream fout, BankFusionEnvironment env) {
		StringBuffer fileData = new StringBuffer();
		fileData.append(setField(new Integer(getRereshProperty("FS-BUN-TRN-TRL-REC-TYPE")).intValue(), "99", 'A'));
		fileData.append(setField(new Integer(getRereshProperty("FS-BUN-TRN-TRL-REC-COUNT")).intValue(), transCtr, 'N'));
		fileData.append(setField(new Integer(getRereshProperty("FS-BUN-TRN-TRL-FILLER")).intValue(),
				CommonConstants.EMPTY_STRING, 'A'));
		fileData.append("\r\n");
		try {
			fout.write(fileData.toString().getBytes());
			fout.flush();
			fout.close();
		}
		// Bug#7222
		catch (NullPointerException npExcpn) {
			return;
		}
		// Bug#7222
		catch (IOException ioExcpn) {
			EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
					new Object[] { ioExcpn.getLocalizedMessage() }, new HashMap(), env);
		}
	}

	// bundlepricing-end

	/**
	 * reads the property for the key passed
	 * 
	 * @param sKey
	 * @return
	 */
	private static String getBPRereshProperty(String sKey) {
		String sValue = fileBPRefreshProp.get(sKey).toString();
		return sValue;
	}

	/**
	 * reads the property for the key passed
	 * 
	 * @param sKey
	 * @return
	 */
	private static String getRereshProperty(String sKey) {
		String sValue = fileRefreshProp.get(sKey).toString();
		return sValue;
	}

	/**
	 * This method formats fields input using the type ('A' or 'N') and length
	 * values passed. returns the formatted string back to calling method
	 * 
	 * @param ind
	 * @param string
	 * @param type
	 * @return
	 */
	private static String setField(int ind, String string, char type) {
		int count = 0;
		if (null != string) {
			count = string.length();
		}
		final StringBuffer sbuff = new StringBuffer();
		if (type == 'A') {
			sbuff.append(string);
		}
		for (int index = count; index < ind; index++) {
			if (type == 'A') {
				sbuff.append(" ");
			} else {
				sbuff.append("0");
			}
		}
		if (type == 'N') {
			sbuff.append(string);
		}
		return sbuff.toString();
	}

	/**
	 * This method will be called by the Batch Framework when there is an exception
	 * caught and the process page is marked as failed. The Batch Framework creates
	 * a private Session and the factory is passed to this method so that the values
	 * get committed.
	 * 
	 * @param unprocessedIds
	 *            - List of Unprocessed IDs
	 * @param failedId
	 *            - Failed Account Number
	 * @param exception
	 *            - Exception Object
	 * @param factory
	 *            - Instance of persistenceObjectFactory created as a private
	 *            Session.
	 * 
	 */
	public void logException(List<String> unprocessedIds, String failedId, Exception exception,
			IPersistenceObjectsFactory factory) {

		Iterator unprocessedIter = unprocessedIds.iterator();
		String status = CommonConstants.EMPTY_STRING;
		String message = CommonConstants.EMPTY_STRING;
		while (unprocessedIter.hasNext()) {
			IBOBranch branchItem = (IBOBranch) unprocessedIter.next();

			String key = branchItem.getBoID();
			if (key.equalsIgnoreCase(failedId)) {
				status = "E";
				message = exception.getMessage();
			} else {
				status = "U";
			}
			createLogMessage(key, message, status, factory);
		}
	}

	/**
	 * This method is used to create log error message
	 *
	 * @param key
	 * @param message
	 * @param status
	 */
	private void createLogMessage(String key, String message, String status, IPersistenceObjectsFactory factory) {
		IBOUB_CMN_BatchProcessLog batchException = (IBOUB_CMN_BatchProcessLog) factory
				.getStatelessNewInstance(IBOUB_CMN_BatchProcessLog.BONAME);
		batchException.setBoID(GUIDGen.getNewGUID());
		batchException.setF_PROCESSNAME(this.context.getBatchProcessName());

		batchException.setF_RUNDATETIME(
				SystemInformationManager.getInstance().getBFBusinessDateTime(environment.getRuntimeMicroflowID()));
		batchException.setF_RECORDID(key);

		if (status.equalsIgnoreCase("E") || status.equalsIgnoreCase("W")) {
			if (logger.isErrorEnabled()) {
				logger.error("Error processing for Account [ " + key + " ] Reason :- " + message);
			}
			if (null == message) {
				message = CommonConstants.EMPTY_STRING;
			}
			message = message.replaceAll(",", "");
			message = message.replaceAll(":", "");
			message = message.replaceAll("':", "");

			batchException.setF_ERRORMESSAGE(message);
			batchException.setF_STATUS(status);
		} else {
			if (logger.isInfoEnabled()) {
				logger.info("Unprocessed Account [ " + key + " ] ");
			}
			batchException.setF_STATUS(status);
		}
		factory.create(IBOUB_CMN_BatchProcessLog.BONAME, batchException);
	}

}
