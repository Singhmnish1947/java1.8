package com.finastra.iso8583.atm.processes;

import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.misys.cbs.common.GetConfigLocation;

public class ParsingEngine {

	private static final Log LOGGER = LogFactory.getLog(ParsingEngine.class);

	private static final byte[] HEXAVALUE = new byte[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B',
			'C', 'D', 'E', 'F' };

	static JsonObject fieldDefinitions = null;
	static JsonObject isoFieldDefinitions = null;

	static {
		fieldDefinitions = getATMConfiguration("ISOFieldsSmartVista.json");
	}

	public Message parse(String isoMessage) {

		Message parsedMessage = ParsingEngineHelper.getMessage(fieldDefinitions, isoMessage);
		initializeMessage(parsedMessage);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("input iso message parse() :" + isoMessage);
			LOGGER.debug("parsed message parse() : " + parsedMessage.toString());
		}
		return parsedMessage;
	}

	public Message prepareMessage(Message message) {
		List<Integer> availableFieldsList = new ArrayList<>();
		HashMap<Integer, String> finalFieldValueMap = getIsoFields(message, availableFieldsList);

		StringBuilder isoMessage = new StringBuilder();
		Collections.sort(availableFieldsList);
		String bitMap = packField(availableFieldsList);
		message.setPrimaryBitMap(bitMap.substring(0, 16));

		if (availableFieldsList.get(0) == 1) {
			message.setSecondaryBitMap(bitMap.substring(16));
			availableFieldsList.remove(0);
		}

		for (int field : availableFieldsList) {

			isoMessage = isoMessage.append(finalFieldValueMap.get(field));
		}
		message.setMessageData(isoMessage.toString());
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("prepareMessage() finalFieldValueMap :" + finalFieldValueMap);
			LOGGER.debug("prepareMessage() bitMap :" + bitMap);
			LOGGER.debug("prepareMessage() isoMessage : " + isoMessage);
		}
		getMessageHeader(message);
		return message;
	}

	public Message prepareResponseMessage(Message message) {
		List<Integer> availableFieldsList = new ArrayList<>();
		StringBuilder messageData = getIsoFieldsResponse(message, availableFieldsList);

		StringBuilder isoMessage = new StringBuilder();
		Collections.sort(availableFieldsList);
		String bitMap = packField(availableFieldsList);
		message.setPrimaryBitMap(bitMap.substring(0, 16));

		if (availableFieldsList.get(0) == 1) {
			message.setSecondaryBitMap(bitMap.substring(16));
			availableFieldsList.remove(0);
		}

		isoMessage = isoMessage.append(messageData);
		message.setMessageData(isoMessage.toString());
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("prepareMessage() messageData :" + messageData);
			LOGGER.debug("prepareMessage() bitMap :" + bitMap);
			LOGGER.debug("prepareMessage() isoMessage : " + isoMessage);
		}
		getMessageHeader(message);
		return message;
	}

	private void getMessageHeader(Message message) {
		message.setMessageTypeIdentifier(ATMPOSTransactionDetails.getResponseMTI(message.getMessageTypeIdentifier()));
		JsonObject messageHeaderDefinition = ParsingUtil.getSubFieldDefinition(fieldDefinitions,
				ISOParsingConstants.MESSAGE_HEADER_DEFINITION);

		int messgaeLengthStarts = ParsingUtil.getFieldDefinitionInteger(messageHeaderDefinition,
				ISOParsingConstants.MESSAGE_LENGTH, ISOParsingConstants.START);
		int messageLength = ParsingUtil.getFieldDefinitionInteger(messageHeaderDefinition,
				ISOParsingConstants.MESSAGE_LENGTH, ISOParsingConstants.LENGTH);
		int messgaeTypeStarts = ParsingUtil.getFieldDefinitionInteger(messageHeaderDefinition,
				ISOParsingConstants.MESSAGE_TYPE, ISOParsingConstants.START);
		int messgaeTypeLength = ParsingUtil.getFieldDefinitionInteger(messageHeaderDefinition,
				ISOParsingConstants.MESSAGE_TYPE, ISOParsingConstants.LENGTH);

		String messageHeaderConstant = ParsingUtil.getFieldDefinitionString(messageHeaderDefinition,
				ISOParsingConstants.MESSAGE_CONSTANT, ISOParsingConstants.VALUE);
		int messageHeaderConstantstart = ParsingUtil.getFieldDefinitionInteger(messageHeaderDefinition,
				ISOParsingConstants.MESSAGE_CONSTANT, ISOParsingConstants.START);
		int messageHeaderConstantLength = ParsingUtil.getFieldDefinitionInteger(messageHeaderDefinition,
				ISOParsingConstants.MESSAGE_CONSTANT, ISOParsingConstants.LENGTH);
		StringBuilder defaultMessageHeader = new StringBuilder(StringUtils.leftPad("", 8, " "));

		defaultMessageHeader.replace(messageHeaderConstantstart, messageHeaderConstantLength, messageHeaderConstant);
		defaultMessageHeader.replace(messgaeLengthStarts, (messgaeLengthStarts + messageLength), "0000");
		defaultMessageHeader.replace(messgaeTypeStarts, (messgaeTypeStarts + messgaeTypeLength),
				message.getMessageTypeIdentifier());

		message.setMessageHeader(defaultMessageHeader.toString());
	}

	private static StringBuilder getIsoFieldsResponse(Message message, List<Integer> availableFieldsList) {

		Integer fieldNumber = 2;
		StringBuilder messageData = new StringBuilder();
		boolean hasSecondryBitMap = false;

		while (fieldNumber < 128) {
			String rawValue = "";
			String fieldNumberString = fieldNumber.toString();
			String fieldName = ParsingUtil.getFieldDefinitionString(fieldDefinitions, fieldNumberString,
					ISOParsingConstants.NAME);
			int varNoOfDigits = ParsingUtil.getFieldDefinitionInteger(fieldDefinitions, fieldNumberString,
					"VarNoOfDigits");
			Object fieldValue = message.getFieldValue(fieldName);
			if (null != fieldValue) {
				Boolean hasSubFields = ParsingUtil.getFieldDefinitionBoolean(fieldDefinitions, fieldNumberString,
						ISOParsingConstants.HAS_SUB_FIELDS);
				if (hasSubFields) {
					HashMap<String, Object> subMap = (HashMap) fieldValue;
					rawValue = ParsingEngineHelper.getParentField(subMap,
							ParsingUtil.getSubFieldDefinition(fieldDefinitions, fieldNumberString), fieldNumberString,
							1, rawValue);
				} else {
					rawValue = fieldValue.toString();
				}
				Integer length = rawValue.length();
				String lengthSize = length.toString();
				if (varNoOfDigits != 0 && (lengthSize.length() <= varNoOfDigits)) {
					Integer rawLength = rawValue.length();
					String finalRawLength = org.apache.commons.lang.StringUtils.leftPad(rawLength.toString(),
							varNoOfDigits, "0");

					rawValue = finalRawLength.concat(rawValue);
				}

				messageData = messageData.append(rawValue);
				availableFieldsList.add(fieldNumber);
			}
			if (!hasSecondryBitMap && fieldNumber > 64 && availableFieldsList.size()>9) {
				availableFieldsList.add(1);
				hasSecondryBitMap = true;
			}
			fieldNumber++;
		}

		return messageData;

	}

	private static HashMap<Integer, String> getIsoFields(Message message, List<Integer> availableFieldsList) {

		int fieldNumber = 0;
		int length = 0;
		String type = "";
		String parentValue = "";
		String rawValue = "";
		HashMap<Integer, String> finalFieldValueMap = new HashMap<>();
		HashMap<String, Object> availableFields = message.getMessageFields();

		boolean hasSecondryBitMap = false;
		for (Map.Entry<String, Object> mapElement : availableFields.entrySet()) {

			boolean isSubField = ParsingUtil.getFieldDefinitionBoolean(isoFieldDefinitions, mapElement.getKey(),
					ISOParsingConstants.IS_SUB_FIELD);

			if (isSubField) {
				String parentFieldName = ParsingUtil.getFieldDefinitionString(isoFieldDefinitions, mapElement.getKey(),
						ISOParsingConstants.PARENT_FIELD);
				fieldNumber = ParsingUtil.getFieldDefinitionInteger(isoFieldDefinitions, parentFieldName,
						ISOParsingConstants.FIELD_NUMBER);
				if (finalFieldValueMap.get(fieldNumber) == null) {
					type = ParsingUtil.getFieldDefinitionString(isoFieldDefinitions, parentFieldName,
							ISOParsingConstants.TYPE);
					length = ParsingUtil.getFieldDefinitionInteger(isoFieldDefinitions, parentFieldName,
							ISOParsingConstants.LENGTH);
					parentValue = ParsingEngineHelper.getParentField(
							ParsingUtil.getSubFieldDefinition(isoFieldDefinitions, parentFieldName), availableFields,
							parentFieldName, 1, "");
					rawValue = getFormattedToRawData(parentValue, type, length);
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("getIsoFields() fieldNumber : isSubField = " + isSubField + " | parentValue = "
								+ parentValue + " | rawValue = " + rawValue);
					}
				} else {
					continue;
				}
			} else if (finalFieldValueMap.get(mapElement.getKey()) == null) {
				fieldNumber = ParsingUtil.getFieldDefinitionInteger(isoFieldDefinitions, mapElement.getKey(),
						ISOParsingConstants.FIELD_NUMBER);
				type = ParsingUtil.getFieldDefinitionString(isoFieldDefinitions, mapElement.getKey(),
						ISOParsingConstants.TYPE);
				length = ParsingUtil.getFieldDefinitionInteger(isoFieldDefinitions, mapElement.getKey(),
						ISOParsingConstants.LENGTH);
				rawValue = getFormattedToRawData(mapElement.getValue(), type, length);
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("getIsoFields() fieldNumber :" + fieldNumber);
					LOGGER.debug("getIsoFields() rawValue :" + rawValue);
				}
			}
			if (!hasSecondryBitMap && fieldNumber > 64) {
				availableFieldsList.add(1);
				hasSecondryBitMap = true;
			}
			finalFieldValueMap.put(fieldNumber, rawValue);
		}
		return finalFieldValueMap;
	}

	public static String getFormattedToRawData(Object value, String type, int length) {
		String finalValue = value.toString();
		switch (type) {
		case ISOParsingConstants.LLVAR:
			if (finalValue.length() < 10) {
				finalValue = ISOParsingConstants.ZERO + finalValue.length() + finalValue;
			} else {
				finalValue = finalValue.length() + finalValue;
			}
			break;

		case ISOParsingConstants.LLLVAR:
			if (finalValue.length() > 9 && finalValue.length() < 100) {
				finalValue = ISOParsingConstants.ZERO + finalValue.length() + finalValue;
			} else if (finalValue.length() < 10) {
				finalValue = ISOParsingConstants.ZERO_ZERO + finalValue.length() + finalValue;
			} else {
				finalValue = finalValue.length() + finalValue;
			}
			break;

		case ISOParsingConstants.YYMM:
			SimpleDateFormat inputSimpleDateFormatYm = new SimpleDateFormat("yyyy-MM");
			SimpleDateFormat outputSimpleDateFormatYm = new SimpleDateFormat("yyMM");
			try {
				finalValue = outputSimpleDateFormatYm.format(inputSimpleDateFormatYm.parse(value.toString()));
			} catch (ParseException exception) {
				LOGGER.info(exception.getCause() != null ? ExceptionUtils.getStackTrace(exception.getCause())
						: ExceptionUtils.getStackTrace(exception));
			}
			break;
		case ISOParsingConstants.YYMMDD:
			SimpleDateFormat inputSimpleDateFormatYmd = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat outputSimpleDateFormatYmd = new SimpleDateFormat("yyMMdd");
			try {
				finalValue = outputSimpleDateFormatYmd.format(inputSimpleDateFormatYmd.parse(value.toString()));
			} catch (ParseException exception) {
				LOGGER.info(exception.getCause() != null ? ExceptionUtils.getStackTrace(exception.getCause())
						: ExceptionUtils.getStackTrace(exception));
			}
			break;
		case ISOParsingConstants.YYMMDDHHMMSS:
			SimpleDateFormat inputSimpleDateFormatYmdhms = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss");
			SimpleDateFormat outputSimpleDateFormatYmdhms = new SimpleDateFormat("yyMMddHHmmss");
			try {
				finalValue = outputSimpleDateFormatYmdhms.format(inputSimpleDateFormatYmdhms.parse(value.toString()));
			} catch (ParseException exception) {
				LOGGER.info(exception.getCause() != null ? ExceptionUtils.getStackTrace(exception.getCause())
						: ExceptionUtils.getStackTrace(exception));
			}
			break;
		case ISOParsingConstants.YYYYMMDDHHMMSS:
			SimpleDateFormat inputSimpleDateFormatYYmdhms = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");
			SimpleDateFormat outputSimpleDateFormatYYmdhms = new SimpleDateFormat("yyMMddHHmmss");
			try {
				finalValue = outputSimpleDateFormatYYmdhms.format(inputSimpleDateFormatYYmdhms.parse(value.toString()));
			} catch (ParseException exception) {
				LOGGER.info(exception.getCause() != null ? ExceptionUtils.getStackTrace(exception.getCause())
						: ExceptionUtils.getStackTrace(exception));
			}
			break;
		default:
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getIsoFields() getData :" + finalValue + " | type : " + type);
		}
		return finalValue;
	}

	public static String getRawToFormattedData(Object value, String type, int length) {
		String finalValue = value.toString();
		switch (type) {
		case ISOParsingConstants.LLVAR:
			if (finalValue.length() < 10) {
				finalValue = ISOParsingConstants.ZERO + finalValue.length() + finalValue;
			} else {
				finalValue = finalValue.length() + finalValue;
			}
			break;

		case ISOParsingConstants.LLLVAR:
			if (finalValue.length() > 9 && finalValue.length() < 100) {
				finalValue = ISOParsingConstants.ZERO + finalValue.length() + finalValue;
			} else if (finalValue.length() < 10) {
				finalValue = ISOParsingConstants.ZERO_ZERO + finalValue.length() + finalValue;
			} else {
				finalValue = finalValue.length() + finalValue;
			}
			break;

		case ISOParsingConstants.YYMM:
			SimpleDateFormat inputSimpleDateFormatYm = new SimpleDateFormat("yyyy-MM");
			SimpleDateFormat outputSimpleDateFormatYm = new SimpleDateFormat("yyMM");
			try {
				finalValue = inputSimpleDateFormatYm.format(outputSimpleDateFormatYm.parse(value.toString()));
			} catch (ParseException exception) {
				LOGGER.info(exception.getCause() != null ? ExceptionUtils.getStackTrace(exception.getCause())
						: ExceptionUtils.getStackTrace(exception));
			}
			break;
		case ISOParsingConstants.YYMMDD:
			SimpleDateFormat inputSimpleDateFormatYmd = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat outputSimpleDateFormatYmd = new SimpleDateFormat("yyMMdd");
			try {
				finalValue = inputSimpleDateFormatYmd.format(outputSimpleDateFormatYmd.parse(value.toString()));
			} catch (ParseException exception) {
				LOGGER.info(exception.getCause() != null ? ExceptionUtils.getStackTrace(exception.getCause())
						: ExceptionUtils.getStackTrace(exception));
			}
			break;
		case ISOParsingConstants.YYMMDDHHMMSS:
			SimpleDateFormat inputSimpleDateFormatYmdhms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat outputSimpleDateFormatYmdhms = new SimpleDateFormat("yyMMddHHmmss");
			try {
				finalValue = inputSimpleDateFormatYmdhms.format(outputSimpleDateFormatYmdhms.parse(value.toString()));
			} catch (ParseException exception) {
				LOGGER.info(exception.getCause() != null ? ExceptionUtils.getStackTrace(exception.getCause())
						: ExceptionUtils.getStackTrace(exception));

			}
			break;
		default:
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getIsoFields() getData :" + finalValue + " | type : " + type);
		}
		return finalValue;
	}

	private void initializeMessage(Message parsedMessage) {
		String bitMap;
		String binaryPrimaryBitMap = unpackFields(parsedMessage.getPrimaryBitMap());

		if (binaryPrimaryBitMap.startsWith(ISOParsingConstants.ONE)) {
			parsedMessage.setSecondaryBitMap(parsedMessage.getMessageData().substring(0, 16));
			parsedMessage.setMessageData(parsedMessage.getMessageData().substring(16));
			String binarySecondaryBitMap = unpackFields(parsedMessage.getSecondaryBitMap());
			bitMap = binaryPrimaryBitMap + binarySecondaryBitMap;

		} else {

			bitMap = binaryPrimaryBitMap;
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("initializeMessage()  bitMap :" + bitMap);
		}
		getAvaialbleFields(bitMap, parsedMessage);

	}

	private void getAvaialbleFields(String bitMap, Message parsedMessage) {
		char[] charArray = bitMap.toCharArray();
		int fieldNumber = 1;

		for(char a : charArray) {

			if (a == '1') {
				if (fieldNumber == 1) {
					fieldNumber++;
					continue;
				} else {
					String fieldNumberString = String.valueOf(fieldNumber);
					String fieldValue;
					String fieldName = ParsingUtil.getFieldDefinitionString(fieldDefinitions, fieldNumberString,
							ISOParsingConstants.NAME);
					// String type = ParsingUtil.getFieldDefinitionString(fieldDefinitions,
					// fieldNumberString, ISOParsingConstants.TYPE);
					int length = ParsingUtil.getFieldDefinitionInteger(fieldDefinitions, fieldNumberString,
							ISOParsingConstants.LENGTH);
					int varNoOfDigits = ParsingUtil.getFieldDefinitionInteger(fieldDefinitions, fieldNumberString,
							"VarNoOfDigits");
					Boolean hasSubFields = ParsingUtil.getFieldDefinitionBoolean(fieldDefinitions, fieldNumberString,
							ISOParsingConstants.HAS_SUB_FIELDS);

					if (varNoOfDigits == 0) {
						fieldValue = parsedMessage.getMessageData().substring(0, length);
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("message data : " + parsedMessage.getMessageData());
							LOGGER.debug("Field Name : " + fieldName + " | value :" + fieldValue);
							LOGGER.debug("setting message data : " + parsedMessage.getMessageData().substring(length,
									parsedMessage.getMessageData().length()));
						}
						parsedMessage.setMessageData(parsedMessage.getMessageData().substring(length,
								parsedMessage.getMessageData().length()));
					} else {

						fieldValue = parsedMessage.getMessageData().substring(varNoOfDigits, varNoOfDigits
								+ (Integer.parseInt(parsedMessage.getMessageData().substring(0, varNoOfDigits))));
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("message data : " + parsedMessage.getMessageData());
							LOGGER.debug("Field Name : " + fieldName + " | value :" + fieldValue);

							LOGGER.debug("setting message data : " + parsedMessage.getMessageData().substring(
									varNoOfDigits + (Integer
											.parseInt(parsedMessage.getMessageData().substring(0, varNoOfDigits))),
									parsedMessage.getMessageData().length()));
						}
						parsedMessage.setMessageData(parsedMessage.getMessageData().substring(varNoOfDigits
								+ (Integer.parseInt(parsedMessage.getMessageData().substring(0, varNoOfDigits))),
								parsedMessage.getMessageData().length()));
					}
					if (hasSubFields) {
						HashMap<String, Object> subFieldMap = new HashMap<>();
						ParsingUtil.getSubFieldDefinition(fieldDefinitions, fieldNumberString);
						ParsingEngineHelper.getSubFields(fieldValue, subFieldMap,
								ParsingUtil.getSubFieldDefinition(fieldDefinitions, fieldNumberString),
								fieldNumberString, 1);
						fieldNumber++;
						parsedMessage.setMessageFields(fieldName, subFieldMap);
						continue;
					}

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("getAvaialbleFields() fieldName :" + fieldName + " | fieldValue : " + fieldValue);
					}
					parsedMessage.setMessageFields(fieldName, fieldValue);

				}
			}
			fieldNumber++;
		}
	}

	public static HashMap<String, String> parseField48and54(String fieldValue) {
		HashMap<String, String> parsedValue = new HashMap<String, String>();
		String valuesToParse = fieldValue;
		for (int j = 0; j < valuesToParse.length() + j; j++) {
			if (valuesToParse.length() < 7) {
				break;
			} else {
				String key = valuesToParse.substring(0, 3);
				int keyLength = Integer.parseInt(valuesToParse.substring(3, 6));
				String value = valuesToParse.substring(6, 6 + keyLength);
				parsedValue.put(key, value);
				valuesToParse = valuesToParse.substring(6 + keyLength, valuesToParse.length());
			}
		}
		return parsedValue;
	}

	private static JsonObject getATMConfiguration(String fileName) {
		JsonObject fieldDefinitions = null;
		try {
			StringBuilder path = new StringBuilder();
			String configLocation = GetConfigLocation.getUBConfigLocation();
			String ubLocation = "conf/business/atm/";
			path.append(configLocation);
			path.append(ubLocation);
			path.append(fileName);
			JsonParser parser = new JsonParser();
			fieldDefinitions = (JsonObject) parser.parse(new FileReader(String.valueOf(path)));
		} catch (Exception exception) {
			LOGGER.info(exception.getCause() != null ? ExceptionUtils.getStackTrace(exception.getCause())
					: ExceptionUtils.getStackTrace(exception));
		}
		return fieldDefinitions;
	}

	private static String packField(List<Integer> fieldID) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		Collections.sort(fieldID);
		// [to construct secondary bit map]
		// Set the Size to 128 to hold the Maximum fields that can be present for
		// ISO8583 Message.
		BitSet bitValues = new BitSet(128);
		for (Integer pos : fieldID) {
			bitValues.set(pos - 1);
		}
		// Group the Fields in size of 4 and assign a hexa values. The primary bitmap
		// field on ISO8583 holds 16 char representing field from
		// 1 to 64 and secondary bitmap hold next 16 char from 65 to 128. This means
		// that the primary and secondary bitmap will represent the
		// the entire field set. This may not be the elegant way, as it is based on the
		// document shared by PROSA page 13 Primary BitMap Section.
		int bitpos = 0;
		int bitgroup = bitValues.size() / 4;
		for (int iPos = 0; iPos < bitgroup; iPos++) {
			int npos = 0;
			if (bitValues.get(bitpos++))
				npos += 8;
			if (bitValues.get(bitpos++))
				npos += 4;
			if (bitValues.get(bitpos++))
				npos += 2;
			if (bitValues.get(bitpos++))
				npos++;
			byteArrayOutputStream.write(HEXAVALUE[npos]);
		}

		return byteArrayOutputStream.toString();
	}

	private static String unpackFields(String bitMap) {
		String bitMapValue = new BigInteger(bitMap, 16).toString(2);
		return String.format("%64s", bitMapValue).replace(ISOParsingConstants.SPACE, ISOParsingConstants.ZERO);
	}
}
