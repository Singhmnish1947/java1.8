package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.ub.types.interfaces.MessageHeader;
import bf.com.misys.ub.types.interfaces.SwiftMT103;
import bf.com.misys.ub.types.interfaces.SwiftMT200;
import bf.com.misys.ub.types.interfaces.SwiftMT201;
import bf.com.misys.ub.types.interfaces.SwiftMT202;
import bf.com.misys.ub.types.interfaces.SwiftMT203;
import bf.com.misys.ub.types.interfaces.SwiftMT205;
import bf.com.misys.ub.types.interfaces.Ub_MT103;
import bf.com.misys.ub.types.interfaces.Ub_MT200;
import bf.com.misys.ub.types.interfaces.Ub_MT201;
import bf.com.misys.ub.types.interfaces.Ub_MT202;
import bf.com.misys.ub.types.interfaces.Ub_MT203;
import bf.com.misys.ub.types.interfaces.Ub_MT205;

import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_SWTMessageDetail;
import com.trapedza.bankfusion.boundary.outward.BankFusionIOSupport;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_MessageDetail;
/*@Itesh kumar
 *@28 July 2009
 * Description: This class persist the information for every incoming message into Swift Message Detail table. 
 */



public class UB_SWT_MessageDetail extends AbstractUB_SWT_MessageDetail {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	
	public UB_SWT_MessageDetail(BankFusionEnvironment env) {
		super(env);
	}
	private transient final static Log logger = LogFactory
	.getLog(UB_SWT_MessageDetail.class.getName());


	@Override
	
