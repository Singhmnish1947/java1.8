/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.types.narration.SwiftMT103NarrativeCodes;
import bf.com.misys.cbs.types.narration.SwiftMT202NarrativeCodes;

import com.misys.ub.swift.UB_MT103;
import com.misys.ub.swift.UB_MT202;
import com.misys.ub.swift.UB_SWT_DisposalObject;
import com.misys.ub.swift.UB_SWT_Util;
import com.trapedza.bankfusion.bo.refimpl.IBOCurrencyPositionsFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOSWTDisposal;
import com.trapedza.bankfusion.core.BankFusionObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_GetSwiftMessageDetails;
import com.trapedza.bankfusion.steps.refimpl.IUB_SWT_GetSwiftMessageDetails;

/**
 * @author Aklesh
 * @date May 06, 2011
 * @project Universal Banking
 * @Description: This Activity step is replica of UB_SWT_MessageGenerator that is used to get Swift
 *               Message details for before posting for posting narrative.
 * 
 */
public class UB_SWT_GetSwiftMessageDetails extends AbstractUB_SWT_GetSwiftMessageDetails implements IUB_SWT_GetSwiftMessageDetails {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private final static String VALUE_103 = "103";
    private final static String VALUE_202 = "202";
    private final static String VALUE_300 = "300";
    private final static String VALUE_320 = "320";
    private static final String fetchDisposalWhere ="WHERE " + IBOSWTDisposal.DEALNO + "=? AND "
    + IBOSWTDisposal.TRANSACTIONSTATUS+ " LIKE ? ORDER BY "+IBOSWTDisposal.VALUEDATE+ " DESC ";
    
    private transient final static Log logger = LogFactory.getLog(UB_SWT_GetSwiftMessageDetails.class.getName());

    /**
     * @param env
     */
    public UB_SWT_GetSwiftMessageDetails(BankFusionEnvironment env) {
        super(env);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_MessageGenerator#process(com.trapedza
     * .bankfusion.servercommon.commands.BankFusionEnvironment)
     */
    public void process(BankFusionEnvironment env) {
        UB_SWT_Util util = new UB_SWT_Util();
        /**
         * /* Message Status values and what it means -1 - Posting is pending 0 - Messgae to be
         * generated 1 - Partial Sent 2 - Completed 3 - Rejected by MMM 4 - Corrected Rejected
         * message 5 - Corrected message sent
         */
        IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
        IBOSWTDisposal disposalRecord =null;
        disposalRecord= (IBOSWTDisposal) factory.findByPrimaryKey(IBOSWTDisposal.BONAME, this.getF_IN_DisposalId(),
                true);
        
        if(disposalRecord!=null&&getF_IN_CodeWord()!=null && getF_IN_CodeWord().trim().length()>0){
        	disposalRecord=getDisposalRrecord(disposalRecord.getF_DEALNO(),this.getF_IN_CodeWord(),this.getF_IN_DisposalId());
        }
      
        
        if (disposalRecord == null) {
            setF_OUT_SwiftMT103NarrativeCodes(new SwiftMT103NarrativeCodes());
            setF_OUT_SwiftMT202NarrativeCodes(new SwiftMT202NarrativeCodes());
        }
        else {
            BankFusionObject disposalObject = new UB_SWT_DisposalObject();
            UB_SWT_MessageGenerator messageGenerator = new UB_SWT_MessageGenerator(env);
            disposalObject = messageGenerator.getDisposalObject(disposalRecord);
            if (disposalRecord.getF_CONFIRMATIONFLAG() != 9 || disposalRecord.getF_CANCELFLAG() != 9||disposalRecord.getF_PAYMENTFLAG()!=9) {
                Timestamp bankFusionSystemDate2 = SystemInformationManager.getInstance().getBFBusinessDateTime();
                if (disposalRecord.getF_MESSAGETYPE().equalsIgnoreCase(VALUE_103)) {
                    boolean generate103 = util.generateCategory2Message(((UB_SWT_DisposalObject) disposalObject).getValueDate(),
                            ((UB_SWT_DisposalObject) disposalObject).getPostDate(), env, ((UB_SWT_DisposalObject) disposalObject)
                                    .getContraAccCurrencyCode(), new java.sql.Date(bankFusionSystemDate2.getTime()), VALUE_103);

                    if (generate103) {
                        UB_SWT_MT103Populate populate103 = new UB_SWT_MT103Populate(env);
                        ((UB_SWT_DisposalObject) disposalObject).setConfirmationFlag(0);
                        populate103.setF_IN_DisposalObject(disposalObject);
                        populate103.setF_IN_BranchSortCode(BankFusionThreadLocal.getUserSession().getBranchSortCode());
                        populate103.process(env);
                        List data103 = populate103.getF_OUT_XMLTAGVALUEMAPLIST();
                        for (Object object : data103) {
                            UB_MT103 mt103 = (UB_MT103) object;
                            setF_OUT_SwiftMT103NarrativeCodes(UB_SWT_Util.generateMT103ComplexType(mt103));
                        }
                        if (populate103.isF_OUT_generateMT202Or292()) {
                            UB_SWT_MT202Populate populate202 = new UB_SWT_MT202Populate(env);
                            populate202.setF_IN_DisposalObject(disposalObject);
                            populate202.setF_IN_BranchSortCode(BankFusionThreadLocal.getUserSession().getBranchSortCode());
                            populate202.setF_IN_generateMT103(populate103.isF_OUT_GEN_MT103());
                            populate202.setF_IN_generateMT202(populate103.isF_OUT_GEN_MT202());
                            populate202.setF_IN_isOriginatedFromMT103_MT103Plus(true);
                            populate202.setF_IN_messageGenMatrix(populate103.getF_OUT_messageGenMatrix());
                            populate202.process(env);
                            List data202 = populate202.getF_OUT_xmlTagValueList();
                            for (Object object : data202) {
                                UB_MT202 mt202 = (UB_MT202) object;
                                setF_OUT_SwiftMT202NarrativeCodes(UB_SWT_Util.generateMT202ComplexType(mt202));
                            }
                        }
                    }
                }
                else if(disposalRecord.getF_MESSAGETYPE().equalsIgnoreCase(VALUE_300)){
                    UB_SWT_PopulateMT300 populate300 = new UB_SWT_PopulateMT300(env);
                    populate300.setF_IN_DisposalObject(disposalObject);
                    populate300.setF_IN_BranchSortCode(BankFusionThreadLocal.getUserSession().getBranchSortCode());
                    populate300.process(env);
                    if (populate300.isF_OUT_generateMT202()) {
                        setF_OUT_SwiftMT202NarrativeCodes(generate202(disposalObject));
                    }
                }else if(disposalRecord.getF_MESSAGETYPE().equalsIgnoreCase(VALUE_320)){
                	 UB_SWT_PopulateMT320 populate320 = new UB_SWT_PopulateMT320(env);
                     populate320.setF_IN_DisposalObject(disposalObject);
                     populate320.setF_IN_BranchSortCode(BankFusionThreadLocal.getUserSession().getBranchSortCode());
                     populate320.process(env);
                     if (populate320.isF_OUT_GenerateMT202()||disposalRecord.getF_PAYMENTFLAG()==1) {
                         setF_OUT_SwiftMT202NarrativeCodes(generate202(disposalObject));
                     }
                }
            }
            else {
                Timestamp bankFusionSystemDate = SystemInformationManager.getInstance().getBFBusinessDateTime();
                boolean generate202 = util.generateCategory2Message(((UB_SWT_DisposalObject) disposalObject).getValueDate(),
                        ((UB_SWT_DisposalObject) disposalObject).getPostDate(), env, ((UB_SWT_DisposalObject) disposalObject)
                                .getContraAccCurrencyCode(), new java.sql.Date(bankFusionSystemDate.getTime()), VALUE_202);

                if ((generate202) && (disposalRecord.getF_PAYMENTFLAG() == 0 || disposalRecord.getF_PAYMENTFLAG() == 2)) {
                    setF_OUT_SwiftMT202NarrativeCodes(generate202(disposalObject));
                }
            }
        }
    }

