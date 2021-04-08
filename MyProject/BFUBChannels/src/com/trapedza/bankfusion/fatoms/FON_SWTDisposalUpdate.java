package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.fontis.FON_BatchRecord;
import com.misys.ub.fontis.FON_CreditRecord;
import com.misys.ub.fontis.FON_DebitRecord;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.FinderMethods;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;

/**
 * @author Gaurav.Aggarwal
 *
 */
public class FON_SWTDisposalUpdate {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    HashMap inputmap = new HashMap();
    HashMap outputmap = new HashMap();
    private static final String GETADDRESS = "GetAddress";
    private static final String CUSTOMERCODE = "CustomerCode";
    private static final String SWT_DISPOSAL_ADDRESSLINE1 = "ADDRESSLINE1";
    private static final String SWT_DISPOSAL_ADDRESSLINE2 = "ADDRESSLINE2";
    private static final String SWT_DISPOSAL_ADDRESSLINE3 = "ADDRESSLINE3";
    String addline1 = "";
    String addline2 = "";
    String addline3 = "";
    public static final String MICROFLOW_NAME = "UB_SWT_MessageValidator_SRV";
    public static final String BROKER = "Broker";
    public static final String CURRENCY = "CURRENCY";
    public static final String CHARGEAMOUNT = "ChargeAmount";
    public static final String CHARGECURRENCY = "ChargeCurrency";
    public static final String CHARGECODE = "ChargeCode";
    public static final String CONTRAAMOUNT = "ContraAmount";
    public static final String CONTRAACCOUNT = "Contra_Account";
    public static final String CUSTOMERNUMBER = "Customer_Number";
    public static final String DRCRCONFIRMFLAG = "DRCRConfirmFlag";
    public static final String DAYCOUNTFRACTION = "DayCountFraction";
    public static final String DEALNUMBER = "Deal_Number";
    public static final String DRAFTNUMBER = "DraftNumber";
    public static final String FXTRANSACTION = "FXTransaction";
    public static final String INTCAPITALPAYAWAY = "IntCapitalPayAway";
    public static final String INTERESTPERIODENDDATE = "InterestPeriodEndDate";
    public static final String INTERESTPERIODSTARTDATE = "InterestPeriodStartDate";
    public static final String INTERESTAMOUNT = "Interest_Amount";
    public static final String INTERESTRATE = "Interest_Rate";
    public static final String MAINACCOUNT = "Main_account";
    public static final String MATURITYDATE = "Maturity_Date";
    public static final String MESSAGETYPE = "MessageType";
    public static final String NEXTINTERESTDUEDATE = "NextInterestDueDate";
    public static final String PAYRECEIVEFLAG = "pay_Receive_Flag";
    public static final String POSTDATE = "Post_Date";
    public static final String PRINCIPLEINCREASEDECREASE = "PrincipleIncreaseDecrease";
    public static final String PURCHASECURRENCY = "PurchaseCurrency";
    public static final String ROLLOVEROPTION = "ROLLOVEROPTION";
    public static final String BANKINSTRUCTIONCODE = "BankInstructionCode";
    public static final String BANKINSTRUCTIONCODETEXT = "BankInstructionCodeText";
    public static final String BANKOPERATIONCODE = "BankOperationCode";
    public static final String BANKTOBANKINFORMATION1 = "BankToBankInformation1";
    public static final String BANKTOBANKINFORMATION2 = "BankToBankInformation2";
    public static final String BANKTOBANKINFORMATION3 = "BankToBankInformation3";
    public static final String BANKTOBANKINFORMATION4 = "BankToBankInformation4";
    public static final String BANKTOBANKINFORMATION5 = "BankToBankInformation5";
    public static final String BANKTOBANKINFORMATION6 = "BankToBankInformation6";
    public static final String BENEFICIARYCUSTOMERIDENTIFIERCODE = "BeneficiaryCustomerIdentifierCode";
    public static final String BENEFICIARYCUSTOMERPARTYIDENTIFIER = "BeneficiaryCustomerPartyIdentifier";
    public static final String BENEFICIARYINSTITUTEIDENTIFIERCODE = "BeneficiaryInstituteIdentifierCode";
    public static final String BENEFICIARYINSTITUTEPARTYIDENTIFIER = "BeneficiaryInstitutePartyIdentifier";
    public static final String BENEFICIARYCUSTOMERTEXT1 = "BeneficiaryCustomerText1";
    public static final String BENEFICIARYCUSTOMERTEXT2 = "BeneficiaryCustomerText2";
    public static final String BENEFICIARYCUSTOMERTEXT3 = "BeneficiaryCustomerText3";
    public static final String BENEFICIARYCUSTOMERTEXT4 = "BeneficiaryCustomerText4";
    public static final String BENEFICIARYINSTITUTETEXT1 = "BeneficiaryInstituteText1";
    public static final String BENEFICIARYINSTITUTETEXT2 = "BeneficiaryInstituteText2";
    public static final String BENEFICIARYINSTITUTETEXT3 = "BeneficiaryInstituteText3";
    public static final String BENEFICIARYINSTITUTETEXT4 = "BeneficiaryInstituteText4";
    public static final String GENERATE103PLUS = "Generate103Plus";
    public static final String INTERMEDIARYIDENTIFIERCODE = "IntermediaryIdentifierCode";
    public static final String INTERMEDIARYPARTYIDENTIFIER = "IntermediaryPartyIdentifier";
    public static final String INTERMEDIARYTEXT1 = "IntermediaryText1";
    public static final String INTERMEDIARYTEXT2 = "IntermediaryText2";
    public static final String INTERMEDIARYTEXT3 = "IntermediaryText3";
    public static final String INTERMEDIARYTEXT4 = "IntermediaryText4";
    public static final String KEYCURRENCY = "KeyCurrency";
    public static final String NARRATIVE = "Narrative";
    public static final String ORDERINGCUSTDTL1 = "ORDERING_CUSTDTL1";
    public static final String ORDERINGCUSTDTL2 = "ORDERING_CUSTDTL2";
    public static final String ORDERINGCUSTDTL3 = "ORDERING_CUSTDTL3";
    public static final String ORDERINGCUSTDTL4 = "ORDERING_CUSTDTL4";
    public static final String ORDERINGCUSTOMERIDENTIFIERCODE = "OrderingCustomerIdentifierCode";
    public static final String ORDERINGCUSTOMERPARTYIDENTIFIER = "OrderingCustomerPartyIdentifier";
    public static final String ORDERINGINSTITUTEIDENTIFIERCODE = "OrderingInstituteIdentifierCode";
    public static final String ORDERINGINSTITUTENAMEANDADDRESS1 = "OrderingInstituteNameAndAddress1";
    public static final String ORDERINGINSTITUTENAMEANDADDRESS2 = "OrderingInstituteNameAndAddress2";
    public static final String ORDERINGINSTITUTENAMEANDADDRESS3 = "OrderingInstituteNameAndAddress3";
    public static final String ORDERINGINSTITUTENAMEANDADDRESS4 = "OrderingInstituteNameAndAddress4";
    public static final String ORDERINGINSTITUTEPARTYIDENTIFIER = "OrderingInstitutePartyIdentifier";
    public static final String PARTYIDENTIFIER = "PartyIdentifier";
    public static final String PARTYIDENTIFIERADDRESS1 = "PartyIdentifierAddress1";
    public static final String PARTYIDENTIFIERADDRESS2 = "PartyIdentifierAddress2";
    public static final String PARTYIDENTIFIERADDRESS3 = "PartyIdentifierAddress3";
    public static final String PARTYIDENTIFIERADDRESS4 = "PartyIdentifierAddress4";
    public static final String PARTYIDENTIFIERTEXT = "PartyIdentifierText";
    public static final String PAYTOIDENTIFIERCODE = "PayToIdentifierCode";
    public static final String PAYTOPARTYIDENTIFIER = "PayToPartyIdentifier";
    public static final String PAYTOTEXT1 = "PayToText1";
    public static final String PAYTOTEXT2 = "PayToText2";
    public static final String PAYTOTEXT3 = "PayToText3";
    public static final String PAYTOTEXT4 = "PayToText4";
    public static final String PAYMENTDETAILS1 = "PayMentDetails1";
    public static final String PAYMENTDETAILS2 = "PayMentDetails2";
    public static final String PAYMENTDETAILS3 = "PayMentDetails3";
    public static final String PAYMENTDETAILS4 = "PayMentDetails4";
    public static final String RELATEDDEALNUMBER = "RelatedDealNumber";
    public static final String SENDERTORECEIVERINFORMATION1 = "SenderToReceiverInformation1";
    public static final String SENDERTORECEIVERINFORMATION2 = "SenderToReceiverInformation2";
    public static final String SENDERTORECEIVERINFORMATION3 = "SenderToReceiverInformation3";
    public static final String SENDERTORECEIVERINFORMATION4 = "SenderToReceiverInformation4";
    public static final String SENDERTORECEIVERINFORMATION5 = "SenderToReceiverInformation5";
    public static final String SENDERTORECEIVERINFORMATION6 = "SenderToReceiverInformation6";
    public static final String SETTLINSTRUCTIONNUMBER = "Settl_Instruction_Number";
    public static final String SOLDCURRENCY = "SoldCurrency";
    public static final String TERMSANDCONDITIONS1 = "TermsAndConditions1";
    public static final String TERMSANDCONDITIONS2 = "TermsAndConditions2";
    public static final String TERMSANDCONDITIONS3 = "TermsAndConditions3";
    public static final String TERMSANDCONDITIONS4 = "TermsAndConditions4";
    public static final String TERMSANDCONDITIONS5 = "TermsAndConditions5";
    public static final String TERMSANDCONDITIONS6 = "TermsAndConditions6";
    public static final String TRANSACTIONID = "TransactionID";
    public static final String TRANSACTIONAMOUNT = "Transaction_Amount";
    public static final String TRANSACTIONCODE = "TransactionCode";
    public static final String VALUEDATE = "Value_Date";
    public static final String VERIFYFLAG = "Verify_Flag";
    public static final String WALKINCUSTOMER = "WalkInCustomer";
    public static final String ZERO = "ZERO";
    public static final String CODEWORD = "code_word";
    public static final String PAYAWAYINTERESTACCOUNT = "payawayinterestaccount";
    public static final String PAYAWAYPRINCIPLEACCOUNTNUMBER = "payawayprincipleAccountnumber";
    public static final String TERM = "term";
    private static final String Originate_Deal = "NEW";
    private static final String CRED = "CRED";
    public static final int STATUS_AUTHORISED = 1;
    public static final String MESSAGE_TYPE = "103";
    private transient final static Log logger = LogFactory.getLog(FON_SWTDisposalUpdate.class.getName());

