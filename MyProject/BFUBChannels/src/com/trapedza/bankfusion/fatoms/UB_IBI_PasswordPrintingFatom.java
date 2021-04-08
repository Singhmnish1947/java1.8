/* ********************************************************************************
 *  Copyright (c) 2002,2004 Trapedza Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Trapedza Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 *
 *
 ***********************************************************/
package com.trapedza.bankfusion.fatoms;

import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_IBI_PasswordPrintingFatom;
import com.trapedza.bankfusion.steps.refimpl.IUB_IBI_PasswordPrintingFatom;
/**
 *  This class genarates report based on the value of PRINTREQUESTTYPE input tag 
 * @author Manu.Chadha
 *
 */
public class UB_IBI_PasswordPrintingFatom extends AbstractUB_IBI_PasswordPrintingFatom implements IUB_IBI_PasswordPrintingFatom{

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	public UB_IBI_PasswordPrintingFatom(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * This method is called to generate reports using crystal report fatom. The type of report to
	 * be generated depends on the value of PRINTREQUESTTYPE input tag. 
	 */
	public void process(BankFusionEnvironment env){
				
		String printRequestType=getF_IN_PRINTREQUESTTYPE();
		CrystalReportFatom crystalFatom=new CrystalReportFatom(env);
		
		crystalFatom.setF_IN_PARAMETER_1_NAME("HOSTCUSTID");
		crystalFatom.setF_IN_PARAMETER_1_VALUE(getF_IN_HOSTCUSTID());
		
		crystalFatom.setF_IN_PARAMETER_2_NAME("CUSTNAME");
		crystalFatom.setF_IN_PARAMETER_2_VALUE(getF_IN_CUSTNAME());
		
		crystalFatom.setF_IN_PARAMETER_3_NAME("CUSTCONTACTADDR1");
		crystalFatom.setF_IN_PARAMETER_3_VALUE(getF_IN_CUSTCONTACTADDR1());
		
		crystalFatom.setF_IN_PARAMETER_4_NAME("CUSTCONTACTADDR2");
		crystalFatom.setF_IN_PARAMETER_4_VALUE(getF_IN_CUSTCONTACTADDR2());
		
		crystalFatom.setF_IN_PARAMETER_5_NAME("CUSTCONTACTADDR3");
        crystalFatom.setF_IN_PARAMETER_5_VALUE(getF_IN_CUSTCONTACTADDR3());
        
		crystalFatom.setF_IN_PARAMETER_6_NAME("CUSTCONTACTCITY");
		crystalFatom.setF_IN_PARAMETER_6_VALUE(getF_IN_CUSTCONTACTCITY());
		
		crystalFatom.setF_IN_PARAMETER_7_NAME("CUSTCONTACTCOUNTRY");
		crystalFatom.setF_IN_PARAMETER_7_VALUE(getF_IN_CUSTCONTACTCOUNTRY());
		
		crystalFatom.setF_IN_PARAMETER_8_NAME("CUSTCONTACTCOUNTY");
		crystalFatom.setF_IN_PARAMETER_8_VALUE(getF_IN_CUSTCONTACTCOUNTY());		
				
		crystalFatom.setF_IN_PARAMETER_9_NAME("CUSTCONTACTPOSTCODE");
		crystalFatom.setF_IN_PARAMETER_9_VALUE(getF_IN_CUSTCONTACTPOSTCODE());
		
		if(printRequestType.equals("0")){
			crystalFatom.setF_IN_PARAMETER_10_NAME("MOBILEPIN");		
			crystalFatom.setF_IN_PARAMETER_10_VALUE(getF_IN_FIELDTOBEPRINTED());
			crystalFatom.setF_IN_ReportName("UB_IBI_MobilePinLetter");
		}else if(printRequestType.equals("1")){
			crystalFatom.setF_IN_PARAMETER_10_NAME("MOBILEACTIVATECODE");	
			crystalFatom.setF_IN_PARAMETER_10_VALUE(getF_IN_FIELDTOBEPRINTED());
			crystalFatom.setF_IN_ReportName("UB_IBI_MobileActivationLetter");
		}else if(printRequestType.equals("2")){
			crystalFatom.setF_IN_PARAMETER_10_NAME("MOBILEPIN");
			crystalFatom.setF_IN_PARAMETER_10_VALUE(getF_IN_FIELDTOBEPRINTED());
			crystalFatom.setF_IN_ReportName("UB_IBI_MobilePinResetLetter");
		}else if(printRequestType.equals("3")){
			crystalFatom.setF_IN_PARAMETER_10_NAME("LOGONID");
			crystalFatom.setF_IN_PARAMETER_10_VALUE(getF_IN_FIELDTOBEPRINTED());
			crystalFatom.setF_IN_ReportName("UB_IBI_LogonIdLetter");			
		}else if(printRequestType.equals("4")){
			crystalFatom.setF_IN_PARAMETER_10_NAME("PASSWORD");
			crystalFatom.setF_IN_PARAMETER_10_VALUE(getF_IN_FIELDTOBEPRINTED());
			crystalFatom.setF_IN_ReportName("UB_IBI_PasswordLetter");	
		}else if(printRequestType.equals("5")){
			crystalFatom.setF_IN_PARAMETER_10_NAME("PASSWORD");
			crystalFatom.setF_IN_PARAMETER_10_VALUE(getF_IN_FIELDTOBEPRINTED());
			crystalFatom.setF_IN_ReportName("UB_IBI_PasswordResetLetter");	
		}else if(printRequestType.equals("6")){
			crystalFatom.setF_IN_PARAMETER_10_NAME("AUTHTYPE");
			crystalFatom.setF_IN_PARAMETER_10_VALUE(getF_IN_FIELDTOBEPRINTED());
			crystalFatom.setF_IN_ReportName("UB_IBI_AuthTypeLetter");	
		}
		
		crystalFatom.setF_IN_DoPrint(false);
		crystalFatom.setF_IN_DoSpool(true);
		crystalFatom.setF_IN_Report_ID("IBIReport");
		crystalFatom.setF_IN_Report_Spool_Format("pdf");
		crystalFatom.setF_IN_Report_Ref(getF_IN_HOSTCUSTID());
		crystalFatom.process(env);
		
		
	}

}
