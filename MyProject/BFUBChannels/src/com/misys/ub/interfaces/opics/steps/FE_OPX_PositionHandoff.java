package com.misys.ub.interfaces.opics.steps;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.misys.bankfusion.subsystem.microflow.runtime.impl.MFExecuter;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

@SuppressWarnings({ "rawtypes", "deprecation" })

public class FE_OPX_PositionHandoff {

	private static Log LOGGER = LogFactory.getLog(PositionUpdateToOpics.class.getName());

	private static IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

	private static final String UPDATE_SENT_STATUS = "UPDATE INFTB_OPXPOSITIONACCTUPDTXN SET INSTATUS = ? WHERE INOPXPOSTRANSRIDPK = ? OR INNETTEDTXNID = ?";

	private static final String INSERT_NETTED_RECORD = "INSERT INTO INFTB_OPXPOSITIONACCTUPDTXN(INOPXPOSTRANSRIDPK,INNETTEDTXNID,INDEALFLAG,INAMOUNT1,INCURRENCYCODE1,INAMOUNT2,INCURRENCYCODE2,INEXCHANGERATE,INMULTIPLYDIVIDEFLG,INSTATUS,INBASEQUIVAMT,INVALUEDT,INPROCESSDTTM,INOPICSDEALNO,INBROKERDEALCODE,VERSIONNUM,INTRANSACTIONID)VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private static final String UPDATE_NETTED_IDENTITY = "UPDATE INFTB_OPXPOSITIONACCTUPDTXN SET INNETTEDTXNID=? WHERE INCURRENCYCODE1 = ? AND INCURRENCYCODE2=? AND INMULTIPLYDIVIDEFLG = ? AND INVALUEDT = ? AND INDEALFLAG = ? AND INSTATUS = ?";

