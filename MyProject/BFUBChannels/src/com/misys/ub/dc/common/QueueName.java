/* ********************************************************************************
 *  Copyright(c)2018  Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms. *
 *
 * ********************************************************************************
 */
package com.misys.ub.dc.common;

/**
 * <code>QueueName</code> type will hold the constant name of Queues
 */
public final class QueueName {

    private QueueName() {
    }

    /**
     * This queue has been used for Create SEPA Standing Order, Cancel SEPA Standing order Create ChequeBook Order, Change Personal Details
     * Response, Rejected Offer Reply, Failed Account Reply
     */
    public static final String QM_BFDC_UB_RESPONSE = "QM_BFDC_UB_Response";
}