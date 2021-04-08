import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trapedza.bankfusion.core.VectorTable;

public class TRY {

	public static void main(String[] args) {

		VectorTable transactionInput = new VectorTable();
		Map a = new HashMap();
		ArrayList b = new ArrayList();
		b.add("EUR");
		b.add("EUR");
		a.put("ACCTCURRENCYCODE", b);
		b.remove(0);

		b.add("11727a32fa6202By");
		b.add("11727a32fa6202Bx");
		a.put("MESSAGEID", b);/*
		b.clear();
		b.add("2020-05-26");
		b.add("2020-05-26");
		a.put("VALUEDATE", b);
		b.clear();
		b.add("ATM");
		b.add("ATM");
		a.put("CHANNELID", b);
		b.clear();
		b.add("false");
		b.add("false");
		a.put("CROSSCURRENCY", b);
		b.clear();
		b.add("2020-06-03");
		b.add("2020-06-03");
		a.put("TRANSACTIONDATE", b);
		b.clear();
		b.add("00000001");
		b.add("00000001");
		a.put("BRANCHSORTCODE", b);
		b.clear();
		b.add("C02");
		b.add("C02");
		a.put("TRANSACTIONCODE", b);
		b.clear();
		b.add("");
		b.add("");
		a.put("DRAWERNUMBER", b);
		b.clear();
		b.add("11.0");
		b.add("11.0");
		a.put("AMOUNTDEBIT", b);
		b.clear();
		b.add("false");
		b.add("false");
		a.put("FORCEPOST", b);
		b.clear();*/
		transactionInput.populateAllRows(a);
		System.out.println("transaction*common validationnInput");
	}

}
