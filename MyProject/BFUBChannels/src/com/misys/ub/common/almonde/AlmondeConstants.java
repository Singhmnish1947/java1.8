/* ***********************************************************************************
 * Copyright (c) 2005, 2008 Misys International Banking Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * ********************************************************************************
 * $Id: AlmondeConstants.java,v.1.0,May 20, 2009 2:29:45 PM ayerla
 *
 */
package com.misys.ub.common.almonde;

import java.io.File;

import com.trapedza.bankfusion.bo.refimpl.IBOCustomer;
import com.trapedza.bankfusion.bo.refimpl.IBOProduct;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_ALD_CUSTOMERGROUPMAP;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_ALD_GROUPS;

/**
 * @author ayerla
 * @date May 20, 2009
 * @project Universal Banking
 * @Description: List Of Almonde Constants.
 */
public final class AlmondeConstants {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}


	/**
	 * Instantiates a new almonde constants.
	 */
	public AlmondeConstants() {
	}

	/** <code>CONTRACT_MM</code> = "ContractMM". */
	public static final String CONTRACT_MM = "ContractMM";

	/** <code>CONTRACT_NI</code> = "ContractNI". */
	public static final String CONTRACT_NI = "ContractNI";

	/** <code>CONTRACT_LENDING</code> = "ContractLending". */
	public static final String CONTRACT_LENDING = "ContractLending";

	/** <code>CONTRACT_FX</code> = "ContractFX". */
	public static final String CONTRACT_FX = "ContractFX";

	/** <code>CONTRACT_OTHERS</code> = "ContractOthers". */
	public static final String CONTRACT_OTHERS = "ContractOthers";

	/** <code>ALM_EXTRACT</code> = "ALMExtract". */
	public static final String ALM_EXTRACT = "ALMExtract";

	/** <code>Others_EXTRACTS</code> = "StaticAndCurrentDataOthers". */
	public static final String Others_EXTRACTS = "StaticAndCurrentDataOthers";

	/** <code>OTHER_PROD_LIST</code> = "ContractOthers_PROD". */
	public static final String OTHER_PROD_LIST = "ContractOthers_PROD";

	/** <code>BUSINESSDATE</code> = "BusinessDate". */
	public static final String BUSINESSDATE = "BusinessDate";

	/** <code>EXTRACTTION_FILE_LOCATION</code> = "FileLocation". */
	public static final String EXTRACTTION_FILE_LOCATION = "FileLocation";

	/** <code>MODULE_NAME</code> = "Almonde". */
	public static final String MODULE_NAME = "Almonde";

	/** <code>EXTRACTION_PATH</code> = "ExtractionPath". */
	public static final String EXTRACTION_PATH = "ExtractionPath";

	/** <code>ALM</code> = "ALM". */
	public static final String ALM = "001";

	/** <code>BASEL2</code> = "BASEL II". */
	public static final String BASEL2 = "002";

	/** <code>EXTRACTIONFOR</code> = "ExtractionFor". */
	public static final String EXTRACTIONFOR = "ExtractionFor";

	/** Param key for Cash Flow Extract**/
	public static final String CASHFLOW_EXTRACT = "CashFlowExtract";

	/** Param key for Contract Assets Ratings**/
	public static final String CONTRACT_ASSET_RATINGS = "ContractAssetRatings";

	/** Param key for Contract FX Deal Ratings**/
	public static final String CONTRACT_FXDEAL_RATINGS = "ContractFXDealRatings";

	/** Param key for Contract Party Reference**/
	public static final String CONTRACT_PARTY_XREF = "ContractPartyXRef";

	/** Param key for Contract Mitigant Data**/
	public static final String CONTRACT_MITIGANT = "MitigantExtractJob";

	/** Param key for Counter Party Ratings**/
	public static final String COUNTERPARTY_RATINGS = "CounterPartyRatings";

	
	/** Param key for Country Ratings**/
	public static final String COUNTRY_RATINGS = "CountryRatings";

	/** Param key for Mitigant Extract**/
	public static final String MITIGANT_EXTRACTJOB = "MitigantExtractJob";

	/** Param key for Mitigant Ratings**/
	public static final String MITIGANT_RATINGS = "MitigantRatings";

	/** Param key for Party Data Extract**/
	public static final String PARTYDATA_EXTRACT = "PartyDataExtract";

	/** Param key for Activity Sector**/
	public static final String ACTIVITYSECTOR_EXTRACT = "ActivitySectorExtract";

	/** Param key for Exchange Rates**/
	public static final String EXCHANGERATES_EXTRACT = "ExchangeRatesExtract";

	/** Param key for External Ratings**/
	public static final String EXTERNALRATINGS_EXTRACT = "ExternalRatingsExtract";

	/** Param key for Interest Base Code**/
	public static final String INTERESTBASECODE_EXTRACT = "InterestBaseCodeExtract";

	/** Param key for Internal Ratings**/
	public static final String INTERNALRATINGS_EXTRACT = "InternalRatingsExtract";

	/** Param key for Product Type**/
	public static final String PRODUCTTYPE_EXTRACT = "ProductTypeExtract";

	/** Param key for Rating Agency**/
	public static final String RATINGAGENCY_EXTRACT = "RatingAgencyExtract";

	/** Param key for Source System**/
	public static final String SOURCESYSTEM_EXTRACT = "SourceSystemExtract";

	/** Param key for General Provision **/
	public static final String GENERALPROVISION_EXTRACT = "GeneralProvisionExtract";

	/** Param key for Specific Provision **/
	public static final String SPECIFICPROVISION_EXTRACT = "SpecificProvisionExtract";

	/** Param key for Contract MM and NI**/
	public static final String CONTRACT_MM_NI = "ContractMMNI";
	

	
	/** Param key for Contract Internal Products**/
	public static final String INTERNAL_PROD = "Internal_Products";
	
	/** Delimeter for Products**/
	public static final String PROD_DELIMETER = "','";	
	/**
	 * <code>PROPERTIES_FILE_LOCATION</code> =
	 * "conf"+File.separator+"business"+File.separator+"ALD"+File
	 * .separator+"AlmondeExtract.properties".
	 */
	public static final String PROPERTIES_FILE_LOCATION = "conf"
		+ File.separator + "business" + File.separator + "ALD"
		+ File.separator + "AlmondeExtract.properties";

	/** <code>SINGLEQUOTE</code> = "'". */
	public static final String SINGLEQUOTE = "'";

	/** <code>ALD_MASTER_PROP_LOC</code> = "ALDMasterPropLoc". */
	public static final String ALD_MASTER_PROP_LOC = "ALDMasterPropLoc";
	
	/** Param key for BOID **/
	public static final String  BOID                           = "BOID";
	
	/**Param key for  UB_ALD_CUSTOMERGROUPMAP_UBREFERENCEID    */
    public static final String CUSTOMERGROUPMAP_REFERENCEID    = "UB_ALD_CUSTOMERGROUPMAP_UBREFERENCEID";
   
    /**Param key for   UB_ALD_GROUPS_UBGROUPDESC   */
    public static final String GROUPS_DESC                     = "UB_ALD_GROUPS_UBGROUPDESC";
    
    /**Param key for SHORTNAME */
    public static final String CUSTOMERNAME                    = "SHORTNAME";
    
    /**Param key for UBGROUPDESC */
    public static final String DESC                            = "UBGROUPDESC";
     
    /**Param key for      */
    public static final String ISCUSTOMER                      = "UB_ALD_CUSTOMERGROUPMAP_UBISCUSTOMER";
    
    /**Param key for UB_ALD_CUSTOMERGROUPMAP_UBISCUSTOMER */
    public static final String ORDERBY                         = " ORDER BY ";
    
    /**Param key for  CustomerCode */
    public static final String CUSTOMERCODE                    = "CustomerCode";
    
    /**Param key for EnableKYCCheck */
    public static final String KYCCHECK                        = "EnableKYCCheck";
    
    /**Param key for UB_CNF_ReadKYCStatus_SRV  */
    public static final String UB_CNF_ReadKYCStatus_SRV        = "UB_CNF_ReadKYCStatus_SRV";
    
    /**Param key for continue */
    public static final String CONTINUE                        = "continue";
    
    /**Param key for  Type */
    public static final String TYPE                            = "Type";
    
    /**Param key for  Customer    */
    public static final String CUSTOMER                        = "Customer";
    
    /**Param key for      */
    public static final String GROUP                           = "Group";
    
    /**Param key for   Group   */
    public static final String DUPLICATE                       = "Duplicate ";
    
    /**Param key for   Comments   */
    public static final String COMMENTS                        = "Comments";
    
    /**Where Clause Query      */
    public static final String WHEREFORFINDINGPARENT           = "where "+IBOUB_ALD_CUSTOMERGROUPMAP.UBREFERENCEID + " = ? ";
    
    /**Param key for  contains    */
    public static final String CONTAINS                        = " contains ";
    
    /**Param key for  Black Listed    */
    public static final String BLACKLISTED                     = "Black Listed";
    
    /**Param key for  Query    */
    public static final String reqCust                         = " or " + IBOCustomer.CUSTOMERCODE + " =? ";
    
    /**Param key for  Query    */
    public static final String reqGrp                          = " or " + IBOUB_ALD_GROUPS.UBGROUPIDPK  + " =? ";
    
    /**Param key for  WHERE    */
    public static final String WHERE                           = "where ";
    
    /**Param key for  QUERY    */
    public static final String MORETHANONEPRD = " and " + IBOProduct.PRODUCTID + " != ? ";

    /**Param key for  UBEXTRACTDATATYPEATTR    */
    public static final String UBEXTRACTDATATYPEATTR = "UBEXTRACTDATATYPEATTR";
    
    /**Param key for  UBEXTRACTDATATYPE    */
    public static final String UBEXTRACTDATATYPE = "UBEXTRACTDATATYPE";
    
    /**Param key for  AbstractProducts    */
    public static final String ABSTRACTPRDS = "AbstractProducts";
   

}
