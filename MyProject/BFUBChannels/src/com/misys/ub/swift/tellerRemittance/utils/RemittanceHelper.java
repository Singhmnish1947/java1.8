package com.misys.ub.swift.tellerRemittance.utils;

import org.apache.commons.lang.StringUtils;

import com.misys.bankfusion.common.ComplexTypeConvertor;

public class RemittanceHelper {

    private RemittanceHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @param str
     * @return
     */
    public static String checkNullValue(String str) {
        String output = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(str)) {
            output = str;
        }
        return output;
    }

    /**
     * @param str
     * @return
     */
    public static String get35CharacterTextLine(String str) {
        String spaceConstant = " ";
        String output = StringUtils.EMPTY;
        if (!StringUtils.isBlank(str) && !str.equals(spaceConstant)) {
            if (str.length() <= 35) {
                output = str.substring(0, str.length());
            }
            else {
                output = str.substring(0, 35);
            }
        }
        return output;
    }

    /**
     * @param str
     * @return
     */
    public static String get100CharacterTextLine(String str) {
        String output = StringUtils.EMPTY;
        if (!StringUtils.isBlank(str)) {
            if (str.length() <= 100) {
                output = str.substring(0, str.length());
            }
            else {
                output = str.substring(0, 100);
            }
        }
        return output;
    }

    /**
     * Method Description:Get Xml from complex type object
     * 
     * @param obj
     * @return
     */
    public static String getXmlFromComplexType(Object obj) {
        ComplexTypeConvertor complexConverter = new ComplexTypeConvertor(obj.getClass().getClassLoader());
        return complexConverter.getXmlFromJava(obj.getClass().getName(), obj);
    }
}
