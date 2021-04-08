/* ********************************************************************************
 *  Copyright (c) 2002,2004 Trapedza Financial Systems Ltd. All Rights Reserved.
 *
 *  This software is the proprietary information of Trapedza Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 *
 * $Id: BeneficiaryNamePercentageFatom.java,v 1.4 2011/08/10 20:13:30 mdwivedi Exp $
 *
 * 
 * ------------------------------------------------------------
 */
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.bo.refimpl.IBOAccountCustomers;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.bo.refimpl.IBOOrgDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOPersonDetails;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.FinderMethods;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_BeneficiaryNamePercentageFatom;
import com.trapedza.bankfusion.steps.refimpl.IUB_SWT_BeneficiaryNamePercentageFatom;

/**
 * BeneficiaryNamePercentageFatom is responsible for comparing 2 strings and return their matched
 * percentage Note - it is specific for BCB CR, it is not optimized,it is just the replica of the
 * logic used in BM+
 * 
 * @author mdwivedi
 * 
 */
public class BeneficiaryNamePercentageFatom extends AbstractUB_SWT_BeneficiaryNamePercentageFatom
        implements IUB_SWT_BeneficiaryNamePercentageFatom {

    private transient final static Log logger = LogFactory.getLog(UB_CNF_AccountStatementFatom.class.getName());

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    private IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
    private static String fetchJointCustomerQuery = " WHERE " + IBOAccountCustomers.ACCOUNTID + " = ? ";

    BankFusionEnvironment env;
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    @SuppressWarnings("deprecation")
    public BeneficiaryNamePercentageFatom(BankFusionEnvironment env) {
        super(env);
    }

    /**
     * Updating user access restriction policy
     */
    @SuppressWarnings("unchecked")
    public void process(BankFusionEnvironment env) throws BankFusionException {
        this.env = env;
        boolean expectedMatchFlag = false;
        // String sFullName = getF_IN_customerNameInDb();
        String sNameToTest = getF_IN_beneficiaryName();
        if (sNameToTest != null && sNameToTest.contains("/")) {
            sNameToTest = sNameToTest.substring(2, sNameToTest.length());
        }
        String accountNo = getF_IN_accountNo();
        BigDecimal configuredPercentage = getF_IN_configuredPercentage();

        List jointCustomerList = getCustomerNameInDB(accountNo);

        int percentage = 0;
        for (int i = 0; i < jointCustomerList.size(); i++) {
            // Call the function to get the string comparison percentage
            percentage = compareNames((String) jointCustomerList.get(i), sNameToTest, false);
            if (percentage >= configuredPercentage.intValue()) {
                expectedMatchFlag = true;
                break;
            }
            if (logger.isInfoEnabled()) {
                logger.info("**BeneficiaryNamePercentageFatom - calculating string matching percentage***" + accountNo + " "
                        + sNameToTest + " " + (String) jointCustomerList.get(i) + " " + percentage);
            }
        }
        BigDecimal perc = new BigDecimal(percentage);

        setF_OUT_percentage(perc);
        setF_OUT_matchedFlag(expectedMatchFlag);
    }

    /**
     * Get all the customers associated for the given account no
     * 
     * @param accountNo
     * @return
     */
    @SuppressWarnings({ "unchecked" })
    private List getCustomerNameInDB(String accountNo) {

        IBOAccountCustomers jointAccountDetails = null;
        List jointCustomerList = null;
        List customerNameList = new ArrayList();

        // Create parameters for query
        ArrayList queryParams = new ArrayList();
        queryParams.add(accountNo);

        // Find customers for the given account
        jointCustomerList = factory.findByQuery(IBOAccountCustomers.BONAME, fetchJointCustomerQuery, queryParams, null, false);

        // Iterate through all the customers
        for (int i = 0; i < jointCustomerList.size(); i++) {
            jointAccountDetails = (IBOAccountCustomers) jointCustomerList.get(i);
            String customerCode = jointAccountDetails.getF_CUSTOMERCODE();

            // Get customer details for the associated account
            IBOCustomer custDetails = ((IBOCustomer) FinderMethods.findCustomerDetailsbyCustomerCode(customerCode, env, null)
                    .get(0));
            if (custDetails != null) {

                // If customer type is personal customer then get the customer name from
                // persondetails table
                // concatenating title, first name, middle name and surname
                String customerType = custDetails.getF_CUSTOMERTYPE();
                if (customerType.equals(IValidateCustomer.INDIVIDUAL_CUSTOMER_TYPE)) {
                    IBOPersonDetails personDetails = (IBOPersonDetails) factory.findByPrimaryKey(IBOPersonDetails.BONAME,
                            customerCode, false);
                    if (personDetails != null) {
                        String custName = "";
                        String forename = personDetails.getF_FORENAME();
                        String middleName = personDetails.getF_MIDDLENAME();
                        String surname = personDetails.getF_SURNAME();
                        // Append title if title exists
                        /*
                         * if (titleCode != null && titleCode.length() != 0) { String title =
                         * getCodeTypeDescription(personDetails.getF_TITLE()); custName = title; }
                         */
                        // Append forename if forename exists
                        if (forename != null && forename.length() != 0) {
                            custName = forename;
                        }
                        // Append middleName if middleName exists
                        if (middleName != null && middleName.length() != 0) {
                            String data = custName + " " + middleName;
                            custName = data;
                        }
                        // Append surname if surname exists
                        if (surname != null && surname.length() != 0) {
                            String data = custName + " " + surname;
                            custName = data;
                        }
                        customerNameList.add(custName);
                    }
                } // If customer type is enterprise customer then get the customer name from
                  // orgdetails table
                else if (customerType.equals(IValidateCustomer.ENTERPRISE_CUSTOMER_TYPE)) {
                    IBOOrgDetails orgDetails = (IBOOrgDetails) factory.findByPrimaryKey(IBOOrgDetails.BONAME, customerCode, false);
                    String orgName = orgDetails.getF_ORGNAME();
                    customerNameList.add(orgName);
                }
            }
            logger.debug("Customer name " + customerNameList);
        }
        return customerNameList;
    }

    /**
     * This method fetches all the generic codes for the Titles.
     * 
     */

    /*
     * private void getGenericCodeTypes() { ArrayList params = new ArrayList();
     * List<SimplePersistentObject> genericCodeTypes; int localeId =
     * GetLocaleID.run(BankFusionThreadLocal.getUserSession() .getUserLocale().toString(), "Y");
     * 
     * params.add(CODETYPE); params.add(localeId); genericCodeTypes = (List<SimplePersistentObject>)
     * factory.findByQuery( IBOCODETYPES.BONAME, getTitleFromGenericCodeWhereClause, params, null);
     * }
     */

    /**
     * This method returns the description string value based on the deal status value passed.
     * 
     * @param subCodeType
     *            - This is the Deal Status value
     * @return Description String Object
     */

    /*
     * private String getCodeTypeDescription(String subCodeType) {
     * 
     * String description = ""; Iterator itr1 = genericCodeTypes.iterator(); while (itr1.hasNext())
     * { IBOCODETYPES types = (IBOCODETYPES) itr1.next(); if
     * (types.getF_SUBCODETYPE().equalsIgnoreCase(subCodeType)) { description =
     * types.getF_DESCRIPTION(); } } return description; }
     */
    /**
     * StrSimilar will compare 2 strings and give u the % of matching
     * 
     * @param s1
     * @param s2
     * @return
     */
    private static int getPercentageMatch(String s1, String s2) {

        // test array to hold boolean values

        // calculated percentage
        int result = 0;
        boolean flag = false;

        // Both are empty then 100%. If one of them is empty 0%
        if ((s1 == null || s1.trim().length() == 0) && (s2 == null || s2.trim().length() == 0)) {
            result = 100; // Both empty
            flag = true;
        }
        else if ((s1 == null || s1.trim().length() == 0) || (s2 == null || s2.trim().length() == 0)) {
            result = 0; // Only s1 is empty
            flag = true;
        }

        // Check if to return result or not, if one of them empty or both empty
        if (flag) {
            return result;
        }

        String hstr;
        // Test Length and swap, if s1 is smaller,
        // searching along the longer string
        if (s1.length() < s2.length()) {
            hstr = s2;
            s2 = s1;
            s1 = hstr;
        }
        boolean[] test = new boolean[511];
        int p1, p2, pt;
        int hit;
        int l1, l2;
        int diff;
        // Store length of strings to speed up the function
        l1 = s1.length();
        l2 = s2.length();
        p1 = 0;
        p2 = 0;
        hit = 0;

        // Calc the unsharp factor depending on the length
        // of the strings. Its about a third of the length
        diff = Math.max(l1, l2) / 3 + Math.abs(l1 - l2);
        // init the test array
        for (pt = 0; pt < l1; pt++) {
            test[pt] = false;
        }

        // loop through the string
        do {
            // position tested?
            if (!test[p1]) {
                // found a matching character?
                if ((s1.charAt(p1) == s2.charAt(p2)) && (Math.abs(p1 - p2) <= diff)) {
                    test[p1] = true;
                    hit++; // increment the hit count
                    // next positions
                    p1++;
                    p2++;
                    if (p1 > l1 - 1) {
                        p1 = 0;
                    }
                }
                else {
                    // Set test array
                    test[p1] = false;
                    p1++;
                    // Loop back to next test position if end of the string
                    if (p1 > l1 - 1) {
                        while ((p1 > 0) && !(test[p1])) {
                            p1--;
                        }
                        p2++;
                    }
                }
            }
            else {
                p1++;
                // Loop back to next test position if end of string
                boolean condition = false;
                if (p1 > l1 - 1) {
                    do {
                        p1--;
                        condition = (p1 == 0 || test[p1]);
                    }
                    while (!condition);
                    p2++;
                }
            }
            if (p2 > (s2.length() - 1)) {
                break;
            }
        }
        while (true);
        // calc procentual value
        result = 100 * hit / l1;
        return result;
    }

    /**
     * CompareNames will call StrSimilar to get the percentage for surname and fullname and will
     * return final percentage
     * 
     * @param sNameToTest
     * @param sFullName
     * @param bCaseSensitive
     * @return
     */
    private static int compareNames(String sNameToTest, String sFullName, boolean bCaseSensitive) {

        int iResult;

        if (sFullName == null || sNameToTest == null) {
            iResult = 0;
            return iResult;
        }
        String surname1, surname2;
        int iFullNameResult, iSurnameResult;
        surname1 = getSurname(sFullName);
        surname2 = getSurname(sNameToTest);

        // Convert to uppercase in no case sensitivity
        if (!bCaseSensitive) {
            sFullName = sFullName.toUpperCase();
            sNameToTest = sNameToTest.toUpperCase();
            surname1 = surname1.toUpperCase();
            surname2 = surname2.toUpperCase();
        }

        // Get the percentage match - Percentage calculation
        iFullNameResult = getPercentageMatch(sFullName, sNameToTest);
        iSurnameResult = getPercentageMatch(surname1, surname2);
        if (iFullNameResult + iSurnameResult == 0) {
            iResult = 0;
        }
        else {
            iResult = (iFullNameResult + iSurnameResult) / 2;
        }
        // Return percentage match
        return iResult;
    }

    /**
     * Function to extract surname from fullName
     * 
     * @param fullName
     * @return
     */
    private static String getSurname(String fullName) {
        int iPosDot = 0, iPosSpace = 0, iPosComma = 0;
        String surname = null;
        /* Check if name has space or dot in between */
        iPosSpace = fullName.lastIndexOf(' ');
        iPosDot = fullName.lastIndexOf('.');

        if (iPosDot == fullName.length() - 1) {
            iPosDot = 0;
            // If last character is a full-stop then it cannot be used to
            // seperate First name /
            // surname
            // Check to see if space pos. is also invalid
            if (iPosSpace == fullName.length() - 1) {
                iPosSpace = 0;
            }
        }

        iPosComma = fullName.lastIndexOf(',');
        // Note : substring will exclude end index value
        if (iPosComma > 0) {
            // If name contains a comma then assume surname is before the comma
            surname = fullName.substring(0, iPosComma);
        }
        else {
            if ((iPosSpace == 0) && (iPosDot == 0)) {
                surname = fullName;
            }
            else {
                // Determine which is latter the space or dot
                if (iPosSpace > iPosDot) {
                    surname = fullName.substring(iPosSpace + 1, fullName.length());
                }
                else {
                    surname = fullName.substring(iPosDot + 1, fullName.length());
                }
            }
        }
        return surname;
    }
}
