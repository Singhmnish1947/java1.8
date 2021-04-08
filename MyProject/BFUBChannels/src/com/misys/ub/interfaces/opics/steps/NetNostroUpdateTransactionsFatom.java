/* **********************************************************
 * Copyright (c) 2009 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ************************************************************************
 * Modification History
 * ************************************************************************
 * $Id: NetNostroUpdateTransactionsFatom.java,v 1.0 2009/05/08 abdrahim Exp $
 *
 */
package com.misys.ub.interfaces.opics.steps;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.bo.refimpl.IBOUB_OPX_NostroUpdateTransaction;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_OPX_NetNostroUpdateTransactionsFatom;

/**
 * 
 * @AUTHOR Abdul Rahim
 * @PROJECT Universal Banking
 * @description This will net amount of all records with same Nostro Account Id and Value Date from
 *              BO UB_OPX_NostroUpdateTransaction and return list of record of type Vector
 *              netAmountVec.
 */

public class NetNostroUpdateTransactionsFatom extends AbstractUB_OPX_NetNostroUpdateTransactionsFatom {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}



    /** The Constant serialVersionUID. */
    public static final long serialVersionUID = 1;

    /** For logging/debug/error message. */
    private transient final static Log logger = LogFactory.getLog(NetNostroUpdateTransactionsFatom.class.getName());


    private static final String NET_NOSTRO_AMOUNT_QUERY = CommonConstants.SELECT + CommonConstants.SPACE + "T1."
            + IBOUB_OPX_NostroUpdateTransaction.NOSTROACCOUNTID + " AS " + IBOUB_OPX_NostroUpdateTransaction.NOSTROACCOUNTID
            + CommonConstants.COMMA + "T1." + IBOUB_OPX_NostroUpdateTransaction.VALUEDT + " AS "
            + IBOUB_OPX_NostroUpdateTransaction.VALUEDT + CommonConstants.COMMA + "SUM(" + "T1."
            + IBOUB_OPX_NostroUpdateTransaction.AMOUNT + ")" + " AS " + IBOUB_OPX_NostroUpdateTransaction.AMOUNT
            + CommonConstants.SPACE + CommonConstants.FROM + CommonConstants.SPACE + IBOUB_OPX_NostroUpdateTransaction.BONAME
            + " T1 " + CommonConstants.WHERE + "T1." + IBOUB_OPX_NostroUpdateTransaction.STATUS + CommonConstants.EQUAL + "'U'"
            + " GROUP BY " + IBOUB_OPX_NostroUpdateTransaction.NOSTROACCOUNTID + CommonConstants.COMMA
            + IBOUB_OPX_NostroUpdateTransaction.VALUEDT;

    /**
     * @param env
     */
    public NetNostroUpdateTransactionsFatom(BankFusionEnvironment env) {
        super(env);
    }

    /**
     * This process will get all unprocessed transaction from INFTB_OPXNOSTROACCTUPDTXN table with
     * netted amount with same Nostro Account Id and Value Date.
     * 
     * @param env -
     *            Bankfusion environment
     */

    public void process(BankFusionEnvironment env) {

        List<SimplePersistentObject> netAmountList = BankFusionThreadLocal.getPersistanceFactory().executeGenericQuery(
                NET_NOSTRO_AMOUNT_QUERY, new ArrayList(), null, false);
        VectorTable netAmountVec = new VectorTable();

        // If there is netted amount list, create Vector
        if (netAmountList != null) {
            for (SimplePersistentObject lSimplePersistentObject : netAmountList) {
                Map<String, Object> dataMap = lSimplePersistentObject.getDataMap();
                String nostroAccountID = (String) dataMap.get(IBOUB_OPX_NostroUpdateTransaction.NOSTROACCOUNTID);
                Date valueDate = (Date) dataMap.get(IBOUB_OPX_NostroUpdateTransaction.VALUEDT);
                BigDecimal amount = (BigDecimal) dataMap.get(IBOUB_OPX_NostroUpdateTransaction.AMOUNT);

                Map writeMap = new HashMap();
                writeMap.put("NOSTROACCOUNTID", nostroAccountID);
                writeMap.put("VALUEDATE", valueDate);
                writeMap.put("AMOUNT", amount);

                netAmountVec.addAll(new VectorTable(writeMap));
            }
        }
        setF_OUT_netAmountVec(netAmountVec);
    }
}
