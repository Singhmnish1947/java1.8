package com.misys.ub.payment.posting;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import com.misys.ub.datacenter.DataCenterCommonUtils;
import com.misys.ub.payment.swift.DBUtils.SwiftNonStpChargeTable;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.misys.ub.swift.SWT_Constants;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.fatoms.CB_TXN_GetChargesVector;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

import bf.com.misys.cbs.msgs.v1r0.ReadCustomerRs;
import bf.com.misys.cbs.types.Charges;
import bf.com.misys.cbs.types.header.Orig;
import bf.com.misys.cbs.types.header.RqHeader;

public class SWTPostingUtils {
    /**
     * @param txnCurrencyCode
     * @param branchCode
     * @return
     */
    public static String getSuspenseAccountFromModuleConfig(String txnCurrencyCode, String branchCode) {
        String suspenseAccount = CommonConstants.EMPTY_STRING;
        String moduleId = PaymentSwiftConstants.MODULE_ID;
        String pseudonymKey = PaymentSwiftConstants.PSEDONYM_KEY;
        String defaultBranchForPseudonym = PaymentSwiftConstants.PSEDONYM_DEFAULT_BRANCHID;
        String psuedonymName = DataCenterCommonUtils.readModuleConfiguration(moduleId, pseudonymKey);
        if (StringUtils.isBlank(branchCode)) {
            branchCode = DataCenterCommonUtils.readModuleConfiguration(moduleId, defaultBranchForPseudonym);
        }
        suspenseAccount = PaymentSwiftUtils.retrievePsuedonymAcctId(txnCurrencyCode, branchCode,
                PaymentSwiftConstants.PSEDONYM_CONTEXT, psuedonymName);
        return suspenseAccount;
    }

    /**
     * @param accountID
     * @return
     */
    public static String getAccountBranchSortCode(String accountID) {
        String accountBranch = CommonConstants.EMPTY_STRING;
        IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
        IBOAccount accountBO = (IBOAccount) factory.findByPrimaryKey(IBOAccount.BONAME, accountID, true);
        if (accountBO != null) {
            accountBranch = accountBO.getF_BRANCHSORTCODE();
        }
        return accountBranch;
    }

    /**
     * @param remittanceDtls
     * @param
     * @return
     */
    public static VectorTable getChargeVector(String messageId) {
        VectorTable chargeVector = new VectorTable();
        CB_TXN_GetChargesVector c = new CB_TXN_GetChargesVector();
        SwiftNonStpChargeTable swiftCharge = new SwiftNonStpChargeTable();
        VectorTable vectorList = swiftCharge.listChargeByMessageId(messageId);
        if (vectorList.size() != 0) {
            chargeVector = c.getChargeVector(vectorList, chargeVector);
        }
        return chargeVector;
    }

    /**
     * @param remittanceDtls
     * @return
     */
    public static VectorTable getChargeDetails(Charges[] txnCharges) {
        VectorTable chargeVector = new VectorTable();
        CB_TXN_GetChargesVector c = new CB_TXN_GetChargesVector();
        ArrayList<Charges> chargesList = new ArrayList<>();
        if (txnCharges != null) {
            for (int i = 0, n = txnCharges.length; i < n; i++) {
                if (null!=txnCharges[i].getCharge() && (!StringUtils.isEmpty(txnCharges[i].getCharge().getChargeCode())
                        || !StringUtils.isEmpty(txnCharges[i].getCharge().getTaxCode()))) {
                    chargesList.add(txnCharges[i]);
                }
            }
            chargeVector = c.getChargeDetailsInVector(chargesList.toArray((Charges[]) new Charges[chargesList.size()]),
                    chargeVector);
        }
        return chargeVector;
    }

    /**
     * return request header object
     * 
     * @return
     */
    public static RqHeader rqHeaderInput() {
        RqHeader rqHeader = new RqHeader(); // need to check with Machamma
        Orig orig = new Orig();
        orig.setOrigCtxtId(StringUtils.EMPTY);
        orig.setAppId(StringUtils.EMPTY);
        orig.setAppVer(StringUtils.EMPTY);
        orig.setChannelId("UXP");
        orig.setOfflineMode(false);
        orig.setOrigBranchCode(BankFusionThreadLocal.getUserSession().getBranchSortCode());
        orig.setOrigId(StringUtils.EMPTY);
        orig.setOrigLocale(StringUtils.EMPTY);
        orig.setZoneId(StringUtils.EMPTY);
        rqHeader.setOrig(orig);
        return rqHeader;
    }

