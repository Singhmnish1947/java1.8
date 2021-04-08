package com.trapedza.bankfusion.fatoms;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOPseudonymAccountMap;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_RCN_ACCOUNTRECONCONF;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.AddDaysToDate;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_RCN_PseudonymRuleMaintenanceFatom;


/**
 * 
 * @author hemadrihcl
 *
 */
public class PseudonymRuleMaintenanceFatom extends AbstractUB_RCN_PseudonymRuleMaintenanceFatom
{
	private IPersistenceObjectsFactory factory = null;
	
  /**
   * 
   * @param env
   */
	public PseudonymRuleMaintenanceFatom(BankFusionEnvironment env) {
        super(env);
    }
	private final static String ACCOUNT_COLUMNNAME = "ACCOUNTID";

	private final static String PSEUDONAME_COLUMNNAME = "PSEUDONAME";
    
	//CURRENCY
	
	private final static String CURRENCY_COLUMNNAME = "CURRENCY";
	
	
	private final static String CREATEDBY_COLUMNNAME = "CREATEDBY";
	
	
	private final static String CREATEDDTTM_COLUMNNAME = "CREATEDDTTM";
	
	private final static String RECONFREQ_COLUMNNAME = "COLRECONFREQ";
	
	
	private final static String RECONFREQUNIT_COLUMNNAME = "COLRECONFREQUNIT";

	//NEXTRECONDTTM
	private final static String NEXTRECONDTTM_COLUMNNAME = "NEXTRECONDTTM";
	
	
	//CREATEDBY
	//CREATEDDTTM
	
	/**
	 * 
	 */
	private static final String PSEUDONYMACCMAPWHERECLAUSE = " SELECT DISTINCT T1."
			+ IBOAttributeCollectionFeature.ACCOUNTID + " AS ACCOUNTID ,"
			+ "T1." + IBOAttributeCollectionFeature.PRODUCTCONTEXTCODE
			+ " AS SUBPRODUCTID, " + "T1."
			+ IBOAttributeCollectionFeature.PRODUCTID + " AS PRODUCTID, "
			+ "T1." + IBOAttributeCollectionFeature.ISOCURRENCYCODE
			+ " AS CURRENCY ," + "T1."
			+ IBOAttributeCollectionFeature.ACCOUNTDESCRIPTION
			+ " AS ACCOUNTDESCRIPTION ," + "T2."
			+ IBOPseudonymAccountMap.PSEUDONAME + " AS PSEUDONAME " + " FROM  "
			+ IBOAttributeCollectionFeature.BONAME + " T1 , "
			+ IBOPseudonymAccountMap.BONAME + " T2 " + " WHERE T1."
			+ IBOAttributeCollectionFeature.ACCOUNTID + "=T2."
			+ IBOPseudonymAccountMap.ACCOUNTID + " AND T2."
			+ IBOPseudonymAccountMap.PSEUDONAME + "=?";

