package com.misys.ub.atm.batch.model;


public class PaymentOrder {
    String payment_order_id;
    String payment_order_status;
    
    public String getPayment_order_id() {
        return payment_order_id;
    }
    
    public void setPayment_order_id(String payment_order_id) {
        this.payment_order_id = payment_order_id;
    }
    
    public String getPayment_order_status() {
        return payment_order_status;
    }
    
    public void setPayment_order_status(String payment_order_status) {
        this.payment_order_status = payment_order_status;
    }
    

}
