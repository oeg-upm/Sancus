package sancus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableAutoConfiguration
public class SancusProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(SancusProjectApplication.class, args);
	}

}
