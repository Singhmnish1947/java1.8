package com.misys.ub.atm.batch.model;


public class Operation {
    String oper_type;
    String msg_type;
    String oper_date;
    String host_date;
    OperationAmount oper_amount;
    OperationRequestAmount oper_request_amount;
    OperationSurchargeAmount oper_surcharge_amount;
    OperationCashbackAmount oper_cashback_amount;
    SttlAmount sttl_amount;
    String originator_refnum;
    String response_code;
    String oper_reason;
    String status;
    String is_reversal;
    String merchant_number;
    String mcc;
    String merchant_name;
    String merchant_street;
    String merchant_city;
    String merchant_country;
    String terminal_type;
    String terminal_number;
    String sttl_date;
    String acq_sttl_date;
    PaymentOrder payment_order;
    Issuer issuer;
    Acquirer acquirer;    
    Auth_Data auth_data;
    Transaction transaction;
    
    public String getOper_type() {
        return oper_type;
    }
    
    public void setOper_type(String oper_type) {
        this.oper_type = oper_type;
    }
    
    public String getMsg_type() {
        return msg_type;
    }
    
    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }
    
    public String getOper_date() {
        return oper_date;
    }
    
    public void setOper_date(String oper_date) {
        this.oper_date = oper_date;
    }
    
    public String getHost_date() {
        return host_date;
    }
    
    public void setHost_date(String host_date) {
        this.host_date = host_date;
    }
    
    public OperationAmount getOper_amount() {
        return oper_amount;
    }
    
    public void setOper_amount(OperationAmount oper_amount) {
        this.oper_amount = oper_amount;
    }
    
    public OperationRequestAmount getOper_request_amount() {
        return oper_request_amount;
    }
    
    public void setOper_request_amount(OperationRequestAmount oper_request_amount) {
        this.oper_request_amount = oper_request_amount;
    }
    
    public OperationSurchargeAmount getOper_surcharge_amount() {
        return oper_surcharge_amount;
    }
    
    public void setOper_surcharge_amount(OperationSurchargeAmount oper_surcharge_amount) {
        this.oper_surcharge_amount = oper_surcharge_amount;
    }
    
    public OperationCashbackAmount getOper_cashback_amount() {
        return oper_cashback_amount;
    }
    
    public void setOper_cashback_amount(OperationCashbackAmount oper_cashback_amount) {
        this.oper_cashback_amount = oper_cashback_amount;
    }
    
    public String getOriginator_refnum() {
        return originator_refnum;
    }
    
    public void setOriginator_refnum(String originator_refnum) {
        this.originator_refnum = originator_refnum;
    }
    
    public String getResponse_code() {
        return response_code;
    }
    
    public void setResponse_code(String response_code) {
        this.response_code = response_code;
    }
    
    public String getOper_reason() {
        return oper_reason;
    }
    
    public void setOper_reason(String oper_reason) {
        this.oper_reason = oper_reason;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getIs_reversal() {
        return is_reversal;
    }
    
    public void setIs_reversal(String is_reversal) {
        this.is_reversal = is_reversal;
    }
    
    public String getMerchant_number() {
        return merchant_number;
    }
    
    public void setMerchant_number(String merchant_number) {
        this.merchant_number = merchant_number;
    }
    
    public String getMcc() {
        return mcc;
    }
    
    public void setMcc(String mcc) {
        this.mcc = mcc;
    }
    
    public String getMerchant_name() {
        return merchant_name;
    }
    
    public void setMerchant_name(String merchant_name) {
        this.merchant_name = merchant_name;
    }
    
    public String getMerchant_street() {
        return merchant_street;
    }
    
    public void setMerchant_street(String merchant_street) {
        this.merchant_street = merchant_street;
    }
    
    public String getMerchant_city() {
        return merchant_city;
    }
    
    public void setMerchant_city(String merchant_city) {
        this.merchant_city = merchant_city;
    }
    
    public String getMerchant_country() {
        return merchant_country;
    }
    
    public void setMerchant_country(String merchant_country) {
        this.merchant_country = merchant_country;
    }
    
    public String getTerminal_type() {
        return terminal_type;
    }
    
    public void setTerminal_type(String terminal_type) {
        this.terminal_type = terminal_type;
    }
    
    public String getTerminal_number() {
        return terminal_number;
    }
    
    public void setTerminal_number(String terminal_number) {
        this.terminal_number = terminal_number;
    }
    
    public String getSttl_date() {
        return sttl_date;
    }
    
    public void setSttl_date(String sttl_date) {
        this.sttl_date = sttl_date;
    }
    
    public String getAcq_sttl_date() {
        return acq_sttl_date;
    }
    
    public void setAcq_sttl_date(String acq_sttl_date) {
        this.acq_sttl_date = acq_sttl_date;
    }
    
    public PaymentOrder getPayment_order() {
        return payment_order;
    }
    
    public void setPayment_order(PaymentOrder payment_order) {
        this.payment_order = payment_order;
    }
    
    public Issuer getIssuer() {
        return issuer;
    }
    
    public void setIssuer(Issuer issuer) {
        this.issuer = issuer;
    }
    
    public Acquirer getAcquirer() {
        return acquirer;
    }
    
    public void setAcquirer(Acquirer acquirer) {
        this.acquirer = acquirer;
    }
    
    public Transaction getTransaction() {
        return transaction;
    }
    
    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    
    public SttlAmount getSttl_amount() {
        return sttl_amount;
    }

    
    public void setSttl_amount(SttlAmount sttl_amount) {
        this.sttl_amount = sttl_amount;
    }

    
    public Auth_Data getAuth_data() {
        return auth_data;
    }

    
    public void setAuth_data(Auth_Data auth_data) {
        this.auth_data = auth_data;
    }
    
}
