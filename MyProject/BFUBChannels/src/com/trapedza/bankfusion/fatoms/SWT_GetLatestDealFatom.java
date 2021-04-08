
/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOSWTDisposal;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_GetLatestDealDetails;
import com.trapedza.bankfusion.utils.BankFusionMessages;

public class SWT_GetLatestDealFatom extends AbstractSWT_GetLatestDealDetails {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private final String fetchDisposal = "SELECT T1.boID AS SWTDISPOSALID, T1.f_POSTDATE as POSTDATE, "

	+ "T1.f_VALUEDATE AS VALUEDATE, T1.f_INTERESTRATE AS INTERESTRATE, T1.f_CUSTACCOUNTID AS CUSTACCOUNTID, "

	+ "T1.f_CONTRAACCOUNTID AS CONTRAACCOUNTID,T1.f_TRANSACTIONAMOUNT AS TRANSACTIONAMOUNT FROM "
			+ IBOSWTDisposal.BONAME + " T1 WHERE T1.boID NOT IN ("

			+ "SELECT T2.f_PREVSWTDISPOSALID FROM " + IBOSWTDisposal.BONAME
			+ " T2 WHERE T2.f_DEALNO = ? AND T2.f_PREVSWTDISPOSALID is not null) and T1.f_DEALNO = ?";

	private final String fetchDisposal1 = "SELECT T1.boID AS SWTDISPOSALID, T1.f_POSTDATE as POSTDATE, "

	+ "T1.f_VALUEDATE AS VALUEDATE, T1.f_INTERESTRATE AS INTERESTRATE, T1.f_CUSTACCOUNTID AS CUSTACCOUNTID, "

	+ "T1.f_CONTRAACCOUNTID AS CONTRAACCOUNTID,T1.f_TRANSACTIONAMOUNT AS TRANSACTIONAMOUNT FROM "
			+ IBOSWTDisposal.BONAME + " T1 WHERE T1.boID  = ?";

	public SWT_GetLatestDealFatom(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	public void process(BankFusionEnvironment env) {
		boolean flag = true;
		ArrayList DealDetailParams = new ArrayList();
		Iterator DealIterator = null;
		if (getF_IN_SWTREF() != null && getF_IN_SWTREF().trim().length() > 0) {
			DealDetailParams.add(getF_IN_SWTREF());
			DealIterator = env.getFactory().executeGenericQuery(fetchDisposal1, DealDetailParams, null).iterator();
		}
		else if (getF_IN_DEALNO() != null && getF_IN_DEALNO().trim().length() > 0) {
			String Deal = getF_IN_DEALNO();
			DealDetailParams.add(Deal);
			DealDetailParams.add(Deal);

			DealIterator = env.getFactory().executeGenericQuery(fetchDisposal, DealDetailParams, null).iterator();
		}
		SimplePersistentObject DealDetails = null;
		while (null!= DealIterator && DealIterator.hasNext()) {//IT SHOULD HAVE ONE RECORD
			flag = false;
			DealDetails = (SimplePersistentObject) DealIterator.next();
			setF_OUT_POSTDATE((Date) DealDetails.getDataMap().get("POSTDATE"));
			setF_OUT_VALUEDATE((Date) DealDetails.getDataMap().get("VALUEDATE"));
			setF_OUT_MAINACCOUNTID((String) DealDetails.getDataMap().get("CUSTACCOUNTID"));
			setF_OUT_CONTRAACCOUNTID((String) DealDetails.getDataMap().get("CONTRAACCOUNTID"));
			setF_OUT_INTRESTRATE((BigDecimal) DealDetails.getDataMap().get("INTERESTRATE"));
			setF_OUT_AMOUNT((BigDecimal) DealDetails.getDataMap().get("TRANSACTIONAMOUNT"));
			setF_OUT_DISPOSEID((String) DealDetails.getDataMap().get("SWTDISPOSALID"));

		}
		if (flag) {
			/*String eMessage = BankFusionMessages.getFormattedMessage(BankFusionMessages.ERROR_LEVEL, 9436, env,
					new Object[] { "Record not Found" });
			throw new BankFusionException(9436, eMessage);*/
			EventsHelper.handleEvent(ChannelsEventCodes.E_ERROR, new Object[] { "Record not Found" }, new HashMap(), env);
		}

	}
}
