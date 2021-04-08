package com.misys.test;

import java.io.IOException;
import java.math.BigDecimal;

public abstract class Test2 {

	public static void main(String[] args) throws IOException {

		String s = "ababc";
		for(int i=0;i<s.length();i++)
		{
			if(i<(s.length()-3))
			{
			if(s.charAt(i)==s.charAt(i+1))  
			{
				continue;
			}
			else {
				
				if(s.charAt(i+1)==s.charAt(i+2)) 
				{
					continue;
				}
				else {
					System.out.println(s.charAt(i+1));
					break;
				}
			}
			}
			else
			{
				System.out.println(s.charAt(s.length()-1));
				break;
			}
		}

}
}