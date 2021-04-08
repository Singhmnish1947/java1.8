package com.trapedza.bankfusion.fatoms;

import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.misys.ub.batchgateway.persistence.PrivatePersistenceFactory;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_INF_CONTROLFILEDETAILS;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.persistence.services.IPersistenceService;
import com.trapedza.bankfusion.servercommon.services.IServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManager;
import com.trapedza.bankfusion.servercommon.services.ServiceManagerFactory;
import com.trapedza.bankfusion.utils.GUIDGen;

public class UB_INF_ControlFileInformationProcessing {

	private static final transient Log logger = LogFactory
			.getLog(UB_INF_ControlFileInformationProcessing.class.getName());

	static final String READ_MODULE_CONFIGURATION = "CB_CMN_ReadModuleConfiguration_SRV";
	private final IServiceManager SERVICE_MANAGER = ServiceManagerFactory.getInstance().getServiceManager();

	private final IPersistenceService PERSISTENCE_SERVICE = (IPersistenceService) SERVICE_MANAGER
			.getServiceForName(ServiceManager.PERSISTENCE_SERVICE);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void postMessage(Exchange exchange) {
		logger.info("start of UB_INF_ControlFileInformationProcessing");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		UB_INF_LIQEODPaymentPosting liqEOD = new UB_INF_LIQEODPaymentPosting();
		try {

			Message params = exchange.getIn();
			String requestMsg = params.getBody().toString();
			dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource inputsource = new InputSource(new StringReader(requestMsg));
			Document doc = dBuilder.parse(inputsource);
			NodeList list = doc.getElementsByTagName("ControlFileInformation");
			insertControlFileDetails(list);
			liqEOD.checkAndNotifyControlFile();
		} catch (Exception e) {
			logger.error(e);
		}
		logger.info("End of UB_INF_ControlFileInformationProcessing");
	}

	private void insertControlFileDetails(NodeList nodeList) throws ParseException {
		logger.info("start of insertControlFileDetails");
		String messageId = null;

		List<SimplePersistentObject> controlFileDetailsList = new ArrayList<>();
		if (nodeList == null || nodeList.getLength() <= 0) {
		}
		IPersistenceObjectsFactory privateFactory = PERSISTENCE_SERVICE.getPrivatePersistenceFactory(false);
		PrivatePersistenceFactory persistancefactory = new PrivatePersistenceFactory();
		try {
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
			for (int i = 0; i < nodeList.getLength(); i++) {
				IBOUB_INF_CONTROLFILEDETAILS controlFileDetail = (IBOUB_INF_CONTROLFILEDETAILS) privateFactory
						.getStatelessNewInstance(IBOUB_INF_CONTROLFILEDETAILS.BONAME);
				if (controlFileDetail != null) {

					messageId = GUIDGen.getNewGUID();
					controlFileDetail.setBoID(messageId);
					controlFileDetail.setF_CONTROLFILENAME(getNodeValue(nodeList.item(i), "ControlFileName"));
					controlFileDetail.setF_LOANIQBRANCH(getNodeValue(nodeList.item(i), "LIQSourceBranch"));
					controlFileDetail.setF_FILESCOUNT(Integer.parseInt((getNodeValue(nodeList.item(i), "FileCount"))));
					controlFileDetail.setF_TRANSACTIONCOUNT(
							Integer.parseInt((getNodeValue(nodeList.item(i), "TransactionCount"))));
					java.util.Date u = sf.parse(getNodeValue(nodeList.item(i), "Date"));
					java.sql.Date s = new java.sql.Date(u.getTime());
					controlFileDetail.setF_PROCESSINGDATE(s);
					controlFileDetailsList.add(controlFileDetail);
				}

			}
			persistancefactory.create(controlFileDetailsList);
		} catch (Exception e) {
			logger.error(e);
			privateFactory.rollbackTransaction();

		} finally {
			privateFactory.closePrivateSession();
		}

		logger.info("End of insertControlFileDetails");

	}

	private static String getNodeValue(Node node, String nodeName) {

		NodeList list = node.getChildNodes();
		String nodeValue = null;

		for (int i = 0; i < list.getLength(); i++) {

			if (list.item(i).getChildNodes().getLength() > 1) {

				nodeValue = getNodeValue(list.item(i), nodeName);

				if (nodeValue != null) {
					return nodeValue;
				}

			} else {

				if ((list.item(i).getNodeName().equals(nodeName)) && (list.item(i).getFirstChild() != null)) {

					nodeValue = list.item(i).getFirstChild().getTextContent();
				}
			}
		}

		return nodeValue;
	}

}
