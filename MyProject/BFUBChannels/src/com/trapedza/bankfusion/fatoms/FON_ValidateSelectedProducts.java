/* ***********************************************************************************
 * Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * $Id: FON_ValidateSelectedProducts.java,v 1.6 2008/08/12 20:14:30 vivekr Exp $
 * **********************************************************************************
 * 
 * $Log: FON_ValidateSelectedProducts.java,v $
 * Revision 1.6  2008/08/12 20:14:30  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.4.4.1  2008/07/03 17:55:45  vivekr
 * Moved from Dublin CVS Head (For BF1.01 package restructuring)
 *
 * Revision 1.7  2008/06/19 09:26:33  arun
 * FatomUtils' usage of getBankFusionException changed to call BankFusionException directly
 *
 * Revision 1.6  2008/06/16 15:23:56  arun
 * UB Refactoring -
 * 1. Formatted/Organized imports
 * 2. BODefinitionException, InvalidExtensionPointException and references removed
 * 3. Removed ServerManager deprecated methods/variables
 * 4. BankfusionPropertyAccess removed - changed to BankfusionPropertySupport
 * 5. Exception Handling refactoring
 * 6. General Refactoring
 *
 *
 *
 */

package com.trapedza.bankfusion.fatoms;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractFON_ValidateSelectedProducts;
import com.trapedza.bankfusion.core.EventsHelper;

public class FON_ValidateSelectedProducts extends AbstractFON_ValidateSelectedProducts {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	/**
	 * Logger instance
	 */
	private transient final static Log log = LogFactory.getLog(FON_ValidateSelectedProducts.class.getName());

	/**
	 * Default value in the selected products field
	 */
	String defaultValue = CommonConstants.EMPTY_STRING;

	/**
	 * Constructor
	 * @param env
	 */
	public FON_ValidateSelectedProducts(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	/**
	 * implements process(...) method in AbstractFON_ValidateSelectedSupervisors
	 */
	public void process(BankFusionEnvironment env) {
		//		Validate suspense pseudonym is selected or not
		if (getF_IN_fontisSuspensePseudonym().trim().equalsIgnoreCase(defaultValue)) {
			/*throw new BankFusionException(9017, new Object[] { getF_IN_fontisSuspensePseudonym() }, log, env);*/
			EventsHelper.handleEvent(ChannelsEventCodes.E_SELECT_AN_SUSPENSE_ACCOUNT_PSEUDONYM,new Object[] {getF_IN_fontisSuspensePseudonym() } , new HashMap(), env);
		}

		//		Validate sub product list selected
		String[] productsList = { getF_IN_product1(), getF_IN_product2(), getF_IN_product3(), getF_IN_product4(),
				getF_IN_product5(), getF_IN_product6(), getF_IN_product7(), getF_IN_product8(), getF_IN_product9(),
				getF_IN_product10() };

		//		Check whether any duplicate value has been input
		for (int i = 0; i < productsList.length; i++) {
			for (int j = i + 1; j < productsList.length; j++) {
				if (productsList[i].equalsIgnoreCase(productsList[j])
						&& !productsList[i].trim().equalsIgnoreCase(defaultValue)) {
					/*throw new BankFusionException(9008, new Object[] { productsList[i] }, log, env);*/
					EventsHelper.handleEvent(ChannelsEventCodes.E_SUB_PRODUCT_HAS_BEEN_SELECTED_MORE_THAN_ONCE,new Object[] {productsList[i]} , new HashMap(), env);
				}
			}
		}
	}
}
