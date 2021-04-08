package com.misys.ub.interfaces.exchangeRate;

import java.util.List;

public class SubCode implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3420352814691419707L;
	/**
	 * Field code.
	 */
	private java.lang.Integer code;

	/**
	 * Field fieldName.
	 */
	private java.lang.String fieldName = " ";

	/**
	 * Field severity.
	 */
	private java.lang.String severity;

	/**
	 * Field description.
	 */
	private java.lang.String description;

	/**
	 * Field parametersList.
	 */
	private List<String> parametersList;

	public java.lang.Integer getCode() {
		return code;
	}

	public void setCode(java.lang.Integer code) {
		this.code = code;
	}

	public java.lang.String getFieldName() {
		return fieldName;
	}

	public void setFieldName(java.lang.String fieldName) {
		this.fieldName = fieldName;
	}

	public java.lang.String getSeverity() {
		return severity;
	}

	public void setSeverity(java.lang.String severity) {
		this.severity = severity;
	}

	public java.lang.String getDescription() {
		return description;
	}

	public void setDescription(java.lang.String description) {
		this.description = description;
	}

	public List<String> getParametersList() {
		return parametersList;
	}

	public void setParametersList(List<String> parametersList) {
		this.parametersList = parametersList;
	}


}
