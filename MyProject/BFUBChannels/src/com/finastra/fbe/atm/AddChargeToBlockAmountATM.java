package com.finastra.fbe.atm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.finastra.fbe.atm.batch.ChargeCollection;
import com.finastra.fbe.utils.UnclearedFundsUtils;
import com.misys.bankfusion.subsystem.infrastructure.common.impl.SystemInformationManager;
import com.misys.ub.atm.ExternalLoroIndicator;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.misys.ub.treasury.events.TreasuryEventCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOATMTransactionCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.FinderMethods;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.AvailableBalanceFunction;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractAddChargeToBlockAmountATM;
import com.trapedza.bankfusion.utils.BankFusionMessages;

import bf.com.misys.ub.types.atm.CardIssuerData;
import bf.com.misys.ub.types.atm.UB_ATM_Financial_Details;
import bf.com.misys.ub.types.atm.postingvector.UB_Atm_PostingMessage;
import bf.com.misys.ub.types.atm.postingvector.UB_Atm_PostingMessages;
import bf.com.misys.ub.types.iso8583.UB_Financial_Details;

/**
 * @author summahto
 *
 */
public class AddChargeToBlockAmountATM extends AbstractAddChargeToBlockAmountATM {
	
	private static final String AVAILABLE_BALANCE_WITH_UNCLEAREDFUNDS = "AvailableBalanceWithUnclearedFunds";
    private static final String AVAILABLEBALANCE = "AvailableBalance";
    private static final String MESSAGETYPE_DualATMCashWithdrawal = "DualATMCashWithdrawal";
    private static final String APPLY_ISSUER_CHARGES_ATM_BLOCKING = "APPLY_ISSUER_CHARGES_ATM_BLOCKING";
    private static final String ATM_CHANNEL = "ATM";
    public static final int E_INSUFFICIENT_AVAILABLE_BALANCE = 40507020;
    
	private transient final static Log logger = LogFactory.getLog(AddChargeToBlockAmountATM.class.getClass());
	private IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory.getInstance()
	        .getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);

	IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
	private ChargeCollection chargeCollection = new ChargeCollection();
	
	public AddChargeToBlockAmountATM(BankFusionEnvironment env) {
		
		super(env);
	}

	/**public AddChargeToBlockAmountATM() {
		super(env);
	}*/
	
	public AddChargeToBlockAmountATM() {
		
	}
	
	
	@Override
	public void process(BankFusionEnvironment env) throws BankFusionException {
		BigDecimal blkAmount = getF_IN_BlockedAmount();
		String accountId = getF_IN_AccountID();
		String channelID = getF_IN_ChannelID();
		String accIDC = getF_IN_AcquirerID();
        String cardIssuerId = getF_IN_CardIssuerFIID();
		String txnCode = getTxnCode(accIDC, cardIssuerId, MESSAGETYPE_DualATMCashWithdrawal, MESSAGETYPE_DualATMCashWithdrawal);
		
		if (channelID.equalsIgnoreCase(ATM_CHANNEL)) {
			
	        if (Boolean.TRUE == getModuleConfig(ATM_CHANNEL, APPLY_ISSUER_CHARGES_ATM_BLOCKING)) {
	            
	            String currencyCode = getF_IN_CurrencyCode();
	            String contraAccId = getF_IN_ContraAccountID();
	            BigDecimal txnAmount = getF_IN_TxnAmount();
	            String terminalNumber = getF_IN_TerminalID();
	            String indicator = getIndicator(accIDC, cardIssuerId);
	            UB_Atm_PostingMessage atmPostingMessage = chargeCollection.getCharges(accountId, txnCode, indicator, terminalNumber, contraAccId, txnAmount, currencyCode,null);
	            if(chargeCollection.getIsChargeWaivedBasedOnCounter()) {
	                chargeCollection.updateChargeCounter(accountId, "I", txnCode, SystemInformationManager.getInstance().getBFBusinessDate());
	            } else {
	                blkAmount = addChargeAmtToBlkAmt(blkAmount, atmPostingMessage);
	            }
	        }
	        
	        boolean isUncleredFUndsWIthdrawalAllowed = UnclearedFundsUtils.isUnclearedFundWithdrwalAllowed(BankFusionThreadLocal.getChannel(), txnCode);
        	String AVAILABLE_BALANCE_CONSTANT = AVAILABLEBALANCE;
        	if(isUncleredFUndsWIthdrawalAllowed) {
        		AVAILABLE_BALANCE_CONSTANT = AVAILABLE_BALANCE_WITH_UNCLEAREDFUNDS;
        	}
        	Map<Object, Object> resultMap;
        	IBOAttributeCollectionFeature accountBO = FinderMethods.getAccountBO(accountId);
        	String eMessage = null;
        	String eCode = null;
            if (accountBO != null) {
            	resultMap = AvailableBalanceFunction.run(accountId);
            	
            	if(((BigDecimal)resultMap.get(AVAILABLE_BALANCE_CONSTANT)).compareTo(blkAmount) < 0) {
            		eMessage = BankFusionMessages.getFormattedMessage(E_INSUFFICIENT_AVAILABLE_BALANCE, new String[] { accountId });
            	    setF_OUT_errorCode("40507020");
            	    setF_OUT_errorDescription(eMessage);
            	}
            }
               
	        setF_OUT_BlockAmount(blkAmount);
	        setF_OUT_isChargeWaived(chargeCollection.getIsChargeWaivedBasedOnCounter()?"Y":"N");
		}
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