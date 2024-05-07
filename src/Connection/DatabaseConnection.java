package Connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

	static String currentDirectory = System.getProperty("user.dir");
	static String databasePath = currentDirectory + "\\resources\\library.db";

	private static final String DATABASE_URL = "jdbc:sqlite:" + databasePath;

	public Connection getConnection() {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(DATABASE_URL);
		} catch (SQLException e) {
			System.err.println("Error al conectar con la base de datos: " + e.getMessage());
		}
		return conn;
	}

	public void closeResultSet(ResultSet resultSet) {
		try {
			if (resultSet != null) {
				resultSet.close();
			}
		} catch (SQLException e) {
			System.err.println("Error al cerrar el ResultSet: " + e.getMessage());
		}
	}

	public void closeStatement(Statement statement) {
		try {
			if (statement != null) {
				statement.close();
			}
		} catch (SQLException e) {
			System.err.println("Error al cerrar el Statement: " + e.getMessage());
		}
	}

	public void closeConnection(Connection connection) {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			System.err.println("Error al cerrar la conexión: " + e.getMessage());
		}
	}

}
