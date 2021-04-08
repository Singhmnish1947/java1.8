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

import bf.com.misys.cbs.types.ExtractAccountTypeDetail;
import bf.com.misys.cbs.types.ExtractBasicProdDtlsOutput;
import bf.com.misys.cbs.types.ProductSummaryDtls;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractBasicProdDtlsRs;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.bo.refimpl.IBOProductInheritance;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_ExtractProductTypes;

public class ExtractProductAccTypes extends AbstractUB_TIP_ExtractProductTypes {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}
	private static final Log logger = LogFactory
			.getLog(ExtractProductAccTypes.class.getName());

	private IPersistenceObjectsFactory factory;

	String FECTH_PRODUCTACC_DETAILS = " SELECT IPI."
			+ IBOProductInheritance.PRODUCTDESCRIPTION + " AS "
			+ IBOProductInheritance.PRODUCTDESCRIPTION + " , IPI."
			+ IBOProductInheritance.CRINT_INTERESTBASEDAYSCR + " AS "
			+ IBOProductInheritance.CRINT_INTERESTBASEDAYSCR + " , IPI."
			+ IBOProductInheritance.DRINT_INTERESTBASEDAYSDR + " AS "
			+ IBOProductInheritance.DRINT_INTERESTBASEDAYSDR + " , IPI."
			+ IBOProductInheritance.PRODUCT_ACC_PRODUCTID + " AS "
			+ IBOProductInheritance.PRODUCT_ACC_PRODUCTID + " , IPI."
			+ IBOProductInheritance.ACC_ISOCURRENCYCODE + " AS "
			+ IBOProductInheritance.ACC_ISOCURRENCYCODE + " , IPI."
			+ IBOProductInheritance.UBDOCCHECKLISTDEFID + " AS "
			+ IBOProductInheritance.UBDOCCHECKLISTDEFID + " , IPI."
			+ IBOProductInheritance.PRODUCTCONTEXTCODE + " AS "
			+ IBOProductInheritance.PRODUCTCONTEXTCODE + " FROM "
			+ IBOProductInheritance.BONAME + " IPI " + " WHERE IPI."
			+ IBOProductInheritance.PRODUCTCONTEXTCODE + " = ? ";

	String productContextCode = CommonConstants.EMPTY_STRING;
	String crudMode = CommonConstants.EMPTY_STRING;

	public ExtractProductAccTypes(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {

		try {
			factory = BankFusionThreadLocal.getPersistanceFactory();

			ExtractBasicProdDtlsRs extractBasicProdDtlsRs = new ExtractBasicProdDtlsRs();
			extractBasicProdDtlsRs.setRsHeader(new RsHeader());
			extractBasicProdDtlsRs.getRsHeader().setMessageType("Product"); 
			ExtractBasicProdDtlsOutput extractBasicProdDtlsOutput = new ExtractBasicProdDtlsOutput();
			ExtractAccountTypeDetail[] extractAccountTypeDetail = new ExtractAccountTypeDetail[1];
			extractAccountTypeDetail[0] = new ExtractAccountTypeDetail();
			ProductSummaryDtls productSummaryDtls = new ProductSummaryDtls();

			crudMode = getF_IN_mode();
			productContextCode = getF_IN_extractBasicProdDtlsRq()
					.getExtractBasicProdDtlsInput().getProductId();
			List<SimplePersistentObject> fetchProductData = fetchProductDetails();

			if (fetchProductData != null && fetchProductData.size() > 0) {
				for (SimplePersistentObject productData : fetchProductData) {

					productSummaryDtls
							.setProductDescription((String) productData
									.getDataMap()
									.get(
											IBOProductInheritance.PRODUCTDESCRIPTION));
					productSummaryDtls.setProductID((String) productData
							.getDataMap().get(
									IBOProductInheritance.PRODUCTCONTEXTCODE));
					productSummaryDtls.setIsoCurrencyCode((String) productData
							.getDataMap().get(
									IBOProductInheritance.ACC_ISOCURRENCYCODE));
					productSummaryDtls.setCheckListID((String) productData
							.getDataMap().get(
									IBOProductInheritance.UBDOCCHECKLISTDEFID));
					productSummaryDtls.setProductName((String) productData
							.getDataMap()
							.get(IBOProductInheritance.PRODUCT_ACC_PRODUCTID));

					/*
					 * Below fields Not Available
					 * productSummaryDtls.setHostExtension(hostExtension);
					 * productSummaryDtls.setIsActive(isActive);
					 * productSummaryDtls.setProductCategory(productCategory);
					 * productSummaryDtls.setProductType(productType);
					 * productSummaryDtls.setStartDate(startDate);
					 * productSummaryDtls.setUserExtension(userExtension);;
					 */

				}
			}
			extractAccountTypeDetail[0].setCrudMode(crudMode);
			if (fetchProductData != null) {
				extractAccountTypeDetail[0].setDrInterestDaysBasis(String.valueOf(
						fetchProductData.get(0).getDataMap().get(IBOProductInheritance.DRINT_INTERESTBASEDAYSDR)));
				extractAccountTypeDetail[0].setCrInterestDaysBasis(String.valueOf(
						fetchProductData.get(0).getDataMap().get(IBOProductInheritance.CRINT_INTERESTBASEDAYSCR)));

			}					
			/*
			 * Below fields Not Available
			 * extractAccountTypeDetail[0].setHostExtension(hostExtension);
			 * extractAccountTypeDetail[0].setIsContingent(isContingent)
			 * extractAccountTypeDetail[0].setIsInternal(isInternal)
			 * extractAccountTypeDetail
			 * [0].setIsValidForSettlement(isValidForSettlement);
			 */
			extractAccountTypeDetail[0]
					.setProductSummaryDtls(productSummaryDtls);
			extractBasicProdDtlsOutput
					.setExtractAccountTypeDetail(extractAccountTypeDetail);
			extractBasicProdDtlsRs
					.setExtractBasicProdDtlsOutput(extractBasicProdDtlsOutput);
			setF_OUT_extractBasicProdDtlsRs(extractBasicProdDtlsRs);
		} catch (Exception e) {
			logger
					.error("Error in ExtractProductAccTypes.java for Primary Key "
							+ productContextCode + " Error is " + ExceptionUtil.getExceptionAsString(e));

			throw new BankFusionException(e);
		}

	}

	private List<SimplePersistentObject> fetchProductDetails() {
		ArrayList<String> params = new ArrayList<String>();
		params.add(productContextCode);
		return factory.executeGenericQuery(FECTH_PRODUCTACC_DETAILS, params,
				null, true);

	}
}
