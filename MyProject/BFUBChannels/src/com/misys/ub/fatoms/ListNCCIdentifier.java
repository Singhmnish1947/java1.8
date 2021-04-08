/**
 * 
 */
package com.misys.ub.fatoms;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.bo.refimpl.IBOUB_SWT_NCCCODES;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_ListNCCIdentifier;
import com.trapedza.bankfusion.steps.refimpl.IUB_SWT_ListNCCIdentifier;

/**
 * @author Gupta,Vikash
 * @date 30 April 2011
 * @project Universal Banking 1.6.3
 * @Description:
 */
public class ListNCCIdentifier extends AbstractUB_SWT_ListNCCIdentifier
implements IUB_SWT_ListNCCIdentifier {

	/**
	 * <code>svnRevision</code> = "$Revision: 1.0 $"
	 */
	public static final String svnRevision = "$Revision: 1.0 $";

	public static final String LIKE = " LIKE ";
	public static final String EQUALS = " = ";
	public static final String QUERYPARAM = " ? ";

	public static final String CLEARINGCODEQUERY = " WHERE "
		+ IBOUB_SWT_NCCCODES.CLEARINGCODE + " = ?";
	public static final String IDENTIFIERCODEQUERY = " AND "
		+ IBOUB_SWT_NCCCODES.IDENTIFIERCODE;
	public static final String BANKCODEQUERY = " AND "
		+ IBOUB_SWT_NCCCODES.BANKCODE;
	public static final String BANKSHORTNAMEQUERY = " AND "
		+ IBOUB_SWT_NCCCODES.BANKSHORTNAME;
	public static final String SUBBRANCHSUFFIXQUERY = " AND "
		+ IBOUB_SWT_NCCCODES.SUBBRANCHSUFFIX;
	public static final String BRANCHNAMEQUERY = " AND "
		+ IBOUB_SWT_NCCCODES.BRANCHNAME;

	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}
	private transient final static Log logger = LogFactory
	.getLog(ListNCCIdentifier.class.getName());

	/**
	 * @param env
	 */
	public ListNCCIdentifier(BankFusionEnvironment env) {
		super(env);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_ListNCCIdentifier#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
	 */
	public void process(BankFusionEnvironment env) throws BankFusionException {

		ArrayList<String> params = new ArrayList<String>();
		List resultList = new ArrayList();

		// Build complete query based on input parameters
		String queryWhereClause = CLEARINGCODEQUERY;
		params.add(getF_IN_NCCCode());
		queryWhereClause = dynamicQueryBuilder(getF_IN_IdentifierCode(),
				queryWhereClause, IDENTIFIERCODEQUERY, params);
		queryWhereClause = dynamicQueryBuilder(getF_IN_BankCode(),
				queryWhereClause, BANKCODEQUERY, params);
		queryWhereClause = dynamicQueryBuilder(getF_IN_BankShortName(),
				queryWhereClause, BANKSHORTNAMEQUERY, params);
		queryWhereClause = dynamicQueryBuilder(getF_IN_SubBranchSuffix(),
				queryWhereClause, SUBBRANCHSUFFIXQUERY, params);
		queryWhereClause = dynamicQueryBuilder(getF_IN_BankBranchName(),
				queryWhereClause, BRANCHNAMEQUERY, params);

		// Execute final build query.
		resultList = env.getFactory().findByQuery(IBOUB_SWT_NCCCODES.BONAME,
				queryWhereClause, params, null, true);


		// Set Output Parameters.
		if (resultList.size() > CommonConstants.INTEGER_ZERO) {
			VectorTable nccIdentifierList = getVectorTableFromList(resultList);
			Map selectMap = (nccIdentifierList.getRowTags(CommonConstants.INTEGER_ZERO));
			selectMap.put(CommonConstants.SELECT, Boolean.TRUE);
			nccIdentifierList.addAll(new VectorTable(selectMap));
			setF_OUT_ListNCCIdentifier(nccIdentifierList);
			setF_OUT_ListNCCIdentifier_NOOFROWS(resultList.size());
		} else {
			setF_OUT_ListNCCIdentifier(new VectorTable());
			setF_OUT_ListNCCIdentifier_NOOFROWS(CommonConstants.INTEGER_ZERO);
		}

	}

	// Build dynamic query
	/**
	 * @param field
	 * @param baseQuery
	 * @param apenderQuery
	 * @param params
	 * @return resultQuery
	 */
	public String dynamicQueryBuilder(String field, String baseQuery,
			String apenderQuery, ArrayList<String> params) {

		String resultQuery = baseQuery;
		if (!isStringEmpty(field)) {
			resultQuery = field.contains("%") ? baseQuery + apenderQuery + LIKE
					: baseQuery + apenderQuery + EQUALS;
			resultQuery += QUERYPARAM;
			params.add(field);
		}
		return resultQuery;
	}

	// Method to convert List to Vector.
	/**
	 * @param list
	 * @return vectorTable
	 */
	private VectorTable getVectorTableFromList(List<IBOUB_SWT_NCCCODES> list) {
		VectorTable vectorTable = new VectorTable();

		for (IBOUB_SWT_NCCCODES listNccCodes : list) {
			vectorTable.addAll(new VectorTable(new VectorTable(listNccCodes
					.getDataMap()).getRowTags(0)));
		}
		return vectorTable;
	}

	// Method to check whether string is empty or not.
	/**
	 * @param str
	 * @return true/false
	 */
	private boolean isStringEmpty(String str) {
		if ((str == null) || (str.length() == CommonConstants.INTEGER_ZERO)) {
			return true;
		} else {
			return false;
		}
	}
}
