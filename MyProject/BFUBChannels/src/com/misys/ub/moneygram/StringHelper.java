/* ***********************************************************************************
 * Copyright (c) 2007 Misys Financial Systems Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of Misys Financial Systems Ltd.
 * Use is subject to license terms.
 *
 * **********************************************************************************
 *
 * $Id: StringHelper.java,v 1.5 2008/08/12 20:15:25 vivekr Exp $
 *
 */

package com.misys.ub.moneygram;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

/** StringPadder provides left pad and right pad functionality for Strings
 */
public class StringHelper {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private StringBuffer strb;
	private StringCharacterIterator sci;

	/** method to left pad a string with a given string to a given size. This
	 *  method will repeat the padder string as many times as is necessary until
	 *  the exact specified size is reached. If the specified size is less than the size of
	 *  the original string then the original string is returned unchanged.
	 *  Example1 - original string "cat", padder string "white", size 8 gives "whitecat".
	 *  Example2 - original string "cat", padder string "white", size 15 gives "whitewhitewhcat".
	 *  Example3 - original string "cat", padder string "white", size 2 gives "cat".
	 *  @return String the newly padded string
	 *  @param  stringToPad The original string
	 *  @param  padder The string to pad onto the original string
	 *  @param  size The required size of the new string
	 */
	public String leftPad(String stringToPad, String padder, int size) {
		if (padder.length() == 0) {
			return stringToPad;
		}
		if (stringToPad.length() > size) {
			return stringToPad.substring(0, size);
		}
		strb = new StringBuffer(size);
		sci = new StringCharacterIterator(padder);

		while (strb.length() < (size - stringToPad.length())) {
			for (char ch = sci.first(); ch != CharacterIterator.DONE; ch = sci.next()) {
				if (strb.length() < size - stringToPad.length()) {
					strb.insert(strb.length(), String.valueOf(ch));
				}
			}
		}
		return strb.append(stringToPad).toString();
	}

	/** method to right pad a string with a given string to a given size. This
	 *  method will repeat the padder string as many times as is necessary until
	 *  the exact specified size is reached. If the specified size is less than the size of
	 *  the original string then the original string is returned unchanged.
	 *  Example1 - original string "cat", padder string "white", size 8 gives "catwhite".
	 *  Example2 - original string "cat", padder string "white", size 15 gives "catwhitewhitewh".
	 *  Example3 - original string "cat", padder string "white", size 2 gives "cat".
	 *  @return String the newly padded string
	 *  @param  stringToPad The original string
	 *  @param  padder The string to pad onto the original string
	 *  @param  size The required size of the new string
	 */
	public String rightPad(String stringToPad, String padder, int size) {
		if (padder.length() == 0) {
			return stringToPad;
		}
		if (stringToPad.length() > size) {
			return stringToPad.substring(0, size);
		}

		strb = new StringBuffer(stringToPad);
		sci = new StringCharacterIterator(padder);

		while (strb.length() < size) {
			for (char ch = sci.first(); ch != CharacterIterator.DONE; ch = sci.next()) {
				if (strb.length() < size) {
					strb.append(String.valueOf(ch));
				}
			}
		}
		return strb.toString();
	}
}
