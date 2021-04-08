/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: GetRatingInfo.java,v.1.0,Apr 21, 2009 12:06:44 PM shaiks
 *
 */
package com.trapedza.bankfusion.fatoms;

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
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ALD_CaptureRatings_GetRatingInfo;
import com.trapedza.bankfusion.utils.BankFusionMessages;

import java.util.*;

/**
 * @author ssarpa
 * 
 */
public class GetRatingInfo extends AbstractUB_ALD_CaptureRatings_GetRatingInfo {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


    /**
     * @param env
     */
    public GetRatingInfo(BankFusionEnvironment env) {
        super(env);
    }

    private IPersistenceObjectsFactory factory;
    private static final String UBRATINGDLTSID = "UBRATINGDLTSID";
    private static final String UBRATINGCODE = "UBRATINGCODE";
    private static final String UBRATINGTERM = "UBRATINGTERM";
    private static final String UBRATINGVALUE = "UBRATINGVALUE";
    private static final String UBOVRLRATINGID = "UBOVRLRATINGID";
    private static final String UBRATINGENTITYTYPE = "UBRATINGENTITYTYPE";
    private static final String UBRATINGENTITYID = "UBRATINGENTITYID";
    private static final String UBCURRENCYRATINGID = "UBCURRENCYRATINGID";
    private static final String UBISOCURRENCYCODE = "UBISOCURRENCYCODE";

    private static final String CURRENCY_RATING_QUERY = CommonConstants.SELECT + CommonConstants.SPACE + "T1."
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
            + IBOUB_ALD_CURRENCYRATING.UBRATINGDLTSID + " AND T1." + IBOUBTB_RATINGDETAILS.UBRATINGCODE + CommonConstants.EQUAL
            + " ? AND T1." + IBOUBTB_RATINGDETAILS.UBRATINGTERM + CommonConstants.EQUAL + " ? AND T2."
            + IBOUB_ALD_CURRENCYRATING.UBRATINGENTITYID + CommonConstants.EQUAL + " ? AND T2."
            + IBOUB_ALD_CURRENCYRATING.UBISOCURRENCYCODE + CommonConstants.EQUAL + " ? ";

    private static final String OVERALL_RATING_QUERY = CommonConstants.SELECT + CommonConstants.SPACE + "T1."
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
            + " AND T1." + IBOUBTB_RATINGDETAILS.UBRATINGCODE + CommonConstants.EQUAL + " ? AND T1."
            + IBOUBTB_RATINGDETAILS.UBRATINGTERM + CommonConstants.EQUAL + " ? AND T2." + IBOUB_ALD_OVERALLRATING.UBRATINGENTITYID
            + CommonConstants.EQUAL + CommonConstants.QUESTION;

    private static final String ENTITY_OVERALL_RATING_QUERY = CommonConstants.SELECT + CommonConstants.SPACE + "T1."
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
            + " AND T2." + IBOUB_ALD_OVERALLRATING.UBRATINGENTITYID + CommonConstants.EQUAL + CommonConstants.QUESTION;

