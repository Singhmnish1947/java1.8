package com.misys.ub.dc.common;

import java.text.SimpleDateFormat;

import com.google.gson.JsonObject;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;

/**
 * Holds all common methods which required update or tune the offer Json value
 * 
 * @author wsiddiqu
 */
public class OfferJsonTuner {

    /**
     * update the Json Object with Sub product ID "_subProductId": "" ---> "_subProductId": subProd
     * 
     * @param jsonCasaAccountFields - Casa account fields Json Object prepared from DC request
     * @param subProd - subproduct id {@IBOProductInheritance.UBSUBPRODUCTID}
     */
    public void updateSubProductIdToCasaAcc(JsonObject jsonCasaAccountFields, String subProd) {
        jsonCasaAccountFields.get(RequestResponseConstants.CREATE_ACCOUNT_REQ).getAsJsonObject().addProperty(RequestResponseConstants.SUB_PRODUCT_ID, subProd);
    }
    
    /**
     * update the Json Object with Sub product ID "_accountOpenDate": "" ---> "_accountOpenDate": subProd
     * 
     * @param jsonCasaAccountFields - Casa account fields Json Object prepared from DC request
     */
    public void updateAccountOpeningDateToCasaAcc(JsonObject jsonCasaAccountFields) {
        SimpleDateFormat format = new SimpleDateFormat(RequestResponseConstants.ACC_OPENING_DATE_FORMAT);
        jsonCasaAccountFields.get(RequestResponseConstants.CREATE_ACCOUNT_REQ).getAsJsonObject().addProperty(
                RequestResponseConstants.ACC_OPENING_DATE, format.format(SystemInformationManager.getInstance().getBFSystemDate()));
    }

    /**
     * update the Json Object with Sub product Currency "_currency": "" ---> "_currency": subProdCur
     * 
     * @param jsonCasaAccountFields - Casa account fields Json Object prepared from DC request
     * @param subProdCur - sub product currency code (USD, GBP etc..)
     */
    public void updateSubProdCurrToCasaAcc(JsonObject jsonCasaAccountFields, String subProdCur) {
        jsonCasaAccountFields.get(RequestResponseConstants.CREATE_ACCOUNT_REQ).getAsJsonObject().addProperty(RequestResponseConstants.CURRENCY, subProdCur);
    }
    
    /**
     * update the customer id inside Json Object based on the account type passed here
     * @param jsonCasaTDObject - CASA/TD account fields Json Object prepared from DC request
     * @param partyId - customer id to be updated
     * @param accountType - type of account (TD, CASA etc.)
     */
    public void updateCustomerNoToAccount(JsonObject jsonCasaTDObject, String partyId, String accountType) {
        if(accountType.equals(RequestResponseConstants.CASA_ACCOUNT_TYPE))
            updateCustomerNoToCasaAcc(jsonCasaTDObject, partyId);
        else if(accountType.equals(RequestResponseConstants.TD_ACCOUNT_TYPE))
            updateCustomerNoToTDAcc(jsonCasaTDObject, partyId);
    }
    
    /**
     * update the Json Object with  "_customerNo": "" ---> "_customerNo": partyId
     * 
     * @param jsonCasaAccountFields - Casa account fields Json Object prepared from DC request
     * @param partyId - customer no
     */
    public void updateCustomerNoToCasaAcc(JsonObject jsonCasaAccountFields, String partyId) {
        jsonCasaAccountFields.get(RequestResponseConstants.CREATE_ACCOUNT_REQ).getAsJsonObject().addProperty(RequestResponseConstants.CASA_CUSTOMERNO, partyId);
    }
    
    /**
     * update the Json Object with  "_customerId": "" ---> "_customerId": partyId
     * 
     * @param jsonObject - TD account fields Json Object prepared from DC request
     * @param partyId - customer Id
     */
    public void updateCustomerNoToTDAcc(JsonObject jsonTDAccountFields, String partyId) {
        jsonTDAccountFields.get(RequestResponseConstants.TERM_DEPOSIT_REQ).getAsJsonObject().addProperty(RequestResponseConstants.TD_CUSTOMERNO, partyId);
    }

}
