package src.manish.practice;

import java.util.ArrayList;

public class Substring {

	public static void main(String[] args) {

		String s = "ababcde";
		int l = 3;
		System.out.println(substringUnique(s, l));
	}

	public static ArrayList substringUnique(String s, int l) {

		String out = "";
		ArrayList arr = new ArrayList();

		int j = 0;
		while (j < s.length()) {
			char check = ' ';
			boolean repeat = false;
			for (int k = 0; k < l; k++) {
				check = s.charAt(j);
				if (check == s.charAt(k + 1)) {
					repeat = true;
				}
			}
			for (int i = j; i < l; i++) {

				if (repeat == false) {
					out = out + String.valueOf(s.charAt(i));
				} else {
					continue;
				}
			}
			if (out.length() == l) {
				arr.add(out);
			}
			j++;

		}

		return arr;
	}
}
