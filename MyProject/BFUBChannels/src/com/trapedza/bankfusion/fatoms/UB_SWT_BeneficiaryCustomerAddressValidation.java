package com.trapedza.bankfusion.fatoms;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.constant.CommonConstants;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.microflow.MFExecuter;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_SWT_BeneficiaryCustomerAddressValidation;

@SuppressWarnings("PMD")

public class UB_SWT_BeneficiaryCustomerAddressValidation extends AbstractUB_SWT_BeneficiaryCustomerAddressValidation
{
	public String Errormsg = CommonConstants.EMPTY_STRING;
	public Boolean result = false;
	public String validationStatus = "false";
	private transient final static Log logger = LogFactory.getLog(UB_SWT_BeneficiaryCustomerAddressValidation.class.getName());
	
	public UB_SWT_BeneficiaryCustomerAddressValidation(BankFusionEnvironment env)
	{
		super(env);
	}
	BankFusionEnvironment env;
	
	int slases[] = new int[4];
	
	public void process(BankFusionEnvironment env)
	{
		this.env=env;
		this.AddressValidation();
		this.SetOutputParams();
		
	}
	public void AddressValidation()
	{
		if(getF_IN_beneCustomerAddress1().length()!=0 && getF_IN_beneCustomerAddress1().charAt(0)<='9'&& getF_IN_beneCustomerAddress1().charAt(1)=='/')
		{
			getSlases();
			if(slases[0]>0 && slases[0]<=9)
			{
				if (slases[0]==1)
				{
					if (validateSlases())
					{
						if(isNumBetween())
						{
						//now sequences of slases are correct , now to check  /2 is not without /3.
							if (checkPair())
						{

							// first occurence of 3 is /3/IN/.
							if(firstOccurence())
							{
								if(dataEmpty())
								{
									
								}
								else
								{
									validationStatus = "false";
									displayMessifError(ChannelsEventCodes.E_DATA_NOT_PRESENT_AFTER_SLASH,null, logger, env);
								}
//							//2 can't repeat
//								if(is_NotRepeat())
//								{
//									// Format is correct
//								}
//								else
//								{
//									// 2 can't repeat
//									String[] err = { "2" };
//									EventsHelper.handleEvent(ChannelsEventCodes.E_FORMAT_OF_BENEFICIARY_ADDRESS_LINE_IS_INVALID,err, new HashMap(), env);
//								}
								validationStatus = "true";
							}
							else 
							{
//								String[] err = { "3" };
//								 EventsHelper.handleEvent(ChannelsEventCodes.E_FORMAT_OF_BENEFICIARY_ADDRESS_LINE_IS_INVALID,err, new HashMap(), env);
								// first occurence of 3 should contain ISO country code
//								 Errormsg="40401034";
								validationStatus = "false";
							}

						}
						else
						{
							String[] err = { "2" };
							 EventsHelper.handleEvent(ChannelsEventCodes.E_ADDRESS_3_CANNOT_EXIST_WITHOUT_ADDR_2,err, new HashMap(), env);
							// 2 is always with 3 .
//							 Errormsg="40401034";
							validationStatus = "false";
//							Errormsg="Line 2 should be followed by line 3";
						}
					}
					else{
						validationStatus = "false";
						displayMessifError(ChannelsEventCodes.E_NUMERIC_VALUE_SHOULD_BE_BETWEEN_1_TO_3,null, logger, env);
						//Number Should be between 1 and 3
					}
					}
					else
					{
						displayMessifError(ChannelsEventCodes.E_ADDRESS_SHOULD_BE_IN_INCREASING_ORDER,null, logger, env);
						validationStatus = "false";
						Errormsg="40401034";
//						Errormsg = "Number should be in Increasing order and sholud be between 1 to 3";
					}
				}
				else
				{
					String[] err = { "1" };
					 EventsHelper.handleEvent(ChannelsEventCodes.E_FORMAT_OF_BENEFICIARY_ADDRESS_LINE_IS_INVALID,err, new HashMap(), env);
					//first line should be name, it should start with /1
					 Errormsg="40401034";
					validationStatus = "false";
//					Errormsg="Line 1 should be mandatory";
				}
			}
		}
		else
		{
			validationStatus="true";
			Errormsg="It is 59 message";
			//it is 59 not 59 F
		}
	}
	public void SetOutputParams()
	{
		setF_OUT_ValidationStatus(validationStatus);
		setF_OUT_ErrorMessage(Errormsg);
	}

