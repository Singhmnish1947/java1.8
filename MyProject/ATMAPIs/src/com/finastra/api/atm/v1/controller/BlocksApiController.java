package com.finastra.api.atm.v1.controller;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finastra.api.atm.v1.constant.ATMAPIConstant;
import com.finastra.api.atm.v1.mapping.ATMCashWithdrawalMapping;
import com.finastra.api.atm.v1.model.AccountBlockResponse;
import com.finastra.api.atm.v1.service.IATMApiService;
import com.finastra.api.utils.ATMTransactionUtil;
import com.finastra.atm.helper.ATMCashWithdrawalHelper;
import com.misys.fbp.common.event.FBPErrorResponseHandler;
import com.misys.fbp.common.util.FBPServiceAppContext;
import com.misys.fbp.events.model.Error;

import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2021-01-06T09:31:53.977Z")

@RestController
public class BlocksApiController implements BlocksApi {

	private static final Logger log = LoggerFactory.getLogger(BlocksApiController.class);

	private final ObjectMapper objectMapper;

	private final HttpServletRequest request;

	IATMApiService atmApiService;

	@org.springframework.beans.factory.annotation.Autowired
	public BlocksApiController(ObjectMapper objectMapper, HttpServletRequest request) {
		this.objectMapper = objectMapper;
		this.request = request;
	}

	public ResponseEntity searchBlocksbyAccountId(
			@ApiParam(value = "A unique ID of the request, unique to the call, as determined by the initiating party. A UUID must be set in this header to uniquely identify the request", required = true) @RequestHeader(value = "X-Request-ID", required = true) UUID xRequestID,
			@Size(max = 20) @ApiParam(value = "Specific Account Number to search for existing blocked Transaction", required = true) @PathVariable("accountId") String accountId,
			@ApiParam(value = "The maximum number of rows that may be returned. This parameter can be thought of as the page size", defaultValue = "10") @Valid @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit,
			@ApiParam(value = "Set to offset the results to a particular row count  Example: GET /accountId?offset=100 will return the ATM/POS Block details of 101 and more", defaultValue = "0") @Valid @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset) {

		atmApiService = (IATMApiService) FBPServiceAppContext.getInstance().getApplicationContext()
				.getBean(ATMAPIConstant.ATM_API_SERVICE);
		ATMCashWithdrawalMapping mapping = new ATMCashWithdrawalMapping();
		ResponseEntity<Object> rsEntity = null;
		String UPDATE_ERROR_STATUS = "update_ERRORSTATUS";
		String UPDATE_ERROR_DESCRIPTION = "update_ERRORDESCRIPTION";
		AccountBlockResponse accountBlockResponse = new AccountBlockResponse();

		try {
			accountBlockResponse = atmApiService.getAtmPosBlockedTransaction(accountId, limit, offset);
			rsEntity = new ResponseEntity<Object>(accountBlockResponse, HttpStatus.OK);

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

			log.error("==================" + "   ErrorCode :  " + errorCode + " ErrorDescription :  "
					+ errorCodeDescription + "================");
			com.finastra.api.atm.v1.model.Error cashWithdrawalErrorResponse = mapping.prepareFailureResponse(errorCode,
					errorCodeDescription);
			rsEntity = new ResponseEntity<Object>(cashWithdrawalErrorResponse, HttpStatus.BAD_REQUEST);
		}
		return rsEntity;
	}

}
