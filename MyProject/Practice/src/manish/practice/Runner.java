package src.manish.practice;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import edu.emory.mathcs.backport.java.util.Collections;

public class Runner {

	public static void main(String[] args) {
		
		
		Scanner in = new Scanner(System.in);
		
		String[] y = new String[5];
		for(int i =0; i<5;i++) {
		y[i]= in.nextLine();
		System.out.println(y[i]);
		}
		
		ArrayList a = new ArrayList();
		Object[] b = new String[10];
		a.add("3");
		a.add("1");
		a.add("a");
		a.add("2");
		a.add("f");
		a.add("a");

		b = a.toArray();
		
		
		
		
		StringBuilder  e = new StringBuilder("abccel");
		StringBuffer  z = new StringBuffer("abccel");
		e.reverse();
		String l = "1a2bcdcm";
		
		String m = l.replaceAll("c", "b");
		char[] x = l.toCharArray();
		Set r = new LinkedHashSet();
		r.addAll(a);
//		
//		for (int i =0; i<x.length;i++) {
//			r.add(x[i]);
//		}
		Arrays.sort(b);

		System.out.println(b);
		
		
		
	}
}