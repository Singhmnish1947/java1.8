package com.misys.ub.fatoms;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.InputSource;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.common.runtime.service.ServiceManagerFactory;
import com.misys.ub.batchgateway.persistence.BatchGatewayCreateMethods;
import com.misys.ub.common.GetUBConfigLocation;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOBatchError;
import com.trapedza.bankfusion.boundary.outward.BankFusionCastorSupport;
import com.trapedza.bankfusion.boundary.outward.BankFusionResourceSupport;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractTellerBatchProcessingServiceFatom;
import com.trapedza.bankfusion.utils.GUIDGen;

import bf.com.misys.ub.types.DraftDetails;
import bf.com.misys.ub.types.Mandatory;
import bf.com.misys.ub.types.Optional;
import bf.com.misys.ub.types.TFSBatchPosting;
import bf.com.misys.ub.types.TransactionDetails;
import bf.com.misys.ub.types.TxnItem;

/**
 * This fatom is used to post the Bulk Draft Issuance and Bulk Draft Deposits. It validates input
 * xml and re-write for the type of DFI or DFP Transactions.
 * 
 * @author Suresh Kumar modified by Haseeb on 29-06-2011
 */

public class TellerBatchFileProcessingService extends AbstractTellerBatchProcessingServiceFatom {

    private static final transient Log logger = LogFactory.getLog(TellerBatchFileProcessingService.class.getName());
    private static final String MODULE_NAME = "BAT";
    private static final String PROCESSEDFILEPATHNAME = "PROCESSED_FILE_PATH";
    private static final String BATCH_MAPPING_XML_PATH = "conf/business/batch/BatchDraftGatewayMapping.xml";
    private static final String ISSUEDRAFT_SRV = "CB_TTB_IssueDraftAccountValidation_SRV";
    private static final String GET_TXN_KEYS_SRV = "BT_TTB_GetTransactionObjectKeys";
    private static final String DEPOSITDRAFT_SRV = "CB_TTB_DepositAccDrftVald_SRV";
    private static final String DFRAFT_STOCKREGISTERUPDATE_SRV = "BT_TTB_StockRegisterUpdate_SRV";
    private static final String CREATE_TXN_OBJECT = "BT_CMN_CreateTxnObject_SRV";
    private static final String INIT_TXN_OBJECT = "BT_TTB_IssueDraftAcctInitialization_SRV";
    private static final String DRAFT_AUTOIDGEN_SRV = "BT_TTB_AllocateDraftNumber_SRV";
    private static final String DRAFT_STOCKREG_REFRESH_SRV = "CB_TTB_StockRegisterRefresh";
    private static final String DEL_UN_ISSUED_DRAFT = "BT_TTB_DeleteUnIssuedDrafts_SRV";
    private static final String DRAFT_PRINT_SRV = "BT_TTB_DraftPrintQueueInput_SRV";
    private static final String DRAFT_HOLDINGLOC_UPDATE_SRV = "BT_TTB_BulkDraftHoldingLocUpdate_SRV";
    private static final String BATCH_GATEWAY_SRV = "BT_TTB_BatchGateway_SRV";
    private static final String GET_HOLDINGLOC_ID_SRV = "BT_VCB_GetHoldingLocationId_SRV";
    private static final String SWIFT_GENERATION_SRV = "UB_CMN_GenerateSWiftMessage_SRV";
    private static final String DFT_ISSUE_TXN_CODE = "DFT_ISSUE_TXN_CODE";
    private static final String DFT_PAYMENT_TXN_CODE = "DFT_PAYMENT_TXN_CODE";
    private String draftIssueTxnCode;
    private String draftPaymentTxnCode;
    private TFSBatchPosting objectModel;
    private BankFusionEnvironment environment;
    private Map<String, String> txnObjectKeyMap;
    private boolean processedFlag = true;
    private static final Date businessDate = SystemInformationManager.getInstance().getBFBusinessDate();
    private List<Map> drafts;

    private int depositedDraftCount;
    private String batchReference;
    // pageErros to hold the error records for BatchError table
    private List<SimplePersistentObject> pageErrors = new ArrayList<SimplePersistentObject>();
    private boolean validationStatus;

    /**
     * The constructor that indicates we're in a runtime environment and we should initialize the
     * Fatom with only those attributes necessary.
     * 
     * @param env
     *            BankFusionEnvironment
     * @return void
     */
    public TellerBatchFileProcessingService(BankFusionEnvironment env) {
        super(env);
    }

