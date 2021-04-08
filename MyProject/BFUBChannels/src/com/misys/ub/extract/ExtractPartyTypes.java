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

import bf.com.misys.cbs.types.ExtractPartyTypesDtl;
import bf.com.misys.cbs.types.ExtractPartyTypesOutput;
import bf.com.misys.cbs.types.GcCodeDetails;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractPartyTypesRs;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.bo.refimpl.IBOCBVW_GENERICCODE;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_ExtractPartyTypes;

public class ExtractPartyTypes extends AbstractUB_TIP_ExtractPartyTypes {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";

	private static final Log logger = LogFactory.getLog(ExtractPartyTypes.class
			.getName());
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

	public ExtractPartyTypes(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {

		try {
			factory = BankFusionThreadLocal.getPersistanceFactory();

			ExtractPartyTypesRs extractPartyTypesRs = new ExtractPartyTypesRs();
			extractPartyTypesRs.setRsHeader(new RsHeader());
			extractPartyTypesRs.getRsHeader().setMessageType("CustomerTypes");
			ExtractPartyTypesOutput extractPartyTypesOutput = new ExtractPartyTypesOutput();
			ExtractPartyTypesDtl[] extractPartyTypesDtl = new ExtractPartyTypesDtl[1];
			extractPartyTypesDtl[0] = new ExtractPartyTypesDtl();
			GcCodeDetails gcCodeDetails = new GcCodeDetails();

			crudMode = getF_IN_mode();
			partyTypeID = getF_IN_extractPartyTypesRq()
					.getExtractPartyTypesInput().getPartyTypeId();

			List<SimplePersistentObject> extractPartyTypes = fetchExtractPartyTypes();
			if (extractPartyTypes != null && extractPartyTypes.size() > 0) {
				for (SimplePersistentObject extractPartyType : extractPartyTypes) {
					gcCodeDetails.setCodeDescription((String) extractPartyType
							.getDataMap().get(IBOCBVW_GENERICCODE.DESCRIPTION));
					gcCodeDetails.setCodeReference((String) extractPartyType
							.getDataMap().get(IBOCBVW_GENERICCODE.CODETYPE));
					gcCodeDetails.setCodeValue((String) extractPartyType
							.getDataMap().get(IBOCBVW_GENERICCODE.CODETYPEID));
					// gcCodeDetails.setHostExtension(hostExtension);

				}
			}

			extractPartyTypesDtl[0].setCrudMode(crudMode);
			// extractPartyTypesDtl[0].setPartyLocality(extractPartyTypes.get(0).getDataMap().get(IBOCBVW_GENERICCODE.LOCALEID));
			extractPartyTypesDtl[0].setGcCodeDetails(gcCodeDetails);
			extractPartyTypesOutput
					.setExtractPartyTypesDtls(extractPartyTypesDtl);
			extractPartyTypesRs
					.setExtractPartyTypesOutput(extractPartyTypesOutput);
			setF_OUT_extractPartyTypesRs(extractPartyTypesRs);
		} catch (Exception e) {

			logger.error("Error in ExtractPartyTypes.java for Primary Key "
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
