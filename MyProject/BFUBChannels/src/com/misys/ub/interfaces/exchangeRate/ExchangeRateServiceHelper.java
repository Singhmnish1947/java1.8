/**
 * * Copyright (c) 2016 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 */
package com.misys.ub.interfaces.exchangeRate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.RequestBody;

import com.misys.bankfusion.common.GUIDGen;
import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.events.IBusinessEventsService;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.bankfusion.subsystem.security.runtime.impl.CurrencyUtil;
import com.misys.fbe.common.util.CommonUtil;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOCurrency;
import com.trapedza.bankfusion.bo.refimpl.IBOExchangeRates;
import com.trapedza.bankfusion.bo.refimpl.IBOExchangeRatesHistory;
import com.trapedza.bankfusion.bo.refimpl.IBOForwardPoints;
import com.trapedza.bankfusion.bo.refimpl.IBOForwardPointsHistory;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.persistence.services.IPersistenceService;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.IServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;

import bf.com.misys.cbs.services.ListGenericCodeRq;
import bf.com.misys.cbs.services.ListGenericCodeRs;
import bf.com.misys.cbs.types.GcCodeDetail;
import bf.com.misys.cbs.types.InputListHostGCRq;
import bf.com.misys.cbs.types.events.Event;

@SuppressWarnings({ "deprecation", "rawtypes", "unchecked" })
public class ExchangeRateServiceHelper {
    private static final transient Log LOGGER = LogFactory.getLog(ExchangeRateServiceHelper.class.getName());

    private static final String LIMIT1 = "999999999999999999.99";

    private static final String MODULE_NAME = "FEX";

    private static final String TOLERANCE_PARAM_NAME = "EXCHANGE_RATE_MAINT_TOLERANCE";

    public ExchangeRateResponse uploadExchangeRates(@RequestBody ExchangeRateRequest exchangeRateRequest) {

        ExchangeRateResponse response = null;
        ArrayList<SubCode> codeList = new ArrayList<SubCode>();
        Set<String> exchangeRateTypeGCCodeValuesSet = getValidExchangeRateTypes();

        // BankFusionThreadLocal.participateInExtTx(true);
        IPersistenceObjectsFactory privateFactory = getPrivateFactory();
        try {
            List<String> currencies = getValidCurrencies();

            privateFactory.beginTransaction();
            for (ExchangeRateDetails details : exchangeRateRequest.getDetails()) {

                if (!isValideExchangeRateDetail(currencies, details, codeList, exchangeRateTypeGCCodeValuesSet))
                    break;

                if (details.getExchangeRateType().equals(ExchangeRateConstants.exchangeRateForward)) {
                    updateForwardPoints(details, privateFactory);
                }
                else {
                    updateExchangeRates(details, privateFactory);
                }
            }

            raiseEvent(40430028, null);

        }
        catch (Exception e) {

            SubCode code = new SubCode();
            code.setSeverity("E");
            Integer codeNum = ExchangeRateConstants.E_GENERIC_ERROR_UB15;
            code.setCode(codeNum);
            code.setDescription(e.toString());
            codeList.add(code);
            LOGGER.error(ExceptionUtil.getExceptionAsString(e));
        }

        /*
         * The error occurred is not thrown but send as part of the response. This is specifically
         * done to maintain the compatibility older version and applications[Kondor] already using
         * this API. If this compatibility is not required please raise the even as and when
         * encoutered.
         */
        if (codeList.isEmpty()) {
            privateFactory.commitTransaction();
            privateFactory.closePrivateSession();
            response = new ExchangeRateResponse();
            response.setResponseCode(ExchangeRateConstants.successCode);
            response.setResponseMessage(ExchangeRateConstants.successMessage);
            response.setOverallStatus("S");
            response.setCodeList(codeList);
        }
        else {
            privateFactory.rollbackTransaction();
            privateFactory.closePrivateSession();
            response = new ExchangeRateResponse();
            response.setResponseCode(ExchangeRateConstants.failureCode);
            response.setResponseMessage(ExchangeRateConstants.failureMessage);
            response.setOverallStatus("E");
            response.setCodeList(getCodeWithDesc(codeList));
        }

        return response;
    }

