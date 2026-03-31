package edu.brandeis.cosi103a.groupb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.databind.Module;

@SpringBootApplication(scanBasePackages = "edu.brandeis.cosi103a.groupb")
public class App {

	/**
	* To test:
	* curl -X POST -H "Content-Type: application/json" -d '{"maxes": [500, 10]}' http://localhost:8080/generate
	*/
	public static void main(String[] args) {
		System.out.println("Hi from the server!");
		SpringApplication.run(App.class, args);
	}

    @Bean
    public Module guavaModule() {
        return new GuavaModule();
    }
}
