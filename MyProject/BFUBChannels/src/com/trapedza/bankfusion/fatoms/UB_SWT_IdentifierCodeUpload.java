package com.trapedza.bankfusion.fatoms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.cbs.common.functions.GetBankFusionMessage;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.GetUBConfigLocation;
import com.misys.ub.common.events.CommonsEventCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOBicCodes;
import com.trapedza.bankfusion.boundary.outward.BankFusionResourceSupport;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_IdentifierCodeUpload;
import com.trapedza.bankfusion.steps.refimpl.IUB_SWT_IdentifierCodeUpload;

public class UB_SWT_IdentifierCodeUpload extends AbstractUB_SWT_IdentifierCodeUpload implements IUB_SWT_IdentifierCodeUpload {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private transient final static Log logger = LogFactory.getLog(UB_SWT_IdentifierCodeUpload.class.getName());

    private final static String SWIFT_PROPERTY_FILE_NAME = "SWT_Configruation.properties";

    private final static String SWIFT_BICCODE_DIRLOCATION_FILENAME_KEY = "SWT_FileDirectoryLocation";

    private final static String SWIFT_BICCODE_FIELDPOSITION_FILENAME_KEY = "SWT_BICCodeFieldPostionFileName";

    private final static String SWIFT_BICCODE_FIELTYPE_KEY = "SWT_FileType";

    private final static String SWIFT_BICCODE_REJECT_FILE_KEY = "SWT_BICCodeRejectAllFilesOnError";

    // artf816846 changes starts.
    private String IDENTIFIERCODEALREADYEXIST = CommonConstants.EMPTY_STRING;

    private String IDENTIFIERCODENOTFOUND = CommonConstants.EMPTY_STRING;
    // artf816846 changes ends.

    private final static String separator = File.separator;
    FileWriter SuccessRecord;

    File fontisDir;

    BufferedReader freader;
    BufferedWriter outputError;
    BufferedWriter outputSuccess;

    String directoryLocation;

    /**
     * Location of Swift files directory
     */
    String SwiftICFileDirectoryLocation;

    /**
     * Name of the BICCodeUpload file.
     */
    String swiftBICUploadFileName;

    /**
     * File separator delimeter to creating file object with absolute path
     */
    // private final static String separator = "\\";
    /**
     * constant for file starting with .SWT
     */

    /**
     * Field separator delimeter for file reading
     */
    String fieldSeparatorDelim = new Character('\u002C').toString();

    /**
     * BICCodeUploadRecord Object
     */
    UB_SWT_IdentifierCodeUploadRecord bicCodeUploadRecord;

    /**
     * Properties where config settings are defined
     */
    Properties swiftProperties = new Properties();

    Properties swiftPositionProperties = new Properties();

    /**
     * This Hash set will be populated with all the BIC codes available in the BICCODES table
     */
    // HashSet identifierCode = new HashSet();
    HashMap toModify = new HashMap();

    /**
     * Reject all files on error flag defaults to true. Can be overridden by specifying
     * SWT_BICCodeRejectWholeFileOnError=false on config file.
     */
    boolean flagBICCodeRejectAllFilesOnError = true;
    boolean isValidBICCode;
    private final static String dotBICCodeUploadFileConstant = ".SWT";
    // Changes start for artf800667.
    private static String Active = "N";
    private static String Deleted = "Y";
    private static String XXX = "XXX";
    private IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
    IBOBicCodes identifierCodes;

    // Constants for txt file formats
    private final static String TXTFILETYPE = "txt";
    private final static String BICLENGHT_KEY = "SWT_BicLength";
    private final static String INSTITUTIONNAMELENGTH_KEY = "SWT_InstitutionNameLength";
    private final static String BRANCHLENGTH_KEY = "SWT_BranchLength";
    private final static String CITYLENGHT_KEY = "SWT_CityLength";
    private int BICLENGTH;
    private int INSTITUTIONNAMELENGTH;
    private int BRANCHLENGTH;
    private int CITYLENGHT;
    private String fileType;
    private String tab = "\t";
    private static int maxRecords = 10000;

    // Changes ends for artf800667.

