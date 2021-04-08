/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: AlmondeConstants.java,v.1.0,May 20, 2009 2:29:45 PM ayerla
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
import com.trapedza.bankfusion.bo.refimpl.IBOProduct;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_ALD_EXTRACTIONCFG;

import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.features.FeatureIDs;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.products.ProductFactoryProvider;
import com.trapedza.bankfusion.servercommon.products.SimpleRuntimeProduct;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ALD_PopulatingAbstractProducts;
import com.trapedza.bankfusion.utils.BankFusionMessages;


/**
 * @author vpamidip
 *
 */
/**
 * @author ssarpa
 *
 */
@SuppressWarnings("serial")
public class UB_ALD_PopulatingAbstractProducts extends AbstractUB_ALD_PopulatingAbstractProducts {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


    private transient final static Log logger = LogFactory.getLog(UB_ALD_PopulatingAbstractProducts.class.getName());

    private IPersistenceObjectsFactory factory;
    private BankFusionEnvironment env = null; 

    /** Build Condition to Fetch **/
    
    private Properties almondeExtractProperties = null;

    private boolean isNotEmpty = false;
    
    @SuppressWarnings("rawtypes")
	private List prdContractOthers = null;

    /**
     * @param env
     */
    public UB_ALD_PopulatingAbstractProducts(BankFusionEnvironment env) {
        super(env);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trapedza.bankfusion.steps.refimpl.AbstractUB_ALD_PopulatingAbstractProducts#process(com
     * .trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
     */
    @SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	public void process(BankFusionEnvironment environment) {
        boolean forTransferBox = isF_IN_forTransBox(); 
        factory = BankFusionThreadLocal.getPersistanceFactory();
        StringBuffer whereForAvailableList = new StringBuffer(AlmondeConstants.WHERE + IBOProduct.PRODUCTID + " <> ? "); 
        Object[] associatedList = null;
        VectorTable tempAssociatedList = null;
        VectorTable sortedAssocList = null;
        Iterator confPrdListItr = null;
        Iterator remainigPrds = null;
        Iterator allPrdList = null;
        String[] internalProd = null;
        String orderBy = CommonConstants.EMPTY_STRING; 
        StringBuffer propertiesPath = new StringBuffer(GetUBConfigLocation.getUBConfigLocation());
        propertiesPath.append(AlmondeConstants.PROPERTIES_FILE_LOCATION);
        try {
            almondeExtractProperties = loadProperties(new FileInputStream(propertiesPath.toString()));
        }
        catch (FileNotFoundException e) {
        	logger.error(e);
            throw new BankFusionException(40413024, BankFusionMessages.getFormattedMessage(40413024, new Object[] { propertiesPath.toString() }));
        }
        if (almondeExtractProperties != null) {
             internalProd = almondeExtractProperties.getProperty(AlmondeConstants.INTERNAL_PROD).split(CommonConstants.COMMA);
             String otherProd = almondeExtractProperties.getProperty(AlmondeConstants.OTHER_PROD_LIST);
             otherProd = otherProd.substring(1, otherProd.length()-1);
             prdContractOthers = Arrays.asList(otherProd.split(AlmondeConstants.PROD_DELIMETER));
             
        }
        ArrayList params = null;
        @SuppressWarnings("unused")
		boolean isCfgListEmpty = true;
        env = environment;

        // Before Transfer Box
        if (forTransferBox) {
            try {
                orderBy = AlmondeConstants.ORDERBY + IBOProduct.PRODUCTID;
                params = new ArrayList(Arrays.asList(internalProd));
                confPrdListItr = factory.findAll(IBOUB_ALD_EXTRACTIONCFG.BONAME, null, false).iterator();
                tempAssociatedList = associatedProductsList(confPrdListItr);
                sortedAssocList = sortVectorTable(tempAssociatedList, AlmondeConstants.BOID);
                setF_OUT_associatedList(sortedAssocList);

                associatedList = tempAssociatedList.getColumn(AlmondeConstants.BOID);

                // set all products to right box
                if (associatedList != null) {
                    for (int i = 0; i < associatedList.length; i++) {
                        params.add(associatedList[i]);
                    }

                    for (int i = 0; i < params.size() - 1; i++) {
                        whereForAvailableList = whereForAvailableList.append(AlmondeConstants.MORETHANONEPRD);

                    }

                    whereForAvailableList = whereForAvailableList.append(orderBy);

                    remainigPrds = factory.findByQuery(IBOProduct.BONAME, whereForAvailableList.toString(), params, null)
                            .iterator();

                    // set all products to left box
                    VectorTable sortedAvailList = availableProductsList(remainigPrds);
                    setF_OUT_availableList(sortedAvailList);
                }
                else {
                    for (int i = 0; i < params.size() - 1; i++) {
                        whereForAvailableList = whereForAvailableList.append(AlmondeConstants.MORETHANONEPRD);

                    }

                    whereForAvailableList = whereForAvailableList.append(orderBy);

                    allPrdList = factory.findByQuery(IBOProduct.BONAME, whereForAvailableList.toString(), params, null).iterator();
                    setF_OUT_availableList(availableProductsList(allPrdList));
                }
            }

            catch (NullPointerException e) {
                if (logger.isInfoEnabled())
                    logger.info("All Products Are Associated");
                // set all products to left box
                logger.error(e);
            }
        }
        // After TransferBox
        else {
            VectorTable tempRightGridData = getF_IN_rightGridData();
            setF_OUT_resultProducts(resultProductsList(tempRightGridData));
            setF_OUT_notEmptyVector(isNotEmpty);
        }
    }

    // Listing all the Items in Sorted manner
    private VectorTable sortVectorTable(VectorTable vectorTable, String sortByColumn) {
        int size;
        String outerMapValue = CommonConstants.EMPTY_STRING;
        String innerMapValue = CommonConstants.EMPTY_STRING;
        String tempValue = null;
        @SuppressWarnings("rawtypes")
		HashMap outerMap = null;
        @SuppressWarnings("rawtypes")
		HashMap innerMap = null;
        size = vectorTable.size();
        if (size > 0) {
            try {
                for (int i = 0; i < size - 1; i++) {
                    outerMap = vectorTable.getRowTags(i);
                    outerMapValue = (String) outerMap.get(sortByColumn);
                    for (int j = i + 1; j < size; j++) {
                        innerMap = vectorTable.getRowTags(j);
                        innerMapValue = (String) innerMap.get(sortByColumn);
                        if (outerMapValue.compareTo(innerMapValue) > 0) {
                            vectorTable.populateRow(innerMap, i);
                            vectorTable.populateRow(outerMap, j);
                            tempValue = outerMapValue;
                            outerMapValue = innerMapValue;
                            innerMapValue = tempValue;
                            outerMap = innerMap;
                        }
                    }
                }
            }
            catch (ArrayIndexOutOfBoundsException a) {
                logger.error("No Item Is Selected");
                logger.error(a);
            }
        }

        return vectorTable;
    }

    // This is for Left Grid in Transfer Box
    @SuppressWarnings("unchecked")
	private VectorTable availableProductsList(@SuppressWarnings("rawtypes") Iterator tempProductIterator) {
        VectorTable tempResultVector = new VectorTable();

        while (tempProductIterator.hasNext()) {
            IBOProduct product = (IBOProduct) tempProductIterator.next();
            @SuppressWarnings("rawtypes")
			Map record1 = new HashMap<String, Object>();
            
            record1.put(CommonConstants.SELECT, new Boolean(false));
            record1.put(AlmondeConstants.BOID, product.getBoID());
            tempResultVector.addAll(new VectorTable(record1));

        }
        return tempResultVector;
    }

    // This is for Right Grid in Transfer Box
    @SuppressWarnings("unchecked")
	private VectorTable associatedProductsList(@SuppressWarnings("rawtypes") Iterator tempProductIterator) {
        VectorTable tempResultVector = new VectorTable();
        while (tempProductIterator.hasNext()) {
            IBOUB_ALD_EXTRACTIONCFG product = (IBOUB_ALD_EXTRACTIONCFG) tempProductIterator.next();
            @SuppressWarnings("rawtypes")
			Map record1 = new HashMap<String, Object>();
            record1.put(CommonConstants.SELECT, new Boolean(false));
            record1.put(AlmondeConstants.BOID, product.getF_UBEXTRACTDATATYPEATTR());
            tempResultVector.addAll(new VectorTable(record1));
        }
        return tempResultVector;
    }

    // List of associated Products
    @SuppressWarnings({ "unchecked", "deprecation" })
	private VectorTable resultProductsList(VectorTable tempRightVector) {

        VectorTable tempResultVector = new VectorTable();
        SimpleRuntimeProduct product = null;
        String productId = CommonConstants.EMPTY_STRING;
        @SuppressWarnings("rawtypes")
		Map allFeatures = null;

        for (int i = 0; i < tempRightVector.size(); i++) {

            Map<String, Object> record = tempRightVector.getRowTags(i);

            @SuppressWarnings("rawtypes")
			Map record1 = new HashMap<String, Object>();
            record1.put(AlmondeConstants.UBEXTRACTDATATYPEATTR, record.get(AlmondeConstants.BOID));
            productId = (String) record.get(AlmondeConstants.BOID);
            product = ProductFactoryProvider.getInstance().getProductFactory().getRuntimeProduct(productId, env);

            allFeatures = product.getAllFeatures(env);

            if (allFeatures.containsKey(FeatureIDs.FIXTUREFTR) && allFeatures.containsKey(FeatureIDs.SECURITYFTR)) {
                record1.put(AlmondeConstants.UBEXTRACTDATATYPE, AlmondeConstants.CONTRACT_MM_NI);
            }
            else if (allFeatures.containsKey(FeatureIDs.FIXTUREFTR)) {
                record1.put(AlmondeConstants.UBEXTRACTDATATYPE, AlmondeConstants.CONTRACT_MM);
            }
            else if (allFeatures.containsKey(FeatureIDs.SECURITYFTR)) {
                record1.put(AlmondeConstants.UBEXTRACTDATATYPE, AlmondeConstants.CONTRACT_NI);
            }
            else if (allFeatures.containsKey(FeatureIDs.LENDINGFTR)) {
                record1.put(AlmondeConstants.UBEXTRACTDATATYPE, AlmondeConstants.CONTRACT_LENDING);
            }
            else if (allFeatures.containsKey(FeatureIDs.FOREXFTR)) {
                record1.put(AlmondeConstants.UBEXTRACTDATATYPE, AlmondeConstants.CONTRACT_FX);
            }
            else if (prdContractOthers.contains(productId)) {
                record1.put(AlmondeConstants.UBEXTRACTDATATYPE, AlmondeConstants.CONTRACT_OTHERS);
            }
            else {
                record1.put(AlmondeConstants.UBEXTRACTDATATYPE, AlmondeConstants.ABSTRACTPRDS);
            }

            tempResultVector.addAll(new VectorTable(record1));
            isNotEmpty = true;
        }
        return tempResultVector;
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
        	logger.error(e);
            throw new FileNotFoundException();
        }
        return prop;
    }

}
