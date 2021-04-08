package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.common.MISTransactionCodeDetails;
import com.misys.ub.common.events.CommonsEventCodes;
import com.misys.ub.systeminformation.IBusinessInformation;
import com.misys.ub.systeminformation.IBusinessInformationService;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOTransactionScreenControl;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.steps.refimpl.AbstractIBSNetConfigValidationFatom;

public class IBSNetConfigValidationFatom extends AbstractIBSNetConfigValidationFatom {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    public BankFusionEnvironment env;

    private transient final static Log logger = LogFactory.getLog(IBSNetConfigValidationFatom.class.getName());

    public IBSNetConfigValidationFatom(BankFusionEnvironment env) {
        super(env);
    }

    public void process(BankFusionEnvironment environment) {

        // validateInputTags(environment);
        validityOftags(environment);
    }

    private void displayMessifError(ArrayList list, int number, Object[] value) {
        // TODO Auto-generated method stub
        if (list == null || list.isEmpty()) {
            throw new BankFusionException(number, value, logger, env);
        }
    }

    private void validateInputTags(BankFusionEnvironment env) {
        Map inputTags = getInDataMap();
        Iterator iterator = inputTags.keySet().iterator();
        String key = null;
        String value = null;
        while (iterator.hasNext()) {
            key = (String) iterator.next();
            value = inputTags.get(key).toString();
            if (value instanceof String && ((String) value).equals(CommonConstants.EMPTY_STRING)) {
                /* throw new BankFusionException(123, new String[] { key, value }, logger, env); */
                EventsHelper.handleEvent(CommonsEventCodes.E_THE_INPUT_TAG_HAS_AN_INVALID_VALUE, new Object[] { key, value },
                        new HashMap(), env);
            }

        }
    }

    private void validityOftags(BankFusionEnvironment environment) {
        String accountNumber = CommonConstants.EMPTY_STRING;
        IBOAttributeCollectionFeature accountValues = null;
        String whereClause = null;
        String branchSortCode = getF_IN_branchCode();
        whereClause = "where " + IBOBranch.BRANCHSORTCODE + " = ? ";
        ArrayList params = new ArrayList();
        ArrayList list = new ArrayList();
        params.add(branchSortCode);
        list = (ArrayList) environment.getFactory().findByQuery(IBOBranch.BONAME, whereClause, params, null, true);

        if (list == null) {
            displayMessifError(list, 9305, new String[] { branchSortCode });
        }

        list.clear();
        String transactionCode = getF_IN_transactionCode();
        // Using the Cache of TransactionScreenControl Table for fetching the details.
        try {
            MISTransactionCodeDetails mistransDetails;
            IBusinessInformationService ubInformationService = (IBusinessInformationService) ServiceManagerFactory.getInstance()
                    .getServiceManager().getServiceForName(IBusinessInformationService.BUSINESS_INFORMATION_SERVICE);
            mistransDetails = ((IBusinessInformation) ubInformationService.getBizInfo())
                    .getMisTransactionCodeDetails(transactionCode);

            IBOTransactionScreenControl screenControlBO = mistransDetails.getTransactionScreenControl();
        }
        catch (BankFusionException ex) {
            displayMessifError(list, 9306, new String[] { branchSortCode });
            logger.error(ex);
        }
    }

    /*
     * whereClause = "where " + IBOAccount.PSEUDONAME + " like ? and " + IBOAccount.BRANCHSORTCODE +
     * " = ?";
     * 
     * params.add(creditSuspensePseudoCode + "%"); params.add(branchSortCode); list = (ArrayList)
     * environment.getFactory().findByQuery(IBOAccount.BONAME, whereClause, params, null, true);
     * 
     * if (list == null) { displayMessifError(list, 9307, new String[] { branchSortCode }); }
     * 
     * params.clear(); list.clear(); String debitSuspensePseudoCode =
     * getF_IN_debitSuspensePseudoCode(); whereClause = "where " + IBOAccount.PSEUDONAME +
     * " like ? and " + IBOAccount.BRANCHSORTCODE + " = ?";
     * 
     * params.add(debitSuspensePseudoCode + "%"); params.add(branchSortCode); list = (ArrayList)
     * environment.getFactory().findByQuery(IBOAccount.BONAME, whereClause, params, null, true); if
     * (list == null) { displayMessifError(list, 9308, new String[] { branchSortCode }); }
     */

    /*
     * if (key.equalsIgnoreCase("country")) { whereClause = "where " + IBOCountry.COUNTRYID +
     * " = ? "; ArrayList params = new ArrayList(); ArrayList list = new ArrayList();
     * 
     * try { value = inputTags.get(key); params.add(value); list = (ArrayList)
     * environment.getFactory().findByQuery(IBOCountry.BONAME,whereClause, params,null);
     * //displayMessifError(list, key, value); throw new BankFusionException(123, new String[] {
     * key, value }, logger, env); } catch (BODefinitionException d) { displayMessifError(list, key,
     * value); } catch (BankFusionException b) { displayMessifError(list, key, value); } }
     * 
     * if (key.equalsIgnoreCase("isoIMd")) { whereClause = "where " + IBOBranch.BRANCHSORTCODE +
     * " = ? "+IBOBranch.IMDCODE + " = ? "; ArrayList params1 = new ArrayList(); ArrayList list =
     * new ArrayList(); try { value = inputTags.get(key); branchSortCode = value.toString();
     * params1.add(branchSortCode); params1.add(value); Iterator sourceIt =
     * environment.getFactory().findByQuery(IBOBranch.BONAME, whereClause, params1, null)
     * .iterator(); if (sourceIt.hasNext()) { IBOBranch branch = (IBOBranch) sourceIt.next(); if
     * (branch == null){ displayMessifError(list, key, value); } } } catch (BODefinitionException d)
     * { displayMessifError(list, key, value); } catch (BankFusionException b) {
     * displayMessifError(list, key, value); } }
     */
}
