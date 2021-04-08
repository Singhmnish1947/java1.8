package com.misys.test;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPPumper {

	public static void main(String[] args) throws UnknownHostException,
			IOException {
		Socket clientSocket = new Socket("10.220.129.105", 8133);
		DataOutputStream outToServer = new DataOutputStream(
				clientSocket.getOutputStream());

		// Balance Enquiry with all the fields
		String Header= asciiToHex("00331804");
		String BitMap ="0030010000000000";
		String fieldData= asciiToHex("123457200526144832831");
		 
//		String Header= asciiToHex("02811100");       
//        String BitMap ="BCF6400108A1E4000000000014000000";
//        String fieldData= asciiToHex("0100000000000112200000000112200000000112206100001361000013345678201126160346221220112660111111000589395POS02007346500000009ATM BANISTMO>ATM BANISTMO / ATM /PanamPA0350020037000260049001031010RE20073215840978978018001012000000000000111100058939415029211000000100");
//        String PrimaryAccNo = asciiToHex("150202010PTY00100");

//		
/*		 String Header= asciiToHex("02311100");
		 String BitMap ="B49000010881A4000000000014000000";
		 String fieldData=asciiToHex("310000000000013300000000013300000000002005221448321111000589394BLQM00002204000000010520010011002003702026007BancNet0250011031010REF00000038408400180010120000000011001111000589394150202010PTY00100");
*/	
	/*	String Header= asciiToHex("02631100");
		String BitMap ="E03640010CA1A4000000000014000000";
		String fieldData1= asciiToHex("161012100000040900310000345678200605144832221220060560111111000589394");
		
		//Please Update retrivalReferenceNo after every successful transaction as this field is a unique Id for each transaction.	
		String retrivalReferenceNo = asciiToHex("BAL000200713");
		
		String fieldData2 = asciiToHex("12345600000001ATM BANISTMO>ATM BANISTMO / ATM /PanamPA04800100110020037020260039990250011031010REF00000049789780180010120000000000001111000589394150202010PTY00100");
		String requestMsg1100 = Header + BitMap + fieldData1 +retrivalReferenceNo+fieldData2;
		*/ 
			/*String Header= asciiToHex("00331804");
			String BitMap ="0030010000000000";
			String fieldData= asciiToHex("123456200602144832831"); 
			String requestMsg1100 = Header + BitMap + fieldData;
*/		
		
		
		//with functionCode Field
/*		String Header= asciiToHex("03011100");
        String BitMap ="FCF6410108A1E4000000000014000000";
        String fieldData= asciiToHex("1610121000000409002010000000000052200000000003300000000003366200001362000013345678200626155646221220062560110001111000589395POS010TRS20000000009ATM BANISTMO>ATM BANISTMO / ATM /PanamPA034002003774026003999031010REF00000069789789780180010120000000010001111000589394150202010PTY00100");
		*/
		
/*		String Header = asciiToHex("03181100");
		String BitMap = "FCF640010CA1E4000000000014000000";
		String fieldData = asciiToHex("1610121000000409000110000000000133000000000133000000000133000000000000000000345678200728144832221220052160111111000589394CWD00000272012345600000001ATM BANISTMO>ATM BANISTMO / ATM /PanamPA04800100110020037000260039990250011031010REF00000048408408400180010120000000000001111000589394150202010PTY00100");

	*/		
		// working Withdrawal
//		String Header= asciiToHex("02811100");       
//        String BitMap ="BCF6400108A1E4000000000014000000";
//        String fieldData= asciiToHex("0100000000000112200000000112200000000112206100001361000013345678200731160346221220073160111111000589395POS02007326200000001ATM BANISTMO>ATM BANISTMO / ATM /PanamPA0350020037000260049001031010RE200731089789789780180010120000000010001111000589394150202010PTY00100");
//        String PrimaryAccNo = asciiToHex("150202010PTY00100");

		//POS Working
//		String Header= asciiToHex("02811100");       
//        String BitMap ="BCF6400108A1E4000000000014000000";
//        String fieldData= asciiToHex("0000000000000112200000000112200000000112206100001361000013345678200731160346221220073160111111000589395POS02007321700000009ATM BANISTMO>ATM BANISTMO / ATM /PanamPA0350020037740260049001031010RE200731089789789780180010120000000010001111000589394150202010PTY00100");
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
