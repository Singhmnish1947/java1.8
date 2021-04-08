package com.misys.ub.dc.common;

import com.misys.bankfusion.subsystem.messaging.jms.runtime.impl.MessageProducerUtil;
import com.trapedza.bankfusion.fatoms.AccountDetailsWithLob;

import bf.com.misys.cbs.types.AccountMandateDetails;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


public class AccountPushToDC {
    private Log logger = LogFactory.getLog(AccountPushToDC.class.getName());


    public void pushAccountToDCQueue(AccountDetailsWithLob accountDetailsWithLob) throws ParserConfigurationException {
        
        logger.info("---------------Entered into Push Account To DC Queue--------------");
        
        String id = accountDetailsWithLob.getAccountInterfacesRs().getAccountDetailsOverviewList().get(0)
                .getExtensiveAccountDetails().getAccountBasicDetails().getAccountKeys().getStandardAccountId();

        String customerId = accountDetailsWithLob.getAccountInterfacesRs().getAccountDetailsOverviewList().get(0)
                .getExtensiveAccountDetails().getAccountBasicDetails().getCustomerShortDetails().getCustomerId();

        String productCode = accountDetailsWithLob.getAccountInterfacesRs().getAccountDetailsOverviewList().get(0)
                .getExtensiveAccountDetails().getAccountBasicDetails().getSubProductId();

        String status = "";
        Boolean isClosed = accountDetailsWithLob.getAccountInterfacesRs().getAccountDetailsOverviewList().get(0)
                .getExtensiveAccountDetails().getAcctCharacteristics().getIsClosed();

        Boolean isDormant = accountDetailsWithLob.getAccountInterfacesRs().getAccountDetailsOverviewList().get(0)
                .getExtensiveAccountDetails().getAcctCharacteristics().getIsDormant();
        Boolean isStopped = accountDetailsWithLob.getAccountInterfacesRs().getAccountDetailsOverviewList().get(0)
                .getExtensiveAccountDetails().getAcctCharacteristics().getIsStoped();

        AccountMandateDetails[] accountRelationList = accountDetailsWithLob.getAccountInterfacesRs().getAccountDetailsOverviewList()
                .get(0).getExtensiveAccountDetails().getListMandateDetails().getAccountMandateDtls();

        if (isClosed == false && isDormant == false && isStopped == false) {
            status = "NORMAL";
        }
        else {
            status = "BLOCKED";
        }
        
        String currency =  accountDetailsWithLob.getAccountInterfacesRs().getAccountDetailsOverviewList().get(0)
                .getExtensiveAccountDetails().getAccountBasicDetails().getAccountKeys().getPseudonym().getIsoCurrencyCode();
        
        String modeOfOperation = modeOfOperation(accountDetailsWithLob.getAccountInterfacesRs().getAccountDetailsOverviewList().get(0)
                .getExtensiveAccountDetails().getAcctCharacteristics().getModeOfOperation());
        
       

        String accountType = "SINGLE";
        if (accountDetailsWithLob.getAccountInterfacesRs().getAccountDetailsOverviewList().get(0).getExtensiveAccountDetails()
                .getAcctCharacteristics().getIsJoint() == true) {

            accountType = "JOINT";
        }

        Timestamp startDate = accountDetailsWithLob.getAccountInterfacesRs().getAccountDetailsOverviewList().get(0)
                .getExtensiveAccountDetails().getAccountBasicDetails().getDateOpened();

        

        String message = documentBuilder(id, customerId, productCode, status, currency, modeOfOperation, accountType, startDate,
                accountRelationList);
        postToQueue(message, "RECIEVEQUEUE");

      
        }


    private void postToQueue(String message, String queueEndpoint) {
        MessageProducerUtil.sendMessage(message, queueEndpoint);
    }

    public String documentBuilder(String id, String customerId, String productCode, String status, String currency,
            String modeOfOperation, String accountType, Timestamp startDate, AccountMandateDetails[] accountRelationList)
            throws ParserConfigurationException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        DocumentBuilder loader = factory.newDocumentBuilder();
        Document document = loader.newDocument();



        Element order = document.createElementNS(AccountDCPuchConst.docNS, "p:PushAccountsRequest");
        document.appendChild(order);
        order.setAttribute("xmlns:p", AccountDCPuchConst.docNS);
        order.setAttribute("xmlns:p1", AccountDCPuchConst.docNS1);
        order.setAttribute("xmlns:p2", AccountDCPuchConst.docNS2);
        order.setAttribute("xmlns:p3", AccountDCPuchConst.docNS3);
        order.setAttribute("xmlns:xsi", AccountDCPuchConst.docNS4);
        order.setAttribute("xsi:schemaLocation", AccountDCPuchConst.docNS5);

        Element account = createElement(document, "Account");
        order.appendChild(account);

        Element id1 = createElement(document, "ID");
        id1.appendChild(document.createTextNode(id));
        account.appendChild(id1);

        Element custId = document.createElement("CustomerId");
        custId.appendChild(document.createTextNode(customerId));
        account.appendChild(custId);

