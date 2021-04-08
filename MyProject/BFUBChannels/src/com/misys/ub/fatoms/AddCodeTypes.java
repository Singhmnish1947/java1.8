package com.misys.ub.fatoms;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_INF_AddCodeTypes;

public class AddCodeTypes extends AbstractUB_INF_AddCodeTypes {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	public static final String CODE_TYPE = "%";
	public static final String DESCRIPTION = "ALL";

	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	public AddCodeTypes(BankFusionEnvironment env) {
		super(env);
	}

	private transient final static Log logger = LogFactory
			.getLog(AddCodeTypes.class.getName());

	public void process(BankFusionEnvironment env) {

		VectorTable resultGrid = constructVectorRow(getF_IN_codeTypes());

		setF_OUT_codeTypes(resultGrid);

	}

	private VectorTable constructVectorRow(VectorTable grid) {
		Map codeType = null;
		codeType = new HashMap();
		codeType.put("CODETYPEID", CommonConstants.EMPTY_STRING);
		codeType.put("BOTYPEID", CommonConstants.EMPTY_STRING);
		codeType.put("DESCRIPTION", DESCRIPTION);
		codeType.put("SELECT", Boolean.FALSE);
		codeType.put("SRNO", CommonConstants.EMPTY_STRING);
		codeType.put("SUBCODETYPE", CODE_TYPE);
		VectorTable gridOutput = new VectorTable();
		gridOutput.addAll(new VectorTable(codeType));
		for (int x = 0; x < grid.size(); x++) {

			codeType = grid.getRowTags(x);
			codeType.put("SUBCODETYPE", codeType.get("DESCRIPTION"));
			gridOutput.addAll(new VectorTable(codeType));
		}

		return gridOutput;

	}
}
