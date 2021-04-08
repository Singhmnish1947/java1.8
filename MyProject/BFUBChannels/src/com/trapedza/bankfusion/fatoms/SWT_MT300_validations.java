package com.trapedza.bankfusion.fatoms;

import java.util.HashMap;

import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_MT300_validations;

public class SWT_MT300_validations extends AbstractUB_SWT_MT300_validations {
	/**
	 * The Class SWT_MT300_validations.
	 * 
	 * @AUTHOR Binit Kumar
	 * @PROJECT Swift
	 */
	private static final long serialVersionUID = 4574002842397996508L;

	@SuppressWarnings("deprecation")
	public SWT_MT300_validations(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) {
		super.process(env);
		String MessageType = getF_IN_MessageType();
		String SenderToreceiverInformation1 = getF_IN_SenderToreceiverInformation1();
		String SenderToreceiverInformation2 = getF_IN_SenderToreceiverInformation2();
		String SenderToreceiverInformation3 = getF_IN_SenderToreceiverInformation3();
		String SenderToreceiverInformation4 = getF_IN_SenderToreceiverInformation4();
		String SenderToreceiverInformation5 = getF_IN_SenderToreceiverInformation5();
		String SenderToreceiverInformation6 = getF_IN_SenderToreceiverInformation6();

		String[] Params = {""};
		
		if (MessageType.equals("300")) {

			
			if (SenderToreceiverInformation1.startsWith("/PUTI/")
					|| SenderToreceiverInformation1.startsWith("/UTI/")
					||SenderToreceiverInformation1.startsWith("/PUSI/")
					|| SenderToreceiverInformation1.startsWith("/USI/")) {
				setF_OUT_ErrorMessage("Fail");
				EventsHelper.handleEvent(40009290, Params, new HashMap(), env);
				setF_OUT_ValidationStatus(false);//take form event code message
			} else if(SenderToreceiverInformation2.startsWith("/PUTI/")
					|| SenderToreceiverInformation2.startsWith("/UTI/")
					||SenderToreceiverInformation2.startsWith("/PUSI/")
					|| SenderToreceiverInformation2.startsWith("/USI/")) {
				setF_OUT_ErrorMessage("Fail");
				EventsHelper.handleEvent(40009290, Params, new HashMap(), env);
				setF_OUT_ValidationStatus(false);//take form event code message
			} else if (SenderToreceiverInformation3.startsWith("/PUTI/")
					|| SenderToreceiverInformation3.startsWith("/UTI/")
					||SenderToreceiverInformation3.startsWith("/PUSI/")
					|| SenderToreceiverInformation3.startsWith("/USI/")) {
				setF_OUT_ErrorMessage("Fail");
				EventsHelper.handleEvent(40009290, Params, new HashMap(), env);
				setF_OUT_ValidationStatus(false);//take form event code message
			} else if(SenderToreceiverInformation4.startsWith("/PUTI/")
					|| SenderToreceiverInformation4.startsWith("/UTI/")
					||SenderToreceiverInformation4.startsWith("/PUSI/")
					|| SenderToreceiverInformation4.startsWith("/USI/")) {
				setF_OUT_ErrorMessage("Fail");//take form event code message
				EventsHelper.handleEvent(40009290, Params, new HashMap(), env);
				setF_OUT_ValidationStatus(false);
			} else if (SenderToreceiverInformation5.startsWith("/PUTI/")
					|| SenderToreceiverInformation5.startsWith("/UTI/")
					||SenderToreceiverInformation5.startsWith("/PUSI/")
					|| SenderToreceiverInformation5.startsWith("/USI/")) {
				setF_OUT_ErrorMessage("Fail");//take form event code message
				EventsHelper.handleEvent(40009290, Params, new HashMap(), env);
				setF_OUT_ValidationStatus(false);
			} else if(SenderToreceiverInformation6.startsWith("/PUTI/")
					|| SenderToreceiverInformation6.startsWith("/UTI/")
					||SenderToreceiverInformation6.startsWith("/PUSI/")
					|| SenderToreceiverInformation6.startsWith("/USI/")){
				setF_OUT_ErrorMessage("Fail");//take form event code message
				EventsHelper.handleEvent(40009290, Params, new HashMap(), env);
				setF_OUT_ValidationStatus(false);
			}  else 
			
			{
				setF_OUT_ErrorMessage("Pass");//take form event code message
				setF_OUT_ValidationStatus(true);
			} 
			
						

		}
		
		else {
			setF_OUT_ErrorMessage("Pass");//take form event code message
			setF_OUT_ValidationStatus(true);
		
		}

	}
}
