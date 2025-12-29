package classes;

import java.sql.Connection;
import java.sql.DriverManager;

public class Connect {

	private static Connection con;
	static {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			System.out.println("Driver ok");
			// TODO: Remplacer par votre configuration MySQL
			String url = "jdbc:mysql://localhost:3306/project_management?useSSL=false&serverTimezone=UTC";
			String user = "root";
			String password = ""; // Changez selon votre configuration
			con = DriverManager.getConnection(url, user, password);
			System.out.println("Connected to database");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Connection getConnection() {
		return con;
	}
}