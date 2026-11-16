package com.hob.hobsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class HobsystemApplication {

	public static void main(String[] args) {
		// All dates and times in the system use Philippine time, even when the server (like Render) runs in UTC.
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Manila"));
		SpringApplication.run(HobsystemApplication.class, args);
	}

}
