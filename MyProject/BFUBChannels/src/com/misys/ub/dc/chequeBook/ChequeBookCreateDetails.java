package com.misys.ub.dc.chequeBook;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.BankFusionMessages;
import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.serviceinvocation.IUserExitInvokerService;
import com.misys.ub.interfaces.IfmConstants;
import com.misys.ub.interfaces.UB_IBI_PaymentsHelper;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAddressLinks;
import com.trapedza.bankfusion.bo.refimpl.IBOChequeBookType;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_CNF_ACCTMANDATE;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_IBI_ChequeBookCreateDetails;
import com.trapedza.bankfusion.steps.refimpl.IUB_IBI_ChequeBookCreateDetails;

import bf.com.misys.cbs.msgs.v1r0.ReadProductSummaryDtlsRq;
import bf.com.misys.cbs.msgs.v1r0.ReadProductSummaryDtlsRs;
import bf.com.misys.cbs.services.CalcEventChargeRq;
import bf.com.misys.cbs.services.CalcEventChargeRs;
import bf.com.misys.cbs.services.CalcExchangeRateRq;
import bf.com.misys.cbs.services.CalcExchangeRateRs;
import bf.com.misys.cbs.services.ReadModuleConfigurationRq;
import bf.com.misys.cbs.services.ReadModuleConfigurationRs;
import bf.com.misys.cbs.types.AccountKeys;
import bf.com.misys.cbs.types.CalcExchRateDetails;
import bf.com.misys.cbs.types.Currency;
import bf.com.misys.cbs.types.EventChgInputDtls;
import bf.com.misys.cbs.types.ExchangeRateDetails;
import bf.com.misys.cbs.types.InputAccount;
import bf.com.misys.cbs.types.ModuleKeyRq;
import bf.com.misys.cbs.types.ReadProductSummaryDtlsReq;
import bf.com.misys.cbs.types.dc.ChqBookCreateRes;
import bf.com.misys.cbs.types.dc.ChqBookCreateRs;
import bf.com.misys.cbs.types.dc.ChqBookRq;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.Orig;
import bf.com.misys.cbs.types.header.RqHeader;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;

public class ChequeBookCreateDetails extends AbstractUB_IBI_ChequeBookCreateDetails implements IUB_IBI_ChequeBookCreateDetails {

    /**
     * 
     */
    private static final long serialVersionUID = 3305096419755704006L;
    private IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

    final String READ_MODULE_CONFIGURATION = "CB_CMN_ReadModuleConfiguration_SRV";
    final String MODULEID = "TAX";
    final String YES = "Yes";
    private static final String CHECK_DIGIT_PARAM = "ChequeNumberWithCheckDigit";
    private static final String CBS_MODULE = "CBS";
    private String channelId = "";
    private transient final static Log logger = LogFactory.getLog(ChequeBookCreateDetails.class.getName());

    private static final String VALID_KYC_STATUS = "001";
    private static final String CUSTOMER_STATUS = "001";

    public ChequeBookCreateDetails(BankFusionEnvironment env) {
        super(env);
    }

