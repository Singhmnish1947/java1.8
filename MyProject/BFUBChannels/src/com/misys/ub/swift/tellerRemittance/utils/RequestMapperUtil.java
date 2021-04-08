package com.misys.ub.swift.tellerRemittance.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.finastra.api.paymentInititation.AccountIdentification4Choice;
import com.finastra.api.paymentInititation.ActiveOrHistoricCurrencyAndAmount;
import com.finastra.api.paymentInititation.AmountType4Choice;
import com.finastra.api.paymentInititation.BranchAndFinancialInstitutionIdentification5;
import com.finastra.api.paymentInititation.CashAccount24;
import com.finastra.api.paymentInititation.ClearingSystemIdentification2Choice;
import com.finastra.api.paymentInititation.ClearingSystemMemberIdentification2;
import com.finastra.api.paymentInititation.CreditTransferTransaction20;
import com.finastra.api.paymentInititation.ExchangeRate1;
import com.finastra.api.paymentInititation.FinancialInstitutionIdentification8;
import com.finastra.api.paymentInititation.GenericAccountIdentification1;
import com.finastra.api.paymentInititation.GenericPersonIdentification1;
import com.finastra.api.paymentInititation.InstructionForCreditorAgent1;
import com.finastra.api.paymentInititation.PartyIdentification43;
import com.finastra.api.paymentInititation.PaymentIdentification1;
import com.finastra.api.paymentInititation.PaymentInstruction16;
import com.finastra.api.paymentInititation.PaymentTypeInformation19;
import com.finastra.api.paymentInititation.PostalAddress6;
import com.finastra.api.paymentInititation.Purpose2Choice;
import com.finastra.api.paymentInititation.ReferredDocumentInformation6;
import com.finastra.api.paymentInititation.RemittanceInformation10;
import com.finastra.api.paymentInititation.ServiceLevel8Choice;
import com.finastra.api.paymentInititation.StructuredRemittanceInformation12;
import com.finastra.api.paymentInititation.SupplementaryData1;

public class RequestMapperUtil {
    private transient final static Log LOGGER = LogFactory.getLog(RequestMapperUtil.class);

    private RequestMapperUtil() {
    }

    /**
     * Method Description:
     * 
     * @param grpHdr
     * @return
     */
	public static Map<String, Object> buildInitgPty(PartyIdentification43 initgPty) {
		Map<String, Object> initgPtyMap = new LinkedHashMap<>();
		Map<String, Object> party11ChoiceMap = new LinkedHashMap<>();
		Map<String, Object> genericIdMap = new LinkedHashMap<>();
		Map<String, Object> othrMap = new LinkedHashMap<>();
		Map<String, Object> orgIdMap = new LinkedHashMap<>();
		List<Object> genericOrgIdList = new ArrayList<>();
		if (initgPty.getNm() != null) {
			initgPtyMap.put("Nm", initgPty.getNm());
		}

		if (initgPty.getId() != null && initgPty.getId().getOrgId() != null) {
			orgIdMap.put("AnyBIC", initgPty.getId().getOrgId().getAnyBIC());
		}

		if (initgPty.getId() != null && initgPty.getId().getPrvtId() != null
				&& initgPty.getId().getPrvtId().getOthr() != null) {
			List<GenericPersonIdentification1> othr = initgPty.getId().getPrvtId().getOthr();
			for (GenericPersonIdentification1 a : othr) {
				if (a.getId() != null) {
					genericIdMap.put("Id", a.getId());
				}

				if (a.getIssr() != null) {
					genericIdMap.put("Issr", a.getIssr());
				}
			}
			genericOrgIdList.add(genericIdMap);
			othrMap.put("Othr", genericOrgIdList);
		}
		party11ChoiceMap.put("PrvtId", othrMap);
		party11ChoiceMap.put("OrgId", orgIdMap);
		initgPtyMap.put("Id", party11ChoiceMap);

		return initgPtyMap;
	}

