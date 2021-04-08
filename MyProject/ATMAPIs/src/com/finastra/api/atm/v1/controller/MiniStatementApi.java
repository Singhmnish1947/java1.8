/**
 * NOTE: This class is auto generated by the swagger code generator program (2.4.1).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package com.finastra.api.atm.v1.controller;

import java.util.UUID;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.finastra.api.atm.v1.model.Error;
import com.finastra.api.atm.v1.model.MiniStatementRequest;
import com.finastra.api.atm.v1.model.MiniStatementResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-09-26T12:19:00.869+05:30")

@Api(value = "miniStatement", description = "the miniStatement API")
public interface MiniStatementApi {

	@ApiOperation(value = "atmMiniStatement", nickname = "atmMiniStatementPost", notes = "__Background__  Customer is using the ATM/Debit card at ATM machines to get mini statement with last 10 or 5 transactions.   While customer is requesting, in the background ATM machine will be communicating with cardholder bank via SWITCH, Card networks and Forwarding institution (if required).  The API is a way achieve the this integration from external network (ATM SWITCH/Card Management application) to core banking solution.  __Solution Summary & Approach__  Core Banking Solution will develop and expose a new API to enable SWITCHES/Card management applications to send the mini statement requests.   By using this API, SWITCHES can request following * __Request__-> Mini statement request * __Repeat Request__-> Mini statement repeat request for times out transactions  As a result of processing this API, Core Banking Solution will:Validate the account for available balance, statuses etc.., if all the validations are successful, in response the mini statement is sent.   __Assumptions__  Based on the configuration in Core Banking Solution, either last 5 or 10 transactions are sent in the mini statement.", response = MiniStatementResponse.class, authorizations = {
			@Authorization(value = "Oauth2", scopes = {
 
			}) }, tags = { "ATM Mini Statement", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Mini Statement Request Successful", response = MiniStatementResponse.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Error.class),
			@ApiResponse(code = 401, message = "Access forbidden, invalid Authorization was used", response = Error.class),
			@ApiResponse(code = 403, message = "Forbidden", response = Error.class),
			@ApiResponse(code = 404, message = "Not Found", response = Error.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = Error.class) })
	@RequestMapping(value = "atm/v1/miniStatement", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	ResponseEntity<Object> atmMiniStatementPost(
			@ApiParam(value = "Format UUID - ID of the request, unique to the call, as determined by the initiating party", required = true) @RequestHeader(value = "X-Request-ID", required = true) UUID xRequestID,
			@ApiParam(value = "Mini Statement Api", required = true) @Valid @RequestBody MiniStatementRequest miniStatementRequest);

}
