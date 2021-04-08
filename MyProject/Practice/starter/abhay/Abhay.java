package starter.abhay;

import java.util.ArrayList;

public class Abhay {

	
	public static void main(String args[]) {
		int count = magicalString("pops");
	}
	public static int magicalString(String str) {


		int count = 0;
		ArrayList stringArr = new ArrayList();
		
		
		for (int i = 0; i < str.length(); i++) {
			for (int j = i + 1; j <= str.length(); j++) {
				stringArr.add(str.substring(i, j));// logic
			}
		}
		
		
		System.out.println(stringArr);
		
		for(int i=0; i< stringArr.size(); i++) {
			
			String check = (String)stringArr.get(i);
			String revCheck = reverse(check);
			
			
			
			if(str.contains(revCheck) && (revCheck.equals(check))){
				count++;	
			}
			
		}return count;
		
	}
	public static String reverse(String input) {

     byte[] strAsByteArray = input.getBytes(); 
     
     byte[] result = new byte[strAsByteArray.length]; 
     
     for (int i = 0; i < strAsByteArray.length; i++) 
         result[i] = strAsByteArray[strAsByteArray.length - i - 1]; 

     return new String(result); 
}}
