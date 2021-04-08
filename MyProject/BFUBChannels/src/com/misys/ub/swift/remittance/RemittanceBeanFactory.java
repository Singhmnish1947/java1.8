package com.misys.ub.swift.remittance;

import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;

import com.misys.ub.common.GetUBConfigLocation;

public class RemittanceBeanFactory {

    private final static String CONF = "conf/swift/";
    public static final String REMITTANCE_CONFIG_XML = "remittance-config.xml";
    
    @SuppressWarnings("deprecation")
    static XmlBeanFactory factory = new XmlBeanFactory(new FileSystemResource(
            GetUBConfigLocation.getUBConfigLocation() + CONF  + REMITTANCE_CONFIG_XML));

    @SuppressWarnings("deprecation")
    public static XmlBeanFactory getFactory() {
        return factory;
    }
}
