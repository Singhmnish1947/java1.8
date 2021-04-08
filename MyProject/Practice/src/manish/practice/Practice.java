package src.manish.practice;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class Practice {

	public static void rearrange(Boolean small) {
		/*
		 * String messageTypeIdentifier = ""; String isoMessageTypeIdentifier = "1100";
		 * String processingCode = "18"; String DMS = "SMS";
		 * 
		 * givenUsingPlainJava_whenGeneratingRandomStringUnbounded_thenCorrect(); if
		 * (small == false) { if ("1100".equals(isoMessageTypeIdentifier) &&
		 * !("18".equals(processingCode))) { if (DMS.equals("DMS")) {
		 * messageTypeIdentifier = "1100"; } else { messageTypeIdentifier = "1200"; } }
		 * else { messageTypeIdentifier = isoMessageTypeIdentifier; } } else {
		 * messageTypeIdentifier = ((("1100".equals(isoMessageTypeIdentifier) &&
		 * "18".equals(processingCode)) ? (DMS.equals("DMS") ? "1100" : "1200") :
		 * isoMessageTypeIdentifier)); } return messageTypeIdentifier;
		 */
//		Set a;
//
//
//			a = new HashSet();
//			
//
//		a.add("ABC");
//		a.add("DE");
//		a.add("f");
//		a.add("4");
//		a.add("5");
//		a.add("true");
		
//		Object[] b = a.toArray();
//		Arrays.sort(b);
//		
		
		String a = "A,B,C";
		String b = a.replace(",", "");

		System.out.println(b);

	}

	public static void givenUsingPlainJava_whenGeneratingRandomStringUnbounded_thenCorrect() {
		byte[] array = new byte[7]; // length is bounded by 7
		new Random().nextBytes(array);
		String generatedString = new String(array, Charset.forName("UTF-8"));

		System.out.println(generatedString);
	}

	public static void main(String[] args) {
		rearrange(true);
	}

}
