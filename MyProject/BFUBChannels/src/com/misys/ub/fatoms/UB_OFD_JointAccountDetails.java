/**
 * 
 */
package com.misys.ub.fatoms;

/**
 * @author Ram.Pandey
 *
 */


import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.subsystem.persistence.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_CNF_JOINTACCOUNT;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_OFD_JointAccountDetails;

/**
 * @author Ram.Pandey
 * 
 */

public class UB_OFD_JointAccountDetails extends
		AbstractUB_OFD_JointAccountDetails {

	private static final long serialVersionUID = 1L;

	private transient final static Log logger = LogFactory
			.getLog(UB_OFD_JointAccountDetails.class);

	public UB_OFD_JointAccountDetails() {
		super();
	}

	public UB_OFD_JointAccountDetails(BankFusionEnvironment env) {
		super();
	}
	private static String accountNumber = CommonConstants.EMPTY_STRING;
	private static String customerCode = CommonConstants.EMPTY_STRING;
	private static String query = " WHERE "+ IBOUB_CNF_JOINTACCOUNT.ACCOUNTID + " = ? ";;
	public void process(BankFusionEnvironment env) throws BankFusionException {
		accountNumber = getF_IN_inputAccountID();
		IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
		ArrayList<String> queryParams = new ArrayList<String>();
		queryParams.add(accountNumber);
		HashMap listOfJointCustomer = (HashMap) factory
				.findFirstByQuery(IBOUB_CNF_JOINTACCOUNT.BONAME,query,queryParams, true);
		if (!(listOfJointCustomer == null)) {
		for(int i=0; i<= listOfJointCustomer.size(); i++)
		{
			customerCode = "";
		}
			
		
			
			
			
		}
		
		
	}
}
