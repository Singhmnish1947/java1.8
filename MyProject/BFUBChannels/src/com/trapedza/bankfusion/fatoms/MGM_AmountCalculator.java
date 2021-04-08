/* ********************************************************************************
 *  Copyright(c)2007  Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 * 
 * $Id: MGM_AmountCalculator.java,v 1.3 2008/08/12 20:14:29 vivekr Exp $
 *  
 * $Log: MGM_AmountCalculator.java,v $
 * Revision 1.3  2008/08/12 20:14:29  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.1.4.1  2008/07/03 17:55:53  vivekr
 * Moved from Dublin CVS Head (For BF1.01 package restructuring)
 *
 * Revision 1.5  2008/06/16 15:21:34  arun
 * UB Refactoring -
 * 1. Formatted/Organized imports
 * 2. BODefinitionException, InvalidExtensionPointException and references removed
 * 3. Removed ServerManager deprecated methods/variables
 * 4. BankfusionPropertyAccess removed - changed to BankfusionPropertySupport
 * 5. Exception Handling refactoring
 * 6. General Refactoring
 *
 * Revision 1.4  2008/06/12 10:51:53  arun
 *  RIO on Head
 *
 * Revision 1.1  2007/09/28 12:39:43  nileshk
 * Added for 3.3a MoneyGram release
 *
 * 
 */

package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractMGM_AmountCalculator;
import com.trapedza.bankfusion.steps.refimpl.IMGM_AmountCalculator;

/**
 * This fatom is used to calculate the debit amount from cash or customer account.
 * @author nileshk
 *
 */
public class MGM_AmountCalculator extends AbstractMGM_AmountCalculator implements IMGM_AmountCalculator {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	/**
	 * Logger defined.
	 */
	private transient final static Log logger = LogFactory.getLog(MGM_AmountCalculator.class.getName());

	/**
	 * Constructor
	 * @param env
	 */
	public MGM_AmountCalculator(BankFusionEnvironment env) {
		super(env);
	}

	/**
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractMGM_AmountCalculator#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 * @param env The BankFusion Environment
	 * @
	 */
	public void process(BankFusionEnvironment environment) {
		try {
			BigDecimal CommAmount1 = this.getF_IN_CommAmount1();
			BigDecimal CommAmount2 = this.getF_IN_CommAmount2();
			BigDecimal CommAmount3 = this.getF_IN_CommAmount3();
			BigDecimal totalSendAmount = this.getF_IN_totalSendAmount();
			BigDecimal debitAmount = null;
			debitAmount = totalSendAmount.add(CommAmount1).add(CommAmount2).add(CommAmount3);
			this.setF_OUT_debitAmount(debitAmount);
		}
		catch (Exception e) {
			logger.error("Error", e);
		}
	}

}