    /**
     * This method is to process the TellerBatchFileProcessingService fatom.
     * 
     * @param env
     *            BankFusionEnvironment
     * @return void
     */
    public void process(BankFusionEnvironment env) throws BankFusionException {
        super.process(env);
        this.environment = env;
        String fileName = getF_IN_FileName();
        objectModel = null;

        // read module configuration for payment & issue transaction code
        draftIssueTxnCode = readConfigFromModuleConfiguation(DFT_ISSUE_TXN_CODE);
        draftPaymentTxnCode = readConfigFromModuleConfiguation(DFT_PAYMENT_TXN_CODE);

        // Read input xml file
        objectModel = readXML(fileName);
		if (objectModel == null) {
			setF_OUT_BatchReference(batchReference);
			setF_OUT_BatchStatus(false);
			return;
		}
        // set posting option to 0
        objectModel.getTFSBatchInformation().setPostingOption(new Long(0));

        txnObjectKeyMap = MFExecuter.executeMF(GET_TXN_KEYS_SRV, environment, null);

        if (validateDraftAmt(objectModel)) {
            persistErrors();
            setF_OUT_BatchReference(batchReference);
            setF_OUT_BatchStatus(false);
            return;
        }

        // Prepare transaction object for DFI or DFP and validates the draft (issue/Deposit) data
        prepareBatchTxnStore(objectModel);

        // Check if processedFlag and postBulkProcess() return true then post the Batch
        // updating the stock register and sending into print queue
        // otherwise delete the captured draft details
        if (processedFlag && postBulkProcess(fileName)) {

            // Issue or Deposit draft
            for (Map currentDraft : drafts) {
                String type = (String) currentDraft.get("type");
                if (type.equals("ISSUE")) {
                    String holdLocId = (String) currentDraft.get("holdLocId");
                    String draftPrimaryKey = (String) currentDraft.get("draftPrimaryKey");
                    Map txnNewObject = (Map) currentDraft.get("txnNewObject");
                    TxnItem txnItem = (TxnItem) currentDraft.get("txnItem");
                    issueDraftAndGenerateSwiftMsg(txnItem, holdLocId, txnNewObject);
                    HashMap<String, String> inputs = new HashMap<String, String>();
                    inputs.put("PrintStatus", "Not Printed");
                    inputs.put("StockregID", draftPrimaryKey);
                    MFExecuter.executeMF(DRAFT_PRINT_SRV, environment, inputs);
                }
                else if (type.equals("DEPOSIT")) {
                    String holdLocId = (String) currentDraft.get("holdLocId");
                    TxnItem txnItem = (TxnItem) currentDraft.get("txnItem");
                    depositDraft(txnItem, holdLocId);
                }
            }
            // save all warning in batcherror
            persistErrors();
            if (drafts != null && !drafts.isEmpty()) {
                setF_OUT_IssueDepositDraftListCount(new Integer(drafts.size()));
            }
            else if (depositedDraftCount != 0) {
                setF_OUT_IssueDepositDraftListCount(new Integer(depositedDraftCount));
            }
            // Write modified xml file
            writeXML(fileName, objectModel);
            setF_OUT_BatchStatus(true);
        }
        else {
            for (Map currentDraft : drafts) {
                String type = (String) currentDraft.get("type");
                if (type.equals("ISSUE")) {
                    String draftPrimaryKey = (String) currentDraft.get("draftPrimaryKey");
                    HashMap<String, String> inputs = new HashMap<String, String>();
                    inputs.put("PrimaryKey", draftPrimaryKey);
                    MFExecuter.executeMF(DEL_UN_ISSUED_DRAFT, environment, inputs);
                }
            }
            persistErrors();
            setF_OUT_BatchReference(batchReference);
            setF_OUT_BatchStatus(false);
            return;
        }
    }

    /**
     * This method is used to return Draft Number and Draft Primary Key Map
     * 
     * @param txnItem
     * @param holdLocId
     * @return Map
     */
    private Map<String, String> getDraftGenIdMap(TxnItem txnItem, String holdLocId) {
        Map<String, String> draftIdGenMap = null;
        DraftDetails draftDetails = null;
        Map<String, Object> draftAutoIdGenInputMap = null;
        draftDetails = txnItem.getDraftDetails();
        if (draftDetails != null) {
            draftAutoIdGenInputMap = new HashMap<String, Object>();
            draftAutoIdGenInputMap.put("DraftType", draftDetails.getDraftType());
            draftAutoIdGenInputMap.put("DraftCurrency", draftDetails.getDraftCurrency());
            draftAutoIdGenInputMap.put("Currency", draftDetails.getDraftCurrency());
            draftAutoIdGenInputMap.put("InstrumentName", draftDetails.getDraftInstrument());
            draftAutoIdGenInputMap.put("HoldingLocation", draftDetails.getHoldingLocation());
            draftAutoIdGenInputMap.put("HoldingLocationId", holdLocId);
            draftAutoIdGenInputMap.put("TrasRef", GUIDGen.getNewGUID());
            draftIdGenMap = MFExecuter.executeMF(DRAFT_AUTOIDGEN_SRV, environment, draftAutoIdGenInputMap);
        }
        return draftIdGenMap;
    }

