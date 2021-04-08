/**
 * 
 */
package com.trapedza.bankfusion.fatoms;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.GUIDGen;
import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.common.util.BankFusionIOSupport;
import com.misys.bankfusion.subsystem.persistence.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.bo.refimpl.IBOUBTB_TXNUDFDATA;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TXN_CreateOrUpdateUDFData;
import com.trapedza.bankfusion.steps.refimpl.IUB_TXN_CreateOrUpdateUDFData;

/**
 * This class has been used for recording UDF / HDF data for transaction.
 * 
 * @author abpurwar
 * 
 */
public class UB_TXN_CreateOrUpdateUDFData extends AbstractUB_TXN_CreateOrUpdateUDFData implements IUB_TXN_CreateOrUpdateUDFData {
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 5429545029934046901L;
	/**
	 * Logger object for doing logging
	 */
	private transient static final Log LOGGER = LogFactory.getLog(UB_TXN_CreateOrUpdateUDFData.class.getName());
	/**
	 * Constants
	 */
	private static final int INT_ONE_CONST = 1;
	private static final int INT_TWO_CONST = 2;
	private static final int INT_THREE_CONST = 3;
	private static final int INT_FOUR_CONST = 4;
	/**
	 * Persistence factory reference
	 */
	IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

	/**
	 * Default Constructor for class
	 */
	public UB_TXN_CreateOrUpdateUDFData() {
		super();
	}

	/**
	 * Parameterized constructor for class
	 * 
	 * @param env
	 */
	@SuppressWarnings("deprecation")
	public UB_TXN_CreateOrUpdateUDFData(BankFusionEnvironment env) {
		super(env);
	}

	/*
	 * (non-Javadoc)
	 * 
	 *
	 */
	public void process(BankFusionEnvironment env) {
		String txnReference = getF_IN_txnReference();
		String txnCode = getF_IN_txnCode();
		Object udfFieldData = null, hdfFieldData = null;
	
		//for screen 1
		udfFieldData = getF_IN_udfField1();
		hdfFieldData = getF_IN_hdfField1();
		createTxnUDFObject(txnCode, txnReference, udfFieldData, hdfFieldData, INT_ONE_CONST);
		//for screen 2
		udfFieldData = getF_IN_udfField2();
		hdfFieldData = getF_IN_hdfField2();
		createTxnUDFObject(txnCode, txnReference, udfFieldData, hdfFieldData, INT_TWO_CONST);
		//for screen 3
		udfFieldData = getF_IN_udfField3();
		hdfFieldData = getF_IN_hdfField3();
		createTxnUDFObject(txnCode, txnReference, udfFieldData, hdfFieldData, INT_THREE_CONST);
		//for screen 4
		udfFieldData = getF_IN_udfField4();
		hdfFieldData = getF_IN_hdfField4();
		createTxnUDFObject(txnCode, txnReference, udfFieldData, hdfFieldData, INT_FOUR_CONST);
	}

	/**
	 * This method has been used for creating transaction udf object with
	 * txnCode, txnReference and udf data.
	 * 
	 * @param txnCode
	 * @param txnReference
	 * @param udfFieldData
	 * @param hdfFieldData
	 * @return
	 */
	private void createTxnUDFObject(String txnCode, String txnReference, Object udfFieldData, Object hdfFieldData, int id) {
		boolean isUDFEnabled = validateUDFExists(udfFieldData);
		boolean isHDFEnabled = validateHDFExists(hdfFieldData);
		//check if udf and hdf exists in the input request. 
		if (isUDFEnabled || isHDFEnabled) {
			IBOUBTB_TXNUDFDATA txnUDFData = (IBOUBTB_TXNUDFDATA) factory.getStatelessNewInstance(IBOUBTB_TXNUDFDATA.BONAME);
			txnUDFData.setBoID(GUIDGen.getNewGUID());
			txnUDFData.setF_TXNREF(txnReference);
			txnUDFData.setF_TXNCODE(txnCode);
			txnUDFData.setF_TXNSCREENCOUNT(id);
			if (isUDFEnabled) {
				txnUDFData.setF_UDFFIELD(BankFusionIOSupport.convertToBytes(udfFieldData));
			}
			if (isHDFEnabled) {
				txnUDFData.setF_HDFFIELD(BankFusionIOSupport.convertToBytes(hdfFieldData));
			}
			try {
				factory.create(IBOUBTB_TXNUDFDATA.BONAME, txnUDFData);
				LOGGER.info(txnUDFData.getBoID());
			}
			catch (Exception e) {
					LOGGER.error(ExceptionUtil.getExceptionAsString(e));
			}
		}
	}

	/**
	 * This method has been used for validating if UDF data exists in the input.
	 * @param udfFieldData
	 */
	private boolean validateUDFExists(Object udfFieldData) {
		boolean isUDFExists = Boolean.FALSE;
		Class clazz = udfFieldData.getClass();
		String name = CommonConstants.EMPTY_STRING;
		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			fields[i].setAccessible(true);
			name = fields[i].getName();
			if (name.equals("_userDefinedFieldList")) {
				List value;
				try {
					value = (List) fields[i].get(udfFieldData);
					if (value.size() != 0) {
						isUDFExists = Boolean.TRUE;
					}
				}
				catch (IllegalAccessException e) {
						LOGGER.error(ExceptionUtil.getExceptionAsString(e));
				}
			}
		}
		return isUDFExists;
	}

	/**
	 * This method has been used for validating if HDF data exists in the input.
	 * @param hdfFieldData
	 */
	private boolean validateHDFExists(Object hdfFieldData) {
		boolean isHDFExists = Boolean.FALSE;
		String name = CommonConstants.EMPTY_STRING;
		Class clazz = hdfFieldData.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			fields[i].setAccessible(true);
			name = fields[i].getName();
			if (name.equals("_userDefinedFieldList")) {
				List value;
				try {
					value = (List) fields[i].get(hdfFieldData);
					if (value.size() != 0) {
						isHDFExists = Boolean.TRUE;
					}
				}
				catch (IllegalAccessException e) {
						LOGGER.error(ExceptionUtil.getExceptionAsString(e));
				}
			}
		}
		return isHDFExists;
	}
}
