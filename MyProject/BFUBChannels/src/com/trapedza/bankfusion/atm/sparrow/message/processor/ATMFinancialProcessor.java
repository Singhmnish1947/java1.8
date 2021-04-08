/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 * $Log: ATMFinancialProcessor.java,v $
 * Revision 1.8  2008/08/12 20:15:05  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.6.4.1  2008/07/03 17:55:27  vivekr
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
 * Revision 1.6  2008/01/02 12:07:23  sushmax
 * *** empty log message ***
 *
 * Revision 1.5  2007/11/30 12:18:46  sushmax
 * *** empty log message ***
 *
 * Revision 1.4  2007/11/28 12:18:20  sushmax
 * removed println statements
 *
 * Revision 1.3  2007/11/28 09:40:26  sushmax
 * Code modified to raise exceptions in case of failure in Posting.
 *
 * Revision 1.2  2007/11/14 11:06:53  prashantk
 * ATM Financial Message Processors
 *
 * Revision 1.9  2007/10/29 06:53:57  prashantk
 * Updated
 *
 * Revision 1.1.2.1  2007/08/08 18:42:08  prashantk
 * Message processor for ATM Messages
 *
 * Revision 1.8  2007/07/05 07:58:30  sushmax
 * *** empty log message ***
 *
 * 
 */
package com.trapedza.bankfusion.atm.sparrow.message.processor;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.atm.sparrow.helper.ATMConstants;
import com.trapedza.bankfusion.atm.sparrow.message.ATMSparrowMessage;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.postingengine.router.PostingResponse;
import com.trapedza.bankfusion.postingengine.router.PostingRouter;
import com.trapedza.bankfusion.postingengine.services.PostingEngine;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;

/**
 * The ATMFinancialProcessor contains execute and postTransaction method implementations to post the
 * ATM financial messages.
 * 
 */
public abstract class ATMFinancialProcessor implements IATMMessageProcessor {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    /**
	 */

    /**
     * Holds the reference for logger object
     */
    private transient final static Log logger = LogFactory.getLog(ATMFinancialProcessor.class.getName());

    /**
     * The implementation of this method will be done in the derived subclasses
     */
    public abstract void execute(ATMSparrowMessage atmSparrowMessage, BankFusionEnvironment env);

    /**
     * This method invokes the Posting Engine service to post the financial transactions.
     * 
     * @param env
     *            @
     */
    protected final void postTransactions(ATMSparrowMessage message, ArrayList postingMessages, BankFusionEnvironment env) {
        try {
            PostingEngine pe = null;
            /*
             * ************************* Perform Postings ************************** Get the
             * PostingEngine Service first, and from that get a reference to the router. Post the
             * messages through the router, then indicate that the posting has completed.
             */

            pe = (PostingEngine) ServiceManager.getService(ServiceManager.POSTING_ENGINE_SERVICE);
            PostingRouter pr = (PostingRouter) pe.getNewInstance();
            ArrayList response = pr.post(postingMessages, env);
            pe.postingComplete();
            int size = response.size();
            for (int index = 0; index < size; index++) {
                PostingResponse postingResponse = (PostingResponse) response.get(index);
                if (postingResponse.getStatus() != 0) {
                    message.setAuthorisedFlag(ATMConstants.NOTAUTHORIZED_MESSAGE_FLAG);
                    message.setErrorCode(ATMConstants.WARNING);
                    message.setErrorDescription(postingResponse.getErrMessage());
                    throw new BankFusionException(40507007, new Object[] { postingResponse.getErrMessage() }, logger, env);

                }

            }
            env.getFactory().commitTransaction();
            env.getFactory().beginTransaction(); //
        }
        catch (BankFusionException exception) {
            env.getFactory().rollbackTransaction();
            env.getFactory().beginTransaction(); //
        }
        finally {
            env.getFactory().beginTransaction();
        }

    }
}
