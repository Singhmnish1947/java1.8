package com.misys.ub.payment.swift.posting;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.msgs.v1r0.OutwardSwtRemittanceRq;

import com.misys.bankfusion.common.util.BankFusionIOSupport;
import com.misys.bankfusion.subsystem.persistence.SimplePersistentObject;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;


public class UnblobOutwardRemittanceRq {
	
	private transient final static Log LOG = LogFactory.getLog(UnblobOutwardRemittanceRq.class);
	
	public static OutwardSwtRemittanceRq run(String boName, String queryIDAttributeName,
			String queryIDValue, String blobAttributeName) {

		if (boName == null || queryIDAttributeName == null
				|| queryIDValue == null || blobAttributeName == null) {
			// TODO Re-factoring - review, use of events?
			throw new IllegalArgumentException();
		}
		final String WHERECLAUSE = " WHERE " + queryIDAttributeName + " = ?";
		ArrayList<String> params = new ArrayList<String>(1);
		params.add(queryIDValue);

		IPersistenceObjectsFactory factory = BankFusionThreadLocal
				.getPersistanceFactory();

		SimplePersistentObject boWithBLOB = factory.findFirstByQuery(boName,
				WHERECLAUSE, params, true);

		byte[] bytesOfBLOB = new byte[0];

		final Object blobObject = boWithBLOB.getDataMap().get(
				"f_" + blobAttributeName);

		// Assume the data stored in BLOB is byte array.
		if (blobObject instanceof byte[]) {
			bytesOfBLOB = (byte[]) blobObject;
		} else {
			if (LOG.isWarnEnabled()) {
				LOG.warn("The blob from BO:[" + boName + "], columnKey:["
							+ blobAttributeName
							+ "] can not be casted to byte array.");
			}
			return new OutwardSwtRemittanceRq();
		}

		if (bytesOfBLOB.length>0) {
			Object object = BankFusionIOSupport.convertFromBytes(bytesOfBLOB);
			
			if (object instanceof OutwardSwtRemittanceRq) {
				// Assume it is originally a vector table and stored as byte
				// array in the BLOB.
				return (OutwardSwtRemittanceRq) object;
			} else {
				if (LOG.isWarnEnabled()) {
					LOG.warn("The blob from BO:[" + boName + "], columnKey:["
							+ blobAttributeName
							+ "] can not be casted to hash Map.");
				}
				// return the empty vector table, instead of null.
				return new OutwardSwtRemittanceRq();
			}

		}

		return new OutwardSwtRemittanceRq();
	}
}
