/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: RatingDetails.java,v.1.0,Apr 21, 2009 5:15:48 PM ayerla
 *
 */
package com.misys.ub.common.almonde;


/**
 * @author ayerla
 * @date Apr 21, 2009
 * @project Universal Banking
 * @Description: RatingDetails represents the rating details.
 */

public class RatingDetails {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


    private String entityCode;
    private String entityType;
    private OverallRating overallRating;
    private CurrencyRating currencyRating;

    /**
     * @return the currencyRating
     */
    public CurrencyRating getCurrencyRating() {
        return currencyRating;
    }

    /**
     * @param currencyRating the currencyRating to set
     */
    public void setCurrencyRating(CurrencyRating currencyRating) {
        this.currencyRating = currencyRating;
    }

    /**
     * @return the overallRating
     */
    public OverallRating getOverallRating() {
        return overallRating;
    }

    /**
     * @param overallRating the overallRating to set
     */
    public void setOverallRating(OverallRating overallRating) {
        this.overallRating = overallRating;
    }
    /**
     * @return the entityCode
     */
    public String getEntityCode() {
        return entityCode;
    }
    /**
     * @param entityCode the entityCode to set
     */
    public void setEntityCode(String entityCode) {
        this.entityCode = entityCode;
    }
    /**
     * @return the entityType
     */
    public String getEntityType() {
        return entityType;
    }
    /**
     * @param entityType the entityType to set
     */
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
}

