package com.misys.ub.dc.chequeBook;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.BankFusionMessages;
import com.misys.bankfusion.common.GUIDGen;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.common.runtime.eventcode.BusinessEventSupport;
import com.misys.bankfusion.serviceinvocation.UserExitHelper;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.bankfusion.subsystem.persistence.IPersistenceObjectsFactory;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.fbe.common.util.CommonUtil;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOChequeBookType;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.FinderMethods;
import com.trapedza.bankfusion.fatoms.GenrateCheckNumber;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_IBI_ChequeBookCreate;
import com.trapedza.bankfusion.steps.refimpl.IUB_IBI_ChequeBookCreate;

import bf.com.misys.cbs.msgs.v1r0.ReadProductSummaryDtlsRs;
import bf.com.misys.cbs.services.CalcEventChargeRq;
import bf.com.misys.cbs.services.ReadModuleConfigurationRq;
import bf.com.misys.cbs.services.ReadModuleConfigurationRs;
import bf.com.misys.cbs.types.AccountKeys;
import bf.com.misys.cbs.types.EventChgInputDtls;
import bf.com.misys.cbs.types.InputAccount;
import bf.com.misys.cbs.types.ModuleKeyRq;
import bf.com.misys.cbs.types.dc.ChqBookCreateRes;
import bf.com.misys.cbs.types.dc.ChqBookCreateRs;
import bf.com.misys.cbs.types.dc.ChqBookRq;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;
import bf.com.misys.ub.userexit.types.ChequeBookStartNumber;

@SuppressWarnings("unchecked")
public class IssueChequeBook extends AbstractUB_IBI_ChequeBookCreate implements IUB_IBI_ChequeBookCreate {

    /**
     *
     */
    private static final long serialVersionUID = 4643936617577682528L;
    private IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
    final String MODULEID = "SYS";
    final String KEY = "CHEQUEBOOK_ISSUE_STATUS";
    final String READ_MODULE_CONFIGURATION = "CB_CMN_ReadModuleConfiguration_SRV";
    private transient final static Log logger = LogFactory.getLog(IssueChequeBook.class.getName());

    private static final String BRANCHADDRESS = "BRANCHADDRESS";
    private static final String CUSTOMERADDRESS = "CUSTOMERADDRESS";
    private static final String NUMBER_OF_DIGIT_IN_CHECK_DIGIT = "NumberOfDigitsInCheckDigit";
    private static final String CBS_MODULE = "CBS";
    private static final String CHECK_NUMBER_LENGTH = "ChequeNumberLength";
    private static final int E_CHEQUE_NUMBER_LENGTH_EXCEEDS_UB = 40112422;
    private static String FROM_CHQ_NO = " ";
    private static String TO_CHQ_NO = " ";
    private String channelId = "";
    private String ADDRESS = "";
    private String ADDRESSID = "";
    private transient final static Log LOG = LogFactory.getLog(GenrateCheckNumber.class.getName());
    private static final String ACCOUNT_WISE_CHEQUE_SEQUENCE_NUMBER = "AccountWiseChequeSequenceNumber";
    private static final String BEAN_ID = "ub_ChequeBookStartNumberId";

    public IssueChequeBook(BankFusionEnvironment env) {
        super(env);
    }

