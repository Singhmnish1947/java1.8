/* ***********************************************************************************
 * Copyright (c) 2002,2004 Trapedza Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Trapedza Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 * $Id: BPW_AlphaSearch.java,v 1.4.4.3 2008/07/25 23:25:46 deepac Exp $
 */

package com.trapedza.bankfusion.fatoms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.bo.refimpl.IBOAddress;
import com.trapedza.bankfusion.bo.refimpl.IBOAddressLinks;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_BPW_AlphaSearchString;
import com.trapedza.bankfusion.core.VectorTable;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractBPW_AlphaSearch;
import com.trapedza.bankfusion.steps.refimpl.IBPW_AlphaSearch;

public class BPW_AlphaSearch extends AbstractBPW_AlphaSearch implements IBPW_AlphaSearch {

    private transient final static Log logger = LogFactory.getLog(BPW_AlphaSearch.class.getName());

    private IPersistenceObjectsFactory factoryObj = BankFusionThreadLocal.getPersistanceFactory();
    private static int PAGE_SIZE = 10;
    // Changes start for artf124723
    public static final String INTERNAL = "I";
    public static final String INTERNAL_ALPHACODE = "INT";
    // Changes ends for artf124723

    private static final String ALPHA_SEARCH_QUERY = "SELECT * FROM (SELECT CUSTOMERCODE, SHORTNAME, ALPHACODE, BRANCHSORTCODE, "
            + "ROW_NUMBER() OVER (ORDER BY ALPHACODE) AS SEQ FROM CUSTOMER WHERE ALPHACODE LIKE ?) TMP WHERE TMP.SEQ BETWEEN ? AND ? ";
    private transient final static Log LOGGER = LogFactory.getLog(BPW_AlphaSearch.class.getName());

    public BPW_AlphaSearch(BankFusionEnvironment env) {
        super(env);
    }

