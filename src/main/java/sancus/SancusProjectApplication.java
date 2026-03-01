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
		Keys.loadFromJson();

		for (String arg : args) {
			if (arg.equalsIgnoreCase("--demo")) {
				Keys.setDemoMode(true);
			} else if (arg.startsWith("--ethkey=")) {
				String value = arg.substring("--ethkey=".length());
				if (value.startsWith("\"") && value.endsWith("\"")) {
					value = value.substring(1, value.length() - 1);
				}
				Keys.setEthKey(value);
			} else if (arg.startsWith("--etherscankey=")) {
				String value = arg.substring("--etherscankey=".length());
				if (value.startsWith("\"") && value.endsWith("\"")) {
					value = value.substring(1, value.length() - 1);
				}
				Keys.setEtherscanKey(value);
			}
		}

		if (Keys.isDemoMode()) {
			System.out.println("===========================================");
			System.out.println("  SANCUS - Demo Mode Enabled");
			System.out.println("===========================================");
		}

		SpringApplication.run(SancusProjectApplication.class, args);
	}

}
