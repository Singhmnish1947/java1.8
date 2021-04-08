package com.finastra.api.atm.v1.model;

import java.util.Objects;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Used to define additional causes associated with the error when the associated error message does not provide sufficient clarity on remediation of the error
 */
@ApiModel(description = "Used to define additional causes associated with the error when the associated error message does not provide sufficient clarity on remediation of the error")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-09-26T12:19:00.869+05:30")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Causes   {
  @JsonProperty("code")
  private String code = null;

  @JsonProperty("message")
  private String message = null;

  @JsonProperty("severity")
  private String severity = null;

  @JsonProperty("comment")
  private String comment = null;

  @JsonProperty("field")
  private String field = null;

  @JsonProperty("fieldValue")
  private String fieldValue = null;

  public Causes code(String code) {
    this.code = code;
    return this;
  }

  /**
   * A code referencing the problem associated with this cause of the error
   * @return code
  **/
  @ApiModelProperty(example = "40280151", value = "A code referencing the problem associated with this cause of the error")


  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Causes message(String message) {
    this.message = message;
    return this;
  }

  /**
   * A short human-readable summary of the problem associated with this cause of the error
   * @return message
  **/
  @ApiModelProperty(example = "The account '1234567890' is dormant", value = "A short human-readable summary of the problem associated with this cause of the error")


  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Causes severity(String severity) {
    this.severity = severity;
    return this;
  }

  /**
   * The severity associated with this cause of the error
   * @return severity
  **/
  @ApiModelProperty(example = "ERROR", value = "The severity associated with this cause of the error")


  public String getSeverity() {
    return severity;
  }

  public void setSeverity(String severity) {
    this.severity = severity;
  }

  public Causes comment(String comment) {
    this.comment = comment;
    return this;
  }

  /**
   * A short human-readable comment or note associated with this cause of the error
   * @return comment
  **/
  @ApiModelProperty(example = "Ensure the account is active for the request", value = "A short human-readable comment or note associated with this cause of the error")


  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public Causes field(String field) {
    this.field = field;
    return this;
  }

  /**
   * The field associated with this cause of the error
   * @return field
  **/
  @ApiModelProperty(example = "account", value = "The field associated with this cause of the error")


  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public Causes fieldValue(String fieldValue) {
    this.fieldValue = fieldValue;
    return this;
  }

  /**
   * The value of the field associated with this cause of the error
   * @return fieldValue
  **/
  @ApiModelProperty(example = "543123467083", value = "The value of the field associated with this cause of the error")


  public String getFieldValue() {
    return fieldValue;
  }

  public void setFieldValue(String fieldValue) {
    this.fieldValue = fieldValue;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Causes causes = (Causes) o;
    return Objects.equals(this.code, causes.code) &&
        Objects.equals(this.message, causes.message) &&
        Objects.equals(this.severity, causes.severity) &&
        Objects.equals(this.comment, causes.comment) &&
        Objects.equals(this.field, causes.field) &&
        Objects.equals(this.fieldValue, causes.fieldValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, message, severity, comment, field, fieldValue);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Causes {\n");
    
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    severity: ").append(toIndentedString(severity)).append("\n");
    sb.append("    comment: ").append(toIndentedString(comment)).append("\n");
    sb.append("    field: ").append(toIndentedString(field)).append("\n");
    sb.append("    fieldValue: ").append(toIndentedString(fieldValue)).append("\n");
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