	/**
	 * 
	 */
	private static final String FETCH_CONFIGURED_PSUEDONAME = " SELECT DISTINCT T1."
		+ IBOAttributeCollectionFeature.ACCOUNTID + " AS ACCOUNTID ,"
		+ "T1." + IBOAttributeCollectionFeature.PRODUCTCONTEXTCODE
		+ " AS SUBPRODUCTID, " + "T1."
		+ IBOAttributeCollectionFeature.PRODUCTID + " AS PRODUCTID, "
		+ "T1." + IBOAttributeCollectionFeature.ISOCURRENCYCODE
		+ " AS CURRENCY ," + "T1."
		+ IBOAttributeCollectionFeature.ACCOUNTDESCRIPTION
		+ " AS ACCOUNTDESCRIPTION ," + "T2."
		+ IBOUB_RCN_ACCOUNTRECONCONF.PSEUDONYMCODE + " AS PSEUDONAME " 
		+ " ,T2."+  IBOUB_RCN_ACCOUNTRECONCONF.CREATEDBY + " AS CREATEDBY"
		+ " ,T2."+  IBOUB_RCN_ACCOUNTRECONCONF.CREATEDDTTM + " AS CREATEDDTTM"
		+ " ,T2."+  IBOUB_RCN_ACCOUNTRECONCONF.RECONFREQ + " AS COLRECONFREQ"
		+ " ,T2."+  IBOUB_RCN_ACCOUNTRECONCONF.RECONFREQUNIT + " AS COLRECONFREQUNIT"
		+ " ,T2."+  IBOUB_RCN_ACCOUNTRECONCONF.NEXTRECONDTTM + " AS NEXTRECONDTTM"
		+ " FROM  "
		+ IBOAttributeCollectionFeature.BONAME + " T1 , "
		+ IBOUB_RCN_ACCOUNTRECONCONF.BONAME + " T2 " + " WHERE T1."
		+ IBOAttributeCollectionFeature.ACCOUNTID + "=T2."
		+ IBOUB_RCN_ACCOUNTRECONCONF.ACCOUNTID + " AND T2."
		+ IBOUB_RCN_ACCOUNTRECONCONF.PSEUDONYMCODE + "=?";
	
	
	/**
	 * 
	 */
	private static final String FETCH_AVAILABLE_PSUEDONAME = PSEUDONYMACCMAPWHERECLAUSE + " AND T2."
			+ IBOPseudonymAccountMap.ACCOUNTID + " NOT IN ( SELECT T3."
			+ IBOUB_RCN_ACCOUNTRECONCONF.ACCOUNTID + " FROM "
			+ IBOUB_RCN_ACCOUNTRECONCONF.BONAME + " T3 WHERE T3."
			+ IBOUB_RCN_ACCOUNTRECONCONF.PSEUDONYMCODE + " =?)";
	
	
	private final static String ADD_MODE = "ADD";
	private final static String MOVE_RIGHT_MODE = "MOVERIGHT";
	private final static String MOVE_LEFT_MODE = "MOVELEFT";
	private final static String SAVE_MODE = "SAVE";
	private final static String LOAD_MODE = "LOAD";
	private final static String EDIT_MODE = "EDIT";
	private final static String VALIDATE_MODE = "VALIDATE";
	
	private final static String FETCH_CONFIG_DETAILS = " WHERE "
			+ IBOUB_RCN_ACCOUNTRECONCONF.PSEUDONYMCODE + " =?";
    
	private final static Integer ATLEAST_ONE_MATCH_NEEDED = 40112153;
	
	private final static Integer FREQUENCY_NUM_SHOULD_BE_BETWEEN_1_TO_99 = 40112160;
	
	private final static Integer NO_CONFIGURATION_DETAILS_AVAILABLE_TO_SAVE = 40112162;
	

