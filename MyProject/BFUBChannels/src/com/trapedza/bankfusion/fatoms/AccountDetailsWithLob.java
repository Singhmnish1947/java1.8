package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;

import com.misys.ub.cc.types.SearchAccountInterfacesRs;

public class AccountDetailsWithLob {
	SearchAccountInterfacesRs accountInterfacesRs;
	ArrayList<String> lobList;

	public SearchAccountInterfacesRs getAccountInterfacesRs() {
		return accountInterfacesRs;
	}

	public void setAccountInterfacesRs(SearchAccountInterfacesRs accountInterfacesRs) {
		this.accountInterfacesRs = accountInterfacesRs;
	}

	public ArrayList<String> getLobList() {
		return lobList;
	}

	public void setLobList(ArrayList<String> lobList) {
		this.lobList = lobList;
	}
}
