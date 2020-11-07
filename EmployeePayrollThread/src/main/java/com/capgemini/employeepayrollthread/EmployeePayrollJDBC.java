package com.capgemini.employeepayrollthread;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import com.mysql.cj.jdbc.Driver;

public class EmployeePayrollJDBC {
	static String url = "jdbc:mysql://localhost:3306/employee_payroll?useSSL=false";
	static String userName = "root";
	static String password = "Manasi@1998";
	private static Connection con = null;
	/**
	 * @return
	 */
	public static Connection getConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection(url, userName, password);
			System.out.println("Connection Successful");

		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("cannot find the driver");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		listAllDrivers();
		try {
			System.out.println("Connecting to database" + url);
			con = DriverManager.getConnection(url, userName, password);
			System.out.println("Connected successfully " + con);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return con;
	}
	private static void listAllDrivers() {
		Enumeration<java.sql.Driver> driverList = DriverManager.getDrivers();
		while (driverList.hasMoreElements()) {
			Driver driver = (Driver) driverList.nextElement();
			System.out.println(" " + driver.getClass().getName());
		}
	}
}
