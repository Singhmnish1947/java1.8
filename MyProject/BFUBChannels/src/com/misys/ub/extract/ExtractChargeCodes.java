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

import bf.com.misys.cbs.types.ExtractPLChgCodeDetail;
import bf.com.misys.cbs.types.ExtractPLChgCodeOutput;
import bf.com.misys.cbs.types.OnlineChgCalcDtls;
import bf.com.misys.cbs.types.OnlineChgKeyDtls;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractPLChgCodeRs;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.bo.refimpl.IBOChargeCode;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_ExtractChargeCodes;

public class ExtractChargeCodes extends AbstractUB_TIP_ExtractChargeCodes {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private static final Log logger = LogFactory
			.getLog(ExtractChargeCodes.class.getName());

	private IPersistenceObjectsFactory factory;

	String FECTH_CHARGECODE_DETAILS = " SELECT IER." + IBOChargeCode.CHARGECODE
			+ " AS " + IBOChargeCode.CHARGECODE + " , IER."
			+ IBOChargeCode.CHARGECODEDESC + " AS "
			+ IBOChargeCode.CHARGECODEDESC + " , IER."
			+ IBOChargeCode.CHARGETYPEID + " AS " + IBOChargeCode.CHARGETYPEID
			+ " , IER." + IBOChargeCode.CHARGENARRATIVE + " AS "
			+ IBOChargeCode.CHARGENARRATIVE + " , IER."
			+ IBOChargeCode.CHARGEBASISID + " AS "
			+ IBOChargeCode.CHARGEBASISID + " , IER."
			+ IBOChargeCode.FUNDINDICATOR + " AS "
			+ IBOChargeCode.FUNDINDICATOR + " , IER." + IBOChargeCode.TAXCODE
			+ " AS " + IBOChargeCode.TAXCODE + " FROM " + IBOChargeCode.BONAME
			+ " IER " + " WHERE IER." + IBOChargeCode.CHARGECODE + " = ? ";

	String chargeCode = CommonConstants.EMPTY_STRING;
	String crudMode = CommonConstants.EMPTY_STRING;

	public ExtractChargeCodes(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {

		try {
			factory = BankFusionThreadLocal.getPersistanceFactory();

			ExtractPLChgCodeRs extractPLChgCodeRs = new ExtractPLChgCodeRs();
			extractPLChgCodeRs.setRsHeader(new RsHeader());
			extractPLChgCodeRs.getRsHeader().setMessageType("Charge"); 
			ExtractPLChgCodeOutput extractPLChgCodeOutput = new ExtractPLChgCodeOutput();
			ExtractPLChgCodeDetail[] extractPLChgCodeDetail = new ExtractPLChgCodeDetail[1];
			extractPLChgCodeDetail[0] = new ExtractPLChgCodeDetail();

			OnlineChgCalcDtls onlineChgCalcDtls = new OnlineChgCalcDtls();
			OnlineChgKeyDtls onlineChgKeyDtls = new OnlineChgKeyDtls();

			crudMode = getF_IN_mode();
			chargeCode = getF_IN_extractPLChgCodeRq()
					.getExtractPLChgCodeInput().getChargeCodeId();

			List<SimplePersistentObject> exchangeRateData = fetchChargeCodeDetails();

			if (exchangeRateData != null && exchangeRateData.size() > 0) {
				for (SimplePersistentObject exhangeRate : exchangeRateData) {
					// OnlineChgCalcDtls data is not available

					/*
					 * onlineChgKeyDtls.setAccount(account);
					 * onlineChgKeyDtls.setAccountStyle(accountStyle);
					 * onlineChgKeyDtls.setChannelId(channelId);
					 * onlineChgKeyDtls.setOnlineChgId();
					 * onlineChgKeyDtls.setChargeLevel(chargeLevel)
					 * onlineChgKeyDtls.setWalkIn(walkIn);
					 * onlineChgKeyDtls.setEventCategory(eventCategory);
					 * onlineChgKeyDtls.setEventSubCategory(eventSubCategory);
					 * onlineChgKeyDtls.setHostExtension(hostExtension);
					 * onlineChgKeyDtls.setIsApply(isApply);
					 * onlineChgKeyDtls.setPartySubType(partySubType);
					 * onlineChgKeyDtls.setProduct(product);
					 * onlineChgKeyDtls.setProductCategory(productCategory);
					 * onlineChgKeyDtls.setTxnCurrency(txnCurrency);
					 */
					onlineChgKeyDtls.setChargeCodeId(chargeCode);
					onlineChgKeyDtls.setChargeType((String) exhangeRate
							.getDataMap().get(IBOChargeCode.CHARGETYPEID));
					onlineChgKeyDtls.setChargeBasis((String) exhangeRate
							.getDataMap().get(IBOChargeCode.CHARGEBASISID));
					onlineChgKeyDtls
							.setInsufficientFundsInd((String) exhangeRate
									.getDataMap().get(
											IBOChargeCode.FUNDINDICATOR));
					onlineChgKeyDtls.setOnlineChargeDesc((String) exhangeRate
							.getDataMap().get(IBOChargeCode.CHARGECODEDESC));
					onlineChgKeyDtls.setChargeCodeId((String) exhangeRate
							.getDataMap().get(IBOChargeCode.CHARGECODE));

				}
			}else{
				onlineChgKeyDtls.setChargeCodeId(chargeCode);
			}
			extractPLChgCodeDetail[0].setCrudMode(crudMode);
			extractPLChgCodeDetail[0].setOnlineChgCalcDtls(onlineChgCalcDtls);
			extractPLChgCodeDetail[0].setOnlineChgKeyDtls(onlineChgKeyDtls);
			/*
			 * userExtension is not available
			 * extractPLChgCodeDetail[0].setUserExtension(userExtension);
			 */
			extractPLChgCodeOutput
					.setExtractPLChgCodeDetail(extractPLChgCodeDetail);
			extractPLChgCodeRs
					.setExtractPLChgCodeOutput(extractPLChgCodeOutput);
			setF_OUT_extractPLChgCodeRs(extractPLChgCodeRs);
		} catch (Exception e) {
			logger.error("Error in ExtractChargeCodes.java for Primary Key "
					+ chargeCode + " Error is " + ExceptionUtil.getExceptionAsString(e));

			throw new BankFusionException(e);

		}

	}

	private List<SimplePersistentObject> fetchChargeCodeDetails() {
		ArrayList<String> params = new ArrayList<String>();
		params.add(chargeCode);
		return factory.executeGenericQuery(FECTH_CHARGECODE_DETAILS, params,
				null, true);

	}
}
