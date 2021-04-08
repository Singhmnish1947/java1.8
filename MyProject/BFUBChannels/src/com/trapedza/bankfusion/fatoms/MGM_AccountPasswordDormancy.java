/**
 * 
 */
package com.trapedza.bankfusion.fatoms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.moneygram.MGM_ExceptionHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractMGM_AccountPasswordDormancy;
import com.trapedza.bankfusion.steps.refimpl.IMGM_AccountPasswordDormancy;

/**
 * This class validates the Account for Account Stopped, Dormancy and Account Password conditions.
 * @author nileshk
 *
 */
public class MGM_AccountPasswordDormancy extends AbstractMGM_AccountPasswordDormancy implements
		IMGM_AccountPasswordDormancy {

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
	private transient final static Log logger = LogFactory.getLog(MGM_AccountPasswordDormancy.class.getName());

	/**
	 * Constructor
	 * @param env
	 */
	public MGM_AccountPasswordDormancy(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractMGM_AccountPasswordDormancy#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 * @param environment The BankFusion Environment
	 * @
	 */
	public void process(BankFusionEnvironment environment) {
		MGM_ExceptionHelper exceptionHelper = new MGM_ExceptionHelper();
		String forcePostFlag = this.getF_IN_ForcePostFlag();
		String dormancyPostingAction = this.getF_IN_DormancyPostingAction();
		String accountID = this.getF_IN_Account();
		String messageType = this.getF_IN_MessageType();
		int errorCode = 0;
		boolean dormancyStatus = this.isF_IN_AccountDormancyStatus().booleanValue();
		boolean stoppedStatus = this.isF_IN_AccountStopped().booleanValue();
		int accountRightsFlag = this.getF_IN_AccountRightsIndicator().intValue();
		if (forcePostFlag.equals("0")) {
			if (stoppedStatus) {
				errorCode = 9352;//ACCOUNT_STOPPED
			}
			else if (errorCode == 0 && dormancyStatus) {
				if (dormancyPostingAction.equals("0"))
					;
				//Allow Without Checking for Dormancy
				if (dormancyPostingAction.equals("1"))
					exceptionHelper.throwMoneyGramException(9360, environment);//Reject Transaction
				if (dormancyPostingAction.equals("2"))
					exceptionHelper.throwMoneyGramException(9359, environment);//Refer to Supervisor and Re-activate account
				if (dormancyPostingAction.equals("3"))
					logger.info("Posting to Account no: " + accountID + " where Dormant posting action is "
							+ dormancyPostingAction);//Allow and Log Exception				
			}
			else if (errorCode == 0) {
				if (accountRightsFlag == -1)
					errorCode = 9351;//PASSWD_REQ_FOR_POSTING_ENQUIRY
				else if (accountRightsFlag == 0)
					errorCode = 0;//PASSWD_NOT_REQ
				else if (accountRightsFlag == 1)
					errorCode = 9351;//PASSWD_REQ_FOR_POSTING
				else if (accountRightsFlag == 2)
					errorCode = 9352;//ACCOUNT_STOPPED
				else if (accountRightsFlag == 3)
					errorCode = 9353;//ACCOUNT_STOPPED_PASSWD_REQ_FOR_POSTING_ENQUIRY
				else if (accountRightsFlag == 4) {
					if (messageType.equalsIgnoreCase("MG04"))
						errorCode = 0;//DEBITS_NOT_ALLOWED
					else
						errorCode = 9354;//DEBITS_NOT_ALLOWED
				}
				else if (accountRightsFlag == 5) {
					if (messageType.equalsIgnoreCase("MG04"))
						errorCode = 0;//DEBITS_NOT_ALLOWED_PASSWD_REQ_TO_OVERRIDE
					else
						errorCode = 9355;//DEBITS_NOT_ALLOWED_PASSWD_REQ_TO_OVERRIDE
				}
				else if (accountRightsFlag == 6) {
					if (messageType.equalsIgnoreCase("MG02"))
						errorCode = 0;//CREDITS_NOT_ALLOWED
					else
						errorCode = 9356;//CREDITS_NOT_ALLOWED
				}
				else if (accountRightsFlag == 7) {
					if (messageType.equalsIgnoreCase("MG02"))
						errorCode = 0;//CREDITS_NOT_ALLOWED_PASSWD_REQ_TO_OVERRIDE
					else
						errorCode = 9357;//CREDITS_NOT_ALLOWED_PASSWD_REQ_TO_OVERRIDE
				}
				else if (accountRightsFlag == 8)
					errorCode = 0;//PASSWD_REQ_FOR_ENQUIRY_MUST_POST
				else if (accountRightsFlag == 9)
					errorCode = 9351;//PASSWD_REQ_FOR_POSTING
				else {
					//to do
				}
			}
		}

		if (errorCode > 0)
			exceptionHelper.throwMoneyGramException(errorCode, environment);
	}

}
