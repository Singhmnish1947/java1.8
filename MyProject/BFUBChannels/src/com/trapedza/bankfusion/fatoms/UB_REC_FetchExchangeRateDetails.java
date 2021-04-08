/**
 * 
 */
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import bf.com.misys.cbs.services.ListGenericCodeRq;
import bf.com.misys.cbs.services.ListGenericCodeRs;
import bf.com.misys.cbs.types.ExRtDetails;
import bf.com.misys.cbs.types.ExtractExchangeRatesInput;
import bf.com.misys.cbs.types.ExtractExchangeRatesOutput;
import bf.com.misys.cbs.types.GcCodeDetail;
import bf.com.misys.cbs.types.InputListHostGCRq;
import bf.com.misys.cbs.types.RetailExRtDetails;
import bf.com.misys.cbs.types.events.Event;
import bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractExchangeRatesRq;
import bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractExchangeRatesRs;

import com.misys.bankfusion.common.runtime.toolkit.expression.function.ConvertToTimestamp;
import com.misys.bankfusion.subsystem.security.runtime.util.UtilHelper;
import com.misys.fbe.common.util.CommonUtil;
import com.trapedza.bankfusion.bo.refimpl.IBOCurrency;
import com.trapedza.bankfusion.bo.refimpl.IBOExchangeRates;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_REC_FetchExchangeRateDetails;

/**
 * @author Shreyas.MR
 *
 */
