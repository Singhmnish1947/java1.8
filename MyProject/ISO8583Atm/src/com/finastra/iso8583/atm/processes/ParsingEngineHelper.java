package com.finastra.iso8583.atm.processes;

import com.google.gson.JsonObject;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ParsingEngineHelper {

	private static final Log LOGGER = LogFactory.getLog(ParsingEngine.class);

	public static Message getMessage(JsonObject fieldDefinitions, String isoMessage) {

		int headerLength = ParsingUtil.getFieldDefinitionInteger(fieldDefinitions,
				ISOParsingConstants.MESSAGE_HEADER_DEFINITION, ISOParsingConstants.LENGTH);

		JsonObject messageHeaderDefinition = ParsingUtil.getSubFieldDefinition(fieldDefinitions,
				ISOParsingConstants.MESSAGE_HEADER_DEFINITION);

		int messgaeLength = ParsingUtil.getFieldDefinitionInteger(messageHeaderDefinition,
				ISOParsingConstants.MESSAGE_LENGTH, ISOParsingConstants.LENGTH);
		int messgaeLengthStarts = ParsingUtil.getFieldDefinitionInteger(messageHeaderDefinition,
				ISOParsingConstants.MESSAGE_LENGTH, ISOParsingConstants.START);

		int messgaeTypeStarts = ParsingUtil.getFieldDefinitionInteger(messageHeaderDefinition,
				ISOParsingConstants.MESSAGE_TYPE, ISOParsingConstants.START);
		int messgaeTypeLength = ParsingUtil.getFieldDefinitionInteger(messageHeaderDefinition,
				ISOParsingConstants.MESSAGE_TYPE, ISOParsingConstants.LENGTH);

		String messageHeader = isoMessage.substring(0, headerLength);

		String messageData = isoMessage.substring(headerLength);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getMessage() | headerLength = " + headerLength + " | messgaeLength = " + messgaeLength
					+ " | messageHeader = " + messageHeader);
			LOGGER.debug("constructor Args for Message during request Parsing"
					+ messageHeader.substring(messgaeLengthStarts, messgaeLengthStarts + messgaeLength)
					+ messageHeader.substring(messgaeTypeStarts, messgaeTypeStarts + messgaeTypeLength) + messageData);
		}

		Message message = new Message(messageHeader.substring(messgaeLengthStarts, messgaeLengthStarts + messgaeLength),
				messageHeader.substring(messgaeTypeStarts, messgaeTypeStarts + messgaeTypeLength), messageData);
		message.setMessageHeader(messageHeader);

		return message;
	}

	public static void getSubFields(String fieldValue, HashMap<String, Object> subFieldMap, JsonObject fieldDefinitions,
			String fieldNumber, int count) {
		boolean hasSubFields = ParsingUtil.getFieldDefinitionBoolean(fieldDefinitions,
				ISOParsingConstants.HAS_SUB_FIELDS);
		JsonObject subFieldDefinitions;
		if (hasSubFields) {
			while (true) {
				String subFieldNumber = fieldNumber + ISOParsingConstants.DOT + count;
				if (fieldDefinitions.has(subFieldNumber)) {
					String subFieldName = ParsingUtil.getFieldDefinitionString(fieldDefinitions, subFieldNumber,
							ISOParsingConstants.NAME);
					int starts = ParsingUtil.getFieldDefinitionInteger(fieldDefinitions, subFieldNumber,
							ISOParsingConstants.START);
					int length = ParsingUtil.getFieldDefinitionInteger(fieldDefinitions, subFieldNumber,
							ISOParsingConstants.LENGTH);
					String subFieldValue = fieldValue.substring(starts, length);
					if (!"".equals(subFieldValue)) {
						subFieldMap.put(subFieldName, subFieldValue);
					}
					subFieldDefinitions = ParsingUtil.getSubFieldDefinition(fieldDefinitions, subFieldNumber);

				} else {
					break;
				}
				getSubFields(fieldValue, subFieldMap, subFieldDefinitions,
						fieldNumber + ISOParsingConstants.DOT + count, 1);
				count++;
			}
		}
	}

	public static String getParentField(JsonObject fieldDefinitions, HashMap<String, Object> availableFields,
			String fieldNumber, int count, String rawValue) {
		while (true) {
			String subFieldNumber = fieldNumber + ISOParsingConstants.DOT + count;
			if (fieldDefinitions.has(subFieldNumber)) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("value in getParentField() : subFieldNumber = " + subFieldNumber + " | rawValue = "
							+ rawValue + " | subField = "
							+ ParsingUtil.getFieldDefinitionValue(fieldDefinitions, subFieldNumber));
				}
				rawValue = rawValue + availableFields.get(subFieldNumber);
			} else {
				break;
			}
			rawValue = getParentField(fieldDefinitions, availableFields, fieldNumber + "." + count, count, rawValue);
			count++;
		}

		return rawValue;
	}

	public static String getParentField(HashMap<String, Object> fieldValue, JsonObject fieldDefinitions,
			String fieldNumber, int count, String rawValue) {

		boolean hasSubFields = ParsingUtil.getFieldDefinitionBoolean(fieldDefinitions,
				ISOParsingConstants.HAS_SUB_FIELDS);
		JsonObject subFieldDefinitions;
		if (hasSubFields) {
			while (true) {
				String subFieldNumber = fieldNumber + ISOParsingConstants.DOT + count;
				if (fieldDefinitions.has(subFieldNumber)) {
					String subFieldName = ParsingUtil.getFieldDefinitionString(fieldDefinitions, subFieldNumber,
							ISOParsingConstants.NAME);
					if (null != fieldValue.get(subFieldName)) {
						rawValue = rawValue.concat(fieldValue.get(subFieldName).toString());
					}
					subFieldDefinitions = ParsingUtil.getSubFieldDefinition(fieldDefinitions, subFieldNumber);
				} else {
					break;
				}
				getParentField(fieldValue, subFieldDefinitions, fieldNumber, count, rawValue);
				count++;
			}
		}
		return rawValue;
	}
}
