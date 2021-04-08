/* ********************************************************************************
 *  Copyright(c)2018  Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms. *
 *
 * ********************************************************************************
 */
package com.misys.ub.dc.validator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.misys.cbs.party.constants.PartyConstants;
import com.misys.cbs.party.helper.CustomerTypes;
import com.misys.fbe.common.util.CommonUtil;
import com.misys.fbe.interfaces.party.PartyMicroflowConstants;
import com.misys.ub.dc.common.RequestResponseConstants;
import com.trapedza.bankfusion.servercommon.microflow.FBPMicroflowServiceInvoker;

import bf.com.misys.cbs.msgs.party.v1r0.ValidatePtyDedupRq;
import bf.com.misys.cbs.msgs.party.v1r0.ValidatePtyDedupRs;
import bf.com.misys.cbs.types.NameDtl;
import bf.com.misys.cbs.types.PartyAddrDtl;
import bf.com.misys.cbs.types.PartyIdDtl;
import bf.com.misys.cbs.types.PerPtyDedupKeys;
import bf.com.misys.cbs.types.ValidatePtyDedupInp;
import bf.com.misys.cbs.types.header.Orig;
import bf.com.misys.cbs.types.header.RqHeader;

/**
 * A Validation service for Party details
 * 
 * @author sdoddama
 *
 */
@SuppressWarnings({ "rawtypes" })
public class PartyValidator {

    private static final Log LOGGER = LogFactory.getLog(PartyValidator.class.getName());

    private String emailContact;

    public String getEmailContact() {
        return this.emailContact;
    }

    public void setEmailContact(String emailContact) {
        this.emailContact = emailContact;
    }

    /**
     * Method Description: Validate party based on dedup search
     * 
     * @param jsonObj
     *            - Create Party json Object
     * @return boolean - true when duplication party found else false
     * @throws ParseException
     */

    public boolean validateDeDupDetails(JsonObject jsonObj) throws ParseException {
        LOGGER.info("Entering validateDeDup() method");
        HashMap<String, Object> params = new HashMap<>();
        RqHeader rqHeader = new RqHeader();
        Orig orig = new Orig();
        ValidatePtyDedupRq validatePtyDedupRq = new ValidatePtyDedupRq();
        ValidatePtyDedupInp validatePtyDedupInp = new ValidatePtyDedupInp();

        setValidatePtyDedupInputs(jsonObj, validatePtyDedupInp);

        orig.setChannelId(PartyConstants.CHANNEL_ID);
        rqHeader.setOrig(orig);
        validatePtyDedupRq.setRqHeader(rqHeader);
        validatePtyDedupRq.setValidatePtyDedupInp(validatePtyDedupInp);
        params.put("validatePtyDedupRq", validatePtyDedupRq);
        boolean isDupPartyFound = checkPartyDeDup(params);
        LOGGER.info("Exiting checkPartyDeDup() method with phone as contact value : " + isDupPartyFound);

        // Check for dedup with email as contact value
        if ((isDupPartyFound == false) && CommonUtil.checkIfNotNullOrEmpty(getEmailContact())) {
            LOGGER.info("Email : " + getEmailContact());
            validatePtyDedupInp.setPaContactValue(getEmailContact());
            validatePtyDedupRq.setValidatePtyDedupInp(validatePtyDedupInp);
            params.put("validatePtyDedupRq", validatePtyDedupRq);
            boolean isDupPartyFoundForEmail = checkPartyDeDup(params);

            LOGGER.info("Exiting checkPartyDeDup() method checked with email as contact value : " + isDupPartyFoundForEmail);
            isDupPartyFound = isDupPartyFoundForEmail;
        }

        LOGGER.info("Exiting validateDeDup() method with the value : " + isDupPartyFound);
        return isDupPartyFound;
    }

    private void setValidatePtyDedupInputs(JsonObject jsonObj, ValidatePtyDedupInp validatePtyDedupInp) throws ParseException {
        PerPtyDedupKeys personPartyDetails = preparePersonalPartyDtls(jsonObj);

        prepareContactAndAddressDtls(jsonObj, validatePtyDedupInp);

        validatePtyDedupInp.setPerPtyDtls(personPartyDetails);
        validatePtyDedupInp.setPartyType(CustomerTypes.Personal.toString());

        preparePartyTaxDtls(jsonObj, validatePtyDedupInp);
        preparePartyIdDtls(jsonObj, validatePtyDedupInp);
    }

    private void prepareContactAndAddressDtls(JsonObject partyFieldsJsonObj, ValidatePtyDedupInp validatePtyDedupInp) {
        if (isJsonFieldNotNull(partyFieldsJsonObj, RequestResponseConstants.REPEATABLE_BLOCKS)) {
            JsonArray repeatableBlockArrays = partyFieldsJsonObj.get(RequestResponseConstants.REPEATABLE_BLOCKS).getAsJsonArray();

            for (JsonElement jsonRepeatableElement : repeatableBlockArrays) {
                JsonObject jsonRepeatableBlock = jsonRepeatableElement.getAsJsonObject();
                if (isJsonFieldNotNull(jsonRepeatableBlock, RequestResponseConstants.CONTACT_METHOD)) {
                    prepareContactValueDtls(validatePtyDedupInp, jsonRepeatableBlock);
                }
                else if (isJsonFieldNotNull(jsonRepeatableBlock, RequestResponseConstants.PARTY_DEFAULT_ADDRESS)
                        && Boolean.valueOf(jsonRepeatableBlock.get(RequestResponseConstants.PARTY_DEFAULT_ADDRESS).getAsString())) {
                    preparePartyAddressDtls(jsonRepeatableBlock, validatePtyDedupInp);
                }
            }

        }
    }

