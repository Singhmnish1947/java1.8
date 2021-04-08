package com.misys.ub.swift.tellerRemittance.charges;

import java.math.BigDecimal;
import java.util.ArrayList;
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

import com.finastra.api.paymentInititation.FeesCalculationResponse;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.common.runtime.toolkit.expression.function.RoundToScale;
import com.misys.fbe.common.util.CommonUtil;
import com.misys.ub.common.events.CommonsEventCodes;
import com.misys.ub.swift.tellerRemittance.utils.ApiUrls;
import com.misys.ub.swift.tellerRemittance.utils.FeesDto;
import com.misys.ub.swift.tellerRemittance.utils.RemittanceConstants;

import bf.com.misys.cbs.types.Currency;
import bf.com.misys.cbs.types.swift.FeesInformation;
import bf.com.misys.cbs.types.swift.TaxInformation;
import bf.com.misys.cbs.types.swift.TaxOnTaxInformation;
import bf.com.misys.cbs.types.swift.TxnfeesInformation;

public class RemittanceChargesRestClient {

	private static final Log LOGGER = LogFactory.getLog(RemittanceChargesRestClient.class);

	private static String feeCurrency = StringUtils.EMPTY;

	private RemittanceChargesRestClient() {
	}

	public static TxnfeesInformation feeCalculation(String feeCalculationRq) {
		TxnfeesInformation txnFeeInfo = new TxnfeesInformation();
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		HttpEntity request = new HttpEntity(feeCalculationRq, headers);

		FeesDto feeDto = new FeesDto();
		LOGGER.info("Api URI::::: " + ApiUrls.getSingleCTFeesUri());

		try {
			ResponseEntity<FeesCalculationResponse> genericResponse = getRestTemplateInstance()
					.exchange(ApiUrls.getSingleCTFeesUri(), HttpMethod.POST, request, FeesCalculationResponse.class);
			switch (genericResponse.getStatusCode().value()) {
			case 200:
				if (null != genericResponse.getBody()) {
					FeesCalculationResponse feeResponse = genericResponse.getBody();
					// feesInformation
					List feesInformationList = (List) feeResponse.get("feesInformation");
					if (!feesInformationList.isEmpty()) {
						FeesInformation[] feeInfoArray = new FeesInformation[feesInformationList.size()];
						feeDto = getFeeInformation(feesInformationList, feeDto);
						txnFeeInfo.setFeesInformation(feeDto.getFeelist().toArray(feeInfoArray));
					}

					// taxInformation
					List taxObjList = (List) feeResponse.get("taxInformation");
					if (!taxObjList.isEmpty()) {
						TaxInformation[] taxInfoArray = new TaxInformation[taxObjList.size()];
						feeDto = getTaxInformation(taxObjList, feeDto);
						txnFeeInfo.setTaxInformation(feeDto.getTaxlist().toArray(taxInfoArray));
					}

					// taxOnTaxInformation
					List taxOnTaxList = (List) feeResponse.get("taxOnTaxInformation");
					if (!taxOnTaxList.isEmpty()) {
						TaxOnTaxInformation[] taxOnTaxInfoArray = new TaxOnTaxInformation[taxOnTaxList.size()];
						feeDto = getTaxOnTaxInformation(taxOnTaxList, feeDto);
						txnFeeInfo.setTaxOnTaxInformation(feeDto.getTaxOnTaxlist().toArray(taxOnTaxInfoArray));
					}

					txnFeeInfo.setTotalFeeAmount(feeDto.getTotalFees());
					txnFeeInfo.setTotalTaxAmount(feeDto.getTotalTax());
					txnFeeInfo.setTotalTaxOnTaxAmount(feeDto.getTotalTaxOnTax());
					Currency totalChargeDebitAmount = new Currency();
					totalChargeDebitAmount
							.setAmount(feeDto.getTotalFees().getAmount().add(feeDto.getTotalTax().getAmount()));
					totalChargeDebitAmount.setIsoCurrencyCode(feeDto.getTotalFees().getIsoCurrencyCode());
					txnFeeInfo.setTotalChargeDebitAmount(totalChargeDebitAmount);
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
		return txnFeeInfo;
	}

	private static RestTemplate getRestTemplateInstance() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
		return restTemplate;
	}

	/**
	 * // feesInformation // feeAmount // feeCurrency // feeName // feeCategory
	 * 
	 * @param feeObject
	 */
	private static FeesDto getFeeInformation(List feesInformationList, FeesDto feeDto) {
		List<FeesInformation> feelist = new ArrayList<>();
		Currency totalFeeAmount = new Currency();
		BigDecimal consolidatedFeeAmount = BigDecimal.ZERO;

		for (Object fee : feesInformationList) {
			LinkedHashMap feesInformationMap = (LinkedHashMap) fee;
			Set set = feesInformationMap.entrySet();
			Iterator i = set.iterator();
			FeesInformation feeInfo = new FeesInformation();
			Currency feeAmount = new Currency();
			// Display elements
			while (i.hasNext()) {
				Map.Entry me = (Map.Entry) i.next();
				LOGGER.info("FeeInformation::::: " + me.getKey() + ": " + me.getValue());

				// feeCurrency
				if (me.getKey().equals("feeCurrency")) {
					feeCurrency = (String) me.getValue();
					feeAmount.setIsoCurrencyCode(feeCurrency);
				}

				// feeAmount
				if (me.getKey().equals("feeAmount")) {
					Double feeAmt = (Double) me.getValue();
					feeAmount.setAmount(RoundToScale.run(new BigDecimal(feeAmt), feeCurrency));
					consolidatedFeeAmount = consolidatedFeeAmount.add(feeAmount.getAmount());
				}
				feeInfo.setFeeAmount(feeAmount);

				// feeCategory
				if (me.getKey().equals("feeCategory")) {
					feeInfo.setFeeCategory((String) me.getValue());
				}

				// feeName
				if (me.getKey().equals("feeName")) {
					feeInfo.setFeeName((String) me.getValue());
				}
			}
			feelist.add(feeInfo);
		}

		totalFeeAmount.setIsoCurrencyCode(feeCurrency);
		totalFeeAmount.setAmount(RoundToScale.run(consolidatedFeeAmount, feeCurrency));
		feeDto.setTotalFees(totalFeeAmount);
		feeDto.setFeelist(feelist);

		return feeDto;
	}

	/**
	 * // taxInformation // taxAmount // feeCurrency // taxPercentage // description
	 * 
	 * @param taxObject
	 */
	private static FeesDto getTaxInformation(List taxList, FeesDto feeDto) {
		List<TaxInformation> taxlist = new ArrayList<>();
		BigDecimal consolidatedTaxAmount = BigDecimal.ZERO;
		Currency totalTaxAmount = new Currency();
		for (Object taxObj : taxList) {
			LinkedHashMap taxMap = (LinkedHashMap) taxObj;
			Set set = taxMap.entrySet();
			Iterator i = set.iterator();
			TaxInformation taxInfo = new TaxInformation();
			Currency taxAmount = new Currency();
			// Display elements
			while (i.hasNext()) {
				Map.Entry me = (Map.Entry) i.next();
				LOGGER.info("TaxInformation::::: " + me.getKey() + ": " + me.getValue());
				// feeCurrency
				taxAmount.setIsoCurrencyCode(feeCurrency);

				// taxAmount
				if (me.getKey().equals("taxAmount")) {
					Double taxAmt = (Double) me.getValue();
					taxAmount.setAmount(RoundToScale.run(new BigDecimal(taxAmt), feeCurrency));
					consolidatedTaxAmount = consolidatedTaxAmount.add(taxAmount.getAmount());
				}
				taxInfo.setTaxAmount(taxAmount);

				// taxPercentage
				if (me.getKey().equals("taxPercentage")) {
					Double taxPercentage = (Double) me.getValue();
					taxInfo.setTaxPercentage(taxPercentage.intValue());
				}

				// description
				if (me.getKey().equals("description")) {
					taxInfo.setDescription((String) me.getValue());
				}
			}
			taxlist.add(taxInfo);
		}
		totalTaxAmount.setIsoCurrencyCode(feeCurrency);
		totalTaxAmount.setAmount(RoundToScale.run(consolidatedTaxAmount, feeCurrency));
		feeDto.setTotalTax(totalTaxAmount);
		feeDto.setTaxlist(taxlist);
		return feeDto;
	}

	/**
	 * // taxOnTaxInformation // taxOnTaxAmount // feeCurrency // taxOnTaxPercentage
	 * // description
	 * 
	 * @param taxOnTaxObject
	 */
	private static FeesDto getTaxOnTaxInformation(List taxOnTaxList, FeesDto feeDto) {
		List<TaxOnTaxInformation> taxOnTaxNewlist = new ArrayList<>();
		Currency totalTaxOnTaxAmount = new Currency();
		BigDecimal consolidateTaxOnTaxAmt = BigDecimal.ZERO;
		int count = 0;
		for (Object taxOnTaxObj : taxOnTaxList) {
			LinkedHashMap taxMap = (LinkedHashMap) taxOnTaxObj;
			Set set = taxMap.entrySet();
			Iterator i = set.iterator();
			TaxOnTaxInformation taxOnTaxInfo = new TaxOnTaxInformation();
			Currency taxOnTaxAmount = new Currency();
			// Display elements
			while (i.hasNext()) {
				Map.Entry me = (Map.Entry) i.next();
				LOGGER.info("TaxOnTaxInformation::::: " + me.getKey() + ": " + me.getValue());

				// feeCurrency
				taxOnTaxAmount.setIsoCurrencyCode(feeCurrency);
				// taxOnTaxAmount
				if (me.getKey().equals("taxOnTaxAmount")) {
					Double taxAmt = (Double) me.getValue();
					taxOnTaxAmount.setAmount(RoundToScale.run(new BigDecimal(taxAmt), feeCurrency));
					consolidateTaxOnTaxAmt = consolidateTaxOnTaxAmt.add(taxOnTaxAmount.getAmount());
				}
				taxOnTaxInfo.setTaxOnTaxAmount(taxOnTaxAmount);

				// taxOnTaxPercentage
				if (me.getKey().equals("taxOnTaxPercentage")) {
					Double taxPercentage = (Double) me.getValue();
					taxOnTaxInfo.setTaxOnTaxPercentage(taxPercentage.intValue());
				}

				// description
				if (me.getKey().equals("description")) {
					taxOnTaxInfo.setDescription((String) me.getValue());
				}
			}
			taxOnTaxNewlist.add(taxOnTaxInfo);
		}
		totalTaxOnTaxAmount.setIsoCurrencyCode(feeCurrency);
		totalTaxOnTaxAmount.setAmount(RoundToScale.run(consolidateTaxOnTaxAmt, feeCurrency));
		feeDto.setTotalTaxOnTax(totalTaxOnTaxAmount);
		feeDto.setTaxOnTaxlist(taxOnTaxNewlist);
		return feeDto;
	}
}