    public void process(BankFusionEnvironment env) {

        ChqBookRq chqBookRq = getF_IN_ChqBookRq();
        channelId = chqBookRq.getRqHeader().getOrig().getChannelId();
        String chequeBookID = getF_IN_ChequeBookID();
        String idPk = GUIDGen.getNewGUID();
        headerCreate(idPk);
        if (issueChequeBook(chqBookRq, chequeBookID, idPk)) {
            headerUpdate(idPk, "P");
            ChqBookCreateRs chqBookCreateRs = new ChqBookCreateRs();
            ChqBookCreateRes chqBookCreateRes = new ChqBookCreateRes();
            RsHeader rsHeader = new RsHeader();
            MessageStatus messageStatus = new MessageStatus();
            SubCode subCode = new SubCode();
            subCode.setDescription(BankFusionMessages.getFormattedMessage(40009327, new String[] {FROM_CHQ_NO,TO_CHQ_NO}));
            messageStatus.addCodes(subCode);
            messageStatus.setOverallStatus("S");
            rsHeader.setStatus(messageStatus);
            rsHeader.setOrigCtxtId(getF_IN_ChqBookRq().getRqHeader().getOrig().getChannelId());
            chqBookCreateRes.setOrderStatus("ordered");
            chqBookCreateRs.setRsHeader(rsHeader);
            chqBookCreateRs.setChqBookCreateRes(chqBookCreateRes);
            setF_OUT_ChequeBookCreateRs(chqBookCreateRs);
        }
        else {
            headerUpdate(idPk, "F");
        }

        IBusinessInformation bizInfo = ((IBusinessInformationService) ServiceManagerFactory.getInstance().getServiceManager()
                .getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();
        Boolean isTaxPerLeafEnabed = (Boolean) bizInfo.getModuleConfigurationValue("TAX", "IS_CHQ_TAX_PER_LEAF", env);
        String taxCurrency = (String) bizInfo.getModuleConfigurationValue("TAX", "CHQ_TAX_CURRENCY", env);

        IBOAttributeCollectionFeature mainAccount = FinderMethods.getAccountBO(getF_IN_ChqBookRq().getChqBookReq().getAccountID());

        ArrayList param = new ArrayList();

        param.add(mainAccount.getF_PRODUCTID());

        String reqChequeBookType = getF_IN_ChqBookRq().getChqBookReq().getChequeBookType();

        if (CommonUtil.checkIfNullOrEmpty(reqChequeBookType)) {
            reqChequeBookType = "0";
        }

        param.add(reqChequeBookType);

        param.add(getF_IN_ChqBookRq().getChqBookReq().getNumberOfLeaves());

        IBOChequeBookType chequeBookType = (IBOChequeBookType) factory.findFirstByQuery(IBOChequeBookType.BONAME, "where "
                + IBOChequeBookType.CHEQUETYPEID + "= ? and "
                + IBOChequeBookType.CHEQUETYPECODE + "= ? " +
                "and " + IBOChequeBookType.NUMBEROFLEAVES + "= ?", param);

        if(isTaxPerLeafEnabed) {
            setF_OUT_taxCurrency(taxCurrency);
            if (null != chequeBookType) {
                setF_OUT_taxPerLeaf(chequeBookType.getF_TAXPERLEAF());
            }
        }
        postEventCharges(chqBookRq, getF_IN_ProductResponse());
    }

    private void headerUpdate(String idPk, String result) {
        Connection connection = factory.getJDBCConnection();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection
                    .prepareStatement("UPDATE INFTB_MESSAGEHEADER SET INMESSAGESTATUS = ? WHERE INMESSAGEID1PK = ? ");
            preparedStatement.setString(1, result);
            preparedStatement.setString(2, idPk);
            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            logger.error(ExceptionUtil.getExceptionAsString(e));
        }
        finally {
            try {
                if (preparedStatement != null)
                    preparedStatement.close();
            }
            catch (SQLException e) {
                logger.error(ExceptionUtil.getExceptionAsString(e));
            }
        }
    }

    private boolean postEventCharges(ChqBookRq chqBookRq, ReadProductSummaryDtlsRs productResponse) {
        CalcEventChargeRq calcEventChargeRq = new CalcEventChargeRq();
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
        eventChgInputDtls.setChequeNoOfLeaves(chqBookRq.getChqBookReq().getNumberOfLeaves());
        eventChgInputDtls.setNoOfChequeBooks(chqBookRq.getChqBookReq().getNumberofChqBooks());
        calcEventChargeRq.setEventChgInputDtls(eventChgInputDtls);
        HashMap<String, Object> MfParams = new HashMap<String, Object>();
        MfParams.put("CalcEventChargeRq", calcEventChargeRq);

        // HashMap outputChargeParams =
        // MFExecuter.executeMF("CB_CHG_ApplyEventCharges_SRV",BankFusionThreadLocal.getBankFusionEnvironment(),
        // MfParams);
        MFExecuter.executeMF("CB_CHG_ApplyEventCharges_SRV", BankFusionThreadLocal.getBankFusionEnvironment(), MfParams);

        return true;
    }

