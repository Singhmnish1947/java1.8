package com.trapedza.bankfusion.fatoms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.ComplexTypeConvertorFactory;
import com.misys.bankfusion.common.IComplexTypeConvertor;
import com.misys.bankfusion.subsystem.microflow.runtime.impl.MicroflowHelper;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.IMFManager;
import com.trapedza.bankfusion.servercommon.microflow.IMicroflowHelper;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ATM_NarrUserExitInput;

import bf.com.misys.cbs.types.msgs.atm.v1r1.AtmAccDualPosRq;
import bf.com.misys.cbs.types.msgs.atm.v1r1.AtmAccountTransferTxnRq;
import bf.com.misys.cbs.types.msgs.atm.v1r1.AtmBalanceEnquiryRq;
import bf.com.misys.cbs.types.msgs.atm.v1r1.AtmBillPmtRq;
import bf.com.misys.cbs.types.msgs.atm.v1r1.AtmCashTransactionRq;
import bf.com.misys.cbs.types.msgs.atm.v1r1.AtmChequeDepositRq;
import bf.com.misys.cbs.types.msgs.atm.v1r1.AtmChqBkRequestRq;
import bf.com.misys.cbs.types.msgs.atm.v1r1.AtmChqStatusEnqRq;
import bf.com.misys.cbs.types.msgs.atm.v1r1.AtmMiniStatementRq;

public class NarrUserExitInput extends AbstractUB_ATM_NarrUserExitInput {

    public NarrUserExitInput(BankFusionEnvironment env) {
        super(env);
    }
    private static final transient Log logger = LogFactory.getLog(NarrUserExitInput.class.getName());

    public void process(BankFusionEnvironment env) throws BankFusionException {

        if (getF_IN_Mode().equals("Input")) {
            env.putObject(BankFusionThreadLocal.getCorrelationID(), getF_IN_input());
        }
        else if (getF_IN_Mode().equals("Remove")) {
            env.removeObject(BankFusionThreadLocal.getCorrelationID());
            if (env.getObject(BankFusionThreadLocal.getCorrelationID()) != null) {
                logger.info("Values Still available for " + BankFusionThreadLocal.getCorrelationID());
            }
        }
        else if (getF_IN_Mode().equals("Retrieve")) {
            Object retrievedObj = env.getObject(BankFusionThreadLocal.getCorrelationID());
            setF_OUT_commonOutput(retrievedObj);
            if (retrievedObj instanceof AtmAccDualPosRq) {
                setF_OUT_dualPosRq((AtmAccDualPosRq) retrievedObj);
            }
            else if (retrievedObj instanceof AtmAccountTransferTxnRq) {
                setF_OUT_fundsTrfRq((AtmAccountTransferTxnRq) retrievedObj);
            }
            else if (retrievedObj instanceof AtmBalanceEnquiryRq) {
                setF_OUT_balEnqRq((AtmBalanceEnquiryRq) retrievedObj);
            }
            else if (retrievedObj instanceof AtmBillPmtRq) {
                setF_OUT_billPmtRq((AtmBillPmtRq) retrievedObj);
            }
            else if (retrievedObj instanceof AtmCashTransactionRq) {
                setF_OUT_cashTxnRq((AtmCashTransactionRq) retrievedObj);
            }
            else if (retrievedObj instanceof AtmChequeDepositRq) {
                setF_OUT_chqDepRq((AtmChequeDepositRq) retrievedObj);
            }
            else if (retrievedObj instanceof AtmChqBkRequestRq) {
                setF_OUT_chqBookRq((AtmChqBkRequestRq) retrievedObj);
            }
            else if (retrievedObj instanceof AtmChqStatusEnqRq) {
                setF_OUT_chqStatusRq((AtmChqStatusEnqRq) retrievedObj);
            }
            else if (retrievedObj instanceof AtmMiniStatementRq) {
                setF_OUT_miniStmtRq((AtmMiniStatementRq) retrievedObj);
            }
        }
        else {
            logger.info("Wrong mode" + getF_IN_Mode() + " Valid Modes are Input, Remove & Retrieve");
        }
    }

    public static void printXMLTag(Object xmlOject) {
        if (logger.isInfoEnabled()) {
            logger.info(convertObjectToXMLString(BankFusionThreadLocal.getBankFusionEnvironment(), xmlOject));
        }
    }

    public static String convertObjectToXMLString(BankFusionEnvironment env, Object obj) {
        IMicroflowHelper microflowHelper = new MicroflowHelper(env);
        IMFManager mfManager = microflowHelper.getMFManager();
        ClassLoader cl = mfManager.getDynamicClassLoader();
        IComplexTypeConvertor complexTypeConvertor = ComplexTypeConvertorFactory.getComplexTypeConvertor(cl);
        return complexTypeConvertor.getXmlFromJava(obj.getClass().getName(), obj);
    }
}
