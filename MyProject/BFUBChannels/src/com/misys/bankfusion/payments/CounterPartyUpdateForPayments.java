package com.misys.bankfusion.payments;

import com.trapedza.bankfusion.fatoms.UB_TXN_CounterPartyUpdate;

public class CounterPartyUpdateForPayments implements ICounterPartyUpdateForPayments{
	
	@Override
	public void setCounterPartyDetails(String transactionId, String direction, String paymentNetwork, String counterPartyName, String counterPartyAccount){
		UB_TXN_CounterPartyUpdate counterPartyUpdate = new UB_TXN_CounterPartyUpdate();
		counterPartyUpdate.setF_IN_TRANSACTIONID(transactionId);
		counterPartyUpdate.setF_IN_CHANNELNAME(paymentNetwork);
		counterPartyUpdate.setF_IN_CONTRAACCNUM(counterPartyAccount);
		counterPartyUpdate.setF_IN_TRANSACTIONDIRECTION(direction);
		counterPartyUpdate.setF_IN_CUSTOMERNAME(counterPartyName);
		counterPartyUpdate.populateTable();
		
	}

}
