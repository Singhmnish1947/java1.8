package com.misys.ub.swift.tellerRemittance;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.finastra.api.paymentInititation.PaymentInitiationRequest;
import com.misys.fbe.common.util.CommonUtil;
import com.misys.ub.datacenter.DataCenterCommonUtils;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.misys.ub.swift.remittance.dao.DocumentUploadDao;
import com.misys.ub.swift.tellerRemittance.persistence.RemittanceFeeDao;
import com.misys.ub.swift.tellerRemittance.persistence.RemittanceMessageDao;
import com.misys.ub.swift.tellerRemittance.persistence.RemittanceTaxDao;
import com.misys.ub.swift.tellerRemittance.persistence.RemittanceTaxOnTaxDao;
import com.misys.ub.swift.tellerRemittance.utils.ApiUrls;
import com.misys.ub.swift.tellerRemittance.utils.PostRemittanceMessage;
import com.misys.ub.swift.tellerRemittance.utils.PrepareRemittanceApiRequestMsg;
import com.misys.ub.swift.tellerRemittance.utils.RemittanceConstants;
import com.misys.ub.swift.tellerRemittance.utils.RemittanceHelper;
import com.misys.ub.swift.tellerRemittance.utils.RemittanceRequestMapper;
import com.misys.ub.swift.tellerRemittance.utils.RemittanceStatusDto;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractRemittanceInitiationProcess;

import bf.com.misys.cbs.msgs.v1r0.TellerRemittanceRq;
import bf.com.misys.cbs.msgs.v1r0.TellerRemittanceRs;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.swift.DocumentUploadDtls;
import bf.com.misys.cbs.types.swift.TellerRemittanceRsDtls;
import bf.com.misys.cbs.types.swift.TxnfeesInformation;

public class RemittanceInitiationProcess extends AbstractRemittanceInitiationProcess {
    private transient final static Log LOGGER = LogFactory.getLog(RemittanceInitiationProcess.class);

    @SuppressWarnings("deprecation")
    public RemittanceInitiationProcess(BankFusionEnvironment env) {
        super(env);
    }

    @Override
    public void process(BankFusionEnvironment env) {

        TellerRemittanceRq remittanceRq = getF_IN_tellerRemittanceRq();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("IN RemittanceInitiationProcess" + RemittanceHelper.getXmlFromComplexType(remittanceRq));
        }

        Boolean isCashTxn = remittanceRq.getTxnAdditionalDtls().getFundingMode().equals(RemittanceConstants.CASH_FUNDING_MODE)
                ? Boolean.TRUE
                : Boolean.FALSE;
        String txnCurrencyCode = remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                .getCreditAmount().getIsoCurrencyCode();
        // RemittanceStatusDto is initialised
        RemittanceStatusDto statusDto = getDefaultConfigDetails(txnCurrencyCode,
                BankFusionThreadLocal.getUserSession().getBranchSortCode(), isCashTxn);
        // remittance ID PK
        String remittanceIDPK = PaymentSwiftUtils.getRemittanceId(remittanceRq.getInitiateSwiftMessageRqDtls()
                .getTransactionDetails().getCreditPostingDtls().getCreditAmount().getIsoCurrencyCode(),
                PaymentSwiftConstants.REMITTACNCE_OUTWARD);
        remittanceRq.getTxnAdditionalDtls().setRemittanceId(remittanceIDPK);

        // get nostro account for account and account cheque
        remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls()
                .setCreditAccountId(statusDto.getGppNostroAccountId());

