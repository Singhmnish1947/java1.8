/* ********************************************************************************
 *  Copyright(c)2007  Misys Financial Systems Ltd. All Rights Reserved.
 *  
 *  
 *  
 *  
 *
 *  This software is the proprietary information of Misys Financial Systems Ltd.
 *  Use is subject to license terms.
 *
 * ********************************************************************************
 *
 * $Id: MGM_InvokeMoneyGram.java,v 1.4 2008/08/12 20:13:59 vivekr Exp $
 *
 * $Log: MGM_InvokeMoneyGram.java,v $
 * Revision 1.4  2008/08/12 20:13:59  vivekr
 * Merge from 3-3B branch to Head (Ref_Tag_UB-33B_11Aug08)
 *
 * Revision 1.2.4.1  2008/07/03 17:55:54  vivekr
 * Moved from Dublin CVS Head (For BF1.01 package restructuring)
 *
 * Revision 1.5  2008/06/16 15:21:34  arun
 * UB Refactoring -
 * 1. Formatted/Organized imports
 * 2. BODefinitionException, InvalidExtensionPointException and references removed
 * 3. Removed ServerManager deprecated methods/variables
 * 4. BankfusionPropertyAccess removed - changed to BankfusionPropertySupport
 * 5. Exception Handling refactoring
 * 6. General Refactoring
 *
 * Revision 1.4  2008/06/12 10:51:53  arun
 *  RIO on Head
 *
 * Revision 1.2  2007/09/28 12:10:43  vinayac
 * Moneygram Refresh Activity Steps
 *
 *
 * 
 *  * Code has been changed for Ref : Raised in SFDC with case reference as 00333384  and CSFE artf39821    Date : 19/06/2009 
 * Changes are :
 *  1. Removed hard coded value to handle timeout period.
 *  2. Implemented socket.setSoTimeout method  to replace number of while loop.
 *  3. Removed commented codes.
 *  4. Handled exception for Transaction Time Out period to display proper error message inplace of going  offline.
 *    
 */

package com.trapedza.bankfusion.fatoms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.moneygram.MGM_ExceptionHelper;
import com.misys.ub.moneygram.MGM_ReadProperties;
import com.trapedza.bankfusion.boundary.outward.BankFusionPropertySupport;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractMGM_InvokeMoneyGram;
import com.trapedza.bankfusion.steps.refimpl.IMGM_InvokeMoneyGram;

/**
 * This fatom sends the xml request to MoneyGram remote server and receives the xml response
 * synchronously through a given server ip address and port.
 * 
 * @author nileshk
 * 
 */
// Time is calculated in Seconds.
public class MGM_InvokeMoneyGram extends AbstractMGM_InvokeMoneyGram implements IMGM_InvokeMoneyGram {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    /**
     * Logger defined.
     */
    private transient final static Log logger = LogFactory.getLog(MGM_InvokeMoneyGram.class.getName());
    private String mgRequestXML;
    private String mgResponseXML;
    private static final char endCharacter = '\u0000';
    private int serverPort;
    private String address;
    protected boolean isTimerStarted;
    protected boolean TimeOut;
    protected long timerStartTime;
    protected long lTimeOut;
    private Integer iTimeOut = new Integer(0);
    public static final String MONEYGRAM_PROPERTY_FILENAME = "conf/moneygram/moneygram.properties";
    public static final String TIMEOUT = "TimeOut";

    /**
     * Constructor
     * 
     * @param env
     */
    public MGM_InvokeMoneyGram(BankFusionEnvironment env) {
        super(env);
    }

    /**
     * @see com.trapedza.bankfusion.steps.refimpl.AbstractMGM_InvokeMoneyGram#process(com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
     * @param environment
     *            The BankFusion Environment @
     */
    public void process(BankFusionEnvironment environment) {
        mgRequestXML = this.getF_IN_InputXML();
        lTimeOut = getTimeOut();
        String sTimeout = String.valueOf(lTimeOut);
        iTimeOut = Integer.valueOf(sTimeout);
        try {
            mgResponseXML = getMoneyGramResponse(mgRequestXML, environment);
            logger.debug("MoneyGram response :" + mgResponseXML);
        }
        catch (IOException e) {
            logger.error("IOException", e);
            MGM_ExceptionHelper exceptionHelper = new MGM_ExceptionHelper();
            exceptionHelper.throwMoneyGramException(006, environment);
        }
        catch (Exception e) {
            throw new BankFusionException(40507007, new Object[] { e.getLocalizedMessage() }, logger, e);
        }
        this.setF_OUT_OutputXML(mgResponseXML);
        logger.info("MoneyGram response from server received successfully");
    }

    /**
     * This method take the xml string and send it to MoneyGram server and returns the response as
     * xml string.
     * 
     * @param reqXML
     * @return responseXML @
     */
    public String getMoneyGramResponse(String reqXML, BankFusionEnvironment env) throws Exception {
        String resXML = CommonConstants.EMPTY_STRING;
        serverPort = (this.getF_IN_MoneyGramServerPort()).intValue();
        address = this.getF_IN_MoneyGramIPAddress();
        InetAddress ipAddress = InetAddress.getByName(address);
        logger.info("Invoking remote server");

        Socket socket;
        socket = new Socket(ipAddress, serverPort);

        // This Code has been added to replace manually check time out method.
        socket.setSoTimeout(iTimeOut);
        logger.debug("Remote server invoked at port no " + serverPort + "and at ip address " + address);
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
        bufferedWriter.write(reqXML + endCharacter);
        bufferedWriter.flush();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;

        try {
            while ((line = bufferedReader.readLine()) != null) {
                String initialLine = line.trim();

                line = initialLine.trim();
                resXML += line;
                logger.info("getMoneyGramResponse executed successfull");
            }
        }
        catch (SocketTimeoutException ste) {
        	logger.error(ste);
            if (logger.isInfoEnabled())
                logger.info("No Response Received from MoneyGram " + ste.getMessage());
            throw new BankFusionException(40507007, new Object[] { "Transaction Time Out" }, logger, null);
            
        }
        finally {
            try {
                inputStream.close();
                bufferedReader.close();
            }
            catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Exception occured" + e);
                }
            }
            try{
            socket.close();
            }catch (Exception e){
            	 logger.error("Exception occured" + e);
            }
        }
        String responseXML = resXML.trim();
        return responseXML;
    }

    long getTimeOut() {
        String timeOut = "0.0";
        try {
            timeOut = BankFusionPropertySupport.getProperty(MGM_ReadProperties.MONEYGRAM_PROPERTY_FILENAME,
                    MGM_ReadProperties.TIMEOUT, "40000");
        }
        catch (Exception ioe) {
            // General Exception is caught temporary purpose only should be removed once appropriate
            // exception
            // This will be identified by testing.
            if (logger.isInfoEnabled()) {
                logger.info("Error reading MoneyGram properties file defaulting TimeOut to 40000" + ioe.getMessage());
            }
            timeOut = "40000";
            logger.error(ioe);
        }
        if (timeOut == null)
            timeOut = "40000";
        return Long.valueOf(timeOut);
    }
}