    // Changes start for artf53453
    public void SWTDisposalUpdate(BankFusionEnvironment env, FON_BatchRecord fontisBatch, String transactionID, String accountID) {
        // Changes end for artf53453
        for (int i = 0; i < fontisBatch.getCreditRecords().size(); i++) {
            if (((FON_CreditRecord) fontisBatch.getCreditRecords().get(i)).isSWIFTMessage()) {
                FON_CreditRecord fontis = (FON_CreditRecord) fontisBatch.getCreditRecords().get(i);
                // Changes start for artf53453
                setValues(env, fontis, fontisBatch, accountID, transactionID);
                // Changes end for artf53453
                callSWIFTInterface(env);
            }
        }
    }

    // Changes start for artf53453
    private void setValues(BankFusionEnvironment env, FON_CreditRecord fontis, FON_BatchRecord fontisBatch, String accountID,
            String transactionID) {
        // Changes end for artf53453
        setBankToBankInformation(fontisBatch);
        setSenderToReceiverInformation();
        setTermsAndConditions();
        setPayToDetails(fontis);
        setPaymentDetails(fontis);
        setIntermediaryDetails(fontis);
        setBeneficiaryCustomerDetails(fontis);
        setBeneficiaryInstituteDetails(fontis);
        setOrderingInstitutionDetails(fontis);
        setOrderingCustomerDetails(fontis);
        setPartyIdentifierDetails(env, fontisBatch);
        // Changes start for artf53453
        setOtherValues(fontis, fontisBatch, accountID, transactionID);
        // Changes end for artf53453
    }

