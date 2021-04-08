package com.trapedza.bankfusion.fatoms;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.bankfusion.subsystem.microflow.runtime.impl.MFExecuter;
import com.misys.ub.payment.swift.utils.MFInputOutPutKeys;
import com.misys.ub.payment.swift.utils.PaymentSwiftConstants;
import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.misys.ub.swift.remittance.process.SwiftEventCodes;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_ConstructOptionJ;

/**
 * Class file constructing Option J
 * 
 * @author schowdh4
 *
 */
public class UB_SWT_ConstructOptionJ extends AbstractUB_SWT_ConstructOptionJ {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("deprecation")
    public UB_SWT_ConstructOptionJ(BankFusionEnvironment env) {
        super(env);
    }

    public UB_SWT_ConstructOptionJ() {
    }

    private transient final static Log logger = LogFactory.getLog(UB_SWT_ConstructOptionJ.class.getName());

    @Override
    public void process(BankFusionEnvironment env) throws BankFusionException {
        String countryCode = getF_IN_CountryCode();

        String partyIdentificationCode = getF_IN_PartyIdentifierCode();
        String value = getF_IN_Value();
        String mode = getF_IN_Mode();
        switch (mode) {
            case "ONSELECT":
                onSelectItem(partyIdentificationCode, countryCode, env);
                break;

            case "ONFOCUSLOST":
                onFocusLost(partyIdentificationCode, countryCode, value, env);
                break;

            case "ONLOAD":
                setF_OUT_ReadOnly(Boolean.TRUE);
                setF_OUT_enableBicSearch(Boolean.TRUE);
                break;

            case "VALIDATE":
                validateCodeValueForLEIC(value, partyIdentificationCode);
                validateCodeValueForCLRC(value, countryCode, partyIdentificationCode, env);
                validateCodeValueForABIC(value, partyIdentificationCode, env);
                break;

            default:
                break;
        }

    }

    /**
     * Method Description:On Focus Lost event of the Value text field
     * 
     * @param partyIdentificationCode
     * @param countryCode
     * @param value
     * @param env
     */
    private void onFocusLost(String partyIdentificationCode, String countryCode, String value, BankFusionEnvironment env) {

        validateCodeValueForLEIC(value, partyIdentificationCode);

        validateCodeValueForCLRC(value, countryCode, partyIdentificationCode, env);

        setF_OUT_outputString(getOutputString(partyIdentificationCode, countryCode, value));
    }

    /**
     * Method Description:
     * 
     * @param value
     * @param countryCode
     * @param partyIdentificationCode
     * @param env
     */
    private void validateCodeValueForABIC(String value, String partyIdentificationCode, BankFusionEnvironment env) {
        if (PaymentSwiftConstants.PARTY_IDENT_ABIC.equals(partyIdentificationCode) && value.isEmpty()) {
            PaymentSwiftUtils.handleEvent(Integer.parseInt(SwiftEventCodes.E_CB_CMN_MANDATORY_ENTRY_CB05),
                    new String[] { "BIC Code" });

        }
    }

    /**
     * Method Description:Masking validation based on the NCC clearing code
     * 
     * @param value
     * @param countryCode
     */
    private void validateCodeValueForCLRC(String value, String countryCode, String partyIdentificationCode,
            BankFusionEnvironment env) {
        if (PaymentSwiftConstants.PARTY_IDENT_CLRC.equals(partyIdentificationCode) && !value.isEmpty()) {
            HashMap map = new HashMap();
            map.put(MFInputOutPutKeys.IDENTIFIER_CODE, value);
            map.put(MFInputOutPutKeys.NCC_CODE, countryCode);
            try {
                MFExecuter.executeMF(MFInputOutPutKeys.UB_SWT_ValidateNCCCodes_SRV, env, map);
            }
            catch (BankFusionException e) {
                logger.error(ExceptionUtil.getExceptionAsString(e));
                throw e;
            }
        }

    }

