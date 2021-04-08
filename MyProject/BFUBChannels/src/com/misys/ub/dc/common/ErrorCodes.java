package com.misys.ub.dc.common;

/**
 * @author susikdar <code>ErrorCodes</code> type are collection of event codes for both failure &
 *         success message responses
 */
public final class ErrorCodes {

    private ErrorCodes() {
    }

    /**
     * Customer KYC status is not complied, can not enable the customer
     */
    public static final String CUST_KYC_STATUS_NOT_COMPLIED_UB = "40109361";

    /**
     * Raised during Account Creation
     */
    public static final String EVT_ACCOUNT_CREATION = "40430056";

    /**
     * Generic error code for any kind of unexpected exception
     */
    public static final String UNEXPECTED_ERROR = "20020832";

    /**
     * Value against {0} is blank/inappropriate
     */
    public static final Integer INVALID_INPUT_VALUE = 40509733;

    /**
     * Document upload failed. Please retry
     */
    public static final String DOCUMENT_UPLOAD_FAILURE = "40112762";

	/**
     * When party Dedup match found, party creation would be rejected.
     */
    public static final String PARTY_DEDUP_MATCH_REJECTED = "30500121";

}