    /**
     * Method Description:
     * 
     * @param cstmrCdtTrfInitn
     * @return
     */
    public static List<Object> preparePaymentInstruction(List<PaymentInstruction16> pmtInfList) {
        List<Object> pmtInstructionList = new ArrayList<>();
        Map<String, Object> pmtInstructionMap = new LinkedHashMap<>();
        if (pmtInfList != null) {
            for (PaymentInstruction16 pmtInf : pmtInfList) {
                if (pmtInf.getPmtInfId() != null) {
                    pmtInstructionMap.put("PmtInfId", pmtInf.getPmtInfId());
                }

                if (pmtInf.getBtchBookg()) {
                    pmtInstructionMap.put("BtchBookg", pmtInf.getBtchBookg());
                }

                if (pmtInf.getPmtMtd() != null) {
                    pmtInstructionMap.put("PmtMtd", pmtInf.getPmtMtd());
                }

                if (pmtInf.getChrgBr() != null) {
                    pmtInstructionMap.put("ChrgBr", pmtInf.getChrgBr());
                }

                // SvcLvl
                if (pmtInf.getPmtTpInf() != null) {
                    pmtInstructionMap.put("PmtTpInf", buildPmtTpInf(pmtInf.getPmtTpInf()));
                }

                if (pmtInf.getReqdExctnDt() != null) {
                    pmtInstructionMap.put("ReqdExctnDt", pmtInf.getReqdExctnDt());
                }

                // 50 Ordering Customer
                if (pmtInf.getDbtr() != null) {
                    pmtInstructionMap.put("Dbtr", buildPartyIdentification43(pmtInf.getDbtr()));
                }

                // 50 Ordering Customer
                if (pmtInf.getDbtrAcct() != null) {
                    pmtInstructionMap.put("DbtrAcct", buildCashAccount24(pmtInf.getDbtrAcct()));
                }
                // 52 Ordering Institution
                if (pmtInf.getDbtrAgt() != null) {
                    pmtInstructionMap.put("DbtrAgt", buildBranchFinancialInst(pmtInf.getDbtrAgt()));
                }

                // 52 Ordering Institution
                if (pmtInf.getDbtrAgtAcct() != null) {
                    pmtInstructionMap.put("DbtrAgtAcct", buildCashAccount24(pmtInf.getDbtrAgtAcct()));
                }

                // CdtTrfTxInf
                if (pmtInf.getCdtTrfTxInf() != null) {
                    pmtInstructionMap.put("CdtTrfTxInf", prepareCustomerCreditTransferInitiationV06(pmtInf.getCdtTrfTxInf()));
                }
            }

            pmtInstructionList.add(pmtInstructionMap);
        }

        return pmtInstructionList;
    }

    public static Map<String, Object> buildBranchFinancialInst(BranchAndFinancialInstitutionIdentification5 branchInstitution) {
        // 52A
        Map<String, Object> branchMap = new LinkedHashMap<>();
        if (branchInstitution.getFinInstnId() != null) {
            Map<String, Object> finInstnIdMap = new LinkedHashMap<>();
            Map<String, Object> clrSysIdMap = new LinkedHashMap<>();
            FinancialInstitutionIdentification8 finInstnId = branchInstitution.getFinInstnId();
            if (finInstnId.getBICFI() != null) {
                finInstnIdMap.put("BICFI", finInstnId.getBICFI());
            }

            if (finInstnId.getClrSysMmbId() != null) {
                Map<String, Object> clrSysMmbIdMap = new LinkedHashMap<>();
                ClearingSystemMemberIdentification2 clrSysMmbId = finInstnId.getClrSysMmbId();

                // NCC Code (Clearing code): DbtrAgt/FinInstnId/ClrSysMmbId/ClrSysId/Cd
                Map<String, Object> clrSysIdMap1 = new LinkedHashMap<>();
                if (clrSysMmbId.getClrSysId() != null) {
                    ClearingSystemIdentification2Choice clrSysId = clrSysMmbId.getClrSysId();
                    clrSysIdMap1.put("Cd", clrSysId.getCd());
                    clrSysIdMap.put("ClrSysId", clrSysIdMap1);
                }

                // NCC (Clearing Code value):DbtrAgt/FinInstnId/ClrSysMmbId/MmbId
                if (clrSysMmbId.getMmbId() != null) {
                    clrSysIdMap.put("MmbId", clrSysMmbId.getMmbId());
                }
                clrSysMmbIdMap.put("ClrSysMmbId", clrSysIdMap);

            }
            // Name: DbtrAgt/FinInstnId/Nm
            if (branchInstitution.getFinInstnId().getNm() != null) {
                finInstnIdMap.put("Nm", branchInstitution.getFinInstnId().getNm());
            }

            // Address:DbtrAgt/FinInstnId/PstlAdr/AdrLine
            if (branchInstitution.getFinInstnId().getPstlAdr() != null) {
                finInstnIdMap.put("PstlAdr", buildPstlAdr(branchInstitution.getFinInstnId().getPstlAdr()));
            }

            branchMap.put("FinInstnId", finInstnIdMap);
        }
        return branchMap;
    }

