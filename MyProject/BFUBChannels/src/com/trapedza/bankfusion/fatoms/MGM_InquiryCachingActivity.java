/* ********************************************************************************
 *  Copyright(c)2007  Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 * 
 * $Id: MGM_InquiryCachingActivity.java,v 1.3 2008/08/12 20:14:13 vivekr Exp $
 *  
 * $Log: MGM_InquiryCachingActivity.java,v $
 * Revision 1.3  2008/08/12 20:14:13  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.1.4.1  2008/07/03 17:55:54  vivekr
 * Moved from Dublin CVS Head (For BF1.01 package restructuring)
 *
 * Revision 1.6  2008/06/16 15:19:22  arun
 * UB Refactoring -
 * 1. Formatted/Organized imports
 * 2. BODefinitionException, InvalidExtensionPointException and references removed
 * 3. Removed ServerManager deprecated methods/variables
 * 4. BankfusionPropertyAccess removed - changed to BankfusionPropertySupport
 * 5. Exception Handling refactoring
 * 6. General Refactoring
 *
 * Revision 1.5  2008/06/12 10:51:53  arun
 *  RIO on Head
 *
 * Revision 1.1  2007/09/27 11:58:03  nileshk
 * Added for 3.3a MoneyGram release
 *
 * 
 */

package com.trapedza.bankfusion.fatoms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.moneygram.MGM_Constants;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.servercommon.services.cache.ICacheService;
import com.trapedza.bankfusion.steps.refimpl.AbstractMGM_InquiryCachingActivity;
import com.trapedza.bankfusion.steps.refimpl.IMGM_InquiryCachingActivity;

/**
 * This fatom is used to cache the CityList or AgentList inquiry string.
 * @author nileshk
 *
 */
public class MGM_InquiryCachingActivity extends AbstractMGM_InquiryCachingActivity implements
		IMGM_InquiryCachingActivity {

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
	private transient final static Log logger = LogFactory.getLog(MGM_InquiryCachingActivity.class.getName());

	/**
	 * Constructor
	 * @param env
	 */
	public MGM_InquiryCachingActivity(BankFusionEnvironment env) {
		super(env);
	}

	/**
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractMGM_InquiryCachingActivity#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 * @param environment The BankFusion Environment
	 * @
	 */
	public void process(BankFusionEnvironment environment) {
		int noOfLines = this.getF_IN_NoOfLines().intValue();
		int nextReccord = this.getF_IN_NextRecord().intValue();
		int fieldLength = this.getF_IN_fieldLength().intValue();
		String messageType = this.getF_IN_MessageType();
		char cacheChar = 0;
		int indexOne = 0;
		int indexTwo = 10 * fieldLength;
		String cacheString = CommonConstants.EMPTY_STRING;
		String resultString = CommonConstants.EMPTY_STRING;
		try {
			if (messageType.equalsIgnoreCase(MGM_Constants.MESSAGE_CITY_LIST))
				cacheChar = '$';
			if (messageType.equalsIgnoreCase(MGM_Constants.MESSAGE_AGENT_LIST))
				cacheChar = '#';
			ICacheService cache = (ICacheService) ServiceManager.getService(ServiceManager.CACHE_SERVICE);
			if (nextReccord == 0)
				cacheString = this.getF_IN_InquiryString();
			else
				cacheString = (String) cache.cacheGet(this.getF_IN_UserId() + this.getF_IN_BranchCode(), cacheChar);

			if (indexTwo > cacheString.length())
				indexTwo = indexOne + (cacheString.length() - indexOne);
			resultString = cacheString.substring(indexOne, indexTwo);
			cacheString = cacheString.substring(indexTwo);

			cache.cacheRemove(this.getF_IN_UserId() + this.getF_IN_BranchCode(), cacheChar);
			cache.cachePut(this.getF_IN_UserId() + this.getF_IN_BranchCode(), cacheChar, cacheString);
			if (resultString.length() <= 0)
				noOfLines = 00;
			this.setF_OUT_NoOfLines(new Integer(noOfLines));
			this.setF_OUT_resultString(resultString.trim());
			logger.info("Cache Service invoked successfully");
		}
		catch (Exception e1) {
			logger.error("Error invoking cache service", e1);
			this.setF_OUT_resultString(CommonConstants.EMPTY_STRING);
		}
	}

}
