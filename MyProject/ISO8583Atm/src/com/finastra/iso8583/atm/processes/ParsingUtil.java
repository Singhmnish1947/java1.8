package com.finastra.iso8583.atm.processes;

import com.google.gson.JsonObject;

public class ParsingUtil {
	
	public static String getFieldDefinitionString(JsonObject fieldDefinitions, String field, String property) {

		return fieldDefinitions.getAsJsonObject(field).get(property).getAsString();
	}
	
	public static Integer getFieldDefinitionInteger(JsonObject fieldDefinitions, String field, String property) {

		return fieldDefinitions.getAsJsonObject(field).get(property).getAsInt();
	}
	
	public static JsonObject getSubFieldDefinition(JsonObject fieldDefinitions, String field) {

		return fieldDefinitions.getAsJsonObject(field);
	}
	
	public static boolean getFieldDefinitionBoolean(JsonObject fieldDefinitions, String field, String property) {

		return fieldDefinitions.getAsJsonObject(field).get(property).getAsBoolean();
	}
	
	public static boolean getFieldDefinitionBoolean(JsonObject fieldDefinitions, String property) {

		return fieldDefinitions.get(property).getAsBoolean();
	}
	
	public static String getFieldDefinitionValue(JsonObject fieldDefinitions, String property) {

		return fieldDefinitions.get(property).getAsString();
	}
	
}
