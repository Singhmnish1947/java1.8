/* ********************************************************************************
 *  Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 *
 */
package com.trapedza.bankfusion.fatoms;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.common.MISTransactionCodeDetails;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOCurrency;
import com.trapedza.bankfusion.bo.refimpl.IBOMisTransactionCodes;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractBPW_NarrativeExtraction;
import com.trapedza.bankfusion.steps.refimpl.IBPW_NarrativeExtraction;

public class BPW_NarrativeExtraction extends AbstractBPW_NarrativeExtraction implements IBPW_NarrativeExtraction {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private transient final static Log logger = LogFactory.getLog(BPW_NarrativeExtraction.class.getName());
    //private BankFusionEnvironment environment;
    //private String narrDelimeter = CommonConstants.EMPTY_STRING;

    public BPW_NarrativeExtraction(BankFusionEnvironment env) {
        super(env);

    }

    public void process(BankFusionEnvironment env) {
        String mainNarrative = new String(CommonConstants.EMPTY_STRING);
        String mainSettlements = new String(CommonConstants.EMPTY_STRING);
        String commZone = new String(CommonConstants.EMPTY_STRING);
        String TransactionCode = getF_IN_TrxCode();
        Long chequeNumber = getF_IN_chequeNumber(); 
        String chequeStr = null;
        if(chequeNumber==null || chequeNumber.intValue()==0){
        	chequeStr="";
        }else{
        	chequeStr = chequeNumber.toString();
        }
        String dArea = getF_IN_Data_Area();
        int nLines = getF_IN_NarrLine().intValue();
        int sLines = getF_IN_SettleLines().intValue();
        int commAmntFlag = getF_IN_CommAmntFlag().intValue();
        int narrSize = 0, settleSize = 0;
        int[] lenNarr = new int[3];
//		int[] lenSettle = new int[3];
       // int totalTranNarrLength = 0;
        /* Transaction Narrative is Mandatory for Posting Engine */
        List params = new ArrayList();
        params.add(TransactionCode);
        //String narrative = CommonConstants.EMPTY_STRING;
        String narration = CommonConstants.EMPTY_STRING;
        // Using the Cache of TransactionScreenControl Table for fetching the details.
        MISTransactionCodeDetails mistransDetails;
        IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory.getInstance()
                .getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
        mistransDetails = ((IBusinessInformation) ubInformationService.getBizInfo()).getMisTransactionCodeDetails(TransactionCode);

        IBOMisTransactionCodes transNarrative = mistransDetails.getMisTransactionCodes();
        HashMap par = new HashMap();
		HashMap mfOutputValues = MFExecuter.executeMF("UB_BPW_GetNarrativeLengthConfig_SRV", env, par);
        lenNarr[0] =  ((Integer)mfOutputValues.get("LEN_NARRLINE1")).intValue();
        lenNarr[1] = ((Integer)mfOutputValues.get("LEN_NARRLINE2")).intValue();
        lenNarr[2] = ((Integer)mfOutputValues.get("LEN_NARRLINE3")).intValue();
//        lenSettle[0] = ((Integer)mfOutputValues.get("LEN_SETTLELINE1")).intValue();
//        lenSettle[1] = ((Integer)mfOutputValues.get("LEN_SETTLELINE2")).intValue();
//        lenSettle[2] = ((Integer)mfOutputValues.get("LEN_SETTLELINE3")).intValue();
       // totalTranNarrLength = lenNarr[0] + lenNarr[1] + lenNarr[2];
//        StringBuffer narr = new StringBuffer();
//        StringBuffer settle = new StringBuffer();

        if (nLines == 0)
            setF_OUT_Main_Narr(transNarrative.getF_DESCRIPTION());

        /* Extracting Narratives if NarrativeLine is greater than Zero */
        if (nLines > 0) {
        	
            narrSize = nLines * 25;
            int dAreaLength = dArea.length();

            if (nLines > 0 || commAmntFlag > 0) {
                if (dAreaLength > narrSize) {
                    mainNarrative = dArea.substring(0, (narrSize));
                }
                else {
                    mainNarrative = dArea;
                }
            }
            else mainNarrative = dArea;

            int nLinesLength = 25;
    		int startIndex =0;
    		int lastIndex = 0;
    		String finalNarr = "";
    		//fetchBPW_ModuleConfig_Delimeter();
            for (int i = 1; i <= nLines; i++) {
                if (dArea.length() <= nLinesLength) {
                    finalNarr = finalNarr.concat(dArea.substring(startIndex, dArea.length()));
                    finalNarr = new String(rightPad(finalNarr, nLinesLength));
                }
                else if (lenNarr[i - 1] > 25) {
                    finalNarr = finalNarr.concat(dArea.substring(startIndex, startIndex + nLinesLength));
                }
                else {
                    if (i == 1) {
                        if (mistransDetails.getTransactionScreenControl().isF_APPENDCHEQUENUMBER()) {
                            finalNarr = finalNarr.concat(dArea.substring(startIndex, startIndex + nLinesLength));                            
                            for (int j = startIndex + nLinesLength - 1; finalNarr.charAt(j) == ' '; j--) {
                                lastIndex = j;
                            }// If the first narrative is without empty spaces the lastIndex will be 25 (nLinesLength)
                            if(lastIndex==0){
                            	lastIndex = nLinesLength;	
                            }
                            // Trimming the Narrative one to accommodate chequeNumber.							
							int lengthOfSpace = 1;
							if(lastIndex+chequeStr.length()+lengthOfSpace>nLinesLength){
								finalNarr = finalNarr.substring(startIndex, lastIndex-(lengthOfSpace+chequeStr.length()));
							}else{
								finalNarr = finalNarr.substring(startIndex, lastIndex);
							}
                            String space = " ";
							//finalNarr +=  space.concat(chequeStr);
							finalNarr = finalNarr.concat(space.concat(chequeStr));
                            finalNarr = finalNarr.concat(rightPad(" ", startIndex + nLinesLength - lastIndex - lengthOfSpace - chequeStr.length()));
                        }
                        else {
                            finalNarr = finalNarr.concat(dArea.substring(startIndex, startIndex + lenNarr[i - 1]));
                        }
                        // finalNarr = finalNarr.concat(rightPad(" ", startIndex + nLinesLength -
                        // lastIndex - 4));
                    }
                    else {

                        finalNarr = finalNarr.concat(dArea.substring(startIndex, startIndex + lenNarr[i - 1]));
                    }
                }
                /*
                 * if(i <= 3){ narr.append(narrDelimeter); }
                 */
                nLinesLength = nLinesLength + 25;
                startIndex = startIndex + 25;
            }
			if(nLines ==1){
			    finalNarr = finalNarr.concat(rightPad("",(lenNarr[0]+lenNarr[1]-1)));
				//narr.append(narrDelimeter);
			}
			narration= rightPad(finalNarr,(lenNarr[0]+lenNarr[1]+lenNarr[2]));
			narration = narration.substring(0, (lenNarr[0]+lenNarr[1]+lenNarr[2]));

            narrSize = mainNarrative.length();
            setF_OUT_Main_Narr(narration);

        }
        /* Extracting Settlement Instructions if settlement Line is greater Than Zero */
        if (sLines > 0) {
            settleSize = sLines * 35;

            if ((sLines > 0) && (commAmntFlag > 0))
                mainSettlements = dArea.substring(narrSize, (narrSize + settleSize));

            else if ((sLines > 0) && (commAmntFlag == 0))
                mainSettlements = dArea.substring(narrSize, dArea.length());

            else if ((sLines == 0) && (commAmntFlag > 0))
                mainSettlements = dArea.substring(0, (settleSize - 1));

            else mainSettlements = dArea;

            settleSize = mainSettlements.length();
            setF_OUT_Settlement_Instructions(mainSettlements);
            
//            int sLinesLength = (nLines*25) + 35;
//			int startIndex = (nLines*25);
//			for (int i = 1; i <= sLines; i++) {
//				if (dArea.length() < sLinesLength) {
//					if((dArea.length()-startIndex) > lenSettle[i - 1]){
//						settle = settle.append(dArea.substring(startIndex, startIndex + lenSettle[i - 1]));
//					}
//					else{
//						settle = settle.append(rightPad((dArea.substring(startIndex, dArea.length())),(lenSettle[i - 1])));
//					}
//					
//				} else if (lenSettle[i - 1] > 35) {
//					settle = settle.append(dArea.substring(startIndex, startIndex + sLinesLength));
//				} else {
//					settle = settle.append(dArea.substring(startIndex, startIndex + lenSettle[i - 1]));
//				}
//				sLinesLength = sLinesLength + 35;
//				startIndex = startIndex + 35;
//			}
//			
//			narration= narration + settle.toString();
//			if(narration.length()< totalTranNarrLength)
//			{
//				narration = rightPad(narration,totalTranNarrLength);
//			}
//			setF_OUT_Main_Narr(narration);

        }
        /* Extracting Commonission code and Ammount if commission Amount Flag is greater than Zero */
        if (commAmntFlag > 0) {
            String comCode1;
            BigInteger tempComAmnt1, tempComAmnt2;
            BigDecimal comAmnt1, comAmnt2, comAmnt3, comAmnt4, comAmnt5, comAmnt6;
            int decScale;

            commZone = dArea.substring((narrSize + settleSize), dArea.length());
            commZone = commZone.trim();

            /* Extracting decimal scale from Currecny table for the defined currency */
            IBOCurrency currency = (IBOCurrency) env.getFactory().findByPrimaryKey(IBOCurrency.BONAME, commZone.substring(0, 3));
            decScale = currency.getF_CURRENCYSCALE();
            logger.info("decScale" + decScale);

            comCode1 = commZone.substring(3, 5);
            setF_OUT_ComCode1(comCode1);

            tempComAmnt1 = new BigInteger(commZone.substring(5, 23));
            comAmnt1 = new BigDecimal(tempComAmnt1, decScale);

            setF_OUT_ComAmnt1(comAmnt1.setScale(decScale));
            logger.info("ComCode1" + comCode1);
            logger.info("ComAmnt1" + comAmnt1);
            /* COMMCODE2 */
            try {
                String comCode2;

                setF_OUT_ComAmnt1(comAmnt1.setScale(decScale));

                comCode2 = commZone.substring(23, 25);

                tempComAmnt2 = new BigInteger(commZone.substring(25, 43));
                comAmnt2 = new BigDecimal(tempComAmnt2, decScale);

                logger.info("ComCode2" + comCode2);
                logger.info("ComAmnt2" + comAmnt2);

                setF_OUT_ComCode2(comCode2);
                setF_OUT_ComAmnt2(comAmnt2.setScale(decScale));

            }
            catch (Exception e) {
                logger.error("Error Occured for comCode2", e);
                return;
            }
            /* COMMCODE3 */
            try {
                String comCode3;

                comCode3 = commZone.substring(43, 45);

                tempComAmnt2 = new BigInteger(commZone.substring(45, 63));
                comAmnt3 = new BigDecimal(tempComAmnt2, decScale);

                logger.info("ComCode3" + comCode3);
                logger.info("ComAmnt3" + comAmnt3);

                setF_OUT_ComCode3(comCode3);
                setF_OUT_ComAmnt3(comAmnt3.setScale(decScale));

            }
            catch (Exception e) {
                logger.error("Error Occured for comCode3", e);
                return;
            }
            /* COMMCODE4 */
            try {
                String comCode4;

                comCode4 = commZone.substring(63, 65);

                tempComAmnt2 = new BigInteger(commZone.substring(65, 83));
                comAmnt4 = new BigDecimal(tempComAmnt2, decScale);

                logger.info("ComCode2" + comCode4);
                logger.info("ComAmnt2" + comAmnt4);

                setF_OUT_ComCode4(comCode4);
                setF_OUT_ComAmnt4(comAmnt4.setScale(decScale));

            }
            catch (Exception e) {
                logger.error("Error Occured for comCode2", e);
                return;
            }
            /* COMMCODE5 */
            try {
                String comCode5;

                comCode5 = commZone.substring(83, 85);

                tempComAmnt2 = new BigInteger(commZone.substring(85, 103));
                comAmnt5 = new BigDecimal(tempComAmnt2, decScale);

                logger.info("ComCode5" + comCode5);
                logger.info("ComAmnt5" + comAmnt5);

                setF_OUT_ComCode5(comCode5);
                setF_OUT_ComAmnt5(comAmnt5.setScale(decScale));

            }
            catch (Exception e) {
                logger.error("Error Occured for comCode5", e);
                return;
            }
            /* COMMCODE6 */
            try {
                String comCode6;

                comCode6 = commZone.substring(103, 105);

                tempComAmnt2 = new BigInteger(commZone.substring(105, 123));
                comAmnt6 = new BigDecimal(tempComAmnt2, decScale);

                logger.info("ComCode6" + comCode6);
                logger.info("ComAmnt6" + comAmnt6);

                setF_OUT_ComCode6(comCode6);
                setF_OUT_ComAmnt6(comAmnt6.setScale(decScale));

            }
            catch (Exception e) {
                logger.error("Error Occured for comCode6", e);
                
                return;
            }

        }

    }
    public static String rightPad(String s, int width) {
    	if(width<=0)
    	{
   		return s;
    	}
        return String.format("%-" + width + "s", s).replace(' ', ' ');

    }
   /* public void fetchBPW_ModuleConfig_Delimeter() {
        ModuleConfigurationFatom mconf = new ModuleConfigurationFatom(environment);
        mconf.setF_IN_ModuleName("BPW");
        mconf.setF_IN_OpMode("FETCH");
        mconf.setF_IN_ParamName("NARRLINEDELIMETER");
        mconf.process(environment);
        narrDelimeter = mconf.getF_OUT_ValueString();
    }*/
}
