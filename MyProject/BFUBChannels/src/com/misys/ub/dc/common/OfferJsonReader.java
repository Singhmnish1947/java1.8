package com.misys.ub.dc.common;

import org.apache.commons.lang.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The purpose of this class to provide all the Utils methods to read the JSonObject field values
 * 
 * @author wsiddiqu
 */
public class OfferJsonReader {

    /**
     * Reads the subproductId from Json Casa Account Fields tag
     * 
     * @param jsonCasaAccountFields - Casa account fields Json Object prepared from DC request
     * @return String - SubProductId
     */
    public String getSubProductIdForCasa(JsonObject jsonCasaAccountFields) {

        String subProductId = StringUtils.EMPTY;
        JsonElement subProdIdElement = jsonCasaAccountFields.get(RequestResponseConstants.CREATE_ACCOUNT_REQ)
            .getAsJsonObject().get(RequestResponseConstants.SUB_PRODUCT_ID);

        if (subProdIdElement != null) {
            subProductId = subProdIdElement.getAsString();
        }
        return subProductId;
    }
    
    /**
     * Checks if the account type is CASA and return the CASA account fields as json object
     * 
     * @param jsonObject - prepared Json Object from DC request
     * @return jsonCasaAccountFields - return CASA account fields as json object 
     */
    public JsonObject checkIfCasaAccount(JsonObject jsonObject) {
        JsonElement casaJsonElement = null;
        JsonObject jsonCasaAccountFields = null;
        JsonArray accountJsonArray = jsonObject.get(RequestResponseConstants.ACCOUNT_FIELDS).getAsJsonArray();
        int size = accountJsonArray.size();
        
        while (size > 0) {
            casaJsonElement = accountJsonArray.get(size - 1).getAsJsonObject().get(RequestResponseConstants.CASA_ACCOUNT_FIELDS);
            if (null != casaJsonElement ) {
                jsonCasaAccountFields = casaJsonElement.getAsJsonObject();
                break;
            }
            size--;
        }
        return jsonCasaAccountFields;
    }
    
    /**
     * Checks if the account type is TD and return the TD account fields as json object
     * 
     * @param jsonObject - prepared Json Object from DC request
     * @return jsonTDAccountFields - return TD account fields as json object
     */
    public JsonObject checkIfTDAccount(JsonObject jsonObject) {
        JsonElement tdJsonElement = null;
        JsonObject jsonTDAccountFields = null;
        JsonArray accountJsonArray = jsonObject.get(RequestResponseConstants.ACCOUNT_FIELDS).getAsJsonArray();
        int size = accountJsonArray.size();
        
        while (size > 0) {
            tdJsonElement = accountJsonArray.get(size - 1).getAsJsonObject().get(RequestResponseConstants.TD_ACCOUNT_FIELDS);
            if (null != tdJsonElement ) {
                jsonTDAccountFields = tdJsonElement.getAsJsonObject();
                break;
            }
            size--;
        }
        return jsonTDAccountFields;
    }
}