    private IBOSWTDisposal getDisposalRrecord(String DealNo, String codeWord,String DisposalId) {
    	IBOSWTDisposal disposalRecord =null;
    	ArrayList params = new ArrayList();
        params.add(DealNo);
        params.add("%"+codeWord+"%");
    	IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
    	 List disposalRecordList=  factory.findByQuery(IBOSWTDisposal.BONAME, fetchDisposalWhere, params, null, false);
    	if(disposalRecordList!=null&&!disposalRecordList.isEmpty()){
    		disposalRecord=(IBOSWTDisposal)disposalRecordList.get(0);
    	}else{
    			
    		disposalRecord= (IBOSWTDisposal) factory.findByPrimaryKey(IBOSWTDisposal.BONAME, DisposalId,
                    true);
    	}
    	
    	return disposalRecord;
		// TODO Auto-generated method stub
    	
    	
    		}

	/**
     * @param disposalObject
     * @return
     */
    private SwiftMT202NarrativeCodes generate202(BankFusionObject disposalObject) {
        SwiftMT202NarrativeCodes swiftMT202NarrativeCodes = new SwiftMT202NarrativeCodes();
        BankFusionEnvironment env = BankFusionThreadLocal.getBankFusionEnvironment();
        UB_SWT_MT202Populate populate202 = new UB_SWT_MT202Populate(env);
        populate202.setF_IN_DisposalObject(disposalObject);
        populate202.setF_IN_BranchSortCode(BankFusionThreadLocal.getUserSession().getBranchSortCode());
        populate202.setF_IN_generateMT202(true);
        populate202.process(env);
        List data202 = populate202.getF_OUT_xmlTagValueList();
        for (Object object : data202) {
            UB_MT202 mt202 = (UB_MT202) object;
            swiftMT202NarrativeCodes = UB_SWT_Util.generateMT202ComplexType(mt202);
        }
        return swiftMT202NarrativeCodes;
    }
}
