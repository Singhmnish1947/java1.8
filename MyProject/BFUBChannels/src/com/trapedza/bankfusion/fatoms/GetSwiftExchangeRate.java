package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.HashMap;

import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_CMN_GetExchangeRateSwift;

import bf.com.misys.cbs.services.ReadRetailExRtRq;
import bf.com.misys.cbs.services.ReadRetailExRtRs;
import bf.com.misys.cbs.types.RetailExRtShrtDetails;
import bf.com.misys.cbs.types.header.Orig;
import bf.com.misys.cbs.types.header.RqHeader;

public class GetSwiftExchangeRate extends AbstractUB_CMN_GetExchangeRateSwift {

    /**
     * 
     */
    private static final long serialVersionUID = -3958026972032302411L;

    String fromCurrencyCode;
    String toCurrencyCode;
    String TransactionCode;

    public GetSwiftExchangeRate(BankFusionEnvironment env) {
        super(env);
    }

    public void process(BankFusionEnvironment env) {
        fromCurrencyCode = getF_IN_FromCurrency();
        toCurrencyCode = getF_IN_ToCurrency();
        TransactionCode = getF_IN_TransactionCode();

        HashMap map = new HashMap();
        map.put("miscode", TransactionCode);
        HashMap Params = MFExecuter.executeMF("100_CheckMISTransCode", env, map);
        String exchangeRateType = Params.get("ExchangeRateType").toString();

        ReadRetailExRtRq exRtRq = new ReadRetailExRtRq();
        RetailExRtShrtDetails retailExRtShrtDetails = new RetailExRtShrtDetails();
        RqHeader rqHeader = new RqHeader();
        Orig orig = new Orig();
        orig.setChannelId("SWIFT");
        rqHeader.setOrig(orig);
        exRtRq.setRqHeader(rqHeader);
        retailExRtShrtDetails.setExchangeRateType(exchangeRateType);
        retailExRtShrtDetails.setFromCurrency(fromCurrencyCode);
        retailExRtShrtDetails.setToCurrency(toCurrencyCode);
        retailExRtShrtDetails.setExRateCat(exchangeRateType);
        exRtRq.setRetailExRtShrtDetails(retailExRtShrtDetails);
        HashMap ipMap = new HashMap();
        ipMap.put("ReadRetailExRtRq", exRtRq);
        HashMap opParams = MFExecuter.executeMF("CB_FEX_ReadRetailExchangeRate_SRV", env, ipMap);
        ReadRetailExRtRs readRetailExRtRs = (ReadRetailExRtRs) opParams.get("ReadRetailExRtRs");
        BigDecimal exchRate = readRetailExRtRs.getRetailExRtDetail().getExchangeRate();
        setF_OUT_ExchangeRate(exchRate);

    }
}
