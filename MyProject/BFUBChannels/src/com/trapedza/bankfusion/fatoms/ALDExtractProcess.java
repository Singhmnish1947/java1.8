/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: ALDExtractProcess.java,v.1.0,May 20, 2009 2:46:02 PM ayerla
 *
 */
package com.trapedza.bankfusion.fatoms;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.common.GetUBConfigLocation;
import com.misys.ub.common.almonde.AlmondeConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOCB_GCD_CodesView;
import com.trapedza.bankfusion.bo.refimpl.IBOProduct;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_ALD_EXTRACTIONCFG;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_ALD_INFCFGPARAM;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.features.FeatureIDs;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.products.ProductFactoryProvider;
import com.trapedza.bankfusion.servercommon.products.SimpleRuntimeProduct;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ALD_ExtractProcess;
import com.trapedza.bankfusion.utils.BankFusionMessages;
import com.trapedza.bankfusion.utils.GUIDGen;

/**
 * @author ayerla
 * @date May 20, 2009
 * @project Universal Banking
 * @Description: The ALDExtractProcess Class takes the Extraction Type and Abstract Product List for
 *               Almonde Extraction.
 */

/**
 * @author ssarpa
 * @date Jun 12, 2009
 * @project Universal Banking
 * @Description: Code Changes to support Invidual Job Extract and continuation of extraction incase
 *               of failures.
 */

@SuppressWarnings("serial")
public class ALDExtractProcess extends AbstractUB_ALD_ExtractProcess {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


    private Properties almondeExtractProperties = null; //NOPMD
    private IPersistenceObjectsFactory factory = null;//NOPMD
    private BankFusionEnvironment environment = null;//NOPMD
    private ExtractJobExecuter executer = null;//NOPMD
    private Map<String, String> staticDataMap = null;//NOPMD
    private Map<String, String> currentDataMap = null;//NOPMD
    private static int noofSuccess = 0;//NOPMD
    private static int noofFailures = 0;//NOPMD
    private List<String> fileNames = new ArrayList<String>(); //NOPMD
	@SuppressWarnings("unused")
	private String CBCODEIDPK = "CBCODEIDPK";
    
    private VectorTable fileNameVector = new VectorTable();
    
     
    private static final transient Log logger = LogFactory.getLog(ALDExtractProcess.class.getName());

    private static final String CFG_PARAM_WHERECLAUSE = CommonConstants.WHERE + CommonConstants.SPACE
            + IBOUB_ALD_INFCFGPARAM.MODULENAME + CommonConstants.EQUAL + CommonConstants.SPACE + CommonConstants.QUESTION;

    /**
     * @param env
     */
    @SuppressWarnings("deprecation")
	public ALDExtractProcess(BankFusionEnvironment env) {
        super(env);

        staticDataMap = new HashMap<String, String>();
        currentDataMap = new HashMap<String, String>();
        
    }

    /**
     * 
     * @see com.trapedza.bankfusion.steps.refimpl.AbstractUB_ALD_ExtractProcess#process(com.trapedza.
     *      bankfusion.servercommon.commands.BankFusionEnvironment)
     */
    @SuppressWarnings("deprecation")
	public void process(BankFusionEnvironment env) throws BankFusionException {
        try {
            factory = BankFusionThreadLocal.getPersistanceFactory();
            environment = env;
            executer = new ExtractJobExecuter(env);
            executer.setF_IN_IgnoreException(isF_IN_IsEOD());
            String extractfor = getF_IN_ExtractFor();
            StringBuffer propertiesPath = new StringBuffer(GetUBConfigLocation.getUBConfigLocation());
            propertiesPath.append(AlmondeConstants.PROPERTIES_FILE_LOCATION);
            fileNameVector = new VectorTable();
            
            if (extractfor.equalsIgnoreCase("ALM"))
            	extractfor = AlmondeConstants.ALM;
            else if(extractfor.equalsIgnoreCase("BASEL"))
            	extractfor = AlmondeConstants.BASEL2;
            
            try {
                almondeExtractProperties = loadProperties(new FileInputStream(propertiesPath.toString()));
            }
            catch (FileNotFoundException e) {
                if (!isF_IN_IsEOD()) {
                    throw new BankFusionException(40413024, BankFusionMessages.getFormattedMessage(
                            40413024, new Object[] { propertiesPath.toString() }));
                }
                else {
                    logger.error(BankFusionMessages.getFormattedMessage( 40413024,
                            new Object[] { propertiesPath.toString() }));
                }
            }

            CB_CMN_ModuleConfigurationFatom moduleConf = new CB_CMN_ModuleConfigurationFatom(env);
           // moduleConf.setF_IN_doCache(false);
            moduleConf.setF_IN_ModuleName(AlmondeConstants.MODULE_NAME);
            moduleConf.setF_IN_ParamName(AlmondeConstants.EXTRACTION_PATH);
            moduleConf.process(env);

            cleanCFGPARAMSTable();
            setBusinessDateParam();
            setALDMasterPropLocParam(propertiesPath.toString());
            setFileLocationParam(moduleConf.getF_OUT_StringValue());
            setExtractionForParam(extractfor);

            noofFailures = 0;
            noofSuccess = 0;

            if (extractfor.equals(AlmondeConstants.ALM)) {

                processALMExtract();
            }
            else if (extractfor.equals(AlmondeConstants.BASEL2)) {
                processBasel2Extract();
            }
        }

        finally {
            factory = null;
            executer = null;
        }

    }

