package com.misys.test;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LogTest {

	public static void main(String[] args) {
		try {
			printRequestResponse("Hello Manish");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void printRequestResponse(String data) throws IOException {
		if(null == Paths.get("D:/TEXT/samplefile.txt")) {
		Files.createDirectories(Paths.get("D:/TEXT/"));
		}
		Path path = Paths.get("D:/TEXT/samplefile.txt");
		Files.write(path, data.getBytes());
		
		
	}
}
