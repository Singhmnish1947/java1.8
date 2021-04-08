package com.misys.ub.atm.batch.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "clearing" , namespace = "http://bpc.ru/sv/SVXP/clearing")
public class Clearing {
    List<Operation> operations;

    @XmlElement(name = "operation")
    public List<Operation> getOperations() {
        return operations;
    }

    
    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }
    
    
}
