package com.capgemini.employeepayrollthread;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class EmployeePayRollDBService {

	private int connectionCounter = 0;
	private PreparedStatement preparedStatement;

	private static EmployeePayRollDBService employeePayRollDBService;

	private EmployeePayRollDBService() {
	}

	public static EmployeePayRollDBService getInstance() {
		if (employeePayRollDBService == null)
			employeePayRollDBService = new EmployeePayRollDBService();
		return employeePayRollDBService;
	}

	public List<EmployeePayRoll> readData() throws CustomSQLException {
		List<EmployeePayRoll> employeePayRollList = new ArrayList<EmployeePayRoll>();
		String query = "select * from employee_payroll where is_active = true";
		Statement statement;
		ResultSet result = null;
		try (Connection connection = this.getConnection();) {
			statement = connection.createStatement();
			result = statement.executeQuery(query);
			employeePayRollList = getDatafromResultset(result);
		} catch (SQLException e) {
			throw new CustomSQLException(e.getMessage(), CustomSQLException.Exception_Type.READ_FAILED);
		}
		return employeePayRollList;
	}

	public static int updateSalary(int n, String name, Double salary) throws CustomSQLException {
		if (n == 1)
			return this.updateSalaryUsingStatement(name, salary);
		else
			return this.updateSalaryUsingPreparedStatement(name, salary);
	}

	private int updateSalaryUsingPreparedStatement(String name, Double salary) throws CustomSQLException {
		try (Connection connection = this.getConnection();) {
			preparedStatement = connection
					.prepareStatement("update employee_payroll set salary = ? where name = ? and is_active = true");
			preparedStatement.setDouble(1, salary);
			preparedStatement.setString(2, name);
			int result = preparedStatement.executeUpdate();
			return result;
		} catch (SQLException e) {
			throw new CustomSQLException(e.getMessage(), CustomSQLException.Exception_Type.UPDATE_FAILED);
		}
	}

	private int updateSalaryUsingStatement(String name, Double salary) throws CustomSQLException {
		String query = String.format(
				"update employee_payroll set salary = %.2f where name = '%s' and is_active = true ;", salary, name);
		Statement statement;
		int result = 0;
		try (Connection connection = this.getConnection();) {
			statement = connection.createStatement();
			result = statement.executeUpdate(query);
			return result;
		} catch (SQLException e) {
			throw new CustomSQLException(e.getMessage(), CustomSQLException.Exception_Type.UPDATE_FAILED);
		}
	}

	public EmployeePayRoll preparedStatementReadData(String name) throws CustomSQLException {
		List<EmployeePayRoll> employeePayRollList = new ArrayList<EmployeePayRoll>();
		try (Connection connection = this.getConnection();) {
			preparedStatement = connection
					.prepareStatement("select * from  employee_payroll where name = ? and is_active = true");
			preparedStatement.setString(1, name);
			ResultSet result = preparedStatement.executeQuery();
			employeePayRollList = getDatafromResultset(result);
			return employeePayRollList.get(0);
		} catch (SQLException e) {
			throw new CustomSQLException(e.getMessage(), CustomSQLException.Exception_Type.READ_FAILED);
		}
	}

	public List<EmployeePayRoll> getDataInDateRange(String startDate, String endDate) throws CustomSQLException {
		List<EmployeePayRoll> employeePayRollList = new ArrayList<EmployeePayRoll>();
		try (Connection connection = this.getConnection();) {
			preparedStatement = connection.prepareStatement(
					"select * from employee_payroll where is_active = true and id in (select emp_id from employee_department where start_date between cast(? as date) and cast(? as date))");
			preparedStatement.setString(1, startDate);
			preparedStatement.setString(2, endDate);
			ResultSet result = preparedStatement.executeQuery();
			employeePayRollList = getDatafromResultset(result);
			return employeePayRollList;
		} catch (SQLException e) {
			throw new CustomSQLException(e.getMessage(), CustomSQLException.Exception_Type.READ_IN_DATERANGE_FAILED);
		}
	}

	public HashMap<String, Double> getMinMaxSumAvgCount() throws CustomSQLException {
		HashMap<String, Double> functionMap = new HashMap<String, Double>();
		List<Double> min = getDataBasedOnQuery(
				"select min(salary),gender from employee_payroll where is_active = true group by gender");
		List<Double> max = getDataBasedOnQuery(
				"select max(salary),gender from employee_payroll where is_active = true group by gender");
		List<Double> sum = getDataBasedOnQuery(
				"select sum(salary),gender from employee_payroll where is_active = true group by gender");
		List<Double> avg = getDataBasedOnQuery(
				"select avg(salary),gender from employee_payroll where is_active = true group by gender;");
		List<Double> count = getDataBasedOnQuery(
				"select count(*),gender from employee_payroll where is_active = true group by gender;");
		functionMap.put("minMale", min.get(0));
		functionMap.put("minFemale", min.get(1));
		functionMap.put("maxMale", max.get(0));
		functionMap.put("maxFemale", max.get(1));
		functionMap.put("sumMale", sum.get(0));
		functionMap.put("sumFemale", sum.get(1));
		functionMap.put("avgMale", avg.get(0));
		functionMap.put("avgFemale", avg.get(1));
		functionMap.put("countMale", count.get(0));
		functionMap.put("countFemale", count.get(1));
		return functionMap;
	}

	public List<Double> getDataBasedOnQuery(String query) throws CustomSQLException {
		List<Double> functionList = new ArrayList<Double>();
		Statement statement;
		ResultSet result;
		try (Connection connection = this.getConnection();) {
			statement = connection.createStatement();
			result = statement.executeQuery(query);
			int i = 0;
			while (result.next()) {
				i = i + 1;
				functionList.add(result.getDouble(1));
			}
			return functionList;
		} catch (SQLException e) {
			throw new CustomSQLException(e.getMessage(), CustomSQLException.Exception_Type.QUERY_FAILED);
		}
	}

	public EmployeePayRoll addEmployee(String name, String gender, double salary, LocalDate startDate)
			throws CustomSQLException {
		int employeeId = 0;
		EmployeePayRoll employee = null;
		String query = String.format(
				"insert into employee_payroll(name,gender,salary,startdate) " + "values('%s','%s',%s,'%s')", name,
				gender, salary, startDate);
		try (Connection connection = this.getConnection();) {
			Statement statement = connection.createStatement();
			int rowAffected = statement.executeUpdate(query, statement.RETURN_GENERATED_KEYS);
			if (rowAffected == 1) {
				ResultSet result = statement.getGeneratedKeys();
				if (result.next())
					employeeId = result.getInt(1);
			}
			return employee;
		} catch (SQLException e) {
			throw new CustomSQLException(e.getMessage(), CustomSQLException.Exception_Type.ADD_FAILED);
		}
	}

	private void insertIntoEmployeeDepartment(Connection connection, int employeeId, int departmentId,
			LocalDate startDate) throws CustomSQLException {
		String query = String.format(
				"insert into employee_department(emp_id,dept_id,start_date) " + "values('%s',%s,'%s')", employeeId,
				departmentId, startDate);
		try (Statement statement = connection.createStatement();) {
			int rowAffected = statement.executeUpdate(query);
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new CustomSQLException(e.getMessage(), CustomSQLException.Exception_Type.ADD_FAILED);
		}
	}

	private boolean insertIntoPayrollTable(Connection connection, double salary, int employeeId)
			throws CustomSQLException {
		try (Statement statement = connection.createStatement();) {
			double basic_pay = salary;
			double deductions = salary * 0.2;
			double taxablePay = salary - deductions;
			double tax = taxablePay * 0.1;
			double netPay = salary - tax;
			String query = String
					.format("insert into payroll_details(emp_id,basic_pay,deductions,taxable_pay,tax,net_pay) "
							+ "values(%s,%s,%s,%s,%s,%s)", employeeId, basic_pay, deductions, taxablePay, tax, netPay);
			int rowAffected = statement.executeUpdate(query);
			if (rowAffected == 1) {
				return true;
			}
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new CustomSQLException(e.getMessage(), CustomSQLException.Exception_Type.ADD_FAILED);
		}
		return false;
	}

	public EmployeePayRoll addEmployeeAndPayRoll(String name, String gender, double salary, int companyId,
			List<String> departments, List<LocalDate> dates) throws CustomSQLException {
		HashMap<Integer, Boolean> additionStatus = new HashMap<Integer, Boolean>();
		String departmentName = departments.get(0);
		LocalDate startDate[] = { dates.get(0) };
		Double[] salaryArray = { salary };
		int employeeId[] = { 0 };
		int departmentId[] = { 0 };
		String query;
		boolean[] outcome = { false };
		EmployeePayRoll[] employee = new EmployeePayRoll[1];
		Connection[] connection = new Connection[1];
		try {
			connection[0] = this.getConnection();
			connection[0].setAutoCommit(false);
		} catch (SQLException e2) {
			e2.printStackTrace();
		}

		query = String.format(
				"insert into employee_payroll(name,gender,salary,company_id) " + "values('%s','%s',%s,'%s')", name,
				gender, salary, companyId);
		try (Statement statement = connection[0].createStatement();) {

			int rowAffected = statement.executeUpdate(query, statement.RETURN_GENERATED_KEYS);
			if (rowAffected == 1) {
				ResultSet result = statement.getGeneratedKeys();
				if (result.next()) {
					employeeId[0] = result.getInt(1);
				}
			}
		} catch (SQLException e) {
			try {
				connection[0].rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new CustomSQLException(e.getMessage(), CustomSQLException.Exception_Type.ADD_FAILED);
		}

		try {
			query = String.format("select * from department where name = '%s'", departmentName);
			Statement statement = connection[0].createStatement();
			ResultSet result3 = statement.executeQuery(query);
			while (result3.next()) {
				departmentId[0] = (result3.getInt("id"));
			}
		} catch (SQLException e2) {
			try {
				connection[0].rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new CustomSQLException(e2.getMessage(), CustomSQLException.Exception_Type.ADD_FAILED);
		}

		additionStatus.put(1, false);

		Runnable task1 = () -> {
			try {
				insertIntoEmployeeDepartment(connection[0], employeeId[0], departmentId[0], startDate[0]);
				additionStatus.put(1, true);
			} catch (CustomSQLException e) {
				e.printStackTrace();
			}
		};
		Thread thread1 = new Thread(task1, name + "3");
		thread1.start();

		additionStatus.put(2, false);

		Runnable task2 = () -> {
			try {
				outcome[0] = insertIntoPayrollTable(connection[0], salaryArray[0], employeeId[0]);
				if (outcome[0] == true) {
					List<String> departmentNameList = new ArrayList<String>();
					departmentNameList.add(departmentName);
					List<LocalDate> startDates = new ArrayList<LocalDate>();
					startDates.add(startDate[0]);
					employee[0] = new EmployeePayRoll(employeeId[0], name, gender, salary, companyId,
							departmentNameList, startDates);
				}
				additionStatus.put(2, true);
			} catch (CustomSQLException e) {
				e.printStackTrace();
			}
		};
		Thread thread2 = new Thread(task2, name + "4");
		thread2.start();

		while (additionStatus.containsValue(false)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		try {
			connection[0].commit();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			if (connection[0] != null)
				try {
					connection[0].close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		return employee[0];
	}

	public List<EmployeePayRoll> deleteEmployee(String name) throws CustomSQLException {
		List<EmployeePayRoll> employeePayRollList = new ArrayList<EmployeePayRoll>();
		String query = String.format("update employee_payroll set is_active = false where name = '%s'", name);
		Statement statement;
		int result = 0;
		try (Connection connection = this.getConnection();) {
			statement = connection.createStatement();
			result = statement.executeUpdate(query);
			employeePayRollList = readData();
			for (int i = 0; i < employeePayRollList.size(); i++) {
				EmployeePayRoll employee = employeePayRollList.get(i);
				if (employee.getName().equals(name)) {
					employeePayRollList.remove(employee);
				}
			}
			return employeePayRollList;
		} catch (SQLException e) {
			throw new CustomSQLException(e.getMessage(), CustomSQLException.Exception_Type.READ_FAILED);
		}
	}

	private synchronized Connection getConnection() throws CustomSQLException {
		connectionCounter += 1;
		String jdbcURL = "jdbc:mysql://localhost:3306/employee_payroll_service?useSSL=false";
		String userName = "root";
		String password = "manasi@1998";
		System.out
				.println("Processing Thread : " + Thread.currentThread().getName() + " with Id : " + connectionCounter);
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(jdbcURL, userName, password);
		} catch (SQLException e) {
			throw new CustomSQLException(e.getMessage(), CustomSQLException.Exception_Type.CONNECTION_FAILED);
		}
		System.out.println("Processing Thread : " + Thread.currentThread().getName() + " with Id : " + connectionCounter
				+ " " + connection + " Connection Successfully Established");
		return connection;
	}

	public EmployeePayRoll getEmployee(String name) throws CustomSQLException {
		List<EmployeePayRoll> employeePayRollList = new ArrayList<EmployeePayRoll>();
		employeePayRollList = this.readData();
		return employeePayRollList.stream().filter(e -> e.getName().equals(name)).findFirst().orElse(null);
	}

	public List<EmployeePayRoll> getDatafromResultset(ResultSet result) throws CustomSQLException {

		List<EmployeePayRoll> employeePayRollList = new ArrayList<EmployeePayRoll>();
		try {
			while (result.next()) {
				List<LocalDate> startDates = new ArrayList<LocalDate>();
				List<Integer> department_id = new ArrayList<Integer>();
				List<String> department_name = new ArrayList<String>();
				try (Connection connection = this.getConnection();) {
					PreparedStatement preparedStatement1 = connection
							.prepareStatement("select * from employee_department where emp_id = ?");
					preparedStatement1.setInt(1, result.getInt("id"));
					ResultSet result1 = preparedStatement1.executeQuery();

					while (result1.next()) {
						startDates.add(result1.getDate("start_date").toLocalDate());
						department_id.add(result1.getInt("dept_id"));
					}

					for (int i : department_id) {
						PreparedStatement preparedStatement2 = connection
								.prepareStatement("select * from department where id = ?");
						preparedStatement2.setInt(1, i);
						ResultSet result2 = preparedStatement2.executeQuery();
						while (result2.next()) {
							department_name.add(result2.getString("name"));
						}
					}
				}
				employeePayRollList.add(
						new EmployeePayRoll(result.getInt("id"), result.getString("name"), result.getString("gender"),
								result.getDouble("salary"), result.getInt("company_id"), department_name, startDates));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayRollList;
	}
	public int updateSalaryInPayrollTable(String name, Double salary) {
		HashMap<Integer, Boolean> additionStatus = new HashMap<Integer, Boolean>();
		int success[] = { 0 };
		additionStatus.put(1, false);
		Runnable task1 = () -> {
			try {
				success[0] = this.updateSalaryUsingPreparedStatement(name, salary);
				additionStatus.put(1, true);
			} catch (CustomSQLException e) {
				e.printStackTrace();
			}
		};
		Thread thread1 = new Thread(task1, name + "1");
		thread1.start();

		additionStatus.put(2, false);
		Runnable task2 = () -> {
			try {
				updateSalaryInPayRollTable(name, salary);
				additionStatus.put(2, true);
			} catch (CustomSQLException e) {
				e.printStackTrace();
			}
		};
		Thread thread2 = new Thread(task2, name + "2");
		thread2.start();

		while (additionStatus.containsValue(false)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return success[0];
	}

	private void updateSalaryInPayRollTable(String name, Double salary) throws CustomSQLException {
		Connection connection = null;
		String query = String.format("select * from employee_payroll where is_active = true and name = '%s' ", name);
		int employeeId = 0;
		ResultSet result = null;
		try {
			connection = this.getConnection();
			connection.setAutoCommit(false);
		} catch (SQLException e2) {
			e2.printStackTrace();
		}

		try (Statement statement = connection.createStatement();) {
			result = statement.executeQuery(query);
			while (result.next()) {
				employeeId = result.getInt("Id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try (Statement statement = connection.createStatement();) {
			double basic_pay = salary;
			double deductions = salary * 0.2;
			double taxablePay = salary - deductions;
			double tax = taxablePay * 0.1;
			double netPay = salary - tax;
			query = String
					.format("update payroll_details set basic_pay= %s,deductions=%s,taxable_pay=%s,tax=%s,net_pay=%s "
							+ "where emp_id = %s", basic_pay, deductions, taxablePay, tax, netPay, employeeId);
			int rowAffected = statement.executeUpdate(query);

		} catch (SQLException e) {
			throw new CustomSQLException(e.getMessage(), CustomSQLException.Exception_Type.UPDATE_FAILED);
		}

		try {
			connection.commit();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}

	}

}