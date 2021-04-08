package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractExchangeRatePushToDC;

import bf.com.misys.cbs.types.eventdetails.ExchRateDetails;
import bf.com.misys.cbs.types.eventdetails.ExchangeRateList;


public class ExchangeRateListPushToDC extends AbstractExchangeRatePushToDC {


    public ExchangeRateListPushToDC(BankFusionEnvironment env) {
        super(env);
    }

    public ExchangeRateListPushToDC() {
        super();
    }

    private transient final static Log logger = LogFactory.getLog(UB_IND_PaymentPostingFatom.class.getName());

    @Override
    public void process(BankFusionEnvironment env) throws BankFusionException {
        ExchangeRateList exchangeRateList = getF_IN_exchangeRateList();
        Map<String, BigDecimal> exchangeRatesMap = createExchangeRatesMap(exchangeRateList);
        String response = buildResponseXML(exchangeRatesMap);
        postToQueue(response, "RECIEVEQUEUE");

    }

    public String buildResponseXML(Map<String, BigDecimal> exchangeRatesMap) {
        String responseXMLToDC = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><BFO:PushExchangeRateRequest xmlns:BFO=\"http://pegasus/integration/exchangerate\">";
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date dateobj = new Date();

        for (String s : exchangeRatesMap.keySet())
        {
        String[] ccys = s.split("-");
            if (ccys[2].equals("BUY01") || ccys[2].equals("SEL01") || ccys[2].equals("SPOT"))
        {
            responseXMLToDC = responseXMLToDC
                    .concat("<ExchangeRateList>")
                    .concat("<fromCurrency>")
                    .concat(ccys[0])
                    .concat("</fromCurrency>")
                    .concat("<toCurrency>")
                    .concat(ccys[1])
                    .concat("</toCurrency>")
                    .concat("<unit>1</unit>")
                        .concat("<exchangeRateType>SPOT</exchangeRateType>");
                    if((ccys[2].toString()).equals("BUY01")){
                    logger.warn("String BUY CCYS[2]" + ccys[2] + "rate is " + exchangeRatesMap.get(s));
                    responseXMLToDC = responseXMLToDC.concat("<exchangeRateSubType>BUY</exchangeRateSubType>");
                    } else if((ccys[2].toString()).equals("SEL01")){
                    logger.warn("String SEL CCYS[2]" + ccys[2] + "rate is " + exchangeRatesMap.get(s));
                    responseXMLToDC = responseXMLToDC.concat("<exchangeRateSubType>SELL</exchangeRateSubType>");
                    } else if((ccys[2].toString()).equals("SPOT")){
                    logger.warn("String SPOT CCYS[2]" + ccys[2] + "rate is " + exchangeRatesMap.get(s));
                    responseXMLToDC = responseXMLToDC.concat("<exchangeRateSubType>MIDDLE</exchangeRateSubType>");
                    }
                    else
                    {
                    logger.warn("String SPOT CCYS[2]" + ccys[2] + "rate is " + exchangeRatesMap.get(s));
                    responseXMLToDC = responseXMLToDC.concat("<exchangeRateSubType>SELL</exchangeRateSubType>");
                    }

                    responseXMLToDC =responseXMLToDC.concat("<exchangeRates><exchangeRate>")
                        .concat(exchangeRatesMap.get(s).toString())
                            .concat("</exchangeRate>")
                            .concat("<dateTime>")
                        .concat(dateFormat.format(dateobj).toString())
                            .concat("</dateTime>")
                        .concat("</exchangeRates></ExchangeRateList>");
            
            }
        }

        responseXMLToDC = responseXMLToDC.concat("</BFO:PushExchangeRateRequest>");
        logger.info("DC response xml" + responseXMLToDC);
        return responseXMLToDC;
    }

    private void postToQueue(String message, String queueEndpoint) {
        MessageProducerUtil.sendMessage(message, queueEndpoint);
    }



    public Map<String, BigDecimal> createExchangeRatesMap(ExchangeRateList exchangeRateList) {
        Map<String, BigDecimal> exchangeRatesMap = new HashMap<>();
        List<ExchRateDetails> exchRateDetailsList = Arrays.asList(exchangeRateList.getEXCHRATEDETAILS());
        exchRateDetailsList.stream().forEach(p -> exchangeRatesMap
                .put(p.getFROMCURRENCYCODE() + "-" + p.getTOCURRENCYCODE() + "-" + p.getEXCHANGERATETYPE() + "-"
                        + p.getMULTIPLYDIVIDE(), p.getRATE()));
        return exchangeRatesMap;
    }

}