    public IPersistenceObjectsFactory getPrivateFactory() {
        IServiceManager serviceManager = ServiceManagerFactory.getInstance().getServiceManager();
        IPersistenceService persistenceService = (IPersistenceService) serviceManager
                .getServiceForName(ServiceManager.PERSISTENCE_SERVICE);
        return persistenceService.getPrivatePersistenceFactory(false);
    }

    private void updateExchangeRates(ExchangeRateDetails details, IPersistenceObjectsFactory privateFactory) {
        List<IBOExchangeRates> rates = null;
        ArrayList<String> param = new ArrayList<String>();
        Map<String, Object> eventParams = null;
        boolean error = false;
        String crudMode = null;
        String primaryKey = null;

        try {
            param.add(details.getFromCurrency());
            param.add(details.getToCurrency());
            param.add(details.getExchangeRateType());

            rates = privateFactory.findByQuery(IBOExchangeRates.BONAME, ExchangeRateConstants.whereClauseType, param, null);

            if (rates == null || rates.isEmpty()) {
                IBOExchangeRates rate = addExchangeRate(details, privateFactory);
                addExchangeRateHistory(rate, privateFactory);
                crudMode = "C";
                primaryKey = rate.getBoID();
            }
            else {
                updateExchangeRate(rates, details);
                addExchangeRateHistory(rates, privateFactory);
                crudMode = "U";
                primaryKey = rates.get(0).getBoID();
            }
        }
        catch (Exception e) {
            error = true;
            throw e;
        }

        if (!error) {
            eventParams = new HashMap<String, Object>();
            eventParams.put("FCurrency", details.getFromCurrency());
            eventParams.put("ToCurrency", details.getToCurrency());
            eventParams.put("ExchangeRateType", details.getExchangeRateType());
            eventParams.put("Rate", String.valueOf(details.getRate()));

            raiseEvent(40411025, eventParams);

            eventParams = new HashMap<String, Object>();
            eventParams.put("crudMode", crudMode);
            eventParams.put("primaryKey", primaryKey);

            raiseEvent(20020655, eventParams);
        }
    }

    private void updateExchangeRate(List<IBOExchangeRates> rates, ExchangeRateDetails details) {

        for (IBOExchangeRates rate : rates) {

            if (CommonUtil.checkIfNotNullOrEmpty(details.getMultiplyDivideFlag())) {
                rate.setF_MULTIPLYDIVIDE(details.getMultiplyDivideFlag());
            }

            if (CommonUtil.checkIfNotNullOrEmpty(details.getTolerance())) {
                rate.setF_TOLERANCE(new BigDecimal(details.getTolerance()));
            }

            if (CommonUtil.checkIfNotNullOrEmpty(details.getRate())) {
                rate.setF_RATE(new BigDecimal(details.getRate()));
            }

            rate.setF_UPDATEDATE(SystemInformationManager.getInstance().getBFBusinessDate());
        }
    }

    private void addExchangeRateHistory(List<IBOExchangeRates> rates, IPersistenceObjectsFactory privateFactory) {

        for (IBOExchangeRates rate : rates) {
            addExchangeRateHistory(rate, privateFactory);
        }
    }

    private void addExchangeRateHistory(IBOExchangeRates rate, IPersistenceObjectsFactory privateFactory) {

        IBOExchangeRatesHistory history = (IBOExchangeRatesHistory) privateFactory
                .getStatelessNewInstance(IBOExchangeRatesHistory.BONAME);
        history.setBoID(GUIDGen.getNewGUID());
        history.setF_FROMCURRENCYCODE(rate.getF_FROMCURRENCYCODE());
        history.setF_TOCURRENCYCODE(rate.getF_TOCURRENCYCODE());
        history.setF_EXCHANGERATETYPE(rate.getF_EXCHANGERATETYPE());
        history.setF_MULTIPLYDIVIDE(rate.getF_MULTIPLYDIVIDE());
        history.setF_TOLERANCE(rate.getF_TOLERANCE());
        history.setF_RATE(rate.getF_RATE());
        history.setF_LIMIT1(rate.getF_LIMIT1());
        history.setF_DATELASTMODIFIED(SystemInformationManager.getInstance().getBFBusinessDateTime());

        privateFactory.create(IBOExchangeRatesHistory.BONAME, history);
    }

