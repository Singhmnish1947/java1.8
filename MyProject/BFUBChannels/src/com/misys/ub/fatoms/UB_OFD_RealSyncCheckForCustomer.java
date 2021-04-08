package com.misys.ub.fatoms;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.persistence.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.bo.refimpl.IBOProperty;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_OFD_RealSyncCheckForCustomer;

public class UB_OFD_RealSyncCheckForCustomer extends AbstractUB_OFD_RealSyncCheckForCustomer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** <code>svnRevision</code> = $Revision: 1.0 $ */
	public static final String svnRevision = "$Revision: 1.0 $"; //$NON-NLS-1$
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	/** The Constant logger. */
	private static final transient Log logger = LogFactory.getLog(UB_OFD_RealSyncCheckForCustomer.class.getName());

	private static boolean offlineSupported;
	private static String trueConstant = "true" ;
	private static String constant = "";
	private static final String queyToSupportOffline =  " WHERE "+ IBOProperty.PROPID + " = ? ";
	private static final String keyForOfflineSupport = "EnablePartySyncForBranchServer";
	
	@SuppressWarnings("deprecation")
	public UB_OFD_RealSyncCheckForCustomer(BankFusionEnvironment environment) {
		super(environment);
	}
	public void process(BankFusionEnvironment env) throws BankFusionException {
		//Check condition for DB Property of NearRealTimeSyncForOfflineTeller is true or false
		boolean	offlineSupported = isOfflineSupported(keyForOfflineSupport);
		//Setting up the boolean value for offline teller support with UB
		setF_OUT_IsOfflineSupported(offlineSupported);
	}
		public static boolean isOfflineSupported(String key)
		{
			IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
			ArrayList<String> queryParams = new ArrayList<String>();
			queryParams.add(key);
			logger.info("key is : " + key);
			IBOProperty offlineSupportValue = (IBOProperty) factory
						.findFirstByQuery(
								IBOProperty.BONAME,
								queyToSupportOffline,
								queryParams, true);
			if (null != offlineSupportValue) 
			{
				constant  = offlineSupportValue.getF_VALUE();
				if(constant.equalsIgnoreCase(trueConstant))
				{
					offlineSupported = true;
					logger.info(" Offline support is  [" + offlineSupported + "]");
				}
				else 
				{
					offlineSupported =false;
					logger.info(" Offline support is  [" + offlineSupported + "]");
				}
			} 
			return offlineSupported;
		}
	}	