    /**
     * Method Description:Validate value for code option LEIC
     * 
     * @param value
     */
    private void validateCodeValueForLEIC(String value, String partyIdentificationCode) {
        if (PaymentSwiftConstants.PARTY_IDENT_LEIC.equals(partyIdentificationCode)) {
            if (logger.isInfoEnabled())
                logger.info("value.length():::::: " + value.length());

            // validation to add 18 characters and 2 numbers
            if (value.length() == 20) {
                String message = value.substring(0, value.length() - 2);
                if (!StringUtils.isAlphanumeric(message)) {
                    PaymentSwiftUtils.handleEvent(SwiftEventCodes.E_LEIC_CODE_VALUE_FBE, new String[] {});
                }

                String lastTwoNumbers = value.substring(value.length() - 2);
                if (!StringUtils.isNumeric(lastTwoNumbers)) {
                    PaymentSwiftUtils.handleEvent(SwiftEventCodes.E_LEIC_CODE_VALUE_FBE, new String[] {});
                }
            }
            else {
                PaymentSwiftUtils.handleEvent(SwiftEventCodes.E_LEIC_CODE_VALUE_FBE, new String[] {});
            }
        }
    }

    /**
     * Method Description:
     * 
     * @param partyIdentificationCode
     * @param countryCode
     * @param env
     */
    private void onSelectItem(String partyIdentificationCode, String countryCode, BankFusionEnvironment env) {
        Boolean enableBICSearch = Boolean.FALSE;
        Boolean enableCountryCode = Boolean.FALSE;
        partyIdentificationCode = getValidPartyIdentifier(partyIdentificationCode);

        if (!StringUtils.isEmpty(partyIdentificationCode)) {
            switch (partyIdentificationCode) {
                case PaymentSwiftConstants.PARTY_IDENT_ABIC:
                    enableCountryCode = Boolean.TRUE;
                    countryCode = StringUtils.EMPTY;
                    break;

                case PaymentSwiftConstants.PARTY_IDENT_CLRC:
                    enableBICSearch = Boolean.TRUE;
                    break;

                case PaymentSwiftConstants.PARTY_OTHERS:
                    enableBICSearch = Boolean.TRUE;
                    enableCountryCode = Boolean.TRUE;
                    countryCode = StringUtils.EMPTY;
                    break;

                default:
                    break;
            }
        }

        setF_OUT_CountryCode(countryCode);
        setF_OUT_ReadOnly(enableCountryCode);
        setF_OUT_enableBicSearch(enableBICSearch);
    }

    /**
     * Method Description:Concat the required output string
     * 
     * @param partyIdentificationCode
     * @param nccClearingode
     * @param value
     * @return
     */
    private String getOutputString(String partyIdentificationCode, String nccClearingode, String value) {
        StringBuilder outputString = new StringBuilder();
        String slash = "/";

        outputString.append(slash).append(partyIdentificationCode).append(slash);

        if (!nccClearingode.isEmpty()) {
            outputString.append(nccClearingode);
        }

        if (!value.isEmpty()) {
            outputString.append(value);
        }

        return outputString.toString();
    }

    private enum PartyIdentificationCodeEnum {
        ACCT, ADD1, ADD2, CITY, NAME, SVBY, TXID, LEIC;
    }

    /**
     * Method Description:Get the Valid PartyIdentifier
     * 
     * @param partyIdentificationCode
     * @return
     */
    private String getValidPartyIdentifier(String partyIdentificationCode) {
        String code = "";
        switch (partyIdentificationCode) {
            case PaymentSwiftConstants.PARTY_IDENT_ABIC:
                code = PaymentSwiftConstants.PARTY_IDENT_ABIC;
                break;

            case PaymentSwiftConstants.PARTY_IDENT_CLRC:
                code = PaymentSwiftConstants.PARTY_IDENT_CLRC;
                break;
            default:
                for (PartyIdentificationCodeEnum myVar : PartyIdentificationCodeEnum.values()) {
                    if (partyIdentificationCode.equals(myVar.toString())) {
                        code = PaymentSwiftConstants.PARTY_OTHERS;
                    }
                }
                break;
        }
        return code;

    }

}
