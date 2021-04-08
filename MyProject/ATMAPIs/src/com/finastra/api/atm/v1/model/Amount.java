package com.finastra.api.atm.v1.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Blocked Amount due to ATM/POS transaction
 */
@ApiModel(description = "Blocked Amount due to ATM/POS transaction")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2021-01-06T09:31:53.977Z")




public class Amount   {
  @JsonProperty("value")
  private Double value = null;

  @JsonProperty("currency")
  private String currency = null;
  Amount value(Double value) {
    this.value = value;
    return this;
  }

  /**
   * A monetary amount in a given currency supporting an amount format of 18.3. The amount is always positive.
   * @return value
  **/
  @ApiModelProperty(example = "10000.0", value = "A monetary amount in a given currency supporting an amount format of 18.3. The amount is always positive.")


  public Double getValue() {
    return value;
  }

  public void setValue(Double value) {
    this.value = value;
  }

  public Amount currency(String currency) {
    this.currency = currency;
    return this;
  }

  /**
   * ISO 4217: currency code, it is the identifier of the currency
   * @return currency
  **/
  @ApiModelProperty(example = "USD", value = "ISO 4217: currency code, it is the identifier of the currency")

@Size(min=3,max=3) 
  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Amount amount = (Amount) o;
    return Objects.equals(this.value, amount.value) &&
        Objects.equals(this.currency, amount.currency);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, currency);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Amount {\n");
    
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
    sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
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

