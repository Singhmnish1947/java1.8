package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOAccountLimitFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOCreditInterestFeature;
import com.trapedza.bankfusion.bo.refimpl.IBODebitInterestFeature;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.CurrencyValue;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.gateway.persistence.interfaces.IPagingData;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.persistence.core.PagingData;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.AvailableBalanceFunction;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractBPW_InquiryAccountList;
import com.trapedza.bankfusion.steps.refimpl.IBPW_InquiryAccountList;

public class BPW_InquiryAccountList extends AbstractBPW_InquiryAccountList implements IBPW_InquiryAccountList {

    /**
	 */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }
    

    public BPW_InquiryAccountList(BankFusionEnvironment env) {
        super(env);
    }
    
    private static final String whereClause = " WHERE " + IBOAccount.CUSTOMERCODE + " = ? AND " + IBOAccount.ACCOUNTID
            + " > ? ORDER BY " + IBOAccount.ACCOUNTID;
    private static final String whereClause1 = " WHERE " + IBOAccount.CUSTOMERCODE + " = ? ORDER BY " + IBOAccount.ACCOUNTID;

    private IPersistenceObjectsFactory factory;

    public void process(BankFusionEnvironment env) {

        String customerNumber = getF_IN_CustomerNumber();
        String accNum = getF_IN_AccountNum();
        BigDecimal CrInterest =BigDecimal.ZERO;
        BigDecimal DrInterest = BigDecimal.ZERO;
        // Current BankFusion Business date
        Timestamp businessDate = SystemInformationManager.getInstance().getBFBusinessDateTime();

        VectorTable accountVector = getF_OUT_AccountResultSet();
        HashMap accountMap = new HashMap();
       
        factory = BankFusionThreadLocal.getPersistanceFactory();
        if (customerNumber != null) {
            int counter = 0;
            
            /*
            Iterator it = FinderMethods.findAccountByCustomerCode(customerNumber).iterator();
            if (!accNum.equals(CommonConstants.EMPTY_STRING)) {
                while (it.hasNext()) {
                    accountObj = (SimplePersistentObject) it.next();
                    accountNumber = (String) accountObj.getDataMap().get(IBOAccount.ACCOUNTID);
                    if (accountNumber.equals(accNum))
                        break;
                }
            }
            */
            IPagingData pagingData = new PagingData(1, 10);
            pagingData.setCurrentPageNumber(1);
            pagingData.setRequiresTotalPages(false);
            pagingData.setPageSize(10);
            Iterator it = null;			
            ArrayList<String> params = new ArrayList<String>();
            params.add(customerNumber);
            if (accNum != null && accNum.trim().length() > 0) {
                params.add(accNum);
                it = factory.findByQuery(IBOAccount.BONAME, whereClause, params, pagingData, false).iterator();
            }
            else {
                it = factory.findByQuery(IBOAccount.BONAME, whereClause1, params, pagingData, false).iterator();
            }
            while (it.hasNext() && counter < 10) {
                SimplePersistentObject account = (SimplePersistentObject) it.next();
                String Account = (String) account.getDataMap().get(IBOAccount.ACCOUNTID);
                String AccDesc = (String) account.getDataMap().get(IBOAccount.ACCOUNTDESCRIPTION);
                String Accname = (String) account.getDataMap().get(IBOAccount.ACCOUNTNAME);
                BigDecimal debitLimit = (BigDecimal) account.getDataMap().get(IBOAccount.DEBITLIMIT);
                BigDecimal Bookedbalance = (BigDecimal) account.getDataMap().get(IBOAccount.BOOKEDBALANCE);
                // available balance is shown as cleared balance in account list since BMLAN does
                // the same.
                BigDecimal clearedbalanceWithoutCreditLimit = getAvailableBalance(Account);
                BigDecimal clearedbalance = clearedbalanceWithoutCreditLimit;

                // Read Account Limit Details
                IBOAccountLimitFeature accountLimitItem = (IBOAccountLimitFeature) BankFusionThreadLocal.getPersistanceFactory()
                        .findByPrimaryKey(IBOAccountLimitFeature.BONAME, Account, true);
                Timestamp limitExpiryDate = accountLimitItem.getF_LIMITEXPIRYDATE();
                if (clearedbalanceWithoutCreditLimit.signum() > 0 || limitExpiryDate.compareTo(businessDate) > 0) {
                    clearedbalance = clearedbalanceWithoutCreditLimit.subtract(debitLimit);
                }
                String currencyCode = ((CurrencyValue) account.getDataMap().get(IBOAccount.BOOKEDBALANCE)).getCurrencyCode();
                CurrencyValue clearedbalanceWithCurrency = new CurrencyValue(currencyCode, clearedbalance);
                String Brchsortcode = (String) account.getDataMap().get(IBOAccount.BRANCHSORTCODE);
                String Currencycode = (String) account.getDataMap().get(IBOAccount.ISOCURRENCYCODE);
                int accRigthsIndicator = (Integer) account.getDataMap().get(IBOAccount.ACCRIGHTSINDICATOR);

                accountMap.put("ACCOUNTID", Account);
                accountMap.put("BOTYPEID", Account);
                accountMap.put("BOID", Account);
                accountMap.put("ACCOUNTNAME", Accname);
                accountMap.put("BRANCHSORTCODE", "00" + Brchsortcode);
                accountMap.put("ISOCURRENCYCODE", Currencycode);
                accountMap.put("ACCRIGHTSINDICATOR", accRigthsIndicator);

                if (accRigthsIndicator == 8 || accRigthsIndicator == -1) {
                	 HashMap<String, Integer> inputs = new HashMap<String, Integer>();
                     inputs.put("EVENTNUMBER", 40409358);
                     HashMap<String, String> result = MFExecuter.executeMF("CB_CMN_GetEventMessage_SRV", env, inputs);
                     String formattedMessage = result.get("FormattedMessage");
                     accountMap.put("ACCOUNTDESCRIPTION", formattedMessage);
                     accountMap.put("BOOKEDBALANCE", CommonConstants.BIGDECIMAL_ZERO);
                     accountMap.put("CLEAREDBALANCE", CommonConstants.BIGDECIMAL_ZERO);
                    
                }
                else {
                    accountMap.put("ACCOUNTDESCRIPTION", AccDesc);
                    accountMap.put("BOOKEDBALANCE", Bookedbalance);
                    accountMap.put("CLEAREDBALANCE", clearedbalanceWithCurrency);
                
                }

                /* Credit Interest Table */
                String CrInterestwhere = " WHERE " + IBOCreditInterestFeature.ACCOUNTID + " = ? ";
                ArrayList crinTparams = new ArrayList();

                crinTparams.add((String) account.getDataMap().get(IBOAccount.ACCOUNTID));
                IBOCreditInterestFeature crIntFeat = null;
                try {
                    crIntFeat = (IBOCreditInterestFeature) env.getFactory().findFirstByQuery(IBOCreditInterestFeature.BONAME,
                            CrInterestwhere, crinTparams);
                }
                catch (Exception e) {
                    crIntFeat = null;
                }
                if (crIntFeat != null)
                    CrInterest = (BigDecimal) crIntFeat.getF_ACCDCRINTEREST();

                /* Debit Interest Table */
                String DrInterestwhere = " WHERE " + IBODebitInterestFeature.ACCOUNTID + " = ? ";
                ArrayList drinTparams = new ArrayList();

                drinTparams.add((String) account.getDataMap().get(IBOAccount.ACCOUNTID));

                IBODebitInterestFeature drIntFeat = null;
                try {
                    drIntFeat = (IBODebitInterestFeature) env.getFactory().findFirstByQuery(IBODebitInterestFeature.BONAME,
                            DrInterestwhere, drinTparams);
                }
                catch (Exception e) {
                    drIntFeat = null;
                }

                if (drIntFeat != null)
                    DrInterest = (BigDecimal) drIntFeat.getF_DEBITACCDINTEREST();

                /*
                 * CREDITLIMIT field of Account Table is utlisied to have accrued interest value
                 * since the table doesn't contain any corressponding filed for it
                 */
                accountMap.put("CREDITLIMIT", CrInterest.add(DrInterest));

                /* To get AccountType 
                String accTypeWhere = " WHERE " + IBOProductInheritance.PRODUCTCONTEXTCODE + " = ? ";
                ArrayList accTypeparams = new ArrayList();

                accTypeparams.add((String) account.getDataMap().get(IBOAccount.PRODUCTCONTEXTCODE));

                IBOProductInheritance accType = null;
                try {
                    accType = (IBOProductInheritance) env.getFactory().findFirstByQuery(IBOProductInheritance.BONAME, accTypeWhere,
                            accTypeparams);
                }
                catch (Exception e) {
                    accType = null;
                }*/

                accountVector.addAll(new VectorTable(accountMap));
                counter++;
            }
            setF_OUT_AccountResultSet(accountVector);
            setF_OUT_NUMOFROWS(new Integer(counter));
        }
    }

    // new method added to fetch available balance for a given account
    private BigDecimal getAvailableBalance(String accountID) {
        HashMap result = new HashMap();
        BigDecimal availableBalance = BigDecimal.ZERO;
        result = AvailableBalanceFunction.run(accountID);
        availableBalance = (BigDecimal) result.get("AvailableBalanceWithOutCreditLimit");
        return availableBalance;
    }

}
