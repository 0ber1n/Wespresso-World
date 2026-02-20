package io.wespresso_world.wespresso_world;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import io.wespresso_world.wespresso_world.drinks.Drinks;
import io.wespresso_world.wespresso_world.drinks.DrinksRepository;	

@SpringBootApplication
public class WespressoWorldApplication {

	public static void main(String[] args) {
		SpringApplication.run(WespressoWorldApplication.class, args);
	}

	@Bean
	CommandLineRunner initDatabase(DrinksRepository DrinksRepository) {
		return args -> {
			// Initialize the database with some sample data
			DrinksRepository.save(new Drinks(null, "Espresso", "Strong and bold", 2.50));
			DrinksRepository.save(new Drinks(null, "Latte", "Smooth and creamy", 3.50));
			DrinksRepository.save(new Drinks(null, "Cappuccino", "Rich and frothy", 3.00));
		};
	}

}
