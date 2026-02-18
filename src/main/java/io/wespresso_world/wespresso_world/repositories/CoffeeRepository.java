package io.wespresso_world.wespresso_world.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import io.wespresso_world.wespresso_world.models.Coffee;

public interface CoffeeRepository extends JpaRepository<Coffee, Long> {
    
}
