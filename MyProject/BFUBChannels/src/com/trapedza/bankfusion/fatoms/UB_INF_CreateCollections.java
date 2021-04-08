package com.trapedza.bankfusion.fatoms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_INF_CreateCollections;

public class UB_INF_CreateCollections extends AbstractUB_INF_CreateCollections {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4772406098061880255L;
	/**
	 * 
	 */
	private transient final static Log logger = LogFactory.getLog(UB_INF_CreateCollections.class.getName());

	public UB_INF_CreateCollections() {
		super();
	}


	public UB_INF_CreateCollections(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) throws BankFusionException {

		bf.com.misys.ub.types.Parameters fromDate = new bf.com.misys.ub.types.Parameters();
		fromDate.setParamName("param5_val");
		fromDate.setParamValue(getF_IN_param5_val());
		bf.com.misys.ub.types.Parameters channel = new bf.com.misys.ub.types.Parameters();
		channel.setParamName("channel");
		channel.setParamValue(getF_IN_channel());
		bf.com.misys.ub.types.Parameters toDate = new bf.com.misys.ub.types.Parameters();
		toDate.setParamName("param6_value");
		toDate.setParamValue(getF_IN_param6_value());
		bf.com.misys.ub.types.Parameters locale = new bf.com.misys.ub.types.Parameters();
		locale.setParamName("param7_value");
		locale.setParamValue(getF_IN_param7_value());
		this.getF_OUT_BatchUXEnhancementRq().addParameters(fromDate);
		this.getF_OUT_BatchUXEnhancementRq().addParameters(toDate);
		this.getF_OUT_BatchUXEnhancementRq().addParameters(locale);
		this.getF_OUT_BatchUXEnhancementRq().addParameters(channel);

	}

}
