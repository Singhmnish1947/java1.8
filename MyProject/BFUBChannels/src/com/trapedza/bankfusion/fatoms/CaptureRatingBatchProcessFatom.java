/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: CaptureRatingBatchProcessFatom.java,v.1.0,Apr 21, 2009 12:06:44 PM ayerla
 *
 */
package com.trapedza.bankfusion.fatoms;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exolab.castor.core.exceptions.CastorException;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.GetUBConfigLocation;
import com.misys.ub.common.almonde.BatchInformation;
import com.misys.ub.common.almonde.CurrencyRating;
import com.misys.ub.common.almonde.OverallRating;
import com.misys.ub.common.almonde.RatingDetails;
import com.misys.ub.common.almonde.helper.BatchLogger;
import com.misys.ub.common.almonde.helper.RatingDetailsValidator;
import com.trapedza.bankfusion.bo.refimpl.IBOUBTB_RATINGDETAILS;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_ALD_CURRENCYRATING;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_ALD_OVERALLRATING;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ALD_CaptureRating_BatchProcess_Fatom;
import com.trapedza.bankfusion.utils.BankFusionMessages;
import com.trapedza.bankfusion.utils.GUIDGen;

/**
 * @author ayerla
 * @date Apr 21, 2009
 * @project Universal Banking
 * @Description: CaptureRatingBatchProcessFatom processes the batch Files from the batchLoaction and
 *               creates the Overallrating and currencyrating details.
 */

