package com.spring.batch.demo.listener;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.spring.batch.demo.model.Employee;

@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

	private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public JobCompletionNotificationListener(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
			log.info("!!! JOB FINISHED! Time to verify the results");

			List<Employee> results = jdbcTemplate.query("SELECT name, age, address FROM employee",
					new RowMapper<Employee>() {
						@Override
						public Employee mapRow(ResultSet rs, int row) throws SQLException {
							Employee employee = new Employee();
							employee.setName(rs.getString("name"));
							employee.setAge(rs.getString("age"));
							employee.setAddress(rs.getString("address"));
							return employee;
						}
					});

			for (Employee employee : results) {
				log.info("Found <" + employee + "> in the database.");
			}

		}
	}
}
