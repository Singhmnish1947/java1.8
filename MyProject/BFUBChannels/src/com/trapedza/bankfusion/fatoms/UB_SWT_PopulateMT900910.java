/* **********************************************************
 * Copyright (c) 2007 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 **/
package com.trapedza.bankfusion.fatoms;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.security.runtime.impl.BranchUtil;
import com.misys.ub.swift.SWT_Constants;
import com.misys.ub.swift.SWT_MT900910Constants;
import com.misys.ub.swift.UB_MT900910;
import com.misys.ub.swift.UB_SWT_DisposalObject;
import com.misys.ub.swift.UB_SWT_Util;
import com.trapedza.bankfusion.bo.refimpl.IBOBranch;
import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.bo.refimpl.IBOFinancialPostingMessage;
import com.trapedza.bankfusion.bo.refimpl.IBOSwtCustomerDetail;
import com.trapedza.bankfusion.bo.refimpl.IBOTransaction;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_PopulateMT900910;

/**
 * @author Madhuchandra Fatom class to validate disposal object to generate
 *         MT900, MT910 and MT992 message
 */

public class UB_SWT_PopulateMT900910 extends AbstractUB_SWT_PopulateMT900910 {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	String MessageName = CommonConstants.EMPTY_STRING;

	/**
	 * Default constructor which overrides super class constructor
	 *
	 * @param env
	 */
	/* NEW */
	private transient final static Log logger = LogFactory.getLog(UB_SWT_PopulateMT900910.class.getClass());
	private UB_SWT_Util util = new UB_SWT_Util();
	private static final String ADDRESSLINE1 = "ADDRESSLINE1";
	private static final String ADDRESSLINE2 = "ADDRESSLINE2";
	private static final String ADDRESSLINE3 = "ADDRESSLINE3";
	private final static String getMainAccTransactionRecord = "WHERE " + IBOTransaction.ACCOUNTPRODUCT_ACCPRODID + " = ? AND "
			+ IBOTransaction.REFERENCE + " = ?";
	private final static String getFinMainAccTransactionRecord = "WHERE " + IBOFinancialPostingMessage.PRIMARYID + " = ? AND "
	+ IBOFinancialPostingMessage.TRANSACTIONREF + " = ?";
	private final static String getMainAccTransactionRecordId = "WHERE " + IBOTransaction.ACCOUNTPRODUCT_ACCPRODID + " = ? AND "
			+ IBOTransaction.TRANSACTIONID + " = ?";
	private final static String getFinMainAccTransactionRecordId = "WHERE " + IBOFinancialPostingMessage.PRIMARYID + " = ? AND "
	+ IBOFinancialPostingMessage.TRANSACTIONID + " = ?";

	private static final String DEBIT_FLAG = "D";
	private static final String CREDIT_FLAG = "C";

	private static final String DEBIT_SYMBOL = "-";
	private static final String CREDIT_SYMBOL = "+";
	private static final String RECORD_EXISTS ="SELECT UBBICCODE FROM UBTB_SWTMSGBICMAP WHERE UBCUSTOMERCODE = ? AND UBMESSAGETYPE = ?";
	private IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();	

