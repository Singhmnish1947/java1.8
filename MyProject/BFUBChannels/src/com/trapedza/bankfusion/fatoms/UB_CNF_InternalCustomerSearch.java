package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
import com.misys.bankfusion.subsystem.persistence.SimplePersistentObject;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.misys.bankfusion.subsystem.persistence.runtime.impl.PagingData;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.events.IEvent;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_CNF_InternalCustomerSearch;
import com.trapedza.bankfusion.steps.refimpl.IUB_CNF_InternalCustomerSearch;

public class UB_CNF_InternalCustomerSearch extends
		AbstractUB_CNF_InternalCustomerSearch implements
		IUB_CNF_InternalCustomerSearch {

	public static final long serialVersionUID = 1L;

	private SearchGlobalPartyRs searchGlobalPartyRs;

	private static final Log logger = LogFactory
			.getLog(UB_CNF_InternalCustomerSearch.class.getName());

	private static final String ATLEAST_ONE_FILTER_ERROR_CODE = "20020238";

	private static final String GET_EVENT_MESSAGE_MFID = "CB_CMN_GetEventMessageToDisplay_SRV";

	private static final String SELECT_QUERY_INNER = "SELECT  DISTINCT "
			+ IBOCustomer.CUSTOMERCODE + " as PARTYID, "
			+ IBOCustomer.CUSTOMERTYPE + " as PARTYTYPE, "
			+ IBOCustomer.UBRELATIONSHIPMGRID + " as RELATIONSHIPMANAGERID, "
			+ IBOCustomer.ALPHACODE + " as NAME, " + IBOCustomer.SHORTNAME
			+ " as SHORTNAME, " + IBOCustomer.CUSTOMERSTATUS
			+ " as PARTYSTATUS, " + IBOCustomer.UBCUSTOMERSUBTYPE
			+ " as PARTYSUBTYPE " + " FROM " + IBOCustomer.BONAME;

	private static final String CUSTOMER_TYPE_INTERNAL = "I";
	private static final String INTERNAL_CUSTOMER_SUBTYPE = "I";

	public UB_CNF_InternalCustomerSearch(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) {

		logger.info("Entering Internal Customer Search");

		SearchGlobalPartyRq searchGlobalPartyRq = getF_IN_searchGlobalPartyRq();
		searchGlobalPartyRs = getF_OUT_searchGlobalPartyRs();
		searchGlobalPartyRs.getSearchGlobalPartyOutput()
				.removeAllPartySearchKey();
		executeInternalCustomerSearch(env, searchGlobalPartyRq);
	}

	private boolean isValidString(String value) {

		return value != null && value.length() > 0 ? true : false;
	}

	/*private boolean hasValidPrefix(String value) {
		return value.startsWith("%") ? true : false;
	}*/

	private void executeInternalCustomerSearch(BankFusionEnvironment env,
			SearchGlobalPartyRq searchGlobalPartyRq) {

		ArrayList queryParams = new ArrayList();
		StringBuffer WHERE_CLAUSE = new StringBuffer(" WHERE ");
		boolean andFlag = false;

		RsHeader rsHeader = new RsHeader();
		SubCode subCode = new SubCode();
		MessageStatus msgStatus = new MessageStatus();
		msgStatus.setOverallStatus("S");
		rsHeader.setStatus(msgStatus);

		try {
			if (null != searchGlobalPartyRq
					&& null != searchGlobalPartyRq.getSearchGlobalPartyInput()) {
				SearchGlobalPartyInput searchGlobalPartyInput = searchGlobalPartyRq
						.getSearchGlobalPartyInput();
				if (null != searchGlobalPartyInput.getPartySearchKey()) {
					PartySearchKey partySearchKeyObj = searchGlobalPartyInput
							.getPartySearchKey();

					if (null != partySearchKeyObj.getPartyIdDtl()
							&& null != partySearchKeyObj.getPartyIdDtl()
									.getIdType()) {
						if (!partySearchKeyObj.getPartyIdDtl().getIdType()
								.equals(CommonConstants.EMPTY_STRING)) {
							String partyIdType = partySearchKeyObj
									.getPartyIdDtl().getIdType();
							if (partyIdType.equalsIgnoreCase("00")) {
								/*
								 * Search includes neither External Customers
								 * not Internal Customers
								 */
								logger.info("Execution TI Customer Search Request for neither External Customer nor Internal Customers");
								subCode.setCode(ATLEAST_ONE_FILTER_ERROR_CODE);
								msgStatus.setOverallStatus("E");
							} else {
								if (logger.isDebugEnabled()) {
									logger.debug("executeInternalCustomerSearchQuery::internalCustomer::"
											+ CUSTOMER_TYPE_INTERNAL);
								}

								if (isValidString(CUSTOMER_TYPE_INTERNAL)) {
									andFlag = true;
									WHERE_CLAUSE
											.append(IBOCustomer.CUSTOMERTYPE
													+ " = ? ");
									queryParams.add(CUSTOMER_TYPE_INTERNAL);
								}

								String partyId = partySearchKeyObj.getPartyId();
								if (logger.isDebugEnabled()) {
									logger.debug("executeInternalCustomerSearchQuery::partyId::"
											+ partyId);
								}
								if (isValidString(partyId)) {
									if (andFlag) {
										WHERE_CLAUSE.append(" AND ");
									}
									andFlag = true;
									WHERE_CLAUSE
											.append(IBOCustomer.CUSTOMERCODE
													+ " like ? ");
									if (partyId.toUpperCase().contains("%")) {
										queryParams.add(partyId.toUpperCase());
									} else {
										queryParams.add("%"
												+ partyId.toUpperCase() + "%");
									}
								}

								String partyName = partySearchKeyObj.getName();
								if (logger.isDebugEnabled()) {
									logger.debug("executeInternalCustomerSearchQuery::partyName::"
											+ partyName);
								}
								if (isValidString(partyName)) {
									if (andFlag) {
										WHERE_CLAUSE.append(" AND ");
									}

									andFlag = true;
									WHERE_CLAUSE.append("UPPER("
											+ IBOCustomer.ALPHACODE
											+ ") like ? ");
									if (partyName.toUpperCase().contains("%")) {
										queryParams
												.add(partyName.toUpperCase());
									} else {
										queryParams
												.add("%"
														+ partyName
																.toUpperCase()
														+ "%");
									}
								}

								if (logger.isDebugEnabled()) {
									logger.debug("executeInternalCustomerSearchQuery::partySubType::"
											+ INTERNAL_CUSTOMER_SUBTYPE);
								}

								if (isValidString(INTERNAL_CUSTOMER_SUBTYPE)) {
									if (andFlag) {
										WHERE_CLAUSE.append(" AND ");
									}
									andFlag = true;
									WHERE_CLAUSE
											.append(IBOCustomer.UBCUSTOMERSUBTYPE
													+ " = ? ");
									queryParams.add(INTERNAL_CUSTOMER_SUBTYPE);

								}

								String partyRelationshipManager = partySearchKeyObj
										.getPartyRelationshipManager();
								if (logger.isDebugEnabled()) {
									logger.debug("executeInternalCustomerSearchQuery::partyRelationshipManager::"
											+ partyRelationshipManager);
								}
								if (isValidString(partyRelationshipManager)) {
									if (andFlag) {
										WHERE_CLAUSE.append(" AND ");
									}
									andFlag = true;
									WHERE_CLAUSE
											.append("UPPER("
													+ IBOCustomer.UBRELATIONSHIPMGRID
													+ ") like ? ");
									if(partyRelationshipManager.toUpperCase().contains("%")){
									queryParams.add(partyRelationshipManager.toUpperCase());}
									else{
										queryParams
										.add("%"
												+ partyRelationshipManager
														.toUpperCase()
												+ "%");
									}

								}

								String finalQuery = SELECT_QUERY_INNER
										+ WHERE_CLAUSE;

								logger.info("executeInternalCustomerSearchQuery::query::"+ finalQuery.toString());

								logger.info("internal customer search params "
										+ queryParams);

								PagingData pdate = new PagingData(
										searchGlobalPartyRq.getPagedQuery()
												.getPagingRequest()
												.getRequestedPage(),
										searchGlobalPartyRq.getPagedQuery()
												.getPagingRequest()
												.getNumberOfRows());
								pdate.setRequiresTotalPages(true);

								List<SimplePersistentObject> dbRows = BankFusionThreadLocal
										.getPersistanceFactory()
										.executeGenericQuery(finalQuery,
												queryParams, pdate, false);
								if (dbRows == null || dbRows.size() <= 0) {
									subCode.setCode("20020040");
									msgStatus.setOverallStatus("E");
								} else {
									for (SimplePersistentObject partyDetailsObj : dbRows) {

										PartySearchKey partySearchKey = new PartySearchKey();

										String partyID = partyDetailsObj
												.getDataMap().get("PARTYID") != null ? partyDetailsObj
												.getDataMap().get("PARTYID")
												.toString()
												: CommonConstants.EMPTY_STRING;
										logger.info("executeInternalCustomerSearchQuery::partyID::"
												+ partyID);
										partySearchKey.setPartyId(partyID);

										String partyNm = partyDetailsObj
												.getDataMap().get("NAME") != null ? partyDetailsObj
												.getDataMap().get("NAME")
												.toString()
												: CommonConstants.EMPTY_STRING;
										logger.info("executeInternalCustomerSearchQuery::partyName::"
												+ partyNm);
										partySearchKey.setName(partyNm);

										String partyType = partyDetailsObj
												.getDataMap().get("PARTYTYPE") != null ? partyDetailsObj
												.getDataMap().get("PARTYTYPE")
												.toString()
												: CommonConstants.EMPTY_STRING;
										logger.info("executeInternalCustomerSearchQuery::partyType::"
												+ partyType);
										partySearchKey.setPartyType(partyType);

										String relationshipManager = partyDetailsObj
												.getDataMap()
												.get("RELATIONSHIPMANAGERID") != null ? partyDetailsObj
												.getDataMap()
												.get("RELATIONSHIPMANAGERID")
												.toString()
												: CommonConstants.EMPTY_STRING;
										logger.info("executeInternalCustomerSearchQuery::relationshipManager::"
												+ relationshipManager);
										partySearchKey
												.setPartyRelationshipManager(relationshipManager);

										String partyShortName = partyDetailsObj
												.getDataMap().get("SHORTNAME") != null ? partyDetailsObj
												.getDataMap().get("SHORTNAME")
												.toString()
												: CommonConstants.EMPTY_STRING;
										logger.info("executeInternalCustomerSearchQuery::partyShortName::"
												+ partyShortName);
										partySearchKey
												.setPartyShortName(partyShortName);

										String partyStatus = partyDetailsObj
												.getDataMap()
												.get("PARTYSTATUS") != null ? partyDetailsObj
												.getDataMap()
												.get("PARTYSTATUS").toString()
												: CommonConstants.EMPTY_STRING;
										logger.info("executeInternalCustomerSearchQuery::partyStatus::"
												+ partyShortName);
										partySearchKey.setPartySts(partyStatus);

										String subType = partyDetailsObj
												.getDataMap().get(
														"PARTYSUBTYPE") != null ? partyDetailsObj
												.getDataMap()
												.get("PARTYSUBTYPE").toString()
												: CommonConstants.EMPTY_STRING;
										logger.info("executeInternalCustomerSearchQuery::partySubType::"
												+ subType);
										partySearchKey.setPartySubType(subType);

										searchGlobalPartyRs
												.getSearchGlobalPartyOutput()
												.addPartySearchKey(
														partySearchKey);

									}
								}

							}
						}
					} else {
						msgStatus.setOverallStatus("E");
						subCode.setCode(ATLEAST_ONE_FILTER_ERROR_CODE);
					}

				} else {
					msgStatus.setOverallStatus("E");
					subCode.setCode(ATLEAST_ONE_FILTER_ERROR_CODE);
				}

			} else {
				msgStatus.setOverallStatus("E");
				subCode.setCode(ATLEAST_ONE_FILTER_ERROR_CODE);
			}
		} catch (BankFusionException bfException) {
			msgStatus.setOverallStatus("F");
			logger.error(bfException.getMessageNumber() + " : "
					+ bfException.getLocalizedMessage());
			logger.info("BF Exception occured while doing Customer Search for TI ",  bfException);
			Collection<IEvent> errors = bfException.getEvents();
			Iterator<IEvent> errorIterator = errors.iterator();
			IEvent event = errorIterator.next();
			// setting Status E for ERROR
			subCode.setCode(Integer.toString((event.getEventNumber())));
			subCode.setDescription(event.getDescription());
		} finally {
			String errorDesc;
			try {
				HashMap errorMsgInputParam = new HashMap();
				errorMsgInputParam.put("MessageStatus", msgStatus);
				errorDesc = (String) (MFExecuter.executeMF(
						GET_EVENT_MESSAGE_MFID, env, errorMsgInputParam))
						.get("Message");
			} catch (Exception e) {
				logger.error("Exception occured while getting event message description ", e);
				errorDesc = "";
			}
			subCode.setDescription(errorDesc);
			searchGlobalPartyRs.setRsHeader(rsHeader);
		}

	}
}