	private void getSlases() {
		slases[0] = getF_IN_beneCustomerAddress1().trim().length() <= 0 ? -1 : Integer.parseInt(getF_IN_beneCustomerAddress1().trim().substring(0, 1));
		slases[1] = getF_IN_beneCustomerAddress2().trim().length() <= 0 ? -1 : Integer.parseInt(getF_IN_beneCustomerAddress2().trim().substring(0, 1));
		slases[2] = getF_IN_beneCustomerAddress3().trim().length() <= 0 ? -1 : Integer.parseInt(getF_IN_beneCustomerAddress3().trim().substring(0, 1));
		slases[3] = getF_IN_beneCustomerAddress4().trim().length() <= 0 ? -1 : Integer.parseInt(getF_IN_beneCustomerAddress4().trim().substring(0, 1));
	}
	
	private Boolean dataEmpty()
	{
		for(int i=0;i<=3;i++)
		{
			if(slases[i]!=-1)
			{
				if(i==0)
				{
					if(getF_IN_beneCustomerAddress1().charAt(getF_IN_beneCustomerAddress1().length()-1)=='/')
						return false;
				}
				else if(i==1)
				{
					if(getF_IN_beneCustomerAddress2().charAt(getF_IN_beneCustomerAddress2().length()-1)=='/')
						return false;
				}
				else if(i==2)
				{
					if(getF_IN_beneCustomerAddress3().charAt(getF_IN_beneCustomerAddress3().length()-1)=='/')
						return false;
				}
				else if(i==3)
				{
					if(getF_IN_beneCustomerAddress4().charAt(getF_IN_beneCustomerAddress4().length()-1)=='/')
						return false;
				}
			}
		}
		return true;
	}
	
