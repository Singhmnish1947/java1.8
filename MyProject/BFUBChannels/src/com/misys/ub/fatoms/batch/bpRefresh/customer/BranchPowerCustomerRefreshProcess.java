/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: BranchPowerCustomerRefreshProcess.java,v.1.1.2.3,Aug 21, 2008 2:58:06 PM KrishnanRR
 *
 * $Log: BranchPowerCustomerRefreshProcess.java,v $
 * Revision 1.1.2.5  2008/08/25 23:06:46  krishnanr
 * Branch Power Refresh Changes
 *
 * Revision 1.1.2.4  2008/08/22 00:26:19  krishnanr
 * Updated CVS Header Log
 *
 *
 */
package com.misys.ub.fatoms.batch.bpRefresh.customer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.common.events.CommonsEventCodes;
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.process.AbstractBatchProcess;
import com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator;
import com.trapedza.bankfusion.batch.process.BatchProcessException;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOUBTB_BPWCUSTREFRESHTAG;
import com.trapedza.bankfusion.boundary.outward.BankFusionIOSupport;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.gateway.persistence.interfaces.IPagingData;
import com.trapedza.bankfusion.persistence.core.PagingData;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;

public class BranchPowerCustomerRefreshProcess extends AbstractBatchProcess {
    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $"; //$NON-NLS-1$
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private static final Log logger = LogFactory.getLog(BranchPowerCustomerRefreshProcess.class.getName());

    private int branchCode = 0;
    private int fromBranchCode = 0;
    private int toBranchCode = 0;
    private int pageSize = 0;
    private String extractPath = CommonConstants.EMPTY_STRING;
    private String branchSortCode = CommonConstants.EMPTY_STRING;
    private String branch = CommonConstants.EMPTY_STRING;
    private String hdrActionFlag = CommonConstants.EMPTY_STRING;
    String strBusinessDate = null;
    // artf638548-To maximize I/O performance, BufferedOutPutStream is introduced.
    private BufferedOutputStream fout = null;
    StringBuffer fileData = new StringBuffer();

    private ArrayList params = new ArrayList();
    Date businessDate = null;

    private BankFusionEnvironment env = null;
    private AbstractProcessAccumulator accumulator;
    
    private int accPageSize = 1000;
    int totalCount = 0;
    IPagingData pageData = null;
    Properties fileProp;
    Boolean Status;
    
    private static Map<String, String> bpRefreshPropertiesMap = new HashMap<String, String>();
    
    private static Map<String, String> refreshPropertiesMap = new HashMap<String, String>();

