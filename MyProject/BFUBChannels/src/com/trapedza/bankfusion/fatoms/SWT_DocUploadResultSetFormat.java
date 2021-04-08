package com.trapedza.bankfusion.fatoms;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import bf.com.misys.ub.types.remittanceprocess.DocumentUpload;
import bf.com.misys.ub.types.remittanceprocess.DocumentUploadList;

import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractDocUploadResulttSetFormat;

public class SWT_DocUploadResultSetFormat extends AbstractDocUploadResulttSetFormat {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("deprecation")
	public SWT_DocUploadResultSetFormat(BankFusionEnvironment env) {
		super(env);
	}

	public SWT_DocUploadResultSetFormat() {
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {
		VectorTable InputVectorTable  = getF_IN_InputVector();
		VectorTable OUTputVectorTable  = new VectorTable();

		DocumentUploadList documentUploadList = new DocumentUploadList();
		DocumentUpload  documentUpload = null;


		if (InputVectorTable != null && InputVectorTable.hasData()) {
			int Size = InputVectorTable.size();
			for (int i = 0; i < Size; i++) {
				HashMap attributes   =  (HashMap) InputVectorTable.getRowTagsAsFields(i);
				HashMap OUTattributes = new HashMap();
				if(attributes!=null){
					documentUpload = new DocumentUpload();
					documentUpload.setDocumentType(attributes.get("f_UBDOCUMENTTYPE").toString());
					documentUpload.setDescription(attributes.get("f_UBDESCRIPTION").toString());
					documentUpload.setDocumentSavedId(attributes.get("f_UBDOCUMENTSAVEDID").toString());
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					Date parsed = null;
					try {
						parsed = format.parse(attributes.get("f_UBATTACHEDDATE").toString());
					} catch (ParseException e) {
						e.printStackTrace();
					}
					if(parsed != null){
						java.sql.Date sql = new java.sql.Date(parsed.getTime());
						documentUpload.setAttachedDate(sql);						
					}
					documentUpload.setDocumentReference(attributes.get("f_UBREFERENCENUMBER").toString());
					documentUploadList.addDocumentUpload(documentUpload);
				}
			}
		}

		setF_OUT_DocumentUploadList(documentUploadList);
	}

}
