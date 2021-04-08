/* ********************************************************************************
 *  Copyright(c)2018  Finastra Financial Software Solutions. All Rights Reserved.
 *
 *  This software is the proprietary information of Finastra Financial Software Solutions.
 *  Use is subject to license terms.
 * ********************************************************************************
 * 
 *
 */

package com.misys.ub.dc.api.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.finastra.api.party.dto.EnableDisableLOBDtlDTO;
import com.finastra.api.party.dto.EnableDisableLOBDtlsRqDTO;
import com.finastra.api.party.dto.EnableDisableLOBDtlsRsDTO;
import com.finastra.api.party.partyConstants.PartyAPIConstants;
import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.util.BankFusionPropertySupport;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

/**
 * Rest client used for calling Party Rest service
 * 
 * @author Nisha Kumari
 *
 */
public class PartyApiClient {

    private PartyApiClient() {

    }

    private static final String REMOTE_PARTY_URL = "UB_PARTY_REMOTE_URL";

    private static final String REST_PATH = "bfweb/rest/";

    private static final String USER_LOCATOR = "userLocator";

    private static final String FORWARD_SLASH = "/";

    /**
     * Method Description: Method used to call party rest service to enable/disable line of business
     * 
     * @param partyId
     *            Unique Identifier of Party
     * @param lineOfBusiness
     *            Line Of Business
     * @param enableLOB
     *            <code>true</code> if line of business needs to be enabled. <code>false</code> if
     *            line of business needs to be disabled
     * @return <code>EnableDisableLOBDtlsRsDTO<code> containing the response details of party rest
     *         service
     * @throws URISyntaxException
     *             If error occurs while accessing the party rest service
     */
    public static EnableDisableLOBDtlsRsDTO enableLOB(String partyId, String lineOfBusiness, boolean enableLOB)
            throws URISyntaxException {

        RestTemplate restTemplate = new RestTemplate();
        EnableDisableLOBDtlsRsDTO lineOfBusinessRsDTO = null;

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(USER_LOCATOR, BankFusionThreadLocal.getUserLocator().toString());

        String partyUrl = BankFusionPropertySupport.getProperty(REMOTE_PARTY_URL, CommonConstants.EMPTY_STRING);

        StringBuilder requestUri = new StringBuilder(partyUrl);

        if (!partyUrl.endsWith(FORWARD_SLASH)) {
            requestUri.append(FORWARD_SLASH);
        }

        requestUri.append(REST_PATH).append(PartyAPIConstants.PARTY_REQ_MAPPING).append(PartyAPIConstants.PARTY_ENABLE_LOB);

        EnableDisableLOBDtlsRqDTO lineOfBusinessRqDTO = getLineOfBusinessDTO(partyId, lineOfBusiness, enableLOB);

        RequestEntity<EnableDisableLOBDtlsRqDTO> rqEntity = new RequestEntity<>(lineOfBusinessRqDTO, headers, HttpMethod.POST,
                new URI(requestUri.toString()));

        ResponseEntity<EnableDisableLOBDtlsRsDTO> responseEntity = restTemplate.exchange(rqEntity, EnableDisableLOBDtlsRsDTO.class);

        if (null != responseEntity && HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            lineOfBusinessRsDTO = responseEntity.getBody();
        }

        return lineOfBusinessRsDTO;
    }

    private static EnableDisableLOBDtlsRqDTO getLineOfBusinessDTO(String partyId, String lineOfBusiness, boolean enableLOB) {

        EnableDisableLOBDtlDTO lobDtlDTO = new EnableDisableLOBDtlDTO();
        lobDtlDTO.setLineOfBusiness(lineOfBusiness);
        lobDtlDTO.setEnableLOB(enableLOB);

        List<EnableDisableLOBDtlDTO> lobDtlDTOList = new ArrayList<>();
        lobDtlDTOList.add(lobDtlDTO);

        EnableDisableLOBDtlsRqDTO lineOfBusinessRqDTO = new EnableDisableLOBDtlsRqDTO();
        lineOfBusinessRqDTO.setPartyID(partyId);

        lineOfBusinessRqDTO.setEnableDisableLOBDtlDTO(lobDtlDTOList);

        return lineOfBusinessRqDTO;
    }
}