    public static Map<String, Object> buildCashAccount24(CashAccount24 cashAccount) {
        Map<String, Object> cashAccountMap = new LinkedHashMap<>();
        Map<String, Object> acctIdentityMap = new LinkedHashMap<>();
        Map<String, Object> othrMap = new LinkedHashMap<>();
        if (cashAccount.getId() != null) {
            AccountIdentification4Choice acctIdentity = cashAccount.getId();
            // IBAN
            if (acctIdentity.getIBAN() != null) {
                othrMap.put("IBAN", acctIdentity.getIBAN());
            }
            // Account No: DbtrAcct/Id/Othr/Id
            if (acctIdentity.getOthr() != null) {
                GenericAccountIdentification1 othr = acctIdentity.getOthr();
                othrMap.put("Id", othr.getId());
            }
            acctIdentityMap.put("Othr", othrMap);
            cashAccountMap.put("Id", acctIdentityMap);
        }
        // account currency
        cashAccountMap.put("Ccy", cashAccount.getCcy());
        return cashAccountMap;
    }

    public static Map<String, Object> buildPartyIdentification43(PartyIdentification43 dbtr) {
        // Ordering Customer 50A
        Map<String, Object> dbtrMap = new LinkedHashMap<>();
        Map<String, Object> party11ChoiceMap = new LinkedHashMap<>();
        Map<String, Object> orgIdMap = new LinkedHashMap<>();

        if (dbtr.getNm() != null) {
            dbtrMap.put("Nm", dbtr.getNm());
        }

        if (dbtr.getPstlAdr() != null) {
            dbtrMap.put("PstlAdr", buildPstlAdr(dbtr.getPstlAdr()));
        }

        // BIC/BEI: Dbtr/Id/OrgId/AnyBIC
        if (dbtr.getId() != null && dbtr.getId().getOrgId() != null && dbtr.getId().getOrgId().getAnyBIC() != null) {
            orgIdMap.put("AnyBIC", dbtr.getId().getOrgId().getAnyBIC());
            party11ChoiceMap.put("OrgId", orgIdMap);
            dbtrMap.put("Id", party11ChoiceMap);
        }

        return dbtrMap;
    }

    /**
     * Method Description:Build postal address
     * 
     * @param pstlAdr
     * @return
     */
    public static Map<String, Object> buildPstlAdr(PostalAddress6 pstlAdr) {
        Map<String, Object> pstlAdrMap = new LinkedHashMap<>();
        if (pstlAdr != null && pstlAdr.getAdrLine() != null && !pstlAdr.getAdrLine().isEmpty()) {
            pstlAdr.getAdrLine().removeIf(StringUtils::isEmpty);
            pstlAdrMap.put("AdrLine", pstlAdr.getAdrLine());
        }
        return pstlAdrMap;
    }

