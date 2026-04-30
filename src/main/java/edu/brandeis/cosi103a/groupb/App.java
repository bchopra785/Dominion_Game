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

    @Bean
    public ParentPlayer strategyPlayerBean() {
        // Determine which strategy to use from environment variable
        String strategy = System.getenv("PLAYER_STRATEGY");
        if (strategy == null || strategy.isEmpty()) {
            strategy = "V3"; // default to V3
        }

        System.out.println("Initializing PlayerServer with strategy: " + strategy);

        switch (strategy.toUpperCase()) {
            case "V2":
                return new V2StrategyPlayer("V2StrategyPlayer");
            case "V3":
                return new V3StrategyPlayer("V3StrategyPlayer");
            case "BIGMONEY":
                return new BigMoneyPlayer("BigMoneyPlayer");
            default:
                System.out.println("Unknown strategy: " + strategy + ". Defaulting to V3.");
                return new V3StrategyPlayer("V3StrategyPlayer");
        }
    }
}
