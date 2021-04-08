package com.trapedza.bankfusion.steps.refimpl;

import java.util.ArrayList;
import com.trapedza.bankfusion.microflow.ActivityStep;
import java.util.Map;
import java.util.List;
import com.trapedza.bankfusion.core.BankFusionException;
import java.util.HashMap;
import com.trapedza.bankfusion.utils.Utils;
import com.trapedza.bankfusion.core.DataType;
import com.trapedza.bankfusion.core.CommonConstants;
import java.util.Iterator;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.core.ExtensionPointHelper;
import bf.com.misys.bankfusion.attributes.UserDefinedFields;

/**
 * 
 * DO NOT CHANGE MANUALLY - THIS IS AUTOMATICALLY GENERATED CODE.<br>
 * This will be overwritten by any subsequent code-generation.
 *
 */
public abstract class AbstractAlertMessageFormatter implements IAlertMessageFormatter {
    /**
     * @deprecated use no-argument constructor!
     */
    public AbstractAlertMessageFormatter(BankFusionEnvironment env) {
    }

    public AbstractAlertMessageFormatter() {
    }

    private bf.com.misys.cbs.msgs.v1r0.CreateAlertMessageRq f_IN_CreateAlertMessageRq = new bf.com.misys.cbs.msgs.v1r0.CreateAlertMessageRq();
    {
        bf.com.misys.cbs.types.AlertMsgRequest var_019_CreateAlertMessageRq_alertMsgRequest = new bf.com.misys.cbs.types.AlertMsgRequest();

        var_019_CreateAlertMessageRq_alertMsgRequest.setTransactionAmount(CommonConstants.EMPTY_STRING);
        var_019_CreateAlertMessageRq_alertMsgRequest.setUserExtension(Utils.getJAVA_OBJECTValue(""));
        var_019_CreateAlertMessageRq_alertMsgRequest.setHostExtension(Utils.getJAVA_OBJECTValue(""));
        bf.com.misys.cbs.types.AlertKeys var_019_CreateAlertMessageRq_alertMsgRequest_alertKeys = new bf.com.misys.cbs.types.AlertKeys();

        var_019_CreateAlertMessageRq_alertMsgRequest_alertKeys.setAlertId(CommonConstants.EMPTY_STRING);
        var_019_CreateAlertMessageRq_alertMsgRequest_alertKeys.setAlertEntityType(CommonConstants.EMPTY_STRING);
        var_019_CreateAlertMessageRq_alertMsgRequest_alertKeys.setUserExtension(Utils.getJAVA_OBJECTValue(""));
        var_019_CreateAlertMessageRq_alertMsgRequest_alertKeys.setHostExtension(Utils.getJAVA_OBJECTValue(""));
        var_019_CreateAlertMessageRq_alertMsgRequest_alertKeys.setAlertEntityId(CommonConstants.EMPTY_STRING);
        var_019_CreateAlertMessageRq_alertMsgRequest.setAlertKeys(var_019_CreateAlertMessageRq_alertMsgRequest_alertKeys);

        bf.com.misys.cbs.types.EventHeader var_019_CreateAlertMessageRq_alertMsgRequest_eventHeader = new bf.com.misys.cbs.types.EventHeader();

        var_019_CreateAlertMessageRq_alertMsgRequest_eventHeader.setUserExtension(Utils.getJAVA_OBJECTValue(""));
        var_019_CreateAlertMessageRq_alertMsgRequest_eventHeader.setApplicationId(Utils.getSTRINGValue(""));
        var_019_CreateAlertMessageRq_alertMsgRequest_eventHeader.setTimeOutPeriod(Utils.getINTEGERValue(""));
        bf.com.misys.cbs.types.Narrative var_019_CreateAlertMessageRq_alertMsgRequest_eventHeader_eventNarrative = new bf.com.misys.cbs.types.Narrative();

        var_019_CreateAlertMessageRq_alertMsgRequest_eventHeader_eventNarrative.setNarrativeLine3(Utils.getSTRINGValue(""));
        var_019_CreateAlertMessageRq_alertMsgRequest_eventHeader_eventNarrative.setReference(Utils.getSTRINGValue(""));
        var_019_CreateAlertMessageRq_alertMsgRequest_eventHeader_eventNarrative.setNarrativeLine2(Utils.getSTRINGValue(""));
        var_019_CreateAlertMessageRq_alertMsgRequest_eventHeader_eventNarrative.setNarrativeLine1(Utils.getSTRINGValue(""));
        var_019_CreateAlertMessageRq_alertMsgRequest_eventHeader_eventNarrative.setHostExtension(Utils.getJAVA_OBJECTValue(""));
        var_019_CreateAlertMessageRq_alertMsgRequest_eventHeader_eventNarrative.setUserExtension(Utils.getJAVA_OBJECTValue(""));
        var_019_CreateAlertMessageRq_alertMsgRequest_eventHeader_eventNarrative.setNarrativeLine4(Utils.getSTRINGValue(""));
        var_019_CreateAlertMessageRq_alertMsgRequest_eventHeader.addEventNarrative(0,
                var_019_CreateAlertMessageRq_alertMsgRequest_eventHeader_eventNarrative);

        var_019_CreateAlertMessageRq_alertMsgRequest_eventHeader.setEventSource(Utils.getSTRINGValue(""));
        var_019_CreateAlertMessageRq_alertMsgRequest_eventHeader.setEventNumber(Utils.getSTRINGValue(""));
        var_019_CreateAlertMessageRq_alertMsgRequest_eventHeader.setBusinessId(Utils.getSTRINGValue(""));
        var_019_CreateAlertMessageRq_alertMsgRequest_eventHeader.setApplicationUnit(Utils.getSTRINGValue(""));
        var_019_CreateAlertMessageRq_alertMsgRequest_eventHeader.setHostExtension(Utils.getJAVA_OBJECTValue(""));
        var_019_CreateAlertMessageRq_alertMsgRequest.setEventHeader(var_019_CreateAlertMessageRq_alertMsgRequest_eventHeader);

        f_IN_CreateAlertMessageRq.setAlertMsgRequest(var_019_CreateAlertMessageRq_alertMsgRequest);

        bf.com.misys.cbs.types.header.RqHeader var_019_CreateAlertMessageRq_rqHeader = new bf.com.misys.cbs.types.header.RqHeader();

        var_019_CreateAlertMessageRq_rqHeader.setVersion(CommonConstants.EMPTY_STRING);
        var_019_CreateAlertMessageRq_rqHeader.setEntity(Utils.getSTRINGValue(""));
        bf.com.misys.cbs.types.header.AuthenticationDetails var_019_CreateAlertMessageRq_rqHeader_authenticationDetails = new bf.com.misys.cbs.types.header.AuthenticationDetails();

        var_019_CreateAlertMessageRq_rqHeader_authenticationDetails.setPassword(Utils.getSTRINGValue(""));
        var_019_CreateAlertMessageRq_rqHeader_authenticationDetails.setToken(Utils.getSTRINGValue(""));
        var_019_CreateAlertMessageRq_rqHeader_authenticationDetails.setUserId(Utils.getSTRINGValue(""));
        var_019_CreateAlertMessageRq_rqHeader.setAuthenticationDetails(var_019_CreateAlertMessageRq_rqHeader_authenticationDetails);

        var_019_CreateAlertMessageRq_rqHeader.setMessageType(Utils.getSTRINGValue(""));
        bf.com.misys.cbs.types.header.TransReference var_019_CreateAlertMessageRq_rqHeader_transReference = new bf.com.misys.cbs.types.header.TransReference();

        var_019_CreateAlertMessageRq_rqHeader_transReference.setTransRepairLoc(Utils.getSTRINGValue(""));
        var_019_CreateAlertMessageRq_rqHeader_transReference.setUIdTransReference(Utils.getSTRINGValue(""));
        var_019_CreateAlertMessageRq_rqHeader_transReference.setSubTransReference(Utils.getSTRINGValue(""));
        var_019_CreateAlertMessageRq_rqHeader.setTransReference(var_019_CreateAlertMessageRq_rqHeader_transReference);

        bf.com.misys.cbs.types.header.Overrides var_019_CreateAlertMessageRq_rqHeader_overrides = new bf.com.misys.cbs.types.header.Overrides();

        bf.com.misys.cbs.types.AuthCodes var_019_CreateAlertMessageRq_rqHeader_overrides_authCodes = new bf.com.misys.cbs.types.AuthCodes();

        var_019_CreateAlertMessageRq_rqHeader_overrides_authCodes.setEventCode(Utils.getSTRINGValue(""));
        var_019_CreateAlertMessageRq_rqHeader_overrides_authCodes.addSupervisorIds(0, Utils.getSTRINGValue(""));

        var_019_CreateAlertMessageRq_rqHeader_overrides.addAuthCodes(0, var_019_CreateAlertMessageRq_rqHeader_overrides_authCodes);

        bf.com.misys.cbs.types.EventCodes var_019_CreateAlertMessageRq_rqHeader_overrides_eventCodes = new bf.com.misys.cbs.types.EventCodes();
        var_019_CreateAlertMessageRq_rqHeader_overrides_eventCodes.addEventCode(0, Utils.getSTRINGValue(""));

        var_019_CreateAlertMessageRq_rqHeader_overrides
                .addEventCodes(0, var_019_CreateAlertMessageRq_rqHeader_overrides_eventCodes);

        var_019_CreateAlertMessageRq_rqHeader_overrides.setForcePost(Utils.getBOOLEANValue("false"));
        var_019_CreateAlertMessageRq_rqHeader_overrides.setLastSupervisorId(Utils.getSTRINGValue(""));
        var_019_CreateAlertMessageRq_rqHeader_overrides.setAuthRequired(Utils.getBOOLEANValue(""));
        var_019_CreateAlertMessageRq_rqHeader.setOverrides(var_019_CreateAlertMessageRq_rqHeader_overrides);

        bf.com.misys.cbs.types.header.Orig var_019_CreateAlertMessageRq_rqHeader_orig = new bf.com.misys.cbs.types.header.Orig();

        var_019_CreateAlertMessageRq_rqHeader_orig.setZoneId(Utils.getSTRINGValue(""));
        var_019_CreateAlertMessageRq_rqHeader_orig.setChannelId(CommonConstants.EMPTY_STRING);
        var_019_CreateAlertMessageRq_rqHeader_orig.setBankid(CommonConstants.EMPTY_STRING);
        var_019_CreateAlertMessageRq_rqHeader_orig.setOfflineMode(Utils.getBOOLEANValue("false"));
        var_019_CreateAlertMessageRq_rqHeader_orig.setOrigId(Utils.getSTRINGValue(""));
        var_019_CreateAlertMessageRq_rqHeader_orig.setAppVer(CommonConstants.EMPTY_STRING);
        var_019_CreateAlertMessageRq_rqHeader_orig.setOrigCtxtId(Utils.getSTRINGValue(""));
        var_019_CreateAlertMessageRq_rqHeader_orig.setOrigLocale(Utils.getSTRINGValue(""));
        var_019_CreateAlertMessageRq_rqHeader_orig.setOrigBranchCode(Utils.getSTRINGValue(""));
        var_019_CreateAlertMessageRq_rqHeader_orig.setAppId(CommonConstants.EMPTY_STRING);
        var_019_CreateAlertMessageRq_rqHeader.setOrig(var_019_CreateAlertMessageRq_rqHeader_orig);

        f_IN_CreateAlertMessageRq.setRqHeader(var_019_CreateAlertMessageRq_rqHeader);
    }
    private ArrayList<String> udfBoNames = new ArrayList<String>();
    private HashMap udfStateData = new HashMap();