	/* (non-Javadoc)
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractUB_RCN_PseudonymRuleMaintenanceFatom#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 */
	public void process(BankFusionEnvironment env){
		 factory = BankFusionThreadLocal.getPersistanceFactory();
		 String modeType = getF_IN_Mode();
		 String pseudoName = getF_IN_Psuedoname();
		 String subMode = getF_IN_SubMode();
		 int frequencyUnit=getF_IN_FrequencyUnit();
		if (modeType.equalsIgnoreCase(ADD_MODE)) {
			if(!subMode.equals(EDIT_MODE))
			{
			ArrayList<String> params = new ArrayList<String>();
			params.add(pseudoName);

			List<SimplePersistentObject> accountMapList = null;
			
			accountMapList = factory.executeGenericQuery(
					PSEUDONYMACCMAPWHERECLAUSE, params, null, true);

			VectorTable outVector = new VectorTable();
			int firstRow = 0;
			for (SimplePersistentObject accountList : accountMapList) {
				Map rowData = new HashMap();
				rowData.putAll(accountList.getDataMap());
				if(firstRow ==0)
					rowData.put(CommonConstants.SELECT, Boolean.TRUE);
				else
					rowData.put(CommonConstants.SELECT, Boolean.FALSE);
				firstRow ++;
				rowData.remove("boID");
				rowData.remove("BOID");
				rowData.remove("VERSIONNUM");
				rowData.remove("versionNum");
				rowData.put(CREATEDBY_COLUMNNAME, BankFusionThreadLocal.getUserSession().getUserId());
				rowData.put(CREATEDDTTM_COLUMNNAME, SystemInformationManager.getInstance().getBFBusinessDateTime());
				rowData.put(RECONFREQ_COLUMNNAME, CommonConstants.EMPTY_STRING);
				rowData.put(RECONFREQUNIT_COLUMNNAME, new Integer(0));
				rowData.put(NEXTRECONDTTM_COLUMNNAME, SystemInformationManager.getInstance().getBFBusinessDateTime());
				VectorTable rowDataVector = new VectorTable(rowData);
				outVector.addAll(rowDataVector);
			}
				setF_OUT_OutVector(outVector);
				setF_OUT_InVector(new VectorTable());
			}
			else
			{
				setF_OUT_OutVector(getF_IN_OutVector());
				setF_OUT_InVector(getF_IN_InVector());
				
			}
		}
		else if(modeType.equals(LOAD_MODE))
		{
			ArrayList<String> params = new ArrayList<String>();
			params.add(pseudoName);
			
			List<IBOUB_RCN_ACCOUNTRECONCONF> reconConfig = factory.findByQuery(
					IBOUB_RCN_ACCOUNTRECONCONF.BONAME, FETCH_CONFIG_DETAILS,
					params, null, true);
			
			if(reconConfig!=null && !reconConfig.isEmpty())
			{
				setF_OUT_IsAmountMatch(reconConfig.get(0).isF_ISAMOUNTMATCH());
				setF_OUT_IsNarrativeMatch(reconConfig.get(0).isF_ISNARRATIVEMATCH());
				setF_OUT_IsReferenceMatch(reconConfig.get(0).isF_ISTXNREFMATCH());
				setF_OUT_Frequency(reconConfig.get(0).getF_RECONFREQ());
				setF_OUT_FrequencyUnit(reconConfig.get(0).getF_RECONFREQUNIT());
			}
			
			List<SimplePersistentObject> accountMapList = null;

			accountMapList = factory.executeGenericQuery(
					FETCH_CONFIGURED_PSUEDONAME, params, null, true);

			VectorTable outVector = new VectorTable();
			int rowCounter = 0;
			/**
			 * 
			 */
			for (SimplePersistentObject accountList : accountMapList) {
				Map rowData = new HashMap();
				rowData.putAll(accountList.getDataMap());
				rowData.remove("boID");
				rowData.remove("BOID");
				rowData.remove("VERSIONNUM");
				rowData.remove("versionNum");
				
				if(rowCounter == 0)
					rowData.put(CommonConstants.SELECT, Boolean.TRUE);
				else
					rowData.put(CommonConstants.SELECT, Boolean.FALSE);
				
				rowCounter ++;
				VectorTable rowDataVector = new VectorTable(rowData);
				outVector.addAll(rowDataVector);
			}
			
			setF_OUT_OutVector(outVector);
			
			params.add(pseudoName);
			accountMapList = factory.executeGenericQuery(
					FETCH_AVAILABLE_PSUEDONAME, params, null, true);
			
			
			VectorTable invector = new VectorTable();
			rowCounter = 0;
			/**
			 * 
			 */
			for (SimplePersistentObject accountList : accountMapList) {
				Map rowData = new HashMap();
				rowData.putAll(accountList.getDataMap());
				rowData.remove("boID");
				rowData.remove("BOID");
				rowData.remove("VERSIONNUM");
				rowData.remove("versionNum");
				
				if(rowCounter == 0)
					rowData.put(CommonConstants.SELECT, Boolean.TRUE);
				else
					rowData.put(CommonConstants.SELECT, Boolean.FALSE);
				
				rowData.put(CREATEDBY_COLUMNNAME, BankFusionThreadLocal.getUserSession().getUserId());
				rowData.put(CREATEDDTTM_COLUMNNAME, SystemInformationManager.getInstance().getBFBusinessDateTime());
				rowData.put(RECONFREQ_COLUMNNAME, CommonConstants.EMPTY_STRING);
				rowData.put(RECONFREQUNIT_COLUMNNAME, new Integer(0));
				rowData.put(NEXTRECONDTTM_COLUMNNAME, SystemInformationManager.getInstance().getBFBusinessDateTime());
				
				rowCounter ++;
				VectorTable rowDataVector = new VectorTable(rowData);
				invector.addAll(rowDataVector);
			}
			setF_OUT_InVector(invector);
		}
	// If mode type = MR(Move values form left to Right)
		else if(modeType.equalsIgnoreCase(MOVE_RIGHT_MODE)){
		 
		 VectorTable invector = getF_IN_InVector();
		 VectorTable outVector = getF_IN_OutVector();
		 VectorTable newInVector = new VectorTable();
		 int rowCounter = 0;
		 /**
		  * 
		  */
		 int inVectorSize = invector.size();
		 
		 /**
		  * 
		  */
		 for(int i=0;i<inVectorSize;i++)
		 {
			Map rowTags =  invector.getRowTags(i);
			boolean selected = (Boolean) rowTags.get(CommonConstants.SELECT);
			rowTags.remove("VERSIONNUM");
			rowTags.remove("versionNum");
			VectorTable rowDataVector = new VectorTable(rowTags);
			if(selected)
			{
				outVector.addAll(rowDataVector);
			}
			else
			{
				if(rowCounter == 0)
					rowTags.put(CommonConstants.SELECT, Boolean.TRUE);
				else
					rowTags.put(CommonConstants.SELECT, Boolean.FALSE);
				
				newInVector.addAll(rowDataVector);
				rowCounter ++;
			}
		 }
		 setF_OUT_InVector(newInVector);
		 setF_OUT_OutVector(outVector);
		 
	 }
	// If mode type = ML(Move values form Right to Left)
		else if(modeType.equalsIgnoreCase(MOVE_LEFT_MODE)){
		 VectorTable outVector = getF_IN_OutVector();
		 VectorTable inVector = getF_IN_InVector();
		 VectorTable newOutVector = new VectorTable();
		 int rowCounter = 0;
		 /**
		  * 
		  */
		 int inVectorSize = outVector.size();
		
		 for(int i=0;i<inVectorSize;i++)
		 {
			Map rowTags =  outVector.getRowTags(i);
			boolean selected = (Boolean) rowTags.get(CommonConstants.SELECT);
			VectorTable rowDataVector = new VectorTable(rowTags);
			 rowTags.remove("VERSIONNUM");
			 rowTags.remove("versionNum");
			if(selected)
			{
				inVector.addAll(rowDataVector);
			}
			else
			{
				if(rowCounter == 0)
					rowTags.put(CommonConstants.SELECT, Boolean.TRUE);
				else
					rowTags.put(CommonConstants.SELECT, Boolean.FALSE);
				newOutVector.addAll(rowDataVector);
				rowCounter ++;
			}
		 }
		 setF_OUT_InVector(inVector);
		 setF_OUT_OutVector(newOutVector);
	 }
	else if(modeType.equalsIgnoreCase(VALIDATE_MODE)){
		 checkForMandatoryDetails(frequencyUnit,env);
		 validateExistingRecord(getF_IN_OutVector() , pseudoName , subMode);
		 }
	else if(modeType.equalsIgnoreCase(SAVE_MODE)){		
		 checkForMandatoryDetails(frequencyUnit,env);
		 validateExistingRecord(getF_IN_OutVector() , pseudoName , subMode);
		 deleteExistingPseduoNameRecords(pseudoName);
		 persistNewRecords(getF_IN_OutVector() , isF_IN_IsAmountMatch(), isF_IN_IsReferenceMatch(),isF_IN_IsNarrativeMatch() ,subMode , getF_IN_Frequency() , getF_IN_FrequencyUnit() );
		
	 }
}

