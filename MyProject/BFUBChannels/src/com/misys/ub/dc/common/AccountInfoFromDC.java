package com.misys.ub.dc.common;

/**
 * @author susikdar
 * 
 *  <code>AccountInfo</code> will hold the data which will pass to OpenAccount API
 *  to open the account based on different account type
 */
public class AccountInfoFromDC {
    
    private String customerId;
    private String subProdCurrency;
    private String accountBranchCode;
    private String subProduct;
    private String messageId;
    private String accountType;
    
    public String getCustomerId() {
        return customerId;
    }
    
    public AccountInfoFromDC setCustomerId(String customerId) {
        this.customerId = customerId;
        return this;
    }
    
    public String getSubProdCurrency() {
        return subProdCurrency;
    }
    
    public AccountInfoFromDC setSubProdCurrency(String subProdCurrency) {
        this.subProdCurrency = subProdCurrency;
        return this;
    }
    
    public String getAccountBranchCode() {
        return accountBranchCode;
    }
    
    public AccountInfoFromDC setAccountBranchCode(String accountBranchCode) {
        this.accountBranchCode = accountBranchCode;
        return this;
    }
    
    public String getSubProduct() {
        return subProduct;
    }
    
    public AccountInfoFromDC setSubProduct(String subProduct) {
        this.subProduct = subProduct;
        return this;
    }
    
    public String getMessageId() {
        return messageId;
    }
    
    public AccountInfoFromDC setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }
    
    public String getAccountType() {
        return accountType;
    }
    
    public AccountInfoFromDC setAccountType(String accountType) {
        this.accountType = accountType;
        return this;
    }
}