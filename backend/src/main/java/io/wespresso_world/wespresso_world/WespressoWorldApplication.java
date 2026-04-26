package io.wespresso_world.wespresso_world;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.wespresso_world.wespresso_world.drinks.Drinks;
import io.wespresso_world.wespresso_world.drinks.DrinksRepository;
import io.wespresso_world.wespresso_world.user.User;
import io.wespresso_world.wespresso_world.beans.Beans;
import io.wespresso_world.wespresso_world.beans.BeansRepository;
import io.wespresso_world.wespresso_world.user.UserRepository;
import io.wespresso_world.wespresso_world.VulnConfig;


@SpringBootApplication
public class WespressoWorldApplication {



	public static void main(String[] args) {
		SpringApplication.run(WespressoWorldApplication.class, args);
	}

	@Bean
	CommandLineRunner initDatabase(
			DrinksRepository drinksRepository, 
			BeansRepository beansRepository, 
			UserRepository userRepository, 
			PasswordEncoder passwordEncoder, 
			VulnConfig VulnConfig
		) {
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
			
				// Seed admin user: Initialize the database with an admin user for testing
				if (userRepository.findByUsername("admin").isEmpty()) {
					User admin = new User();
					admin.setUsername("admin");
					admin.setEmail("admin@admin.com");
					admin.setPassword(passwordEncoder.encode("admin123"));
					admin.setRole(User.Role.admin);
					userRepository.save(admin);
				}

				// Seed vulnerable user for SQLi login vuln
				if (VulnConfig.getSqliLogin().isEnabled()) {
					if (userRepository.findByUsername("steve").isEmpty()) {
						User steve = new User();
						steve.setUsername("steve");
						steve.setPassword(passwordEncoder.encode("admin123"));
						steve.setEmail("wes{$ql1_1nj3ct10n_w1ns}");
						steve.setRole(User.Role.user);
						userRepository.save(steve);
					}
				}
			
			};
	}

}