    private String getModuleConfigValue() {
        String value = "";
        HashMap<String, ReadModuleConfigurationRq> moduleParams = new HashMap<String, ReadModuleConfigurationRq>();
        ModuleKeyRq module = new ModuleKeyRq();
        ReadModuleConfigurationRq read = new ReadModuleConfigurationRq();
        module.setKey(KEY);
        module.setModuleId(MODULEID);
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

    private void headerCreate(String messageId) {
        Connection connection = factory.getJDBCConnection();
        PreparedStatement preparedStatement = null;
        final String CREATE_MESSAGE_HEADER = "INSERT INTO INFTB_MESSAGEHEADER (INMESSAGEID1PK, INMESSAGEID2, INMESSAGETYPE, INCHANNELID, INMESSAGESTATUS, INERRORCODE, INMSGRECEIVEDTTM, INMSGLASTUPDTDTTM, VERSIONNUM, INRUNTIMEMICROFLOWID, INDIRECTION) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        Date resultDate = SystemInformationManager.getInstance().getBFSystemDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            preparedStatement = connection.prepareStatement(CREATE_MESSAGE_HEADER);
            preparedStatement.setString(1, messageId);
            preparedStatement.setString(2, "");
            preparedStatement.setString(3, "CHQISS");
            preparedStatement.setString(4, channelId);
            preparedStatement.setString(5, "I");
            preparedStatement.setInt(6, 0);
            preparedStatement.setTimestamp(7, Timestamp.valueOf(sdf.format(resultDate)));
            preparedStatement.setTimestamp(8, Timestamp.valueOf(sdf.format(resultDate)));
            preparedStatement.setInt(9, 1);
            preparedStatement.setString(10, "");
            preparedStatement.setString(11, "I");
            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            logger.error(ExceptionUtil.getExceptionAsString(e));
        }
        finally {
            try {
                if (preparedStatement != null)
                    preparedStatement.close();
            }
            catch (SQLException e) {
                logger.error(ExceptionUtil.getExceptionAsString(e));
            }
        }

    }

