/*
 * ******************************************************************************
 * Copyright (c) 2018 Finastra Software Solutions Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Finastra Software Solutions Ltd.
 * Use is subject to license terms.
 * ******************************************************************************
 */
package com.misys.ub.swift.remittance.process;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.chain.impl.ChainBase;
import org.apache.commons.chain.impl.ContextBase;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import com.misys.bankfusion.common.BankFusionMessages;
import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.microflow.runtime.exception.MicroflowIsParkedException;
import com.misys.bankfusion.subsystem.task.runtime.exception.ReferralDialogException;
import com.misys.cbs.common.constants.CBSConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.swift.remittance.dto.RemittanceProcessDto;
import com.trapedza.bankfusion.exceptions.CollectedEventsDialogException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.tasks.ITask;

import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRq;
import bf.com.misys.cbs.msgs.v1r0.SwiftRemittanceRs;
import bf.com.misys.cbs.types.events.Event;
import bf.com.misys.cbs.types.header.EventParameters;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;

/**
 * @author Machamma.Devaiah
 *
 */
public class RemittanceProcessPipeline implements BeanFactoryAware {

    private static final Log LOGGER = LogFactory.getLog(RemittanceProcessPipeline.class);

    private BeanFactory beanFactory;
    private String userId;
    private String branchSortCode;
    private String userLocale;
    private BankFusionEnvironment env;
    private SwiftRemittanceRq swtRemitanceReq;
    private SwiftRemittanceRs swtRemitterResp;
    private RemittanceProcessDto remittanceDto;

