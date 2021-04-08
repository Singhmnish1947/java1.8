/* ********************************************************************************
 *  Copyright (c) 2002,2004 Trapedza Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Trapedza Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 *
 * $Id: PostCloseDrawerCommand.java,v 1.6 2008/08/12 20:15:26 vivekr Exp $
 *
 * CVS/RCS Ident Block inserted at Wed Feb 11 12:31:35 GMT 2004
 * ------------------------------------------------------------
 *
 * $Log: PostCloseDrawerCommand.java,v $
 * Revision 1.6  2008/08/12 20:15:26  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.4.20.1  2008/07/03 17:55:30  vivekr
 * Moved from Dublin CVS Head (For BF1.01 package restructuring)
 *
 * Revision 1.4  2008/06/12 10:50:11  arun
 *  RIO on Head
 *
 * Revision 1.4  2006/12/12 05:42:36  arun
 * Removed reference to BankFusionMessaging (MessageTransformer - set(In/Out)comingMessageDescriptor)
 *
 * Revision 1.3  2006/05/16 18:04:55  ronnie
 * Implementation of new Persistence Support almost throughout the Server and Business Projects.
 *
 * Revision 1.2  2006/03/30 14:37:19  ronnie
 * Internationalisation support; Cache as Service; Performance enhancements.
 *
 * Revision 1.1  2006/01/18 22:14:33  ronnie
 * Credit Card mods plus further separation of business and framework.
 *
 * Revision 1.7  2005/10/14 17:11:42  ronnie
 * Separated incoming and outgoing data representation in messaging and applied efficiencies to
 * the use of CommandHelper and AbstractCommand data
 *
 * Revision 1.6  2005/10/13 16:27:23  ronnie
 * Tidied up the now redundant checking for the existance of IUserSession as AbstractCommand will
 * always check if it has got a valid one when it attempts to locate it.
 *
 * Revision 1.5  2005/10/13 13:07:38  ronnie
 * Next phase of messaging: Reduced payload to/from BFTC; Application of Pipes and Filters when
 * transforming messages; Use of the MessageDescriptor for outgoing asynch messages; added more comments.
 *
 * Revision 1.4  2005/07/13 12:29:04  ronnie
 * New features:
 * 1.	Applied use of CommonConstants throughout the entire codebase
 * 2.	Implemented use of commons-logging from Apache
 * 3.	First steps in optimistic locking support
 * 4.	Some small performance changes to persistence
 * 5.	Some bug fixes to audit logging
 *
 * Revision 1.3  2005/07/04 13:55:34  ronnie
 * Upgrade of BankFusion's Eclipse plug-ins to Ver 3.0 and implementation of
 * RAD 6.0
 *
 * Revision 1.2  2005/04/18 14:28:31  ronnie
 * Separate Resources into client and server; re-factor many classes to provide
 * better separation between framework code and business functionality.
 *
 * Revision 1.1  2005/01/07 15:52:31  ronnie
 * JMX Support, Service Management, Performance Improvements, Fault
 * Tolerance Improvements.
 *
 * Revision 1.4  2004/12/07 18:12:00  ronnie
 * Re-implemented UserSession as a Stateful Session Bean
 *
 * Revision 1.3  2004/09/24 13:37:32  nitin
 * Merged with Branch 2.6
 *
 * Revision 1.2.2.1  2004/07/17 21:59:14  ronnie
 * Initial phase on the re-structuring of the Posting Engine.
 * Account Limit Change supported
 *
 * Revision 1.2  2004/06/08 09:06:04  ronnie
 * Implemented the mapping of the Attributed ID to the DbColumnName,
 * as specified in a BO XML descriptor.
 *
 * Revision 1.1  2004/05/24 16:16:20  ronnie
 * Refactoring of all projects
 *
 * Revision 1.7  2004/02/17 09:54:59  angus
 * Automatic: add revision trace
 *
 * Revision 1.6  2004/02/11 12:31:36  angus
 * Automatic: add CVS/RCS Ident Block & revision constant
 *
 * ------------------------------------------------------------------------------------
 * File name	: PostCloseDrawerCommand.java
 * Author 	: vaishalih 
 * Date created	: 2003/10/15
 * Purpose	: 
 * -------------------------------------------------------------------------------------
 * Modified : 	Ronnie Nolan		Date :	8 Dec 2003			Project :	PR0131
 * Reason :		Implement Generic BO for all BO's
 * **********************************************************************************
 */

package com.trapedza.bankfusion.commands.teller;

import java.util.ArrayList;
import java.util.Iterator;

import com.trapedza.bankfusion.bo.refimpl.IBOCashDrawer;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.AbstractCommand;

/**
 * @author vaishalih
 *
 * Used only for Teller.
 * This command is fired by BFTC when CloseDrawer BP ends its execution. 
 * This is expected to update UserSessionManager's DrawerNumber from logged in DrawerNumber to 'SUSPENSE'.
 */
public class PostCloseDrawerCommand extends AbstractCommand {

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
	 * @see com.trapedza.bankfusion.commands.core.AbstractCommand#execute()
	 */
	public Object execute() {

		try {
			// #1271 fix : BFTC has to get the flag whether logoutFromDrawer is successful or not

			ArrayList params = new ArrayList();
			params.add(usm.getDrawerNumber());
			String whereClause = "where " + IBOCashDrawer.DRAWERNUMBER + " = ?";

			Iterator sourceIt = environment.getFactory().findByQuery(IBOCashDrawer.BONAME, whereClause, params, null)
					.iterator();

			if (sourceIt.hasNext()) {
				IBOCashDrawer sourceSuper = (IBOCashDrawer) sourceIt.next();
				if (sourceSuper == null)
					return Boolean.FALSE;

				if (sourceSuper.getF_INUSE().equalsIgnoreCase("N")) {
					usm.setDrawerNumber("SUSPENSE");
					return Boolean.TRUE;
				}
			}
			return Boolean.FALSE;
		} //try
		catch (BankFusionException bfe) {
			return Boolean.FALSE;
		}
	}

	/**
	 * @see com.trapedza.bankfusion.commands.core.AbstractCommand#isUpdateCommand()
	 */
	public boolean isUpdateCommand() {
		return false;
	}

	/**
	 * @see com.trapedza.bankfusion.commands.core.AbstractCommand#isCommandManagedTxnReqd()
	 */
	public boolean isCommandManagedTxnReqd() {
		return false;
	}

	/**
	 * Sets <code>null</code> as this command is internal and will always be transformed by
	 * the standard <code>CmdHlprMessageTransformer</code>.  It doesn't have a corresponding 
	 * <code>MessageDescriptor</code>
	 * 
	 * @see com.trapedza.bankfusion.commands.core.AbstractCommand#setIncomingMessageDescriptor()
	 */
	protected void setIncomingMessageDescriptor() {
		//TODO - Arun - Doesn't do anything
		//msgTransformer.setIncomingMessageDescriptor(null);
	}
}
