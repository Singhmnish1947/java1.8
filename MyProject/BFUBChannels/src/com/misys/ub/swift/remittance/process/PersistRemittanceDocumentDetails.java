package com.misys.ub.swift.remittance.process;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.swift.remittance.dao.DocumentUploadDao;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRs;
import bf.com.misys.cbs.types.header.RsHeader;

public class PersistRemittanceDocumentDetails  implements Command {
	private static final transient Log LOGGER = LogFactory.getLog(PersistRemittanceDocumentDetails.class.getName());

	@Override
	public boolean execute(Context context) throws Exception {
		if (LOGGER.isInfoEnabled())
			LOGGER.info("START PersistRemittanceDocumentData");

		boolean endofChain = Boolean.FALSE;

		SwiftRemittanceRs swtRemitterResp = (SwiftRemittanceRs) context.get("swtRemitterResp");
		SwiftRemittanceRq swtRemittanceReq = (SwiftRemittanceRq) context.get("swtRemitanceReq");
		RsHeader rsHeader = new RsHeader();
		DocumentUploadDao documentUploadDao = new DocumentUploadDao();
		rsHeader = documentUploadDao.insertDocumentDetails(swtRemittanceReq.getInitiateSwiftMessageRqDtls().getDocumentUpload(), 
				swtRemitterResp.getInitiateSwiftMessageRsDtls().getMessageId(), swtRemittanceReq.getRqHeader().getOrig().getChannelId()); 

		if (rsHeader.getStatus().getOverallStatus().equals(PaymentSwiftConstants.ERROR_STATUS)) {
			endofChain = Boolean.TRUE;
		}
		if (LOGGER.isInfoEnabled())
            LOGGER.info("END PersistRemittanceDocumentData  ");
		return endofChain;
	}



}