public class UB_REC_FetchExchangeRateDetails extends AbstractUB_REC_FetchExchangeRateDetails {
	
	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}
	
	private List<SimplePersistentObject> exchangeRateDetails = null;
	
	//private static String baseCurrency = SystemInformationManager.getInstance().getBaseCurrencyCode();
	
	private static String getExchangeRatesSpotForBaseCurr = " SELECT   T1."
		+IBOExchangeRates.FROMCURRENCYCODE + " AS " +  CommonConstants.getTagName(IBOExchangeRates.FROMCURRENCYCODE) + ",T1."
		+IBOExchangeRates.TOCURRENCYCODE + " AS " +  CommonConstants.getTagName(IBOExchangeRates.TOCURRENCYCODE) + ",T1."
		+IBOExchangeRates.EXCHANGERATETYPE + " AS " +  CommonConstants.getTagName(IBOExchangeRates.EXCHANGERATETYPE) + ",T1."
		+IBOExchangeRates.MULTIPLYDIVIDE + " AS " +  CommonConstants.getTagName(IBOExchangeRates.MULTIPLYDIVIDE) + ",T1."
		+IBOExchangeRates.TOLERANCE + " AS " +  CommonConstants.getTagName(IBOExchangeRates.TOLERANCE) + ",T1."
		+IBOExchangeRates.RATE + " AS " +  CommonConstants.getTagName(IBOExchangeRates.RATE) + CommonConstants.FROM + " " + IBOExchangeRates.BONAME + " T1 "
        + "WHERE T1."+IBOExchangeRates.EXCHANGERATETYPE+" LIKE ? AND "+IBOExchangeRates.FROMCURRENCYCODE+" LIKE ? AND " +
        IBOExchangeRates.TOCURRENCYCODE + " LIKE ?";
	
	
	/**
	 * The constructor that indicates we're in a runtime environment and we
	 * should initialise the Fatom with only those attributes necessary.
	 * 
	 * @param env
	 *            The BankFusion Environment
	 */
	public UB_REC_FetchExchangeRateDetails(BankFusionEnvironment env) {
		super(env);
	}
	
	
	public void process(BankFusionEnvironment env) {
		
		java.sql.Timestamp businessDateTime = ConvertToTimestamp.run(SystemInformationManager.getInstance().getBFBusinessDate());
		
		ExtractExchangeRatesOutput o = new ExtractExchangeRatesOutput();
		ExtractExchangeRatesRq extractExchangeRatesRq = getF_IN_extractExchangeRatesRq();
		ExtractExchangeRatesInput extractExchangeRatesInput = extractExchangeRatesRq.getExtractExchangeRatesInput();
		initializeInputFields(extractExchangeRatesInput);
		validateInput(extractExchangeRatesInput);
		exchangeRateDetails = fetchExchangeRatesSpotForBaseCurr(extractExchangeRatesInput);
		ExRtDetails[] dtls = new ExRtDetails[exchangeRateDetails.size()];
		
		int j = 0;
		Iterator<SimplePersistentObject> i = exchangeRateDetails
		.iterator();
		while(i.hasNext()){
			
			ExRtDetails dtl = new ExRtDetails();
			dtl.setCrudMode("Read");
			dtl.setMarginContextType("BRANCH");
			dtl.setUseMargin(false);
			dtl.setBankCode(SystemInformationManager.getInstance().getBankName());
			RetailExRtDetails d = new RetailExRtDetails();
			
			SimplePersistentObject simplePersistentObject = (SimplePersistentObject) i.next();
			Map data = simplePersistentObject.getDataMap();
			
			d.setFromCurrency((String)data.get(CommonConstants.getTagName(IBOExchangeRates.FROMCURRENCYCODE)));
			d.setToCurrency((String)data.get(CommonConstants.getTagName(IBOExchangeRates.TOCURRENCYCODE)));
			d.setExchangeRateType((String)data.get(CommonConstants.getTagName(IBOExchangeRates.EXCHANGERATETYPE)));
			d.setMultiplyOrDivide((String)data.get(CommonConstants.getTagName(IBOExchangeRates.MULTIPLYDIVIDE)));
			d.setTolerancePercenatge((BigDecimal)data.get(CommonConstants.getTagName(IBOExchangeRates.TOLERANCE)));
			d.setExchangeRate((BigDecimal)data.get(CommonConstants.getTagName(IBOExchangeRates.RATE)));
			d.setDateTime(businessDateTime);
			
			dtl.setExRtDetail(d);
			dtls[j] = dtl;
			
			j++;
			
		}
		
		o.setExchangeRateDtls(dtls);
		ExtractExchangeRatesRs rs = new ExtractExchangeRatesRs();
		rs.setExtractExchangeRatesOutput(o);
		setF_OUT_extractExchangeRatesRs(rs);
	}
	
	private void initializeInputFields(ExtractExchangeRatesInput extractExchangeRatesInput) 
	{
		if(CommonUtil.checkIfNullOrEmpty(extractExchangeRatesInput.getExchangeRateType()))
		{
			extractExchangeRatesInput.setExchangeRateType("%");
		}
		if(CommonUtil.checkIfNullOrEmpty(extractExchangeRatesInput.getFromCurrency()))
		{
			extractExchangeRatesInput.setFromCurrency("%");
		}
		if(CommonUtil.checkIfNullOrEmpty(extractExchangeRatesInput.getToCurrency()))
		{
			extractExchangeRatesInput.setToCurrency("%");
		}
		
	}


	private void validateInput(ExtractExchangeRatesInput extractExchangeRatesInput)
	{
			HashSet curSet = getValidCurrencies();
			curSet.add("%");
			HashSet exchangeRateTypeSet = getValidExchangeRateTypes();
			exchangeRateTypeSet.add("%");
			if(!curSet.contains(extractExchangeRatesInput.getFromCurrency()))
			{
				String[] params = {extractExchangeRatesInput.getFromCurrency()};
				raiseEvent(20020012, params);
			}
			if(!curSet.contains(extractExchangeRatesInput.getToCurrency()))
			{
				String[] params = {extractExchangeRatesInput.getToCurrency()};
				raiseEvent(20020012, params);
			}
			if(!exchangeRateTypeSet.contains(extractExchangeRatesInput.getExchangeRateType()))
			{
				String[] params = {extractExchangeRatesInput.getExchangeRateType()};
				raiseEvent(40580179, params);
			}
	}


	@SuppressWarnings("unchecked")
	private List<SimplePersistentObject> fetchExchangeRatesSpotForBaseCurr(ExtractExchangeRatesInput extractExchangeRatesInput){
		ArrayList params = new ArrayList();
		params.add(extractExchangeRatesInput.getExchangeRateType());
		params.add(extractExchangeRatesInput.getFromCurrency());
		params.add(extractExchangeRatesInput.getToCurrency());
		return exchangeRateDetails = BankFusionThreadLocal.getPersistanceFactory().executeGenericQuery(getExchangeRatesSpotForBaseCurr,params,null,false);
	
	}
	private HashSet<String> getValidCurrencies() {

		List<IBOCurrency> currencies = null;
		HashSet<String> currencyCodes = new HashSet<String>();

		try {
			currencies = BankFusionThreadLocal.getPersistanceFactory().findAll(IBOCurrency.BONAME, null);
			for(IBOCurrency currency : currencies) {
				currencyCodes.add(currency.getBoID());
			}

		} catch (Exception e) {
			 e.printStackTrace();
		}

		return currencyCodes;
	}
	
	private HashSet<String> getValidExchangeRateTypes() {
		HashSet<String> exchangeRateTypeGCCodeValuesSet = new HashSet<String>();
		String CB_GCD_LISTGENERICCODES_SRV = "CB_GCD_ListGenericCodes_SRV";
        HashMap<String, Object> paramsargupdate = new HashMap<String, Object>();
        ListGenericCodeRq listGenericCodeRq = new ListGenericCodeRq();
        InputListHostGCRq inputListHostGCRq = new InputListHostGCRq();
        inputListHostGCRq.setCbReference("058");
        listGenericCodeRq.setInputListCodeValueRq(inputListHostGCRq);
        paramsargupdate.put("listGenericCodeRq", listGenericCodeRq);
       	HashMap output = MFExecuter.executeMF(CB_GCD_LISTGENERICCODES_SRV, paramsargupdate, BankFusionThreadLocal
                    .getUserLocator().getStringRepresentation());

        ListGenericCodeRs listGenericCodeRs = (ListGenericCodeRs) output.get("listGenericCodeRs");
        for(GcCodeDetail paymentOptionCode : listGenericCodeRs.getGcCodeDetails())
        {
        	exchangeRateTypeGCCodeValuesSet.add(paymentOptionCode.getCodeReference());
        }
		return exchangeRateTypeGCCodeValuesSet;
	}
	
	private void raiseEvent(int eventNumber, String[] params) {
		Event raiseEvent = new Event();
        raiseEvent.setEventNumber(eventNumber);
        raiseEvent.setMessageArguments(params);
        EventsHelper.handleEvent(raiseEvent, getBankFusionEnvironment());
		
	}
	
	private BankFusionEnvironment getBankFusionEnvironment() {
        if (null == BankFusionThreadLocal.getBankFusionEnvironment()) {
            BankFusionEnvironment env = new UtilHelper().createEnvironment(BankFusionThreadLocal.getUserLocator()
                    .getStringRepresentation(), null);
            BankFusionThreadLocal.setBankFusionEnvironment(env);
        }
        return BankFusionThreadLocal.getBankFusionEnvironment();
    }

}
