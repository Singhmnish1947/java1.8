/* ***********************************************************************************
 * Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 * $Id: UB_SWT_Util.java,v 1.11 2008/11/18 10:49:39 shailejar Exp $
 * $Id: UB_SWT_Util.java,v 1.11 2008/11/18 10:49:39 shailejar Exp $
 */
package com.misys.ub.swift;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.security.runtime.impl.CurrencyUtil;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.lending.LendingConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOCurrency;
import com.trapedza.bankfusion.bo.refimpl.IBOPseudonymAccountMap;
import com.trapedza.bankfusion.bo.refimpl.IBOSWTDisposal;
import com.trapedza.bankfusion.bo.refimpl.IBOSWTDisposalHistory;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.BankFusionObject;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.IsWorkingDay;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.NextWorkingDateForDate;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.PreviousWorkingDateForDate;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;

import bf.com.misys.cbs.types.narration.SwiftMT103NarrativeCodes;
import bf.com.misys.cbs.types.narration.SwiftMT202NarrativeCodes;

/**
 * @author Girish
 * 
 */
public class UB_SWT_Util {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private transient final static Log logger = LogFactory.getLog(UB_SWT_Util.class.getName());

    private static final String ModuleName = "FEX";

    private static final String VOSTROPARAMMANE = "DEFAULT_VOSTRO";
    
    private static final String whereClause1 = " WHERE " + IBOPseudonymAccountMap.PSEUDONAME + " like ? AND "
            + IBOPseudonymAccountMap.ACCOUNTID + " = ?";

    private static final String disposalhistoryWhere = " WHERE " + IBOSWTDisposalHistory.SWTDISPOSALID + " = ? ORDER BY "
            + IBOSWTDisposalHistory.CREATEDTTM + " ASC";

    private static final String GETADDRESS = "GetAddress";
    private static final String CUSTOMERCODE = "CustomerCode";
    private static final String ADDRESSLINE1 = "ADDRESSLINE1";
    private static final String ADDRESSLINE2 = "ADDRESSLINE2";
    private static final String ADDRESSLINE3 = "ADDRESSLINE3";
    private static final String ADDRESSLINE4 = "ADDRESSLINE4";
    private static final String ADDRESSLINE5 = "ADDRESSLINE5";
    private static final String ADDRESSLINE6 = "ADDRESSLINE6";
    private static final String ADDRESSLINE7 = "ADDRESSLINE7";
    private static final String SWIFTMODULE = "SWIFT";
    private static final String SPLCHARPARAMNAME = "SWIFT_SPL_CHARS";

    String ParamValue_LookAheadDays;
    String ParamValue_AdviceDays;
    String ParamValue_Date;
    int LookAheadDays;
    int AdviceDays;
    Date FinalDate;

    /**
     * Method to check whether a given value is null or not
     * 
     * @param inputString
     * @return String if not null return inputValue otherwise blank
     */
    public String verifyForNull(String inputString) {

        if (inputString != null && inputString.trim().length() > 0) {
            return inputString.trim();
        }
        return CommonConstants.EMPTY_STRING;
    }

    public Date GetNextStatementDay(java.util.Date laststmtdate, String FrequencyCode, int FrequencyUnit, BankFusionEnvironment env,
            int interval, boolean noOfstmtCompleted) {
        Calendar nextStatementDate = Calendar.getInstance();
        nextStatementDate.setTime(laststmtdate);
        if (FrequencyCode != null && FrequencyCode.trim().length() > 0) {
            switch (FrequencyCode.charAt(0)) {
                case 'D':
                    nextStatementDate.add(Calendar.DATE, 1);

                    break;

                case 'W':
                    nextStatementDate.add(Calendar.WEEK_OF_MONTH, 1);
                    nextStatementDate.set(Calendar.DAY_OF_WEEK, FrequencyUnit);

                    break;

                case 'M':
                    nextStatementDate.add(Calendar.MONTH, 1);
                    nextStatementDate.set(Calendar.DATE, FrequencyUnit);

                    break;

                case 'Q':
                    nextStatementDate.add(Calendar.MONTH, 3);
                    nextStatementDate.set(Calendar.DATE, FrequencyUnit);

                    break;

                case 'H':
                    nextStatementDate.add(Calendar.MONTH, 6);
                    nextStatementDate.set(Calendar.DATE, FrequencyUnit);

                    break;

                case 'Y':
                    nextStatementDate.add(Calendar.YEAR, 1);
                    nextStatementDate.set(Calendar.DATE, FrequencyUnit);

                    break;

                case 'I':

                    if (noOfstmtCompleted) {
                        nextStatementDate.add(Calendar.DATE, 1);

                    }
                    else {
                        nextStatementDate.add(Calendar.MINUTE, interval);
                    }

                    break;

                case 'T':

                    nextStatementDate.add(Calendar.MINUTE, FrequencyUnit);

                    break;

                default:

                    break;
            }
        }
        java.util.Date d1 = new java.util.Date();
        d1 = nextStatementDate.getTime();
        if (!IsWorkingDay.run(LendingConstants.WORKING_DAY_CONTEXT_BANK, CommonConstants.EMPTY_STRING, CommonConstants.INTEGER_ZERO,
                d1, env))
            d1 = NextWorkingDateForDate.run(LendingConstants.WORKING_DAY_CONTEXT_BANK, CommonConstants.EMPTY_STRING,
                    CommonConstants.INTEGER_ZERO, d1, env);
        Date d2 = new Date(d1.getTime());
        return d2;
    }

    /**
     * Method Description:Prepare the branchcode and BicCode Map
     * @param env
     * @return
     */
    public HashMap populateBranch_BICCodeMap(BankFusionEnvironment env) {
        HashMap branch_BICCodeMap = new HashMap();
        VectorTable vectorList = listAllBranches();
        if (vectorList.size() != 0) {
            for (int i = 0; i < vectorList.size(); ++i) {
                Map<?, ?> paramValues = vectorList.getRowTags(i);
                String bicCode=(String) paramValues.get("BICCODE");
                String branchCode=(String) paramValues.get("BRANCHSORTCODE");
                if (bicCode != null) {
                    branch_BICCodeMap.put(branchCode,bicCode);
                }
                else {
                    branch_BICCodeMap.put(branchCode, CommonConstants.EMPTY_STRING);
                }
            }
        }
        else {
            new BankFusionException(40507007, new Object[] { "populate Branch & BIC Codes." }, logger, env);
        }

        return branch_BICCodeMap;
    }

    /**
     * This method returns date&time String where date as SWIFT format(YYDDMM) and time as 'HHMMSS'
     * with " "(space) as delimiter.
     * 
     * @param timeStamp
     * @return
     */
    public String getSwiftDateTimeString(Timestamp timeStamp) {
        StringBuffer dateTimeBuffer = new StringBuffer();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeStamp.getTime());
        int date = cal.get(Calendar.DATE);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        StringBuffer dateBuffer = new StringBuffer();

        dateBuffer.append(Integer.toString(year).substring(2));
        month++;
        if (month < 10)
            dateBuffer.append("0" + Integer.toString(month));
        else dateBuffer.append(Integer.toString(month));
        if (date < 10)
            dateBuffer.append("0" + Integer.toString(date));
        else dateBuffer.append(Integer.toString(date));

