/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: BatchInformation.java,v.1.0,Apr 21, 2009 3:25:47 PM ayerla
 *
 */
package com.misys.ub.common.almonde;

import java.util.Vector;

/**
 * @author ayerla
 * @date Apr 21, 2009
 * @project Universal Banking
 * @Description: BatchInformation represents the batch details.
 */

public class BatchInformation {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * @param ratingDetails the ratingDetails to set
     */
    public void setRatingDetails(Vector<RatingDetails> ratingDetails) {
        this.ratingDetails = ratingDetails;
    }
    /**
     * @return the batchReference
     */
    public String getBatchReference() {
        return batchReference;
    }
    /**
     * @param batchReference the batchReference to set
     */
    public void setBatchReference(String batchReference) {
        this.batchReference = batchReference;
    }
    /**
     * @return the ratingDetails
     */
    public Vector<RatingDetails> getRatingDetails() {
        return ratingDetails;
    }
    private String batchReference;
    private String description;
    private Vector<RatingDetails> ratingDetails;
}