    public void process(BankFusionEnvironment env) {

        ChqBookRq chqBookRq = getF_IN_chqBookRq();
        String accID = chqBookRq.getChqBookReq().getAccountID();
        channelId = chqBookRq.getRqHeader().getOrig().getChannelId();
        String formattedMessage;

        // message header to be added or not

        ArrayList param = new ArrayList();
       
       
        IBOAccount mainAccount = (IBOAccount) factory.findByPrimaryKey(IBOAccount.BONAME, accID, true);
        if (mainAccount == null) {
            formattedMessage = BankFusionMessages.getInstance().getFormattedEventMessage(Integer.parseInt("20020824"), null,
                    BankFusionThreadLocal.getUserSession().getUserLocale(), true);
            setErrorResponse("20020824", formattedMessage);
            return;
        }

        String checkDigitParam = getModuleConfigValue(CHECK_DIGIT_PARAM, CBS_MODULE);
        boolean isAccountIDNumeric = StringUtils.isNumeric(accID);

        if (YES.equalsIgnoreCase(checkDigitParam) && !isAccountIDNumeric) {
            formattedMessage = BankFusionMessages.getInstance().getFormattedEventMessage(Integer.parseInt("40009289"),
                    new Object[] { "Account ID" }, BankFusionThreadLocal.getUserSession().getUserLocale(), true);
            setErrorResponse("40009289", formattedMessage);
            return;
        }

        ArrayList<String> param1 = new ArrayList<String>();

        param1.add(accID);
        List<IBOUB_CNF_ACCTMANDATE> jointAcct = (List<IBOUB_CNF_ACCTMANDATE>) factory.findByQuery(IBOUB_CNF_ACCTMANDATE.BONAME,
                "where " + IBOUB_CNF_ACCTMANDATE.UBACCOUNTID + " =  ? ", param1, null);

        ArrayList<String> acctholders = new ArrayList<String>();
        for (IBOUB_CNF_ACCTMANDATE rec : jointAcct) {
            acctholders.add(rec.getF_UBCUSTOMERCODE());
        }

        String custID = chqBookRq.getChqBookReq().getCustomerId();
        String customerID = mainAccount.getF_CUSTOMERCODE();
        Boolean isJointCust = false;

        for (String jointcust : acctholders) {
            if (jointcust.equalsIgnoreCase(custID)) {
                isJointCust = true;
                break;
            }
        }

        if (!customerID.equalsIgnoreCase(custID) && !isJointCust) {

            formattedMessage = BankFusionMessages.getInstance().getFormattedEventMessage(Integer.parseInt("40209136"), null,
                    BankFusionThreadLocal.getUserSession().getUserLocale(), true);
            setErrorResponse("40209136", formattedMessage);
            return;
        }

        boolean accountAndCustomerStatus = validateAccountAndCustomer(mainAccount, custID);
        if (accountAndCustomerStatus == false) {
            return;
        }

        boolean isAccountValidations = isAccountPasswordProtected(mainAccount.getBoID(), IfmConstants.DR);

        if (isAccountValidations == false) {
            return;
        }

        if (YES.equalsIgnoreCase(checkDigitParam) && (getF_IN_chqBookRq().getChqBookReq().getChequeBookType() == null
                || getF_IN_chqBookRq().getChqBookReq().getChequeBookType().isEmpty())) {

            formattedMessage = BankFusionMessages.getInstance().getFormattedEventMessage(Integer.parseInt("40112610"), null,
                    BankFusionThreadLocal.getUserSession().getUserLocale(), true);
            setErrorResponse("40112610", formattedMessage);
            return;
        }
        boolean typeAndNumberOfLeaves = validateChequeBookType(mainAccount, env);
        if (typeAndNumberOfLeaves == false) {

            return;
        }

        param.clear();
        param.add(mainAccount.getF_PRODUCTID());
        param.add(getF_IN_chqBookRq().getChqBookReq().getNumberOfLeaves());

        StringBuilder whereClause = new StringBuilder();
        whereClause.append("where " + IBOChequeBookType.CHEQUETYPEID + "= ? and " + IBOChequeBookType.NUMBEROFLEAVES + "= ?");

        if (YES.equalsIgnoreCase(checkDigitParam)) {
            param.add(getF_IN_chqBookRq().getChqBookReq().getChequeBookType());
            whereClause.append(" and " + IBOChequeBookType.CHEQUETYPECODE + "= ? ");
        }

        @SuppressWarnings("deprecation")
        IBOChequeBookType chequeBookType = (IBOChequeBookType) factory.findFirstByQuery(IBOChequeBookType.BONAME,
                whereClause.toString(), param);

        if (chequeBookType == null) {
            formattedMessage = BankFusionMessages.getInstance().getFormattedEventMessage(Integer.parseInt("40421573"), null,
                    BankFusionThreadLocal.getUserSession().getUserLocale(), true);
            setErrorResponse("40421573", formattedMessage);
            return;
        }

        if (getF_IN_chqBookRq().getChqBookReq().getNumberofChqBooks() == 0) {

            formattedMessage = BankFusionMessages.getInstance().getFormattedEventMessage(Integer.parseInt("40112612"), null,
                    BankFusionThreadLocal.getUserSession().getUserLocale(), true);
            setErrorResponse("40112612", formattedMessage);
            return;
        }
        if (getF_IN_chqBookRq().getChqBookReq().getNumberofChqBooks() > 99
                || getF_IN_chqBookRq().getChqBookReq().getNumberofChqBooks() < 1) {

            formattedMessage = BankFusionMessages.getInstance().getFormattedEventMessage(Integer.parseInt("40112613"), null,
                    BankFusionThreadLocal.getUserSession().getUserLocale(), true);
            setErrorResponse("40112613", formattedMessage);
            return;
        }

        ReadProductSummaryDtlsRs productResponse = readProductSummaryDetails(mainAccount.getF_PRODUCTCONTEXTCODE());
        CalcEventChargeRs charges = new CalcEventChargeRs();

        charges = calculateCharge(chqBookRq, productResponse);

        Currency charge = new Currency();

        if (!charges.getChargeResults().getIsWaived() && charges.getChargeResults().getNoOfChgTaxAmtDetails() > 0) {
            charge.setAmount(charges.getChargeResults().getTotalChgTaxAmtTxnCur().getAmount());
            charge.setIsoCurrencyCode(charges.getChargeResults().getTotalChgAmtTxnCur().getIsoCurrencyCode());
        }
        else {
            charge.setAmount(BigDecimal.ZERO);
            charge.setIsoCurrencyCode(mainAccount.getF_ISOCURRENCYCODE());
        }
        String taxTransactionCode = null;
        String taxReceivingAccount = null;
        Currency taxPerLeave = new Currency();
        BigDecimal taxPerLeaveAmount = CommonConstants.BIGDECIMAL_ZERO;
        if (getModuleConfigValue("IS_CHQ_TAX_PER_LEAF", MODULEID).equalsIgnoreCase("true")) {
            String taxCurrency = getModuleConfigValue("CHQ_TAX_CURRENCY", MODULEID);
            taxTransactionCode = getModuleConfigValue("CHQ_TAX_TRANSACTION_CODE", MODULEID);
            taxReceivingAccount = getModuleConfigValue("TAX_RECEIVING_ACCOUNT", MODULEID);
            taxPerLeaveAmount = calcTax(chequeBookType, taxCurrency, taxTransactionCode, mainAccount.getF_ISOCURRENCYCODE(), env);
            taxPerLeave.setAmount(taxPerLeaveAmount);

        }
        else {
            taxPerLeave.setAmount(BigDecimal.ZERO);
        }

        Currency tax = new Currency();
        tax.setAmount(taxPerLeave.getAmount());
        tax.setIsoCurrencyCode(mainAccount.getF_ISOCURRENCYCODE());

        boolean validateAccount = validateAccountForAmount(mainAccount, env,
                charges.getChargeResults().getTotalChgTaxAmtTxnCur().getAmount(), tax.getAmount());
        if (validateAccount == false) {
            return;
        }

        if (!getF_IN_chqBookRq().getChqBookReq().getCollectAtBranch()) {
            param.clear();
            param.add(custID);
            IBOAddressLinks addressLinks = (IBOAddressLinks) factory.findFirstByQuery(IBOAddressLinks.BONAME,
                    "where " + IBOAddressLinks.CUSTACC_KEY + " =  ? ", param);
            if (addressLinks != null) {
                getF_IN_chqBookRq().getChqBookReq().setBranchCode(addressLinks.getF_ADDRESSID());
            }
        }

        IUserExitInvokerService userExitInvokerService = (IUserExitInvokerService) ServiceManagerFactory.getInstance()

                .getServiceManager().getServiceForName(IUserExitInvokerService.SERVICE_NAME);

        userExitInvokerService.invokeService("chequeBookForecastAndIssue", getF_IN_chqBookRq());

        ChqBookCreateRs chqBookCreateRs = new ChqBookCreateRs();
        RsHeader rsHeader = new RsHeader();
        MessageStatus messageStatus = new MessageStatus();
        SubCode subCode = new SubCode();
        subCode.setCode("");
        subCode.setDescription("");
        messageStatus.addCodes(subCode);
        messageStatus.setOverallStatus("S");
        rsHeader.setStatus(messageStatus);
        rsHeader.setOrigCtxtId(getF_IN_chqBookRq().getRqHeader().getOrig().getChannelId());
        chqBookCreateRs.setRsHeader(rsHeader);
        setF_OUT_chequeBookId(chequeBookType.getBoID());
        setF_OUT_Tax(taxPerLeave);
        setF_OUT_productResponse(productResponse);
        setF_OUT_taxReceivingAccount(taxReceivingAccount);
        setF_OUT_taxTransactionCode(taxTransactionCode);
        setF_OUT_chqBookCreateRs(chqBookCreateRs);
    }