    private IBOExchangeRates addExchangeRate(ExchangeRateDetails details, IPersistenceObjectsFactory privateFactory) {

        IBOExchangeRates rate = (IBOExchangeRates) privateFactory.getStatelessNewInstance(IBOExchangeRates.BONAME);

        rate.setBoID(GUIDGen.getNewGUID());
        rate.setF_FROMCURRENCYCODE(details.getFromCurrency());
        rate.setF_TOCURRENCYCODE(details.getToCurrency());
        rate.setF_EXCHANGERATETYPE(details.getExchangeRateType());
        rate.setF_MULTIPLYDIVIDE(CommonUtil.checkIfNotNullOrEmpty(details.getMultiplyDivideFlag()) ? details.getMultiplyDivideFlag()
                : ExchangeRateConstants.multiplyDivideFlagM);

        if (CommonUtil.checkIfNotNullOrEmpty(details.getTolerance())) {
            rate.setF_TOLERANCE(new BigDecimal(details.getTolerance()));
        }
        else {
            rate.setF_TOLERANCE(getTolerance(details, privateFactory, BankFusionThreadLocal.getBankFusionEnvironment()));
        }

        if (CommonUtil.checkIfNotNullOrEmpty(details.getRate())) {
            rate.setF_RATE(new BigDecimal(details.getRate()));
        }

        rate.setF_LIMIT1(new BigDecimal(LIMIT1));
        rate.setF_SPOTPERIODINDAYS(2);
        rate.setF_UPDATEDATE(SystemInformationManager.getInstance().getBFBusinessDate());

        privateFactory.create(IBOExchangeRates.BONAME, rate);
        return rate;
    }

    private void updateForwardPoints(ExchangeRateDetails exchangeRateDetails, IPersistenceObjectsFactory privateFactory) {

        List<IBOForwardPoints> points = null;
        ArrayList<String> param = new ArrayList<String>();

        try {
            param.add(exchangeRateDetails.getFromCurrency());
            param.add(exchangeRateDetails.getToCurrency());
            param.add(Integer.toString(exchangeRateDetails.getRange()));

            points = privateFactory.findByQuery(IBOForwardPoints.BONAME, ExchangeRateConstants.whereClause, param, null);

            if (points == null || points.size() == 0) {
                IBOForwardPoints point = addForwardPointsRecord(exchangeRateDetails, privateFactory);
                addForwardPointsHistory(point, privateFactory);
            }
            else {
                updateForwardPointsRecord(points, exchangeRateDetails);
                addForwardPointsHistory(points, privateFactory);
            }

        }
        catch (Exception e) {
            throw e;
        }
    }

    private void updateForwardPointsRecord(List<IBOForwardPoints> points, ExchangeRateDetails exchangeRateDetails) {

        for (IBOForwardPoints point : points) {

            point.setF_BASISPOINTS(new BigDecimal(exchangeRateDetails.getBasisPoints()));
            point.setF_DISCOUNTPREMIUM(exchangeRateDetails.getDiscountPremiumFlag());
            point.setF_FROMCURRENCYCODE(exchangeRateDetails.getFromCurrency());
            point.setF_TOCURRENCYCODE(exchangeRateDetails.getToCurrency());
        }
    }

    private void addForwardPointsHistory(List<IBOForwardPoints> points, IPersistenceObjectsFactory privateFactory) {

        for (IBOForwardPoints point : points) {

            addForwardPointsHistory(point, privateFactory);
        }
    }

