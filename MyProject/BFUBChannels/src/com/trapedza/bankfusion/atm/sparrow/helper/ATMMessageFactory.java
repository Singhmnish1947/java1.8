/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 * $Log: ATMMessageFactory.java,v $
 * Revision 1.9  2008/08/31 13:19:52  nishantd
 * Interface sprint 1 delivery
 *
 * Revision 1.1  2008/08/28 05:21:00  nishantd
 * bug 10399
 *
 * Revision 1.8  2008/08/20 00:24:57  nishantd
 * Taken from Rel-Branch-1_01 . Checked in as a merging activity.
 *
 * Revision 1.7  2008/08/12 20:15:33  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.5.4.1  2008/07/03 17:55:26  vivekr
 * Moved from Dublin CVS Head (For BF1.01 package restructuring)
 *
 * Revision 1.5  2008/06/12 10:50:11  arun
 *  RIO on Head
 *
 * Revision 1.5  2008/02/08 15:22:57  sushmax
 * Checkins after Sprint Cycle 5 & 6.
 *
 * Revision 1.4  2008/02/06 08:02:14  sushmax
 * *** empty log message ***
 *
 * Revision 1.3  2008/01/30 11:19:33  prashantk
 * For Reversals
 *
 * Revision 1.4  2008/01/28 07:42:38  sushmax
 * ATM Helper Files
 *
 * Revision 1.2  2008/01/24 15:51:58  sushmax
 * changed to include the new classes
 *
 * Revision 1.1  2008/01/24 08:09:09  sushmax
 * Changed to add the correction class
 *
 * Revision 1.3  2007/11/28 16:27:14  prashantk
 * POS Fixes after Dev Test
 *
 * Revision 1.2  2007/11/14 11:03:18  prashantk
 * ATM Common Helper Classes
 *
 * Revision 1.6  2007/10/29 06:52:47  prashantk
 * Updated
 *
 * Revision 1.1.2.1  2007/08/08 18:40:20  prashantk
 * ATM Message Factory
 *
 * Revision 1.4  2007/06/19 04:20:11  sushmax
 * *** empty log message ***
 *
 * Revision 30/01/09 Debjit Basu
 * Changes for supporting Smart Card messages - 581, 584, 589
 * 
 */
package com.trapedza.bankfusion.atm.sparrow.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.atm.sparrow.configuration.ATMConfigCache;
import com.trapedza.bankfusion.atm.sparrow.configuration.ATMControlDetails;
import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowMessage;
import com.trapedza.bankfusion.atm.sparrow.message.processor.ATMCorrectionTxns;
import com.trapedza.bankfusion.atm.sparrow.message.processor.ATMFundsTransfer;
import com.trapedza.bankfusion.atm.sparrow.message.processor.ExtNwCashWithdrawal;
import com.trapedza.bankfusion.atm.sparrow.message.processor.IATMMessageProcessor;
import com.trapedza.bankfusion.atm.sparrow.message.processor.LocalCashWithdrawal;
import com.trapedza.bankfusion.atm.sparrow.message.processor.LocalLoro;
import com.trapedza.bankfusion.atm.sparrow.message.processor.POSCashProcessor;
import com.trapedza.bankfusion.atm.sparrow.message.processor.TravellersCheque;
import com.trapedza.bankfusion.atm.sparrow.message.processor.UB_ATM_SPA_CashDeposit;
import com.trapedza.bankfusion.atm.sparrow.message.processor.UB_ATM_SPA_FundsTransferFromSmartPurse;
import com.trapedza.bankfusion.atm.sparrow.message.processor.UB_ATM_SPA_FundsTransferToSmartPurse;
import com.trapedza.bankfusion.atm.sparrow.message.processor.UB_ATM_SPA_ReversalAndCorrectionTxns;
import com.trapedza.bankfusion.atm.sparrow.message.processor.UB_ATM_SPA_SmartCardProcessor;
import com.trapedza.bankfusion.atm.sparrow.message.processor.UB_ATM_SPA_SmartPurseCashWithdrawal;
import com.trapedza.bankfusion.atm.sparrow.message.processor.UB_ATM_SPA_UtilityBillPayment;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;

/**
 * The ATMMessageFactory implements getMessageProcessor method which sreate 
 * instances of valid messages to be processed.  
 */
public class ATMMessageFactory {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	
	/**
	 */
	ATMControlDetails controlDetails = null;
	private static final String SMART_CARD = "S";
	private static final String DEBIT_CARD = "D";

	/**
	 * Holds the reference for logger object
	 */
	private transient final static Log logger = LogFactory.getLog(ATMMessageFactory.class.getName());

