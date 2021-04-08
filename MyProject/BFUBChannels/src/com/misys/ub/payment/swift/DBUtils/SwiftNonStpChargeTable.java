package com.misys.ub.payment.swift.DBUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.types.Charges;

import com.misys.bankfusion.subsystem.persistence.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.bo.refimpl.IBOTaxRates;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_NONSTPCHARGE;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.utils.GUIDGen;

/**
 * @author machamma.devaiah
 *
 */
public class SwiftNonStpChargeTable {
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;
	private IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
	private static final transient Log LOGGER = LogFactory.getLog(SwiftNonStpChargeTable.class.getName());
	public static final String NONSTP_CHARGE_LIST_LOOKUPSRV = "UB_SWT_ListNonStpCharge_SRV";

	/**
	 * @param txnCharges
	 * @param messageId
	 * @param isNonStp
	 */
	public void insertSwtNonStpCharge(Charges[] txnCharges, String messageId, boolean isNonStp) {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("insertSwtNonStpCharge:::" + messageId);
		}
		if (isNonStp) {
			for (int i = 0, n = txnCharges.length; i < n; i++) {
				Charges charges = txnCharges[i];
				if (charges != null) {
					IBOUB_SWT_NONSTPCHARGE nonStpCharge = (IBOUB_SWT_NONSTPCHARGE) factory.getStatelessNewInstance(IBOUB_SWT_NONSTPCHARGE.BONAME);
					nonStpCharge.setBoID(GUIDGen.getNewGUID());
					nonStpCharge.setF_UBMESSAGEID(messageId);
					nonStpCharge.setF_UBCHARGEAMOUNT(charges.getCharge().getChargeCcyAmtDetails().getAmount());
					nonStpCharge.setF_UBCHARGECURRENCYCODE(charges.getCharge().getChargeCcyAmtDetails().getIsoCurrencyCode());
					nonStpCharge.setF_UBCHARGECALCCODE(charges.getCharge().getChargeCalculationCode() != null ? charges.getCharge().getChargeCalculationCode() : StringUtils.EMPTY);
					nonStpCharge.setF_UBCHARGECODEID(charges.getCharge().getChargeCode() != null ? charges.getCharge().getChargeCode() : StringUtils.EMPTY);
					nonStpCharge.setF_UBCHARGEDESCRIPTION(charges.getCharge().getChargeNarrative() != null ? charges.getCharge().getChargeNarrative() : StringUtils.EMPTY);
					nonStpCharge.setF_UBCHARGENARRATIVE(charges.getCharge().getChargeNarrative() != null ? charges.getCharge().getChargeNarrative() : StringUtils.EMPTY);
					nonStpCharge.setF_UBCHARGERECEIVINGACCOUNTID(charges.getCharge().getChargeRecAcctDetails().getStandardAccountId());
					nonStpCharge.setF_UBCHARGEPOSTINGTXNCODE(charges.getCharge().getChargePostingTxnCode());
					//charge amt in account ccy code
					nonStpCharge.setF_UBCHARGEAMTINACCTCCY(charges.getCharge().getChargeCcyAmtDetails().getAmount() != null ? charges.getCharge().getChargeCcyAmtDetails().getAmount() : new BigDecimal(0));
					nonStpCharge.setF_UBCHARGEAMTINACCTCCYCODE(charges.getCharge().getChargeCcyAmtDetails().getIsoCurrencyCode());
					//charge amount in funding account ccy
					nonStpCharge.setF_UBCHARGEAMTINFUNDINGACCTCCY(charges.getCharge().getFundingAcctCcyDetails().getAmount()!= null ? charges.getCharge().getFundingAcctCcyDetails().getAmount() : new BigDecimal(0));
					nonStpCharge.setF_UBCHARGEFUNDINGACCTIDCCYCODE(charges.getCharge().getFundingAcctCcyDetails().getIsoCurrencyCode());
					nonStpCharge.setF_UBCHARGEEXCHANGERATETYPE(charges.getCharge().getChargeExRateDetails().getExchangeRateType() != null ? charges.getCharge().getChargeExRateDetails().getExchangeRateType() : "SPOT");
					nonStpCharge.setF_UBCHARGEEXCHANGERATE(charges.getCharge().getChargeExRateDetails().getExchangeRate() != null ? charges.getCharge().getChargeExRateDetails().getExchangeRate() : BigDecimal.ONE);
					nonStpCharge.setF_UBCHARGEFUNDINGACCTID(charges.getCharge().getFundingAccount().getStandardAccountId() != null ? charges.getCharge().getFundingAccount().getStandardAccountId() : StringUtils.EMPTY);
					nonStpCharge.setF_UBCHARGEFUNDINGACCTIDCCYCODE(charges.getCharge().getFundingAcctCcyDetails().getIsoCurrencyCode() != null ? charges.getCharge().getFundingAcctCcyDetails().getIsoCurrencyCode() : StringUtils.EMPTY);
					nonStpCharge.setF_UBTAXAMOUNT(charges.getCharge().getTaxCcyAmtDetails().getAmount() != null ? charges.getCharge().getTaxCcyAmtDetails().getAmount() : BigDecimal.ZERO);
					nonStpCharge.setF_UBTAXCURRENCYCODE(!StringUtils.isBlank(charges.getCharge().getTaxCcyAmtDetails().getIsoCurrencyCode()) ? charges.getCharge().getTaxCcyAmtDetails().getIsoCurrencyCode() : charges.getCharge().getFundingAcctCcyDetails().getIsoCurrencyCode());
					nonStpCharge.setF_UBTAXCODE(charges.getCharge().getTaxCode() != null ? charges.getCharge().getTaxCode() : StringUtils.EMPTY);
					nonStpCharge.setF_UBTAXPOSTINGTXNCODE(charges.getCharge().getTaxTxnCode() != null ? charges.getCharge().getTaxTxnCode() : StringUtils.EMPTY);
					nonStpCharge.setF_UBTAXRECEIVINGACCOUNTID(charges.getCharge().getTaxRecAcct().getStandardAccountId() != null ? charges.getCharge().getTaxRecAcct().getStandardAccountId() : StringUtils.EMPTY);
					nonStpCharge.setF_UBTAXDESCRIPTION(getTaxDesciption(charges.getCharge().getTaxCode()));
					nonStpCharge.setF_UBTAXNARRATIVE(charges.getCharge().getTaxNarrative() != null ? charges.getCharge().getTaxNarrative() : StringUtils.EMPTY);
					nonStpCharge.setF_UBTAXEXCHANGERATE(charges.getCharge().getTaxExchangeRateDetails().getExchangeRate() != null ? charges.getCharge().getTaxExchangeRateDetails().getExchangeRate() : BigDecimal.ONE);
					nonStpCharge.setF_UBTAXEXCHANGERATETYPE(charges.getCharge().getTaxExchangeRateDetails().getExchangeRateType() != null ? charges.getCharge().getTaxExchangeRateDetails().getExchangeRateType() : "SPOT");
					nonStpCharge.setF_UBTAXAMTINACCTCCY(charges.getCharge().getTaxFndAcctAmtDetails().getAmount() != null ? charges.getCharge().getTaxFndAcctAmtDetails().getAmount() : BigDecimal.ZERO);
					nonStpCharge.setF_UBTAXAMTINACCTCCYCODE(!StringUtils.isBlank(charges.getCharge().getTaxFndAcctAmtDetails().getIsoCurrencyCode()) ? charges.getCharge().getTaxFndAcctAmtDetails().getIsoCurrencyCode() : charges.getCharge().getFundingAcctCcyDetails().getIsoCurrencyCode());
					nonStpCharge.setF_TAXAMTINFUNDINGACCTCCY(charges.getCharge().getTaxFndAcctAmtDetails().getAmount() != null ? charges.getCharge().getTaxFndAcctAmtDetails().getAmount() : BigDecimal.ZERO);
					nonStpCharge.setF_UBTAXAMTINFUNDINGACCTCCYCODE(!StringUtils.isBlank(charges.getCharge().getTaxFndAcctAmtDetails().getIsoCurrencyCode()) ? charges.getCharge().getTaxFndAcctAmtDetails().getIsoCurrencyCode() : charges.getCharge().getFundingAcctCcyDetails().getIsoCurrencyCode());
					nonStpCharge.setF_UBISCHARGEWAIVED("N");
					nonStpCharge.setF_UBISNETTINGALLOWED("N");
					nonStpCharge.setF_UBISAMENDMENTALLOWED("Y");
					factory.create(IBOUB_SWT_NONSTPCHARGE.BONAME, nonStpCharge);
				}
			}
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("END of insertSwtNonStpCharge:::");
			}
		}
	}

	/**
	 * @param messageId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public VectorTable listChargeByMessageId(String messageId) {
		Map<String, Object> inputParams = new HashMap<String, Object>();
		inputParams.put("messageId", messageId);
		Map<?, ?> outputParams = MFExecuter.executeMF(NONSTP_CHARGE_LIST_LOOKUPSRV, BankFusionThreadLocal.getBankFusionEnvironment(), inputParams);
		VectorTable vectorList = (VectorTable) outputParams.get("NONSTPCHARGE");
		return vectorList;
	}
	
	/**
	 * @param taxRateId
	 * @return
	 */
	private String getTaxDesciption(String taxRateId) {
		String taxDescription = CommonConstants.EMPTY_STRING;
		IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
		IBOTaxRates taxRateBO = (IBOTaxRates) factory.findByPrimaryKey(IBOTaxRates.BONAME, taxRateId, true);
		if (taxRateBO != null) {
			taxDescription = taxRateBO.getF_DESCRIPTION();
		}
		return taxDescription;
	}
}
