package com.finastra.api.atm.v1.model;

import java.math.BigDecimal;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Balance Enquiry Response
 */
@ApiModel(description = "Balance Enquiry Response")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-09-26T12:19:00.869+05:30")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BalanceEnquiryResponse {
	@JsonProperty("blockATMBalanceEnquiry")
	private BalanceEnquiryRequest blockATMBalanceEnquiry = null;

	@JsonProperty("accountLedgerBalance")
	private BigDecimal accountLedgerBalance = null;

	@JsonProperty("accountAvailableBalance")
	private BigDecimal accountAvailableBalance = null;

	@JsonProperty("accountAvailableBalanceWithoutCredit")
	private BigDecimal accountAvailableBalanceWithoutCredit = null;

	public BalanceEnquiryResponse blockATMBalanceEnquiry(BalanceEnquiryRequest blockATMBalanceEnquiry) {
		this.blockATMBalanceEnquiry = blockATMBalanceEnquiry;
		return this;
	}

	/**
	 * Get blockATMBalanceEnquiry
	 * 
	 * @return blockATMBalanceEnquiry
	 **/
	@ApiModelProperty(required = true, value = "")
	@NotNull

	@Valid

	public BalanceEnquiryRequest getBlockATMBalanceEnquiry() {
		return blockATMBalanceEnquiry;
	}

	public void setBlockATMBalanceEnquiry(BalanceEnquiryRequest blockATMBalanceEnquiry) {
		this.blockATMBalanceEnquiry = blockATMBalanceEnquiry;
	}

	public BalanceEnquiryResponse accountLedgerBalance(BigDecimal accountLedgerBalance) {
		this.accountLedgerBalance = accountLedgerBalance;
		return this;
	}

	/**
	 * Account Running Balance (Ledger Balance). BigDecimal(24,6) indicating 18
	 * digits before and 6 after decimal minimum: 1
	 * 
	 * @return accountLedgerBalance
	 **/
	@ApiModelProperty(example = "50.0", required = true, value = "Account Running Balance (Ledger Balance). BigDecimal(24,6) indicating 18 digits before and 6 after decimal")
	@NotNull
	@JsonRawValue

	@DecimalMin("1")
	public BigDecimal getAccountLedgerBalance() {
		return accountLedgerBalance;
	}

	public void setAccountLedgerBalance(BigDecimal accountLedgerBalance) {
		this.accountLedgerBalance = accountLedgerBalance;
	}

	public BalanceEnquiryResponse accountAvailableBalance(BigDecimal accountAvailableBalance) {
		this.accountAvailableBalance = accountAvailableBalance;
		return this;
	}

	/**
	 * Available balance including OD/Limits. BigDecimal(24,6) indicating 18 digits
	 * before and 6 after decimal minimum: 1
	 * 
	 * @return accountAvailableBalance
	 **/
	@ApiModelProperty(example = "50.0", required = true, value = "Available balance including OD/Limits. BigDecimal(24,6) indicating 18  digits before and 6 after decimal")
	@NotNull
	@JsonRawValue

	@DecimalMin("1")
	public BigDecimal getAccountAvailableBalance() {
		return accountAvailableBalance;
	}

	public void setAccountAvailableBalance(BigDecimal accountAvailableBalance) {
		this.accountAvailableBalance = accountAvailableBalance;
	}

	public BalanceEnquiryResponse accountAvailableBalanceWithoutCredit(
			BigDecimal accountAvailableBalanceWithoutCredit) {
		this.accountAvailableBalanceWithoutCredit = accountAvailableBalanceWithoutCredit;
		return this;
	}

	/**
	 * Available balance excluding OD/Limits. BigDecimal(24,6) indicating 18 digits
	 * before and 6 after decimal minimum: 1
	 * 
	 * @return accountAvailableBalanceWithoutCredit
	 **/
	@ApiModelProperty(example = "10.0", required = true, value = "Available balance excluding OD/Limits. BigDecimal(24,6) indicating 18 digits before and 6 after decimal")
	@NotNull
	@JsonRawValue

	@DecimalMin("1")
	public BigDecimal getAccountAvailableBalanceWithoutCredit() {
		return accountAvailableBalanceWithoutCredit;
	}

	public void setAccountAvailableBalanceWithoutCredit(BigDecimal accountAvailableBalanceWithoutCredit) {
		this.accountAvailableBalanceWithoutCredit = accountAvailableBalanceWithoutCredit;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		BalanceEnquiryResponse balanceEnquiryResponse = (BalanceEnquiryResponse) o;
		return Objects.equals(this.blockATMBalanceEnquiry, balanceEnquiryResponse.blockATMBalanceEnquiry)
				&& Objects.equals(this.accountLedgerBalance, balanceEnquiryResponse.accountLedgerBalance)
				&& Objects.equals(this.accountAvailableBalance, balanceEnquiryResponse.accountAvailableBalance)
				&& Objects.equals(this.accountAvailableBalanceWithoutCredit,
						balanceEnquiryResponse.accountAvailableBalanceWithoutCredit);
	}

	@Override
	public int hashCode() {
		return Objects.hash(blockATMBalanceEnquiry, accountLedgerBalance, accountAvailableBalance,
				accountAvailableBalanceWithoutCredit);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class BalanceEnquiryResponse {\n");

		sb.append("    blockATMBalanceEnquiry: ").append(toIndentedString(blockATMBalanceEnquiry)).append("\n");
		sb.append("    accountLedgerBalance: ").append(toIndentedString(accountLedgerBalance)).append("\n");
		sb.append("    accountAvailableBalance: ").append(toIndentedString(accountAvailableBalance)).append("\n");
		sb.append("    accountAvailableBalanceWithoutCredit: ")
				.append(toIndentedString(accountAvailableBalanceWithoutCredit)).append("\n");
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