    /**
     * @param accountId
     * @return
     */
    public static String getCustomerDetails(String customerId, String accountId) {
        StringBuilder sb = new StringBuilder();
        ReadCustomerRs custResponse = DataCenterCommonUtils.readCustomerDetails(customerId);
        sb.append("/").append(accountId);
        // customer not found
        if (null != custResponse && null != custResponse.getCustomerDetails()) {
            sb.append(SWT_Constants.delimiter);
            sb.append(!StringUtils.isBlank(custResponse.getCustomerDetails().getCustBasicDetails().getShortName())
                    ? custResponse.getCustomerDetails().getCustBasicDetails().getShortName()
                    : StringUtils.EMPTY);
            if (null != custResponse.getCustomerDetails().getCustBasicDetails().getAddress()) {
                sb.append(
                        !StringUtils.isBlank(custResponse.getCustomerDetails().getCustBasicDetails().getAddress().getAddressLine1())
                                ? SWT_Constants.delimiter.concat(
                                        custResponse.getCustomerDetails().getCustBasicDetails().getAddress().getAddressLine1())
                                : StringUtils.EMPTY);
                sb.append(
                        !StringUtils.isBlank(custResponse.getCustomerDetails().getCustBasicDetails().getAddress().getAddressLine2())
                                ? SWT_Constants.delimiter.concat(
                                        custResponse.getCustomerDetails().getCustBasicDetails().getAddress().getAddressLine2())
                                : StringUtils.EMPTY);
                sb.append(
                        !StringUtils.isBlank(custResponse.getCustomerDetails().getCustBasicDetails().getAddress().getAddressLine3())
                                ? SWT_Constants.delimiter.concat(
                                        custResponse.getCustomerDetails().getCustBasicDetails().getAddress().getAddressLine3())
                                : StringUtils.EMPTY);

            }
        }

        return sb.toString();

    }
	
	 /**
     * @param accountId
     * @param customerId
     * @return
     */
    public static String getCustomerDetailsForOptionJ(String customerId, String accountId) {
        StringBuilder sb = new StringBuilder();
        ReadCustomerRs custResponse = DataCenterCommonUtils.readCustomerDetails(customerId);
        if(!StringUtils.isBlank(accountId)) {
        	   sb.append(PaymentSwiftConstants.ACCT_CODE).append(accountId);
        }
        // customer not found
        if (null != custResponse && null != custResponse.getCustomerDetails()) {
            if(sb.length()>0) {
            	sb.append(SWT_Constants.delimiter);
            }
            if(!StringUtils.isBlank(custResponse.getCustomerDetails().getCustBasicDetails().getShortName())){
            	sb.append(PaymentSwiftConstants.NAME_CODE).append(custResponse.getCustomerDetails().getCustBasicDetails().getShortName());
            }
            if (null != custResponse.getCustomerDetails().getCustBasicDetails().getAddress()) {
              
                  if(!StringUtils.isBlank(custResponse.getCustomerDetails().getCustBasicDetails().getAddress().getAddressLine1())){
                      sb.append(SWT_Constants.delimiter).append(PaymentSwiftConstants.ADD1_CODE).append(custResponse.getCustomerDetails().getCustBasicDetails().getAddress().getAddressLine1().length()>40?
                      custResponse.getCustomerDetails().getCustBasicDetails().getAddress().getAddressLine1().substring(0, 40):custResponse.getCustomerDetails().getCustBasicDetails().getAddress().getAddressLine1());
                        }
                        if(!StringUtils.isBlank(custResponse.getCustomerDetails().getCustBasicDetails().getAddress().getAddressLine2())){
                       	 sb.append(SWT_Constants.delimiter).append(PaymentSwiftConstants.ADD2_CODE).append(custResponse.getCustomerDetails().getCustBasicDetails().getAddress().getAddressLine2().length()>40?
                                 custResponse.getCustomerDetails().getCustBasicDetails().getAddress().getAddressLine2().substring(0, 40):custResponse.getCustomerDetails().getCustBasicDetails().getAddress().getAddressLine2());
                       }
                               
                        if(!StringUtils.isBlank(custResponse.getCustomerDetails().getCustBasicDetails().getAddress().getAddressLine3())){
                          	 sb.append(SWT_Constants.delimiter).append(PaymentSwiftConstants.CITY_CODE).append(custResponse.getCustomerDetails().getCustBasicDetails().getAddress().getAddressLine3().length()>40?
                                     custResponse.getCustomerDetails().getCustBasicDetails().getAddress().getAddressLine3().substring(0, 40):custResponse.getCustomerDetails().getCustBasicDetails().getAddress().getAddressLine3());
                          }

            }
        }

        return sb.toString();

    }

}
