package src.manish.practice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class NewPractice {

	public static void main(String[] args) throws ClassNotFoundException, SQLException {

		Class.forName("com.ibm.db2.jcc.DB2Driver");
		Connection con = DriverManager.getConnection("jdbc:db2://localhost:50000/FBEM61", "bfdbusr", "Misys123$");
		Statement stmt = con.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("Select " + "*" + " from WASADMIN.ATMTRANSACTIONCODES");
			ResultSetMetaData rsmt = rs.getMetaData();
			int columnNumber = rsmt.getColumnCount();
			for (int i = 1; i <= columnNumber; i++) {
				String columnValue = rs.getString(i);
				System.out.println(rsmt.getColumnName(i) + " ===" + columnValue);
			}

		} catch (Exception e) {
			System.out.println(e.toString());
		}

	}

}
