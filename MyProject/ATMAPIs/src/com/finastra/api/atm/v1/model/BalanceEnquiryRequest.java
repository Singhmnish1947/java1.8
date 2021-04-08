package com.finastra.api.atm.v1.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Balance Enquiry request
 */
@ApiModel(description = "Balance Enquiry request")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-09-26T12:19:00.869+05:30")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BalanceEnquiryRequest {
	@JsonProperty("channelId")
	private String channelId = null;

	@JsonProperty("primaryAccountNumberIdentifier")
	private String primaryAccountNumberIdentifier = null;

	@JsonProperty("dateTimeTransmission")
	private Timestamp dateTimeTransmission = null;

	@JsonProperty("amountCurrencyCardholderBillingFee")
	private String amountCurrencyCardholderBillingFee = null;

	@JsonProperty("amountCardholderBillingFee")
	private BigDecimal amountCardholderBillingFee = new BigDecimal(0.00);

	@JsonProperty("systemTraceAuditNumber")
	private String systemTraceAuditNumber = null;

	@JsonProperty("timeLocalTransaction")
	private Timestamp timeLocalTransaction = null;

	@JsonProperty("messageTypeIdentifier")
	private String messageTypeIdentifier = null;

	/**
	 * Value indicating the specific purpose of the message within its message
	 * class.
	 */
	public enum MessageFunctionEnum {
		REQUEST("Request"),

		REPEAT("Repeat");

		private String value;

		MessageFunctionEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static MessageFunctionEnum fromValue(String text) {
			for (MessageFunctionEnum b : MessageFunctionEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}
	}

	@JsonProperty("messageFunction")
	private MessageFunctionEnum messageFunction = null;

	@JsonProperty("acquiringInstitutionIdentificationCode")
	private String acquiringInstitutionIdentificationCode = null;

	@JsonProperty("forwardingInstitutionIdentificationCode")
	private String forwardingInstitutionIdentificationCode = null;

	@JsonProperty("retrievalReferenceNumber")
	private String retrievalReferenceNumber = null;

	@JsonProperty("approvalCode")
	private String approvalCode = null;

	@JsonProperty("actionCode")
	private String actionCode = null;

	@JsonProperty("cardAcceptorTerminalIdentification")
	private String cardAcceptorTerminalIdentification = null;

	@JsonProperty("cardAcceptorIdentificationCode")
	private String cardAcceptorIdentificationCode = null;

	@JsonProperty("cardAcceptorNameLocation")
	private String cardAcceptorNameLocation = null;

	@JsonProperty("additionalDataPrivate")
	private String additionalDataPrivate = null;

	@JsonProperty("dataRecord")
	private String dataRecord = null;

	@JsonProperty("transactionOriginatorInstitutionIdentificationCode")
	private String transactionOriginatorInstitutionIdentificationCode = null;

	@JsonProperty("cardIssuerReferenceNumber")
	private String cardIssuerReferenceNumber = null;

	@JsonProperty("receivingInstitutionIdentificationCode")
	private String receivingInstitutionIdentificationCode = null;

	@JsonProperty("accountIdentification1")
	private String accountIdentification1 = null;

	public BalanceEnquiryRequest channelId(String channelId) {
		this.channelId = channelId;
		return this;
	}

	/**
	 * Channel ID of ATM where the transaction is initiated. For example, POS, ATM.
	 * These channel IDs are configured in Core Banking Solution.
	 * 
	 * @return channelId
	 **/
	@ApiModelProperty(example = "POS", required = true, value = "Channel ID of ATM where the transaction is initiated. For example, POS, ATM. These channel IDs are configured in Core Banking Solution.")
	@NotNull
	

	@Size(min = 1, max = 20)
	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public BalanceEnquiryRequest primaryAccountNumberIdentifier(String primaryAccountNumberIdentifier) {
		this.primaryAccountNumberIdentifier = primaryAccountNumberIdentifier;
		return this;
	}

	/**
	 * This is card identifier that is used to link to card number in Card
	 * Management application or Switch.Card Identifier is used to trace the
	 * transactions in core banking application as well downstream applications such
	 * as Digital Channels, and so on.
	 * 
	 * @return primaryAccountNumberIdentifier
	 **/
	@ApiModelProperty(example = "5522605002030111", required = true, value = "This is card identifier that is used to link to card number in Card Management application or Switch.Card Identifier is used to trace the transactions in core banking application as well downstream applications such as Digital Channels, and so on.")
	
	@Size(max = 19)
	public String getPrimaryAccountNumberIdentifier() {
		return primaryAccountNumberIdentifier;
	}

	public void setPrimaryAccountNumberIdentifier(String primaryAccountNumberIdentifier) {
		this.primaryAccountNumberIdentifier = primaryAccountNumberIdentifier;
	}

	public BalanceEnquiryRequest dateTimeTransmission(Timestamp dateTimeTransmission) {
		this.dateTimeTransmission = dateTimeTransmission;
		return this;
	}

	/**
	 * Indicates the message transmission date and time from the channel. Switch/
	 * Card Management applications need to send this as-is.
	 * 
	 * @return dateTimeTransmission
	 **/
	@ApiModelProperty(value = "Indicates the message transmission date and time from the channel. Switch/ Card Management applications need to send this as-is.")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
	@Valid
	

	public Timestamp getDateTimeTransmission() {
		return dateTimeTransmission;
	}

	public void setDateTimeTransmission(Timestamp dateTimeTransmission) {
		this.dateTimeTransmission = dateTimeTransmission;
	}

	public BalanceEnquiryRequest amountCurrencyCardholderBillingFee(String amountCurrencyCardholderBillingFee) {
		this.amountCurrencyCardholderBillingFee = amountCurrencyCardholderBillingFee;
		return this;
	}

	/**
	 * Indicates the fee currency.Only numeric ISO Code is allowed. For example, 840
	 * (for USD).The Field ID dependent on amountCardholderBillingFee.
	 * 
	 * @return amountCurrencyCardholderBillingFee
	 **/
	@ApiModelProperty(example = "978", value = "Indicates the fee currency.Only numeric ISO Code is allowed. For example, 840 (for USD).The Field ID dependent on amountCardholderBillingFee.")
	
	@Size(min = 1, max = 3)
	public String getAmountCurrencyCardholderBillingFee() {
		return amountCurrencyCardholderBillingFee;
	}

	public void setAmountCurrencyCardholderBillingFee(String amountCurrencyCardholderBillingFee) {
		this.amountCurrencyCardholderBillingFee = amountCurrencyCardholderBillingFee;
	}

	public BalanceEnquiryRequest amountCardholderBillingFee(BigDecimal amountCardholderBillingFee) {
		this.amountCardholderBillingFee = amountCardholderBillingFee;
		return this;
	}

	/**
	 * Indicates the fee amount in amountCurrencyCardholderBillingFee.
	 * 
	 * @return amountCardholderBillingFee
	 **/
	@ApiModelProperty(example = "10.0", value = "Indicates the fee amount in amountCurrencyCardholderBillingFee.")
	@JsonRawValue
	

	public BigDecimal getAmountCardholderBillingFee() {
		return amountCardholderBillingFee;
	}

	public void setAmountCardholderBillingFee(BigDecimal amountCardholderBillingFee) {
		this.amountCardholderBillingFee = amountCardholderBillingFee;
	}

	public BalanceEnquiryRequest systemTraceAuditNumber(String systemTraceAuditNumber) {
		this.systemTraceAuditNumber = systemTraceAuditNumber;
		return this;
	}

	/**
	 * It uniquely identifies a transaction that is assigned the originator of the
	 * transaction. The trace number remains unchanged for all messages within a
	 * two-message exchange, for example, request/ repeat and response. Only numeric
	 * values are allowed in this field.
	 * 
	 * @return systemTraceAuditNumber
	 **/
	@ApiModelProperty(example = "123456", required = true, value = "It uniquely identifies a transaction that is assigned the originator of the transaction. The trace number remains unchanged for all messages within a two-message exchange, for example, request/ repeat and response. Only numeric values are allowed in this field.")
	@NotNull
	

	@Size(min = 1, max = 6)
	public String getSystemTraceAuditNumber() {
		return systemTraceAuditNumber;
	}

	public void setSystemTraceAuditNumber(String systemTraceAuditNumber) {
		this.systemTraceAuditNumber = systemTraceAuditNumber;
	}

	public BalanceEnquiryRequest timeLocalTransaction(Timestamp timeLocalTransaction) {
		this.timeLocalTransaction = timeLocalTransaction;
		return this;
	}

	/**
	 * Indicates the transaction's date and time stamp of the ATM machine. In
	 * reversal, charge back, reconciliation, administrative and network management
	 * transactions, it’s the date and time set by the initiator of the first
	 * message in the transaction. This date and time is used to track all the
	 * subsequent messages.
	 * 
	 * @return timeLocalTransaction
	 **/
	@ApiModelProperty(required = true, value = "Indicates the transaction's date and time stamp of the ATM machine. In reversal, charge back, reconciliation, administrative and network management transactions, it’s the date and time set by the initiator of the first message in the transaction. This date and time is used to track all the subsequent messages.")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
	@NotNull
	
	@Valid

	public Timestamp getTimeLocalTransaction() {
		return timeLocalTransaction;
	}

	public void setTimeLocalTransaction(Timestamp timeLocalTransaction) {
		this.timeLocalTransaction = timeLocalTransaction;
	}

	public BalanceEnquiryRequest messageTypeIdentifier(String messageTypeIdentifier) {
		this.messageTypeIdentifier = messageTypeIdentifier;
		return this;
	}

	/**
	 * The message type identifier is a 4-digit numeric field identifying the
	 * message class, message function and transaction originator. Only numeric
	 * values are allowed. For example, 1100 --> Ministatement request Allowed
	 * values are:- x100, x101
	 * 
	 * @return messageTypeIdentifier
	 **/
	@ApiModelProperty(example = "1100", required = true, value = "The message type identifier is a 4-digit numeric field identifying the message class, message function and transaction originator. Only numeric values are allowed. For example, 1100 --> Ministatement request Allowed values are:- x100, x101")
	@NotNull
	
	@Size(min = 1, max = 4)
	public String getMessageTypeIdentifier() {
		return messageTypeIdentifier;
	}

	public void setMessageTypeIdentifier(String messageTypeIdentifier) {
		this.messageTypeIdentifier = messageTypeIdentifier;
	}

	public BalanceEnquiryRequest messageFunction(MessageFunctionEnum messageFunction) {
		this.messageFunction = messageFunction;
		return this;
	}

	/**
	 * Value indicating the specific purpose of the message within its message
	 * class.
	 * 
	 * @return messageFunction
	 **/
	@ApiModelProperty(example = "Request", required = true, value = "Value indicating the specific purpose of the message within its message class.")
	@NotNull
	

	public MessageFunctionEnum getMessageFunction() {
		return messageFunction;
	}

	public void setMessageFunction(MessageFunctionEnum messageFunction) {
		this.messageFunction = messageFunction;
	}

	public BalanceEnquiryRequest acquiringInstitutionIdentificationCode(String acquiringInstitutionIdentificationCode) {
		this.acquiringInstitutionIdentificationCode = acquiringInstitutionIdentificationCode;
		return this;
	}

	/**
	 * Code identifying the acquirer.This is the IMD code assigned to transaction
	 * initiating terminals. This information is sent from the transaction
	 * initiating terminals, to be sent in request unchanged.
	 * 
	 * @return acquiringInstitutionIdentificationCode
	 **/
	@ApiModelProperty(example = "11000589394", required = true, value = "Code identifying the acquirer.This is the IMD code assigned to transaction initiating terminals. This information is sent from the transaction initiating terminals, to be sent in request unchanged.")
	@NotNull
	

	@Size(min = 1, max = 11)
	public String getAcquiringInstitutionIdentificationCode() {
		return acquiringInstitutionIdentificationCode;
	}

	public void setAcquiringInstitutionIdentificationCode(String acquiringInstitutionIdentificationCode) {
		this.acquiringInstitutionIdentificationCode = acquiringInstitutionIdentificationCode;
	}

	public BalanceEnquiryRequest forwardingInstitutionIdentificationCode(
			String forwardingInstitutionIdentificationCode) {
		this.forwardingInstitutionIdentificationCode = forwardingInstitutionIdentificationCode;
		return this;
	}

	/**
	 * Indicates the forwarding institution code.This is the IMD code assigned to
	 * transaction initiating terminals. This information is sent from the
	 * transaction initiating terminals, to be sent in request unchanged.
	 * 
	 * @return forwardingInstitutionIdentificationCode
	 **/
	@ApiModelProperty(example = "11000589394", value = "Indicates the forwarding institution code.This is the IMD code assigned to transaction initiating terminals. This information is sent from the transaction initiating terminals, to be sent in request unchanged.")
	
	@Size(max = 11)
	public String getForwardingInstitutionIdentificationCode() {
		return forwardingInstitutionIdentificationCode;
	}

	public void setForwardingInstitutionIdentificationCode(String forwardingInstitutionIdentificationCode) {
		this.forwardingInstitutionIdentificationCode = forwardingInstitutionIdentificationCode;
	}

	public BalanceEnquiryRequest retrievalReferenceNumber(String retrievalReferenceNumber) {
		this.retrievalReferenceNumber = retrievalReferenceNumber;
		return this;
	}

	/**
	 * A reference supplied by the system retaining the original source information
	 * and that is used to assist in locating information or a copy thereof.The
	 * reference is used to check duplicate or to match the original messages.
	 * 
	 * @return retrievalReferenceNumber
	 **/
	@ApiModelProperty(example = "614001102092", required = true, value = "A reference supplied by the system retaining the original source information and that is used to assist in locating information or a copy thereof.The reference is used to check duplicate or to match the original messages.")
	@NotNull
	
	@Size(min = 1, max = 40)
	public String getRetrievalReferenceNumber() {
		return retrievalReferenceNumber;
	}

	public void setRetrievalReferenceNumber(String retrievalReferenceNumber) {
		this.retrievalReferenceNumber = retrievalReferenceNumber;
	}

	public BalanceEnquiryRequest approvalCode(String approvalCode) {
		this.approvalCode = approvalCode;
		return this;
	}

	/**
	 * Code assigned by the authorizing institution indicating approval.
	 * 
	 * @return approvalCode
	 **/
	@ApiModelProperty(example = "65533", value = "Code assigned by the authorizing institution indicating approval.")
	
	@Size(max = 6)
	public String getApprovalCode() {
		return approvalCode;
	}

	public void setApprovalCode(String approvalCode) {
		this.approvalCode = approvalCode;
	}

	public BalanceEnquiryRequest actionCode(String actionCode) {
		this.actionCode = actionCode;
		return this;
	}

	/**
	 * A code which defines the action taken or to be taken as well as the reason
	 * for taking this action. Only numeric values are allowed. For example, 000 for
	 * approves status
	 * 
	 * @return actionCode
	 **/
	@ApiModelProperty(example = "0", value = "A code which defines the action taken or to be taken as well as the reason for taking this action. Only numeric values are allowed. For example, 000 for approves status")
	
	@Size(max = 3)
	public String getActionCode() {
		return actionCode;
	}

	public void setActionCode(String actionCode) {
		this.actionCode = actionCode;
	}

	public BalanceEnquiryRequest cardAcceptorTerminalIdentification(String cardAcceptorTerminalIdentification) {
		this.cardAcceptorTerminalIdentification = cardAcceptorTerminalIdentification;
		return this;
	}

	/**
	 * Unique code identifying a terminal at the card acceptor location. ATM machine
	 * ID.
	 * 
	 * @return cardAcceptorTerminalIdentification
	 **/
	@ApiModelProperty(example = "7", required = true, value = "Unique code identifying a terminal at the card acceptor location. ATM machine ID.")
	@NotNull
	
	@Size(min = 1, max = 8)
	public String getCardAcceptorTerminalIdentification() {
		return cardAcceptorTerminalIdentification;
	}

	public void setCardAcceptorTerminalIdentification(String cardAcceptorTerminalIdentification) {
		this.cardAcceptorTerminalIdentification = cardAcceptorTerminalIdentification;
	}

	public BalanceEnquiryRequest cardAcceptorIdentificationCode(String cardAcceptorIdentificationCode) {
		this.cardAcceptorIdentificationCode = cardAcceptorIdentificationCode;
		return this;
	}

	/**
	 * Code identifying the card acceptor. IMD code of acquirer Bank.This is the IMD
	 * code assigned to transaction initiating terminals. This information is sent
	 * from the transaction initiating terminals, to be sent in request unchanged.
	 * 
	 * @return cardAcceptorIdentificationCode
	 **/
	@ApiModelProperty(example = "11000589394", value = "Code identifying the card acceptor. IMD code of acquirer Bank.This is the IMD code assigned to transaction initiating terminals. This information is sent from the transaction initiating terminals, to be sent in request unchanged.")
	
	@Size(max = 15)
	public String getCardAcceptorIdentificationCode() {
		return cardAcceptorIdentificationCode;
	}

	public void setCardAcceptorIdentificationCode(String cardAcceptorIdentificationCode) {
		this.cardAcceptorIdentificationCode = cardAcceptorIdentificationCode;
	}

	public BalanceEnquiryRequest cardAcceptorNameLocation(String cardAcceptorNameLocation) {
		this.cardAcceptorNameLocation = cardAcceptorNameLocation;
		return this;
	}

	/**
	 * Card acceptor name/ location (1-23 address 24-36 city 37-38 state 39-40
	 * country). The name and location of the card acceptor as known to the
	 * cardholder.
	 * 
	 * @return cardAcceptorNameLocation
	 **/
	@ApiModelProperty(example = "ABCBank", value = "Card acceptor name/ location (1-23 address 24-36 city 37-38 state 39-40 country). The name and location of the card acceptor as known to the cardholder.")
	
	@Size(max = 99)
	public String getCardAcceptorNameLocation() {
		return cardAcceptorNameLocation;
	}

	public void setCardAcceptorNameLocation(String cardAcceptorNameLocation) {
		this.cardAcceptorNameLocation = cardAcceptorNameLocation;
	}

	public BalanceEnquiryRequest additionalDataPrivate(String additionalDataPrivate) {
		this.additionalDataPrivate = additionalDataPrivate;
		return this;
	}

	/**
	 * Reserved for private data. The use of this data element is determined by
	 * bilateral agreement.
	 * 
	 * @return additionalDataPrivate
	 **/
	@ApiModelProperty(required = true, value = "Reserved for private data. The use of this data element is determined by bilateral agreement.")
	@NotNull
	
	@Size(min = 1, max = 999)
	public String getAdditionalDataPrivate() {
		return additionalDataPrivate;
	}

	public void setAdditionalDataPrivate(String additionalDataPrivate) {
		this.additionalDataPrivate = additionalDataPrivate;
	}

	public BalanceEnquiryRequest dataRecord(String dataRecord) {
		this.dataRecord = dataRecord;
		return this;
	}

	/**
	 * Other data required to be passed to support an administrative or file action
	 * message.
	 * 
	 * @return dataRecord
	 **/
	@ApiModelProperty(required = true, value = "Other data required to be passed to support an administrative or file action message.")
	@NotNull
	
	@Size(min = 1, max = 999)
	public String getDataRecord() {
		return dataRecord;
	}

	public void setDataRecord(String dataRecord) {
		this.dataRecord = dataRecord;
	}

	public BalanceEnquiryRequest transactionOriginatorInstitutionIdentificationCode(
			String transactionOriginatorInstitutionIdentificationCode) {
		this.transactionOriginatorInstitutionIdentificationCode = transactionOriginatorInstitutionIdentificationCode;
		return this;
	}

	/**
	 * Code identifying the institution that is the transaction originator. This is
	 * the IMD code assigned to transaction initiating terminals. This information
	 * is sent from the transaction initiating terminals, to be sent in request
	 * unchanged.
	 * 
	 * @return transactionOriginatorInstitutionIdentificationCode
	 **/
	@ApiModelProperty(example = "11000589394", value = "Code identifying the institution that is the transaction originator. This is the IMD code assigned to transaction initiating terminals. This information is sent from the transaction initiating terminals, to be sent in request unchanged.")
	
	@Size(max = 11)
	public String getTransactionOriginatorInstitutionIdentificationCode() {
		return transactionOriginatorInstitutionIdentificationCode;
	}

	public void setTransactionOriginatorInstitutionIdentificationCode(
			String transactionOriginatorInstitutionIdentificationCode) {
		this.transactionOriginatorInstitutionIdentificationCode = transactionOriginatorInstitutionIdentificationCode;
	}

	public BalanceEnquiryRequest cardIssuerReferenceNumber(String cardIssuerReferenceNumber) {
		this.cardIssuerReferenceNumber = cardIssuerReferenceNumber;
		return this;
	}

	/**
	 * Data supplied by a card issuer in an authorization response message,
	 * financial resentment response message, or in a charge back transaction that
	 * the acquirer may be required to be provided in subsequent transactions.
	 * 
	 * @return cardIssuerReferenceNumber
	 **/
	@ApiModelProperty(example = "209089", value = "Data supplied by a card issuer in an authorization response message, financial resentment response message, or in a charge back transaction that the acquirer may be  required to be provided in subsequent transactions.")
	
	@Size(max = 99)
	public String getCardIssuerReferenceNumber() {
		return cardIssuerReferenceNumber;
	}

	public void setCardIssuerReferenceNumber(String cardIssuerReferenceNumber) {
		this.cardIssuerReferenceNumber = cardIssuerReferenceNumber;
	}

	public BalanceEnquiryRequest receivingInstitutionIdentificationCode(String receivingInstitutionIdentificationCode) {
		this.receivingInstitutionIdentificationCode = receivingInstitutionIdentificationCode;
		return this;
	}

	/**
	 * Code identifying the receiving institution.The cardholder bank institution
	 * code. This is the IMD code assigned to transaction initiating terminals. This
	 * information is sent from the transaction initiating terminals, to be sent in
	 * request unchanged.
	 * 
	 * @return receivingInstitutionIdentificationCode
	 **/
	@ApiModelProperty(example = "11000589394", required = true, value = "Code identifying the receiving institution.The cardholder bank  institution code. This is the IMD code assigned to transaction initiating terminals. This information is sent from the transaction initiating terminals, to be sent in request unchanged.")
	@NotNull
	
	@Size(min = 1, max = 11)
	public String getReceivingInstitutionIdentificationCode() {
		return receivingInstitutionIdentificationCode;
	}

	public void setReceivingInstitutionIdentificationCode(String receivingInstitutionIdentificationCode) {
		this.receivingInstitutionIdentificationCode = receivingInstitutionIdentificationCode;
	}

	public BalanceEnquiryRequest accountIdentification1(String accountIdentification1) {
		this.accountIdentification1 = accountIdentification1;
		return this;
	}

	/**
	 * Account identification 1, Balance inquiry account.
	 * 
	 * @return accountIdentification1
	 **/
	@ApiModelProperty(example = "123456788", required = true, value = "Account identification 1, Balance inquiry account.")
	@NotNull
	
	@Size(min = 1, max = 28)
	public String getAccountIdentification1() {
		return accountIdentification1;
	}

	public void setAccountIdentification1(String accountIdentification1) {
		this.accountIdentification1 = accountIdentification1;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		BalanceEnquiryRequest balanceEnquiryRequest = (BalanceEnquiryRequest) o;
		return Objects.equals(this.channelId, balanceEnquiryRequest.channelId)
				&& Objects.equals(this.primaryAccountNumberIdentifier,
						balanceEnquiryRequest.primaryAccountNumberIdentifier)
				&& Objects.equals(this.dateTimeTransmission, balanceEnquiryRequest.dateTimeTransmission)
				&& Objects.equals(this.amountCurrencyCardholderBillingFee,
						balanceEnquiryRequest.amountCurrencyCardholderBillingFee)
				&& Objects.equals(this.amountCardholderBillingFee, balanceEnquiryRequest.amountCardholderBillingFee)
				&& Objects.equals(this.systemTraceAuditNumber, balanceEnquiryRequest.systemTraceAuditNumber)
				&& Objects.equals(this.timeLocalTransaction, balanceEnquiryRequest.timeLocalTransaction)
				&& Objects.equals(this.messageTypeIdentifier, balanceEnquiryRequest.messageTypeIdentifier)
				&& Objects.equals(this.messageFunction, balanceEnquiryRequest.messageFunction)
				&& Objects.equals(this.acquiringInstitutionIdentificationCode,
						balanceEnquiryRequest.acquiringInstitutionIdentificationCode)
				&& Objects.equals(this.forwardingInstitutionIdentificationCode,
						balanceEnquiryRequest.forwardingInstitutionIdentificationCode)
				&& Objects.equals(this.retrievalReferenceNumber, balanceEnquiryRequest.retrievalReferenceNumber)
				&& Objects.equals(this.approvalCode, balanceEnquiryRequest.approvalCode)
				&& Objects.equals(this.actionCode, balanceEnquiryRequest.actionCode)
				&& Objects.equals(this.cardAcceptorTerminalIdentification,
						balanceEnquiryRequest.cardAcceptorTerminalIdentification)
				&& Objects.equals(this.cardAcceptorIdentificationCode,
						balanceEnquiryRequest.cardAcceptorIdentificationCode)
				&& Objects.equals(this.cardAcceptorNameLocation, balanceEnquiryRequest.cardAcceptorNameLocation)
				&& Objects.equals(this.additionalDataPrivate, balanceEnquiryRequest.additionalDataPrivate)
				&& Objects.equals(this.dataRecord, balanceEnquiryRequest.dataRecord)
				&& Objects.equals(this.transactionOriginatorInstitutionIdentificationCode,
						balanceEnquiryRequest.transactionOriginatorInstitutionIdentificationCode)
				&& Objects.equals(this.cardIssuerReferenceNumber, balanceEnquiryRequest.cardIssuerReferenceNumber)
				&& Objects.equals(this.receivingInstitutionIdentificationCode,
						balanceEnquiryRequest.receivingInstitutionIdentificationCode)
				&& Objects.equals(this.accountIdentification1, balanceEnquiryRequest.accountIdentification1);
	}

	@Override
	public int hashCode() {
		return Objects.hash(channelId, primaryAccountNumberIdentifier, dateTimeTransmission,
				amountCurrencyCardholderBillingFee, amountCardholderBillingFee, systemTraceAuditNumber,
				timeLocalTransaction, messageTypeIdentifier, messageFunction, acquiringInstitutionIdentificationCode,
				forwardingInstitutionIdentificationCode, retrievalReferenceNumber, approvalCode, actionCode,
				cardAcceptorTerminalIdentification, cardAcceptorIdentificationCode, cardAcceptorNameLocation,
				additionalDataPrivate, dataRecord, transactionOriginatorInstitutionIdentificationCode,
				cardIssuerReferenceNumber, receivingInstitutionIdentificationCode, accountIdentification1);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class BalanceEnquiryRequest {\n");

		sb.append("    channelId: ").append(toIndentedString(channelId)).append("\n");
		sb.append("    primaryAccountNumberIdentifier: ").append(toIndentedString(primaryAccountNumberIdentifier))
				.append("\n");
		sb.append("    dateTimeTransmission: ").append(toIndentedString(dateTimeTransmission)).append("\n");
		sb.append("    amountCurrencyCardholderBillingFee: ")
				.append(toIndentedString(amountCurrencyCardholderBillingFee)).append("\n");
		sb.append("    amountCardholderBillingFee: ").append(toIndentedString(amountCardholderBillingFee)).append("\n");
		sb.append("    systemTraceAuditNumber: ").append(toIndentedString(systemTraceAuditNumber)).append("\n");
		sb.append("    timeLocalTransaction: ").append(toIndentedString(timeLocalTransaction)).append("\n");
		sb.append("    messageTypeIdentifier: ").append(toIndentedString(messageTypeIdentifier)).append("\n");
		sb.append("    messageFunction: ").append(toIndentedString(messageFunction)).append("\n");
		sb.append("    acquiringInstitutionIdentificationCode: ")
				.append(toIndentedString(acquiringInstitutionIdentificationCode)).append("\n");
		sb.append("    forwardingInstitutionIdentificationCode: ")
				.append(toIndentedString(forwardingInstitutionIdentificationCode)).append("\n");
		sb.append("    retrievalReferenceNumber: ").append(toIndentedString(retrievalReferenceNumber)).append("\n");
		sb.append("    approvalCode: ").append(toIndentedString(approvalCode)).append("\n");
		sb.append("    actionCode: ").append(toIndentedString(actionCode)).append("\n");
		sb.append("    cardAcceptorTerminalIdentification: ")
				.append(toIndentedString(cardAcceptorTerminalIdentification)).append("\n");
		sb.append("    cardAcceptorIdentificationCode: ").append(toIndentedString(cardAcceptorIdentificationCode))
				.append("\n");
		sb.append("    cardAcceptorNameLocation: ").append(toIndentedString(cardAcceptorNameLocation)).append("\n");
		sb.append("    additionalDataPrivate: ").append(toIndentedString(additionalDataPrivate)).append("\n");
		sb.append("    dataRecord: ").append(toIndentedString(dataRecord)).append("\n");
		sb.append("    transactionOriginatorInstitutionIdentificationCode: ")
				.append(toIndentedString(transactionOriginatorInstitutionIdentificationCode)).append("\n");
		sb.append("    cardIssuerReferenceNumber: ").append(toIndentedString(cardIssuerReferenceNumber)).append("\n");
		sb.append("    receivingInstitutionIdentificationCode: ")
				.append(toIndentedString(receivingInstitutionIdentificationCode)).append("\n");
		sb.append("    accountIdentification1: ").append(toIndentedString(accountIdentification1)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}
