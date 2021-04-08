/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: CaptureRatingMaintenanceFatom.java,v.1.0,Jun 5, 2009 10:39:25 AM ayerla
 *
 */
package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trapedza.bankfusion.bo.refimpl.IBOUBTB_RATINGDETAILS;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_ALD_CURRENCYRATING;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_ALD_OVERALLRATING;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ALD_CaptureRatingMaintenanceFatom;
import com.trapedza.bankfusion.utils.BankFusionMessages;

/**
 * @author ayerla
 * @date Jun 5, 2009
 * @project Universal Banking
 * @Description: CaptureRatingMaintenanceFatom is used to add, update or remove the ratings for an
 *               entity and update Database.
 */

public class CaptureRatingMaintenanceFatom extends AbstractUB_ALD_CaptureRatingMaintenanceFatom {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


    private static final int ADD = 0;
    private static final int UPDATE = 1;
    private static final int REMOVE = 2;
    private static final int UPDATEDB = 3;

    private static final String MODIFIED = "Modified";
    private static final String UBRATINGDLTSID = "UBRATINGDLTSID";
    private static final String UBRATINGCODE = "UBRATINGCODE";
    private static final String UBRATINGTERM = "UBRATINGTERM";
    private static final String UBRATINGVALUE = "UBRATINGVALUE";
    private static final String UBOVRLRATINGID = "UBOVRLRATINGID";
    private static final String UBRATINGENTITYTYPE = "UBRATINGENTITYTYPE";
    private static final String UBRATINGENTITYID = "UBRATINGENTITYID";
    private static final String UBCURRENCYRATINGID = "UBCURRENCYRATINGID";
    private static final String UBISOCURRENCYCODE = "UBISOCURRENCYCODE";

    private final String ENTITY_OVERALL_RATING_QUERY = CommonConstants.SELECT + CommonConstants.SPACE + "T1."
            + IBOUBTB_RATINGDETAILS.UBRATINGDLTSIDPK + " AS " + UBRATINGDLTSID + CommonConstants.COMMA + "T1."
            + IBOUBTB_RATINGDETAILS.UBRATINGCODE + " AS " + UBRATINGCODE + CommonConstants.COMMA + "T1."
            + IBOUBTB_RATINGDETAILS.UBRATINGTERM + " AS " + UBRATINGTERM + CommonConstants.COMMA + "T1."
            + IBOUBTB_RATINGDETAILS.UBRATINGVALUE + " AS " + UBRATINGVALUE + CommonConstants.COMMA + "T2."
            + IBOUB_ALD_OVERALLRATING.UBOVRLRATINGIDPK + " AS " + UBOVRLRATINGID + CommonConstants.COMMA + "T2."
            + IBOUB_ALD_OVERALLRATING.UBRATINGENTITYTYPE + " AS " + UBRATINGENTITYTYPE + CommonConstants.COMMA + "T2."
            + IBOUB_ALD_OVERALLRATING.UBRATINGENTITYID + " AS " + UBRATINGENTITYID + CommonConstants.SPACE + CommonConstants.FROM
            + CommonConstants.SPACE + IBOUBTB_RATINGDETAILS.BONAME + " T1" + CommonConstants.COMMA + IBOUB_ALD_OVERALLRATING.BONAME
            + " T2" + CommonConstants.SPACE + CommonConstants.WHERE + CommonConstants.SPACE + "T1."
            + IBOUBTB_RATINGDETAILS.UBRATINGDLTSIDPK + CommonConstants.EQUAL + "T2." + IBOUB_ALD_OVERALLRATING.UBRATINGDLTSID
            + " AND T2." + IBOUB_ALD_OVERALLRATING.UBRATINGENTITYTYPE + CommonConstants.EQUAL + " ? AND T2."
            + IBOUB_ALD_OVERALLRATING.UBRATINGENTITYID + CommonConstants.EQUAL + " ?";

