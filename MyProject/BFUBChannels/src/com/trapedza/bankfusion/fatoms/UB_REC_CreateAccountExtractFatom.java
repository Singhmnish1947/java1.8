/**
 * 
 */
package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.bankfusion.attributes.PagedQuery;
import bf.com.misys.cbs.types.AccountKeys;
import bf.com.misys.cbs.types.ExtractIntrnlAcctDtl;
import bf.com.misys.cbs.types.ExtractIntrnlAcctDtlsOutput;
import bf.com.misys.cbs.types.Pseudonym;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractIntrnlAcctDtlsRq;
import bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractIntrnlAcctDtlsRs;

import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.features.FeatureIDs;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_REC_CreateAccountExtract;
import com.trapedza.bankfusion.steps.refimpl.IUB_REC_CreateAccountExtract;

/**
 * @author bhanupratap.singh
 * 
 */
@SuppressWarnings("unused")
public class UB_REC_CreateAccountExtractFatom extends AbstractUB_REC_CreateAccountExtract implements IUB_REC_CreateAccountExtract {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("deprecation")
	public UB_REC_CreateAccountExtractFatom(BankFusionEnvironment env) {
		super(env);
	}

	private static final Log LOGGER = LogFactory.getLog(UB_REC_CreateAccountExtractFatom.class.getName());
	private static final String GET_NOSTRO_PRODUCTS_MFID = "UB_CNF_GetListOfProductsForGivenFeature_SRV";
	private static final String GET_NOSTRO_PRODUCTS_OUTPUT = "LIST_OF_PRODUCTS";
	private static final Object FEATUREID = "FEATUREID";
	private static final String PRODUCTID = "PRODUCT_PRODUCTNAME";
	private static final String PRODUCTDESCRIPTION = "PRODUCT_PRODUCTDESCRIPTION";
	private static final String PRODUCTNAME = "PRODUCT_PRODUCTLABEL";
	List<SimplePersistentObject> listofAccounts = null;
	private static String getAccountsOfProduct = "SELECT A." + IBOAccount.ACCOUNTID + " AS "
			+ CommonConstants.getTagName(IBOAccount.ACCOUNTID) + ",A." + IBOAccount.ACCOUNTNAME + " AS "
			+ CommonConstants.getTagName(IBOAccount.ACCOUNTNAME) + ",A." + IBOAccount.ACCOUNTDESCRIPTION + " AS "
			+ CommonConstants.getTagName(IBOAccount.ACCOUNTDESCRIPTION) + ",A." + IBOAccount.PRODUCTID + " AS "
			+ CommonConstants.getTagName(IBOAccount.PRODUCTID) + ",A." + IBOAccount.ISOCURRENCYCODE + " AS "
			+ CommonConstants.getTagName(IBOAccount.ISOCURRENCYCODE) + ",A." + IBOAccount.CUSTOMERCODE + " AS "
			+ CommonConstants.getTagName(IBOAccount.CUSTOMERCODE) + " " + CommonConstants.FROM + " " + IBOAccount.BONAME + " A "
			+ CommonConstants.WHERE + " A." + IBOAccount.PRODUCTID + " = ?";

	private static String getSwiftAccount = "SELECT S." + IBOSwtCustomerDetail.SWTACCOUNTNO + " AS "
			+ CommonConstants.getTagName(IBOSwtCustomerDetail.SWTACCOUNTNO) + ",S." + IBOSwtCustomerDetail.BICCODE + " AS " + CommonConstants.getTagName(IBOSwtCustomerDetail.BICCODE) + " " + CommonConstants.FROM + " "
			+ IBOSwtCustomerDetail.BONAME + " S " + CommonConstants.WHERE + " S." + IBOSwtCustomerDetail.CUSTOMERCODE + " = ?";

