package com.trapedza.bankfusion.fatoms.tiplus;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bf.com.misys.cbs.msgs.party.v1r0.SearchGlobalPartyRq;
import bf.com.misys.cbs.msgs.party.v1r0.SearchGlobalPartyRs;
import bf.com.misys.cbs.types.PartySearchKey;
import bf.com.misys.cbs.types.SearchGlobalPartyInput;
import bf.com.misys.cbs.types.header.MessageStatus;
import bf.com.misys.cbs.types.header.RsHeader;
import bf.com.misys.cbs.types.header.SubCode;

import com.misys.bankfusion.common.exception.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.events.IEvent;
import com.trapedza.bankfusion.fatoms.UB_CNF_InternalCustomerSearch;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_TIP_CustomerSearch_SRV;
import com.trapedza.bankfusion.steps.refimpl.IUB_TIP_CustomerSearch_SRV;

public class UB_TIP_CustomerSearch extends AbstractUB_TIP_CustomerSearch_SRV
		implements IUB_TIP_CustomerSearch_SRV {

	public static final long serialVersionUID = 1L;

	public UB_TIP_CustomerSearch(BankFusionEnvironment env) {
		super(env);
	}

	private static final Log logger = LogFactory
			.getLog(UB_TIP_CustomerSearch.class.getName());
	private static final String SEARCH_GLOBAL_PARTY_RQ_INTAG = "SearchGlobalPartyRq";
	private static final String SEARCH_GLOBAL_PARTY_RQ_OUTTAG = "SearchGlobalPartyRs";
	private static final String SEARCH_GLOBAL_PARTY_MF_INF = "CB_PTY_TIGlobalPartySearch_SRV";
	private static final String ATLEAST_ONE_FILTER_ERROR_CODE = "20020238";
	private static final String NO_DETAILS_FOUND_ERROR_CODE = "20020040";
	private static final String GET_EVENT_MESSAGE_MFID = "CB_CMN_GetEventMessageToDisplay_SRV";

	private SearchGlobalPartyRs searchGlobalPartyRs;
	private boolean isError;
	private String errorCode;
	private boolean resultFound;

	public void process(BankFusionEnvironment env) {
		logger.info("Entering into TI Customer Search");
		SearchGlobalPartyRq searchGlobalPartyRq = getF_IN_searchGlobalPartyRq();
		searchGlobalPartyRs = getF_OUT_searchGlobalPartyRs();
		searchGlobalPartyRs.getSearchGlobalPartyOutput()
				.removeAllPartySearchKey();
		

		RsHeader rsHeader = new RsHeader();
		SubCode subCode = new SubCode();
		MessageStatus msgStatus = new MessageStatus();
		msgStatus.addCodes(subCode);
		msgStatus.setOverallStatus("S");
		rsHeader.setStatus(msgStatus);

		try {
			if (null != searchGlobalPartyRq
					&& null != searchGlobalPartyRq.getSearchGlobalPartyInput()) {
				SearchGlobalPartyInput searchGlobalPartyInput = searchGlobalPartyRq
						.getSearchGlobalPartyInput();
				if (null != searchGlobalPartyInput.getPartySearchKey()) {
					PartySearchKey partySearchKey = searchGlobalPartyInput
							.getPartySearchKey();

					if (null != partySearchKey.getPartyIdDtl()
							&& null != partySearchKey.getPartyIdDtl()
									.getIdType()) {
						if (!partySearchKey.getPartyIdDtl().getIdType()
								.equals(CommonConstants.EMPTY_STRING)) {
							String partyIdType = partySearchKey.getPartyIdDtl()
									.getIdType();
							if (partyIdType.equalsIgnoreCase("00")) {
								/*
								 * Search includes neither External Customers
								 * not Internal Customers
								 */
								logger.info("Execution TI Customer Search Request for neither External Customer nor Internal Customers");
								isError = Boolean.TRUE;
								errorCode = ATLEAST_ONE_FILTER_ERROR_CODE;
								subCode.setCode(ATLEAST_ONE_FILTER_ERROR_CODE);
								msgStatus.setOverallStatus("E");
							} else {
								if (partyIdType.substring(1, 2).equals("1")) {
									/*
									 * Search for Internal Customer & Append
									 * records to searchAccountRs object.
									 */
									logger.info("Executing TI Customer Search Request for Internal Customers");
									addSearchResultsToGlobalPartyRs(internalCustomerSearch(
											env, searchGlobalPartyRq));

								}
								if (partyIdType.substring(0, 1).equals("1")) {
									/*
									 * Search for External Customers & append
									 * records to searchAccountRs object.
									 */
									logger.info("Executing TI Customer Search Request for External Customers");
										

									HashMap externalCustomerSearchInputParams = new HashMap();
									partySearchKey.getPartyIdDtl().setIdType(CommonConstants.EMPTY_STRING);
									externalCustomerSearchInputParams.put(
											SEARCH_GLOBAL_PARTY_RQ_INTAG,
											searchGlobalPartyRq);
									HashMap externalCustomerSearchOutputParams = MFExecuter
											.executeMF(
													SEARCH_GLOBAL_PARTY_MF_INF,
													env,
													externalCustomerSearchInputParams);
									addSearchResultsToGlobalPartyRs((SearchGlobalPartyRs) externalCustomerSearchOutputParams
											.get(SEARCH_GLOBAL_PARTY_RQ_OUTTAG));
								}
							}
						}
					} else {
						isError = Boolean.TRUE;
						errorCode = ATLEAST_ONE_FILTER_ERROR_CODE;
					}

				} else {
					isError = Boolean.TRUE;
					errorCode = ATLEAST_ONE_FILTER_ERROR_CODE;
				}

			} else {
				isError = Boolean.TRUE;
				errorCode = ATLEAST_ONE_FILTER_ERROR_CODE;
			}
		} catch (BankFusionException bfException) {
			msgStatus.setOverallStatus("F");
			logger.info(bfException.getMessageNumber() + " : "
					+ bfException.getLocalizedMessage());
			logger.info("BF Exception occured while doing Customer Search for TI "
					+ bfException.getStackTrace());
			Collection<IEvent> errors = bfException.getEvents();
			Iterator<IEvent> errorIterator = errors.iterator();
			IEvent event = errorIterator.next();
			// setting Status E for ERROR
			subCode.setCode(Integer.toString((event.getEventNumber())));
			subCode.setDescription(event.getDescription());
		} finally {
			String errorDesc;
			try {
				subCode.setCode(errorCode);
				if (isError || !resultFound) {
					msgStatus.setOverallStatus("E");
				} else {
					msgStatus.setOverallStatus("S");
				}
				HashMap errorMsgInputParam = new HashMap();
				errorMsgInputParam.put("MessageStatus", msgStatus);
				errorDesc = (String) (MFExecuter.executeMF(
						GET_EVENT_MESSAGE_MFID, env, errorMsgInputParam))
						.get("Message");
			} catch (Exception e) {
				logger.info("Exception occured while getting event message description "
						+ e.getStackTrace());
				errorDesc = "";
			}
			subCode.setDescription(errorDesc);
			searchGlobalPartyRs.setRsHeader(rsHeader);
		}

	}

	private SearchGlobalPartyRs internalCustomerSearch(
			BankFusionEnvironment env, SearchGlobalPartyRq searchGlobalPartyRq) {
		UB_CNF_InternalCustomerSearch internalCustSearch = new UB_CNF_InternalCustomerSearch(
				env);
		internalCustSearch.setF_IN_searchGlobalPartyRq(searchGlobalPartyRq);
		internalCustSearch.process(env);
		return internalCustSearch.getF_OUT_searchGlobalPartyRs();
	}

	private void addSearchResultsToGlobalPartyRs(
			SearchGlobalPartyRs searchResults) {
		if (null != searchResults
				&& null != searchResults.getSearchGlobalPartyOutput()
				&& null != searchResults.getSearchGlobalPartyOutput()
						.getPartySearchKey()
				&& searchResults.getSearchGlobalPartyOutput()
						.getPartySearchKeyCount() > 0) {
			int noOfResults = searchResults.getSearchGlobalPartyOutput()
					.getPartySearchKeyCount();

			for (int index = 0; index < noOfResults; index++) {
				searchGlobalPartyRs.getSearchGlobalPartyOutput()
						.addPartySearchKey(
								searchResults.getSearchGlobalPartyOutput()
										.getPartySearchKey(index));
				errorCode = CommonConstants.EMPTY_STRING;
				resultFound = Boolean.TRUE;
			}

		} else if (!resultFound) {
			resultFound = Boolean.FALSE;
			errorCode = NO_DETAILS_FOUND_ERROR_CODE;
		}

	}
}