    /**
     * Method Description: Invokes Basel II Extraction job using ExtractJobExecuter.
     */
    private void processBasel2Extract() {

        VectorTable abstractProducts = getF_IN_AbstractProducts();
        String key = CommonConstants.EMPTY_STRING;
        String value = CommonConstants.EMPTY_STRING;

        StringBuffer moneyMarketProdList = new StringBuffer();
        StringBuffer niProdList = new StringBuffer();
        StringBuffer lendingProdList = new StringBuffer();
        StringBuffer fxProdList = new StringBuffer();
        StringBuffer otherProdList = new StringBuffer();

        try {
            for (int i = 0; i < abstractProducts.size(); i++) {
                @SuppressWarnings("unchecked")
				Map<String, String> dataRecord = abstractProducts.getRowTags(i);
                if (isF_IN_IsEOD() || isF_IN_isIndividualExtract()) {
                    key = dataRecord.get(CommonConstants.getTagName(IBOUB_ALD_EXTRACTIONCFG.UBEXTRACTDATATYPE)).toString();
                    value = dataRecord.get(CommonConstants.getTagName(IBOUB_ALD_EXTRACTIONCFG.UBEXTRACTDATATYPEATTR)).toString();
                }
                else {
                    value = dataRecord.get(CommonConstants.getTagName(IBOProduct.PRODUCTID)).toString();
                    key = getProductType(value);
                }

                if (key.equals(AlmondeConstants.CONTRACT_MM_NI)) {
                    moneyMarketProdList.append(AlmondeConstants.SINGLEQUOTE);
                    moneyMarketProdList.append(value);
                    moneyMarketProdList.append(AlmondeConstants.SINGLEQUOTE + CommonConstants.COMMA);

                    niProdList.append(AlmondeConstants.SINGLEQUOTE);
                    niProdList.append(value);
                    niProdList.append(AlmondeConstants.SINGLEQUOTE + CommonConstants.COMMA);

                }
                if (key.equals(AlmondeConstants.CONTRACT_MM)) {
                    moneyMarketProdList.append(AlmondeConstants.SINGLEQUOTE);
                    moneyMarketProdList.append(value);
                    moneyMarketProdList.append(AlmondeConstants.SINGLEQUOTE + CommonConstants.COMMA);
                }
                if (key.equals(AlmondeConstants.CONTRACT_NI)) {
                    niProdList.append(AlmondeConstants.SINGLEQUOTE);
                    niProdList.append(value);
                    niProdList.append(AlmondeConstants.SINGLEQUOTE + CommonConstants.COMMA);

                }
                if (key.equals(AlmondeConstants.CONTRACT_LENDING)) {
                    lendingProdList.append(AlmondeConstants.SINGLEQUOTE);
                    lendingProdList.append(value);
                    lendingProdList.append(AlmondeConstants.SINGLEQUOTE + CommonConstants.COMMA);

                }
                if (key.equals(AlmondeConstants.CONTRACT_FX)) {
                    fxProdList.append(AlmondeConstants.SINGLEQUOTE);
                    fxProdList.append(value);
                    fxProdList.append(AlmondeConstants.SINGLEQUOTE + CommonConstants.COMMA);

                }
                if (key.equals(AlmondeConstants.CONTRACT_OTHERS)) {
                    otherProdList.append(AlmondeConstants.SINGLEQUOTE);
                    otherProdList.append(value);
                    otherProdList.append(AlmondeConstants.SINGLEQUOTE + CommonConstants.COMMA);
                }
            }
            if (moneyMarketProdList.length() > 0)
                setMMProdParam(moneyMarketProdList.deleteCharAt(moneyMarketProdList.length() - 1).toString());
            if (niProdList.length() > 0)
                setNIProdParam(niProdList.deleteCharAt(niProdList.length() - 1).toString());
            if (fxProdList.length() > 0)
                setFXProdParam(fxProdList.deleteCharAt(fxProdList.length() - 1).toString());
            if (lendingProdList.length() > 0)
                setLenProdParam(lendingProdList.deleteCharAt(lendingProdList.length() - 1).toString());
            if (otherProdList.length() > 0)
                setOtherProdParam(otherProdList.deleteCharAt(otherProdList.length() - 1).toString());

            if (isF_IN_isIndividualExtract()) {

                processIndividualExtract();
            }

            else {

                // Invoke Static Data Extraction and Current Data other than Contracts

                processStaticData();
                processCurrentDataOthers();

               
                    executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.CONTRACT_MM));
                    executer.process(environment);
                    getStatus(AlmondeConstants.CONTRACT_MM);
                
              
                    executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.CONTRACT_NI));
                    executer.process(environment);
                    getStatus(AlmondeConstants.CONTRACT_NI);
                
                
                    executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.CONTRACT_FX));
                    executer.process(environment);
                    getStatus(AlmondeConstants.CONTRACT_FX);
                
                
                    executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.CONTRACT_LENDING));
                    executer.process(environment);
                    getStatus(AlmondeConstants.CONTRACT_LENDING);

                
            
                    executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.CONTRACT_OTHERS));
                    executer.process(environment);
                    getStatus(AlmondeConstants.CONTRACT_OTHERS);

                
                if(fileNames.size() != 0)
                {
                	Iterator<String> fileNamesItr = fileNames.iterator();
            		while (fileNamesItr.hasNext()) 
            		{
            			String fileName = fileNamesItr.next();
            			constructFileNameVector(fileName, fileNameVector);
            		}
                }  
                setStatus();
            }
        }
        finally {
            abstractProducts = null;
            moneyMarketProdList = null;
            niProdList = null;
            lendingProdList = null;
            fxProdList = null;
            otherProdList = null;
        }
    }

    @SuppressWarnings("deprecation")
	private void processIndividualExtract() {

        VectorTable staticData = getF_IN_StaticData();
        @SuppressWarnings("unused") //NOPMD
		Object[] StatcodeIDPK= staticData.getColumn("CODEIDPK");
        
        staticDataMap.put("AS", AlmondeConstants.ACTIVITYSECTOR_EXTRACT);
        staticDataMap.put("EXR", AlmondeConstants.EXTERNALRATINGS_EXTRACT);
        staticDataMap.put("IR", AlmondeConstants.INTERNALRATINGS_EXTRACT);
        staticDataMap.put("PT", AlmondeConstants.PRODUCTTYPE_EXTRACT);
        staticDataMap.put("RA", AlmondeConstants.RATINGAGENCY_EXTRACT);
        staticDataMap.put("SS", AlmondeConstants.SOURCESYSTEM_EXTRACT);
        staticDataMap.put("ER", AlmondeConstants.EXCHANGERATES_EXTRACT);
        staticDataMap.put("IBC", AlmondeConstants.INTERESTBASECODE_EXTRACT);

        VectorTable currentData = getF_IN_CurrentData();
        @SuppressWarnings("unused") //NOPMD
		Object[] CurrcodeIDPK= currentData.getColumn("CODEIDPK");
        
        currentDataMap.put("PD", AlmondeConstants.PARTYDATA_EXTRACT);
        currentDataMap.put("CPD",AlmondeConstants.CONTRACT_PARTY_XREF);
        currentDataMap.put("FXD", AlmondeConstants.CONTRACT_FX);
        currentDataMap.put("MD", AlmondeConstants.MITIGANT_EXTRACTJOB);
        currentDataMap.put("CMD", AlmondeConstants.CONTRACT_MITIGANT);
        currentDataMap.put("SPD", AlmondeConstants.SPECIFICPROVISION_EXTRACT);
        currentDataMap.put("GPD", AlmondeConstants.GENERALPROVISION_EXTRACT);
        currentDataMap.put("RCA", AlmondeConstants.CONTRACT_ASSET_RATINGS);
        currentDataMap.put("RCFX", AlmondeConstants.CONTRACT_FXDEAL_RATINGS);
        currentDataMap.put("RP", AlmondeConstants.COUNTERPARTY_RATINGS);
        currentDataMap.put("RC", AlmondeConstants.COUNTRY_RATINGS);
        currentDataMap.put("RM", AlmondeConstants.MITIGANT_RATINGS);
        currentDataMap.put("CL", AlmondeConstants.CONTRACT_LENDING);
        currentDataMap.put("CMM", AlmondeConstants.CONTRACT_MM);
        currentDataMap.put("CNI", AlmondeConstants.CONTRACT_NI);
        currentDataMap.put("CCSBOD", AlmondeConstants.CONTRACT_OTHERS);
        currentDataMap.put("CD", AlmondeConstants.CASHFLOW_EXTRACT);    
        
        List<Object> staticDataSelectColumn = Arrays.asList(staticData.getColumn(CommonConstants.getTagName(CommonConstants.SELECT)));
        List<Object> currentDataSelectColumn = Arrays.asList(currentData.getColumn(CommonConstants.getTagName(CommonConstants.SELECT)));
   		   
        if(!staticDataSelectColumn.contains(Boolean.TRUE) && !currentDataSelectColumn.contains(Boolean.TRUE)){
            throw new BankFusionException(40413025,BankFusionMessages.getFormattedMessage(40413025, new Object[]{}));
        }
        try {
            if (staticData != null && staticData.size() > 0) {
                @SuppressWarnings("rawtypes")
				Map staticextractFileMap = staticData.getSelectedRowsTags();
                if (staticextractFileMap != null && staticextractFileMap.size() > 0) {
                    Object[] staticDataSubCodeType = (Object[]) staticextractFileMap.get(IBOCB_GCD_CodesView.REFERENCE);
                    for (int index = 0; index < staticDataSubCodeType.length; index++) {
                        String staticDataParamKey = staticDataMap.get(staticDataSubCodeType[index].toString());
                        executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(staticDataParamKey));
                        executer.process(environment);
                        getStatus(staticDataMap.get(staticDataSubCodeType[index].toString()));
                    }
                    
                }
            }
            
            if (currentData != null && currentData.size() > 0) {
                @SuppressWarnings("rawtypes")
				Map currentExtractFileMap = currentData.getSelectedRowsTags();
                if (currentExtractFileMap != null && currentExtractFileMap.size() > 0) {
                    Object[] currentDataSubCodeType = (Object[]) currentExtractFileMap.get(IBOCB_GCD_CodesView.REFERENCE);
                    for (int index = 0; index < currentDataSubCodeType.length; index++) {
                        String currentDataParamKey = currentDataMap.get(currentDataSubCodeType[index].toString());
                        executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(currentDataParamKey));
                        executer.process(environment);
                        getStatus(currentDataMap.get(currentDataSubCodeType[index].toString()));
                    }
                }
            }
            if(fileNames.size() != 0)
            {
            	Iterator<String> fileNamesItr = fileNames.iterator();
        		while (fileNamesItr.hasNext()) 
        		{
        			String fileName = fileNamesItr.next();
        			constructFileNameVector(fileName, fileNameVector);
        		}
            }
            setStatus();
        }
        finally {
            staticData = null;
            currentData = null;
        }

    }

    /**
     * Method Description: Invokes ALM Extraction job using ExtractJobExecuter.
     */
    private void processALMExtract() {
        setOtherProdParam(almondeExtractProperties.getProperty(AlmondeConstants.OTHER_PROD_LIST));

        if (isF_IN_isIndividualExtract()) {
            processIndividualExtract();
        }

        else {

            // Invoke Static Data Extraction
            processStaticData();

            // Invoke Current Data Others
            processCurrentDataOthers();

            // Extract for Contract MM
            executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.CONTRACT_MM));
            executer.process(environment);
            getStatus(AlmondeConstants.CONTRACT_MM);
            // Extract for Contract NI
            executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.CONTRACT_NI));
            executer.process(environment);
            getStatus(AlmondeConstants.CONTRACT_NI);
            // Extract for Contract Others
            executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.CONTRACT_OTHERS));
            executer.process(environment);
            getStatus(AlmondeConstants.CONTRACT_OTHERS);
            // Extract for Contract Lending
            executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.CONTRACT_LENDING));
            executer.process(environment);
            getStatus(AlmondeConstants.CONTRACT_LENDING);
            // Extract for Contract FX
            executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.CONTRACT_FX));
            executer.process(environment);
            getStatus(AlmondeConstants.CONTRACT_FX);
            // Extract for Contract Mitigant
            executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.CONTRACT_MITIGANT));
            executer.process(environment);
            getStatus(AlmondeConstants.CONTRACT_MITIGANT);
            if(fileNames.size() != 0)
            {
            	Iterator<String> fileNamesItr = fileNames.iterator();
        		while (fileNamesItr.hasNext()) 
        		{
        			String fileName = fileNamesItr.next();
        			constructFileNameVector(fileName, fileNameVector);
        		}
            }  
            setStatus();

        }

    }

    /**
     * Find out the no of files successfully extracted and failed.
     */

    private void getStatus(String jobName) {
        if (executer.isF_IN_Success())
            noofSuccess++;
        else{ 
        	noofFailures++;
        	
        	
        	fileNames.add(jobName);
        }

    }
   
    /**
     * Send the status to user to view.
     */
    private void setStatus() {

        setF_OUT_NoofFilesExtracted(noofSuccess + noofFailures);
        setF_OUT_NoofFilesFailed(noofFailures);
        setF_OUT_NoofFilesSuccess(noofSuccess);
        setF_OUT_FileNamesList(fileNameVector);
    }

    /**
     * Invoke the Current Data and Others.
     */
    private void processCurrentDataOthers() {
    	
    	
        // Extract for Party Data
        executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.PARTYDATA_EXTRACT));
        executer.process(environment);
        getStatus(AlmondeConstants.PARTYDATA_EXTRACT);
        // Extract for Customer Ratings
        executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.COUNTERPARTY_RATINGS));
        executer.process(environment);
        getStatus(AlmondeConstants.COUNTERPARTY_RATINGS);
        // Extract for Country Ratings
        executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.COUNTRY_RATINGS));
        executer.process(environment);
        getStatus(AlmondeConstants.COUNTRY_RATINGS);
        // Extract for Asset Ratings
        executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.CONTRACT_ASSET_RATINGS));
        executer.process(environment);
        getStatus(AlmondeConstants.CONTRACT_ASSET_RATINGS);
        // Extract for FXDeals Ratings
        executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.CONTRACT_FXDEAL_RATINGS));
        executer.process(environment);
        getStatus(AlmondeConstants.CONTRACT_FXDEAL_RATINGS);
        // Extract for Mitigants Ratings
        executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.MITIGANT_RATINGS));
        executer.process(environment);
        getStatus(AlmondeConstants.MITIGANT_RATINGS);
        // Extract for Contract Mitigants Data
        executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.MITIGANT_EXTRACTJOB));
        executer.process(environment);
        getStatus(AlmondeConstants.MITIGANT_EXTRACTJOB);
        
        /*if(!executer.isF_IN_Success())
        {
        	fileNames.add(executer.getF_IN_FILEPATH());
        }*/
        
        // Extract for Cashflows
        executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.CASHFLOW_EXTRACT));
        executer.process(environment);
        getStatus(AlmondeConstants.CASHFLOW_EXTRACT);
        // Extract for Contract Party XRef
        executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.CONTRACT_PARTY_XREF));
        executer.process(environment);
        getStatus(AlmondeConstants.CONTRACT_PARTY_XREF);
     
        
         // Waiting for Delinquency design to be finalised
          
          //Extract for Specific Provision
          executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty
         (AlmondeConstants.SPECIFICPROVISION_EXTRACT)); executer.process(environment);
          getStatus(AlmondeConstants.SPECIFICPROVISION_EXTRACT); //Extract for General Provision
          executer.setF_IN_FILEPATH(almondeExtractProperties
          .getProperty(AlmondeConstants.GENERALPROVISION_EXTRACT)); executer.process(environment);
          getStatus(AlmondeConstants.GENERALPROVISION_EXTRACT);
          
    }
  
    private  VectorTable constructFileNameVector(String fileName, VectorTable vectorTable)
	{	
		Map<String, String> lDataMap = new HashMap<String, String>();
		lDataMap.put("FileName", fileName);
		vectorTable.addAll(new VectorTable(lDataMap));
		return vectorTable;
	}

    /**
     * Extract the Individual Static Data Files.
     */
    private void processStaticData() {

        // Extract for Activity Sector
        executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.ACTIVITYSECTOR_EXTRACT));
        executer.process(environment);
        getStatus(AlmondeConstants.ACTIVITYSECTOR_EXTRACT);

        // Extract for External Ratings
        executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.EXTERNALRATINGS_EXTRACT));
        executer.process(environment);
        getStatus(AlmondeConstants.EXTERNALRATINGS_EXTRACT);
        // Extract for Internal Ratings
        executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.INTERNALRATINGS_EXTRACT));
        executer.process(environment);
        getStatus(AlmondeConstants.INTERNALRATINGS_EXTRACT);
        // Extract for Exchange Rates
        executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.EXCHANGERATES_EXTRACT));
        executer.process(environment);
        getStatus(AlmondeConstants.EXCHANGERATES_EXTRACT);

        // Extract for Interest Base Codes
        executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.INTERESTBASECODE_EXTRACT));
        executer.process(environment);
        getStatus(AlmondeConstants.INTERESTBASECODE_EXTRACT);

        // Extract for Product Type
        executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.PRODUCTTYPE_EXTRACT));
        executer.process(environment);
        getStatus(AlmondeConstants.PRODUCTTYPE_EXTRACT);

        // Extract for Source System
        executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.SOURCESYSTEM_EXTRACT));
        executer.process(environment);
        getStatus(AlmondeConstants.SOURCESYSTEM_EXTRACT);
        
     // Extract for Rating Agencies
        executer.setF_IN_FILEPATH(almondeExtractProperties.getProperty(AlmondeConstants.RATINGAGENCY_EXTRACT));
        executer.process(environment);
        getStatus(AlmondeConstants.RATINGAGENCY_EXTRACT);
    }

    /**
     * Method Description: Reads a property list (key and element pairs) from the input stream,
     * loads it to a Properties Instance and returns the Properties Instance.
     * 
     * @param inputStream
     * @return prop
     * @throws FileNotFoundException
     */
    private Properties loadProperties(InputStream inputStream) throws FileNotFoundException {

        Properties prop = null;
        try {
            prop = new Properties();
            prop.load(inputStream);
        }
        catch (IOException e) {
            throw new FileNotFoundException();
        }
        return prop;
    }

    /**
     * Method Description: Deletes All the Entries from the INFTB_INFCFGPARAM table based on the
     * given Module Name.
     */
    private void cleanCFGPARAMSTable() {
        factory.beginTransaction();
        ArrayList<String> params = new ArrayList<String>();
        params.add(AlmondeConstants.MODULE_NAME.toUpperCase());
        factory.bulkDelete(IBOUB_ALD_INFCFGPARAM.BONAME, CFG_PARAM_WHERECLAUSE, params);
        factory.commitTransaction();
    }

    /**
     * Method Description: Inserts the business Date for a given Module Name.
     */
    private void setBusinessDateParam() {
        factory.beginTransaction();
        IBOUB_ALD_INFCFGPARAM infCfgParamsBO = (IBOUB_ALD_INFCFGPARAM) factory
                .getStatelessNewInstance(IBOUB_ALD_INFCFGPARAM.BONAME);
        infCfgParamsBO.setBoID(GUIDGen.getNewGUID());
        infCfgParamsBO.setF_CFGPARAMKEY(AlmondeConstants.BUSINESSDATE);
        infCfgParamsBO.setF_CFGPARAMVALUE(SystemInformationManager.getInstance().getBFBusinessDateTime().toString());
        infCfgParamsBO.setF_MODULENAME(AlmondeConstants.MODULE_NAME.toUpperCase());
        factory.create(IBOUB_ALD_INFCFGPARAM.BONAME, infCfgParamsBO);
        factory.commitTransaction();
    }

    /**
     * Method Description: Inserts the ALDMasterPropLoc for a given Module Name.
     */
    private void setALDMasterPropLocParam(String aldMasterPropLoc) {
        factory.beginTransaction();
        IBOUB_ALD_INFCFGPARAM infCfgParamsBO = (IBOUB_ALD_INFCFGPARAM) factory
                .getStatelessNewInstance(IBOUB_ALD_INFCFGPARAM.BONAME);
        infCfgParamsBO.setBoID(GUIDGen.getNewGUID());
        infCfgParamsBO.setF_CFGPARAMKEY(AlmondeConstants.ALD_MASTER_PROP_LOC);
        infCfgParamsBO.setF_CFGPARAMVALUE(aldMasterPropLoc);
        infCfgParamsBO.setF_MODULENAME(AlmondeConstants.MODULE_NAME.toUpperCase());
        factory.create(IBOUB_ALD_INFCFGPARAM.BONAME, infCfgParamsBO);
        factory.commitTransaction();
    }

    /**
     * Method Description: Inserts the Extract File Location for a given Module Name.
     * 
     * @param fileLoc
     */
    private void setFileLocationParam(String fileLoc) {
        if (fileLoc.length() > 0) {
            factory.beginTransaction();
            IBOUB_ALD_INFCFGPARAM infCfgParamsBO = (IBOUB_ALD_INFCFGPARAM) factory
                    .getStatelessNewInstance(IBOUB_ALD_INFCFGPARAM.BONAME);
            infCfgParamsBO.setBoID(GUIDGen.getNewGUID());
            infCfgParamsBO.setF_CFGPARAMKEY(AlmondeConstants.EXTRACTTION_FILE_LOCATION);
            infCfgParamsBO.setF_CFGPARAMVALUE(fileLoc);
            infCfgParamsBO.setF_MODULENAME(AlmondeConstants.MODULE_NAME.toUpperCase());
            factory.create(IBOUB_ALD_INFCFGPARAM.BONAME, infCfgParamsBO);
            factory.commitTransaction();
        }
    }

    /**
     * Method Description: Inserts the Extraction For Value 'BASEL II' OR 'ALM' for a given Module
     * Name.
     * 
     * @param extractionFor
     */
    private void setExtractionForParam(String extractionFor) {
        if (extractionFor.length() > 0) {
            factory.beginTransaction();
            IBOUB_ALD_INFCFGPARAM infCfgParamsBO = (IBOUB_ALD_INFCFGPARAM) factory
                    .getStatelessNewInstance(IBOUB_ALD_INFCFGPARAM.BONAME);
            infCfgParamsBO.setBoID(GUIDGen.getNewGUID());
            infCfgParamsBO.setF_CFGPARAMKEY(AlmondeConstants.EXTRACTIONFOR);
            infCfgParamsBO.setF_CFGPARAMVALUE(extractionFor.equals(AlmondeConstants.ALM)?"ALM":"BASEL");
            infCfgParamsBO.setF_MODULENAME(AlmondeConstants.MODULE_NAME.toUpperCase());
            factory.create(IBOUB_ALD_INFCFGPARAM.BONAME, infCfgParamsBO);
            factory.commitTransaction();
        }
    }

    /**
     * Method Description: Inserts the MM Product List for a given Module Name.
     * 
     * @param param
     */
    private void setMMProdParam(String param) {
        if (param.length() > 0) {
            factory.beginTransaction();
            IBOUB_ALD_INFCFGPARAM infCfgParamsBO = (IBOUB_ALD_INFCFGPARAM) factory
                    .getStatelessNewInstance(IBOUB_ALD_INFCFGPARAM.BONAME);
            infCfgParamsBO.setBoID(GUIDGen.getNewGUID());
            infCfgParamsBO.setF_CFGPARAMKEY(AlmondeConstants.CONTRACT_MM);
            infCfgParamsBO.setF_CFGPARAMVALUE(param);
            infCfgParamsBO.setF_MODULENAME(AlmondeConstants.MODULE_NAME.toUpperCase());
            factory.create(IBOUB_ALD_INFCFGPARAM.BONAME, infCfgParamsBO);
            factory.commitTransaction();
        }
    }

    /**
     * Method Description: Inserts the NI Product List for a given Module Name.
     * 
     * @param param
     */
    private void setNIProdParam(String param) {
        if (param.length() > 0) {
            factory.beginTransaction();
            IBOUB_ALD_INFCFGPARAM infCfgParamsBO = (IBOUB_ALD_INFCFGPARAM) factory
                    .getStatelessNewInstance(IBOUB_ALD_INFCFGPARAM.BONAME);
            infCfgParamsBO.setBoID(GUIDGen.getNewGUID());
            infCfgParamsBO.setF_CFGPARAMKEY(AlmondeConstants.CONTRACT_NI);
            infCfgParamsBO.setF_CFGPARAMVALUE(param);
            infCfgParamsBO.setF_MODULENAME(AlmondeConstants.MODULE_NAME.toUpperCase());
            factory.create(IBOUB_ALD_INFCFGPARAM.BONAME, infCfgParamsBO);
            factory.commitTransaction();
        }
    }

    /**
     * Method Description: Inserts the FX Product List for a given Module Name.
     * 
     * @param param
     */
    private void setFXProdParam(String param) {
        if (param.length() > 0) {
            factory.beginTransaction();
            IBOUB_ALD_INFCFGPARAM infCfgParamsBO = (IBOUB_ALD_INFCFGPARAM) factory
                    .getStatelessNewInstance(IBOUB_ALD_INFCFGPARAM.BONAME);
            infCfgParamsBO.setBoID(GUIDGen.getNewGUID());
            infCfgParamsBO.setF_CFGPARAMKEY(AlmondeConstants.CONTRACT_FX);
            infCfgParamsBO.setF_CFGPARAMVALUE(param);
            infCfgParamsBO.setF_MODULENAME(AlmondeConstants.MODULE_NAME.toUpperCase());
            factory.create(IBOUB_ALD_INFCFGPARAM.BONAME, infCfgParamsBO);
            factory.commitTransaction();
        }
    }

    /**
     * Method Description: Inserts the Lending Product List for a given Module Name.
     * 
     * @param param
     */
    private void setLenProdParam(String param) {
        if (param.length() > 0) {
            factory.beginTransaction();
            IBOUB_ALD_INFCFGPARAM infCfgParamsBO = (IBOUB_ALD_INFCFGPARAM) factory
                    .getStatelessNewInstance(IBOUB_ALD_INFCFGPARAM.BONAME);
            infCfgParamsBO.setBoID(GUIDGen.getNewGUID());
            infCfgParamsBO.setF_CFGPARAMKEY(AlmondeConstants.CONTRACT_LENDING);
            infCfgParamsBO.setF_CFGPARAMVALUE(param);
            infCfgParamsBO.setF_MODULENAME(AlmondeConstants.MODULE_NAME.toUpperCase());
            factory.create(IBOUB_ALD_INFCFGPARAM.BONAME, infCfgParamsBO);
            factory.commitTransaction();
        }
    }

    /**
     * Method Description: Inserts the Other Product List for a given Module Name.
     * 
     * @param param
     */
    private void setOtherProdParam(String param) {
        if (param.length() > 0) {
            factory.beginTransaction();
            IBOUB_ALD_INFCFGPARAM infCfgParamsBO = (IBOUB_ALD_INFCFGPARAM) factory
                    .getStatelessNewInstance(IBOUB_ALD_INFCFGPARAM.BONAME);
            infCfgParamsBO.setBoID(GUIDGen.getNewGUID());
            infCfgParamsBO.setF_CFGPARAMKEY(AlmondeConstants.CONTRACT_OTHERS);
            infCfgParamsBO.setF_CFGPARAMVALUE(param);
            infCfgParamsBO.setF_MODULENAME(AlmondeConstants.MODULE_NAME.toUpperCase());
            factory.create(IBOUB_ALD_INFCFGPARAM.BONAME, infCfgParamsBO);
            factory.commitTransaction();
        }
    }

    
    /**
     * Method Description: Identifies and returns the product type for a given Product ID
     * 
     * @param productID
     * @return
     */
    private String getProductType(String productID) {

        if (almondeExtractProperties.getProperty(AlmondeConstants.OTHER_PROD_LIST).indexOf(productID) > 0) {
            return AlmondeConstants.CONTRACT_OTHERS;
        }

        @SuppressWarnings("deprecation")
		SimpleRuntimeProduct productRuntime = ProductFactoryProvider.getInstance().getProductFactory().getRuntimeProduct(productID,
                environment);

        @SuppressWarnings("rawtypes")
		Map features = productRuntime.getAllFeatures(environment);

        if (features.containsKey(FeatureIDs.FIXTUREFTR) && features.containsKey(FeatureIDs.SECURITYFTR)) {
            return AlmondeConstants.CONTRACT_MM_NI;
        }
        else if (features.containsKey(FeatureIDs.FIXTUREFTR)) {
            return AlmondeConstants.CONTRACT_MM;
        }
        else if (features.containsKey(FeatureIDs.SECURITYFTR)) {
            return AlmondeConstants.CONTRACT_NI;

        }
        else if (features.containsKey(FeatureIDs.LENDINGFTR)) {
            return AlmondeConstants.CONTRACT_LENDING;

        }
        else if (features.containsKey(FeatureIDs.FOREXFTR)) {
        	return AlmondeConstants.CONTRACT_FX;

        }
      
        return CommonConstants.EMPTY_STRING;

    }
    
    
}
