/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/

package com.trapedza.bankfusion.fatoms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.GetUBConfigLocation;
import com.misys.ub.common.events.CommonsEventCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOBicCodes;
import com.trapedza.bankfusion.boundary.outward.BankFusionResourceSupport;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractSWT_BICCodeUploadFileReaderActivityStep;

/**
 * This fatom is responsible for reading Swift BICCodeUpload file from given location and updating
 * the underlying database for the given file.
 * 
 * @author singhh
 * 
 */
public class SWT_BICCodeUploadFatom extends AbstractSWT_BICCodeUploadFileReaderActivityStep {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private transient final static Log logger = LogFactory.getLog(SWT_BICCodeUploadFatom.class.getName());

    private final static String SWIFT_PROPERTY_FILE_NAME = "SWT_Configruation.properties";

    private final static String CONF = "conf/swift/";

    private final static String SWIFT_BICCODE_DIRLOCATION_FILENAME_KEY = "SWT_FileDirectoryLocation";

    private final static String SWIFT_BICCODE_FIELDPOSITION_FILENAME_KEY = "SWT_BICCodeFieldPostionFileName";

    private final static String SWIFT_BICCODE_REJECT_FILE_KEY = "SWT_BICCodeRejectAllFilesOnError";

    private final static String whereByBOID = "WHERE " + IBOBicCodes.BICCODE + " = ?";

    /**
     * Location of Swift files directory
     */
    String swiftFileDirectoryLocation;

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
    private final static String dotBICCodeUploadFileConstant = ".SWT";

    /**
     * Field separator delimeter for file reading
     */
    String fieldSeparatorDelim = new Character('\u002C').toString();

    /**
     * this ArrayList holds all swift BICCode record object - BICCodeUploadRecord in file
     */
    ArrayList bicCodeUploadRecords = new ArrayList();

    /**
     * BICCodeUploadRecord Object
     */
    SWT_BICCodeUploadRecord bicCodeUploadRecord;

    /**
     * Properties where config settings are defined
     */
    Properties swiftProperties = new Properties();

    Properties swiftPositionProperties = new Properties();

    /**
     * This Hash set will be populated with all the BIC codes available in the BICCODES table
     */
    HashSet BICCodes = new HashSet();

    /**
     * BO Object for BicCodes record.
     */
    IBOBicCodes bicCodesRecordBO;

    /**
     * Reject all files on error flag defaults to true. Can be overridden by specifying
     * SWT_BICCodeRejectWholeFileOnError=false on config file.
     */
    boolean flagBICCodeRejectAllFilesOnError = true;

    public SWT_BICCodeUploadFatom(BankFusionEnvironment env) {
        super(env);
    }

    public void process(BankFusionEnvironment env) {
        init(env);
        /* Accessing directory where all swift file(s) are stored */
        File fontisDir = new File(this.getF_IN_SwiftFileDirectoryLocation());
        String[] swiftFileArry = fontisDir.list();
        if (swiftFileArry == null || swiftFileArry.length <= 0)
            // displayMessifError(9413, new String[] {}, logger, env);
            displayMessifError(ChannelsEventCodes.E_SWIFT_DIRECTORY_IS_EMPTY, new String[] {}, env);
        else {

            String fileName = CommonConstants.EMPTY_STRING;
            ArrayList swiftFileList = filterSwiftFileOnName(swiftFileArry);

            if (swiftFileList == null || swiftFileList.size() <= 0)
                // displayMessifError(9414, new String[] {}, logger, env);
                displayMessifError(ChannelsEventCodes.E_NO_FILE_TO_READ_WITH_SWT_EXTENSION, new String[] {}, env);
            try {
                // env.getFactory().beginTransaction();
                for (int i = 0; i < swiftFileList.size(); i++) {
                    fileName = (String) swiftFileList.get(i);

                    try {
                        /* Reading Swift BICCode file */
                        ArrayList bicCodeUploadRecordList = (ArrayList) readSwiftFile(fileName, env);
                        if (bicCodeUploadRecordList != null) {
                            /*
                             * Validate each BICCode record(s) read from the file and store it in
                             * database table
                             */
                            boolean isValidBICCode = validateBICCodeUpload(bicCodeUploadRecordList);

                            if (flagBICCodeRejectAllFilesOnError && !isValidBICCode)
                                /*
                                 * //throw new BankFusionException(9420,
                                 * "Invalid BICCode File Format");
                                 */
                                EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_IDENTIFIERCODE_FILE_FORMAT, new Object[] {},
                                        new HashMap(), env);
                            else if (isValidBICCode) {
                                getAllBICCodes(env);
                                for (int j = 0; j < bicCodeUploadRecordList.size(); j++) {
                                    SWT_BICCodeUploadRecord uploadBICCodeRecord = (SWT_BICCodeUploadRecord) bicCodeUploadRecordList
                                            .get(j);
                                    /* Storing BICCode record(s) into database */
                                    storeSwiftBICCode(uploadBICCodeRecord, env);

                                }
                            }
                        }
                    }
                    catch (Exception be) {
                        renameProcessedFile(fileName, true);
                        /*
                         * throw new BankFusionException(9420, new Object[] { be
                         * .getLocalizedMessage() }, logger, env);
                         */
                        EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_IDENTIFIERCODE_FILE_FORMAT,
                                new Object[] { be.getLocalizedMessage() }, new HashMap(), env);
                        logger.error(ExceptionUtil.getExceptionAsString(be));
                    }
                }

                // env.getFactory().commitTransaction();
                /* Renaming Swift BICCode file on No Error */
                for (int i = 0; i < swiftFileList.size(); i++)
                    renameProcessedFile((String) swiftFileList.get(i), false);
                logger.info("SUCCESS changed file name : " + fileName);
            }
            catch (Exception ex) {
                logger.error(ExceptionUtil.getExceptionAsString(ex));
                renameProcessedFile(fileName, true);
                /*
                 * throw new BankFusionException(9420, new Object[] { ex .getLocalizedMessage() },
                 * logger, env);
                 */
                EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_IDENTIFIERCODE_FILE_FORMAT,
                        new Object[] { ex.getLocalizedMessage() }, new HashMap(), env);

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
            getSwiftConfigProperties(env);

