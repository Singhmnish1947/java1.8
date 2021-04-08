package com.misys.ub.interfaces.opics.steps;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trapedza.bankfusion.bo.refimpl.IBOUB_OPX_PositionUpdateTransaction;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class PositionUpdateToOpics {

	private static final String NET_POSITION_UPDATE_QUERY = CommonConstants.SELECT + CommonConstants.SPACE + "T1."
			+ IBOUB_OPX_PositionUpdateTransaction.CURRENCYCODE1 + " AS "
			+ IBOUB_OPX_PositionUpdateTransaction.CURRENCYCODE1 + CommonConstants.COMMA + "T1."
			+ IBOUB_OPX_PositionUpdateTransaction.CURRENCYCODE2 + " AS "
			+ IBOUB_OPX_PositionUpdateTransaction.CURRENCYCODE2 + CommonConstants.COMMA + "T1."
			+ IBOUB_OPX_PositionUpdateTransaction.MULTIPLYDIVIDEFLG + " AS "
			+ IBOUB_OPX_PositionUpdateTransaction.MULTIPLYDIVIDEFLG + CommonConstants.COMMA + "T1."
			+ IBOUB_OPX_PositionUpdateTransaction.VALUEDT + " AS " + IBOUB_OPX_PositionUpdateTransaction.VALUEDT
			+ CommonConstants.COMMA + "T1." + IBOUB_OPX_PositionUpdateTransaction.DEALFLAG + " AS "
			+ IBOUB_OPX_PositionUpdateTransaction.DEALFLAG + CommonConstants.COMMA + "SUM(" + "T1."
			+ IBOUB_OPX_PositionUpdateTransaction.AMOUNT1 + ")" + " AS " + IBOUB_OPX_PositionUpdateTransaction.AMOUNT1
			+ CommonConstants.COMMA + "SUM(" + "T1." + IBOUB_OPX_PositionUpdateTransaction.AMOUNT2 + ")" + " AS "
			+ IBOUB_OPX_PositionUpdateTransaction.AMOUNT2 + CommonConstants.COMMA + "SUM(" + "T1."
			+ IBOUB_OPX_PositionUpdateTransaction.BASEQUIVAMT + ")" + " AS "
			+ IBOUB_OPX_PositionUpdateTransaction.BASEQUIVAMT + CommonConstants.SPACE + CommonConstants.FROM
			+ CommonConstants.SPACE + IBOUB_OPX_PositionUpdateTransaction.BONAME + " T1 " + CommonConstants.WHERE
			+ "T1." + IBOUB_OPX_PositionUpdateTransaction.STATUS + CommonConstants.EQUAL + "'U'" + " GROUP BY "
			+ IBOUB_OPX_PositionUpdateTransaction.CURRENCYCODE1 + CommonConstants.COMMA
			+ IBOUB_OPX_PositionUpdateTransaction.CURRENCYCODE2 + CommonConstants.COMMA
			+ IBOUB_OPX_PositionUpdateTransaction.MULTIPLYDIVIDEFLG + CommonConstants.COMMA
			+ IBOUB_OPX_PositionUpdateTransaction.VALUEDT + CommonConstants.COMMA
			+ IBOUB_OPX_PositionUpdateTransaction.DEALFLAG;

	public static void processPosition(BankFusionEnvironment env) {
		List<SimplePersistentObject> data1 = BankFusionThreadLocal.getPersistanceFactory()
				.executeGenericQuery(NET_POSITION_UPDATE_QUERY, new ArrayList(), null, false);

		for (SimplePersistentObject lSimplePersistentObject : data1) {
			Map<String, Object> dataMap = lSimplePersistentObject.getDataMap();
			String currency1 = (String) dataMap.get(IBOUB_OPX_PositionUpdateTransaction.CURRENCYCODE1);
			String currency2 = (String) dataMap.get(IBOUB_OPX_PositionUpdateTransaction.CURRENCYCODE2);
			Date valueDate = (Date) dataMap.get(IBOUB_OPX_PositionUpdateTransaction.VALUEDT);
			BigDecimal amount1 = (BigDecimal) dataMap.get(IBOUB_OPX_PositionUpdateTransaction.AMOUNT1);
			BigDecimal amount2 = (BigDecimal) dataMap.get(IBOUB_OPX_PositionUpdateTransaction.AMOUNT2);
			BigDecimal baseEquivalent = (BigDecimal) dataMap.get(IBOUB_OPX_PositionUpdateTransaction.BASEQUIVAMT);
			String dealFlag = (String) dataMap.get(IBOUB_OPX_PositionUpdateTransaction.DEALFLAG);
			String mulDivFlag = (String) dataMap.get(IBOUB_OPX_PositionUpdateTransaction.MULTIPLYDIVIDEFLG);

			HashMap<Object, Object> writeMap = new HashMap<>();
			writeMap.put("Currency1", currency1);
			writeMap.put("Currency2", currency2);
			writeMap.put("ValueDate", valueDate);
			writeMap.put("SumAmount1", amount1);
			writeMap.put("SumAmount2", amount2);
			writeMap.put("BaseEquivalent", baseEquivalent);
			writeMap.put("DealFlag", dealFlag);
			writeMap.put("MultiPlyDivide", mulDivFlag);

			FE_OPX_PositionHandoff.createNettedPositionEntry(writeMap);
		}
	}
}
