package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.services.ReadModuleConfigurationRq;
import bf.com.misys.cbs.services.ReadModuleConfigurationRs;
import bf.com.misys.cbs.types.ModuleKeyRq;

import com.blackbear.flatworm.FileCreator;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.common.util.BankFusionPropertySupport;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.ub.common.GetUBConfigLocation;
import com.misys.ub.recon.SmartStream.Banks;
import com.trapedza.bankfusion.bo.refimpl.IBOBicCodes;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_REC_CreateBankExtract;
import com.trapedza.bankfusion.steps.refimpl.IUB_REC_CreateBankExtract;

public class UB_REC_CreateBankExtractFatom extends AbstractUB_REC_CreateBankExtract implements IUB_REC_CreateBankExtract {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("deprecation")
	public UB_REC_CreateBankExtractFatom(BankFusionEnvironment env) {
		super(env);
	}

	private static final String RECON_SMARTSTREAM_EXTRACT_FORMAT_LOCATION = "conf/business/recon/SmartStreamExtractFormat.xml";
	private static final String RECON_MODULE_NAME = "REC";
	private static final String RECON_EXTRACT_PARAM_NAME = "SmartStreamExtractLoc";
	private static final String READ_MODULE_CONFIGURATION_SERVICE = "CB_CMN_ReadModuleConfiguration_SRV";
	private transient final static Log logger = LogFactory.getLog(UB_REC_CreateBankExtractFatom.class.getName());
	private static final String BANK_EXTRACT_FILE_NAME = "BankExtract.imp";

	@SuppressWarnings("unchecked")
	public void process(BankFusionEnvironment env) {

		// Get SmartStream Extract Format Definition File
		//String path = System.getProperty("BFconfigLocation");
		String path = GetUBConfigLocation.getUBConfigLocation();
		String smartStreamExtractFormatFile = (path + RECON_SMARTSTREAM_EXTRACT_FORMAT_LOCATION);
		logger.info("Bank Extract SmartStream format loaded from " + smartStreamExtractFormatFile);

		// Get Bank Extract Output File Name & Location
		ReadModuleConfigurationRq readModuleConfRq = new ReadModuleConfigurationRq();
		ModuleKeyRq moduleKeyRq = new ModuleKeyRq();
		moduleKeyRq.setModuleId(RECON_MODULE_NAME);
		moduleKeyRq.setKey(RECON_EXTRACT_PARAM_NAME);
		readModuleConfRq.setModuleKeyRq(moduleKeyRq);
		HashMap inputParams = new HashMap();
		inputParams.put("ReadModuleConfigurationRq", readModuleConfRq);
		HashMap outputParams = MFExecuter.executeMF(READ_MODULE_CONFIGURATION_SERVICE, env, inputParams);
		ReadModuleConfigurationRs readModuleConfRs = (ReadModuleConfigurationRs) (outputParams.get("ReadModuleConfigurationRs"));
		String smartStreamStaticExtractLocation = readModuleConfRs.getModuleConfigDetails().getValue();
		String bankExtractFile = (smartStreamStaticExtractLocation + BANK_EXTRACT_FILE_NAME);
		// Get Bank Data
		String Query = " SELECT B." + IBOBicCodes.BICCODE + " AS " + CommonConstants.getTagName(IBOBicCodes.BICCODE) + ",B."
				+ IBOBicCodes.NAME + " AS " + CommonConstants.getTagName(IBOBicCodes.NAME) + " " + CommonConstants.FROM + " "
				+ IBOBicCodes.BONAME + " B ";

		List<SimplePersistentObject> ResultSet = null;
		List<Banks> bankRecords = new ArrayList<Banks>();
		try {
			ArrayList queryParams = new ArrayList();

			ResultSet = BankFusionThreadLocal.getPersistanceFactory().executeGenericQuery(Query, queryParams, null, false);

			Iterator<SimplePersistentObject> resultIterator = ResultSet.iterator();
			while (resultIterator.hasNext()) {
				SimplePersistentObject simplePersistentObject = (SimplePersistentObject) resultIterator.next();
				Map data = simplePersistentObject.getDataMap();
				Banks bank = new Banks();
				bank.setPARTNER_REC_TYPE("1");
				bank.setPARTNER_MESSAGE("");
				bank.setPARTNER_BANK_CODE((String) (data.get(CommonConstants.getTagName(IBOBicCodes.BICCODE))));
				bank.setPARTNER_SHORT_NAME("");
				bank.setPARTNER_FULL_NAME((String) (data.get(CommonConstants.getTagName(IBOBicCodes.NAME))));
				bank.setPARTNER_TYPE("1");
				bank.setPARTNER_SWIFT_ADDR1((String) (data.get(CommonConstants.getTagName(IBOBicCodes.BICCODE))));
				bankRecords.add(bank);
			}
			this.writeBankExtract(bankRecords, smartStreamExtractFormatFile, bankExtractFile);

		} catch (Exception e) {
			logger.error(ExceptionUtil.getExceptionAsString(e));
		}

	}

