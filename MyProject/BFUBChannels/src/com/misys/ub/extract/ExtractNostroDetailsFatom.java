package com.misys.ub.extract;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.bankfusion.attributes.PagedQuery;
import bf.com.misys.cbs.types.AccountKeys;
import bf.com.misys.cbs.types.ExtractIntrnlAcctDtl;
import bf.com.misys.cbs.types.ExtractIntrnlAcctDtlsOutput;
import bf.com.misys.cbs.types.ProfileDtls;
import bf.com.misys.cbs.types.Pseudonym;
import bf.com.misys.cbs.types.StatementDetails;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.msgs.extract.v1r1.ExtractIntrnlAcctDtlsRs;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.persistence.IPersistenceObjectsFactory;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.bo.refimpl.IBOAccount;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_Extract_Nostro_Details;

public class ExtractNostroDetailsFatom extends
		AbstractUB_TIP_Extract_Nostro_Details {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Log logger = LogFactory
			.getLog(ExtractNostroDetailsFatom.class.getName());
	public static final String svnRevision = "$Revision: 1.0 $";

	private IPersistenceObjectsFactory factory;
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private static String queryForExtractNostroAccDetails = "SELECT  ACC."
			+ IBOAccount.STOPCODE + " AS " + IBOAccount.STOPCODE + " , ACC."
			+ IBOAccount.LOWESTBALANCE + " AS " + IBOAccount.LOWESTBALANCE
			+ " , ACC." + IBOAccount.LASTCREDITLIMITCHANGEDATE + " AS "
			+ IBOAccount.LASTCREDITLIMITCHANGEDATE + " , ACC."
			+ IBOAccount.CREDITLIMIT + " AS " + IBOAccount.CREDITLIMIT
			+ " , ACC." + IBOAccount.UBINDUSTRYCODE + " AS "
			+ IBOAccount.UBINDUSTRYCODE + " , ACC." + IBOAccount.CLOSUREDATE
			+ " AS " + IBOAccount.CLOSUREDATE + " , ACC."
			+ IBOAccount.ACCOUNTNAME + " AS " + IBOAccount.ACCOUNTNAME
			+ " , ACC." + IBOAccount.UBACCOUNTSTATUS + " AS "
			+ IBOAccount.UBACCOUNTSTATUS + " , ACC." + IBOAccount.CUSTOMERCODE
			+ " AS " + IBOAccount.CUSTOMERCODE + " , ACC."
			+ IBOAccount.OPENEDBYUSER + " AS " + IBOAccount.OPENEDBYUSER
			+ " , ACC." + IBOAccount.DORMANTSTATUS + " AS "
			+ IBOAccount.DORMANTSTATUS + " , ACC." + IBOAccount.LIMITREF5
			+ " AS " + IBOAccount.LIMITREF5 + " , ACC." + IBOAccount.LIMITREF4
			+ " AS " + IBOAccount.LIMITREF4 + " , ACC." + IBOAccount.LIMITREF3
			+ " AS " + IBOAccount.LIMITREF3 + " , ACC." + IBOAccount.STOPPED
			+ " AS " + IBOAccount.STOPPED + " , ACC." + IBOAccount.LIMITREF2
			+ " AS " + IBOAccount.LIMITREF2 + " , ACC." + IBOAccount.LIMITREF1
			+ " AS " + IBOAccount.LIMITREF1 + " , ACC."
			+ IBOAccount.MINOPENINGBALANCEAMOUNT + " AS "
			+ IBOAccount.MINOPENINGBALANCEAMOUNT + " , ACC."
			+ IBOAccount.STOPREASON + " AS " + IBOAccount.STOPREASON
			+ " , ACC." + IBOAccount.LASTINTERESTACCRUALDATE + " AS "
			+ IBOAccount.LASTINTERESTACCRUALDATE + " , ACC."
			+ IBOAccount.CLOSED + " AS " + IBOAccount.CLOSED + " , ACC."
			+ IBOAccount.ACCOUNTID + " AS " + IBOAccount.ACCOUNTID + " , ACC."
			+ IBOAccount.LASTDEBITTRANSDTTM + " AS "
			+ IBOAccount.LASTDEBITTRANSDTTM + " , ACC."
			+ IBOAccount.LASTSTATICAMENDMENTDATE + " AS "
			+ IBOAccount.LASTSTATICAMENDMENTDATE + " , ACC."
			+ IBOAccount.DATEOFDORMANCY + " AS " + IBOAccount.DATEOFDORMANCY
			+ " , ACC." + IBOAccount.CLEAREDBALANCE + " AS "
			+ IBOAccount.CLEAREDBALANCE + " , ACC." + IBOAccount.LIMITINDICATOR
			+ " AS " + IBOAccount.LIMITINDICATOR + " , ACC."
			+ IBOAccount.LASTCREDITTRANSDTTM + " AS "
			+ IBOAccount.LASTCREDITTRANSDTTM + " , ACC."
			+ IBOAccount.UBACCRIGHTSINDREASON + " AS "
			+ IBOAccount.UBACCRIGHTSINDREASON + " , ACC."
			+ IBOAccount.LASTDEBITLIMITCHANGEDATE + " AS "
			+ IBOAccount.LASTDEBITLIMITCHANGEDATE + " , ACC."
			+ IBOAccount.CHEQUEDEPOSITBALANCE + " AS "
			+ IBOAccount.CHEQUEDEPOSITBALANCE + " , ACC."
			+ IBOAccount.BLOCKEDBALANCE + " AS " + IBOAccount.BLOCKEDBALANCE
			+ " , ACC." + IBOAccount.AMENDEDBYUSER + " AS "
			+ IBOAccount.AMENDEDBYUSER + " , ACC."
			+ IBOAccount.ACCRIGHTSINDICATOR + " AS "
			+ IBOAccount.ACCRIGHTSINDICATOR + " , ACC." + IBOAccount.SERVICEFEE
			+ " AS " + IBOAccount.SERVICEFEE + " , ACC."
			+ IBOAccount.BOOKEDBALANCE + " AS " + IBOAccount.BOOKEDBALANCE
			+ " , ACC." + IBOAccount.TAXINDICATORDR + " AS "
			+ IBOAccount.TAXINDICATORDR + " , ACC."
			+ IBOAccount.PEROUTERPROFILEID + " AS "
			+ IBOAccount.PEROUTERPROFILEID + " , ACC."
			+ IBOAccount.TEMPLIMEXPIRYDATE + " AS "
			+ IBOAccount.TEMPLIMEXPIRYDATE + " , ACC." + IBOAccount.PSEUDONAME
			+ " AS " + IBOAccount.PSEUDONAME + " , ACC."
			+ IBOAccount.UBACCRIGHTSINDCHANGEDT + " AS "
			+ IBOAccount.UBACCRIGHTSINDCHANGEDT + " , ACC."
			+ IBOAccount.LOWESTBALANCEDATE + " AS "
			+ IBOAccount.LOWESTBALANCEDATE + " , ACC."
			+ IBOAccount.UBACCRIGHTSINDCHANGEDBY + " AS "
			+ IBOAccount.UBACCRIGHTSINDCHANGEDBY + " , ACC."
			+ IBOAccount.PASSBOOKINDICATOR + " AS "
			+ IBOAccount.PASSBOOKINDICATOR + " , ACC." + IBOAccount.PRODUCTID
			+ " AS " + IBOAccount.PRODUCTID + " , ACC." + IBOAccount.DEBITLIMIT
			+ " AS " + IBOAccount.DEBITLIMIT + " , ACC."
			+ IBOAccount.BRANCHSORTCODE + " AS " + IBOAccount.BRANCHSORTCODE
			+ " , ACC." + IBOAccount.TEMPACCOUNTLIMIT + " AS "
			+ IBOAccount.TEMPACCOUNTLIMIT + " , ACC."
			+ IBOAccount.STOPPEDBYUSER + " AS " + IBOAccount.STOPPEDBYUSER
			+ " , ACC." + IBOAccount.STOPDATE + " AS " + IBOAccount.STOPDATE
			+ " , ACC." + IBOAccount.CHARGEFUNDINGACCOUNTID + " AS "
			+ IBOAccount.CHARGEFUNDINGACCOUNTID + " , ACC."
			+ IBOAccount.UBACCTRANSCOUNTER + " AS "
			+ IBOAccount.UBACCTRANSCOUNTER + " , ACC." + IBOAccount.OPENDATE
			+ " AS " + IBOAccount.OPENDATE + " , ACC."
			+ IBOAccount.PRODUCTCONTEXTCODE + " AS "
			+ IBOAccount.PRODUCTCONTEXTCODE + " , ACC."
			+ IBOAccount.FEEWAIVEINDICATOR + " AS "
			+ IBOAccount.FEEWAIVEINDICATOR + " , ACC."
			+ IBOAccount.TAXINDICATORCR + " AS " + IBOAccount.TAXINDICATORCR
			+ " , ACC." + IBOAccount.JOINTACCOUNT + " AS "
			+ IBOAccount.JOINTACCOUNT + " , ACC."
			+ IBOAccount.LASTTRANSACTIONDATE + " AS "
			+ IBOAccount.LASTTRANSACTIONDATE + " , ACC."
			+ IBOAccount.ACCOUNTDESCRIPTION + " AS "
			+ IBOAccount.ACCOUNTDESCRIPTION + " , ACC."
			+ IBOAccount.ACCOUNTSTYLE + " AS " + IBOAccount.ACCOUNTSTYLE
			+ " , ACC." + IBOAccount.ISOCURRENCYCODE + " AS "
			+ IBOAccount.ISOCURRENCYCODE + " FROM " + IBOAccount.BONAME
			+ " ACC  WHERE ACC." + IBOAccount.ACCOUNTID + " = ? ";
	String crudMode = CommonConstants.EMPTY_STRING;
	String accountID = CommonConstants.EMPTY_STRING;

	public ExtractNostroDetailsFatom(BankFusionEnvironment env) {
		super(env);

	}

	public void process(BankFusionEnvironment env) throws BankFusionException {
		try {
			factory = BankFusionThreadLocal.getPersistanceFactory();
			crudMode = getF_IN_CrudMode();
			accountID = getF_IN_ExtractIntrnlNostroAcctDtlsRq()
					.getExtractIntrnlAcctDtlsInput().getProductID();
			

			ExtractIntrnlAcctDtlsRs res = new ExtractIntrnlAcctDtlsRs();
			ExtractIntrnlAcctDtlsOutput extractIntrnlAcctDtlsOutput = new ExtractIntrnlAcctDtlsOutput();
			ExtractIntrnlAcctDtl[] intrnlNostroAccArr = new ExtractIntrnlAcctDtl[1];
			ExtractIntrnlAcctDtl intrnlNostroAcc = new ExtractIntrnlAcctDtl();
			intrnlNostroAccArr[0] = intrnlNostroAcc;
			extractIntrnlAcctDtlsOutput
					.setExtractIntrnlAcctDtls(intrnlNostroAccArr);
			RsHeader header = new RsHeader();
			header.setMessageType("Nostro"); 
			PagedQuery pagingInfo = new PagedQuery();
			res.setPagingInfo(pagingInfo);
			res.setRsHeader(header);
			res.setExtractIntrnlAcctDtlsOutput(extractIntrnlAcctDtlsOutput);

			List<SimplePersistentObject> extractIntrnlNostroDetails = getExtractItrnlNostroDetails();

			if (extractIntrnlNostroDetails != null
					&& extractIntrnlNostroDetails.size() > 0) {
				for (SimplePersistentObject intrnlNostroDtls : extractIntrnlNostroDetails) {

					intrnlNostroAcc.setAccountName((String) intrnlNostroDtls
							.getDataMap().get(IBOAccount.ACCOUNTNAME));
					intrnlNostroAcc.setCrudMode(crudMode);
					intrnlNostroAcc.setCustomerName((String) intrnlNostroDtls
							.getDataMap().get(IBOAccount.CUSTOMERCODE));
					intrnlNostroAcc
							.setExternalAccountId((String) intrnlNostroDtls
									.getDataMap().get(IBOAccount.ACCOUNTID));
					intrnlNostroAcc
							.setInternalAcctType((String) intrnlNostroDtls
									.getDataMap().get(IBOAccount.PRODUCTID));
					intrnlNostroAcc.setAccountKeys(new AccountKeys());
					intrnlNostroAcc.getAccountKeys().setExternalAccountId(
							(String) intrnlNostroDtls.getDataMap().get(
									IBOAccount.ACCOUNTID));
					Pseudonym pseudonym = new Pseudonym();
					pseudonym.setBranchCode((String) intrnlNostroDtls
							.getDataMap().get(IBOAccount.BRANCHSORTCODE));
					pseudonym.setIsoCurrencyCode((String) intrnlNostroDtls
							.getDataMap().get(IBOAccount.ISOCURRENCYCODE));
					intrnlNostroAcc.getAccountKeys().setPseudonym(pseudonym);
					intrnlNostroAcc.getAccountKeys().setStandardAccountId(
							(String) intrnlNostroDtls.getDataMap().get(
									IBOAccount.ACCOUNTID));
					// intrnlNostroAcc.getAccountKeys().setIBAN(IBOAccount.);
					// intrnlNostroAcc.setIsSwiftRecAcc((Boolean)
					// intrnlNostroDtls.getDataMap().get(IBOAccount.));
					// intrnlNostroAcc.setIsSwiftSendAcc(isSwiftSendAcc)((String)
					// intrnlNostroDtls.getDataMap().get(IBOAccount.CUSTOMERCODE));
					ProfileDtls profileDtls = new ProfileDtls();
					// profileDtls.set
					intrnlNostroAcc.setProfileDtls(profileDtls);
					// intrnlNostroAcc.setReceiverBIC(receiverBIC);
					intrnlNostroAcc.setSenderBIC("");
					StatementDetails statementDetails = new StatementDetails();
					intrnlNostroAcc.setStatementDetails(statementDetails);
				}
			}else{
				intrnlNostroAcc
				.setExternalAccountId(accountID);
			}

			setF_OUT_ExtractIntrnlAcctDtlsRs(res);
		} catch (Exception e) {

			logger
					.error("Error in ExtractNostroDetailsFatom.java for Primary Key "
							+ accountID + " Error is " + ExceptionUtil.getExceptionAsString(e));

			throw new BankFusionException(e);
		}
	}

	private List<SimplePersistentObject> getExtractItrnlNostroDetails() {
		ArrayList<String> params = new ArrayList<String>();
		params.add(accountID);
		return factory.executeGenericQuery(queryForExtractNostroAccDetails,
				params, null, true);

	}
}