    // String queryCustomer ="Select * from VW_CUSTOMERDTL where BRANCHSORTCODE=?";
    // String queryCustomer = " WHERE " + IBOVW_CUSTOMERDTL.BRANCHSORTCODE + " = ? ";
    String queryCustomer = CommonConstants.SELECT + CommonConstants.SPACE + "CUSTDTL."
					    + IBOUBTB_BPWCUSTREFRESHTAG.UBCUSTOMERCODE + " AS " + IBOUBTB_BPWCUSTREFRESHTAG.UBCUSTOMERCODE + ", CUSTDTL."
					    + IBOUBTB_BPWCUSTREFRESHTAG.UBSHORTNAME + " AS " + IBOUBTB_BPWCUSTREFRESHTAG.UBSHORTNAME + ", CUSTDTL."
					    + IBOUBTB_BPWCUSTREFRESHTAG.UBADDRESSLINE1 + " AS " + IBOUBTB_BPWCUSTREFRESHTAG.UBADDRESSLINE1 + ", CUSTDTL."
					    + IBOUBTB_BPWCUSTREFRESHTAG.UBADDRESSLINE2 + " AS " + IBOUBTB_BPWCUSTREFRESHTAG.UBADDRESSLINE2 + ", CUSTDTL."
					    + IBOUBTB_BPWCUSTREFRESHTAG.UBADDRESSLINE3 + " AS " + IBOUBTB_BPWCUSTREFRESHTAG.UBADDRESSLINE3 + ", CUSTDTL."
					    + IBOUBTB_BPWCUSTREFRESHTAG.UBADDRESSLINE4 + " AS " + IBOUBTB_BPWCUSTREFRESHTAG.UBADDRESSLINE4 + ", CUSTDTL."
					    + IBOUBTB_BPWCUSTREFRESHTAG.UBADDRESSLINE5 + " AS " + IBOUBTB_BPWCUSTREFRESHTAG.UBADDRESSLINE5 + ", CUSTDTL."
					    + IBOUBTB_BPWCUSTREFRESHTAG.UBADDRESSLINE6 + " AS " + IBOUBTB_BPWCUSTREFRESHTAG.UBADDRESSLINE6 + ", CUSTDTL."
					    + IBOUBTB_BPWCUSTREFRESHTAG.UBADDRESSLINE7 + " AS " + IBOUBTB_BPWCUSTREFRESHTAG.UBADDRESSLINE7 + ", CUSTDTL."
					    + IBOUBTB_BPWCUSTREFRESHTAG.UBALPHACODE + " AS " + IBOUBTB_BPWCUSTREFRESHTAG.UBALPHACODE + ", CUSTDTL."
					    + IBOUBTB_BPWCUSTREFRESHTAG.UBBRANCHSORTCODE + " AS " + IBOUBTB_BPWCUSTREFRESHTAG.UBBRANCHSORTCODE + ", CUSTDTL."
					    + IBOUBTB_BPWCUSTREFRESHTAG.UBLIMITINDICATOR + " AS " + IBOUBTB_BPWCUSTREFRESHTAG.UBLIMITINDICATOR + ", CUSTDTL."
					    + IBOUBTB_BPWCUSTREFRESHTAG.UBPOSTZIPCODE + " AS " + IBOUBTB_BPWCUSTREFRESHTAG.UBPOSTZIPCODE + ", CUSTDTL."
					    + IBOUBTB_BPWCUSTREFRESHTAG.UBREPORTINGCURRENCY + " AS " + IBOUBTB_BPWCUSTREFRESHTAG.UBREPORTINGCURRENCY + ", CUSTDTL."
					    + IBOUBTB_BPWCUSTREFRESHTAG.UBROWSEQ + " AS " + IBOUBTB_BPWCUSTREFRESHTAG.UBROWSEQ + " FROM "
					    + IBOUBTB_BPWCUSTREFRESHTAG.BONAME + " AS CUSTDTL WHERE " + IBOUBTB_BPWCUSTREFRESHTAG.UBBRANCHSORTCODE+ " = ? AND " 
					    + IBOUBTB_BPWCUSTREFRESHTAG.UBROWSEQ + " BETWEEN ? AND ? ORDER BY "+ IBOUBTB_BPWCUSTREFRESHTAG.UBCUSTOMERCODE;
					    
    private static final String branchSortCodeQuery = " WHERE " + IBOUBTB_BPWCUSTREFRESHTAG.UBBRANCHSORTCODE + "=? ";
    

    /**
     * @param environment
     *            Used to get a handle on the BankFusion environment
     * @param context
     *            A set of data passed to the PreProcess, Process and PostProcess classes
     * @param priority
     *            Thread priority
     */
    public BranchPowerCustomerRefreshProcess(BankFusionEnvironment environment, AbstractFatomContext context, Integer priority) {
        super(environment, context, priority);
        this.context = context;
        env = environment;
        Object[] additionalParameters = context.getAdditionalProcessParams();
        fileProp = (Properties) additionalParameters[0];
        bpRefreshPropertiesMap = (HashMap<String, String>) additionalParameters[2];
        fromBranchCode = Integer.parseInt(bpRefreshPropertiesMap.get("FROMBRANCH"));
        toBranchCode = Integer.parseInt(bpRefreshPropertiesMap.get("TOBRANCH"));
        hdrActionFlag = bpRefreshPropertiesMap.get("HDRACTIONFLAG");
        extractPath = bpRefreshPropertiesMap.get("EXTRACTPATH");
        if(null==extractPath)
        	extractPath = CommonConstants.EMPTY_STRING;
        String pageSize=bpRefreshPropertiesMap.get("PAGE-SIZE");
        if(null!=pageSize)
        	accPageSize = Integer.parseInt(pageSize);
        fileProp = (Properties) additionalParameters[1];
        refreshPropertiesMap = (HashMap<String, String>) additionalParameters[3];
    }