        // call to the Rest Client
        if (statusDto.getIsGppConnected()) {
            PrepareRemittanceApiRequestMsg prepareRquestMsg = new PrepareRemittanceApiRequestMsg();
            PaymentInitiationRequest apiRequest = prepareRquestMsg.preparePaymentInitiationRequest(remittanceRq);

            //send request as string
            statusDto = RemittanceRestClient.initaitePaymentApi(RemittanceRequestMapper.mapPaymentInitiationRequest(apiRequest),
                    statusDto);


            // Rest api call using class object
            if (null != statusDto.getGppTransactionIndividualStatus()) {
                if (statusDto.getGppTransactionIndividualStatus().equals(RemittanceConstants.REJECTED_STATUS)) {
                    CommonUtil.handleUnParameterizedEvent(RemittanceConstants.E_FBE_GPP_TRANSACTION_REJECTED);
                }
                
                if (statusDto.getGppTransactionIndividualStatus().equals(RemittanceConstants.DUPLICATE_STATUS)) {
                    CommonUtil.handleUnParameterizedEvent(RemittanceConstants.E_FBE_GPP_TRANSACTION_DUPLICATE);
                }
                
                LOGGER.info("GPP Payment status OrgnlEndToEndId :::: " + statusDto.getOriginalEndToEndId());
                LOGGER.info("GPP Payment status gppPaymentStatusId :::: " + statusDto.getGppPaymentStatusId());
                LOGGER.info("GPP Payment status gppTranscationStatusCode :::: " + statusDto.getGppTransactionIndividualStatus());
            }
        }

        // call the posting code in case of cash
        if (isCashTxn) {
            PostRemittanceMessage postRemittanceMsg = new PostRemittanceMessage();
            RsHeader rsHeader = postRemittanceMsg.postCashTxn(remittanceRq, statusDto);

            if (null != rsHeader && !rsHeader.getOrigCtxtId().isEmpty()
                    && PaymentSwiftConstants.SUCCESS.equalsIgnoreCase(rsHeader.getStatus().getOverallStatus())) {
                statusDto.setHostTransactionId(rsHeader.getOrigCtxtId());
            }
            else if (null != rsHeader && null != rsHeader.getStatus()
                    && StringUtils.isNotBlank(rsHeader.getStatus().getCodes(0).getCode())) {
                PaymentSwiftUtils.handleEvent(Integer.parseInt(rsHeader.getStatus().getCodes(0).getCode()),
                        new String[] { PaymentSwiftUtils.getEventParameter(rsHeader) });
            }
            else {
                CommonUtil.handleUnParameterizedEvent(Integer.valueOf(PaymentSwiftConstants.EVT_POSTING_FAILED));
            }

        }

        // Insert into Remittance Message Table
        RemittanceMessageDao.insertData(remittanceRq, statusDto);

        //Insert feesAndInformation table.
        RemittanceFeeDao.insertData(remittanceRq);
        RemittanceTaxDao.insertData(remittanceRq);
        RemittanceTaxOnTaxDao.insertData(remittanceRq);
               
        // Document Upload
        DocumentUploadDtls[] docUploadDtls = remittanceRq.getInitiateSwiftMessageRqDtls().getDocumentUpload();
        documentUpload(docUploadDtls, remittanceIDPK);

