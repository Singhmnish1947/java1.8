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
public interface IAlertMessageFormatter extends com.trapedza.bankfusion.servercommon.steps.refimpl.Processable {
    public static final String IN_CreateAlertMessageRq = "CreateAlertMessageRq";
    public static final String OUT_Message = "Message";

    public void process(BankFusionEnvironment env) throws BankFusionException;

    public bf.com.misys.cbs.msgs.v1r0.CreateAlertMessageRq getF_IN_CreateAlertMessageRq();

    public void setF_IN_CreateAlertMessageRq(bf.com.misys.cbs.msgs.v1r0.CreateAlertMessageRq param);

    public Map getInDataMap();

    public String getF_OUT_Message();

    public void setF_OUT_Message(String param);

    public Map getOutDataMap();
}