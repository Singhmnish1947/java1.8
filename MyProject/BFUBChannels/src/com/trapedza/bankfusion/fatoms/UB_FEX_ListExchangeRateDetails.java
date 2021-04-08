/**
 * 
 */
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.misys.bankfusion.common.runtime.toolkit.expression.function.ConvertToTimestamp;
import com.trapedza.bankfusion.bo.refimpl.IBOExchangeRates;

import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_FEX_ListExchangeRateDetails;

import bf.com.misys.cbs.types.ExRtDetails;
import bf.com.misys.cbs.types.ExtractExchangeRatesOutput;
import bf.com.misys.cbs.types.RetailExRtDetails;
import bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractExchangeRatesRs;;

/**
 * @author Shreyas.MR
 *
 */
public class UB_FEX_ListExchangeRateDetails extends AbstractUB_FEX_ListExchangeRateDetails {
	
	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}
	
	private List<SimplePersistentObject> exchangeRateDetails;
	
		
	private static String getListExchangeRates = " SELECT   T1."
		+IBOExchangeRates.FROMCURRENCYCODE + " AS " +  CommonConstants.getTagName(IBOExchangeRates.FROMCURRENCYCODE) + ",T1."
		+IBOExchangeRates.TOCURRENCYCODE + " AS " +  CommonConstants.getTagName(IBOExchangeRates.TOCURRENCYCODE) + ",T1."
		+IBOExchangeRates.EXCHANGERATETYPE + " AS " +  CommonConstants.getTagName(IBOExchangeRates.EXCHANGERATETYPE) + ",T1."
		+IBOExchangeRates.MULTIPLYDIVIDE + " AS " +  CommonConstants.getTagName(IBOExchangeRates.MULTIPLYDIVIDE) + ",T1."
		+IBOExchangeRates.TOLERANCE + " AS " +  CommonConstants.getTagName(IBOExchangeRates.TOLERANCE) + ",T1."
		+IBOExchangeRates.RATE + " AS " +  CommonConstants.getTagName(IBOExchangeRates.RATE) + CommonConstants.FROM + " " + IBOExchangeRates.BONAME + " T1 ";
      //  + "WHERE T1."+IBOExchangeRates.EXCHANGERATETYPE+"= ? "+" and "+IBOExchangeRates.FROMCURRENCYCODE+"= ? ";


	
	/**
	 * The constructor that indicates we're in a runtime environment and we
	 * should initialise the Fatom with only those attributes necessary.
	 * 
	 * @param env
	 *            The BankFusion Environment
	 */
	public UB_FEX_ListExchangeRateDetails(BankFusionEnvironment env) {
		super(env);
	}
	
	
	public void process(BankFusionEnvironment env) {
		
		java.sql.Timestamp businessDateTime = ConvertToTimestamp.run(SystemInformationManager.getInstance().getBFBusinessDate());
		exchangeRateDetails = fetchExchangeRatesSpotForBaseCurr();
		ExtractExchangeRatesOutput o = new ExtractExchangeRatesOutput();
		ExRtDetails[] dtls = new ExRtDetails[exchangeRateDetails.size()];
		
		//RetailExRtDetails rt = null;
		int j = 0;
		Iterator<SimplePersistentObject> i = exchangeRateDetails
		.iterator();
		while(i.hasNext()){
			
			ExRtDetails dtl = new ExRtDetails();
			dtl.setCrudMode("List");
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
	
	private List<SimplePersistentObject> fetchExchangeRatesSpotForBaseCurr(){
		ArrayList params = new ArrayList();
		return exchangeRateDetails = BankFusionThreadLocal.getPersistanceFactory().executeGenericQuery(getListExchangeRates,params,null,false);
	
	}

}
