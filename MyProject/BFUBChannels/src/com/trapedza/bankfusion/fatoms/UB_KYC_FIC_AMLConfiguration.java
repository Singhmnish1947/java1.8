package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.microflow.ActivityStep;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_KYC_FIC_AMLConfiguration;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_CNF_ProdTransCdException;
import com.trapedza.bankfusion.bo.refimpl.IBOProductInheritance;
import com.trapedza.bankfusion.bo.refimpl.IBOUBTB_PRODTRANSAMLCONFIG;
import com.trapedza.bankfusion.boundary.outward.BankFusionPropertySupportImpl;

public class UB_KYC_FIC_AMLConfiguration extends AbstractUB_KYC_FIC_AMLConfiguration {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	public UB_KYC_FIC_AMLConfiguration(BankFusionEnvironment env) {
        super(env);
        // TODO Auto-generated constructor stub
    }
	private static final String query="WHERE "
		+ IBOProductInheritance.PRODUCTCONTEXTCODE +" = ? ";
	private static final String exceptionCodeQuery="WHERE "
		+ IBOUB_CNF_ProdTransCdException.UBPRODUCTCONTEXTCODE +" = ? ";
	private static final String transCode="WHERE "
		+ IBOMisTransactionCodes.CODE +" = ? ";
	private static final String AmlCheckClause = "WHERE "
		+ IBOUBTB_PRODTRANSAMLCONFIG.UBPRODUCTCONTEXTCODE + " = ? AND "
		+ IBOUBTB_PRODTRANSAMLCONFIG.UBMISTRANSACTIONCODE + "=?";