    public SwiftRemittanceRs runChain(String chainName) {
        try {
            remittanceDto.setEnv(getEnv());
            setRemittanceDto(remittanceDto);
            // Loading required input into ContextBase for the chain to work
            ContextBase contextBase = new ContextBase();
            contextBase.put("userId", getUserId());
            contextBase.put("branchSortCode", getBranchSortCode());
            contextBase.put("userLocale", getUserLocale());
            contextBase.put("beanFactory", beanFactory);
            contextBase.put("swtRemitanceReq", getSwtRemitanceReq());
            contextBase.put("swtRemitterResp", getSwtRemitterResp());
            contextBase.put("remittanceDto", getRemittanceDto());
            contextBase.put("bfEnv", getEnv());
            createChain(chainName).execute(contextBase);
        }
        catch (ReferralDialogException rde) {
            LOGGER.error(ExceptionUtil.getExceptionAsString(rde));
            throw rde;
        }
        catch (CollectedEventsDialogException collectedEventexc) {
            swtRemitterResp = getSwtRemitterResp();
            RsHeader rsHeader = new RsHeader();
            MessageStatus status = new MessageStatus();
            SubCode subCode = new SubCode();
            subCode.setCode(String.valueOf(collectedEventexc.getEvents().iterator().next().getEventNumber()));
            ArrayList<Object> paramsList = new ArrayList<>();
            for (int j = 0; j < collectedEventexc.getEvents().iterator().next().getDetails().length; j++) {
                EventParameters parameter = new EventParameters();
                Object param = collectedEventexc.getEvents().iterator().next().getDetails()[j];
                if (null != param) {
                    parameter.setEventParameterValue(param.toString());
                    paramsList.add(param.toString());
                }
                subCode.addParameters(parameter);
            }
            subCode.setDescription(getErrorDescription(subCode.getCode(), paramsList.toArray(), remittanceDto.getEnv()));
            subCode.setFieldName(CommonConstants.EMPTY_STRING);
            subCode.setSeverity(CBSConstants.ERROR);
            status.addCodes(subCode);
            status.setOverallStatus(PaymentSwiftConstants.ERROR_STATUS);
            rsHeader.setStatus(status);
            swtRemitterResp.setRsHeader(rsHeader);
        }
        catch (MicroflowIsParkedException microflowIsParkedException) {
            swtRemitterResp = getSwtRemitterResp();
            RsHeader rsHeader = new RsHeader();
            MessageStatus status = new MessageStatus();
            SubCode subCode = new SubCode();
            EventParameters eventParameters = new EventParameters();
            List<ITask> taskList = (List<ITask>) microflowIsParkedException.getTasks();
            for (ITask task : taskList) {
                Event eventDetails = (Event) task.getPayLoad();
                subCode.setCode(eventDetails.getEventNumber().toString());
                subCode.setDescription(task.getComment());
                String[] args = eventDetails.getMessageArguments();
                for (String argument : args) {
                    eventParameters.setEventParameterValue(argument);
                    subCode.addParameters(eventParameters);
                }
            }
            status.addCodes(subCode);
            status.setOverallStatus(PaymentSwiftConstants.ERROR_STATUS);
            rsHeader.setStatus(status);
            swtRemitterResp.setRsHeader(rsHeader);
        }
        catch (Exception exc) {
            LOGGER.error(ExceptionUtil.getExceptionAsString(exc));
            RsHeader rsHeader = new RsHeader();
            MessageStatus status = new MessageStatus();
            SubCode subCode = new SubCode();
            subCode.setCode(SwiftEventCodes.E_REMITTANCE_PROCESS_FAILED);
            subCode.setDescription(getErrorDescription(subCode.getCode(), new Object[] {}, remittanceDto.getEnv()));
            subCode.setFieldName(CommonConstants.EMPTY_STRING);
            subCode.setSeverity(CBSConstants.ERROR);
            status.addCodes(subCode);
            status.setOverallStatus(PaymentSwiftConstants.ERROR_STATUS);
            rsHeader.setStatus(status);
            swtRemitterResp.setRsHeader(rsHeader);
        }
        if (LOGGER.isInfoEnabled())
            LOGGER.info("END RemittanceProcessPipeline");

        return swtRemitterResp;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    protected ChainBase createChain(String chainName) {
        return (ChainBase) this.beanFactory.getBean(chainName);
    }

    public RemittanceProcessDto getRemittanceDto() {
        return remittanceDto;
    }

    public void setRemittanceDto(RemittanceProcessDto remittanceDto) {
        this.remittanceDto = remittanceDto;
    }

    public SwiftRemittanceRs getSwtRemitterResp() {
        return swtRemitterResp;
    }

    public void setSwtRemitterResp(SwiftRemittanceRs swtRemitterResp) {
        this.swtRemitterResp = swtRemitterResp;
    }

    /**
     * @param userId
     *            the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param branchSortCode
     *            the branchSortCode to set
     */
    public void setBranchSortCode(String branchSortCode) {
        this.branchSortCode = branchSortCode;
    }

    /**
     * @return the branchSortCode
     */
    public String getBranchSortCode() {
        return branchSortCode;
    }

    /**
     * @param userLocale
     *            the userLocale to set
     */
    public void setUserLocale(String userLocale) {
        this.userLocale = userLocale;
    }

    /**
     * @return the userLocale
     */
    public String getUserLocale() {
        return userLocale;
    }

    /**
     * @param env
     *            the env to set
     */
    public void setEnv(BankFusionEnvironment env) {
        this.env = env;
    }

    /**
     * @return the env
     */
    public BankFusionEnvironment getEnv() {
        return env;
    }

    /**
     * @return
     */

    public SwiftRemittanceRq getSwtRemitanceReq() {
        return swtRemitanceReq;
    }

    public void setSwtRemitanceReq(SwiftRemittanceRq swtRemitanceReq) {
        this.swtRemitanceReq = swtRemitanceReq;
    }

    /**
     * @param eventCode
     * @param param1
     * @param env
     * @return
     */
    private String getErrorDescription(String eventCode, Object[] params, BankFusionEnvironment env) {
        String errorRsn = StringUtils.EMPTY;
        if (!StringUtils.isBlank(eventCode)) {
            errorRsn = BankFusionMessages.getInstance().getFormattedEventMessage(Integer.parseInt(eventCode), params,
                    env.getUserSession().getUserLocale());
        }
        return errorRsn;

    }
}
