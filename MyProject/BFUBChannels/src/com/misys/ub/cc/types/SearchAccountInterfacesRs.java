package com.misys.ub.cc.types;

import java.util.ArrayList;

import com.misys.ub.utils.types.LineOfBusinessListRs;


public class SearchAccountInterfacesRs{
	/**
	 * 
	 */
	private ArrayList<AccountDetailsOverview> accountDetailsOverviewList;
	
	public ArrayList<AccountDetailsOverview> getAccountDetailsOverviewList() {
		return accountDetailsOverviewList;
	}

	public void setAccountDetailsOverviewList(ArrayList<AccountDetailsOverview> accountDetailsOverviewList) {
		this.accountDetailsOverviewList = accountDetailsOverviewList;
	}

}