	private Boolean checkPair()
	{
		Boolean t=false;
		for(int i=1;i<3;i++)
		{
			if(slases[i]==2 && (slases[i+1]==3 || slases[i+1]==2 ))
			{
			t=true;}	
		else if(slases[i]==2 && (slases[i+1]!=3 || slases[i+1]==-1) )
			{
				return false;}
		else 
		{
			t=true;
		}
		}
		if(slases[3]==2)
			return false;
		return t;
	}
		private Boolean firstOccurence()
		{ 
			Boolean t=false;
			for (int i=1;i<=3;i++)
			{
				if(slases[i]==3)
				{
					Map hmpParams = new HashMap();
					String CountryCode2Char;
					if(i==1)
					{
						
						if(getF_IN_beneCustomerAddress2().lastIndexOf('/')!=1)
						{
						hmpParams.put("CountryCode2Char", getF_IN_beneCustomerAddress2().substring(getF_IN_beneCustomerAddress2().indexOf('/') + 1, getF_IN_beneCustomerAddress2().lastIndexOf('/')));
//						CountryCode2Char=getF_IN_beneCustomerAddress2().substring(getF_IN_beneCustomerAddress2().indexOf('/') + 1, getF_IN_beneCustomerAddress2().lastIndexOf('/'));
						}
						else
						{
							hmpParams.put("CountryCode2Char", getF_IN_beneCustomerAddress2().substring(getF_IN_beneCustomerAddress2().indexOf('/') + 1, getF_IN_beneCustomerAddress2().length()));
//							CountryCode2Char=getF_IN_beneCustomerAddress2().substring(getF_IN_beneCustomerAddress2().indexOf('/') + 1, getF_IN_beneCustomerAddress2().length());
						}
					}
					else if (i==2)
					{
						if(getF_IN_beneCustomerAddress3().lastIndexOf('/')!=1){
						hmpParams.put("CountryCode2Char", getF_IN_beneCustomerAddress3().substring(getF_IN_beneCustomerAddress3().indexOf('/') + 1, getF_IN_beneCustomerAddress3().lastIndexOf('/')));
//						CountryCode2Char=getF_IN_beneCustomerAddress3().substring(getF_IN_beneCustomerAddress3().indexOf('/') + 1, getF_IN_beneCustomerAddress3().lastIndexOf('/'));
						}
						else{
							hmpParams.put("CountryCode2Char", getF_IN_beneCustomerAddress3().substring(getF_IN_beneCustomerAddress3().indexOf('/') + 1, getF_IN_beneCustomerAddress3().length()));
//							CountryCode2Char=getF_IN_beneCustomerAddress3().substring(getF_IN_beneCustomerAddress3().indexOf('/') + 1, getF_IN_beneCustomerAddress3().length());
						}
					}
					else
					{
							if(getF_IN_beneCustomerAddress4().lastIndexOf('/')!=1){
							hmpParams.put("CountryCode2Char", getF_IN_beneCustomerAddress4().substring(getF_IN_beneCustomerAddress4().indexOf('/') + 1, getF_IN_beneCustomerAddress4().lastIndexOf('/')));
//							CountryCode2Char=getF_IN_beneCustomerAddress4().substring(getF_IN_beneCustomerAddress4().indexOf('/') + 1, getF_IN_beneCustomerAddress4().lastIndexOf('/'));
							}
							else{
								hmpParams.put("CountryCode2Char", getF_IN_beneCustomerAddress4().substring(getF_IN_beneCustomerAddress4().indexOf('/') + 1, getF_IN_beneCustomerAddress4().length()));
//								CountryCode2Char=getF_IN_beneCustomerAddress4().substring(getF_IN_beneCustomerAddress4().indexOf('/') + 1, getF_IN_beneCustomerAddress4().length());
							}
					}
				         Map result = MFExecuter.executeMF("UB_SWT_Validate2CharCountryCode_SRV", env, hmpParams);
				         Boolean isCountryCodeInvalid = (Boolean)result.get("IsCountryCodeValid");
					if(isCountryCodeInvalid)
						return true;
//				        if (CountryCode2Char.equalsIgnoreCase("IN"))
				        else	
				        {
				        	EventsHelper.handleEvent(ChannelsEventCodes.E_COUNTRYCODEINVALID,new Object[] {}, new HashMap(), env);
				        	return false;
				        }
				} 
				else if(slases[i]!=3)
				{
					t=true;
				}
			}
			return t;
		}
	private Boolean isNumBetween()
	{
		for(int i=1;i<4;i++)
		{
			if(slases[i]<-1 || slases[i]>3 || slases[i]==0)
				return false;
		}
		return true;
	}
	private void displayMessifError(int val, String[] obj, Log logger,BankFusionEnvironment env) 
	{
		EventsHelper.handleEvent(val, obj, new HashMap(), env);
	}

	private Boolean validateSlases()
	{
		Boolean c=false;
		for (int i=0;i<3;i++)
		{
			if(slases[i]<=9)
			{
				if(slases[i]!=-1 && slases[i+1]!=-1)
				{
					if(slases[i]<=slases[i+1])
					{
						c=true;
					}
					else
					{
						c=false;
						//slases should be in increasing order.
						break;
					}
				}
				else if(slases[i]!=-1 && slases[i+1]==-1)
				{
					c=true;
					break;
				}
				else if(slases[i]==-1 && slases[i+1]!=-1)
				{
					c=false;
					//can't fill next line without filling earlier line.
					break;
				}
				else
				{
					c=false;
					// some other error is there
					break;
				}
			}
			else 
			{
				c=false;
				//it should have value between 1 to 3 raise event.
			}
		}
		
		return c;
	}
}
