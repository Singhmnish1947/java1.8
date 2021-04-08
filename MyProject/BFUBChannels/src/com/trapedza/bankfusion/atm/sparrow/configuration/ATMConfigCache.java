/*
 * Copyright (c) 2003 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 * **********************************************************************************
 * Modification History
 * **********************************************************************************
 *
 * $Log: ATMConfigCache.java,v $
 * Revision 1.7  2008/08/12 20:15:28  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.5.4.1  2008/07/03 17:55:25  vivekr
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
 * Revision 1.4  2008/06/12 10:51:00  arun
 *  RIO on Head
 *
 * Revision 1.5  2008/01/18 07:15:53  sushmax
 * Updated files
 *
 * Revision 1.3  2008/01/10 14:25:07  prashantk
 * Updations for Incorporating Module Config. Changes for ATM
 *
 * Revision 1.4  2007/11/12 10:14:57  sushmax
 * ATMConfiguration for Build 28 del
 *
 * Revision 1.3.2.1  2007/08/08 18:38:29  prashantk
 * Changes made to a few Attribute Types and Data Types to make it more Meaningful
 *
 * Revision 1.2  2007/06/19 04:20:20  sushmax
 * *** empty log message ***
 *
 * Revision 1.2  2007/05/16 08:32:50  sushmax
 * ATM Configuration
 *
 */

package com.trapedza.bankfusion.atm.sparrow.configuration;

import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The ATMConfigCache is a singleton class that manages the configuration of the ATM module that
 * have been entered in the ATMConfig.xml file for the currently running BankFusion instance.
 */

public final class ATMConfigCache {

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
    private transient final static Log logger = LogFactory.getLog(ATMConfigCache.class.getName());

    private static ATMConfigCache localInstance = null;

    /**
     * The default constructor.
     * 
     * @
     */
    private ATMConfigCache() {

    }

    /**
     * Gets the instance of the class if it is null.
     * 
     * @
     */
    public synchronized static ATMConfigCache getInstance() {
        if (localInstance == null) {
            localInstance = new ATMConfigCache();
        }
        return localInstance;
    }

    /**
     * Returns the AtmControlDetails object
     * 
     * @
     */
    public ATMControlDetails getInformation(BankFusionEnvironment env) {
        ATMControlDetails atmControlDetails = new ATMControlDetails(env);
        return atmControlDetails;
    }
}
