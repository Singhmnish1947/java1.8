package com.trapedza.bankfusion.fatoms;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOCB_CMN_ModuleConfiguration;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_DataMaintenanceFatom;

public class SWT_DataMaintenanceFatom extends AbstractSWT_DataMaintenanceFatom {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	private transient final static Log logger = LogFactory.getLog(SWT_DataMaintenanceFatom.class.getName());
	HashMap paramNameValues = new HashMap();

	public SWT_DataMaintenanceFatom(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) {
		if (getF_IN_Mode().equals("CREATE")) {
			insertAllRecords(env);
		}
		else {
			validateInputTags(env);
		}
	}

	private void validateInputTags(BankFusionEnvironment env) {
		String valueCheckHolder = getF_IN_ModuleName();
		logger.debug("moduleName-->" + valueCheckHolder);
		if (isEmptyOrNull(valueCheckHolder))
			setF_OUT_ModuleName("Global");
		else
			setF_OUT_ModuleName(getF_IN_ModuleName());

		valueCheckHolder = getF_IN_ParamName();
		logger.debug("paramName-->" + valueCheckHolder);
		if (!isEmptyOrNull(valueCheckHolder))
			setF_OUT_ParamName(getF_IN_ParamName());

		valueCheckHolder = getF_IN_ParamValue();
		logger.debug("paramValue-->" + valueCheckHolder);
		if (!isEmptyOrNull(valueCheckHolder))
			setF_OUT_ParamValue(getF_IN_ParamValue());

		valueCheckHolder = getF_IN_ParamDesc();
		logger.debug("paramDesc-->" + valueCheckHolder);
		if (!isEmptyOrNull(valueCheckHolder))
			setF_OUT_ParamDesc(getF_IN_ParamDesc());

		valueCheckHolder = getF_IN_ParamDataType();
		logger.debug("paramDataType-->" + valueCheckHolder);
		if (!isEmptyOrNull(valueCheckHolder))
			setF_OUT_ParamDataType(getF_IN_ParamDataType());

		valueCheckHolder = getF_IN_Mode();
		logger.debug("MODE FOUND IS-->" + valueCheckHolder);

		try {
			if (!isEmptyOrNull(valueCheckHolder) && "I".equals(valueCheckHolder)) {
				getAllParamValues(env);
				logger.debug("getAllParamValues module name" + getF_OUT_ModuleName());
				logger.debug("getAllParamValues param name" + getF_IN_ParamName());
				logger.debug("getAllParamValues param name from hash: " + paramNameValues.get(getF_OUT_ModuleName()));
				if (paramNameValues != null && paramNameValues.size() > 0) {
					ArrayList arryLst = (ArrayList) paramNameValues.get(getF_OUT_ModuleName());
					String paramName = CommonConstants.EMPTY_STRING;
					String moduleName = CommonConstants.EMPTY_STRING;
					boolean recordExists = false;
					if (arryLst != null && arryLst.size() > 0) {
						for (int i = 0; i < arryLst.size(); i++) {
							paramName = getF_IN_ParamName();
							moduleName = (String) arryLst.get(i);
							if (paramName.equalsIgnoreCase(moduleName))
								recordExists = true;
						}
					}
					if (recordExists) {
						logger.debug("The ModuleName = " + moduleName + " and ParamName =" + paramName
								+ " already exists in the database");						
						/*throw new BankFusionException(9522,
								"The 'ModuleName' and 'ParamName' already exists in the database");*/
				//		SQL Message : The ModuleName {0} and ParamName {1} already exists in the database
						EventsHelper.handleEvent(ChannelsEventCodes.THE_MODULE_PARAM_ALREADY_EXISTS,
								new Object[]{moduleName,paramName}, new HashMap(), env);
					}

				}
			}
		}
		catch (Exception ex) {
			//throw new BankFusionException(9522, ex.getLocalizedMessage());
			EventsHelper.handleEvent(ChannelsEventCodes.THE_MODULE_PARAM_ALREADY_EXISTS, new Object[]{ex.getLocalizedMessage()}, new HashMap(), env);
		logger.error(ExceptionUtil.getExceptionAsString(ex));
		}

	}

	private boolean isEmptyOrNull(String str) {
		if (str == null || str.trim().length() == 0 || "null".equals(str))
			return true;
		return false;
	}

