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
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractFircoStandingOrderExtract;

public class FircoStandingOrderExtract extends
		AbstractFircoStandingOrderExtract {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private IPersistenceObjectsFactory factory = BankFusionThreadLocal
			.getPersistanceFactory();
	/**
	 * Logger instance
	 */
	private transient final static Log logger = LogFactory.getLog(FircoSettlementInstructionExtract.class.getName());
	
	public FircoStandingOrderExtract(BankFusionEnvironment env) {
		super(env);
	}

	private static final String EXECUTELOCATION = "SELECT CBPARAMVALUE from CBTB_MODULECONFIGURATION where CBMODULENAME ='KYC' and CBPARAMNAME ='CB_FIR_STAND_CUST_OUTPUT_FILE_LOCATION'";

	// private static final String GETLASTEXECUTEDATE =
	// "Select CBFIELDVALUE from CBTB_TALENDJOBCONFIG where CBTALENDJOBIDPK = 'CreateCustDetailBsnsDt'";

	private static final String GENERATEREPORT = "SELECT  ST.UBPAYMENTORDERIDPK StandingOrderNumber,          P.UBSETTLEMENTINSTRUCTIONSID SettlementInstructionID,          A.UBCUSTOMERCODE CustomerAccNo,          ST.UBISOCURRENCYCODE SetUpCurrency,          ST.UBAMOUNT SetupAmount,          REPLACE(SI.FOR_ACCOUNT_PARTY_IDENTIFIER,'/','') BeneficiaryPayeeAccNo,          REPLACE(SI.FOR_ACCOUNT_TEXT1,'/','') BeneficiaryPayeeName,          ST.UBAMOUNTPAID AmountPaid,          A.ISOCURRENCYCODE PaidCurrency,          REPLACE(SI.BENEFICIARY_CODE,'/','') BENEFICIARY_CODE,          REPLACE(SI.BENEFICIARY_PARTY_IDENTIFIER,'/','') BENEFICIARY_PARTY_IDENTIFIER,          REPLACE(SI.BENEFICIARY_TEXT1,'/','') BENEFICIARY_TEXT1,          REPLACE(SI.BENEFICIARY_TEXT2,'/','') BENEFICIARY_TEXT2,          REPLACE(SI.BENEFICIARY_TEXT3,'/','') BENEFICIARY_TEXT3,          REPLACE(SI.BENEFICIARY_TEXT4,'/','') BENEFICIARY_TEXT4,          REPLACE(SI.FOR_ACC_IDENTIFIERCODE,'/','') FOR_ACC_IDENTIFIERCODE,          REPLACE(SI.FOR_ACCOUNT_TEXT2,'/','') FOR_ACCOUNT_TEXT2,          REPLACE(SI.FOR_ACCOUNT_TEXT3,'/','') FOR_ACCOUNT_TEXT3,          REPLACE(SI.FOR_ACCOUNT_TEXT4,'/','') FOR_ACCOUNT_TEXT4,          REPLACE(SI.INTERMEDIARY_CODE,'/','') INTERMEDIARY_CODE,          REPLACE(SI.INTERMEDIARY_PARTY_IDENTIFIER,'/','') INTERMEDIARY_PARTY_IDENTIFIER,          REPLACE(SI.INTERMEDIARY_TEXT1,'/','') INTERMEDIARY_TEXT1,          REPLACE(SI.INTERMEDIARY_TEXT2,'/','') INTERMEDIARY_TEXT2,          REPLACE(SI.INTERMEDIARY_TEXT3,'/','') INTERMEDIARY_TEXT3,          REPLACE(SI.INTERMEDIARY_TEXT4,'/','') INTERMEDIARY_TEXT4,          REPLACE(SI.ORDERINGCUST_IDENTIFIERCODE,'/','') ORDERINGCUST_IDENTIFIERCODE,          REPLACE(SI.ORDERINGINSTITUTION,'/','') ORDERINGINSTITUTION,          REPLACE(SI.ORDERINGINSTITUTIONDTL1,'/','') ORDERINGINSTITUTIONDTL1,          REPLACE(SI.ORDERINGINSTITUTIONDTL2,'/','') ORDERINGINSTITUTIONDTL2,          REPLACE(SI.ORDERINGINSTITUTIONDTL3,'/','') ORDERINGINSTITUTIONDTL3,          REPLACE(SI.ORDERINGINSTITUTIONDTL4,'/','') ORDERINGINSTITUTIONDTL4,          REPLACE(SI.PAY_TO_PARTY_IDENTIFIER,'/','') PAY_TO_PARTY_IDENTIFIER,          REPLACE(SI.PARTYIDENTIFIER,'/','')   PARTYIDENTIFIER,          REPLACE(SI.PARTYADDRESSLINE1,'/','') PARTYADDRESSLINE1,          REPLACE(SI.PARTYADDRESSLINE2,'/','') PARTYADDRESSLINE2,          REPLACE(SI.PARTYADDRESSLINE3,'/','') PARTYADDRESSLINE3,          REPLACE(SI.PARTYADDRESSLINE4,'/','') PARTYADDRESSLINE4,          REPLACE(SI.PAY_TO_CODE,'/','')  PAY_TO_CODE,          REPLACE(SI.PAY_TO_TEXT1,'/','') PAY_TO_TEXT1,          REPLACE(SI.PAY_TO_TEXT2,'/','') PAY_TO_TEXT2,          REPLACE(SI.PAY_TO_TEXT3,'/','') PAY_TO_TEXT3,          REPLACE(SI.PAY_TO_TEXT4,'/','') PAY_TO_TEXT4,          REPLACE(SI.PAY_DETAILS1,'/','') PAY_DETAILS1,          REPLACE(SI.PAY_DETAILS2,'/','') PAY_DETAILS2,          REPLACE(SI.PAY_DETAILS3,'/','') PAY_DETAILS3,          REPLACE(SI.PAY_DETAILS4,'/','') PAY_DETAILS4   FROM UBTB_PAYMENTORDER ST,SETTLEMENTINSTRUCTIONSDETAIL SI,ACCOUNT A,UBTB_PAYMENTORDERACCMAP P  WHERE ST.UBPAYMENTORDERIDPK = P.UBPAYMENTORDERID  AND   P.UBSETTLEMENTINSTRUCTIONSID = SI.DETAILID  AND   ST.UBACCOUNTID = A.ACCOUNTID  AND   ST.UBPAYMENTORDERSTATUS IN ('001','002')  UNION  SELECT  ST.UBPAYMENTORDERIDPK StandingOrderNumber,          P.UBSETTLEMENTINSTRUCTIONSID SettlementInstructionID,          A.UBCUSTOMERCODE CustomerAccNo,          ST.UBISOCURRENCYCODE SetUpCurrency,          ST.UBAMOUNT SetupAmount,          SI.UBBENEFICIARYACCOUNTNUMBER BeneficiaryPayeeAccNo,          SI.UBBENEFICIARYNAME BeneficiaryPayeeName,          ST.UBAMOUNTPAID AmountPaid,          A.ISOCURRENCYCODE PaidCurrency,          SI.UBLOCATION BENEFICIARY_CODE,          '' BENEFICIARY_PARTY_IDENTIFIER,          '' BENEFICIARY_TEXT1,          '' BENEFICIARY_TEXT2,          '' BENEFICIARY_TEXT3,          '' BENEFICIARY_TEXT4,          '' FOR_ACC_IDENTIFIERCODE,          '' FOR_ACCOUNT_TEXT2,          '' FOR_ACCOUNT_TEXT3,          '' FOR_ACCOUNT_TEXT4,          '' INTERMEDIARY_CODE,          '' INTERMEDIARY_PARTY_IDENTIFIER,          '' INTERMEDIARY_TEXT1,          '' INTERMEDIARY_TEXT2,          '' INTERMEDIARY_TEXT3,          '' INTERMEDIARY_TEXT4,          '' ORDERINGCUST_IDENTIFIERCODE,          '' ORDERINGINSTITUTION,          '' ORDERINGINSTITUTIONDTL1,          '' ORDERINGINSTITUTIONDTL2,          '' ORDERINGINSTITUTIONDTL3,          '' ORDERINGINSTITUTIONDTL4,          '' PAY_TO_PARTY_IDENTIFIER,          '' PARTYIDENTIFIER,          '' PARTYADDRESSLINE1,          '' PARTYADDRESSLINE2,          '' PARTYADDRESSLINE3,          '' PARTYADDRESSLINE4,          '' PAY_TO_CODE,          '' PAY_TO_TEXT1,          '' PAY_TO_TEXT2,          '' PAY_TO_TEXT3,          '' PAY_TO_TEXT4,          '' PAY_DETAILS1,          '' PAY_DETAILS2,          '' PAY_DETAILS3,          '' PAY_DETAILS4   FROM UBTB_PAYMENTORDER ST,UBTB_DOMSTPAYMNTINSTS SI,ACCOUNT A,UBTB_PAYMENTORDERACCMAP P  WHERE ST.UBPAYMENTORDERIDPK = P.UBPAYMENTORDERID  AND   P.UBSETTLEMENTINSTRUCTIONSID = SI.UBSETTLEMENTINSTRID  AND   ST.UBACCOUNTID = A.ACCOUNTID  AND   ST.UBPAYMENTORDERSTATUS IN ('001','002')  UNION  SELECT  ST.UBPAYMENTORDERIDPK StandingOrderNumber,          P.UBSETTLEMENTINSTRUCTIONSID SettlementInstructionID,          A.UBCUSTOMERCODE CustomerAccNo,          ST.UBISOCURRENCYCODE SetUpCurrency,          ST.UBAMOUNT SetupAmount,          SI.UBBENEFICIARYACCOUNTNUMBER BeneficiaryPayeeAccNo,          SI.UBBENEFICIARYNAME BeneficiaryPayeeName,          ST.UBAMOUNTPAID AmountPaid,          A.ISOCURRENCYCODE PaidCurrency,          REPLACE(SI.UBPLACEOFREFERENCE1,'/','') BENEFICIARY_CODE,          REPLACE(SI.UBPLACEOFREFERENCE2,'/','') BENEFICIARY_PARTY_IDENTIFIER,          UBABACODE BENEFICIARY_TEXT1,          '' BENEFICIARY_TEXT2,          '' BENEFICIARY_TEXT3,          '' BENEFICIARY_TEXT4,          '' FOR_ACC_IDENTIFIERCODE,          '' FOR_ACCOUNT_TEXT2,          '' FOR_ACCOUNT_TEXT3,          '' FOR_ACCOUNT_TEXT4,          '' INTERMEDIARY_CODE,          '' INTERMEDIARY_PARTY_IDENTIFIER,          '' INTERMEDIARY_TEXT1,          '' INTERMEDIARY_TEXT2,          '' INTERMEDIARY_TEXT3,          '' INTERMEDIARY_TEXT4,          '' ORDERINGCUST_IDENTIFIERCODE,          '' ORDERINGINSTITUTION,          '' ORDERINGINSTITUTIONDTL1,          '' ORDERINGINSTITUTIONDTL2,          '' ORDERINGINSTITUTIONDTL3,          '' ORDERINGINSTITUTIONDTL4,          '' PAY_TO_PARTY_IDENTIFIER,          '' PARTYIDENTIFIER,          '' PARTYADDRESSLINE1,          '' PARTYADDRESSLINE2,          '' PARTYADDRESSLINE3,          '' PARTYADDRESSLINE4,          '' PAY_TO_CODE,          '' PAY_TO_TEXT1,          '' PAY_TO_TEXT2,          '' PAY_TO_TEXT3,          '' PAY_TO_TEXT4,          '' PAY_DETAILS1,          '' PAY_DETAILS2,          '' PAY_DETAILS3,          '' PAY_DETAILS4   FROM UBTB_PAYMENTORDER ST,UBTB_ABASETLMNTINSTR SI,ACCOUNT A,UBTB_PAYMENTORDERACCMAP P  WHERE ST.UBPAYMENTORDERIDPK = P.UBPAYMENTORDERID   AND P.UBSETTLEMENTINSTRUCTIONSID = SI.UBSETTLEMENTINSTRID  AND   ST.UBACCOUNTID = A.ACCOUNTID  AND   ST.UBPAYMENTORDERSTATUS IN ('001','002')";

	private static final String UPDATEDATE = "UPDATE CBTB_TALENDJOBCONFIG SET CBFIELDVALUE = ? WHERE CBTALENDJOBIDPK = 'CreateCustDetailFileNm'";
	private static final String UPDATEREFRENCE = "UPDATE CBTB_TALENDJOBCONFIG SET CBFIELDVALUE = ? WHERE CBTALENDJOBIDPK = 'SettlementInstructionFileNm'";
	private static final String UPDATESTATUS = "UPDATE CBTB_TALENDJOBCONFIG SET CBFIELDVALUE = ? WHERE CBTALENDJOBIDPK = 'CreateCustDetailRetCd'";
	private  Random rand = new SecureRandom();
	public void process(BankFusionEnvironment env) {

		PreparedStatement ps = null;
		Connection connection = null;
		String dateBusiness = SystemInformationManager.getInstance()
				.getBFBusinessDateTimeAsString();
		String refUpdate = String.valueOf(rand.nextInt(12));
		String location = null;
		ResultSet result1= null;
		ResultSet result= null;
		FileWriter fout = null;
		
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
					+ "StandingOrder"
					+ SystemInformationManager.getInstance()
							.getBFBusinessDateAsString() + ".txt";
			fout = new FileWriter(loc);
			while (result.next()) {
				for (int i = 1; i <= 46; i++) {
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