    private static final String ENTITY_CURRENCY_RATING_QUERY = CommonConstants.SELECT + CommonConstants.SPACE + "T1."
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
            + IBOUB_ALD_CURRENCYRATING.UBRATINGDLTSID + " AND T2." + IBOUB_ALD_CURRENCYRATING.UBRATINGENTITYID
            + CommonConstants.EQUAL + " ? ";
    private static final String MODIFIED = "Modified";

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trapedza.bankfusion.steps.refimpl.AbstractUB_ALD_CaptureRatings_GetRatingInfo#process
     * (com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
     */
    public void process(BankFusionEnvironment env) {
        factory = BankFusionThreadLocal.getPersistanceFactory();
        if (isF_IN_getAllRatings()) {
            if (isF_IN_IsCurrencyCode())
                fetchAllCurrencyRatingDetails();
            else fetchAllOverallRatingDetails();
        }
        else if (isF_IN_getSelectedValues()) {
            VectorTable vectorTable = getF_IN_RatingDetails();
            if (vectorTable.size() <= 0)
                throw new BankFusionException(40413025, BankFusionMessages.getFormattedMessage(40413025,null));
            Map rowMap = vectorTable.getRowTags(vectorTable.getSelectedRowIndex());
            setF_OUT_RatingTerm((String) rowMap.get(CommonConstants.getTagName(IBOUBTB_RATINGDETAILS.UBRATINGTERM)));
            setF_OUT_RatingValue((String) rowMap.get(CommonConstants.getTagName(IBOUBTB_RATINGDETAILS.UBRATINGVALUE)));
            setF_OUT_AgencyCode((String) rowMap.get(CommonConstants.getTagName(IBOUBTB_RATINGDETAILS.UBRATINGCODE)));
            if (isF_IN_IsCurrencyCode()) {
                setF_OUT_ISOCurrencyCode((String) rowMap
                        .get(CommonConstants.getTagName(IBOUB_ALD_CURRENCYRATING.UBISOCURRENCYCODE)));
            }
        }
        else {
            if (isF_IN_IsCurrencyCode())
                verifyCurrencyRating();
            else verifyOverallRatingDetails();
        }
    }

    /**
     * Method Description: fetches all Overall Rating Details of an entity.
     */
    private void fetchAllOverallRatingDetails() {
        ArrayList params = new ArrayList();
        params.add(getF_IN_EntityID());
        List ratingList = factory.executeGenericQuery(ENTITY_OVERALL_RATING_QUERY, params, null, false);
        setF_OUT_RatingDetails(convertToVectorTable(ratingList));

    }

    /**
     * Method Description: fetches all Currency Rating Details of an entity.
     */
    private void fetchAllCurrencyRatingDetails() {
        ArrayList params = new ArrayList();
        params.add(getF_IN_EntityID());
        List ratingList = factory.executeGenericQuery(ENTITY_CURRENCY_RATING_QUERY, params, null, false);
        setF_OUT_RatingDetails(convertToVectorTable(ratingList));
    }

    /**
     * Method Description: Converts a List to vector.
     * @param dataList
     * @return
     */
    private VectorTable convertToVectorTable(List dataList) {

        VectorTable result = new VectorTable();

        if (dataList != null && dataList.size() != 0) {
            int index = 1;
            Iterator iterator = dataList.iterator();
            while (iterator.hasNext()) {
                SimplePersistentObject rating = (SimplePersistentObject) iterator.next();
                Map dataMap = rating.getDataMap();
                Map<String, Object> rowMap = new HashMap<String, Object>();
                if (index == 1)
                    rowMap.put(CommonConstants.SELECT, Boolean.TRUE);
                else rowMap.put(CommonConstants.SELECT, Boolean.FALSE);
                rowMap.put(CommonConstants.SRNO, index);
                rowMap.put(CommonConstants.getTagName(IBOUBTB_RATINGDETAILS.UBRATINGTERM), dataMap.get(UBRATINGTERM));
                rowMap.put(CommonConstants.getTagName(IBOUBTB_RATINGDETAILS.UBRATINGVALUE), dataMap.get(UBRATINGVALUE));
                rowMap.put(CommonConstants.getTagName(IBOUBTB_RATINGDETAILS.UBRATINGCODE), dataMap.get(UBRATINGCODE));
                rowMap.put(MODIFIED,null);
                if (isF_IN_IsCurrencyCode()) {
                    rowMap.put(CommonConstants.getTagName(IBOUB_ALD_CURRENCYRATING.UBCURRENCYRATINGIDPK), dataMap
                            .get(UBCURRENCYRATINGID));
                    rowMap.put(CommonConstants.getTagName(IBOUB_ALD_CURRENCYRATING.UBRATINGDLTSID), dataMap.get(UBRATINGDLTSID));
                    rowMap
                            .put(CommonConstants.getTagName(IBOUB_ALD_CURRENCYRATING.UBRATINGENTITYID), dataMap
                                    .get(UBRATINGENTITYID));
                    rowMap.put(CommonConstants.getTagName(IBOUB_ALD_CURRENCYRATING.UBISOCURRENCYCODE), dataMap
                            .get(UBISOCURRENCYCODE));
                }
                else {
                    rowMap.put(CommonConstants.getTagName(IBOUB_ALD_OVERALLRATING.UBOVRLRATINGIDPK), dataMap.get(UBOVRLRATINGID));
                    rowMap.put(CommonConstants.getTagName(IBOUB_ALD_OVERALLRATING.UBRATINGDLTSID), dataMap.get(UBRATINGDLTSID));
                    rowMap.put(CommonConstants.getTagName(IBOUB_ALD_OVERALLRATING.UBRATINGENTITYID), dataMap.get(UBRATINGENTITYID));
                }
                result.addAll(new VectorTable(rowMap));
                index++;
            }
        }
        return result;
    }

    /**
     * Method Description: Validates Overall Rating
     */
    private void verifyOverallRatingDetails() {
        ArrayList params = new ArrayList();
        params.add(getF_IN_RatingAgencyCode());
        params.add(getF_IN_RatingAgencyTerm());
        params.add(getF_IN_EntityID());
        List overAllRatinglist = factory.executeGenericQuery(OVERALL_RATING_QUERY, params, null, false);
        String overallRatingID = null;
        int noofRows = 0;
        if (overAllRatinglist != null && overAllRatinglist.size() != 0) {
            Iterator iterator = overAllRatinglist.iterator();
            while (iterator.hasNext()) {
                SimplePersistentObject overallRating = (SimplePersistentObject) iterator.next();
                overallRatingID = (String) overallRating.getDataMap().get(UBOVRLRATINGID);
                noofRows++;
            }
        }
        setF_OUT_NoofRows(noofRows);
        setF_OUT_RatingsIDPK(overallRatingID);
    }

    /**
     * Method Description: validates Currency Rating.
     */
    private void verifyCurrencyRating() {
        ArrayList params = new ArrayList();
        params.add(getF_IN_RatingAgencyCode());
        params.add(getF_IN_RatingAgencyTerm());
        params.add(getF_IN_EntityID());
        params.add(getF_IN_ISOCurrencyCode());
        List currencyRatinglist = factory.executeGenericQuery(CURRENCY_RATING_QUERY, params, null, false);
        String currencyRatingID = null;
        int noofRows = 0;
        if (currencyRatinglist != null && currencyRatinglist.size() != 0) {
            Iterator iterator = currencyRatinglist.iterator();
            while (iterator.hasNext()) {
                SimplePersistentObject currencyRating = (SimplePersistentObject) iterator.next();
                currencyRatingID = (String) currencyRating.getDataMap().get(UBCURRENCYRATINGID);
                noofRows++;
            }
        }
        setF_OUT_NoofRows(noofRows);
        setF_OUT_RatingsIDPK(currencyRatingID);
    }

}