    /**
     * Initialise parameters and the accumulator for the BalanceSheetCollection process
     * 
     * @see com.trapedza.bankfusion.batch.process.AbstractBatchProcess#init()
     */
    public void init() {

        initialiseAccumulator();
    }

    /**
     * Gets a reference to the accumulator
     * 
     * @return A reference to the accumulator
     * @see com.trapedza.bankfusion.batch.process.AbstractBatchProcess#getAccumulator()
     */
    public AbstractProcessAccumulator getAccumulator() {
        return accumulator;
    }

    /**
     * @see com.trapedza.bankfusion.batch.process.AbstractBatchProcess#initialiseAccumulator()
     */
    protected void initialiseAccumulator() {
        Object[] accumulatorArgs = new Object[0];
        accumulator = new BranchPowerCustomerRefreshAccumulator(accumulatorArgs);
    }

    /**
     * Processes the branchpowerRefresh on the specified page, and accumulates the totals.
     * 
     * @param pageToProcess
     *            Page number of the page to be processed
     * @return The accumulator
     * @see com.trapedza.bankfusion.batch.process.AbstractBatchProcess#process(int)
     * @throws BatchProcessException
     *             Thrown if a BankFusionException occurs when processing the balance sheets and
     *             accumulating the totals, or if ServiceException or ErrorOnCommitException occur
     *             when commit or rolling back the transaction.
     */
    public AbstractProcessAccumulator process(int pageToProcess) throws IllegalArgumentException, BatchProcessException,
            BankFusionException {

        List list = null;
        IBOBranch branchBO = null;
        if(logger.isDebugEnabled())
        	logger.debug("Invoking Page: " + pageToProcess);
        pagingData.setCurrentPageNumber(pageToProcess);
        list = BankFusionThreadLocal.getPersistanceFactory().findAll(IBOBranch.BONAME, pagingData,true);
        if(null==list){
        	return accumulator;
        }
        Iterator branchIterator = list.iterator();
        while (branchIterator.hasNext()) {
        	int accTotalNoPages = CommonConstants.INTEGER_ZERO;
            branchBO = (IBOBranch) branchIterator.next();
            branchCode = Integer.parseInt(branchBO.getF_BMBRANCH());
            branch = branchBO.getF_BMBRANCH();
            params.clear();
            branchSortCode = branchBO.getBoID();
            params.add(branchSortCode);
            try {
                if (branchCode >= fromBranchCode && branchCode <= toBranchCode) {
                    businessDate = new SimpleDateFormat("yyyy-MM-dd").parse(SystemInformationManager.getInstance()
                            .getBFBusinessDate().toString());
                    strBusinessDate = new SimpleDateFormat("yyyyMMdd").format(businessDate);

                    //refreshCustomer(env);
                    pageData = new PagingData(1, pageSize);

                    accTotalNoPages = getTotalNumberOfPages(accPageSize, branchSortCode);
                    openRefreshFile(extractPath + "mcfc" + branchBO.getF_BMBRANCH().toString() + ".dat");
                    formatCustomerHeader(branchCode + "", fout, BankFusionThreadLocal.getBankFusionEnvironment());
                    totalCount = 0;
                    for (int i = 1; i <= accTotalNoPages; i++) {
                    	
                        // artf638548-passing the second parameter for pagination
                    	refreshCustomer(BankFusionThreadLocal.getBankFusionEnvironment(), i);
                    }
                    formatCustomerTrail(String.valueOf(totalCount), fout, BankFusionThreadLocal.getBankFusionEnvironment());
                    fout.close();
                    
                }
            }
            catch (BankFusionException e) {
                if (logger.isErrorEnabled()) {
                    logger.error(logException(e));
                }

            }
            catch (ParseException textException) {
                if (logger.isErrorEnabled()) {
                    logger.error(logException(textException));
                }

            }
            catch (IOException ioExp) {
                if (logger.isErrorEnabled()) {
                    logger.error(logException(ioExp));
                }

            }
        }
        return accumulator;
    }

