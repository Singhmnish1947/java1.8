package com.misys.ub.atm.batch.model;


public class Issuer {
    String client_id_type;
    String client_id_value;
    String inst_id;
    String network_id;
    
    public String getClient_id_type() {
        return client_id_type;
    }
    
    public void setClient_id_type(String client_id_type) {
        this.client_id_type = client_id_type;
    }
    
    public String getClient_id_value() {
        return client_id_value;
    }
    
    public void setClient_id_value(String client_id_value) {
        this.client_id_value = client_id_value;
    }
    
    public String getInst_id() {
        return inst_id;
    }
    
    public void setInst_id(String inst_id) {
        this.inst_id = inst_id;
    }
    
    public String getNetwork_id() {
        return network_id;
    }
    
    public void setNetwork_id(String network_id) {
        this.network_id = network_id;
    }
    
    
}