	/**
	 * @param frequencyUnit
	 * @param env
	 */
	private void checkForMandatoryDetails(int frequencyUnit,BankFusionEnvironment env) {
		
		if(getF_IN_OutVector() == null || !getF_IN_OutVector().hasData())
		{
			EventsHelper.handleEvent(NO_CONFIGURATION_DETAILS_AVAILABLE_TO_SAVE, new Object[] { CommonConstants.EMPTY_STRING }, new HashMap(), env);
		}
		else if(isAllMatchesEmpty(isF_IN_IsAmountMatch(), isF_IN_IsReferenceMatch(),isF_IN_IsNarrativeMatch()))
		{
			  EventsHelper.handleEvent(ATLEAST_ONE_MATCH_NEEDED, new Object[] { CommonConstants.EMPTY_STRING }, new HashMap(), env);
		}
		else if(!(frequencyUnit > 0 && frequencyUnit < 100))
		{
			  EventsHelper.handleEvent(FREQUENCY_NUM_SHOULD_BE_BETWEEN_1_TO_99, new Object[] { CommonConstants.EMPTY_STRING }, new HashMap(), env);
		}	
		
	}
	
	private final static String CHECK_ACCOUNT_ALREADY_EXIST = " WHERE "
			+ IBOUB_RCN_ACCOUNTRECONCONF.ACCOUNTID + " =? AND "
			+ IBOUB_RCN_ACCOUNTRECONCONF.PSEUDONYMCODE + "<>?";
	
