package com.misys.ub.swift.remittance.dao;

import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.BankFusionException;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.cbs.common.constants.CBSConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_DOCUPLOADDTLS;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.utils.GUIDGen;

import bf.com.misys.cbs.types.header.EventParameters;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;
import bf.com.misys.cbs.types.swift.DocumentUploadDtls;

public class DocumentUploadDao {

    private transient final static Log logger = LogFactory.getLog(DocumentUploadDao.class.getName());

	public RsHeader insertDocumentDetails(DocumentUploadDtls[] docUploadDtls, String messageId, String channelId) {
		RsHeader rsHeader = new RsHeader();
		MessageStatus txnStatus = new MessageStatus();
		txnStatus.setOverallStatus(PaymentSwiftConstants.SUCCESS);
		rsHeader.setStatus(txnStatus);
		IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
		try {
			for(DocumentUploadDtls docUpload : docUploadDtls) {
				IBOUB_SWT_DOCUPLOADDTLS iboDocUpload = (IBOUB_SWT_DOCUPLOADDTLS) factory.getStatelessNewInstance(IBOUB_SWT_DOCUPLOADDTLS.BONAME);
				iboDocUpload.setBoID(GUIDGen.getNewGUID());
				iboDocUpload.setF_UBCHANNELID(channelId);
				iboDocUpload.setF_UBDIRECTION(PaymentSwiftConstants.OUTWARD);
				iboDocUpload.setF_UBDOCUMENTTYPE(docUpload.getDocumentType());
				iboDocUpload.setF_UBDESCRIPTION(docUpload.getDescription());
				iboDocUpload.setF_UBMESSAGEREFID(messageId);
				iboDocUpload.setF_UBDOCUMENTSAVEDID(docUpload.getDocumentSavedId());
				iboDocUpload.setF_UBREFERENCENUMBER(docUpload.getReferenceNumber());
				iboDocUpload.setF_UBATTACHEDDATE(new java.sql.Date(Calendar.getInstance().getTime().getTime()));
				factory.create(IBOUB_SWT_DOCUPLOADDTLS.BONAME, iboDocUpload);
			}
		}
		catch (BankFusionException e) {
			logger.error("Error Message during insertion into UBTB_DOCUPLOADDTLS" + ExceptionUtil.getExceptionAsString(e));
			SubCode subCode = new SubCode();
			int error = 20600092;
			String errorCode = Integer.toString(error);
			EventParameters parameter = new EventParameters();
			parameter.setEventParameterValue("UBTB_DOCUPLOADDTLS");
			subCode.addParameters(parameter);
			subCode.setCode(errorCode);
			subCode.setDescription(e.getEvents().iterator().next().getMessage());
			subCode.setFieldName(CommonConstants.EMPTY_STRING);
			subCode.setSeverity(CBSConstants.ERROR);
			txnStatus.addCodes(subCode);
			txnStatus.setOverallStatus("E");
			rsHeader.setStatus(txnStatus);
		}
		return rsHeader;

	}

}
