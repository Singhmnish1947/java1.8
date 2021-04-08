/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 * $Log: ATMFeature.java,v $
 * Revision 1.4  2008/08/12 20:13:11  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.2.4.1  2008/07/03 17:56:10  vivekr
 * Moved from Dublin CVS Head (For BF1.01 package restructuring)
 *
 * Revision 1.7  2008/06/16 15:19:22  arun
 * UB Refactoring -
 * 1. Formatted/Organized imports
 * 2. BODefinitionException, InvalidExtensionPointException and references removed
 * 3. Removed ServerManager deprecated methods/variables
 * 4. BankfusionPropertyAccess removed - changed to BankfusionPropertySupport
 * 5. Exception Handling refactoring
 * 6. General Refactoring
 *
 * Revision 1.6  2008/06/12 10:49:25  arun
 *  RIO on Head
 *
 * Revision 1.2  2008/02/13 12:57:08  sushmax
 * ATM Balance Download
 *
 * Revision 1.2  2007/06/12 08:48:52  sushmax
 * ATM Team Share
 *
 * 
 */
package com.trapedza.bankfusion.features;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.bo.refimpl.IBOATMAccountDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.PostingHelper;
import com.trapedza.bankfusion.features.refimpl.AbstractATMFeature;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.postingengine.gateway.interfaces.IPostingMessage;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.systeminformation.PostingMessageConstants;

/**
 * The implementation of the ATM feature as an optional feature
 * for the core account product.

 */
public class ATMFeature extends AbstractATMFeature {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	/**
	 */

	private transient final static Log logger = LogFactory.getLog(ATMFeature.class.getName());

	/**
	 * serialVersionUID: PLEASE DONT CHANGE THIS VALUE!!
	 **/
	public static final long serialVersionUID = -312435;

	/**
	 * @param env
	 */
	public ATMFeature(BankFusionEnvironment env) {
		super(env);
	}

	/**
	 * 
	 * @see com.trapedza.bankfusion.features.AbstractFeature#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 */
	public void process(BankFusionEnvironment env) {

	}

	/**
	 * @see com.trapedza.bankfusion.features.AbstractFeature#postingEngineUpdate(com.trapedza.bankfusion.postingengine.gateway.interfaces.IPostingMessage)
	 */
	public void postingEngineUpdate(IPostingMessage message) {
		
		// Account Closure Process changes
		env = BankFusionThreadLocal.getBankFusionEnvironment();
        
		char messageType = message.getMessageType();

        if (messageType == PostingMessageConstants.ACCOUNT_CLOSURE_POSTING_MESSAGE) {

            HashMap params = new HashMap();
            params.put("accountID", message.getPrimaryID());

            IBOAttributeCollectionFeature account = getAccountValues(message, env);
            String subproductID = account.getF_PRODUCTCONTEXTCODE();

            params.put("AccountID", message.getPrimaryID());
            HashMap result = MFExecuter.executeMF("CreditDebitAccountCheck_SRV", env, params);// periodiTransactionCharge
            Boolean isATMcard = (Boolean) result.get("isCardNumberExist");
            Boolean isActiveBlockedCardExists = (Boolean) result.get("isActiveBlockedCardExists");
            if (isATMcard && isActiveBlockedCardExists) {
                HashMap input = new HashMap();
                input.put("eventLevel", subproductID);
                input.put("eventNumber", 40107342);
                MFExecuter.executeMF("UB_CNF_AccountClosureEventFatom_SRV", env, input);

            }

        }



		/**
		 * DO NOT DO ANYTHIN FOR RATE CHANGED.
		 */
		if(PostingHelper.isAccountRateChangeMessage(message) || messageType == PostingMessageConstants.INTEREST_ADJUSTMENT_MESSAGE)
			 return;
		boolean isForwardValued = message.isForwardValued();
		boolean isForwardValuedIntoValue = message.isForwardValuedIntoValue();
		if (isForwardValued && !isForwardValuedIntoValue)
			return;

		//PERF DE7180
		//IBOAttributeCollectionFeature accountItem = getAccountValues(message, env);
		//String accountId = accountItem.getBoID();
		String accountId = message.getPrimaryID();
		IBOATMAccountDetails atmAccountDetails = null;
		try {
			atmAccountDetails = (IBOATMAccountDetails) getFactory().findByPrimaryKey(IBOATMAccountDetails.BONAME,
					accountId,true);
			if (atmAccountDetails != null)
				atmAccountDetails.setF_BALANCEMOVEDFLAG(true);
		}
		catch (BankFusionException bfe) {
			//Processing should continue hence only debug info logged.
			if (logger.isDebugEnabled()){
			logger.debug("Error while find by query for IBOATMAccountDetails for accountId: " + accountId, bfe);
			}
		}
	}

	/**
	 * 
	 * @param requestMessage
	 * @param env
	 * @return
	 */
	private IBOAttributeCollectionFeature getAccountValues(IPostingMessage requestMessage, BankFusionEnvironment env) {

		return PostingHelper.retrieveAccount(requestMessage.getPrimaryID(), requestMessage.getBranchID(),
				requestMessage.getAcctCurrencyCode(), env);
	}

	/**
	 * 
	 * @return
	 */
	private IPersistenceObjectsFactory getFactory()
	{
		return BankFusionThreadLocal.getPersistanceFactory();
	}
	
}
