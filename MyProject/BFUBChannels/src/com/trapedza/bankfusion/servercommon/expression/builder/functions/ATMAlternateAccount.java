package com.trapedza.bankfusion.servercommon.expression.builder.functions;

import java.util.ArrayList;
import java.util.List;

import com.trapedza.bankfusion.bo.refimpl.IBOPseudonymAccountMap;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

public class ATMAlternateAccount {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	private static String SORT_CONTEXT = null;

	private static String SORT_CONTEXT_VALUE = null;

	/**
	 * Query for retrieving MainAccountNumber for an AlternateAccountNumber
	 */
	private static final String findMainAccountNo = " WHERE "
			+ IBOPseudonymAccountMap.PSEUDONAME + " = ? AND "
			+ IBOPseudonymAccountMap.SORTCONTEXT + " = ? AND "
			+ IBOPseudonymAccountMap.SORTCONTEXTVALUE + " = ? ";

	/**
	 * This method returns MainAccountNumber for an Alternate AccountNumber If
	 * there is no MainAccountNumber for an Alternate AccountNumber, return
	 * input account number
	 * 
	 * @param accountNumber
	 * @param sortContext
	 * @param sortContextValue
	 * @return returnAccNumber
	 */
	public static String run(String accountNumber, String sortContext,
			String sortContextValue) {
		String returnAccNumber = null;
		SORT_CONTEXT = sortContext;
		SORT_CONTEXT_VALUE = sortContextValue;

		// retrieve main account number
		ArrayList params = new ArrayList();
		params.add(accountNumber);
		params.add(SORT_CONTEXT);
		params.add(SORT_CONTEXT_VALUE);

		// If there is no MainAccountNumber for an Alternate AccountNumber,
		// return input account number
		returnAccNumber = accountNumber;

		List<IBOPseudonymAccountMap> pseudonymAccountMapList = BankFusionThreadLocal
				.getPersistanceFactory().findByQuery(
						IBOPseudonymAccountMap.BONAME, findMainAccountNo,
						(ArrayList) params, null, false);
		if (!pseudonymAccountMapList.isEmpty()) {
			IBOPseudonymAccountMap pseudonymAccountMapObj = pseudonymAccountMapList
					.get(0);
			if (null != pseudonymAccountMapObj) {
				returnAccNumber = pseudonymAccountMapObj.getF_ACCOUNTID();
			}
		}

		return returnAccNumber;
	}
}
