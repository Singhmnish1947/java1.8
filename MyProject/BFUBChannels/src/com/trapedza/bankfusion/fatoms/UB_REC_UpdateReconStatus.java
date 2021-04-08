/**
 * 
 */
package com.trapedza.bankfusion.fatoms;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_REC_UpdateReconStatus;

import bf.com.misys.cbs.services.ReadModuleConfigurationRq;
import bf.com.misys.cbs.services.ReadModuleConfigurationRs;
import bf.com.misys.cbs.types.ModuleKeyRq;
import bf.com.misys.ub.types.AmendReconTranDtlInput;
import bf.com.misys.ub.types.ReconTransDtl;
import bf.com.misys.ub.types.recon.msgs.v1r0.AmendReconTransDtlRq;

/**
 * @author Shreyas.MR
 * 
 */
public class UB_REC_UpdateReconStatus extends AbstractUB_REC_UpdateReconStatus {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    private transient final static Log logger = LogFactory.getLog(UB_REC_ReconciliationFatom.class.getName());
    private static final String RECON_MODULE_NAME = "REC";
    private static final String RECON_EXTRACT_PARAM_NAME = "SmartStreamExtractLoc";
    private static final String READ_MODULE_CONFIGURATION_SERVICE = "CB_CMN_ReadModuleConfiguration_SRV";

    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    public UB_REC_UpdateReconStatus(BankFusionEnvironment env) {
        super(env);
    }

    public void process(BankFusionEnvironment env) {
        UpdateReconTranDetails(env);
    }

    public void UpdateReconTranDetails(BankFusionEnvironment env) {
        Scanner sc = null;
        try {
            ReadModuleConfigurationRq readModuleConfRq = new ReadModuleConfigurationRq();
            ModuleKeyRq moduleKeyRq = new ModuleKeyRq();
            moduleKeyRq.setModuleId(RECON_MODULE_NAME);
            moduleKeyRq.setKey(RECON_EXTRACT_PARAM_NAME);
            readModuleConfRq.setModuleKeyRq(moduleKeyRq);
            HashMap inputParams = new HashMap();
            inputParams.put("ReadModuleConfigurationRq", readModuleConfRq);
            HashMap outputParams = MFExecuter.executeMF(READ_MODULE_CONFIGURATION_SERVICE, env, inputParams);
            ReadModuleConfigurationRs readModuleConfRs = (ReadModuleConfigurationRs) (outputParams
                    .get("ReadModuleConfigurationRs"));
            String smartStreamStaticExtractLocation = readModuleConfRs.getModuleConfigDetails().getValue();

            sc = new Scanner(new File(smartStreamStaticExtractLocation + "/Misys_matched.csv"));
            sc.nextLine();
            while (sc.hasNextLine()) {
                String[] statusRecord = sc.nextLine().split(";"); // split by
                // semi
                // colon
                String statusCode = statusRecord[7];
                String status = "Not Reconciled";

                if (statusCode.startsWith("M"))
                    status = "Reconciled";

                String txnsrid = statusRecord[45];
                logger.debug(status + txnsrid);

                AmendReconTransDtlRq rq = new AmendReconTransDtlRq();
                AmendReconTranDtlInput ip = new AmendReconTranDtlInput();
                ReconTransDtl dtl = new ReconTransDtl();
                dtl.setTransactionSRID(txnsrid);
                dtl.setTransactionID(statusRecord[66]);
                dtl.setReconStatus(status);
                ip.setAmendReconTransDtl(dtl);
                rq.setAmendReconTransDtlInput(ip);
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("amendReconTransDtlRq", rq);

                MFExecuter.executeMF("UB_REC_AmendReconTranDtls_SRV", env, map);

                // now you can manipulate the numbers in num[]
            }
        }
        catch (FileNotFoundException e) {
            logger.error(ExceptionUtil.getExceptionAsString(e));
        }
        finally {
            if (sc != null)
                sc.close();
        }
    }

}
