package com.misys.ub.fatoms.batch.CardTechBalanceDownload;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMHelper;
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.process.AbstractBatchProcess;
import com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator;
import com.trapedza.bankfusion.bo.refimpl.IBOATMCardAccountMap;
import com.trapedza.bankfusion.bo.refimpl.IBOAccountDebitCardView;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOCurrency;
import com.trapedza.bankfusion.bo.refimpl.IBODormancyFeature;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.utils.Utils;
public class CardTechBalanceDownloadProcess extends AbstractBatchProcess {
	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}
	private static IBusinessInformation bizInfo = null;
    static {
        IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory.getInstance()
                .getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
        bizInfo = ubInformationService.getBizInfo();
    }
	private transient final static Log logger = LogFactory.getLog(CardTechBalanceDownloadProcess.class.getName());


	private static final String CardTechAtmCardNumbers = "WHERE " + IBOATMCardAccountMap.ACCOUNTID + " = ?";
	private static final String NumericCurrCodes = "WHERE " + IBOCurrency.ISOCURRENCYCODE + " = ?";

	private AbstractProcessAccumulator accumulator;
	private IBODormancyFeature currentDormancyAccountItem = null;
	private IBOAttributeCollectionFeature currentAccountItem = null;
	private String runtimeMFID;
	private  String ISOCurrencyCodes="";

	private   String BalType=null;
	private  String BalanceType=null;
	private   String ATMCardNumbers=null;
	private  Iterator ATMCardMapAccountsResultSet = null;
	private static final int RecordCount=0;
	private   String  AccountID= null;
	boolean hasError = false;
	private   String clearedBal=null;
	private  String PipeString=null;
	private String convertedAmount = null;
	BigDecimal clearedBalance = null;
	String clearedBalType=null;
	String availBal = null;

	private ATMHelper atmHelper =  new ATMHelper();
	/**
	 * @param environment
	 * @param context
	 * @param priority
	 */
	public CardTechBalanceDownloadProcess(BankFusionEnvironment environment, AbstractFatomContext context, Integer priority) {
		super(environment, context, priority);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.trapedza.bankfusion.batch.process.AbstractBatchProcess#getAccumulator()
	 */
	public AbstractProcessAccumulator getAccumulator() {
		return accumulator;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.trapedza.bankfusion.batch.process.AbstractBatchProcess#init()
	 */
	public void init() {
		initialiseAccumulator();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.trapedza.bankfusion.batch.process.AbstractBatchProcess#initialiseAccumulator()
	 */
	protected void initialiseAccumulator() {
		Object[] accumulatorArgs = new Object[0];
		accumulator = new CardTechBalanceDownloadAccumulator(accumulatorArgs);
	}

	/**
	 *
	 * @description This is the process method that runs per page of dormancy
	 *              enabled accounts.
	 * @see com.trapedza.bankfusion.batch.process.AbstractBatchProcess#process(int)
	 */
	public AbstractProcessAccumulator process(int pageToProcess) throws BankFusionException {

		pagingData.setCurrentPageNumber(pageToProcess);
		try {
			ATMCardMapAccountsResultSet = environment.getFactory().findAll(IBOAccountDebitCardView.BONAME, pagingData).iterator();
		} catch (BankFusionException exception) {
			return accumulator;
		}
		while (ATMCardMapAccountsResultSet.hasNext()) {

			ArrayList params = new ArrayList();
			String ISOCurrencyCode=null;
			IBOAccountDebitCardView IBOAccountDebitCardViewdetails=null;
			IBOAccountDebitCardViewdetails = (IBOAccountDebitCardView) ATMCardMapAccountsResultSet.next();
			AccountID  = IBOAccountDebitCardViewdetails.getBoID();
			BigDecimal AvailableBalance = getAvailableBalance(AccountID, environment);
			String availableBalanceSign = atmHelper.getSign(AvailableBalance);
			currentAccountItem = (IBOAttributeCollectionFeature)environment.getFactory().findByPrimaryKey(IBOAttributeCollectionFeature.BONAME, AccountID);
			//clearedBalance = currentAccountItem.getF_CLEAREDBALANCE();
			//clearedBal=getSign(clearedBalance);
			if(currentAccountItem.isF_CLOSED())
				continue;

			int c =0;
			ISOCurrencyCode = currentAccountItem.getF_ISOCURRENCYCODE();
			String ISONumCurrCode=getNumericCurrCodes(ISOCurrencyCode,environment);

			//Rounding with Currency Scale
			BigDecimal convertedAvailBal = Utils.roundCurrency( AvailableBalance, bizInfo.getCurrencyRoundingUnits( ISOCurrencyCode , environment ) , bizInfo.getCurrencyScale( ISOCurrencyCode , environment ) );
			String availBalanceStr = convertedAvailBal.toString();

			//Format amount for interface file
			int currScale = bizInfo.getCurrencyScale( ISOCurrencyCode , environment );
			convertedAmount = (currScale==0?availBalanceStr + ".000":currScale==1?availBalanceStr+"00":currScale==2?availBalanceStr+"0":availBalanceStr);
			availBal = atmHelper.leftPad(convertedAmount.toString(), "0", 20);

			params.add(AccountID);
			List CardTechAtmCardsList = environment.getFactory().findByQuery(IBOATMCardAccountMap.BONAME,CardTechAtmCardNumbers,params,null);


			Iterator AtmCardNumbers = CardTechAtmCardsList.iterator();
			if(availableBalanceSign.equals("+")){
				clearedBalType="C";
			}
			else{
				clearedBalType="D";
			}



			String Balances = new String("BALS");
			String BALS = new String(Balances);
			int asciiPipe = 124;
			PipeString= ""+((char)asciiPipe);

			VectorTable AllDetails = new VectorTable();

			while(AtmCardNumbers.hasNext()) {
				if(CardTechAtmCardsList.size()>1){
					BalType="G";

				}
				else{
					BalType="C";
				}

				if(c>=1&&BalType.equals("G")){
					BalType="C";
					availBal = new String("0000000000000000.000");
				}

			    HashMap details = new HashMap();

				IBOATMCardAccountMap IBOATMCardAccountMapdetails = (IBOATMCardAccountMap)AtmCardNumbers.next();
				ATMCardNumbers=IBOATMCardAccountMapdetails.getF_ATMCARDNUMBER();
				details.put("BALS",BALS);
				details.put("Pipe", PipeString);
				details.put("BalanceType",BalType);
				details.put("clearedBal",clearedBalType);
				if(BalType.equals("G"))
				details.put("ATMCardNumbers",ATMCardNumbers+ISONumCurrCode);
				else
				details.put("ATMCardNumbers",ATMCardNumbers);

				String ISOCurrCode=ISOCurrencyCode;
				details.put("ISOCurrCode",ISONumCurrCode);
				details.put("OTB","");
				details.put("availableBalance",availBal);
				AllDetails.addAll(new VectorTable(details));
				c++;

			}

			balanceDownload(AllDetails,environment);


		}


		return accumulator;
	}






	private  synchronized void balanceDownload(VectorTable vector,BankFusionEnvironment environment) {



		HashMap updateMap = null;
		try{
			for(int j=0;j<vector.size();j++){
				updateMap = vector.getRowTags(j);
				VectorTable vectorTab = new VectorTable();
				vectorTab.addAll(new VectorTable(updateMap));
				HashMap attributes=new HashMap();
				attributes.put("Resultset", vectorTab);
				HashMap output = MFExecuter.executeMF("CardTechBalanceDownloadProcess", environment, attributes);

			}
		}


		catch (Exception e) {

			logger.error("process() General Exception: " + e.getLocalizedMessage());
		}
	}


	public BigDecimal getAvailableBalance(String accountID, BankFusionEnvironment env) 	{
		BigDecimal availableBalance = new BigDecimal("0");
		try	{
			HashMap hashMap = new HashMap();
			hashMap.put("AccountID", accountID);
			HashMap output = MFExecuter.executeMF("GetAvailableBalance", env, hashMap);
			availableBalance = (BigDecimal)output.get("AvailableBalance");
		}
		catch(Exception exception)	{
			availableBalance = new BigDecimal("0");
		}
		return availableBalance;

	}

	public String getSign (BigDecimal amount){
		String sign = "";
		if (amount.abs() == amount)
			sign = "+";
		else
			sign = "-";
		return sign;
	}

private String GetNumericCurrCodes(String CurrCode,BankFusionEnvironment env){
	ArrayList currNumCodes= new ArrayList();
	currNumCodes.add(CurrCode);
	try{
	List CurrNumericCodesList = env.getFactory().findByQuery(IBOCurrency.BONAME,NumericCurrCodes,currNumCodes,null);
	Iterator CurrCodes = CurrNumericCodesList.iterator();
	}
	catch (Exception e) {

		logger.error("process() General Exception: " + e.getLocalizedMessage());
	}
	return null;
}



public String getNumericCurrCodes(String AlphaCurrCode, BankFusionEnvironment env) {
    String NumericCode = "";
    try {
        ArrayList params = new ArrayList();
        params.add(AlphaCurrCode);
        List currencyList = env.getFactory().findByQuery(IBOCurrency.BONAME, NumericCurrCodes, params, null);
        if (currencyList.size() > 0) {
            IBOCurrency currency = (IBOCurrency) currencyList.get(0);
            NumericCode = currency.getF_NumericISOCurrencyCode();
        }
    } catch (BankFusionException exception) {
    }
    return NumericCode;
}

}