public class CaptureRatingBatchProcessFatom extends AbstractUB_ALD_CaptureRating_BatchProcess_Fatom {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}



    /**
     * @param env
     */
    public CaptureRatingBatchProcessFatom(BankFusionEnvironment env) {
        super(env);
    }

    private static final transient Log logger = LogFactory.getLog(CaptureRatingBatchProcessFatom.class.getName());
    private String confLocation = CommonConstants.EMPTY_STRING;
    private String mappingFile = CommonConstants.EMPTY_STRING;
    private String batchLocation = CommonConstants.EMPTY_STRING;
    private static final String UBRATINGDLTSID = "UBRATINGDLTSID";
    private static final String UBRATINGCODE = "UBRATINGCODE";
    private static final String UBRATINGTERM = "UBRATINGTERM";
    private static final String UBRATINGVALUE = "UBRATINGVALUE";
    private static final String UBOVRLRATINGID = "UBOVRLRATINGID";
    private static final String UBRATINGENTITYTYPE = "UBRATINGENTITYTYPE";
    private static final String UBRATINGENTITYID = "UBRATINGENTITYID";
    private static final String UBCURRENCYRATINGID = "UBCURRENCYRATINGID";
    private static final String UBISOCURRENCYCODE = "UBISOCURRENCYCODE";
    private static final String BATCH_ARCHIVE = "BATCH ARCHIVE";
    private static final String errorMessage = "Capture Rating Batch Process Cannot Be Completed";
    private static final String successMessage = "Capture Rating Batch Process Completed Successfully";
    private static final String noFilesMessage = "No Files to Process at ";
    private static final String CaptureRatingBatchProcessSchema = "CaptureRatingBatchProcessSchema.xsd";
    private static final String schemaLang = "http://www.w3.org/2001/XMLSchema";
    private static final String NOTPROCESSED = "Unable To Process The File";

    private BatchInformation captureRatingsBatchData;
    private BatchLogger batchLogger;
    private IPersistenceObjectsFactory factory;

    private Map<String, String> ratingDetailsMap;
    private Map<String, String> overAllRatingMap;
    private Map<String, String> currencyRatingMap;
    private boolean error = false;

    private final String CURRENCY_RATING_QUERY = CommonConstants.SELECT + CommonConstants.SPACE + "T1."
            + IBOUBTB_RATINGDETAILS.UBRATINGDLTSIDPK + " AS " + UBRATINGDLTSID + CommonConstants.COMMA + "T1."
            + IBOUBTB_RATINGDETAILS.UBRATINGCODE + " AS " + UBRATINGCODE + CommonConstants.COMMA + "T1."
            + IBOUBTB_RATINGDETAILS.UBRATINGTERM + " AS " + UBRATINGTERM + CommonConstants.COMMA + "T1."
            + IBOUBTB_RATINGDETAILS.UBRATINGVALUE + " AS " + UBRATINGVALUE + CommonConstants.COMMA + "T2."
            + IBOUB_ALD_CURRENCYRATING.UBCURRENCYRATINGIDPK + " AS " + UBCURRENCYRATINGID + CommonConstants.COMMA + "T2."
            + IBOUB_ALD_CURRENCYRATING.UBRATINGENTITYTYPE + " AS " + UBRATINGENTITYTYPE + CommonConstants.COMMA + "T2."
            + IBOUB_ALD_CURRENCYRATING.UBRATINGENTITYID + " AS " + UBRATINGENTITYID + CommonConstants.COMMA + "T2."
            + IBOUB_ALD_CURRENCYRATING.UBISOCURRENCYCODE + " AS " + UBISOCURRENCYCODE + CommonConstants.SPACE
            + CommonConstants.FROM + CommonConstants.SPACE + IBOUBTB_RATINGDETAILS.BONAME + " T1" + CommonConstants.COMMA
            + IBOUB_ALD_CURRENCYRATING.BONAME + " T2" + CommonConstants.SPACE + CommonConstants.WHERE + CommonConstants.SPACE
            + "T1." + IBOUBTB_RATINGDETAILS.UBRATINGDLTSIDPK + CommonConstants.EQUAL + "T2."
            + IBOUB_ALD_CURRENCYRATING.UBRATINGDLTSID;;

    private final String OVERALL_RATING_QUERY = CommonConstants.SELECT + CommonConstants.SPACE + "T1."
            + IBOUBTB_RATINGDETAILS.UBRATINGDLTSIDPK + " AS " + UBRATINGDLTSID + CommonConstants.COMMA + "T1."
            + IBOUBTB_RATINGDETAILS.UBRATINGCODE + " AS " + UBRATINGCODE + CommonConstants.COMMA + "T1."
            + IBOUBTB_RATINGDETAILS.UBRATINGTERM + " AS " + UBRATINGTERM + CommonConstants.COMMA + "T1."
            + IBOUBTB_RATINGDETAILS.UBRATINGVALUE + " AS " + UBRATINGVALUE + CommonConstants.COMMA + "T2."
            + IBOUB_ALD_OVERALLRATING.UBOVRLRATINGIDPK + " AS " + UBOVRLRATINGID + CommonConstants.COMMA + "T2."
            + IBOUB_ALD_OVERALLRATING.UBRATINGENTITYTYPE + " AS " + UBRATINGENTITYTYPE + CommonConstants.COMMA + "T2."
            + IBOUB_ALD_OVERALLRATING.UBRATINGENTITYID + " AS " + UBRATINGENTITYID + CommonConstants.SPACE + CommonConstants.FROM
            + CommonConstants.SPACE + IBOUBTB_RATINGDETAILS.BONAME + " T1" + CommonConstants.COMMA + IBOUB_ALD_OVERALLRATING.BONAME
            + " T2" + CommonConstants.SPACE + CommonConstants.WHERE + CommonConstants.SPACE + "T1."
            + IBOUBTB_RATINGDETAILS.UBRATINGDLTSIDPK + CommonConstants.EQUAL + "T2." + IBOUB_ALD_OVERALLRATING.UBRATINGDLTSID;
    private Timestamp processedTime;
    private String newFileName;

    /*
     * (non-Javadoc)
     *
     * @see
     * com.trapedza.bankfusion.steps.refimpl.AbstractUB_ALD_CaptureRating_BatchProcess_Fatom#process
     * (com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment)
     */
    public void process(BankFusionEnvironment env) {
        batchLogger = new BatchLogger();

        ratingDetailsMap = new HashMap<String, String>();
        overAllRatingMap = new HashMap<String, String>();
        currencyRatingMap = new HashMap<String, String>();
        IBOUB_ALD_OVERALLRATING overallRatingBO;
        IBOUB_ALD_CURRENCYRATING currencyRatingBO;
        factory = BankFusionThreadLocal.getPersistanceFactory();
        batchLocation = getF_IN_FilesLocation();
        //confLocation = System.getProperty("BFconfigLocation", CommonConstants.EMPTY_STRING);
        confLocation = GetUBConfigLocation.getUBConfigLocation();
        mappingFile = confLocation + "conf" + File.separator + "business" + File.separator + "ALD" + File.separator
                + "UB_ALD_CaptureRatingMappingFile.xml";
        File[] fileList = getFileList();
        //artf53218 changes start
        if (fileList == null){
        	EventsHelper.handleEvent(ChannelsEventCodes.E_INV_FILE_LOCATION_UB14, new Object[] {}, new HashMap(), env);
        	return;
        }
      //artf53218 changes end
        if (fileList.length == 0) {
            setF_OUT_Message(noFilesMessage + batchLocation);
            return;
        }

        RatingDetailsValidator validator = new RatingDetailsValidator(env);
        populateRatingDetails();
        for (int i = 0; i < fileList.length; i++) {
            newFileName = CommonConstants.EMPTY_STRING;
            processedTime = Timestamp.valueOf(CommonConstants.DEFAULT_DATETIME);
            parseXML(fileList[i]);
            Vector<RatingDetails> ratingDetails = captureRatingsBatchData.getRatingDetails();
            Integer count = CommonConstants.INTEGER_ZERO;
            Integer numberUploaded = ratingDetails.size();
            Integer numberFailed = CommonConstants.INTEGER_ZERO;
            for (Iterator<RatingDetails> iterator = ratingDetails.iterator(); iterator.hasNext();) {

                if (logger.isInfoEnabled()) {
                    logger.info("Processed Record : " + count++);
                }

                RatingDetails ratingDetails2 = iterator.next();
                OverallRating overallRating = ratingDetails2.getOverallRating();
                CurrencyRating oCurrencyRating = ratingDetails2.getCurrencyRating();

                if (validator.validAndLogErrors(ratingDetails2, captureRatingsBatchData.getBatchReference(), batchLogger)) {

                    if ((ratingDetails2.getEntityType().equals(RatingDetailsValidator.ENTITY_TYPE_CUSTOMER) || ratingDetails2
                            .getEntityType().equals(RatingDetailsValidator.ENTITY_TYPE_COUNTRY))) {
                        if (!oCurrencyRating.getCurrencyCode().equals(CommonConstants.EMPTY_STRING)) {

                            if (currencyRatingMap.containsKey(ratingDetails2.getEntityCode() + oCurrencyRating.getAgencyCode()
                                    + oCurrencyRating.getTerm() + oCurrencyRating.getCurrencyCode())) {
                                String currencyRatingID = currencyRatingMap.get(ratingDetails2.getEntityCode()
                                        + oCurrencyRating.getAgencyCode() + oCurrencyRating.getTerm()
                                        + oCurrencyRating.getCurrencyCode());
                                currencyRatingBO = (IBOUB_ALD_CURRENCYRATING) factory.findByPrimaryKey(
                                        IBOUB_ALD_CURRENCYRATING.BONAME, currencyRatingID, true);
                                currencyRatingBO.setF_UBRATINGDLTSID(ratingDetailsMap.get(oCurrencyRating.getAgencyCode()
                                        + oCurrencyRating.getTerm() + oCurrencyRating.getValue()));
                            }
                            else {
                                currencyRatingBO = (IBOUB_ALD_CURRENCYRATING) factory
                                        .getStatelessNewInstance(IBOUB_ALD_CURRENCYRATING.BONAME);
                                currencyRatingBO.setF_UBRATINGDLTSID(ratingDetailsMap.get(oCurrencyRating.getAgencyCode()
                                        + oCurrencyRating.getTerm() + oCurrencyRating.getValue()));
                                currencyRatingBO.setF_UBRATINGENTITYID(ratingDetails2.getEntityCode());
                                currencyRatingBO.setF_UBRATINGENTITYTYPE(ratingDetails2.getEntityType());
                                currencyRatingBO.setF_UBISOCURRENCYCODE(oCurrencyRating.getCurrencyCode());
                                factory.create(IBOUB_ALD_CURRENCYRATING.BONAME, currencyRatingBO);
                            }
                            currencyRatingMap.put(ratingDetails2.getEntityCode() + oCurrencyRating.getAgencyCode()
                                    + oCurrencyRating.getTerm() + oCurrencyRating.getCurrencyCode(), currencyRatingBO.getBoID());
                        }
                        if (!overallRating.getAgencyCode().equals(CommonConstants.EMPTY_STRING)
                                && !overallRating.getTerm().equals(CommonConstants.EMPTY_STRING)
                                && !overallRating.getValue().equals(CommonConstants.EMPTY_STRING)) {

                            if (overAllRatingMap.containsKey(ratingDetails2.getEntityCode() + overallRating.getAgencyCode()
                                    + overallRating.getTerm())) {
                                String overAllRatingID = overAllRatingMap.get(ratingDetails2.getEntityCode()
                                        + overallRating.getAgencyCode() + overallRating.getTerm());
                                overallRatingBO = (IBOUB_ALD_OVERALLRATING) factory.findByPrimaryKey(
                                        IBOUB_ALD_OVERALLRATING.BONAME, overAllRatingID, true);
                                overallRatingBO.setF_UBRATINGDLTSID(ratingDetailsMap.get(overallRating.getAgencyCode()
                                        + overallRating.getTerm() + overallRating.getValue()));
                            }
                            else {

                                overallRatingBO = (IBOUB_ALD_OVERALLRATING) factory
                                        .getStatelessNewInstance(IBOUB_ALD_OVERALLRATING.BONAME);
                                overallRatingBO.setF_UBRATINGDLTSID(ratingDetailsMap.get(overallRating.getAgencyCode()
                                        + overallRating.getTerm() + overallRating.getValue()));
                                overallRatingBO.setF_UBRATINGENTITYID(ratingDetails2.getEntityCode());
                                overallRatingBO.setF_UBRATINGENTITYTYPE(ratingDetails2.getEntityType());
                                factory.create(IBOUB_ALD_OVERALLRATING.BONAME, overallRatingBO);

                            }
                            factory.commitTransaction();
                            factory.beginTransaction();
                            overAllRatingMap.put(ratingDetails2.getEntityCode() + overallRating.getAgencyCode()
                                    + overallRating.getTerm(), overallRatingBO.getBoID());
                        }

                    }
                    else {
                        if (overAllRatingMap.containsKey(ratingDetails2.getEntityCode() + overallRating.getAgencyCode()
                                + overallRating.getTerm())) {
                            String overAllRatingID = overAllRatingMap.get(ratingDetails2.getEntityCode()
                                    + overallRating.getAgencyCode() + overallRating.getTerm());
                            overallRatingBO = (IBOUB_ALD_OVERALLRATING) factory.findByPrimaryKey(IBOUB_ALD_OVERALLRATING.BONAME,
                                    overAllRatingID, true);
                            overallRatingBO.setF_UBRATINGDLTSID(ratingDetailsMap.get(overallRating.getAgencyCode()
                                    + overallRating.getTerm() + overallRating.getValue()));
                        }
                        else {

                            overallRatingBO = (IBOUB_ALD_OVERALLRATING) factory
                                    .getStatelessNewInstance(IBOUB_ALD_OVERALLRATING.BONAME);
                            overallRatingBO.setF_UBRATINGDLTSID(ratingDetailsMap.get(overallRating.getAgencyCode()
                                    + overallRating.getTerm() + overallRating.getValue()));
                            overallRatingBO.setF_UBRATINGENTITYID(ratingDetails2.getEntityCode());
                            overallRatingBO.setF_UBRATINGENTITYTYPE(ratingDetails2.getEntityType());
                            factory.create(IBOUB_ALD_OVERALLRATING.BONAME, overallRatingBO);

                        }
                        factory.commitTransaction();
                        factory.beginTransaction();
                        overAllRatingMap.put(ratingDetails2.getEntityCode() + overallRating.getAgencyCode()
                                + overallRating.getTerm(), overallRatingBO.getBoID());

                    }

                }
                else {
                    numberFailed++;
                    error = true;
                }
            }
            batchLogger.createBatchFileLog(captureRatingsBatchData.getBatchReference()+i, newFileName, fileList[i].getAbsolutePath(),
                    captureRatingsBatchData.getDescription(), processedTime, numberUploaded, numberFailed);

        }

        if (error) {
            setF_OUT_Message(errorMessage);
        }
        else {
            setF_OUT_Message(successMessage);
        }

    }

    /**
     * Method Description: fetches all the xml files from the given batchLocation
     *
     * @return Array of Files
     */
    private File[] getFileList() {
        FileFilter xmlFilter = new FileFilter() {
            public boolean accept(File file) {
                String sFilePath = file.getName().toLowerCase();
                if (sFilePath.endsWith(".xml")) {
                    return true;
                }
                else {
                    return false;
                }
            }
        };
        File folder = new File(batchLocation);
        return folder.listFiles(xmlFilter);
    }

    /**
     * Method Description: Parses the XML File using Castor API
     *
     * @param dataFile
     * @throws BankFusionException
     */
    private void parseXML(File dataFile) throws BankFusionException {
        InputStream isMap = null;
        InputStream isConfig = null;
        String oldFileName = dataFile.getAbsolutePath();

        doXMLValidation(dataFile);
        try {
            isMap = new FileInputStream(mappingFile);
            isConfig = new FileInputStream(dataFile);
            ClassLoader cl = getClass().getClassLoader();
            Mapping mapping = new Mapping(cl);
            // 1. Load the mapping information from the file
            mapping.loadMapping(new InputSource(new InputStreamReader(isMap)));

            // 2. Unmarshal the data
            Unmarshaller unmar = new Unmarshaller(mapping);
            unmar.setClassLoader(cl);
            captureRatingsBatchData = (BatchInformation) unmar.unmarshal(new InputSource(new InputStreamReader(isConfig)));
        }
       
        catch (IOException e) {
        	logger.error(e);
            throw new BankFusionException(40000352, BankFusionMessages.getFormattedMessage(40000352, new Object[] { dataFile.getName(),
                    mappingFile }));
              }
        catch (MappingException e) {
        	logger.error(e);
            throw new BankFusionException(40000352, BankFusionMessages.getFormattedMessage(40000352, new Object[] { dataFile.getName(),
                    mappingFile }));
        }
        catch (ValidationException e) {
        	logger.error(e);
            throw new BankFusionException(40000352, BankFusionMessages.getFormattedMessage(40000352, new Object[] { dataFile.getName(),
                    mappingFile }));
        }
        catch (CastorException e) {
        	logger.error(e);
            throw new BankFusionException(40000352, BankFusionMessages.getFormattedMessage(40000352, new Object[] { dataFile.getName(),
                    mappingFile }));
        }
        finally {
        	if( isMap !=null){
        		closeReader(isMap, mappingFile);
        	}
        	if( isConfig!=null){
        		closeReader(isConfig, dataFile.getName());
        	}
        	renameFile(oldFileName);
        }
    }

    /**
     * Method Description: validates the XML File with the XSD schema.
     *
     * @param file
     */
    private void doXMLValidation(File file) {

        try {
            // define the type of schema - we use W3C:
            // get validation driver:
            SchemaFactory factory = SchemaFactory.newInstance(schemaLang);
            Schema schema = factory.newSchema(new StreamSource(confLocation + "conf/business/ALD/"
                    + CaptureRatingBatchProcessSchema));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(file));

        }
        catch (SAXException ex) {
            // we throw exception if the xml file validation fails
            logger.equals(ex);
            throw new BankFusionException(40609616, ex.getLocalizedMessage());
        }
        catch (IOException ex) {
        	logger.error(ex);
            throw new BankFusionException(40609616, ex.getLocalizedMessage());
        }
    }

    /**
     * Method Description: Closes the input stream of the file.
     *
     * @param fileStream
     * @param fileLoc
     * @throws BankFusionException
     */
    private void closeReader(InputStream fileStream, String fileLoc) throws BankFusionException {
        try {
            fileStream.close();
        }
        catch (NullPointerException e) {
        	logger.error(e);
            throw new BankFusionException(40000239, BankFusionMessages.getFormattedMessage(40000239, new Object[] { fileLoc }));
          }
        catch (IOException ioe) {
        	logger.error(ioe);
            throw new BankFusionException(40000239, BankFusionMessages.getFormattedMessage(40000239, new Object[] { fileLoc }));
        }
    }

    /**
     * Method Description: Renames the processes file and moves it to archive folder.
     *
     * @param oldFileName
     * @param newFileName
     */
    private void renameFile(String oldFileName) {

        try {
            String time = SystemInformationManager.getInstance().getBFBusinessDateTimeAsString();
            processedTime = Timestamp.valueOf(time);
            time = time.replace(':', '-').replace(' ', '_');
            File oldFile = new File(oldFileName);
            File newFile = new File(oldFileName + time);
            File archiveDir = new File(batchLocation + File.separator + BATCH_ARCHIVE);
            oldFile.renameTo(newFile);
            if (!archiveDir.exists()) {
                archiveDir.mkdirs();
                File archiveFile = new File(batchLocation + File.separator + BATCH_ARCHIVE + File.separator + newFile.getName());
                newFile.renameTo(archiveFile);
                newFileName = archiveFile.getAbsolutePath();
            }
            else {
                File archiveFile = new File(batchLocation + File.separator + BATCH_ARCHIVE + File.separator + newFile.getName());
                newFile.renameTo(archiveFile);
                newFileName = archiveFile.getAbsolutePath();
            }

            if (captureRatingsBatchData == null) {
                batchLogger.createBatchFileLog(GUIDGen.getNewGUID(), newFileName, oldFileName, NOTPROCESSED, processedTime,
                        CommonConstants.INTEGER_ZERO, CommonConstants.INTEGER_ZERO);
            }
        }
        catch (NullPointerException e) {
            logger.error(e);
        }
    }

    /**
     * Method Description: Populates the Overallrating, currency rating details and rating details
     * in to data structures.
     */
    private void populateRatingDetails() {
        List<SimplePersistentObject> overAllRatinglist = factory.executeGenericQuery(OVERALL_RATING_QUERY, null, null, false);
        for (Iterator<SimplePersistentObject> iterator = overAllRatinglist.iterator(); iterator.hasNext();) {
            SimplePersistentObject simplePersistentObject = iterator.next();
            Map<String, String> dataMap = simplePersistentObject.getDataMap();
            overAllRatingMap.put(dataMap.get(UBRATINGENTITYID) + dataMap.get(UBRATINGCODE) + dataMap.get(UBRATINGTERM), dataMap
                    .get(UBOVRLRATINGID));

        }

        List<SimplePersistentObject> currencyRatinglist = factory.executeGenericQuery(CURRENCY_RATING_QUERY, null, null, false);
        for (Iterator<SimplePersistentObject> iterator = currencyRatinglist.iterator(); iterator.hasNext();) {
            SimplePersistentObject simplePersistentObject = iterator.next();
            Map<String, String> dataMap = simplePersistentObject.getDataMap();
            currencyRatingMap.put(dataMap.get(UBRATINGENTITYID) + dataMap.get(UBRATINGCODE) + dataMap.get(UBRATINGTERM)
                    + dataMap.get(UBISOCURRENCYCODE), dataMap.get(UBCURRENCYRATINGID));

        }

        List<IBOUBTB_RATINGDETAILS> ratingDetailsList = factory.findAll(IBOUBTB_RATINGDETAILS.BONAME, null, false);
        for (Iterator<IBOUBTB_RATINGDETAILS> iterator = ratingDetailsList.iterator(); iterator.hasNext();) {
            IBOUBTB_RATINGDETAILS iboubtb_ratingdetails = iterator.next();
            ratingDetailsMap.put(iboubtb_ratingdetails.getF_UBRATINGCODE() + iboubtb_ratingdetails.getF_UBRATINGTERM()
                    + iboubtb_ratingdetails.getF_UBRATINGVALUE(), iboubtb_ratingdetails.getBoID());
        }
    }
}
