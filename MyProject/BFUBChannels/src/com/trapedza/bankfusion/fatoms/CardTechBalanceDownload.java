package com.trapedza.bankfusion.fatoms;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.fatoms.batch.CardTechBalanceDownload.CardTechBalanceDownloadFatomContext;
import com.misys.ub.systeminformation.BusinessInformationService;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.services.BatchService;
import com.trapedza.bankfusion.bo.refimpl.IBOCB_CMN_ModuleConfiguration;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractCardTechBalanceDownload;
import com.trapedza.bankfusion.steps.refimpl.ICardTechBalanceDownload;


public class CardTechBalanceDownload extends AbstractCardTechBalanceDownload
		implements ICardTechBalanceDownload {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	  private static final String BATCH_PROCESS_NAME = "CardTechBalanceDownload";
	 private transient final static Log logger = LogFactory.getLog(CardTechBalanceDownload.class.getName());

	public CardTechBalanceDownload(BankFusionEnvironment env) {
        super(env);
    }
	protected AbstractFatomContext getFatomContext() {
		// TODO Auto-generated method stub
		return new CardTechBalanceDownloadFatomContext(BATCH_PROCESS_NAME);
	}

	
	

	@Override
	protected void processBatch(BankFusionEnvironment environment,
			AbstractFatomContext context) throws BankFusionException {
		// TODO Auto-generated method stub
		 String runtimeBPID = environment.getRuntimeMicroflowID();
		 String cardTechPath = CommonConstants.EMPTY_STRING;
		 context.setRuntimeBPID(runtimeBPID);
	        if (logger.isDebugEnabled()) {
	            logger.debug("process(): starting service Card Tech Balance Download");
	        }
	        
	        IBOCB_CMN_ModuleConfiguration modConfigRec = null;
			Iterator modConfig = null;
			
			Map inputDataMap = context.getInputTagDataMap();
			String moduleName="CARDTECH";
			try {
				environment.getFactory().commitTransaction();
				environment.getFactory().beginTransaction();
				
				IBusinessInformation bizInfo = ((IBusinessInformationService) ServiceManagerFactory.getInstance().getServiceManager()
	                    .getServiceForName(BusinessInformationService.BUSINESS_INFORMATION_SERVICE)).getBizInfo();
				Map keyList = ((Map) bizInfo.getModuleConfigurationValue(moduleName, environment));
				Set st =  keyList.keySet();
				modConfig = st.iterator();
				while(modConfig.hasNext()){
					modConfigRec = (IBOCB_CMN_ModuleConfiguration)modConfig.next();
					if(modConfigRec.getF_PARAMNAME().trim().compareTo("CARDTECHBALDOWNLOADPATH")==0) {
						cardTechPath = modConfigRec.getF_PARAMVALUE().trim();
					}

				}
				inputDataMap.put("CARDTECHFILEPATH", cardTechPath);
				context.setInputTagDataMap(inputDataMap);
	    }
			catch (BankFusionException bfe) {
				
				environment.getFactory().rollbackTransaction();   //
				environment.getFactory().beginTransaction();      //
				throw bfe;
				
				
			}catch (Exception ex)
			{
               logger.error(ex);
           	environment.getFactory().rollbackTransaction();   //
			environment.getFactory().beginTransaction();      //
			}

	        // Start the batch running and put its status in the tags.
	        BatchService service = (BatchService) ServiceManager.getService(ServiceManager.BATCH_SERVICE);
	        service.runBatch(environment, context);
	}

	@Override
	protected void setOutputTags(AbstractFatomContext arg0) {
		// TODO Auto-generated method stub

	}

}
