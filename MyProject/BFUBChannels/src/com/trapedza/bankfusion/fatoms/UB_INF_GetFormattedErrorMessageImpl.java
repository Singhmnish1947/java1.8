package com.trapedza.bankfusion.fatoms;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import com.misys.bankfusion.common.BankFusionMessages;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_INF_GetFormattedErrorMessage;


public class UB_INF_GetFormattedErrorMessageImpl extends AbstractUB_INF_GetFormattedErrorMessage {

    /**
     * 
     */
    private static final long serialVersionUID = -4796956676415353635L;

    public UB_INF_GetFormattedErrorMessageImpl(BankFusionEnvironment env) {
		super(env);
	}

	@Override
    public void process(BankFusionEnvironment env) throws BankFusionException {
        String errorRsn = StringUtils.EMPTY;
        String params=StringUtils.EMPTY;
        if (StringUtils.isNotBlank(getF_IN_ErrorParam())) {
            String[] parameters = getF_IN_ErrorParam().split(",");
            params = Arrays.asList(parameters).toString().replace("[", "").replace("]", "");//remove brackets([) convert it to string
        }

        if (getF_IN_ErrorCode().intValue() != 0 && getF_IN_ErrorCode().toString().length() == 8) {
            errorRsn = BankFusionMessages.getInstance().getFormattedEventMessage(getF_IN_ErrorCode(),
                    new Object[] { params }, env.getUserSession().getUserLocale());
        }

        setF_OUT_ErrorMessage(errorRsn);

    }

}
