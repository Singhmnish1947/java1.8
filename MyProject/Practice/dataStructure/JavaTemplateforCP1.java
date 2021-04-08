package dataStructure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;

public class JavaTemplateforCP1 {

	public static void main(String[] args) {
		FastScanner fs = new FastScanner();
		int T = fs.nextInt();

		for (int tt = 0; tt < T; tt++) {

			HashMap<Integer, BigInteger> hm = new HashMap<Integer, BigInteger>();
			BigInteger noOfRow ;
			BigInteger noOfColumn ;
			BigInteger value;
			BigInteger newRow = new BigInteger("0");
			BigInteger newColumn = new BigInteger("0");
			BigInteger output =new BigInteger("0");
			
			Scanner sc = new Scanner(System.in);
			
			noOfRow = sc.nextBigInteger();
			noOfColumn = sc.nextBigInteger();
			value = sc.nextBigInteger();
			
			
			if(value.remainder(noOfRow) == new BigInteger("0")){
		    newRow = noOfRow;
			newColumn =  value.divide(noOfRow);
			output = (newRow.subtract(new BigInteger("1"))).multiply(noOfColumn).add(newColumn);
			}else{
			newRow = (value.remainder(noOfRow)).subtract(new BigInteger(String.valueOf(1)));
			newColumn =  value.divide(noOfRow);
			output = newRow.multiply(noOfColumn).add(newColumn).add(new BigInteger(String.valueOf(1)));
			}
			
			
//			newRow = (value.remainder(noOfRow)).subtract(new BigInteger(String.valueOf(1)));
//			newColumn =  value.divide(noOfRow);
			output = newRow.multiply(noOfColumn).add(newColumn).add(new BigInteger(String.valueOf(1)));

			System.out.println(output);
		}

	}

	static class FastScanner {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer("");

		public String next() {
			while (!st.hasMoreElements())
				try {
					st = new StringTokenizer(br.readLine());
				} catch (IOException e) {
					e.printStackTrace();
				}
			return st.nextToken();
		}

		int[] readArray(int n) {
			int[] a = new int[n];
			for (int i = 0; i < n; i++)
				a[i] = nextInt();
			return a;
		}

		int nextInt() {
			return Integer.parseInt(next());
		}
	}

}