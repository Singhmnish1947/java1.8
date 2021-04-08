package com.finastra.api.atm.v1.model;

import java.math.BigDecimal;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import com.finastra.api.atm.v1.model.CashDepositRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Cash Deposit Response
 */
@ApiModel(description = "Cash Deposit Response")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-09-26T12:19:00.869+05:30")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CashDepositResponse   {
  @JsonProperty("blockATMCashDeposit")
  private CashDepositRequest blockATMCashDeposit = null;

  @JsonProperty("accountLedgerBalance")
  private BigDecimal accountLedgerBalance = null;

  @JsonProperty("accountAvailableBalance")
  private BigDecimal accountAvailableBalance = null;

  @JsonProperty("accountAvailableBalanceWithoutCredit")
  private BigDecimal accountAvailableBalanceWithoutCredit = null;

  /**
   * Status of the ATM cash withdrawal transaction. S for Success,F for Failure
   */
  public enum TransactionStatusEnum {
    S("S"),
    
    F("F");

    private String value;

    TransactionStatusEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static TransactionStatusEnum fromValue(String text) {
      for (TransactionStatusEnum b : TransactionStatusEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("transactionStatus")
  private TransactionStatusEnum transactionStatus = null;

  public CashDepositResponse blockATMCashDeposit(CashDepositRequest blockATMCashDeposit) {
    this.blockATMCashDeposit = blockATMCashDeposit;
    return this;
  }

  /**
   * Get blockATMCashDeposit
   * @return blockATMCashDeposit
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid

  public CashDepositRequest getBlockATMCashDeposit() {
    return blockATMCashDeposit;
  }

  public void setBlockATMCashDeposit(CashDepositRequest blockATMCashDeposit) {
    this.blockATMCashDeposit = blockATMCashDeposit;
  }

  public CashDepositResponse accountLedgerBalance(BigDecimal accountLedgerBalance) {
    this.accountLedgerBalance = accountLedgerBalance;
    return this;
  }

  /**
   * Account Running Balance (Ledger Balance). BigDecimal(24,6) indicating 18 digits before and 6 after decimal
   * minimum: 1
   * @return accountLedgerBalance
  **/
  @ApiModelProperty(example = "7000.0", required = true, value = "Account Running Balance (Ledger Balance). BigDecimal(24,6) indicating 18 digits before and 6 after decimal")
  @NotNull
  @JsonRawValue

@DecimalMin("1")
  public BigDecimal getAccountLedgerBalance() {
    return accountLedgerBalance;
  }

  public void setAccountLedgerBalance(BigDecimal accountLedgerBalance) {
    this.accountLedgerBalance = accountLedgerBalance;
  }

  public CashDepositResponse accountAvailableBalance(BigDecimal accountAvailableBalance) {
    this.accountAvailableBalance = accountAvailableBalance;
    return this;
  }

  /**
   * Available balance including OD/Limits. BigDecimal(24,6) indicating 18 digits before and 6 after decimal
   * minimum: 1
   * @return accountAvailableBalance
  **/
  @ApiModelProperty(example = "7000.0", required = true, value = "Available balance including OD/Limits. BigDecimal(24,6) indicating 18 digits before and 6 after decimal")
  @NotNull
  @JsonRawValue

@DecimalMin("1")
  public BigDecimal getAccountAvailableBalance() {
    return accountAvailableBalance;
  }

  public void setAccountAvailableBalance(BigDecimal accountAvailableBalance) {
    this.accountAvailableBalance = accountAvailableBalance;
  }

  public CashDepositResponse accountAvailableBalanceWithoutCredit(BigDecimal accountAvailableBalanceWithoutCredit) {
    this.accountAvailableBalanceWithoutCredit = accountAvailableBalanceWithoutCredit;
    return this;
  }

  /**
   * Available balance excluding OD/Limits. BigDecimal(24,6) indicating 18 digits before and 6 after decimal
   * minimum: 1
   * @return accountAvailableBalanceWithoutCredit
  **/
  @ApiModelProperty(example = "7000.0", required = true, value = "Available balance excluding OD/Limits. BigDecimal(24,6) indicating 18 digits before and 6 after decimal")
  @NotNull
  @JsonRawValue

@DecimalMin("1")
  public BigDecimal getAccountAvailableBalanceWithoutCredit() {
    return accountAvailableBalanceWithoutCredit;
  }

  public void setAccountAvailableBalanceWithoutCredit(BigDecimal accountAvailableBalanceWithoutCredit) {
    this.accountAvailableBalanceWithoutCredit = accountAvailableBalanceWithoutCredit;
  }

  public CashDepositResponse transactionStatus(TransactionStatusEnum transactionStatus) {
    this.transactionStatus = transactionStatus;
    return this;
  }

  /**
   * Status of the ATM cash withdrawal transaction. S for Success,F for Failure
   * @return transactionStatus
  **/
  @ApiModelProperty(example = "S", required = true, value = "Status of the ATM cash withdrawal transaction. S for Success,F for Failure")
  @NotNull

@Size(min=1,max=1) 
  public TransactionStatusEnum getTransactionStatus() {
    return transactionStatus;
  }

  public void setTransactionStatus(TransactionStatusEnum transactionStatus) {
    this.transactionStatus = transactionStatus;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CashDepositResponse cashDepositResponse = (CashDepositResponse) o;
    return Objects.equals(this.blockATMCashDeposit, cashDepositResponse.blockATMCashDeposit) &&
        Objects.equals(this.accountLedgerBalance, cashDepositResponse.accountLedgerBalance) &&
        Objects.equals(this.accountAvailableBalance, cashDepositResponse.accountAvailableBalance) &&
        Objects.equals(this.accountAvailableBalanceWithoutCredit, cashDepositResponse.accountAvailableBalanceWithoutCredit) &&
        Objects.equals(this.transactionStatus, cashDepositResponse.transactionStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(blockATMCashDeposit, accountLedgerBalance, accountAvailableBalance, accountAvailableBalanceWithoutCredit, transactionStatus);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CashDepositResponse {\n");
    
    sb.append("    blockATMCashDeposit: ").append(toIndentedString(blockATMCashDeposit)).append("\n");
    sb.append("    accountLedgerBalance: ").append(toIndentedString(accountLedgerBalance)).append("\n");
    sb.append("    accountAvailableBalance: ").append(toIndentedString(accountAvailableBalance)).append("\n");
    sb.append("    accountAvailableBalanceWithoutCredit: ").append(toIndentedString(accountAvailableBalanceWithoutCredit)).append("\n");
    sb.append("    transactionStatus: ").append(toIndentedString(transactionStatus)).append("\n");
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