        dateTimeBuffer.append(dateBuffer.toString() + " ");

        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);

        StringBuffer timeBuffer = new StringBuffer();

        if (hour < 10)
            timeBuffer.append("0" + Integer.toString(hour));
        else timeBuffer.append(Integer.toString(hour));
        if (minute < 10)
            timeBuffer.append("0" + Integer.toString(minute));
        else timeBuffer.append(Integer.toString(minute));
        if (second < 10)
            timeBuffer.append("0" + Integer.toString(second));
        else timeBuffer.append(Integer.toString(second));

        dateTimeBuffer.append(timeBuffer.toString());

        return dateTimeBuffer.toString();

    }

    /**
     * This method returns date&time String where date as SWIFT format(YYDDMM) and time as 'HHMMSS'
     * with " "(space) as delimiter.
     * 
     * @param timeStamp
     * @return
     */
    // String "accInfo" and String "nationalCLRCode" is replaced with String "partyIdentifier" and
    // String text4 is added by Angraj because of Reorganizing Settlement Instruction.
    public String createSwiftTagString(String bicCode, String partyIdentifier, String text1, String text2, String text3,
            String text4) {
        String BIC_code = verifyForNull(bicCode);
        String PartyIdentifier = verifyForNull(partyIdentifier);
        String Narrative1 = verifyForNull(text1);
        String Narrative2 = verifyForNull(text2);
        String Narrative3 = verifyForNull(text3);
        String Narrative4 = verifyForNull(text4);

        return getTagString(BIC_code, PartyIdentifier, Narrative1, Narrative2, Narrative3, Narrative4, StringUtils.EMPTY,StringUtils.EMPTY);

    }

    /**
     * Method Description:SWift 2019 changes to reduce impact
     * 
     * @param bicCode
     * @param partyIdentifier
     * @param text1
     * @param text2
     * @param text3
     * @param text4
     * @param text5
     * @return
     */
    public String createSwiftTagForMT300(String bicCode, String partyIdentifier, String text1, String text2, String text3,
            String text4, String text5) {
        String BIC_code = verifyForNull(bicCode);
        String PartyIdentifier = verifyForNull(partyIdentifier);
        String Narrative1 = verifyForNull(text1);
        String Narrative2 = verifyForNull(text2);
        String Narrative3 = verifyForNull(text3);
        String Narrative4 = verifyForNull(text4);
        String Narrative5 = verifyForNull(text5);

        return getTagString(BIC_code, PartyIdentifier, Narrative1, Narrative2, Narrative3, Narrative4, Narrative5,PaymentSwiftConstants.MT_300);

    }

    public String IsPublish(String messageType, int confirmflag, int cancelflag) {
        if ((messageType.charAt(0) == '3') && confirmflag == 2 && cancelflag == 0) {
            return "N";
        }
        return "Y";
    }

    /**
     * Method to Create a tag based on input tags
     * 
     * @param bic_code
     *            BIC Code
     * @param partyIdentifier
     *            Party Identifierl
     * @param narrative1
     *            Narrative line 1
     * @param narrative2
     *            Narrative Line 2
     * @param narrative3
     *            Narrative Line 3
     * @param narrative3
     *            Narrative Line 4
     * @return String 52Tag value
     */

    private String getTagString(String bic_code, String partyIdentifier, String narrative1, String narrative2, String narrative3,
            String narrative4, String narrative5, String messageType) {
        StringBuffer tmp_tag52 = new StringBuffer();
        if (bic_code != null && !bic_code.equals(CommonConstants.EMPTY_STRING)) {

            // it is going to be 52A
            if (!partyIdentifier.equals(CommonConstants.EMPTY_STRING)) {
                tmp_tag52.append(partyIdentifier);
                tmp_tag52.append(SWT_Constants.delimiter);
            }

            tmp_tag52.append(bic_code);
            if (!tmp_tag52.toString().equals(CommonConstants.EMPTY_STRING))
                tmp_tag52.append("A");
            else {
                tmp_tag52.append(" ");
            }
        }
        else {
            tmp_tag52.append(partyIdentifier);
            if (tmp_tag52.length() > 0)
                tmp_tag52.append(SWT_Constants.delimiter);

            tmp_tag52.append(narrative1);
            if (tmp_tag52.length() > 0)
                tmp_tag52.append(SWT_Constants.delimiter);
            tmp_tag52.append(narrative2);
            if (tmp_tag52.length() > 0)
                tmp_tag52.append(SWT_Constants.delimiter);
            tmp_tag52.append(narrative3);
            if (tmp_tag52.length() > 0)
                tmp_tag52.append(SWT_Constants.delimiter);

            tmp_tag52.append(narrative4);
            if (tmp_tag52.length() > 0)
                tmp_tag52.append(SWT_Constants.delimiter);

            if (!StringUtils.isBlank(narrative5)) {
                tmp_tag52.append(narrative5);
                if (tmp_tag52.length() > 0)
                    tmp_tag52.append(SWT_Constants.delimiter);
            }

            if (!tmp_tag52.toString().equals(CommonConstants.EMPTY_STRING)) {
                
                tmp_tag52.append(PaymentSwiftConstants.MT_300.equals(messageType)? "J" : "D");
            }
            else {
                tmp_tag52.append(" ");
            }
        }
        return tmp_tag52.toString();
    }

    public BankFusionObject getDisposalHistoryRecord(String DisposalId, BankFusionEnvironment env) {

        List list = null;
        ArrayList param = new ArrayList();
        param.add(DisposalId);

        // UB_SWT_DisposalObject disposalObject=new UB_SWT_DisposalObject();
        BankFusionObject disposalObject = new UB_SWT_DisposalObject();
        list = env.getFactory().findByQuery(IBOSWTDisposalHistory.BONAME, disposalhistoryWhere, param, null);
        if (list.size() > 0) {
            IBOSWTDisposalHistory disposalRecord = (IBOSWTDisposalHistory) list.get(0);

            ((UB_SWT_DisposalObject) disposalObject).setDisposalRef(disposalRecord.getF_SWTDISPOSALID());
            ((UB_SWT_DisposalObject) disposalObject).setBrokerNumber(disposalRecord.getF_BROKERCODE());
            ((UB_SWT_DisposalObject) disposalObject).setDraftNumber(disposalRecord.getF_DRAFTNUMBER().toString());
            ((UB_SWT_DisposalObject) disposalObject).setCancelFlag(disposalRecord.getF_CANCELFLAG());
            ((UB_SWT_DisposalObject) disposalObject).setConfirmationFlag(disposalRecord.getF_CONFIRMATIONFLAG());
            ((UB_SWT_DisposalObject) disposalObject).setContraAccountNo(disposalRecord.getF_CONTRAACCOUNTID());
            ((UB_SWT_DisposalObject) disposalObject).setContractAmount(disposalRecord.getF_CONTRACTAMOUNT());
            ((UB_SWT_DisposalObject) disposalObject).setCurrentDealNumber(disposalRecord.getF_DEALNO());
            ((UB_SWT_DisposalObject) disposalObject).setInterestAmount(disposalRecord.getF_INTERESTAMOUNT());
            ((UB_SWT_DisposalObject) disposalObject).setInterestOrExchangeRate(disposalRecord.getF_INTERESTRATE());
            ((UB_SWT_DisposalObject) disposalObject).setTerm(disposalRecord.getF_TERM() + CommonConstants.EMPTY_STRING);
            ((UB_SWT_DisposalObject) disposalObject).setMainAccountNo(disposalRecord.getF_CUSTACCOUNTID());
            ((UB_SWT_DisposalObject) disposalObject).setMaturityDate(disposalRecord.getF_MATURITYDATE());
            ((UB_SWT_DisposalObject) disposalObject).setNextInterestDueDate(disposalRecord.getF_NEXTINTERESTDUEDT());
            ((UB_SWT_DisposalObject) disposalObject).setInterestPeriodStartDate(disposalRecord.getF_INTERESTPERIODSTARTDT());
            ((UB_SWT_DisposalObject) disposalObject).setInterestPeriodEndDate(disposalRecord.getF_INTERESTPERIODENDDT());
            ((UB_SWT_DisposalObject) disposalObject).setMessageStatus(disposalRecord.getF_MESSAGESTATUS());
            ((UB_SWT_DisposalObject) disposalObject).setMessageType(disposalRecord.getF_MESSAGETYPE());
            ((UB_SWT_DisposalObject) disposalObject).setPaymentFlagMT202(disposalRecord.getF_PAYMENTFLAG());
            ((UB_SWT_DisposalObject) disposalObject).setPostDate(disposalRecord.getF_POSTDATE());
            ((UB_SWT_DisposalObject) disposalObject).setPreviousDealRecordNumber(disposalRecord.getF_PREVSWTDISPOSALID());
            ((UB_SWT_DisposalObject) disposalObject).setReceiptFlagMT210(disposalRecord.getF_RECEIPTFLAG());
            ((UB_SWT_DisposalObject) disposalObject).setRelatedDealNumber(CommonConstants.EMPTY_STRING);

            ((UB_SWT_DisposalObject) disposalObject).setSI_AccWithCode(disposalRecord.getF_BENEFICIARY_CODE());
            /* start of Added new fields for the reorganized Settlement instruction by Sharan */
            ((UB_SWT_DisposalObject) disposalObject)
                    .setSI_AccWithPartyIdentifier(disposalRecord.getF_BENEFICIARY_PARTY_IDENTIFIER());
            /* end of Added new fields for the reorganized Settlement instruction by Sharan */
            ((UB_SWT_DisposalObject) disposalObject).setSI_AccWithText1(disposalRecord.getF_BENEFICIARY_TEXT1());
            ((UB_SWT_DisposalObject) disposalObject).setSI_AccWithText2(disposalRecord.getF_BENEFICIARY_TEXT2());
            ((UB_SWT_DisposalObject) disposalObject).setSI_AccWithText3(disposalRecord.getF_BENEFICIARY_TEXT3());
            /* start of Added new fields for the reorganized Settlement instruction by Sharan */
            ((UB_SWT_DisposalObject) disposalObject).setSI_AccWithText4(disposalRecord.getF_BENEFICIARY_TEXT4());
            /* end of Added new fields for the reorganized Settlement instruction by Sharan */
            ((UB_SWT_DisposalObject) disposalObject).setSI_BankAddlInstrCode(disposalRecord.getF_BANK_ADDL_INSTRUCTION_CODE());
            ((UB_SWT_DisposalObject) disposalObject).setSI_BankInstructionCode(disposalRecord.getF_BANK_INSTRUCTION_CODE());
            ((UB_SWT_DisposalObject) disposalObject).setSI_BankOpCode(disposalRecord.getF_BANK_OPERATION_CODE());
            ((UB_SWT_DisposalObject) disposalObject).setSI_BankToBankInfo1(disposalRecord.getF_BANK_TO_BANK_INFO1());
            ((UB_SWT_DisposalObject) disposalObject).setSI_BankToBankInfo2(disposalRecord.getF_BANK_TO_BANK_INFO2());
            ((UB_SWT_DisposalObject) disposalObject).setSI_BankToBankInfo3(disposalRecord.getF_BANK_TO_BANK_INFO3());
            ((UB_SWT_DisposalObject) disposalObject).setSI_BankToBankInfo4(disposalRecord.getF_BANK_TO_BANK_INFO4());
            ((UB_SWT_DisposalObject) disposalObject).setSI_BankToBankInfo5(disposalRecord.getF_BANK_TO_BANK_INFO5());
            ((UB_SWT_DisposalObject) disposalObject).setSI_BankToBankInfo6(disposalRecord.getF_BANK_TO_BANK_INFO6());
            ((UB_SWT_DisposalObject) disposalObject).setSI_ChargeCode(disposalRecord.getF_CHARGECODE());
            /* start of Added new fields for the reorganized Settlement instruction by Sharan */
            ((UB_SWT_DisposalObject) disposalObject)
                    .setSI_ForAccountPartyIdentifier(disposalRecord.getF_FOR_ACCOUNT_PARTY_IDENTIFIER());
            /* start of Added new fields for the reorganized Settlement instruction by Sharan */
            ((UB_SWT_DisposalObject) disposalObject).setSI_ForAccountText1(disposalRecord.getF_FOR_ACCOUNT_TEXT1());
            ((UB_SWT_DisposalObject) disposalObject).setSI_ForAccountText2(disposalRecord.getF_FOR_ACCOUNT_TEXT2());
            ((UB_SWT_DisposalObject) disposalObject).setSI_ForAccountText3(disposalRecord.getF_FOR_ACCOUNT_TEXT3());
            /* start of Added new fields for the reorganized Settlement instruction by Sharan */
            ((UB_SWT_DisposalObject) disposalObject).setSI_ForAccountText4(disposalRecord.getF_FOR_ACCOUNT_TEXT4());
            /* start of Added new fields for the reorganized Settlement instruction by Sharan */
            /* start of Added new fields for the reorganized Settlement instruction by Sharan */
            ((UB_SWT_DisposalObject) disposalObject)
                    .setSI_IntermediaryPartyIdentifier(disposalRecord.getF_INTERMEDIARY_PARTY_IDENTIFIER());
            /* end of Added new fields for the reorganized Settlement instruction by Sharan */
            ((UB_SWT_DisposalObject) disposalObject).setSI_IntermediatoryCode(disposalRecord.getF_INTERMEDIARY_CODE());

            ((UB_SWT_DisposalObject) disposalObject).setPayReceiveFlag(disposalRecord.getF_PAY_RECEIVE_FLAG());

            ((UB_SWT_DisposalObject) disposalObject).setSI_IntermediatoryText1(disposalRecord.getF_INTERMEDIARY_TEXT1());
            ((UB_SWT_DisposalObject) disposalObject).setSI_IntermediatoryText2(disposalRecord.getF_INTERMEDIARY_TEXT2());
            ((UB_SWT_DisposalObject) disposalObject).setSI_IntermediatoryText3(disposalRecord.getF_INTERMEDIARY_TEXT3());
            /* start of Added new fields for the reorganized Settlement instruction by Sharan */
            ((UB_SWT_DisposalObject) disposalObject).setSI_IntermediatoryText4(disposalRecord.getF_INTERMEDIARY_TEXT4());
            /* end of Added new fields for the reorganized Settlement instruction by Sharan */
            ((UB_SWT_DisposalObject) disposalObject).setSI_OrdCustAccInfo(disposalRecord.getF_ORDERINGINSTITUTE_ACC_INFO());
            ((UB_SWT_DisposalObject) disposalObject).setSI_OrdCustText1(disposalRecord.getF_ORDERINGCUSTDTL1());
            ((UB_SWT_DisposalObject) disposalObject).setSI_OrdCustText2(disposalRecord.getF_ORDERINGCUSTDTL2());
            ((UB_SWT_DisposalObject) disposalObject).setSI_OrdCustText3(disposalRecord.getF_ORDERINGCUSTDTL3());
            ((UB_SWT_DisposalObject) disposalObject).setSI_OrdCustText4(disposalRecord.getF_ORDERINGCUSTDTL4());
            ((UB_SWT_DisposalObject) disposalObject).setSI_OrdCustBICCode(CommonConstants.EMPTY_STRING);

            ((UB_SWT_DisposalObject) disposalObject).setSI_OrdInstAccInfo(disposalRecord.getF_ORDERINGINSTITUTE_ACC_INFO());
            ((UB_SWT_DisposalObject) disposalObject).setSI_OrdInstBICCode(disposalRecord.getF_ORDERINGINSTITUTE_CODE());
            ((UB_SWT_DisposalObject) disposalObject).setSI_OrdInstText1(disposalRecord.getF_ORDERINGINSTITUTE_TEXT1());
            ((UB_SWT_DisposalObject) disposalObject).setSI_OrdInstText2(disposalRecord.getF_ORDERINGINSTITUTE_TEXT2());
            ((UB_SWT_DisposalObject) disposalObject).setSI_OrdInstText3(disposalRecord.getF_ORDERINGINSTITUTE_TEXT3());
            /* start of Added new fields for the reorganized Settlement instruction by Sharan */
            ((UB_SWT_DisposalObject) disposalObject).setSI_OrdInstText4(disposalRecord.getF_ORDERINGINSTITUTE_TEXT4());
            /* end of Added new fields for the reorganized Settlement instruction by Sharan */
            ((UB_SWT_DisposalObject) disposalObject).setSI_PayDetails1(disposalRecord.getF_PAY_DETAILS1());
            ((UB_SWT_DisposalObject) disposalObject).setSI_PayDetails2(disposalRecord.getF_PAY_DETAILS2());
            ((UB_SWT_DisposalObject) disposalObject).setSI_PayDetails3(disposalRecord.getF_PAY_DETAILS3());
            ((UB_SWT_DisposalObject) disposalObject).setSI_PayDetails4(disposalRecord.getF_PAY_DETAILS4());
            ((UB_SWT_DisposalObject) disposalObject).setSI_PayReceiveFlag(disposalRecord.getF_PAY_RECEIVE_FLAG());
            ((UB_SWT_DisposalObject) disposalObject).setSI_PayToBICCode(disposalRecord.getF_PAY_TO_CODE());
            ((UB_SWT_DisposalObject) disposalObject).setSI_PayToPartyIdentifier(disposalRecord.getF_PAY_TO_PARTY_IDENTIFIER());
            /* end of Added new fields for the reorganized Settlement instruction by Sharan */
            ((UB_SWT_DisposalObject) disposalObject).setSI_PayToText1(disposalRecord.getF_PAY_TO_TEXT1());
            ((UB_SWT_DisposalObject) disposalObject).setSI_PayToText2(disposalRecord.getF_PAY_TO_TEXT2());
            ((UB_SWT_DisposalObject) disposalObject).setSI_PayToText3(disposalRecord.getF_PAY_TO_TEXT3());
            /* start of Added new fields for the reorganized Settlement instruction by Sharan */
            ((UB_SWT_DisposalObject) disposalObject).setSI_PayToText4(disposalRecord.getF_PAY_TO_TEXT4());
            /* end of Added new fields for the reorganized Settlement instruction by Sharan */
            ((UB_SWT_DisposalObject) disposalObject).setSI_SendersCharges(disposalRecord.getF_SENDERCHARGEAMOUNT());
            ((UB_SWT_DisposalObject) disposalObject).setTransactionAmount(disposalRecord.getF_TRANSACTIONAMOUNT());
            ((UB_SWT_DisposalObject) disposalObject).setTransactionStatus(disposalRecord.getF_TRANSACTIONSTATUS());
            ((UB_SWT_DisposalObject) disposalObject).setValueDate(disposalRecord.getF_VALUEDATE());
            ((UB_SWT_DisposalObject) disposalObject).setVerifyFlag(Integer.parseInt("1"));
            ((UB_SWT_DisposalObject) disposalObject).setCrdrFlag(disposalRecord.getF_CRDRCONFIRMATIONFLAG());
            ((UB_SWT_DisposalObject) disposalObject).setDealOriginator(disposalRecord.getF_DEALORIGINATOR());
            ((UB_SWT_DisposalObject) disposalObject).setSenderChargeCurrency(disposalRecord.getF_SENDERCHARGECURRENCYCODE());
            ((UB_SWT_DisposalObject) disposalObject).setClientNumber(disposalRecord.getF_CUSTOMERCODE());
            ((UB_SWT_DisposalObject) disposalObject).setPartyIdentifier(disposalRecord.getF_PARTYIDENTIFIER());
            ((UB_SWT_DisposalObject) disposalObject).setPartyIdentifierAdd1(disposalRecord.getF_PARTYADDRESSLINE1());
            ((UB_SWT_DisposalObject) disposalObject).setPartyIdentifierAdd2(disposalRecord.getF_PARTYADDRESSLINE2());
            ((UB_SWT_DisposalObject) disposalObject).setPartyIdentifierAdd3(disposalRecord.getF_PARTYADDRESSLINE3());
            ((UB_SWT_DisposalObject) disposalObject).setPartyIdentifierAdd4(disposalRecord.getF_PARTYADDRESSLINE4());
            IBOSWTDisposalHistory SwtHistory = (IBOSWTDisposalHistory) env.getFactory()
                    .findByPrimaryKey(IBOSWTDisposalHistory.BONAME, disposalRecord.getBoID(), false);
            SwtHistory.setF_PAYMENTFLAG(2);
            SwtHistory.setF_RECEIPTFLAG(2);
        }
        return disposalObject;
    }

    /**
     * @param disposalObject
     * @return
     */
    public boolean intermedaitoryDetailsExists(UB_SWT_DisposalObject disposalObject) {
        boolean detailsExists = true;
        String interBICCode = verifyForNull(disposalObject.getSI_IntermediatoryCode());

        // getSI_IntermediatoryAccInfo() is changed to getSI_IntermediaryPartyIdentifier()&
        // interText4 is added because of Reorganizing Settlement Instruction.
        String interPartyIdentifier = verifyForNull(disposalObject.getSI_IntermediaryPartyIdentifier());
        String interText1 = verifyForNull(disposalObject.getSI_IntermediatoryText1());
        String interText2 = verifyForNull(disposalObject.getSI_IntermediatoryText2());
        String interText3 = verifyForNull(disposalObject.getSI_IntermediatoryText3());
        String interText4 = verifyForNull(disposalObject.getSI_IntermediatoryText4());

        if (interBICCode.equals(CommonConstants.EMPTY_STRING) && interPartyIdentifier.equals(CommonConstants.EMPTY_STRING)
                && interText1.equals(CommonConstants.EMPTY_STRING) && interText2.equals(CommonConstants.EMPTY_STRING)
                && interText3.equals(CommonConstants.EMPTY_STRING) && interText4.equals(CommonConstants.EMPTY_STRING)) {
            detailsExists = false;
        }

        return detailsExists;
    }

    /**
     * @param disposalObject
     * @return
     */
    // AccWithAccInfo is changed to partyIdentifier and String "accWithText4" is added because of
    // Reorganizing Settlement Instruction.
    public boolean accountWithDetailsExists(UB_SWT_DisposalObject disposalObject) {
        boolean detailsExists = true;
        String accWithBICCode = verifyForNull(disposalObject.getSI_AccWithCode());
        String accWithText1 = verifyForNull(disposalObject.getSI_AccWithText1());
        String accWithText2 = verifyForNull(disposalObject.getSI_AccWithText2());
        String accWithText3 = verifyForNull(disposalObject.getSI_AccWithText3());
        String accWithText4 = verifyForNull(disposalObject.getSI_AccWithText4());
        String partyIdentifier = verifyForNull(disposalObject.getSI_AccWithPartyIdentifier());

        if (accWithBICCode.equals(CommonConstants.EMPTY_STRING) && partyIdentifier.equals(CommonConstants.EMPTY_STRING)
                && accWithText1.equals(CommonConstants.EMPTY_STRING) && accWithText2.equals(CommonConstants.EMPTY_STRING)
                && accWithText3.equals(CommonConstants.EMPTY_STRING) && accWithText4.equals(CommonConstants.EMPTY_STRING)) {
            detailsExists = false;
        }

        return detailsExists;
    }

    /**
     * @param disposalObject
     * @return
     */
    public boolean orderingInstituteDetailsExists(UB_SWT_DisposalObject disposalObject) {
        boolean detailsExists = true;
        String accWithBICCode = verifyForNull(disposalObject.getSI_OrdInstBICCode());
        String accWithText1 = verifyForNull(disposalObject.getSI_OrdInstText1());
        String accWithText2 = verifyForNull(disposalObject.getSI_OrdInstText2());
        String accWithText3 = verifyForNull(disposalObject.getSI_OrdInstText3());
        String accWithText4 = verifyForNull(disposalObject.getSI_OrdInstText4());
        String accWithAccInfo = verifyForNull(disposalObject.getSI_OrdInstAccInfo());

        if (accWithBICCode.equals(CommonConstants.EMPTY_STRING) && accWithAccInfo.equals(CommonConstants.EMPTY_STRING)
                && accWithText1.equals(CommonConstants.EMPTY_STRING) && accWithText2.equals(CommonConstants.EMPTY_STRING)
                && accWithText3.equals(CommonConstants.EMPTY_STRING) && accWithText4.equals(CommonConstants.EMPTY_STRING)) {
            detailsExists = false;
        }

        return detailsExists;
    }

    /**
     * @param disposalObject
     * @return
     */
    public boolean forAccountDetailsExist(UB_SWT_DisposalObject disposalObject) {
        boolean detailsExists = true;
        // getSI_ForAccountInfo() is changed to getSI_ForAccountPartyIdentifier() and String
        // forAccText4 is added because of Reorganizing Settlement Instruction by Angraj.
        String forAccPartyIdentifier = verifyForNull(disposalObject.getSI_ForAccountPartyIdentifier());
        String forAccText1 = verifyForNull(disposalObject.getSI_ForAccountText1());
        String forAccText2 = verifyForNull(disposalObject.getSI_ForAccountText2());
        String forAccText3 = verifyForNull(disposalObject.getSI_ForAccountText3());
        String forAccText4 = verifyForNull(disposalObject.getSI_ForAccountText4());

        if (forAccPartyIdentifier.equals(CommonConstants.EMPTY_STRING) && forAccText1.equals(CommonConstants.EMPTY_STRING)
                && forAccText2.equals(CommonConstants.EMPTY_STRING) && forAccText3.equals(CommonConstants.EMPTY_STRING)
                && forAccText4.equals(CommonConstants.EMPTY_STRING)) {
            detailsExists = false;
        }

        return detailsExists;
    }

    /**
     * @param disposalObject
     * @return
     */
    // getSI_PayToAccInfo() is changed to getSI_PayToPartyIdentifier() and String accWithText4 is
    // added because of Reorganizing Settlement Instruction by Angraj.
    public boolean payToDetailsExists(UB_SWT_DisposalObject disposalObject) {
        boolean detailsExists = true;
        String accWithBICCode = verifyForNull(disposalObject.getSI_PayToBICCode());
        String accWithText1 = verifyForNull(disposalObject.getSI_PayToText1());
        String accWithText2 = verifyForNull(disposalObject.getSI_PayToText2());
        String accWithText3 = verifyForNull(disposalObject.getSI_PayToText3());
        String accWithText4 = verifyForNull(disposalObject.getSI_PayToText4());

        String accWithAccInfo = verifyForNull(disposalObject.getSI_PayToPartyIdentifier());

        if (accWithBICCode.equals(CommonConstants.EMPTY_STRING) && accWithAccInfo.equals(CommonConstants.EMPTY_STRING)
                && accWithText1.equals(CommonConstants.EMPTY_STRING) && accWithText2.equals(CommonConstants.EMPTY_STRING)
                && accWithText3.equals(CommonConstants.EMPTY_STRING) && accWithText4.equals(CommonConstants.EMPTY_STRING)) {
            detailsExists = false;
        }

        return detailsExists;
    }

    /**
     * @param settlementDetail
     * @return
     */
    public String getBankToBankInfo(UB_SWT_DisposalObject disposalObject) {
        StringBuffer temp57 = new StringBuffer();

        if (!verifyForNull(disposalObject.getSI_BankToBankInfo1()).equals(CommonConstants.EMPTY_STRING)) {
            temp57.append(replaceSpecialChars(disposalObject.getSI_BankToBankInfo1()));
            temp57.append(SWT_Constants.delimiter);
        }
        if (!verifyForNull(disposalObject.getSI_BankToBankInfo2()).equals(CommonConstants.EMPTY_STRING)) {
            temp57.append(replaceSpecialChars(disposalObject.getSI_BankToBankInfo2()));
            temp57.append(SWT_Constants.delimiter);
        }
        if (!verifyForNull(disposalObject.getSI_BankToBankInfo3()).equals(CommonConstants.EMPTY_STRING)) {
            temp57.append(replaceSpecialChars(disposalObject.getSI_BankToBankInfo3()));
            temp57.append(SWT_Constants.delimiter);
        }
        if (!verifyForNull(disposalObject.getSI_BankToBankInfo4()).equals(CommonConstants.EMPTY_STRING)) {
            temp57.append(replaceSpecialChars(disposalObject.getSI_BankToBankInfo4()));
            temp57.append(SWT_Constants.delimiter);
        }
        if (!verifyForNull(disposalObject.getSI_BankToBankInfo5()).equals(CommonConstants.EMPTY_STRING)) {
            temp57.append(replaceSpecialChars(disposalObject.getSI_BankToBankInfo5()));
            temp57.append(SWT_Constants.delimiter);
        }
        if (!verifyForNull(disposalObject.getSI_BankToBankInfo6()).equals(CommonConstants.EMPTY_STRING)) {
            temp57.append(replaceSpecialChars(disposalObject.getSI_BankToBankInfo6()));
        }

        return temp57.toString();
    }

    /**
     * @param settlementDetail
     * @return
     */
    public String getTag70String(UB_SWT_DisposalObject disposalObject) {
        StringBuffer temp57 = new StringBuffer();

        if (!verifyForNull(disposalObject.getSI_PayDetails1()).equals(CommonConstants.EMPTY_STRING)) {
            temp57.append(replaceSpecialChars(disposalObject.getSI_PayDetails1()));
            temp57.append(SWT_Constants.delimiter);
        }
        if (!verifyForNull(disposalObject.getSI_PayDetails2()).equals(CommonConstants.EMPTY_STRING)) {
            temp57.append(replaceSpecialChars(disposalObject.getSI_PayDetails2()));
            temp57.append(SWT_Constants.delimiter);
        }
        if (!verifyForNull(disposalObject.getSI_PayDetails3()).equals(CommonConstants.EMPTY_STRING)) {
            temp57.append(replaceSpecialChars(disposalObject.getSI_PayDetails3()));
            temp57.append(SWT_Constants.delimiter);
        }
        if (!verifyForNull(disposalObject.getSI_PayDetails4()).equals(CommonConstants.EMPTY_STRING)) {
            temp57.append(replaceSpecialChars(disposalObject.getSI_PayDetails4()));
        }

        return temp57.toString();
    }

    /**
     * @param settlementDetail
     * @param isCHQB
     * @return
     */
    public String getForAccountInfoString(UB_SWT_DisposalObject disposalObject, boolean isCHQB) {
        StringBuffer temp57 = new StringBuffer();
        if (isCHQB) {
            // Changes start for artf49964
            /*
             * if (!verifyForNull(disposalObject.getSI_ForAccountPartyIdentifier()).equals(
             * CommonConstants .EMPTY_STRING)) {
             * temp57.append(disposalObject.getSI_ForAccountPartyIdentifier());
             * temp57.append(SWT_Constants.delimiter); }
             */
            // Changes end for artf49964
            if (!verifyForNull(disposalObject.getSI_ForAccountText1()).equals(CommonConstants.EMPTY_STRING)) {
                temp57.append(replaceSpecialChars(disposalObject.getSI_ForAccountText1()));
                temp57.append(SWT_Constants.delimiter);
            }
            if (!verifyForNull(disposalObject.getSI_ForAccountText2()).equals(CommonConstants.EMPTY_STRING)) {
                temp57.append(replaceSpecialChars(disposalObject.getSI_ForAccountText2()));
                temp57.append(SWT_Constants.delimiter);
            }
            if (!verifyForNull(disposalObject.getSI_ForAccountText3()).equals(CommonConstants.EMPTY_STRING)) {
                temp57.append(replaceSpecialChars(disposalObject.getSI_ForAccountText3()));
                temp57.append(SWT_Constants.delimiter);
            }
            if (!verifyForNull(disposalObject.getSI_ForAccountText4()).equals(CommonConstants.EMPTY_STRING)) {
                temp57.append(replaceSpecialChars(disposalObject.getSI_ForAccountText4()));
            }
        }
        // getSI_ForAccountInfo() is changed to getSI_ForAccountPartyIdentifier() because of
        // Reorganizing Settlement Instruction.
        else {
            if (!verifyForNull(disposalObject.getSI_ForAccountPartyIdentifier()).equals(CommonConstants.EMPTY_STRING)) {
                temp57.append(disposalObject.getSI_ForAccountPartyIdentifier());
                temp57.append(SWT_Constants.delimiter);
            }
            if (!verifyForNull(disposalObject.getSI_ForAccountText1()).equals(CommonConstants.EMPTY_STRING)) {
                temp57.append(replaceSpecialChars(disposalObject.getSI_ForAccountText1()));
                temp57.append(SWT_Constants.delimiter);
            }
            if (!verifyForNull(disposalObject.getSI_ForAccountText2()).equals(CommonConstants.EMPTY_STRING)) {
                temp57.append(replaceSpecialChars(disposalObject.getSI_ForAccountText2()));
                temp57.append(SWT_Constants.delimiter);
            }
            if (!verifyForNull(disposalObject.getSI_ForAccountText3()).equals(CommonConstants.EMPTY_STRING)) {
                temp57.append(replaceSpecialChars(disposalObject.getSI_ForAccountText3()));
                temp57.append(SWT_Constants.delimiter);
            }
            if (!verifyForNull(disposalObject.getSI_ForAccountText4()).equals(CommonConstants.EMPTY_STRING)) {
                temp57.append(replaceSpecialChars(disposalObject.getSI_ForAccountText4()));
            }
        }
        return temp57.toString();
    }

    /**
     * 
     * @param ValueDate
     * @param env
     * @param currency
     * @param bankfusionSystemDate
     * @param messageType
     * @return boolean for category 2 message
     */
    public boolean generateCategory2Message(Date ValueDate, Date EventDate, BankFusionEnvironment env, String currency,
            Date bankfusionSystemDate, String messageType) {
        /*
         * Checkes whether newDate is Valuedate or EventDate and that date is working day or not.
         * Return true if new calculated date is equal to or before the bankfusionSystemDate.
         */

        Date newDate = null;
        messageType = "MT" + messageType;

        try {
            LookAheadDays = calDate(currency, env, messageType);

            if ("V".equals(ParamValue_Date)) {
                newDate = ValueDate;
            }
            else {
                newDate = EventDate;
            }

            Boolean isCurrentDayWorking = IsWorkingDay.run("CURRENCY", currency, new Integer(0), newDate, env);
            if (!isCurrentDayWorking.booleanValue()) {
                newDate = PreviousWorkingDateForDate.run("CURRENCY", currency, new Integer(0), newDate, env);
            }

            for (int i = 0; i < Math.abs(LookAheadDays); i++) {

                newDate = previousDate(newDate.toString(), currency, env, messageType);
                Boolean isWorking = IsWorkingDay.run("CURRENCY", currency, new Integer(0), newDate, env);
                if (!isWorking.booleanValue()) {
                    newDate = PreviousWorkingDateForDate.run("CURRENCY", currency, new Integer(0), newDate, env);
                }

            }

        }
        catch (BankFusionException e) {
            logger.info("Exception in checkWorkingDay method: ", e);
        }
        logger.info("New Calculated Date: " + newDate);
        Calendar c = Calendar.getInstance();
        c.setTime(newDate);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        newDate = new Date(c.getTime().getTime());
        if (newDate == bankfusionSystemDate || newDate.before(bankfusionSystemDate))
            return true;
        else return false;
    }

    /**
     * 
     * @param aDate
     * @param Currency
     * @param env
     * @param messageType
     * @return Date
     */

    private int calDate(String currency, BankFusionEnvironment env, String messageType) {

        Map resultMap = new HashMap();
        /*
         * Execute the Query for MODULECONFIGURATION and fetch the records for ADVICEDAYS,
         * LOOKAHEADDAYS and DATETYPE. returns the LookAheadDays.
         */
        int aDays = 0;
        IBOCurrency currencyDtls = CurrencyUtil.getCurrencyDetailsOfCurrentZone(currency);
        if (null != currencyDtls) {
            aDays = currencyDtls.getF_SWTADVICEDAYS();
        }
        else {
            logger.error("Currency not available on this " + currency);
            EventsHelper.handleEvent(ChannelsEventCodes.E_ADDRESS_LINE_IS_MANDATORY_WITH_PARTY_IDENTIFIER,
                    new Object[] { currency }, new HashMap(), env);
        }

        IBusinessInformation bizInfo = ((IBusinessInformationService) ServiceManagerFactory.getInstance().getServiceManager()
                .getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();

        resultMap = (Map) bizInfo.getModuleConfigurationValue(messageType, env);

        ParamValue_AdviceDays = resultMap.get("ADVICEDAYS").toString();
        ParamValue_LookAheadDays = resultMap.get("LOOKAHEADDAYS").toString();
        ParamValue_Date = (String) resultMap.get("DATETYPE");

        if ("false".equals(ParamValue_AdviceDays)) {
            AdviceDays = 0;
        }
        else {
            AdviceDays = aDays;
        }

        LookAheadDays = AdviceDays + Integer.parseInt(ParamValue_LookAheadDays);
        return (LookAheadDays);

    }

    public Date previousDate(String aDate, String Currency, BankFusionEnvironment env, String messageType) {

        /*
         * returns the previous day of the passed date.
         */

        int previousDay = -1;
        Calendar cal = null;
        String[] date;

        cal = Calendar.getInstance();
        date = aDate.split("-");
        cal.clear();
        cal.set(Integer.parseInt(date[0]), Integer.parseInt(date[1]) - 1, Integer.parseInt(date[2]));
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 1);
        cal.add(cal.DATE, previousDay);
        return new Date(cal.getTimeInMillis());
    }

    /**
     * 
     * @param Amount
     * @param noDecimalPoints
     * @return
     */
    public String DecimalRounding(String amount, int noDecimalPoints) {

        if (amount.indexOf(".") == -1 && noDecimalPoints == 0)
            return amount;
        if (amount.indexOf(".") == -1 && noDecimalPoints > 0) {
            amount = amount.concat(".");
            for (int i = 0; i < noDecimalPoints; i++) {
                amount = amount.concat("0");
            }
            return amount;
        }
        if (noDecimalPoints > 1) {
            int decimal = amount.indexOf(".");
            String afterDecimal = amount.substring(decimal + 1);
            if (afterDecimal.length() < noDecimalPoints) {
                for (int i = 0; i < noDecimalPoints; i++) {
                    amount = amount.concat("0");
                }
            }
            return amount.substring(0, amount.indexOf(".") + 1 + noDecimalPoints);
        }
        else return amount.substring(0, amount.indexOf("."));
    }

    /**
     * 
     * @param currency
     * @param env
     * @return decimalPlace
     */
    @SuppressWarnings("rawtypes")
    public int noDecimalPlaces(String currency, BankFusionEnvironment env) {
        int decimalPlace = 0;
        IBOCurrency currencyDtls = CurrencyUtil.getCurrencyDetailsOfCurrentZone(currency);
        if (null != currencyDtls) {
            decimalPlace = currencyDtls.getF_CURRENCYSCALE();
        }
        else {
            logger.error("Currency not available on this " + currency);
            EventsHelper.handleEvent(ChannelsEventCodes.E_ADDRESS_LINE_IS_MANDATORY_WITH_PARTY_IDENTIFIER,
                    new Object[] { currency }, new HashMap(), env);
        }
        return decimalPlace;
    }

    /**
     * 
     * @param bicCheck
     * @return updateBicCheck
     */
    public String convertBicCheck(String bicCheck) {
        String updateBicCheck = bicCheck;
        updateBicCheck = updateBicCheck.replace('0', 'a');
        updateBicCheck = updateBicCheck.replace('1', 'b');
        updateBicCheck = updateBicCheck.replace('2', 'c');
        updateBicCheck = updateBicCheck.replace('3', 'd');
        updateBicCheck = updateBicCheck.replace('4', 'e');
        updateBicCheck = updateBicCheck.replace('5', 'f');
        updateBicCheck = updateBicCheck.replace('6', 'g');
        updateBicCheck = updateBicCheck.replace('7', 'h');
        updateBicCheck = updateBicCheck.replace('8', 'i');
        updateBicCheck = updateBicCheck.replace('9', 'j');
        return updateBicCheck;
    }

    /**
     * 
     * @param value
     * @return
     */
    public String nonZeroValues(String value) {
        String nonZerovalue = " ";
        int j = value.length();
        int firstValuefetched = 0;
        for (int i = value.length() - 1; i >= 0 && nonZerovalue.trim().length() < 4; i--) {
            if ((value.substring(i, j).compareTo("0") != 0 || firstValuefetched == 1)
                    && (value.substring(i, j).compareTo(".") != 0)) {
                nonZerovalue = value.substring(i, j).concat(nonZerovalue);
                firstValuefetched = 1;
            }

            j--;
        }
        return concateZeroBefore(nonZerovalue.trim());
    }

    /**
     * 
     * @param nonZeroValue
     * @return concatenated string with zero.
     */
    private String concateZeroBefore(String nonZeroValue) {
        if (nonZeroValue.trim().length() == 4)
            return nonZeroValue;
        else if (nonZeroValue.trim().length() == 3)
            return "0" + nonZeroValue;
        else if (nonZeroValue.trim().length() == 2)
            return "00" + nonZeroValue;
        else if (nonZeroValue.trim().length() == 1)
            return "000" + nonZeroValue;
        else return "0000";

    }

    /**
     * 
     * @param Amount1
     * @param Amount2
     * @return
     */
    public BigDecimal BigDecimalSubtract(BigDecimal Amount1, BigDecimal Amount2) {

        // return new BigDecimal(Amount1.doubleValue() - Amount2.doubleValue());
        return Amount1.subtract(Amount2);

    }

    /**
     * 
     * @param env
     * @param msgType
     * @param disposalId
     * @return msgStatus
     */
    public int updateFlagValues(BankFusionEnvironment env, int msgType, String disposalId) {
        int msgStatus = 0;
        IBOSWTDisposal swtDisposal = (IBOSWTDisposal) env.getFactory().findByPrimaryKey(IBOSWTDisposal.BONAME, disposalId);

        if (msgType == 202) {
            msgStatus = updateMsgStatusFlag(swtDisposal.getF_CONFIRMATIONFLAG(), swtDisposal.getF_RECEIPTFLAG(),
                    swtDisposal.getF_CRDRCONFIRMATIONFLAG());
        }

        else if (msgType == 210) {
            msgStatus = updateMsgStatusFlag(swtDisposal.getF_CONFIRMATIONFLAG(), swtDisposal.getF_PAYMENTFLAG(),
                    swtDisposal.getF_CRDRCONFIRMATIONFLAG());
        }

        else if (msgType == 900910) {
            msgStatus = updateMsgStatusFlagfor900(swtDisposal.getF_CONFIRMATIONFLAG(), swtDisposal.getF_PAYMENTFLAG(),
                    swtDisposal.getF_RECEIPTFLAG());
        }

        else {
            msgStatus = updateMsgStatusFlag(swtDisposal.getF_RECEIPTFLAG(), swtDisposal.getF_PAYMENTFLAG(),
                    swtDisposal.getF_CRDRCONFIRMATIONFLAG());
        }

        return msgStatus;
    }

    /**
     * 
     * @param flag1
     * @param flag2
     * @param flag3
     * @return msgStatus
     */
    private int updateMsgStatusFlag(int flag1, int flag2, int flag3) {
        int msgStatus = 0;
        if (flag1 == 0 || flag2 == 0 || flag3 == 0 || flag1 == 2 || flag2 == 2 || flag3 == 1 || flag3 == 4 || flag3 == 5)
            msgStatus = 1;
        else msgStatus = 2;
        return msgStatus;
    }

    /**
     * 
     * @param flag1
     * @param flag2
     * @param flag3
     * @return msgStatus for 900
     */
    private int updateMsgStatusFlagfor900(int flag1, int flag2, int flag3) {
        int msgStatus = 0;
        if (flag1 == 0 || flag2 == 0 || flag3 == 0 || flag1 == 2 || flag2 == 2 || flag3 == 2)
            msgStatus = 1;
        else msgStatus = 2;
        return msgStatus;
    }

    /**
     * 
     * @param flag1
     * @param flag2
     * @param flag3
     * @param flag4
     * @return status of flag4
     */
    private int CancelFlag(int flag1, int flag2, int flag3, int flag4) {

        if ((flag1 == 3 || flag1 == 9) && (flag2 == 3 || flag2 == 9) && (flag3 == 9 || flag3 == 6 || flag3 == 7))
            flag4 = 1;
        else flag4 = 0;

        return flag4;
    }

    /**
     * 
     * @param flag1
     * @param flag2
     * @param flag3
     * @param flag4
     * @return status of flag4 incase of 900910
     */
    private int CancelFlag900910(int flag1, int flag2, int flag3, int flag4) {

        if ((flag1 == 3 || flag1 == 9) && (flag2 == 3 || flag2 == 9) && (flag3 == 3 || flag3 == 9))
            flag4 = 1;
        else flag4 = 0;

        return flag4;
    }

    /**
     * 
     * @param accountno
     * @param env
     * @return boolean
     */
    public boolean isSwiftNostro(String accountno, BankFusionEnvironment env) {

        HashMap hashmapout = new HashMap();
        HashMap hashmap = new HashMap();
        hashmap.put("ACCOUNTID", accountno);
        hashmapout = MFExecuter.executeMF("UB_SWT_IdentifyNostroAccount_SRV", env, hashmap);
        Boolean f = (Boolean) hashmapout.get("RESULT");

        return f;

    }

    /**
     * 
     * @param accountno
     * @param env
     * @return boolean
     */
    @SuppressWarnings({ "deprecation", "unchecked" })
    public boolean isSwiftVostro(String accountno, BankFusionEnvironment env) {
        PaymentSwiftUtils utils = new PaymentSwiftUtils();
        String pseudoname = utils.getModuleConfigValue(VOSTROPARAMMANE, ModuleName);
        List list = null;
        ArrayList params = new ArrayList();
        params.add("%" + pseudoname + "%");
        params.add(accountno);
        list = env.getFactory().findByQuery(IBOPseudonymAccountMap.BONAME, whereClause1, params, null);
        if (list.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * 
     * @param env
     * @param msgType
     * @param disposalId
     * @return cancelStatus
     */
    public int updateCancelFlag(BankFusionEnvironment env, int msgType, String disposalId) {

        UB_SWT_DisposalObject disposalObject = new UB_SWT_DisposalObject();
        int cancelStatus = disposalObject.getCancelFlag();
        IBOSWTDisposal swtDisposal = (IBOSWTDisposal) env.getFactory().findByPrimaryKey(IBOSWTDisposal.BONAME, disposalId);
        // int cancelStatus = swtDisposal.getF_CANCELFLAG();
        if (msgType == 202292) {
            cancelStatus = CancelFlag(swtDisposal.getF_CONFIRMATIONFLAG(), swtDisposal.getF_RECEIPTFLAG(),
                    swtDisposal.getF_CRDRCONFIRMATIONFLAG(), swtDisposal.getF_CANCELFLAG());
        }

        else if (msgType == 210292) {
            cancelStatus = CancelFlag(swtDisposal.getF_CONFIRMATIONFLAG(), swtDisposal.getF_PAYMENTFLAG(),
                    swtDisposal.getF_CRDRCONFIRMATIONFLAG(), swtDisposal.getF_CANCELFLAG());
        }

        else if (msgType == 900910) {
            cancelStatus = CancelFlag900910(swtDisposal.getF_CONFIRMATIONFLAG(), swtDisposal.getF_PAYMENTFLAG(),
                    swtDisposal.getF_RECEIPTFLAG(), swtDisposal.getF_CANCELFLAG());
        }

        else {
            cancelStatus = CancelFlag(swtDisposal.getF_RECEIPTFLAG(), swtDisposal.getF_PAYMENTFLAG(),
                    swtDisposal.getF_CRDRCONFIRMATIONFLAG(), swtDisposal.getF_CANCELFLAG());
        }

        return cancelStatus;
    }

    // MT103+ starts
    /**
     * @author Bhavya Gupta
     * @param disposalObject
     * @return sender to receiever info
     */
    public String getSenderToReceiverInfo(UB_SWT_DisposalObject disposalObject) {
        StringBuffer temp58 = new StringBuffer();

        if (!verifyForNull(disposalObject.getSenderToReceiverInfo1()).equals(CommonConstants.EMPTY_STRING)) {
            temp58.append(disposalObject.getSenderToReceiverInfo1());
            temp58.append(SWT_Constants.delimiter);
        }
        if (!verifyForNull(disposalObject.getSenderToReceiverInfo2()).equals(CommonConstants.EMPTY_STRING)) {
            temp58.append(disposalObject.getSenderToReceiverInfo2());
            temp58.append(SWT_Constants.delimiter);
        }
        if (!verifyForNull(disposalObject.getSenderToReceiverInfo3()).equals(CommonConstants.EMPTY_STRING)) {
            temp58.append(disposalObject.getSenderToReceiverInfo3());
            temp58.append(SWT_Constants.delimiter);
        }
        if (!verifyForNull(disposalObject.getSenderToReceiverInfo4()).equals(CommonConstants.EMPTY_STRING)) {
            temp58.append(disposalObject.getSenderToReceiverInfo4());
            temp58.append(SWT_Constants.delimiter);
        }
        if (!verifyForNull(disposalObject.getSenderToReceiverInfo5()).equals(CommonConstants.EMPTY_STRING)) {
            temp58.append(disposalObject.getSenderToReceiverInfo5());
            temp58.append(SWT_Constants.delimiter);
        }
        if (!verifyForNull(disposalObject.getSenderToReceiverInfo6()).equals(CommonConstants.EMPTY_STRING)) {
            temp58.append(disposalObject.getSenderToReceiverInfo6());
            temp58.append(SWT_Constants.delimiter);
        }

        return temp58.toString();
    }

    // MT103+ ends

    /**
     * @author angraj
     * @param disposalObject
     * @return Terms And Condition For Deals
     */
    public String getTermsAndConditionForDeals(UB_SWT_DisposalObject disposalObject) {
        StringBuffer temp59 = new StringBuffer();

        if (!verifyForNull(disposalObject.getTermsAndConditionForDeals1()).equals(CommonConstants.EMPTY_STRING)) {
            temp59.append(disposalObject.getTermsAndConditionForDeals1());
            temp59.append(SWT_Constants.delimiter);
        }
        if (!verifyForNull(disposalObject.getTermsAndConditionForDeals2()).equals(CommonConstants.EMPTY_STRING)) {
            temp59.append(disposalObject.getTermsAndConditionForDeals2());
            temp59.append(SWT_Constants.delimiter);
        }
        if (!verifyForNull(disposalObject.getTermsAndConditionForDeals3()).equals(CommonConstants.EMPTY_STRING)) {
            temp59.append(disposalObject.getTermsAndConditionForDeals3());
            temp59.append(SWT_Constants.delimiter);
        }
        if (!verifyForNull(disposalObject.getTermsAndConditionForDeals4()).equals(CommonConstants.EMPTY_STRING)) {
            temp59.append(disposalObject.getTermsAndConditionForDeals4());
            temp59.append(SWT_Constants.delimiter);
        }
        if (!verifyForNull(disposalObject.getTermsAndConditionForDeals5()).equals(CommonConstants.EMPTY_STRING)) {
            temp59.append(disposalObject.getTermsAndConditionForDeals5());
            temp59.append(SWT_Constants.delimiter);
        }
        if (!verifyForNull(disposalObject.getTermsAndConditionForDeals6()).equals(CommonConstants.EMPTY_STRING)) {
            temp59.append(disposalObject.getTermsAndConditionForDeals6());
            temp59.append(SWT_Constants.delimiter);
        }

        return temp59.toString();
    }

    public Map getAddress(String customerCode, BankFusionEnvironment env) {
        Map<String, Object> list = new HashMap<String, Object>();
        Map<String, Object> addressList = new HashMap<String, Object>();
        addressList.put(CUSTOMERCODE, customerCode);
        Map<String, Object> addressDetails = MFExecuter.executeMF(GETADDRESS, env, addressList);
        String addressline1 = addressDetails.get(ADDRESSLINE1).toString();
        String addressline2 = addressDetails.get(ADDRESSLINE2).toString();
        String addressline3 = addressDetails.get(ADDRESSLINE3).toString();
        String addressline4 = addressDetails.get(ADDRESSLINE4).toString();
        String addressline5 = addressDetails.get(ADDRESSLINE5).toString();
        String addressline6 = addressDetails.get(ADDRESSLINE6).toString();
        String addressline7 = addressDetails.get(ADDRESSLINE7).toString();
        list.put(ADDRESSLINE1, addressline1);
        list.put(ADDRESSLINE2, addressline2);
        list.put(ADDRESSLINE3, addressline3);
        list.put(ADDRESSLINE4, addressline4);
        list.put(ADDRESSLINE5, addressline5);
        list.put(ADDRESSLINE6, addressline6);
        list.put(ADDRESSLINE7, addressline7);
        return list;
    }

    public static SwiftMT103NarrativeCodes generateMT103ComplexType(UB_MT103 mt103) {
        SwiftMT103NarrativeCodes complexMT103 = new SwiftMT103NarrativeCodes();
        if (mt103.getAccountWithInstitution() != null)
            complexMT103.setAccountWithInstitution(mt103.getAccountWithInstitution());
        complexMT103.setBankOperationCode(mt103.getBankOperationCode());
        String[] beneficiaryCustomer = getSeprateTags(mt103.getBeneficiaryCustomer());
        complexMT103.setBeneficiaryCustomer_1(beneficiaryCustomer[0]);
        complexMT103.setBeneficiaryCustomer_2(beneficiaryCustomer[1]);
        complexMT103.setBeneficiaryCustomer_3(beneficiaryCustomer[2]);
        complexMT103.setBeneficiaryCustomer_4(beneficiaryCustomer[3]);
        if (mt103.getCharges().size() > 0) {
            complexMT103.setCharges(((SendersCharges) mt103.getCharges().get(0)).getSenderCharge());
        }
        complexMT103.setDetailsOfCharges(mt103.getDetailsOfCharges());
        if (mt103.getEnvelopeContents() != null)
            complexMT103.setEnvelopeContents(mt103.getEnvelopeContents());
        if (mt103.getExchangeRate() != null) {
            complexMT103.setExchangeRate(mt103.getExchangeRate());
        }
        if (mt103.getInstructedAmount() != null)
            complexMT103.setInstructedAmount(mt103.getInstructedAmount());
        if (mt103.getInstructedCurrency() != null)
            complexMT103.setInstructedCurrency(mt103.getInstructedCurrency());
        complexMT103.setInstruction(
                (String) (mt103.getInstruction().size() > 0 ? ((InstructionCode) mt103.getInstruction().get(0)).getInstructionCode()
                        : CommonConstants.EMPTY_STRING));
        if (mt103.getIntermediaryInstitution() != null)
            complexMT103.setIntermediaryInstitution(mt103.getIntermediaryInstitution());

        if (mt103.getOrderingCustomerOption().equals("K")) {
            String[] orderingCustomer = getSeprateTags(mt103.getOrderingCustomer());
            complexMT103.setOrderingCustomerAccount50K(orderingCustomer[0]);
            complexMT103.setOrderingCustomerNameAddress50K_1(orderingCustomer[1]);
            complexMT103.setOrderingCustomerNameAddress50K_2(orderingCustomer[2]);
            complexMT103.setOrderingCustomerNameAddress50k_3(orderingCustomer[3]);
            complexMT103.setOrderingCustomerNameAddress50K_4(orderingCustomer[4]);
        }
        else {
            String[] orderingCustomer = getSeprateTags(mt103.getOrderingCustomer());
            complexMT103.setOrderingCustomerAccount50A(orderingCustomer[0]);
            complexMT103.setOrderingCustomerNameAddress50K_1(CommonConstants.EMPTY_STRING);
            complexMT103.setOrderingCustomerNameAddress50K_2(CommonConstants.EMPTY_STRING);
            complexMT103.setOrderingCustomerNameAddress50k_3(CommonConstants.EMPTY_STRING);
            complexMT103.setOrderingCustomerNameAddress50K_4(CommonConstants.EMPTY_STRING);
        }

        String[] orderingInstitution = null;
        if (mt103.getOrderingInstitution() != null && !mt103.getOrderingInstitution().isEmpty()) {
            orderingInstitution = getSeprateTags(mt103.getOrderingInstitution());
            if (mt103.getOrderInstitutionOption().equalsIgnoreCase("D")) {
                complexMT103.setOrderingInstitution52D_1(
                        orderingInstitution.length > 0 ? orderingInstitution[0] : CommonConstants.EMPTY_STRING);
                complexMT103.setOrderingInstitution52D_2(
                        orderingInstitution.length > 1 ? orderingInstitution[1] : CommonConstants.EMPTY_STRING);
                complexMT103.setOrderingInstitution52D_3(
                        orderingInstitution.length > 2 ? orderingInstitution[2] : CommonConstants.EMPTY_STRING);
                complexMT103.setOrderingInstitution52D_4(
                        orderingInstitution.length > 3 ? orderingInstitution[3] : CommonConstants.EMPTY_STRING);
            }
            else if (mt103.getOrderInstitutionOption().equalsIgnoreCase("A")) {
                complexMT103.setOrderingInstitution52A(
                        orderingInstitution.length > 0 ? orderingInstitution[0] : CommonConstants.EMPTY_STRING);
            }
        }

        complexMT103.setReceiver(mt103.getReceiver());
        if (mt103.getReceiversCharges() != null)
            complexMT103.setReceiversCharges(mt103.getReceiversCharges());
        if (mt103.getReceiversCorrespondent() != null)
            complexMT103.setReceiversCorrespondent(mt103.getReceiversCorrespondent());
        if (mt103.getRegulatoryReporting() != null)
            complexMT103.setRegulatoryReporting(mt103.getRegulatoryReporting());
        if (mt103.getRemittanceInfo() != null) {
            String[] remittanceInfo = getSeprateTags(mt103.getRemittanceInfo());
            complexMT103.setRemittanceInfo_1(remittanceInfo[0]);
            complexMT103.setRemittanceInfo_2(remittanceInfo[1]);
            complexMT103.setRemittanceInfo_3(remittanceInfo[2]);
            complexMT103.setRemittanceInfo_4(remittanceInfo[3]);
        }
        if (mt103.getSender() != null)
            complexMT103.setSender(mt103.getSender());
        String[] senderToReceiverInfo = null;
        if (mt103.getSenderToReceiverInfo() != null) {
            senderToReceiverInfo = getSeprateTags(mt103.getSenderToReceiverInfo());
            complexMT103.setSenderToReceiverInfo_1(senderToReceiverInfo[0]);
            complexMT103.setSenderToReceiverInfo_2(senderToReceiverInfo[1]);
            complexMT103.setSenderToReceiverInfo_3(senderToReceiverInfo[2]);
            complexMT103.setSenderToReceiverInfo_4(senderToReceiverInfo[3]);

        }
        if (mt103.getSendersCorrespondent() != null)
            complexMT103.setSendersCorrespondent(mt103.getSendersCorrespondent());
        complexMT103.setSendersReference(mt103.getSendersReference());
        if (mt103.getSendingInstitution() != null)
            complexMT103.setSendingInstitution(mt103.getSendingInstitution());
        complexMT103.setTdAmount(mt103.getTdAmount());
        complexMT103.setTdCurrencyCode(mt103.getTdCurrencyCode());
        complexMT103.setTdValueDate(mt103.getTdValueDate());
        if (mt103.getThirdReimbursementInstitution() != null)
            complexMT103.setThirdReimbursementInstitution(mt103.getThirdReimbursementInstitution());
        if (mt103.getTransactionTypeCode() != null) {
            complexMT103.setTransactionTypeCode(mt103.getTransactionTypeCode());
        }
        return complexMT103;
    }

    public static SwiftMT202NarrativeCodes generateMT202ComplexType(UB_MT202 mt202) {

        SwiftMT202NarrativeCodes complexMT202 = new SwiftMT202NarrativeCodes();
        if (mt202.getAccountWithInstitution() != null)
            complexMT202.setAccountWithInstitution(mt202.getAccountWithInstitution());
        if (mt202.getBeneficiary() != null)
            complexMT202.setBeneficiary(mt202.getBeneficiary());
        if (mt202.getIntermediary() != null)
            complexMT202.setIntermediary(mt202.getIntermediary());
        if (mt202.getOrderingInstitution() != null)
            complexMT202.setOrderingInstitution(mt202.getOrderingInstitution());
        if (mt202.getReceiver() != null)
            complexMT202.setReceiver(mt202.getReceiver());
        if (mt202.getReceiversCorrespondent() != null)
            complexMT202.setReceiversCorrespondent(mt202.getReceiversCorrespondent());
        if (mt202.getSender() != null)
            complexMT202.setSender(mt202.getSender());
        if (mt202.getSendersCorrespondent() != null)
            complexMT202.setSendersCorrespondent(mt202.getSendersCorrespondent());
        if (mt202.getSendertoReceiverInformation() != null)
            complexMT202.setSendertoReceiverInformation(mt202.getSendertoReceiverInformation());

        complexMT202.setRelatedReference(mt202.getRelatedReference());
        complexMT202.setTdAmount(mt202.getTdAmount());
        complexMT202.setTdCurrencyCode(mt202.getTdCurrencyCode());
        complexMT202.setTdValueDate(mt202.getTdValueDate());
        complexMT202.setTransactionReferenceNumber(mt202.getTransactionReferenceNumber());

        return complexMT202;
    }

    private static String[] getSeprateTags(String field) {
        String[] array = field.split("[$]");
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < 5; i++) {
            list.add(array.length >= i + 1 ? array[i] : CommonConstants.EMPTY_STRING);
        }
        return list.toArray(array);
    }

    public static String replaceSpecialChars(String fieldValue) {
        IBusinessInformation bizInfo = ((IBusinessInformationService) ServiceManagerFactory.getInstance().getServiceManager()
                .getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();
        String value = (String) bizInfo.getModuleConfigurationValue(SWIFTMODULE, SPLCHARPARAMNAME,
                BankFusionThreadLocal.getBankFusionEnvironment());
        value = value.replaceAll("\\\\\\\\", "");
        for (int i = 0; i < value.length(); i++) {
            String replaceString = "[\\".concat(Character.toString(value.charAt(i))).concat("]");
            fieldValue = fieldValue.replaceAll(replaceString, " ");
        }
        fieldValue = fieldValue.replaceAll("( )+", " ");
        return fieldValue;
    }
    
    /**
     * Method Description:List All Branches
     * @return
     */
    private VectorTable listAllBranches() {
        Map<String, Object> inputParams = new HashMap<String, Object>();
        Map<?, ?> outputParams = MFExecuter.executeMF("UB_FIN_ListAllBranches_SRV", BankFusionThreadLocal.getBankFusionEnvironment(), inputParams);
        VectorTable vectorList = (VectorTable) outputParams.get("BranchResult");
        return vectorList; 
    }
    

    

}