            setF_IN_SwiftFileDirectoryLocation(swiftProperties.getProperty(SWIFT_BICCODE_DIRLOCATION_FILENAME_KEY));
            flagBICCodeRejectAllFilesOnError = Boolean
                    .valueOf(swiftProperties.getProperty(SWT_BICCodeUploadFatom.SWIFT_BICCODE_REJECT_FILE_KEY, "true"));

        }
        catch (Exception ex) {
            logger.error(ExceptionUtil.getExceptionAsString(ex));
            throw new BankFusionException(40507007, new Object[] { ex.getLocalizedMessage() }, logger, env);
            /*
             * EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_IDENTIFIERCODE_FILE_FORMAT, new
             * Object[] { ex .getLocalizedMessage() }, new HashMap(), env);
             */
        }

    }

    /**
     * Reads the BICCode input file gateway and processes the record(s) for database updates.
     * 
     * @param env
     * @
     */
    private List readSwiftFile(String fileName, BankFusionEnvironment env) {
        logger.debug("Read files with .SWT extension");
        ArrayList batchRecordList = null;
        if (fileName.startsWith(SWT_BICCodeUploadFatom.dotBICCodeUploadFileConstant)) {
            batchRecordList = (ArrayList) swtFileReader(fileName, env);
        }

        return batchRecordList;
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
            if (fileList[i].startsWith(SWT_BICCodeUploadFatom.dotBICCodeUploadFileConstant)) {
                swiftFileList.add(fileList[i]);
            }
        }
        return swiftFileList;

    }

    /**
     * Reads a SWT file from given path returns ArrayList of all batches from given SWT file.
     * 
     * @param filepath
     * @param env
     * @
     */
    private List swtFileReader(String fileName, BankFusionEnvironment env) {
        /*
         * String absoluteFilePath = this.getF_IN_SwiftFileDirectoryLocation() +
         * SWT_BICCodeUploadFatom.separator + fileName;
         */
        String absoluteFilePath = this.getF_IN_SwiftFileDirectoryLocation() + "/" + fileName;

        String tempString = CommonConstants.EMPTY_STRING;

        ArrayList swiftBatchRecords = null;
        FileReader input = null;
        BufferedReader fileReader = null;
        try {
            input = new FileReader(absoluteFilePath);
            fileReader = new BufferedReader(input);
            swiftBatchRecords = new ArrayList();
            String modificationFlag = CommonConstants.EMPTY_STRING;
            while ((tempString = fileReader.readLine()) != null) {

                /*
                 * If Modification_Flag is 'U'-Unchanged or null or empty or (any value other than
                 * 'A', 'D', 'M') --> ignore the record
                 */
                modificationFlag = getFieldValue(tempString, "Modification_Flag");

                if (!isEmptyOrNull(modificationFlag)
                        && (("M").equals(modificationFlag) || ("D").equals(modificationFlag) || ("A").equals(modificationFlag))) {
                    bicCodeUploadRecord = new SWT_BICCodeUploadRecord();
                    bicCodeUploadRecord = getSWTBatchDetailsFromString(tempString, bicCodeUploadRecord, env, modificationFlag);
                    swiftBatchRecords.add(bicCodeUploadRecord);
                }

            }
        }
        catch (Exception e) {
            renameProcessedFile(fileName, true);
            /*
             * throw new BankFusionException(9502, new Object[] { e .getLocalizedMessage() },
             * logger, env);
             */
            EventsHelper.handleEvent(CommonsEventCodes.E_NO_SLASH_IN_2_CHAR_IN_PARTY_ID_ADDR,
                    new Object[] { e.getLocalizedMessage() }, new HashMap(), env);
            logger.error(ExceptionUtil.getExceptionAsString(e));
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
                logger.error(ExceptionUtil.getExceptionAsString(ioe));
            }
            try {
                if (fileReader != null) {
                    fileReader.close();
                }
            }
            catch (IOException e) {
                EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { e.getLocalizedMessage() },
                        new HashMap(), env);
                logger.error(ExceptionUtil.getExceptionAsString(e));
            }
        }
        return swiftBatchRecords;
    }

    /**
     * Getiing batchrecord as a string from TPP file and set the general Instruction for
     * FontisBatchRecord
     * 
     * @param inputstring
     * @param BICCodeUploadRecord
     * @
     */
    private SWT_BICCodeUploadRecord getSWTBatchDetailsFromString(String inputString, SWT_BICCodeUploadRecord swtBatchRecord,
            BankFusionEnvironment env, String modificationFlag) {

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
            /*
             * throw new BankFusionException(127, new Object[] { e .getLocalizedMessage() }, logger,
             * env);
             */
            EventsHelper.handleEvent(CommonsEventCodes.E_AN_UNEXPECTED_ERROR_OCCURRED, new Object[] { e.getLocalizedMessage() },
                    new HashMap(), env);
            logger.error(ExceptionUtil.getExceptionAsString(e));
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
            appendStr = "_ERROR_";
        else appendStr = "_SUCCESS_";

        String time = (SystemInformationManager.getInstance().getBFBusinessDateTimeAsString()).replace(':', '-').replace(' ', '_');

        String newFileName = "_" + fileName + appendStr + time;
        logger.info("NEW FILE NAME::" + newFileName);
        File oldFileName = new File(this.getF_IN_SwiftFileDirectoryLocation(), fileName);

        File newFile = new File(this.getF_IN_SwiftFileDirectoryLocation(), newFileName);

        oldFileName.renameTo(newFile);

    }

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
            SWT_BICCodeUploadRecord uploadBICCodeRecord = (SWT_BICCodeUploadRecord) bicCodeUploadRecordList.get(j);
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
     * store the BICCodeUploadRecord values to database
     * 
     * @param BICCodeUploadRecord
     * @param env
     * @
     */
    private void storeSwiftBICCode(SWT_BICCodeUploadRecord uploadRecord, BankFusionEnvironment env) {

        String cmdFlag = uploadRecord.getModificationFlag();

        bicCodesRecordBO = (IBOBicCodes) env.getFactory().getStatelessNewInstance(IBOBicCodes.BONAME);

        /* Perform Database updates based on cmdFlag (create, modify, delete) */
        ArrayList columnList = null;
        ArrayList valueList = null;
        ArrayList primaryKeyValueList = null;
        if (("A").equals(cmdFlag)) {

            bicCodesRecordBO.setBoID(uploadRecord.getBicCode());

            bicCodesRecordBO.setF_NAME(uploadRecord.getInstitutionName1());

            bicCodesRecordBO.setF_LOCATION(uploadRecord.getBranchName1());

            bicCodesRecordBO.setF_CITY(uploadRecord.getCityHeading());

            try {
                String bicCode = bicCodesRecordBO.getBoID();

                if (BICCodes != null && BICCodes.contains((String) bicCode)) {

                    /*
                     * throw new BankFusionException(9420, "Invalid BICCode File Format");
                     */
                    EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_IDENTIFIERCODE_FILE_FORMAT, new Object[] {},
                            new HashMap(), env);
                }
                env.getFactory().create(IBOBicCodes.BONAME, bicCodesRecordBO);

            }
            catch (Exception e) {
                /*
                 * throw new BankFusionException(9420, "Invalid BICCode File Format");
                 */
                EventsHelper.handleEvent(ChannelsEventCodes.E_INVALID_IDENTIFIERCODE_FILE_FORMAT, new Object[] {}, new HashMap(),
                        env);
                logger.error(ExceptionUtil.getExceptionAsString(e));
            }
            logger.info("Inside storeSwiftBICCode1 Swift BICCode Upload...A END");
        }
        else if (("D").equals(cmdFlag)) {
            primaryKeyValueList = new ArrayList();
            primaryKeyValueList.add(uploadRecord.getBicCode());
            logger.info("storeSwiftBICCode: uploadRecord.getBicCode() ::" + uploadRecord.getBicCode());
            env.getFactory().bulkDelete(IBOBicCodes.BONAME, whereByBOID, primaryKeyValueList);
        }
        else if (("M").equals(cmdFlag)) {

            columnList = new ArrayList();
            valueList = new ArrayList();
            primaryKeyValueList = new ArrayList();

            columnList.add(IBOBicCodes.NAME);
            columnList.add(IBOBicCodes.LOCATION);
            columnList.add(IBOBicCodes.CITY);

            valueList.add(uploadRecord.getInstitutionName1());
            valueList.add(uploadRecord.getBranchName1());
            valueList.add(uploadRecord.getCityHeading());

            primaryKeyValueList.add(uploadRecord.getBicCode());

            env.getFactory().bulkUpdate(IBOBicCodes.BONAME, whereByBOID, primaryKeyValueList, columnList, valueList);

        }

    }

    /**
     * Get All BICCodes and Cache it
     * 
     * @param @throws
     */
    private void getAllBICCodes(BankFusionEnvironment env) {
        List bicCodesList = env.getFactory().findAll(IBOBicCodes.BONAME, null, false);
        for (int i = 0; i < bicCodesList.size(); i++) {
            IBOBicCodes bicCodeObj = (IBOBicCodes) bicCodesList.get(i);
            logger.info("BICCODE :getAllBICCodes :" + bicCodeObj.getBoID());
            BICCodes.add(bicCodeObj.getBoID());
        }
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
            /*
             * configLocation = System.getProperty("BFconfigLocation",
             * CommonConstants.EMPTY_STRING);
             */
            configLocation = GetUBConfigLocation.getUBConfigLocation();
            is = BankFusionResourceSupport.getResourceLoader().getInputStreamResourceFromLocationOnly(
                    CONF + SWIFT_PROPERTY_FILE_NAME, configLocation, BankFusionThreadLocal.getUserZone());
            swiftProperties.load(is);

            positionFileName = swiftProperties.getProperty(SWIFT_BICCODE_FIELDPOSITION_FILENAME_KEY);
        }
        catch (Exception ex) {
            if (is == null) {
                if (logger.isDebugEnabled())

                    is = this.getClass().getClassLoader().getResourceAsStream(CONF + SWIFT_PROPERTY_FILE_NAME);
                try {
                    swiftProperties.load(is);

                    positionFileName = swiftProperties.getProperty(SWIFT_BICCODE_FIELDPOSITION_FILENAME_KEY);
                }
                catch (Exception e) {
                    logger.error(ExceptionUtil.getExceptionAsString(e));

                    /*
                     * displayMessifError(9421, new String[] { "conf/swift/" +
                     * SWIFT_PROPERTY_FILE_NAME }, logger, env);
                     */
                    displayMessifError(ChannelsEventCodes.E_NOT_FOUND_AS_FILE, new String[] { CONF + SWIFT_PROPERTY_FILE_NAME },
                            env);

                }
            }
            // throw new BankFusionException(9506,ex.getLocalizedMessage());
            logger.error(ExceptionUtil.getExceptionAsString(ex));
        }
        if (positionFileName == null)
            /*
             * throw new BankFusionException(9506, positionFileName +
             * " not found as file, trying as resource");
             */
            EventsHelper.handleEvent(ChannelsEventCodes.E_NOT_FOUND_AS_FILE, new Object[] { positionFileName }, new HashMap(), env);
        else getSwiftPositionProperties(configLocation, positionFileName, env);
    }

    /**
     * Get Swift Position Properties and loads it
     * 
     * @param @throws
     */
    private void getSwiftPositionProperties(String configLocation, String positionFileName, BankFusionEnvironment env) {
        InputStream is = null;

        try {
            is = BankFusionResourceSupport.getResourceLoader().getInputStreamResourceFromLocationOnly("swift/" + positionFileName,
                    configLocation, BankFusionThreadLocal.getUserZone());
            swiftPositionProperties.load(is);

        }
        catch (Exception ex) {
            if (is == null) {
                if (logger.isDebugEnabled())

                    is = this.getClass().getClassLoader().getResourceAsStream("swift/" + positionFileName);
                try {
                    swiftPositionProperties.load(is);

                }
                catch (Exception e) {
                    /*
                     * displayMessifError(9421, new String[] { "swift/" + positionFileName },
                     * logger, env);
                     */
                    displayMessifError(ChannelsEventCodes.E_NOT_FOUND_AS_FILE, new String[] { "swift/" + positionFileName }, env);
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
        // throw new BankFusionException(val, obj, logger, env);
        EventsHelper.handleEvent(val, obj, new HashMap(), env);
    }
}