        Element prodCode = document.createElement("ProductCode");
        prodCode.appendChild(document.createTextNode(productCode));
        account.appendChild(prodCode);

        Element status1 = document.createElement("Status");
        status1.appendChild(document.createTextNode(status));
        account.appendChild(status1);

        Element currency1 = document.createElement("Currency");
        currency1.appendChild(document.createTextNode(currency));
        account.appendChild(currency1);

        Element accountNumberLocal = document.createElement("AccountNumberLocal");
        accountNumberLocal.appendChild(document.createTextNode(id));
        account.appendChild(accountNumberLocal);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Element accountOpeningDate = document.createElement("AccountOpeningDate");
        accountOpeningDate.appendChild(document.createTextNode(sdf.format(startDate)));
        account.appendChild(accountOpeningDate);


        Element modeOfOperation1 = document.createElement("ModeOfOperation");
        modeOfOperation1.appendChild(document.createTextNode(modeOfOperation));
        account.appendChild(modeOfOperation1);

        Element accountType1 = document.createElement("AccountType");
        accountType1.appendChild(document.createTextNode(accountType));
        account.appendChild(accountType1);

        for (int i = 0; i <= accountRelationList.length - 1; i++) {
            Element accountRelationList1 = document.createElement("AccountRelationList");
            account.appendChild(accountRelationList1);

            Element accountId = document.createElement("AccountId");
            accountId.appendChild(document.createTextNode(id));
            accountRelationList1.appendChild(accountId);

            Element CustomerId1 = document.createElement("CustomerId");
            if (accountRelationList[i].getRole() == "OWNER" || accountRelationList[i].getRole() == "JOINTACHOLDER") {
                CustomerId1.appendChild(document.createTextNode(accountRelationList[i].getCustID()));
                accountRelationList1.appendChild(CustomerId1);
            }
            else {
                CustomerId1.appendChild(document.createTextNode(customerId));
                accountRelationList1.appendChild(CustomerId1);
            }

            Element roleOwnerId = document.createElement("RoleOwnerId");
            roleOwnerId.appendChild(document.createTextNode(accountRelationList[i].getCustID()));
            accountRelationList1.appendChild(roleOwnerId);

            Element role = document.createElement("Role");
            role.appendChild(document.createTextNode(essenceToDCRole(accountRelationList[i].getRole())));
            accountRelationList1.appendChild(role);
        }
        return documentToString(document);
    }

    private String documentToString(Document document) {
        try {
        	TransformerFactory factory = TransformerFactory.newInstance();
        	factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        	Transformer transformer = factory.newTransformer();
            StringWriter sw = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(sw));
            return sw.toString();
        }
        catch (TransformerException tEx) {
            tEx.printStackTrace();
        }
        return null;
    }
    public String essenceToDCRole(String essenceRole) {
        switch (essenceRole) {
            case AccountDCPuchConst.OWNER:
                essenceRole = AccountDCPuchConst.OWNER;
                break;
            case AccountDCPuchConst.AUTHORIZEDONE:
                essenceRole = AccountDCPuchConst.AUTHORIZED;
                break;
            case AccountDCPuchConst.LEGALREPRESTATIVE:
                essenceRole = AccountDCPuchConst.SIGNATORY;
                break;
            case AccountDCPuchConst.SIGNATORY:
                essenceRole = AccountDCPuchConst.SIGNATORY;
                break;
            case AccountDCPuchConst.POWEROFATTORNEY:
                essenceRole = AccountDCPuchConst.POWER_OF_ATTORNEY;
                break;
            case AccountDCPuchConst.JOINTACHOLDER:
                essenceRole = AccountDCPuchConst.JOINT_OWNER;
                break;
            case AccountDCPuchConst.GUARANTOR:
                essenceRole = AccountDCPuchConst.GUARANTOR;


        }
        return essenceRole;
    }

    public String modeOfOperation(String modeOfOperation) {
        switch (modeOfOperation) {
            case AccountDCPuchConst.SINGLE:
                modeOfOperation = AccountDCPuchConst.SINGLE;
                break;
            case AccountDCPuchConst.EITHERORSURVIVOR:
                modeOfOperation = AccountDCPuchConst.EITHER_OR_SURVIVOR;
                break;
            case AccountDCPuchConst.JOINTLYSEVERALLY:
                modeOfOperation = AccountDCPuchConst.JOINTLY;
                break;
            case AccountDCPuchConst.ANYORSURVIVOR:
                modeOfOperation = AccountDCPuchConst.ANYONE_OR_SURVIVOR;
                break;
            case AccountDCPuchConst.ANYTWOJOINTLY:
                modeOfOperation = AccountDCPuchConst.ANY_TWO_JOINTLY;
                break;
            default:
                modeOfOperation = AccountDCPuchConst.SINGLE;
        }
        return modeOfOperation;

}

    private Element createElement(Document document, String elementName) {
        return document.createElement(elementName);
    }
}