    private boolean validateAccountForAmount(IBOAccount mainAccount, BankFusionEnvironment env, BigDecimal finalCharge,
            BigDecimal finalTax) {

        HashMap<String, Object> inputparam = new HashMap<String, Object>();
        HashMap<String, Object> outputparam = new HashMap<String, Object>();

        String formattedMessage;

        inputparam.put("AccountID", mainAccount.getBoID());

        outputparam = MFExecuter.executeMF("GetAvailableBalance", env, inputparam);

        BigDecimal availbleBalance = (BigDecimal) outputparam.get("AvailableBalance");

        finalCharge = finalCharge.add(finalTax);
        if (availbleBalance.compareTo(finalCharge) != -1) // available Balance
                                                          // <= eventCharge
        {
            return true;
        }
        else {

            formattedMessage = BankFusionMessages.getInstance().getFormattedEventMessage(
                    // insufficient available balance
                    Integer.parseInt("40009269"), null, BankFusionThreadLocal.getUserSession().getUserLocale(), true);
            setErrorResponse("40009269", formattedMessage);
            return false;
        }

    }

    private void setErrorResponse(String errorCode, String formattedMsg) {
        ChqBookCreateRs chqBookCreateRs = new ChqBookCreateRs();
        ChqBookCreateRes chqBookCreateRes = new ChqBookCreateRes();
        RsHeader rsHeader = new RsHeader();
        MessageStatus messageStatus = new MessageStatus();
        SubCode subCode = new SubCode();
        subCode.setCode(errorCode);
        subCode.setDescription(formattedMsg);
        messageStatus.addCodes(subCode);
        messageStatus.setOverallStatus("F");
        rsHeader.setStatus(messageStatus);
        rsHeader.setOrigCtxtId(getF_IN_chqBookRq().getRqHeader().getOrig().getChannelId());
        chqBookCreateRs.setRsHeader(rsHeader);
        chqBookCreateRes.setOrderStatus("");
        chqBookCreateRs.setChqBookCreateRes(chqBookCreateRes);
        setF_OUT_chqBookCreateRs(chqBookCreateRs);
    }

