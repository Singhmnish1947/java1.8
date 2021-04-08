package com.finastra.api.atm.v1.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Account Block Details
 */
@ApiModel(description = "Account Block Details")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2021-01-06T09:31:53.977Z")




public class AccountBlockResponse   {
  @JsonProperty("accountId")
  private String accountId = null;

  @JsonProperty("accountCurrency")
  private String accountCurrency = null;

  @JsonProperty("totalBlockAmount")
  private Amount totalBlockAmount = null;

  @JsonProperty("blockedTransaction")
  @Valid
  private List<BlockedTransaction> blockedTransaction = null;

  public AccountBlockResponse accountId(String accountId) {
    this.accountId = accountId;
    return this;
  }

  /**
   *   Account number of the customer provided in request message. Account number is used to identify Blocked Transactions for ATM/POS
   * @return accountId
  **/
  @ApiModelProperty(example = "608PTY00360013", required = true, value = "  Account number of the customer provided in request message. Account number is used to identify Blocked Transactions for ATM/POS")
  @NotNull

@Size(min=1,max=20) 
  public String getAccountId() {
    return accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public AccountBlockResponse accountCurrency(String accountCurrency) {
    this.accountCurrency = accountCurrency;
    return this;
  }

  /**
   * Currency of Account provided in request message.
   * @return accountCurrency
  **/
  @ApiModelProperty(example = "USD", required = true, value = "Currency of Account provided in request message.")
  @NotNull

@Size(min=3,max=3) 
  public String getAccountCurrency() {
    return accountCurrency;
  }

  public void setAccountCurrency(String accountCurrency) {
    this.accountCurrency = accountCurrency;
  }

  public AccountBlockResponse totalBlockAmount(Amount totalBlockAmount) {
    this.totalBlockAmount = totalBlockAmount;
    return this;
  }

  /**
   * Total Blocked Amount on Account due to ATM/POS DMS Transactions. This should not include other non-ATM related blocked Transactions.
   * @return totalBlockAmount
  **/
  @ApiModelProperty(required = true, value = "Total Blocked Amount on Account due to ATM/POS DMS Transactions. This should not include other non-ATM related blocked Transactions.")
  @NotNull


  public Amount getTotalBlockAmount() {
    return totalBlockAmount;
  }

  public void setTotalBlockAmount(Amount totalBlockAmount) {
    this.totalBlockAmount = totalBlockAmount;
  }

  public AccountBlockResponse blockedTransaction(List<BlockedTransaction> blockedTransaction) {
    this.blockedTransaction = blockedTransaction;
    return this;
  }

  public AccountBlockResponse addBlockedTransactionItem(BlockedTransaction blockedTransactionItem) {
    if (this.blockedTransaction == null) {
      this.blockedTransaction = new ArrayList<BlockedTransaction>();
    }
    this.blockedTransaction.add(blockedTransactionItem);
    return this;
  }

  /**
   * Array to display multiple block on the account
   * @return blockedTransaction
  **/
  @ApiModelProperty(value = "Array to display multiple block on the account")

  @Valid

  public List<BlockedTransaction> getBlockedTransaction() {
    return blockedTransaction;
  }

  public void setBlockedTransaction(List<BlockedTransaction> blockedTransaction) {
    this.blockedTransaction = blockedTransaction;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AccountBlockResponse accountBlockResponse = (AccountBlockResponse) o;
    return Objects.equals(this.accountId, accountBlockResponse.accountId) &&
        Objects.equals(this.accountCurrency, accountBlockResponse.accountCurrency) &&
        Objects.equals(this.totalBlockAmount, accountBlockResponse.totalBlockAmount) &&
        Objects.equals(this.blockedTransaction, accountBlockResponse.blockedTransaction);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accountId, accountCurrency, totalBlockAmount, blockedTransaction);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccountBlockResponse {\n");
    
    sb.append("    accountId: ").append(toIndentedString(accountId)).append("\n");
    sb.append("    accountCurrency: ").append(toIndentedString(accountCurrency)).append("\n");
    sb.append("    totalBlockAmount: ").append(toIndentedString(totalBlockAmount)).append("\n");
    sb.append("    blockedTransaction: ").append(toIndentedString(blockedTransaction)).append("\n");
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

