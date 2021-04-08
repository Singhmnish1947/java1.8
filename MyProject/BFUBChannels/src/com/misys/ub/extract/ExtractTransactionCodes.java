package com.misys.ub.extract;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.types.ExtractTxnCodesOutput;
import bf.com.misys.cbs.types.ExtractTxnDetail;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractTxnCodesRs;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_ExtractTransactionCodes;

public class ExtractTransactionCodes extends
		AbstractUB_TIP_ExtractTransactionCodes {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private static final Log logger = LogFactory
			.getLog(ExtractTransactionCodes.class.getName());
	private IPersistenceObjectsFactory factory;

	String FECTH_TRANSACTION_DETAILS = " SELECT " + IBOMisTransactionCodes.CODE
			+ " AS " + IBOMisTransactionCodes.CODE + " , "
			+ IBOMisTransactionCodes.DESCRIPTION + " AS "
			+ IBOMisTransactionCodes.DESCRIPTION + " , "
			+ IBOMisTransactionCodes.WASTECODE + " AS "
			+ IBOMisTransactionCodes.WASTECODE + " , "
			+ IBOMisTransactionCodes.VERSIONNUM + " AS "
			+ IBOMisTransactionCodes.VERSIONNUM + " , "
			+ IBOMisTransactionCodes.CASHTRANS + " AS "
			+ IBOMisTransactionCodes.CASHTRANS + " , "
			+ IBOMisTransactionCodes.NUMERICTRANSCODE + " AS "
			+ IBOMisTransactionCodes.NUMERICTRANSCODE + " , "
			+ IBOMisTransactionCodes.SYSTEMTRANS + " AS "
			+ IBOMisTransactionCodes.SYSTEMTRANS + " , "
			+ IBOMisTransactionCodes.POSTINGMESSAGETYPE + " AS "
			+ IBOMisTransactionCodes.POSTINGMESSAGETYPE + " , "
			+ IBOMisTransactionCodes.ZEROAMOUNTTRANS + " AS "
			+ IBOMisTransactionCodes.ZEROAMOUNTTRANS + " , "
			+ IBOMisTransactionCodes.FORWARDTRANS + " AS "
			+ IBOMisTransactionCodes.FORWARDTRANS + " , "
			+ IBOMisTransactionCodes.ADVICEFORMATCODE + " AS "
			+ IBOMisTransactionCodes.ADVICEFORMATCODE + " , "
			+ IBOMisTransactionCodes.LOCALCURRENCYONLY + " AS "
			+ IBOMisTransactionCodes.LOCALCURRENCYONLY + " , "
			+ IBOMisTransactionCodes.REVERSALTRANS + " AS "
			+ IBOMisTransactionCodes.REVERSALTRANS + " , "
			+ IBOMisTransactionCodes.NOTONSTATEMENTS + " AS "
			+ IBOMisTransactionCodes.NOTONSTATEMENTS + " , "
			+ IBOMisTransactionCodes.BLOCKINGCONTROL + " AS "
			+ IBOMisTransactionCodes.BLOCKINGCONTROL + " , "
			+ IBOMisTransactionCodes.BLOCKINGDAYS + " AS "
			+ IBOMisTransactionCodes.BLOCKINGDAYS + " , "
			+ IBOMisTransactionCodes.IGNORESERVICEFEES + " AS "
			+ IBOMisTransactionCodes.IGNORESERVICEFEES + " , "
			+ IBOMisTransactionCodes.TAKEWHATYOUCAN + " AS "
			+ IBOMisTransactionCodes.TAKEWHATYOUCAN + " , "
			+ IBOMisTransactionCodes.EXCHANGERATETYPE + " AS "
			+ IBOMisTransactionCodes.EXCHANGERATETYPE + " , "
			+ IBOMisTransactionCodes.DONTUPDATELIMIT + " AS "
			+ IBOMisTransactionCodes.DONTUPDATELIMIT + " , "
			+ IBOMisTransactionCodes.BANKCHEQUEISSUE + " AS "
			+ IBOMisTransactionCodes.BANKCHEQUEISSUE + " , "
			+ IBOMisTransactionCodes.FORCENOTICE + " AS "
			+ IBOMisTransactionCodes.FORCENOTICE + " , "
			+ IBOMisTransactionCodes.AUTOREFPREFIXSUFFIX + " AS "
			+ IBOMisTransactionCodes.AUTOREFPREFIXSUFFIX + " , "
			+ IBOMisTransactionCodes.PREFIXSUFFIX + " AS "
			+ IBOMisTransactionCodes.PREFIXSUFFIX + " , "
			+ IBOMisTransactionCodes.LOANPENALTYINTEREST + " AS "
			+ IBOMisTransactionCodes.LOANPENALTYINTEREST + " , "
			+ IBOMisTransactionCodes.PRODUCTIDNEWACC + " AS "
			+ IBOMisTransactionCodes.PRODUCTIDNEWACC + " , "
			+ IBOMisTransactionCodes.POSTPOSTINGBPID + " AS "
			+ IBOMisTransactionCodes.POSTPOSTINGBPID + " , "
			+ IBOMisTransactionCodes.NEWACCOUNTBPID + " AS "
			+ IBOMisTransactionCodes.NEWACCOUNTBPID + " , "
			+ IBOMisTransactionCodes.NEWACCOUNTCONTROL + " AS "
			+ IBOMisTransactionCodes.NEWACCOUNTCONTROL + " , "
			+ IBOMisTransactionCodes.DORMANCYACTIVATIONCODE + " AS "
			+ IBOMisTransactionCodes.DORMANCYACTIVATIONCODE + " , "
			+ IBOMisTransactionCodes.INCLUDEFORSTATISTICS + " AS "
			+ IBOMisTransactionCodes.INCLUDEFORSTATISTICS + " , "
			+ IBOMisTransactionCodes.DORMANCYPOSTINGACTION + " AS "
			+ IBOMisTransactionCodes.DORMANCYPOSTINGACTION + " , "
			+ IBOMisTransactionCodes.BUNDLESFIFLAG + " AS "
			+ IBOMisTransactionCodes.BUNDLESFIFLAG + " , "
			+ IBOMisTransactionCodes.SWTELEMSGMNEMONIC + " AS "
			+ IBOMisTransactionCodes.SWTELEMSGMNEMONIC + " , "
			+ IBOMisTransactionCodes.SWTDRCRCONFIRMATION + " AS "
			+ IBOMisTransactionCodes.SWTDRCRCONFIRMATION + " , "
			+ IBOMisTransactionCodes.SWTMESSAGETYPE + " AS "
			+ IBOMisTransactionCodes.SWTMESSAGETYPE + " , "
			+ IBOMisTransactionCodes.ROUNDINGMETHOD + " AS "
			+ IBOMisTransactionCodes.ROUNDINGMETHOD + " , "
			+ IBOMisTransactionCodes.MULTIPLEOFCONSTANT + " AS "
			+ IBOMisTransactionCodes.MULTIPLEOFCONSTANT

			+ " FROM " + IBOMisTransactionCodes.BONAME + "  WHERE "
			+ IBOMisTransactionCodes.CODE + " = ?";

	String crudMode = CommonConstants.EMPTY_STRING;
	String transactionCodeId = CommonConstants.EMPTY_STRING;

	public ExtractTransactionCodes(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {

		try {
			factory = BankFusionThreadLocal.getPersistanceFactory();
			ExtractTxnCodesRs extractTxnCodesRs = new ExtractTxnCodesRs();
			extractTxnCodesRs.setRsHeader(new RsHeader());
			extractTxnCodesRs.getRsHeader().setMessageType("Transaction"); 
			ExtractTxnCodesOutput extractTxnCodesOutput = new ExtractTxnCodesOutput();
			ExtractTxnDetail[] extractTxnDetail = new ExtractTxnDetail[1];
			extractTxnDetail[0] = new ExtractTxnDetail();
			crudMode = getF_IN_crudMode();
			transactionCodeId = getF_IN_extractTxnCodesRq()
					.getExtractTxnCodesInput().getTxnCodeKey();
			List<SimplePersistentObject> transactionDataList = fetchTransactionDetails();
			if (transactionDataList != null && transactionDataList.size() > 0) {
				for (SimplePersistentObject transactionData : transactionDataList) {
					extractTxnDetail[0]
							.setDebitCreditFlag((CommonConstants.EMPTY_STRING + transactionData
									.getDataMap()
									.get(
											IBOMisTransactionCodes.SIGNVERIFICATIONFLAG)));
					/*
					 * extractTxnDetail[0].setInterestMethod((((String)
					 * transactionData
					 * .getDataMap().get(IBOMisTransactionCodes.)));
					 */

					extractTxnDetail[0]
							.setInterestRateBasis(((String) transactionData
									.getDataMap()
									.get(
											IBOMisTransactionCodes.EXCHANGERATETYPE)));
					extractTxnDetail[0].setMnemonic(((String) transactionData
							.getDataMap().get(
									IBOMisTransactionCodes.SWTELEMSGMNEMONIC)));
					extractTxnDetail[0]
							.setNumericCode(((Integer) transactionData
									.getDataMap()
									.get(
											IBOMisTransactionCodes.NUMERICTRANSCODE)));
					/*
					 * extractTxnDetail[0] .setReversalCode(((String)
					 * transactionData.getDataMap()
					 * .get(IBOMisTransactionCodes.)));
					 */
					extractTxnDetail[0]
							.setTxnCodeDescription(((String) transactionData
									.getDataMap().get(
											IBOMisTransactionCodes.DESCRIPTION)));
					extractTxnDetail[0].setTxnCodeKey(((String) transactionData
							.getDataMap().get(IBOMisTransactionCodes.CODE)));
					/*
					 * extractTxnDetail[0].setTxnCodeValue(((String)
					 * transactionData
					 * .getDataMap().get(IBOMisTransactionCodes.)))
					 */

				}
			} else {
				extractTxnDetail[0].setTxnCodeKey(transactionCodeId);
			}
			extractTxnDetail[0].setCrudMode(crudMode);
			extractTxnCodesOutput.setExtractTxnDetails(extractTxnDetail);

			extractTxnCodesRs.setExtractTxnCodesOutput(extractTxnCodesOutput);

			setF_OUT_extractTxnCodesRs(extractTxnCodesRs);
		} catch (Exception e) {

			logger
					.error("Error in ExtractTransactionCodes.java for Primary Key "
							+ transactionCodeId + " Error is " + ExceptionUtil.getExceptionAsString(e));

			throw new BankFusionException(e);
		}

	}

	private List<SimplePersistentObject> fetchTransactionDetails() {
		ArrayList<String> params = new ArrayList<String>();
		params.add(transactionCodeId);
		return factory.executeGenericQuery(FECTH_TRANSACTION_DETAILS, params,
				null, true);

	}
}