    private CalcEventChargeRs calculateCharge(ChqBookRq chqBookRq, ReadProductSummaryDtlsRs productResponse) {
        CalcEventChargeRq claceEventChargeRq = new CalcEventChargeRq();
        EventChgInputDtls eventChgInputDtls = new EventChgInputDtls();
        AccountKeys accountKeys = new AccountKeys();
        InputAccount inputAcct = new InputAccount();
        inputAcct.setInputAccountId(chqBookRq.getChqBookReq().getAccountID());
        accountKeys.setStandardAccountId(chqBookRq.getChqBookReq().getAccountID());
        accountKeys.setInputAccount(inputAcct);
        eventChgInputDtls.setAccountId(accountKeys);
        eventChgInputDtls.setEventCategory("CORE");
        eventChgInputDtls.setEventSubCategory("40112127");
        eventChgInputDtls
                .setProductCategory(productResponse.getReadProductSummaryDtlsRes().getProductSummaryDtls().getProductCategory());
        eventChgInputDtls.setProductId(productResponse.getReadProductSummaryDtlsRes().getProductSummaryDtls().getProductID());
        if (channelId.equalsIgnoreCase("CCI")) {
            eventChgInputDtls.setChannelId("CCI");
        }
        else {
            eventChgInputDtls.setChannelId("IBI");
        }
        eventChgInputDtls.setChgFundingAccount(accountKeys);
        claceEventChargeRq.setEventChgInputDtls(eventChgInputDtls);
        HashMap<String, Object> MfParams = new HashMap<String, Object>();
        MfParams.put("CalcEventChargeRq", claceEventChargeRq);
        HashMap outputChargeParams = MFExecuter.executeMF("CB_CHG_CalculateEventCharges_SRV",
                BankFusionThreadLocal.getBankFusionEnvironment(), MfParams);
        CalcEventChargeRs calcEventChargesRs = (CalcEventChargeRs) outputChargeParams.get("CalcEventChargesRs");
        return calcEventChargesRs;
    }

