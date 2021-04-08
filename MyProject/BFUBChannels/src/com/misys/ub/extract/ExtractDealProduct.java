package com.misys.ub.extract;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.types.AccountBasicDetails;
import bf.com.misys.cbs.types.AccountKeys;
import bf.com.misys.cbs.types.ExtractBasicFXDealOutput;
import bf.com.misys.cbs.types.FxDealBasicDtls;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractBasicFXDealRs;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.bo.refimpl.IBOFxDealType;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_ExtractDealProduct;

public class ExtractDealProduct extends AbstractUB_TIP_ExtractDealProduct {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";

	private static final Log logger = LogFactory
			.getLog(ExtractDealProduct.class.getName());
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private IPersistenceObjectsFactory factory;

	String FECTH_DEAL_DETAILS = " SELECT IC." + IBOFxDealType.FXDEALTYPEID
			+ " AS " + IBOFxDealType.FXDEALTYPEID + " , IC."
			+ IBOFxDealType.BUYCURRENYTOBASEEXRATETYPE + " AS "
			+ IBOFxDealType.BUYCURRENYTOBASEEXRATETYPE + " , IC."
			+ IBOFxDealType.CHARGECODE + " AS " + IBOFxDealType.CHARGECODE
			+ " , IC." + IBOFxDealType.DEALCODE + " AS "
			+ IBOFxDealType.DEALCODE + " , IC." + IBOFxDealType.DEALDESCRIPTION
			+ " AS " + IBOFxDealType.DEALDESCRIPTION + " , IC."
			+ IBOFxDealType.FXDEALTYPEID + " AS " + IBOFxDealType.FXDEALTYPEID
			+ " , IC." + IBOFxDealType.PRODUCTID + " AS "
			+ IBOFxDealType.PRODUCTID + " , IC."
			+ IBOFxDealType.REVALUATIONLOSSACCOUNT + " AS "
			+ IBOFxDealType.REVALUATIONLOSSACCOUNT + " , IC."
			+ IBOFxDealType.REVALUATIONPROFITACCOUNT + " AS "
			+ IBOFxDealType.REVALUATIONPROFITACCOUNT + " , IC."
			+ IBOFxDealType.SELLCURRENYTOBASEEXRATETYPE + " AS "
			+ IBOFxDealType.SELLCURRENYTOBASEEXRATETYPE + " , IC."
			+ IBOFxDealType.UBISAMLCHECKREQUIRED + " AS "
			+ IBOFxDealType.UBISAMLCHECKREQUIRED + " FROM "
			+ IBOFxDealType.BONAME + " IC " + " WHERE IC."
			+ IBOFxDealType.FXDEALTYPEID + " = ?";

	String crudMode = CommonConstants.EMPTY_STRING;
	String fxDealCode = CommonConstants.EMPTY_STRING;

