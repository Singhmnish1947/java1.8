package com.trapedza.bankfusion.extensionpoints;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.GetUBConfigLocation;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.core.UpdateAuditLoggerHolder;
import com.trapedza.bankfusion.servercommon.core.UpdateAuditLoggerManager;
import com.trapedza.bankfusion.servercommon.extensionpoints.ExtensionPoint;

public class NCCUploadFW implements ExtensionPoint {
    private static final Log LOGGER = LogFactory.getLog(NCCUploadFW.class);

    private Map attributes = null;

    static Properties fedwirePositionProperties = new Properties();

    private final static String FEDWIRE_POSITION_PROPERY_NAME = "SWT_FWCodeFieldPostion.properties";

    private static final String FEDWIRE = "FW";

    private NCCUploadHelper nccUploadHelper = new NCCUploadHelper();

    private String fileSuffix = ".txt";

    /**
     * @see com.trapedza.bankfusion.core.ExtensionPoint#setAttributes(Map)
     */
    public void setAttributes(Map attributes) {
        this.attributes = attributes;
    }

    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    /**
     * @see com.trapedza.bankfusion.core.ExtensionPoint#getAttributes()
     */
    public Map getAttributes() {
        return attributes;
    }

    private boolean uploadFile(String fileName, BankFusionEnvironment env) {

        FileReader input = null;
        String inputLine = CommonConstants.EMPTY_STRING;
        int lineNumber = 0;
        boolean uploadStatus = true;
        String fileExtn = fileName.substring(fileName.length() - 4, fileName.length());

        if (!fileExtn.equalsIgnoreCase(fileSuffix)) {
            EventsHelper.handleEvent(ChannelsEventCodes.E_TXT_FILES_SUPPORTED_UB, new String[] {}, new HashMap(), env);
            uploadStatus = false;

        }
        else {
            try {

                nccUploadHelper.deleteNCCCRecords(FEDWIRE);
                input = new FileReader(fileName);
                try (BufferedReader fileReader = new BufferedReader(input)) {
                    while ((inputLine = fileReader.readLine()) != null) {
                        lineNumber++;
                        if (!parseAndStore(inputLine, lineNumber))
                            uploadStatus = false;
                    }
                }
            }
            catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
                EventsHelper.handleEvent(ChannelsEventCodes.E_NOT_FOUND_AS_FILE, new Object[] { fnfe.getLocalizedMessage() },
                        new HashMap(), env);
                uploadStatus = false;
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
                EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_NCCUPLOAD_FORMAT_UB,
                        new Object[] { ioe.getLocalizedMessage() }, new HashMap(), env);
                uploadStatus = false;
            }
            finally {
                if (input != null)
                    try {
                        input.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }
        return uploadStatus;
    }

    private boolean parseAndStore(String input, int lineNumber) {

        boolean uploadStatus = false;
        try {
            HashMap<NCCUploadFields, String> mapOfNCCFlds = new HashMap<NCCUploadFields, String>();
            mapOfNCCFlds = new HashMap<NCCUploadFields, String>();

            mapOfNCCFlds.put(NCCUploadFields.CLEARINGCODE, FEDWIRE);
            mapOfNCCFlds.put(NCCUploadFields.IDENTIFIERCODE, getFieldValue(input, FedWireProperties.ROUTING_NUMBER).trim());
            mapOfNCCFlds.put(NCCUploadFields.SUBBRANCHSUFFIX, "");
            mapOfNCCFlds.put(NCCUploadFields.BANKSHORTNAME, getFieldValue(input, FedWireProperties.TELEGRAPHIC_NAME).trim());
            mapOfNCCFlds.put(NCCUploadFields.BANKLONGNAME, getFieldValue(input, FedWireProperties.CUSTOMER_NAME).trim());
            mapOfNCCFlds.put(NCCUploadFields.BANKCODE, "");
            mapOfNCCFlds.put(NCCUploadFields.ADDRESSLINE1, getFieldValue(input, FedWireProperties.STATE).trim());
            mapOfNCCFlds.put(NCCUploadFields.ADDRESSLINE2, getFieldValue(input, FedWireProperties.CITY).trim());
            mapOfNCCFlds.put(NCCUploadFields.REVISIONDATE, getFieldValue(input, FedWireProperties.REVISIONDATE).trim());
            mapOfNCCFlds.put(NCCUploadFields.BRANCHNAME, "");
            uploadStatus = nccUploadHelper.validateAndStoreNCCCodes(mapOfNCCFlds, lineNumber);
        }
        catch (Exception runTimeExp) {
            EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_NCCUPLOAD_FORMAT_UB,
                    new Object[] { runTimeExp.getLocalizedMessage() }, new HashMap(),
                    BankFusionThreadLocal.getBankFusionEnvironment());
        }

        return uploadStatus;

    }

    private String getFieldValue(String inputString, FedWireProperties propKey) {

        int liFieldPos = 0;
        int liFieldLength = 0;
        String tempStr = CommonConstants.EMPTY_STRING;
        String configLocation = null;

        try {
            BankFusionEnvironment env = BankFusionThreadLocal.getBankFusionEnvironment();
            /*
             * configLocation = System.getProperty("BFconfigLocation",
             * CommonConstants.EMPTY_STRING);
             */
            configLocation = GetUBConfigLocation.getUBConfigLocation();
            // String path = System.getProperty("BFconfigLocation");
            readSWT_FWFieldPosition(configLocation + "conf/swift/" + FEDWIRE_POSITION_PROPERY_NAME, env);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        tempStr = fedwirePositionProperties.getProperty(propKey.toString());
        LOGGER.debug(tempStr);
        if (tempStr != null) {
            StringTokenizer tempTok = new StringTokenizer(tempStr, ",");
            liFieldPos = Integer.parseInt(tempTok.nextToken().trim());
            liFieldLength = Integer.parseInt(tempTok.nextToken().trim());

        }
        return inputString.substring(liFieldPos, liFieldLength).trim();
    }

    private void readSWT_FWFieldPosition(String positionFileName, BankFusionEnvironment env) throws IOException {

        InputStream is = null;

        try {
            is = new FileInputStream(positionFileName);
            fedwirePositionProperties.load(is);
            is.close();

        }
        catch (FileNotFoundException fnfe) {
            EventsHelper.handleEvent(ChannelsEventCodes.E_NOT_FOUND_AS_FILE, new Object[] { fnfe.getLocalizedMessage() },
                    new HashMap(), env);
        }

    }

    public void process(BankFusionEnvironment env) {

        String uploadFileName = attributes.get(NCCUploadHelper.NCC_UPLOADFILENAME).toString();

        boolean uploadStatus = uploadFile(uploadFileName, env);

        Boolean uploadStatusB = new Boolean(uploadStatus);

        attributes.put(NCCUploadHelper.NCC_UPLOADSTATUS, uploadStatusB);

    }

    public void continueProcess() {

    }

    public boolean isComplete() {
        return true;
    }

    public void registerWithUpdateLoggerManager(UpdateAuditLoggerManager manager) {
        if (manager.isTransactionLoggingEnabled()) {
            manager.addNewUpdateHolder(new UpdateAuditLoggerHolder(NCCUploadFW.class.getName(), svnRevision));
        }

    }
}
