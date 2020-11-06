package com.capgemini.employeepayrollthread;
import java.time.LocalDate;
import java.util.*;

public class EmployeePayService {
	private static List<EmployeePayRoll> empPayRollList;
	private static EmployeePayrollDBService EmployeePayrollDBService;
	private static Scanner sc = new Scanner(System.in);

	public EmployeePayService(List<EmployeePayRoll> empPayRollList) {
		this();
		this.empPayRollList = empPayRollList;
	}

	public EmployeePayService() {
		EmployeePayrollDBService = EmployeePayrollDBService.getInstance();
	}

	public static void main(String[] args) {
		ArrayList<EmployeePayRoll> empPayRollList = new ArrayList<EmployeePayRoll>();
		EmployeePayService empPayRollService = new EmployeePayService(empPayRollList);

		empPayRollService.readData();

		empPayRollService.writeData("Console");

	}

	public static void readData() {
		System.out.println("Please Enter the following details :");
		System.out.println("Enter ID");
		int id = sc.nextInt();
		sc.nextLine();
		System.out.println("Enter Name :");
		String name = sc.nextLine();
		System.out.println("Enter Salary :");
		double salary = sc.nextDouble();

		EmployeePayRoll empPayRollObject = new EmployeePayRoll(id, name, salary);
		empPayRollList.add(empPayRollObject);
	}

	public int readData(String source) throws CustomSQLException {
		if (source.equals("File")) {
			empPayRollList = new EmployeeFile().readData();
			return empPayRollList.size();
		} else if (source.equals("DB")) {
			empPayRollList = EmployeePayrollDBService.readData();
			return empPayRollList.size();
		}
		return 0;
	}

	public void updateSalary(int n, String name, Double salary) throws CustomSQLException {
		int success = EmployeePayrollDBService.updateSalary(n, name, salary);
		if (success == 1) {
			for (EmployeePayRoll e : empPayRollList) {
				if (e.getName().equals(name)) {
					e.setSalary(salary);
				}
			}
		}
	}

	public void writeData(String destination) {
		if (destination.equals("Console"))
			System.out.println("Employee Pay Roll Data : \n" + empPayRollList.get(0).toString());
		else if (destination.equals("File"))
			new EmployeeFile().writeData(empPayRollList);
	}

	public int noOfEntries(String destination) {
		if (destination.equals("Console"))
			return empPayRollList.size();
		else if (destination.equals("File"))
			return new EmployeeFile().noOfEntries();
		return 0;
	}

	public void printData(String destination) {
		if (destination.equals("Console"))
			for (EmployeePayRoll e : empPayRollList) {
				System.out.println(e.toString() + "\n");
			}
		else if (destination.equals("File"))
			new EmployeeFile().printData();

	}

	public boolean checkDBInSyncWithList(String name) throws CustomSQLException {
		EmployeePayRoll employeeDB = EmployeePayrollDBService.getEmployee(name);
		EmployeePayRoll employeeList = getEmployee(name);
		return employeeDB.equals(employeeList);
	}

	private EmployeePayRoll getEmployee(String name) {
		return empPayRollList.stream().filter(e -> e.getName().equals(name)).findFirst().orElse(null);
	}

	public EmployeePayRoll preparedStatementReadData(String name) throws CustomSQLException {
		return EmployeePayrollDBService.preparedStatementReadData(name);
	}

	public List<EmployeePayRoll> getDataInDateRange(String startDate, String endDate) throws CustomSQLException {
		return EmployeePayrollDBService.getDataInDateRange(startDate, endDate);
	}

	public HashMap<String, Double> getMinMaxSumAvgCount() throws CustomSQLException {
		return EmployeePayrollDBService.getMinMaxSumAvgCount();
	}

	public void addEmployee(String name, String gender, double salary, LocalDate startDate) throws CustomSQLException {
		EmployeePayRoll employee = EmployeePayrollDBService.addEmployee(name, gender, salary, startDate);
		if (employee != null)
			empPayRollList.add(employee);
	}

	public void addEmployeeAndPayRoll(String name, String gender, double salary, int companyId,
			List<String> departmentName, List<LocalDate> startDate) throws CustomSQLException {
		EmployeePayRoll employee = EmployeePayrollDBService.addEmployeeAndPayRoll(name, gender, salary, companyId,
				departmentName, startDate);
		if (employee != null)
			empPayRollList.add(employee);
	}

	public void deleteEmployee(String name) throws CustomSQLException {
		empPayRollList = EmployeePayrollDBService.deleteEmployee(name);
	}

	public boolean checkIFDeletedFromList(String name) {
		boolean result = false;
		for (EmployeePayRoll e : empPayRollList) {
			if (e.getName().equals(name)) {
				System.out.println(e.getName());
				result = true;
			}
		}
		return result;
	}

	public int addEmployeeAndPayRoll(List<EmployeePayRoll> employeeList) {

		employeeList.forEach(e -> {
			System.out.println("Employee adding : " + e.getName());
			try {
				this.addEmployeeAndPayRoll(e.name, e.gender, e.salary, e.companyId, e.departmentName, e.startDate);
			} catch (CustomSQLException e1) {
				e1.printStackTrace();
			}
			System.out.println("Employee added : " + e.getName());
		});
		System.out.println(empPayRollList.size());
		return empPayRollList.size();
	}

	public int addEmployeeAndPayRollWithThread(List<EmployeePayRoll> employeeList) {

		HashMap<Integer, Boolean> additionStatus = new HashMap<Integer, Boolean>();
		employeeList.forEach(e -> {
			Runnable task = () -> {
				additionStatus.put(e.hashCode(), false);
				System.out.println("Employee adding : " + Thread.currentThread().getName());
				try {
					this.addEmployeeAndPayRoll(e.name, e.gender, e.salary, e.companyId, e.departmentName, e.startDate);
				} catch (CustomSQLException e1) {
					e1.printStackTrace();
				}
				additionStatus.put(e.hashCode(), true);
				System.out.println("Employee added : " + Thread.currentThread().getName());
			};

			Thread thread = new Thread(task, e.name);
			thread.start();
		});

		while (additionStatus.containsValue(false)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return empPayRollList.size();
	}

}
