/* **********************************************************
 * Copyright (c) 2009 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ************************************************************************
 * Modification History
 * ************************************************************************
 * $Id: NetPositionUpdateTransactionsFatom.java,v 1.0 2009/05/08 Itesh Kumar Exp $
 *
 */
package com.misys.ub.interfaces.opics.steps;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.bo.refimpl.IBOUB_OPX_PositionUpdateTransaction;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_OPX_NetPositionUpdateTransactionsFatom;

/**
 * 
 * @AUTHOR Abdul Rahim
 * @PROJECT Universal Banking
 * @description This will net amount of all records with same Currency Code1, Currency Code 2
 *              Account Id and Value Date from BO UB_OPX_PositionUpdateTransaction and return list
 *              of record of type Vector PositionUpdateVector.
 */

public class NetPositionUpdateTransactionsFatom extends AbstractUB_OPX_NetPositionUpdateTransactionsFatom {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

    /**
     */

    private transient final static Log logger = LogFactory.getLog(NetPositionUpdateTransactionsFatom.class.getName());

    private static final String NET_POSITION_UPDATE_QUERY = CommonConstants.SELECT + CommonConstants.SPACE + "T1."
            + IBOUB_OPX_PositionUpdateTransaction.CURRENCYCODE1 + " AS " + IBOUB_OPX_PositionUpdateTransaction.CURRENCYCODE1
            + CommonConstants.COMMA + "T1." + IBOUB_OPX_PositionUpdateTransaction.CURRENCYCODE2 + " AS "
            + IBOUB_OPX_PositionUpdateTransaction.CURRENCYCODE2 + CommonConstants.COMMA + "T1."
            + IBOUB_OPX_PositionUpdateTransaction.VALUEDT + " AS " + IBOUB_OPX_PositionUpdateTransaction.VALUEDT
            + CommonConstants.COMMA + "T1." + IBOUB_OPX_PositionUpdateTransaction.DEALFLAG + " AS "
            + IBOUB_OPX_PositionUpdateTransaction.DEALFLAG + CommonConstants.COMMA + "SUM(" + "T1." + IBOUB_OPX_PositionUpdateTransaction.AMOUNT1 + ")" + " AS "
            + IBOUB_OPX_PositionUpdateTransaction.AMOUNT1 + CommonConstants.COMMA + "SUM(" + "T1."
            + IBOUB_OPX_PositionUpdateTransaction.AMOUNT2 + ")" + " AS " + IBOUB_OPX_PositionUpdateTransaction.AMOUNT2
            + CommonConstants.COMMA + "SUM(" + "T1." + IBOUB_OPX_PositionUpdateTransaction.BASEQUIVAMT + ")" + " AS "
            + IBOUB_OPX_PositionUpdateTransaction.BASEQUIVAMT + CommonConstants.SPACE + CommonConstants.FROM
            + CommonConstants.SPACE + IBOUB_OPX_PositionUpdateTransaction.BONAME + " T1 " + CommonConstants.WHERE + "T1."
            + IBOUB_OPX_PositionUpdateTransaction.STATUS + CommonConstants.EQUAL + "'U'" + " GROUP BY "
            + IBOUB_OPX_PositionUpdateTransaction.CURRENCYCODE1 + CommonConstants.COMMA
            + IBOUB_OPX_PositionUpdateTransaction.CURRENCYCODE2 + CommonConstants.COMMA
            + IBOUB_OPX_PositionUpdateTransaction.VALUEDT + CommonConstants.COMMA + IBOUB_OPX_PositionUpdateTransaction.DEALFLAG;

    /**
     * @param env
     */
    public NetPositionUpdateTransactionsFatom(BankFusionEnvironment env) {
        super(env);
    }

    /**
     * This will net amount of all records with same Currency Code1, Currency Code 2 Account Id and
     * Value Date from BO UB_OPX_PositionUpdateTransaction and return list of record of type Vector
     * PositionUpdateVector.
     * 
     * @param env -
     *            Bankfusion environment
     */

    public void process(BankFusionEnvironment env) {

        List<SimplePersistentObject> data1 = BankFusionThreadLocal.getPersistanceFactory().executeGenericQuery(
                NET_POSITION_UPDATE_QUERY, new ArrayList(), null, false);
        VectorTable PositionUpdateVector = new VectorTable();

        // If there is netted amount list, create Vector
        for (SimplePersistentObject lSimplePersistentObject : data1) {
            Map<String, Object> dataMap = lSimplePersistentObject.getDataMap();
            String currency1 = (String) dataMap.get(IBOUB_OPX_PositionUpdateTransaction.CURRENCYCODE1);
            String currency2 = (String) dataMap.get(IBOUB_OPX_PositionUpdateTransaction.CURRENCYCODE2);
            Date valueDate = (Date) dataMap.get(IBOUB_OPX_PositionUpdateTransaction.VALUEDT);
            BigDecimal amount1 = (BigDecimal) dataMap.get(IBOUB_OPX_PositionUpdateTransaction.AMOUNT1);
            BigDecimal amount2 = (BigDecimal) dataMap.get(IBOUB_OPX_PositionUpdateTransaction.AMOUNT2);
            BigDecimal baseEquivalent = (BigDecimal) dataMap.get(IBOUB_OPX_PositionUpdateTransaction.BASEQUIVAMT);
            String DealFlag = (String) dataMap.get(IBOUB_OPX_PositionUpdateTransaction.DEALFLAG);

            Map writeMap = new HashMap();
            writeMap.put("Currency1", currency1);
            writeMap.put("Currency2", currency2);
            writeMap.put("ValueDate", valueDate);
            writeMap.put("SumAmount1", amount1);
            writeMap.put("SumAmount2", amount2);
            writeMap.put("BaseEquivalent", baseEquivalent);
            writeMap.put("DealFlag", DealFlag);
            
            PositionUpdateVector.addAll(new VectorTable(writeMap));
        }
        setF_OUT_PositionUpdateVector(PositionUpdateVector);
    }
}
