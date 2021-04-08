package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exolab.castor.types.AnyNode;

import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractAnyTypeToBigDecimal;
import com.trapedza.bankfusion.steps.refimpl.IAnyTypeToBigDecimal;

public class AnyTypeToBigDecimal extends AbstractAnyTypeToBigDecimal implements IAnyTypeToBigDecimal {
	private static final transient Log logger = LogFactory.getLog(PostingEngineWithTryCatch.class.getName());
	private BankFusionEnvironment env;
	public AnyTypeToBigDecimal(BankFusionEnvironment env) {
		super(env);
	}
	
	public void process(BankFusionEnvironment env) throws BankFusionException {
		this.env = env;
		Object inputObject = getF_IN_inputObject();
		 BigDecimal ret = null;
		if( inputObject != null ) {
			if( inputObject instanceof BigDecimal ) {
                ret = (BigDecimal) inputObject;
            } else if( inputObject instanceof String ) {
                ret = new BigDecimal( (String) inputObject );
            } else if( inputObject instanceof BigInteger ) {
                ret = new BigDecimal( (BigInteger) inputObject );
            } else if( inputObject instanceof Number ) {
                ret = BigDecimal.valueOf(((Number) inputObject).doubleValue());
            } else if( inputObject instanceof AnyNode ) {
            	AnyNode n = (AnyNode) inputObject;
                ret = new BigDecimal( n.getStringValue());
            } else {
            	ret = new BigDecimal( (String) inputObject );
            }
			inputObject = ret;
			
	}
		setF_IN_inputObject(inputObject);
		setF_OUT_outputBigDecimal(ret);
	}
}