    private void setPayToDetails(FON_CreditRecord fontisBatch) {
        inputmap.put(PAYTOPARTYIDENTIFIER, ((FON_CreditRecord) fontisBatch).getBeneficiaryAccountCode());
        inputmap.put(PAYTOIDENTIFIERCODE, CommonConstants.EMPTY_STRING);
        inputmap.put(PAYTOTEXT1, ((FON_CreditRecord) fontisBatch).getBeneficiaryBankName());
        inputmap.put(PAYTOTEXT2, ((FON_CreditRecord) fontisBatch).getBeneficiaryBankAddress());
        inputmap.put(PAYTOTEXT3, ((FON_CreditRecord) fontisBatch).getBeneficiaryAddress());
        inputmap.put(PAYTOTEXT4, CommonConstants.EMPTY_STRING);
    }

    private void setPaymentDetails(FON_CreditRecord fontisBatch) {
        inputmap.put(PAYMENTDETAILS1, CommonConstants.EMPTY_STRING);
        inputmap.put(PAYMENTDETAILS2, CommonConstants.EMPTY_STRING);
        inputmap.put(PAYMENTDETAILS3, CommonConstants.EMPTY_STRING);
        inputmap.put(PAYMENTDETAILS4, CommonConstants.EMPTY_STRING);
    }

