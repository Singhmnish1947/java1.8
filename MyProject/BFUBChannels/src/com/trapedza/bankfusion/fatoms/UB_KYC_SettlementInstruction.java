package com.trapedza.bankfusion.fatoms;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_KYC_SettlementInstruction;
import com.trapedza.bankfusion.steps.refimpl.IUB_KYC_SettlementInstruction;
import com.trapedza.bankfusion.utils.GUIDGen;

public class UB_KYC_SettlementInstruction extends AbstractUB_KYC_SettlementInstruction implements IUB_KYC_SettlementInstruction {
    private transient final static Log logger = LogFactory.getLog(UB_KYC_SettlementInstruction.class.getName());
    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    public UB_KYC_SettlementInstruction(BankFusionEnvironment env) {
        super(env);
        // TODO Auto-generated constructor stub
    }

    String Status = "0";

    public void process(BankFusionEnvironment env) {

        if (env.getData().containsKey(IN_CustomerCode)) {
            logger.debug("Fircosoft inside if");
        }
        else {
            env.getData().put(IN_CustomerCode, "Y");

            String Currency = getF_IN_Currency();
            String input1 = getF_IN_Input1();
            String input2 = getF_IN_Input2();
            String input3 = getF_IN_Input3();
            String input4 = getF_IN_Input4();
            String input5 = getF_IN_Input5();
            String input6 = getF_IN_Input6();
            String input7 = getF_IN_Input7();
            String input8 = getF_IN_Input8();
            String input9 = getF_IN_Input9();
            String input10 = getF_IN_Input10();
            String input11 = getF_IN_Input11();
            String input12 = getF_IN_Input12();
            String input13 = getF_IN_Input13();
            String input14 = getF_IN_Input14();
            String input15 = getF_IN_Input15();
            String input16 = getF_IN_Input16();
            String input17 = getF_IN_Input17();
            String input18 = getF_IN_Input18();
            String input19 = getF_IN_Input19();
            String input20 = getF_IN_Input20();
            String input21 = getF_IN_Input21();
            String input22 = getF_IN_Input22();
            String input23 = getF_IN_Input23();
            String input24 = getF_IN_Input24();
            String input25 = getF_IN_Input25();
            String input26 = getF_IN_Input26();
            String input27 = getF_IN_Input27();
            String input28 = getF_IN_Input28();
            String input29 = getF_IN_Input29();
            String input30 = getF_IN_Input30();
            String input31 = getF_IN_Input31();
            String input32 = getF_IN_Input32();

            HashMap FircosoftDetail = new HashMap();

            FircosoftDetail.put("message1", input1);
            FircosoftDetail.put("message2", input2);
            FircosoftDetail.put("message3", input3);
            FircosoftDetail.put("message4", input4);
            FircosoftDetail.put("message5", input5);
            FircosoftDetail.put("message6", input6);
            FircosoftDetail.put("message7", input7);
            FircosoftDetail.put("message8", input8);
            FircosoftDetail.put("message9", input9);
            FircosoftDetail.put("message10", input10);
            FircosoftDetail.put("message11", input11);
            FircosoftDetail.put("message12", input12);
            FircosoftDetail.put("message13", input13);
            FircosoftDetail.put("message14", input14);
            FircosoftDetail.put("message15", input15);
            FircosoftDetail.put("message16", input16);
            FircosoftDetail.put("message17", input17);
            FircosoftDetail.put("message18", input18);
            FircosoftDetail.put("message19", input19);
            FircosoftDetail.put("message20", input20);
            FircosoftDetail.put("message21", input21);
            FircosoftDetail.put("message22", input22);
            FircosoftDetail.put("message23", input23);
            FircosoftDetail.put("message24", input24);
            FircosoftDetail.put("message25", input25);
            FircosoftDetail.put("message26", input26);
            FircosoftDetail.put("message27", input27);
            FircosoftDetail.put("message28", input28);
            FircosoftDetail.put("message29", input29);
            FircosoftDetail.put("message30", input30);
            FircosoftDetail.put("message31", input31);
            FircosoftDetail.put("message32", input32);
            FircosoftDetail.put("message33", Currency);
            FircosoftDetail.put("MESSAGEID", GUIDGen.getNewGUID());

            HashMap referralDetails = new HashMap();

            HashMap Noofrecords = new HashMap();
            try {
                Noofrecords = MFExecuter.executeMF("UB_KYC_FIC_FircosoftRequest_SRV", env, FircosoftDetail);
                Status = (String) Noofrecords.get("Status");
            }
            catch (BankFusionException e) {
                logger.error(ExceptionUtil.getExceptionAsString(e));
                /*
                 * EventsHelper.handleEvent(7328, BankFusionMessages.ERROR_LEVEL, new Object[] {},
                 * referralDetails, env);
                 */
                EventsHelper.handleEvent(ChannelsEventCodes.E_COULD_NOT_ESTABLISH_CONNECTION_WITH_FIRCOSOFT, new Object[] {},
                        referralDetails, env);

            }

            // Noofrecords = 1;
            logger.debug("Fircosoft inside else");

        }

        setF_OUT_Status(Status);

    }

}
