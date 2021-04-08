/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_MaskFormatValidation;

public class UB_SWT_MaskFormatValidation extends
		AbstractUB_SWT_MaskFormatValidation {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private transient final static Log logger = LogFactory.getLog(UB_SWT_MaskFormatValidation.class
			.getName());
	public UB_SWT_MaskFormatValidation(BankFusionEnvironment env) {
		super(env);

	}
	public void process(BankFusionEnvironment env) {
		String MaskFormat="";
			if(getF_IN_MaskInput().startsWith("//")){
				if(getF_IN_MaskInput().length()>=4)
					MaskFormat=getMaskFormat(getF_IN_MaskInput().substring(0, 4),env);	
				setF_OUT_IsValidFormat(validateText(MaskFormat,getF_IN_MaskInput().substring(4)));
		}/*else if(getF_IN_MaskInput().startsWith("/")){
			if(getF_IN_MaskInput().length()>=1)
				MaskFormat=getMaskFormat(getF_IN_MaskInput().substring(0, 1),env);
			setF_OUT_IsValidFormat(validateText(MaskFormat,getF_IN_MaskInput().substring(1)));
		}*/else{
			setF_OUT_IsValidFormat(new Boolean(true));
		}
	}
	private String getMaskFormat(String inputString,BankFusionEnvironment env){
		 Map inputParams = new HashMap();
		 inputParams.put("MaskCode", inputString);
		 Map result= MFExecuter.executeMF("UB_SWT_MaskCodeRead_SRV", env, inputParams);
		 
		 return(String) result.get("MaskFormat");
		
	}
	private boolean validateText(String f_mask_format, String inputString) {
		char c = ' ';

		String pattern = "(";
		for (int i = 0; i < f_mask_format.length(); i++) {
			c = f_mask_format.charAt(i);

			if (c == 'X') {
				pattern = pattern + "[(A-Z)||(0-9)]";
			}
			else if (c == 'x') {
				pattern = pattern + "[(a-z)||(0-9)]";
			}
			else if (c == 'n') {
				pattern = pattern + "[(0-9)|| (\\s)]";
			}
			else if (c == 'N') {
				pattern = pattern + "[(0-9)]";
			}

		}
		pattern = pattern + ")";

		Pattern patternMatch = Pattern.compile(pattern);
		Matcher matcher = patternMatch.matcher(inputString);
		if (matcher.matches()) {
			return true;
		}
		else {
			return false;
		}
	}
	
}
