package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.List;

import bf.com.misys.bankfusion.attributes.UserDefinedFields;
import bf.com.misys.cbs.types.ListAccountDetails;
import bf.com.misys.cbs.types.ListAccounts;

import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOUDFEXTAttributeCollectionFeature;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ACC_PopulateAccountUDF;

public class PopulateAccountUDFs extends AbstractUB_ACC_PopulateAccountUDF{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6620704416102673822L;

	public PopulateAccountUDFs()
	{
		super();
	}
	
	public PopulateAccountUDFs(BankFusionEnvironment env)
	{
		super(env);
	}

	@Override
	public void process(BankFusionEnvironment env)
	{
		ListAccounts listAccounts = getF_IN_listAccounts();
		for(ListAccountDetails accounts : listAccounts.getListAccountDetails())
		{
			String accountID = accounts.getAccountInfo().getAcctBasicDetails().getAccountKeys().getStandardAccountId();
			accounts.getAccountInfo().getAcctBasicDetails().setUserExtension(populateUDFFieldsValues(accountID));
			
		}
		setF_OUT_listAccounts(listAccounts);
	}
	
	private Object populateUDFFieldsValues(String accountID)
    {
    	ArrayList<String> param = new ArrayList<String>();
    	param.add(accountID);
    	
    	//getting all the UDF field values for the given accountID
    	
    	IBOUDFEXTAttributeCollectionFeature result=(IBOUDFEXTAttributeCollectionFeature)com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal.getPersistanceFactory().findByPrimaryKey(IBOUDFEXTAttributeCollectionFeature.BONAME, accountID, true);

    	UserDefinedFields userDefinedFields = null;
    	
    	if (result!=null) {
			userDefinedFields = result.getUserDefinedFields();
			userDefinedFields.setAssociatedBoName(IBOAttributeCollectionFeature.BONAME);
		}
    	return userDefinedFields;
    }
}