    private void setIntermediaryDetails(FON_CreditRecord fontisBatch) {
        inputmap.put(INTERMEDIARYPARTYIDENTIFIER, ((FON_CreditRecord) fontisBatch).getIntermediaryBankAccountNo());
        inputmap.put(INTERMEDIARYIDENTIFIERCODE, ((FON_CreditRecord) fontisBatch).getIntermediaryBankCode());
        inputmap.put(INTERMEDIARYTEXT1, ((FON_CreditRecord) fontisBatch).getIntermediaryBankName());
        inputmap.put(INTERMEDIARYTEXT2, ((FON_CreditRecord) fontisBatch).getIntermediaryBankCity());
        inputmap.put(INTERMEDIARYTEXT3, ((FON_CreditRecord) fontisBatch).getIntermediaryBankType());
        inputmap.put(INTERMEDIARYTEXT4, CommonConstants.EMPTY_STRING);
    }

    private void setBeneficiaryCustomerDetails(FON_CreditRecord fontisBatch) {
        inputmap.put(BENEFICIARYCUSTOMERPARTYIDENTIFIER, ((FON_CreditRecord) fontisBatch).getBeneficiaryRefNo());
        inputmap.put(BENEFICIARYCUSTOMERIDENTIFIERCODE, CommonConstants.EMPTY_STRING);
        inputmap.put(BENEFICIARYCUSTOMERTEXT1, ((FON_CreditRecord) fontisBatch).getBeneficiaryName());
        inputmap.put(BENEFICIARYCUSTOMERTEXT2, ((FON_CreditRecord) fontisBatch).getBeneficiaryAddress());
        inputmap.put(BENEFICIARYCUSTOMERTEXT3, CommonConstants.EMPTY_STRING);
        inputmap.put(BENEFICIARYCUSTOMERTEXT4, CommonConstants.EMPTY_STRING);
    }

    private void setBeneficiaryInstituteDetails(FON_CreditRecord fontisBatch) {
        inputmap.put(BENEFICIARYINSTITUTEPARTYIDENTIFIER, ((FON_CreditRecord) fontisBatch).getBeneficiaryAccountCode());
        inputmap.put(BENEFICIARYINSTITUTEIDENTIFIERCODE, ((FON_CreditRecord) fontisBatch).getBeneficiaryBankCode());
        inputmap.put(BENEFICIARYINSTITUTETEXT1, ((FON_CreditRecord) fontisBatch).getBeneficiaryBankName());
        inputmap.put(BENEFICIARYINSTITUTETEXT2, ((FON_CreditRecord) fontisBatch).getBeneficiaryBankType());
        inputmap.put(BENEFICIARYINSTITUTETEXT3, ((FON_CreditRecord) fontisBatch).getBeneficiaryBankAddress());
        inputmap.put(BENEFICIARYINSTITUTETEXT4, CommonConstants.EMPTY_STRING);
    }

    private void setOrderingInstitutionDetails(FON_CreditRecord fontisBatch) {
        inputmap.put(ORDERINGINSTITUTEPARTYIDENTIFIER, CommonConstants.EMPTY_STRING);
        inputmap.put(ORDERINGINSTITUTEIDENTIFIERCODE, CommonConstants.EMPTY_STRING);
        inputmap.put(ORDERINGINSTITUTENAMEANDADDRESS1, CommonConstants.EMPTY_STRING);
        inputmap.put(ORDERINGINSTITUTENAMEANDADDRESS2, CommonConstants.EMPTY_STRING);
        inputmap.put(ORDERINGINSTITUTENAMEANDADDRESS3, CommonConstants.EMPTY_STRING);
        inputmap.put(ORDERINGINSTITUTENAMEANDADDRESS4, CommonConstants.EMPTY_STRING);
    }

