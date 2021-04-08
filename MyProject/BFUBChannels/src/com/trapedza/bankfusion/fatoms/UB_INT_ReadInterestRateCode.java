package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.misys.bankfusion.subsystem.infrastructure.common.impl.CurrencyValue;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_INT_ReadInterestRateCode;

import bf.com.misys.cbs.msgs.v1r0.InterestBaseCodeDtlsRs;
import bf.com.misys.cbs.types.InterestBaseCodeDetails;
import bf.com.misys.cbs.types.TieredInterestRateDetails;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;

public class UB_INT_ReadInterestRateCode extends AbstractUB_INT_ReadInterestRateCode {

    public UB_INT_ReadInterestRateCode(BankFusionEnvironment env) {
        super(env);

    }

    public void process(BankFusionEnvironment env) {

        VectorTable interestBaseCodeDtls = getF_IN_InterestBaseCodeDtls();
        VectorTable tieredInterestRateDtls = getF_IN_tieredInterestRateDtls();
        InterestBaseCodeDetails interestBaseCodeDetails = new InterestBaseCodeDetails();
        List<TieredInterestRateDetails> TieredInterestRateDtlsList = new ArrayList<TieredInterestRateDetails>();
        InterestBaseCodeDtlsRs interestBaseCodeDtlsRs = new InterestBaseCodeDtlsRs();
        RsHeader rsheader = new RsHeader();
        MessageStatus status = new MessageStatus();
        rsheader.setStatus(status);

        HashMap interestBaseCodeDtlsMap = new HashMap();
        HashMap tieredInterestRateDtlsMap = new HashMap();
        for (int i = 0; i < tieredInterestRateDtls.size(); i++) {
            tieredInterestRateDtlsMap = tieredInterestRateDtls.getRowTags(i);
            TieredInterestRateDetails tieredInterestRateDtlsList = new TieredInterestRateDetails();
            tieredInterestRateDtlsList.setInterestRate((BigDecimal) tieredInterestRateDtlsMap.get("INTRATE"));
            CurrencyValue value = (CurrencyValue) tieredInterestRateDtlsMap.get("BALANCE");
            tieredInterestRateDtlsList.setBalance(value.roundCurrency());
            tieredInterestRateDtlsList.setTieredInterestRateID((String) tieredInterestRateDtlsMap.get("TIEREDINTERESTRATEID"));
            tieredInterestRateDtlsList.setIsoCurrencyCode((String) tieredInterestRateDtlsMap.get("ISOCURRENCYCODE"));
            tieredInterestRateDtlsList.setBaseCode((String) tieredInterestRateDtlsMap.get("BASECODE"));
            TieredInterestRateDtlsList.add(tieredInterestRateDtlsList);

        }
        for (int i = 0; i < interestBaseCodeDtls.size(); i++) {
            interestBaseCodeDtlsMap = interestBaseCodeDtls.getRowTags(i);

            interestBaseCodeDetails.setInterestRate((BigDecimal) interestBaseCodeDtlsMap.get("INTRATE"));
            interestBaseCodeDetails.setIsoCurrencyCode((String) interestBaseCodeDtlsMap.get("ISOCURRENCYCODE"));
            interestBaseCodeDetails.setBaseCode((String) interestBaseCodeDtlsMap.get("BASECODE"));
            interestBaseCodeDetails.setDescription((String) interestBaseCodeDtlsMap.get("DESCRIPTION"));
            interestBaseCodeDetails.setChangeDate((Date) interestBaseCodeDtlsMap.get("CHANGEDATE"));
            interestBaseCodeDetails.setCalcMethodType((Integer) interestBaseCodeDtlsMap.get("CALCMETHODTYPE"));
            interestBaseCodeDetails.setBaseYearDays((Integer) interestBaseCodeDtlsMap.get("BASEYEARDAYS"));
            interestBaseCodeDetails.setEffectiveDate((Timestamp) interestBaseCodeDtlsMap.get("EFFECTIVEDATE"));
            interestBaseCodeDetails.setInterestType((String) interestBaseCodeDtlsMap.get("UBTYPE"));
            interestBaseCodeDetails.setLastApprovedBy((String) interestBaseCodeDtlsMap.get("LASTAPPROVEDBY"));
            interestBaseCodeDetails.setLastModifiedBy((String) interestBaseCodeDtlsMap.get("LASTMODIFIEDBY"));
            interestBaseCodeDetails.setTiredInterest((Boolean) interestBaseCodeDtlsMap.get("TIEREDINTEREST"));

        }

        interestBaseCodeDtlsRs.setTieredInterestRateDetails(
                TieredInterestRateDtlsList.toArray(new TieredInterestRateDetails[TieredInterestRateDtlsList.size()]));
        interestBaseCodeDtlsRs.setRsHeader(rsheader);
        interestBaseCodeDtlsRs.setInterestBaseCodeDetails(interestBaseCodeDetails);
        setF_OUT_interestBaseCodeDtlsRs(interestBaseCodeDtlsRs);

    }

}
