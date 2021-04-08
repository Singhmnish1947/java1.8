/**
 * * Copyright (c) 2008 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.

 */
package com.trapedza.bankfusion.fatoms;

/**
 * @author Gaurav.Aggarwal
 *
 */
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.security.runtime.impl.BranchUtil;
import com.misys.ub.common.GetUBConfigLocation;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_MessageHeader;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.FinderMethods;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_IncomingMatrixValue;

public class UB_SWT_IncomingMatrixValue extends AbstractUB_SWT_IncomingMatrixValue {
    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }
	static final String query1 = " WHERE " + IBOUB_INF_MessageHeader.REFERENCE	+ " = ?";
	IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
    final String SWIFT_PROPERTY_FILENAME = "SWIFT_Incoming.properties";
    String NostroBICode = null;
    private transient final static Log logger = LogFactory.getLog(UB_SWT_IncomingMatrixValue.class.getName());

    public UB_SWT_IncomingMatrixValue(BankFusionEnvironment env) {
        super(env);
    }

    public void process(BankFusionEnvironment env) {

        String Key = getF_IN_MessageType();
        String FundingParty = null;
        String FundingOption = "D";
        String FundingBIC = null;
        String BranchBic = null;

        String AccountNumber = CommonConstants.EMPTY_STRING;
        String AccountNumberWithoutSlash = CommonConstants.EMPTY_STRING;
        String Name = CommonConstants.EMPTY_STRING;
        String Name1 = CommonConstants.EMPTY_STRING;
        String IBICCode = CommonConstants.EMPTY_STRING;
        String ABICCode = CommonConstants.EMPTY_STRING;
        String BBICCode = CommonConstants.EMPTY_STRING;
        String transactionReference = getF_IN_TransactionReference();
        String messageStatus = CommonConstants.EMPTY_STRING;
        
        if(null != transactionReference && !transactionReference.equals(CommonConstants.EMPTY_STRING)) {
    		IBOUB_INF_MessageHeader messageHeader=null;
    		ArrayList param = new ArrayList();
    		param.add(transactionReference);
        	List msgHdrList =  factory.findByQuery(IBOUB_INF_MessageHeader.BONAME, query1, param, null);
    		if(msgHdrList!=null && msgHdrList.size()>0){
    			messageHeader=(IBOUB_INF_MessageHeader)msgHdrList.get(0);
        	messageStatus = messageHeader.getF_MESSAGESTATUS();
    		}
        }

        if (getF_IN_Intermediary().length() > 0 || getF_IN_IntermediaryOption().equals("A")) {
            FundingParty = "I";
            FundingOption = getF_IN_IntermediaryOption();
            FundingBIC = split(getF_IN_Intermediary(), messageStatus);


        }
        else if (getF_IN_AccountWith().length() > 0 || getF_IN_AccountWithOption().equals("A")) {
            FundingParty = "A";
            FundingOption = getF_IN_AccountWithOption();
            FundingBIC = split(getF_IN_AccountWith(), messageStatus);

        }
        if ((FundingBIC.length())== 8){
        	FundingBIC = FundingBIC+"XXX";

        }
        Key = Key + FundingParty + FundingOption;

        HashMap identiCodeList = new HashMap();
        HashMap identiCodeOutputList = new HashMap();
        identiCodeList.put("IdentifierCode", FundingBIC);
        identiCodeOutputList = MFExecuter.executeMF("UB_SWT_IdentifierCodeRead_SRV", env, identiCodeList);
        Boolean authorisationStatus = (Boolean) identiCodeOutputList.get("AuthorizationStatus");
        if (authorisationStatus) {
            Key = Key + "A";
        }
        else Key = Key + "N";

        if(FundingOption.equals("A")){
            IBOBranch branchObj = BranchUtil.getBranchDetailsInCurrentZone(getF_IN_BranchSortCode());
        BranchBic = branchObj.getF_BICCODE();
        String Branchlocation = BranchBic.substring(4, 6);
        String FundingLocation = FundingBIC.substring(4, 6);
        if (Branchlocation.equals(FundingLocation)) {
            Key = Key + "Y";
        }
        else {
            Key = Key + "N";
        }

        identiCodeList.clear();
        identiCodeOutputList.clear();

        identiCodeList.put("IdentifierCode", FundingBIC);
        // HashMap creditvalue=new HashMap();
        identiCodeOutputList = MFExecuter.executeMF("UB_SWT_GetDefault_NOSTRO_SRV", env, identiCodeList);
        String defaultNostroAccount = (String) identiCodeOutputList.get("DefaultNostroAccount");
        setF_OUT_DefaultNostroAccount(defaultNostroAccount);

        identiCodeList.clear();
        identiCodeOutputList.clear();
        /*
         * identiCodeList.put("AccountNumber",defaultNostroAccount);
         * HashMap creditvalue1 = new HashMap();
         * identiCodeOutputList = MFExecuter.executeMF("UB_SWT_ReturnBiCodeforAccNo_SRV", env, identiCodeList);
         * NostroBICode= (String)identiCodeOutputList.get("BICCode");
         */
        List customerList = FinderMethods.findCustomerByAccount(defaultNostroAccount, env, null);
        if (customerList.size() > 0) {
            String customerCode = ((IBOCustomer) customerList.get(0)).getBoID();
            NostroBICode = ((IBOSwtCustomerDetail) BankFusionThreadLocal.getPersistanceFactory().findByPrimaryKey(
                    IBOSwtCustomerDetail.BONAME, customerCode)).getF_BICCODE();
        }

        if (FundingBIC.equals(NostroBICode)) {
            Key = Key + "Y";
        }
        else {
            Key = Key + "N";
        }
  	}else
  	Key = Key + "N" +"N";

        Key = Key + getF_IN_DefaultNostro();
        if(logger.isInfoEnabled()){
        logger.info("In coming Matrix Value : " +  Key);
        }
        if (FundingOption.equals("A")) {
            Properties swiftProperties = new Properties();
            //String configLocation = System.getProperty("BFconfigLocation", CommonConstants.EMPTY_STRING);
            String configLocation = GetUBConfigLocation.getUBConfigLocation();
            try {
                FileInputStream is = new FileInputStream(configLocation + "conf//swift//" + SWIFT_PROPERTY_FILENAME);
                swiftProperties.load(is);
                String readingIncomingPropertiesFile = swiftProperties.getProperty(Key);

                if (readingIncomingPropertiesFile == null || readingIncomingPropertiesFile.charAt(0) == 'R') {

                    setF_OUT_ErrorNumber("09717");

                }
                else {
                    setF_OUT_Process("P");
                    setF_OUT_Credit(readingIncomingPropertiesFile.substring(1, 2));
                    setF_OUT_Generate103(readingIncomingPropertiesFile.substring(2, 3));
                    setF_OUT_Generate202(readingIncomingPropertiesFile.substring(3, 4));
                    setF_OUT_Generate205(readingIncomingPropertiesFile.substring(4, 5));
                    setF_OUT_intermediary(getvalue(readingIncomingPropertiesFile.charAt(6), "value", messageStatus));
                    setF_OUT_AccountWith(getvalue(readingIncomingPropertiesFile.charAt(7), "value", messageStatus));
                    setF_OUT_BeneficiaryInstitution(getvalue(readingIncomingPropertiesFile.charAt(8), "value", messageStatus));
                    setF_OUT_intermediaryOption(getvalue(readingIncomingPropertiesFile.charAt(6), "option", messageStatus));
                    setF_OUT_AccountWithOption(getvalue(readingIncomingPropertiesFile.charAt(7), "option", messageStatus));
                    setF_OUT_BeneficiaryOption(getvalue(readingIncomingPropertiesFile.charAt(8), "option", messageStatus));
                    setF_OUT_103Receiver(getvalue(readingIncomingPropertiesFile.charAt(2), "biccode", messageStatus));
                    setF_OUT_202Receiver(getvalue(readingIncomingPropertiesFile.charAt(3), "biccode", messageStatus));
                    setF_OUT_205Receiver(getvalue(readingIncomingPropertiesFile.charAt(4), "biccode", messageStatus));
                    if (readingIncomingPropertiesFile.substring(5, 6).equals("B")) {
                        setF_OUT_Sender(NostroBICode);
                    }
                    if(FundingBIC.equalsIgnoreCase(BranchBic))
                    {
                    	setF_OUT_Generate103("false");
                    }
                }

            }
            catch (FileNotFoundException e) {
               logger.error(ExceptionUtil.getExceptionAsString(e));
            }
            catch (IOException e) {
            	logger.error(ExceptionUtil.getExceptionAsString(e));
            }
        }
        else {
            setF_OUT_ErrorNumber("09717");
        }
    }

    private String getvalue(char i, String option, String messageStatus) {
        String Output = "";
        String biccode = "";
        String opt = "";
        switch (i) {
            case 'I':
                Output = getF_IN_Intermediary();
                biccode = split(getF_IN_Intermediary(), messageStatus);
                opt = getF_IN_IntermediaryOption();
                break;
            case 'A':
                Output = getF_IN_AccountWith();
                biccode = split(getF_IN_AccountWith(), messageStatus);
                opt = getF_IN_AccountWithOption();
                break;
            case 'B':
                Output = getF_IN_BeneficiaryInstitution();
                biccode = split(getF_IN_BeneficiaryInstitution(), messageStatus);
                opt = getF_IN_BeneficiaryInstitutionOption();
                break;
            case 'N':
                Output = NostroBICode;
                biccode = NostroBICode;
                opt = "A";
                break;
        }
        if ("value".equals(option))
            return Output;
        else if ("biccode".equals(option))
            return biccode;
        else return opt;
    }

    private String split(String text, String messageStatus) {
    	
    
    	if( messageStatus != null && (messageStatus.equals("W") || messageStatus.equals("R")  || messageStatus.equals("F") )) {
        String[] arrays = new String[3];
        arrays = text.split("[$]");
        return arrays[0];
    } 
    	else 
    	{
    		String[] arrays = new String[3];
            arrays = text.split("[$]");
            return arrays[arrays.length - 1];
    		
    	}
    }
    	
}