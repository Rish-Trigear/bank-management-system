package com.bank.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Bank Management System
 * 
 * This is the entry point of the Spring Boot application.
 * @SpringBootApplication annotation enables:
 * - Auto-configuration: Automatically configures Spring based on dependencies
 * - Component scanning: Scans for Spring components in this package and sub-packages
 * - Configuration: Allows defining beans and configuration
 */
@SpringBootApplication
public class BankManagementSystemApplication {

	/**
	 * Main method to start the Spring Boot application
	 * Starts embedded Tomcat server on port 8080 (default)
	 */
	public static void main(String[] args) {
		SpringApplication.run(BankManagementSystemApplication.class, args);
	}

}

