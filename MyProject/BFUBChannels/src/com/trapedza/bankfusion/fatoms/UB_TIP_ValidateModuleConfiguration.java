/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: UB_TIP_ValidateModuleConfiguration.java,v.1.0,May 18, 2009 11:35:34 AM Apoorva
 *
 */
package com.trapedza.bankfusion.fatoms;

import java.util.HashMap;
import java.util.Map;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_ValidateModuleConfiguration;
 
/**
 * @author Apoorva
 * @date May 18, 2009
 * @project Universal Banking
 * @Description:to validate if Cr and Dr Suspense pseudonyms are distinct or not 
 */
public class UB_TIP_ValidateModuleConfiguration extends AbstractUB_TIP_ValidateModuleConfiguration {

    public UB_TIP_ValidateModuleConfiguration(BankFusionEnvironment env) {
        super(env);
    }

    public void process(BankFusionEnvironment env) {

    	VectorTable inputs = getF_IN_DATA();
        final int loopMaxCount = inputs.size();
        Map ResultMap = new HashMap();
        
        for (int counter = 0; counter < loopMaxCount; counter++) 
        {
            Map rowTags = inputs.getRowTags(counter);
                String paramName = (String) rowTags.get("PARAMNAME");
                String paramValue = (String) rowTags.get("PARAMVALUE");
   
                if (paramName.equalsIgnoreCase(TIPlusModuleConfigurationConstants.CREDIT_SUSPENSE_PSEUDONYM)) {
                	ResultMap.put(TIPlusModuleConfigurationConstants.CREDIT_SUSPENSE_PSEUDONYM, paramValue);
                }
                if (paramName.equalsIgnoreCase(TIPlusModuleConfigurationConstants.DEBIT_SUSPENSE_PSEUDONYM)) {
                	ResultMap.put(TIPlusModuleConfigurationConstants.DEBIT_SUSPENSE_PSEUDONYM, paramValue);
                }
                if (paramName.equalsIgnoreCase(TIPlusModuleConfigurationConstants.CONTRA_SUSPENSE_ACCOUNT_PSEUDONYM)) {
                	ResultMap.put(TIPlusModuleConfigurationConstants.CONTRA_SUSPENSE_ACCOUNT_PSEUDONYM, paramValue);
                }
                if (paramName.equalsIgnoreCase(TIPlusModuleConfigurationConstants.ALTERNATE_GENERATE_REQUIRED)) {
                	ResultMap.put(TIPlusModuleConfigurationConstants.ALTERNATE_GENERATE_REQUIRED, paramValue.toLowerCase());
                }
                if (paramName.equalsIgnoreCase(TIPlusModuleConfigurationConstants.ALLOW_FORCE_POST)) {
                	ResultMap.put(TIPlusModuleConfigurationConstants.ALLOW_FORCE_POST, paramValue.toLowerCase());
                }
           
          }
	      if(ResultMap.get(TIPlusModuleConfigurationConstants.CREDIT_SUSPENSE_PSEUDONYM).equals(ResultMap.get(TIPlusModuleConfigurationConstants.DEBIT_SUSPENSE_PSEUDONYM)) || (ResultMap.get(TIPlusModuleConfigurationConstants.CREDIT_SUSPENSE_PSEUDONYM).equals(ResultMap.get(TIPlusModuleConfigurationConstants.CONTRA_SUSPENSE_ACCOUNT_PSEUDONYM))) || (ResultMap.get(TIPlusModuleConfigurationConstants.DEBIT_SUSPENSE_PSEUDONYM).equals(ResultMap.get(TIPlusModuleConfigurationConstants.CONTRA_SUSPENSE_ACCOUNT_PSEUDONYM))))
	      {
	    	 
              EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_SUSPENSE_PSEUDONYM, new Object[]{}, new HashMap(), env);
	       }
	      
//	      if(ResultMap.get(TIPlusModuleConfigurationConstants.ALLOW_FORCE_POST).equals(TIPlusModuleConfigurationConstants.PARAMVALUE_NO)&& ResultMap.get(TIPlusModuleConfigurationConstants.SUPERVISOR_AUTHORIZATION_REQUIRED).equals(TIPlusModuleConfigurationConstants.PARAMVALUE_YES))
//	  
//	      {
//	    	 
//              EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_FORCEPOST_SUPERVISOR_AUTHORIZATION, new Object[]{}, new HashMap(), env);
//	       }
//	      
//
    }
}

   
