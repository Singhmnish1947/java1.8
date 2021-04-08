package com.trapedza.bankfusion.fatoms;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.UpdateAuditLoggerHolder;
import com.trapedza.bankfusion.servercommon.core.UpdateAuditLoggerManager;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_IBI_InsertPrintReqArgs;

/**
 * @author Gurruprasad
 * Helper class to insert the print request argument name and values dynamically into the INFTB_IBIREQPRINTARG table.
 */
public class UB_IBI_InsertPrintReqArgs extends AbstractUB_IBI_InsertPrintReqArgs {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}



    private transient final static Log logger = LogFactory.getLog(UB_IBI_InsertPrintReqArgs.class.getName());

    public void registerWithUpdateLoggerManager(UpdateAuditLoggerManager manager) {
        if (manager.isTransactionLoggingEnabled()) {
            manager.addNewUpdateHolder(new UpdateAuditLoggerHolder(UB_IBI_InsertPrintReqArgs.class.getName(), svnRevision));
        }
    }

    public UB_IBI_InsertPrintReqArgs(BankFusionEnvironment env) {
        super(env);
    }

    public static final String EXIT_CLASS = "Returning from UB_IBI_InsertPrintReqArgs";
    public static final String EXIT_METHOD_MESSAGE = "Returning from addPrintReqArgs() with value=";
    public static final String ENTRY_METHOD_MESSAGE = "In addPrintReqArgs()";

    /**
     * This is to insert IBI PrintReqName and PrintReqValue into the IBIREQPRINT table.
     * 
     */

    public void process(BankFusionEnvironment env) {

        VectorTable printReqVecotr = new VectorTable();
        int seqCounter = 0;

        seqCounter = addPrintReqArgs(getF_IN_PRINTREQARGNAME1(), getF_IN_PRINTREQARGVALUE1(), printReqVecotr, seqCounter);
        seqCounter = addPrintReqArgs(getF_IN_PRINTREQARGNAME2(), getF_IN_PRINTREQARGVALUE2(), printReqVecotr, seqCounter);
        seqCounter = addPrintReqArgs(getF_IN_PRINTREQARGNAME3(), getF_IN_PRINTREQARGVALUE3(), printReqVecotr, seqCounter);
        seqCounter = addPrintReqArgs(getF_IN_PRINTREQARGNAME4(), getF_IN_PRINTREQARGVALUE4(), printReqVecotr, seqCounter);
        seqCounter = addPrintReqArgs(getF_IN_PRINTREQARGNAME5(), getF_IN_PRINTREQARGVALUE5(), printReqVecotr, seqCounter);
        seqCounter = addPrintReqArgs(getF_IN_PRINTREQARGNAME6(), getF_IN_PRINTREQARGVALUE6(), printReqVecotr, seqCounter);
        seqCounter = addPrintReqArgs(getF_IN_PRINTREQARGNAME7(), getF_IN_PRINTREQARGVALUE7(), printReqVecotr, seqCounter);
        seqCounter = addPrintReqArgs(getF_IN_PRINTREQARGNAME8(), getF_IN_PRINTREQARGVALUE8(), printReqVecotr, seqCounter);
        seqCounter = addPrintReqArgs(getF_IN_PRINTREQARGNAME9(), getF_IN_PRINTREQARGVALUE9(), printReqVecotr, seqCounter);
        seqCounter = addPrintReqArgs(getF_IN_PRINTREQARGNAME10(), getF_IN_PRINTREQARGVALUE10(), printReqVecotr, seqCounter);
        seqCounter = addPrintReqArgs(getF_IN_PRINTREQARGNAME11(), getF_IN_PRINTREQARGVALUE11(), printReqVecotr, seqCounter);
        seqCounter = addPrintReqArgs(getF_IN_PRINTREQARGNAME12(), getF_IN_PRINTREQARGVALUE12(), printReqVecotr, seqCounter);
        seqCounter = addPrintReqArgs(getF_IN_PRINTREQARGNAME13(), getF_IN_PRINTREQARGVALUE13(), printReqVecotr, seqCounter);
        seqCounter = addPrintReqArgs(getF_IN_PRINTREQARGNAME14(), getF_IN_PRINTREQARGVALUE14(), printReqVecotr, seqCounter);
        seqCounter = addPrintReqArgs(getF_IN_PRINTREQARGNAME15(), getF_IN_PRINTREQARGVALUE15(), printReqVecotr, seqCounter);
        seqCounter = addPrintReqArgs(getF_IN_PRINTREQARGNAME16(), getF_IN_PRINTREQARGVALUE16(), printReqVecotr, seqCounter);
        seqCounter = addPrintReqArgs(getF_IN_PRINTREQARGNAME17(), getF_IN_PRINTREQARGVALUE17(), printReqVecotr, seqCounter);
        seqCounter = addPrintReqArgs(getF_IN_PRINTREQARGNAME18(), getF_IN_PRINTREQARGVALUE18(), printReqVecotr, seqCounter);
        seqCounter = addPrintReqArgs(getF_IN_PRINTREQARGNAME19(), getF_IN_PRINTREQARGVALUE19(), printReqVecotr, seqCounter);
        seqCounter = addPrintReqArgs(getF_IN_PRINTREQARGNAME20(), getF_IN_PRINTREQARGVALUE20(), printReqVecotr, seqCounter);
        seqCounter = addPrintReqArgs(getF_IN_PRINTREQARGNAME21(), getF_IN_PRINTREQARGVALUE21(), printReqVecotr, seqCounter);
        seqCounter = addPrintReqArgs(getF_IN_PRINTREQARGNAME22(), getF_IN_PRINTREQARGVALUE22(), printReqVecotr, seqCounter);
        seqCounter = addPrintReqArgs(getF_IN_PRINTREQARGNAME23(), getF_IN_PRINTREQARGVALUE23(), printReqVecotr, seqCounter);

        setF_OUT_PRINTREQVECTOR(printReqVecotr);
        if (logger.isInfoEnabled()) {
            logger.info(EXIT_CLASS);
        }

    }

    /**
     * This method is to add Name, Value and SequenceNumber into Vector Table.
     * 
     * @param printReqName
     * @param printReqValue
     * @param addPrintArgs
     * @param seqCounter
     * @return
     */
    private int addPrintReqArgs(String printReqName, String printReqValue, VectorTable printReqVecotr, int seqCounter) {
        Map writeMap = new HashMap();
        if (printReqName != "" && printReqValue != "") {
            if (logger.isInfoEnabled()) {
                logger.info(ENTRY_METHOD_MESSAGE);
            }
            seqCounter++;
            writeMap.put("PRINTREQNAME", printReqName);
            writeMap.put("PRINTREQVALUE", printReqValue);
            writeMap.put("SEQNO", seqCounter);
            printReqVecotr.addAll(new VectorTable(writeMap));
        }
        if (logger.isInfoEnabled()) {
            logger.info(EXIT_METHOD_MESSAGE + seqCounter);
        }
        return seqCounter;
    }
}
