package com.trapedza.bankfusion.fatoms;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractFircoSettlementInstructionExtract;

public class FircoSettlementInstructionExtract extends
		AbstractFircoSettlementInstructionExtract {
	private IPersistenceObjectsFactory factory = BankFusionThreadLocal
			.getPersistanceFactory();
	/**
	 * Logger instance
	 */
	private transient final static Log logger = LogFactory.getLog(FircoSettlementInstructionExtract.class.getName());
	public FircoSettlementInstructionExtract(BankFusionEnvironment env) {
		super(env);
	}

	private static final String EXECUTELOCATION = "SELECT CBPARAMVALUE from CBTB_MODULECONFIGURATION where CBMODULENAME ='KYC' and CBPARAMNAME ='CB_FIR_SETLMTINSTR_OUTPUT_FILE_LOCATION'";

	private static final String GENERATEREPORT = "SELECT  SD.DETAILID SETTLEMENTINSTRUCTIONSID,  REPLACE(SD.BENEFICIARY_CODE,'/','') AS BICCODE,  REPLACE(SD.BENEFICIARY_PARTY_IDENTIFIER,'/','') AS NAME,  REPLACE(SD.BENEFICIARY_TEXT1,'/','') ||' '||REPLACE(SD.BENEFICIARY_TEXT2,'/','')||' '||REPLACE(SD.BENEFICIARY_TEXT3,'/','')||' '||REPLACE(SD.BENEFICIARY_TEXT4,'/','') AS ADDRESS,  SD.DEBITACCOUNTID UBBENEFICIARYCARDNUMBER,  '' UBBRANCHID,  '' UBLOCATION,  '' UBBANKID,  SI.ISOCURRENCYCODE ISOCURRENCYCODE,  SI.CUSTOMERCODE CUSTOMERCODE,  SI.UBDEBITACCOUNTID UBDEBITACCOUNTID  FROM  WASADMIN.SETTLEMENTINSTRUCTIONSDETAIL SD,WASADMIN.SETTLEMENTINSTRUCTIONS SI,CBS.CBTB_TALENDJOBCONFIG  WHERE SD.SETTLEMENTINSTRUCTIONSID = SI.SETTLEMENTINSTRUCTIONSID AND CBTALENDJOBIDPK ='SettlementInstructionBsnsDt' AND SI.UBEXPIRYDTTM > WASADMIN.UB_TOTIMESTAMP(CBFIELDVALUE)  UNION  SELECT  SD.DETAILID SETTLEMENTINSTRUCTIONSID,  REPLACE(SD.FOR_ACC_IDENTIFIERCODE,'/','') AS BICCODE,  REPLACE(SD.FOR_ACCOUNT_PARTY_IDENTIFIER,'/','') AS NAME,  REPLACE(SD.FOR_ACCOUNT_TEXT1,'/','')||' '||REPLACE(SD.FOR_ACCOUNT_TEXT2,'/','')||' '||REPLACE(SD.FOR_ACCOUNT_TEXT3,'/','')||' '||REPLACE(SD.FOR_ACCOUNT_TEXT4,'/','') AS ADDRESS,  SD.DEBITACCOUNTID UBBENEFICIARYCARDNUMBER,  '' UBBRANCHID,  '' UBLOCATION,  '' UBBANKID,  SI.ISOCURRENCYCODE ISOCURRENCYCODE,  SI.CUSTOMERCODE CUSTOMERCODE,  SI.UBDEBITACCOUNTID UBDEBITACCOUNTID  FROM  WASADMIN.SETTLEMENTINSTRUCTIONSDETAIL SD,WASADMIN.SETTLEMENTINSTRUCTIONS SI,CBS.CBTB_TALENDJOBCONFIG  WHERE SD.SETTLEMENTINSTRUCTIONSID = SI.SETTLEMENTINSTRUCTIONSID AND CBTALENDJOBIDPK ='SettlementInstructionBsnsDt' AND SI.UBEXPIRYDTTM > WASADMIN.UB_TOTIMESTAMP(CBFIELDVALUE)  UNION  SELECT  SD.DETAILID SETTLEMENTINSTRUCTIONSID,  REPLACE(SD.INTERMEDIARY_CODE,'/','') AS BICCODE,  REPLACE(SD.INTERMEDIARY_PARTY_IDENTIFIER,'/','') AS NAME,  REPLACE(SD.INTERMEDIARY_TEXT1,'/','')||' '||REPLACE(SD.INTERMEDIARY_TEXT2,'/','')||' '||REPLACE(SD.INTERMEDIARY_TEXT3,'/','')||' '||REPLACE(SD.INTERMEDIARY_TEXT4,'/','') AS ADDRESS,  SD.DEBITACCOUNTID UBBENEFICIARYCARDNUMBER,  '' UBBRANCHID,  '' UBLOCATION,  '' UBBANKID,  SI.ISOCURRENCYCODE ISOCURRENCYCODE,  SI.CUSTOMERCODE CUSTOMERCODE,  SI.UBDEBITACCOUNTID UBDEBITACCOUNTID  FROM  WASADMIN.SETTLEMENTINSTRUCTIONSDETAIL SD,WASADMIN.SETTLEMENTINSTRUCTIONS SI,CBS.CBTB_TALENDJOBCONFIG  WHERE SD.SETTLEMENTINSTRUCTIONSID = SI.SETTLEMENTINSTRUCTIONSID AND CBTALENDJOBIDPK ='SettlementInstructionBsnsDt' AND SI.UBEXPIRYDTTM > WASADMIN.UB_TOTIMESTAMP(CBFIELDVALUE)  UNION  SELECT  SD.DETAILID SETTLEMENTINSTRUCTIONSID,  REPLACE(ORDERINGCUST_IDENTIFIERCODE,'/','') AS BICCODE,  REPLACE(ORDERINGINSTITUTION,'/','') AS NAME,  REPLACE(ORDERINGINSTITUTIONDTL1,'/','')||' '||REPLACE(ORDERINGINSTITUTIONDTL2,'/','')||' '||REPLACE(ORDERINGINSTITUTIONDTL3,'/','')||' '||REPLACE(ORDERINGINSTITUTIONDTL4,'/','') AS ADDRESS,  SD.DEBITACCOUNTID UBBENEFICIARYCARDNUMBER,  '' UBBRANCHID,  '' UBLOCATION,  '' UBBANKID,  SI.ISOCURRENCYCODE ISOCURRENCYCODE,  SI.CUSTOMERCODE CUSTOMERCODE,  SI.UBDEBITACCOUNTID UBDEBITACCOUNTID  FROM  WASADMIN.SETTLEMENTINSTRUCTIONSDETAIL SD,WASADMIN.SETTLEMENTINSTRUCTIONS SI,CBS.CBTB_TALENDJOBCONFIG  WHERE SD.SETTLEMENTINSTRUCTIONSID = SI.SETTLEMENTINSTRUCTIONSID AND CBTALENDJOBIDPK ='SettlementInstructionBsnsDt' AND SI.UBEXPIRYDTTM > WASADMIN.UB_TOTIMESTAMP(CBFIELDVALUE)  UNION  SELECT  SD.DETAILID SETTLEMENTINSTRUCTIONSID,  REPLACE(SD.PAY_TO_PARTY_IDENTIFIER,'/','') AS BICCODE,  REPLACE(SD.PARTYIDENTIFIER,'/','') AS NAME,  REPLACE(SD.PARTYADDRESSLINE1,'/','')||' '||REPLACE(SD.PARTYADDRESSLINE2,'/','')||' '||REPLACE(SD.PARTYADDRESSLINE3,'/','')||' '||REPLACE(SD.PARTYADDRESSLINE4,'/','') AS ADDRESS,  SD.DEBITACCOUNTID UBBENEFICIARYCARDNUMBER,  '' UBBRANCHID,  '' UBLOCATION,  '' UBBANKID,  SI.ISOCURRENCYCODE ISOCURRENCYCODE,  SI.CUSTOMERCODE CUSTOMERCODE,  SI.UBDEBITACCOUNTID UBDEBITACCOUNTID  FROM  WASADMIN.SETTLEMENTINSTRUCTIONSDETAIL SD,WASADMIN.SETTLEMENTINSTRUCTIONS SI,CBS.CBTB_TALENDJOBCONFIG  WHERE SD.SETTLEMENTINSTRUCTIONSID = SI.SETTLEMENTINSTRUCTIONSID AND CBTALENDJOBIDPK ='SettlementInstructionBsnsDt' AND SI.UBEXPIRYDTTM > WASADMIN.UB_TOTIMESTAMP(CBFIELDVALUE)  UNION  SELECT  SD.DETAILID SETTLEMENTINSTRUCTIONSID,  REPLACE(SD.PAY_TO_CODE,'/','') BICCODE,  '' AS NAME,  REPLACE(SD.PAY_TO_TEXT1,'/','')||' '||REPLACE(SD.PAY_TO_TEXT2,'/','')||' '||REPLACE(SD.PAY_TO_TEXT3,'/','')||' '||REPLACE(SD.PAY_TO_TEXT4,'/','') AS ADDRESS,  SD.DEBITACCOUNTID UBBENEFICIARYCARDNUMBER,  '' UBBRANCHID,  '' UBLOCATION,  '' UBBANKID,  SI.ISOCURRENCYCODE ISOCURRENCYCODE,  SI.CUSTOMERCODE CUSTOMERCODE,  SI.UBDEBITACCOUNTID UBDEBITACCOUNTID  FROM  WASADMIN.SETTLEMENTINSTRUCTIONSDETAIL SD,WASADMIN.SETTLEMENTINSTRUCTIONS SI,CBS.CBTB_TALENDJOBCONFIG  WHERE SD.SETTLEMENTINSTRUCTIONSID = SI.SETTLEMENTINSTRUCTIONSID AND CBTALENDJOBIDPK ='SettlementInstructionBsnsDt' AND SI.UBEXPIRYDTTM > WASADMIN.UB_TOTIMESTAMP(CBFIELDVALUE)  UNION  SELECT  SD.DETAILID SETTLEMENTINSTRUCTIONSID,  '' AS BICCODE,  '' AS NAME,  REPLACE(SD.PAY_DETAILS1,'/','')||' '||REPLACE(SD.PAY_DETAILS2,'/','')||' '||REPLACE(SD.PAY_DETAILS3,'/','')||' '||REPLACE(SD.PAY_DETAILS4,'/','') AS ADDRESS,  SD.DEBITACCOUNTID UBBENEFICIARYCARDNUMBER,  '' UBBRANCHID,  '' UBLOCATION,  '' UBBANKID,  SI.ISOCURRENCYCODE ISOCURRENCYCODE,  SI.CUSTOMERCODE CUSTOMERCODE,  SI.UBDEBITACCOUNTID UBDEBITACCOUNTID  FROM  WASADMIN.SETTLEMENTINSTRUCTIONSDETAIL SD,WASADMIN.SETTLEMENTINSTRUCTIONS SI,CBS.CBTB_TALENDJOBCONFIG  WHERE SD.SETTLEMENTINSTRUCTIONSID = SI.SETTLEMENTINSTRUCTIONSID AND CBTALENDJOBIDPK ='SettlementInstructionBsnsDt' AND SI.UBEXPIRYDTTM > WASADMIN.UB_TOTIMESTAMP(CBFIELDVALUE)  UNION   SELECT UBSETTLEMENTINSTRID SETTLEMENTINSTRUCTIONSID,          UBBENEFICIARYACCOUNTNUMBER BICCODE,          UBBENEFICIARYNAME NAME,          UBLOCATION ADDRESS,          UBBENEFICIARYCARDNUMBER UBBENEFICIARYCARDNUMBER,          UBBRANCHID UBBRANCHID,          UBLOCATION UBLOCATION,          UBBANKID UBBANKID,          ISOCURRENCYCODE ISOCURRENCYCODE,          CUSTOMERCODE CUSTOMERCODE,          UBDEBITACCOUNTID UBDEBITACCOUNTID  FROM   WASADMIN.UBTB_DOMSTPAYMNTINSTS,WASADMIN.SETTLEMENTINSTRUCTIONS,CBS.CBTB_TALENDJOBCONFIG  WHERE SETTLEMENTINSTRUCTIONSID = UBSETTLEMENTINSTRID AND CBTALENDJOBIDPK ='SettlementInstructionBsnsDt' AND UBEXPIRYDTTM > WASADMIN.UB_TOTIMESTAMP(CBFIELDVALUE)  UNION  SELECT  UBSETTLEMENTINSTRID SETTLEMENTINSTRUCTIONSID,          UBABACODE BICCODE,          UBBENEFICIARYNAME NAME,          REPLACE(UBPLACEOFREFERENCE1,'/','')||' '||REPLACE(UBPLACEOFREFERENCE2,'/','') ADDRESS,          UBBENEFICIARYACCOUNTNUMBER UBBENEFICIARYCARDNUMBER,          '' UBBRANCHID,          '' UBLOCATION,          '' UBBANKID,          ISOCURRENCYCODE ISOCURRENCYCODE,          CUSTOMERCODE CUSTOMERCODE,          UBDEBITACCOUNTID UBDEBITACCOUNTID  FROM  WASADMIN.UBTB_ABASETLMNTINSTR,WASADMIN.SETTLEMENTINSTRUCTIONS,CBS.CBTB_TALENDJOBCONFIG  WHERE SETTLEMENTINSTRUCTIONSID = UBSETTLEMENTINSTRID AND CBTALENDJOBIDPK ='SettlementInstructionBsnsDt' AND UBEXPIRYDTTM > WASADMIN.UB_TOTIMESTAMP(CBFIELDVALUE)";

	private static final String UPDATEDATE = "UPDATE CBTB_TALENDJOBCONFIG SET CBFIELDVALUE = ? WHERE CBTALENDJOBIDPK = 'SettlementInstructionBsnsDt'";
	private static final String UPDATEREFRENCE = "UPDATE CBTB_TALENDJOBCONFIG SET CBFIELDVALUE = ? WHERE CBTALENDJOBIDPK = 'SettlementInstructionFileNm'";
	private static final String UPDATESTATUS = "UPDATE CBTB_TALENDJOBCONFIG SET CBFIELDVALUE = ? WHERE CBTALENDJOBIDPK = 'SettlementInstructionRetCd'";
	private  Random rand = new SecureRandom();
	public void process(BankFusionEnvironment env) {

		PreparedStatement ps = null;
		Connection connection = null;
		String dateBusiness = SystemInformationManager.getInstance()
				.getBFBusinessDateAsString();
		String refUpdate = String.valueOf(rand.nextInt(12));
		String location = null;
		ResultSet result1=null;
		ResultSet result=null;
		FileWriter fout=null;
		try {

			connection = factory.getJDBCConnection();

			ps = connection.prepareStatement(EXECUTELOCATION);
			 result1 = ps.executeQuery();
			while (result1.next()) {
				location = result1.getString("CBPARAMVALUE");
			}
			ps.close();
			result1.close();
			ps = connection.prepareStatement(GENERATEREPORT);
			result = ps.executeQuery();
			String loc = location
					+ "SettlementInstruction"
					+ SystemInformationManager.getInstance()
							.getBFBusinessDateAsString() + ".txt";
			// new FileOutputStream(loc,false);
			 fout = new FileWriter(loc);
			while (result.next()) {
				for (int i = 1; i <= 11; i++) {
					if (null != result.getString(i))
						fout.write(result.getString(i).trim());
					fout.write(":");
				}
				fout.write("$");
				fout.write("\n");
			}
			ps.close();
			result.close();
			
			ps = connection.prepareStatement(UPDATEDATE);
			ps.setString(1, dateBusiness);
			ps.executeUpdate();
			ps.close();
			ps = connection.prepareStatement(UPDATEREFRENCE);
			ps.setString(1, refUpdate);
			ps.executeUpdate();
			ps.close();
			ps = connection.prepareStatement(UPDATESTATUS);
			ps.setString(1, "00");
			ps.executeUpdate();
			ps.close();

		} catch (SQLException e) {
			setF_OUT_Error(true);
		    logger.error(ExceptionUtil.getExceptionAsString(e));
		} catch (FileNotFoundException e) {
			setF_OUT_Error(true);
			 logger.error(ExceptionUtil.getExceptionAsString(e));
		} catch (IOException e) {
			setF_OUT_Error(true);
			 logger.error(ExceptionUtil.getExceptionAsString(e));
		}
		finally{
			if(fout!=null)
				try {
					fout.close();
				} catch (IOException e) {
					logger.error(ExceptionUtil.getExceptionAsString(e));
				}
			if(ps!=null)
				try {
					ps.close();
				} catch (SQLException e) {
					logger.error(ExceptionUtil.getExceptionAsString(e));
				}
			if(result!=null)
				try {
					result.close();
				} catch (SQLException e) {
					logger.error(ExceptionUtil.getExceptionAsString(e));
				}
			if(result1!=null)
				try {
					result1.close();
				} catch (SQLException e) {
					logger.error(ExceptionUtil.getExceptionAsString(e));
				}
		}
	}
}
