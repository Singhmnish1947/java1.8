package com.trapedza.bankfusion.fatoms;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.trapedza.bankfusion.fatoms.UB_SWT_SplitSettlementRateSrc;

public class TEST_UB_SWT_SplitSettlementRateSrc {
	private UB_SWT_SplitSettlementRateSrc ubSplitSettlementRateSrc;

	private String inputWithForwardSlash = "ABC12/1212/ABCD";
	private String inputWithoutForwardSlash = "ABC12";
	private String expectedOutputStringWithForwardSlash1 = "ABC12";
	private String expectedOutputStringWithForwardSlash2 = "/1212";
	private String expectedOutputStringWithForwardSlash3 = "/ABCD";
	private String expectedOutputStringWithoutForwardSlash1 = "ABC12";
	private String expectedOutputStringWithoutForwardSlash2 = "";
	private String expectedOutputStringWithoutForwardSlash3 = "";
	
	@Before
	public void setUp() throws Exception {
		ubSplitSettlementRateSrc = new UB_SWT_SplitSettlementRateSrc(null);
	}
	
	@Test
	public void testSplitWithoutForwardSlash(){
		ubSplitSettlementRateSrc.setF_IN_input(inputWithoutForwardSlash);
		ubSplitSettlementRateSrc.process(null);
		assertTrue(ubSplitSettlementRateSrc.getF_OUT_string_1().equalsIgnoreCase(expectedOutputStringWithoutForwardSlash1));
		assertTrue(ubSplitSettlementRateSrc.getF_OUT_string_2().equalsIgnoreCase(expectedOutputStringWithoutForwardSlash2));
		assertTrue(ubSplitSettlementRateSrc.getF_OUT_string_3().equalsIgnoreCase(expectedOutputStringWithoutForwardSlash3));
	}
		
	@Test
	public void testSplitWithForwardSlash(){
		ubSplitSettlementRateSrc.setF_IN_input(inputWithForwardSlash);
		ubSplitSettlementRateSrc.process(null);
		assertTrue(ubSplitSettlementRateSrc.getF_OUT_string_1().equalsIgnoreCase(expectedOutputStringWithForwardSlash1));
		assertTrue(ubSplitSettlementRateSrc.getF_OUT_string_2().equalsIgnoreCase(expectedOutputStringWithForwardSlash2));
		assertTrue(ubSplitSettlementRateSrc.getF_OUT_string_3().equalsIgnoreCase(expectedOutputStringWithForwardSlash3));
	}
	
}
