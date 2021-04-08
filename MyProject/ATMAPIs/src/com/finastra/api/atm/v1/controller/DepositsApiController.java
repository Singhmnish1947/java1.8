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
import com.finastra.api.atm.v1.mapping.ATMCashDepositMapping;
import com.finastra.api.atm.v1.model.CashDepositRequest;
import com.finastra.api.atm.v1.service.IATMApiService;
import com.finastra.api.utils.ATMTransactionUtil;
import com.finastra.atm.helper.ATMCashDepositHelper;
import com.misys.fbp.common.event.FBPErrorResponseHandler;
import com.misys.fbp.common.util.FBPServiceAppContext;
import com.misys.fbp.events.model.Error;

import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-09-26T12:19:00.869+05:30")

@RestController
public class DepositsApiController implements DepositsApi {

	private static final Logger logger = LoggerFactory.getLogger(DepositsApiController.class);

	private final ObjectMapper objectMapper;

	private final HttpServletRequest request;

	IATMApiService atmApiService;

	@org.springframework.beans.factory.annotation.Autowired
	public DepositsApiController(ObjectMapper objectMapper, HttpServletRequest request) {
		this.objectMapper = objectMapper;
		this.request = request;
	}

	public ResponseEntity<Object> atmCashDepositPost(
			@ApiParam(value = "Format UUID - ID of the request, unique to the call, as determined by the initiating party", required = true) @RequestHeader(value = "X-Request-ID", required = true) UUID xRequestID,
			@ApiParam(value = "Cash Deposit Api", required = true) @Valid @RequestBody CashDepositRequest cashDepositRequest) {
		atmApiService = (IATMApiService) FBPServiceAppContext.getInstance().getApplicationContext()
				.getBean(ATMAPIConstant.ATM_API_SERVICE);
		ATMCashDepositMapping mapping = new ATMCashDepositMapping();
		ResponseEntity<Object> rsEntity = null;
		String UPDATE_ERROR_STATUS = "update_ERRORSTATUS";
		String UPDATE_ERROR_DESCRIPTION = "update_ERRORDESCRIPTION";
		String transactionReference = cashDepositRequest.getRetrievalReferenceNumber();
		Object cashDepositResponse;
		HashMap<String, Object> essenceRs = null;

		ATMCashDepositHelper helper = new ATMCashDepositHelper();

		try {
			essenceRs = atmApiService.processCashDeposit(cashDepositRequest);

			if (!essenceRs.get(UPDATE_ERROR_STATUS).equals("0")) {
				helper.updateATMActivity(cashDepositRequest, String.valueOf(essenceRs.get(UPDATE_ERROR_STATUS)),
						String.valueOf(essenceRs.get(UPDATE_ERROR_DESCRIPTION)));
				cashDepositResponse = ATMTransactionUtil.createBusinessErrorResponse(
						String.valueOf(essenceRs.get(UPDATE_ERROR_STATUS)),
						String.valueOf(essenceRs.get(UPDATE_ERROR_DESCRIPTION)));

				logger.error(
						"==================" + " Transaction Reference : " + transactionReference + "   ErrorCode : "
								+ String.valueOf(essenceRs.get(UPDATE_ERROR_STATUS)) + "  ErrorDescription : "
								+ String.valueOf(essenceRs.get(UPDATE_ERROR_DESCRIPTION)) + "==================");

				rsEntity = new ResponseEntity<Object>(cashDepositResponse, HttpStatus.BAD_REQUEST);
			} else {
				helper.updateATMActivity(essenceRs);
				cashDepositResponse = mapping.prepareSuccessResponse(cashDepositRequest, essenceRs);
				rsEntity = new ResponseEntity<Object>(cashDepositResponse, HttpStatus.OK);

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

			ATMCashDepositHelper atmHelper = new ATMCashDepositHelper();
			atmHelper.updateATMActivity(cashDepositRequest, errorCode, errorCodeDescription);
			com.finastra.api.atm.v1.model.Error cashDepositErrorResponse = mapping.prepareFailureResponse(errorCode, errorCodeDescription);
			rsEntity = new ResponseEntity<Object>(cashDepositErrorResponse, HttpStatus.BAD_REQUEST);
		}
		return rsEntity;
	}

}
