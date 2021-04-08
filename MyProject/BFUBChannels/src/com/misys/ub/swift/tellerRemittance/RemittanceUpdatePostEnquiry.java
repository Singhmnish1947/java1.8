package com.misys.ub.swift.tellerRemittance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.fatoms.SWT_DocUploadAddDelete;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractRemittanceUpdatePostEnquiry;

import bf.com.misys.cbs.msgs.v1r0.TellerRemittanceRq;
import bf.com.misys.cbs.types.swift.DocumentUploadDtls;
import bf.com.misys.ub.types.remittanceprocess.DocumentUpload;
import bf.com.misys.ub.types.remittanceprocess.DocumentUploadList;

public class RemittanceUpdatePostEnquiry extends AbstractRemittanceUpdatePostEnquiry {

    private transient final static Log logger = LogFactory.getLog(RemittanceUpdatePostEnquiry.class.getName());

    private static final long serialVersionUID = 1L;

    public RemittanceUpdatePostEnquiry(BankFusionEnvironment env) {
        super(env);
    }

    public RemittanceUpdatePostEnquiry() {
    }

    public void process(BankFusionEnvironment env) throws BankFusionException {

        logger.info("IN RemittanceUpdatePostEnquiry");

        TellerRemittanceRq request = getF_IN_tellerRemittanceRq();
        DocumentUploadDtls[] vDocumentUploadArray = request.getInitiateSwiftMessageRqDtls().getDocumentUpload();
        if (null != vDocumentUploadArray && vDocumentUploadArray.length > 0) {
            SWT_DocUploadAddDelete docUpload = new SWT_DocUploadAddDelete();
            docUpload.setF_IN_channelId(env.getUserSession().getChannelID());
            docUpload.setF_IN_direction("O");
            docUpload.setF_IN_MessageId(request.getTxnAdditionalDtls().getRemittanceId());
            DocumentUploadList documentList = new DocumentUploadList();

            for (int i = 0; i < vDocumentUploadArray.length; i++) {
                DocumentUploadDtls docDtls = vDocumentUploadArray[i];
                DocumentUpload vDocumentUpload = new DocumentUpload();
                vDocumentUpload.setAttachedDate(docDtls.getAttachedDate());
                vDocumentUpload.setDescription(docDtls.getDescription());
                vDocumentUpload.setDocumentReference(docDtls.getReferenceNumber());
                vDocumentUpload.setDocumentSavedId(docDtls.getDocumentSavedId());
                vDocumentUpload.setDocumentType(docDtls.getDocumentType());
                documentList.addDocumentUpload(vDocumentUpload);
            }

            docUpload.setF_IN_DocumentUploadList(documentList);
            docUpload.process(env);
        }

        logger.info("OUT RemittanceUpdatePostEnquiry");
    }

}