    public void process(BankFusionEnvironment env) {
        String customerNumber = getF_IN_CustomerNumber();
        String alphaCode = getF_IN_SearchString();
        List custListTemp = new ArrayList();
        int toIndex, fromIndex;
        int nxtRprv = getF_IN_nxtORprv().intValue();
        String bpwUserID = getF_IN_userId();
        HashMap customerMap = new HashMap();
        VectorTable customerVector = getF_OUT_Result();
        Iterator<IBOCustomer> it = null;
        int pageNumber;

        // TODO factoryObj.bulkDelete(arg0, arg1)

        if (alphaCode != null) {
            ArrayList params;
            // Changes start for artf124723
            if (alphaCode.startsWith(INTERNAL_ALPHACODE)) {
                if (nxtRprv == 0 || nxtRprv == 2) {
                    params = new ArrayList();
                    String alpha = alphaCode.substring(0, 3);
                    params.add(alpha);
                }
                else {
                    params = new ArrayList();
                    String alpha = alphaCode.substring(0, 3);
                    params.add(alpha);
                    params.add(customerNumber);
                }

            }
            else {
                // Changes end for artf124723
                if (nxtRprv == 0 || nxtRprv == 2) {
                    createRecord(1, bpwUserID, alphaCode);
                    it = runQuery(1, alphaCode);
                }
                else {
                    IBOUB_BPW_AlphaSearchString alphaSearchRec = getExistingRecord(bpwUserID);
                    if (alphaSearchRec != null) {
                        String originalAlphaCode = alphaSearchRec.getF_UBALPHASEARCHSTRING();
                        pageNumber = alphaSearchRec.getF_UBPAGENUM() + 1;
                        it = runQuery(pageNumber, originalAlphaCode);
                        alphaSearchRec.setF_UBALPHASEARCHSTRING(originalAlphaCode);
                        alphaSearchRec.setF_UBPAGENUM(pageNumber);
                    }
                    else {
                        // This will never get called
                        createRecord(1, bpwUserID, alphaCode);
                    }
                }

                if (null != it) {
                    int counter = 0;
                    while (it.hasNext()) {
                        counter++;
                        if (counter > 10) {
                            break;
                        }
                        custListTemp.add(it.next());
                    }
                }
                if (custListTemp.size() != 0) {

                    if (custListTemp.size() > 9)
                        toIndex = 9;
                    else toIndex = custListTemp.size() - 1;

                    if (nxtRprv == 0 || nxtRprv == 2) {
                        int noRows = 0;
                        for (int i = 0; i <= toIndex; i++) {

                            IBOCustomer cust = (IBOCustomer) custListTemp.get(i);

                            if (cust.getF_CUSTOMERTYPE().equalsIgnoreCase(INTERNAL)) {
                                customerMap.put("ADDRESS1", "");
                            }
                            else {
                                // Getting AddressID from AddressLinks Table for this Customer
                                String whereAddLinkClause = "where " + IBOAddressLinks.CUSTACC_KEY + " =?";
                                ArrayList paramsAddLink = new ArrayList();
                                paramsAddLink.add(cust.getBoID());
                                IBOAddressLinks addLinks = (IBOAddressLinks) env.getFactory().findFirstByQuery(
                                        IBOAddressLinks.BONAME, whereAddLinkClause, paramsAddLink);

                                // Getting AddressLine1 from Address Table for this Customer
                                if (addLinks != null) {
                                    String whereAddClause = "where " + IBOAddress.ADDRESSID + " =?";
                                    ArrayList paramsAdd = new ArrayList();
                                    paramsAdd.add(addLinks.getF_ADDRESSID());

                                    IBOAddress address = (IBOAddress) env.getFactory().findFirstByQuery(IBOAddress.BONAME,
                                            whereAddClause, paramsAdd);

                                    customerMap.put("ADDRESS1", address.getF_ADDRESSLINE1());
                                }
                                else {
                                    customerMap.put("ADDRESS1", "");
                                }
                            }
                            customerMap.put("BOTYPEID", cust.getBoID());
                            customerMap.put("BOID", cust.getBoID());
                            if (cust.getF_ALPHACODE().length() > 15) {
                                customerMap.put("ALPHACODE", cust.getF_ALPHACODE().substring(0, 15));
                            }
                            else {
                                customerMap.put("ALPHACODE", cust.getF_ALPHACODE());
                            }
                            customerMap.put("BRANCHSORTCODE", "00" + cust.getF_BRANCHSORTCODE());
                            customerMap.put("CUSTOMERCODE", cust.getBoID());
                            customerMap.put("SHORTNAME", cust.getF_SHORTNAME());
                            customerVector.addAll(new VectorTable(customerMap));
                            noRows++;
                        }
                        setF_OUT_NUMOFROWS(new Integer(noRows));
                    }

                    if (nxtRprv == 1) {
                        int icount = 0;
                        int noRows = 0;
                        if (custListTemp.size() > 9)
                            fromIndex = 10;
                        else fromIndex = custListTemp.size();

                        while (icount < fromIndex) {

                            IBOCustomer cust = (IBOCustomer) custListTemp.get(icount);
                            if (cust.getF_CUSTOMERTYPE().equalsIgnoreCase(INTERNAL)) {
                                customerMap.put("ADDRESS1", "");
                            }
                            else {
                                // Getting AddressID from AddressLinks Table for this Customer
                                String whereAddLinkClause = "where " + IBOAddressLinks.CUSTACC_KEY + " =?";
                                ArrayList paramsAddLink = new ArrayList();
                                paramsAddLink.add(cust.getBoID());
                                IBOAddressLinks addLinks = (IBOAddressLinks) env.getFactory().findFirstByQuery(
                                        IBOAddressLinks.BONAME, whereAddLinkClause, paramsAddLink);
                                if (addLinks != null) {
                                    // Getting AddressLine1 from Address Table for this Customer
                                    String whereAddClause = "where " + IBOAddress.ADDRESSID + " =?";
                                    ArrayList paramsAdd = new ArrayList();
                                    paramsAdd.add(addLinks.getF_ADDRESSID());

                                    IBOAddress address = (IBOAddress) env.getFactory().findFirstByQuery(IBOAddress.BONAME,
                                            whereAddClause, paramsAdd);

                                    customerMap.put("ADDRESS1", address.getF_ADDRESSLINE1());
                                }
                                else {
                                    customerMap.put("ADDRESS1", "");
                                }
                            }
                            customerMap.put("BOTYPEID", cust.getBoID());
                            customerMap.put("BOID", cust.getBoID());
                            if (cust.getF_ALPHACODE().length() > 15) {
                                customerMap.put("ALPHACODE", cust.getF_ALPHACODE().substring(0, 15));
                            }
                            else {
                                customerMap.put("ALPHACODE", cust.getF_ALPHACODE());
                            }
                            customerMap.put("BRANCHSORTCODE", "00" + cust.getF_BRANCHSORTCODE());
                            customerMap.put("CUSTOMERCODE", cust.getBoID());
                            customerMap.put("SHORTNAME", cust.getF_SHORTNAME());
                            customerVector.addAll(new VectorTable(customerMap));
                            noRows++;
                            icount++;

                        }
                        setF_OUT_NUMOFROWS(new Integer(noRows));
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private Iterator runQuery(int pageNumber, String alphaCode) {

        Connection con = BankFusionThreadLocal.getPersistanceFactory().getJDBCConnection();
        List<IBOCustomer> resultList = new ArrayList<IBOCustomer>();
        IBOCustomer valObj = null;
        int fromValue = ((pageNumber - 1) * PAGE_SIZE) + 1;
        int toValue = pageNumber * PAGE_SIZE;
        PreparedStatement ps1 = null;
        ResultSet rs = null;
        try {
            ps1 = con.prepareStatement(ALPHA_SEARCH_QUERY);
            ps1.setString(1, alphaCode);
            ps1.setInt(2, fromValue);
            ps1.setInt(3, toValue);
            rs = ps1.executeQuery();
            while (rs.next()) {
                valObj = (IBOCustomer) factoryObj.getStatelessNewInstance(IBOCustomer.BONAME);
                valObj.setBoID(rs.getString("CUSTOMERCODE"));
                valObj.setF_SHORTNAME(rs.getString("SHORTNAME"));
                if (rs.getString("ALPHACODE").length() > 15) {
                    rs.getString("ALPHACODE").substring(0, 15);
                }
                else {
                    valObj.setF_ALPHACODE(rs.getString("ALPHACODE"));
                }
                valObj.setF_BRANCHSORTCODE(rs.getString("BRANCHSORTCODE"));
                resultList.add(valObj);
            }
        }
        catch (Exception e) {
            logger.error(e);
        }
        finally {
            valObj = null;
            try {
                if (ps1 != null)
                    ps1.close();
            }
            catch (SQLException e) {
                LOGGER.error("Error while closing the prepared statement : ", e);
            }
            try {
                if (rs != null)
                    rs.close();
            }
            catch (SQLException e) {
                LOGGER.error("Error while closing the result set : ", e);
            }
        }
        Iterator<IBOCustomer> it = resultList.iterator();
        return it;
    }

    private void createRecord(int pageNumber, String bpwUserID, String alphaCode) {
        IBOUB_BPW_AlphaSearchString bpwAlphaSearchString = getExistingRecord(bpwUserID);
        if (bpwAlphaSearchString == null) {
            bpwAlphaSearchString = (IBOUB_BPW_AlphaSearchString) ((IPersistenceObjectsFactory) factoryObj)
                    .getStatelessNewInstance(IBOUB_BPW_AlphaSearchString.BONAME);
            bpwAlphaSearchString.setBoID(bpwUserID);
            bpwAlphaSearchString.setF_UBALPHASEARCHSTRING(alphaCode);
            bpwAlphaSearchString.setF_UBPAGENUM(pageNumber);
            ((IPersistenceObjectsFactory) factoryObj).create(IBOUB_BPW_AlphaSearchString.BONAME, bpwAlphaSearchString);
        }
        else {
            bpwAlphaSearchString.setF_UBALPHASEARCHSTRING(alphaCode);
            bpwAlphaSearchString.setF_UBPAGENUM(pageNumber);
        }
    }

    private IBOUB_BPW_AlphaSearchString getExistingRecord(String bpwUserID) {
        @SuppressWarnings("unused")
        IBOUB_BPW_AlphaSearchString bpwAlphaSearchString;
        ArrayList params = new ArrayList();
        params.add(bpwUserID);
        return bpwAlphaSearchString = (IBOUB_BPW_AlphaSearchString) factoryObj.findByPrimaryKey(IBOUB_BPW_AlphaSearchString.BONAME,
                bpwUserID, true);
    }
}