    private void setOrderingCustomerDetails(FON_CreditRecord fontisBatch) {
        inputmap.put(ORDERINGCUSTDTL1, CommonConstants.EMPTY_STRING);
        inputmap.put(ORDERINGCUSTDTL2, CommonConstants.EMPTY_STRING);
        inputmap.put(ORDERINGCUSTDTL3, CommonConstants.EMPTY_STRING);
        inputmap.put(ORDERINGCUSTDTL4, CommonConstants.EMPTY_STRING);
    }

    private void setPartyIdentifierDetails(BankFusionEnvironment env, FON_BatchRecord fontisBatch) {
        getAddress(findCustomerCode(((FON_DebitRecord) fontisBatch.getDebitRecords().get(0)).getDebitAccountNo()), env);
        findCustomerName(((FON_DebitRecord) fontisBatch.getDebitRecords().get(0)).getDebitAccountNo());
        inputmap.put(PARTYIDENTIFIER, CommonConstants.EMPTY_STRING);
        inputmap.put(PARTYIDENTIFIERTEXT, CommonConstants.EMPTY_STRING);
        inputmap.put(PARTYIDENTIFIERADDRESS1,
                findCustomerName(((FON_DebitRecord) fontisBatch.getDebitRecords().get(0)).getDebitAccountNo()));
        inputmap.put(PARTYIDENTIFIERADDRESS2, addline1);
        inputmap.put(PARTYIDENTIFIERADDRESS3, addline2);
        inputmap.put(PARTYIDENTIFIERADDRESS4, addline3);
        inputmap.put(ORDERINGCUSTOMERPARTYIDENTIFIER,
                ("/" + ((FON_DebitRecord) fontisBatch.getDebitRecords().get(0)).getDebitAccountNo()));
        inputmap.put(ORDERINGCUSTOMERIDENTIFIERCODE, CommonConstants.EMPTY_STRING);
    }

    private void setBankToBankInformation(FON_BatchRecord fontisBatch) {
        if (CommonConstants.EMPTY_STRING.equals(fontisBatch.getBankToBankInfo())) {
            inputmap.put(BANKTOBANKINFORMATION1, CommonConstants.EMPTY_STRING);
            inputmap.put(BANKTOBANKINFORMATION2, CommonConstants.EMPTY_STRING);
            inputmap.put(BANKTOBANKINFORMATION3, CommonConstants.EMPTY_STRING);
            inputmap.put(BANKTOBANKINFORMATION4, CommonConstants.EMPTY_STRING);
            inputmap.put(BANKTOBANKINFORMATION5, CommonConstants.EMPTY_STRING);
            inputmap.put(BANKTOBANKINFORMATION6, CommonConstants.EMPTY_STRING);
        }
        else {
            if (fontisBatch.getBankToBankInfo().length() >= 35) {
                inputmap.put(BANKTOBANKINFORMATION1, fontisBatch.getBankToBankInfo().substring(0, 35));
            }
            else {
                inputmap.put(BANKTOBANKINFORMATION1, fontisBatch.getBankToBankInfo());
            }

            if (fontisBatch.getBankToBankInfo().length() >= 70) {
                inputmap.put(BANKTOBANKINFORMATION2, fontisBatch.getBankToBankInfo().substring(35, 70));
            }
            else if (fontisBatch.getBankToBankInfo().length() >= 35) {
                inputmap.put(BANKTOBANKINFORMATION2,
                        fontisBatch.getBankToBankInfo().substring(35, fontisBatch.getBankToBankInfo().length() - 1));
            }

            if (fontisBatch.getBankToBankInfo().length() > 105) {
                inputmap.put(BANKTOBANKINFORMATION3, fontisBatch.getBankToBankInfo().substring(70, 105));
            }
            else if (fontisBatch.getBankToBankInfo().length() > 70) {
                inputmap.put(BANKTOBANKINFORMATION3,
                        fontisBatch.getBankToBankInfo().substring(70, fontisBatch.getBankToBankInfo().length() - 1));
            }

            if (fontisBatch.getBankToBankInfo().length() >= 140) {
                inputmap.put(BANKTOBANKINFORMATION4, fontisBatch.getBankToBankInfo().substring(105, 140));
            }
            else if (fontisBatch.getBankToBankInfo().length() > 100) {
                inputmap.put(BANKTOBANKINFORMATION4,
                        fontisBatch.getBankToBankInfo().substring(100, fontisBatch.getBankToBankInfo().length() - 1));
            }

            if (fontisBatch.getBankToBankInfo().length() >= 175) {
                inputmap.put(BANKTOBANKINFORMATION5, fontisBatch.getBankToBankInfo().substring(140, 175));
            }
            else if (fontisBatch.getBankToBankInfo().length() > 140) {
                inputmap.put(BANKTOBANKINFORMATION5,
                        fontisBatch.getBankToBankInfo().substring(140, fontisBatch.getBankToBankInfo().length() - 1));
            }

            if (fontisBatch.getBankToBankInfo().length() >= 210) {
                inputmap.put(BANKTOBANKINFORMATION6, fontisBatch.getBankToBankInfo().substring(175, 210));
            }
            else if (fontisBatch.getBankToBankInfo().length() > 175) {
                inputmap.put(BANKTOBANKINFORMATION6,
                        fontisBatch.getBankToBankInfo().substring(175, fontisBatch.getBankToBankInfo().length() - 1));
            }
        }
    }