	public void writeBankExtract(List<Banks> bankRecords, String SmartStreamExtractFormatFile, String BankExtractFile) {
		FileCreator fileCreator = null;
		try {
			fileCreator = new FileCreator(SmartStreamExtractFormatFile, BankExtractFile);
			try{
				fileCreator.open();
			}catch (Exception e) {
				//String path = System.getProperty("BFconfigLocation");
				String path = GetUBConfigLocation.getUBConfigLocation();
				BankExtractFile = path+BANK_EXTRACT_FILE_NAME;
				fileCreator = new FileCreator(SmartStreamExtractFormatFile, BankExtractFile);
				fileCreator.open();
				logger.error(ExceptionUtil.getExceptionAsString(e));
			}
			fileCreator.setRecordSeperator("\r\n");
			Banks bank = new Banks();
			Banks bankRecord = new Banks();
			Iterator<Banks> bankRecordsIterator = bankRecords.iterator();
			while (bankRecordsIterator.hasNext()) {
				bankRecord = bankRecordsIterator.next();
				Object ob = new Object();
				ob = bankRecord.getPARTNER_REC_TYPE();
				if((ob!=null) && (bankRecord.getPARTNER_REC_TYPE() == "1")) {
					fileCreator.setBean("bank", bank);
					bank.setPARTNER_REC_TYPE(bankRecord.getPARTNER_REC_TYPE());
					ob = bankRecord.getPARTNER_BANK_CODE();
					bank.setPARTNER_BANK_CODE(ob!=null?bankRecord.getPARTNER_BANK_CODE():"");
					ob = bankRecord.getPARTNER_SHORT_NAME();
					bank.setPARTNER_SHORT_NAME(ob!=null?bankRecord.getPARTNER_SHORT_NAME():"");
					ob = bankRecord.getPARTNER_FULL_NAME();
					bank.setPARTNER_FULL_NAME(ob!=null?bankRecord.getPARTNER_FULL_NAME():"");
					bank.setPARTNER_ADDRESS(bankRecord.getPARTNER_ADDRESS());
					ob = bankRecord.getPARTNER_TYPE();
					bank.setPARTNER_TYPE(ob!=null?bankRecord.getPARTNER_TYPE():"");
					ob = bankRecord.getPARTNER_SWIFT_ADDR1();
					bank.setPARTNER_SWIFT_ADDR1(ob!=null?bankRecord.getPARTNER_SWIFT_ADDR1():"");
					bank.setPARTNER_SWIFT_ADDR2(bankRecord.getPARTNER_SWIFT_ADDR2());
					bank.setPARTNER_SWIFT_ADDR3(bankRecord.getPARTNER_SWIFT_ADDR3());
					bank.setPARTNER_SWIFT_ADDR4(bankRecord.getPARTNER_SWIFT_ADDR4());
					bank.setPARTNER_USER_CODE(bankRecord.getPARTNER_USER_CODE());
					fileCreator.write("bankData");
				} else if (bankRecord.getPARTNER_REC_TYPE() == "2") {
					fileCreator.setBean("contactPerson", bank);
					bank.setPARTNER_REC_TYPE(bankRecord.getPARTNER_REC_TYPE());
					bank.setPARTNER_CONTACT(bankRecord.getPARTNER_CONTACT());
					bank.setPARTNER_LANGUAGE(bankRecord.getPARTNER_LANGUAGE());
					bank.setPARTNER_TELL_NR(bankRecord.getPARTNER_TELL_NR());
					bank.setPARTNER_FAX_NR(bankRecord.getPARTNER_FAX_NR());
					bank.setPARTNER_TELEX_NR(bankRecord.getPARTNER_TELEX_NR());
					bank.setPARTNER_EMAIL_ADDR(bankRecord.getPARTNER_EMAIL_ADDR());
					bank.setFILLER(bankRecord.getFILLER());
					fileCreator.write("contactPersonData");
				}
			}
			logger.info("Bank Extract required for Reconciliation by SmartStream created at " + BankExtractFile);
		} catch (Exception e) {
			logger.error(ExceptionUtil.getExceptionAsString(e));
		} finally {
			try {
				fileCreator.close();
			} catch (Exception e) {
				logger.error(ExceptionUtil.getExceptionAsString(e));
			}
		}
	}

}
