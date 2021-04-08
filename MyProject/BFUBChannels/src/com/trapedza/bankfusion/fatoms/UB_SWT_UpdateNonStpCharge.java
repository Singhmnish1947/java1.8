package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.charges.ChargeConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_NONSTPCHARGE;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_UpdateNonStpCharge;


/**
 * @author Machamma.Devaiah
 *
 */
public class UB_SWT_UpdateNonStpCharge extends AbstractUB_SWT_UpdateNonStpCharge {
	private static final long serialVersionUID = 1L;
	private transient final static Log LOGGER = LogFactory.getLog(UB_SWT_UpdateNonStpCharge.class.getName());
	private static String NONSTP_CHARGE_WHERE_CLAUSE = " WHERE " + IBOUB_SWT_NONSTPCHARGE.UBMESSAGEID + " = ? " + " AND " + IBOUB_SWT_NONSTPCHARGE.UBCHARGECODEID + " = ? ";

	/**
	 * @param env
	 */
	public UB_SWT_UpdateNonStpCharge(BankFusionEnvironment env) {
		super(env);
	}

	/* (non-Javadoc)
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_UpdateNonStpCharge#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void process(BankFusionEnvironment env) {
		VectorTable onlineChargesVector = getF_IN_ChargeVector();
		String messageId = getF_IN_messageId();
		int size = onlineChargesVector.size();
		HashMap<String, Object> chargeDetailsMap = null;
		for (int i = 0; i < size; i++) {
			chargeDetailsMap = onlineChargesVector.getRowTags(i);
			//(CHARGECODE:[T08])
			String chargeCode = (String) chargeDetailsMap.get(ChargeConstants.OCV_CHARGECODE);
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("START UB_SWT_UpdateNonStpCharge messageid::::" + messageId + "ChargeCodeid ::: " + chargeCode);
			}
			IBOUB_SWT_NONSTPCHARGE nonStpChargeDtls = getNonStpCharge(messageId, chargeCode);
			if (nonStpChargeDtls != null) {
				//(CALCULATEDCHARGEAMOUNT:[5.00])  default amount
				//(ORIGINAL_CHARGEAMOUNT:[5.000000])
				nonStpChargeDtls.setF_UBORIGINALCHGAMT((BigDecimal) chargeDetailsMap.get(ChargeConstants.OCV_CALCULATEDCHARGEAMOUNT));
				//(CHARGEAMOUNT:[15.00])  amended charge amount
				nonStpChargeDtls.setF_UBCHARGEAMOUNT((BigDecimal) chargeDetailsMap.get(ChargeConstants.OCV_CHARGEAMOUNT));
				//(CHARGEAMOUNT_IN_FUND_ACC_CURRENCY:[15.00])
				nonStpChargeDtls.setF_UBCHARGEAMTINFUNDINGACCTCCY((BigDecimal) chargeDetailsMap.get(ChargeConstants.OCV_CHARGEAMOUNT_IN_FUND_ACC_CURRENCY));
				//(CHARGEAMOUNT_IN_ACC_CURRENCY:[15.00])
				nonStpChargeDtls.setF_UBCHARGEAMTINACCTCCY((BigDecimal) chargeDetailsMap.get(ChargeConstants.OCV_CHARGEAMOUNT_IN_ACC_CURRENCY));
				//(CHARGEAMOUNT_IN_TXN_CURRENCY:[15.00])
				
				
				//tax update 
				//(TAXAMOUNT:[2.50])
				nonStpChargeDtls.setF_UBTAXAMOUNT((BigDecimal) chargeDetailsMap.get(ChargeConstants.OCV_TAXAMOUNT));
				//(TAXAMOUNT_IN_ACC_CURRENCY:[2.50])
				nonStpChargeDtls.setF_UBTAXAMTINACCTCCY((BigDecimal) chargeDetailsMap.get(ChargeConstants.OCV_TAXAMOUNT_IN_ACC_CURRENCY));
				//(TAXAMOUNT_IN_FUND_ACC_CURRENCY:[2.50])
				nonStpChargeDtls.setF_TAXAMTINFUNDINGACCTCCY((BigDecimal) chargeDetailsMap.get(ChargeConstants.OCV_TAXAMOUNT_IN_FUND_ACC_CURRENCY));
				
				
			}
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("END UB_SWT_UpdateNonStpCharge messageid::::");
			}
		}
	}

	/**
	 * @param messageId
	 * @param chargeCode
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private IBOUB_SWT_NONSTPCHARGE getNonStpCharge(String messageId, String chargeCode) {
		ArrayList queryParams = new ArrayList();
		queryParams.add(messageId);
		queryParams.add(chargeCode);
		IBOUB_SWT_NONSTPCHARGE nonstpChargeDetails = (IBOUB_SWT_NONSTPCHARGE) BankFusionThreadLocal.getPersistanceFactory().findFirstByQuery(IBOUB_SWT_NONSTPCHARGE.BONAME, NONSTP_CHARGE_WHERE_CLAUSE, queryParams, false);
		return nonstpChargeDetails;
	}
}