        setF_OUT_tellerRemittanceRs(buildTellerRemittanceResponse(remittanceRq, statusDto));
    }

    /**
     * Method Description:Document Upload Insert
     * 
     * @param docUploadDtls
     * @param remittanceIdPk
     */
    private void documentUpload(DocumentUploadDtls[] docUploadDtls, String remittanceIdPk) {
        DocumentUploadDao document = new DocumentUploadDao();
        RsHeader rsHeader = document.insertDocumentDetails(docUploadDtls, remittanceIdPk, PaymentSwiftConstants.CHANNEL_UXP);
        if (!PaymentSwiftConstants.SUCCESS.equalsIgnoreCase(rsHeader.getStatus().getOverallStatus())) {
            PaymentSwiftUtils.handleEvent(Integer.parseInt(rsHeader.getStatus().getCodes(0).getCode()),
                    new String[] { PaymentSwiftUtils.getEventParameter(rsHeader) });
        }
    }

    private TellerRemittanceRs buildTellerRemittanceResponse(TellerRemittanceRq remittanceRq, RemittanceStatusDto statusDto) {
        TellerRemittanceRs response = new TellerRemittanceRs();
        TellerRemittanceRsDtls tellerRemittanceRsDtls = new TellerRemittanceRsDtls();

        tellerRemittanceRsDtls.setNostroCreditAccountId(
                remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getCreditPostingDtls().getCreditAccountId());
        tellerRemittanceRsDtls
                .setHostTxnId(remittanceRq.getTxnAdditionalDtls().getFundingMode().equals(RemittanceConstants.CASH_FUNDING_MODE)
                        ? statusDto.getHostTransactionId()
                        : "DUMMYTxnId");
        tellerRemittanceRsDtls.setIsCashTxn(
                remittanceRq.getTxnAdditionalDtls().getFundingMode().equals(RemittanceConstants.CASH_FUNDING_MODE) ? Boolean.TRUE
                        : Boolean.FALSE);
        tellerRemittanceRsDtls.setRemittanceStatus(remittanceRq.getTxnAdditionalDtls().getRemittanceStatus());
        tellerRemittanceRsDtls.setRemittanceId(remittanceRq.getTxnAdditionalDtls().getRemittanceId());
        tellerRemittanceRsDtls
                .setSenderReference(remittanceRq.getInitiateSwiftMessageRqDtls().getTransactionDetails().getSenderReference());
        response.setTellerRemittanceRsDtls(tellerRemittanceRsDtls);

        return response;
    }

    /**
     * Method Description:Get default configuration details from module configuration
     * 
     * @param txnCurrencyCode
     * @param branchCode
     * @param isCash
     * @return
     */
    private RemittanceStatusDto getDefaultConfigDetails(String txnCurrencyCode, String branchCode, Boolean isCash) {
        RemittanceStatusDto statusDto = new RemittanceStatusDto();
        String psuedonymName = DataCenterCommonUtils.readModuleConfiguration(RemittanceConstants.FEX_MODULE_ID,
                RemittanceConstants.PSEUDONYM_NOSTRO_ACCOUNT_GPP);

        // GPP_CONNECTED
        statusDto.setIsGppConnected(StringUtils.isNotBlank(ApiUrls.getBaseUrl()) ? Boolean.TRUE : Boolean.FALSE);

        // nostro accountId
        statusDto.setGppNostroAccountId(DataCenterCommonUtils.retrievePsuedonymAcctId(txnCurrencyCode, branchCode,
                PaymentSwiftConstants.CURRENCY_PSEDONYM_CONTEXT, psuedonymName));

        // is cash Txn
        if (isCash) {

            String internalCashPsuedonymName = DataCenterCommonUtils.readModuleConfiguration(RemittanceConstants.FEX_MODULE_ID,
                    RemittanceConstants.PSEUDONYM_INTERNAL_CASH_ACCOUNT_GPP);
            statusDto.setGppInternalCashAccountId(DataCenterCommonUtils.retrievePsuedonymAcctId(txnCurrencyCode, branchCode,
                    PaymentSwiftConstants.CURRENCY_PSEDONYM_CONTEXT, internalCashPsuedonymName));

            String chargePsuedonymName = DataCenterCommonUtils.readModuleConfiguration(RemittanceConstants.FEX_MODULE_ID,
                    RemittanceConstants.PSEUDONYM_CHARGE_ACCOUNT_GPP);
            statusDto.setGppInternalChargeAccountId(DataCenterCommonUtils.retrievePsuedonymAcctId(txnCurrencyCode, branchCode,
                    PaymentSwiftConstants.CURRENCY_PSEDONYM_CONTEXT, chargePsuedonymName));

            String taxPsuedonymName = DataCenterCommonUtils.readModuleConfiguration(RemittanceConstants.FEX_MODULE_ID,
                    RemittanceConstants.PSEUDONYM_TAX_ACCOUNT_GPP);
            statusDto.setGppInternalTaxAccountId(DataCenterCommonUtils.retrievePsuedonymAcctId(txnCurrencyCode, branchCode,
                    PaymentSwiftConstants.CURRENCY_PSEDONYM_CONTEXT, taxPsuedonymName));

            statusDto.setGppMisTxnCodeForChargeAndTax(DataCenterCommonUtils
                    .readModuleConfiguration(RemittanceConstants.FEX_MODULE_ID, RemittanceConstants.MIS_TXN_CODE_CHARGE_TAX_GPP));
        }

        return statusDto;

    }

}