	public ExtractDealProduct(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {

		try {
			factory = BankFusionThreadLocal.getPersistanceFactory();
			ExtractBasicFXDealRs extractBasicFXDealRs = new ExtractBasicFXDealRs();
			extractBasicFXDealRs.setRsHeader(new RsHeader());
			extractBasicFXDealRs.getRsHeader().setMessageType("DealProduct");
			ExtractBasicFXDealOutput extractBasicFXDealOutput = new ExtractBasicFXDealOutput();
			FxDealBasicDtls fxDealBasicDtls = new FxDealBasicDtls();
			crudMode = getF_IN_crudMode();
			fxDealCode = getF_IN_extractBasicFXDealRq()
					.getExtractBasicFXDealInput().getDealTxnType();
			List<SimplePersistentObject> dealDataList = fetchDealDetails();
			if (dealDataList != null && dealDataList.size() > 0) {
				for (SimplePersistentObject dealData : dealDataList) {
					/*
					 * BigDecimal bd= new BigDecimal ((String) dealData
					 * .getDataMap().get(
					 * IBOFxDealType.BUYCURRENYTOBASEEXRATETYPE));
					 * fxDealBasicDtls.setBaseEquivalent(bd);
					 */
					// fxDealBasicDtls.setBranchCode(branchCode);
					// fxDealBasicDtls.setBranchMargin(branchMargin)
					/*
					 * fxDealBasicDtls.setBuyAmount((BigDecimal) dealData
					 * .getDataMap().get(IBOFxDealType. ))
					 */

					AccountBasicDetails ac = new AccountBasicDetails();
					ac.setAccountKeys(new AccountKeys());
					ac.getAccountKeys().setStandardAccountId(
							(String) dealData.getDataMap().get(
									IBOFxDealType.REVALUATIONPROFITACCOUNT));
					fxDealBasicDtls.setCrAccountDetails(ac);
					/*
					 * fxDealBasicDtls.setCustomerMargin((String) dealData
					 * .getDataMap().get(IBOFxDealType. ))
					 */
					/*
					 * fxDealBasicDtls.setCustShortDetails((String) dealData
					 * .getDataMap().get(IBOFxDealType. ))
					 */
					fxDealBasicDtls
							.setDealReference(CommonConstants.EMPTY_STRING
									+ dealData.getDataMap().get(
											IBOFxDealType.DEALCODE));
					fxDealBasicDtls.setDealTxnType((String) dealData
							.getDataMap().get(IBOFxDealType.FXDEALTYPEID));

					AccountBasicDetails acDebit = new AccountBasicDetails();
					acDebit.setAccountKeys(new AccountKeys());
					acDebit.getAccountKeys().setStandardAccountId(
							(String) dealData.getDataMap().get(
									IBOFxDealType.REVALUATIONLOSSACCOUNT));
					fxDealBasicDtls.setDrAccountDetails(acDebit);
					/*
					 * fxDealBasicDtls.setExchangeRateDetails(exchangeRateDetails
					 * )
					 */
					/*
					 * fxDealBasicDtls.setLinkedDealRef(linkedDealRef)
					 * fxDealBasicDtls.setNettingValues(nettingValues)
					 * fxDealBasicDtls.setPostingDate(postingDate)
					 * fxDealBasicDtls.setSellAmount((String) dealData
					 * .getDataMap().get(IBOFxDealType. ))
					 * fxDealBasicDtls.setToleranceDifference
					 * (toleranceDifference)
					 * fxDealBasicDtls.setTransactionDesc((String) dealData
					 * .getDataMap().get(IBOFxDealType.DEALDESCRIPTION ));
					 * fxDealBasicDtls.setTxnAction(vTxnActionArray)
					 * fxDealBasicDtls.setNarrative(vNarrativeArray)
					 * fxDealBasicDtls.setTxnRefOther(txnRefOther)
					 * fxDealBasicDtls.setTxnTypeCategory(txnTypeCategory)
					 */
					/*
					 * fxDealBasicDtls.setValueDate((String) dealData
					 * .getDataMap().get(IBOFxDealType.DEALCODE ))
					 */
				}
			} else {
				fxDealBasicDtls.setDealReference(fxDealCode);
			}

			extractBasicFXDealOutput.setFxDealBasicData(fxDealBasicDtls);
			extractBasicFXDealOutput.setExtractMode(crudMode);
			extractBasicFXDealRs
					.setExtractBasicFXDealOutput(extractBasicFXDealOutput);

			setF_OUT_extractBasicFXDealRs(extractBasicFXDealRs);
		} catch (Exception e) {

			logger.error("Error in ExtractDealProduct.java for Primary Key "
					+ fxDealCode + " Error is " + ExceptionUtil.getExceptionAsString(e));

			throw new BankFusionException(e);
		}

	}

	private List<SimplePersistentObject> fetchDealDetails() {
		ArrayList<String> params = new ArrayList<String>();
		params.add(fxDealCode);
		return factory.executeGenericQuery(FECTH_DEAL_DETAILS, params, null,
				true);

	}
}
