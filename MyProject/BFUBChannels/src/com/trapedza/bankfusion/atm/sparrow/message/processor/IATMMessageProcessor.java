/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 * $Log: IATMMessageProcessor.java,v $
 * Revision 1.4  2008/08/12 20:15:04  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.2.4.1  2008/07/03 17:55:28  vivekr
 * Moved from Dublin CVS Head (For BF1.01 package restructuring)
 *
 * Revision 1.5  2008/06/16 15:18:45  arun
 * UB Refactoring -
 * 1. Formatted/Organized imports
 * 2. BODefinitionException, InvalidExtensionPointException and references removed
 * 3. Removed ServerManager deprecated methods/variables
 * 4. BankfusionPropertyAccess removed - changed to BankfusionPropertySupport
 * 5. Exception Handling refactoring
 * 6. General Refactoring
 *
 * Revision 1.4  2008/06/12 10:50:11  arun
 *  RIO on Head
 *
 * Revision 1.2  2007/11/14 11:06:53  prashantk
 * ATM Financial Message Processors
 *
 * Revision 1.9  2007/10/29 06:53:58  prashantk
 * Updated
 *
 * Revision 1.1.2.1  2007/08/08 18:42:09  prashantk
 * Message processor for ATM Messages
 *
 * Revision 1.8  2007/07/05 07:58:30  sushmax
 * *** empty log message ***
 *
 * 
 */
package com.trapedza.bankfusion.atm.sparrow.message.processor;

import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowMessage;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.core.BankFusionException;

/**
 * The IATMMessageProcessor interface class defines the ATM Message processors.
 */
public interface IATMMessageProcessor {
	/**
	 * <code>cvsRevision</code> = $Revision: 1.4 $
	 */
	public static final String cvsRevision = "$Revision: 1.4 $";

	public void execute(ATMSparrowMessage atmSparrowMessage, BankFusionEnvironment env);

}
