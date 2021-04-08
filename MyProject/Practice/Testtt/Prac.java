package Testtt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;

public class Prac {

	public static void main(String[] args) {
		
		

		Hashtable a = new Hashtable();
		a.put(1, 2);
		a.put(1, 3);
		a.put(2, 4);
		a.put(2, 5);
		a.put(4, 7);
		
		System.out.println("Manish");

		ArrayList b = new ArrayList();
		b.add(a.get(1));

		while (b.size() < a.size()) {
			for (int i = 0; i < a.size(); i++) {
				
				int n = (int) a.get(b.get(i));
				b.add(n);
			}

			System.out.println(b);
		}

	}
}
