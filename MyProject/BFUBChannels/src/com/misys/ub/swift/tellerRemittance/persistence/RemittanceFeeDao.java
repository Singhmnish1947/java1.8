package com.misys.ub.swift.tellerRemittance.persistence;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.BankFusionException;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.fbe.common.constant.QueryConstants;
import com.misys.fbe.common.util.CommonUtil;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_RemittanceFee;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.utils.GUIDGen;

import bf.com.misys.cbs.msgs.v1r0.TellerRemittanceRq;
import bf.com.misys.cbs.types.swift.FeesInformation;
import bf.com.misys.cbs.types.swift.TxnfeesInformation;

public class RemittanceFeeDao {

	private transient final static Log LOGGER = LogFactory.getLog(RemittanceFeeDao.class.getName());

	public static final String QUERY_TO_FIND_BY_REMITTANCEID = QueryConstants.WHERE
			+ IBOUB_SWT_RemittanceFee.UBREMITTANCEID + QueryConstants.EQUALS_PARAM;

	public static void insertData(TellerRemittanceRq remittanceRq) {
		LOGGER.info("INSERT into UBTB_REMITTANCEFEE");
		if (remittanceRq.getTxnfeesInformation() != null) {
			TxnfeesInformation txnfeesInformation = remittanceRq.getTxnfeesInformation();
			FeesInformation[] vFeesInformationArray = txnfeesInformation.getFeesInformation();
			for (FeesInformation fee : vFeesInformationArray) {
				IBOUB_SWT_RemittanceFee remittanceFee = (IBOUB_SWT_RemittanceFee) BankFusionThreadLocal
						.getPersistanceFactory().getStatelessNewInstance(IBOUB_SWT_RemittanceFee.BONAME);
				remittanceFee.setBoID(GUIDGen.getNewGUID());
				remittanceFee.setF_UBFEEAMOUNT(fee.getFeeAmount().getAmount());
				remittanceFee.setF_UBFEECURRENCY(fee.getFeeAmount().getIsoCurrencyCode());
				remittanceFee.setF_UBFEENAME(
						StringUtils.isNotBlank(fee.getFeeName()) ? fee.getFeeName() : StringUtils.EMPTY);
				remittanceFee.setF_UBFEECATEGORY(
						StringUtils.isNotBlank(fee.getFeeCategory()) ? fee.getFeeCategory() : StringUtils.EMPTY);
				remittanceFee.setF_UBLASTUPDATETIME(SystemInformationManager.getInstance().getBFBusinessDateTime());
				remittanceFee.setF_UBREMITTANCEID(remittanceRq.getTxnAdditionalDtls().getRemittanceId());

				// create
				try {
					BankFusionThreadLocal.getPersistanceFactory().create(IBOUB_SWT_RemittanceFee.BONAME, remittanceFee);
				} catch (BankFusionException e) {
					LOGGER.error("Error Message during insertion into UBTB_REMITTANCEFEE "
							+ ExceptionUtil.getExceptionAsString(e));
					CommonUtil.handleParameterizedEvent(Integer.parseInt("20600092"),
							new String[] { "UBTB_REMITTANCEFEE" });
				}
			}
		}
	}

	public static List<IBOUB_SWT_RemittanceFee> findByRemittanceId(String remittanceId) {
		ArrayList params = new ArrayList();
		params.add(remittanceId);
		List<IBOUB_SWT_RemittanceFee> result = BankFusionThreadLocal.getPersistanceFactory()
				.findByQuery(IBOUB_SWT_RemittanceFee.BONAME, QUERY_TO_FIND_BY_REMITTANCEID, params, null, true);

		return result;
	}

}
