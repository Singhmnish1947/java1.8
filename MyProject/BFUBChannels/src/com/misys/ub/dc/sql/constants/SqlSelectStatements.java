package com.misys.ub.dc.sql.constants;

import com.trapedza.bankfusion.bo.refimpl.IBOProductInheritance;

/**
 * @author susikdar <code>SqlSelectStatements</code> type hold all the constant select queries
 */
public final class SqlSelectStatements {

    private SqlSelectStatements() {
    }

    /**
     * retrieve all the data from opics position account update transaction based on transaction id <br>
     * SELECT * FROM UBINTERFACE.INFTB_OPXPOSITIONACCTUPDTXN WHERE INTRANSACTIONID = ?
     */
    public static final String positionClause = "SELECT * FROM INFTB_OPXPOSITIONACCTUPDTXN WHERE INTRANSACTIONID = ?";

    /**
     * retrieve all the data from transaction counter type date based on transaction id & transaction direction <br>
     * SELECT * FROM UBTB_TRANSACTIONCOUNTERPTYDATA WHERE UBTRANSACTIONID = ? AND UBTRANSACTIONDIRECTION = ?
     */
    public static final String extnClauseForATMPOSTxns = "SELECT TCPD.*, AD.ATMCARDID FROM UBTB_TRANSACTIONCOUNTERPTYDATA TCPD   "
    		+ " LEFT OUTER JOIN ATMCARDDETAILS AD ON AD.ATMCARDNUMBER = TCPD.UBUNMASKEDCARDNUMBER WHERE UBTRANSACTIONID = ? AND UBTRANSACTIONDIRECTION = ?";
    
    /**
     * retrieve all the data from transaction counter type date based on transaction id & transaction direction <br>
     * SELECT * FROM UBTB_TRANSACTIONCOUNTERPTYDATA WHERE UBTRANSACTIONID = ? AND UBTRANSACTIONDIRECTION = ?
     */
    public static final String extnClause =
        "SELECT * FROM UBTB_TRANSACTIONCOUNTERPTYDATA WHERE UBTRANSACTIONID = ? AND UBTRANSACTIONDIRECTION = ?";
    /**
     * Retrieve end-to-end Reference, Creditor ID & Mandate Number from dd transaction info based on provided host transaction reference
     * <br>
     * SELECT MPPMTIDENDTOENDID, MPDDTMRIAIDORGCDSCHMID, MPDDTMRIMNDTID FROM MPTB_DDTXNINFO WHERE MPHOSTTXNREFERENCE = ?
     */
    public static final String ddTxnInfoClause =
        "SELECT MPPMTIDENDTOENDID, MPDDTMRIAIDORGCDSCHMID, MPDDTMRIMNDTID FROM MPTB_DDTXNINFO WHERE MPHOSTTXNREFERENCE = ?";
    
    /**
     * retrieve subproductid and subproduct currency from product inheritance table based on product context code
     */
    @SuppressWarnings("FBPE")
    public static final String SUB_PRODUCT_QUERY =
        "SELECT PRODH." + IBOProductInheritance.UBSUBPRODUCTID + " AS " + IBOProductInheritance.UBSUBPRODUCTID + ", PRODH."
            + IBOProductInheritance.ACC_ISOCURRENCYCODE + " AS " + IBOProductInheritance.ACC_ISOCURRENCYCODE + " FROM "
            + IBOProductInheritance.BONAME + " AS " + " PRODH " + " WHERE PRODH." + IBOProductInheritance.PRODUCTCONTEXTCODE + " = ?";
    
    
    /**
     * Retrieve bulk payment file name based on provided host transaction reference
     * <br>
     * SELECT MPFILENAME FROM MPTB_CTTXNINFO WHERE MPHOSTTXNREFERENCE = ?
     */
    public static final String ctTxnInfoClause =
        "SELECT MPFILENAME FROM MPTB_CTGRPHDR WHERE MPCTGRPHDRIDPK = ?";

}