	private final static String PSEUDONAME_ALREADY_EXIST = " WHERE "
		+ IBOUB_RCN_ACCOUNTRECONCONF.PSEUDONYMCODE + " =? ";
		
	/**
	 * 40112154
	 * 
	 */
	private final static Integer ACCOUNT_ALREADY_CONFIGURED = 40112154;
	
	
	
	/**
	 * @param configuredAccounts
	 * @param pseudoName
	 * @param subMode
	 */
	private void validateExistingRecord(VectorTable configuredAccounts, String pseudoName, String subMode) {
		ArrayList<String> params = new ArrayList<String>();
		List<IBOUB_RCN_ACCOUNTRECONCONF> existingList;
		/**
		 * 
		 */
		if(!subMode.equals(EDIT_MODE))
		{
			params.add(pseudoName);
			existingList = factory.findByQuery(
					IBOUB_RCN_ACCOUNTRECONCONF.BONAME,
					PSEUDONAME_ALREADY_EXIST, params, null, true);
			
			if(existingList != null && !existingList.isEmpty())
			{
				EventsHelper.handleEvent(ACCOUNT_ALREADY_CONFIGURED, new Object[] {pseudoName}, new HashMap(), BankFusionThreadLocal.getBankFusionEnvironment());
			}
			
		}
		
		String accountId = CommonConstants.EMPTY_STRING;
		
	
		for(int i=0;i<configuredAccounts.size();i++)
		{
			params.clear();
			Map rowTags = configuredAccounts.getRowTags(i);
			accountId = (String) rowTags.get(ACCOUNT_COLUMNNAME);
			params.add(accountId);
			params.add(pseudoName);
			
			existingList = factory.findByQuery(
					IBOUB_RCN_ACCOUNTRECONCONF.BONAME,
					CHECK_ACCOUNT_ALREADY_EXIST, params, null, true);
			
			if(existingList !=null && !existingList.isEmpty())
			{
				 EventsHelper.handleEvent(ACCOUNT_ALREADY_CONFIGURED, new Object[] {accountId}, new HashMap(), BankFusionThreadLocal.getBankFusionEnvironment());
			}
		}
	}
	


	/**
	 * 
	 * @return
	 */
	private boolean isAllMatchesEmpty(boolean isAccountMatch, boolean isTxnRefMatch, boolean isNarrativeMatch)
	 {
		 if(!isAccountMatch && !isTxnRefMatch && !isNarrativeMatch)
			  return true;
		 
		return false;
	 }
	
