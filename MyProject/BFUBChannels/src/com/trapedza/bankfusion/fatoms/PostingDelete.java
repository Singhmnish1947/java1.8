package com.trapedza.bankfusion.fatoms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractPostingDelete;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PostingDelete extends AbstractPostingDelete {


	private IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
	private transient final static Log logger = LogFactory
	.getLog(PostingDelete.class.getName());

	public PostingDelete(BankFusionEnvironment env) {
		super(env);
	}

	private static final String DELETE_ACCOUNT = "DELETE FROM ACCOUNT WHERE ACCOUNTID = '01210PER00101'";

	private static final String DELETE_ACCOUNT1 = "DELETE FROM ACCOUNT WHERE ACCOUNTID = '0100500000100'";

	private static final String DELETE_CUSTOMER = "DELETE FROM CUSTOMER WHERE CUSTOMERCODE = 'PER001'";

	private static final String DELETE_CUSTOMER_NOSTRO = "DELETE FROM CUSTOMER WHERE CUSTOMERCODE = '000001'";

	private static final String DELETE_PRODUCTINHERITANCE = "DELETE FROM PRODUCTINHERITANCE WHERE PRODUCTCONTEXTCODE = '01210DEFAULTEUR'";

	private static final String DELETE_PRODUCTINHERITANCE1 = "DELETE FROM PRODUCTINHERITANCE WHERE PRODUCTCONTEXTCODE = '01005InternalEUR'";

	private static final String DELETE_SWTCUSTOMERDETAIL = "DELETE FROM SWTCUSTOMERDETAIL WHERE CUSTOMERCODE = '000001'";

	private static final String DELETE_SWTCUSTOMERDETAIL1 = "DELETE FROM SWTCUSTOMERDETAIL WHERE CUSTOMERCODE = 'PER001'";

	private static final String DELETE_SWTCUSTOMERDETAIL2 = "DELETE FROM SWTCUSTOMERDETAIL WHERE CUSTOMERCODE = '000005'";

	private static final String DELETE_EXCHANGERATES = "DELETE FROM EXCHANGERATES WHERE CURRENCYRATEID = '1'";

	private static final String DELETE_EXCHANGERATES1 = "DELETE FROM EXCHANGERATES WHERE CURRENCYRATEID = '3'";

	private static final String DELETE_PSEUDONYM = "DELETE FROM PSEUDONYM WHERE PSEUDONYMCODE = 'SPOTPOS'";

	private static final String DELETE_PSEUDONYM1 = "DELETE FROM PSEUDONYM WHERE PSEUDONYMCODE = 'CURRPOS'";

	private static final String DELETE_PSEUDONYMACCMAP = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '572befaa1f4a1175'";

	private static final String DELETE_PSEUDONYMACCMAP1 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '572c15e42013ffff'";

	private static final String DELETE_PSEUDONYMACCMAP2 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '572c41c720deffff'";

	private static final String DELETE_PSEUDONYMACCMAP3 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '572c680f21a702c6'";

	private static final String DELETE_PSEUDONYMACCMAP4 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '572c8f0f2270ffff'";

	private static final String DELETE_PSEUDONYMACCMAP5 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '572cb64c2339ffff'";

	private static final String DELETE_PSEUDONYMACCMAP6 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '572cdcfe24022056'";

	private static final String DELETE_PSEUDONYMACCMAP7 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '572d041924cb0959'";

	private static final String DELETE_PSEUDONYMACCMAP8 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '572d2b412594ffff'";

	private static final String DELETE_PSEUDONYMACCMAP9 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '572d516f265b0293'";

	private static final String DELETE_PSEUDONYMACCMAP10 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '572d776d0014ffff'";

	private static final String DELETE_PSEUDONYMACCMAP11 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '572da05a00dfffff'";

	private static final String DELETE_PSEUDONYMACCMAP12 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '572dc7d901a8ffff'";

	private static final String DELETE_PSEUDONYMACCMAP13 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '572decdd026f0339'";

	private static final String DELETE_PSEUDONYMACCMAP14 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '572e19aa033c2482'";

	private static final String DELETE_PSEUDONYMACCMAP15 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '572e405f0405ffff'";

	private static final String DELETE_PSEUDONYMACCMAP16 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '572e699b04ceffff'";

	private static final String DELETE_PSEUDONYMACCMAP17 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '572e90340597ffff'";

	private static final String DELETE_PSEUDONYMACCMAP18 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '572eb94f0660ffff'";

	private static final String DELETE_PSEUDONYMACCMAP19 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '572ee1a2072b1eb7'";

	private static final String DELETE_PSEUDONYMACCMAP20 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '9e017c2c1813ffff'";

	private static final String DELETE_PSEUDONYMACCMAP21 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '9e01db4218ca14fb'";

	private static final String DELETE_PSEUDONYMACCMAP22 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = 'b7f1a07e08d525f9'";

	private static final String DELETE_PSEUDONYMACCMAP23 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = 'b7f246b60a9802cf'";

	private static final String DELETE_DEBITINTEREST = "DELETE FROM DEBITINTEREST WHERE DEBITINTERESTCODE = '11'";

	private static final String DELETE_CREDITINTEREST = "DELETE FROM CREDITINTEREST WHERE CREDITINTERESTCODE = '11'";

	private static final String DELETE_PENALTYINTEVENTS = "DELETE FROM UBTB_PENALTYINTEVENTS WHERE UBPIEVENTIDPK = '9e0f1f190724ffff'";

	private static final String DELETE_PENALTYINTEVENTS1 = "DELETE FROM UBTB_PENALTYINTEVENTS WHERE UBPIEVENTIDPK = '9e0f202a0767244f'";

	private static final String DELETE_PENALTYINTFEATURE = "DELETE FROM UBTB_PENALTYINTFEATURE WHERE UBACCOUNTID = '01210PER00101'";

	private static final String DELETE_PENALTYINTPROFILE = "DELETE FROM UBTB_PENALTYINTPROFILE WHERE UBPENALINTPROFIDPK = '5'";

	private static final String DELETE_PENALTYINTRATE = "DELETE FROM UBTB_PENALTYINTRATE WHERE UBPENALINTRATEIDPK = '1'";

	private static final String DELETE_PENALTYINTRATE1 = "DELETE FROM UBTB_PENALTYINTRATE WHERE UBPENALINTRATEIDPK = '2'";

	private static final String DELETE_PENALTYINTRATE2 = "DELETE FROM UBTB_PENALTYINTRATE WHERE UBPENALINTRATEIDPK = '4'";

	private static final String DELETE_ACCOUNTSTATEMENTCONFIG = "DELETE FROM UBTB_ACCOUNTSTATEMENTCONFIG WHERE UBACCSTMTCFGPK = '1'";

	private static final String DELETE_ACCOUNTSTATEMENTCONFIG1 = "DELETE FROM UBTB_ACCOUNTSTATEMENTCONFIG WHERE UBACCSTMTCFGPK = '2'";

	private static final String DELETE_ACCOUNTSTATEMENT = "DELETE FROM ACCOUNTSTATEMENT WHERE STATEMENTID = '18'";

	private static final String DELETE_ACCOUNTSTATEMENT1 = "DELETE FROM ACCOUNTSTATEMENT WHERE STATEMENTID = '19'";

	private static final String DELETE_DORMANCY = "DELETE FROM DORMANCYFEATURE WHERE DORMANCYFEATUREID = '9deb67420e500dfe'";

	private static final String DELETE_SHADOWACC = "DELETE FROM SHADOWACCOUNT WHERE ACCOUNTID = '01210PER00101'";

	private static final String DELETE_SHADOWACC1 = "DELETE FROM SHADOWACCOUNT WHERE ACCOUNTID = '0100500000100'";

	private static final String DELETE_BICCODES = "DELETE FROM BICCODES WHERE BICCODE = 'SBSASAJJ999'";

	private static final String DELETE_BICCODES1 = "DELETE FROM BICCODES WHERE BICCODE = 'SBSASAJJ900'";

	private static final String DELETE_BICCODES2 = "DELETE FROM BICCODES WHERE BICCODE = 'SBSASAJJ004'";

	private static final String DELETE_BICCODES3 = "DELETE FROM BICCODES WHERE BICCODE = 'ABNANL2AXXX'";

	private static final String DELETE_BICCODES4 = "DELETE FROM BICCODES WHERE BICCODE = 'ABNAGB2FXXX'";

	private static final String DELETE_BICCODES5 = "DELETE FROM BICCODES WHERE BICCODE = 'AXABDE31XXX'";

	private static final String DELETE_BICCODES6 = "DELETE FROM BICCODES WHERE BICCODE = 'SBZAZAJJICM'";

	private static final String DELETE_BICCODES7 = "DELETE FROM BICCODES WHERE BICCODE = 'SBZAZAJJXXX'";

	private static final String DELETE_BICCODES8 = "DELETE FROM BICCODES WHERE BICCODE = 'BARCSCSCXXX'";

	private static final String DELETE_SETTLINSTR = "DELETE FROM SETTLEMENTINSTRUCTIONS WHERE SETTLEMENTINSTRUCTIONSID = 'SWT0PER001EUR001'";

	private static final String DELETE_SETTLINSTR1 = "DELETE FROM SETTLEMENTINSTRUCTIONS WHERE SETTLEMENTINSTRUCTIONSID = 'SWT0PER001EUR023'";

	private static final String DELETE_SETTLINSTRDETAIL = "DELETE FROM SETTLEMENTINSTRUCTIONSDETAIL WHERE DETAILID = 'SWT0PER001EUR023'";

	private static final String DELETE_SETTLINSTRDETAIL1 = "DELETE FROM SETTLEMENTINSTRUCTIONSDETAIL WHERE DETAILID = 'SWT0PER001EUR030'";

	private static final String UPDATE_MODULECONFIG = "UPDATE CBTB_MODULECONFIGURATION SET CBPARAMVALUE = '' WHERE CBMODULECONFIGURATIONID = 'UBModuleConfig98'";

	private static final String UPDATE_BRANCH = "UPDATE BFTB_BRANCH SET BFBICCODE = 'SBSASAJJ999' WHERE BFBRANCHSORTCODEPK = '99999999'";

	private static final String UPDATE_BRANCH1 = "UPDATE BRANCH SET BICCODE = 'SBSASAJJ999' WHERE BRANCHSORTCODE = '99999999'";

	private static final String DELETE_INTHIST = "DELETE FROM INTERESTHISTORY WHERE ACCOUNTID = '01210PER00101'";

	
	private static final String DELETE_PSEUDONYMACCMAP31 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '9dgdf943fddfdfv999'";
	private static final String DELETE_PSEUDONYMACCMAP32 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '9999999999999'";
	private static final String DELETE_PSEUDONYMACCMAP33 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '9dgdf943fd999dfdfv111'";
	private static final String DELETE_PSEUDONYMACCMAP34 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '9dgdf943fddfdfv111111'";
	private static final String DELETE_PSEUDONYMACCMAP35 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '9dgdf9443111111'";
	private static final String DELETE_PSEUDONYMACCMAP36 = "DELETE FROM PSEUDONYMACCOUNTMAP WHERE ID = '9dgdf9443111122'";
		
	
	public void process(BankFusionEnvironment env) {

       PreparedStatement ps = null;
      Connection connection = null;
		try {

			connection = factory.getJDBCConnection();

			ps = connection.prepareStatement(DELETE_ACCOUNT);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_ACCOUNT1);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_CUSTOMER);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_CUSTOMER_NOSTRO);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PRODUCTINHERITANCE);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PRODUCTINHERITANCE1);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_SWTCUSTOMERDETAIL);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_SWTCUSTOMERDETAIL1);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_SWTCUSTOMERDETAIL2);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_EXCHANGERATES);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_EXCHANGERATES1);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYM);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYM1);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP1);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP2);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP3);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP4);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP5);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP6);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP7);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP8);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP9);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP10);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP11);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP12);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP13);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP14);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP15);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP16);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP17);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP18);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP19);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP20);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP21);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP22);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP23);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_DEBITINTEREST);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_CREDITINTEREST);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PENALTYINTEVENTS);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PENALTYINTEVENTS1);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PENALTYINTFEATURE);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PENALTYINTPROFILE);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PENALTYINTRATE);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PENALTYINTRATE1);
			ps.executeUpdate();
			ps.close();

			ps = connection.prepareStatement(DELETE_PENALTYINTRATE2);
			ps.executeUpdate();
			ps.close();

			ps = connection.prepareStatement(DELETE_ACCOUNTSTATEMENTCONFIG);
			ps.executeUpdate();
			ps.close();

			ps = connection.prepareStatement(DELETE_ACCOUNTSTATEMENTCONFIG1);
			ps.executeUpdate();
			ps.close();

			ps = connection.prepareStatement(DELETE_ACCOUNTSTATEMENT);
			ps.executeUpdate();
			ps.close();

			ps = connection.prepareStatement(DELETE_ACCOUNTSTATEMENT1);
			ps.executeUpdate();
			ps.close();

			ps = connection.prepareStatement(DELETE_DORMANCY);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_SHADOWACC);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_SHADOWACC1);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_BICCODES);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_BICCODES1);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_BICCODES2);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_BICCODES3);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_BICCODES4);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_BICCODES5);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_BICCODES6);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_BICCODES7);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_BICCODES8);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_SETTLINSTR);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_SETTLINSTR1);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_SETTLINSTRDETAIL);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_SETTLINSTRDETAIL1);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(UPDATE_BRANCH);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(UPDATE_MODULECONFIG);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(UPDATE_BRANCH1);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_INTHIST);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP31);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP32);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP33);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP34);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP35);
			ps.executeUpdate();
			ps.close();
			
			ps = connection.prepareStatement(DELETE_PSEUDONYMACCMAP36);
			ps.executeUpdate();
			
			factory.commitTransaction();
			factory.beginTransaction(); //

	}catch (SQLException sqlException) {
		logger.error("populate details()", sqlException);
		factory.rollbackTransaction(); //

		factory.beginTransaction(); //

		throw new BankFusionException(-1, sqlException.getLocalizedMessage());

	} finally {
		if(ps != null) {
			try {
				ps.close();
			} catch (SQLException e) {
				logger.error(ExceptionUtil.getExceptionAsString(e));
			}
		}
	}
}
}
