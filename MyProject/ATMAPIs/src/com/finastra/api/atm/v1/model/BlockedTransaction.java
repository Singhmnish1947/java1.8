package com.finastra.api.atm.v1.model;

import java.sql.Timestamp;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * block object for ATM/POS transaction
 */
@ApiModel(description = "block object for ATM/POS transaction")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2021-01-06T09:31:53.977Z")




public class BlockedTransaction   {
  @JsonProperty("blockedAmount")
  private Amount blockedAmount = null;

  @JsonProperty("startDate")
  private Timestamp startDate = null;

  @JsonProperty("endDate")
  private Timestamp endDate = null;

  @JsonProperty("narrative")
  private String narrative = null;

  @JsonProperty("blockingReference")
  private String blockingReference = null;

  public BlockedTransaction blockedAmount(Amount blockedAmount) {
    this.blockedAmount = blockedAmount;
    return this;
  }

  /**
   * Blocked Amount due to ATM/POS transaction
   * @return blockedAmount
  **/
  @ApiModelProperty(value = "Blocked Amount due to ATM/POS transaction")


  public Amount getBlockedAmount() {
    return blockedAmount;
  }

  public void setBlockedAmount(Amount blockedAmount) {
    this.blockedAmount = blockedAmount;
  }

  public BlockedTransaction startDate(Timestamp startDate) {
    this.startDate = startDate;
    return this;
  }

  /**
   * Start Date/time stamp up to seconds of Block Transaction. Start date is when the transaction is executed in Essence.
   * @return startDate
  **/
  @ApiModelProperty(example = "2020-12-22 11:49:41", value = "Start Date/time stamp up to seconds of Block Transaction. Start date is when the transaction is executed in Essence.")

  @Valid

  public Timestamp getStartDate() {
    return startDate;
  }

  public void setStartDate(Timestamp startDate) {
    this.startDate = startDate;
  }

  public BlockedTransaction endDate(Timestamp endDate) {
    this.endDate = endDate;
    return this;
  }

  /**
   * End Date/time stamp up to seconds, of Blocked Transaction. Post End-Date transaction should be unblocked as per the Module configuration
   * @return endDate
  **/
  @ApiModelProperty(example = "2020-12-25 11:49:41", value = "End Date/time stamp up to seconds, of Blocked Transaction. Post End-Date transaction should be unblocked as per the Module configuration")

  @Valid

  public Timestamp getEndDate() {
    return endDate;
  }

  public void setEndDate(Timestamp endDate) {
    this.endDate = endDate;
  }

  public BlockedTransaction narrative(String narrative) {
    this.narrative = narrative;
    return this;
  }

  /**
   * Transaction Narrative for Blocked Transaction as captured during Online Transaction
   * @return narrative
  **/
  @ApiModelProperty(example = "2020-12-22 11:49:41_10000.00_USD_Flipkart_kjhkks19kjhkj", value = "Transaction Narrative for Blocked Transaction as captured during Online Transaction")

@Size(max=100) 
  public String getNarrative() {
    return narrative;
  }

  public void setNarrative(String narrative) {
    this.narrative = narrative;
  }

  public BlockedTransaction blockingReference(String blockingReference) {
    this.blockingReference = blockingReference;
    return this;
  }

  /**
   * Blocking Reference
   * @return blockingReference
  **/
  @ApiModelProperty(example = "POS123456", value = "Blocking Reference")

@Size(max=40) 
  public String getBlockingReference() {
    return blockingReference;
  }

  public void setBlockingReference(String blockingReference) {
    this.blockingReference = blockingReference;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BlockedTransaction blockedTransaction = (BlockedTransaction) o;
    return Objects.equals(this.blockedAmount, blockedTransaction.blockedAmount) &&
        Objects.equals(this.startDate, blockedTransaction.startDate) &&
        Objects.equals(this.endDate, blockedTransaction.endDate) &&
        Objects.equals(this.narrative, blockedTransaction.narrative) &&
        Objects.equals(this.blockingReference, blockedTransaction.blockingReference);
  }

  @Override
  public int hashCode() {
    return Objects.hash(blockedAmount, startDate, endDate, narrative, blockingReference);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BlockedTransaction {\n");
    
    sb.append("    blockedAmount: ").append(toIndentedString(blockedAmount)).append("\n");
    sb.append("    startDate: ").append(toIndentedString(startDate)).append("\n");
    sb.append("    endDate: ").append(toIndentedString(endDate)).append("\n");
    sb.append("    narrative: ").append(toIndentedString(narrative)).append("\n");
    sb.append("    blockingReference: ").append(toIndentedString(blockingReference)).append("\n");
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