	/**
	 * 
	 */
	private final static String DELETE_EXISTING_PSEUDONAME_CONFIG = " WHERE "
			+ IBOUB_RCN_ACCOUNTRECONCONF.PSEUDONYMCODE + "=?";
	/**
	 * 
	 * @param pseudoName
	 */
	private void deleteExistingPseduoNameRecords(String pseudoName) {
		ArrayList<String> params = new ArrayList<String>();
		params.add(pseudoName);
		BankFusionThreadLocal.getPersistanceFactory().bulkDelete(
				IBOUB_RCN_ACCOUNTRECONCONF.BONAME,
				DELETE_EXISTING_PSEUDONAME_CONFIG, params);
	}
	
	

	/**
	 * 
	 * @param configuredAccounts
	 * @param isAccountMatch
	 * @param isTxnRefMatch
	 * @param isNarrativeMatch
	 * @param subMode 
	 * @param integer 
	 * @param string 
	 */
	private void persistNewRecords(VectorTable configuredAccounts,boolean isAccountMatch, boolean isTxnRefMatch, boolean isNarrativeMatch, String subMode, String frequency, Integer ferquencyUnit)
	{
		IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
		for (int i = 0; i < configuredAccounts.size(); i++)
		{
			Map rowTags = configuredAccounts.getRowTags(i);
			IBOUB_RCN_ACCOUNTRECONCONF reconf = (IBOUB_RCN_ACCOUNTRECONCONF) factory.getStatelessNewInstance(IBOUB_RCN_ACCOUNTRECONCONF.BONAME);
			reconf.setF_ACCOUNTID((String) rowTags.get(ACCOUNT_COLUMNNAME));
			reconf.setF_PSEUDONYMCODE((String) rowTags.get(PSEUDONAME_COLUMNNAME));
			reconf.setF_ISACTIVE(Boolean.TRUE);
			reconf.setF_ISAMOUNTMATCH(isAccountMatch);
			reconf.setF_ISTXNREFMATCH(isTxnRefMatch);
			reconf.setF_ISNARRATIVEMATCH(isNarrativeMatch);
			reconf.setF_LASTMODIFIEDBY(BankFusionThreadLocal.getUserSession().getUserId());
			reconf.setF_LASTUPDATEDDTTM(SystemInformationManager.getInstance().getBFBusinessDateTime());
			reconf.setF_ISOCURRENCYCODE((String) rowTags.get(CURRENCY_COLUMNNAME));
			reconf.setF_CREATEDBY((String) rowTags.get(CREATEDBY_COLUMNNAME));
			reconf.setF_CREATEDDTTM((Timestamp) rowTags.get(CREATEDDTTM_COLUMNNAME));
			String actualFrequency = (String) rowTags.get(RECONFREQ_COLUMNNAME);
			Integer actualFrequencyUnit = (Integer) rowTags.get(RECONFREQUNIT_COLUMNNAME);
			reconf.setF_RECONFREQ(frequency);
			reconf.setF_RECONFREQUNIT(ferquencyUnit);
			Timestamp nextReconDate =  (Timestamp) rowTags.get(NEXTRECONDTTM_COLUMNNAME);
			if(!actualFrequency.equals(frequency) || actualFrequencyUnit != ferquencyUnit)
			{
				reconf.setF_NEXTRECONDTTM(calculateNextReconDate(SystemInformationManager.getInstance().getBFBusinessDateTime() , frequency , ferquencyUnit));	
			}
			else
			{
				reconf.setF_NEXTRECONDTTM(nextReconDate);
			}
			

			factory.create(IBOUB_RCN_ACCOUNTRECONCONF.BONAME, reconf);
		}
	}
	
	/**
	 * 
	 * @param nextReconDTTM
	 * @param frequency
	 * @param frequencyUnit
	 * @return
	 */
	private Timestamp calculateNextReconDate(Timestamp nextReconDTTM , String frequency , int frequencyUnit) {
		Date nextReconDate = new Date(nextReconDTTM.getTime());
		nextReconDate = AddDaysToDate.run(nextReconDate, frequencyUnit);
		return new Timestamp(nextReconDate.getTime());
	}
	

}
