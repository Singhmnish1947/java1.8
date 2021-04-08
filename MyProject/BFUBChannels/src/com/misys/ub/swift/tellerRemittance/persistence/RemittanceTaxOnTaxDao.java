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
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_RemittanceTaxOnTax;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.utils.GUIDGen;

import bf.com.misys.cbs.msgs.v1r0.TellerRemittanceRq;
import bf.com.misys.cbs.types.swift.TaxOnTaxInformation;
import bf.com.misys.cbs.types.swift.TxnfeesInformation;

public class RemittanceTaxOnTaxDao {

	private transient final static Log LOGGER = LogFactory.getLog(RemittanceTaxOnTaxDao.class.getName());

	public static final String QUERY_TO_FIND_BY_REMITTANCEID = QueryConstants.WHERE
			+ IBOUB_SWT_RemittanceTaxOnTax.UBREMITTANCEID + QueryConstants.EQUALS_PARAM;

	public static void insertData(TellerRemittanceRq remittanceRq) {
		LOGGER.info("INSERT into UBTB_REMITTANCETAXONTAX");
		if (remittanceRq.getTxnfeesInformation() != null) {
			TxnfeesInformation txnfeesInformation = remittanceRq.getTxnfeesInformation();
			TaxOnTaxInformation[] vTaxOnTaxInformationArray = txnfeesInformation.getTaxOnTaxInformation();
			for (TaxOnTaxInformation tax : vTaxOnTaxInformationArray) {
				IBOUB_SWT_RemittanceTaxOnTax remittanceTax = (IBOUB_SWT_RemittanceTaxOnTax) BankFusionThreadLocal
						.getPersistanceFactory().getStatelessNewInstance(IBOUB_SWT_RemittanceTaxOnTax.BONAME);
				remittanceTax.setBoID(GUIDGen.getNewGUID());
				remittanceTax.setF_UBTAXONTAXAMOUNT(tax.getTaxOnTaxAmount().getAmount());
				remittanceTax.setF_UBTAXONTAXCURRENCY(tax.getTaxOnTaxAmount().getIsoCurrencyCode());
				remittanceTax.setF_UBTAXONTAXDESCRIPTION(tax.getDescription());
				remittanceTax.setF_UBTAXONTAXPERCENTAGE(tax.getTaxOnTaxPercentage());
				remittanceTax.setF_UBLASTUPDATETIME(SystemInformationManager.getInstance().getBFBusinessDateTime());
				remittanceTax.setF_UBREMITTANCEID(remittanceRq.getTxnAdditionalDtls().getRemittanceId());

				// create
				try {
					BankFusionThreadLocal.getPersistanceFactory().create(IBOUB_SWT_RemittanceTaxOnTax.BONAME,
							remittanceTax);
				} catch (BankFusionException e) {
					LOGGER.error("Error Message during insertion into UBTB_REMITTANCETAXONTAX "
							+ ExceptionUtil.getExceptionAsString(e));
					CommonUtil.handleParameterizedEvent(Integer.parseInt("20600092"),
							new String[] { "UBTB_REMITTANCETAXONTAX" });
				}
			}
		}
	}

	public static List<IBOUB_SWT_RemittanceTaxOnTax> findByRemittanceId(String remittanceId) {
		ArrayList params = new ArrayList();
		params.add(remittanceId);
		List<IBOUB_SWT_RemittanceTaxOnTax> result = BankFusionThreadLocal.getPersistanceFactory()
				.findByQuery(IBOUB_SWT_RemittanceTaxOnTax.BONAME, QUERY_TO_FIND_BY_REMITTANCEID, params, null, true);

		return result;
	}

}
