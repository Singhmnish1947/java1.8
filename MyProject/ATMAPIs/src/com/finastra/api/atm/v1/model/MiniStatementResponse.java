package com.finastra.api.atm.v1.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Mini Statement Response
 */
@ApiModel(description = "Mini Statement Response")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-09-26T12:19:00.869+05:30")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MiniStatementResponse   {
  @JsonProperty("blockATMMiniStatement")
  private MiniStatementRequest blockATMMiniStatement = null;

  @JsonProperty("accountLedgerBalance")
  private BigDecimal accountLedgerBalance = null;

  @JsonProperty("accountAvailableBalance")
  private BigDecimal accountAvailableBalance = null;

  @JsonProperty("accountAvailableBalanceWithoutCredit")
  private BigDecimal accountAvailableBalanceWithoutCredit = null;

  @JsonProperty("statement")
  @Valid
  private List<RepeatBlockForMiniStatement> statement = new ArrayList<RepeatBlockForMiniStatement>();

  public MiniStatementResponse blockATMMiniStatement(MiniStatementRequest blockATMMiniStatement) {
    this.blockATMMiniStatement = blockATMMiniStatement;
    return this;
  }

  /**
   * Get blockATMMiniStatement
   * @return blockATMMiniStatement
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid

  public MiniStatementRequest getBlockATMMiniStatement() {
    return blockATMMiniStatement;
  }

  public void setBlockATMMiniStatement(MiniStatementRequest blockATMMiniStatement) {
    this.blockATMMiniStatement = blockATMMiniStatement;
  }

  public MiniStatementResponse accountLedgerBalance(BigDecimal accountLedgerBalance) {
    this.accountLedgerBalance = accountLedgerBalance;
    return this;
  }

  /**
   * Account Running Balance (Ledger Balance). BigDecimal(24,6) indicating 18 digits before and 6 after decimal
   * minimum: 1
   * @return accountLedgerBalance
  **/
  @ApiModelProperty(example = "70.0", required = true, value = "Account Running Balance (Ledger Balance). BigDecimal(24,6) indicating 18 digits before and 6 after decimal")
  @NotNull
  @JsonRawValue

@DecimalMin("1")
  public BigDecimal getAccountLedgerBalance() {
    return accountLedgerBalance;
  }

  public void setAccountLedgerBalance(BigDecimal accountLedgerBalance) {
    this.accountLedgerBalance = accountLedgerBalance;
  }

  public MiniStatementResponse accountAvailableBalance(BigDecimal accountAvailableBalance) {
    this.accountAvailableBalance = accountAvailableBalance;
    return this;
  }

  /**
   * Available balance including OD/Limits. BigDecimal(24,6) indicating 18  digits before and 6 after decimal
   * minimum: 1
   * @return accountAvailableBalance
  **/
  @ApiModelProperty(example = "70.0", required = true, value = "Available balance including OD/Limits. BigDecimal(24,6) indicating 18  digits before and 6 after decimal")
  @NotNull
  @JsonRawValue

@DecimalMin("1")
  public BigDecimal getAccountAvailableBalance() {
    return accountAvailableBalance;
  }

  public void setAccountAvailableBalance(BigDecimal accountAvailableBalance) {
    this.accountAvailableBalance = accountAvailableBalance;
  }

  public MiniStatementResponse accountAvailableBalanceWithoutCredit(BigDecimal accountAvailableBalanceWithoutCredit) {
    this.accountAvailableBalanceWithoutCredit = accountAvailableBalanceWithoutCredit;
    return this;
  }

  /**
   * Available balance excluding OD/Limits. BigDecimal(24,6) indicating 18  digits before and 6 after decimal
   * minimum: 1
   * @return accountAvailableBalanceWithoutCredit
  **/
  @ApiModelProperty(example = "70.0", required = true, value = "Available balance excluding OD/Limits. BigDecimal(24,6) indicating 18  digits before and 6 after decimal")
  @NotNull
  @JsonRawValue

@DecimalMin("1")
  public BigDecimal getAccountAvailableBalanceWithoutCredit() {
    return accountAvailableBalanceWithoutCredit;
  }

  public void setAccountAvailableBalanceWithoutCredit(BigDecimal accountAvailableBalanceWithoutCredit) {
    this.accountAvailableBalanceWithoutCredit = accountAvailableBalanceWithoutCredit;
  }

  public MiniStatementResponse statement(List<RepeatBlockForMiniStatement> statement) {
    this.statement = statement;
    return this;
  }

  public MiniStatementResponse addStatementItem(RepeatBlockForMiniStatement statementItem) {
    this.statement.add(statementItem);
    return this;
  }

  /**
   * record wise list
   * @return statement
  **/
  @ApiModelProperty(required = true, value = "record wise list")
  @NotNull

  @Valid
@Size(min=10,max=10) 
  public List<RepeatBlockForMiniStatement> getStatement() {
    return statement;
  }

  public void setStatement(List<RepeatBlockForMiniStatement> statement) {
    this.statement = statement;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MiniStatementResponse miniStatementResponse = (MiniStatementResponse) o;
    return Objects.equals(this.blockATMMiniStatement, miniStatementResponse.blockATMMiniStatement) &&
        Objects.equals(this.accountLedgerBalance, miniStatementResponse.accountLedgerBalance) &&
        Objects.equals(this.accountAvailableBalance, miniStatementResponse.accountAvailableBalance) &&
        Objects.equals(this.accountAvailableBalanceWithoutCredit, miniStatementResponse.accountAvailableBalanceWithoutCredit) &&
        Objects.equals(this.statement, miniStatementResponse.statement);
  }

  @Override
  public int hashCode() {
    return Objects.hash(blockATMMiniStatement, accountLedgerBalance, accountAvailableBalance, accountAvailableBalanceWithoutCredit, statement);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MiniStatementResponse {\n");
    
    sb.append("    blockATMMiniStatement: ").append(toIndentedString(blockATMMiniStatement)).append("\n");
    sb.append("    accountLedgerBalance: ").append(toIndentedString(accountLedgerBalance)).append("\n");
    sb.append("    accountAvailableBalance: ").append(toIndentedString(accountAvailableBalance)).append("\n");
    sb.append("    accountAvailableBalanceWithoutCredit: ").append(toIndentedString(accountAvailableBalanceWithoutCredit)).append("\n");
    sb.append("    statement: ").append(toIndentedString(statement)).append("\n");
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