    /**
     * This method is used to insert draft values into StockRegister table
     * 
     * @param txnItem
     *            TxnItem
     * @return void
     */
    private void insertDraftIntoStockRegister(TxnItem txnItem, String swiftIndicator) {
        Map<String, Object> inputDraftMap = new HashMap<String, Object>();
        DraftDetails draftDetails = null;
        String txnCode = CommonConstants.EMPTY_STRING;

        if (txnItem != null) {
            if (txnItem.getMandatory() != null) {
                txnCode = txnItem.getMandatory().getTxnCode();
                inputDraftMap.put("DebitAccount", txnItem.getMandatory().getAccountID());
                inputDraftMap.put("DebitAmount", txnItem.getMandatory().getAmount());
                inputDraftMap.put("Narrative", txnItem.getMandatory().getNarrative());
            }
            if (txnItem.getCurrency() != null) {
                inputDraftMap.put("DebitCurrency", txnItem.getCurrency().getCurrency());
                inputDraftMap.put("Exchange", txnItem.getCurrency().getExchangeRate());
            }

            draftDetails = txnItem.getDraftDetails();
            if (draftDetails != null) {
                if (draftDetails.getContraAccount() != null) {
                    inputDraftMap.put("ContraAccount", draftDetails.getContraAccount());
                }
                if (draftDetails.getDraftAmount() != null) {
                    inputDraftMap.put("DraftAmount", draftDetails.getDraftAmount());
                    inputDraftMap.put("BuyEquivAmt", draftDetails.getDraftAmount());
                }
                if (draftDetails.getDraftCurrency() != null)
                    inputDraftMap.put("DraftCurrency", draftDetails.getDraftCurrency());
                if (draftDetails.getCustomerNo() != null)
                    inputDraftMap.put("Customerno", draftDetails.getCustomerNo());
                if (draftDetails.getDraftNumber() != null)
                    inputDraftMap.put("DraftNumbr", draftDetails.getDraftNumber());
                if (draftDetails.getHoldingLocation() != null)
                    inputDraftMap.put("HoldingLocationId", new Integer(draftDetails.getHoldingLocation()));
                if (draftDetails.getDraftInstrument() != null)
                    inputDraftMap.put("InstrumentName", draftDetails.getDraftInstrument());
                if (draftDetails.getNarrative() != null)
                    inputDraftMap.put("Narrative2", draftDetails.getNarrative());
                if (draftDetails.getPayableAt() != null)
                    inputDraftMap.put("Payableat", draftDetails.getPayableAt());
                if (draftDetails.getBeneficiaryAddress() != null)
                    inputDraftMap.put("Payeeaddress", draftDetails.getBeneficiaryAddress());
                if (draftDetails.getBeneficiaryName() != null)
                    inputDraftMap.put("PayeeName", draftDetails.getBeneficiaryName());

                if (txnCode != null && !txnCode.equals("")) {
                    if (txnCode.equalsIgnoreCase(draftIssueTxnCode)) {
                        if (!txnItem.getDraftDetails().getDraftCurrency().equals(txnItem.getDraftDetails().getDebitCurrency())) {
                            inputDraftMap.put("Status", "ISSUED_TO_NOSTRO");
                        }
                        else {
                            inputDraftMap.put("Status", "ISSUED");
                        }
                    }
                    else if (txnCode.equalsIgnoreCase(draftPaymentTxnCode))
                        inputDraftMap.put("Status", "PAID");
                }
                inputDraftMap.put("Remarks", CommonConstants.EMPTY_STRING);
                inputDraftMap.put("SUPERVISORID", CommonConstants.EMPTY_STRING);
                inputDraftMap.put("ChargeHistoryId", CommonConstants.EMPTY_STRING);
                inputDraftMap.put("SwiftIndicator", swiftIndicator);
                inputDraftMap.put("TransactionReference", GUIDGen.getNewGUID());
                inputDraftMap.put("Mode", "U");
                inputDraftMap.put("TxnMode", "Account");
            }
        }
        inputDraftMap.put("DATETIME", new Timestamp(SystemInformationManager.getInstance().getBFSystemDateTime().getTime()));
        MFExecuter.executeMF(DFRAFT_STOCKREGISTERUPDATE_SRV, environment, inputDraftMap);
    }

    /**
     * This method is to invoke the Batch Gateway to process the Bulk Draft Details.
     * 
     * @param fileName
     *            String
     * @return boolean
     */
    private boolean postBulkProcess(String fileName) {
        Map<String, Object> batchGatewayInputMap = new HashMap<String, Object>();
        Map batchGatewayOutputMap = null;
        batchGatewayInputMap.put("batchFile", fileName);
        // Set PostingOption to 'Reject the Batch' (i.e 0)
        batchGatewayInputMap.put("postingOption", Integer.valueOf(0));
        batchGatewayInputMap.put("validateAndPostFlag", Boolean.TRUE);
        batchGatewayOutputMap = MFExecuter.executeMF(BATCH_GATEWAY_SRV, environment, batchGatewayInputMap);
        return (Boolean) batchGatewayOutputMap.get("Status");
    }

    /**
     * This method is used to return the Holding Location Id by passing Holding Location Name and
     * BaseCode to BT_VCB_GetHoldingLocationId_SRV
     * 
     * @param holdingLoc
     *            String
     * @param branchCode
     *            String
     * @return holdLocId String
     */
    private String getHoldingLocId(String holdingLoc, String branchCode) {
        Integer holdLocId = CommonConstants.INTEGER_ZERO;

        Map<String, Object> batchGatewayInputMap = new HashMap<String, Object>();
        Map batchGatewayOutputMap = null;
        batchGatewayInputMap.put("HoldingLocationName", holdingLoc);
        batchGatewayInputMap.put("Branch", branchCode);
        batchGatewayOutputMap = MFExecuter.executeMF(GET_HOLDINGLOC_ID_SRV, environment, batchGatewayInputMap);
        holdLocId = (Integer) batchGatewayOutputMap.get("HoldingLocationId");
        return holdLocId.toString();
    }

    /**
     * This method is used to update the Holding Location for an InstrumentName, DraftCurrency and
     * HoldingLocationId
     * 
     * @param instrumentName
     *            String
     * @param draftCurrency
     *            String
     * @param holdingLocId
     *            String
     * 
     * @return void
     */
    private void updateHoldingLocForDraftAcc1(String instrumentName, String draftCurrency, String holdingLocId) {
        Map<String, Object> holdLocMap = new HashMap<String, Object>();
        holdLocMap.put("InstrumentName", instrumentName);
        holdLocMap.put("DraftCurrency", draftCurrency);
        holdLocMap.put("HoldingLocationId", new Integer(holdingLocId));
        MFExecuter.executeMF(DRAFT_HOLDINGLOC_UPDATE_SRV, environment, holdLocMap);
    }

