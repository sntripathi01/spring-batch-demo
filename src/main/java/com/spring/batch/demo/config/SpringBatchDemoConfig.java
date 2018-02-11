package com.spring.batch.demo.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.spring.batch.demo.listener.JobCompletionNotificationListener;
import com.spring.batch.demo.model.Employee;
import com.spring.batch.demo.processor.EmployeeDataProcessor;

@Configuration
@EnableBatchProcessing
public class SpringBatchDemoConfig {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Bean
	public EmployeeDataProcessor processor() {
		return new EmployeeDataProcessor();
	}

	/*
	 * @Bean
	 * 
	 * @ConfigurationProperties("spring.datasource") public DataSource dataSource()
	 * { return DataSourceBuilder.create().build(); }
	 */

	

	@Bean
	public JdbcTemplate getJdbcTemplate() {
		return new JdbcTemplate(dataSource());
	}
	
	


	@Bean
	public StaxEventItemReader<Employee> reader() throws IOException {

		StaxEventItemReader<Employee> xmlFileReader = new StaxEventItemReader<>();
		Resource resource = new ClassPathResource("employee.xml");
		BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
		reader.lines().forEach(System.out::println);
		xmlFileReader.setResource(resource);
		xmlFileReader.setFragmentRootElementName("emp");
		Jaxb2Marshaller employeeMarshaller = new Jaxb2Marshaller();
		employeeMarshaller.setClassesToBeBound(Employee.class);
		xmlFileReader.setUnmarshaller(employeeMarshaller);
		return xmlFileReader;

	}

	@Bean
	 public JdbcBatchItemWriter<Employee> writer() {
		JdbcBatchItemWriter<Employee> writer = new JdbcBatchItemWriter<Employee>();
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Employee>());
		writer.setSql("INSERT INTO employee (name, age,address) VALUES (:name, :age, :address)");
		writer.setDataSource(dataSource());
		return writer;
	}

	@Bean
	public Job importUserJob(JobCompletionNotificationListener listener) throws IOException {
		return jobBuilderFactory.get("importUserJob").incrementer(new RunIdIncrementer()).listener(listener)
				.flow(step1()).end().build();
	}

	@Bean
	public Step step1() throws IOException {
		return stepBuilderFactory.get("step1").<Employee, Employee>chunk(10).reader(reader()).processor(processor())
				.writer(writer()).build();
	}

	@Bean
	public DataSource dataSource() {

		// no need shutdown, EmbeddedDatabaseFactoryBean will take care of this
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		EmbeddedDatabase db = builder.setType(EmbeddedDatabaseType.H2).build();
		return db;
	}


}