    private void prepareContactValueDtls(ValidatePtyDedupInp validatePtyDedupInp, JsonObject jsonRepeatableBlock) {
        if (isJsonFieldNotNull(jsonRepeatableBlock, RequestResponseConstants.CONTACT_VALUE)) {

            switch (jsonRepeatableBlock.get(RequestResponseConstants.CONTACT_METHOD).getAsString()) {
                case PartyConstants.CONTACT_PHONE:
                    validatePtyDedupInp
                            .setPaContactValue(jsonRepeatableBlock.get(RequestResponseConstants.CONTACT_VALUE).getAsString());
                    break;
                case PartyConstants.CONTACT_EMAIL:
                    setEmailContact(jsonRepeatableBlock.get(RequestResponseConstants.CONTACT_VALUE).getAsString());
                    break;
                default:
                    LOGGER.debug("Invalid choice");
            }

        }
    }

    private void preparePartyAddressDtls(JsonObject jsonObj, ValidatePtyDedupInp validatePtyDedupInp) {
        PartyAddrDtl partyAddress = new PartyAddrDtl();
        if (isJsonFieldNotNull(jsonObj, RequestResponseConstants.POSTAL_CODE)) {
            partyAddress.setPostCode(jsonObj.get(RequestResponseConstants.POSTAL_CODE).getAsString());
        }
        validatePtyDedupInp.setPartyAddress(partyAddress);
    }

    private void preparePartyIdDtls(JsonObject jsonObj, ValidatePtyDedupInp validatePtyDedupInp) {
        PartyIdDtl partyIdDtls = new PartyIdDtl();
        if (isJsonFieldNotNull(jsonObj, RequestResponseConstants.NATIONAL_ID)) {
            partyIdDtls.setIdReference(jsonObj.get(RequestResponseConstants.NATIONAL_ID).getAsString());
        }
        validatePtyDedupInp.setPartyIdDtls(partyIdDtls);
    }

    private void preparePartyTaxDtls(JsonObject jsonObj, ValidatePtyDedupInp validatePtyDedupInp) {
        PartyIdDtl partyTaxDtls = new PartyIdDtl();
        if (isJsonFieldNotNull(jsonObj, RequestResponseConstants.TAX_ID)) {
            partyTaxDtls.setIdReference(jsonObj.get(RequestResponseConstants.TAX_ID).getAsString());
        }
        validatePtyDedupInp.setPartyTaxDtls(partyTaxDtls);
    }

    private PerPtyDedupKeys preparePersonalPartyDtls(JsonObject jsonObj) throws ParseException {
        PerPtyDedupKeys personPartyDetails = new PerPtyDedupKeys();
        NameDtl nameDetails = new NameDtl();
        if (isJsonFieldNotNull(jsonObj, RequestResponseConstants.PARTY_FIRST_NAME)) {
            nameDetails.setFirstName(jsonObj.get(RequestResponseConstants.PARTY_FIRST_NAME).getAsString());
        }
        if (isJsonFieldNotNull(jsonObj, RequestResponseConstants.PARTY_LAST_NAME)) {
            nameDetails.setLastName(jsonObj.get(RequestResponseConstants.PARTY_LAST_NAME).getAsString());
        }
        personPartyDetails.setPartyNames(nameDetails);

        if (isJsonFieldNotNull(jsonObj, RequestResponseConstants.PARTY_DATE_OF_BIRTH)
                && CommonUtil.checkIfNotNullOrEmpty(jsonObj.get(RequestResponseConstants.PARTY_DATE_OF_BIRTH).getAsString())) {

            String dateInput = jsonObj.get(RequestResponseConstants.PARTY_DATE_OF_BIRTH).getAsString();
            String dateString = dateInput.substring(0, 10);
            String dateFormat = dateInput.substring(dateInput.length() - 10);

            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            Date dateOfBirth = sdf.parse(dateString);
            personPartyDetails.setDateOfBirth(new java.sql.Date(dateOfBirth.getTime()));
        }
        return personPartyDetails;
    }

    private boolean checkPartyDeDup(HashMap<String, Object> params) {
        boolean isDupPartyFound = false;

        FBPMicroflowServiceInvoker invoker = new FBPMicroflowServiceInvoker(PartyConstants.PARTY_DEDUP_CHECK_SERVICE);
        HashMap outputMap = invoker.invokeMicroflow(params, Boolean.valueOf(false));

        if (outputMap != null) {
            ValidatePtyDedupRs rs = (ValidatePtyDedupRs) outputMap.get(PartyMicroflowConstants.VALIDATE_PTY_DEDUP_RS);

            if (rs != null && rs.getRsHeader() != null && rs.getRsHeader().getStatus() != null
                    && RequestResponseConstants.STATUS_SUCCESS.equals(rs.getRsHeader().getStatus().getOverallStatus())) {
                isDupPartyFound = true;
            }
        }

        return isDupPartyFound;
    }

    private boolean isJsonFieldNotNull(JsonObject jsonObj, String fieldName) {
        boolean retValue = false;
        if (jsonObj.get(fieldName) != null && !jsonObj.get(fieldName).isJsonNull()) {
            retValue = true;
        }
        return retValue;
    }
}
