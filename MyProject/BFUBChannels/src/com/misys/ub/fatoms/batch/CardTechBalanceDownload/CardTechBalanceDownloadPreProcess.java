package com.misys.ub.fatoms.batch.CardTechBalanceDownload;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.process.IBatchPreProcess;
import com.trapedza.bankfusion.batch.process.PreProcessException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;

public class CardTechBalanceDownloadPreProcess implements IBatchPreProcess {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	 /**
     */

    private static final transient Log logger = LogFactory.getLog(CardTechBalanceDownloadPreProcess.class.getName());

    private BankFusionEnvironment environment;
    private AbstractFatomContext context;

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.batch.process.IBatchPreProcess#init(com.trapedza.bankfusion.commands.core.BankFusionEnvironment)
     */
    public void init(BankFusionEnvironment env) throws PreProcessException {
        this.environment = env;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trapedza.bankfusion.batch.process.IBatchPreProcess#process(com.trapedza.bankfusion.batch.fatom.AbstractFatomContext)
     */
    public void process(AbstractFatomContext context) throws PreProcessException {
        this.context = context;
    }
}

