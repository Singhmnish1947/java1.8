package com.trapedza.bankfusion.fatoms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.subsystem.persistence.runtime.impl.BankFusionThreadLocal;
import com.trapedza.bankfusion.atm.sparrow.helper.ATMConstants;
import com.trapedza.bankfusion.bo.refimpl.IBOAttributeCollectionFeature;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CardTechMessageValidator;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractCardTech_RepairQueueValidationFatom;
import com.trapedza.bankfusion.utils.BankFusionMessages;

public class CardTech_RepairQueueValidationFatom extends AbstractCardTech_RepairQueueValidationFatom {

    /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $";
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private String cardNumber = CommonConstants.EMPTY_STRING;

    private String accountNumber = CommonConstants.EMPTY_STRING;

    private String currency = CommonConstants.EMPTY_STRING;

    private String errorMessage = CommonConstants.EMPTY_STRING;
    private String errorStatus = CommonConstants.EMPTY_STRING;

    CardTechMessageValidator messageValidator = new CardTechMessageValidator();
    /**
     * Holds the reference for logger object
     */
    private transient final static Log logger = LogFactory.getLog(CardTech_FileProcessFatom.class.getName());

    public CardTech_RepairQueueValidationFatom(BankFusionEnvironment env) {
        super(env);
    }

    public void process(BankFusionEnvironment env) throws BankFusionException {

        cardNumber = getF_IN_CardNumber();
        accountNumber = getF_IN_AccountNumber();
        currency = getF_IN_Currency();
        boolean isValid = validateMessageDetails(cardNumber, accountNumber, currency, env);
        setF_OUT_IsValid(isValid);
    }

    private boolean validateMessageDetails(String cardNo, String mainAccountID, String currency, BankFusionEnvironment env)
            throws BankFusionException {
        if (!messageValidator.isCardNumberValid(cardNumber, env)) {
            errorMessage = BankFusionMessages.getInstance().getFormattedEventMessage(40407510, new Object[] { cardNumber },
                    BankFusionThreadLocal.getUserSession().getUserLocale());
            errorStatus = ATMConstants.ERROR;
            logger.error(errorStatus + ": " + errorMessage);
            setF_OUT_ErrorMessage(errorMessage);
            return false;
        }
        else if (!messageValidator.isAccountExist(mainAccountID, env)) {
            errorMessage = BankFusionMessages.getInstance().getFormattedEventMessage(40407516, new Object[] { mainAccountID },
                    BankFusionThreadLocal.getUserSession().getUserLocale());
            errorStatus = ATMConstants.ERROR;
            logger.error(errorStatus + ": " + errorMessage);
            setF_OUT_ErrorMessage(errorMessage);
            return false;
        }
        else if (!messageValidator.isAccountMappedToCard(cardNumber, mainAccountID, env)) {
            errorMessage = BankFusionMessages.getInstance().getFormattedEventMessage(40407537, new Object[] { mainAccountID },
                    BankFusionThreadLocal.getUserSession().getUserLocale());
            errorStatus = ATMConstants.ERROR;
            logger.error(errorStatus + ": " + errorMessage);
            setF_OUT_ErrorMessage(errorMessage);
            return false;
        }
        else if (!isCurrencyValid(mainAccountID, currency, env)) {
            errorMessage = BankFusionMessages.getInstance().getFormattedEventMessage(40407513, new Object[] { currency },
                    BankFusionThreadLocal.getUserSession().getUserLocale());
            errorStatus = ATMConstants.ERROR;
            logger.error(errorStatus + ": " + errorMessage);
            setF_OUT_ErrorMessage(errorMessage);
            return false;
        }
        return true;

    }

    public boolean isCurrencyValid(String accountNumber, String CurrencyCode, BankFusionEnvironment env) {
        boolean result = false;
        try {

            if ((CurrencyCode == null) || (CommonConstants.EMPTY_STRING.equals(CurrencyCode))) {
                result = false;
                return result;
            }
            IBOAttributeCollectionFeature accountValues = (IBOAttributeCollectionFeature) env.getFactory().findByPrimaryKey(
                    IBOAttributeCollectionFeature.BONAME, accountNumber);
            if (accountValues.getF_ISOCURRENCYCODE().equalsIgnoreCase(CurrencyCode)) {
                result = true;
            }
            else {
                result = false;
            }

        }
        catch (Exception exception) {
            result = false;
        }
        return result;

    }
}
