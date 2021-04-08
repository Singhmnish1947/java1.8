 /**
 * * Copyright (c) 2008 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 
 */
 package com.misys.ub.fircosoft;
 /**
  * @author Gaurav.Aggarwal
  *
  */
import java.util.ArrayList;

import com.misys.ub.swift.UB_203Message_Details;
import com.trapedza.bankfusion.core.CommonConstants;

public class HitDetails {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	String RequestMessage = CommonConstants.EMPTY_STRING;
	String NoOfHits = CommonConstants.EMPTY_STRING;
	private ArrayList HitDetail = new ArrayList();
	public ArrayList getHitDetail() {
		return HitDetail;
	}
	public void setHitDetail(ArrayList hitDetail) {
		HitDetail = hitDetail;
	}
	public String getRequestMessage() {
		return RequestMessage;
	}
	public void setRequestMessage(String requestMessage) {
		RequestMessage = requestMessage;
	}
	public void addHitDetail(HitDetail hitdetail) {
		 HitDetail.add(hitdetail);
	   }
	public String getNoOfHits() {
		return NoOfHits;
	}
	public void setNoOfHits(String noOfHits) {
		NoOfHits = noOfHits;
	}
	 
}
