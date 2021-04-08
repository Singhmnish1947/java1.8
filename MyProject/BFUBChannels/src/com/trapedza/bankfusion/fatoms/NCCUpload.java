package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.core.ExtensionPointHelper;
import com.trapedza.bankfusion.extensionpoints.NCCUploadHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.extensionpoints.ExtensionPoint;
import com.trapedza.bankfusion.servercommon.extensionpoints.ExtensionPointUtils;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_PAY_NCCUpload;

/*
 * 
 */
public class NCCUpload extends AbstractUB_PAY_NCCUpload {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private transient final static Log logger = LogFactory
			.getLog(NCCUpload.class.getName());

	// Added to store the UniqueIdentifiers:Combination of clearing
	// code,identifierCode and subbranch suffix

	ArrayList<String> uniqueIdentifiers = new ArrayList<String>();

	/**
	 * The constructor that indicates we're in a runtime environment and we
	 * should initialise the Fatom with only those attributes necessary.
	 * 
	 * @param env
	 *            The BankFusion Environment
	 */
	public NCCUpload(BankFusionEnvironment env) {
		super(env);
	}

	/**
	 * The method will invoke the Extension Point
	 * 
	 * @param env
	 *            Used to get a handle on the BankFusion environment
	 */

	public void process(BankFusionEnvironment env) {

		ExtensionPoint extensionPoint = null;
		ExtensionPointHelper nccUploadProcess = null;

		nccUploadProcess = getExtensionPoint(getF_IN_NationalClearingCode());

		HashMap<String, String> attributes = new HashMap<String, String>();
		attributes.put(NCCUploadHelper.NCC_UPLOADFILENAME,
				getF_IN_NCCUploadFileName());

		extensionPoint = ExtensionPointUtils.executeExtensionPoint(env,
				attributes, nccUploadProcess);

		Map resultMap = extensionPoint.getAttributes();
		Boolean UploadStatus = (Boolean) resultMap
				.get(NCCUploadHelper.NCC_UPLOADSTATUS);

		setF_OUT_UploadStatus(UploadStatus.booleanValue());
	}

}
