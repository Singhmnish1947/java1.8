/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: CB_TIP_ExchangeRateTypes,v.1.0,April 20, 2012 11:35:34 AM Ayyappa
 *
 */
package com.misys.ub.extract;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.types.AttributeChange;
import bf.com.misys.cbs.types.ExtractCriticalChngDtl;
import bf.com.misys.cbs.types.ExtractCriticalChngOutput;
import bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractCriticalChngRs;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.bo.refimpl.IBOCBVW_GENERICCODE;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_ExtractCustomerAccMobility;

public class ExtractCustomerAccMobility extends
		AbstractUB_TIP_ExtractCustomerAccMobility {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";

	private static final Log logger = LogFactory
			.getLog(ExtractCustomerAccMobility.class.getName());

	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private IPersistenceObjectsFactory factory;

	String FECTH_ExtractPartyTypes_DETAILS = " SELECT IER."
			+ IBOCBVW_GENERICCODE.CODETYPE + " AS "
			+ IBOCBVW_GENERICCODE.CODETYPE + " , IER."
			+ IBOCBVW_GENERICCODE.CODETYPEID + " AS "
			+ IBOCBVW_GENERICCODE.CODETYPEID + " , IER."
			+ IBOCBVW_GENERICCODE.DESCRIPTION + " AS "
			+ IBOCBVW_GENERICCODE.DESCRIPTION + " , IER."
			+ IBOCBVW_GENERICCODE.SUBCODETYPE + " AS "
			+ IBOCBVW_GENERICCODE.SUBCODETYPE + " , IER."
			+ IBOCBVW_GENERICCODE.ENTITY_TYPE + " AS "
			+ IBOCBVW_GENERICCODE.ENTITY_TYPE + " , IER."
			+ IBOCBVW_GENERICCODE.LOCALEID + " AS "
			+ IBOCBVW_GENERICCODE.LOCALEID + " FROM "
			+ IBOCBVW_GENERICCODE.BONAME + " IER " + " WHERE IER."
			+ IBOCBVW_GENERICCODE.CODETYPEID + " = ?";

	String partyTypeID = CommonConstants.EMPTY_STRING;
	String crudMode = CommonConstants.EMPTY_STRING;

	public ExtractCustomerAccMobility(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {

		try {
			factory = BankFusionThreadLocal.getPersistanceFactory();

			ExtractCriticalChngRs extractCriticalChngRs = new ExtractCriticalChngRs();
			ExtractCriticalChngOutput extractCriticalChngOutput = new ExtractCriticalChngOutput();
			ExtractCriticalChngDtl[] extractCriticalChngDtls = new ExtractCriticalChngDtl[2];
			extractCriticalChngDtls[0] = new ExtractCriticalChngDtl();
			AttributeChange[] attributeChange = new AttributeChange[2];
			attributeChange[0] = new AttributeChange();

			crudMode = getF_IN_mode();

			partyTypeID = getF_IN_extractCriticalChngRq()
					.getExtractCriticalChngInput().getAttributeName();

			List<SimplePersistentObject> extractCustAccMobTypes = fetchExtractPartyTypes();

			if (extractCustAccMobTypes != null
					&& extractCustAccMobTypes.size() > 0) {
				for (SimplePersistentObject custAccMobTypes : extractCustAccMobTypes) {
					/*
					 * attributeChange[0].setAttributeName(attributeName);
					 * attributeChange[0].setNewValue(newValue);
					 * attributeChange[0].setOldValue(oldValue);
					 */

				}
			}
			extractCriticalChngDtls[0].setCrudMode(crudMode);
			extractCriticalChngDtls[0].setAttributeChange(attributeChange[0]);
			extractCriticalChngOutput
					.setExtractCriticalChngDtls(extractCriticalChngDtls);
			extractCriticalChngRs
					.setExtractCriticalChngOutput(extractCriticalChngOutput);
			setF_OUT_extractCriticalChngRs(extractCriticalChngRs);
		} catch (Exception e) {

			logger
					.error("Error in ExtractCustomerAccMobility.java for Primary Key "
							+ partyTypeID + " Error is " + ExceptionUtil.getExceptionAsString(e));

			throw new BankFusionException(e);

		}
	}

	private List<SimplePersistentObject> fetchExtractPartyTypes() {
		ArrayList<String> params = new ArrayList<String>();
		params.add(partyTypeID);
		return factory.executeGenericQuery(FECTH_ExtractPartyTypes_DETAILS,
				params, null, true);
	}
}