    private final String ENTITY_CURRENCY_RATING_QUERY = CommonConstants.SELECT + CommonConstants.SPACE + "T1."
            + IBOUBTB_RATINGDETAILS.UBRATINGDLTSIDPK + " AS " + UBRATINGDLTSID + CommonConstants.COMMA + "T1."
            + IBOUBTB_RATINGDETAILS.UBRATINGCODE + " AS " + UBRATINGCODE + CommonConstants.COMMA + "T1."
            + IBOUBTB_RATINGDETAILS.UBRATINGTERM + " AS " + UBRATINGTERM + CommonConstants.COMMA + "T1."
            + IBOUBTB_RATINGDETAILS.UBRATINGVALUE + " AS " + UBRATINGVALUE + CommonConstants.COMMA + "T2."
            + IBOUB_ALD_CURRENCYRATING.UBCURRENCYRATINGIDPK + " AS " + UBCURRENCYRATINGID + CommonConstants.COMMA + "T2."
            + IBOUB_ALD_CURRENCYRATING.UBRATINGENTITYTYPE + " AS " + UBRATINGENTITYTYPE + CommonConstants.COMMA + "T2."
            + IBOUB_ALD_CURRENCYRATING.UBRATINGENTITYID + " AS " + UBRATINGENTITYID + CommonConstants.COMMA + "T2."
            + IBOUB_ALD_CURRENCYRATING.UBISOCURRENCYCODE + " AS " + UBISOCURRENCYCODE + CommonConstants.SPACE
            + CommonConstants.FROM + CommonConstants.SPACE + IBOUBTB_RATINGDETAILS.BONAME + " T1" + CommonConstants.COMMA
            + IBOUB_ALD_CURRENCYRATING.BONAME + " T2" + CommonConstants.SPACE + CommonConstants.WHERE + CommonConstants.SPACE
            + "T1." + IBOUBTB_RATINGDETAILS.UBRATINGDLTSIDPK + CommonConstants.EQUAL + " T2."
            + IBOUB_ALD_CURRENCYRATING.UBRATINGDLTSID + " AND T2." + IBOUB_ALD_CURRENCYRATING.UBRATINGENTITYTYPE
            + CommonConstants.EQUAL + " ? AND T2." + IBOUB_ALD_CURRENCYRATING.UBRATINGENTITYID + CommonConstants.EQUAL + " ?";

    private static final String CURRENCY_RATING_DELETE_WHERECLAUSE = CommonConstants.WHERE + CommonConstants.SPACE
            + IBOUB_ALD_CURRENCYRATING.UBCURRENCYRATINGIDPK + CommonConstants.SPACE + CommonConstants.EQUAL
            + CommonConstants.QUESTION;

    private static final String OVERALL_RATING_DELETE_WHERECLAUSE = CommonConstants.WHERE + CommonConstants.SPACE
            + IBOUB_ALD_OVERALLRATING.UBOVRLRATINGIDPK + CommonConstants.SPACE + CommonConstants.EQUAL + CommonConstants.QUESTION;

    private static final String GET_RATING_DTLS_ID_SERVICE = "UB_ALD_GetRatingDtlsId_SRV";

    private static final String GET_RATING_DTLS_ID_SERVICE_IN_PARRAM1_AGENCYCODE = "agencyCode";
    private static final String GET_RATING_DTLS_ID_SERVICE_IN_PARRAM2_RATING_TERM = "ratingTerm";
    private static final String GET_RATING_DTLS_ID_SERVICE_IN_PARRAM3_RATING_VALUE = "ratingValue";

    private static final String GET_RATING_DTLS_ID_SERVICE_OUT_PARRAM1_NOFROWS = "NoofRows";
    private static final String GET_RATING_DTLS_ID_SERVICE_OUT_PARRAM2_RATING_DTLID = "RatingDtlsIDPK";

    private BankFusionEnvironment environment;

    private IPersistenceObjectsFactory factory;

