package com.finastra.fbe.atm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.finastra.fbe.atm.batch.ChargeCollection;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.ub.atm.ExternalLoroIndicator;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOATMTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractaddBlockChargeAmount;

import bf.com.misys.ub.types.atm.CardIssuerData;
import bf.com.misys.ub.types.atm.UB_ATM_Financial_Details;
import bf.com.misys.ub.types.atm.postingvector.UB_Atm_PostingMessage;
import bf.com.misys.ub.types.atm.postingvector.UB_Atm_PostingMessages;
import bf.com.misys.ub.types.iso8583.UB_Financial_Details;

public class AddBlockChargeAmount extends AbstractaddBlockChargeAmount {

    private IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory.getInstance()
        .getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);

    IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
    private ChargeCollection chargeCollection = new ChargeCollection();
    public AddBlockChargeAmount() {
        super();
        // TODO Auto-generated constructor stub
    }

    public AddBlockChargeAmount(BankFusionEnvironment env) {
        super(env);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void process(BankFusionEnvironment env) throws BankFusionException {
        // TODO Auto-generated method stub
        super.process(env);
        BigDecimal blkAmount = getF_IN_blockedAmount();
        if (Boolean.TRUE == getModuleConfig("ATM", "APPLY_ISSUER_CHARGES_POS_BLOCKING")) {
            String accountId = getF_IN_accountId();
            String currencyCode = getF_IN_currencyCode();
            String contraAccId = getF_IN_contraAccountId();
            BigDecimal txnAmount = getF_IN_txnAmount();
            String accIDC = getF_IN_accInstId();
            String cardIssuerId = getF_IN_cardIssuerFIID();
            String terminalNumber = getF_IN_posFinDtls().getFinancialDetails().getCardAcceptorTerminalId();
            String txnCode = getTxnCode(accIDC, cardIssuerId,"ATMDualAccPOS","ATMDualAccPOS");
            String indicator = getIndicator(accIDC, cardIssuerId);
            UB_Atm_PostingMessage atmPostingMessage = chargeCollection.getCharges(accountId, txnCode, indicator, terminalNumber, contraAccId, txnAmount, currencyCode,null);
            if(chargeCollection.getIsChargeWaivedBasedOnCounter()) {
                chargeCollection.updateChargeCounter(accountId, "I", txnCode, SystemInformationManager.getInstance().getBFBusinessDate());
            } else {
                blkAmount = addChargeAmtToBlkAmt(blkAmount, atmPostingMessage);
            }
        }
        setF_OUT_blockAmount(blkAmount);
        setF_OUT_isChargeWaived(chargeCollection.getIsChargeWaivedBasedOnCounter()?"Y":"N");
    }

    private BigDecimal addChargeAmtToBlkAmt(BigDecimal blkAmount, UB_Atm_PostingMessage atmPostingMessage) {
        if(atmPostingMessage!=null) {                    
            UB_Atm_PostingMessages[]  atmPostingMsgs = atmPostingMessage.getUB_Atm_PostingMessages();
            for(UB_Atm_PostingMessages ubatmPostMsg : atmPostingMsgs) {
                if(validateatmChargePost(ubatmPostMsg)) {
                    blkAmount = blkAmount.add(ubatmPostMsg.getAMOUNT());
                }
            }
        }
        return blkAmount;
    }
    
    private boolean validateatmChargePost(UB_Atm_PostingMessages ubatmPostMsg) {
        // TODO Auto-generated method stub
        
        if(ubatmPostMsg==null || ubatmPostMsg.getSIGN().equals("-") || ubatmPostMsg.getAMOUNT().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }        
        return true;
    }
    
    

    private String getTxnCode(String accIDC, String cardIssAuthData,String configModule,String configKey) {
        String indicator = getIndicator(accIDC, cardIssAuthData);
        String atmTxnCode = (String) ubInformationService.getBizInfo().getModuleConfigurationValue(configModule, configKey,
            BankFusionThreadLocal.getBankFusionEnvironment());
        List<IBOATMTransactionCodes> ibotransactionCodes = getAtmTxnCodeDtls(indicator, atmTxnCode);
        return ibotransactionCodes.get(0).getF_MISTRANSACTIONCODE();

    }

    public String getIndicator(String accIDC, String cardIssAuthData) {
        HashMap paramsForIndicator = new HashMap();
        HashMap result = null;
        UB_ATM_Financial_Details finDetails = new UB_ATM_Financial_Details();
        UB_Financial_Details acceptor = new UB_Financial_Details();
        CardIssuerData issuer = new CardIssuerData();
        acceptor.setAcquiringInstitutionId(accIDC);
        issuer.setCardIssuerFIID(cardIssAuthData);
        finDetails.setCardIssuerData(issuer);
        finDetails.setFinancialDetails(acceptor);
        result = new ExternalLoroIndicator().getLoroIndicator(finDetails);
        return (String) result.get("atmTxnType");
    }

    private List<IBOATMTransactionCodes> getAtmTxnCodeDtls(String indicator, String atmTxnCode) {
        ArrayList<String> param = new ArrayList<>();
        param.add(atmTxnCode);
        param.add(indicator);
        List<IBOATMTransactionCodes> ibotransactionCodes = null;
        ibotransactionCodes = (List<IBOATMTransactionCodes>) factory.findByQuery(IBOATMTransactionCodes.BONAME,
            " where " + IBOATMTransactionCodes.ATMTRANSACTIONCODE + " = ? and " + IBOATMTransactionCodes.UBATMTRANSACTIONTYPE + " = ? ",
            param, null);
        return ibotransactionCodes;
    }
    
    private Boolean getModuleConfig(String cModuleName, String cParam) {
        return (Boolean) ubInformationService.getBizInfo().getModuleConfigurationValue(cModuleName, cParam,
            BankFusionThreadLocal.getBankFusionEnvironment());
    }
}