    private void setTermsAndConditions() {
        inputmap.put(TERMSANDCONDITIONS1, CommonConstants.EMPTY_STRING);
        inputmap.put(TERMSANDCONDITIONS2, CommonConstants.EMPTY_STRING);
        inputmap.put(TERMSANDCONDITIONS3, CommonConstants.EMPTY_STRING);
        inputmap.put(TERMSANDCONDITIONS4, CommonConstants.EMPTY_STRING);
        inputmap.put(TERMSANDCONDITIONS5, CommonConstants.EMPTY_STRING);
        inputmap.put(TERMSANDCONDITIONS6, CommonConstants.EMPTY_STRING);
    }

    private void setSenderToReceiverInformation() {
        inputmap.put(SENDERTORECEIVERINFORMATION1, CommonConstants.EMPTY_STRING);
        inputmap.put(SENDERTORECEIVERINFORMATION2, CommonConstants.EMPTY_STRING);
        inputmap.put(SENDERTORECEIVERINFORMATION3, CommonConstants.EMPTY_STRING);
        inputmap.put(SENDERTORECEIVERINFORMATION4, CommonConstants.EMPTY_STRING);
        inputmap.put(SENDERTORECEIVERINFORMATION5, CommonConstants.EMPTY_STRING);
        inputmap.put(SENDERTORECEIVERINFORMATION6, CommonConstants.EMPTY_STRING);
    }

