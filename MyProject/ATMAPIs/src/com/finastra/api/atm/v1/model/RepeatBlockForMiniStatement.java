package com.finastra.api.atm.v1.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * repeat block for mini statement
 */
@ApiModel(description = "repeat block for mini statement")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-09-26T12:19:00.869+05:30")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RepeatBlockForMiniStatement {
	@JsonProperty("dateTransactionPosting")
	private Timestamp dateTransactionPosting = null;

	@JsonProperty("dateTransactionValue")
	private Date dateTransactionValue = null;

	@JsonProperty("transactionType")
	private String transactionType = null;

	@JsonProperty("transactionPostingAction")
	private String transactionPostingAction = null;

	@JsonProperty("transactionAmount")
	private BigDecimal transactionAmount = null;

	@JsonProperty("transactionCurrency")
	private String transactionCurrency = null;

	@JsonProperty("transactionNarrative")
	private String transactionNarrative = null;

	@JsonProperty("transactionLedgerBalance")
	private BigDecimal transactionLedgerBalance = null;

	@JsonProperty("transactionAvailableBalance")
	private BigDecimal transactionAvailableBalance = null;

	public RepeatBlockForMiniStatement dateTransactionPosting(Timestamp dateTransactionPosting) {
		this.dateTransactionPosting = dateTransactionPosting;
		return this;
	}

	/**
	 * Transaction posting date.
	 * 
	 * @return dateTransactionPosting
	 **/
	@ApiModelProperty(required = true, value = "Transaction posting date.")
	@NotNull
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
	@Valid
	

	public Timestamp getDateTransactionPosting() {
		return dateTransactionPosting;
	}

	public void setDateTransactionPosting(Timestamp dateTransactionPosting) {
		this.dateTransactionPosting = dateTransactionPosting;
	}

	public RepeatBlockForMiniStatement dateTransactionValue(Date dateTransactionValue) {
		this.dateTransactionValue = dateTransactionValue;
		return this;
	}

	/**
	 * Transaction value date.
	 * 
	 * @return dateTransactionValue
	 **/
	@ApiModelProperty(value = "Transaction value date.")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Valid
	

	public Date getDateTransactionValue() {
		return dateTransactionValue;
	}

	public void setDateTransactionValue(Date dateTransactionValue) {
		this.dateTransactionValue = dateTransactionValue;
	}

	public RepeatBlockForMiniStatement transactionType(String transactionType) {
		this.transactionType = transactionType;
		return this;
	}

	/**
	 * Transaction type
	 * 
	 * @return transactionType
	 **/
	@ApiModelProperty(value = "Transaction type")
	
	@Size(max = 8)
	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public RepeatBlockForMiniStatement transactionPostingAction(String transactionPostingAction) {
		this.transactionPostingAction = transactionPostingAction;
		return this;
	}

	/**
	 * D- Indicates that the transaction is a debit. C- Indicates that the
	 * transaction is a credit
	 * 
	 * @return transactionPostingAction
	 **/
	@ApiModelProperty(value = "D- Indicates that the transaction is a debit. C- Indicates that the transaction is a credit")
	
	@Size(max = 1)
	public String getTransactionPostingAction() {
		return transactionPostingAction;
	}

	public void setTransactionPostingAction(String transactionPostingAction) {
		this.transactionPostingAction = transactionPostingAction;
	}

	public RepeatBlockForMiniStatement transactionAmount(BigDecimal transactionAmount) {
		this.transactionAmount = transactionAmount;
		return this;
	}

	/**
	 * Transaction amount
	 * 
	 * @return transactionAmount
	 **/
	@ApiModelProperty(value = "Transaction amount")
	@JsonRawValue
	
	public BigDecimal getTransactionAmount() {
		return transactionAmount;
	}

	public void setTransactionAmount(BigDecimal transactionAmount) {
		this.transactionAmount = transactionAmount;
	}

	public RepeatBlockForMiniStatement transactionCurrency(String transactionCurrency) {
		this.transactionCurrency = transactionCurrency;
		return this;
	}

	/**
	 * Transaction currency
	 * 
	 * @return transactionCurrency
	 **/
	@ApiModelProperty(value = "Transaction currency")
	
	@Size(max = 3)
	public String getTransactionCurrency() {
		return transactionCurrency;
	}

	public void setTransactionCurrency(String transactionCurrency) {
		this.transactionCurrency = transactionCurrency;
	}

	public RepeatBlockForMiniStatement transactionNarrative(String transactionNarrative) {
		this.transactionNarrative = transactionNarrative;
		return this;
	}

	/**
	 * Transaction narration
	 * 
	 * @return transactionNarrative
	 **/
	@ApiModelProperty(value = "Transaction narration")
	
	@Size(max = 100)
	public String getTransactionNarrative() {
		return transactionNarrative;
	}

	public void setTransactionNarrative(String transactionNarrative) {
		this.transactionNarrative = transactionNarrative;
	}

	public RepeatBlockForMiniStatement transactionLedgerBalance(BigDecimal transactionLedgerBalance) {
		this.transactionLedgerBalance = transactionLedgerBalance;
		return this;
	}

	/**
	 * Account Running Balance (Ledger Balance) after the transaction.
	 * 
	 * @return transactionLedgerBalance
	 **/
	@ApiModelProperty(value = "Account Running Balance (Ledger Balance) after the transaction.")
	@JsonRawValue
	
	public BigDecimal getTransactionLedgerBalance() {
		return transactionLedgerBalance;
	}

	public void setTransactionLedgerBalance(BigDecimal transactionLedgerBalance) {
		this.transactionLedgerBalance = transactionLedgerBalance;
	}

	public RepeatBlockForMiniStatement transactionAvailableBalance(BigDecimal transactionAvailableBalance) {
		this.transactionAvailableBalance = transactionAvailableBalance;
		return this;
	}

	/**
	 * Account available Balance (Ledger Balance) after the transaction
	 * 
	 * @return transactionAvailableBalance
	 **/
	@ApiModelProperty(value = "Account available Balance (Ledger Balance) after the transaction")
	@JsonRawValue
	
	public BigDecimal getTransactionAvailableBalance() {
		return transactionAvailableBalance;
	}

	public void setTransactionAvailableBalance(BigDecimal transactionAvailableBalance) {
		this.transactionAvailableBalance = transactionAvailableBalance;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		RepeatBlockForMiniStatement repeatBlockForMiniStatement = (RepeatBlockForMiniStatement) o;
		return Objects.equals(this.dateTransactionPosting, repeatBlockForMiniStatement.dateTransactionPosting)
				&& Objects.equals(this.dateTransactionValue, repeatBlockForMiniStatement.dateTransactionValue)
				&& Objects.equals(this.transactionType, repeatBlockForMiniStatement.transactionType)
				&& Objects.equals(this.transactionPostingAction, repeatBlockForMiniStatement.transactionPostingAction)
				&& Objects.equals(this.transactionAmount, repeatBlockForMiniStatement.transactionAmount)
				&& Objects.equals(this.transactionCurrency, repeatBlockForMiniStatement.transactionCurrency)
				&& Objects.equals(this.transactionNarrative, repeatBlockForMiniStatement.transactionNarrative)
				&& Objects.equals(this.transactionLedgerBalance, repeatBlockForMiniStatement.transactionLedgerBalance)
				&& Objects.equals(this.transactionAvailableBalance,
						repeatBlockForMiniStatement.transactionAvailableBalance);
	}

	@Override
	public int hashCode() {
		return Objects.hash(dateTransactionPosting, dateTransactionValue, transactionType, transactionPostingAction,
				transactionAmount, transactionCurrency, transactionNarrative, transactionLedgerBalance,
				transactionAvailableBalance);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class RepeatBlockForMiniStatement {\n");

		sb.append("    dateTransactionPosting: ").append(toIndentedString(dateTransactionPosting)).append("\n");
		sb.append("    dateTransactionValue: ").append(toIndentedString(dateTransactionValue)).append("\n");
		sb.append("    transactionType: ").append(toIndentedString(transactionType)).append("\n");
		sb.append("    transactionPostingAction: ").append(toIndentedString(transactionPostingAction)).append("\n");
		sb.append("    transactionAmount: ").append(toIndentedString(transactionAmount)).append("\n");
		sb.append("    transactionCurrency: ").append(toIndentedString(transactionCurrency)).append("\n");
		sb.append("    transactionNarrative: ").append(toIndentedString(transactionNarrative)).append("\n");
		sb.append("    transactionLedgerBalance: ").append(toIndentedString(transactionLedgerBalance)).append("\n");
		sb.append("    transactionAvailableBalance: ").append(toIndentedString(transactionAvailableBalance))
				.append("\n");
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
