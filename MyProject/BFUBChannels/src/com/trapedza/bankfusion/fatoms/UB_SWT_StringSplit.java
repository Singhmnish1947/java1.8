/* **********************************************************
 * Copyright (c) 2008 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/

package com.trapedza.bankfusion.fatoms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_StringSplit;
/**
 * @author Gaurav Aggarwal
 * 
 */
public class UB_SWT_StringSplit extends AbstractUB_SWT_StringSplit {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private transient final static Log logger = LogFactory.getLog(UB_SWT_StringSplit.class.getName());

	String AccountNumber=CommonConstants.EMPTY_STRING;
	String Text=CommonConstants.EMPTY_STRING;
	String TagValue=CommonConstants.EMPTY_STRING;
	String StringType = CommonConstants.EMPTY_STRING;


	public UB_SWT_StringSplit(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}
	/*
	 * To Initialise the value of the Party identifier
	 */
	/*private void init() {
	 * 
	}*/	
	/*
	 *Start of the Process Function and to set the output of the class.
	 */
	public void process(BankFusionEnvironment environment) {

		String NCCCode = CommonConstants.EMPTY_STRING;
		String IdentifierCode = CommonConstants.EMPTY_STRING;
		StringType = getF_IN_StringSplitType();
		TagValue = getF_IN_TagValue();
		if(StringType.equals("PARTYIDENTIFIER")){
			if(!isStringEmpty(TagValue)){
				if(TagValue.startsWith("//")){
					Text=TagValue.substring(2,4);
					AccountNumber=TagValue.substring(4, TagValue.length());
				}
				else{
					AccountNumber =TagValue;
				}	
			}
		}
		else{
			SplitTagValue(getF_IN_TagValue());
		}
		setF_OUT_AccountNo(AccountNumber);
		setF_OUT_Text(Text);

	}
	/*
	 * Starting point of the Split function which will split the Party Identifier in the Account Number,
	 *  Identifier Code or Name of the Institution.
	 *  
	 */
	private void SplitTagValue(String TagValue)
	{
		if(TagValue.startsWith("/"))
		{
			if(TagValue.indexOf("$")!=-1){
				AccountNumber=TagValue.substring(1, TagValue.indexOf("$"));	
				logger.info(AccountNumber);
				Text=TagValue.substring((TagValue.indexOf("$")+1), TagValue.length());
			}
			else {
				AccountNumber=TagValue.substring(1, TagValue.length());	

			}
			String delimeter="[$]";
			int [] limits = {0};
			for (int limit : limits) 
			{
				String [] tokens = TagValue.split(delimeter, limit);
				logger.info(tokens.length);
				if (tokens.length > 1) {
					Text = tokens[1];
				}
			}
		}
		else{
			String delimeter="[$]";
			int [] limits = {0};
			for (int limit : limits) 
			{
				String [] tokens = TagValue.split(delimeter, limit);
				logger.info(tokens.length);
				Text = tokens[0];
			}
		}
	}
	
	boolean isStringEmpty(String str){
		return str.equals(CommonConstants.EMPTY_STRING);
	}
}

