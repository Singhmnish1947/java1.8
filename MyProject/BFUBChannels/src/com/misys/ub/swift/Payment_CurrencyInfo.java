/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: Payment_CurrencyInfo.java,v.1.0,Aug 11, 2008 4:02:38 PM Shreyas.MR
 *
 */
package com.misys.ub.swift;

import java.io.Serializable;

import com.trapedza.bankfusion.core.CommonConstants;

/**
 * @author Shreyas.MR
 * @date Aug 11, 2008
 * @project Universal Banking
 * @Description:
 */

public class Payment_CurrencyInfo   implements Serializable{

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private String paymentDate = CommonConstants.EMPTY_STRING;
    private String currency_PaymentAmount = CommonConstants.EMPTY_STRING;

    public Payment_CurrencyInfo(){

    }

    public String getCurrency_PaymentAmount() {
        return currency_PaymentAmount;
    }

    public void setCurrency_PaymentAmount(String currency_PaymentAmount) {
        currency_PaymentAmount = currency_PaymentAmount;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        paymentDate = paymentDate;
    }


}

