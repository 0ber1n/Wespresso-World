package io.wespresso_world.wespresso_world;

import io.wespresso_world.wespresso_world.models.Coffee;
import io.wespresso_world.wespresso_world.repositories.CoffeeRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;	

@SpringBootApplication
public class WespressoWorldApplication {

	public static void main(String[] args) {
		SpringApplication.run(WespressoWorldApplication.class, args);
	}

	@Bean
	CommandLineRunner initDatabase(CoffeeRepository coffeeRepository) {
		return args -> {
			// Initialize the database with some sample data
			coffeeRepository.save(new Coffee(null, "Espresso", "Strong and bold", 2.50));
			coffeeRepository.save(new Coffee(null, "Latte", "Smooth and creamy", 3.50));
			coffeeRepository.save(new Coffee(null, "Cappuccino", "Rich and frothy", 3.00));
		};
	}

}