    private void addForwardPointsHistory(IBOForwardPoints point, IPersistenceObjectsFactory privateFactory) {

        /*
         * IBOForwardPointsHistory history = (IBOForwardPointsHistory) BankFusionThreadLocal
         * .getPersistanceFactory() .getStatelessNewInstance(IBOForwardPointsHistory.BONAME);
         */
        IBOForwardPointsHistory history = (IBOForwardPointsHistory) privateFactory
                .getStatelessNewInstance(IBOForwardPointsHistory.BONAME);
        history.setBoID(GUIDGen.getNewGUID());
        history.setF_BASISPOINTS(point.getF_BASISPOINTS());
        history.setF_DISCOUNTPREMIUM(point.getF_DISCOUNTPREMIUM());
        history.setF_FROMCURRENCYCODE(point.getF_FROMCURRENCYCODE());
        history.setF_TOCURRENCYCODE(point.getF_TOCURRENCYCODE());
        history.setF_RANGE(point.getF_RANGE());
        history.setF_DATELASTMODIFIED(SystemInformationManager.getInstance().getBFBusinessDateTime());
        history.setVersionNum(point.getVersionNum());

        privateFactory.create(IBOForwardPointsHistory.BONAME, history);
    }

    private IBOForwardPoints addForwardPointsRecord(ExchangeRateDetails exchangeRateDetails,
            IPersistenceObjectsFactory privateFactory) {

        IBOForwardPoints point = (IBOForwardPoints) privateFactory.getStatelessNewInstance(IBOForwardPoints.BONAME);

        point.setBoID(GUIDGen.getNewGUID());
        point.setF_BASISPOINTS(new BigDecimal(exchangeRateDetails.getBasisPoints()));
        point.setF_DISCOUNTPREMIUM(exchangeRateDetails.getDiscountPremiumFlag());
        point.setF_FROMCURRENCYCODE(exchangeRateDetails.getFromCurrency());
        point.setF_TOCURRENCYCODE(exchangeRateDetails.getToCurrency());
        point.setF_RANGE(exchangeRateDetails.getRange());
        point.setVersionNum(0);

        privateFactory.create(IBOForwardPoints.BONAME, point);
        return point;
    }

    private void raiseEvent(int eventNumber, Map<String, Object> params) {

        Event event = new Event();
        event.setEventNumber(eventNumber);
        event.setMessageArguments(new String[0]);
        IBusinessEventsService businessEventsService = (IBusinessEventsService) ServiceManagerFactory.getInstance()
                .getServiceManager().getServiceForName("BusinessEventsService");
        if (params == null) {
            businessEventsService.handleEvent(event, new HashMap());
        }
        else {
            businessEventsService.handleEvent(event, params);
        }

    }

    private List<String> getValidCurrencies() {

        List<String> currencyCodes = new ArrayList<String>();
        IPersistenceObjectsFactory privateFactory = getPrivateFactory();

        try {
            privateFactory.beginTransaction();

            List<IBOCurrency> currencies = CurrencyUtil.getCurrencyListOfCurrentZone();

            for (IBOCurrency currency : currencies) {
                currencyCodes.add(currency.getBoID());
            }
            privateFactory.commitTransaction();
        }
        catch (Exception e) {
            privateFactory.rollbackTransaction();
            throw e;
        }
        finally {
            privateFactory.closePrivateSession();
        }

        return currencyCodes;
    }

