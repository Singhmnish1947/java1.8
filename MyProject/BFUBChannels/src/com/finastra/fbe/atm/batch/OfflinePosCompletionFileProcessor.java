package com.finastra.fbe.atm.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.finastra.iso8583.atm.processes.ATMSettlementPseudonymResolver;
import com.misys.bankfusion.common.BankFusionMessages;
import com.misys.bankfusion.common.exception.BankFusionException;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.ub.atm.batch.model.Clearing;
import com.misys.ub.atm.batch.model.Operation;
import com.trapedza.bankfusion.bo.refimpl.IBOPosClearFileDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOPosOperationDetails;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.persistence.services.IPersistenceService;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.utils.GUIDGen;

public class OfflinePosCompletionFileProcessor {

    private final IPersistenceObjectsFactory persistanceFactory = BankFusionThreadLocal.getPersistanceFactory();

    private static final Log LOGGER = LogFactory.getLog(OfflinePosCompletionFileProcessor.class.getName());

    private final IPersistenceService pService = (IPersistenceService) ServiceManagerFactory.getInstance().getServiceManager()
            .getServiceForName(ServiceManager.PERSISTENCE_SERVICE);

    public void process(Exchange exchange) throws Exception {
        String fileBoid = GUIDGen.getNewGUID();
        File filePath = null;
        try {
            filePath = exchange.getIn().getBody(File.class);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Processing of file:::" + filePath.getName() + "::started");
            }
            validateFile(filePath.getName());
            if (!insertFileRecord(fileBoid, filePath.getName())) {
                return;
            }
            if (!OfflinePosCompletionFatom.checkForRetry(filePath.getName())) {
                JAXBContext jaxbContext = JAXBContext.newInstance(Clearing.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                InputStream inputStream = new FileInputStream(filePath);
                Clearing clearing = (Clearing) jaxbUnmarshaller.unmarshal(inputStream);
                insertProcessRecord(clearing, fileBoid, filePath);
            }
            HashMap inParam = new HashMap();
            inParam.put("FileId", fileBoid);
            inParam.put("FileName", filePath.getName());
            HashMap outParams = MFExecuter.executeMF("UB_ATM_PosBatchProcessForClearing",
                    BankFusionThreadLocal.getBankFusionEnvironment(), inParam);
            updateFileRecord(fileBoid, "SUCCESSFUL", 0, null);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Processing of file:::" + filePath.getName() + "::ended");
            }
        }
        catch (JAXBException e) {
            // TODO Auto-generated catch block
            LOGGER.error("Parsing failed for file:::" + filePath);
            updateFileRecord(fileBoid, "FAILED", PosOperationEventCodes.TECHNICAL_EXCEPTION,
                    BankFusionMessages.getFormattedMessage(PosOperationEventCodes.TECHNICAL_EXCEPTION, new String[] {}));
            throw e;
        }
        catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            LOGGER.error("File not found:::::: " + filePath);
            updateFileRecord(fileBoid, "FAILED", PosOperationEventCodes.TECHNICAL_EXCEPTION,
                    BankFusionMessages.getFormattedMessage(PosOperationEventCodes.TECHNICAL_EXCEPTION, new String[] {}));
            throw e;
        }
        catch (Exception e) {
            LOGGER.error("Exception occurred processing clearing file:::" + filePath.getName() + "::::::::"
                    + ExceptionUtil.getExceptionAsString(e));
            updateFileRecord(fileBoid, "FAILED", PosOperationEventCodes.TECHNICAL_EXCEPTION,
                    BankFusionMessages.getFormattedMessage(PosOperationEventCodes.TECHNICAL_EXCEPTION, new String[] {}));
            throw e;
        }
    }

    private void validateFile(String name) throws Exception {
        // TODO Auto-generated method stub
        IPersistenceObjectsFactory privateFactory = null;
        try {

            privateFactory = pService.getPrivatePersistenceFactory(false);
            privateFactory.beginTransaction();
            ArrayList param = new ArrayList();
            param.add(name);
            List<IBOPosClearFileDetails> clearFile = (List<IBOPosClearFileDetails>) privateFactory
                    .findByQuery(IBOPosClearFileDetails.BONAME, "WHERE " + IBOPosClearFileDetails.FILENAME + " = ?", param, null);
            if (clearFile != null && clearFile.size() != 0) {
                LOGGER.error("File is already processed");
                throw new Exception();
            }
            privateFactory.commitTransaction();
        }
        catch (Exception exception) {
            LOGGER.error(ExceptionUtil.getExceptionAsString(exception));
            if (privateFactory != null) {
                privateFactory.rollbackTransaction();
            }
            throw exception;
        }
        finally {
            if (privateFactory != null)
                privateFactory.closePrivateSession();
        }
    }

    private void insertProcessRecord(Clearing clearing, String fileId, File filePath) throws Exception {
        IPersistenceObjectsFactory privateFactory = null;
        String error = "";
        String externalAuthId = "";
        int lastCount = 0;
        int failed = 0;

        StringBuilder errorDescription = new StringBuilder();

        for (Operation op : clearing.getOperations()) {
            try {
                privateFactory = pService.getPrivatePersistenceFactory(false);
                privateFactory.beginTransaction();
                if (op.getStatus().equals("OPST0100") || op.getStatus().equals("OPST0400")) {
                    IBOPosOperationDetails operationDetails = (IBOPosOperationDetails) privateFactory
                            .getStatelessNewInstance(IBOPosOperationDetails.BONAME);
                    // IBOPosOperationDetailsTag operationDetailsTag =
                    // (IBOPosOperationDetailsTag)
                    // privateFactory.getStatelessNewInstance(IBOPosOperationDetailsTag.BONAME);
                    String boId = GUIDGen.getNewGUID();
                    operationDetails.setF_FILENAME(filePath.getName());

                    error = "External Auth ID";
                    externalAuthId = op.getAuth_data().getExternal_auth_id();
                    if ("".equals(externalAuthId)) {
                        throw new BankFusionException();
                    }
                    else {
                        operationDetails.setF_CMSUNIQUEENDTXNREF(externalAuthId);
                    }
                    error = "BOID";
                    operationDetails.setBoID(boId);

                    error = "Operation Type";

                    if ("".equals(op.getOper_type())) {
                        throw new BankFusionException();
                    }
                    else if ("".equals(op.getAcquirer().getInst_id())) {
                        error = "Institution Id";
                        throw new BankFusionException();
                    }
                    else {
                        ATMSettlementPseudonymResolver atmSettlementPseudonymResolver = new ATMSettlementPseudonymResolver();
                        String IMDCode = atmSettlementPseudonymResolver.getImdCode(op.getAcquirer().getInst_id(),
                                op.getOper_type());
                        if ("".equals(IMDCode) || null == IMDCode) {
                            throw new BankFusionException();
                        }
                    }
                    operationDetails.setF_OPERATIONTYPE(op.getOper_type());


                    error = "Settlement Currency";
                    if ("".equals(op.getSttl_amount().getCurrency())) {
                        throw new BankFusionException();
                    }
                    else {
                        operationDetails.setF_RECONCURRENCY(op.getSttl_amount().getCurrency());
                    }

                    error = "Amount Settlement";
                    if ("".equals(op.getSttl_amount().getAmount_value())) {
                        throw new BankFusionException();
                    }
                    else {
                        BigDecimal reconAmount = parseAmount(op.getSttl_amount().getAmount_value(),
                                op.getSttl_amount().getCurrency());
                        operationDetails.setF_AMOUNTRECON(reconAmount);
                    }
                    BigDecimal amountAccount = new BigDecimal(0);
                    if ("OPTP0020".equals(op.getOper_type()) || "OPTP0422".equals(op.getOper_type())) {
                        error = "Account ID";
                        operationDetails.setF_ACCOUNTID(op.getTransaction().getCredit_entry().getAccount().getAccount_number());

                        error = "Amount Account";
                        amountAccount = parseAmount(op.getTransaction().getCredit_entry().getAmount().getAmount_value(),
                                op.getTransaction().getCredit_entry().getAmount().getCurrency());

                        operationDetails.setF_AMOUNTACCOUNT(amountAccount);

                        error = "Account currency";
                        operationDetails.setF_UBACCOUNTCURRENCY(op.getTransaction().getCredit_entry().getAmount().getCurrency());
                    }
                    else {
                        error = "Account ID";

                        String account = op.getTransaction().getDebit_entry().getAccount().getAccount_number();
                        operationDetails.setF_ACCOUNTID(account);

                        error = "Amount Account";
                        amountAccount = parseAmount(op.getTransaction().getDebit_entry().getAmount().getAmount_value(),
                                op.getTransaction().getDebit_entry().getAmount().getCurrency());
                        operationDetails.setF_AMOUNTACCOUNT(amountAccount);

                        error = "Account currency";
                        operationDetails.setF_UBACCOUNTCURRENCY(op.getTransaction().getDebit_entry().getAmount().getCurrency());
                    }



                    error = "Originator Referece Number";
                    operationDetails.setF_ORIGINATORREFNUM(op.getOriginator_refnum());
                    error = "Transaction Type";
                    operationDetails.setF_TRANSACTIONTYPE(op.getTransaction().getTransaction_type());
                    error = "Acquiring Institution ID";
                    operationDetails.setF_ACQUIRINGINSTITUTIONID(op.getAcquirer().getInst_id());
                    error = "Card Number";
                    operationDetails.setF_CARDNUMBER(op.getIssuer().getClient_id_value());
                    error = "Terminal Number";
                    operationDetails.setF_TERIMANALID(op.getTerminal_number());

                    if (validateDate(op.getSttl_date())) {
                        error = "Settlement Date";
                        operationDetails.setF_TRANDATE(Timestamp.valueOf(
                                LocalDateTime.from(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").parse(op.getSttl_date()))));
                    }
                    else {
                        error = "Settlement Date";
                        operationDetails.setF_TRANDATE(SystemInformationManager.getInstance().getBFBusinessDateTime());
                    }
                    error = "File ID";
                    operationDetails.setF_FILEID(fileId);
                    error = "Institution ID";
                    operationDetails.setF_ISSUERID(op.getIssuer().getInst_id());
                    error = "Terminal Type";
                    operationDetails.setF_TERMINALTYPE(op.getTerminal_type());
                    error = "Merchant Name";
                    /* operationDetails.setF_MERCHANTNAME(op.getMerchant_name()); */

                    error = "Auth Code";
                    operationDetails.setF_UBAUTHCODE(op.getIssuer().getAuth_code());
                    operationDetails.setF_UBMERCHANTNAME(op.getMerchant_name());
                    if (validateDate(op.getOper_date())) {
                        error = "Operation Date";
                        operationDetails.setF_UBOPERDATE(Timestamp.valueOf(
                                LocalDateTime.from(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").parse(op.getOper_date()))));
                    }
                    else {
                        error = "Operation Date";
                        operationDetails.setF_UBOPERDATE(SystemInformationManager.getInstance().getBFBusinessDateTime());
                    }

                    if (validateDate(op.getTransaction().getPosting_date())) {
                        error = "Posting Date";
                        operationDetails.setF_UBPOSTINGDTTM(Timestamp.valueOf(
                                LocalDateTime.from(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                                        .parse(op.getTransaction().getPosting_date()))));
                    }
                    else {
                        error = "Posting Date";
                        operationDetails.setF_UBPOSTINGDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
                    }


                    error = "Operation Currency";
                    /*
                     * if ("".equals(op.getOper_amount().getCurrency())) { throw new
                     * BankFusionException();
                     * 
                     * }else {
                     */
                    operationDetails.setF_UBOPERCURRENCY(op.getOper_amount().getCurrency());
                    // }

                    error = "Operation Amount";
                    BigDecimal operationAmount = new BigDecimal(0);
                    if ("".equals(op.getOper_amount().getAmount_value())) {

                        operationDetails.setF_UBOPERAMOUNT(operationAmount);
                    }
                    else {
                        operationAmount = parseAmount(op.getOper_amount().getAmount_value(), op.getOper_amount().getCurrency());
                        operationDetails.setF_UBOPERAMOUNT(operationAmount);
                    }

                    // operationDetailsTag.setF_ACCOUNTID(op.getDebit_entry().getAccount().getAccount_number());
                    // operationDetailsTag.setF_FILEID(fileId);
                    // operationDetailsTag.setBoID(boId);

                    privateFactory.create(IBOPosOperationDetails.BONAME, operationDetails);
                    lastCount++;

                    // privateFactory.create(IBOPosOperationDetailsTag.BONAME, operationDetailsTag);
                }
            }
            catch (Exception exception) {
                LOGGER.error(ExceptionUtil.getExceptionAsString(exception));
                if (privateFactory != null) {
                    privateFactory.rollbackTransaction();
                    privateFactory.beginTransaction();
                    failed = 1;
                    errorDescription.append("Invalid " + error + " for externalAuthId : " + externalAuthId + ". ");
                }
            }
            finally {
                if (privateFactory != null) {
                    privateFactory.commitTransaction();
                    privateFactory.closePrivateSession();
                }
            }
        }
        if (failed == 1 && lastCount >= 1) {
            String fileBoid = GUIDGen.getNewGUID();
            insertFileRecord(fileBoid, filePath.getName());
            if (errorDescription.length() < 1024) {
                updateFileRecord(fileBoid, "FAILED", 11500227, errorDescription.toString());
            }
            else {
                updateFileRecord(fileBoid, "FAILED", 11500227, errorDescription.substring(0, 1023));
            }
        }
        else if (failed == 0 && lastCount >= 1) {

        }
        else {
            throw new Exception();
        }
    }

    private boolean insertFileRecord(String boid, String fileName) {
        IPersistenceObjectsFactory privateFactory = null;
        try {
            privateFactory = pService.getPrivatePersistenceFactory(false);
            privateFactory.beginTransaction();

            IBOPosClearFileDetails file = (IBOPosClearFileDetails) privateFactory
                    .getStatelessNewInstance(IBOPosClearFileDetails.BONAME);
            file.setBoID(boid);
            file.setF_FILENAME(fileName);
            file.setF_FILEPROCESSSTARTTIME(Timestamp.valueOf(LocalDateTime.now()));
            file.setF_RETRIED(0);
            file.setF_FILESTATUS("STARTED");
            privateFactory.create(IBOPosClearFileDetails.BONAME, file);
            privateFactory.commitTransaction();
            return true;
        }
        catch (Exception exception) {
            LOGGER.error(ExceptionUtil.getExceptionAsString(exception));
            if (privateFactory != null) {
                privateFactory.rollbackTransaction();
            }

        }
        finally {
            if (privateFactory != null)
                privateFactory.closePrivateSession();
        }
        return false;
    }

    private boolean updateFileRecord(String boid, String status, int errorCode, String errorDesc) {
        IPersistenceObjectsFactory privateFactory = null;
        try {
            privateFactory = pService.getPrivatePersistenceFactory(false);
            privateFactory.beginTransaction();

            IBOPosClearFileDetails file = (IBOPosClearFileDetails) privateFactory.findByPrimaryKey(IBOPosClearFileDetails.BONAME,
                    boid);
            file.setF_FILEPROCESSENDTIME(Timestamp.valueOf(LocalDateTime.now()));
            file.setF_FILESTATUS(status);
            if (status.equals("FAILED")) {
                file.setF_ERRORCODE(Integer.toString(errorCode));
                file.setF_ERRORDESC(errorDesc);
            }
            privateFactory.commitTransaction();
            return true;
        }
        catch (Exception exception) {
            LOGGER.error(ExceptionUtil.getExceptionAsString(exception));
            if (privateFactory != null) {
                privateFactory.rollbackTransaction();
            }

        }
        finally {
            if (privateFactory != null)
                privateFactory.closePrivateSession();
        }
        return false;
    }

    private boolean validateDate(String sttl_date) {
        // TODO Auto-generated method stub
        try {
            if (StringUtils.isBlank(sttl_date)) {
                return false;
            }
            Timestamp.valueOf(LocalDateTime.from(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").parse(sttl_date)));
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    public BigDecimal parseAmount(String amount, String currency) {
        if ("".equals(amount)) {
            amount = "0.00";
        }
        int scale = SystemInformationManager.getInstance().getCurrencyScale(currency);
        double amountScale = Math.pow(10, scale);
        double parsingAmount = Double.parseDouble(amount);
        BigDecimal parsedAmount = BigDecimal.valueOf(parsingAmount / amountScale);
        return parsedAmount;
    }

}