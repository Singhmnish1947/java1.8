package com.finastra.iso8583.atm;

import java.io.BufferedReader;
import java.io.InputStreamReader;

class TCPServer{
public static void main(String args[])throws Exception{

 InputStreamReader r=new InputStreamReader(System.in);
 BufferedReader br=new BufferedReader(r);

 String name="";

  while(!name.equals("stop")){
   System.out.print("Enter data: ");
   name=br.readLine();
   System.out.println("Jerry is: "+name);
  }

 br.close();
 r.close();
 }
}
