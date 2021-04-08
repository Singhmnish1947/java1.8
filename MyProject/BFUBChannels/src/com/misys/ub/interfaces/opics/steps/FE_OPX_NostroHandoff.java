package com.misys.ub.interfaces.opics.steps;

import java.math.BigDecimal;
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

@SuppressWarnings({ "deprecation", "rawtypes" })

public class FE_OPX_NostroHandoff {

	protected static IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

	protected static Log LOGGER = LogFactory.getLog(NostroUpdateToOpics.class.getName());

	private static final String INSERT_NETTED_RECORD = "INSERT INTO INFTB_OPXNOSTROACCTUPDTXN(INOPXNOSTROTRANSRIDPK,INNOSTROACCOUNTID,INNETTEDTXNID,INAMOUNT,INSTATUS,INBASEQUIVAMT,INVALUEDT,INPROCESSDTTM,INOPICSDEALNO,INDESCRIPTION,VERSIONNUM)VALUES(?,?,?,?,?,?,?,?,?,?,?)";

	private static final String UPDATE_NETTED_IDENTITY = "UPDATE INFTB_OPXNOSTROACCTUPDTXN SET INNETTEDTXNID=? WHERE INNOSTROACCOUNTID = ? AND INVALUEDT=? AND INSTATUS = ?";

	private static final String UPDATE_SENT_STATUS = "UPDATE INFTB_OPXNOSTROACCTUPDTXN SET INSTATUS=? WHERE INOPXNOSTROTRANSRIDPK =? OR INNETTEDTXNID =?";

	public static void createNettedEntry(HashMap<Object, Object> hm) {

		String nostroAcctId = "";

		Date valueDt = null;

		String amount = "";

		String description = "Netted Amount";

		for (Map.Entry<Object, Object> entry : hm.entrySet()) {

			if (entry.getKey().equals("NOSTROACCOUNTID")) {

				nostroAcctId = entry.getValue().toString();

			}

			if (entry.getKey().equals("VALUEDATE")) {

				valueDt = (Date) entry.getValue();

			}

			if (entry.getKey().equals("AMOUNT")) {

				amount = entry.getValue().toString();

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

		factory.beginTransaction();

		PreparedStatement ps1 = null;

		PreparedStatement ps2 = null;

		PreparedStatement ps3 = null;

		Connection connection = factory.getJDBCConnection();

		try {

			String processedTimeStamp = SystemInformationManager.getInstance().getBFBusinessDateTimeAsString();

			ps1 = connection.prepareStatement(UPDATE_NETTED_IDENTITY);

			ps1.setString(1, frontEndDealId);

			ps1.setString(2, nostroAcctId);

			ps1.setDate(3, valueDt);

			ps1.setString(4, "U");

			ps1.executeUpdate();

			ps2 = connection.prepareStatement(INSERT_NETTED_RECORD);

			ps2.setString(1, frontEndDealId);

			ps2.setString(2, nostroAcctId);

			ps2.setString(3, "");

			ps2.setBigDecimal(4, new BigDecimal(amount));

			ps2.setString(5, "H");

			ps2.setString(6, "0.0000");

			ps2.setDate(7, valueDt);

			ps2.setTimestamp(8, SystemInformationManager.getInstance().getBFBusinessDateTime());

			ps2.setString(9, uniqueDealId);

			ps2.setString(10, description);

			ps2.setString(11, "2");

			ps2.execute();

			String msg = createXml(frontEndDealId, processedTimeStamp.substring(0, 10), uniqueDealId, nostroAcctId,

					valueDt.toString(), amount, description);

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

	private static String createXml(String frontEndDealId, String substring, String uniqueDealId, String nostroAcctId,

			String valueDt, String amount, String description) {

		String messageSent = "";

		String message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
				+ "<bfub:UBTOOPICS_NOSTROUPDATES xmlns:bfub=\"http://www.misys.com/ub/types\">\r\n"
				+ "	<bfub:FRONTENDDEALNO>FRONTENDDEALNO1</bfub:FRONTENDDEALNO>\r\n" + "	<bfub:ERRORCODE/>\r\n"
				+ "	<bfub:FRONTENDDATE>FRONTENDDATE1</bfub:FRONTENDDATE>\r\n" + "	<bfub:FRONTENDTIME/>\r\n"
				+ "	<bfub:DEALNUMBER>DEALNUMBER1</bfub:DEALNUMBER>\r\n"
				+ "	<bfub:SWAPDEALNUMBER>0</bfub:SWAPDEALNUMBER>\r\n" + "	<bfub:BOODATE/>\r\n"
				+ "	<bfub:BOOTIME/>\r\n" + "	<bfub:LSTMNTDATE/>\r\n"
				+ "	<bfub:DEALSEQUENCENUMBER>0</bfub:DEALSEQUENCENUMBER>\r\n"
				+ "	<bfub:NOSTROACCOUNT>NOSTROACCOUNT1</bfub:NOSTROACCOUNT>\r\n" + "	<bfub:PRODUCTCODE/>\r\n"
				+ "	<bfub:PRODUCTTYPE/>\r\n" + "	<bfub:VALUEDATE>VALUEDATE1</bfub:VALUEDATE>\r\n"
				+ "	<bfub:UPDATEAMOUNT>UPDATEAMOUNT1</bfub:UPDATEAMOUNT>\r\n"
				+ "	<bfub:DESCRIPTION>DESCRIPTION1</bfub:DESCRIPTION>\r\n" + "</bfub:UBTOOPICS_NOSTROUPDATES>";

		StringBuilder sb = new StringBuilder(message);
		List<Integer> list = new ArrayList<>();
		swapAll(sb, "FRONTENDDEALNO1", frontEndDealId, list);
		swapAll(sb, "FRONTENDDATE1", substring, list);
		swapAll(sb, "DEALNUMBER1", uniqueDealId, list);
		swapAll(sb, "NOSTROACCOUNT1", nostroAcctId, list);
		swapAll(sb, "VALUEDATE1", valueDt, list);
		swapAll(sb, "UPDATEAMOUNT1", amount, list);
		swapAll(sb, "DESCRIPTION1", description, list);

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