package com.misys.ub.interfaces.opics.steps;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trapedza.bankfusion.bo.refimpl.IBOUB_OPX_NostroUpdateTransaction;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class NostroUpdateToOpics {
	private static final String NET_NOSTRO_AMOUNT_QUERY = CommonConstants.SELECT + CommonConstants.SPACE + "T1."
			+ IBOUB_OPX_NostroUpdateTransaction.NOSTROACCOUNTID + " AS "
			+ IBOUB_OPX_NostroUpdateTransaction.NOSTROACCOUNTID + CommonConstants.COMMA + "T1."
			+ IBOUB_OPX_NostroUpdateTransaction.VALUEDT + " AS " + IBOUB_OPX_NostroUpdateTransaction.VALUEDT
			+ CommonConstants.COMMA + "SUM(" + "T1." + IBOUB_OPX_NostroUpdateTransaction.AMOUNT + ")" + " AS "
			+ IBOUB_OPX_NostroUpdateTransaction.AMOUNT + CommonConstants.SPACE + CommonConstants.FROM
			+ CommonConstants.SPACE + IBOUB_OPX_NostroUpdateTransaction.BONAME + " T1 " + CommonConstants.WHERE + "T1."
			+ IBOUB_OPX_NostroUpdateTransaction.STATUS + CommonConstants.EQUAL + "'U'" + " GROUP BY "
			+ IBOUB_OPX_NostroUpdateTransaction.NOSTROACCOUNTID + CommonConstants.COMMA
			+ IBOUB_OPX_NostroUpdateTransaction.VALUEDT;

	public static void processNostro(BankFusionEnvironment env) {

		List<SimplePersistentObject> netAmountList = BankFusionThreadLocal.getPersistanceFactory()
				.executeGenericQuery(NET_NOSTRO_AMOUNT_QUERY, new ArrayList(), null, false);
		if (netAmountList != null) {
			for (SimplePersistentObject lSimplePersistentObject : netAmountList) {
				Map<String, Object> dataMap = lSimplePersistentObject.getDataMap();
				String nostroAccountID = (String) dataMap.get(IBOUB_OPX_NostroUpdateTransaction.NOSTROACCOUNTID);
				Date valueDate = (Date) dataMap.get(IBOUB_OPX_NostroUpdateTransaction.VALUEDT);
				BigDecimal amount = (BigDecimal) dataMap.get(IBOUB_OPX_NostroUpdateTransaction.AMOUNT);
				HashMap<Object, Object> writeMap = new HashMap<>();
				writeMap.put("NOSTROACCOUNTID", nostroAccountID);
				writeMap.put("VALUEDATE", valueDate);
				writeMap.put("AMOUNT", amount);
				FE_OPX_NostroHandoff.createNettedEntry(writeMap);
			}
		}

	}

}
