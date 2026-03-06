package io.wespresso_world.wespresso_world.drinks;

import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Drinks Menu API", description = "Endpoints for managing the drink menu") // Adds OpenAPI tag for grouping endpoints in documentation
@RestController
@RequestMapping("/menu")
public class DrinksController {
    private final DrinksRepository drinksRepository;

    public DrinksController(DrinksRepository DrinksRepository) {
        this.drinksRepository = DrinksRepository;
    }

    @Operation(summary = "Get all drinks", description = "Returns a list of all drinks in the menu") // Adds OpenAPI operation summary and description for API documentation
    @GetMapping
    public List<Drinks> getAllDrinkss() {
        return drinksRepository.findAll();
    }

    @Operation(summary = "Create a new drink", description = "Creates a new drink in the menu") // Adds OpenAPI operation summary and description for API documentation
    @PostMapping
    public Drinks createDrinks(@RequestBody Drinks drinks) {
        return drinksRepository.save(drinks);
    }   
    
    @Operation(summary = "Delete a drink", description = "Deletes a drink from the menu") // Adds OpenAPI operation summary and description for API documentation
    @DeleteMapping("/{id}")
    public void deleteDrinks(@PathVariable Long id) {
        drinksRepository.deleteById(id);
    }


    // PUT is for full updates, PATCH is for partial updates. PUT nulls out values not included in body.
    
    @Operation(summary = "Update a drink", description = "Updates an existing drink in the menu") // Adds OpenAPI operation summary and description for API documentation
    @PutMapping("/{id}")
    public Drinks updateDrinks(@PathVariable Long id, @RequestBody Drinks updatedDrinks) {
        return drinksRepository.findById(id)
                .map(Drinks -> {
                    Drinks.setName(updatedDrinks.getName());
                    Drinks.setDescription(updatedDrinks.getDescription());
                    Drinks.setPrice(updatedDrinks.getPrice());
                    return drinksRepository.save(Drinks);
                })
                .orElseGet(() -> {
                    return drinksRepository.save(updatedDrinks);
                });
    }
    
    @Operation(summary = "Partially update a drink", description = "Updates only the specified fields of an existing drink in the menu") // Adds OpenAPI operation summary and description for API documentation
    @PatchMapping("/{id}")
    public Drinks partialUpdateDrinks(@PathVariable Long id, @RequestBody Drinks updatedDrinks) {
        return drinksRepository.findById(id)
                .map(Drinks -> {
                    if (updatedDrinks.getName() != null) {
                        Drinks.setName(updatedDrinks.getName());
                    }
                    if (updatedDrinks.getDescription() != null) {
                        Drinks.setDescription(updatedDrinks.getDescription());
                    }
                    if (updatedDrinks.getPrice() != null) {
                        Drinks.setPrice(updatedDrinks.getPrice());
                    }
                    return drinksRepository.save(Drinks);
                })
                .orElseGet(() -> {
                    return drinksRepository.save(updatedDrinks);
                });
    }



}