    /**
     * @param env
     */
    public CaptureRatingMaintenanceFatom(BankFusionEnvironment env) {
        super(env);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trapedza.bankfusion.steps.refimpl.AbstractUB_ALD_CaptureRatingMaintenanceFatom#process
     * (com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
     */
    public void process(BankFusionEnvironment env) throws BankFusionException {
        environment = env;
        factory = BankFusionThreadLocal.getPersistanceFactory();
        switch (getF_IN_Mode().intValue()) {
            case ADD:
                addRating();
                break;
            case UPDATE:
                updateRating();
                break;
            case REMOVE:
                deleteRating();
                break;
            case UPDATEDB:
                updateDB();
                break;
            default:
                break;
        }
    }

    /**
     * Method Description: Adds rating to the entity code.
     */
    private void addRating() {
        VectorTable sourceVector = getF_IN_RatingDetails();
        VectorTable deleteVector = getF_IN_DeletedRatingDetails();
        String agencyCode = getF_IN_AgencyCode();
        String ratingTerm = getF_IN_RatingTerm();
        String ratingValue = getF_IN_RatingValue();
        String currency = getF_IN_ISOCurrencyCode();
        String entityCode = getF_IN_EntityCode();

        String ratingDtlIdPK = validateRatingDetails();
        checkForDuplicate(sourceVector);

        Map<String, Object> rowMap = new HashMap<String, Object>();
        rowMap.put(MODIFIED, null);
        int index = sourceVector.size();
        if (index == 0) {
            rowMap.put(CommonConstants.SELECT, Boolean.TRUE);
            index = 1;
            rowMap.put(CommonConstants.SRNO, index);
        }
        else {
            index++;
            rowMap.put(CommonConstants.SELECT, Boolean.FALSE);
            rowMap.put(CommonConstants.SRNO, index);
        }

        rowMap.put(CommonConstants.getTagName(IBOUBTB_RATINGDETAILS.UBRATINGTERM), ratingTerm);
        rowMap.put(CommonConstants.getTagName(IBOUBTB_RATINGDETAILS.UBRATINGVALUE), ratingValue);
        rowMap.put(CommonConstants.getTagName(IBOUBTB_RATINGDETAILS.UBRATINGCODE), agencyCode);

        if (isF_IN_IsCurrency()) {
            rowMap.put(CommonConstants.getTagName(IBOUB_ALD_CURRENCYRATING.UBCURRENCYRATINGIDPK), CommonConstants.EMPTY_STRING);
            rowMap.put(CommonConstants.getTagName(IBOUB_ALD_CURRENCYRATING.UBRATINGDLTSID), ratingDtlIdPK);
            rowMap.put(CommonConstants.getTagName(IBOUB_ALD_CURRENCYRATING.UBRATINGENTITYID), entityCode);
            rowMap.put(CommonConstants.getTagName(IBOUB_ALD_CURRENCYRATING.UBISOCURRENCYCODE), currency);
        }
        else {
            rowMap.put(CommonConstants.getTagName(IBOUB_ALD_OVERALLRATING.UBOVRLRATINGIDPK), CommonConstants.EMPTY_STRING);
            rowMap.put(CommonConstants.getTagName(IBOUB_ALD_OVERALLRATING.UBRATINGDLTSID), ratingDtlIdPK);
            rowMap.put(CommonConstants.getTagName(IBOUB_ALD_OVERALLRATING.UBRATINGENTITYID), entityCode);
        }
        sourceVector.addAll(new VectorTable(rowMap));

        setF_OUT_RatingDetails(sourceVector);
        setF_OUT_DeletedRatingDetails(deleteVector);

    }

    /**
     * Method Description: Updates rating of the entity code.
     */
    private void updateRating() {
        VectorTable sourceVector = getF_IN_RatingDetails();
        VectorTable deleteVector = getF_IN_DeletedRatingDetails();
        String ratingValue = getF_IN_RatingValue();

        String ratingDtlIdPK = validateRatingDetails();

        int rowIndex = sourceVector.getSelectedRowIndex();
        Map rowDatamap = sourceVector.getRowTags(rowIndex);
        rowDatamap.put(CommonConstants.SELECT, Boolean.FALSE);
        String rowDataRatingDtlSID = CommonConstants.EMPTY_STRING;
        if (isF_IN_IsCurrency()) {
            rowDataRatingDtlSID = (String) rowDatamap.get(CommonConstants.getTagName(IBOUB_ALD_CURRENCYRATING.UBRATINGDLTSID));
            if (!ratingDtlIdPK.equals(rowDataRatingDtlSID)) {
                rowDatamap.put(CommonConstants.getTagName(IBOUBTB_RATINGDETAILS.UBRATINGVALUE), ratingValue);
                rowDatamap.put(CommonConstants.getTagName(IBOUB_ALD_CURRENCYRATING.UBRATINGDLTSID), ratingDtlIdPK);
                rowDatamap.put(MODIFIED, MODIFIED);
            }
        }
        else {
            rowDataRatingDtlSID = (String) rowDatamap.get(CommonConstants.getTagName(IBOUB_ALD_OVERALLRATING.UBRATINGDLTSID));
            if (!ratingDtlIdPK.equals(rowDataRatingDtlSID)) {
                rowDatamap.put(CommonConstants.getTagName(IBOUBTB_RATINGDETAILS.UBRATINGVALUE), ratingValue);
                rowDatamap.put(CommonConstants.getTagName(IBOUB_ALD_OVERALLRATING.UBRATINGDLTSID), ratingDtlIdPK);
                rowDatamap.put(MODIFIED, MODIFIED);
            }
        }
        sourceVector.populateRow(rowDatamap, rowIndex);
        if (sourceVector.size() > 0) {
            rowDatamap = sourceVector.getRowTags(0);
            rowDatamap.put(CommonConstants.SELECT, Boolean.TRUE);
            sourceVector.populateRow(rowDatamap, 0);
        }

        setF_OUT_RatingDetails(sourceVector);
        setF_OUT_DeletedRatingDetails(deleteVector);
    }

    /**
     * Method Description: Deleted rating of the entity code
     */
    private void deleteRating() {
        VectorTable sourceVector = getF_IN_RatingDetails();
        VectorTable tempVector = new VectorTable();
        VectorTable deleteVector = getF_IN_DeletedRatingDetails();
        int rowIndex = sourceVector.getSelectedRowIndex();
        int srno = 1;
        for (int index = 0; index < sourceVector.size(); index++) {
            if (index != rowIndex) {
                Map rowDataMap = sourceVector.getRowTags(index);
                rowDataMap.put(CommonConstants.SRNO, srno);
                if (index == 0) {
                    rowDataMap.put(CommonConstants.SELECT, Boolean.TRUE);
                }
                else {
                    rowDataMap.put(CommonConstants.SELECT, Boolean.FALSE);
                }
                tempVector.addAll(new VectorTable(rowDataMap));
                srno++;
            }
            else{
                Map rowDataMap = sourceVector.getRowTags(index);
                deleteVector.addAll(new VectorTable(rowDataMap));
            }
        }
        setF_OUT_RatingDetails(tempVector);
        setF_OUT_DeletedRatingDetails(deleteVector);
    }

    /**
     * Method Description: updates database with the rating details.
     */
    private void updateDB() {
        List<Map<String, String>> newRecordList = new ArrayList<Map<String, String>>();
        List<Map<String, String>> updateRecordsList = new ArrayList<Map<String, String>>();
        VectorTable sourceVector = getF_IN_RatingDetails();
        
        VectorTable deleteVector = getF_IN_DeletedRatingDetails();
        
        if(deleteVector.size()>0){
            deleDataFromDB();
            }
        
        List<List<String>> dataFromDb = getRatingsPkAndRatingDtlsList();
        List<String> ratingPkList = dataFromDb.get(0);
        List<String> ratingDtlList = dataFromDb.get(1);

        if (isF_IN_IsCurrency()) {
            for (int index = 0; index < sourceVector.size(); index++) {
                Map dataMap = sourceVector.getRowTags(index);
                String primaryKey = (String) dataMap.get(CommonConstants.getTagName(IBOUB_ALD_CURRENCYRATING.UBCURRENCYRATINGIDPK));
                if (primaryKey.equals(CommonConstants.EMPTY_STRING)) {
                    newRecordList.add(dataMap);
                }
                else  {
                    updateRecordsList.add(dataMap);
                }
            }
        }
        else {
            for (int index = 0; index < sourceVector.size(); index++) {
                Map dataMap = sourceVector.getRowTags(index);
                String primaryKey = (String) dataMap.get(CommonConstants.getTagName(IBOUB_ALD_OVERALLRATING.UBOVRLRATINGIDPK));
                if (primaryKey.equals(CommonConstants.EMPTY_STRING)) {
                    newRecordList.add(dataMap);
                }
                else {
                    updateRecordsList.add(dataMap);
                }
            }
        }
        
        updateRecords(updateRecordsList, ratingPkList);
        createRecords(newRecordList, ratingDtlList);

    }

    /**
     * Method Description: Creates rating details rows in DB
     * 
     * @param newRecordList
     * @param ratingDtlList
     */
    private void createRecords(List<Map<String, String>> newRecordList, List<String> ratingDtlList) {
        factory.beginTransaction();
        String entityCode = getF_IN_EntityCode();
        String entityType = getF_IN_EntityType();
        if (isF_IN_IsCurrency()) {
            for (Iterator iterator = newRecordList.iterator(); iterator.hasNext();) {
                Map<String, String> dataMap = (Map<String, String>) iterator.next();

           //     String primaryKey = dataMap.get(CommonConstants.getTagName(IBOUB_ALD_CURRENCYRATING.UBCURRENCYRATINGIDPK));
                String currencyCode = dataMap.get(CommonConstants.getTagName(IBOUB_ALD_CURRENCYRATING.UBISOCURRENCYCODE));
                String agencyCode = dataMap.get(CommonConstants.getTagName(IBOUBTB_RATINGDETAILS.UBRATINGCODE));
                String ratingTerm = dataMap.get(CommonConstants.getTagName(IBOUBTB_RATINGDETAILS.UBRATINGTERM));
                String ratingDtlId = dataMap.get(CommonConstants.getTagName(IBOUB_ALD_CURRENCYRATING.UBRATINGDLTSID));
                if (!ratingDtlList.contains(currencyCode + agencyCode + ratingTerm)) {
                    IBOUB_ALD_CURRENCYRATING currencyRatingBO = (IBOUB_ALD_CURRENCYRATING) factory
                            .getStatelessNewInstance(IBOUB_ALD_CURRENCYRATING.BONAME);
                    currencyRatingBO.setF_UBISOCURRENCYCODE(currencyCode);
                    currencyRatingBO.setF_UBRATINGDLTSID(ratingDtlId);
                    currencyRatingBO.setF_UBRATINGENTITYID(entityCode);
                    currencyRatingBO.setF_UBRATINGENTITYTYPE(entityType);
                    factory.create(IBOUB_ALD_CURRENCYRATING.BONAME, currencyRatingBO);
                }
            }
        }
        else {
            for (Iterator iterator = newRecordList.iterator(); iterator.hasNext();) {
                Map<String, String> dataMap = (Map<String, String>) iterator.next();
  //              String primaryKey = dataMap.get(CommonConstants.getTagName(IBOUB_ALD_OVERALLRATING.UBOVRLRATINGIDPK));
                String ratingDtlId = dataMap.get(CommonConstants.getTagName(IBOUB_ALD_CURRENCYRATING.UBRATINGDLTSID));
                String agencyCode = dataMap.get(CommonConstants.getTagName(IBOUBTB_RATINGDETAILS.UBRATINGCODE));
                String ratingTerm = dataMap.get(CommonConstants.getTagName(IBOUBTB_RATINGDETAILS.UBRATINGTERM));
                if (!ratingDtlList.contains(agencyCode + ratingTerm)) {
                    IBOUB_ALD_OVERALLRATING overallRatingBO = (IBOUB_ALD_OVERALLRATING) factory
                            .getStatelessNewInstance(IBOUB_ALD_OVERALLRATING.BONAME);
                    overallRatingBO.setF_UBRATINGDLTSID(ratingDtlId);
                    overallRatingBO.setF_UBRATINGENTITYID(entityCode);
                    overallRatingBO.setF_UBRATINGENTITYTYPE(entityType);
                    factory.create(IBOUB_ALD_OVERALLRATING.BONAME, overallRatingBO);
                }
            }

        }
        factory.commitTransaction();
        factory.beginTransaction();
    }

    /**
     * Method Description: Updates rating details rows in DB
     * 
     * @param updateRecordsList
     * @param datafromDB
     */
    private void updateRecords(List<Map<String, String>> updateRecordsList, List<String> datafromDB) {
        factory.beginTransaction();
        if (isF_IN_IsCurrency()) {
            for (Iterator iterator = updateRecordsList.iterator(); iterator.hasNext();) {
                Map<String, String> dataMap = (Map<String, String>) iterator.next();

                String primaryKey = dataMap.get(CommonConstants.getTagName(IBOUB_ALD_CURRENCYRATING.UBCURRENCYRATINGIDPK));
                if (datafromDB.contains(primaryKey)) {
                    IBOUB_ALD_CURRENCYRATING currencyRatingBO = (IBOUB_ALD_CURRENCYRATING) factory.findByPrimaryKey(
                            IBOUB_ALD_CURRENCYRATING.BONAME, primaryKey, false);
                    currencyRatingBO.setF_UBRATINGDLTSID(dataMap.get(CommonConstants
                            .getTagName(IBOUB_ALD_CURRENCYRATING.UBRATINGDLTSID)));
                }
            }
        }
        else {
            for (Iterator iterator = updateRecordsList.iterator(); iterator.hasNext();) {
                Map<String, String> dataMap = (Map<String, String>) iterator.next();

                String primaryKey = dataMap.get(CommonConstants.getTagName(IBOUB_ALD_OVERALLRATING.UBOVRLRATINGIDPK));
                if (datafromDB.contains(primaryKey)) {
                    IBOUB_ALD_OVERALLRATING overallRatingBO = (IBOUB_ALD_OVERALLRATING) factory.findByPrimaryKey(
                            IBOUB_ALD_OVERALLRATING.BONAME, primaryKey, false);
                    overallRatingBO.setF_UBRATINGDLTSID(dataMap.get(CommonConstants
                            .getTagName(IBOUB_ALD_OVERALLRATING.UBRATINGDLTSID)));
                }
            }

        }
        factory.commitTransaction();
        factory.beginTransaction();

    }

    /**
     * Method Description: Deletes rating details rows from DB
     * 
     * @param primaryKeyList
     * @param datafromDB
     */
    private void deleDataFromDB() {

        VectorTable deleteVector = getF_IN_DeletedRatingDetails();
        if (isF_IN_IsCurrency()) {
            List primaryKeyList = new ArrayList(Arrays.asList(deleteVector.getColumn(CommonConstants
                    .getTagName(IBOUB_ALD_CURRENCYRATING.UBCURRENCYRATINGIDPK))));
            primaryKeyList.remove(CommonConstants.EMPTY_STRING);
            if (!primaryKeyList.isEmpty()) {
                ArrayList<String> params = new ArrayList<String>();
                factory.beginTransaction();
                for (Iterator<String> iterator = primaryKeyList.iterator(); iterator.hasNext();) {
                    String primaryKey = iterator.next();
                    params.clear();
                    params.add(primaryKey);
                    factory.bulkDelete(IBOUB_ALD_CURRENCYRATING.BONAME, CURRENCY_RATING_DELETE_WHERECLAUSE, params);
                }
                factory.commitTransaction();
                factory.beginTransaction();
            }
        }
        else {
            List primaryKeyList = new ArrayList(Arrays.asList(deleteVector.getColumn(CommonConstants
                    .getTagName(IBOUB_ALD_OVERALLRATING.UBOVRLRATINGIDPK))));
            primaryKeyList.remove(CommonConstants.EMPTY_STRING);
            if (!primaryKeyList.isEmpty()) {
                ArrayList<String> params = new ArrayList<String>();
                factory.beginTransaction();
                for (Iterator<String> iterator = primaryKeyList.iterator(); iterator.hasNext();) {
                    String primaryKey = iterator.next();
                    params.clear();
                    params.add(primaryKey);
                    factory.bulkDelete(IBOUB_ALD_OVERALLRATING.BONAME, OVERALL_RATING_DELETE_WHERECLAUSE, params);
                }
                factory.commitTransaction();
                factory.beginTransaction();

            }
        }
    }

    /**
     * Method Description: Validates the agency code, rating term, rating value.
     * 
     * @return rating details PK
     * @throws BankFusionException
     */
    private String validateRatingDetails(){
        String agencyCode = getF_IN_AgencyCode();
        String ratingTerm = getF_IN_RatingTerm();
        String ratingValue = getF_IN_RatingValue();

        if (agencyCode == null || agencyCode.equals(CommonConstants.EMPTY_STRING))
            throw new BankFusionException(40413008, BankFusionMessages.getFormattedMessage( 40413008,
                    new Object[] { agencyCode }));
        if (agencyCode == null || agencyCode.equals(CommonConstants.EMPTY_STRING))
            throw new BankFusionException(40413009, BankFusionMessages.getFormattedMessage( 40413009,
                    new Object[] { ratingTerm, agencyCode }));
        Map<String, String> inPutMap = new HashMap<String, String>();
        inPutMap.put(GET_RATING_DTLS_ID_SERVICE_IN_PARRAM1_AGENCYCODE, agencyCode);
        inPutMap.put(GET_RATING_DTLS_ID_SERVICE_IN_PARRAM2_RATING_TERM, ratingTerm);
        inPutMap.put(GET_RATING_DTLS_ID_SERVICE_IN_PARRAM3_RATING_VALUE, ratingValue);

        Map outPutMap = MFExecuter.executeMF(GET_RATING_DTLS_ID_SERVICE, environment, inPutMap);
        if (((Integer) outPutMap.get(GET_RATING_DTLS_ID_SERVICE_OUT_PARRAM1_NOFROWS)).intValue() <= 0) {
            throw new BankFusionException(40413010, BankFusionMessages.getFormattedMessage( 40413010,
                    new Object[] { ratingValue, agencyCode, ratingTerm }));
        }

        return (String) outPutMap.get(GET_RATING_DTLS_ID_SERVICE_OUT_PARRAM2_RATING_DTLID);
    }

    /**
     * Method Description: Checks for duplicate rating term for a given agency code.
     * 
     * @param ratingDetails
     * @throws BankFusionException
     */
    private void checkForDuplicate(VectorTable ratingDetails){
        String agencyCode = getF_IN_AgencyCode();
        String ratingTerm = getF_IN_RatingTerm();
        String currencyCode = getF_IN_ISOCurrencyCode();
        String entityCode = getF_IN_EntityCode();

        if (isF_IN_IsCurrency()) {
            for (int index = 0; index < ratingDetails.size(); index++) {
                Map rowDataMap = ratingDetails.getRowTags(index);
                String rowDataAgencyCode = (String) rowDataMap.get(CommonConstants.getTagName(IBOUBTB_RATINGDETAILS.UBRATINGCODE));
                String rowDataRatingTerm = (String) rowDataMap.get(CommonConstants.getTagName(IBOUBTB_RATINGDETAILS.UBRATINGTERM));
                String rowDataCurrencyCode = (String) rowDataMap.get(CommonConstants
                        .getTagName(IBOUB_ALD_CURRENCYRATING.UBISOCURRENCYCODE));
                if (rowDataCurrencyCode.equals(currencyCode) && rowDataAgencyCode.equals(agencyCode)
                        && rowDataRatingTerm.equals(ratingTerm)) {
                    throw new BankFusionException(40413027, BankFusionMessages.getFormattedMessage(
                            40413027, new Object[] { entityCode, currencyCode, ratingTerm, agencyCode }));
                }
            }
        }
        else {

            for (int index = 0; index < ratingDetails.size(); index++) {
                Map rowDataMap = ratingDetails.getRowTags(index);
                String rowDataAgencyCode = (String) rowDataMap.get(CommonConstants.getTagName(IBOUBTB_RATINGDETAILS.UBRATINGCODE));
                String rowDataRatingTerm = (String) rowDataMap.get(CommonConstants.getTagName(IBOUBTB_RATINGDETAILS.UBRATINGTERM));
                if (rowDataAgencyCode.equals(agencyCode) && rowDataRatingTerm.equals(ratingTerm)) {
                    throw new BankFusionException(40413026, BankFusionMessages.getFormattedMessage(40413026, new Object[] { entityCode, ratingTerm, agencyCode }));
                }
            }

        }
    }

    /**
     * Method Description: Fetches rating dtl pk from DB.
     * 
     * @return
     */
    private List getRatingsPkAndRatingDtlsList() {
        List<List<String>> ratingPkandRatingDtlList = new ArrayList<List<String>>();
        List<String> ratingDtlList = new ArrayList<String>();
        List<String> pkList = new ArrayList<String>();

        ratingPkandRatingDtlList.add(pkList);
        ratingPkandRatingDtlList.add(ratingDtlList);

        ArrayList<String> params = new ArrayList<String>();
        params.add(getF_IN_EntityType());
        params.add(getF_IN_EntityCode());

        if (isF_IN_IsCurrency()) {
            List ratingList = factory.executeGenericQuery(ENTITY_CURRENCY_RATING_QUERY, params, null, false);
            for (Iterator<SimplePersistentObject> iterator = ratingList.iterator(); iterator.hasNext();) {
                SimplePersistentObject rowData = iterator.next();
                Map rowDataMap = rowData.getDataMap();
                String primaryKey = (String) rowDataMap.get(UBCURRENCYRATINGID);
                String currencyCode = (String) rowDataMap.get(UBCURRENCYRATINGID);
                String agencyCode = (String) rowDataMap.get(UBRATINGCODE);
                String ratingTerm = (String) rowDataMap.get(UBRATINGTERM);
                pkList.add(primaryKey);
                ratingDtlList.add(currencyCode + agencyCode + ratingTerm);
            }
        }
        else {
            List ratingList = factory.executeGenericQuery(ENTITY_OVERALL_RATING_QUERY, params, null, false);
            for (Iterator<SimplePersistentObject> iterator = ratingList.iterator(); iterator.hasNext();) {
                SimplePersistentObject rowData = iterator.next();
                Map rowDataMap = rowData.getDataMap();
                String primaryKey = (String) rowDataMap.get(UBOVRLRATINGID);
                String agencyCode = (String) rowDataMap.get(UBRATINGCODE);
                String ratingTerm = (String) rowDataMap.get(UBRATINGTERM);
                pkList.add(primaryKey);
                ratingDtlList.add(agencyCode + ratingTerm);
            }
        }
        return ratingPkandRatingDtlList;
    }
}