	/**
	 * Get All Param Names & Values and Cache It 
	 * 
	 * @param
	 * @throws
	 */
	private void getAllParamValues(BankFusionEnvironment env) {
		List paramValuesList = null;
		paramNameValues = new HashMap();
		ArrayList valueList = null;
		try {
			paramValuesList = env.getFactory().findAll(IBOCB_CMN_ModuleConfiguration.BONAME, null);
			for (int i = 0; i < paramValuesList.size(); i++) {
				IBOCB_CMN_ModuleConfiguration moduleConfigObj = (IBOCB_CMN_ModuleConfiguration) paramValuesList.get(i);
				logger.debug("moduleConfigObj :getAll-MN :" + moduleConfigObj.getF_MODULENAME());
				logger.debug("moduleConfigObj :getAll-PN :" + moduleConfigObj.getF_PARAMNAME());
				valueList = new ArrayList();
				if (moduleConfigObj.getF_MODULENAME() != null && moduleConfigObj.getF_PARAMNAME() != null) {
					if (paramNameValues != null && paramNameValues.size() > 0
							&& paramNameValues.containsKey(moduleConfigObj.getF_MODULENAME())) {
						valueList = (ArrayList) paramNameValues.get((String) moduleConfigObj.getF_MODULENAME());
						if (moduleConfigObj.getF_PARAMNAME() != null)
							valueList.add(moduleConfigObj.getF_PARAMNAME());
						paramNameValues.put(moduleConfigObj.getF_MODULENAME(), valueList);
					}
					else {
						if (moduleConfigObj.getF_PARAMNAME() != null)
							valueList.add(moduleConfigObj.getF_PARAMNAME());
						paramNameValues.put(moduleConfigObj.getF_MODULENAME(), valueList);
					}
				}
			}
			logger.debug("paramNameValues-->" + paramNameValues);

		}
		catch (Exception ex) {
			//throw new BankFusionException(9522, ex.getLocalizedMessage());
			EventsHelper.handleEvent(ChannelsEventCodes.THE_MODULE_PARAM_ALREADY_EXISTS, new Object[]{ex.getLocalizedMessage()}, new HashMap(), env);
		logger.error(ExceptionUtil.getExceptionAsString(ex));
		}
	}

	/**
	 * Multiple insert into ModuleConfigurationBO
	 * @param moduleConfiguration
	 * @param env
	 */
	private void insertAllRecords(BankFusionEnvironment env) {
		IBOCB_CMN_ModuleConfiguration moduleConfiguration = null;
		boolean flag = false;
		try {
			Class obj = Class.forName("com.trapedza.bankfusion.fatoms.SWT_DataMaintenanceFatom");
			//SWT_DataMaintenanceFatom dataMaintenanceFatom=(SWT_DataMaintenanceFatom)obj.newInstance();		
			Method meth = obj.getMethod("getF_IN_ModuleName", new Class[] {});
			Object moduleName = meth.invoke(this, new Object[] {});
			for (int j = 0; j <= 9; j++) {

				moduleConfiguration = (IBOCB_CMN_ModuleConfiguration) env.getFactory().getStatelessNewInstance(
						IBOCB_CMN_ModuleConfiguration.BONAME);
				moduleConfiguration.setF_MODULENAME(moduleName.toString());
				if (!flag) {
					moduleConfiguration.setF_MODULENAME(getF_IN_ModuleName());
					moduleConfiguration.setF_PARAMNAME(getF_IN_ParamName());
					moduleConfiguration.setF_MODULENAME(getF_IN_ParamValue());
					moduleConfiguration.setF_PARAMNAME(getF_IN_ParamDataType());
					moduleConfiguration.setF_MODULENAME(getF_IN_ParamDesc());
					flag = true;
				}
				else {

					Object paramName = obj.getMethod("getF_IN_ParamName" + j, new Class[] {}).invoke(this,
							new Object[] {});
					moduleConfiguration.setF_MODULENAME(paramName.toString());
					Object paramValue = obj.getMethod("getF_IN_ParamValue" + j, new Class[] {}).invoke(this,
							new Object[] {});
					moduleConfiguration.setF_MODULENAME(paramValue.toString());
					Object paramDataType = obj.getMethod("getF_IN_ParamDataType" + j, new Class[] {}).invoke(this,
							new Object[] {});
					moduleConfiguration.setF_PARAMNAME(paramDataType.toString());
					Object paramDesc = obj.getMethod("getF_IN_ParamDesc" + j, new Class[] {}).invoke(this,
							new Object[] {});
					moduleConfiguration.setF_MODULENAME(paramDesc.toString());
					flag = true;
				}
				env.getFactory().create(IBOCB_CMN_ModuleConfiguration.BONAME, moduleConfiguration);
			}

		}
		catch (BankFusionException bfe) {
			logger.error(ExceptionUtil.getExceptionAsString(bfe));
		}
		catch (Exception e) {
			logger.error(ExceptionUtil.getExceptionAsString(e));
		}
	}
}
