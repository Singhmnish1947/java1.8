/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_CustomerVector;
import com.trapedza.bankfusion.steps.refimpl.ISWT_CustomerVector;

public class SWT_CustomerVectorFatom extends AbstractSWT_CustomerVector implements ISWT_CustomerVector {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    public BankFusionEnvironment env;

    private transient final static Log logger = LogFactory.getLog(SWT_CustomerVectorFatom.class.getName());

    public SWT_CustomerVectorFatom(BankFusionEnvironment env) {
        super(env);
    }

    public void process(BankFusionEnvironment environment) {
        validitingCusNumber(environment);
    }

    private void validitingCusNumber(BankFusionEnvironment environment) {
        VectorTable columnVector = getF_IN_moduleConfigResult();
        HashMap row = new HashMap();
        String[] key = new String[100];
        String plac1 = null, plac2 = null;
        HashMap holder = new HashMap();
        int k = 0;
        for (int i = 0; i < columnVector.size(); i++) {
            row.putAll(columnVector.getRowTagsAsFields(i));
            logger.debug(columnVector.getRowTagsAsFields(i));

            Iterator ia = row.keySet().iterator();
            while (ia.hasNext()) {
                key[k] = (String) ia.next();
                if (key[k].equalsIgnoreCase("f_paramname")) {
                    plac1 = row.get(key[k]).toString();
                }
                if (key[k].equalsIgnoreCase("f_paramvalue")) {
                    plac2 = row.get(key[k]).toString();
                }
                if ((plac1 != null && plac2 != null)
                        && (key[k].equalsIgnoreCase("f_paramvalue") || key[k].equalsIgnoreCase("f_paramname"))) {
                    holder.put(plac1, plac2);
                }
                k++;
            }

        }
        String payMessage = getF_IN_PayMessage();
        if (!payMessage.equalsIgnoreCase("Y") && !payMessage.equalsIgnoreCase("N")) {
            Iterator itr = holder.keySet().iterator();
            while (itr.hasNext()) {
                String value = (String) itr.next();
                if (value.equalsIgnoreCase("pay_message")) {
                    setF_OUT_Pay_Message(holder.get(value).toString());
                }

            }
        }
        else {
            setF_OUT_Pay_Message(payMessage);
        }
        String swtStatementFlag = getF_IN_SwtStatementFlag();
        if (!swtStatementFlag.equalsIgnoreCase("Y") && !swtStatementFlag.equalsIgnoreCase("N")) {
            Iterator itr = holder.keySet().iterator();
            while (itr.hasNext()) {
                String value = (String) itr.next();
                if (value.equalsIgnoreCase("SWIFT_STATEMENT_FLAG")) {
                    setF_OUT_swtStatementFlag(holder.get(value).toString());
                }

            }
        }
        else {
            setF_OUT_swtStatementFlag(swtStatementFlag);
        }
        String transactionFlagStatement = getF_IN_TransactionFlagStatement();
        if (!transactionFlagStatement.equalsIgnoreCase("Y") && !transactionFlagStatement.equalsIgnoreCase("N")) {
            Iterator itr = holder.keySet().iterator();
            while (itr.hasNext()) {
                String value = (String) itr.next();
                if (value.equalsIgnoreCase("TRANSACTION_FLAG_STAT")) {
                    setF_OUT_transactionFlagStatement(holder.get(value).toString());
                }

            }
        }
        else {
            setF_OUT_transactionFlagStatement(transactionFlagStatement);
        }
        int adviceDays = getF_IN_ADVICEDAYS().intValue();

        if (adviceDays == 100) {
            Iterator itr = holder.keySet().iterator();
            while (itr.hasNext()) {
                String value = (String) itr.next();
                if (value.equalsIgnoreCase("ADVICE_DAYS")) {
                    // setF_OUT_AdviceDays(new
                    // Integer(Integer.parseInt(holder.get(value).toString())));
                    setF_OUT_AdviceDays(Integer.valueOf(holder.get(value).toString()));
                }

            }
        }
        else {

            setF_OUT_AdviceDays(new Integer(adviceDays));
            // setF_OUT_transactionFlagStatement(transactionFlagStatement);
        }
        String fxConfirmation = getF_IN_FxConfirmation();
        if (!fxConfirmation.equalsIgnoreCase("Y") && !fxConfirmation.equalsIgnoreCase("N")) {
            Iterator itr = holder.keySet().iterator();
            while (itr.hasNext()) {
                String value = (String) itr.next();
                if (value.equalsIgnoreCase("FX_CONFIRMATION")) {
                    setF_OUT_fxConfirmation(holder.get(value).toString());
                }

            }
        }
        else {
            setF_OUT_fxConfirmation(fxConfirmation);
        }
        String depositLoanConfirmation = getF_IN_DepositLoanConfirmation();
        if (!depositLoanConfirmation.equalsIgnoreCase("Y") && !depositLoanConfirmation.equalsIgnoreCase("N")) {
            Iterator itr = holder.keySet().iterator();
            while (itr.hasNext()) {
                String value = (String) itr.next();
                if (value.equalsIgnoreCase("DEPOSIT_LOAN_CONFIRMATION")) {
                    setF_OUT_depositLoanConfirmation(holder.get(value).toString());
                }

            }
        }
        else {
            setF_OUT_depositLoanConfirmation(depositLoanConfirmation);
        }
        String callDepositLoanConfirmation = getF_IN_CallDepositLoanConfirmation();
        if (!callDepositLoanConfirmation.equalsIgnoreCase("Y") && !callDepositLoanConfirmation.equalsIgnoreCase("N")) {
            Iterator itr = holder.keySet().iterator();
            while (itr.hasNext()) {
                String value = (String) itr.next();
                if (value.equalsIgnoreCase("CALL_DEPOSIT_LOAN_CONFIRMATION")) {
                    setF_OUT_callDepositLoanConfirmation(holder.get(value).toString());
                }

            }
        }
        else {
            setF_OUT_callDepositLoanConfirmation(callDepositLoanConfirmation);
        }
        String drConfirmation = getF_IN_DrConfirmation();
        if (!drConfirmation.equalsIgnoreCase("Y") && !drConfirmation.equalsIgnoreCase("N")) {
            Iterator itr = holder.keySet().iterator();
            while (itr.hasNext()) {
                String value = (String) itr.next();
                if (value.equalsIgnoreCase("DR_CONFIRMATION")) {
                    setF_OUT_drConfirmation(holder.get(value).toString());
                }

            }
        }
        else {
            setF_OUT_drConfirmation(drConfirmation);
        }
        String crConfirmation = getF_IN_CrConfirmation();
        if (!crConfirmation.equalsIgnoreCase("Y") && !crConfirmation.equalsIgnoreCase("N")) {
            Iterator itr = holder.keySet().iterator();
            while (itr.hasNext()) {
                String value = (String) itr.next();
                if (value.equalsIgnoreCase("CR_CONFIRMATION")) {
                    setF_OUT_crConfirmation(holder.get(value).toString());
                }

            }
        }
        else {
            setF_OUT_crConfirmation(crConfirmation);
        }

    }
}
