package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_DOCUPLOADDTLS;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_DocUploadAddDelete;
import com.trapedza.bankfusion.utils.GUIDGen;

import bf.com.misys.ub.types.remittanceprocess.DocumentUpload;
import bf.com.misys.ub.types.remittanceprocess.DocumentUploadList;

public class SWT_DocUploadAddDelete extends AbstractSWT_DocUploadAddDelete {

	/**
	 * 
	 */
	
	private transient final static Log logger = LogFactory.getLog(SWT_DocUploadAddDelete.class.getName());
	private static final long serialVersionUID = 1L;

	IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
	IBOUB_SWT_DOCUPLOADDTLS iboDocUpload;
	private final static String whereByBOID = "WHERE " + IBOUB_SWT_DOCUPLOADDTLS.UBDOCUMENTIDPK + " = ?";
	private static final String documentListQuery = " WHERE " + IBOUB_SWT_DOCUPLOADDTLS.UBMESSAGEREFID	+ " = ?";
	private static final String documentSavedIdQuery = " WHERE " + IBOUB_SWT_DOCUPLOADDTLS.UBDOCUMENTSAVEDID	+ " = ?";
	private static final String DOCUMENT_DEL_MICROFLOW = "DMS_R_CB_DOC_DeleteDocument_SRV";

	@SuppressWarnings("deprecation")
	public SWT_DocUploadAddDelete(BankFusionEnvironment env) {
		super(env);
	}

	public SWT_DocUploadAddDelete() {
	}	


	public void process(BankFusionEnvironment env) throws BankFusionException {
		logger.info("Fatom for Document Upload");
		DocumentUploadList documentUploadList = getF_IN_DocumentUploadList();
		String messageId = getF_IN_MessageId();
		String documentSavedId = getF_IN_DocSavedId();
		ArrayList param = new ArrayList();
		param.add(messageId);
		
		DocumentUploadList documentAddList = new DocumentUploadList();
		ArrayList documentIdDeletePKList = new ArrayList();
		ArrayList<String> documentSavedIdList = new ArrayList<String>();

		if(null!=documentSavedId && !documentSavedId.isEmpty()){
			ArrayList param1 = new ArrayList();
			param1.add(documentSavedId);
			List<IBOUB_SWT_DOCUPLOADDTLS> documentSavedIdListDB = factory.findByQuery(IBOUB_SWT_DOCUPLOADDTLS.BONAME, documentSavedIdQuery, param1,null);
			if(documentSavedIdListDB.size()==0){
				documentSavedIdList.add(documentSavedId);
				deleteDocumentFromDMS(documentSavedIdList);
			}
		}
		else{
			List<IBOUB_SWT_DOCUPLOADDTLS> documentDtlList = factory.findByQuery(IBOUB_SWT_DOCUPLOADDTLS.BONAME, documentListQuery, param,null);
			if (null != documentDtlList && !documentDtlList.isEmpty()) {
				//Delete Block
				for(int i=0;i<documentDtlList.size();i++){
					IBOUB_SWT_DOCUPLOADDTLS docUpldDtl = (IBOUB_SWT_DOCUPLOADDTLS) documentDtlList.get(i);
					boolean flag = true;
		            for(DocumentUpload docUpload: documentUploadList.getDocumentUpload()){
		            	if(docUpldDtl.getF_UBDOCUMENTSAVEDID().equalsIgnoreCase(docUpload.getDocumentSavedId())){
		            		flag = false;
		            	}
		            }
		            if(flag){
		            	documentIdDeletePKList.add(docUpldDtl.getBoID());
		            	documentSavedIdList.add(docUpldDtl.getF_UBDOCUMENTSAVEDID());
		            }
				}
				//Add Block
				for(DocumentUpload docUpload: documentUploadList.getDocumentUpload()){
					boolean flag = true;
					for(int i=0;i<documentDtlList.size();i++){
						IBOUB_SWT_DOCUPLOADDTLS docUpldDtl = (IBOUB_SWT_DOCUPLOADDTLS) documentDtlList.get(i);
						if(docUpload.getDocumentSavedId().equals(docUpldDtl.getF_UBDOCUMENTSAVEDID())){
							flag = false;
						}
					}
					if(flag && !docUpload.getDocumentSavedId().isEmpty()){
						documentAddList.addDocumentUpload(docUpload);
					}
				}
	        }
			else{
				documentAddList = documentUploadList;
			}
			//Call Add Block
			if(null!=documentAddList && documentAddList.getDocumentUploadCount()>0){
				addDocumentsToMessage(documentAddList);
			}
			//Call Delete Block
			if(!documentIdDeletePKList.isEmpty()){
				deleteDocumentFromDMS(documentSavedIdList);
				deleteDocumentFromMessage(documentIdDeletePKList);
			}
		}
		
			
		
	}


	public void addDocumentsToMessage(DocumentUploadList documentUploadList){
		logger.info("Method to add records in UB table for Document Upload");
		int count = documentUploadList.getDocumentUploadCount();
		for(int i=0; i<count; i++ ){
			DocumentUpload docUpload = documentUploadList.getDocumentUpload(i);
			iboDocUpload = (IBOUB_SWT_DOCUPLOADDTLS) factory.getStatelessNewInstance(IBOUB_SWT_DOCUPLOADDTLS.BONAME);
			iboDocUpload.setBoID(GUIDGen.getNewGUID());
			iboDocUpload.setF_UBCHANNELID("SWIFT");
			iboDocUpload.setF_UBDIRECTION("I");
			iboDocUpload.setF_UBDOCUMENTTYPE(docUpload.getDocumentType());
			iboDocUpload.setF_UBDESCRIPTION(docUpload.getDescription());
			iboDocUpload.setF_UBMESSAGEREFID(getF_IN_MessageId());
			iboDocUpload.setF_UBDOCUMENTSAVEDID(docUpload.getDocumentSavedId());
			iboDocUpload.setF_UBREFERENCENUMBER(docUpload.getDocumentReference());
			iboDocUpload.setF_UBATTACHEDDATE(new java.sql.Date(Calendar.getInstance().getTime().getTime()));
			factory.create(IBOUB_SWT_DOCUPLOADDTLS.BONAME, iboDocUpload);
		}
	}

	public void deleteDocumentFromDMS(ArrayList<String> docSavedIdList){
		logger.info("Method to delete documents from DMS");
		for(String docSavedId:docSavedIdList){
			HashMap moduleParams = new HashMap<>();
			bf.com.misys.cbs.msgs.doc.v1r0.DeleteDocumentRq deleteDocumentRq = new bf.com.misys.cbs.msgs.doc.v1r0.DeleteDocumentRq();
			bf.com.misys.cbs.types.DeleteDocInput deleteDocInput = new bf.com.misys.cbs.types.DeleteDocInput();
			
			deleteDocInput.setDocumentID(docSavedId);
			deleteDocumentRq.setDeleteDocInput(deleteDocInput);
			moduleParams.put("deleteDocumentRq", deleteDocumentRq);
			try{
				MFExecuter.executeMF(DOCUMENT_DEL_MICROFLOW,BankFusionThreadLocal.getBankFusionEnvironment(), moduleParams);
			}catch(Exception ex){
				logger.error(ExceptionUtil.getExceptionAsString(ex));
			}
		}
		
	}

	public void deleteDocumentFromMessage(ArrayList documentIdPKList){
		logger.info("Method to delete document details from UB table");
		if(null!=documentIdPKList && !documentIdPKList.isEmpty()){
			factory.bulkDelete(IBOUB_SWT_DOCUPLOADDTLS.BONAME, whereByBOID, documentIdPKList);
		}			
	}



}
