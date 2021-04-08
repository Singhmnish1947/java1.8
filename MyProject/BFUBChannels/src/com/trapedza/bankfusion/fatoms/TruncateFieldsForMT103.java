package com.trapedza.bankfusion.fatoms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_TruncateFields;

public class TruncateFieldsForMT103 extends AbstractUB_SWT_TruncateFields {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("deprecation")
	public TruncateFieldsForMT103(BankFusionEnvironment env) {
		super(env);
	}

	public TruncateFieldsForMT103() {
	}

	private transient final static Log logger = LogFactory.getLog(TruncateFieldsForMT103.class.getName());

	public void process(BankFusionEnvironment env) throws BankFusionException {
	  logger.info("Truncating data to set in swift remittance table::");
		setF_OUT_text1(get35CharText(getF_IN_text1()));
		setF_OUT_text2(get35CharText(getF_IN_text2()));
		setF_OUT_text3(get35CharText(getF_IN_text3()));
		setF_OUT_text4(get35CharText(getF_IN_text4()));

	}

	private String get35CharText(String text) {
		if (null != text && !text.isEmpty()) {
			if (text.length() > 35) {
				text = text.substring(0, 35);
			}
		}
		return text;
	}

}
