/* ***********************************************************************************
 * Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * $Id: FON_ValidateSelectedSupervisors.java,v 1.9 2008/08/12 20:14:04 vivekr Exp $
 * **********************************************************************************
 * 
 * Revision 1.14  2008/02/16 14:37:17  Vinayachandrakantha.B.K
 * JavaDoc Comments added : For all the attributes
 */

package com.trapedza.bankfusion.fatoms;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractFON_ValidateSelectedSupervisors;
import com.trapedza.bankfusion.core.EventsHelper;

/**
 * This Class contains methods for validating various conditions on the selected supervisor list & their levels
 * such as duplicate supervisor entries, no levels assigned for a supervisor & level selected but not a supervisor etc. 
 * @author Vinayachandrakantha.B.K
 *
 */
public class FON_ValidateSelectedSupervisors extends AbstractFON_ValidateSelectedSupervisors {

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
	private transient final static Log logger = LogFactory.getLog(FON_ValidateSelectedSupervisors.class.getName());

	/**
	 * The default value in the supervisor field
	 */
	String defaultSupervisorValue = "None";

	/**
	 * The default value in the supervisor level field
	 */
	int defaultSupervisorLevelValue = 0;

	/**
	 * Constructor
	 * @param env
	 */
	public FON_ValidateSelectedSupervisors(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	/**
	 * implements process(...) method in AbstractFON_ValidateSelectedSupervisors
	 */
	public void process(BankFusionEnvironment env) {
		//		Validate the Supervisors & Supervisor levels

		/**
		 * TRUE if no supervisor specified, else FALSE
		 */
		boolean noSupervisor = true;

		/**
		 * Supervisor level field values input
		 */
		int[] supervisorLevelList = { getF_IN_supervisorLevel1().intValue(), getF_IN_supervisorLevel2().intValue(),
				getF_IN_supervisorLevel3().intValue(), getF_IN_supervisorLevel4().intValue(),
				getF_IN_supervisorLevel5().intValue(), getF_IN_supervisorLevel6().intValue(),
				getF_IN_supervisorLevel7().intValue(), getF_IN_supervisorLevel8().intValue(),
				getF_IN_supervisorLevel9().intValue(), getF_IN_supervisorLevel10().intValue() };

		/**
		 * Supervisor field values input
		 */
		String[] supervisorList = { getF_IN_supervisor1(), getF_IN_supervisor2(), getF_IN_supervisor3(),
				getF_IN_supervisor4(), getF_IN_supervisor5(), getF_IN_supervisor6(), getF_IN_supervisor7(),
				getF_IN_supervisor8(), getF_IN_supervisor9(), getF_IN_supervisor10() };

		//		Iterate for all the Supervisor values & their levels
		for (int i = 0; i < supervisorList.length; i++) {
			//			Set noSupervisor flag to false if atleast 1 supervisor is specified
			if (!supervisorList[i].trim().equalsIgnoreCase(defaultSupervisorValue)) {
				noSupervisor = false;
			}

			//			Check for Supervisor Level not assigned
			if (!supervisorList[i].trim().equalsIgnoreCase(defaultSupervisorValue)
					&& supervisorLevelList[i] == defaultSupervisorLevelValue) {
				/*throw new BankFusionException(9007, new Object[] { supervisorList[i] }, logger, env);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_AUTHORIZATION_LEVEL_NOT_SELECTED_FOR_SUPERVISOR,new Object[] {supervisorList[i] } , new HashMap(), env);
			}

			//			Check for Supervisor not assigned
			else if (supervisorList[i].trim().equalsIgnoreCase(defaultSupervisorValue)
					&& supervisorLevelList[i] != defaultSupervisorLevelValue) {
				/*throw new BankFusionException(9018, new Object[] { new Integer(i + 1) }, logger, env);*/
				EventsHelper.handleEvent(ChannelsEventCodes.E_SET_SUPERVISOR_OR_ELSE_AUTHORIZATION_LEVEL_NONE,new Object[] {new Integer(i + 1)} , new HashMap(), env);
			}

			//			Check for duplicate entries
			for (int j = i + 1; j < supervisorList.length; j++) {
				if (supervisorList[i].equalsIgnoreCase(supervisorList[j])
						&& !supervisorList[i].trim().equalsIgnoreCase(defaultSupervisorValue)) {
					/*throw new BankFusionException(9006, new Object[] { supervisorList[i] }, logger, env);*/
					EventsHelper.handleEvent(ChannelsEventCodes.E_SUPERVISOR_HAS_MORE_THAN_ONE_AUTHORIZATION_LEVEL,new Object[] {supervisorList[i]} , new HashMap(), env);
				}
			}
		}

		//		throw exception if no supervisor is specified
		if (noSupervisor) {
			/*throw new BankFusionException(9011, null, logger, env);*/
		}

		//		NOT BEING USED SINCE Destination Sort Code FIELD IS NOT EDITABLE in UI. HOWEVER, CAN BE USED IF IT'S MADE EDITABLE IN FUTURE RELEASES.
		//		Validate the Destination Sort Code entered.
		/*String destSortCode = getF_IN_destinationSortCode().substring(0, 2)
		+ "-" + getF_IN_destinationSortCode().substring(2, 4)
		+ "-" + getF_IN_destinationSortCode().substring(4, 6);
		IBOBranch branchObj = null;

		try
		{
			branchObj = (IBOBranch)env.getFactory().findByPrimaryKey(IBOBranch.BONAME, destSortCode);
		}
		catch(PersistenceException ex)
		{
			throw new BankFusionException(9015, new Object[] {destSortCode}, logger, env);
		}

		if(branchObj==null)
		{
			throw new BankFusionException(9016, new Object[] {destSortCode}, logger, env);
		}*/

		/**
		 * Numeric code of the transaction type selected
		 */
		int numericCode = 0;
		try {
			IBOMisTransactionCodes transCodeObj = (IBOMisTransactionCodes) env.getFactory().findByPrimaryKey(
					IBOMisTransactionCodes.BONAME, getF_IN_transType());
			numericCode = transCodeObj.getF_NUMERICTRANSCODE();
		}
		catch (Exception ex) {
			/*throw new BankFusionException(9021, new Object[] { ex.getMessage() }, logger, env);*/
			EventsHelper.handleEvent(ChannelsEventCodes.E_EXCEPTION_OCCURED,new Object[] {ex.getMessage() } , new HashMap(), env);
		logger.error(ex);
		}
		//		Checks whether the length of the Numeric Code provided is <= 2 digits, since this will be used in the
		//		generation of EFT file where transaction code must be of 2 digits in length.
		if (numericCode > 99 || numericCode < -9) {
			/*throw new BankFusionException(9022, new String[] { getF_IN_transType(),
					CommonConstants.EMPTY_STRING + numericCode }, logger, env);*/
			EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_NUMERIC_CODE_FOR_THE_TRANSACTION_TYPE,new Object[] {} , new HashMap(), env);
			
		}

		//		Checks whether the Destination Sort Code field is empty.
		if (getF_IN_destinationSortCode() == null
				|| getF_IN_destinationSortCode().trim().equalsIgnoreCase(CommonConstants.EMPTY_STRING)) {
			/*throw new BankFusionException(9016, null, logger, env);*/
			EventsHelper.handleEvent(ChannelsEventCodes.E_DESTINATION_BRANCH_SORT_CODE_MUST_BE_ENTERED,new Object[] {} , new HashMap(), env);
		}
	}
}