	public void process(BankFusionEnvironment env) throws BankFusionException {
		VectorTable resultVector = new VectorTable();
        Map newMap = null;
        newMap = new HashMap();
        if (getF_IN_AllAllowed().equals("N")){
        	int Flag = 0;
        ArrayList params = new ArrayList();
        ArrayList param = new ArrayList();
		params.add(getF_IN_Product());
		ArrayList exceptionalTransactionCodes = new ArrayList();
		ArrayList productCodes = new ArrayList();
		productCodes = (ArrayList) env.getFactory()
		.findByQuery(IBOProductInheritance.BONAME, query,
				params, null);
		exceptionalTransactionCodes = (ArrayList) env.getFactory()
		.findByQuery(IBOUB_CNF_ProdTransCdException.BONAME, exceptionCodeQuery,
				params, null);
		Iterator iterator1 = exceptionalTransactionCodes.iterator();
		Iterator transactionList = BankFusionThreadLocal.getPersistanceFactory().findAll(IBOMisTransactionCodes.BONAME, null, false).iterator();
		Boolean isAllTransctionCodeAllowed = false;
		
		if (productCodes.size() > 0) {
			SimplePersistentObject transactionCodesObject = (SimplePersistentObject) productCodes
					.get(0);
			isAllTransctionCodeAllowed = (Boolean) (transactionCodesObject.getDataMap().get(
					IBOProductInheritance.ISALLTRANSCODEALLOWED));
		}
		if (isAllTransctionCodeAllowed == true){
			if (exceptionalTransactionCodes.size() >0){
				while (transactionList.hasNext()){
					IBOMisTransactionCodes obj = (IBOMisTransactionCodes) transactionList.next();
					iterator1 = exceptionalTransactionCodes.iterator();
					while (iterator1.hasNext()){
						IBOUB_CNF_ProdTransCdException obj1 = (IBOUB_CNF_ProdTransCdException) iterator1.next();
						if (!(obj.getBoID().equals(obj1.getF_UBMISTRANSACTIONCODE()))){
						param.add(getF_IN_Product());
						param.add(obj.getBoID());
						ArrayList amlCheckResult = new ArrayList();
						amlCheckResult = (ArrayList) env.getFactory()
								.findByQuery(IBOUBTB_PRODTRANSAMLCONFIG.BONAME, AmlCheckClause,
										param, null);
						String amlCheck = "No";
						if (amlCheckResult.size() > 0) {
							SimplePersistentObject amlCheckSimpleObject = (SimplePersistentObject) amlCheckResult
							.get(0);
							amlCheck = (String) amlCheckSimpleObject.getDataMap().get(
							IBOUBTB_PRODTRANSAMLCONFIG.UBISAMLFLAG);
							if (amlCheck.equals("Y")){
								amlCheck = "Yes";
							}
							else {
								Flag = 1;
							}
							}else
								Flag =1;
								newMap.put("TransactionCode", obj.getBoID());
								newMap.put("Description", obj.getF_DESCRIPTION());
								newMap.put("AMLCheckRequired", amlCheck);
								resultVector.addAll(new VectorTable(newMap));
								param.clear();
								break;
					}
				}
					
					
					
					
			}
		}else {
			while (transactionList.hasNext()){
				IBOMisTransactionCodes obj = (IBOMisTransactionCodes) transactionList.next();
					param.add(getF_IN_Product());
					param.add(obj.getBoID());
					ArrayList amlCheckResult = new ArrayList();
					amlCheckResult = (ArrayList) env.getFactory()
							.findByQuery(IBOUBTB_PRODTRANSAMLCONFIG.BONAME, AmlCheckClause,
									param, null);
					String amlCheck = "No";
					if (amlCheckResult.size() > 0) {
						SimplePersistentObject amlCheckSimpleObject = (SimplePersistentObject) amlCheckResult
						.get(0);
						amlCheck = (String) (amlCheckSimpleObject.getDataMap().get(
						IBOUBTB_PRODTRANSAMLCONFIG.UBISAMLFLAG));
						if (amlCheck.equals("Y")){
							amlCheck = "Yes";
						}
						else {
							Flag = 1;
						}
						}else
							Flag = 1;
						newMap.put("TransactionCode", obj.getBoID());
						newMap.put("Description", obj.getF_DESCRIPTION());
						newMap.put("AMLCheckRequired", amlCheck);
						resultVector.addAll(new VectorTable(newMap));
						param.clear();
				}
			}
		}
		
		else {
			while (iterator1.hasNext()){
				param.add(getF_IN_Product());
				IBOUB_CNF_ProdTransCdException obj = (IBOUB_CNF_ProdTransCdException) iterator1.next();
				String o = (obj.getF_UBMISTRANSACTIONCODE()).toString();
				param.add(o);
				ArrayList amlCheckResult = new ArrayList();
				amlCheckResult = (ArrayList) env.getFactory()
						.findByQuery(IBOUBTB_PRODTRANSAMLCONFIG.BONAME, AmlCheckClause,
								param, null);
				String amlCheck = "No";
				if (amlCheckResult.size() > 0) {
					SimplePersistentObject amlCheckSimpleObject = (SimplePersistentObject) amlCheckResult
					.get(0);
					amlCheck = (String) amlCheckSimpleObject.getDataMap().get(
					IBOUBTB_PRODTRANSAMLCONFIG.UBISAMLFLAG);
					if (amlCheck.equals("Y")){
						amlCheck = "Yes";
					}
					else {
						Flag =1;
					}
					}else
						Flag =1;
					ArrayList list = new ArrayList();
					ArrayList list1 = new ArrayList();
					list.add(obj.getF_UBMISTRANSACTIONCODE());
					list1 =(ArrayList) env.getFactory()
					.findByQuery(IBOMisTransactionCodes.BONAME, transCode,
							list, null);
					String Description = null;
					if (list1.size()>0){
						SimplePersistentObject description = (SimplePersistentObject) list1
						.get(0);
						Description = (String) description.getDataMap().get(
								IBOMisTransactionCodes.DESCRIPTION);
					}
					newMap.put("TransactionCode", obj.getF_UBMISTRANSACTIONCODE());
					newMap.put("Description", Description);
					newMap.put("AMLCheckRequired", amlCheck);
					resultVector.addAll(new VectorTable(newMap));
				param.clear();
			}
		}
		if (Flag ==0){
			setF_OUT_AllTransactionAllowed(new Boolean(true));
		}else {
			setF_OUT_AllTransactionAllowed(new Boolean(false));
		}
		setF_OUT_Result(resultVector);
	}
        else if (getF_IN_AllAllowed().equals("Y")) {
        	VectorTable inputVectorTable = getF_IN_InputVector();
    		int size = inputVectorTable.size();
    		HashMap row = new HashMap();
    		for (int i = 0; i<size; i++){
    			row.putAll(inputVectorTable.getRowTags(i));
    			newMap.put("TransactionCode", row.get("TransactionCode"));
    			newMap.put("Description", row.get("Description"));
    			if (isF_IN_AllTransactionAllowed() == true)
    			newMap.put("AMLCheckRequired", "Yes");
    			else
    			newMap.put("AMLCheckRequired", "No");
    			resultVector.addAll(new VectorTable(newMap));	
    		}
    		setF_OUT_Result(resultVector);
        }
	}
	
}
