package com.spring.batch.demo.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.spring.batch.demo.model.Employee;

public class EmployeeDataProcessor implements ItemProcessor<Employee, Employee> {

	private static final Logger log = LoggerFactory.getLogger(EmployeeDataProcessor.class);

	@Override
	public Employee process(Employee employee) throws Exception {
		log.info("Converting (" + employee + ")");
		employee.setName(employee.getName().toUpperCase());
		log.info("into (" + employee + ")");
		return employee;
	}

	
}