    /**
     * Method Description:Build PaymentTypeInformation
     * 
     * @param pmtTpInf
     * @return
     */
    public static Map<String, Object> buildPmtTpInf(PaymentTypeInformation19 pmtTpInf) {
        Map<String, Object> pmtTpInfMap = new LinkedHashMap<>();
        Map<String, Object> svcLvlMap = new LinkedHashMap<>();
        if (pmtTpInf.getSvcLvl() != null) {
            ServiceLevel8Choice svcLvl = pmtTpInf.getSvcLvl();
            // Bank Instruction Code
            if (svcLvl.getCd() != null) {
                svcLvlMap.put("Cd", svcLvl.getCd());
            }
            // Bank Operation Code
            if (svcLvl.getPrtry() != null) {
                svcLvlMap.put("Prtry", svcLvl.getPrtry());
            }
        }
        pmtTpInfMap.put("SvcLvl", svcLvlMap);
        return pmtTpInfMap;

    }

    /**
     * Method Description:Prepare CustomerCreditTransferInitiationV06
     * 
     * @param pmtInf
     * @return
     */
    public static List<Object> prepareCustomerCreditTransferInitiationV06(List<CreditTransferTransaction20> cdtTrfTxInfList) {
        Map<String, Object> cdtTrfTxInfMap = new LinkedHashMap<>();
        List<Object> cdtTrfTxInfSuperList = new ArrayList<>();

        // CdtTrfTxInf
        if (cdtTrfTxInfList != null) {
            for (CreditTransferTransaction20 cdtTrfTxInf : cdtTrfTxInfList) {

                if (cdtTrfTxInf.getPmtId() != null) {
                    cdtTrfTxInfMap.put("PmtId", buildPmtId(cdtTrfTxInf.getPmtId()));
                }

                if (cdtTrfTxInf.getAmt() != null) {
                    cdtTrfTxInfMap.put("Amt", buildAmount(cdtTrfTxInf.getAmt()));
                }

                // XchgRateInf
                if (cdtTrfTxInf.getXchgRateInf() != null) {
                    cdtTrfTxInfMap.put("XchgRateInf", buildXchgRateInf(cdtTrfTxInf.getXchgRateInf()));
                }

                // 59
                // Beneficiary Customer
                if (cdtTrfTxInf.getCdtr() != null) {
                    cdtTrfTxInfMap.put("Cdtr", buildPartyIdentification43(cdtTrfTxInf.getCdtr()));
                }

                // 59
                // Beneficiary Customer
                if (cdtTrfTxInf.getCdtrAcct() != null) {
                    cdtTrfTxInfMap.put("CdtrAcct", buildCashAccount24(cdtTrfTxInf.getCdtrAcct()));
                }

                // TransactionTypeCode = CdtTrfTxInf/Purp/Prtry
                if (cdtTrfTxInf.getPurp() != null) {
                    cdtTrfTxInfMap.put("Purp", buildPurpose2Choice(cdtTrfTxInf.getPurp()));
                }

                // 57
                // Account With Institution
                if (cdtTrfTxInf.getCdtrAgt() != null) {
                    cdtTrfTxInfMap.put("CdtrAgt", buildBranchFinancialInst(cdtTrfTxInf.getCdtrAgt()));
                }

                // 57
                // Account With Institution
                if (cdtTrfTxInf.getCdtrAgtAcct() != null) {
                    cdtTrfTxInfMap.put("CdtrAgtAcct", buildCashAccount24(cdtTrfTxInf.getCdtrAgtAcct()));
                }

                // 56
                // IntermediaryInstitution
                if (cdtTrfTxInf.getIntrmyAgt1() != null) {
                    cdtTrfTxInfMap.put("IntrmyAgt1", buildBranchFinancialInst(cdtTrfTxInf.getIntrmyAgt1()));
                }
                // 56
                // IntermediaryInstitution
                if (cdtTrfTxInf.getIntrmyAgt1Acct() != null) {
                    cdtTrfTxInfMap.put("IntrmyAgt1Acct", buildCashAccount24(cdtTrfTxInf.getIntrmyAgt1Acct()));
                }

                // senderToReciever information
                if (cdtTrfTxInf.getInstrForCdtrAgt() != null && !cdtTrfTxInf.getInstrForCdtrAgt().get(0).isEmpty()) {
                    cdtTrfTxInfMap.put("InstrForCdtrAgt", buildInstrForCdtrAgt(cdtTrfTxInf.getInstrForCdtrAgt()));
                }

                if (cdtTrfTxInf.getRmtInf() != null) {
                    cdtTrfTxInfMap.put("RmtInf", buildRemittanceInformation(cdtTrfTxInf.getRmtInf()));
                }

				//Supplementary Data for cash mode
				if (cdtTrfTxInf.getSplmtryData() != null) {
					cdtTrfTxInfMap.put("SplmtryData", prepareSplmtryData(cdtTrfTxInf));
				}
                 
            }
            cdtTrfTxInfSuperList.add(cdtTrfTxInfMap);
        }
        return cdtTrfTxInfSuperList;
    }