    private String f_OUT_Message = CommonConstants.EMPTY_STRING;

    public void process(BankFusionEnvironment env) throws BankFusionException {
    }

    public bf.com.misys.cbs.msgs.v1r0.CreateAlertMessageRq getF_IN_CreateAlertMessageRq() {
        return f_IN_CreateAlertMessageRq;
    }

    public void setF_IN_CreateAlertMessageRq(bf.com.misys.cbs.msgs.v1r0.CreateAlertMessageRq param) {
        f_IN_CreateAlertMessageRq = param;
    }

    public Map getInDataMap() {
        Map dataInMap = new HashMap();
        dataInMap.put(IN_CreateAlertMessageRq, f_IN_CreateAlertMessageRq);
        return dataInMap;
    }

    public String getF_OUT_Message() {
        return f_OUT_Message;
    }

    public void setF_OUT_Message(String param) {
        f_OUT_Message = param;
    }

    public void setUDFData(String boName, UserDefinedFields fields) {
        if (!udfBoNames.contains(boName.toUpperCase())) {
            udfBoNames.add(boName.toUpperCase());
        }
        String udfKey = boName.toUpperCase() + CommonConstants.CUSTOM_PROP;
        udfStateData.put(udfKey, fields);
    }

    public Map getOutDataMap() {
        Map dataOutMap = new HashMap();
        dataOutMap.put(OUT_Message, f_OUT_Message);
        dataOutMap.put(CommonConstants.ACTIVITYSTEP_UDF_BONAMES, udfBoNames);
        dataOutMap.put(CommonConstants.ACTIVITYSTEP_UDF_STATE_DATA, udfStateData);
        return dataOutMap;
    }
}