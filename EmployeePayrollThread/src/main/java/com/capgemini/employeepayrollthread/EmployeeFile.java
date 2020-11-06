package com.capgemini.employeepayrollthread;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.*;
import java.nio.file.*;

public class EmployeeFile {

	public String DATA_FILE = "PayRollData.txt";

	EmployeeFile() {

	}

	public void writeData(List<EmployeePayRoll> empPayRollList) {
		StringBuffer data = new StringBuffer();
		for (EmployeePayRoll e : empPayRollList) {
			data.append(e.toString() + "\n");
		}
		Path filePath = Paths.get(DATA_FILE);
		try {
			Files.write(filePath, data.toString().getBytes());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public int noOfEntries() {
		int count = 0;
		Path filePath = Paths.get(DATA_FILE);
		try {
			count = (int) Files.lines(filePath).count();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return count;
	}

	public void printData() {
		Path filePath = Paths.get(DATA_FILE);
		try {
			Stream<String> stringStream = Files.lines(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<EmployeePayRoll> readData() {
		List<String[]> listOfElements = null;
		List<EmployeePayRoll> empPayRoll = new ArrayList<EmployeePayRoll>();
		Path filePath = Paths.get(DATA_FILE);
		String[] data = new String[3];
		int i = 0;
		try {
			Stream<String> stringStr = Files.lines(filePath);
			listOfElements = stringStr.map(s -> s.split(", ")).collect(Collectors.toList());
			for (String[] e : listOfElements) {
				for (String s : e) {
					data[i] = e[i].split("=")[1];
					i += 1;
				}
				i = 0;
				empPayRoll.add(new EmployeePayRoll(Integer.parseInt(data[0]), data[1], Double.parseDouble(data[2])));
			}
			return empPayRoll;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

}