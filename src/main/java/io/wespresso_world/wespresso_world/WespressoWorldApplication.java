package io.wespresso_world.wespresso_world;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import io.wespresso_world.wespresso_world.drinks.Drinks;
import io.wespresso_world.wespresso_world.drinks.DrinksRepository;	
import io.wespresso_world.wespresso_world.beans.Beans;
import io.wespresso_world.wespresso_world.beans.BeansRepository;

@SpringBootApplication
public class WespressoWorldApplication {

	public static void main(String[] args) {
		SpringApplication.run(WespressoWorldApplication.class, args);
	}

	@Bean
	CommandLineRunner initDatabase(DrinksRepository drinksRepository, BeansRepository beansRepository) {
		return args -> {
			// Seed drinks: Initialize the database with some sample data
			drinksRepository.save(new Drinks(null, "Espresso", "Strong and bold", 2.50));
			drinksRepository.save(new Drinks(null, "Latte", "Smooth and creamy", 3.50));
			drinksRepository.save(new Drinks(null, "Cappuccino", "Rich and frothy", 3.00));
		
		    // Seed beans: Initialize the database with some sample data
		    beansRepository.save(new Beans(null, "Get Pwned", "Smooth and sweet", "Ethiopia", "Medium", false, 15.00));
			beansRepository.save(new Beans(null, "Green Get Pwned", "Smooth and sweet", "Ethiopia", null, true, 5.00));
			beansRepository.save(new Beans(null, "Rooted", "Citrusy and bright", "Brazil", "Light", false, 16.00));
			beansRepository.save(new Beans(null, "Green Rooted", "Citrusy and bright", "Brazil", null, true, 5.50));
		};
	}

}


