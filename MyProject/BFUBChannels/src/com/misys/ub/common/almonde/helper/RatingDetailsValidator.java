/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: RatingDetailsValidator.java,v.1.0,Apr 21, 2009 5:13:35 PM ayerla
 *
 */
package com.misys.ub.common.almonde.helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.misys.bankfusion.common.runtime.service.ServiceManagerFactory;
import com.misys.ub.common.almonde.CurrencyRating;
import com.misys.ub.common.almonde.OverallRating;
import com.misys.ub.common.almonde.RatingDetails;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOCountry;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomerCollateral;
import com.trapedza.bankfusion.bo.refimpl.IBOForexDeals;
import com.trapedza.bankfusion.bo.refimpl.IBOUBTB_RATINGAGENCYCODES;
import com.trapedza.bankfusion.bo.refimpl.IBOUBTB_RATINGDETAILS;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.utils.BankFusionMessages;

/**
 * @author ayerla
 * @date Apr 21, 2009
 * @project Universal Banking
 * @Description: RatingDetailsValidator validates rating details and logs the errors.
 */

public class RatingDetailsValidator {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    public static final String ENTITY_TYPE_CUSTOMER = "001";
    public static final String ENTITY_TYPE_COUNTRY = "002";
    public static final String ENTITY_TYPE_FXDEAL = "003";
    public static final String ENTITY_TYPE_ASSET = "004";
    public static final String ENTITY_TYPE_MITIGANTS = "005";

    private static final String RATING_AGENCY_CODE_WHERECLAUSE = CommonConstants.WHERE + CommonConstants.SPACE
            + IBOUBTB_RATINGAGENCYCODES.UBAGENCYCODE + CommonConstants.EQUAL + CommonConstants.QUESTION;

    private static final String RATING_TYPE_WHERECLAUSE = CommonConstants.WHERE + CommonConstants.SPACE
            + IBOUBTB_RATINGDETAILS.UBRATINGCODE + CommonConstants.EQUAL + CommonConstants.QUESTION + " AND "
            + CommonConstants.SPACE + IBOUBTB_RATINGDETAILS.UBRATINGTERM + CommonConstants.EQUAL + CommonConstants.QUESTION;

    private static final String RATING_VALUE_WHERECLAUSE = CommonConstants.WHERE + CommonConstants.SPACE
            + IBOUBTB_RATINGDETAILS.UBRATINGCODE + CommonConstants.EQUAL + CommonConstants.QUESTION + " AND "
            + CommonConstants.SPACE + IBOUBTB_RATINGDETAILS.UBRATINGTERM + CommonConstants.EQUAL + CommonConstants.QUESTION
            + " AND " + CommonConstants.SPACE + IBOUBTB_RATINGDETAILS.UBRATINGVALUE + CommonConstants.EQUAL
            + CommonConstants.QUESTION;

    private static final String COLLATERAL_WHERECLAUSE = CommonConstants.WHERE + CommonConstants.SPACE
            + IBOCustomerCollateral.COLLATERALDTLID + CommonConstants.EQUAL + CommonConstants.QUESTION;

    private static final String FXDEAL_WHERECLAUSE = CommonConstants.WHERE + CommonConstants.SPACE + IBOForexDeals.DEALREFERENCE
            + CommonConstants.EQUAL + CommonConstants.QUESTION;

    private List<String> errorRatingCodes;
    private List<String> errorRatingTerms;
    private List<String> errorRatingValues;
    private List<String> errorEntityCodes;
    private List<String> errorCurriencies;
    private Map currencies;
    private List<String> countriesList;
    private IPersistenceObjectsFactory factory;

    /**
     *
     */
    private RatingDetailsValidator() {
        factory = BankFusionThreadLocal.getPersistanceFactory();
        errorRatingCodes = new ArrayList<String>();
        errorRatingTerms = new ArrayList<String>();
        errorRatingValues = new ArrayList<String>();
        errorEntityCodes = new ArrayList<String>();
        errorCurriencies = new ArrayList<String>();
        countriesList = new ArrayList<String>();
        List<IBOCountry> temp = factory.findAll(IBOCountry.BONAME, null, false);
        for (Iterator<IBOCountry> iterator = temp.iterator(); iterator.hasNext();) {
            IBOCountry countryBO = iterator.next();
            countriesList.add(countryBO.getBoID());
        }
    }