	public void process(BankFusionEnvironment env)throws BankFusionException  {
		

		IBOUB_INF_SWTMessageDetail swtmessageDetail ;
		if (getF_IN_MessageType().equals("MT200")) {

			Ub_MT200 MT200 = getF_IN_Ub_MT200();
			SwiftMT200 MT200Details = MT200.getDetails();
			MessageHeader MT200header = MT200.getHeader();
            
			
			swtmessageDetail = (IBOUB_INF_SWTMessageDetail) BankFusionThreadLocal.getPersistanceFactory().getStatelessNewInstance(IBOUB_INF_SWTMessageDetail.BONAME);
			swtmessageDetail.setF_AMOUNT(new BigDecimal(MT200Details.getTdamount()));
			swtmessageDetail.setF_CURRENCYCODE(MT200Details.getTdcurrencyCode());
			swtmessageDetail.setF_MESSAGEOBJECT(BankFusionIOSupport.convertToBytes(MT200));
			
            swtmessageDetail.setF_SENDER(MT200Details.getSender());
            swtmessageDetail.setF_VALUEDT(Date.valueOf(MT200Details.getTdvalueDate()));
            swtmessageDetail.setF_REFERENCE(MT200Details.getTransactionReferenceNumber());
            swtmessageDetail.setBoID(getF_IN_MESSAGEID());
            BankFusionThreadLocal.getPersistanceFactory().create(IBOUB_INF_SWTMessageDetail.BONAME, swtmessageDetail);
          
			
			
		}
		
		/*MT201*/

		
		
		if (getF_IN_MessageType().equals("MT201")) {

			Ub_MT201 MT201 = getF_IN_Ub_MT201();
			SwiftMT201 MT201Details = MT201.getDetails();
			MessageHeader MT201header = MT201.getHeader();
			
			swtmessageDetail = (IBOUB_INF_SWTMessageDetail) BankFusionThreadLocal.getPersistanceFactory().getStatelessNewInstance(IBOUB_INF_SWTMessageDetail.BONAME);
			swtmessageDetail.setF_AMOUNT(new BigDecimal(MT201Details.getSumOfAmounts()));
			swtmessageDetail.setF_MESSAGEOBJECT(BankFusionIOSupport.convertToBytes(MT201));
            swtmessageDetail.setF_SENDER(MT201Details.getSender());
            swtmessageDetail.setF_VALUEDT(Date.valueOf(MT201Details.getValueDate()));
            swtmessageDetail.setBoID(getF_IN_MESSAGEID());
            BankFusionThreadLocal.getPersistanceFactory().create(IBOUB_INF_SWTMessageDetail.BONAME, swtmessageDetail);
          
			
			
		}
		
		/*MT203*/

		
		
		if (getF_IN_MessageType().equals("MT203")) {

			Ub_MT203 MT203 = getF_IN_UB_MT203();
			SwiftMT203 MT203Details = MT203.getDetails();
			MessageHeader MT203header = MT203.getHeader();
			
			swtmessageDetail = (IBOUB_INF_SWTMessageDetail) BankFusionThreadLocal.getPersistanceFactory().getStatelessNewInstance(IBOUB_INF_SWTMessageDetail.BONAME);
			swtmessageDetail.setF_AMOUNT(new BigDecimal(MT203Details.getSumOfAmounts()));
			swtmessageDetail.setF_MESSAGEOBJECT(BankFusionIOSupport.convertToBytes(MT203));
			
            swtmessageDetail.setF_SENDER(MT203Details.getSender());
            swtmessageDetail.setF_VALUEDT(Date.valueOf(MT203Details.getValueDate()));
            swtmessageDetail.setBoID(getF_IN_MESSAGEID());
            BankFusionThreadLocal.getPersistanceFactory().create(IBOUB_INF_SWTMessageDetail.BONAME, swtmessageDetail);
          
			
			
		}


		/*
		 * FOR MT103
		 */

		if (getF_IN_MessageType().equals("MT103")) {
			Ub_MT103 MT103 = getF_IN_Ub_MT103();
			SwiftMT103 MT103Details = MT103.getDetails();
			MessageHeader MT103header = MT103.getHeader();
			
			swtmessageDetail = (IBOUB_INF_SWTMessageDetail) BankFusionThreadLocal.getPersistanceFactory().getStatelessNewInstance(IBOUB_INF_SWTMessageDetail.BONAME);
			swtmessageDetail.setF_AMOUNT(new BigDecimal(MT103Details.getTdAmount()));
			swtmessageDetail.setF_CURRENCYCODE(MT103Details.getTdCurrencyCode());
			swtmessageDetail.setF_MESSAGEOBJECT(BankFusionIOSupport.convertToBytes(MT103));
			swtmessageDetail.setF_BENFCYCUSTOMER(MT103Details.getBeneficiaryCustomer());
			swtmessageDetail.setF_BENFCYCUSTOPTION(MT103Details.getBeneficiaryCustOption());
			swtmessageDetail.setF_ORDERINGCUSTOMER(MT103Details.getOrderingCustomer());
            swtmessageDetail.setF_SENDER(MT103Details.getSender());
            swtmessageDetail.setF_ORDERINGCUSTOPTION(MT103Details.getOrderingCustomerOption());
            swtmessageDetail.setF_ORDERINGINSTITUTION(MT103Details.getOrderingInstitution());
            swtmessageDetail.setF_ORDERINGINSTOPTION(MT103Details.getOrderInstitutionOption());
            swtmessageDetail.setF_VALUEDT(Date.valueOf(MT103Details.getTdValueDate()));
            swtmessageDetail.setF_REFERENCE(MT103Details.getSendersReference());
            swtmessageDetail.setBoID(getF_IN_MESSAGEID());
            BankFusionThreadLocal.getPersistanceFactory().create(IBOUB_INF_SWTMessageDetail.BONAME, swtmessageDetail);
					}
		
		
		

		/*
		 * FOR MT202
		 */

		if (getF_IN_MessageType().equals("MT202")) {
			Ub_MT202 MT202 = getF_IN_Ub_MT202();
			SwiftMT202 MT202Details = MT202.getDetails();
			MessageHeader MT202header = MT202.getHeader();

			
			swtmessageDetail = (IBOUB_INF_SWTMessageDetail) BankFusionThreadLocal.getPersistanceFactory().getStatelessNewInstance(IBOUB_INF_SWTMessageDetail.BONAME);
			swtmessageDetail.setF_AMOUNT(new BigDecimal(MT202Details.getTdAmount()));
			swtmessageDetail.setF_CURRENCYCODE(MT202Details.getTdCurrencyCode());
			swtmessageDetail.setF_MESSAGEOBJECT(BankFusionIOSupport.convertToBytes(MT202));
			swtmessageDetail.setF_BENFCYINSTITUTION(MT202Details.getBeneficiary());
			swtmessageDetail.setF_BENFCYINSTOPTION(MT202Details.getBeneficiaryOption());
            swtmessageDetail.setF_SENDER(MT202Details.getSender());
            swtmessageDetail.setF_ORDERINGINSTITUTION(MT202Details.getOrderingInstitution());
            swtmessageDetail.setF_ORDERINGINSTOPTION(MT202Details.getOrderingInstitutionOption());
            swtmessageDetail.setF_VALUEDT(Date.valueOf(MT202Details.getTdValueDate()));
            swtmessageDetail.setF_REFERENCE(MT202Details.getTransactionReferenceNumber());
            swtmessageDetail.setBoID(getF_IN_MESSAGEID());
            swtmessageDetail.setF_RELATEDREFERENCE(MT202Details.getRelatedReference());
            BankFusionThreadLocal.getPersistanceFactory().create(IBOUB_INF_SWTMessageDetail.BONAME, swtmessageDetail);
					}

		
		/*
		 * FOR MT205
		 */

		if (getF_IN_MessageType().equals("MT205")) {
			Ub_MT205 MT205 = getF_IN_Ub_MT205();
			SwiftMT205 MT205Details = MT205.getDetails();
			MessageHeader MT205header = MT205.getHeader();
			
			swtmessageDetail = (IBOUB_INF_SWTMessageDetail) BankFusionThreadLocal.getPersistanceFactory().getStatelessNewInstance(IBOUB_INF_SWTMessageDetail.BONAME);
			swtmessageDetail.setF_AMOUNT(new BigDecimal(MT205Details.getTdamount()));
			swtmessageDetail.setF_CURRENCYCODE(MT205Details.getTdcurrencyCode());
			swtmessageDetail.setF_MESSAGEOBJECT(BankFusionIOSupport.convertToBytes(MT205));
            swtmessageDetail.setF_SENDER(MT205Details.getSender());
            swtmessageDetail.setF_BENFCYINSTITUTION(MT205Details.getBeneficiaryInstitute());
            swtmessageDetail.setF_BENFCYINSTOPTION(MT205Details.getBeneficiaryInstOption());
            swtmessageDetail.setF_ORDERINGINSTITUTION(MT205Details.getOrderingInstitute());
            swtmessageDetail.setF_ORDERINGINSTOPTION((MT205Details.getOrderingInstitutionOption()));
            swtmessageDetail.setF_VALUEDT(Date.valueOf((MT205Details.getTdvalueDate())));
            swtmessageDetail.setF_REFERENCE(MT205Details.getTransactionReferenceNumber());
            swtmessageDetail.setF_RELATEDREFERENCE(MT205Details.getRelatedReference());
            swtmessageDetail.setBoID(getF_IN_MESSAGEID());
            BankFusionThreadLocal.getPersistanceFactory().create(IBOUB_INF_SWTMessageDetail.BONAME, swtmessageDetail);
            
					}
		
	}
	}