	static void createNettedPositionEntry(HashMap<Object, Object> writeMap) {

		String currency1 = null;

		String currency2 = null;

		Date valueDate = null;

		String sumAmount1 = null;

		String sumAmount2 = null;

		String baseEquivalent = null;

		String dealFlag = null;

		String multiPlyDivide = null;

		BigDecimal exchRt;

		for (Map.Entry<Object, Object> entry : writeMap.entrySet()) {

			if (entry.getKey().equals("Currency1")) {

				currency1 = entry.getValue().toString();

			}

			if (entry.getKey().equals("Currency2")) {

				currency2 = entry.getValue().toString();

			}

			if (entry.getKey().equals("ValueDate")) {

				valueDate = (Date) entry.getValue();

			}

			if (entry.getKey().equals("SumAmount1")) {

				sumAmount1 = entry.getValue().toString();

			}

			if (entry.getKey().equals("SumAmount2")) {

				sumAmount2 = entry.getValue().toString();

			}

			if (entry.getKey().equals("BaseEquivalent")) {

				baseEquivalent = entry.getValue().toString();

			}

			if (entry.getKey().equals("DealFlag")) {

				dealFlag = entry.getValue().toString();

			}

			if (entry.getKey().equals("MultiPlyDivide")) {

				multiPlyDivide = entry.getValue().toString();

			}

		}

		Map<Object, Object> idGenerationFormula = new HashMap<>();

		idGenerationFormula.put("idGenerationFormula", "UB_OPX_Autonumber");

		HashMap fdealid = MFExecuter.executeMF("UB_OPX_FrontEndDealIdgeneration_SRV",

				BankFusionThreadLocal.getBankFusionEnvironment());

		HashMap dealid = MFExecuter.executeMF("UB_OPX_Idgeneration_SRV",

				BankFusionThreadLocal.getBankFusionEnvironment(), idGenerationFormula);

		String frontEndDealId = (String) fdealid.get("FrontEndDealId");

		String uniqueDealId = String.valueOf(dealid.get("UniqueID"));

		exchRt = new BigDecimal(sumAmount2).divide(new BigDecimal(sumAmount1), RoundingMode.CEILING);

		factory.beginTransaction();

		PreparedStatement ps1 = null;

		PreparedStatement ps2 = null;

		PreparedStatement ps3 = null;

		Connection connection = factory.getJDBCConnection();

		try {

			String processedTimeStamp = SystemInformationManager.getInstance().getBFBusinessDateTimeAsString();

			ps1 = connection.prepareStatement(UPDATE_NETTED_IDENTITY);

			ps1.setString(1, frontEndDealId);

			ps1.setString(2, currency1);

			ps1.setString(3, currency2);

			ps1.setString(4, multiPlyDivide);

			ps1.setDate(5, valueDate);

			ps1.setString(6, dealFlag);

			ps1.setString(7, "U");

			ps1.executeUpdate();

			ps2 = connection.prepareStatement(INSERT_NETTED_RECORD);

			ps2.setString(1, frontEndDealId);

			ps2.setString(2, "");

			ps2.setString(3, dealFlag);

			ps2.setBigDecimal(4, new BigDecimal(sumAmount1));

			ps2.setString(5, currency1);

			ps2.setBigDecimal(6, new BigDecimal(sumAmount2));

			ps2.setString(7, currency2);

			ps2.setString(8, exchRt.toString());

			ps2.setString(9, multiPlyDivide);

			ps2.setString(10, "H");

			ps2.setString(11, baseEquivalent);

			ps2.setDate(12, valueDate);

			ps2.setTimestamp(13, SystemInformationManager.getInstance().getBFBusinessDateTime());

			ps2.setString(14, uniqueDealId);

			ps2.setString(15, "");

			ps2.setString(16, "2");

			ps2.setString(17, "");

			ps2.execute();

			String msg = createXml(frontEndDealId, processedTimeStamp.substring(0, 10), uniqueDealId,
					valueDate.toString(),

					valueDate.toString(), currency1, sumAmount1, multiPlyDivide, exchRt.toString(), currency2,
					sumAmount2,

					baseEquivalent, dealFlag, frontEndDealId);

			MessageProducerUtil.sendMessage(msg, "UB_to_OPICS_Queue");

			LOGGER.info(msg);

			ps3 = connection.prepareStatement(UPDATE_SENT_STATUS);

			ps3.setString(1, "P");

			ps3.setString(2, frontEndDealId);

			ps3.setString(3, frontEndDealId);

			ps3.executeUpdate();

			factory.commitTransaction();

		} catch (SQLException e) {

			LOGGER.error(ExceptionUtil.getExceptionAsString(e));

		} finally {

			if (ps1 != null) {

				try {

					ps1.close();

				} catch (SQLException e) {

					LOGGER.error(ExceptionUtil.getExceptionAsString(e));

				}

			}

			if (ps2 != null) {

				try {

					ps2.close();

				} catch (SQLException e) {

					LOGGER.error(ExceptionUtil.getExceptionAsString(e));

				}

			}

			if (ps3 != null) {

				try {

					ps3.close();

				} catch (SQLException e) {

					LOGGER.error(ExceptionUtil.getExceptionAsString(e));

				}

			}

		}

	}