    /**
     * performs the main customer refresh outputs branchwise data to mcfc9999.dat
     * 
     * @param env
     * @throws BankFusionException
     */
    private void refreshCustomer(BankFusionEnvironment env, int currentPage) throws BankFusionException {

        PagingData accPagingData = new PagingData(0, accPageSize);
        accPagingData.setCurrentPageNumber(currentPage);
        int fromValue = ((currentPage - 1) * accPageSize) + 1;
        int toValue = currentPage * accPageSize;

        ArrayList params = new ArrayList();
        params.add(branchSortCode);
        params.add(fromValue);
        params.add(toValue);
        String CustomerCode,ShortName,alphaCode,ReportingCurrency,AddressLine1,AddressLine2,
        		AddressLine3,AddressLine4,AddressLine5,AddressLine6,AddressLine7;
        String LimitIndicator;
        
        List customerDetails = null;
        SimplePersistentObject customerView = null;

        try {
        	customerDetails = BankFusionThreadLocal.getPersistanceFactory().executeGenericQuery(queryCustomer, params, null, true);
            if (customerDetails == null) {
                return;
            }

            Iterator customerIter = customerDetails.iterator();
            while (customerIter.hasNext()) {
                customerView = (SimplePersistentObject) customerIter.next();
                fileData = new StringBuffer();
                fileData.append(setField(new Integer(getProperty("FC-DTL-REC-TYPE")).intValue(), "02", 'A'));
                CustomerCode=customerView.getDataMap().get(IBOUBTB_BPWCUSTREFRESHTAG.UBCUSTOMERCODE).toString();
                if (null!=CustomerCode){
                	if(CustomerCode.length() > (new Integer(getProperty("FC-DTL-CLIENT-NUMBER")).intValue()))
                		fileData.append(setField(new Integer(getProperty("FC-DTL-CLIENT-NUMBER")).intValue(), CustomerCode.substring(0, 8), 'A'));
                	else fileData.append(setField(new Integer(getProperty("FC-DTL-CLIENT-NUMBER")).intValue(), (String) CustomerCode, 'A'));
                } else fileData.append(setField(new Integer(getProperty("FC-DTL-CLIENT-NUMBER")).intValue(), CommonConstants.EMPTY_STRING, 'A'));
                ShortName =customerView.getDataMap().get(IBOUBTB_BPWCUSTREFRESHTAG.UBSHORTNAME).toString();
                if(null!=ShortName){
                	if (ShortName.length() > (new Integer(getProperty("FC-DTL-SHORTNAME1")).intValue()))
                		fileData.append(setField(new Integer(getProperty("FC-DTL-SHORTNAME1")).intValue(), ShortName.substring(0, 29).trim(), 'A'));
                	else fileData.append(setField(new Integer(getProperty("FC-DTL-SHORTNAME1")).intValue(), (String) ShortName.toString().trim(), 'A'));
                } else fileData.append(setField(new Integer(getProperty("FC-DTL-SHORTNAME1")).intValue(), CommonConstants.EMPTY_STRING, 'A'));
                fileData.append(setField(new Integer(getProperty("FC-DTL-SHORTNAME2")).intValue(), "", 'A'));
                fileData.append(setField(new Integer(getProperty("FC-DTL-LOCATION")).intValue(), "", 'A'));
               
                alphaCode=customerView.getDataMap().get(IBOUBTB_BPWCUSTREFRESHTAG.UBALPHACODE).toString();
                if(null!=alphaCode){
                	if (alphaCode.length() > (new Integer(getProperty("FC-DTL-ALPHA-CODE")).intValue())) {
                		fileData.append(setField(new Integer(getProperty("FC-DTL-ALPHA-CODE")).intValue(), (String) alphaCode.substring(0, 9), 'A'));
                	}
                	else {
                		fileData.append(setField(new Integer(getProperty("FC-DTL-ALPHA-CODE")).intValue(), (String) alphaCode, 'A'));
                	}
                }else fileData.append(setField(new Integer(getProperty("FC-DTL-ALPHA-CODE")).intValue(), CommonConstants.EMPTY_STRING, 'A'));
                
                ReportingCurrency =customerView.getDataMap().get(IBOUBTB_BPWCUSTREFRESHTAG.UBREPORTINGCURRENCY).toString();
                if(null!=ReportingCurrency)
                	fileData.append(setField(new Integer(getProperty("FC-DTL-MAIN-CURR")).intValue(), (String)ReportingCurrency , 'A'));
                else fileData.append(setField(new Integer(getProperty("FC-DTL-MAIN-CURR")).intValue(), CommonConstants.EMPTY_STRING, 'A'));

                fileData.append(setField(new Integer(getProperty("FC-DTL-SEC-RATING")).intValue(), "", 'A'));
                fileData.append(setField(new Integer(getProperty("FC-DTL-CLOSED-IND")).intValue(), "", 'A'));
                fileData.append(setField(new Integer(getProperty("FC-DTL-GARN-ORDER")).intValue(), "", 'A'));

                /* Address lines reading from address table */
                AddressLine1=customerView.getDataMap().get(IBOUBTB_BPWCUSTREFRESHTAG.UBADDRESSLINE1).toString();
                if(null!=AddressLine1){
                	if (AddressLine1.length() > (new Integer(getProperty("FC-DTL-ADDRESS1")).intValue()))
                		fileData.append(setField(new Integer(getProperty("FC-DTL-ADDRESS1")).intValue(), AddressLine1.trim().substring(0, 44), 'A'));
                	else fileData.append(setField(new Integer(getProperty("FC-DTL-ADDRESS1")).intValue(), (String)AddressLine1.trim(), 'A'));
                }else fileData.append(setField(new Integer(getProperty("FC-DTL-ADDRESS1")).intValue(), CommonConstants.EMPTY_STRING, 'A')); 
                AddressLine2=customerView.getDataMap().get(IBOUBTB_BPWCUSTREFRESHTAG.UBADDRESSLINE2).toString();
                if(null!=AddressLine2){
                	if (AddressLine2.length() > (new Integer(getProperty("FC-DTL-ADDRESS2")).intValue()))
                		fileData.append(setField(new Integer(getProperty("FC-DTL-ADDRESS2")).intValue(), AddressLine2.substring(0, 44), 'A'));
                	else fileData.append(setField(new Integer(getProperty("FC-DTL-ADDRESS2")).intValue(), (String) AddressLine2.trim(), 'A'));
                }else fileData.append(setField(new Integer(getProperty("FC-DTL-ADDRESS2")).intValue(), CommonConstants.EMPTY_STRING, 'A'));
                AddressLine3 =customerView.getDataMap().get(IBOUBTB_BPWCUSTREFRESHTAG.UBADDRESSLINE3).toString();
                if(null!=AddressLine3){
                if (AddressLine3.length() > (new Integer(getProperty("FC-DTL-ADDRESS3")).intValue()))
                    fileData.append(setField(new Integer(getProperty("FC-DTL-ADDRESS3")).intValue(), AddressLine3.trim().substring(0, 44), 'A'));
                	else fileData.append(setField(new Integer(getProperty("FC-DTL-ADDRESS3")).intValue(), (String) AddressLine3.trim(), 'A'));
                }else fileData.append(setField(new Integer(getProperty("FC-DTL-ADDRESS3")).intValue(), CommonConstants.EMPTY_STRING, 'A'));
                AddressLine4 =customerView.getDataMap().get(IBOUBTB_BPWCUSTREFRESHTAG.UBADDRESSLINE4).toString();
                if(null!=AddressLine4){
                	if (AddressLine4.length() > (new Integer(getProperty("FC-DTL-ADDRESS4")).intValue()))
                		fileData.append(setField(new Integer(getProperty("FC-DTL-ADDRESS4")).intValue(), AddressLine4.trim().substring(0, 44), 'A'));
                    else fileData.append(setField(new Integer(getProperty("FC-DTL-ADDRESS4")).intValue(), (String) AddressLine4.trim(), 'A'));
                }else fileData.append(setField(new Integer(getProperty("FC-DTL-ADDRESS4")).intValue(), CommonConstants.EMPTY_STRING, 'A'));
                AddressLine5=customerView.getDataMap().get(IBOUBTB_BPWCUSTREFRESHTAG.UBADDRESSLINE5).toString();
                if(null!=AddressLine5){
                	if (AddressLine5.length() > (new Integer(getProperty("FC-DTL-ADDRESS5")).intValue()))
                		fileData.append(setField(new Integer(getProperty("FC-DTL-ADDRESS5")).intValue(),AddressLine5.trim().substring(0, 44), 'A'));
                	else fileData.append(setField(new Integer(getProperty("FC-DTL-ADDRESS5")).intValue(), (String) AddressLine5.trim(), 'A'));
                } else fileData.append(setField(new Integer(getProperty("FC-DTL-ADDRESS5")).intValue(), CommonConstants.EMPTY_STRING, 'A'));
                AddressLine6 =customerView.getDataMap().get(IBOUBTB_BPWCUSTREFRESHTAG.UBADDRESSLINE6).toString();
                if(null!=AddressLine6){
                	if (AddressLine6.length() > (new Integer(getProperty("FC-DTL-ADDRESS6")).intValue()))
                    fileData.append(setField(new Integer(getProperty("FC-DTL-ADDRESS6")).intValue(), AddressLine6.trim().substring(0, 44), 'A'));
                	else fileData.append(setField(new Integer(getProperty("FC-DTL-ADDRESS6")).intValue(), (String) AddressLine6.trim(), 'A'));
                }else fileData.append(setField(new Integer(getProperty("FC-DTL-ADDRESS6")).intValue(), CommonConstants.EMPTY_STRING, 'A'));
                AddressLine7 = customerView.getDataMap().get(IBOUBTB_BPWCUSTREFRESHTAG.UBADDRESSLINE7).toString();
                if(null!=AddressLine7){
                	if (AddressLine7.length() > (new Integer(getProperty("FC-DTL-ADDRESS7")).intValue()) )
                    fileData.append(setField(new Integer(getProperty("FC-DTL-ADDRESS7")).intValue(), AddressLine7.trim().substring(0, 44), 'A'));
                	else fileData.append(setField(new Integer(getProperty("FC-DTL-ADDRESS7")).intValue(), (String)AddressLine7.trim(), 'A'));
                }else fileData.append(setField(new Integer(getProperty("FC-DTL-ADDRESS7")).intValue(), CommonConstants.EMPTY_STRING, 'A'));
                fileData.append(setField(new Integer(getProperty("FC-DTL-MNEMONIC")).intValue(), "", 'A'));
                fileData.append(setField(new Integer(getProperty("FC-DTL-STAFF-IND")).intValue(), "", 'A'));
                fileData.append(setField(new Integer(getProperty("FC-DTL-CLIENT-CLASS")).intValue(), "", 'A'));
                fileData.append(setField(new Integer(getProperty("FC-DTL-CLIENT-OCCUP")).intValue(), "", 'A'));
                fileData.append(setField(new Integer(getProperty("FC-DTL-BRANCH-RESP")).intValue(), "", 'A'));
                fileData.append(setField(new Integer(getProperty("FC-DTL-LANG-PREF")).intValue(), "", 'A'));
                
                LimitIndicator =  customerView.getDataMap().get(IBOUBTB_BPWCUSTREFRESHTAG.UBLIMITINDICATOR).toString();
                if(null!=LimitIndicator)
                	fileData.append(setField(new Integer(getProperty("FC-DTL-LIMIT-CHK-FLAG")).intValue(),LimitIndicator, 'A'));
                else
                	fileData.append(setField(new Integer(getProperty("FC-DTL-LIMIT-CHK-FLAG")).intValue(),CommonConstants.EMPTY_STRING, 'A'));

                alphaCode=customerView.getDataMap().get(IBOUBTB_BPWCUSTREFRESHTAG.UBALPHACODE).toString();
                if(null!=alphaCode){
                	if (alphaCode.length() > (new Integer(getProperty("FC-DTL-EXT-ALPHA-CODE")).intValue()))
                		fileData.append(setField(new Integer(getProperty("FC-DTL-EXT-ALPHA-CODE")).intValue(), (String) alphaCode.substring(0, 14), 'A'));
                	else fileData.append(setField(new Integer(getProperty("FC-DTL-EXT-ALPHA-CODE")).intValue(), (String) alphaCode, 'A'));
                }else fileData.append(setField(new Integer(getProperty("FC-DTL-EXT-ALPHA-CODE")).intValue(), CommonConstants.EMPTY_STRING, 'A'));
                
                fileData.append(setField(new Integer(getProperty("FC-DTL-EXT-ALT-CUS-ID")).intValue(), "", 'A'));
                fileData.append(setField(new Integer(getProperty("FC-DTL-FILLER-1")).intValue(), "", 'A'));
                fileData.append(setField(new Integer(getProperty("FC-DTL-BRANCH")).intValue(),  branch, 'A'));
                fileData.append(setField(new Integer(getProperty("FC-DTL-ACTION")).intValue(), "A", 'A'));

                BigDecimal checksum = null;
                if(null==branch){
                	checksum = CommonConstants.BIGDECIMAL_ZERO;
                	checksum = checksum.setScale(0);
                	fileData.append(setField(new Integer(getProperty("FC-DTL-CHECKSUM")).intValue(), checksum.abs().toString(), 'N'));
                }
                else{
                	checksum=new BigDecimal(branch + "");
                	checksum = checksum.setScale(0);
                	fileData.append(setField(new Integer(getProperty("FC-DTL-CHECKSUM")).intValue(),checksum.abs().toString(), 'N'));
                }
              
                fileData.append(setField(new Integer(getProperty("FC-DTL-FILLER-2")).intValue(), "", 'A'));
                fileData.append("\r\n");

                fout.write(fileData.toString().getBytes());
                fout.flush();
                totalCount++;
            }
           
        }
        catch (FileNotFoundException fnfExcpn) {
        	if(logger.isErrorEnabled()){
        		logger.error(fnfExcpn.getLocalizedMessage());
        	}
            BranchPowerCustomerRefreshFatomContext.Status = Boolean.FALSE;
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { fnfExcpn.getLocalizedMessage()  }, new HashMap(), BankFusionThreadLocal.getBankFusionEnvironment());
        }
        catch (IOException ioExcpn) {
        	if(logger.isErrorEnabled()){
        		logger.error(ioExcpn);
        	}
            BranchPowerCustomerRefreshFatomContext.Status = Boolean.FALSE;
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { ioExcpn.getLocalizedMessage()  }, new HashMap(), BankFusionThreadLocal.getBankFusionEnvironment());
        }
        catch (Exception Excpn) {
        	if(logger.isErrorEnabled()){
        		logger.error(Excpn.getLocalizedMessage());
        	}
            BranchPowerCustomerRefreshFatomContext.Status = Boolean.FALSE;
           
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { Excpn.getLocalizedMessage()  }, new HashMap(), BankFusionThreadLocal.getBankFusionEnvironment());
        }

    }

    /**
     * formats and writes the customer header record to mcfc9999.dat
     * 
     * @param Branch
     * @param fout
     * @throws ParseException
     */
    private void formatCustomerHeader(String Branch, BufferedOutputStream fout, BankFusionEnvironment env) throws BankFusionException {
        StringBuffer fileData = new StringBuffer();
        fileData.append(setField(new Integer(getProperty("FC-HDR-REC-TYPE")).intValue(), "01", 'A'));
        fileData.append(setField(new Integer(getProperty("FC-HDR-ACTION")).intValue(), hdrActionFlag, 'A'));
        fileData.append(setField(new Integer(getProperty("FC-HDR-SOURCE-SYSTEM")).intValue(), "MCAS", 'A'));
        fileData.append(setField(new Integer(getProperty("FC-HDR-DEST-SYSTEM")).intValue(), "BPWR", 'A'));
        fileData.append(setField(new Integer(getProperty("FC-HDR-BRANCH-CODE")).intValue(), Branch, 'N'));
        fileData.append(setField(new Integer(getProperty("FC-HDR-FILE-ID")).intValue(), "CL", 'A'));
        fileData.append(setField(new Integer(getProperty("FC-HDR-PROCESS-DATE")).intValue(), strBusinessDate, 'N'));
        fileData.append(setField(new Integer(getProperty("FC-HDR-FILLER-1")).intValue(), "", ' '));
        fileData.append("\r\n");
        try {
            fout.write(fileData.toString().getBytes());
            fout.flush();
        }
        catch (IOException ioExcpn) {
            //throw new BankFusionException(127, new Object[] { ioExcpn.getLocalizedMessage() }, logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { ioExcpn.getLocalizedMessage()  }, new HashMap(), BankFusionThreadLocal.getBankFusionEnvironment());
        }
    }

    /**
     * formats and writes the customer trailer record to mcfc9999.dat
     * 
     * @param branchCounter
     * @param fout
     */
    private void formatCustomerTrail(String branchCounter, BufferedOutputStream fout, BankFusionEnvironment env)
            throws BankFusionException {
        StringBuffer fileData = new StringBuffer();
        fileData.append(setField(new Integer(getProperty("FC-TRL-REC-TYPE")).intValue(), "99", 'A'));
        fileData.append(setField(new Integer(getProperty("FC-TRL-RECORD-COUNT")).intValue(), branchCounter, 'N'));
        fileData.append(setField(new Integer(getProperty("FC-TRL-FILLER-1")).intValue(), "", ' '));
        fileData.append("\r\n");
        try {
            fout.write(fileData.toString().getBytes());
            fout.flush();
            fout.close();
        }
        catch (NullPointerException npExcpn) {
            return;
        }
        catch (IOException ioExcpn) {
            //throw new BankFusionException(127, new Object[] { ioExcpn.getLocalizedMessage() }, logger, env);
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { ioExcpn.getLocalizedMessage()  }, new HashMap(), BankFusionThreadLocal.getBankFusionEnvironment());
        }
    }

    /**
     * reads the property for the key passed
     * 
     * @param sKey
     * @return
     */
    private  String getProperty(String sKey) {        
        return refreshPropertiesMap.get(sKey);
    }

    /**
     * This method formats fields input using the type ('A' or 'N') and length values passed.
     * returns the formatted string back to calling method
     * 
     * @param ind
     * @param string
     * @param type
     * @return
     */
    private static String setField(int ind, String string, char type) {
        int count = 0;
        if (null != string) {
            count = string.length();
        }
        final StringBuffer sbuff = new StringBuffer();
        if (type == 'A') {
            sbuff.append(string);
        }
        for (int index = count; index < ind; index++) {
            if (type == 'A') {
                sbuff.append(" ");
            }
            else {
                sbuff.append("0");
            }
        }

        if (type == 'N') {
            sbuff.append(string);
        }
        return sbuff.toString();
    }

    public static String logException(Throwable ex) {
        StringWriter exWriter = new StringWriter();
        ex.printStackTrace(new PrintWriter(exWriter));
        return exWriter.toString();

    }
    
    // artf638548- Total no of pages is derived to enable paging support to refresh Customer per
    // branch.
    private int getTotalNumberOfPages(int accPageSize, String branchSortCode) {

        int count = 0;
        int totalPage = 0;

        ArrayList params = new ArrayList();
        params.add(branchSortCode);

        ArrayList columnList = new ArrayList();
        columnList.add(IBOUBTB_BPWCUSTREFRESHTAG.UBBRANCHSORTCODE);

        List list = BankFusionThreadLocal.getPersistanceFactory().aggregateFunction(IBOUBTB_BPWCUSTREFRESHTAG.BONAME,
                branchSortCodeQuery, params, null, BankFusionThreadLocal.getPersistanceFactory().COUNT_FUNCTION_CODE, columnList, false);

        count = ((Integer) ((SimplePersistentObject) list.get(0)).getDataMap().get(IBOUBTB_BPWCUSTREFRESHTAG.UBBRANCHSORTCODE))
                .intValue();

        totalPage = (count % accPageSize == 0) ? (count / accPageSize) : (count / accPageSize) + 1;

        return totalPage;

    }
    
    private void openRefreshFile(String fName) {
        // Get the file name from the properties file

        File foutLocal = new File(fName);
        if (foutLocal.exists()) {
            foutLocal.delete();
        }
        BankFusionIOSupport.createNewFile(foutLocal);
        fout = new BufferedOutputStream(BankFusionIOSupport.createBufferedOutputStream(foutLocal, true));
    }
}
