package com.misys.ub.swift.tellerRemittance.utils;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.cbs.common.functions.CB_CMN_ReadProperty;
import com.misys.fbe.common.util.CommonUtil;

public class ApiUrls {

    private static final Log logger = LogFactory.getLog(ApiUrls.class);

    private static final String GPP_API_BASE_URI = "gpp.base.uri";

    private static final String SINGLE_CT_INITIATE_PATH = "/credit-transfer/single-ct/initiate";
    
    private static final String SINGLE_CT_FEES_PATH = "/single-ct/fees";

    private static final int E_FBE_GPP_INVALID_HOST_URI = 40430074;

    /**
     * Method Description:Single CT initiate Payment api url
     * 
     * @return
     */
    public static String getSingleCTInitiatePaymentUri() {
        String uri = getBaseUrl() + SINGLE_CT_INITIATE_PATH;

        if (validateHTTP_URI(uri)) {
            return uri;
        }
        else {
            logger.error("Error while parsing uri: " + uri);
            CommonUtil.handleUnParameterizedEvent(E_FBE_GPP_INVALID_HOST_URI);
        }
        return uri;
    }

    /**
     * Method Description:Single CT initiate Payment api url
     * 
     * @return
     */
    public static String getSingleCTFeesUri() {
        String uri = getBaseUrl() + SINGLE_CT_FEES_PATH;

        if (validateHTTP_URI(uri)) {
            return uri;
        }
        else {
            logger.error("Error while parsing uri: " + uri);
            CommonUtil.handleUnParameterizedEvent(E_FBE_GPP_INVALID_HOST_URI);
        }
        return uri;
    }
    
    public static String getBaseUrl() {
        return CB_CMN_ReadProperty.run(GPP_API_BASE_URI);

    }

    /**
     * Method Description:Validate teh URI
     * 
     * @param uri
     * @return
     */
    private static boolean validateHTTP_URI(String uri) {
        final URL url;
        try {
            url = new URL(uri);
        }
        catch (Exception e) {
            logger.error("FAB - Invalid request uri: ", e);
            return false;
        }
        return "http".equals(url.getProtocol());
    }

}
