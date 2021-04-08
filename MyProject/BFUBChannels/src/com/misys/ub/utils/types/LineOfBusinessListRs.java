package com.misys.ub.utils.types;

import java.util.ArrayList;

public class LineOfBusinessListRs {
	String id;
	String customerId;
	ArrayList<String> lisOfBusinesses; 
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public ArrayList<String> getLisOfBusinesses() {
		return lisOfBusinesses;
	}
	public void setLisOfBusinesses(ArrayList<String> lisOfBusinesses) {
		this.lisOfBusinesses = lisOfBusinesses;
	}
	
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
}