    private BigDecimal calcTax(IBOChequeBookType chequeBookType, String taxCurrency, String taxTransactionCode,
            String f_ISOCURRENCYCODE, BankFusionEnvironment env) {
        BigDecimal finaltax = CommonConstants.BIGDECIMAL_ZERO;
        if (!taxCurrency.equalsIgnoreCase(f_ISOCURRENCYCODE)) {
            IBusinessInformation bizInformation = ((IBusinessInformationService) ServiceManagerFactory.getInstance()
                .getServiceManager().getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();
            IBOMisTransactionCodes transactionCode =
                (IBOMisTransactionCodes) bizInformation.getMisTransactionCodeDetails(taxTransactionCode);
            String exchangeRateType = transactionCode.getF_EXCHANGERATETYPE();
            BigDecimal totalTax = totalTaxPerLeave(chequeBookType);
            BigDecimal exchangeRate = computeExchangeRate(taxCurrency, f_ISOCURRENCYCODE, exchangeRateType, totalTax, env);
            finaltax = calculateExchRateAmt(taxCurrency, f_ISOCURRENCYCODE, exchangeRate, totalTax);
            return finaltax;
        }
        else {
            finaltax = totalTaxPerLeave(chequeBookType);
            return finaltax;
        }
    }

    private BigDecimal totalTaxPerLeave(IBOChequeBookType chequeBookType) {
        Double taxPerLeaf = chequeBookType.getF_TAXPERLEAF().doubleValue();
        Double totTax = taxPerLeaf * chequeBookType.getF_NUMBEROFLEAVES();
        BigDecimal taxPerLeave = new BigDecimal(totTax);
        return taxPerLeave;
    }

    private BigDecimal calculateExchRateAmt(String buyCurrency, String sellCurrency, BigDecimal exchangeRate,
            BigDecimal buyAmount) {

        RqHeader rqHeader = new RqHeader();
        Orig orig = new Orig();
        if (channelId.equalsIgnoreCase("CCI")) {
            orig.setChannelId("CCI");
        }
        else {
            orig.setChannelId("IBI");
        }
        rqHeader.setOrig(orig);
        CalcExchangeRateRq exchRq = new CalcExchangeRateRq();
        CalcExchRateDetails exchangeDtls = new CalcExchRateDetails();
        exchangeDtls.setSellAmount(BigDecimal.ZERO);
        if (buyAmount.signum() < 0) {
            exchangeDtls.setBuyAmount(buyAmount.abs());
        }
        else {
            exchangeDtls.setBuyAmount(buyAmount);
        }

        exchangeDtls.setBuyCurrency(buyCurrency);
        exchangeDtls.setSellCurrency(sellCurrency);
        exchRq.setCalcExchRateDetails(exchangeDtls);
        ExchangeRateDetails exchangeRateDetails = new ExchangeRateDetails();
        exchangeRateDetails.setExchangeRate(exchangeRate);
        exchangeRateDetails.setExchangeRateType("SPOT");
        exchangeDtls.setExchangeRateDetails(exchangeRateDetails);
        exchRq.setRqHeader(rqHeader);
        BankFusionEnvironment env = new BankFusionEnvironment(null);
        HashMap inputMap = new HashMap();
        inputMap.put("CalcExchangeRateRq", exchRq);
        env.setData(new HashMap());
        HashMap outputParams = MFExecuter.executeMF("CB_FEX_CalculateExchangeRateAmount_SRV", env, inputMap);

        CalcExchangeRateRs calcExchangeRateRs = (CalcExchangeRateRs) outputParams.get("CalcExchangeRateRs");
        BigDecimal equivalentAmount = calcExchangeRateRs.getCalcExchRateResults().getSellAmountDetails().getAmount();
        if (buyAmount.signum() < 0) {
            equivalentAmount = BigDecimal.ZERO.subtract(equivalentAmount);
        }
        return equivalentAmount;
    }

    private boolean validateAccountAndCustomer(IBOAccount mainAccount, String customerId) {

        Boolean isClosed = mainAccount.isF_CLOSED();
        Boolean isStopped = mainAccount.isF_STOPPED();
        Boolean isDormant = mainAccount.isF_DORMANTSTATUS();
        String errorCode;
        String formattedMessage;
        Object[] params = { mainAccount.getBoID() };
        if (isClosed == true || isStopped == true || isDormant == true) {
            if (isClosed) {
                errorCode = "40407566";
            }
            else if (isStopped) {
                errorCode = "40400055";
            }
            else {
                errorCode = "40400057";
            }
            formattedMessage = BankFusionMessages.getInstance().getFormattedEventMessage(Integer.parseInt(errorCode), params,
                    BankFusionThreadLocal.getUserSession().getUserLocale(), true);
            setErrorResponse(errorCode, formattedMessage);
            return false;
        }
        

        IBOCustomer customer = (IBOCustomer) factory.findByPrimaryKey(IBOCustomer.BONAME, customerId,true);
        
        if (customer != null) {

            if (!CUSTOMER_STATUS.equals("001")) {
                errorCode = "40411056";
                formattedMessage = BankFusionMessages.getInstance().getFormattedEventMessage(Integer.parseInt(errorCode), params,
                        BankFusionThreadLocal.getUserSession().getUserLocale(), true);
                setErrorResponse(errorCode, formattedMessage);
                return false;
            }
            if (!VALID_KYC_STATUS.equals(customer.getF_UBKYCSTATUS())) {
                errorCode = "34000151";
                formattedMessage = BankFusionMessages.getInstance().getFormattedEventMessage(Integer.parseInt(errorCode), params,
                        BankFusionThreadLocal.getUserSession().getUserLocale(), true);
                setErrorResponse(errorCode, formattedMessage);
                return false;
            }
            return true;
        }
        else {
            errorCode = "40401080";
            formattedMessage = BankFusionMessages.getInstance().getFormattedEventMessage(Integer.parseInt(errorCode), params,
                    BankFusionThreadLocal.getUserSession().getUserLocale(), true);
            setErrorResponse(errorCode, formattedMessage);
            return false;
        }
    }

    public boolean isAccountPasswordProtected(String accountId, String accountType) {
        int accRightIndicator = 0;
        Map<String, String> map = new HashMap<String, String>();
        Map outPutMap = new HashMap();
        map.put("AccountID", accountId);
        outPutMap = UB_IBI_PaymentsHelper.getAccountDetails(accountId);
        Object[] params = { accountId };
        String errorCode;
        String formattedMessage;
        if (accountType.equalsIgnoreCase(IfmConstants.DR)) {
            if (outPutMap.get("ACCRIGHTSINDICATOR") != null) {
                accRightIndicator = (Integer) outPutMap.get("ACCRIGHTSINDICATOR");
                if (accRightIndicator == 1 || accRightIndicator == 9)
                    errorCode = "40007319";
                else if (accRightIndicator == -1)
                    errorCode = "40007318";
                else if (accRightIndicator == 2)
                    errorCode = "40007321";
                else if (accRightIndicator == 3)
                    errorCode = "40112172";
                else if (accRightIndicator == 4)
                    errorCode = "40007322";
                else if (accRightIndicator == 5)
                    errorCode = "40007323";
                else {
                    return true;
                }
                formattedMessage = BankFusionMessages.getInstance().getFormattedEventMessage(Integer.parseInt(errorCode), params,
                        BankFusionThreadLocal.getUserSession().getUserLocale(), true);
                setErrorResponse(errorCode, formattedMessage);
                return false;
            }
            else {
                return true;
            }
        }
        else {
            if (outPutMap.get("ACCRIGHTSINDICATOR") != null) {
                accRightIndicator = (Integer) outPutMap.get("ACCRIGHTSINDICATOR");
                if (accRightIndicator == 1 || accRightIndicator == 9)
                    errorCode = "40007319";
                else if (accRightIndicator == -1)
                    errorCode = "40007318";
                else if (accRightIndicator == 2)
                    errorCode = "40007321";
                else if (accRightIndicator == 3)
                    errorCode = "40112172";
                else if (accRightIndicator == 6)
                    errorCode = "40409356";
                else if (accRightIndicator == 7)
                    errorCode = "40007325";
                else {
                    return true;
                }
                formattedMessage = BankFusionMessages.getInstance().getFormattedEventMessage(Integer.parseInt(errorCode), params,
                        BankFusionThreadLocal.getUserSession().getUserLocale(), true);
                setErrorResponse(errorCode, formattedMessage);
                return false;
            }
            else {
                return true;
            }
        }
    }

    private BigDecimal computeExchangeRate(String fromCurrency, String toCurrency, String rateType, BigDecimal amount,
            BankFusionEnvironment env) {
        BigDecimal exchangeRate = null;
        try {
            IBusinessInformation bizInformation = ((IBusinessInformationService) ServiceManagerFactory.getInstance()
                    .getServiceManager().getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();
            exchangeRate = bizInformation.getExchangeRateDetail(fromCurrency, toCurrency, rateType, amount, env);

        }
        catch (BankFusionException bankFusionException) {
            logger.info(bankFusionException);
        }

        return exchangeRate;
    }

    @SuppressWarnings("unchecked")
    private ReadProductSummaryDtlsRs readProductSummaryDetails(String productId) {
        HashMap<String, Object> MfParams = new HashMap<String, Object>();
        ReadProductSummaryDtlsRq readProductSummaryDtlsRq = new ReadProductSummaryDtlsRq();
        ReadProductSummaryDtlsReq readProductSummaryDtlsReq = new ReadProductSummaryDtlsReq();
        readProductSummaryDtlsReq.setProductID(productId);
        readProductSummaryDtlsRq.setReadProductSummaryDtlsReq(readProductSummaryDtlsReq);
        MfParams.put("readProductSummaryDtlsRq", readProductSummaryDtlsRq);
        HashMap outputParams = MFExecuter.executeMF("CB_PRD_ReadProductSummaryDetails_SRV",
                BankFusionThreadLocal.getBankFusionEnvironment(), MfParams);
        ReadProductSummaryDtlsRs response = (ReadProductSummaryDtlsRs) outputParams.get("readProductSummaryDtlsRs");
        return response;
    }

    private String getModuleConfigValue(String val, String moduleID) {
        String value = "";
        HashMap<String, ReadModuleConfigurationRq> moduleParams = new HashMap<String, ReadModuleConfigurationRq>();
        ModuleKeyRq module = new ModuleKeyRq();
        ReadModuleConfigurationRq read = new ReadModuleConfigurationRq();
        module.setModuleId(moduleID);
        module.setKey(val);
        read.setModuleKeyRq(module);
        moduleParams.put("ReadModuleConfigurationRq", read);
        HashMap valueFromModuleConfiguration = MFExecuter.executeMF(READ_MODULE_CONFIGURATION,
                BankFusionThreadLocal.getBankFusionEnvironment(), moduleParams);
        if (valueFromModuleConfiguration != null) {
            ReadModuleConfigurationRs rs = (ReadModuleConfigurationRs) valueFromModuleConfiguration
                    .get("ReadModuleConfigurationRs");
            value = rs.getModuleConfigDetails().getValue().toString();
        }
        return value;
    }

    private boolean validateChequeBookType(IBOAccount mainAccount, BankFusionEnvironment env) {

        HashMap<String, Object> inputparam = new HashMap<String, Object>();
        HashMap<String, Object> outputparam = new HashMap<String, Object>();
        inputparam.put("ACCOUNTID", mainAccount.getBoID());

        String formattedMessage;

        outputparam = MFExecuter.executeMF("UB_IBI_ValChqBookTypeToAcc_SRV", env, inputparam);

        Integer errorCode = (Integer) outputparam.get("EventCode");
        if (errorCode != 0) {
            formattedMessage = BankFusionMessages.getInstance().getFormattedEventMessage(errorCode, null,
                    BankFusionThreadLocal.getUserSession().getUserLocale(), true);
            setErrorResponse(errorCode.toString(), formattedMessage);
            return false;
        }
        return true;
    }

}
