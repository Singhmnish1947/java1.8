package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.List;

import bf.com.misys.ub.types.interfaces.Ub_MT103;
import bf.com.misys.ub.types.interfaces.Ub_MT200;
import bf.com.misys.ub.types.interfaces.Ub_MT202;
import bf.com.misys.ub.types.interfaces.Ub_MT205;
import bf.com.misys.ub.types.remittanceprocess.UB_SWT_RemittanceProcessRq;

import com.misys.bankfusion.common.runtime.service.ServiceManagerFactory;
import com.misys.bankfusion.serviceinvocation.IUserExitInvokerService;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.misys.ub.swift.RemittanceDataTransform;
import com.misys.ub.swift.RemittanceIdGenerator;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_MessageHeader;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_SWTMessageDetail;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractRemittanceProcessing;


public class RemittanceProcessing extends AbstractRemittanceProcessing{

	static final String query1 = " WHERE " + IBOUB_INF_MessageHeader.MESSAGEID1	+ " = ?";


	IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();

	public RemittanceProcessing(BankFusionEnvironment env)
	{
		super(env);
	}

	public void process(BankFusionEnvironment env)
	{

		UB_SWT_RemittanceProcessRq remittanceProcessRq = getF_IN_RemittanceRequest();
		RemittanceIdGenerator remitIdGenerator = new RemittanceIdGenerator();
		RemittanceDataTransform remitDataTransform = new RemittanceDataTransform();
		Ub_MT205 ubMT205 = null;
		Ub_MT202 ubMT202 = null;
		Ub_MT103 ubMT103 = null;
		Ub_MT200 ubMT200 = null;
		IBOUB_INF_MessageHeader messageHeader=null;
		ArrayList param = new ArrayList();
		param.add(remittanceProcessRq.getREMITTANCE_ID());

		List msgHdrList =  factory.findByQuery(IBOUB_INF_MessageHeader.BONAME, query1, param, null);
		if(msgHdrList!=null && msgHdrList.size()>0){
			messageHeader=(IBOUB_INF_MessageHeader)msgHdrList.get(0);

			//IBOUB_INF_MessageHeader messageHeader = (IBOUB_INF_MessageHeader) (factory.findByQuery(IBOUB_INF_MessageHeader.BONAME, query1, param, null)).get(0);

			if("MT205".equals(messageHeader.getF_MESSAGETYPE())){
				ubMT205 = remitDataTransform.getMT205FromRemittance(remittanceProcessRq);
				setF_OUT_Ub_MT205(ubMT205);
			}
			if("MT202".equals(messageHeader.getF_MESSAGETYPE())){
				ubMT202 = remitDataTransform.getMT202FromRemittance(remittanceProcessRq);
				setF_OUT_Ub_MT202(ubMT202);
			}
			if("MT103".equals(messageHeader.getF_MESSAGETYPE())){
				ubMT103 = remitDataTransform.getMT103FromRemittance(remittanceProcessRq);
				setF_OUT_Ub_MT103(ubMT103);
			}
			if("MT200".equals(messageHeader.getF_MESSAGETYPE())){
				ubMT200 = remitDataTransform.getMT200FromRemittance(remittanceProcessRq);
				setF_OUT_Ub_MT200(ubMT200);
			}
			if("IPAY".equals(messageHeader.getF_MESSAGETYPE())){
				PaymentSwiftUtils.handleEvent(Integer.parseInt(PaymentSwiftConstants.NO_RECORD_EXISTS), new String[] {});
			}
		}
		else{
			PaymentSwiftUtils.handleEvent(Integer.parseInt(PaymentSwiftConstants.NO_RECORD_EXISTS), new String[] {});
		}

		String creditAccount = remittanceProcessRq.getCREDITORDTL().getCREDITACCOUNTID();
		String debitAccount = remittanceProcessRq.getDEBITORDTL().getDEBITACCOUNTID();
		IUserExitInvokerService userExitInvokerService = (IUserExitInvokerService) ServiceManagerFactory.getInstance()
				.getServiceManager().getServiceForName(IUserExitInvokerService.SERVICE_NAME);
		ArrayList<String> params = new ArrayList<String>();
		Object response = null;
		String remittanceId = null;
		params.add(remittanceProcessRq.getTRANSACTIONDETAISINFO().getCURRENCY());
		/*if(userExitInvokerService.isValidBeanId("remittanceIdGenerator")){
			response = userExitInvokerService.invokeService("remittanceIdGenerator", params);
			remittanceId = (String)response;
		} else{
			remittanceId = remitIdGenerator.getRemittanceId(remittanceProcessRq.getTRANSACTIONDETAISINFO().getCURRENCY());
		}*/
		remittanceId = remitIdGenerator.getRemittanceId(remittanceProcessRq.getTRANSACTIONDETAISINFO().getCURRENCY());

		setF_OUT_CreditAccount(creditAccount);
		setF_OUT_DebitAccount(debitAccount);
		setF_OUT_FromRemitScreen("Y");
		setF_OUT_RemittanceId(remittanceId);


	}
}