    public RatingDetailsValidator(BankFusionEnvironment env) {
        this();
        IBusinessInformation bizInfo = ((IBusinessInformationService) ServiceManagerFactory.getInstance().getServiceManager()
                .getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();
        currencies = bizInfo.getAllCurrencyProperties(env);
    }

    /**
     * Method Description: Validated the rating details and logs the errors
     * 
     * @param ratingDetails
     * @param ratingType
     * @return true is details are valid else false
     */
    public boolean validAndLogErrors(RatingDetails ratingDetails, String batchRef, BatchLogger batchLogger) {
        boolean validEntityCode = false;
        boolean validEntityType = true;
        boolean validCurrency = true;
        boolean validRatingCode = false;
        boolean validRatingTerm = false;
        boolean ValidRatingValue = false;
        boolean validCurrencyRatingCode = false;
        boolean validCurrencyRatingTerm = false;
        boolean ValidCurrencyRatingValue = false;

        OverallRating overallRating = ratingDetails.getOverallRating();
        CurrencyRating oCurrencyRating = ratingDetails.getCurrencyRating();
        String ratingType = ratingDetails.getEntityType();

        String errorMessage = CommonConstants.EMPTY_STRING;

        validEntityType = (ratingType.equals(ENTITY_TYPE_ASSET) || ratingType.equals(ENTITY_TYPE_COUNTRY)
                || ratingType.equals(ENTITY_TYPE_CUSTOMER) || ratingType.equals(ENTITY_TYPE_FXDEAL) || ratingType
                .equals(ENTITY_TYPE_MITIGANTS));
        if (!errorEntityCodes.contains(ratingDetails.getEntityCode())) {
            validEntityCode = isEntityValid(ratingType, ratingDetails.getEntityCode());
        }

        if (ratingType.equals(ENTITY_TYPE_CUSTOMER)) {
            if (!oCurrencyRating.getCurrencyCode().equals(CommonConstants.EMPTY_STRING)) {

                if (!errorCurriencies.contains(oCurrencyRating.getCurrencyCode())) {
                    validCurrency = isCurrencyValid(oCurrencyRating.getCurrencyCode());
                }
                else {
                    validCurrency = false;
                }

                if (!errorRatingCodes.contains(oCurrencyRating.getAgencyCode())) {
                    validCurrencyRatingCode = isRatingAgencyValid(oCurrencyRating.getAgencyCode());

                }

                if (!errorRatingTerms.contains(oCurrencyRating.getAgencyCode() + oCurrencyRating.getTerm())) {
                    validCurrencyRatingTerm = isRatingTermValid(oCurrencyRating.getAgencyCode(), oCurrencyRating.getTerm());

                }

                if (!errorRatingValues.contains(oCurrencyRating.getAgencyCode() + oCurrencyRating.getTerm()
                        + oCurrencyRating.getValue())) {
                    ValidCurrencyRatingValue = isRatingValueValid(oCurrencyRating.getAgencyCode(), oCurrencyRating.getTerm(),
                            oCurrencyRating.getValue());

                }
                if (overallRating.getAgencyCode().equals(CommonConstants.EMPTY_STRING)
                        && overallRating.getTerm().equals(CommonConstants.EMPTY_STRING)
                        && overallRating.getValue().equals(CommonConstants.EMPTY_STRING)) {
                    validRatingCode = (ValidRatingValue = (validRatingTerm = true));
                }
                else {
                    if (!errorRatingCodes.contains(overallRating.getAgencyCode())) {
                        validRatingCode = isRatingAgencyValid(overallRating.getAgencyCode());

                    }

                    if (!errorRatingTerms.contains(overallRating.getAgencyCode() + overallRating.getTerm())) {
                        validRatingTerm = isRatingTermValid(overallRating.getAgencyCode(), overallRating.getTerm());

                    }

                    if (!errorRatingValues.contains(overallRating.getAgencyCode() + overallRating.getTerm()
                            + overallRating.getValue())) {
                        ValidRatingValue = isRatingValueValid(overallRating.getAgencyCode(), overallRating.getTerm(),
                                overallRating.getValue());

                    }
                }
            }
            else {
                if (!errorRatingCodes.contains(overallRating.getAgencyCode())) {
                    validRatingCode = isRatingAgencyValid(overallRating.getAgencyCode());

                }

                if (!errorRatingTerms.contains(overallRating.getAgencyCode() + overallRating.getTerm())) {
                    validRatingTerm = isRatingTermValid(overallRating.getAgencyCode(), overallRating.getTerm());

                }

                if (!errorRatingValues.contains(overallRating.getAgencyCode() + overallRating.getTerm() + overallRating.getValue())) {
                    ValidRatingValue = isRatingValueValid(overallRating.getAgencyCode(), overallRating.getTerm(),
                            overallRating.getValue());

                }

            }

        }
        else if (ratingType.equals(ENTITY_TYPE_COUNTRY)) {
            if (!oCurrencyRating.getCurrencyCode().equals(CommonConstants.EMPTY_STRING)) {

                if (!errorCurriencies.contains(oCurrencyRating.getCurrencyCode())) {
                    validCurrency = isCurrencyValid(oCurrencyRating.getCurrencyCode());
                }
                else {
                    validCurrency = false;
                }

                if (!errorRatingCodes.contains(oCurrencyRating.getAgencyCode())) {
                    validCurrencyRatingCode = isRatingAgencyValid(oCurrencyRating.getAgencyCode());

                }

                if (!errorRatingTerms.contains(oCurrencyRating.getAgencyCode() + oCurrencyRating.getTerm())) {
                    validCurrencyRatingTerm = isRatingTermValid(oCurrencyRating.getAgencyCode(), oCurrencyRating.getTerm());

                }

                if (!errorRatingValues.contains(oCurrencyRating.getAgencyCode() + oCurrencyRating.getTerm()
                        + oCurrencyRating.getValue())) {
                    ValidCurrencyRatingValue = isRatingValueValid(oCurrencyRating.getAgencyCode(), oCurrencyRating.getTerm(),
                            oCurrencyRating.getValue());

                }
                if (overallRating.getAgencyCode().equals(CommonConstants.EMPTY_STRING)
                        && overallRating.getTerm().equals(CommonConstants.EMPTY_STRING)
                        && overallRating.getValue().equals(CommonConstants.EMPTY_STRING)) {
                    validRatingCode = (ValidRatingValue = (validRatingTerm = true));
                }
                else {
                    if (!errorRatingCodes.contains(overallRating.getAgencyCode())) {
                        validRatingCode = isRatingAgencyValid(overallRating.getAgencyCode());

                    }

                    if (!errorRatingTerms.contains(overallRating.getAgencyCode() + overallRating.getTerm())) {
                        validRatingTerm = isRatingTermValid(overallRating.getAgencyCode(), overallRating.getTerm());

                    }

                    if (!errorRatingValues.contains(overallRating.getAgencyCode() + overallRating.getTerm()
                            + overallRating.getValue())) {
                        ValidRatingValue = isRatingValueValid(overallRating.getAgencyCode(), overallRating.getTerm(),
                                overallRating.getValue());

                    }
                }
            }
            else {
                if (!errorRatingCodes.contains(overallRating.getAgencyCode())) {
                    validRatingCode = isRatingAgencyValid(overallRating.getAgencyCode());

                }

                if (!errorRatingTerms.contains(overallRating.getAgencyCode() + overallRating.getTerm())) {
                    validRatingTerm = isRatingTermValid(overallRating.getAgencyCode(), overallRating.getTerm());

                }

                if (!errorRatingValues.contains(overallRating.getAgencyCode() + overallRating.getTerm() + overallRating.getValue())) {
                    ValidRatingValue = isRatingValueValid(overallRating.getAgencyCode(), overallRating.getTerm(),
                            overallRating.getValue());

                }

            }

        }
        else {

            if (!errorRatingCodes.contains(overallRating.getAgencyCode())) {
                validRatingCode = isRatingAgencyValid(overallRating.getAgencyCode());

            }

            if (!errorRatingTerms.contains(overallRating.getAgencyCode() + overallRating.getTerm())) {
                validRatingTerm = isRatingTermValid(overallRating.getAgencyCode(), overallRating.getTerm());

            }

            if (!errorRatingValues.contains(overallRating.getAgencyCode() + overallRating.getTerm() + overallRating.getValue())) {
                ValidRatingValue = isRatingValueValid(overallRating.getAgencyCode(), overallRating.getTerm(),
                        overallRating.getValue());

            }
        }

        if (((!validEntityType) && (!validEntityCode) && (!validRatingCode) && (!validRatingTerm) && (!ValidRatingValue))) {
            errorMessage = BankFusionMessages.getFormattedMessage(40413000, new Object[] {});
            batchLogger.createBatchErrorLog(batchRef, ratingType, ratingDetails.getEntityCode(), overallRating.getAgencyCode(),
                    overallRating.getTerm(), overallRating.getValue(), CommonConstants.EMPTY_STRING, errorMessage);
            return validEntityType && validEntityCode && validCurrency && validRatingCode && validRatingTerm && ValidRatingValue;
        }

        if (!(validEntityType && validEntityCode && validCurrency && validRatingCode && validRatingTerm && ValidRatingValue)) {
            if (!validEntityType) {
                errorMessage = BankFusionMessages.getFormattedMessage(getEntityCodeErrorNumber(ratingType),
                        new Object[] { ratingType });
                batchLogger.createBatchErrorLog(batchRef, ratingType, CommonConstants.EMPTY_STRING, CommonConstants.EMPTY_STRING,
                        CommonConstants.EMPTY_STRING, CommonConstants.EMPTY_STRING, CommonConstants.EMPTY_STRING, errorMessage);
                return validEntityType;
            }

            if (!validEntityCode) {
                if (!errorEntityCodes.contains(ratingDetails.getEntityCode())) {
                    errorEntityCodes.add(ratingDetails.getEntityCode());
                }
                errorMessage = BankFusionMessages.getFormattedMessage(getEntityCodeErrorNumber(ratingType),
                        new Object[] { ratingDetails.getEntityCode() });
                batchLogger.createBatchErrorLog(batchRef, ratingType, ratingDetails.getEntityCode(), CommonConstants.EMPTY_STRING,
                        CommonConstants.EMPTY_STRING, CommonConstants.EMPTY_STRING, CommonConstants.EMPTY_STRING, errorMessage);
            }

            if (!validRatingCode) {
                if (!errorRatingCodes.contains(overallRating.getAgencyCode())) {
                    errorRatingCodes.add(overallRating.getAgencyCode());
                }
                errorMessage = BankFusionMessages.getFormattedMessage(40413008, new Object[] { overallRating.getAgencyCode() });
                batchLogger.createBatchErrorLog(batchRef, ratingType, ratingDetails.getEntityCode(), overallRating.getAgencyCode(),
                        CommonConstants.EMPTY_STRING, CommonConstants.EMPTY_STRING, CommonConstants.EMPTY_STRING, errorMessage);
            }
            if (!validRatingTerm) {
                if (!errorRatingTerms.contains(overallRating.getAgencyCode() + overallRating.getTerm())) {
                    errorRatingTerms.add(overallRating.getAgencyCode() + overallRating.getTerm());
                }
                errorMessage = BankFusionMessages.getFormattedMessage(40413009, new Object[] { overallRating.getTerm(),
                        overallRating.getAgencyCode() });
                batchLogger.createBatchErrorLog(batchRef, ratingType, ratingDetails.getEntityCode(), overallRating.getAgencyCode(),
                        CommonConstants.EMPTY_STRING, overallRating.getTerm(), CommonConstants.EMPTY_STRING, errorMessage);
            }
            if (!ValidRatingValue) {
                if (!errorRatingValues.contains(overallRating.getAgencyCode() + overallRating.getTerm() + overallRating.getValue())) {
                    errorRatingValues.add(overallRating.getAgencyCode() + overallRating.getTerm() + overallRating.getValue());
                }
                errorMessage = BankFusionMessages.getFormattedMessage(40413010, new Object[] { overallRating.getValue(),
                        overallRating.getAgencyCode(), overallRating.getTerm() });
                batchLogger.createBatchErrorLog(batchRef, ratingType, ratingDetails.getEntityCode(), overallRating.getAgencyCode(),
                        overallRating.getTerm(), overallRating.getValue(), CommonConstants.EMPTY_STRING, errorMessage);
            }
        }
        if ((ratingType.equals(ENTITY_TYPE_CUSTOMER) || ratingType.equals(ENTITY_TYPE_COUNTRY))
                && !oCurrencyRating.getCurrencyCode().equals(CommonConstants.EMPTY_STRING)) {

            if (!validCurrency) {
                if (!errorCurriencies.contains(oCurrencyRating.getCurrencyCode())) {
                    errorCurriencies.add(oCurrencyRating.getCurrencyCode());
                }
                errorMessage = BankFusionMessages.getFormattedMessage(40413002, new Object[] { oCurrencyRating.getCurrencyCode() });
                batchLogger
                        .createBatchErrorLog(batchRef, ratingType, ratingDetails.getEntityCode(), CommonConstants.EMPTY_STRING,
                                CommonConstants.EMPTY_STRING, CommonConstants.EMPTY_STRING, oCurrencyRating.getCurrencyCode(),
                                errorMessage);
            }

            if (!validCurrencyRatingCode) {
                if (!errorRatingCodes.contains(oCurrencyRating.getAgencyCode())) {
                    errorRatingCodes.add(oCurrencyRating.getAgencyCode());
                }
                errorMessage = BankFusionMessages.getFormattedMessage(40413008, new Object[] { oCurrencyRating.getAgencyCode() });
                batchLogger.createBatchErrorLog(batchRef, ratingType, ratingDetails.getEntityCode(),
                        oCurrencyRating.getAgencyCode(), CommonConstants.EMPTY_STRING, CommonConstants.EMPTY_STRING,
                        oCurrencyRating.getCurrencyCode(), errorMessage);
            }
            if (!validCurrencyRatingTerm) {
                if (!errorRatingTerms.contains(oCurrencyRating.getAgencyCode() + oCurrencyRating.getTerm())) {
                    errorRatingTerms.add(oCurrencyRating.getAgencyCode() + oCurrencyRating.getTerm());
                }
                errorMessage = BankFusionMessages.getFormattedMessage(40413009, new Object[] { oCurrencyRating.getTerm(),
                        oCurrencyRating.getAgencyCode() });
                batchLogger.createBatchErrorLog(batchRef, ratingType, ratingDetails.getEntityCode(),
                        oCurrencyRating.getAgencyCode(), CommonConstants.EMPTY_STRING, oCurrencyRating.getTerm(),
                        oCurrencyRating.getCurrencyCode(), errorMessage);
            }
            if (!ValidCurrencyRatingValue) {
                if (!errorRatingValues.contains(oCurrencyRating.getAgencyCode() + oCurrencyRating.getTerm()
                        + oCurrencyRating.getValue())) {
                    errorRatingValues.add(oCurrencyRating.getAgencyCode() + oCurrencyRating.getTerm() + oCurrencyRating.getValue());
                }
                errorMessage = BankFusionMessages.getFormattedMessage(40413010, new Object[] { oCurrencyRating.getValue(),
                        oCurrencyRating.getAgencyCode(), oCurrencyRating.getTerm() });
                batchLogger.createBatchErrorLog(batchRef, ratingType, ratingDetails.getEntityCode(),
                        oCurrencyRating.getAgencyCode(), oCurrencyRating.getTerm(), oCurrencyRating.getValue(),
                        oCurrencyRating.getCurrencyCode(), errorMessage);
            }
            return validEntityType && validEntityCode && validCurrency && validRatingCode && validRatingTerm && ValidRatingValue
                    && validCurrencyRatingCode && validCurrencyRatingTerm && ValidCurrencyRatingValue;
        }
        else {
            return validEntityType && validEntityCode && validCurrency && validRatingCode && validRatingTerm && ValidRatingValue;
        }
    }

    /**
     * Method Description: Validates the Customer number
     * 
     * @param customerCode
     * @return true if customer is valid else returns false
     */
    private boolean isCustomerValid(String customerCode) {
        if (customerCode.equals(CommonConstants.EMPTY_STRING)) {
            return false;
        }
        IBOCustomer customerBO = (IBOCustomer) factory.findByPrimaryKey(IBOCustomer.BONAME, customerCode, true);
        return customerBO == null ? false : true;
    }

    /**
     * Method Description: Validates Rating Agency code
     * 
     * @param ratingAgencyCode
     * @return true if Rating Agency Code is valid else returns false
     */
    private boolean isRatingAgencyValid(String ratingAgencyCode) {
        if (ratingAgencyCode.equals(CommonConstants.EMPTY_STRING)) {
            return false;
        }
        ArrayList<String> params = new ArrayList<String>();
        params.add(ratingAgencyCode);
        List<IBOUBTB_RATINGAGENCYCODES> ratingCodes = factory.findByQuery(IBOUBTB_RATINGAGENCYCODES.BONAME,
                RATING_AGENCY_CODE_WHERECLAUSE, params, null, false);
        return ratingCodes.size() == 0 ? false : true;
    }

    /**
     * Method Description: Validates Rating Term for a given Rating Agency
     * 
     * @param ratingAgencyCode
     * @param ratingType
     * @return true if valid else false
     */
    private boolean isRatingTermValid(String ratingAgencyCode, String ratingTerm) {
        if (ratingAgencyCode.equals(CommonConstants.EMPTY_STRING) || ratingTerm.equals(CommonConstants.EMPTY_STRING)) {
            return false;
        }
        ArrayList<String> params = new ArrayList<String>();
        params.add(ratingAgencyCode);
        params.add(ratingTerm);
        List<IBOUBTB_RATINGDETAILS> ratingDetails = factory.findByQuery(IBOUBTB_RATINGDETAILS.BONAME, RATING_TYPE_WHERECLAUSE,
                params, null, false);
        return ratingDetails.size() == 0 ? false : true;
    }

    /**
     * Method Description: Validates Rating Value for a given Rating Agency and Rating Term
     * 
     * @param ratingAgencyCode
     * @param ratingType
     * @param ratingValue
     * @return true if valid else false
     */
    private boolean isRatingValueValid(String ratingAgencyCode, String ratingTerm, String ratingValue) {
        if (ratingAgencyCode.equals(CommonConstants.EMPTY_STRING) || ratingTerm.equals(CommonConstants.EMPTY_STRING)
                || ratingValue.equals(CommonConstants.EMPTY_STRING)) {
            return false;
        }
        ArrayList<String> params = new ArrayList<String>();
        params.add(ratingAgencyCode);
        params.add(ratingTerm);
        params.add(ratingValue);
        List<IBOUBTB_RATINGDETAILS> ratingDetails = factory.findByQuery(IBOUBTB_RATINGDETAILS.BONAME, RATING_VALUE_WHERECLAUSE,
                params, null, false);
        return ratingDetails.size() == 0 ? false : true;
    }

    /**
     * Method Description: Validates the Country Code
     * 
     * @param countryCode
     * @return true if valid else false
     */
    private boolean isCountryValid(String countryCode) {
        if (countryCode.equals(CommonConstants.EMPTY_STRING)) {
            return false;
        }
        return countriesList.contains(countryCode);
    }

    /**
     * Method Description: Validates Currency Code
     * 
     * @param isoCurrencyCode
     * @return true if valid else false
     */
    private boolean isCurrencyValid(String isoCurrencyCode) {
        if (isoCurrencyCode.equals(CommonConstants.EMPTY_STRING)) {
            return false;
        }

        return currencies.containsKey(isoCurrencyCode);
    }

    /**
     * Method Description:
     * 
     * @param asset
     * @return true if valid else false
     */
    private boolean isValidAssit(String asset) {
        if (asset.equals(CommonConstants.EMPTY_STRING)) {
            return false;
        }
        IBOAttributeCollectionFeature accountBO = (IBOAttributeCollectionFeature) factory.findByPrimaryKey(
                IBOAttributeCollectionFeature.BONAME, asset, true);
        return accountBO == null ? false : true;
    }

    /**
     * Method Description: Validates Collateral Code
     * 
     * @param collateralCode
     * @return true if valid else false
     */
    private boolean isCollateralValid(String collateralCode) {
        if (collateralCode.equals(CommonConstants.EMPTY_STRING)) {
            return false;
        }
        ArrayList<String> params = new ArrayList<String>();
        params.add(collateralCode);
        List<IBOCustomerCollateral> collateralDetail = factory.findByQuery(IBOCustomerCollateral.BONAME, COLLATERAL_WHERECLAUSE,
                params, null, false);
        return collateralDetail.size() == 0 ? false : true;
    }

    /**
     * Method Description: Validates Fx Deal Reference
     * 
     * @param dealRef
     * @return true if valid else false
     */
    private boolean isFxDealValid(String dealRef) {
        if (dealRef.equals(CommonConstants.EMPTY_STRING)) {
            return false;
        }
        ArrayList<String> params = new ArrayList<String>();
        params.add(dealRef);
        List<IBOForexDeals> fxDealDetails = factory.findByQuery(IBOForexDeals.BONAME, FXDEAL_WHERECLAUSE, params, null, false);
        return fxDealDetails.size() == 0 ? false : true;
    }

    /**
     * Method Description: Returns the error code based on the entity type
     * 
     * @param ratingType
     * @return error code
     */
    private int getEntityCodeErrorNumber(String ratingType) {

        if (ratingType.equals(ENTITY_TYPE_CUSTOMER)) {
            return 40413003;
        }
        else if (ratingType.equals(ENTITY_TYPE_COUNTRY)) {
            return 40413004;
        }
        else if (ratingType.equals(ENTITY_TYPE_MITIGANTS)) {
            return 40413005;
        }
        else if (ratingType.equals(ENTITY_TYPE_ASSET)) {
            return 40413006;
        }
        else if (ratingType.equals(ENTITY_TYPE_FXDEAL)) {
            return 40413007;
        }
        else return 40413001;
    }

    /**
     * Method Description: Validates the Entity Code based on the Entity type
     * 
     * @param ratingType
     * @param entityCode
     * @return true if valid else false.
     */
    private boolean isEntityValid(String ratingType, String entityCode) {

        if (ratingType.equals(ENTITY_TYPE_CUSTOMER) && !errorEntityCodes.contains(entityCode)) {

            return isCustomerValid(entityCode);
        }
        else if (ratingType.equals(ENTITY_TYPE_COUNTRY) && !errorEntityCodes.contains(entityCode)) {
            return isCountryValid(entityCode);

        }
        else if (ratingType.equals(ENTITY_TYPE_MITIGANTS) && !errorEntityCodes.contains(entityCode)) {
            return isCollateralValid(entityCode);

        }
        else if (ratingType.equals(ENTITY_TYPE_ASSET) && !errorEntityCodes.contains(entityCode)) {
            return isValidAssit(entityCode);

        }
        else if (ratingType.equals(ENTITY_TYPE_FXDEAL) && !errorEntityCodes.contains(entityCode)) {
            return isFxDealValid(entityCode);

        }
        else return false;
    }

}
