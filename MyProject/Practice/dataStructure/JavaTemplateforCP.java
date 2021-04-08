package dataStructure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

public class JavaTemplateforCP {

	public static void main(String[] args) {
		FastScanner fs = new FastScanner();
		int T = fs.nextInt();
		
		for (int tt = 0; tt < T; tt++) {
			HashMap<Integer ,Integer> hm = new HashMap<Integer ,Integer> ();
			int n = 0;
			int k1 = 0;
			int k2 = 0;
			int w=0;
			int b =0;
			
			
			
			for (int i = 1; i <= 3; i++) {
				hm.put(i, fs.nextInt());
			}
			
			n = hm.get(1);
			k1 = hm.get(2);
			k2 = hm.get(3);
			
			
			for (int i = 0; i < 2; i++) {
				hm.put(i, fs.nextInt());
			}
			
			w = hm.get(1);
			b = hm.get(2);
			
			int noOfWhite = k1+k2;
			int noOfBlack = n - (k1+k2);
			
			if (noOfWhite>w && noOfBlack > b) {

			System.out.println("YES");
			}else {
			System.out.println("NO");
			}
		}
		
	}
	
	
	public static char[] rev(String s)
    {
        String input = "GeeksforGeeks";
 
        // getBytes() method to convert string
        // into bytes[].
        byte[] strAsByteArray = input.getBytes();
 
        byte[] result = new byte[strAsByteArray.length];
 
        // Store result in reverse order into the
        // result byte[]
        for (int i = 0; i < strAsByteArray.length; i++)
            result[i] = strAsByteArray[strAsByteArray.length - i - 1];
 
        String revString = new String(result);
     return revString.toCharArray();
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