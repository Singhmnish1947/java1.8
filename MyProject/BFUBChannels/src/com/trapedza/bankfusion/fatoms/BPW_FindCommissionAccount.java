/* ********************************************************************************
 *  Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 *
 */
package com.trapedza.bankfusion.fatoms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractBPW_FindCommissionAccount;
import com.trapedza.bankfusion.steps.refimpl.IBPW_FindCommissionAccount;

public class BPW_FindCommissionAccount extends AbstractBPW_FindCommissionAccount implements IBPW_FindCommissionAccount {
	public BPW_FindCommissionAccount(BankFusionEnvironment env) {
		super(env);
	}

	private transient final static Log logger = LogFactory.getLog(BPW_FindCommissionAccount.class.getName());
	public static final String cvsRevision = "$Revision: 1.5.4.1 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(cvsRevision);
	}

	public void process(BankFusionEnvironment env) {
		String CommisCode1 = getF_IN_ComCode1();
		String CommisCode2 = getF_IN_ComCode2();
		String CommisCode3 = getF_IN_ComCode3();
		String CommisCode4 = getF_IN_ComCode4();
		String CommisCode5 = getF_IN_ComCode5();
		String CommisCode6 = getF_IN_ComCode6();
		String brchSortCode = getF_IN_BranchSortCode();
		String CurrencyCode = getF_IN_CurrencyCode();
		CalculateOnlineChargesFatom calcOnlineCharge = null;
		brchSortCode = brchSortCode.substring(2, 10);
		if (!CommisCode1.equals(CommonConstants.EMPTY_STRING) && !CommisCode1.equals("00")) {
			calcOnlineCharge = new CalculateOnlineChargesFatom(env);
			calcOnlineCharge.setF_IN_CHARGECODE(CommisCode1);
			calcOnlineCharge.setF_IN_BRANCHSORTCODE(brchSortCode);
			calcOnlineCharge.setF_IN_TRANSACTIONCURRENCY(CurrencyCode);
			calcOnlineCharge.setF_IN_WALKINCUSTOMER(new Boolean(true));
			calcOnlineCharge.setF_IN_BRANCHSORTCODE_FOR_CHG_REC_ACC(brchSortCode);
			calcOnlineCharge.process(env);
			VectorTable chargeResult = calcOnlineCharge.getF_OUT_RESULT();
			Object Accobj[] = chargeResult.getColumn("CHARGERECIEVINGACCOUNT");
			logger.info("CHARGERECEIVINGACCOUNTID" + Accobj[0].toString());
			setF_OUT_CommissionAcc1(Accobj[0].toString());
			Object Accobj1[] = chargeResult.getColumn("CHARGENARRATIVE");
			logger.info("CHARGENARRATIVE" + Accobj1[0].toString());
			String commNarr = Accobj1[0].toString();
			commNarr = commNarr.concat(rightPad(" ", 25-commNarr.length()));
			setF_OUT_Narrative1(commNarr);

		}
		if (!CommisCode2.equals(CommonConstants.EMPTY_STRING) && !CommisCode2.equals("00")) {
			calcOnlineCharge = new CalculateOnlineChargesFatom(env);
			calcOnlineCharge.setF_IN_CHARGECODE(CommisCode2);
			calcOnlineCharge.setF_IN_BRANCHSORTCODE(brchSortCode);
			calcOnlineCharge.setF_IN_TRANSACTIONCURRENCY(CurrencyCode);
			calcOnlineCharge.setF_IN_WALKINCUSTOMER(new Boolean(true));
			calcOnlineCharge.setF_IN_BRANCHSORTCODE_FOR_CHG_REC_ACC(brchSortCode);
			calcOnlineCharge.process(env);
			VectorTable chargeResult = calcOnlineCharge.getF_OUT_RESULT();
			Object Accobj[] = chargeResult.getColumn("CHARGERECIEVINGACCOUNT");
			logger.info("CHARGERECEIVINGACCOUNTID" + Accobj[0].toString());
			setF_OUT_CommissionAcc2(Accobj[0].toString());
			Object Accobj1[] = chargeResult.getColumn("CHARGENARRATIVE");
			logger.info("CHARGENARRATIVE" + Accobj1[0].toString());
			String commNarr = Accobj1[0].toString();
			commNarr = commNarr.concat(rightPad(" ", 25-commNarr.length()));
			setF_OUT_Narrative2(commNarr);
		}
		if (!CommisCode3.equals(CommonConstants.EMPTY_STRING) && !CommisCode3.equals("00")) {
			calcOnlineCharge = new CalculateOnlineChargesFatom(env);
			calcOnlineCharge.setF_IN_CHARGECODE(CommisCode3);
			calcOnlineCharge.setF_IN_BRANCHSORTCODE(brchSortCode);
			calcOnlineCharge.setF_IN_TRANSACTIONCURRENCY(CurrencyCode);
			calcOnlineCharge.setF_IN_WALKINCUSTOMER(new Boolean(true));
			calcOnlineCharge.setF_IN_BRANCHSORTCODE_FOR_CHG_REC_ACC(brchSortCode);
			calcOnlineCharge.process(env);
			VectorTable chargeResult = calcOnlineCharge.getF_OUT_RESULT();
			Object Accobj[] = chargeResult.getColumn("CHARGERECIEVINGACCOUNT");
			logger.info("CHARGERECEIVINGACCOUNTID" + Accobj[0].toString());
			setF_OUT_CommissionAcc3(Accobj[0].toString());
			Object Accobj1[] = chargeResult.getColumn("CHARGENARRATIVE");
			logger.info("CHARGENARRATIVE" + Accobj1[0].toString());
			String commNarr = Accobj1[0].toString();
			commNarr = commNarr.concat(rightPad(" ", 25-commNarr.length()));
			setF_OUT_Narrative3(commNarr);
		}
		if (!CommisCode4.equals(CommonConstants.EMPTY_STRING) && !CommisCode4.equals("00")) {
			calcOnlineCharge = new CalculateOnlineChargesFatom(env);
			calcOnlineCharge.setF_IN_CHARGECODE(CommisCode4);
			calcOnlineCharge.setF_IN_BRANCHSORTCODE(brchSortCode);
			calcOnlineCharge.setF_IN_TRANSACTIONCURRENCY(CurrencyCode);
			calcOnlineCharge.setF_IN_WALKINCUSTOMER(new Boolean(true));
			calcOnlineCharge.setF_IN_BRANCHSORTCODE_FOR_CHG_REC_ACC(brchSortCode);
			calcOnlineCharge.process(env);
			VectorTable chargeResult = calcOnlineCharge.getF_OUT_RESULT();
			Object Accobj[] = chargeResult.getColumn("CHARGERECIEVINGACCOUNT");
			logger.info("CHARGERECEIVINGACCOUNTID" + Accobj[0].toString());
			setF_OUT_CommissionAcc4(Accobj[0].toString());
			Object Accobj1[] = chargeResult.getColumn("CHARGENARRATIVE");
			logger.info("CHARGENARRATIVE" + Accobj1[0].toString());
			String commNarr = Accobj1[0].toString();
			commNarr = commNarr.concat(rightPad(" ", 25-commNarr.length()));
			setF_OUT_Narrative4(commNarr);
		}
		if (!CommisCode5.equals(CommonConstants.EMPTY_STRING) && !CommisCode5.equals("00")) {
			calcOnlineCharge = new CalculateOnlineChargesFatom(env);
			calcOnlineCharge.setF_IN_CHARGECODE(CommisCode5);
			calcOnlineCharge.setF_IN_BRANCHSORTCODE(brchSortCode);
			calcOnlineCharge.setF_IN_TRANSACTIONCURRENCY(CurrencyCode);
			calcOnlineCharge.setF_IN_WALKINCUSTOMER(new Boolean(true));
			calcOnlineCharge.setF_IN_BRANCHSORTCODE_FOR_CHG_REC_ACC(brchSortCode);
			calcOnlineCharge.process(env);
			VectorTable chargeResult = calcOnlineCharge.getF_OUT_RESULT();
			Object Accobj[] = chargeResult.getColumn("CHARGERECIEVINGACCOUNT");
			logger.info("CHARGERECEIVINGACCOUNTID" + Accobj[0].toString());
			setF_OUT_CommissionAcc5(Accobj[0].toString());
			Object Accobj1[] = chargeResult.getColumn("CHARGENARRATIVE");
			logger.info("CHARGENARRATIVE" + Accobj1[0].toString());
			String commNarr = Accobj1[0].toString();
			commNarr = commNarr.concat(rightPad(" ", 25-commNarr.length()));
			setF_OUT_Narrative5(commNarr);
		}

		if (!CommisCode6.equals(CommonConstants.EMPTY_STRING) && !CommisCode6.equals("00")) {
			calcOnlineCharge = new CalculateOnlineChargesFatom(env);
			calcOnlineCharge.setF_IN_CHARGECODE(CommisCode6);
			calcOnlineCharge.setF_IN_BRANCHSORTCODE(brchSortCode);
			calcOnlineCharge.setF_IN_TRANSACTIONCURRENCY(CurrencyCode);
			calcOnlineCharge.setF_IN_WALKINCUSTOMER(new Boolean(true));
			calcOnlineCharge.setF_IN_BRANCHSORTCODE_FOR_CHG_REC_ACC(brchSortCode);
			calcOnlineCharge.process(env);
			VectorTable chargeResult = calcOnlineCharge.getF_OUT_RESULT();
			Object Accobj[] = chargeResult.getColumn("CHARGERECIEVINGACCOUNT");
			logger.info("CHARGERECEIVINGACCOUNTID" + Accobj[0].toString());
			setF_OUT_CommissionAcc6(Accobj[0].toString());
			Object Accobj1[] = chargeResult.getColumn("CHARGENARRATIVE");
			logger.info("CHARGENARRATIVE" + Accobj1[0].toString());
			String commNarr = Accobj1[0].toString();
			commNarr = commNarr.concat(rightPad(" ", 25-commNarr.length()));
			setF_OUT_Narrative6(commNarr);
		}
	}
	
    public static String rightPad(String s, int width) {
    	if(width<=0)
    	{
   		return s;
    	}
        return String.format("%-" + width + "s", s).replace(' ', ' ');

    }
}
