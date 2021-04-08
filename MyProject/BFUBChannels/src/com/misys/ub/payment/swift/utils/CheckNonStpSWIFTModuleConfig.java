package com.misys.ub.payment.swift.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CheckNonStpSWIFTModuleConfig {
	
	private transient final static Log logger = LogFactory.getLog(CheckNonStpSWIFTModuleConfig.class.getName());
	public boolean checkNonStpModuleConfig(String channel)
    {
		boolean nonStp = false;
		PaymentSwiftUtils paymentSwiftUtils = new PaymentSwiftUtils();
        switch(channel){
        	case PaymentSwiftConstants.CHANNELID_IBI:
        		nonStp = Boolean.valueOf(paymentSwiftUtils.getModuleConfigValue(PaymentSwiftConstants.MODULE_VALUE_DC,PaymentSwiftConstants.CHANNELID_SWIFT));
        		break;
        	case PaymentSwiftConstants.CHANNELID_CCI:
        		nonStp = Boolean.valueOf(paymentSwiftUtils.getModuleConfigValue(PaymentSwiftConstants.MODULE_VALUE_CC,PaymentSwiftConstants.CHANNELID_SWIFT));	
        		break;
        	case PaymentSwiftConstants.CHANNELID_TELLER:
        		nonStp = Boolean.valueOf(paymentSwiftUtils.getModuleConfigValue(PaymentSwiftConstants.MODULE_VALUE_TELLER,PaymentSwiftConstants.CHANNELID_SWIFT));	
        		break;
        	case PaymentSwiftConstants.CHANNEL_UXP:	
        		break;
        	default:
        		nonStp = Boolean.valueOf(paymentSwiftUtils.getModuleConfigValue("OUTGOING_"+channel+"_NON_STP",PaymentSwiftConstants.CHANNELID_SWIFT));     	
        }	
        return nonStp;
    }
	
}
