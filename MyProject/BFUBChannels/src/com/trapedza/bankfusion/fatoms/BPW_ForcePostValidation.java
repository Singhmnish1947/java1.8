/* ********************************************************************************
 *  Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 *
 */

package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractBPW_ForcePostValidation;
import com.trapedza.bankfusion.steps.refimpl.IBPW_ForcePostValidation;

/**
 * This class Validates the ForcePost Flag and results in the posting actions of the Account
 */

public class BPW_ForcePostValidation extends AbstractBPW_ForcePostValidation implements IBPW_ForcePostValidation {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	public BPW_ForcePostValidation(BankFusionEnvironment env) {
		super(env);
	}

	private transient final static Log logger = LogFactory.getLog(BPW_ForcePostValidation.class.getName());

	public void process(BankFusionEnvironment env) {
		int mCode;
		int fpFlag;
		String amntSign;
		BigDecimal transAmnt;
		String transRef;

		mCode = getF_IN_MessageCode().intValue();
		fpFlag = getF_IN_ForcePostFlag().intValue();

		amntSign = getF_IN_TxnAmntSign();
		transAmnt = getF_IN_Trans_Amount();
		transRef = getF_IN_Trans_Ref();

		if ((mCode == 5) && amntSign.equals("+")) {

			setF_OUT_PossibleDuplicateIndicator("NO");
			switch (fpFlag) {
				case 0:
				case 1:
					setF_OUT_PostingActionIdMain("C");
					setF_OUT_TransCodeMain("CRV");
					setF_OUT_PostingActionIdContra("D");
					setF_OUT_TransCodeContra("DRV");
					break;

				case 2:
					setF_OUT_PostingActionIdMain("D");
					setF_OUT_TransCodeMain("DRV");
					setF_OUT_PostingActionIdContra("C");
					setF_OUT_TransCodeContra("CRV");
					break;

				case 3:
					String whereClause = "WHERE " + IBOTransaction.REFERENCE + "=? AND " + IBOTransaction.AMOUNT + "=?";
					ArrayList params = new ArrayList();

					params.add(transRef);
					logger.info("Params1" + transRef);
					params.add(transAmnt);
					logger.info("Params2" + transAmnt);
					ArrayList transaction = (ArrayList) env.getFactory().findByQuery(IBOTransaction.BONAME,
							whereClause, params, null);

					if (!transaction.isEmpty()) {
						setF_OUT_PossibleDuplicateIndicator("YES");
						logger.info("There in Yes");
					}
					else {
						setF_OUT_PossibleDuplicateIndicator("NO");
						logger.info("There in No");
					}
					setF_OUT_PostingActionIdMain("C");
					setF_OUT_TransCodeMain("CRV");
					setF_OUT_PostingActionIdContra("D");
					setF_OUT_TransCodeContra("DRV");
					break;

			}

		}
		if ((mCode == 5) && amntSign.equals("-")) {
			switch (fpFlag) {
				case 0:
				case 1:
					setF_OUT_PostingActionIdMain("D");
					setF_OUT_TransCodeMain("DRV");
					setF_OUT_PostingActionIdContra("C");
					setF_OUT_TransCodeContra("CRV");
					break;

				case 2:
					setF_OUT_PostingActionIdMain("C");
					setF_OUT_TransCodeMain("CRV");
					setF_OUT_PostingActionIdContra("D");
					setF_OUT_TransCodeContra("DRV");
					break;
				case 3:
					String whereClause = "WHERE " + IBOTransaction.REFERENCE + "=? AND " + IBOTransaction.AMOUNT + "=?";
					ArrayList params = new ArrayList();
					params.add(transRef);
					logger.info("Params1" + transRef);
					params.add(transAmnt);
					logger.info("Params2" + transAmnt);
					ArrayList transaction = (ArrayList) env.getFactory().findByQuery(IBOTransaction.BONAME,
							whereClause, params, null);

					if (!transaction.isEmpty()) {
						setF_OUT_PossibleDuplicateIndicator("YES");
						logger.info("There in Yes");
					}
					else {
						setF_OUT_PossibleDuplicateIndicator("NO");
						logger.info("There in No");
					}
					setF_OUT_PostingActionIdMain("D");
					setF_OUT_TransCodeMain("DRV");
					setF_OUT_PostingActionIdContra("C");
					setF_OUT_TransCodeContra("CRV");
					break;

			}
		}

	}

}