	@SuppressWarnings("unchecked")
	public void process(BankFusionEnvironment env) {

		ExtractIntrnlAcctDtlsRq extractInternalAcctDtlsRq = getF_IN_extractIntrnlAcctDtlsRq();
		ExtractIntrnlAcctDtlsRs extractInternalAcctDtlsRs = new ExtractIntrnlAcctDtlsRs();
		ExtractIntrnlAcctDtlsOutput extractInternalAcctDtlsOutput = new ExtractIntrnlAcctDtlsOutput();
		ArrayList<ExtractIntrnlAcctDtl> extractIntrnlAcctDtls = new ArrayList<ExtractIntrnlAcctDtl>();

		HashMap inputParams = new HashMap();
		inputParams.put(FEATUREID, FeatureIDs.NOSTROFEATURE);
		HashMap OutputParams = MFExecuter.executeMF(GET_NOSTRO_PRODUCTS_MFID, env, inputParams);
		VectorTable listOfProducts = (VectorTable) OutputParams.get(GET_NOSTRO_PRODUCTS_OUTPUT);
		int noOfNostroProducts = listOfProducts.size();
		HashMap productListMap = new HashMap();

		String productID = null;
		String customerCode = null;
		if (noOfNostroProducts != 0) {
			for (int i = 0; i < noOfNostroProducts; i++) {

				productListMap = listOfProducts.getRowTags(i);
				productID = (productListMap.get(PRODUCTID)).toString();
				ArrayList params = new ArrayList();
				params.add(productID);
				listofAccounts = BankFusionThreadLocal.getPersistanceFactory().executeGenericQuery(getAccountsOfProduct, params, null,
						false);
				Iterator<SimplePersistentObject> accListItr = listofAccounts.iterator();
				if (listofAccounts.size() != 0) {
					while (accListItr.hasNext()) {
						AccountKeys accountKeys = new AccountKeys();
						Pseudonym pseudonym = new Pseudonym();
						ExtractIntrnlAcctDtl extractInternalAcctDtl = new ExtractIntrnlAcctDtl();
						SimplePersistentObject simplePersistenceObject = accListItr.next();
						Map accountRow = simplePersistenceObject.getDataMap();
						accountKeys.setStandardAccountId((String) accountRow.get(CommonConstants.getTagName(IBOAccount.ACCOUNTID)));
						accountKeys.setExternalAccountId((String) accountRow.get(CommonConstants.getTagName(IBOAccount.ACCOUNTID)));
						pseudonym.setIsoCurrencyCode((String) accountRow.get(CommonConstants.getTagName(IBOAccount.ISOCURRENCYCODE)));
						accountKeys.setPseudonym(pseudonym);
						extractInternalAcctDtl.setAccountKeys(accountKeys);
						extractInternalAcctDtl.setCrudMode("Read");
						extractInternalAcctDtl.setAccountName((String) accountRow.get(CommonConstants.getTagName(IBOAccount.ACCOUNTNAME)));
						customerCode = (String) accountRow.get(CommonConstants.getTagName(IBOAccount.CUSTOMERCODE));
						ArrayList params2 = new ArrayList();
						params2.add(customerCode);
						List<SimplePersistentObject> swtCustomerDetails = BankFusionThreadLocal.getPersistanceFactory()
								.executeGenericQuery(getSwiftAccount, params2, null, false);
						extractInternalAcctDtl.setIsSwiftSendAcc(Boolean.TRUE);
						if (swtCustomerDetails.size() != 0) {
							SimplePersistentObject swtCustomerDetail = swtCustomerDetails.get(0);

							extractInternalAcctDtl.setSenderBIC((String) (swtCustomerDetail.getDataMap()).get(CommonConstants
									.getTagName(IBOSwtCustomerDetail.BICCODE)));
							extractInternalAcctDtl.setExternalAccountId((String) (swtCustomerDetail.getDataMap()).get(CommonConstants
									.getTagName(IBOSwtCustomerDetail.SWTACCOUNTNO)));
						}
						extractIntrnlAcctDtls.add(extractInternalAcctDtl);
					}
				}
			}
		}
		extractInternalAcctDtlsOutput.setExtractMode("Full");
		int listSize = extractIntrnlAcctDtls.size();
		ExtractIntrnlAcctDtl[] extIntrlAccDtlArray = new ExtractIntrnlAcctDtl[listSize];
		Iterator<ExtractIntrnlAcctDtl> i = extractIntrnlAcctDtls.iterator();
		int j = 0;
		while (i.hasNext()) {
			ExtractIntrnlAcctDtl extIntrnlAccDtl = i.next();
			ExtractIntrnlAcctDtl extractIntrnlAcctDtlElement = new ExtractIntrnlAcctDtl();
			extractIntrnlAcctDtlElement.setAccountKeys(extIntrnlAccDtl.getAccountKeys());
			extractIntrnlAcctDtlElement.setExternalAccountId(extIntrnlAccDtl.getExternalAccountId());
			extractIntrnlAcctDtlElement.setAccountName(extIntrnlAccDtl.getAccountName());
			extractIntrnlAcctDtlElement.setCrudMode(extIntrnlAccDtl.getCrudMode());
			extractIntrnlAcctDtlElement.setInternalAcctType(extIntrnlAccDtl.getInternalAcctType());
			extractIntrnlAcctDtlElement.setCustomerName(extIntrnlAccDtl.getCustomerName());
			extractIntrnlAcctDtlElement.setReceiverBIC(extIntrnlAccDtl.getReceiverBIC());
			extractIntrnlAcctDtlElement.setSenderBIC(extIntrnlAccDtl.getSenderBIC());
			extractIntrnlAcctDtlElement.setIsSwiftRecAcc(extIntrnlAccDtl.getIsSwiftRecAcc());
			extractIntrnlAcctDtlElement.setIsSwiftSendAcc(extIntrnlAccDtl.getIsSwiftSendAcc());
			extractIntrnlAcctDtlElement.setStatementDetails(extIntrnlAccDtl.getStatementDetails());
			extractIntrnlAcctDtlElement.setProfileDtls(extIntrnlAccDtl.getProfileDtls());
			extractIntrnlAcctDtlElement.setHostExtension(extIntrnlAccDtl.getHostExtension());
			extractIntrnlAcctDtlElement.setUserExtension(extIntrnlAccDtl.getUserExtension());
			extIntrlAccDtlArray[j] = extractIntrnlAcctDtlElement;
			j++;
		}
		extractInternalAcctDtlsOutput.setExtractIntrnlAcctDtls(extIntrlAccDtlArray);
		extractInternalAcctDtlsRs.setExtractIntrnlAcctDtlsOutput(extractInternalAcctDtlsOutput);
		extractInternalAcctDtlsRs.setPagingInfo(new PagedQuery());
		extractInternalAcctDtlsRs.setRsHeader(new RsHeader());
		setF_OUT_extractIntrnlAcctDtlsRs(extractInternalAcctDtlsRs);
	}
}