    private boolean isValideExchangeRateDetail(List<String> currencies, ExchangeRateDetails details, ArrayList<SubCode> codeList,
            Set<String> exchangeRateTypeGCCodeValuesSet) {
        ArrayList<String> eventParams = new ArrayList<String>();

        // fromCurrency field validation
        if (CommonUtil.checkIfNullOrEmpty(details.getFromCurrency()) || !currencies.contains(details.getFromCurrency())) {
            eventParams.clear();
            eventParams.add(details.getFromCurrency());
            codeList.add(getSubCode(ExchangeRateConstants.E_CB_CMN_CURRENCY_DOES_NOT_EXIST, eventParams));
            return false;
        }
        // toCurrency field validation
        if (CommonUtil.checkIfNullOrEmpty(details.getToCurrency()) || !currencies.contains(details.getToCurrency())) {
            eventParams.clear();
            eventParams.add(details.getToCurrency());
            codeList.add(getSubCode(ExchangeRateConstants.E_CB_CMN_CURRENCY_DOES_NOT_EXIST, eventParams));
            return false;
        }

        // currency equality validation
        if (details.getFromCurrency().equals(details.getToCurrency())) {
            eventParams.clear();
            codeList.add(getSubCode(ExchangeRateConstants.FROM_TO_CURR_SAME, eventParams));
            return false;
        }

        // Checking for the valid exchange rate type
        if (CommonUtil.checkIfNullOrEmpty(details.getExchangeRateType())) {
            eventParams.clear();
            eventParams.add(details.getExchangeRateType());
            codeList.add(getSubCode(ExchangeRateConstants.E_EXCHGRATETYPE_DOES_NOT_EXIST, eventParams));
            return false;
        }

        if (!ExchangeRateConstants.exchangeRateForward.equals(details.getExchangeRateType())) {
            /*
             * Checking for the rate zero
             */
            if (CommonUtil.checkIfNullOrEmpty(details.getRate()) || !isNumeric(details.getRate())) {

                eventParams.clear();
                eventParams.add("rate");
                eventParams.add(details.getRate());
                codeList.add(getSubCode(ExchangeRateConstants.E_CB_INVALID_FIELDS_CB05, eventParams));
                return false;

            }
            else if (BigDecimal.ZERO.compareTo(new BigDecimal(details.getRate())) == 0) {
                eventParams.clear();
                codeList.add(getSubCode(ExchangeRateConstants.EXCHG_RATE_ZERO, eventParams));
                return false;
            }

            if (!exchangeRateTypeGCCodeValuesSet.contains(details.getExchangeRateType())) {
                eventParams.clear();
                eventParams.add(details.getExchangeRateType());
                codeList.add(getSubCode(ExchangeRateConstants.E_EXCHGRATETYPE_DOES_NOT_EXIST, eventParams));
                return false;
            }

            /*
             * Checking for multiplyDivide flag character
             */
            if (CommonUtil.checkIfNotNullOrEmpty(details.getMultiplyDivideFlag())
                    && !ExchangeRateConstants.multiplyDivideFlagM.equals(details.getMultiplyDivideFlag())
                    && !ExchangeRateConstants.multiplyDivideFlagD.equals(details.getMultiplyDivideFlag())) {

                eventParams.clear();
                eventParams.add("multiplyDivideFlag");
                eventParams.add(details.getMultiplyDivideFlag());
                codeList.add(getSubCode(ExchangeRateConstants.E_CB_INVALID_FIELDS_CB05, eventParams));
                return false;

            }

            if (CommonUtil.checkIfNotNullOrEmpty(details.getTolerance()) && !isNumeric(details.getTolerance())) {
                eventParams.clear();
                eventParams.add("tolerance");
                eventParams.add(details.getTolerance());
                codeList.add(getSubCode(ExchangeRateConstants.E_CB_INVALID_FIELDS_CB05, eventParams));
                return false;
            }

        }
        if (ExchangeRateConstants.exchangeRateForward.equals(details.getExchangeRateType())) {

            if (CommonUtil.checkIfNullOrEmpty(details.getBasisPoints())) {
                eventParams.clear();
                eventParams.add("basisPoint");
                eventParams.add("cannot be empty");
                codeList.add(getSubCode(ExchangeRateConstants.E_CB_INVALID_FIELDS_CB05, eventParams));
                return false;
            }
            else if (!isNumeric(details.getBasisPoints())) {
                eventParams.clear();
                eventParams.add("basisPoint");
                eventParams.add(details.getBasisPoints());
                codeList.add(getSubCode(ExchangeRateConstants.E_CB_INVALID_FIELDS_CB05, eventParams));
                return false;
            }

            /*
             * Checking for discountPremium flag character
             */
            if (!ExchangeRateConstants.discountPremiumFlagD.equals(details.getDiscountPremiumFlag())
                    && !ExchangeRateConstants.discountPremiumFlagP.equals(details.getDiscountPremiumFlag())) {
                eventParams.clear();
                eventParams.add("discountPremiumFlag");
                eventParams.add(details.getDiscountPremiumFlag());
                codeList.add(getSubCode(ExchangeRateConstants.E_CB_INVALID_FIELDS_CB05, eventParams));
                return false;
            }

            if (details.getRange() <= 0) {
                eventParams.clear();
                eventParams.add("range");
                eventParams.add(CommonConstants.EMPTY_STRING + details.getRange());
                codeList.add(getSubCode(ExchangeRateConstants.E_CB_INVALID_FIELDS_CB05, eventParams));
                return false;
            }
        }
        return true;
    }

