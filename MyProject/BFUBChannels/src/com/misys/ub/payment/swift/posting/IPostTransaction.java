package com.misys.ub.payment.swift.posting;

import bf.com.misys.cbs.types.header.RsHeader;

/**
 * @author machamma.devaiah
 *
 */
public interface IPostTransaction {
	/**
	 * @param postingdto
	 * @return
	 */
	public RsHeader postTxn(PostingDto postingdto);
}
