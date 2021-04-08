/**
 * * Copyright (c) 2016 Misys International Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys International Financial Systems Ltd.
 * Use is subject to license terms.
 */
package com.trapedza.bankfusion.fatoms;

public class LIQDDAPostingResponse {

	LIQPostingResponse[] liqPostingResponse;
	String fileName;
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public LIQPostingResponse[] getLiqPostingResponse() {
		return liqPostingResponse;
	}
	
	public void setLiqPostingResponse(LIQPostingResponse[] liqPostingResponse) {
		this.liqPostingResponse = liqPostingResponse;
	}
}