    /**
     * This method is to validate and prepare Transaction object for DFI or DFI
     * 
     * @param objectModel
     *            TFSBatchPosting
     * @return void
     */
    @SuppressWarnings("unchecked")
    private void prepareBatchTxnStore(TFSBatchPosting objectModel) {
        if (logger.isInfoEnabled()) {
            logger.info("loading bg txns " + SystemInformationManager.getInstance().getBFBusinessDateTimeMilliAsString());
        }
        String txnCode = CommonConstants.EMPTY_STRING;

        String draftNbr = CommonConstants.EMPTY_STRING;
        String draftPrimaryKey = CommonConstants.EMPTY_STRING;

        String dRcRFlag = CommonConstants.EMPTY_STRING;
        String holdLocId = CommonConstants.EMPTY_STRING;

        drafts = new ArrayList<Map>();
        TransactionDetails transactionDetails = objectModel.getTFSBatchInformation().getTransactionDetails();
        TxnItem[] txnItemArray = objectModel.getTFSBatchInformation().getTransactionDetails().getTxnItem();
        batchReference = objectModel.getTFSBatchInformation().getBatchReference();

        // Loop through all transactions
        for (int i = 0; txnItemArray != null && i < txnItemArray.length; i++) {
            TxnItem txnItem = transactionDetails.getTxnItem()[i];
            Map draft = new HashMap();
            txnCode = txnItem.getMandatory().getTxnCode();

            // prepare the common posting array
            Map commonPostingArrayMap = prepareCommonPostingArray(txnItem);
            Object[] postingMessageObjectArray = new Object[10];
            postingMessageObjectArray[0] = commonPostingArrayMap;
            postingMessageObjectArray[1] = commonPostingArrayMap;
            dRcRFlag = txnItem.getMandatory().getDRCRFlag();
            if (txnItem.getDraftDetails() != null) {
                // prepare the txn object and Check if DFI then validate
                if (txnCode.equalsIgnoreCase(draftIssueTxnCode)
                        && (dRcRFlag != null && !dRcRFlag.equals("") && dRcRFlag.equalsIgnoreCase("C"))) {

                    // Get the Holding Location Id
                    holdLocId = getHoldingLocId(txnItem.getDraftDetails().getHoldingLocation(), txnItem.getDraftDetails()
                            .getIssueBranch());

                    // Get Draft AutoGen Id
                    Map<String, String> draftIdGenMap = getDraftGenIdMap(txnItem, holdLocId);

                    if (draftIdGenMap != null && !draftIdGenMap.equals("")) {
                        // FIXME: need to check the error and see how to log in
                        // batcherror
                        draftNbr = draftIdGenMap.get("DrftNum");
                        draftPrimaryKey = draftIdGenMap.get("UNIQUEID");
                        // issuedDrafts.add(draftPrimaryKey);
                        HashMap inputs = new HashMap();
                        inputs.put("DraftNbr", draftPrimaryKey);
                        MFExecuter.executeMF(DRAFT_STOCKREG_REFRESH_SRV, environment, inputs);
                        // prepare the MultiCheque array
                        Map multiChequeArrayMap = prepareMultiChequeArray(draftNbr);
                        Object[] multiChequeObjecArray = new Object[10];

                        multiChequeObjecArray[0] = multiChequeArrayMap;
                        multiChequeObjecArray[1] = multiChequeArrayMap;
                        Map txnNewObject = getTxnObjMap(txnItem, postingMessageObjectArray, multiChequeObjecArray, null, txnCode,
                                holdLocId, false, "ISSDRFTACC", draftNbr);

                        logger.info("Calling the MFExecuter on " + ISSUEDRAFT_SRV + " Service.");
                        HashMap outputIssueDraft = MFExecuter.executeMF(ISSUEDRAFT_SRV, environment, txnNewObject);
                        logger.info("Finished the MFExecuter on " + ISSUEDRAFT_SRV + " Service.");

                        // Set Draft Number into Txn Object
                        if (outputIssueDraft != null && !outputIssueDraft.equals("")) {
                            txnItem.getDraftDetails().setDraftNumber(draftNbr);
                            // add data to draft hashmap to issue/deposit draft
                            // after successful batch posting
                            // FIXME: put all string literal as constant
                            draft.put("type", "ISSUE");
                            draft.put("holdLocId", holdLocId);
                            draft.put("draftPrimaryKey", draftPrimaryKey);
                            draft.put("txnNewObject", txnNewObject);
                            draft.put("txnItem", txnItem);
                            drafts.add(draft);
                        }
                        else {
                            if (logger.isInfoEnabled()) {
                                logger.info(ISSUEDRAFT_SRV + " Service is returning zero values.");
                            }
                            // add an error into the pageErrors
                            pageErrors.add(BatchGatewayCreateMethods.createBatchFileTransactionError(30200165, objectModel,
                                    txnItem, environment));
                            processedFlag = false;
                            break;
                        }
                    }
                    else {
                            logger.error(ISSUEDRAFT_SRV + "AutoId Generation is failed.");
                        // add an error into the pageErrors
                        pageErrors.add(BatchGatewayCreateMethods.createBatchFileTransactionError(30200166, objectModel, txnItem,
                                environment));
                        processedFlag = false;
                        break;
                    }
                }

                // Check if DFP then validate
                else if (txnCode.equalsIgnoreCase(draftPaymentTxnCode)
                        && (dRcRFlag != null && !dRcRFlag.equals("") && dRcRFlag.equalsIgnoreCase("D"))) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Draft Payment Transaction Encountered");
                    }
                    // prepare the DraftsMultiDrafts array
                    Map draftsMultiDraftsMap = prepareDraftsMultiDraftsArray(txnItem);
                    Object[] draftMultiDraftObjectArray = new Object[10];
                    draftMultiDraftObjectArray[0] = draftsMultiDraftsMap;
                    draftMultiDraftObjectArray[1] = draftsMultiDraftsMap;

                    // prepare the MultiCheque array
                    draftNbr = txnItem.getDraftDetails().getDraftNumber();
                    Map multiChequeArrayMap = prepareMultiChequeArray(draftNbr);
                    Object[] multiChequeObjecArray = new Object[10];

                    multiChequeObjecArray[0] = multiChequeArrayMap;
                    multiChequeObjecArray[1] = multiChequeArrayMap;

                    // Get the Holding Location Id
                    holdLocId = getHoldingLocId(txnItem.getDraftDetails().getHoldingLocation(), txnItem.getDraftDetails()
                            .getIssueBranch());

                    Map txnNewObject = getTxnObjMap(txnItem, postingMessageObjectArray, multiChequeObjecArray,
                            draftMultiDraftObjectArray, txnCode, holdLocId, true, "DEPDRFTACC", draftNbr);
                    logger.info("Calling the MFExecuter on " + DEPOSITDRAFT_SRV + " Service");
                    HashMap outputIssueDraft = MFExecuter.executeMF(DEPOSITDRAFT_SRV, environment, txnNewObject);
                    logger.info("finished the MFExecuter on " + DEPOSITDRAFT_SRV + " Service");
                    if (outputIssueDraft != null) {
                        // add data to draft hashmap to issue/deposit draft
                        // after successful batch posting
                        // FIXME: put all string literals as constant
                        draft.put("type", "DEPOSIT");
                        draft.put("holdLocId", holdLocId);
                        draft.put("txnItem", txnItem);
                        drafts.add(draft);
                    }
                    else {
                        if (logger.isInfoEnabled()) {
                            logger.info(ISSUEDRAFT_SRV + " Service is returning zero values.");
                        }
                        // add an error into the pageErrors
                        pageErrors.add(BatchGatewayCreateMethods.createBatchFileTransactionError(30200167, objectModel, txnItem,
                                environment));
                        processedFlag = false;
                        break;
                    }
                }
            }
        }
        if (processedFlag) {
            objectModel.getTFSBatchInformation().setProcessed(new Long(1));
        }
        if (logger.isInfoEnabled()) {
            logger.info("loaded bg txns " + SystemInformationManager.getInstance().getBFBusinessDateTimeMilliAsString());
        }
    }

    /**
     * This method is used to invoke BT_CMN_CreateTxnObject_SRV and return TxnObj and set the values
     * XML data to it.
     * 
     * @param txnItem
     * @param postingMessageObjectArray
     * @param multiChequeObjecArray
     * @param draftMultiDraftObjectArray
     * @param txnCode
     * @param monetaryInstrumentId
     * @param draftPymtFlag
     * @param inputTxnCode
     * @return Map
     */
    private Map<String, Object> getTxnObjMap(TxnItem txnItem, Object[] postingMessageObjectArray, Object[] multiChequeObjecArray,
            Object[] draftMultiDraftObjectArray, String txnCode, String monetaryInstrumentId, boolean draftPymtFlag,
            String inputTxnCode, String draftNbr) {
        HashMap<String, String> inputsTxnCodeMap = null;
        HashMap<String, Object> tmpTxnObject = null;
        HashMap<String, Object> txnNewObject = null;
        VectorTable newVectTable = null;
        Map<String, Object> txnMetaDataMap = null;

        inputsTxnCodeMap = new HashMap<String, String>();
        inputsTxnCodeMap.put("TxnCode", inputTxnCode);
        String dRcRFlag = txnItem.getMandatory().getDRCRFlag();
        if (txnCode.equalsIgnoreCase(draftIssueTxnCode)
                && (dRcRFlag != null && !dRcRFlag.equals("") && dRcRFlag.equalsIgnoreCase("C"))) {
            tmpTxnObject = MFExecuter.executeMF(CREATE_TXN_OBJECT, environment, inputsTxnCodeMap);
            tmpTxnObject.put("TxnCode", inputTxnCode);
            txnNewObject = MFExecuter.executeMF(INIT_TXN_OBJECT, environment, tmpTxnObject);
        }
        else {
            txnNewObject = MFExecuter.executeMF(CREATE_TXN_OBJECT, environment, inputsTxnCodeMap);
        }
        newVectTable = (VectorTable) txnNewObject.get("TxnObject");
        txnMetaDataMap = newVectTable.getMetaData();

        txnMetaDataMap.put(txnObjectKeyMap.get("Common_PostingMessagesArrayKey"), postingMessageObjectArray);
        txnMetaDataMap.put(txnObjectKeyMap.get("ChequesMultiChequesArray"), multiChequeObjecArray);
        // set FXTransaction to 1 for MT110
        txnMetaDataMap.put(txnObjectKeyMap.get("FXTransactionKey"), new Integer(7));

        if (draftPymtFlag) {
            txnMetaDataMap.put(txnObjectKeyMap.get("Drafts_MultiDraftsArrayKey"), draftMultiDraftObjectArray);
            Date valueDate = SystemInformationManager.getInstance().getBFBusinessDate(txnItem.getDraftDetails().getValueDate()) != null ? SystemInformationManager
                    .getInstance().getBFBusinessDate(txnItem.getDraftDetails().getValueDate()) : businessDate;
            txnMetaDataMap.put(txnObjectKeyMap.get("ValueDateKey"), valueDate);
            txnMetaDataMap.put(txnObjectKeyMap.get("Draft_ReferenceKey"), txnItem.getDraftDetails().getDraftNumber());
            txnMetaDataMap.put(txnObjectKeyMap.get("isInstrumentOnUsKey"), true);
            txnMetaDataMap.put("BulkIssuenceFlag", true);
        }
        if (txnItem.getMandatory() != null) {
            txnMetaDataMap.put(txnObjectKeyMap.get("Narrative1Key"), txnItem.getMandatory().getNarrative());
            txnMetaDataMap.put(txnObjectKeyMap.get("Narrative2Key"), txnItem.getMandatory().getNarrative());
        }
        if (txnItem.getCurrency() != null)
            txnMetaDataMap.put(txnObjectKeyMap.get("ExchangeRateTypeKey"), txnItem.getCurrency().getExchangeRateType());
        if (txnItem.getDraftDetails() != null) {
            txnMetaDataMap.put(txnObjectKeyMap.get("LocalHoldingLocationKey"), txnItem.getDraftDetails().getHoldingLocation());// "VAULT1");
            txnMetaDataMap.put(txnObjectKeyMap.get("InstrumentNameKey"), txnItem.getDraftDetails().getDraftInstrument());
        }
        txnMetaDataMap.put(txnObjectKeyMap.get("LocalMonetaryInstrumentIdKey"), monetaryInstrumentId);
        txnMetaDataMap.put(txnObjectKeyMap.get("SellAmountKey"), txnItem.getDraftDetails().getDraftAmount());
        txnMetaDataMap.put(txnObjectKeyMap.get("BuyAmountKey"), txnItem.getDraftDetails().getDraftAmount());
        txnMetaDataMap.put(txnObjectKeyMap.get("Draft_ReferenceKey"), draftNbr);
        txnMetaDataMap.put(txnObjectKeyMap.get("HostPartyIdKey"), txnItem.getDraftDetails().getCustomerNo());

        return txnNewObject;
    }

    /**
     * This method is used to prepare the multiChequeArrayData map with XML data
     * 
     * @param objectModel
     * @param draftNbr
     * @return
     */
    private Map<String, Object> prepareMultiChequeArray(String draftNbr) {
        Map<String, Object> multiChequeArrayData = new HashMap<String, Object>();
        multiChequeArrayData.put(txnObjectKeyMap.get("Draft_ReferenceKey"), draftNbr);
        multiChequeArrayData.put(txnObjectKeyMap.get("Cheque_SerialNumberKey"), new Integer(draftNbr));
        return multiChequeArrayData;
    }

    /**
     * This is used to prepare draftsMultiDraftsArrayData map with XML data
     * 
     * @param objectModel
     * @param txnItem
     * @return
     */
    private Map<String, Object> prepareDraftsMultiDraftsArray(TxnItem txnItem) {
        Map<String, Object> draftsMultiDraftsArrayData = new HashMap<String, Object>();
        draftsMultiDraftsArrayData.put(txnObjectKeyMap.get("IssuingBranchKey"), txnItem.getOptional().getSourceBranch());
        return draftsMultiDraftsArrayData;
    }

    /**
     * This method is used to read input XML file and load through TFSBatchPosting and return
     * 
     * @param inputXml
     * @return
     */
    private TFSBatchPosting readXML(String inputXml) {
        TFSBatchPosting objectModel = null;
        Reader xmlReader = null;
        try {
            xmlReader = new InputStreamReader(new FileInputStream(inputXml));
        }
        catch (Exception e) {
            e.printStackTrace();
            EventsHelper.handleEvent(40210003, new Object[] { inputXml }, new HashMap(), environment);
        }

        try {
            objectModel = (TFSBatchPosting) BankFusionCastorSupport.loadProfile(xmlReader,
                    GetUBConfigLocation.getUBConfigLocation() + BATCH_MAPPING_XML_PATH);
        }
        catch (Exception e) {
            logger.error("Exception Occured while loading the " + BATCH_MAPPING_XML_PATH + " - Mapping XML File");
            logger.error(ExceptionUtil.getExceptionAsString(e));
            EventsHelper.handleEvent(40210004, new Object[] { inputXml }, new HashMap(), environment);
        }
 finally {
			if (xmlReader != null) {
				try {
					xmlReader.close();
				} catch (IOException e) {
					logger.error("Exception Occured", e);
				}
			}
		}
        return objectModel;
    }

    /**
     * This method is used to write the XML file in processedFilePath
     * 
     * @param processedFilePath
     *            String
     * @param objectModel
     *            TFSBatchPosting
     * @return void
     */
    private void writeXML(String inputFileName, TFSBatchPosting objectModel) {
        Writer xmlWriter = null;
        StringTokenizer strToken = null;
        String dateStr = CommonConstants.EMPTY_STRING;
        Marshaller marshaller = null;
        ClassLoader classLoader = null;
        String processedFilePath = CommonConstants.EMPTY_STRING;
        File file = new File(inputFileName);
        String inputFileNameStr = CommonConstants.EMPTY_STRING;

        try {
            // Read
            processedFilePath = readConfigFromModuleConfiguation(PROCESSEDFILEPATHNAME);
            strToken = new StringTokenizer(processedFilePath, ".");
            processedFilePath = strToken.nextToken();
            dateStr = SystemInformationManager.getInstance().getBFBusinessDateTimeAsString();
            dateStr = dateStr.replace(' ', '_');
            dateStr = dateStr.replace(':', '-');

            strToken = new StringTokenizer(file.getName(), ".");
            inputFileNameStr = strToken.nextToken();
			processedFilePath = processedFilePath + inputFileNameStr + "."
					+ dateStr + ".XML";

            logger.info(processedFilePath);
            // Create xml file using Writer
            xmlWriter = new OutputStreamWriter(new FileOutputStream(processedFilePath));

            // load the xml mapping
            marshaller = new Marshaller();
            classLoader = Thread.currentThread().getContextClassLoader();
            Mapping mapping = new Mapping(classLoader);

            mapping.loadMapping(readMapping(BATCH_MAPPING_XML_PATH));

            marshaller.setMapping(mapping);
            marshaller.setWriter(xmlWriter);
            marshaller.setSuppressNamespaces(true);
            marshaller.marshal(objectModel);

        }
        catch (MappingException e) {
            e.printStackTrace();
            EventsHelper.handleEvent(40210006, new Object[] { processedFilePath }, new HashMap(), environment);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            EventsHelper.handleEvent(40210005, new Object[] { processedFilePath }, new HashMap(), environment);
        }
        catch (RuntimeException e) {
            e.printStackTrace();
            EventsHelper.handleEvent(40210006, new Object[] { processedFilePath }, new HashMap(), environment);
        }
        catch (MarshalException e) {
            e.printStackTrace();
            EventsHelper.handleEvent(40210006, new Object[] { processedFilePath }, new HashMap(), environment);
        }
        catch (ValidationException e) {
            e.printStackTrace();
            EventsHelper.handleEvent(40210006, new Object[] { processedFilePath }, new HashMap(), environment);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
 finally {
			if (xmlWriter != null) {
				try {
					xmlWriter.close();
				} catch (IOException e) {
					logger.error("Exception Occured", e);
				}
			}
		}
    }

    /**
     * This method is used to read mappings from input xml file and return InputSource
     * 
     * @param batchMappingXMLPath
     *            String
     * @return InputSource
     */
    private InputSource readMapping(String batchMappingXMLPath) {
        InputStream isMap = BankFusionResourceSupport.getResourceLoader().getInputStreamResourceFromLocationOnly(
                batchMappingXMLPath, GetUBConfigLocation.getUBConfigLocation());
        InputSource inputSource = new InputSource(new InputStreamReader(isMap));
        return inputSource;
    }

    /**
     * This method is used to return file path from BAT Module Configuration
     * 
     * @param processedFilePath
     *            String
     * @return String
     */
    private String readConfigFromModuleConfiguation(String configParamName) {
        String configuredParamValue = CommonConstants.EMPTY_STRING;
        try {
            IBusinessInformation bizInfo = ((IBusinessInformationService) ServiceManagerFactory.getInstance().getServiceManager()
                    .getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();
            configuredParamValue = bizInfo.getModuleConfigurationValue(MODULE_NAME, configParamName, environment).toString();
        }
        catch (BankFusionException e) {
            if (logger.isInfoEnabled()) {
                logger.info("Path for processed batch file is not configured");
            }
        }
        return configuredParamValue;
    }

    /**
     * This method is used to xml data to Common Posting map
     * 
     * @param objectModel
     *            TFSBatchPosting
     * @param txnItem
     *            TxnItem
     * @return Map
     */
    private Map<String, Object> prepareCommonPostingArray(TxnItem txnItem) {
        if (logger.isInfoEnabled()) {
            logger.info("prepareCommonPostingArray - Preparing the Common Posting Array Vector");
        }
        DraftDetails draftDetails = txnItem.getDraftDetails();
        Mandatory mandatoryTxnItemDetails = txnItem.getMandatory();
        Optional optionalBatchTxnItemDetails = txnItem.getOptional();
        String custShortName = optionalBatchTxnItemDetails.getShortName();
        String postingType = txnItem.getPostingType();

        // set common posting array details
        Map<String, Object> postingMessageData = new HashMap<String, Object>();
        if (mandatoryTxnItemDetails != null) {
            postingMessageData.put(txnObjectKeyMap.get("AccountIdKey"), mandatoryTxnItemDetails.getAccountID());
            postingMessageData.put(txnObjectKeyMap.get("AmountKey"), mandatoryTxnItemDetails.getAmount());
        }
        if (draftDetails != null) {
            postingMessageData.put(txnObjectKeyMap.get("HoldingLocationIdKey"), draftDetails.getHoldingLocation());
            postingMessageData.put(txnObjectKeyMap.get("BuyMonetaryInstrumentIdKey"), draftDetails.getDraftInstrument());
            postingMessageData.put(txnObjectKeyMap.get("SellMonetaryInstrumentIdKey"), draftDetails.getDraftInstrument());
            postingMessageData.put(txnObjectKeyMap.get("CurrencyCodeKey"), draftDetails.getDraftCurrency());
            Date valueDate = SystemInformationManager.getInstance().getBFBusinessDate(txnItem.getDraftDetails().getValueDate()) != null ? SystemInformationManager
                    .getInstance().getBFBusinessDate(txnItem.getDraftDetails().getValueDate()) : businessDate;
            postingMessageData.put(txnObjectKeyMap.get("ValueDateKey"), valueDate);
        }
        postingMessageData.put(txnObjectKeyMap.get("AccountShortNameKey"), CommonConstants.EMPTY_STRING);

        postingMessageData.put(txnObjectKeyMap.get("PostingActionKey"), postingType);
        postingMessageData.put(txnObjectKeyMap.get("CurrencyDescriptionKey"), CommonConstants.EMPTY_STRING);
        postingMessageData.put(txnObjectKeyMap.get("AccountFormatKey"), CommonConstants.EMPTY_STRING);
        postingMessageData.put(txnObjectKeyMap.get("HostPartyIdKey"), CommonConstants.EMPTY_STRING);
        postingMessageData.put(txnObjectKeyMap.get("CustomerShortNameKey"), custShortName);
        postingMessageData.put("AccountBalance", CommonConstants.BIGDECIMAL_ZERO);
        return postingMessageData;
    }

    /**
     * This method is to validate and add the error message into SimplePersistentObject List.
     * 
     * @param tfsBatchPosting
     *            TFSBatchPosting
     * @param txnItem
     *            TxnItem
     * @param environment
     *            BankFusionEnvironment
     * @return statusFlag
     */
    private boolean validateTxnDraft(TFSBatchPosting tfsBatchPosting, TxnItem txnItem, BankFusionEnvironment environment) {
        boolean statusFlag = false;
        // validate txn amount against draft amount
        if (txnItem.getDraftDetails() != null) {
            BigDecimal txnAmount = txnItem.getMandatory().getAmount();
            BigDecimal dftAmount = txnItem.getDraftDetails().getDraftAmount();

            if (!txnAmount.equals(dftAmount)) {
                pageErrors.add(BatchGatewayCreateMethods.createBatchFileTransactionError(30200161, tfsBatchPosting, txnItem,
                        environment));
                statusFlag = true;
            }

            String valueDate = txnItem.getDraftDetails().getValueDate();
            if (valueDate == null || valueDate.trim().equals("")) {
                pageErrors.add(BatchGatewayCreateMethods.createBatchFileTransactionError(30200162, tfsBatchPosting, txnItem,
                        environment));
                statusFlag = true;
            }
            // check for txnCode != null
            String txnCode = txnItem.getMandatory().getTxnCode();
            if (txnCode == null || txnCode.equals("")) {
                pageErrors.add(BatchGatewayCreateMethods.createBatchFileTransactionError(30200163, tfsBatchPosting, txnItem,
                        environment));
                statusFlag = true;
            }

            String draftType = txnItem.getDraftDetails().getDraftType();
            if (draftType == null || draftType.equals("") || !(draftType.equals("Blank") || draftType.equals("Pre-numbered"))) {
                pageErrors.add(BatchGatewayCreateMethods.createBatchFileTransactionError(30200164, tfsBatchPosting, txnItem,
                        environment));
                statusFlag = true;
            }

        }
        return statusFlag;
    }

    /**
     * This method is insert the error messaged inot BatchError Table.
     * 
     * @return void
     */
    private void persistErrors() {
        for (SimplePersistentObject pageError : pageErrors) {
            getFactory().create(IBOBatchError.BONAME, pageError);
        }
    }

    /**
     * This method is to return IPersistenceObjectsFactory object.
     * 
     * @return IPersistenceObjectsFactory
     */
    private IPersistenceObjectsFactory getFactory() {
        return BankFusionThreadLocal.getPersistanceFactory();
    }

    /**
     * This method is to validate Draft Amount against Draft Amount in XML and return false if
     * validation is success otherwise true.
     * 
     * @param objectModel
     * @return validationStatus
     */
    private boolean validateDraftAmt(TFSBatchPosting objectModel) {
        TransactionDetails transactionDetails = null;
        TxnItem txnItem = null;
        TxnItem[] txnItemArray = null;
        transactionDetails = objectModel.getTFSBatchInformation().getTransactionDetails();
        txnItemArray = objectModel.getTFSBatchInformation().getTransactionDetails().getTxnItem();
        batchReference = objectModel.getTFSBatchInformation().getBatchReference();
        // Loop through all transactions
        if (txnItemArray != null) {
            for (int i = 0; i < txnItemArray.length; i++) {
                txnItem = transactionDetails.getTxnItem()[i];
                if (txnItem != null) {
                    // call validation...
                    validationStatus = validateTxnDraft(objectModel, txnItem, environment) || validationStatus;
                }
            }
        }
        return validationStatus;
    }

    private void issueDraftAndGenerateSwiftMsg(TxnItem txnItem, String holdLocId, Map txnNewObject) {
        String holdLocName = txnItem.getDraftDetails().getHoldingLocation();
        txnItem.getDraftDetails().setHoldingLocation(holdLocId);
        String swiftIndicator = "N/A";
        // Generate Swift Message only for cross currency
        // FIXME: fix it require if debitCurrency is null
        if (!txnItem.getDraftDetails().getDraftCurrency().equals(txnItem.getDraftDetails().getDebitCurrency())) {
            HashMap outputIssueDraft = MFExecuter.executeMF(SWIFT_GENERATION_SRV, environment, txnNewObject);
            // check the status of swift generation
            Boolean isSIAvailable = (Boolean) outputIssueDraft.get("SwiftFlag");
            if (isSIAvailable.booleanValue()) {
                swiftIndicator = "Yes";
            }
            // TODO: Rise a DBCR for warning msg.
            // Once it is available this should be uncommented
            /*
             * if(!isSIAvailable.booleanValue()){ pageErrors.add(BatchGatewayCreateMethods
             * .createBatchFileTransactionError(30200168, objectModel, txnItem, environment)); }
             */
        }

        insertDraftIntoStockRegister(txnItem, swiftIndicator);
        updateHoldingLocForDraftAcc1(txnItem.getDraftDetails().getDraftInstrument(), txnItem.getDraftDetails().getDraftCurrency(),
                holdLocId);
        txnItem.getDraftDetails().setHoldingLocation(holdLocName);

    }

    private void depositDraft(TxnItem txnItem, String holdLocId) {
        String holdLocName = txnItem.getDraftDetails().getHoldingLocation();
        txnItem.getDraftDetails().setHoldingLocation(holdLocId);
        String swiftIndicator = "N/A";
        // Insert Validated Draft into StockRegister table
        insertDraftIntoStockRegister(txnItem, swiftIndicator);
        txnItem.getDraftDetails().setHoldingLocation(holdLocName);
        // FIXME: this can be removed as drafts list will hold the actual size
        // of deposit draft
        ++depositedDraftCount;
    }
}