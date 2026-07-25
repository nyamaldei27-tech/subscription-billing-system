package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling// This activates scheduling capabilities/ Enables spring's background task execution
public class DemoApplication {

	public static void main(String[] args){
		SpringApplication.run(DemoApplication.class, args);
	}
}



