package com.trapedza.bankfusion.fatoms;

import java.util.HashMap;

import bf.com.misys.cbs.msgs.cards.v1r0.MaintainCustCardRq;
import bf.com.misys.cbs.types.CustCrdBasicDtls;

import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.bo.refimpl.IBOATMCardDetails;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_CMS_ProcessOnlineCMSRequest;
import com.trapedza.bankfusion.utils.GUIDGen;

public class UB_CMS_ProcessOnlineCMSRequest extends
AbstractUB_CMS_ProcessOnlineCMSRequest {

	private static final String PROCESS_MFID_FOR_CRMSGHD = "UB_CMS_CreateMessageHeaderWraper_SRV";
	private static final String PROCESS_MFID_FOR_UPMSGHD = "UB_INF_UpdateMessageHeader_SRV";
	private static final String PROCESS_MFID_FOR_CRCARDDT = "UB_CMS_CreateCardDetails_SRV";
	private static final String PROCESS_MFID_FOR_CRCMSMSG = "UB_CMS_CreateCmsStatusMsg_SRV";
	private static final String PROCESS_MFID_FOR_UPCMSMSG = "UB_CMS_UpdateCmsStatusMsg_SRV";
	private static final String PROCESS_MFID_FOR_VALD = "UB_CMS_ValidateMessage_SRV";
	private static final String PROCESSED_CODE= "P";
	private static final String FAILED_CODE= "F";
	private static final String MESSAGEID1 = "MESSAGEID1";
	private static final String CARD_ACTION_NEW = "NEW";
	private static final String CARD_ACTION_AMEND = "AMEND";
	private static final String CARD_ACTION_CANCEL = "CANCEL";
	private static final String CARD_AMEND_MFID = "UB_CMS_AmendCardDetails_SRV";

	public UB_CMS_ProcessOnlineCMSRequest() {

	}

	public UB_CMS_ProcessOnlineCMSRequest(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param env
	 *            Environment
	 */

	@Override
	public void process(BankFusionEnvironment env) throws BankFusionException {
		String errorCode = String.valueOf(ChannelsEventCodes.I_PROCESS_SUCCESS); // SUCCESS
		MaintainCustCardRq maintainCustCardRq = new MaintainCustCardRq();
		maintainCustCardRq = getF_IN_cardRequest();

		/*
		 * create message header
		 */

		HashMap paramsOfMessageHeader = new HashMap();
		// messageId need to be mapped
		String messageType = BankFusionThreadLocal.getSourceId();
//		String messageType = maintainCustCardRq.getRqHeader().getMessageType();
		String messageID = GUIDGen.getNewGUID();
		String branch = BankFusionThreadLocal.getUserSession()
		.getBranchSortCode();

		paramsOfMessageHeader.put(MESSAGEID1, messageID);
		paramsOfMessageHeader.put("MESSAGETYPE", messageType);
		//		paramsOfMessageHeader.put("CHID", CHID);
//		paramsOfMessageHeader.put("CHANNELID", BankFusionThreadLocal.getSourceId());

		MFExecuter.executeMF(PROCESS_MFID_FOR_CRMSGHD,
				env, paramsOfMessageHeader);

		for (int i = 0; i < maintainCustCardRq.getCustCrdBasicInput()
		.getCustCrdBasicDtlsCount(); i++) {
			CustCrdBasicDtls custCrdBasicDtls = maintainCustCardRq
			.getCustCrdBasicInput().getCustCrdBasicDtls(i);
			String cmsStatusMsgIDPK = GUIDGen.getNewGUID();
			String cardAction = custCrdBasicDtls.getCardAction();

			/*
			 * 
			 * create CMS MSG Activity Log
			 */
			HashMap paramOfCmsMsg = new HashMap();
			paramOfCmsMsg.put("CMSSTATUSMSGID", cmsStatusMsgIDPK);
			paramOfCmsMsg.put("MESSAGEID", messageID);
			//TODO: Add code to derive Party ID from Customer Code once Party is delivered
			paramOfCmsMsg.put("PARTYID", custCrdBasicDtls.getPartyId());
			paramOfCmsMsg.put("CARDNUMBER", custCrdBasicDtls.getCardNumber());
			paramOfCmsMsg.put("ACCOUNTID", custCrdBasicDtls.getAccountID().getStandardAccountId());
			paramOfCmsMsg.put("CARDACTION", cardAction);
			paramOfCmsMsg.put("CARDSTATUS", custCrdBasicDtls.getCardStatus());
			paramOfCmsMsg.put("EXPIRYDATE", custCrdBasicDtls.getExpiryDate());
			paramOfCmsMsg.put("CARDTYPE", custCrdBasicDtls.getCardType());
			paramOfCmsMsg.put("CUSTOMERCODE", custCrdBasicDtls.getPartyId());
			paramOfCmsMsg.put("REASON", custCrdBasicDtls.getReason());
			paramOfCmsMsg.put("MSGSTATUS", "R");

			MFExecuter.executeMF(PROCESS_MFID_FOR_CRCMSMSG,
					env, paramOfCmsMsg);


			HashMap paramsOfValidation = new HashMap();
			// paramsOfValidation.put("AccountNumber",
			// custCrdBasicDtls.getAccountID());
			// String accountNumber=custCrdBasicDtls.getAccountID().toString();
			paramsOfValidation.put("AccountNumber", custCrdBasicDtls.getAccountID().getStandardAccountId());
			paramsOfValidation.put("ATMCARDNUMBER", custCrdBasicDtls.getCardNumber());
			paramsOfValidation.put("CustomerNumber", custCrdBasicDtls.getPartyId());
			paramsOfValidation.put("cardStatus", custCrdBasicDtls.getCardStatus());
			paramsOfValidation.put("cardType", custCrdBasicDtls.getCardType());

			HashMap status = MFExecuter.executeMF(PROCESS_MFID_FOR_VALD,
					env, paramsOfValidation);
			errorCode = status.get("errorNumber").toString();
			if (errorCode.equals(CommonConstants.EMPTY_STRING)) {
				
				String atmCardNumber = custCrdBasicDtls
				.getCardNumber();
				String imdCode = custCrdBasicDtls.getImdCode();
				String key = atmCardNumber.concat(imdCode);
				IBOATMCardDetails atmCardNumberBO = (IBOATMCardDetails) BankFusionThreadLocal
				.getPersistanceFactory().findByPrimaryKey(
						IBOATMCardDetails.BONAME, key, true);

				HashMap<String, Object> paramOfCardDetail = new HashMap<String, Object>();
				if (cardAction.equals(CARD_ACTION_NEW)) {
					/*
					 * create Card Details
					 */
					if (atmCardNumberBO==null || atmCardNumberBO.getBoID().toString().equals(CommonConstants.EMPTY_STRING)) {
						String bmBranch = CommonConstants.EMPTY_STRING;
						IBOBranch branchBO = (IBOBranch) BankFusionThreadLocal
						.getPersistanceFactory().findByPrimaryKey(
								IBOBranch.BONAME, branch, true);
						if (branchBO != null) {
							bmBranch = branchBO.getF_BMBRANCH();
						}
						paramOfCardDetail.put("ATMCARDNUMBER", custCrdBasicDtls
								.getCardNumber());
						paramOfCardDetail.put("ACCOUNTID", custCrdBasicDtls
								.getAccountID().getStandardAccountId());
						paramOfCardDetail.put("CARDSEQUENCENUMBER", "0");
						paramOfCardDetail.put("UBCARDSTATUS", status.get("refrencedCode").toString());// changed from status.get("ReferenceCode")to this as this value was not coming
						paramOfCardDetail.put("UBCARDSTATUSDTTM", custCrdBasicDtls
								.getExpiryDate());
						paramOfCardDetail.put("UBCARDTYPE", status.get("UBCardType").toString());// changed from status.get("UBCardType")to this as this value was not coming
						paramOfCardDetail.put("UBCUSTOMERCODE", custCrdBasicDtls
								.getPartyId());
						paramOfCardDetail.put("UBREASONFORSTATUS", custCrdBasicDtls
								.getReason());
						paramOfCardDetail.put("BMBRANCH", bmBranch);
						paramOfCardDetail.put("ImdCode", custCrdBasicDtls.getImdCode());
						paramOfCardDetail.put("ATMCARDID",
								custCrdBasicDtls.getCardID());
	
						MFExecuter.executeMF(PROCESS_MFID_FOR_CRCARDDT,
								env, paramOfCardDetail);
					} else {
						// Card Exists, cannot insert
						errorCode = "40400020";
					}
				} else if (cardAction.equals(CARD_ACTION_AMEND) || (cardAction.equals(CARD_ACTION_CANCEL))) {
					if (atmCardNumberBO != null) {

						/*
						 * Need to put the logic for all field update
						 */
						paramOfCardDetail.put("ATMCARDNUMBER", custCrdBasicDtls
								.getCardNumber());
						paramOfCardDetail.put("ACCOUNTID", custCrdBasicDtls
								.getAccountID().getStandardAccountId());
						paramOfCardDetail.put("UBCARDSTATUS", status.get(
								"refrencedCode").toString());
//						paramOfCardDetail.put("CARDTYPE", custCrdBasicDtls
//								.getCardType());
						paramOfCardDetail.put("CARDTYPE", 
								status.get("UBCardType").toString());
						paramOfCardDetail.put("UBCARDSTATUSDTTM",
								custCrdBasicDtls.getExpiryDate());
						paramOfCardDetail.put("UBREASONFORSTATUS",
								custCrdBasicDtls.getReason());
						paramOfCardDetail.put("IMDCODE",
								custCrdBasicDtls.getImdCode());

						MFExecuter.executeMF(CARD_AMEND_MFID,
								env, paramOfCardDetail);
					} else  {
						// Card does not exist, cannot amend!
						errorCode = "40400005";
					}
				}
			}

			/*
			 * update CMS MSG Activity Log
			 */
			HashMap paramOfUpdateCmsMsg = new HashMap();
			//			String cmsStatusMsgID = (IBOUB_CMS_CmsStatusMsg.CMSSTATUSMSGID
			//					.toString());
			paramOfUpdateCmsMsg.put("messageStatusID", cmsStatusMsgIDPK);
			paramOfUpdateCmsMsg.put("MESSAGEID", messageID);
			if(!errorCode.equals(CommonConstants.EMPTY_STRING)){
				paramOfUpdateCmsMsg.put("ERRORCODE", errorCode);
				paramOfUpdateCmsMsg.put("MSGSTATUS", FAILED_CODE);
			} else {
				paramOfUpdateCmsMsg.put("ERRORCODE", CommonConstants.EMPTY_STRING);
				paramOfUpdateCmsMsg.put("MSGSTATUS", PROCESSED_CODE);
			}

			MFExecuter.executeMF(PROCESS_MFID_FOR_UPCMSMSG,
					env, paramOfUpdateCmsMsg);

		}

		/*
		 * update message header
		 */

		HashMap paramsOfUpdateMessageHeader = new HashMap();
		// messageId need to be mapped
		paramsOfUpdateMessageHeader.put(MESSAGEID1, messageID);
		if(!errorCode.equals(CommonConstants.EMPTY_STRING)){
			paramsOfUpdateMessageHeader.put("MESSAGESTATUS", FAILED_CODE);
			paramsOfUpdateMessageHeader.put("ERRORCODE", errorCode);
		}else{
			paramsOfUpdateMessageHeader.put("MESSAGESTATUS", PROCESSED_CODE);
			paramsOfUpdateMessageHeader.put("ERRORCODE", ChannelsEventCodes.I_PROCESS_SUCCESS);
		}
		MFExecuter.executeMF(PROCESS_MFID_FOR_UPMSGHD,
				env, paramsOfUpdateMessageHeader);
	}	
}