    @SuppressWarnings("static-access")
    private boolean issueChequeBook(ChqBookRq chqBookRq, String chequeBookID, String idPk) {
        headerUpdate(idPk, "R");
        PreparedStatement preparedStatement0 = null;
        ResultSet rs = null;
        Date expiryDate = null;
        if (channelId.equalsIgnoreCase("IBI") || channelId.equalsIgnoreCase("CCI") || channelId.equalsIgnoreCase("MOB")) {
            ArrayList param = new ArrayList();
            param.add(chqBookRq.getChqBookReq().getAccountID());
            final String FIND_CHEQUEBOOK_DETAIL = "SELECT TOCHEQUENUMBER FROM ChequeBookDetails WHERE ACCOUNTID = ?  ";
            Connection connection = factory.getJDBCConnection();

            Long oldToChequeBook = new Long(0);
            try {
                preparedStatement0 = connection.prepareStatement(FIND_CHEQUEBOOK_DETAIL);
                preparedStatement0.setString(1, chqBookRq.getChqBookReq().getAccountID());
                rs = preparedStatement0.executeQuery();

                while (rs.next()) {
                    long maxChqBookNo = rs.getLong(1);
                    if (oldToChequeBook < maxChqBookNo) {
                        oldToChequeBook = maxChqBookNo;
                    }
                }
            }
            catch (SQLException e1) {

                logger.error(ExceptionUtil.getExceptionAsString(e1));
            }
            finally {
                if (preparedStatement0 != null) {
                    try {
                        preparedStatement0.close();
                    }
                    catch (SQLException sqlException) {
                        logger.error(ExceptionUtil.getExceptionAsString(sqlException));
                    }
                }
                if (rs != null) {
                    try {
                        rs.close();
                    }
                    catch (SQLException sqlException) {
                        logger.error(ExceptionUtil.getExceptionAsString(sqlException));
                    }
                }
            }
            int numberOfLeaves = chqBookRq.getChqBookReq().getNumberOfLeaves();
            long fromChequeBook = oldToChequeBook + 1;
            int chequeBookType = 0;
            String chequeBookTypeStr = chqBookRq.getChqBookReq().getChequeBookType();
            if (CommonUtil.checkIfNotNullOrEmpty(chequeBookTypeStr)) {
                chequeBookType = Integer.parseInt(chequeBookTypeStr);
            }
            int numberOfDigitsInCheckDigit = Integer
                    .parseInt(readPropertyFromModuleConfiguration(NUMBER_OF_DIGIT_IN_CHECK_DIGIT, CBS_MODULE));
            int chequeNumberLength = Integer.parseInt(readPropertyFromModuleConfiguration(CHECK_NUMBER_LENGTH, CBS_MODULE));
            fromChequeBook = getFromChequeNumber(fromChequeBook);
            Long toChequeBook = fromChequeBook + numberOfLeaves - 1;


            PreparedStatement preparedStatement4 = null;
            PreparedStatement preparedStatement3 = null;
            PreparedStatement preparedStatement = null;
            ResultSet rs4 = null;
            // PreparedStatement preparedStatement2 = null;
            boolean inPreparedStatement = false;
            String chqDetailsId = GUIDGen.getNewGUID();
            if (String.valueOf(toChequeBook).length() <= (chequeNumberLength)) {
                final String CREATE_CHEQUEBOOK_REQUEST = "INSERT INTO CHEQUEDETAILSFEATURE ( CHEQUEDETAILSFEATUREID,  BANKCHEQUE,  CHEQUEBOOKID,  ACCOUNTID, VERSIONNUM,  UBISARCHIVE,  UBISDELETE, UBCHANNELID ) VALUES (?,?,?,?,?,?,?,?)";

                try {
                    preparedStatement4 = connection.prepareStatement(
                            "SELECT CHEQUEDETAILSFEATUREID FROM CHEQUEDETAILSFEATURE WHERE ( ACCOUNTID = ? AND CHEQUEBOOKID = ? )");
                    preparedStatement4.setString(1, chqBookRq.getChqBookReq().getAccountID());
                    preparedStatement4.setString(2, chequeBookID);
                    rs4 = preparedStatement4.executeQuery();
                    String id = "";
                    if (rs4.next()) {
                        id = rs4.getString(1);
                    }
                    if (id.equals("")) {
                        inPreparedStatement = true;
                        preparedStatement = connection.prepareStatement(CREATE_CHEQUEBOOK_REQUEST);
                        preparedStatement.setString(1, idPk);
                        preparedStatement.setString(2, "N");
                        preparedStatement.setString(3, chequeBookID);
                        preparedStatement.setString(4, chqBookRq.getChqBookReq().getAccountID());
                        preparedStatement.setInt(5, 1);
                        preparedStatement.setString(6, "N");
                        preparedStatement.setString(7, "N");
                        preparedStatement.setString(8, channelId);
                        preparedStatement.executeUpdate();
                    }
                    else {
                        idPk = id;
                    }
                }
                catch (SQLException e) {
                    logger.error(ExceptionUtil.getExceptionAsString(e));
                }
                if (!chqBookRq.getChqBookReq().getCollectAtBranch()) {
                    ADDRESS = CUSTOMERADDRESS;
                    ADDRESSID = chqBookRq.getChqBookReq().getBranchCode();
                }
                else {
                    ADDRESS = BRANCHADDRESS;
                    ADDRESSID = chqBookRq.getChqBookReq().getBranchCode();
                }

                for (int i = 0; i < chqBookRq.getChqBookReq().getNumberofChqBooks(); i++) {
                    Map firstAndLastChequeNumberMap = GenrateCheckNumber.persistCheckBookDetail(
                            chqBookRq.getChqBookReq().getAccountID(), BankFusionThreadLocal.getUserId(), ADDRESSID, idPk,
                            SystemInformationManager.getInstance().getBFBusinessDateTime(),
                            SystemInformationManager.getInstance().getBFBusinessDateTime(), ADDRESS, fromChequeBook,
                            getModuleConfigValue(), BankFusionThreadLocal.getUserId(), numberOfLeaves, chequeBookType, null,
                            expiryDate, channelId);

                    fromChequeBook = fromChequeBook + numberOfLeaves;
                    if(i == 0) {
                    	FROM_CHQ_NO = String.valueOf(firstAndLastChequeNumberMap.get("FROM_CHECK_NUMBER"));
                    }
                    if(i == chqBookRq.getChqBookReq().getNumberofChqBooks()-1) {
                    	TO_CHQ_NO = String.valueOf(firstAndLastChequeNumberMap.get("TO_CHECK_NUMBER"));
                    }
                }

                try {
                    preparedStatement3 = connection.prepareStatement(
                            "INSERT INTO UBTB_CHEQUEBOOKDETAILS_UD (CHEQUEBOOKDETAILSID,VERSIONNUM) VALUES (?,?)");
                    preparedStatement3.setString(1, chqDetailsId);
                    preparedStatement3.setInt(2, 0);
                    preparedStatement3.executeUpdate();
                }
                catch (SQLException e) {
                    logger.error(ExceptionUtil.getExceptionAsString(e));
                }
                finally {
                    if (rs4 != null) {
                        try {
                            rs4.close();
                        }
                        catch (SQLException sqlException) {
                            logger.error(ExceptionUtil.getExceptionAsString(sqlException));
                        }
                    }
                    if (preparedStatement3 != null) {
                        try {
                            preparedStatement3.close();
                        }
                        catch (SQLException sqlException) {
                            logger.error(ExceptionUtil.getExceptionAsString(sqlException));
                        }
                    }
                    if (preparedStatement != null) {
                        if (inPreparedStatement)
                            try {
                                preparedStatement.close();
                            }
                            catch (SQLException sqlException) {
                                logger.error(ExceptionUtil.getExceptionAsString(sqlException));
                            }
                    }
                    if (preparedStatement4 != null)
                        try {
                            preparedStatement4.close();
                        }
                        catch (SQLException sqlException) {
                            logger.error(ExceptionUtil.getExceptionAsString(sqlException));
                        }
                }
            }
            else {
                /*
                 * String formattedMsg = BankFusionMessages.getInstance().getFormattedEventMessage
                 * (Integer .parseInt("40180453"), null,
                 * BankFusionThreadLocal.getUserSession().getUserLocale(), true); ; String errorCode
                 * = "40180453"; privateSetErrorResponse(formattedMsg, errorCode);
                 */

                LOG.error("Cheque Number Length Is More Than " + (chequeNumberLength - numberOfDigitsInCheckDigit));
                BusinessEventSupport.getInstance().raiseBusinessErrorEvent(E_CHEQUE_NUMBER_LENGTH_EXCEEDS_UB,
                        new Object[] { chequeNumberLength }, LOG, BankFusionThreadLocal.getBankFusionEnvironment());
                return false;
            }
            return true;
        }

        else {
            String formattedMsg = BankFusionMessages.getInstance().getFormattedEventMessage(Integer.parseInt("40411012"), null,
                    BankFusionThreadLocal.getUserSession().getUserLocale(), true);
            String errorCode = "40411012";
            privateSetErrorResponse(formattedMsg, errorCode);
            return false;
        }
    }