	/**
	 * This method creates an instance of the message processor required for each message.
	 * @returns IATMMessageProcessor
	 *   
	 */
	public IATMMessageProcessor getMessageProcessor(ATMSparrowMessage atmSparrowMessage, BankFusionEnvironment env) {
		//TODO create instances of messageProcessors
		logger.debug("In getMessageProcessor()");
		IATMMessageProcessor messageProcessor = null;
		String atmMessageType = atmSparrowMessage.getMessageType() + atmSparrowMessage.getTransactionType();
		String messageType = atmSparrowMessage.getMessageType();
		controlDetails = ATMConfigCache.getInstance().getInformation(env);

		String magStripTrans=controlDetails.getProcessMagstripeTxns();
		if (atmMessageType.equals("580") || atmMessageType.equals("520") || atmMessageType.equals("585")) {
			messageProcessor = new LocalCashWithdrawal();
		}
		//Added new entry for 561 message
		if (atmMessageType.equals("561")){
			messageProcessor = new UB_ATM_SPA_CashDeposit();
		}
		if (atmMessageType.equals("529")){
			messageProcessor = new TravellersCheque();
		} 
		//Message Type 565 is for utility bill payments
		if (atmMessageType.equals("565")){
			messageProcessor = new UB_ATM_SPA_UtilityBillPayment();
		} 
		// Changes for supporting Smart Card messages - 581, 584, 589 - Start
		//Smart Purse Pool Account to Card Holder's Account.
        if (atmMessageType.equals("589")){
            messageProcessor = new UB_ATM_SPA_FundsTransferFromSmartPurse();
        } 
       //Card Holder's Account to Smart Purse Pool Account.
        if (atmMessageType.equals("584")){
            messageProcessor = new UB_ATM_SPA_FundsTransferToSmartPurse();
        } 
       //Cash withdrawal from Smart Card Purse.
        if (atmMessageType.equals("581")){
            messageProcessor = new UB_ATM_SPA_SmartPurseCashWithdrawal();
        }
        // Changes for supporting Smart Card messages - 581, 584, 589 - End
		
		else if (atmMessageType.equals("599")) {
			messageProcessor = new LocalLoro();
		}
		//Added new entry for 614 message

		else if ((atmMessageType.equals("620") || atmMessageType.equals("680")) && ((atmSparrowMessage.getForcePost().equals("0") || atmSparrowMessage.getForcePost().equals("1") || atmSparrowMessage
				.getForcePost().equals("3")))){
			messageProcessor = new ExtNwCashWithdrawal();
		}
		else if ((atmMessageType.equals("620") || atmMessageType.equals("680")) && ((atmSparrowMessage.getForcePost().equals("5") || atmSparrowMessage.getForcePost().equals("6")
				|| atmSparrowMessage.getForcePost().equals("7") || atmSparrowMessage.getForcePost().equals("8")))){
			messageProcessor = new POSCashProcessor();
		}
		else if ((atmMessageType.equals("610")
				|| atmMessageType.equals("611") || atmMessageType.equals("612") || atmMessageType.equals("613")||atmMessageType.equals("614"))
				&& (magStripTrans.equals("N")) && (atmSparrowMessage.getForcePost().equals("0") || atmSparrowMessage.getForcePost().equals("1") || atmSparrowMessage
						.getForcePost().equals("3"))) {
			messageProcessor = new ExtNwCashWithdrawal();
		}
		else if ((atmMessageType.equals("610")
				|| atmMessageType.equals("611") || atmMessageType.equals("612") || atmMessageType.equals("613")||atmMessageType.equals("614") )
				&& (magStripTrans.equals("N"))&& (atmSparrowMessage.getForcePost().equals("5") || atmSparrowMessage.getForcePost().equals("6")
						|| atmSparrowMessage.getForcePost().equals("7") || atmSparrowMessage.getForcePost().equals("8"))) {
			messageProcessor = new POSCashProcessor();
		}        
        // added for artf45801 [start]
		else if ((atmMessageType.equals("610")
				|| atmMessageType.equals("611") || atmMessageType.equals("612") || atmMessageType.equals("613")||atmMessageType.equals("614"))
				&& (magStripTrans.equals("Y")) && (atmSparrowMessage.getForcePost().equals("0") || atmSparrowMessage.getForcePost().equals("1") || atmSparrowMessage
						.getForcePost().equals("3"))) {
			messageProcessor = new UB_ATM_SPA_SmartCardProcessor();
		}
        // added for artf45801 [end]        
		//Added for MagStrip Transactions=Y (Smart Card)
		else if ((atmMessageType.equals("610")|| atmMessageType.equals("611") || atmMessageType.equals("612") || atmMessageType.equals("613")||atmMessageType.equals("614") )
				&& (magStripTrans.equals("Y"))&&(atmSparrowMessage.getForcePost().equals("5") || atmSparrowMessage.getForcePost().equals("6")
						|| atmSparrowMessage.getForcePost().equals("7") || atmSparrowMessage.getForcePost().equals("8"))) {
			messageProcessor = new UB_ATM_SPA_SmartCardProcessor();																																																																																																																																																																																																																																																																																																																																																																																																															
		}
		//Added new entry for 623 message		
		else if ((atmMessageType.equals("621") || atmMessageType.equals("622") || atmMessageType.equals("623")|| atmMessageType.equals("624")
				|| atmMessageType.equals("629") || atmMessageType.equals("625")|| atmMessageType.equals("626"))&&(magStripTrans.equals("N"))) {
			messageProcessor = new POSCashProcessor();
		}
		else if ((atmMessageType.equals("621") || atmMessageType.equals("622") || atmMessageType.equals("623")|| atmMessageType.equals("624")
				|| atmMessageType.equals("629") || atmMessageType.equals("625")|| atmMessageType.equals("626"))&&(magStripTrans.equals("Y"))) {
			messageProcessor = new UB_ATM_SPA_SmartCardProcessor();
		}
		else if ((atmMessageType.equals("625")&&(magStripTrans.equals("Y"))) || (atmMessageType.equals("626")&&(magStripTrans.equals("Y")))) {
			messageProcessor = new UB_ATM_SPA_SmartCardProcessor();
		}
		else if (atmMessageType.equals("627")||atmMessageType.equals("633")||atmMessageType.equals("628")){
			messageProcessor = new UB_ATM_SPA_SmartCardProcessor();
		}
		else if (messageType.startsWith("8")||messageType.startsWith("7") || messageType.startsWith("0")) {
			messageProcessor = new UB_ATM_SPA_ReversalAndCorrectionTxns();
		}
		else if (atmMessageType.equals("540")) {
			messageProcessor = new ATMFundsTransfer();
		}
		return messageProcessor;
	}
}
