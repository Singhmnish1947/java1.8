
package com.trapedza.bankfusion.fatoms;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.ub.common.events.CommonsEventCodes;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_KYC_StandingOrder;
import com.trapedza.bankfusion.steps.refimpl.IUB_KYC_StandingOrder;
import com.trapedza.bankfusion.utils.GUIDGen;

public class UB_KYC_StandingOrder extends AbstractUB_KYC_StandingOrder implements IUB_KYC_StandingOrder {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    /**
     */

    /**
     * The logger instance for this feature
     */
    private transient final static Log logger = LogFactory.getLog(UB_KYC_StandingOrder.class.getName());

    public UB_KYC_StandingOrder(BankFusionEnvironment env) {
        super(env);
        // TODO Auto-generated constructor stub
    }

    String Status = "0";

    public void process(BankFusionEnvironment env) {

        logger.info("UB_KYC_SOFircosoft started===============>>>>");
        HashMap CustomerTypeForAccount = new HashMap();
        String CustomerType = CommonConstants.EMPTY_STRING;

        if (env.getData().containsKey(getF_IN_AccId())) {
            logger.debug("UB_KYC_SOFircosoft inside if===============>>>>");
            // env.getData().remove("TransactionProcessed");
        }
        else {
            env.getData().put(getF_IN_AccId(), "Y");

            String BeneficiarySettlement = getF_IN_BeneficiarySettlement();
            String BeneficiaryAccNo = getF_IN_BeneficiaryAccno();
            String SetUpAmount = (getF_IN_SetUpCurrency().toString());
            String PaidAmount = (getF_IN_PaidCurrency().toString());
            String PaidCurrency = getF_IN_PaidCurrencyCode();
            String SetUpCurrency = getF_IN_SetUpCurrencyCode();

            // Finding Details Of Debit Customer
            HashMap AccountId = new HashMap();
            AccountId.put("AccountNo", getF_IN_AccId());
            HashMap CustDetail = new HashMap();
            CustDetail = MFExecuter.executeMF("UB_KYC_GetCustomerDetailsFromAccount_SRV", env, AccountId);
            CustDetail.get("Address1");
            CustDetail.get("Address2");
            CustDetail.get("Address3");
            CustDetail.get("CountryOfIncorporation");
            CustDetail.get("CountryOfResidence");
            CustDetail.get("CountryofOrigin");
            CustDetail.get("EmpAddress");
            CustDetail.get("EmployerName");
            CustDetail.get("FirstName");
            CustDetail.get("MaidenName");
            CustDetail.get("MiddleName");
            CustDetail.get("Nationality");
            CustDetail.get("PassportNumber");
            CustDetail.get("RegisteredName");
            CustDetail.get("RegisteredNumber");
            CustDetail.get("SurName");
            CustDetail.get("TradeName");
            CustDetail.get("DateOfBirth");

            // Debit Customer Detail

            HashMap FircosoftCustDetail = new HashMap();

            FircosoftCustDetail.put("message1", CustDetail.get("Address1"));
            FircosoftCustDetail.put("message2", CustDetail.get("Address2"));
            FircosoftCustDetail.put("message3", CustDetail.get("Address3"));
            FircosoftCustDetail.put("message4", CustDetail.get("CountryOfIncorporation"));
            FircosoftCustDetail.put("message5", CustDetail.get("RegisteredName"));
            FircosoftCustDetail.put("message6", CustDetail.get("RegisteredNumber"));
            FircosoftCustDetail.put("message7", CustDetail.get("TradeName"));
            FircosoftCustDetail.put("message8", CustDetail.get("CountryOfResidence"));
            FircosoftCustDetail.put("message9", CustDetail.get("CountryofOrigin"));
            FircosoftCustDetail.put("message10", (CustDetail.get("DateOfBirth")).toString());
            FircosoftCustDetail.put("message11", CustDetail.get("EmpAddress"));
            FircosoftCustDetail.put("message12", CustDetail.get("EmployerName"));
            FircosoftCustDetail.put("message13", CustDetail.get("FirstName"));
            FircosoftCustDetail.put("message14", CustDetail.get("PassportNumber"));
            FircosoftCustDetail.put("message15", CustDetail.get("MaidenName"));
            FircosoftCustDetail.put("message16", CustDetail.get("MiddleName"));
            FircosoftCustDetail.put("message17", CustDetail.get("Nationality"));

            // Finding Details of Credit Customer
            HashMap BeneficiaryAccountId = new HashMap();
            BeneficiaryAccountId.put("ACCOUNTID", BeneficiaryAccNo);
            CustomerTypeForAccount = MFExecuter.executeMF("UB_CHG_GetCustomerTypeForAccount_SRV", env, BeneficiaryAccountId);
            CustomerType = CustomerTypeForAccount.get("CUSTOMER_CUSTOMERTYPE").toString();

            if (!CustomerType.equals("I")) {

                BeneficiaryAccountId.clear();
                BeneficiaryAccountId.put("AccountNo", BeneficiaryAccNo);
                HashMap BeneficiaryCustDetail = new HashMap();

                BeneficiaryCustDetail = MFExecuter.executeMF("UB_KYC_GetCustomerDetailsFromAccount_SRV", env, BeneficiaryAccountId);

                BeneficiaryCustDetail.get("Address1");
                BeneficiaryCustDetail.get("Address2");
                BeneficiaryCustDetail.get("Address3");
                BeneficiaryCustDetail.get("CountryOfIncorporation");
                BeneficiaryCustDetail.get("CountryOfResidence");
                BeneficiaryCustDetail.get("CountryofOrigin");
                BeneficiaryCustDetail.get("EmpAddress");
                BeneficiaryCustDetail.get("EmployerName");
                BeneficiaryCustDetail.get("FirstName");
                BeneficiaryCustDetail.get("MaidenName");
                BeneficiaryCustDetail.get("MiddleName");
                BeneficiaryCustDetail.get("Nationality");
                BeneficiaryCustDetail.get("PassportNumber");
                BeneficiaryCustDetail.get("RegisteredName");
                BeneficiaryCustDetail.get("RegisteredNumber");
                BeneficiaryCustDetail.get("SurName");
                BeneficiaryCustDetail.get("TradeName");
                BeneficiaryCustDetail.get("DateOfBirth");

                // BeneficiaryCustomer Detail
                FircosoftCustDetail.put("message18", BeneficiaryCustDetail.get("Address1"));
                FircosoftCustDetail.put("message19", BeneficiaryCustDetail.get("Address2"));
                FircosoftCustDetail.put("message20", BeneficiaryCustDetail.get("Address3"));
                FircosoftCustDetail.put("message21", BeneficiaryCustDetail.get("CountryOfIncorporation"));
                FircosoftCustDetail.put("message22", BeneficiaryCustDetail.get("RegisteredName"));
                FircosoftCustDetail.put("message23", BeneficiaryCustDetail.get("RegisteredNumber"));
                FircosoftCustDetail.put("message24", BeneficiaryCustDetail.get("TradeName"));
                FircosoftCustDetail.put("message25", BeneficiaryCustDetail.get("CountryOfResidence"));
                FircosoftCustDetail.put("message26", BeneficiaryCustDetail.get("CountryofOrigin"));
                FircosoftCustDetail.put("message27", (BeneficiaryCustDetail.get("DateOfBirth")).toString());
                FircosoftCustDetail.put("message28", BeneficiaryCustDetail.get("EmpAddress"));
                FircosoftCustDetail.put("message29", BeneficiaryCustDetail.get("EmployerName"));
                FircosoftCustDetail.put("message30", BeneficiaryCustDetail.get("FirstName"));
                FircosoftCustDetail.put("message31", BeneficiaryCustDetail.get("PassportNumber"));
                FircosoftCustDetail.put("message32", BeneficiaryCustDetail.get("MaidenName"));
                FircosoftCustDetail.put("message33", BeneficiaryCustDetail.get("MiddleName"));
                FircosoftCustDetail.put("message34", BeneficiaryCustDetail.get("Nationality"));
            }
            // Other Details

            FircosoftCustDetail.put("message35", SetUpAmount);
            FircosoftCustDetail.put("message36", PaidAmount);
            FircosoftCustDetail.put("message37", PaidCurrency);
            FircosoftCustDetail.put("message38", SetUpCurrency);
            FircosoftCustDetail.put("MESSAGEID", GUIDGen.getNewGUID());

            HashMap refferralDetails = new HashMap();
            // To be uncommented when UB_KYC_FIC_FircosoftRequest_SRV sevice
            // is ready
            HashMap Noofrecords = new HashMap();
            try {
                Noofrecords = MFExecuter.executeMF("UB_KYC_FIC_FircosoftRequest_SRV", env, FircosoftCustDetail);
                Status = (String) Noofrecords.get("Status");
            }
            catch (BankFusionException e) {

                EventsHelper.handleEvent(CommonsEventCodes.E_COULD_NOT_ESTABLISH_CONNECTION_WITH_FIRCOSOFT, new Object[] {},
                        refferralDetails, env);
                logger.error(ExceptionUtil.getExceptionAsString(e));
            }

            // Noofrecords = 1;
            logger.debug("Fircosoft inside else===============>>>>");

        }

        setF_OUT_NoOfRecords(Status);

    }
}
