/* ********************************************************************************
 *  Copyright (c) 2002,2004 Trapedza Financial Systems Ltd. All Rights Reserved.
 */
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.ub.forex.core.ForexConstants;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOCurrency;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOTradeFinanceTxnAudit;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.BusinessValidatorBean;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.features.AccountLimitFeature;
import com.trapedza.bankfusion.features.LimitsFeature;
import com.trapedza.bankfusion.features.extensionpoints.PasswordProtectedConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.IsWorkingDay;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.NextWorkingDateForDate;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.PreviousWorkingDateForDate;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractTradeFinanceValidate;
import com.trapedza.bankfusion.steps.refimpl.ITradeFinanceValidate;
import com.trapedza.bankfusion.utils.BankFusionMessages;
import com.trapedza.bankfusion.utils.GUIDGen;


/**
 * 
 * Validate IBSnet Message fields and UB Business validations.
 * 
 * @author Anand Khamitkar
 * @created Feb 12, 2008 5:35:17 PM
 * 
 */
public class TradeFinanceValidate extends AbstractTradeFinanceValidate
    implements ITradeFinanceValidate {
  /**
   * <code>svnRevision</code> = $Revision: 1.0 $
   */
  public static final String svnRevision = "$Revision: 1.0 $";
  static {
    com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
  }

  /**
   */

  /**
   * logger instance
   */
  private transient final static Log LOGGER = LogFactory
      .getLog(TradeFinanceValidate.class.getName());

  /**
   * Find by query using ISOCURRENCYCODE on currency table
   */
  private static final String findbyISOCurrencyCode = " WHERE "
      + IBOCurrency.ISOCURRENCYCODE + " = ?";

  /**
   * Find by query using NUMERICTRANSCODE on MisTransactionCodes table
   */
  private static final String findbyNumbericTransCode = " WHERE "
      + IBOMisTransactionCodes.NUMERICTRANSCODE + " = ?";

  /**
   * Find by query using ACCOUNTNUMBER and TRANSACTIONREFERENCE on
   * MisTransactionCodes table
   */
  private static final String findbyAccIdAndTxn = " WHERE "
      + IBOTradeFinanceTxnAudit.ACCOUNTNUMBER + " = ? AND "
      + IBOTradeFinanceTxnAudit.TRANSACTIONREFERENCE + " = ?";

  /**
   * boolean instance
   */
  private static boolean TRUEFLAG = true;

  /**
   * boolean instance
   */
  private static boolean FALSEFLAG;

  /**
   * This holds the GUI ID, to be set on the TradeFinanceTxnAudit table.
   */
  private String txnID = CommonConstants.EMPTY_STRING;

  /**
   * detailed errorcodes for invalid data.
   */
  private static String INVALID_CURRENCY = "001";
  private static String INVALID_ACCOUN_NO = "002";
  private static String INVALID_TRANSACTION_CODE = "003";
  private static String INVALID_EXCHANGE_RATE = "004";
  private static String INVALID_BASE_EQUALANT = "005";
  private static String INVALID_DATE_FORMAT = "006";
  private static String INVALID_M_D_FLAG = "007";
  private static String INVALID_SIGN = "008";
  private static String INVALID_AMOUNT = "009";
  private static String DEBIT_POSTINGS_NOT_ALLOWED = "010";
  private static String CREDIT_POSTINGS_NOT_ALLOWED = "011";
  private static String ACCOUNT_CLOSED = "012";
  private static String LIMIT_EXEEDS = "013";
  private static String EXCHANGE_RATE_NOT_IN_TOLERANCE = "014";

  /**
   * TradeFinanceValidate consturactor
   * 
   * @param env
   */
  public TradeFinanceValidate(BankFusionEnvironment env) {
    super(env);
  }

  /*
   * These status code are taken from Bank Master. 0 - Message processed
   * successfully, no further action required. 1 - Message is not
   * required(Currently used for duplicate transactions). 2 - Message is to be
   * resent immediately by the Store and Forward as a). a locking error was
   * encountered in the TPM. // Not currently used in UB 3 - Message is
   * invalid and will be output to the Invalid Message File. 4 - Posted into
   * Suspense account 5,6,7 - Not currently used. 8 - Message could not be
   * processed by the Translator due to invalid fields or Incomplete setup.
   * The Message is to be output to the Retry file, to be resent at the End of
   * Day. 9 - Message could not be processed due to a Fatal Error, either in
   * the Translator or the TPM. The Message is to be output to the Retry file,
   * to be resent at End of Day.
   */

  /**
   * main process mathed
   * 
   * @param env
   */
  public void process(BankFusionEnvironment env) {

    BigDecimal transAmt = null;
    BigDecimal exchangeRt = null;
    BigDecimal baseEqulival = null;

    String userId = env.getUserID();
    txnID = GUIDGen.getNewGUID();
    LOGGER.info("GUIDGen txnID: " + txnID);
    if (txnID.length() >= 30) {
      txnID = txnID.substring(0, 30);
    }
    setF_OUT_TXNGUID(txnID);
    String runtimeBPID = env.getRuntimeMicroflowID();
    Timestamp toDay = SystemInformationManager.getInstance()
        .getBFBusinessDateTime(runtimeBPID);

    String accountID = getF_IN_ACCOUNT_NUMBER();

    String baseEquivalent = getF_IN_BASE_EQUIVALENT();
    // not used fields
    String nominalValueOfBill = getF_IN_NOMINAL_VALUE_OF_BILL();
    String interestRate = getF_IN_INTEREST_RATE();
    String signOfNominalAmount = getF_IN_SIGN_OF_NOMINAL_AMOUNT();

    String transactionReference = getF_IN_TRANSACTION_REFERENCE();
    String baseEquivalentSign = getF_IN_BASE_EQUIVALENT_SIGN();
    String exchangeRate = getF_IN_EXCHANGE_RATE();
    String isoCurrencyCode = getF_IN_ISO_CURRENCY_CODE();
    String maturityDate = getF_IN_MATURITY_DATE();
    String multipyDevideFlag = getF_IN_MULTIPLY_DIVIDE_FLAG();
    String signOfAmount = getF_IN_SIGN_OF_AMOUNT();
    String transactionAmount = getF_IN_TRANSACTION_AMOUNT();
    Integer transactionType = getF_IN_TRANSACTON_TYPE();
    String valueDate = getF_IN_VALUE_DATE();

    setF_OUT_TRANS_STATUS(new Integer(0));
    setF_OUT_AUTHORISED_FLG("0");
    setF_OUT_USER_ID(userId);

    // *****************************IBSNet Translator Validation
    // Section*******************************

    if (isoCurrencyCode == null
        || isoCurrencyCode.equals(CommonConstants.EMPTY_STRING)
        || isoCurrencyCode.length() < 0) {
      setMessageStatus(new String[] { isoCurrencyCode }, "3", 40000266,
          INVALID_CURRENCY);
    }

    isCurrencyExist(isoCurrencyCode, env);
    
    int currencyScale = getCurrencyScaleFromCurrency(isoCurrencyCode, env);

    // changes for bug 36044 starts
    if (getCurrencyNumericCode(isoCurrencyCode, env) != null) {
      accountID = getCurrencyNumericCode(isoCurrencyCode, env)
          + accountID;
    }
    // changes for bug 36044 ends

    if (accountID == null || accountID.equals(CommonConstants.EMPTY_STRING)) {
      setMessageStatus(new String[] { accountID }, "3", 40000126,
          INVALID_ACCOUN_NO);
    }

    if (transactionType == null
        || transactionType.equals(CommonConstants.EMPTY_STRING)) {
      setMessageStatus(new String[] { CommonConstants.EMPTY_STRING
          + transactionType }, "3", 40409304,
          INVALID_TRANSACTION_CODE);
    }

    validateMISNumberTransCode(transactionType, env);

    setF_OUT_ACCOUNT_NUMBER(accountID);

    transAmt = validateSwiftAmount("TransactionAmount", transactionAmount,
        currencyScale, env);

    setF_OUT_TRANSACTION_AMOUNT(transAmt);

    validateSignOfAmount("SignOfAmount", signOfAmount);

    setF_OUT_VALUE_DATE(checkForDate(valueDate, toDay, "V"));

    if (maturityDate != null && maturityDate.length() > 0) {
      Date maturityDt = checkWorkingDay(
          (checkForDate(maturityDate, toDay, "M")), env);
      setF_OUT_MATURITY_DATE(maturityDt);
    } else {
      setF_OUT_MATURITY_DATE(new Date(0L));
    }

    setF_OUT_INTEREST_RATE(validateRate(interestRate));// not used currently

    exchangeRt = validateRate(exchangeRate);

    setF_OUT_EXCHANGE_RATE(exchangeRt);

    baseEqulival = validateSwiftAmount("BaseEquivalent", baseEquivalent,
        currencyScale, env);

    setF_OUT_BASE_EQUIVALENT(baseEqulival);

    validateBaseEquivalentSign("BaseEquivalentSign", baseEquivalentSign);

    validateMultipyDevideFlag(multipyDevideFlag);

    validateSignOfNominalAmount(signOfNominalAmount);// not used
                              // currently
    // Changes stared for artf36059
    if (exchangeRt != null && multipyDevideFlag != null && multipyDevideFlag.equals("M")) {
      BigDecimal totalamt = transAmt.multiply(exchangeRt);
      if ((totalamt.compareTo(baseEqulival)) == 0) {
        setF_OUT_BASE_EQUIVALENT(baseEqulival);
      } else {
        setMessageStatus(new String[] { baseEqulival.toString() }, "3",
            40009249, INVALID_BASE_EQUALANT);
      }

    } else if (exchangeRt != null && multipyDevideFlag != null && multipyDevideFlag.equals("D")) {
      BigDecimal totalamt = transAmt.divide(exchangeRt);
      if ((totalamt.compareTo(baseEqulival)) == 0) {
        setF_OUT_BASE_EQUIVALENT(baseEqulival);
      } else {
        setMessageStatus(new String[] { baseEqulival.toString() }, "3",
            40009249, INVALID_BASE_EQUALANT);
      }
    }
    // Changes ended for artf36059

    setF_OUT_NOMINAL_VALUE_OF_BILL(validateSwiftAmount(
        "NominalValueofBill", nominalValueOfBill, currencyScale, env));
    // setF_OUT_ISSUE_DATE(checkForDate(issueDate,systemDate,CommonConstants.EMPTY_STRING));//not
    // used currently

    // *************************************Business Validation
    // Section***********************************

    if (isAccountExist(accountID, env)) {

      // Check for insufficient fund
      // insufficientFunds(accountID,transAmt,env);
      // check for account closed,stopped,dormant
      isAccountStatus(accountID, env);

      checkAccountLimits(isoCurrencyCode, accountID, transAmt,
          signOfAmount, env);

      checkMultiCurrencyValidity(isoCurrencyCode, transAmt, exchangeRt,
          baseEqulival, multipyDevideFlag, env);

      debitPostingAllowed(accountID, env);

      creditPostingAllowed(accountID, env);

      checkForDuplicateTxn(accountID, transactionReference, env);
    } else {
      setMessageStatus(new String[] { accountID }, "3", 40000126,
          INVALID_ACCOUN_NO);
    }
  }

  /**
   * Check for the ISO curruncy existance in UB
   * 
   * @param currency
   * @param env
   */
  private void isCurrencyExist(String currency, BankFusionEnvironment env) {
        String numCurrencyCode;
        try {
            if (StringUtils.isEmpty(currency) || StringUtils.isNumeric(currency)) {
                setMessageStatus(new String[] { currency }, "3", 40205007, INVALID_CURRENCY);
                return;
            }
            numCurrencyCode = SystemInformationManager.getInstance().transformCurrencyCode(currency, false);
            if (StringUtils.isEmpty(numCurrencyCode)) {
                setMessageStatus(new String[] { currency }, "3", 40205007, INVALID_CURRENCY);
            }
        } catch (BankFusionException e) {
            setMessageStatus(new String[] { currency }, "3", 40205007, INVALID_CURRENCY);
            LOGGER.error(ExceptionUtil.getExceptionAsString(e));
        }
  }

  /**
   * Checks whether the Debit postings are allowed on the given A/C
   * 
   * @param account
   * @return boolean
   */
  public boolean debitPostingAllowed(String accountNumber,
      BankFusionEnvironment env) {
    boolean valid = FALSEFLAG;
    try {
      IBOAccount accountObj = (IBOAccount) env.getFactory()
          .findByPrimaryKey(IBOAccount.BONAME, accountNumber);
      if (accountObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.DEBITS_NOT_ALLOWED) {
        valid = TRUEFLAG;
        setMessageStatus(new String[] { accountNumber }, "8", 40000405,
            DEBIT_POSTINGS_NOT_ALLOWED);
      }
    } catch (BankFusionException e) {
      LOGGER.error("Debit postings are allowed on the given A/C no. "
          + accountNumber);
      LOGGER.error(ExceptionUtil.getExceptionAsString(e));
    }
    return valid;
  }

  /**
   * Checks whether the Credit postings are allowed on the given A/C
   * 
   * @param account
   * @return boolean
   */
  public boolean creditPostingAllowed(String accountNumber,
      BankFusionEnvironment env) {
    boolean valid = TRUEFLAG;
    try {
      IBOAccount accountObj = (IBOAccount) env.getFactory()
          .findByPrimaryKey(IBOAccount.BONAME, accountNumber);
      if (accountObj.getF_ACCRIGHTSINDICATOR() == PasswordProtectedConstants.CREDITS_NOT_ALLOWED) {
        valid = FALSEFLAG;
        setMessageStatus(new String[] { accountNumber }, "8", 40000406,
            CREDIT_POSTINGS_NOT_ALLOWED);
      }
    } catch (BankFusionException e) {
      LOGGER.error("Credit postings are allowed on the given A/C no. "
          + accountNumber);
      LOGGER.error(ExceptionUtil.getExceptionAsString(e));
    }
    return valid;
  }

  /**
   * IBSNet date format is 020306 (yymmdd) needs to be converted into UB
   * format i.e. 1970-01-01 (yyyy-mm-dd)
   * 
   * @param ibsdate
   * @param systemDate
   * @param checkMaturityDt
   * @return
   */
  private Date checkForDate(String ibsdate, Timestamp systemDate,
      String typeofDt) {
    if (ibsdate != null && !ibsdate.equals(CommonConstants.EMPTY_STRING)) {
      int mm = Integer.parseInt(ibsdate.substring(2, 4));

      int dd = Integer.parseInt(ibsdate.substring(4, 6));

      if (mm > 12 || dd > 31) {
        setMessageStatus(new String[] { ibsdate }, "3", 40409313,
            INVALID_DATE_FORMAT);
        return null;
      }

      int yy = Integer.parseInt(ibsdate.substring(0, 2));
      Calendar cal = Calendar.getInstance();

      if (yy >= 70 && yy <= 99) {
        cal.set(Integer.parseInt("19" + yy), mm - 1, dd);
      } else if (yy >= 00 && yy <= 70) {
        String dateStr = new String(CommonConstants.EMPTY_STRING + yy);
        if (dateStr.length() == 1) {
          cal.set(Integer.parseInt("200" + yy), mm - 1, dd);
        } else {
          cal.set(Integer.parseInt("20" + yy), mm - 1, dd);
        }
      }
      if (typeofDt.equals("V")) {
        Date toDay = SystemInformationManager.getInstance()
            .getBFBusinessDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(systemDate.getTime());
        Calendar newCal = Calendar.getInstance();
        newCal.set(calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DATE));
        toDay = new Date(newCal.getTimeInMillis());
        Date toDate = new Date(cal.getTimeInMillis());
        LOGGER.info("Value Date: " + new Date(cal.getTimeInMillis())
            + " today's Date: " + toDay);

        // if (new java.sql.Date(cal.getTimeInMillis()).compareTo(toDay)
        // < 0) {

        if (getDateWithZeroTime(toDate).compareTo(
            getDateWithZeroTime(toDay)) < 0) {
          setMessageStatus(
              new Object[] { CommonConstants.EMPTY_STRING
                  + new Date(cal.getTimeInMillis()) }, "3",
              40409314, INVALID_DATE_FORMAT);
        }
      }
      return new Date(cal.getTimeInMillis());
    } else {
      if (typeofDt.equals("M")) {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
            cal.get(Calendar.DATE) + 1);
        return new Date(cal.getTimeInMillis());
      }
      return new Date(systemDate.getTime());
    }
  }

  /**
   * UB Exchange rate format is 7,8
   * 
   * @param rate
   * @return
   */
  private BigDecimal validateRate(String rate) {
    BigDecimal actualrate = CommonConstants.BIGDECIMAL_ZERO;
    if (rate != null && !rate.equals(CommonConstants.EMPTY_STRING)) {
      try {
        if (rate.indexOf(",") != -1) {
          int beforeComma = rate.indexOf(",");

          String beforeCommaStr = null;
          String afterCommaStr = rate.substring(beforeComma + 1,
              rate.length());
          if (beforeComma <= 7) {
            beforeCommaStr = rate.substring(0, beforeComma);
          } else {
            beforeCommaStr = rate.substring(0, 7);
          }

          if (afterCommaStr.length() <= 8) {
            int count = 8 - afterCommaStr.length();
            for (int i = 1; i <= count; i++) {
              afterCommaStr += 0;
            }
          } else {
            if (LOGGER.isInfoEnabled())
              LOGGER.info(afterCommaStr);
          }
          actualrate = new BigDecimal(beforeCommaStr + "."
              + afterCommaStr);
          return actualrate;
        } else {
          /*
           * actualrate= new BigDecimal(rate+"."+0);
           * logger.info("Rate value is "+actualrate); return
           * actualrate;
           */
          setMessageStatus(new String[] { rate }, "3", 40000266,
              INVALID_EXCHANGE_RATE);
        }
      } catch (Exception e) {
        setMessageStatus(new String[] { rate }, "9", 40000266,
            CommonConstants.EMPTY_STRING);
        LOGGER.error(ExceptionUtil.getExceptionAsString(e));
      }
    }
    LOGGER.info("Rate value is " + actualrate);
    return actualrate;
  }

  /**
   * Validate BaseEquivalentSign field for sign '+' or '-'
   * 
   * @param baseEquivalentSign
   * @param env
   *            @
   */
  private void validateBaseEquivalentSign(String fieldName,
      String baseEquivalentSign) {
    if (baseEquivalentSign.equals("+")) {
      setF_OUT_BASE_EQUIVALENT_SIGN("C");
    } else if (baseEquivalentSign.equals("-")) {
      setF_OUT_BASE_EQUIVALENT_SIGN("D");
    } else {
      errorHandler(fieldName, baseEquivalentSign);
    }
  }

  /**
   * Validate SignOfAmount field for sign '+' or '-'
   * 
   * @param signOfAmount
   * @param env
   *            @
   */
  private void validateSignOfAmount(String fieldName, String signOfAmount) {
    if (signOfAmount.equals("+")) {
      setF_OUT_SIGN_OF_AMOUNT("C");
      setF_OUT_SIGN_FOR_POSTINGACTION("D");
    } else if (signOfAmount.equals("-")) {
      setF_OUT_SIGN_OF_AMOUNT("D");
      setF_OUT_SIGN_FOR_POSTINGACTION("C");
    } else {
      errorHandler(fieldName, signOfAmount);
    }
  }

  /**
   * Validate SignOfNominalAmount field for sign '+' or '-'
   * 
   * @param signOfNominalAmount
   * @param env
   *            @
   */
  private void validateSignOfNominalAmount(String signOfNominalAmount) {
    if (signOfNominalAmount.equals("+")) {
      setF_OUT_BASE_EQUIVALENT_SIGN("C");
    } else if (signOfNominalAmount.equals("-")) {
      setF_OUT_BASE_EQUIVALENT_SIGN("D");
    }
    // errorHandler(signOfNominalAmount,env);
  }

  /**
   * Validate MultipyDevideFlag field for value 'M' or 'D' or '<space>'
   * 
   * @param multipyDevideFlag
   * @param env
   *            @
   */
  private void validateMultipyDevideFlag(String multipyDevideFlag) {
    if (multipyDevideFlag != null
        && !multipyDevideFlag.equals(CommonConstants.EMPTY_STRING)
        && !multipyDevideFlag.equalsIgnoreCase("D")
        && !multipyDevideFlag.equalsIgnoreCase("M")
        && !multipyDevideFlag.equalsIgnoreCase(" ")) {
      setMessageStatus(new String[] { multipyDevideFlag }, "3", 40409301,
          INVALID_M_D_FLAG);
    }
  }

  /**
   * Common error handler
   * 
   * @param sign
   * @param env
   */
  private void errorHandler(String fieldName, String sign) {
    if (sign != null && !sign.equals(CommonConstants.EMPTY_STRING)) {
      if (!sign.equals("+") && !sign.equals("-")) {
        setMessageStatus(new String[] { fieldName }, "3", 40409302,
            INVALID_SIGN);
      }
    } else {
      if (!fieldName.equalsIgnoreCase("BaseEquivalentSign")) {
        setMessageStatus(new String[] { fieldName }, "3", 40409302,
            INVALID_SIGN);
      }
    }
  }

  /**
   * Validate swift amount fields
   * 
   * @param amount
   * @param currencyScale
   * @param env
   * @return @
   */
  public BigDecimal validateSwiftAmount(String fieldName, String amount,
      int currencyScale, BankFusionEnvironment env) {
    if (amount != null && !amount.equals(CommonConstants.EMPTY_STRING)) {
      // return checkForSwiftFormat(amount, currencyScale, env);
      // artf41436. consider decimals while comparing base currency amount
      // and transaction
      // amount.
      if (amount.indexOf(",") != -1) {
        String beforeComaVal = amount.substring(0, amount.indexOf(","));
        String afterComaVal = amount.substring(amount.indexOf(",") + 1,
            amount.length());
        String txnAmt = beforeComaVal + "." + afterComaVal;
        LOGGER.info("SWIFT amount : " + new BigDecimal(txnAmt));
        return new BigDecimal(txnAmt);
      } else {
        setMessageStatus(new String[] { fieldName }, "3", 40000266,
            INVALID_AMOUNT);
      }
    } else {
      if (!fieldName.equals("BaseEquivalent")
          && !fieldName.equals("NominalValueofBill")) {
        setMessageStatus(new String[] { fieldName, amount }, "3",
            40409303, INVALID_AMOUNT);
      }
    }
    return CommonConstants.BIGDECIMAL_ZERO;
  }

  /**
   * check for swift format
   * 
   * @param amount
   * @param currencyScale
   * @param env
   * @return @
   */
  public BigDecimal checkForSwiftFormat(String amount, int currencyScale,
      BankFusionEnvironment env) {
    String txnAmount = amount;
    String txnAmt = null;
    try {
      if (txnAmount.indexOf(",") != -1) {
        String beforeComaVal = txnAmount.substring(0,
            txnAmount.indexOf(","));
        String afterComaVal = txnAmount.substring(
            txnAmount.indexOf(",") + 1, txnAmount.length());
        if (afterComaVal.length() >= currencyScale) {
          String actualCurrScale = afterComaVal.substring(0,
              currencyScale);
          txnAmt = beforeComaVal + "." + actualCurrScale;
          LOGGER.info("SWIFT amount : " + new BigDecimal(txnAmt));
          return new BigDecimal(txnAmt);
        } else {
          return new BigDecimal(beforeComaVal);
        }
      } else {
        LOGGER.info("SWIFT amount : " + new BigDecimal(txnAmount));
        setMessageStatus(new String[] { amount }, "3", 40409303,
            INVALID_AMOUNT);
      }
    } catch (Exception e) {
      setMessageStatus(new String[] {}, "9", 40409315,
          CommonConstants.EMPTY_STRING);
      LOGGER.error(ExceptionUtil.getExceptionAsString(e));
    }
    return CommonConstants.BIGDECIMAL_ZERO;
  }

  /**
   * get currencyScale field from currency BO
   * 
   * @param currencyCode
   * @param env
   * @return
   */
  private String validateMISNumberTransCode(Integer transactionType,
      BankFusionEnvironment env) {

    IBOMisTransactionCodes misTransactionCodesBO = null;
    String code = null;
    try {
      ArrayList params = new ArrayList();
      params.add(transactionType);
      @SuppressWarnings("FBPE")
      ArrayList misList = (ArrayList) env.getFactory().findByQuery(
          IBOMisTransactionCodes.BONAME, findbyNumbericTransCode,
          params, null);
      if (!misList.isEmpty() && misList.get(0) != null) {
        misTransactionCodesBO = (IBOMisTransactionCodes) misList.get(0);
        code = misTransactionCodesBO.getBoID();
        if (code == null) {
          setMessageStatus(
              new String[] { CommonConstants.EMPTY_STRING
                  + transactionType }, "3", 40409309,
              INVALID_TRANSACTION_CODE);
        }

      } else {
        setMessageStatus(new String[] { CommonConstants.EMPTY_STRING
            + transactionType }, "3", 40409309,
            INVALID_TRANSACTION_CODE);
      }
    } catch (BankFusionException e) {
      setMessageStatus(new String[] { CommonConstants.EMPTY_STRING
          + transactionType }, "9", 40409309,
          CommonConstants.EMPTY_STRING);
      LOGGER.error(ExceptionUtil.getExceptionAsString(e));
    }
    return code;
  }

  /**
   * Find the currency scal from the currency
   * 
   * @param currencyCode
   * @param env
   * @return
   */
  private int getCurrencyScaleFromCurrency(String currencyCode,
      BankFusionEnvironment env) {

    int currencyScale = 0;
    try {
        currencyScale=SystemInformationManager.getInstance().getCurrencyScale(currencyCode);
    } catch (BankFusionException e) {
        LOGGER.error(ExceptionUtil.getExceptionAsString(e));
    }
    return currencyScale;
  }

  /**
   * get Numeric Code field from currency BO
   * 
   * @param currencyCode
   * @param env
   * @return
   */
  private String getCurrencyNumericCode(String currencyCode,
      BankFusionEnvironment env) {
    String numericCodeVal = null;
    int numericCode = 0;
    try {
      numericCode = Integer.parseInt(SystemInformationManager.getInstance().transformCurrencyCode(currencyCode, false));
      numericCodeVal = CommonConstants.EMPTY_STRING + numericCode;
      if (numericCodeVal.length() == 1) {
          numericCodeVal = "0" + numericCodeVal;
      }
    } catch (BankFusionException e) {
        LOGGER.error(ExceptionUtil.getExceptionAsString(e));
    }
    return numericCodeVal;
  }

  /**
   * Check for working day
   * 
   * @param maturityDate
   * @param env
   * @return
   */
  private Date checkWorkingDay(Date maturityDate, BankFusionEnvironment env) {
    Date newDate = null;
    String mode = getModuleConfigForIBSNet("IBSNet", "WDProcessing", env);
    // 2 ? Process before holidays and value date on processing date.
    // 3 ? Process before holidays and value date on scheduled date
    // 4 ? Process after holidays and value date on processing date
    // 5 ? Process after holidays and value date on scheduled date.

    // Boolean isWorking= IsWorkingDay.run("BRANCH", "23-00-01", new
    // Integer( 0 ), getF_IN_REQUIREDDATETIME(), env);
    // Boolean isWorking= IsWorkingDay.run("BRANCH", env.getUserBranch(),
    // new Integer( 0 ), getF_IN_REQUIREDDATETIME(), env);
    // Boolean isWorking= IsWorkingDay.run("COUNTRY", "IRL", new Integer( 0
    // ), getF_IN_REQUIREDDATETIME(), env);
    // Boolean isWorking= IsWorkingDay.run("CURRENCY", "EUR", new Integer( 0
    // ), getF_IN_REQUIREDDATETIME(), env);
    try {
      Boolean isWorking = IsWorkingDay.run("BANK",
          CommonConstants.EMPTY_STRING, new Integer(0), maturityDate,
          env);
      if (mode != null) {
        if (mode.equals("2")) {
          if (!isWorking.booleanValue()) {
            newDate = PreviousWorkingDateForDate.run("BANK",
                CommonConstants.EMPTY_STRING, new Integer(0),
                maturityDate, env);
            return newDate;
          } else {
            newDate = maturityDate;
          }
        }

        else if (mode.equals("4")) {
          if (!isWorking.booleanValue()) {
            newDate = NextWorkingDateForDate.run("BANK",
                CommonConstants.EMPTY_STRING, new Integer(0),
                maturityDate, env);

            return newDate;

          } else {
            newDate = maturityDate;
          }
        }
      }
      newDate = maturityDate;
    } catch (BankFusionException e) {
      setMessageStatus(new String[] {}, "9", 40409315,
          CommonConstants.EMPTY_STRING);
      LOGGER.error("Exception in checkWorkingDay method: "
          + e.getMessage());
      LOGGER.error(ExceptionUtil.getExceptionAsString(e));

    }
    return newDate;
  }

  /**
   * This method is used to get the module name i.e. IBSNet from
   * ModuleConfiguration table
   * 
   * @param module
   * @param paramname
   * @param env
   * @return
   */
  

  private String getModuleConfigForIBSNet(String module, String paramname,
      BankFusionEnvironment env) {
    IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory
        .getInstance()
        .getServiceManager()
        .getServiceForName(
            IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
    String value = (String) ubInformationService.getBizInfo()
        .getModuleConfigurationValue(module, paramname, env);
    return value;

  }

  /**
   * 
   * @param exchRate
   * @param newExchangeRate
   * @param buyCurrencyCode
   * @param env
   * @return @
   */
  /*
   * private boolean checkExchangeRateTolerance(BigDecimal exchRate,
   * BigDecimal newExchangeRate, String buyCurrencyCode, BankFusionEnvironment
   * env) { if (exchRate != null &&
   * exchRate.compareTo(CommonConstants.BIGDECIMAL_ZERO) > 0) { double
   * currencyTolerance = ((IServerBusinessInfo)
   * SystemInformationManager.getInstance())
   * .getCurrencyTolerancePercentage(buyCurrencyCode, env); BigDecimal
   * curTolerance = new BigDecimal(currencyTolerance / 100); BigDecimal tol =
   * exchRate.multiply(curTolerance).setScale(8, BigDecimal.ROUND_UP);
   * BigDecimal lowTolerance = exchRate.subtract(tol); BigDecimal
   * highTolerance = exchRate.add(tol); if
   * (newExchangeRate.compareTo(lowTolerance) < 0 ||
   * newExchangeRate.compareTo(highTolerance) > 0) { return false; } } return
   * true; }
   */

  /**
   * Validate whether all these combination are not null values
   * 
   * @param currencyCode
   * @param transAmt
   * @param exchangeRate
   * @param baseEquivalent
   * @param multiDiv
   * @return
   */
  private void checkMultiCurrencyValidity(String currencyCode,
      BigDecimal transAmt, BigDecimal exchangeRate,
      BigDecimal baseEquivalent, String multiDiv,
      BankFusionEnvironment env) {

    if (currencyCode != null && transAmt != null && exchangeRate != null
        && baseEquivalent != null && multiDiv != null) {
      try {
        checkTolerance(currencyCode, exchangeRate, transAmt, env);
      } catch (BankFusionException b) {
        LOGGER.error("Exception in Tolerance Checking :"
            + b.getMessage());
        LOGGER.error(ExceptionUtil.getExceptionAsString(b));
        // TODO - UB_REFACTOR - Exception consumed in checkTolerance -
        // Change this. Impossible to debug!
      }
    }
  }

  /**
   * This method checks given account is closed or not
   * 
   * @param accountObj
   * @return
   */
  private boolean isAccountStatus(String accountId, BankFusionEnvironment env) {
    boolean valid = FALSEFLAG;
    IBOAccount accountObj = null;
    BusinessValidatorBean validatorBean = null;
    IBOAttributeCollectionFeature accountItem = null;

    try {
      accountItem = (IBOAttributeCollectionFeature) env.getFactory()
          .findByPrimaryKey(IBOAttributeCollectionFeature.BONAME,
              accountId);
      validatorBean = new BusinessValidatorBean();

      accountObj = (IBOAccount) env.getFactory().findByPrimaryKey(
          IBOAccount.BONAME, accountId);
    } catch (BankFusionException bfe) {
      LOGGER.error("Failed Validations:\n" + "Account details not found"
          + accountId);
      LOGGER.error(ExceptionUtil.getExceptionAsString(bfe));
    }
    if (accountObj != null && accountObj.isF_DORMANTSTATUS()) {
      valid = TRUEFLAG;
      LOGGER.info(accountId
          + " is Dormant Account,Posting to Suspense Account");
      setMessageStatus(new String[] { accountId }, "4", 40507136,
          CommonConstants.EMPTY_STRING);
    }

    if (validatorBean != null && validatorBean.validateAccountClosed(accountItem, env)) {
      valid = TRUEFLAG;
      setMessageStatus(new String[] { accountId }, "8", 40000132,
          ACCOUNT_CLOSED);
    }
    if (validatorBean != null && validatorBean.validateAccountStopped(accountItem, env)) {
      valid = TRUEFLAG;
      LOGGER.info(accountId
          + " is Stop Account,Posting to Suspense Account");
      setMessageStatus(new String[] { accountId }, "4", 40507136,
          CommonConstants.EMPTY_STRING);
    }
    return valid;
  }

  /**
   * Validate group limit
   * 
   * @param postingMsg
   *            @
   */
  private void checkLimits(String accCurrCode, String accId,
      BigDecimal amount, String sign, BankFusionEnvironment env) {
    // Call LimitsFeature to validate limits
    LimitsFeature limitsFeature = new LimitsFeature(env);
    limitsFeature.setF_IN_AccountCurrencyIfPseudonym(accCurrCode);
    limitsFeature.setF_IN_AccountNo(accId);
    limitsFeature.setF_IN_Amount(amount);
    limitsFeature.setF_IN_Amount_Curr(accCurrCode);
    limitsFeature.setF_IN_AmountSign(sign);
    limitsFeature.setF_IN_PerformValidationsOnly(true);
    limitsFeature.setF_IN_ProcessAccountLimits(true);
    try {
      limitsFeature.process(env);
      boolean limitsFeatureResult = limitsFeature.isF_OUT_ProcessStatus()
          .booleanValue();
      if (!limitsFeatureResult) {
        // limit validation failed
        setMessageStatus(new String[] { accId }, "8", 40507019,
            LIMIT_EXEEDS);
      }
    } catch (BankFusionException bFExcp) {
      LOGGER.error("Limits Validation Exception :" + bFExcp.getMessage());
      LOGGER.error(ExceptionUtil.getExceptionAsString(bFExcp));
    }
  }

  /**
   * Validate account limit
   * 
   * @param postingMsg
   *            @
   */
  private void checkAccountLimits(String accCurrCode, String accId,
      BigDecimal amount, String sign, BankFusionEnvironment env) {
    // Call AccountLimitFeature to validate limits
    AccountLimitFeature accLimitFeature = new AccountLimitFeature(env);
    accLimitFeature.setF_IN_ACCOUNTID(accId);
    accLimitFeature.setF_IN_TRANSACTIONAMOUNT(amount);
    accLimitFeature.setF_IN_TRANSACTIONCURRENCY(accCurrCode);
    accLimitFeature.setF_IN_TRANSACTIONSIGN(sign);
    try {
      accLimitFeature.process(env);
      boolean limitsFeatureResult = accLimitFeature
          .isF_OUT_LIMITVALIDATIONSTATUS().booleanValue();
      if (!limitsFeatureResult) {
        // limit validation failed
        setMessageStatus(new String[] { accId }, "8", 40507019,
            LIMIT_EXEEDS);
      } else {
        // call group limit validation
        checkLimits(accCurrCode, accId, amount, sign, env);
      }
    } catch (Exception bFExcp) {
      LOGGER.error("Limits Validation Exception :" + bFExcp.getMessage());
      LOGGER.error(ExceptionUtil.getExceptionAsString(bFExcp));
    }
  }

  /**
   * Check for tolerance
   * 
   * @param buyCurr
   * @param exchangeRate
   * @param amount
   * @param env
   *            @
   */

  private void checkTolerance(String buyCurr, BigDecimal exchangeRate,
      BigDecimal amount, BankFusionEnvironment env) {

    try {
      if (!exchangeRate.toString().equals("0")) {
        // fetching multiply divide flag
        IBusinessInformation bizInfo = ((IBusinessInformationService) ServiceManagerFactory
            .getInstance()
            .getServiceManager()
            .getServiceForName(
                BusinessInformationService.BUSINESS_INFORMATION_SERVICE))
            .getBizInfo();
        boolean isFlagMultiply = bizInfo.isMultiply(buyCurr,
            SystemInformationManager.getInstance()
                .getBaseCurrencyCode(),
            ForexConstants.DEFAULT_SPOT_RATE_TYPE);
        String multiplyDivide = isFlagMultiply == true ? ForexConstants.EXCHANGE_RATE_MULTIPLY
            : ForexConstants.EXCHANGE_RATE_DIVIDE;

        // Exchange Rate Tolerance validation using BusinessInformation

        // Exchange Rate is fetched from the ExchangeRate table using
        // the fromCurrency and toCurrency

        // Tolerance is fetched from the Currency table on the basis on
        // the source currency
        IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory
            .getInstance()
            .getServiceManager()
            .getServiceForName(
                IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
        boolean isExchangeRateWithinCurrencyTolerance = ubInformationService
            .getBizInfo().isExchangeRateWithinCurrencyTolerance(
                exchangeRate,
                multiplyDivide,
                ForexConstants.DEFAULT_SPOT_RATE_TYPE,
                buyCurr,
                SystemInformationManager.getInstance()
                    .getBaseCurrencyCode(), amount, env);

        if (isExchangeRateWithinCurrencyTolerance == false) {
          setMessageStatus(
              new String[] { CommonConstants.EMPTY_STRING
                  + exchangeRate }, "8", 40507024,
              EXCHANGE_RATE_NOT_IN_TOLERANCE);
        }
      }
    } catch (BankFusionException bFEx) {
      LOGGER.error("Exception in Tolerance Checking :"
          + bFEx.getMessage());
      LOGGER.error(ExceptionUtil.getExceptionAsString(bFEx));
    }
  }

  /**
   * Check for the duplicate posting of a transaction.
   * 
   * @param accId
   * @param txnRef
   * @param env
   */
  private void checkForDuplicateTxn(String accId, String txnRef,
      BankFusionEnvironment env) {
    IBOTradeFinanceTxnAudit tradeFinanceTxnAuditBO = null;
    String id = null;
    String ref = null;
    int status = 0;
    try {
      ArrayList params = new ArrayList();
      params.add(accId);
      params.add(txnRef);
      ArrayList misList = (ArrayList) env.getFactory().findByQuery(
          IBOTradeFinanceTxnAudit.BONAME, findbyAccIdAndTxn, params,
          null);
      if (!misList.isEmpty() && misList.get(0) != null) {
        tradeFinanceTxnAuditBO = (IBOTradeFinanceTxnAudit) misList
            .get(0);
        id = tradeFinanceTxnAuditBO.getF_ACCOUNTNUMBER();
        ref = tradeFinanceTxnAuditBO.getF_TRANSACTIONREFERENCE();
        status = tradeFinanceTxnAuditBO.getF_STATUS();
        if (id != null && ref != null && (status == 0 || status == 4)) {
          setMessageStatus(new String[] { accId, txnRef }, "1",
              40409312, CommonConstants.EMPTY_STRING);
        }

      }
    } catch (BankFusionException e) {
      setMessageStatus(new String[] { accId, txnRef }, "9", 40409315,
          CommonConstants.EMPTY_STRING);
      LOGGER.error(ExceptionUtil.getExceptionAsString(e));
    }
  }

  /**
   * Set the IBSNet Status codes
   * 
   * @param obj
   * @param errorNo
   * @param env
   */
  private void setMessageStatus(Object[] obj, String statusCode, int errorNo,
      String errorCode) {
    if (isF_OUT_hasErrors()) {
      setF_OUT_TRANS_STATUS(new Integer(statusCode));
      if (!statusCode.equals("4")) { // ignore when posting into suspence
                      // account
        setF_OUT_hasErrors(Boolean.FALSE);
        setF_OUT_AUTHORISED_FLG(statusCode);
        setF_OUT_ERRORCODE(errorCode);
        String eMessage = BankFusionMessages.getInstance()
            .getFormattedEventMessage(
                errorNo,
                new Object[] { obj.toString() },
                BankFusionThreadLocal.getUserSession()
                    .getUserLocale());
        LOGGER.error(eMessage);
      }
    }
  }

  /**
   * This method checks that given account is closed or not
   * 
   * @param accountObj
   * @return
   */
  private boolean isAccountExist(String accountId, BankFusionEnvironment env) {
    boolean valid = FALSEFLAG;
    IBOAccount accountObj = null;
    try {
      accountObj = (IBOAccount) env.getFactory().findByPrimaryKey(
          IBOAccount.BONAME, accountId);
    } catch (BankFusionException bfe) {
      LOGGER.error("Account Validations:\n"
          + "Account details not found should post to suspense "
          + accountId);
      LOGGER.error(ExceptionUtil.getExceptionAsString(bfe));
    }
    if (accountObj != null) {
      valid = TRUEFLAG;
    }
    return valid;
  }

  /**
   * This method returns an Util Date with the time set to zero.
   * 
   * @param date
   *            - java.util.Date
   * @return
   */
  public static java.util.Date getDateWithZeroTime(java.util.Date date) {
    Calendar cDate = Calendar.getInstance();
    cDate.setTime(date);
    cDate.set(Calendar.HOUR, 0);
    cDate.set(Calendar.MINUTE, 0);
    cDate.set(Calendar.SECOND, 0);
    cDate.set(Calendar.MILLISECOND, 0);
    cDate.set(Calendar.AM_PM, Calendar.AM);
    cDate.set(Calendar.DST_OFFSET, 0);
    return (cDate.getTime());
  }
}