	private static String createXml(String frontEndDealId, String frdate, String uniqueDealId, String valueDate,

			String valueDate2, String currency1, String sumAmount1, String multiPlyDivide, String exchrt,

			String currency2, String sumAmount2, String baseEquivalent, String dealFlag, String frontEndDealId2) {


		String message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
				+ "<bfub:UBTOOPICS_TRANSACTIONUPDATES xmlns:bfub=\"http://www.misys.com/ub/types\">\r\n"
				+ "	<bfub:FRONTENDDEALNO>FRENDDEALNO</bfub:FRONTENDDEALNO>\r\n" + "	<bfub:ERRORCODE/>\r\n"
				+ "	<bfub:FRONTENDDATE>FRENDDATE</bfub:FRONTENDDATE>\r\n" + "	<bfub:FRONTENDTIME/>\r\n"
				+ "	<bfub:DEALNUMBER>DLNO</bfub:DEALNUMBER>\r\n" + "	<bfub:SWAPDEALNUMBER/>\r\n"
				+ "	<bfub:BOODATE/>\r\n" + "	<bfub:BOOTIME/>\r\n" + "	<bfub:LSTMNTDATE/>\r\n"
				+ "	<bfub:VALUEDATE>VALDT</bfub:VALUEDATE>\r\n" + "	<bfub:DEALDATE>DLDATE</bfub:DEALDATE>\r\n"
				+ "	<bfub:CURRENCYCODE>CURNCODE</bfub:CURRENCYCODE>\r\n"
				+ "	<bfub:CURRENCYAMOUNT>CURAMT</bfub:CURRENCYAMOUNT>\r\n"
				+ "	<bfub:TERMSOFTHERATE>MULDIV</bfub:TERMSOFTHERATE>\r\n"
				+ "	<bfub:DEALRATEASSOCIATEDWITHCURRENCY>EXCHRT</bfub:DEALRATEASSOCIATEDWITHCURRENCY>\r\n"
				+ "	<bfub:CURRENCYPREMIUMANDDISCOUNT>0</bfub:CURRENCYPREMIUMANDDISCOUNT>\r\n"
				+ "	<bfub:COUNTERCURRENCYCODE>COUNTERCURCODE</bfub:COUNTERCURRENCYCODE>\r\n"
				+ "	<bfub:COUNTERCURRENCYAMOUNT>COUNTERCURAMOUNT</bfub:COUNTERCURRENCYAMOUNT>\r\n"
				+ "	<bfub:COUNTRYBASEAMOUNT>BASEEQ</bfub:COUNTRYBASEAMOUNT>\r\n"
				+ "	<bfub:FIXEDRATEORNDFDEALINDICATOR/>\r\n"
				+ "	<bfub:PURCHASESALEINDICATOR>PURSELL</bfub:PURCHASESALEINDICATOR>\r\n"
				+ "	<bfub:DEALTEXT>UID</bfub:DEALTEXT>\r\n" + "</bfub:UBTOOPICS_TRANSACTIONUPDATES>";

		StringBuilder sb = new StringBuilder(message);
		List<Integer> list = new ArrayList<>();
		swapAll(sb, "FRENDDEALNO", frontEndDealId, list);
		swapAll(sb, "FRENDDATE", frdate, list);
		swapAll(sb, "DLNO", uniqueDealId, list);
		swapAll(sb, "VALDT", valueDate, list);
		swapAll(sb, "DLDATE", valueDate2, list);
		swapAll(sb, "CURNCODE", currency1, list);
		swapAll(sb, "CURAMT", sumAmount1, list);
		swapAll(sb, "MULDIV", multiPlyDivide, list);
		swapAll(sb, "EXCHRT", exchrt, list);
		swapAll(sb, "COUNTERCURCODE", currency2, list);
		swapAll(sb, "COUNTERCURAMOUNT", sumAmount2, list);
		swapAll(sb, "BASEEQ", baseEquivalent, list);
		swapAll(sb, "PURSELL", dealFlag, list);
		swapAll(sb, "UID", frontEndDealId2, list);
		

		return sb.toString();

	}

	public static List<Integer> occurrences(StringBuilder src, String s, List<Integer> list) {
		for (int idx = 0;;)
			if ((idx = src.indexOf(s, idx)) >= 0) {
				list.add(idx);
				idx += s.length();
			} else
				return list;
	}

	public static void swapAll(StringBuilder sb, String s1, String s2, List<Integer> list) {
		list.clear();
		List<Integer> l1 = occurrences(sb, s1, list);
		for (int i1 = l1.size() - 1; i1 >= 0;) {
			int idx1 = i1 < 0 ? -1 : l1.get(i1);
			if (idx1 > -1)
				sb.replace(idx1, idx1 + s1.length(), s2);
			i1--;
		}
	}
}
