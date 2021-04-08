package starter.abhay;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Converter {

	public static void main(String[] args) throws UnknownHostException, IOException {
		Socket clientSocket = new Socket("localhost", 5150);
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

		String Header = asciiToHex("03341100");
		String BitMap = "BCF6400108E1E4000000000014000000";

		String PrimaryAccNo_02 = asciiToHex("");
		//String PrimaryAccNo_02 = asciiToHex("191234512345123451234");
		
		String ProcessingCode_03 = asciiToHex("001000");
		String TransactionAmount_04 = asciiToHex("000000000120");
		String amountAccount_05 = asciiToHex("000000000120");
		String CardHolderBillingAmt_06 = asciiToHex("000000000120");
		String conversionRateCardHolderBilling_09 = asciiToHex("00000013");
		String conversionRateAccount_10 = asciiToHex("00000013");
		String STAN_11 = asciiToHex("345678");
		String localTransactionDateTime_12 = asciiToHex("200513144832");
		String dateExpirationForCard_14 = asciiToHex("2212");
		String settlementDate_15 = asciiToHex("200513");
		String merchantType_18 = asciiToHex("6011");
		String acquiringInstitutionIdentificationCode_32 = asciiToHex("1111000589395");
		String ReferenceNo_37 = asciiToHex("POS000TEST01");
		String cardAcceptorTerminalIdentification_41 = asciiToHex("00000009");
		String CardAcceptorId_42 = asciiToHex("11000589395    ");
		String cardAcceptorNameandLocation_43 = asciiToHex("ATM BANISTMO>ATM BANISTMO / ATM /PanamPA");
		String additionalData1_48 = asciiToHex("0520010011002003774026007BancNet0250011031010REF0000005");
		String currencyCodeTransaction_49 = asciiToHex("840");
		String currencyCodeAccount_50 = asciiToHex("840");
		String transactionCurrencyCode_51 = asciiToHex("840");
		String additionalAmounts_54 = asciiToHex("018001012000000001000");
		String issuerInstitutionIdentifier_100 = asciiToHex("1111000589395");
		String AccNo_102 = asciiToHex("150201010PTY00100");

		String requestMsg1100 = Header + BitMap + PrimaryAccNo_02 + ProcessingCode_03 + TransactionAmount_04
				+ amountAccount_05 + CardHolderBillingAmt_06 + conversionRateCardHolderBilling_09
				+ conversionRateAccount_10 + STAN_11 + localTransactionDateTime_12 + dateExpirationForCard_14
				+ settlementDate_15 + merchantType_18 + acquiringInstitutionIdentificationCode_32 + ReferenceNo_37
				+ cardAcceptorTerminalIdentification_41 + CardAcceptorId_42 + cardAcceptorNameandLocation_43
				+ additionalData1_48 + currencyCodeTransaction_49 + currencyCodeAccount_50 + transactionCurrencyCode_51
				+ additionalAmounts_54 + issuerInstitutionIdentifier_100 + AccNo_102;

		byte[] requestMessage1100Array = hexStringToByteArray(requestMsg1100);

		outToServer.write(requestMessage1100Array);

		InputStream stream = clientSocket.getInputStream();

		byte[] data = new byte[350];

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
			data[(i / 2)] = ((byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16)));
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