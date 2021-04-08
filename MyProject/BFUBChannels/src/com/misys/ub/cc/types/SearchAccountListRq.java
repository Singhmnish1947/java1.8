package com.misys.ub.cc.types;

import java.util.List;

public class SearchAccountListRq {

	/**
	 * 
	 */
	private List<String> accountIdList;
	
	public void setAccountIdList(List<String> accountIdList) {
		this.accountIdList = accountIdList;
	}


	public List<String> getAccountIdList() {
		return accountIdList;
	}
	
	
}