    public UB_SWT_IdentifierCodeUpload(BankFusionEnvironment env) {
        super(env);
    }

    public void process(BankFusionEnvironment env) {
        init(env);
        /* Accessing directory where all swift file(s) are stored */
        fontisDir = new File(this.getF_IN_SwiftICFileDirectoryLocation());
        // artf816846 changes starts.
        populateErrorDetails();
        // artf816846 changes ends.
        String[] swiftFileArry = fontisDir.list();
        if (swiftFileArry == null || swiftFileArry.length <= 0)
            displayMessifError(ChannelsEventCodes.E_SWIFT_DIRECTORY_IS_EMPTY, new String[] {}, env);
        else {

            String fileName = CommonConstants.EMPTY_STRING;
            ArrayList swiftFileList = filterSwiftFileOnName(swiftFileArry);

            if (swiftFileList == null || swiftFileList.size() <= 0)
                displayMessifError(ChannelsEventCodes.E_NO_FILE_TO_READ_WITH_SWT_EXTENSION, new String[] {}, env);
            try {
                for (int i = 0; i < swiftFileList.size(); i++) {
                    fileName = (String) swiftFileList.get(i);
                    try {
                        /* Reading Swift BICCode file and process */
                        readSwiftFile(fileName, env);
                    }
                    catch (Exception be) {
                        if (flagBICCodeRejectAllFilesOnError && !isValidBICCode)
                            displayMessifError(ChannelsEventCodes.E_INVALID_IDENTIFIERCODE_FILE_FORMAT, new String[] {}, env);
                        renameProcessedFile(fileName, true);
                    }
                }
                /* Renaming Swift BICCode file on No Error */
                for (int i = 0; i < swiftFileList.size(); i++)
                    renameProcessedFile((String) swiftFileList.get(i), false);
                logger.info("SUCCESS changed file name : " + fileName);
            }
            catch (Exception ex) {
                if (flagBICCodeRejectAllFilesOnError && !isValidBICCode)
                    displayMessifError(ChannelsEventCodes.E_INVALID_IDENTIFIERCODE_FILE_FORMAT, new String[] {}, env);
                ex.printStackTrace();
                renameProcessedFile(fileName, true);

            }
        }
    }

    /**
     * Initialize initial values
     * 
     * @param @
     */
    private void init(BankFusionEnvironment env) {
        try {
            logger.debug("Inside INIT Method");
            getSwiftConfigProperties(env);

            setF_IN_SwiftICFileDirectoryLocation(swiftProperties.getProperty(SWIFT_BICCODE_DIRLOCATION_FILENAME_KEY));
            flagBICCodeRejectAllFilesOnError = Boolean
                    .valueOf(swiftProperties.getProperty(UB_SWT_IdentifierCodeUpload.SWIFT_BICCODE_REJECT_FILE_KEY, "true"))
                    .booleanValue();

        }
        catch (Exception ex) {

            EventsHelper.handleEvent(ChannelsEventCodes.E_EXCEPTION_OCCURED, new Object[] { ex.getLocalizedMessage() },
                    new HashMap(), env);
        }

    }

    /**
     * Reads the BICCode input file gateway and processes the record(s) for database updates.
     * 
     * @param env
     * @
     */
    private void readSwiftFile(String fileName, BankFusionEnvironment env) {
        logger.debug("Read files with .SWT extension");
        if (fileName.startsWith(UB_SWT_IdentifierCodeUpload.dotBICCodeUploadFileConstant)) {
            swtFileReader(fileName, env);
        }
    }

