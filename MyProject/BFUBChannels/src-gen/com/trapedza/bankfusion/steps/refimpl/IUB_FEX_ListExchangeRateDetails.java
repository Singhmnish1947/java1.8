package com.trapedza.bankfusion.steps.refimpl;

import java.util.ArrayList;
import com.trapedza.bankfusion.microflow.ActivityStep;
import java.util.Map;
import java.util.List;
import com.trapedza.bankfusion.core.BankFusionException;
import java.sql.Timestamp;
import java.util.HashMap;
import com.trapedza.bankfusion.utils.Utils;
import com.trapedza.bankfusion.core.DataType;
import com.trapedza.bankfusion.core.CommonConstants;
import java.util.Iterator;
import java.math.BigDecimal;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.core.ExtensionPointHelper;
import bf.com.misys.bankfusion.attributes.UserDefinedFields;

/**
 * 
 * DO NOT CHANGE MANUALLY - THIS IS AUTOMATICALLY GENERATED CODE.<br>
 * This will be overwritten by any subsequent code-generation.
 *
 */
public interface IUB_FEX_ListExchangeRateDetails extends
		com.trapedza.bankfusion.servercommon.steps.refimpl.Processable {
	public static final String IN_extractExchangeRatesRq = "extractExchangeRatesRq";
	public static final String OUT_extractExchangeRatesRs = "extractExchangeRatesRs";

	public void process(BankFusionEnvironment env) throws BankFusionException;

	public bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractExchangeRatesRq getF_IN_extractExchangeRatesRq();

	public void setF_IN_extractExchangeRatesRq(
			bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractExchangeRatesRq param);

	public Map getInDataMap();

	public bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractExchangeRatesRs getF_OUT_extractExchangeRatesRs();

	public void setF_OUT_extractExchangeRatesRs(
			bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractExchangeRatesRs param);

	public Map getOutDataMap();
}