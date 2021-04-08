package com.misys.ub.dc.handler.helper;

import com.misys.bankfusion.common.ComplexTypeConvertor;
import com.misys.bankfusion.common.util.BankFusionPropertySupport;

public class PartyCreateHandlerHelper {
    private static final String COLUMN_SEPARATOR = "PARTY_COLUMN_SEPARATOR";
    private static final String DEFAULT_COLUMN_SEPARATOR = ";";
    public static final String PARTY_COLUMN_SEPARATOR = BankFusionPropertySupport.getProperty(COLUMN_SEPARATOR,
            DEFAULT_COLUMN_SEPARATOR);
    private static final String KEY_KEYVALUE_SEPARTOR = "PARTY_KEY_VALUE_SEPARATOR";
    private static final String DEFAULTVALUE_KEY_VALUE_SEPARATOR = "=";
    public static final String PARTY_KEY_VALUE_SEPARATOR = BankFusionPropertySupport.getProperty(KEY_KEYVALUE_SEPARTOR,
            DEFAULTVALUE_KEY_VALUE_SEPARATOR);
    private static final String KEY_SUB_RECORD_SEPARATOR_START = "PARTY_SUB_RECORD_SEPARATOR_START";
    public static final String DEFAULT_SUB_RECORD_SEPARATOR_START = "{";
    public static final String REPETEABLE_BLOCK_OPEN_SEPARATOR = BankFusionPropertySupport
            .getProperty(KEY_SUB_RECORD_SEPARATOR_START, DEFAULT_SUB_RECORD_SEPARATOR_START);
    private static final String KEY_SUB_RECORD_SEPARATOR_END = "PARTY_SUB_RECORD_SEPARATOR_END";
    public static final String DEFAULT_SUB_RECORD_SEPARATOR_END = "}";
    public static final String REPETEABLE_BLOCK_CLOSE_SEPARATOR = BankFusionPropertySupport
            .getProperty(KEY_SUB_RECORD_SEPARATOR_END, DEFAULT_SUB_RECORD_SEPARATOR_END);
    public static final String PARTY_DOC_IDENTIFIER = "PT_PFN_PartyDocumentData";
    public static final String PARTY_FIELD_KEY = "PARTY_FIELDS";
    public static final String INPUT_REPETEABLE_BLOCK_IDENTIFIER = "REPEATABLE_BLOCKS";
    public static final String INPUT_DOC_IDENTIFIER = "DOCUMENTS";
    public static final String KEY_PARTYID_IDENTIFIER = "PT_PFN_Party#PARTYID";
    public static final String KEY_UNIQUEID_IDENTIFIER = "UNIQUE_ID";
    public static final String KEY_FATCA_IDENTIFIER = "PT_PFN_FatcaDetails#ISREPORTINGREQUIRED";
    public static final String IS_USATAXRESIDENCE = "PT_PFN_FatcaDetails#ISUSATAXRESIDENT";
    public static final String IS_USACOUNTRYRESIDENCE = "PT_PFN_FatcaDetails#ISUSACOUNTRYRESIDENT";
    public static final String KEY_RESIDENTCOUNTRY = "PT_PFN_PersonalDetails#RESIDENTCOUNTRY";
    
    public static final String TAXSUBCLASSIFICATION_USPERSONPARTICIPATING = "USPERSONPARTICIPATING";
    public static final String TAXSUBCLASSIFICATION_USPERSONNONPARTICIPATE = "USPERSONNONPARTICIPATE";
    public static final String TAXSUBCLASSIFICATION_NONUSPERSONPARTICIPATE = "NONUSPERSONPARTICIPATE";
    public static final String TAXSUBCLASSIFICATION_NONUSPERSONNONPARTICIPATE = "NONUSPERSONNONPARTICIPATE";
      
    public static String getXML(Object obj, String objType) {
        @SuppressWarnings("deprecation")
        ComplexTypeConvertor converter = new ComplexTypeConvertor(PartyCreateHandlerHelper.class.getClassLoader());
        return converter.getXmlFromJava(objType, obj);
    }
}
