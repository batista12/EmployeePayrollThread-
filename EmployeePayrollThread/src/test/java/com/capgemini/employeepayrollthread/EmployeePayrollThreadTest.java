package com.capgemini.employeepayrollthread;
import org.junit.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

public class EmployeePayrollThreadTest {

	@Test
	public void given3Employees_StoreToFile_ShouldPassTest() {
		ArrayList<EmployeePayRoll> empPayRoll = new ArrayList<EmployeePayRoll>();
		empPayRoll.add(new EmployeePayRoll(1, "Bill Gates", 1000000));
		empPayRoll.add(new EmployeePayRoll(2, "Mark Zuckerburg", 500000));
		EmployeePayService empPayRollService = new EmployeePayService(empPayRoll);
		empPayRollService.writeData("File");
		empPayRollService.printData("File");
		int entries = empPayRollService.noOfEntries("File");
		boolean result = entries == 2 ? true : false;
		Assert.assertTrue(result);
	}

	@Test
	public void readingFromFile_NoOfEntries_ShouldMatchActual() {
		EmployeePayService empPayRollService = new EmployeePayService();
		int entries;
		try {
			entries = empPayRollService.readData("File");
			boolean result = entries == 2 ? true : false;
			Assert.assertTrue(result);
		} catch (CustomSQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void readingFromDB_NoOfEntries_ShouldMatchActual() {
		EmployeePayService empPayRollService = new EmployeePayService();
		int entries;
		try {
			entries = empPayRollService.readData("DB");
			boolean result = entries == 7 ? true : false;
			Assert.assertTrue(result);
		} catch (CustomSQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void givenNewSalary_UpdatinginDB_ShouldMatch() {
		EmployeePayService empPayRollService = new EmployeePayService();
		try {
			int entries = empPayRollService.readData("DB");
			empPayRollService.updateSalary(1, "Bill Gates", 90000.0);
			boolean result = empPayRollService.checkDBInSyncWithList("Bill Gates");
			Assert.assertTrue(result);
		} catch (CustomSQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void givenNewSalary_UpdatinginDB_UsingPreparedStatement_ShouldMatch() {
		EmployeePayService empPayRollService = new EmployeePayService();
		try {
			int entries = empPayRollService.readData("DB");
			empPayRollService.updateSalary(2, "Bill Gates", 80000.0);
			boolean result = empPayRollService.checkDBInSyncWithList("Bill Gates");
			Assert.assertTrue(result);
		} catch (CustomSQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void giveName_RetreiveDataFromDB_UsingPreparedStatement_ShouldMatch() {
		EmployeePayRoll employee;
		LocalDate startDate = LocalDate.parse("2020-04-29");
		EmployeePayService empPayRollService = new EmployeePayService();
		try {
			int entries = empPayRollService.readData("DB");
			employee = empPayRollService.preparedStatementReadData("Mark");
			boolean result = employee.getStartDate().contains(startDate);
			Assert.assertTrue(result);
		} catch (CustomSQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void giveDateRange_RetreiveDataFromDB_UsingPreparedStatement_ShouldMatch() {
		List<EmployeePayRoll> empPayRoll = new ArrayList<EmployeePayRoll>();
		EmployeePayService empPayRollService = new EmployeePayService();
		try {
			empPayRoll = empPayRollService.getDataInDateRange("2010-04-29", "2018-04-29");
			boolean result = empPayRoll.size() == 3;
			Assert.assertTrue(result);
		} catch (CustomSQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void findMinMaxSumAvgcount_GroupedByGender_ShouldMatch() {
		EmployeePayService empPayRollService = new EmployeePayService();
		try {
			HashMap<String, Double> output = empPayRollService.getMinMaxSumAvgCount();
			boolean result = output.get("minMale").equals(60000.0) && output.get("maxMale").equals(200000.0)
					&& output.get("sumMale").equals(660000.0) && output.get("avgMale").equals(110000.0)
					&& output.get("minFemale").equals(90000.0) && output.get("sumFemale").equals(90000.0)
					&& output.get("countMale").equals(6.0) && output.get("countFemale").equals(1.0);
			Assert.assertTrue(result);
		} catch (CustomSQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void givenNewEmployee_WhenAdded_ShouldSyncWithDB() {
		EmployeePayService empPayRollService = new EmployeePayService();
		try {
			int entries = empPayRollService.readData("DB");
			LocalDate startDate = LocalDate.parse("2008-09-01");
			int companyId = 3;
			String departmentName = "Management";
			empPayRollService.addEmployeeAndPayRoll("Surya", "M", 100000.0, companyId, Arrays.asList(departmentName),
					Arrays.asList(startDate));
			boolean result = empPayRollService.checkDBInSyncWithList("Surya");
			Assert.assertTrue(result);
		} catch (CustomSQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void givenNewEmployee_WhenAddedWithPayrollData_ShouldSyncWithDB() {
		EmployeePayService empPayRollService = new EmployeePayService();
		try {
			int entries = empPayRollService.readData("DB");
			LocalDate startDate = LocalDate.parse("2016-11-04");
			int companyId = 3;
			String departmentName = "Sales";
			empPayRollService.addEmployeeAndPayRoll("Suraj", "M", 60000.0, companyId, Arrays.asList(departmentName),
					Arrays.asList(startDate));
			boolean result = empPayRollService.checkDBInSyncWithList("Suraj");
			Assert.assertTrue(result);
		} catch (CustomSQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void givenNewEmployee_WhenAddedWithPayrollDataNewERDiagram_ShouldSyncWithDB() {
		EmployeePayService empPayRollService = new EmployeePayService();
		try {
			int entries = empPayRollService.readData("DB");
			LocalDate startDate = LocalDate.parse("2019-01-29");
			int companyId = 4;
			String departmentName = "Sales";
			empPayRollService.addEmployeeAndPayRoll("Rita", "F", 200000.0, companyId, Arrays.asList(departmentName),
					Arrays.asList(startDate));
			boolean result = empPayRollService.checkDBInSyncWithList("Rita");
			Assert.assertTrue(result);
		} catch (CustomSQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void givenEmployeeName_ShouldRemoveFromListAndDB() {
		EmployeePayService empPayRollService = new EmployeePayService();
		List<EmployeePayRoll> empPayRoll = new ArrayList<EmployeePayRoll>();
		try {
			int entries = empPayRollService.readData("DB");
			System.out.println("before " + entries);
			empPayRollService.deleteEmployee("Rita");
			int entrie = empPayRollService.readData("DB");
			System.out.println("after " + entrie);
			boolean result = empPayRollService.checkIFDeletedFromList("Rita");
			Assert.assertFalse(result);
		} catch (CustomSQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void givenMultipleEmployee_WhenAddedToDB_ShouldMatchCountOfEntries() {
		EmployeePayService empPayRollService = new EmployeePayService();
		try {
			int entries = empPayRollService.readData("DB");
			String departmentName = "Management";
			EmployeePayRoll[] employeeArray = {
					new EmployeePayRoll(0, "Manasi", "F", 130000.0, 3, Arrays.asList(departmentName),
							Arrays.asList(LocalDate.parse("2018-05-21"))),
					new EmployeePayRoll(0, "rinal", "F", 100000.0, 3, Arrays.asList(departmentName),
							Arrays.asList(LocalDate.parse("2016-07-29"))),
					new EmployeePayRoll(0, "Riya", "F", 70000.0, 4, Arrays.asList(departmentName),
							Arrays.asList(LocalDate.parse("2015-08-07"))),
					new EmployeePayRoll(0, "Kunal", "M", 60000.0, 3, Arrays.asList(departmentName),
							Arrays.asList(LocalDate.parse("2017S-04-29"))) };
			Instant start = Instant.now();
			int countOfEntries = empPayRollService.addEmployeeAndPayRoll(Arrays.asList(employeeArray));
			Instant end = Instant.now();
			System.out.println("Duration without Thread : " + Duration.between(start, end));
			boolean result = countOfEntries == 7 ? true : false;
			Assert.assertTrue(result);
		} catch (CustomSQLException e) {
			e.printStackTrace();
		}
	}
		@Test
		public void givenMultipleEmployeeWithThreads_WhenAddedToDB_ShouldMatchCountOfEntries() {
			EmployeePayService empPayRollService = new EmployeePayService();
			try {
				int entries = empPayRollService.readData("DB");
				String departmentName = "Management";
				EmployeePayRoll[] employeeArray = {
						new EmployeePayRoll(0, "Ranju", "F", 400000.0, 3, Arrays.asList(departmentName),
								Arrays.asList(LocalDate.parse("2008-05-01"))),
						new EmployeePayRoll(0, "Kanika", "F", 200000.0, 3, Arrays.asList(departmentName),
								Arrays.asList(LocalDate.parse("2011-07-29"))),
						new EmployeePayRoll(0, "Sunita", "F", 90000.0, 4, Arrays.asList(departmentName),
								Arrays.asList(LocalDate.parse("2017-07-07"))),
						new EmployeePayRoll(0, "Soni", "F", 60000.0, 3, Arrays.asList(departmentName),
								Arrays.asList(LocalDate.parse("2020-04-29"))) };
				Instant start = Instant.now();
				empPayRollService.addEmployeeAndPayRoll(Arrays.asList(employeeArray));
				Instant end = Instant.now();
				System.out.println("Duration without Thread : " + Duration.between(start, end));
				Instant startThread = Instant.now();
				int countOfEntries = empPayRollService.addEmployeeAndPayRollWithThread(Arrays.asList(employeeArray));
				Instant endThread = Instant.now();
				System.out.println("Duration with Thread : " + Duration.between(startThread, endThread));
				boolean result = countOfEntries == 11 ? true : false;
				Assert.assertTrue(result);
			} catch (CustomSQLException e) {
				e.printStackTrace();
			}
		}
	
	}
	



