package com.trapedza.bankfusion.fatoms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractBPW_CustomerTruncate;
import com.trapedza.bankfusion.steps.refimpl.IBPW_CustomerTruncate;

public class BPW_CustomerTruncate extends AbstractBPW_CustomerTruncate implements IBPW_CustomerTruncate {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	public BPW_CustomerTruncate(BankFusionEnvironment env) {
		super(env);
	}

	private transient final static Log logger = LogFactory.getLog(BPW_CustomerTruncate.class.getName());

	public void process(BankFusionEnvironment env) {

		String LongName, postName1, postName2;

		if (getF_IN_Address1().length() > 30)
			setF_OUT_Address1(getF_IN_Address1().substring(0, 30));
		else
			setF_OUT_Address1(getF_IN_Address1());

		if (getF_IN_Address2().length() > 30)
			setF_OUT_Address2(getF_IN_Address2().substring(0, 30));
		else
			setF_OUT_Address2(getF_IN_Address2());

		if (getF_IN_Address3().length() > 20)
			setF_OUT_Address3(getF_IN_Address3().substring(0, 20));
		else
			setF_OUT_Address3(getF_IN_Address3());

		if (getF_IN_Address4().length() > 20)
			setF_OUT_Address4(getF_IN_Address4().substring(0, 20));
		else
			setF_OUT_Address4(getF_IN_Address4());

		if (getF_IN_BusUnit().length() > 3)
			setF_OUT_BusUnit(getF_IN_BusUnit().substring(0, 3));
		else
			setF_OUT_BusUnit(getF_IN_BusUnit());

		if (getF_IN_AlphaCode().length() > 15)
			setF_OUT_AlphaCode(getF_IN_AlphaCode().substring(0, 15));
		else
			setF_OUT_AlphaCode(getF_IN_AlphaCode());

		if (getF_IN_CustSegID().length() > 3)
			setF_OUT_CustSegID(getF_IN_CustSegID().substring(0, 3));
		else
			setF_OUT_CustSegID(getF_IN_CustSegID());

		if (getF_IN_IndusType().length() > 15)
			setF_OUT_IndusType(getF_IN_IndusType().substring(0, 15));
		else
			setF_OUT_IndusType(getF_IN_IndusType());

		if (getF_IN_LongName().length() > 60)
			LongName = getF_IN_LongName().substring(0, 60);
		else
			LongName = getF_IN_LongName();

		if (LongName.indexOf(":") > 0) {
			postName1 = LongName.substring(0, LongName.indexOf(":"));
			postName2 = LongName.substring(LongName.indexOf(":") + 1, LongName.length());
			setF_OUT_PostName1(postName1);
			setF_OUT_PostName2(postName2);
		}
		else {
			if (LongName.length() > 30) {
				setF_OUT_PostName1(LongName.substring(0, 30));
				setF_OUT_PostName2(LongName.substring(31, LongName.length()));
			}
			else {
				setF_OUT_PostName1(LongName);
				setF_OUT_PostName2(CommonConstants.EMPTY_STRING);
			}

		}

		if (getF_IN_PostCode().length() > 10)
			setF_OUT_PostCode(getF_IN_PostCode().substring(0, 10));
		else
			setF_OUT_PostCode(getF_IN_PostCode());

		if (getF_IN_SectorType().length() > 2)
			setF_OUT_SectorType(getF_IN_SectorType().substring(0, 2));
		else
			setF_OUT_SectorType(getF_IN_SectorType());

		if (getF_IN_ShortName().length() > 30)
			setF_OUT_ShortName(getF_IN_ShortName().substring(0, 30));
		else
			setF_OUT_ShortName(getF_IN_ShortName());

		if (getF_IN_HomeTeleNum().length() > 10)
			setF_OUT_HomeTeleNum(getF_IN_HomeTeleNum().substring(0, 10));
		else
			setF_OUT_HomeTeleNum(getF_IN_HomeTeleNum());

		if (getF_IN_WorkTeleNum().length() > 10)
			setF_OUT_WorkTeleNum(getF_IN_WorkTeleNum().substring(0, 10));
		else
			setF_OUT_WorkTeleNum(getF_IN_WorkTeleNum());

		if (getF_IN_Employer().length() > 30)
			setF_OUT_Employer(getF_IN_Employer().substring(0, 30));
		else
			setF_OUT_Employer(getF_IN_Employer());

		if (getF_IN_IdNum().length() > 15)
			setF_OUT_IdNum(getF_IN_IdNum().substring(0, 15));
		else
			setF_OUT_IdNum(getF_IN_IdNum());

		logger.info("getF_IN_Gender()" + getF_IN_Gender());
		if (getF_IN_Gender().equals("002"))
			setF_OUT_Gender("0");
		else
			setF_OUT_Gender("1");
	}

}
