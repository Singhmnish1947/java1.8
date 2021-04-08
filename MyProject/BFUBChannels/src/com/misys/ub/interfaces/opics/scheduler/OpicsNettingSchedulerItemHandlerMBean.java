/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: OPICSNostroAndPositionNettingSchedulerItemHandlerMBean.java,v.1.0,May 15, 2009 12:39:09 PM avashish
 *
 */
package com.misys.ub.interfaces.opics.scheduler;
/**
 * @author Zubin Kavarana/avashish
 * @date May 15, 2009
 * @project Universal Banking
 * @Description: This is the MBean which is used by the JMX service to reset the
 * Nostro and Transaction Update hand-off frequency and the flags to enable and
 * disable the Handoff netting.
 */

public interface OpicsNettingSchedulerItemHandlerMBean {
    
    /**
     * Method Description: This method will return the Nostro Update Netting Frequency in mins 
     * @return frequency in minutes
     */
    public int getNostroUpdateFrequency();
    
    /**
     * Method Description: This method sets the Nostro Update Netting Frequency
     * @param frequency - Netting frequency in minutes
     */
    public void setNostroUpdateFrequency(int frequency);
    
    /**
     * Method Description: This method will return the Position Update Netting Frequency in mins 
     * @return frequency in minutes
     */
    public int getPositionsUpdateFrequency();
    
    /**
     * 
     * Method Description: This method sets the Position Update Netting Frequency
     * @param frequency - Netting frequency in minutes
     */
    public void setPositionsUpdateFrequency(int frequency);
    
    /**
     * Method Description: This method return a String "Y" or "N" to represent if the Nostro Update
     * Netting is enabled or disabled.
     * @return - "Y" for enabled and "N" for disabled
     */
    public String getIsNostroUpdateEnabled();
    
    /**
     * Method Description: This method accepts a String "Y" or "N" to represent if the Nostro Update
     * Netting is enabled or disabled.
     * @param flag - "Y" for enabled and "N" for disabled
     */
    public void setIsNostroUpdateEnabled(String flag);

    /**
     * Method Description: This method return a String "Y" or "N" to represent if the Position Update
     * Netting is enabled or disabled.
     * @return - "Y" for enabled and "N" for disabled
     */
    public String getIsPositionUpdateEnabled();
    
    
    /**
     * Method Description: This method accepts a String "Y" or "N" to represent if the Position Update
     * Netting is enabled or disabled.
     * @param flag - "Y" for enabled and "N" for disabled
     */
    public void setIsPositionUpdateEnabled(String flag);
}

