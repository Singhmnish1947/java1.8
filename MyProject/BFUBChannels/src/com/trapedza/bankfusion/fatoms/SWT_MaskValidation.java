/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_MaskValidation;

/**
 * @author prasanthj
 * 
 */
public class SWT_MaskValidation extends AbstractSWT_MaskValidation {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	private transient final static Log logger = LogFactory.getLog(SWT_MaskValidation.class.getName());

	/**
	 * @param env
	 */
	public SWT_MaskValidation(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractSWTMaskValidation#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 */
	public void process(BankFusionEnvironment env) {

		final String inputExpr = getF_IN_mask_format();

		final String expected_pattern = getF_IN_expected_format_pattern();

		Pattern pattern = Pattern.compile(expected_pattern);

		Matcher matcher = pattern.matcher(inputExpr);

		// Format supports only a fixed allowed format
		if (!matcher.matches()) {
//			throw new BankFusionException(9416, new String[] { getF_IN_mask_format() }, logger, env);
			EventsHelper.handleEvent(ChannelsEventCodes.E_MASK_FORMAT_NOT_SUPPORTED, new String[] { getF_IN_mask_format() }, new HashMap(), env);
		}

	}

}
