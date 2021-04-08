package com.trapedza.bankfusion.fatoms;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.ub.payment.swift.utils.PaymentSwiftUtils;
import com.misys.ub.swift.remittance.process.SwiftEventCodes;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_ValidateOptionJFields;


public class ValidateOptionJFields extends AbstractUB_SWT_ValidateOptionJFields {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("deprecation")
    public ValidateOptionJFields(BankFusionEnvironment env) {
        super(env);
    }

    public ValidateOptionJFields() {
    }

    private transient final static Log logger = LogFactory.getLog(ValidateOptionJFields.class.getName());

    public void process(BankFusionEnvironment env) throws BankFusionException {
    	
    	String identifierCode = getF_IN_IdentifierCode();
    	
    	validateTextfields ( getF_IN_text1(),identifierCode);
    	validateTextfields ( getF_IN_text2(),identifierCode);
    	validateTextfields ( getF_IN_text3(),identifierCode);
    	validateTextfields ( getF_IN_text4(),identifierCode);
    	validateTextfields ( getF_IN_text5(),identifierCode);
    	
    
    	
    	
    }
    
    /**
     * Method Description: Validate the length of the name and address fields 
     * @param nameAddr
     */
    private void validateTextfields (String  nameAddr, String identifierCode){
    	
    	if (StringUtils.isNotBlank(nameAddr) && StringUtils.isBlank(identifierCode) ) {
    		if (nameAddr.length()>40){
    			 PaymentSwiftUtils.handleEvent(SwiftEventCodes.E_LENGTH_OPTIONJ_EXCEEDED, new String[] {});
    		}
    	}
    }
    
    }