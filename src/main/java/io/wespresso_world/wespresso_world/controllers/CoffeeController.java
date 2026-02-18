package io.wespresso_world.wespresso_world.controllers;

import io.wespresso_world.wespresso_world.models.Coffee;
import io.wespresso_world.wespresso_world.repositories.CoffeeRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/menu")
public class CoffeeController {
    private final CoffeeRepository coffeeRepository;

    public CoffeeController(CoffeeRepository coffeeRepository) {
        this.coffeeRepository = coffeeRepository;
    }

    @GetMapping
    public List<Coffee> getAllCoffees() {
        return coffeeRepository.findAll();
    }

    @PostMapping
    public Coffee createCoffee(@RequestBody Coffee coffee) {
        return coffeeRepository.save(coffee);
    }   
    
    @DeleteMapping("/{id}")
    public void deleteCoffee(@PathVariable Long id) {
        coffeeRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    public Coffee updateCoffee(@PathVariable Long id, @RequestBody Coffee updatedCoffee) {
        return coffeeRepository.findById(id)
                .map(coffee -> {
                    coffee.setName(updatedCoffee.getName());
                    coffee.setDescription(updatedCoffee.getDescription());
                    coffee.setPrice(updatedCoffee.getPrice());
                    return coffeeRepository.save(coffee);
                })
                .orElseGet(() -> {
                    return coffeeRepository.save(updatedCoffee);
                });
    }
    
    @PatchMapping("/{id}")
    public Coffee partialUpdateCoffee(@PathVariable Long id, @RequestBody Coffee updatedCoffee) {
        return coffeeRepository.findById(id)
                .map(coffee -> {
                    if (updatedCoffee.getName() != null) {
                        coffee.setName(updatedCoffee.getName());
                    }
                    if (updatedCoffee.getDescription() != null) {
                        coffee.setDescription(updatedCoffee.getDescription());
                    }
                    if (updatedCoffee.getPrice() != null) {
                        coffee.setPrice(updatedCoffee.getPrice());
                    }
                    return coffeeRepository.save(coffee);
                })
                .orElseGet(() -> {
                    return coffeeRepository.save(updatedCoffee);
                });
    }



}