    private Set<String> getValidExchangeRateTypes() {
        Set<String> exchangeRateTypeGCCodeValuesSet = new HashSet<String>();
        String CB_GCD_LISTGENERICCODES_SRV = "CB_GCD_ListGenericCodes_SRV";
        HashMap<String, Object> paramsargupdate = new HashMap<String, Object>();
        ListGenericCodeRq listGenericCodeRq = new ListGenericCodeRq();
        InputListHostGCRq inputListHostGCRq = new InputListHostGCRq();
        inputListHostGCRq.setCbReference("EXCHRATETYPE");
        listGenericCodeRq.setInputListCodeValueRq(inputListHostGCRq);
        paramsargupdate.put("listGenericCodeRq", listGenericCodeRq);
        HashMap output = MFExecuter.executeMF(CB_GCD_LISTGENERICCODES_SRV, paramsargupdate,
                BankFusionThreadLocal.getUserLocator().getStringRepresentation());

        ListGenericCodeRs listGenericCodeRs = (ListGenericCodeRs) output.get("listGenericCodeRs");
        for (GcCodeDetail paymentOptionCode : listGenericCodeRs.getGcCodeDetails()) {
            exchangeRateTypeGCCodeValuesSet.add(paymentOptionCode.getCodeReference());
        }
        return exchangeRateTypeGCCodeValuesSet;
    }

    private SubCode getSubCode(Integer eventNumber, ArrayList<String> paramList) {
        SubCode code = new SubCode();
        code.setSeverity("E");
        code.setCode(eventNumber);
        // code.setDescription(getEventMessage(eventNumber,paramList));
        code.setParametersList(paramList);
        return code;
    }

    public static String getEventMessage(Integer eventNumber, List<String> list) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("EVENTNUMBER", eventNumber);
        HashMap<String, Object> output = MFExecuter.executeMF("CB_CMN_GetEventMessage_SRV", params,
                BankFusionThreadLocal.getUserLocator().getStringRepresentation());
        String message = (String) output.get("FormattedMessage");
        if (!list.isEmpty()) {
            int i = 0;
            for (String pamam : list) {
                String oldString = "{" + i + "}";
                message = message.replace(oldString, pamam);
                i++;
            }
        }
        return message;
    }

    private List<SubCode> getCodeWithDesc(ArrayList<SubCode> codeList) {
        for (SubCode subCode : codeList) {
            if (!ExchangeRateConstants.E_GENERIC_ERROR_UB15.equals(subCode.getCode())) {
                subCode.setDescription(getEventMessage(subCode.getCode(), subCode.getParametersList()));

            }
        }
        return codeList;
    }

    private boolean isNumeric(String string) {
        return string.matches("^[+]?\\d+(\\.\\d+)?$");
    }

    private BigDecimal getTolerance(ExchangeRateDetails details, IPersistenceObjectsFactory privateFactory,
            BankFusionEnvironment env) {
        ArrayList<String> params = new ArrayList<>();
        List<IBOExchangeRates> rate = null;
        params.add(details.getFromCurrency());
        params.add(details.getToCurrency());
        params.add(details.getExchangeRateType());
        BigDecimal tolerance = BigDecimal.ZERO;

        rate = privateFactory.findByQuery(IBOExchangeRates.BONAME, ExchangeRateConstants.whereClauseType, params, null, false);

        if (CommonUtil.checkIfNotNullOrEmpty(rate)) {
            tolerance = rate.get(0).getF_TOLERANCE();
        }
        else {
            String toleranceStr = readToleranceModuleConfig(env);
            if (CommonUtil.checkIfNotNullOrEmpty(toleranceStr))
                tolerance = new BigDecimal(toleranceStr);
        }
        return tolerance;
    }

    private String readToleranceModuleConfig(BankFusionEnvironment env) {
        IBusinessInformation bizInfo = ((IBusinessInformationService) ServiceManagerFactory.getInstance().getServiceManager()
                .getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();
        Object value = bizInfo.getModuleConfigurationValue(MODULE_NAME, TOLERANCE_PARAM_NAME, env);
        if (value != null) {
            return value.toString();
        }
        else {
            return null;
        }
    }
}
