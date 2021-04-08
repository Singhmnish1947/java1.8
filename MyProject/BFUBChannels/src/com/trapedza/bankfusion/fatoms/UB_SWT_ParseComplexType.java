/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: ParseComplexType.java,v.1.0,Jun 12, 2009 12:31:50 PM harishrao $
 *
 */
package com.trapedza.bankfusion.fatoms;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.microflow.ActivityStep;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_ParseComplexType;

/**
 * @author itesh kumar
 * @date Jun 12, 2009
 * @project Universal Banking
 * @Description:
 */
public class UB_SWT_ParseComplexType extends AbstractUB_SWT_ParseComplexType {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}



	/**
	 * The Logger and Constants defined.
	 */
	private transient final static Log logger = LogFactory
			.getLog(UB_SWT_ParseComplexType.class.getName());

	private static final String inPrefix = "IN_";
	private String inKey = CommonConstants.EMPTY_STRING;
	private Object complexType = null;
	private Object subType = null;
	private String inputTagsInMF = CommonConstants.EMPTY_STRING;
	private String repeatingInputTagsInMF = CommonConstants.EMPTY_STRING;
    private String mfName = CommonConstants.EMPTY_STRING;
    private String outputMessageStatus = "P";
	private VectorTable vectorTable = new VectorTable();
    private Integer outputErrorCode=new Integer(0);
	public UB_SWT_ParseComplexType() {
		super();
	}

	public UB_SWT_ParseComplexType(BankFusionEnvironment env) {
		super(env);
	}

	@Override
	public void process(BankFusionEnvironment env, ActivityStep as)
			throws BankFusionException {
		init(as);
	
		if (complexType != null) {
			JXPathContext context = JXPathContext.newContext(complexType);
			subType = context.getValue(getF_IN_JXPathQuery());
			
			if (subType instanceof Object[]) {
				Object[] array = (Object[]) subType;

				for (int counter = 0; counter < array.length; counter++) {
					Map<String, Object> row = new HashMap<String, Object>();
					row.put("mtDetail", array[counter]);
					vectorTable.addAll(new VectorTable(row));
				}
			}
			
		}
		
		shutdown(as, env);
		getF_IN_inputTagsinMF();
	}

	private void init(ActivityStep as) {
		String jXPathQuery = (String) getInputTags().get(IN_JXPathQuery);
		setF_IN_JXPathQuery(jXPathQuery);
		Iterator<String> keys = as.getInTags().keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			if (key.startsWith(inPrefix)) {
				inKey = key;
			}
		}
		complexType = as.getInTag(inKey);
	}

	private void shutdown(ActivityStep as, BankFusionEnvironment env) {
		Map<String, Object> outputTags = new HashMap<String, Object>();
	
		 inputTagsInMF = (String) getInputTags().get(IN_inputTagsinMF);
		 repeatingInputTagsInMF =(String) getInputTags().get(IN_repeatingInputTagsInMF);
	     mfName = (String) getInputTags().get(IN_MicroflowName); 
   
		int rowCount = vectorTable.size();
		
		for (int counter = 0; counter < rowCount; counter++) {
			
			Map<String, Object> inputTags = new HashMap<String, Object>();
			inputTags.put(repeatingInputTagsInMF, vectorTable.getRowTags(counter).get(
			"mtDetail"));
			
			inputTags.put(inputTagsInMF, complexType);
			HashMap hashmapout = MFExecuter.executeMF(mfName, env, inputTags);
			String MessageStatus =(String)hashmapout.get("MessageStatus");
			Integer errorCode =  (Integer)hashmapout.get("ErrorNumber");
			
			if(MessageStatus.equals("F")&& outputMessageStatus.equals("P")){
				outputMessageStatus = "F";
			    outputErrorCode = errorCode; 
			}
			 
		}
		 outputTags.put("MessageStatus", outputMessageStatus);
		 outputTags.put("ErrorNumber", outputErrorCode);
		setOutputTags(outputTags);

	}
}
