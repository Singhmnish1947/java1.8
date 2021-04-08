/* **********************************************************
 * Copyright (c) 2008 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/

package com.misys.bankfusion.events.reactor.publisher;

/**
 * @author Gaurav Aggarwal
 * @date Oct 26, 2016
 * @project Universal Banking
 * @Description: This class is used to override the group Name method of BusinessEventPublisher.
 */

public class InterfaceEventsPublisher extends BusinessEventPublisher {

    String groupName = "Interfaces";
    Boolean isExecutionInProgress = false;
    long numberOfExecutions;

    public void setIsExecutionInProgress(Boolean isExecutionInProgress) {
        this.isExecutionInProgress = isExecutionInProgress;
    }

    public String getGroupName() {
        return groupName;
    }

    public Boolean isExecutionInProgress() {
        return isExecutionInProgress;
    }

    public Boolean isFirstExecution() {
        if (numberOfExecutions <= 0) {
            return true;
        }
        else {
            return false;
        }
    }

    public void increaseExecutionCount() {
        numberOfExecutions = numberOfExecutions + 1;
    }

}