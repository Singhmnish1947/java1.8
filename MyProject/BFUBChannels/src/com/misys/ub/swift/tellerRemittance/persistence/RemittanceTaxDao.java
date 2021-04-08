package com.misys.ub.swift.tellerRemittance.persistence;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.BankFusionException;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.fbe.common.constant.QueryConstants;
import com.misys.fbe.common.util.CommonUtil;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_RemittanceTax;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.utils.GUIDGen;

import bf.com.misys.cbs.msgs.v1r0.TellerRemittanceRq;
import bf.com.misys.cbs.types.swift.TaxInformation;
import bf.com.misys.cbs.types.swift.TxnfeesInformation;

public class RemittanceTaxDao {

	private transient final static Log LOGGER = LogFactory.getLog(RemittanceTaxDao.class.getName());

	public static final String QUERY_TO_FIND_BY_REMITTANCEID = QueryConstants.WHERE
			+ IBOUB_SWT_RemittanceTax.UBREMITTANCEID + QueryConstants.EQUALS_PARAM;

	public static void insertData(TellerRemittanceRq remittanceRq) {
		LOGGER.info("INSERT into UBTB_REMITTANCETAX");
		if (remittanceRq.getTxnfeesInformation() != null) {
			TxnfeesInformation txnfeesInformation = remittanceRq.getTxnfeesInformation();
			TaxInformation[] vTaxInformationArray = txnfeesInformation.getTaxInformation();
			for (TaxInformation tax : vTaxInformationArray) {
				IBOUB_SWT_RemittanceTax remittanceTax = (IBOUB_SWT_RemittanceTax) BankFusionThreadLocal
						.getPersistanceFactory().getStatelessNewInstance(IBOUB_SWT_RemittanceTax.BONAME);
				remittanceTax.setBoID(GUIDGen.getNewGUID());
				remittanceTax.setF_UBTAXAMOUNT(tax.getTaxAmount().getAmount());
				remittanceTax.setF_UBTAXCURRENCY(tax.getTaxAmount().getIsoCurrencyCode());
				remittanceTax.setF_UBTAXDESCRIPTION(tax.getDescription());
				remittanceTax.setF_UBTAXPERCENTAGE(tax.getTaxPercentage());
				remittanceTax.setF_UBLASTUPDATETIME(SystemInformationManager.getInstance().getBFBusinessDateTime());
				remittanceTax.setF_UBREMITTANCEID(remittanceRq.getTxnAdditionalDtls().getRemittanceId());

				// create
				try {
					BankFusionThreadLocal.getPersistanceFactory().create(IBOUB_SWT_RemittanceTax.BONAME, remittanceTax);
				} catch (BankFusionException e) {
					LOGGER.error("Error Message during insertion into UBTB_REMITTANCETAX "
							+ ExceptionUtil.getExceptionAsString(e));
					CommonUtil.handleParameterizedEvent(Integer.parseInt("20600092"),
							new String[] { "UBTB_REMITTANCETAX" });
				}
			}
		}
	}

	public static List<IBOUB_SWT_RemittanceTax> findByRemittanceId(String remittanceId) {
		ArrayList params = new ArrayList();
		params.add(remittanceId);
		List<IBOUB_SWT_RemittanceTax> result = BankFusionThreadLocal.getPersistanceFactory()
				.findByQuery(IBOUB_SWT_RemittanceTax.BONAME, QUERY_TO_FIND_BY_REMITTANCEID, params, null, true);

		return result;
	}

}
