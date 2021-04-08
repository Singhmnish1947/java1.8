/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: OverallRating.java,v.1.0,Apr 24, 2009 12:15:34 PM ayerla
 *
 */
package com.misys.ub.common.almonde;
/**
 * @author ayerla
 * @date Apr 24, 2009
 * @project Universal Banking
 * @Description: OverallRating represents the Overall Rating details.
 */

public class OverallRating {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


    /**
     * @return the agencyCode
     */
    public String getAgencyCode() {
        return agencyCode;
    }
    /**
     * @param agencyCode the agencyCode to set
     */
    public void setAgencyCode(String agencyCode) {
        this.agencyCode = agencyCode;
    }
    /**
     * @return the term
     */
    public String getTerm() {
        return term;
    }
    /**
     * @param term the term to set
     */
    public void setTerm(String term) {
        this.term = term;
    }
    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }
    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }
    private String agencyCode;
    private String term;
    private String value;
    
}

