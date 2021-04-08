package com.trapedza.bankfusion.fatoms;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.events.IEvent;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_INF_ProcessExecutor;

/**
 * @author Vipul.Sharma
 * @date 26 Aug 2011
 * @project Universal Banking
 * @Description This class file calls business process in try catch mode. It catches the BankFusion
 *              Exception and pass the event code number to output
 * 
 */
public class InterfaceProcessExecutorWithtryCatch extends AbstractUB_INF_ProcessExecutor {

    private static final long serialVersionUID = -5280142101890117077L;
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }
    private static final transient Log logger = LogFactory.getLog(InterfaceProcessExecutorWithtryCatch.class.getName());

    public InterfaceProcessExecutorWithtryCatch() {
        super(null);
    }

    public InterfaceProcessExecutorWithtryCatch(BankFusionEnvironment env) {
        super(env);
    }

    public void process(BankFusionEnvironment env) throws BankFusionException {

        String mfID = getF_IN_BusinessProcessID();
        if ((mfID.compareTo(CommonConstants.EMPTY_STRING) != 0) && (mfID != null)) {
            HashMap<String, Object> params = new HashMap<String, Object>();

            params.put("Message", getF_IN_Message());

            try {
                HashMap<String, Object> outputParams = MFExecuter.executeMF(mfID, env, params);
                setF_OUT_EventCode((Integer) outputParams.get("EventCode"));
                if ((Integer) outputParams.get("EventCode") == null)
                    setF_OUT_EventCode((Integer) 0);
                env.getFactory().commitTransaction();
                env.getFactory().beginTransaction();
                
            }
            catch (BankFusionException bfException) {
                if (logger.isDebugEnabled()) {
                    logger.info(bfException.getMessageNumber() + " : " + bfException.getLocalizedMessage());
                    logger.error(bfException);
                }

                for (IEvent error : bfException.getEvents()) {
                    setF_OUT_EventCode(error.getEventNumber());
                }
                try {
                    env.getFactory().rollbackTransaction();
                    env.getFactory().beginTransaction();
                }
                catch (Exception ignored) {
                    logger.error("Exception :", ignored);
                }
            }
            catch (Exception e) {
                setF_OUT_EventCode(40000127);
                try {
                    env.getFactory().rollbackTransaction();
                    env.getFactory().beginTransaction();
                }
                catch (Exception ignored) {
                    logger.error("Exception :", ignored);
                    
                }
                logger.error(ExceptionUtil.getExceptionAsString(e));
            }
            finally {
                try {
                    env.getFactory().beginTransaction();
                }
                catch (Exception ignored) {
                    logger.error("Exception :", ignored);
                }
            }
        }

    }

}