    // Changes start for artf53453
    private void setOtherValues(FON_CreditRecord fontis, FON_BatchRecord fontisBatch, String accountID, String transactionID) {
        // Changes end for artf53453
        java.sql.Date date = new java.sql.Date(0);
        inputmap.put(VERIFYFLAG, STATUS_AUTHORISED);
        inputmap.put(MESSAGETYPE, MESSAGE_TYPE);
        inputmap.put(BROKER, CommonConstants.EMPTY_STRING);
        inputmap.put(CHARGEAMOUNT, new BigDecimal(0));
        inputmap.put(CHARGECURRENCY, CommonConstants.EMPTY_STRING);
        inputmap.put(CODEWORD, Originate_Deal);
        inputmap.put(CONTRAACCOUNT, accountID);
        inputmap.put(CONTRAAMOUNT, (BigDecimal) ((FON_CreditRecord) fontis).getAmount());
        inputmap.put(CUSTOMERNUMBER,
                findCustomerCode(((FON_DebitRecord) fontisBatch.getDebitRecords().get(0)).getDebitAccountNo()));
        inputmap.put(DEALNUMBER, fontisBatch.getTransactionReference());
        inputmap.put(INTERESTRATE, new BigDecimal(0));
        inputmap.put(MAINACCOUNT, ((FON_DebitRecord) fontisBatch.getDebitRecords().get(0)).getDebitAccountNo());
        inputmap.put(MATURITYDATE, fontisBatch.getValueDate());
        // Changes start for artf53453
        inputmap.put(POSTDATE, SystemInformationManager.getInstance().getBFBusinessDate());
        // Changes end for artf53453
        inputmap.put(SETTLINSTRUCTIONNUMBER, 0);
        inputmap.put(TRANSACTIONAMOUNT, ((FON_CreditRecord) fontis).getAmount());
        inputmap.put(VALUEDATE, fontisBatch.getValueDate());
        inputmap.put(BANKOPERATIONCODE, CRED);
        inputmap.put(GENERATE103PLUS, "N");
        inputmap.put(BANKINSTRUCTIONCODE, CommonConstants.EMPTY_STRING);
        inputmap.put(BANKINSTRUCTIONCODETEXT, CommonConstants.EMPTY_STRING);
        inputmap.put(CHARGECODE, CommonConstants.EMPTY_STRING);
        inputmap.put(DRCRCONFIRMFLAG, 9);
        inputmap.put(DRAFTNUMBER, 0);
        inputmap.put(INTERESTAMOUNT, new BigDecimal(0));
        inputmap.put(PAYRECEIVEFLAG, CommonConstants.EMPTY_STRING);
        inputmap.put(INTERESTPERIODENDDATE, date);
        inputmap.put(INTERESTPERIODSTARTDATE, date);
        inputmap.put(NEXTINTERESTDUEDATE, date);
        inputmap.put(WALKINCUSTOMER, true);
        inputmap.put(NARRATIVE, CommonConstants.EMPTY_STRING);
        inputmap.put(CURRENCY, ((FON_DebitRecord) fontisBatch.getDebitRecords().get(0)).getDebitCurrencyCode());
        inputmap.put(DAYCOUNTFRACTION, CommonConstants.EMPTY_STRING);
        inputmap.put(INTCAPITALPAYAWAY, CommonConstants.EMPTY_STRING);
        inputmap.put(PRINCIPLEINCREASEDECREASE, CommonConstants.EMPTY_STRING);
        inputmap.put(PURCHASECURRENCY, ((FON_DebitRecord) fontisBatch.getDebitRecords().get(0)).getDebitCurrencyCode());
        inputmap.put(SOLDCURRENCY, ((FON_CreditRecord) fontis).getCreditCurrencyCode());
        inputmap.put(ROLLOVEROPTION, CommonConstants.EMPTY_STRING);
        inputmap.put(PAYAWAYINTERESTACCOUNT, CommonConstants.EMPTY_STRING);
        inputmap.put(PAYAWAYPRINCIPLEACCOUNTNUMBER, CommonConstants.EMPTY_STRING);
        inputmap.put(RELATEDDEALNUMBER, CommonConstants.EMPTY_STRING);
        // Changes start for artf53453
        inputmap.put(TRANSACTIONID, transactionID);
        // Changes end for artf53453
        inputmap.put(TRANSACTIONCODE, CommonConstants.EMPTY_STRING);
        inputmap.put(ZERO, 0);
        inputmap.put(KEYCURRENCY, ((FON_DebitRecord) fontisBatch.getDebitRecords().get(0)).getDebitCurrencyCode());
        // Changes start for artf53453
        inputmap.put(FXTRANSACTION, 7);
        // Changes end for `artf53453
    }

    private void callSWIFTInterface(BankFusionEnvironment env) {
        try {
            outputmap = MFExecuter.executeMF(MICROFLOW_NAME, env, inputmap);
        }
        catch (BankFusionException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e);
            }
        }
    }

    private String findCustomerCode(String accountId) {
        String customerCode = "";
        BankFusionEnvironment env = null;
        List customerList = FinderMethods.findCustomerByAccount(accountId, env, null);
        customerCode = ((IBOCustomer) customerList.get(0)).getBoID();
        return customerCode;
    }

    private String findCustomerName(String accountId) {
        String customerName = "";
        BankFusionEnvironment env = null;
        List customerList = FinderMethods.findCustomerByAccount(accountId, env, null);
        customerName = ((IBOCustomer) customerList.get(0)).getBoID();
        return customerName;
    }

    private void getAddress(String customerCode, BankFusionEnvironment env) {
        Map<String, Object> addressList = new HashMap<>();
        addressList.put(CUSTOMERCODE, customerCode);
        Map<String, Object> addressDetails = MFExecuter.executeMF(GETADDRESS, env, addressList);
        addline1 = addressDetails.get(SWT_DISPOSAL_ADDRESSLINE1).toString();
        addline2 = addressDetails.get(SWT_DISPOSAL_ADDRESSLINE2).toString();
        addline2 = addressDetails.get(SWT_DISPOSAL_ADDRESSLINE3).toString();
    }
}