    public static Map<String, Object> buildXchgRateInf(ExchangeRate1 xchgRateInf) {
        Map<String, Object> xchgRateInfMap = new LinkedHashMap<>();
        if (xchgRateInf.getUnitCcy() != null) {
            xchgRateInfMap.put("UnitCcy", xchgRateInf.getUnitCcy());
        }

        if (xchgRateInf.getRateTp() != null) {
            xchgRateInfMap.put("XchgRate", xchgRateInf.getXchgRate());
        }
        if (xchgRateInf.getRateTp() != null) {
            xchgRateInfMap.put("RateTp", xchgRateInf.getRateTp());
        }

        if (xchgRateInf.getCtrctId() != null) {
            xchgRateInfMap.put("CtrctId", xchgRateInf.getCtrctId());
        }

        return xchgRateInfMap;
    }

    public static Map<String, Object> buildPurpose2Choice(Purpose2Choice purp) {
        Map<String, Object> purpMap = new LinkedHashMap<>();
        if (purp != null) {
            purpMap.put("Prtry", purp.getPrtry());
        }
        return purpMap;
    }

    public static Map<String, Object> buildRemittanceInformation(RemittanceInformation10 rmtInf) {
        Map<String, Object> remitInfoMap = new LinkedHashMap<>();
        List<Object> structuredInfoList = new ArrayList<>();
        Map<String, Object> structuredInfoMap = new LinkedHashMap<>();

        List<Object> rfrdDocInfList = new ArrayList<>();

        if (rmtInf != null && rmtInf.getUstrd() != null) {
            remitInfoMap.put("Ustrd", rmtInf.getUstrd());
        }

        // document reference number
        // CdtTrfTxInf/RmtInf/Strd/RfrdDocInf/Nb
        if (rmtInf != null && rmtInf.getStrd() != null) {
            List<StructuredRemittanceInformation12> strdList = rmtInf.getStrd();
            for (StructuredRemittanceInformation12 str : strdList) {
                List<ReferredDocumentInformation6> rfrdDocInf = str.getRfrdDocInf();
                {
                    for (ReferredDocumentInformation6 ref : rfrdDocInf) {
                        Map<String, Object> referredDocumentMap = new LinkedHashMap<>();
                        referredDocumentMap.put("Nb", ref.getNb());
                        rfrdDocInfList.add(referredDocumentMap);
                    }
                }
                structuredInfoMap.put("RfrdDocInf", rfrdDocInfList);
            }
            structuredInfoList.add(structuredInfoMap);
            remitInfoMap.put("Strd", structuredInfoList);
        }

        return remitInfoMap;

    }

