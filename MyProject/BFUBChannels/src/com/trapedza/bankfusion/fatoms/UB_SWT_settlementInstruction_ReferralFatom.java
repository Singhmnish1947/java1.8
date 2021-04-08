package com.trapedza.bankfusion.fatoms;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.fatoms.ReferralFatom;
import com.trapedza.bankfusion.notification.gateway.interfaces.ITask;
import com.trapedza.bankfusion.notification.tasks.TaskManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_settlementInstruction_ReferralFatom;

public class UB_SWT_settlementInstruction_ReferralFatom extends
		AbstractUB_SWT_settlementInstruction_ReferralFatom {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	private transient final static Log logger = LogFactory.getLog(ReferralFatom.class.getName());
	public UB_SWT_settlementInstruction_ReferralFatom(BankFusionEnvironment env) {
		super(env);
	}
	public void process(BankFusionEnvironment env) {
		String eventType = getF_IN_EventType();
		Integer eventNumber = getF_IN_EventNumber();
		String eventLevel = getF_IN_EventLevel();
		String eventMessage = getF_IN_EventMessage();
		if (logger.isDebugEnabled()) {
			logger.info("Raising Event ...");
			logger.info("Event - Type  " + eventType);
			logger.info("Event - Message" + eventMessage);
			logger.info("Event - Number  " + eventNumber);
			logger.info("Event - Level  " + eventLevel);
		}
		Map eventDetails = new HashMap();

		eventDetails.put(getF_IN_KEY1(), getF_IN_Value1().toString());
		eventDetails.put(getF_IN_KEY2(), getF_IN_Value2().toString());
		eventDetails.put(getF_IN_KEY3(), getF_IN_Value3().toString());
		eventDetails.put(getF_IN_KEY4(), getF_IN_Value4().toString());
		eventDetails.put(getF_IN_KEY5(), getF_IN_Value5().toString());
		eventDetails.put(getF_IN_KEY6(), getF_IN_Value6().toString());
		eventDetails.put(getF_IN_KEY7(), getF_IN_Value7().toString());
		eventDetails.put(getF_IN_KEY8(), getF_IN_Value8().toString());
		eventDetails.put(getF_IN_KEY9(), getF_IN_Value9().toString());
		eventDetails.put(getF_IN_KEY10(), getF_IN_value10().toString());
		eventDetails.put(getF_IN_KEY11(), getF_IN_value11().toString());
		eventDetails.put(getF_IN_KEY12(), getF_IN_value12().toString());
		eventDetails.put(getF_IN_KEY13(), getF_IN_value13().toString());
		eventDetails.put(getF_IN_KEY14(), getF_IN_value14().toString());
		eventDetails.put(getF_IN_KEY15(), getF_IN_value15().toString());
		eventDetails.put(getF_IN_KEY16(), getF_IN_value16().toString());
		eventDetails.put(getF_IN_KEY17(), getF_IN_value17().toString());
		eventDetails.put(getF_IN_KEY18(), getF_IN_value18().toString());
		eventDetails.put(getF_IN_KEY19(), getF_IN_value19().toString());
		eventDetails.put(getF_IN_KEY20(), getF_IN_value20().toString());
		eventDetails.put(getF_IN_KEY21(), getF_IN_value21().toString());
		eventDetails.put(getF_IN_KEY22(), getF_IN_value22().toString());
		eventDetails.put(getF_IN_KEY23(), getF_IN_value23().toString());
		eventDetails.put(getF_IN_KEY24(), getF_IN_value24().toString());
		eventDetails.put(getF_IN_KEY25(), getF_IN_value25().toString());
		eventDetails.put(getF_IN_KEY26(), getF_IN_value26().toString());
		eventDetails.put(getF_IN_KEY27(), getF_IN_value27().toString());
		eventDetails.put(getF_IN_KEY28(), getF_IN_value28().toString());
		eventDetails.put(getF_IN_KEY29(), getF_IN_value29().toString());
		eventDetails.put(getF_IN_KEY30(), getF_IN_value30().toString());
		eventDetails.put(getF_IN_KEY31(), getF_IN_value31().toString());
		eventDetails.put(getF_IN_KEY32(), getF_IN_value32().toString());
		eventDetails.put(getF_IN_KEY33(), getF_IN_value33().toString());
		if (eventType != null && eventType.trim().length() != 0 && eventType.equals("business.referral")) {
			TaskManager taskManager = new TaskManager();
			ITask task = taskManager.createTask(eventType);

			task.setComment(eventMessage);
			task.setDetails(eventDetails);

			taskManager.raiseTask(task, env);
		}
		else if (eventNumber != null && eventNumber.intValue() != 0 && eventType != null
				&& eventType.trim().equals(CommonConstants.EMPTY_STRING)) {
			//EventsHelper.handleEvent(eventNumber.intValue(), eventLevel, new Object[] {}, eventDetails, env);
			EventsHelper.handleEvent(ChannelsEventCodes.W_HIT_FOUND_IN_WATCH_LIST,								
					new Object[] {}, new HashMap(), env);
    
		}
		
	}
	
}