    /**
     * Reads the BICCode input file gateway and processes the record(s) for database updates.
     * 
     * @param env
     * @
     */
    private ArrayList filterSwiftFileOnName(String[] fileList) {

        ArrayList swiftFileList = new ArrayList();
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].startsWith(UB_SWT_IdentifierCodeUpload.dotBICCodeUploadFileConstant)) {
                swiftFileList.add(fileList[i]);
            }
        }
        return swiftFileList;

    }

    /**
     * Reads a SWT file from given path and process it to database.
     * 
     * @param filepath
     * @param env
     * @
     */
    private void swtFileReader(String fileName, BankFusionEnvironment env) {

        String absoluteFilePath = this.getF_IN_SwiftICFileDirectoryLocation() + "/" + fileName;
        String tempString = CommonConstants.EMPTY_STRING;
        int batchCounter = 0;
        int modifyCounter = 0;
        ArrayList swiftBatchRecords = null;
        FileReader input = null;
        BufferedReader fileReader = null;
        try {
            input = new FileReader(absoluteFilePath);
            fileReader = new BufferedReader(input);
            swiftBatchRecords = new ArrayList();
            String modificationFlag = CommonConstants.EMPTY_STRING;
            while ((tempString = fileReader.readLine()) != null) {
                // this block will be executed if the format is txt to bypass the first record of
                // file as first record is a header row.
                if (batchCounter == 0 && fileType.equals(TXTFILETYPE)) {
                    batchCounter++;
                    continue;
                }
                batchCounter++;
                bicCodeUploadRecord = new UB_SWT_IdentifierCodeUploadRecord();
                // if block is will be executed for txt file format and else for dat file format.
                if (fileType.equals(TXTFILETYPE)) {
                    bicCodeUploadRecord = getSWTBatchDetailsFromTxtString(tempString, bicCodeUploadRecord, env);
                    modificationFlag = bicCodeUploadRecord.getModificationFlag();
                    if (!isEmptyOrNull(modificationFlag) && (("M").equals(modificationFlag) || ("D").equals(modificationFlag)
                            || ("A").equals(modificationFlag))) {
                        toModify.put(modifyCounter, tempString);
                        modifyCounter++;
                        swiftBatchRecords.add(bicCodeUploadRecord);
                    }
                }
                else {
                    modificationFlag = getFieldValue(tempString, "Modification_Flag");

                    if (!isEmptyOrNull(modificationFlag) && (("M").equals(modificationFlag) || ("D").equals(modificationFlag)
                            || ("A").equals(modificationFlag))) {
                        bicCodeUploadRecord = getSWTBatchDetailsFromString(tempString, bicCodeUploadRecord, env, modificationFlag);
                        toModify.put(modifyCounter, tempString);
                        modifyCounter++;
                        swiftBatchRecords.add(bicCodeUploadRecord);
                    }
                }
                if (swiftBatchRecords.size() >= maxRecords) {
                    if (swiftBatchRecords != null) {
                        isValidBICCode = validateBICCodeUpload(swiftBatchRecords);

                        if (flagBICCodeRejectAllFilesOnError && !isValidBICCode)
                            displayMessifError(ChannelsEventCodes.E_INVALID_IDENTIFIERCODE_FILE_FORMAT, new String[] {}, env);
                        else if (isValidBICCode) {
                            storeSwiftIdentifierCodes(swiftBatchRecords);
                            swiftBatchRecords.clear();
                            toModify.clear();
                            modifyCounter = 0;
                        }
                    }
                }
            }
            if (swiftBatchRecords != null) {
                isValidBICCode = validateBICCodeUpload(swiftBatchRecords);
                if (flagBICCodeRejectAllFilesOnError && !isValidBICCode)
                    displayMessifError(ChannelsEventCodes.E_INVALID_IDENTIFIERCODE_FILE_FORMAT, new String[] {}, env);
                else if (isValidBICCode) {
                    storeSwiftIdentifierCodes(swiftBatchRecords);
                    swiftBatchRecords.clear();
                    batchCounter = 1;
                    toModify.clear();
                }
            }
            input.close();
        }
        catch (Exception e) {
            try {
                if (input != null) {
                    input.close();
                }
            }
            catch (IOException ex) {
                EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
                        new Object[] { ex.getLocalizedMessage() }, new HashMap(), env);
            }
            renameProcessedFile(fileName, true);
            EventsHelper.handleEvent(ChannelsEventCodes.E_EXCEPTION_OCCURED, new Object[] { e.getLocalizedMessage() },
                    new HashMap(), env);
        }
        finally {
            try {
                if (input != null) {
                    input.close();
                }
            }
            catch (IOException ioe) {
                EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED,
                        new Object[] { ioe.getLocalizedMessage() }, new HashMap(), env);
            }
            try {
                if (fileReader != null)
                    fileReader.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Getiing batchrecord as a string from TPP file and set the general Instruction for
     * FontisBatchRecord
     * 
     * @param inputstring
     * @param BICCodeUploadRecord
     * @
     */
    private UB_SWT_IdentifierCodeUploadRecord getSWTBatchDetailsFromString(String inputString,
            UB_SWT_IdentifierCodeUploadRecord swtBatchRecord, BankFusionEnvironment env, String modificationFlag) {

        String tempStr = CommonConstants.EMPTY_STRING;

        try {
            tempStr = getFieldValue(inputString, "Record_Type");
            swtBatchRecord.setRecordType(tempStr);

            swtBatchRecord.setModificationFlag(modificationFlag);

            tempStr = getFieldValue(inputString, "BIC_Code");
            swtBatchRecord.setBicCode(tempStr);

            tempStr = getFieldValue(inputString, "Instituition_Name1");
            swtBatchRecord.setInstitutionName1(tempStr);

            tempStr = getFieldValue(inputString, "Branch_name1");
            swtBatchRecord.setBranchName1(tempStr);

            tempStr = getFieldValue(inputString, "City_Heading");
            swtBatchRecord.setCityHeading(tempStr);

        }
        catch (Exception e) {
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { e.getLocalizedMessage() },
                    new HashMap(), env);
        }

        return swtBatchRecord;
    }

    /**
     * Gettting filename and renaming the given file based on the error Flag
     * 
     * @param fileName
     * @param errFlag
     * @
     */
    private void renameProcessedFile(String fileName, boolean errFlag) {
        String appendStr = CommonConstants.EMPTY_STRING;
        if (errFlag)
            appendStr = "_PROCESSED_";
        else appendStr = "_PROCESSED_";

        String time = (SystemInformationManager.getInstance().getBFBusinessDateTimeAsString()).replace(':', '-').replace(' ', '_');

        String newFileName = appendStr + time;
        File oldFileName = new File(this.getF_IN_SwiftICFileDirectoryLocation(), fileName);

        File newFile = new File(this.getF_IN_SwiftICFileDirectoryLocation(), newFileName);

        oldFileName.renameTo(newFile);

    }

    /**
     * @param psString
     * @param propKey
     * @return
     */
    private String getFieldValue(String psString, String propKey) {
        if (isEmptyOrNull(psString)) {
            return CommonConstants.EMPTY_STRING;
        }
        int liFieldPos = 0;
        int liFieldLength = 0;
        String tempStr = CommonConstants.EMPTY_STRING;
        StringBuffer lsColumnMapping = new StringBuffer();
        tempStr = swiftPositionProperties.getProperty(propKey);
        StringTokenizer tempTok = new StringTokenizer(tempStr, fieldSeparatorDelim);
        liFieldPos = Integer.parseInt(tempTok.nextToken().trim()) - 1;
        liFieldLength = Integer.parseInt(tempTok.nextToken().trim());
        lsColumnMapping.append(psString.substring(liFieldPos, liFieldPos + liFieldLength));
        return lsColumnMapping.toString();
    }

    /**
     * Checks whether the BICCode is supplied returns TRUE Any of the record have BICCode no. else
     * returns FALSE
     */

    private boolean validateBICCodeUpload(ArrayList bicCodeUploadRecordList) {
        boolean validbatch = true;
        for (int j = 0; j < bicCodeUploadRecordList.size(); j++) {
            UB_SWT_IdentifierCodeUploadRecord uploadBICCodeRecord = (UB_SWT_IdentifierCodeUploadRecord) bicCodeUploadRecordList
                    .get(j);
            if (isEmptyOrNull(uploadBICCodeRecord.getBicCode())) {
                validbatch = false;

            }
        }
        return validbatch;
    }

    private boolean isEmptyOrNull(String str) {
        if (str == null || str.trim().length() == 0 || "null".equals(str))
            return true;
        return false;
    }

    /**
     * Get Swift Configuation Properties and loads it
     * 
     * @param @throws
     */
    private void getSwiftConfigProperties(BankFusionEnvironment env) {
        InputStream is = null;

        String configLocation = null;
        String positionFileName = null;
        try {
            // configLocation = System.getProperty("BFconfigLocation",
            // CommonConstants.EMPTY_STRING);
            configLocation = GetUBConfigLocation.getUBConfigLocation();
            is = BankFusionResourceSupport.getResourceLoader().getInputStreamResourceFromLocationOnly(
                    "conf/swift/" + SWIFT_PROPERTY_FILE_NAME, configLocation, BankFusionThreadLocal.getUserZone());
            swiftProperties.load(is);
            // To analyze the format of file.
            fileType = swiftProperties.getProperty(SWIFT_BICCODE_FIELTYPE_KEY).trim().toLowerCase();
            // Used for dat file format.
            if (!fileType.equals(TXTFILETYPE)) {
                positionFileName = swiftProperties.getProperty(SWIFT_BICCODE_FIELDPOSITION_FILENAME_KEY);
                if (positionFileName == null)
                    EventsHelper.handleEvent(ChannelsEventCodes.E_EXCEPTION_OCCURED,
                            new Object[] { positionFileName + " not found as file, trying as resource" }, new HashMap(), env);
                else getSwiftPositionProperties(configLocation, positionFileName, env);
            }
            // Used for txt file format.
            else if (fileType.equals(TXTFILETYPE)) {
                BICLENGTH = Integer.parseInt(swiftProperties.getProperty(BICLENGHT_KEY));
                INSTITUTIONNAMELENGTH = Integer.parseInt(swiftProperties.getProperty(INSTITUTIONNAMELENGTH_KEY));
                BRANCHLENGTH = Integer.parseInt(swiftProperties.getProperty(BRANCHLENGTH_KEY));
                CITYLENGHT = Integer.parseInt(swiftProperties.getProperty(CITYLENGHT_KEY));
            }
        }
        catch (Exception ex) {
            if (is == null) {
                if (logger.isDebugEnabled())

                    is = this.getClass().getClassLoader().getResourceAsStream("conf/swift/" + SWIFT_PROPERTY_FILE_NAME);
                try {
                    swiftProperties.load(is);

                    positionFileName = swiftProperties.getProperty(SWIFT_BICCODE_FIELDPOSITION_FILENAME_KEY);
                }
                catch (Exception e) {

                    displayMessifError(ChannelsEventCodes.E_NOT_FOUND_AS_FILE,
                            new String[] { "conf/swift/" + SWIFT_PROPERTY_FILE_NAME }, env);
                    logger.error(ExceptionUtil.getExceptionAsString(e));
                }
            }
        }
    }

    /**
     * Get Swift Position Properties and loads it
     * 
     * @param @throws
     */
    private void getSwiftPositionProperties(String configLocation, String positionFileName, BankFusionEnvironment env) {
        InputStream is = null;

        try {
            is = BankFusionResourceSupport.getResourceLoader().getInputStreamResourceFromLocationOnly(
                    "conf/swift/" + positionFileName, configLocation, BankFusionThreadLocal.getUserZone());
            swiftPositionProperties.load(is);

        }
        catch (Exception ex) {
            if (is == null) {
                if (logger.isDebugEnabled())

                    is = this.getClass().getClassLoader().getResourceAsStream("conf/swift/" + positionFileName);
                try {
                    swiftPositionProperties.load(is);

                }
                catch (Exception e) {
                    displayMessifError(ChannelsEventCodes.E_NOT_FOUND_AS_FILE, new String[] { "conf/swift/" + positionFileName },
                            env);
                    logger.error(ExceptionUtil.getExceptionAsString(e));
                }
            }
            logger.error(ExceptionUtil.getExceptionAsString(ex));
        }
    }

    /**
     * Generic method to display errors
     * 
     * @param val
     * @param obj
     * @param logger
     * @param env
     * @
     */
    private void displayMessifError(int val, String[] obj, BankFusionEnvironment env) {
        EventsHelper.handleEvent(val, obj, new HashMap(), env);
    }

    /**
     * @param bicCodeUploadRecordlist
     * @param env
     */
    private void storeSwiftIdentifierCodes(ArrayList bicCodeUploadRecordlist) {

        try {
            directoryLocation = fontisDir.getPath();
            String fileNamStrErr = "_ERROR_";
            String fileNamStrSucc = "_SUCCESS_";
            String time = (SystemInformationManager.getInstance().getBFBusinessDateTimeAsString()).replace(':', '-').replace(' ',
                    '_');

            String errorFileName = fileNamStrErr + time;
            String successFileName = fileNamStrSucc + time;
            /*
             * Perform Database updates based on cmdFlag (create, modify, delete)
             */
            outputError = new BufferedWriter(new FileWriter(directoryLocation + separator + errorFileName));
            outputSuccess = new BufferedWriter(new FileWriter(directoryLocation + separator + successFileName));
            for (int j = 0; j < bicCodeUploadRecordlist.size(); j++) {
                UB_SWT_IdentifierCodeUploadRecord uploadBICCodeRecord = (UB_SWT_IdentifierCodeUploadRecord) bicCodeUploadRecordlist
                        .get(j);
                String cmdFlag = uploadBICCodeRecord.getModificationFlag(); // getFieldValue
                String finalBicCode = (uploadBICCodeRecord.getBicCode().length() == 8) ? (uploadBICCodeRecord.getBicCode() + XXX)
                        : (uploadBICCodeRecord.getBicCode());
                identifierCodes = (IBOBicCodes) factory.findByPrimaryKey(IBOBicCodes.BONAME, finalBicCode, true);
                if (("A").equals(cmdFlag) && (identifierCodes != null)) {
                    outputError.write(toModify.get(j).toString());
                    outputError.newLine();
                    // artf816846 changes starts.
                    outputError.write(IDENTIFIERCODEALREADYEXIST);
                    outputError.newLine();
                    // artf816846 changes ends.
                    outputError.flush();
                }
                else if (("A").equals(cmdFlag) && (identifierCodes == null)) {
                    outputSuccess.write(toModify.get(j).toString());
                    outputSuccess.newLine();
                    outputSuccess.flush();
                    identifierCodes = (IBOBicCodes) factory.getStatelessNewInstance(IBOBicCodes.BONAME);
                    identifierCodes.setBoID(finalBicCode);
                    identifierCodes.setF_BKEAUTH(Boolean.TRUE);
                    identifierCodes.setF_CITY(uploadBICCodeRecord.getCityHeading());
                    identifierCodes.setF_ISDELETED(Active);
                    identifierCodes.setF_LOCATION(uploadBICCodeRecord.getBranchName1());
                    identifierCodes.setF_NAME(uploadBICCodeRecord.getInstitutionName1());
                    factory.create(IBOBicCodes.BONAME, identifierCodes);
                }

                else if ((("M").equals(cmdFlag)) && (identifierCodes == null)) {
                    outputError.write(toModify.get(j).toString());
                    outputError.newLine();
                    // artf816846 changes starts.
                    outputError.write(IDENTIFIERCODENOTFOUND);
                    outputError.newLine();
                    // artf816846 changes ends.
                    outputError.flush();
                }
                else if ((("M").equals(cmdFlag)) && (identifierCodes != null)) {
                    outputSuccess.write(toModify.get(j).toString());
                    outputSuccess.newLine();
                    outputSuccess.flush();
                    if (identifierCodes != null) {
                        identifierCodes.setF_CITY(uploadBICCodeRecord.getCityHeading());
                        identifierCodes.setF_ISDELETED(Active);
                        identifierCodes.setF_LOCATION(uploadBICCodeRecord.getBranchName1());
                        identifierCodes.setF_NAME(uploadBICCodeRecord.getInstitutionName1());
                    }
                }
                else if ((("D").equals(cmdFlag)) && (identifierCodes == null)) {
                    outputError.write(toModify.get(j).toString());
                    outputError.newLine();
                    // artf816846 changes starts.
                    outputError.write(IDENTIFIERCODENOTFOUND);
                    outputError.newLine();
                    // artf816846 changes ends
                    outputError.flush();

                }
                else if ((("D").equals(cmdFlag)) && (identifierCodes != null)) {
                    outputSuccess.write(toModify.get(j).toString());
                    outputSuccess.newLine();
                    outputSuccess.flush();
                    if (identifierCodes != null) {
                        identifierCodes.setF_BKEAUTH(Boolean.FALSE);
                        identifierCodes.setF_CITY(uploadBICCodeRecord.getCityHeading());
                        identifierCodes.setF_ISDELETED(Deleted);
                        identifierCodes.setF_LOCATION(uploadBICCodeRecord.getBranchName1());
                        identifierCodes.setF_NAME(uploadBICCodeRecord.getInstitutionName1());
                    }
                }
            }

        }
        catch (Exception e) {
            logger.error(ExceptionUtil.getExceptionAsString(e));
        }
        finally {

            try {
                if (freader != null) {
                    freader.close();
                }

                if (outputError != null) {
                    outputError.close();
                }

                if (outputSuccess != null) {
                    outputSuccess.close();
                }

            }
            catch (Exception e) {
                logger.error(ExceptionUtil.getExceptionAsString(e));
            }

        }
    }

    // This method is used for txt file format.
    private UB_SWT_IdentifierCodeUploadRecord getSWTBatchDetailsFromTxtString(String inputString,
            UB_SWT_IdentifierCodeUploadRecord swtBatchRecord, BankFusionEnvironment env) {

        String tempStr = CommonConstants.EMPTY_STRING;
        try {
            tempStr = inputString.substring(0, inputString.indexOf(tab)).trim();
            inputString = inputString.substring(inputString.indexOf(tab)).trim();
            swtBatchRecord.setRecordType(tempStr);

            tempStr = inputString.substring(0, inputString.indexOf(tab)).trim();
            inputString = inputString.substring(inputString.indexOf(tab)).trim();
            swtBatchRecord.setModificationFlag(tempStr);

            tempStr = inputString.substring(0, inputString.indexOf(tab)).trim();
            inputString = inputString.substring(inputString.indexOf(tab)).trim();
            tempStr = tempStr + inputString.substring(0, inputString.indexOf(tab)).trim();
            inputString = inputString.substring(inputString.indexOf(tab)).trim();
            if (tempStr.length() > BICLENGTH)
                tempStr = tempStr.substring(0, BICLENGTH);
            swtBatchRecord.setBicCode(tempStr);

            if (inputString.indexOf(tab) >= 0) {
                tempStr = inputString.substring(0, inputString.indexOf(tab)).trim();
                inputString = inputString.substring(inputString.indexOf(tab)).trim();
                if (tempStr.length() > INSTITUTIONNAMELENGTH)
                    tempStr = tempStr.substring(0, INSTITUTIONNAMELENGTH);
                swtBatchRecord.setInstitutionName1(tempStr);
            }

            if (inputString.indexOf(tab) >= 0) {
                tempStr = inputString.substring(0, inputString.indexOf(tab)).trim();
                inputString = inputString.substring(inputString.indexOf(tab)).trim();
                if (tempStr.length() > BRANCHLENGTH)
                    tempStr = tempStr.substring(0, BRANCHLENGTH);
                swtBatchRecord.setBranchName1(tempStr);
            }

            if (inputString.indexOf(tab) >= 0) {
                tempStr = inputString.substring(0, inputString.indexOf(tab)).trim();
                inputString = inputString.substring(inputString.indexOf(tab)).trim();
                if (tempStr.length() > CITYLENGHT)
                    tempStr = tempStr.substring(0, CITYLENGHT);
                swtBatchRecord.setCityHeading(tempStr);
            }

        }
        catch (Exception e) {
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { e.getLocalizedMessage() },
                    new HashMap(), env);
            logger.error(ExceptionUtil.getExceptionAsString(e));
        }

        return swtBatchRecord;
    }

    // artf816846 changes starts.
    private void populateErrorDetails() {
        IDENTIFIERCODEALREADYEXIST = GetBankFusionMessage.run(ChannelsEventCodes.E_IDENTIFIER_CODE_ALREADY_EXIST_UB);
        IDENTIFIERCODENOTFOUND = GetBankFusionMessage.run(ChannelsEventCodes.E_IDENTIFIER_CODE_DOES_NOT_EXIST_UB);
    }
    // artf816846 changes ends.
}
