package com.finastra.api.atm.v1.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
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
 * Cash Deposit Request
 */
@ApiModel(description = "Cash Deposit Request")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-09-26T12:19:00.869+05:30")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CashDepositRequest {
	@JsonProperty("channelId")
	private String channelId = null;

	@JsonProperty("primaryAccountNumberIdentifier")
	private String primaryAccountNumberIdentifier = null;

	@JsonProperty("amountCurrencyTransaction")
	private String amountCurrencyTransaction = null;

	@JsonProperty("amountTransaction")
	private BigDecimal amountTransaction = new BigDecimal(0.00);

	@JsonProperty("amountCurrencySettlement")
	private String amountCurrencySettlement = null;

	@JsonProperty("amountSettlement")
	private BigDecimal amountSettlement = new BigDecimal(0.00);

	@JsonProperty("amountCurrencyCardholderBilling")
	private String amountCurrencyCardholderBilling = null;

	@JsonProperty("amountCardholderBilling")
	private BigDecimal amountCardholderBilling = new BigDecimal(0.00);

	@JsonProperty("dateTimeTransmission")
	private Timestamp dateTimeTransmission = null;

	@JsonProperty("amountCurrencyCardholderBillingFee")
	private String amountCurrencyCardholderBillingFee = null;

	@JsonProperty("amountCardholderBillingFee")
	private BigDecimal amountCardholderBillingFee = new BigDecimal(0.00);

	@JsonProperty("conversionRateSettlement")
	private BigDecimal conversionRateSettlement = new BigDecimal(0.00);

	@JsonProperty("conversionRateCardholderBilling")
	private BigDecimal conversionRateCardholderBilling = new BigDecimal(0.00);

	@JsonProperty("systemTraceAuditNumber")
	private String systemTraceAuditNumber = null;

	@JsonProperty("timeLocalTransaction")
	private Timestamp timeLocalTransaction = null;

	@JsonProperty("dateSettlement")
	private Date dateSettlement = null;

	@JsonProperty("messageTypeIdentifier")
	private String messageTypeIdentifier = null;

	/**
	 * Code indicating the specific purpose of the message within its message class.
	 * Only numeric values are allowed. Example- 200 --> For financial presentment
	 * ,200 example- 202
	 */
	public enum FunctionCodeEnum {
		_200("200"),

		_202("202");

		private String value;

		FunctionCodeEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static FunctionCodeEnum fromValue(String text) {
			for (FunctionCodeEnum b : FunctionCodeEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}
	}

	@JsonProperty("functionCode")
	private FunctionCodeEnum functionCode = null;

	/**
	 * Value indicating the specific purpose of the message within its message
	 * class. example- Online
	 */
	public enum MessageFunctionEnum {
		ONLINE("Online"),

		ADVICE("Advice"),

		REPEAT("Repeat"),

		REPEATADVICE("RepeatAdvice"),

		REPLACEMENT("Replacement"),

		REVERSAL("Reversal"),

		REPEATREVERSAL("RepeatReversal");

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

	@JsonProperty("cardAcceptorBusinessCode")
	private String cardAcceptorBusinessCode = null;

	@JsonProperty("dateReconciliation")
	private Date dateReconciliation = null;

	@JsonProperty("reconciliationIndicator")
	private String reconciliationIndicator = null;

	@JsonProperty("currencyAmountOriginal")
	private String currencyAmountOriginal = null;

	@JsonProperty("amountOriginal")
	private BigDecimal amountOriginal = new BigDecimal(0.00);

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

	@JsonProperty("currencyCodeAmountFee")
	private String currencyCodeAmountFee = null;

	@JsonProperty("valueAmountFee")
	private BigDecimal valueAmountFee = new BigDecimal(0.00);

	@JsonProperty("additionalDataPrivate")
	private String additionalDataPrivate = null;

	@JsonProperty("originalMessageTypeIdentifier")
	private String originalMessageTypeIdentifier = null;

	@JsonProperty("originalSystemTraceAuditNumber")
	private String originalSystemTraceAuditNumber = null;

	@JsonProperty("originalDateAndTimeLocalTransaction")
	private Timestamp originalDateAndTimeLocalTransaction = null;

	@JsonProperty("originalAcquiringInstitutionIdentificationCode")
	private String originalAcquiringInstitutionIdentificationCode = null;

	@JsonProperty("dataRecord")
	private String dataRecord = null;

	@JsonProperty("transactionOriginatorInstitutionIdentificationCode")
	private String transactionOriginatorInstitutionIdentificationCode = null;

	@JsonProperty("cardIssuerReferenceNumber")
	private String cardIssuerReferenceNumber = null;

	@JsonProperty("receivingInstitutionIdentificationCode")
	private String receivingInstitutionIdentificationCode = null;

	@JsonProperty("accountIdentification2")
	private String accountIdentification2 = null;

	public CashDepositRequest channelId(String channelId) {
		this.channelId = channelId;
		return this;
	}

	/**
	 * Channel ID of ATM initiation.e.g., POS, ATM .These channel IDs to be
	 * configured in Core Banking Solution.
	 * 
	 * @return channelId
	 **/
	@ApiModelProperty(example = "POS", required = true, value = "Channel ID of ATM initiation.e.g., POS, ATM .These channel IDs to be configured in Core Banking Solution.")
	@NotNull
	
	@Size(min = 1, max = 20)
	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public CashDepositRequest primaryAccountNumberIdentifier(String primaryAccountNumberIdentifier) {
		this.primaryAccountNumberIdentifier = primaryAccountNumberIdentifier;
		return this;
	}

	/**
	 * Primary account number (PAN) This is card number which is used to deposit
	 * amount in ATM machine. This card should be active at the time of transaction.
	 * Is not saved in the Core Banking Solution.
	 * 
	 * @return primaryAccountNumberIdentifier
	 **/
	@ApiModelProperty(example = "10000000000000000", required = true, value = "Primary account number (PAN) This is card number which is used to deposit amount in ATM machine. This card should be active at the time of transaction. Is not saved in the Core Banking Solution.")

	@Size(max = 19)
	public String getPrimaryAccountNumberIdentifier() {
		return primaryAccountNumberIdentifier;
	}

	public void setPrimaryAccountNumberIdentifier(String primaryAccountNumberIdentifier) {
		this.primaryAccountNumberIdentifier = primaryAccountNumberIdentifier;
	}

	public CashDepositRequest amountCurrencyTransaction(String amountCurrencyTransaction) {
		this.amountCurrencyTransaction = amountCurrencyTransaction;
		return this;
	}

	/**
	 * Currency of the Amount deposited by the cardholder (account owner) in the
	 * local currency of the acquirer* or source location (ATM machine location) of
	 * the transaction, exclusive of Amounts fees. *Acquirer:Is a Bank or Financial
	 * institution who owns the ATM machine Only numeric ISO Code is allowed in this
	 * field.
	 * 
	 * @return amountCurrencyTransaction
	 **/
	@ApiModelProperty(example = "840", required = true, value = "Currency of the Amount deposited by the cardholder (account owner) in the local currency of the acquirer* or source location (ATM machine location) of the transaction, exclusive of Amounts fees. *Acquirer:Is a Bank or Financial institution who owns the ATM machine Only numeric ISO Code is allowed in this field.")
	@NotNull

	@Size(min = 1, max = 3)
	public String getAmountCurrencyTransaction() {
		return amountCurrencyTransaction;
	}

	public void setAmountCurrencyTransaction(String amountCurrencyTransaction) {
		this.amountCurrencyTransaction = amountCurrencyTransaction;
	}

	public CashDepositRequest amountTransaction(BigDecimal amountTransaction) {
		this.amountTransaction = amountTransaction;
		return this;
	}

	/**
	 * Amount deposited by the cardholder (account owner) in the local currency of
	 * the acquirer or source location (ATM machine location) of the transaction,
	 * exclusive of Amounts fees.Acquirer:Is a Bank or Financial institution who
	 * owns the ATM machine. BigDecimal(24,6) indicating 18 digits before and 6
	 * after decimal. minimum: 1
	 * 
	 * @return amountTransaction
	 **/
	@ApiModelProperty(example = "1200.0", required = true, value = "Amount deposited by the cardholder (account owner) in the local currency of the acquirer or source location (ATM machine location) of the transaction, exclusive of Amounts fees.Acquirer:Is a Bank or Financial institution who owns the ATM machine. BigDecimal(24,6) indicating 18 digits before and 6 after decimal.")
	@NotNull
	@JsonRawValue
	
	@DecimalMin("0")
	public BigDecimal getAmountTransaction() {
		return amountTransaction;
	}

	public void setAmountTransaction(BigDecimal amountTransaction) {
		this.amountTransaction = amountTransaction;
	}

	public CashDepositRequest amountCurrencySettlement(String amountCurrencySettlement) {
		this.amountCurrencySettlement = amountCurrencySettlement;
		return this;
	}

	/**
	 * Currency of funds to be transferred between the acquirer and card issuer
	 * equal to the Amount transaction in the reconciliation currency.Only numeric
	 * ISO Code is allowed in this field.BigDecimal(24,6) indicating 18 digits
	 * before and 6 after decimal.
	 * 
	 * @return amountCurrencySettlement
	 **/
	@ApiModelProperty(example = "840", value = "Currency of funds to be transferred between the acquirer and card issuer equal to the Amount transaction in the reconciliation currency.Only numeric ISO Code is allowed in this field.BigDecimal(24,6) indicating 18 digits before and 6 after decimal.")
	
	@Size(max = 3)
	public String getAmountCurrencySettlement() {
		return amountCurrencySettlement;
	}

	public void setAmountCurrencySettlement(String amountCurrencySettlement) {
		this.amountCurrencySettlement = amountCurrencySettlement;
	}

	public CashDepositRequest amountSettlement(BigDecimal amountSettlement) {
		this.amountSettlement = amountSettlement;
		return this;
	}

	/**
	 * Funds to be transferred between the acquirer and card issuer equal to the
	 * Amount transaction in the reconciliation currency.BigDecimal(24,6) indicating
	 * 18 digits before and 6 after decimal.
	 * 
	 * @return amountSettlement
	 **/
	@ApiModelProperty(example = "1200.0", value = "Funds to be transferred between the acquirer and card issuer equal to the Amount transaction in the reconciliation currency.BigDecimal(24,6) indicating 18 digits before and 6 after decimal.")
	@JsonRawValue
	
	public BigDecimal getAmountSettlement() {
		return amountSettlement;
	}

	public void setAmountSettlement(BigDecimal amountSettlement) {
		this.amountSettlement = amountSettlement;
	}

	public CashDepositRequest amountCurrencyCardholderBilling(String amountCurrencyCardholderBilling) {
		this.amountCurrencyCardholderBilling = amountCurrencyCardholderBilling;
		return this;
	}

	/**
	 * Currency of amount billed to the cardholder exclusive of cardholder billing
	 * fees. Only numeric ISO Code is allowed in this field.
	 * 
	 * @return amountCurrencyCardholderBilling
	 **/
	@ApiModelProperty(example = "840", required = true, value = "Currency of amount billed to the cardholder exclusive of cardholder billing fees. Only numeric ISO Code is allowed in this field.")
	@NotNull
	
	@Size(min = 1, max = 3)
	public String getAmountCurrencyCardholderBilling() {
		return amountCurrencyCardholderBilling;
	}

	public void setAmountCurrencyCardholderBilling(String amountCurrencyCardholderBilling) {
		this.amountCurrencyCardholderBilling = amountCurrencyCardholderBilling;
	}

	public CashDepositRequest amountCardholderBilling(BigDecimal amountCardholderBilling) {
		this.amountCardholderBilling = amountCardholderBilling;
		return this;
	}

	/**
	 * Amount billed to the cardholder in the currency of the cardholder account,
	 * exclusive of cardholder billing fees.BigDecimal(24,6) indicating 18 digits
	 * before and 6 after decimal. minimum: 1
	 * 
	 * @return amountCardholderBilling
	 **/
	@ApiModelProperty(example = "1200.0", required = true, value = "Amount billed to the cardholder in the currency of the cardholder account, exclusive of cardholder billing fees.BigDecimal(24,6) indicating 18 digits before and 6 after decimal.")
	@NotNull
	@JsonRawValue
	
	@DecimalMin("0")
	public BigDecimal getAmountCardholderBilling() {
		return amountCardholderBilling;
	}

	public void setAmountCardholderBilling(BigDecimal amountCardholderBilling) {
		this.amountCardholderBilling = amountCardholderBilling;
	}

	public CashDepositRequest dateTimeTransmission(Timestamp dateTimeTransmission) {
		this.dateTimeTransmission = dateTimeTransmission;
		return this;
	}

	/**
	 * Date and time received from the message initiator.SWITCH/ Card Management
	 * applications need to send this unchanged as received by the initiator.
	 * 
	 * @return dateTimeTransmission
	 **/
	@ApiModelProperty(value = "Date and time received from the message initiator.SWITCH/ Card Management applications need to send this unchanged as received by the initiator.")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
	@Valid
	
	public Timestamp getDateTimeTransmission() {
		return dateTimeTransmission;
	}

	public void setDateTimeTransmission(Timestamp dateTimeTransmission) {
		this.dateTimeTransmission = dateTimeTransmission;
	}

	public CashDepositRequest amountCurrencyCardholderBillingFee(String amountCurrencyCardholderBillingFee) {
		this.amountCurrencyCardholderBillingFee = amountCurrencyCardholderBillingFee;
		return this;
	}

	/**
	 * Currency of Fee to be billed to the cardholder by the card issuing
	 * institution.The currency to be provided in numeric ISO Code.
	 * 
	 * @return amountCurrencyCardholderBillingFee
	 **/
	@ApiModelProperty(example = "978", value = "Currency of Fee to be billed to the cardholder by the card issuing institution.The currency to be provided in numeric ISO Code.")
	
	@Size(min = 0, max = 3)
	public String getAmountCurrencyCardholderBillingFee() {
		return amountCurrencyCardholderBillingFee;
	}

	public void setAmountCurrencyCardholderBillingFee(String amountCurrencyCardholderBillingFee) {
		this.amountCurrencyCardholderBillingFee = amountCurrencyCardholderBillingFee;
	}

	public CashDepositRequest amountCardholderBillingFee(BigDecimal amountCardholderBillingFee) {
		this.amountCardholderBillingFee = amountCardholderBillingFee;
		return this;
	}

	/**
	 * Fee to be billed to the cardholder by the card issuing institution in the
	 * same currency as Amount currency cardholder billing. BigDecimal(24,6)
	 * indicating 18 digits before and 6 after decimal.
	 * 
	 * @return amountCardholderBillingFee
	 **/
	@ApiModelProperty(example = "10.0", value = "Fee to be billed to the cardholder by the card issuing institution in the same currency as Amount currency cardholder billing. BigDecimal(24,6) indicating 18 digits before and 6 after decimal.")
	@JsonRawValue
	
	public BigDecimal getAmountCardholderBillingFee() {
		return amountCardholderBillingFee;
	}

	public void setAmountCardholderBillingFee(BigDecimal amountCardholderBillingFee) {
		this.amountCardholderBillingFee = amountCardholderBillingFee;
	}

	public CashDepositRequest conversionRateSettlement(BigDecimal conversionRateSettlement) {
		this.conversionRateSettlement = conversionRateSettlement;
		return this;
	}

	/**
	 * Conversion rate_ settlement:The factor used in the conversion from
	 * transaction to reconciliation amount. The Amount transaction is multiplied by
	 * Conversion rate reconciliation to determine the Amount reconciliation. This
	 * is information field which explains at what rate the transaction amount is
	 * converted to reconciliation currency at the acquirer level. This field is
	 * dependent on AmountSettlement field. BigDecimal(14,6) indicating 18 digits
	 * before and 6 after decimal. minimum: 1
	 * 
	 * @return conversionRateSettlement
	 **/
	@ApiModelProperty(example = "54.55", value = "Conversion rate_ settlement:The factor used in the conversion from transaction to reconciliation amount. The Amount transaction is multiplied by Conversion rate reconciliation to determine the Amount reconciliation. This is information field which explains at what rate the transaction amount is converted to reconciliation currency at the acquirer level. This field is dependent on AmountSettlement field. BigDecimal(14,6) indicating 18 digits before and 6 after decimal.")
	@JsonRawValue
	@DecimalMin("0")
	
	public BigDecimal getConversionRateSettlement() {
		return conversionRateSettlement;
	}

	public void setConversionRateSettlement(BigDecimal conversionRateSettlement) {
		this.conversionRateSettlement = conversionRateSettlement;
	}

	public CashDepositRequest conversionRateCardholderBilling(BigDecimal conversionRateCardholderBilling) {
		this.conversionRateCardholderBilling = conversionRateCardholderBilling;
		return this;
	}

	/**
	 * Conversion rate_ cardholder billing The factor used in the conversion from
	 * transaction to cardholder billing amount. The Amount transaction is
	 * multiplied by Conversion rate cardholder billing to determine Amount
	 * cardholder billing. This is information field which explains at what rate the
	 * transaction amount is converted to cardholder currency at the acquirer level.
	 * BigDecimal(14,6) indicating 18 digits before and 6 after decimal. minimum: 1
	 * 
	 * @return conversionRateCardholderBilling
	 **/
	@ApiModelProperty(example = "60.55", required = true, value = "Conversion rate_ cardholder billing The factor used in the conversion from transaction to cardholder billing amount. The Amount transaction is multiplied by Conversion rate cardholder billing to determine Amount cardholder billing. This is information field which explains at what rate the transaction amount is converted to cardholder currency at the acquirer level. BigDecimal(14,6) indicating 18 digits before and 6 after decimal.")
	@NotNull
	@JsonRawValue
	

	@DecimalMin("0")
	public BigDecimal getConversionRateCardholderBilling() {
		return conversionRateCardholderBilling;
	}

	public void setConversionRateCardholderBilling(BigDecimal conversionRateCardholderBilling) {
		this.conversionRateCardholderBilling = conversionRateCardholderBilling;
	}

	public CashDepositRequest systemTraceAuditNumber(String systemTraceAuditNumber) {
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

	public CashDepositRequest timeLocalTransaction(Timestamp timeLocalTransaction) {
		this.timeLocalTransaction = timeLocalTransaction;
		return this;
	}

	/**
	 * Indicates the transaction's date and time stamp of the ATM machine. In
	 * reversal, charge back, reconciliation, administrative and network management
	 * transactions, it’s the date and time set by the initiator of the first
	 * message in the transaction. This date and time is used to track all the
	 * subsequent messages
	 * 
	 * @return timeLocalTransaction
	 **/
	@ApiModelProperty(required = true, value = "Indicates the transaction's date and time stamp of the ATM machine. In reversal, charge back, reconciliation, administrative and network management transactions, it’s the date and time set by the initiator of the first message in the transaction. This date and time is used to track all the subsequent messages")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
	@NotNull
	
	@Valid

	public Timestamp getTimeLocalTransaction() {
		return timeLocalTransaction;
	}

	public void setTimeLocalTransaction(Timestamp timeLocalTransaction) {
		this.timeLocalTransaction = timeLocalTransaction;
	}

	public CashDepositRequest dateSettlement(Date dateSettlement) {
		this.dateSettlement = dateSettlement;
		return this;
	}

	/**
	 * Indicates the settlements date.
	 * 
	 * @return dateSettlement
	 **/
	@ApiModelProperty(value = "Indicates the settlements date.")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Valid
	

	public Date getDateSettlement() {
		return dateSettlement;
	}

	public void setDateSettlement(Date dateSettlement) {
		this.dateSettlement = dateSettlement;
	}

	public CashDepositRequest messageTypeIdentifier(String messageTypeIdentifier) {
		this.messageTypeIdentifier = messageTypeIdentifier;
		return this;
	}

	/**
	 * The message type identifier is a 4-digit numeric field identifying the
	 * message class,message function and transaction originator. Only numeric
	 * values are allowed. For example, x200 --> Financial Presentment (used in case
	 * of Cash withdrawal) -x200, x201, x220, x221, x420, x421 Note- In Core Banking
	 * Solution, only last 3-digits are used for processing.
	 * 
	 * @return messageTypeIdentifier
	 **/
	@ApiModelProperty(example = "1200", required = true, value = "The message type identifier is a 4-digit numeric field identifying the message class,message function and transaction originator. Only numeric values are allowed. For example, x200 --> Financial Presentment (used in case of Cash withdrawal) -x200, x201, x220, x221, x420, x421 Note- In Core Banking Solution, only last 3-digits are used for processing.")
	@NotNull
	

	@Size(min = 1, max = 4)
	public String getMessageTypeIdentifier() {
		return messageTypeIdentifier;
	}

	public void setMessageTypeIdentifier(String messageTypeIdentifier) {
		this.messageTypeIdentifier = messageTypeIdentifier;
	}

	public CashDepositRequest functionCode(FunctionCodeEnum functionCode) {
		this.functionCode = functionCode;
		return this;
	}

	/**
	 * Code indicating the specific purpose of the message within its message class.
	 * Only numeric values are allowed. Example- 200 --> For financial presentment
	 * ,200 example- 202
	 * 
	 * @return functionCode
	 **/
	@ApiModelProperty(required = true, value = "Code indicating the specific purpose of the message within its message class. Only numeric values are allowed. Example- 200 --> For financial presentment ,200 example- 202")
	@NotNull
	

	public FunctionCodeEnum getFunctionCode() {
		return functionCode;
	}

	public void setFunctionCode(FunctionCodeEnum functionCode) {
		this.functionCode = functionCode;
	}

	public CashDepositRequest messageFunction(MessageFunctionEnum messageFunction) {
		this.messageFunction = messageFunction;
		return this;
	}

	/**
	 * Value indicating the specific purpose of the message within its message
	 * class. example- Online
	 * 
	 * @return messageFunction
	 **/
	@ApiModelProperty(required = true, value = "Value indicating the specific purpose of the message within its message  class. example- Online")
	@NotNull
	

	public MessageFunctionEnum getMessageFunction() {
		return messageFunction;
	}

	public void setMessageFunction(MessageFunctionEnum messageFunction) {
		this.messageFunction = messageFunction;
	}

	public CashDepositRequest cardAcceptorBusinessCode(String cardAcceptorBusinessCode) {
		this.cardAcceptorBusinessCode = cardAcceptorBusinessCode;
		return this;
	}

	/**
	 * Code classifying the type of business being done by the card acceptor for
	 * this transaction. Only numeric values are allowed. For example, 6011 -->
	 * Financial Institutions – Manual Cash Disbursements. These are Merchant
	 * Category codes defined in ISO 18245.Please refer ISO documentation for more
	 * details. This information is sent from the transaction initiating terminals,
	 * to be sent in request unchanged.
	 * 
	 * @return cardAcceptorBusinessCode
	 **/
	@ApiModelProperty(example = "6011", value = "Code classifying the type of business being done by the card acceptor          for this transaction. Only numeric values are allowed. For example, 6011 --> Financial Institutions – Manual Cash Disbursements. These are Merchant Category codes defined in ISO 18245.Please refer ISO documentation for more details. This information is sent from the transaction initiating terminals, to be sent in request unchanged. ")
	
	@Size(max = 4)
	public String getCardAcceptorBusinessCode() {
		return cardAcceptorBusinessCode;
	}

	public void setCardAcceptorBusinessCode(String cardAcceptorBusinessCode) {
		this.cardAcceptorBusinessCode = cardAcceptorBusinessCode;
	}

	public CashDepositRequest dateReconciliation(Date dateReconciliation) {
		this.dateReconciliation = dateReconciliation;
		return this;
	}

	/**
	 * Indicates the reconciliation date (year, month and day) between the acquirer
	 * and the card issuer.
	 * 
	 * @return dateReconciliation
	 **/
	@ApiModelProperty(value = "Indicates the reconciliation date (year, month and day) between the acquirer and the card issuer.")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Valid

	public Date getDateReconciliation() {
		return dateReconciliation;
	}

	public void setDateReconciliation(Date dateReconciliation) {
		this.dateReconciliation = dateReconciliation;
	}

	public CashDepositRequest reconciliationIndicator(String reconciliationIndicator) {
		this.reconciliationIndicator = reconciliationIndicator;
		return this;
	}

	/**
	 * Indicates the reconciliation of time period that is allowed within a
	 * reconciliation date. The value is subject to bilateral agreement.
	 * 
	 * @return reconciliationIndicator
	 **/
	@ApiModelProperty(example = "101", value = "Indicates the reconciliation of time period that is allowed within a reconciliation date. The value is subject to bilateral agreement.")
	
	@Size(max = 3)
	public String getReconciliationIndicator() {
		return reconciliationIndicator;
	}

	public void setReconciliationIndicator(String reconciliationIndicator) {
		this.reconciliationIndicator = reconciliationIndicator;
	}

	public CashDepositRequest currencyAmountOriginal(String currencyAmountOriginal) {
		this.currencyAmountOriginal = currencyAmountOriginal;
		return this;
	}

	/**
	 * Indicates the Original amount currency. Only numeric ISO Code is allowed. For
	 * example, 840 (for USD). The Field ID dependent on amountOriginal.
	 * 
	 * @return currencyAmountOriginal
	 **/
	@ApiModelProperty(example = "840", value = "Indicates the Original amount currency. Only numeric ISO Code is allowed. For example, 840 (for USD). The Field ID dependent on amountOriginal.")
	
	@Size(min = 1, max = 3)
	public String getCurrencyAmountOriginal() {
		return currencyAmountOriginal;
	}

	public void setCurrencyAmountOriginal(String currencyAmountOriginal) {
		this.currencyAmountOriginal = currencyAmountOriginal;
	}

	public CashDepositRequest amountOriginal(BigDecimal amountOriginal) {
		this.amountOriginal = amountOriginal;
		return this;
	}

	/**
	 * BigDecimal(24,6) indicating 18 digits before and 6 after decimal.The original
	 * amount of the transaction. It is dependent on Message Function, Function Code
	 * and Processing Code.
	 * 
	 * @return amountOriginal
	 **/
	@ApiModelProperty(example = "50.0", value = "BigDecimal(24,6) indicating 18 digits before and 6 after decimal.The original amount of the transaction. It is dependent on Message Function, Function Code and Processing Code.")
	@JsonRawValue
	
	public BigDecimal getAmountOriginal() {
		return amountOriginal;
	}

	public void setAmountOriginal(BigDecimal amountOriginal) {
		this.amountOriginal = amountOriginal;
	}

	public CashDepositRequest acquiringInstitutionIdentificationCode(String acquiringInstitutionIdentificationCode) {
		this.acquiringInstitutionIdentificationCode = acquiringInstitutionIdentificationCode;
		return this;
	}

	/**
	 * Code identifying the acquirer. This is the IMD code assigned to transaction
	 * initiating terminals. This information is sent from the transaction
	 * initiating terminals, to be sent in request unchanged.
	 * 
	 * @return acquiringInstitutionIdentificationCode
	 **/
	@ApiModelProperty(example = "11000589394", required = true, value = "Code identifying the acquirer. This is the IMD code assigned to transaction initiating terminals. This information is sent from the transaction initiating terminals, to be sent in request unchanged.")
	@NotNull
	
	@Size(min = 1, max = 11)
	public String getAcquiringInstitutionIdentificationCode() {
		return acquiringInstitutionIdentificationCode;
	}

	public void setAcquiringInstitutionIdentificationCode(String acquiringInstitutionIdentificationCode) {
		this.acquiringInstitutionIdentificationCode = acquiringInstitutionIdentificationCode;
	}

	public CashDepositRequest forwardingInstitutionIdentificationCode(String forwardingInstitutionIdentificationCode) {
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

	public CashDepositRequest retrievalReferenceNumber(String retrievalReferenceNumber) {
		this.retrievalReferenceNumber = retrievalReferenceNumber;
		return this;
	}

	/**
	 * A reference supplied by the system retaining the original source information
	 * and that is used to assist in locating that information or a copy thereof.
	 * The reference is used to check duplicate or to match the original messages.
	 * 
	 * @return retrievalReferenceNumber
	 **/
	@ApiModelProperty(example = "614001102092", required = true, value = "A reference supplied by the system retaining the original source information and that is  used to assist in locating that information or a copy thereof. The reference is used to check duplicate or to match the original messages.")
	@NotNull
	
	@Size(min = 1, max = 40)
	public String getRetrievalReferenceNumber() {
		return retrievalReferenceNumber;
	}

	public void setRetrievalReferenceNumber(String retrievalReferenceNumber) {
		this.retrievalReferenceNumber = retrievalReferenceNumber;
	}

	public CashDepositRequest approvalCode(String approvalCode) {
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

	public CashDepositRequest actionCode(String actionCode) {
		this.actionCode = actionCode;
		return this;
	}

	/**
	 * A code which defines the action taken or to be taken as well as the reason
	 * for taking this action. Only numeric values are allowed. For example, 000 for
	 * approves status.
	 * 
	 * @return actionCode
	 **/
	@ApiModelProperty(example = "0", value = "A code which defines the action taken or to be taken as well as the reason for taking this action. Only numeric values are allowed. For example, 000 for approves status.")
	
	@Size(max = 3)
	public String getActionCode() {
		return actionCode;
	}

	public void setActionCode(String actionCode) {
		this.actionCode = actionCode;
	}

	public CashDepositRequest cardAcceptorTerminalIdentification(String cardAcceptorTerminalIdentification) {
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

	public CashDepositRequest cardAcceptorIdentificationCode(String cardAcceptorIdentificationCode) {
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

	public CashDepositRequest cardAcceptorNameLocation(String cardAcceptorNameLocation) {
		this.cardAcceptorNameLocation = cardAcceptorNameLocation;
		return this;
	}

	/**
	 * Card acceptor name/ location (1-23 address, 24-36 city, 37-38 state, 39-40
	 * country) The name and location of the card acceptor as known to the
	 * cardholder.
	 * 
	 * @return cardAcceptorNameLocation
	 **/
	@ApiModelProperty(example = "ABCBank", value = "Card acceptor name/ location (1-23 address, 24-36 city, 37-38 state, 39-40 country) The name and location of the card acceptor as known to the cardholder.")
	
	@Size(max = 99)
	public String getCardAcceptorNameLocation() {
		return cardAcceptorNameLocation;
	}

	public void setCardAcceptorNameLocation(String cardAcceptorNameLocation) {
		this.cardAcceptorNameLocation = cardAcceptorNameLocation;
	}

	public CashDepositRequest currencyCodeAmountFee(String currencyCodeAmountFee) {
		this.currencyCodeAmountFee = currencyCodeAmountFee;
		return this;
	}

	/**
	 * Fee currency Only numeric ISO Code is allowed. This field is dependent on
	 * valueAmountFee.
	 * 
	 * @return currencyCodeAmountFee
	 **/
	@ApiModelProperty(example = "978", value = "Fee currency Only numeric ISO Code is allowed. This field is dependent on valueAmountFee.")
	
	@Size(min = 0, max = 3)
	public String getCurrencyCodeAmountFee() {
		return currencyCodeAmountFee;
	}

	public void setCurrencyCodeAmountFee(String currencyCodeAmountFee) {
		this.currencyCodeAmountFee = currencyCodeAmountFee;
	}

	public CashDepositRequest valueAmountFee(BigDecimal valueAmountFee) {
		this.valueAmountFee = valueAmountFee;
		return this;
	}

	/**
	 * BigDecimal(24,6) indicating 18 digits before and 6 after decimal. The fee
	 * amount in the indicated currency.
	 * 
	 * @return valueAmountFee
	 **/
	@ApiModelProperty(example = "10.0", value = "BigDecimal(24,6) indicating 18 digits before and 6 after decimal. The fee amount in the indicated currency.")
	@JsonRawValue
	
	public BigDecimal getValueAmountFee() {
		return valueAmountFee;
	}

	public void setValueAmountFee(BigDecimal valueAmountFee) {
		this.valueAmountFee = valueAmountFee;
	}

	public CashDepositRequest additionalDataPrivate(String additionalDataPrivate) {
		this.additionalDataPrivate = additionalDataPrivate;
		return this;
	}

	/**
	 * Reserved for private data. The use of this data element is determined by
	 * bilateral agreement.
	 * 
	 * @return additionalDataPrivate
	 **/
	@ApiModelProperty(example = "ATM cash deposit request", required = true, value = "Reserved for private data. The use of this data element is determined  by bilateral agreement.")
	@NotNull
	
	@Size(min = 1, max = 999)
	public String getAdditionalDataPrivate() {
		return additionalDataPrivate;
	}

	public void setAdditionalDataPrivate(String additionalDataPrivate) {
		this.additionalDataPrivate = additionalDataPrivate;
	}

	public CashDepositRequest originalMessageTypeIdentifier(String originalMessageTypeIdentifier) {
		this.originalMessageTypeIdentifier = originalMessageTypeIdentifier;
		return this;
	}

	/**
	 * The message type identifier of the original transaction. Only numeric values
	 * are allowed. Required for messages with:-Repeat, RepeatAdvice, Reversal,
	 * RepeatReversal.
	 * 
	 * @return originalMessageTypeIdentifier
	 **/
	@ApiModelProperty(example = "1200", value = "The message type identifier of the original transaction. Only numeric values are allowed. Required for messages with:-Repeat, RepeatAdvice, Reversal, RepeatReversal.")
	
	@Size(min = 0, max = 4)
	public String getOriginalMessageTypeIdentifier() {
		return originalMessageTypeIdentifier;
	}

	public void setOriginalMessageTypeIdentifier(String originalMessageTypeIdentifier) {
		this.originalMessageTypeIdentifier = originalMessageTypeIdentifier;
	}

	public CashDepositRequest originalSystemTraceAuditNumber(String originalSystemTraceAuditNumber) {
		this.originalSystemTraceAuditNumber = originalSystemTraceAuditNumber;
		return this;
	}

	/**
	 * The system trace audit number of the original transaction. Contained in
	 * Original data elements. Only numeric values are allowed. Required for
	 * messages with:- Repeat, RepeatAdvice, Reversal, RepeatReversal
	 * 
	 * @return originalSystemTraceAuditNumber
	 **/
	@ApiModelProperty(example = "123456", value = "The system trace audit number of the original transaction. Contained in Original data elements. Only numeric values are allowed. Required for messages with:- Repeat, RepeatAdvice, Reversal, RepeatReversal")
	
	@Size(min = 0, max = 6)
	public String getOriginalSystemTraceAuditNumber() {
		return originalSystemTraceAuditNumber;
	}

	public void setOriginalSystemTraceAuditNumber(String originalSystemTraceAuditNumber) {
		this.originalSystemTraceAuditNumber = originalSystemTraceAuditNumber;
	}

	public CashDepositRequest originalDateAndTimeLocalTransaction(Timestamp originalDateAndTimeLocalTransaction) {
		this.originalDateAndTimeLocalTransaction = originalDateAndTimeLocalTransaction;
		return this;
	}

	/**
	 * The local date and time of the original transaction. Required for messages
	 * with:-Repeat, RepeatAdvice, Replacement, Reversal, RepeatReversal
	 * 
	 * @return originalDateAndTimeLocalTransaction
	 **/
	@ApiModelProperty(value = "The local date and time of the original transaction. Required for messages with:-Repeat, RepeatAdvice, Replacement, Reversal, RepeatReversal")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
	@Valid
	

	public Timestamp getOriginalDateAndTimeLocalTransaction() {
		return originalDateAndTimeLocalTransaction;
	}

	public void setOriginalDateAndTimeLocalTransaction(Timestamp originalDateAndTimeLocalTransaction) {
		this.originalDateAndTimeLocalTransaction = originalDateAndTimeLocalTransaction;
	}

	public CashDepositRequest originalAcquiringInstitutionIdentificationCode(
			String originalAcquiringInstitutionIdentificationCode) {
		this.originalAcquiringInstitutionIdentificationCode = originalAcquiringInstitutionIdentificationCode;
		return this;
	}

	/**
	 * The acquiring institution identification code of the original financial
	 * presentment. Required for messages with:- Repeat, RepeatAdvice, Replacement,
	 * Reversal, RepeatReversal. This is the IMD code assigned to transaction
	 * initiating terminals. This information is sent from the transaction
	 * initiating terminals, to be sent in request unchanged.
	 * 
	 * @return originalAcquiringInstitutionIdentificationCode
	 **/
	@ApiModelProperty(example = "11000589394", value = "The acquiring institution identification code of the original financial  presentment. Required for messages with:- Repeat, RepeatAdvice, Replacement, Reversal, RepeatReversal. This is the IMD code assigned to transaction initiating terminals. This information is sent from the transaction initiating terminals, to be sent in request unchanged.")
	
	@Size(min = 0, max = 11)
	public String getOriginalAcquiringInstitutionIdentificationCode() {
		return originalAcquiringInstitutionIdentificationCode;
	}

	public void setOriginalAcquiringInstitutionIdentificationCode(
			String originalAcquiringInstitutionIdentificationCode) {
		this.originalAcquiringInstitutionIdentificationCode = originalAcquiringInstitutionIdentificationCode;
	}

	public CashDepositRequest dataRecord(String dataRecord) {
		this.dataRecord = dataRecord;
		return this;
	}

	/**
	 * Other data required to be passed to support an administrative or file action
	 * message.
	 * 
	 * @return dataRecord
	 **/
	@ApiModelProperty(example = "T01078AC1000ACT001CAM00042000AM10042000CY0003KESCY1003KESOTY003207RTY003227TYP003207", required = true, value = "Other data required to be passed to support an administrative or file action message.")
	@NotNull
	
	@Size(min = 1, max = 999)
	public String getDataRecord() {
		return dataRecord;
	}

	public void setDataRecord(String dataRecord) {
		this.dataRecord = dataRecord;
	}

	public CashDepositRequest transactionOriginatorInstitutionIdentificationCode(
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

	public CashDepositRequest cardIssuerReferenceNumber(String cardIssuerReferenceNumber) {
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
	@ApiModelProperty(example = "209089", value = "Data supplied by a card issuer in an authorization response message, financial resentment response message, or in a charge back transaction that the acquirer may be required to be provided in subsequent transactions.")
	
	@Size(max = 99)
	public String getCardIssuerReferenceNumber() {
		return cardIssuerReferenceNumber;
	}

	public void setCardIssuerReferenceNumber(String cardIssuerReferenceNumber) {
		this.cardIssuerReferenceNumber = cardIssuerReferenceNumber;
	}

	public CashDepositRequest receivingInstitutionIdentificationCode(String receivingInstitutionIdentificationCode) {
		this.receivingInstitutionIdentificationCode = receivingInstitutionIdentificationCode;
		return this;
	}

	/**
	 * Code identifying the receiving institution.The cardholder bank institution
	 * code.This is the IMD code assigned to transaction initiating terminals. This
	 * information is sent from the transaction initiating terminals, to be sent in
	 * request unchanged.
	 * 
	 * @return receivingInstitutionIdentificationCode
	 **/
	@ApiModelProperty(example = "11000589394", required = true, value = "Code identifying the receiving institution.The cardholder bank institution code.This is the IMD code assigned to transaction initiating terminals. This information is sent from the transaction initiating terminals, to be sent in request unchanged.")
	@NotNull
	
	@Size(min = 1, max = 11)
	public String getReceivingInstitutionIdentificationCode() {
		return receivingInstitutionIdentificationCode;
	}

	public void setReceivingInstitutionIdentificationCode(String receivingInstitutionIdentificationCode) {
		this.receivingInstitutionIdentificationCode = receivingInstitutionIdentificationCode;
	}

	public CashDepositRequest accountIdentification2(String accountIdentification2) {
		this.accountIdentification2 = accountIdentification2;
		return this;
	}

	/**
	 * Account identification 2 (Card holder account) Credit account for cash
	 * deposit transactions.
	 * 
	 * @return accountIdentification2
	 **/
	@ApiModelProperty(example = "123456788", required = true, value = "Account identification 2 (Card holder account) Credit account for cash deposit transactions.")
	@NotNull
	
	@Size(min = 1, max = 28)
	public String getAccountIdentification2() {
		return accountIdentification2;
	}

	public void setAccountIdentification2(String accountIdentification2) {
		this.accountIdentification2 = accountIdentification2;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CashDepositRequest cashDepositRequest = (CashDepositRequest) o;
		return Objects.equals(this.channelId, cashDepositRequest.channelId)
				&& Objects.equals(this.primaryAccountNumberIdentifier,
						cashDepositRequest.primaryAccountNumberIdentifier)
				&& Objects.equals(this.amountCurrencyTransaction, cashDepositRequest.amountCurrencyTransaction)
				&& Objects.equals(this.amountTransaction, cashDepositRequest.amountTransaction)
				&& Objects.equals(this.amountCurrencySettlement, cashDepositRequest.amountCurrencySettlement)
				&& Objects.equals(this.amountSettlement, cashDepositRequest.amountSettlement)
				&& Objects.equals(this.amountCurrencyCardholderBilling,
						cashDepositRequest.amountCurrencyCardholderBilling)
				&& Objects.equals(this.amountCardholderBilling, cashDepositRequest.amountCardholderBilling)
				&& Objects.equals(this.dateTimeTransmission, cashDepositRequest.dateTimeTransmission)
				&& Objects.equals(this.amountCurrencyCardholderBillingFee,
						cashDepositRequest.amountCurrencyCardholderBillingFee)
				&& Objects.equals(this.amountCardholderBillingFee, cashDepositRequest.amountCardholderBillingFee)
				&& Objects.equals(this.conversionRateSettlement, cashDepositRequest.conversionRateSettlement)
				&& Objects.equals(this.conversionRateCardholderBilling,
						cashDepositRequest.conversionRateCardholderBilling)
				&& Objects.equals(this.systemTraceAuditNumber, cashDepositRequest.systemTraceAuditNumber)
				&& Objects.equals(this.timeLocalTransaction, cashDepositRequest.timeLocalTransaction)
				&& Objects.equals(this.dateSettlement, cashDepositRequest.dateSettlement)
				&& Objects.equals(this.messageTypeIdentifier, cashDepositRequest.messageTypeIdentifier)
				&& Objects.equals(this.functionCode, cashDepositRequest.functionCode)
				&& Objects.equals(this.messageFunction, cashDepositRequest.messageFunction)
				&& Objects.equals(this.cardAcceptorBusinessCode, cashDepositRequest.cardAcceptorBusinessCode)
				&& Objects.equals(this.dateReconciliation, cashDepositRequest.dateReconciliation)
				&& Objects.equals(this.reconciliationIndicator, cashDepositRequest.reconciliationIndicator)
				&& Objects.equals(this.currencyAmountOriginal, cashDepositRequest.currencyAmountOriginal)
				&& Objects.equals(this.amountOriginal, cashDepositRequest.amountOriginal)
				&& Objects.equals(this.acquiringInstitutionIdentificationCode,
						cashDepositRequest.acquiringInstitutionIdentificationCode)
				&& Objects.equals(this.forwardingInstitutionIdentificationCode,
						cashDepositRequest.forwardingInstitutionIdentificationCode)
				&& Objects.equals(this.retrievalReferenceNumber, cashDepositRequest.retrievalReferenceNumber)
				&& Objects.equals(this.approvalCode, cashDepositRequest.approvalCode)
				&& Objects.equals(this.actionCode, cashDepositRequest.actionCode)
				&& Objects.equals(this.cardAcceptorTerminalIdentification,
						cashDepositRequest.cardAcceptorTerminalIdentification)
				&& Objects.equals(this.cardAcceptorIdentificationCode,
						cashDepositRequest.cardAcceptorIdentificationCode)
				&& Objects.equals(this.cardAcceptorNameLocation, cashDepositRequest.cardAcceptorNameLocation)
				&& Objects.equals(this.currencyCodeAmountFee, cashDepositRequest.currencyCodeAmountFee)
				&& Objects.equals(this.valueAmountFee, cashDepositRequest.valueAmountFee)
				&& Objects.equals(this.additionalDataPrivate, cashDepositRequest.additionalDataPrivate)
				&& Objects.equals(this.originalMessageTypeIdentifier, cashDepositRequest.originalMessageTypeIdentifier)
				&& Objects.equals(this.originalSystemTraceAuditNumber,
						cashDepositRequest.originalSystemTraceAuditNumber)
				&& Objects.equals(this.originalDateAndTimeLocalTransaction,
						cashDepositRequest.originalDateAndTimeLocalTransaction)
				&& Objects.equals(this.originalAcquiringInstitutionIdentificationCode,
						cashDepositRequest.originalAcquiringInstitutionIdentificationCode)
				&& Objects.equals(this.dataRecord, cashDepositRequest.dataRecord)
				&& Objects.equals(this.transactionOriginatorInstitutionIdentificationCode,
						cashDepositRequest.transactionOriginatorInstitutionIdentificationCode)
				&& Objects.equals(this.cardIssuerReferenceNumber, cashDepositRequest.cardIssuerReferenceNumber)
				&& Objects.equals(this.receivingInstitutionIdentificationCode,
						cashDepositRequest.receivingInstitutionIdentificationCode)
				&& Objects.equals(this.accountIdentification2, cashDepositRequest.accountIdentification2);
	}

	@Override
	public int hashCode() {
		return Objects.hash(channelId, primaryAccountNumberIdentifier, amountCurrencyTransaction, amountTransaction,
				amountCurrencySettlement, amountSettlement, amountCurrencyCardholderBilling, amountCardholderBilling,
				dateTimeTransmission, amountCurrencyCardholderBillingFee, amountCardholderBillingFee,
				conversionRateSettlement, conversionRateCardholderBilling, systemTraceAuditNumber, timeLocalTransaction,
				dateSettlement, messageTypeIdentifier, functionCode, messageFunction, cardAcceptorBusinessCode,
				dateReconciliation, reconciliationIndicator, currencyAmountOriginal, amountOriginal,
				acquiringInstitutionIdentificationCode, forwardingInstitutionIdentificationCode,
				retrievalReferenceNumber, approvalCode, actionCode, cardAcceptorTerminalIdentification,
				cardAcceptorIdentificationCode, cardAcceptorNameLocation, currencyCodeAmountFee, valueAmountFee,
				additionalDataPrivate, originalMessageTypeIdentifier, originalSystemTraceAuditNumber,
				originalDateAndTimeLocalTransaction, originalAcquiringInstitutionIdentificationCode, dataRecord,
				transactionOriginatorInstitutionIdentificationCode, cardIssuerReferenceNumber,
				receivingInstitutionIdentificationCode, accountIdentification2);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class CashDepositRequest {\n");

		sb.append("    channelId: ").append(toIndentedString(channelId)).append("\n");
		sb.append("    primaryAccountNumberIdentifier: ").append(toIndentedString(primaryAccountNumberIdentifier))
				.append("\n");
		sb.append("    amountCurrencyTransaction: ").append(toIndentedString(amountCurrencyTransaction)).append("\n");
		sb.append("    amountTransaction: ").append(toIndentedString(amountTransaction)).append("\n");
		sb.append("    amountCurrencySettlement: ").append(toIndentedString(amountCurrencySettlement)).append("\n");
		sb.append("    amountSettlement: ").append(toIndentedString(amountSettlement)).append("\n");
		sb.append("    amountCurrencyCardholderBilling: ").append(toIndentedString(amountCurrencyCardholderBilling))
				.append("\n");
		sb.append("    amountCardholderBilling: ").append(toIndentedString(amountCardholderBilling)).append("\n");
		sb.append("    dateTimeTransmission: ").append(toIndentedString(dateTimeTransmission)).append("\n");
		sb.append("    amountCurrencyCardholderBillingFee: ")
				.append(toIndentedString(amountCurrencyCardholderBillingFee)).append("\n");
		sb.append("    amountCardholderBillingFee: ").append(toIndentedString(amountCardholderBillingFee)).append("\n");
		sb.append("    conversionRateSettlement: ").append(toIndentedString(conversionRateSettlement)).append("\n");
		sb.append("    conversionRateCardholderBilling: ").append(toIndentedString(conversionRateCardholderBilling))
				.append("\n");
		sb.append("    systemTraceAuditNumber: ").append(toIndentedString(systemTraceAuditNumber)).append("\n");
		sb.append("    timeLocalTransaction: ").append(toIndentedString(timeLocalTransaction)).append("\n");
		sb.append("    dateSettlement: ").append(toIndentedString(dateSettlement)).append("\n");
		sb.append("    messageTypeIdentifier: ").append(toIndentedString(messageTypeIdentifier)).append("\n");
		sb.append("    functionCode: ").append(toIndentedString(functionCode)).append("\n");
		sb.append("    messageFunction: ").append(toIndentedString(messageFunction)).append("\n");
		sb.append("    cardAcceptorBusinessCode: ").append(toIndentedString(cardAcceptorBusinessCode)).append("\n");
		sb.append("    dateReconciliation: ").append(toIndentedString(dateReconciliation)).append("\n");
		sb.append("    reconciliationIndicator: ").append(toIndentedString(reconciliationIndicator)).append("\n");
		sb.append("    currencyAmountOriginal: ").append(toIndentedString(currencyAmountOriginal)).append("\n");
		sb.append("    amountOriginal: ").append(toIndentedString(amountOriginal)).append("\n");
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
		sb.append("    currencyCodeAmountFee: ").append(toIndentedString(currencyCodeAmountFee)).append("\n");
		sb.append("    valueAmountFee: ").append(toIndentedString(valueAmountFee)).append("\n");
		sb.append("    additionalDataPrivate: ").append(toIndentedString(additionalDataPrivate)).append("\n");
		sb.append("    originalMessageTypeIdentifier: ").append(toIndentedString(originalMessageTypeIdentifier))
				.append("\n");
		sb.append("    originalSystemTraceAuditNumber: ").append(toIndentedString(originalSystemTraceAuditNumber))
				.append("\n");
		sb.append("    originalDateAndTimeLocalTransaction: ")
				.append(toIndentedString(originalDateAndTimeLocalTransaction)).append("\n");
		sb.append("    originalAcquiringInstitutionIdentificationCode: ")
				.append(toIndentedString(originalAcquiringInstitutionIdentificationCode)).append("\n");
		sb.append("    dataRecord: ").append(toIndentedString(dataRecord)).append("\n");
		sb.append("    transactionOriginatorInstitutionIdentificationCode: ")
				.append(toIndentedString(transactionOriginatorInstitutionIdentificationCode)).append("\n");
		sb.append("    cardIssuerReferenceNumber: ").append(toIndentedString(cardIssuerReferenceNumber)).append("\n");
		sb.append("    receivingInstitutionIdentificationCode: ")
				.append(toIndentedString(receivingInstitutionIdentificationCode)).append("\n");
		sb.append("    accountIdentification2: ").append(toIndentedString(accountIdentification2)).append("\n");
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