	public UB_SWT_PopulateMT900910(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Super class process method overriding to generate messages
	 */
		
	
	
	public void process(BankFusionEnvironment env) {
		// TODO Auto-generated method stub
		Timestamp bankFusionSystemDate = SystemInformationManager.getInstance().getBFBusinessDateTime();
		super.process(env);
		if (this.getF_IN_DisposalObject() != null) {
			UB_SWT_DisposalObject disposalObject = (UB_SWT_DisposalObject) this.getF_IN_DisposalObject();
			int CrDrFlag = ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getCrdrFlag();
			int CancelFlag = ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getCancelFlag();
			String messageType = ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getMessageType();
			IBOSwtCustomerDetail CustDetails = null;
			setF_OUT_disposalId(disposalObject.getDisposalRef());
			String debitCredit = null;
			String swiftActive = null;
			String receiverBic = CommonConstants.EMPTY_STRING;
			// if(disposalObject.getDealOriginator().equalsIgnoreCase("F")){
			if (getMessageType(env).equals(SWT_MT900910Constants.DBConfirm)) {
				CustDetails = getContraCustDetails(env);
				// debitCredit =
				// getContraCustDetails(env).getF_DRCONFIRMREQUIRED();
				// swiftActive = getContraCustDetails(env).getF_SWTACTIVE();
				// receiverBic = getContraCustDetails(env).getF_BICCODE();

			} else {
				if (disposalObject.getDealOriginator().equalsIgnoreCase("F")) {
					CustDetails = getMainCustDetails(env);
					// debitCredit =
					// getMainCustDetails(env).getF_CRCONFIRMREQUIRED();
					// swiftActive = getContraCustDetails(env).getF_SWTACTIVE();
					// receiverBic = getMainCustDetails(env).getF_BICCODE();

				} else {
					CustDetails = getContraCustDetails(env);
					// debitCredit =
					// getContraCustDetails(env).getF_CRCONFIRMREQUIRED();
					// swiftActive = getContraCustDetails(env).getF_SWTACTIVE();
					// receiverBic = getContraCustDetails(env).getF_BICCODE();
				}

			}
			// }else{
		
			// debitCredit= getContraCustDetails(env).getF_DRCONFIRMREQUIRED();
			// }
			if (CustDetails != null) {
				debitCredit = CustDetails.getF_CRCONFIRMREQUIRED();
				swiftActive = CustDetails.getF_SWTACTIVE();
				receiverBic = CustDetails.getF_BICCODE();
				if ((debitCredit.equalsIgnoreCase("Y") && swiftActive.equalsIgnoreCase("Y"))
						&& ((CrDrFlag < 8 && messageType.equalsIgnoreCase("900")) || (CrDrFlag < 8 && messageType.equalsIgnoreCase("910")) || (CrDrFlag < 8 || CrDrFlag < 8))
						&& receiverBic != null && !receiverBic.trim().equals(CommonConstants.EMPTY_STRING)) {
					if (disposalObject.getCrdrFlag() == 0 || disposalObject.getCrdrFlag() == 1) {
						setF_OUT_cancelFlagStatus(new Integer(9));
						if (disposalObject.getCrdrFlag() == 0) {
							setF_OUT_updatedFlag(new Integer(2));
						} else
							setF_OUT_updatedFlag(new Integer(3));
					} else if ((disposalObject.getCrdrFlag() == 4 || disposalObject.getCrdrFlag() == 5)
							&& (disposalObject.getCancelFlag() == 0)) {
						int cancelStatus = util.updateCancelFlag(env, 900910, disposalObject.getDisposalRef());
						setF_OUT_cancelFlagStatus(new Integer(cancelStatus));

						if (disposalObject.getCrdrFlag() == 4) {
							setF_OUT_updatedFlag(new Integer(6));
						} else
							setF_OUT_updatedFlag(new Integer(7));
					}
					ArrayList xmlTags = new ArrayList();

					// HashMap xmlTagValueMap = new HashMap();
					UB_MT900910 messageObject_900910 = new UB_MT900910();
					if (disposalObject.getTransactionStatus().indexOf("AM") != -1)
						messageObject_900910.setAction("A");

					if (CancelFlag == 0 && (CrDrFlag == 4 || CrDrFlag == 5)) {
						messageObject_900910.setAction("C");
						if (CrDrFlag == 4) {
							messageObject_900910.setMessageType("MT900");
							MessageName = "900";
						} else {
							messageObject_900910.setMessageType("MT910");
							MessageName = "910";
						}
						// xmlTagValueMap
						// .put(SWT_MT900910Constants.MessageType, "992");
						// messageObject_900910.setMessageType("MT992");

					} else if (messageType.equalsIgnoreCase("900") || CrDrFlag == 0) {
						// xmlTagValueMap
						// .put(SWT_MT900910Constants.MessageType, "900");

						messageObject_900910.setMessageType("MT900");
						MessageName = "900";

					} else {
						// xmlTagValueMap
						// .put(SWT_MT900910Constants.MessageType, "910");
						messageObject_900910.setMessageType("MT910");
						MessageName = "910";
					}
					boolean generate900910 = util.generateCategory2Message(((UB_SWT_DisposalObject) disposalObject).getValueDate(),
							((UB_SWT_DisposalObject) disposalObject).getPostDate(), env, ((UB_SWT_DisposalObject) disposalObject)
									.getContraAccCurrencyCode(), new java.sql.Date(bankFusionSystemDate.getTime()), MessageName);

					if (generate900910) {

						String disposalRef = getDisposalReference(env);
						// xmlTagValueMap.put(SWT_MT900910Constants.DisposalRef,disposalRef);
						messageObject_900910.setDisposalRef(disposalRef);
						String sender = getSender(env);
						// xmlTagValueMap.put(SWT_MT900910Constants.Sender,
						// sender);
						messageObject_900910.setSender(sender);
						String receiver = getReceiver(env);
					
						// xmlTagValueMap.put(SWT_MT900910Constants.Receiver,
						// receiver);
						messageObject_900910.setReceiver(receiver);
						// String transactionReference =
						// getTransactionReference(env);
						// xmlTagValueMap.put(SWT_MT900910Constants.TransactionReference,((SWT_DisposalObject)
						// getF_IN_DisposalObject()).getCurrentDealNumber());
						messageObject_900910.setTransactionReference(((UB_SWT_DisposalObject) getF_IN_DisposalObject())
								.getCurrentDealNumber());
						// String releatedReference = getReleatedReference(env);
						// xmlTagValueMap.put(SWT_MT900910Constants.ReleatedReference,
						// ((UB_SWT_DisposalObject)
						// getF_IN_DisposalObject()).getCurrentDealNumber());
						messageObject_900910.setRelatedReference(((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getCurrentDealNumber());
						// HashMap TAG11S = null;
						// TAG11S.put(SWT_MT103192Constants.MESSAGETYPE11S,
						// CommonConstants.EMPTY_STRING);
						// TAG11S.put(SWT_MT103192Constants.DATE11S,
						// CommonConstants.EMPTY_STRING);
						// TAG11S.put(SWT_MT103192Constants.TRANSREFNO20,
						// CommonConstants.EMPTY_STRING);
						// TAG11S.put(SWT_MT103192Constants.RELATEDREF21,
						// CommonConstants.EMPTY_STRING);

						// if (CancelFlag == 0) {

						// messageObject_900910 = getTag11s(env,
						// messageObject_900910);
//*********************************************************
						// }
						// xmlTagValueMap.put(SWT_MT900910Constants.TAG11S,
						// TAG11S);
						
						String custID = "";
						if(messageObject_900910.getMessageType().equalsIgnoreCase("MT900")) {
							custID=(((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getMainAccCustomerNumber());
						}
							else {
								custID=(((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getContraAccCustomerNumber());
							}
							
							
						
						String BICCODE_25P = null;
						Connection connection = factory.getJDBCConnection();
						ResultSet rs = null;
					    PreparedStatement preparedStatement = null;
					   // String srt = GetCustomerDetailsFromAccount
				  try {   
					    preparedStatement = connection.prepareStatement(RECORD_EXISTS);
					    preparedStatement.setString(1, custID);
						preparedStatement.setString(2,MessageName);
						rs = preparedStatement.executeQuery();
						while(rs.next())
						      	 BICCODE_25P = rs.getString("UBBICCODE");  
						preparedStatement.close();
					  }
					  catch(SQLException e){
				         logger.error(ExceptionUtil.getExceptionAsString(e));
				  }finally{
					  
					  try{
						  if(preparedStatement!=null )
						  preparedStatement.close();
						  if(rs!=null)
						  rs.close();
					  }catch(Exception e){
						  e.printStackTrace();
					  }
				  }
					  
					  
					  if((MessageName.equals("900") || MessageName.equals("910")) && BICCODE_25P != null)
							messageObject_900910.setAccountIdentificationP(getTransactionReference(env)+"$"+BICCODE_25P);
					  else 
						    messageObject_900910.setAccountIdentification(getTransactionReference(env));

						messageObject_900910 = getTransactiondetail(env, messageObject_900910);
						String dateTime = getDateTime(env);
						messageObject_900910.setTdDateTime(dateTime);
						

						String orderingInstitution = CommonConstants.EMPTY_STRING;
						String orderingCustomer = CommonConstants.EMPTY_STRING;
						IBOSwtCustomerDetail clientCustomerDetails = getClientCustDetail(env);
						String tag50 = CommonConstants.EMPTY_STRING;
						String tag52 = CommonConstants.EMPTY_STRING;

						/*
						 * MT910 may be generated in several cases.
						 *
						 * Case 1: MT910 message is generated for outgoing MT103
						 * (result of Bank Posting. In this case a separate
						 * MT910 entry may not be there in the disposal table.
						 * Instead this will be driven by confirmation flag)
						 *
						 * Case 2: MT 910 generation from FX module where there
						 * will be a separate entry in disposal table and the
						 * the message type column value will be 910.
						 *
						 * Case 3: MT910 message is generated for incoming
						 * MT103/MT202/MT205 messages.
						 *
						 * The ordering customer / ordering institute details
						 * populated in the existing code is for cases 1 & 2
						 * whereas for case 3 the ordering customer detail
						 * should be copied from incoming MT103 to outgoing 910.
						 *
						 * Hence to support Case 3, the transaction type code
						 * column is used to indicate that the 910 generation is
						 * for incoming MT103 since this column is not relevant
						 * for MT910.
						 *
						 * The existing code will continue to work for other
						 * cases as they are 'else' part.
						 */ 
						 
						 
						 if (messageType.equals("910") 
						 && ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getTransactionStatus().equals("INC"))
						{
							if (((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getOrderingCustomerIdentifierCode() != null
									&& ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getOrderingCustomerIdentifierCode().trim()
											.length() > 0) 
							{
								StringBuffer strbuff = new StringBuffer();
								if (((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getOrderingCustomerAccountNumber() != null
										&& (((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getOrderingCustomerAccountNumber().trim()
												.length() > 0))
								{
									strbuff.append(((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getOrderingCustomerAccountNumber());
									strbuff.append(SWT_Constants.delimiter);
								}
								strbuff.append(((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getOrderingCustomerIdentifierCode());
								orderingCustomer = strbuff.toString().trim();
								tag50 = "A";
							} else {
								tag50 = ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPayReceiveFlag();

								StringBuffer strbuff = new StringBuffer();
								if (disposalObject.getOrderingCustomerAccountNumber() != null
										&& (disposalObject.getOrderingCustomerAccountNumber().trim().length() > 0)) {
									strbuff.append(disposalObject.getOrderingCustomerAccountNumber());
									strbuff.append(SWT_Constants.delimiter);
								}
								if (((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getSI_OrdCustText1() != null
										&& ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getSI_OrdCustText1().trim().length() > 0) {
									strbuff.append(SWT_Constants.delimiter);
								}
								strbuff.append(((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getSI_OrdCustText1());

								if (((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getSI_OrdCustText2() != null
										&& ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getSI_OrdCustText2().trim().length() > 0) {
									strbuff.append(SWT_Constants.delimiter);
								}
								strbuff.append(((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getSI_OrdCustText2());

								if (((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getSI_OrdCustText3() != null
										&& ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getSI_OrdCustText3().trim().length() > 0) {
									strbuff.append(SWT_Constants.delimiter);
								}
								strbuff.append(((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getSI_OrdCustText3());

								if (((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getSI_OrdCustText4() != null
										&& ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getSI_OrdCustText4().trim().length() > 0) {
									strbuff.append(SWT_Constants.delimiter);
								}
								strbuff.append(((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getSI_OrdCustText4());
								orderingCustomer = strbuff.toString().trim();
							}
						} else {
							if (((CrDrFlag == 1 || CrDrFlag == 5))&&(((((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getOrderingCustomerIdentifierCode() != null)||(((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifier() != null)))) {
								
									// artf44780 changes start
									if (((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getOrderingCustomerIdentifierCode() != null
											&& ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getOrderingCustomerIdentifierCode()
													.trim().length() > 0) {
										StringBuffer strbuff = new StringBuffer();
										if (((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getOrderingCustomerAccountNumber() != null
												&& (((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getOrderingCustomerAccountNumber()
														.trim().length() > 0)) {
											strbuff.append(((UB_SWT_DisposalObject) getF_IN_DisposalObject())
													.getOrderingCustomerAccountNumber());
											strbuff.append(SWT_Constants.delimiter);
										}
										strbuff.append(((UB_SWT_DisposalObject) getF_IN_DisposalObject())
												.getOrderingCustomerIdentifierCode());
										orderingCustomer = strbuff.toString().trim();
										tag50 = "A";
									}
									// artf44780 changes end
									
									else if ((((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifier() != null || ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getOrderingCustomerAccountNumber() !=null) 
											&& (((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifier().trim().length() > 0 || ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getOrderingCustomerAccountNumber().trim().length() >0)  && ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd1().substring(0, 2).equalsIgnoreCase("1/") ) {
										if(!((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getOrderingCustomerAccountNumber().isEmpty())
										{
											orderingCustomer = ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getOrderingCustomerAccountNumber()
													+ SWT_Constants.delimiter
													+ ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd1()
													+ SWT_Constants.delimiter
													+ ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd2()
													+ SWT_Constants.delimiter
													+ ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd3()
													+ SWT_Constants.delimiter
													+ ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd4();
										}
										else if(!((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifier().isEmpty())
										{
										orderingCustomer = ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifier()
												+ SWT_Constants.delimiter
												+ ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd1()
												+ SWT_Constants.delimiter
												+ ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd2()
												+ SWT_Constants.delimiter
												+ ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd3()
												+ SWT_Constants.delimiter
												+ ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd4();
										}

										tag50 = "F";
									}
									// artf44780 changes start
									else if (((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd1() != null
											&& (((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd1().trim().length() > 0)) {
										StringBuffer strbuff = new StringBuffer();
										if (disposalObject.getOrderingCustomerAccountNumber() != null
												&& (disposalObject.getOrderingCustomerAccountNumber().trim().length() > 0)) {
											strbuff.append(disposalObject.getOrderingCustomerAccountNumber());
											strbuff.append(SWT_Constants.delimiter);
										}
										if (((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd1() != null
												&& ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd1().trim()
														.length() > 0) {
											strbuff.append(SWT_Constants.delimiter);
										}
										strbuff.append(((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd1());
										if (((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd2() != null
												&& ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd2().trim()
														.length() > 0) {
											strbuff.append(SWT_Constants.delimiter);
										}
										strbuff.append(((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd2());
										if (((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd3() != null
												&& ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd3().trim()
														.length() > 0) {
											strbuff.append(SWT_Constants.delimiter);
										}
										strbuff.append(((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd3());
										if (((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd4() != null
												&& ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd4().trim()
														.length() > 0) {
											strbuff.append(SWT_Constants.delimiter);
										}
										strbuff.append(((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getPartyIdentifierAdd4());
										orderingCustomer = strbuff.toString().trim();
										tag50 = "K";
									}
									// artf44780 changes end
									else {
										orderingCustomer = getCustomerDetailsString(clientCustomerDetails.getBoID(), env);
										tag50 = "K";
									}
								
							}
						}
						if (((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getMessageType().compareTo("103") != 0) {
							// artf44780 changes start
							// orderingInstitution =
							// getOrderingInstitution(env);
							if (!(disposalObject.getSI_PayToBICCode().equals(CommonConstants.EMPTY_STRING))
									|| (disposalObject.getSI_PayToBICCode().equals(CommonConstants.EMPTY_STRING))) {
								if (disposalObject.getOrderingInstitution() != null
										&& disposalObject.getOrderingInstitution().trim().length() > 0) {
									StringBuffer strbuff = new StringBuffer();
									if (disposalObject.getSI_OrdInstAccInfo() != null
											&& (disposalObject.getSI_OrdInstAccInfo().trim().length() > 0)) {
										strbuff.append(disposalObject.getSI_OrdInstAccInfo());
										strbuff.append(SWT_Constants.delimiter);
									}
									strbuff.append(disposalObject.getOrderingInstitution());
									orderingInstitution = strbuff.toString().trim();
									tag52 = "A";

								} else if (disposalObject.getSI_OrdInstText1() != null
										&& disposalObject.getSI_OrdInstText1().trim().length() > 0) {
									StringBuffer strbuff = new StringBuffer();
									if (disposalObject.getSI_OrdInstAccInfo() != null
											&& (disposalObject.getSI_OrdInstAccInfo().trim().length() > 0)) {
                                        strbuff.append(get35CharacterTextLine(disposalObject.getSI_OrdInstAccInfo()));
										strbuff.append(SWT_Constants.delimiter);
									}
									if (disposalObject.getSI_OrdInstText1() != null
											&& (disposalObject.getSI_OrdInstText1().trim().length() > 0)) {
										strbuff.append(SWT_Constants.delimiter);
									}
                                    strbuff.append(get35CharacterTextLine(disposalObject.getSI_OrdInstText1()));
									if (disposalObject.getSI_OrdInstText2() != null
											&& (disposalObject.getSI_OrdInstText2().trim().length() > 0)) {
										strbuff.append(SWT_Constants.delimiter);
									}
                                    strbuff.append(get35CharacterTextLine(disposalObject.getSI_OrdInstText2()));
									if (disposalObject.getSI_OrdInstText3() != null
											&& (disposalObject).getSI_OrdInstText3().trim().length() > 0) {
										strbuff.append(SWT_Constants.delimiter);
									}
                                    strbuff.append(get35CharacterTextLine(disposalObject.getSI_OrdInstText3()));
									if (disposalObject.getSI_OrdInstText4() != null
											&& (disposalObject.getSI_OrdInstText4().trim().length() > 0)) {
										strbuff.append(SWT_Constants.delimiter);
									}
                                    strbuff.append(get35CharacterTextLine(disposalObject.getSI_OrdInstText4()));
									orderingInstitution = strbuff.toString().trim();
									tag52 = "D";

								} else if (disposalObject.getTerm().toString().trim().equals("1")
										|| disposalObject.getTerm().toString().trim().equals("2")) {
									IBOSwtCustomerDetail clientsDetails = null;
									clientsDetails = getClientCustDetail(env);
									orderingInstitution = clientsDetails.getF_BICCODE().toString();
									tag52 = "A";
								} else {
									orderingInstitution = "";
									tag52 = "";
								}
							}
						} else {
							// orderingInstitution = sender;
							if (disposalObject.getOrderingInstitution() != null
									&& disposalObject.getOrderingInstitution().trim().length() > 0) {
								StringBuffer strbuff = new StringBuffer();
								if (disposalObject.getSI_OrdInstAccInfo() != null
										&& (disposalObject.getSI_OrdInstAccInfo().trim().length() > 0)) {
									strbuff.append(disposalObject.getSI_OrdInstAccInfo());
									strbuff.append(SWT_Constants.delimiter);
								}
								strbuff.append(disposalObject.getOrderingInstitution());
								orderingInstitution = strbuff.toString().trim();
								tag52 = "A";

							} else if (disposalObject.getSI_OrdInstText1() != null
									&& disposalObject.getSI_OrdInstText1().trim().length() > 0) {
								StringBuffer strbuff = new StringBuffer();
								if (disposalObject.getSI_OrdInstAccInfo() != null
										&& (disposalObject.getSI_OrdInstAccInfo().trim().length() > 0)) {
                                    strbuff.append(get35CharacterTextLine(disposalObject.getSI_OrdInstAccInfo()));
									strbuff.append(SWT_Constants.delimiter);
								}
								if (disposalObject.getSI_OrdInstText1() != null
										&& (disposalObject.getSI_OrdInstText1().trim().length() > 0)) {
									strbuff.append(SWT_Constants.delimiter);
								}
                                strbuff.append(get35CharacterTextLine(disposalObject.getSI_OrdInstText1()));
								if (disposalObject.getSI_OrdInstText2() != null
										&& (disposalObject.getSI_OrdInstText2().trim().length() > 0)) {
									strbuff.append(SWT_Constants.delimiter);
								}
                                strbuff.append(get35CharacterTextLine(disposalObject.getSI_OrdInstText2()));
								if (disposalObject.getSI_OrdInstText3() != null
										&& (disposalObject).getSI_OrdInstText3().trim().length() > 0) {
									strbuff.append(SWT_Constants.delimiter);
								}
                                strbuff.append(get35CharacterTextLine(disposalObject.getSI_OrdInstText3()));
								if (disposalObject.getSI_OrdInstText4() != null
										&& (disposalObject.getSI_OrdInstText4().trim().length() > 0)) {
									strbuff.append(SWT_Constants.delimiter);
								}
                                strbuff.append(get35CharacterTextLine(disposalObject.getSI_OrdInstText4()));
								orderingInstitution = strbuff.toString().trim();
								tag52 = "D";

							} else if (disposalObject.getTerm().toString().trim().equals("1")
									|| disposalObject.getTerm().toString().trim().equals("2")) {
								orderingInstitution = sender;
								tag52 = "A";
							} else {
								orderingInstitution = "";
								tag52 = "";
							}
							// artf44780 changes end
						}

						// xmlTagValueMap.put(SWT_MT900910Constants.OrderingCustomer,
						// orderingCustomer);

						// xmlTagValueMap.put(SWT_MT900910Constants.TAG50,
						// tag50);

						// xmlTagValueMap.put(SWT_MT900910Constants.OrderingInstitution,
						// orderingInstitution);
						// String tag52 = getTag52(env);
						/*
						 * The following workaround that exist in the earlier
						 * versions of BFUB impacts 910 generation for incoming
						 * MT103 being done as part of SWIFT 2012. The below
						 * workaround should work only for FX. Hence another
						 * workaround introduced for 'generating 910
						 * corresponding to incoming MT103'. When 910 record is
						 * written into SWIFT disposal table for an incoming
						 * MT103, the TxnTypeCode column, which is no meaning
						 * for 910 message, is populated with 'INC' indicating
						 * that the 910 is for incoming message.
						 */
						/*
						 * We are taking main client customer to confirm whether
						 * its a financial institution or not this work around
						 * is only for FX . MM & (T & D) still not covered
						 * because we are not sure what customer they are
						 * sending.
						 */
						if (((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getTransactionStatus().equals("INC")) {
							if (tag50.length() != 0) {
								messageObject_900910.setOrderingCustomer(orderingCustomer);
								messageObject_900910.setOrderingCustOption(tag50);
							} else {
								messageObject_900910.setOrderingInstitution(orderingInstitution);
								messageObject_900910.setOrderingInstOption(tag52);
							}
						} else {
							if (orderingCustomer==null) {
								messageObject_900910.setOrderingInstitution(orderingInstitution);
								messageObject_900910.setOrderingInstOption(tag52);
							} else if (orderingCustomer!=null) {
								messageObject_900910.setOrderingCustomer(orderingCustomer);
								messageObject_900910.setOrderingCustOption(tag50);
							}
						}
						
						if (messageObject_900910.getMessageType().equals("MT910"))
								{
							messageObject_900910.setOrderingInstitution(orderingInstitution);
							messageObject_900910.setOrderingInstOption(tag52);
						
							messageObject_900910.setOrderingCustomer(orderingCustomer);
							messageObject_900910.setOrderingCustOption(tag50);
								}
						// xmlTagValueMap.put(SWT_MT900910Constants.Tag52,
						// tag52);

						// if MT910 message
						if ((CrDrFlag == 1 || CrDrFlag == 5)
								&& ((UB_SWT_DisposalObject) getF_IN_DisposalObject()).getMessageType().compareTo("103") != 0) {
							String InterMediatoryInformation = getIntermediatoryInformation(env);
							// xmlTagValueMap.put(SWT_MT900910Constants.InterMediatory,
							// InterMediatoryInformation);
							// By sharan To remove the char A from the
							// Intermediary Bic code ref: Bug 13268
							if (InterMediatoryInformation.length() > 1) {
								messageObject_900910.setIntermediary(InterMediatoryInformation.substring(0, InterMediatoryInformation
										.length() - 1));
							}
							// String tag56 = getTag56(env); By sharan To remove
							// the char A from the Intermediary Bic code ref:
							// Bug 13268
							// xmlTagValueMap.put(SWT_MT900910Constants.Tag56,
							// tag56);
							// By sharan To remove the char A from the
							// Intermediary Bic code ref: Bug 13268
							if (InterMediatoryInformation.length() > 1) {
								messageObject_900910.setIntermediaryOption(InterMediatoryInformation.substring(InterMediatoryInformation
										.length() - 1));
							}
						}

						String senderToReceiverInformation = getSenderToReceiverInformation(env);
						// xmlTagValueMap.put(
						// SWT_MT900910Constants.SenderToReceiverInformation,
						// senderToReceiverInformation);
						messageObject_900910.setSenderToReceiverInformation(senderToReceiverInformation);

						// xmlTags.add(xmlTagValueMap);
						xmlTags.add(messageObject_900910);
						this.setF_OUT_XmlTagsInput(xmlTags);
						this.setF_OUT_BranchSortCode(this.getF_IN_BranchSortCode());

						setF_OUT_ispublish(util.IsPublish(disposalObject.getMessageType(), disposalObject.getConfirmationFlag(),
								disposalObject.getCancelFlag()));
				
						this.setF_OUT_generateMessage(Boolean.TRUE);
						
					}
				}
			} else {
				setF_OUT_updatedFlag(new Integer(9));
			}
			int msgStatus = util.updateFlagValues(env, 900910, disposalObject.getDisposalRef());
			setF_OUT_msgStatusFlag(new Integer(msgStatus));

		} else { // if disposal object is null
			setF_OUT_disposalId("0");
			this.setF_OUT_generateMessage(Boolean.FALSE);
		}

	}

	/**
	 * Method to get Tag56
	 *
	 * @param env
	 * @return
	 */
	@SuppressWarnings("unused")
	private String getTag56(BankFusionEnvironment env) {
		// TODO Auto-generated method stub

		String intermediatory_Info = getIntermediatoryInformation(env);
		if (intermediatory_Info.equals(CommonConstants.EMPTY_STRING)) {
			return intermediatory_Info;
		} else {
			return intermediatory_Info.substring(0, 1);
		}

	}

	/**
	 * Method to get intermediatory info
	 *
	 * @param env
	 * @return
	 */
    @SuppressWarnings("unused")
    private String getIntermediatoryInformation(BankFusionEnvironment env) {
        UB_SWT_DisposalObject disposalObject = (UB_SWT_DisposalObject) this.getF_IN_DisposalObject();
        String tempString = util.createSwiftTagString(disposalObject.getSI_IntermediatoryCode(),
                disposalObject.getSI_IntermediaryPartyIdentifier(),
                get35CharacterTextLine(disposalObject.getSI_IntermediatoryText1()),
                get35CharacterTextLine(disposalObject.getSI_IntermediatoryText2()),
                get35CharacterTextLine(disposalObject.getSI_IntermediatoryText3()),
                get35CharacterTextLine(disposalObject.getSI_IntermediatoryText4()));
        return tempString;
    }

	/**
	 *
	 * @param env
	 * @return @
	 */
	private IBOSwtCustomerDetail getContraCustDetails(BankFusionEnvironment env) {
		// contra account customer details
		IBOSwtCustomerDetail contraAccCustDetails = null;
		try {			
		    contraAccCustDetails = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(
			IBOSwtCustomerDetail.BONAME, ((UB_SWT_DisposalObject)this.getF_IN_DisposalObject()).getContraAccCustomerNumber());
			
		} catch (Exception e) {
			logger.info(e);
			
		}
		return contraAccCustDetails;
	}

	/**
	 *
	 * @param env
	 * @return @
	 */
	private IBOSwtCustomerDetail getMainCustDetails(BankFusionEnvironment env) {
		// main account customer details
		IBOSwtCustomerDetail mainAccCustDetails = null;
		try {			
			mainAccCustDetails = (IBOSwtCustomerDetail)env.getFactory().findByPrimaryKey(
		    IBOSwtCustomerDetail.BONAME,((UB_SWT_DisposalObject)this.getF_IN_DisposalObject()).getMainAccCustomerNumber());
			 
		} catch (Exception e) {
			logger.info(e);
		}
		return mainAccCustDetails;

	}

	private IBOSwtCustomerDetail getClientCustDetail(BankFusionEnvironment env) {

		IBOSwtCustomerDetail clientCustomer = (IBOSwtCustomerDetail) env.getFactory().findByPrimaryKey(IBOSwtCustomerDetail.BONAME,
				((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getClientNumber());
		return clientCustomer;
	}

	/**
	 *
	 * @param env
	 * @return @
	 */
	@SuppressWarnings("unused")
	private String getSenderToReceiverInformation(BankFusionEnvironment env) {
		// TODO Auto-generated method stub
		UB_SWT_DisposalObject disposalObject = (UB_SWT_DisposalObject) this.getF_IN_DisposalObject();
		return util.getBankToBankInfo(disposalObject);
	}


	/**
	 *
	 * @param env
	 * @return @
	 */
	@SuppressWarnings("unused")
	private String getOrderingInstitution(BankFusionEnvironment env) {
		// TODO Auto-generated method stub
		UB_SWT_DisposalObject disposalObject = (UB_SWT_DisposalObject) this.getF_IN_DisposalObject();
		String tempString = CommonConstants.EMPTY_STRING;
		IBOSwtCustomerDetail clientsDetails = null;
		if (!(disposalObject.getSI_PayToBICCode().equals(CommonConstants.EMPTY_STRING))
				|| (disposalObject.getSI_PayToBICCode().equals(CommonConstants.EMPTY_STRING))) {
			clientsDetails = getClientCustDetail(env);
			tempString = clientsDetails.getF_BICCODE();
			return tempString;
		} else {
			tempString = util.createSwiftTagString(disposalObject.getSI_PayToBICCode(), disposalObject.getSI_PayToPartyIdentifier(),
					disposalObject.getSI_PayToText1(), disposalObject.getSI_PayToText2(), disposalObject.getSI_PayToText3(), disposalObject
							.getSI_PayToText4());
			if (tempString.trim().length() == 0)
				return tempString;
			return tempString.substring(0, tempString.length() - 1);

		}
	}

	/**
	 *
	 * @param env
	 * @param xmlTagValueMap
	 * @return @
	 */
	private UB_MT900910 getTransactiondetail(BankFusionEnvironment env, UB_MT900910 messageObject_900910) {
		// TODO Auto-generated method stub
		String valueDate = CommonConstants.EMPTY_STRING;
		String currencyCode = CommonConstants.EMPTY_STRING;
		String amount = CommonConstants.EMPTY_STRING;
		// HashMap transDetails = new HashMap();

		if (((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getTransactionStatus().indexOf("ROL") != -1)
			valueDate = ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getPostDate().toString();
		else
			valueDate = ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getValueDate().toString();
		if ((((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getDealOriginator().equals("F"))) {
			if (((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getMessageType().compareTo("900") == 0) {
				currencyCode = ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getContraAccCurrencyCode();
				amount = util.DecimalRounding(((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getTransactionAmount().abs()
						.toString(), util.noDecimalPlaces(currencyCode, env));
			} else {
				currencyCode = ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getMainAccCurrencyCode();
				amount = util.DecimalRounding(((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getContractAmount().abs().toString(),
						util.noDecimalPlaces(currencyCode, env));
			}
		} else {
			currencyCode = ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getMainAccCurrencyCode();
			if(((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getFundingAmount()!=null && ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getFundingAmount().compareTo(CommonConstants.BIGDECIMAL_ZERO)>0)
				amount = util.DecimalRounding(((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getFundingAmount().abs().toString(),
						util.noDecimalPlaces(currencyCode, env));
				else{
					amount = util.DecimalRounding(((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getContractAmount().abs().toString(),
							util.noDecimalPlaces(currencyCode, env));
				}
			if(((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getContractAmount().equals(BigDecimal.ZERO)){
				amount = util.DecimalRounding(((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getTransactionAmount().abs().toString(),
						util.noDecimalPlaces(currencyCode, env));
			}
		}
		if ((((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getDealOriginator().equals("8"))) {
			amount = util.DecimalRounding(((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getContractAmount().abs().toString(),
					util.noDecimalPlaces(currencyCode, env));
		}

		/*
		 * amount = ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject())
		 * .getContractAmount().abs().toString();
		 */

		// transDetails.put(SWT_MT900910Constants.ValueDate, valueDate);
		// transDetails.put(SWT_MT900910Constants.CurrencyCode, currencyCode);
		// transDetails.put(SWT_MT900910Constants.Amount, amount);
		// int currScale =
		// SystemInformationManager.getInstance().getCurrencyScale(currencyCode);
		// BigDecimal tempAmount = new BigDecimal(amount);
		// BigDecimal amountForCurrency = tempAmount.movePointRight(currScale);
		messageObject_900910.setTdValueDate(valueDate);
		messageObject_900910.setTdCurrencyCode(currencyCode);
		messageObject_900910.setTdAmount(amount);
		return messageObject_900910;
	}

	private String getTransactionReference(BankFusionEnvironment env) {
		// TODO Auto-generated method stub
		// IBOSwtCustomerDetail custDetails = null;
		// String CustomerNumber = CommonConstants.EMPTY_STRING;
		UB_SWT_DisposalObject disposalObject = (UB_SWT_DisposalObject) this.getF_IN_DisposalObject();
		if (disposalObject.getDealOriginator().equalsIgnoreCase("F")) {
			if (this.getMessageType(env).equals(SWT_MT900910Constants.DBConfirm)) {
				return ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getContraAccountNo();
				// return ((UB_SWT_DisposalObject)
				// this.getF_IN_DisposalObject()).getMainAccountNo();
			} else {
				// return ((UB_SWT_DisposalObject)
				// this.getF_IN_DisposalObject()).getContraAccountNo();
				return ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getMainAccountNo();

			}
		} else {

			if (((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getTransactionStatus().equals("INC")) {
				return ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getMainAccountNo();
			} else if (disposalObject.getDealOriginator().equalsIgnoreCase("7") || disposalObject.getDealOriginator().equalsIgnoreCase("I")) {
				ArrayList params = new ArrayList();
				params.clear();
				params.add(disposalObject.getMainAccountNo());
				params.add(disposalObject.getCurrentDealNumber());
				IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
				List transRecords = factory.findByQuery(IBOTransaction.BONAME, getMainAccTransactionRecord, params, null, true);
				List fintransRecords = factory.findByQuery(IBOFinancialPostingMessage.BONAME, getFinMainAccTransactionRecord, params, null, true);

				if (transRecords.size() > 0){
				IBOTransaction mainAccTransactioRecord = (IBOTransaction) transRecords.get(0);
				if (this.getMessageType(env).equals(SWT_MT900910Constants.DBConfirm)) {
					if (mainAccTransactioRecord.getF_DEBITCREDITFLAG().equalsIgnoreCase(DEBIT_FLAG))
						return ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getMainAccountNo();
					else if (mainAccTransactioRecord.getF_DEBITCREDITFLAG().equalsIgnoreCase(CREDIT_FLAG))
						return ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getContraAccountNo();
					else
						return ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getMainAccountNo();
				} else {
					if (mainAccTransactioRecord.getF_DEBITCREDITFLAG().equalsIgnoreCase(CREDIT_FLAG))
						return ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getMainAccountNo();
					else if (mainAccTransactioRecord.getF_DEBITCREDITFLAG().equalsIgnoreCase(DEBIT_FLAG))
						return ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getContraAccountNo();
					else
						return ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getMainAccountNo();
				   }
				}else if (fintransRecords.size() > 0){ //artf1084261 Changes Start

					IBOFinancialPostingMessage mainFinAccTransactioRecord = (IBOFinancialPostingMessage) fintransRecords.get(0);
					if (this.getMessageType(env).equals(SWT_MT900910Constants.DBConfirm)) {
						if (mainFinAccTransactioRecord.getF_SIGN().equalsIgnoreCase(DEBIT_SYMBOL))
							return ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getMainAccountNo();
						else if (mainFinAccTransactioRecord.getF_SIGN().equalsIgnoreCase(CREDIT_SYMBOL))
							return ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getContraAccountNo();
						else
							return ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getMainAccountNo();
					} else {
						if (mainFinAccTransactioRecord.getF_SIGN().equalsIgnoreCase(CREDIT_SYMBOL))
							return ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getMainAccountNo();
						else if (mainFinAccTransactioRecord.getF_SIGN().equalsIgnoreCase(DEBIT_SYMBOL))
							return ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getContraAccountNo();
						else
							return ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getMainAccountNo();
					}
			    }  //artf1084261 Changes End
				else return ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getMainAccountNo();
			} else {
				if (this.getMessageType(env).equals(SWT_MT900910Constants.DBConfirm)) {
					return ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getContraAccountNo();
				} else {
					return ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getMainAccountNo();
				}
			}

		}

	}

	/**
	 *
	 * @param env
	 * @return @
	 */
	private String getReceiver(BankFusionEnvironment env) {
		// TODO Auto-generated method stub
		IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
		UB_SWT_DisposalObject disposalObject = (UB_SWT_DisposalObject) this.getF_IN_DisposalObject();
		if (disposalObject.getDealOriginator().equalsIgnoreCase("F")) {
			if (this.getMessageType(env).equals(SWT_MT900910Constants.DBConfirm) && this.getContraCustDetails(env) != null) {
				// return this.getMainCustDetails(env).getF_BICCODE();
				return this.getContraCustDetails(env).getF_BICCODE();

			} else {
				if (this.getMainCustDetails(env) != null)
					return this.getMainCustDetails(env).getF_BICCODE();
				else 
					return null;

			}
		} else if (disposalObject.getDealOriginator().equalsIgnoreCase("7") || disposalObject.getDealOriginator().equalsIgnoreCase("I")) {
			ArrayList params = new ArrayList();
			params.clear();
			params.add(disposalObject.getMainAccountNo());
			params.add(disposalObject.getCurrentDealNumber());
			List transRecords = factory.findByQuery(IBOTransaction.BONAME, getMainAccTransactionRecord, params, null, true);
			List fintransRecords = factory.findByQuery(IBOFinancialPostingMessage.BONAME, getFinMainAccTransactionRecord, params, null, true);

			if (transRecords.size() > 0){
			IBOTransaction mainAccTransactioRecord = (IBOTransaction) transRecords.get(0);
			if (this.getMessageType(env).equals(SWT_MT900910Constants.DBConfirm)) {
				if (mainAccTransactioRecord.getF_DEBITCREDITFLAG().equalsIgnoreCase(DEBIT_FLAG) && this.getMainCustDetails(env) != null)
					return this.getMainCustDetails(env).getF_BICCODE();
				else if (mainAccTransactioRecord.getF_DEBITCREDITFLAG().equalsIgnoreCase(CREDIT_FLAG) && this.getContraCustDetails(env) != null)
					return this.getContraCustDetails(env).getF_BICCODE();
				else {
					if (this.getContraCustDetails(env) != null)
						return this.getContraCustDetails(env).getF_BICCODE();
					else
						return null;
				}
					
			} else if (this.getMessageType(env).equals(SWT_MT900910Constants.CRConfirm)) {
				if (mainAccTransactioRecord.getF_DEBITCREDITFLAG().equalsIgnoreCase(CREDIT_FLAG) && this.getMainCustDetails(env) != null)
					return this.getMainCustDetails(env).getF_BICCODE();
				else if (mainAccTransactioRecord.getF_DEBITCREDITFLAG().equalsIgnoreCase(DEBIT_FLAG) && this.getContraCustDetails(env) != null)
					return this.getContraCustDetails(env).getF_BICCODE();
				else {
					if (this.getContraCustDetails(env) != null)
						return this.getContraCustDetails(env).getF_BICCODE();
					else
						return null;
				}
					
			}
		 }	else if (fintransRecords.size() > 0){ //artf1084261 Changes Start

			 IBOFinancialPostingMessage mainFinAccTransactioRecord = (IBOFinancialPostingMessage) fintransRecords.get(0);
				if (this.getMessageType(env).equals(SWT_MT900910Constants.DBConfirm)) {
					if (mainFinAccTransactioRecord.getF_SIGN().equalsIgnoreCase(DEBIT_SYMBOL) && this.getMainCustDetails(env) != null)
						return this.getMainCustDetails(env).getF_BICCODE();
					else if (mainFinAccTransactioRecord.getF_SIGN().equalsIgnoreCase(CREDIT_SYMBOL) && this.getContraCustDetails(env) != null)
						return this.getContraCustDetails(env).getF_BICCODE();
					else {
						if (this.getContraCustDetails(env) != null)
							return this.getContraCustDetails(env).getF_BICCODE();
						else
							return null;
					}
						
				} else if (this.getMessageType(env).equals(SWT_MT900910Constants.CRConfirm)) {
					if (mainFinAccTransactioRecord.getF_SIGN().equalsIgnoreCase(CREDIT_SYMBOL) && this.getMainCustDetails(env) != null)
						return this.getMainCustDetails(env).getF_BICCODE();
					else if (mainFinAccTransactioRecord.getF_SIGN().equalsIgnoreCase(DEBIT_SYMBOL) && this.getContraCustDetails(env) != null)
						return this.getContraCustDetails(env).getF_BICCODE();
					else {
						if (this.getContraCustDetails(env) != null)
							return this.getContraCustDetails(env).getF_BICCODE();
						else
							return null;
					}
						
				}
		   }  //artf1084261 Changes End
		}
		if (this.getMessageType(env).equals(SWT_MT900910Constants.DBConfirm) && this.getMainCustDetails(env) != null) {
			return this.getMainCustDetails(env).getF_BICCODE();
		} else {
			if (this.getContraCustDetails(env) != null)
				return this.getContraCustDetails(env).getF_BICCODE();
			else
				return null;
		}
		// return null;
	}
	

	/**
	 *
	 * @param env
	 * @return @
	 */
	private String getSender(BankFusionEnvironment env) {
		// TODO Auto-generated method stub
		IBOBranch branchObj = BranchUtil.getBranchDetailsInCurrentZone(getF_IN_BranchSortCode());
		return branchObj.getF_BICCODE();
		// return null;
	}

	@SuppressWarnings("unused")
	private String getDateTime(BankFusionEnvironment env){
		IPersistenceObjectsFactory factory = BankFusionThreadLocal.getPersistanceFactory();
		UB_SWT_DisposalObject disposalObject = (UB_SWT_DisposalObject) this.getF_IN_DisposalObject();
		String dateTime = CommonConstants.EMPTY_STRING;
		ArrayList params = new ArrayList();
		params.clear();
		params.add(disposalObject.getMainAccountNo());
		params.add(disposalObject.getTransactionId());
		List transRecords = factory.findByQuery(IBOTransaction.BONAME, getMainAccTransactionRecordId, params, null, true);
		List fintransRecords = factory.findByQuery(IBOFinancialPostingMessage.BONAME, getFinMainAccTransactionRecordId, params, null, true);
		if (transRecords.size() > 0){
			IBOTransaction mainAccTransactioRecord = (IBOTransaction) transRecords.get(0);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmZ");
			dateTime = dateFormat.format(mainAccTransactioRecord.getF_POSTINGDATE());
		}
			else if(fintransRecords.size() > 0){
				 IBOFinancialPostingMessage mainFinAccTransactioRecord = (IBOFinancialPostingMessage) fintransRecords.get(0);
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmZ");
				 dateTime  = dateFormat.format(mainFinAccTransactioRecord.getF_TRANSACTIONDATE());
			}
		return dateTime;
		}


	/**
	 *
	 * @param env
	 * @return
	 */
	@SuppressWarnings("unused")
	private String getDisposalReference(BankFusionEnvironment env) {
		// TODO Auto-generated method stub
		return ((UB_SWT_DisposalObject) this.getF_IN_DisposalObject()).getDisposalRef();
		// return null;
	}

	/**
	 *
	 * @param env
	 * @return
	 */
	@SuppressWarnings("unused")
	private String getMessageType(BankFusionEnvironment env) {
		// TODO Auto-generated method stub
		UB_SWT_DisposalObject disposalObject = (UB_SWT_DisposalObject) this.getF_IN_DisposalObject();
			if (disposalObject.getCancelFlag() == 0 && (disposalObject.getCrdrFlag() == 2 || disposalObject.getCrdrFlag() == 3))
			return SWT_MT900910Constants.CRDBCancel;
		if ((SWT_MT900910Constants.DBConfirm.equals(disposalObject.getMessageType()) || disposalObject.getCrdrFlag() == 0)
				&& disposalObject.getCancelFlag() > 0)
			return SWT_MT900910Constants.DBConfirm;
		if ((SWT_MT900910Constants.CRConfirm.equals(disposalObject.getMessageType()) || disposalObject.getCrdrFlag() == 1)
				&& disposalObject.getCancelFlag() > 0)
			return SWT_MT900910Constants.CRConfirm;

		return SWT_MT900910Constants.InvalidMessage;

	}

	private String getCustomerDetailsString(String custCode, BankFusionEnvironment env) {
		String custNameAddressString = null;
		try {
			// IBOBroker BrokerBo = (IBOBroker)
			// env.getFactory().findByPrimaryKey(IBOBroker.BONAME, custCode);
			// IBOCustomer customerBO = (IBOCustomer)
			// env.getFactory().findByPrimaryKey(IBOCustomer.BONAME,
			// BrokerBo.getF_CUSTOMERCODE());
			IBOCustomer customerBO = (IBOCustomer) env.getFactory().findByPrimaryKey(IBOCustomer.BONAME, custCode);
			StringBuffer cBuffer = new StringBuffer();
			cBuffer.append(customerBO.getF_SHORTNAME() + "$");
			// String whereCluaseForAddressLink = " WHERE " +
			// IBOAddressLinks.CUSTACC_KEY + " = ? AND "
			// + IBOAddressLinks.DEFAULTADDRINDICATOR + " = ?";
			// ArrayList params = new ArrayList();
			// params.add(customerBO.getBoID());
			// params.add(new Boolean(true));
			// ArrayList addressLinkList = (ArrayList)
			// env.getFactory().findByQuery(IBOAddressLinks.BONAME,
			// whereCluaseForAddressLink, params, null);
			// IBOAddressLinks addressLink = (IBOAddressLinks)
			// addressLinkList.get(0);
			// IBOAddress addressDetails = (IBOAddress)
			// env.getFactory().findByPrimaryKey(IBOAddress.BONAME,
			// addressLink.getF_ADDRESSID());
			Map<String, Object> addressDetails = new HashMap<String, Object>();
			addressDetails = util.getAddress(custCode, env);
			String addressline1 = addressDetails.get(ADDRESSLINE1).toString();
			String addressline2 = addressDetails.get(ADDRESSLINE2).toString();
			String addressline3 = addressDetails.get(ADDRESSLINE3).toString();
			cBuffer.append(addressline1 + "$" + addressline2 + "$" + addressline3);
			custNameAddressString = cBuffer.toString();
		} catch (BankFusionException bfe) {
			logger.error("Error while getting customer name and address", bfe);
		}
		return custNameAddressString;
	}

    /**
     * @param str
     * @return
     */
    private String get35CharacterTextLine(String str) {
        String output = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(str)) {
            if (str.length() <= 35) {
                output = str.substring(0, str.length());
            }
            else {
                output = str.substring(0, 35);
            }
        }
        return output;
    }

}
