package com.misys.ub.cc.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.util.BankFusionIOSupport;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_CCPAYMENTREQUEST;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

public class CCFinancialResponsePrep {

	private static final String MESSAGEQUERY = " WHERE " + IBOUB_INF_CCPAYMENTREQUEST.CHANNELREF + "=? " + "AND "
			+ IBOUB_INF_CCPAYMENTREQUEST.CHANNEL + "= ? ";
	private static final String CHANNEL = "CCI";
	private static final transient Log LOGGER = LogFactory.getLog(CCFinancialResponsePrep.class.getName());

	public String prepareResponse(String essenceResponse, String channelRef) {

		String regPayload = getReqPayload(channelRef);
		return essenceResponse.replace("REQUEST_PAYLOAD", StringEscapeUtils.escapeXml(regPayload));
	}

	private String getReqPayload(String channelRef) {
		String reqPayload = "REQUEST_PAYLOAD";
		IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
		ArrayList params = new ArrayList();
		params.add(channelRef);
		params.add(CHANNEL);

		List<IBOUB_INF_CCPAYMENTREQUEST> ccRequestList = factory.findByQuery(IBOUB_INF_CCPAYMENTREQUEST.BONAME,
				MESSAGEQUERY.toString(), params, null, true);

		if (ccRequestList!= null && ccRequestList.size() > 0) {
			IBOUB_INF_CCPAYMENTREQUEST ccReq = ccRequestList.get(0);
			reqPayload = (String) BankFusionIOSupport.convertFromBytes(ccReq.getF_PAYMENTREQDATA());
		}
		LOGGER.info("Request PayLoad ::: " + reqPayload);
		return reqPayload;
	}

}
