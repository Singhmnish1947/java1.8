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
public abstract class AbstractUB_FEX_ListExchangeRateDetails implements
		IUB_FEX_ListExchangeRateDetails {
	/**
	 * @deprecated use no-argument constructor!
	 */
	public AbstractUB_FEX_ListExchangeRateDetails(BankFusionEnvironment env) {
	}

	public AbstractUB_FEX_ListExchangeRateDetails() {
	}

	private bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractExchangeRatesRq f_IN_extractExchangeRatesRq = new bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractExchangeRatesRq();
	{
		bf.com.misys.cbs.types.ExtractExchangeRatesInput var_019_extractExchangeRatesRq_extractExchangeRatesInput = new bf.com.misys.cbs.types.ExtractExchangeRatesInput();

		var_019_extractExchangeRatesRq_extractExchangeRatesInput
				.setUserExtension(Utils.getJAVA_OBJECTValue(""));
		var_019_extractExchangeRatesRq_extractExchangeRatesInput
				.setMarginContextType(CommonConstants.EMPTY_STRING);
		var_019_extractExchangeRatesRq_extractExchangeRatesInput
				.setMarginContextValue(Utils.getSTRINGValue(""));
		var_019_extractExchangeRatesRq_extractExchangeRatesInput
				.setExchangeRateType(Utils.getSTRINGValue(""));
		var_019_extractExchangeRatesRq_extractExchangeRatesInput
				.setToCurrency(Utils.getSTRINGValue(""));
		var_019_extractExchangeRatesRq_extractExchangeRatesInput
				.setExRateCat(Utils.getSTRINGValue(""));
		var_019_extractExchangeRatesRq_extractExchangeRatesInput
				.setFromCurrency(Utils.getSTRINGValue(""));
		var_019_extractExchangeRatesRq_extractExchangeRatesInput
				.setExtRateId(Utils.getSTRINGValue(""));
		var_019_extractExchangeRatesRq_extractExchangeRatesInput
				.setHostExtension(Utils.getJAVA_OBJECTValue(""));
		bf.com.misys.cbs.types.ExtractParams var_019_extractExchangeRatesRq_extractExchangeRatesInput_extractParams = new bf.com.misys.cbs.types.ExtractParams();

		var_019_extractExchangeRatesRq_extractExchangeRatesInput_extractParams
				.setExtractFileName(Utils.getSTRINGValue(""));
		var_019_extractExchangeRatesRq_extractExchangeRatesInput_extractParams
				.setExtractMode(CommonConstants.EMPTY_STRING);
		var_019_extractExchangeRatesRq_extractExchangeRatesInput
				.setExtractParams(var_019_extractExchangeRatesRq_extractExchangeRatesInput_extractParams);

		f_IN_extractExchangeRatesRq
				.setExtractExchangeRatesInput(var_019_extractExchangeRatesRq_extractExchangeRatesInput);

		bf.com.misys.bankfusion.attributes.PagedQuery var_019_extractExchangeRatesRq_pagedQuery = new bf.com.misys.bankfusion.attributes.PagedQuery();

		bf.com.misys.bankfusion.attributes.PagingRequest var_019_extractExchangeRatesRq_pagedQuery_PagingRequest = new bf.com.misys.bankfusion.attributes.PagingRequest();

		var_019_extractExchangeRatesRq_pagedQuery_PagingRequest
				.setTotalPages(Utils.getINTEGERValue("0"));
		var_019_extractExchangeRatesRq_pagedQuery_PagingRequest
				.setRequestedPage(Utils.getINTEGERValue("0"));
		var_019_extractExchangeRatesRq_pagedQuery_PagingRequest
				.setNumberOfRows(Utils.getINTEGERValue("0"));
		var_019_extractExchangeRatesRq_pagedQuery
				.setPagingRequest(var_019_extractExchangeRatesRq_pagedQuery_PagingRequest);

		var_019_extractExchangeRatesRq_pagedQuery
				.setQueryData(CommonConstants.EMPTY_STRING);
		f_IN_extractExchangeRatesRq
				.setPagedQuery(var_019_extractExchangeRatesRq_pagedQuery);

		bf.com.misys.cbs.types.header.RqHeader var_019_extractExchangeRatesRq_rqHeader = new bf.com.misys.cbs.types.header.RqHeader();

		var_019_extractExchangeRatesRq_rqHeader
				.setVersion(CommonConstants.EMPTY_STRING);
		var_019_extractExchangeRatesRq_rqHeader.setMessageType(Utils
				.getSTRINGValue(""));
		bf.com.misys.cbs.types.header.TransReference var_019_extractExchangeRatesRq_rqHeader_transReference = new bf.com.misys.cbs.types.header.TransReference();

		var_019_extractExchangeRatesRq_rqHeader_transReference
				.setTransRepairLoc(Utils.getSTRINGValue(""));
		var_019_extractExchangeRatesRq_rqHeader_transReference
				.setUIdTransReference(Utils.getSTRINGValue(""));
		var_019_extractExchangeRatesRq_rqHeader_transReference
				.setSubTransReference(Utils.getSTRINGValue(""));
		var_019_extractExchangeRatesRq_rqHeader
				.setTransReference(var_019_extractExchangeRatesRq_rqHeader_transReference);

		bf.com.misys.cbs.types.header.Overrides var_019_extractExchangeRatesRq_rqHeader_overrides = new bf.com.misys.cbs.types.header.Overrides();

		bf.com.misys.cbs.types.AuthCodes var_019_extractExchangeRatesRq_rqHeader_overrides_authCodes = new bf.com.misys.cbs.types.AuthCodes();

		var_019_extractExchangeRatesRq_rqHeader_overrides_authCodes
				.setEventCode(Utils.getSTRINGValue(""));
		var_019_extractExchangeRatesRq_rqHeader_overrides_authCodes
				.addSupervisorIds(0, Utils.getSTRINGValue(""));

		var_019_extractExchangeRatesRq_rqHeader_overrides.addAuthCodes(0,
				var_019_extractExchangeRatesRq_rqHeader_overrides_authCodes);

		bf.com.misys.cbs.types.EventCodes var_019_extractExchangeRatesRq_rqHeader_overrides_eventCodes = new bf.com.misys.cbs.types.EventCodes();
		var_019_extractExchangeRatesRq_rqHeader_overrides_eventCodes
				.addEventCode(0, Utils.getSTRINGValue(""));

		var_019_extractExchangeRatesRq_rqHeader_overrides.addEventCodes(0,
				var_019_extractExchangeRatesRq_rqHeader_overrides_eventCodes);

		var_019_extractExchangeRatesRq_rqHeader_overrides.setForcePost(Utils
				.getBOOLEANValue("false"));
		var_019_extractExchangeRatesRq_rqHeader_overrides
				.setLastSupervisorId(Utils.getSTRINGValue(""));
		var_019_extractExchangeRatesRq_rqHeader_overrides.setAuthRequired(Utils
				.getBOOLEANValue(""));
		var_019_extractExchangeRatesRq_rqHeader
				.setOverrides(var_019_extractExchangeRatesRq_rqHeader_overrides);

		bf.com.misys.cbs.types.header.Orig var_019_extractExchangeRatesRq_rqHeader_orig = new bf.com.misys.cbs.types.header.Orig();

		var_019_extractExchangeRatesRq_rqHeader_orig.setZoneId(Utils
				.getSTRINGValue(""));
		var_019_extractExchangeRatesRq_rqHeader_orig
				.setChannelId(CommonConstants.EMPTY_STRING);
		var_019_extractExchangeRatesRq_rqHeader_orig.setOfflineMode(Utils
				.getBOOLEANValue("false"));
		var_019_extractExchangeRatesRq_rqHeader_orig.setOrigId(Utils
				.getSTRINGValue(""));
		var_019_extractExchangeRatesRq_rqHeader_orig
				.setAppVer(CommonConstants.EMPTY_STRING);
		var_019_extractExchangeRatesRq_rqHeader_orig.setOrigCtxtId(Utils
				.getSTRINGValue(""));
		var_019_extractExchangeRatesRq_rqHeader_orig.setOrigLocale(Utils
				.getSTRINGValue(""));
		var_019_extractExchangeRatesRq_rqHeader_orig.setOrigBranchCode(Utils
				.getSTRINGValue(""));
		var_019_extractExchangeRatesRq_rqHeader_orig
				.setAppId(CommonConstants.EMPTY_STRING);
		var_019_extractExchangeRatesRq_rqHeader
				.setOrig(var_019_extractExchangeRatesRq_rqHeader_orig);

		f_IN_extractExchangeRatesRq
				.setRqHeader(var_019_extractExchangeRatesRq_rqHeader);
	}
	private ArrayList<String> udfBoNames = new ArrayList<String>();
	private HashMap udfStateData = new HashMap();

	private bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractExchangeRatesRs f_OUT_extractExchangeRatesRs = new bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractExchangeRatesRs();
	{
		bf.com.misys.cbs.types.header.RsHeader var_020_extractExchangeRatesRs_rsHeader = new bf.com.misys.cbs.types.header.RsHeader();

		bf.com.misys.cbs.types.header.MessageStatus var_020_extractExchangeRatesRs_rsHeader_status = new bf.com.misys.cbs.types.header.MessageStatus();

		var_020_extractExchangeRatesRs_rsHeader_status
				.setOverallStatus(CommonConstants.EMPTY_STRING);
		bf.com.misys.cbs.types.header.SubCode var_020_extractExchangeRatesRs_rsHeader_status_codes = new bf.com.misys.cbs.types.header.SubCode();

		var_020_extractExchangeRatesRs_rsHeader_status_codes.setSeverity(Utils
				.getSTRINGValue(""));
		bf.com.misys.cbs.types.header.EventParameters var_020_extractExchangeRatesRs_rsHeader_status_codes_parameters = new bf.com.misys.cbs.types.header.EventParameters();

		var_020_extractExchangeRatesRs_rsHeader_status_codes_parameters
				.setEventParameterValue(Utils.getSTRINGValue(""));
		var_020_extractExchangeRatesRs_rsHeader_status_codes
				.addParameters(0,
						var_020_extractExchangeRatesRs_rsHeader_status_codes_parameters);

		var_020_extractExchangeRatesRs_rsHeader_status_codes
				.setDescription(Utils.getSTRINGValue(""));
		var_020_extractExchangeRatesRs_rsHeader_status_codes.setCode(Utils
				.getSTRINGValue(""));
		var_020_extractExchangeRatesRs_rsHeader_status_codes.setFieldName(Utils
				.getSTRINGValue(" "));
		var_020_extractExchangeRatesRs_rsHeader_status.addCodes(0,
				var_020_extractExchangeRatesRs_rsHeader_status_codes);

		var_020_extractExchangeRatesRs_rsHeader_status.setSubStatus(Utils
				.getSTRINGValue(""));
		var_020_extractExchangeRatesRs_rsHeader
				.setStatus(var_020_extractExchangeRatesRs_rsHeader_status);

		var_020_extractExchangeRatesRs_rsHeader
				.setVersion(CommonConstants.EMPTY_STRING);
		var_020_extractExchangeRatesRs_rsHeader.setMessageType(Utils
				.getSTRINGValue(""));
		var_020_extractExchangeRatesRs_rsHeader.setOrigCtxtId(Utils
				.getSTRINGValue(""));
		f_OUT_extractExchangeRatesRs
				.setRsHeader(var_020_extractExchangeRatesRs_rsHeader);

		bf.com.misys.cbs.types.ExtractExchangeRatesOutput var_020_extractExchangeRatesRs_extractExchangeRatesOutput = new bf.com.misys.cbs.types.ExtractExchangeRatesOutput();

		var_020_extractExchangeRatesRs_extractExchangeRatesOutput
				.setExtractMode(CommonConstants.EMPTY_STRING);
		bf.com.misys.cbs.types.ExRtDetails var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls = new bf.com.misys.cbs.types.ExRtDetails();

		var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls
				.setUserExtension(Utils.getJAVA_OBJECTValue(""));
		var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls
				.setBankCode(Utils.getSTRINGValue(""));
		var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls
				.setMarginContextType(Utils.getSTRINGValue(""));
		var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls
				.setMarginContextValue(Utils.getSTRINGValue(""));
		var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls
				.setUseSpreadPercentage(Utils.getBOOLEANValue(""));
		var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls
				.setUseMargin(Utils.getBOOLEANValue(""));
		var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls
				.setCrudMode(Utils.getSTRINGValue(""));
		var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls
				.setMargin(Utils.getBIGDECIMALValue(""));
		bf.com.misys.cbs.types.RetailExRtDetails var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls_exRtDetail = new bf.com.misys.cbs.types.RetailExRtDetails();

		var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls_exRtDetail
				.setUserExtension(Utils.getJAVA_OBJECTValue(""));
		var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls_exRtDetail
				.setExchangeRateType(Utils.getSTRINGValue(""));
		var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls_exRtDetail
				.setToCurrency(Utils.getSTRINGValue(""));
		var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls_exRtDetail
				.setActiveFlag(Utils.getBOOLEANValue("false"));
		var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls_exRtDetail
				.setExchangeRate(Utils.getBIGDECIMALValue(""));
		var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls_exRtDetail
				.setExRateCat(Utils.getSTRINGValue(""));
		var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls_exRtDetail
				.setFromCurrency(Utils.getSTRINGValue(""));
		var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls_exRtDetail
				.setTolerancePercenatge(Utils.getBIGDECIMALValue(""));
		var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls_exRtDetail
				.setUnit(Utils.getINTEGERValue(""));
		var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls_exRtDetail
				.setMultiplyOrDivide(Utils.getSTRINGValue(""));
		var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls_exRtDetail
				.setHostExtension(Utils.getJAVA_OBJECTValue(""));
		var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls_exRtDetail
				.setDateTime(Utils.getTIMESTAMPValue(""));
		var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls
				.setExRtDetail(var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls_exRtDetail);

		var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls
				.setSpreadPercentage(Utils.getBIGDECIMALValue(""));
		var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls
				.setSelect(Utils.getBOOLEANValue("false"));
		var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls
				.setHostExtension(Utils.getJAVA_OBJECTValue(""));
		var_020_extractExchangeRatesRs_extractExchangeRatesOutput
				.addExchangeRateDtls(0,
						var_020_extractExchangeRatesRs_extractExchangeRatesOutput_exchangeRateDtls);

		f_OUT_extractExchangeRatesRs
				.setExtractExchangeRatesOutput(var_020_extractExchangeRatesRs_extractExchangeRatesOutput);

		bf.com.misys.bankfusion.attributes.PagedQuery var_020_extractExchangeRatesRs_pagedQuery = new bf.com.misys.bankfusion.attributes.PagedQuery();

		bf.com.misys.bankfusion.attributes.PagingRequest var_020_extractExchangeRatesRs_pagedQuery_PagingRequest = new bf.com.misys.bankfusion.attributes.PagingRequest();

		var_020_extractExchangeRatesRs_pagedQuery_PagingRequest
				.setTotalPages(Utils.getINTEGERValue("0"));
		var_020_extractExchangeRatesRs_pagedQuery_PagingRequest
				.setRequestedPage(Utils.getINTEGERValue("0"));
		var_020_extractExchangeRatesRs_pagedQuery_PagingRequest
				.setNumberOfRows(Utils.getINTEGERValue("0"));
		var_020_extractExchangeRatesRs_pagedQuery
				.setPagingRequest(var_020_extractExchangeRatesRs_pagedQuery_PagingRequest);

		var_020_extractExchangeRatesRs_pagedQuery
				.setQueryData(CommonConstants.EMPTY_STRING);
		f_OUT_extractExchangeRatesRs
				.setPagedQuery(var_020_extractExchangeRatesRs_pagedQuery);
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {
	}

	public bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractExchangeRatesRq getF_IN_extractExchangeRatesRq() {
		return f_IN_extractExchangeRatesRq;
	}

	public void setF_IN_extractExchangeRatesRq(
			bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractExchangeRatesRq param) {
		f_IN_extractExchangeRatesRq = param;
	}

	public Map getInDataMap() {
		Map dataInMap = new HashMap();
		dataInMap.put(IN_extractExchangeRatesRq, f_IN_extractExchangeRatesRq);
		return dataInMap;
	}

	public bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractExchangeRatesRs getF_OUT_extractExchangeRatesRs() {
		return f_OUT_extractExchangeRatesRs;
	}

	public void setF_OUT_extractExchangeRatesRs(
			bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractExchangeRatesRs param) {
		f_OUT_extractExchangeRatesRs = param;
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
		dataOutMap
				.put(OUT_extractExchangeRatesRs, f_OUT_extractExchangeRatesRs);
		dataOutMap.put(CommonConstants.ACTIVITYSTEP_UDF_BONAMES, udfBoNames);
		dataOutMap.put(CommonConstants.ACTIVITYSTEP_UDF_STATE_DATA,
				udfStateData);
		return dataOutMap;
	}
}