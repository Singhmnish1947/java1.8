/**
 * 
 */
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



import bf.com.misys.cbs.types.CollateralDetailsList;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.msgs.v1r0.CollateralDetailsRs;

import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_COL_CustCollateralDetails_SRV;


public class UB_COL_CustCollateralDetails_SRV extends AbstractUB_COL_CustCollateralDetails_SRV {

  

	public UB_COL_CustCollateralDetails_SRV(BankFusionEnvironment env) {
        super(env);

    }

    public void process(BankFusionEnvironment env) {
    	
        VectorTable listCollateralDetails = getF_IN_listCollateralDetails();
        String customerName = getF_IN_customerName();
        List<CollateralDetailsList> CollateralDetails = new ArrayList<CollateralDetailsList>();
        CollateralDetailsRs collateralDetailsrs = new CollateralDetailsRs();
        RsHeader rsheader = new RsHeader();
        MessageStatus status = new MessageStatus();
        rsheader.setStatus(status);
                                           
        HashMap collateralDetailsMap = new HashMap();
        for (int i = 0; i < listCollateralDetails.size(); i++) {
            collateralDetailsMap = listCollateralDetails.getRowTags(i);
            CollateralDetailsList collateralDetails = new CollateralDetailsList();
            
            collateralDetails.setAvailableCoverValue((BigDecimal) collateralDetailsMap.get("CUSTOMERCOLLATERAL_AVAILABLECOVERVALUE"));
            collateralDetails.setCollateralCode((String) collateralDetailsMap.get("CUSTOMERCOLLATERAL_COLLATERALCODE"));
            collateralDetails.setCustomerCode((String) collateralDetailsMap.get("CUSTOMERCOLLATERAL_CUSTOMERCODE"));
            collateralDetails.setCollateralDesc((String) collateralDetailsMap.get("COLLATERALTYPE_TYPEDESCRIPTION"));
            collateralDetails.setCollateralDtlId((String) collateralDetailsMap.get("CUSTOMERCOLLATERAL_COLLATERALDTLID"));
            collateralDetails.setCollateralType((String) collateralDetailsMap.get("COLLATERALTYPE_COLLATERALTYPE"));
            collateralDetails.setCoverValue((BigDecimal) collateralDetailsMap.get("CUSTOMERCOLLATERAL_COVERVALUE"));
            collateralDetails.setCustomerName(customerName);
            collateralDetails.setExpiryDate((Date) collateralDetailsMap.get("CUSTOMERCOLLATERAL_EXPIRYDATE"));
            collateralDetails.setFaceValue((BigDecimal) collateralDetailsMap.get("CUSTOMERCOLLATERAL_FACEVALUE"));
            collateralDetails.setValuationDate((Date) collateralDetailsMap.get("CUSTOMERCOLLATERAL_VALUATIONDATE"));
            collateralDetails.setIsoCurrencyCode((String) collateralDetailsMap.get("CUSTOMERCOLLATERAL_ISOCURRENCYCODE"));
            collateralDetails.setForcedSaleValue((BigDecimal) collateralDetailsMap.get("CUSTOMERCOLLATERAL_FORCEDSALEVALUE"));
            collateralDetails.setMarketValue((BigDecimal) collateralDetailsMap.get("CUSTOMERCOLLATERAL_MARKETVALUE"));
            collateralDetails.setAuthDate((Date) collateralDetailsMap.get("CUSTOMERCOLLATERAL_AUTHDATE"));
            collateralDetails.setAvailableCap((BigDecimal) collateralDetailsMap.get("CUSTOMERCOLLATERAL_AVAILABLECAP"));
            collateralDetails.setCap((BigDecimal) collateralDetailsMap.get("CUSTOMERCOLLATERAL_CAP"));
            collateralDetails.setCollateralSeq((Integer) collateralDetailsMap.get("CUSTOMERCOLLATERAL_COLLATERALSEQ"));
            collateralDetails.setCoverageCurrencyCd((String) collateralDetailsMap.get("CUSTOMERCOLLATERAL_COVERAGECURRENCYCD"));
            collateralDetails.setCreateDate((Date) collateralDetailsMap.get("CUSTOMERCOLLATERAL_CREATEDATE"));
            collateralDetails.setEffectiveDate((Date) collateralDetailsMap.get("CUSTOMERCOLLATERAL_EFFECTIVEDATE"));
            collateralDetails.setAllocateStatus((String) collateralDetailsMap.get("CUSTOMERCOLLATERAL_ALLOCATESTATUS"));
            collateralDetails.setPledgeAccountId((String) collateralDetailsMap.get("CUSTOMERCOLLATERAL_PLEDGEACCOUNTID"));
            collateralDetails.setRealisationCode((String) collateralDetailsMap.get("CUSTOMERCOLLATERAL_REALISATIONCODE"));
            collateralDetails.setLiquidationCost((BigDecimal) collateralDetailsMap.get("CUSTOMERCOLLATERAL_LIQUIDATIONCOST"));
            collateralDetails.setReviewDate((Date) collateralDetailsMap.get("CUSTOMERCOLLATERAL_REVIEWDATE"));
            collateralDetails.setFixedAmountAllocation((BigDecimal) collateralDetailsMap.get("CUSTOMERCOLLATERALLINK_FIXEDAMOUNTALLOCATION"));
            collateralDetails.setAccountCoverPercent((BigDecimal) collateralDetailsMap.get("CUSTOMERCOLLATERALLINK_ACCOUNTCOVERPERCENT"));
            collateralDetails.setCTLAllocatePercent((BigDecimal) collateralDetailsMap.get("CUSTOMERCOLLATERALLINK_CLTALLOCATEDPERCENT"));
            CollateralDetails.add(collateralDetails);
           
                                      
        }
        collateralDetailsrs.setCollateralDetailsList(CollateralDetails.toArray(new CollateralDetailsList[CollateralDetails.size()]));
        collateralDetailsrs.setRsHeader(rsheader);
        setF_OUT_collateralDetailsRs(collateralDetailsrs);

    }

  
}