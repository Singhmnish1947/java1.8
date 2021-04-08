package com.misys.test;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPPumper2 {

	public static void main(String[] args) throws UnknownHostException,
			IOException {
		Socket clientSocket = new Socket("localhost", 5150);
		DataOutputStream outToServer = new DataOutputStream(
				clientSocket.getOutputStream());		
//		 working Withdrawal
		String Header= asciiToHex("02791100");       
        String BitMap ="BCF6400108A1E4000000000014000000";
        String fieldData= asciiToHex("0100000000000112200000000112200000000112206100001361000013345678200731160346221220073160111111000589395POS02007343000000001ATM BANISTMO>ATM BANISTMO / ATM /PanamPA0350020037000260049001031010RE2007310884097897801800101200000000100011110005893941301110PTY00100");
        String PrimaryAccNo = asciiToHex("150202010PTY00100");

//		//POS Working
//		String Header= asciiToHex("02811100");       
//        String BitMap ="BCF6400108A1E4000000000014000000";
//        String fieldData= asciiToHex("0000000000000112200000000112200000000112206100001361000013345678200731160346221220073160111111000589395POS02007351000000009ATM BANISTMO>ATM BANISTMO / ATM /PanamPA0350020037740260049001031010RE200731089789788400180010120000000000001111000589394150202010PTY00100");
//        String PrimaryAccNo = asciiToHex("150202010PTY00100");

        String requestMsg1100 = Header + BitMap + fieldData;
		
		//without functionCode Field
		
/*		String Header= asciiToHex("02981420");
        String BitMap ="FCF6400108A1E4000000000014000000";
        String fieldData= asciiToHex("1610121000000409002010000000000052200000000003300000000003300000001300000013345678200626155646221220062560111111000589395POS010TRS12500000009ATM BANISTMO>ATM BANISTMO / ATM /PanamPA034002003774026003999031010REF00000069788408400180010120000000010001111000589394150202010PTY00100");
		
        String requestMsg1100 = Header + BitMap + fieldData;
        
      */  
        
        
        
        byte[] requestMessage1100Array = hexStringToByteArray(requestMsg1100);

		outToServer.write(requestMessage1100Array);

		InputStream stream = clientSocket.getInputStream();

		byte[] data = new byte[600];

		int count = stream.read(data);

		System.out.println(count);
		System.out.println(byteArrayToHex(data));
		clientSocket.close();
	}

	public static String byteArrayToHex(byte[] A) {
		StringBuilder sb = new StringBuilder(A.length * 2);
		for (byte B : A)
			sb.append(String.format("%02X", B));
		return sb.toString();
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[(i / 2)] = ((byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
					.digit(s.charAt(i + 1), 16)));
		}
		return data;

	}

	public static String asciiToHex(String ascii) {
		char[] ch = ascii.toCharArray();
		StringBuilder hex = new StringBuilder();

		for (char c : ch) {
			int i = (int) c;
			hex.append(Integer.toHexString(i).toUpperCase());
		}
		String hexConverted = String.valueOf(hex);
		return hexConverted;
	}
}