    private void privateSetErrorResponse(String formattedMsg, String errorCode) {
        ChqBookCreateRs chqBookCreateRs = new ChqBookCreateRs();
        ChqBookCreateRes chqBookCreateRes = new ChqBookCreateRes();
        RsHeader rsHeader = new RsHeader();
        MessageStatus messageStatus = new MessageStatus();
        SubCode subCode = new SubCode();
        subCode.setDescription(formattedMsg);
        subCode.setCode(errorCode);
        messageStatus.addCodes(subCode);
        messageStatus.setOverallStatus("F");
        rsHeader.setStatus(messageStatus);
        rsHeader.setOrigCtxtId(BankFusionThreadLocal.getUserSession().getChannelID());// what is
                                                                                      // this
        chqBookCreateRs.setRsHeader(rsHeader);
        chqBookCreateRes.setOrderStatus("order cancelled");
        chqBookCreateRs.setChqBookCreateRes(chqBookCreateRes);
        setF_OUT_ChequeBookCreateRs(chqBookCreateRs);

    }

    private static String readPropertyFromModuleConfiguration(String key, String module) {
        String value = "";
        IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory.getInstance()
                .getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
        Object moduleParamValue = ubInformationService.getBizInfo().getModuleConfigurationValue(module, key,
                BankFusionThreadLocal.getBankFusionEnvironment());
        if (moduleParamValue != null) {
            value = moduleParamValue.toString();
        }
        return value;
    }

    private long getFromChequeNumber(long fromChequeBook) {
        String chequeSequenceNumber = readPropertyFromModuleConfiguration(ACCOUNT_WISE_CHEQUE_SEQUENCE_NUMBER, CBS_MODULE);
        if (CommonConstants.YES.equalsIgnoreCase(chequeSequenceNumber)) {
            ChequeBookStartNumber chequeBookStartNumber = new ChequeBookStartNumber();
            chequeBookStartNumber.setAccountId(getF_IN_ChqBookRq().getChqBookReq().getAccountID());
            chequeBookStartNumber.setIsReadOnly(false);
            chequeBookStartNumber.setNextChqNum(String.valueOf(fromChequeBook));
            chequeBookStartNumber.setBranchSortCode(BankFusionThreadLocal.getUserSession().getBranchSortCode());
            chequeBookStartNumber.setUserId(BankFusionThreadLocal.getUserId());
            chequeBookStartNumber.setBusinessDateTime(SystemInformationManager.getInstance().getBFBusinessDateTime());
            chequeBookStartNumber = (ChequeBookStartNumber) UserExitHelper.executeUserExit(chequeBookStartNumber, BEAN_ID);
            if (chequeBookStartNumber != null && Long.valueOf(chequeBookStartNumber.getNextChqNum()) != 0L) {
                fromChequeBook = Long.valueOf(chequeBookStartNumber.getNextChqNum());
            }
        }
        return fromChequeBook;
    }
}
