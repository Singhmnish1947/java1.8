package com.finastra.api.atm.v1.controller;

import java.util.HashMap;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finastra.api.atm.v1.constant.ATMAPIConstant;
import com.finastra.api.atm.v1.mapping.POSTransactionMapping;
import com.finastra.api.atm.v1.model.PosRequest;
import com.finastra.api.atm.v1.service.IATMApiService;
import com.finastra.api.utils.ATMTransactionUtil;
import com.finastra.atm.helper.POSTransactionHelper;
import com.misys.fbp.common.event.FBPErrorResponseHandler;
import com.misys.fbp.common.util.FBPServiceAppContext;
import com.misys.fbp.events.model.Error;

import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-09-26T12:19:00.869+05:30")

@RestController
public class PosApiController implements PosApi {

	private static final Logger logger = LoggerFactory.getLogger(PosApiController.class);

	private final ObjectMapper objectMapper;

	private final HttpServletRequest request;

	IATMApiService atmApiService;

	@org.springframework.beans.factory.annotation.Autowired
	public PosApiController(ObjectMapper objectMapper, HttpServletRequest request) {
		this.objectMapper = objectMapper;
		this.request = request;
	}

	public ResponseEntity<Object> posTransactionPost(
			@ApiParam(value = "Format UUID - ID of the request, unique to the call, as determined by the initiating party", required = true) @RequestHeader(value = "X-Request-ID", required = true) UUID xRequestID,
			@ApiParam(value = "POS Api", required = true) @Valid @RequestBody PosRequest posRequest) {

		atmApiService = (IATMApiService) FBPServiceAppContext.getInstance().getApplicationContext()
				.getBean(ATMAPIConstant.ATM_API_SERVICE);
		POSTransactionMapping mapping = new POSTransactionMapping();
		ResponseEntity<Object> rsEntity = null;
		String responderCode = "Responder_Code";
		String errorDescription = "errorDescription";
		String transactionReference = posRequest.getRetrievalReferenceNumber();
		Object posResponse;
		HashMap<String, Object> essenceRs = null;
		
		boolean a = true;
		Boolean c = new Boolean(true);

		POSTransactionHelper helper = new POSTransactionHelper();

		try {
			essenceRs = atmApiService.processPosTransaction(posRequest);

			if (!essenceRs.get(responderCode).equals("0")) {
				helper.updateATMActivity(posRequest, String.valueOf(essenceRs.get(responderCode)),
						String.valueOf(essenceRs.get(errorDescription)));
				posResponse = ATMTransactionUtil.createBusinessErrorResponse(
						String.valueOf(essenceRs.get(responderCode)), String.valueOf(essenceRs.get(errorDescription)));

				logger.error("==================" + " Transaction Reference : " + transactionReference
						+ "   ErrorCode : " + String.valueOf(essenceRs.get(responderCode)) + "  ErrorDescription : "
						+ String.valueOf(essenceRs.get(errorDescription)) + "==================");

				rsEntity = new ResponseEntity<Object>(posResponse, HttpStatus.BAD_REQUEST);
			} else {
				helper.updateATMActivity(essenceRs);
				posResponse = mapping.prepareSuccessResponse(posRequest, essenceRs);
				rsEntity = new ResponseEntity<Object>(posResponse, HttpStatus.OK);

				logger.info(
						"================" + " Transaction Reference : " + transactionReference + "================");
			}

		} catch (Exception exception) {

			String badRequestTitle = "The request is invalid and cannot be processed.";
			String badRequestDetail = "Ensure that the request is valid.";

			Error errorResponse = FBPErrorResponseHandler.createErrorResponse(exception, ATMAPIConstant.ERROR_TYPE,
					ATMTransactionUtil.getErrorResponseTitle(), null);
			errorResponse.setTitle(badRequestTitle);
			errorResponse.setDetail(badRequestDetail);
			errorResponse.setStatus(400);
			String errorCode = errorResponse.getCauses().get(0).getCode();
			String errorCodeDescription = errorResponse.getCauses().get(0).getMessage();

			logger.error("==================" + " Transaction Reference : " + transactionReference + "   ErrorCode :  "
					+ errorCode + " ErrorDescription :  " + errorCodeDescription + "================");

			helper.updateATMActivity(posRequest, errorCode, errorCodeDescription);
			com.finastra.api.atm.v1.model.Error posErrorResponse = mapping.prepareFailureResponse(errorCode, errorCodeDescription);
			rsEntity = new ResponseEntity<Object>(posErrorResponse, HttpStatus.BAD_REQUEST);
		}
		return rsEntity;
	}
}
