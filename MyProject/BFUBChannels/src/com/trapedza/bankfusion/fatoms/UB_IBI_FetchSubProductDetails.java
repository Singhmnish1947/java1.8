/**
 * 
 */
package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOProductInheritance;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_IBI_EnabledSubProducts;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_IBI_FetchSubProductDetails;

/**
 * @author asrivast
 * 
 */
public class UB_IBI_FetchSubProductDetails extends
		AbstractUB_IBI_FetchSubProductDetails {

	public static final String svnRevision = "$Revision: 1.0 $";

	private static final String PRODUCTID = "PRODUCTINHERITENCE_PRODUCTID";
	private static final String PRODUCTCONTEXTCODE = "PRODUCTINHERITENCE_PRODUCTCONTEXTCODE";
	private static final String PRODUCTDESCRIPTION = "PRODUCTINHERITENCE_PRODUCTDESCRIPTION";
	String inputProductID = CommonConstants.EMPTY_STRING;
	String isoCurrencyCode = CommonConstants.EMPTY_STRING;
	private static final String WHERE_CLAUSE_AVAILABLE_SUB_PRODUCTS_FOR_ENABLE = "WHERE "
			+ IBOProductInheritance.PRODUCTCONTEXTCODE
			+ " NOT IN (SELECT "
			+ IBOUB_IBI_EnabledSubProducts.PRODUCTCONTEXTCODE
			+ " FROM "
			+ IBOUB_IBI_EnabledSubProducts.BONAME
			+ " WHERE "
			+ IBOUB_IBI_EnabledSubProducts.ISIBIENABLED
			+ " = 'Y')"
			+ " AND "
			+ IBOProductInheritance.PRODUCT_ACC_PRODUCTID
			+ "= ? AND "
			+ IBOProductInheritance.ACC_ISOCURRENCYCODE + "= ?";

	private static final String WHERE_CLAUSE_AVAILABLE_SUB_PRODUCTS_FOR_DISABLE = "WHERE "
			+ IBOProductInheritance.PRODUCTCONTEXTCODE
			+ " IN (SELECT "
			+ IBOUB_IBI_EnabledSubProducts.PRODUCTCONTEXTCODE
			+ " FROM "
			+ IBOUB_IBI_EnabledSubProducts.BONAME
			+ " WHERE "
			+ IBOUB_IBI_EnabledSubProducts.ISIBIENABLED
			+ " = 'Y')"
			+ " AND "
			+ IBOProductInheritance.PRODUCT_ACC_PRODUCTID
			+ "= ? AND "
			+ IBOProductInheritance.ACC_ISOCURRENCYCODE + "= ?";

	private static final String WHERE_CLAUSE_ENABLED_SUB_PRODUCTS = "WHERE "
			+ IBOProductInheritance.PRODUCTCONTEXTCODE + " IN (SELECT "
			+ IBOUB_IBI_EnabledSubProducts.PRODUCTCONTEXTCODE + " FROM "
			+ IBOUB_IBI_EnabledSubProducts.BONAME + " WHERE "
			+ IBOUB_IBI_EnabledSubProducts.ISIBIENABLED + " = 'Y')" + " AND "
			+ IBOProductInheritance.PRODUCT_ACC_PRODUCTID + "= ? AND "
			+ IBOProductInheritance.ACC_ISOCURRENCYCODE + "= ?";

	/*
	 * isEnable will be Enable when mode="true"
	 */
	Boolean isEnable = Boolean.FALSE;

	public UB_IBI_FetchSubProductDetails(BankFusionEnvironment env) {
		super(env);

	}

	public void process(BankFusionEnvironment env) {

		inputProductID = getF_IN_ProductID();
		isoCurrencyCode = getF_IN_IsoCurrencyCode();
		/*
		 * mode will be Enable when mode="true"
		 */
		isEnable = isF_IN_Mode();
		ArrayList params = new ArrayList();
		params.add(inputProductID);
		params.add(isoCurrencyCode);
		VectorTable enabledSubProducts = new VectorTable();
		VectorTable availableSubProducts = new VectorTable();
		Map productDetails;

		IPersistenceObjectsFactory factory = BankFusionThreadLocal
				.getPersistanceFactory();
		List<IBOProductInheritance> listOfAvailableSubProducts;

		if (isEnable) {

			listOfAvailableSubProducts = factory.findByQuery(
					IBOProductInheritance.BONAME,
					WHERE_CLAUSE_AVAILABLE_SUB_PRODUCTS_FOR_ENABLE, params, null,
					false);
		} else {
			listOfAvailableSubProducts = factory.findByQuery(
					IBOProductInheritance.BONAME,
					WHERE_CLAUSE_AVAILABLE_SUB_PRODUCTS_FOR_DISABLE, params, null,
					false);
		}

		List<IBOProductInheritance> listOfEnabledSubProducts = factory
				.findByQuery(IBOProductInheritance.BONAME,
						WHERE_CLAUSE_ENABLED_SUB_PRODUCTS, params, null, false);
		for (IBOProductInheritance product : listOfEnabledSubProducts) {
			productDetails = new Hashtable();
			productDetails.put(PRODUCTCONTEXTCODE, product.getBoID());
			productDetails.put(PRODUCTDESCRIPTION, product
					.getF_PRODUCTDESCRIPTION());
			productDetails.put(PRODUCTID, product.getF_PRODUCT_ACC_PRODUCTID());
			enabledSubProducts.addAll(new VectorTable(productDetails));

		}

		for (IBOProductInheritance product : listOfAvailableSubProducts) {
			productDetails = new Hashtable();
			productDetails.put(PRODUCTCONTEXTCODE, product.getBoID());
			productDetails.put(PRODUCTDESCRIPTION, product
					.getF_PRODUCTDESCRIPTION());
			productDetails.put(PRODUCTID, product.getF_PRODUCT_ACC_PRODUCTID());
			productDetails.put(CommonConstants.SELECT, Boolean.FALSE);
			availableSubProducts.addAll(new VectorTable(productDetails));

		}

		setF_OUT_AvailableSubProducts(availableSubProducts);
		setF_OUT_IBIEnabledSubProducts(enabledSubProducts);
	}
}
