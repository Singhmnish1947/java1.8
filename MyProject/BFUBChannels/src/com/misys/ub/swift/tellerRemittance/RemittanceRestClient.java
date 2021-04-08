package com.misys.ub.swift.tellerRemittance;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.finastra.api.paymentInititation.PaymentResponse;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.fbe.common.util.CommonUtil;
import com.misys.ub.common.events.CommonsEventCodes;
import com.misys.ub.swift.tellerRemittance.utils.ApiUrls;
import com.misys.ub.swift.tellerRemittance.utils.RemittanceConstants;
import com.misys.ub.swift.tellerRemittance.utils.RemittanceStatusDto;

/**
 * @author sravind0 Client for consuming fee and billing (Fusion billing)
 *         APIs.<br />
 *         All methods are static and require no instantiation.
 */
public class RemittanceRestClient {

	private static final Log LOGGER = LogFactory.getLog(RemittanceRestClient.class);

	private RemittanceRestClient() {
	}

	/**
	 * Method Description:Rest Client call for GPP Initiate Payment Api
	 * 
	 * @param paymentInitiationRq
	 * @return
	 */

	public static RemittanceStatusDto initaitePaymentApi(String paymentInitiationRq,
			RemittanceStatusDto gppPaymentStatus) {
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		HttpEntity request = new HttpEntity(paymentInitiationRq, headers);

		LOGGER.info("Api URI::::: " + ApiUrls.getSingleCTInitiatePaymentUri());

		try {
			ResponseEntity<PaymentResponse> genericResponse = getRestTemplateInstance()
					.exchange(ApiUrls.getSingleCTInitiatePaymentUri(), HttpMethod.POST, request, PaymentResponse.class);
			switch (genericResponse.getStatusCode().value()) {
			case 200:
				if (null != genericResponse.getBody()) {
					PaymentResponse payResp = genericResponse.getBody();

					if (genericResponse.getHeaders().getLocation() != null
							&& StringUtils.isNotBlank(genericResponse.getHeaders().getLocation().toString())) {
						LOGGER.info("LocationStatusId::::: " + genericResponse.getHeaders().getLocation().toString());
						gppPaymentStatus.setGppLocationUrl(genericResponse.getHeaders().getLocation().toString());

						gppPaymentStatus.setGppPaymentStatusId(
								getGppPaymentStatusId(genericResponse.getHeaders().getLocation().toString()));
					}

					LinkedHashMap inputMap = (LinkedHashMap) (LinkedHashMap) payResp.get("CstmrPmtStsRpt");
					if (inputMap != null) {
						getCstmrPmtStsRpt(inputMap, gppPaymentStatus);
					}

				}
				break;
			case 400:
				CommonUtil.handleUnParameterizedEvent(RemittanceConstants.E_FBE_GPP_API_REQ_ERROR);
				break;
			case 401:
			case 500:
				CommonUtil.handleUnParameterizedEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED);
				break;
			default:
				LOGGER.error("Invalid response code (" + genericResponse.getStatusCode() + ") from GPP!");
			}
		} catch (HttpClientErrorException cEx) {
			LOGGER.error("GPP Error Response:::::" + cEx.getResponseBodyAsString());
			CommonUtil.handleParameterizedEvent(RemittanceConstants.E_GPP_API_SCHEMA_VALIDATION_ERROR,
					new String[] { cEx.getResponseBodyAsString() });
		} catch (RestClientException rex) {
			LOGGER.error("Error while processing API response" + ExceptionUtil.getExceptionAsString(rex));
			CommonUtil.handleUnParameterizedEvent(RemittanceConstants.E_FBE_GPP_API_REQ_ERROR);
		} catch (Exception e) {
			LOGGER.error("Unexpected error: " + ExceptionUtil.getExceptionAsString(e));
			CommonUtil.handleUnParameterizedEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED);
		}
		return gppPaymentStatus;
	}

	private static RestTemplate getRestTemplateInstance() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
		return restTemplate;
	}

	private static String getGppPaymentStatusId(String location) {
		String statusId = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(location)) {
			String c = "credit-transfer";
			String params = location.replace("/", "");
			int status_occurence = params.indexOf("status");
			statusId = params.substring(c.length(), status_occurence);
		}
		return statusId;
	}

	private static RemittanceStatusDto getCstmrPmtStsRpt(LinkedHashMap map, RemittanceStatusDto gppStatusDto) {

		// Get a set of the entries
		Set set = map.entrySet();

		// Get an iterator
		Iterator itr = set.iterator();

		// Display elements
		while (itr.hasNext()) {
			Map.Entry me = (Map.Entry) itr.next();
			LOGGER.info("Map entry::::: " + me.getKey() + ": " + me.getValue());
			if (me.getKey().equals("SplmtryData")) {
				getSplmtryData(me.getValue(), gppStatusDto);
			}

			if (me.getKey().equals("OrgnlPmtInfAndSts")) {
				getOrgnlPmtInfAndSts(me.getValue(), gppStatusDto);
			}
		}
		return gppStatusDto;

	}

	/**
	 * Method Description:Get SplmtryData from GPP response
	 * 
	 * @param value
	 * @param gppStatusDto
	 * @return
	 */
	private static RemittanceStatusDto getSplmtryData(Object value, RemittanceStatusDto gppStatusDto) {
		List splmtryDataList = (List) value;
		LinkedHashMap map4 = (LinkedHashMap) splmtryDataList.get(0);
		// Get a set of the entries
		Set child4Set = map4.entrySet();

		// Get an iterator
		Iterator child4Itr = child4Set.iterator();
		while (child4Itr.hasNext()) {
			Map.Entry child4Me = (Map.Entry) child4Itr.next();
			LOGGER.info("SplmtryData Map entry::::: " + child4Me.getKey() + ": " + child4Me.getValue());
			LinkedHashMap map5 = (LinkedHashMap) child4Me.getValue();
			Set child5Set = map5.entrySet();
			Iterator child5Itr = child5Set.iterator();
			while (child5Itr.hasNext()) {
				Map.Entry child5Me = (Map.Entry) child5Itr.next();
				if (child5Me.getKey().equals("Uetr")) {
					gppStatusDto.setUetr(child5Me.getValue().toString());
				} else {
					gppStatusDto.setUetr(StringUtils.EMPTY);
				}
			}
		}

		return gppStatusDto;

	}

	/**
	 * Method Description:Get OrgnlPmtInfAndSts from GPP response
	 * 
	 * @param value
	 * @param gppStatusDto
	 * @return
	 */
	private static RemittanceStatusDto getOrgnlPmtInfAndSts(Object value, RemittanceStatusDto gppStatusDto) {
		// Temporary hashmap created
		LinkedHashMap map1 = new LinkedHashMap();
		map1.put("temporaryMap", value);

		// Get a set of the entries
		Set childSet = map1.entrySet();

		// Get an iterator
		Iterator childItr = childSet.iterator();
		while (childItr.hasNext()) {
			Map.Entry childMe = (Map.Entry) childItr.next();
			List paymentInfoList = (List) childMe.getValue();
			LinkedHashMap map2 = (LinkedHashMap) paymentInfoList.get(0);
			// Get a set of the entries
			Set child2Set = map2.entrySet();

			// Get an iterator
			Iterator child2Itr = child2Set.iterator();
			while (child2Itr.hasNext()) {
				Map.Entry child2Me = (Map.Entry) child2Itr.next();
				LOGGER.info("OrgnlPmtInfAndSts Map entry::::: " + child2Me.getKey() + ": " + child2Me.getValue());
				if (child2Me.getKey().equals("TxInfAndSts")) {
					getTxInfAndSts(child2Me.getValue(), gppStatusDto);
				}
			}

		}

		return gppStatusDto;
	}

	/**
	 * Method Description:Get TxInfAndSts from GPP response
	 * 
	 * @param value
	 * @param gppStatusDto
	 * @return
	 */
	private static RemittanceStatusDto getTxInfAndSts(Object value, RemittanceStatusDto gppStatusDto) {
		List txInfAndStsList = (List) value;
		LinkedHashMap map3 = (LinkedHashMap) txInfAndStsList.get(0);
		Set child3Set = map3.entrySet();

		// Get an iterator
		Iterator child3Itr = child3Set.iterator();
		while (child3Itr.hasNext()) {
			Map.Entry child3Me = (Map.Entry) child3Itr.next();
			LOGGER.info("TxInfAndSts Map entry::::: " + child3Me.getKey() + ": " + child3Me.getValue());

			/*
			 * if (child3Me.getKey().equals("StsId")) {
			 * gppStatusDto.setGppPaymentStatusId((String) child3Me.getValue()); }
			 */

			if (child3Me.getKey().equals("OrgnlEndToEndId")) {
				gppStatusDto.setOriginalEndToEndId((String) child3Me.getValue());
			}

			if (child3Me.getKey().equals("TxSts")) {
				gppStatusDto.setGppTransactionIndividualStatus((String) child3Me.getValue());
			}

		}

		return gppStatusDto;
	}

}
