package io.wespresso_world.wespresso_world;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import java.nio.file.Files;
import java.nio.file.Path;

import io.wespresso_world.wespresso_world.drinks.Drinks;
import io.wespresso_world.wespresso_world.drinks.DrinksRepository;
import io.wespresso_world.wespresso_world.user.User;
import io.wespresso_world.wespresso_world.beans.Beans;
import io.wespresso_world.wespresso_world.beans.BeansRepository;
import io.wespresso_world.wespresso_world.user.UserRepository;
import io.wespresso_world.wespresso_world.cart.Cart;
import io.wespresso_world.wespresso_world.cart.CartItem;
import io.wespresso_world.wespresso_world.cart.CartRepository;
import io.wespresso_world.wespresso_world.cart.CartItemRepository;


@SpringBootApplication
public class WespressoWorldApplication {

	@Value("${ADMIN_PASSWORD:admin1234}")
	private String adminPassword;

	public static void main(String[] args) {
		SpringApplication.run(WespressoWorldApplication.class, args);
	}

	@Bean
	CommandLineRunner initDatabase(
			DrinksRepository drinksRepository,
			BeansRepository beansRepository,
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			VulnConfig VulnConfig,
			FlagConfig flagConfig,
			CartRepository cartRepository,
			CartItemRepository cartItemRepository
		) {
			return args -> {
				try {
					Files.writeString(Path.of("/flag.txt"), flagConfig.getSsti() + "\n");
				} catch (Exception ignored) {
					// Only writable in container context
				}
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
					admin.setPassword(passwordEncoder.encode(adminPassword));
					admin.setRole(User.Role.admin);
					userRepository.save(admin);
				}

				// Seed vulnerable user for SQLi login vuln
				if (VulnConfig.getSqliLogin().isEnabled()) {
					if (userRepository.findByUsername("steve").isEmpty()) {
						User steve = new User();
						steve.setUsername("steve");
						steve.setPassword(passwordEncoder.encode(adminPassword));
						steve.setEmail(flagConfig.getSqli());
						steve.setRole(User.Role.user);
						userRepository.save(steve);
					}
				}

				// Seed carts and cart items for testing
				if (userRepository.findByUsername("admin").isPresent()) {
            		User admin = userRepository.findByUsername("admin").get();

					// Only seed if admin has no cart yet (idempotent)
					if (cartRepository.findByUserId(admin.getId()).isEmpty()) {
						Cart adminCart = new Cart();
						adminCart.setCustomerName("admin");
						adminCart.setUserId(admin.getId());
						cartRepository.save(adminCart);

						CartItem flagItem = new CartItem();
						flagItem.setItemName(flagConfig.getIdorCart());
						flagItem.setPrice(0.00);
						flagItem.setQuantity(1);
						flagItem.setCategory("special");
						flagItem.setCart(adminCart);
						cartItemRepository.save(flagItem);
					}
				}
			};
	}

}


