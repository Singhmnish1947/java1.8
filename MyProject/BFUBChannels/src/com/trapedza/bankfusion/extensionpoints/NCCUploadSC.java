package com.trapedza.bankfusion.extensionpoints;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.core.UpdateAuditLoggerHolder;
import com.trapedza.bankfusion.servercommon.core.UpdateAuditLoggerManager;
import com.trapedza.bankfusion.servercommon.extensionpoints.ExtensionPoint;

public class NCCUploadSC implements ExtensionPoint {

    private static final Log LOGGER = LogFactory.getLog(NCCUploadSC.class);
    private Map attributes = null;

    private static final String NCC_UPLOADSTATUS = "UploadStatus";

    private static final String SORTCODE = "SC";

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
        LOGGER.debug(fileExtn.equalsIgnoreCase(fileSuffix));
        if (!fileExtn.equalsIgnoreCase(fileSuffix)) {
            EventsHelper.handleEvent(ChannelsEventCodes.E_TXT_FILES_SUPPORTED_UB, new String[] {}, new HashMap(), env);
            uploadStatus = false;

        }
        else {
            try {
                nccUploadHelper.deleteNCCCRecords(SORTCODE);
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

    private boolean parseAndStore(String inputLine, int lineNumber) {
        boolean uploadStatus = false;

        try {
            String sortCodeDetails[] = inputLine.split("\t");

            HashMap<NCCUploadFields, String> mapOfNCCFlds = new HashMap<NCCUploadFields, String>();

            mapOfNCCFlds = new HashMap<NCCUploadFields, String>();

            mapOfNCCFlds.put(NCCUploadFields.CLEARINGCODE, SORTCODE);
            mapOfNCCFlds.put(NCCUploadFields.IDENTIFIERCODE, sortCodeDetails[0].trim());
            mapOfNCCFlds.put(NCCUploadFields.SUBBRANCHSUFFIX, sortCodeDetails[3].trim());
            mapOfNCCFlds.put(NCCUploadFields.BRANCHNAME, sortCodeDetails[4].trim());
            mapOfNCCFlds.put(NCCUploadFields.BANKSHORTNAME, sortCodeDetails[5].trim());
            mapOfNCCFlds.put(NCCUploadFields.BANKLONGNAME, sortCodeDetails[6].trim() + sortCodeDetails[7].trim());
            mapOfNCCFlds.put(NCCUploadFields.BANKCODE, sortCodeDetails[8].trim());
            mapOfNCCFlds.put(NCCUploadFields.ADDRESSLINE1, "");
            mapOfNCCFlds.put(NCCUploadFields.ADDRESSLINE2, "");
            mapOfNCCFlds.put(NCCUploadFields.REVISIONDATE, sortCodeDetails[12].trim());

            uploadStatus = nccUploadHelper.validateAndStoreNCCCodes(mapOfNCCFlds, lineNumber);
        }
        catch (Exception runTimeExp) {
            EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_NCCUPLOAD_FORMAT_UB,
                    new Object[] { runTimeExp.getLocalizedMessage() }, new HashMap(),
                    BankFusionThreadLocal.getBankFusionEnvironment());

        }
        return uploadStatus;

    }

    public void continueProcess() {

    }

    public boolean isComplete() {
        return true;
    }

    public void registerWithUpdateLoggerManager(UpdateAuditLoggerManager manager) {
        if (manager.isTransactionLoggingEnabled()) {
            manager.addNewUpdateHolder(new UpdateAuditLoggerHolder(NCCUploadSC.class.getName(), svnRevision));
        }
    }

    public void process(BankFusionEnvironment env) {

        String uploadFileName = attributes.get(NCCUploadHelper.NCC_UPLOADFILENAME).toString();

        boolean uploadStatus = uploadFile(uploadFileName, env);

        Boolean uploadStatusB = new Boolean(uploadStatus);

        attributes.put(NCCUploadHelper.NCC_UPLOADSTATUS, uploadStatusB);

    }

}
