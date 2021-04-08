package src.manish.practice;

import java.beans.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import com.sun.corba.se.pept.transport.Connection;

public class Test {

	public static void main(String[] args) {
		ArrayList<Integer> a = new ArrayList<Integer>();
		a.add(0);
		a.add(1);
		a.add(1);
		a.add(0);
		a.add(0);
		a.add(1);
		a.add(0);
		
		ArrayList<Integer> result = zerosAndOnes(a);
		System.out.println(result.get(0));
		System.out.println(result.get(1));
		
		
		
		
		HashMap<String, String> hashmap = new HashMap<String, String>();
	    hashmap.put("Key1", "Value1");
	    hashmap.put("Key2", "Value2");
	    hashmap.put("Key3", "Value3");
	    hashmap.put("Key4", "Value4");
	    hashmap.put("Key5", "Value5");
	    hashmap.put("Key6", "Value6");
	    
	    
	    for (String key : hashmap.keySet()) {
	    	System.out.println(hashmap.get(key));	
		}

	    

	}
	
	public static ArrayList zerosAndOnes(ArrayList<Integer> a) {
		ArrayList<ArrayList> both_0_1 = new ArrayList<ArrayList>();
		ArrayList<Integer> _0 = new ArrayList<Integer>();
		ArrayList<Integer> _1 = new ArrayList<Integer>();
		
		for (int i=0; i< a.size();i++) {
			if(a.get(i)==1) {
				_1.add(a.get(i));
			}
			else {
				_0.add(a.get(i));
			}
		}
		both_0_1.add(_0);
		both_0_1.add(_1);
		
		
		return 	both_0_1;}

}
