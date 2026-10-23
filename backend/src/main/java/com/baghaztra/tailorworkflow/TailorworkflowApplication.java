package com.baghaztra.tailorworkflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(excludeName = "org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration")
public class TailorworkflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(TailorworkflowApplication.class, args);
	}

}
