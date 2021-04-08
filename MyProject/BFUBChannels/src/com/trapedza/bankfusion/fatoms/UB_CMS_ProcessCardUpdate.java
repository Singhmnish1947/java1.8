package com.trapedza.bankfusion.fatoms;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_CMS_UpdateATMDetails;
import com.trapedza.bankfusion.steps.refimpl.IUB_CMS_UpdateATMDetails;

import bf.com.misys.cbs.msgs.cards.v1r0.MaintainCustCardRq;
import bf.com.misys.cbs.types.CustCrdBasicDtls;

public class UB_CMS_ProcessCardUpdate extends AbstractUB_CMS_UpdateATMDetails implements IUB_CMS_UpdateATMDetails {

    /**
     * 
     */
    private static final long serialVersionUID = 264110985815003053L;
    private static final String CARD_ACTION_NEW = "NEW";
    private static final String PROCESS_MFID_FOR_ATMACCOUNT = "UB_CMS_UpdateCardDetails_SRV";
    private static final Log logger = LogFactory.getLog(UB_CMS_ProcessCardUpdate.class);

    public UB_CMS_ProcessCardUpdate(BankFusionEnvironment env) {
        super(env);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void process(BankFusionEnvironment env) {

        MaintainCustCardRq CustCardRq = new MaintainCustCardRq();
        CustCardRq = getF_IN_maintainCustCardRq();

        for (int i = 0; i < CustCardRq.getCustCrdBasicInput().getCustCrdBasicDtlsCount(); i++) {
            CustCrdBasicDtls custCrdBasicDtls = CustCardRq.getCustCrdBasicInput().getCustCrdBasicDtls(i);
            String cardAction = custCrdBasicDtls.getCardAction();

            if (cardAction.equals(CARD_ACTION_NEW)) {
                HashMap<String, Object> params = new HashMap<String, Object>();

                params.put("AccountNumber", custCrdBasicDtls.getAccountID().getStandardAccountId());
                params.put("IMDCode", custCrdBasicDtls.getImdCode());
                params.put("CardNumber", custCrdBasicDtls.getCardNumber());

                MFExecuter.executeMF(PROCESS_MFID_FOR_ATMACCOUNT, env, params);
            }
            else {
                logger.info("NO NEW RECORDS");
            }

        }
    }

}
