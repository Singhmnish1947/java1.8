/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.util.HashMap;

import com.misys.bankfusion.subsystem.security.runtime.impl.CurrencyUtil;
import com.misys.ub.common.events.CommonsEventCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOBicCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOCurrency;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_CorresAccountBICValidate;

/**
 * @author prasanthj
 * 
 */
public class SWT_CorresAccountBICValidate extends AbstractSWT_CorresAccountBICValidate {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	private static final String CURRENCY_CODE = "Currency code";

	private static final String BIC = "BIC";

	private static final String EMPTY_STRING = CommonConstants.EMPTY_STRING;

	/**
	 * @param env
	 */
	public SWT_CorresAccountBICValidate(BankFusionEnvironment env) {
		super(env);

	}

	public void process(BankFusionEnvironment env) {
		// TODO Auto-generated method stub
		super.process(env);

		final String bic = getF_IN_bic();
		final String iso_currency = getF_IN_iso_currrency();

		if (bic == null || bic.trim().equals(EMPTY_STRING) || !isValidBIC(env)) {
			//throw new BankFusionException(444, new String[] { BIC, bic }, logger, env);
			EventsHelper.handleEvent(CommonsEventCodes.E_WITH_ID_ALREADY_EXISTS, new String[] { BIC, bic }, new HashMap(), env);
		}

		if (iso_currency == null || iso_currency.trim().equals(EMPTY_STRING) || !isValidCurrencyCode(env)) {
			//throw new BankFusionException(444, new String[] { CURRENCY_CODE, iso_currency }, logger, env);
			EventsHelper.handleEvent(CommonsEventCodes.E_WITH_ID_ALREADY_EXISTS, new String[] { CURRENCY_CODE, iso_currency }, new HashMap(), env);
		}

	}

	/**
	 * Check for integrity of currency code
	 * 
	 * @param env
	 * @return
	 * @
	 */
	private boolean isValidCurrencyCode(BankFusionEnvironment env) {

        IBOCurrency currCode = CurrencyUtil.getCurrencyDetailsOfCurrentZone(getF_IN_iso_currrency());
        if (currCode == null) {
			return false;
		}

		return true;
	}

	/**
	 * 
	 * Check for the correctness of BIC
	 * 
	 * @param env
	 * @return
	 * @
	 */
	private boolean isValidBIC(BankFusionEnvironment env) {
        IBOBicCodes bicCode = (IBOBicCodes) env.getFactory().findByPrimaryKey(IBOBicCodes.BONAME, getF_IN_bic(), true);
        if (bicCode == null) {
			return false;
		}

		return true;
	}
}