    // senderTorecieverInformation
    public static List<Object> buildInstrForCdtrAgt(List<InstructionForCreditorAgent1> instrForCdtrAgt) {
        List<Object> instrForCdtrAgtList = new ArrayList<>();
        if (instrForCdtrAgt != null) {
            Map<String, Object> instrInfMap = new LinkedHashMap<>();
            for (InstructionForCreditorAgent1 intr : instrForCdtrAgt) {
                instrInfMap.put("InstrInf", intr.getInstrInf());
                LOGGER.info("Sender To Reciever Info:::" + intr.getInstrInf());
            }
            instrForCdtrAgtList.add(instrInfMap);
        }
        return instrForCdtrAgtList;
    }

    // ActiveOrHistoricCurrencyAndAmount
    public static Map<String, Object> buildAmount(AmountType4Choice amount4Choice) {
        Map<String, Object> currencyMap = new LinkedHashMap<>();

        if (amount4Choice.getInstdAmt() != null) {
            currencyMap.put("InstdAmt", buildAmt(amount4Choice.getInstdAmt()));
        }

        if (amount4Choice.getEqvtAmt() != null) {
            Map<String, Object> eqvtAmtMap = new LinkedHashMap<>();
            eqvtAmtMap.put("Amt", buildAmt(amount4Choice.getEqvtAmt().getAmt()));
            if (amount4Choice.getEqvtAmt().getCcyOfTrf() != null) {
                eqvtAmtMap.put("CcyOfTrf", amount4Choice.getEqvtAmt().getCcyOfTrf());
            }
            currencyMap.put("EqvtAmt", eqvtAmtMap);
        }

        return currencyMap;
    }

    public static Map<String, Object> buildAmt(ActiveOrHistoricCurrencyAndAmount amt) {
        Map<String, Object> amountCcyMap = new LinkedHashMap<>();
        if (amt != null) {
            amountCcyMap.put("Amt", amt.getAmt());
            amountCcyMap.put("Ccy", amt.getCcy());
        }
        return amountCcyMap;
    }

    public static Map<String, Object> buildPmtId(PaymentIdentification1 pmtId) {
        Map<String, Object> pmtIdMap = new LinkedHashMap<>();
        pmtIdMap.put("InstrId", pmtId.getInstrId());
        pmtIdMap.put("EndToEndId", pmtId.getEndToEndId());
        return pmtIdMap;
    }

    /**
     * Method Description:prepare SplmtryData in CreditTransferTransaction20 block
     * 
     * @param cstmrCdtTrfInitn
     * @return
     */
    public static List<Object> prepareSplmtryData(CreditTransferTransaction20 cdtTrfTxInf) {
        List<Object> splmtryDataList = new ArrayList<>();
        Map<String, Object> splmtryDataMap = new LinkedHashMap<>();
        Map<String, Object> splmtryDataEnvlpMap = new LinkedHashMap<>();
        List<SupplementaryData1> splmtryDataRqList = cdtTrfTxInf.getSplmtryData();
        for (SupplementaryData1 splmtryData : splmtryDataRqList) {
            if (splmtryData.getEnvlp() != null) {
                Map<String, Object> tellerCashIndSupplementaryDataMap = new LinkedHashMap<>();
                if (splmtryData.getEnvlp().getDocument() != null) {
                    tellerCashIndSupplementaryDataMap.put("CashIndication",
                            splmtryData.getEnvlp().getDocument().getCashIndication());
                }
                tellerCashIndSupplementaryDataMap.put("BranchId", splmtryData.getEnvlp().getDocument().getBranchId());
                tellerCashIndSupplementaryDataMap.put("TransferMethod", splmtryData.getEnvlp().getDocument().getTransferMethod());
                splmtryDataEnvlpMap.put("Document", tellerCashIndSupplementaryDataMap);
                splmtryDataEnvlpMap.put("@xmlns", "http://fundtech.com/TellerCashIndSupplementaryData");
                splmtryDataMap.put("Envlp", splmtryDataEnvlpMap);
            }
        }
        splmtryDataList.add(splmtryDataMap);

        return splmtryDataList;
    }

}
